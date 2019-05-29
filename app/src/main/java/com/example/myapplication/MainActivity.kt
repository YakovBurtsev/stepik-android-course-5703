package com.example.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    var httpRequestResult: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.main_act_recycler_view)

        val url = "https://api.rss2json.com/v1/api.json?rss_url=http%3A%2F%2Ffeeds.bbci.co.uk%2Fnews%2Frss.xml"
        val observable =
            createRequest(url)
                .map { Gson().fromJson(it, Feed::class.java) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        httpRequestResult = observable.subscribe({
            //result handling
            showRecyclerView(it.items)
        }, {
            //error handling
            Log.e("test", "", it)
        })

    }

    private fun showRecyclerView(feedItems: ArrayList<FeedItem>) {
        recyclerView.adapter = RecyclerViewAdapter(feedItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
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


class RecyclerViewAdapter(val feedItems: ArrayList<FeedItem>) : RecyclerView.Adapter<RecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        val itemView = inflater.inflate(R.layout.list_item, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return feedItems.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder?, position: Int) {
        val feedItem = feedItems[position]
        holder?.bind(feedItem)

    }
}

class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(feedItem: FeedItem) {
        val titleView = itemView.findViewById<TextView>(R.id.item_title)
        titleView.text = feedItem.title

        val descriptionView = itemView.findViewById<TextView>(R.id.item_description)
        descriptionView.text = feedItem.description

        val thumbnailView = itemView.findViewById<ImageView>(R.id.item_thumbnail)
        Picasso.with(thumbnailView.context).load(feedItem.thumbnail).into(thumbnailView)
    }
}
