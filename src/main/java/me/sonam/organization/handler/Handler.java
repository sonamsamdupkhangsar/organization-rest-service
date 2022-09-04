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

@Service
public class Handler {
    private static final Logger LOG = LoggerFactory.getLogger(Handler.class);

    @Autowired
    private OrganizationBehavior organizationBehavior;

    public Mono<ServerResponse> getOrganizations(ServerRequest serverRequest) {
        LOG.info("get organizations");
        Pageable pageable = Util.getPageable(serverRequest);

        return organizationBehavior.getOrganizations(pageable)
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("get organizations call failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }

    public Mono<ServerResponse> createOrganization(ServerRequest serverRequest) {
        LOG.info("create organization");

        return organizationBehavior.createOrganization(serverRequest.bodyToMono(OrganizationBody.class))
                .flatMap(s -> ServerResponse.created(URI.create("/organizations/"+s)).contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("create organization failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }

    public Mono<ServerResponse> updateOrganization(ServerRequest serverRequest) {
        LOG.info("update organization");

        return organizationBehavior.updateOrganization(serverRequest.bodyToMono(OrganizationBody.class))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("update organization failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }

    public Mono<ServerResponse> deleteOrganization(ServerRequest serverRequest) {
        LOG.info("delete organization");

        return organizationBehavior.deleteOrganization(UUID.fromString(serverRequest.pathVariable("organizationId")))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("delete organization failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }

    public Mono<ServerResponse> updateOrganizationUsers(ServerRequest serverRequest) {
        LOG.info("update organization user");

        return organizationBehavior.updateOrganizationUsers(serverRequest.bodyToMono(OrganizationUserBody.class))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("update organization user failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }

    public Mono<ServerResponse> getOrganizationUsers(ServerRequest serverRequest) {
        LOG.info("get organization user");

        Pageable pageable = Util.getPageable(serverRequest);

        return organizationBehavior.getOrganizationUsers(UUID.fromString(serverRequest.pathVariable("organizationId")), pageable)
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("get organization user call failed", throwable);
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(throwable.getMessage());
                });
    }

}