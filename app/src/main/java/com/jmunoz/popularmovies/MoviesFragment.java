package com.jmunoz.popularmovies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jmunoz.popularmovies.data.MoviesContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;


public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String TAG = MoviesFragment.class.getSimpleName();
    public static final String MOVIE_EXTRA = "Movie";
    public static final int MAX_PAGES = 1000;

    private static final int MOVIES_LOADER = 0;

    private static final String[] MOVIES_COLUMS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.SERVER_ID,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_POSTER,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_RATING
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_SERVER_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_POSTER = 3;
    static final int COL_RELEASE_DATE = 4;
    static final int COL_OVERVIEW = 5;
    static final int COL_RATING = 6;

    private MovieAdapter mMovieAdapter;
    private int mPage;
    private boolean mLoadingPage;
    private View mRootView;
    private String mSortSelected;
    private SharedPreferences mPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSortSelected = mPreferences.getString(getString(R.string.sort_by),
                getString(R.string.popularity_value));
    }

    @Override
    public void onResume() {
        super.onResume();
        String sortPreference = mPreferences.getString(getString(R.string.sort_by),
                getString(R.string.popularity_value));
        if(!(sortPreference.equalsIgnoreCase(mSortSelected))){
            mSortSelected = sortPreference;
            if(sortPreference.equalsIgnoreCase(getString(R.string.favorites_value))){
                getLoaderManager().initLoader(MOVIES_LOADER, null, this);
            }
            else{
                mPage = 0;
                mMovieAdapter.resetAdapter();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            mRootView = inflater.inflate(R.layout.fragment_movies, container, false);

                mMovieAdapter = new MovieAdapter(getActivity());
                GridView gridView = (GridView) mRootView.findViewById(R.id.gridView);
                gridView.setAdapter(mMovieAdapter);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ((Callback) getActivity()).onItemSelected((Movie) mMovieAdapter.getItem(position));
                    }
                });
                gridView.setOnScrollListener(new EndlessScrollListener());

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mPreferences.getString(getString(R.string.sort_by), getString(R.string.popularity_value))
                .equalsIgnoreCase(getString(R.string.favorites_value))){
            getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        }
    }

    private void getMoviesList() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(getString(R.string.schema)).authority(getString(R.string.base_url)).
                appendPath("3").appendPath("discover").appendPath("movie").
                appendQueryParameter(getString(R.string.sort_by),
                        mSortSelected).
                appendQueryParameter(getString(R.string.api_key), getString(R.string.api_key_value)).
                appendQueryParameter(getString(R.string.page), String.valueOf(mPage));
        String url = builder.build().toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Movie> moviesList = Movie.getMoviesFromJSON(response);
                            mMovieAdapter.addMovies(moviesList);
                            mLoadingPage = false;

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri moviesUri = MoviesContract.MoviesEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                moviesUri,
                MOVIES_COLUMS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<Movie> moviesList = Movie.getMoviesFromCursor(data);
        mMovieAdapter.setMovies(moviesList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    class EndlessScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            if ((!(mLoadingPage)) && ((totalItemCount - visibleItemCount) <= (firstVisibleItem))
                    && (mPage < MAX_PAGES) && (!getString(R.string.favorites_value).equalsIgnoreCase(mSortSelected))) {
                mPage++;
                getMoviesList();
                mLoadingPage = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie movie);
    }

}
