package com.secondhand.backend.service;

import com.secondhand.backend.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Saves and loads advertisement images on the disk.
 * The client sends images as Base64 text, we decode them and
 * store them as normal files inside the "uploads" folder.
 */
@Service
public class ImageStorageService {

    private final Path uploadDir;

    public ImageStorageService(@Value("${app.upload.dir}") String uploadDirName) {
        this.uploadDir = Paths.get(uploadDirName);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload folder", e);
        }
    }

    /** Decodes the Base64 text and saves it as a jpg file. Returns the file name. */
    public String saveBase64Image(String base64, Long adId, int index) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new ApiException(400, "فرمت تصویر ارسال شده درست نیست");
        }
        if (bytes.length > 5 * 1024 * 1024) {
            throw new ApiException(400, "حجم هر تصویر باید کمتر از ۵ مگابایت باشد");
        }

        String fileName = "ad_" + adId + "_" + System.currentTimeMillis() + "_" + index + ".jpg";
        try {
            Files.write(uploadDir.resolve(fileName), bytes);
        } catch (IOException e) {
            throw new ApiException(500, "خطا در ذخیره تصویر");
        }
        return fileName;
    }

    /** Reads the bytes of a saved image. */
    public byte[] loadImage(String fileName) {
        // simple security check so nobody can read files outside the uploads folder
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ApiException(400, "نام فایل معتبر نیست");
        }
        Path path = uploadDir.resolve(fileName);
        if (!Files.exists(path)) {
            throw new ApiException(404, "تصویر پیدا نشد");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ApiException(500, "خطا در خواندن تصویر");
        }
    }
}
