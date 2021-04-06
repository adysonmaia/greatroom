<?php namespace greatRoom\helper;

/**
 * Helper que manipula path
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
Class Path {

	/**
	 * Junta varios caminhos
	 * @param string $paths,... ilimitados caminhos
	 * @return string
	 */
	public static function join($paths)
	{
		$paths = func_get_args();
		$size = count($paths);
		if ($size == 0 ) { 
			return "";
		} else if ($size == 1)  {
			return rtrim(trim($paths[0]), "/");
		} else {
			$join = "";
			for ($i=0; $i < $size; $i++) {
				$path1 = trim($paths[$i]);
				$path1 = rtrim($path1,"/");
				if ($i > 0)
					$path1 = ltrim($path1,"/");
				$join .= $path1 . "/";
			}
			return rtrim($join, "/");
		}
	}
	
	/**
	 * Junta varios caminhos para formar uma url
	 * @param string $paths,... ilimitados caminhos
	 * @return string
	 */
	public static function joinUrl($paths)
	{
		$paths = func_get_args();
		$size  = count($paths);
		if ($size == 0 ) {
			return "";
		} else if ($size == 1)  {
			return rtrim(trim($paths[0]), "/");
		} else {
			$join = "";
			for ($i=0; $i < $size; $i++) {
				$path1 = trim($paths[$i]);
				$path1 = trim($path1,"/");
				$join .= $path1 . "/";
			}
			return rtrim($join, "/");
		}
	}
}