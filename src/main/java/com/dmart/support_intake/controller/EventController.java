package com.dmart.support_intake.controller;

import com.dmart.support_intake.dto.EventRequest;
import com.dmart.support_intake.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<?> receiveEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.processEvent(request));
    }
}