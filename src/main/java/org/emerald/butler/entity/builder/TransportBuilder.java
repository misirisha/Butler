package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.Transport;

@RequiredArgsConstructor
public class TransportBuilder {
    private final Metadata metadata;
    private Long order;
    private Dweller dweller;
    private String number;

    public TransportBuilder order(Long order) {
        this.order = order;
        return this;
    }

    public TransportBuilder dweller(Dweller dweller) {
        this.dweller = dweller;
        return this;
    }

    public TransportBuilder number(String number) {
        this.number = number;
        return this;
    }

    public Transport build() {
        Transport transport = metadata.create(Transport.class);
        transport.setOrder(order);
        transport.setDweller(dweller);
        transport.setNumber(number);
        return transport;
    }
}
