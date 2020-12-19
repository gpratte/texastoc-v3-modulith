-- noinspection SqlNoDataSourceInspectionForFile
-- Assumes the following
--   log in as root
--     CREATE USER '<user>'@'localhost' IDENTIFIED BY '<pass>';
--     GRANT ALL PRIVILEGES ON * . * TO '<user>'@'localhost';
--   log in as <user>
--     CREATE DATABASE toc;
-- Run this file from command line
--   mysql -u <user> -p toc < 1-create-mysql-schema.sql

create table if not exists tocconfig (kittyDebit INT NOT NULL, annualTocCost INT NOT NULL, quarterlyTocCost INT NOT NULL, quarterlyNumPayouts INT NOT NULL, regularBuyInCost INT NOT NULL, regularRebuyCost INT NOT NULL, regularRebuyTocDebit INT NOT NULL, doubleBuyInCost INT NOT NULL, doubleRebuyCost INT NOT NULL, doubleRebuyTocDebit INT NOT NULL);

create table if not exists season (id INT auto_increment, startDate DATE, endDate DATE, kittyPerGame INT, tocPerGame INT, quarterlyTocPerGame INT, quarterlyTocPayouts INT, buyInCost INT, rebuyAddOnCost INT, rebuyAddOnTocDebit INT, doubleBuyInCost INT, doubleRebuyAddOnCost INT, doubleRebuyAddOnTocDebit INT, buyInCollected INT, rebuyAddOnCollected INT, annualTocCollected INT, totalCollected INT, annualTocFromRebuyAddOnCalculated INT, rebuyAddOnLessAnnualTocCalculated INT, totalCombinedAnnualTocCalculated INT, kittyCalculated INT, prizePotCalculated INT, numGames INT, numGamesPlayed INT, finalized BOOLEAN, lastCalculated DATE, primary key(id));

create table if not exists quarterlyseason (id INT auto_increment, seasonId INT NOT NULL, startDate DATE, endDate DATE, finalized BOOLEAN, quarter INT NOT NULL, numGames INT, numGamesPlayed INT, qTocCollected INT, qTocPerGame INT, numPayouts INT NOT NULL, lastCalculated DATE, primary key(id));

create table if not exists seasonplayer (id INT auto_increment, playerId INT NOT NULL, seasonId INT NOT NULL, name varchar(64) DEFAULT NULL, entries INT DEFAULT 0, points INT DEFAULT 0, place INT DEFAULT 0, forfeit BOOLEAN DEFAULT false, primary key(id));

ALTER TABLE seasonplayer ADD CONSTRAINT SPlayer_Unique UNIQUE (playerId, seasonId);

create table if not exists quarterlyseasonplayer (id INT auto_increment, playerId INT NOT NULL, seasonId INT NOT NULL, qSeasonId INT NOT NULL, name varchar(64) DEFAULT NULL, entries INT DEFAULT 0, points INT DEFAULT 0, place INT, primary key(id));

ALTER TABLE quarterlyseasonplayer ADD CONSTRAINT QSPlayer_Unique UNIQUE (playerId, seasonId, qSeasonId);

create table if not exists supply (id INT auto_increment, amount INT NOT NULL, date DATE NOT NULL, type varchar(16) NOT NULL, description varchar(64), primary key(id));

create table if not exists player (id INT auto_increment, firstName varchar(32) DEFAULT NULL, lastName varchar(32) DEFAULT NULL, phone varchar(32) DEFAULT NULL, email varchar(64) DEFAULT NULL, password varchar(255) DEFAULT NULL, primary key (id));

create table game (id INT AUTO_INCREMENT, seasonId INT NOT NULL, qSeasonId INT NOT NULL, hostId INT DEFAULT NULL, gameDate DATE NOT NULL, hostName varchar(64) DEFAULT NULL, quarter INT DEFAULT NULL, doubleBuyIn BOOLEAN DEFAULT FALSE, transportRequired BOOLEAN DEFAULT FALSE, kittyCost INT DEFAULT 0, buyInCost INT DEFAULT 0, rebuyAddOnCost INT DEFAULT 0, rebuyAddOnTocDebit INT DEFAULT 0, annualTocCost INT DEFAULT 0, quarterlyTocCost INT DEFAULT 0, started TIMESTAMP DEFAULT NULL, numPlayers INT DEFAULT 0, buyInCollected INT DEFAULT 0, rebuyAddOnCollected INT DEFAULT 0, annualTocCollected INT DEFAULT 0, quarterlyTocCollected INT DEFAULT 0, totalCollected INT DEFAULT 0, kittyCalculated INT DEFAULT 0, annualTocFromRebuyAddOnCalculated INT DEFAULT 0, rebuyAddOnLessAnnualTocCalculated INT DEFAULT 0, totalCombinedTocCalculated INT DEFAULT 0, prizePotCalculated INT DEFAULT 0, payoutDelta INT DEFAULT NULL, seasonGameNum INT, quarterlyGameNum INT, finalized BOOLEAN DEFAULT FALSE, lastCalculated DATE DEFAULT NULL, PRIMARY KEY (id));

create table if not exists gameplayer (id INT auto_increment, playerId INT NOT NULL, gameId INT NOT NULL, qSeasonId INT NOT NULL, seasonId INT NOT NULL, name varchar(64) NOT NULL, place INT DEFAULT NULL, points INT DEFAULT NULL, knockedOut BOOLEAN DEFAULT FALSE, roundUpdates BOOLEAN DEFAULT FALSE, buyInCollected INT DEFAULT NULL, rebuyAddOnCollected INT DEFAULT NULL, annualTocCollected INT DEFAULT NULL, quarterlyTocCollected INT DEFAULT NULL, chop INT DEFAULT NULL, primary key (id));

create table if not exists gamepayout (id INT auto_increment, gameId INT NOT NULL, place INT NOT NULL, amount INT DEFAULT NULL, chopAmount INT DEFAULT NULL, chopPercent DOUBLE DEFAULT NULL, PRIMARY KEY (id));

ALTER TABLE gamepayout ADD CONSTRAINT GPayout_Unique UNIQUE (gameId, place);

create table if not exists seasonpayout (id INT auto_increment, seasonId INT NOT NULL, place INT NOT NULL, amount INT DEFAULT NULL, PRIMARY KEY (id));

ALTER TABLE seasonpayout ADD CONSTRAINT SPayout_Unique UNIQUE (seasonId, place);

create table if not exists quarterlyseasonpayout (id INT auto_increment, seasonId INT NOT NULL, qSeasonId INT NOT NULL, place INT NOT NULL, amount INT DEFAULT NULL, PRIMARY KEY (id));

ALTER TABLE quarterlyseasonpayout ADD CONSTRAINT QSPayout_Unique UNIQUE (seasonId, qSeasonId, place);

create table if not exists seating (gameId INT NOT NULL, settings varchar(8192) NOT NULL, PRIMARY KEY (gameId));

create table if not exists role (id int auto_increment, description varchar(255), name varchar(255), primary key (id));

create table if not exists player_roles (playerId int not null, roleId int not null, primary key (playerId, roleId));

alter table player_roles add constraint fk_role_id foreign key (roleId) references role (id);

alter table player_roles add constraint fk_player_id foreign key (playerId) references player (id);

create table if not exists payout (numPayouts INT NOT NULL, place INT NOT NULL, percent DOUBLE DEFAULT NULL, PRIMARY KEY (numPayouts, place));
