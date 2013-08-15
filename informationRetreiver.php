<?php

require_once ('includes/google/Google_Client.php');
require_once ('includes/google/Google_YouTubeService.php');

class informationRetreiver {

	private $result;
	private $client;
	private $dbpediaSPARQL ="http://dbpedia.org/sparql?query=";
	private $google_key;

	public function __construct() { 
		$this->result     = array();
		$this->cache      = new SimpleCache();
		$this->client     = new Google_Client();
		$this->google_key = "AIzaSyCa58KRY6XmjxmMsiSBMXLKxsSBC3_Yf40";
		$this->client->setDeveloperKey($this->google_key);
	}

	function movie($uri,$relatedItems ) {
		return ($this->dbpediaMovieDetails($uri,$relatedItems ));
	}

	function dbpediaMovieDetails($uri,$relatedItems ) {
		$query = 'select ?b ?c where{
			{ 
				<'.$uri.'> ?b ?c .
				FILTER( ?b = foaf:name || ?b = dbpedia-owl:abstract).
				FILTER langMatches(lang(?c),"en").
			}
			UNION
			{
				<'.$uri.'> ?b ?c.
				FILTER( ?b = dbpprop:director || ?b = dbpprop:writer || ?b =  dbpprop:basedOn || ?b = dbpprop:starring).
			}
		}';
		if ($movie_encoded_result = $this->cache->get_cache('movie_info:'.sha1($uri))){
			$movie = json_decode($movie_encoded_result);
		} else {
			$movie = array();
			$SPARQLQuery  = $this->dbpediaSPARQL.urlencode($query);
			$SPARQLResult = json_decode(utility::getCURLJSONResult($SPARQLQuery));
			if ( $SPARQLResult && isset($SPARQLResult->results) && is_array($SPARQLResult->results->bindings) && count($SPARQLResult->results->bindings) > 0 ){
				foreach ($SPARQLResult->results->bindings as $information) {
					switch ($information->b->value) {
						case "http://dbpedia.org/ontology/abstract" : $abstract = $information->c->value; break;
						case "http://xmlns.com/foaf/0.1/name" : $name = $information->c->value; break;
						case "http://dbpedia.org/property/director" : $director = $information->c->value; break;
					}
				}
				$video = ($this->youtube_search($name));
				$jsonurl = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=".urlencode($name);
				$result = json_decode(file_get_contents($jsonurl), true);
				$images = array();
				if ($result["responseData"]["results"]) {
					foreach($result["responseData"]["results"] as $image) {
						$images[] = $image["url"];
					}
				}
				$movie = array(
					"id" => $uri,
					"name" => $name,
					"director" => $director,
					"abstract" => $abstract,
					"image" => $images,
					"category"=> "Film",
					"video" => $video,
					"children" => $relatedItems);
			}
			$this->cache->set_cache('movie_info:'.sha1($uri), json_encode($movie));
		}	
		return $movie;
	}

	function youtube_search($keyword) {
		$category = "Film";
		$result = array();
		if($youtube_search_decoded = $this->cache->get_cache('youtube_search-'.$keyword)){
			$youtube_search = json_decode($youtube_search_decoded);
		} else {
			$url = "select * from json where url='http://gdata.youtube.com/feeds/api/videos?q=".urlencode($keyword." trailer")."&alt=json&max-results=10&?category=". $category ."&key=". $this->google_key ."'";
			$youtube_search = utility::getYQLResult($url);
			$this->cache->set_cache('youtube_search-'.$keyword, json_encode($youtube_search));
		}
		if ( isset($youtube_search->json->feed->entry) ) {
			if ( is_array($youtube_search->json->feed->entry) ) {
				foreach ($youtube_search->json->feed->entry as $video) {
					$result[] =  $video;
				}
			} else  $result[] =  $youtube_search->json->feed->entry;  
		} 
		return $this->youTubeParser($result);
	}

	function youTubeParser($result) {
		$results = array();
		if (isset($result) && !empty($result)) {
			foreach ($result as $video) {	
				$args = array(
					"service" => "youtube",
					"type"    => "video",
					"time"    => isset($video->published->_t) ? utility::timeAgo(strtotime($video->published->_t)) : utility::timeAgo(strtotime($video->published)),
					"title"   => isset($video->title->_t) ? $video->title->_t : $video->title,
					"link"    => str_replace("&feature=youtube_gdata", "", $video->link[0]->href),
					"author"  => isset($video->author->name->_t) ? $video->author->name->_t  : $video->group->credit->display,
					"embed"   => utility::parse_youtube_url($video->link[0]->href),
					"thumbnail" => $video->media_group->media_thumbnail[0]->url
					);
				$results[] = $args; 
			}	
		}
		return $results;
	}
}

?>