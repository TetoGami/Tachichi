# TachiyomiSY Android Manga Reader

TachiyomiSY is an Android manga reader application built with Kotlin, Gradle, and Android SDK. This is a fork of Mihon (formerly Tachiyomi) with additional features and customizations.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Prerequisites and Setup
- Requires JDK 17 (OpenJDK Temurin recommended)
- Android SDK with build tools
- Gradle 8.14.1 (wrapper provided)
- No additional setup required for dev builds

### Essential Build Commands
**NEVER CANCEL any of these commands - they take time but will complete:**

- Code formatting check: `./gradlew spotlessCheck` -- takes 3 minutes. NEVER CANCEL. Set timeout to 5+ minutes.
- Dev debug build: `./gradlew assembleDevDebug` -- takes 6-7 minutes. NEVER CANCEL. Set timeout to 10+ minutes.
- Unit tests (debug): `./gradlew testDebugUnitTest` -- takes 12 seconds.
- Unit tests (release): `./gradlew testReleaseUnitTest` -- takes 30 seconds.
- Clean build: `./gradlew clean` -- takes 3 seconds.

### Build Variants
The project has three flavors with different requirements:
- **dev**: Works without any setup, has stub Firebase implementations
- **standard**: Requires `google-services.json` file for Firebase (production builds)
- **fdroid**: F-Droid variant, also requires Firebase setup despite the name

**IMPORTANT**: Only use dev variants (assembleDevDebug, assembleDevRelease) unless you have the required Firebase configuration files.

### Validation and CI Commands
Always run these before committing changes:
- `./gradlew spotlessCheck` -- validates code formatting (required by CI)
- `./gradlew assembleDevDebug` -- builds the app successfully 
- `./gradlew testDebugUnitTest` -- runs unit tests

The CI workflow uses these exact commands:
- PR builds: `./gradlew spotlessCheck assembleDevDebug`
- Release builds: `./gradlew spotlessCheck assembleStandardRelease testReleaseUnitTest testStandardReleaseUnitTest` (requires Firebase setup)

### Android Lint
- Run with: `./gradlew lint` (takes ~1.5 minutes)
- Lint is configured as non-blocking (`abortOnError = false`)
- Expect some lint warnings - this is normal and acceptable

## Project Structure

### Module Organization
- `:app` - Main Android application module
- `:core:common` - Core shared utilities
- `:core-metadata` - Metadata handling
- `:data` - Data layer with repositories
- `:domain` - Business logic and use cases  
- `:i18n` - Internationalization resources
- `:i18n-sy` - TachiyomiSY-specific translations
- `:presentation-core` - Shared presentation components
- `:presentation-widget` - Widget implementations
- `:source-api` - Source plugin API
- `:source-local` - Local source implementation
- `:macrobenchmark` - Performance benchmarking

### Key Directories
- `app/src/main/java/` - Main application code
- `app/src/dev/java/mihon/core/firebase/` - Dev flavor Firebase stubs
- `app/src/standard/java/mihon/core/firebase/` - Standard flavor Firebase implementation
- `buildSrc/` - Custom Gradle plugins and build logic
- `gradle/` - Version catalogs and Gradle configuration

### Build System
- Uses Gradle with version catalogs (`gradle/*.versions.toml`)
- Custom build plugins in `buildSrc/`
- Spotless for code formatting
- Multiple build variants for different distribution channels

## Common Development Tasks

### Making Code Changes
1. Always run `./gradlew spotlessCheck` first to see current formatting state
2. Make your changes
3. Run `./gradlew spotlessCheck` to verify formatting (auto-fixes most issues)
4. Run `./gradlew assembleDevDebug` to verify builds work
5. Run `./gradlew testDebugUnitTest` to verify tests pass

### Firebase Setup (for standard/fdroid builds)
Standard and fdroid variants require `google-services.json` in `app/` directory. For development:
- Use dev variant which has stub implementations
- See CONTRIBUTING.md for full Firebase setup instructions if needed

### Testing Changes
- Unit tests: `./gradlew testDebugUnitTest testReleaseUnitTest`
- Build verification: `./gradlew assembleDevDebug`
- Code formatting: `./gradlew spotlessCheck`
- Android lint (optional): `./gradlew lint`

### Build Outputs
Successful dev builds create APKs in:
- `app/build/outputs/apk/dev/debug/`
- Multiple architecture variants: universal, arm64-v8a, armeabi-v7a, x86, x86_64

## Troubleshooting

### Common Issues
- **Firebase missing errors**: Use dev variant instead of standard/fdroid
- **Lint failures**: Expected and non-blocking, safe to ignore for development
- **Long build times**: Normal, especially first build (downloads dependencies)
- **Multiple format warnings**: Normal for internationalization strings

### Performance Notes
- First build downloads dependencies (~6-7 minutes)
- Subsequent builds are faster due to caching
- Spotless check is incremental and faster on subsequent runs
- Clean builds reset all caches

### Build Requirements
- Minimum: JDK 17, Android SDK
- Memory: Configure with `-Xmx4g` (already set in gradle.properties)
- Network: Required for dependency downloads on first build

## Validation Checklist
Before submitting changes, always verify:
- [ ] `./gradlew spotlessCheck` passes
- [ ] `./gradlew assembleDevDebug` succeeds
- [ ] `./gradlew testDebugUnitTest` passes
- [ ] Changes are minimal and focused
- [ ] No accidental Firebase configuration committed