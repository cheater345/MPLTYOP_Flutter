#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Patch settings.gradle: add Chaquopy to existing pluginManagement block
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Check if Chaquopy is already referenced
if 'chaquo.com' not in content:
    # Find the existing pluginManagement block's repositories and add Chaquopy repo there
    # The Flutter-generated settings.gradle has:
    # pluginManagement {
    #     repositories {
    #         google()
    #         mavenCentral()
    #         gradlePluginPortal()
    #     }
    # }
    # We need to add Chaquopy repo + classpath to the buildscript
    
    # Add a buildscript block with Chaquopy classpath before the existing pluginManagement
    # Actually, we need to merge into existing pluginManagement
    # The approach: find the first 'pluginManagement {' and inject buildscript inside it
    
    content = content.replace(
        'pluginManagement {\n    def flutterSdkPath = {',
        '''pluginManagement {
    buildscript {
        repositories {
            google()
            mavenCentral()
            maven { url = uri("https://chaquo.com/maven") }
        }
        dependencies {
            classpath("com.chaquo.python:gradle:12.0.0")
        }
    }

    def flutterSdkPath = {''',
        1  # Only replace first occurrence
    )

with open('android/settings.gradle', 'w') as f:
    f.write(content)
print("settings.gradle patched")
PYEOF

# ================================================================
# 2. Patch project build.gradle: add Chaquopy to allprojects repos
# ================================================================
sed -i 's|mavenCentral()|mavenCentral()\n        maven { url "https://chaquo.com/maven" }|g' android/build.gradle

# ================================================================
# 3. Patch app build.gradle
# ================================================================
python3 << 'PYEOF'
with open('android/app/build.gradle', 'r') as f:
    content = f.read()

# Fix namespace and applicationId
content = content.replace('com.example.mpltyop_flutter', 'com.cheater345.mpltyop')

# Add Chaquopy plugin after kotlin-android
content = content.replace(
    'id "kotlin-android"',
    'id "kotlin-android"\n    id "com.chaquo.python"'
)

# Add Chaquopy python config after namespace line
content = content.replace(
    'namespace = "com.cheater345.mpltyop"',
    '''namespace = "com.cheater345.mpltyop"

    python {
        version = "3.11"
        buildType = "release"
        pip {
            install "ytmusicapi==1.8.0"
            install "yt-dlp==2024.1.2"
            install "requests==2.31.0"
        }
    }'''
)

# Change Java version to 11 (for Chaquopy compatibility)
content = content.replace('JavaVersion.VERSION_1_8', 'JavaVersion.VERSION_11')

# Change minSdk to 24 for Chaquopy
content = content.replace(
    'minSdk = flutter.minSdkVersion',
    'minSdk = 24'
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
