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
    private final OrganizationUserRepository members = mock(OrganizationUserRepository.class);
    private final UserDefaultOrganizationRepository defaults = mock(UserDefaultOrganizationRepository.class);
    private final InternalServiceAuthorization authorization = mock(InternalServiceAuthorization.class);
    private final PersonalAccountMembershipHandler handler = new PersonalAccountMembershipHandler(members, defaults, authorization);
    private final WebTestClient client = WebTestClient
            .bindToRouterFunction(new Router().accountDeletionRoutes(handler))
            .build();

    @Test
    void removesOnlyMembershipsAndPersonalDefault() {
        when(authorization.requireService())
                .thenReturn(Mono.empty());
        when(members.deleteByUserId(userId))
                .thenReturn(Mono.just(2L));
        when(defaults.deleteById(userId))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/organizations/internal/accounts/{userId}/memberships", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(members).deleteByUserId(userId);
        verify(defaults).deleteById(userId);
        verifyNoMoreInteractions(members, defaults);
    }

    @Test
    void failedDelegationDoesNotDeleteMemberships() {
        when(authorization.requireService())
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));

        client.delete()
                .uri("/organizations/internal/accounts/{userId}/memberships", userId)
                .exchange()
                .expectStatus().isForbidden();

        verifyNoInteractions(members, defaults);
    }

    @Test
    void malformedUserIdReturnsBadRequestWithoutRepositoryAccess() {
        when(authorization.requireService())
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/organizations/internal/accounts/not-a-uuid/memberships")
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(members, defaults);
    }

    @Test
    void membershipFailureStopsDefaultCleanup() {
        when(authorization.requireService())
                .thenReturn(Mono.empty());
        when(members.deleteByUserId(userId))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)));

        client.delete()
                .uri("/organizations/internal/accounts/{userId}/memberships", userId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        verifyNoInteractions(defaults);
    }
}
