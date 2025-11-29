# Gemini Instructions for Kindling

This file contains instructions specific to Google Gemini when working with the Kindling codebase.

## Common Instructions

Please refer to [common-ai-instructions.md](./common-ai-instructions.md) for general project guidelines, code style, and development practices.

## Gemini-Specific Guidelines

### Code Analysis

When analyzing code:
1. Consider the full context of the Kindling application
2. Understand that this is a Swing desktop application, not a web app
3. Be aware of JVM-specific considerations (memory, threading)

### Code Generation

1. **Kotlin Focus**: Generate idiomatic Kotlin code, not Java-style Kotlin
2. **Coroutines**: Use `EDT_SCOPE` for UI operations, `Dispatchers.IO` for file I/O
3. **Functional Style**: Prefer functional approaches where appropriate

### Architecture Understanding

- The application uses a plugin-like architecture with `Tool` implementations
- Each tool can open specific file types and provides a specialized view
- CLI mode mirrors GUI functionality with JSON output

### Suggestions for Improvements

When suggesting improvements:
- Consider backward compatibility
- Maintain consistent code style
- Focus on user experience for Ignition administrators
- Keep CLI output machine-readable (JSON format)

### Documentation Generation

When generating documentation:
- Use Docusaurus-compatible Markdown
- Include code examples in Kotlin
- Provide both GUI and CLI usage examples where applicable
