CREATE TABLE historicalseason(id int NOT NULL AUTO_INCREMENT, seasonId int NOT NULL, startYear int DEFAULT NULL, endYear int DEFAULT NULL, PRIMARY KEY (id));

CREATE TABLE historicalseasonplayer (id int NOT NULL AUTO_INCREMENT, seasonId int NOT NULL, firstName varchar(64), lastName varchar(64), name varchar(64), points int, entries int, PRIMARY KEY (id));
