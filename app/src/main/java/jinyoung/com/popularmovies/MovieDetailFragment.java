package jinyoung.com.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import jinyoung.com.popularmovies.models.Movie;


public class MovieDetailFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            Movie movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);

            ((TextView) rootView.findViewById(R.id.movie_title)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.movie_release_date)).setText(movie.getReleaseDate());
            ((TextView) rootView.findViewById(R.id.movie_rate)).setText(getString(R.string.vote_late_prefix) + movie.getRate());
            ((TextView) rootView.findViewById(R.id.movie_plot_synopsis)).setText(movie.getSynopsis());

            Picasso.with(getContext()).load(movie.getPosterPath()).into(((ImageView) rootView.findViewById(R.id.movie_poster)));
        }

        return rootView;
    }
}
