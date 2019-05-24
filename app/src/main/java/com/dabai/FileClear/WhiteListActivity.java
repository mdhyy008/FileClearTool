package com.dabai.FileClear;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;



import java.util.HashSet;

import java.util.Set;


public class WhiteListActivity extends AppCompatActivity {

    ListView lv;
    ArrayAdapter adapter;
    private HashSet<String> WhiteList;
    private String TAG = "MainActivity";

int por;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_list);

        lv = findViewById(R.id.lv);
        getSupportActionBar().setTitle("一键清理白名单");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

          por = position;
                new AlertDialog.Builder(WhiteListActivity.this).setTitle(getString(R.string.del_tip)).setMessage(getString(R.string.yaoba)+WhiteList.toArray()[position]+getString(R.string.yichu)).setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WhiteList.remove(WhiteList.toArray()[por]);

                        data_f5();  sub_data();
                        Toast.makeText(WhiteListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                }).show();


            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        data_f5();
    }


    void del_data(String data) {
        WhiteList.remove(data);
        sub_data();
        data_f5();
    }

    void sub_data() {

        SharedPreferences sp = this.getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("WhiteList", WhiteList);
        editor.putBoolean("first", false);
        editor.commit();

    }

    void data_f5() {

        SharedPreferences sp = this.getSharedPreferences("data", 0);
        if (sp.getBoolean("first", true)) {
            Toast.makeText(this, "请添加规则", Toast.LENGTH_SHORT).show();
        } else {

            Set<String> se = new HashSet<>();
            WhiteList = (HashSet<String>) sp.getStringSet("WhiteList", se);

            if (WhiteList.size() <= 0){
                findViewById(R.id.textView6).setVisibility(View.VISIBLE);
            }else {
                findViewById(R.id.textView6).setVisibility(View.GONE);

            }

            Log.d(TAG, "onCreate: " + WhiteList);
            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, WhiteList.toArray());
            lv.setAdapter(adapter);
        }

    }

}
