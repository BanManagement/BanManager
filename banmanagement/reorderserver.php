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
else if(!isset($_GET['server']) || !is_numeric($_GET['server']))
	die('Hacking attempt');
else if(!isset($settings['servers'][$_GET['server']]))
	die('Hacking attempt');
else if(!isset($_GET['order']) || (isset($_GET['order']) && $_GET['order'] != 'up' && $_GET['order'] != 'down'))
	die('Hacking attempt');
else {
	// Get the server details
	$servers = $settings['servers'];
	
	if($_GET['order'] == 'up') {
		$servers[$_GET['server']] = $servers[$_GET['server'] - 1];
		$servers[$_GET['server'] - 1] = $settings['servers'][$_GET['server']];
	} else {
		$servers[$_GET['server']] = $servers[$_GET['server'] + 1];
		$servers[$_GET['server'] + 1] = $settings['servers'][$_GET['server']];
	}
	
	// Success! Add it
	$settings['servers'] = $servers;
	$servers = serialize($servers);
	$servers = "['servers'] = '".$servers;
	$contents = file_get_contents('settings.php');
	$contents = preg_replace("/\['servers'\] = '(.*?)/", $servers, $contents);
	file_put_contents('settings.php', $contents);
}
if(isset($error))
	$array['error'] = $error;
else {
	$id = array_keys($settings['servers']);
	$i = 0;
	$count = count($settings['servers']) - 1;
	$table = '
	<table class="table table-striped table-bordered" id="servers">
		<thead>
			<th>Server Name</th>
			<th>Options</th>
		</thead>
		<tbody>';
	foreach($settings['servers'] as $server) {
		$table .= '
			<tr>
				<td>'.$server['name'].'</td>
				<td>
					<a href="#" class="btn btn-danger deleteServer" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-trash"></span></a>';
		if($count > 0) {
			if($i == 0)
				$table .= '
				<a href="#" class="btn reorderServer" data-order="down" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-down"></span></a>';
			else if($i == $count)
				$table .= '
				<a href="#" class="btn reorderServer" data-order="up" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-up"></span></a>';
			else {
				$table .= '
				<a href="#" class="btn reorderServer" data-order="up" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-up"></span></a>
				<a href="#" class="btn reorderServer" data-order="down" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-down"></span></a>';
			}
		}
		$table .= '
				</td>
			</tr>';
		++$i;
	}
	$table .= '
		</tbody>
	</table>';
	$array['success'] = array('table' => $table);
}
echo json_encode($array);
?>