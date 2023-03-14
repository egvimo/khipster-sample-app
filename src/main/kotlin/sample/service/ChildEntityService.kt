package sample.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sample.domain.ChildEntity
import sample.repository.ChildEntityRepository
import sample.service.dto.ChildEntityDTO
import sample.service.mapper.ChildEntityMapper
import java.util.Optional

/**
 * Service Implementation for managing [ChildEntity].
 */
@Service
@Transactional
class ChildEntityService(
    private val childEntityRepository: ChildEntityRepository,
    private val childEntityMapper: ChildEntityMapper,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a childEntity.
     *
     * @param childEntityDTO the entity to save.
     * @return the persisted entity.
     */
    fun save(childEntityDTO: ChildEntityDTO): ChildEntityDTO {
        log.debug("Request to save ChildEntity : $childEntityDTO")
        var childEntity = childEntityMapper.toEntity(childEntityDTO)
        childEntity = childEntityRepository.save(childEntity)
        return childEntityMapper.toDto(childEntity)
    }

    /**
     * Update a childEntity.
     *
     * @param childEntityDTO the entity to save.
     * @return the persisted entity.
     */
    fun update(childEntityDTO: ChildEntityDTO): ChildEntityDTO {
        log.debug("Request to update ChildEntity : {}", childEntityDTO)
        var childEntity = childEntityMapper.toEntity(childEntityDTO)
        childEntity = childEntityRepository.save(childEntity)
        return childEntityMapper.toDto(childEntity)
    }

    /**
     * Partially updates a childEntity.
     *
     * @param childEntityDTO the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(childEntityDTO: ChildEntityDTO): Optional<ChildEntityDTO> {
        log.debug("Request to partially update ChildEntity : {}", childEntityDTO)

        return childEntityRepository.findById(childEntityDTO.id)
            .map {
                childEntityMapper.partialUpdate(it, childEntityDTO)
                it
            }
            .map { childEntityRepository.save(it) }
            .map { childEntityMapper.toDto(it) }
    }

    /**
     * Get all the childEntities.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<ChildEntityDTO> {
        log.debug("Request to get all ChildEntities")
        return childEntityRepository.findAll()
            .mapTo(mutableListOf(), childEntityMapper::toDto)
    }

    /**
     * Get all the childEntities with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    fun findAllWithEagerRelationships(pageable: Pageable) =
        childEntityRepository.findAllWithEagerRelationships(pageable).map(childEntityMapper::toDto)

    /**
     * Get one childEntity by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<ChildEntityDTO> {
        log.debug("Request to get ChildEntity : $id")
        return childEntityRepository.findOneWithEagerRelationships(id)
            .map(childEntityMapper::toDto)
    }

    /**
     * Delete the childEntity by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long) {
        log.debug("Request to delete ChildEntity : $id")

        childEntityRepository.deleteById(id)
    }
}
