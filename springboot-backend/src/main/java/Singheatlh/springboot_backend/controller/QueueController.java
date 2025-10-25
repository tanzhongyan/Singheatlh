package Singheatlh.springboot_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/queue")
public class QueueController {
    
    
    @Autowired
    private QueueService queueService;

    @PostMapping("/check-in/{appointmentId}")
    public ResponseEntity<?> checkIn(@PathVariable String appointmentId) {
        try {
            QueueTicketDto queueTicket = queueService.checkIn(appointmentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(queueTicket);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Check-in Failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "BAD_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getQueueTicketById(@PathVariable Integer ticketId) {
        try {
            QueueTicketDto queueTicket = queueService.getQueueTicketById(ticketId);
            return ResponseEntity.ok(queueTicket);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Queue Ticket Not Found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<QueueTicketDto> getQueueTicketByAppointmentId(@PathVariable String appointmentId) {
        try {
            QueueTicketDto queueTicket = queueService.getQueueTicketByAppointmentId(appointmentId);
            return ResponseEntity.ok(queueTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    

    @GetMapping("/status/{ticketId}")
    public ResponseEntity<?> getQueueStatus(@PathVariable Integer ticketId) {
        try {
            QueueStatusDto status = queueService.getQueueStatus(ticketId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Queue Status Not Found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<QueueTicketDto>> getActiveQueueByDoctor(@PathVariable String doctorId) {
        try {
            List<QueueTicketDto> queue = queueService.getActiveQueueByDoctor(doctorId);
            return ResponseEntity.ok(queue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<QueueTicketDto>> getActiveQueueByClinic(@PathVariable Integer clinicId) {
        try {
            List<QueueTicketDto> queue = queueService.getActiveQueueByClinic(clinicId);
            return ResponseEntity.ok(queue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PostMapping("/call-next/{doctorId}")
    public ResponseEntity<?> callNextQueue(@PathVariable String doctorId) {
        try {
            QueueTicketDto nextTicket = queueService.callNextQueue(doctorId);
            
            if (nextTicket == null) {
                // q empty
                Map<String, String> response = new HashMap<>();
                response.put("message", "Current patient completed. No more patients in queue.");
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(nextTicket);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Call Next Failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "BAD_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid Request");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "BAD_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/status")
    public ResponseEntity<QueueTicketDto> updateQueueStatus(
            @PathVariable Integer ticketId,
            @RequestParam QueueStatus status) {
        try {
            QueueTicketDto updatedTicket = queueService.updateQueueStatus(ticketId, status);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/checked-in")
    public ResponseEntity<QueueTicketDto> markAsCheckedIn(@PathVariable Integer ticketId) {
        try {
            QueueTicketDto updatedTicket = queueService.markAsCheckedIn(ticketId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/no-show")
    public ResponseEntity<QueueTicketDto> markAsNoShow(@PathVariable Integer ticketId) {
        try {
            QueueTicketDto updatedTicket = queueService.markAsNoShow(ticketId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/completed")
    public ResponseEntity<QueueTicketDto> markAsCompleted(@PathVariable Integer ticketId) {
        try {
            QueueTicketDto updatedTicket = queueService.markAsCompleted(ticketId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @PutMapping("/ticket/{ticketId}/fast-track")
    public ResponseEntity<?> fastTrackPatient(
            @PathVariable Integer ticketId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Emergency/Priority");
            QueueTicketDto updatedTicket = queueService.fastTrackPatient(ticketId, reason);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Fast-Track Failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "BAD_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    

    @DeleteMapping("/ticket/{ticketId}")
    public ResponseEntity<Map<String, String>> cancelQueueTicket(@PathVariable Integer ticketId) {
        try {
            queueService.cancelQueueTicket(ticketId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Queue ticket removed successfully - marked as NO_SHOW");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<QueueTicketDto>> getQueueTicketsByPatientId(@PathVariable java.util.UUID patientId) {
        try {
            List<QueueTicketDto> tickets = queueService.getQueueTicketsByPatientId(patientId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/notify/{doctorId}")
    public ResponseEntity<Map<String, String>> processNotifications(@PathVariable String doctorId) {
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
    public ResponseEntity<Map<String, Integer>> getCurrentServingTicketId(@PathVariable String doctorId) {
        try {
            Integer currentTicketId = queueService.getCurrentServingTicketId(doctorId);
            Map<String, Integer> response = new HashMap<>();
            response.put("currentTicketId", currentTicketId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

    @GetMapping("/count/{doctorId}")
    public ResponseEntity<Map<String, Long>> getActiveQueueCount(@PathVariable String doctorId) {
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
