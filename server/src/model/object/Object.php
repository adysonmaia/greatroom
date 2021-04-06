<?php namespace greatRoom\model\object;

/**
 * Classe de modelo dos objetos
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Object extends \greatRoom\model\Model {
	
	/**
	 * @var \greatRoom\model\object\Person
	 */
	protected $modelPerson;
	
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		parent::__construct();
		$this->modelPerson = new Person();
	}
	
	/**
	 * Encontra um objeto
	 * @param array $data
	 * @return \stdClass|NULL
	 */
	public function find($data)
	{
		if (is_object($data))
			$data = (array)$data;
		if (empty($data) || !is_array($data))
			return NULL;
		$object = NULL;
		if (key_exists("type", $data)) {
			$type = strtoupper(trim($data["type"]));
			switch ($type) {
				case Person::OBJECT_TYPE_PERSON:
					$object = $this->modelPerson->find($data);
					break;
				default:
					break;
			}
		}
		
		if (empty($object)) {
			if (key_exists("id", $data))
				$object = $this->findByKey("id" , (int)$data["id"]);
			if (empty($object) && key_exists("uuid", $data))
				$object = $this->findByKey("uuid" , strtolower(trim($data["uuid"])));
		}
		
		return $object;
	}
	
	/**
	 * Procura um objeto por alguma chave
	 * @param string $key nome da coluna
	 * @param mixed $value
	 * @return \stdClass|NULL
	 */
	protected function findByKey($key, $value)
	{
		$sql = "SELECT * FROM `object` AS o WHERE `$key` = :value LIMIT 1";
		$stmt = $this->pdo->prepare($sql);
		$this->bindValue($stmt, ":value", $value);
		return $this->requestSingleObj($stmt);
	}
}