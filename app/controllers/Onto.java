package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.*;
import com.mycsense.carbondb.Reasoner;
import com.mycsense.carbondb.architecture.UnitToolsWebService;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;
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

import com.mongodb.util.JSON;

import static play.libs.Json.toJson;

public class Onto extends Controller {

    protected static String baseOntoFileName = "/tmp/uploaded-ontology.rdf";

    protected static MongoClient mongoClient;

    protected static UnitToolsWebService unitTools;

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
            if (!contentType.equals("application/rdf+xml")) {
                play.Logger.error("Aborting: content type not supported");
                return badRequest("Content type not supported");
            }
            else {
                File file = filePart.getFile();
                File newFile = new File(baseOntoFileName);
                try {
                    FileUtils.copyFile(file, newFile);
                } catch (IOException e) {
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
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        return badRequest(e.getMessage());
                    } finally {
                        play.Logger.info("Clearing the ontology");
                        CarbonOntology.getInstance().clear();
                    }
                    result.put("result", "The ontology has been processed without error");
                    /*if (report.errors.size() > 0) {
                        result.put("result", "The ontology has been processed and contains some errors");
                    }
                    else {
                        result.put("result", "The ontology has been processed without error");
                    }
                    result.put("report", toJson(report));*/
                    play.Logger.info("Processing finished");
                    return ok(result);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    return badRequest(e.toString());
                }
            }
        }
        else {
            return badRequest("File missing");
        }
    }

    protected static Model getInferredModel() {
        Model model = ModelFactory.createDefaultModel( );
        play.Logger.info("Model size after init = " + model.size());

        InputStream in = FileManager.get().open(baseOntoFileName);
        if (in == null) {
            throw new IllegalArgumentException("File " + baseOntoFileName + " not found");
        }

        model.read(in, null);
        play.Logger.info("Model size after reading = " + model.size());

        //Logger.getLogger("").setLevel(Level.WARNING);

        Reasoner reasoner = new Reasoner(model);
        reasoner.run();
        play.Logger.info("Model size after reasoning = " + model.size());
        return reasoner.getInfModel();
    }

    protected static void feedMongoDB(String database) throws Exception {
        ArrayList<String> processGroupsId = new ArrayList<>();
        ArrayList<HashMap<String, String>> nodes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> links = new ArrayList<>();

        ArrayList<String> processesId = new ArrayList<>();
        ArrayList<HashMap<String, String>> derivedNodes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> derivedLinks = new ArrayList<>();

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

            if (group.getType() == Type.PROCESS) {
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

    protected static DB mongoConnect(String database) throws Exception {
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
            return ok(e.getMessage());
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
