package com.mnn.bridge;

import org.nanohttpd.Firesponsive;
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
        Log.d(TAG, "Received request: " + uri);

        if (uri.equals("/status")) {
            return newFixedLengthResponse(Response.Status.OK, "application/json", 
                "{"status": "online", "hardware": "Snapdragon 888", "engine": "MNN"}");
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Endpoint not found");
    }
}
