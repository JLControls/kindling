# Archive Explorer

The Archive Explorer opens ZIP archives, including Ignition-specific formats like gateway backups (`.gwbk`) and modules (`.modl`).

## Supported Formats

- `.gwbk` - Gateway backup files
- `.modl` - Ignition module files
- `.zip` - Standard ZIP archives
- `.jar` - Java archive files

## Features

### File Tree Navigation

- Hierarchical view of archive contents
- Expand/collapse directories
- File type icons

### Integrated Tool Opening

Double-click files within the archive to open them with the appropriate tool:
- `.idb` files → IDB Viewer
- `project.json` → Project View
- Log files → Log Viewer
- Images → Image Preview

### Gateway Backup Statistics

For `.gwbk` files, automatic statistics extraction:
- Gateway name and version
- Project count (Perspective/Vision)
- Database connections
- Device configurations
- OPC server settings

## Usage

1. Open a `.gwbk`, `.modl`, or `.zip` file
2. Browse the file tree on the left
3. Double-click files to open in appropriate viewers
4. View statistics in the dedicated panel (for `.gwbk`)

## CLI Usage

### Analyze a Gateway Backup

```bash
kindling backup-stats /path/to/gateway.gwbk
```

Output:
```json
{
  "path": "/path/to/gateway.gwbk",
  "displayName": "gateway.gwbk",
  "isZipBased": true,
  "hasConfigDb": true,
  "hasProjects": true,
  "hasConfig": true,
  "meta": {
    "gatewayName": "Production Gateway",
    "version": "8.1.20",
    "edition": "Standard"
  },
  "projects": {
    "projectCount": 5,
    "perspectiveProjects": 3,
    "visionProjects": 2
  }
}
```

## Gateway Backup Structure

A typical `.gwbk` contains:
```
├── backupinfo.xml        # Backup metadata
├── db_backup_sqlite.idb  # Configuration database
├── ignition.conf         # Gateway configuration
├── projects/             # Project resources
│   ├── MyProject/
│   │   ├── project.json
│   │   └── ...
├── config/               # Additional configuration
└── redundancy.xml        # Redundancy settings (if applicable)
```

## Tips

- Gateway backups can be large; extraction happens on-demand
- The IDB within contains all configuration data
- Projects folder uses the 8.1+ resource format
