package com.jmunoz.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jmunoz.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MovieDetailFragment extends Fragment {

    public static final String TAG = MovieDetailFragment.class.getSimpleName();
    public static final String FAVORITE_MOVIES = "favorite_movies";

    private Movie mMovie;
    private ShareActionProvider mShareActionProvider;

    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie = getArguments().getParcelable(MoviesFragment.MOVIE_EXTRA);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(getString(R.string.schema)).authority(getString(R.string.youtube_url)).
                appendPath("watch").
                appendQueryParameter("v", mMovie.getmTrailerList().get(1).getKey());
        shareIntent.putExtra(Intent.EXTRA_TEXT, builder.build().toString());
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        if(mMovie != null) {
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
            CheckBox favoriteCheckBox = (CheckBox) rootView.findViewById(R.id.favoriteCheckBox);
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Set<String> favoriteMovies = preferences.getStringSet(FAVORITE_MOVIES, new HashSet<String>());
            if (favoriteMovies.contains(mMovie.getId())) {
                favoriteCheckBox.setChecked(true);
            }
            favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ContentValues values = new ContentValues();
                        values.put(MoviesContract.MoviesEntry.SERVER_ID, mMovie.getId());
                        values.put(MoviesContract.MoviesEntry.COLUMN_TITLE, mMovie.getTitle());
                        values.put(MoviesContract.MoviesEntry.COLUMN_POSTER, mMovie.getPosterPath());
                        values.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
                        values.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, mMovie.getOverview());
                        values.put(MoviesContract.MoviesEntry.COLUMN_RATING, mMovie.getRating());
                        getActivity().getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, values);
                        favoriteMovies.add(mMovie.getId());
                    } else {
                        String selection = MoviesContract.MoviesEntry.SERVER_ID + " = ?";
                        String[] selectionArgs = {mMovie.getId()};
                        getActivity().getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI, selection, selectionArgs);
                        favoriteMovies.remove(mMovie.getId());
                    }
                    preferences.edit().putStringSet(FAVORITE_MOVIES, favoriteMovies).commit();
                }
            });
            getTrailerList();
            getReviewList();
        }
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

    private void getTrailerList() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(getString(R.string.schema)).authority(getString(R.string.base_url)).
                appendPath("3").appendPath("movie").appendPath(mMovie.getId()).appendPath("videos").
                appendQueryParameter(getString(R.string.api_key), getString(R.string.api_key_value));
        String url = builder.build().toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Trailer> trailers = Trailer.getTrailersFromJSON(response);
                            mMovie.setmTrailerList(trailers);
                            TrailerAdapter adapter = new TrailerAdapter();
                            ListView trailerList = (ListView) getView().findViewById(R.id.trailerListView);
                            trailerList.setAdapter(adapter);
                            setListViewHeightBasedOnChildren(trailerList);
                            trailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Uri.Builder builder = new Uri.Builder();
                                    builder.scheme(getString(R.string.schema)).authority(getString(R.string.youtube_url)).
                                            appendPath("watch").
                                            appendQueryParameter("v", mMovie.getmTrailerList().get(position).getKey());
                                    startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));

                                }
                            });
                            if(trailers.size() > 0){
                                TextView trailerLabel = (TextView) getView().findViewById(R.id.trailersLabel);
                                trailerLabel.setVisibility(View.VISIBLE);
                                mShareActionProvider.setShareIntent(createShareTrailerIntent());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error retrieving trailer list");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, volleyError.getMessage());
                        Toast.makeText(getActivity(), volleyError.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        PopularMoviesApp.getInstance().addToRequestQueue(request, TAG);
    }

    private void getReviewList() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(getString(R.string.schema)).authority(getString(R.string.base_url)).
                appendPath("3").appendPath("movie").appendPath(mMovie.getId()).appendPath("reviews").
                appendQueryParameter(getString(R.string.api_key), getString(R.string.api_key_value));
        String url = builder.build().toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Review> reviews = Review.getReviewsFromJSON(response);
                            mMovie.setReviewList(reviews);
                            LinearLayout container = (LinearLayout) getView().findViewById(R.id.container);
                            for(Review review : reviews){
                                LinearLayout reviewLayout = (LinearLayout)LayoutInflater.from(getActivity())
                                        .inflate(R.layout.adapter_review, null);
                                TextView contentText = (TextView)reviewLayout.findViewById(R.id.reviewText);
                                TextView authorText = (TextView)reviewLayout.findViewById(R.id.authorText);
                                contentText.setText(review.getContent());
                                authorText.setText(review.getAuthor());
                                container.addView(reviewLayout);
                            }

                            if(reviews.size() > 0){
                                TextView reviewLabel = (TextView) getView().findViewById(R.id.reviewsLabel);
                                reviewLabel.setVisibility(View.VISIBLE);

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error retrieving review list");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, volleyError.getMessage());
                        Toast.makeText(getActivity(), volleyError.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        PopularMoviesApp.getInstance().addToRequestQueue(request, TAG);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private class TrailerAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mMovie.getmTrailerList() != null ? mMovie.getmTrailerList().size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mMovie.getmTrailerList() != null ?
                    ((ArrayList)mMovie.getmTrailerList()).get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.adapter_trailer, null);
                holder.title = (TextView) convertView.findViewById(R.id.trailerTitle);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(mMovie.getmTrailerList().get(position).getName());
            return convertView;
        }

        class ViewHolder{
            TextView title;
        }
    }
}
