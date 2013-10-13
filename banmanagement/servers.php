<?php
/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
if(empty($settings['servers']))
	echo 'No servers found!';
else {
	?>
	<table class="table table-striped table-bordered">
		<h1 style="font-size: 49px;">Ban Statistics</h1>
		<thead>
			<th>Server</th>
			<th>Current Temporary Bans</th>
			<th>Current Permanent Bans</th>
			<th>Past Bans</th>
		</thead>
		<tbody>
	<?php
	$id = array_keys($settings['servers']);
	$i = 0;
	foreach($settings['servers'] as $server) {
		// Make sure we can connecet
		if(!connect($server)) {
			?><tr><td colspan="3">Unable to connect to database</td></tr><?php
		} else {
			list($currentTempBans) = cache("SELECT COUNT(*) FROM ".$server['bansTable']." WHERE ban_expires_on != 0", 3600, '', $server, $server['name'].'currentTempStats');

			list($currentPermBans) = cache("SELECT COUNT(*) FROM ".$server['bansTable']." WHERE ban_expires_on = 0", 3600, '', $server, $server['name'].'currentPermStats');

			list($pastBans) = cache("SELECT COUNT(*) FROM ".$server['recordTable'], 3600, '', $server, $server['name'].'pastBanStats');
			
			echo '
			<tr>
				<td>'.$server['name'].'</td>
				<td>'.$currentTempBans.'</td>
				<td>'.$currentPermBans.'</td>
				<td>'.$pastBans.'</td>
			</tr>';
		}
		?>
		
		<?php
	}
	?>
		</tbody>
	</table><?php
}
?>