package com.uncommongoods.tugboat.engine.hooks.impl;

import com.google.gson.JsonObject;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.hooks.TugboatHook;

public class PrintedHookImpl implements TugboatHook {

    public PrintedHookImpl() {
    }

    @Override
    public void execute(Tugboat tugboat) throws TugboatException {
//        setLetterZPLAdjustments(tugboat);
    }

    private void setLetterZPLAdjustments(Tugboat tugboat) {
        JsonObject metaData = tugboat.getMetadata();
        boolean isLetter = metaData.has("isLetter") && metaData.get("isLetter").getAsBoolean();
        if (isLetter) {
            for(IPostageLabel label: tugboat.getPostageLabels()) {
                label.setLabelFile(label.getLabelFile().replace("FO52,810", "FO52,1200"));
                label.setLabelFile(label.getLabelFile().replace("FO400,270", "FO400,650"));
                label.setLabelFile(label.getLabelFile().replace("FO500,200", "FO500,500"));
            }
        }
    }
}
