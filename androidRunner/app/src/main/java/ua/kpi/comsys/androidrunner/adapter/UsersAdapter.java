package ua.kpi.comsys.androidrunner.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.androidrunner.FriendsPostsActivity;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.UsersActivity;
import ua.kpi.comsys.androidrunner.list.Friend;
import ua.kpi.comsys.androidrunner.models.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> implements Filterable {

    private final LayoutInflater inflater;
    private final List<Friend> friends;
    private List<Friend> friendsSearch;

    public UsersAdapter(Context context, List<Friend> friends){
        this.friends = friends;
        this.inflater = LayoutInflater.from(context);
        friendsSearch = new ArrayList<>(friends);
    }

    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.possible_friends_list_item, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UsersAdapter.ViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.profileImageView.setImageBitmap(friend.getProfileImage());
        holder.nameView.setText(friend.getName());
        holder.nicknameView.setText(friend.getNickname());
        holder.addFriendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(holder.nicknameView.getText().toString());
            }
        });
        holder.feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFeed(holder.nicknameView.getText().toString(), inflater.getContext());
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView profileImageView, addFriendView;
        final TextView nameView, nicknameView;
        final LinearLayout feed;
        ViewHolder(View view){
            super(view);
            profileImageView = (ImageView)view.findViewById(R.id.profileImage);
            addFriendView = (ImageView) view.findViewById(R.id.add_user_to_friends);
            nameView = (TextView) view.findViewById(R.id.name);
            nicknameView = (TextView) view.findViewById(R.id.nickname);
            feed = (LinearLayout) view.findViewById(R.id.user_feed);
        }
    }

    public void addFriend(String nickname){
        AlertDialog.Builder builder = new AlertDialog.Builder(inflater.getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("Adding following");
        builder.setMessage("You wanna follow this user?");

        builder.setPositiveButton("Of course!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference("Nicknames")
                        .child(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String id = String.valueOf(snapshot.getValue());
                        FirebaseDatabase.getInstance().getReference("Friends")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(id).setValue("true");
                        Toast.makeText(inflater.getContext(), "You are following this user now!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        builder.setNegativeButton("I'm not sure...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void openFeed(String nickname, Context context){
        FirebaseDatabase.getInstance().getReference("Nicknames")
                .child(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String id = String.valueOf(snapshot.getValue());
                FirebaseStorage.getInstance().getReference("Users")
                        .child(id).child("accountPhotos").child("accountPhoto.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String link = uri.toString();
                        Intent intent = new Intent(inflater.getContext(), FriendsPostsActivity.class);
                        intent.putExtra("FRIEND_FEED_ID", id);
                        intent.putExtra("FRIEND_PHOTO", link);
                        context.startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public Filter getFilter() {
        return friendsFilter;
    }

    private Filter friendsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Friend> filteredFriendsList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filteredFriendsList.addAll(friendsSearch);
            }
            else{
                String filterItem = constraint.toString().toLowerCase().trim();
                for(Friend friend : friendsSearch){
                    if(friend.getNickname().toLowerCase().contains(filterItem)){
                        filteredFriendsList.add(friend);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredFriendsList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            friends.clear();
            friends.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
