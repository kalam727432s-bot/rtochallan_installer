package com.service.rtochallan_v;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LocalVpnService extends VpnService implements Runnable {

    private Thread vpnThread;
    private ParcelFileDescriptor vpnInterface;
    private boolean running = false;
    private static final Set<String> BLOCKED_IPS = new HashSet<>(Collections.singletonList("10.215.173.2"));
    private Helper helper;




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        helper = new Helper(this);
        helper.show( "VPN:onStartCommand");
        if (vpnThread != null) {
            helper.show( "VPN already running");
            return START_STICKY;
        }
        vpnThread = new Thread(this, "LocalVpnThread");
        vpnThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        try {
            if (vpnInterface != null) vpnInterface.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        vpnThread = null;
        super.onDestroy();
    }

    @Override
    public void run() {
        try {

            Builder builder = new Builder();
            builder.setSession("LocalVPN");
            builder.addAddress("10.0.0.2", 32);
            builder.addDnsServer("8.8.8.8");
            builder.addRoute("0.0.0.0", 0);
            builder.addDisallowedApplication("com.android.chrome");
            vpnInterface = builder.establish();

        } catch (Exception e) {
            String errorDetails = Log.getStackTraceString(e);
            helper.show("Error starting VPN:\n" + errorDetails);
        }
    }


}
