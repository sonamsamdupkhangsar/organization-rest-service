package me.sonam.organization.repo;

import me.sonam.organization.repo.entity.SubdomainOrganization;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubdomainOrganizationRepository extends ReactiveCrudRepository<SubdomainOrganization, UUID> {
    Flux<SubdomainOrganization> findBySubdomainIdOrderByCreatedAsc(UUID subdomainId);
    Flux<SubdomainOrganization> findByOrganizationId(UUID organizationId);
    Mono<Boolean> existsBySubdomainIdAndOrganizationId(UUID subdomainId, UUID organizationId);
    Mono<Integer> deleteBySubdomainIdAndOrganizationId(UUID subdomainId, UUID organizationId);
    Mono<Integer> deleteByOrganizationId(UUID organizationId);
}
