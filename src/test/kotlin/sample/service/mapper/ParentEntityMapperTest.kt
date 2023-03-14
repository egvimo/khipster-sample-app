package sample.service.mapper

import org.junit.jupiter.api.BeforeEach

class ParentEntityMapperTest {

    private lateinit var parentEntityMapper: ParentEntityMapper

    @BeforeEach
    fun setUp() {
        parentEntityMapper = ParentEntityMapperImpl()
    }
}
