package com.secondhand.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A conversation between a buyer and the seller about one advertisement.
 * For each (advertisement, buyer) pair only one conversation exists.
 */
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Advertisement advertisement;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User seller;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Conversation() {
    }

    public Conversation(Advertisement advertisement, User buyer, User seller) {
        this.advertisement = advertisement;
        this.buyer = buyer;
        this.seller = seller;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
