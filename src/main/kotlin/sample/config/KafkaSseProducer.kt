package sample.config

import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel

interface KafkaSseProducer {

    companion object {
        const val CHANNELNAME = "binding-out-sse"
    }

    @Output(CHANNELNAME)
    fun output(): MessageChannel
}
