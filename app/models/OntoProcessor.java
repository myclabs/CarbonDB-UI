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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.mycsense.carbondb.Reasoner;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.group.*;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import static play.libs.Json.toJson;

public class OntoProcessor {

    public OntoProcessor(InputStream inputStream, String databaseName) {
        this.inputStream = inputStream;
        this.databaseName = databaseName;
        initializeMapper();
    }

    /**
     * MongoDB connection
     */
    private MongoClient mongoClient;

    /**
     * Database
     */
    private DB db;

    /**
     * The ontology as provided after upload
     */
    private InputStream inputStream;

    /**
     * Jackson serialization mapper
     */
    protected ObjectMapper mapper;

    /**
     * Name of the Mongo database where to store the ontology cache
     */
    private String databaseName;

    /**
     * Initialize the Jackson Mapper with personalize serializer
     * for every class in the ontology domain model.
     */
    protected void initializeMapper() {
        mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Category.class, new CategorySerializer());
        module.addSerializer(ImpactType.class, new TypeSerializer());
        module.addSerializer(ElementaryFlowType.class, new TypeSerializer());
        module.addSerializer(Group.class, new GroupSerializer());
        module.addSerializer(SourceRelation.class, new SourceRelationSerializer());
        module.addSerializer(DerivedRelation.class, new DerivedRelationSerializer());
        module.addSerializer(com.mycsense.carbondb.domain.Process.class, new ProcessSerializer());
        module.addSerializer(Coefficient.class, new CoefficientSerializer());
        module.addSerializer(Reference.class, new ReferenceSerializer());
        mapper.registerModule(module);
    }

    public void processOntology() throws UnknownHostException, Exception {
        mongoConnect(databaseName);
        Model model = ModelFactory.createDefaultModel();
        play.Logger.info("Model size after init = " + model.size());

        model.read(inputStream, null);
        play.Logger.info("Model size after reading = " + model.size());

        //Logger.getLogger("").setLevel(Level.WARNING);

        Reasoner reasoner = new Reasoner(model);
        reasoner.run();
        play.Logger.info("Model size after reasoning = " + model.size());

        play.Logger.info("Feeding MongoDB");
        feedMongoDB();
    }

    protected void feedMongoDB() throws Exception {
        ArrayList<String> processGroupsId = new ArrayList<>();
        ArrayList<HashMap<String, String>> nodes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> links = new ArrayList<>();

        ArrayList<String> processesId = new ArrayList<>();
        ArrayList<HashMap<String, String>> derivedNodes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> derivedLinks = new ArrayList<>();

        BasicDBObject dbObject;

        CarbonOntology ontology = CarbonOntology.getInstance();
        DBCollection categoriesColl = db.getCollection("categories");
        categoriesColl.drop();

        String serialized = mapper.writeValueAsString(ontology.getCategoryTree());
        dbObject = (BasicDBObject) JSON.parse(serialized);

        categoriesColl.insert(dbObject);


        DBCollection impactAndFlowTypesColl = db.getCollection("impactAndFlowTypes");
        impactAndFlowTypesColl.drop();

        String impactTypesSerialized = mapper.writeValueAsString(ontology.getImpactTypes());
        String elementaryFlowTypesSerialized = mapper.writeValueAsString(ontology.getElementaryFlowTypes());

        dbObject = (BasicDBObject) JSON.parse("{impactTypes:" + impactTypesSerialized
                + ", flowTypes:" + elementaryFlowTypesSerialized + "}");
        impactAndFlowTypesColl.insert(dbObject);

        DBCollection impactAndFlowTypesTreeColl = db.getCollection("ontologyTypes");
        impactAndFlowTypesTreeColl.drop();
        String impactTypesTreeSerialized = mapper.writeValueAsString(ontology.getImpactTypesTree());
        String elementaryFlowTypesTreeSerialized = mapper.writeValueAsString(ontology.getElementaryFlowTypesTree());

        dbObject = (BasicDBObject) JSON.parse("{impactTypesTree:" + impactTypesTreeSerialized
                + ", flowTypesTree:" + elementaryFlowTypesTreeSerialized + "}");
        impactAndFlowTypesTreeColl.insert(dbObject);

        DBCollection relationTypesColl = db.getCollection("relationTypes");
        relationTypesColl.drop();
        String relationTypesTreeSerialized = mapper.writeValueAsString(ontology.getRelationTypes());
        dbObject = (BasicDBObject) JSON.parse("{relationTypes: " + relationTypesTreeSerialized + "}");
        relationTypesColl.insert(dbObject);


        DBCollection groupsColl = db.getCollection("groups");
        groupsColl.drop();

        for (Group group: ontology.getGroups().values()) {
            String serializedGroup = mapper.writeValueAsString(group);
            dbObject = (BasicDBObject) JSON.parse(serializedGroup);
            dbObject.append("_id", group.getId());
            groupsColl.insert(dbObject);

            if (group.getType() == com.mycsense.carbondb.domain.group.Type.PROCESS) {
                HashMap<String, String> node = new HashMap<>();
                node.put("id", group.getId());
                node.put("label", group.getLabel());
                nodes.add(node);
                processGroupsId.add(group.getId());
            }
        }

        DBCollection processesColl = db.getCollection("processes");
        processesColl.drop();

        for (Process process : ontology.getProcesses()) {
            String serializedProcess = mapper.writeValueAsString(process);
            dbObject = (BasicDBObject) JSON.parse(serializedProcess);
            dbObject.append("_id", process.getId());
            processesColl.insert(dbObject);

            processesId.add(process.getId());
            HashMap<String, String> node = new HashMap<>();
            node.put("id", process.getId());
            String label = "";
            for (Keyword keyword : process.getKeywords()) {
                label += keyword.getLabel() + " - ";
            }
            label = label.substring(0, label.length() - 3);
            label += " [" + process.getUnit().getSymbol() + "]";
            node.put("label", label);
            derivedNodes.add(node);
        }

        DBCollection coefficientsColl = db.getCollection("coefficients");
        coefficientsColl.drop();

        for (Coefficient coeff : ontology.getCoefficients()) {
            String serializedCoeff = mapper.writeValueAsString(coeff);
            dbObject = (BasicDBObject) JSON.parse(serializedCoeff);
            dbObject.append("_id", coeff.getId());
            coefficientsColl.insert(dbObject);
        }

        DBCollection refColl = db.getCollection("references");
        refColl.drop();

        String serializedReferences = "{references:" + mapper.writeValueAsString(ontology.getReferences().values()) + "}";
        dbObject = (BasicDBObject) JSON.parse(serializedReferences);
        refColl.insert(dbObject);

        DBCollection graphColl = db.getCollection("graph");
        graphColl.drop();

        for (SourceRelation sourceRelation : ontology.getSourceRelations().values()) {
            String sourceId = sourceRelation.getSource().getId();
            String destinationId = sourceRelation.getDestination().getId();
            HashMap<String, Object> link = new HashMap<>();
            link.put("id", sourceRelation.getId());
            link.put("source", processGroupsId.indexOf(sourceId));
            link.put("target", processGroupsId.indexOf(destinationId));
            if (sourceRelation.getType() != null)
                link.put("type", sourceRelation.getType().getId());
            else
                link.put("type", "#none");
            links.add(link);
        }

        dbObject = (BasicDBObject) JSON.parse("{nodes:" + toJson(nodes).toString()
                + ",links:" + toJson(links).toString() + "}");
        graphColl.insert(dbObject);

        DBCollection derivedGraphColl = db.getCollection("derivedGraph");
        derivedGraphColl.drop();

        for (DerivedRelation relation : ontology.getDerivedRelations()) {
            String sourceId = relation.getSource().getId();
            String destinationId = relation.getDestination().getId();
            HashMap<String, Object> link = new HashMap<>();
            link.put("source", processesId.indexOf(sourceId));
            link.put("target", processesId.indexOf(destinationId));
            if (relation.getType() != null)
                link.put("type", relation.getType().getId());
            else
                link.put("type", "#none");
            derivedLinks.add(link);
        }

        dbObject = (BasicDBObject) JSON.parse("{nodes:" + toJson(derivedNodes).toString()
                + ",links:" + toJson(derivedLinks).toString() + "}");
        derivedGraphColl.insert(dbObject);

        DBCollection statsColl = db.getCollection("ontologyStats");
        statsColl.drop();

        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("coefficientGroups", ontology.getCoefficientGroups().size());
        stats.put("processGroups", ontology.getProcessGroups().size());
        stats.put("coefficients", ontology.getCoefficients().size());
        stats.put("processes", ontology.getProcesses().size());
        Integer numberOfInputElementaryFlow = 0;
        Integer numberOfCalculatedElementaryFlow = 0;
        Integer numberOfImpact = 0;
        for (Process process : ontology.getProcesses()) {
            numberOfInputElementaryFlow += process.getInputFlows().size();
            numberOfCalculatedElementaryFlow += process.getCalculatedFlows().size();
            numberOfImpact += process.getImpacts().size();
        }
        stats.put("inputFlows", numberOfInputElementaryFlow);
        stats.put("calculatedFlows", numberOfCalculatedElementaryFlow);
        stats.put("impacts", numberOfImpact);
        stats.put("sourceRelations", ontology.getSourceRelations().size());
        stats.put("derivedRelations", ontology.getDerivedRelations().size());
        stats.put("references", ontology.getReferences().size());

        dbObject = (BasicDBObject) JSON.parse(mapper.writeValueAsString(stats));
        statsColl.insert(dbObject);

        mongoClose();
        /*DBCollection reportColl = db.getCollection("report");
        reportColl.drop();

        dbObject = (BasicDBObject) JSON.parse(toJson(report).toString());
        reportColl.insert(dbObject);*/
    }

    /**
     * Open connection to MongoDB
     * @param database database name
     * @throws UnknownHostException
     */
    protected void mongoConnect(String database) throws UnknownHostException {
        mongoClient = new MongoClient( "localhost" , 27017 );
        db = mongoClient.getDB( database );
    }

    protected void mongoClose() {
        mongoClient.close();
    }
}
