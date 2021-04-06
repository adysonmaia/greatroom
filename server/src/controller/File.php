<?php namespace greatRoom\controller;

/**
 *  
  * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class File extends \greatRoom\controller\Controller {
	const ERROR_COD_INVALID_DATA = 1;
	const ERROR_STR_INVALID_DATA = "Dados inválidos";
	const ERROR_COD_GROUP_NOT_FOUND = 2;
	const ERROR_STR_GROUP_NOT_FOUND = "Grupo não encontrado";
	
	/**
	 * @var \greatRoom\model\Group
	 */
	protected $modelGroup;
	/**
	 * @var string
	 */
	protected $uploadPath;
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
	
		$this->gcm = \greatRoom\library\Gcm::getInstance();
		$this->modelGroup = new \greatRoom\model\Group();
		$uploadConfig = \greatRoom\config\Config::getValue("upload");
		$this->uploadPath = $uploadConfig["path"];
		
		$this->app->map("/api/group/file/upload/:idGroup",
				array($this, 'upload'))
				->via("POST");
		
		$this->app->map("/api/group/files",
				array($this, 'listFiles'))
				->via("POST");
		
		$this->app->map("/api/group/files/delete",
				array($this, 'deleteFiles'))
				->via("POST", "DELETE");
	}

	/**
	 * Upload de um arquivo para um grupo
	 * @param integer $idGroup
	 */
	public function upload($idGroup) 
	{
		try {
			$idGroup = (int) $idGroup;
			$group = $this->modelGroup->get($idGroup);
			if (empty($group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			if (empty($_FILES) || ! key_exists("file", $_FILES) || ! key_exists("tmp_name", $_FILES["file"])) 
				throw new \Exception(self::ERROR_STR_INVALID_DATA, self::ERROR_COD_INVALID_DATA);
			
			$tmpFilePath = $_FILES["file"]["tmp_name"];
			$fileName = $_FILES["file"]["name"];
			$fileName = \greatRoom\helper\File::createFileName($fileName);
			$filePath = \greatRoom\helper\Path::join($this->uploadPath, $group->id, $fileName);
			// TODO configurar permissao
			move_uploaded_file($tmpFilePath, $filePath);		
			
			$response = $this->getFileInfo($filePath);
			$this->notifyFilesChanged($group);
			$this->api->sendSuccessResponse($response);
		} catch (\Exception $e) {
			$this->api->sendExceptionResponse($e);
		}	
	}
	
	/**
	 * lista os arquivos de um grupo
	 * 
	 */
	public function listFiles()
	{
		try {
			$group = $this->api->getRequestParam("group");
			if (empty($group) || !is_array($group) || !key_exists("id", $group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			$group = $this->modelGroup->get($group["id"]);
			if (empty($group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			
			$list = array();
			$dirPath = \greatRoom\helper\Path::join($this->uploadPath, $group->id) . "/";
			if (file_exists($dirPath) && is_dir($dirPath)) {
				$handle = opendir($dirPath);
				while (FALSE !== ($item = readdir($handle)))
				{
					$itemPath = \greatRoom\helper\Path::join($dirPath, $item);
					if (is_file($itemPath)) {
						$list[] = $this->getFileInfo($itemPath);
					}
				}
				closedir($handle);
			}
			
			$cmpFunction = function($a, $b) {
				$a = \greatRoom\helper\String::lowerCase($a["name"]);	
				$b = \greatRoom\helper\String::lowerCase($b["name"]);
				return strcasecmp($a, $b);
			};
			usort($list, $cmpFunction);
			
			$this->api->sendSuccessResponse($list);
		} catch (\Exception $e) {
			$this->api->sendExceptionResponse($e);
		}
	}
	
	/**
	 * Apaga os arquivos de um grupo
	 */
	public function deleteFiles()
	{
		try {
			$group = $this->api->getRequestParam("group");
			if (empty($group) || !is_array($group) || !key_exists("id", $group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			$group = $this->modelGroup->get($group["id"]);
			if (empty($group))
				throw new \Exception(self::ERROR_STR_GROUP_NOT_FOUND, self::ERROR_COD_GROUP_NOT_FOUND);
			$files = $this->api->getRequestParam("files");
			if (empty($files) || !is_array($files))
				throw new \Exception(self::ERROR_STR_INVALID_DATA, self::ERROR_COD_INVALID_DATA);
				
			$response = array("count"=>0);
			foreach ($files as $file) {
				if (! is_array($file) || ! key_exists("name", $file))
					continue;
				
				$filePath = \greatRoom\helper\Path::join($this->uploadPath, $group->id, $file["name"]);
				if (file_exists($filePath) && is_file($filePath)) {
					unlink($filePath);
					$response["count"]++;
				} else if (key_exists("extension", $file)) {
					$filePath = \greatRoom\helper\Path::join($this->uploadPath, $group->id, $file["name"] . "." . $file["extension"]);
					if (file_exists($filePath) && is_file($filePath)) {
						unlink($filePath);
						$response["count"]++;
					}
				}
			}
			
			$this->notifyFilesChanged($group);
			$this->api->sendSuccessResponse($response);
		} catch (\Exception $e) {
			$this->api->sendExceptionResponse($e);
		}
	}
	
	/**
	 * Pega as informacoes de um arquivo
	 * @param string $filePath
	 * @return array
	 */
	private function getFileInfo($filePath)
	{
		$resource = fopen($filePath, 'r');
		$contentType = \google\appengine\api\cloud_storage\CloudStorageTools::getContentType($resource);
		fclose($resource);
		$url = \google\appengine\api\cloud_storage\CloudStorageTools::getPublicUrl($filePath, false);
		$size = filesize($filePath);
		$info = pathinfo($filePath);
		
		$fileInfo = array(
			"url" => $url,
			"name" => $info["filename"],
			"extension" => $info["extension"],
			"size" => $size,
			"type" => $contentType,
		);
		return $fileInfo;
	}
	
	/**
	 * 
	 * @param object $group
	 */
	private function notifyFilesChanged($group)
	{
		try {
			$topic = "group.{$group->id}.files.changed";
			$this->gcm->sendDataToTopic($topic, (array) $group);
		} catch (\Exception $e) {
		}
	}
}