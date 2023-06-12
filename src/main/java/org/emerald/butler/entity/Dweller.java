package org.emerald.butler.entity;

import java.time.LocalDate;
import java.util.Collection;
import javax.persistence.*;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.DwellerBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JmixEntity
@Entity(name = "Dweller")
@Table(name = "Dweller")
public class Dweller extends StandardEntity {

    @OneToMany(mappedBy = "dweller", cascade = CascadeType.REMOVE)
    private Collection<DwellerApartmentRole> apartments;

    @OneToMany(mappedBy = "dweller", cascade = CascadeType.REMOVE)
    private Collection<DwellerChatRole> chats;

    @OneToMany(mappedBy = "dweller", cascade = CascadeType.REMOVE)
    private Collection<Transport> transports;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "telegram_Id")
    private Long telegramId;

    @Column(name = "username")
    private String userName;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    public static DwellerBuilder builder(Metadata metadata) {
        return new DwellerBuilder(metadata);
    }

    @DependsOnProperties({"firstName", "lastName"})
    @InstanceName
    public String getInstanceName() {
        return firstName + " " + lastName;
    }
}