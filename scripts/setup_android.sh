#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Upgrade Gradle wrapper to 8.9 (AGP 8.7.3 requires Gradle 8.9)
# ================================================================
python3 << 'PYEOF'
with open('android/gradle/wrapper/gradle-wrapper.properties', 'r') as f:
    content = f.read()
content = content.replace(
    'gradle-7.6.3-all',
    'gradle-8.9-all'
)
content = content.replace(
    'gradle-7.6.3-bin',
    'gradle-8.9-bin'
)
with open('android/gradle/wrapper/gradle-wrapper.properties', 'w') as f:
    f.write(content)
print("Gradle wrapper upgraded to 8.9")
PYEOF

# ================================================================
# 2. Patch settings.gradle: update AGP version + add Chaquopy
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Update AGP version in settings.gradle to match plugin (8.7.3)
content = content.replace(
    'id "com.android.application" version "7.3.0" apply false',
    'id "com.android.application" version "8.7.3" apply false'
)
content = content.replace(
    'id "com.android.library" version "7.3.0" apply false',
    'id "com.android.library" version "8.7.3" apply false'
)

# Add Chaquopy to pluginManagement (for plugins DSL) + dependencyResolutionManagement (for repos)
if 'chaquo.com' not in content:
    # Find pluginManagement repositories block and add Chaquopy repo
    # The Flutter-generated settings.gradle has pluginManagement with google(), mavenCentral(), gradlePluginPortal()
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
# 3. Patch project build.gradle: add Chaquopy repo + classpath
# ================================================================
python3 << 'PYEOF'
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

# Add Chaquopy to plugins block
content = content.replace(
    'id "kotlin-android"',
    'id "kotlin-android"\n    id "com.chaquo.python"'
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
