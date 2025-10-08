create table jooq.author
(
    id         bigint auto_increment
        primary key,
    name       varchar(255)                        null,
    email      varchar(255)                        null,
    birth_date date                                null,
    country    varchar(100)                        null,
    created_at timestamp default CURRENT_TIMESTAMP null
);

CREATE TABLE `book` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `title` varchar(255) DEFAULT NULL,
                        `isbn` varchar(50) DEFAULT NULL,
                        `published_year` int DEFAULT NULL,
                        `price` decimal(10,2) DEFAULT NULL,
                        `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `book_author` (
                               `book_id` bigint NOT NULL,
                               `author_id` bigint NOT NULL,
                               `contribution` varchar(255) DEFAULT NULL,
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`book_id`,`author_id`),
                               KEY `fk_book_author_author` (`author_id`),
                               CONSTRAINT `fk_book_author_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`),
                               CONSTRAINT `fk_book_author_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci