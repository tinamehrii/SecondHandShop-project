package com.secondhand.backend.repository;

import com.secondhand.backend.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    boolean existsByRaterIdAndAdvertisementId(Long raterId, Long advertisementId);
}
