<?php namespace greatRoom\model;

/**
 * Classe raiz dos modelos do banco de dados
 * @author Adyson Magalhaes Maia <adyson.maia@gmail.com>
 *
 */
class Model {
	
	const ERROR_STR_SAVE = "Erro ao salvar os dados";
	const ERROR_STR_DELETE = "Erro ao apagar os dados";
	
	/**
	 * @var \PDO
	 */
	protected $pdo;
	
	/**
	 * Construtor
	 */
	public function __construct()
	{
		$this->pdo = Connection::getConn();
	}
	
	/**
	 * Retorna a conexao com o banco de dados
	 * @return \PDO
	 */
	protected function getConn() 
	{
		return $this->pdo;	
	}
	
	/**
	 * Faz a consulta e retorna apenas um dado da resposta
	 * @param \PDOStatement $stmt
	 * @return \stdClass|NULL dado ou NULL caso sem resultado
	 */
	protected function requestSingleObj(\PDOStatement $stmt)
	{
		$stmt->execute();
		if ($stmt->rowCount() <= 0)
			return NULL;
		$obj = $stmt->fetch(\PDO::FETCH_OBJ);
		if (empty($obj))
			return NULL;
		return $obj;
	}
	
	/**
	 * Faz a consulta e retorna lista de dados
	 * @param \PDOStatement $stmt
	 * @return array
	 */
	protected function requestMultipleObjs(\PDOStatement $stmt)
	{
		$stmt->execute();
		if ($stmt->rowCount() <= 0)
			return array();
		return $stmt->fetchAll(\PDO::FETCH_OBJ);
	}
	
	/**
	 * Insere os dados em tabela
	 * @param string $table nome da tabela
	 * @param array $data mape dos dados a serem inseridos
	 * @throws \Exception
	 * @return integer chave dos novos dados inseridos
	 */
	protected function insert($table, $data)
	{
		$fields = array_keys($data);
		$fieldsStr = array_map(function ($field) {return "`$field`";}, $fields);
		$fieldsStr = implode(", ", $fieldsStr);
		
		$binds = array_map(function ($field) {return ":".$field;}, $fields);
		$bindsStr = implode(", ", $binds);
		$sql = "INSERT INTO `$table` ($fieldsStr) VALUES ($bindsStr)";
		
		$stmt = $this->pdo->prepare($sql);
		foreach ($data as $field => $value) {
			$this->bindValue($stmt, ":$field", $value);
		}
		$this->executeStatementWithErrorException($stmt);
			
		return (int) $this->pdo->lastInsertId();
	}
	
	/**
	 * Faz a atualizacao de dados em uma tabela
	 * @param string $table
	 * @param array $data mapeamento com os dados a serem autlizados
	 * @param array $where mapeamento para a identificacao dos dados a serem atualizados
	 * @throws \Exception
	 */
	protected function update($table, $data, $where=array())
	{
		$fields = array_keys($data);
		$binds = array_map(function ($field) {return "`$field` = :$field";}, $fields);
		$bindsStr = implode(", ", $binds);
		
		$whereCount = count($where);
		if ($whereCount > 0) {
			$whereFields = array_keys($where);
			$whereStr = array_map(function ($field) {return "`$field` = :{$field}_where";}, $whereFields);
			$whereStr = implode(" AND ", $whereStr);
			$sql = "UPDATE `$table` SET $bindsStr WHERE $whereStr";
		} else {
			$sql = "UPDATE `$table` SET $bindsStr WHERE 1";
		}
		
		$stmt = $this->pdo->prepare($sql);
		
		foreach ($where as $field => $value) {
			$this->bindValue($stmt, ":{$field}_where", $value);
		}
		
		foreach ($data as $field => $value) {
			$this->bindValue($stmt, ":$field", $value);
		}
		$this->executeStatementWithErrorException($stmt);
	}
	
	/**
	 * Faz o bind
	 * @param \PDOStatement $stmt
	 * @param string $bindParam
	 * @param mixed $value
	 */
	protected function bindValue(\PDOStatement &$stmt, $bindParam, $value)
	{
		if (is_null($value))
			$stmt->bindValue($bindParam, NULL, \PDO::PARAM_NULL);
		else if (is_int($value))
			$stmt->bindValue($bindParam, (int) $value, \PDO::PARAM_INT);
		else
			$stmt->bindValue($bindParam, $value);
	}
	
	/**
	 * Executa uma consulta
	 * @param \PDOStatement $stmt
	 * @throws \Exception
	 */
	private function executeStatementWithErrorException(\PDOStatement &$stmt)
	{
		if (!$stmt->execute()) {
			$errorStr = self::ERROR_STR_SAVE;
			if (\greatRoom\config\Config::getInstance()->isDevelopmentMode()) {
				$errorInfo = $stmt->errorInfo();
				if (count($errorInfo) >= 3) {
					$errorStr = $errorStr . " : " . $errorInfo[2];
				}
			}
			throw new \Exception($errorStr, (int) $stmt->errorCode());
		}
	}
}