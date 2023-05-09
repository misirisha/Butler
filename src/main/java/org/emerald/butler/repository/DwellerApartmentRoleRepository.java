package org.emerald.butler.repository;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.springframework.stereotype.Repository;

@Repository
public interface DwellerApartmentRoleRepository extends JmixDataRepository<DwellerApartmentRole, Long> {
}
