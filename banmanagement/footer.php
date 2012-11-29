			<hr>
			<footer>
				<p class="pull-left"><?php echo $settings['footer']; ?></p>
				<!-- Must not be removed as per the licence terms -->
				<p class="pull-right">Created By <a href="http://www.frostcast.net" target="_blank">Frostcast</a></p>
			</footer>
		</div> <!-- /container -->
		<script type="text/javascript">
			$(function() {
				$(".span4 button[rel='popover']").popover({trigger: 'hover', placement: 'left'});
				$("#search li").click(function(e) {
					var s = $(this);
					if(s.attr("id") == 'ip') {
						var player = $("#player");
						$("#ip").attr('id', 'player').find("a").text('Player');
						player.attr('id', 'ip').html('IP&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="caret"></span>');
						$("#search input[type=text]").attr('placeholder', 'Enter IP Address');
						$("#search input[name=action]").attr('value', 'searchip');
					} else {
						var ip = $("#ip");
						$("#player").attr('id', 'ip').find("a").text('IP');
						ip.attr('id', 'player').html('Player <span class="caret"></span>');
						$("#search input[type=text]").attr('placeholder', 'Enter Player Name');
						$("#search input[name=action]").attr('value', 'searchplayer');
					}
				});
				$("#viewall").click(function() {
					window.location.href = 'index.php?action='+$("#search input[name=action]").val()+'&server='+$("#search input[name=server]").val()+'&player=%';
				});
			});
		</script>
	</body>
</html>