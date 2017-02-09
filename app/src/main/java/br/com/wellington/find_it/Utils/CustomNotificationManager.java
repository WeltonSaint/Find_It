package br.com.wellington.find_it.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import br.com.wellington.find_it.Activity.ChatActivity;
import br.com.wellington.find_it.Activity.MainActivity;
import br.com.wellington.find_it.Bean.ChatConversation;
import br.com.wellington.find_it.R;

/**
 * Classe de Gerenciamento de Notificação
 *
 * @author Wellington
 * @version 1.0 - 11/01/2017.
 */
public class CustomNotificationManager {

    private static long[] patternVibrate = new long[]{150, 300, 150, 600};

    public static void showNotification(Context context, String title, String content, String ticker, int number, String[] fullContent) {
        NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navItemIndex",4);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setTicker(ticker);
        notification.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        notification.setContentTitle(title);
        notification.setContentText(content);
        notification.setSmallIcon(R.drawable.ic_stat_logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.setPriority(Notification.PRIORITY_HIGH);
        }
        notification.setAutoCancel(true);
        notification.setContentIntent(contentIntent);
        notification.setLights(0xff00ff00, 1000, 700);
        notification.setVibrate(patternVibrate);
        notification.setNumber(number);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.setSound(uri);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (String aFullContent : fullContent) {
            style.addLine(aFullContent);
        }
        notification.setStyle(style);

        Notification n = notification.build();
        n.vibrate = patternVibrate;
        n.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNM.notify(R.string.app_name, n);

    }

    public static void showNotification(final Context context, String title, String content, String ticker, int number, String[] fullContent, String action, String pictureUrl, ChatConversation conv) {
        System.out.println("Chamada de notificação");
        //NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        Intent intent = new Intent(context, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("conversation", conv);
        intent.putExtras(bundle);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setTicker(ticker);
        notification.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        notification.setContentTitle(title);
        notification.setContentText(content);
        notification.setSmallIcon(R.drawable.ic_stat_logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.setPriority(Notification.PRIORITY_HIGH);
        }
        notification.setAutoCancel(true);
        notification.addAction(R.drawable.ic_comment, action, contentIntent);

        Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                notification.setLargeIcon(bitmap);
                notification.setDefaults(Notification.DEFAULT_ALL);
                // send the notification again to update it w/ the right image
                Notification n = notification.build();
                n.vibrate = patternVibrate;
                n.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
                ((NotificationManager) (context.getSystemService(Context.NOTIFICATION_SERVICE)))
                        .notify(R.string.app_name, n);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }

        };

        Picasso.with(context).load(pictureUrl).transform(new CircleTransform()).placeholder(R.drawable.profile).into(mTarget);
        notification.setContentIntent(contentIntent);
        notification.setLights(0xff00ff00, 1000, 700);
        notification.setVibrate(patternVibrate);
        notification.setNumber(number);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.setSound(uri);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (String aFullContent : fullContent) {
            style.addLine(aFullContent);
        }
        notification.setStyle(style);

    }

}
