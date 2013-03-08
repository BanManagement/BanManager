<?php
/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
if(!isset($_GET['server']) || !is_numeric($_GET['server']))
	redirect('index.php');
else if(!isset($settings['servers'][$_GET['server']]))
	redirect('index.php');
else if(!isset($_GET['ip']) || empty($_GET['ip']))
	redirect('index.php');
else if(!filter_var($_GET['ip'], FILTER_VALIDATE_IP))
	redirect('index.php');
else {
	// Get the server details
	$server = $settings['servers'][$_GET['server']];

	// Clear old ip
	clearCache($_GET['server'].'/ips', 300);
	
	// Check if they are logged in as an admin
	if(isset($_SESSION['admin']) && $_SESSION['admin'])
		$admin = true;
	else
		$admin = false;
	
	// Check if the player exists
	$currentBans = cache("SELECT * FROM ".$server['ipTable']." WHERE banned = '".$_GET['ip']."'", 300, $_GET['server'].'/ips', $server);
	$pastBans = cache("SELECT * FROM ".$server['ipRecordTable']." WHERE banned = '".$_GET['ip']."'", 300, $_GET['server'].'/ips', $server);
	if(count($currentBans) == 0 && count($pastBans) == 0) {
		errors('IP does not exist');
		?><a href="index.php" class="btn btn-primary">New Search</a><?php
	} else {
		// They have been banned, naughty!
		// Now check the time differences!
		$timeDiff = cache('SELECT ('.time().' - UNIX_TIMESTAMP(now()))/3600 AS mysqlTime', 5, $_GET['server'], $server); // Cache it for a few seconds
		
		$mysqlTime = $timeDiff['mysqlTime'];
		$mysqlTime = ($mysqlTime > 0)  ? floor($mysqlTime) : ceil ($mysqlTime);
		$mysqlSecs = ($mysqlTime * 60) * 60;
		?>
		<div class="hero-unit">
			<h2><?php echo $_GET['ip']; ?></h2>
			<h3>Server: <?php echo $server['name']; ?></h3>
		<?php
		$id = array_keys($settings['servers']);
		$i = 0;
		$html = '';
		if(count($settings['servers']) > 1) {
			echo '
			<h5>Change Server: ';
			foreach($settings['servers'] as $serv) {
				if($serv['name'] != $server['name']) {
					$html .= '<a href="index.php?action=viewplayer&player='.$_GET['player'].'&server='.$id[$i].'">'.$serv['name'].'</a>, ';
				}
				++$i;
			}
			echo substr($html, 0, -2).'
			</h5>';
		}
			?>
			<br />
			<table class="table table-striped table-bordered">
				<caption>Current Ban</caption>
				<tbody>
				<?php
		if(count($currentBans) == 0) {
			echo '
					<tr>
						<td colspan="2">None</td>
					</tr>';
		} else {
			$reason = str_replace(array('&quot;', '"'), array('&#039;', '\''), $currentBans['ban_reason']);
			echo '
					<tr>
						<td>Expires in:</td>
						<td>';
			if($currentBans['ban_expires_on'] == 0)
				echo '<span class="label label-important">Never</span>';
			else {
				$currentBans['ban_expires_on'] = $currentBans['ban_expires_on'] + $mysqlSecs;
				$currentBans['ban_time'] = $currentBans['ban_time'] + $mysqlSecs;
				$expires = $currentBans['ban_expires_on'] - time();
				if($expires > 0)
					echo '<time datetime="'.date('c', $currentBans['ban_expires_on']).'">'.secs_to_h($expires).'</time>';
				else
					echo 'Now';
			}
			echo '</td>
					</tr>
					<tr>
						<td>Banned by:</td>
						<td>'.$currentBans['banned_by'].'</td>
					</tr>
					<tr>
						<td>Banned at:</td>
						<td>'.date('jS F Y h:i:s A', $currentBans['ban_time']).'</td>
					</tr>
					<tr>
						<td>Reason:</td>
						<td>'.$reason.'</td>
					</tr>';
			if(!empty($currentBans['server'])) {
				echo '
					<tr>
						<td>Server:</td>
						<td>'.$currentBans['server'].'</td>
					</tr>';
			}
		}
				?>
				</tbody>
			</table>
			<br />
			<table class="table table-striped table-bordered" id="previous-ip-bans">
				<caption>Previous Bans</caption>
				<thead>
					<th>ID</th>
					<th>Reason</th>
					<th>By</th>
					<th>On</th>
					<th>Length</th>
					<th>Unbanned By</th>
					<th>At</th><?php
		if(!is_array($pastBans[0]))
			$pastBans = array($pastBans);
		$serverName = false;
		foreach($pastBans as $r) {
			if(!empty($r['server'])) {
				$serverName = true;
				break;
			}
		}
		if($serverName) {
				echo '
					<th>Server</th>';
		}		
				?>
				</thead>
				<tbody><?php
		if(isset($pastBans[0]) && count($pastBans[0]) == 0) {
			echo '
					<tr>
						<td colspan="8">None</td>
					</tr>';
		} else {
			$i = 1;
			if(!isset($pastBans[0]) || (isset($pastBans[0]) && !is_array($pastBans[0])))
				$pastBans = array($pastBans);
			foreach($pastBans as $r) {
				$r['ban_reason'] = str_replace(array('&quot;', '"'), array('&#039;', '\''), $r['ban_reason']);
				$r['ban_expired_on'] = ($r['ban_expired_on'] != 0 ? $r['ban_expired_on'] + $mysqlSecs : $r['ban_expired_on']);
				$r['ban_time'] = $r['ban_time'] + $mysqlSecs;
				$r['unbanned_time'] = $r['unbanned_time'] + $mysqlSecs;

				echo '
					<tr>
						<td>'.$i.'</td>
						<td>'.$r['ban_reason'].'</td>
						<td>'.$r['banned_by'].'</td>
						<td>'.date('H:i:s d/m/y', $r['ban_time']).'</td>
						<td>'.($r['ban_expired_on'] == 0 ? 'Never' : secs_to_h($r['ban_expired_on'] - $r['ban_time'])).'</td>
						<td>'.$r['unbanned_by'].'</td>
						<td>'.date('H:i:s d/m/y', $r['unbanned_time']).'</td>'.($serverName ? '
						<td>'.$r['server'].'</td>' : '').($admin ? '
						<td class="admin-options"><a href="#" class="btn btn-danger delete" title="Remove" data-server="'.$_GET['server'].'" data-record-id="'.$r['ban_record_id'].'"><i class="icon-trash icon-white"></i></a></td>' : '').'
					</tr>';
				++$i;
			}
		}
				?>
				</tbody>
			</table>
		</div>
		<?php
	}
}
?>