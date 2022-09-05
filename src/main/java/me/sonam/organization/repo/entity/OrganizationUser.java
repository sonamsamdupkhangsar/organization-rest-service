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
    private String userRole;
    @Transient
    private boolean isNew;

    @Override
    public String toString() {
        return "OrganizationUser{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", userId=" + userId +
                ", isNew=" + isNew +
                ", userRole='" + userRole +"'" +
                '}';
    }

    public OrganizationUser(UUID id, UUID organizationId, UUID userId, String userRole) {
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
        this.userRole = userRole;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserRole() {
        return this.userRole;
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
