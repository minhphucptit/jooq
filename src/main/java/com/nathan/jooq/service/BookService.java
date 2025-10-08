package com.nathan.jooq.service;

import com.nathan.jooq.dto.AuthorDTO;
import com.nathan.jooq.dto.BookCreateRequest;
import com.nathan.jooq.dto.BookDTO;
import com.nathan.jooq.dto.PageResult;
import com.nathan.jooq.dto.UpdateBookPriceRequest;
import com.nathan.jooq.generated.tables.records.BookAuthorRecord;
import com.nathan.jooq.generated.tables.records.BookRecord;
import com.nathan.jooq.repository.BookRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nathan.jooq.generated.tables.Author.AUTHOR;
import static com.nathan.jooq.generated.tables.Book.BOOK;
import static com.nathan.jooq.generated.tables.BookAuthor.BOOK_AUTHOR;

@Service
@RequiredArgsConstructor
public class BookService {
    private final DSLContext ctx;
    private final BookRepository bookRepository;

    @Transactional
    public BookDTO create(BookCreateRequest req) throws BadRequestException {

        if (bookRepository.findByTitle(req.title()).isPresent()) {
            throw new BadRequestException("Title already exists");
        }
        // Insert book
        Long bookId = ctx.insertInto(BOOK)
                .columns(BOOK.TITLE, BOOK.ISBN, BOOK.PUBLISHED_YEAR, BOOK.PRICE)
                .values(req.title(), req.isbn(), req.publishedYear(), req.price())
                .returning(BOOK.ID)
                .fetchOne()
                .get(BOOK.ID);

        // Insert relations (book_author)
        if (req.authors() != null && !req.authors().isEmpty()) {
            ctx.batchInsert(req.authors().stream()
                    .map(a -> new BookAuthorRecord(
                            bookId,
                            a.authorId(),
                            a.contribution(),
                            LocalDateTime.now()
                    ))
                    .toList()
            ).execute();
        }

        return getBookById(bookId);
    }

    @Transactional
    public Long updatePrice(Long id, BigDecimal newPrice) {
        BookRecord book = ctx.selectFrom(BOOK)
                .where(BOOK.ID.eq(id))
                .forUpdate()
                .fetchOne();
        Objects.requireNonNull(book, "Book not found with id: " + id);

        if(book.getPrice().compareTo(newPrice) != 0) {
            book.setPrice(newPrice);
            book.setUpdatedAt(LocalDateTime.now());
        }
        book.store();
        return book.getId();
    }

    @Transactional
    public int updateBookPriceByAuthor(UpdateBookPriceRequest req) {
        Condition condition = DSL.noCondition();

        if (req.getAuthorName() != null && !req.getAuthorName().isBlank()) {
            condition = condition.and(AUTHOR.NAME.likeIgnoreCase("%" + req.getAuthorName() + "%"));
        }
        if (req.getAuthorCountry() != null && !req.getAuthorCountry().isBlank()) {
            condition = condition.and(AUTHOR.COUNTRY.likeIgnoreCase("%" + req.getAuthorCountry() + "%"));
        }

        BigDecimal multiplier = BigDecimal.ONE.add(req.getPercent().divide(BigDecimal.valueOf(100)));

        int updated = ctx.update(BOOK)
                .set(BOOK.PRICE, BOOK.PRICE.mul(multiplier))
                .where(BOOK.ID.in(
                        ctx.select(BOOK_AUTHOR.BOOK_ID)
                                .from(BOOK_AUTHOR)
                                .join(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                                .where(AUTHOR.NAME.eq(req.getAuthorName()))
                ))
                .execute();

        return updated;
    }

    public BookDTO getBookById(Long id) {
        var b = ctx.selectFrom(BOOK)
                .where(BOOK.ID.eq(id))
                .fetchOne();

        if (b == null) return null;

        List<AuthorDTO> authors = ctx.select(
                        AUTHOR.ID,
                        AUTHOR.NAME,
                        AUTHOR.EMAIL,
                        AUTHOR.BIRTH_DATE,
                        AUTHOR.COUNTRY,
                        AUTHOR.CREATED_AT,
                        BOOK_AUTHOR.CONTRIBUTION)
                .from(BOOK_AUTHOR)
                .join(AUTHOR).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHOR.ID))
                .where(BOOK_AUTHOR.BOOK_ID.eq(id))
                .fetch(r -> new AuthorDTO(
                        r.get(AUTHOR.ID),
                        r.get(AUTHOR.NAME),
                        r.get(AUTHOR.EMAIL),
                        r.get(AUTHOR.BIRTH_DATE),
                        r.get(AUTHOR.COUNTRY),
                        r.get(AUTHOR.CREATED_AT),
                        r.get(BOOK_AUTHOR.CONTRIBUTION)
                ));

        return new BookDTO(
                b.get(BOOK.ID),
                b.get(BOOK.TITLE),
                b.get(BOOK.ISBN),
                b.get(BOOK.PUBLISHED_YEAR),
                b.get(BOOK.PRICE),
                b.get(BOOK.CREATED_AT),
                authors
        );
    }

    @Transactional(readOnly = true)
    public PageResult<BookDTO> search(String authorName, Integer publishedYear, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Condition condition = buildCondition(authorName, publishedYear, minPrice, maxPrice);

        // base + count query
        var baseQuery = buildBaseQuery(authorName);
        var countQuery = buildCountQuery(authorName);

        Integer total = countQuery.where(condition).fetchOne(0, Integer.class);
        // Get paging result
        Result<? extends Record> books = baseQuery
                .where(condition)
                .groupBy(BOOK.ID)
                .orderBy(BOOK.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        if (books.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), page, size, total == null ? 0 : total);
        }

        // Enrich Author data for each Book
        List<BookDTO> content = enrichAuthors(books);

        return new PageResult<>(content, page, size, total == null ? 0 : total);
    }

    private Condition buildCondition(String authorName, Integer publishedYear,
                                     BigDecimal minPrice, BigDecimal maxPrice) {
        Condition condition = DSL.noCondition();

        if (StringUtils.isNotBlank(authorName)) {
            condition = condition.and(AUTHOR.NAME.like("%" + authorName + "%"));
        }
        if (publishedYear != null) {
            condition = condition.and(BOOK.PUBLISHED_YEAR.eq(publishedYear));
        }
        if (minPrice != null) {
            condition = condition.and(BOOK.PRICE.ge(minPrice));
        }
        if (maxPrice != null) {
            condition = condition.and(BOOK.PRICE.le(maxPrice));
        }
        return condition;
    }

    private SelectJoinStep<? extends Record> buildBaseQuery(String authorName) {
        var base = ctx.select(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.PUBLISHED_YEAR, BOOK.PRICE, BOOK.CREATED_AT)
                .from(BOOK);
        if (StringUtils.isNotBlank(authorName)) {
            base = base.join(BOOK_AUTHOR).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                    .join(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID));
        }
        return base;
    }

    private SelectJoinStep<Record1<Integer>> buildCountQuery(String authorName) {
        var countQuery = ctx.select(DSL.countDistinct(BOOK.ID))
                .from(BOOK);
        if (StringUtils.isNotBlank(authorName)) {
            countQuery = countQuery.join(BOOK_AUTHOR).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                    .join(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID));
        }
        return countQuery;
    }

    private List<BookDTO> enrichAuthors(Result<? extends Record> books) {
        List<Long> bookIds = books.stream()
                .map(r -> r.get(BOOK.ID))
                .toList();

        //Enrich Author data for each Book
        Map<Long, List<AuthorDTO>> authorsMap = ctx.select(
                        BOOK_AUTHOR.BOOK_ID,
                        AUTHOR.ID,
                        AUTHOR.NAME,
                        AUTHOR.EMAIL,
                        AUTHOR.BIRTH_DATE,
                        AUTHOR.COUNTRY,
                        AUTHOR.CREATED_AT,
                        BOOK_AUTHOR.CONTRIBUTION)
                .from(BOOK_AUTHOR)
                .join(AUTHOR).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHOR.ID))
                .where(BOOK_AUTHOR.BOOK_ID.in(bookIds))
                .fetchGroups(
                        r -> r.get(BOOK_AUTHOR.BOOK_ID),
                        r -> new AuthorDTO(
                                r.get(AUTHOR.ID),
                                r.get(AUTHOR.NAME),
                                r.get(AUTHOR.EMAIL),
                                r.get(AUTHOR.BIRTH_DATE),
                                r.get(AUTHOR.COUNTRY),
                                r.get(AUTHOR.CREATED_AT),
                                r.get(BOOK_AUTHOR.CONTRIBUTION)
                        )
                );

        return books.stream().map(r -> {
            Long id = r.get(BOOK.ID);
            List<AuthorDTO> a = authorsMap.getOrDefault(id, Collections.emptyList());
            return new BookDTO(
                    id,
                    r.get(BOOK.TITLE),
                    r.get(BOOK.ISBN),
                    r.get(BOOK.PUBLISHED_YEAR),
                    r.get(BOOK.PRICE),
                    r.get(BOOK.CREATED_AT),
                    a
            );
        }).toList();
    }

}
