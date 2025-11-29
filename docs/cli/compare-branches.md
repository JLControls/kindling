# compare-branches

Compare two Git branches and output a diff summary in JSON format.

## Synopsis

```bash
kindling compare-branches <repo-path> [--base <branch>] [--compare <branch>]
```

## Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `repo-path` | Yes | Path to the Git repository |

## Options

| Option | Short | Required | Description |
|--------|-------|----------|-------------|
| `--base` | `-b` | No | Base branch for comparison (default: main/master) |
| `--compare` | `-c` | No | Branch to compare against (default: first other branch) |

## Output

```json
{
  "repositoryPath": "/path/to/repo",
  "baseBranch": "main",
  "compareBranch": "develop",
  "availableBranches": ["main", "develop", "feature/new-tags"],
  "changedFiles": [
    {
      "changeType": "MODIFY",
      "oldPath": "projects/MyProject/project.json",
      "newPath": "projects/MyProject/project.json"
    }
  ],
  "totalChanges": 1,
  "additions": 0,
  "deletions": 0,
  "modifications": 1
}
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `repositoryPath` | string | Path to the repository |
| `baseBranch` | string | Name of the base branch |
| `compareBranch` | string | Name of the comparison branch |
| `availableBranches` | string[] | List of all available branches |
| `changedFiles` | object[] | List of changed files |
| `totalChanges` | number | Total number of changed files |
| `additions` | number | Number of added files |
| `deletions` | number | Number of deleted files |
| `modifications` | number | Number of modified files |

### Changed File Object

| Field | Type | Description |
|-------|------|-------------|
| `changeType` | string | Type of change: ADD, DELETE, MODIFY, RENAME, COPY |
| `oldPath` | string? | Original path (null for additions) |
| `newPath` | string? | New path (null for deletions) |

## Examples

### Compare with default branches

```bash
kindling compare-branches /path/to/repo
```

Automatically selects `main` or `master` as base.

### Specify both branches

```bash
kindling compare-branches /path/to/repo --base main --compare feature/new-views
```

### Count additions only

```bash
kindling compare-branches . --base main --compare develop | jq '.additions'
```

### List all changed files

```bash
kindling compare-branches . | jq -r '.changedFiles[].newPath // .changedFiles[].oldPath'
```

### Filter by change type

```bash
kindling compare-branches . | jq '.changedFiles[] | select(.changeType == "ADD")'
```

## Error Cases

### Not a Git repository

```bash
kindling compare-branches /tmp
```
```
Error: Not a Git repository: /tmp
```
Exit code: 1

### Missing branch argument

```bash
kindling compare-branches /repo --base
```
```
Error: --base requires a branch name
```
Exit code: 1

### Cannot determine branches

```bash
kindling compare-branches /empty-repo
```
```
Error: Could not determine branches to compare
```
Exit code: 1

## Default Branch Selection

When `--base` is not specified:
1. Looks for `main`
2. Falls back to `master`
3. Uses the first available branch

When `--compare` is not specified:
1. Selects the first branch that differs from base

## See Also

- [Git Branch Viewer](../tools/git-branch-viewer.md) - GUI equivalent
- [analyze-directory](./analyze-directory.md) - For analyzing directory contents
