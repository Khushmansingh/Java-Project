CREATE DATABASE IF NOT EXISTS f1_management;
USE f1_management;

CREATE TABLE IF NOT EXISTS teams (
    team_id INT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL UNIQUE,
    country VARCHAR(80) NOT NULL,
    budget DECIMAL(14,2) NOT NULL CHECK (budget >= 0),
    max_seats INT NOT NULL DEFAULT 2 CHECK (max_seats = 2)
);

CREATE TABLE IF NOT EXISTS drivers (
    driver_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    nationality VARCHAR(80) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    rating DECIMAL(5,2) NOT NULL CHECK (rating >= 0 AND rating <= 100),
    salary DECIMAL(14,2) NOT NULL CHECK (salary >= 0),
    market_value DECIMAL(14,2) NOT NULL CHECK (market_value >= 0),
    team_id INT NULL,
    status VARCHAR(15) NOT NULL CHECK (status IN ('FIT','INJURED','SUSPENDED')),
    FOREIGN KEY (team_id) REFERENCES teams(team_id)
);

CREATE TABLE IF NOT EXISTS transfer_offers (
    offer_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id INT NOT NULL,
    offered_salary DECIMAL(14,2) NOT NULL CHECK (offered_salary >= 0),
    offer_amount DECIMAL(14,2) NOT NULL CHECK (offer_amount >= 0),
    deadline DATE NOT NULL,
    min_rating DECIMAL(5,2) NOT NULL CHECK (min_rating >= 0 AND min_rating <= 100),
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (team_id) REFERENCES teams(team_id)
);

CREATE TABLE IF NOT EXISTS transfer_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    driver_id INT NOT NULL,
    offer_id INT NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SHORTLISTED','REJECTED','SIGNED')),
    UNIQUE (driver_id, offer_id),
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id),
    FOREIGN KEY (offer_id) REFERENCES transfer_offers(offer_id)
);

CREATE TABLE IF NOT EXISTS race_results (
    driver_id INT PRIMARY KEY,
    points INT NOT NULL DEFAULT 0 CHECK (points >= 0),
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)
);

INSERT IGNORE INTO teams(team_id,team_name,country,budget,max_seats) VALUES
(1,'Red Falcon','Italy',50000000,2);
INSERT IGNORE INTO drivers(driver_id,name,nationality,email,rating,salary,market_value,team_id,status) VALUES
(1,'Leo','Monaco','leo@example.com',91.5,8000000,25000000,NULL,'FIT'),
(2,'Max','Netherlands','max@example.com',84.0,6500000,18000000,NULL,'FIT'),
(3,'Sam','United Kingdom','sam@example.com',72.5,4000000,9000000,NULL,'FIT');
