package sample.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.messaging.MessageChannel

interface KafkaSseConsumer {

    companion object {
        const val CHANNELNAME = "binding-in-sse"
    }

    @Input(CHANNELNAME)
    fun input(): MessageChannel
}
