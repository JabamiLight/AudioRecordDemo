package com.example.yllds.audiorecorddemo.recoder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.FileOutputStream

/*
* Created by TY on 2018/6/17.
*      
*
*      
*          ┌─┐       ┌─┐
*       ┌──┘ ┴───────┘ ┴──┐
*       │                 │
*       │       ───       │
*       │  ─┬┘       └┬─  │
*       │                 │
*       │       ─┴─       │
*       │                 │
*       └───┐         ┌───┘
*           │         │
*           │         │
*           │         │
*           │         └──────────────┐
*           │                        │
*           │                        ├─┐
*           │                        ┌─┘    
*           │                        │
*           └─┐  ┐  ┌───────┬──┐  ┌──┘         
*             │ ─┤ ─┤       │ ─┤ ─┤         
*             └──┴──┘       └──┴──┘ 
*                 神兽保佑 
*                 代码无BUG! 
*/
class AudioRecordRecorderService {
    val TAG = "AudioRecordRecorderServiceImpl"

    val WRITE_FILE_FAIL = 9208911

    private var audioRecoder: AudioRecord? = null
    private var recordThread: Thread? = null

    private var AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

    companion object {
        var SAMPLE_RATE_IN_HZ = 44100
        private val CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO
        private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        val instance = AudioRecordRecorderService()
    }


    private var bufferSizeInBytes = 0

    private var isRecording = false

    fun initMeta() {
        audioRecoder?.release()

        bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIGURATION, AUDIO_FORMAT)
        audioRecoder = AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIGURATION, AUDIO_FORMAT, bufferSizeInBytes)

        if (audioRecoder == null || audioRecoder?.state != AudioRecord.STATE_INITIALIZED) {
            SAMPLE_RATE_IN_HZ = 16000;
            bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIGURATION, AUDIO_FORMAT)
            audioRecoder = AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIGURATION, AUDIO_FORMAT, bufferSizeInBytes)
        }

        if (audioRecoder?.state != AudioRecord.STATE_INITIALIZED) {
            throw AudioConfigurationException()
        }
    }

    private var outputStream: FileOutputStream? = null
    private var outputFilePath: String? = null
    fun start(filePath: String) {
        audioRecoder?.run {
            if (state != AudioRecord.STATE_INITIALIZED)
                null
            else
                startRecording()

        } ?: throw StartRecordingException()
        isRecording = true
        recordThread = Thread(RecordThread(), "RecordThread")
        outputFilePath = filePath
        recordThread?.start() ?: throw StartRecordingException();
    }

    inner class RecordThread : Runnable {
        override fun run() {
            outputStream = FileOutputStream(outputFilePath)

            val audioSamples = ByteArray(bufferSizeInBytes)
            outputStream.use {
                while (isRecording) {
                    val audioSampleSize = getAudioRecordBuffer(bufferSizeInBytes, audioSamples)
                    if (audioSampleSize != 0) {
                        outputStream?.write(audioSamples)
                    }
                }

            }

        }

    }

    private fun getAudioRecordBuffer(bufferSizeInBytes: Int, audioSamples: ByteArray): Int {
        audioRecoder?.run {
            return read(audioSamples, 0, bufferSizeInBytes)
        } ?: return 0
    }

    fun stop() {
        audioRecoder?.run {
            isRecording = false
            recordThread?.join()
            releaseAudioRecord();

        }
    }

    private fun releaseAudioRecord() {
        audioRecoder?.run {
            stop()
            release()
        }


    }


}