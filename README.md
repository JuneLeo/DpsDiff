# 比较两个版本AAR包差异

## dependency-diff是什么？

[点我查看](https://www.juneleo.cn/2021/06/02/Gradle%E8%8E%B7%E5%8F%96%E4%BE%9D%E8%B5%96aar%E5%8C%85%E5%A4%A7%E5%B0%8F2/#more)

## 开发

**gradle.properties**

```
org.gradle.jvmargs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
org.gradle.daemon=true
```

**build.gradle**

```
classpath project(':dependency-diff')
```

**app/build.gralde**

```
task dps(type: com.card.script.get.DependenciesGetTask){
    exclude = ["support"]
    classpath = 'runtime' // compile,runtime,all
}

task dps2(type:com.card.script.diff.DependenciesDiffTask){
    firstPath='/Users/juneleo/amap/amap_android/dependencies.csv'
    secondPath='/Users/juneleo/amap/amap_android_backup/amap_android/dependencies.csv'
    outType='console'
    excludeModuleFile =['/arm64-v8a/']
}
```

**dependency-diff/build.gradle**

```
//apply plugin: 'com.github.dcendents.android-maven'
```

**build.sh**

```
./build.sh 1
./build.sh 2
```

## 编译问题


**首次编译可能失败**

解决方式：因为dependency-diff会在构建脚本中引入，为了开发和发版，没有使用buildSrc，编译app项目并不会触发dependency-diff的编译，所以我们可以先编译dependency-diff
```
./gradlew :dependency-diff:build
```
编译完成后，我们再使用DependenciesGetTask和DependenciesDiffTask



