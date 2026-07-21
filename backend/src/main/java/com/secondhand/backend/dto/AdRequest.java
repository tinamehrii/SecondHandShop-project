package com.secondhand.backend.dto;

import java.util.List;

/**
 * Body used for creating or editing an advertisement.
 * Images are sent as Base64 strings so we do not need multipart upload
 * (this keeps the JavaFX client simple).
 */
public class AdRequest {
    public String title;
    public String description;
    public Long price;
    public String itemCondition;
    public Long categoryId;
    public Long cityId;
    public List<String> imagesBase64;
}
