package me.sonam.organization.handler;

import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationBehavior {
    Mono<Page<Organization>> getOrganizations(Pageable pageable);
    Mono<Organization> getOrganizationById(UUID organizationId);
    Mono<String> createOrganization(Mono<OrganizationBody> organizationBodyMono);
    Mono<String> updateOrganization(Mono<OrganizationBody> organizationBodyMono);
    Mono<String> deleteOrganization(UUID applicationId);
    Mono<String> updateOrganizationUsers(Flux<OrganizationUserBody> organizationUserBodyFlux);
    Mono<Page<OrganizationUser>> getOrganizationUsers(UUID organizationId, Pageable pageable);
    Mono<Boolean> userExistsInOrganization(UUID organizationId, UUID userId);
}
