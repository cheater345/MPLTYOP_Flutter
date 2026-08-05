#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Patch project build.gradle: add buildscript with Chaquopy
# ================================================================
python3 << 'PYEOF'
with open('android/build.gradle', 'r') as f:
    content = f.read()

# Add Chaquopy repository to allprojects
content = content.replace(
    'mavenCentral()',
    'mavenCentral()\n        maven { url "https://chaquo.com/maven" }'
)

# Add buildscript block with Chaquopy classpath at the top
buildscript_block = '''buildscript {
    ext {
        chaquopyVersion = "12.0.0"
    }
    repositories {
        google()
        mavenCentral()
        maven { url "https://chaquo.com/maven" }
    }
    dependencies {
        classpath "com.chaquo.python:gradle:12.0.0"
    }
}

'''
# Insert at the very beginning
content = buildscript_block + content

with open('android/build.gradle', 'w') as f:
    f.write(content)
print("project build.gradle patched")
PYEOF

# ================================================================
# 2. Patch settings.gradle: add Chaquopy to dependencyResolutionManagement
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Add Chaquopy repo to dependencyResolutionManagement repositories if present
if 'chaquo.com' not in content:
    if 'dependencyResolutionManagement' in content:
        # Add to existing repositories block
        content = content.replace(
            'repositories {',
            'repositories {\n        maven { url = uri("https://chaquo.com/maven") }',
            1
        )
    elif 'pluginManagement' in content:
        # Add to existing pluginManagement repositories
        # Find the first repositories block inside pluginManagement
        idx = content.find('pluginManagement')
        if idx != -1:
            repos_idx = content.find('repositories {', idx)
            if repos_idx != -1:
                content = content[:repos_idx + len('repositories {')] + '\n        maven { url = uri("https://chaquo.com/maven") }' + content[repos_idx + len('repositories {'):]

with open('android/settings.gradle', 'w') as f:
    f.write(content)
print("settings.gradle patched")
PYEOF

# ================================================================
# 3. Patch app build.gradle
# ================================================================
python3 << 'PYEOF'
with open('android/app/build.gradle', 'r') as f:
    content = f.read()

# Fix namespace and applicationId
content = content.replace('com.example.mpltyop_flutter', 'com.cheater345.mpltyop')

# Change Java version to 11 (for Chaquopy compatibility)
content = content.replace('JavaVersion.VERSION_1_8', 'JavaVersion.VERSION_11')

# Change minSdk to 24 for Chaquopy
content = content.replace(
    'minSdk = flutter.minSdkVersion',
    'minSdk = 24'
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

# Apply Chaquopy plugin via apply statement (not plugins DSL)
content = content.replace(
    'flutter {',
    '''apply plugin: "com.chaquo.python"

flutter {'''
)

with open('android/app/build.gradle', 'w') as f:
    f.write(content)
print("app build.gradle patched")
PYEOF

echo "=== project build.gradle ==="
cat android/build.gradle
echo ""
echo "=== settings.gradle ==="
cat android/settings.gradle
echo ""
echo "=== app build.gradle ==="
cat android/app/build.gradle
