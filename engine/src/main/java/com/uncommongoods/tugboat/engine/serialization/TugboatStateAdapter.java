// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.state.State;
import com.uncommongoods.tugboat.engine.state.TugboatStateBase;

import java.io.IOException;
import java.util.List;

public class TugboatStateAdapter extends TypeAdapter<TugboatStateBase> {
    @Override
    public void write(JsonWriter out, TugboatStateBase value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getState().name());
        }
    }

    @Override
    public TugboatStateBase read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String stateName = in.nextString();
        State state = State.valueOf(stateName);
        // Return a temporary state holder that can provide the State enum
        return new TugboatStateHolder(state);
    }

    // Temporary state holder for deserialization
    private static class TugboatStateHolder extends TugboatStateBase {
        private final State state;

        public TugboatStateHolder(State state) {
            super(null); // No tugboat reference during deserialization
            this.state = state;
            this.packageState = state;
        }

        @Override
        public State getState() {
            return state;
        }

        // All other methods should not be called on this temporary holder
        @Override public TugboatStateBase initialize() { throw new UnsupportedOperationException(); }
        @Override public TugboatStateBase rate() { throw new UnsupportedOperationException(); }
        @Override public TugboatStateBase shop() { throw new UnsupportedOperationException(); }
        @Override public TugboatStateBase purchase() { throw new UnsupportedOperationException(); }
        @Override public List<IPostageLabel> print() { throw new UnsupportedOperationException(); }
        @Override public List<IPostageLabel> reprint() { throw new UnsupportedOperationException(); }
        @Override public TugboatStateBase voidLabel() { throw new UnsupportedOperationException(); }
    }
}
