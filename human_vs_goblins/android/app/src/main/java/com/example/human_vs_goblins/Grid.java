package com.example.human_vs_goblins;

import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import lombok.Getter;

public class Grid {
    private final static int MAX_HEIGHT = 3;
    private final static int MAX_WIDTH = 50;

    private static final int winX = 15;
    private boolean hasWon = false;

    @Getter private final Human player;
    private final char[][] stringRep = new char[MAX_HEIGHT][MAX_WIDTH];
    private final TreeSet<Tile> tileMap = new TreeSet<>();
    private final Set<Entity> entitySet = new HashSet<>();

    boolean gameOver() {
        if(hasWon) {
            notifyGameOver(true);
            return true;
        } else if(player.isDead()) {
            notifyGameOver(false);
            return true;
        }
        return false;
    }

    int[] entToIntArr() {
        int[] toReturn = new int[entitySet.size() * 5];
        System.out.println("Sending Player move : " + player.getAction());
        toReturn[0] = player.getID();
        toReturn[1] = player.getChar();
        toReturn[2] = player.getAction().ordinal();
        toReturn[3] = player.getPos().getX();
        toReturn[4] = player.getPos().getY();
        int i = 5;

        for(Entity e : entitySet) {
            if(e.equals(player)) { continue; }

            toReturn[i++] = e.getID();
            toReturn[i++] = e.getChar();
            toReturn[i++] = e.getAction().ordinal();
            toReturn[i++] = e.getPos().getX();
            toReturn[i++] = e.getPos().getY();
        }

        return toReturn;
    }

    int[] mapToIntArr() {
        int[] toReturn = new int[tileMap.size() * 2];
        int idx = 0;
        for(Tile t : tileMap) {
            toReturn[idx++] = t.getPos().getX();
            toReturn[idx++] = t.getPos().getY();
        }
        return toReturn;
    }

    void printConsoleDisplay() {
        for(char[] row : stringRep)
            Arrays.fill(row, ' ');

        for(Tile t : tileMap)
            stringRep[t.getPos().getY()][t.getPos().getX()] = '_';

        for(Entity e : entitySet) {
            if(e.isDead()) continue;
            stringRep[e.getPos().getY()][e.getPos().getX()] = e.getChar();
        }

        for(int i = stringRep.length-1; i >= 0; i--)
            System.out.println(new String(stringRep[i]));
    }

    //TODO read from outside source
    Grid(boolean localMode) {
        tileMap.add(new Tile(new Point(0, 0)));
        tileMap.add(new Tile(new Point(1, 0)));
        tileMap.add(new Tile(new Point(2, 0)));
        tileMap.add(new Tile(new Point(2, 1)));
        tileMap.add(new Tile(new Point(3, 0)));
        tileMap.add(new Tile(new Point(3, 1)));
        tileMap.add(new Tile(new Point(4, 0)));
        tileMap.add(new Tile(new Point(4, 1)));
        tileMap.add(new Tile(new Point(5, 0)));
        tileMap.add(new Tile(new Point(5, 1)));
        tileMap.add(new Tile(new Point(7, 0)));
        tileMap.add(new Tile(new Point(8, 0)));
        tileMap.add(new Tile(new Point(9, 0)));
        tileMap.add(new Tile(new Point(10, 0)));
        tileMap.add(new Tile(new Point(12, 0)));
        tileMap.add(new Tile(new Point(14, 0)));
        tileMap.add(new Tile(new Point(15, 0)));

        player = new Human(localMode);
        entitySet.add(player);

        Goblin g = new Goblin(5, 0, new Point(0, 0));
        entitySet.add(g);
    }

    void nextTurn() {
        for(Entity e : entitySet) {
            if(!e.equals(player)) continue;//TODO remove testing
            if(e.isDead()) continue;

            Point destPoint;
            boolean isFloating = !tileMap.contains(new Tile(e.getPos()));
            if(isFloating) {
                e.fall();
            }
            destPoint = e.getDest(!isFloating);

            if(destPoint.getY() <= 0 && !tileMap.contains(new Tile(destPoint))) {
                System.out.println("Falling!");
                e.die(true);
            } else {
                boolean attacking = false;
                //Collision check
                for(Entity e2 : entitySet) {
                    if(!e.equals(e2) && !e2.isDead() && e2.getPos().equals(destPoint)) {
                        System.out.println("Attacking!: "+destPoint);
                        attacking = true;
                        e.attack(e2);
                        break;
                    }
                }
                if(!attacking) {
                    System.out.println("Moving!");
                    e.move();
                    if(e.equals(player) && player.getPos().getX() == winX) {
                        hasWon = true;
                        System.out.println("Congratulation, you are wiener!");
                    }
                }
            }
        }

        if(!getPlayer().localPlayer) {
            updateEntities();
        } else {
            printConsoleDisplay();
        }
    }

    void notifyGameOver(boolean winner) {
        if(getPlayer().localPlayer) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                MainActivity.getPlatform().invokeMethod(winner ? "notifyWin" : "notifyLoss", null);
            }
        });
    }

    void updateEntities() {
        if(getPlayer().localPlayer) return;
        int[] move = entToIntArr();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                MainActivity.getPlatform().invokeMethod("updateEntities", move);
            }
        });
    }

    @Override
    public String toString() {
        return Arrays.toString(mapToIntArr());
    }
}
