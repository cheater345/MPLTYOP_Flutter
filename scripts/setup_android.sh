#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Patch settings.gradle: add Chaquopy repo to pluginManagement
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Add Chaquopy repo to pluginManagement (for plugin resolution)
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
# Keep Gradle 7.6.3 and AGP 7.3.0 (Flutter 3.22 defaults)
# ================================================================
python3 << 'PYEOF'
new_content = '''buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url "https://chaquo.com/maven" }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:7.3.0"
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
# Use old-style build.gradle (not plugins DSL) for Chaquopy compatibility
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

apply from: flutterRoot + "/packages/flutter_tool/gradle/flutter.gradle"

def flutterVersionCode = localProperties.getProperty("flutter.versionCode")
if (flutterVersionCode == null) {
    flutterVersionCode = "1"
}

def flutterVersionName = localProperties.getProperty("flutter.versionName")
if (flutterVersionName == null) {
    flutterVersionName = "1.0.0"
}

android {
    namespace "com.cheater345.mpltyop"
    compileSdk 34

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
        minSdk 24
        targetSdk 34
        versionCode flutterVersionCode.toInteger()
        versionName flutterVersionName
        multiDexEnabled true
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

dependencies {
    implementation "androidx.multidex:multidex:2.0.1"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.20"
}
'''

with open('android/app/build.gradle', 'w') as f:
    f.write(new_app_content)
print("app build.gradle patched")
PYEOF

echo "=== project build.gradle ==="
cat android/build.gradle
echo ""
echo "=== app build.gradle ==="
cat android/app/build.gradle

# ================================================================
# 5. Generate local.properties with flutter.sdk (needed for $flutterRoot)
# ================================================================
cat > android/local.properties << EOF
flutter.sdk=$FLUTTER_ROOT
flutter.versionCode=1
flutter.versionName=1.0.0
EOF
echo "FLUTTER_ROOT=$FLUTTER_ROOT"
echo "=== local.properties ==="
cat android/local.properties
