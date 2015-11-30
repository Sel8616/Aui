package cn.sel.aui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;

@SuppressLint("ClickableViewAccessibility")
public class ViewLooper extends ViewGroup implements GestureDetector.OnGestureListener
{
  private int width;
  private int height;
  private int left_leftPage;
  private int right_leftPage;
  private int left_display;
  private int right_display;
  private int left_rightPage;
  private int right_rightPage;
  private int scrolledX;
  private int pageCount;
  private int curPageIndex;
  private int curViewIndex;
  private boolean isActionUpHandled;
  private boolean isScrollTo;
  private boolean isScrollStopped;
  private boolean isFirstLayout;
  private ArrayList<View> loopingViews;
  private final GestureDetector gestureDetector;
  private final Scroller scroller;

  public ViewLooper(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    isFirstLayout = true;
    scroller = new Scroller(context);
    gestureDetector = new GestureDetector(context, this);
    setOnTouchListener(new OnTouchListener()
    {
      @Override
      public boolean onTouch(View v, MotionEvent event)
      {
        if(GetCount() > 1)
        {
          gestureDetector.onTouchEvent(event);
          if(event.getAction() == MotionEvent.ACTION_UP && !isActionUpHandled && !isScrollStopped)
          {
            scrolledX = (int) event.getX() - scrolledX;
            if(Math.abs(scrolledX) >= width / 2)
            {
              if(scrolledX > 0)
              {
                ShowPrevious();
              }else
              {
                ShowNext();
              }
            }else
            {
              Scroll(false);
            }
          }
          return true;
        }
        return false;
      }
    });
    setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b)
  {
    if(isFirstLayout)
    {
      isFirstLayout = false;
      width = getWidth();
      height = getHeight();
      left_leftPage = -width;
      right_leftPage = 0;
      left_display = 0;
      right_display = width;
      left_rightPage = width;
      right_rightPage = 2 * width;
      Update();
    }
  }

  @Override
  public void computeScroll()
  {
    if(isScrollTo && scroller.computeScrollOffset())
    {
      scrollTo(scroller.getCurrX(), scroller.getCurrY());
    }
    postInvalidate();
  }

  public void SetLoopingViews(ArrayList<View> views)
  {
    loopingViews = views;
    Update();
  }

  public void SetCurrentItem(int index)
  {
    if(pageCount > 0)
    {
      int viewCount = GetCount();
      if(index >= 0 && index < viewCount)
      {
        if(index == GetPreviousIndex())
        {
          ShowPrevious();
        }else if(index == GetNextIndex())
        {
          ShowNext();
        }
      }else
      {
        curViewIndex = index;
        LoadViews();
      }
    }
  }

  private void LoadViews()
  {
    removeAllViews();
    switch(pageCount)
    {
      case 0:
        curPageIndex = -1;
        return;
      case 1:
        addView(loopingViews.get(0), 0);
        curPageIndex = 0;
        break;
      case 2:
        addView(loopingViews.get(0), 0);
        addView(loopingViews.get(1), 1);
        curPageIndex = curViewIndex;
        break;
      case 3:
        addView(loopingViews.get(GetPreviousIndex()), 0);
        addView(loopingViews.get(curViewIndex), 1);
        addView(loopingViews.get(GetNextIndex()), 2);
        curPageIndex = 1;
        break;
    }
    scrollTo(0, 0);
    Order(0);
  }

  public void Update()
  {
    int viewCount = GetCount();
    pageCount = viewCount < 3 ? viewCount : 3;
    if(viewCount > 0)
    {
      if(curViewIndex < 0)
      {
        curViewIndex = 0;
      }
      if(curViewIndex >= viewCount)
      {
        curViewIndex = viewCount - 1;
      }
    }else
    {
      curViewIndex = -1;
    }
    LoadViews();
  }

  private void ShowPrevious()
  {
    curViewIndex = GetPreviousIndex();
    Order(-1);
    Scroll(true);
  }

  private void ShowNext()
  {
    curViewIndex = GetNextIndex();
    Order(1);
    Scroll(true);
  }

  private int GetCount()
  {
    if(loopingViews != null)
    {
      return loopingViews.size();
    }
    return 0;
  }

  private void Order(int scrollDirection)
  {
    int viewCount = GetCount();
    int index_PreviousPage = GetPreviousPage();
    int index_NextPage = GetNextPage();
    switch(scrollDirection)
    {
      case -1:
        if(pageCount == 3)
        {
          int index_PreviousView = (curViewIndex + viewCount - 1) % viewCount;
          removeViewAt(index_NextPage);
          addView(loopingViews.get(index_PreviousView), index_NextPage);
          getChildAt(index_PreviousPage).layout(left_display, 0, right_display, height);
          getChildAt(curPageIndex).layout(left_rightPage, 0, right_rightPage, height);
          getChildAt(index_NextPage).layout(left_leftPage, 0, right_leftPage, height);
        }
        curPageIndex = (curPageIndex + pageCount - 1) % pageCount;
        break;
      case 0:
        if(pageCount == 3)
        {
          getChildAt(index_PreviousPage).layout(left_leftPage, 0, right_leftPage, height);
        }
        getChildAt(curPageIndex).layout(left_display, 0, right_display, height);
        if(pageCount > 1)
        {
          getChildAt(index_NextPage).layout(left_rightPage, 0, right_rightPage, height);
        }
        break;
      case 1:
        if(pageCount == 3)
        {
          int index_NextView = (curViewIndex + 1) % viewCount;
          removeViewAt(index_PreviousPage);
          addView(loopingViews.get(index_NextView), index_PreviousPage);
          getChildAt(index_PreviousPage).layout(left_rightPage, 0, right_rightPage, height);
          getChildAt(curPageIndex).layout(left_leftPage, 0, right_leftPage, height);
          getChildAt(index_NextPage).layout(left_display, 0, right_display, height);
        }
        curPageIndex = (curPageIndex + 1) % pageCount;
        break;
    }
  }

  private void Scroll(boolean changed)
  {
    isScrollTo = true;
    if(changed)
    {
      if(scrolledX > 0)
      {
        if(pageCount == 3)
        {
          scroller.startScroll(-scrolledX + width, 0, scrolledX - width, 0);
        }else
        {
          scroller.startScroll(width - scrolledX, 0, scrolledX - width, 0);
        }
      }else
      {
        if(pageCount == 3)
        {
          scroller.startScroll(-scrolledX - width, 0, scrolledX + width, 0);
        }else
        {
          scroller.startScroll(-scrolledX, 0, width + scrolledX, 0);
        }
      }
    }else
    {
      if(pageCount == 3)
      {
        scroller.startScroll(-scrolledX, 0, scrolledX, 0);
      }else
      {
        scroller.startScroll(curPageIndex * width - scrolledX, 0, scrolledX, 0);
      }
    }
    invalidate();
  }

  private int GetPreviousIndex()
  {
    int viewCount = GetCount();
    if(curViewIndex >= 0 && viewCount > 0)
    {
      return (curViewIndex + viewCount - 1) % viewCount;
    }
    return -1;
  }

  private int GetNextIndex()
  {
    int viewCount = GetCount();
    if(curViewIndex >= 0 && viewCount > 0)
    {
      return (curViewIndex + 1) % viewCount;
    }
    return -1;
  }

  private int GetPreviousPage()
  {
    return (curPageIndex + pageCount - 1) % pageCount;
  }

  private int GetNextPage()
  {
    return (curPageIndex + 1) % pageCount;
  }

  @Override
  public boolean onDown(MotionEvent e)
  {
    scrolledX = (int) e.getX();
    isActionUpHandled = false;
    return true;
  }

  @Override
  public void onShowPress(MotionEvent e)
  {
  }

  @Override
  public boolean onSingleTapUp(MotionEvent e)
  {
    return true;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
  {
    if(distanceX != 0 && Math.abs(distanceY / distanceX) < 1)
    {
      isScrollTo = false;
      if(pageCount < 3)
      {
        int[] pos = new int[2];
        getChildAt(0).getLocationOnScreen(pos);
        if(distanceX < 0 && pos[0] == 0)
        {
          isScrollStopped = true;
          return true;
        }
        getChildAt(pageCount - 1).getLocationOnScreen(pos);
        if(distanceX > 0 && pos[0] == 0)
        {
          isScrollStopped = true;
          return true;
        }
      }
      isScrollStopped = false;
      scrollBy((int) distanceX, 0);
    }
    return true;
  }

  @Override
  public void onLongPress(MotionEvent e)
  {
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
  {
    scrolledX = (int) e2.getX() - scrolledX;
    if(velocityX > 0 && e2.getX() - e1.getX() > 0)
    {
      if(pageCount == 2 && curPageIndex == 0)
      {
        return true;
      }
      ShowPrevious();
    }else if(velocityX < 0 && e2.getX() - e1.getX() < 0)
    {
      if(pageCount == 2 && curPageIndex == 1)
      {
        return true;
      }
      ShowNext();
    }else
    {
      Scroll(true);
    }
    isActionUpHandled = true;
    return true;
  }
}