# Architecture Overview

Understanding Kindling's architecture helps when contributing or extending the application.

## Core Concepts

### Tools

A `Tool` is the primary extension point in Kindling. Each tool:
- Opens specific file types
- Provides a specialized view
- Is registered in the application

```kotlin
interface Tool : KindlingSerializable {
    val title: String           // Display name
    val description: String     // File chooser description
    val icon: FlatSVGIcon       // Tab and UI icon
    val filter: FileFilter      // File type filter
    
    fun open(path: Path): ToolPanel
}
```

### Tool Extensions

| Interface | Purpose |
|-----------|---------|
| `MultiTool` | Open multiple files at once |
| `ClipboardTool` | Open from clipboard data |
| `DirectoryTool` | Open directories |

### ToolPanel

`ToolPanel` is the base class for all tool views:

```kotlin
abstract class ToolPanel(constraints: String) : JPanel() {
    abstract val icon: Icon?
    open val tabName: String
    open val tabTooltip: String
}
```

## Package Structure

### Core (`core/`)

- `Tool.kt` - Tool interface and registry
- `ToolPanel.kt` - Base panel class
- `Kindling.kt` - Preferences and configuration
- `KindlingSerializable.kt` - Serialization support

### Tools

Each tool has its own package:

| Package | Tool |
|---------|------|
| `thread/` | Thread dump viewer |
| `idb/` | IDB file viewer |
| `log/` | Log file viewer |
| `zip/` | Archive explorer |
| `cache/` | S&F cache viewer |
| `alarm/` | Alarm cache viewer |
| `xml/` | XML viewer |
| `directory/` | Directory viewer |
| `git/` | Git branch viewer |
| `statistics/` | Backup statistics |

### Utilities (`utils/`)

Shared utilities:
- UI components (`Components.kt`)
- Table utilities (`Tables.kt`)
- File operations (`FileFilter.kt`)
- Swing extensions (`Swing.kt`)

## Data Flow

### Opening Files

```
File Selection → Tool.find() → tool.open(path) → ToolPanel → Tab
```

1. User selects file via dialog/drag-drop
2. `Tool.find()` matches file to tool
3. `tool.open()` creates `ToolPanel`
4. Panel added to tab strip

### CLI Mode

```
CLI Args → Command Parser → Analysis → JSON Output
```

1. Args parsed in `KindlingCli`
2. Appropriate analysis function called
3. Results serialized to JSON
4. Output to stdout

## Key Patterns

### Configuration Sources

Two ways to access Ignition configuration:

```kotlin
sealed interface IgnitionConfigSource {
    val projectsDirectory: Path
    val configDirectory: Path
    val configDb: Connection?
}

class ZipConfigSource(path: Path)     // .gwbk files
class DirectoryConfigSource(path: Path)  // 8.3+ directories
```

### Statistics Calculators

```kotlin
fun interface StatisticCalculator<T : Statistic> {
    suspend fun calculate(backup: GatewayBackup): T?
}
```

Each calculator:
- Takes a `GatewayBackup`
- Returns typed statistics or null
- Runs in coroutine context

### Coroutines

UI operations use coroutines:
- `EDT_SCOPE` for UI updates
- `Dispatchers.IO` for file operations

```kotlin
EDT_SCOPE.launch {
    val data = withContext(Dispatchers.IO) {
        loadData()  // Background
    }
    updateUI(data)  // EDT
}
```

## UI Framework

### FlatLaf Theming

Kindling uses FlatLaf for modern Swing appearance:

```kotlin
// Apply style classes
component.putClientProperty("FlatLaf.styleClass", "h1")

// Use themed icons
FlatSVGIcon("icons/my-icon.svg")
```

### MigLayout

All panels use MigLayout for layout:

```kotlin
class MyPanel : ToolPanel("fill, ins 6") {
    init {
        add(component, "grow, wrap")
        add(other, "span")
    }
}
```

## Extension Points

### Adding a New Tool

1. Create tool package
2. Implement `Tool` interface
3. Create `ToolPanel` subclass
4. Register in `Tool.tools`

### Adding CLI Command

1. Add command case in `KindlingCli.main()`
2. Create handler function
3. Define result data class with `@Serializable`

### Adding Statistics

1. Create `Statistic` data class
2. Implement `StatisticCalculator` companion
3. Add to backup stats output

## Testing

### Unit Tests

```kotlin
class MyToolTest : FunSpec({
    test("should parse file") {
        val result = MyTool.parse(testFile)
        result.shouldNotBeNull()
    }
})
```

### Integration Tests

```kotlin
test("should open and display") {
    val panel = MyTool.open(testPath)
    panel.shouldBeInstanceOf<MyToolPanel>()
}
```
