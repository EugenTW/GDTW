package com.gdtw.reportviolation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportViolationController {

    @GetMapping("/violation_report")
    public String violationReport() {
        return "forward:/report_violation.html";
    }

}
