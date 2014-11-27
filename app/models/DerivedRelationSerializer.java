package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.DerivedRelation;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.SourceRelation;

import java.io.IOException;

public class DerivedRelationSerializer extends JsonSerializer<DerivedRelation> {
    @Override
    public void serialize(DerivedRelation derivedRelation, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
            jgen.writeNumberField("exponent", derivedRelation.getExponent());
            jgen.writeStringField("sourceId", derivedRelation.getSource().getId());
            jgen.writeFieldName("sourceUnit");
            jgen.writeObject(derivedRelation.getSource().getUnit());
            jgen.writeFieldName("source");
            jgen.writeObject(derivedRelation.getSource().getKeywords());
            jgen.writeStringField("coeffId", derivedRelation.getCoeff().getId());
            jgen.writeFieldName("coeffUnit");
            jgen.writeObject(derivedRelation.getCoeff().getUnit());
            jgen.writeFieldName("coeff");
            jgen.writeObject(derivedRelation.getCoeff().getKeywords());
            jgen.writeStringField("destinationId", derivedRelation.getDestination().getId());
            jgen.writeFieldName("destinationUnit");
            jgen.writeObject(derivedRelation.getDestination().getUnit());
            jgen.writeFieldName("destination");
            jgen.writeObject(derivedRelation.getDestination().getKeywords());
            jgen.writeFieldName("sourceRelation");
            jgen.writeStartObject();
            SourceRelation sourceRelation = derivedRelation.getSourceRelation();
            jgen.writeStringField("id", sourceRelation.getId());
            jgen.writeNumberField("exponent", sourceRelation.getExponent());

            jgen.writeFieldName("type");
            jgen.writeObject(sourceRelation.getType());

            jgen.writeFieldName("source");
            writeSimplifiedGroup(sourceRelation.getSource(), jgen);

            jgen.writeFieldName("coeff");
            writeSimplifiedGroup(sourceRelation.getCoeff(), jgen);

            jgen.writeFieldName("destination");
            writeSimplifiedGroup(sourceRelation.getDestination(), jgen);
            jgen.writeEndObject();
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
