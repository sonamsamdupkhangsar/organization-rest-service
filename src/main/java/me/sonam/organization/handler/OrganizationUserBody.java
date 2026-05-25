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
    private String subdomain;
    private boolean restrictToSubdomain;

    public OrganizationUserBody() {
    }

    public OrganizationUserBody(UUID id, UUID organizationId, UUID userId, UUID positionId) {
        this(id, organizationId, userId, positionId, null, false);
    }

    public OrganizationUserBody(UUID id, UUID organizationId, UUID userId, UUID positionId,
                                String subdomain, boolean restrictToSubdomain) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.positionId = positionId;
        this.subdomain = subdomain;
        this.restrictToSubdomain = restrictToSubdomain;
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

    public String getSubdomain() {
        return subdomain;
    }

    public boolean isRestrictToSubdomain() {
        return restrictToSubdomain;
    }
}
