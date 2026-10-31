package com.travelapp.repository;

import com.travelapp.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByExternalSourceAndExternalSourceId(String externalSource, String externalSourceId);

    // Filters by destination, optionally by interest names and a max price tier.
    // priceRange is stored as '$'..'$$$$'; we filter by length <= maxPriceLevel as a simple budget proxy.
    @Query("""
           SELECT DISTINCT a FROM Activity a
           LEFT JOIN a.interests i
           WHERE a.destination.id = :destinationId
           AND (:interestNames IS NULL OR i.name IN :interestNames)
           AND (:maxPriceLevel IS NULL OR a.priceRange IS NULL OR LENGTH(a.priceRange) <= :maxPriceLevel)
           ORDER BY a.rating DESC NULLS LAST
           """)
    List<Activity> search(
            @Param("destinationId") Long destinationId,
            @Param("interestNames") List<String> interestNames,
            @Param("maxPriceLevel") Integer maxPriceLevel
    );

    List<Activity> findByDestinationId(Long destinationId);
}
