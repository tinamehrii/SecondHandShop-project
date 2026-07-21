package com.secondhand.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An advertisement created by a user.
 * Every new advertisement starts in PENDING status and must be approved by an admin.
 */
@Entity
@Table(name = "advertisements")
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    /** Price in Toman. */
    @Column(nullable = false)
    private Long price;

    /** Condition of the item, for example: نو / در حد نو / کارکرده */
    private String itemCondition;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(nullable = false)
    private City city;

    @Enumerated(EnumType.STRING)
    private AdStatus status = AdStatus.PENDING;

    /** Reason written by admin when the advertisement is rejected. */
    private String rejectReason;

    private LocalDateTime createdAt = LocalDateTime.now();

    /** Images of the advertisement (bonus feature: more than one image). */
    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdImage> images = new ArrayList<>();

    public Advertisement() {
    }

    // ---------- getters and setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public AdStatus getStatus() {
        return status;
    }

    public void setStatus(AdStatus status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AdImage> getImages() {
        return images;
    }

    public void setImages(List<AdImage> images) {
        this.images = images;
    }
}
