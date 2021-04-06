<?php namespace greatRoom\model\location;

/**
 * Classe de modelo da localizacao por iBeacon
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Ibeacon extends \greatRoom\model\Model {
	const LOCATION_TYPE = "IBEACON";
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		parent::__construct();
	}
	
	/**
	 * Procurar os groups de um ibeacon
	 * @param array $data
	 * @return array
	 */
	public function findGroups($data)
	{
		if (empty($data) || !is_array($data) || !key_exists("uuid", $data))
			return array();
		$uuid = strtolower(trim($data["uuid"]));
		$sql = "SELECT g.* 
				FROM `location_ibeacon` AS li
				JOIN `location` AS l ON l.id=li.location_id
				JOIN `group` AS g ON g.id=l.group_id 
				WHERE li.uuid = :uuid";
		$stmt = $this->pdo->prepare($sql);
		$stmt->bindValue(":uuid", $uuid, \PDO::PARAM_STR);
		
		$distance = (double) key_exists("distance", $data) ? $data["distance"] : -1;
		$groups = $this->requestMultipleObjs($stmt);
		foreach ($groups as $group) {
			$group->distance = $distance; 
		}
		
		return $groups;
	}
}