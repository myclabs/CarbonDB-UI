package controllers;

import play.*;
import play.mvc.*;
import java.util.List;
import static play.libs.Json.toJson;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.util.FileManager;
import org.mindswap.pellet.PelletOptions;

import com.mycsense.carbondb.*;
import com.mycsense.carbondb.architecture.*;
import com.mycsense.carbondb.domain.*;

import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Enumeration;

import org.glassfish.jersey.client.ClientConfig;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.util.JSON;

import java.util.Set;

import views.html.*;

public class Onto extends Controller {

    protected static String inferredOntoFileName = "/tmp/uploaded-ontology-processed.rdf";
    protected static String baseOntoFileName = "/tmp/uploaded-ontology.rdf";
    protected static HashMap<String, String> unitsLabel = new HashMap<String, String>();

    // graph containers
    protected static ArrayList<String> nodesLabel = new ArrayList<>();
    protected static ArrayList<String> nodesURI = new ArrayList<>();
    protected static ArrayList<String> nodesId = new ArrayList<>();
    protected static ArrayList<HashMap<String, Object>> links = new ArrayList<>();
    protected static HashMap<String, Double> conversionFactors = new HashMap<>();
    protected static HashMap<String, HashMap<String, Boolean>> compatibleUnits = new HashMap<>();

    protected static MongoClient mongoClient;

    protected static ReasonnerReport report;

    protected static UnitsRepo unitsRepo;

    public class Link {
        public int source, target;
        public Link(int source, int target) { this.source = source; this.target = target; }
    }

    public static Result upload() throws Exception {
        System.out.println("begin processing");
        initUnitsRepo();
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart filePart = body.getFile("file");
        ObjectNode result = Json.newObject();
        if (filePart != null) {
            String fileName = filePart.getFilename();
            String contentType = filePart.getContentType(); 
            File file = filePart.getFile();
            File newFile = new File(baseOntoFileName);
            try {
               FileUtils.copyFile(file, newFile);
            }
            catch (IOException e) {
                result.put("result", e.getMessage());
                return ok(result);
            }
            try {
                PelletOptions.USE_CLASSIFICATION_MONITOR = PelletOptions.MonitorType.NONE;

                // Write the inferred ontology to a file -> sloooow
                /*Model model = ModelFactory.createDefaultModel();
                App.runAll(baseOntoFileName, inferredOntoFileName);*/
                try {
                    Cache.set("conversionFactors", ((UnitsRepoCache)unitsRepo).getConversionFactorsCache());
                    Cache.set("compatibleUnits", ((UnitsRepoCache)unitsRepo).getCompatibleUnitsCache());
                    Model model = getInferredModel();
                    System.out.println("feeding MongoDB");
                    feedMongoDB(model);
                }
                catch (Exception e) {
                    //throw e;
                    result.put("result", e.getMessage());
                    result.put("report", toJson(report));
                    e.printStackTrace(System.out);
                    return ok(result);
                }
                if (report.errors.size() > 0) {
                    result.put("result", "The ontology has been processed and contains some errors");
                }
                else {
                    result.put("result", "The ontology has been processed without error");
                }
                result.put("report", toJson(report));
                System.out.println("processing finished");
                return ok(result);
            }
            catch (Exception e) {
                throw e;
                //result.put("result", e.toString());
                //return ok(result);
            }
        }
        else {
            result.put("result", "File missing");
            return ok(result);
        }



        //File file = request().body().asRaw().asFile();
        //return ok("File uploaded");
    }

    protected static void feedMongoDB(Model model) throws Exception {
        nodesLabel.clear();
        nodesURI.clear();
        nodesId.clear();

        BasicDBObject dbObject;

        DB db = mongoConnect();

        DBCollection reportColl = db.getCollection("report");
        reportColl.drop();

        dbObject = (BasicDBObject) JSON.parse(toJson(report).toString());
        reportColl.insert(dbObject);

        DBCollection categoriesColl = db.getCollection("categories");
        categoriesColl.drop();

        dbObject = (BasicDBObject) JSON.parse(toJson(RepoFactory.getCategoryRepo().getCategoriesTree()).toString());
        categoriesColl.insert(dbObject);

        DBCollection groupsColl = db.getCollection("groups");
        groupsColl.drop();

        for (Group group: RepoFactory.getGroupRepo().getGroups()) {
            dbObject = (BasicDBObject) JSON.parse(getGroupAsJSON(group, model));
            dbObject.append("_id", group.getURI());
            groupsColl.insert(dbObject);
        }

        DBCollection graphColl = db.getCollection("graph");
        graphColl.drop();

        /*dbObject = (BasicDBObject) JSON.parse(toJson(nodesLabel).toString());
        dbObject.append("_id", "nodes");
        graphColl.insert(dbObject);*/

        ArrayList<Resource> macroRelationResources = RepoFactory.getRelationRepo().getMacroRelationsResources();
        for (Resource macroRelationResource: macroRelationResources) {
            String sourceURI = macroRelationResource.getProperty(Datatype.hasOriginProcess).getResource().getURI();
            String targetURI = macroRelationResource.getProperty(Datatype.hasDestinationProcess).getResource().getURI();
            HashMap<String, Object> link = new HashMap<>();
            link.put("uri", macroRelationResource.getURI());
            link.put("source", nodesURI.indexOf(sourceURI));
            link.put("target", nodesURI.indexOf(targetURI));
            links.add(link);
        }

        dbObject = (BasicDBObject) JSON.parse("{nodes:" + toJson(nodesLabel).toString()
                                            + ",nodesId:" + toJson(nodesId).toString()
                                            + ",links:" + toJson(links).toString() + "}");
        graphColl.insert(dbObject);

        mongoClose();
    }

    protected static String getGroupAsJSON(Group group, Model model) throws Exception {
        HashMap<String, Object> output = new HashMap<String, Object>();

        boolean isProcessGroup = false;
        if (model.contains(ResourceFactory.createResource(group.getURI()), RDF.type, Datatype.ProcessGroup)) {
            isProcessGroup = true;
        }
        HashMap<String, Value> elementsValue = new HashMap<String, Value>();

        String unit = group.getUnit();
        String unitLabel;
        if (!unitsLabel.containsKey(unit)) {
            Response response = ClientBuilder.newClient()
                            .target("http://units.myc-sense.com/api")
                            .path("unit")
                            .path(unit)
                            .request(MediaType.TEXT_PLAIN_TYPE)
                            .get();
            if (response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                JSONObject obj = new JSONObject(responseString);
                if (obj.getJSONObject("symbol").isNull("en")) {
                    unitLabel = "-";
                }
                else {
                    unitLabel = obj.getJSONObject("symbol").getString("en");
                }
            }
            else {
                unitLabel = unit;
            }
            unitsLabel.put(unit, unitLabel);
        }

        unitLabel = unitsLabel.get(unit);
        if (isProcessGroup) {
            unitLabel = "kg équ. CO2 / " + unitLabel;
        }

        for (Dimension element: group.elements.dimensions) {
            Resource elementResource;
            try {
                if (isProcessGroup) {
                    elementResource = RepoFactory.getSingleElementRepo().getProcessForDimension(element, group.getUnitURI());
                }
                else {
                    elementResource = RepoFactory.getSingleElementRepo().getCoefficientForDimension(element, group.getUnitURI());
                }
            }
            catch (NoElementFoundException e) {
                elementResource = null;
            }
            catch (MultipleElementsFoundException e) {
                elementResource = null;
            }
            if (null == elementResource) {
                elementsValue.put(joinDimensionKeywords(element), new Value(0.0, 0.0));
            }
            else if (isProcessGroup) {
                HashMap<Resource, Value> emissions = RepoFactory.getSingleElementRepo().getCalculatedEmissionsForProcess(elementResource);
                elementsValue.put(joinDimensionKeywords(element), calculateProcessImpact(emissions));
            }
            else {
                if (elementResource.hasProperty(Datatype.value) && null != elementResource.getProperty(Datatype.value)) {
                    elementsValue.put(
                        joinDimensionKeywords(element),
                        new Value(
                            elementResource.getProperty(Datatype.value).getDouble(),
                            RepoFactory.getSingleElementRepo().getUncertainty(elementResource)
                        )
                    );
                }
                else {
                    elementsValue.put(
                        joinDimensionKeywords(element),
                        new Value(
                            0.0,
                            RepoFactory.getSingleElementRepo().getUncertainty(elementResource)
                        )
                    );
                }
            }
        }
        output.put("URI", group.getURI());
        output.put("label", group.getLabel());
        output.put("elements", elementsValue);
        output.put("elementsNumber", elementsValue.size());
        output.put("dimensions", group.dimSet.dimensions);
        output.put("unit", unitLabel);
        output.put("commonKeywords", group.commonKeywords.keywords);
        output.put("macroRelations", RepoFactory.getRelationRepo().getMacroRelationsForProcessGroup(ResourceFactory.createResource(group.getURI())));
        output.put("type", group.type);

        if (isProcessGroup) {
            nodesLabel.add(group.getLabel());
            nodesURI.add(group.getURI());
            nodesId.add(group.getId());
        }

        return toJson(output).toString();
    }

    protected static DB mongoConnect() throws Exception {
        mongoClient = new MongoClient( "localhost" , 27017 );
        DB db = mongoClient.getDB( "carbondb" );
        return db;
    }

    protected static void mongoClose() {
        mongoClient.close();
    }

    public static Result getCategories() {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect();
            DBCollection categoriesColl = db.getCollection("categories");
            String response = categoriesColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getGraph() {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect();
            DBCollection graphColl = db.getCollection("graph");
            String response = graphColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getLastReport() {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect();
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

    protected static void initUnitsRepo() {
        if (null == unitsRepo) {
            unitsRepo = new UnitsRepoWebService();
            if (null != Cache.get("conversionFactors")) {
                ((UnitsRepoCache)unitsRepo).setConversionFactorsCache((HashMap)Cache.get("conversionFactors"));                
            }
            if (null != Cache.get("compatibleUnits")) {
                ((UnitsRepoCache)unitsRepo).setCompatibleUnitsCache((HashMap)Cache.get("compatibleUnits"));
            }
        }
    }

    protected static Model getInferredModel() {
        Model model = ModelFactory.createDefaultModel( );

        InputStream in = FileManager.get().open(baseOntoFileName);
        if (in == null) {
            throw new IllegalArgumentException("File not found");
        }

        model.read( in, null );

        Logger.getLogger("").setLevel(Level.WARNING);

        Reasoner reasoner = new Reasoner(model, unitsRepo);
        reasoner.run();
        report = reasoner.report;
        return reasoner.getInfModel();
    }

    public static Result getGroup(String groupId) {
        System.out.println("group access");
        try {
            DB db = mongoConnect();
            DBCollection groupsColl = db.getCollection("groups");
            BasicDBObject query = new BasicDBObject("_id", Datatype.getURI() + groupId);
            String response = groupsColl.findOne(query).toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static void printRelationsForElement(Resource element, Model model, Resource singleType) {
        Selector selector;
        if (singleType == Datatype.SingleProcess) {
            selector = new SimpleSelector(null, Datatype.hasDestinationProcess, element);
        }
        else {
            selector = new SimpleSelector(null, Datatype.hasWeightCoefficient, element);
        }
        StmtIterator iter = model.listStatements( selector );

        ArrayList<Resource> relations = new ArrayList<Resource>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                relations.add(s.getSubject());
            }
        }
        System.out.println("relations for element: " + element.getURI());
        for (Resource relation: relations) {
            System.out.println(relation.getProperty(Datatype.hasOriginProcess).getResource().getURI()
                               + " * "
                               + relation.getProperty(Datatype.hasWeightCoefficient).getResource().getURI()
                               + " (" + RepoFactory.getRelationRepo().getCoefficientValueForRelation(relation) + ")"
                               + " -> "
                               + relation.getProperty(Datatype.hasDestinationProcess).getResource().getURI()
                               + " (" + relation.getURI() + ")");
        }
    }

    public static Value calculateProcessImpact(HashMap<Resource, Value> emissions)
    {
        Value value = new Value(0.0, 0.0);
        for (Resource key: emissions.keySet()) {
            if (key.getURI().equals(Datatype.getURI() + "ghg/ch4")) {
                value.add(new Value(emissions.get(key).value * 34, emissions.get(key).uncertainty));
            }
            else if (key.getURI().equals(Datatype.getURI() + "ghg/n2o")) {
                value.add(new Value(emissions.get(key).value * 298, emissions.get(key).uncertainty));
            }
            else if (key.getURI().equals(Datatype.getURI() + "ghg/co2")) {
                value.add(emissions.get(key));
            }
            else {
                value.add(emissions.get(key));
            }
        }
        return value;
    }

    public static Result getProcessGroups() {
        Model model = getInferredModel();

        authorizeCrossRequests();
    	return ok(toJson(RepoFactory.getGroupRepo().getProcessGroups()));
    }

    public static Result getCoefficientGroups() {
        Model model = getInferredModel();

        authorizeCrossRequests();
        return ok(toJson(RepoFactory.getGroupRepo().getCoefficientGroups()));
    }

    /**
     * Create a string containing all the keywords URI alphabetically sorted
     */
    protected static String joinDimensionKeywords(Dimension dimension) {
        String output = new String();
        String[] keywords = new String[dimension.keywords.size()];
        int i = 0;
        for (Keyword keyword: dimension.keywords) {
            keywords[i] = keyword.getName();
            i++;
        }
        Arrays.sort(keywords);
        for (i = 0; i < keywords.length; i++) {
            output += keywords[i].replace(".", "____");
        }
        return output;
    }
}
