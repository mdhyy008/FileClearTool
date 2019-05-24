package com.dabai.FileClear

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        title = "帮助文档"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.whatyoursee), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.gotogit)) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("http://www.baidu.com")
                        startActivity(intent)
                    }.show()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        when (item.itemId) {
            android.R.id.home -> {
                // 处理返回逻辑
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

}
