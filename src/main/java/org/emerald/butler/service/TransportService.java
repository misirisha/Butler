package org.emerald.butler.service;

import java.util.Collection;

import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Transport;
import org.emerald.butler.repository.TransportJmixRepository;
import org.emerald.butler.repository.TransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TransportService {
    private final TransportJmixRepository transportJmixRepository;
    private final TransportRepository transportRepository;

    @Transactional
    public void delete(Transport transport) {
        transportJmixRepository.delete(transport);
        transportRepository.updateOrderByDwellerAndStartOrder(transport.getDweller(), transport.getOrder());
    }

    @Transactional
    public void delete(Collection<Transport> transports) {
        for (Transport transport : transports) {
            transportJmixRepository.delete(transport);
            transportRepository.updateOrderByDwellerAndStartOrder(transport.getDweller(), transport.getOrder());
        }
    }
}
