package com.anytypeio.anytype.presentation.sets.main

import MockDataFactory
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DuplicateDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.RenameDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.EditDataViewViewerViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class ObjectSetViewerDeleteTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var renameDataViewViewer: RenameDataViewViewer

    @Mock
    lateinit var deleteDataViewViewer: DeleteDataViewViewer

    @Mock
    lateinit var duplicateDataViewViewer: DuplicateDataViewViewer

    @Mock
    lateinit var setActiveViewer: SetActiveViewer

    private val ctx: Id = MockDataFactory.randomUuid()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not update active view after inactive view is deleted`() = runBlockingTest {
        // SETUP

        val firstViewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList(),
            type = DVViewerType.GRID,
            sorts = emptyList(),
            filters = emptyList()
        )

        val secondViewer = firstViewer.copy(id = MockDataFactory.randomUuid())
        val thirdViewer = firstViewer.copy(id = MockDataFactory.randomUuid())

        val activeViewerId = firstViewer.id

        val objectSetSession = ObjectSetSession().apply {
            currentViewerId = activeViewerId
        }

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                source = MockDataFactory.randomString(),
                relations = emptyList(),
                viewers = listOf(firstViewer, secondViewer, thirdViewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val objectSetState = MutableStateFlow(
            ObjectSet(
                blocks = listOf(
                    header,
                    title,
                    dv
                )
            )
        )

        stubRemoveDataViewViewer(
            dv = dv.id,
            viewer = secondViewer.id
        )

        val vm = buildViewModel(
            objectSetState = objectSetState,
            objectSetSession = objectSetSession
        )

        // TESTING

        vm.onDeleteClicked(
            ctx = ctx,
            viewer = secondViewer.id
        )

        verifyBlocking(deleteDataViewViewer, times(1)) {
            invoke(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    viewer = secondViewer.id,
                    dataview = dv.id
                )
            )
        }
        verifyNoMoreInteractions(setActiveViewer)
    }

    @Test
    fun `should set next view as active view if currently active view is being deleted`() {

        // SETUP

        val firstViewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList(),
            type = DVViewerType.GRID,
            sorts = emptyList(),
            filters = emptyList()
        )

        val secondViewer = firstViewer.copy(id = MockDataFactory.randomUuid())
        val thirdViewer = firstViewer.copy(id = MockDataFactory.randomUuid())

        val activeViewerId = firstViewer.id

        val objectSetSession = ObjectSetSession().apply {
            currentViewerId = activeViewerId
        }

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                source = MockDataFactory.randomString(),
                relations = emptyList(),
                viewers = listOf(firstViewer, secondViewer, thirdViewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val objectSetState = MutableStateFlow(
            ObjectSet(
                blocks = listOf(
                    header,
                    title,
                    dv
                )
            )
        )

        stubRemoveDataViewViewer(
            dv = dv.id,
            viewer = firstViewer.id
        )

        val vm = buildViewModel(
            objectSetState = objectSetState,
            objectSetSession = objectSetSession
        )

        // TESTING

        vm.onDeleteClicked(
            ctx = ctx,
            viewer = firstViewer.id
        )

        verifyBlocking(deleteDataViewViewer, times(1)) {
            invoke(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    viewer = firstViewer.id,
                    dataview = dv.id
                )
            )
        }
        verifyBlocking(setActiveViewer, times(1)) {
            invoke(
                SetActiveViewer.Params(
                    context = ctx,
                    block = dv.id,
                    view = secondViewer.id,
                    offset = 0,
                    limit = ObjectSetConfig.DEFAULT_LIMIT
                )
            )
        }
    }

    @Test
    fun `should set prevous view as active view if currently active view is being deleted`() {

        // SETUP

        val firstViewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList(),
            type = DVViewerType.GRID,
            sorts = emptyList(),
            filters = emptyList()
        )

        val secondViewer = firstViewer.copy(id = MockDataFactory.randomUuid())
        val thirdViewer = firstViewer.copy(id = MockDataFactory.randomUuid())

        val activeViewerId = thirdViewer.id

        val objectSetSession = ObjectSetSession().apply {
            currentViewerId = activeViewerId
        }

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                source = MockDataFactory.randomString(),
                relations = emptyList(),
                viewers = listOf(firstViewer, secondViewer, thirdViewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val objectSetState = MutableStateFlow(
            ObjectSet(
                blocks = listOf(
                    header,
                    title,
                    dv
                )
            )
        )

        stubRemoveDataViewViewer(
            dv = dv.id,
            viewer = thirdViewer.id
        )

        val vm = buildViewModel(
            objectSetState = objectSetState,
            objectSetSession = objectSetSession
        )

        // TESTING

        vm.onDeleteClicked(
            ctx = ctx,
            viewer = thirdViewer.id
        )

        verifyBlocking(deleteDataViewViewer, times(1)) {
            invoke(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    viewer = thirdViewer.id,
                    dataview = dv.id
                )
            )
        }
        verifyBlocking(setActiveViewer, times(1)) {
            invoke(
                SetActiveViewer.Params(
                    context = ctx,
                    block = dv.id,
                    view = secondViewer.id,
                    offset = 0,
                    limit = ObjectSetConfig.DEFAULT_LIMIT
                )
            )
        }
    }

    fun buildViewModel(
        objectSetState: StateFlow<ObjectSet>,
        objectSetSession: ObjectSetSession
    ): EditDataViewViewerViewModel {
        return EditDataViewViewerViewModel(
            renameDataViewViewer = renameDataViewViewer,
            deleteDataViewViewer = deleteDataViewViewer,
            duplicateDataViewViewer = duplicateDataViewViewer,
            setActiveViewer = setActiveViewer,
            objectSetSession = objectSetSession,
            objectSetState = objectSetState,
            dispatcher = dispatcher,
        )
    }

    private fun stubRemoveDataViewViewer(
        viewer: Id,
        dv: Id
    ) {
        deleteDataViewViewer.stub {
            onBlocking {
                invoke(
                    DeleteDataViewViewer.Params(
                        ctx = ctx,
                        viewer = viewer,
                        dataview = dv
                    )
                )
            } doReturn Either.Right(
                Payload(
                    context = ctx,
                    events = emptyList()
                )
            )
        }
    }

    private fun stubSetActiveViewer(
        viewer: Id,
        dv: Id
    ) {
        setActiveViewer.stub {
            onBlocking {
                invoke(
                    SetActiveViewer.Params(
                        context = ctx,
                        view = viewer,
                        block = dv,
                        offset = 0,
                        limit = ObjectSetConfig.DEFAULT_LIMIT
                    )
                )
            } doReturn Either.Right(
                Payload(
                    context = ctx,
                    events = emptyList()
                )
            )
        }
    }
}