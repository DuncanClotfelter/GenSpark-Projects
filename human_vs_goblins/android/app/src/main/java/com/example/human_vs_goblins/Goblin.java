package com.example.human_vs_goblins;

public class Goblin extends Entity {
    Goblin(int x, int y, Point dir) {
        this.pos = new Point(x, y);
        this.setDirVector(dir);
    }

    @Override
    int getStr() {return 2;}

    @Override
    char getChar() {return 'G';}
}
