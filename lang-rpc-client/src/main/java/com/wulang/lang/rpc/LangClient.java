package com.wulang.lang.rpc;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LangClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 10001));
        OutputStream outputStream = socket.getOutputStream();
        Context context = new Context();
        context.message = "hello";
        context.identity=LangRpcFilter.Clint;

        new LangRpcFilter().invoke(context);

        outputStream.write((new Gson().toJson(context)+"@").getBytes());
        outputStream.close();
    }
}
