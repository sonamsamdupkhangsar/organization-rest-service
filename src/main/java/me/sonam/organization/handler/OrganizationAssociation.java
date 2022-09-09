package me.sonam.organization.handler;

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

    @Override
    public Mono<Page<Organization>> getOrganizations(Pageable pageable) {
        LOG.info("get all organizations");

        return organizationRepository.findAllBy(pageable).collectList()
                .zipWith(organizationRepository.count())
                .map(objects -> new PageImpl<>(objects.getT1(), pageable, objects.getT2()));
    }

    @Override
    public Mono<String> createOrganization(Mono<OrganizationBody> organizationBodyMono) {
        LOG.info("create application");

        return organizationBodyMono.flatMap(organizationBody ->
                organizationRepository.save(new Organization(null, organizationBody.getName(),
                        organizationBody.getCreatorUserId())))
                .map(organization ->
                        new OrganizationUser
                                (null,organization.getId(), organization.getCreatorUserId(),
                                        OrganizationUser.RoleNamesEnum.admin.name()))
                .flatMap(organizationUser -> organizationUserRepository.save(organizationUser))
                .map(organizationUser -> organizationUser.getOrganizationId())
                .flatMap(uuid -> Mono.just(uuid.toString()));
    }

    @Override
    public Mono<String> updateOrganization(Mono<OrganizationBody> organizationBodyMono) {
        LOG.info("update application");

        return organizationBodyMono.flatMap(organizationBody ->
                organizationRepository.save(new Organization(organizationBody.getId(),
                        organizationBody.getName(), organizationBody.getCreatorUserId()))
                .flatMap(organization -> Mono.just(organization.getId().toString()))
        );
    }

    @Override
    public Mono<String> deleteOrganization(UUID organizationId) {
        LOG.info("delete organization");
        return organizationRepository.deleteById(organizationId).thenReturn("organization deleted");
    }

    @Override
    public Mono<String> updateOrganizationUsers(Mono<OrganizationUserBody> organizationUserBodyMono) {
        LOG.info("updated users in organization");

        return organizationUserBodyMono.doOnNext(organizationUserBody -> {
            LOG.info("save organization user updates");
             organizationUserBody.getUserUpdates().forEach(userUpdate -> {
                if (userUpdate.getUpdate().equals(UserUpdate.UpdateAction.add)) {
                    organizationUserRepository.existsByOrganizationIdAndUserId(
                            organizationUserBody.getOrganizationId(), userUpdate.getUserId())
                            .doOnNext(aBoolean -> LOG.info("exists by orgIdAndUserId already?: {}", aBoolean))
                            .filter(aBoolean -> !aBoolean)
                            .map(aBoolean -> new OrganizationUser
                                    (null, organizationUserBody.getOrganizationId(),
                                            userUpdate.getUserId(), userUpdate.getUserRole()))
                            .flatMap(organizationUser -> organizationUserRepository.save(organizationUser))
                            .subscribe(organizationUser -> LOG.info("saved organizationUser"));


                } else if (userUpdate.getUpdate().equals(UserUpdate.UpdateAction.delete)) {
                    if (organizationUserBody.getId() != null) {
                        organizationUserRepository.existsById(organizationUserBody.getId())
                                .filter(aBoolean -> aBoolean)
                                .map(aBoolean -> organizationUserRepository.deleteById(organizationUserBody.getId()))
                                .subscribe(organizationUser -> LOG.info("deleted organizationUser"));
                    }
                    else {
                        LOG.info("deleting using userId and orgId");
                        organizationUserRepository.deleteByOrganizationIdAndUserId(
                                organizationUserBody.getOrganizationId(), userUpdate.getUserId())
                                .subscribe(integer -> LOG.info("delted by organizationId and userId"));
                    }
                }
                else if (userUpdate.getUpdate().equals(UserUpdate.UpdateAction.update)) {
                    organizationUserRepository.findByOrganizationIdAndUserId(
                            organizationUserBody.getOrganizationId(), userUpdate.getUserId())
                            .switchIfEmpty(Mono.just(
                                    new OrganizationUser(null, organizationUserBody.getOrganizationId(),
                                            userUpdate.getUserId(), userUpdate.getUserRole())))
                            .flatMap(organizationUser -> organizationUserRepository.save(organizationUser))
                            .subscribe(organizationUser -> LOG.info("updated orgainzationUser"));
                }
                 else {
                    throw new OrgException("UserUpdate action invalid: " + userUpdate.getUpdate().name());
                }
            });
        }).thenReturn("organization update done");
    }

    @Override
    public Mono<Page<OrganizationUser>> getOrganizationUsers(UUID organizationId, Pageable pageable) {
        LOG.info("get users in organization");

        return organizationUserRepository.findByOrganizationId(organizationId, pageable)
                .collectList()
                .zipWith(organizationUserRepository.countByOrganizationId(organizationId))
                .map(objects -> new PageImpl<>(objects.getT1(), pageable, objects.getT2()));
    }
}
