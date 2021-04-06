<?php

/**
 *
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class GroupTest {

	/**
	 * Construtor
	 */
	public function __construct()
	{
		
	}
	
	/**
	 * 
	 */
	public function doCheckIn() 
	{ 
		$group = array("id"=>2);
		$object = array(
			"email" => "adyson.maia@gmail.com",
			"name" => "Adyson Magalhães Maia",
			"type" => "PERSON",
			"image_url" => "https://graph.facebook.com/924633957597958/picture?height=150&width=150&migration_overrides=%7Boctober_2012%3Atrue%7D",
		);
		
		$data = array("group"=>$group, "objects"=>array($object));
		$json = json_encode($data);
		echo json_encode($data, JSON_PRETTY_PRINT) . "\n";
		 
// 		$ch = curl_init('http://localhost:9080/api/group/checkin');
		$ch = curl_init('https://great-room.appspot.com/api/group/checkin');
		curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
		curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_HTTPHEADER, array(
			'Content-Type: application/json',
			'Content-Length: ' . strlen($json))
		);
		 
		$result = curl_exec($ch);
		echo $result . "\n";
	}
	
	/**
	 * 
	 */
	public function exec()
	{
		$this->doCheckIn();
	}
}

$test = new GroupTest();
$test->exec();