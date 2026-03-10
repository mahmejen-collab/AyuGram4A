package org.telegram.plugins;

import android.content.Context;
import android.util.Log;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.telegram.messenger.ApplicationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PluginLoader {
    private static final String TAG = "PluginLoader";
    private static Python py = null;
    private static boolean initialized = false;
    
    public static void initPython() {
        if (!initialized) {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(ApplicationLoader.applicationContext));
            }
            py = Python.getInstance();
            initialized = true;
            Log.d(TAG, "Python инициализирован");
        }
    }
    
    public static void loadPlugins() {
        initPython();
        
        // Копируем плагины из assets в доступную папку
        File pluginsDir = new File(ApplicationLoader.applicationContext.getFilesDir(), "plugins");
        pluginsDir.mkdirs();
        
        try {
            String[] plugins = ApplicationLoader.applicationContext.getAssets().list("plugins");
            if (plugins != null) {
                for (String plugin : plugins) {
                    if (plugin.endsWith(".py")) {
                        File destFile = new File(pluginsDir, plugin);
                        
                        // Копируем файл из assets
                        InputStream is = ApplicationLoader.applicationContext.getAssets().open("plugins/" + plugin);
                        FileOutputStream os = new FileOutputStream(destFile);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                        os.close();
                        is.close();
                        
                        Log.d(TAG, "Скопирован плагин: " + plugin);
                        
                        // Загружаем модуль Python
                        String moduleName = plugin.replace(".py", "");
                        py.getModule(moduleName);
                        Log.d(TAG, "Загружен плагин: " + plugin);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки плагинов", e);
        }
    }
}
