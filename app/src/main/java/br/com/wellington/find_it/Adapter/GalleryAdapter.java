package br.com.wellington.find_it.Adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.wellington.find_it.R;

/**
 * Classe Adaptadora da Galeria de Imagens
 *
 * @author Wellington
 * @version 1.0 - 05/01/2017.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private ArrayList<String> gallery;
    private List<Uri> galleryUri;
    private Context context;

    GalleryAdapter(Context context, ArrayList<String> gallery) {
        this.gallery = gallery;
        this.context = context;
    }

    public GalleryAdapter(Context context, List<Uri> galleryUri) {
        this.galleryUri = galleryUri;
        this.context = context;
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_gallery, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GalleryAdapter.ViewHolder viewHolder, int i) {
        //viewHolder.img.setImageResource(context.getResources().getIdentifier("drawable/" + gallery.get(i), null, context.getPackageName()));
        //Picasso.with(context).load(gallery.get(i).getAndroid_image_url()).resize(240, 120).into(viewHolder.img);
        if (gallery != null) {
            Picasso.with(context)
                    .load(gallery.get(i))
                    .into(viewHolder.img, new Callback() {
                        @Override
                        public void onSuccess() {
                            viewHolder.loadingImg.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            viewHolder.loadingImg.setVisibility(View.GONE);
                        }
                    });
        } else {
            Picasso.with(context)
                    .load(galleryUri.get(i))
                    .into(viewHolder.img, new Callback() {
                        @Override
                        public void onSuccess() {
                            viewHolder.loadingImg.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            viewHolder.loadingImg.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        if(gallery != null)
        return gallery.size();
        else return galleryUri.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private ProgressBar loadingImg;

        ViewHolder(View view) {
            super(view);
            img = (ImageView) view.findViewById(R.id.img);
            loadingImg = (ProgressBar) view.findViewById(R.id.loading_img);
        }
    }

}