<?php

/**
 *
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class FilesDeleteTest {

	/**
	 * Construtor
	 */
	public function __construct()
	{
		
	}
	
	/**
	 * 
	 */
	public function deleteFiles() 
	{ 
		$group = array("id"=>2);
		$files = array();
		$files[] = array("name"=>"test", "extension"=>"pdf");
		$data = array("group"=>$group, "files"=>$files);
		$json = json_encode($data);
		echo json_encode($data, JSON_PRETTY_PRINT) . "\n";
		 
// 		$ch = curl_init('http://localhost:9080/api/group/files/delete');
		$ch = curl_init('https://great-room.appspot.com/api/group/files/delete');
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
		$this->deleteFiles();
	}
}

$test = new FilesDeleteTest();
$test->exec();