package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder

class ObjectSearchViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val getFlavourConfig: GetFlavourConfig
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ObjectSearchViewModel(
            urlBuilder = urlBuilder,
            getObjectTypes = getObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            getFlavourConfig = getFlavourConfig
        ) as T
    }
}