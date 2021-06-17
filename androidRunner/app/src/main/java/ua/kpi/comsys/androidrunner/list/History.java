package ua.kpi.comsys.androidrunner.list;


import android.graphics.Bitmap;

public class History {

    private String date;
    private Bitmap mapBitmap;
    private double distance;
    private String time;

    public History(String date, Bitmap mapBitmap, double distance, String time) {
        this.date = date;
        this.mapBitmap = mapBitmap;
        this.distance = distance;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Bitmap getMapBitmap() {
        return mapBitmap;
    }

    public void setMapBitmap(Bitmap mapBitmap) {
        this.mapBitmap = mapBitmap;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
