package cn.sel.aui_demo;

import android.app.Activity;
import android.os.Bundle;

import cn.sel.aui.Common;
import cn.sel.aui.RockerButton;

public class Activity_RockerButton extends Activity
{
  final RockerButton.ActionLister actionListener = new RockerButton.ActionLister()
  {
    @Override
    public void onLeft(float traction)
    {
      Common.ShowMessage(Activity_RockerButton.this, "LEFT:" + traction);
    }

    @Override
    public void onRight(float traction)
    {
      Common.ShowMessage(Activity_RockerButton.this, "RIGHT:" + traction);
    }

    @Override
    public void onUp(float traction)
    {
      Common.ShowMessage(Activity_RockerButton.this, "UP:" + traction);
    }

    @Override
    public void onDown(float traction)
    {
      Common.ShowMessage(Activity_RockerButton.this, "DOWN:" + traction);
    }

    @Override
    public void onStop(RockerButton.Direction direction)
    {
      Common.ShowMessage(Activity_RockerButton.this, "STOP:");
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_rocker);
    RockerButton rockerButton1 = (RockerButton) findViewById(R.id.rocker_button1);
    RockerButton rockerButton2 = (RockerButton) findViewById(R.id.rocker_button2);
    RockerButton rockerButton3 = (RockerButton) findViewById(R.id.rocker_button3);
    RockerButton rockerButton4 = (RockerButton) findViewById(R.id.rocker_button4);
    rockerButton1.setRockerMode(RockerButton.RockerMode.ONCE, RockerButton.RockerMode.ONCE, RockerButton.RockerMode.ONCE, RockerButton.RockerMode.ONCE);
    rockerButton2.setRockerMode(RockerButton.RockerMode.ONCE, RockerButton.RockerMode.ONCE, RockerButton.RockerMode.ELASTIC, RockerButton.RockerMode.ELASTIC);
    rockerButton3.setRockerMode(RockerButton.RockerMode.ONCE, RockerButton.RockerMode.ONCE, RockerButton.RockerMode.ELASTIC, RockerButton.RockerMode.ELASTIC);
    rockerButton4.setRockerMode(RockerButton.RockerMode.ELASTIC, RockerButton.RockerMode.ELASTIC, RockerButton.RockerMode.ELASTIC, RockerButton.RockerMode.ELASTIC);
    rockerButton1.setActionListener(actionListener);
    rockerButton2.setActionListener(actionListener);
    rockerButton3.setActionListener(actionListener);
    rockerButton4.setActionListener(actionListener);
  }
}