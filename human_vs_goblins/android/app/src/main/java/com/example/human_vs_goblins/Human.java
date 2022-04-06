package com.example.human_vs_goblins;

import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor
public class Human extends Entity {
    @Getter private final LinkedBlockingQueue<Point> moveInput = new LinkedBlockingQueue<>();
    private static final Scanner s = new Scanner(System.in);
    boolean localPlayer;

    @SneakyThrows
    @Override
    Point getDest(boolean onLand) {
        if(!onLand) {return super.getDest(onLand);}

        if(localPlayer) {
            System.out.println("Enter a direction to move (n/s/e/w):");
            Point newDir = getVectorFromStr(s.nextLine());
            if(newDir == null) {
                return getDest(onLand);
            } else {
                setDirVector(newDir);
            }
        } else {
            setDirVector(moveInput.take());
        }

        return super.getDest(onLand);
    }

    static Point getVectorFromStr(String dir) {
        switch(dir) {
            case "ne": return new Point(1, 1);
            case "nw": return new Point(-1, 1);
            case "n": return new Point(0, 1);
            case "e": return new Point(1, 0);
            case "w": return new Point(-1, 0);
            case "sw": return new Point(-1, -1);
            case "s": return new Point(0, -1);
            case "se": return new Point(1, -1);
            default: return null;
        }
    }

    int getStr() {return 9001;}
    char getChar() {return 'H';}
}
