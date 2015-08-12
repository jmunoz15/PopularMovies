package com.jmunoz.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Trailer implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    private String mId;
    private String mKey;
    private String mName;
    private String mSite;
    private String mType;

    public Trailer(){};

    public Trailer(Parcel parcel){
        mId = parcel.readString();
        mKey = parcel.readString();
        mName = parcel.readString();
        mSite = parcel.readString();
        mType = parcel.readString();
    }

    public void setId(String id){
        mId = id;
    }

    public void setKey(String key){
        mKey = key;
    }

    public void setName(String name){
        mName = name;
    }

    public void setSite(String site){
        mSite = site;
    }

    public void setType(String type){
        mType = type;
    }

    public String getName(){
        return mName;
    }

    public String getKey(){ return mKey; };

    public static List<Trailer> getTrailersFromJSON(JSONObject response) throws JSONException {
        List<Trailer> trailers = new ArrayList<>();
        JSONArray trailerArray = response.getJSONArray("results");
        JSONObject trailerObject;
        Trailer trailer;
        int index;
        for(index = 0; index < trailerArray.length(); index++){
            trailerObject = trailerArray.getJSONObject(index);
            trailer = new Trailer();
            trailer.setId(trailerObject.optString("id"));
            trailer.setKey(trailerObject.optString("key"));
            trailer.setName(trailerObject.optString("name"));
            trailer.setSite(trailerObject.optString("site"));
            trailer.setType(trailerObject.optString("type"));
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
        dest.writeString(mKey);
        dest.writeString(mName);
        dest.writeString(mSite);
        dest.writeString(mType);
    }
}
