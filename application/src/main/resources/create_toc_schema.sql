CREATE TABLE tocconfig
(
    kittyDebit           int NOT NULL,
    annualTocCost        int NOT NULL,
    quarterlyTocCost     int NOT NULL,
    quarterlyNumPayouts  int NOT NULL,
    regularBuyInCost     int NOT NULL,
    regularRebuyCost     int NOT NULL,
    regularRebuyTocDebit int NOT NULL
);
INSERT INTO tocconfig
VALUES (10, 20, 20, 3, 40, 40, 20);

CREATE TABLE season
(
    id                                int NOT NULL AUTO_INCREMENT,
    startDate                         date    DEFAULT NULL,
    endDate                           date    DEFAULT NULL,
    kittyPerGame                      int     DEFAULT NULL,
    tocPerGame                        int     DEFAULT NULL,
    quarterlyTocPerGame               int     DEFAULT NULL,
    quarterlyTocPayouts               int     DEFAULT NULL,
    buyInCost                         int     DEFAULT NULL,
    rebuyAddOnCost                    int     DEFAULT NULL,
    rebuyAddOnTocDebit                int     DEFAULT NULL,
    buyInCollected                    int     DEFAULT NULL,
    rebuyAddOnCollected               int     DEFAULT NULL,
    annualTocCollected                int     DEFAULT NULL,
    totalCollected                    int     DEFAULT NULL,
    annualTocFromRebuyAddOnCalculated int     DEFAULT NULL,
    rebuyAddOnLessAnnualTocCalculated int     DEFAULT NULL,
    totalCombinedAnnualTocCalculated  int     DEFAULT NULL,
    kittyCalculated                   int     DEFAULT NULL,
    prizePotCalculated                int     DEFAULT NULL,
    numGames                          int     DEFAULT NULL,
    numGamesPlayed                    int     DEFAULT NULL,
    finalized                         boolean DEFAULT NULL,
    lastCalculated                    date    DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE quarterlyseason
(
    id             int NOT NULL AUTO_INCREMENT,
    seasonId       int NOT NULL,
    startDate      date    DEFAULT NULL,
    endDate        date    DEFAULT NULL,
    finalized      boolean DEFAULT NULL,
    quarter        int NOT NULL,
    numGames       int     DEFAULT NULL,
    numGamesPlayed int     DEFAULT NULL,
    qTocCollected  int     DEFAULT NULL,
    qTocPerGame    int     DEFAULT NULL,
    numPayouts     int NOT NULL,
    lastCalculated date    DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE seasonplayer
(
    id       int NOT NULL AUTO_INCREMENT,
    playerId int NOT NULL,
    seasonId int NOT NULL,
    name     varchar(64) DEFAULT NULL,
    entries  int         DEFAULT 0,
    points   int         DEFAULT 0,
    place    int         DEFAULT 0,
    forfeit  boolean     DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY SPlayer_Unique (playerId, seasonId)
);

CREATE TABLE quarterlyseasonplayer
(
    id        int NOT NULL AUTO_INCREMENT,
    playerId  int NOT NULL,
    seasonId  int NOT NULL,
    qSeasonId int NOT NULL,
    name      varchar(64) DEFAULT NULL,
    entries   int         DEFAULT 0,
    points    int         DEFAULT 0,
    place     int         DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY QSPlayer_Unique (playerId, seasonId, qSeasonId)
);

CREATE TABLE player
(
    id        int NOT NULL AUTO_INCREMENT,
    firstName varchar(32)  DEFAULT NULL,
    lastName  varchar(32)  DEFAULT NULL,
    phone     varchar(32)  DEFAULT NULL,
    email     varchar(64)  DEFAULT NULL,
    password  varchar(255) DEFAULT NULL,
    PRIMARY KEY (id)
);
# password is password
INSERT INTO player
VALUES (1, 'Gil', 'Pratte', '5121231235', 'gilpratte@texastoc.com',
        '$2a$10$qXQo4z4oXKPEKyYO7bAQmOQ9PhIcHK4LOo/L1U9j/xkLEmseLWECK'),
       (2, 'Guest', 'User', '5121231235', 'guest@texastoc.com',
        '$2a$10$qXQo4z4oXKPEKyYO7bAQmOQ9PhIcHK4LOo/L1U9j/xkLEmseLWECK'),
       (3, 'Guest', 'Admin', '5121231235', 'guest-admin@texastoc.com',
        '$2a$10$qXQo4z4oXKPEKyYO7bAQmOQ9PhIcHK4LOo/L1U9j/xkLEmseLWECK');

CREATE TABLE game
(
    id                                int       NOT NULL AUTO_INCREMENT,
    seasonId                          int       NOT NULL,
    qSeasonId                         int       NOT NULL,
    hostId                            int            DEFAULT NULL,
    gameDate                          date      NOT NULL,
    hostName                          varchar(64)    DEFAULT NULL,
    quarter                           int            DEFAULT NULL,
    transportRequired                 boolean        DEFAULT FALSE,
    kittyCost                         int            DEFAULT 0,
    buyInCost                         int            DEFAULT 0,
    rebuyAddOnCost                    int            DEFAULT 0,
    rebuyAddOnTocDebit                int            DEFAULT 0,
    annualTocCost                     int            DEFAULT 0,
    quarterlyTocCost                  int            DEFAULT 0,
    started                           timestamp NULL DEFAULT NULL,
    numPlayers                        int            DEFAULT 0,
    buyInCollected                    int            DEFAULT 0,
    rebuyAddOnCollected               int            DEFAULT 0,
    annualTocCollected                int            DEFAULT 0,
    quarterlyTocCollected             int            DEFAULT 0,
    totalCollected                    int            DEFAULT 0,
    kittyCalculated                   int            DEFAULT 0,
    annualTocFromRebuyAddOnCalculated int            DEFAULT 0,
    rebuyAddOnLessAnnualTocCalculated int            DEFAULT 0,
    totalCombinedTocCalculated        int            DEFAULT 0,
    prizePotCalculated                int            DEFAULT 0,
    payoutDelta                       int            DEFAULT NULL,
    seasonGameNum                     int            DEFAULT NULL,
    quarterlyGameNum                  int            DEFAULT NULL,
    finalized                         boolean        DEFAULT FALSE,
    lastCalculated                    date           DEFAULT NULL,
    canRebuy                          boolean        DEFAULT TRUE,
    PRIMARY KEY (id)
);

CREATE TABLE gameplayer
(
    id                    int         NOT NULL AUTO_INCREMENT,
    playerId              int         NOT NULL,
    gameId                int         NOT NULL,
    qSeasonId             int         NOT NULL,
    seasonId              int         NOT NULL,
    name                  varchar(64) NOT NULL,
    place                 int     DEFAULT NULL,
    points                int     DEFAULT NULL,
    knockedOut            boolean DEFAULT FALSE,
    roundUpdates          boolean DEFAULT FALSE,
    buyInCollected        int     DEFAULT NULL,
    rebuyAddOnCollected   int     DEFAULT NULL,
    annualTocCollected    int     DEFAULT NULL,
    quarterlyTocCollected int     DEFAULT NULL,
    chop                  int     DEFAULT NULL,
    PRIMARY KEY (id)
);


CREATE TABLE gamepayout
(
    id          int NOT NULL AUTO_INCREMENT,
    gameId      int NOT NULL,
    place       int NOT NULL,
    amount      int    DEFAULT NULL,
    chopAmount  int    DEFAULT NULL,
    chopPercent double DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY GPayout_Unique (gameId, place)
);

CREATE TABLE quarterlyseasonpayout
(
    id        int NOT NULL AUTO_INCREMENT,
    seasonId  int NOT NULL,
    qSeasonId int NOT NULL,
    place     int NOT NULL,
    amount    int DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY QSPayout_Unique (seasonId, qSeasonId, place)
);

CREATE TABLE role
(
    id          int NOT NULL AUTO_INCREMENT,
    description varchar(255) DEFAULT NULL,
    name        varchar(255) DEFAULT NULL,
    PRIMARY KEY (id)
);
INSERT INTO role
VALUES (1, 'Admin role', 'ADMIN'),
       (2, 'User role', 'USER');

CREATE TABLE player_roles
(
    playerId int NOT NULL,
    roleId   int NOT NULL,
    PRIMARY KEY (playerId, roleId)
);
INSERT INTO player_roles
VALUES (1, 1),
       (1, 2),
       (2, 1),
       (3, 1),
       (3, 2);


CREATE TABLE seasonpayout
(
    id         int NOT NULL AUTO_INCREMENT,
    seasonId   int NOT NULL,
    place      int NOT NULL,
    amount     int     DEFAULT NULL,
    guarenteed boolean DEFAULT false,
    estimated  boolean DEFAULT false,
    cash       boolean DEFAULT false,
    PRIMARY KEY (id),
    UNIQUE KEY SPayout_Unique (seasonId, place, estimated)
);

CREATE TABLE seasonpayoutsettings
(
    id       int           NOT NULL AUTO_INCREMENT,
    seasonId int           NOT NULL,
    settings varchar(8192) NOT NULL,
    PRIMARY KEY (id)
);
INSERT INTO seasonpayoutsettings
VALUES (1, 1,
        '[{"lowRange" : 5000,"highRange" : 7000,
           "guaranteed": [{"place" : 1,"amount" : 1400,"percent" : 20}],
           "finalTable": [{"place" : 2,"amount" : 1350,"percent" : 20},
                          {"place" : 3,"amount" : 1150,"percent" : 16},
                          {"place" : 4,"amount" : 1100,"percent" : 14},
                          {"place" : 5,"amount" : 0,"percent" : 30}]}]');
INSERT INTO seasonpayoutsettings
VALUES (2, 2,
        '[{"lowRange" : 5000,"highRange" : 7000,
           "guaranteed": [{"place" : 1,"amount" : 1400,"percent" : 20}],
           "finalTable": [{"place" : 2,"amount" : 1350,"percent" : 20},
                          {"place" : 3,"amount" : 1150,"percent" : 16},
                          {"place" : 4,"amount" : 1100,"percent" : 14},
                          {"place" : 5,"amount" : 0,"percent" : 30}]}]');


DROP TABLE IF EXISTS seating;
CREATE TABLE seating
(
    gameId   int           NOT NULL,
    settings varchar(8192) NOT NULL,
    PRIMARY KEY (gameId)
);

DROP TABLE IF EXISTS settings;
CREATE TABLE settings
(
    id       int NOT NULL AUTO_INCREMENT,
    settings varchar(1024) DEFAULT NULL,
    PRIMARY KEY (id)
);
INSERT INTO settings
VALUES (1,
        '{"uiVersions": [{"env": "local", "version": "2.21"}, {"env": "heroku", "version": "2.21"}]}');

DROP TABLE IF EXISTS supply;
CREATE TABLE supply
(
    id          int         NOT NULL AUTO_INCREMENT,
    amount      int         NOT NULL,
    date        date        NOT NULL,
    type        varchar(16) NOT NULL,
    description varchar(64) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE payout
(
    numPayouts int NOT NULL,
    place      int NOT NULL,
    percent    double DEFAULT NULL,
    PRIMARY KEY (numPayouts, place)
);
INSERT INTO payout
VALUES (2, 1, 0.65),
       (2, 2, 0.35),
       (3, 1, 0.5),
       (3, 2, 0.3),
       (3, 3, 0.2),
       (4, 1, 0.45),
       (4, 2, 0.25),
       (4, 3, 0.18),
       (4, 4, 0.12),
       (5, 1, 0.4),
       (5, 2, 0.23),
       (5, 3, 0.16),
       (5, 4, 0.12),
       (5, 5, 0.09),
       (6, 1, 0.38),
       (6, 2, 0.22),
       (6, 3, 0.15),
       (6, 4, 0.11),
       (6, 5, 0.08),
       (6, 6, 0.06),
       (7, 1, 0.35),
       (7, 2, 0.21),
       (7, 3, 0.15),
       (7, 4, 0.11),
       (7, 5, 0.08),
       (7, 6, 0.06),
       (7, 7, 0.04),
       (8, 1, 0.335),
       (8, 2, 0.2),
       (8, 3, 0.145),
       (8, 4, 0.11),
       (8, 5, 0.08),
       (8, 6, 0.06),
       (8, 7, 0.04),
       (8, 8, 0.03),
       (9, 1, 0.32),
       (9, 2, 0.195),
       (9, 3, 0.14),
       (9, 4, 0.11),
       (9, 5, 0.08),
       (9, 6, 0.06),
       (9, 7, 0.04),
       (9, 8, 0.03),
       (9, 9, 0.025),
       (10, 1, 0.3),
       (10, 2, 0.19),
       (10, 3, 0.1325),
       (10, 4, 0.105),
       (10, 5, 0.075),
       (10, 6, 0.055),
       (10, 7, 0.0375),
       (10, 8, 0.03),
       (10, 9, 0.0225),
       (10, 10, 0.015);

alter table player_roles
    add constraint fk_role_id foreign key (roleId) references role (id);
alter table player_roles
    add constraint fk_player_id foreign key (playerId) references player (id);

CREATE TABLE historicalseasonplayer
(
    id int NOT NULL AUTO_INCREMENT,
    seasonId int NOT NULL,
    firstName varchar(64),
    lastName varchar(64),
    name varchar(64),
    points int,
    entries int,
    PRIMARY KEY (id)
);

CREATE TABLE historicalseason
(
    id int NOT NULL AUTO_INCREMENT,
    seasonId int NOT NULL,
    startYear int DEFAULT NULL,
    endYear int DEFAULT NULL,
    PRIMARY KEY (id)
);
