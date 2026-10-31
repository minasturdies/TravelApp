package com.travelapp.repository;

import com.travelapp.model.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByNameIgnoreCase(String name);
    List<Interest> findByNameIgnoreCaseIn(List<String> names);
}
