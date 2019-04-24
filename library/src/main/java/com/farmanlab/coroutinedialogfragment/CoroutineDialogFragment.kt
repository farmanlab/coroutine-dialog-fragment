package com.farmanlab.coroutinedialogfragment

import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.GlobalScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.jvm.java
import kotlin.let

abstract class CoroutineDialogFragment<T> : androidx.appcompat.app.AppCompatDialogFragment() {
    private val onAttachEventChannel = Channel<Unit>()
    protected val channelViewModel: InternalViewModel<T> by lazy { provideViewModel() }

    /**
     * You can override this property if want to use fragment scope, your factory and so on...
     */
    protected open val viewModelProvider by lazy { ViewModelProviders.of(requireActivity()) }

    @Suppress("UNCHECKED_CAST")
    private fun provideViewModel(): InternalViewModel<T> =
        viewModelProvider.let {
            val viewModelKey = tag ?: this::class.java.simpleName
            it.get(viewModelKey, InternalViewModel::class.java) as InternalViewModel<T>
        }

    @androidx.annotation.CallSuper
    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        provideViewModel()
        GlobalScope.launch { onAttachEventChannel.send(Unit) }
    }

    @androidx.annotation.CallSuper
    override fun onCancel(dialog: android.content.DialogInterface) {
        channelViewModel.channel.offer(DialogResult.Cancel)
        super.onCancel(dialog)
    }

    suspend fun showAndResult(fragmentManager: FragmentManager, tag: String? = null): DialogResult<T> {
        show(fragmentManager, tag)
        onAttachEventChannel.receive()
        return channelViewModel.channel.receive()
    }


    suspend fun result(): DialogResult<T> = channelViewModel.channel.receive()

    class InternalViewModel<T> : ViewModel() {
        val channel: Channel<DialogResult<T>> = Channel()
    }
}
