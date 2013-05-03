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
else if(!isset($_POST['footer']))
	die('Hacking attempt');

// Validate the data!
$latestBans = 'false';
$latestMutes = 'false';
$latestWarnings = 'false';
$utf8 = 'false';
if(isset($_POST['latestbans']))
	$latestBans = 'true';
if(isset($_POST['latestmutes']))
	$latestMutes = 'true';
if(isset($_POST['latestwarnings']))
	$latestWarnings = 'true';
if(isset($_POST['utf8']))
	$utf8 = 'true';
	
$footer = htmlspecialchars_decode($_POST['footer'], ENT_QUOTES);

// Save it
$latest_bans = "['latest_bans'] = ".$latestBans.";".PHP_EOL;
$latest_mutes = "['latest_mutes'] = ".$latestMutes.";".PHP_EOL;
$latest_warnings = "['latest_warnings'] = ".$latestWarnings.";".PHP_EOL;
$utf8 = "['utf8'] = ".$utf8.";".PHP_EOL;
$footer = "['footer'] = '".$footer."';".PHP_EOL;

$contents = file_get_contents('settings.php');

// Latest bans
$contents = preg_replace("/\['latest_bans'\] = (.*?)".PHP_EOL."/", $latest_bans, $contents, -1, $count);
if($count == 0)
	$contents = str_replace("<?php".PHP_EOL, "<?php".PHP_EOL."$"."settings$latest_bans", $contents);
	
// Latest mutes
$contents = preg_replace("/\['latest_mutes'\] = (.*?)".PHP_EOL."/", $latest_mutes, $contents, -1, $count);
if($count == 0)
	$contents = str_replace("<?php".PHP_EOL, "<?php".PHP_EOL."$"."settings$latest_mutes", $contents);

// Latest warnings
$contents = preg_replace("/\['latest_warnings'\] = (.*?)".PHP_EOL."/", $latest_warnings, $contents, -1, $count);
if($count == 0)
	$contents = str_replace("<?php".PHP_EOL, "<?php".PHP_EOL."$"."settings$latest_warnings", $contents);
	
// UTF8
$contents = preg_replace("/\['utf8'\] = (.*?)".PHP_EOL."/", $utf8, $contents, -1, $count);
if($count == 0)
	$contents = str_replace("<?php".PHP_EOL, "<?php".PHP_EOL."$"."settings$utf8", $contents);
	
// Footer
$contents = preg_replace("/\['footer'\] = (.*?)".PHP_EOL."/", $footer, $contents, -1, $count);
if($count == 0)
	$contents = str_replace("<?php".PHP_EOL, "<?php".PHP_EOL."$"."settings$footer", $contents);

file_put_contents('settings.php', $contents);

$array['success'] = 'true';

if(isset($error))
	$array['error'] = $error;

echo json_encode($array);
?>