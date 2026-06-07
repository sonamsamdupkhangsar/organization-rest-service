package me.sonam.organization;

import me.sonam.organization.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Set AccountService methods route for checking active and to actiate acccount
 */
@Configuration
public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    @Bean
    public RouterFunction<ServerResponse> route(Handler handler) {
        LOG.info("building router function");
        return RouterFunctions.route(POST("/organizations").and(accept(MediaType.APPLICATION_JSON)),
                handler::createOrganization)
                .andRoute(PUT("/organizations")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::updateOrganization)
                .andRoute(GET("/organizations/owner/{ownerId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationsByOwnerId)
                .andRoute(GET("/organizations/{id}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationById)
                .andRoute(GET("/organizations/subdomain/{subdomain}/organizations")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationsBySubdomain)
                .andRoute(GET("/organizations/subdomain/{subdomain}/users/{userId}/organizations/{organizationId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::userExistsInSubdomainOrganization)
                .andRoute(GET("/organizations/subdomain/{subdomain}/users/{userId}/organizations/{organizationId}/can-add")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::canAddUserToSubdomainOrganization)
                .andRoute(GET("/organizations/subdomain/{subdomain}/organizations/{organizationId}/can-add-user")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::canAddUserToSubdomainOrganizationWithoutUser)
                .andRoute(GET("/organizations/subdomain/{subdomain}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationBySubdomain)
                .andRoute(POST("/organizations/subdomain/{subdomain}/organizations/{organizationId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::addOrganizationToSubdomain)
                .andRoute(DELETE("/organizations/subdomain/{subdomain}/organizations/{organizationId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::removeOrganizationFromSubdomain)
                .andRoute(GET("/organizations/subdomain/{subdomain}/users/{userId}/default-organization-id")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getDefaultOrganizationIdBySubdomainAndUserId)
                .andRoute(PUT("/organizations/ids")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationByIds)
                .andRoute(DELETE("/organizations/{id}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::deleteOrganization)
                .andRoute(POST("/organizations/users")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::addUserToOrganization)
                .andRoute(DELETE("/organizations/{id}/users/{userId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::removeUserFromOrganization)
                .andRoute(GET("/organizations/{id}/users")
                    .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationUsers)
                .andRoute(GET("/organizations/users/{userId}/ids")
                    .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationIdsForUser)
                .andRoute(GET("/organizations/users/{userId}/default-organization-id")
                    .and(accept(MediaType.APPLICATION_JSON)), handler::getDefaultOrganizationIdForUser)
                .andRoute(PUT("/organizations/{id}/users/{userId}/default")
                    .and(accept(MediaType.APPLICATION_JSON)), handler::setDefaultOrganization)
                .andRoute(GET("/organizations/{id}/users/{userId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::userExistsInOrganization)
                .andRoute(POST("/organizations/{id}/positions")
                    .and(accept(MediaType.APPLICATION_JSON)), handler::createOrganizationPosition)
                .andRoute(GET("organizations/{id}/positions")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationPositions)
                .andRoute(PUT("/organizations/{id}/positions")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::updatePosition)
                .andRoute(GET("/organizations/{id}/positions/{positionId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getPositionById)
                .andRoute(DELETE("/organizations/{id}/positions/{positionId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::deletePosition)
                .andRoute(DELETE("/organizations/{organizationId}/users/{userId}/data")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::deleteUserOrganizationData);


    }
}
