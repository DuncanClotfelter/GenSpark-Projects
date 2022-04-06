package com.example.human_vs_goblins;

public class GameLauncher implements Runnable {
    boolean localMode;

    private GameLauncher(boolean localMode) {this.localMode = localMode;}

    public static void main(String[] args) {
        startGame(true);
    }

    public void run() {
        Grid map = new Grid(localMode);

        if(!localMode) {
            MainActivity.game = map;
        }

        while(!map.gameOver()) {
            map.nextTurn();
        }

        System.out.println("Game over!");
    }

    static void startGame(boolean localMode) {
        new Thread(new GameLauncher(localMode)).start();
    }
}