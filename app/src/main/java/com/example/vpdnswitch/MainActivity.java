package com.example.vpdnswitch;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.util.ApnUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {
    private CornerListView cornerListView = null;
    private ArrayList<HashMap<String, String>> mapList = null;
    private SimpleAdapter simpleAdapter = null;
    private ApnUtility apnutility = null;
    // 在Android 4.2 以及以上版本：
//	private static final String[] APN_PROJECTION = {
//	     Telephony.Carriers.TYPE,            // 0
//	     Telephony.Carriers.MMSC,            // 1
//	     Telephony.Carriers.MMSPROXY,        // 2
//	     Telephony.Carriers.MMSPORT          // 3
//	 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置窗口特征
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_apn);

        apnutility = new ApnUtility(this);

        simpleAdapter = new SimpleAdapter(this, getDataSource(), R.layout.simple_list_item_1, new String[]{"item_title", "item_value"}, new int[]{R.id.item_title});

        cornerListView = (CornerListView) findViewById(R.id.apn_list);
        cornerListView.setAdapter(simpleAdapter);
        cornerListView.setOnItemClickListener(new OnItemListSelectedListener());

        // 请求获取VPN设置权限
        // 要求com.android.support:appcompat-v7:23.2.1 至少23
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_APN_SETTINGS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //requesting permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_APN_SETTINGS}, 1);
        } else {
            //permission is granted and you can change APN settings
            Log.i("andli", "权限已经成功获取!");
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //premission granted by user
                    Log.i("andli", "用户同意授权!");
                } else {
                    //permission denied by user
                    Log.i("andli", "用户禁止授权!");
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    // 设置列表数据
    public ArrayList<HashMap<String, String>> getDataSource() {
        mapList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("item_title", "设置APN选项");
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("item_title", "编辑APN内容");
        mapList.add(map1);
        mapList.add(map2);

        return mapList;
    }

    // ListView事件监听器
    class OnItemListSelectedListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            switch (position) {
                case 0:
                    openApnActivity();
                    break;
                case 1:
                    editMobileApn();
                    break;
            }
        }
    }

    // 设置APN选项
    private void openApnActivity() {
        Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
        startActivity(intent);
    }


//	final Cursor apnCursor =SqliteWrapper.query(context, this.context.getContentResolver(), Uri.withAppendedPath(Carriers.CONTENT_URI, "current"), APN_PROJECTION, null, null, null);

    // 编辑APN内容
    private void editMobileApn() {
        int id = -1;
        Uri uri = Uri.parse("content://telephony/carriers");
        ContentResolver resolver = getContentResolver();
        try {
            // 针对Android4.0以下版本
            Cursor c = resolver.query(uri, new String[]{"_id", "name", "apn"}, "apn like '%hnydz.ha%'", null, null);

            // 该项APN存在
            if (c != null && c.moveToNext()) {
                id = c.getShort(c.getColumnIndex("_id"));
                String name = c.getString(c.getColumnIndex("name"));
                String apn = c.getString(c.getColumnIndex("apn"));

                Log.v("APN", id + name + apn);

                Uri uri1 = Uri.parse("content://telephony/carriers/" + id);

                Intent intent = new Intent(Intent.ACTION_EDIT, uri1);
                startActivity(intent);
                apnutility.setDefaultApn(id);
            } else {
                // 如果不存在该项APN则进行添加
                apnutility.setDefaultApn(apnutility.AddYidongApn());
                Toast.makeText(getApplicationContext(), "再次点击APN内容即可编辑！", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无权限操作!", Toast.LENGTH_SHORT).show();
        }


    }
}