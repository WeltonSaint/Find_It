package br.com.wellington.find_it.Activity;


import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.TouchImageView;

public class ZoomImageActivity extends AppCompatActivity {

    private ViewPager mPager;

    private ArrayList<String> linksFotosItem;
    private String option;
    private int mPosition;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            option = extras.getString("option");
            if(option != null && option.equals("zoomPhotoItem")){
                linksFotosItem = (ArrayList<String>) extras.getSerializable("linksFotosItem");
                mPosition = extras.getInt("position");
            } else {
                String foto = extras.getString("fotoPerfil");
                linksFotosItem = new ArrayList<>();
                linksFotosItem.add(foto);
            }
        }

        ImageButton mLeftImages = (ImageButton) findViewById(R.id.left_images);
        ImageButton mRightImages = (ImageButton) findViewById(R.id.right_images);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new ZoomImagePagerAdapter(ZoomImageActivity.this, linksFotosItem));
        mPager.setCurrentItem(mPosition);
        if(option.equals("zoomPhotoItem")) {
            setTitle(getString(R.string.title_activity_zoom_image, mPosition + 1, linksFotosItem.size()));
        } else {
            setTitle(getString(R.string.title_activity_zoom_profile));
            mLeftImages.setVisibility(View.GONE);
            mRightImages.setVisibility(View.GONE);
        }
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                setTitle(getString(R.string.title_activity_zoom_image, position+1, linksFotosItem.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mLeftImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPosition = mPager.getCurrentItem() - 1;
                if (mPosition < 0) {
                    mPosition = linksFotosItem.size() - 1;
                }
                mPager.setCurrentItem(mPosition);
            }
        });

        mRightImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPosition = mPager.getCurrentItem() + 1;
                if (mPosition > linksFotosItem.size() - 1) {
                    mPosition = 0;
                }
                mPager.setCurrentItem(mPosition);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ZoomImagePagerAdapter extends PagerAdapter {


        private ArrayList<String> IMAGES;
        private LayoutInflater inflater;
        private Context context;


        ZoomImagePagerAdapter(Context context, ArrayList<String> IMAGES) {
            this.context = context;
            this.IMAGES = IMAGES;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return IMAGES.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.zoom_image_page, view, false);

            assert imageLayout != null;
            final ProgressBar mLoadingImages = (ProgressBar) imageLayout.findViewById(R.id.loading_images);
            TouchImageView mZoomImages = (TouchImageView) imageLayout.findViewById(R.id.zoom_images);
            mZoomImages.setMaxZoom(4f);

            Picasso.with(context)
                    .load(IMAGES.get(position))
                    .into(mZoomImages, new Callback() {
                        @Override
                        public void onSuccess() {
                            mLoadingImages.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mLoadingImages.setVisibility(View.GONE);
                        }
                    });


            view.addView(imageLayout, 0);

            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }


    }

}
