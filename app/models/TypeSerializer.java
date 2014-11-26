package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.Type;

import java.io.IOException;

public class TypeSerializer extends JsonSerializer<Type> {
    @Override
    public void serialize(Type type, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("label", type.getLabel());
        jgen.writeFieldName("unit");
        jgen.writeObject(type.getUnit());
        jgen.writeEndObject();
    }
}
