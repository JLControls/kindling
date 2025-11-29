# Cache Viewer

The Cache Viewer opens the HSQLDB files that contain Ignition's Store and Forward disk cache.

## Overview

Store and Forward caching temporarily stores data when database connections are unavailable. This tool allows inspection of cached data.

## Features

### Data Deserialization

- Attempts to deserialize Java-serialized data
- Falls back to string explanation if classes are missing
- Shows data types and values

### Cache Statistics

- Number of cached entries
- Data size information
- Timestamp ranges

### Table Browser

- View all cache tables
- Inspect individual entries
- SQL query interface

## Usage

1. Locate the S&F cache directory
2. Open the HSQLDB files
3. Browse cached data
4. Export or clear if needed

## Cache Location

Default locations:
- **Windows**: `C:\Program Files\Inductive Automation\Ignition\data\store_and_forward\`
- **Linux**: `/usr/local/bin/ignition/data/store_and_forward/`

## CLI Usage

```bash
kindling analyze /path/to/cache.script
```

## Troubleshooting

### Missing Classes

If you see deserialization errors:
1. Note the missing class name
2. [File an issue](https://github.com/inductiveautomation/kindling/issues) with details
3. The raw serialized data is still displayed

### Large Caches

For very large caches:
- Use filtering to narrow down
- Export to CSV for external analysis
- Consider time-based queries

## Tips

- S&F caches grow when databases are unreachable
- Check cache after network/database issues
- Clear caches after resolving underlying issues
