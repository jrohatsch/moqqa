package com.github.jrohatsch.moqqa.ui;

import com.github.jrohatsch.moqqa.utils.ColorUtils;

import javax.swing.*;
import java.awt.*;

public class TwoStateButton extends JButton {
    private final String[] states;
    private int selectedState;
    private final Runnable[] callbacks;
    private final Color[] colors;


    public TwoStateButton(String stateA, String stateB) {
        super(stateA);
        states = new String[2];
        states[0] = stateA;
        states[1] = stateB;
        callbacks = new Runnable[2];
        colors = new Color[2];
        colors[0] = ColorUtils.BLUE;
        colors[1] = Color.DARK_GRAY;
        selectedState = 0;
        setBackground(colors[0]);
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
        setBackground(colors[selectedState]);
    }


}
