package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("user_default_organization")
public class UserDefaultOrganization implements Persistable<UUID> {
    @Id
    private UUID userId;
    private UUID organizationId;
    private LocalDateTime created;
    private LocalDateTime updated;
    @Transient
    private boolean isNew;

    public UserDefaultOrganization() {
    }

    public UserDefaultOrganization(UUID userId, UUID organizationId, boolean isNew) {
        this(userId, organizationId, LocalDateTime.now(), LocalDateTime.now(), isNew);
    }

    public UserDefaultOrganization(UUID userId, UUID organizationId, LocalDateTime created, LocalDateTime updated, boolean isNew) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.isNew = isNew;
        this.created = created;
        this.updated = updated;
    }

    @Override
    public UUID getId() {
        return userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
