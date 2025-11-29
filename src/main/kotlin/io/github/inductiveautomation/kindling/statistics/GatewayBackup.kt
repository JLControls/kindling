package io.github.inductiveautomation.kindling.statistics

import org.w3c.dom.Document
import java.nio.file.Path
import java.sql.Connection
import java.util.Properties

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
