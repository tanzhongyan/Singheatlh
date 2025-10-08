package Singheatlh.springboot_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Singheatlh.springboot_backend.dto.QueueStatusDto;
import Singheatlh.springboot_backend.dto.QueueTicketDto;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;
import Singheatlh.springboot_backend.service.QueueService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/queue")
public class QueueController {
    
    
    @Autowired
    private QueueService queueService;

    @PostMapping("/check-in/{appointmentId}")
    public ResponseEntity<QueueTicketDto> checkIn(@PathVariable Long appointmentId) {
        try {
            QueueTicketDto queueTicket = queueService.checkIn(appointmentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(queueTicket);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<QueueTicketDto> getQueueTicketById(@PathVariable Long ticketId) {
        try {
            QueueTicketDto queueTicket = queueService.getQueueTicketById(ticketId);
            return ResponseEntity.ok(queueTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<QueueTicketDto> getQueueTicketByAppointmentId(@PathVariable Long appointmentId) {
        try {
            QueueTicketDto queueTicket = queueService.getQueueTicketByAppointmentId(appointmentId);
            return ResponseEntity.ok(queueTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    

    @GetMapping("/status/{ticketId}")
    public ResponseEntity<QueueStatusDto> getQueueStatus(@PathVariable Long ticketId) {
        try {
            QueueStatusDto status = queueService.getQueueStatus(ticketId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<QueueTicketDto>> getActiveQueueByDoctor(@PathVariable Long doctorId) {
        try {
            List<QueueTicketDto> queue = queueService.getActiveQueueByDoctor(doctorId);
            return ResponseEntity.ok(queue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<QueueTicketDto>> getActiveQueueByClinic(@PathVariable Long clinicId) {
        try {
            List<QueueTicketDto> queue = queueService.getActiveQueueByClinic(clinicId);
            return ResponseEntity.ok(queue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PostMapping("/call-next/{doctorId}")
    public ResponseEntity<QueueTicketDto> callNextQueue(@PathVariable Long doctorId) {
        try {
            QueueTicketDto nextTicket = queueService.callNextQueue(doctorId);
            return ResponseEntity.ok(nextTicket);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/status")
    public ResponseEntity<QueueTicketDto> updateQueueStatus(
            @PathVariable Long ticketId,
            @RequestParam QueueStatus status) {
        try {
            QueueTicketDto updatedTicket = queueService.updateQueueStatus(ticketId, status);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/checked-in")
    public ResponseEntity<QueueTicketDto> markAsCheckedIn(@PathVariable Long ticketId) {
        try {
            QueueTicketDto updatedTicket = queueService.markAsCheckedIn(ticketId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/no-show")
    public ResponseEntity<QueueTicketDto> markAsNoShow(@PathVariable Long ticketId) {
        try {
            QueueTicketDto updatedTicket = queueService.markAsNoShow(ticketId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/completed")
    public ResponseEntity<QueueTicketDto> markAsCompleted(@PathVariable Long ticketId) {
        try {
            QueueTicketDto updatedTicket = queueService.markAsCompleted(ticketId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/fast-track")
    public ResponseEntity<QueueTicketDto> fastTrackPatient(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Emergency/Priority");
            QueueTicketDto updatedTicket = queueService.fastTrackPatient(ticketId, reason);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @DeleteMapping("/ticket/{ticketId}")
    public ResponseEntity<Map<String, String>> cancelQueueTicket(@PathVariable Long ticketId) {
        try {
            queueService.cancelQueueTicket(ticketId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Queue ticket cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<QueueTicketDto>> getQueueTicketsByPatientId(@PathVariable Long patientId) {
        try {
            List<QueueTicketDto> tickets = queueService.getQueueTicketsByPatientId(patientId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/notify/{doctorId}")
    public ResponseEntity<Map<String, String>> processNotifications(@PathVariable Long doctorId) {
        try {
            queueService.processQueueNotifications(doctorId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notifications processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/current/{doctorId}")
    public ResponseEntity<Map<String, Integer>> getCurrentServingNumber(@PathVariable Long doctorId) {
        try {
            Integer currentNumber = queueService.getCurrentServingNumber(doctorId);
            Map<String, Integer> response = new HashMap<>();
            response.put("currentQueueNumber", currentNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/count/{doctorId}")
    public ResponseEntity<Map<String, Long>> getActiveQueueCount(@PathVariable Long doctorId) {
        try {
            Long count = queueService.getActiveQueueCount(doctorId);
            Map<String, Long> response = new HashMap<>();
            response.put("activeCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
