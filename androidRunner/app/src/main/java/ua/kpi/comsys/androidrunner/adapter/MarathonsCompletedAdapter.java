package ua.kpi.comsys.androidrunner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.list.Marathon;

public class MarathonsCompletedAdapter extends RecyclerView.Adapter<MarathonsCompletedAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Marathon> marathons;

    public MarathonsCompletedAdapter(Context context, List<Marathon> marathons){
        this.marathons = marathons;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public MarathonsCompletedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.marathons_compl_list_item, parent, false);
        return new MarathonsCompletedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MarathonsCompletedAdapter.ViewHolder holder, int position) {
        Marathon marathon = marathons.get(position);
        holder.marathonTitleView.setText(marathon.getTitle());
    }

    @Override
    public int getItemCount() {
        return marathons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView marathonTitleView;
        ViewHolder(View view){
            super(view);
            marathonTitleView = (TextView) view.findViewById(R.id.marathon_compl_title);
        }
    }
}
