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
    first_name varchar(32)  DEFAULT NULL,
    last_name  varchar(32)  DEFAULT NULL,
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
       (3, 'Guest', 'Admin', '5121231235', 'admin@texastoc.com',
        '$2a$10$qXQo4z4oXKPEKyYO7bAQmOQ9PhIcHK4LOo/L1U9j/xkLEmseLWECK');

CREATE TABLE role
(
    id          int NOT NULL AUTO_INCREMENT,
    type        varchar(255) DEFAULT NULL,
    player      int,
    PRIMARY KEY (id)
);
alter table role
    add constraint fk_role_player foreign key (player) references player (id);

INSERT INTO role
VALUES (1, 'ADMIN', 1),
       (2, 'USER', 1),
       (3, 'USER', 2),
       (4, 'ADMIN', 3),
       (5, 'USER', 3);


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
    id int NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
);
INSERT INTO settings VALUES (1);

# TODO figure out settings_key column 
CREATE TABLE toc_config
(
    id                      int NOT NULL AUTO_INCREMENT,
    start_year              int NOT NULL,
    kitty_debit             int NOT NULL,
    annual_toc_cost         int NOT NULL,
    quarterly_toc_cost      int NOT NULL,
    quarterly_num_payouts   int NOT NULL,
    regular_buy_in_cost     int NOT NULL,
    regular_rebuy_cost      int NOT NULL,
    regular_rebuy_toc_debit int NOT NULL,
    settings                int NOT NULL,
    settings_key            int,
    PRIMARY KEY (id)
);
alter table toc_config
    add constraint fk_toc_config_settings foreign key (settings) references settings (id);
INSERT INTO toc_config VALUES (1, 2020, 10, 20, 20, 3, 40, 40, 20, 1, NULL);

CREATE TABLE version
(
    id            int NOT NULL AUTO_INCREMENT,
    env           varchar(255) DEFAULT NULL,
    version       varchar(255) DEFAULT NULL,
    settings      int NOT NULL,
    settings_key  int,
    PRIMARY KEY (id)
);
alter table version
    add constraint fk_version_settings foreign key (settings) references settings (id);
INSERT INTO version VALUES (1, 'local', '2.21', 1, NULL);
INSERT INTO version VALUES (2, 'heroku', '2.21', 1, NULL);

CREATE TABLE payout
(
    id            int NOT NULL AUTO_INCREMENT,
    num_payouts   int NOT NULL,
    place         int NOT NULL,
    percent       double DEFAULT NULL,
    settings      int NOT NULL,
    settings_key  int,
    PRIMARY KEY (id)
);
alter table payout
    add constraint fk_payout_settings foreign key (settings) references settings (id);
INSERT INTO payout (id, num_payouts, place, percent, settings)
VALUES (1, 2, 1, 0.65, 1),
       (2, 2, 2, 0.35, 1),
       (3, 3, 1, 0.5, 1),
       (4, 3, 2, 0.3, 1),
       (5, 3, 3, 0.2, 1),
       (6, 4, 1, 0.45, 1),
       (7, 4, 2, 0.25, 1),
       (8, 4, 3, 0.18, 1),
       (9, 4, 4, 0.12, 1),
       (10, 5, 1, 0.4, 1),
       (11, 5, 2, 0.23, 1),
       (12, 5, 3, 0.16, 1),
       (13, 5, 4, 0.12, 1),
       (14, 5, 5, 0.09, 1),
       (15, 6, 1, 0.38, 1),
       (16, 6, 2, 0.22, 1),
       (17, 6, 3, 0.15, 1),
       (18, 6, 4, 0.11, 1),
       (19, 6, 5, 0.08, 1),
       (20, 6, 6, 0.06, 1),
       (21, 7, 1, 0.35, 1),
       (22, 7, 2, 0.21, 1),
       (23, 7, 3, 0.15, 1),
       (24, 7, 4, 0.11, 1),
       (25, 7, 5, 0.08, 1),
       (26, 7, 6, 0.06, 1),
       (27, 7, 7, 0.04, 1),
       (28, 8, 1, 0.335, 1),
       (29, 8, 2, 0.2, 1),
       (30, 8, 3, 0.145, 1),
       (31, 8, 4, 0.11, 1),
       (32, 8, 5, 0.08, 1),
       (33, 8, 6, 0.06, 1),
       (34, 8, 7, 0.04, 1),
       (35, 8, 8, 0.03, 1),
       (36, 9, 1, 0.32, 1),
       (37, 9, 2, 0.195, 1),
       (38, 9, 3, 0.14, 1),
       (39, 9, 4, 0.11, 1),
       (40, 9, 5, 0.08, 1),
       (41, 9, 6, 0.06, 1),
       (42, 9, 7, 0.04, 1),
       (43, 9, 8, 0.03, 1),
       (44, 9, 9, 0.025, 1),
       (45, 10, 1, 0.3, 1),
       (46, 10, 2, 0.19, 1),
       (47, 10, 3, 0.1325, 1),
       (48, 10, 4, 0.105, 1),
       (49, 10, 5, 0.075, 1),
       (50, 10, 6, 0.055, 1),
       (51, 10, 7, 0.0375, 1),
       (52, 10, 8, 0.03, 1),
       (53, 10, 9, 0.0225, 1),
       (54, 10, 10, 0.015, 1);


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
