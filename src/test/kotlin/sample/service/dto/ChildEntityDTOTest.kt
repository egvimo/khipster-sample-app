package sample.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sample.web.rest.equalsVerifier

class ChildEntityDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(ChildEntityDTO::class)
        val childEntityDTO1 = ChildEntityDTO()
        childEntityDTO1.id = 1L
        val childEntityDTO2 = ChildEntityDTO()
        assertThat(childEntityDTO1).isNotEqualTo(childEntityDTO2)
        childEntityDTO2.id = childEntityDTO1.id
        assertThat(childEntityDTO1).isEqualTo(childEntityDTO2)
        childEntityDTO2.id = 2L
        assertThat(childEntityDTO1).isNotEqualTo(childEntityDTO2)
        childEntityDTO1.id = null
        assertThat(childEntityDTO1).isNotEqualTo(childEntityDTO2)
    }
}
