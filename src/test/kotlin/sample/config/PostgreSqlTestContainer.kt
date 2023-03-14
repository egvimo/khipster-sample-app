package sample.config

import org.slf4j.LoggerFactory
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import java.util.Collections

class PostgreSqlTestContainer : SqlTestContainer {

    private val log = LoggerFactory.getLogger(javaClass)

    private var postgreSQLContainer: PostgreSQLContainer<*>? = null

    override fun destroy() {
        if (null != postgreSQLContainer && postgreSQLContainer?.isRunning == true) {
            postgreSQLContainer?.stop()
        }
    }

    override fun afterPropertiesSet() {
        if (null == postgreSQLContainer) {
            postgreSQLContainer = PostgreSQLContainer("postgres:14.5")
                .withDatabaseName("sample")
                .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
                .withLogConsumer(Slf4jLogConsumer(log))
                .withReuse(true)
        }
        if (postgreSQLContainer?.isRunning != true) {
            postgreSQLContainer?.start()
        }
    }

    override fun getTestContainer(): JdbcDatabaseContainer<*> = postgreSQLContainer as JdbcDatabaseContainer<*>
}
