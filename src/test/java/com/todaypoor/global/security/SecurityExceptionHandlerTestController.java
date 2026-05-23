package com.todaypoor.global.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityExceptionHandlerTestController {

    @GetMapping("/security-test/authenticated")
    public String authenticatedOnly() {
        return "ok";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/security-test/admin")
    public String adminOnly() {
        return "ok";
    }
}
