package com.farmanlab.coroutinedialogfragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

abstract class CoroutineBottomSheetDialogFragment<T> : BottomSheetDialogFragment() {
    private val onAttachEventChannel = Channel<Unit>()
    private val channelViewModel: ChannelDialogViewModel<T> by lazy { provideViewModel() }

    /**
     * You can override this property if want to use fragment scope, your factory and so on...
     */
    protected open val viewModelProvider by lazy { ViewModelProvider(requireActivity()) }

    @Suppress("UNCHECKED_CAST")
    private fun provideViewModel(): ChannelDialogViewModel<T> =
        viewModelProvider.let {
            val viewModelKey = tag ?: this::class.java.simpleName
            it.get(viewModelKey, ChannelDialogViewModel::class.java) as ChannelDialogViewModel<T>
        }

    @androidx.annotation.CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch { onAttachEventChannel.send(Unit) }
    }

    @androidx.annotation.CallSuper
    override fun onCancel(dialog: DialogInterface) {
        channelViewModel.channel.offer(DialogResult.Cancel)
        super.onCancel(dialog)
    }

    suspend fun showAndResult(
        fragmentManager: FragmentManager,
        tag: String? = null
    ): DialogResult<T> {
        val ft: FragmentTransaction = fragmentManager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
        onAttachEventChannel.receive()
        return channelViewModel.channel.receive()
    }

    suspend fun result(): DialogResult<T> = channelViewModel.channel.receive()

    class ChannelDialogViewModel<T> : ViewModel() {
        val channel: Channel<DialogResult<T>> = Channel()
    }
}
