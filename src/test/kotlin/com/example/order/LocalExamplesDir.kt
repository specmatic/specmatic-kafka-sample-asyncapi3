package com.example.order

import java.nio.file.Files
import java.nio.file.Path

private const val EXAMPLES_DIR = "EX_DIR"

object LocalExamplesDir {
    fun setup() {
        val sourceDir = Path.of("examples")
        val targetDir = Path.of("build/tmp/specmatic/local-examples")

        if (Files.exists(targetDir)) {
            targetDir.toFile().deleteRecursively()
        }

        sourceDir.toFile().copyRecursively(targetDir.toFile(), overwrite = true)

        var replacementCount = 0
        targetDir.toFile().walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val originalContent = file.readText()
                val updatedContent = originalContent.replace("http://host.docker.internal:8080", "http://localhost:8080")
                if (updatedContent != originalContent) {
                    file.writeText(updatedContent)
                    replacementCount++
                }
            }

        check(replacementCount > 0) {
            "Could not find any mock URL to rewrite under ${targetDir.toAbsolutePath()}"
        }

        System.setProperty(EXAMPLES_DIR, targetDir.toAbsolutePath().toString())
        println("Examples directory set to ${System.getProperty(EXAMPLES_DIR)} with $replacementCount URL replacements")
    }

    fun tearDown() {
        System.clearProperty(EXAMPLES_DIR)
    }
}
