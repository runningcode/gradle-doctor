package com.osacky.doctor

import com.osacky.doctor.internal.Finish

interface BuildStartFinishListener {
    fun onStart()
    fun onFinish(): Finish
}
