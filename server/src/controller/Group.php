<?php namespace greatRoom\controller;

/**
 *  
  * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class Group extends \greatRoom\controller\Controller {
	const ERROR_COD_INVALID_DATA = 1;
	const ERROR_STR_INVALID_DATA = "Dados inválidos";
	const ERROR_COD_OBJECTS_NOT_FOUND = 2;
	const ERROR_STR_OBJECTS_NOT_FOUND = "Nenhum objeto encontrado";
	const ERROR_COD_GROUP_NOT_FOUND = 3;
	const ERROR_STR_GROUP_NOT_FOUND = "Grupo não encontrado";
	
	
	/**
	 * @var \greatRoom\model\Group
	 */
	protected $modelGroup;
	/**
	 * @var \greatRoom\model\object\Object
	 */
	protected $modelObject;
	/**
	 * @var \greatRoom\library\Gcm
	 */
	protected $gcm;
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		parent::__construct();
		
		$this->modelGroup = new \greatRoom\model\Group();
		$this->modelObject = new \greatRoom\model\object\Object();
		$this->gcm = \greatRoom\library\Gcm::getInstance();
		
		$this->app->map("/api/group/checkin",
				array($this, 'doCheckIn'))
				->via("POST", "PUT");
		
		$this->app->map("/api/group/objects/current",
				array($this, 'getCurrentObjects'))
				->via("POST");
	}
	
	/**
	 * Faz o check-in de um lugar para um usuario
	 */
	public function doCheckIn()
	{
		try {
			$group = $this->api->getRequestParam("group");
			if (empty($group) || !is_array($group) || !key_exists("id", $group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			$group = $this->modelGroup->get($group["id"]);
			if (empty($group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			
			$objects = $this->api->getRequestParam("objects");
			if (empty($objects) || !is_array($objects))
				throw new \Exception(self::ERROR_STR_OBJECTS_NOT_FOUND, self::ERROR_COD_OBJECTS_NOT_FOUND);
			foreach ($objects as $object) {
				$object = $this->modelObject->find($object);
				if (!empty($object)) 
					$this->modelGroup->doCheckIn($group->id, $object->id);
			}
			
			$this->notifyCurrentObjectsChanged($group);
			$response = TRUE;
			$this->api->sendSuccessResponse($response);
		} catch (\Exception $e) {
			$this->api->sendExceptionResponse($e);
		}
	}
	
	/**
	 * Obtem os objetos atuais do grupo
	 */
	public function getCurrentObjects()
	{
		try {
			$group = $this->api->getRequestParam("group");
			if (empty($group) || !is_array($group) || !key_exists("id", $group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			$group = $this->modelGroup->get($group["id"]);
			if (empty($group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			
			$type = $this->api->getRequestParam("type");
			$objects = $this->modelGroup->getCurrentObjects($group->id, $type);
			$list = array();
			foreach ($objects as $row) {
				$object = $this->modelObject->find($row);
				if (!empty($object)) {
					$checkIn = $this->modelGroup->getObjectLastCheckIn($object->id, $group->id);
					if (!empty($checkIn)) {
						$object->checkin_date = \greatRoom\helper\DateTime::jsonFormat($checkIn->date);
					}
					$object = (array) $object;
					foreach ($object as $key => $value) {
						if (is_null($value)) {
							$object[$key] = "";
						}
					}
					$list[] = $object;
				}
			}
			
			$this->api->sendSuccessResponse($list);
		} catch (\Exception $e) {
			$this->api->sendExceptionResponse($e);
		}
	}
	/**
	 *
	 * @param object $group
	 */
	private function notifyCurrentObjectsChanged($group)
	{
		try {
			$topic = "group.{$group->id}.objects.changed";
			$this->gcm->sendDataToTopic($topic, (array) $group);
		} catch (\Exception $e) {
		}
	}
}