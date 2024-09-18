package me.sonam.organization.repo;

import me.sonam.organization.repo.entity.Organization;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationRepository extends ReactiveCrudRepository<Organization, UUID> {
    Flux<Organization> findAllBy(Pageable pageable);
    Flux<Organization> findByCreatorUserId(UUID ownerId, Pageable pageable);
    Mono<Long> countByCreatorUserId(UUID ownerId);
    Mono<Integer> deleteByCreatorUserId(UUID userId);
    Flux<Organization> findByCreatorUserId(UUID ownerId);
}
