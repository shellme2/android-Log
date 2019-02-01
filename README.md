
#日志库使用说明
----

## 关于

- 版本: V1.0


## 前置条件

- API>=15

- 如果使用方法耗时打印插件, 需要编译jdk版本: 1.7

- 权限: 如果需要保存log, 需要添加对应的写文件的权限


## 功能列表

- 提供日志调试开关,用于全局控制
- 提供Log的全局Tag配置,简化代码
- 提供Log时的线程堆栈信息, 方便分析定位问题
- 提供Log的格式化(log json)打印功能,方便查看log
- 提供日志保存功能,便于自己取log分析
- 提供方法耗时统计

## 升级清单文档

- 请参考[升级清单文档](./UPDATE.md)

### 项目依赖
1. log依赖    
	无

2. 方法耗时插件依赖:   

	org.aspectj:aspectjrt:1.8.6


## 公共接口

2. BfcLog     
	日志库提供的打印log的接口, 通过`BfcLog.Builder`构建实例对象, 一般用于自定义的log工具类中
3. BfcLog.Builder     
	用于构建`BfcLog`的实例
1. BfcLogger   
	日志库提供的静态方法, 是对`BfcLog`的封装, 使用静态方法以方便使用

## 使用说明


### 1.添加依赖

    compile 'com.eebbk.bfc:bfc-log:2.1.0'
> 如果依赖使用的[网络配置](http://172.28.2.93/bfc/Bfc/blob/develop/public-config/%E4%BE%9D%E8%B5%96%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E.md), 请参考网络配置使用; 添加`compile bfcBuildConfig.deps.'bfc-log'`

> 如果提示有support-annotation包中间的注解冲突, 可以在依赖的时候排除掉注解包, 参考如下

  	compile (bfcBuildConfig.deps.'bfc-log'){
        exclude group: 'com.android.support'
        exclude module: 'support-annotations'
    }

### 2.使用

	 BfcLogger.d("Hello BfcLogger");		

![bfc_log_base](http://172.28.2.93/bfc/BfcLog/raw/develop/doc/pic/59fc084d58226f6690d0db20ebdd38a5.png)

```java	
BfcLogger.v("Hello BfcLogger");     
BfcLogger.d("Hello BfcLogger");     
BfcLogger.i("Hello BfcLogger");     
BfcLogger.w("Hello BfcLogger");     
BfcLogger.e("Hello BfcLogger");     
BfcLogger.wtf("Hello BfcLogger");     
BfcLogger.json( JSON_CONTENT ); 
```


 除了上面的接口外, BfcLogger提供了和系统 android.util.Log 一样的接口, 如果你现在使用的系统,可以直接替换即可     

	原	先:    	Log.d(TAG, "hello");     
	替换为:  	BfcLogger.d("hello");
	附加TAG:		BfcLogger.d(TAG, "hello");		

> Android系统的log,不支持tag换行; 由于效率问题,我这边并未检测输入的tag, 输入tag时,请不要输入换行

#### 配置
库本身不初始化也可以使用, 但是如果想达到部分自定义效果, 建议初始化一下  

- 简单初始化 

		BfcLogger.init( YOUR_TAG );	


- 修改默认Tag, 堆栈, 是否显示线程, 保存log等

	```java
	BfcLogger.init( YOUR_TAG )         	// 默认的Tag;	default "BfcLog"; 不使用tag时, 默认使用的tag
	    .showLog( true )				// Log开关;		default true
	    .logLevel(BfcLog.VERBOSE)      	// 显示Log的等级;default BfcLog.VERBOSE
	    .showThreadInfo( true )       	// 是否显示线程;	default false
	    .methodCount(2)					// 函数调用层数;	default 2;  设置为0时, 不显示函数调用
	    .methodOffset(0)				// 函数调用偏移;	default 0
	    .saveLog( false, SAVE_PATH );	// Log保存配置;	default false
	```
> 建议初始化配置的语句, 放到`Application.onCreate`中, 或者放在自定义日志工具类的static语句中


#### 临时配置
某些情况下, 我们希望临时**`调整单条log显示`**的Tag, 堆栈, 线程信息等, 可以使用下面的方法

```java
BfcLogger.tag( TAG ).d("hello");		// 修改tag
BfcLogger.method( 5 ).d("hello");		// 修改显示的函数调用层级
BfcLogger.thread( true ).d("hello");	// 修改是否显示线程信息
```
> 临时配置,都是在基于初始化配置之上修改, 并且一次log之后, 临时配置就失效


### 自定义日志工具类
BfcLogger都是提供的配置和打印Log方法,都是静态方法; 如果**`多处配置,会存在配置相互覆盖的情况`**;  如果你是提供库给其他人用,或者有自定义的日志工具类, **`建议使用BfcLog的实例`**, 防止出现上诉问题;

```java
// 你自己定义的工具类
public class L {
    private static BfcLog bfcLog;
    static {
        bfcLog = new BfcLog.Builder()
                .tag( YOUR_TAG )
                .showLog(true)
                .methodOffset(1)
                .methodCount(2)
                .showThreadInfo( true )
                .saveLog(false, SAVE_PATH)
                .build();
    }

	// 使用默认tag打印
    public static void e(String msg){
        bfcLog.e(msg);
    }

	// 使用自定义的tag打印
	public static void e(String tag, String msg){
		bfcLog.tag(tag).e(msg);
	}
}
```
> 使用`BfcLog.Builder`构建实例, 实例的构建的方法 和实例拥有的方法, 和`BfcLogger`的静态方法一致

##### 示例图片
![bfclog_more_example](http://172.28.2.93/bfc/BfcLog/raw/develop/doc/pic/747af0612ea930d01555e3a2539fe3ad.png)


----
## 方法耗时统计
### 要求
- jdk编译版本: 1.7

#### 1.添加依赖
1. 在根目录的build.gradle中加上插件依赖

		buildscript {
		    repositories {
		        jcenter()
		        maven { url "http://172.28.1.147:8081/nexus/content/repositories/thirdparty/"}
		    }
		    dependencies {
		        classpath 'com.android.tools.build:gradle:2.1.0'
			classpath bfcBuildConfig.deps.'bfc-log-plugin'
		    }
		}

> 如果使用的不是网络配置, 添加的是`classpath "com.eebbk.bfc:bfc-log-plugin:2.1.0"`


2. 项目上使用BfcLog耗时统计插件

		apply plugin: 'com.android.application'
		apply plugin: 'BfcLog'   //BfcLog耗时统计的插件

3. 项目build.gradle中指定jdk的版本

		android {
			...
		    compileOptions {
		        sourceCompatibility JavaVersion.VERSION_1_7
		        targetCompatibility JavaVersion.VERSION_1_7
		    }
			...
		}


#### 2. 使用
只需要在需要统计耗时的方法上, 加上注解 `@BfcLog`

```java
@BfcLogTime
private String add(int x, String input){
    ...
}
```

>打印方法耗时的功能实现的原理是, 通过修改编译后的class字节码, 在方法前后插入了一段代码, 从而去计算耗时; 同时,由于在方法前后插入了代码, 会造成原方法的调用堆栈变化, 从而打印的log中的方法调用层级,可能会有部分名称变化 

![bfclog_time](http://172.28.2.93/bfc/BfcLog/raw/develop/doc/pic/584eb5a8cd4bca9b5b670aae9fab48d8.png)     
统计效果如上图, 会将输入输出参数 和耗时打印出来

