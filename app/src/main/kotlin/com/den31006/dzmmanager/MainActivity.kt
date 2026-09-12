package com.den31006.dzmmanager

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.*
import android.view.ViewGroup
import java.io.File

class MainActivity : Activity() {
    private val files = mutableListOf<File>()
    private lateinit var list: LinearLayout
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 32, 28, 24)
        }

        val title = TextView(this).apply {
            text = "DZM Manager"
            textSize = 28f
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        val subtitle = TextView(this).apply {
            text = "Собственный контейнер .dzm — без ZIP"
            textSize = 15f
        }
        root.addView(subtitle, LinearLayout.LayoutParams(-1, -2))

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val add = Button(this).apply { text = "Добавить файл" }
        val create = Button(this).apply { text = "Создать DZM" }
        buttons.addView(add, LinearLayout.LayoutParams(0, -2, 1f))
        buttons.addView(create, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(buttons)

        status = TextView(this).apply { text = "Файлы не выбраны"; textSize = 16f }
        root.addView(status)

        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))

        add.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(intent, 10)
        }

        create.setOnClickListener { createDzm() }
        setContentView(root)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        Toast.makeText(this, "Получен файл: ${uri.lastPathSegment}", Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 10 || resultCode != RESULT_OK || data == null) return

        if (data.clipData != null) {
            for (i in 0 until data.clipData!!.itemCount) addUri(data.clipData!!.getItemAt(i).uri)
        } else {
            data.data?.let { addUri(it) }
        }
        refresh()
    }

    private fun addUri(uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "file"
        val target = File(cacheDir, "${System.currentTimeMillis()}_$name")
        contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        files += target
    }

    private fun refresh() {
        list.removeAllViews()
        files.forEach { f ->
            list.addView(TextView(this).apply {
                text = "• ${f.name}"
                textSize = 16f
                setPadding(0, 8, 0, 8)
            })
        }
        status.text = if (files.isEmpty()) "Файлы не выбраны" else "Выбрано файлов: ${files.size}"
    }

    private fun createDzm() {
        if (files.isEmpty()) {
            Toast.makeText(this, "Сначала добавь хотя бы один файл", Toast.LENGTH_SHORT).show()
            return
        }
        val out = File(getExternalFilesDir(null), "archive_${System.currentTimeMillis()}.dzm")
        try {
            DzmFormat.create(out, files)
            Toast.makeText(this, "Создано: ${out.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
