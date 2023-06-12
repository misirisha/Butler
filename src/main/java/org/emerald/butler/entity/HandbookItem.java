package org.emerald.butler.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@JmixEntity
@Table(name = "HANDBOOK_ITEM")
@Entity
public class HandbookItem extends StandardEntity {

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "CATEGORY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private HandbookCategory category;

    @Column(name = "VALUE_", length = 1023)
    private String value;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
}