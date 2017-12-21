package oboard.ch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.widget.Spinner;
import android.widget.SimpleAdapter;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;

public class MainActivity extends Activity {

	FrameLayout r;
	LinearLayout t;
	ImageView iv;
	MultiTouchView s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		s = new MultiTouchView(this, R.drawable.s1);
		s.setVisibility(View.GONE);
		iv = (ImageView)findViewById(R.id.main_image);
		r = (FrameLayout)findViewById(R.id.main_frame);
		t = (LinearLayout)findViewById(R.id.main_tool);
		
		r.addView(s);
    }


	public void news(View v) {
		//来自图库的图
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, 1);
	}

	public void onClick(View v) {
		//保存图
		Uri u = saveBitmap(getWebDrawing(), Long.toHexString(System.currentTimeMillis()));

		//分享图
		if (v.getId() == R.id.main_share)
			shareMsg(getTitle().toString(), "head", u);
		else
			new AlertDialog.Builder(this).setMessage("已保存到 " + u.toString()).setPositiveButton("知道了", null).show();
	}

	public void onAbout(View v) {
		Toast.makeText(this, "作者：一块小板子   QQ：2232442466", Toast.LENGTH_LONG).show();
	}
		
	public void shareMsg(String msgTitle, String msgText, Uri imguri) {
		Intent intent = new Intent(Intent.ACTION_SEND); //设置分享行为
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
		intent.putExtra(Intent.EXTRA_TEXT, msgText);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Intent.EXTRA_STREAM, imguri);

		startActivity(Intent.createChooser(intent, msgTitle));
	}

	public Bitmap getWebDrawing() {

		//获得图像在ImageView的位置
		//!!!!!!

		Matrix matrix = iv.getImageMatrix();
		Rect rect = iv.getDrawable().getBounds();
		float[] values = new float[9];
		matrix.getValues(values);
		RectF mapState = new RectF();
		mapState.left = values[2];
		mapState.top = values[5];
		mapState.right = mapState.left + rect.width() * values[0];
		mapState.bottom = mapState.top + rect.height() * values[0];

		r.setDrawingCacheEnabled(true);
        r.buildDrawingCache();  //启用DrawingCache并创建位图 
		//创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收 
		//顺便裁剪
        Bitmap bitmap = Bitmap.createBitmap(r.getDrawingCache(), 
											(int)mapState.left, (int)mapState.top,
											(int)mapState.width(), (int)mapState.height()); 
        r.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能  

        return bitmap;
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

					//开始编辑！（圣诞帽出来。。）
					t.setVisibility(View.VISIBLE);
					s.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	/** * 将图片存到本地 */
	private Uri saveBitmap(Bitmap bm, String picName) {
		try {
			String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/" + picName + ".png";
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
