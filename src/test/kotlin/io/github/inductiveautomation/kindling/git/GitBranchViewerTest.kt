package io.github.inductiveautomation.kindling.git

import io.github.inductiveautomation.kindling.core.ToolOpeningException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

class GitBranchViewerTest : FunSpec({
    test("isGitRepository returns true for directory with .git folder") {
        val tempDir = Files.createTempDirectory("kindling-git-test")
        try {
            // Create .git directory
            tempDir.resolve(".git").createDirectories()

            GitBranchViewer.isGitRepository(tempDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("isGitRepository returns false for directory without .git folder") {
        val tempDir = Files.createTempDirectory("kindling-git-test")
        try {
            GitBranchViewer.isGitRepository(tempDir).shouldBeFalse()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("isGitRepository returns false for file path") {
        val tempFile = Files.createTempFile("kindling-git-test", ".txt")
        try {
            GitBranchViewer.isGitRepository(tempFile).shouldBeFalse()
        } finally {
            Files.delete(tempFile)
        }
    }

    test("isGitRepository returns false when .git is a file not a directory") {
        val tempDir = Files.createTempDirectory("kindling-git-test")
        try {
            // Create .git as a file (not a directory)
            tempDir.resolve(".git").createFile()

            GitBranchViewer.isGitRepository(tempDir).shouldBeFalse()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("acceptsDirectory delegates to isGitRepository") {
        val tempDir = Files.createTempDirectory("kindling-git-test")
        try {
            tempDir.resolve(".git").createDirectories()

            val isGitRepo = GitBranchViewer.isGitRepository(tempDir)
            val acceptsDir = GitBranchViewer.acceptsDirectory(tempDir)

            isGitRepo.shouldBeTrue()
            acceptsDir.shouldBeTrue()
            (isGitRepo == acceptsDir).shouldBeTrue()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("open throws ToolOpeningException for non-git directory") {
        val tempDir = Files.createTempDirectory("kindling-git-test")
        try {
            shouldThrow<ToolOpeningException> {
                GitBranchViewer.open(tempDir)
            }
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})
