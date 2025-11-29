# Directory Viewer

The Directory Viewer explores Ignition 8.3+ file-structure based configurations directly from disk, without requiring a gateway backup.

## Overview

Starting with Ignition 8.3, configurations can be stored as plain files on disk rather than only in the internal database. This tool allows you to browse and analyze these directory-based configurations.

## Features

### Configuration Detection

Automatically identifies Ignition configuration directories by detecting:
- `projects/` directory
- `config/` directory  
- `db_backup_sqlite.idb` file

### Integrated Views

- **Statistics View** - Summary of the configuration
- **Tag Configuration View** - Browse tag providers and tags
- **Project View** - Explore project resources
- **File View** - View individual files with syntax highlighting

### File Type Support

- JSON files with syntax highlighting
- XML files with syntax highlighting
- Image preview for icons and resources
- Python scripts with syntax highlighting

## Usage

1. Open a directory containing Ignition configuration
2. The file tree shows all contents
3. Double-click files to open in appropriate viewers
4. Statistics are shown for recognized config directories

## CLI Usage

### Analyze Directory

```bash
kindling analyze-directory /path/to/ignition-config
```

Output:
```json
{
  "path": "/path/to/ignition-config",
  "isIgnitionConfigDirectory": true,
  "hasProjectsDirectory": true,
  "hasConfigDirectory": true,
  "hasIdb": false
}
```

### Get Backup Statistics

For directories with full configuration:
```bash
kindling backup-stats /path/to/ignition-config
```

## Directory Structure

A typical Ignition 8.3+ configuration directory:
```
ignition-config/
├── projects/
│   ├── MyProject/
│   │   ├── project.json
│   │   ├── com.inductiveautomation.perspective/
│   │   │   └── views/
│   │   └── ignition/
│   │       └── named-query/
├── config/
│   ├── tags/
│   │   └── default/
│   └── resources/
└── db_backup_sqlite.idb (optional)
```

## Use Cases

### Local Development

Browse configuration while developing:
- View tag structure
- Check project resources
- Validate JSON formatting

### Git Integration

Combine with Git Branch Viewer:
1. Clone repository with Ignition config
2. Use Directory Viewer to browse current state
3. Use Git Branch Viewer to compare branches

### Configuration Review

Before deploying configuration changes:
- Verify project structure
- Check tag counts and organization
- Review resource allocations

## Tips

- Works best with Ignition 8.3+ configurations
- Older gateway backups should use Archive Explorer instead
- The directory doesn't need to be a complete gateway backup
