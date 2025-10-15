package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.request.CreatePatientRequest;
import Singheatlh.springboot_backend.dto.PatientDto;
import Singheatlh.springboot_backend.service.PatientService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private PatientService patientService;

    @GetMapping("{id}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable("id") String patientId) {
        PatientDto patientDTO = patientService.getById(patientId);
        return ResponseEntity.ok(patientDTO);
    }

    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        List<PatientDto> patients =  patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@RequestBody CreatePatientRequest createPatientRequest) {
        PatientDto patientDto = PatientDto.builder()
                .id(createPatientRequest.getId())
                .username(createPatientRequest.getUsername())
                .name(createPatientRequest.getName())
                .email(createPatientRequest.getEmail())
                .appointmentIds(null)
                .build();

        PatientDto newPatient = patientService.createPatient(patientDto);
        return new ResponseEntity<>(newPatient,HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable("id") String patientId, @RequestBody PatientDto patientDTO) {
        patientDTO.setId(patientId);
        PatientDto newPatient = patientService.updatePatient(patientDTO);
        return ResponseEntity.ok(newPatient);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deletePatient(@PathVariable("id") String  patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.ok("Patient Deleted successfully!");
    }



}
