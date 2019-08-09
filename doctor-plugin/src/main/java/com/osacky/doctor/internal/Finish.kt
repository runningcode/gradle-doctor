package com.osacky.doctor.internal

sealed class Finish {
    object None : Finish()
    data class FinishMessage(val message: String) : Finish()
}
