package com.example.human_vs_goblins;

import lombok.Value;

@Value
public class Tile implements Comparable<Tile> {
    Point pos;

    @Override
    public int compareTo(Tile t) {
        if(t.pos.getX() == pos.getX()) {
            return t.pos.getY() - pos.getY();
        } else {
            return t.pos.getX() - pos.getX();
        }
    }

    @Override
    public String toString() {
        return pos.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Tile && ((Tile) o).getPos().equals(pos);
    }

    @Override
    public int hashCode() { return pos.getX() * 1000 + pos.getY(); }
}
