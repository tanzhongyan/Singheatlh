package Singheatlh.springboot_backend.service.impl;

import org.springframework.stereotype.Service;

import Singheatlh.springboot_backend.dto.MedicalSummaryDto;
import Singheatlh.springboot_backend.entity.MedicalSummary;
import Singheatlh.springboot_backend.mapper.MedicalSummaryMapper;
import Singheatlh.springboot_backend.repository.MedicalSummaryRepository;
import Singheatlh.springboot_backend.service.MedicalSummaryService;

@Service
public class MedicalSummaryServiceImpl implements MedicalSummaryService {
    
    private final MedicalSummaryRepository medicalSummaryRepository;
    
    public MedicalSummaryServiceImpl(MedicalSummaryRepository medicalSummaryRepository) {
        this.medicalSummaryRepository = medicalSummaryRepository;
    }
    
    @Override
    public MedicalSummaryDto getMedicalSummaryByAppointmentId(String appointmentId) {
        MedicalSummary medicalSummary = medicalSummaryRepository.findByAppointmentId(appointmentId)
            .orElse(null);
        return MedicalSummaryMapper.toDto(medicalSummary);
    }
}
