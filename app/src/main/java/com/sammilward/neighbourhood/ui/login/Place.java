package com.sammilward.neighbourhood.ui.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Place implements Parcelable {
    private String id;
    private String Name;
    private String CreatedByEmail;
    private String ImageURL;
    private String Description;
    private long ImageUpdatedEpochTime;
    private LatLng Location;
    private String City;

    public Place() {
    }

    protected Place(Parcel in) {
        id = in.readString();
        Name = in.readString();
        CreatedByEmail = in.readString();
        ImageURL = in.readString();
        Description = in.readString();
        ImageUpdatedEpochTime = in.readLong();
        Location = in.readParcelable(LatLng.class.getClassLoader());
        City = in.readString();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCreatedByEmail() {
        return CreatedByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        CreatedByEmail = createdByEmail;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public long getImageUpdatedEpochTime() {
        return ImageUpdatedEpochTime;
    }

    public void setImageUpdatedEpochTime(long imageUpdatedEpochTime) {
        ImageUpdatedEpochTime = imageUpdatedEpochTime;
    }

    public LatLng getLocation() {
        return Location;
    }

    public void setLocation(LatLng location) {
        Location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(Name);
        parcel.writeString(CreatedByEmail);
        parcel.writeString(ImageURL);
        parcel.writeString(Description);
        parcel.writeLong(ImageUpdatedEpochTime);
        parcel.writeParcelable(Location, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeString(City);
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }
}
