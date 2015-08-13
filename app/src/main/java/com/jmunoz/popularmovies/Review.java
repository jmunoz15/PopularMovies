package com.jmunoz.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Review implements Parcelable {

    public static final Creator CREATOR = new Creator() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    private String mId;
    private String mAuthor;
    private String mContent;


    public Review(){};

    public Review(Parcel parcel){
        mId = parcel.readString();
        mAuthor = parcel.readString();
        mContent = parcel.readString();
    }

    public void setId(String id){
        mId = id;
    }

    public void setAuthor(String author){
        mAuthor = author;
    }

    public void setContent(String content){
        mContent = content;
    }

    public String getAuthor(){
        return mAuthor;
    }

    public String getContent(){ return mContent; };

    public static List<Review> getReviewsFromJSON(JSONObject response) throws JSONException {
        List<Review> trailers = new ArrayList<>();
        JSONArray trailerArray = response.getJSONArray("results");
        JSONObject trailerObject;
        Review trailer;
        int index;
        for(index = 0; index < trailerArray.length(); index++){
            trailerObject = trailerArray.getJSONObject(index);
            trailer = new Review();
            trailer.setId(trailerObject.optString("id"));
            trailer.setAuthor(trailerObject.optString("author"));
            trailer.setContent(trailerObject.optString("content"));
            trailers.add(trailer);
        }
        return trailers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mAuthor);
        dest.writeString(mContent);
    }
}
