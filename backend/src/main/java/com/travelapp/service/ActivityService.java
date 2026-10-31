package com.travelapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.travelapp.dto.ActivityResponse;
import com.travelapp.model.Activity;
import com.travelapp.model.Destination;
import com.travelapp.model.Interest;
import com.travelapp.repository.ActivityRepository;
import com.travelapp.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    // If cached activities for a destination are older than this, refresh from the external API.
    private static final long CACHE_TTL_HOURS = 24;

    private final ActivityRepository activityRepository;
    private final InterestRepository interestRepository;
    private final FoursquareClient foursquareClient;
    private final DestinationService destinationService;

    /**
     * Main entry point for the search endpoint.
     * city        - required, e.g. "Paris"
     * interestNames - optional list, e.g. ["food", "art"]
     * maxBudget   - optional, used to derive a Foursquare-style price tier (1-4)
     */
    @Transactional
    public List<ActivityResponse> searchActivities(String city, List<String> interestNames, BigDecimal maxBudget) {
        Destination destination = destinationService.findOrCreate(city);

        refreshIfStale(destination, interestNames);

        Integer maxPriceLevel = mapBudgetToPriceLevel(maxBudget);

        List<Activity> activities = activityRepository.search(destination.getId(), interestNames, maxPriceLevel);

        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Pulls fresh data from Foursquare if we don't have recent activities cached for this destination.
     * This keeps the external API call off the hot path once a city has been searched once.
     */
    private void refreshIfStale(Destination destination, List<String> interestNames) {
        List<Activity> existing = activityRepository.findByDestinationId(destination.getId());

        boolean isStale = existing.isEmpty() || existing.stream()
                .allMatch(a -> a.getFetchedAt().isBefore(LocalDateTime.now().minus(CACHE_TTL_HOURS, ChronoUnit.HOURS)));

        if (!isStale) {
            return;
        }

        List<JsonNode> results = foursquareClient.searchPlaces(destination.getCityName(), interestNames, 30);
        log.info("Fetched {} places from Foursquare for {}", results.size(), destination.getCityName());

        for (JsonNode place : results) {
            saveOrUpdateFromFoursquare(destination, place, interestNames);
        }
    }

    private void saveOrUpdateFromFoursquare(Destination destination, JsonNode place, List<String> interestNames) {
        String fsqId = textOrNull(place, "fsq_place_id");
        if (fsqId == null) return;

        Activity activity = activityRepository
                .findByExternalSourceAndExternalSourceId("foursquare", fsqId)
                .orElseGet(Activity::new);

        activity.setDestination(destination);
        activity.setName(textOrNull(place, "name"));
        activity.setExternalSource("foursquare");
        activity.setExternalSourceId(fsqId);
        activity.setFetchedAt(LocalDateTime.now());

        if (place.has("categories") && place.get("categories").size() > 0) {
            activity.setCategory(textOrNull(place.get("categories").get(0), "name"));
        }

        if (place.has("location")) {
            JsonNode location = place.get("location");
            activity.setAddress(textOrNull(location, "formatted_address"));
        }

        if (place.has("geocodes") && place.get("geocodes").has("main")) {
            JsonNode main = place.get("geocodes").get("main");
            if (main.has("latitude")) activity.setLatitude(main.get("latitude").asDouble());
            if (main.has("longitude")) activity.setLongitude(main.get("longitude").asDouble());
        }

        if (place.has("price")) {
            int priceLevel = place.get("price").asInt(2);
            activity.setPriceRange("$".repeat(Math.max(1, Math.min(priceLevel, 4))));
        }

        if (place.has("rating")) {
            // Foursquare rates 0-10; normalize to a 0-5 scale to match our schema's intent
            activity.setRating(BigDecimal.valueOf(place.get("rating").asDouble() / 2.0));
        }

        // Tag the activity with whichever requested interests match its category loosely.
        // This is a simple heuristic — a production version might use a proper category mapping table.
        if (interestNames != null && !interestNames.isEmpty()) {
            Set<Interest> matchedInterests = new HashSet<>(interestRepository.findByNameIgnoreCaseIn(interestNames));
            activity.setInterests(matchedInterests);
        }

        activityRepository.save(activity);
    }

    private Integer mapBudgetToPriceLevel(BigDecimal maxBudget) {
        if (maxBudget == null) return null;
        double v = maxBudget.doubleValue();
        if (v <= 20) return 1;
        if (v <= 50) return 2;
        if (v <= 100) return 3;
        return 4;
    }

    private String textOrNull(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : null;
    }

    private ActivityResponse toResponse(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .description(a.getDescription())
                .priceRange(a.getPriceRange())
                .category(a.getCategory())
                .rating(a.getRating())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .address(a.getAddress())
                .imageUrl(a.getImageUrl())
                .interests(a.getInterests().stream().map(Interest::getName).collect(Collectors.toList()))
                .build();
    }

    public Optional<ActivityResponse> getById(Long id) {
        return activityRepository.findById(id).map(this::toResponse);
    }
}
