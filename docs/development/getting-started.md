# Getting Started with Development

This guide covers setting up a development environment for contributing to Kindling.

## Prerequisites

- **JDK 21** - Amazon Corretto recommended, but any JDK 21 works
- **Git** - For version control
- **IDE** - IntelliJ IDEA recommended (Kotlin support)

## Clone the Repository

```bash
git clone https://github.com/inductiveautomation/kindling.git
cd kindling
```

## Build and Run

### Using Gradle Wrapper

```bash
# Build (skip tests for faster build)
./gradlew build -x test

# Run the application
./gradlew run

# Run tests
./gradlew test

# Check code formatting
./gradlew spotlessCheck

# Fix formatting issues
./gradlew spotlessApply
```

### Using an IDE

1. Open the project in IntelliJ IDEA
2. Wait for Gradle sync to complete
3. Run the `MainPanel` class directly

## Project Structure

```
kindling/
├── src/
│   ├── main/
│   │   ├── kotlin/          # Kotlin source code
│   │   ├── java/            # Java source (minimal)
│   │   └── resources/       # Icons, configs
│   └── test/
│       ├── kotlin/          # Test source
│       └── resources/       # Test fixtures
├── docs/                    # Docusaurus documentation
├── .ai/                     # AI assistant instructions
├── build.gradle.kts         # Build configuration
├── settings.gradle.kts      # Gradle settings
└── gradle/
    └── libs.versions.toml   # Dependency versions
```

## Configuration

### Build Configuration

- `build.gradle.kts` - Main build configuration
- `gradle/libs.versions.toml` - Centralized version catalog
- `settings.gradle.kts` - Gradle settings and plugins

### Code Style

The project uses [Spotless](https://github.com/diffplug/spotless) with ktlint for Kotlin formatting.

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply
```

## Testing

Tests use [Kotest](https://kotest.io/) framework:

```kotlin
class MyToolTest : FunSpec({
    test("should do something") {
        val result = myFunction()
        result.shouldBe(expected)
    }
})
```

Run tests:
```bash
./gradlew test
```

## Building Releases

Kindling uses [Conveyor](https://www.hydraulic.software/) for cross-platform packaging:

```bash
# Build installers
./gradlew convey
```

## Next Steps

- [Architecture Overview](./architecture.md) - Understanding the codebase
- [Adding a New Tool](./adding-tools.md) - Creating new file viewers
- [Contributing](./contributing.md) - Contribution guidelines
