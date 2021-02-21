package com.farmanlab.coroutinedialogfragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimpleCoroutineDialogFragment : CoroutineDialogFragment<Unit>() {
    private val title: String
        get() = checkNotNull(requireArguments().getString(ARGS_TITLE))

    private val titleIcon: Int?
        get() = requireArguments().getInt(ARGS_TITLE_ICON).let { if (it == 0) null else it }

    private val message: String
        get() = checkNotNull(requireArguments().getString(ARGS_MESSAGE))

    private val positive: String?
        get() = requireArguments().getString(ARGS_POSITIVE_LABEL)

    private val positiveIcon: Int?
        get() = requireArguments().getInt(ARGS_POSITIVE_ICON).let { if (it == 0) null else it }

    private val negative: String?
        get() = requireArguments().getString(ARGS_NEGATIVE_LABEL)

    private val negativeIcon: Int?
        get() = requireArguments().getInt(ARGS_NEGATIVE_ICON).let { if (it == 0) null else it }

    private val neutral: String?
        get() = requireArguments().getString(ARGS_NEUTRAL_LABEL)

    private val neutralIcon: Int?
        get() = requireArguments().getInt(ARGS_NEUTRAL_ICON).let { if (it == 0) null else it }

    private val isDialogCancelable: Boolean
        get() = checkNotNull(requireArguments().getBoolean(ARGS_IS_CANCELABLE))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .apply {
                this@SimpleCoroutineDialogFragment.isCancelable = isDialogCancelable
                setCancelable(isDialogCancelable)
            }
            .apply {
                titleIcon?.let {
                    setIcon(it)
                }
                positive?.let {
                    setPositiveButton(it) { _, _ ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            channelViewModel.channel.send(
                                DialogResult.Ok(Unit)
                            )
                        }
                    }
                }
                positiveIcon?.let {
                    setPositiveButtonIcon(ContextCompat.getDrawable(context, it))
                }
                negative?.let {
                    setNegativeButton(it) { _, _ ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            channelViewModel.channel.send(
                                DialogResult.Cancel
                            )
                        }
                    }
                }
                negativeIcon?.let {
                    setNegativeButtonIcon(ContextCompat.getDrawable(context, it))
                }
                neutral?.let {
                    setNeutralButton(it) { _, _ ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            channelViewModel.channel.send(
                                DialogResult.Neutral
                            )
                        }
                    }
                }
                neutralIcon?.let {
                    setNeutralButtonIcon(ContextCompat.getDrawable(context, it))
                }
            }
            .create()

    companion object {
        private const val ARGS_TITLE = "title"
        private const val ARGS_TITLE_ICON = "title_icon"
        private const val ARGS_MESSAGE = "message"
        private const val ARGS_POSITIVE_LABEL = "positive_label"
        private const val ARGS_POSITIVE_ICON = "positive_icon"
        private const val ARGS_NEGATIVE_LABEL = "negative_label"
        private const val ARGS_NEGATIVE_ICON = "negative_icon"
        private const val ARGS_NEUTRAL_LABEL = "neutral_label"
        private const val ARGS_NEUTRAL_ICON = "neutral_icon"
        private const val ARGS_IS_CANCELABLE = "is_cancelable"

        fun newInstance(
            title: String,
            @androidx.annotation.DrawableRes titleIcon: Int? = null,
            message: String,
            positive: String? = null,
            @androidx.annotation.DrawableRes positiveIcon: Int? = null,
            negative: String? = null,
            @androidx.annotation.DrawableRes negativeIcon: Int? = null,
            neutral: String? = null,
            @androidx.annotation.DrawableRes neutralIcon: Int? = null,
            isCancelable: Boolean = true
        ): SimpleCoroutineDialogFragment = SimpleCoroutineDialogFragment().apply {
            arguments = bundleOf(
                ARGS_TITLE to title,
                ARGS_TITLE_ICON to titleIcon,
                ARGS_MESSAGE to message,
                ARGS_POSITIVE_LABEL to positive,
                ARGS_POSITIVE_ICON to positiveIcon,
                ARGS_NEGATIVE_LABEL to negative,
                ARGS_NEGATIVE_ICON to negativeIcon,
                ARGS_NEUTRAL_LABEL to neutral,
                ARGS_NEUTRAL_ICON to neutralIcon,
                ARGS_IS_CANCELABLE to isCancelable
            )
        }
    }
}
