package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.MqttServerCertificate;
import com.github.jrohatsch.moqqa.data.MqttUsernamePassword;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionPanel {
    private final Datahandler datahandler;
    private JPanel panel;
    private final Runnable onConnect;

    private boolean isConnected;

    public ConnectionPanel(Datahandler datahandler, Runnable onConnect) {
        this.datahandler = datahandler;
        this.onConnect = onConnect;
    }


    public JPanel get() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        var gc = new GridBagConstraints();
        gc.insets = new Insets(20, 5, 20, 5);
        gc.gridx = 0;
        panel.add(new JLabel(TextUtils.getText("label.mqttAddress")), gc);
        gc.gridx = 1;
        var address = new JTextField("localhost:1883", 15);
        panel.add(address, gc);

        var connectButton = new JButton(TextUtils.getText("button.connect"));

        gc.gridx = 2;
        panel.add(connectButton, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(new JLabel(TextUtils.getText("label.auth")), gc);

        String[] authChoices = new String[]{"Anonymous", "Username/Password", "Server Certificate"};
        var authComboBox = new JComboBox<>(authChoices);

        gc.gridx = 1;
        panel.add(authComboBox, gc);


        gc.gridx = 0;
        gc.gridy = 2;
        var userNameText = new JLabel(TextUtils.getText("label.username"));
        userNameText.setVisible(false);
        panel.add(userNameText, gc);

        gc.gridx = 1;
        var userNameInput = new JTextField("", 15);
        userNameInput.setVisible(false);
        panel.add(userNameInput, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        var passwordText = new JLabel(TextUtils.getText("label.password"));
        passwordText.setVisible(false);
        panel.add(passwordText, gc);

        gc.gridx = 1;
        var passwordInput = new JTextField("", 15);
        passwordInput.setVisible(false);
        panel.add(passwordInput, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        var serverCertificateText = new JLabel(TextUtils.getText("label.serverCertificate"));
        serverCertificateText.setVisible(false);
        panel.add(serverCertificateText, gc);

        gc.gridx = 1;
        var serverCertificateButton = new JButton("Choose File");
        AtomicReference<String> serverCertificatePath = new AtomicReference<>("");
        serverCertificateButton.addActionListener((a) -> {
            try {
                var chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Certificate Files (.crt, .der)", "crt", "der"));
                chooser.showOpenDialog(panel);
                serverCertificatePath.set(chooser.getSelectedFile().getAbsolutePath());
                serverCertificateButton.setText(chooser.getSelectedFile().getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        serverCertificateButton.setVisible(false);
        panel.add(serverCertificateButton, gc);


        authComboBox.addItemListener((ItemEvent itemEvent) -> {
            int index = authComboBox.getSelectedIndex();
            String item = authChoices[index];

            boolean useUsernamePassword = item.equals("Username/Password");
            userNameText.setVisible(useUsernamePassword);
            userNameInput.setVisible(useUsernamePassword);
            passwordText.setVisible(useUsernamePassword);
            passwordInput.setVisible(useUsernamePassword);

            boolean useServerCertificate = item.equals("Server Certificate");
            serverCertificateText.setVisible(useServerCertificate);
            serverCertificateButton.setVisible(useServerCertificate);
        });


        connectButton.addActionListener(action -> {
            // first disable connect button
            connectButton.setEnabled(false);

            // check authentication
            if (authChoices[authComboBox.getSelectedIndex()].equals("Username/Password")) {
                System.out.println("Using password/username");
                datahandler.connector().auth(new MqttUsernamePassword(userNameInput.getText(), passwordInput.getText()));
            }

            if (authChoices[authComboBox.getSelectedIndex()].equals("Server Certificate")) {
                datahandler.connector().auth(new MqttServerCertificate(serverCertificatePath.get()));
            }

            boolean connected = datahandler.connector().connect(address.getText());
            if (connected) {
                isConnected = true;
            } else {
                System.out.println("Could not connect");
            }
            // now enable again
            connectButton.setEnabled(true);
        });

        return panel;
    }

    public boolean connected() {
        return isConnected;
    }

    public void setVisible(boolean visible) {
        panel.setVisible(visible);
    }
}
