#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Upgrade Gradle wrapper to 8.7 (compatible with AGP 8.7.3)
# ================================================================
flutter pub global activate gradle_wrapper 2>/dev/null
cd android && flutter pub global run gradle_wrapper --gradle-version 8.7 && cd ..

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
    # Find the first repositories block and add Chaquopy after google()
    content = content.replace(
        'google()\n',
        'google()\n        maven { url = uri("https://chaquo.com/maven") }\n',
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
