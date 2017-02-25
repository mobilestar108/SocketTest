package com.gmbeauty.sockettest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketService extends Service {

    private Socket ioSocket;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initConnection();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initConnection() {
        String jwtToken = getResources().getString(R.string.JWT_TOKEN);
        String socketHost = getResources().getString(R.string.SOCKET_HOST);
        IO.Options options = new IO.Options();
        options.query = "token=" + jwtToken;
        options.reconnectionDelay = 0;
        options.reconnection = true;
        options.reconnectionAttempts = 100;
        options.forceNew = true;
        try {
            ioSocket = IO.socket(socketHost, options);
            ioSocket.on(Socket.EVENT_CONNECT, onConnect);
            ioSocket.on(Socket.EVENT_RECONNECT, onReconnect);
            ioSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            ioSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            ioSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            ioSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("SocketEvent", "Socket Connected");

            try {
                JSONObject rider = new JSONObject();
                rider.put("_id", "58b1a60c098db06645af5bee");
                rider.put("email", "test@testuser.com");
                rider.put("password", "123456");
                rider.put("userType", "rider");
                rider.put("fname", "Loredana");
                rider.put("lname", "Zamfir");
                rider.put("phoneNo", "1112223344");

                JSONObject tripRequest = new JSONObject();
                tripRequest.put("srcLoc", "[25, 26]");
                tripRequest.put("destLoc", "[27, 28]");
                tripRequest.put("pickUpAddress", "geekyants");
                tripRequest.put("destAddress", "bommanahalli");
                tripRequest.put("latitudeDelta", 0.022);
                tripRequest.put("longitudeDelta", 0.023);

                JSONObject payload = new JSONObject();
                payload.put("rider", rider);
                payload.put("tripRequest", tripRequest);

                ioSocket.emit("requestTrip", payload);
//                ioSocket.emit("requestTrip", payload, new Ack() {
//                    @Override
//                    public void call(Object... args) {
//                        Log.e("SocketEvent", "Response");
//                    }
//                });
                ioSocket.on("tripRequestUpdated", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e("SocketEvent", "Request Trip Event");
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("SocketEvent", "Socket Reconnecting");
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("SocketEvent", "Socket Disconnected");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("SocketEvent", "Socket Connect Error");
        }
    };

    private Emitter.Listener onRequestTrip = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("SocketEvent", "Request Trip");
        }
    };

    private void reconnect() {
        disconnect();
        initConnection();
    }

    private void disconnect() {
        if (ioSocket != null) {
            ioSocket.off();
            ioSocket.disconnect();
            ioSocket = null;
        }
    }
}