package com.example.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EmbeddedKafka(
    ports = [9092],
    topics = [
        "new-orders",
        "wip-orders",
        "to-be-cancelled-orders",
        "cancelled-orders",
        "accepted-orders",
        "out-for-delivery-orders"
    ],
    brokerProperties = [
        "listeners=PLAINTEXT://0.0.0.0:9092",
        "advertised.listeners=PLAINTEXT://localhost:9092"
    ],
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
class ContractTestUsingTestContainer {

    companion object {
        private const val LOCAL_EXAMPLES_DIR = "build/tmp/specmatic/local-examples"
        private val isCI = System.getenv("CI") == "true"

        @JvmStatic
        fun isNonCIOrLinux(): Boolean =
            !isCI || System.getProperty("os.name").lowercase().contains("linux")

        @JvmStatic
        @BeforeAll
        fun prepareLocalExamples() {
            val localExamplesDir = File(LOCAL_EXAMPLES_DIR)
            localExamplesDir.deleteRecursively()
            localExamplesDir.mkdirs()

            File("examples").copyRecursively(localExamplesDir, overwrite = true)

            // On CI (Linux), replace host.docker.internal with localhost since we use --network=host
            if (isCI) {
                localExamplesDir.walkTopDown().filter { it.isFile && it.extension == "json" }.forEach { file ->
                    val content = file.readText()
                    val updatedContent = content.replace("host.docker.internal", "localhost")
                    file.writeText(updatedContent)
                }
            }
        }
    }



    private val testContainer: GenericContainer<*> =
        GenericContainer("specmatic/enterprise")
            .withCommand("test")
            .withFileSystemBind("specmatic.yaml", "/usr/src/app/specmatic.yaml", BindMode.READ_ONLY)
            .withFileSystemBind("specs", "/usr/src/app/specs", BindMode.READ_ONLY)
            .withFileSystemBind(LOCAL_EXAMPLES_DIR, "/usr/src/app/$LOCAL_EXAMPLES_DIR", BindMode.READ_ONLY)
            .withFileSystemBind("build/reports/specmatic", "/usr/src/app/build/reports/specmatic", BindMode.READ_WRITE)
            .withEnv("EX_DIR", LOCAL_EXAMPLES_DIR)
            .waitingFor(Wait.forLogMessage(".*Failed:.*", 1))
            .withNetworkMode("host")
            .withLogConsumer { print(it.utf8String) }

    @Container
    private val mockContainer: GenericContainer<*> =
        GenericContainer("specmatic/enterprise")
            .withCommand("mock")
            .withFileSystemBind("specmatic.yaml", "/usr/src/app/specmatic.yaml", BindMode.READ_ONLY)
            .withFileSystemBind("specs", "/usr/src/app/specs", BindMode.READ_ONLY)
            .withFileSystemBind(LOCAL_EXAMPLES_DIR, "/usr/src/app/$LOCAL_EXAMPLES_DIR", BindMode.READ_ONLY)
            .withFileSystemBind("build/reports/specmatic/stub", "/usr/src/app/build/reports/specmatic/stub", BindMode.READ_WRITE)
            .withEnv("EX_DIR", LOCAL_EXAMPLES_DIR)
            .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
            .withNetworkMode("host")
            .withLogConsumer { print(it.utf8String) }


    @Test
    fun specmaticContractTest() {
        testContainer.start()
        assertThat(testContainer.logs).contains("Failed: 0").doesNotContain("Passed: 0")
    }
}
