package com.coderscampus.backgammon_vanilla.domain;

public class Point {
    private int position;
    private int number;
    private String color;


    public Point(int position, String color, int number) {
        this.position = position;
        this.color = color;
        this.number = number;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
