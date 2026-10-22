package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.exception.DuplicateResourceException;
import com.example.warehouse_logistics.exception.DuplicateWarehouseCodeException;
import com.example.warehouse_logistics.exception.ResourceNotFoundException;
import com.example.warehouse_logistics.exception.WarehouseNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages = "com.example.warehouse_logistics.web")
public class WebExceptionHandler {

    @ExceptionHandler({WarehouseNotFoundException.class, ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(RuntimeException exception) {
        return errorView("error/not-found", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleTypeMismatch() {
        return errorView("error/not-found", "The requested record could not be found.");
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            DuplicateResourceException.class,
            DuplicateWarehouseCodeException.class,
            DataIntegrityViolationException.class
    })
    public String handleDomainError(RuntimeException exception, HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        String message = exception instanceof DataIntegrityViolationException
                ? "The operation conflicts with existing data or an inventory constraint"
                : exception.getMessage();
        redirectAttributes.addFlashAttribute("errorMessage", message);
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath().path("/").toUriString();
    }

    private ModelAndView errorView(String view, String message) {
        ModelAndView modelAndView = new ModelAndView(view);
        modelAndView.addObject("message", message);
        modelAndView.addObject("currentPage", "");
        return modelAndView;
    }
}
