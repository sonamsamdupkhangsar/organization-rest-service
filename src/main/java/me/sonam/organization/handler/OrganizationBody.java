package me.sonam.organization.handler;


import java.util.UUID;

public class OrganizationBody {
    private UUID id;
    private String name;
    private UUID creatorUserId;
    private UUID positionId;
    private String subdomain;

    public OrganizationBody(UUID id, String name, UUID creatorUserId, UUID positionId, String subdomain) {
        this.id = id;
        this.name = name;
        this.creatorUserId = creatorUserId;
        this.positionId = positionId;
        this.subdomain = subdomain;
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
    public String getSubdomain() { return this.subdomain; }

}
