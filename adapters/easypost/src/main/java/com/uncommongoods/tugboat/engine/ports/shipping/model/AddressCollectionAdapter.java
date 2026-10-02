package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Address;
import com.easypost.model.AddressCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressCollectionAdapter extends PaginatedCollectionAdapter<IAddress, Address, AddressCollection> implements IAddressCollection {

    public AddressCollectionAdapter(AddressCollection collection) {
        super(collection);
    }

    @Override
    public List<IAddress> getAddresses() {
        List<Address> addresses = collection.getAddresses();
        if (addresses == null) {
            return null;
        }

        List<IAddress> adaptedAddresses = new ArrayList<>();
        for (Address address : addresses) {
            adaptedAddresses.add(new AddressAdapter(address));
        }

        return adaptedAddresses;
    }

    @Override
    protected List<Address> convertInterfaceToConcreteList(List<IAddress> interfaceEntries) {
        if (interfaceEntries == null) {
            return null;
        }

        List<Address> concreteEntries = new ArrayList<>();
        for (IAddress item : interfaceEntries) {
            if (item instanceof AddressAdapter) {
                concreteEntries.add(((AddressAdapter) item).getAddress());
            } else {
                throw new IllegalArgumentException("Address must be an AddressAdapter");
            }
        }

        return concreteEntries;
    }

    @Override
    public Map<String, Object> buildNextPageParameters(List<IAddress> entries, Integer pageSize) throws EndOfPaginationException {
        if (entries == null || entries.isEmpty()) {
            throw new EndOfPaginationException("No current entries to paginate from");
        }

        String lastId = entries.get(entries.size() - 1).getId();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("before_id", lastId);

        if (pageSize != null) {
            parameters.put("page_size", pageSize);
        }

        return parameters;
    }
}
