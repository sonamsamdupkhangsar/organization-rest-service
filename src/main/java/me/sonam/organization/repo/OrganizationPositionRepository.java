package me.sonam.organization.repo;

import me.sonam.organization.repo.entity.OrganizationPosition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationPositionRepository extends ReactiveCrudRepository<OrganizationPosition, UUID> {
    Flux<OrganizationPosition> findAllBy(Pageable pageable);
    Mono<Void> deleteByIdAndOrganizationId(UUID id, UUID organizationId);
    Mono<Void> deleteByOrganizationId(UUID organizationId);
    Mono<Long> countByOrganizationId(UUID organizationId);
}
