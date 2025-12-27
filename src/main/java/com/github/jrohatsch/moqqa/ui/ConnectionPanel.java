package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.MqttServerCertificate;
import com.github.jrohatsch.moqqa.data.MqttUsernamePassword;
import com.github.jrohatsch.moqqa.session.*;
import com.github.jrohatsch.moqqa.session.impl.JsonSessionHandler;
import com.github.jrohatsch.moqqa.utils.TextUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionPanel {
    private final Datahandler datahandler;
    private JTabbedPane tabbedPane;
    private JPanel panel;
    private final SessionHandler sessionHandler;
    private int addSessionCounter = 1;

    private boolean isConnected;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ConnectionPanel(Datahandler datahandler) {
        this.datahandler = datahandler;
        this.sessionHandler = new JsonSessionHandler();
    }

    private static void displayAuthInputs(String[] authChoices, JComboBox<String> authComboBox, JLabel userNameText, JTextField userNameInput, JLabel passwordText, JTextField passwordInput, JLabel serverCertificateText, JButton serverCertificateButton) {
        int index = authComboBox.getSelectedIndex();
        String item = authChoices[index];

        boolean useUsernamePassword = item.equals(AuthenticationType.USERNAME_PASSWORD.text);
        userNameText.setVisible(useUsernamePassword);
        userNameInput.setVisible(useUsernamePassword);
        passwordText.setVisible(useUsernamePassword);
        passwordInput.setVisible(useUsernamePassword);

        boolean useServerCertificate = item.equals(AuthenticationType.SERVER_CERT.text);
        serverCertificateText.setVisible(useServerCertificate);
        serverCertificateButton.setVisible(useServerCertificate);
    }

    public JPanel getSessionPanel(String sessionName) {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        var gc = new GridBagConstraints();
        gc.insets = new Insets(20, 5, 20, 5);

        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Session Name:"), gc);

        var sessionNameTextField = new JTextField(sessionName, 15);

        gc.gridx = 1;
        gc.gridy = 0;
        panel.add(sessionNameTextField, gc);

        // load session
        var session = sessionHandler.get(sessionName).orElse(Session.anonymous("New Session", "localhost:1883"));


        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(new JLabel(TextUtils.getText("label.mqttAddress")), gc);

        var address = new JTextField(session.address(), 15);

        gc.gridx = 1;
        gc.gridy = 1;
        panel.add(address, gc);

        var connectButton = new TwoStateButton(TextUtils.getText("button.connect"), TextUtils.getText("button.cancel"));

        gc.gridx = 2;
        gc.gridy = 1;
        panel.add(connectButton, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        panel.add(new JLabel(TextUtils.getText("label.auth")), gc);

        String[] authChoices = new String[]{AuthenticationType.ANONYMOUS.text, AuthenticationType.USERNAME_PASSWORD.text, AuthenticationType.SERVER_CERT.text};
        var authComboBox = new JComboBox<>(authChoices);

        gc.gridx = 1;
        gc.gridy = 2;
        panel.add(authComboBox, gc);


        gc.gridx = 0;
        gc.gridy = 3;
        var userNameText = new JLabel(TextUtils.getText("label.username"));
        userNameText.setVisible(false);
        panel.add(userNameText, gc);

        gc.gridx = 1;
        gc.gridy = 3;
        var userNameInput = new JTextField("", 15);
        userNameInput.setVisible(false);
        panel.add(userNameInput, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        var passwordText = new JLabel(TextUtils.getText("label.password"));
        passwordText.setVisible(false);
        panel.add(passwordText, gc);

        gc.gridx = 1;
        gc.gridy = 4;
        var passwordInput = new JTextField("", 15);
        passwordInput.setVisible(false);
        panel.add(passwordInput, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        var serverCertificateText = new JLabel(TextUtils.getText("label.serverCertificate"));
        serverCertificateText.setVisible(false);
        panel.add(serverCertificateText, gc);

        gc.gridx = 1;
        gc.gridy = 3;
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


        // use loaded session auth values
        authComboBox.setSelectedItem(session.authentication().type.text);
        switch (session.authentication().type) {
            case ANONYMOUS -> {
                // nothing to do
            }
            case USERNAME_PASSWORD -> {
                UsernamePassword auth = (UsernamePassword) session.authentication();
                userNameInput.setText(auth.username);
                passwordInput.setText(auth.password);
            }
            case SERVER_CERT -> {
                ServerCertificate auth = (ServerCertificate) session.authentication();
                File cert = new File(auth.certPath);
                if (cert.exists()) {
                    serverCertificatePath.set(auth.certPath);
                    serverCertificateButton.setText(cert.getName());
                }

            }
        }
        displayAuthInputs(authChoices, authComboBox, userNameText, userNameInput, passwordText, passwordInput, serverCertificateText, serverCertificateButton);


        authComboBox.addItemListener((ItemEvent itemEvent) -> displayAuthInputs(authChoices, authComboBox, userNameText, userNameInput, passwordText, passwordInput, serverCertificateText, serverCertificateButton));

        connectButton.addCallback(0, () -> {

            if (userNameInput.isVisible() && passwordInput.isVisible()) {
                sessionHandler.save(Session.usernamePassword(sessionNameTextField.getText(), address.getText(), userNameInput.getText(), passwordInput.getText()));
            } else if (serverCertificateText.isVisible()) {
                sessionHandler.save(Session.certPath(sessionNameTextField.getText(), address.getText(), serverCertificatePath.get()));
            } else {
                // save session details
                sessionHandler.save(Session.anonymous(sessionNameTextField.getText(), address.getText()));
            }

            // check authentication
            if (authChoices[authComboBox.getSelectedIndex()].equals(AuthenticationType.USERNAME_PASSWORD.text)) {
                System.out.println("Using password/username");
                datahandler.connector().auth(new MqttUsernamePassword(userNameInput.getText(), passwordInput.getText()));
            }

            if (authChoices[authComboBox.getSelectedIndex()].equals(AuthenticationType.SERVER_CERT.text)) {
                datahandler.connector().auth(new MqttServerCertificate(serverCertificatePath.get()));
            }

            executorService.submit(() -> {
                boolean connected = datahandler.connector().connect(address.getText());
                if (connected) {
                    isConnected = true;
                } else {
                    System.out.println("Could not connect");
                }
            });
        });

        connectButton.addCallback(1, () -> {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                System.err.println("could not stop connection thread in time");
            }
            executorService = Executors.newSingleThreadExecutor();
        });

        return panel;
    }

    private void updateTabs() {
        // clean up
        tabbedPane.removeAll();

        var savedSessions = sessionHandler.load();

        if (savedSessions.isEmpty()) {
            tabbedPane.add("New Session", getSessionPanel("New Session"));
        }

        System.out.printf("Before loop size: %d%n", savedSessions.size());
        savedSessions.forEach(session -> {
                    System.out.printf("Add Tab %s%n", session.name());
                    tabbedPane.add(session.name(), getSessionPanel(session.name()));
                }
        );

        tabbedPane.add(" + ", new JPanel());
    }

    public boolean connected() {
        return isConnected;
    }

    public void setVisible(boolean visible) {
        tabbedPane.setVisible(visible);
    }

    public Component get() {
        tabbedPane = new JTabbedPane();

        updateTabs();

        tabbedPane.addChangeListener(e -> {
            var pane = (JTabbedPane) e.getSource();
            if (pane.getSelectedIndex() == pane.indexOfTab(" + ")) {
                tabbedPane.removeTabAt(pane.getSelectedIndex());
                int index = tabbedPane.getTabCount();
                String sessionName = "New Session (%d)".formatted(addSessionCounter++);
                tabbedPane.add(sessionName, getSessionPanel(sessionName));
                tabbedPane.setSelectedIndex(index);
                tabbedPane.add(" + ", new JPanel());
            }
        });

        tabbedPane.addMouseListener(new SessionTabMouseListener(sessionHandler));

        return tabbedPane;
    }
}
