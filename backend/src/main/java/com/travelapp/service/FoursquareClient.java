package com.travelapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper around the Foursquare Places API (v3).
 * Docs: https://location.foursquare.com/developer/reference/place-search
 *
 * Requires FOURSQUARE_API_KEY to be set as an environment variable.
 * Sign up for a free key at https://location.foursquare.com/developer/
 */
@Component
@Slf4j
public class FoursquareClient {

    private final RestTemplate restTemplate;

    @Value("${app.foursquare.api-key}")
    private String apiKey;

    @Value("${app.foursquare.base-url}")
    private String baseUrl;

    public FoursquareClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Searches Foursquare for places near a city, optionally filtered by category keywords
     * (e.g. "food", "museum"). Returns raw JSON nodes for the service layer to map into entities.
     */
    public List<JsonNode> searchPlaces(String city, List<String> categoryKeywords, int limit) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("FOURSQUARE_API_KEY is not set — skipping external activity fetch");
            return List.of();
        }

        String query = categoryKeywords == null || categoryKeywords.isEmpty()
                ? "things to do"
                : String.join(" ", categoryKeywords);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("near", city)
                .queryParam("query", query)
                .queryParam("limit", Math.min(limit, 50))
                .queryParam("sort", "RATING");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("X-Places-Api-Version", "2025-06-17");
        headers.set("Accept", "application/json");

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            List<JsonNode> results = new ArrayList<>();
            if (body != null && body.has("results")) {
                body.get("results").forEach(results::add);
            }
            return results;

        } catch (Exception e) {
            log.error("Foursquare API call failed for city={}: {}", city, e.getMessage());
            return List.of();
        }
    }
}
