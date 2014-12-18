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
