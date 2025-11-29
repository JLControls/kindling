# IDB Viewer

The IDB Viewer opens Ignition's internal SQLite database files (`.idb`) and provides SQL query capabilities with special handling for common Ignition data types.

## Features

### Table Browser

- Lists all tables in the database
- Shows row counts and schema information
- Quick navigation via table list sidebar

### SQL Query Interface

- Execute arbitrary SQL queries
- Syntax highlighting for SQL
- Query history and favorites

### Special Handling

The IDB Viewer has enhanced support for:

#### Metrics Files
- Automatic chart generation for time-series data
- Performance metric visualization

#### System Logs
- Log level filtering
- Timestamp parsing and sorting
- Logger hierarchy display

#### Configuration Database
- Image preview for stored images
- Tag configuration tree view
- Project resource browsing

## Usage

### Basic Query

1. Open an `.idb` file
2. Select a table from the sidebar
3. View results in the data grid
4. Or write custom SQL in the query panel

### Exporting Data

- Export query results to CSV
- Copy rows to clipboard
- Export to Excel format

## CLI Usage

```bash
kindling analyze /path/to/database.idb
```

## Common Queries

### View Recent Logs
```sql
SELECT * FROM logging_event 
ORDER BY timestmp DESC 
LIMIT 100
```

### Count Tags by Provider
```sql
SELECT provider, COUNT(*) 
FROM tag_config 
GROUP BY provider
```

## Tips

- The `.idb` file in a gateway backup contains configuration data
- Metrics `.idb` files are found in the `data/` directory
- Use `PRAGMA table_info(tablename)` to inspect schema
