package com.example.yllds.audiorecorddemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import com.example.yllds.audiorecorddemo.recoder.AudioRecordRecorderService
import kotlinx.android.synthetic.main.activity_audio_recorder.*
import java.util.*
/**
 * 点击record开始录音，点击stop停止录音
 * 利用adb pull 导出PCM文件
 * 		adb pull /mnt/sdcard/vocal.pcm ~/Desktop/
 * 利用ffplay播放声音
 * 		ffplay -f s16le  -sample_rate 44100  -channels 1 -i ~/Desktop/vocal.pcm
 * 利用ffmpeg将PCM文件转换为WAV文件
 * 		ffmpeg -f s16le  -sample_rate 44100  -channels 1 -i ~/Desktop/vocal.pcm -acodec pcm_s16le ~/Desktop/ssss.wav
 */

class AudioRecorderActivity : AppCompatActivity() {

    private val DISPLAY_RECORDING_TIME_FLAG = 100000
    private val record = R.string.record
    private val stop = R.string.stop

    private var isRecording = false
    private  lateinit var recorderService: AudioRecordRecorderService
    private val outputPath = "/mnt/sdcard/vocal.pcm"
    private var timer: Timer? = null
    private var recordingTimeInSecs = 0
    private var displayRecordingTimeTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_recorder)

        recorder_time_tip.text = "00:00"

        recorder_btn.setOnClickListener {
            if (isRecording) {
                isRecording = false
                recorder_btn.setText(getString(record))
                recordingTimeInSecs = 0
                recorderService?.stop()
                mHandler.sendEmptyMessage(DISPLAY_RECORDING_TIME_FLAG)
                displayRecordingTimeTask?.cancel()
                timer?.cancel()

            } else {
                isRecording = true
                recorder_btn.setText(getString(stop))
                recorderService = AudioRecordRecorderService.instance
                recorderService.initMeta()
                recorderService.start(outputPath)
                recordingTimeInSecs = 0
                timer = Timer()
                displayRecordingTimeTask = object : TimerTask() {
                    override fun run() {
                        mHandler.sendEmptyMessage(DISPLAY_RECORDING_TIME_FLAG)
                        recordingTimeInSecs++
                    }
                }
                timer?.schedule(displayRecordingTimeTask, 0, 1000);
            }
        }



    }



    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                DISPLAY_RECORDING_TIME_FLAG -> {
                    val minutes = recordingTimeInSecs / 60
                    val seconds = recordingTimeInSecs % 60
                    val timeTip = String.format("%02d:%02d", minutes, seconds)
                    recorder_time_tip.text = timeTip
                }
            }


        }


    }

}
