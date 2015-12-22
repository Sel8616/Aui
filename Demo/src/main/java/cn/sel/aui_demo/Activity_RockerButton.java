package cn.sel.aui_demo;

import android.app.Activity;
import android.os.Bundle;

import cn.sel.aui.Common;
import cn.sel.aui.RockerButton;

public class Activity_RockerButton extends Activity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_rocker);
    RockerButton rockerButton = (RockerButton) findViewById(R.id.rocker_button);
    rockerButton.setActionListener(new RockerButton.ActionLister()
    {
      @Override
      public void onLeft()
      {
        Common.ShowMessage(Activity_RockerButton.this, "LEFT");
      }

      @Override
      public void onRight()
      {
        Common.ShowMessage(Activity_RockerButton.this, "RIGHT");
      }

      @Override
      public void onUp()
      {
        Common.ShowMessage(Activity_RockerButton.this, "UP");
      }

      @Override
      public void onDown()
      {
        Common.ShowMessage(Activity_RockerButton.this, "DOWN");
      }
    });
  }
}