package com.jtv.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var progress: ProgressBar

    private var all: List<Channel> = emptyList()
    private var shown: List<Channel> = emptyList()
    private lateinit var adapter: ChannelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycler = findViewById(R.id.recycler)
        spinner = findViewById(R.id.groupSpinner)
        progress = findViewById(R.id.progress)

        adapter = ChannelAdapter { openPlayer(it) }
        recycler.layoutManager = GridLayoutManager(this, spanCount())
        recycler.adapter = adapter

        loadChannels()
    }

    private fun spanCount(): Int {
        val dp = resources.configuration.smallestScreenWidthDp
        return when {
            dp >= 720 -> 5   // TV / tablet
            dp >= 600 -> 4
            else -> 3
        }
    }

    private fun loadChannels() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            all = ChannelRepo.load(this@MainActivity)
            progress.visibility = View.GONE
            setupGroups()
        }
    }

    private fun setupGroups() {
        val groups = listOf("Tất cả") + all.map { it.group }.distinct()
        val sa = ArrayAdapter(this, android.R.layout.simple_spinner_item, groups)
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = sa
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val g = groups[pos]
                shown = if (pos == 0) all else all.filter { it.group == g }
                adapter.submit(shown)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        shown = all
        adapter.submit(all)
    }

    private fun openPlayer(ch: Channel) {
        val list = ArrayList(shown.map { ChannelParcel.from(it) })
        val idx = shown.indexOf(ch)
        val i = Intent(this, PlayerActivity::class.java)
        i.putParcelableArrayListExtra(PlayerActivity.EXTRA_LIST, list)
        i.putExtra(PlayerActivity.EXTRA_INDEX, idx)
        startActivity(i)
    }
}
