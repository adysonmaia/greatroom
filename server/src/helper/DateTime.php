<?php namespace greatRoom\helper;

/**
 * Helper que manipula datas
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class DateTime {
	
	/**
	 * Converte uma data para ser usado no json
	 * @param string $date
	 * @return string
	 */
	public static function jsonFormat($date) 
	{
		if (empty($date))
			return NULL;
// 		return date("c", strtotime($date));
		return date("r", strtotime($date));
	}
	
	/**
	 * Converte uma data para o formato salvo no banco de dados
	 * @param string $date
	 * @return string
	 */
	public static function dbFormat($date)
	{ 
		$date = str_replace("\"", "", $date);
		$date = str_replace("'", "", $date);
		return date("Y-m-d H:i:s", strtotime($date));
	}
}