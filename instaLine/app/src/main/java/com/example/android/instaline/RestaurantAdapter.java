package com.example.android.instaline;

/**
 * Created by JERRYYI on 2018-03-04.
 */

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
 * RecyclerView.Adapter
 * RecyclerView.ViewHolder
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>{

    private Context mContext;
    private List<RestaurantModel> restaurantList;

    public RestaurantAdapter(Context mContext, List<RestaurantModel> restaurantList) {
        this.mContext = mContext;
        this.restaurantList = restaurantList;
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.restaurant_card, null);
        RestaurantViewHolder holder = new RestaurantViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RestaurantViewHolder holder, int position) {
        RestaurantModel restaurant = restaurantList.get(position);

        holder.restaurant_title.setText(restaurant.getRestaurantTitle());
        holder.time_detail.setText(restaurant.getTime());

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
        TextView restaurant_title, time_detail;
        String id;
        public RestaurantViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.restaurant_cover_image);
            restaurant_title = itemView.findViewById(R.id.restaurant_title);
            time_detail = itemView.findViewById(R.id.item_time_detail);



            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    Intent intent = new Intent(itemView.getContext(), ExploreContentActivity.class);
                    intent.putExtra("id",id);
                    itemView.getContext().startActivity(intent);

                }
            });
        }
    }
}
