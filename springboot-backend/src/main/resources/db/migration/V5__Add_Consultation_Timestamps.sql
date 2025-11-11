-- =====================================================
-- Migration: Add consultation timestamps to Queue_Ticket
-- Description: Track when consultation starts and completes for waiting time calculation
-- =====================================================

ALTER TABLE Queue_Ticket 
ADD COLUMN consultation_start_time TIMESTAMP,
ADD COLUMN consultation_complete_time TIMESTAMP;

-- Add index for performance when querying by dates
CREATE INDEX idx_queue_ticket_consultation_times ON Queue_Ticket(consultation_start_time, consultation_complete_time);

-- Add comments for documentation
COMMENT ON COLUMN Queue_Ticket.consultation_start_time IS 'Timestamp when patient enters consultation (status changes to CALLED)';
COMMENT ON COLUMN Queue_Ticket.consultation_complete_time IS 'Timestamp when consultation completes (status changes to COMPLETED)';

