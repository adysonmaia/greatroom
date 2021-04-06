<?php namespace greatRoom\controller;

/**
 * Classe raiz dos controles
 * 
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Controller {
	
	/**
	 * @var \Slim\Slim
	 */
	protected $app;
	/**
	 * @var \greatRoom\library\Api
	 */
	protected $api;
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		$this->app = \Slim\Slim::getInstance();
		$this->api = \greatRoom\library\Api::getInstance();
	}	
}
