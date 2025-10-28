package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.entity.Schedule;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {

    public ScheduleDto toDto(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        String doctorName = (schedule.getDoctor() != null) ? schedule.getDoctor().getName() : null;

        return ScheduleDto.builder()
                .scheduleId(schedule.getScheduleId())
                .doctorId(schedule.getDoctorId())
                .startDatetime(schedule.getStartDatetime())
                .endDatetime(schedule.getEndDatetime())
                .type(schedule.getType())
                .doctorName(doctorName)
                .build();
    }

    public Schedule toEntity(ScheduleDto scheduleDto) {
        if (scheduleDto == null) {
            return null;
        }

        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleDto.getScheduleId());
        schedule.setDoctorId(scheduleDto.getDoctorId());
        schedule.setStartDatetime(scheduleDto.getStartDatetime());
        schedule.setEndDatetime(scheduleDto.getEndDatetime());
        schedule.setType(scheduleDto.getType());
        // Doctor entity will be loaded by JPA automatically

        return schedule;
    }
}
