$(function() {
	jQuery.validator.setDefaults({
		errorPlacement: function(error, placement) {
			error.wrap('<span class="help-inline" />');
			$(placement).after(error.parent());
			error.parent().parent().parent().removeClass('success').addClass('error');
		},
		success: function(label) {
			label.parent().parent().parent().removeClass('error').addClass('success');
		}
	});
	$("form").validate();
	$("time").each(function() {
		$(this).countdown({
			until: new Date($(this).attr('datetime')),
			format: 'yowdhms', layout: '{y<} {yn} {yl}, {y>} {o<} {on} {ol}, {o>} {w<} {wn} {wl}, {w>} {d<} {dn} {dl}, {d>} {h<} {hn} {hl}, {h>} {m<} {mn} {ml}, {m>} {s<} {sn} {sl} {s>}',
			onExpiry: function() {
				location.reload();
			}
		});
	});
});