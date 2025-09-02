package tachiyomi.data.source

import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.MetadataMangasPage
import exh.metadata.metadata.EHentaiSearchMetadata
import exh.metadata.metadata.RaisedSearchMetadata
import mihon.domain.manga.model.toDomainManga
import tachiyomi.domain.manga.model.Manga

abstract class EHentaiPagingSource(
    override val source: CatalogueSource,
) : BaseSourcePagingSource(source) {

    private fun applySorting(
        data: List<Pair<Manga, RaisedSearchMetadata?>>,
        sortType: String?,
        ascending: Boolean,
    ): List<Pair<Manga, RaisedSearchMetadata?>> {
        if (sortType.isNullOrEmpty() || sortType == "None") {
            return data
        }

        return data.sortedWith { a, b ->
            val metadataA = a.second as? EHentaiSearchMetadata
            val metadataB = b.second as? EHentaiSearchMetadata

            val comparison = when (sortType) {
                "Score" -> {
                    val ratingA = metadataA?.averageRating ?: 0.0
                    val ratingB = metadataB?.averageRating ?: 0.0
                    ratingA.compareTo(ratingB)
                }
                "Pages" -> {
                    val pagesA = metadataA?.length ?: 0
                    val pagesB = metadataB?.length ?: 0
                    pagesA.compareTo(pagesB)
                }
                "Date" -> {
                    val dateA = metadataA?.datePosted ?: 0L
                    val dateB = metadataB?.datePosted ?: 0L
                    dateA.compareTo(dateB)
                }
                "Favorites" -> {
                    val favA = metadataA?.favorites ?: 0
                    val favB = metadataB?.favorites ?: 0
                    favA.compareTo(favB)
                }
                "Size" -> {
                    val sizeA = metadataA?.size ?: 0L
                    val sizeB = metadataB?.size ?: 0L
                    sizeA.compareTo(sizeB)
                }
                else -> 0
            }

            if (ascending) comparison else -comparison
        }
    }

    override suspend fun getPageLoadResult(
        params: LoadParams<Long>,
        mangasPage: MangasPage,
    ): LoadResult.Page<Long, Pair<Manga, RaisedSearchMetadata?>> {
        mangasPage as MetadataMangasPage
        val metadata = mangasPage.mangasMetadata

        val manga = mangasPage.mangas.map { it.toDomainManga(source.id) }
            .let { networkToLocalManga(it) }

        val data = manga.mapIndexed { index, sManga -> sManga to metadata.getOrNull(index) }

        // Apply sorting if this is a search source with filters
        val sortedData = if (this is EHentaiSearchPagingSource) {
            val sortFilters = filters.filterIsInstance<Filter.Sort>()
            val sortFilter = sortFilters.firstOrNull { it.name == "Local Sort" }
            if (sortFilter != null && sortFilter.state != null) {
                val selection = sortFilter.state!!
                val sortOptions = arrayOf("None", "Score", "Pages", "Date", "Favorites", "Size")
                val sortType = sortOptions.getOrNull(selection.index)
                applySorting(data, sortType, selection.ascending)
            } else {
                data
            }
        } else {
            data
        }

        return LoadResult.Page(
            data = sortedData,
            prevKey = null,
            nextKey = mangasPage.nextKey,
        )
    }
}

class EHentaiSearchPagingSource(
    source: CatalogueSource,
    val query: String,
    val filters: FilterList,
) : EHentaiPagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): MangasPage {
        return source.getSearchManga(currentPage, query, filters)
    }
}

class EHentaiPopularPagingSource(source: CatalogueSource) : EHentaiPagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): MangasPage {
        return source.getPopularManga(currentPage)
    }
}

class EHentaiLatestPagingSource(source: CatalogueSource) : EHentaiPagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): MangasPage {
        return source.getLatestUpdates(currentPage)
    }
}
