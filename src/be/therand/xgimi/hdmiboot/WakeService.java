package be.therand.xgimi.hdmiboot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/** Tiny foreground service: listens to interactive state, does not poll. */
public final class WakeService extends Service {
    private static final String CHANNEL_ID = "xgimi_hdmi2_wake";
    private static final int NOTIFICATION_ID = 2202;
    private BroadcastReceiver receiver;

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID, "HDMI 2 au reveil", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Detecte la sortie de veille du videoprojecteur");
        if (nm != null) nm.createNotificationChannel(channel);
        Notification note = new Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("XGIMI HDMI 2")
            .setContentText("Commutation HDMI 2 activee au reveil")
            .setOngoing(true).build();
        startForeground(NOTIFICATION_ID, note);
        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.i(HdmiSwitch.TAG, "SCREEN_ON recu : HDMI 2 programme dans 8 secondes");
                    SwitchReceiver.schedule(context, 8000);
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.i(HdmiSwitch.TAG, "SCREEN_OFF recu : annulation des commutations en attente");
                    SwitchReceiver.cancel(context);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);
        Log.i(HdmiSwitch.TAG, "Service de veille actif : attente des evenements SCREEN_ON/OFF");
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(HdmiSwitch.TAG, "WakeService en execution");
        return START_STICKY;
    }

    @Override public void onDestroy() {
        if (receiver != null) unregisterReceiver(receiver);
        Log.i(HdmiSwitch.TAG, "WakeService arrete");
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    static void start(Context context) {
        context.startForegroundService(new Intent(context, WakeService.class));
    }
}
