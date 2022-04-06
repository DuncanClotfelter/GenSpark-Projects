package com.example.human_vs_goblins;

import lombok.Getter;
import lombok.Setter;

public abstract class Entity {
    private static int idCounter = 0;
    @Getter private final int ID;

    @Getter Point pos = new Point(0, 0);
    @Getter private int HP = 10;
    @Getter @Setter private Point dirVector = new Point(0, 0);
    @Getter private Action action = Action.IDLING;

    public Entity() {
        ID = idCounter++;
    }

    /**
     * Checks whether this Entity died last turn
     * @return true if dead
     */
    boolean isDead() {
        return action == Action.DYING ||
                (action == Action.FALLING && pos.getY() < 0);
    }

    void move() {
        System.out.println("Moving (Entity): "+getDirVector());
        if(dirVector.equals(new Point(0, 0))) {
            action = Action.IDLING;
        } else if(dirVector.getY() > 0) {
            action = Action.JUMPING;
        } else if(dirVector.getY() < 0) {
            action = action.FALLING;
        } else {
            action = action.WALKING;
        }
        pos = pos.add(dirVector);
    }

    void fall() {
        if(getDirVector().getY() > 0) {
            dirVector = dirVector.inverseY();
        } else {
            dirVector = new Point(dirVector.getX(), -1);
        }
        action = Action.FALLING;
    }

    void die(boolean fallDeath) {
        action = Action.DYING;
        System.out.println("Oh dear, I have died!");
    }

    void attack(Entity other) {
        action = Action.ATTACKING;
        other.HP -= Math.random() * getStr();
        if(other.HP <= 0) {
            other.die(false);
        }
    }

    Point getDest(boolean onLand) {
        return pos.add(dirVector);
    }

    abstract int getStr();

    /**
     *
     * @return The UTF char that represents this Entity
     */
    abstract char getChar();

    /**
     * Classes in this project must override toString
     */
    public String toString() {
        return String.valueOf(getChar());
    }

    /**
     * Google says .ordinal() is not recommended because the ordinal can change if you insert into the middle of the enum
     * But, the entire point of the enum is code legibility for arbitrary values?
     * So... don't put things into the middle of the list. Let this enum match the Dart enum. Smh my head
     */
    enum Action {
        ATTACKING, WALKING, IDLING, DYING, FALLING, JUMPING
    }
}
