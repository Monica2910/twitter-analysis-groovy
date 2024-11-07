package exercises

import static com.mongodb.client.model.Accumulators.*
import static com.mongodb.client.model.Aggregates.*
import static com.mongodb.client.model.Filters.*
import static com.mongodb.client.model.Sorts.*
import static com.mongodb.client.model.Projections.*
import com.mongodb.client.model.Field
import com.mongodb.client.model.Indexes

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

// Access the 'Tweets' and 'Location' collections
MongoCollection<Document> tweetsCollection = db.getCollection("world_tweet")
MongoCollection<Document> locationCollection = db.getCollection("world_location")

if (!tweetsCollection.listIndexes().any { it["name"] == "LocationID_1" }) {
    tweetsCollection.createIndex(Indexes.ascending("LocationID"))
}
if (!locationCollection.listIndexes().any { it["name"] == "LocationID_1" }) {
    locationCollection.createIndex(Indexes.ascending("LocationID"))
}
if (!locationCollection.listIndexes().any { it["name"] == "Country_1" }) {
    locationCollection.createIndex(Indexes.ascending("Country"))
}

// country-wise total reach and avg sentiment
def result = tweetsCollection.aggregate([
    lookup('world_location', 'LocationID', 'LocationID', 'locationData'),
    unwind('$locationData'),
    group('$locationData.Country',
        sum('TotalReach', '$Reach'),
        avg('AvgSentiment', '$Sentiment')
    ),
    project(new Document("Country", '$_id')
        .append("TotalReach", 1)
        .append("AvgSentiment", 1)),
    sort(descending('TotalReach')),
    limit(10)
]).into([])
result.each { doc -> println doc.toJson() }
println()


// total reach for english, japanese, french languages
def result1 = tweetsCollection.aggregate([
    match(new Document("Lang", new Document("\$in", ["en", "ja", "fr"]))),
    group('$Lang', sum('TotalReach', '$Reach')),
    project(new Document("Lang", '$_id').append("TotalReach", 1))
]).into([])
result1.each { doc ->
    println "Total Reach for language '${doc.Lang}': ${doc.TotalReach}"
}
println()


// total tweets for english, japanese, french languages
def result2 = tweetsCollection.aggregate([
    match(new Document("Lang", new Document("\$in", ["en", "ja", "fr"]))),
    group('$Lang', sum('TweetCount', 1)),
    project(new Document("Lang", '$_id').append("TweetCount", 1))
]).into([])
result2.each { doc ->
    println "Total tweets for language '${doc.Lang}': ${doc.TweetCount}"
}
println()


// avg sentiment for english, japanese, french languages
def result3 = tweetsCollection.aggregate([
    match(new Document("Lang", new Document("\$in", ["en", "ja", "fr"]))),
    group('$Lang', avg('AvgSentiment', '$Sentiment')),
    project(new Document("Lang", '$_id').append("AvgSentiment", 1))
]).into([])
result3.each { doc ->
    println "Average sentiment for language '${doc.Lang}': ${doc.AvgSentiment}"
}
println()

mongoClient.close()
