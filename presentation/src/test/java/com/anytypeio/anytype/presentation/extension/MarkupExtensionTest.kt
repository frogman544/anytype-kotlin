package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.presentation.page.editor.Markup
import org.junit.Test

class MarkupExtensionTest {

    @Test
    fun `should not update marks ranges when length is zero`() {
        val given = listOf(
            Markup.Mark(
                from = 10,
                to = 15,
                type = Markup.Type.BOLD
            )
        )

        val from = 0
        val length = 0

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark(
                from = 10,
                to = 15,
                type = Markup.Type.BOLD
            )
        )

        kotlin.test.assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should not update marks ranges when no marks after from`() {
        val given = listOf(
            Markup.Mark(
                from = 0,
                to = 5,
                type = Markup.Type.BOLD
            )
        )

        val from = 6
        val length = 13

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark(
                from = 0,
                to = 5,
                type = Markup.Type.BOLD
            )
        )

        kotlin.test.assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with length`() {
        val given = listOf(
            Markup.Mark(
                from = 0,
                to = 5,
                type = Markup.Type.BOLD
            ),
            Markup.Mark(
                from = 23,
                to = 31,
                type = Markup.Type.STRIKETHROUGH
            ),
            Markup.Mark(
                from = 23,
                to = 31,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 32,
                to = 43,
                type = Markup.Type.LINK,
                param = "https://anytype.io/"
            )
        )

        val from = 6
        val length = 13

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark(
                from = 0,
                to = 5,
                type = Markup.Type.BOLD
            ),
            Markup.Mark(
                from = 36,
                to = 44,
                type = Markup.Type.STRIKETHROUGH
            ),
            Markup.Mark(
                from = 36,
                to = 44,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 45,
                to = 56,
                type = Markup.Type.LINK,
                param = "https://anytype.io/"
            )
        )

        kotlin.test.assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with length add overlay`() {
        val given = listOf(
            Markup.Mark(
                from = 0,
                to = 10,
                type = Markup.Type.BOLD
            ),
            Markup.Mark(
                from = 3,
                to = 8,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 23,
                to = 31,
                type = Markup.Type.STRIKETHROUGH
            ),
            Markup.Mark(
                from = 32,
                to = 43,
                type = Markup.Type.LINK,
                param = "https://anytype.io/"
            )
        )

        val from = 4
        val length = 5

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark(
                from = 0,
                to = 15,
                type = Markup.Type.BOLD
            ),
            Markup.Mark(
                from = 3,
                to = 13,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 28,
                to = 36,
                type = Markup.Type.STRIKETHROUGH
            ),
            Markup.Mark(
                from = 37,
                to = 48,
                type = Markup.Type.LINK,
                param = "https://anytype.io/"
            )
        )

        kotlin.test.assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with negative length 1`() {
        val given = listOf(
            Markup.Mark(
                from = 10,
                to = 15,
                type = Markup.Type.BOLD
            )
        )

        val from = 4
        val length = -3

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark(
                from = 7,
                to = 12,
                type = Markup.Type.BOLD
            )
        )

        kotlin.test.assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with negative length 2`() {
        val given = listOf(
            Markup.Mark(
                from = 0,
                to = 8,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 10,
                to = 15,
                type = Markup.Type.BOLD
            )
        )

        val from = 2
        val length = -3

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark(
                from = 0,
                to = 5,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 7,
                to = 12,
                type = Markup.Type.BOLD
            )
        )

        kotlin.test.assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with negative length 3`() {
        val given = listOf(
            Markup.Mark(
                from = 0,
                to = 10,
                type = Markup.Type.BOLD
            ),
            Markup.Mark(
                from = 3,
                to = 8,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 23,
                to = 31,
                type = Markup.Type.STRIKETHROUGH
            ),
            Markup.Mark(
                from = 32,
                to = 43,
                type = Markup.Type.LINK,
                param = "https://anytype.io/"
            )
        )

        val from = 4
        val length = -3

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark(
                from = 0,
                to = 7,
                type = Markup.Type.BOLD
            ),
            Markup.Mark(
                from = 3,
                to = 5,
                type = Markup.Type.ITALIC
            ),
            Markup.Mark(
                from = 20,
                to = 28,
                type = Markup.Type.STRIKETHROUGH
            ),
            Markup.Mark(
                from = 29,
                to = 40,
                type = Markup.Type.LINK,
                param = "https://anytype.io/"
            )
        )

        kotlin.test.assertEquals(
            expected = expected,
            actual = result
        )
    }
}