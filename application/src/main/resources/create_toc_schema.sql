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
ALTER TABLE player ADD UNIQUE (email);
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


CREATE TABLE seating
(
    gameId   int           NOT NULL,
    settings varchar(8192) NOT NULL,
    PRIMARY KEY (gameId)
);

CREATE TABLE version
(
    id      int NOT NULL AUTO_INCREMENT,
    version varchar(8) NOT NULL,
    PRIMARY KEY (id)
);
INSERT INTO version VALUES (1, '2.21');

CREATE TABLE settings
(
    id      int NOT NULL AUTO_INCREMENT,
    version int,
    PRIMARY KEY (id)
);
alter table settings
    add constraint fk_settings_version foreign key (version) references version (id);
INSERT INTO settings VALUES (1, 1);

CREATE TABLE toc_config
(
    id                      int NOT NULL AUTO_INCREMENT,
    settings_key            int NOT NULL,
    kitty_debit             int NOT NULL,
    annual_toc_cost         int NOT NULL,
    quarterly_toc_cost      int NOT NULL,
    quarterly_num_payouts   int NOT NULL,
    regular_buy_in_cost     int NOT NULL,
    regular_rebuy_cost      int NOT NULL,
    regular_rebuy_toc_debit int NOT NULL,
    settings                int NOT NULL,
    PRIMARY KEY (id)
);
alter table toc_config
    add constraint fk_toc_config_settings foreign key (settings) references settings (id);
INSERT INTO toc_config VALUES (1, 2020, 10, 20, 20, 3, 40, 40, 20, 1);
INSERT INTO toc_config VALUES (2, 2021, 10, 20, 20, 3, 40, 40, 20, 1);

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
