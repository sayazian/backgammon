package com.coderscampus.backgammon_vanilla.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardStatus {
    private final List<Point> points;
    private final Map<String, Integer> hits;
    private final Map<String, Integer> outs;
    private final int[] dice;
    private final String turn;
    public BoardStatus(List<Point> points, Map<String, Integer> hits, Map<String, Integer> outs, String turn) {
        this.points = points;
        this.hits = hits;
        this.outs = outs;
        this.turn = turn;
        this.dice = new int[2];
    }

    public List<Point> getPoints() {
        return points;
    }

    public Map<String, Integer> getHits() {
        return hits;
    }

    public Map<String, Integer> getOuts() {
        return outs;
    }

    public int[] getDice() {
        return dice;
    }

    public String getTurn() {
        return turn;
    }

    public BoardStatus() {
        this.points = initializePoints();
        this.hits = new HashMap<>(2);
        this.hits.put("black", 0);
        this.hits.put("white", 0);

        this.outs = new HashMap<>(2);
        this.outs.put("black", 0);
        this.outs.put("white", 0);

        this.dice = new int[2];
        this.turn = "black";
    }

    private List<Point> initializePoints() {
        List<Point> points = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            points.add(new Point(i+1, null, 0));
        }
        points.set(24-1,new Point(24, "white", 2));
        points.set(13-1,new Point(13, "white", 5));
        points.set(8-1,new Point(8, "white", 3));
        points.set(6-1,new Point(6, "white", 5));

        points.set(1-1,new Point(1, "black", 2));
        points.set(12-1,new Point(12, "black", 5));
        points.set(17-1,new Point(17, "black", 3));
        points.set(19-1,new Point(19, "black", 5));

        return points;
    }
}
