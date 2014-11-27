package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.DerivedRelation;
import com.mycsense.carbondb.domain.ElementaryFlow;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.Impact;
import com.mycsense.carbondb.domain.Process;

import java.io.IOException;

public class ProcessSerializer extends JsonSerializer<Process> {
    @Override
    public void serialize(Process process, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
            jgen.writeStringField("id", process.getId());
            jgen.writeFieldName("unit");
            jgen.writeObject(process.getUnit());
            jgen.writeFieldName("keywords");
            jgen.writeObject(process.getKeywords());

            jgen.writeFieldName("groups");
            jgen.writeStartArray();
            for (Group group : process.getGroups()) {
                jgen.writeStartObject();
                jgen.writeStringField("id", group.getId());
                jgen.writeStringField("label", group.getLabel());
                jgen.writeEndObject();
            }
            jgen.writeEndArray();

            jgen.writeFieldName("relations");
            jgen.writeStartArray();
            for (DerivedRelation relation : process.getUpstreamDerivedRelations()) {
                jgen.writeObject(relation);
            }
            for (DerivedRelation relation : process.getDownstreamDerivedRelations()) {
                jgen.writeObject(relation);
            }
            jgen.writeEndArray();

            jgen.writeFieldName("impacts");
            jgen.writeStartObject();
            for (Impact impact : process.getImpacts().values()) {
                jgen.writeFieldName(impact.getType().getId());
                jgen.writeObject(impact.getValue());
            }
            jgen.writeEndObject();

            jgen.writeFieldName("flows");
            jgen.writeStartObject();
            for (ElementaryFlow flow : process.getCalculatedFlows().values()) {
                jgen.writeFieldName(flow.getType().getId());
                jgen.writeObject(flow.getValue());
            }
            jgen.writeEndObject();

        jgen.writeEndObject();
    }
}
