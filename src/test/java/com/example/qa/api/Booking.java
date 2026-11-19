package com.example.qa.api;

public record Booking(String firstname, String lastname, Integer totalprice,
                      Boolean depositpaid, BookingDates bookingdates, String additionalneeds) {
    public record BookingDates(String checkin, String checkout) {}
}
