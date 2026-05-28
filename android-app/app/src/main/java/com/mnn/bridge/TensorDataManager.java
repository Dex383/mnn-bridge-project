package com.mnn.bridge;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TensorDataManager {
    private static final String TAG = "TensorDataManager";
    private Context context;
    private File sharedDir;

    public TensorDataManager(Context context) {
        this.context = context;
        // Using the app's internal files directory as the shared data plane for stability
        // In a real deployment, this would be the /sdcard/Android/data/... folder
        this.sharedDir = new File(context.getExternalFilesDir(null));
        if (sharedDir == null) {
            this.sharedDir = context.getFilesDir();
        }
        Log.d(TAG, "Data Plane initialized at: " + sharedDir.getAbsolutePath());
    }

    public File getInputFile() {
        return new File(sharedDir, "input.bin");
    }

    public File getOutputFile() {
        return new File(sharedDir, "output.bin");
    }

    /**
     * Reads a binary tensor from a file into a ByteBuffer.
     */
    public ByteBuffer readTensor(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Tensor file not found: " + file.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            long size = file.length();
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            byte[] bytes = new byte[8192];
            int read;
            while ((read = fis.read(bytes)) != -1) {
                buffer.put(bytes, 0, read);
            }
            buffer.flip();
            return buffer;
        }
    }

    /**
     * Writes a ByteBuffer tensor to a binary file.
     */
    public void writeTensor(ByteBuffer buffer, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Ensure we read from the start of the buffer
            buffer.rewind();
            byte[] bytes = new byte[8192];
            while (buffer.hasRemaining()) {
                int len = Math.min(buffer.remaining(), bytes.length);
                buffer.get(bytes, 0, len);
                fos.write(bytes, 0, len);
            }
        }
    }

    public String getSharedDirPath() {
        return sharedDir.getAbsolutePath();
    }
}
