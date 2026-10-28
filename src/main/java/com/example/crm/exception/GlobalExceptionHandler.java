package com.example.crm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Global exception handler providing user-friendly error views
 * while safeguarding system details, stack traces, and database internals.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles CustomerNotFoundException by redirecting to the customer list with an error banner.
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public String handleCustomerNotFound(CustomerNotFoundException ex, RedirectAttributes redirectAttributes) {
        log.warn("Customer not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/customers";
    }

    /**
     * Handles DuplicateEmailException by redirecting to the customer list with an error banner.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public String handleDuplicateEmail(DuplicateEmailException ex, RedirectAttributes redirectAttributes) {
        log.warn("Duplicate email conflict: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/customers";
    }

    /**
     * Handles unexpected generic application errors with a friendly error page.
     * Prevents leakage of stack traces, SQL syntax, or internal system configurations.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unhandled application exception occurred", ex);
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "An unexpected error occurred while processing your request. Please try again later.");
        model.addAttribute("timestamp", LocalDateTime.now());
        return "error";
    }
}
