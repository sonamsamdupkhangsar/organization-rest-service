package me.sonam.organization.handler;


import java.util.UUID;

public class OrganizationBody {
    private UUID id;
    private String name;

    public OrganizationBody(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

}
