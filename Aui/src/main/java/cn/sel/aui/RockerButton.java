package cn.sel.aui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class RockerButton extends ImageView implements View.OnTouchListener
{
  public interface ActionLister
  {
    void onLeft();

    void onRight();

    void onUp();

    void onDown();
  }

  final int dip44 = Common.getPixFromDip(getContext(), 44);
  final int padding = Common.getPixFromDip(getContext(), 7);
  private ActionLister action_lister;

  public RockerButton(Context context)
  {
    super(context);
    init();
  }

  public RockerButton(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    init();
  }

  public RockerButton(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init()
  {
    setMinimumWidth(dip44);
    setMinimumHeight(dip44);
    setBackgroundResource(R.drawable.bg_rocker_button);
    setImageResource(R.drawable.shape_rocker_button_normal);
    setLayoutParams(new ViewGroup.LayoutParams(dip44, dip44));
    setPadding(padding, padding, padding, padding);
    setOnTouchListener(this);
    setLongClickable(true);
    setClickable(true);
  }

  public void setActionListener(@NonNull ActionLister actionListener)
  {
    action_lister = actionListener;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event)
  {
    switch(event.getAction())
    {
      case MotionEvent.ACTION_DOWN:
        setImageResource(R.drawable.shape_rocker_button_press);
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        setImageResource(R.drawable.shape_rocker_button_normal);
        setPadding(padding, padding, padding, padding);
        break;
      case MotionEvent.ACTION_MOVE:
        final float x = event.getX();
        final float y = event.getY();
        if(isLeft(x, y))
        {
          setPadding(0, padding, padding * 2, padding);
          if(action_lister != null)
          {
            action_lister.onLeft();
          }
        }else if(isRight(x, y))
        {
          setPadding(padding * 2, padding, 0, padding);
          if(action_lister != null)
          {
            action_lister.onRight();
          }
        }else if(isUp(x, y))
        {
          setPadding(padding, 0, padding, padding * 2);
          if(action_lister != null)
          {
            action_lister.onUp();
          }
        }else if(isDown(x, y))
        {
          setPadding(padding, padding * 2, padding, 0);
          if(action_lister != null)
          {
            action_lister.onDown();
          }
        }else
        {
          setPadding(padding, padding, padding, padding);
        }
      case MotionEvent.ACTION_OUTSIDE:
      default:
        break;
    }
    return super.onTouchEvent(event);
  }

  private boolean isLeft(float x, float y)
  {
    return x < getWidth() / 3.0 && Math.abs(x) > Math.abs(y);
  }

  private boolean isRight(float x, float y)
  {
    return x > getWidth() * 2 / 3.0 && Math.abs(x) > Math.abs(y);
  }

  private boolean isUp(float x, float y)
  {
    return y < getHeight() / 3.0 && Math.abs(x) < Math.abs(y);
  }

  private boolean isDown(float x, float y)
  {
    return y > getHeight() * 2 / 3.0 && Math.abs(x) < Math.abs(y);
  }
}