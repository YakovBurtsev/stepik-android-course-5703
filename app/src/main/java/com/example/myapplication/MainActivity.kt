package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var helloWorldTextView: TextView
    private lateinit var listView: ListView
    var httpRequestResult: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.act1_ListView)

        val observable =
            createRequest("https://api.rss2json.com/v1/api.json?rss_url=http%3A%2F%2Ffeeds.bbci.co.uk%2Fnews%2Frss.xml")
                .map { Gson().fromJson(it, Feed::class.java) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        httpRequestResult = observable.subscribe({
            //result handling
            showListView(it.items)
        }, {
            //error handling
            Log.e("test", "", it)
        })

    }

    fun showListView(feedList: ArrayList<FeedItem>) {
        listView.adapter = Adapter(feedList)
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

class Adapter(val items: ArrayList<FeedItem>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val inflater = LayoutInflater.from(parent!!.context)
        val view = convertView ?: inflater.inflate(R.layout.list_item, parent, false)
        val item = getItem(position) as FeedItem
        view.findViewById<TextView>(R.id.item_title).text = item.title
        return view
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}