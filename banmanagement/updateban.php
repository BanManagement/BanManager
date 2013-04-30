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
else if(!isset($_POST['server']) || !is_numeric($_POST['server']))
	die('Hacking attempt');
else if(!isset($settings['servers'][$_POST['server']]))
	die('Hacking attempt');
else if(!isset($_POST['id']) || !is_numeric($_POST['id']))
	die('Hacking attempt');
else {

	// Validate the timestamp
	if(isset($_POST['expires'])) {
		if(!is_numeric($_POST['expiresTimestamp']))
			$error = 'Invalid timestamp data';
		else
			$timestamp = $_POST['expiresTimestamp'];
	} else
		$timestamp = 0;
		
	if(!isset($_POST['reason']))
		$_POST['reason'] = '';
	
	if(!isset($error)) {
		// Get the server details
		$server = $settings['servers'][$_POST['server']];
		
		if(!connect($server))
			$error = 'Unable to connect to database';
		else {
			$currentBan = mysql_query("SELECT ban_id FROM ".$server['bansTable']." WHERE ban_id = '".$_POST['id']."'");
			
			if(mysql_num_rows($currentBan) == 0)
				$error = 'That ban does not exist';
			else {
				mysql_query("UPDATE ".$server['bansTable']." SET ban_reason = '".$_POST['reason']."',  ban_expires_on = '$timestamp' WHERE ban_id = '".$_POST['id']."'");
				
				// Clear the cache
				clearCache($_POST['server'].'/players');
				
				$array['success'] = 'true';
			}
		}
	}
}
if(isset($error))
	$array['error'] = $error;
echo json_encode($array);
?>