<?php

/**
 *
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class GroupObjectsTest {

	/**
	 * Construtor
	 */
	public function __construct()
	{
		
	}
	
	/**
	 * 
	 */
	public function objects() 
	{
		$group = array("id"=>2);
		$data = array("group"=>$group, "type"=>"person");
		$json = json_encode($data);
		echo json_encode($data, JSON_PRETTY_PRINT) . "\n";
		
		
// 		$ch = curl_init('http://localhost:9080/api/group/objects/current');
		$ch = curl_init('https://great-room.appspot.com/api/group/objects/current');
		curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
		curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_HTTPHEADER, array(
			'Content-Type: application/json',
			'Content-Length: ' . strlen($json))
		);
		 
		$result = curl_exec($ch);
		echo $result . "\n";
		
		echo "\n\n";
		$json = json_decode($result);
		echo json_encode($json, JSON_PRETTY_PRINT) . "\n";
	}
	
	/**
	 * 
	 */
	public function exec()
	{
		$this->objects();
	}
}

$test = new GroupObjectsTest();
$test->exec();