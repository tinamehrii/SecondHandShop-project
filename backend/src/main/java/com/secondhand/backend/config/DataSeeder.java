package com.secondhand.backend.config;

import com.secondhand.backend.model.*;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Fills the database with the first admin user, a few cities and
 * default categories, only when the database is empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;

    public DataSeeder(UserRepository userRepository,
                      CityRepository cityRepository,
                      CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        // 1) default admin account
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("\u0645\u062f\u06cc\u0631 \u0633\u0627\u0645\u0627\u0646\u0647", "admin",
                    AuthService.hashPassword("admin123"), "09120000000", "admin@example.com");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        // 2) a few cities
        String[] cities = {"\u062a\u0647\u0631\u0627\u0646", "\u0645\u0634\u0647\u062f", "\u0627\u0635\u0641\u0647\u0627\u0646", "\u0634\u06cc\u0631\u0627\u0632", "\u062a\u0628\u0631\u06cc\u0632", "\u06a9\u0631\u062c"};
        for (String cityName : cities) {
            if (!cityRepository.existsByName(cityName)) {
                cityRepository.save(new City(cityName));
            }
        }

        // 3) default categories with sub-categories (bonus feature)
        if (categoryRepository.count() == 0) {
            Category electronics = categoryRepository.save(new Category("\u0648\u0633\u0627\u06cc\u0644 \u0627\u0644\u06a9\u062a\u0631\u0648\u0646\u06cc\u06a9\u06cc", null));
            categoryRepository.save(new Category("\u0645\u0648\u0628\u0627\u06cc\u0644", electronics));
            categoryRepository.save(new Category("\u0644\u067e\u200c\u062a\u0627\u067e", electronics));
            categoryRepository.save(new Category("\u06a9\u0646\u0633\u0648\u0644 \u0628\u0627\u0632\u06cc", electronics));

            Category vehicles = categoryRepository.save(new Category("\u0648\u0633\u0627\u06cc\u0644 \u0646\u0642\u0644\u06cc\u0647", null));
            categoryRepository.save(new Category("\u062e\u0648\u062f\u0631\u0648", vehicles));
            categoryRepository.save(new Category("\u0645\u0648\u062a\u0648\u0631\u0633\u06cc\u06a9\u0644\u062a", vehicles));

            Category home = categoryRepository.save(new Category("\u062e\u0627\u0646\u0647 \u0648 \u0622\u0634\u067e\u0632\u062e\u0627\u0646\u0647", null));
            categoryRepository.save(new Category("\u0645\u0628\u0644\u0645\u0627\u0646", home));
            categoryRepository.save(new Category("\u0644\u0648\u0627\u0632\u0645 \u0622\u0634\u067e\u0632\u062e\u0627\u0646\u0647", home));

            categoryRepository.save(new Category("\u067e\u0648\u0634\u0627\u06a9", null));
            categoryRepository.save(new Category("\u0648\u0631\u0632\u0634\u06cc", null));
            categoryRepository.save(new Category("\u06a9\u062a\u0627\u0628 \u0648 \u0644\u0648\u0627\u0632\u0645 \u062a\u062d\u0631\u06cc\u0631", null));
        }
    }
}
