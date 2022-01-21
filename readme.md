## Get Stack Info by Frida

a simple tool to get method stack

为了更好的排查代码运行时问题，打印运行堆栈等信息

### How to use

##### Step 1:  clone this project



##### Step 2: 更高assets目录下的 frida-config.json文件，填入你想打出堆栈信息的方法

```java
//比如app例子中 hook了点击监听中的 clickStack()方法  那么json就写成这样
{
    "com.tools.yangpingfrida.MainActivity:clickStack": {
        "key": "com.tools.yangpingfrida.MainActivity:clickStack",
        "className": "com.tools.yangpingfrida.MainActivity",
        "methodName": "clickStack",
        "paramCount": 0,
        "returnType": "void"
    }
}

//如果你想hook framework里面的方法，比如Handler的dispatchMessagen方法
{
    "com.tools.yangpingfrida.MainActivity:clickStack": {
        "key": "com.tools.yangpingfrida.MainActivity:clickStack",
        "className": "com.tools.yangpingfrida.MainActivity",
        "methodName": "clickStack",
        "paramCount": 0,
        "returnType": "void"
    },
    "android.os.Handler:dispatchMessage": {   
        "key": "android.os.Handler:dispatchMessage", 
        "className": "android.os.Handler",
        "methodName": "dispatchMessage",
        "paramCount": 1,
        "returnType": "void"
    }
}

```



##### step3: 将printstack构建成aar  比如: printstack-release.aar, 并集成进你的项目，放到libs里面就可以

```java
1.build.gradle:中添加路径

allprojects{
  repositories{
   		 ......
       flatDir { dirs 'libs' }
  }
}

2. add dependence
implementation (name:' printstack-release', ext:'aar')
```



##### step4：运行你的项目。工具设置了打印时间是10min所以只能打印10min内的消息。



##### step5: 导出日志信息    adb pull  /sdcard/Android/data/你的应用包名/cache/stack_frida.log

```java
>>>>>> pid:1379, thread(id:1523, name:frida-stack-worker), timestamp:1642405020522
android.os.Handler.dispatchMessage
java.lang.Exception
	at android.os.Handler.dispatchMessage(Native Method)
	at android.os.Looper.loop(Looper.java:214)
	at android.os.HandlerThread.run(HandlerThread.java:65)
<<<<<<
>>>>>> pid:1379, thread(id:1379, name:main), timestamp:1642405020505
com.tools.yangpingfrida.MainActivity.clickStack
java.lang.Exception
	at com.tools.yangpingfrida.MainActivity.clickStack(Native Method)
	at com.tools.yangpingfrida.MainActivity.lambda$onCreate$0$com-tools-yangpingfrida-MainActivity(MainActivity.java:17)
	at com.tools.yangpingfrida.MainActivity$$ExternalSyntheticLambda0.onClick(Unknown Source:2)
	at android.view.View.performClick(View.java:7360)
	at android.widget.TextView.performClick(TextView.java:14234)
	at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1119)
	at android.view.View.performClickInternal(View.java:7326)
	at android.view.View.access$3200(View.java:849)
	at android.view.View$PerformClick.run(View.java:27808)
	at android.os.Handler.handleCallback(Handler.java:873)
	at android.os.Handler.dispatchMessage(Handler.java:99)
	at android.os.Handler.dispatchMessage(Native Method)
	at android.os.Looper.loop(Looper.java:214)
	at android.app.ActivityThread.main(ActivityThread.java:7050)
	at java.lang.reflect.Method.invoke(Native Method)
	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:494)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:965)
<<<<<<
>>>>>> pid:1379, thread(id:1523, name:frida-stack-worker), timestamp:1642405020527
android.os.Handler.dispatchMessage
java.lang.Exception
	at android.os.Handler.dispatchMessage(Native Method)
	at android.os.Looper.loop(Looper.java:214)
	at android.os.HandlerThread.run(HandlerThread.java:65)
<<<<<<
```











