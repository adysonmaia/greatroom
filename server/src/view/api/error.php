<?php
$app = \Slim\Slim::getInstance();
$app->response->headers->set('Content-Type', 'application/json');
$code = 0;
$message = "";
if ($this->has("exception")) {
	$exception = $this->getData("exception");
	if ($exception instanceof \Exception) {
		$code = $exception->getCode();
		$message = $exception->getMessage();
	}
} else {
	$code = $this->getData("code");
	$message = $this->getData("message");
}

$json = array("success" => FALSE, "code" => $code, "message" => $message);
echo json_encode($json);