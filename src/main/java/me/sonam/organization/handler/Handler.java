package me.sonam.organization.handler;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface Handler {

    Mono<ServerResponse> getOrganizationsByOwnerId(ServerRequest serverRequest);
    Mono<ServerResponse> getOrganizationById(ServerRequest serverRequest);
     Mono<ServerResponse> createOrganization(ServerRequest serverRequest);
     Mono<ServerResponse> updateOrganization(ServerRequest serverRequest);
     Mono<ServerResponse> deleteOrganization(ServerRequest serverRequest);
    Mono<ServerResponse> addUserToOrganization(ServerRequest serverRequest);
    Mono<ServerResponse> removeUserFromOrganization(ServerRequest serverRequest);
     Mono<ServerResponse> getOrganizationUsers(ServerRequest serverRequest);
    Mono<ServerResponse> createOrganizationPosition(ServerRequest serverRequest);
    Mono<ServerResponse> updatePosition(ServerRequest serverRequest);
    Mono<ServerResponse> getOrganizationPositions(ServerRequest serverRequest);
    Mono<ServerResponse> deletePosition(ServerRequest serverRequest);
    Mono<ServerResponse> getPositionById(ServerRequest serverRequest);
    Mono<ServerResponse> userExistsInOrganization(ServerRequest serverRequest);
    Mono<ServerResponse> deleteByUserId(ServerRequest serverRequest);
}