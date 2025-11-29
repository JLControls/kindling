# Thread Viewer

The Thread Viewer parses and analyzes Ignition thread dump files, helping diagnose performance issues and deadlocks.

## Supported Formats

- **JSON format** - Modern Ignition thread dump format
- **Plain text format** - Traditional JVM thread dump format

## Features

### Thread Aggregation

When multiple thread dumps from the same system are opened together, they are automatically aggregated to show:
- Thread state changes over time
- Consistent blocking patterns
- Resource contention issues

### Thread State Visualization

Threads are color-coded by state:
- 🟢 **RUNNABLE** - Actively executing
- 🟡 **WAITING** - Waiting for a monitor
- 🟠 **TIMED_WAITING** - Waiting with timeout
- 🔴 **BLOCKED** - Blocked on a monitor

### Stack Trace Analysis

- Full stack trace display for each thread
- Syntax highlighting for package names
- Link detection for Ignition-specific classes

## Usage

1. Open a thread dump file (`.json` or `.txt`)
2. Or drag multiple dumps for aggregated analysis
3. Use the filter sidebar to narrow down threads
4. Click a thread to view its full stack trace

## CLI Usage

```bash
kindling analyze /path/to/thread-dump.json
```

Output:
```json
{
  "path": "/path/to/thread-dump.json",
  "type": "json",
  "isIgnitionConfig": false,
  "isGitRepository": false
}
```

## Tips

- Export thread dumps from the Gateway Status page
- Take multiple dumps 5-10 seconds apart for better analysis
- Look for threads stuck in the same state across dumps
