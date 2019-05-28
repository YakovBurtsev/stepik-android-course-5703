package com.example.myapplication

import io.reactivex.Observable
import java.net.HttpURLConnection
import java.net.URL

fun createRequest(url: String) = Observable.create<String> {
    val urlConnection = URL(url).openConnection() as HttpURLConnection
    try {
        urlConnection.connect()

        if (urlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            it.onError(RuntimeException(urlConnection.responseMessage))
        } else {
            it.onNext(urlConnection.inputStream.bufferedReader().readText());
        }
    } finally {
        urlConnection.disconnect()
    }
}