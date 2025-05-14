package com.gdtw.cssjsminify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CssJsMinifyController {

    @GetMapping("/cs_js_minify")
    public String cssJsMinify() {
        return "forward:/css_js_minify.html";
    }

}
