package me.sonam.organization.handler;

import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.UserDefaultOrganizationRepository;
import me.sonam.organization.security.InternalServiceAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Deletes only personal memberships. Never deletes an organization or its owned records. */
@Service
public class PersonalAccountMembershipHandler {
    private final OrganizationUserRepository organizationUserRepository;
    private final UserDefaultOrganizationRepository userDefaultOrganizationRepository;
    private final InternalServiceAuthorization internalServiceAuthorization;

    public PersonalAccountMembershipHandler(OrganizationUserRepository organizationUserRepository,
            UserDefaultOrganizationRepository userDefaultOrganizationRepository, InternalServiceAuthorization internalServiceAuthorization) {
        this.organizationUserRepository = organizationUserRepository;
        this.userDefaultOrganizationRepository = userDefaultOrganizationRepository;
        this.internalServiceAuthorization = internalServiceAuthorization;
    }

    public Mono<ServerResponse> removeMemberships(ServerRequest request) {
        return internalServiceAuthorization.requireService()
                .then(Mono.defer(() -> organizationUserRepository.deleteByUserId(extractUserId(request))))
                .then(Mono.defer(() -> userDefaultOrganizationRepository.deleteById(extractUserId(request))))
                .then(ServerResponse.noContent().build());
    }

    private UUID extractUserId(ServerRequest request) {
        try {
            return UUID.fromString(request.pathVariable("userId"));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID", exception);
        }
    }
}
