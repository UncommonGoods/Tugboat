package com.uncommongoods.tugboat.engine.hooks.impl;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.hooks.TugboatHook;
import java.time.*;
import java.util.UUID;

public class InitialHookImpl implements TugboatHook {

    public InitialHookImpl() {
    }

    @Override
    public void execute(Tugboat tugboat) throws TugboatException {
        tugboat.setReference("WH-"+tugboat.getCargoId()+"-"+(UUID.randomUUID().toString().substring(0,7)));
        tugboat.getOptions().setSelectLowestRate(false);
    }
}
