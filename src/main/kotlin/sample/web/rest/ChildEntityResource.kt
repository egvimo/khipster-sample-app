package sample.web.rest

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import sample.repository.ChildEntityRepository
import sample.service.ChildEntityService
import sample.service.dto.ChildEntityDTO
import sample.web.rest.errors.BadRequestAlertException
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "sampleChildEntity"
/**
 * REST controller for managing [sample.domain.ChildEntity].
 */
@RestController
@RequestMapping("/api")
class ChildEntityResource(
    private val childEntityService: ChildEntityService,
    private val childEntityRepository: ChildEntityRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "sampleChildEntity"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /child-entities` : Create a new childEntity.
     *
     * @param childEntityDTO the childEntityDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new childEntityDTO, or with status `400 (Bad Request)` if the childEntity has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/child-entities")
    fun createChildEntity(@Valid @RequestBody childEntityDTO: ChildEntityDTO): ResponseEntity<ChildEntityDTO> {
        log.debug("REST request to save ChildEntity : $childEntityDTO")
        if (childEntityDTO.id != null) {
            throw BadRequestAlertException(
                "A new childEntity cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = childEntityService.save(childEntityDTO)
        return ResponseEntity.created(URI("/api/child-entities/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /child-entities/:id} : Updates an existing childEntity.
     *
     * @param id the id of the childEntityDTO to save.
     * @param childEntityDTO the childEntityDTO to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated childEntityDTO,
     * or with status `400 (Bad Request)` if the childEntityDTO is not valid,
     * or with status `500 (Internal Server Error)` if the childEntityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/child-entities/{id}")
    fun updateChildEntity(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody childEntityDTO: ChildEntityDTO
    ): ResponseEntity<ChildEntityDTO> {
        log.debug("REST request to update ChildEntity : {}, {}", id, childEntityDTO)
        if (childEntityDTO.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, childEntityDTO.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!childEntityRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = childEntityService.update(childEntityDTO)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                    childEntityDTO.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /child-entities/:id} : Partial updates given fields of an existing childEntity, field will ignore if it is null
     *
     * @param id the id of the childEntityDTO to save.
     * @param childEntityDTO the childEntityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated childEntityDTO,
     * or with status {@code 400 (Bad Request)} if the childEntityDTO is not valid,
     * or with status {@code 404 (Not Found)} if the childEntityDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the childEntityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/child-entities/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateChildEntity(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody childEntityDTO: ChildEntityDTO
    ): ResponseEntity<ChildEntityDTO> {
        log.debug("REST request to partial update ChildEntity partially : {}, {}", id, childEntityDTO)
        if (childEntityDTO.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, childEntityDTO.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!childEntityRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = childEntityService.partialUpdate(childEntityDTO)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, childEntityDTO.id.toString())
        )
    }

    /**
     * `GET  /child-entities` : get all the childEntities.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the [ResponseEntity] with status `200 (OK)` and the list of childEntities in body.
     */
    @GetMapping("/child-entities")
    fun getAllChildEntities(@RequestParam(required = false, defaultValue = "false") eagerload: Boolean): MutableList<ChildEntityDTO> {

        log.debug("REST request to get all ChildEntities")

        return childEntityService.findAll()
    }

    /**
     * `GET  /child-entities/:id` : get the "id" childEntity.
     *
     * @param id the id of the childEntityDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the childEntityDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/child-entities/{id}")
    fun getChildEntity(@PathVariable id: Long): ResponseEntity<ChildEntityDTO> {
        log.debug("REST request to get ChildEntity : $id")
        val childEntityDTO = childEntityService.findOne(id)
        return ResponseUtil.wrapOrNotFound(childEntityDTO)
    }
    /**
     *  `DELETE  /child-entities/:id` : delete the "id" childEntity.
     *
     * @param id the id of the childEntityDTO to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/child-entities/{id}")
    fun deleteChildEntity(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete ChildEntity : $id")

        childEntityService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
