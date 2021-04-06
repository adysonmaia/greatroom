<?php namespace greatRoom\library;

/**
 * Classe para enviar mensagens para o GCM (Google Cloud Message)
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Gcm {
	/**
	 * @var Gcm
	 */
	private static $instance;
	/**
	 * @var string
	 */
	private $apiKey;
	/**
	 * @var string
	 */
	private $url;
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		$gcmConfig = \greatRoom\config\Config::getValue("gcm");
		$this->url = $gcmConfig["url"];
		$this->apiKey = $gcmConfig["api_key"];
	}
	
	/**
	 * Singleton
	 * @return \greatRoom\library\Gcm
	 */
	public static function getInstance()
	{
		if (!isset(self::$instance)) {
			$c = __CLASS__;
			self::$instance = new $c;
		}
	
		return self::$instance;
	}
	
	/**
	 * 
	 * @param string $topic
	 * @param array $data
	 * @throws \Exception
	 * @return integer message id
	 */
	public function sendDataToTopic($topic, $data) 
	{
		$postData = array(
			"to" => "/topics/$topic",
			"data" => $data,
		);
		
		$errorMessage = "Response not found";
		$response = $this->sendData($postData);
		if (! empty($response) && is_array($response)) {
			if (key_exists("message_id", $response)) {
				return (int)$response["message_id"];
			}
			if (key_exists("error", $response)) {
				$errorMessage = $response["error"];
			}
		}
		throw new \Exception($errorMessage);
	}
	
	/**
	 * 
	 * @param array $data
	 * @throws \Exception
	 * @return array
	 */
	private function sendData($data)
	{
		$fields = json_encode($data);
		$contextOptions = array(
			"http" => array(
				"method" => "POST",
				"header" =>
					"Authorization: key={$this->apiKey}\r\n" .
					"Content-Type: application/json\r\n",
				"content" => $fields,
			),
			"ssl" => array(
				"allow_self_signed" => TRUE,
				"verify_peer" => FALSE,
			),
		);
		$contextOptions = stream_context_create($contextOptions);
		$result = file_get_contents($this->url, FALSE, $contextOptions);
		
		/*
		$headers = array(
			'Authorization: key=' . $this->apiKey,
			'Content-Type: application/json'
		);
		
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $this->url);
		curl_setopt($ch, CURLOPT_POST, TRUE);
		curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
		curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
		$result = curl_exec($ch);
		curl_close($ch);*/
		
		$response = NULL;
		if (! empty($result))
			$response = json_decode($result, TRUE);
		
		if (empty($response))
			throw new \Exception("Failed to parse the response");
		
		return $response;
	}
}
