package com.farmanlab.coroutinedialogfragment

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

abstract class CoroutineDialogFragment<T> : AppCompatDialogFragment() {
    private val onAttachEventChannel = Channel<Unit>()
    protected val channelViewModel: InternalViewModel<T> by lazy { provideViewModel() }

    /**
     * You can override this property if want to use fragment scope, your factory and so on...
     */
    protected open val viewModelProvider by lazy { ViewModelProvider(requireActivity()) }

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
        // viewLifecycleOwner is not available here yet
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

    class InternalViewModel<T> : ViewModel() {
        val channel: Channel<DialogResult<T>> = Channel()
    }
}
