package cn.sel.aui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class RockerButton extends ImageView implements View.OnTouchListener
{
  public enum Direction
  {
    LEFT, RIGHT, UP, DOWN
  }

  public enum RockerMode
  {
    ONCE, ELASTIC
  }

  public interface ActionLister
  {
    void onLeft(float traction);

    void onRight(float traction);

    void onUp(float traction);

    void onDown(float traction);

    void onStop(@Nullable Direction direction);
  }

  private static final int SIZE_NORMAL = 48;
  private static final int SIZE_MIN = 40;

  private int size;
  private int padding;
  private float x_press;
  private float y_press;
  private boolean action_canceled = false;
  private boolean action_moved = false;
  private float pulling_limit = 2.0f;
  private RockerMode rockerMode_left = RockerMode.ELASTIC;
  private RockerMode rockerMode_right = RockerMode.ELASTIC;
  private RockerMode rockerMode_up = RockerMode.ELASTIC;
  private RockerMode rockerMode_down = RockerMode.ELASTIC;
  private ActionLister action_lister;

  public RockerButton(Context context)
  {
    super(context);
    initView(null);
  }

  public RockerButton(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    initView(attrs);
  }

  public RockerButton(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
    initView(attrs);
  }

  private void initView(AttributeSet attrs)
  {
    if(getBackground() == null)
    {
      setBackgroundResource(R.drawable.bg_rocker_button);
    }
    if(getDrawable() == null)
    {
      setImageResource(R.drawable.shape_rocker_button_normal);
    }
    setScaleType(ScaleType.FIT_CENTER);
    setOnTouchListener(this);
    setLongClickable(true);
    setClickable(true);
    int minSize = Common.getPixFromDip(getContext(), SIZE_MIN);
    setMinimumHeight(minSize);
    setMinimumWidth(minSize);
    resetSize(attrs);
    recenter();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
  {
    super.onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    setMeasuredDimension(size, size);
  }

  public void setActionListener(@NonNull ActionLister actionListener)
  {
    action_lister = actionListener;
  }

  private int calcEdge()
  {
    int dp = Common.getPixFromDip(getContext(), 5);
    dp = Math.max(dp, size / 8);
    return dp;
  }

  private void resetSize(AttributeSet attrs)
  {
    TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.layout_width, android.R.attr.layout_height});
    int width = 0, height = 0;
    int index0 = a.getIndex(0);
    if(a.hasValue(index0))
    {
      width = a.getLayoutDimension(index0, 0);
    }
    int index1 = a.getIndex(1);
    if(a.hasValue(index1))
    {
      height = a.getLayoutDimension(index1, 0);
    }
    a.recycle();
    if(width <= 0)
    {
      width = Common.getPixFromDip(getContext(), SIZE_NORMAL);
    }
    if(height <= 0)
    {
      height = Common.getPixFromDip(getContext(), SIZE_NORMAL);
    }
    size = Math.min(width, height);
    padding = calcEdge();
  }

  public void setPullingLimit(float pullingLimit)
  {
    pulling_limit = pullingLimit;
  }

  public void setRockerMode(@NonNull RockerMode left, @NonNull RockerMode right, @NonNull RockerMode up, @NonNull RockerMode down)
  {
    rockerMode_left = left;
    rockerMode_right = right;
    rockerMode_up = up;
    rockerMode_down = down;
  }

  public RockerMode getRockerMode(@NonNull Direction direction)
  {
    switch(direction)
    {
      case LEFT:
        return rockerMode_left;
      case RIGHT:
        return rockerMode_right;
      case UP:
        return rockerMode_up;
      case DOWN:
        return rockerMode_down;
      default:
        return null;
    }
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event)
  {
    return (event.getAction() != MotionEvent.ACTION_MOVE || !action_canceled) && super.dispatchTouchEvent(event);
  }

  @Override
  public boolean onTouch(View v, MotionEvent event)
  {
    float x = event.getX();
    float y = event.getY();
    Direction direction = getDirection(x, y);
    switch(event.getAction())
    {
      case MotionEvent.ACTION_DOWN:
        action_moved = false;
        x_press = x;
        y_press = y;
        setImageResource(R.drawable.shape_rocker_button_press);
        break;
      case MotionEvent.ACTION_UP:
        action_canceled = false;
        if(action_moved)
        {
          resetButton(direction);
          return true;
        }
        resetButton(null);
        break;
      case MotionEvent.ACTION_CANCEL:
        action_canceled = false;
        resetButton(null);
        break;
      case MotionEvent.ACTION_MOVE:
        action_moved = true;
        changeButtonView(x, y, direction);
        return true;
      default:
        break;
    }
    return super.onTouchEvent(event);
  }

  private void changeButtonView(float moveActionX, float moveActionY, Direction direction)
  {
    float traction;
    if(direction != null)
    {
      switch(direction)
      {
        case LEFT:
          traction = getTraction(moveActionX, x_press);
          setPadding(0, padding, padding * 2, padding);
          if(action_lister != null)
          {
            action_lister.onLeft(traction);
          }
          if(rockerMode_left == RockerMode.ONCE)
          {
            releaseTouchFocus(Direction.LEFT);
          }
          break;
        case RIGHT:
          traction = getTraction(moveActionX, x_press);
          setPadding(padding * 2, padding, 0, padding);
          if(action_lister != null)
          {
            action_lister.onRight(traction);
          }
          if(rockerMode_right == RockerMode.ONCE)
          {
            releaseTouchFocus(Direction.RIGHT);
          }
          break;
        case UP:
          traction = getTraction(moveActionY, y_press);
          setPadding(padding, 0, padding, padding * 2);
          if(action_lister != null)
          {
            action_lister.onUp(traction);
          }
          if(rockerMode_up == RockerMode.ONCE)
          {
            releaseTouchFocus(Direction.UP);
          }
          break;
        case DOWN:
          traction = getTraction(moveActionY, y_press);
          setPadding(padding, padding * 2, padding, 0);
          if(action_lister != null)
          {
            action_lister.onDown(traction);
          }
          if(rockerMode_down == RockerMode.ONCE)
          {
            releaseTouchFocus(Direction.DOWN);
          }
          break;
      }
    }else
    {
      recenter();
    }
  }

  private void resetButton(Direction direction)
  {
    setImageResource(R.drawable.shape_rocker_button_normal);
    recenter();
    if(action_lister != null)
    {
      action_lister.onStop(direction);
    }
  }

  private void recenter()
  {
    setPadding(padding, padding, padding, padding);
  }

  private void releaseTouchFocus(final Direction direction)
  {
    action_canceled = true;
    postDelayed(new Runnable()
    {
      @Override
      public void run()
      {
        resetButton(direction);
      }
    }, 200);
  }

  /**
   * @param x Current x
   * @param y Current y
   *
   * @return {@link Direction} or Null
   */
  private Direction getDirection(float x, float y)
  {
    float limit = size / 3.0f;
    float distance_h = x - x_press;
    float distance_v = y - y_press;
    float abs_distance_h = Math.abs(distance_h);
    float abs_distance_v = Math.abs(distance_v);
    if(abs_distance_h > abs_distance_v)
    {//Horizontal
      if(abs_distance_h > limit)
      {
        if(distance_h < 0)
        {
          return Direction.LEFT;
        }else
        {
          return Direction.RIGHT;
        }
      }
    }else if(abs_distance_h < abs_distance_v)
    {//Vertical
      if(abs_distance_v > limit)
      {
        if(distance_v < 0)
        {
          return Direction.UP;
        }else
        {
          return Direction.DOWN;
        }
      }
    }
    return null;
  }

  private float getTraction(float pos_current, float pos_press)
  {
    float result = Math.abs(pos_current - pos_press) / pulling_limit / size;
    return Math.min(result, 1);
  }
}