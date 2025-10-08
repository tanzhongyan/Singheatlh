package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.controller.request.CreateUserRequest;
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
@RequestMapping("/api/patient")
public class PatientController {
    private PatientService patientService;

    @GetMapping("{id}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable("id") long patientId) {
        PatientDto patientDTO = patientService.getById(patientId);
        return ResponseEntity.ok(patientDTO);
    }

    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        List<PatientDto> patients =  patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@RequestBody CreateUserRequest createPatientRequest) {
        PatientDto patientDto = PatientDto.builder()
                .username(createPatientRequest.getUsername())
                .name(createPatientRequest.getName())
                .email(createPatientRequest.getEmail())
                .appointments(null)
                .build();

        PatientDto newPatient = patientService.createPatient(patientDto, createPatientRequest.getHashedPassword());
        return new ResponseEntity<>(newPatient,HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<PatientDto> updatePatient(@RequestBody PatientDto patientDTO) {
        PatientDto newPatient = patientService.updatePatient(patientDTO);
        return ResponseEntity.ok(newPatient);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deletePatient(@PathVariable("id") Long  patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.ok("Patient Deleted successfully!");
    }



}
