'use strict';

const LOG_TAG = "Stack-Frida";
const INTERVAL = 600000;

var startTime = 0;

if(Java.available){
    Java.perform(function(){
        startTime = Date.now();
        var Log = Java.use("android.util.Log");
        try{
            Log.v(LOG_TAG,"init - " + Process.getCurrentThreadId())
            if(Process.getCurrentThreadId() != Process.id){
                return;
            }
            var currentTime = new Date().getTime();
            Log.v(LOG_TAG, "inject start >> " + currentTime);
            var fridaInit = Java.use("com.tools.printstack.YpFridaInit");
            var jsonConfig = JSON.parse(fridaInit.getConfig());
            for(var key in jsonConfig){
                doHook(jsonConfig[key]["className"],jsonConfig[key]["methodName"])
            }
            Log.v(LOG_TAG, "inject end >> " + (new Date().getTime() - currentTime));
        }catch(e){
            Log.e(LOG_TAG, e.toString());
        }
    })
}

function doHook(className, methodName){
    var Log = Java.use("android.util.Log");
    try{
        var fridaInit = Java.use("com.tools.printstack.YpFridaInit");
        var javaClazz = Java.use(className);
        if(javaClazz[methodName]){
            for(var o = 0; o < javaClazz[methodName].overloads.length;o++){
                javaClazz[methodName].overloads[o].implementation = function(){
                    if(Date.now() - startTime < INTERVAL){
                        var content = buildOneContent(className+"."+methodName)
                        fridaInit.writeFile(content);
                    }
                    return this[methodName].apply(this, arguments);
                }
            }
        }else{
            Log.w(LOG_TAG, className + "." + methodName + "does not exist!");
        }
    }catch(error){

    }
}



function buildOneContent(text) {
    var threadClz = Java.use("java.lang.Thread");
    var androidLogClz = Java.use("android.util.Log");
    var exceptionClz = Java.use("java.lang.Exception");
    var processClz = Java.use("android.os.Process");
    var currentThread = threadClz.currentThread();

    //var invokeId = Math.random().toString(36).slice(- 8);
    var pid = processClz.myPid();
    var threadId = processClz.myTid();
    var threadName = currentThread.getName();
    var currentTime = new Date().getTime();
    var stackInfo = androidLogClz.getStackTraceString(exceptionClz.$new());

    var str = (">>>>>> pid:" + pid + ", thread(id:" + threadId + ", name:" + threadName + "), timestamp:" + currentTime + "\n");
    str += text + "\n";
    str += stackInfo;
    str += ("<<<<<<\n");

    return str;
};