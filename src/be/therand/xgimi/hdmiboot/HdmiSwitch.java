package be.therand.xgimi.hdmiboot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

final class HdmiSwitch {
    static final String TAG = "XgimiHdmiBoot";
    private static final String URI =
        "content://android.media.tv/passthrough/" +
        "com.xgimi.tvinput%2F.inputservice.Hdmi2TvInputService%2FHW2";

    private HdmiSwitch() { }

    static boolean openHdmi2(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URI));
        intent.setComponent(new ComponentName("com.xgimi.xhplayer", "com.xgimi.xhplayer.InputActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            Log.i(TAG, "Demande de commutation HDMI 2 envoyee");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Impossible de lancer HDMI 2: " + e, e);
            return false;
        }
    }
}
