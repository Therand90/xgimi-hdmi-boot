package be.therand.xgimi.hdmiboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(HdmiSwitch.TAG, "BOOT_COMPLETED recu");
            if (!context.getSharedPreferences("config", Context.MODE_PRIVATE)
                    .getBoolean("enabled", true)) {
                Log.i(HdmiSwitch.TAG, "Automatisation desactivee");
                return;
            }
            try {
                WakeService.start(context);
                SwitchReceiver.schedule(context, 6000);
            } catch (RuntimeException e) {
                Log.e(HdmiSwitch.TAG, "Echec demarrage du service au boot", e);
            }
        }
    }
}
