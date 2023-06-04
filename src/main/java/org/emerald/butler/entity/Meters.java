package org.emerald.butler.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

@Setter
@Getter
@JmixEntity
@Table(name = "METERS", indexes = {
        @Index(name = "IDX_METERS_DWELLER", columnList = "DWELLER_ID")
})
@Entity
public class Meters extends StandardEntity {

    @JoinColumn(name = "DWELLER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private DwellerApartmentRole dweller;

    @Column(name = "HOT_WATER")
    private Integer hotWater;

    @Column(name = "COLD_WATER")
    private Integer coldWater;

    @Column(name = "GAS")
    private Integer gas;

    @Column(name = "ELECTRICITY")
    private Integer electricity;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    public DwellerApartmentRole getDweller() {
        return dweller;
    }

    public void setDweller(DwellerApartmentRole dweller) {
        this.dweller = dweller;
    }
}