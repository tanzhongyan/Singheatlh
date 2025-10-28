package Singheatlh.springboot_backend.validation;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates that the doctor exists before creating/updating a schedule
 */
@Component
@RequiredArgsConstructor
public class DoctorExistenceValidationRule implements ScheduleValidationRule {

    private final DoctorRepository doctorRepository;

    @Override
    public void validate(ScheduleDto scheduleDto) {
        if (scheduleDto.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID must not be null");
        }

        doctorRepository.findById(scheduleDto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundExecption(
                        "Doctor does not exist with id: " + scheduleDto.getDoctorId()));
    }
}
