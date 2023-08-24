package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

public class OrganizationPosition implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID organizationId;
    private String name;

    @Transient
    private boolean isNew;

    public OrganizationPosition() {
    }

    public OrganizationPosition(UUID id, UUID organizationId, String name) {
        if (id == null) {
            this.isNew = true;
            this.id = UUID.randomUUID();
        }
        else {
            this.isNew = false;
            this.id = id;
        }
        this.organizationId = organizationId;
        this.name = name;
    }

    public String getName() {
        return  this.name;
    }
    public UUID getOrganizationId() { return  this.organizationId; }
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
        return "Position{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", organizationId'" + organizationId +'\''+
                ", isNew=" + isNew +
                '}';
    }
}
