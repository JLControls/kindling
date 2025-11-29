# analyze-directory

Analyze an Ignition configuration directory and output statistics in JSON format.

## Synopsis

```bash
kindling analyze-directory <path>
```

## Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `path` | Yes | Path to the directory to analyze |

## Output

```json
{
  "path": "/path/to/directory",
  "isIgnitionConfigDirectory": true,
  "hasProjectsDirectory": true,
  "hasConfigDirectory": true,
  "hasIdb": false
}
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `path` | string | Absolute path to the directory |
| `isIgnitionConfigDirectory` | boolean | Whether this appears to be an Ignition config directory |
| `hasProjectsDirectory` | boolean | Whether a `projects/` subdirectory exists |
| `hasConfigDirectory` | boolean | Whether a `config/` subdirectory exists |
| `hasIdb` | boolean | Whether `db_backup_sqlite.idb` exists |

## Examples

### Analyze a valid configuration

```bash
kindling analyze-directory /opt/ignition/data
```

```json
{
  "path": "/opt/ignition/data",
  "isIgnitionConfigDirectory": true,
  "hasProjectsDirectory": true,
  "hasConfigDirectory": true,
  "hasIdb": true
}
```

### Check if directory is valid

```bash
if kindling analyze-directory ./config | jq -e '.isIgnitionConfigDirectory'; then
  echo "Valid Ignition configuration"
fi
```

### Analyze extracted gateway backup

```bash
# First extract the backup
unzip gateway.gwbk -d /tmp/backup

# Then analyze
kindling analyze-directory /tmp/backup
```

## Error Cases

### Directory does not exist

```bash
kindling analyze-directory /nonexistent
```
```
Error: Path does not exist: /nonexistent
```
Exit code: 1

### Path is a file, not directory

```bash
kindling analyze-directory /path/to/file.txt
```
```
Error: Path is not a directory: /path/to/file.txt
```
Exit code: 1

## Detection Logic

A directory is considered an Ignition configuration directory if it contains any of:
- A `projects/` subdirectory
- A `config/` subdirectory
- A `db_backup_sqlite.idb` file

If none of these are present, `isIgnitionConfigDirectory` will be `false`, but the command will still succeed with a warning.

## See Also

- [backup-stats](./backup-stats.md) - For detailed statistics including projects and databases
- [Directory Viewer](../tools/directory-viewer.md) - GUI equivalent
