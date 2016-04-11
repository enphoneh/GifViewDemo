package gifview.aven.gifviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

	private GifView mGifView1;
	private GifView mGifView2;
	private GifView mGifView3;
	private GifView mGifView4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGifView1 = (GifView) findViewById(R.id.gifView1);
//		mGifView.setImageFromRaw(R.raw.fulitu);
//		mGifView.onAnimationShow();
		mGifView1.setImageFromNetwork("http://ww2.sinaimg.cn/mw690/6adc108fjw1ewjgu0e237g20b4060hdu.gif");

		mGifView2 = (GifView) findViewById(R.id.gifView2);
//		mGifView.setImageFromRaw(R.raw.fulitu);
//		mGifView.onAnimationShow();
		mGifView2.setImageFromNetwork("http://d.hiphotos.baidu.com/zhidao/wh%3D600%2C800/sign=d0c44a8f9113b07ebde8580e3ce7bd1b/03087bf40ad162d9d9e5afb213dfa9ec8a13cd11.jpg");

		mGifView3 = (GifView) findViewById(R.id.gifView3);
//		mGifView.setImageFromRaw(R.raw.fulitu);
//		mGifView.onAnimationShow();
		mGifView3.setImageFromNetwork("http://h.hiphotos.baidu.com/zhidao/pic/item/b7003af33a87e9507ab5cbcc12385343faf2b4eb.jpg");

		mGifView4 = (GifView) findViewById(R.id.gifView4);
//		mGifView.setImageFromRaw(R.raw.fulitu);
//		mGifView.onAnimationShow();
		mGifView4.setImageFromNetwork("http://ww2.sinaimg.cn/mw690/6adc108fjw1ewjgu0e237g20b4060hdu.gif");
	}
}
