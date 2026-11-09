package me.sonam.organization.repo;

import me.sonam.organization.repo.entity.UserDefaultOrganization;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface UserDefaultOrganizationRepository extends ReactiveCrudRepository<UserDefaultOrganization, UUID> {
    Flux<UserDefaultOrganization> findByOrganizationId(UUID organizationId);

    Mono<Integer> deleteByOrganizationId(UUID organizationId);
    Mono<Integer> deleteByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
