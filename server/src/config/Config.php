<?php namespace greatRoom\config;

/**
 * Classe para carregar as configuracoes
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Config {
	/**
	 * @var Config
	 */
	private static $instance;
	/**
	 * @var array
	 */
	private $values;
	
	/**
	 * Construtor
	 */
	private function __construct()
	{
		$file = ($this->isDevelopmentMode()) ? "/config.dev.json" : "/config.prod.json";
		$wherePath = ($this->isRunningLocally()) ? "/local" : "/server";
		$file = $wherePath . $file;
		$path = realpath(__DIR__ . $file); 
		
		$json = file_get_contents($path);
		$this->values = json_decode($json, TRUE);
	}
	
	/**
	 * Singleton
	 * @return \greatRoom\config\Config
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
	 * Obtem uma configuracao pela sua chave
	 * @param string $key chave
	 * @param mixed $default valor que sera retornado caso a chave nao seja encontrada
	 * @return mixed|NULL retorna NULL se a chave nao foi encontrada
	 */
	public function getValueByKey($key, $default = NULL)
	{
		if (isset($this->values[$key]))
			return $this->values[$key];
		else 
			return $default;
	}
	
	/**
	 * Verifica se o codigo esta rodando localmente ou no servidor
	 * @return boolean
	 */
	public function isRunningLocally()
	{
		return !isset($_SERVER['SERVER_SOFTWARE']) || strpos($_SERVER['SERVER_SOFTWARE'],'Google App Engine') === FALSE;
	}
	
	/**
	 * Verifica se o modo e de desenvolvimento
	 * @return boolean
	 */
	public function isDevelopmentMode()
	{
		return getenv("SLIM_MODE") != FALSE && getenv("SLIM_MODE") == "development";
	}
	
	/**
	 * Obtem uma configuracao pela sua chave
	 * @param string $key chave
	 * @param mixed $default valor que sera retornado caso a chave nao seja encontrada
	 * @return mixed|NULL retorna NULL se a chave nao foi encontrada
	 */
	public static function getValue($key, $default = NULL)
	{
		return self::getInstance()->getValueByKey($key, $default);
	}
}
