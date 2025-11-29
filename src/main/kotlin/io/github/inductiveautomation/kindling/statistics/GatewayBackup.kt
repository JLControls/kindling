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
import java.nio.file.FileSystems
import java.nio.file.Path
import java.sql.Connection
import java.util.Properties
import kotlin.io.path.createTempFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.outputStream

/**
 * Represents a Gateway Backup or configuration directory.
 * Supports both .gwbk files (ZIP archives) and file-structure based configurations (Ignition 8.3+).
 */
class GatewayBackup private constructor(
    private val source: IgnitionConfigSource,
) {
    val info: Document? get() = source.info

    val projectsDirectory: Path get() = source.projectsDirectory

    val configDirectory: Path get() = source.configDirectory

    val configDb: Connection? get() = source.configDb

    val ignitionConf: Properties? get() = source.ignitionConf

    val redundancyInfo: Properties? get() = source.redundancyInfo

    val isZipBased: Boolean get() = source.isZipBased

    val displayName: String get() = source.displayName

    companion object {
        private const val IDB = "db_backup_sqlite.idb"
        private const val BACKUP_INFO = "backupinfo.xml"
        private const val REDUNDANCY = "redundancy.xml"
        private const val IGNITION_CONF = "ignition.conf"
        private const val PROJECTS = "projects"
        private const val CONFIG = "config"

        /**
         * Create a GatewayBackup from a path.
         * If the path is a directory, it will be treated as a file-structure configuration.
         * If the path is a file, it will be treated as a .gwbk ZIP archive.
         */
        operator fun invoke(path: Path): GatewayBackup {
            return GatewayBackup(IgnitionConfigSource.from(path))
        }
    }
}
