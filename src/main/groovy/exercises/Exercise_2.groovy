package exercises

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def jsonSlurper = new JsonSlurper()
def startTime = System.currentTimeMillis()
def tweetsfile = new File('src/main/resources/tweet_data_uk.json')
def locationsfile = new File('src/main/resources/location_data_uk.json')
def tweetsdata = jsonSlurper.parseText(tweetsfile.text)
def locationsdata = jsonSlurper.parseText(locationsfile.text)

// ====================================================================================================

println('\n\n--------------------Data Selection--------------------\n')

// Data Selection
def alltweets = tweetsdata.findAll()
alltweets.take(5).each { println(JsonOutput.toJson(it)) }

// ====================================================================================================

println('\n\n--------------------Data Projection--------------------\n')

// Data Projection
def projection = tweetsdata.collect{tweets ->
[
	"TweetID": tweets.TweetID, 
	"Reach": tweets.Reach, 
	"Sentiment": tweets.Sentiment
]}
println(JsonOutput.prettyPrint(JsonOutput.toJson(projection.take(5))))


// ====================================================================================================

println('\n\n--------------------Data Filtering--------------------\n')

// Data Filtering 
def filtertweets = tweetsdata.findAll{ tweets ->
	tweets.Reach > 1000 && tweets.Sentiment>1
}
filtertweets.take(5).each { println(JsonOutput.toJson(it)) } 


// ====================================================================================================

println('\n\n--------------------Data Combination/Grouping--------------------\n')

// Data Combination
def joinedData = tweetsdata.collect { tweet ->
    def location = locationsdata.find { it.LocationID == tweet.LocationID }
    [
        City: location ? location.City : null, 
        Reach: tweet.Reach ?: 0, 
        Sentiment: tweet.Sentiment ?: 0 
    ]
}.findAll { it.City } 

// Data Grouping
def aggregatedResults = joinedData.groupBy { it.City }.collectEntries { cityGroup ->
    def city = cityGroup.key
    def tweetsForCity = cityGroup.value

    def totalReach = tweetsForCity.sum { it.Reach }
    def avgSentiment = tweetsForCity*.Sentiment.sum() / (tweetsForCity.size() ?: 1)

    [(city): [TotalReach: totalReach, AvgSentiment: avgSentiment]]
}

def sortedResults = aggregatedResults.sort { -it.value.TotalReach }

println "\nAggregated Results:"
sortedResults.take(10).each { city, metrics ->
    println "City: ${city}, TotalReach: ${metrics.TotalReach}, AvgSentiment: ${metrics.AvgSentiment}"
}


// Execution time
def endTime = System.currentTimeMillis()
println("\n\nTime taken for execution using groovy:: " + (endTime - startTime) + " ms")
