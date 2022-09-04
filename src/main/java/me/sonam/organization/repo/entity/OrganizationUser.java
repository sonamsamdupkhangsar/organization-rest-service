package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

public class OrganizationUser implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID organizationId;
    private UUID userId;
    @Transient
    private boolean isNew;

    @Override
    public String toString() {
        return "OrganizationUser{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", userId=" + userId +
                ", isNew=" + isNew +
                '}';
    }

    public OrganizationUser(UUID id, UUID organizationId, UUID userId) {
        if (this.id != null) {
            this.id = id;
            this.isNew = false;
        }
        else {
            this.id = UUID.randomUUID();
            this.isNew = true;
        }
        this.organizationId = organizationId;
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
