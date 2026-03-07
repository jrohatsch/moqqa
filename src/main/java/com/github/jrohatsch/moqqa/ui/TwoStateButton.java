package com.github.jrohatsch.moqqa.ui;


import javax.swing.*;

public class TwoStateButton extends JButton {
    private final String[] states;
    private int selectedState;
    private final Runnable[] callbacks;


    public TwoStateButton(String stateA, String stateB) {
        super(stateA);
        states = new String[2];
        states[0] = stateA;
        states[1] = stateB;
        callbacks = new Runnable[2];
        selectedState = 0;
        addActionListener(a -> {
            callbacks[selectedState].run();
            changeState();
        });
    }

    public void addCallback(int id, Runnable callback) {
        assert(id == 0 || id == 1);
        callbacks[id] = callback;
    }

    public void changeState() {
        if (selectedState == 0) {
            selectedState = 1;
        } else if (selectedState == 1) {
            selectedState = 0;
        }
        setText(states[selectedState]);
    }


}
