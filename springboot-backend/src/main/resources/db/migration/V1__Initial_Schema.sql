-- =====================================================
-- Initial Database Schema for SingHealth Clinic System
-- Migration: V1__Initial_Schema.sql
-- Description: Creates all core tables for the appointment and queue management system
-- =====================================================

-- =====================================================
-- Table: Clinic
-- Description: Stores information about general and specialist clinics
-- =====================================================
CREATE TABLE Clinic (
    clinic_id SERIAL PRIMARY KEY,
    type VARCHAR(1) NOT NULL CHECK (type IN ('G', 'S')),
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    telephone_number VARCHAR(20),  -- Made nullable
    opening_hours TIME,
    closing_hours TIME
);

-- =====================================================
-- Table: User_Profile
-- Description: Stores user profile information linked to Supabase auth.users
-- Note: user_id type matches Supabase auth.users.id (UUID)
-- =====================================================
CREATE TABLE User_Profile (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255),
    role VARCHAR(1) NOT NULL CHECK (role IN ('P', 'C', 'S')),
    email VARCHAR(255) UNIQUE NOT NULL,
    telephone_number VARCHAR(20),
    clinic_id INT,  -- Made nullable (only Clinic Staff use this)
    CONSTRAINT fk_user_clinic FOREIGN KEY (clinic_id) 
        REFERENCES Clinic(clinic_id) 
        ON DELETE SET NULL
);

-- =====================================================
-- Table: Doctor
-- Description: Stores doctor information and their clinic assignment
-- =====================================================
CREATE TABLE Doctor (
    doctor_id CHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    clinic_id INT NOT NULL,
    appointment_duration_in_minutes INT NOT NULL,
    CONSTRAINT fk_doctor_clinic FOREIGN KEY (clinic_id) 
        REFERENCES Clinic(clinic_id) 
        ON DELETE CASCADE
);

-- =====================================================
-- Table: Schedule
-- Description: Tracks doctor availability and unavailability periods
-- =====================================================
CREATE TABLE Schedule (
    schedule_id CHAR(10) PRIMARY KEY,
    doctor_id CHAR(10) NOT NULL,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    type VARCHAR(11) NOT NULL CHECK (type IN ('AVAILABLE', 'UNAVAILABLE')),
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) 
        REFERENCES Doctor(doctor_id) 
        ON DELETE CASCADE,
    CONSTRAINT unique_doctor_schedule UNIQUE (doctor_id, start_datetime, end_datetime)
);

-- =====================================================
-- Table: Appointment
-- Description: Stores patient appointment bookings with doctors
-- =====================================================
CREATE TABLE Appointment (
    appointment_id CHAR(10) PRIMARY KEY,
    patient_id UUID NOT NULL,
    doctor_id CHAR(10) NOT NULL,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    status VARCHAR(20),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) 
        REFERENCES User_Profile(user_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) 
        REFERENCES Doctor(doctor_id) 
        ON DELETE CASCADE
);

-- =====================================================
-- Table: Queue_Ticket
-- Description: Manages queue tickets for checked-in appointments
-- Valid statuses: CHECKED_IN, CALLED, COMPLETED, NO_SHOW, FAST_TRACKED
-- Note: Notifications ("3 away", "next") are triggered programmatically without changing status
-- Note: CALLED status indicates patient is being seen by doctor
-- =====================================================
CREATE TABLE Queue_Ticket (
    ticket_id SERIAL PRIMARY KEY,
    appointment_id CHAR(10) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('CHECKED_IN', 'CALLED', 'COMPLETED', 'NO_SHOW', 'FAST_TRACKED')),
    check_in_time TIMESTAMP,
    queue_number INTEGER NOT NULL,
    is_fast_tracked BOOLEAN DEFAULT FALSE,
    fast_track_reason VARCHAR(255),
    CONSTRAINT fk_queue_appointment FOREIGN KEY (appointment_id)
        REFERENCES Appointment(appointment_id)
        ON DELETE CASCADE
);

-- =====================================================
-- Table: Medical_Summary
-- Description: Stores medical summaries/notes for completed appointments
-- =====================================================
CREATE TABLE Medical_Summary (
    summary_id CHAR(10) PRIMARY KEY,
    appointment_id CHAR(10) NOT NULL UNIQUE,
    treatment_summary TEXT,
    CONSTRAINT fk_summary_appointment FOREIGN KEY (appointment_id) 
        REFERENCES Appointment(appointment_id) 
        ON DELETE CASCADE
);

-- =====================================================
-- Indexes for Performance Optimization
-- =====================================================

-- Index on User_Profile email for faster login lookups
CREATE INDEX idx_user_profile_email ON User_Profile(email);

-- Index on User_Profile role for filtering by user type
CREATE INDEX idx_user_profile_role ON User_Profile(role);

-- Index on User_Profile clinic_id for clinic-based queries
CREATE INDEX idx_user_profile_clinic ON User_Profile(clinic_id);

-- Index on Doctor clinic_id for finding doctors by clinic
CREATE INDEX idx_doctor_clinic ON Doctor(clinic_id);

-- Index on Schedule doctor_id for schedule lookups
CREATE INDEX idx_schedule_doctor ON Schedule(doctor_id);

-- Index on Schedule datetime range for availability queries
CREATE INDEX idx_schedule_datetime ON Schedule(start_datetime, end_datetime);

-- Index on Appointment patient_id for patient appointment history
CREATE INDEX idx_appointment_patient ON Appointment(patient_id);

-- Index on Appointment doctor_id for doctor's appointment list
CREATE INDEX idx_appointment_doctor ON Appointment(doctor_id);

-- Index on Appointment datetime range for scheduling queries
CREATE INDEX idx_appointment_datetime ON Appointment(start_datetime, end_datetime);

-- Index on Appointment status for filtering active/cancelled appointments
CREATE INDEX idx_appointment_status ON Appointment(status);

-- Index on Queue_Ticket status for active queue management
CREATE INDEX idx_queue_status ON Queue_Ticket(status);

-- Index on Queue_Ticket check_in_time for FIFO ordering
CREATE INDEX idx_queue_checkin_time ON Queue_Ticket(check_in_time);

-- Index on Queue_Ticket queue_number for queue ordering
CREATE INDEX idx_queue_number ON Queue_Ticket(queue_number);

-- Index on Queue_Ticket appointment_id for joins
CREATE INDEX idx_queue_appointment_id ON Queue_Ticket(appointment_id);

-- =====================================================
-- Comments for Documentation
-- =====================================================

COMMENT ON TABLE Clinic IS 'Stores clinic information for general (G) and specialist (S) clinics';
COMMENT ON TABLE User_Profile IS 'User profiles linked to Supabase auth - Patient (P), Clinic Staff (C), System Admin (S)';
COMMENT ON TABLE Doctor IS 'Doctor information and clinic assignments';
COMMENT ON TABLE Schedule IS 'Doctor availability schedule with AVAILABLE/UNAVAILABLE time slots';
COMMENT ON TABLE Appointment IS 'Patient appointment bookings';
COMMENT ON TABLE Queue_Ticket IS 'Queue management for checked-in patients';
COMMENT ON TABLE Medical_Summary IS 'Medical notes and treatment summaries post-appointment';

COMMENT ON COLUMN User_Profile.user_id IS 'UUID matching Supabase auth.users.id';
COMMENT ON COLUMN User_Profile.role IS 'P=Patient, C=Clinic Staff, S=System Administrator';
COMMENT ON COLUMN Clinic.type IS 'G=General Practice, S=Specialist';
COMMENT ON COLUMN Schedule.type IS 'AVAILABLE=Open for appointments, UNAVAILABLE=Doctor not available';
