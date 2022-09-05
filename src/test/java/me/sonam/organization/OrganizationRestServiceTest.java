package me.sonam.organization;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class OrganizationRestServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationRestServiceTest.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationUserRepository organizationUserRepository;

    @Autowired
    private WebTestClient webTestClient;

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
        OrganizationBody organizationBody = new OrganizationBody(null, "Baggy Pants Company", creatorId);
        EntityExchangeResult<String> result = webTestClient.post().uri("/organizations").bodyValue(organizationBody)
                .exchange().expectStatus().isCreated().expectBody(String.class).returnResult();

        LOG.info("result: {}", result.getResponseBody());
        assertThat(result.getResponseBody()).isNotEmpty();

        UUID id = UUID.fromString(result.getResponseBody());

        organizationRepository.findById(id)
                .subscribe(organization -> LOG.info("found organization with id: {}", organization));

        LOG.info("verify organization can be retrieved");

        result = webTestClient.get().uri("/organizations").exchange().expectStatus().isOk().expectBody(String.class)
                .returnResult();

        LOG.info("page result contains: {}", result);

        organizationBody = new OrganizationBody(id, "New Name", creatorId);
        result = webTestClient.put().uri("/organizations").bodyValue(organizationBody)
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult();

        LOG.info("result from update: {}", result.getResponseBody());
        assertThat(result.getResponseBody()).isEqualTo(organizationBody.getId().toString());

        organizationRepository.findById(id)
                .subscribe(organization -> LOG.info("found organization with id: {}", organization));

        result = webTestClient.delete().uri("/organizations/"+id).exchange().expectStatus().isOk().expectBody(String.class)
                .returnResult();
        assertThat(result.getResponseBody()).isEqualTo("organization deleted");

        StepVerifier.create(organizationRepository.existsById(id)).expectNext(false).expectComplete();
        organizationRepository.existsById(id).subscribe(aBoolean -> LOG.info("should be false after deletion: {}",aBoolean));

        //UID id, UUID organizationId, List< UserUpdate > userUpdates
        UserUpdate userUpdates1 = new UserUpdate(UUID.randomUUID(), OrganizationUser.RoleNamesEnum.user.name(), UserUpdate.UpdateAction.add.name());
        UserUpdate userUpdates2 = new UserUpdate(UUID.randomUUID(), OrganizationUser.RoleNamesEnum.user.name(), UserUpdate.UpdateAction.add.name());
        UserUpdate userUpdates3 = new UserUpdate(UUID.randomUUID(), OrganizationUser.RoleNamesEnum.user.name(), UserUpdate.UpdateAction.add.name());

        //leave null for id to generate its own
        OrganizationUserBody organizationUserBody = new OrganizationUserBody(null, id,
                Arrays.asList(userUpdates1, userUpdates2, userUpdates3));
        LOG.info("add user to organization");

        result = webTestClient.put().uri("/organizations/users").bodyValue(organizationUserBody)
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult();
        LOG.info("result: {}", result.getResponseBody());

        LOG.info("delete 2 users and leave only 1 to organization");
        userUpdates1 = new UserUpdate(userUpdates1.getUserId(), OrganizationUser.RoleNamesEnum.user.name(), UserUpdate.UpdateAction.add.name());
        userUpdates2 = new UserUpdate(userUpdates2.getUserId(), OrganizationUser.RoleNamesEnum.admin.name(), UserUpdate.UpdateAction.delete.name());
      //  userUpdates3 = new UserUpdate(userUpdates3.getUserId(), UserUpdate.UpdateAction.delete.name());

        //leave id null but pass in organization id 'id'
        organizationUserBody = new OrganizationUserBody(null, id,
                Arrays.asList(userUpdates1, userUpdates2, userUpdates3));
        LOG.info("add user to organization");

        result = webTestClient.put().uri("/organizations/users").bodyValue(organizationUserBody)
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult();
        LOG.info("result: {}", result.getResponseBody());

        LOG.info("get all users in organization {}", id);
        result = webTestClient.get().uri("/organizations/"+id+"/users")
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult();
        LOG.info("result: {}", result.getResponseBody());

    }


}