<?php

header('Content-type: application/json');
date_default_timezone_set('UTC');

require ('includes/util.php');
require ('includes/simpleCache.php');

$cache = new SimpleCache();
$url   = $_POST['url'];

if ($dbpedia_encoded_result = $cache->get_cache('profile:'.sha1($url))){
	$results = json_decode($dbpedia_encoded_result);
} else {
	$fields = array('url' => $url);
	$results = utility::getCURLPostResult("http://ahmadassaf.com/projects/SPARA/ajax-builder-userProfile.php", $fields);
	$cache->set_cache('profile:'.sha1($url), json_encode($results));
}

// The user profile has been retreived, save it in the data.csv
$data = fopen('cache/data.csv', 'w');
foreach ($results->dbpedia as $field) {
	fwrite($data, $field); fwrite($data, "\n");
}
fclose($data);
// Copy the main data.csv to a core.csv
copy('cache/data.csv', 'cache/core-data.csv');

//build the user profile.csv
$profile = fopen('cache/data_profile.csv', 'w');
foreach ($results->movies as $movie) {
	fwrite($profile, $movie->profile);
	fwrite($profile, "\n");
}
fclose($profile);

//execute the java recommendation code
exec("java -jar cache/spara.jar --jaccard-generate-data cache/data.csv 100");
exec("java -jar cache/spara.jar --jaccard-recommend cache/data.csv cache/data_profile.csv");

$csvData = file_get_contents("cache/data_jaccard_recommendation.csv"); 
$csvNumColumns = 1;  $csvDelim = "\n"; $recommendationsArray = array();

$counter = 0;
$data = array_chunk(str_getcsv($csvData, $csvDelim), $csvNumColumns);
$moviesURIs = array();
foreach ($data as $recommendedmovie) {
	if ($counter < 10) {
		$parts = explode(",", $recommendedmovie[0]);
    $keys = parse_url($parts[0]); // parse the url
     $path = explode("/", $keys['path']); // splitting the path
     $last = end($path); // get the value of the last elemen
     $moviesURIs[] = $parts[0];
     $recommendationsArray[] = array(
     	"id" => $parts[0],
     	"name" => utf8_decode($last),
     	"data" => array( '$'.'dim' => (float)str_replace('"','',$parts[1]) * 20 , "uri" => $parts[0] , "category" => "Film" )
     	);
 }
 $counter++;
}
$cache->set_cache('unique_movies_list', json_encode($moviesURIs));
$profile = array();
$profile["id"] = "1";
$profile["name"] = "Ahmad-A-Assaf";
$profile["children"] = $recommendationsArray;
echo json_encode($profile);
die();

?>