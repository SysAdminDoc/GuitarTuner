package com.sysadmindoc.guitartuner.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreBoundaryTest {
    @Test
    fun corePackagesDoNotDependOnPhoneUi() {
        val sourceRoot = Path.of("src/main/java/com/sysadmindoc/guitartuner")
        val coreFiles = listOf("pitch", "tuning", "settings")
            .flatMap { packageName ->
                Files.walk(sourceRoot.resolve(packageName)).use { paths ->
                    paths
                        .filter { it.extension == "kt" }
                        .toList()
                }
            }

        val violations = coreFiles.flatMap { file ->
            val text = file.readText()
            ForbiddenImports
                .filter { forbidden -> text.contains(forbidden) }
                .map { forbidden -> "${sourceRoot.relativize(file)} imports $forbidden" }
        }

        assertTrue(violations.joinToString(separator = "\n"), violations.isEmpty())
    }

    private companion object {
        val ForbiddenImports = listOf(
            "androidx.activity",
            "androidx.compose",
            "androidx.lifecycle.compose",
            "com.sysadmindoc.guitartuner.ui",
        )
    }
}
