package ua.kpi.comsys.androidrunner.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.kpi.comsys.androidrunner.HomeActivity;
import ua.kpi.comsys.androidrunner.InfoMarathonActivity;
import ua.kpi.comsys.androidrunner.MarathonsActivity;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.list.Marathon;

public class MarathonsAvailableAdapter extends RecyclerView.Adapter<MarathonsAvailableAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Marathon> marathons;

    public MarathonsAvailableAdapter(Context context, List<Marathon> marathons){
        this.marathons = marathons;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public MarathonsAvailableAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.marathons_avail_list_item, parent, false);
        return new MarathonsAvailableAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MarathonsAvailableAdapter.ViewHolder holder, int position) {
        Marathon marathon = marathons.get(position);
        holder.marathonTitleView.setText(marathon.getTitle());
        holder.marathonRunView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflater.getContext().startActivity(new Intent(inflater.getContext(), InfoMarathonActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return marathons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView marathonTitleView;
        final ImageView marathonRunView;
        ViewHolder(View view){
            super(view);
            marathonTitleView = (TextView) view.findViewById(R.id.marathon_avail_title);
            marathonRunView = (ImageView) view.findViewById(R.id.start_single_marathon);
        }
    }
}
