package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.DoctorDto;
import Singheatlh.springboot_backend.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private DoctorService doctorService;

    @GetMapping("{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable("id") Long doctorId) {
        DoctorDto doctorDto = doctorService.getById(doctorId);
        return ResponseEntity.ok(doctorDto);
    }

    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        List<DoctorDto> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<DoctorDto>> getDoctorsByClinicId(@PathVariable("clinicId") Integer clinicId) {
        List<DoctorDto> doctors = doctorService.getDoctorsByClinicId(clinicId);
        return ResponseEntity.ok(doctors);
    }

    @PostMapping
    public ResponseEntity<DoctorDto> createDoctor(@RequestBody DoctorDto doctorDto) {
        DoctorDto newDoctor = doctorService.createDoctor(doctorDto);
        return new ResponseEntity<>(newDoctor, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<DoctorDto> updateDoctor(@RequestBody DoctorDto doctorDto) {
        DoctorDto updatedDoctor = doctorService.updateDoctor(doctorDto);
        return ResponseEntity.ok(updatedDoctor);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteDoctor(@PathVariable("id") Long doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.ok("Doctor deleted successfully!");
    }
}
