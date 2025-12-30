# analyze

Analyze a file or directory and output type information in JSON format.

## Synopsis

```bash
kindling analyze <file-path>
```

## Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `file-path` | Yes | Path to the file or directory to analyze |

## Output

```json
{
  "path": "/path/to/file",
  "type": "json",
  "isIgnitionConfig": false,
  "isGitRepository": false
}
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `path` | string | Absolute path to the file |
| `type` | string | File type (extension or "directory"/"unknown") |
| `isIgnitionConfig` | boolean | Whether this is an Ignition configuration file/directory |
| `isGitRepository` | boolean | Whether this directory contains a Git repository |

## Examples

### Analyze a JSON file

```bash
kindling analyze /path/to/thread-dump.json
```

```json
{
  "path": "/path/to/thread-dump.json",
  "type": "json",
  "isIgnitionConfig": false,
  "isGitRepository": false
}
```

### Analyze a gateway backup

```bash
kindling analyze /path/to/gateway.gwbk
```

```json
{
  "path": "/path/to/gateway.gwbk",
  "type": "gwbk",
  "isIgnitionConfig": true,
  "isGitRepository": false
}
```

### Analyze a directory

```bash
kindling analyze /path/to/ignition-config
```

```json
{
  "path": "/path/to/ignition-config",
  "type": "directory",
  "isIgnitionConfig": true,
  "isGitRepository": false
}
```

### Analyze a Git repository

```bash
kindling analyze /path/to/config-repo
```

```json
{
  "path": "/path/to/config-repo",
  "type": "directory",
  "isIgnitionConfig": true,
  "isGitRepository": true
}
```

### Check file type programmatically

```bash
filetype=$(kindling analyze myfile | jq -r '.type')
case $filetype in
  gwbk) echo "Gateway backup" ;;
  idb)  echo "Ignition database" ;;
  json) echo "JSON file" ;;
  *)    echo "Other: $filetype" ;;
esac
```

## File Type Detection

The `type` field contains:
- For files: the lowercase file extension
- For directories: `"directory"`
- For unknown/special files: `"unknown"`

### Ignition Config Detection

`isIgnitionConfig` is `true` for:
- Files with `.gwbk` extension
- Directories containing Ignition configuration markers

### Git Repository Detection

`isGitRepository` is `true` for:
- Directories containing a `.git` subdirectory

## Error Cases

### File does not exist

```bash
kindling analyze /nonexistent
```
```
Error: File does not exist: /nonexistent
```
Exit code: 1

## Use Cases

### Routing in automation scripts

```bash
#!/bin/bash
file=$1
info=$(kindling analyze "$file")
type=$(echo "$info" | jq -r '.type')

case $type in
  gwbk)
    kindling backup-stats "$file"
    ;;
  directory)
    if echo "$info" | jq -e '.isGitRepository' > /dev/null; then
      kindling compare-branches "$file"
    else
      kindling analyze-directory "$file"
    fi
    ;;
  *)
    echo "File type: $type"
    ;;
esac
```

## See Also

- [analyze-directory](./analyze-directory.md) - Detailed directory analysis
- [backup-stats](./backup-stats.md) - Gateway backup statistics
