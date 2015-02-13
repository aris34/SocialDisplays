package com.cosmo.socialdisplays;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gajah.inkcaseLib.InkCase;
import com.gajah.inkcaseLib.InkCaseUtils;


public class MainActivity extends Activity {

	public final String TAG = "MainActivity";
	
	public List<Drawable> icons = null;
	
	private RunningAppReceiver mReceiver = null;
	
	public static String currentAppName = null;
	
	public static MainActivity instance = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
          
        currentAppName = "ASDF";
        instance = this;
        
        initialize();
    }
	
	
 
    private void initialize() {
         
        // Start receiver with the name StartupReceiver_Manual_Start
        // Check AndroidManifest.xml file
//        getBaseContext().getApplicationContext().sendBroadcast(
//                new Intent("StartupReceiver_Manual_Start"));
    	

    	getBaseContext().getApplicationContext().sendBroadcast(
                new Intent("StartupReceiver_Manual_Start"));
    	
    }


    public void getAllICONS() {

        PackageManager pm = getPackageManager();

        ActivityManager am1 = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningTaskInfo> processes = am1
                .getRunningTasks(Integer.MAX_VALUE);

        if (processes != null) {
            for (int k = 0; k < processes.size(); k++) {
	                // String pkgName = app.getPackageName();
	                String packageName = processes.get(k).topActivity
	                        .getPackageName();
	                Log.e("packageName-->", "" + packageName);
	                Log.e("Activity:", "" + processes.get(k).topActivity.getClassName());
	                Drawable ico = null;
	                try {
	                    ApplicationInfo a = pm.getApplicationInfo(packageName,
	                            PackageManager.GET_META_DATA);
	                    ApplicationInfo b = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
	                    ico = getPackageManager().getApplicationIcon(
	                            processes.get(k).topActivity.getPackageName());
	                    getPackageManager();
	                    Log.e("ico-->", "" + ico);
	                    final String applicationName = (String) (a != null ? pm.getApplicationLabel(a) : "(unknown)");

	                    
	                    Log.e("LOGO", "" + applicationName);
	
	                } catch (NameNotFoundException e) {
	                    Log.e("ERROR", "Unable to find icon for package '"
	                            + packageName + "': " + e.getMessage());
	                }
	                // icons.put(processes.get(k).topActivity.getPackageName(),ico);
	                icons.add(ico);
            }
        }
    }
    
    public void sendToInkCase(Drawable icon, String name) {
    	
        InputStream istr;
        Bitmap bitmap = null;
        bitmap = drawableToBitmap(icon);
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(android.R.color.white));
        paint.setStyle(Paint.Style.FILL);
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 175, false);
        //bitmap = Bitmap.
        Bitmap newBitmap = Bitmap.createBitmap(300, 600, Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(newBitmap, 300, 600, paint);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(68); 
        canvas.drawText(name, 15, 440, paint);
        canvas.drawBitmap(bitmap, 75, 160, paint);
        
        if (newBitmap == null)
			throw new RuntimeException("No image to send");

        File fileToSend = new File(getExternalCacheDir(), "helloInkCase.jpg");
		try {
			FileOutputStream fOut = new FileOutputStream(fileToSend);

			newBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	
    	try {
    	    Intent sharingIntent = new Intent(InkCase.ACTION_SEND_TO_INKCASE);
    	    sharingIntent.setType("image/jpeg");
    	    sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE,InkCase.CODE_SEND_WALLPAPER);
    	    //sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE,InkCase.CODE_SEND_WALLPAPER);
    	    sharingIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(fileToSend));
    	    sharingIntent.putExtra(InkCase.EXTRA_FILENAME,fileToSend.getName());
    	    InkCaseUtils.startInkCaseActivity(this, sharingIntent);
    	 } catch (Exception e) {
    	    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    	 }
    	
	}
    
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    static public void test() {
    	
    }
}
