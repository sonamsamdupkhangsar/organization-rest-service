package me.sonam.organization.repo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("subdomain")
public class Subdomain implements Persistable<UUID> {
    @Id
    private UUID id;
    private String host;
    private LocalDateTime created;
    @Transient
    private boolean isNew;

    public Subdomain() {
    }

    public Subdomain(UUID id, String host) {
        if (id == null) {
            this.id = UUID.randomUUID();
            this.isNew = true;
        }
        else {
            this.id = id;
            this.isNew = false;
        }
        this.host = host;
        this.created = LocalDateTime.now();
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getHost() {
        return host;
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
        return "Subdomain{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", created=" + created +
                ", isNew=" + isNew +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Subdomain subdomain = (Subdomain) object;
        return Objects.equals(id, subdomain.id) && Objects.equals(host, subdomain.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host);
    }
}
