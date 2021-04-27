package com.luiz.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;


public class LoginWindow extends JFrame {
    private final ChatClient client;
    JTextField usernameField = new JTextField();
    JTextField countryField = new JTextField();
    JTextField ageField = new JTextField();
    JLabel usernameLabel = new JLabel("Username");
    JLabel countryLabel = new JLabel("Country");
    JLabel ageLabel = new JLabel("Age");
    JButton loginButton = new JButton("Login");

    public LoginWindow() {
        super("Login");

        this.client = new ChatClient("localhost", 8818);
        client.connect();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    client.logoff();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        usernameLabel.setLabelFor(usernameField);
        countryLabel.setLabelFor(countryField);
        ageLabel.setLabelFor(ageField);
        p.add(usernameLabel);
        p.add(usernameField);
        p.add(countryLabel);
        p.add(countryField);
        p.add(ageLabel);
        p.add(ageField);
        p.add(loginButton);

        loginButton.addActionListener(e -> doLogin());

        getContentPane().add(p, BorderLayout.CENTER);

        pack();

        setVisible(true);
    }

    // it'll grab the text fields and send to the server
    private void doLogin() {
        String login = usernameField.getText();
        String country = countryField.getText();
        String age = ageField.getText();

        try {
            if (client.login(login, country, age)) {
                // bring up the user list window
                UserListPane userListPane = new UserListPane(client);
                JFrame frame = new JFrame("User List");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            client.logoff();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                });
                frame.setSize(400, 600);

                frame.getContentPane().add(userListPane, BorderLayout.CENTER);
                frame.setVisible(true);

                setVisible(false);
            } else {
                // show error message
                JOptionPane.showMessageDialog(this, "Invalid login/password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LoginWindow loginWin = new LoginWindow();
        loginWin.setVisible(true);
    }
}
