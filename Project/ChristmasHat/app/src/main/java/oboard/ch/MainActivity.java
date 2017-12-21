package oboard.ch;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	ImageView iv;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }


	public void news(View v) {
		//来自图库的图
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, 1);
		iv = (ImageView)v;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
				//接收图库的图
                try {
					iv.setImageBitmap(BitmapFactory.decodeStream(cr.openInputStream(uri)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	/** * 将图片存到本地 */
	private static Uri saveBitmap(Bitmap bm, String picName) {
		try {
			String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/zdrawbook/" + picName + ".png";    
			File f = new File(dir);        
			if (!f.exists()) {
				f.getParentFile().mkdirs();          
				f.createNewFile();     
			}
			FileOutputStream out = new FileOutputStream(f);            
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);       
			out.flush();      
			out.close();      
			Uri uri = Uri.fromFile(f);    
			return uri;  
		} catch (FileNotFoundException e) {
            e.printStackTrace();  
		} catch (IOException e) {
			e.printStackTrace();   
		}
		return null;
	}
}
