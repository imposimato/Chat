package com.luiz.server;

public class ServerMain {
    // It'll create the main server
    public static void main(String[] args) {
        int port = 8818;
        Server server = new Server(port);
        server.start();
    }
}
