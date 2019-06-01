package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.realm.RealmList
import io.realm.RealmObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        if (savedInstanceState == null) {
            val bundle = Bundle()
            bundle.putString("param", "value")
            val fragment = MainFragment()
            fragment.arguments = bundle
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_place, fragment)
                .commitAllowingStateLoss()
        }
    }

    fun showArticle(url: String) {
        val bundle = Bundle()
        bundle.putString("url", url)
        val fragment = SecondFragment()
        fragment.arguments = bundle

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_place, fragment)
            .addToBackStack("main")
            .commitAllowingStateLoss()
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
            (thumbnailView.context as MainActivity).showArticle(feedItem.link)
        }
    }
}
