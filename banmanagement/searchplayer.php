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
else {
	$excluderecords = false;
	if(isset($_GET['excluderecords']))
		$excluderecords = true;

	// Get the server details
	$server = $settings['servers'][$_GET['server']];
	
	// Clear old search cache's
	clearCache($_GET['server'].'/search', 300);
	
	$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expires_on FROM ".$server['bansTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
	if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
		$result = array($result);
	
	$rows = count($result);
	$found = array();
	$noneCurrent = false;
	$nonePast = false;
	$noneMuted = false;
	$noneMutesPast = false;
	
	if($rows == 1 && $_GET['player'] != '%') {
		// Found the player! Redirect
		$fetch = $result[0];
		redirect('index.php?action=viewplayer&player='.$fetch['banned'].'&server='.$_GET['server']);
	} else if($rows == 0)
		$noneCurrent = true;
	else if($rows > 0) {
		foreach($result as $r)
			$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expires_on']);
	}
	
	// Check past bans!
	if(!$excluderecords) {
		$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_on FROM ".$server['recordTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		$rows = count($result);
		if($rows > 0 && $_GET['player'] != '%') {
			// Found the player! Redirect
			$fetch = $result[0];
			redirect('index.php?action=viewplayer&player='.$fetch['banned'].'&server='.$_GET['server']);
		} else if($rows == 0)
			$nonePast = true;
		else if($rows > 0) {
			foreach($result as $r) {
				if(!isset($found[$r['banned']]))
					$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expired_on'], 'past' => true);
				else if($found[$r['banned']]['time'] < $r['ban_time'])
					$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expired_on'], 'past' => true);
			}
		}
	}
	
	// Check current mutes
	$result = cache("SELECT muted, muted_by, mute_reason, mute_time, mute_expires_on FROM ".$server['mutesTable']." WHERE muted LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
	if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
		$result = array($result);
	$rows = count($result);
	
	if($rows == 1 && $_GET['player'] != '%') {
		// Found the player! Redirect
		$fetch = $result[0];
		redirect('index.php?action=viewplayer&player='.$fetch['muted'].'&server='.$_GET['server']);
	} else if($rows == 0)
		$noneMuted = true;
	else if($rows > 0) {
		foreach($result as $r) {
			if(!isset($found[$r['muted']]))
				$found[$r['muted']] = array('by' => $r['muted_by'], 'reason' => $r['mute_reason'], 'type' => 'Mute', 'time' => $r['mute_time'], 'expires' => $r['mute_expires_on']);
		}
	}
	
	// Check past mutes!
	if(!$excluderecords) {
		$result = cache("SELECT muted, muted_by, mute_reason, mute_time, mute_expired_on FROM ".$server['mutesRecordTable']." WHERE muted LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		$rows = count($result);
		if($rows > 0 && $_GET['player'] != '%') {
			// Found the player! Redirect
			$fetch = $result[0];
			redirect('index.php?action=viewplayer&player='.$fetch['muted'].'&server='.$_GET['server']);
		} else if($rows == 0)
			$noneMutesPast = true;
		else if($rows > 0) {
			foreach($result as $r) {
				if(!isset($found[$r['muted']]))
					$found[$r['muted']] = array('by' => $r['muted_by'], 'reason' => $r['mute_reason'], 'type' => 'Mute', 'time' => $r['mute_time'], 'expires' => $r['mute_expired_on'], 'past' => true);
				else if($found[$r['muted']]['time'] < $r['mute_time'])
					$found[$r['muted']] = array('by' => $r['muted_by'], 'reason' => $r['mute_reason'], 'type' => 'Mute', 'time' => $r['mute_time'], 'expires' => $r['mute_expired_on'], 'past' => true);
			}
		}
	}
	
	 // Check past kicks!
	if(!$excluderecords) {
		$result = cache("SELECT kicked, kicked_by, kick_reason, kick_time FROM ".$server['kicksTable']." WHERE kicked LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		$rows = count($result);
		if($rows > 0 && $_GET['player'] != '%') {
			// Found the player! Redirect
			$fetch = $result[0];
			redirect('index.php?action=viewplayer&player='.$fetch['kicked'].'&server='.$_GET['server']);
		} else if($rows == 0)
			$noneKicksPast = true;
		else if($rows > 0) {
			foreach($result as $r) {
				if(!isset($found[$r['kicked']]))
					$found[$r['kicked']] = array('by' => $r['kicked_by'], 'reason' => $r['kick_reason'], 'type' => 'Kick', 'time' => $r['kick_time'], 'expires' => 0, 'past' => true);
				else if($found[$r['kicked']]['time'] < $r['kick_time'])
					$found[$r['kicked']] = array('by' => $r['kicked_by'], 'reason' => $r['kick_reason'], 'type' => 'Kick', 'time' => $r['kick_time'], 'expires' => 0, 'past' => true);
			}
		}
	}

	if($noneCurrent && $nonePast && $noneMuted && $noneMutesPast && $noneKicksPast) {
		errors('No matched players found');
		?><a href="index.php" class="btn btn-primary">New Search</a><?php
	} else {
		// Lets list all the players found!
		ksort($found, SORT_STRING); // Order them in ascending order
		?>
	<form class="form-inline" action="" method="get">
		<fieldset>
			<legend>Search Options</legend>
			<input type="hidden" name="action" value="searchplayer" />
			<input type="hidden" name="server" value="<?php echo $_GET['server']; ?>" />
			<input type="hidden" name="player" value="<?php echo $_GET['player']; ?>" />
			<label class="checkbox">
				Exclude Past<input type="checkbox" name="excluderecords" value="1" <?php
				if(isset($_GET['excluderecords']))
					echo 'checked="checked"';
				?>/>
			</label>
			<button type="submit" class="btn"><i class="icon-search"></i></button>
		</fieldset>
	</form>
	<table class="table table-striped table-bordered sortable">
		<thead>
			<th>Player Name</th>
			<th>Type</th>
			<th>By</th>
			<th>Reason</th>
			<th>Expires</th>
			<th>Date</th>
		</thead>
		<tbody>
		<?php
		
		$timeDiff = cache('SELECT ('.time().' - UNIX_TIMESTAMP(now()))/3600 AS mysqlTime', 5, $_GET['server'], $server); // Cache it for a few seconds
		
		$mysqlTime = $timeDiff['mysqlTime'];
		$mysqlTime = ($mysqlTime > 0)  ? floor($mysqlTime) : ceil ($mysqlTime);
		$mysqlSecs = ($mysqlTime * 60) * 60;
		
		foreach($found as $player => $f) {
			
			if($f['type'] != 'Kick' && $f['expires'] != 0) {
				$expires = ($f['expires'] + $mysqlSecs)- time();
			}
		
			echo '
				<tr'.(isset($f['past']) ? ' class="warning"' : '').'>
					<td><a href="index.php?action=viewplayer&player='.$player.'&server='.$_GET['server'].'">'.$player.'</a></td>
					<td>'.$f['type'].'</td>
					<td>'.$f['by'].'</td>
					<td>'.$f['reason'].'</td>
					<td>';		
			if($f['type'] != 'Kick') {
				if($f['expires'] == 0)
					echo '<span class="label label-important">Never</span>';
				else if(isset($expires) && $expires > 0) {
					echo '<span class="label label-warning">'.secs_to_hmini($expires).'</span>';
					unset($expires);
				} else
					echo '<span class="label label-success">Past</span>';
			}
			echo '</td>
					<td>'.(!empty($f['time']) ? date('j F Y h:i:s A', $f['time']) : '').'</td>';
			
			echo '
				</tr>';
		}
		?>
		</tbody>
		<tfoot>
			<tr>
				<th colspan="7" class="pager form-horizontal">
					<button class="btn first"><i class="icon-step-backward"></i></button>
					<button class="btn prev"><i class="icon-arrow-left"></i></button>
					<span class="pagedisplay"></span> <!-- this can be any element, including an input -->
					<button class="btn next"><i class="icon-arrow-right"></i></button>
					<button class="btn last"><i class="icon-step-forward"></i></button>
					<select class="pagesize input-mini" title="Select page size">
						<option selected="selected" value="10">10</option>
						<option value="20">20</option>
						<option value="30">30</option>
						<option value="40">40</option>
					</select>
					<select class="pagenum input-mini" title="Select page number"></select>
				</th>
			</tr>
		</tfoot>
	</table>
		<?php
	}
}
?>