package com.wulang.lang.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class LangServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(10001);
        while (true) {
            final Socket socket = serverSocket.accept();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[2048];
                        inputStream.read(bytes);
                        String str = new String(bytes);
                        str = str.split("@")[0];

//                        str=str.substring(0,str.indexOf(" "));
                        System.out.println("*"+str+"*");
                        Context context = new Gson().fromJson(str, Context.class);
                        context.identity = LangRpcFilter.Server;
                        new LangRpcFilter().invoke(context);
                        inputStream.close();
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            }).start();
        }
    }

}
