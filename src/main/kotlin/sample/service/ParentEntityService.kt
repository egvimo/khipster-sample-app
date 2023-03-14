package sample.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sample.domain.ParentEntity
import sample.repository.ParentEntityRepository
import sample.service.dto.ParentEntityDTO
import sample.service.mapper.ParentEntityMapper
import java.util.Optional

/**
 * Service Implementation for managing [ParentEntity].
 */
@Service
@Transactional
class ParentEntityService(
    private val parentEntityRepository: ParentEntityRepository,
    private val parentEntityMapper: ParentEntityMapper,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a parentEntity.
     *
     * @param parentEntityDTO the entity to save.
     * @return the persisted entity.
     */
    fun save(parentEntityDTO: ParentEntityDTO): ParentEntityDTO {
        log.debug("Request to save ParentEntity : $parentEntityDTO")
        var parentEntity = parentEntityMapper.toEntity(parentEntityDTO)
        parentEntity = parentEntityRepository.save(parentEntity)
        return parentEntityMapper.toDto(parentEntity)
    }

    /**
     * Update a parentEntity.
     *
     * @param parentEntityDTO the entity to save.
     * @return the persisted entity.
     */
    fun update(parentEntityDTO: ParentEntityDTO): ParentEntityDTO {
        log.debug("Request to update ParentEntity : {}", parentEntityDTO)
        var parentEntity = parentEntityMapper.toEntity(parentEntityDTO)
        parentEntity = parentEntityRepository.save(parentEntity)
        return parentEntityMapper.toDto(parentEntity)
    }

    /**
     * Partially updates a parentEntity.
     *
     * @param parentEntityDTO the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(parentEntityDTO: ParentEntityDTO): Optional<ParentEntityDTO> {
        log.debug("Request to partially update ParentEntity : {}", parentEntityDTO)

        return parentEntityRepository.findById(parentEntityDTO.id)
            .map {
                parentEntityMapper.partialUpdate(it, parentEntityDTO)
                it
            }
            .map { parentEntityRepository.save(it) }
            .map { parentEntityMapper.toDto(it) }
    }

    /**
     * Get all the parentEntities.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<ParentEntityDTO> {
        log.debug("Request to get all ParentEntities")
        return parentEntityRepository.findAll()
            .mapTo(mutableListOf(), parentEntityMapper::toDto)
    }

    /**
     * Get one parentEntity by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<ParentEntityDTO> {
        log.debug("Request to get ParentEntity : $id")
        return parentEntityRepository.findById(id)
            .map(parentEntityMapper::toDto)
    }

    /**
     * Delete the parentEntity by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long) {
        log.debug("Request to delete ParentEntity : $id")

        parentEntityRepository.deleteById(id)
    }
}
