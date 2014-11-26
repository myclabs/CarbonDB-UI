package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import models.CategorySerializer;
import models.GroupSerializer;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import org.mindswap.pellet.PelletOptions;

import com.mycsense.carbondb.*;
import com.mycsense.carbondb.architecture.*;
import com.mycsense.carbondb.domain.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class Onto extends Controller {

    protected static String baseOntoFileName = "/tmp/uploaded-ontology.rdf";
    protected static HashMap<String, String> unitsLabel = new HashMap<String, String>();

    // graph containers
    protected static ArrayList<String> nodesLabel = new ArrayList<>();
    protected static ArrayList<String> nodesURI = new ArrayList<>();
    protected static ArrayList<String> nodesId = new ArrayList<>();
    protected static ArrayList<HashMap<String, Object>> links = new ArrayList<>();
    protected static HashMap<String, HashMap<String, Object>> processes = new HashMap<>();
    protected static HashMap<String, HashMap<String, Object>> coefficients = new HashMap<>();
    protected static HashMap<String, Reference> references = new HashMap<>();
    protected static HashMap<String, ArrayList<HashMap<String, String>>> referencesGroups = new HashMap<>();
    protected static HashMap<String, HashMap<String, String>> groupOverlap = new HashMap<>();

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
        HashMap<String, HashMap<String, Object>> groups = new HashMap<>();

        for (Group group: ontology.getGroups().values()) {
            String serializedGroup = mapper.writeValueAsString(group);
            dbObject = (BasicDBObject) JSON.parse(serializedGroup);
            dbObject.append("_id", group.getId());
            groupsColl.insert(dbObject);
        }

        /*nodesLabel.clear();
        nodesURI.clear();
        nodesId.clear();
        links.clear();
        processes.clear();
        coefficients.clear();
        references.clear();
        referencesGroups.clear();
        groupOverlap.clear();

        BasicDBObject dbObject;

        DB db = mongoConnect(database);

        DBCollection categoriesColl = db.getCollection("categories");
        categoriesColl.drop();

        dbObject = (BasicDBObject) JSON.parse(toJson(RepoFactory.getCategoryRepo().getCategoriesTree()).toString());
        categoriesColl.insert(dbObject);

        DBCollection impactAndFlowTypesColl = db.getCollection("impactAndFlowTypes");
        impactAndFlowTypesColl.drop();

        HashMap<String, String> impactTypes = new HashMap<>();
        for (Resource impactTypeResource: RepoFactory.getSingleElementRepo().getImpactTypes()) {
            impactTypes.put(mongonize(impactTypeResource.getURI()), RepoFactory.getSingleElementRepo().getLabelOrURI(impactTypeResource));
        }
        HashMap<String, String> flowTypes = new HashMap<>();
        for (Resource flowTypeResource: RepoFactory.getSingleElementRepo().getElementaryFlowTypes()) {
            flowTypes.put(mongonize(flowTypeResource.getURI()), RepoFactory.getSingleElementRepo().getLabelOrURI(flowTypeResource));
        }
        dbObject = (BasicDBObject) JSON.parse("{impactTypes:" + toJson(impactTypes).toString()
                                            + ", flowTypes:" + toJson(flowTypes).toString() + "}");
        impactAndFlowTypesColl.insert(dbObject);

        // Impact and elementary flow types tree retrieval and insertion in a collection
        DBCollection impactAndFlowTypesTreeColl = db.getCollection("impactAndFlowTypesTree");
        impactAndFlowTypesTreeColl.drop();
        dbObject = (BasicDBObject) JSON.parse("{impactTypesTree:" + toJson(RepoFactory.getSingleElementRepo().getImpactTypesTree()).toString()
                                            + ", flowTypesTree:" + toJson(RepoFactory.getSingleElementRepo().getElementaryFlowTypesTree()).toString() + "}");
        impactAndFlowTypesTreeColl.insert(dbObject);

        DBCollection groupsColl = db.getCollection("groups");
        groupsColl.drop();
        HashMap<String, HashMap<String, Object>> groups = new HashMap<>();

        for (Group group: RepoFactory.getGroupRepo().getGroups()) {
            groups.put(group.getId(), getGroup(group, model));
        }

        computeGoupsOverlap();

        for (Entry<String, HashMap<String, Object>> entry : groups.entrySet()) {
            String groupId = entry.getKey();
            HashMap<String, Object> groupInfos = entry.getValue();
            groupInfos.put("overlap", groupOverlap.get(groupId));
            dbObject = (BasicDBObject) JSON.parse(toJson(groupInfos).toString());
            dbObject.append("_id", groupId);
            groupsColl.insert(dbObject);
        }

        DBCollection processesColl = db.getCollection("processes");
        processesColl.drop();

        for(Entry<String, HashMap<String, Object>> entry : processes.entrySet()) {
            String uri = entry.getKey();
            HashMap process = entry.getValue();
            dbObject = (BasicDBObject) JSON.parse(toJson(process).toString());
            dbObject.append("_id", mongonize(uri));
            processesColl.insert(dbObject);
        }

        DBCollection coefficientsColl = db.getCollection("coefficients");
        coefficientsColl.drop();

        for(Entry<String, HashMap<String, Object>> entry : coefficients.entrySet()) {
            String uri = entry.getKey();
            HashMap coefficient = entry.getValue();
            dbObject = (BasicDBObject) JSON.parse(toJson(coefficient).toString());
            dbObject.append("_id", mongonize(uri));
            coefficientsColl.insert(dbObject);
        }

        DBCollection refColl = db.getCollection("references");
        refColl.drop();

        dbObject = (BasicDBObject) JSON.parse("{references:" + toJson(references).toString()
                                            + ", referencesGroups:" + toJson(referencesGroups).toString() + "}");
        refColl.insert(dbObject);

        DBCollection graphColl = db.getCollection("graph");
        graphColl.drop();

        //ArrayList<Resource> sourceRelationResources = RepoFactory.getRelationRepo().getSourceRelationsResources();
        for (SourceRelation sourceRelation: RepoFactory.getRelationRepo().getSourceRelations()) {
            //String sourceURI = sourceRelationResource.getProperty(Datatype.hasOriginProcess).getResource().getURI();
            //String targetURI = sourceRelationResource.getProperty(Datatype.hasDestinationProcess).getResource().getURI();
            String sourceURI = sourceRelation.source.getURI();
            String targetURI = sourceRelation.destination.getURI();
            HashMap<String, Object> link = new HashMap<>();
            link.put("uri", sourceRelation.getURI());
            link.put("source", nodesURI.indexOf(sourceURI));
            link.put("target", nodesURI.indexOf(targetURI));
            if (sourceRelation.getType() != null)
                link.put("type", mongonize(sourceRelation.getType().getURI()));
            else
                link.put("type", "#none");
            links.add(link);
        }
        HashMap<String, RelationType> relationTypes = new HashMap<>();
        for (RelationType type: RepoFactory.getRelationRepo().getRelationTypes()) {
            relationTypes.put(mongonize(type.getURI()), type);
        }


        dbObject = (BasicDBObject) JSON.parse("{nodes:" + toJson(nodesLabel).toString()
                                            + ",nodesId:" + toJson(nodesId).toString()
                                            + ",links:" + toJson(links).toString()
                                            + ",types:" + toJson(relationTypes).toString() + "}");
        graphColl.insert(dbObject);

        DBCollection reportColl = db.getCollection("report");
        reportColl.drop();

        dbObject = (BasicDBObject) JSON.parse(toJson(report).toString());
        reportColl.insert(dbObject);

        mongoClose();*/
    }

    protected static void computeGoupsOverlap() {
        /*for(Entry<String, HashMap<String, Object>> entry : processes.entrySet()) {
            String processURI = entry.getKey();
            HashMap<String, Object> process = entry.getValue();
            for(Entry<String, Object> subEntry : ((HashMap<String, Object>)process.get("groups")).entrySet()) {
                String groupId = subEntry.getKey();
                if (!groupOverlap.containsKey(groupId)) {
                    groupOverlap.put(groupId, new HashMap<String, String>());
                }
                for(Entry<String, Object> subEntry2 : ((HashMap<String, Object>)process.get("groups")).entrySet()) {
                    String groupId2 = subEntry2.getKey();
                    String group2Label = (String) subEntry2.getValue();
                    if (groupId2 != groupId && !groupOverlap.get(groupId).containsKey(groupId2)) {
                        groupOverlap.get(groupId).put(groupId2, group2Label);
                    }
                }
            }
        }*/
    }

    protected static HashMap<String, Object> getGroup(Group group, Model model) throws Exception  {
        HashMap<String, Object> output = new HashMap<String, Object>();

        /*boolean isProcessGroup = false;
        if (model.contains(ResourceFactory.createResource(group.getURI()), RDF.type, Datatype.ProcessGroup)) {
            isProcessGroup = true;
        }
        HashMap<String, Object> elementsValue = new HashMap<>();
        HashMap<String, Object> elementsFlows = new HashMap<>();
        HashMap<String, Object> elementsImpacts = new HashMap<>();
        HashMap elementImpactsAndFlows;
        HashMap<String, Object> elementsImpactsAndFlows = new HashMap<>();
        HashMap<String, Object> processInfos, coeffInfos;
        HashMap<String, String> elementsURI = new HashMap<>();
        HashMap<String, String> groupInfosForRef = new HashMap<>();

        String unitLabel = unitsRepo.getUnitSymbol(group.getUnit().getSymbol());

        for (Dimension element: group.elements.dimensions) {
            Resource elementResource;
            try {
                if (isProcessGroup) {
                    elementResource = RepoFactory.getSingleElementRepo().getProcessForDimension(element, group.getUnit().getURI());
                    elementImpactsAndFlows = new HashMap();
                    elementImpactsAndFlows.putAll(transformValueHashMapURIKeys(RepoFactory.getSingleElementRepo().getCalculatedEmissionsForProcess(elementResource)));
                    elementImpactsAndFlows.putAll(transformValueHashMapURIKeys(RepoFactory.getSingleElementRepo().getImpactsForProcess(elementResource)));
                    elementsImpactsAndFlows.put(joinDimensionKeywords(element), elementImpactsAndFlows);

                    // process: keywords, impacts, flows, unit, groups (where the process is referenced), individual relations
                    if (!processes.containsKey(elementResource.getURI())) {
                        processInfos = new HashMap<>();
                        processInfos.put("keywords", element);
                        processInfos.put("impacts", transformValueHashMapURIKeys(RepoFactory.getSingleElementRepo().getImpactsForProcess(elementResource)));
                        processInfos.put("flows", transformValueHashMapURIKeys(RepoFactory.getSingleElementRepo().getCalculatedEmissionsForProcess(elementResource)));
                        processInfos.put("unit", unitLabel);
                        processInfos.put("uri", elementResource.getURI());
                        processInfos.put("id", elementResource.getURI().replace(Datatype.getURI(), ""));
                        processInfos.put("relations", getRelationsForElement(elementResource, model));
                        processInfos.put("groups", new HashMap<String, Object>());
                        processes.put(elementResource.getURI(), processInfos);
                    }
                    else {
                        processInfos = processes.get(elementResource.getURI());
                    }
                    ((HashMap)processInfos.get("groups")).put(mongonize(group.getId()), group.getLabel());
                }
                else {
                    elementResource = RepoFactory.getSingleElementRepo().getCoefficientForDimension(element, group.getUnit().getURI());
                    if (elementResource.hasProperty(Datatype.value) && null != elementResource.getProperty(Datatype.value)) {
                        Value value = new Value(
                            elementResource.getProperty(Datatype.value).getDouble(),
                            RepoFactory.getSingleElementRepo().getUncertainty(elementResource)
                        );
                        elementsValue.put(
                            joinDimensionKeywords(element),
                            value
                        );
                        if (!coefficients.containsKey(elementResource.getURI())) {
                            coeffInfos = new HashMap<>();
                            coeffInfos.put("keywords", element);
                            coeffInfos.put("unit", unitLabel);
                            coeffInfos.put("value", value);
                            coeffInfos.put("uri", elementResource.getURI());
                            coeffInfos.put("id", elementResource.getURI().replace(Datatype.getURI(), ""));
                            coeffInfos.put("relations", getRelationsForElement(elementResource, model));
                            coeffInfos.put("groups", new HashMap<String, Object>());
                            coefficients.put(elementResource.getURI(), coeffInfos);
                        }
                        else {
                            coeffInfos = coefficients.get(elementResource.getURI());
                        }
                        ((HashMap)coeffInfos.get("groups")).put(mongonize(group.getId()), group.getLabel());
                    }
                    else {
                        elementsValue.put(joinDimensionKeywords(element), "empty");
                    }
                }
                elementsURI.put(joinDimensionKeywords(element), elementResource.getURI().replace(Datatype.getURI(), ""));
            }
            catch (NoElementFoundException | MultipleElementsFoundException e) {
                if (isProcessGroup) {
                    elementsImpactsAndFlows.put(joinDimensionKeywords(element), "empty");
                }
                else {
                    elementsValue.put(joinDimensionKeywords(element), "empty");
                }
            }
        }
        for (Reference reference: group.getReferences()) {
            if (!references.containsKey(mongonize(reference.getURI()))) {
                references.put(mongonize(reference.getURI()), reference);
                referencesGroups.put(mongonize(reference.getURI()), new ArrayList<HashMap<String, String>>());
            }
            groupInfosForRef = new HashMap<>();
            groupInfosForRef.put("URI", group.getURI());
            groupInfosForRef.put("id", group.getId());
            groupInfosForRef.put("type", group.type.name());
            groupInfosForRef.put("label", group.getLabel());
            referencesGroups.get(mongonize(reference.getURI())).add(groupInfosForRef);
        }
        output.put("URI", group.getURI());
        output.put("id", group.getId());
        output.put("label", group.getLabel());
        output.put("comment", group.getComment());
        output.put("references", group.getReferences());
        output.put("elementsURI", elementsURI);
        output.put("dimensions", group.dimSet.dimensions);
        output.put("unit", unitLabel);
        output.put("commonKeywords", group.commonKeywords.keywords);
        output.put("sourceRelations", RepoFactory.getRelationRepo().getSourceRelationsForProcessGroup(ResourceFactory.createResource(group.getURI())));
        output.put("type", group.type);
        if (isProcessGroup) {
            output.put("elementsImpactsAndFlows", elementsImpactsAndFlows);
            nodesLabel.add(group.getLabel());
            nodesURI.add(group.getURI());
            nodesId.add(group.getId());
        }
        else {
            output.put("elementsValue", elementsValue);
        }*/

        return output;
    }

    protected static HashMap<String, Value> transformValueHashMapURIKeys(HashMap<String, Value> input) {
        HashMap<String, Value> output = new HashMap<>();
        Iterator it = input.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            output.put(mongonize((String)pairs.getKey()), (Value)pairs.getValue());
        }
        return output;
    }

    protected static String mongonize(String fieldName) {
        return fieldName.replace(".", "____");
    }

    protected static DB mongoConnect(String database) throws Exception {
        mongoClient = new MongoClient( "localhost" , 27017 );
        DB db = mongoClient.getDB( database );
        return db;
    }

    protected static void mongoClose() {
        mongoClient.close();
    }

    public static Result getDatabaseInfos(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection databaseInfosColl = db.getCollection("databaseInfos");
            String response = databaseInfosColl.findOne().toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
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

    public static Result getImpactAndFlowTypesPlain(String database) {
        authorizeCrossRequests();
        try {
            DB db = mongoConnect(database);
            DBCollection impactAndFlowTypesColl = db.getCollection("impactAndFlowTypes");
            String response = impactAndFlowTypesColl.findOne().toString();
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
            BasicDBObject query = new BasicDBObject("_id", mongonize(Datatype.getURI() + processURI));
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
            BasicDBObject query = new BasicDBObject("_id", mongonize(Datatype.getURI() + coeffURI));
            String response = coefficientsColl.findOne(query).toString();
            mongoClose();
            return ok(response);
        }
        catch (Exception e) {
            return ok(e.getMessage());
        }
    }

    protected static ArrayList<HashMap<String, Object>> getRelationsForElement(Resource element, Model model) {
        ArrayList<HashMap<String, Object>> relations = new ArrayList<>();

        /*Selector selector = new SimpleSelector(null, Datatype.involvesElement, element);
        StmtIterator iter = model.listStatements( selector );

        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Resource relationResource = s.getSubject();

                HashMap<String, Object> relation = new HashMap<>();

                Resource origin = relationResource.getProperty(Datatype.hasOriginProcess).getResource();
                Dimension originKeywords = RepoFactory.getSingleElementRepo().getSingleElementKeywords(origin);
                relation.put("originId", origin.getURI().replace(Datatype.getURI(), ""));
                relation.put("originKeywords", originKeywords);
                relation.put("originUnit", unitsRepo.getUnitSymbol(RepoFactory.getSingleElementRepo().getUnit(origin)));

                Resource coeff = relationResource.getProperty(Datatype.hasWeightCoefficient).getResource();
                Dimension coeffKeywords = RepoFactory.getSingleElementRepo().getSingleElementKeywords(coeff);
                relation.put("coeffId", coeff.getURI().replace(Datatype.getURI(), ""));
                relation.put("coeffKeywords", coeffKeywords);
                relation.put("coeffUnit", unitsRepo.getUnitSymbol(RepoFactory.getSingleElementRepo().getUnit(coeff)));

                Resource dest = relationResource.getProperty(Datatype.hasDestinationProcess).getResource();
                Dimension destKeywords = RepoFactory.getSingleElementRepo().getSingleElementKeywords(dest);
                relation.put("destId", dest.getURI().replace(Datatype.getURI(), ""));
                relation.put("destKeywords", destKeywords);
                relation.put("destUnit", unitsRepo.getUnitSymbol(RepoFactory.getSingleElementRepo().getUnit(dest)));

                Resource sourceRelationResource = relationResource.getProperty(Datatype.isDerivedFrom).getResource();
                SourceRelation sourceRelation = RepoFactory.getRelationRepo().getSourceRelation(sourceRelationResource);
                relation.put("sourceRelation", sourceRelation);

                if (relationResource.hasProperty(Datatype.exponent) && null != relationResource.getProperty(Datatype.exponent)) {
                    relation.put("exponent", relationResource.getProperty(Datatype.exponent).getDouble());
                }
                else {
                    relation.put("exponent", 1.0);
                }


                relations.add(relation);
            }
        }*/
        return relations;
    }

    /**
     * Create a string containing all the keywords URI alphabetically sorted
     */
    protected static String joinDimensionKeywords(Dimension dimension) {
        String output = new String();
        String[] keywords = new String[dimension.keywords.size()];
        int i = 0;
        for (Keyword keyword: dimension.keywords) {
            keywords[i] = keyword.getId();
            i++;
        }
        Arrays.sort(keywords);
        for (i = 0; i < keywords.length; i++) {
            output += mongonize(keywords[i]);
        }
        return output;
    }
}
