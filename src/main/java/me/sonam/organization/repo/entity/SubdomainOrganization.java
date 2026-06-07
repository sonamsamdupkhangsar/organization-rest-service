package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("subdomain_organization")
public class SubdomainOrganization implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID subdomainId;
    private UUID organizationId;
    private LocalDateTime created;
    @Transient
    private boolean isNew;

    public SubdomainOrganization() {
    }

    public SubdomainOrganization(UUID id, UUID subdomainId, UUID organizationId) {
        if (id == null) {
            this.id = UUID.randomUUID();
            this.isNew = true;
        }
        else {
            this.id = id;
            this.isNew = false;
        }
        this.subdomainId = subdomainId;
        this.organizationId = organizationId;
        this.created = LocalDateTime.now();
    }

    @Override
    public UUID getId() {
        return id;
    }

    public UUID getSubdomainId() {
        return subdomainId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        return "SubdomainOrganization{" +
                "id=" + id +
                ", subdomainId=" + subdomainId +
                ", organizationId=" + organizationId +
                ", created=" + created +
                ", isNew=" + isNew +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SubdomainOrganization that = (SubdomainOrganization) object;
        return Objects.equals(id, that.id) && Objects.equals(subdomainId, that.subdomainId) && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subdomainId, organizationId);
    }
}
