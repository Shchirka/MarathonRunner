package ua.kpi.comsys.androidrunner.list;

import android.graphics.Bitmap;

public class Friend {
    private String name;
    private String nickname;
    private Bitmap profileImage;

    public Friend(String name, String nickname, Bitmap profileImage) {
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return this.nickname;
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
}
