#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Upgrade Gradle wrapper to 8.7 (compatible with AGP 8.7.3)
# ================================================================
flutter pub global activate gradle_wrapper 2>/dev/null || true
# Use Flutter's built-in Gradle wrapper upgrade
# Or manually update the Gradle wrapper
python3 << 'PYEOF'
with open('android/gradle/wrapper/gradle-wrapper.properties', 'r') as f:
    content = f.read()
content = content.replace(
    'gradle-7.6.3-all',
    'gradle-8.7-all'
)
with open('android/gradle/wrapper/gradle-wrapper.properties', 'w') as f:
    f.write(content)
print("Gradle wrapper upgraded to 8.7")
PYEOF

# ================================================================
# 2. Patch settings.gradle: update AGP version + add Chaquopy repo
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Update AGP version in settings.gradle to match plugin (8.7.3)
content = content.replace(
    'id "com.android.application" version "7.3.0" apply false',
    'id "com.android.application" version "8.7.3" apply false'
)

# Add Chaquopy repo to pluginManagement repositories
if 'chaquo.com' not in content:
    # Find pluginManagement repositories block
    idx = content.find('pluginManagement {')
    if idx != -1:
        repos_idx = content.find('repositories {', idx)
        if repos_idx != -1:
            # Find the end of the repositories block in pluginManagement
            gradle_plugin_portal_idx = content.find('gradlePluginPortal()', repos_idx)
            if gradle_plugin_portal_idx != -1:
                content = content[:repo_idx] + '        maven { url = uri("https://chaquo.com/maven") }\n' + content[repos_idx:]

# Simpler: just replace mavenCentral() in the first repositories block
# Actually, let's just add Chaquopy repo to the dependencyResolutionManagement
if 'chaquo.com' not in content:
    # Find the second repositories block (in dependencyResolutionManagement)
    parts = content.split('repositories {')
    if len(parts) > 2:
        # Insert Chaquoo repo into the second repositories block
        second_idx = content.find('repositories {', content.find('repositories {') + 1)
        if second_idx != -1:
            content = content[:second_idx + len('repositories {')] + '\n        maven { url = uri("https://chaquo.com/maven") }' + content[second_idx + len('repositories {'):]

with open('android/settings.gradle', 'w') as f:
    f.write(content)
print("settings.gradle patched")
PYEOF

# ================================================================
# 3. Patch project build.gradle: add Chaquopy repo + classpath
# ================================================================
python3 << 'PYEOF'
with open('android/build.gradle', 'r') as f:
    content = f.read()

# Replace entire content with Chaquopy-enabled build.gradle
new_content = '''buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url "https://chaquo.com/maven" }
    }
    dependencies {
        classpath "com.chaquo.python:gradle:17.0.0"
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://chaquo.com/maven" }
    }
}

rootProject.buildDir = "../build"
subprojects {
    project.buildDir = "${rootProject.buildDir}/${project.name}"
    project.evaluationDependsOn(":app")
}

tasks.register("clean", Delete) {
    delete rootProject.buildDir
}
'''

with open('android/build.gradle', 'w') as f:
    f.write(new_content)
print("project build.gradle patched")
PYEOF

# ================================================================
# 4. Patch app build.gradle
# ================================================================
python3 << 'PYEOF'
with open('android/app/build.gradle', 'r') as f:
    content = f.read()

# Fix namespace and applicationId
content = content.replace('com.example.mpltyop_flutter', 'com.cheater345.mpltyop')

# Change Java version to 11
content = content.replace('JavaVersion.VERSION_1_8', 'JavaVersion.VERSION_11')

# Change minSdk to 24 for Chaquopy
content = content.replace(
    'minSdk = flutter.minSdkVersion',
    'minSdk = 24'
)

# Update compileSdk to 35
content = content.replace(
    'compileSdk = flutter.compileSdkVersion',
    'compileSdk = 35'
)

# Add Chaquopy plugin via apply (not plugins DSL)
content = content.replace(
    '}\n\ndef localProperties = new Properties()',
    '}\n\napply plugin: "com.chaquo.python"\n\ndef localProperties = new Properties()'
)

# Add Chaquopy python config OUTSIDE the android block
content = content.replace(
    '}\n\nflutter {',
    '''}

python {
    version = "3.11"
    buildType = "release"
    pip {
        install "ytmusicapi==1.8.0"
        install "yt-dlp==2024.1.2"
        install "requests==2.31.0"
    }
}

flutter {'''
)

with open('android/app/build.gradle', 'w') as f:
    f.write(content)
print("app build.gradle patched")
PYEOF

echo "=== Gradle wrapper ==="
cat android/gradle/wrapper/gradle-wrapper.properties
echo ""
echo "=== settings.gradle ==="
cat android/settings.gradle
echo ""
echo "=== project build.gradle ==="
cat android/build.gradle
echo ""
echo "=== app build.gradle ==="
cat android/app/build.gradle
