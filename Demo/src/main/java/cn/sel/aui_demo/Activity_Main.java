package cn.sel.aui_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class Activity_Main extends Activity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ListView listView = (ListView) findViewById(R.id.list_main);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
        Intent intent = null;
        switch(position)
        {
          case 0:
            intent = new Intent(Activity_Main.this, Activity_SwipeRocker.class);
            break;
          case 1:
            intent = new Intent(Activity_Main.this, Activity_PictureView.class);
            break;
          case 2:
            intent = new Intent(Activity_Main.this, Activity_ViewLooper.class);
            break;
          case 3:
            intent = new Intent(Activity_Main.this, Activity_RockerButton.class);
            break;
          default:
            Toast.makeText(Activity_Main.this, "Coming soon...", Toast.LENGTH_SHORT).show();
            break;
        }
        if(intent != null)
        {
          Activity_Main.this.startActivity(intent);
        }
      }
    });
  }
}