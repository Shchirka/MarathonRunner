package ua.kpi.comsys.androidrunner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = (ImageView) findViewById(R.id.my_image_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int imageId = extras.getInt("image_id");
            imageView.setImageResource(imageId);
        }
    }
}