package com.gdtw.dailystatistic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DailyStatisticController {

    @GetMapping("/show_statistics")
    public String showStatistics() {
        return "forward:/show_statistics.html";
    }

}
