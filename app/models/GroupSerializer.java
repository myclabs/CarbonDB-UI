package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.NotFoundException;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.group.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupSerializer  extends JsonSerializer<Group> {
    private final Logger log = LoggerFactory.getLogger(GroupSerializer.class);

    @Override
    public void serialize(Group group, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("id", group.getId());
        jgen.writeStringField("comment", parseReferencesInComment(group));
        jgen.writeStringField("label", group.getLabel());
        jgen.writeFieldName("unit");
        jgen.writeObject(group.getUnit());
        jgen.writeFieldName("type");
        jgen.writeObject(group.getType());

        jgen.writeFieldName("commonKeywords");
        jgen.writeObject(group.getCommonKeywords());

        jgen.writeFieldName("dimensions");
        jgen.writeStartArray();
        for (Dimension dim : group.getDimSet().dimensions) {
            jgen.writeStartObject();
            jgen.writeFieldName("id");
            jgen.writeObject(dim.getId());
            jgen.writeFieldName("keywords");
            jgen.writeObject(dim.keywords);
            jgen.writeFieldName("keywordsPosition");
            jgen.writeObject(dim.keywordsPosition);
            jgen.writeFieldName("orientation");
            jgen.writeObject(group.getDimSet().getDimensionOrientation(dim));
            jgen.writeEndObject();
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
            jgen.writeStringField(StringUtils.join(element.getKeywords(), "+") + "+" + element.getUnit().getId(), element.getId());
        }
        jgen.writeEndObject();

        if (group.getType() == Type.COEFFICIENT) {
            jgen.writeFieldName("elementsValue");
            jgen.writeStartObject();
            for (SingleElement element : group.getElements()) {
                Coefficient coeff = (Coefficient) element;
                jgen.writeFieldName(StringUtils.join(element.getKeywords(), "+") + "+" + element.getUnit().getId());
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
                jgen.writeFieldName(StringUtils.join(element.getKeywords(), "+") + "+" + element.getUnit().getId());
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

    protected String parseReferencesInComment(Group group) {
        CarbonOntology ontology = CarbonOntology.getInstance();
        Pattern p = Pattern.compile("\\\\ref\\{[^}]*\\}");
        Matcher m = p.matcher(group.getComment());
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String refId = m.group().substring(5, m.group().length()-1);
            try {
                Reference ref = ontology.getReference(refId);
                m.appendReplacement(sb, "[" + ref.getShortName() + "]");
            } catch (NotFoundException e) {
                log.warn("Unable to replace a reference in the group " + group.getId() + " comment: " + e.getMessage());
                m.appendReplacement(sb, "[" + refId + "]");
            }
        }
        m.appendTail(sb);
        return sb.toString();
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
