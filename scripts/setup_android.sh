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
    'mavenCentral()\n        maven { url "https://chaquo.com/maven" }',
    1  # Only in buildscript repos, not allprojects
)

# Add buildscript block with Chaquopy classpath at the top
buildscript_block = '''buildscript {
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
content = buildscript_block + content

# Add Chaquopy repo to allprojects repositories (second occurrence)
content = content.replace(
    'mavenCentral()\n        maven { url "https://chaquo.com/maven" }',
    'mavenCentral()\n        maven { url "https://chaquo.com/maven" }\n        maven { url "https://chaquo.com/maven" }',
    1
)

# Remove duplicate (keep original approach simpler)
# Actually let's just add Chaquopy repo to allprojects
print("project build.gradle patched")
PYEOF

# Simpler approach for project build.gradle
python3 << 'PYEOF'
with open('android/build.gradle', 'r') as f:
    content = f.read()

# Add Chaquopy repo to allprojects repositories
if 'chaquo.com' not in content:
    content = content.replace(
        'mavenCentral()',
        'mavenCentral()\n        maven { url "https://chaquo.com/maven" }',
        1
    )
    # Add buildscript at the very top
    buildscript = '''buildscript {
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
    content = buildscript + content
    # Also add Chaquopy to the second mavenCentral (in allprojects)
    content = content.replace(
        'mavenCentral()',
        'mavenCentral()\n        maven { url "https://chaquo.com/maven" }',
        1
    )

with open('android/build.gradle', 'w') as f:
    f.write(content)
print("project build.gradle patched (clean)")
PYEOF

# ================================================================
# 2. Patch settings.gradle: add Chaquopy to repositories
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Add Chaquoo repo to the first repositories block in settings.gradle (inside pluginManagement)
if 'chaquo.com' not in content:
    # Find first 'repositories {' and add Chaquopy after google()
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
# 3. Patch app build.gradle
# Use apply plugin for Chaquopy (not plugins DSL)
# Place apply plugin + python config OUTSIDE the android block
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

# Apply Chaquopy plugin right after the plugins block, before localProperties
content = content.replace(
    '}\n\ndef localProperties = new Properties()',
    '}\n\napply plugin: "com.chaquo.python"\n\ndef localProperties = new Properties()'
)

# Add Chaquopy python config OUTSIDE the android block (before buildTypes closing or after android block)
# Add after the android { block closes, before flutter {
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

echo "=== project build.gradle ==="
cat android/build.gradle
echo ""
echo "=== settings.gradle ==="
cat android/settings.gradle
echo ""
echo "=== app build.gradle ==="
cat android/app/build.gradle
