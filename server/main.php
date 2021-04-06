<?php
require 'vendor/autoload.php';

date_default_timezone_set("America/Fortaleza");
$app = new \Slim\Slim(array(
	'mode' => \greatRoom\helper\Enviroment::getAppMode(),
	'templates.path' => realpath(__DIR__ . "/src/view/"),
	'log.writer' => new \greatRoom\library\DummyLogWriter(),
	'log.enabled' => FALSE
));
$app->setName("greatRoom");

// Permitir chamada AJAX cross-domain
$app->response->headers->set('Access-Control-Allow-Origin', '*');
$app->response->headers->set('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');

// Configura os middlewares
$app->add(new \Slim\Middleware\ContentTypes());

// Configura a sessao
$sessionConfig = \greatRoom\config\Config::getValue("session", array());
$app->add(new \Slim\Middleware\SessionCookie($sessionConfig));

// Cria os controles e consequentemente especifica as rotas
$controllers = array(
	new \greatRoom\controller\Groups(),
	new \greatRoom\controller\Group(),
	new \greatRoom\controller\File(),
);

$app->run();
