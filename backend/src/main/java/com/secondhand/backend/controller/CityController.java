package com.secondhand.backend.controller;

import com.secondhand.backend.model.City;
import com.secondhand.backend.repository.CityRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public list of cities.
 */
@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityRepository cityRepository;

    public CityController(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @GetMapping
    public List<City> getAll() {
        return cityRepository.findAll();
    }
}
