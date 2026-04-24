CREATE DATABASE IF NOT EXISTS smarttracker
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smarttracker;

CREATE TABLE IF NOT EXISTS users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100)  NOT NULL,
    email    VARCHAR(191)  NOT NULL UNIQUE,
    password VARCHAR(255)  NOT NULL
);

CREATE TABLE IF NOT EXISTS habits (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    category    VARCHAR(50),
    frequency   VARCHAR(16)  NOT NULL DEFAULT 'DAILY',
    streak      INT          NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS workouts (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT          NOT NULL,
    title            VARCHAR(100) NOT NULL,
    duration_minutes INT          NOT NULL DEFAULT 0,
    calories         INT          NOT NULL DEFAULT 0,
    intensity        VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    completed        TINYINT(1)   NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tasks (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    completed   TINYINT(1)   NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
