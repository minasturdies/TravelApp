package com.travelapp.controller;

import com.travelapp.dto.ActivityResponse;
import com.travelapp.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * GET /api/activities?city=Paris&interests=food,art&budget=100
     * This is the core search endpoint the frontend's results screen calls.
     */
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> search(
            @RequestParam String city,
            @RequestParam(required = false) List<String> interests,
            @RequestParam(required = false) BigDecimal budget
    ) {
        List<String> interestList = interests == null ? Collections.emptyList() : interests;
        return ResponseEntity.ok(activityService.searchActivities(city, interestList, budget));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getById(@PathVariable Long id) {
        return activityService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
