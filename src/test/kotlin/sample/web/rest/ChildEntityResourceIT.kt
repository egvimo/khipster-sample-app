package sample.web.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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
import sample.domain.ChildEntity
import sample.domain.ParentEntity
import sample.repository.ChildEntityRepository
import sample.service.ChildEntityService
import sample.service.mapper.ChildEntityMapper
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [ChildEntityResource] REST controller.
 */
@IntegrationTest
@Extensions(
    ExtendWith(MockitoExtension::class)
)
@AutoConfigureMockMvc
@WithMockUser
class ChildEntityResourceIT {
    @Autowired
    private lateinit var childEntityRepository: ChildEntityRepository

    @Mock
    private lateinit var childEntityRepositoryMock: ChildEntityRepository

    @Autowired
    private lateinit var childEntityMapper: ChildEntityMapper

    @Mock
    private lateinit var childEntityServiceMock: ChildEntityService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var restChildEntityMockMvc: MockMvc

    private lateinit var childEntity: ChildEntity

    @BeforeEach
    fun initTest() {
        childEntity = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createChildEntity() {
        val databaseSizeBeforeCreate = childEntityRepository.findAll().size
        // Create the ChildEntity
        val childEntityDTO = childEntityMapper.toDto(childEntity)
        restChildEntityMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(childEntityDTO))
        ).andExpect(status().isCreated)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeCreate + 1)
        val testChildEntity = childEntityList[childEntityList.size - 1]

        assertThat(testChildEntity.childField).isEqualTo(DEFAULT_CHILD_FIELD)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createChildEntityWithExistingId() {
        // Create the ChildEntity with an existing ID
        childEntity.id = 1L
        val childEntityDTO = childEntityMapper.toDto(childEntity)

        val databaseSizeBeforeCreate = childEntityRepository.findAll().size
        // An entity with an existing ID cannot be created, so this API call must fail
        restChildEntityMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(childEntityDTO))
        ).andExpect(status().isBadRequest)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllChildEntities() {
        // Initialize the database
        childEntityRepository.saveAndFlush(childEntity)

        // Get all the childEntityList
        restChildEntityMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(childEntity.id?.toInt())))
            .andExpect(jsonPath("$.[*].childField").value(hasItem(DEFAULT_CHILD_FIELD)))
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllChildEntitiesWithEagerRelationshipsIsEnabled() {
        `when`(childEntityServiceMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restChildEntityMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false"))
            .andExpect(status().isOk)

        verify(childEntityRepositoryMock, times(1)).findAll(any(Pageable::class.java))
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllChildEntitiesWithEagerRelationshipsIsNotEnabled() {
        `when`(childEntityServiceMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restChildEntityMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true"))
            .andExpect(status().isOk)

        verify(childEntityServiceMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getChildEntity() {
        // Initialize the database
        childEntityRepository.saveAndFlush(childEntity)

        val id = childEntity.id
        assertNotNull(id)

        // Get the childEntity
        restChildEntityMockMvc.perform(get(ENTITY_API_URL_ID, childEntity.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(childEntity.id?.toInt()))
            .andExpect(jsonPath("$.childField").value(DEFAULT_CHILD_FIELD))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingChildEntity() {
        // Get the childEntity
        restChildEntityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingChildEntity() {
        // Initialize the database
        childEntityRepository.saveAndFlush(childEntity)

        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size

        // Update the childEntity
        val updatedChildEntity = childEntityRepository.findById(childEntity.id).get()
        // Disconnect from session so that the updates on updatedChildEntity are not directly saved in db
        em.detach(updatedChildEntity)
        updatedChildEntity.childField = UPDATED_CHILD_FIELD
        val childEntityDTO = childEntityMapper.toDto(updatedChildEntity)

        restChildEntityMockMvc.perform(
            put(ENTITY_API_URL_ID, childEntityDTO.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(childEntityDTO))
        ).andExpect(status().isOk)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
        val testChildEntity = childEntityList[childEntityList.size - 1]
        assertThat(testChildEntity.childField).isEqualTo(UPDATED_CHILD_FIELD)
    }

    @Test
    @Transactional
    fun putNonExistingChildEntity() {
        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size
        childEntity.id = count.incrementAndGet()

        // Create the ChildEntity
        val childEntityDTO = childEntityMapper.toDto(childEntity)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChildEntityMockMvc.perform(
            put(ENTITY_API_URL_ID, childEntityDTO.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(childEntityDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchChildEntity() {
        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size
        childEntity.id = count.incrementAndGet()

        // Create the ChildEntity
        val childEntityDTO = childEntityMapper.toDto(childEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildEntityMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(childEntityDTO))
        ).andExpect(status().isBadRequest)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamChildEntity() {
        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size
        childEntity.id = count.incrementAndGet()

        // Create the ChildEntity
        val childEntityDTO = childEntityMapper.toDto(childEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildEntityMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(childEntityDTO))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateChildEntityWithPatch() {
        childEntityRepository.saveAndFlush(childEntity)

        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size

// Update the childEntity using partial update
        val partialUpdatedChildEntity = ChildEntity().apply {
            id = childEntity.id

            childField = UPDATED_CHILD_FIELD
        }

        restChildEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedChildEntity.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedChildEntity))
        )
            .andExpect(status().isOk)

// Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
        val testChildEntity = childEntityList.last()
        assertThat(testChildEntity.childField).isEqualTo(UPDATED_CHILD_FIELD)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateChildEntityWithPatch() {
        childEntityRepository.saveAndFlush(childEntity)

        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size

// Update the childEntity using partial update
        val partialUpdatedChildEntity = ChildEntity().apply {
            id = childEntity.id

            childField = UPDATED_CHILD_FIELD
        }

        restChildEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedChildEntity.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedChildEntity))
        )
            .andExpect(status().isOk)

// Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
        val testChildEntity = childEntityList.last()
        assertThat(testChildEntity.childField).isEqualTo(UPDATED_CHILD_FIELD)
    }

    @Throws(Exception::class)
    fun patchNonExistingChildEntity() {
        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size
        childEntity.id = count.incrementAndGet()

        // Create the ChildEntity
        val childEntityDTO = childEntityMapper.toDto(childEntity)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChildEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, childEntityDTO.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(childEntityDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchChildEntity() {
        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size
        childEntity.id = count.incrementAndGet()

        // Create the ChildEntity
        val childEntityDTO = childEntityMapper.toDto(childEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildEntityMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(childEntityDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamChildEntity() {
        val databaseSizeBeforeUpdate = childEntityRepository.findAll().size
        childEntity.id = count.incrementAndGet()

        // Create the ChildEntity
        val childEntityDTO = childEntityMapper.toDto(childEntity)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildEntityMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(childEntityDTO))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the ChildEntity in the database
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteChildEntity() {
        // Initialize the database
        childEntityRepository.saveAndFlush(childEntity)
        val databaseSizeBeforeDelete = childEntityRepository.findAll().size
        // Delete the childEntity
        restChildEntityMockMvc.perform(
            delete(ENTITY_API_URL_ID, childEntity.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val childEntityList = childEntityRepository.findAll()
        assertThat(childEntityList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_CHILD_FIELD = "AAAAAAAAAA"
        private const val UPDATED_CHILD_FIELD = "BBBBBBBBBB"

        private val ENTITY_API_URL: String = "/api/child-entities"
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
        fun createEntity(em: EntityManager): ChildEntity {
            val childEntity = ChildEntity(
                childField = DEFAULT_CHILD_FIELD

            )

            // Add required entity
            val user = UserResourceIT.createEntity(em)
            em.persist(user)
            em.flush()
            childEntity.user = user
            // Add required entity
            val parentEntity: ParentEntity
            if (findAll(em, ParentEntity::class).isEmpty()) {
                parentEntity = ParentEntityResourceIT.createEntity(em)
                em.persist(parentEntity)
                em.flush()
            } else {
                parentEntity = findAll(em, ParentEntity::class)[0]
            }
            childEntity.parent = parentEntity
            return childEntity
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): ChildEntity {
            val childEntity = ChildEntity(
                childField = UPDATED_CHILD_FIELD

            )

            // Add required entity
            val user = UserResourceIT.createEntity(em)
            em.persist(user)
            em.flush()
            childEntity.user = user
            // Add required entity
            val parentEntity: ParentEntity
            if (findAll(em, ParentEntity::class).isEmpty()) {
                parentEntity = ParentEntityResourceIT.createUpdatedEntity(em)
                em.persist(parentEntity)
                em.flush()
            } else {
                parentEntity = findAll(em, ParentEntity::class)[0]
            }
            childEntity.parent = parentEntity
            return childEntity
        }
    }
}
