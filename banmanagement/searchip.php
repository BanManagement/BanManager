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
	$pastbans = true;
	if(isset($_GET['excluderecords']))
		$pastbans = false;

	// Get the server details
	$server = $settings['servers'][$_GET['server']];
	
	// Clear old search cache's
	clearCache($_GET['server'].'/search', 300);
	clearCache($_GET['server'].'/mysqlTime', 300);
	
	$search = $_GET['player'];
	$page = 0;
	$size = 10;
	$sortByCol = 0;
	$sortBy = 'ASC';
	$filter = '';
	$filterCol = 0;
	
	$timeDiff = cache('SELECT ('.time().' - UNIX_TIMESTAMP(now()))/3600 AS mysqlTime', 5, $_GET['server'].'/mysqlTime', $server); // Cache it for a few seconds
		
	$mysqlTime = $timeDiff['mysqlTime'];
	$mysqlTime = ($mysqlTime > 0)  ? floor($mysqlTime) : ceil ($mysqlTime);
	$mysqlSecs = ($mysqlTime * 60) * 60;
	
	if(isset($_GET['ajax'])) {

		if(isset($_GET['page']) && is_numeric($_GET['page']))
			$page = $_GET['page'];
		if(isset($_GET['size']) && is_numeric($_GET['size']))
			$size = $_GET['size'];
		if(isset($_GET['filter']) && $_GET['filter'] != 'filter') {
			preg_match('/filter\[([0-9])\]=([a-z0-9]*)/', $_GET['filter'], $filters);
			if(!empty($filters)) {
				if(isset($filters[1]) && is_numeric($filters[1]))
					$filterCol = $filters[1];
				if(isset($filters[2]) && is_alphanum($filters[2]))
					$filter = $filters[2];
			}
		}
		if(isset($_GET['sortby'])) {
			preg_match('/column\[([0-9])\]=([0-9])/', $_GET['sortby'], $orders);
			if(!empty($orders)) {
				if(isset($orders[1]) && is_numeric($orders[1]))
					$sortByCol = $orders[1];
				if(isset($orders[2]) && is_numeric($orders[2]))
					$sortBy = ($orders[2] == 0 ? 'ASC' : 'DESC');
			}
		}
		$found = searchIps($search, $_GET['server'], $server, $sortByCol, $sortBy, $pastbans);
		$total = count($found);
		$timeNow = time();
		
		if(is_array($found)) {
			$playerNames = array_keys($found);
			
			$ajaxArray = array();
			$ajaxArray['total_rows'] = $total;
			
			$start = $page * $size;
			$end = $start + $size;
			
			if(!empty($filter)) {
				$start = 0;
				$end = $total;
			}
			
			for($i = $start; $i < $end; ++$i) {
				if(!isset($playerNames[$i]))
					break;
					
				$player = $found[$playerNames[$i]];
				$expireTime = ($player['expires'] + $mysqlSecs)- $timeNow;
				
				if($player['expires'] == 0)
					$expires = '<span class="label label-important">Never</span>';
				else if(isset($expireTime) && $expireTime > 0) {
					$expires = '<span class="label label-warning">'.secs_to_hmini($expireTime).'</span>';
				} else
					$expires = '<span class="label label-success">Past</span>';
				
				if(!empty($filter)) {
					$skip = false;
					switch($filterCol) {
						case 0:
							if(stripos($playerNames[$i], $filter) === false)
								$skip = true;
						break;
						case 1:
							if(stripos($player['type'], $filter) === false)
								$skip = true;
						break;
						case 2:
							if(stripos($player['by'], $filter) === false)
								$skip = true;
						break;
						case 3:
							if(stripos($player['reason'], $filter) === false)
								$skip = true;
						break;
						case 4:
							if(stripos($expires, $filter) === false)
								$skip = true;
						break;
						case 5:
							$time = (!empty($player['time']) ? date('j F Y h:i:s A', $player['time']) : '');
							if(stripos($time, $filter) === false)
								$skip = true;
						break;
					}
					
					if($skip)
						continue;
				}
				
				if(!isset($time))
					$time = (!empty($player['time']) ? date('j F Y h:i:s A', $player['time']) : '');
				
				$ajaxArray['rows'][] = array(
					'<a href="index.php?action=viewip&ip='.$playerNames[$i].'&server='.$_GET['server'].'">'.$playerNames[$i].'</a>',
					$player['type'],
					$player['by'],
					$player['reason'],
					$expires,
					$time
				);
				unset($time);
			}
			
			if(!empty($filter) && isset($ajaxArray['rows'])) {
				$ajaxArray['total_rows'] = count($ajaxArray['rows']);
				$start = $page * $size;
				
				if($ajaxArray['total_rows'] >= $start) {	
					$ajaxArray['rows'] = array_slice($ajaxArray['rows'], $start, $size);
				}
			}
		} else
			$total = 1;
		if(!is_array($found) || !isset($ajaxArray['rows']))
			$ajaxArray = array('total_rows' => 1, 'rows' => array(array('None Found', '', '', '', '', '')));
		
		die(json_encode($ajaxArray));
	}
	$found = searchIps($search, $_GET['server'], $server, $sortByCol, $sortBy, $pastbans);

	if(!$found) {
		errors('No matched players found');
		?><a href="index.php" class="btn btn-primary">New Search</a><?php
	} else {
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
			<tr>
				<th>IP Address</th>
				<th>Type</th>
				<th>By</th>
				<th>Reason</th>
				<th>Expires</th>
				<th>Date</th>
			</tr>
		</thead>
		<tbody>
		</tbody>
		<tfoot>
			<tr>
				<th colspan="7" class="pager form-horizontal">
					<button class="btn first"><i class="icon-step-backward"></i></button>
					<button class="btn prev"><i class="icon-arrow-left"></i></button>
					<span class="pagedisplay"></span>
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