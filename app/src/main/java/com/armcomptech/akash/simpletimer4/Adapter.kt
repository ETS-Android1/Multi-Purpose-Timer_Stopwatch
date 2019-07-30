package com.armcomptech.akash.simpletimer4

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.lang.*
import java.util.ArrayList

class Adapter internal constructor(private val context: Context, private val timerName: ArrayList<String>?, private val count: ArrayList<Int>, private val timeInSeconds: ArrayList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val row = inflater.inflate(R.layout.statistics_recycler_vew, viewGroup, false)
        return Item(row)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        //prints the name of the timer
        (viewHolder as Item).textViewTimerName.text = "Timer Name: " + timerName!![position]

        //prints how many times this particular timer was used
        if (count[position] == 1) {
            viewHolder.textViewCount.text = "Used only once"
        } else {
            viewHolder.textViewCount.text = "Used " + count[position] + " times"
        }

        //prints total time this timer was used
        var totalTimerTimeString = ""
        val newString = StringBuilder(totalTimerTimeString)
        val temp = timeInSeconds[position]
        val second = temp % 60
        val minute = temp / 60 % 60
        val hour = temp / 60 / 60

        //build the string
        while (true) {
            if (hour > 0) {
                if (hour == 1) {
                    newString.append(hour).append(" Hour ")
                } else {
                    newString.append(hour).append(" Hours ")
                }

                if (minute == 0 || minute == 1) {
                    newString.append(minute).append(" Minute ")
                } else {
                    newString.append(minute).append(" Minutes ")
                }

                if (second == 0 || second == 1) {
                    newString.append(second).append(" Second ")
                } else {
                    newString.append(second).append(" Seconds ")
                }
                break
            }

            if (minute > 0) {
                if (minute == 1) {
                    newString.append(minute).append(" Minute ")
                } else {
                    newString.append(minute).append(" Minutes ")
                }

                if (second == 0 || second == 1) {
                    newString.append(second).append(" Second ")
                } else {
                    newString.append(second).append(" Seconds ")
                }
                break
            }

            if (second > 0) {
                if (second == 1) {
                    newString.append(second).append(" Second ")
                } else {
                    newString.append(second).append(" Seconds ")
                }
                break
            }

            if (second == 0) {
                newString.append(second).append(" Second ")
                break
            }
        }

        totalTimerTimeString = newString.toString()
        viewHolder.textViewTimeInSeconds.text = "Total time: $totalTimerTimeString"
    }

    override fun getItemCount(): Int {
        return timerName?.size ?: 0
    }

    inner class Item internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var textViewTimerName: TextView
        internal var textViewCount: TextView
        internal var textViewTimeInSeconds: TextView

        init {
            textViewTimerName = itemView.findViewById(R.id.timerName)
            textViewCount = itemView.findViewById(R.id.count)
            textViewTimeInSeconds = itemView.findViewById(R.id.timeInSeconds)
        }
    }
}
