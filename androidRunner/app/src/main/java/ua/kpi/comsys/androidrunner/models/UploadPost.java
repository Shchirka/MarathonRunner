package ua.kpi.comsys.androidrunner.models;

public class UploadPost {

    public String imageName;
    public String imageURL;
    public double mTime;
    public double mDistance;
    public UploadPost(){}

    public UploadPost(String imageName, String imageURL, double mTime, double mDistance) {
        this.imageName = imageName;
        this.imageURL = imageURL;
        this.mTime = mTime;
        this.mDistance = mDistance;
    }

    public double getmTime() {
        return mTime;
    }

    public double getmDistance() {
        return mDistance;
    }

    public String getImageName() {
        return imageName;
    }
    public String getImageURL() {
        return imageURL;
    }
}
