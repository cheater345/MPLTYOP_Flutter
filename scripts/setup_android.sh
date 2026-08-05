#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

export FLUTTER_ROOT="${FLUTTER_ROOT:-$(which flutter | xargs dirname 2>/dev/null || echo /opt/hostedtoolcache/flutter/stable)}"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Patch settings.gradle: update AGP version + add Chaquopy
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Update AGP version to 8.0.2 (compatible with Gradle 7.6.3)
content = content.replace(
    'id "com.android.application" version "7.3.0" apply false',
    'id "com.android.application" version "8.0.2" apply false'
)
content = content.replace(
    'id "com.android.library" version "7.3.0" apply false',
    'id "com.android.library" version "8.0.2" apply false'
)

# Add Chaquopy repo to pluginManagement
if 'chaquo.com' not in content:
    content = content.replace(
        'google()\n        mavenCentral()\n        gradlePluginPortal()',
        'google()\n        mavenCentral()\n        gradlePluginPortal()\n        maven { url = uri("https://chaquo.com/maven") }',
        1
    )

with open('android/settings.gradle', 'w') as f:
    f.write(content)
print("settings.gradle patched")
PYEOF

# ================================================================
# 2. Patch project build.gradle: add Chaquopy repo + classpath
# ================================================================
python3 << 'PYEOF'
new_content = '''buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url "https://chaquo.com/maven" }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:8.0.2"
        classpath "com.chaquo.python:gradle:12.0.0"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20"
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
# 3. Patch app build.gradle
# Use plugins DSL for Flutter (handles flutterRoot), apply Chaquopy via buildscript classpath
# ================================================================
python3 << 'PYEOF'
with open('android/app/build.gradle', 'r') as f:
    content = f.read()

# Fix namespace and applicationId
content = content.replace('com.example.mpltyop_flutter', 'com.cheater345.mpltyop')

# Change Java version to 11
content = content.replace('JavaVersion.VERSION_1_8', 'JavaVersion.VERSION_11')

# Change minSdk to 24 for Chaquopy
content = content.replace('minSdk = flutter.minSdkVersion', 'minSdk = 24')

# Update compileSdk to 34
content = content.replace('compileSdk = flutter.compileSdkVersion', 'compileSdk = 34')

# Add Chaquopy python config inside android block (after namespace)
content = content.replace(
    'namespace = "com.cheater345.mpltyop"',
    'namespace = "com.cheater345.mpltyop"\n\n    python {\n        version = "3.11"\n        buildType = "release"\n        pip {\n            install "ytmusicapi==1.8.0"\n            install "yt-dlp==2024.1.2"\n            install "requests==2.31.0"\n        }\n    }'
)

# Apply Chaquopy plugin via apply plugin (after the plugins block closes, before android)
# Insert apply plugin right before 'android {'
content = content.replace(
    '\nandroid {',
    '\napply plugin: "com.chaquo.python"\n\nandroid {'
)

# Add multidex dependency
if 'multidex' not in content:
    # Find dependencies block or implementation line
    content = content.replace(
        'implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"',
        'implementation "androidx.multidex:multidex:2.0.1"\n    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"'
    )

with open('android/app/build.gradle', 'w') as f:
    f.write(content)
print("app build.gradle patched")
PYEOF

echo "=== settings.gradle ==="
cat android/settings.gradle
echo ""
echo "=== project build.gradle ==="
cat android/build.gradle
echo ""
echo "=== app build.gradle ==="
cat android/app/build.gradle
