package com.kinoapp.kino.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

// Shows a friendly error page
@ControllerAdvice
public class GlobalExceptionHandler {

    // Business rule broken or bad input like a wrong file type
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ModelAndView handleRuleViolation(RuntimeException ex) {
        return errorPage(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Thrown by the services for missing movies, screenings and bookings
    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleStatus(ResponseStatusException ex) {
        return errorPage(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    // File bigger than max-file-size
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleTooBig(MaxUploadSizeExceededException ex) {
        return errorPage(HttpStatus.PAYLOAD_TOO_LARGE, "The file is too large (max 5 MB)");
    }

    private ModelAndView errorPage(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("message", message);
        return mav;
    }
}
