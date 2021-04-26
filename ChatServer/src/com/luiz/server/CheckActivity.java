package com.luiz.server;

import java.io.IOException;

public class CheckActivity extends Thread {
    private final Server server;
    private final int timeout;

    public CheckActivity(Server server, int timeout) {
        this.server = server;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        // Will check from time to time if the user is still alive
        while (true) {
            for (ServerWorker worker: server.getWorkerList()) {
                if (worker.getLastTime() != null &&
                        System.currentTimeMillis() - worker.getLastTime() > timeout) {
                    try {
                        worker.handleLogoff("Disconnected due to inactivity");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                // checks every minute
                sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
