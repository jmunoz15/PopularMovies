package com.jmunoz.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends BaseAdapter {

    private List<Movie> mMoviesList;
    private LayoutInflater mInflater;
    private Context mContext;

    public MovieAdapter(Context context) {
        mMoviesList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void addMovies(List<Movie> movies) {
        mMoviesList.addAll(movies);
        notifyDataSetChanged();
    }

    public void setMovies(List<Movie> movies) {
        mMoviesList = movies;
        notifyDataSetChanged();
    }

    public void resetAdapter(){
        mMoviesList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMoviesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMoviesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        String posterPath = mMoviesList.get(position).getPosterPath();
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_movie, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.poster = (ImageView) convertView.findViewById(R.id.poster);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Picasso.with(mContext)
                .load(Movie.getPosterUrl(posterPath, mContext))
                .into(viewHolder.poster);

        return convertView;
    }

    static class ViewHolder {
        ImageView poster;
    }
}
