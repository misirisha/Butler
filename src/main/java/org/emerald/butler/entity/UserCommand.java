package org.emerald.butler.entity;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.UserCommandBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JmixEntity
@Entity(name = "UserCommand")
@Table(name = "User_command")
public class UserCommand extends StandardEntity {

    @OneToMany(mappedBy = "userCommand", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<UserCommandTrace> traces;

    @JoinColumn(name = "dweller_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Dweller dweller;

    @Column(name = "command")
    private String command;

    @Column(name = "progress")
    private String progress;

    public static UserCommandBuilder builder(Metadata metadata) {
        return new UserCommandBuilder(metadata);
    }
}
