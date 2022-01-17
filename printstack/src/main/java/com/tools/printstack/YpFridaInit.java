package com.tools.printstack;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * @autor yangping
 */
@Keep
public class YpFridaInit {

    public static final String TAG = YpFridaInit.class.getName();
    public static final String JS_FILE_NAME = "stack_frida.js";
    public static final String GADGET_FILE_NAME = "libgadget";
    public static final String LOG_FILE_NAME = "stack_frida.log";
    public static final String JSON_CONFIG_NAME = "frida-config.json";
    public static final HandlerThread ht = new HandlerThread("frida-stack-worker");
    public static  Handler handler;

    private static Context appCtx;

    @SuppressWarnings({"UnsafeDynamicallyLoadedCode","ResultOfMethodCallIgnored"})
    public static void init(Context context){
        appCtx = context;
        ht.start();
        handler = new Handler(ht.getLooper());
        //获取执行js文件路径
        String jsPatch = context.getCacheDir().getAbsolutePath() + File.separator + JS_FILE_NAME;
        long time = System.currentTimeMillis();
        //生成libgadget.so文件
        releaseGadgetSO(context);
        //生成js执行脚本文件
        releaseJSFile(context,jsPatch);
        //生成libgadget.config.so文件
        releaseConfigSO(context,jsPatch);
        Log.v(TAG, "printstack init time:" + (System.currentTimeMillis() - time));
        //删除之前写过的stack_frida.log日志文件
        deleteAndReWriteLog(context);
        //加载so
        System.load(context.getCacheDir() + File.separator + GADGET_FILE_NAME + ".so");
    }

    @SuppressWarnings({"UnsafeDynamicallyLoadedCode","ResultOfMethodCallIgnored"})
    private static void deleteAndReWriteLog(Context context) {
        File file = new File(context.getExternalCacheDir().getAbsolutePath() + File.separator + LOG_FILE_NAME);
        if (file.exists() && !file.delete()) {
            Log.v(TAG, "delete report file failed!");
            throw new RuntimeException("YpFridaInit init failed - 0");
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("YpFridaInit init failed - 1", e);
        }
    }


    /**
     * {
     *   "interaction": {
     *     "type": "script",
     *     "path": jsPatch,
     *     "on_change":"reload
     *   }
     * }
     * @param context
     * @param jsPatch
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void releaseConfigSO(Context context, String jsPatch) {
        File file = new File(context.getCacheDir().getAbsolutePath() + File.separator + GADGET_FILE_NAME + ".config.so");
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("YpFridaInit releaseConfigSO failed - file cant delete: -1");
        }

        JSONObject jsonObject = new JSONObject();
        JSONObject interaction = new JSONObject();
        try (FileWriter fileWriter = new FileWriter(file, false);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            interaction.put("type", "script");
            interaction.put("path", jsPatch);
            interaction.put("on_change", "reload");
            jsonObject.put("interaction", interaction);

            writer.append(jsonObject.toString());
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("YpFridaInit releaseConfigSO failed - 1", e);
        }


    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void releaseJSFile(Context context,String jsPath) {
        File file = new File(jsPath);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("YpFridaInit releaseJsFile failed -  file cant delete: -1");
        }

        try (InputStream inputStream = context.getApplicationContext().getAssets().open(JS_FILE_NAME);
             FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            file.createNewFile();

            byte[] bytes = new byte[8192];
            int readLen;
            while ((readLen = inputStream.read(bytes)) > 0) {
                bos.write(bytes, 0, readLen);
            }
        } catch (Exception e) {
            throw new RuntimeException("YpFridaInit releaseJsFile failed - file create error:", e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void releaseGadgetSO(Context context) {
        File file = new File(context.getCacheDir().getAbsolutePath(),GADGET_FILE_NAME + ".so");
        if (file.exists() && !file.delete()){
            throw new RuntimeException("YpFridaInit releaseGadgetSO failed - file cant delete: -1");
        }

        try(InputStream inputStream = context.getAssets()
                .open(GADGET_FILE_NAME + (is64bit(context) ? "64":"32"));
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos)){

            file.createNewFile();

            byte[] bytes = new byte[8192];
            int readLen;
            while ((readLen = inputStream.read(bytes)) > 0){
                bos.write(bytes,0,readLen);
            }
        }catch (Exception e){
            throw new RuntimeException("YpFridaInit releaseGadgetSO failed - file create error:"+e);
        }
    }

    // check 32 or 64位
    private static boolean is64bit(Context context) {
        File file = new File(context.getApplicationInfo().nativeLibraryDir);
        return file.getName().equals("arm64");
    }

    /**
     * js脚本调用，获取自定义config的配置
     */
    @Keep
    private static String getConfig() throws JSONException{
        JSONObject stackJson = getApiJson(JSON_CONFIG_NAME);
        return stackJson.toString();
    }

    @Keep
    private static void writeFile(String content) {
        handler.post(() -> {
            Log.v("Frida-worker", Process.myTid() + " - " + content);

            File file = new File(appCtx.getExternalCacheDir().getAbsolutePath() + File.separator + LOG_FILE_NAME);
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.append(content);
                writer.flush();
            } catch (Exception e) {
                throw new RuntimeException("YpFridaInit writeFile failed", e);
            }
        });
    }



    private static JSONObject getApiJson(String fileName) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(appCtx.getApplicationContext().getAssets().open(fileName));
             BufferedReader bf = new BufferedReader(inputStreamReader)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bf.readLine()) != null) {
                sb.append(line);
            }

            return new JSONObject(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("YpFridaInit getApiJson failed, " + fileName, e);
        }
    }
}
