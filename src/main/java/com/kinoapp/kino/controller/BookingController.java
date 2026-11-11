package com.kinoapp.kino.controller;

import com.kinoapp.kino.dto.BookingForm;
import com.kinoapp.kino.entity.Screening;
import com.kinoapp.kino.service.BookingService;
import com.kinoapp.kino.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final ScreeningService screeningService;

    public BookingController(BookingService bookingService, ScreeningService screeningService) {
        this.bookingService = bookingService;
        this.screeningService = screeningService;
    }

    @GetMapping("/screenings/{id}/book")
    public String bookForm(@PathVariable Long id, Model model) {
        BookingForm form = new BookingForm();
        form.setScreeningId(id);
        form.setSeats(1);
        model.addAttribute("bookingForm", form);
        return bookView(id, model);
    }

    @PostMapping("/screenings/{id}/book")
    public String book(@PathVariable Long id, @Valid @ModelAttribute("bookingForm") BookingForm form,
                       BindingResult result, Model model, RedirectAttributes redirect) {
        // The URL decides the screening, not the hidden form field
        form.setScreeningId(id);
        if (!result.hasErrors()) {
            try {
                bookingService.book(form);
                redirect.addFlashAttribute("message", "Booking confirmed");
                redirect.addAttribute("email", form.getCustomerEmail());
                return "redirect:/bookings";
            } catch (IllegalStateException e) {
                // e.g. not enough seats left: show it on the form
                result.reject("booking.failed", e.getMessage());
            }
        }
        return bookView(id, model);
    }

    // "My bookings": look up by email
    @GetMapping("/bookings")
    public String myBookings(@RequestParam(defaultValue = "") String email, Model model) {
        String trimmed = email.trim();
        model.addAttribute("email", trimmed);
        if (!trimmed.isEmpty()) {
            model.addAttribute("bookings", bookingService.findByEmail(trimmed));
        }
        return "bookings/list";
    }

    // The cancel button posts to /bookings/{id}?_method=delete with the email as proof
    @DeleteMapping("/bookings/{id}")
    public String cancel(@PathVariable Long id, @RequestParam String email, RedirectAttributes redirect) {
        bookingService.cancel(id, email);
        redirect.addFlashAttribute("message", "Booking cancelled");
        redirect.addAttribute("email", email);
        return "redirect:/bookings";
    }

    private String bookView(Long screeningId, Model model) {
        Screening screening = screeningService.get(screeningId);
        model.addAttribute("screening", screening);
        model.addAttribute("freeSeats", bookingService.freeSeats(screening));
        return "bookings/book";
    }
}
