package com.secondhand.backend.repository;

import com.secondhand.backend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    boolean existsByName(String name);
}
