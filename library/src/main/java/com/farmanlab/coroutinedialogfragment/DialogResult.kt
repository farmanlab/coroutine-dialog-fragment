package com.farmanlab.coroutinedialogfragment

sealed class DialogResult<out T> {
    data class Ok<T>(val data: T) : DialogResult<T>()
    object Cancel : DialogResult<Nothing>()
    object Neutral : DialogResult<Nothing>()
    object Pending : DialogResult<Nothing>()
}
