package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.DoctorDto;
import Singheatlh.springboot_backend.entity.Doctor;

public class DoctorMapper {

    public static DoctorDto mapToDoctorDto(Doctor doctor) {
        return new DoctorDto(
                doctor.getDoctorId(),
                doctor.getName(),
                doctor.getSchedule(),
                doctor.getClinicId()
        );
    }

    public static Doctor mapToDoctor(DoctorDto doctorDto) {
        return new Doctor(
                doctorDto.getDoctorId(),
                doctorDto.getName(),
                doctorDto.getSchedule(),
                doctorDto.getClinicId()
        );
    }
}
