package io.github.inductiveautomation.kindling.statistics

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.writeText

class IgnitionConfigSourceTest : FunSpec({
    test("DirectoryConfigSource.isIgnitionConfigDirectory returns true for directory with projects") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            // Create projects directory
            (tempDir.resolve("projects")).createDirectories()
            
            DirectoryConfigSource.isIgnitionConfigDirectory(tempDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("DirectoryConfigSource.isIgnitionConfigDirectory returns true for directory with config") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            // Create config directory
            (tempDir.resolve("config")).createDirectories()
            
            DirectoryConfigSource.isIgnitionConfigDirectory(tempDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("DirectoryConfigSource.isIgnitionConfigDirectory returns true for directory with idb") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            // Create idb file
            (tempDir.resolve("db_backup_sqlite.idb")).createFile()
            
            DirectoryConfigSource.isIgnitionConfigDirectory(tempDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("DirectoryConfigSource.isIgnitionConfigDirectory returns false for empty directory") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            DirectoryConfigSource.isIgnitionConfigDirectory(tempDir).shouldBeFalse()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("DirectoryConfigSource.isIgnitionConfigDirectory returns false for files") {
        val tempFile = Files.createTempFile("kindling-test", ".txt")
        try {
            DirectoryConfigSource.isIgnitionConfigDirectory(tempFile).shouldBeFalse()
        } finally {
            Files.delete(tempFile)
        }
    }

    test("IgnitionConfigSource.from returns DirectoryConfigSource for directories") {
        val tempDir = Files.createTempDirectory("kindling-test")
        try {
            val source = IgnitionConfigSource.from(tempDir)
            (source is DirectoryConfigSource).shouldBeTrue()
            source.isZipBased.shouldBeFalse()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})
