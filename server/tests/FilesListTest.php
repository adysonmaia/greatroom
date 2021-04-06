<?php

/**
 *
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class FilesListTest {

	/**
	 * Construtor
	 */
	public function __construct()
	{
		
	}
	
	/**
	 * 
	 */
	public function listFiles() 
	{ 
		$group = array("id"=>2);
		$data = array("group"=>$group);
		$json = json_encode($data);
		echo json_encode($data, JSON_PRETTY_PRINT) . "\n";
		 
		$ch = curl_init('http://localhost:9080/api/group/files');
// 		$ch = curl_init('https://great-room.appspot.com/api/group/files');
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
		$this->listFiles();
	}
}

$test = new FilesListTest();
$test->exec();