# password is password
INSERT INTO player
VALUES (1, 'Gil', 'Pratte', '5121231235', 'gilpratte@texastoc.com',
        '$2a$10$qXQo4z4oXKPEKyYO7bAQmOQ9PhIcHK4LOo/L1U9j/xkLEmseLWECK'),
       (2, 'Guest', 'User', '5121231235', 'guest@texastoc.com',
        '$2a$10$qXQo4z4oXKPEKyYO7bAQmOQ9PhIcHK4LOo/L1U9j/xkLEmseLWECK'),
       (3, 'Guest', 'Admin', '5121231235', 'admin@texastoc.com',
        '$2a$10$qXQo4z4oXKPEKyYO7bAQmOQ9PhIcHK4LOo/L1U9j/xkLEmseLWECK');
INSERT INTO role
VALUES (1, 'ADMIN', 1),
       (2, 'USER', 1),
       (3, 'USER', 2),
       (4, 'ADMIN', 3),
       (5, 'USER', 3);
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
INSERT INTO version VALUES (1, '2.21');
INSERT INTO settings VALUES (1, 1);
INSERT INTO toc_config VALUES (1, 10, 20, 20, 3, 40, 40, 20, 2020, 1);
INSERT INTO toc_config VALUES (2, 0, 20, 20, 3, 40, 40, 20, 2021, 1);