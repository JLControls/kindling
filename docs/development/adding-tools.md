# Adding New Tools

This guide walks through creating a new tool for Kindling.

## Overview

A tool in Kindling consists of:
1. **Tool object** - Implements `Tool` interface, handles file matching and opening
2. **ToolPanel** - The UI component displayed in tabs
3. **Registration** - Adding to the tool registry

## Step 1: Create the Package

Create a new package under `kindling/`:

```
src/main/kotlin/io/github/inductiveautomation/kindling/mytool/
├── MyToolViewer.kt    # Tool object
└── MyToolView.kt      # ToolPanel implementation
```

## Step 2: Implement the Tool

```kotlin
package io.github.inductiveautomation.kindling.mytool

import com.formdev.flatlaf.extras.FlatSVGIcon
import io.github.inductiveautomation.kindling.core.Tool
import io.github.inductiveautomation.kindling.core.ToolPanel
import io.github.inductiveautomation.kindling.utils.FileFilter
import java.nio.file.Path

object MyToolViewer : Tool {
    override val serialKey = "my-tool"  // Unique identifier
    override val title = "My Tool"
    override val description = "Description for file chooser"
    override val icon = FlatSVGIcon("icons/my-icon.svg")
    
    // File extensions this tool handles
    override val filter = FileFilter(
        description,
        listOf("myext", "other")
    )
    
    override fun open(path: Path): ToolPanel {
        return MyToolView(path)
    }
}
```

## Step 3: Create the View

```kotlin
package io.github.inductiveautomation.kindling.mytool

import io.github.inductiveautomation.kindling.core.ToolPanel
import java.nio.file.Path
import javax.swing.Icon

class MyToolView(private val path: Path) : ToolPanel("fill, ins 6") {
    
    override val icon: Icon = MyToolViewer.icon
    override val tabName: String = path.fileName.toString()
    override val tabTooltip: String = path.toString()
    
    init {
        // Build your UI here
        add(createMainComponent(), "grow")
    }
    
    private fun createMainComponent(): JComponent {
        // Your implementation
    }
}
```

## Step 4: Register the Tool

Add to `Tool.kt`:

```kotlin
companion object {
    val tools: List<Tool> by lazy {
        listOf(
            // ... existing tools
            MyToolViewer,  // Add here
        )
    }
}
```

## Tool Variations

### MultiTool (Multiple Files)

```kotlin
object MyMultiViewer : Tool, MultiTool {
    override fun open(paths: List<Path>): ToolPanel {
        return MyMultiView(paths)
    }
    
    // Single file delegates to multi
    override fun open(path: Path): ToolPanel = open(listOf(path))
}
```

### DirectoryTool

```kotlin
object MyDirViewer : Tool, DirectoryTool {
    override fun acceptsDirectory(path: Path): Boolean {
        return path.resolve("marker-file.txt").exists()
    }
}
```

### ClipboardTool

```kotlin
object MyClipboardViewer : Tool, ClipboardTool {
    override fun open(data: String): ToolPanel {
        return MyClipboardView(data)
    }
}
```

## UI Patterns

### Sidebar + Content

```kotlin
class MyToolView(path: Path) : ToolPanel("fill, ins 6") {
    private val sidebar = createSidebar()
    private val content = createContent()
    
    init {
        add(
            HorizontalSplitPane(sidebar, content, 0.2),
            "grow"
        )
    }
}
```

### Tabbed Content

```kotlin
class MyToolView(path: Path) : ToolPanel("fill, ins 6") {
    private val tabs = TabStrip()
    
    init {
        add(tabs, "grow")
        tabs.addTab(component = FirstTab(), select = true)
        tabs.addTab(component = SecondTab())
    }
}
```

### Table View

```kotlin
class MyToolView(path: Path) : ToolPanel("fill, ins 6") {
    private val table = ReifiedJXTable(MyColumnList, data)
    
    init {
        add(FlatScrollPane(table), "grow")
    }
}
```

## Error Handling

Throw `ToolOpeningException` for user-friendly errors:

```kotlin
override fun open(path: Path): ToolPanel {
    if (!isValidFile(path)) {
        throw ToolOpeningException("Invalid file format: $path")
    }
    return MyToolView(path)
}
```

## Adding Icons

1. Add SVG icon to `src/main/resources/icons/`
2. Reference with `FlatSVGIcon("icons/my-icon.svg")`
3. Use [BoxIcons](https://boxicons.com/) for consistency

## Testing

Create tests in `src/test/kotlin/`:

```kotlin
class MyToolViewerTest : FunSpec({
    test("filter accepts correct extensions") {
        MyToolViewer.filter.accept(Path.of("file.myext")).shouldBeTrue()
        MyToolViewer.filter.accept(Path.of("file.txt")).shouldBeFalse()
    }
    
    test("opens valid file") {
        val panel = MyToolViewer.open(validTestFile)
        panel.shouldBeInstanceOf<MyToolView>()
    }
    
    test("throws for invalid file") {
        shouldThrow<ToolOpeningException> {
            MyToolViewer.open(invalidTestFile)
        }
    }
})
```

## Checklist

- [ ] Tool object with unique `serialKey`
- [ ] ToolPanel with proper icon/name/tooltip
- [ ] Registered in `Tool.tools`
- [ ] Icon added to resources
- [ ] Tests for filter and opening
- [ ] Documentation in `docs/tools/`

## Example: Full Tool

See existing tools for complete examples:
- `thread/` - MultiTool example
- `directory/` - DirectoryTool example  
- `git/` - Complex UI with coroutines
- `cache/` - Database interaction
