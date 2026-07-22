package com.secondhand.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * One image file that belongs to an advertisement.
 * The real file is saved inside the "uploads" folder and
 * only the file name is stored in the database.
 */
@Entity
@Table(name = "ad_images")
public class AdImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(nullable = false)
    private Advertisement advertisement;

    public AdImage() {
    }

    public AdImage(String fileName, Advertisement advertisement) {
        this.fileName = fileName;
        this.advertisement = advertisement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonIgnore
    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }
}
