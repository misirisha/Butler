package org.emerald.butler.entity;

import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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

    @OneToMany(mappedBy = "dweller")
    private Collection<DwellerApartmentRole> apartments;

    @OneToMany(mappedBy = "dweller")
    private Collection<DwellerChatRole> chats;

    @OneToMany(mappedBy = "dweller")
    private Collection<Transport> transports;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "telegram_Id")
    private Long telegramId;

    @Column(name = "username")
    private String userName;

    public static DwellerBuilder builder(Metadata metadata) {
        return new DwellerBuilder(metadata);
    }

    public Collection<Transport> getTransports() {
        return transports;
    }

    public void setTransports(Collection<Transport> transports) {
        this.transports = transports;
    }

    public Collection<DwellerChatRole> getChats() {
        return chats;
    }

    public void setChats(Collection<DwellerChatRole> chats) {
        this.chats = chats;
    }

    public Collection<DwellerApartmentRole> getApartments() {
        return apartments;
    }

    public void setApartments(Collection<DwellerApartmentRole> apartments) {
        this.apartments = apartments;
    }

    @DependsOnProperties({"firstName", "lastName"})
    @InstanceName
    public String getInstanceName() {
        return firstName + " " + lastName;
    }
}