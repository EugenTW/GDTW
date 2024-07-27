package com.GDTW;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/about_us")
    public String aboutUs() {
        return "forward:/about_us.html";
    }

    @GetMapping("/terms_of_use")
    public String turnOfUse() {
        return "forward:/terms_of_use.html";
    }

}
