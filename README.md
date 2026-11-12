# Kino

A small cinema web app: browse movies, see screenings and book seats.
Built with Spring Boot 4, Spring Data JPA, Thymeleaf and an in-memory H2 database.

## Run it

Needs Java 17 or newer. From the project folder:

```
./mvnw spring-boot:run          # Windows PowerShell: .\mvnw.cmd spring-boot:run
```

Then open <http://localhost:8080>.

Run the tests with `./mvnw test`.

**Sample data.** On every start the app loads 22 real films (title, description, year, duration, poster)
from the free [Studio Ghibli API](https://ghibliapi.vercel.app), with two future screenings each.
If the API cannot be reached it falls back to 10 built-in sample movies. The database is in memory,
so everything you add is gone after a restart.

**H2 console:** <http://localhost:8080/h2-console> with JDBC URL `jdbc:h2:mem:kinodb`, user `sa`, empty password.

## What you can do

- Browse movies with search, genre filter, sorting (title, release date, duration) and pagination (8 per page).
- Add and edit a movie, including a poster image upload (jpg, png or webp, up to 5 MB).
- Add a screening to a movie (hall, start time, seats, price).
- Book seats for a screening. The seats left are shown, and a booking is rejected when there are not enough.
- Look up your bookings by email and cancel them (cancelling needs the same email).
- Delete a movie (blocked while it has bookings).

## Pages and endpoints

| Method | Path | What it does |
|---|---|---|
| GET | `/movies` | list; params `title`, `genre`, `page`, `sort`, `dir` |
| GET | `/movies/{id}` | movie page with upcoming screenings and seats left |
| GET, POST | `/movies/new`, `/movies` | add a movie (multipart form with poster) |
| GET, PUT | `/movies/{id}/edit`, `/movies/{id}` | edit a movie |
| DELETE | `/movies/{id}` | delete a movie |
| GET, POST | `/movies/{movieId}/screenings/new`, `/screenings` | add a screening |
| GET, POST | `/screenings/{id}/book` | booking form and booking |
| GET | `/bookings?email=` | bookings for an email |
| DELETE | `/bookings/{id}` | cancel a booking (param `email`) |

HTML forms only send GET and POST, so `PUT` and `DELETE` are sent as `POST ...?_method=put|delete`
and turned into the real method by Spring (`spring.mvc.hiddenmethod.filter.enabled=true`).

## How it is built

```
com.kinoapp.kino
  entity/      Movie, Genre, Screening, Booking
  repository/  Spring Data JPA repositories
  service/     business rules, @Transactional, file storage, Ghibli API client
  controller/  Thymeleaf controllers and the global error handler
  dto/         form objects with validation (MovieForm, ScreeningForm, BookingForm)
  config/      WebConfig (serves uploaded posters), DataSeeder
```

**Data model**

- `Movie` ↔ `Genre`: many-to-many (join table `movie_genres`).
- `Movie` → `Screening`: one-to-many (each screening has a many-to-one to its movie).
- `Screening` → `Booking`: one-to-many (each booking has a many-to-one to its screening).

**Queries**

- Method-name queries: `findByTitleContainingIgnoreCase`, `findByGenres_NameAndTitleContainingIgnoreCase`,
  `existsByScreening_Movie_Id`, `findByCustomerEmailIgnoreCase`, `existsByHallAndStartTime`.
- `@Query`: upcoming screenings of a movie, seats already booked for a screening, and a row-locking
  lookup of a screening used while booking.

**Rules worth knowing**

- Layers use constructor injection only (no `@Autowired` on fields).
- Services that change data are `@Transactional`.
- Booking locks the screening row (`PESSIMISTIC_WRITE`) so two people cannot take the last seat.
- Uploaded posters are saved under a random name in `uploads/` (setting `app.upload-dir`); the
  file name is stored in `Movie.posterPath`. Seeded movies store the poster's web address instead.
- Validation uses JSR-303 annotations on the form objects, with `@Valid` in the controllers.
- Sorting only accepts a fixed list of fields, so `?sort=anything` falls back to the title.

## Known limits

- There is no login or user accounts. Anyone can add, edit or delete movies, and "My bookings"
  trusts the email that is typed in.
- Two screenings in the same hall are only rejected when they start at exactly the same time;
  overlapping run times are not checked.
- Posters and the Archivo font load from the internet.
- The Ghibli API has no genres, so genres are assigned by title in `DataSeeder`.
