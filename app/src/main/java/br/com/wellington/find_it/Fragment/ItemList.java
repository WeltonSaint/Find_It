package br.com.wellington.find_it.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import br.com.wellington.find_it.Activity.MainActivity;
import br.com.wellington.find_it.Bean.Item;
import br.com.wellington.find_it.Adapter.ListItemAdapter;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.ConnectionManagement;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;

/**
 * Classe Nova
 *
 * @author Wellington
 * @version 1.0 - 03/01/2017.
 */
public class ItemList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "option";
    private static final String ARG_PARAM2 = "userCode";

    // TODO: Rename and change types of parameters
    private int mUserCode;
    private int mOption;
    private String[] complementQuery = {"nomeStatus <> ''", "nomeStatus = 'Perdido'", "nomeStatus = 'Encontrado'", "nomeStatus = 'Devolvido'"};
    private ArrayList<Item> listItem;

    private ProgressBar mListItemProgress;
    private RelativeLayout mListItemView;
    private RecyclerView mListItems;
    private TextView lblNotResults;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public ItemList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListItem.
     */
    // TODO: Rename and change types and number of parameters
    public static ItemList newInstance(int param1, int param2) {
        ItemList fragment = new ItemList();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOption = getArguments().getInt(ARG_PARAM1);
            mUserCode = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // save views as variables in this method
        // "view" is the one returned from onCreateView
        String[] listItemDescriptions = getResources().getStringArray(R.array.list_item_descriptions);
        TextView lblItemDescription = (TextView) view.findViewById(R.id.lbl_list_item_desc);
        lblNotResults = (TextView) view.findViewById(R.id.lbl_not_results);
        mListItemProgress = (ProgressBar) view.findViewById(R.id.list_item_progress);
        mListItems = (RecyclerView) view.findViewById(R.id.list_item);
        mListItemView = (RelativeLayout) view.findViewById(R.id.list_item_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                if (isAdded()) {
                                    loadItems();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        }, 3000);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mListItems.setLayoutManager(layoutManager);
        lblItemDescription.setText(listItemDescriptions[mOption]);

        if (savedInstanceState != null) {
            showProgress(false);
            listItem = (ArrayList<Item>) savedInstanceState.getSerializable("listItem");
            startAdapterListItem();
        } else {
            showProgress(true);
            loadItems();
        }

    }

    private void loadItems() {
        if (ConnectionManagement.isConnected(getContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            //Disimissing the progress dialog
                            showProgress(false);

                            JSONObject array;
                            listItem = new ArrayList<>();
                            try {
                                array = new JSONObject(s);
                                JSONArray jsonArrayMessages = array.getJSONArray("listItem");
                                boolean status = array.getBoolean("hasNotReadNotification");
                                MainActivity.hasNotReadNotification(status);

                                for (int i = 0; i < jsonArrayMessages.length(); i++) {
                                    JSONObject jObject = (JSONObject) jsonArrayMessages.get(i);

                                    Item item = new Item();
                                    item.setCodigoItem(jObject.getInt("codigoItem"));
                                    item.setNomeItem(jObject.getString("nomeItem"));
                                    item.setDataCadastro(jObject.getString("dataCadastro"));
                                    item.setDescricaoItem(jObject.getString("descricaoItem"));
                                    item.setLatitudeItem(jObject.getDouble("latitudeItem"));
                                    item.setLongitudeItem(jObject.getDouble("longitudeItem"));
                                    item.setRaioItem(jObject.getDouble("raioItem"));
                                    item.setCategoriaItem(jObject.getString("nomeCategoria"));
                                    item.setStatusItem(jObject.getString("nomeStatus"));
                                    item.setCodigoItem(mUserCode);

                                    ArrayList<String> linksFotos = new ArrayList<>();
                                    JSONArray fotosItem = jObject.getJSONArray("fotosItem");
                                    for (int j = 0; j < fotosItem.length(); j++) {
                                        String foto = fotosItem.getString(j);
                                        linksFotos.add(foto);
                                    }
                                    item.setLinksFotosItem(linksFotos);

                                    listItem.add(item);

                                }

                                startAdapterListItem();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            //Dismissing the progress dialog and show Toast
                            showProgress(false);
                            Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    //Adding parameters
                    params.put("action", "loadItems");
                    params.put("codigoCliente", String.valueOf(mUserCode));
                    params.put("complementoQuery", complementQuery[mOption]);

                    //returning parameters
                    return params;
                }
            };

            //Creating a Request Queue
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());

            //Adding request to the queue
            requestQueue.add(stringRequest);
        } else {
            ConnectionManagement.showIsNotConected(getActivity());
            showProgress(false);
            lblNotResults.setVisibility(View.VISIBLE);
        }
    }

    private void startAdapterListItem() {
        if (listItem.size() == 0)
            lblNotResults.setVisibility(View.VISIBLE);
        else
            lblNotResults.setVisibility(View.GONE);
        ListItemAdapter adapter = new ListItemAdapter(getActivity(), listItem);
        mListItems.setAdapter(adapter);
        mListItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    MainActivity.setFabVisibility(false);
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    MainActivity.setFabVisibility(true);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("listItem", listItem);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (isAdded() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListItemView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListItemView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListItemView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mListItemProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mListItemProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListItemProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mListItemProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mListItemView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}