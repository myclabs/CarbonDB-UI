package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.Coefficient;
import com.mycsense.carbondb.domain.DerivedRelation;
import com.mycsense.carbondb.domain.Group;

import java.io.IOException;

public class CoefficientSerializer extends JsonSerializer<Coefficient> {
    @Override
    public void serialize(Coefficient coefficient, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
            jgen.writeStringField("id", coefficient.getId());
            jgen.writeFieldName("unit");
            jgen.writeObject(coefficient.getUnit());
            jgen.writeFieldName("keywords");
            jgen.writeObject(coefficient.getKeywords());
            jgen.writeFieldName("value");
            jgen.writeObject(coefficient.getValue());

            jgen.writeFieldName("groups");
            jgen.writeStartArray();
            for (Group group : coefficient.getGroups()) {
                jgen.writeStartObject();
                jgen.writeStringField("id", group.getId());
                jgen.writeStringField("label", group.getLabel());
                jgen.writeEndObject();
            }
            jgen.writeEndArray();

            jgen.writeFieldName("relations");
            jgen.writeStartArray();
            for (DerivedRelation relation : coefficient.getDerivedRelations()) {
                jgen.writeObject(relation);
            }
            jgen.writeEndArray();
        jgen.writeEndObject();
    }
}
