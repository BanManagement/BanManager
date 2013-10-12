<?php
/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/

// Disable errors to prevent invalid JSON
error_reporting(0);
@ini_set('display_errors', 0); // Fallback incase error_reporting(0) fails

if(!isset($_SESSION['admin']) || (isset($_SESSION['admin']) && !$_SESSION['admin']))
	die('Hacking attempt');
else if(!isset($_GET['authid']) || (isset($_GET['authid']) && $_GET['authid'] != sha1($settings['password'])))
	die('Hacking attempt');
else if(!isset($_POST['type']))
	die('Hacking attempt');

if($_POST['type'] == 'mainsettings') {
	// Validate the data!
	$latest_bans = 'false';
	$latest_mutes = 'false';
	$latest_warnings = 'false';
	$utf8 = 'false';
	$iframe_protection = 'false';
	$submit_buttons_before_html = '';
	$submit_buttons_after_html = '';
	
	if(isset($_POST['latestbans']))
		$latest_bans = 'true';
	if(isset($_POST['latestmutes']))
		$latest_mutes = 'true';
	if(isset($_POST['latestwarnings']))
		$latest_warnings = 'true';
	if(isset($_POST['utf8']))
		$utf8 = 'true';
	if(isset($_POST['iframe']))
		$iframe_protection = 'true';
	if(isset($_POST['buttons_before']))
		$submit_buttons_before_html = $_POST['buttons_before'];
	if(isset($_POST['buttons_after']))
		$submit_buttons_after_html = $_POST['buttons_after'];

	$footer = htmlspecialchars_decode($_POST['footer'], ENT_QUOTES);

	$variables = array('latest_bans', 'latest_mutes', 'latest_warnings', 'utf8', 'footer', 'iframe_protection', 'submit_buttons_before_html', 'submit_buttons_after_html');
	
} else if($_POST['type'] == 'viewplayer') {
	// Validate the data!
	$player_current_ban = 'false';
	$player_current_mute = 'false';
	$player_previous_bans = 'false';
	$player_previous_mutes = 'false';
	$player_kicks = 'false';
	$player_warnings = 'false';
	$player_current_ban_extra_html = '';
	
	if(isset($_POST['ban']))
		$player_current_ban = 'true';
	if(isset($_POST['mute']))
		$player_current_mute = 'true';
	if(isset($_POST['prevbans']))
		$player_previous_bans = 'true';
	if(isset($_POST['prevmutes']))
		$player_previous_mutes = 'true';
	if(isset($_POST['warnings']))
		$player_kicks = 'true';
	if(isset($_POST['kicks']))
		$player_warnings = 'true';
	if(isset($_POST['banextra']))
		$player_current_ban_extra_html = $_POST['banextra'];
	if(isset($_POST['muteextra']))
		$player_current_mute_extra_html = $_POST['muteextra'];

	$variables = array('player_current_ban', 'player_current_ban_extra_html', 'player_current_mute', 'player_current_mute_extra_html', 'player_previous_bans', 'player_previous_mutes', 'player_kicks', 'player_warnings');
}

if(!isset($variables))
	die('Could be a hacking attempt.');

$contents = file_get_contents('settings.php');

foreach($variables as $var) {
	if($$var == 'true' || $$var == 'false')
		$$var = "['".$var."'] = ".$$var.";".PHP_EOL;
	else
		$$var = "['".$var."'] = '".$$var."';".PHP_EOL;

	$contents = preg_replace("/\['".$var."'\] = (.*?)".PHP_EOL."/", $$var, $contents, -1, $count);
	if($count == 0)
		$contents = str_replace("<?php".PHP_EOL, "<?php".PHP_EOL."$"."settings".$$var, $contents);
}

file_put_contents('settings.php', $contents);

$array['success'] = 'true';

if(isset($error))
	$array['error'] = $error;

echo json_encode($array);
?>