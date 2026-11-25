CREATE DATABASE IF NOT EXISTS sportify CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE sportify;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS match_stats;
DROP TABLE IF EXISTS points_table;
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS players;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS venues;
DROP TABLE IF EXISTS admins;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE admins (
    username VARCHAR(50) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE teams (
    team_id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(80) NOT NULL,
    coach VARCHAR(100) NOT NULL,
    captain_id VARCHAR(10) NULL,
    UNIQUE KEY uq_team_name (name)
);

CREATE TABLE venues (
    venue_id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(80) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0)
);

CREATE TABLE players (
    player_id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age BETWEEN 14 AND 50),
    role VARCHAR(30) NOT NULL,
    jersey_no INT NOT NULL CHECK (jersey_no BETWEEN 1 AND 99),
    team_id VARCHAR(10) NOT NULL,
    UNIQUE KEY uq_team_jersey (team_id, jersey_no),
    UNIQUE KEY uq_player_team (player_id, team_id),
    CONSTRAINT fk_player_team FOREIGN KEY (team_id) REFERENCES teams(team_id)
);

ALTER TABLE teams ADD CONSTRAINT fk_team_captain_same_team FOREIGN KEY (captain_id, team_id) REFERENCES players(player_id, team_id);

CREATE TABLE matches (
    match_id VARCHAR(10) PRIMARY KEY,
    home_team_id VARCHAR(10) NOT NULL,
    away_team_id VARCHAR(10) NOT NULL,
    venue_id VARCHAR(10) NOT NULL,
    match_date DATE NOT NULL,
    match_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    home_score INT NULL CHECK (home_score IS NULL OR home_score >= 0),
    away_score INT NULL CHECK (away_score IS NULL OR away_score >= 0),
    winner_id VARCHAR(10) NULL,
    CONSTRAINT ck_different_teams CHECK (home_team_id <> away_team_id),
    CONSTRAINT fk_match_home FOREIGN KEY (home_team_id) REFERENCES teams(team_id),
    CONSTRAINT fk_match_away FOREIGN KEY (away_team_id) REFERENCES teams(team_id),
    CONSTRAINT fk_match_venue FOREIGN KEY (venue_id) REFERENCES venues(venue_id),
    CONSTRAINT fk_match_winner FOREIGN KEY (winner_id) REFERENCES teams(team_id),
    UNIQUE KEY uq_venue_slot (venue_id, match_date, match_time),
    INDEX ix_home_date (home_team_id, match_date),
    INDEX ix_away_date (away_team_id, match_date)
);

CREATE TABLE match_stats (
    match_id VARCHAR(10) NOT NULL,
    player_id VARCHAR(10) NOT NULL,
    score_contribution INT NOT NULL DEFAULT 0 CHECK (score_contribution >= 0),
    secondary_contribution INT NOT NULL DEFAULT 0 CHECK (secondary_contribution >= 0),
    PRIMARY KEY (match_id, player_id),
    CONSTRAINT fk_stat_match FOREIGN KEY (match_id) REFERENCES matches(match_id),
    CONSTRAINT fk_stat_player FOREIGN KEY (player_id) REFERENCES players(player_id)
);

CREATE TABLE points_table (
    team_id VARCHAR(10) PRIMARY KEY,
    played INT NOT NULL DEFAULT 0,
    won INT NOT NULL DEFAULT 0,
    drawn INT NOT NULL DEFAULT 0,
    lost INT NOT NULL DEFAULT 0,
    points INT NOT NULL DEFAULT 0,
    score_difference INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_points_team FOREIGN KEY (team_id) REFERENCES teams(team_id)
);

-- Demo password is: sportify123. This is a salted PBKDF2WithHmacSHA256 hash.
INSERT INTO admins (username, password_hash) VALUES
('admin', 'pbkdf2$120000$U3BvcnRpZnlTYWx0MTIzNDU2Nw==$0i0RLzSFU3VTlfz8ZS2wzZDHAvS44UVe2+rltX5QVf4=');

INSERT INTO teams (team_id, name, city, coach) VALUES
('T01', 'Mumbai Strikers', 'Mumbai', 'Arjun Coach'),
('T02', 'Delhi Dynamos', 'Delhi', 'Dev Coach'),
('T03', 'Chennai Kings', 'Chennai', 'Kiran Coach'),
('T04', 'Punjab Lions', 'Chandigarh', 'Manpreet Coach');

INSERT INTO venues (venue_id, name, city, capacity) VALUES ('V01', 'City Stadium', 'Mumbai', 20000);

INSERT INTO players (player_id, name, age, role, jersey_no, team_id) VALUES
('P001','Aarav Mehta',22,'GOALKEEPER',1,'T01'), ('P002','Vihaan Shah',23,'DEFENDER',2,'T01'), ('P003','Aditya Rao',24,'DEFENDER',3,'T01'),
('P004','Arjun Nair',21,'DEFENDER',4,'T01'), ('P005','Sai Patel',22,'MIDFIELDER',5,'T01'), ('P006','Ishaan Das',25,'MIDFIELDER',6,'T01'),
('P007','Krish Iyer',20,'MIDFIELDER',7,'T01'), ('P008','Rohan Singh',23,'FORWARD',8,'T01'), ('P009','Kabir Kumar',24,'FORWARD',9,'T01'),
('P010','Asha Mehta',22,'FORWARD',10,'T01'), ('P011','Reyansh Joshi',26,'FORWARD',11,'T01'),
('P012','Dev Malhotra',23,'GOALKEEPER',1,'T02'), ('P013','Aryan Khanna',22,'DEFENDER',2,'T02'), ('P014','Dhruv Bhat',24,'DEFENDER',3,'T02'),
('P015','Yash Verma',21,'DEFENDER',4,'T02'), ('P016','Om Kapoor',25,'DEFENDER',5,'T02'), ('P017','Anaya Sethi',20,'MIDFIELDER',6,'T02'),
('P018','Riya Anand',22,'MIDFIELDER',7,'T02'), ('P019','Mira Gill',23,'MIDFIELDER',8,'T02'), ('P020','Kavya Roy',24,'FORWARD',9,'T02'),
('P021','Neha Rao',20,'FORWARD',10,'T02'), ('P022','Tara Jain',26,'FORWARD',11,'T02'),
('P023','Karthik Menon',23,'GOALKEEPER',1,'T03'), ('P024','Vikram Reddy',22,'DEFENDER',2,'T03'), ('P025','Nikhil Bose',24,'DEFENDER',3,'T03'),
('P026','Pranav Pillai',21,'DEFENDER',4,'T03'), ('P027','Manav Krishnan',25,'DEFENDER',5,'T03'), ('P028','Sanjay Kumar',20,'MIDFIELDER',6,'T03'),
('P029','Arvind Raj',22,'MIDFIELDER',7,'T03'), ('P030','Hari Iyer',23,'MIDFIELDER',8,'T03'), ('P031','Deepak Nair',24,'FORWARD',9,'T03'),
('P032','Kavin Das',20,'FORWARD',10,'T03'), ('P033','Ramesh Balan',26,'FORWARD',11,'T03'),
('P034','Gurpreet Singh',23,'GOALKEEPER',1,'T04'), ('P035','Harjit Gill',22,'DEFENDER',2,'T04'), ('P036','Jaspreet Kaur',24,'DEFENDER',3,'T04'),
('P037','Amandeep Brar',21,'DEFENDER',4,'T04'), ('P038','Simran Kaur',25,'DEFENDER',5,'T04'), ('P039','Navdeep Sandhu',20,'MIDFIELDER',6,'T04'),
('P040','Baljit Singh',22,'MIDFIELDER',7,'T04'), ('P041','Sukhdeep Gill',23,'MIDFIELDER',8,'T04'), ('P042','Manpreet Kaur',24,'FORWARD',9,'T04'),
('P043','Ranjit Singh',20,'FORWARD',10,'T04'), ('P044','Jatinder Brar',26,'FORWARD',11,'T04');

UPDATE teams SET captain_id = 'P010' WHERE team_id = 'T01';
UPDATE teams SET captain_id = 'P020' WHERE team_id = 'T02';
UPDATE teams SET captain_id = 'P032' WHERE team_id = 'T03';
UPDATE teams SET captain_id = 'P042' WHERE team_id = 'T04';

INSERT INTO matches (match_id, home_team_id, away_team_id, venue_id, match_date, match_time, status, home_score, away_score, winner_id) VALUES
('M101','T01','T02','V01','2026-12-01','18:00:00','SCHEDULED',NULL,NULL,NULL),
('M102','T03','T04','V01','2026-12-02','18:00:00','COMPLETED',0,0,NULL),
('M103','T01','T03','V01','2026-12-03','18:00:00','SCHEDULED',NULL,NULL,NULL);

INSERT INTO points_table (team_id, played, won, drawn, lost, points, score_difference) VALUES
('T03',1,0,1,0,1,0), ('T04',1,0,1,0,1,0);
