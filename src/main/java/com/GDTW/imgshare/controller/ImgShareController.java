package com.GDTW.imgshare.controller;

import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.imgshare.model.ImgShareService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class ImgShareController {

    private final ImgShareService imgShareService;
    private final DailyStatisticService dailyStatisticService;

    public ImgShareController(ImgShareService imgShareService, DailyStatisticService dailyStatisticService) {
        this.imgShareService = imgShareService;
        this.dailyStatisticService = dailyStatisticService;
    }

    @GetMapping("/img_upload")
    public String imageUpload() {
        return "forward:/image_share.html";
    }

    @GetMapping("/image_view")
    public String imageAlbum() {
        return "forward:/image_view.html";
    }

    @GetMapping("/a/{code:[a-zA-Z0-9]{12}}")
    public String imageAlbumView(@PathVariable String code, HttpServletResponse response) throws IOException {
        if (!imgShareService.isShareImageAlbumCodeValid(code)) {
            response.sendRedirect("/error");
            return null;
        }
        dailyStatisticService.incrementImgAlbumUsed();
        return "forward:/image_view.html";
    }

    @GetMapping("/i/{code:[a-zA-Z0-9]{12}}")
    public String imageSingleView(@PathVariable String code, HttpServletResponse response) throws IOException {
        if (!imgShareService.isShareImageCodeValid(code)) {
            response.sendRedirect("/error");
            return null;
        }
        dailyStatisticService.incrementImgUsed();
        return "forward:/image_view.html";
    }

}