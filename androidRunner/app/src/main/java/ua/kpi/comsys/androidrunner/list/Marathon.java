package ua.kpi.comsys.androidrunner.list;

public class Marathon {

    private String title;
    private long price;

    public Marathon(String title) {
        this.title = title;
    }

    public Marathon(String title, long price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
