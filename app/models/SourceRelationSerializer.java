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
import com.mycsense.carbondb.domain.DerivedRelation;
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

            jgen.writeFieldName("derivedRelations");
            jgen.writeStartArray();
            for (DerivedRelation derivedRelation : relation.getDerivedRelations()) {
                jgen.writeObject(derivedRelation);
            }
            jgen.writeEndArray();
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
