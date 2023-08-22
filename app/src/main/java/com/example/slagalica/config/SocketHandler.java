package com.example.slagalica.config;

import android.util.Log;

import com.example.slagalica.BuildConfig;

import java.util.Map;
import java.util.Properties;

import io.socket.client.IO;
import io.socket.client.Socket;


public class SocketHandler {
    static Socket socket;

    public static void setSocket(){
        try {
            socket = IO.socket("http://192.168.1.5:3000");
            Log.d("SocketHandler", "Socket initialized");
        }catch (Exception e){
            Log.d("SocketError", e.getMessage().toString());
        }
    }

    public static Socket getSocket(){
        return socket;
    }

}
