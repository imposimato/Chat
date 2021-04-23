package com.luiz.client;

public interface MessageListener {
    void onMessage(String fromLogin, String msgBody);
}
