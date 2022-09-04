package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

public class Organization implements Persistable<UUID> {
    @Id
    private UUID id;
    private String name;
    @Transient
    private boolean isNew;

    public Organization(UUID id, String name) {
        if (id != null) {
            this.id = id;
            this.isNew = false;
        }
        else {
            this.isNew = true;
            this.id = UUID.randomUUID();
        }
        this.name = name;

    }

    public String getName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isNew=" + isNew +
                '}';
    }
}
