package me.sonam.organization.handler.service;

import me.sonam.organization.handler.OrgException;
import me.sonam.organization.handler.OrganizationBody;
import me.sonam.organization.handler.PositionManager;
import me.sonam.organization.repo.OrganizationPositionRepository;
import me.sonam.organization.repo.entity.Organization;
import me.sonam.organization.repo.entity.OrganizationPosition;
import me.sonam.organization.repo.entity.OrganizationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class PositionService implements PositionManager {
    private static final Logger LOG = LoggerFactory.getLogger(PositionService.class);

    @Autowired
    private OrganizationPositionRepository organizationPositionRepository;

    @Override
    public Mono<Page<OrganizationPosition>> getPositions(Pageable pageable) {
        LOG.info("get all organizations");

        return organizationPositionRepository.findAllBy(pageable).collectList()
                .zipWith(organizationPositionRepository.count())
                .map(objects -> new PageImpl<>(objects.getT1(), pageable, objects.getT2()));
    }



    @Override
    public Mono<OrganizationPosition> getPositionById(UUID positionId) {
        LOG.info("find organization by id");
        return organizationPositionRepository.findById(positionId).
                switchIfEmpty(Mono.error(new OrgException("No organization position found with id")));
    }
    @Override
    public Mono<UUID> deletePosition(UUID id, UUID organizationId) {
        LOG.info("delete position by id");

        return organizationPositionRepository.deleteByIdAndOrganizationId(id, organizationId).thenReturn(id);
    }

    @Override
    public Mono<String> createOrganizationPosition(Mono<Map> mapMono) {
        LOG.info("create organization position");

        return mapMono.map(map -> new OrganizationPosition(null, UUID.fromString(
                map.get("organizationId").toString()), map.get("name").toString()))
                .flatMap(organizationPosition -> organizationPositionRepository.save(organizationPosition))
                .map(organizationPosition -> organizationPosition.getId().toString());
    }

    @Override
    public Mono<String> updatePosition(Mono<Map> mapMono) {
        LOG.info("update organization position");

        return mapMono.map(map -> new OrganizationPosition(UUID.fromString(map.get("id").toString()),
                        UUID.fromString(
                        map.get("organizationId").toString()), map.get("name").toString()))
                .flatMap(organizationPosition -> organizationPositionRepository.save(organizationPosition))
                .thenReturn("organizationPosition updated");
    }
}
