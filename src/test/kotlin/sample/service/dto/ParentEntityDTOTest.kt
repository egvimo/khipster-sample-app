package sample.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sample.web.rest.equalsVerifier

class ParentEntityDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(ParentEntityDTO::class)
        val parentEntityDTO1 = ParentEntityDTO()
        parentEntityDTO1.id = 1L
        val parentEntityDTO2 = ParentEntityDTO()
        assertThat(parentEntityDTO1).isNotEqualTo(parentEntityDTO2)
        parentEntityDTO2.id = parentEntityDTO1.id
        assertThat(parentEntityDTO1).isEqualTo(parentEntityDTO2)
        parentEntityDTO2.id = 2L
        assertThat(parentEntityDTO1).isNotEqualTo(parentEntityDTO2)
        parentEntityDTO1.id = null
        assertThat(parentEntityDTO1).isNotEqualTo(parentEntityDTO2)
    }
}
