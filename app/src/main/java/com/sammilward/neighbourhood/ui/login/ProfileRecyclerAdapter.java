package com.sammilward.neighbourhood.ui.login;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sammilward.neighbourhood.R;

import java.util.List;

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final List<User> users;
    private final User currentUser;
    private final LayoutInflater layoutInflater;

    private StorageReference storageReference;

    public ProfileRecyclerAdapter(Context context, List<User> users, User currentUser) {
        this.context = context;
        this.users = users;
        this.currentUser = currentUser;
        this.layoutInflater = android.view.LayoutInflater.from(context);
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_profile_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.currentPosition = position;
        holder.lblDisplayName.setText(user.getDisplayName());
        holder.lblAge.setText(Integer.toString(user.getAge()));
        holder.lblEthnicity.setText(user.getEthnicity());
        holder.lblInterests.setText(user.getInterestListAsText());
        displayProfilePicture(holder.imgProfilePicture, user);
        if (user.getFriendsList().contains(currentUser.getEmail())) holder.imgFriendsHeart.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void displayProfilePicture(ImageView imageView, User user)
    {
        StorageReference storageReference = this.storageReference.child(user.getImageURL());
        GlideApp.with(context).load(storageReference).signature(new ObjectKey(user.getImageUpdatedEpochTime())).into(imageView);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView lblDisplayName, lblEthnicity, lblAge, lblInterests;
        public ImageView imgProfilePicture, imgFriendsHeart;
        public int currentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblDisplayName = itemView.findViewById(R.id.lblPersonListDisplayName);
            lblEthnicity = itemView.findViewById(R.id.lblPersonListEthnicity);
            lblAge = itemView.findViewById(R.id.lblPersonListAge);
            lblInterests = itemView.findViewById(R.id.lblPersonListInterests);
            imgProfilePicture = itemView.findViewById(R.id.imgPeopleListProfilePicture);
            imgFriendsHeart = itemView.findViewById(R.id.imgFriendsHeart);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UsersProfileActivity.class);
                    intent.putExtra("SELECTEDUSER", users.get(currentPosition));
                    intent.putExtra("CURRENTUSER", currentUser);
                    context.startActivity(intent);
                }
            });
        }
    }
}
