package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Shipment;
import com.easypost.model.ShipmentCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipmentCollectionAdapter extends PaginatedCollectionAdapter<IShipment, Shipment, ShipmentCollection> implements IShipmentCollection {

    public ShipmentCollectionAdapter(ShipmentCollection collection) {
        super(collection);
    }

    @Override
    public List<IShipment> getShipments() {
        List<Shipment> shipments = collection.getShipments();
        if (shipments == null) {
            return null;
        }

        List<IShipment> adaptedShipments = new ArrayList<>();
        for (Shipment shipment : shipments) {
            adaptedShipments.add(new ShipmentAdapter(shipment));
        }

        return adaptedShipments;
    }

    @Override
    public Boolean getPurchased() {
        return collection.getPurchased();
    }

    @Override
    public void setPurchased(Boolean purchased) {
        collection.setPurchased(purchased);
    }

    @Override
    public Boolean getIncludeChildren() {
        return collection.getIncludeChildren();
    }

    @Override
    public void setIncludeChildren(Boolean includeChildren) {
        collection.setIncludeChildren(includeChildren);
    }

    @Override
    protected List<Shipment> convertInterfaceToConcreteList(List<IShipment> interfaceEntries) {
        if (interfaceEntries == null) {
            return null;
        }

        List<Shipment> concreteEntries = new ArrayList<>();
        for (IShipment item : interfaceEntries) {
            if (item instanceof ShipmentAdapter) {
                concreteEntries.add(((ShipmentAdapter) item).getShipment());
            } else {
                throw new IllegalArgumentException("Shipment must be a ShipmentAdapter");
            }
        }

        return concreteEntries;
    }

    @Override
    public Map<String, Object> buildNextPageParameters(List<IShipment> entries, Integer pageSize) throws EndOfPaginationException {
        if (entries == null || entries.isEmpty()) {
            throw new EndOfPaginationException("No current entries to paginate from");
        }

        String lastId = entries.get(entries.size() - 1).getId();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("before_id", lastId);

        if (pageSize != null) {
            parameters.put("page_size", pageSize);
        }

        // Include the special parameters for ShipmentCollection
        if (getPurchased() != null) {
            parameters.put("purchased", getPurchased());
        }

        if (getIncludeChildren() != null) {
            parameters.put("include_children", getIncludeChildren());
        }

        return parameters;
    }
}
