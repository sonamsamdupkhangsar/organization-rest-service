package me.sonam.organization.handler.service;

import me.sonam.organization.config.OrganizationUserLimitProperties;
import me.sonam.organization.handler.OrgException;
import me.sonam.organization.repo.OrganizationRepository;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationAssociationLimitTest {

    @Test
    void enforceAddedUserLimitFailsWhenDemoIssuerReachesLimit() {
        UUID creatorUserId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();

        OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
        OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
        OrganizationUserLimitProperties properties = new OrganizationUserLimitProperties();
        properties.setDefaultMaxAddedUsers(5);
        properties.getHosts().put("demo.openissuer.com", 2);

        when(organizationRepository.findById(organizationId))
                .thenReturn(Mono.just(new Organization(organizationId, "Demo Organization", creatorUserId)));
        when(organizationUserRepository.countByOrganizationIdAndUserIdNot(organizationId, creatorUserId))
                .thenReturn(Mono.just(2L));

        OrganizationAssociation organizationAssociation = new OrganizationAssociation();
        ReflectionTestUtils.setField(organizationAssociation, "organizationRepository", organizationRepository);
        ReflectionTestUtils.setField(organizationAssociation, "organizationUserRepository", organizationUserRepository);
        ReflectionTestUtils.setField(organizationAssociation, "organizationUserLimitProperties", properties);

        StepVerifier.create(organizationAssociation.enforceAddedUserLimit(
                        organizationId, "https://demo.openissuer.com"))
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(OrgException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Max number of organization users reached");
                })
                .verify();

        verify(organizationUserRepository, never()).save(any(OrganizationUser.class));
    }
}
