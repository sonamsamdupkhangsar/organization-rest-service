package me.sonam.organization.handler;

import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationBehavior {
    Mono<Page<Organization>> getOrganizations(Pageable pageable);
    Mono<String> createOrganization(Mono<OrganizationBody> organizationBodyMono);
    Mono<String> updateOrganization(Mono<OrganizationBody> organizationBodyMono);
    Mono<String> deleteOrganization(UUID applicationId);
    Mono<String> updateOrganizationUsers(Mono<OrganizationUserBody> userBodyMono);
    Mono<Page<OrganizationUser>> getOrganizationUsers(UUID organizationId, Pageable pageable);
}
