<?php
/*  BanManagement � 2012, a web interface for the Bukkit plugin BanManager
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
	
	$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expires_on FROM ".$server['ipTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
	if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
		$result = array($result);
	
	$rows = count($result);
	$found = array();
	$noneCurrent = false;
	$nonePast = false;
	
	if($rows == 1 && $_GET['player'] != '%') {
		// Found the player! Redirect
		$fetch = $result[0];
		redirect('index.php?action=viewip&ip='.$fetch['banned'].'&server='.$_GET['server']);
	} else if($rows == 0)
		$noneCurrent = true;
	else if($rows > 0) {
		foreach($result as $r)
			$found[$r['banned']] = array('by' => $r['banned_by'], 'reason' => $r['ban_reason'], 'type' => 'Ban', 'time' => $r['ban_time'], 'expires' => $r['ban_expires_on']);
	}
	// Check past bans!
	if(!$excluderecords) {
		$result = cache("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_onFROM ".$server['ipRecordTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
		if(isset($result[0]) && !is_array($result[0]) && !empty($result[0]))
			$result = array($result);
		$rows = count($result);
		if($rows == 1 && $_GET['player'] != '%') {
			// Found the player! Redirect
			$fetch = $result[0];
			redirect('index.php?action=viewip&ip='.$fetch['banned'].'&server='.$_GET['server']);
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
	
	if($noneCurrent && $nonePast) {
		errors('No matched IPs found');
		?><a href="index.php" class="btn btn-primary">New Search</a><?php
	} else {
		// Lets list all the players found!
		ksort($found, SORT_STRING); // Order them in ascending order
		?>
	<form class="form-inline" action="" method="get">
		<fieldset>
			<legend>Search Options</legend>
			<input type="hidden" name="action" value="searchip" />
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
			<th>IP Address</th>
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
		
		foreach($found as $ip => $f) {
		
			if($f['expires'] != 0) {
				$expires = ($f['expires'] + $mysqlSecs)- time();
			}
			
			echo '
				<tr'.(isset($f['past']) ? ' class="warning"' : '').'>
					<td><a href="index.php?action=viewip&ip='.$ip.'&server='.$_GET['server'].'">'.$ip.'</a></td>
					<td>'.$f['type'].'</td>
					<td>'.$f['by'].'</td>
					<td>'.$f['reason'].'</td>
					<td>';		
			if($f['expires'] == 0)
				echo '<span class="label label-important">Never</span>';
			else if(isset($expires) && $expires > 0) {
				echo '<span class="label label-warning">'.secs_to_hmini($expires).'</span>';
				unset($expires);
			} else
				echo '<span class="label label-success">Past</span>';
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