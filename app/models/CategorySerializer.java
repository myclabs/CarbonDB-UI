/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-UI project <http://www.carbondb.org>
 *
 * CarbonDB-UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-UI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

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
