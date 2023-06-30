package com.example.myapplication;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyBroadcastReceiver extends BroadcastReceiver {

    public interface NetworkStateListener {
        void onNetworkStateChange(boolean isWifiConnected, boolean isCellularConnected);
    }

    public interface WifiStateListener {
        void onWifiStateChange(boolean isWifiOn);
    }

    private NetworkStateListener networkListener;
    private WifiStateListener wifiListener;

    public MyBroadcastReceiver(NetworkStateListener networkListener, WifiStateListener wifiListener) {
        this.networkListener = networkListener;
        this.wifiListener = wifiListener;
    }

    public MyBroadcastReceiver(NetworkStateListener networkListener) {
        this.networkListener = networkListener;
    }

    public MyBroadcastReceiver(WifiStateListener wifiListener) {
        this.wifiListener = wifiListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();

        boolean isWifiConnected = false;
        boolean isCellularConnected = false;

        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConnected = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                isCellularConnected = true;
            }
        }

        int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        switch (wifiStateExtra) {
            case WifiManager.WIFI_STATE_ENABLED:
                if (wifiListener != null) {
                    wifiListener.onWifiStateChange(true);
                }
                showNotification(context, "WIFI פועל", "WIFI is ON");
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                if (wifiListener != null) {
                    wifiListener.onWifiStateChange(false);
                }
                showNotification(context, "WIFI אינו פועל", "WIFI is OFF");
                break;
        }

        if (networkListener != null) {
            networkListener.onNetworkStateChange(isWifiConnected, isCellularConnected);
        }
    }

    private void showNotification(Context context, String title, String content) {
        String channelId = "MY_CHANNEL_ID7";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_add_24)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true);

        Intent activityIntent = new Intent(context, DesignSportActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "My Notification Channel";
            String channelDescription = "Channel for handling all notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, builder.build());
    }
}
