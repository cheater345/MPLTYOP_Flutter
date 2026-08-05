#!/bin/bash
set -e

cd "$(dirname "$0")/../mpltyop_flutter"

export FLUTTER_ROOT="${FLUTTER_ROOT:-$(which flutter | xargs dirname 2>/dev/null || echo /opt/hostedtoolcache/flutter/stable)}"

# Remove and regenerate Android project
rm -rf android
flutter create . --platforms android

# ================================================================
# 1. Upgrade Gradle wrapper to 8.9 (needed for AGP 8.7.3)
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
# 2. Patch settings.gradle: update AGP version
# ================================================================
python3 << 'PYEOF'
with open('android/settings.gradle', 'r') as f:
    content = f.read()

# Update AGP version to 8.7.3 (matches yt_flutter_musicapi plugin)
content = content.replace(
    'id "com.android.application" version "7.3.0" apply false',
    'id "com.android.application" version "8.7.3" apply false'
)
content = content.replace(
    'id "com.android.library" version "7.3.0" apply false',
    'id "com.android.library" version "8.7.3" apply false'
)

with open('android/settings.gradle', 'w') as f:
    f.write(content)
print("settings.gradle patched")
PYEOF

# ================================================================
# 3. Write project build.gradle with Chaquopy + Kotlin + AGP classpaths
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
# 4. Write app build.gradle using apply plugin syntax (not plugins DSL)
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
    namespace "com.cheater345.mpltyop"
    compileSdk 35

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
        targetSdk 35
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

chaquopy {
    defaultConfig {
        version = "3.11"
        pip {
            install "ytmusicapi==1.8.0"
            install "yt-dlp==2024.1.2"
            install "requests==2.31.0"
        }
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
# 5. Generate local.properties with flutterRoot
# ================================================================
cat > android/local.properties << EOF
flutter.sdk=$FLUTTER_ROOT
flutter.versionCode=1
flutter.versionName=1.0.0
EOF

echo "=== app build.gradle ==="
cat android/app/build.gradle
echo ""
echo "=== local.properties ==="
cat android/local.properties
