package org.emerald.butler.repository;

import org.emerald.butler.entity.Dweller;

public interface TransportRepository {
    void updateOrderByDwellerAndStartOrder(Dweller dweller, Long startOrder);
}
