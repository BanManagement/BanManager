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
	
	$result = cache("SELECT banned FROM ".$server['ipTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
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
			$found[] = $r['banned'];
	}
	// Check past bans!
	$result = cache("SELECT banned FROM ".$server['ipRecordTable']." WHERE banned LIKE '%".$_GET['player']."%'", 300, $_GET['server'].'/search', $server);
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
		foreach($result as $r)
			$found[] = $r['banned'];
	}
	if($noneCurrent && $nonePast) {
		errors('No matched players found');
		?><a href="index.php" class="btn btn-primary">New Search</a><?php
	} else {
		// Lets list all the players found!
		$found = array_unique($found); // Removes duplicates
		sort($found, SORT_STRING); // Order them in ascending order
		?>
	<table class="table table-striped table-bordered">
		<thead>
			<th>IP Address</th>
		</thead>
		<tbody>
		<?php
		foreach($found as $f) {
			echo '
				<tr>
					<td><a href="index.php?action=viewip&ip='.$f.'&server='.$_GET['server'].'">'.$f.'</a></td>
				</tr>';
		}
		?>
		</tbody>
	</table>
		<?php
	}
}
?>