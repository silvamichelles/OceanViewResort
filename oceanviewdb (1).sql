-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 03, 2026 at 06:05 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `oceanviewdb`
--

-- --------------------------------------------------------

--
-- Table structure for table `bills`
--

CREATE TABLE `bills` (
  `bill_id` int(11) NOT NULL,
  `res_id` int(11) DEFAULT NULL,
  `total_nights` int(11) DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `billing_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bills`
--

INSERT INTO `bills` (`bill_id`, `res_id`, `total_nights`, `total_amount`, `billing_date`) VALUES
(1, 1, 2, 10000.00, '2026-03-02 15:09:53'),
(2, 2, 5, 75000.00, '2026-03-02 15:09:53'),
(3, 3, 4, 34000.00, '2026-03-02 15:09:53'),
(4, 4, 3, 15000.00, '2026-03-02 17:39:23'),
(5, 5, 3, 15000.00, '2026-03-02 17:40:12'),
(6, 6, 1, 5000.00, '2026-03-02 18:40:07'),
(7, 8, 12, 60000.00, '2026-03-03 04:48:08'),
(8, 9, 9, 45000.00, '2026-03-03 04:53:27'),
(9, 10, 19, 95000.00, '2026-03-03 04:59:15');

-- --------------------------------------------------------

--
-- Table structure for table `guests`
--

CREATE TABLE `guests` (
  `guest_id` int(11) NOT NULL,
  `guest_name` varchar(100) NOT NULL,
  `address` text DEFAULT NULL,
  `contact_number` varchar(15) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `guests`
--

INSERT INTO `guests` (`guest_id`, `guest_name`, `address`, `contact_number`) VALUES
(1, 'Kamal Perera', '123 Beach Road, Colombo', '0771234567'),
(2, 'Nimali Fernando', '45 Kandy Road, Kandy', '0719876543'),
(3, 'John Smith', '78 Ocean Drive, Galle', '0705556666'),
(4, 'Sarah Wijesinghe', '12 Galle Road, Matara', '0773344556'),
(5, 'Arjun Silva', 'Wattala', '0712233445');

-- --------------------------------------------------------

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `res_id` int(11) NOT NULL,
  `reservation_number` varchar(20) NOT NULL,
  `guest_id` int(11) DEFAULT NULL,
  `room_id` int(11) DEFAULT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservations`
--

INSERT INTO `reservations` (`res_id`, `reservation_number`, `guest_id`, `room_id`, `check_in_date`, `check_out_date`) VALUES
(1, 'RES-1001', 1, 1, '2026-03-10', '2026-03-12'),
(2, 'RES-1002', 2, 3, '2026-03-15', '2026-03-20'),
(3, 'RES-1003', 3, 2, '2026-03-01', '2026-03-05'),
(4, 'RES-1772473163177', 1, 1, '2026-03-07', '2026-03-10'),
(5, 'RES-1772473212481', 1, 1, '2026-03-02', '2026-03-05'),
(6, 'RES-1772476807663', 1, 1, '2026-03-04', '2026-03-05'),
(8, 'RES-1772513288805', 1, 1, '2026-03-13', '2026-03-25'),
(9, 'RES-1772513607648', 1, 1, '2026-03-03', '2026-03-12'),
(10, 'RES-1772513955954', 1, 1, '2026-03-21', '2026-04-09');

--
-- Triggers `reservations`
--
DELIMITER $$
CREATE TRIGGER `AfterReservationInsert` AFTER INSERT ON `reservations` FOR EACH ROW BEGIN
    DECLARE nights INT;
    DECLARE room_rate DECIMAL(10, 2);
    
    -- රැඳී සිටින දින ගණන ගණනය කිරීම
    SET nights = DATEDIFF(NEW.check_out_date, NEW.check_in_date);
    IF nights = 0 THEN SET nights = 1; END IF;
    
    -- අදාළ කාමරයේ මිල ලබා ගැනීම
    SELECT rate_per_night INTO room_rate FROM Rooms WHERE room_id = NEW.room_id;
    
    -- Bills වගුවට ස්වයංක්‍රීයව දත්ත ඇතුළත් කිරීම 
    INSERT INTO Bills (res_id, total_nights, total_amount)
    VALUES (NEW.res_id, nights, (nights * room_rate));
    
    -- කාමරයේ තත්ත්වය 'Booked' ලෙස වෙනස් කිරීම
    UPDATE Rooms SET status = 'Booked' WHERE room_id = NEW.room_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `room_id` int(11) NOT NULL,
  `room_type` varchar(50) NOT NULL,
  `price` double DEFAULT 0,
  `rate_per_night` decimal(10,2) NOT NULL,
  `status` varchar(20) DEFAULT 'Available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`room_id`, `room_type`, `price`, `rate_per_night`, `status`) VALUES
(1, 'Single', 8500, 5000.00, 'Booked'),
(2, 'Double', 12000, 8500.00, 'Booked'),
(3, 'Deluxe', 12000, 15000.00, 'Booked'),
(4, 'Single', 8500, 5000.00, 'Available'),
(5, 'Double', 12000, 8500.00, 'Available'),
(6, 'Ocean View Suite', 0, 25000.00, 'Available');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) DEFAULT 'Staff'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role`) VALUES
(1, 'admin', 'admin123', 'Admin'),
(2, 'manager', 'manager123', 'Manager'),
(3, 'staff', 'staff123', 'Staff'),
(4, 'a', 'a', 'Staff');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bills`
--
ALTER TABLE `bills`
  ADD PRIMARY KEY (`bill_id`),
  ADD KEY `res_id` (`res_id`);

--
-- Indexes for table `guests`
--
ALTER TABLE `guests`
  ADD PRIMARY KEY (`guest_id`);

--
-- Indexes for table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`res_id`),
  ADD UNIQUE KEY `reservation_number` (`reservation_number`),
  ADD KEY `guest_id` (`guest_id`),
  ADD KEY `room_id` (`room_id`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`room_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bills`
--
ALTER TABLE `bills`
  MODIFY `bill_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `guests`
--
ALTER TABLE `guests`
  MODIFY `guest_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `res_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `room_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bills`
--
ALTER TABLE `bills`
  ADD CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`res_id`) REFERENCES `reservations` (`res_id`) ON DELETE CASCADE;

--
-- Constraints for table `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`guest_id`) REFERENCES `guests` (`guest_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
