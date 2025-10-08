package com.nathan.jooq.service;

import com.nathan.jooq.dto.AuthorRankingDTO;
import com.nathan.jooq.dto.AuthorReportDTO;
import com.nathan.jooq.dto.BookYearReportDTO;
import com.nathan.jooq.dto.CountryRankingDTO;
import com.nathan.jooq.dto.InactiveAuthorDTO;
import com.nathan.jooq.dto.YearlyStatsDTO;
import com.nathan.jooq.generated.tables.Book;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

import static com.nathan.jooq.generated.Tables.AUTHOR;
import static com.nathan.jooq.generated.Tables.BOOK;
import static com.nathan.jooq.generated.Tables.BOOK_AUTHOR;

@Service
public class ReportService {
    private final DSLContext ctx;
    public ReportService(DSLContext ctx) { this.ctx = ctx; }

    /** Report yearly total books and average price */
    public List<YearlyStatsDTO> yearlyStats(int minPublishedYear) {
        return ctx.select(Book.BOOK.PUBLISHED_YEAR.as("year"),
                        DSL.count().as("total_books"),
                        DSL.avg(Book.BOOK.PRICE).as("avg_price"))
                .from(Book.BOOK)
                .where(Book.BOOK.PUBLISHED_YEAR.ge(minPublishedYear))
                .and(Book.BOOK.PUBLISHED_YEAR.isNotNull())
                .groupBy(Book.BOOK.PUBLISHED_YEAR)
                .orderBy(Book.BOOK.PUBLISHED_YEAR.desc())
                .fetch(r -> new YearlyStatsDTO(
                        r.get("year", Integer.class),
                        r.get("total_books", Integer.class),
                        r.get("avg_price", BigDecimal.class)
                ));
    }

    /** Report total books and total value per author */
    public List<AuthorReportDTO> reportAuthorBookValue() {
        return ctx.select(
                        AUTHOR.ID,
                        AUTHOR.NAME,
                        DSL.countDistinct(BOOK.ID).as("total_books"),
                        DSL.sum(BOOK.PRICE).as("total_value"))
                .from(AUTHOR)
                .join(BOOK_AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                .join(BOOK).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                .groupBy(AUTHOR.ID, AUTHOR.NAME)
                .orderBy(DSL.sum(BOOK.PRICE).desc())
                .fetchInto(AuthorReportDTO.class);
    }

    /** Report total books and total value by publication year */
    public List<BookYearReportDTO> reportBookByYear() {
        return ctx.select(
                        BOOK.PUBLISHED_YEAR,
                        DSL.count().as("total_books"),
                        DSL.sum(BOOK.PRICE).as("total_value"))
                .from(BOOK)
                .groupBy(BOOK.PUBLISHED_YEAR)
                .orderBy(BOOK.PUBLISHED_YEAR)
                .fetchInto(BookYearReportDTO.class);
    }

    /** Report top 5 authors by average book price */
    public List<AuthorReportDTO> topAuthorsByAvgPrice() {
        return ctx.select(
                        AUTHOR.ID,
                        AUTHOR.NAME,
                        DSL.count(BOOK.ID).as("total_books"),
                        DSL.avg(BOOK.PRICE).as("avg_price"))
                .from(AUTHOR)
                .join(BOOK_AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                .join(BOOK).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                .groupBy(AUTHOR.ID, AUTHOR.NAME)
                .orderBy(DSL.avg(BOOK.PRICE).desc())
                .limit(5)
                .fetchInto(AuthorReportDTO.class);
    }

    /** Rank countries by total book market value */
    public List<CountryRankingDTO> rankCountriesByBookValue() {
        Field<BigDecimal> totalValue = DSL.sum(BOOK.PRICE).as("total_value");
        Field<Integer> rank = DSL.rank().over().orderBy(DSL.sum(BOOK.PRICE).desc()).as("rank");

        return ctx.select(
                        AUTHOR.COUNTRY,
                        totalValue,
                        rank)
                .from(AUTHOR)
                .join(BOOK_AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                .join(BOOK).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                .groupBy(AUTHOR.COUNTRY)
                .orderBy(totalValue.desc())
                .fetchInto(CountryRankingDTO.class);
    }

    /**
     * Find authors who have never published a book OR haven't published in the last N years.
     */
    public List<InactiveAuthorDTO> findInactiveAuthors(int yearsThreshold) {
        int cutoffYear = Year.now().getValue() - yearsThreshold;

        // Authors with no books
        var noBookAuthors = ctx.select(AUTHOR.ID, AUTHOR.NAME, AUTHOR.COUNTRY,
                        DSL.inline(-9999).as("last_published_year"),
                        DSL.inline("NO_BOOK").as("reason"))
                .from(AUTHOR)
                .where(AUTHOR.ID.notIn(
                        DSL.selectDistinct(BOOK_AUTHOR.AUTHOR_ID).from(BOOK_AUTHOR)
                ));

        // Authors with old books only
        var oldBooksAuthors = ctx.select(AUTHOR.ID, AUTHOR.NAME, AUTHOR.COUNTRY,
                        DSL.max(BOOK.PUBLISHED_YEAR).as("last_published_year"),
                        DSL.inline("OLD_BOOKS").as("reason"))
                .from(AUTHOR)
                .join(BOOK_AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                .join(BOOK).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                .where(BOOK.PUBLISHED_YEAR.lt(cutoffYear))
                .groupBy(AUTHOR.ID, AUTHOR.NAME, AUTHOR.COUNTRY);

        return noBookAuthors.unionAll(oldBooksAuthors)
                .orderBy(AUTHOR.NAME.asc())
                .fetchInto(InactiveAuthorDTO.class);
    }

    /**
     * Top authors ranked by total book price (sales value).
     */
    public List<AuthorRankingDTO> getAuthorRankings() {
        Field<BigDecimal> totalValue = DSL.sum(BOOK.PRICE).as("total_value");
        Field<Integer> rank = DSL.rank().over(DSL.orderBy(DSL.sum(BOOK.PRICE).desc())).as("rank");
        return ctx.select(AUTHOR.ID, AUTHOR.NAME, AUTHOR.COUNTRY,
                        totalValue,
                        rank
                )
                .from(AUTHOR)
                .join(BOOK_AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                .join(BOOK).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                .groupBy(AUTHOR.ID, AUTHOR.NAME, AUTHOR.COUNTRY)
                .orderBy(totalValue.desc())
                .fetchInto(AuthorRankingDTO.class);
    }
}
