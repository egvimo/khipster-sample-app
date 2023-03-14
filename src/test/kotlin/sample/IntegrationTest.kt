package sample

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import sample.config.AsyncSyncConfiguration
import sample.config.EmbeddedKafka
import sample.config.EmbeddedSQL
import sample.config.TestSecurityConfiguration

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [SampleApp::class, AsyncSyncConfiguration::class, TestSecurityConfiguration::class])
@EmbeddedKafka
@EmbeddedSQL
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
annotation class IntegrationTest
