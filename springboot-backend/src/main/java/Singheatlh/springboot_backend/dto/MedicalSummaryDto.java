package Singheatlh.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalSummaryDto {
    private String summaryId;
    private String appointmentId;
    private String treatmentSummary;
}
