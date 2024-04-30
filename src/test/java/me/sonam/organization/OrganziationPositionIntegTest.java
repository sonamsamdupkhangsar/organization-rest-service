package me.sonam.organization;

import me.sonam.organization.repo.OrganizationPositionRepository;
import me.sonam.organization.repo.OrganizationRepository;
import me.sonam.organization.repo.OrganizationUserRepository;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest( classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class OrganziationPositionIntegTest {
    private static final Logger LOG = LoggerFactory.getLogger(OrganziationPositionIntegTest.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationUserRepository organizationUserRepository;

    @Autowired
    private OrganizationPositionRepository organizationPositionRepository;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    ReactiveJwtDecoder jwtDecoder;

    @Before
    public void setUp() {
        LOG.info("setup mock");
        MockitoAnnotations.openMocks(this);
    }

    // save a organization position and return its id
    public UUID save(Jwt jwt, UUID orgId) {
        LOG.info("saving a organization position");

        EntityExchangeResult<Map> entityExchangeResult = webTestClient.post()
                .uri("/organizations/id/"+orgId+"/positions")
                .headers(addJwt(jwt)).bodyValue(
                Map.of("organizationId", orgId, "name", "VP")).exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult();

        assertThat(entityExchangeResult.getResponseBody().get("id")).isNotNull();
        return UUID.fromString(entityExchangeResult.getResponseBody().get("id").toString());
    }

    public void update(Jwt jwt, UUID orgId, Map<String, String> map) {
        LOG.info("update organization position");

        EntityExchangeResult<Map> entityExchangeResult = webTestClient.put()
                .uri("/organizations/id/"+orgId+"/positions")
                .headers(addJwt(jwt))
                .bodyValue(map)
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult();

        LOG.info("response: {}", entityExchangeResult.getResponseBody());
        assertThat(entityExchangeResult.getResponseBody().get("message")).isEqualTo("organizationPosition updated");
    }

    public Map<String, String> getOrganizationPositions(Jwt jwt, UUID orgId, UUID positionId, HttpStatus httpStatus) {
        LOG.info("get organization position");

        EntityExchangeResult<Map> entityExchangeResult = webTestClient.get()
                .uri("/organizations/id/"+orgId+"/positions/"+positionId)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isEqualTo(httpStatus)
                .expectBody(Map.class).returnResult();

        return entityExchangeResult.getResponseBody();
    }

    public Map<String, String> deletePosition(Jwt jwt, UUID orgId, UUID positionId) {
        LOG.info("delete position by id: {}", positionId);

        EntityExchangeResult<Map> entityExchangeResult = webTestClient.delete()
                .uri("/organizations/id/"+orgId+"/positions/"+positionId)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult();
        return entityExchangeResult.getResponseBody();
    }
    @Test
    public void create() {
        LOG.info("create organization position");
        UUID orgId = UUID.randomUUID();
        final String authenticationId = "sonam";
        Jwt jwt = jwt(authenticationId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        save(jwt, orgId);
    }

    @Test
    public void update() {
        LOG.info("update organization position");
        UUID orgId = UUID.randomUUID();
        final String authenticationId = "sonam";
        Jwt jwt = jwt(authenticationId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        UUID posId = save(jwt, orgId);

        update(jwt, orgId, Map.of("id", posId.toString(), "organizationId", orgId.toString(), "name", "Sales"));

        Map<String, String> map = getOrganizationPositions(jwt, orgId, posId, HttpStatus.OK);
        LOG.info("map: {}", map);
        assertThat(map.get("name")).isEqualTo("Sales");
        assertThat(map.get("id")).isEqualTo(posId.toString());
        assertThat(map.get("organizationId")).isEqualTo(orgId.toString());

    }

    @Test
    public void delete() {
        LOG.info("delete position");
        UUID orgId = UUID.randomUUID();
        final String authenticationId = "sonam";
        Jwt jwt = jwt(authenticationId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        UUID posId = save(jwt, orgId);
        deletePosition(jwt, orgId, posId);
        Map<String, String> map = getOrganizationPositions(jwt, orgId, posId, HttpStatus.BAD_REQUEST);
        assertThat(map.get("error")).isEqualTo("No organization position found with id");
    }



    private Jwt jwt(String subjectName) {
        return new Jwt("token", null, null,
                Map.of("alg", "none"), Map.of("sub", subjectName));
    }

    private Consumer<HttpHeaders> addJwt(Jwt jwt) {
        return headers -> headers.setBearerAuth(jwt.getTokenValue());
    }

}
