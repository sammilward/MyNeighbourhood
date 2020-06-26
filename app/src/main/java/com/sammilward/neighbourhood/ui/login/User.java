package com.sammilward.neighbourhood.ui.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User implements Parcelable {
    private String Id;
    private String DisplayName;
    private String Email;
    private String ImageURL;
    private String Bio;
    private String Ethnicity;
    private String Postcode;
    private long ImageUpdatedEpochTime;
    private LatLng Location;
    private String City;
    private List<String> Interests;
    private Date DOB;
    private int NumberEvents;
    private int NumberPlaces;
    private int NumberFriends;
    private List<String> FriendsList;

    public User() {
    }

    protected User(Parcel in) {
        Id = in.readString();
        DisplayName = in.readString();
        Email = in.readString();
        Ethnicity = in.readString();
        Bio = in.readString();
        ImageURL = in.readString();
        Postcode = in.readString();
        ImageUpdatedEpochTime = in.readLong();
        List <String> tempInterests = new ArrayList<>();
        in.readStringList(tempInterests);
        Interests = tempInterests;
        DOB = (Date) in.readValue(getClass().getClassLoader());
        NumberEvents = in.readInt();
        NumberPlaces = in.readInt();
        NumberFriends = in.readInt();
        Location = in.readParcelable(LatLng.class.getClassLoader());
        City = in.readString();
        List <String> tempFriends = new ArrayList<>();
        in.readStringList(tempFriends);
        FriendsList = tempFriends;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getBio() {
        return Bio;
    }

    public void setBio(String bio) {
        Bio = bio;
    }

    public String getEthnicity() {
        return Ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        Ethnicity = ethnicity;
    }

    public List<String> getInterests() {
        return Interests;
    }

    public void setInterests(List<String> interests) {
        Interests = interests;
    }


    public String getPostcode() {
        return Postcode;
    }

    public void setPostcode(String postcode) {
        Postcode = postcode;
    }

    public long getImageUpdatedEpochTime() {
        return ImageUpdatedEpochTime;
    }

    public void setImageUpdatedEpochTime(long imageUpdatedEpochTime) {
        this.ImageUpdatedEpochTime = imageUpdatedEpochTime;
    }

    public LatLng getLatLon() {
        return Location;
    }

    public void setLocation(LatLng location) {
        Location = location;
    }

    public Date getDOB() {
        return DOB;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }

    public int getNumberEvents() {
        return NumberEvents;
    }

    public void setNumberEvents(int numberEvents) {
        NumberEvents = numberEvents;
    }

    public int getNumberPlaces() {
        return NumberPlaces;
    }

    public void setNumberPlaces(int numberPlaces) {
        NumberPlaces = numberPlaces;
    }

    public int getNumberFriends() {
        return NumberFriends;
    }

    public void setNumberFriends(int numberFriends) {
        NumberFriends = numberFriends;
    }

    public int getAge()
    {
        LocalDate dob = DOB.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(dob, currentDate).getYears();
    }

    public List<String> getFriendsList() {
        return FriendsList;
    }

    public void setFriendsList(List<String> friendsList) {
        FriendsList = friendsList;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }

        User user = (User) obj;

        if(!this.getDisplayName().equals(user.getDisplayName())) return false;
        if(!this.getEmail().equals(user.getEmail())) return false;
        if(!this.getBio().equals(user.getBio())) return false;
        if(!this.getEthnicity().equals(user.getEthnicity())) return false;
        if(!this.getPostcode().equals(user.getPostcode())) return false;
        if(!this.getImageURL().equals(user.getImageURL())) return false;
        if(!(this.getNumberEvents() == user.getNumberEvents())) return false;
        if(!(this.getNumberFriends() == user.getNumberFriends())) return false;
        if(!(this.getNumberPlaces() == user.getNumberPlaces())) return false;
        if(!(this.getDOB().equals(user.getDOB()))) return false;
        if(!this.getInterests().equals(user.getInterests())) return false;
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Id);
        parcel.writeString(DisplayName);
        parcel.writeString(Email);
        parcel.writeString(Ethnicity);
        parcel.writeString(Bio);
        parcel.writeString(ImageURL);
        parcel.writeString(Postcode);
        parcel.writeLong(ImageUpdatedEpochTime);
        parcel.writeStringList(Interests);
        parcel.writeValue(DOB);
        parcel.writeInt(NumberEvents);
        parcel.writeInt(NumberPlaces);
        parcel.writeInt(NumberFriends);
        parcel.writeParcelable(Location, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeString(City);
        parcel.writeStringList(FriendsList);
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getInterestListAsText()
    {
        String interestText = "";
        if (!Interests.isEmpty())
        {
            for (String interest : Interests) {
                interestText += (interest + ", ");
            }
            return interestText.trim().substring(0, interestText.lastIndexOf(","));
        } else return  interestText;
    }
}
