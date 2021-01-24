package cn.funcoding.router.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import cn.funcoding.router.R;
import cn.funcoding.router.annotations.Destination;

@Destination(url = "router://page-home", description = "主页")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}