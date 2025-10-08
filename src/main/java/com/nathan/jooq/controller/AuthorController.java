package com.nathan.jooq.controller;

import com.nathan.jooq.dto.AuthorDTO;
import com.nathan.jooq.dto.AuthorRevenueDTO;
import com.nathan.jooq.service.AuthorService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/authors")
public class AuthorController {
    private final AuthorService service;
    public AuthorController(AuthorService service) { this.service = service; }

    @PostMapping
    public AuthorDTO create(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam(required = false) LocalDate birthDate,
                            @RequestParam(required = false) String country) {
        return service.create(name, email, birthDate, country);
    }

    @GetMapping("/top-revenue")
    public List<AuthorRevenueDTO> topRevenue(@RequestParam(defaultValue = "5") int limit) {
        return service.topAuthorsByRevenue(limit);
    }
}
