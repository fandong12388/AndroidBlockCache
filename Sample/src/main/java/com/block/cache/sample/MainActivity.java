package com.block.cache.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.block.cache.BlockCache;
import com.block.cache.sample.data.Data;
import com.block.cache.sample.data.Student;
import com.block.cache.sample.data.Teacher;
import com.block.cache.sample.interceptor.EncyptInterceptor;
import com.block.cache.sample.interceptor.SerializeInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv_cookie);
        initMTLiteCache();
    }

    private void initMTLiteCache() {
        BlockCache.initializer(this)
                .debug(BuildConfig.DEBUG)
                .encyptInterceptor(new EncyptInterceptor())
                .serializeInterceptor(new SerializeInterceptor())
                .rootBlock("sample")
                .initialize();
    }

    //存长字符串
    public void saveStr(View view) {
        BlockCache.putString("news", "today_0", Data.TEST_STR);
    }

    //连存100次长字符串
    public void saveStr100(View view) {
        for (int i = 0; i < 100; i++) {
            BlockCache.putString("news", "today_" + i, i + "______" + Data.TEST_STR);
        }

    }

    //存对象
    public void saveObj(View view) {
        BlockCache.put("teacher", "techer_1", getDefaultObject());
    }

    //连存100次对象
    public void saveObj100(View view) {
        for (int i = 0; i < 100; i++) {
            BlockCache.put("teacher", "techer_" + i, getDefaultObject());
        }
    }

    //删除KEY
    public void removeKey(View view) {
        BlockCache.remove("teacher", "techer_99");
        List<String> keys = BlockCache.keys("teacher");
        if (null != keys) {
            textView.setText("当前block长度：" + keys.size() + ";" + keys.toString());
        } else {
            textView.setText("当前block长度为0");
        }
    }

    //删除Block
    public void removeBlock(View view) {
        BlockCache.remove("teacher");
        List<String> keys = BlockCache.keys("teacher");
        if (null != keys) {
            textView.setText("当前block长度：" + keys.size() + ";" + keys.toString());
        } else {
            textView.setText("当前block长度为0");
        }
    }

    //取字符串
    public void getStr(View view) {
        BlockCache.loadString("news", "today_0")
                .on(value -> textView.setText(value));
    }

    //取对象
    public void getObj(View view) {
        BlockCache.load("teacher", "techer_1", Teacher.class)
                .on(teacher -> {
                    if (null != teacher) {
                        textView.setText(teacher.toString());
                    } else {
                        textView.setText("获取为空");
                    }
                });
    }

    public void clear(View view) {
        BlockCache.clear();
    }

    public void getKeys(View view) {
        List<String> keys = BlockCache.keys("teacher");
        StringBuilder builder = new StringBuilder();
        if (null == keys) {
            builder.append("长度：").append(0).append("\n");
        } else {
            Collections.sort(keys, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            builder.append("长度：").append(keys.size()).append("\n");
            for (String key : keys) {
                builder.append(key).append("\n");
            }
        }
        textView.setText(builder.toString());
    }


    public Teacher getDefaultObject() {
        Student student = new Student("lilei", "男", 18);
        List<Student> list = new ArrayList<>();
        list.add(student);
        return new Teacher("zhangsan", "女", 29, list);
    }

}
