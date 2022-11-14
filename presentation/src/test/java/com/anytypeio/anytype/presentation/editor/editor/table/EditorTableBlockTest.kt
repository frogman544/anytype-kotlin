package com.anytypeio.anytype.presentation.editor.editor.table

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLayoutColumns
import com.anytypeio.anytype.core_models.StubLayoutRows
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTableCells
import com.anytypeio.anytype.core_models.StubTableColumns
import com.anytypeio.anytype.core_models.StubTableRows
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals

class EditorTableBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not amend second empty cell click`() {

        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(
            columns = listOf(columns[0], columns[1], columns[2]),
            rows = listOf(rows[0], rows[1])
        )
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(
            id = root,
            children = listOf(header.id, table.id)
        )

        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells

        stubInterceptEvents()
        stubOpenDocument(document)
        stubFillRow()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(
                ListenerType.TableEmptyCell(
                    cell = BlockView.Table.Cell(
                        rowId = rows[0].id,
                        columnId = columns[1].id,
                        rowIndex = BlockView.Table.RowIndex(0),
                        columnIndex = BlockView.Table.ColumnIndex(1),
                        block = null,
                        cellIndex = 0,
                        tableId = table.id
                    )
                )
            )
            onClickListener(
                ListenerType.TableEmptyCell(
                    cell = BlockView.Table.Cell(
                        rowId = rows[1].id,
                        columnId = columns[0].id,
                        rowIndex = BlockView.Table.RowIndex(1),
                        columnIndex = BlockView.Table.ColumnIndex(0),
                        block = null,
                        cellIndex = 1,
                        tableId = table.id
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(50L)

        runBlocking {
            val inOrder = inOrder(fillTableRow)
            inOrder.verify(fillTableRow).invoke(
                params = FillTableRow.Params(ctx = root, targetIds = listOf(rows[0].id))
            )
            inOrder.verify(fillTableRow).invoke(
                params = FillTableRow.Params(ctx = root, targetIds = listOf(rows[1].id))
            )
        }
    }

    @Test
    fun `should not amend second text cell click`() {

        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(
            id = root,
            children = listOf(header.id, table.id)
        )

        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells

        stubInterceptEvents()
        stubOpenDocument(document)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(
                ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        rowId = rows[0].id,
                        columnId = columns[1].id,
                        rowIndex = BlockView.Table.RowIndex(0),
                        columnIndex = BlockView.Table.ColumnIndex(1),
                        block = BlockView.Text.Paragraph(
                            id = cells[0].id,
                            text = cells[0].content.asText().text
                        ),
                        cellIndex = 0,
                        tableId = table.id
                    )
                )
            )
            onClickListener(
                ListenerType.TableTextCell(
                    cell = BlockView.Table.Cell(
                        rowId = rows[1].id,
                        columnId = columns[0].id,
                        rowIndex = BlockView.Table.RowIndex(1),
                        columnIndex = BlockView.Table.ColumnIndex(0),
                        block = BlockView.Text.Paragraph(
                            id = cells[1].id,
                            text = cells[1].content.asText().text
                        ),
                        cellIndex = 1,
                        tableId = table.id
                    )
                )
            )
        }


        val selectedState = vm.currentSelection()
        runBlocking {
            assertEquals(0, selectedState.size)
            verifyNoInteractions(fillTableRow)
        }
    }
}