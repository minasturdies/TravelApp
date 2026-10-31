package com.travelapp.controller;

import com.travelapp.dto.SavedTripRequest;
import com.travelapp.model.SavedTrip;
import com.travelapp.service.SavedTripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class SavedTripController {

    private final SavedTripService savedTripService;

    @PostMapping
    public ResponseEntity<SavedTrip> save(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavedTripRequest request
    ) {
        return ResponseEntity.ok(savedTripService.save(userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<SavedTrip>> myTrips(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(savedTripService.getTripsForUser(userDetails.getUsername()));
    }
}
