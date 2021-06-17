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
import ua.kpi.comsys.androidrunner.list.History;
import ua.kpi.comsys.androidrunner.list.Post;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<History> posts;

    public HistoryAdapter(Context context, List<History> posts){
        this.posts = posts;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.history_list_item, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, int position) {
        History post = posts.get(position);
        holder.dateView.setText(post.getDate());
        holder.mapView.setImageBitmap(post.getMapBitmap());
        holder.timeView.setText(post.getTime());
        holder.distanceView.setText(Double.toString(post.getDistance()));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView mapView;
        final TextView timeView, distanceView, dateView;
        ViewHolder(View view){
            super(view);
            dateView = (TextView) view.findViewById(R.id.history_date);
            mapView = (ImageView)view.findViewById(R.id.history_map);
            timeView = (TextView) view.findViewById(R.id.history_time);
            distanceView = (TextView) view.findViewById(R.id.history_distance);
        }
    }
}
