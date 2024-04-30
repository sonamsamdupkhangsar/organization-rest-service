package me.sonam.organization.handler.service;

import me.sonam.organization.handler.OrgException;
import me.sonam.organization.handler.OrganizationBehavior;
import me.sonam.organization.handler.OrganizationBody;
import me.sonam.organization.handler.OrganizationUserBody;
import me.sonam.organization.repo.OrganizationPositionRepository;
import me.sonam.organization.repo.OrganizationRepository;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OrganizationAssociation implements OrganizationBehavior {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationAssociation.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationUserRepository organizationUserRepository;

    @Autowired
    private OrganizationPositionRepository organizationPositionRepository;

    @Override
    public Mono<Page<Organization>> getOrganizationsByOwnerId(UUID ownerId, Pageable pageable) {
        LOG.info("get all organizations");

        return organizationRepository.findByCreatorUserId(ownerId, pageable).collectList()
                .doOnNext(organizations -> {
                    LOG.info("organizations found by ownerId: {}, {}", ownerId, organizations);
                })

                .zipWith(organizationRepository.countByCreatorUserId(ownerId))
                .map(objects -> new PageImpl<>(objects.getT1(), pageable, objects.getT2()));
    }

    @Override
    public Mono<Organization> getOrganizationById(UUID organizationId) {
        LOG.info("find organization by id");
        return organizationRepository.findById(organizationId).
        switchIfEmpty(Mono.error(new OrgException("No organization found with id")));
    }

    @Override
    public Mono<Organization> createOrganization(Mono<OrganizationBody> organizationBodyMono) {
        LOG.info("create organization");

        return organizationBodyMono.flatMap(organizationBody ->
                organizationRepository.save(new Organization(null, organizationBody.getName(),
                        organizationBody.getCreatorUserId())))
                .flatMap(organization->
                        Mono.just(new OrganizationUser
                                (null, organization.getId(), organization.getCreatorUserId(),
                                        null)).zipWith(Mono.just(organization)))
                .flatMap(objects -> organizationUserRepository.save(objects.getT1()).thenReturn(objects.getT2()));
    }

    @Override
    public Mono<Organization> updateOrganization(Mono<OrganizationBody> organizationBodyMono) {
        LOG.info("update application");

        return organizationBodyMono.flatMap(organizationBody ->
                organizationRepository.save(new Organization(organizationBody.getId(),
                        organizationBody.getName(), organizationBody.getCreatorUserId())));
    }

    @Override
    public Mono<String> deleteOrganization(UUID organizationId) {
        LOG.info("delete organization and organization users");
        return organizationRepository.deleteById(organizationId).
                then(organizationUserRepository.deleteByOrganizationId(organizationId)).
                thenReturn("organization deleted");
    }

    @Override
    public Mono<String> removeUserFromOrganization(UUID userId, UUID organizationId) {
        LOG.info("delete user {} and organization {} association", userId, organizationId);

        return  organizationUserRepository.deleteByOrganizationIdAndUserId(organizationId, userId)
                .thenReturn("deleted by organizationId and userId");
    }

    @Override
    public Mono<String> addUserToOrganization(OrganizationUserBody organizationUserBody) {
        LOG.info("create organization user");
        return organizationUserRepository.existsByOrganizationIdAndUserId(
                        organizationUserBody.getOrganizationId(), organizationUserBody.getUserId())
                .doOnNext(aBoolean -> LOG.info("exists by orgIdAndUserId already?: {}", aBoolean))
                .filter(aBoolean -> !aBoolean)
                .map(aBoolean -> new OrganizationUser
                        (null, organizationUserBody.getOrganizationId(), organizationUserBody.getUserId(),
                                organizationUserBody.getPositionId()))
                .flatMap(organizationUser -> organizationUserRepository.save(organizationUser))
                .thenReturn("added user to organization");

    }

    @Override
    public Mono<Page<UUID>> getOrganizationUsers(UUID organizationId, Pageable pageable) {
        LOG.info("get users in organization");

        return organizationUserRepository.findByOrganizationId(organizationId, pageable)
                .map(OrganizationUser::getUserId)
                .collectList()
                .zipWith(organizationUserRepository.countByOrganizationId(organizationId))
                .map(objects -> new PageImpl<>(objects.getT1(), pageable, objects.getT2()));
    }

    @Override
    public Mono<Boolean> userExistsInOrganization(UUID organizationId, UUID userId) {
        LOG.info("check if user-id {} exists in organization-id {}", userId, organizationId);

        return organizationUserRepository.existsByOrganizationIdAndUserId(organizationId, userId);
    }

}
