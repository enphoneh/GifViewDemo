package gifview.aven.gifviewdemo;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

/**
 * <br>功能简述:gifview
 * <br>功能详细描述:用于播放gif
 * <br>注意:
 * @author Aven
 **/
public class GifView extends ImageView implements Handler.Callback {

	public static final String TAG = "gifView";
	public static boolean DEBUG = false;

	private final static int SHOW_TYPE_FIRSTFRAME = 0;
	private final static int SHOW_TYPE_ANIMAITON = 1;
	private final static long MIN_GIF_FRAME_DELAY = 33; //绘图的最小时间间隔，根据每秒30帧算得

	private final static int DEFAULT_WIDTH = -1;
	private final static int DEFAULT_HEIGHT = -1;

	private GifDecoder mGifDecoder = null;
	private boolean mIsRunning = true;
	private boolean mIsPause = false;
	private int mShowType = SHOW_TYPE_FIRSTFRAME;
	private Bitmap mCurrentBitmp;
	private boolean isDrawThreadDestory = true;
	private LruCache<Integer, Bitmap> mMemoryCache;
	private Handler mUiHandler = null;
	private int mWidth;
	private int mHeight;
	private OnlineGifLoader mGifLoader;

	public GifView(Context context) {
		super(context);
		init(null, 0);
	}

	public GifView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.gifView, defStyle, 0);
		mWidth = a.getDimensionPixelSize(R.styleable.gifView_GifWidth, DEFAULT_WIDTH);
		mHeight = a.getDimensionPixelSize(R.styleable.gifView_GifHeight, DEFAULT_HEIGHT);
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int mCacheSize = maxMemory / 8;
		//给LruCache分配1/8 M
		mMemoryCache = new LruCache<Integer, Bitmap>(mCacheSize) {

			//必须重写此方法，来测量Bitmap的大小
			@Override
			protected int sizeOf(Integer key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
		mUiHandler = new Handler(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mGifLoader != null) {
			mGifLoader.setActive(true);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mGifLoader != null) {
			mGifLoader.setActive(false);
		}
		onDestroy();
	}

	/**
	 * 开始gif动画
	 */
	public void onGifPlay() {
		mShowType = SHOW_TYPE_ANIMAITON;
		isDrawThreadDestory = false;
		new DrawThread().start();
		onGifResume();
	}

	/**
	 * 停止gif动画
	 */
	public void onGifStop() {
		isDrawThreadDestory = true;
	}

	/**
	 * 恢复gif动画
	 */
	public void onGifResume() {
		mIsPause = false;
	}

	public void onGifPause() {
		mIsPause = true;
	}

	public void onDestroy() {
		onGifStop();
		if (mCurrentBitmp != null) {
			mCurrentBitmp.recycle();
			mCurrentBitmp = null;
		}
		if (mGifDecoder != null) {
			mGifDecoder.recycle();
			mGifDecoder = null;
		}
		if (mUiHandler != null) {
			mUiHandler = null;
		}
		clearCache();
	}

	public void clearCache() {
		if (mMemoryCache != null) {
			if (mMemoryCache.size() > 0) {
				mMemoryCache.evictAll();
			}
			mMemoryCache = null;
		}
	}

	/**
	 * 设置图片，并开始解码
	 * @param gif 要设置的图片
	 */
	private void setGifDecoderImage(byte[] gif) {
		if (mGifDecoder == null) {
			mGifDecoder = new GifDecoder();
		}
		mGifDecoder.read(gif);
	}

	/**
	 * 设置图片，开始解码
	 * @param is 要设置的图片
	 */
	private void setGifDecoderImage(InputStream is) {
		if (mGifDecoder == null) {
			mGifDecoder = new GifDecoder();
		}
		mGifDecoder.read(is, 4096);
	}

	/**
	 * 以字节数据形式设置gif图片
	 * @param gif 图片
	 */
	public void setGifImage(byte[] gif) {
		setGifDecoderImage(gif);
	}

	/**
	 * 以字节流形式设置gif图片
	 * @param is 图片
	 */
	public void setGifImage(InputStream is) {
		setGifDecoderImage(is);
	}

	public void setImageFromRaw(int resId) {
		Resources r = getResources();
		InputStream is = r.openRawResource(resId);
		setGifDecoderImage(is);
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setImageFromNetwork(String address) {
		mGifLoader = new OnlineGifLoader(getContext(), address, new OnlineGifLoader.GifLoaderLinstener() {
			@Override
			public void onLoaded(InputStream is) {
				setGifDecoderImage(is);
				onGifPlay();
			}

			@Override
			public void onFailed(String errMsg) {

			}
		});
		GifThreadPoolExecutor.getInstance().execute(mGifLoader);
	}

	public void setImageBitmap(Bitmap bm) {
		if (mGifDecoder == null) {
			mGifDecoder = new GifDecoder();
		}
		mCurrentBitmp = bm;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mGifDecoder == null) {
			return;
		}
		if (mCurrentBitmp == null) {
			return;
		}
		int saveCount = canvas.getSaveCount();
		canvas.save();
		int woffset = (getWidth() - mCurrentBitmp.getWidth()) / 2;
		int hoffset = (getHeight() - mCurrentBitmp.getHeight()) / 2;
		canvas.translate(woffset, hoffset);
		canvas.drawBitmap(mCurrentBitmp, 0, 0, null);
		canvas.restoreToCount(saveCount);
	}

	private Bitmap resizeBitmap(Bitmap bitmap, int width, int heigt) {
		if (bitmap == null) {
			return null;
		}
		Matrix matrix = new Matrix();
		float w = (float) (1.0 * width / bitmap.getWidth());
		float h = (float) (1.0 * heigt / bitmap.getHeight());
		matrix.postScale(w, h); //长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
				matrix, true);
		return resizeBmp;
	}

	@Override
	public boolean handleMessage(Message msg) {
		requestLayout();
		invalidate();
		return true;
	}

	/**
	 * 动画线程
	 */
	private class DrawThread extends Thread {

		@Override
		public void run() {
			if (mGifDecoder == null) {
				return;
			}
			while (mIsRunning) {
				if (isDrawThreadDestory) {
					onDestroy();
					return;
				}
				if (!mIsPause) {
					mGifDecoder.advance();
					int index = mGifDecoder.getCurrentFrameIndex();
					long sleepTime = Math.max(mGifDecoder.getNextDelay(), MIN_GIF_FRAME_DELAY);
					if (getBitmapFromMemCache(index) != null) {
						mCurrentBitmp = getBitmapFromMemCache(index);
					} else {
						if (mGifDecoder.parseOk() && mGifDecoder.getFrameCount() > 1) {
							Bitmap bitmap = mGifDecoder.getNextFrame();
							if (mWidth == DEFAULT_WIDTH) {
								mWidth = bitmap.getWidth();
							}
							if (mHeight == DEFAULT_HEIGHT) {
								mHeight = bitmap.getHeight();
							}
							mCurrentBitmp = resizeBitmap(bitmap, mWidth, mHeight);
							addBitmapToMemoryCache(index, mCurrentBitmp);
						}
					}
					if (mUiHandler != null) {
						Message msg = mUiHandler.obtainMessage();
						mUiHandler.sendMessage(msg);
						try {
							sleep(sleepTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
						}
					} else {
						break;
					}
					if (mShowType == SHOW_TYPE_FIRSTFRAME) {
						break;
					}

				} else {
					try {
						sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mGifDecoder == null) {
			return;
		}
		if (mCurrentBitmp == null) {
			return;
		}
		int pleft = getPaddingLeft();
		int pright = getPaddingRight();
		int ptop = getPaddingTop();
		int pbottom = getPaddingBottom();

		int widthSize;
		int heightSize;

		int w;
		int h;
		w = mCurrentBitmp.getWidth();
		h = mCurrentBitmp.getHeight();

		w += pleft + pright;
		h += ptop + pbottom;
		w = Math.max(w, getSuggestedMinimumWidth());
		h = Math.max(h, getSuggestedMinimumHeight());

		widthSize = resolveSize(w, widthMeasureSpec);
		heightSize = resolveSize(h, heightMeasureSpec);
		setMeasuredDimension(widthSize, heightSize);
	}

	/**
	 * 添加Bitmap到内存缓存
	 * @param key
	 * @param bitmap
	 */
	private void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null && bitmap != null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * 从内存缓存中获取一个Bitmap
	 * @param key 
	 * @return 
	 */
	private Bitmap getBitmapFromMemCache(Integer key) {
		return mMemoryCache.get(key);
	}

	/**
	 * 是不是调试模式
	 * @param isDebug
     */
	public void setDebugMode(boolean isDebug) {
		DEBUG = isDebug;
	}
}
