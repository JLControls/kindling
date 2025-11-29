# Tools Overview

Kindling provides a variety of specialized tools for working with Ignition data formats. Each tool is designed to open specific file types and provide meaningful visualization and analysis.

## Available Tools

| Tool | File Types | Description |
|------|------------|-------------|
| [Thread Viewer](./thread-viewer.md) | `.json`, `.txt` | Analyze Ignition thread dumps |
| [IDB Viewer](./idb-viewer.md) | `.idb` | Explore SQLite databases |
| [Log Viewer](./log-viewer.md) | `wrapper.log` | Parse and aggregate log files |
| [Archive Explorer](./archive-explorer.md) | `.gwbk`, `.modl`, `.zip` | Browse archive contents |
| [Cache Viewer](./cache-viewer.md) | HSQLDB files | View Store and Forward cache |
| [Alarm Cache Viewer](./alarm-cache-viewer.md) | `.alarms_*` | View persisted alarm data |
| [Gateway Network Viewer](./gateway-network-viewer.md) | `.json`, `.txt` | Visualize GAN diagrams |
| [XML Viewer](./xml-viewer.md) | `.xml` | View and edit XML files |
| [Translation Editor](./translation-editor.md) | `.properties`, `.xml` | Edit translation bundles |
| [Directory Viewer](./directory-viewer.md) | Directories | Explore Ignition 8.3+ configs |
| [Git Branch Viewer](./git-branch-viewer.md) | Git repositories | Compare branch configurations |

## Opening Files

### Drag and Drop
Drag any supported file directly onto the Kindling window.

### File Menu
Use **File > Open** or **File > Open [Tool Name]** to select specific files.

### Tab Strip
Click the `+` button in the tab strip to open the file chooser.

## Multi-File Support

Some tools support opening multiple files at once:
- **Thread Viewer** - Aggregates multiple thread dumps
- **Log Viewer** - Sequences multiple log files

## Tool Selection

When opening a file, Kindling automatically selects the appropriate tool based on:
1. File extension
2. File content analysis
3. User-selected filter in file chooser
