# Kindling AI Development Instructions

This document contains common instructions for AI assistants working with the Kindling codebase.

## Project Overview

Kindling is a standalone desktop application for advanced [Ignition](https://inductiveautomation.com/) users. It provides various tools to read and access Ignition's data export formats.

### Technology Stack

- **Language**: Kotlin (JVM)
- **UI Framework**: Java Swing with FlatLaf theming
- **Build Tool**: Gradle with Kotlin DSL
- **JDK**: Amazon Corretto 21
- **Testing**: Kotest

### Project Structure

```
kindling/
├── src/
│   ├── main/
│   │   ├── kotlin/io/github/inductiveautomation/kindling/
│   │   │   ├── MainPanel.kt          # Main application entry point (GUI)
│   │   │   ├── cli/                   # CLI mode for LLM access
│   │   │   ├── core/                  # Core interfaces (Tool, ToolPanel)
│   │   │   ├── alarm/                 # Alarm cache viewer
│   │   │   ├── cache/                 # Store and forward cache viewer
│   │   │   ├── directory/             # Directory-based config viewer
│   │   │   ├── git/                   # Git branch comparison
│   │   │   ├── idb/                   # IDB file viewer
│   │   │   ├── log/                   # Log file viewer
│   │   │   ├── statistics/            # Gateway backup statistics
│   │   │   ├── tagconfig/             # Tag configuration viewer
│   │   │   ├── thread/                # Thread dump viewer
│   │   │   ├── utils/                 # Utility classes
│   │   │   ├── xml/                   # XML viewer
│   │   │   └── zip/                   # Archive explorer
│   │   └── resources/
│   └── test/kotlin/
├── docs/                              # Docusaurus documentation
├── build.gradle.kts                   # Build configuration
└── settings.gradle.kts
```

## Development Guidelines

### Code Style

1. **Kotlin Conventions**: Follow Kotlin coding conventions
2. **Formatting**: Use ktlint via Spotless plugin (`./gradlew spotlessCheck`)
3. **No Comments Unless Necessary**: Only add comments matching existing file style or explaining complex logic

### Building and Testing

```bash
# Build the project
./gradlew build -x test

# Run tests
./gradlew test

# Run the application
./gradlew run

# Check formatting
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply
```

### Adding New Tools

1. Create a new package under `kindling/`
2. Implement the `Tool` interface from `core/Tool.kt`
3. Register the tool in `Tool.companion.tools` list
4. For directory-based tools, also implement `DirectoryTool`
5. For clipboard support, implement `ClipboardTool`
6. For multi-file support, implement `MultiTool`

### CLI Mode

The CLI mode (`cli/KindlingCli.kt`) provides command-line access for LLM integration:

- All output is JSON-formatted for easy parsing
- Commands: `analyze-directory`, `compare-branches`, `analyze`, `backup-stats`
- Entry point: `KindlingCli.main()`

## Key Patterns

### Tool Interface

```kotlin
interface Tool : KindlingSerializable {
    val title: String
    val description: String
    val icon: FlatSVGIcon
    val filter: FileFilter
    fun open(path: Path): ToolPanel
}
```

### ToolPanel Base Class

All tool views extend `ToolPanel`, which provides:
- Tab name and tooltip
- Icon for tab display
- MigLayout-based layout

### Configuration Sources

Two types of Ignition configuration sources:
1. `ZipConfigSource` - Traditional .gwbk files
2. `DirectoryConfigSource` - Ignition 8.3+ file-structure configs

## Common Tasks

### Adding a New Statistic Calculator

1. Create a data class implementing `Statistic`
2. Add a companion object implementing `StatisticCalculator<YourStatistic>`
3. The calculator receives a `GatewayBackup` and returns the statistic or null

### Working with Git Integration

- `GitBranchViewer` - Tool for opening Git repos
- `GitBranchView` - UI for comparing branches
- Uses JGit library for Git operations

### Error Handling

- Throw `ToolOpeningException` for user-friendly errors
- Use Kotlin's `runCatching` for graceful error handling
- Errors are displayed in error tabs in the UI

## Testing Guidelines

1. Use Kotest's FunSpec style
2. Create temp directories for file-based tests
3. Clean up temp files in finally blocks
4. Test both success and failure scenarios

## Dependencies

Key dependencies (see `gradle/libs.versions.toml`):
- JGit for Git operations
- FlatLaf for modern Swing theming
- kotlinx-serialization for JSON
- RSyntaxTextArea for syntax highlighting
- Kotest for testing
