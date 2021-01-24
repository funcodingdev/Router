package cn.funcoding.router.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.funcoding.router.R
import cn.funcoding.router.annotations.Destination

@Destination(url = "router://page-sign", description = "登陆页面")
class KtMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kt_main)
    }
}