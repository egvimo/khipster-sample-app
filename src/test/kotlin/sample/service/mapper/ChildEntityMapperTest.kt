package sample.service.mapper

import org.junit.jupiter.api.BeforeEach

class ChildEntityMapperTest {

    private lateinit var childEntityMapper: ChildEntityMapper

    @BeforeEach
    fun setUp() {
        childEntityMapper = ChildEntityMapperImpl()
    }
}
