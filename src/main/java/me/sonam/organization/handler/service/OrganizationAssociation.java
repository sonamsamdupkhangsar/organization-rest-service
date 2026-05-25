package me.sonam.organization.handler.service;

import me.sonam.organization.handler.OrgException;
import me.sonam.organization.handler.OrganizationBehavior;
import me.sonam.organization.handler.OrganizationBody;
import me.sonam.organization.handler.OrganizationUserBody;
import me.sonam.organization.repo.OrganizationPositionRepository;
import me.sonam.organization.repo.OrganizationRepository;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.SubdomainOrganizationRepository;
import me.sonam.organization.repo.SubdomainRepository;
import me.sonam.organization.repo.UserDefaultOrganizationRepository;
import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import me.sonam.organization.repo.entity.Subdomain;
import me.sonam.organization.repo.entity.SubdomainOrganization;
import me.sonam.organization.repo.entity.UserDefaultOrganization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
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

    @Autowired
    private SubdomainRepository subdomainRepository;

    @Autowired
    private SubdomainOrganizationRepository subdomainOrganizationRepository;

    @Autowired
    private UserDefaultOrganizationRepository userDefaultOrganizationRepository;

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
    public Mono<Organization> getOrganizationBySubdomain(String subdomain) {
        LOG.info("find organization by subdomain {}", subdomain);
        return findOrganizationsByMappedSubdomain(normalizeSubdomain(subdomain))
                .next()
                .switchIfEmpty(Mono.error(new OrgException("No organization found with subdomain")));
    }

    @Override
    public Mono<List<Organization>> getOrganizationsBySubdomain(String subdomain) {
        String normalizedSubdomain = normalizeSubdomain(subdomain);
        LOG.info("find organizations by subdomain {}", normalizedSubdomain);

        return findOrganizationsByMappedSubdomain(normalizedSubdomain)
                .collectList();
    }

    @Override
    public Mono<Boolean> userExistsInSubdomainOrganization(String subdomain, UUID userId, UUID organizationId) {
        String normalizedSubdomain = normalizeSubdomain(subdomain);
        LOG.info("check user {} exists in organization {} mapped to subdomain {}",
                userId, organizationId, normalizedSubdomain);

        return subdomainRepository.findByHost(normalizedSubdomain)
                .flatMap(subdomainEntity -> subdomainOrganizationRepository
                        .existsBySubdomainIdAndOrganizationId(subdomainEntity.getId(), organizationId))
                .defaultIfEmpty(false)
                .flatMap(mapped -> {
                    if (!mapped) {
                        return Mono.just(false);
                    }
                    return organizationUserRepository.existsByOrganizationIdAndUserId(organizationId, userId);
                });
    }

    @Override
    public Mono<Boolean> canAddUserToSubdomainOrganization(String subdomain, UUID userId, UUID organizationId) {
        LOG.info("check if user {} can be added to organization {} in subdomain {}",
                userId, organizationId, subdomain);
        return validateSubdomainMembershipBoundary(new OrganizationUserBody(null, organizationId, userId, null,
                subdomain, true))
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> canAddUserToSubdomainOrganization(String subdomain, UUID organizationId) {
        LOG.info("check if organization {} can accept users from subdomain {}", organizationId, subdomain);
        return validateOrganizationMappedToSubdomain(subdomain, organizationId)
                .thenReturn(true);
    }

    @Override
    public Mono<List<Organization>> getOrganizationByIds(List<UUID> organizationIdList) {
        LOG.info("find organization by id list {}", organizationIdList);

        return organizationRepository.findByIdIn(organizationIdList).collectList();
    }

    @Override
    public Mono<Organization> createOrganization(Mono<OrganizationBody> organizationBodyMono) {
        LOG.info("create organization");

        return organizationBodyMono.flatMap(organizationBody ->
                organizationRepository.save(new Organization(null, organizationBody.getName(),
                        organizationBody.getCreatorUserId()))
                .doOnNext(organization -> LOG.info("saved organization {}", organization))
                .flatMap(organization->
                        Mono.just(new OrganizationUser
                                (null, organization.getId(), organization.getCreatorUserId(),
                                        null)).zipWith(Mono.just(organization)))
                .flatMap(objects -> organizationUserRepository.save(objects.getT1())
                        .doOnNext(organizationUser -> LOG.info("saved organizationUser {}", organizationUser))
                        .then(setDefaultOrganization(objects.getT2().getId(), objects.getT2().getCreatorUserId()))
                        .thenReturn(objects.getT2())));
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
        return subdomainOrganizationRepository.deleteByOrganizationId(organizationId).
                then(userDefaultOrganizationRepository.deleteByOrganizationId(organizationId)).
                then(organizationUserRepository.deleteByOrganizationId(organizationId)).
                then(organizationPositionRepository.deleteByOrganizationId(organizationId)).
                then(organizationRepository.deleteById(organizationId)).
                thenReturn("organization deleted");
    }

    @Override
    public Mono<String> addOrganizationToSubdomain(String subdomain, UUID organizationId) {
        String normalizedSubdomain = normalizeSubdomain(subdomain);
        LOG.info("add organization {} to normalized subdomain {}", organizationId, normalizedSubdomain);

        return organizationRepository.findById(organizationId)
                .switchIfEmpty(Mono.error(new OrgException("No organization found with id")))
                .then(getOrCreateSubdomain(normalizedSubdomain))
                .flatMap(subdomainEntity -> subdomainOrganizationRepository
                        .existsBySubdomainIdAndOrganizationId(subdomainEntity.getId(), organizationId)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.just("organization already exists for subdomain");
                            }
                            return subdomainOrganizationRepository.save(new SubdomainOrganization(null, subdomainEntity.getId(), organizationId))
                                    .thenReturn("organization added to subdomain");
                        }));
    }

    @Override
    public Mono<String> removeOrganizationFromSubdomain(String subdomain, UUID organizationId) {
        String normalizedSubdomain = normalizeSubdomain(subdomain);
        LOG.info("remove organization {} from normalized subdomain {}", organizationId, normalizedSubdomain);

        return subdomainRepository.findByHost(normalizedSubdomain)
                .switchIfEmpty(Mono.error(new OrgException("No subdomain found")))
                .flatMap(subdomainEntity -> subdomainOrganizationRepository
                        .deleteBySubdomainIdAndOrganizationId(subdomainEntity.getId(), organizationId))
                .thenReturn("organization removed from subdomain");
    }

    @Override
    public Mono<String> setDefaultOrganization(UUID organizationId, UUID userId) {
        LOG.info("set default organization {} for user {}", organizationId, userId);

        return organizationUserRepository.existsByOrganizationIdAndUserId(organizationId, userId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new OrgException("No organization user association found"));
                    }
                    return userDefaultOrganizationRepository.findById(userId)
                            .flatMap(existingDefault -> {
                                if (organizationId.equals(existingDefault.getOrganizationId())) {
                                    return Mono.just(existingDefault);
                                }
                                return userDefaultOrganizationRepository.save(new UserDefaultOrganization(
                                        userId, organizationId, existingDefault.getCreated(),
                                        java.time.LocalDateTime.now(), false));
                            })
                            .switchIfEmpty(userDefaultOrganizationRepository.save(
                                    new UserDefaultOrganization(userId, organizationId, true)))
                            .thenReturn("default organization updated");
                });
    }

    @Override
    public Mono<UUID> getDefaultOrganizationIdBySubdomainAndUserId(String subdomain, UUID userId) {
        String normalizedSubdomain = normalizeSubdomain(subdomain);
        LOG.info("get default organization id for subdomain {} and user {}", normalizedSubdomain, userId);

        return userDefaultOrganizationRepository.findById(userId)
                .flatMap(userDefaultOrganization -> subdomainRepository.findByHost(normalizedSubdomain)
                        .flatMap(subdomainEntity -> subdomainOrganizationRepository.existsBySubdomainIdAndOrganizationId(
                                subdomainEntity.getId(), userDefaultOrganization.getOrganizationId()))
                        .filter(Boolean::booleanValue)
                        .map(ignored -> userDefaultOrganization.getOrganizationId()));
    }

    @Override
    public Mono<UUID> getDefaultOrganizationIdForUser(UUID userId) {
        LOG.info("get default organization id for user {}", userId);

        return userDefaultOrganizationRepository.findById(userId)
                .map(UserDefaultOrganization::getOrganizationId);
    }

    @Override
    public Mono<String> removeUserFromOrganization(UUID userId, UUID organizationId) {
        LOG.info("delete user {} and organization {} association", userId, organizationId);

        return  userDefaultOrganizationRepository.deleteByUserIdAndOrganizationId(userId, organizationId)
                .then(organizationUserRepository.deleteByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn("deleted by organizationId and userId");
    }

    @Override
    public Mono<String> addUserToOrganization(OrganizationUserBody organizationUserBody) {
        LOG.info("create organization user");
        return organizationUserRepository.existsByOrganizationIdAndUserId(
                        organizationUserBody.getOrganizationId(), organizationUserBody.getUserId())
                .doOnNext(aBoolean -> LOG.info("exists by orgIdAndUserId already?: {}", aBoolean))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just("user already exists in organization");
                    }
                    return validateSubdomainMembershipBoundary(organizationUserBody)
                            .then(saveOrganizationUser(organizationUserBody));
                });

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
    public Mono<List<UUID>> getOrganizationIdsForUser(UUID userId) {
        LOG.info("get organization ids for user {}", userId);

        return organizationUserRepository.findByUserId(userId)
                .map(OrganizationUser::getOrganizationId)
                .distinct()
                .collectList();
    }

    @Override
    public Mono<Boolean> userExistsInOrganization(UUID organizationId, UUID userId) {
        LOG.info("check if user-id {} exists in organization-id {}", userId, organizationId);

        return organizationUserRepository.existsByOrganizationIdAndUserId(organizationId, userId);
    }

    @Override
    public Mono<String> deleteOrganizationForUser(UUID organizationId, UUID userId) {
        LOG.info("delete organization data for explicit userId: {}", userId);
        return userDefaultOrganizationRepository.deleteByUserIdAndOrganizationId(userId, organizationId)
                .then(organizationUserRepository.deleteByOrganizationIdAndUserId(organizationId, userId))
                .flatMap(integer -> organizationUserRepository.countByOrganizationId(organizationId))
                .flatMap(aLong -> {
                    if (aLong > 0) {
                        LOG.info("there are other users associated to this organization, don't delete the Organization.");
                        return Mono.just("organization user association deleted only");
                    } else {
                        LOG.info("there are no other user associated to this organization, DELETE the Organization too.");
                        return subdomainOrganizationRepository.deleteByOrganizationId(organizationId)
                                .then(userDefaultOrganizationRepository.deleteByOrganizationId(organizationId))
                                .then(organizationRepository.deleteById(organizationId))
                                .thenReturn("Organization and its user associated deleted");
                    }
                });
    }

    private Flux<Organization> findOrganizationsByMappedSubdomain(String subdomain) {
        return findOrganizationIdsByMappedSubdomain(subdomain)
                .concatMap(organizationRepository::findById);
    }

    private Flux<UUID> findOrganizationIdsByMappedSubdomain(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            return Flux.empty();
        }
        return subdomainRepository.findByHost(subdomain)
                .flatMapMany(subdomainEntity -> subdomainOrganizationRepository
                        .findBySubdomainIdOrderByCreatedAsc(subdomainEntity.getId()))
                .map(SubdomainOrganization::getOrganizationId);
    }

    private Mono<Subdomain> getOrCreateSubdomain(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            return Mono.error(new IllegalArgumentException("subdomain is required"));
        }
        return subdomainRepository.findByHost(subdomain)
                .switchIfEmpty(subdomainRepository.save(new Subdomain(null, subdomain)));
    }

    private Mono<Void> validateSubdomainMembershipBoundary(OrganizationUserBody organizationUserBody) {
        if (!organizationUserBody.isRestrictToSubdomain()) {
            return Mono.empty();
        }

        String normalizedSubdomain = normalizeSubdomain(organizationUserBody.getSubdomain());
        if (normalizedSubdomain == null || normalizedSubdomain.isEmpty()) {
            return Mono.error(new IllegalArgumentException("subdomain is required when restricting subdomain membership"));
        }

        return validateOrganizationMappedToSubdomain(normalizedSubdomain, organizationUserBody.getOrganizationId())
                .flatMap(subdomainEntity -> userHasMembershipInDifferentSubdomain(
                        organizationUserBody.getUserId(), subdomainEntity.getId()));
    }

    private Mono<Subdomain> validateOrganizationMappedToSubdomain(String subdomain, UUID organizationId) {
        String normalizedSubdomain = normalizeSubdomain(subdomain);
        if (normalizedSubdomain == null || normalizedSubdomain.isEmpty()) {
            return Mono.error(new IllegalArgumentException("subdomain is required"));
        }

        return subdomainRepository.findByHost(normalizedSubdomain)
                .switchIfEmpty(Mono.error(new OrgException("No subdomain found")))
                .flatMap(subdomainEntity -> subdomainOrganizationRepository
                        .existsBySubdomainIdAndOrganizationId(subdomainEntity.getId(), organizationId)
                        .flatMap(mappedToTargetSubdomain -> {
                            if (!mappedToTargetSubdomain) {
                                return Mono.error(new OrgException("organization is not mapped to subdomain"));
                            }
                            return Mono.just(subdomainEntity);
                        }));
    }

    private Mono<Void> userHasMembershipInDifferentSubdomain(UUID userId, UUID targetSubdomainId) {
        return organizationUserRepository.findByUserId(userId)
                .concatMap(organizationUser -> subdomainOrganizationRepository.findByOrganizationId(organizationUser.getOrganizationId()))
                .map(SubdomainOrganization::getSubdomainId)
                .any(subdomainId -> !subdomainId.equals(targetSubdomainId))
                .flatMap(hasDifferentSubdomainMembership -> {
                    if (hasDifferentSubdomainMembership) {
                        return Mono.error(new OrgException("user belongs to a different subdomain"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<String> saveOrganizationUser(OrganizationUserBody organizationUserBody) {
        OrganizationUser organizationUser = new OrganizationUser(
                null,
                organizationUserBody.getOrganizationId(),
                organizationUserBody.getUserId(),
                organizationUserBody.getPositionId());
        return organizationUserRepository.save(organizationUser)
                .thenReturn("added user to organization");
    }

    private String normalizeSubdomain(String subdomain) {
        return subdomain == null ? null : subdomain.trim().toLowerCase(Locale.ROOT);
    }

}
