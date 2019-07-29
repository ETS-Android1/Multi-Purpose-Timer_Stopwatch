package com.armcomptech.akash.simpletimer4

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment

import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.FragmentActivity

import java.util.Objects

class ExampleDialog : AppCompatDialogFragment() {
    private var editTextTimer: EditText? = null
    private var listner: ExmapleDialogListner? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val inflater = Objects.requireNonNull<FragmentActivity>(activity).layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.layout_dialog_timerset, null)

        builder.setView(view)
                .setTitle("Timer")
                .setNegativeButton("Cancel") { _, _ ->

                }
                .setPositiveButton("Set Timer") { _, _ ->
                    val time = editTextTimer!!.text.toString()

                    if (!time.matches("".toRegex())) {
                        listner!!.applyText(time)
                    }
                }

        editTextTimer = view.findViewById(R.id.timer)


        editTextTimer!!.setOnFocusChangeListener { _, _ ->
            editTextTimer!!.post {
                val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        editTextTimer!!.requestFocus()


        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            listner = context as ExmapleDialogListner?
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + "must implement ExampleDialogListner")
        }

    }

    interface ExmapleDialogListner {
        fun applyText(time: String)
    }
}
