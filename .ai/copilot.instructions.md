# GitHub Copilot Instructions for Kindling

This file contains instructions specific to GitHub Copilot when working with the Kindling codebase.

## Common Instructions

Please refer to [common-ai-instructions.md](./common-ai-instructions.md) for general project guidelines, code style, and development practices.

## Copilot-Specific Guidelines

### Code Generation

1. **Follow Existing Patterns**: When generating new code, follow patterns from existing similar files
2. **Use Kotlin Idioms**: Prefer Kotlin-specific features like `apply`, `let`, `also`, `run`
3. **Null Safety**: Always handle nullability properly using `?.`, `?:`, and safe casts

### Suggested Completions

When suggesting completions:
- Prefer extension functions over utility classes
- Use data classes for DTOs
- Follow the existing naming conventions (camelCase for functions, PascalCase for classes)

### Testing Suggestions

When generating tests:
- Use Kotest FunSpec style: `test("description") { ... }`
- Use `shouldBe`, `shouldBeTrue`, `shouldBeFalse` for assertions
- Create and clean up temp files properly

### UI Code

When generating Swing UI code:
- Use MigLayout for layouts
- Apply FlatLaf styling via `putClientProperty("FlatLaf.styleClass", "...")`
- Use `FlatSVGIcon` for icons

### JSON Serialization

- Use `kotlinx.serialization` with `@Serializable` annotations
- Use the project's configured `Json` instance with `prettyPrint = true`
