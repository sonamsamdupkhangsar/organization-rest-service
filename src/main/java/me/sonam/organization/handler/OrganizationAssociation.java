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
import reactor.core.publisher.Flux;
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
    public Mono<Organization> getOrganizationById(UUID organizationId) {
        LOG.info("find organization by id");
        return organizationRepository.findById(organizationId).
        switchIfEmpty(Mono.error(new OrgException("No organization found with id")));
    }

    @Override
    public Mono<String> createOrganization(Mono<OrganizationBody> organizationBodyMono) {
        LOG.info("create application");

        return organizationBodyMono.flatMap(organizationBody ->
                organizationRepository.save(new Organization(null, organizationBody.getName(),
                        organizationBody.getCreatorUserId())))
                .map(organization->
                        new OrganizationUser
                                (null, organization.getId(), organization.getCreatorUserId(),
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
    public Mono<String> updateOrganizationUsers(Flux<OrganizationUserBody> organizationUserBodyFlux) {
        LOG.info("update users in organization");

        return organizationUserBodyFlux.doOnNext(organizationUserBody -> {
            LOG.info("save organization user updates");

            organizationRepository.existsById(organizationUserBody.getOrganizationId()).
                    switchIfEmpty(Mono.error(new OrgException("No organization found with organizationId: "
                            + organizationUserBody.getOrganizationId())));

            if (organizationUserBody.getUpdateAction().equals(OrganizationUserBody.UpdateAction.add)) {
                organizationUserRepository.existsByOrganizationIdAndUserId(
                                organizationUserBody.getOrganizationId(), organizationUserBody.getUserId())
                        .doOnNext(aBoolean -> LOG.info("exists by orgIdAndUserId already?: {}", aBoolean))
                        .filter(aBoolean -> !aBoolean)
                        .map(aBoolean -> new OrganizationUser
                                (null, organizationUserBody.getOrganizationId(), organizationUserBody.getUserId(),
                                        organizationUserBody.getUserRole()))
                        .flatMap(organizationUser -> organizationUserRepository.save(organizationUser))
                        .subscribe(organizationUser -> LOG.info("saved organizationUser"));
            }
            else if (organizationUserBody.getUpdateAction().equals(OrganizationUserBody.UpdateAction.delete)) {
                if (organizationUserBody.getId() != null) {
                    organizationUserRepository.existsById(organizationUserBody.getId())
                            .filter(aBoolean -> aBoolean)
                            .map(aBoolean -> organizationUserRepository.deleteById(organizationUserBody.getId()))
                            .subscribe(organizationUser -> LOG.info("deleted organizationUser"));
                }
                else {
                    LOG.info("deleting using userId and appId");
                    organizationUserRepository.deleteByOrganizationIdAndUserId(
                                    organizationUserBody.getOrganizationId(), organizationUserBody.getUserId())
                            .subscribe(rows -> LOG.info("deleted by organizationId and userId: {}", rows));
                }
            }
            else if (organizationUserBody.getUpdateAction().equals(OrganizationUserBody.UpdateAction.update)) {
                if (organizationUserBody.getId() == null) {
                    LOG.warn("organizationUserId is null on update action");
                }

                //in update the organizationUser with appId and userId must existacc
                organizationUserRepository.findByOrganizationIdAndUserId(
                                organizationUserBody.getOrganizationId(), organizationUserBody.getUserId())
                        .doOnNext(organizationUser -> LOG.info("organizationUser found in update is; {}", organizationUser))
                        .map(organizationUser ->  new OrganizationUser(organizationUser.getId()
                                , organizationUser.getOrganizationId(), organizationUser.getUserId()
                                , organizationUserBody.getUserRole()))
                        .doOnNext(organizationUser -> {
                            organizationUserRepository.countByOrganizationId(organizationUser.getOrganizationId()).subscribe(aLong -> LOG.info("count of organizationUser: {}", aLong));

                        })
                        .flatMap(organizationUser -> organizationUserRepository.save(organizationUser))
                        .subscribe(organizationUser -> LOG.info("updated organizationUser"));
            }
            else {
                throw new OrgException("UserUpdate action invalid: " + organizationUserBody.getUpdateAction().name());
            }
        }).then(Mono.just("applicationUser update done"));
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
