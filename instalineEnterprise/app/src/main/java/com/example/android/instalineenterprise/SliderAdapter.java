package com.example.android.instalineenterprise;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by GOR on 2018-02-06.
 */

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context){

        this.context = context;
    }

    public int[] slide_images = {
            R.mipmap.intro1,
            R.mipmap.intro2,
            R.mipmap.intro3
    };

    public  String[] slide_title = {
            "Anytime",
            "Everywhere",
            "Whatever"
    };

    public String[] slide_description = {
            "春眠不觉晓,\n处处闻啼鸟.\n夜来风雨声,\n花落知多少.",
            "床前明月光,\n疑是地上霜.\n举头望明月,\n低头思故乡.",
            "靖康耻,犹未雪.\n臣子恨,何时灭.\n驾长车,踏破贺兰山缺.\n壮志饥餐胡虏肉,\n笑谈渴饮匈奴血.\n待从头,收拾旧山河,\n朝天阙."
    };

    @Override
    public  int getCount(){

        return slide_images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object o){

        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container,false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.slide_image);
        TextView slideTitle = (TextView) view.findViewById(R.id.title);
        TextView slideDescription = (TextView) view.findViewById(R.id.description);

        slideImageView.setImageResource(slide_images[position]);
        slideTitle.setText(slide_title[position]);
        slideDescription.setText(slide_description[position]);

        container.addView(view);

        return view;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        container.removeView((LinearLayout)object);
    }
}
