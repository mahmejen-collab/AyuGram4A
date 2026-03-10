package org.telegram.plugins;

import android.content.Context;
import android.util.Log;
import android.os.Environment;

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
    private static File pluginsDir = null;
    
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
    
    public static File getPluginsDir() {
        if (pluginsDir == null) {
            // Создаём папку в публичной памяти телефона
            File baseDir = new File(Environment.getExternalStorageDirectory(), "SaidGram");
            pluginsDir = new File(baseDir, "plugins");
            pluginsDir.mkdirs();
            
            Log.d(TAG, "Папка для плагинов: " + pluginsDir.getAbsolutePath());
        }
        return pluginsDir;
    }
    
    public static void loadPlugins() {
        initPython();
        
        File dir = getPluginsDir();
        
        if (!dir.exists()) {
            Log.d(TAG, "Папка с плагинами не найдена: " + dir.getAbsolutePath());
            return;
        }
        
        File[] plugins = dir.listFiles((d, name) -> name.endsWith(".py"));
        
        if (plugins != null) {
            for (File plugin : plugins) {
                try {
                    String moduleName = plugin.getName().replace(".py", "");
                    py.getModule(moduleName);
                    Log.d(TAG, "Загружен плагин: " + plugin.getName());
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка загрузки плагина: " + plugin.getName(), e);
                }
            }
        }
    }
    
    // Метод для перезагрузки плагинов (можно вызвать из меню)
    public static void reloadPlugins() {
        Log.d(TAG, "Перезагрузка плагинов...");
        // Python уже загружен, просто перезагружаем модули
        loadPlugins();
    }
}
