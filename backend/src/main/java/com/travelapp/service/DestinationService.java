package com.travelapp.service;

import com.travelapp.model.Destination;
import com.travelapp.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;

    /**
     * Finds an existing destination by city name, or creates a bare-bones one.
     * Lat/long enrichment can be added later via a geocoding call if needed.
     */
    public Destination findOrCreate(String cityName) {
        return destinationRepository.findByCityNameIgnoreCase(cityName)
                .orElseGet(() -> {
                    Destination destination = new Destination();
                    destination.setCityName(cityName);
                    return destinationRepository.save(destination);
                });
    }
}
