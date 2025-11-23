package com.github.jrohatsch.moqqa.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.MqttServerCertificate;
import com.github.jrohatsch.moqqa.data.MqttUsernamePassword;
import com.github.jrohatsch.moqqa.utils.TextUtils;

public class ConnectionFrame {
    private final Datahandler datahandler;
    private final Runnable onConnect;

    public ConnectionFrame(Datahandler datahandler, Runnable onConnect) {
        this.datahandler = datahandler;
        this.onConnect = onConnect;
    }


    public JFrame get() {
        JFrame connectFrame = new JFrame();

        connectFrame.setTitle("Moqqa: %s".formatted(TextUtils.getText("label.connectOptions")));
        connectFrame.setLayout(new GridBagLayout());
        connectFrame.setSize(400, 250);

        var gc = new GridBagConstraints();
        gc.insets = new Insets(20, 5, 20, 5);
        gc.gridx = 0;
        connectFrame.add(new JLabel(TextUtils.getText("label.mqttAddress")), gc);
        gc.gridx = 1;
        var address = new JTextField("localhost:1883", 15);
        connectFrame.add(address, gc);

        var connectButton = new JButton(TextUtils.getText("button.connect"));

        gc.gridx = 2;
        connectFrame.add(connectButton, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        connectFrame.add(new JLabel(TextUtils.getText("label.auth")), gc);

        String[] authChoices = new String[] {"Anonymous", "Username/Password", "Server Certificate"};
        var authComboBox = new JComboBox<>(authChoices);

        gc.gridx = 1;
        connectFrame.add(authComboBox, gc);


        gc.gridx = 0;
        gc.gridy = 2;
        var userNameText = new JLabel(TextUtils.getText("label.username"));
        userNameText.setVisible(false);
        connectFrame.add(userNameText, gc);

        gc.gridx = 1;
        var userNameInput = new JTextField("", 15);
        userNameInput.setVisible(false);
        connectFrame.add(userNameInput, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        var passwordText = new JLabel(TextUtils.getText("label.password"));
        passwordText.setVisible(false);
        connectFrame.add(passwordText, gc);

        gc.gridx = 1;
        var passwordInput = new JTextField("", 15);
        passwordInput.setVisible(false);
        connectFrame.add(passwordInput, gc);



        authComboBox.addItemListener((ItemEvent itemEvent)->{
            int index = authComboBox.getSelectedIndex();
            String item = authChoices[index];

            if (item.equals("Username/Password")) {
                userNameText.setVisible(true);
                userNameInput.setVisible(true);
                passwordText.setVisible(true);
                passwordInput.setVisible(true);
            } else {
                userNameText.setVisible(false);
                userNameInput.setVisible(false);
                passwordText.setVisible(false);
                passwordInput.setVisible(false);
            }

            if (item.equals("Server Certificate")) {
                // TODO get path from user submitted file
                datahandler.connector().auth(new MqttServerCertificate("/home/user/Downloads/mosquitto.org.crt"));
            }
        });


        connectButton.addActionListener(action -> {
            // first disable connect button
            connectButton.setEnabled(false);

            // check authentication
            if (authChoices[authComboBox.getSelectedIndex()].equals("Username/Password")) {
                System.out.println("Using password/username");
                datahandler.connector().auth(new MqttUsernamePassword(userNameInput.getText(), passwordInput.getText()));
            }

            boolean connected = datahandler.connector().connect(address.getText());
            if (connected) {
                onConnect.run();
            } else {
                System.out.println("Could not connect");
            }
            // now enable again
            connectButton.setEnabled(true);
        });
        
        return connectFrame;
    }
}
