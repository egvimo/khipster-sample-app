package sample.web.rest

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import sample.repository.ParentEntityRepository
import sample.service.ParentEntityService
import sample.service.dto.ParentEntityDTO
import sample.web.rest.errors.BadRequestAlertException
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "sampleParentEntity"
/**
 * REST controller for managing [sample.domain.ParentEntity].
 */
@RestController
@RequestMapping("/api")
class ParentEntityResource(
    private val parentEntityService: ParentEntityService,
    private val parentEntityRepository: ParentEntityRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "sampleParentEntity"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /parent-entities` : Create a new parentEntity.
     *
     * @param parentEntityDTO the parentEntityDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new parentEntityDTO, or with status `400 (Bad Request)` if the parentEntity has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/parent-entities")
    fun createParentEntity(@Valid @RequestBody parentEntityDTO: ParentEntityDTO): ResponseEntity<ParentEntityDTO> {
        log.debug("REST request to save ParentEntity : $parentEntityDTO")
        if (parentEntityDTO.id != null) {
            throw BadRequestAlertException(
                "A new parentEntity cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = parentEntityService.save(parentEntityDTO)
        return ResponseEntity.created(URI("/api/parent-entities/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /parent-entities/:id} : Updates an existing parentEntity.
     *
     * @param id the id of the parentEntityDTO to save.
     * @param parentEntityDTO the parentEntityDTO to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated parentEntityDTO,
     * or with status `400 (Bad Request)` if the parentEntityDTO is not valid,
     * or with status `500 (Internal Server Error)` if the parentEntityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/parent-entities/{id}")
    fun updateParentEntity(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody parentEntityDTO: ParentEntityDTO
    ): ResponseEntity<ParentEntityDTO> {
        log.debug("REST request to update ParentEntity : {}, {}", id, parentEntityDTO)
        if (parentEntityDTO.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, parentEntityDTO.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!parentEntityRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = parentEntityService.update(parentEntityDTO)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                    parentEntityDTO.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /parent-entities/:id} : Partial updates given fields of an existing parentEntity, field will ignore if it is null
     *
     * @param id the id of the parentEntityDTO to save.
     * @param parentEntityDTO the parentEntityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated parentEntityDTO,
     * or with status {@code 400 (Bad Request)} if the parentEntityDTO is not valid,
     * or with status {@code 404 (Not Found)} if the parentEntityDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the parentEntityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/parent-entities/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateParentEntity(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody parentEntityDTO: ParentEntityDTO
    ): ResponseEntity<ParentEntityDTO> {
        log.debug("REST request to partial update ParentEntity partially : {}, {}", id, parentEntityDTO)
        if (parentEntityDTO.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, parentEntityDTO.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!parentEntityRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = parentEntityService.partialUpdate(parentEntityDTO)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, parentEntityDTO.id.toString())
        )
    }

    /**
     * `GET  /parent-entities` : get all the parentEntities.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of parentEntities in body.
     */
    @GetMapping("/parent-entities")
    fun getAllParentEntities(): MutableList<ParentEntityDTO> {

        log.debug("REST request to get all ParentEntities")

        return parentEntityService.findAll()
    }

    /**
     * `GET  /parent-entities/:id` : get the "id" parentEntity.
     *
     * @param id the id of the parentEntityDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the parentEntityDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/parent-entities/{id}")
    fun getParentEntity(@PathVariable id: Long): ResponseEntity<ParentEntityDTO> {
        log.debug("REST request to get ParentEntity : $id")
        val parentEntityDTO = parentEntityService.findOne(id)
        return ResponseUtil.wrapOrNotFound(parentEntityDTO)
    }
    /**
     *  `DELETE  /parent-entities/:id` : delete the "id" parentEntity.
     *
     * @param id the id of the parentEntityDTO to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/parent-entities/{id}")
    fun deleteParentEntity(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete ParentEntity : $id")

        parentEntityService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
