package com.anytypeio.anytype.presentation.sets.sort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsChangeSortValueEvent
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ModifyViewerSortViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val analytics: Analytics,
    private val storeOfRelations: StoreOfRelations
) : BaseViewModel() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    val viewState = MutableStateFlow<ViewState?>(null)
    private val jobs = mutableListOf<Job>()

    fun onStart(sortId: Id, relationKey: Key) {
        Timber.d("onStart, sortId: [$sortId], relationKey:[$relationKey]")
        jobs += viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect { state ->
                val viewer = state.viewerById(session.currentViewerId.value) ?: return@collect
                val sort = viewer.sorts.find { it.id == sortId }
                if (sort != null) {
                    val relation = storeOfRelations.getByKey(relationKey)
                    if (relation != null) {
                        viewState.value = ViewState(
                            format = relation.format,
                            type = sort.type,
                            name = relation.name.orEmpty()
                        )
                    } else {
                        Timber.e("Couldn't find relation in StoreOfRelations by relationKey:[$relationKey]")
                    }
                } else {
                    Timber.e("Couldn't find sort in view:[$viewer] by relation:[$relationKey]")
                }
            }
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    fun onSortDescSelected(ctx: Id, sortId: Id) {
        Timber.d("onSortDescSelected, ctx:[$ctx], sortId:[$sortId]")
        proceedWithUpdatingSortType(
            ctx = ctx,
            sortId = sortId,
            type = DVSortType.DESC
        )
    }

    fun onSortAscSelected(ctx: Id, sortId: Id) {
        Timber.d("onSortDescSelected, ctx:[$ctx], sortId:[$sortId]")
        proceedWithUpdatingSortType(
            ctx = ctx,
            sortId = sortId,
            type = DVSortType.ASC
        )
    }

    private fun proceedWithUpdatingSortType(
        ctx: Id,
        sortId: Id,
        type: Block.Content.DataView.Sort.Type
    ) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        val sort = viewer.sorts.find { it.id == sortId }
        if (sort == null) {
            Timber.e("Couldn't find sort in view:[$viewer] by sortId:[$sortId]")
            return
        }
        viewModelScope.launch {
            val params = UpdateDataViewViewer.Params.Sort.Replace(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                view = viewer.id,
                sort = sort.copy(type = type)
            )
            updateDataViewViewer(params).process(
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsChangeSortValueEvent(analytics)
                        isDismissed.emit(true)
                    }
                },
                failure = { Timber.e(it, "Error while updating sort type") }
            )
        }
    }

    class ViewState(
        val format: Relation.Format,
        val type: DVSortType,
        val name: String
    )

    class Factory(
        private val state: StateFlow<ObjectState>,
        private val dispatcher: Dispatcher<Payload>,
        private val session: ObjectSetSession,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val analytics: Analytics,
        private val storeOfRelations: StoreOfRelations
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ModifyViewerSortViewModel(
                objectState = state,
                dispatcher = dispatcher,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                analytics = analytics,
                storeOfRelations = storeOfRelations
            ) as T
        }
    }
}