package com.secondhand.backend.controller;

import com.secondhand.backend.service.ImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Serves the image files of advertisements.
 * GET /api/images/{fileName}
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable String fileName) {
        return imageStorageService.loadImage(fileName);
    }
}
