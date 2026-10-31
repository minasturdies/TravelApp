package com.travelapp.repository;

import com.travelapp.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    Optional<Destination> findByCityNameIgnoreCase(String cityName);
}
