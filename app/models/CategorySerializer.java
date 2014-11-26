package models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycsense.carbondb.domain.Category;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.group.Type;

import java.io.IOException;

public class CategorySerializer extends JsonSerializer<Category> {
    @Override
    public void serialize(Category cat, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("id", cat.getId());
        jgen.writeStringField("label", cat.getLabel());
        jgen.writeFieldName("children");
        jgen.writeStartArray();
        for (Object obj : cat.getChildren()) {
            if (obj instanceof Group) {
                Group group = (Group) obj;
                jgen.writeStartObject();
                jgen.writeStringField("id", group.getId());
                jgen.writeStringField("label", group.getLabel());
                if (group.getType() == Type.PROCESS) {
                    jgen.writeStringField("type", "PROCESS");
                }
                else {
                    jgen.writeStringField("type", "COEFFICIENT");
                }
                jgen.writeEndObject();
            }
            else {
                jgen.writeObject(obj);
            }
        }
        jgen.writeEndArray();
        jgen.writeEndObject();
    }
}
