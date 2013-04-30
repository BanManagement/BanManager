<?php
/*  muteManagement © 2012, a web interface for the Bukkit plugin muteManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/mute-Management/master/mutemanagement/licence.txt
*/
if(!isset($_SESSION['admin']) || (isset($_SESSION['admin']) && !$_SESSION['admin']))
	die('Hacking attempt');
else if(!isset($_GET['authid']) || (isset($_GET['authid']) && $_GET['authid'] != sha1($settings['password'])))
	die('Hacking attempt');
else if(!isset($_GET['server']) || !is_numeric($_GET['server']))
	die('Hacking attempt');
else if(!isset($settings['servers'][$_GET['server']]))
	die('Hacking attempt');
else if(!isset($_GET['id']) || !is_numeric($_GET['id']))
	die('Hacking attempt');
else {
	// Get the server details
	$server = $settings['servers'][$_GET['server']];
	
	if(!connect($server))
		$error = 'Unable to connect to database';
	else {
		$currentMute = mysql_query("SELECT mute_id FROM ".$server['mutesTable']." WHERE mute_id = '".$_GET['id']."'");
		
		if(mysql_num_rows($currentMute) == 0)
			$error = 'That record does not exist';
		else {
			
			mysql_query("INSERT INTO ".$server['mutesRecordTable']." (muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time, server) SELECT b.muted, b.muted_by, b.mute_reason, b.mute_time, b.mute_expires_on, \"Web\", UNIX_TIMESTAMP(now()), b.server FROM ".$server['mutesTable']." b WHERE b.mute_id = '".$_GET['id']."'");
			// Now delete it
			mysql_query("DELETE FROM ".$server['mutesTable']." WHERE mute_id = '".$_GET['id']."'");
			
			// Clear the cache
			clearCache($_GET['server'].'/players');
			
			$array['success'] = 'true';
		}
	}
}
if(isset($error))
	$array['error'] = $error;
echo json_encode($array);
?>