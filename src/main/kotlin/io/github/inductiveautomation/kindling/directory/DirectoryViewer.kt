package io.github.inductiveautomation.kindling.directory

import com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSABLE
import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.components.FlatPopupMenu
import com.formdev.flatlaf.extras.components.FlatTabbedPane
import io.github.inductiveautomation.kindling.core.DirectoryTool
import io.github.inductiveautomation.kindling.core.Tool
import io.github.inductiveautomation.kindling.core.ToolOpeningException
import io.github.inductiveautomation.kindling.core.ToolPanel
import io.github.inductiveautomation.kindling.statistics.DirectoryConfigSource
import io.github.inductiveautomation.kindling.statistics.GatewayBackup
import io.github.inductiveautomation.kindling.tagconfig.TagConfigView
import io.github.inductiveautomation.kindling.tagconfig.model.TagProvider
import io.github.inductiveautomation.kindling.utils.Action
import io.github.inductiveautomation.kindling.utils.FileFilter
import io.github.inductiveautomation.kindling.utils.FlatScrollPane
import io.github.inductiveautomation.kindling.utils.HorizontalSplitPane
import io.github.inductiveautomation.kindling.utils.TabStrip
import io.github.inductiveautomation.kindling.utils.attachPopupMenu
import io.github.inductiveautomation.kindling.zip.views.ImageView
import io.github.inductiveautomation.kindling.zip.views.ProjectView
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonPrimitive
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.tree.TreeSelectionModel.CONTIGUOUS_TREE_SELECTION
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

/**
 * A view for exploring Ignition 8.3+ file-structure based configurations.
 * Similar to ZipView but works with local directories instead of ZIP files.
 */
class DirectoryView(path: Path) : ToolPanel("ins 6, flowy") {
    private val fileTree = DirectoryFileTree(path).apply {
        selectionModel.selectionMode = CONTIGUOUS_TREE_SELECTION
    }

    private val tabStrip = TabStrip()

    private val FlatTabbedPane.tabs: Sequence<DirectoryPathView>
        get() = sequence {
            repeat(tabCount) { i ->
                yield(getComponentAt(i) as DirectoryPathView)
            }
        }

    init {
        name = path.name
        toolTipText = path.toString()

        fileTree.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        val pathNode = fileTree.selectionPath?.lastPathComponent as? DirectoryPathNode ?: return
                        val actualPath = pathNode.userObject
                        maybeAddNewTab(actualPath)
                    }
                }
            },
        )

        fileTree.addKeyListener(
            object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_SPACE) {
                        val pathNode = fileTree.selectionPath?.lastPathComponent as? DirectoryPathNode ?: return
                        val actualPath = pathNode.userObject
                        maybeAddNewTab(actualPath)
                    }
                }
            },
        )

        fileTree.attachPopupMenu {
            selectionPaths?.let { selectedPaths ->
                FlatPopupMenu().apply {
                    val openAction =
                        Action("Open File") {
                            for (treePath in selectedPaths) {
                                val actualPath = (treePath.lastPathComponent as DirectoryPathNode).userObject
                                maybeAddNewTab(actualPath)
                            }
                        }
                    if (selectedPaths.size > 1) {
                        openAction.name = "Open ${selectedPaths.size} files individually"
                    }
                    add(openAction)
                }
            }
        }

        add(
            HorizontalSplitPane(
                FlatScrollPane(fileTree),
                tabStrip,
                0.1,
            ),
            "push, grow, span",
        )

        // If this looks like an Ignition config directory, add statistics view
        if (DirectoryConfigSource.isIgnitionConfigDirectory(path)) {
            maybeAddNewTab(path)

            val configPath = path.resolve("config")
            if (configPath.exists()) {
                maybeAddNewTab(configPath, select = false)
            }
        }
    }

    override val icon: Icon = DirectoryViewer.icon

    @OptIn(ExperimentalSerializationApi::class)
    private fun maybeAddNewTab(vararg paths: Path, select: Boolean = true) {
        val pathList = paths.toList()
        val existingTab = tabStrip.tabs.find { tab -> tab.paths == pathList }
        if (existingTab == null) {
            val pathView = createView(*paths)
            if (pathView != null) {
                pathView.putClientProperty(TABBED_PANE_TAB_CLOSABLE, pathView.closable)
                tabStrip.addTab(component = pathView, select = select)
            }
        } else {
            tabStrip.selectedComponent = existingTab
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createView(vararg paths: Path): DirectoryPathView? = runCatching {
        val path = paths.single()
        when {
            DirectoryConfigSource.isIgnitionConfigDirectory(path) -> DirectoryStatsView(path)
            TagConfigView.isConfigDirectory(path) -> createTagConfigView(path)
            ProjectView.isProjectDirectory(path) -> DirectoryProjectView(path)
            ImageView.isImageFile(path) -> DirectoryImageView(path)
            path.isRegularFile() -> DirectoryFileView(path)
            else -> null
        }
    }.getOrNull()

    @OptIn(ExperimentalSerializationApi::class)
    private fun createTagConfigView(configDir: Path): DirectoryPathView {
        val tagProviderData = TagProvider.loadProviders(configDir)
        val systemPropsPath = configDir / "resources/core/ignition/system-properties/config.json"

        val systemName = if (systemPropsPath.exists()) {
            systemPropsPath.inputStream().use {
                val json = Json.decodeFromStream<JsonObject>(it)
                json["systemName"]?.jsonPrimitive?.content ?: "Gateway"
            }
        } else {
            "Gateway"
        }

        val view = TagConfigView(systemName, tagProviderData)

        return object : DirectoryPathView("fill, ins 0") {
            override val path: Path = configDir
            override val icon: Icon? = null
            override val closable = false
            override val tabName = "Tag Config"

            init {
                add(view, "push, grow")
            }
        }
    }
}

/**
 * A PathView for directory-based paths (not within a ZIP file).
 */
abstract class DirectoryPathView(constraints: String = "ins 6, fill") : ToolPanel(constraints) {
    abstract val path: Path
    val paths: List<Path> by lazy { listOf(path) }
    open val closable: Boolean = true
    override val tabName: String by lazy { path.name }
    override val tabTooltip: String by lazy { path.toString() }
}

/**
 * Stats view for Ignition configuration directories.
 */
class DirectoryStatsView(override val path: Path) : DirectoryPathView("ins 6, fill, wrap 2, gap 20, hidemode 3") {
    override val icon: Icon? = null
    override val tabName: String = "Directory Statistics"
    override val closable: Boolean = false

    private val gatewayBackup = GatewayBackup(path)

    init {
        // Similar to GwbkStatsView, but for directory sources
        // We add basic info since directory-based configs may not have all IDB data
        val infoLabel = javax.swing.JLabel("Ignition Configuration Directory: ${path.name}").apply {
            putClientProperty("FlatLaf.styleClass", "h2")
        }
        add(infoLabel, "span, wrap")

        if (gatewayBackup.configDb != null) {
            add(javax.swing.JLabel("Contains configuration database (idb)"), "span, wrap")
        }
        if (gatewayBackup.projectsDirectory.exists()) {
            add(javax.swing.JLabel("Contains projects directory"), "span, wrap")
        }
        if (gatewayBackup.configDirectory.exists()) {
            add(javax.swing.JLabel("Contains config directory"), "span, wrap")
        }
    }
}

/**
 * File view for directory-based paths.
 */
class DirectoryFileView(override val path: Path) : DirectoryPathView() {
    override val icon: Icon? = null

    init {
        // Load the file content
        val textArea = org.fife.ui.rsyntaxtextarea.RSyntaxTextArea().apply {
            isEditable = false
            syntaxEditingStyle = when (path.extension.lowercase()) {
                "json" -> org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_JSON
                "xml" -> org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_XML
                "py" -> org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_PYTHON
                "properties" -> org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE
                else -> org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_NONE
            }
            text = path.inputStream().bufferedReader().readText()
        }
        add(org.fife.ui.rtextarea.RTextScrollPane(textArea), "push, grow")
    }
}

/**
 * Image view for directory-based paths.
 */
class DirectoryImageView(override val path: Path) : DirectoryPathView() {
    override val icon: Icon? = null

    init {
        val image = javax.imageio.ImageIO.read(path.inputStream())
        val imageIcon = javax.swing.ImageIcon(image)
        val label = javax.swing.JLabel(imageIcon)
        add(io.github.inductiveautomation.kindling.utils.FlatScrollPane(label), "push, grow")
    }
}

/**
 * Project view for directory-based paths.
 */
class DirectoryProjectView(override val path: Path) : DirectoryPathView() {
    override val icon: Icon = FlatSVGIcon("icons/bx-box.svg")

    init {
        val projectJsonPath = path / "project.json"
        if (projectJsonPath.exists()) {
            add(DirectoryFileView(projectJsonPath), "push, grow")
        }
    }
}

/**
 * Tool for opening Ignition 8.3+ file-structure based configurations.
 */
object DirectoryViewer : Tool, DirectoryTool {
    override val serialKey = "directory-viewer"
    override val title = "Configuration Directory"
    override val description = "Ignition 8.3+ file-structure based configurations"
    override val icon = FlatSVGIcon("icons/bx-folder-open.svg")
    override val filter = FileFilter(description, listOf()) // We'll handle directory selection differently

    override fun open(path: Path): ToolPanel {
        if (!path.isDirectory()) {
            throw ToolOpeningException("Expected a directory, but got a file: $path")
        }
        return DirectoryView(path)
    }

    override fun acceptsDirectory(path: Path): Boolean = DirectoryConfigSource.isIgnitionConfigDirectory(path)

    /**
     * Check if a path is a valid Ignition configuration directory.
     */
    fun isValidConfigDirectory(path: Path): Boolean = DirectoryConfigSource.isIgnitionConfigDirectory(path)
}
