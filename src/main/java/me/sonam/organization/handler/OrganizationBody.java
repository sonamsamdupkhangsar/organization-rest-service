package me.sonam.organization.handler;


import java.util.UUID;

public class OrganizationBody {
    private UUID id;
    private String name;
    private UUID creatorUserId;
    private UUID positionId;

    public OrganizationBody(UUID id, String name, UUID creatorUserId, UUID positionId) {
        this.id = id;
        this.name = name;
        this.creatorUserId = creatorUserId;
        this.positionId = positionId;
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
    public UUID getPositionId() {return this.positionId;}

}
