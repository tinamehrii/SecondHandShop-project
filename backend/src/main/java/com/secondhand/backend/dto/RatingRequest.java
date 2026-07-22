package com.secondhand.backend.dto;

/**
 * Body for rating a seller of one advertisement.
 */
public class RatingRequest {
    public Long advertisementId;
    public Integer score;
    public String comment;
}
