package com.example.qa.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("api")
class BookingApiTest {
    private final BookingClient client = new BookingClient();
    private Integer bookingId;

    @Test
    @DisplayName("A newly created booking can be retrieved with all original data")
    void createdBookingCanBeRetrieved() {
        LocalDate checkIn = LocalDate.now(ZoneOffset.UTC).plusDays(7);
        Booking expected = new Booking("Automation", "Guest-" + UUID.randomUUID(), 237, true,
                new Booking.BookingDates(checkIn.toString(), checkIn.plusDays(3).toString()),
                "Breakfast");

        Response created = client.create(expected);
        created.then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON);
        bookingId = created.jsonPath().getInt("bookingid");
        created.then().log().ifValidationFails().body("bookingid", greaterThan(0));
        assertEquals(expected, created.jsonPath().getObject("booking", Booking.class),
                "POST must echo every submitted booking field");

        Response retrieved = client.get(bookingId);
        retrieved.then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON);
        assertEquals(expected, retrieved.as(Booking.class),
                "GET /booking/" + bookingId + " must return exactly the created booking");
    }

    @AfterEach
    void deleteCreatedBooking() {
        if (bookingId != null && bookingId > 0) {
            client.delete(bookingId).then().log().ifValidationFails().statusCode(201);
        }
    }
}
