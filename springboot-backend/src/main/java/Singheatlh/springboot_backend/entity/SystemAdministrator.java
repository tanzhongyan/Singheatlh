package Singheatlh.springboot_backend.entity;

import Singheatlh.springboot_backend.entity.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("SYSTEM_ADMINISTRATOR")
public class SystemAdministrator extends User{

}
