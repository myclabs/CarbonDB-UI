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

package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.*;
import com.mycsense.carbondb.Reasoner;
import com.mycsense.carbondb.architecture.UnitToolsWebService;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.group.Type;
import log.OntoProcessorMessageStore;
import models.CategorySerializer;
import models.CoefficientSerializer;
import models.DerivedRelationSerializer;
import models.GroupSerializer;
import models.OntoProcessor;
import models.ProcessSerializer;
import models.ReferenceSerializer;
import models.SourceRelationSerializer;
import models.TypeSerializer;
import org.slf4j.Logger;
import play.*;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.cache.Cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import org.mindswap.pellet.PelletOptions;

import com.mongodb.util.JSON;

import org.slf4j.LoggerFactory;

import static play.libs.Json.toJson;

public class Onto extends Controller {

    protected static String baseOntoFileName = "/tmp/uploaded-ontology.rdf";

    protected static MongoClient mongoClient;

    protected static InputStream inputStream;

    protected static UnitToolsWebService unitTools;

    public static Result upload(String database) {
        play.Logger.info("----------------");
        play.Logger.info("Begin processing (slot: " + database + ")");
        initUnitTools();
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart filePart = body.getFile("file");
        ObjectNode result = Json.newObject();
        if (!database.equals("latest") && !database.equals("wip")) {
            return badRequest("Invalid slot name");
        }
        else if (filePart != null) {
            String contentType = filePart.getContentType();
            if (!contentType.equals("application/rdf+xml")) {
                play.Logger.error("Aborting: content type not supported");
                return badRequest("Content type not supported");
            }
            else {
                File file = filePart.getFile();
                Cache.set("conversionFactors", unitTools.getConversionFactorsCache());
                Cache.set("compatibleUnits", unitTools.getCompatibleUnitsCache());
                Cache.set("unitSymbols", unitTools.getSymbolsCache());
                // we suppress the Pellet reasoner progression outputs
                PelletOptions.USE_CLASSIFICATION_MONITOR = PelletOptions.MonitorType.NONE;
                try {
                    inputStream = new FileInputStream(file);
                    DB db = mongoConnect(database);
                    OntoProcessor processor = new OntoProcessor(inputStream, db);
                    processor.processAndSave();
                } catch (FileNotFoundException
                        | UnknownHostException
                        | JsonProcessingException e) {
                    play.Logger.error("Error while processing the ontology", e);
                    return badRequest(e.getMessage());
                } finally {
                    play.Logger.info("Clearing the ontology");
                    CarbonOntology.getInstance().clear();
                }
                if (OntoProcessorMessageStore.getInstance().hasErrors()) {
                    result.put("result", "The ontology has been processed and contains some errors");
                }
                else if (OntoProcessorMessageStore.getInstance().hasWarnings()) {
                    result.put("result", "The ontology has been processed and contains some warnings");
                }
                else {
                    result.put("result", "The ontology has been processed without error and warning");
                }
                result.put("report", toJson(OntoProcessorMessageStore.getInstance()));

                play.Logger.info("Processing finished (slot: " + database + ")");
                return ok(result);
            }
        }
        else {
            return badRequest("File missing");
        }
    }

    /**
     * Open connection to MongoDB
     * @param database database name
     * @throws UnknownHostException
     */
    protected static DB mongoConnect(String database) throws UnknownHostException {
        mongoClient = new MongoClient( "localhost" , 27017 );
        return mongoClient.getDB( database );
    }

    protected static void mongoClose() {
        mongoClient.close();
    }

    public static Result getCategories(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection categoriesColl = db.getCollection("categories");
            String response = categoriesColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getReferences(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection categoriesColl = db.getCollection("references");
            String response = categoriesColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getGraph(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection graphColl = db.getCollection("graph");
            String response = graphColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getDerivedGraph(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection graphColl = db.getCollection("derivedGraph");
            String response = graphColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getOntologyTypes(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection impactAndFlowTypesTreeColl = db.getCollection("impactAndFlowTypesTree");
            String responseTree = impactAndFlowTypesTreeColl.findOne().toString();
            DBCollection impactAndFlowTypesColl = db.getCollection("impactAndFlowTypes");
            String responsePlain = impactAndFlowTypesColl.findOne().toString();
            DBCollection relationTypesColl = db.getCollection("relationTypes");
            String response = relationTypesColl.findOne().toString();

            mongoClose();
            return ok("{\"tree\": " + responseTree + ", \"plain\": " + responsePlain + ", \"relationTypes\": " + response + "}");
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getOntologyStats(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection statsColl = db.getCollection("ontologyStats");
            String response = statsColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getLastReport(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection reportColl = db.getCollection("report");
            String response = reportColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.toString());
        }
    }

    protected static void authorizeCrossRequests() {
        // Change the response to accept cross-domain requests
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "POST");
        response().setHeader("Access-Control-Allow-Headers", "accept, origin, Content-type, x-json, x-prototype-version, x-requested-with");
    }

    protected static void initUnitTools() {
        if (null == unitTools) {
            unitTools = new UnitToolsWebService();
            Unit.setUnitTools(unitTools);
            String unitAPIURL = Play.application().configuration().getString("unitAPI.url");
            unitTools.setUnitsAPIURI(unitAPIURL);
            if (null != Cache.get("conversionFactors")) {
                unitTools.setConversionFactorsCache((HashMap)Cache.get("conversionFactors"));
            }
            if (null != Cache.get("unitSymbols")) {
                unitTools.setSymbolsCache((HashMap)Cache.get("unitSymbols"));
            }
            if (null != Cache.get("compatibleUnits")) {
                unitTools.setCompatibleUnitsCache((HashMap)Cache.get("compatibleUnits"));
            }
        }
    }

    public static Result getGroup(String database, String groupId) {
        try {
            DB db = mongoConnect(database);
            DBCollection groupsColl = db.getCollection("groups");
            BasicDBObject query = new BasicDBObject("_id", groupId);
            DBObject result = groupsColl.findOne(query);
            if (null == result) {
                mongoClose();
                return notFound("The group " + groupId + " could not be found");
            }
            else {
                mongoClose();
                return ok(result.toString());
            }
        }
        catch (Exception e) {
            mongoClose();
            return ok(e.getMessage());
        }
    }

    public static Result getProcess(String database, String processId) {
        try {
            DB db = mongoConnect(database);
            DBCollection processesColl = db.getCollection("processes");
            BasicDBObject query = new BasicDBObject("_id", processId);
            String response = processesColl.findOne(query).toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok("error");
        }
    }

    public static Result getCoefficient(String database, String coeffId) {
        try {
            DB db = mongoConnect(database);
            DBCollection coefficientsColl = db.getCollection("coefficients");
            BasicDBObject query = new BasicDBObject("_id", coeffId);
            String response = coefficientsColl.findOne(query).toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }
}
