package com.osacky.doctor.internal

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

infix operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}