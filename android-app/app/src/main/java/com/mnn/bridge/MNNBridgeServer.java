package com.mnn.bridge;

import org.nanohttpd.NanoHTTPD;
import android.util.Log;

public class MNNBridgeServer extends NanoHTTPD {
    private static final String TAG = "MNNBridgeServer";

    public MNNBridgeServer(int port) {
        super(port);
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

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Endpoint not found");
    }
}
