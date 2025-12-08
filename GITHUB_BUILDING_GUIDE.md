# GitHub 项目构建流程设置指南

## 概述
本文档介绍如何为新项目设置 GitHub Actions 构建流程，特别是Android应用的APK自动构建。

## 步骤 1: 创建 GitHub 仓库
1. 登录 GitHub 并创建一个新的仓库
2. 将您的项目代码推送到该仓库

## 步骤 2: 配置 GitHub Actions 工作流
1. 在仓库中创建 `.github/workflows/` 目录
2. 创建工作流配置文件（如 `main.yml` 或 `android.yml`）
3. 定义构建步骤，包括：
   - Checkout 代码
   - 设置运行环境
   - 安装依赖
   - 运行构建命令
   - 上传构建产物

## 步骤 3: 设置访问令牌（如需要）
1. 前往 GitHub 设置页面
2. 进入 Developer settings > Personal access tokens
3. 创建新的令牌并设置适当的权限
4. 在仓库的 Secrets 中配置令牌，例如：
   - 名称：GITHUB_TOKEN
   - 值：您的访问令牌值

## Android项目构建方法和文件模板

### 项目结构
```
AndroidTestApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/testapp/
│   │   │   └── MainActivity.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── colors.xml
│   │   │   │   └── styles.xml
│   │   │   ├── xml/
│   │   │   │   ├── data_extraction_rules.xml
│   │   │   │   └── backup_rules.xml
│   │   │   └── mipmap-*/ (各种分辨率)
│   │   │       ├── ic_launcher.xml
│   │   │       └── ic_launcher_round.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── .github/workflows/
│   └── android.yml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

### 关键文件模板

#### 1. GitHub Actions工作流文件 (.github/workflows/android.yml)
```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Initialize Gradle Wrapper
      run: |
        curl -L -o gradle.zip https://services.gradle.org/distributions/gradle-8.1.1-bin.zip
        unzip gradle.zip
        export GRADLE_HOME=$PWD/gradle-8.1.1
        export PATH=$GRADLE_HOME/bin:$PATH
        chmod +x $GRADLE_HOME/bin/gradle
        echo "GRADLE_HOME=$GRADLE_HOME" >> $GITHUB_ENV
        echo "$GRADLE_HOME/bin" >> $GITHUB_PATH
        
    - name: Build Debug APK
      run: gradle assembleDebug
      
    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk
        
    - name: Build Release APK
      run: gradle assembleRelease
      
    - name: Upload Release APK
      uses: actions/upload-artifact@v4
      with:
        name: release-apk
        path: app/build/outputs/apk/release/app-release.apk
```

#### 2. 应用级build.gradle (app/build.gradle)
```gradle
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.example.testapp'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.testapp"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.debug
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

#### 3. ProGuard规则文件 (app/proguard-rules.pro)
```proguard
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes
-keep class com.example.testapp.** { *; }
```

#### 4. 样式文件 (app/src/main/res/values/styles.xml)
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Theme.TestApp" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <!-- Primary brand color. -->
        <item name="colorPrimary">@android:color/holo_blue_bright</item>
        <item name="colorPrimaryVariant">@android:color/holo_blue_dark</item>
        <item name="colorOnPrimary">@android:color/white</item>
        <!-- Secondary brand color. -->
        <item name="colorSecondary">@android:color/holo_green_light</item>
        <item name="colorSecondaryVariant">@android:color/holo_green_dark</item>
        <item name="colorOnSecondary">@android:color/black</item>
        <!-- Status bar color. -->
        <item name="android:statusBarColor" tools:targetApi="l">?attr/colorPrimaryVariant</item>
        <!-- Customize your theme here. -->
    </style>
</resources>
```

#### 5. 数据提取规则 (app/src/main/res/xml/data_extraction_rules.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="."/>
        <exclude domain="sharedpref" path="device.xml"/>
    </cloud-backup>
    <device-transfer>
        <include domain="sharedpref" path="."/>
        <exclude domain="sharedpref" path="device.xml"/>
    </device-transfer>
</data-extraction-rules>
```

#### 6. 备份规则 (app/src/main/res/xml/backup_rules.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="."/>
    <exclude domain="sharedpref" path="device.xml"/>
</full-backup-content>
```

#### 7. 矢量图标示例 (app/src/main/res/mipmap-*/ic_launcher.xml)
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#2196F3"
        android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M54,24c16.5,0 30,13.5 30,30s-13.5,30 -30,30s-30,-13.5 -30,-30S37.5,24 54,24z" />
</vector>
```

### 构建命令
```bash
# 本地构建Debug APK
./gradlew assembleDebug

# 本地构建Release APK
./gradlew assembleRelease

# 清理项目
./gradlew clean

# 构建所有变体
./gradlew assemble
```

### 常见问题和解决方案

1. **Gradle Wrapper缺失**
   - 在GitHub Actions中下载Gradle而不是依赖本地文件
   - 确保设置正确的GRADLE_HOME环境变量

2. **资源冲突**
   - 避免同时使用PNG和XML格式的同名资源
   - 优先使用矢量图标(XML)替代位图(PNG)

3. **Release构建失败**
   - 添加ProGuard规则文件
   - 配置签名设置(可使用debug签名用于测试)

4. **命名空间错误**
   - 在XML资源文件中添加tools命名空间声明
   - 确保所有使用的属性都有对应的命名空间

### 推送和构建流程
```bash
# 初始化Git仓库
git init
git branch -m main

# 添加所有文件
git add .

# 提交代码
git commit -m "Initial commit"

# 添加远程仓库
git remote add origin https://github.com/username/repository.git

# 推送代码(自动触发GitHub Actions)
git push -u origin main
```

## 注意事项
- 请勿推送此文件到任何仓库或网络
- 确保工作流配置文件语法正确
- 测试构建流程确保正常运行
- 保护好您的访问令牌，不要在代码中直接暴露
- 使用最新版本的GitHub Actions(v4)以避免弃用警告

## 令牌信息
您的个人访问令牌: [请在此处填入您的访问令牌]
（请妥善保管此令牌，并在需要时配置到仓库的 Secrets 中）