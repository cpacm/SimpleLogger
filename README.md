# SimpleLogger

## 简介
一款基于 `log4j` 处理Android平台的 log 注解框架。

## Import

1. 在主工程的 `build.gradle` 添加
```groovy
buildscript {
    repositories {
        maven { url 'http://nexus.yipiaoyun.com/repository/android/' }

    }
    dependencies {
        classpath 'com.cpacm:logger-gradle-plugin:1.0.0'
        //...
    }
}
```
2. 在需要log注解处理的项目或者子模块中添加
```groovy
apply plugin: 'com.cpacm.log'

dependencies {
    
    //...
    implementation 'com.cpacm:logger:1.0.0'
}

```

## How to Use

### 初始化
在 `application` 中进行log的注册
```kotlin
SimpleLogger.init(SimpleLoggerConfig(this))
```

### 代码使用
```kotlin
fun d(key: String, content: String, throwable: Throwable? = null, specialName: String? = null)
```
### 注解使用

#### CLog
`@CLog` 是应用于类上的注解，作用是为类里面的所有方法都加上 logger
```java
@CLog
public class Test {
    public void doSomeThing(String var1){
        
    } 
}
//编译后将会变成
@CLog
public class Test {
    public void doSomeThing(String var1){
        SimpleLogger.logger("DEBUG", "test", "<doSomeThing>:(" + var1 + ")", true, "");
    } 
}
```
#### MLog
`@MLog` 是应用于方法上的注解，作用是方法加上 logger 输出。`MLog`的logger输出将会覆盖`CLog`的输出
```java
public class Test {
    @MLog(key = "test", level = LoggerLevel.ERROR)
    public void doSomeThing(String var1){
        
    } 
}
//编译后将会变成
public class Test {
    @MLog(key = "test", level = LoggerLevel.ERROR)
    public void doSomeThing(String var1){
        SimpleLogger.logger("ERROR", "test", "<doSomeThing>:(" + var1 + ")", true, "");
                
    } 
}
```
#### TLog
`@TLog` 是应用于方法上的注解，用来统计该方法的运行时间

```java
public class Test {
    @TLog
    public long getTime(int k) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}

//编译后将会变成

public class Test {
    @TLog
    public long getTime(int k) {
        long var10998 = System.currentTimeMillis();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var10999 = System.currentTimeMillis() - var10998;
        SimpleLogger.logger("DEBUG", "Test", "<getTime>:cost mills--" + var10999, true, "");
        return 0L;
    }
}
```

#### NoLog
`@NoLog` 是应用于方法上的注解，被注解的方法中将不会生成任何注解

#### LifeLog
`@LifeLog` 是应用于类上的注解，与`@LifeLogStart`和`@LifeLogEnd`一起使用，作用是控制类里面的方法的 logger 输出区间

```kotlin
@LifeLog(key = "lifecycle")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @LifeLogStart
    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    @LifeLogEnd
    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

```
输出结果为
```cmd
D/lifecycle: Lifecycle Start at <onStart>:()
D/lifecycle: Lifecycle Running at <onResume>:()
D/lifecycle: Lifecycle Running at <onPause>:()
D/lifecycle: Lifecycle End at <onStop>:()
```


## What's Mean?

### 初始化
`SimpleLoggerConfig`中各参数分别代表为

| 参数名 | 作用  |
| ------ | ------ | 
| debugEnv: Boolean | true:表示为调试环境，所有log都可以输出；false: 除去 error 级别外，所有log都无法输出 | 
| level: LoggerLevel | 默认输出等级，一共分为`VERBOSE`,`DEBUG`,`INFO`,`WARN` 和 `ERROR` |
| filePath：String | `log4j` 文件输出地址，默认为应用内部文件地址 |

### log注解中的各参数
注解以及代码中的各个参数分别代表为：

| 参数名 | 作用  |
| ------ | ------ | 
| level | log输出等级 | 
| key | 关键字，默认为类名 |
| content | 输出内容，方法注解上的参数。默认为方法名和参数的组合 |
| debug | 是否只在debug环境中显示，默认为true. false表示在正式环境中也会输出日志 |
| special | 是否保存至额外文件，针对 `log4j` |