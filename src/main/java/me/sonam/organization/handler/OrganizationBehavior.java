package me.sonam.organization.handler;

import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationBehavior {
    Mono<Page<Organization>> getOrganizationsByOwnerId(UUID ownerId, Pageable pageable);
    Mono<Organization> getOrganizationById(UUID organizationId);
    Mono<Organization> createOrganization(Mono<OrganizationBody> organizationBodyMono);
    Mono<Organization> updateOrganization(Mono<OrganizationBody> organizationBodyMono);
    Mono<String> deleteOrganization(UUID applicationId);
    //Mono<String> updateOrganizationUsers(Mono<OrganizationUserBody> organizationUserBodyFlux);
    Mono<String> removeUserFromOrganization(UUID userId, UUID organizationId);
    Mono<String> addUserToOrganization(OrganizationUserBody organizationUserBody);
    Mono<Page<UUID>> getOrganizationUsers(UUID organizationId, Pageable pageable);
    Mono<Boolean> userExistsInOrganization(UUID organizationId, UUID userId);
    Mono<String> deleteMyOrganization();
}
