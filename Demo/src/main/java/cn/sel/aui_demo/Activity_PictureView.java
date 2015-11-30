package cn.sel.aui_demo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.FileNotFoundException;

import cn.sel.aui.PictureView;

public class Activity_PictureView extends Activity implements View.OnClickListener
{
  private EditText input_Url;
  private PictureView pictureView;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pic);
    input_Url = (EditText) findViewById(R.id.input_url);
    input_Url.setText("https://git.oschina.net/sel/R/raw/master/img/aui.png");
    pictureView = (PictureView) findViewById(R.id.pictureView);
    findViewById(R.id.btn_url).setOnClickListener(this);
    findViewById(R.id.btn_choose).setOnClickListener(this);
  }

  @Override
  public void onClick(View v)
  {
    switch(v.getId())
    {
      case R.id.btn_url:
        pictureView.SetImageURL(input_Url.getText().toString());
        break;
      case R.id.btn_choose:
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), 0);
        break;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    switch(resultCode)
    {
      case RESULT_OK:
        try
        {
          Uri uri = data.getData();
          ContentResolver cr = this.getContentResolver();
          pictureView.SetImage(new BitmapDrawable(getResources(), cr.openInputStream(uri)));
        }catch(FileNotFoundException e)
        {
          e.printStackTrace();
        }
        break;
      default:
        break;
    }
  }
}