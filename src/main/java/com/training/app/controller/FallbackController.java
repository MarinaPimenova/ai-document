package com.training.app.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;

@Controller
public class FallbackController implements ErrorController {
    private final String redirectURI;

    public FallbackController(
            @Value("${springdoc.swagger-ui.path}") String redirectURI) {
        this.redirectURI = redirectURI;
    }

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) throws MalformedURLException {

        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        boolean swaggerExists = request.getServletContext()
                .getResource(redirectURI) != null;

        if (swaggerExists) {
            return "redirect:" + redirectURI;
        }

        if (statusCode != null && statusCode == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("404 Page Not Found");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error");
    }
}

