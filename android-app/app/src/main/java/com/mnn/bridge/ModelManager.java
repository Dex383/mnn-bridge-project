package com.mnn.bridge;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Iterator;

public class ModelManager {
    private static final String TAG = "ModelManager";
    private static final String ALIAS_FILE = "model_aliases.json";
    private Context context;
    private Map<String, String> aliases = new ConcurrentHashMap<>();

    public ModelManager(Context context) {
        this.context = context;
        loadAliases();
    }

    private void loadAliases() {
        File file = new File(context.getFilesDir(), ALIAS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                aliases.put(key, json.getString(key));
            }
            Log.d(TAG, "Loaded aliases: " + aliases.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error loading aliases: " + e.getMessage());
        }
    }

    private synchronized void saveAliases() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : aliases.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            File file = new File(context.getFilesDir(), ALIAS_FILE);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving aliases: " + e.getMessage());
        }
    }

    public boolean registerModel(String alias, String externalPath) {
        // Basic path traversal protection
        if (externalPath == null || externalPath.contains("..")) {
            Log.e(TAG, "Invalid path provided: " + externalPath);
            return false;
        }

        File sourceFile = new File(externalPath);
        if (!sourceFile.exists()) {
            Log.e(TAG, "Source model file does not exist: " + externalPath);
            return false;
        }

        // Copy to internal storage for stability
        String internalPath = context.getFilesDir() + "/models/" + alias + ".mnn";
        File destFile = new File(internalPath);
        
        // Optimization: If file exists and size matches, skip copying
        if (destFile.exists() && destFile.length() == sourceFile.length()) {
            Log.d(TAG, "Model already exists internally and size matches. Skipping copy: " + alias);
            aliases.put(alias, internalPath);
            saveAliases();
            return true;
        }

        destFile.getParentFile().mkdirs();

        try {
            copyFile(sourceFile, destFile);
            aliases.put(alias, internalPath);
            saveAliases();
            Log.d(TAG, "Model registered: " + alias + " -> " + internalPath);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying model file: " + e.getMessage());
            return false;
        }
    }

    public String getInternalPath(String alias) {
        return aliases.get(alias);
    }

    public boolean unloadModel(String alias) {
        if (aliases.containsKey(alias)) {
            String path = aliases.get(alias);
            
            // Delete the physical file to prevent storage leaks
            File file = new File(path);
            if (file.exists()) {
                if (!file.delete()) {
                    Log.w(TAG, "Could not delete model file: " + path);
                } else {
                    Log.d(TAG, "Deleted internal model file: " + path);
                }
            }
            
            aliases.remove(alias);
            saveAliases();
            return true;
        }
        return false;
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[1024 * 8];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}
