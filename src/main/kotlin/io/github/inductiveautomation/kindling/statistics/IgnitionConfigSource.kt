package io.github.inductiveautomation.kindling.statistics

import io.github.inductiveautomation.kindling.utils.Properties
import io.github.inductiveautomation.kindling.utils.SQLiteConnection
import io.github.inductiveautomation.kindling.utils.XML_FACTORY
import io.github.inductiveautomation.kindling.utils.parse
import io.github.inductiveautomation.kindling.utils.transferTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.w3c.dom.Document
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.sql.Connection
import java.util.Properties
import kotlin.io.path.createTempFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.outputStream

/**
 * Abstraction over configuration sources for Ignition.
 * Supports both .gwbk files (ZIP archives) and file-structure based configurations (Ignition 8.3+).
 */
sealed interface IgnitionConfigSource {
    val projectsDirectory: Path
    val configDirectory: Path
    val info: Document?
    val configDb: Connection?
    val ignitionConf: Properties?
    val redundancyInfo: Properties?

    /**
     * Indicates if this source is from a .gwbk file or a directory.
     */
    val isZipBased: Boolean

    /**
     * A display name for this configuration source.
     */
    val displayName: String

    companion object {
        /**
         * Create an IgnitionConfigSource from a path.
         * If the path is a directory, it creates a DirectoryConfigSource.
         * If the path is a file, it creates a ZipConfigSource (for .gwbk files).
         */
        fun from(path: Path): IgnitionConfigSource {
            return if (path.isDirectory()) {
                DirectoryConfigSource(path)
            } else {
                ZipConfigSource(path)
            }
        }
    }
}

/**
 * Configuration source from a .gwbk file (ZIP archive).
 * This is the traditional backup format used by Ignition.
 */
class ZipConfigSource(path: Path) : IgnitionConfigSource {
    private val zipFile: FileSystem = FileSystems.newFileSystem(path)
    private val root: Path = zipFile.rootDirectories.first()

    override val displayName: String = path.name

    override val isZipBased: Boolean = true

    override val info: Document = root.resolve(BACKUP_INFO).inputStream().use(XML_FACTORY::parse)

    override val projectsDirectory: Path = root.resolve(PROJECTS)

    override val configDirectory: Path = root.resolve(CONFIG)

    private val tempFile: Path = createTempFile("gwbk-stats", "idb")

    // eagerly copy out the IDB, since we're always building the statistics view anyways
    private val dbCopyJob =
        CoroutineScope(Dispatchers.IO).launch {
            val idbPath = root.resolve(IDB)
            if (idbPath.exists()) {
                idbPath.inputStream() transferTo tempFile.outputStream()
            }
        }

    override val configDb: Connection? by lazy {
        val idbPath = root.resolve(IDB)
        if (!idbPath.exists()) return@lazy null

        // ensure the file copy is complete
        runBlocking { dbCopyJob.join() }

        SQLiteConnection(tempFile)
    }

    override val ignitionConf: Properties? by lazy {
        val confPath = root.resolve(IGNITION_CONF)
        if (confPath.exists()) {
            Properties(confPath.inputStream())
        } else {
            null
        }
    }

    override val redundancyInfo: Properties? by lazy {
        val redundancyPath = root.resolve(REDUNDANCY)
        if (redundancyPath.exists()) {
            Properties(redundancyPath.inputStream(), Properties::loadFromXML)
        } else {
            null
        }
    }

    companion object {
        private const val IDB = "db_backup_sqlite.idb"
        private const val BACKUP_INFO = "backupinfo.xml"
        private const val REDUNDANCY = "redundancy.xml"
        private const val IGNITION_CONF = "ignition.conf"
        private const val PROJECTS = "projects"
        private const val CONFIG = "config"
    }
}

/**
 * Configuration source from a directory (Ignition 8.3+ file-structure based configurations).
 * This is the new format that allows configurations to be stored as plain files on disk.
 */
class DirectoryConfigSource(private val rootPath: Path) : IgnitionConfigSource {
    override val displayName: String = rootPath.name

    override val isZipBased: Boolean = false

    override val projectsDirectory: Path = rootPath.resolve(PROJECTS)

    override val configDirectory: Path = rootPath.resolve(CONFIG)

    override val info: Document? by lazy {
        val infoPath = rootPath.resolve(BACKUP_INFO)
        if (infoPath.exists()) {
            infoPath.inputStream().use(XML_FACTORY::parse)
        } else {
            null
        }
    }

    override val configDb: Connection? by lazy {
        val idbPath = rootPath.resolve(IDB)
        if (idbPath.exists()) {
            val tempFile = createTempFile("dir-config", "idb")
            idbPath.inputStream() transferTo tempFile.outputStream()
            SQLiteConnection(tempFile)
        } else {
            null
        }
    }

    override val ignitionConf: Properties? by lazy {
        val confPath = rootPath.resolve(IGNITION_CONF)
        if (confPath.exists()) {
            Properties(confPath.inputStream())
        } else {
            null
        }
    }

    override val redundancyInfo: Properties? by lazy {
        val redundancyPath = rootPath.resolve(REDUNDANCY)
        if (redundancyPath.exists()) {
            Properties(redundancyPath.inputStream(), Properties::loadFromXML)
        } else {
            null
        }
    }

    companion object {
        private const val IDB = "db_backup_sqlite.idb"
        private const val BACKUP_INFO = "backupinfo.xml"
        private const val REDUNDANCY = "redundancy.xml"
        private const val IGNITION_CONF = "ignition.conf"
        private const val PROJECTS = "projects"
        private const val CONFIG = "config"

        /**
         * Check if a directory looks like an Ignition configuration directory.
         */
        fun isIgnitionConfigDirectory(path: Path): Boolean {
            if (!path.isDirectory()) return false
            // Check for common Ignition configuration markers
            val hasProjects = path.resolve(PROJECTS).exists()
            val hasConfig = path.resolve(CONFIG).exists()
            val hasIdb = path.resolve(IDB).exists()
            return hasProjects || hasConfig || hasIdb
        }
    }
}
