package org.mcrendering.basic;

import org.mcrendering.common.GameEngine;

public class Main {
    
    public static void main(String[] args) {
        try {
            new GameEngine("GAME", new BasicRenderer()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
