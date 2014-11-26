package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.Coefficient;
import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.ElementaryFlow;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.Impact;
import com.mycsense.carbondb.domain.Keyword;
import com.mycsense.carbondb.domain.Reference;
import com.mycsense.carbondb.domain.SingleElement;
import com.mycsense.carbondb.domain.SourceRelation;
import com.mycsense.carbondb.domain.group.Type;
import com.mycsense.carbondb.domain.Process;

import java.io.IOException;
import java.util.Arrays;

public class GroupSerializer  extends JsonSerializer<Group> {
    @Override
    public void serialize(Group group, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("id", group.getId());
        jgen.writeStringField("comment", group.getComment());
        jgen.writeStringField("label", group.getLabel());
        jgen.writeStringField("unit", group.getUnit().getSymbol());
        jgen.writeFieldName("type");
        jgen.writeObject(group.getType());

        jgen.writeFieldName("commonKeywords");
        jgen.writeObject(group.getCommonKeywords().keywords);

        jgen.writeFieldName("dimensions");
        jgen.writeStartArray();
        for (Dimension dim : group.getDimSet().dimensions) {
            jgen.writeObject(dim);
        }
        jgen.writeEndArray();

        jgen.writeFieldName("overlap");
        jgen.writeStartArray();
        for (Group otherGroup : group.getOverlappingGroups()) {
            jgen.writeStartObject();
            jgen.writeStringField(otherGroup.getId(), otherGroup.getLabel());
            jgen.writeEndObject();
        }
        jgen.writeEndArray();

        jgen.writeFieldName("references");
        jgen.writeStartArray();
        for (Reference ref : group.getReferences()) {
            jgen.writeObject(ref);
        }
        jgen.writeEndArray();

        jgen.writeFieldName("sourceRelations");
        jgen.writeStartArray();
        for (SourceRelation sourceRelation : group.getSourceRelations()) {
            jgen.writeStartObject();
            jgen.writeObject(sourceRelation);
            jgen.writeEndObject();
        }
        jgen.writeEndArray();

        jgen.writeFieldName("elementsURI");
        jgen.writeStartObject();
        if (group.getId().equals("gp/direct_emission_of_greenhouse_gas")) {
            System.out.println("number of elements = " + group.getElements().size());
        }
        for (SingleElement element : group.getElements()) {
            jgen.writeStringField(joinDimensionKeywords(element.getKeywords()), element.getId());
        }
        jgen.writeEndObject();

        if (group.getType() == Type.COEFFICIENT) {
            jgen.writeFieldName("elementsValue");
            jgen.writeStartObject();
            for (SingleElement element : group.getElements()) {
                Coefficient coeff = (Coefficient) element;
                jgen.writeFieldName(coeff.getId());
                jgen.writeStartObject();
                jgen.writeNumberField("value", coeff.getValue().value);
                jgen.writeNumberField("uncertainty", coeff.getValue().uncertainty);
                jgen.writeEndObject();
            }
            jgen.writeEndObject();
        }
        else {
            jgen.writeFieldName("elementsImpactsAndFlows");
            jgen.writeStartObject();
            for (SingleElement element : group.getElements()) {
                Process process = (Process) element;
                jgen.writeFieldName(process.getId());
                jgen.writeStartObject();
                for (ElementaryFlow flow : process.getCalculatedFlows().values()) {
                    jgen.writeFieldName(flow.getType().getId());
                    jgen.writeStartObject();
                    jgen.writeNumberField("value", flow.getValue().value);
                    jgen.writeNumberField("uncertainty", flow.getValue().uncertainty);
                    jgen.writeEndObject();
                }
                for (Impact impact : process.getImpacts().values()) {
                    jgen.writeFieldName(impact.getType().getId());
                    jgen.writeStartObject();
                    jgen.writeNumberField("value", impact.getValue().value);
                    jgen.writeNumberField("uncertainty", impact.getValue().uncertainty);
                    jgen.writeEndObject();
                }
                jgen.writeEndObject();
            }
            jgen.writeEndObject();
        }

        jgen.writeEndObject();
    }

    /**
     * Create a string containing all the keywords URI alphabetically sorted
     */
    protected static String joinDimensionKeywords(Dimension dimension) {
        String output = "";
        String[] keywords = new String[dimension.keywords.size()];
        int i = 0;
        for (Keyword keyword: dimension.keywords) {
            keywords[i] = keyword.getId();
            i++;
        }
        Arrays.sort(keywords);
        for (i = 0; i < keywords.length; i++) {
            output += keywords[i];
        }
        return output;
    }
}
