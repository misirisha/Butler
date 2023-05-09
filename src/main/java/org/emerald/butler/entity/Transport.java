package org.emerald.butler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.TransportBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JmixEntity
@Entity(name = "Transport")
@Table(name = "Transport")
public class Transport extends StandardEntity {

    @Column(name = "order_")
    private Long order;

    @JoinColumn(name = "dweller_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Dweller dweller;

    @InstanceName
    @Column(name = "number")
    private String number;

    public static TransportBuilder builder(Metadata metadata) {
        return new TransportBuilder(metadata);
    }
}
