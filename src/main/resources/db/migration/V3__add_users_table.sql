-- Backs the JWT auth layer added after the original schema. Runs for every
-- database (fresh or pre-existing/baselined) since neither V1 nor V2 create it.
-- username is unique at the DB level - the application's existsByUsername() check
-- alone can't stop two concurrent signups racing each other.
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','MANAGER','STAFF') NOT NULL,
    enabled BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
