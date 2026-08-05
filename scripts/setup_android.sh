#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

export FLUTTER_ROOT="${FLUTTER_ROOT:-$(which flutter | xargs dirname 2>/dev/null || echo /opt/hostedtoolcache/flutter/stable)}"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Upgrade Gradle wrapper to 8.9 (AGP 8.7.3 requires Gradle 8.5+)
# ================================================================
python3 << 'PYEOF'
with open('android/gradle/wrapper/gradle-wrapper.properties', 'r') as f:
    content = f.read()
content = content.replace('gradle-7.6.3-all', 'gradle-8.9-all')
content = content.replace('gradle-7.6.3-bin', 'gradle-8.9-bin')
with open('android/gradle/wrapper/gradle-wrapper.properties', 'w') as f:
    f.write(content)
print("Gradle wrapper upgraded to 8.9")
PYEOF

# ================================================================
# 2. Patch settings.gradle: update AGP version + add Chaquopy repo
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Update AGP version to 8.7.3 (matches yt_flutter_musicapi plugin)
content = content.replace(
    'id "com.android.application" version "7.3.0" apply false',
    'id "com.android.application" version "8.7.3" apply false'
)

# Add Chaquopy repo to pluginManagement (Maven Central has Chaquopy 17.0.0)
if 'chaquo.com' not in content and 'chaquopy' not in content:
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
# 3. Patch project build.gradle: add Chaquopy classpath from Maven Central
# ================================================================
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

# ================================================================
# 4. Patch app build.gradle
# Use apply plugin for Chaquopy + python config inside android block
# ================================================================
python3 << 'PYEOF'
new_app_content = '''apply plugin: "com.android.application"
apply plugin: "kotlin-android"
apply plugin: "com.chaquo.python"

def localProperties = new Properties()
def localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.withReader("UTF-8") { reader ->
        localProperties.load(reader)
    }
}

def flutterRoot = localProperties.getProperty("flutter.sdk")
if (flutterRoot == null) {
    flutterRoot = "C:/flutter"
}

apply from: flutterRoot + "/packages/flutter_tools/gradle/flutter.gradle"

def flutterVersionCode = localProperties.getProperty("flutter.versionCode")
if (flutterVersionCode == null) {
    flutterVersionCode = "1"
}

def flutterVersionName = localProperties.getProperty("flutter.versionName")
if (flutterVersionName == null) {
    flutterVersionName = "1.0.0"
}

android {
    compileSdkVersion 35

    sourceSets {
        main.java.srcDirs += "src/main/kotlin"
        main.kotlin.srcDirs += "src/main/kotlin"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = '1.8'
    }

    defaultConfig {
        applicationId "com.cheater345.mpltyop"
        minSdkVersion 24
        targetSdkVersion 35
        versionCode flutterVersionCode.toInteger()
        versionName flutterVersionName
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
        }
    }

    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            signingConfig signingConfigs.debug
        }
    }

    lint {
        abortOnError false
    }
}

python {
    version "3.11"
    buildType "release"
    pip {
        install "ytmusicapi==1.8.0"
        install "yt-dlp==2024.1.2"
        install "requests==2.31.0"
    }
}

flutter {
    source = ".."
}

dependencies {
    implementation "androidx.multidex:multidex:2.0.1"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0"
}
'''

with open('android/app/build.gradle', 'w') as f:
    f.write(new_app_content)
print("app build.gradle patched")
PYEOF

# ================================================================
# 5. Generate local.properties
# ================================================================
cat > android/local.properties << EOF
flutter.sdk=$FLUTTER_ROOT
flutter.versionCode=1
flutter.versionName=1.0.0
EOF

echo "=== app build.gradle ==="
cat android/app/build.gradle
