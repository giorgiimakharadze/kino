package com.kinoapp.kino.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingForm {

    @NotNull(message = "Screening is required")
    private Long screeningId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String customerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String customerEmail;

    @Min(value = 1, message = "Book at least 1 seat")
    @Max(value = 10, message = "You can book at most 10 seats at once")
    private int seats;
}
