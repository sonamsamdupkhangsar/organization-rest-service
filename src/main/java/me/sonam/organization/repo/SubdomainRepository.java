package me.sonam.organization.repo;

import me.sonam.organization.repo.entity.Subdomain;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubdomainRepository extends ReactiveCrudRepository<Subdomain, UUID> {
    Mono<Subdomain> findByHost(String host);
}
