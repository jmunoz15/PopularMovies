package com.jmunoz.popularmovies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MoviesFragment extends Fragment {

    public static final String TAG = MoviesFragment.class.getSimpleName();

    public static final String MOVIE_EXTRA = "Movie";

    private MovieAdapter mMovieAdapter;

    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        mMovieAdapter = new MovieAdapter(getActivity());
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                intent.putExtra(MOVIE_EXTRA, (Movie) mMovieAdapter.getItem(position));
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMovieAdapter.resetMovies();
        getMoviesList();
    }

    private void getMoviesList() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(getString(R.string.schema)).authority(getString(R.string.base_url)).
                appendPath("3").appendPath("discover").appendPath("movie").
                appendQueryParameter(getString(R.string.sort_by),
                        preferences.getString(getString(R.string.sort_by),
                                getString(R.string.popularity_value))).
                appendQueryParameter(getString(R.string.api_key), getString(R.string.api_key_value));
        String url = builder.build().toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Movie> moviesList = Movie.getMoviesFromJSON(response);
                            mMovieAdapter.addMovies(moviesList);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
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
}
