package io.github.inductiveautomation.kindling.directory

import io.github.inductiveautomation.kindling.statistics.DirectoryConfigSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

class DirectoryViewerTest : FunSpec({
    test("acceptsDirectory returns true for directory with projects") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            // Create projects directory
            tempDir.resolve("projects").createDirectories()

            DirectoryViewer.acceptsDirectory(tempDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("acceptsDirectory returns true for directory with config") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            // Create config directory
            tempDir.resolve("config").createDirectories()

            DirectoryViewer.acceptsDirectory(tempDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("acceptsDirectory returns true for directory with idb") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            // Create idb file
            tempDir.resolve("db_backup_sqlite.idb").createFile()

            DirectoryViewer.acceptsDirectory(tempDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("acceptsDirectory returns false for empty directory") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            DirectoryViewer.acceptsDirectory(tempDir).shouldBeFalse()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("acceptsDirectory returns false for files") {
        val tempFile = Files.createTempFile("kindling-test", ".txt")
        try {
            DirectoryViewer.acceptsDirectory(tempFile).shouldBeFalse()
        } finally {
            Files.delete(tempFile)
        }
    }

    test("isValidConfigDirectory delegates to DirectoryConfigSource.isIgnitionConfigDirectory") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            // Create projects directory
            tempDir.resolve("projects").createDirectories()

            // Both should return the same result
            val acceptsResult = DirectoryViewer.acceptsDirectory(tempDir)
            val isValidResult = DirectoryViewer.isValidConfigDirectory(tempDir)

            acceptsResult.shouldBeTrue()
            isValidResult.shouldBeTrue()
            (acceptsResult == isValidResult).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})
