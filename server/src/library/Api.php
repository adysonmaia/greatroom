<?php namespace greatRoom\library;

/**
 * Classe com funcoes uteis para a api
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Api {
	/**
	 * @var Api
	 */
	private static $instance;
	
	/**
	 * @var \Slim\Slim
	 */
	protected $app;
	protected $requestData;
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		$this->app = \Slim\Slim::getInstance();
		$this->requestData = array();
	}
	
	/**
	 * Singleton
	 * @return \greatRoom\library\Api
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
	 * Obtem um valor de um parametro da requisicao
	 * @param string $key chave do paramentro
	 * @param mixed $defaultValue valor padrao caso a chave nao seja encontrada
	 * @return mixed
	 */
	public function getRequestParam($key, $defaultValue = NULL)
	{
		$this->loadRequestData();
		if (key_exists($key, $this->requestData))
			return $this->requestData[$key];
		else
			return $defaultValue;
	}
	
	/**
	 * Obtem todos os dados da requisicao
	 * @return array
	 */
	public function getAllRequestData()
	{
		$this->loadRequestData();
		return $this->requestData;
	}
	
	/**
	 * Envia mensagem de sucesso
	 * @param mixed $data resposta da api
	 */
	public function sendSuccessResponse($data = array())
	{
		$templateData = array("data" => $data);
		$this->renderResponse("api/success.php", $templateData);
	}
	
	/**
	 * Envia mensagem de erro
	 * @param \Exception $e erro em formato de excessao
	 * @param integer $statusCode codigo HTTP da resposta
	 */
	public function sendExceptionResponse(\Exception $e, $statusCode = 404)
	{
		$this->renderResponse("api/error.php", array("exception" => $e), $statusCode);
	}
	
	/**
	 * Faz o render da resposta
	 * @param string $template template da resposta
	 * @param array $data dados para o template
	 * @param integer $status codigo HTTP, padrao 200
	 */
	protected function renderResponse($template, $data = array(), $status = NULL)
	{
		$view = $this->app->view();
// 		$currentTemplatePath = $view->getTemplatesDirectory();
// 		$newTemplatePath = realpath(__DIR__ . "/../view/");
// 		$view->setTemplatesDirectory($newTemplatePath);
		$this->app->render($template, $data, $status);
// 		$view->setTemplatesDirectory($currentTemplatePath);
	}
	
	/**
	 * Carrega os dados da requisicao
	 */
	protected function loadRequestData()
	{
		if (!empty($this->requestData))
			return;
		
		if ($this->app->request->isPost()) {
			$this->requestData = $this->app->request->post();
		} else {
			$this->requestData = $this->app->request->params();
		}
		
		$env = $this->app->environment();
		if (empty($this->requestData) && isset($env['slim.input']))
			$this->requestData = $env['slim.input'];
		if (!is_array($this->requestData))
			$this->requestData = array();
	}
}