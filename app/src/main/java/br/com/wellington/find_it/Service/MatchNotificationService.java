package br.com.wellington.find_it.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
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

import java.util.Hashtable;
import java.util.Map;

import br.com.wellington.find_it.Bean.Cliente;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.ConnectionManagement;
import br.com.wellington.find_it.Utils.CustomNotificationManager;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;

/**
 * Classe de Service de Notificacao de Match
 *
 * @author Wellington
 * @version 1.0 - 18/01/2017.
 */
public class MatchNotificationService extends Service {

    public static int TIME_BETWEEN_REQUESTS = 420000;
    public static int SERVICE_ID = 153;
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

            getMatchListRequest();
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void getMatchListRequest() {
        if (ConnectionManagement.isConnected(getApplicationContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {

                            JSONArray array;
                            try {
                                array = new JSONArray(s);
                                if (array.length() > 0) {

                                    String[] contentNotification = new String[array.length() + 1];
                                    contentNotification[0] = getString(R.string.first_message_match_notification, array.length());
                                    for (int i = 1; i < array.length() + 1; i++) {

                                        JSONObject jObject = (JSONObject) array.get(i - 1);
                                        contentNotification[i] = getResources().getString(R.string.message_match_notification, jObject.getString("nomeItem").trim(), jObject.getString("nomeStatus"));

                                    }

                                    CustomNotificationManager.showNotification(getApplicationContext(), getResources().getString(R.string.title_match_notification), getResources().getString(R.string.simple_message_match_notification), getResources().getString(R.string.simple_message_match_notification), contentNotification.length - 1, contentNotification);

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
                            Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    //Adding parameters
                    params.put("action", "getListMatch");
                    params.put("codigoCliente", String.valueOf(user.getCodigoCliente()));

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
}