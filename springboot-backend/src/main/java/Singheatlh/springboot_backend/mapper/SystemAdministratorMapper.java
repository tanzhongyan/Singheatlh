package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.SystemAdministratorDto;
import Singheatlh.springboot_backend.entity.SystemAdministrator;
import org.springframework.stereotype.Component;

@Component
public class SystemAdministratorMapper {
    public SystemAdministratorDto toDto(SystemAdministrator systemAdministrator) {
        if  (systemAdministrator == null) {
            return null;
        }

        return SystemAdministratorDto.builder()
                .userId(systemAdministrator.getUserId())
                .name(systemAdministrator.getName())
                .email(systemAdministrator.getEmail())
                .role(systemAdministrator.getRole())
                .telephoneNumber(systemAdministrator.getTelephoneNumber())
                .build();
    }

    public SystemAdministrator toEntity(SystemAdministratorDto systemAdministratorDto) {
        if (systemAdministratorDto == null) {
            return null;
        }
        SystemAdministrator systemAdministrator = new SystemAdministrator();
        systemAdministrator.setUserId(systemAdministratorDto.getUserId());
        systemAdministrator.setName(systemAdministratorDto.getName());
        systemAdministrator.setEmail(systemAdministratorDto.getEmail());
        systemAdministrator.setRole(systemAdministratorDto.getRole());
        systemAdministrator.setTelephoneNumber(systemAdministratorDto.getTelephoneNumber());
        return systemAdministrator;
    }
}
