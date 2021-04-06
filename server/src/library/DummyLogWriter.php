<?php namespace greatRoom\library;

/**
 * Lib para adaptar o sistema de log do slim ao google app engine
 * 
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class DummyLogWriter {
	
	/**
	 * 
	 * @param string $message
	 * @param string $level
	 * @return boolean
	 */
	public function write($message, $level = null) 
	{
		error_log((string) $message);
		return true;
	}
}