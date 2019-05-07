# FineReport插件开发Gradle插件
这是一个用于简化FineReport插件开发的gradle插件，无需maven和ant以及繁杂的配置即可打包出符合FineReport插件要求的插件包。
支持Kotlin开发插件。
## 开始开发旅程
### 第一步：新建一个gradle Java项目
![-w871](https://user-gold-cdn.xitu.io/2019/5/7/16a92604532e300e?w=1742&h=974&f=jpeg&s=177871)

### 第二步：引入插件
在项目的build.gradle文件头部新增插件：
``` groovy
apply plugin: 'com.huangmb.fine-report-plugin'
```

注：目前该插件还未在gradle插件中心上架，所以还需要通过本地插件的方式引用。待插件上架后不需要后面的操作.
在build.gradle文件顶部添加：

``` groovy
buildscript {
    repositories {
        mavenLocal() 
        // 插件所在仓库，比如本地某个文件文件夹
        //maven {
        //    url = uri("../build/repo")
        //}
    }
    
    dependencies {
        classpath "com.huangmb.gradle:fine-report-plugin:0.0.1-SNAPSHOT"
    }
}
```

### 第三步：刷新gradle
现在你就能看见所有插件所需的依赖都自动引用进来了。可以愉快的开始写插件代码了。
![依赖](https://user-gold-cdn.xitu.io/2019/5/7/16a926045e7acb53?w=1056&h=1498&f=jpeg&s=480528)


### 第四步：打包
只需要一行命令
```
./gradlew bundlePlugin
```

或者在gradle面板中双击bundlePlugin命令
![gradle命令](https://user-gold-cdn.xitu.io/2019/5/7/16a92604501ba363?w=864&h=760&f=jpeg&s=88339)

现在，你就可以在build文件夹看到插件包了，其中install文件夹下的zip就是最终的插件包了，拿去设计器安装吧。
![插件包](https://user-gold-cdn.xitu.io/2019/5/7/16a92604503db867?w=706&h=522&f=jpeg&s=75918)

运行效果：
![运行效果](https://user-gold-cdn.xitu.io/2019/5/7/16a92604578e5211?w=1386&h=1022&f=jpeg&s=192291)


## 进阶
### 引用第三方Jar
不用纠结，尽管用gradle的dependencies添加第三方库吧，插件会自动帮你打包进插件包的lib文件夹下的。
- 你可以引本地文件：
  在libs文件夹下放jar文件，然后引用整个文件夹：
    
    ```
    compile fileTree(include: ['*.jar'], dir: 'libs')
    ```

- 你也可以引远程仓库上面的文件：

    ```
    compile "com.alibaba:fastjson:1.2.58"
    ```
 
- 添加运行时依赖：

```
compileOnly  'javax.servlet:javax.servlet-api:4.0.1'
```

- 添加报表内置库：

```
reportLib "com.fr.report:fine-report-engine:10.0-RELEASE-SNAPSHOT"
```

> `reportLib`是插件增加的一种依赖类型，等价于compileOnly，用于标记报表内置库的库.
> 上面的报表引擎包不需要自己引入，因为插件已经把常用的报表库都自动引入了.

最后，一个dependencies可能长这样，构建命令只会把编译期依赖的库打包进最终的插件包，compileOnly、reportLib、testCompile等依赖自动忽略。

``` groovy
dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile "com.alibaba:fastjson:1.2.58"
    compile "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```

### 使用Kotlin
腻歪了Java的冗长代码，可以试试Kotlin吧。
1. 首先，引入Kotlin：
在build.gradle顶部添加Kotlin插件。

    ``` groovy
    buildscript {
        ext.kotlin_version = '1.3.10'
        dependencies {
            classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        }
    }

    apply plugin: 'kotlin'
    ```
2. 添加Kotlin依赖：

    ```
    dependencies {
        ...
        compile "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
        ...
    }
    ```

3. 现在，来一段Kotlin代码：
  这是一段自定义公式插件
  
    ```
    package com.fr.plugin

    import com.fr.intelli.record.Focus
    import com.fr.intelli.record.Original
    import com.fr.record.analyzer.EnableMetrics
    import com.fr.script.AbstractFunction
    
    @EnableMetrics
    class AnotherFunc :AbstractFunction() {
        @Focus(id = "com.fr.plugin.function.test", text = "Plugin-Test_Function_Abs", source = Original.PLUGIN)
        override fun run(args: Array<out Any>?): Any {
            return "来自Kotlin"
        }
    }
    ```
  
    打包插件看看效果吧。

### 自定义配置项
插件默认引用的是10.0-RELEASE-SNAPSHOT版本，如果需要其他版本，可以在build.gradle通过配置项指定。

目前允许如下配置项，均为可选：

```
FineReportPlugin {
    pluginName = "test" //生成插件包文件夹的插件名称,默认是project名称
    pluginVersion = "1.0" //生成插件包文件夹的插件版本，默认是project版本
    coreVersion = "10.0-RELEASE" // 报表库版本，默认是"10.0-RELEASE"，可选"10.0"、"10.0-FEATURE"等
    useSnapshot = true // 使用报表库的快照版本，会在版本号后面自动添加-SNAPSHOT，默认true
}
```

## 运行demo
1. 在项目根目录下，运行./gradlew uploadArchives将插件发布到本地文件夹（build/repo文件夹）
2. 使用IDEA打开demo插件gradle项目hello-world-plugin，运行./gradlew bundlePlugin生成设计器插件包(build/install文件夹)

## 插件做了什么
- 自动添加fanruan的maven仓库
- 自动引用报表库以及一些必要的第三方库
- 新增依赖配置项reportLib，标记报表内部库
- 自动拷贝第三方库和plugin.xml,生成符合报表插件规范的zip

## 插件还需要做什么
- 更多的配置项
- 集成调试能力
- 根据项目信息、配置项以及代码注解，自动生成plugin.xml
