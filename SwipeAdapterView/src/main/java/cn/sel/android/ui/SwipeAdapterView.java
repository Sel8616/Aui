package cn.sel.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * SwipeRefreshLayout + GridView
 *
 * @author Sel  QQ:117764756  E-Mail:philshang@163.com
 */
public class SwipeAdapterView extends LinearLayout implements SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener, GridView.OnScrollListener, GridView.OnItemClickListener
{
  public enum ViewMode
  {
    ListView, GridView
  }

  public enum PromptType
  {
    Text, Image, ImageText_TB, ImageText_BT, ImageText_LR, ImageText_RL
  }

  private enum FooterStatus
  {
    Hide, Loading, Loaded, Empty, Fail
  }

  public interface ActionListener
  {
    void OnRefreshing();

    void OnRefreshed(boolean success);

    void OnNexting();

    void OnNexted(boolean success);

    void OnItemSelect(int position, Object data, View view);
  }

  private Context context;
  private ViewMode viewMode;
  private int columnNum;
  private boolean footerEnabled = true;
  private Drawable footerBackground;
  private PromptType promptType;
  private ActionListener actionListener;

  private SwipeRefreshLayout swipeRefreshLayout;
  private ViewFlipper flipper_Main;
  private GridView data_GridView;
  private LinearLayout footer;
  private TextView footer_Text;
  private ProgressBar footer_Progress;
  private TextView prompt_Text;
  private ImageView prompt_Image;


  public SwipeAdapterView(Context context)
  {
    super(context);
    Init();
    InitCustomProperties(null, 0);
  }

  public SwipeAdapterView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    Init();
    InitCustomProperties(attrs, 0);
  }

  public SwipeAdapterView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    Init();
    InitCustomProperties(attrs, defStyle);
  }

  private void Init()
  {
    context = getContext();
    setOrientation(VERTICAL);

    swipeRefreshLayout = (SwipeRefreshLayout) View.inflate(context, R.layout.swipe_main, null);
    footer = (LinearLayout) View.inflate(context, R.layout.swipe_footer, null);
    swipeRefreshLayout.setOnRefreshListener(this);

    flipper_Main = (ViewFlipper) swipeRefreshLayout.findViewById(R.id.asharp_swipe_container);
    prompt_Text = (TextView) flipper_Main.findViewById(R.id.asharp_swipe_prompt_text);
    prompt_Image = (ImageView) flipper_Main.findViewById(R.id.asharp_swipe_prompt_image);
    data_GridView = (GridView) flipper_Main.findViewById(R.id.asharp_swipe_content);
    data_GridView.setOnScrollListener(this);
    data_GridView.setOnItemClickListener(this);
    SetViewMode(ViewMode.ListView);

    footer_Text = (TextView) footer.findViewById(R.id.asharp_swipe_footer_text);
    footer_Progress = (ProgressBar) footer.findViewById(R.id.asharp_swipe_footer_progress);
    footer.setOnClickListener(this);
    ChangeFooter(FooterStatus.Hide, null);
  }

  private void InitCustomProperties(AttributeSet attrs, int defStyle)
  {
    int style_view_mode, style_prompt_mode, style_prompt_footer;
    int footerHeight = GetPixFromDip(context, 44);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeAdapterView, defStyle, 0);
    if(a.hasValue(R.styleable.SwipeAdapterView_ViewMode))
    {
      style_view_mode = a.getInt(R.styleable.SwipeAdapterView_ViewMode, 0);
    }else
    {
      style_view_mode = 0;
    }
    if(a.hasValue(R.styleable.SwipeAdapterView_PromptMode))
    {
      style_prompt_mode = a.getInt(R.styleable.SwipeAdapterView_PromptMode, 0);
    }else
    {
      style_prompt_mode = 0;
    }
    if(a.hasValue(R.styleable.SwipeAdapterView_FooterEnabled))
    {
      style_prompt_footer = a.getInt(R.styleable.SwipeAdapterView_FooterEnabled, 1);
    }else
    {
      style_prompt_footer = 1;
    }
    if(a.hasValue(R.styleable.SwipeAdapterView_FooterHeight))
    {
      footerHeight = a.getDimensionPixelSize(R.styleable.SwipeAdapterView_FooterHeight, footerHeight);
    }
    if(a.hasValue(R.styleable.SwipeAdapterView_FooterBackground))
    {
      footerBackground = a.getDrawable(R.styleable.SwipeAdapterView_FooterBackground);
    }
    a.recycle();
    viewMode = style_view_mode == 0 ? ViewMode.ListView : ViewMode.GridView;
    footerEnabled = style_prompt_footer == 1;
    if(footerBackground == null)
    {
      footerBackground = getResources().getDrawable(android.R.drawable.bottom_bar);
    }
    switch(style_prompt_mode)
    {
      case 0:
        promptType = PromptType.Text;
        break;
      case 1:
        promptType = PromptType.Image;
        break;
      case 2:
        promptType = PromptType.ImageText_TB;
        break;
      case 3:
        promptType = PromptType.ImageText_BT;
        break;
      case 4:
        promptType = PromptType.ImageText_LR;
        break;
      case 5:
        promptType = PromptType.ImageText_RL;
        break;
    }
    footer.setBackgroundDrawable(footerBackground);
    addView(swipeRefreshLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
    addView(footer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, footerHeight));
  }

  public void SetActionListener(ActionListener listener)
  {
    actionListener = listener;
  }

  public void SetContentView(View view)
  {
    Log.i("SwipeAdapterView", "SetContentView");
    swipeRefreshLayout.addView(view);
  }

  public void SetContentView(int view_res_id)
  {
    swipeRefreshLayout.addView(View.inflate(context, view_res_id, null));
  }

  public void SetFooterEnabled(boolean enabled)
  {
    footerEnabled = enabled;
    int visibility = enabled ? VISIBLE : GONE;
    footer.setVisibility(visibility);
    footer_Text.setVisibility(visibility);
    footer_Progress.setVisibility(GONE);
  }

  public boolean GetFooterEnabled()
  {
    return footerEnabled;
  }

  public void SetViewMode(@Nullable ViewMode view_mode)
  {
    if(view_mode != null)
    {
      viewMode = view_mode;
      if(viewMode == ViewMode.ListView)
      {
        columnNum = data_GridView.getNumColumns();
        data_GridView.setNumColumns(1);
      }else
      {
        if(columnNum <= 0)
        {
          columnNum = 1;
        }
        data_GridView.setNumColumns(columnNum);
      }
    }
  }

  public void SetDataAdapter(BaseAdapter adapter)
  {
    data_GridView.setAdapter(adapter);
    adapter.notifyDataSetChanged();
  }

  public void SetColumnNum(int cols)
  {
    Log.i("SwipeAdapterView", "SetColumnNum:" + cols);
    columnNum = cols > 0 ? cols : 1;
    if(viewMode == ViewMode.GridView)
    {
      data_GridView.setNumColumns(columnNum);
    }
  }

  public void SetPromptType(PromptType type)
  {
    if(type != null)
    {
      promptType = type;
    }
    if(promptType != null)
    {
      switch(promptType)
      {
        case Text:
          prompt_Image.setVisibility(GONE);
          break;
        case Image:
          prompt_Text.setVisibility(GONE);
          break;
        case ImageText_TB:
          prompt_Text.setVisibility(VISIBLE);
          prompt_Image.setVisibility(VISIBLE);
          break;
        case ImageText_LR:
          prompt_Text.setVisibility(VISIBLE);
          prompt_Image.setVisibility(VISIBLE);
          break;
        case ImageText_RL:
          prompt_Text.setVisibility(VISIBLE);
          prompt_Image.setVisibility(VISIBLE);
          break;
        case ImageText_BT:
          prompt_Text.setVisibility(VISIBLE);
          prompt_Image.setVisibility(VISIBLE);
          break;
      }
    }
  }

  public void SetPrompt(int text_res_id, int img_res_id)
  {
    prompt_Text.setText(text_res_id);
    prompt_Image.setImageResource(img_res_id);
  }

  public void SetPrompt(String text, int img_res_id)
  {
    prompt_Text.setText(text);
    prompt_Image.setImageResource(img_res_id);
  }

  public void SetPrompt(int text_res_id, Drawable img)
  {
    prompt_Text.setText(text_res_id);
    prompt_Image.setImageDrawable(img);
  }

  public void SetPrompt(String text, Drawable img)
  {
    prompt_Text.setText(text);
    prompt_Image.setImageDrawable(img);
  }

  public void SetPromptText(int text_res_id)
  {
    prompt_Text.setText(text_res_id);
  }

  public void SetPromptText(String text)
  {
    prompt_Text.setText(text);
  }

  public void SetPromptImage(Drawable img)
  {
    prompt_Image.setImageDrawable(img);
  }

  public void SetPromptImage(int img_res_id)
  {
    prompt_Image.setImageResource(img_res_id);
  }

  public void SetFooterText(@Nullable String text)
  {
    Log.i("SwipeAdapterView", "SetFooterText: " + text);
    footer_Text.setText(text);
  }

  public void NotifyRefreshSuccess(@Nullable String message)
  {
    Log.i("SwipeAdapterView", "NotifyRefreshSuccess: " + message);
    if(TextUtils.isEmpty(message))
    {
      message = "Success.";
    }
    swipeRefreshLayout.setRefreshing(false);
    ChangeFooter(FooterStatus.Loaded, message);
    if(actionListener != null)
    {
      actionListener.OnRefreshed(true);
      ShowContent();
    }else
    {
      ShowError();
    }
  }

  public void NotifyRefreshEmpty(@Nullable String message)
  {
    Log.i("SwipeAdapterView", "NotifyRefreshEmpty: " + message);
    if(TextUtils.isEmpty(message))
    {
      message = "Empty.";
    }
    swipeRefreshLayout.setRefreshing(false);
    ChangeFooter(FooterStatus.Hide, message);
    if(actionListener != null)
    {
      actionListener.OnRefreshed(true);
      SetPromptText("empty");
      ShowPrompt();
    }else
    {
      ShowError();
    }
  }

  public void NotifyRefreshFailure(@Nullable String message)
  {
    Log.i("SwipeAdapterView", "NotifyRefreshFailure: " + message);
    if(TextUtils.isEmpty(message))
    {
      message = "Failure.";
    }
    swipeRefreshLayout.setRefreshing(false);
    ChangeFooter(FooterStatus.Hide, message);
    if(actionListener != null)
    {
      actionListener.OnRefreshed(false);
      SetPromptText("fail");
      ShowPrompt();
    }else
    {
      ShowError();
    }
  }

  public void NotifyNextSuccess(@Nullable String message)
  {
    Log.i("SwipeAdapterView", "NotifyNextSuccess: " + message);
    if(TextUtils.isEmpty(message))
    {
      message = "Success.";
    }
    ChangeFooter(FooterStatus.Loaded, message);
    if(actionListener != null)
    {
      actionListener.OnNexted(true);
    }else
    {
      ShowError();
    }
  }

  public void NotifyNextEmpty(@Nullable String message)
  {
    Log.i("SwipeAdapterView", "NotifyNextEmpty: " + message);
    if(TextUtils.isEmpty(message))
    {
      message = "Empty.";
    }
    ChangeFooter(FooterStatus.Empty, message);
    if(actionListener != null)
    {
      actionListener.OnNexted(true);
    }else
    {
      ShowError();
    }
  }

  public void NotifyNextFailure(@Nullable String message)
  {
    Log.i("SwipeAdapterView", "NotifyNextFailure: " + message);
    if(TextUtils.isEmpty(message))
    {
      message = "Failure.";
    }
    ChangeFooter(FooterStatus.Fail, message);
    if(actionListener != null)
    {
      actionListener.OnNexted(false);
    }else
    {
      ShowError();
    }
  }

  @Override
  public void onClick(View v)
  {
    Log.i("SwipeAdapterView", "onClick");
    if(actionListener != null)
    {
      ChangeFooter(FooterStatus.Loading, "Loading...");
      actionListener.OnNexting();
    }else
    {
      ShowError();
    }
  }

  @Override
  public void onRefresh()
  {
    Log.i("SwipeAdapterView", "onRefresh");
    if(actionListener != null)
    {
      ShowPrompt("loading");
      ChangeFooter(FooterStatus.Hide, null);
      actionListener.OnRefreshing();
    }else
    {
      ShowError();
    }
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState)
  {

  }

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
  {
    swipeRefreshLayout.setEnabled(firstVisibleItem == 0);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    if(actionListener != null)
    {
      actionListener.OnItemSelect(position, data_GridView.getItemAtPosition(position), view);
    }
  }

  private void ShowContent()
  {
    Log.i("SwipeAdapterView", "ShowContent");
    int curPage = flipper_Main.getDisplayedChild();
    if(curPage != 0)
    {
      flipper_Main.setDisplayedChild(0);
    }
  }

  private void ShowPrompt(@Nullable String message)
  {
    Log.i("SwipeAdapterView", "ShowPrompt");
    int curPage = flipper_Main.getDisplayedChild();
    if(curPage != 1)
    {
      flipper_Main.setDisplayedChild(1);
      if(!TextUtils.isEmpty(message))
      {
        SetPromptText(message);
      }
    }
  }

  private void ShowPrompt()
  {
    ShowPrompt(null);
  }

  private void ShowError()
  {
    Log.i("SwipeAdapterView", "ShowError");
    int curPage = flipper_Main.getDisplayedChild();
    if(curPage != 2)
    {
      flipper_Main.setDisplayedChild(2);
    }
  }

  private void ChangeFooter(@NonNull FooterStatus status, @Nullable String message)
  {
    Log.i("SwipeAdapterView", "ChangeFooter: " + status.name() + "__" + message + "_" + footerEnabled);
    SetFooterText(message);
    if(footerEnabled)
    {
      switch(status)
      {
        case Hide:
          footer.setVisibility(GONE);
          break;
        case Loading:
          footer.setVisibility(VISIBLE);
          footer_Progress.setVisibility(VISIBLE);
          break;
        case Loaded:
          footer.setVisibility(VISIBLE);
          footer_Progress.setVisibility(GONE);
          break;
        case Empty:
          footer.setVisibility(VISIBLE);
          footer_Progress.setVisibility(GONE);
          break;
        case Fail:
          footer.setVisibility(VISIBLE);
          footer_Progress.setVisibility(GONE);
          break;
      }
    }
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