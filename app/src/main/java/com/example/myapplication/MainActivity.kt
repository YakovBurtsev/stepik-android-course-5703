package com.example.myapplication

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject

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
                .map { Gson().fromJson(it, FeedAPI::class.java) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        httpRequestResult = observable.subscribe({
            //result handling
            val feed = Feed(
                it.items.mapTo(
                    RealmList(),
                    { f -> FeedItem(f.title, f.link, f.thumbnail, f.description) })
            )

            Realm.getDefaultInstance().executeTransaction { realm ->
                val oldFeeds = realm.where(Feed::class.java).findAll()
                oldFeeds.forEach { oldFeed ->
                    oldFeed.deleteFromRealm()
                }
                realm.copyToRealm(feed)
            }

            showRecyclerView()
        }, {
            //error handling
            Log.e("test", "", it)
            showRecyclerView()
        })

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }

    private fun showRecyclerView() {
        //getting from DB
        Realm.getDefaultInstance().executeTransaction {realm ->

        val feeds = realm.where(Feed::class.java).findAll()
            if (feeds.size > 0) {
                recyclerView.adapter = RecyclerViewAdapter(feeds[0]!!.items)
                recyclerView.layoutManager = LinearLayoutManager(this)

            }
        }
    }


    override fun onDestroy() {
        httpRequestResult?.dispose()
        super.onDestroy()
    }
}

//dto classes

class FeedAPI(val items: ArrayList<FeedItemAPI>)

class FeedItemAPI(
    val title: String,
    val link: String,
    val thumbnail: String,
    val description: String
)

//domain classes (for DB)

open class Feed(var items: RealmList<FeedItem> = RealmList<FeedItem>()) : RealmObject()

open class FeedItem(
    var title: String = "",
    var link: String = "",
    var thumbnail: String = "",
    var description: String = ""
) : RealmObject()


class RecyclerViewAdapter(val feedItems: RealmList<FeedItem>) : RecyclerView.Adapter<RecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        val itemView = inflater.inflate(R.layout.list_item, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return feedItems.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder?, position: Int) {
        val feedItem = feedItems[position]!!
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

        itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(feedItem.link)
            thumbnailView.context.startActivity(intent)
        }
    }
}
