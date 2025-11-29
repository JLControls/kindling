package io.github.inductiveautomation.kindling.git

import com.formdev.flatlaf.extras.FlatSVGIcon
import io.github.inductiveautomation.kindling.core.DirectoryTool
import io.github.inductiveautomation.kindling.core.Tool
import io.github.inductiveautomation.kindling.core.ToolOpeningException
import io.github.inductiveautomation.kindling.core.ToolPanel
import io.github.inductiveautomation.kindling.utils.FileFilter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

/**
 * Tool for opening Git repositories containing Ignition configurations
 * and comparing branches.
 */
object GitBranchViewer : Tool, DirectoryTool {
    override val serialKey = "git-branch-viewer"
    override val title = "Git Branch Comparison"
    override val description = "Compare Ignition configurations across Git branches"
    override val icon = FlatSVGIcon("icons/bx-git-branch.svg")
    override val filter = FileFilter(description, listOf())

    override fun open(path: Path): ToolPanel {
        if (!isGitRepository(path)) {
            throw ToolOpeningException("Not a valid Git repository: $path")
        }
        return GitBranchView(path)
    }

    override fun acceptsDirectory(path: Path): Boolean = isGitRepository(path)

    /**
     * Check if a path is a Git repository.
     */
    fun isGitRepository(path: Path): Boolean {
        if (!path.isDirectory()) return false
        val gitDir = path.resolve(".git")
        return gitDir.exists() && gitDir.isDirectory()
    }

    /**
     * Open a Git repository from a path.
     */
    fun openRepository(path: Path): Repository {
        return FileRepositoryBuilder()
            .setGitDir(path.resolve(".git").toFile())
            .readEnvironment()
            .findGitDir()
            .build()
    }
}
