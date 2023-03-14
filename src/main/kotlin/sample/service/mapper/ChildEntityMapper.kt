package sample.service.mapper

import org.mapstruct.*
import sample.domain.ChildEntity
import sample.domain.ParentEntity
import sample.domain.User
import sample.service.dto.ChildEntityDTO
import sample.service.dto.ParentEntityDTO
import sample.service.dto.UserDTO

/**
 * Mapper for the entity [ChildEntity] and its DTO [ChildEntityDTO].
 */
@Mapper(componentModel = "spring")
interface ChildEntityMapper :
    EntityMapper<ChildEntityDTO, ChildEntity> {

    @Mappings(
        Mapping(target = "user", source = "user", qualifiedByName = ["userLogin"]), Mapping(target = "parent", source = "parent", qualifiedByName = ["parentEntityParentRequiredField"])
    )
    override fun toDto(s: ChildEntity): ChildEntityDTO

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)

    @Mappings(
        Mapping(target = "id", source = "id"), Mapping(target = "login", source = "login")
    )
    fun toDtoUserLogin(user: User): UserDTO

    @Named("parentEntityParentRequiredField")
    @BeanMapping(ignoreByDefault = true)

    @Mappings(
        Mapping(target = "id", source = "id"), Mapping(target = "login", source = "login"), Mapping(target = "parentRequiredField", source = "parentRequiredField")
    )
    fun toDtoParentEntityParentRequiredField(parentEntity: ParentEntity): ParentEntityDTO
}
