<?php

header('Content-type: application/json');
date_default_timezone_set('UTC');

require_once("includes/qpath/src/qp.php");
require ('includes/util.php');
require ('includes/simpleCache.php');

$url    = $_POST['url']; 
$cache  = new SimpleCache();

$SPARQL_endpoint ="http://dbpedia.org/sparql?query=";
$number_of_movies = 13;

$user_profile_cache = 'imdb:'.sha1($url);
if ($user_profile_encoded_result = $cache->get_cache($user_profile_cache)){
	$result = json_decode($user_profile_encoded_result);
} else {
	$result = array(); $dbpediaRelated = array();
	$curl = utility::getCURLResult($url);
	$qp = htmlqp($curl);
	$items = $qp->find('.list_item');
	$itemscounter = 0;
	foreach($items as $item) {
		if ($itemscounter < $number_of_movies ) {
			$movieName = utf8_decode($item->find('.info b:first-of-type a')->text());
			$secondaries = $item->find('.secondary');
			foreach($secondaries as $secondary) { 
				if (stripos($secondary->text(), "Director") !== false) {
					$director =  $secondary->find('a')->text();
				}
			}
			$query = 'SELECT ?uri ?name WHERE { ?uri a dbpedia-owl:Film; dbpprop:name ?name FILTER regex(?name, "^'. $movieName .'$", "i") }'; 
			$SPARQLQuery  = $SPARQL_endpoint.urlencode($query);
			$SPARQLResult = json_decode(utility::getCURLJSONResult($SPARQLQuery));
			if ( $SPARQLResult && isset($SPARQLResult->results) && is_array($SPARQLResult->results->bindings) && count($SPARQLResult->results->bindings) > 0 ){
				$dbpediaURI = $SPARQLResult->results->bindings[0]->uri->value;

				$dbpediaQuery = '
				SELECT DISTINCT ?m ?p ?o
				WHERE
				{
					{
						{
							?m ?p ?o.
							FILTER(regex(?p,"^http://dbpedia.org/ontology/")) .
							MINUS { ?m dbpedia-owl:abstract ?o } .
							MINUS { ?m dbpedia-owl:thumbnail ?o } .
							MINUS { ?m dbpedia-owl:wikiPageExternalLink ?o} .
							MINUS { ?m ?p ?o.
								?p a owl:DatatypeProperty .}
							}
							{ 
								SELECT ?m count(?m) AS ?num
								WHERE
								{
									?m dcterms:subject ?o.
									<'. $dbpediaURI .'> dcterms:subject ?o.
									?m a dbpedia-owl:Film.
								}
								GROUP BY ?m ORDER BY DESC(?num)	
								LIMIT 20
							}
						}
						UNION
						{
							?m ?p ?o.
							?m dcterms:subject ?o .
							{ 
								SELECT ?m count(?m) AS ?num
								WHERE
								{
									?m dcterms:subject ?o.
									<'. $dbpediaURI .'> dcterms:subject ?o.
									?m a dbpedia-owl:Film.
								}
								GROUP BY ?m ORDER BY DESC(?num)
								LIMIT 20
							}
						}
					}'; 
					if ($dbpedia_encoded_result = $cache->get_cache('dbpedia:'.sha1($dbpediaURI))){
						$dbpediaRelated = json_decode($dbpedia_encoded_result);
					} else {
						$dbpedia_results = array();
						$dbpediaFullQuery  = $SPARQL_endpoint.urlencode($dbpediaQuery);
						$dbpedia_SPARQLResult = json_decode(utility::getCURLJSONResult($dbpediaFullQuery));
						if ( $dbpedia_SPARQLResult && isset($dbpedia_SPARQLResult->results) && is_array($dbpedia_SPARQLResult->results->bindings) && count($dbpedia_SPARQLResult->results->bindings) > 0 ){
							foreach ($dbpedia_SPARQLResult->results->bindings as $dbpedia_result) {
								$triple = '"'.$dbpedia_result->m->value.'","'.$dbpedia_result->p->value.'","'.$dbpedia_result->o->value.'"';
								$dbpediaRelated[] = $triple;
							}
						}
						$cache->set_cache('dbpedia:'.sha1($dbpediaURI), json_encode($dbpediaRelated));
					}
					$rating = $item->find('.rating-rating .value')->text();
					$result["movies"][] = array("profile" => '"'.$dbpediaURI.'","'.strstr($rating, '.', true).'"');
				}
				$itemscounter++;
			}
		}
		$result["dbpedia"] = array_unique($dbpediaRelated);
		$cache->set_cache($user_profile_cache, json_encode($result));
	}
	echo json_encode($result);
	die();
?>