<?php namespace greatRoom\model;

/**
 * Classe para gerenciar a conexao com o banco de dados
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Connection {
	/**
	 * @var Connection
	 */
	private static $instance;
	/**
	 * @var \PDO
	 */
	private $pdo;
	
	/**
	 * Construtor
	 */
	private function __construct()
	{
		$config = \greatRoom\config\Config::getValue('database');
		$hostParamKey = ($config["unix_socket"]) ? "unix_socket" : "host";
		$dsn = "mysql:$hostParamKey=".$config["host"].";dbname=".$config["dbname"];
		$this->pdo = new \PDO($dsn, $config['username'], $config['password'], 
				array(\PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));
	}
	
	/**
	 * Singleton
	 * @return \greatRoom\model\Connection
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
	 * Obtem a conexao com o banco de dados
	 * @return \PDO
	 */
	public function gePDO()
	{
		return $this->pdo;
	}
	
	/**
	 * Obtem a conexao com o banco de dados
	 * @return \PDO
	 */
	public static function getConn()
	{
		return self::getInstance()->gePDO();
	}
}