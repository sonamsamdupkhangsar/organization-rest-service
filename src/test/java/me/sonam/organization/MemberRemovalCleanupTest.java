package me.sonam.organization;

import java.util.UUID;
import me.sonam.organization.handler.service.MemberClientRoleCleanup;
import me.sonam.organization.handler.service.OrganizationAssociation;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.UserDefaultOrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class MemberRemovalCleanupTest {
    @Test
    void cleanupFailurePreservesMembershipAndDefaultSelection() {
        UUID userId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        MemberClientRoleCleanup memberClientRoleCleanup = mock(MemberClientRoleCleanup.class);
        OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
        UserDefaultOrganizationRepository userDefaultOrganizationRepository = mock(UserDefaultOrganizationRepository.class);
        OrganizationAssociation organizationAssociation = new OrganizationAssociation();
        ReflectionTestUtils.setField(organizationAssociation, "memberClientRoleCleanup", memberClientRoleCleanup);
        ReflectionTestUtils.setField(organizationAssociation, "organizationUserRepository", organizationUserRepository);
        ReflectionTestUtils.setField(organizationAssociation, "userDefaultOrganizationRepository", userDefaultOrganizationRepository);
        when(memberClientRoleCleanup.removeMemberClientRoles(organizationId, userId))
                .thenReturn(Mono.error(new IllegalStateException("Role service unavailable")));

        StepVerifier.create(organizationAssociation.removeUserFromOrganization(userId, organizationId))
                .expectErrorMessage("Role service unavailable")
                .verify();

        verifyNoInteractions(organizationUserRepository, userDefaultOrganizationRepository);
    }

    @Test
    void membershipIsRemovedOnlyAfterCleanupCompletes() {
        UUID userId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        MemberClientRoleCleanup memberClientRoleCleanup = mock(MemberClientRoleCleanup.class);
        OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
        UserDefaultOrganizationRepository userDefaultOrganizationRepository = mock(UserDefaultOrganizationRepository.class);
        OrganizationAssociation organizationAssociation = new OrganizationAssociation();
        ReflectionTestUtils.setField(organizationAssociation, "memberClientRoleCleanup", memberClientRoleCleanup);
        ReflectionTestUtils.setField(organizationAssociation, "organizationUserRepository", organizationUserRepository);
        ReflectionTestUtils.setField(organizationAssociation, "userDefaultOrganizationRepository", userDefaultOrganizationRepository);
        when(memberClientRoleCleanup.removeMemberClientRoles(organizationId, userId)).thenReturn(Mono.empty());
        when(userDefaultOrganizationRepository.deleteByUserIdAndOrganizationId(userId, organizationId)).thenReturn(Mono.just(1));
        when(organizationUserRepository.deleteByOrganizationIdAndUserId(organizationId, userId)).thenReturn(Mono.just(1));

        StepVerifier.create(organizationAssociation.removeUserFromOrganization(userId, organizationId))
                .expectNext("deleted by organizationId and userId")
                .verifyComplete();

        var order = inOrder(memberClientRoleCleanup, userDefaultOrganizationRepository, organizationUserRepository);
        order.verify(memberClientRoleCleanup).removeMemberClientRoles(organizationId, userId);
        order.verify(userDefaultOrganizationRepository).deleteByUserIdAndOrganizationId(userId, organizationId);
        order.verify(organizationUserRepository).deleteByOrganizationIdAndUserId(organizationId, userId);
    }
}
