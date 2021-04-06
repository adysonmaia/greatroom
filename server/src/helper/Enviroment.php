<?php namespace greatRoom\helper;

/**
 * Helper que manipula as variaveis de ambiente
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class Enviroment {
	
	/**
	 * Retorna o modo da aplicacao especificao no ambiente
	 * @return string production|development
	 */
	public static function getAppMode()
	{
		$mode = "production";
		if (getenv("SLIM_MODE") != FALSE)
			$mode = getenv("SLIM_MODE");
		return $mode;
	}
	
}