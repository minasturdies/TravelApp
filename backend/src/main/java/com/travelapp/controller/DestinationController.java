package com.travelapp.controller;

import com.travelapp.model.Destination;
import com.travelapp.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping
    public ResponseEntity<Destination> lookup(@RequestParam String city) {
        return ResponseEntity.ok(destinationService.findOrCreate(city));
    }
}
