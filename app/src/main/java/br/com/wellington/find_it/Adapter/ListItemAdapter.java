package br.com.wellington.find_it.Adapter;

/**
 * Classe Adaptadora de Listagem de Itens
 *
 * @author Wellington
 * @version 1.0 - 03/01/2017.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.wellington.find_it.Activity.ZoomImageActivity;
import br.com.wellington.find_it.Bean.Item;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.RecyclerItemClickListener;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ViewHolder> {

    private ArrayList<Item> items;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public ListItemAdapter(Context context, ArrayList<Item> items) {
        this.items = items;
        ListItemAdapter.context = context;
    }

    @Override
    public ListItemAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        @SuppressLint("InflateParams") View rootView = LayoutInflater.from(context).inflate(R.layout.list_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return new ViewHolder(rootView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ListItemAdapter.ViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {
        viewHolder.lbNome.setText(items.get(i).getNomeItem());
        viewHolder.lblData.setText(items.get(i).getDataCadastro());
        viewHolder.lblCategoria.setText(context.getString(R.string.lbl_category) + items.get(i).getCategoriaItem());
        viewHolder.lblStatus.setText(context.getString(R.string.lbl_status) + items.get(i).getStatusItem());
        viewHolder.lblDescricao.setText(context.getString(R.string.lbl_description) + items.get(i).getDescricaoItem());

        LinearLayoutManager llm = new LinearLayoutManager(context);
        viewHolder.noPhotos.setVisibility((items.size() > 0) ? View.GONE : View.VISIBLE);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        viewHolder.cardRecyclerView.setLayoutManager(llm);

        viewHolder.menuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(context, viewHolder.menuItem);
                popup.inflate(R.menu.menu_item);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_alter:
                                showDialogAlterStatusItem(i);
                                break;
                            case R.id.action_delete:
                                //handle menu2 click
                                break;
                        }
                        return false;
                    }
                });
                popup.show();

            }
        });

        GalleryAdapter adapter = new GalleryAdapter(context, items.get(i).getLinksFotosItem());
        viewHolder.cardRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, viewHolder.cardRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        openZoomImageLayout(items.get(i).getLinksFotosItem(), position);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
        viewHolder.cardRecyclerView.setAdapter(adapter);
    }

    private void showDialogAlterStatusItem(int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        View mView = layoutInflaterAndroid.inflate(R.layout.alter_item_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(mView);

        final Spinner mAlterStatusItem = (Spinner) mView.findViewById(R.id.spin_alter_status);
        String[] options = {items.get(position).getStatusItem(), "Devolvido"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAlterStatusItem.setAdapter(spinnerArrayAdapter);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.lbl_alter), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        String status = mAlterStatusItem.getSelectedItem().toString();
                        if (status.equals("Devolvido")) {
                        /*try {
                            UpdateStatusItemTask task = new UpdateStatusItemTask(listItem.get(position).getCodigo());
                            boolean success = false;

                            success = task.execute((Void) null).get();
                            AlertDialog.Builder dlg = new AlertDialog.Builder(ItemList.this.getContext());

                            if (success) {
                                dlg.setMessage(ItemList.this.getResources().getString(R.string.alter_status_item_success));
                            } else {
                                dlg.setMessage(ItemList.this.getResources().getString(R.string.alter_status_item_error));
                            }
                            dlg.setNeutralButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {
                                            dialogBox.cancel();
                                        }
                                    });
                            dlg.show();

                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }*/

                        } else {
                            dialogBox.cancel();
                        }
                    }
                }).setNegativeButton(context.getString(R.string.lbl_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        dialogBox.cancel();
                    }
                });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    private void openZoomImageLayout(ArrayList<String> linksFotosItem, int position){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("linksFotosItem", linksFotosItem);
        bundle.putInt("position", position);
        bundle.putString("option", "zoomPhotoItem");
        intent.putExtras(bundle);
        intent.setClass(context, ZoomImageActivity.class);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView lbNome, lblData, lblCategoria, lblStatus, lblDescricao;
        private ImageView menuItem;
        private RecyclerView cardRecyclerView;
        private TextView noPhotos;

        ViewHolder(View view) {
            super(view);
            lbNome = (TextView) view.findViewById(R.id.lbl_titulo);
            lblData = (TextView) view.findViewById(R.id.lbl_data);
            lblCategoria = (TextView) view.findViewById(R.id.lbl_categoria);
            lblStatus = (TextView) view.findViewById(R.id.lbl_status);
            lblDescricao = (TextView) view.findViewById(R.id.lbl_desc);
            cardRecyclerView = (RecyclerView) view.findViewById(R.id.card_recycler_view);
            noPhotos = (TextView) view.findViewById(R.id.no_photos);
            menuItem = (ImageView) view.findViewById(R.id.menu_item);
        }


    }

}
