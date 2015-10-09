package com.doodeec.utils.network.converters;

/**
 * @author Dusan Bartos
 */
public interface IConverter<InputType, ResultType> {

    ResultType convert(InputType input, Class<ResultType> resultClass) throws OutOfMemoryError;
}
