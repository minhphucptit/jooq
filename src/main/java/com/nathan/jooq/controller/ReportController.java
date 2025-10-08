package com.nathan.jooq.controller;

import com.nathan.jooq.dto.AuthorRankingDTO;
import com.nathan.jooq.dto.AuthorReportDTO;
import com.nathan.jooq.dto.BookYearReportDTO;
import com.nathan.jooq.dto.CountryRankingDTO;
import com.nathan.jooq.dto.InactiveAuthorDTO;
import com.nathan.jooq.dto.YearlyStatsDTO;
import com.nathan.jooq.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/yearly")
    public List<YearlyStatsDTO> yearly(@RequestParam(defaultValue = "2000") int minPublishedYear) {
        return service.yearlyStats(minPublishedYear);
    }

    @GetMapping("/authors/value")
    public List<AuthorReportDTO> reportAuthorBookValue() {
        return service.reportAuthorBookValue();
    }

    @GetMapping("/books/year")
    public List<BookYearReportDTO> reportBookByYear() {
        return service.reportBookByYear();
    }

    @GetMapping("/authors/top-avg")
    public List<AuthorReportDTO> topAuthorsByAvgPrice() {
        return service.topAuthorsByAvgPrice();
    }

    @GetMapping("/countries/rank")
    public List<CountryRankingDTO> rankCountriesByBookValue() {
        return service.rankCountriesByBookValue();
    }

    @GetMapping("/authors/inactive-authors")
    public List<InactiveAuthorDTO> findInactiveAuthors(
            @RequestParam(defaultValue = "2") int yearsThreshold
    ) {
        return service.findInactiveAuthors(yearsThreshold);
    }

    @GetMapping("/authors/author-ranking")
    public List<AuthorRankingDTO> getRankings() {
        return service.getAuthorRankings();
    }
}
