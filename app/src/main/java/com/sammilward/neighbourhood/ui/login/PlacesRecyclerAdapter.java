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

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final List<Place> places;
    private final LayoutInflater layoutInflater;

    private StorageReference storageReference;

    public PlacesRecyclerAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
        this.layoutInflater = LayoutInflater.from(context);
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_place_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = places.get(position);
        holder.currentPosition = position;
        holder.lblPlaceName.setText(place.getName());
        holder.lblDescription.setText(place.getDescription());
        displayEventPicture(holder.imgEventPicture, place);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    private void displayEventPicture(ImageView imageView, Place place)
    {
        StorageReference storageReference = this.storageReference.child(place.getImageURL());
        GlideApp.with(context).load(storageReference).signature(new ObjectKey(place.getImageUpdatedEpochTime())).into(imageView);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView lblPlaceName, lblDescription;
        public ImageView imgEventPicture;
        public int currentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblPlaceName = itemView.findViewById(R.id.lblPlacesListName);
            lblDescription = itemView.findViewById(R.id.lblPlacesListDescription);
            imgEventPicture = itemView.findViewById(R.id.imgPlacesListPicture);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewEditPlaceActivity.class);
                    intent.putExtra("PLACE", places.get(currentPosition));
                    context.startActivity(intent);
                }
            });
        }
    }
}
