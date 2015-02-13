package com.cosmo.socialdisplays;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
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
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.gajah.inkcaseLib.InkCase;
import com.gajah.inkcaseLib.InkCaseUtils;

public class RunningAppReceiver extends BroadcastReceiver {
	 
    public final String TAG = "CheckRunningApplicationReceiver"; 
    Context mContext;
    MainActivity mActivity = null;
    Drawable icon = null;
    String applicationName;
    static String packageName;
    
    @Override
    public void onReceive(Context mContext, Intent anIntent) {
    	this.mContext = mContext;

    	int notificationId = anIntent.getIntExtra("notificationId", 0);
    	MainActivity mActivity = MainActivity.instance;
    	packageName = mActivity.currentAppName;
//    	NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//    	manager.cancel(notificationId);
    	
//    	MainActivity mActivity = (MainActivity) mContext;
//    	packageName = mActivity.currentAppName;
    	Log.i("Intent: ", "" + anIntent);
    	Log.i("packageName: ", "" + packageName);
    	
    	try {
    		PackageManager pm = mContext.getPackageManager();
    		
    		ActivityManager am1 = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> processes = am1.getRunningTasks(Integer.MAX_VALUE);

            if (processes != null) {
            
            	icon = null;
            	applicationName = null;
            	packageName = processes.get(0).topActivity
                        .getPackageName();
            	Log.i(TAG, "packageName: " + packageName);
            	Log.i(TAG, "mActivity.currentAppName: " + mActivity.currentAppName);
            	
            	//if(packageName != mActivity.currentAppName) 
            	if ( (!packageName.equals(mActivity.currentAppName)) && 
            			(!packageName.equals("com.gajah.inkcase.companion")) &&
            			(!packageName.contains("launcher")) ){
            		
	                Log.e("packageName-->", "" + packageName);
	                Log.e("Activity:", "" + processes.get(0).topActivity.getClassName());
	                
	                try {
	                    ApplicationInfo a = pm.getApplicationInfo(packageName,
	                            PackageManager.GET_META_DATA);
	                    ApplicationInfo b = mContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
	                    icon = mContext.getPackageManager().getApplicationIcon(
	                            processes.get(0).topActivity.getPackageName());
	                    Log.e("ico-->", "" + icon);
	                    String applicationName = (String) (a != null ? pm.getApplicationLabel(a) : "(unknown)");
	                    
	                    
	                    Log.e("LOGO", "" + applicationName);
	
	                    sendToInkCase(icon, applicationName);
	                } catch (NameNotFoundException e) {
	                    Log.e("ERROR", "Unable to find icon for package '"
	                            + packageName + "': " + e.getMessage());
	                }
	                mActivity.currentAppName = packageName;
            	}
            }
 
        } catch (Throwable t) {
            Log.i(TAG, "Throwable caught: "
                        + t.getMessage(), t);
        }
    	
         
    }
    
    public void sendToInkCase(Drawable icon, String name) {
    	String newName = addLinebreaks(name, 9);
    	
    	String[] parts = newName.split("\n");
    	
    	Log.i(TAG, newName);
        InputStream istr;
        Bitmap bitmap = null;
        bitmap = drawableToBitmap(icon);
        Paint paint = new Paint();
        paint.setColor(mContext.getResources().getColor(android.R.color.white));
        paint.setStyle(Paint.Style.FILL);
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 175, false);
        //bitmap = Bitmap.
        Bitmap newBitmap = Bitmap.createBitmap(300, 600, Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(newBitmap, 300, 600, paint);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(60); 
        
        Typeface mono = Typeface.createFromAsset(mContext.getAssets(), "VeraMono.ttf");
        paint.setTypeface(mono);
        
        int counter = 0;
        for(String part : parts) {
    		canvas.drawText(part, 10, 250 + counter*65, paint);
    		counter++;
        }
        canvas.drawBitmap(bitmap, 75, 10, paint);
        
       
        
        if (newBitmap == null)
			throw new RuntimeException("No image to send");

        File fileToSend = new File(mContext.getExternalCacheDir(), "helloInkCase.jpg");
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
    	    sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    InkCaseUtils.startInkCaseActivity(mContext, sharingIntent);
    	 } catch (Exception e) {
    	    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    
    public String addLinebreaks(String input, int maxLineLength) {
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            if (lineLen + word.length() > maxLineLength) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word);
            lineLen += word.length();
        }
        return output.toString();
    }
}
