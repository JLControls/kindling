# Log Viewer

The Log Viewer opens and analyzes Ignition wrapper.log files, providing filtering and search capabilities.

## Supported Formats

- `wrapper.log` - Standard Ignition log format
- Multiple log files can be opened together

## Features

### Multi-File Aggregation

When multiple log files are selected, they are:
- Automatically sequenced by timestamp
- Presented as a single unified view
- Gaps between files are indicated

### Filtering

- Filter by log level (TRACE, DEBUG, INFO, WARN, ERROR)
- Filter by logger name
- Filter by message content
- Time range filtering

### Log Level Colors

- **TRACE** - Gray
- **DEBUG** - Light blue
- **INFO** - Black
- **WARN** - Orange
- **ERROR** - Red

### Stack Trace Display

- Expandable stack traces
- Syntax highlighting for class names
- Copy stack trace to clipboard

## Usage

1. Open one or more `wrapper.log` files
2. Use the filter sidebar to narrow down entries
3. Click entries to view full details
4. Use search to find specific messages

## CLI Usage

```bash
kindling analyze /path/to/wrapper.log
```

## Tips

- Collect logs from both Gateway and Designer
- Include logs before and after the issue
- Look for `ERROR` entries first, then check preceding `WARN` entries
- Stack traces often contain the root cause

## Common Patterns

### Finding Errors

Look for log entries with level `ERROR`:
- Database connection failures
- Script execution errors
- OPC communication issues

### Tracing Issues

1. Find the error entry
2. Look at entries just before the error
3. Check for warning patterns
4. Identify the triggering event

### Performance Analysis

Look for:
- Slow query warnings
- Memory pressure messages
- Thread pool exhaustion
