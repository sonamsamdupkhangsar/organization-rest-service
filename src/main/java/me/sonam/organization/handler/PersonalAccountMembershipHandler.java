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
    private final OrganizationUserRepository members;
    private final UserDefaultOrganizationRepository defaults;
    private final InternalServiceAuthorization authorization;

    public PersonalAccountMembershipHandler(OrganizationUserRepository members,
            UserDefaultOrganizationRepository defaults, InternalServiceAuthorization authorization) {
        this.members = members;
        this.defaults = defaults;
        this.authorization = authorization;
    }

    public Mono<ServerResponse> removeMemberships(ServerRequest request) {
        return authorization.requireService()
                .then(Mono.defer(() -> members.deleteByUserId(userId(request))))
                .then(Mono.defer(() -> defaults.deleteById(userId(request))))
                .then(ServerResponse.noContent().build());
    }

    private UUID userId(ServerRequest request) {
        try {
            return UUID.fromString(request.pathVariable("userId"));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID", exception);
        }
    }
}
