package com.luiz.client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener {

    private ChatClient client;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();
    private JScrollPane jScrollPane;
    JScrollBar jScrollBar;

    public MessagePane(ChatClient client) {

        this.client = client;

        client.addMessageListener(this);

        jScrollPane = new JScrollPane(messageList);
        jScrollBar = jScrollPane.getVerticalScrollBar();

        setLayout(new BorderLayout());
        add(jScrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        try {
            client.triggerUpdateUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputField.addActionListener(e -> {
            try {
                String text = inputField.getText();
                client.msg(text);
                listModel.addElement("You: " + text);
                inputField.setText("");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    // Listener override, this will do the trick to listen the messages
    @Override
    public void onMessage(String fromUsername, String msgBody) {
        if (!this.client.getUsername().equalsIgnoreCase(fromUsername)) {
            String line = fromUsername + ": " + msgBody;
            listModel.addElement(line);
            this.jScrollBar.setValue(this.jScrollBar.getMinimum());
        }
    }
}
