package com.uncommongoods.tugboat.engine.hooks.impl;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.hooks.TugboatErrorHook;


public class ErrorHookImpl implements TugboatErrorHook {
    private TugboatException currentException;

    public ErrorHookImpl() {
    }

    @Override
    public void execute(Tugboat tugboat, Exception ex) throws TugboatException {
        this.currentException = new TugboatException(ex.getLocalizedMessage(), ex);
        modifyEngineError();
        addHazmatPrefix(tugboat);
        addInternationalPrefix(tugboat);
        addShipmentIdAndBarcode(tugboat);
        if (currentException != null) {
            throw currentException;
        }
    }

    private void modifyEngineError() throws TugboatException {
        String message = this.currentException.getLocalizedMessage();
        if (message != null && message.toLowerCase().contains("cannot go backwards from printed")) {
            this.currentException = new TugboatException("Already shipped. Must be voided to re-ship.");
        }
    }

    private void addHazmatPrefix(Tugboat tugboat) {
        if (tugboat.getOptions() != null && tugboat.getOptions().getHazmat() != null) {
            currentException = new TugboatException("\nHAZMAT\n" + this.currentException.getLocalizedMessage(), this.currentException);
        }
    }

    private void addInternationalPrefix(Tugboat tugboat){
        if (tugboat.getMetadata() != null && tugboat.getMetadata().has("isInternational") && tugboat.getMetadata().get("isInternational").getAsBoolean()) {
            currentException = new TugboatException("ESW International Error:\n" + this.currentException.getLocalizedMessage(), this.currentException);
        }
    }

    private void addShipmentIdAndBarcode(Tugboat tugboat){
        String carrierService = "";
        if (tugboat.getSelectedRate() != null) {
            carrierService = tugboat.getSelectedRate().getCarrier() + " " + tugboat.getSelectedRate().getService();
        }
        // Add barcode and shipment info as ZPL commands that will be embedded in the error message
        // Match the original TypeScript version - end with ^FD to leave text field open
        String barcodeZpl = "^FO41,250^BY4^B3N,N,200,N,N^FD" + tugboat.getCargoId() + "^FS^FO60,475^A0N,28,0^FDShipment ID: " + tugboat.getCargoId() + "^FS^FO60,510^A0N,28,0^FD" + carrierService + "^FS^FO50,503^FWN^A0,90,90^FB700,14,,^FD";
        String errMsg = barcodeZpl + "\n\n" + this.currentException.getLocalizedMessage();
        currentException = new TugboatException(errMsg, this.currentException);
    }
}
