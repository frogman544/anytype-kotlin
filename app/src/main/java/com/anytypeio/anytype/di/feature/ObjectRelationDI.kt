package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromObject
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationListFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentRelationModule::class])
@PerModal
interface DocumentRelationSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: DocumentRelationModule): Builder
        fun build(): DocumentRelationSubComponent
    }

    fun inject(fragment: RelationListFragment)
}

@Module
object DocumentRelationModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideObjectRelationViewModelFactory(
        stores: Editor.Storage,
        urlBuilder: UrlBuilder,
        dispatcher: Dispatcher<Payload>,
        updateDetail: UpdateDetail,
        detailModificationManager: DetailModificationManager,
        addToFeaturedRelations: AddToFeaturedRelations,
        removeFromFeaturedRelations: RemoveFromFeaturedRelations,
        deleteRelationFromObject: DeleteRelationFromObject,
        analytics: Analytics,
        storeOfRelations: StoreOfRelations
    ): ObjectRelationListViewModelFactory {
        return ObjectRelationListViewModelFactory(
            stores = stores,
            urlBuilder = urlBuilder,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            detailModificationManager = detailModificationManager,
            addToFeaturedRelations = addToFeaturedRelations,
            removeFromFeaturedRelations = removeFromFeaturedRelations,
            deleteRelationFromObject = deleteRelationFromObject,
            analytics = analytics,
            storeOfRelations = storeOfRelations
        )
    }

    @JvmStatic
    @Provides
    @PerModal
    fun addToFeaturedRelations(repo: BlockRepository): AddToFeaturedRelations =
        AddToFeaturedRelations(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun removeFromFeaturedRelations(repo: BlockRepository): RemoveFromFeaturedRelations =
        RemoveFromFeaturedRelations(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun deleteRelationFromObject(repo: BlockRepository): DeleteRelationFromObject =
        DeleteRelationFromObject(repo)
}