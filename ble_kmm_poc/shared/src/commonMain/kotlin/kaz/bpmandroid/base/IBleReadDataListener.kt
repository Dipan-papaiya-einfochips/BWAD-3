package kaz.bpmandroid.base

import kaz.bpmandroid.model.BpMeasurement

interface IBleReadDataListener {

    fun onMeasurement()
    fun onGetReadings(readingData: List<BpMeasurement>)
}