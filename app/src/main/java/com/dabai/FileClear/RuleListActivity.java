package com.dabai.FileClear;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

public class RuleListActivity extends AppCompatActivity {

    List<String> data;
    ListView lv;
    ArrayAdapter arrayAdapter;
    private View dia_v;
    private AlertDialog.Builder dia_add;
    private AlertDialog dia_add_alert;


    ArrayList<File> nodel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_list);
        data = new ArrayList<>();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nodel = new ArrayList<>();
//不推荐你添加的目录
        nodel.add(new File("/sdcard/"));
        nodel.add(new File("/system/"));
        nodel.add(new File("/"));


        //判断是不是第一次打开
        SharedPreferences sp = this.getSharedPreferences("data", 0);
        if (sp.getBoolean("first", true)) {
            Toast.makeText(this, "请添加规则", Toast.LENGTH_SHORT).show();
        } else {
            String Json = sp.getString("rule", "");
            if (null != Json) {
                Gson gson = new Gson();
                data = gson.fromJson(Json, new TypeToken<List>() {
                }.getType());
            }
        }


        lv = findViewById(R.id.lv);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                StringBuffer text = new StringBuffer();
                File file = new File(data.get(position).toString());

                text.append("FileName : ");
                if (file.exists()) {
                    text.append(file.getName());
                } else {
                    text.append("Null");
                }


                text.append("\nExists : ");
                if (file.exists()) {
                    text.append("存在");
                } else {
                    text.append("不存在");
                }

                text.append("\nWrite : ");
                if (file.canWrite()) {
                    text.append("可写");
                } else {
                    text.append("不可写");
                }

                text.append("\nRead : ");
                if (file.canRead()) {
                    text.append("可读");
                } else {
                    text.append("不可读");
                }

                text.append("\nType : ");
                if (file.isDirectory()) {
                    text.append("Folder");
                } else {
                    text.append("File");
                }


                new AlertDialog.Builder(RuleListActivity.this).setTitle("文件信息").setMessage(text.toString())
                        .setPositiveButton("确定", null)
                        .setCancelable(false)
                        .setNeutralButton("移除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                data.remove(position);
                                data_f5();
                            }
                        })
                        .show();


                //断点 DEBUG
            }
        });

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data);
        lv.setAdapter(arrayAdapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                finish();
                return true;

            case R.id.action_add:

                dia_v = getLayoutInflater().inflate(R.layout.dialog_add_rule, null);
                dia_add = new AlertDialog.Builder(this);
                dia_add_alert = dia_add.setView(dia_v).show();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rule_list, menu);
        return true;
    }


    public void data_add(String url) {

        for (File f : nodel) {
            if (new File(url).getAbsolutePath().equals(f.getAbsolutePath())) {
                Snackbar.make(getWindow().getDecorView(), "这个目录不能添加", Snackbar.LENGTH_SHORT).show();
                return;
            }
        }


        data.add(url);
        Snackbar.make(getWindow().getDecorView(), "添加成功", Snackbar.LENGTH_SHORT).show();

        data_f5();
    }

    public void data_f5() {
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data);
        lv.setAdapter(arrayAdapter);

        if (data.size() <= 0) {
            findViewById(R.id.textView13).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.textView13).setVisibility(View.GONE);

        }

        //把list存进数据库
        Gson gson = new Gson();
        String Json = gson.toJson(data);

        SharedPreferences sp = this.getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("rule", Json);
        editor.putBoolean("first", false);
        editor.commit();

    }

    public void showDialog(String title, String text) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("OK", null).show();
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    public void add_rlue(View view) {
        TextInputLayout til1 = dia_v.findViewById(R.id.dia_add_rlue_edinput);
        String file_path = til1.getEditText().getText().toString();
        File file = new File(file_path);

        for (int i = 0; i < data.size(); i++) {
            File file2 = new File(data.get(i));
            if (file.getAbsolutePath().equals(file2.getAbsolutePath())) {
                til1.setError("目标路径已经存在");
                return;
            }

        }

        data_add(file_path);
        dia_add_alert.dismiss();
    }

    public void check_file(View view) {
        TextInputLayout til1 = dia_v.findViewById(R.id.dia_add_rlue_edinput);
        String filepath = til1.getEditText().getText().toString();
        File file = new File(filepath);
        if (file.exists()) {
            showToast("存在");
        } else {
            showToast("不存在");
        }

    }


}


