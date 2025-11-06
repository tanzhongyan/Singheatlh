package Singheatlh.springboot_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medical_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalSummary {
    
    @Id
    @Column(name = "summary_id", length = 10)
    private String summaryId;
    
    @Column(name = "appointment_id", length = 10, nullable = false)
    private String appointmentId;
    
    @Column(name = "treatment_summary", columnDefinition = "TEXT")
    private String treatmentSummary;
}
