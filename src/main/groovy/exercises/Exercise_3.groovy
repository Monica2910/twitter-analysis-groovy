package exercises

import static com.mongodb.client.model.Accumulators.*
import static com.mongodb.client.model.Aggregates.*
import static com.mongodb.client.model.Filters.*
import static com.mongodb.client.model.Sorts.*
import static com.mongodb.client.model.Projections.*
import com.mongodb.client.model.Field

import org.bson.Document
import groovy.json.JsonOutput

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection

def properties = new Properties()
def propertiesFile = new File('src/main/resources/mongodb.properties')
propertiesFile.withInputStream {
	properties.load(it)
}

def mongoClient = MongoClients.create("mongodb+srv://${properties.USN}:${properties.PWD}@cluster0.${properties.SERVER}.mongodb.net/${properties.DB}?retryWrites=true&w=majority");
def db = mongoClient.getDatabase(properties.DB);

def startTime = System.currentTimeMillis()

MongoCollection<Document> tweetsCollection = db.getCollection("uk_tweet")
MongoCollection<Document> locationCollection = db.getCollection("uk_location")

println "Executing query: SELECT * FROM Tweets (Limited to 5 results for brevity)"
def pipeline_1 = [
    limit(5)
]
def result_1 = tweetsCollection.aggregate(pipeline_1).into([])
result_1.each { println it.toJson() }


// ===============================================================================================================

println "\nExecuting query: SELECT TweetID, Reach, Sentiment FROM Tweets (Limited to 5 results)"
def pipeline_2 = [
	project(fields(include('TweetID', 'Reach', 'Sentiment'), excludeId())),
    limit(5)
]
println(JsonOutput.prettyPrint(JsonOutput.toJson(tweetsCollection.aggregate(pipeline_2))))


// ===============================================================================================================

println "\nExecuting query: SELECT * FROM Tweets WHERE Reach > 1000 AND Sentiment > 1 (Limited to 5 results)"
def pipeline_3 = [
    match(and(gt("Reach", 1000), gt("Sentiment", 1))),
    limit(5)
]
def result_3 = tweetsCollection.aggregate(pipeline_3).into([])
result_3.each { println it.toJson() }


// ===============================================================================================================


println "\nExecuting query: SELECT Location.City, SUM(Tweets.Reach) AS TotalReach, AVG(Tweets.Sentiment) AS AvgSentiment FROM Tweets JOIN Location ON Tweets.LocationID = Location.LocationID GROUP BY Location.Country"
def pipeline = [
    lookup('uk_location', 'LocationID', 'LocationID', 'locationData'),
    unwind('$locationData'),
    group('$locationData.City',
        sum('TotalReach', '$Reach'),
        avg('AvgSentiment', '$Sentiment')
    ),
    project(new Document("City", '$_id')
        .append("TotalReach", 1)
        .append("AvgSentiment", 1)),
    sort(descending('TotalReach')),
	limit(10)
]

def result = tweetsCollection.aggregate(pipeline).into([])
result.each { doc -> println doc.toJson() }

// ===============================================================================================================

def endTime = System.currentTimeMillis()
println("\n\nTime taken for execution using mongoDB connection:: " + (endTime - startTime) + " ms")



mongoClient.close()
