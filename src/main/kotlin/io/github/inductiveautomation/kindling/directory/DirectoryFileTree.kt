package io.github.inductiveautomation.kindling.directory

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.jidesoft.comparator.AlphanumComparator
import com.jidesoft.swing.TreeSearchable
import io.github.inductiveautomation.kindling.core.Tool
import io.github.inductiveautomation.kindling.statistics.DirectoryConfigSource
import io.github.inductiveautomation.kindling.utils.AbstractTreeNode
import io.github.inductiveautomation.kindling.utils.ACTION_ICON_SCALE_FACTOR
import io.github.inductiveautomation.kindling.utils.TypedTreeNode
import io.github.inductiveautomation.kindling.utils.treeCellRenderer
import java.nio.file.Path
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption.INCLUDE_DIRECTORIES
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.walk

/**
 * A tree node representing a path in a local directory.
 */
data class DirectoryPathNode(override val userObject: Path) : TypedTreeNode<Path>() {
    override fun isLeaf(): Boolean = super.isLeaf() || !userObject.isDirectory()
}

/**
 * Root node for the directory tree.
 */
@OptIn(ExperimentalPathApi::class)
class DirectoryRootNode(rootDir: Path) : AbstractTreeNode() {
    init {
        val paths = rootDir.walk(INCLUDE_DIRECTORIES)

        val seen = mutableMapOf<Path, DirectoryPathNode>()
        for (path in paths) {
            var lastSeen: AbstractTreeNode = this
            var currentDepth = rootDir

            val relativePath = rootDir.relativize(path)
            for (part in relativePath) {
                currentDepth = currentDepth.resolve(part)
                val next = seen.getOrPut(currentDepth) {
                    val newChild = DirectoryPathNode(currentDepth)
                    lastSeen.children.add(newChild)
                    newChild
                }
                lastSeen = next
            }
        }

        sortWith(comparator, recursive = true)
    }

    companion object {
        private val comparator = compareBy<TreeNode> { node ->
            node as AbstractTreeNode
            val isDir = node.children.isNotEmpty() || (node as? DirectoryPathNode)?.userObject?.isDirectory() == true
            !isDir
        }.thenBy(AlphanumComparator(false)) { node ->
            val path = (node as? DirectoryPathNode)?.userObject
            path?.name.orEmpty()
        }
    }
}

/**
 * Tree model for a directory structure.
 */
class DirectoryTreeModel(rootDir: Path) : DefaultTreeModel(DirectoryRootNode(rootDir))

/**
 * A JTree that displays a local directory structure.
 */
class DirectoryFileTree(rootDir: Path) : JTree(DirectoryTreeModel(rootDir)) {
    init {
        isRootVisible = false
        setShowsRootHandles(true)

        setCellRenderer(
            treeCellRenderer { _, value, _, _, _, _, _ ->
                if (value is DirectoryPathNode) {
                    val path = value.userObject
                    toolTipText = path.toString()
                    text = path.name
                    icon = if (path.isRegularFile()) {
                        Tool.find(path)?.icon?.derive(ACTION_ICON_SCALE_FACTOR) ?: icon
                    } else {
                        icon
                    }
                }
                this
            },
        )

        object : TreeSearchable(this) {
            init {
                isRecursive = true
                isRepeats = true
            }

            override fun convertElementToString(element: Any?): String = when (val node = (element as? TreePath)?.lastPathComponent) {
                is DirectoryPathNode -> node.userObject.name
                else -> ""
            }
        }
    }

    override fun getModel(): DirectoryTreeModel? = super.getModel() as DirectoryTreeModel?
    override fun setModel(newModel: TreeModel?) {
        newModel as DirectoryTreeModel
        super.setModel(newModel)
    }
}
