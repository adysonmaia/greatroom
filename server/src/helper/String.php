<?php namespace greatRoom\helper;

/**
 * Helper que manipula strings
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class String {

	const ENCODING = "UTF-8";
	
	/**
	 * Converte a string para o formato de titulo
	 * @param string $string
	 * @return string
	 */
	public static function titleCase($string) 
	{
		if (function_exists("mb_convert_case"))
			return mb_convert_case($string, MB_CASE_TITLE, self::ENCODING);
		else
			return ucfirst(strtolower($string));
	}
	
	/**
	 * Converte a string para maiusculo
	 * @param string $string
	 * @return string
	 */
	public static function upperCase($string)
	{
		if (function_exists("mb_convert_case"))
			return mb_convert_case($string, MB_CASE_UPPER, self::ENCODING);
		else
			return strtoupper($string);
	}
	
	/**
	 * Converte a string para minusculo
	 * @param string $string
	 * @return string
	 */
	public static function lowerCase($string)
	{
		if (function_exists("mb_convert_case"))
			return mb_convert_case($string, MB_CASE_LOWER, self::ENCODING);
		else
			return strtolower($string);
	}
	
	/**
	 * Retorna o tamanho de uma string
	 * @param string $string
	 * @return integer
	 */
	public static function length($string)
	{
		if (function_exists("mb_strlen"))
			return mb_strlen($string, self::ENCODING);
		else
			return strlen($string);
	}
	
	/**
	 * Pega uma parte da string
	 * @param string $string
	 * @param integer $start
	 * @param integer $length
	 * @return string
	 */
	public static function substr($string, $start, $length = NULL)
	{
		if (function_exists("mb_substr"))
			return mb_substr($string, $start, $length, self::ENCODING);
		else
			return substr($string, $start, $length);
	}
	
	/**
	 * Remove os acentos
	 *
	 * @param string $text
	 * @return string texto sem acento
	 */
	public static function removeAccent($text) {
		$array1 = array(   "á", "à", "â", "ã", "ä", "é", "è", "ê", "ë", "í", "ì", "î", "ï", "ó", "ò", "ô", "õ", "ö", "ú", "ù", "û", "ü", "ç"
				, "Á", "À", "Â", "Ã", "Ä", "É", "È", "Ê", "Ë", "Í", "Ì", "Î", "Ï", "Ó", "Ò", "Ô", "Õ", "Ö", "Ú", "Ù", "Û", "Ü", "Ç" );
		$array2 = array(   "a", "a", "a", "a", "a", "e", "e", "e", "e", "i", "i", "i", "i", "o", "o", "o", "o", "o", "u", "u", "u", "u", "c"
				, "A", "A", "A", "A", "A", "E", "E", "E", "E", "I", "I", "I", "I", "O", "O", "O", "O", "O", "U", "U", "U", "U", "C" );
		return str_replace($array1, $array2, $text);
	}
}