# Git Branch Viewer

The Git Branch Viewer allows you to compare Ignition configurations across Git branches, useful for reviewing changes before merging or understanding configuration drift.

## Requirements

- A Git repository containing Ignition configuration
- Works best with Ignition 8.3+ file-structure based configurations

## Features

### Branch Comparison

- Select two branches to compare
- View list of changed files
- Side-by-side diff view for each file

### Change Type Indicators

- 🟢 **[+] Added** - New files
- 🔴 **[-] Deleted** - Removed files  
- 🔵 **[M] Modified** - Changed files
- **[R] Renamed** - Moved files
- **[C] Copied** - Copied files

### Syntax Highlighting

Diff views support syntax highlighting for:
- JSON (tag configuration, project.json)
- XML (Ignition XML exports)
- Python (scripting)
- Properties files

## Usage

1. Open a directory containing a `.git` folder
2. Select the base branch (usually `main` or `master`)
3. Select the comparison branch
4. Click "Compare Branches"
5. Select files from the changed files list to view diffs

## CLI Usage

### Compare Branches

```bash
kindling compare-branches /path/to/repo --base main --compare develop
```

Output:
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
    },
    {
      "changeType": "ADD",
      "oldPath": null,
      "newPath": "config/tags/default/NewTag.json"
    }
  ],
  "totalChanges": 2,
  "additions": 1,
  "deletions": 0,
  "modifications": 1
}
```

### Options

| Option | Short | Description |
|--------|-------|-------------|
| `--base` | `-b` | Base branch for comparison |
| `--compare` | `-c` | Branch to compare against |

## Use Cases

### Pre-Merge Review

Before merging a feature branch:
```bash
kindling compare-branches . --base main --compare feature/new-views
```

### Configuration Audit

Compare production to staging:
```bash
kindling compare-branches /configs --base production --compare staging
```

### Understanding Changes

Review what changed in a release:
```bash
kindling compare-branches . --base v8.1.19 --compare v8.1.20
```

## Tips

- Default branches (`main`, `master`) are auto-selected
- Branch names can include refs like `refs/heads/branch-name`
- For large repositories, initial comparison may take a moment
