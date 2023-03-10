package com.hocel.texty.utils

class ScanningStatus private constructor(val status: Status) {
    companion object {
        val LOADED = ScanningStatus(Status.SUCCESS)
        val IDLE = ScanningStatus(Status.IDLE)
        val LOADING = ScanningStatus(Status.RUNNING)
        val ERROR = ScanningStatus(Status.FAILED)
    }

    enum class Status{
        RUNNING,
        SUCCESS,
        FAILED,
        IDLE
    }
}