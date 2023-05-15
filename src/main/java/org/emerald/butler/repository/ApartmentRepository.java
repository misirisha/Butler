package org.emerald.butler.repository;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.Apartment;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApartmentRepository extends JmixDataRepository<Apartment, Long> {
    Optional<Apartment> findByHouseIdAndFrontDoorAndFloorAndNumber(UUID idHouse, Integer frontDoor, Integer floor, Integer number);

    Optional<Apartment> findByHouseIdAndNumber(UUID idHouse, Integer number);

    Optional<Apartment> findById(UUID id);
}
