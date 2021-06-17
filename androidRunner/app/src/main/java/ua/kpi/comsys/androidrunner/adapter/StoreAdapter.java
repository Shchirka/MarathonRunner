package ua.kpi.comsys.androidrunner.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import ua.kpi.comsys.androidrunner.FriendsPostsActivity;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.StoreActivity;
import ua.kpi.comsys.androidrunner.list.Marathon;
import ua.kpi.comsys.androidrunner.models.User;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Marathon> marathons;

    public StoreAdapter(Context context, List<Marathon> marathons){
        this.marathons = marathons;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public StoreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.marathons_instore_list_item, parent, false);
        return new StoreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoreAdapter.ViewHolder holder, int position) {
        Marathon marathon = marathons.get(position);
        holder.marathonTitleView.setText(marathon.getTitle());
        holder.marathonPriceView.setText(Long.toString(marathon.getPrice()));
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("points")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long points = (long)snapshot.getValue();
                        if(Long.parseLong(holder.marathonPriceView.getText().toString()) <= points){
                            holder.buyMarathon.setEnabled(true);
                            holder.buyMarathon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    buy(holder.marathonTitleView.getText().toString());
                                }
                            });
                        }
                        else{
                            holder.buyMarathon.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return marathons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView marathonTitleView, marathonPriceView;
        final AppCompatButton buyMarathon;
        ViewHolder(View view){
            super(view);
            marathonTitleView = (TextView) view.findViewById(R.id.marathon_instore_title);
            marathonPriceView = (TextView) view.findViewById(R.id.marathon_instore_price);
            buyMarathon = (AppCompatButton) view.findViewById(R.id.buy_marathon);
        }
    }

    public void buy(String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(inflater.getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("Buying marathon");
        builder.setMessage("You really wanna buy " + title + " marathon?");

        builder.setPositiveButton("Yeah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User userProfile = snapshot.getValue(User.class);
                                FirebaseDatabase.getInstance().getReference("Store")
                                        .child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        long price = (long) snapshot.getValue();
                                        FirebaseDatabase.getInstance().getReference("UsersMarathons")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("available").child(title).setValue("true");
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("points").setValue(userProfile.points - price);
                                        refresh();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });
        builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void refresh(){
        Intent intent = new Intent(inflater.getContext(), StoreActivity.class);
        inflater.getContext().startActivity(intent);
    }
}
