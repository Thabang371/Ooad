-- 10 sample customer records (assignment requirement), each with one or more accounts.
-- Loaded automatically the first time the app starts with an empty database.
-- Every sample customer's password is:  password123

INSERT INTO customers (customer_id, first_name, surname, address, username, password_hash) VALUES
 ('C001', 'Kabo',      'Molefe',   'Plot 123, Block 8, Gaborone',   'kabo',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C002', 'Neo',       'Sebina',   'Plot 45, Tlokweng',             'neo',       'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C003', 'Lorato',    'Kgosi',    'Plot 9, Francistown',           'lorato',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C004', 'Tumelo',    'Dube',     'Plot 712, Maun',                'tumelo',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C005', 'Boitumelo', 'Phiri',    'Plot 30, Mogoditshane',         'boitumelo', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C006', 'Onalenna',  'Mosweu',   'Plot 6, Palapye',               'onalenna',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C007', 'Kagiso',    'Ntsima',   'Plot 88, Serowe',               'kagiso',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C008', 'Mpho',      'Seretse',  'Plot 15, Lobatse',              'mpho',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C009', 'Masego',    'Ramotswa', 'Plot 204, Kanye',               'masego',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
 ('C010', 'Thato',     'Kebonang', 'Plot 51, Selebi-Phikwe',        'thato',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

INSERT INTO accounts (account_number, customer_id, account_type, branch, balance, employer_name, employer_address) VALUES
 ('SAV-1001', 'C001', 'SAVINGS',    'Gaborone Main',   2500.00, NULL, NULL),
 ('INV-2001', 'C001', 'INVESTMENT', 'Gaborone Main',  10000.00, NULL, NULL),
 ('CHQ-3001', 'C001', 'CHEQUE',     'Gaborone Main',   8200.00, 'Debswana', 'Debswana House, Main Mall, Gaborone'),
 ('SAV-1002', 'C002', 'SAVINGS',    'Tlokweng',         300.00, NULL, NULL),
 ('INV-2002', 'C003', 'INVESTMENT', 'Francistown',      750.00, NULL, NULL),
 ('CHQ-3002', 'C003', 'CHEQUE',     'Francistown',     4100.00, 'Botswana Railways', 'Mahalapye'),
 ('SAV-1003', 'C004', 'SAVINGS',    'Maun',            1200.00, NULL, NULL),
 ('CHQ-3003', 'C005', 'CHEQUE',     'Game City',       6350.00, 'BURS', 'Plot 53976, Kudumatse Drive, Gaborone'),
 ('INV-2003', 'C006', 'INVESTMENT', 'Palapye',         5000.00, NULL, NULL),
 ('SAV-1004', 'C007', 'SAVINGS',    'Serowe',           890.00, NULL, NULL),
 ('CHQ-3004', 'C008', 'CHEQUE',     'Lobatse',         3000.00, 'Botswana Meat Commission', 'Lobatse'),
 ('SAV-1005', 'C009', 'SAVINGS',    'Kanye',           4100.00, NULL, NULL),
 ('INV-2004', 'C010', 'INVESTMENT', 'Selebi-Phikwe',   1500.00, NULL, NULL);
