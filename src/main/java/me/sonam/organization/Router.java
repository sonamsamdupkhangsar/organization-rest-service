package me.sonam.organization;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import me.sonam.organization.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Set AccountService methods route for checking active and to actiate acccount
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Swagger Demo", version = "1.0", description = "Documentation APIs v1.0"))

public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(path = "/organizations"
                    , produces = {
                        MediaType.APPLICATION_JSON_VALUE}, method= RequestMethod.GET,
                         operation = @Operation(operationId="activeUserId", responses = {
                            @ApiResponse(responseCode = "200", description = "successful operation"),
                                 @ApiResponse(responseCode = "400", description = "invalid user id")}
                    ))
            }
    )
    public RouterFunction<ServerResponse> route(Handler handler) {
        LOG.info("building router function");
        return RouterFunctions.route(POST("/organizations").and(accept(MediaType.APPLICATION_JSON)),
                handler::createOrganization)
                .andRoute(PUT("/organizations")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::updateOrganization)
                .andRoute(GET("/organizations")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizations)
                .andRoute(DELETE("/organizations/{organizationId}")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::deleteOrganization)
                .andRoute(PUT("/organizations/users")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::updateOrganizationUsers)
                .andRoute(GET("/organizations/{organizationId}/users")
                .and(accept(MediaType.APPLICATION_JSON)), handler::getOrganizationUsers);

    }
}
