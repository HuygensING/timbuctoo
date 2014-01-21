function login() {
	var wl = window.location;
	var hsURL = wl.origin + wl.pathname;
	var loginURL = window.config.securityUri;

	var form = $('<form>');
	form.attr({
		method : 'POST',
		action : loginURL
	});

	hsUrlEl = $('<input>').attr({
		name : 'hsurl',
		value : hsURL,
		type : 'hidden'
	});
	form.append(hsUrlEl);
	$('body').append(form);
	form.submit();
}

function getParameterByName(name) {
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex
			.exec(location.search);
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g,
			" "));
}

function setCookie(key, value) {
	var expires = new Date();
	expires.setTime(expires.getTime() + (1 * 24 * 60 * 60 * 1000));
	document.cookie = key + '=' + value + ';expires=' + expires.toUTCString();
}

function getCookie(key) {
	var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
	return keyValue ? keyValue[2] : null;
}