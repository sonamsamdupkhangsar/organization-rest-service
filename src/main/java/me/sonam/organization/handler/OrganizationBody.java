package me.sonam.organization.handler;


import java.util.UUID;

public class OrganizationBody {
    private UUID id;
    private String name;
    private UUID creatorUserId;

    public OrganizationBody(UUID id, String name, UUID creatorUserId) {
        this.id = id;
        this.name = name;
        this.creatorUserId = creatorUserId;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public UUID getCreatorUserId() {
        return this.creatorUserId;
    }

}
