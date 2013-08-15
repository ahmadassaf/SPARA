<?php

header('Content-type: application/json');
date_default_timezone_set('UTC');

require ('includes/util.php');
require ('includes/simpleCache.php');
require ('informationRetreiver.php');

$cache = new SimpleCache();

$uri   = $_POST['uri'];

if ($movie_encoded_result = $cache->get_cache('movie:'.sha1($uri))){
	$results = json_decode($movie_encoded_result);
} else {
	$fields  = array('uri' => $uri);
	$results = utility::getCURLPostResult("http://ahmadassaf.com/projects/SPARA/ajax-builder-related-movies.php", $fields);
	$cache->set_cache('movie:'.sha1($uri), json_encode($results));
}
$data = fopen('cache/temp-movie-data.csv', 'w');
foreach ($results as $field) {
	fwrite($data, $field);
	fwrite($data, "\n");
}
fclose($data);

utility::joinFiles(array('cache/core-data.csv', 'cache/temp-movie-data.csv'), 'cache/data.csv');

exec("java -jar cache/spara.jar --jaccard-generate-data cache/data.csv 100");
exec("java -jar cache/spara.jar --jaccard-recommend cache/data.csv cache/data_profile.csv");

$csvData = file_get_contents("cache/data_jaccard_recommendation.csv"); 
$csvNumColumns = 1;  $csvDelim = "\n"; $recommendationsArray = array();

$data = array_chunk(str_getcsv($csvData, $csvDelim), $csvNumColumns);
foreach ($data as $recommendedmovie) {
	$parts = explode(",", $recommendedmovie[0]);
    $keys = parse_url($parts[0]); // parse the url
    $path = explode("/", $keys['path']); // splitting the path
    $last = end($path); // get the value of the last elemen
    $recommendationsArray[] = array(
     	"id" => $parts[0],
     	"name" => utf8_decode($last),
     	"data" => array( '$'.'dim' => (float)str_replace('"','',$parts[1]) * 20 , "uri" => $parts[0] , "category" => "Film" )
     	);
 }
 $filteredResult = array();
 $movies = json_decode($cache->get_cache('unique_movies_list'));
 foreach($recommendationsArray as $recommendedItem) {
 	if (!in_array($recommendedItem["id"], $movies) ) {
 		$filteredResult[] = $recommendedItem;
 	} 
 }

 $relatedItems         = array_slice($filteredResult, 0,10);
 $caching_unique = array();
 foreach($relatedItems as $relatedItem) {
 	$caching_unique[] = $relatedItem["id"];
 }
 $cache->set_cache('unique_movies_list', json_encode(array_merge($movies,$caching_unique)));
 $informationRetreiver = new informationRetreiver();
 $richMovie            = $informationRetreiver->movie($uri,$relatedItems);
 echo json_encode($richMovie);
 die();

 ?>