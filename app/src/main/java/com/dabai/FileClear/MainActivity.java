package com.dabai.FileClear;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
//import android.support.design.widget.FloatingActionButton;


public class MainActivity extends AppCompatActivity {


    //控件声明
    CardView setting_window;
    FloatingActionButton b1;
    FloatingActionButton b2;
    ProgressBar pr_bar, pr_bar1;
    FloatingActionsMenu fam;
    Button add_white;

    StringBuffer text;
    TextView tv4;


    private Context context;
    private ArrayAdapter arrayAdapter;
    List<String> data;
    ListView mainlv;

    LinearLayout main_line2;

    //变量声明

    boolean isTwoOk = true;
    boolean isDelDir = false;


    //所有文件
    ArrayList<File> FileList;
    HashSet<String> WhiteList;

    //待删除
    ArrayList FileList_del;

    long tag_size = 0;
    final static String TAG = "MainActivity";

    private File file;
    private Thread th;
    private volatile boolean thread_exit = false;
    private long totalsize;
    private Switch sw1, sw2;
    public int positio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        context = getApplicationContext();

        //文件清单和白名单
        FileList_del = new ArrayList<>();

        FileList = new ArrayList<>();
        WhiteList = new HashSet<>();

        init_val();
        init();


        pr_bar1 = findViewById(R.id.progressBar2);

        SharedPreferences sp = this.getSharedPreferences("data", 0);
        if (sp.getBoolean("first", true)) {
            //第一次打开
            sp.edit().putBoolean("first", false);
            sp.edit().commit();
            //Toast.makeText(context, "第一次打开", Toast.LENGTH_SHORT).show();

        }


        //设置事件监听
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                del_all_file();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * 关闭&打开设置弹窗
                 */
                if (setting_window.getY() == 0) {
                    ObjectAnimator.ofFloat(setting_window, "translationY", 0, -1500).setDuration(400).start();
                    ObjectAnimator.ofFloat(main_line2, "translationX", -1500, 0).setDuration(400).start();

                } else {
                    ObjectAnimator.ofFloat(setting_window, "translationY", -1500, 0).setDuration(400).start();
                    ObjectAnimator.ofFloat(main_line2, "translationX", 0, -1500).setDuration(400).start();

                }

            }
        });


    }


    //禁用白名单
    private void refDelFileList() {

        FileList_del.clear();

        for (int i = 0; i < FileList.size(); i++) {

            for (String a : WhiteList) {
                if (FileList.get(i).getAbsolutePath().equals(a)) {
                    Log.d(TAG, "refDelFileList: 这是一个白名单");
                    return;
                }
                FileList_del.add(FileList.get(i));

            }

        }
        Log.d(TAG, "refDelFileList: " + FileList_del);


    }


    public void refSW() {

        SharedPreferences sp = this.getSharedPreferences("data", 0);
        if (sp.getBoolean("sw1", true)) {
            sw1.setChecked(true);


            isDelDir = false;
        } else {
            sw1.setChecked(false);

            isDelDir = true;
        }
        if (sp.getBoolean("sw2", true)) {
            sw2.setChecked(true);
            isTwoOk = true;
        } else {
            sw2.setChecked(false);
            isTwoOk = false;

        }

    }

    private void init() {
        setting_window.setY(-1500);
        data = new ArrayList();
        getData();

        refSW();

        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    SharedPreferences sp = MainActivity.this.getSharedPreferences("data", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("sw1", isChecked);
                    editor.commit();
                } else {
                    SharedPreferences sp = MainActivity.this.getSharedPreferences("data", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("sw1", isChecked);
                    editor.commit();
                }
                refSW();

            }
        });

        sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences sp = MainActivity.this.getSharedPreferences("data", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("sw2", isChecked);
                    editor.commit();
                } else {
                    SharedPreferences sp = MainActivity.this.getSharedPreferences("data", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("sw2", isChecked);
                    editor.commit();
                }
                refSW();

            }
        });


        mainlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                positio = position;
                if (th != null) {
                    if (th.isAlive()) {
                        showToast("正在计算上一个对象的大小，请稍等");
                        return;
                    }
                }

                refInfo();

            }
        });


    }

    private void refInfo() {


        //点的快会崩溃

        fam.collapse();
        text = new StringBuffer();

        file = new File(data.get(positio).toString());

        text.append("FileName : ");
        if (file.exists()) {
            text.append(file.getName());
        } else {
            text.append("Null");
        }

        text.append("\nSize : ");
        if (file.exists()) {
            text.append("正在计算目标大小...");

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


        tv4 = findViewById(R.id.textView4);
        tv4.setText(text.toString());
        pr_bar.setVisibility(View.VISIBLE);

                /*
                int index = Collections.frequency(WhiteList, file.getAbsolutePath());

                if (index != 0) {
                    add_white.setVisibility(View.GONE);
                } else {
                    add_white.setVisibility(View.VISIBLE);
                }
*/


        th = new Thread(new Runnable() {
            @Override
            public void run() {


                tag_size = getTotalSizeOfFilesInDir(file);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pr_bar.setVisibility(View.GONE);
                    }
                });


                if (tag_size > 1073741824) {
                    double size = tag_size / 1024 / 1024 / 1024;
                    String tag_text = text.toString().replace("正在计算目标大小...", Math.ceil(size) + "GB");
                    tv4.setText(tag_text);
                    return;
                }

                if (tag_size > 1048576) {
                    double size = tag_size / 1024 / 1024;
                    String tag_text = text.toString().replace("正在计算目标大小...", Math.ceil(size) + "MB");
                    tv4.setText(tag_text);
                    return;
                }

                if (tag_size > 1024) {
                    double size = tag_size / 1024;
                    String tag_text = text.toString().replace("正在计算目标大小...", Math.ceil(size) + "KB");
                    tv4.setText(tag_text);
                    return;
                }

                if (tag_size >= 0) {
                    String tag_text = text.toString().replace("正在计算目标大小...", Math.ceil(tag_size) + "B");
                    tv4.setText(tag_text);
                    return;
                }


            }
        }, "启动的线程");
        th.start();

    }


    // 递归方式 计算文件的大小
    public long getTotalSizeOfFilesInDir(final File file) {

        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children) {
                total += getTotalSizeOfFilesInDir(child);

            }

        return total;
    }


    //获取数据
    public void getData() {

        SharedPreferences sp = this.getSharedPreferences("data", 0);
        if (sp.getBoolean("first", true)) {

            new AlertDialog.Builder(this).setTitle("第一次打开").setMessage("1·先去阅读帮助文档\n2·然后申请程序所需要的权限\n3·添加规则程序才能正常运行").setPositiveButton("阅读文档", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this,HelpActivity.class));

                }
            }).show();


        } else {
            String Json = sp.getString("rule", "");
            Set<String> se = new HashSet<>();

            WhiteList = (HashSet<String>) sp.getStringSet("WhiteList", se);
            if (null != Json) {
                Gson gson = new Gson();
                data = gson.fromJson(Json, new TypeToken<List>() {
                }.getType());
            }
        }
    }


    //初始化控件
    private void init_val() {
        setting_window = findViewById(R.id.setting_window);
        b1 = (FloatingActionButton) findViewById(R.id.fab_b1);
        b2 = (FloatingActionButton) findViewById(R.id.fab_b2);
        mainlv = findViewById(R.id.mainlv);
        main_line2 = findViewById(R.id.main_line2);
        fam = findViewById(R.id.float_button);
        pr_bar = findViewById(R.id.progressBar);
        add_white = findViewById(R.id.add_white);

        sw1 = findViewById(R.id.switch_1);
        sw2 = findViewById(R.id.switch_2);


    }

    //显示dialog
    public void showDialog(String title, String text) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("OK", null).show();
    }

    //显示toast
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    //调用chrome打开网页
    public void showWebView(String link) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(); //Sets the toolbar color.
        builder.setToolbarColor(Color.WHITE);
        builder.setShowTitle(true);
        //显示网页标题
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(link));
    }

    //打开规则活动
    public void rule_list(View view) {
        startActivity(new Intent(this, RuleListActivity.class));

    }

    //ui复原
    public void ui_end(View view) {
        fuyuan();


    }


    public void fuyuan() {
        fam.collapse();
        if (setting_window.getY() != -1500) {
            ObjectAnimator.ofFloat(setting_window, "translationY", 0, -1500).setDuration(400).start();
        }

        if (main_line2.getX() != 0) {
            ObjectAnimator.ofFloat(main_line2, "translationX", -1500, 0).setDuration(400).start();
        }
    }


    public void per_start(View view) {
        new AlertDialog.Builder(this).setTitle("提示").setMessage("如需使用本程序所有功能，请申请程序所使用的所有动态权限。如果申请失败，请手动到设置 - 应用程序打开所有权限。").
                setPositiveButton("申请", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //申请权限操作
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            //检查代码是否拥有这个权限
                            int checkResult = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            //if(!=允许),抛出异常
                            if (checkResult != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); // 动态申请读取权限
                            } else {
                                showDialog("提示", "权限已经全部获取成功!");
                            }


                        }
                    }
                }).show();
    }


    public void about(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }


    @Override
    protected void onResume() {
        super.onResume();
        setting_window.setY(-1500);
        main_line2.setX(0);
        data_f5();
        fam.collapse();

    }

    public void data_f5() {

        totalsize = 0;

        getData();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data);
        mainlv.setAdapter(arrayAdapter);


        Runnable runnable = new Runnable() {
            public void run() {
                FileList.clear();
                for (String filepath : data) {
                    final File file = new File(filepath);
                    FileList.add(file);
                    TextView tv = findViewById(R.id.textView9);
                    if (tv.getText().toString().equals("所有目标对象合计信息")) {
                        tv.setText("正在加载");
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pr_bar1.setVisibility(View.VISIBLE);
                        }
                    });


                    long tmpsize = new FileUtils().getTotalSizeOfFilesInDir(file);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pr_bar1.setVisibility(View.GONE);
                        }
                    });


                    totalsize += tmpsize;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = findViewById(R.id.textView9);
                            tv.setText("一共有" + data.size() + "个对象,共" + Math.ceil(totalsize / 1024 / 1024) + "MB");
                        }
                    });


                }
            }
        };
        new Thread(runnable).start();

        //把list存进数据库
        Gson gson = new Gson();
        String Json = gson.toJson(data);

        SharedPreferences sp = this.getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("rule", Json);
        editor.putBoolean("first", false);
        editor.commit();

    }

    public void del_this_dir(View view) {
        fam.collapse();

        if (file != null) {
            del_file();
        }


    }


    public void white_list_activity(View v) {
        showDialog("tip", "白名单已被禁用");
        //startActivity(new Intent(this, WhiteListActivity.class));
    }

    public void add_white_list(View view) {
        //添加当前对象到白名单啊

        if (file != null) {
            WhiteList.add(file.getAbsolutePath());
            int index = Collections.frequency(WhiteList, file.getAbsolutePath());

            if (index != 0) {
                showToast("添加成功");

                if (index != 0) {

                    add_white.setVisibility(View.GONE);

                    SharedPreferences sp = this.getSharedPreferences("data", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putStringSet("WhiteList", WhiteList);
                    editor.putBoolean("first", false);
                    editor.commit();
                } else {
                    add_white.setVisibility(View.VISIBLE);
                }
            }
        }


    }


    void del_all_file() {
        if (isTwoOk) {
            new AlertDialog.Builder(this).setTitle("二次确认").setMessage("是否删除全部标记?").setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    pr_bar1.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (File f : FileList) {
                                file = f;
                                delete(file.getAbsolutePath());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refInfo();
                                        pr_bar1.setVisibility(View.GONE);

                                    }
                                });
                            }

                        }
                    }).start();


                }
            }).show();
        } else {
            delete(file.getAbsolutePath());
            refInfo();

        }

    }


    void del_file() {
        if (isTwoOk) {
            new AlertDialog.Builder(this).setTitle("二次确认").setMessage("是否删除 " + file.getName()).setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    delete(file.getAbsolutePath());
                    refInfo();

                }
            }).show();
        } else {
            delete(file.getAbsolutePath());
            refInfo();

        }

    }


    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */


    public boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }

        if (isDelDir) {
            // 删除当前目录

          dirFile.delete();

        }else
        {
            if (!dirFile.getAbsolutePath().equals(file.getAbsolutePath())){
                dirFile.delete();
            }
        }
        return true;
    }


}
