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

// IE8 frame busting, well thats the only good thing it has :P (Now supported by Firefox woot)
header('X-FRAME-OPTIONS: SAMEORIGIN');

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

if(!function_exists('apc_exists')) {
	function apc_exists($key) { 
		return (bool) apc_fetch($key);
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
	
	if(!mysql_connect($server['host'], $server['username'], $server['password']))
		return false;
	else if(!mysql_select_db($server['database']))
		return false;
	$settings['last_connection'] = $server;
	
	if(isset($settings['utf8']) && $settings['utf8'])
		mysql_query("SET NAMES 'utf8'");
	
	return true;
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

$settings['servers'] = unserialize($settings['servers']);

// Check if APC is enabled to use that instead of file cache
$settings['apc_enabled'] = false;
if(extension_loaded('apc') && ini_get('apc.enabled'))
	$settings['apc_enabled'] = true;

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