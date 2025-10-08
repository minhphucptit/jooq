package com.nathan.jooq.service;

import com.nathan.jooq.dto.AuthorDTO;
import com.nathan.jooq.dto.AuthorRevenueDTO;
import com.nathan.jooq.generated.tables.Author;
import com.nathan.jooq.generated.tables.Book;
import com.nathan.jooq.generated.tables.BookAuthor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthorService {
    private final DSLContext ctx;

    public AuthorService(DSLContext ctx) {
        this.ctx = ctx;
    }

    @Transactional
    public AuthorDTO create(String name, String email, LocalDate birthDate, String country) {
        var rec = ctx.insertInto(Author.AUTHOR)
                .columns(Author.AUTHOR.NAME, Author.AUTHOR.EMAIL, Author.AUTHOR.BIRTH_DATE, Author.AUTHOR.COUNTRY)
                .values(name, email, birthDate, country)
                .returning(Author.AUTHOR.ID, Author.AUTHOR.CREATED_AT)
                .fetchOne();

        return new AuthorDTO(
                rec.get(Author.AUTHOR.ID),
                name,
                email,
                birthDate,
                country,
                rec.get(Author.AUTHOR.CREATED_AT),
                null
        );
    }

    public List<AuthorRevenueDTO> topAuthorsByRevenue(int limit) {
        return ctx.select(Author.AUTHOR.NAME, DSL.sum(Book.BOOK.PRICE).as("total_revenue"))
                .from(Author.AUTHOR)
                .join(BookAuthor.BOOK_AUTHOR).on(Author.AUTHOR.ID.eq(BookAuthor.BOOK_AUTHOR.AUTHOR_ID))
                .join(Book.BOOK).on(Book.BOOK.ID.eq(BookAuthor.BOOK_AUTHOR.BOOK_ID))
                .groupBy(Author.AUTHOR.NAME)
                .orderBy(DSL.sum(Book.BOOK.PRICE).desc())
                .limit(limit)
                .fetch(r -> new AuthorRevenueDTO(
                        r.get(Author.AUTHOR.NAME),
                        r.get("total_revenue", BigDecimal.class)
                ));
    }
}
