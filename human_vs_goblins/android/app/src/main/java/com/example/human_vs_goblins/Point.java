package com.example.human_vs_goblins;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class Point {
    int x, y;

    Point add(Point p2) {
        return new Point(x + p2.getX(), y + p2.getY());
    }

    Point inverseY() { return new Point(x, -y); }

    @Override
    public String toString() {
        return "x: "+x+", y: "+y;
    }
}
