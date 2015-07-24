package com.jmunoz.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Movie implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    private String mId;
    private String mTitle;
    private String mPosterPath;
    private String mReleaseDate;
    private double mRating;
    private String mOverview;

    public Movie(){

    }

    public Movie(Parcel parcel){
        mId = parcel.readString();
        mTitle = parcel.readString();
        mPosterPath = parcel.readString();
        mReleaseDate = parcel.readString();
        mRating = parcel.readDouble();
        mOverview = parcel.readString();
    }

    public static List<Movie> getMoviesFromJSON(JSONObject response) throws JSONException {
        List<Movie> movies = new ArrayList<Movie>();
        JSONArray moviesArray = response.getJSONArray("results");
        JSONObject movieObject;
        Movie movie;
        int index;
        for(index = 0; index < moviesArray.length(); index++){
            movieObject = moviesArray.getJSONObject(index);
            movie = new Movie();
            movie.setId(movieObject.optString("id"));
            movie.setTitle(movieObject.optString("title"));
            movie.setPosterPath(movieObject.optString("poster_path"));
            movie.setReleaseDate(movieObject.optString("release_date"));
            movie.setRating(movieObject.optDouble("vote_average"));
            movie.setOverview(movieObject.optString("overview"));
            movies.add(movie);
        }

        return movies;
    }

    public static String getPosterUrl(String posterPath, Context context){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(context.getString(R.string.schema)).authority(context.getString(R.string.image_url))
                .appendPath("t").appendPath("p").appendPath("w500");
        String url = builder.build().toString().concat(posterPath);
        return url;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public double getRating() {
        return mRating;
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mPosterPath);
        dest.writeString(mReleaseDate);
        dest.writeDouble(mRating);
        dest.writeString(mOverview);
    }
}
