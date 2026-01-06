package com.example.order

import io.specmatic.async.constants.SPECMATIC_OVERLAY_FILE
import io.specmatic.async.test.SpecmaticAsyncContractTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.EmbeddedKafkaZKBroker

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Fix flakiness and then enable")
class ContractTest : SpecmaticAsyncContractTest {
    private lateinit var embeddedKafka: EmbeddedKafkaBroker

    @BeforeAll
    fun setup() {
        embeddedKafka =
            EmbeddedKafkaZKBroker(
                1,
                false,
                "new-orders",
                "wip-orders",
                "to-be-cancelled-orders",
                "cancelled-orders",
                "accepted-orders",
                "out-for-delivery-orders"
            ).kafkaPorts(9092)
        runCatching { embeddedKafka.afterPropertiesSet() }
        System.setProperty(SPECMATIC_OVERLAY_FILE, "src/test/resources/spec_overlay.yaml")
        Thread.sleep(1000)
    }

    @AfterAll
    fun tearDown() {
        embeddedKafka.destroy()
        Thread.sleep(200)
    }
}
