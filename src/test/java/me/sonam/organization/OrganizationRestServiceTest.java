package me.sonam.organization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.sonam.organization.handler.OrganizationBody;
import me.sonam.organization.handler.OrganizationUserBody;
import me.sonam.organization.handler.UserUpdate;
import me.sonam.organization.repo.OrganizationRepository;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
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

    @Test
    public void createOrganization() {
        LOG.info("create organization");
        UUID creatorId = UUID.randomUUID();
        final String authenticationId = "sonam";
        Jwt jwt = jwt(authenticationId);
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        OrganizationBody organizationBody = new OrganizationBody(null, "Baggy Pants Company", creatorId);
        EntityExchangeResult<String> result = webTestClient.post().uri("/organizations").headers(addJwt(jwt)).bodyValue(organizationBody)
                .exchange().expectStatus().isCreated().expectBody(String.class).returnResult();

        LOG.info("result: {}", result.getResponseBody());
        assertThat(result.getResponseBody()).isNotEmpty();

        UUID organizationId = UUID.fromString(result.getResponseBody());

        organizationRepository.findById(organizationId)
                .subscribe(organization -> LOG.info("found organization with id: {}", organization));

        LOG.info("verify organization can be retrieved");

        result = webTestClient.get().uri("/organizations").headers(addJwt(jwt)).exchange().expectStatus().isOk().expectBody(String.class)
                .returnResult();

        LOG.info("page result contains: {}", result);

        organizationBody = new OrganizationBody(organizationId, "New Name", creatorId);
        result = webTestClient.put().uri("/organizations").headers(addJwt(jwt)).bodyValue(organizationBody)
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult();

        LOG.info("result from update: {}", result.getResponseBody());
        assertThat(result.getResponseBody()).isEqualTo(organizationBody.getId().toString());

        organizationRepository.findById(organizationId)
                .subscribe(organization -> LOG.info("found organization with id: {}", organization));

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        LOG.info("userId1: {}, userId2: {}, userId3: {}", userId1, userId2, userId3);

        List<OrganizationUserBody> organizationUserBodies = Arrays.asList(new OrganizationUserBody(null,
                        organizationId, userId1, OrganizationUserBody.UpdateAction.add,
                        OrganizationUser.RoleNamesEnum.admin.name()),
                new OrganizationUserBody(null,
                        organizationId, userId2, OrganizationUserBody.UpdateAction.add,
                        OrganizationUser.RoleNamesEnum.admin.name()),
                new OrganizationUserBody(null,
                        organizationId, userId3, OrganizationUserBody.UpdateAction.add,
                        OrganizationUser.RoleNamesEnum.user.name()));

        LOG.info("add user to organization");

        result = webTestClient.put().uri("/organizations/users").headers(addJwt(jwt)).bodyValue(organizationUserBodies)
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult();
        LOG.info("result: {}", result.getResponseBody());

        LOG.info("get applications by id and all users in it, which should give 4 applicationUsers");
        EntityExchangeResult<RestPage> createdResult = webTestClient.get().uri("/organizations/"+organizationId+"/users")
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(RestPage.class).returnResult();

        LOG.info("pageResult pageable {}", createdResult.getResponseBody().getPageable());
        LOG.info("assert that only applicationuser exists");
        assertThat(createdResult.getResponseBody().getContent().size()).isEqualTo(4);
        LOG.info("applicationUser: {}", createdResult.getResponseBody().getContent().get(0));
        createdResult.getResponseBody().getContent().forEach(o -> {
            LinkedHashMap<String, String> linkedHashMap1 = (LinkedHashMap) o;

            LOG.info("linkedHashMap1: {}", linkedHashMap1);

            if (linkedHashMap1.get("userId").toString().equals(userId1.toString())) {
                assertThat(linkedHashMap1.get("userRole")).isEqualTo("admin");
                LOG.info("verified is admin for userUpdate 1");
            }
            else if (linkedHashMap1.get("userId").toString().equals(userId2.toString())) {
                assertThat(linkedHashMap1.get("userRole")).isEqualTo("admin");
                LOG.info("verified is admin for userUpdate 2");
            }
            else if (linkedHashMap1.get("userId").toString().equals(userId3.toString())) {
                assertThat(linkedHashMap1.get("userRole")).isEqualTo("user");
                LOG.info("verified is user for userUpdate 3");
            }
            else {
                assertThat(linkedHashMap1.get("userRole").toString()).isEqualTo("admin");
                LOG.info("verified is user for userUpdate from initialization which by default is admin");
            }

        });

        //leave null for id to generate its own
        organizationUserBodies = Arrays.asList(new OrganizationUserBody(null,
                        organizationId, userId1, OrganizationUserBody.UpdateAction.update,
                        OrganizationUser.RoleNamesEnum.user.name()),
                new OrganizationUserBody(null,
                        organizationId, userId2, OrganizationUserBody.UpdateAction.update,
                        OrganizationUser.RoleNamesEnum.user.name()),
                new OrganizationUserBody(null,
                        organizationId, userId3, OrganizationUserBody.UpdateAction.delete,
                        null));

        LOG.info("update organizationUsers, delete one");
        result = webTestClient.put().uri("/organizations/users").headers(addJwt(jwt)).bodyValue(organizationUserBodies)
                .headers(addJwt(jwt))
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult();
        LOG.info("update user add and delete result: {}", result.getResponseBody());

        LOG.info("get organizationUsers by organizationId and all users in it, which should give 3 organizatonUsers after deleting the 1");
        createdResult = webTestClient.get().uri("/organizations/"+organizationId+"/users")
                .headers(addJwt(jwt)).exchange().expectStatus().isOk().expectBody(RestPage.class).returnResult();

        LOG.info("pageResult pageable {}", createdResult.getResponseBody().getPageable());
        LOG.info("assert that only organizationuser exists");
        assertThat(createdResult.getResponseBody().getContent().size()).isEqualTo(3);
        LOG.info("applicationUser: {}", createdResult.getResponseBody().getContent().get(0));
        createdResult.getResponseBody().getContent().forEach(o -> {
            LinkedHashMap<String, String> linkedHashMap1 = (LinkedHashMap) o;

            LOG.info("linkedHashMap1: {}", linkedHashMap1);

            if (linkedHashMap1.get("userId").toString().equals(userId1.toString())) {
                assertThat(linkedHashMap1.get("userRole")).isEqualTo("user");
                LOG.info("verified is changed from admin to user for userUpdate 1");
            }
            else if (linkedHashMap1.get("userId").toString().equals(userId2.toString())) {
                assertThat(linkedHashMap1.get("userRole")).isEqualTo("user");
                LOG.info("verified is changed from admin to user for userUpdate 2");
            }
            else if (linkedHashMap1.get("userId").toString().equals(userId3.toString())) {
                fail("this should not happen as userId3 is now deleted after update");
            }
            else {
                assertThat(linkedHashMap1.get("userRole").toString()).isEqualTo("admin");
                LOG.info("verified is user from initialization");
            }

        });

        result = webTestClient.delete().uri("/organizations/"+organizationId).headers(addJwt(jwt)).exchange().expectStatus().isOk().expectBody(String.class)
                .returnResult();
        assertThat(result.getResponseBody()).isEqualTo("organization deleted");

        StepVerifier.create(organizationRepository.existsById(organizationId)).expectNext(false).expectComplete();
        organizationRepository.existsById(organizationId).subscribe(aBoolean -> LOG.info("should be false after deletion: {}",aBoolean));

        LOG.info("expect bad request after deleting the orgainzationId");
        result = webTestClient.get().uri("/organizations/"+organizationId)
                .headers(addJwt(jwt)).exchange().expectStatus().isBadRequest()
                .expectBody(String.class).returnResult();
        LOG.info("got page results for applications by organizations: {}", result);
    }


    private Jwt jwt(String subjectName) {
        return new Jwt("token", null, null,
                Map.of("alg", "none"), Map.of("sub", subjectName));
    }

    private Consumer<HttpHeaders> addJwt(Jwt jwt) {
        return headers -> headers.setBearerAuth(jwt.getTokenValue());
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