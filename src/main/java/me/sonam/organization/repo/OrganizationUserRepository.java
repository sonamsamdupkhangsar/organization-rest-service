package me.sonam.organization.repo;

import me.sonam.organization.repo.entity.OrganizationUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationUserRepository extends ReactiveCrudRepository<OrganizationUser, UUID> {
    Flux<OrganizationUser> findByOrganizationId(UUID organizationId, Pageable pageable);
    Mono<Integer> deleteByOrganizationId(UUID organizationId);
    Mono<Boolean> existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    Mono<OrganizationUser> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    Mono<Integer> deleteByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    Mono<Long> countByOrganizationId(UUID organizationId);
}
