package cn.sel.aui;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class Common
{
  private static Toast TOAST;

  /**
   * Show toast message.
   *
   * @param context Context
   * @param message Text to display
   */
  public static void ShowMessage(Context context, String message)
  {
    if(context != null)
    {
      if(TOAST != null)
      {
        TOAST.cancel();
      }
      Toast newToast = Toast.makeText(context, message, message.length() < 15 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
      TextView textView = (TextView) newToast.getView().findViewById(android.R.id.message);
      textView.setGravity(Gravity.CENTER);
      textView.setSingleLine(false);
      newToast.show();
      TOAST = newToast;
    }
  }

  /**
   * Show toast message.
   *
   * @param context        Context
   * @param resIdOfMessage Resource id Of the message
   */
  public static void ShowMessage(Context context, int resIdOfMessage)
  {
    if(context != null)
    {
      ShowMessage(context, context.getString(resIdOfMessage));
    }
  }

  public static int getPixFromDip(Context context, int dip)
  {
    if(context != null)
    {
      return dip * context.getResources().getDisplayMetrics().densityDpi / 160;
    }else
    {
      return 0;
    }
  }

  public static int getDipFromPix(Context context, int pix)
  {
    if(context != null)
    {
      return pix * 160 / context.getResources().getDisplayMetrics().densityDpi;
    }else
    {
      return 0;
    }
  }
}