<?php
$app = \Slim\Slim::getInstance();
$app->response->headers->set('Content-Type', 'application/json');
$data = $this->getData("data");
$json = array("success" => TRUE, "response" => $data);
echo json_encode($json);