package cn.sel.aui_demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.sel.aui.SwipeRockerListView;

public class Activity_SwipeRocker extends Activity implements SwipeRockerListView.ActionListener
{
  private List<String> data_all;
  private List<String> data_got;
  private List<String> buffer;
  private int pageSize;
  private int cols;
  private SwipeRockerListView swipe;
  private Toast toast;
  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_swipe);
    swipe = (SwipeRockerListView) findViewById(R.id.aui_swipe);
    cols = 1;
    pageSize = 12;
    buffer = new ArrayList<>(pageSize);
    data_all = new ArrayList<>(50);
    for(int i = 0; i < 50; i++)
    {
      data_all.add("This is Item " + (i + 1));
    }
    data_got = new ArrayList<>();
    swipe.setData(data_got);
    swipe.setActionListener(this);
    swipe.setColumnNum(cols);
    handler = new Handler()
    {
      @Override
      public void handleMessage(Message msg)
      {
        switch(msg.what)
        {
          case 0:
            data_got.addAll(buffer);
            swipe.NotifyRefreshSuccess("Success! " + data_got.size() + "Loaded. Click to load more.");
            break;
          case 1:
            swipe.NotifyRefreshEmpty("No data currently! Click to load more.");
            break;
          case 2:
            data_got.addAll(buffer);
            swipe.NotifyMoreSuccess("Success! " + data_got.size() + "Loaded. Click to load more.");
            break;
          case 3:
            swipe.NotifyMoreEmpty("No more currently! Click to load more.");
            break;
        }
      }
    };
  }

  @Override
  public void onStart()
  {
    super.onStart();
    swipe.startLoad();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    new MenuInflater(this).inflate(R.menu.swipe, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case R.id.Menu_ViewMode_List:
        swipe.setViewMode(SwipeRockerListView.ViewMode.ListView);
        break;
      case R.id.Menu_ViewMode_Grid:
        swipe.setViewMode(SwipeRockerListView.ViewMode.GridView);
        break;
      case R.id.Menu_Column_Add:
        swipe.setColumnNum(++cols);
        break;
      case R.id.Menu_Column_Minus:
        if(cols > 1)
        {
          cols--;
        }
        swipe.setColumnNum(cols);
        break;
      case R.id.Menu_FooterMode_Docked:
        swipe.setFooterMode(SwipeRockerListView.FooterMode.Docked);
        break;
      case R.id.Menu_FooterMode_Scrolling:
        swipe.setFooterMode(SwipeRockerListView.FooterMode.Scrolling);
        break;
      case R.id.Menu_FooterMode_Disabled:
        swipe.setFooterMode(SwipeRockerListView.FooterMode.Disabled);
        break;
      case R.id.Menu_TranscriptMode_Disabled:
        swipe.setTranscriptMode(SwipeRockerListView.TranscriptMode.Disabled);
        break;
      case R.id.Menu_TranscriptMode_Normal:
        swipe.setTranscriptMode(SwipeRockerListView.TranscriptMode.Normal);
        break;
      case R.id.Menu_TranscriptMode_AlwaysScroll:
        swipe.setTranscriptMode(SwipeRockerListView.TranscriptMode.AlwaysScroll);
        break;
      case R.id.Menu_Rocker_Enabled:
        swipe.setRockerButtonEnable(true);
        break;
      case R.id.Menu_Rocker_Disabled:
        swipe.setRockerButtonEnable(false);
        break;
      default:
        break;
    }
    return true;
  }

  @Override
  public void onRefresh()
  {
    LoadData(true);
  }

  @Override
  public void onMore()
  {
    LoadData(false);
  }

  @Override
  public void onItemSelect(int position, Object data, View view)
  {
    if(toast != null)
    {
      toast.cancel();
    }
    toast = Toast.makeText(this, "SwipeAdapterView_Demo\nonItemSelect:" + position + "\nData:" + data.toString(), Toast.LENGTH_SHORT);
    toast.show();
  }

  @Override
  public View getViewTemplate()
  {
    return View.inflate(this, R.layout.swipe_item, null);
  }

  @Override
  public Object getViewHolder(View convertView)
  {
    ViewHolder viewHolder = new ViewHolder();
    viewHolder.tv_no = (TextView) convertView.findViewById(R.id.tv_no);
    viewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_data);
    return viewHolder;
  }

  @Override
  public void fillCurrentItem(Object viewHolder, int position)
  {
    ((ViewHolder) viewHolder).tv_no.setText(String.valueOf(position + 1));
    ((ViewHolder) viewHolder).tv_content.setText(data_got.get(position));
  }

  private void LoadData(final boolean refresh)
  {
    buffer.clear();
    if(refresh)
    {
      data_got.clear();
    }
    Thread thread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        int count = data_got.size();
        int total = data_all.size();
        for(int i = count; i < count + pageSize && i < total; i++)
        {
          buffer.add(data_all.get(i));
        }
        if(buffer.size() > 0)
        {//Success
          if(refresh)
          {
            handler.sendEmptyMessageDelayed(0, 1000);
          }else
          {
            handler.sendEmptyMessageDelayed(2, 1000);
          }
        }else
        {//Empty
          if(refresh)
          {
            handler.sendEmptyMessageDelayed(1, 1000);
          }else
          {
            handler.sendEmptyMessageDelayed(3, 1000);
          }
        }
      }
    });
    thread.start();
  }

  class ViewHolder
  {
    public TextView tv_no, tv_content;
  }
}