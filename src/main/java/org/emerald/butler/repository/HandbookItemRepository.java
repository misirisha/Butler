package org.emerald.butler.repository;

import java.util.UUID;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.HandbookItem;

public interface HandbookItemRepository extends JmixDataRepository<HandbookItem, UUID> {
}
