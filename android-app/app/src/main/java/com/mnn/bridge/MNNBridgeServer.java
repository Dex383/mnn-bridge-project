package com.mnn.bridge;

import org.nanohttpd.NanoHTTPD;
import android.util.Log;
import org.json.JSONObject;
import java.util.Map;

public class MNNBridgeServer extends NanoHTTPD {
    private static final String TAG = "MNNBridgeServer";
    private ModelManager modelManager;
    private TensorDataManager dataManager;

    public MNNBridgeServer(int port, ModelManager modelManager, TensorDataManager dataManager) {
        super(port);
        this.modelManager = modelManager;
        this.dataManager = dataManager;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String remoteAddress = session.getRemoteAddress();
        Log.d(TAG, String.format("Received request [%s] from %s", uri, remoteAddress));

        if (uri.equals("/status")) {
            return newFixedLengthResponse(Response.Status.OK, "application/json", 
                "{\"status\": \"online\", \"hardware\": \"Snapdragon 888\", \"engine\": \"MNN\"}");
        }

        if (uri.startsWith("/register")) {
            return handleRegister(session);
        }

        if (uri.startsWith("/unload")) {
            return handleUnload(session);
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Endpoint not found");
    }

    private Response handleRegister(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String alias = params.get("alias");
            String path = params.get("path");

            if (alias == null || path == null) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", 
                    "{\"status\": \"error\", \"message\": \"Missing alias or path\"}");
            }

            if (modelManager.registerModel(alias, path)) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", 
                    "{\"status\": \"success\", \"message\": \"Model registered successfully\"}");
            } else {
                return newFixedLengthResponse(Response.Status.INTERNAL_SERVER_ERROR, "application/json", 
                    "{\"status\": \"error\", \"message\": \"Failed to register model\"}");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during register: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_SERVER_ERROR, "application/json", 
                "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }

    private Response handleUnload(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String alias = params.get("alias");

            if (alias == null) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", 
                    "{\"status\": \"error\", \"message\": \"Missing alias\"}");
            }

            if (modelManager.unloadModel(alias)) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", 
                    "{\"status\": \"success\", \"message\": \"Model unloaded successfully\"}");
            } else {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", 
                    "{\"status\": \"error\", \"message\": \"Alias not found\"}");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during unload: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_SERVER_ERROR, "application/json", 
                "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
