package me.sonam.organization.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

public interface Handler {

    Mono<ServerResponse> getOrganizations(ServerRequest serverRequest);
    Mono<ServerResponse> getOrganizationById(ServerRequest serverRequest);
     Mono<ServerResponse> createOrganization(ServerRequest serverRequest);
     Mono<ServerResponse> updateOrganization(ServerRequest serverRequest);
     Mono<ServerResponse> deleteOrganization(ServerRequest serverRequest);
     Mono<ServerResponse> updateOrganizationUsers(ServerRequest serverRequest);
     Mono<ServerResponse> getOrganizationUsers(ServerRequest serverRequest);
    Mono<ServerResponse> createOrganizationPosition(ServerRequest serverRequest);
    Mono<ServerResponse> updatePosition(ServerRequest serverRequest);
    Mono<ServerResponse> getOrganizationPositions(ServerRequest serverRequest);
    Mono<ServerResponse> deletePosition(ServerRequest serverRequest);
    Mono<ServerResponse> getPositionById(ServerRequest serverRequest);

}