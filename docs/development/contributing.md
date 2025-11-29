# Contributing to Kindling

Thank you for your interest in contributing to Kindling! This guide covers how to contribute effectively.

## Ways to Contribute

- **Bug Reports** - Report issues you encounter
- **Feature Requests** - Suggest new tools or improvements
- **Code Contributions** - Submit pull requests
- **Documentation** - Improve docs and examples
- **Test Files** - Provide sample files for testing

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Set up development environment (see [Getting Started](./getting-started.md))
4. Create a feature branch

```bash
git checkout -b feature/my-new-feature
```

## Code Guidelines

### Kotlin Style

- Follow Kotlin coding conventions
- Use Spotless for formatting: `./gradlew spotlessApply`
- Prefer idiomatic Kotlin over Java-style code

### Comments

- Only add comments when matching existing file style
- Or when explaining complex logic
- Self-documenting code is preferred

### Error Handling

- Use `ToolOpeningException` for user-facing errors
- Use `runCatching` for graceful failures
- Log errors appropriately

## Pull Request Process

### Before Submitting

1. Run tests: `./gradlew test`
2. Check formatting: `./gradlew spotlessCheck`
3. Build successfully: `./gradlew build`
4. Update documentation if needed

### PR Guidelines

- Clear, descriptive title
- Explain what and why in description
- Reference related issues
- Keep changes focused

### Review Process

1. PR triggers CI checks
2. Maintainers review code
3. Address feedback
4. Merge when approved

## Development Workflow

### Feature Development

1. Create feature branch from `main`
2. Implement with tests
3. Ensure CI passes
4. Submit PR

### Bug Fixes

1. Create issue (if not exists)
2. Create fix branch
3. Add test reproducing bug
4. Fix and verify
5. Submit PR

## Testing

### Writing Tests

- Use Kotest FunSpec style
- Create temp files/directories for tests
- Clean up resources properly
- Test both success and error cases

```kotlin
class MyTest : FunSpec({
    test("should handle valid input") {
        // Arrange
        val input = createTestInput()
        
        // Act
        val result = process(input)
        
        // Assert
        result.shouldBe(expected)
    }
    
    test("should throw for invalid input") {
        shouldThrow<ToolOpeningException> {
            process(invalidInput)
        }
    }
})
```

### Test Data

- Include sample files in `src/test/resources/`
- Use minimal representative examples
- Document file format/version

## Documentation

### Code Documentation

- Add KDoc for public APIs
- Document complex algorithms
- Keep docs up to date

### User Documentation

- Update `docs/` for new features
- Include usage examples
- Add CLI documentation for new commands

## Commit Messages

Follow conventional commits:

```
feat: add new thread visualization
fix: handle empty thread dumps
docs: update CLI usage examples
test: add tests for git viewer
refactor: simplify branch resolution
```

## Issue Reporting

### Bug Reports

Include:
- Kindling version
- OS and version
- Steps to reproduce
- Expected vs actual behavior
- Sample file (if possible)

### Feature Requests

Include:
- Use case description
- Proposed solution
- Alternative approaches considered

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help newcomers get started

## Questions?

- Open a GitHub issue
- Check existing discussions
- Review documentation

## License

By contributing, you agree that your contributions will be licensed under the project's license.
