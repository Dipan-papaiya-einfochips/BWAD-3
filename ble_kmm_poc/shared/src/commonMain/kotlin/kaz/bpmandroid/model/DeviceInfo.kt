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


class DeviceInfo {
    var deviceModel: Int = 0
    var user1Hash: Long = 0
    var user2Hash: Long = 0
    var numUsers = 0
    var ledActivated = 0
    var maxStoredReadings = 0
    var maxUserStoredReadings = 0
    var user1Name: String? = null
    var user2Name: String? = null
    var deviceUptime: Long = 0
    var user0Pairable = false
    var user1Pairable = false
    var user0NumReadings = 0
    var user1NumReadings = 0
    var macAddress: String? = null
    var manufacturerName: String? = null
    var serialNumber: String? = null
    var firmwareVersion: String? = null
    var softwareVersion: String? = null
    var modelName: String? = null
    var systemId: ByteArray? = null


}