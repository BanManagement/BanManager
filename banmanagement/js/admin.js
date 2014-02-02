/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
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
			url: 'index.php?action=addserver&ajax=true&authid='+authid,
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
					$("#servers tbody").append('<tr><td>'+data.success.serverName+'</td><td><a href="#" class="btn btn-danger deleteServer" data-serverid="'+data.success.id+'"><i class="glyphicon glyphicon-trash"></i></a></td>');
					$("#noservers").remove();
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				formBody.show();
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
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
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
	});
	$(".reorderServer").live('click', function() {
		var id = $(this).data('serverid');
		var order = $(this).data('order');
		var formBody = $("#container");
		$this = $(this);
		$(this).append('<div id="ajaxLoading" class="small"><span id="loadingSmall"></span></div>').find('.glyphicon-arrow-up').hide().parent().addClass('disabled');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=reorderserver&ajax=true&authid='+authid+'&server='+id+'&order='+order,
			type: 'post',
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
				$this.removeClass('disabled').find('.glyphicon-arrow-up').show();
				if(data.error) {
					formBody.prepend(error(data.error));
				} else {
					errorRemove();
					$("#servers").html(data.success.table);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				$this.removeClass('disabled').find('.glyphicon-arrow-up').show();
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
	});
	$("form.settings").submit(function(e) {
		e.preventDefault();
		var form = $(this);
		var formBody = form.find("table");
		if(!form.valid())
			return false;
		errorRemove();
		formBody.hide().after('<div id="ajaxLoading"><span id="loadingSmall"></span><br />Saving</div>');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=updatesettings&ajax=true&authid='+authid,
			data: form.serialize(),
			type: 'post',
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
				formBody.show();
				if(data.error) {
					form.prepend(error(data.error));
				} else {
					errorRemove();
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				formBody.show();
				form.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
		return false;
	});
	
	if(jQuery.fn.datetimepicker) {
		$('.modal').on('shown.bs.modal', function () {
			$(".datetimepicker").datetimepicker();
		});
	}

	$(".yourtime").html(dateFormat(new Date(), "dd/mm/yyyy HH:MM:ss"));
	
	$("#editban form .bantype, #editmute form .bantype").click(function(e) {
		e.preventDefault();
		var $expires = $(this).parent().parent().find("input[name=expires]");

		if($(this).html() == 'Permanent') {
			$(this).html('Temporary');

			$expires.removeAttr("disabled");
	
			var picker = $(this).parent().parent().data('DateTimePicker');

			picker.setDate(new Date());

		} else {
			$(this).html('Permanent');
			$expires.val('');
			$expires.attr("disabled", "disabled");
		}
	});
	
	$("#editban form").submit(function(e) {
		e.preventDefault();

		var form = $(this);
		var formBody = form.find(".modal-body");
		if(!form.valid())
			return false;
		errorRemove();
		
		if($(this).find("input[name=expires]").attr('disabled') == 'disabled') {
			$(this).find("input[name=expires]").val('');
		}
		
		formBody.hide().after('<div id="ajaxLoading"><span id="loadingSmall"></span><br />Saving</div>');
		showLoading('loadingSmall');
		$("#editban form input[name=expiresTimestamp]").val($("#editban form input[name=expires]").parent().data('DateTimePicker').getDate().toDate().getTime() / 1000);
		$.ajax({
			url: 'index.php?action=updateban&ajax=true&authid='+authid,
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
					$("#editban").modal('hide');
					$("#current-ban .reason").html($("#editban form textarea[name=reason]").text());
					
					var expires = $("#editban form input[name=expires]").val();
					
					if(expires == "")
						$("#current-ban .expires").html('<span class="label label-important">Never</span>');
					else {
						$("#current-ban .expires").countdown({
							until: $("#editban form input[name=expires]").parent().data('DateTimePicker').getDate().toDate(),
							format: 'yowdhms', layout: '{y<} {yn} {yl}, {y>} {o<} {on} {ol}, {o>} {w<} {wn} {wl}, {w>} {d<} {dn} {dl}, {d>} {h<} {hn} {hl}, {h>} {m<} {mn} {ml}, {m>} {s<} {sn} {sl} {s>}',
							onExpiry: function() {
								location.reload();
							}
						});
					}
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				formBody.show();
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
		return false;
	});
	
	$("#editmute form").submit(function(e) {
		e.preventDefault();

		var form = $(this);
		var formBody = form.find(".modal-body");
		if(!form.valid())
			return false;
		errorRemove();
		
		if($(this).find("input[name=expires]").attr('disabled') == 'disabled') {
			$(this).find("input[name=expires]").val('');
		}
		
		formBody.hide().after('<div id="ajaxLoading"><span id="loadingSmall"></span><br />Saving</div>');
		showLoading('loadingSmall');
		$("#editmute form input[name=expiresTimestamp]").val($("#editmute form input[name=expires]").parent().parent().data('DateTimePicker').getDate().getTime()/1000);
		$.ajax({
			url: 'index.php?action=updatemute&ajax=true&authid='+authid,
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
					$("#editmute").modal('hide');
					$("#current-mute .reason").html($("#editmute form textarea[name=reason]").text());
					
					var expires = $("#editmute form input[name=expires]").val();
					
					if(expires == "")
						$("#current-mute .expires").html('<span class="label label-important">Never</span>');
					else {
						$("#current-mute .expires").countdown({
							until: $("#editmute form input[name=expires]").parent().parent().data('DateTimePicker').getDate(),
							format: 'yowdhms', layout: '{y<} {yn} {yl}, {y>} {o<} {on} {ol}, {o>} {w<} {wn} {wl}, {w>} {d<} {dn} {dl}, {d>} {h<} {hn} {hl}, {h>} {m<} {mn} {ml}, {m>} {s<} {sn} {sl} {s>}',
							onExpiry: function() {
								location.reload();
							}
						});
					}
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				formBody.show();
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
		return false;
	});
	
	$("#editbanip form").submit(function(e) {
		e.preventDefault();

		var form = $(this);
		var formBody = form.find(".modal-body");
		if(!form.valid())
			return false;
		errorRemove();
		
		if($(this).find("input[name=expires]").attr('disabled') == 'disabled') {
			$(this).find("input[name=expires]").val('');
		}
		
		formBody.hide().after('<div id="ajaxLoading"><span id="loadingSmall"></span><br />Saving</div>');
		showLoading('loadingSmall');
		$("#editipban form input[name=expiresTimestamp]").val($("#editipban form input[name=expires]").parent().parent().data('DateTimePicker').getDate().getTime()/1000);
		$.ajax({
			url: 'index.php?action=updateipban&ajax=true&authid='+authid,
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
					$("#editipban").modal('hide');
					$("#current-ban .reason").html($("#editipban form textarea[name=reason]").text());
					
					var expires = $("#editipban form input[name=expires]").val();
					
					if(expires == "")
						$("#current-ban .expires").html('<span class="label label-important">Never</span>');
					else {
						$("#current-ban .expires").countdown({
							until: $("#editipban form input[name=expires]").parent().parent().data('DateTimePicker').getDate(),
							format: 'yowdhms', layout: '{y<} {yn} {yl}, {y>} {o<} {on} {ol}, {o>} {w<} {wn} {wl}, {w>} {d<} {dn} {dl}, {d>} {h<} {hn} {hl}, {h>} {m<} {mn} {ml}, {m>} {s<} {sn} {sl} {s>}',
							onExpiry: function() {
								location.reload();
							}
						});
					}
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				formBody.show();
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
		return false;
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
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
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
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
	});
	
	$("#previous-kicks a.delete").live('click', function(e) {
		e.preventDefault();
		var id = $(this).data('record-id');
		var server = $(this).data('server');
		var formBody = $("#container");
		$this = $(this);
		$(this).append('<div id="ajaxLoading" class="small"><span id="loadingSmall"></span></div>').find('i').hide().parent().addClass('disabled');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=deletekickrecord&ajax=true&authid='+authid+'&server='+server+'&id='+id,
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
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
	});
	
	$("#previous-warnings a.delete").live('click', function(e) {
		e.preventDefault();
		var id = $(this).data('record-id');
		var server = $(this).data('server');
		var formBody = $("#container");
		$this = $(this);
		$(this).append('<div id="ajaxLoading" class="small"><span id="loadingSmall"></span></div>').find('i').hide().parent().addClass('disabled');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=deletewarning&ajax=true&authid='+authid+'&server='+server+'&id='+id,
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
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
	});
	
	$("#previous-ip-bans a.delete").live('click', function(e) {
		e.preventDefault();
		var id = $(this).data('record-id');
		var server = $(this).data('server');
		var formBody = $("#container");
		$this = $(this);
		$(this).append('<div id="ajaxLoading" class="small"><span id="loadingSmall"></span></div>').find('i').hide().parent().addClass('disabled');
		showLoading('loadingSmall');
		$.ajax({
			url: 'index.php?action=deleteipbanrecord&ajax=true&authid='+authid+'&server='+server+'&id='+id,
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
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
			}
		});
	});
	
	$("[data-role=confirm]").click(function(e) {
		e.preventDefault();
		$("body").append('<div class="modal fade" id="confirmModal"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><a class="close" data-dismiss="modal">&times;</a><h3>'+$(this).data("confirm-title")+'</h3></div><div class="modal-body"><p>'+$(this).data("confirm-body")+'</p></div><div class="modal-footer"><a href="#" class="btn cancel" data-dismiss="modal">Cancel</a><a href="'+$(this).attr("href")+'" class="btn btn-primary">Confirm</a></div></div></div></div>');
		$("#confirmModal").modal().find(".cancel").focus();
		$('#confirmModal').on('hidden', function () {
			$(this).remove();
		});
		return false;
	});
	
	$("#confirmModal a.btn-primary").live('click', function(e) {
		e.preventDefault();
		var formBody = $("#confirmModal .modal-body");
		$this = $(this);
		
		formBody.hide().after('<div id="ajaxLoading"><span id="loadingSmall"></span><br />Removing</div>');
		showLoading('loadingSmall');
		$.ajax({
			url: $this.attr('href'),
			type: 'get',
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
				formBody.show();
				if(data.error) {
					formBody.prepend(error(data.error));
				} else {
					$("#confirmModal").modal('hide');
					location.reload();
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
				$this.removeClass('disabled').find('i').show();
				formBody.prepend(error('Invalid response from server, try again<br />Response: '+jqXHR.responseText));
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

/*
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */

var dateFormat = function () {
    var token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,
        timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
        timezoneClip = /[^-+\dA-Z]/g,
        pad = function (val, len) {
            val = String(val);
            len = len || 2;
            while (val.length < len) val = "0" + val;
            return val;
        };

    // Regexes and supporting functions are cached through closure
    return function (date, mask, utc) {
        var dF = dateFormat;

        // You can't provide utc if you skip other args (use the "UTC:" mask prefix)
        if (arguments.length == 1 && Object.prototype.toString.call(date) == "[object String]" && !/\d/.test(date)) {
            mask = date;
            date = undefined;
        }

        // Passing date through Date applies Date.parse, if necessary
        date = date ? new Date(date) : new Date;
        if (isNaN(date)) throw SyntaxError("invalid date");

        mask = String(dF.masks[mask] || mask || dF.masks["default"]);

        // Allow setting the utc argument via the mask
        if (mask.slice(0, 4) == "UTC:") {
            mask = mask.slice(4);
            utc = true;
        }

        var _ = utc ? "getUTC" : "get",
            d = date[_ + "Date"](),
            D = date[_ + "Day"](),
            m = date[_ + "Month"](),
            y = date[_ + "FullYear"](),
            H = date[_ + "Hours"](),
            M = date[_ + "Minutes"](),
            s = date[_ + "Seconds"](),
            L = date[_ + "Milliseconds"](),
            o = utc ? 0 : date.getTimezoneOffset(),
            flags = {
                d:    d,
                dd:   pad(d),
                ddd:  dF.i18n.dayNames[D],
                dddd: dF.i18n.dayNames[D + 7],
                m:    m + 1,
                mm:   pad(m + 1),
                mmm:  dF.i18n.monthNames[m],
                mmmm: dF.i18n.monthNames[m + 12],
                yy:   String(y).slice(2),
                yyyy: y,
                h:    H % 12 || 12,
                hh:   pad(H % 12 || 12),
                H:    H,
                HH:   pad(H),
                M:    M,
                MM:   pad(M),
                s:    s,
                ss:   pad(s),
                l:    pad(L, 3),
                L:    pad(L > 99 ? Math.round(L / 10) : L),
                t:    H < 12 ? "a"  : "p",
                tt:   H < 12 ? "am" : "pm",
                T:    H < 12 ? "A"  : "P",
                TT:   H < 12 ? "AM" : "PM",
                Z:    utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
                o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
                S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
            };

        return mask.replace(token, function ($0) {
            return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
        });
    };
}();

// Some common format strings
dateFormat.masks = {
    "default":      "ddd mmm dd yyyy HH:MM:ss",
    shortDate:      "m/d/yy",
    mediumDate:     "mmm d, yyyy",
    longDate:       "mmmm d, yyyy",
    fullDate:       "dddd, mmmm d, yyyy",
    shortTime:      "h:MM TT",
    mediumTime:     "h:MM:ss TT",
    longTime:       "h:MM:ss TT Z",
    isoDate:        "yyyy-mm-dd",
    isoTime:        "HH:MM:ss",
    isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
    isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
};

// Internationalization strings
dateFormat.i18n = {
    dayNames: [
        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    ],
    monthNames: [
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
    ]
};

// For convenience...
Date.prototype.format = function (mask, utc) {
    return dateFormat(this, mask, utc);
};