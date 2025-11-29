package io.github.inductiveautomation.kindling.git

import com.formdev.flatlaf.extras.components.FlatComboBox
import io.github.inductiveautomation.kindling.core.ToolPanel
import io.github.inductiveautomation.kindling.utils.Action
import io.github.inductiveautomation.kindling.utils.EDT_SCOPE
import io.github.inductiveautomation.kindling.utils.FlatScrollPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.miginfocom.swing.MigLayout
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Path
import javax.swing.DefaultListModel
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.ListSelectionModel
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import kotlin.io.path.name

/**
 * A view for comparing Ignition configurations across Git branches.
 */
class GitBranchView(private val repoPath: Path) : ToolPanel("ins 6, fill") {
    private val repository: Repository = GitBranchViewer.openRepository(repoPath)
    private val git: Git = Git(repository)

    private val branches: List<Ref> = git.branchList().call()
    private val branchNames: List<String> = branches.map { it.name.removePrefix("refs/heads/") }

    // Cache for resolved branch references to avoid repeated resolution
    private val branchRefCache = mutableMapOf<String, org.eclipse.jgit.lib.ObjectId>()

    private val leftBranchCombo = FlatComboBox<String>().apply {
        branchNames.forEach { addItem(it) }
        selectedItem = findDefaultBranch(branchNames, listOf("main", "master", "develop"))
    }

    private val rightBranchCombo = FlatComboBox<String>().apply {
        branchNames.forEach { addItem(it) }
        // Try to select a different branch for comparison
        selectedItem = findDefaultBranch(branchNames.filter { it != leftBranchCombo.selectedItem }, listOf("dev", "qa", "develop", "feature"))
            ?: branchNames.firstOrNull { it != leftBranchCombo.selectedItem }
            ?: branchNames.firstOrNull()
    }

    private val compareButton = JButton(
        Action("Compare Branches") {
            compareBranches()
        },
    )

    private val diffListModel = DefaultListModel<DiffEntry>()
    private val diffList = JList(diffListModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        setCellRenderer { _, value, _, isSelected, _ ->
            JLabel().apply {
                text = when (value.changeType) {
                    DiffEntry.ChangeType.ADD -> "[+] ${value.newPath}"
                    DiffEntry.ChangeType.DELETE -> "[-] ${value.oldPath}"
                    DiffEntry.ChangeType.MODIFY -> "[M] ${value.oldPath}"
                    DiffEntry.ChangeType.COPY -> "[C] ${value.oldPath} -> ${value.newPath}"
                    DiffEntry.ChangeType.RENAME -> "[R] ${value.oldPath} -> ${value.newPath}"
                    else -> value.toString()
                }
                foreground = when (value.changeType) {
                    DiffEntry.ChangeType.ADD -> Color(0, 128, 0)
                    DiffEntry.ChangeType.DELETE -> Color(192, 0, 0)
                    DiffEntry.ChangeType.MODIFY -> Color(0, 0, 192)
                    else -> UIManager.getColor("Label.foreground")
                }
                if (isSelected) {
                    background = UIManager.getColor("List.selectionBackground")
                    foreground = UIManager.getColor("List.selectionForeground")
                    isOpaque = true
                }
                border = EmptyBorder(2, 4, 2, 4)
            }
        }
        addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                selectedValue?.let { showDiff(it) }
            }
        }
    }

    private val leftDiffText = RSyntaxTextArea().apply {
        isEditable = false
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JSON
    }

    private val rightDiffText = RSyntaxTextArea().apply {
        isEditable = false
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JSON
    }

    private val statusLabel = JLabel("Select two branches and click Compare")

    init {
        name = "Git: ${repoPath.name}"
        toolTipText = repoPath.toString()

        val controlPanel = JPanel(MigLayout("ins 0, fillx")).apply {
            add(JLabel("Base Branch:"), "")
            add(leftBranchCombo, "growx, wmin 150")
            add(JLabel("  Compare With:"), "gapleft 20")
            add(rightBranchCombo, "growx, wmin 150")
            add(compareButton, "gapleft 20")
            add(statusLabel, "gapleft 20, pushx")
        }

        val diffListPanel = JPanel(BorderLayout()).apply {
            add(JLabel("Changed Files:").apply {
                border = EmptyBorder(0, 0, 5, 0)
                putClientProperty("FlatLaf.styleClass", "h4")
            }, BorderLayout.NORTH)
            add(FlatScrollPane(diffList), BorderLayout.CENTER)
        }

        val diffContentPanel = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            JPanel(BorderLayout()).apply {
                add(JLabel("Base (${leftBranchCombo.selectedItem})").apply {
                    putClientProperty("FlatLaf.styleClass", "h4")
                }, BorderLayout.NORTH)
                add(RTextScrollPane(leftDiffText), BorderLayout.CENTER)
            },
            JPanel(BorderLayout()).apply {
                add(JLabel("Compare (${rightBranchCombo.selectedItem})").apply {
                    putClientProperty("FlatLaf.styleClass", "h4")
                }, BorderLayout.NORTH)
                add(RTextScrollPane(rightDiffText), BorderLayout.CENTER)
            },
        ).apply {
            resizeWeight = 0.5
        }

        val mainSplit = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            diffListPanel,
            diffContentPanel,
        ).apply {
            resizeWeight = 0.2
        }

        add(controlPanel, "growx, wrap")
        add(mainSplit, "push, grow")
    }

    override val icon: Icon = GitBranchViewer.icon

    private fun findDefaultBranch(branches: List<String>, preferred: List<String>): String? {
        for (pref in preferred) {
            branches.find { it.equals(pref, ignoreCase = true) }?.let { return it }
        }
        return branches.firstOrNull()
    }

    private fun compareBranches() {
        val leftBranch = leftBranchCombo.selectedItem as? String ?: return
        val rightBranch = rightBranchCombo.selectedItem as? String ?: return

        if (leftBranch == rightBranch) {
            statusLabel.text = "Please select different branches to compare"
            return
        }

        statusLabel.text = "Comparing $leftBranch with $rightBranch..."
        diffListModel.clear()
        leftDiffText.text = ""
        rightDiffText.text = ""

        EDT_SCOPE.launch {
            val diffs = withContext(Dispatchers.IO) {
                getDiffsBetweenBranches(leftBranch, rightBranch)
            }

            diffListModel.clear()
            diffs.forEach { diffListModel.addElement(it) }
            statusLabel.text = "Found ${diffs.size} changed file(s)"
        }
    }

    private fun getDiffsBetweenBranches(leftBranch: String, rightBranch: String): List<DiffEntry> {
        val leftTree = getTreeIterator(leftBranch)
        val rightTree = getTreeIterator(rightBranch)

        val diffFormatter = DiffFormatter(ByteArrayOutputStream()).apply {
            setRepository(repository)
        }

        return diffFormatter.scan(leftTree, rightTree)
    }

    private fun resolveBranchRef(branchName: String): org.eclipse.jgit.lib.ObjectId? {
        return branchRefCache.getOrPut(branchName) {
            repository.resolve("refs/heads/$branchName")
                ?: repository.resolve(branchName)
                ?: return null
        }
    }

    private fun getTreeIterator(branchName: String): AbstractTreeIterator {
        val revWalk = RevWalk(repository)
        val branchRef = resolveBranchRef(branchName)
            ?: throw IOException("Cannot resolve branch: $branchName")

        val commit = revWalk.parseCommit(branchRef)
        val tree = commit.tree

        val parser = CanonicalTreeParser()
        repository.newObjectReader().use { reader ->
            parser.reset(reader, tree.id)
        }
        return parser
    }

    private fun showDiff(entry: DiffEntry) {
        EDT_SCOPE.launch {
            val (leftContent, rightContent) = withContext(Dispatchers.IO) {
                val left = getFileContent(leftBranchCombo.selectedItem as String, entry.oldPath)
                val right = getFileContent(rightBranchCombo.selectedItem as String, entry.newPath)
                left to right
            }

            leftDiffText.text = leftContent
            rightDiffText.text = rightContent

            // Set syntax highlighting based on file extension
            val extension = entry.newPath.substringAfterLast('.', "").lowercase()
            val syntaxStyle = when (extension) {
                "json" -> SyntaxConstants.SYNTAX_STYLE_JSON
                "xml" -> SyntaxConstants.SYNTAX_STYLE_XML
                "py" -> SyntaxConstants.SYNTAX_STYLE_PYTHON
                "properties" -> SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE
                else -> SyntaxConstants.SYNTAX_STYLE_NONE
            }
            leftDiffText.syntaxEditingStyle = syntaxStyle
            rightDiffText.syntaxEditingStyle = syntaxStyle

            leftDiffText.caretPosition = 0
            rightDiffText.caretPosition = 0
        }
    }

    private fun getFileContent(branchName: String, filePath: String): String {
        if (filePath == "/dev/null") return ""

        return try {
            val branchRef = resolveBranchRef(branchName) ?: return ""

            RevWalk(repository).use { revWalk ->
                val commit = revWalk.parseCommit(branchRef)
                val tree = commit.tree

                TreeWalk(repository).use { treeWalk ->
                    treeWalk.addTree(tree)
                    treeWalk.isRecursive = true
                    treeWalk.filter = PathFilter.create(filePath)

                    if (treeWalk.next()) {
                        val objectId = treeWalk.getObjectId(0)
                        repository.newObjectReader().use { reader ->
                            val loader = reader.open(objectId)
                            String(loader.bytes, Charsets.UTF_8)
                        }
                    } else {
                        ""
                    }
                }
            }
        } catch (e: Exception) {
            "Error loading file: ${e.message}"
        }
    }
}
