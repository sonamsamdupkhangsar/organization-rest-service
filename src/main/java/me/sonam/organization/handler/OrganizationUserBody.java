package me.sonam.organization.handler;

import java.util.List;
import java.util.UUID;

public class OrganizationUserBody {
    private UUID id;
    private UUID organizationId;
    private List<UserUpdate> userUpdates;


    public OrganizationUserBody(UUID id, UUID organizationId, List<UserUpdate> userUpdates) {
        this.id = id;
        this.organizationId = organizationId;
        this.userUpdates = userUpdates;
    }

    public UUID getId() {
        return id;
    }

    public List<UserUpdate> getUserUpdates() {
        return List.copyOf(userUpdates);
    }

    public UUID getOrganizationId() {
        return this.organizationId;
    }
}
