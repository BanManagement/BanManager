<?php
if(!isset($_GET['server']) || !is_numeric($_GET['server']))
	redirect('index.php');
else if(!isset($settings['servers'][$_GET['server']]))
	redirect('index.php');
else if(!isset($_GET['player']) || empty($_GET['player']))
	redirect('index.php');
else {
	// Get the server details
	$server = $settings['servers'][$_GET['server']];
	
	// Clear old search cache's
	clearCache($_GET['server'].'/search', 300);
	
	$result = cache("SELECT banned FROM ".$server['bansTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
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
			$found[] = $r['banned'];
	}
	// Check past bans!
	$result = cache("SELECT banned FROM ".$server['recordTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
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
		foreach($result as $r)
			$found[] = $r['banned'];
	}
	
	// Check current mutes
	$result = cache("SELECT muted FROM ".$server['mutesTable']." WHERE muted LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
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
		foreach($result as $r)
			$found[] = $r['muted'];
	}
	
	// Check past mutes!
	$result = cache("SELECT muted FROM ".$server['mutesRecordTable']." WHERE muted LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
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
		foreach($result as $r)
			$found[] = $r['muted'];
	}
	
	if($noneCurrent && $nonePast && $noneMutes && $noneMutesPast) {
		errors('No matched players found');
		?><a href="index.php" class="btn btn-primary">New Search</a><?php
	} else {
		// Lets list all the players found!
		$found = array_unique($found); // Removes duplicates
		sort($found, SORT_STRING); // Order them in ascending order
		?>
	<table class="table table-striped table-bordered">
		<thead>
			<th>Player Name</th>
		</thead>
		<tbody>
		<?php
		foreach($found as $f) {
			echo '
				<tr>
					<td><a href="index.php?action=viewplayer&player='.$f.'&server='.$_GET['server'].'">'.$f.'</a></td>
				</tr>';
		}
		?>
		</tbody>
	</table>
		<?php
	}
}
?>