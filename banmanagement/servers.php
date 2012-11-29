<?php
if(empty($settings['servers']))
	echo 'No servers found!';
else {
	?>
	<table class="table table-striped table-bordered">
		<caption>Ban Statistics</caption>
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