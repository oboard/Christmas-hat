package oboard.ch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	FrameLayout r;
	LinearLayout t;
	ImageView iv;
	MultiTouchView s;
    TextView l;
    Bitmap sb;

	List<Integer> hats = new ArrayList<Integer>();
	int hat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		s = new MultiTouchView(this, R.drawable.i);
		s.setVisibility(View.GONE);
		iv = (ImageView)findViewById(R.id.main_image);
		r = (FrameLayout)findViewById(R.id.main_frame);
		t = (LinearLayout)findViewById(R.id.main_tool);

		r.addView(s);

		//添加帽子到列表
		hats.add(R.drawable.i);
		hats.add(R.drawable.i1);
		hats.add(R.drawable.i2);
		hats.add(R.drawable.i3);
		hats.add(R.drawable.i4);
		hats.add(R.drawable.i5);
		hats.add(R.drawable.i6);
		hats.add(R.drawable.i7);
		hats.add(R.drawable.i8);
		hats.add(R.drawable.i9);
		hats.add(R.drawable.i10);
		hats.add(R.drawable.i11);
    }


	public void news(View v) {
		//来自图库的图
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, 1);
        l = (TextView)v;
	}

	public void onX(View v) {
		//切换帽子
		hat++;
		if (hat > 11)
			hat = 0;
		((ImageButton)v).setImageResource(hats.get(hat));
		s.setImageResource(hats.get(hat));
	}

	public void onClick(View v) {
        Bitmap bm = getWebDrawing();
		//保存图
		Uri u = saveBitmap(bm, Long.toHexString(System.currentTimeMillis()));

		//分享图
		if (v.getId() == R.id.main_share) {
			shareMsg(getTitle().toString(), "head", u);
		} else {

            final ImageView i = new ImageView(this);
            i.setImageBitmap(bm);

            new AlertDialog.Builder(this)
                .setView(i)
                .setMessage("已保存到 " + u.toString())
                .setPositiveButton("知道了", null)
                .show();

        }
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

        Bitmap bitmap = Bitmap.createBitmap((int)mapState.width(), (int)mapState.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //绘制原图
        canvas.drawBitmap(sb,
        new Rect(0, 0, (int)sb.getWidth(), (int)sb.getHeight()),
        new Rect(0, 0, (int)mapState.width(), (int)mapState.height()),
        null);
        //消除图像锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));  
        canvas.save();
        //绘制帽子
        s.mMatrix.postTranslate(-mapState.left, -mapState.top);
        canvas.drawBitmap(s.mBitmap, s.mMatrix, null);
        s.mMatrix.setTranslate(0.0f, 0.0f);
		canvas.restore();

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
                    //存入Bitmap然后显示到Imageview！
                    sb = BitmapFactory.decodeStream(cr.openInputStream(uri));
					iv.setImageBitmap(sb);

					//开始编辑！（圣诞帽出来。。）
					t.setVisibility(View.VISIBLE);
					s.setVisibility(View.VISIBLE);
                    l.setVisibility(View.GONE);
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
