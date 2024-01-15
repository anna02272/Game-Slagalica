package com.example.slagalica.config;

import android.util.Log;

import com.example.slagalica.BuildConfig;

import io.socket.client.IO;
import io.socket.client.Socket;


public class SocketHandler {
    static Socket socket;
    private static final String SERVER_BASE_URL = "http://192.168.1.6:3000";


    public static void setSocket(){
        try {
//            socket = IO.socket("http://" + BuildConfig.IP_ADDR + ":3000");
            socket = IO.socket(SERVER_BASE_URL);
        }catch (Exception e){
            Log.d("SocketError", e.getMessage().toString());
        }
    }

    public static Socket getSocket(){
        return socket;
    }
    public static String getServerBaseUrl() {
        return SERVER_BASE_URL;
    }
}
