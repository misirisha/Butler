package org.emerald.butler.repository;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DwellerApartmentRoleRepository extends JmixDataRepository<DwellerApartmentRole, Long> {
    Optional<DwellerApartmentRole> findByApartmentIdAndApartmentRole(UUID id, String apartmentRole);
}
