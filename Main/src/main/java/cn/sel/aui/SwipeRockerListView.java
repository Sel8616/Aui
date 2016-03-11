package cn.sel.aui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
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

import java.util.List;

import cn.sel.aui.RockerButton.RockerMode;

public class SwipeRockerListView extends LinearLayout
{
  public enum ViewMode
  {
    ListView, GridView
  }

  public enum PromptType
  {
    Text, Image, ImageText_TB, ImageText_BT, ImageText_LR, ImageText_RL
  }

  public enum FooterMode
  {
    Disabled, Scrolling, Docked
  }

  public enum TranscriptMode
  {
    Disabled, Normal, AlwaysScroll
  }

  private enum FooterStatus
  {
    Hide, Loading, Loaded, Empty, Fail
  }

  public interface ActionListener
  {
    /**
     * Handle 'Refresh' event to reload data.
     */
    void onRefresh();

    /**
     * Handle 'More' event to load next page of data.
     */
    void onMore();

    /**
     * Handle item click event.
     *
     * @param position Index of this item(start with 0)
     * @param data     Current data object(A force-cast may be necessary)
     * @param view     Current item view
     */
    void onItemSelect(int position, Object data, View view);

    /**
     * @return Template view. Normally, it should be inflated from a layout source file.
     */
    View getViewTemplate();

    /**
     * For ViewHolder pattern, when 'convertView' was null, {@link #getViewTemplate} gave a new instance without any holder yet, this method would init a holder for it.     * @param convertView current
     *
     * @param convertView Current new instantiated 'convertView'
     *
     * @return A new ViewHolder that should be defined in user code.
     */
    Object getViewHolder(View convertView);

    /**
     * Fill the view holder with the data in the specific position.<br>
     * Because the data structure was unknown as the view's, this work must be done by the user.
     *
     * @param viewHolder Current view holder
     * @param position   Index of data(start with 0)
     */
    void fillCurrentItem(Object viewHolder, int position);
  }

  /**
   * Listen refresh event of {@link #swipeRefreshLayout}
   */
  final SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener()
  {
    @Override
    public void onRefresh()
    {
      if(isLoading)
      {
        Common.ShowMessage(context, msgBusy);
      }else if(actionListener != null)
      {
        showPrompt(msgLoading);
        changeFooter(FooterStatus.Hide, null);
        isLoading = true;
        actionListener.onRefresh();
      }else
      {
        ShowBug();
      }
    }
  };
  /**
   * Listen scroll event of {@link #dataGridView}
   */
  final AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener()
  {
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
      swipeRefreshLayout.setEnabled(firstVisibleItem == 0);
    }
  };
  /**
   * Listen item clicked event of {@link #dataGridView}
   */
  final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
  {
    @Override
    final public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
      if(savedFooterMode != FooterMode.Scrolling || position != dataAdapter.getCount())
      {
        if(actionListener != null)
        {
          actionListener.onItemSelect(position, dataGridView.getItemAtPosition(position), view);
        }
      }
    }
  };
  /**
   * Listen clicked event of footerBar
   */
  final OnClickListener onClickListener = new OnClickListener()
  {
    @Override
    public void onClick(View v)
    {
      if(v.getId() == R.id.rocker_button)
      {
        dataGridView.smoothScrollToPositionFromTop(0, 0, 0);
      }else
      {
        if(isLoading)
        {
          Common.ShowMessage(context, msgBusy);
        }else if(actionListener != null)
        {
          changeFooter(FooterStatus.Loading, msgLoading);
          isLoading = true;
          actionListener.onMore();
        }else
        {
          ShowBug();
        }
      }
    }
  };
  final RockerButton.ActionLister actionLister = new RockerButton.ActionLister()
  {
    @Override
    public void onLeft(float traction)
    {
      int first = dataGridView.getFirstVisiblePosition();
      dataGridView.smoothScrollToPosition(0, first);
    }

    @Override
    public void onRight(float traction)
    {
      int last = dataGridView.getLastVisiblePosition();
      int pageSize = dataGridView.getChildCount();
      int count = dataAdapter.getCount();
      boolean isLastChildEntirelyVisible = dataGridView.getChildAt(pageSize - 1).getBottom() == dataGridView.getBottom();
      int target = Math.min(count - 1, isLastChildEntirelyVisible ? last + 1 : last);
      dataGridView.smoothScrollToPositionFromTop(target, 0);
    }

    @Override
    public void onUp(float traction)
    {
      dataGridView.smoothScrollToPosition(0);
    }

    @Override
    public void onDown(float traction)
    {
      dataGridView.smoothScrollToPosition(dataAdapter.getCount());
    }

    @Override
    public void onStop(RockerButton.Direction direction)
    {
    }
  };

  /**
   * Data adapter for {@link #dataGridView}
   */
  private class DataAdapter extends BaseAdapter
  {
    @Override
    public void notifyDataSetChanged()
    {
      super.notifyDataSetChanged();
      final int columns = dataGridView.getNumColumns();
      for(int i = 0; i < getCount(); i += columns)
      {
        View itemView = getView(i, null, dataGridView);
        itemView.measure(0, 0);
        totalHeight += itemView.getMeasuredHeight();
      }
    }

    @Override
    public int getCount()
    {
      final int actualCount = dataList == null ? 0 : dataList.size();
      return (savedViewMode == ViewMode.ListView && savedFooterMode == FooterMode.Scrolling) ? actualCount + 1 : actualCount;
    }

    @Override
    public Object getItem(int position)
    {
      if(dataList != null && position >= 0 && position < dataList.size())
      {
        return dataList.get(position);
      }
      return null;
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      if(savedViewMode == ViewMode.ListView && savedFooterMode == FooterMode.Scrolling && position == getCount() - 1)
      {
        return scrollFooterBar;
      }else
      {
        Object viewHolder;
        if(convertView == null || convertView.getId() == scrollFooterBar.getId())
        {
          convertView = actionListener.getViewTemplate();
          viewHolder = actionListener.getViewHolder(convertView);
          convertView.setTag(viewHolder);
        }else
        {
          viewHolder = convertView.getTag();
        }
        actionListener.fillCurrentItem(viewHolder, position);
        return convertView;
      }
    }
  }

  private Context context;
  private int columnNum;
  private int totalHeight;
  private boolean isLoading;
  private Drawable footerBackground;
  private ViewMode savedViewMode;
  private FooterMode savedFooterMode;
  private PromptType savedPromptType;
  private ActionListener actionListener;
  private DataAdapter dataAdapter;
  private List dataList;
  //
  private String msgLoading = "Loading...";
  private String msgBusy = "Slow down your finger!";
  private String msgRefreshSuccess = "Success!";
  private String msgRefreshEmpty = "No Data!";
  private String msgRefreshFailure = "Failure!";
  private String msgMoreSuccess = "Success!";
  private String msgMoreEmpty = "No Data!";
  private String msgMoreFailure = "Failure!";
  //
  private SwipeRefreshLayout swipeRefreshLayout;
  private ViewFlipper viewFlipper;
  private GridView dataGridView;
  private TextView promptText;
  private ImageView promptImage;
  private LinearLayout dockedFooterBar, scrollFooterBar;
  private TextView dockedFooterText, scrollFooterText;
  private ProgressBar dockedFooterProgress, scrollFooterProgress;
  private RockerButton rockerButton;

  public SwipeRockerListView(Context context)
  {
    super(context);
    InitViews();
    InitCustomAttributes(null, 0);
  }

  public SwipeRockerListView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    InitViews();
    InitCustomAttributes(attrs, 0);
  }

  public SwipeRockerListView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    InitViews();
    InitCustomAttributes(attrs, defStyle);
  }

  private void InitViews()
  {
    context = getContext();
    dataAdapter = new DataAdapter();
    setOrientation(VERTICAL);
    View.inflate(context, R.layout.swipe_adapter_view_main, this);
    swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
    dockedFooterBar = (LinearLayout) View.inflate(context, R.layout.swipe_adapter_view_footer, null);
    scrollFooterBar = (LinearLayout) View.inflate(context, R.layout.swipe_adapter_view_footer, null);
    viewFlipper = (ViewFlipper) swipeRefreshLayout.findViewById(R.id.swipe_flipper);
    promptText = (TextView) viewFlipper.findViewById(R.id.swipe_prompt_text);
    promptImage = (ImageView) viewFlipper.findViewById(R.id.swipe_prompt_image);
    dataGridView = (GridView) viewFlipper.findViewById(R.id.swipe_content);
    dockedFooterText = (TextView) dockedFooterBar.findViewById(R.id.swipe_footer_text);
    dockedFooterProgress = (ProgressBar) dockedFooterBar.findViewById(R.id.swipe_footer_progress);
    scrollFooterText = (TextView) scrollFooterBar.findViewById(R.id.swipe_footer_text);
    scrollFooterProgress = (ProgressBar) scrollFooterBar.findViewById(R.id.swipe_footer_progress);
    rockerButton = (RockerButton) findViewById(R.id.rocker_button);
    changeFooter(FooterStatus.Hide, null);
    dataGridView.setAdapter(dataAdapter);
    swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    dataGridView.setOnScrollListener(onScrollListener);
    dataGridView.setOnItemClickListener(onItemClickListener);
    dockedFooterBar.setOnClickListener(onClickListener);
    scrollFooterBar.setOnClickListener(onClickListener);
    rockerButton.setOnClickListener(onClickListener);
    rockerButton.setActionListener(actionLister);
    rockerButton.setRockerMode(RockerMode.ONCE, RockerMode.ONCE, RockerMode.ONCE, RockerMode.ONCE);
  }

  private void InitCustomAttributes(AttributeSet attrs, int defStyle)
  {
    int style_view_mode, style_prompt_mode, style_footer_mode, list_transcript_mode;
    int footerHeight = context.getResources().getDimensionPixelSize(R.dimen.footer_height);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeRockerListView, defStyle, 0);
    if(a.hasValue(R.styleable.SwipeRockerListView_ViewMode))
    {
      style_view_mode = a.getInt(R.styleable.SwipeRockerListView_ViewMode, 0);
    }else
    {
      style_view_mode = 0;
    }
    if(a.hasValue(R.styleable.SwipeRockerListView_PromptMode))
    {
      style_prompt_mode = a.getInt(R.styleable.SwipeRockerListView_PromptMode, 0);
    }else
    {
      style_prompt_mode = 0;
    }
    if(a.hasValue(R.styleable.SwipeRockerListView_FooterMode))
    {
      style_footer_mode = a.getInt(R.styleable.SwipeRockerListView_FooterMode, 1);
    }else
    {
      style_footer_mode = 1;
    }
    if(a.hasValue(R.styleable.SwipeRockerListView_TranscriptMode))
    {
      list_transcript_mode = a.getInt(R.styleable.SwipeRockerListView_TranscriptMode, 1);
    }else
    {
      list_transcript_mode = 1;
    }
    if(a.hasValue(R.styleable.SwipeRockerListView_FooterHeight))
    {
      footerHeight = a.getDimensionPixelSize(R.styleable.SwipeRockerListView_FooterHeight, footerHeight);
    }
    if(a.hasValue(R.styleable.SwipeRockerListView_RockerButtonBackground))
    {
      int background = a.getResourceId(R.styleable.SwipeRockerListView_RockerButtonBackground, R.drawable.bg_rocker_button);
      rockerButton.setBackgroundResource(background);
    }
    if(a.hasValue(R.styleable.SwipeRockerListView_RockerButtonForeground))
    {
      int foreground = a.getResourceId(R.styleable.SwipeRockerListView_RockerButtonForeground, R.drawable.bg_rocker_button);
      rockerButton.setImageResource(foreground);
    }
    if(a.hasValue(R.styleable.SwipeRockerListView_FooterBackground))
    {
      footerBackground = a.getDrawable(R.styleable.SwipeRockerListView_FooterBackground);
    }
    boolean floatingButtonEnabled = !a.hasValue(R.styleable.SwipeRockerListView_RockerButtonEnabled) || a.getBoolean(R.styleable.SwipeRockerListView_RockerButtonEnabled, true);
    a.recycle();
    setRockerButtonEnable(floatingButtonEnabled);
    setViewMode(style_view_mode == 0 ? ViewMode.ListView : ViewMode.GridView);
    if(footerBackground == null)
    {
      footerBackground = getResources().getDrawable(android.R.drawable.bottom_bar);
    }
    switch(style_footer_mode)
    {
      case 1:
        setFooterMode(FooterMode.Docked);
        break;
      case 2:
        setFooterMode(FooterMode.Scrolling);
        break;
      default:
        setFooterMode(FooterMode.Disabled);
        break;
    }
    switch(list_transcript_mode)
    {
      case 1:
        setTranscriptMode(TranscriptMode.Normal);
        break;
      case 2:
        setTranscriptMode(TranscriptMode.AlwaysScroll);
        break;
      default:
        setTranscriptMode(TranscriptMode.Disabled);
        break;
    }
    switch(style_prompt_mode)
    {
      case 0:
        setPromptType(PromptType.Text);
        break;
      case 1:
        setPromptType(PromptType.Image);
        break;
      case 2:
        setPromptType(PromptType.ImageText_TB);
        break;
      case 3:
        setPromptType(PromptType.ImageText_BT);
        break;
      case 4:
        setPromptType(PromptType.ImageText_LR);
        break;
      case 5:
        setPromptType(PromptType.ImageText_RL);
        break;
    }
    LayoutParams footerBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, footerHeight);
    dockedFooterBar.setLayoutParams(footerBarLayoutParams);
    scrollFooterBar.setLayoutParams(footerBarLayoutParams);
    dockedFooterBar.setBackgroundDrawable(footerBackground);
    scrollFooterBar.setBackgroundDrawable(footerBackground);
  }

  /**
   * Set columns number of {@link #dataGridView}
   *
   * @param cols New columns number for GridView mode(min=2)
   */
  public void setColumnNum(int cols)
  {
    columnNum = cols > 1 ? cols : 2;
    if(savedViewMode == ViewMode.GridView)
    {
      dataGridView.setNumColumns(columnNum);
    }
  }

  public void setRockerButtonEnable(boolean enabled)
  {
    rockerButton.setEnabled(enabled);
    rockerButton.setVisibility(enabled ? VISIBLE : GONE);
  }

  /**
   * Set display mode of {@link #dataGridView}
   *
   * @param viewMode {@link ViewMode}
   */
  public void setViewMode(@NonNull ViewMode viewMode)
  {
    savedViewMode = viewMode;
    if(savedViewMode == ViewMode.ListView)
    {
      dataGridView.setNumColumns(1);
    }else
    {
      setFooterMode(FooterMode.Docked);
      if(columnNum < 2)
      {
        columnNum = 2;
      }
      dataGridView.setNumColumns(columnNum);
    }
  }

  /**
   * Set display mode of footerBar
   *
   * @param footerBarMode {@link FooterMode}
   */
  public void setFooterMode(@NonNull FooterMode footerBarMode)
  {
    if(!swipeRefreshLayout.isRefreshing())
    {
      int visibility = footerBarMode == FooterMode.Disabled ? GONE : VISIBLE;
      dockedFooterBar.setVisibility(visibility);
      dockedFooterText.setVisibility(visibility);
      dockedFooterProgress.setVisibility(GONE);
      scrollFooterBar.setVisibility(visibility);
      scrollFooterText.setVisibility(visibility);
      scrollFooterProgress.setVisibility(GONE);
      switch(footerBarMode)
      {
        case Disabled:
          if(savedFooterMode != FooterMode.Disabled)
          {
            if(savedFooterMode == FooterMode.Scrolling)
            {//Release {@link #scrollFooterBar} from dataGridView
              savedFooterMode = FooterMode.Disabled;
              dataAdapter.notifyDataSetChanged();
            }else if(savedFooterMode == FooterMode.Docked)
            {//Release footerBar from container
              removeView(dockedFooterBar);
              savedFooterMode = FooterMode.Disabled;
            }
          }
          break;
        case Scrolling:
          if(savedViewMode == ViewMode.ListView)
          {//GridView must use docked footer bar.
            if(savedFooterMode != FooterMode.Scrolling)
            {
              if(savedFooterMode == FooterMode.Docked)
              {//Release footerBar from container
                removeView(dockedFooterBar);
              }
              savedFooterMode = FooterMode.Scrolling;
              dataAdapter.notifyDataSetChanged();
            }
          }
          break;
        case Docked:
          if(savedFooterMode != FooterMode.Docked)
          {
            if(savedFooterMode == FooterMode.Scrolling)
            {//Release {@link #scrollFooterBar} from dataGridView
              savedFooterMode = FooterMode.Docked;
              dataAdapter.notifyDataSetChanged();
            }
            addView(dockedFooterBar);
            savedFooterMode = FooterMode.Docked;
          }
          break;
      }
    }
  }

  /**
   * Set display mode of {@link #dataGridView}
   *
   * @param transcriptMode {@link TranscriptMode}
   */
  public void setTranscriptMode(@NonNull TranscriptMode transcriptMode)
  {
    switch(transcriptMode)
    {
      case Disabled:
        dataGridView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        break;
      case Normal:
        dataGridView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        break;
      case AlwaysScroll:
        dataGridView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        break;
    }
  }

  /**
   * Set display mode of prompt message
   *
   * @param promptType {@link PromptType}
   */
  public void setPromptType(@NonNull PromptType promptType)
  {
    savedPromptType = promptType;
    switch(savedPromptType)
    {
      case Text:
        promptText.setVisibility(VISIBLE);
        promptImage.setVisibility(GONE);
        break;
      case Image:
        promptText.setVisibility(GONE);
        promptImage.setVisibility(VISIBLE);
        break;
      case ImageText_TB:
      case ImageText_LR:
      case ImageText_RL:
      case ImageText_BT:
        promptText.setVisibility(VISIBLE);
        promptImage.setVisibility(VISIBLE);
        break;
    }
  }

  /**
   * Set display content of prompt message
   *
   * @param text_res_id Resource id of text
   * @param img_res_id  Resource id of icon
   */
  public void setPrompt(int text_res_id, int img_res_id)
  {
    promptText.setText(text_res_id);
    promptImage.setImageResource(img_res_id);
  }

  /**
   * Set display content of prompt message
   *
   * @param text       Text of message
   * @param img_res_id Resource id of icon
   */
  public void setPrompt(String text, int img_res_id)
  {
    promptText.setText(text);
    promptImage.setImageResource(img_res_id);
  }

  /**
   * Set display content of prompt message
   *
   * @param text_res_id Resource id of text
   * @param img         Drawable of icon
   */
  public void setPrompt(int text_res_id, Drawable img)
  {
    promptText.setText(text_res_id);
    promptImage.setImageDrawable(img);
  }

  /**
   * Set display content of prompt message
   *
   * @param text Text of message
   * @param img  Drawable of icon
   */
  public void setPrompt(String text, Drawable img)
  {
    promptText.setText(text);
    promptImage.setImageDrawable(img);
  }

  /**
   * Set display content of prompt message
   *
   * @param text_res_id Resource id of text
   */
  public void setPromptText(int text_res_id)
  {
    promptText.setText(text_res_id);
  }

  /**
   * Set display content of prompt message
   *
   * @param text Text of message
   */
  public void setPromptText(String text)
  {
    promptText.setText(text);
  }

  /**
   * Set display content of prompt message
   *
   * @param img Drawable of icon
   */
  public void setPromptImage(Drawable img)
  {
    promptImage.setImageDrawable(img);
  }

  /**
   * Set display icon of prompt message
   *
   * @param img_res_id Resource id of icon
   */
  public void setPromptImage(int img_res_id)
  {
    promptImage.setImageResource(img_res_id);
  }

  /**
   * Set display text of footerBar
   *
   * @param text text
   */
  public void setFooterText(@Nullable String text)
  {
    dockedFooterText.setText(text);
    scrollFooterText.setText(text);
  }

  /**
   * Set an implementation of {@link List} which contains the data to display.
   *
   * @param list An implementation of {@link List}
   */
  public void setData(List list)
  {
    dataList = list;
  }

  /**
   * Set the listener of SwipeAdapterView's action
   *
   * @param listener {@link ActionListener}
   */
  public void setActionListener(@NonNull ActionListener listener)
  {
    actionListener = listener;
  }

  /**
   * Set default messages.
   *
   * @param msgBusy           Displayed in {@link Common#TOAST} / When a refresh/load-more action happen while a previous one is still in progress.
   * @param msgLoading        Displayed in Footer & Prompt / While loading data
   * @param msgRefreshSuccess Displayed in Footer / When {@link #NotifyRefreshSuccess} is called.
   * @param msgRefreshEmpty   Displayed in Footer / When {@link #NotifyRefreshEmpty} is called.
   * @param msgRefreshFailure Displayed in Footer / When {@link #NotifyRefreshFailure} is called.
   * @param msgMoreSuccess    Displayed in Footer / When {@link #NotifyMoreSuccess} is called.
   * @param msgMoreEmpty      Displayed in Footer / When {@link #NotifyMoreEmpty} is called.
   * @param msgMoreFailure    Displayed in Footer / When {@link #NotifyMoreFailure} is called.
   */
  public void setDefaultMessage(@NonNull String msgBusy, @NonNull String msgLoading, @NonNull String msgRefreshSuccess, @NonNull String msgRefreshEmpty, @NonNull String msgRefreshFailure, @NonNull String msgMoreSuccess, @NonNull String msgMoreEmpty, @NonNull String msgMoreFailure)
  {
    if(!TextUtils.isEmpty(msgBusy))
    {
      this.msgBusy = msgBusy;
    }
    if(!TextUtils.isEmpty(msgLoading))
    {
      this.msgLoading = msgLoading;
    }
    if(!TextUtils.isEmpty(msgRefreshSuccess))
    {
      this.msgRefreshSuccess = msgRefreshSuccess;
    }
    if(!TextUtils.isEmpty(msgRefreshEmpty))
    {
      this.msgRefreshEmpty = msgRefreshEmpty;
    }
    if(!TextUtils.isEmpty(msgRefreshFailure))
    {
      this.msgRefreshFailure = msgRefreshFailure;
    }
    if(!TextUtils.isEmpty(msgMoreSuccess))
    {
      this.msgMoreSuccess = msgMoreSuccess;
    }
    if(!TextUtils.isEmpty(msgMoreEmpty))
    {
      this.msgMoreEmpty = msgMoreEmpty;
    }
    if(!TextUtils.isEmpty(msgMoreFailure))
    {
      this.msgMoreFailure = msgMoreFailure;
    }
  }

  /**
   * First time to load data. Typically used when the UI component first Attached. ForEX:Activity.onStart()
   */
  public void startLoad()
  {
    if(swipeRefreshLayout != null)
    {
      swipeRefreshLayout.post(new Runnable()
      {
        @Override
        public void run()
        {
          swipeRefreshLayout.setRefreshing(true);
          onRefreshListener.onRefresh();
        }
      });
    }
  }

  /**
   * Call this in UI thread when refreshing task has succeeded with data.
   *
   * @param message Text to display on footerBar, or Null to use default string 'Success'.
   */
  public void NotifyRefreshSuccess(@Nullable String message)
  {
    if(TextUtils.isEmpty(message))
    {
      message = msgRefreshSuccess;
    }
    isLoading = false;
    swipeRefreshLayout.setRefreshing(false);
    dataAdapter.notifyDataSetChanged();
    showDataList();
    changeFooter(FooterStatus.Loaded, message);
  }

  /**
   * Call this in UI thread when refreshing task has succeeded with no data.
   *
   * @param message Text to display on footerBar, or Null to use default string 'Empty'.
   */
  public void NotifyRefreshEmpty(@Nullable String message)
  {
    if(TextUtils.isEmpty(message))
    {
      message = msgRefreshEmpty;
    }
    isLoading = false;
    swipeRefreshLayout.setRefreshing(false);
    changeFooter(FooterStatus.Hide, message);
  }

  /**
   * Call this in UI thread when refreshing task has failed.
   *
   * @param message Text to display on footerBar, or Null to use default string 'Failure'.
   */
  public void NotifyRefreshFailure(@Nullable String message)
  {
    if(TextUtils.isEmpty(message))
    {
      message = msgRefreshFailure;
    }
    isLoading = false;
    swipeRefreshLayout.setRefreshing(false);
    changeFooter(FooterStatus.Hide, message);
  }

  /**
   * Call this in UI thread when load-more task has succeeded with data.
   *
   * @param message Text to display on footerBar, or Null to use default string 'Success'.
   */
  public void NotifyMoreSuccess(@Nullable String message)
  {
    if(TextUtils.isEmpty(message))
    {
      message = msgMoreSuccess;
    }
    isLoading = false;
    dataAdapter.notifyDataSetChanged();
    showDataList();
    changeFooter(FooterStatus.Loaded, message);
  }

  /**
   * Call this in UI thread when load-more task has succeeded with no data.
   *
   * @param message Text to display on footerBar, or Null to use default string 'Empty'.
   */
  public void NotifyMoreEmpty(@Nullable String message)
  {
    if(TextUtils.isEmpty(message))
    {
      message = msgMoreEmpty;
    }
    isLoading = false;
    changeFooter(FooterStatus.Empty, message);
  }

  /**
   * Call this in UI thread when load-more task has failed.
   *
   * @param message Text to display on footerBar, or Null to use default string 'Failure'.
   */
  public void NotifyMoreFailure(@Nullable String message)
  {
    if(TextUtils.isEmpty(message))
    {
      message = msgMoreFailure;
    }
    isLoading = false;
    changeFooter(FooterStatus.Fail, message);
  }

  /**
   * Switch {@link #viewFlipper} to show the page of data list.
   */
  private void showDataList()
  {
    int curPage = viewFlipper.getDisplayedChild();
    if(curPage != 0)
    {
      viewFlipper.setDisplayedChild(0);
    }
  }

  /**
   * Switch {@link #viewFlipper} to show the page of prompt having current text not changed.
   */
  private void showPrompt()
  {
    showPrompt(null);
  }

  /**
   * Switch {@link #viewFlipper} to show the page of prompt with new text.
   */
  private void showPrompt(@Nullable String message)
  {
    int curPage = viewFlipper.getDisplayedChild();
    if(curPage != 1)
    {
      viewFlipper.setDisplayedChild(1);
      if(!TextUtils.isEmpty(message))
      {
        setPromptText(message);
      }
    }
  }

  /**
   * Switch {@link #viewFlipper} to show the page of bug hint.<br>
   * This page is used to show only when an unhandled internal exception occurred.
   */
  private void ShowBug()
  {
    int curPage = viewFlipper.getDisplayedChild();
    if(curPage != 2)
    {
      viewFlipper.setDisplayedChild(2);
    }
  }

  /**
   * Change status and text of  footerBar.
   *
   * @param status  New status.{@link FooterStatus}
   * @param message New text to display
   */
  private void changeFooter(@NonNull FooterStatus status, @Nullable String message)
  {
    setFooterText(message);
    if(savedFooterMode != FooterMode.Disabled)
    {
      switch(status)
      {
        case Hide:
          dockedFooterBar.setVisibility(GONE);
          scrollFooterBar.setVisibility(GONE);
          break;
        case Loading:
          dockedFooterBar.setVisibility(VISIBLE);
          dockedFooterProgress.setVisibility(VISIBLE);
          scrollFooterBar.setVisibility(VISIBLE);
          scrollFooterProgress.setVisibility(VISIBLE);
          break;
        case Loaded:
          dockedFooterBar.setVisibility(VISIBLE);
          dockedFooterProgress.setVisibility(GONE);
          scrollFooterBar.setVisibility(VISIBLE);
          scrollFooterProgress.setVisibility(GONE);
          break;
        case Empty:
          dockedFooterBar.setVisibility(VISIBLE);
          dockedFooterProgress.setVisibility(GONE);
          scrollFooterBar.setVisibility(VISIBLE);
          scrollFooterProgress.setVisibility(GONE);
          break;
        case Fail:
          dockedFooterBar.setVisibility(VISIBLE);
          dockedFooterProgress.setVisibility(GONE);
          scrollFooterBar.setVisibility(VISIBLE);
          scrollFooterProgress.setVisibility(GONE);
          break;
      }
    }
  }
}