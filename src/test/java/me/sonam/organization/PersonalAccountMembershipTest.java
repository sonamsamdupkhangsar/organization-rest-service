package me.sonam.organization;

import me.sonam.organization.handler.PersonalAccountMembershipHandler;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.UserDefaultOrganizationRepository;
import me.sonam.organization.security.InternalServiceAuthorization;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.*;

class PersonalAccountMembershipTest {
    private final UUID userId = UUID.randomUUID();
    private final OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
    private final UserDefaultOrganizationRepository userDefaultOrganizationRepository = mock(UserDefaultOrganizationRepository.class);
    private final InternalServiceAuthorization internalServiceAuthorization = mock(InternalServiceAuthorization.class);
    private final PersonalAccountMembershipHandler personalAccountMembershipHandler = new PersonalAccountMembershipHandler(organizationUserRepository, userDefaultOrganizationRepository, internalServiceAuthorization);
    private final WebTestClient webTestClient = WebTestClient
            .bindToRouterFunction(new Router().accountDeletionRoutes(personalAccountMembershipHandler))
            .build();

    @Test
    void removesOnlyMembershipsAndPersonalDefault() {
        when(internalServiceAuthorization.requireInternalServiceIdentity())
                .thenReturn(Mono.empty());
        when(organizationUserRepository.deleteByUserId(userId))
                .thenReturn(Mono.just(2L));
        when(userDefaultOrganizationRepository.deleteById(userId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/organizations/internal/accounts/{userId}/memberships", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(organizationUserRepository).deleteByUserId(userId);
        verify(userDefaultOrganizationRepository).deleteById(userId);
        verifyNoMoreInteractions(organizationUserRepository, userDefaultOrganizationRepository);
    }

    @Test
    void failedDelegationDoesNotDeleteMemberships() {
        when(internalServiceAuthorization.requireInternalServiceIdentity())
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));

        webTestClient.delete()
                .uri("/organizations/internal/accounts/{userId}/memberships", userId)
                .exchange()
                .expectStatus().isForbidden();

        verifyNoInteractions(organizationUserRepository, userDefaultOrganizationRepository);
    }

    @Test
    void malformedUserIdReturnsBadRequestWithoutRepositoryAccess() {
        when(internalServiceAuthorization.requireInternalServiceIdentity())
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/organizations/internal/accounts/not-a-uuid/memberships")
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(organizationUserRepository, userDefaultOrganizationRepository);
    }

    @Test
    void membershipFailureStopsDefaultCleanup() {
        when(internalServiceAuthorization.requireInternalServiceIdentity())
                .thenReturn(Mono.empty());
        when(organizationUserRepository.deleteByUserId(userId))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)));

        webTestClient.delete()
                .uri("/organizations/internal/accounts/{userId}/memberships", userId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        verifyNoInteractions(userDefaultOrganizationRepository);
    }
}
