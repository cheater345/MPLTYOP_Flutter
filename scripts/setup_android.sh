#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

export FLUTTER_ROOT="${FLUTTER_ROOT:-$(which flutter | xargs dirname 2>/dev/null || echo /opt/hostedtoolcache/flutter/stable)}"

rm -rf android
flutter create . --platforms android

python3 << 'PYEOF'
with open('android/gradle/wrapper/gradle-wrapper.properties', 'r') as f:
    content = f.read()
content = content.replace('gradle-7.6.3-all', 'gradle-8.9-all')
content = content.replace('gradle-7.6.3-bin', 'gradle-8.9-bin')
with open('android/gradle/wrapper/gradle-wrapper.properties', 'w') as f:
    f.write(content)
print("Gradle wrapper upgraded to 8.9")
PYEOF

python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()
content = content.replace(
    'id "com.android.application" version "7.3.0" apply false',
    'id "com.android.application" version "8.7.3" apply false'
)
content = content.replace(
    'id "com.android.library" version "7.3.0" apply false',
    'id "com.android.library" version "8.7.3" apply false'
)
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

python3 << 'PYEOF'
new_content = '''buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url "https://chaquo.com/maven" }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:8.7.3"
        classpath "com.chaquo.python:gradle:17.0.0"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0"
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

python3 << 'PYEOF'
with open('android/app/build.gradle', 'r') as f:
    content = f.read()

content = content.replace('com.example.mpltyop_flutter', 'com.cheater345.mpltyop')
content = content.replace('JavaVersion.VERSION_1_8', 'JavaVersion.VERSION_11')
content = content.replace('minSdk = flutter.minSdkVersion', 'minSdk = 24')
content = content.replace('compileSdk = flutter.compileSdkVersion', 'compileSdk = 35')

content = content.replace(
    '}\n\ndef localProperties = new Properties()',
    '}\n\napply plugin: "com.chaquo.python"\n\ndef localProperties = new Properties()'
)

content = content.replace(
    'namespace = "com.cheater345.mpltyop"',
    'namespace = "com.cheater345.mpltyop"\n\n    python {\n        version = "3.11"\n        pip {\n            install "ytmusicapi==1.8.0"\n            install "yt-dlp==2024.1.2"\n            install "requests==2.31.0"\n        }\n    }'
)

with open('android/app/build.gradle', 'w') as f:
    f.write(content)
print("app build.gradle patched")
PYEOF

cat > android/local.properties << EOF
flutter.sdk=$FLUTTER_ROOT
flutter.versionCode=1
flutter.versionName=1.0.0
EOF

echo "=== app build.gradle ==="
cat android/app/build.gradle
