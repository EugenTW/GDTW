package com.gdtw;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "forward:/error_404.html";
            } else if (statusCode == HttpStatus.FORBIDDEN.value() || statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                return "forward:/error_403&405.html";
            }
        }
        return "forward:/error_generic.html";
    }

}
