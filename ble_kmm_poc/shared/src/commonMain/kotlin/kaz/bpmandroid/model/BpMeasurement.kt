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

package kaz.bpmandroid.model


class BpMeasurement {

    var systolic = 0
    var diastolic = 0
    var map = 0
    var pulse = 0
    var year = 0
    var month = 0
    var day = 0
    var hours = 0
    var minutes = 0
    var seconds = 0
    var errorStatus = 0
    var userId = 0
    var irregularPulse = false

    /*   fun isReadingSame(measurement: BpMeasurement): Boolean {
           return measurement.systolic  && measurement.getDiastolic() == getDiastolic() && measurement.getDate()!!
               .compareTo(getDate()) == 0 && measurement.getPulse() == getPulse()
       }*/
    var readingHasDefaultClock = false

    var units: String? = null

}