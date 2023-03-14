package sample.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SyncTaskExecutor

@Configuration
class AsyncSyncConfiguration {
    @Bean(name = ["taskExecutor"])
    fun taskExecutor() = SyncTaskExecutor()
}
