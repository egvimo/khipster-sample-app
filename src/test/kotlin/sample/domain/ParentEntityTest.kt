package sample.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sample.web.rest.equalsVerifier

class ParentEntityTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(ParentEntity::class)
        val parentEntity1 = ParentEntity()
        parentEntity1.id = 1L
        val parentEntity2 = ParentEntity()
        parentEntity2.id = parentEntity1.id
        assertThat(parentEntity1).isEqualTo(parentEntity2)
        parentEntity2.id = 2L
        assertThat(parentEntity1).isNotEqualTo(parentEntity2)
        parentEntity1.id = null
        assertThat(parentEntity1).isNotEqualTo(parentEntity2)
    }
}
