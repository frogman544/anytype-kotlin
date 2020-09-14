package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget

class BulletedBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Text.Bulleted

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_bulleted_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<TextInputWidget>(R.id.bulletedListContent).apply {
            enableReadMode()
            setBlockText(this, block.text, block)
            processTextColor(
                textView = this,
                colorImage = colorView,
                color = block.color,
                imageView = view.findViewById(R.id.bullet)
            )
        }
        processBackgroundColor(
            root = view.findViewById(R.id.root),
            color = block.backgroundColor,
            bgImage = backgroundView
        )
        setConstraints()
    }
}