package org.emerald.butler.repository;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.Apartment;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentRepository extends JmixDataRepository<Apartment, Long> {
}
