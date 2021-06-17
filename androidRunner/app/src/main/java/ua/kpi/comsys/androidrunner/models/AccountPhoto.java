package ua.kpi.comsys.androidrunner.models;

public class AccountPhoto {
    private String imageUrl;

    public AccountPhoto(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public AccountPhoto() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
