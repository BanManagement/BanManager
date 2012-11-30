<?php
if(!isset($_SESSION['admin']) || (isset($_SESSION['admin']) && !$_SESSION['admin']))
	die('Hacking attempt');
else if(!isset($_GET['authid']) || (isset($_GET['authid']) && $_GET['authid'] != sha1($settings['password'])))
	die('Hacking attempt');
else if(!isset($_GET['id']) || !is_numeric($_GET['id']))
	die('Hacking attempt');
else if(!isset($settings['servers'][$_GET['id']]))
	die('Hacking attempt');
else {
	// Success! Remove it
	$servers = $settings['servers'];
	unset($servers[$_GET['id']]);
	
	$servers = serialize($servers);
	
	$servers = "['servers'] = '".$servers;
	
	$contents = file_get_contents('settings.php');
	$contents = preg_replace("/\['servers'\] = '(.*?)/", $servers, $contents);
	
	file_put_contents('settings.php', $contents);
	
	$array['success'] = 'true';
}
if(isset($error))
	$array['error'] = $error;
echo json_encode($array);
?>