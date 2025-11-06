package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.MedicalSummaryDto;

public interface MedicalSummaryService {
    MedicalSummaryDto getMedicalSummaryByAppointmentId(String appointmentId);
}
