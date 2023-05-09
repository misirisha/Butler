package org.emerald.butler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.UserCommandTraceBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JmixEntity
@Entity(name = "UserCommandTrace")
@Table(name = "user_command_trace")
public class UserCommandTrace extends StandardEntity {

    @ManyToOne
    @JoinColumn(name = "user_command_id")
    private UserCommand userCommand;

    @Column(name = "order_")
    private Long order;

    @Column(name = "progress_stage")
    private String progressStage;

    public static UserCommandTraceBuilder builder(Metadata metadata) {
        return new UserCommandTraceBuilder(metadata);
    }

    @InstanceName
    @DependsOnProperties({"userCommand", "order", "progressStage"})
    public String getInstanceName() {
        return String.format("%s %s %s", userCommand, order, progressStage);
    }
}
