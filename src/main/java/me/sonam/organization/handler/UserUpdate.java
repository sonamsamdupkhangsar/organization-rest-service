package me.sonam.organization.handler;

import java.util.UUID;

public class UserUpdate {
    public enum UpdateAction {
        add, delete
    }
    private UUID userId;
    private UpdateAction update;

    public UserUpdate(UUID userId, String update) {
        this.update = UpdateAction.valueOf(update);
        this.userId = userId;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public UpdateAction getUpdate() {
        return this.update;
    }
}
