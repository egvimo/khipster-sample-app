package sample.web.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator
import sample.IntegrationTest
import sample.domain.ParentEntity
import sample.repository.ParentEntityRepository
import sample.service.mapper.ParentEntityMapper
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [ParentEntityResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ParentEntityResourceIT {
    @Autowired
    private lateinit var parentEntityRepository: ParentEntityRepository

    @Autowired
    private lateinit var parentEntityMapper: ParentEntityMapper

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var restParentEntityMockMvc: MockMvc

    private lateinit var parentEntity: ParentEntity

    @BeforeEach
    fun initTest() {
        parentEntity = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createParentEntity() {
        val databaseSizeBeforeCreate = parentEntityRepository.findAll().size
        // Create the ParentEntity
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)
        restParentEntityMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(parentEntityDTO))
        ).andExpect(status().isCreated)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeCreate + 1)
        val testParentEntity = parentEntityList[parentEntityList.size - 1]

        assertThat(testParentEntity.parentRequiredField).isEqualTo(DEFAULT_PARENT_REQUIRED_FIELD)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createParentEntityWithExistingId() {
        // Create the ParentEntity with an existing ID
        parentEntity.id = 1L
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        val databaseSizeBeforeCreate = parentEntityRepository.findAll().size
        // An entity with an existing ID cannot be created, so this API call must fail
        restParentEntityMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(parentEntityDTO))
        ).andExpect(status().isBadRequest)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkParentRequiredFieldIsRequired() {
        val databaseSizeBeforeTest = parentEntityRepository.findAll().size
        // set the field null
        parentEntity.parentRequiredField = null

        // Create the ParentEntity, which fails.
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        restParentEntityMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(parentEntityDTO))
        ).andExpect(status().isBadRequest)

        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllParentEntities() {
        // Initialize the database
        parentEntityRepository.saveAndFlush(parentEntity)

        // Get all the parentEntityList
        restParentEntityMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(parentEntity.id?.toInt())))
            .andExpect(jsonPath("$.[*].parentRequiredField").value(hasItem(DEFAULT_PARENT_REQUIRED_FIELD)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getParentEntity() {
        // Initialize the database
        parentEntityRepository.saveAndFlush(parentEntity)

        val id = parentEntity.id
        assertNotNull(id)

        // Get the parentEntity
        restParentEntityMockMvc.perform(get(ENTITY_API_URL_ID, parentEntity.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(parentEntity.id?.toInt()))
            .andExpect(jsonPath("$.parentRequiredField").value(DEFAULT_PARENT_REQUIRED_FIELD))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingParentEntity() {
        // Get the parentEntity
        restParentEntityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingParentEntity() {
        // Initialize the database
        parentEntityRepository.saveAndFlush(parentEntity)

        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size

        // Update the parentEntity
        val updatedParentEntity = parentEntityRepository.findById(parentEntity.id).get()
        // Disconnect from session so that the updates on updatedParentEntity are not directly saved in db
        em.detach(updatedParentEntity)
        updatedParentEntity.parentRequiredField = UPDATED_PARENT_REQUIRED_FIELD
        val parentEntityDTO = parentEntityMapper.toDto(updatedParentEntity)

        restParentEntityMockMvc.perform(
            put(ENTITY_API_URL_ID, parentEntityDTO.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(parentEntityDTO))
        ).andExpect(status().isOk)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
        val testParentEntity = parentEntityList[parentEntityList.size - 1]
        assertThat(testParentEntity.parentRequiredField).isEqualTo(UPDATED_PARENT_REQUIRED_FIELD)
    }

    @Test
    @Transactional
    fun putNonExistingParentEntity() {
        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size
        parentEntity.id = count.incrementAndGet()

        // Create the ParentEntity
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restParentEntityMockMvc.perform(
            put(ENTITY_API_URL_ID, parentEntityDTO.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(parentEntityDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchParentEntity() {
        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size
        parentEntity.id = count.incrementAndGet()

        // Create the ParentEntity
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentEntityMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(parentEntityDTO))
        ).andExpect(status().isBadRequest)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamParentEntity() {
        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size
        parentEntity.id = count.incrementAndGet()

        // Create the ParentEntity
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentEntityMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(parentEntityDTO))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateParentEntityWithPatch() {
        parentEntityRepository.saveAndFlush(parentEntity)

        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size

// Update the parentEntity using partial update
        val partialUpdatedParentEntity = ParentEntity().apply {
            id = parentEntity.id
        }

        restParentEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedParentEntity.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedParentEntity))
        )
            .andExpect(status().isOk)

// Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
        val testParentEntity = parentEntityList.last()
        assertThat(testParentEntity.parentRequiredField).isEqualTo(DEFAULT_PARENT_REQUIRED_FIELD)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateParentEntityWithPatch() {
        parentEntityRepository.saveAndFlush(parentEntity)

        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size

// Update the parentEntity using partial update
        val partialUpdatedParentEntity = ParentEntity().apply {
            id = parentEntity.id

            parentRequiredField = UPDATED_PARENT_REQUIRED_FIELD
        }

        restParentEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedParentEntity.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedParentEntity))
        )
            .andExpect(status().isOk)

// Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
        val testParentEntity = parentEntityList.last()
        assertThat(testParentEntity.parentRequiredField).isEqualTo(UPDATED_PARENT_REQUIRED_FIELD)
    }

    @Throws(Exception::class)
    fun patchNonExistingParentEntity() {
        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size
        parentEntity.id = count.incrementAndGet()

        // Create the ParentEntity
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restParentEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, parentEntityDTO.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(parentEntityDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchParentEntity() {
        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size
        parentEntity.id = count.incrementAndGet()

        // Create the ParentEntity
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(parentEntityDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamParentEntity() {
        val databaseSizeBeforeUpdate = parentEntityRepository.findAll().size
        parentEntity.id = count.incrementAndGet()

        // Create the ParentEntity
        val parentEntityDTO = parentEntityMapper.toDto(parentEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentEntityMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(parentEntityDTO))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the ParentEntity in the database
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteParentEntity() {
        // Initialize the database
        parentEntityRepository.saveAndFlush(parentEntity)
        val databaseSizeBeforeDelete = parentEntityRepository.findAll().size
        // Delete the parentEntity
        restParentEntityMockMvc.perform(
            delete(ENTITY_API_URL_ID, parentEntity.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val parentEntityList = parentEntityRepository.findAll()
        assertThat(parentEntityList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_PARENT_REQUIRED_FIELD = "AAAAAAAAAA"
        private const val UPDATED_PARENT_REQUIRED_FIELD = "BBBBBBBBBB"

        private val ENTITY_API_URL: String = "/api/parent-entities"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + (2 * Integer.MAX_VALUE))

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): ParentEntity {
            val parentEntity = ParentEntity(
                parentRequiredField = DEFAULT_PARENT_REQUIRED_FIELD

            )

            return parentEntity
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): ParentEntity {
            val parentEntity = ParentEntity(
                parentRequiredField = UPDATED_PARENT_REQUIRED_FIELD

            )

            return parentEntity
        }
    }
}
