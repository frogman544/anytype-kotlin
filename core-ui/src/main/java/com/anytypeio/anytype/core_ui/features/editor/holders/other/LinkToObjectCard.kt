package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardBinding
import com.anytypeio.anytype.core_ui.features.editor.*
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.Glide

class LinkToObjectCard(binding: ItemBlockObjectLinkCardBinding) :
    BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    private val cover = binding.cover
    private val untitled = itemView.resources.getString(R.string.untitled)
    private val objectIcon = binding.cardIcon
    private val title = binding.cardName
    private val description = binding.cardDescription
    private val guideline = binding.pageGuideline
    private val progress = binding.progress
    private val syncing = binding.syncing

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Card,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        itemView.isSelected = item.isSelected

        applyName(item)

        applyDescription(item)

        applyCover(item)

        applySearchHighlight(item)

        applyImageOrEmoji(item)

        itemView.setOnClickListener { clicked(ListenerType.LinkToObject(item.id)) }

        bindLoading(item.isLoading)
    }

    private fun applyName(item: BlockView.LinkToObject.Default.Card) {
        val name = item.text
        when {
            name == null -> title.gone()
            name.isBlank() -> {
                title.visible()
                val sb = SpannableString(untitled)
                title.text = sb
            }
            else -> {
                title.visible()
                val sb = SpannableString(name)
                title.text = sb
            }
        }
    }

    private fun applyDescription(item: BlockView.LinkToObject.Default.Card) {
        if (item.description.isNullOrBlank()) {
            description.gone()
        } else {
            description.visible()
            description.text = item.description
        }
    }

    private fun applyImageOrEmoji(item: BlockView.LinkToObject.Default.Card) {
        when (item.icon) {
            ObjectIcon.None -> {
                objectIcon.gone()
            }
            else -> {
                objectIcon.visible()
                objectIcon.setIcon(item.icon)
            }
        }
    }

    private fun applyCover(item: BlockView.LinkToObject.Default.Card) {
        setCover(
            coverColor = item.coverColor,
            coverGradient = item.coverGradient,
            coverImage = item.coverImage
        )
    }

    private fun bindLoading(isLoading: Boolean) {
        if (isLoading) {
            if (objectIcon.isVisible) {
                objectIcon.invisible()
            }
            title.invisible()
            progress.visible()
            syncing.visible()
        } else {
            progress.invisible()
            syncing.invisible()
            if (objectIcon.isInvisible) {
                objectIcon.visible()
            }
            title.visible()
        }
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY }
            ?.let { field ->
                applySearchHighlight(field, title)
            } ?: clearSearchHighlights()
    }

    private fun applySearchHighlight(field: BlockView.Searchable.Field, input: TextView) {
        input.editableText.removeSpans<SearchHighlightSpan>()
        input.editableText.removeSpans<SearchTargetHighlightSpan>()
        field.highlights.forEach { highlight ->
            input.editableText.setSpan(
                SearchHighlightSpan(),
                highlight.first,
                highlight.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (field.isTargeted) {
            input.editableText.setSpan(
                SearchTargetHighlightSpan(),
                field.target.first,
                field.target.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun clearSearchHighlights() {
        title.editableText?.removeSpans<SearchHighlightSpan>()
        title.editableText?.removeSpans<SearchTargetHighlightSpan>()
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Default.Card) { "Expected a link to object block card, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                itemView.isSelected = item.isSelected
                applyImageOrEmoji(item)
            }
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
            if (payload.isLoadingChanged)
                bindLoading(item.isLoading)
            if (payload.isObjectTitleChanged)
                applyName(item)
            if (payload.isObjectIconChanged)
                applyImageOrEmoji(item)
            if (payload.isObjectDescriptionChanged)
                applyDescription(item)
            if (payload.isObjectCoverChanged)
                applyCover(item)
        }
    }

    private fun setCover(
        coverColor: CoverColor?,
        coverImage: String?,
        coverGradient: String?
    ) {
        when {
            coverColor != null -> {
                cover.apply {
                    visible()
                    setImageDrawable(null)
                    setBackgroundColor(coverColor.color)
                }
            }
            coverImage != null -> {
                cover.apply {
                    visible()
                    setBackgroundColor(0)
                    Glide
                        .with(itemView)
                        .load(coverImage)
                        .centerCrop()
                        .into(this)
                }
            }
            coverGradient != null -> {
                cover.apply {
                    setImageDrawable(null)
                    setBackgroundColor(0)
                    when (coverGradient) {
                        CoverGradient.YELLOW -> setBackgroundResource(R.drawable.cover_gradient_yellow)
                        CoverGradient.RED -> setBackgroundResource(R.drawable.cover_gradient_red)
                        CoverGradient.BLUE -> setBackgroundResource(R.drawable.cover_gradient_blue)
                        CoverGradient.TEAL -> setBackgroundResource(R.drawable.cover_gradient_teal)
                        CoverGradient.PINK_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_1)
                        CoverGradient.BLUE_PINK -> setBackgroundResource(R.drawable.wallpaper_gradient_2)
                        CoverGradient.GREEN_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_3)
                        CoverGradient.SKY -> setBackgroundResource(R.drawable.wallpaper_gradient_4)
                    }
                    visible()
                }
            }
            else -> {
                cover.apply {
                    setImageDrawable(null)
                    setBackgroundColor(0)
                    gone()
                }
            }
        }
    }
}