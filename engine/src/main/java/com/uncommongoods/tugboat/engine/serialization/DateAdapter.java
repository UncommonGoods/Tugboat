// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;


public class DateAdapter extends TypeAdapter<java.util.Date> {
    private static final java.text.SimpleDateFormat ISO_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        ISO_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void write(JsonWriter out, java.util.Date value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            synchronized (ISO_FORMAT) {
                out.value(ISO_FORMAT.format(value));
            }
        }
    }

    @Override
    public java.util.Date read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String dateStr = in.nextString();
        try {
            synchronized (ISO_FORMAT) {
                return ISO_FORMAT.parse(dateStr);
            }
        } catch (java.text.ParseException e) {
            throw new IOException("Failed to parse date: " + dateStr, e);
        }
    }
}
