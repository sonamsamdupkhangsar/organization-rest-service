package me.sonam.organization.handler;

import java.util.UUID;

public class UserUpdate {
    public enum UpdateAction {
        add, update, delete
    }
    private UUID userId;
    private UpdateAction update;
    private String userRole;

    public UserUpdate(UUID userId, String userRole, String update) {
        this.update = UpdateAction.valueOf(update);
        this.userRole = userRole;
        this.userId = userId;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public UpdateAction getUpdate() {
        return this.update;
    }

    public String getUserRole() {
        return this.userRole;
    }
}
