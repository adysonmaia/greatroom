<?php namespace greatRoom\controller;

/**
 *  
  * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class Groups extends \greatRoom\controller\Controller {
	const ERROR_COD_INVALID_DATA = 1;
	const ERROR_STR_INVALID_DATA = "Dados inválidos";
	
	/**
	 * @var \greatRoom\model\location\Location
	 */
	protected $modelLocation;
	/**
	 * @var \greatRoom\model\Group
	 */
	protected $modelGroup;
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		parent::__construct();
		
		$this->modelLocation = new \greatRoom\model\location\Location();
		$this->modelGroup = new \greatRoom\model\Group();
		
		$this->app->map("/api/groups/nearby",
				array($this, 'getNearby'))
				->via("POST");
	}
	
	/**
	 * Retorna os grupos proximos da localizacao de um objeto
	 */
	public function getNearby()
	{
		try {
			$locations = $this->api->getRequestParam("locations");
			if (empty($locations) || !is_array($locations)) {
				throw new \Exception(self::ERROR_STR_INVALID_DATA, self::ERROR_COD_INVALID_DATA);
			}
			
			$groupsId = array();
			foreach ($locations as $location) {
				if (empty($location) || !is_array($location) || !key_exists("type", $location))
					continue;
				$groups = $this->modelLocation->findGroups($location);
				foreach ($groups as $group) {
					$id = (int)$group->id;
					$groupsId[$id] = $group;
				}
			}
			
			$groups = array_values($groupsId);
			$this->calculateProximity($groups);
			$this->api->sendSuccessResponse($groups);
		} catch (\Exception $e) {
			$this->api->sendExceptionResponse($e);
		}
	}
	
	/**
	 * 
	 * @param array $groups
	 */
	private function calculateProximity(&$groups)
	{		
		$distanceTotal = 0.0;
		foreach ($groups as $group) {
			$distance = (double) property_exists($group, "distance") ? $group->distance : -1;
			if ($distance > 0.0)
				$distanceTotal += $distance;
		}
		
		foreach ($groups as $group) {
			$group->nearby = 0.0;
			if (property_exists($group, "distance")) {
				$group->nearby = 1.0 - ($group->distance / $distanceTotal);
				if ($group->nearby <= 0.0)
					$group->nearby = 1.0;
				unset($group->distance);
			}
		}
		
		$cmpFunction = function($a, $b) {
			$nearbyA = (double) $a->nearby;
			$nearbyB = (double) $b->nearby;
			
			if ($nearbyA > $nearbyB)
				return -1;
			else if ($nearbyA < $nearbyB)
				return 1;
			else
				return 0;
		};
		usort($groups, $cmpFunction);
	}
}