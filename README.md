# Android TestApp

这是一个简单的Android测试应用，用于演示如何使用GitHub Actions自动构建APK。

## 功能

- 显示"Hello GitHub Actions!"文本
- 基本的Android应用结构

## 构建说明

### 本地构建

1. 确保已安装Android SDK和Java 17
2. 克隆项目到本地
3. 运行以下命令：

```bash
./gradlew assembleDebug
```

生成的APK文件位于：`app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions自动构建

当您推送代码到main分支或创建Pull Request时，GitHub Actions将自动：

1. 构建Debug和Release版本的APK
2. 将构建产物作为Artifacts上传
3. 您可以在Actions页面下载生成的APK文件

## 项目结构

```
AndroidTestApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/testapp/
│   │   │   └── MainActivity.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── .github/workflows/
│   └── android.yml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 系统要求

- Android Studio Arctic Fox或更高版本
- Android SDK API 34
- Java 17
- Gradle 8.1.1

## 许可证

MIT License