package exercises

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.bson.Document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.mongodb.client.MongoClients

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def objectMapper = new ObjectMapper()

// convert world csv data to json
def tables = ["tweet", "location"]

tables.forEach { tableName ->
	def csvFilePath = "src/main/resources/" + tableName + "_data.csv"
	def jsonFilePath = "src/main/resources/" + tableName + "_data_world.json"
	
	new File(csvFilePath).withReader {
		reader ->CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
		
		ArrayNode jsonArray = objectMapper.createArrayNode()
		
		for(record in csvParser) {
			ObjectNode jsonNode = objectMapper.createObjectNode()
			record.toMap().each {
				key, value ->
				key = key.trim()
				if(value.isNumber()) {
					jsonNode.put(key, value.toBigDecimal())
				}else if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
					jsonNode.put(key, value.toBoolean())
				}else {
					jsonNode.put(key, value.toString())
				}
			}
			jsonArray.add(jsonNode)
		}
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonFilePath), jsonArray)
	}
	
}


// ===============================================================================================================

// convert uk csv data to json
def ukLocationIDs = new HashSet<String>()
def locationCsvFilePath = "src/main/resources/location_data.csv"
def locationJsonFilePath = "src/main/resources/location_data_uk.json"

new File(locationCsvFilePath).withReader { reader ->
	CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
	ArrayNode locationJsonArray = objectMapper.createArrayNode()
	
	for (record in csvParser) {
		ObjectNode jsonNode = objectMapper.createObjectNode()
		def isUKLocation = false
		def locationID = -1
		
		record.toMap().each { key, value ->
			key = key.trim()
			if (value.isNumber()) {
				jsonNode.put(key, value.toBigDecimal())
				if (key.equalsIgnoreCase("LocationID")) {
					locationID = value.toBigDecimal()
				}
			} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				jsonNode.put(key, value.toBoolean())
			} else {
				jsonNode.put(key, value.toString())
				if (key.equalsIgnoreCase("Country") && value.equalsIgnoreCase("United Kingdom")) {
					isUKLocation = true
					ukLocationIDs.add(locationID)
				}
			}
		}
		if (isUKLocation) locationJsonArray.add(jsonNode)
	}
	objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(locationJsonFilePath), locationJsonArray)
}

def tweetCsvFilePath = "src/main/resources/tweet_data.csv"
def tweetJsonFilePath = "src/main/resources/tweet_data_uk.json"

new File(tweetCsvFilePath).withReader { reader ->
	CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
	ArrayNode tweetJsonArray = objectMapper.createArrayNode()
	
	for (record in csvParser) {
		ObjectNode jsonNode = objectMapper.createObjectNode()
		def shouldAdd = true
		
		record.toMap().each { key, value ->
			key = key.trim()
			if (value.isNumber()) {
				def numberValue = value.toBigDecimal()
				jsonNode.put(key, numberValue)
				if (key.equalsIgnoreCase("LocationID") && !ukLocationIDs.contains(numberValue)) {
					shouldAdd = false // Skip non-UK LocationID tweets
				}
			} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				jsonNode.put(key, value.toBoolean())
			} else {
				jsonNode.put(key, value.toString())
			}
		}
		if (shouldAdd) tweetJsonArray.add(jsonNode) // Add only UK-based tweets to JSON
	}
	objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(tweetJsonFilePath), tweetJsonArray)
}


// ===============================================================================================================

// upload all json data to mongo db
def properties = new Properties()
def propertiesFile = new File('src/main/resources/mongodb.properties')
propertiesFile.withInputStream {
	properties.load(it)
}

def mongoClient = MongoClients.create("mongodb+srv://${properties.USN}:${properties.PWD}@cluster0.${properties.SERVER}.mongodb.net/${properties.DB}?retryWrites=true&w=majority");
def db = mongoClient.getDatabase(properties.DB);

def regions = ["uk", "world"]

regions.forEach { region ->
	tables.forEach { tableName ->
		def col = db.getCollection(region + '_' + tableName)

		col.drop()
		println("Uploading " + region + "_" + tableName + " table data to mongoDB...")

		def jsonFile = new File('src/main/resources/'+ tableName +'_data_'+ region + '.json')
		def jsonSlurper = new JsonSlurper()
		def list = jsonSlurper.parseText(jsonFile.text)

		def docsToInsert = list.collect { obj ->
			Document.parse(JsonOutput.toJson(obj))
		}

		col.insertMany(docsToInsert)
		println col.countDocuments()
		println("Uploaded " + region + "_" + tableName + " table data to mongoDB...\n")
	}
}

