<?php namespace greatRoom\model;

/**
 * Classe de modelo dos grupos
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Group extends \greatRoom\model\Model {
	/**
	 * Construtor
	 */
	public function __construct()
	{
		parent::__construct();
	}
	
	/**
	 * Obtem os dados de um grupo
	 * @param integer $id 
	 * @return \stdClass|NULL
	 */
	public function get($id)
	{
		$id = (int)$id;
		$sql = "SELECT * FROM `group` AS p WHERE p.id = :id LIMIT 1";
		$stmt = $this->pdo->prepare($sql);
		$stmt->bindValue(":id", $id, \PDO::PARAM_INT);
		return $this->requestSingleObj($stmt);
	}
	
	/**
	 * Faz um check-in
	 * @param integer $placeId
	 * @param integer $objectId
	 * @throws \Exception
	 */
	public function doCheckIn($groupId, $objectId)
	{
		$groupId = (int)$groupId;
		$objectId = (int)$objectId;
		$data = array(
			"group_id" => $groupId,
			"object_id" => $objectId	
		);
		$this->insert("group_checkin", $data);
	}
	
	/**
	 * Obtem os objetos que fizeram check-in recentimente
	 * @param integer $groupId
	 * @param integer $type
	 * @return array
	 */
	public function getCurrentObjects($groupId, $type = NULL)
	{
		$groupId = (int)$groupId;
		if (!empty($type))
			$type = strtoupper(trim($type));
				
		$sql = "SELECT o.*
				FROM `group_checkin` AS gc
				JOIN `object` AS o ON o.id=gc.object_id
				WHERE gc.group_id = :groupId AND gc.date >= DATE_SUB(NOW(), INTERVAL 15 MINUTE)
				";
		if (!empty($type))
			$sql .= " AND o.type = :type ";
		$sql .= " GROUP BY o.id ORDER BY o.type";
		$stmt = $this->pdo->prepare($sql);
		$stmt->bindValue(":groupId", $groupId, \PDO::PARAM_INT);
		if (!empty($type))
			$stmt->bindValue(":type", $type, \PDO::PARAM_STR);
		return $this->requestMultipleObjs($stmt);
	}
	
	/**
	 * Obtem as informacoes do ultimo check-in em um grupo de um objeto
	 * @param integer $objectId
	 * @param integer $groupId
	 * @return \stdClass|NULL
	 */
	public function getObjectLastCheckIn($objectId, $groupId)
	{
		$groupId = (int)$groupId;
		$objectId = (int)$objectId;
		$sql = "SELECT *
				FROM `group_checkin` AS gc
				WHERE gc.group_id = :groupId AND gc.object_id = :objectId
				ORDER BY date DESC
				LIMIT 1
				";
		$stmt = $this->pdo->prepare($sql);
		$stmt->bindValue(":groupId", $groupId, \PDO::PARAM_INT);
		$stmt->bindValue(":objectId", $objectId, \PDO::PARAM_INT);
		return $this->requestSingleObj($stmt);
	}
}