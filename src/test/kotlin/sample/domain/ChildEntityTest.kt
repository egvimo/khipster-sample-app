package sample.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sample.web.rest.equalsVerifier

class ChildEntityTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(ChildEntity::class)
        val childEntity1 = ChildEntity()
        childEntity1.id = 1L
        val childEntity2 = ChildEntity()
        childEntity2.id = childEntity1.id
        assertThat(childEntity1).isEqualTo(childEntity2)
        childEntity2.id = 2L
        assertThat(childEntity1).isNotEqualTo(childEntity2)
        childEntity1.id = null
        assertThat(childEntity1).isNotEqualTo(childEntity2)
    }
}
