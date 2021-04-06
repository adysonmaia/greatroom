<?php namespace greatRoom\model\location;

/**
 * Classe de modelo da localizacao por iBeacon
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Location extends \greatRoom\model\Model {
	
	/**
	 * @var \greatRoom\model\location\Ibeacon
	 */
	protected $modelIbeacon;
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		parent::__construct();
		$this->modelIbeacon = new Ibeacon();
	}
	
	/**
	 * Procurar os groups de uma localizacao
	 * @param array $data
	 * @return array
	 */
	public function findGroups($data)
	{
		if (is_object($data))
			$data = (array)$data;
		
		if (empty($data) || !is_array($data) || !key_exists("type", $data))
			return array();
		
		$groups = array();
		$type = strtoupper(trim($data["type"]));
		switch ($type) {
			case Ibeacon::LOCATION_TYPE:
				$groups = $this->modelIbeacon->findGroups($data);
				break;
			default:
				break;
		}
		return $groups;
	}
}