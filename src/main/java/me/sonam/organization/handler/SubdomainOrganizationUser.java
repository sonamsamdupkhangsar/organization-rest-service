package me.sonam.organization.handler;

import java.util.UUID;

public record SubdomainOrganizationUser(UUID userId, UUID organizationId, String organizationName) {
}
