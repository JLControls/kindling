# CLI Mode Overview

Kindling provides a command-line interface (CLI) for automation, scripting, and LLM integration. All commands output structured JSON for easy parsing.

## Installation

The CLI is included with the standard Kindling installation. Access it via:

```bash
kindling <command> [options]
```

Or run the CLI entry point directly:
```bash
java -cp kindling.jar io.github.inductiveautomation.kindling.cli.KindlingCli <command>
```

## Available Commands

| Command | Description |
|---------|-------------|
| [`analyze-directory`](./analyze-directory.md) | Analyze Ignition configuration directories |
| [`compare-branches`](./compare-branches.md) | Compare Git branches |
| [`analyze`](./analyze.md) | Analyze any supported file |
| [`backup-stats`](./backup-stats.md) | Get gateway backup statistics |
| `help` | Show help message |
| `version` | Show version information |

## Common Options

```bash
kindling help              # Show usage information
kindling --help            # Same as above
kindling version           # Show version
kindling --version         # Same as above
```

## Output Format

All commands output JSON for easy parsing:

```bash
kindling backup-stats gateway.gwbk | jq '.meta.gatewayName'
# "Production Gateway"
```

### Error Output

Errors are written to stderr with a non-zero exit code:
```bash
kindling analyze-directory /nonexistent
# Error: Path does not exist: /nonexistent
# Exit code: 1
```

## Use Cases

### Automation Scripts

```bash
#!/bin/bash
# Check if directory is valid Ignition config
result=$(kindling analyze-directory /path/to/config)
if echo "$result" | jq -e '.isIgnitionConfigDirectory' > /dev/null; then
    echo "Valid Ignition configuration"
fi
```

### LLM Integration

The JSON output is designed for LLM consumption:
```python
import subprocess
import json

result = subprocess.run(
    ["kindling", "backup-stats", "gateway.gwbk"],
    capture_output=True,
    text=True
)
data = json.loads(result.stdout)
print(f"Gateway: {data['meta']['gatewayName']}")
print(f"Projects: {data['projects']['projectCount']}")
```

### CI/CD Pipelines

```yaml
# GitHub Actions example
- name: Validate Configuration
  run: |
    result=$(kindling analyze-directory ./config)
    if ! echo "$result" | jq -e '.isIgnitionConfigDirectory'; then
      echo "Invalid configuration directory"
      exit 1
    fi
```

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Error (invalid arguments, file not found, etc.) |

## Environment Variables

Currently, no environment variables are used. Configuration is done via command-line arguments.

## Tips

- Pipe output through `jq` for formatted display
- Use `2>/dev/null` to suppress error messages
- Combine with shell scripts for complex workflows
