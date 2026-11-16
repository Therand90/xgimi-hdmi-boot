package be.therand.xgimi.hdmiboot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

public final class SwitchReceiver extends BroadcastReceiver {
    private static final int REQUEST_CODE = 2;

    static void schedule(Context context, long delayMs) {
        scheduleAttempt(context, delayMs, false);
    }

    private static void scheduleAttempt(Context context, long delayMs, boolean retry) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            Log.e(HdmiSwitch.TAG, "AlarmManager introuvable");
            return;
        }
        Intent intent = new Intent(context, SwitchReceiver.class);
        intent.putExtra("retry", retry);
        PendingIntent alarm = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + delayMs, alarm);
        Log.i(HdmiSwitch.TAG, (retry ? "Seconde tentative HDMI 2" : "Commutation HDMI 2")
            + " programmee dans " + delayMs + " ms");
    }

    static void cancel(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        Intent intent = new Intent(context, SwitchReceiver.class);
        PendingIntent alarm = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE);
        if (alarm != null) {
            manager.cancel(alarm);
            alarm.cancel();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!context.getSharedPreferences("config", Context.MODE_PRIVATE)
                .getBoolean("enabled", true)) {
            Log.i(HdmiSwitch.TAG, "Commutation ignoree (desactivee)");
            return;
        }
        if (!Settings.canDrawOverlays(context)) {
            Log.e(HdmiSwitch.TAG,
                "Acces superposition non autorise; Android peut bloquer l'ouverture depuis le fond");
            return;
        }
        boolean success = HdmiSwitch.openHdmi2(context);
        if (!success && !intent.getBooleanExtra("retry", false)) {
            // A single recovery attempt, preserving the previously tested
            // 4s wake / 6s boot total if early activity launch throws.
            Log.w(HdmiSwitch.TAG, "Ouverture HDMI 2 echouee : nouvelle tentative dans 3 s");
            scheduleAttempt(context, 3000, true);
        }
    }
}
