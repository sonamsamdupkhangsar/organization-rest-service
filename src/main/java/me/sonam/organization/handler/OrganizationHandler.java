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
import java.util.Map;
import java.util.UUID;

@Service
public class OrganizationHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(Handler.class);

    @Autowired
    private OrganizationBehavior organizationBehavior;
    @Autowired
    private PositionManager manageOrganizePosition;

    public Mono<ServerResponse> getOrganizationsByOwnerId(ServerRequest serverRequest) {
        LOG.info("get organizations");
        Pageable pageable = Util.getPageable(serverRequest);

        return organizationBehavior.getOrganizationsByOwnerId(UUID.fromString(serverRequest.pathVariable("ownerId")), pageable)
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("get organizations call failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    public Mono<ServerResponse> getOrganizationById(ServerRequest serverRequest) {
        LOG.info("get organization by id");

        return organizationBehavior.getOrganizationById(UUID.fromString(serverRequest.pathVariable("id")))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("get organization by id failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    public Mono<ServerResponse> createOrganization(ServerRequest serverRequest) {
        LOG.info("create organization");

        return organizationBehavior.createOrganization(serverRequest.bodyToMono(OrganizationBody.class))
                .flatMap(organization -> ServerResponse.created(URI.create("/organizations/" + organization.getId())).contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(organization))
                .onErrorResume(throwable -> {
                    LOG.error("create organization failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    public Mono<ServerResponse> updateOrganization(ServerRequest serverRequest) {
        LOG.info("update organization");
        //allow if user is admin

        return organizationBehavior.updateOrganization(serverRequest.bodyToMono(OrganizationBody.class))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("update organization failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    public Mono<ServerResponse> deleteOrganization(ServerRequest serverRequest) {
        LOG.info("delete organization");

        return organizationBehavior.deleteOrganization(UUID.fromString(serverRequest.pathVariable("id")))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("delete organization failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> addUserToOrganization(ServerRequest serverRequest) {
        LOG.info("add user to organization");

        return serverRequest.bodyToMono(OrganizationUserBody.class).flatMap(organizationUserBody ->
                organizationBehavior.addUserToOrganization(organizationUserBody))
                        .flatMap(s -> {
                            LOG.info("added user to organization: {}",s);
                            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s));
                        })
                        .onErrorResume(throwable -> {
                            LOG.error("add user to organization failed: {}", throwable.getMessage());
                            return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(Map.of("error", throwable.getMessage()));
                        });
    }

    @Override
    public Mono<ServerResponse> removeUserFromOrganization(ServerRequest serverRequest) {
        LOG.info("remove user from organization");
        final UUID organizationId = UUID.fromString(serverRequest.pathVariable("id"));
        final UUID userId = UUID.fromString(serverRequest.pathVariable("userId"));

        return organizationBehavior.removeUserFromOrganization(userId, organizationId)
                .flatMap(s -> {
                    LOG.info("removed user from organization: {}", s);
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("message", s));
                })
                .onErrorResume(throwable -> {
                    LOG.error("remove user from organization failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    public Mono<ServerResponse> getOrganizationUsers(ServerRequest serverRequest) {
        LOG.info("get organization user");

        Pageable pageable = Util.getPageable(serverRequest);

        return organizationBehavior.getOrganizationUsers(UUID.fromString(serverRequest.pathVariable("id")), pageable)
                .flatMap(s -> {
                    LOG.info("response: {}", s.getContent());
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s);
                })
                .onErrorResume(throwable -> {
                    LOG.error("get organization user call failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> createOrganizationPosition(ServerRequest serverRequest) {
        LOG.info("create position");

        return manageOrganizePosition.createOrganizationPosition(serverRequest.bodyToMono(Map.class))
                .flatMap(id -> ServerResponse.created(URI.create("/organizations/" + serverRequest
                                .pathVariable("id") + "/positions" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("id", id,"message", "created position with id:" + id)))
                .onErrorResume(throwable -> {
                    LOG.error("create position failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    public Mono<ServerResponse> updatePosition(ServerRequest serverRequest) {
        LOG.info("update organization user");

        return manageOrganizePosition.updatePosition(serverRequest.bodyToMono(Map.class))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.error("update position failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> getOrganizationPositions(ServerRequest serverRequest) {
        LOG.info("get organization positions");
        Pageable pageable = Util.getPageable(serverRequest);

        return manageOrganizePosition.getPositions(pageable)
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("get positions call failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> deletePosition(ServerRequest serverRequest) {
        LOG.info("delete position");

        return manageOrganizePosition.deletePosition(UUID.fromString(serverRequest.pathVariable("positionId")),
                        UUID.fromString(serverRequest.pathVariable("id")))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.error("delete position failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> getPositionById(ServerRequest serverRequest) {
        LOG.info("get position by id");

        return manageOrganizePosition.getPositionById(UUID.fromString(serverRequest.pathVariable("positionId")))
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(s))
                .onErrorResume(throwable -> {
                    LOG.error("get position by id failed: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> userExistsInOrganization(ServerRequest serverRequest) {
        LOG.info("check if user exists in organization");

        return organizationBehavior.userExistsInOrganization(
                UUID.fromString(serverRequest.pathVariable("id")),
                        UUID.fromString(serverRequest.pathVariable("userId"))
                        )
                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.error("user does not exist in organization: {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", throwable.getMessage()));
                });
    }
}
