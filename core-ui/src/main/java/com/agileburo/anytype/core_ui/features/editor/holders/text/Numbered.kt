package com.agileburo.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.addDot
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_numbered.view.*

class Numbered(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Text(view), SupportNesting {

    private val container = itemView.numberedBlockContentContainer
    val number = itemView.number
    override val content: TextInputWidget = itemView.numberedListContent
    override val root: View = itemView

    init {
        setup(onMarkupActionClicked, ContextMenuType.TEXT)
    }

    fun bind(
        item: BlockView.Text.Numbered,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        clicked: (ListenerType) -> Unit,
        onEndLineEnterClicked: (String, Editable) -> Unit,
        onSplitLineEnterClicked: (String, Int, Editable) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) = super.bind(
        item = item,
        onTextChanged = { _, editable ->
            item.apply {
                text = editable.toString()
                marks = editable.marks()
            }
            onTextBlockTextChanged(item)
        },
        onSelectionChanged = onSelectionChanged,
        onFocusChanged = onFocusChanged,
        clicked = clicked,
        onEndLineEnterClicked = onEndLineEnterClicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked
    ).also {
        setNumber(item)
    }

    private fun setNumber(item: BlockView.Text.Numbered) {
        number.gravity = when (item.number) {
            in 1..19 -> Gravity.CENTER_HORIZONTAL
            else -> Gravity.START
        }
        number.text = item.number.addDot()
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        super.processChangePayload(payloads, item, onTextChanged, onSelectionChanged, clicked)
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.NUMBER_CHANGED))
                number.text = "${(item as BlockView.Text.Numbered).number}"
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        number.updateLayoutParams<LinearLayout.LayoutParams> {
            setMargins(
                item.indent * dimen(R.dimen.indent),
                0,
                0,
                0
            )
        }
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }
}