package cn.sel.aui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import java.io.InputStream;
import java.net.URL;

/**
 * @author sel QQ:117764756 Email:sel8616@gmail.com philshang@163.com
 */
public class PictureView extends ViewSwitcher
{
  private Context context;
  private ImageView imageView;
  private ProgressBar progressBar;

  private Drawable loadingImage;
  private Drawable errorImage;

  public PictureView(Context context)
  {
    super(context);
    Init();
  }

  public PictureView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    Init();
  }

  private void Init()
  {
    context = getContext();
    errorImage = getResources().getDrawable(android.R.drawable.stat_notify_error);
    imageView = new ImageView(getContext());
    progressBar = new ProgressBar(getContext());
    addView(imageView, 0);
    addView(progressBar, 1);
    InitProgressBarPosition();
  }

  private void InitProgressBarPosition()
  {
    measure(0, 0);
    int dpi = getResources().getDisplayMetrics().densityDpi;
    int dip = dpi < 120 ? 32 : dpi < 240 ? 48 : dpi < 320 ? 64 : dpi < 480 ? 72 : 96;
    int pix = GetPixFromDip(context, dip);
    int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
    if(size > 0)
    {
      pix = Math.min(size, pix);
    }
    LayoutParams layoutParams = new LayoutParams(pix, pix, Gravity.CENTER);
    progressBar.setLayoutParams(layoutParams);
  }

  public void SetImage(@Nullable Drawable image)
  {
    imageView.setImageDrawable(image);
    ShowImage();
  }

  public void SetImage(int image_res_id)
  {
    imageView.setImageResource(image_res_id);
    ShowImage();
  }

  public void SetImageURL(@Nullable final String url)
  {
    ShowLoading();
    if(!TextUtils.isEmpty(url))
    {
      new AsyncTask<String, Integer, Drawable>()
      {
        @Override
        protected Drawable doInBackground(String... params)
        {
          Drawable drawable = null;
          try
          {
            String url = params[0];
            Log.d("PictureView", "Get PIC from given host: " + url);
            InputStream is = (InputStream) new URL(url).getContent();
            if(is != null)
            {
              drawable = Drawable.createFromStream(is, "PictureView_Result");
            }
          }catch(Exception e)
          {
            Log.e(getClass().getName(), "Request fail! ErrorMessage: " + e.toString());
          }
          if(drawable == null)
          {
            drawable = errorImage;
          }
          return drawable;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
        }

        @Override
        protected void onPostExecute(Drawable result)
        {
          if(result != null)
          {
            imageView.setImageDrawable(result);
          }else
          {
            imageView.setImageDrawable(errorImage);
          }
          ShowImage();
        }

        @Override
        protected void onCancelled(Drawable result)
        {
        }
      }.execute(url);
    }
  }

  public void SetLoadingImage(Drawable img)
  {
    loadingImage = img;
  }

  public void SetLoadingImage(int img_res_id)
  {
    loadingImage = getResources().getDrawable(img_res_id);
  }

  public void SetErrorImage(Drawable img)
  {
    errorImage = img;
  }

  public void SetErrorImage(int img_res_id)
  {
    errorImage = getResources().getDrawable(img_res_id);
  }

  public void ShowImage()
  {
    setDisplayedChild(0);
  }

  public void ShowLoading()
  {
    if(loadingImage != null)
    {
      progressBar.setIndeterminate(true);
      progressBar.setIndeterminateDrawable(loadingImage);
    }
    setDisplayedChild(1);
  }

  private int GetPixFromDip(Context context, int dip)
  {
    if(context != null)
    {
      return (int) (dip * context.getResources().getDisplayMetrics().density + 0.5f);
    }else
    {
      return 0;
    }
  }
}