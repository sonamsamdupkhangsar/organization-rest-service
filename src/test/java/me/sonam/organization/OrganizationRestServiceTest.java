package me.sonam.organization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.sonam.organization.handler.OrganizationBody;
import me.sonam.organization.handler.OrganizationUserBody;
import me.sonam.organization.repo.OrganizationPositionRepository;
import me.sonam.organization.repo.OrganizationRepository;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.entity.Organization;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

/**
 * this will test the organization create, update and add users to organization
 */

@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest( classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class OrganizationRestServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationRestServiceTest.class);

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

    @AfterEach
    public void deleteALl() {
        organizationRepository.deleteAll().subscribe(unused -> LOG.info("deleted all organizations"));
        organizationUserRepository.deleteAll().subscribe(unused -> LOG.info("deleted all organization users"));
    }

    private Organization createOrganization(UUID creatorId, String organizationName, Jwt jwt) {
        OrganizationBody organizationBody = new OrganizationBody(null, organizationName, creatorId, null);
        EntityExchangeResult<Organization> organizationEntityExchangeResult = webTestClient.mutateWith(mockJwt().jwt(jwt))
                .post().uri("/organizations").headers(addJwt(jwt)).bodyValue(organizationBody)
                .exchange().expectStatus().isCreated().expectBody(Organization.class).returnResult();

        assertThat(organizationEntityExchangeResult.getResponseBody()).isNotNull();
        assertThat(organizationEntityExchangeResult.getResponseBody().getId()).isNotNull();
        assertThat(organizationEntityExchangeResult.getResponseBody().getName()).isEqualTo(organizationName);
        assertThat(organizationEntityExchangeResult.getResponseBody().getCreatorUserId()).isEqualTo(creatorId);

        return organizationEntityExchangeResult.getResponseBody();
    }

    private Organization updateOrganization(Organization organization, Jwt jwt) {
        OrganizationBody organizationBody = new OrganizationBody(organization.getId(), "New Name", organization.getCreatorUserId(), null);
        EntityExchangeResult<Organization> organizationEntityExchangeResult = webTestClient.
                mutateWith(mockJwt().jwt(jwt)).put().uri("/organizations").headers(addJwt(jwt)).bodyValue(organizationBody)
                .exchange().expectStatus().isOk().expectBody(Organization.class).returnResult();

        Organization updatedOrganization = organizationEntityExchangeResult.getResponseBody();

        LOG.info("result from update: {}", organization);
        assertThat(updatedOrganization).isNotNull();
        assertThat(updatedOrganization.getCreatorUserId()).isEqualTo(organization.getCreatorUserId());
        assertThat(updatedOrganization.getName()).isEqualTo("New Name");

        return updatedOrganization;
    }

    private void addUserToOrganization(UUID organizationId, UUID userId, Jwt jwt, UUID positionId) {
        LOG.info("add user to organization");

        webTestClient.mutateWith(mockJwt().jwt(jwt)).post().uri("/organizations/users").bodyValue(new OrganizationUserBody(null, organizationId, userId, positionId))
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(Map.class).isEqualTo(Map.of("message", "added user to organization"));
    }

    private void removeUserFromOrganization(UUID organizationId, UUID userId, Jwt jwt) {
        LOG.info("remove user from organization");
        webTestClient.mutateWith(mockJwt().jwt(jwt)).delete().uri("/organizations/"+organizationId+"/users/"+userId)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(Map.class).isEqualTo(Map.of("message", "deleted by organizationId and userId"));
    }

    private void getOrganizationUsers(List<UUID> userIdList, Organization organization, Jwt jwt) {
        LOG.info("get applications by id and all users in it, which should give 4 applicationUsers");

        EntityExchangeResult<RestPage<UUID>> entityExchangeResult = webTestClient
                .mutateWith(mockJwt().jwt(jwt)).get().uri("/organizations/"+organization.getId()+"/users")
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(new ParameterizedTypeReference<RestPage<UUID>>() {}).returnResult();

        RestPage<UUID> restPage = entityExchangeResult.getResponseBody();

        assertThat(restPage).isNotNull();
        LOG.info("uuid in restPage: {}", restPage.getContent());

        LOG.info("assert that only application user exists");
        assertThat(restPage.getContent().size()).isEqualTo(userIdList.size()); //plus 1 for the creator as there will be a organization user association when created

        assertThat(restPage.getContent().containsAll(userIdList)).isTrue();
    }

    @Test
    public void createOrganizationAndUpdateAndAddOrganizationUsers() {
        LOG.info("create organization");
        UUID creatorId = UUID.randomUUID();
        final String authenticationId = "sonam";
        Jwt jwt = jwt(authenticationId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        Organization organization = createOrganization(creatorId, "Baggy Pants Company", jwt);

        updateOrganization(organization, jwt);

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        LOG.info("userId1: {}, userId2: {}, userId3: {}", userId1, userId2, userId3);

        UUID vpPosition = UUID.randomUUID();
        UUID salesPosition = UUID.randomUUID();
        UUID croPosition = UUID.randomUUID();

        addUserToOrganization(organization.getId(), userId1, jwt, vpPosition);
        addUserToOrganization(organization.getId(), userId2, jwt, salesPosition);
        addUserToOrganization(organization.getId(), userId3, jwt, croPosition);


        getOrganizationUsers(List.of(creatorId, userId1, userId2, userId3), organization, jwt);

        removeUserFromOrganization(organization.getId(), userId1, jwt);
        removeUserFromOrganization(organization.getId(), userId2, jwt);
        removeUserFromOrganization(organization.getId(), userId3, jwt);

        getOrganizationUsers(List.of(creatorId), organization, jwt);
    }

    @Autowired
    ApplicationContext context;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(this.context)
                // add Spring Security test Support
                .apply(springSecurity())
                .configureClient()
                .filter(basicAuthentication("user", "password"))
                .build();
    }
    @Test
    public void deleteMyOrganizations() {
        LOG.info("This test is to delete organization and ensure the associated position and user are deleted also");
        UUID creatorId = UUID.randomUUID();
        final String authenticationId = "sonam";
        Jwt jwt = jwt(authenticationId, creatorId);
       // when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
        final String organizationName = "Baggy Pants Company";

        LOG.info("Create organization");
        Organization organization = createOrganization(creatorId, organizationName, jwt);
        Organization organization2 = createOrganization(creatorId, organizationName+2, jwt);
        Organization organization3 = createOrganization(creatorId, organizationName+3, jwt);
        Organization organization4 = createOrganization(creatorId, organizationName+4, jwt);
        Organization organization5 = createOrganization(creatorId, organizationName+5, jwt);
        Organization organization6 = createOrganization(creatorId, organizationName+6, jwt);

        updateOrganization(organization, jwt);

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        LOG.info("userId1: {}, userId2: {}, userId3: {}", userId1, userId2, userId3);

        UUID vpPosition = UUID.randomUUID();
        UUID salesPosition = UUID.randomUUID();
        UUID croPosition = UUID.randomUUID();


        LOG.info("create OrganizationPositions");
        createOrganizationPosition(jwt, organization.getId(), "VP");
        createOrganizationPosition(jwt, organization.getId(), "sales");
        createOrganizationPosition(jwt, organization.getId(), "CRO");

        LOG.info("associate the organizationPosition to users, which will store in OrganizationUser (orgId, postionId, userId) table");
        addUserToOrganization(organization.getId(), userId1, jwt, vpPosition);
        addUserToOrganization(organization.getId(), userId2, jwt, salesPosition);
        addUserToOrganization(organization.getId(), userId3, jwt, croPosition);

        LOG.info("call delete my organization");
        deleteMyOrganizationCall(jwt, creatorId, organization.getId());

    }

    public UUID createOrganizationPosition(Jwt jwt, UUID orgId, String positionName) {
        LOG.info("saving a organization position");

        EntityExchangeResult<Map> entityExchangeResult = webTestClient.mutateWith(mockJwt().jwt(jwt)).post()
                .uri("/organizations/"+orgId+"/positions")
                .headers(addJwt(jwt)).bodyValue(
                        Map.of("organizationId", orgId, "name", positionName)).exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult();

        assertThat(entityExchangeResult.getResponseBody().get("id")).isNotNull();
        return UUID.fromString(entityExchangeResult.getResponseBody().get("id").toString());
    }


    private void deleteMyOrganizationCall(Jwt jwt, UUID userId, UUID orgId) {
        LOG.info("add user to organization");

        webTestClient.mutateWith(mockJwt().jwt(jwt)).delete().uri("/organizations/users/"+userId)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(Map.class).isEqualTo(Map.of("message", "delete organization success for user id: " + userId));


        StepVerifier.create(organizationRepository.findByCreatorUserId(userId)).expectComplete();

        StepVerifier.create(organizationRepository.countByCreatorUserId(userId)).assertNext(aLong -> {
            LOG.info("there should be 0 organization: {}", aLong);
            assertThat(aLong).isEqualTo(0);
        }).verifyComplete();

        StepVerifier.create(organizationUserRepository.countByOrganizationId(orgId)).assertNext(aLong -> {
            LOG.info("there should be 0 organizationUser: {}", aLong);
            assertThat(aLong).isEqualTo(0);
        }).verifyComplete();

        StepVerifier.create(organizationPositionRepository.countByOrganizationId(orgId)).assertNext(aLong -> {
            LOG.info("there should be 0 organizationPosition: {}", aLong);
            assertThat(aLong).isEqualTo(0);
        }).verifyComplete();

    }


    private Jwt jwt(String subjectName) {
        return new Jwt("token", null, null,
                Map.of("alg", "none"), Map.of("sub", subjectName));
    }

    private Consumer<HttpHeaders> addJwt(Jwt jwt) {
        return headers -> headers.setBearerAuth(jwt.getTokenValue());
    }
    private Jwt jwt(String subjectName, UUID userId) {
        return new Jwt("token", null, null,
                Map.of("alg", "none"), Map.of("sub", subjectName, "userId", userId.toString()));
    }

}

@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})
class RestPage<T> extends PageImpl<T> {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPage(@JsonProperty("content") List<T> content,
                    @JsonProperty("number") int page,
                    @JsonProperty("size") int size,
                    @JsonProperty("totalElements") long total,
                    @JsonProperty("numberOfElements") int numberOfElements,
                    @JsonProperty("pageNumber") int pageNumber
    ) {
        super(content, PageRequest.of(page, size), total);
    }

    public RestPage(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }
}