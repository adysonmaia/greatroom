<?php

/**
 *
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class FileTest {

	/**
	 * Construtor
	 */
	public function __construct()
	{
		
	}
	
	private function getCurlValue($filename, $contentType, $postname)
	{
		// PHP 5.5 introduced a CurlFile object that deprecates the old @filename syntax
		// See: https://wiki.php.net/rfc/curl-file-upload
		if (function_exists('curl_file_create')) {
			return curl_file_create($filename, $contentType, $postname);
		}
	
		// Use the old style if using an older version of PHP
		$value = "@{$this->filename};filename=" . $postname;
		if ($contentType) {
			$value .= ';type=' . $contentType;
		}
	
		return $value;
	}
	
	private function getFileMimeType($file) 
	{
	    if (function_exists('finfo_file')) {
	        $finfo = finfo_open(FILEINFO_MIME_TYPE);
	        $type = finfo_file($finfo, $file);
	        finfo_close($finfo);
	    } else {
	        $type = mime_content_type($file);
	    }
	    return $type;
	}
	
	
	/**
	 * 
	 */
	public function upload() 
	{ 
		$idGroup = 2;
		$filePath = realpath("./test.pdf");
		$fileMimeType = $this->getFileMimeType($filePath);
		$data = array(
			"file" => $this->getCurlValue($filePath, $fileMimeType, "test.pdf"),
		);
		 
// 		$ch = curl_init("http://localhost:9080/api/group/file/upload/$idGroup");
		$ch = curl_init("https://great-room.appspot.com/api/group/file/upload/$idGroup");
		curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
		curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		 
		$result = curl_exec($ch);
		echo $result . "\n";
	}
	
	/**
	 * 
	 */
	public function exec()
	{
		$this->upload();
	}
}

$test = new FileTest();
$test->exec();