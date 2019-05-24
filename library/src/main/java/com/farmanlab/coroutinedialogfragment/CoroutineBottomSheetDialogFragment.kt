package com.farmanlab.coroutinedialogfragment

import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

abstract class CoroutineBottomSheetDialogFragment<T> : BottomSheetDialogFragment() {
    private val onAttachEventChannel = Channel<Unit>()
    protected val channelViewModel: ChannelDialogViewModel<T> by lazy { provideViewModel() }

    /**
     * You can override this property if want to use fragment scope, your factory and so on...
     */
    protected open val viewModelProvider by lazy { ViewModelProviders.of(requireActivity()) }

    @Suppress("UNCHECKED_CAST")
    private fun provideViewModel(): ChannelDialogViewModel<T> =
        viewModelProvider.let {
            val viewModelKey = tag ?: this::class.java.simpleName
            it.get(viewModelKey, ChannelDialogViewModel::class.java) as ChannelDialogViewModel<T>
        }

    @androidx.annotation.CallSuper
    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch { onAttachEventChannel.send(Unit) }
    }

    @androidx.annotation.CallSuper
    override fun onCancel(dialog: android.content.DialogInterface) {
        channelViewModel.channel.offer(DialogResult.Cancel)
        super.onCancel(dialog)
    }

    suspend fun showAndResult(
        fragmentManager: androidx.fragment.app.FragmentManager,
        tag: String? = null
    ): DialogResult<T> {
        show(fragmentManager, tag)
        onAttachEventChannel.receive()
        return channelViewModel.channel.receive()
    }

    suspend fun result(): DialogResult<T> = channelViewModel.channel.receive()

    class ChannelDialogViewModel<T> : androidx.lifecycle.ViewModel() {
        val channel: Channel<DialogResult<T>> = Channel()
    }
}
