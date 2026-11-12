package me.sonam.organization;

import me.sonam.organization.handler.AdminOrganizationDeletionHandler;
import me.sonam.organization.repo.*;
import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import me.sonam.organization.repo.entity.UserDefaultOrganization;
import me.sonam.organization.repo.entity.OrganizationPosition;
import me.sonam.organization.security.InternalServiceAuthorization;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.*;

class AdminOrganizationDeletionTest {
    private final UUID userId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
    private final OrganizationPositionRepository organizationPositionRepository = mock(OrganizationPositionRepository.class);
    private final SubdomainOrganizationRepository subdomainOrganizationRepository = mock(SubdomainOrganizationRepository.class);
    private final UserDefaultOrganizationRepository userDefaultOrganizationRepository = mock(UserDefaultOrganizationRepository.class);
    private final InternalServiceAuthorization internalServiceAuthorization = mock(InternalServiceAuthorization.class);

    @Test
    void unauthorizedRequestsCannotReadOrDelete() {
        when(internalServiceAuthorization.requireInternalServiceIdentity())
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));
        WebTestClient webTestClient = client(true, "");

        webTestClient.get()
                .uri(path())
                .exchange()
                .expectStatus()
                .isForbidden();
        webTestClient.delete()
                .uri(path())
                .exchange()
                .expectStatus()
                .isForbidden();

        verifyNoInteractions(organizationRepository, organizationUserRepository, organizationPositionRepository,
                subdomainOrganizationRepository, userDefaultOrganizationRepository);
    }

    @Test
    void soleCreatorCleanupIncludesPositionsAndPreservesSubdomain() {
        stubOrganization(userId, userId);
        when(subdomainOrganizationRepository.deleteByOrganizationId(organizationId))
                .thenReturn(Mono.just(1));
        when(userDefaultOrganizationRepository.deleteByOrganizationId(organizationId))
                .thenReturn(Mono.just(1));
        when(organizationPositionRepository.deleteByOrganizationId(organizationId))
                .thenReturn(Mono.empty());
        when(organizationUserRepository.deleteByOrganizationId(organizationId))
                .thenReturn(Mono.just(1));
        when(organizationRepository.deleteById(organizationId))
                .thenReturn(Mono.empty());

        client(true, "")
                .delete()
                .uri(path())
                .exchange()
                .expectStatus()
                .isNoContent();

        var inOrder = inOrder(subdomainOrganizationRepository, userDefaultOrganizationRepository,
                organizationPositionRepository, organizationUserRepository, organizationRepository);
        inOrder.verify(subdomainOrganizationRepository).deleteByOrganizationId(organizationId);
        inOrder.verify(userDefaultOrganizationRepository).deleteByOrganizationId(organizationId);
        inOrder.verify(organizationUserRepository).deleteByOrganizationId(organizationId);
        inOrder.verify(organizationPositionRepository).deleteByOrganizationId(organizationId);
        inOrder.verify(organizationRepository).deleteById(organizationId);
    }

    @Test
    void sharedOrganizationCannotBeDeleted() {
        stubOrganization(userId, userId, UUID.randomUUID());

        client(true, "")
                .delete()
                .uri(path())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);

        noDeletion();
    }

    @Test
    void anotherCreatorCannotBeDeletedEvenBySoleMember() {
        stubOrganization(UUID.randomUUID(), userId);

        client(true, "")
                .delete()
                .uri(path())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);

        noDeletion();
    }

    @Test
    void protectedOrUnconfiguredPolicyBlocksCleanup() {
        stubOrganization(userId, userId);

        client(false, "UNCONFIGURED")
                .delete()
                .uri(path())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
        client(true, organizationId.toString())
                .delete()
                .uri(path())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);

        noDeletion();
    }

    @Test
    void inventoryIsReadOnlyAndIncludesCreatorAndMembership() {
        stubOrganization(userId, userId);

        client(true, "")
                .get()
                .uri(path())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.creatorUserId")
                .isEqualTo(userId.toString())
                .jsonPath("$.memberUserIds[0]")
                .isEqualTo(userId.toString())
                .jsonPath("$.relationshipChecksPassed")
                .isEqualTo(true)
                .jsonPath("$.complete")
                .doesNotExist()
                .jsonPath("$.protectionPolicyVerified")
                .isEqualTo(true);

        noDeletion();
    }

    @Test
    void sharedMembersDefaultSelectionsAreValidInventoryButStillBlockDeletion() {
        UUID secondUserId = UUID.randomUUID();
        stubOrganization(userId, userId, secondUserId);
        when(userDefaultOrganizationRepository.findByOrganizationId(organizationId))
                .thenReturn(Flux.just(
                        new UserDefaultOrganization(userId, organizationId, false),
                        new UserDefaultOrganization(secondUserId, organizationId, false)));

        client(true, "")
                .get()
                .uri(path())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.relationshipChecksPassed").isEqualTo(true)
                .jsonPath("$.memberUserIds.length()").isEqualTo(2);

        client(true, "")
                .delete()
                .uri(path())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        noDeletion();
    }

    @Test
    void anotherUsersStaleDefaultSelectionIsNotSilentlyDeleted() {
        stubOrganization(userId, userId);
        when(userDefaultOrganizationRepository.findByOrganizationId(organizationId))
                .thenReturn(Flux.just(new UserDefaultOrganization(UUID.randomUUID(), organizationId, false)));

        client(true, "")
                .get()
                .uri(path())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.relationshipChecksPassed").isEqualTo(false);

        client(true, "").delete().uri(path()).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);

        noDeletion();
    }

    @Test
    void positionReferencedByAnotherOrganizationBlocksCleanup() {
        stubOrganization(userId, userId);
        UUID positionId = UUID.randomUUID();
        when(organizationPositionRepository.findByOrganizationId(organizationId))
                .thenReturn(Flux.just(new OrganizationPosition(positionId, organizationId, "Shared position")));
        when(organizationUserRepository.findByPositionId(positionId))
                .thenReturn(Flux.just(new OrganizationUser(null, UUID.randomUUID(), userId, positionId)));

        client(true, "").delete().uri(path()).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);

        noDeletion();
    }

    private WebTestClient client(boolean configured, String protectedIds) {
        AdminOrganizationDeletionHandler adminOrganizationDeletionHandler = new AdminOrganizationDeletionHandler(
                organizationRepository, organizationUserRepository, organizationPositionRepository,
                subdomainOrganizationRepository, userDefaultOrganizationRepository, internalServiceAuthorization,
                configured, protectedIds);

        return WebTestClient.bindToRouterFunction(new Router().adminAccountDeletionRoutes(adminOrganizationDeletionHandler))
                .build();
    }

    private void stubOrganization(UUID creatorId, UUID... memberIds) {
        when(internalServiceAuthorization.requireInternalServiceIdentity())
                .thenReturn(Mono.empty());
        when(organizationRepository.findById(organizationId))
                .thenReturn(Mono.just(new Organization(organizationId, "Disposable organization", creatorId)));
        when(organizationUserRepository.findByOrganizationId(organizationId))
                .thenReturn(Flux.fromArray(memberIds)
                .map(memberId -> new OrganizationUser(null, organizationId, memberId, null)));
        when(userDefaultOrganizationRepository.findByOrganizationId(organizationId)).thenReturn(Flux.empty());
        when(organizationPositionRepository.findByOrganizationId(organizationId)).thenReturn(Flux.empty());
    }

    private String path() {
        return "/organizations/internal/admin-deletion/users/" + userId + "/organizations/" + organizationId;
    }

    private void noDeletion() {
        verify(organizationRepository, never()).deleteById(any(UUID.class));
        verify(organizationUserRepository, never()).deleteByOrganizationId(any());
        verify(organizationPositionRepository, never()).deleteByOrganizationId(any());
        verify(userDefaultOrganizationRepository, never()).deleteByOrganizationId(any());
        verifyNoInteractions(subdomainOrganizationRepository);
    }
}
