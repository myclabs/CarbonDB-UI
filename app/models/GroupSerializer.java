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
import org.apache.commons.lang3.StringUtils;

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
        jgen.writeFieldName("unit");
        jgen.writeObject(group.getUnit());
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
            jgen.writeStringField("id", otherGroup.getId());
            jgen.writeStringField("label", otherGroup.getLabel());
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
            jgen.writeObject(sourceRelation);
        }
        jgen.writeEndArray();

        jgen.writeFieldName("elementsURI");
        jgen.writeStartObject();
        for (SingleElement element : group.getElements()) {
            jgen.writeStringField(StringUtils.join(element.getKeywords().keywords, "+") + "+" + element.getUnit().getId(), element.getId());
        }
        jgen.writeEndObject();

        if (group.getType() == Type.COEFFICIENT) {
            jgen.writeFieldName("elementsValue");
            jgen.writeStartObject();
            for (SingleElement element : group.getElements()) {
                Coefficient coeff = (Coefficient) element;
                jgen.writeFieldName(StringUtils.join(element.getKeywords().keywords, "+") + "+" + element.getUnit().getId());
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
                jgen.writeFieldName(StringUtils.join(element.getKeywords().keywords, "+") + "+" + element.getUnit().getId());
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
