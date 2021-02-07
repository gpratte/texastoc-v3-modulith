DROP TABLE IF EXISTS season;
DROP TABLE IF EXISTS quarterlyseason;
DROP TABLE IF EXISTS seasonplayer;
DROP TABLE IF EXISTS quarterlyseasonplayer;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS seats_per_table;
DROP TABLE IF EXISTS table_request;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS game_table;
DROP TABLE IF EXISTS game_player;
DROP TABLE IF EXISTS game_payout;
DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS seating;
DROP TABLE IF EXISTS quarterlyseasonpayout;
DROP TABLE IF EXISTS seasonpayout;
DROP TABLE IF EXISTS seasonpayoutsettings;
DROP TABLE IF EXISTS toc_config;
DROP TABLE IF EXISTS settings;
DROP TABLE IF EXISTS version;
DROP TABLE IF EXISTS historicalseasonplayer;
DROP TABLE IF EXISTS historicalseason;

CREATE TABLE season
(
    id                                int NOT NULL AUTO_INCREMENT,
    startDate                         date      DEFAULT NULL,
    endDate                           date      DEFAULT NULL,
    kittyPerGame                      int       DEFAULT NULL,
    tocPerGame                        int       DEFAULT NULL,
    quarterlyTocPerGame               int       DEFAULT NULL,
    quarterlyTocPayouts               int       DEFAULT NULL,
    buyInCost                         int       DEFAULT NULL,
    rebuyAddOnCost                    int       DEFAULT NULL,
    rebuyAddOnTocDebit                int       DEFAULT NULL,
    buyInCollected                    int       DEFAULT NULL,
    rebuyAddOnCollected               int       DEFAULT NULL,
    annualTocCollected                int       DEFAULT NULL,
    totalCollected                    int       DEFAULT NULL,
    annualTocFromRebuyAddOnCalculated int       DEFAULT NULL,
    rebuyAddOnLessAnnualTocCalculated int       DEFAULT NULL,
    totalCombinedAnnualTocCalculated  int       DEFAULT NULL,
    kittyCalculated                   int       DEFAULT NULL,
    prizePotCalculated                int       DEFAULT NULL,
    numGames                          int       DEFAULT NULL,
    numGamesPlayed                    int       DEFAULT NULL,
    finalized                         boolean   DEFAULT NULL,
    lastCalculated                    timestamp DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE quarterlyseason
(
    id             int NOT NULL AUTO_INCREMENT,
    seasonId       int NOT NULL,
    startDate      date      DEFAULT NULL,
    endDate        date      DEFAULT NULL,
    finalized      boolean   DEFAULT NULL,
    quarter        int NOT NULL,
    numGames       int       DEFAULT NULL,
    numGamesPlayed int       DEFAULT NULL,
    qTocCollected  int       DEFAULT NULL,
    qTocPerGame    int       DEFAULT NULL,
    numPayouts     int NOT NULL,
    lastCalculated timestamp DEFAULT NULL,
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
    id         int NOT NULL AUTO_INCREMENT,
    first_name varchar(32)  DEFAULT NULL,
    last_name  varchar(32)  DEFAULT NULL,
    phone      varchar(32)  DEFAULT NULL,
    email      varchar(64)  DEFAULT NULL,
    password   varchar(255) DEFAULT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE player
    ADD UNIQUE (email);

CREATE TABLE role
(
    id     int NOT NULL AUTO_INCREMENT,
    type   varchar(255) DEFAULT NULL,
    player int,
    PRIMARY KEY (id)
);
alter table role
    add constraint fk_role_player foreign key (player) references player (id);

CREATE TABLE seating
(
    id      int NOT NULL AUTO_INCREMENT,
    game_id int NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE seating
    ADD UNIQUE (game_id);

CREATE TABLE game
(
    id                                      int  NOT NULL AUTO_INCREMENT,
    host_id                                 int         DEFAULT NULL,
    game_date                               date NOT NULL,
    transport_required                      boolean     DEFAULT FALSE,

    host_name                               varchar(64) DEFAULT NULL,
    season_id                               int  NOT NULL,
    q_season_id                             int  NOT NULL,
    quarter                                 varchar(16) DEFAULT NULL,
    season_game_num                         int         DEFAULT NULL,
    quarterly_game_num                      int         DEFAULT NULL,

    kitty_cost                              int         DEFAULT 0,
    buy_in_cost                             int         DEFAULT 0,
    rebuy_add_on_cost                       int         DEFAULT 0,
    rebuy_add_on_toc_debit_cost             int         DEFAULT 0,
    annual_toc_cost                         int         DEFAULT 0,
    quarterly_toc_cost                      int         DEFAULT 0,

    buy_in_collected                        int         DEFAULT 0,
    rebuy_add_on_collected                  int         DEFAULT 0,
    annual_toc_collected                    int         DEFAULT 0,
    quarterly_toc_collected                 int         DEFAULT 0,
    total_collected                         int         DEFAULT 0,

    annual_toc_from_rebuy_add_on_calculated int         DEFAULT 0,
    rebuy_add_on_less_annual_toc_calculated int         DEFAULT 0,
    total_combined_toc_calculated           int         DEFAULT 0,
    kitty_calculated                        int         DEFAULT 0,
    prize_pot_calculated                    int         DEFAULT 0,

    num_players                             int         DEFAULT 0,
    num_paid_players                        int         DEFAULT 0,
    started                                 timestamp NULL DEFAULT NULL,
    last_calculated                         timestamp   DEFAULT NULL,
    chopped                                 boolean     DEFAULT TRUE,
    can_rebuy                               boolean     DEFAULT TRUE,
    finalized                               boolean     DEFAULT FALSE,
    payout_delta                            int         DEFAULT NULL,
    seating_id                              int,
    PRIMARY KEY (id)
);
alter table game
    add constraint fk_game_seating foreign key (seating_id) references seating (id);

CREATE TABLE game_player
(
    id                        int NOT NULL AUTO_INCREMENT,
    player_id                 int NOT NULL,
    game_id                   int NOT NULL,
    bought_in                 boolean     DEFAULT NULL,
    rebought                  boolean     DEFAULT NULL,
    annual_toc_participant    boolean     DEFAULT NULL,
    quarterly_toc_participant boolean     DEFAULT NULL,
    round_updates             boolean     DEFAULT FALSE,
    place                     int         DEFAULT NULL,
    knocked_out               boolean     DEFAULT FALSE,
    chop                      int         DEFAULT NULL,

    season_id                 int NOT NULL,
    q_season_id               int NOT NULL,
    first_name                varchar(64) DEFAULT NULL,
    last_name                 varchar(64) DEFAULT NULL,
    email                     varchar(64) DEFAULT NULL,
    toc_points                int         DEFAULT NULL,
    toc_chop_points           int         DEFAULT NULL,
    q_toc_points              int         DEFAULT NULL,
    q_toc_chop_points         int         DEFAULT NULL,

    buy_in_collected          boolean     DEFAULT NULL,
    rebuy_add_on_collected    boolean     DEFAULT NULL,
    annual_toc_collected      boolean     DEFAULT NULL,
    quarterly_toc_collected   boolean     DEFAULT NULL,

    game                      int NOT NULL,
    game_key                  int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY game_player_unique (game_id, player_id)
);
alter table game_player
    add constraint fk_game_player_game foreign key (game) references game (id);

CREATE TABLE game_payout
(
    id          int NOT NULL AUTO_INCREMENT,
    game_id     int NOT NULL,
    place       int NOT NULL,
    amount      int DEFAULT NULL,
    chop_amount int DEFAULT NULL,
    game        int NOT NULL,
    game_key    int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY game_payout_unique (game_id, place)
);
alter table game_payout
    add constraint fk_game_payout_game foreign key (game) references game (id);

CREATE TABLE seats_per_table
(
    id          int NOT NULL AUTO_INCREMENT,
    num_seats   int NOT NULL,
    table_num   int NOT NULL,
    seating     int NOT NULL,
    seating_key int NOT NULL,
    PRIMARY KEY (id)
);
alter table seats_per_table
    add constraint fk_seats_per_table_seating foreign key (seating) references seating (id);

CREATE TABLE table_request
(
    id               int          NOT NULL AUTO_INCREMENT,
    game_player_id   int          NOT NULL,
    game_player_name varchar(128) NOT NULL,
    table_num        int          NOT NULL,
    seating          int          NOT NULL,
    seating_key      int          NOT NULL,
    PRIMARY KEY (id)
);
alter table table_request
    add constraint fk_table_request_seating foreign key (seating) references seating (id);

CREATE TABLE game_table
(
    id          int NOT NULL AUTO_INCREMENT,
    table_num   int NOT NULL,
    seating     int NOT NULL,
    seating_key int NOT NULL,
    PRIMARY KEY (id)
);
alter table game_table
    add constraint fk_game_table_seating foreign key (seating) references seating (id);

CREATE TABLE seat
(
    id               int NOT NULL AUTO_INCREMENT,
    seat_num         int NOT NULL,
    table_num        int NOT NULL,
    game_player_id   int          DEFAULT NULL,
    game_player_name varchar(128) DEFAULT NULL,
    game_table       int NOT NULL,
    game_table_key   int NOT NULL,
    PRIMARY KEY (id)
);
alter table seat
    add constraint fk_seat_game_table foreign key (game_table) references game_table (id);

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

CREATE TABLE version
(
    id      int        NOT NULL AUTO_INCREMENT,
    version varchar(8) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE settings
(
    id      int NOT NULL AUTO_INCREMENT,
    version int,
    PRIMARY KEY (id)
);
alter table settings
    add constraint fk_settings_version foreign key (version) references version (id);

CREATE TABLE toc_config
(
    id                      int NOT NULL AUTO_INCREMENT,
    kitty_debit             int NOT NULL,
    annual_toc_cost         int NOT NULL,
    quarterly_toc_cost      int NOT NULL,
    quarterly_num_payouts   int NOT NULL,
    regular_buy_in_cost     int NOT NULL,
    regular_rebuy_cost      int NOT NULL,
    regular_rebuy_toc_debit int NOT NULL,
    year                    int NOT NULL,
    settings                int NOT NULL,
    PRIMARY KEY (id)
);
alter table toc_config
    add constraint fk_toc_config_settings foreign key (settings) references settings (id);

CREATE TABLE historicalseasonplayer
(
    id        int NOT NULL AUTO_INCREMENT,
    seasonId  int NOT NULL,
    firstName varchar(64),
    lastName  varchar(64),
    name      varchar(64),
    points    int,
    entries   int,
    PRIMARY KEY (id)
);

CREATE TABLE historicalseason
(
    id        int NOT NULL AUTO_INCREMENT,
    seasonId  int NOT NULL,
    startYear int DEFAULT NULL,
    endYear   int DEFAULT NULL,
    PRIMARY KEY (id)
);
