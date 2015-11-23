package cn.sel.SwipeAdapterView_Demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.sel.android.ui.SwipeAdapterView;


public class Activity_SwipeAdapterView extends Activity implements SwipeAdapterView.ActionListener
{
  private List<String> data_all;
  private List<String> data_got;
  private List<String> buffer;
  private int pageSize;
  private int cols;
  private SwipeAdapterView swipe;
  private Toast toast;
  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    swipe = (SwipeAdapterView) findViewById(R.id.asharp_swipe_layout);
    cols = 1;
    pageSize = 12;
    buffer = new ArrayList<>(pageSize);
    data_all = new ArrayList<>(50);
    for(int i = 0; i < 50; i++)
    {
      data_all.add("This is Item " + (i + 1));
    }
    data_got = new ArrayList<>();
    final DataAdapter adapter = new DataAdapter();
    swipe.SetDataAdapter(adapter);
    swipe.SetActionListener(this);
    swipe.SetColumnNum(cols);
    handler = new Handler()
    {
      @Override
      public void handleMessage(Message msg)
      {
        switch(msg.what)
        {
          case 0:
            data_got.addAll(buffer);
            adapter.notifyDataSetChanged();
            swipe.NotifyRefreshSuccess("Success! " + data_got.size() + "Loaded. Click to load more.");
            break;
          case 1:
            swipe.NotifyRefreshEmpty("No data currently! Click to load more.");
            break;
          case 2:
            data_got.addAll(buffer);
            adapter.notifyDataSetChanged();
            swipe.NotifyNextSuccess("Success! " + data_got.size() + "Loaded. Click to load more.");
            break;
          case 3:
            swipe.NotifyNextEmpty("No more currently! Click to load more.");
            break;
        }
      }
    };
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    menu.add(Menu.NONE, 0, 0, "ListView").setIcon(android.R.drawable.ic_menu_sort_by_size);
    menu.add(Menu.NONE, 1, 1, "GridView").setIcon(android.R.drawable.ic_dialog_dialer);
    menu.add(Menu.NONE, 2, 2, "+Column").setIcon(android.R.drawable.arrow_up_float);
    menu.add(Menu.NONE, 3, 3, "-Column").setIcon(android.R.drawable.arrow_down_float);
    menu.add(Menu.NONE, 4, 4, "SwitchFooter").setIcon(android.R.drawable.ic_menu_upload);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case 0:
        swipe.SetViewMode(SwipeAdapterView.ViewMode.ListView);
        break;
      case 1:
        swipe.SetViewMode(SwipeAdapterView.ViewMode.GridView);
        break;
      case 2:
        swipe.SetColumnNum(++cols);
        break;
      case 3:
        if(cols > 1)
          cols--;
        swipe.SetColumnNum(cols);
        break;
      case 4:
        swipe.SetFooterEnabled(!swipe.GetFooterEnabled());
        break;
      default:
        break;
    }
    return true;
  }

  @Override
  public void OnRefreshing()
  {
    Log.i("SwipeAdapterView_Demo", "OnRefreshing");
    LoadData(true);
  }

  @Override
  public void OnRefreshed(boolean success)
  {
    Log.i("SwipeAdapterView_Demo", "OnRefreshed: " + success);
  }

  @Override
  public void OnNexting()
  {
    Log.i("SwipeAdapterView_Demo", "OnNexting");
    LoadData(false);
  }

  @Override
  public void OnNexted(boolean success)
  {
    Log.i("SwipeAdapterView_Demo", "OnNexted: " + success);
  }

  @Override
  public void OnItemSelect(int position, Object data, View view)
  {
    Log.i("SwipeAdapterView_Demo", "OnItemSelect: " + position);
    if(toast != null)
    {
      toast.cancel();
    }
    toast = Toast.makeText(this, "SwipeAdapterView_Demo\nOnItemSelect:" + position + "\nData:" + data.toString(), Toast.LENGTH_SHORT);
    toast.show();
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
            handler.sendEmptyMessageDelayed(1, 1000);
          else
            handler.sendEmptyMessageDelayed(3, 1000);
        }
      }
    });
    thread.start();
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private class DataAdapter extends BaseAdapter
  {
    private int tv_no_id = generateViewId(0);
    private int tv_content_id = generateViewId(1);

    public int generateViewId(int tag)
    {
      for(; ; )
      {
        final AtomicInteger atomicInteger = new AtomicInteger(tag);
        final int result = atomicInteger.get();
        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
        int newValue = result + 1;
        if(newValue > 0x00FFFFFF)
          newValue = 1; // Roll over to 1, not 0.
        if(atomicInteger.compareAndSet(result, newValue))
        {
          return result;
        }
      }
    }

    @Override
    public int getCount()
    {
      return data_got.size();
    }

    @Override
    public Object getItem(int position)
    {
      return data_got.get(position);
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      ViewHolder viewHolder;
      if(convertView == null)
      {
        //Generate test layout
        LinearLayout layout = new LinearLayout(Activity_SwipeAdapterView.this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, GetPixFromDip(Activity_SwipeAdapterView.this, 48)));
        TextView tv_no = new TextView(Activity_SwipeAdapterView.this);
        TextView tv_content = new TextView(Activity_SwipeAdapterView.this);
        tv_no.setId(tv_no_id);
        tv_content.setId(tv_content_id);
        tv_no.setPadding(10, 0, 10, 0);
        tv_content.setPadding(10, 0, 10, 0);
        tv_no.setGravity(Gravity.CENTER);
        tv_content.setGravity(Gravity.CENTER_VERTICAL);
        tv_no.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv_content.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        layout.addView(tv_no);
        layout.addView(tv_content);
        //
        convertView = layout;
        viewHolder = new ViewHolder();
        viewHolder.tv_no = (TextView) convertView.findViewById(tv_no_id);
        viewHolder.tv_content = (TextView) convertView.findViewById(tv_content_id);
        convertView.setTag(viewHolder);
      }else
      {
        viewHolder = (ViewHolder) convertView.getTag();
      }
      // 列表序号，按显示顺序从1递增（不是数据的编号）
      viewHolder.tv_no.setText(String.valueOf(position + 1));
      viewHolder.tv_content.setText(data_got.get(position));
      return convertView;
    }

    class ViewHolder
    {
      public TextView tv_no, tv_content;
    }
  }


  /**
   * Dip 2 Pix
   *
   * @param context Context
   * @param dip     Dip
   *
   * @return Pixels
   */
  public int GetPixFromDip(Context context, int dip)
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