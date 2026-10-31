package com.travelapp.service;

import com.travelapp.dto.SavedTripRequest;
import com.travelapp.model.Destination;
import com.travelapp.model.SavedTrip;
import com.travelapp.model.User;
import com.travelapp.repository.SavedTripRepository;
import com.travelapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedTripService {

    private final SavedTripRepository savedTripRepository;
    private final UserRepository userRepository;
    private final DestinationService destinationService;

    public SavedTrip save(String userEmail, SavedTripRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Destination destination = destinationService.findOrCreate(request.getCityName());

        SavedTrip trip = new SavedTrip();
        trip.setUser(user);
        trip.setDestination(destination);
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setBudget(request.getBudget());

        return savedTripRepository.save(trip);
    }

    public List<SavedTrip> getTripsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return savedTripRepository.findByUserId(user.getId());
    }
}
