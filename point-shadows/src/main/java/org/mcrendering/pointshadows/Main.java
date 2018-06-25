package org.mcrendering.pointshadows;

import org.mcrendering.common.GameEngine;

public class Main {
    
    public static void main(String[] args) {
        try {
            new GameEngine("GAME", new PointShadowsRenderer()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
