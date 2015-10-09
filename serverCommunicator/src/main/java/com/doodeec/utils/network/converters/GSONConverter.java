package com.doodeec.utils.network.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Dusan Bartos
 */
public class GSONConverter<T> implements IConverter<String, T> {

    private Gson mGson = new GsonBuilder().create();
    private Class<T> mClass;

    public void setGson(Gson gson) {
        if (gson == null) return;
        mGson = gson;
    }

    @Override
    public T convert(String input, Class<T> resultClass) {
        return mGson.fromJson(input, mClass);
    }
}
