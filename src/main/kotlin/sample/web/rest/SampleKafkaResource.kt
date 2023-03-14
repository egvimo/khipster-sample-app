package sample.web.rest

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.http.MediaType
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.GenericMessage
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import sample.config.KafkaSseConsumer
import sample.config.KafkaSseProducer
import java.io.IOException
import java.security.Principal
import java.util.Optional

@RestController
@RequestMapping("/api/sample-kafka")
class SampleKafkaResource(
    @Qualifier(KafkaSseProducer.CHANNELNAME) val output: MessageChannel
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private var emitters = mutableMapOf<String, SseEmitter>()

    @PostMapping("/publish")
    fun publish(@RequestParam message: String) {
        log.debug("REST request the message : {} to send to Kafka topic ", message)
        val map = mutableMapOf<String, Any>()
        map[MessageHeaders.CONTENT_TYPE] = MimeTypeUtils.TEXT_PLAIN_VALUE
        val headers = MessageHeaders(map)
        output.send(GenericMessage(message, headers))
    }

    @GetMapping("/register")
    fun register(principal: Principal): ResponseBodyEmitter {
        log.debug("Registering sse client for ${principal.name}")
        val emitter = SseEmitter()
        emitter.onCompletion { emitters.remove(principal.name, emitter) }
        emitters[principal.name] = emitter
        return emitter
    }

    @GetMapping("/unregister")
    fun unregister(principal: Principal) {
        val user = principal.name
        log.debug("Unregistering sse emitter for user: $user")
        Optional.ofNullable(emitters[user])
            .ifPresent(ResponseBodyEmitter::complete)
    }

    @StreamListener(value = KafkaSseConsumer.CHANNELNAME, copyHeaders = "false")
    fun consume(message: Message<String>) {
        log.debug("Got message from kafka stream: ${message.payload}")
        emitters.entries
            .map { it.value }
            .forEach {
                try {
                    it.send(event().data(message.payload, MediaType.TEXT_PLAIN))
                } catch (e: IOException) {
                    log.debug("error sending sse message ${message.payload}")
                }
            }
    }
}
