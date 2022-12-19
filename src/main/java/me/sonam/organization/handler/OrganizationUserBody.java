package me.sonam.organization.handler;

import java.util.List;
import java.util.UUID;

public class OrganizationUserBody {
    public enum UpdateAction {
        add, update, delete
    }
    private UUID id;
    private UUID organizationId;

    private UUID userId;
    private String userRole;
    private UpdateAction updateAction;

    public OrganizationUserBody(UUID id, UUID organizationId, UUID userId, UpdateAction updateAction, String userRole) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.userRole = userRole;
        this.updateAction = updateAction;
    }

    public UUID getId() {
        return id;
    }


    public UUID getOrganizationId() {
        return this.organizationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public UpdateAction getUpdateAction() {
        return updateAction;
    }
}
