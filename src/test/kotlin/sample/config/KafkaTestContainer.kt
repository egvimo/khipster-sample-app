package sample.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.utility.DockerImageName

class KafkaTestContainer : InitializingBean, DisposableBean {

    companion object {
        private val log = LoggerFactory.getLogger(KafkaTestContainer::class.java)

        @JvmStatic
        private var kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
            .withLogConsumer(Slf4jLogConsumer(log))
            .withReuse(true)
    }

    override fun destroy() {
        if (null != kafkaContainer && kafkaContainer.isRunning) {
            kafkaContainer.close()
        }
    }

    override fun afterPropertiesSet() {
        if (!kafkaContainer.isRunning) {
            kafkaContainer.start()
        }
    }

    fun getKafkaContainer() = kafkaContainer
}
