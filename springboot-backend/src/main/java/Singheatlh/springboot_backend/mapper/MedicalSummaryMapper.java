package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.MedicalSummaryDto;
import Singheatlh.springboot_backend.entity.MedicalSummary;

public class MedicalSummaryMapper {
    
    public static MedicalSummaryDto toDto(MedicalSummary medicalSummary) {
        if (medicalSummary == null) {
            return null;
        }
        return new MedicalSummaryDto(
            medicalSummary.getSummaryId(),
            medicalSummary.getAppointmentId(),
            medicalSummary.getTreatmentSummary()
        );
    }
    
    public static MedicalSummary toEntity(MedicalSummaryDto dto) {
        if (dto == null) {
            return null;
        }
        return new MedicalSummary(
            dto.getSummaryId(),
            dto.getAppointmentId(),
            dto.getTreatmentSummary()
        );
    }
}
