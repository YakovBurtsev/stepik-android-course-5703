package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var helloWorldTextView: TextView
    private lateinit var vList: LinearLayout
    var httpRequestResult: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vList = findViewById(R.id.act1_list)

        val observable =
            createRequest("https://api.rss2json.com/v1/api.json?rss_url=http%3A%2F%2Ffeeds.bbci.co.uk%2Fnews%2Frss.xml")
                .map { Gson().fromJson(it, Feed::class.java) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        httpRequestResult = observable.subscribe({
            //result handling
            showLinearLayout(it.items)
        }, {
            //error handling
            Log.e("test", "", it)
        })

    }

    fun showLinearLayout(feedList: ArrayList<FeedItem>) {
        val inflater = layoutInflater
        for (feed in feedList) {
            val view = inflater.inflate(R.layout.list_item, vList, false)
            view.findViewById<TextView>(R.id.item_title).text = feed.title
            vList.addView(view)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {
            helloWorldTextView.text = data.getStringExtra("editedText")
        }
    }

    override fun onDestroy() {
        httpRequestResult?.dispose()
        super.onDestroy()
    }
}

class Feed(val items: ArrayList<FeedItem>)

class FeedItem(
    val title: String,
    val link: String,
    val thumbnail: String,
    val description: String
)
