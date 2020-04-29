package com.wulang.lang.rpc;

public class LangRpcFilter {
    public static final String Server = "server";
    public static final String Clint = "client";

    public Context invoke(Context context) {
        System.out.println(context.identity + context.message);
        return context;
    }
}
