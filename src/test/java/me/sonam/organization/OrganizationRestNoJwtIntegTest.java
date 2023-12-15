package me.sonam.organization;

import me.sonam.organization.handler.OrganizationBody;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest( classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrganizationRestNoJwtIntegTest {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationRestNoJwtIntegTest.class);
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void noJwtTestUnAuthorized() {
        LOG.info("create organization without a jwt should give unauthorized error");
        UUID creatorId = UUID.randomUUID();
        final String authenticationId = "sonam";

        OrganizationBody organizationBody = new OrganizationBody(null, "Baggy Pants Company", creatorId, null);
        EntityExchangeResult<String> result = webTestClient.post().uri("/organizations")
                .bodyValue(organizationBody)
                    .exchange().expectStatus().isUnauthorized().expectBody(String.class).returnResult();

        LOG.info("result: {}, httpStatus: {}", result.getResponseBody(), result.getStatus());
    }

}
