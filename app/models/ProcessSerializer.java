package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.Coefficient;
import com.mycsense.carbondb.domain.DerivedRelation;
import com.mycsense.carbondb.domain.ElementaryFlow;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.Impact;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
                jgen.writeStartObject();
                jgen.writeFieldName("value");
                jgen.writeObject(impact.getValue());
                jgen.writeFieldName("upstream");
                jgen.writeObject(getUpstreamImpacts(process, impact));
                jgen.writeEndObject();
            }
            jgen.writeEndObject();

            jgen.writeFieldName("flows");
            jgen.writeStartObject();
            for (ElementaryFlow flow : process.getCalculatedFlows().values()) {
                jgen.writeFieldName(flow.getType().getId());
                jgen.writeStartObject();
                jgen.writeFieldName("value");
                jgen.writeObject(flow.getValue());
                jgen.writeFieldName("upstream");
                jgen.writeObject(getUpstreamFlows(process, flow));
                jgen.writeEndObject();
            }
            jgen.writeEndObject();

        jgen.writeEndObject();
    }

    protected ArrayList<HashMap<String, Object>> getUpstreamImpacts(Process process, Impact impact) {
        Double totalImpactValue = impact.getValue().value;
        Double totalUpStreamValue = 0.0;
        ArrayList<HashMap<String, Object>> upStreamImpacts = new ArrayList<>();
        for (DerivedRelation relation: process.getUpstreamDerivedRelations()) {
            Process up = relation.getSource();
            if (up.getImpacts().containsKey(impact.getType().getId())) {
                Double upImpact = up.getImpacts().get(impact.getType().getId()).getValue().value;
                HashMap<String, Object> upStreamImpact = constructUpstream(relation, totalImpactValue, upImpact);
                upStreamImpacts.add(upStreamImpact);
                totalUpStreamValue += (Double) upStreamImpact.get("value");
            }
        }
        if (process.getInputFlows().size() > 0 && totalImpactValue - totalUpStreamValue > 0) {
            Double ownValue = totalImpactValue - totalUpStreamValue;
            upStreamImpacts.add(0, constructOwn(ownValue, totalImpactValue));
        }
        return upStreamImpacts;
    }

    protected ArrayList<HashMap<String, Object>> getUpstreamFlows(Process process, ElementaryFlow flow) {
        Double totalFlowValue = flow.getValue().value;
        ArrayList<HashMap<String, Object>> upStreamFlows = new ArrayList<>();
        for (DerivedRelation relation: process.getUpstreamDerivedRelations()) {
            Process up = relation.getSource();
            if (up.getCalculatedFlows().containsKey(flow.getType().getId())) {
                Double upFlow = up.getCalculatedFlows().get(flow.getType().getId()).getValue().value;
                HashMap<String, Object> upStreamFlow = constructUpstream(relation, totalFlowValue, upFlow);
                upStreamFlows.add(upStreamFlow);
            }
        }
        if (process.getInputFlows().size() > 0 && process.getInputFlows().containsKey(flow.getType().getId())) {
            ElementaryFlow inputFlow = process.getInputFlows().get(flow.getType().getId());
            Double ownValue = inputFlow.getValue().value;
            upStreamFlows.add(0, constructOwn(ownValue, totalFlowValue));
        }
        return upStreamFlows;
    }

    protected HashMap<String, Object> constructUpstream(DerivedRelation relation, Double total, Double value) {
        Coefficient coeff = relation.getCoeff();
        Process up = relation.getSource();
        Process process = relation.getDestination();
        Double upConversionFactor = up.getUnit().getConversionFactor();
        Double coeffValue = coeff.getValue().value;
        if (relation.getExponent() == -1) {
            coeffValue = 1 / coeffValue;
        }
        Double coeffConversionFactor = coeff.getUnit().getConversionFactor();
        Double upValue = (value / upConversionFactor) * (coeffValue * coeffConversionFactor);
        upValue *= process.getUnit().getConversionFactor();

        HashMap<String, Object> upStream = new HashMap<>();
        upStream.put("processId", up.getId());
        upStream.put("processKeywords", up.getKeywords().keywords);
        upStream.put("processUnit", up.getUnit());
        upStream.put("coeffId", coeff.getId());
        upStream.put("coeffKeywords", coeff.getKeywords().keywords);
        upStream.put("coeffUnit", coeff.getUnit());
        upStream.put("exponent", relation.getExponent());
        upStream.put("value", upValue);
        upStream.put("contribution", ((double) Math.round((upValue * 10000) / total)) / 100);
        return upStream;
    }

    protected HashMap<String, Object> constructOwn(Double value, Double total) {
        HashMap<String, Object> ownImpact = new HashMap<>();
        ownImpact.put("processId", "#own#");
        ownImpact.put("value", value);
        ownImpact.put("contribution", ((double) Math.round((value * 10000) / total))/100);
        return ownImpact;
    }
}
