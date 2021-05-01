package jp.ac.titech.itpro.sdl.suggest_kotlin

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView

import androidx.appcompat.app.AppCompatActivity

import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private val MSG_RESULT: Int = 1234
    }

    private val handler: SuggestHandler = SuggestHandler(this)

    private lateinit var input: EditText
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.input)

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {v ->
            val query: String = input.text.toString().trim()
            if (query.isNotEmpty()) {
                val suggestUrl: String = resources.getString(R.string.suggest_url)
                SuggestThread(suggestUrl, handler, query).start()
            }
        }

        val suggested: ListView = findViewById(R.id.suggested)
        adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            ArrayList<String>()
        )
        suggested.adapter = adapter
        suggested.setOnItemClickListener{parent, view, pos, id ->
            val text: String = parent.getItemAtPosition(pos) as String
            val intent: Intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.putExtra(SearchManager.QUERY, text)
            startActivity(intent)
        }
    }

    fun showResult(result: List<String>): Unit {
        adapter.clear()
        if (result.isEmpty()) {
            adapter.add(resources.getString(R.string.result_no_suggestion))
        } else {
            adapter.addAll(result)
        }
        adapter.notifyDataSetChanged()
        input.selectAll()
    }

    private class SuggestHandler(activity: MainActivity) : Handler(Looper.getMainLooper()) {
        private val activityRef: WeakReference<MainActivity> = WeakReference<MainActivity>(activity)
        override fun handleMessage(msg: Message): Unit {
            val activity: MainActivity? = activityRef.get()
            if (activity == null || activity.isFinishing) {
                return
            }
            if (msg.what == MSG_RESULT) {
                activity.showResult(msg.obj as List<String>)
            }
        }
    }

    private class SuggestThread constructor(
            baseUrl: String,
            val handler: SuggestHandler,
            val query: String
    ) : Thread() {
        private val suggester: Suggester = Suggester(baseUrl)

        override fun run(): Unit {
            val result: List<String> = suggester.suggest(query)
            handler.sendMessage(handler.obtainMessage(MSG_RESULT, result))
        }
    }
}