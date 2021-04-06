<?php namespace greatRoom\model\object;

/**
 * Classe de modelo das pessoas
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Person extends \greatRoom\model\Model {
	const OBJECT_TYPE_PERSON = "PERSON";
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		parent::__construct();
	}
	
	/**
	 * Encontra ou cria um usuario
	 * @param array $data
	 * @return \stdClass|NULL
	 */
	public function find($data)
	{
		if (empty($data) || !is_array($data))
			return NULL;
		$person = NULL;
		if (key_exists("id", $data))
			$person = $this->findByKey("id" , (int)$data["id"]);
		if (empty($person) && key_exists("object_id", $data))
			$person = $this->findByKey("object_id" , (int)$data["object_id"]);
		if (empty($person) && key_exists("uuid", $data))
			$person = $this->findByKey("uuid" , strtolower(trim($data["uuid"])));
		if (empty($person) && key_exists("email", $data))
			$person = $this->findByKey("email" , trim($data["email"]));
		
		$personKeys = array("email", "name", "image_url");
		$objectKeys = array("uuid");
		if (empty($person)) {
			$personData = array();
			$objectData = array("type"=>self::OBJECT_TYPE_PERSON);
			foreach ($data as $key => $value) {
				if (in_array($key, $personKeys)) {
					$personData[$key] = $value;
				}
				if (in_array($key, $objectKeys)) {
					$objectData[$key] = $value;
				}
			}
			if (key_exists("email", $data)) {
				try {
					$id = (int)$this->insert("object", $objectData);
					$personData["object_id"] = $id;
					$this->insert("person", $personData);
					$person = $this->findByKey("object_id" , $id);
				} catch (Exception $e) {
					$person = NULL;
				}
			}
		} else {
			$currentData = (array) $person; 
			$personData = array();
			$objectData = array();
			foreach ($data as $key => $value) {
				if (key_exists($key, $currentData)) {
					if (in_array($key, $personKeys) && $currentData[$key] != $value) {
						$personData[$key] = $value;
					}
					if (in_array($key, $objectKeys) && $currentData[$key] != $value) {
						$objectData[$key] = $value;
					}
				}
			}
			$changed = FALSE;
			if (! empty($objectData)) {
				$this->update("object", $objectData, array("id"=>$person->object_id));
				$changed = TRUE;
			}
			if (! empty($personData)) {
				$this->update("person", $personData, array("object_id"=>$person->object_id));
				$changed = TRUE;
			}
			if ($changed) {
				$person = $this->findByKey("object_id", $person->object_id);
			}
		}
		return $person;
	}
	
	/**
	 * Procura uma pessoa por alguma chave
	 * @param string $key nome da coluna
	 * @param mixed $value
	 * @return \stdClass|NULL
	 */
	protected function findByKey($key, $value)
	{
		$sql = "SELECT * 
		FROM `person` AS p
		JOIN `object` AS o ON o.id=p.object_id
		WHERE `$key` = :value LIMIT 1";
		$stmt = $this->pdo->prepare($sql);
		$this->bindValue($stmt, ":value", $value);
		return $this->requestSingleObj($stmt);
	}
}