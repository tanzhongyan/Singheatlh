package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.DoctorDto;
import Singheatlh.springboot_backend.entity.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorDto toDto(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        Integer clinicId = (doctor.getClinic() != null) ? doctor.getClinic().getClinicId() : doctor.getClinicId();
        String clinicName = (doctor.getClinic() != null) ? doctor.getClinic().getName() : null;

        return DoctorDto.builder()
                .doctorId(doctor.getDoctorId())
                .name(doctor.getName())
                .clinicId(clinicId)
                .clinicName(clinicName)
                .build();
    }

    public Doctor toEntity(DoctorDto doctorDto) {
        if (doctorDto == null) {
            return null;
        }

        Doctor doctor = new Doctor();
        doctor.setDoctorId(doctorDto.getDoctorId());
        doctor.setName(doctorDto.getName());
        doctor.setClinicId(doctorDto.getClinicId());
        // The clinic entity will be set in the service layer

        return doctor;
    }
}
