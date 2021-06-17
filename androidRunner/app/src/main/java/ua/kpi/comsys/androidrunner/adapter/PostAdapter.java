package ua.kpi.comsys.androidrunner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.list.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Post> posts;

    public PostAdapter(Context context, List<Post> posts){
        this.posts = posts;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.posts_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.profileImageView.setImageBitmap(post.getProfileImage());
        holder.mapView.setImageBitmap(post.getMap());
        holder.nicknameView.setText(post.getNickname());
        holder.placeView.setText(post.getPlace());
        holder.likesView.setText(Integer.toString(post.getLikes()));
        holder.timeView.setText(post.getTime());
        holder.distanceView.setText(Double.toString(post.getKilometres()));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView profileImageView, mapView;
        final TextView placeView, nicknameView, likesView, timeView, distanceView;
        ViewHolder(View view){
            super(view);
            profileImageView = (ImageView)view.findViewById(R.id.post_profileImage);
            mapView = (ImageView)view.findViewById(R.id.post_map);
            placeView = (TextView) view.findViewById(R.id.post_place);
            likesView = (TextView) view.findViewById(R.id.post_likes);
            nicknameView = (TextView) view.findViewById(R.id.post_nickname);
            timeView = (TextView) view.findViewById(R.id.post_time);
            distanceView = (TextView) view.findViewById(R.id.post_distance);
        }
    }
}
