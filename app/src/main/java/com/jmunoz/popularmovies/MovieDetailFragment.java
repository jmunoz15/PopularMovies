package com.jmunoz.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MovieDetailFragment extends Fragment {

    public static final String TAG = MovieDetailFragment.class.getSimpleName();

    private Movie mMovie;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie = getArguments().getParcelable(MoviesFragment.MOVIE_EXTRA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        TextView titleText = (TextView) rootView.findViewById(R.id.titleText);
        titleText.setText(mMovie.getTitle());
        TextView overviewText = (TextView) rootView.findViewById(R.id.overviewText);
        overviewText.setText(mMovie.getOverview());
        RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        ratingBar.setRating((float) mMovie.getRating() / 2.0f);
        TextView dateText = (TextView) rootView.findViewById(R.id.dateText);
        dateText.setText(getReleaseDateWithFormat());
        ImageView poster = (ImageView) rootView.findViewById(R.id.imageView);
        Picasso.with(getActivity())
                .load(Movie.getPosterUrl(mMovie.getPosterPath(), getActivity()))
                .into(poster);
        return rootView;
    }

    private String getReleaseDateWithFormat(){
        SimpleDateFormat inputFormat = new SimpleDateFormat(getString(R.string.date_api_format));
        SimpleDateFormat outputFormat = new SimpleDateFormat(getString(R.string.date_app_format));
        Date date;
        String dateStr = "";
        try {
            date = inputFormat.parse(mMovie.getReleaseDate());
            dateStr = outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        finally {
            return dateStr;
        }
    }
}
