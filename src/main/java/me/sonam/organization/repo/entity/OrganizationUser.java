package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

public class OrganizationUser implements Persistable<UUID> {
    public enum RoleNamesEnum {
        admin, user
    }
    @Id
    private UUID id;
    private UUID organizationId;
    private UUID userId;
    private UUID positionId;
    @Transient
    private boolean isNew;

    public OrganizationUser(UUID id, UUID organizationId, UUID userId, UUID positionId) {
        if (id != null) {
            this.id = id;
            this.isNew = false;
        }
        else {
            this.id = UUID.randomUUID();
            this.isNew = true;
        }
        this.organizationId = organizationId;
        this.userId = userId;
        this.positionId = positionId;
    }

    @Override
    public String toString() {
        return "OrganizationUser{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", userId=" + userId +
                ", isNew=" + isNew +
                ", positionId='" + positionId +"'" +
                '}';
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPositionId() {
        return this.positionId;
    }
    public UUID getOrganizationId() {
        return this.organizationId;
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
