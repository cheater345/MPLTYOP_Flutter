#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# Update project build.gradle: add Chaquopy repo + classpath
sed -i 's|mavenCentral()|mavenCentral()\n        maven { url "https://chaquo.com/maven" }|' android/build.gradle
sed -i '/classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:/a\        classpath "com.chaquo.python:gradle:12.0.0"' android/build.gradle

# Add Chaquopy plugin to app build.gradle
sed -i "/apply plugin: 'com.android.application'/a apply plugin: 'com.chaquo.python'" android/app/build.gradle

# Add Chaquopy python config + multidex using Python
python3 << 'PYEOF'
with open('android/app/build.gradle', 'r') as f:
    content = f.read()

# Add Chaquopy python block after namespace
old_ns = 'namespace "com.cheater345.mpltyop"'
new_ns = '''namespace "com.cheater345.mpltyop"

    python {
        version "3.11"
        buildType "release"
        pip {
            install "ytmusicapi==1.8.0"
            install "yt-dlp==2024.1.2"
            install "requests==2.31.0"
        }
    }'''
content = content.replace(old_ns, new_ns)

# Ensure multidex
if 'multidex' not in content:
    content = content.replace(
        'implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20"',
        'implementation "androidx.multidex:multidex:2.0.1"\n    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20"'
    )

with open('android/app/build.gradle', 'w') as f:
    f.write(content)
print("Chaquopy + multidex added successfully")
PYEOF

echo "=== Project build.gradle ==="
cat android/build.gradle
echo ""
echo "=== App build.gradle ==="
cat android/app/build.gradle
