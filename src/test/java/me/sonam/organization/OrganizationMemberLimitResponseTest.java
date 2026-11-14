package me.sonam.organization;

import me.sonam.organization.handler.OrgException;
import me.sonam.organization.handler.OrganizationBehavior;
import me.sonam.organization.handler.OrganizationHandler;
import me.sonam.organization.handler.OrganizationMemberLimitException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationMemberLimitResponseTest {
    @Test
    void quotaFailureHasStableCodeButOrdinaryValidationDoesNot() {
        OrganizationBehavior organizationBehavior = mock(OrganizationBehavior.class);
        OrganizationHandler handler = new OrganizationHandler();
        ReflectionTestUtils.setField(handler, "organizationBehavior", organizationBehavior);
        WebTestClient client = WebTestClient.bindToRouterFunction(RouterFunctions.route()
                .POST("/members", handler::addUserToOrganization).build()).build();

        when(organizationBehavior.addUserToOrganization(any()))
                .thenReturn(Mono.error(new OrganizationMemberLimitException()));
        client.post().uri("/members").contentType(MediaType.APPLICATION_JSON).bodyValue("{}")
                .exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("ORGANIZATION_MEMBER_LIMIT_REACHED");

        when(organizationBehavior.addUserToOrganization(any())).thenReturn(Mono.error(new OrgException("Invalid member")));
        client.post().uri("/members").contentType(MediaType.APPLICATION_JSON).bodyValue("{}")
                .exchange().expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").doesNotExist();
    }
}
