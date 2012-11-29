<?php
if(!isset($_SESSION['admin']) || (isset($_SESSION['admin']) && !$_SESSION['admin']))
	die('Hacking attempt');
error_reporting(0);

function tableExists($name) {
	if(!@mysql_query("SELECT * FROM $name"))
		return false;
	return true;
}

// Test the mysql connection
if(!mysql_connect($_POST['host'], $_POST['username'], $_POST['password']))
	$error = 'Unable to connect, check connection information is correct';
else if(!mysql_select_db($_POST['database']))
	$error = 'Unable to select database';
else if(!tableExists($_POST['banstable'])) 
	$error = 'Bans table not found';
else if(!tableExists($_POST['recordtable']))
	$error = 'Bans record table not found';
else {
	// Success! Add it
	$servers = $settings['servers'];
	$servers[] = array(
		'name' => $_POST['servername'],
		'host' => $_POST['host'],
		'database' => $_POST['database'],
		'username' => $_POST['username'],
		'password' => $_POST['password'],
		'bansTable' => $_POST['banstable'],
		'recordTable' => $_POST['recordtable'],
		'ipTable' => $_POST['iptable'],
		'ipRecordTable' => $_POST['iprecordtable'],
		'mutesTable' => $_POST['mutestable'],
		'mutesRecordTable' => $_POST['mutesrecordtable'],
		'kicksTable' => $_POST['kickstable']
	);
	$settings['servers'] = $servers;
	$servers = serialize($servers);
	$servers = "['servers'] = '".$servers;
	$contents = file_get_contents('settings.php');
	$contents = preg_replace("/\['servers'\] = '(.*?)/", $servers, $contents);
	file_put_contents('settings.php', $contents);
	$array['success'] = 'true';
}
if(isset($error))
	$array['error'] = $error;
else {
	$array['success'] = array('id' => key(end($settings['servers'])), 'serverName' => $_POST['servername']);
}
echo json_encode($array);
?>