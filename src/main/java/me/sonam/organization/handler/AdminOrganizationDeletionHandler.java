package me.sonam.organization.handler;

import me.sonam.organization.repo.OrganizationPositionRepository;
import me.sonam.organization.repo.OrganizationRepository;
import me.sonam.organization.repo.OrganizationUserRepository;
import me.sonam.organization.repo.SubdomainOrganizationRepository;
import me.sonam.organization.repo.UserDefaultOrganizationRepository;
import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationUser;
import me.sonam.organization.security.InternalServiceAuthorization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Internal organization inventory and guarded sole-owner cleanup; never deletes subdomains. */
@Service
public class AdminOrganizationDeletionHandler {
    private final OrganizationRepository organizationRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final OrganizationPositionRepository organizationPositionRepository;
    private final SubdomainOrganizationRepository subdomainOrganizationRepository;
    private final UserDefaultOrganizationRepository userDefaultOrganizationRepository;
    private final InternalServiceAuthorization internalServiceAuthorization;
    private final boolean protectionPolicyVerified;
    private final Set<UUID> protectedOrganizationIds;

    public AdminOrganizationDeletionHandler(
            OrganizationRepository organizationRepository,
            OrganizationUserRepository organizationUserRepository,
            OrganizationPositionRepository organizationPositionRepository,
            SubdomainOrganizationRepository subdomainOrganizationRepository,
            UserDefaultOrganizationRepository userDefaultOrganizationRepository,
            InternalServiceAuthorization internalServiceAuthorization,
            @Value("${account-deletion.protection-policy-configured:false}") boolean configured,
            @Value("${account-deletion.protected-organization-ids:UNCONFIGURED}") String protectedIds) {
        this.organizationRepository = organizationRepository;
        this.organizationUserRepository = organizationUserRepository;
        this.organizationPositionRepository = organizationPositionRepository;
        this.subdomainOrganizationRepository = subdomainOrganizationRepository;
        this.userDefaultOrganizationRepository = userDefaultOrganizationRepository;
        this.internalServiceAuthorization = internalServiceAuthorization;
        this.protectionPolicyVerified = configured && !"UNCONFIGURED".equals(protectedIds);
        this.protectedOrganizationIds = "UNCONFIGURED".equals(protectedIds) ? Set.of() : Arrays.stream(protectedIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Mono<ServerResponse> relatedOrganizations(ServerRequest request) {
        return internalServiceAuthorization.requireInternalServiceIdentity()
                .then(Mono.defer(() -> {
                    UUID userId = extractId(request, "userId");

                    // Combine organizations the user created, belongs to, or selected as their default.
                    return Flux.concat(
                                    organizationRepository.findByCreatorUserId(userId)
                                            .map(Organization::getId),
                                    organizationUserRepository.findByUserId(userId)
                                            .map(OrganizationUser::getOrganizationId),
                                    userDefaultOrganizationRepository.findById(userId)
                                            .map(value -> value.getOrganizationId()))
                            .collect(Collectors.toSet())
                            .flatMap(ids -> json(new RelatedOrganizations(userId, true, ids)));
                }));
    }

    public Mono<ServerResponse> inspectOrganization(ServerRequest request) {
        return internalServiceAuthorization.requireInternalServiceIdentity()
                .then(Mono.defer(() -> inspectOrganizationRelationships(extractId(request, "organizationId"))))
                .flatMap(this::json);
    }

    @Transactional
    public Mono<ServerResponse> deleteOrganization(ServerRequest request) {
        return internalServiceAuthorization.requireInternalServiceIdentity()
                .then(Mono.defer(() -> {
                    UUID userId = extractId(request, "userId");
                    UUID organizationId = extractId(request, "organizationId");

                    return inspectOrganizationRelationships(organizationId)
                            .flatMap(organizationState -> {
                                if (!organizationState.relationshipChecksPassed() || !organizationState.protectionPolicyVerified() || organizationState.protectedResource()
                                        || !userId.equals(organizationState.creatorUserId())
                                        || !organizationState.memberUserIds().equals(Set.of(userId))) {
                                    return Mono.error(new ResponseStatusException(
                                            HttpStatus.CONFLICT,
                                            "Organization protection, ownership, or membership prevents deletion"));
                                }

                                // Admin assignments are checked/removed by the role service before this stage.
                                return subdomainOrganizationRepository.deleteByOrganizationId(organizationId)
                                        .then(Mono.defer(() -> userDefaultOrganizationRepository.deleteByOrganizationId(organizationId)))
                                        .then(Mono.defer(() -> organizationUserRepository.deleteByOrganizationId(organizationId)))
                                        .then(Mono.defer(() -> organizationPositionRepository.deleteByOrganizationId(organizationId)))
                                        .then(Mono.defer(() -> organizationRepository.deleteById(organizationId)));
                            });
                }))
                .then(ServerResponse.noContent()
                .build());
    }

    /**
     * Reads the organization's creator, member user IDs, and deletion-protection settings.
     * Checks that default-organization selections belong to current members and that owned
     * positions are not referenced by memberships in other organizations. Marks the returned
     * state incomplete if either relationship check fails. Does not modify or delete data;
     * callers use this state together with other checks to decide whether deletion is allowed.
     * Returns HTTP 404 if the organization does not exist.
     */
    private Mono<OrganizationState> inspectOrganizationRelationships(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found")))
                .flatMap(organization -> Mono.zip(
                                // Collect all user IDs associated with this organization into a unique set.
                                organizationUserRepository.findByOrganizationId(organizationId)
                                        .map(OrganizationUser::getUserId)
                                        .collect(Collectors.toSet()),
                                // Multiple users can select the same organization as their default.
                                // Collect their IDs so valid member selections do not become inspection errors.
                                userDefaultOrganizationRepository.findByOrganizationId(organizationId)
                                        .map(selection -> selection.getUserId())
                                        .collect(Collectors.toSet()),
                                // Check positions one at a time, waiting for each membership lookup to finish.
                                // Every membership using an owned position must belong to this organization;
                                // the final all() returns false if any position has a cross-organization reference.
                                organizationPositionRepository.findByOrganizationId(organizationId)
                                        .concatMap(position -> organizationUserRepository.findByPositionId(position.getId())
                                                .all(member -> organizationId.equals(member.getOrganizationId())))
                                        .all(Boolean.TRUE::equals))
                        // Transform the combined lookup results into a read-only OrganizationState snapshot.
                        // Default selections must belong to current members. Shared membership is valid
                        // inventory; the coordinator reports it as a deletion blocker separately.
                        // T1: unique member user IDs; T2: users selecting this organization as their default;
                        // T3: no owned position is referenced by a membership in another organization.
                        .map(relationships -> new OrganizationState(
                                organizationId,
                                organization.getCreatorUserId(),
                                relationships.getT1().containsAll(relationships.getT2()) && relationships.getT3(),
                                protectionPolicyVerified,
                                protectedOrganizationIds.contains(organizationId),
                                relationships.getT1())));
    }

    private UUID extractId(ServerRequest request, String variable) {
        try {
            return UUID.fromString(request.pathVariable(variable));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid identifier", exception);
        }
    }

    private Mono<ServerResponse> json(Object body) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    public record RelatedOrganizations(UUID userId, boolean organizationLookupCompleted, Set<UUID> organizationIds) {
    }

    /**
     * @param organizationId organization being inspected
     * @param creatorUserId user who originally created the organization
     * @param relationshipChecksPassed true when every default selection belongs to the creator
     *                                 and no owned position is referenced by another organization's membership;
     *                                 this alone does not mean deletion is allowed
     * @param protectionPolicyVerified true when the protected-organization policy was explicitly configured
     * @param protectedResource true when this organization is protected from account-deletion cleanup
     * @param memberUserIds unique IDs of all current organization members, including the requester if a member
     */
    public record OrganizationState(
            UUID organizationId,
            UUID creatorUserId,
            boolean relationshipChecksPassed,
            boolean protectionPolicyVerified,
            boolean protectedResource,
            Set<UUID> memberUserIds) {
    }
}
