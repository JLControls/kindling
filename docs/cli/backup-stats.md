# backup-stats

Get comprehensive statistics from a gateway backup or configuration directory.

## Synopsis

```bash
kindling backup-stats <path>
```

## Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `path` | Yes | Path to a .gwbk file or configuration directory |

## Output

```json
{
  "path": "/path/to/gateway.gwbk",
  "displayName": "gateway.gwbk",
  "isZipBased": true,
  "hasConfigDb": true,
  "hasProjects": true,
  "hasConfig": true,
  "meta": {
    "uuid": "abc123",
    "gatewayName": "Production Gateway",
    "edition": "Standard",
    "role": "Independent",
    "version": "8.1.20",
    "initMemory": 256,
    "maxMemory": 2048
  },
  "projects": {
    "projectCount": 5,
    "perspectiveProjects": 3,
    "visionProjects": 2,
    "projectNames": ["HMI", "Reporting", "Common"]
  },
  "databases": {
    "connectionCount": 3,
    "connectionNames": ["MySQL", "Historian", "Archive"]
  },
  "devices": {
    "deviceCount": 12,
    "deviceNames": ["PLC1", "PLC2", "Simulator"]
  },
  "opcServers": {
    "serverCount": 2,
    "serverNames": ["Ignition OPC UA Server", "Third Party Server"]
  },
  "gatewayNetwork": {
    "outgoingConnectionCount": 1,
    "incomingConnectionCount": 0
  }
}
```

### Fields

#### Root Level

| Field | Type | Description |
|-------|------|-------------|
| `path` | string | Path to the backup/directory |
| `displayName` | string | Display name |
| `isZipBased` | boolean | True for .gwbk files, false for directories |
| `hasConfigDb` | boolean | Whether config database exists |
| `hasProjects` | boolean | Whether projects directory exists |
| `hasConfig` | boolean | Whether config directory exists |

#### Meta Object

| Field | Type | Description |
|-------|------|-------------|
| `uuid` | string? | Gateway UUID |
| `gatewayName` | string? | Gateway name |
| `edition` | string? | Ignition edition (Standard/Edge/Maker) |
| `role` | string? | Redundancy role |
| `version` | string? | Ignition version |
| `initMemory` | number? | Initial JVM memory (MB) |
| `maxMemory` | number? | Maximum JVM memory (MB) |

#### Projects Object

| Field | Type | Description |
|-------|------|-------------|
| `projectCount` | number | Total project count |
| `perspectiveProjects` | number | Projects with Perspective resources |
| `visionProjects` | number | Projects with Vision resources |
| `projectNames` | string[] | List of project names |

#### Databases Object

| Field | Type | Description |
|-------|------|-------------|
| `connectionCount` | number | Number of database connections |
| `connectionNames` | string[] | List of connection names |

#### Devices Object

| Field | Type | Description |
|-------|------|-------------|
| `deviceCount` | number | Number of device connections |
| `deviceNames` | string[] | List of device names |

#### OPC Servers Object

| Field | Type | Description |
|-------|------|-------------|
| `serverCount` | number | Number of OPC servers |
| `serverNames` | string[] | List of server names |

#### Gateway Network Object

| Field | Type | Description |
|-------|------|-------------|
| `outgoingConnectionCount` | number | Outgoing GAN connections |
| `incomingConnectionCount` | number | Incoming GAN connections |

## Examples

### Basic statistics

```bash
kindling backup-stats /path/to/gateway.gwbk
```

### Get gateway name

```bash
kindling backup-stats gateway.gwbk | jq -r '.meta.gatewayName'
```

### Count projects

```bash
kindling backup-stats gateway.gwbk | jq '.projects.projectCount'
```

### List all project names

```bash
kindling backup-stats gateway.gwbk | jq -r '.projects.projectNames[]'
```

### Check if it's a Perspective deployment

```bash
if kindling backup-stats gateway.gwbk | jq -e '.projects.perspectiveProjects > 0'; then
  echo "Has Perspective projects"
fi
```

### Analyze a directory

```bash
kindling backup-stats /opt/ignition/data
```

## Null Values

Some fields may be null if the information is not available:
- `meta` - Null if no configuration database
- Individual meta fields - Null if not in database
- `databases`, `devices`, etc. - Null if empty or no database

## Error Cases

### Path does not exist

```bash
kindling backup-stats /nonexistent
```
```
Error: Path does not exist: /nonexistent
```
Exit code: 1

### Invalid backup file

```bash
kindling backup-stats invalid.txt
```
```
Error opening backup: Not a valid ZIP file
```
Exit code: 1

## See Also

- [analyze-directory](./analyze-directory.md) - Quick directory analysis
- [Archive Explorer](../tools/archive-explorer.md) - GUI equivalent
