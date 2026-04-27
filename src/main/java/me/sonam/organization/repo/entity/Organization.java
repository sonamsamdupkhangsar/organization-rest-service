package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Organization implements Persistable<UUID> {
    @Id
    private UUID id;
    private String name;
    private UUID creatorUserId;
    private String subdomain;
    @Transient
    private boolean isNew;
    private LocalDateTime created;

    public Organization() {}
    public Organization(UUID id, String name, UUID creatorUserId, String subdomain) {
        if (id != null) {
            this.id = id;
            this.isNew = false;
        }
        else {
            this.isNew = true;
            this.id = UUID.randomUUID();
        }
        this.name = name;
        this.creatorUserId = creatorUserId;
        this.subdomain = subdomain;
        this.created = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public UUID getCreatorUserId() {
        return this.creatorUserId;
    }

    public String getSubdomain() {
        return subdomain;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isNew=" + isNew +
                ", creatorUserId=" + creatorUserId +
                ", subdomain='" + subdomain + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Organization that = (Organization) object;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(creatorUserId, that.creatorUserId) && Objects.equals(subdomain, that.subdomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creatorUserId, subdomain);
    }
}
