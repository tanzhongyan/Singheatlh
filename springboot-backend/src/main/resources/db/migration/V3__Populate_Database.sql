-- =====================================================
-- Database Population Script for SingHealth Clinic System
-- Description: Populates tables with sample data from CSV files
-- =====================================================

-- =====================================================
-- 1. POPULATE CLINIC TABLE
-- Description: Create 10 clinics (General and Specialist)
-- =====================================================

INSERT INTO Clinic (clinic_id, type, name, address, telephone_number, opening_hours, closing_hours) VALUES
(1, 'G', 'SingHealth General Practice - Central', '1 Hospital Drive, Singapore 169608', '+65 6321 4377', '08:00:00', '18:00:00'),
(2, 'S', 'SingHealth Cardiology Centre', '2 Hospital Drive, Singapore 169609', '+65 6321 4378', '08:30:00', '17:30:00'),
(3, 'S', 'SingHealth Orthopedic Centre', '3 Hospital Drive, Singapore 169610', '+65 6321 4379', '09:00:00', '17:00:00'),
(4, 'G', 'SingHealth General Practice - East', '4 Hospital Drive, Singapore 169611', '+65 6321 4380', '08:00:00', '18:00:00'),
(5, 'S', 'SingHealth Dermatology Centre', '5 Hospital Drive, Singapore 169612', '+65 6321 4381', '08:30:00', '17:30:00'),
(6, 'S', 'SingHealth Neurology Centre', '6 Hospital Drive, Singapore 169613', '+65 6321 4382', '09:00:00', '17:00:00'),
(7, 'G', 'SingHealth General Practice - West', '7 Hospital Drive, Singapore 169614', '+65 6321 4383', '08:00:00', '18:00:00'),
(8, 'S', 'SingHealth Ophthalmology Centre', '8 Hospital Drive, Singapore 169615', '+65 6321 4384', '08:30:00', '17:30:00'),
(9, 'S', 'SingHealth ENT Centre', '9 Hospital Drive, Singapore 169616', '+65 6321 4385', '09:00:00', '17:00:00'),
(10, 'G', 'SingHealth General Practice - North', '10 Hospital Drive, Singapore 169617', '+65 6321 4386', '08:00:00', '18:00:00');

-- =====================================================
-- 2. POPULATE USER_PROFILE TABLE (PATIENTS)
-- Description: Insert patient data from patients.csv
-- =====================================================

INSERT INTO User_Profile (user_id, name, email, telephone_number, role, clinic_id) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Ashley Chung', 'ashy.chung@gmail.com', '+6591234567', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440002', 'Mary Lim', 'mary.lim@example.com', '+6591234568', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440003', 'David Wong', 'david.wong@example.com', '+6591234569', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440004', 'Sarah Ng', 'sarah.ng@example.com', '+6591234570', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440005', 'Michael Chen', 'michael.chen@example.com', '+6591234571', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440006', 'Emily Koh', 'emily.koh@example.com', '+6591234572', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440007', 'James Lee', 'james.lee@example.com', '+6591234573', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440008', 'Linda Teo', 'linda.teo@example.com', '+6591234574', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440009', 'Robert Ong', 'robert.ong@example.com', '+6591234575', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440010', 'Jessica Yeo', 'jessica.yeo@example.com', '+6591234576', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440011', 'Daniel Goh', 'daniel.goh@example.com', '+6591234577', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440012', 'Michelle Sim', 'michelle.sim@example.com', '+6591234578', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440013', 'Andrew Tan', 'andrew.tan@example.com', '+6591234579', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440014', 'Rachel Low', 'rachel.low@example.com', '+6591234580', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440015', 'Kevin Ng', 'kevin.ng@example.com', '+6591234581', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440016', 'Grace Lim', 'grace.lim@example.com', '+6591234582', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440017', 'Steven Tay', 'steven.tay@example.com', '+6591234583', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440018', 'Nicole Chua', 'nicole.chua@example.com', '+6591234584', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440019', 'Benjamin Ho', 'benjamin.ho@example.com', '+6591234585', 'P', NULL),
('550e8400-e29b-41d4-a716-446655440020', 'Amanda Yap', 'amanda.yap@example.com', '+6591234586', 'P', NULL);

-- =====================================================
-- 3. POPULATE DOCTOR TABLE
-- Description: Insert doctor data from doctors.csv
-- =====================================================

INSERT INTO Doctor (doctor_id, name, clinic_id) VALUES
('D000000001', 'Dr. Tan Wei Ming', 1),
('D000000002', 'Dr. Sarah Johnson', 1),
('D000000003', 'Dr. Rajesh Kumar', 2),
('D000000004', 'Dr. Emily Chen', 2),
('D000000005', 'Dr. Marcus Lim', 3),
('D000000006', 'Dr. Jennifer Wong', 3),
('D000000007', 'Dr. David Ng', 4),
('D000000008', 'Dr. Michelle Tan', 4),
('D000000009', 'Dr. Andrew Lee', 5),
('D000000010', 'Dr. Rachel Goh', 5),
('D000000011', 'Dr. Kevin Ong', 6),
('D000000012', 'Dr. Grace Teo', 6),
('D000000013', 'Dr. Steven Koh', 7),
('D000000014', 'Dr. Nicole Sim', 7),
('D000000015', 'Dr. Benjamin Yeo', 8),
('D000000016', 'Dr. Amanda Low', 8),
('D000000017', 'Dr. Daniel Chua', 9),
('D000000018', 'Dr. Jessica Tay', 9),
('D000000019', 'Dr. Robert Yap', 10),
('D000000020', 'Dr. Linda Ho', 10);

-- =====================================================
-- 4. POPULATE APPOINTMENT TABLE
-- Description: Insert appointment data from appointments.csv
-- =====================================================

INSERT INTO Appointment (appointment_id, patient_id, doctor_id, start_datetime, end_datetime, status) VALUES
('A000000001', '550e8400-e29b-41d4-a716-446655440001', 'D000000001', '2025-10-21 09:00:00', '2025-10-21 09:30:00', 'Upcoming'),
('A000000002', '550e8400-e29b-41d4-a716-446655440002', 'D000000001', '2025-10-21 10:00:00', '2025-10-21 10:30:00', 'Upcoming'),
('A000000003', '550e8400-e29b-41d4-a716-446655440003', 'D000000003', '2025-10-21 11:00:00', '2025-10-21 11:30:00', 'Upcoming'),
('A000000004', '550e8400-e29b-41d4-a716-446655440004', 'D000000003', '2025-10-21 14:00:00', '2025-10-21 14:30:00', 'Upcoming'),
('A000000005', '550e8400-e29b-41d4-a716-446655440005', 'D000000001', '2025-10-21 15:00:00', '2025-10-21 15:30:00', 'Upcoming'),
('A000000006', '550e8400-e29b-41d4-a716-446655440006', 'D000000001', '2025-10-22 09:00:00', '2025-10-22 09:30:00', 'Upcoming'),
('A000000007', '550e8400-e29b-41d4-a716-446655440007', 'D000000001', '2025-10-22 10:00:00', '2025-10-22 10:30:00', 'Upcoming'),
('A000000008', '550e8400-e29b-41d4-a716-446655440008', 'D000000001', '2025-10-22 11:00:00', '2025-10-22 11:30:00', 'Upcoming'),
('A000000009', '550e8400-e29b-41d4-a716-446655440009', 'D000000009', '2025-10-22 14:00:00', '2025-10-22 14:30:00', 'Upcoming'),
('A000000010', '550e8400-e29b-41d4-a716-446655440010', 'D000000001', '2025-10-22 15:00:00', '2025-10-22 15:30:00', 'Upcoming'),
('A000000011', '550e8400-e29b-41d4-a716-446655440001', 'D000000001', '2025-10-20 09:00:00', '2025-10-20 09:30:00', 'Completed'),
('A000000012', '550e8400-e29b-41d4-a716-446655440002', 'D000000002', '2025-10-20 10:00:00', '2025-10-20 10:30:00', 'Completed'),
('A000000013', '550e8400-e29b-41d4-a716-446655440003', 'D000000003', '2025-10-21 13:00:00', '2025-10-21 13:30:00', 'Upcoming'),
('A000000014', '550e8400-e29b-41d4-a716-446655440004', 'D000000004', '2025-10-19 14:00:00', '2025-10-19 14:30:00', 'Completed'),
('A000000015', '550e8400-e29b-41d4-a716-446655440005', 'D000000005', '2025-10-18 15:00:00', '2025-10-18 15:30:00', 'Completed'),
('A000000016', '550e8400-e29b-41d4-a716-446655440006', 'D000000003', '2025-10-18 09:00:00', '2025-10-18 09:30:00', 'Missed'),
('A000000017', '550e8400-e29b-41d4-a716-446655440007', 'D000000007', '2025-10-17 10:00:00', '2025-10-17 10:30:00', 'Missed'),
('A000000018', '550e8400-e29b-41d4-a716-446655440008', 'D000000008', '2025-10-17 11:00:00', '2025-10-17 11:30:00', 'Cancelled'),
('A000000019', '550e8400-e29b-41d4-a716-446655440009', 'D000000009', '2025-10-23 14:00:00', '2025-10-23 14:30:00', 'Upcoming'),
('A000000020', '550e8400-e29b-41d4-a716-446655440010', 'D000000010', '2025-10-23 15:00:00', '2025-10-23 15:30:00', 'Upcoming'),
('A000000021', '550e8400-e29b-41d4-a716-446655440011', 'D000000011', '2025-10-24 09:00:00', '2025-10-24 09:30:00', 'Upcoming'),
('A000000022', '550e8400-e29b-41d4-a716-446655440012', 'D000000012', '2025-10-24 10:00:00', '2025-10-24 10:30:00', 'Upcoming'),
('A000000023', '550e8400-e29b-41d4-a716-446655440013', 'D000000013', '2025-10-24 11:00:00', '2025-10-24 11:30:00', 'Upcoming'),
('A000000024', '550e8400-e29b-41d4-a716-446655440014', 'D000000014', '2025-10-24 14:00:00', '2025-10-24 14:30:00', 'Upcoming'),
('A000000025', '550e8400-e29b-41d4-a716-446655440015', 'D000000003', '2025-10-24 15:00:00', '2025-10-24 15:30:00', 'Upcoming'),
('A000000026', '550e8400-e29b-41d4-a716-446655440016', 'D000000016', '2025-10-25 09:00:00', '2025-10-25 09:30:00', 'Upcoming'),
('A000000027', '550e8400-e29b-41d4-a716-446655440017', 'D000000017', '2025-10-25 10:00:00', '2025-10-25 10:30:00', 'Upcoming'),
('A000000028', '550e8400-e29b-41d4-a716-446655440018', 'D000000018', '2025-10-25 11:00:00', '2025-10-25 11:30:00', 'Upcoming'),
('A000000029', '550e8400-e29b-41d4-a716-446655440019', 'D000000019', '2025-10-25 14:00:00', '2025-10-25 14:30:00', 'Upcoming'),
('A000000030', '550e8400-e29b-41d4-a716-446655440020', 'D000000003', '2025-10-25 17:00:00', '2025-10-25 17:30:00', 'Upcoming');

-- =====================================================
-- 5. VERIFICATION QUERIES
-- Description: Verify the data has been inserted correctly
-- =====================================================

-- Count records in each table
SELECT 'Clinic' as table_name, COUNT(*) as record_count FROM Clinic
UNION ALL
SELECT 'User_Profile' as table_name, COUNT(*) as record_count FROM User_Profile
UNION ALL
SELECT 'Doctor' as table_name, COUNT(*) as record_count FROM Doctor
UNION ALL
SELECT 'Appointment' as table_name, COUNT(*) as record_count FROM Appointment;

-- Show sample data from each table
SELECT '=== CLINIC SAMPLE ===' as info;
SELECT clinic_id, type, name, telephone_number FROM Clinic LIMIT 5;

SELECT '=== PATIENT SAMPLE ===' as info;
SELECT user_id, name, email, role FROM User_Profile LIMIT 5;

SELECT '=== DOCTOR SAMPLE ===' as info;
SELECT doctor_id, name, clinic_id FROM Doctor LIMIT 5;

SELECT '=== APPOINTMENT SAMPLE ===' as info;
SELECT appointment_id, patient_id, doctor_id, start_datetime, status FROM Appointment LIMIT 5;

-- =====================================================
-- END OF POPULATION SCRIPT
-- =====================================================
