package br.com.wellington.find_it.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;

import br.com.wellington.find_it.R;

/**
 * Classe para Gerenciar Conexão
 *
 * @author Wellington
 * @version 1.0 - 02/01/2017.
 */
public class ConnectionManagement {


        private static final String PREFS_PRIVATE = "PREFS_PRIVATE";

        public static boolean isConnected(Context context) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }



        public static void showIsNotConected(Activity activity){
            Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.no_internet_connectivity), Snackbar.LENGTH_LONG).show();
        }

        public static boolean isPasswordSave(Context context){

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
            String senha = sharedPreferences.getString("passwordFindIt","");

            return senha.length() != 0;
        }

        public static String getPasswordSave(Context context){
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
            return sharedPreferences.getString("passwordFindIt","");
        }

        public static String getLoginSave(Context context){
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
            return sharedPreferences.getString("loginFindIt","");
        }

        public static void savePreferences( String login, String senha, Context context) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);

            SharedPreferences.Editor prefsPrivateEditor = sharedPreferences.edit();

            prefsPrivateEditor.putString("loginFindIt", login);
            prefsPrivateEditor.putString("passwordFindIt", senha);

            prefsPrivateEditor.apply();

        }


}
