package com.secondhand.backend.repository;

import com.secondhand.backend.model.AdStatus;
import com.secondhand.backend.model.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByStatus(AdStatus status);

    List<Advertisement> findByOwnerIdAndStatusNot(Long ownerId, AdStatus status);

    long countByStatus(AdStatus status);

    boolean existsByCategoryId(Long categoryId);
}
