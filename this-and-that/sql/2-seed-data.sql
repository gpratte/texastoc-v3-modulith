-- noinspection SqlNoDataSourceInspectionForFile
-- Run this file from command line
--   mysql -u <user> -p < 2-seed-data.sql

insert into tocconfig (kittyDebit, annualTocCost, quarterlyTocCost, quarterlyNumPayouts, regularBuyInCost, regularRebuyCost, regularRebuyTocDebit, doubleBuyInCost, doubleRebuyCost, doubleRebuyTocDebit) values (10, 20, 20, 3, 40, 40, 20, 40, 40, 20);

INSERT INTO role (id, description, name) VALUES (1, 'Admin role', 'ADMIN');
INSERT INTO role (id, description, name) VALUES (2, 'User role', 'USER');

insert into payout (numPayouts, place, percent) values (2, 1, 0.65);
insert into payout (numPayouts, place, percent) values (2, 2, 0.35);
insert into payout (numPayouts, place, percent) values (3, 1, 0.50);
insert into payout (numPayouts, place, percent) values (3, 2, 0.30);
insert into payout (numPayouts, place, percent) values (3, 3, 0.20);
insert into payout (numPayouts, place, percent) values (4, 1, 0.45);
insert into payout (numPayouts, place, percent) values (4, 2, 0.25);
insert into payout (numPayouts, place, percent) values (4, 3, 0.18);
insert into payout (numPayouts, place, percent) values (4, 4, 0.12);
insert into payout (numPayouts, place, percent) values (5, 1, 0.40);
insert into payout (numPayouts, place, percent) values (5, 2, 0.23);
insert into payout (numPayouts, place, percent) values (5, 3, 0.16);
insert into payout (numPayouts, place, percent) values (5, 4, 0.12);
insert into payout (numPayouts, place, percent) values (5, 5, 0.09);
insert into payout (numPayouts, place, percent) values (6, 1, 0.38);
insert into payout (numPayouts, place, percent) values (6, 2, 0.22);
insert into payout (numPayouts, place, percent) values (6, 3, 0.15);
insert into payout (numPayouts, place, percent) values (6, 4, 0.11);
insert into payout (numPayouts, place, percent) values (6, 5, 0.08);
insert into payout (numPayouts, place, percent) values (6, 6, 0.06);
insert into payout (numPayouts, place, percent) values (7, 1, 0.35);
insert into payout (numPayouts, place, percent) values (7, 2, 0.21);
insert into payout (numPayouts, place, percent) values (7, 3, 0.15);
insert into payout (numPayouts, place, percent) values (7, 4, 0.11);
insert into payout (numPayouts, place, percent) values (7, 5, 0.08);
insert into payout (numPayouts, place, percent) values (7, 6, 0.06);
insert into payout (numPayouts, place, percent) values (7, 7, 0.04);
insert into payout (numPayouts, place, percent) values (8, 1, 0.335);
insert into payout (numPayouts, place, percent) values (8, 2, 0.20);
insert into payout (numPayouts, place, percent) values (8, 3, 0.145);
insert into payout (numPayouts, place, percent) values (8, 4, 0.11);
insert into payout (numPayouts, place, percent) values (8, 5, 0.08);
insert into payout (numPayouts, place, percent) values (8, 6, 0.06);
insert into payout (numPayouts, place, percent) values (8, 7, 0.04);
insert into payout (numPayouts, place, percent) values (8, 8, 0.03);
insert into payout (numPayouts, place, percent) values (9, 1, 0.32);
insert into payout (numPayouts, place, percent) values (9, 2, 0.195);
insert into payout (numPayouts, place, percent) values (9, 3, 0.14);
insert into payout (numPayouts, place, percent) values (9, 4, 0.11);
insert into payout (numPayouts, place, percent) values (9, 5, 0.08);
insert into payout (numPayouts, place, percent) values (9, 6, 0.06);
insert into payout (numPayouts, place, percent) values (9, 7, 0.04);
insert into payout (numPayouts, place, percent) values (9, 8, 0.03);
insert into payout (numPayouts, place, percent) values (9, 9, 0.025);
insert into payout (numPayouts, place, percent) values (10, 1, 0.30);
insert into payout (numPayouts, place, percent) values (10, 2, 0.19);
insert into payout (numPayouts, place, percent) values (10, 3, 0.1325);
insert into payout (numPayouts, place, percent) values (10, 4, 0.105);
insert into payout (numPayouts, place, percent) values (10, 5, 0.075);
insert into payout (numPayouts, place, percent) values (10, 6, 0.055);
insert into payout (numPayouts, place, percent) values (10, 7, 0.0375);
insert into payout (numPayouts, place, percent) values (10, 8, 0.03);
insert into payout (numPayouts, place, percent) values (10, 9, 0.0225);
insert into payout (numPayouts, place, percent) values (10, 10, 0.015);
