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
else if(!isset($_GET['player']) || empty($_GET['player']))
	redirect('index.php');
else if(isset($_GET['player']) && preg_match('/[^a-z0-9_]{2,16}/i', $_GET['player']))
	redirect('index.php');
else {
	// Get the server details
	$server = $settings['servers'][$_GET['server']];

	// Clear old players
	clearCache($_GET['server'].'/players', 300);
	
	// Check if they are logged in as an admin
	if(isset($_SESSION['admin']) && $_SESSION['admin'])
		$admin = true;
	else
		$admin = false;
	
	// Check if the player exists
	$currentBans = cache("SELECT * FROM ".$server['bansTable']." WHERE banned = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$pastBans = cache("SELECT * FROM ".$server['recordTable']." WHERE banned = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$currentMutes = cache("SELECT * FROM ".$server['mutesTable']." WHERE muted = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$pastMutes = cache("SELECT * FROM ".$server['mutesRecordTable']." WHERE muted = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$pastKicks = cache("SELECT * FROM ".$server['kicksTable']." WHERE kicked = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);

	if(count($currentBans) == 0 && count($pastBans) == 0 && count($currentMutes) == 0 && count($pastMutes) == 0 && count($pastKicks) == 0) {
		errors('Player does not exist');
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
			<h2><?php echo $_GET['player']; ?></h2>
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
						<td>'.$currentBans['ban_reason'].'</td>
					</tr>
					<tr>';
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
			<table class="table table-striped table-bordered">
				<caption>Current Mute</caption>
				<tbody>
				<?php
		if(count($currentMutes) == 0) {
			echo '
					<tr>
						<td colspan="2">None</td>
					</tr>';
		} else {
			$reason = str_replace(array('&quot;', '"'), array('&#039;', '\''), $currentMutes['mute_reason']);
			echo '
					<tr>
						<td>Expires in:</td>
						<td>';
			if($currentMutes['mute_expires_on'] == 0)
				echo '<span class="label label-important">Never</span>';
			else {
				$currentMutes['mute_expires_on'] = $currentMutes['mute_expires_on'] + $mysqlSecs;
				$currentMutes['mute_time'] = $currentMutes['mute_time'] + $mysqlSecs;
				$expires = $currentMutes['mute_expires_on'] - time();
				if($expires > 0)
					echo '<time datetime="'.date('c', $currentMutes['mute_expires_on']).'">'.secs_to_h($expires).'</time>';
				else
					echo 'Now';
			}
			echo '</td>
					</tr>
					<tr>
						<td>Muted by:</td>
						<td>'.$currentMutes['muted_by'].'</td>
					</tr>
					<tr>
						<td>Muted at:</td>
						<td>'.date('jS F Y h:i:s A', $currentMutes['mute_time']).'</td>
					</tr>
					<tr>
						<td>Reason:</td>
						<td>'.$currentMutes['mute_reason'].'</td>
					</tr>
					<tr>';
			if(!empty($currentMutes['server'])) {
				echo '
					<tr>
						<td>Server:</td>
						<td>'.$currentMutes['server'].'</td>
					</tr>';
			}
		}
				?>
				</tbody>
			</table>
			<br />
			<table class="table table-striped table-bordered" id="previous-bans">
				<caption>Previous Bans</caption>
				<thead>
					<th>ID</th>
					<th>Reason</th>
					<th>By</th>
					<th>On</th>
					<th>Length</th>
					<th>Unbanned By</th>
					<th>At</th><?php
		if(!isset($pastBans[0]) || (isset($pastBans[0]) && !is_array($pastBans[0])))
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
		if($admin)
			echo '
					<th></th>';
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
			<br />
			<table class="table table-striped table-bordered" id="previous-mutes">
				<caption>Previous Mutes</caption>
				<thead>
					<th>ID</th>
					<th>Reason</th>
					<th>By</th>
					<th>On</th>
					<th>Length</th>
					<th>Unbanned By</th>
					<th>At</th><?php
		if(isset($pastMutes[0]) && !is_array($pastMutes[0]))
			$pastMutes = array($pastMutes);
		$serverName = false;
		foreach($pastMutes as $r) {
			if(!empty($r['server'])) {
				$serverName = true;
				break;
			}
		}
		if($serverName) {
				echo '
					<th>Server</th>';
		}
		if($admin)
			echo '
					<th></th>';
				?>
				</thead>
				<tbody><?php
		if(count($pastMutes) == 0) {
			echo '
					<tr>
						<td colspan="8">None</td>
					</tr>';
		} else {
			$i = 1;
			foreach($pastMutes as $r) {
				$r['mute_reason'] = str_replace(array('&quot;', '"'), array('&#039;', '\''), $r['mute_reason']);
				$r['mute_expired_on'] = ($r['mute_expired_on'] != 0 ? $r['mute_expired_on'] + $mysqlSecs : $r['mute_expired_on']);
				$r['mute_time'] = $r['mute_time'] + $mysqlSecs;
				echo '
					<tr>
						<td>'.$i.'</td>
						<td>'.$r['mute_reason'].'</td>
						<td>'.$r['muted_by'].'</td>
						<td>'.date('d/m/y', $r['mute_time']).'</td>
						<td>'.($r['mute_expired_on'] == 0 ? 'Never' : secs_to_h($r['mute_expired_on'] - $r['mute_time'])).'</td>
						<td>'.$r['unmuted_by'].'</td>
						<td>'.date('d/m/y', $r['unmuted_time']).'</td>'.($serverName ? '
						<td>'.$r['server'].'</td>' : '').($admin ? '
						<td class="admin-options"><a href="#" class="btn btn-danger delete" title="Remove" data-server="'.$_GET['server'].'" data-record-id="'.$r['mute_record_id'].'"><i class="icon-trash icon-white"></i></a></td>' : '').'
					</tr>';
				++$i;
			}
		}
				?>
				</tbody>
			</table>
			<br />
			<table class="table table-striped table-bordered" id="previous-kicks">
				<caption>Kicks</caption>
				<thead>
					<th>ID</th>
					<th>Reason</th>
					<th>By</th>
					<th>At</th><?php
		if(isset($pastKicks[0]) && !is_array($pastKicks[0]))
			$pastKicks = array($pastKicks);
		$serverName = false;
		foreach($pastKicks as $r) {
			if(!empty($r['server'])) {
				$serverName = true;
				break;
			}
		}
		if($serverName) {
				echo '
					<th>Server</th>';
		}
		if($admin)
			echo '
					<th></th>';
				?>
				</thead>
				<tbody><?php
		if(count($pastKicks) == 0) {
			echo '
					<tr>
						<td colspan="8">None</td>
					</tr>';
		} else {
			$i = 1;
			foreach($pastKicks as $r) {
				$r['kick_reason'] = str_replace(array('&quot;', '"'), array('&#039;', '\''), $r['kick_reason']);
				$r['kick_time'] = $r['kick_time'] + $mysqlSecs;
				echo '
					<tr>
						<td>'.$i.'</td>
						<td>'.$r['kick_reason'].'</td>
						<td>'.$r['kicked_by'].'</td>
						<td>'.date('d/m/y', $r['kick_time']).'</td>'.($serverName ? '
						<td>'.$r['server'].'</td>' : '').($admin ? '
						<td class="admin-options"><a href="#" class="btn btn-danger delete" title="Remove" data-server="'.$_GET['server'].'" data-record-id="'.$r['kick_id'].'"><i class="icon-trash icon-white"></i></a></td>' : '').'
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