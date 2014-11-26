package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.SourceRelation;

import java.io.IOException;

public class SourceRelationSerializer extends JsonSerializer<SourceRelation> {


    @Override
    public void serialize(SourceRelation relation, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("id", relation.getId());
        jgen.writeNumberField("exponent", relation.getExponent());
        jgen.writeFieldName("type");
        jgen.writeObject(relation.getType());
        jgen.writeFieldName("source");
        writeSimplifiedGroup(relation.getSource(), jgen);
        jgen.writeFieldName("coeff");
        writeSimplifiedGroup(relation.getCoeff(), jgen);
        jgen.writeFieldName("destination");
        writeSimplifiedGroup(relation.getDestination(), jgen);
        jgen.writeEndObject();
    }

    protected void writeSimplifiedGroup(Group group, JsonGenerator jgen)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("id", group.getId());
        jgen.writeStringField("label", group.getLabel());
        jgen.writeEndObject();
    }
}
