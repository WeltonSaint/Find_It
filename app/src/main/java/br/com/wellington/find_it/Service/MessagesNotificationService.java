package br.com.wellington.find_it.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;

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
import br.com.wellington.find_it.Bean.ChatConversation;
import br.com.wellington.find_it.Bean.Cliente;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.ConnectionManagement;
import br.com.wellington.find_it.Utils.CustomNotificationManager;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;

/**
 * Classe de Service de Notificacao de Mensagens
 *
 * @author Wellington
 * @version 1.0 - 18/01/2017.
 */
public class MessagesNotificationService extends Service {

    public static int TIME_BETWEEN_REQUESTS = 5000;
    public static int SERVICE_ID = 142;
    private RequestQueue requestQueue;
    private static Cliente user;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        if (intent != null) {

            byte[] byteArrayExtra = intent.getByteArrayExtra("user");
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(byteArrayExtra, 0, byteArrayExtra.length);
            parcel.setDataPosition(0);
            user = Cliente.CREATOR.createFromParcel(parcel);

            getMessagesRequest();
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void getMessagesRequest() {
        if (ConnectionManagement.isConnected(getApplicationContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {

                            JSONArray array;
                            try {
                                array = new JSONArray(s);
                                ArrayList<String> contentNotification = new ArrayList<>();

                                if (array.length() > 1) {
                                    int newsMessages = 0;
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jObject = (JSONObject) array.get(i);
                                        JSONArray mensagens = jObject.getJSONArray("mensagens");
                                        newsMessages += mensagens.length();
                                        for (int j = 0; j < mensagens.length(); j++) {
                                            contentNotification.add(jObject.getString("nomeCliente") + ": " + mensagens.getString(j));
                                        }
                                    }

                                    String[] contentNotificationArray = new String[contentNotification.size()];
                                    contentNotificationArray = contentNotification.toArray(contentNotificationArray);


                                    CustomNotificationManager.showNotification(getApplicationContext(), getResources().getString(R.string.title_notification_more_conversations, contentNotificationArray.length, newsMessages), getResources().getString(R.string.message_notification_more_conversations), getResources().getString(R.string.message_notification_more_conversations), contentNotificationArray.length, contentNotificationArray);

                                } else if (array.length() == 1) {
                                    JSONObject jObject = (JSONObject) array.get(0);
                                    JSONArray mensagens = jObject.getJSONArray("mensagens");
                                    for (int j = 0; j < mensagens.length(); j++) {
                                        contentNotification.add(mensagens.getString(j));
                                    }

                                    ChatConversation conv = new ChatConversation();

                                    conv.setMeuId(user.getCodigoCliente());
                                    conv.setLinkMinhaFoto(user.getLinkFotoPerfilCliente());
                                    conv.setIdCliente(jObject.getLong("codigoClienteDestinatario"));
                                    conv.setNomeCliente(jObject.getString("nomeCliente"));
                                    conv.setLinkFotoCliente(jObject.getString("linkFotoCliente"));
                                    conv.setUltimaMensagem(jObject.getString("ultimaMensagem"));
                                    conv.setDataUltimaMensagem(jObject.getString("dataEnvio"));
                                    conv.setNovasMensagens(jObject.getInt("novasMensagens"));

                                    String[] contentNotificationArray = new String[contentNotification.size()];
                                    contentNotificationArray = contentNotification.toArray(contentNotificationArray);

                                    if (!ChatActivity.active || (ChatActivity.active && ChatActivity.conversation.getIdCliente() != conv.getIdCliente()))
                                        CustomNotificationManager.showNotification(getApplicationContext(), jObject.getString("nomeCliente"), getResources().getString(R.string.message_notification, mensagens.length()), getResources().getString(R.string.message_notification, mensagens.length()), contentNotificationArray.length, contentNotificationArray, getResources().getString(R.string.action_notification_message), jObject.getString("linkFotoCliente"), conv);

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            // show Toast
                            //Toast.makeText(getApplicationContext(), volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                            //Log.e("MSG", volleyError.getMessage().toString());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    //Adding parameters
                    params.put("action", "getNewMessages");
                    params.put("codigoCliente", String.valueOf(user.getCodigoCliente()));

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


}
