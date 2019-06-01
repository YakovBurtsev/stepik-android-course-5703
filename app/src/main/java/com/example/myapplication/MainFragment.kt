package com.example.myapplication

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmList


class MainFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    var httpRequestResult: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val param = arguments?.getString("param")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_main, container, false)

        recyclerView = view.findViewById(R.id.main_act_recycler_view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

    private fun showRecyclerView() {
        //getting from DB
        Realm.getDefaultInstance().executeTransaction {realm ->
            if (!isVisible) {
                return@executeTransaction
            }
            val feeds = realm.where(Feed::class.java).findAll()
            if (feeds.size > 0) {
                recyclerView.adapter = RecyclerViewAdapter(feeds[0]!!.items)
                recyclerView.layoutManager = LinearLayoutManager(activity)

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        httpRequestResult?.dispose()
    }

}