package com.anytypeio.anytype.presentation.relations.value.`object`

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.getSpaceWithTechSpace
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.toView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.filterIdsById
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectValueViewModel(
    private val viewModelParams: ViewModelParams,
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val objectSearch: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val gradientProvider: SpaceGradientProvider,
    private val objectListIsArchived: SetObjectListIsArchived,
    private val duplicateObject: DuplicateObject
) : BaseViewModel() {

    val viewState = MutableStateFlow<ObjectValueViewState>(ObjectValueViewState.Loading())
    private val query = MutableSharedFlow<String>(replay = 1)
    private var isEditableRelation = false
    val commands = MutableSharedFlow<Command>(replay = 0)

    private val initialIds = mutableListOf<Id>()
    private var isInitialSortDone = false

    init {
        Timber.d("ObjectValueViewModel init, params: $viewModelParams")
        viewModelScope.launch {
            val relation = relations.get(relation = viewModelParams.relationKey)
            setupIsRelationNotEditable(relation)
            combine(
                values.subscribe(
                    ctx = viewModelParams.ctx,
                    target = viewModelParams.objectId
                ),
                query.onStart { emit("") },
            ) { record, query ->
                val ids = getRecordValues(record)
                if (!isInitialSortDone) {
                    initialIds.clear()
                    if (ids.isNotEmpty()) {
                        initialIds.addAll(ids)
                    } else {
                        emitCommand(Command.Expand)
                    }
                }
                Pair(
                    ids, getSearchParams(
                        relation = relation,
                        query = query,
                        ids = ids
                    )
                )
            }.onEach { (ids, searchParams) ->
                objectSearch(params = searchParams).proceed(
                    success = { objects ->
                        initViewState(
                            relation = relation,
                            ids = ids,
                            objects = objects,
                        )
                    },
                    failure = { Timber.e(it, "Error while searching objects") }
                )
            }.collect()
        }
    }

    private suspend fun getSearchParams(
        relation: ObjectWrapper.Relation,
        query: String,
        ids: List<Id>
    ): SearchObjects.Params {
        return if (isEditableRelation) {
            SearchObjects.Params(
                keys = ObjectSearchConstants.defaultKeys,
                filters = ObjectSearchConstants.filterAddObjectToRelation(
                    spaces = spaceManager.getSpaceWithTechSpace(),
                    targetTypes = relation.relationFormatObjectTypes
                ),
                fulltext = query
            )
        } else {
            SearchObjects.Params(
                keys = ObjectSearchConstants.defaultKeys,
                filters = ObjectSearchConstants.filterObjectsByIds(
                    spaces = spaceManager.getSpaceWithTechSpace(),
                    ids = ids
                )
            )
        }
    }

    private fun emitCommand(command: Command, delay: Long = 0L) {
        viewModelScope.launch {
            delay(delay)
            commands.emit(command)
        }
    }

    fun onQueryChanged(input: String) {
        viewModelScope.launch {
            query.emit(input)
        }
    }

    private fun setupIsRelationNotEditable(relation: ObjectWrapper.Relation) {
        isEditableRelation = !(viewModelParams.isLocked
                || relation.isReadonlyValue
                || relation.isHidden == true
                || relation.isDeleted == true
                || relation.isArchived == true
                || !relation.isValid)
    }

    private suspend fun initViewState(
        relation: ObjectWrapper.Relation,
        ids: List<Id>,
        objects: List<ObjectWrapper.Basic>,
        query: String = ""
    ) {
        val views = mapObjects(ids, objects, query)
        viewState.value = if (views.isNotEmpty()) {
            ObjectValueViewState.Content(
                isEditableRelation = isEditableRelation,
                title = relation.name.orEmpty(),
                items = buildList {
                    val typeNames = mutableListOf<String>()
                    relation.relationFormatObjectTypes.forEach { it ->
                        storeOfObjectTypes.get(it)?.let { type ->
                            val name = type.name
                            if (!name.isNullOrBlank()) typeNames.add(name)
                        }
                    }
                    val objectTypeNames = typeNames.joinToString(", ")
                    if (isEditableRelation) add(ObjectValueItem.ObjectType(name = objectTypeNames))
                    addAll(views)
                }
            )
        } else {
            ObjectValueViewState.Empty(
                isEditableRelation = isEditableRelation,
                title = relation.name.orEmpty(),
            )
        }
    }

    private suspend fun mapObjects(
        ids: List<Id>,
        objects: List<ObjectWrapper.Basic>,
        query: String
    ): List<ObjectValueItem.Object> = objects.mapNotNull { obj ->
        if (!obj.isValid) return@mapNotNull null
        if (query.isNotBlank() && obj.name?.contains(query, true) == false) return@mapNotNull null
        val index = ids.indexOf(obj.id)
        val isSelected = index != -1
        val number = if (isSelected) index + 1 else Int.MAX_VALUE
        ObjectValueItem.Object(
            view = obj.toView(
                urlBuilder = urlBuilder,
                objectTypes = storeOfObjectTypes.getAll(),
                gradientProvider = gradientProvider
            ),
            isSelected = isSelected,
            number = number,
            restrictions = obj.restrictions
        )
    }.let { mappedOptions ->
        if (!isInitialSortDone) {
            isInitialSortDone = true
            mappedOptions.sortedWith(
                compareBy(
                    { !initialIds.contains(it.view.id) },
                    { it.number })
            )
        } else {
            mappedOptions.sortedWith(
                compareBy(
                    { !initialIds.contains(it.view.id) },
                    { initialIds.indexOf(it.view.id) })
            )
        }
    }

    private fun getRecordValues(record: Map<String, Any?>): List<Id> {
        return when (val value = record[viewModelParams.relationKey]) {
            is Id -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
    }

    private fun refreshObjects() {
        val currentQuery = query.replayCache.lastOrNull().orEmpty()
        onQueryChanged(currentQuery)
    }

    //region ACTIONS
    fun onAction(action: ObjectValueItemAction) {
        Timber.d("onAction, action: $action")
        if (!isEditableRelation && action is ObjectValueItemAction.Click) {
            onOpenObjectAction(action.item)
            return
        }
        if (!isEditableRelation && action !is ObjectValueItemAction.Open) {
            Timber.d("ObjectValueViewModel onAction, relation is not editable")
            sendToast("Relation is not editable")
            return
        }
        when (action) {
            ObjectValueItemAction.Clear -> onClearAction()
            is ObjectValueItemAction.Click -> onClickAction(action.item)
            is ObjectValueItemAction.Delete -> emitCommand(Command.DeleteObject(action.item.view.id))
            is ObjectValueItemAction.Duplicate -> onDuplicateAction(action.item)
            is ObjectValueItemAction.Open -> onOpenObjectAction(action.item)
        }
    }

    private fun onDuplicateAction(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            duplicateObject(item.view.id).process(
                success = {
                    Timber.d("Object ${item.view.id} duplicated")
                    refreshObjects()
                },
                failure = { Timber.e(it, "Error while duplicating object") }
            )
        }
    }

    fun onDeleteAction(objectId: Id) {
        val state = viewState.value as? ObjectValueViewState.Content ?: return
        val item = state.items.filterIsInstance<ObjectValueItem.Object>()
            .firstOrNull { it.view.id == objectId } ?: return
        viewModelScope.launch {
            val isSelected = item.isSelected
            if (isSelected) {
                removeObjectValue(item) {
                    proceedWithObjectDeletion(item)
                }
            } else {
                proceedWithObjectDeletion(item)
            }
        }
    }

    private suspend fun proceedWithObjectDeletion(item: ObjectValueItem.Object) {
        val params = SetObjectListIsArchived.Params(
            targets = listOf(item.view.id),
            isArchived = true
        )
        objectListIsArchived.async(params).fold(
            onSuccess = {
                Timber.d("Object ${item.view.id} archived")
                refreshObjects()
            },
            onFailure = { Timber.e(it, "Error while archiving object") }
        )
    }

    private fun onClearAction() {
        viewModelScope.launch {
            val params = UpdateDetail.Params(
                target = viewModelParams.objectId,
                key = viewModelParams.relationKey,
                value = null
            )
            setObjectDetails(params).process(
                failure = { Timber.e(it, "Error while clearing objects") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                })
        }
    }

    private fun onOpenObjectAction(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            val layout = item.view.layout
            if (layout.isDataView()) {
                commands.emit(
                    Command.OpenSet(
                        id = item.view.id,
                        space = item.view.space
                    )
                )
            } else {
                commands.emit(
                    Command.OpenObject(
                        id = item.view.id,
                        space = item.view.space
                    )
                )
            }
        }
    }

    private fun onClickAction(item: ObjectValueItem.Object) {
        if (item.isSelected) {
            viewModelScope.launch { removeObjectValue(item) }
        } else {
            addObjectValue(item)
        }
    }

    private fun addObjectValue(item: ObjectValueItem.Object) {
        viewModelScope.launch {
            val obj = values.get(ctx = viewModelParams.ctx, target = viewModelParams.objectId)
            val result = mutableListOf<Id>()
            val value = obj[viewModelParams.relationKey]
            if (value is List<*>) {
                result.addAll(value.typeOf())
            } else if (value is Id) {
                result.add(value)
            }
            result.add(item.view.id)
            setObjectDetails(
                UpdateDetail.Params(
                    target = viewModelParams.objectId,
                    key = viewModelParams.relationKey,
                    value = result
                )
            ).process(
                failure = { Timber.e(it, "Error while adding object") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    private suspend fun removeObjectValue(item: ObjectValueItem.Object, action: suspend () -> Unit = {}) {
        val obj = values.get(ctx = viewModelParams.ctx, target = viewModelParams.objectId)
        val value = obj[viewModelParams.relationKey].filterIdsById(item.view.id)
        setObjectDetails(
            UpdateDetail.Params(
                target = viewModelParams.objectId,
                key = viewModelParams.relationKey,
                value = value
            )
        ).process(
            failure = { Timber.e(it, "Error while removing object ${item.view.id}") },
            success = {
                dispatcher.send(it)
                viewModelScope.sendAnalyticsRelationValueEvent(analytics)
                action()
            }
        )
    }
    //endregion

    data class ViewModelParams(
        val ctx: Id,
        val space: SpaceId,
        val objectId: Id,
        val relationKey: Key,
        val isLocked: Boolean,
        val relationContext: RelationContext
    )

    sealed class Command {
        object Dismiss : Command()
        object Expand : Command()
        data class OpenObject(val id: Id, val space: Id) : Command()
        data class OpenSet(val id: Id, val space: Id) : Command()
        data class DeleteObject(val id: Id) : Command()
    }
}

sealed class ObjectValueViewState {
    abstract val isEditableRelation: Boolean

    data class Loading(
        override val isEditableRelation: Boolean = false
    ) : ObjectValueViewState()

    data class Empty(
        val title: String,
        override val isEditableRelation: Boolean
    ) : ObjectValueViewState()

    data class Content(
        val title: String,
        override val isEditableRelation: Boolean,
        val items: List<ObjectValueItem>,
    ) : ObjectValueViewState()
}

sealed class ObjectValueItemAction {
    data class Click(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    data class Delete(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    data class Duplicate(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    data class Open(val item: ObjectValueItem.Object) : ObjectValueItemAction()
    object Clear : ObjectValueItemAction()
}

sealed class ObjectValueItem {
    data class ObjectType(val name: String) : ObjectValueItem()
    data class Object(
        val view: DefaultObjectView,
        val isSelected: Boolean,
        val number: Int = Int.MAX_VALUE,
        val restrictions: List<ObjectRestriction> = emptyList()
    ) : ObjectValueItem()
}