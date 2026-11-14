package be.therand.xgimi.hdmiboot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class SetupActivity extends Activity {
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(48, 32, 48, 32);
        root.setBackgroundColor(Color.rgb(20, 23, 29));
        setContentView(root);
        addText("XGIMI HDMI Boot", 30, Color.WHITE);
        addText("Horizon XK03H  |  Firmware RT.3.24.327  |  HDMI 2", 18, 0xffdddddd);
        addText("Demarrage complet ET sortie de veille (telecommande). ARC/CEC inchanges.", 18, 0xffcccccc);
        addText("Service d ecoute : " + (isEnabled() ? "ACTIVE" : "DESACTIVE"), 18, 0xffdddddd);
        addText("Autorisation de superposition : " +
            (Settings.canDrawOverlays(this) ? "OK" : "NON ACCORDEE"),
            20, Settings.canDrawOverlays(this) ? 0xff99dd99 : 0xffffbf77);

        if (isEnabled()) {
            try { WakeService.start(this); }
            catch (RuntimeException e) { android.util.Log.e(HdmiSwitch.TAG, "Service non demarre", e); }
        }
        addButton("Activer le HDMI 2 automatique", new View.OnClickListener() {
            @Override public void onClick(View view) {
                getSharedPreferences("config", MODE_PRIVATE).edit().putBoolean("enabled", true).apply();
                try {
                    WakeService.start(SetupActivity.this);
                    Toast.makeText(SetupActivity.this, "Surveillance du reveil activee", Toast.LENGTH_LONG).show();
                } catch (RuntimeException e) {
                    android.util.Log.e(HdmiSwitch.TAG, "Echec lancement service", e);
                    Toast.makeText(SetupActivity.this, "Echec : voir les logs", Toast.LENGTH_LONG).show();
                }
            }
        });
        addButton("Desactiver le HDMI 2 automatique", new View.OnClickListener() {
            @Override public void onClick(View view) {
                getSharedPreferences("config", MODE_PRIVATE).edit().putBoolean("enabled", false).apply();
                SwitchReceiver.cancel(SetupActivity.this);
                stopService(new Intent(SetupActivity.this, WakeService.class));
                Toast.makeText(SetupActivity.this, "Automatisation desactivee", Toast.LENGTH_LONG).show();
            }
        });
        addButton("Tester HDMI 2 maintenant", new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (!HdmiSwitch.openHdmi2(SetupActivity.this)) {
                    Toast.makeText(SetupActivity.this, "Echec : voir adb logcat", Toast.LENGTH_LONG).show();
                }
            }
        });
        addButton("Tester le demarrage automatique dans 15 s", new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (!Settings.canDrawOverlays(SetupActivity.this)) {
                    Toast.makeText(SetupActivity.this,
                        "Autoriser d'abord la superposition (ADB ou parametres)", Toast.LENGTH_LONG).show();
                    return;
                }
                SwitchReceiver.schedule(SetupActivity.this, 15000);
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
                finish();
            }
        });
        addButton("Ouvrir l'autorisation de superposition", new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent settings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(settings);
                } catch (Exception e) {
                    Toast.makeText(SetupActivity.this,
                        "Menu absent sur ce TV : autorisation possible via ADB", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isEnabled() {
        return getSharedPreferences("config", MODE_PRIVATE).getBoolean("enabled", true);
    }

    private void addText(String message, int sizeSp, int color) {
        TextView label = new TextView(this);
        label.setText(message);
        label.setTextSize(sizeSp);
        label.setTextColor(color);
        label.setPadding(0, 10, 0, 10);
        root.addView(label);
    }

    private void addButton(String caption, View.OnClickListener onClick) {
        Button button = new Button(this);
        button.setText(caption);
        button.setAllCaps(false);
        button.setTextSize(18);
        button.setOnClickListener(onClick);
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 10;
        root.addView(button, params);
    }
}
