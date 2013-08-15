<?php

header('Content-type: application/json');
date_default_timezone_set('UTC');

require ('includes/util.php');
require ('includes/simpleCache.php');

$cache = new SimpleCache();

$uri  = $_POST['uri'];

$dbpediaSPARQL ="http://dbpedia.org/sparql?query=";
$query = '
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
					<'.$uri.'> dcterms:subject ?o.
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
					<'.$uri.'> dcterms:subject ?o.
					?m a dbpedia-owl:Film.
				}
				GROUP BY ?m ORDER BY DESC(?num)
				LIMIT 20
			}
		}
	}'; 

	if ($dbpedia_encoded_result = $cache->get_cache('dbpedia:'.sha1($uri))){
		$dbpediaRelated = json_decode($dbpedia_encoded_result);
	} else {
		$dbpediaRelated = array();
		$SPARQLQuery  = $dbpediaSPARQL.urlencode($query);
		$SPARQLResult = json_decode(utility::getCURLJSONResult($SPARQLQuery));
		if ( $SPARQLResult && isset($SPARQLResult->results) && is_array($SPARQLResult->results->bindings) && count($SPARQLResult->results->bindings) > 0 ){
			foreach ($SPARQLResult->results->bindings as $dbpedia_result) {
				$triple = '"'.$dbpedia_result->m->value.'","'.$dbpedia_result->p->value.'","'.$dbpedia_result->o->value.'"';
				$dbpediaRelated[] = $triple;
			}
		}
		$cache->set_cache('dbpedia:'.sha1($uri), json_encode($dbpediaRelated));
	}	
	echo(json_encode($dbpediaRelated));
	die();

?>