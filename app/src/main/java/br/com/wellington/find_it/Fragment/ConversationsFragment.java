package br.com.wellington.find_it.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

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

import br.com.wellington.find_it.Activity.ChatActivity;
import br.com.wellington.find_it.Activity.MainActivity;
import br.com.wellington.find_it.Adapter.ConversationAdapter;
import br.com.wellington.find_it.Bean.ChatConversation;
import br.com.wellington.find_it.Bean.Cliente;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.ConnectionManagement;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;


public class ConversationsFragment extends Fragment implements AdapterView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RequestQueue requestQueue;
    private Cliente user;

    private ListView mListConversations;
    private RelativeLayout mEmptyListConversationView, mListConversationView;
    private ProgressBar mConversationProgress;
    private ConversationAdapter mAdapterConversation;
    private static final int POLL_INTERVAL = 1000;
    private ArrayList<ChatConversation> conversations;
    private Handler mHandler = new Handler();

    private Runnable mRefreshConversationsRunnable = new Runnable() {
        @Override
        public void run() {
            loadConversations();
            mHandler.postDelayed(this, POLL_INTERVAL);
        }

    };

    public ConversationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConversationsFragment.
     */
    public static ConversationsFragment newInstance(int param1, Cliente param2) {
        ConversationsFragment fragment = new ConversationsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putSerializable(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (Cliente) getArguments().getSerializable(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // save views as variables in this method
        // "view" is the one returned from onCreateView
        mConversationProgress = (ProgressBar) view.findViewById(R.id.conversation_progress);
        mEmptyListConversationView = (RelativeLayout) view.findViewById(R.id.empty_list_conversation_view);
        mListConversationView = (RelativeLayout) view.findViewById(R.id.list_conversation_view);
        mListConversations = (ListView) view.findViewById(R.id.list_conversation);
        mListConversations.setOnItemClickListener(this);
        if (!ConnectionManagement.isConnected(getContext()))
            ConnectionManagement.showIsNotConected(getActivity());

        loadConversations();
        mHandler.postDelayed(mRefreshConversationsRunnable, POLL_INTERVAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mRefreshConversationsRunnable);
        mHandler.postDelayed(mRefreshConversationsRunnable, POLL_INTERVAL);
    }

    public void loadConversations() {
            if (ConnectionManagement.isConnected(getContext())) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                //Disimissing the progress dialog
                                if(isAdded())
                                    showProgress(false);

                                mAdapterConversation = new ConversationAdapter(getActivity(), new ArrayList<ChatConversation>());
                                conversations = new ArrayList<>();
                                mListConversations.setAdapter(mAdapterConversation);

                                JSONArray array;
                                boolean not = false;

                                try {
                                    array = new JSONArray(s);

                                    if (array.length() == 0) {
                                        mEmptyListConversationView.setVisibility(View.VISIBLE);
                                        mListConversationView.setVisibility(View.GONE);
                                    } else {
                                        mEmptyListConversationView.setVisibility(View.GONE);
                                        mListConversationView.setVisibility(View.VISIBLE);
                                    }

                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jObject = (JSONObject) array.get(i);

                                        ChatConversation conv = new ChatConversation();

                                        conv.setMeuId(user.getCodigoCliente());
                                        conv.setLinkMinhaFoto(user.getLinkFotoPerfilCliente());
                                        conv.setIdCliente(jObject.getLong("codigoClienteDestinatario"));
                                        conv.setNomeCliente(jObject.getString("nomeCliente"));
                                        conv.setLinkFotoCliente(jObject.getString("linkFotoCliente"));
                                        conv.setUltimaMensagem(jObject.getString("ultimaMensagem"));
                                        conv.setDataUltimaMensagem(jObject.getString("dataEnvio"));
                                        conv.setNovasMensagens(jObject.getInt("novasMensagens"));

                                        not |= (conv.getNovasMensagens() > 0);

                                        conversations.add(conv);
                                        displayConversation(conv);

                                    }

                                    MainActivity.hasNotReadNotification(not);

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
                                //Toast.makeText(getActivity(), volleyError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String, String> params = new Hashtable<>();

                        //Adding parameters
                        params.put("action", "getListConversation");
                        params.put("codigoCliente", String.valueOf(user.getCodigoCliente()));

                        //returning parameters
                        return params;
                    }
                };

                //Creating a Request Queue
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(getContext());
                }

                //Adding request to the queue
                requestQueue.add(stringRequest);
            } else {
                showProgress(false);
                if (conversations == null || conversations.size() == 0) {
                    mListConversationView.setVisibility(View.GONE);
                    mEmptyListConversationView.setVisibility(View.VISIBLE);
                }
            }

    }

    private void displayConversation(ChatConversation chatConversation) {
        mAdapterConversation.add(chatConversation);
        scroll();
    }

    private void scroll() {
        mListConversations.setSelection(0);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        mHandler.removeCallbacks(mRefreshConversationsRunnable);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListConversationView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListConversationView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListConversationView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mConversationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mConversationProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mConversationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mConversationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mListConversationView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRefreshConversationsRunnable);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent1 = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("conversation", conversations.get(i));
        mHandler.removeCallbacks(mRefreshConversationsRunnable);
        intent1.putExtras(bundle);
        intent1.setClass(getActivity(), ChatActivity.class);
        startActivity(intent1);

    }

}



