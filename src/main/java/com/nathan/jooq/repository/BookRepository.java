package com.nathan.jooq.repository;

import com.nathan.jooq.repository.entity.BookEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookEntity> findByTitle(String title);
}
