package me.sonam.organization.handler.service;

import java.util.UUID;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/** Forwards the caller's identity; the role service independently requires organization admin access. */
@Component
public class MemberClientRoleCleanup {
    private static final Logger LOG = LoggerFactory.getLogger(MemberClientRoleCleanup.class);

    private final WebClient webClient;
    private final String roleServiceUrl;

    public MemberClientRoleCleanup(@Value("${ROLE_REST_SERVICE:http://role-rest-service}") String roleServiceUrl) {
        this.webClient = WebClient.builder().build();
        this.roleServiceUrl = roleServiceUrl.replaceAll("/+$", "");
    }

    public Mono<Void> removeMemberClientRoles(UUID organizationId, UUID userId) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(context -> context.getAuthentication())
                .flatMap(authentication -> {
                    if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                    }

                    return Mono.just(jwt.getTokenValue());
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMap(token -> webClient.delete()
                        .uri(roleServiceUrl + "/roles/organizations/{organizationId}/users/{userId}/client-role-assignments",
                                organizationId, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers -> headers.setBearerAuth(token))
                        .exchangeToMono(response -> {
                            if (response.statusCode().value() != 204) {
                                return response.releaseBody()
                                        .then(Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                                                "Unable to remove member client roles; organization membership was not removed")));
                            }

                            return response.releaseBody();
                        }))
                .timeout(Duration.ofSeconds(15))
                .doOnError(exception -> LOG.error(
                        "Client-role cleanup failed for user {} in organization {}; membership removal stopped",
                        userId, organizationId, exception))
                .onErrorMap(exception -> new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Unable to remove member client roles; organization membership was not removed", exception));
    }
}
