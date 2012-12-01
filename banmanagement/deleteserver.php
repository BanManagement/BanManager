<?php
/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
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