package com.nathan.jooq.controller;

import com.nathan.jooq.dto.*;
import com.nathan.jooq.service.BookService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService service;
    public BookController(BookService service) { this.service = service; }

    @PostMapping
    public BookDTO create(@RequestBody BookCreateRequest req) throws BadRequestException {
        return service.create(req);
    }

    @PutMapping("/{id}/price")
    public Long updatePrice(@PathVariable Long id, @RequestParam BigDecimal newPrice) {
        return service.updatePrice(id, newPrice);
    }

    @GetMapping("/{id}")
    public BookDTO getById(@PathVariable Long id) {
        return service.getBookById(id);
    }

    @GetMapping("/search")
    public PageResult<BookDTO> search(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer publishedYear,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(author, publishedYear, minPrice, maxPrice, page, size);
    }

    @PutMapping("/update-price-by-author")
    public ResponseEntity<Map<String, Object>> updatePrice(@RequestBody UpdateBookPriceRequest req) {
        int count = service.updateBookPriceByAuthor(req);
        return ResponseEntity.ok(Map.of(
                "updatedCount", count,
                "message", "Updated prices for " + count + " books"
        ));
    }
}
