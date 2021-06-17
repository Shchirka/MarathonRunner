package ua.kpi.comsys.androidrunner.list;

import android.graphics.Bitmap;

public class Post {

    private String nickname;
    private Bitmap profileImage;
    private Bitmap map;
    private String place;
    private int likes;
    private String time;
    private double kilometres;

    public Post(String nickname, Bitmap profileImage, Bitmap map, String place, int likes, String time, double kilometres) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.map = map;
        this.place = place;
        this.likes = likes;
        this.time = time;
        this.kilometres = kilometres;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }

    public Bitmap getMap() {
        return map;
    }

    public void setMap(Bitmap map) {
        this.map = map;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getKilometres() {
        return kilometres;
    }

    public void setKilometres(double kilometres) {
        this.kilometres = kilometres;
    }
}
