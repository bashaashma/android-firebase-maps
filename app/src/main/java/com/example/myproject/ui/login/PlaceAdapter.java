package com.example.myproject.ui.login;

import android.content.Context;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private List<String> mPlacesList;

    public PlaceAdapter(List<String> placesList) {
        mPlacesList = placesList;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView placeTextView;

        public ViewHolder(View view) {
            super(view);
            placeTextView = view.findViewById(R.id.textView4);

        }
    }


    @Override
    public PlaceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.recycler_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PlaceAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        String fp = mPlacesList.get(position);

        TextView textView = viewHolder.placeTextView;
        textView.setText(fp);


    }

    @Override
    public int getItemCount() {
        return mPlacesList.size();
    }

}
