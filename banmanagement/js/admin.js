$(function() {
	$("#addserver form").submit(function(e) {
		e.preventDefault();
		var form = $(this);
		var formBody = form.find(".modal-body");
		if(!form.valid())
			return false;
		errorRemove();
		formBody.hide().after('<div id="ajaxLoading"><span id="loadingSmall"></span><br />Testing connection</div>');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=addserver&ajax=true',
			data: form.serialize(),
			type: 'post',
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
				formBody.show();
				if(data.error) {
					formBody.prepend(error(data.error));
				} else {
					errorRemove();
					$("#addserver").modal('hide');
					$("#servers tbody").append('<tr><td>'+data.success.serverName+'</td><td><a href="#" class="btn btn-danger deleteServer" data-serverid="'+data.success.id+'"><i class="icon-trash icon-white"></i></a></td>');
					$("#noservers").remove();
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				formBody.show();
				formBody.prepend(error('Invalid response from server, try again'));
			}
		});
		return false;
	});
	$(".deleteServer").live('click', function() {
		var id = $(this).data('serverid');
		var formBody = $("#container");
		$this = $(this);
		$(this).append('<div id="ajaxLoading" class="small"><span id="loadingSmall"></span></div>').find('i').hide().parent().addClass('disabled');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=deleteserver&ajax=true&authid='+authid+'&id='+id,
			type: 'post',
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
				$this.removeClass('disabled').find('i').show();
				if(data.error) {
					formBody.prepend(error(data.error));
				} else {
					errorRemove();
					$this.parent().parent().fadeOut(function() {
						$(this).remove();
					});
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				$this.removeClass('disabled').find('i').show();
				formBody.prepend(error('Invalid response from server, try again'));
			}
		});
	});
	
	$("#previous-bans a.delete").live('click', function(e) {
		e.preventDefault();
		var id = $(this).data('record-id');
		var server = $(this).data('server');
		var formBody = $("#container");
		$this = $(this);
		$(this).append('<div id="ajaxLoading" class="small"><span id="loadingSmall"></span></div>').find('i').hide().parent().addClass('disabled');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=deletebanrecord&ajax=true&authid='+authid+'&server='+server+'&id='+id,
			type: 'post',
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
				$this.removeClass('disabled').find('i').show();
				if(data.error) {
					$(document).load().scrollTop(0);
					formBody.prepend(error(data.error));
				} else {
					errorRemove();
					$this.parent().parent().fadeOut(function() {
						$(this).remove();
					});
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				$this.removeClass('disabled').find('i').show();
				formBody.prepend(error('Invalid response from server, try again'));
			}
		});
	});
	
	$("#previous-mutes a.delete").live('click', function(e) {
		e.preventDefault();
		var id = $(this).data('record-id');
		var server = $(this).data('server');
		var formBody = $("#container");
		$this = $(this);
		$(this).append('<div id="ajaxLoading" class="small"><span id="loadingSmall"></span></div>').find('i').hide().parent().addClass('disabled');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=deletemuterecord&ajax=true&authid='+authid+'&server='+server+'&id='+id,
			type: 'post',
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
				$this.removeClass('disabled').find('i').show();
				if(data.error) {
					$(document).load().scrollTop(0);
					formBody.prepend(error(data.error));
				} else {
					errorRemove();
					$this.parent().parent().fadeOut(function() {
						$(this).remove();
					});
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				$this.removeClass('disabled').find('i').show();
				formBody.prepend(error('Invalid response from server, try again'));
			}
		});
	});
});

function showLoading(element) {
	var cl = new CanvasLoader(element);
	cl.setColor('#4e68d9'); // default is '#000000'
	cl.setDiameter(16); // default is 40
	cl.setDensity(59); // default is 40
	cl.setRange(1.6); // default is 1.3
	cl.setSpeed(4); // default is 2
	cl.show(); // Hidden by default
}

function hideLoading() {
	$("#ajaxLoading").remove();
}

function error(message) {
	return '<div id="error" class="alert alert-error"><button class="close" data-dismiss="alert">&times;</button><h4 class="alert-heading">Error</h4><ol><li>'+message+'</li></ol></div>';
}

function errorRemove() {
	$("#error").remove();
}