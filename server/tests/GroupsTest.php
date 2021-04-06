<?php

/**
 *
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class GroupsTest {

	/**
	 * Construtor
	 */
	public function __construct()
	{
		
	}
	
	/**
	 * 
	 */
	public function nearby() 
	{
		$locations = array(
			array("type"=>"ibeacon", "uuid"=>"00000000-0000-0000-0000-000000000000", "distance"=>2.5),
			array("type"=>"ibeacon", "uuid"=>"b7d1027d-6788-416e-994f-ea11075f1765", "distance"=>1.5),
		);
		$data = array("locations"=>$locations);
		$json = json_encode($data);
		echo json_encode($data, JSON_PRETTY_PRINT) . "\n";
		 
// 		$ch = curl_init('http://localhost:9080/api/groups/nearby');
		$ch = curl_init('https://great-room.appspot.com/api/groups/nearby');
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
		$this->nearby();
	}
}

$test = new GroupsTest();
$test->exec();