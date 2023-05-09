package org.emerald.butler.repository;

import java.util.Optional;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.House;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepository extends JmixDataRepository<House, Long> {

    Optional<House> findByRegionAndCityAndStreetAndNumber(String region, String city, String street, String number);
}
