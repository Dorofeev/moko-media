/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import dev.icerock.moko.media.FileMedia
import java.io.File
import com.nbsp.materialfilepicker.MaterialFilePicker

class FilePickerFragment : Fragment() {
    init {
        retainInstance = true
    }

    private val codeCallbackMap = mutableMapOf<Int, CallbackData>()

    fun pickFile(callback: (Result<FileMedia>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0

        codeCallbackMap[requestCode] = CallbackData(callback)

        // TODO нужно убрать использование внешней зависимости, сделать конфигурацию способа
        //  выбора файла из вне (аргументом в контроллер передавать)
        MaterialFilePicker().withSupportFragment(this)
            .withCloseMenu(true)
            .withRequestCode(requestCode)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val callbackData = codeCallbackMap[requestCode] ?: return
        codeCallbackMap.remove(requestCode)

        val callback = callbackData.callback

        if (resultCode == Activity.RESULT_CANCELED) {
            callback.invoke(Result.failure(CanceledException()))
            return
        }

        processResult(callback, data)
    }

    private fun processResult(
        callback: (Result<FileMedia>) -> Unit,
        data: Intent?
    ) {
        val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)

        filePath?.let { path ->
            val name = File(path).name
            callback(Result.success(FileMedia(name, path)))
        }
    }

    class CallbackData(val callback: (Result<FileMedia>) -> Unit)
}
