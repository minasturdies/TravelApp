# Travel App — Backend

Spring Boot backend: user auth, destination/activity lookup, and a Foursquare-backed
activity search with local caching in PostgreSQL.

## What's included

- **Auth**: register/login with BCrypt password hashing and JWT tokens.
- **Destinations**: find-or-create by city name.
- **Activities**: `GET /api/activities?city=X&interests=food,art&budget=100` — the core
  search endpoint. First search for a city pulls from Foursquare and caches results in
  Postgres; subsequent searches (within 24h) are served from the cache.
- **Saved trips**: authenticated endpoints to save/list trips.
- **Flyway migration** (`V1__init_schema.sql`) that creates the full schema and seeds
  a starter list of interests.

## Setup

1. **Start Postgres** (Docker is easiest):
   ```bash
   docker run --name travel-db -e POSTGRES_PASSWORD=devpass \
     -e POSTGRES_DB=travelapp -p 5432:5432 -d postgres:16
   ```

2. **Get a Foursquare API key** (free tier): https://location.foursquare.com/developer/
   Without this, the app still runs and serves cached/empty results — it just can't
   pull new activity data.

3. **Set environment variables.** Copy `.env.example` to `.env` and fill it in, or
   export the variables directly in your shell:
   ```bash
   export DB_USERNAME=postgres
   export DB_PASSWORD=devpass
   export JWT_SECRET=$(openssl rand -base64 48)
   export FOURSQUARE_API_KEY=your-key-here
   ```

4. **Run it:**
   ```bash
   ./mvnw spring-boot:run
   ```
   Flyway runs automatically on startup and creates the schema. Check the logs for
   `Successfully applied 1 migration` to confirm.

5. **Test it in Postman.** Import `../docs/travel-app.postman_collection.json`. Run
   Register → Login (this auto-saves your JWT into the collection variable) → then try
   the Activities search and Saved Trips endpoints.

## Try the core flow manually

```bash
# Search activities in Paris, filtered by interest and budget
curl "http://localhost:8080/api/activities?city=Paris&interests=food,art&budget=100"
```

The first call for a new city will be slower (live Foursquare call + writing to the DB).
Repeat the call and it'll be fast — served from cache.

## Notes on design decisions

- **Caching, not live-proxying**: activities are persisted with a 24h freshness window
  (see `ActivityService.CACHE_TTL_HOURS`). This avoids hammering Foursquare's rate
  limits and means your app still works if the external API is briefly down.
- **Interest tagging is a heuristic** right now — `ActivityService` tags fetched
  activities with whatever interests were requested in the search, rather than
  inferring interests from Foursquare's category data. That's a reasonable v1
  shortcut; a natural next step is a proper `category → interest` mapping table.
- **Flyway owns the schema** (`ddl-auto: validate`). If you change an entity, write a
  new migration (`V2__...sql`) rather than letting Hibernate auto-alter tables.

## What's next (not built yet)

- Geocoding for destinations (lat/long currently null unless you add it manually)
- Pagination on the activities search endpoint
- Rate limiting / retry logic around the Foursquare call
- Refresh tokens (current JWT is a single 24h token, no refresh flow)
