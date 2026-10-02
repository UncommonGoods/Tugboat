package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.EasyPostResource;
import com.easypost.model.PaginatedCollection;


public abstract class PaginatedCollectionAdapter<T, U extends EasyPostResource, V extends PaginatedCollection<U>>
    extends EasyPostResource implements IPaginatedCollection<T> {

    protected final V collection;

    public PaginatedCollectionAdapter(V collection) {
        super();
        this.collection = collection;
    }

    @Override
    public Boolean getHasMore() {
        return collection.getHasMore();
    }

    @Override
    public <C extends IPaginatedCollection<T>> C getNextPage(
        Function<Map<String, Object>, C> apiCallFunction,
        List<T> currentEntries) throws EndOfPaginationException {
        return getNextPage(apiCallFunction, currentEntries, null);
    }

    @Override
    public <C extends IPaginatedCollection<T>> C getNextPage(
        Function<Map<String, Object>, C> apiCallFunction,
        List<T> currentEntries,
        Integer pageSize) throws EndOfPaginationException {

        if (currentEntries == null || currentEntries.isEmpty()) {
            throw new EndOfPaginationException("No current entries to paginate from");
        }

        if (!this.getHasMore()) {
            throw new EndOfPaginationException("No more pages available");
        }

        Map<String, Object> parameters = buildNextPageParameters(currentEntries, pageSize);

        return apiCallFunction.apply(parameters);
    }

    public abstract Map<String, Object> buildNextPageParameters(List<T> entries, Integer pageSize) throws EndOfPaginationException;

    protected abstract List<U> convertInterfaceToConcreteList(List<T> interfaceEntries);

    public V getCollection() {
        return collection;
    }
}
