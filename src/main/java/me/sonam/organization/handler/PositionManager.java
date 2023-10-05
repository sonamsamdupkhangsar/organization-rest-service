package me.sonam.organization.handler;

import me.sonam.organization.repo.entity.OrganizationPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public interface PositionManager {
    Mono<Page<OrganizationPosition>> getPositions(Pageable pageable);
    Mono<UUID> deletePosition(UUID id, UUID organizationId);
    Mono<OrganizationPosition> getPositionById(UUID positionId);

    Mono<String> createOrganizationPosition(Mono<Map> map);
    Mono<String> updatePosition(Mono<Map> map);

}
