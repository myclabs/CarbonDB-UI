package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mycsense.carbondb.Reasoner;
import com.mycsense.carbondb.architecture.UnitToolsWebService;
import com.mycsense.carbondb.domain.CarbonOntology;
import com.mycsense.carbondb.domain.Category;
import com.mycsense.carbondb.domain.Coefficient;
import com.mycsense.carbondb.domain.DerivedRelation;
import com.mycsense.carbondb.domain.ElementaryFlowType;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.ImpactType;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.Reference;
import com.mycsense.carbondb.domain.RelationType;
import com.mycsense.carbondb.domain.SourceRelation;
import com.mycsense.carbondb.domain.Unit;
import com.mycsense.carbondb.domain.group.Type;
import models.CategorySerializer;
import models.CoefficientSerializer;
import models.DerivedRelationSerializer;
import models.GroupSerializer;
import models.ProcessSerializer;
import models.ReferenceSerializer;
import models.SourceRelationSerializer;
import models.TypeSerializer;
import play.*;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import org.mindswap.pellet.PelletOptions;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import static play.libs.Json.toJson;

public class Onto extends Controller {

    protected static String baseOntoFileName = "/tmp/uploaded-ontology.rdf";

    protected static MongoClient mongoClient;

    protected static UnitToolsWebService unitTools;

    public class Link {
        public int source, target;
        public Link(int source, int target) { this.source = source; this.target = target; }
    }

    public static Result upload(String database) throws Exception {
        play.Logger.info("----------------");
        play.Logger.info("Begin processing");
        initUnitTools();
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart filePart = body.getFile("file");
        ObjectNode result = Json.newObject();
        if (!database.equals("latest") && !database.equals("wip")) {
            return badRequest("Invalid slot name");
        }
        else if (filePart != null) {
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

                try {
                    Cache.set("conversionFactors", unitTools.getConversionFactorsCache());
                    Cache.set("compatibleUnits", unitTools.getCompatibleUnitsCache());
                    Cache.set("unitSymbols", unitTools.getSymbolsCache());
                    Model model = getInferredModel();
                    play.Logger.info("Feeding MongoDB");
                    feedMongoDB(database);
                }
                catch (Exception e) {
                    //throw e;
                    result.put("result", e.getMessage());
                    //result.put("report", toJson(report));
                    e.printStackTrace(System.out);
                    return ok(result);
                }
                finally {
                    play.Logger.info("Clearing the ontology");
                    CarbonOntology.getInstance().clear();
                }
                result.put("result", "The ontology has been processed without error");
                /*if (report.errors.size() > 0) {
                    result.put("result", "The ontology has been processed and contains some errors");
                }
                else {
                    result.put("result", "The ontology has been processed without error");
                }*/
                //result.put("report", toJson(report));
                play.Logger.info("Processing finished");
                return ok(result);
            }
            catch (Exception e) {
                e.printStackTrace(System.out);
                return badRequest(e.toString());
            }
        }
        else {
            return badRequest("File missing");
        }



        //File file = request().body().asRaw().asFile();
        //return ok("File uploaded");
    }

    protected static Model getInferredModel() {
        Model model = ModelFactory.createDefaultModel( );
        play.Logger.info("Model size after init = " + model.size());

        InputStream in = FileManager.get().open(baseOntoFileName);
        if (in == null) {
            throw new IllegalArgumentException("File not found");
        }

        model.read( in, null );
        play.Logger.info("Model size after reading = " + model.size());

        //Logger.getLogger("").setLevel(Level.WARNING);

        Reasoner reasoner = new Reasoner(model);
        reasoner.run();
        play.Logger.info("Model size after reasoning = " + model.size());
        return reasoner.getInfModel();
    }

    protected static void feedMongoDB(String database) throws Exception {
        ArrayList<String> nodesLabel = new ArrayList<>();
        ArrayList<String> nodesId = new ArrayList<>();
        ArrayList<HashMap<String, Object>> links = new ArrayList<>();

        BasicDBObject dbObject;

        DB db = mongoConnect(database);

        CarbonOntology ontology = CarbonOntology.getInstance();
        DBCollection categoriesColl = db.getCollection("categories");
        categoriesColl.drop();

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Category.class, new CategorySerializer());
        module.addSerializer(ImpactType.class, new TypeSerializer());
        module.addSerializer(ElementaryFlowType.class, new TypeSerializer());
        module.addSerializer(Group.class, new GroupSerializer());
        module.addSerializer(SourceRelation.class, new SourceRelationSerializer());
        module.addSerializer(DerivedRelation.class, new DerivedRelationSerializer());
        module.addSerializer(Process.class, new ProcessSerializer());
        module.addSerializer(Coefficient.class, new CoefficientSerializer());
        module.addSerializer(Reference.class, new ReferenceSerializer());
        mapper.registerModule(module);

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

        DBCollection impactAndFlowTypesTreeColl = db.getCollection("impactAndFlowTypesTree");
        impactAndFlowTypesTreeColl.drop();
        String impactTypesTreeSerialized = mapper.writeValueAsString(ontology.getImpactTypesTree());
        String elementaryFlowTypesTreeSerialized = mapper.writeValueAsString(ontology.getElementaryFlowTypesTree());
        dbObject = (BasicDBObject) JSON.parse("{impactTypesTree:" + impactTypesTreeSerialized
                + ", flowTypesTree:" + elementaryFlowTypesTreeSerialized + "}");
        impactAndFlowTypesTreeColl.insert(dbObject);


        DBCollection groupsColl = db.getCollection("groups");
        groupsColl.drop();

        for (Group group: ontology.getGroups().values()) {
            String serializedGroup = mapper.writeValueAsString(group);
            dbObject = (BasicDBObject) JSON.parse(serializedGroup);
            dbObject.append("_id", group.getId());
            groupsColl.insert(dbObject);

            if (group.getType() == Type.PROCESS) {
                nodesId.add(group.getId());
                nodesLabel.add(group.getLabel());
            }
        }

        DBCollection processesColl = db.getCollection("processes");
        processesColl.drop();

        for (Process process : ontology.getProcesses()) {
            String serializedProcess = mapper.writeValueAsString(process);
            dbObject = (BasicDBObject) JSON.parse(serializedProcess);
            dbObject.append("_id", process.getId());
            processesColl.insert(dbObject);
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

        String serializedReferences = "{references:" + mapper.writeValueAsString(ontology.getReferences()) + "}";
        dbObject = (BasicDBObject) JSON.parse(serializedReferences);
        refColl.insert(dbObject);

        DBCollection graphColl = db.getCollection("graph");
        graphColl.drop();


        for (SourceRelation sourceRelation : ontology.getSourceRelations().values()) {
            String sourceId = sourceRelation.getSource().getId();
            String destinationId = sourceRelation.getDestination().getId();
            HashMap<String, Object> link = new HashMap<>();
            link.put("id", sourceRelation.getId());
            link.put("source", nodesId.indexOf(sourceId));
            link.put("target", nodesId.indexOf(destinationId));
            if (sourceRelation.getType() != null)
                link.put("type", sourceRelation.getType().getId());
            else
                link.put("type", "#none");
            links.add(link);
        }
        HashMap<String, RelationType> relationTypes = new HashMap<>();
        for (RelationType type: ontology.getRelationTypes()) {
            relationTypes.put(type.getId(), type);
        }

        dbObject = (BasicDBObject) JSON.parse("{nodes:" + toJson(nodesLabel).toString()
                + ",nodesId:" + toJson(nodesId).toString()
                + ",links:" + toJson(links).toString()
                + ",types:" + toJson(relationTypes).toString() + "}");
        graphColl.insert(dbObject);

        mongoClose();
        /*DBCollection reportColl = db.getCollection("report");
        reportColl.drop();

        dbObject = (BasicDBObject) JSON.parse(toJson(report).toString());
        reportColl.insert(dbObject);*/
    }

    protected static DB mongoConnect(String database) throws Exception {
        mongoClient = new MongoClient( "localhost" , 27017 );
        DB db = mongoClient.getDB( database );
        return db;
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

    public static Result getImpactAndFlowTypes(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection impactAndFlowTypesTreeColl = db.getCollection("impactAndFlowTypesTree");
            String responseTree = impactAndFlowTypesTreeColl.findOne().toString();
            DBCollection impactAndFlowTypesColl = db.getCollection("impactAndFlowTypes");
            String responsePlain = impactAndFlowTypesColl.findOne().toString();
            mongoClose();
            return ok("{\"tree\": " + responseTree + ", \"plain\": " + responsePlain + "}");
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
            String response = groupsColl.findOne(query).toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getProcess(String database, String processURI) {
        try {
            DB db = mongoConnect(database);
            DBCollection processesColl = db.getCollection("processes");
            BasicDBObject query = new BasicDBObject("_id", processURI);
            String response = processesColl.findOne(query).toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    public static Result getCoefficient(String database, String coeffURI) {
        try {
            DB db = mongoConnect(database);
            DBCollection coefficientsColl = db.getCollection("coefficients");
            BasicDBObject query = new BasicDBObject("_id", coeffURI);
            String response = coefficientsColl.findOne(query).toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }
}
