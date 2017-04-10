package br.com.wellington.find_it.Activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import net.mobindustry.emojilib.DpCalculator;
import net.mobindustry.emojilib.EmojiParser;
import net.mobindustry.emojilib.ObservableLinearLayout;
import net.mobindustry.emojilib.Utils;
import net.mobindustry.emojilib.emoji.Emoji;
import net.mobindustry.emojilib.emoji.EmojiKeyboardView;
import net.mobindustry.emojilib.emoji.EmojiPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import br.com.wellington.find_it.Adapter.ChatAdapter;
import br.com.wellington.find_it.Bean.ChatConversation;
import br.com.wellington.find_it.Bean.ChatMessage;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.ConnectionManagement;
import de.hdodenhof.circleimageview.CircleImageView;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;

public class ChatActivity extends AppCompatActivity {

    // UI Elements
    private EditText mMessageChatEditText;
    private ImageView mIconOnlineChat;
    private TextView mStatusChat;
    private ListView mListMessages;
    private ImageButton mSendEmojiButton;
    private AlphaAnimation mButtonClick = new AlphaAnimation(1F, 0.8F);
    private ChatAdapter adapter;
    private Emoji mEmoji;
    @Nullable
    private EmojiPopup mEmojiPopup;
    private EmojiParser mParser;
    private View.OnClickListener listener;


    // Others Elements
    public static ChatConversation conversation;
    private ArrayList<ChatMessage> chatHistory;
    boolean todayHeader;
    boolean yesterdayHeader;
    String lastDate;
    private static final int POLL_INTERVAL = 1000;
    private RequestQueue requestQueue;
    public static boolean active = false;
    Map<String, String> params = new Hashtable<>();
    private Handler mHandler = new Handler();

    private Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            if (chatHistory != null)
                loadMessages(chatHistory.size());
            mHandler.postDelayed(this, POLL_INTERVAL);

        }

    };

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && chatHistory != null) {
            chatHistory = (ArrayList<ChatMessage>) savedInstanceState.getSerializable("chatHistory");
            assert chatHistory != null;
            for (int i = 0; i < chatHistory.size(); i++)
                displayMessage(chatHistory.get(i));
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                conversation = (ChatConversation) extras.getSerializable("conversation");
                
                addVisualizationMessages();
                addVisualizationMatch();

            }
        }
        setContentView(R.layout.activity_chat);
        initControls();
    }

    private void initControls() {

        mListMessages = (ListView) findViewById(R.id.list_messages);
        mMessageChatEditText = (EditText) findViewById(R.id.message_chat_edit_text);
        ImageButton mSendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        mSendEmojiButton = (ImageButton) findViewById(R.id.send_emoji_button);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            CircleImageView mProfileImageChat = (CircleImageView) toolbar.findViewById(R.id.profile_image_chat);
            mIconOnlineChat = (ImageView) toolbar.findViewById(R.id.icon_online_chat);
            TextView mNameChat = (TextView) toolbar.findViewById(R.id.name_chat);
            mStatusChat = (TextView) toolbar.findViewById(R.id.status_chat);

            mNameChat.setText(conversation.getNomeCliente());
            if (conversation.getLinkFotoCliente() != null && !conversation.getLinkFotoCliente().equals(""))
                Picasso.with(getApplicationContext()).load(conversation.getLinkFotoCliente()).noFade().placeholder(R.drawable.profile).into(mProfileImageChat);
        }

        if (chatHistory == null)
            loadDummyHistory();

        mHandler.postDelayed(mRefreshMessagesRunnable, POLL_INTERVAL);

        final ObservableLinearLayout mObservableLayout = (ObservableLinearLayout) findViewById(R.id.observable_layout);

        Emoji.EmojiCallback mCallback = new Emoji.EmojiCallback() {
            @Override
            public void loaded() {
                mSendEmojiButton.setOnClickListener(listener);
            }
        };

        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(mButtonClick);
                if (mEmojiPopup != null) {
                    dissmissEmojiPopup();
                } else {
                    mSendEmojiButton.setImageResource(R.drawable.ic_emoji_focus);
                    mEmojiPopup = EmojiPopup.create(ChatActivity.this, mObservableLayout, new EmojiKeyboardView.CallBack() {
                        @Override
                        public void backspaceClicked() {
                            mMessageChatEditText.dispatchKeyEvent(new KeyEvent(0, KeyEvent.KEYCODE_DEL));
                        }

                        @Override
                        public void emojiClicked(long code) {
                            String strEmoji = Emoji.toString(code);
                            Editable text = mMessageChatEditText.getText();
                            text.append(mEmoji.replaceEmoji(strEmoji));
                        }

                        @Override
                        public void stickerCLicked(String stickerFilePath) {
                            //stickerResultView.setImageURI(Uri.parse(stickerFilePath));
                        }
                    });
                    assert mEmojiPopup != null;
                    mEmojiPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            mEmojiPopup = null;
                        }
                    });
                    assert mEmojiPopup != null;
                }
            }
        };
        makeEmoji(mCallback);
        mParser = new EmojiParser(mEmoji);

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(View v) {
                Spannable messageText = mParser.parse(mMessageChatEditText.getText().toString());
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setName("Você");
                chatMessage.setId(conversation.getMeuId());//dummy
                chatMessage.setMessage(messageText);
                chatMessage.setLinkFotoPerfil(conversation.getLinkMinhaFoto());
                chatMessage.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                chatMessage.setMe(true);

                postMessage(chatMessage);
                dissmissEmojiPopup();
            }
        });

        mMessageChatEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                addVisualizationMessages();
                scroll();
            }
        });

    }

    private void addVisualizationMessages() {
        if (ConnectionManagement.isConnected(getApplicationContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            //Dismissing the progress dialog and show Toast
                            Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    //Adding parameters
                    params.put("action", "addVisualizationMessages");
                    params.put("codigoClienteRemetente", String.valueOf(conversation.getMeuId()));
                    params.put("codigoClienteDestinatario", String.valueOf(conversation.getIdCliente()));

                    //returning parameters
                    return params;
                }
            };

            //Creating a Request Queue
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            //Adding request to the queue
            requestQueue.add(stringRequest);
        }
    }

    private void addVisualizationMatch() {
        if (ConnectionManagement.isConnected(getApplicationContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            //Dismissing the progress dialog and show Toast
                            Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    //Adding parameters
                    params.put("action", "addVisualizationMatch");
                    params.put("codigoClienteRemetente", String.valueOf(conversation.getMeuId()));
                    params.put("codigoClienteDestinatario", String.valueOf(conversation.getIdCliente()));

                    //returning parameters
                    return params;
                }
            };

            //Creating a Request Queue
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            //Adding request to the queue
            requestQueue.add(stringRequest);
        }
    }


    private void postMessage(final ChatMessage chatMessage) {
        if (ConnectionManagement.isConnected(getApplicationContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            chatHistory.add(chatMessage);
                            displayMessage(chatMessage);
                            mMessageChatEditText.setText("");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            //Dismissing the progress dialog and show Toast
                            Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    //Adding parameters
                    params.put("action", "postMessage");
                    params.put("codigoClienteRemetente", String.valueOf(conversation.getMeuId()));
                    params.put("codigoClienteDestinatario", String.valueOf(conversation.getIdCliente()));
                    params.put("conteudoMensagem", chatMessage.getMessage().toString());

                    //returning parameters
                    return params;
                }
            };

            //Creating a Request Queue
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            //Adding request to the queue
            requestQueue.add(stringRequest);
        } else {
            ConnectionManagement.showIsNotConected(ChatActivity.this);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public void displayMessage(ChatMessage message) {
        try {
            addHeaderView(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(message.getDate()));
            adapter.addItem(message);
            scroll();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void loadMessages(final int numMensagens) {
        if (ConnectionManagement.isConnected(getApplicationContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                    new Response.Listener<String>() {
                        @SuppressLint("SimpleDateFormat")
                        @Override
                        public void onResponse(String s) {

                            JSONObject jArray;
                            try {
                                jArray = new JSONObject(s);
                                JSONObject jObject;
                                ChatMessage msg;

                                JSONArray jsonArrayMessages = jArray.getJSONArray("messages");
                                String status = jArray.getString("lastActivityOfUser");

                                if (status.equals("Online")) {
                                    mStatusChat.setText(status);
                                    mIconOnlineChat.setVisibility(View.VISIBLE);
                                } else {
                                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(status);
                                    status = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
                                    mStatusChat.setText(getString(R.string.not_online_status, status));
                                    mIconOnlineChat.setVisibility(View.GONE);
                                }

                                for (int i = 0; i < jsonArrayMessages.length(); i++) {

                                    jObject = (JSONObject) jsonArrayMessages.get(i);
                                    msg = new ChatMessage();


                                    long codigoRemetente = jObject.getLong("codigoClienteRemetente");
                                    if (numMensagens == 0 || codigoRemetente != conversation.getMeuId()) {
                                        msg.setDate(jObject.getString("dataEnvio"));
                                        msg.setMessage(mParser.parse(jObject.getString("conteudoMensagem")));
                                        boolean test = (codigoRemetente == conversation.getMeuId());
                                        msg.setName(test ? "Você" : conversation.getNomeCliente());
                                        msg.setMe(test);
                                        msg.setLinkFotoPerfil(test ? conversation.getLinkMinhaFoto() : conversation.getLinkFotoCliente());
                                        msg.setId(test ? conversation.getMeuId() : conversation.getIdCliente());

                                        chatHistory.add(msg);
                                        displayMessage(msg);
                                    }

                                }

                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            //Dismissing the progress dialog and show Toast
                            // Toast.makeText(getApplicationContext(), volleyError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    //Adding parameters
                    params.put("action", "getMessages");
                    params.put("codigoClienteRemetente", String.valueOf(conversation.getMeuId()));
                    params.put("codigoClienteDestinatario", String.valueOf(conversation.getIdCliente()));
                    params.put("contadorMensagens", String.valueOf(numMensagens));

                    //returning parameters
                    return params;
                }
            };

            //Creating a Request Queue
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }

            //Adding request to the queue
            requestQueue.add(stringRequest);
        }
    }

    private void scroll() {
        mListMessages.setSelection(mListMessages.getCount() - 1);
    }

    private void loadDummyHistory() {
        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        mListMessages.setAdapter(adapter);
        chatHistory = new ArrayList<>();
        lastDate = "";
        if (!ConnectionManagement.isConnected(getApplicationContext()))
            ConnectionManagement.showIsNotConected(ChatActivity.this);
        loadMessages(chatHistory.size());
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SimpleDateFormat")
    private void addHeaderView(Date lastMessage) {
        Calendar cal = Calendar.getInstance();
        String strDate;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            strDate = DateFormat.getDateInstance(DateFormat.LONG, getResources().getConfiguration().getLocales().get(0)).format(lastMessage);
        } else {
            strDate = DateFormat.getDateInstance(DateFormat.LONG, getResources().getConfiguration().locale).format(lastMessage);
        }
        Date c_date = cal.getTime();
        ChatMessage headerMsg = new ChatMessage();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        if (fmt.format(c_date).equals(fmt.format(lastMessage))) {
            if (!todayHeader) {
                headerMsg.setDate("Hoje");
                adapter.addSectionHeaderItem(headerMsg);
                todayHeader = true;
            }
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            c_date = cal.getTime();
            if (fmt.format(c_date).equals(fmt.format(lastMessage))) {
                if (!yesterdayHeader) {
                    headerMsg.setDate("Ontem");
                    adapter.addSectionHeaderItem(headerMsg);
                    yesterdayHeader = true;
                }
            } else {
                if (!lastDate.equals(strDate)) {
                    headerMsg.setDate(strDate);
                    adapter.addSectionHeaderItem(headerMsg);
                    lastDate = strDate;
                }
            }
        }
    }

    private void makeEmoji(Emoji.EmojiCallback callback) {
        mEmoji = new Emoji(this, new DpCalculator(Utils.getDensity(this.getResources())));
        mEmoji.makeEmoji(callback);
    }

    public boolean dissmissEmojiPopup() {
        if (mEmojiPopup == null) {
            return false;
        }
        mSendEmojiButton.setImageResource(R.drawable.ic_emoji_default);
        mEmojiPopup.dismiss();
        mEmojiPopup = null;
        Utils.hideKeyboard(mMessageChatEditText);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home/back button
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isEmojiAttached() {
        return mEmojiPopup != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mRefreshMessagesRunnable);
        mHandler.postDelayed(mRefreshMessagesRunnable, POLL_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRefreshMessagesRunnable);
        dissmissEmojiPopup();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mRefreshMessagesRunnable);
        active = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRefreshMessagesRunnable);
        active = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mHandler.removeCallbacks(mRefreshMessagesRunnable);
        savedInstanceState.putSerializable("chatHistory", chatHistory);
    }

    @Override
    public void onBackPressed() {
        if (isEmojiAttached()) {
            dissmissEmojiPopup();
        } else {
            mHandler.removeCallbacks(mRefreshMessagesRunnable);
            super.onBackPressed();
        }
    }

}

