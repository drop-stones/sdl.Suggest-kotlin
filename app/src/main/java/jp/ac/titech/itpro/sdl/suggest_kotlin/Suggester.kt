package jp.ac.titech.itpro.sdl.suggest_kotlin

import android.util.Xml

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class Suggester(val baseUrl: String) {

    public fun suggest(query: String): List<String> {
        try {
            val conn: HttpURLConnection = getConnection(getURL(query))
            try {
                val input: InputStream = conn.inputStream
                return parse(input, "UTF-8")
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            val result: ArrayList<String> = ArrayList<String>()
            result.add(e.toString())
            return result
        }
    }

    @Throws(IOException::class)
    private fun getURL(query: String): URL {
        val encodedQuery: String = URLEncoder.encode(query, "UTF-8")
        return URL(baseUrl + encodedQuery)
    }

    @Throws(IOException::class)
    private fun getConnection(url: URL): HttpURLConnection {
        val result: HttpURLConnection = url.openConnection() as HttpURLConnection
        result.connectTimeout = 10000
        result.doInput = true
        result.connect()
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parse(input: InputStream, encoding: String): List<String> {
        val xpp: XmlPullParser = Xml.newPullParser()
        xpp.setInput(input, encoding)

        val result: ArrayList<String> = ArrayList<String>()
        var et: Int = xpp.eventType
        while (et != XmlPullParser.END_DOCUMENT){
            if (et == XmlPullParser.START_TAG && xpp.name.equals("suggestion", ignoreCase = true)) {
                for(i in 0 until xpp.attributeCount) {
                    if (xpp.getAttributeName(i).equals("data", ignoreCase = true)) {
                        result.add(xpp.getAttributeValue(i))
                    }
                }
            }
            et = xpp.next()
        }
        return result
    }
}