package com.mnn.bridge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class MNNBridgeService extends Service {
    private static final String TAG = "MNNBridgeService";
    private static final String CHANNEL_ID = "MNNBridgeChannel";
    private MNNBridgeServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MNNBridgeService created");
        
        // Start the HTTP server on port 8080
        server = new MNNBridgeServer(8080);
        try {
            server.start();
            Log.d(TAG, "Server started on port 8080");
        } catch (Exception e) {
            Log.e(TAG, "Error starting server: " + e.getMessage());
        }

        startForegroundService();
    }

    private void startForegroundService() {
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                CHANNEL_ID,
                "MNN Bridge Service",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MNN Bridge Active")
                .setContentText("Hardware acceleration daemon is running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand called");
        return START_STICKY; // Ensure service restarts if killed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            Log.d(TAG, "Server stopped");
        }
    }
}
