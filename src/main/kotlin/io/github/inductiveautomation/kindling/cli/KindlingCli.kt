package io.github.inductiveautomation.kindling.cli

import io.github.inductiveautomation.kindling.directory.DirectoryViewer
import io.github.inductiveautomation.kindling.git.GitBranchViewer
import io.github.inductiveautomation.kindling.statistics.DirectoryConfigSource
import io.github.inductiveautomation.kindling.statistics.GatewayBackup
import io.github.inductiveautomation.kindling.statistics.categories.DatabaseStatistics
import io.github.inductiveautomation.kindling.statistics.categories.DeviceStatistics
import io.github.inductiveautomation.kindling.statistics.categories.GatewayNetworkStatistics
import io.github.inductiveautomation.kindling.statistics.categories.MetaStatistics
import io.github.inductiveautomation.kindling.statistics.categories.OpcServerStatistics
import io.github.inductiveautomation.kindling.statistics.categories.ProjectStatistics
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.system.exitProcess

/**
 * CLI entry point for Kindling.
 * Provides command-line access to major features for LLM and automation usage.
 */
object KindlingCli {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            printUsage()
            exitProcess(1)
        }

        when (val command = args[0]) {
            "analyze-directory", "analyze-dir" -> analyzeDirectory(args.drop(1))
            "compare-branches", "compare" -> compareBranches(args.drop(1))
            "analyze" -> analyzeFile(args.drop(1))
            "backup-stats" -> backupStats(args.drop(1))
            "help", "--help", "-h" -> printUsage()
            "version", "--version", "-v" -> printVersion()
            else -> {
                System.err.println("Unknown command: $command")
                printUsage()
                exitProcess(1)
            }
        }
    }

    private fun printUsage() {
        println(
            """
            Kindling CLI - Command-line interface for Ignition configuration analysis
            
            Usage: kindling <command> [options]
            
            Commands:
              analyze-directory <path>           Analyze Ignition configuration directory
              compare-branches <repo-path>       Compare Git branches
              analyze <file-path>                Analyze a file (IDB, GWBK, etc.)
              backup-stats <path>                Get gateway backup/config statistics
              help                               Show this help message
              version                            Show version information
            
            Options for analyze-directory:
              <path>                             Path to Ignition configuration directory
            
            Options for compare-branches:
              <repo-path>                        Path to Git repository
              --base <branch>                    Base branch for comparison (default: main/master)
              --compare <branch>                 Branch to compare against
            
            Options for analyze:
              <file-path>                        Path to file to analyze
            
            Options for backup-stats:
              <path>                             Path to GWBK file or config directory
            
            Examples:
              kindling analyze-directory /path/to/ignition-config
              kindling compare-branches /path/to/repo --base main --compare develop
              kindling backup-stats /path/to/gateway.gwbk
              kindling analyze /path/to/file.idb
            """.trimIndent(),
        )
    }

    private fun printVersion() {
        val version = System.getProperty("app.version") ?: "Dev"
        println("Kindling CLI version $version")
    }

    /**
     * Analyze an Ignition configuration directory and output statistics in JSON format.
     */
    private fun analyzeDirectory(args: List<String>) {
        if (args.isEmpty()) {
            System.err.println("Error: Missing path argument")
            System.err.println("Usage: kindling analyze-directory <path>")
            exitProcess(1)
        }

        val path = Path.of(args[0])
        if (!path.exists()) {
            System.err.println("Error: Path does not exist: $path")
            exitProcess(1)
        }
        if (!path.isDirectory()) {
            System.err.println("Error: Path is not a directory: $path")
            exitProcess(1)
        }

        if (!DirectoryConfigSource.isIgnitionConfigDirectory(path)) {
            System.err.println("Warning: Path does not appear to be an Ignition configuration directory")
        }

        val result = DirectoryAnalysisResult(
            path = path.toString(),
            isIgnitionConfigDirectory = DirectoryConfigSource.isIgnitionConfigDirectory(path),
            hasProjectsDirectory = path.resolve("projects").exists(),
            hasConfigDirectory = path.resolve("config").exists(),
            hasIdb = path.resolve("db_backup_sqlite.idb").exists(),
        )

        println(json.encodeToString(result))
    }

    /**
     * Compare two Git branches and output diff summary in JSON format.
     */
    private fun compareBranches(args: List<String>) {
        if (args.isEmpty()) {
            System.err.println("Error: Missing repository path argument")
            System.err.println("Usage: kindling compare-branches <repo-path> --base <branch> --compare <branch>")
            exitProcess(1)
        }

        val repoPath = Path.of(args[0])
        if (!repoPath.exists()) {
            System.err.println("Error: Repository path does not exist: $repoPath")
            exitProcess(1)
        }
        if (!GitBranchViewer.isGitRepository(repoPath)) {
            System.err.println("Error: Not a Git repository: $repoPath")
            exitProcess(1)
        }

        // Parse options
        var baseBranch: String? = null
        var compareBranch: String? = null

        var i = 1
        while (i < args.size) {
            when (args[i]) {
                "--base", "-b" -> {
                    if (i + 1 >= args.size) {
                        System.err.println("Error: --base requires a branch name")
                        exitProcess(1)
                    }
                    baseBranch = args[i + 1]
                    i += 2
                }
                "--compare", "-c" -> {
                    if (i + 1 >= args.size) {
                        System.err.println("Error: --compare requires a branch name")
                        exitProcess(1)
                    }
                    compareBranch = args[i + 1]
                    i += 2
                }
                else -> i++
            }
        }

        // Open repository
        val repository = GitBranchViewer.openRepository(repoPath)
        val git = Git(repository)

        // Get available branches
        val branches = git.branchList().call()
        val branchNames = branches.map { it.name.removePrefix("refs/heads/") }

        // Use default branches if not specified
        if (baseBranch == null) {
            baseBranch = branchNames.find { it in listOf("main", "master") } ?: branchNames.firstOrNull()
        }
        if (compareBranch == null) {
            compareBranch = branchNames.find { it != baseBranch }
        }

        if (baseBranch == null || compareBranch == null) {
            System.err.println("Error: Could not determine branches to compare")
            exitProcess(1)
        }

        // Get diffs
        val diffs = try {
            getDiffs(repository, baseBranch, compareBranch)
        } catch (e: Exception) {
            System.err.println("Error comparing branches: ${e.message}")
            exitProcess(1)
        }

        val result = BranchComparisonResult(
            repositoryPath = repoPath.toString(),
            baseBranch = baseBranch,
            compareBranch = compareBranch,
            availableBranches = branchNames,
            changedFiles = diffs.map { diff ->
                ChangedFile(
                    changeType = diff.changeType.name,
                    oldPath = diff.oldPath.takeIf { it != "/dev/null" },
                    newPath = diff.newPath.takeIf { it != "/dev/null" },
                )
            },
            totalChanges = diffs.size,
            additions = diffs.count { it.changeType == DiffEntry.ChangeType.ADD },
            deletions = diffs.count { it.changeType == DiffEntry.ChangeType.DELETE },
            modifications = diffs.count { it.changeType == DiffEntry.ChangeType.MODIFY },
        )

        repository.close()
        println(json.encodeToString(result))
    }

    private fun getDiffs(
        repository: org.eclipse.jgit.lib.Repository,
        baseBranch: String,
        compareBranch: String,
    ): List<DiffEntry> {
        val baseRef = repository.resolve("refs/heads/$baseBranch")
            ?: repository.resolve(baseBranch)
            ?: throw IllegalArgumentException("Cannot resolve branch: $baseBranch")

        val compareRef = repository.resolve("refs/heads/$compareBranch")
            ?: repository.resolve(compareBranch)
            ?: throw IllegalArgumentException("Cannot resolve branch: $compareBranch")

        val revWalk = RevWalk(repository)
        val baseCommit = revWalk.parseCommit(baseRef)
        val compareCommit = revWalk.parseCommit(compareRef)

        val baseTree = CanonicalTreeParser().apply {
            repository.newObjectReader().use { reader ->
                reset(reader, baseCommit.tree.id)
            }
        }
        val compareTree = CanonicalTreeParser().apply {
            repository.newObjectReader().use { reader ->
                reset(reader, compareCommit.tree.id)
            }
        }

        val diffFormatter = DiffFormatter(ByteArrayOutputStream()).apply {
            setRepository(repository)
        }

        return diffFormatter.scan(baseTree, compareTree)
    }

    /**
     * Analyze a file and output structured data in JSON format.
     */
    private fun analyzeFile(args: List<String>) {
        if (args.isEmpty()) {
            System.err.println("Error: Missing file path argument")
            System.err.println("Usage: kindling analyze <file-path>")
            exitProcess(1)
        }

        val path = Path.of(args[0])
        if (!path.exists()) {
            System.err.println("Error: File does not exist: $path")
            exitProcess(1)
        }

        val result = if (path.isDirectory()) {
            FileAnalysisResult(
                path = path.toString(),
                type = "directory",
                isIgnitionConfig = DirectoryConfigSource.isIgnitionConfigDirectory(path),
                isGitRepository = GitBranchViewer.isGitRepository(path),
            )
        } else if (path.isRegularFile()) {
            val fileName = path.fileName?.toString() ?: path.toString()
            val extension = fileName.substringAfterLast('.', "").lowercase()
            FileAnalysisResult(
                path = path.toString(),
                type = extension,
                isIgnitionConfig = extension == "gwbk",
                isGitRepository = false,
            )
        } else {
            FileAnalysisResult(
                path = path.toString(),
                type = "unknown",
                isIgnitionConfig = false,
                isGitRepository = false,
            )
        }

        println(json.encodeToString(result))
    }

    /**
     * Get gateway backup/configuration statistics and output in JSON format.
     */
    private fun backupStats(args: List<String>) {
        if (args.isEmpty()) {
            System.err.println("Error: Missing path argument")
            System.err.println("Usage: kindling backup-stats <path>")
            exitProcess(1)
        }

        val path = Path.of(args[0])
        if (!path.exists()) {
            System.err.println("Error: Path does not exist: $path")
            exitProcess(1)
        }

        val backup = try {
            GatewayBackup(path)
        } catch (e: Exception) {
            System.err.println("Error opening backup: ${e.message}")
            exitProcess(1)
        }

        val stats = runBlocking {
            BackupStatsResult(
                path = path.toString(),
                displayName = backup.displayName,
                isZipBased = backup.isZipBased,
                hasConfigDb = backup.configDb != null,
                hasProjects = backup.projectsDirectory.exists(),
                hasConfig = backup.configDirectory.exists(),
                meta = MetaStatistics.calculate(backup)?.let {
                    MetaStatsResult(
                        uuid = it.uuid,
                        gatewayName = it.gatewayName,
                        edition = it.edition,
                        role = it.role,
                        version = it.version,
                        initMemory = it.initMemory,
                        maxMemory = it.maxMemory,
                    )
                },
                projects = ProjectStatistics.calculate(backup)?.let { stats ->
                    ProjectStatsResult(
                        projectCount = stats.projects.size,
                        perspectiveProjects = stats.perspectiveProjects,
                        visionProjects = stats.visionProjects,
                        projectNames = stats.projects.map { it.name },
                    )
                },
                databases = DatabaseStatistics.calculate(backup)?.let { stats ->
                    DatabaseStatsResult(
                        connectionCount = stats.connections.size,
                        connectionNames = stats.connections.map { it.name },
                    )
                },
                devices = DeviceStatistics.calculate(backup)?.let { stats ->
                    DeviceStatsResult(
                        deviceCount = stats.devices.size,
                        deviceNames = stats.devices.map { it.name },
                    )
                },
                opcServers = OpcServerStatistics.calculate(backup)?.let { stats ->
                    OpcServerStatsResult(
                        serverCount = stats.servers.size,
                        serverNames = stats.servers.map { it.name },
                    )
                },
                gatewayNetwork = GatewayNetworkStatistics.calculate(backup)?.let { stats ->
                    GatewayNetworkStatsResult(
                        outgoingConnectionCount = stats.outgoing.size,
                        incomingConnectionCount = stats.incoming.size,
                    )
                },
            )
        }

        println(json.encodeToString(stats))
    }
}

// Result data classes for JSON serialization

@Serializable
data class DirectoryAnalysisResult(
    val path: String,
    val isIgnitionConfigDirectory: Boolean,
    val hasProjectsDirectory: Boolean,
    val hasConfigDirectory: Boolean,
    val hasIdb: Boolean,
)

@Serializable
data class BranchComparisonResult(
    val repositoryPath: String,
    val baseBranch: String,
    val compareBranch: String,
    val availableBranches: List<String>,
    val changedFiles: List<ChangedFile>,
    val totalChanges: Int,
    val additions: Int,
    val deletions: Int,
    val modifications: Int,
)

@Serializable
data class ChangedFile(
    val changeType: String,
    val oldPath: String?,
    val newPath: String?,
)

@Serializable
data class FileAnalysisResult(
    val path: String,
    val type: String,
    val isIgnitionConfig: Boolean,
    val isGitRepository: Boolean,
)

@Serializable
data class BackupStatsResult(
    val path: String,
    val displayName: String,
    val isZipBased: Boolean,
    val hasConfigDb: Boolean,
    val hasProjects: Boolean,
    val hasConfig: Boolean,
    val meta: MetaStatsResult?,
    val projects: ProjectStatsResult?,
    val databases: DatabaseStatsResult?,
    val devices: DeviceStatsResult?,
    val opcServers: OpcServerStatsResult?,
    val gatewayNetwork: GatewayNetworkStatsResult?,
)

@Serializable
data class MetaStatsResult(
    val uuid: String?,
    val gatewayName: String?,
    val edition: String?,
    val role: String?,
    val version: String?,
    val initMemory: Int?,
    val maxMemory: Int?,
)

@Serializable
data class ProjectStatsResult(
    val projectCount: Int,
    val perspectiveProjects: Int,
    val visionProjects: Int,
    val projectNames: List<String>,
)

@Serializable
data class DatabaseStatsResult(
    val connectionCount: Int,
    val connectionNames: List<String>,
)

@Serializable
data class DeviceStatsResult(
    val deviceCount: Int,
    val deviceNames: List<String>,
)

@Serializable
data class OpcServerStatsResult(
    val serverCount: Int,
    val serverNames: List<String>,
)

@Serializable
data class GatewayNetworkStatsResult(
    val outgoingConnectionCount: Int,
    val incomingConnectionCount: Int,
)
