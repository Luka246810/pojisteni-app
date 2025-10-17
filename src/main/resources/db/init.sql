-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Počítač: 127.0.0.1
-- Vytvořeno: Úte 07. říj 2025, 11:46
-- Verze serveru: 10.4.32-MariaDB
-- Verze PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Databáze: `projekt_pojistovna`
--

-- --------------------------------------------------------

--
-- Struktura tabulky `pojisteny`
--

CREATE TABLE `pojisteny` (
  `id` int(11) NOT NULL,
  `jmeno` varchar(50) NOT NULL,
  `prijmeni` varchar(50) NOT NULL,
  `telefon` varchar(50) NOT NULL,
  `vek` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `pohlavi` varchar(20) DEFAULT NULL,
  `mesto` varchar(100) DEFAULT NULL,
  `ulice` varchar(120) DEFAULT NULL,
  `cislo_popisne` varchar(20) DEFAULT NULL,
  `psc` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Vypisuji data pro tabulku `pojisteny`
--

INSERT INTO `pojisteny` (`id`, `jmeno`, `prijmeni`, `telefon`, `vek`, `email`, `pohlavi`, `mesto`, `ulice`, `cislo_popisne`, `psc`) VALUES
(7, 'Petr', 'Mráček', '+420 111 222 333', 35, 'p-mrak@gmail.com', 'muz', 'Drahanovice', 'Ludéřov', '58', '783 55'),
(8, 'Eva ', 'Večová', '+420 111 222 333', 49, 'yes@no.cz', NULL, 'Drahanovice', 'Mrkvova', '75', '987 44'),
(14, 'Lukáš', 'Pátek', '+420 738 555 565', 29, 'zdar@gmail.com', NULL, 'Olomouc', 'Střížkov', '78', '783 44'),
(15, 'Petr', 'Petrovič', '///', 15, NULL, NULL, 'Litovel', 'Dolní', '51', '999 88'),
(16, 'user', 'user', '888 777 444', 38, 'user@user.cz', NULL, 'Praha', 'Userova', '99', '222 22');

-- --------------------------------------------------------

--
-- Struktura tabulky `pojistka_osoba`
--

CREATE TABLE `pojistka_osoba` (
  `pojistka_id` int(11) NOT NULL,
  `osoba_id` int(11) NOT NULL,
  `role` enum('POJISTNIK','POJISTENY') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Vypisuji data pro tabulku `pojistka_osoba`
--

INSERT INTO `pojistka_osoba` (`pojistka_id`, `osoba_id`, `role`) VALUES
(20, 8, 'POJISTNIK'),
(20, 8, 'POJISTENY'),
(26, 8, 'POJISTENY'),
(26, 14, 'POJISTNIK'),
(26, 14, 'POJISTENY'),
(27, 7, 'POJISTNIK');

-- --------------------------------------------------------

--
-- Struktura tabulky `pojistna_udalost`
--

CREATE TABLE `pojistna_udalost` (
  `id` int(11) NOT NULL,
  `pojisteny_id` int(11) NOT NULL,
  `typ_pojisteni_id` int(11) DEFAULT NULL,
  `datum` date NOT NULL,
  `popis` varchar(1000) NOT NULL,
  `skoda` decimal(12,2) NOT NULL DEFAULT 0.00,
  `stav` enum('NOVA','RESENA','UZAVRENA') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Vypisuji data pro tabulku `pojistna_udalost`
--

INSERT INTO `pojistna_udalost` (`id`, `pojisteny_id`, `typ_pojisteni_id`, `datum`, `popis`, `skoda`, `stav`) VALUES
(3, 8, NULL, '2025-09-26', 'Auto ', 68300.00, 'RESENA'),
(4, 14, NULL, '2025-10-03', 'Auto se rozbilo ', 65200.00, 'NOVA'),
(9, 16, NULL, '2025-10-07', 'Srážka osobních aut', 98421.00, 'RESENA');

-- --------------------------------------------------------

--
-- Struktura tabulky `typ_pojisteni`
--

CREATE TABLE `typ_pojisteni` (
  `id` int(11) NOT NULL,
  `pojisteny_id` int(11) NOT NULL,
  `nazev` varchar(100) NOT NULL,
  `castka` decimal(12,2) NOT NULL,
  `platnost_do` date NOT NULL DEFAULT curdate(),
  `platnost_od` date NOT NULL DEFAULT curdate()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Vypisuji data pro tabulku `typ_pojisteni`
--

INSERT INTO `typ_pojisteni` (`id`, `pojisteny_id`, `nazev`, `castka`, `platnost_do`, `platnost_od`) VALUES
(20, 8, 'Auto - Škoda', 4853.00, '2027-10-21', '2025-08-21'),
(26, 14, 'Životka - Family + ', 3258.00, '2027-12-30', '2025-09-26'),
(27, 7, 'Dům ', 4532.00, '2026-10-02', '2025-10-02'),
(33, 16, 'User\'s Family', 5412.00, '2026-10-22', '2025-10-07');

-- --------------------------------------------------------

--
-- Struktura tabulky `uzivatel`
--

CREATE TABLE `uzivatel` (
  `id` int(11) NOT NULL,
  `username` varchar(80) NOT NULL,
  `password_hash` varchar(100) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `pojisteny_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Vypisuji data pro tabulku `uzivatel`
--

INSERT INTO `uzivatel` (`id`, `username`, `password_hash`, `enabled`, `pojisteny_id`) VALUES
(4, 'pepa', '{bcrypt}$2a$10$68X51UfAc4CfwfnceWZrjef6nqEk.YMAEbevoGi95Y9Ua8F6v8PjO', 1, 7),
(5, 'eva', '{bcrypt}$2a$10$68X51UfAc4CfwfnceWZrjef6nqEk.YMAEbevoGi95Y9Ua8F6v8PjO', 1, 8),
(8, 'petr', '{bcrypt}$2a$10$68X51UfAc4CfwfnceWZrjef6nqEk.YMAEbevoGi95Y9Ua8F6v8PjO', 1, 7),
(12, 'luka', '{bcrypt}$2a$10$68X51UfAc4CfwfnceWZrjef6nqEk.YMAEbevoGi95Y9Ua8F6v8PjO', 1, NULL),
(13, 'Luka2468', '{bcrypt}$2a$10$E50DeUX57VmFvKo4tI5NguRvUtgp7DfwW21yDwI.VxMmjssepESgK', 1, 14),
(14, 'admin', '{bcrypt}$2a$10$qQttYxhKaiUbc6rlyYCKweA98JMp9OB12Q6AujHiXEcjxn9MPU6GS', 1, NULL),
(15, 'user', '{bcrypt}$2a$10$4lGvMSlrfWdiVfrQRqit4eoovEEGuFCdG.SX5Q0EsBtouccWS4/JS', 1, 16);

-- --------------------------------------------------------

--
-- Struktura tabulky `uzivatel_role`
--

CREATE TABLE `uzivatel_role` (
  `uzivatel_id` int(11) NOT NULL,
  `role_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Vypisuji data pro tabulku `uzivatel_role`
--

INSERT INTO `uzivatel_role` (`uzivatel_id`, `role_name`) VALUES
(5, 'ROLE_USER'),
(8, 'ROLE_USER'),
(12, 'ROLE_USER'),
(13, 'ROLE_USER'),
(14, 'ROLE_ADMIN'),
(15, 'ROLE_USER');

--
-- Indexy pro exportované tabulky
--

--
-- Indexy pro tabulku `pojisteny`
--
ALTER TABLE `pojisteny`
  ADD PRIMARY KEY (`id`);

--
-- Indexy pro tabulku `pojistka_osoba`
--
ALTER TABLE `pojistka_osoba`
  ADD PRIMARY KEY (`pojistka_id`,`osoba_id`,`role`),
  ADD KEY `idx_pojistka` (`pojistka_id`),
  ADD KEY `idx_osoba` (`osoba_id`);

--
-- Indexy pro tabulku `pojistna_udalost`
--
ALTER TABLE `pojistna_udalost`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_ud_typ` (`typ_pojisteni_id`),
  ADD KEY `idx_udalost_poj_datum` (`pojisteny_id`,`datum`);

--
-- Indexy pro tabulku `typ_pojisteni`
--
ALTER TABLE `typ_pojisteni`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_pojisteny` (`pojisteny_id`);

--
-- Indexy pro tabulku `uzivatel`
--
ALTER TABLE `uzivatel`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `uq_uzivatel_username` (`username`),
  ADD KEY `fk_uzivatel_pojisteny_20250902` (`pojisteny_id`);

--
-- Indexy pro tabulku `uzivatel_role`
--
ALTER TABLE `uzivatel_role`
  ADD PRIMARY KEY (`uzivatel_id`,`role_name`);

--
-- AUTO_INCREMENT pro tabulky
--

--
-- AUTO_INCREMENT pro tabulku `pojisteny`
--
ALTER TABLE `pojisteny`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT pro tabulku `pojistna_udalost`
--
ALTER TABLE `pojistna_udalost`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pro tabulku `typ_pojisteni`
--
ALTER TABLE `typ_pojisteni`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT pro tabulku `uzivatel`
--
ALTER TABLE `uzivatel`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Omezení pro exportované tabulky
--

--
-- Omezení pro tabulku `pojistka_osoba`
--
ALTER TABLE `pojistka_osoba`
  ADD CONSTRAINT `fk_po_os` FOREIGN KEY (`osoba_id`) REFERENCES `pojisteny` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_po_poj` FOREIGN KEY (`pojistka_id`) REFERENCES `typ_pojisteni` (`id`) ON DELETE CASCADE;

--
-- Omezení pro tabulku `pojistna_udalost`
--
ALTER TABLE `pojistna_udalost`
  ADD CONSTRAINT `fk_ud_poj` FOREIGN KEY (`pojisteny_id`) REFERENCES `pojisteny` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ud_typ` FOREIGN KEY (`typ_pojisteni_id`) REFERENCES `typ_pojisteni` (`id`) ON DELETE SET NULL;

--
-- Omezení pro tabulku `typ_pojisteni`
--
ALTER TABLE `typ_pojisteni`
  ADD CONSTRAINT `fk_pojisteny` FOREIGN KEY (`pojisteny_id`) REFERENCES `pojisteny` (`id`) ON DELETE CASCADE;

--
-- Omezení pro tabulku `uzivatel`
--
ALTER TABLE `uzivatel`
  ADD CONSTRAINT `fk_uzivatel_pojisteny_20250902` FOREIGN KEY (`pojisteny_id`) REFERENCES `pojisteny` (`id`) ON DELETE SET NULL;

--
-- Omezení pro tabulku `uzivatel_role`
--
ALTER TABLE `uzivatel_role`
  ADD CONSTRAINT `fk_role_user_20250902` FOREIGN KEY (`uzivatel_id`) REFERENCES `uzivatel` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
