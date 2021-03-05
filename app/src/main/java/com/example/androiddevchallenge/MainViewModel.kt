/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androiddevchallenge.utils.GreenProgress
import com.example.androiddevchallenge.utils.RedProgress
import com.example.androiddevchallenge.utils.YellowProgress
import com.example.androiddevchallenge.utils.toFormattedTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var _countDownTimer: PreciseCountdownTimer? = null

    private val _time = MutableLiveData((0L).toFormattedTime())
    val time: LiveData<String> = _time

    private val _progress = MutableLiveData(0.00F)
    val progress: LiveData<Float> = _progress

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _lastTenSeconds = MutableLiveData(false)
    val lastTenSeconds: LiveData<Boolean> = _lastTenSeconds

    private val _progressColor = MutableLiveData(GreenProgress)
    val progressColor: LiveData<Long> = _progressColor

    private val _timeQuotes = MutableLiveData("")
    val timeQuotes: LiveData<String> = _timeQuotes
    private var currentIndex = 0

    fun startTimer(timeInMs: Long = 75 * 1000L) {
        _isRunning.value = true
        _progressColor.value = GreenProgress
        _progress.value = 1f
        _time.value = (timeInMs).toFormattedTime()

        viewModelScope.launch {
            while (isActive && isRunning.value == true) {

                currentIndex += 1
                if (currentIndex >= quotesList().size)
                    currentIndex = 0

                _timeQuotes.postValue(quotesList().get(currentIndex))

                delay(6000)
            }
        }

        _countDownTimer = object : PreciseCountdownTimer(timeInMs, 1000) {
            override fun onTick(timeLeft: Long) {
                _progressColor.postValue(YellowProgress)
                _progress.postValue(timeLeft.toFloat() / timeInMs)
                _time.postValue(timeLeft.toFormattedTime())

                if (timeLeft <= 10_000) {
                    _lastTenSeconds.postValue(true)
                }
            }

            override fun onFinished() {
                _isRunning.postValue(false)
                _progress.postValue(0f)
                _time.postValue((0L).toFormattedTime())
            }
        }

        viewModelScope.launch {
            delay(500)
            _countDownTimer?.start()
        }
    }

    fun cancelTimer() {
        _countDownTimer?.cancel()

        _progressColor.value = RedProgress
        _isRunning.value = false
        _progress.value = 0f
        _time.value = (0L).toFormattedTime()
    }

    fun quotesList() = listOf(
        "Sometimes the secret to getting more done is to take time off.",
        "Taking time to do nothing often brings everything into perspective.",
        "Sometimes you just need to go off the grid and get your soul right.",
        "Time isn't the main thing. It's the only thing.",
        "You can discover more about a person in an hour of play than in a year of conversation."
    )
}
