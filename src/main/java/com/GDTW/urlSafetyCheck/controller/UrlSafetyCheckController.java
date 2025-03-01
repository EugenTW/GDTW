package com.GDTW.urlSafetyCheck.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UrlSafetyCheckController {

    @GetMapping("/url_safety_check")
    public String urlSafetyCheck() {
        return "forward:/url_safety_check.html";
    }

}
