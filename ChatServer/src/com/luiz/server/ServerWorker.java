package com.luiz.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String username = null;
    private String country = null;
    private String age = null;

    private Long lastTime;
    private OutputStream outputStream;
    private final ArrayList<String> blockedUsers = new ArrayList<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        updateLastTime();
    }

    private void updateLastTime() {
        this.lastTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null) {
            System.out.println(line);
            updateLastTime();
            String[] tokens = line.split(" ", 4);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("/logoff".equals(cmd) || "/quit".equalsIgnoreCase(cmd)) {
                    handleLogoff(null);
                    break;
                } else if ("/login".equalsIgnoreCase(cmd)) {
                    handleLogin(tokens);
                } else if ("/block".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = line.split(" ", 2);
                    if (tokensMsg.length == 2) handleBlockUser(tokensMsg[1]);
                    else send("Invalid Username");
                } else if ("/unblock".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = line.split(" ", 3);
                    if (tokensMsg.length == 2) handleUnblockUser(tokensMsg[1]);
                    else send("Invalid Username");
                } else if ("/users".equalsIgnoreCase(cmd)) {
                    sendUsers();
                } else {
                    handleMessage(line);
                }
            }
        }

        clientSocket.close();
    }

    public void handleLogoff(String msg) throws IOException {
        server.removeWorker(this);
        if (msg != null) send(msg);
        // send other online users current user's status
        String onlineMsg = "offline " + username + "\n";
        for(ServerWorker worker : server.getWorkerList()) {
            if (worker.getUsername() != null
                    && !username.equals(worker.getUsername())
                    && !worker.isUserBlocked(username)
                    && !isUserBlocked(worker.getUsername())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    private void handleLogin(String[] tokens) throws IOException {
        if (tokens.length == 4) {
            String username = tokens[1];
            String country = tokens[2];
            String age = tokens[3];

            if (isValidUsername(username) ) {
                this.username = username;
                this.country = country;
                this.age = age;
                System.out.println("User logged in succesfully: " + username);

                List<ServerWorker> workerList = server.getWorkerList();

                // send current user all other online logins
                // checks if the user is blocked

                send("ok login\n");
                for(ServerWorker worker : workerList) {
                    if (worker.getUsername() != null
                            && !worker.isUserBlocked(username)
                            && !isUserBlocked(worker.getUsername())) {
                        if (!username.equals(worker.getUsername())) {
                            String msg2 = "online " + worker.getUsername() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg = "online " + username + "\n";
                for(ServerWorker worker : workerList) {
                    if (!username.equals(worker.getUsername())
                            && !worker.isUserBlocked(username)
                            && !isUserBlocked(worker.getUsername())) {
                        worker.send(onlineMsg);
                    }
                }
            }
        } else {
            String msg = "error login, you must pass Name, Country and Age " +
                    "(All in one word) and the username should be unique\n";
            send(msg);
            System.err.println("Login failed for " + username);
        }
    }

    // Checks both sides to see if the user is blocked or not

    private void handleMessage(String body) {
        for(ServerWorker worker : server.getWorkerList()) {
            if (worker.getUsername() != null
                    && !worker.isUserBlocked(this.username)
                    && !isUserBlocked(worker.getUsername())) {
                String outMsg = "msg " + username + " " + body + "\n";
                worker.send(outMsg);
            }
        }
    }
    private void handleBlockUser(String s) {
        this.blockedUsers.add(s);
        send("User: " + s + " was blocked" + "\n");
    }

    private void handleUnblockUser(String s) {
        for (int i = 0; i < this.blockedUsers.size(); i++) {
            if (this.blockedUsers.get(i).equalsIgnoreCase(s)) {
                this.blockedUsers.remove(i);
                send("User: " + s + " was unblocked" + "\n");
                return;
            }
        }
        send("User: " + s + " not found" + "\n");
    }

    private void sendUsers() {
        String msg = "users ";
        for(ServerWorker worker : server.getWorkerList()) {
            if (worker.getUsername() != null
                    && !worker.isUserBlocked(this.username)
                    && !isUserBlocked(worker.getUsername())) {
                msg += worker.getUsername() + " ";
            }
        }
        System.out.println("users: " + msg);
        send(msg + "\n");
    }

    private boolean isUserBlocked(String login) {
        for (String user : this.blockedUsers) {
            System.out.println("user: " + user + "");
            if (user.equalsIgnoreCase(login)) return true;
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    // Checks if the username is valid
    private boolean isValidUsername(String username) {
        for (ServerWorker worker : server.getWorkerList()) {
            if (worker.getUsername() != null && worker.getUsername().equalsIgnoreCase(username))
                return false;
        }
        return true;
    }

    private void send(String msg) {
        if (username != null) {
            try {
                outputStream.write(msg.getBytes());
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Long getLastTime() {
        return lastTime;
    }
}
