#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# ================================================================
# 1. Patch project-level build.gradle: AGP 8.7.3 + Chaquopy 17.0.0 + Kotlin 2.1.0
# ================================================================
python3 << 'PYEOF'
with open('android/build.gradle', 'r') as f:
    c = f.read()

c = c.replace(
    'classpath \'com.android.tools.build:gradle:8.4.0\'',
    'classpath \'com.android.tools.build:gradle:8.7.3\''
)
c = c.replace(
    'classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20"',
    'classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0"'
)
c = c.replace(
    'classpath "com.chaquo.python:gradle:16.0.0"',
    'classpath "com.chaquo.python:gradle:17.0.0"'
)

with open('android/build.gradle', 'w') as f:
    f.write(c)
print("Project build.gradle patched")
PYEOF

# ================================================================
# 2. Patch app-level build.gradle: compileSdk 35, Kotlin 2.1.0, remove ndkVersion
# ================================================================
python3 << 'PYEOF'
with open('android/app/build.gradle', 'r') as f:
    c = f.read()

c = c.replace('compileSdk 34', 'compileSdk 35')
c = c.replace('targetSdk 34', 'targetSdk 35')
c = c.replace('minSdk 23', 'minSdk 24')
c = c.replace('ndkVersion flutter.ndkVersion\n', '')
c = c.replace(
    'implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20"',
    'implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0"'
)
c = c.replace(
    'sourceCompatibility JavaVersion.VERSION_1_8',
    'sourceCompatibility JavaVersion.VERSION_11'
)
c = c.replace(
    'targetCompatibility JavaVersion.VERSION_1_8',
    'targetCompatibility JavaVersion.VERSION_11'
)
c = c.replace('jvmTarget = \'1.8\'', 'jvmTarget = \'17\'')

with open('android/app/build.gradle', 'w') as f:
    f.write(c)
print("App build.gradle patched")
PYEOF

# ================================================================
# 3. Add Chaquopy Maven to settings.gradle
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    c = f.read()

if 'chaquo.com' not in c:
    c = c.replace(
        'mavenCentral()',
        'mavenCentral()\n        maven { url = uri("https://chaquo.com/maven") }',
        1
    )

with open('android/settings.gradle', 'w') as f:
    f.write(c)
print("settings.gradle patched")
PYEOF

# ================================================================
# 4. Upgrade Gradle wrapper to 8.9
# ================================================================
python3 << 'PYEOF'
with open('android/gradle/wrapper/gradle-wrapper.properties', 'r') as f:
    c = f.read()

c = c.replace('gradle-7.6.3-all', 'gradle-8.9-all')
c = c.replace('gradle-7.6.3-bin', 'gradle-8.9-bin')

with open('android/gradle/wrapper/gradle-wrapper.properties', 'w') as f:
    f.write(c)
print("Gradle wrapper upgraded to 8.9")
PYEOF

# ================================================================
# 5. Patch yt_flutter_musicapi plugin: remove Windows Python path
# ================================================================
PLUGIN_DIR=$(find /home/runner/.pub-cache/git/ -maxdepth 1 -name "yt_flutter_musicapi-*" 2>/dev/null | head -1)
if [ -n "$PLUGIN_DIR" ]; then
    sed -i '/buildPython/d' "$PLUGIN_DIR/android/build.gradle"
    echo "Plugin build.gradle patched"
else
    echo "WARNING: Plugin not found in pub-cache"
fi

echo "=== Setup complete ==="
