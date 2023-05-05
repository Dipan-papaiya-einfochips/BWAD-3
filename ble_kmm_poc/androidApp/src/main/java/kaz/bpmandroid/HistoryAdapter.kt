/*
 * Copyright 2023 Punch Through Design LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kaz.bpmandroid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import kaz.bpmandroid.model.BpMeasurement


@RequiresApi(Build.VERSION_CODES.M)
class HistoryAdapter(
    private val items: ArrayList<BpMeasurement>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_reading, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = items.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]

        if (position == 0) {
            holder.llMain.setBackgroundColor(Color.parseColor("#00B0FF"))
        }else{
            holder.llMain.setBackgroundColor(Color.parseColor("#ffffff"))
        }


        // sets the text to the textview from our itemHolder class
        holder.textView.text =
            ("SYS: ${item.systolic} ${item.units} DIA: ${item.diastolic} ${item.units} PULSE: ${item.pulse}")

    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_data)
        val llMain: LinearLayout = itemView.findViewById(R.id.llMain)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


}