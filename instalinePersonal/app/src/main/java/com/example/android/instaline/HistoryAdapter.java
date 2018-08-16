package com.example.android.instaline;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by GOR on 2018-03-05.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.RestaurantViewHolder>{
    private Context mContext;
    private List<RestaurantModel> restaurantList;

    public HistoryAdapter(Context mContext, List<RestaurantModel> restaurantList) {
        this.mContext = mContext;
        this.restaurantList = restaurantList;
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.history_card, null);
        HistoryAdapter.RestaurantViewHolder holder = new HistoryAdapter.RestaurantViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RestaurantViewHolder holder, int position) {
        RestaurantModel restaurant = restaurantList.get(position);

        holder.restaurant_title.setText(restaurant.getRestaurantTitle());
        holder.time_detail.setText(restaurant.getTime());
        holder.queueRank.setText(restaurant.getQueueRank());

        // SETTING IMAGE
//        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(restaurant.getImage()));
        Glide.with(mContext).load(restaurant.getImageURL()).into(holder.imageView);


        holder.id = restaurant.getId();

    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView restaurant_title, time_detail, queueRank;
        String id;
        public RestaurantViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.restaurant_cover_image);
            restaurant_title = itemView.findViewById(R.id.restaurant_title);
            time_detail = itemView.findViewById(R.id.item_time_detail);
            queueRank = itemView.findViewById(R.id.queueRank);



            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    Intent intent = new Intent(itemView.getContext(), HistoryContentActivity.class);
                    intent.putExtra("id",id);
                    itemView.getContext().startActivity(intent);

                }
            });
        }
    }
}

