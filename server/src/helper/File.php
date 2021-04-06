<?php namespace greatRoom\helper;

/**
 * Helper que manipula arquivos
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class File {
	
	/**
	 * Cria um novo de arquivo apropriado
	 * @param string $fileName
	 * @return string 
	 */
	public static function createFileName($fileName)
	{
		$fileName = \greatRoom\helper\String::removeAccent($fileName);
		$fileName = preg_replace("/[^0-9a-zA-Z_\-\.]/", "", $fileName);
		return $fileName;
	}
}