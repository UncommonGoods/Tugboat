package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;

/**
 * Generic interface for paginated collections
 *
 * @param <T> the type of the items in the collection
 */
public interface IPaginatedCollection<T> extends IEasyPostResource {
    /**
     * Check if there are more pages to retrieve.
     *
     * @return true if there are more pages to retrieve, otherwise false
     */
    Boolean getHasMore();

    /**
     * Get the next page of this collection using a function to retrieve it.
     *
     * @param apiCallFunction Function to retrieve the next page.
     * @param currentEntries  The list of items in the current collection.
     * @param <C>             The type of the collection to retrieve.
     * @return The next page of this collection.
     * @throws EndOfPaginationException if there is no next page.
     */
    <C extends IPaginatedCollection<T>> C getNextPage(
        Function<Map<String, Object>, C> apiCallFunction,
        List<T> currentEntries) throws EndOfPaginationException;

    /**
     * Get the next page of this collection using a function to retrieve it and a custom page size.
     *
     * @param apiCallFunction Function to retrieve the next page.
     * @param currentEntries  The list of items in the current collection.
     * @param pageSize        The size of the next page to retrieve.
     * @param <C>             The type of the collection to retrieve.
     * @return The next page of this collection.
     * @throws EndOfPaginationException if there is no next page.
     */
    <C extends IPaginatedCollection<T>> C getNextPage(
        Function<Map<String, Object>, C> apiCallFunction,
        List<T> currentEntries,
        Integer pageSize) throws EndOfPaginationException;

    /**
     * Build parameters to retrieve the next page.
     *
     * @param entries  The list of items in the current collection.
     * @param pageSize The size of the next page to retrieve.
     * @return Map of parameters for retrieving the next page.
     * @throws EndOfPaginationException if there is no next page.
     */
    Map<String, Object> buildNextPageParameters(List<T> entries, Integer pageSize) throws EndOfPaginationException;
}
