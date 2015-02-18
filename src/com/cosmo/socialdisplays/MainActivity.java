package com.cosmo.socialdisplays;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.gajah.inkcaseLib.InkCase;
import com.gajah.inkcaseLib.InkCaseUtils;


public class MainActivity extends Activity {

	public final String TAG = "MainActivity";
	public List<Drawable> icons = null;
	public static String currentAppName = null;
	private BroadcastReceiver mScreenReceiver;
	
	public static MainActivity instance = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
          
        currentAppName = "";
        instance = this;
        
        initialize();
        
        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, screenOffFilter);
        
        TextView mainTextView1 = (TextView) findViewById(R.id.main_text_view1);
        mainTextView1.setText("Use InkCaseCompanion app to connect to the InkCase display.\n");
        mainTextView1.append("Use InkCaseCompanion app to connect to the InkCase display.\n");
    }
	
 
    private void initialize() {
    	// Initicalize the broadcast receivers
    	getBaseContext().getApplicationContext().sendBroadcast(
                new Intent("StartupReceiver_Manual_Start"));
    	
    }


    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent)
        {
        	if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        	{
        		sendToInkCase("sleep.png");
        		Log.v(TAG, "In Method:  ACTION_SCREEN_OFF");
        	}
        }
    }
    
    public void sendToInkCase(String icon) {
    	
    	AssetManager assetManager = getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(icon);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }
        
        if (bitmap == null)
			throw new RuntimeException("No image to send");

        File fileToSend = new File(getExternalCacheDir(), icon);
		try {
			FileOutputStream fOut = new FileOutputStream(fileToSend);

			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	
    	try {
    	    Intent sharingIntent = new Intent(InkCase.ACTION_SEND_TO_INKCASE);
    	    sharingIntent.setType("image/jpeg");
    	    sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE,InkCase.CODE_SEND_WALLPAPER);
    	    sharingIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(fileToSend));
    	    sharingIntent.putExtra(InkCase.EXTRA_FILENAME,fileToSend.getName());
    	    sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
}
