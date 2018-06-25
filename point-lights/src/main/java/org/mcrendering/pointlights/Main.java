package org.mcrendering.pointlights;

import org.mcrendering.common.GameEngine;

public class Main {
    
    public static void main(String[] args) {
        try {
            new GameEngine("GAME", new PointLightsRenderer()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
