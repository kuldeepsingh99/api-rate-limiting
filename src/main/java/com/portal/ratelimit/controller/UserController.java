package com.portal.ratelimit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/v1/user")
    public String getUser() {
        return "Hello Secure User";
    }

    @GetMapping("/v2/user")
    public String getUserNotSecure() {
        return "Hello Not Secure User";
    }
}
