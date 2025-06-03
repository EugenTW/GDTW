package com.gdtw.imgshare.controller;

import com.gdtw.general.util.ImgServiceValidatorUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.Optional;

@Controller
public class ImgShareController {

    @GetMapping("/img_upload")
    public String imageUpload() {
        return "forward:/image_share.html";
    }

    @GetMapping("/img_view")
    public String imageAlbum() {
        return "forward:/image_view.html";
    }

    @GetMapping({"/a/{code:[a-zA-Z0-9]{6}}", "/i/{code:[a-zA-Z0-9]{6}}"})
    public String imageView(@PathVariable String code, HttpServletResponse response) throws IOException {
        Optional<String> codeError = ImgServiceValidatorUtil.validateShareCode(code);
        if (codeError.isPresent()) {
            response.sendRedirect("/error_404");
            return null;
        }
        return "forward:/image_view.html";
    }

}
