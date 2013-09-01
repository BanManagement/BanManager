<?php
/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
session_start();
ob_start();

if(!isset($_SESSION['initiated'])) {
    session_regenerate_id();
    $_SESSION['initiated'] = true;
}

define('IN_PATH', realpath('.') . '/'); // This allows us to use absolute urls

/**
 * Snippet from php.net by bohwaz
 * below function kills register globals
 * to remove any possible security threats if it is on
 */
if(ini_get('register_globals')) {
	function unregister_globals() {
		foreach(func_get_args() as $name) {
			foreach($GLOBALS[$name] as $key => $value) {
				if(isset($GLOBALS[$key]))
					unset($GLOBALS[$key]);
			}
		}
	}
	unregister_globals('_POST', '_GET', '_COOKIE', '_REQUEST', '_SERVER', '_ENV', '_FILES', '_SESSION');
}

// REQUEST_URI fix for hosts using IIS (Windows)
if(!isset($_SERVER['REQUEST_URI'])) {
	$_SERVER['REQUEST_URI'] = $_SERVER['SCRIPT_NAME'];
	if($_SERVER['QUERY_STRING']) {
		$_SERVER['REQUEST_URI'] .= '?' . $_SERVER['QUERY_STRING'];
	}
}

// mysql_real_escape_string that doesn't require an active database connection
function mysql_escape_mimic($inp) {
    if(is_array($inp))
        return array_map(__METHOD__, $inp);

    if(!empty($inp) && is_string($inp))
        return str_replace(array('\\', "\0", "\n", "\r", "'", '"', "\x1a"), array('\\\\', '\\0', '\\n', '\\r', "\\'", '\\"', '\\Z'), $inp);
 
    return $inp;
}

/** 
 * Encodes HTML within below globals, takes into account magic quotes.
 * Note: $_SERVER is not sanitised, be aware of this when using it.
 * Why repeat it twice? Checking magic quotes everytime in a loop is slow and so is any additional if statements ;)
 */
$in = array(&$_GET, &$_POST);
if(get_magic_quotes_gpc()) {
	while(list($k, $v) = each($in)) {
		foreach($v as $key => $val) {
			if(!is_array($val)) 
				$in[$k][mysql_escape_mimic(htmlspecialchars(stripslashes($key), ENT_QUOTES))] = mysql_escape_mimic(htmlspecialchars(stripslashes($val), ENT_QUOTES));
			else
				$in[] =& $in[$k][$key];
		}
	}
} else {
	while(list($k, $v) = each($in)) {
		foreach($v as $key => $val) {
			if(!is_array($val))
				$in[$k][mysql_escape_mimic(htmlspecialchars($key, ENT_QUOTES))] = mysql_escape_mimic(htmlspecialchars($val, ENT_QUOTES));
			else
				$in[] =& $in[$k][$key];
		}
	}
}

if(!function_exists('json_encode')) {
	function json_encode($a = false) {
		/**
     	* This function encodes a PHP array into JSON
		* Function from php.net by Steve
     	* Returns: @JSON
     	*/
    	if(is_null($a))
			return 'null';
    	if($a === false)
			return 'false';
    	if($a === true)
			return 'true';
		if(is_scalar($a)) {
			if(is_float($a))
				return floatval(str_replace(",", ".", strval($a))); // Always use "." for floats.
      		if(is_string($a)) {
				static $jsonReplaces = array(array("\\", "/", "\n", "\t", "\r", "\b", "\f", '"'), array('\\\\', '\\/', '\\n', '\\t', '\\r', '\\b', '\\f', '\"'));
				return '"' . str_replace($jsonReplaces[0], $jsonReplaces[1], $a) . '"';
			} else
				return $a;
		}
		$isList = true;
		for($i = 0, reset($a); $i < count($a); $i++, next($a)) {
			if(key($a) !== $i) {
				$isList = false;
				break;
			}
		}
		$result = array();
		if($isList) {
			foreach ($a as $v)
				$result[] = json_encode($v);
			return '[' . join(',', $result) . ']';
		} else {
			foreach ($a as $k => $v)
				$result[] = json_encode($k).':'.json_encode($v);
			return '{' . join(',', $result) . '}';
		}
	}
}

$apc_status = extension_loaded('apc') && ini_get('apc.enabled');
if($apc_status) {
	if(!function_exists('apc_exists')) {
		if(version_compare(phpversion('apc'), '3.1.4', '<')) {
			function apc_exists($key) { 
				return (bool) apc_fetch($key);
			}
		}
	}
}

function redirect($location, $code = '302') {
	switch($code) {
		case '301';
			header("HTTP/1.1 301 Moved Permanently");
		break;
		case '303';
			header("HTTP/1.1 303 See Other");
		break;
		case '404';
			header('HTTP/1.1 404 Not Found');
		break;
	}
	//remove any &amp; in the url to prevent any problems
	$location = str_replace('&amp;', '&', $location);
	header("Location: $location");
	//kill the script from running and output a link for browsers which enable turning off header redirects *cough Opera cough* :P
	exit('<a href="'.$location.'">If you were not redirected automatically please click here</a>');
}

function errors($message) {
	echo '
		<div id="error" class="alert alert-error">
			<button class="close" data-dismiss="alert">&times;</button>
			<h4 class="alert-heading">Error</h4>
			<ol>';
	if(is_array($message)) {
		foreach($message as $e)
			echo '
				<li>'.$e.'</li>';
	} else {
		echo '
				<li>'.$message.'</li>';
	}
	echo '
			</ol>
		</div>';
}

/*
 * Convert seconds to human readable text.
 * http://csl.sublevel3.org/php-secs-to-human-text/
 */
function secs_to_h($secs) {
	$units = array(
		"week"   => 7*24*3600,
		"day"    =>   24*3600,
		"hour"   =>      3600,
		"minute" =>        60,
		"second" =>         1,
	);

	// specifically handle zero
	if ( $secs == 0 )
		return "0 seconds";
	$s = '';
	foreach ( $units as $name => $divisor ) {
		if ( $quot = intval($secs / $divisor) ) {
			$s .= "$quot $name";
			$s .= (abs($quot) > 1 ? "s" : "") . ", ";
			$secs -= $quot * $divisor;
		}
	}
	return substr($s, 0, -2);
}

function secs_to_hmini($secs) {
	$units = array(
		"w"   => 7*24*3600,
		"d"    =>   24*3600,
		"h"   =>      3600,
		"m" =>        60,
		"s" =>         1,
	);

	// specifically handle zero
	if ( $secs == 0 )
		return "0s";
	$s = '';
	foreach ( $units as $name => $divisor ) {
		if ( $quot = intval($secs / $divisor) ) {
			if($quot > 0) {
				$s .= $quot.$name;
				$s .= (abs($quot) > 1 && $name == 's' ? 's' : ''). ' ';
			}
			$secs -= $quot * $divisor;
		}
	}
	return substr($s, 0, -2);
}

function is_alphanum($string) {
	if(function_exists('ctype_alnum'))
		return ctype_alnum($string);
	else
		return (preg_match("~^[a-z0-9]*$~iD", $string) !== 0 ? true : false);
}

function is_alphanumdash($string) {
	return (preg_match("~^[a-z0-9_-]*$~iD", $string) !== 0 ? true : false);
}

function cache($query, $time, $folder = '', $server = array(), $name = '') {
	global $settings;
	$md5 = md5($query);
	if($folder == '' && empty($name))
		$file = $md5;
	else if($folder != '' && empty($name))
		$file = $folder.'/'.$md5;
	else if($folder != '' && !empty($name))
		$file = $folder.'/'.$name;
	else if($folder == '' && !empty($name))
		$file = $name;
	
	if($settings['apc_enabled']) {
		if(apc_exists($file))
			return apc_fetch($file);
		else {
			return createCache($query, $server, $file, $time);
		}
	} else {
		$file = IN_PATH.'cache/'.$file.'.php';
		if($folder != '' && !is_dir(IN_PATH.'cache/'.$folder))
			mkdir(IN_PATH.'cache/'.$folder, 0777, true);
		if(file_exists($file)) {
			if(time() - filemtime($file) > $time) {
				// Needs recache
				return createCache($query, $server, $file); // Return the fresh data
			} else {
				// Serve the cache
				return unserialize(file_get_contents($file, NULL, NULL, 16));
			}
		} else {
			// Cache needs creating
			return createCache($query, $server, $file); // Return the fresh data
		}
	}
}

function createCache($query, $server, $file, $time = 0) {
	global $settings;

	if(!empty($server)) {
		if(isset($settings['last_connection'])) {
			$diff = array_diff($settings['last_connection'], $server);
			if(!empty($diff))
				connect($server);
		} else
			connect($server);
	}
	$sql = mysql_query($query);
	$data = array();
	if(mysql_num_rows($sql) > 0) {
		while($fetch = mysql_fetch_array($sql)) // Loop through the data
			array_push($data, $fetch);
	}
	// Check if its only one row
	if(count($data) == 1)
		$data = $data[0];
	// Now save it
	if(!$settings['apc_enabled'])
		file_put_contents($file, "<?php die(); ?>\n".serialize($data)); // Create the file
	else
		apc_store($file, $data, $time);
	return $data; // Return the fresh data
}

function rglob($pattern='*', $flags = 0, $path='') {
    $paths = glob($path.'*', GLOB_MARK|GLOB_ONLYDIR|GLOB_NOSORT);
    $files = glob($path.$pattern, $flags);
	if($path !== false && $files !== false) {
		foreach($paths as $path)
			$files = array_merge($files, rglob($pattern, $flags, $path));
	} else
		$files = array();
    return $files;
}

function clearCache($folder = '', $olderThan = 0) {
	global $settings;

	if($settings['apc_enabled']) {
		apc_delete($folder);
		return;
	}
	
	$timeNow = time();
	if(empty($folder))
		$files = rglob('*.php', null, IN_PATH.'cache');
	else
		$files = rglob('*.php', null, IN_PATH.'cache/'.$folder);
	foreach($files as $file) {
		if($olderThan == 0)
			unlink($file);
		else if($timeNow - filemtime($file) > $olderThan) {
			unlink($file);
		}
	}
}

function connect($server) {
	global $settings;
	
	if(!isset($settings['last_connection']) || (isset($settings['last_connection']) && $settings['last_connection']['host'] != $server['host'] && $settings['last_connection']['database'] != $server['database'])) {
		if(!mysql_connect($server['host'], $server['username'], $server['password']))
			return false;
		else if(!mysql_select_db($server['database']))
			return false;
		$settings['last_connection'] = $server;
		
		if(isset($settings['utf8']) && $settings['utf8'])
			mysql_query("SET NAMES 'utf8'");
	}
	
	return true;
}

function searchPlayers($search, $serverID, $server, $sortByCol = 'name', $sortBy = 'ASC', $past = true) {

	switch($sortByCol) {
		default:
		case 0: // Name
			$sort['bans'] = $sort['banrecords'] = 'banned';
			$sort['mutes'] = $sort['muterecords'] = 'muted';
			$sort['kicks'] = 'kicked';
			$sort['warnings'] = 'warned';
		break;
		case 1: // Type
			$sort['bans'] = $sort['banrecords'] = 'banned';
			$sort['mutes'] = $sort['muterecords'] = 'muted';
			$sort['kicks'] = 'kicked';
			$sort['warnings'] = 'warned';
		break;
		case 2: // By
			$sort['bans'] = $sort['banrecords'] = 'banned_by';
			$sort['mutes'] = $sort['muterecords'] = 'muted_by';
			$sort['kicks'] = 'kicked_by';
			$sort['warnings'] = 'warned_by';
		break;
		case 3: // Reason
			$sort['bans'] = $sort['banrecords'] = 'ban_reason';
			$sort['mutes'] = $sort['muterecords'] = 'mute_reason';
			$sort['kicks'] = 'kick_reason';
			$sort['warnings'] = 'warn_reason';
		break;
		case 4: // Expires
			$sort['bans'] = 'ban_expires_on';
			$sort['banrecords'] = 'ban_expired_on';
			$sort['mutes'] = 'mute_expires_on';
			$sort['muterecords'] = 'mute_expired_on';
			$sort['kicks'] = 'kick_id';
			$sort['warnings'] = 'warn_id';
		break;
		case 5: // Date
			$sort['bans'] = $sort['banrecords'] = 'ban_time';
			$sort['mutes'] = $sort['muterecords'] = 'mute_time';
			$sort['kicks'] = 'kick_time';
			$sort['warnings'] = 'warn_time';
		break;
	}

	// Found results
	$found = array();

	if((isset($settings['player_current_ban']) && $settings['player_current_ban']) || !isset($settings['player_current_ban'])) {
		// Current Bans
		$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expires_on FROM ".$server['bansTable']." WHERE banned LIKE '%".$search."%' ORDER BY ".$sort['bans']." $sortBy", 300, $serverID.'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		
		if(count($result) > 0) {
			foreach($result as $r)
				$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expires_on']);
		}
	}
	
	if((isset($settings['player_previous_bans']) && $settings['player_previous_bans']) || !isset($settings['player_previous_bans'])) {
		if($past) {
			// Past Bans
			$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_on FROM ".$server['recordTable']." WHERE banned LIKE '%".$search."%' ORDER BY ".$sort['banrecords']." $sortBy", 300, $serverID.'/search', $server);
			if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
				$result = array($result);
			
			if(count($result) > 0) {
				foreach($result as $r) {
					if(!isset($found[$r['banned']]))
						$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expired_on'], 'past' => true);
					else if($found[$r['banned']]['time'] < $r['ban_time'])
						$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expired_on'], 'past' => true);
				}
			}
		}
	}
	
	if((isset($settings['player_current_mute']) && $settings['player_current_mute']) || !isset($settings['player_current_mute'])) {
		// Current Mutes
		$result = cache("SELECT muted, muted_by, mute_reason, mute_time, mute_expires_on FROM ".$server['mutesTable']." WHERE muted LIKE '%".$search."%' ORDER BY ".$sort['mutes']." $sortBy", 300, $serverID.'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		
		if(count($result) > 0) {
			foreach($result as $r) {
				if(!isset($found[$r['muted']]))
					$found[$r['muted']] = array('by' => $r['muted_by'], 'reason' => $r['mute_reason'], 'type' => 'Mute', 'time' => $r['mute_time'], 'expires' => $r['mute_expires_on']);
			}
		}
	}
	
	if($past) {
		if((isset($settings['player_previous_mutes']) && $settings['player_previous_mutes']) || !isset($settings['player_previous_mutes'])) {
			// Past Mutes
			$result = cache("SELECT muted, muted_by, mute_reason, mute_time, mute_expired_on FROM ".$server['mutesRecordTable']." WHERE muted LIKE '%".$search."%' ORDER BY ".$sort['muterecords']." $sortBy", 300, $serverID.'/search', $server);
			if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
				$result = array($result);
			
			if(count($result) > 0) {
				foreach($result as $r) {
					if(!isset($found[$r['muted']]))
						$found[$r['muted']] = array('by' => $r['muted_by'], 'reason' => $r['mute_reason'], 'type' => 'Mute', 'time' => $r['mute_time'], 'expires' => $r['mute_expired_on'], 'past' => true);
					else if($found[$r['muted']]['time'] < $r['mute_time'])
						$found[$r['muted']] = array('by' => $r['muted_by'], 'reason' => $r['mute_reason'], 'type' => 'Mute', 'time' => $r['mute_time'], 'expires' => $r['mute_expired_on'], 'past' => true);
				}
			}
		}

		if((isset($settings['player_kicks']) && $settings['player_kicks']) || !isset($settings['player_kicks'])) {		
			// Kicks
			$result = cache("SELECT kicked, kicked_by, kick_reason, kick_time FROM ".$server['kicksTable']." WHERE kicked LIKE '%".$search."%' ORDER BY ".$sort['kicks']." $sortBy", 300, $serverID.'/search', $server);
			if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
				$result = array($result);
				
			if(count($result) > 0) {
				foreach($result as $r) {
					if(!isset($found[$r['kicked']]))
						$found[$r['kicked']] = array('by' => $r['kicked_by'], 'reason' => $r['kick_reason'], 'type' => 'Kick', 'time' => $r['kick_time'], 'expires' => 0, 'past' => true);
					else if($found[$r['kicked']]['time'] < $r['kick_time'])
						$found[$r['kicked']] = array('by' => $r['kicked_by'], 'reason' => $r['kick_reason'], 'type' => 'Kick', 'time' => $r['kick_time'], 'expires' => 0, 'past' => true);
				}
			}
		}
	}
	
	if((isset($settings['player_warnings']) && $settings['player_warnings']) || !isset($settings['player_warnings'])) {
		// Warnings
		$result = cache("SELECT warned, warned_by, warn_reason, warn_time FROM ".$server['warningsTable']." WHERE warned LIKE '%".$search."%' ORDER BY ".$sort['warnings']." $sortBy", 300, $serverID.'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		
		if(count($result) > 0) {
			foreach($result as $r) {
				if(!isset($found[$r['warned']]))
					$found[$r['warned']] = array('by' => $r['warned_by'], 'reason' => $r['warn_reason'], 'type' => 'Warning', 'time' => $r['warn_time'], 'expires' => 0, 'past' => true);
				else if($found[$r['warned']]['time'] < $r['warn_time'])
					$found[$r['warned']] = array('by' => $r['warned_by'], 'reason' => $r['warn_reason'], 'type' => 'Warning', 'time' => $r['warn_time'], 'expires' => 0, 'past' => true);
			}
		}
	}
	
	if(count($found) == 0)
		return false;
	else if(count($found) == 1) {
		// Redirect!
		$p = array_keys($found);
		redirect('index.php?action=viewplayer&player='.$p[0].'&server='.$serverID);
	} else {
		// STUFF
		return $found;
	}
}

function searchIps($search, $serverID, $server, $sortByCol = 'name', $sortBy = 'ASC', $past = true) {
	$found = array();

	switch($sortByCol) {
		default:
		case 0: // Name
			$sort['bans'] = $sort['banrecords'] = 'banned';
		break;
		case 1: // Type
			$sortByType = true;
			$sort['bans'] = $sort['banrecords'] = 'banned';
		break;
		case 2: // By
			$sort['bans'] = $sort['banrecords'] = 'banned_by';
		break;
		case 3: // Reason
			$sort['bans'] = $sort['banrecords'] = 'ban_reason';
		break;
		case 4: // Expires
			$sort['bans'] = 'ban_expires_on';
			$sort['banrecords'] = 'ban_expired_on';
		break;
		case 5: // Date
			$sort['bans'] = $sort['banrecords'] = 'ban_time';
		break;
	}

	// Found results
	$found = array();

	// Current Bans
	$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expires_on FROM ".$server['ipTable']." WHERE banned LIKE '%".$search."%' ORDER BY ".$sort['bans']." $sortBy", 300, $serverID.'/search', $server);
	if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
		$result = array($result);
	
	if(count($result) > 0) {
		foreach($result as $r)
			$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expires_on']);
	}
	
	if($past) {
		// Past Bans
		$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_on FROM ".$server['ipRecordTable']." WHERE banned LIKE '%".$search."%' ORDER BY ".$sort['banrecords']." $sortBy", 300, $serverID.'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		
		if(count($result) > 0) {
			foreach($result as $r) {
				if(!isset($found[$r['banned']]))
					$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expired_on'], 'past' => true);
				else if($found[$r['banned']]['time'] < $r['ban_time'])
					$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expired_on'], 'past' => true);
			}
		}
	}
	
	if(count($found) == 0)
		return false;
	else if(count($found) == 1) {
		// Redirect!
		$p = array_keys($found);
		redirect('index.php?action=viewip&ip='.$p[0].'&server='.$serverID);
	} else {
		// STUFF
		return $found;
	}
	
	if(count($found) == 0)
		return false;
	else if(count($found) == 1) {
		// Redirect!
		$p = array_keys($found);
		redirect('index.php?action=viewip&ip='.$p[0].'&server='.$serverID);
	} else {
		// STUFF
		return $found;
	}
}

$actions = array(
	'addserver',
	'admin',
	'deleteban',
	'deletebanrecord',
	'deletecache',
	'deleteipban',
	'deleteipbanrecord',
	'deletekickrecord',
	'deletemute',
	'deletemuterecord',
	'deleteserver',
	'deletewarning',
	'editserver',
	'logout',
	'reorderserver',
	'searchplayer',
	'searchip',
	'servers',
	'updateban',
	'updateipban',
	'updatemute',
	'updatesettings',
	'viewip',
	'viewplayer'
);
if(file_exists('settings.php'))
	include('settings.php');
else
	errors('You must rename settingsRename.php to settings.php');

// IE8 frame busting, well thats the only good thing it has :P (Now supported by Firefox woot)
if((isset($settings['iframe_protection']) && $settings['iframe_protection']) || !isset($settings['iframe_protection']))
	header('X-FRAME-OPTIONS: SAMEORIGIN');
	
$settings['servers'] = unserialize($settings['servers']);

// Check if APC is enabled to use that instead of file cache
$settings['apc_enabled'] = $apc_status;

if(!isset($_GET['ajax']) || (isset($_GET['ajax']) && !$_GET['ajax']))
	include('header.php');

if(isset($_GET['action']) && in_array($_GET['action'], $actions))
	include($_GET['action'].'.php');
else if(!isset($_GET['action']))
	include('home.php');
else
	echo 'Action not found, possible hacking attempt';
if(!isset($_GET['ajax']) || (isset($_GET['ajax']) && !$_GET['ajax']))
	include('footer.php');
?>