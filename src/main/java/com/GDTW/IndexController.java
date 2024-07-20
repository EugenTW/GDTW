package com.GDTW;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    @GetMapping("/about_us")
    public String aboutUs() {
        return "redirect:/about_us.html";
    }

    @GetMapping("/terms_of_use")
    public String turnOfUse() {
        return "redirect:/terms_of_use.html";
    }

}
