package cn.sel.AuiDemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import cn.sel.aui.ViewLooper;

public class Activity_ViewLooper extends Activity
{
  private ArrayList<View> views;
  private ViewLooper viewlooper;
  private Context context;
  private int count;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_looper);
    context = this;
    views = new ArrayList<>();
    viewlooper = (ViewLooper) findViewById(R.id.aui_looper);
    Button btn_add = (Button) findViewById(R.id.btn_add);
    Button btn_del = (Button) findViewById(R.id.btn_del);
    btn_add.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        TextView tv = new TextView(context);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(64);
        tv.setText("PAGE\n" + count);
        views.add(tv);
        count++;
        viewlooper.Update();
      }
    });
    btn_del.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if(views.size() > 0)
        {
          views.remove(views.size() - 1);
          count--;
          viewlooper.Update();
        }
      }
    });
    viewlooper.SetLoopingViews(views);
  }
}