package com.example.kemono.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kemono.data.model.Post

class PostPagingSource(
    private val fetchPosts: suspend (limit: Int, offset: Int) -> List<Post>
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val currentOffset = params.key ?: 0
        val limit = 50 // Fixed limit matching Kemono API conventions

        return try {
            val response = fetchPosts(limit, currentOffset)
            
            LoadResult.Page(
                data = response,
                prevKey = if (currentOffset == 0) null else Math.max(0, currentOffset - limit),
                nextKey = if (response.isEmpty()) null else currentOffset + response.size
            )
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 400) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (currentOffset == 0) null else Math.max(0, currentOffset - limit),
                    nextKey = null
                )
            } else {
                LoadResult.Error(e)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(50) ?: anchorPage?.nextKey?.minus(50)
        }
    }
}
