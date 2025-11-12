package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Singheatlh.springboot_backend.repository.QueueTicketRepository;
import Singheatlh.springboot_backend.service.QueueNumberCalculator;

/**
 * Responsible ONLY for calculating queue numbers 
 */
@Service
public class QueueNumberCalculatorImpl implements QueueNumberCalculator {
    
    private static final int INITIAL_QUEUE_NUMBER = 1;
    private static final int QUEUE_INCREMENT = 1;
    
    @Autowired
    private QueueTicketRepository queueTicketRepository;
    
    @Override
    public Integer calculateNextQueueNumber(String doctorId, LocalDateTime now) {
        Integer maxQueueNumber = queueTicketRepository.findMaxQueueNumberByDoctorIdAndDate(doctorId, now);
        return (maxQueueNumber == null) ? INITIAL_QUEUE_NUMBER : maxQueueNumber + QUEUE_INCREMENT;
    }
    
    @Override
    public Integer calculateTicketNumberForDay(Integer clinicId, LocalDateTime now) {
        if (clinicId == null) {
            return INITIAL_QUEUE_NUMBER;
        }
        
        Integer maxTicketNumberForDay = queueTicketRepository.findMaxTicketNumberForDayByClinicAndDate(clinicId, now);
        return (maxTicketNumberForDay == null) ? INITIAL_QUEUE_NUMBER : maxTicketNumberForDay + QUEUE_INCREMENT;
    }
}

