package me.sonam.organization.handler;

import java.util.UUID;

public class OrganizationUserBody {
    public enum UpdateAction {
        add, update, delete
    }
    private UUID id;
    private UUID organizationId;

    private UUID userId;
    private UUID positionId;
    private UpdateAction updateAction;

    public OrganizationUserBody(UUID id, UUID organizationId, UUID userId, UpdateAction updateAction, UUID positionId) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.positionId = positionId;
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

    public UUID getPositionId() {
        return positionId;
    }

    public UpdateAction getUpdateAction() {
        return updateAction;
    }
}
