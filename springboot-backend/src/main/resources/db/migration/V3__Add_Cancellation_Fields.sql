-- =====================================================
-- Migration: V3__Add_Cancellation_Fields
-- Description: Add cancellation tracking fields to appointment table
--              for staff cancellation feature (tracks who cancelled, when, and why)
-- =====================================================

-- Add cancellation tracking fields to appointment table
ALTER TABLE appointment
ADD COLUMN cancellation_reason TEXT,
ADD COLUMN cancelled_by UUID,
ADD COLUMN cancelled_at TIMESTAMP;

-- Add comments for documentation
COMMENT ON COLUMN appointment.cancellation_reason IS 'Reason for cancellation (mandatory for staff cancellations)';
COMMENT ON COLUMN appointment.cancelled_by IS 'User ID (staff or patient) who cancelled the appointment';
COMMENT ON COLUMN appointment.cancelled_at IS 'Timestamp when the appointment was cancelled';
