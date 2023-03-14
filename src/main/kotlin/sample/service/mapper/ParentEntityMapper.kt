package sample.service.mapper

import org.mapstruct.*
import sample.domain.ParentEntity
import sample.service.dto.ParentEntityDTO

/**
 * Mapper for the entity [ParentEntity] and its DTO [ParentEntityDTO].
 */
@Mapper(componentModel = "spring")
interface ParentEntityMapper :
    EntityMapper<ParentEntityDTO, ParentEntity>
