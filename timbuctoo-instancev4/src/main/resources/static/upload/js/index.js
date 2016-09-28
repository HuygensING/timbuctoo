(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.ExcelImportMock = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
// shim for using process in browser
var process = module.exports = {};

// cached from whatever global is present so that test runners that stub it
// don't break things.  But we need to wrap it in a try catch in case it is
// wrapped in strict mode code which doesn't define any globals.  It's inside a
// function because try/catches deoptimize in certain engines.

var cachedSetTimeout;
var cachedClearTimeout;

function defaultSetTimout() {
    throw new Error('setTimeout has not been defined');
}
function defaultClearTimeout () {
    throw new Error('clearTimeout has not been defined');
}
(function () {
    try {
        if (typeof setTimeout === 'function') {
            cachedSetTimeout = setTimeout;
        } else {
            cachedSetTimeout = defaultSetTimout;
        }
    } catch (e) {
        cachedSetTimeout = defaultSetTimout;
    }
    try {
        if (typeof clearTimeout === 'function') {
            cachedClearTimeout = clearTimeout;
        } else {
            cachedClearTimeout = defaultClearTimeout;
        }
    } catch (e) {
        cachedClearTimeout = defaultClearTimeout;
    }
} ())
function runTimeout(fun) {
    if (cachedSetTimeout === setTimeout) {
        //normal enviroments in sane situations
        return setTimeout(fun, 0);
    }
    // if setTimeout wasn't available but was latter defined
    if ((cachedSetTimeout === defaultSetTimout || !cachedSetTimeout) && setTimeout) {
        cachedSetTimeout = setTimeout;
        return setTimeout(fun, 0);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedSetTimeout(fun, 0);
    } catch(e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't trust the global object when called normally
            return cachedSetTimeout.call(null, fun, 0);
        } catch(e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error
            return cachedSetTimeout.call(this, fun, 0);
        }
    }


}
function runClearTimeout(marker) {
    if (cachedClearTimeout === clearTimeout) {
        //normal enviroments in sane situations
        return clearTimeout(marker);
    }
    // if clearTimeout wasn't available but was latter defined
    if ((cachedClearTimeout === defaultClearTimeout || !cachedClearTimeout) && clearTimeout) {
        cachedClearTimeout = clearTimeout;
        return clearTimeout(marker);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedClearTimeout(marker);
    } catch (e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't  trust the global object when called normally
            return cachedClearTimeout.call(null, marker);
        } catch (e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error.
            // Some versions of I.E. have different rules for clearTimeout vs setTimeout
            return cachedClearTimeout.call(this, marker);
        }
    }



}
var queue = [];
var draining = false;
var currentQueue;
var queueIndex = -1;

function cleanUpNextTick() {
    if (!draining || !currentQueue) {
        return;
    }
    draining = false;
    if (currentQueue.length) {
        queue = currentQueue.concat(queue);
    } else {
        queueIndex = -1;
    }
    if (queue.length) {
        drainQueue();
    }
}

function drainQueue() {
    if (draining) {
        return;
    }
    var timeout = runTimeout(cleanUpNextTick);
    draining = true;

    var len = queue.length;
    while(len) {
        currentQueue = queue;
        queue = [];
        while (++queueIndex < len) {
            if (currentQueue) {
                currentQueue[queueIndex].run();
            }
        }
        queueIndex = -1;
        len = queue.length;
    }
    currentQueue = null;
    draining = false;
    runClearTimeout(timeout);
}

process.nextTick = function (fun) {
    var args = new Array(arguments.length - 1);
    if (arguments.length > 1) {
        for (var i = 1; i < arguments.length; i++) {
            args[i - 1] = arguments[i];
        }
    }
    queue.push(new Item(fun, args));
    if (queue.length === 1 && !draining) {
        runTimeout(drainQueue);
    }
};

// v8 likes predictible objects
function Item(fun, array) {
    this.fun = fun;
    this.array = array;
}
Item.prototype.run = function () {
    this.fun.apply(null, this.array);
};
process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];
process.version = ''; // empty string to avoid regexp issues
process.versions = {};

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;

process.binding = function (name) {
    throw new Error('process.binding is not supported');
};

process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};
process.umask = function() { return 0; };

},{}],2:[function(require,module,exports){
/*!
  Copyright (c) 2016 Jed Watson.
  Licensed under the MIT License (MIT), see
  http://jedwatson.github.io/classnames
*/
/* global define */

(function () {
	'use strict';

	var hasOwn = {}.hasOwnProperty;

	function classNames () {
		var classes = [];

		for (var i = 0; i < arguments.length; i++) {
			var arg = arguments[i];
			if (!arg) continue;

			var argType = typeof arg;

			if (argType === 'string' || argType === 'number') {
				classes.push(arg);
			} else if (Array.isArray(arg)) {
				classes.push(classNames.apply(null, arg));
			} else if (argType === 'object') {
				for (var key in arg) {
					if (hasOwn.call(arg, key) && arg[key]) {
						classes.push(key);
					}
				}
			}
		}

		return classes.join(' ');
	}

	if (typeof module !== 'undefined' && module.exports) {
		module.exports = classNames;
	} else if (typeof define === 'function' && typeof define.amd === 'object' && define.amd) {
		// register as 'classnames', consistent with npm package name
		define('classnames', [], function () {
			return classNames;
		});
	} else {
		window.classNames = classNames;
	}
}());

},{}],3:[function(require,module,exports){
var pSlice = Array.prototype.slice;
var objectKeys = require('./lib/keys.js');
var isArguments = require('./lib/is_arguments.js');

var deepEqual = module.exports = function (actual, expected, opts) {
  if (!opts) opts = {};
  // 7.1. All identical values are equivalent, as determined by ===.
  if (actual === expected) {
    return true;

  } else if (actual instanceof Date && expected instanceof Date) {
    return actual.getTime() === expected.getTime();

  // 7.3. Other pairs that do not both pass typeof value == 'object',
  // equivalence is determined by ==.
  } else if (!actual || !expected || typeof actual != 'object' && typeof expected != 'object') {
    return opts.strict ? actual === expected : actual == expected;

  // 7.4. For all other Object pairs, including Array objects, equivalence is
  // determined by having the same number of owned properties (as verified
  // with Object.prototype.hasOwnProperty.call), the same set of keys
  // (although not necessarily the same order), equivalent values for every
  // corresponding key, and an identical 'prototype' property. Note: this
  // accounts for both named and indexed properties on Arrays.
  } else {
    return objEquiv(actual, expected, opts);
  }
}

function isUndefinedOrNull(value) {
  return value === null || value === undefined;
}

function isBuffer (x) {
  if (!x || typeof x !== 'object' || typeof x.length !== 'number') return false;
  if (typeof x.copy !== 'function' || typeof x.slice !== 'function') {
    return false;
  }
  if (x.length > 0 && typeof x[0] !== 'number') return false;
  return true;
}

function objEquiv(a, b, opts) {
  var i, key;
  if (isUndefinedOrNull(a) || isUndefinedOrNull(b))
    return false;
  // an identical 'prototype' property.
  if (a.prototype !== b.prototype) return false;
  //~~~I've managed to break Object.keys through screwy arguments passing.
  //   Converting to array solves the problem.
  if (isArguments(a)) {
    if (!isArguments(b)) {
      return false;
    }
    a = pSlice.call(a);
    b = pSlice.call(b);
    return deepEqual(a, b, opts);
  }
  if (isBuffer(a)) {
    if (!isBuffer(b)) {
      return false;
    }
    if (a.length !== b.length) return false;
    for (i = 0; i < a.length; i++) {
      if (a[i] !== b[i]) return false;
    }
    return true;
  }
  try {
    var ka = objectKeys(a),
        kb = objectKeys(b);
  } catch (e) {//happens when one is a string literal and the other isn't
    return false;
  }
  // having the same number of owned properties (keys incorporates
  // hasOwnProperty)
  if (ka.length != kb.length)
    return false;
  //the same set of keys (although not necessarily the same order),
  ka.sort();
  kb.sort();
  //~~~cheap key test
  for (i = ka.length - 1; i >= 0; i--) {
    if (ka[i] != kb[i])
      return false;
  }
  //equivalent values for every corresponding key, and
  //~~~possibly expensive deep test
  for (i = ka.length - 1; i >= 0; i--) {
    key = ka[i];
    if (!deepEqual(a[key], b[key], opts)) return false;
  }
  return typeof a === typeof b;
}

},{"./lib/is_arguments.js":4,"./lib/keys.js":5}],4:[function(require,module,exports){
var supportsArgumentsClass = (function(){
  return Object.prototype.toString.call(arguments)
})() == '[object Arguments]';

exports = module.exports = supportsArgumentsClass ? supported : unsupported;

exports.supported = supported;
function supported(object) {
  return Object.prototype.toString.call(object) == '[object Arguments]';
};

exports.unsupported = unsupported;
function unsupported(object){
  return object &&
    typeof object == 'object' &&
    typeof object.length == 'number' &&
    Object.prototype.hasOwnProperty.call(object, 'callee') &&
    !Object.prototype.propertyIsEnumerable.call(object, 'callee') ||
    false;
};

},{}],5:[function(require,module,exports){
exports = module.exports = typeof Object.keys === 'function'
  ? Object.keys : shim;

exports.shim = shim;
function shim (obj) {
  var keys = [];
  for (var key in obj) keys.push(key);
  return keys;
}

},{}],6:[function(require,module,exports){
var isFunction = require('is-function')

module.exports = forEach

var toString = Object.prototype.toString
var hasOwnProperty = Object.prototype.hasOwnProperty

function forEach(list, iterator, context) {
    if (!isFunction(iterator)) {
        throw new TypeError('iterator must be a function')
    }

    if (arguments.length < 3) {
        context = this
    }
    
    if (toString.call(list) === '[object Array]')
        forEachArray(list, iterator, context)
    else if (typeof list === 'string')
        forEachString(list, iterator, context)
    else
        forEachObject(list, iterator, context)
}

function forEachArray(array, iterator, context) {
    for (var i = 0, len = array.length; i < len; i++) {
        if (hasOwnProperty.call(array, i)) {
            iterator.call(context, array[i], i, array)
        }
    }
}

function forEachString(string, iterator, context) {
    for (var i = 0, len = string.length; i < len; i++) {
        // no such thing as a sparse string.
        iterator.call(context, string.charAt(i), i, string)
    }
}

function forEachObject(object, iterator, context) {
    for (var k in object) {
        if (hasOwnProperty.call(object, k)) {
            iterator.call(context, object[k], k, object)
        }
    }
}

},{"is-function":27}],7:[function(require,module,exports){
(function (global){
if (typeof window !== "undefined") {
    module.exports = window;
} else if (typeof global !== "undefined") {
    module.exports = global;
} else if (typeof self !== "undefined"){
    module.exports = self;
} else {
    module.exports = {};
}

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{}],8:[function(require,module,exports){
/**
 * Indicates that navigation was caused by a call to history.push.
 */
'use strict';

exports.__esModule = true;
var PUSH = 'PUSH';

exports.PUSH = PUSH;
/**
 * Indicates that navigation was caused by a call to history.replace.
 */
var REPLACE = 'REPLACE';

exports.REPLACE = REPLACE;
/**
 * Indicates that navigation was caused by some other action such
 * as using a browser's back/forward buttons and/or manually manipulating
 * the URL in a browser's location bar. This is the default.
 *
 * See https://developer.mozilla.org/en-US/docs/Web/API/WindowEventHandlers/onpopstate
 * for more information.
 */
var POP = 'POP';

exports.POP = POP;
exports['default'] = {
  PUSH: PUSH,
  REPLACE: REPLACE,
  POP: POP
};
},{}],9:[function(require,module,exports){
"use strict";

exports.__esModule = true;
var _slice = Array.prototype.slice;
exports.loopAsync = loopAsync;

function loopAsync(turns, work, callback) {
  var currentTurn = 0,
      isDone = false;
  var sync = false,
      hasNext = false,
      doneArgs = undefined;

  function done() {
    isDone = true;
    if (sync) {
      // Iterate instead of recursing if possible.
      doneArgs = [].concat(_slice.call(arguments));
      return;
    }

    callback.apply(this, arguments);
  }

  function next() {
    if (isDone) {
      return;
    }

    hasNext = true;
    if (sync) {
      // Iterate instead of recursing if possible.
      return;
    }

    sync = true;

    while (!isDone && currentTurn < turns && hasNext) {
      hasNext = false;
      work.call(this, currentTurn++, next, done);
    }

    sync = false;

    if (isDone) {
      // This means the loop finished synchronously.
      callback.apply(this, doneArgs);
      return;
    }

    if (currentTurn >= turns && hasNext) {
      isDone = true;
      callback();
    }
  }

  next();
}
},{}],10:[function(require,module,exports){
(function (process){
/*eslint-disable no-empty */
'use strict';

exports.__esModule = true;
exports.saveState = saveState;
exports.readState = readState;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var KeyPrefix = '@@History/';
var QuotaExceededErrors = ['QuotaExceededError', 'QUOTA_EXCEEDED_ERR'];

var SecurityError = 'SecurityError';

function createKey(key) {
  return KeyPrefix + key;
}

function saveState(key, state) {
  try {
    if (state == null) {
      window.sessionStorage.removeItem(createKey(key));
    } else {
      window.sessionStorage.setItem(createKey(key), JSON.stringify(state));
    }
  } catch (error) {
    if (error.name === SecurityError) {
      // Blocking cookies in Chrome/Firefox/Safari throws SecurityError on any
      // attempt to access window.sessionStorage.
      process.env.NODE_ENV !== 'production' ? _warning2['default'](false, '[history] Unable to save state; sessionStorage is not available due to security settings') : undefined;

      return;
    }

    if (QuotaExceededErrors.indexOf(error.name) >= 0 && window.sessionStorage.length === 0) {
      // Safari "private mode" throws QuotaExceededError.
      process.env.NODE_ENV !== 'production' ? _warning2['default'](false, '[history] Unable to save state; sessionStorage is not available in Safari private mode') : undefined;

      return;
    }

    throw error;
  }
}

function readState(key) {
  var json = undefined;
  try {
    json = window.sessionStorage.getItem(createKey(key));
  } catch (error) {
    if (error.name === SecurityError) {
      // Blocking cookies in Chrome/Firefox/Safari throws SecurityError on any
      // attempt to access window.sessionStorage.
      process.env.NODE_ENV !== 'production' ? _warning2['default'](false, '[history] Unable to read state; sessionStorage is not available due to security settings') : undefined;

      return null;
    }
  }

  if (json) {
    try {
      return JSON.parse(json);
    } catch (error) {
      // Ignore invalid JSON.
    }
  }

  return null;
}
}).call(this,require('_process'))

},{"_process":1,"warning":24}],11:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.addEventListener = addEventListener;
exports.removeEventListener = removeEventListener;
exports.getHashPath = getHashPath;
exports.replaceHashPath = replaceHashPath;
exports.getWindowPath = getWindowPath;
exports.go = go;
exports.getUserConfirmation = getUserConfirmation;
exports.supportsHistory = supportsHistory;
exports.supportsGoWithoutReloadUsingHash = supportsGoWithoutReloadUsingHash;

function addEventListener(node, event, listener) {
  if (node.addEventListener) {
    node.addEventListener(event, listener, false);
  } else {
    node.attachEvent('on' + event, listener);
  }
}

function removeEventListener(node, event, listener) {
  if (node.removeEventListener) {
    node.removeEventListener(event, listener, false);
  } else {
    node.detachEvent('on' + event, listener);
  }
}

function getHashPath() {
  // We can't use window.location.hash here because it's not
  // consistent across browsers - Firefox will pre-decode it!
  return window.location.href.split('#')[1] || '';
}

function replaceHashPath(path) {
  window.location.replace(window.location.pathname + window.location.search + '#' + path);
}

function getWindowPath() {
  return window.location.pathname + window.location.search + window.location.hash;
}

function go(n) {
  if (n) window.history.go(n);
}

function getUserConfirmation(message, callback) {
  callback(window.confirm(message));
}

/**
 * Returns true if the HTML5 history API is supported. Taken from Modernizr.
 *
 * https://github.com/Modernizr/Modernizr/blob/master/LICENSE
 * https://github.com/Modernizr/Modernizr/blob/master/feature-detects/history.js
 * changed to avoid false negatives for Windows Phones: https://github.com/rackt/react-router/issues/586
 */

function supportsHistory() {
  var ua = navigator.userAgent;
  if ((ua.indexOf('Android 2.') !== -1 || ua.indexOf('Android 4.0') !== -1) && ua.indexOf('Mobile Safari') !== -1 && ua.indexOf('Chrome') === -1 && ua.indexOf('Windows Phone') === -1) {
    return false;
  }
  return window.history && 'pushState' in window.history;
}

/**
 * Returns false if using go(n) with hash history causes a full page reload.
 */

function supportsGoWithoutReloadUsingHash() {
  var ua = navigator.userAgent;
  return ua.indexOf('Firefox') === -1;
}
},{}],12:[function(require,module,exports){
'use strict';

exports.__esModule = true;
var canUseDOM = !!(typeof window !== 'undefined' && window.document && window.document.createElement);
exports.canUseDOM = canUseDOM;
},{}],13:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports.extractPath = extractPath;
exports.parsePath = parsePath;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

function extractPath(string) {
  var match = string.match(/^https?:\/\/[^\/]*/);

  if (match == null) return string;

  return string.substring(match[0].length);
}

function parsePath(path) {
  var pathname = extractPath(path);
  var search = '';
  var hash = '';

  process.env.NODE_ENV !== 'production' ? _warning2['default'](path === pathname, 'A path must be pathname + search + hash only, not a fully qualified URL like "%s"', path) : undefined;

  var hashIndex = pathname.indexOf('#');
  if (hashIndex !== -1) {
    hash = pathname.substring(hashIndex);
    pathname = pathname.substring(0, hashIndex);
  }

  var searchIndex = pathname.indexOf('?');
  if (searchIndex !== -1) {
    search = pathname.substring(searchIndex);
    pathname = pathname.substring(0, searchIndex);
  }

  if (pathname === '') pathname = '/';

  return {
    pathname: pathname,
    search: search,
    hash: hash
  };
}
}).call(this,require('_process'))

},{"_process":1,"warning":24}],14:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _Actions = require('./Actions');

var _PathUtils = require('./PathUtils');

var _ExecutionEnvironment = require('./ExecutionEnvironment');

var _DOMUtils = require('./DOMUtils');

var _DOMStateStorage = require('./DOMStateStorage');

var _createDOMHistory = require('./createDOMHistory');

var _createDOMHistory2 = _interopRequireDefault(_createDOMHistory);

/**
 * Creates and returns a history object that uses HTML5's history API
 * (pushState, replaceState, and the popstate event) to manage history.
 * This is the recommended method of managing history in browsers because
 * it provides the cleanest URLs.
 *
 * Note: In browsers that do not support the HTML5 history API full
 * page reloads will be used to preserve URLs.
 */
function createBrowserHistory() {
  var options = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];

  !_ExecutionEnvironment.canUseDOM ? process.env.NODE_ENV !== 'production' ? _invariant2['default'](false, 'Browser history needs a DOM') : _invariant2['default'](false) : undefined;

  var forceRefresh = options.forceRefresh;

  var isSupported = _DOMUtils.supportsHistory();
  var useRefresh = !isSupported || forceRefresh;

  function getCurrentLocation(historyState) {
    try {
      historyState = historyState || window.history.state || {};
    } catch (e) {
      historyState = {};
    }

    var path = _DOMUtils.getWindowPath();
    var _historyState = historyState;
    var key = _historyState.key;

    var state = undefined;
    if (key) {
      state = _DOMStateStorage.readState(key);
    } else {
      state = null;
      key = history.createKey();

      if (isSupported) window.history.replaceState(_extends({}, historyState, { key: key }), null);
    }

    var location = _PathUtils.parsePath(path);

    return history.createLocation(_extends({}, location, { state: state }), undefined, key);
  }

  function startPopStateListener(_ref) {
    var transitionTo = _ref.transitionTo;

    function popStateListener(event) {
      if (event.state === undefined) return; // Ignore extraneous popstate events in WebKit.

      transitionTo(getCurrentLocation(event.state));
    }

    _DOMUtils.addEventListener(window, 'popstate', popStateListener);

    return function () {
      _DOMUtils.removeEventListener(window, 'popstate', popStateListener);
    };
  }

  function finishTransition(location) {
    var basename = location.basename;
    var pathname = location.pathname;
    var search = location.search;
    var hash = location.hash;
    var state = location.state;
    var action = location.action;
    var key = location.key;

    if (action === _Actions.POP) return; // Nothing to do.

    _DOMStateStorage.saveState(key, state);

    var path = (basename || '') + pathname + search + hash;
    var historyState = {
      key: key
    };

    if (action === _Actions.PUSH) {
      if (useRefresh) {
        window.location.href = path;
        return false; // Prevent location update.
      } else {
          window.history.pushState(historyState, null, path);
        }
    } else {
      // REPLACE
      if (useRefresh) {
        window.location.replace(path);
        return false; // Prevent location update.
      } else {
          window.history.replaceState(historyState, null, path);
        }
    }
  }

  var history = _createDOMHistory2['default'](_extends({}, options, {
    getCurrentLocation: getCurrentLocation,
    finishTransition: finishTransition,
    saveState: _DOMStateStorage.saveState
  }));

  var listenerCount = 0,
      stopPopStateListener = undefined;

  function listenBefore(listener) {
    if (++listenerCount === 1) stopPopStateListener = startPopStateListener(history);

    var unlisten = history.listenBefore(listener);

    return function () {
      unlisten();

      if (--listenerCount === 0) stopPopStateListener();
    };
  }

  function listen(listener) {
    if (++listenerCount === 1) stopPopStateListener = startPopStateListener(history);

    var unlisten = history.listen(listener);

    return function () {
      unlisten();

      if (--listenerCount === 0) stopPopStateListener();
    };
  }

  // deprecated
  function registerTransitionHook(hook) {
    if (++listenerCount === 1) stopPopStateListener = startPopStateListener(history);

    history.registerTransitionHook(hook);
  }

  // deprecated
  function unregisterTransitionHook(hook) {
    history.unregisterTransitionHook(hook);

    if (--listenerCount === 0) stopPopStateListener();
  }

  return _extends({}, history, {
    listenBefore: listenBefore,
    listen: listen,
    registerTransitionHook: registerTransitionHook,
    unregisterTransitionHook: unregisterTransitionHook
  });
}

exports['default'] = createBrowserHistory;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./Actions":8,"./DOMStateStorage":10,"./DOMUtils":11,"./ExecutionEnvironment":12,"./PathUtils":13,"./createDOMHistory":15,"_process":1,"invariant":26}],15:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _ExecutionEnvironment = require('./ExecutionEnvironment');

var _DOMUtils = require('./DOMUtils');

var _createHistory = require('./createHistory');

var _createHistory2 = _interopRequireDefault(_createHistory);

function createDOMHistory(options) {
  var history = _createHistory2['default'](_extends({
    getUserConfirmation: _DOMUtils.getUserConfirmation
  }, options, {
    go: _DOMUtils.go
  }));

  function listen(listener) {
    !_ExecutionEnvironment.canUseDOM ? process.env.NODE_ENV !== 'production' ? _invariant2['default'](false, 'DOM history needs a DOM') : _invariant2['default'](false) : undefined;

    return history.listen(listener);
  }

  return _extends({}, history, {
    listen: listen
  });
}

exports['default'] = createDOMHistory;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./DOMUtils":11,"./ExecutionEnvironment":12,"./createHistory":17,"_process":1,"invariant":26}],16:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _Actions = require('./Actions');

var _PathUtils = require('./PathUtils');

var _ExecutionEnvironment = require('./ExecutionEnvironment');

var _DOMUtils = require('./DOMUtils');

var _DOMStateStorage = require('./DOMStateStorage');

var _createDOMHistory = require('./createDOMHistory');

var _createDOMHistory2 = _interopRequireDefault(_createDOMHistory);

function isAbsolutePath(path) {
  return typeof path === 'string' && path.charAt(0) === '/';
}

function ensureSlash() {
  var path = _DOMUtils.getHashPath();

  if (isAbsolutePath(path)) return true;

  _DOMUtils.replaceHashPath('/' + path);

  return false;
}

function addQueryStringValueToPath(path, key, value) {
  return path + (path.indexOf('?') === -1 ? '?' : '&') + (key + '=' + value);
}

function stripQueryStringValueFromPath(path, key) {
  return path.replace(new RegExp('[?&]?' + key + '=[a-zA-Z0-9]+'), '');
}

function getQueryStringValueFromPath(path, key) {
  var match = path.match(new RegExp('\\?.*?\\b' + key + '=(.+?)\\b'));
  return match && match[1];
}

var DefaultQueryKey = '_k';

function createHashHistory() {
  var options = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];

  !_ExecutionEnvironment.canUseDOM ? process.env.NODE_ENV !== 'production' ? _invariant2['default'](false, 'Hash history needs a DOM') : _invariant2['default'](false) : undefined;

  var queryKey = options.queryKey;

  if (queryKey === undefined || !!queryKey) queryKey = typeof queryKey === 'string' ? queryKey : DefaultQueryKey;

  function getCurrentLocation() {
    var path = _DOMUtils.getHashPath();

    var key = undefined,
        state = undefined;
    if (queryKey) {
      key = getQueryStringValueFromPath(path, queryKey);
      path = stripQueryStringValueFromPath(path, queryKey);

      if (key) {
        state = _DOMStateStorage.readState(key);
      } else {
        state = null;
        key = history.createKey();
        _DOMUtils.replaceHashPath(addQueryStringValueToPath(path, queryKey, key));
      }
    } else {
      key = state = null;
    }

    var location = _PathUtils.parsePath(path);

    return history.createLocation(_extends({}, location, { state: state }), undefined, key);
  }

  function startHashChangeListener(_ref) {
    var transitionTo = _ref.transitionTo;

    function hashChangeListener() {
      if (!ensureSlash()) return; // Always make sure hashes are preceeded with a /.

      transitionTo(getCurrentLocation());
    }

    ensureSlash();
    _DOMUtils.addEventListener(window, 'hashchange', hashChangeListener);

    return function () {
      _DOMUtils.removeEventListener(window, 'hashchange', hashChangeListener);
    };
  }

  function finishTransition(location) {
    var basename = location.basename;
    var pathname = location.pathname;
    var search = location.search;
    var state = location.state;
    var action = location.action;
    var key = location.key;

    if (action === _Actions.POP) return; // Nothing to do.

    var path = (basename || '') + pathname + search;

    if (queryKey) {
      path = addQueryStringValueToPath(path, queryKey, key);
      _DOMStateStorage.saveState(key, state);
    } else {
      // Drop key and state.
      location.key = location.state = null;
    }

    var currentHash = _DOMUtils.getHashPath();

    if (action === _Actions.PUSH) {
      if (currentHash !== path) {
        window.location.hash = path;
      } else {
        process.env.NODE_ENV !== 'production' ? _warning2['default'](false, 'You cannot PUSH the same path using hash history') : undefined;
      }
    } else if (currentHash !== path) {
      // REPLACE
      _DOMUtils.replaceHashPath(path);
    }
  }

  var history = _createDOMHistory2['default'](_extends({}, options, {
    getCurrentLocation: getCurrentLocation,
    finishTransition: finishTransition,
    saveState: _DOMStateStorage.saveState
  }));

  var listenerCount = 0,
      stopHashChangeListener = undefined;

  function listenBefore(listener) {
    if (++listenerCount === 1) stopHashChangeListener = startHashChangeListener(history);

    var unlisten = history.listenBefore(listener);

    return function () {
      unlisten();

      if (--listenerCount === 0) stopHashChangeListener();
    };
  }

  function listen(listener) {
    if (++listenerCount === 1) stopHashChangeListener = startHashChangeListener(history);

    var unlisten = history.listen(listener);

    return function () {
      unlisten();

      if (--listenerCount === 0) stopHashChangeListener();
    };
  }

  function push(location) {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](queryKey || location.state == null, 'You cannot use state without a queryKey it will be dropped') : undefined;

    history.push(location);
  }

  function replace(location) {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](queryKey || location.state == null, 'You cannot use state without a queryKey it will be dropped') : undefined;

    history.replace(location);
  }

  var goIsSupportedWithoutReload = _DOMUtils.supportsGoWithoutReloadUsingHash();

  function go(n) {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](goIsSupportedWithoutReload, 'Hash history go(n) causes a full page reload in this browser') : undefined;

    history.go(n);
  }

  function createHref(path) {
    return '#' + history.createHref(path);
  }

  // deprecated
  function registerTransitionHook(hook) {
    if (++listenerCount === 1) stopHashChangeListener = startHashChangeListener(history);

    history.registerTransitionHook(hook);
  }

  // deprecated
  function unregisterTransitionHook(hook) {
    history.unregisterTransitionHook(hook);

    if (--listenerCount === 0) stopHashChangeListener();
  }

  // deprecated
  function pushState(state, path) {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](queryKey || state == null, 'You cannot use state without a queryKey it will be dropped') : undefined;

    history.pushState(state, path);
  }

  // deprecated
  function replaceState(state, path) {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](queryKey || state == null, 'You cannot use state without a queryKey it will be dropped') : undefined;

    history.replaceState(state, path);
  }

  return _extends({}, history, {
    listenBefore: listenBefore,
    listen: listen,
    push: push,
    replace: replace,
    go: go,
    createHref: createHref,

    registerTransitionHook: registerTransitionHook, // deprecated - warning is in createHistory
    unregisterTransitionHook: unregisterTransitionHook, // deprecated - warning is in createHistory
    pushState: pushState, // deprecated - warning is in createHistory
    replaceState: replaceState // deprecated - warning is in createHistory
  });
}

exports['default'] = createHashHistory;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./Actions":8,"./DOMStateStorage":10,"./DOMUtils":11,"./ExecutionEnvironment":12,"./PathUtils":13,"./createDOMHistory":15,"_process":1,"invariant":26,"warning":24}],17:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _deepEqual = require('deep-equal');

var _deepEqual2 = _interopRequireDefault(_deepEqual);

var _PathUtils = require('./PathUtils');

var _AsyncUtils = require('./AsyncUtils');

var _Actions = require('./Actions');

var _createLocation2 = require('./createLocation');

var _createLocation3 = _interopRequireDefault(_createLocation2);

var _runTransitionHook = require('./runTransitionHook');

var _runTransitionHook2 = _interopRequireDefault(_runTransitionHook);

var _deprecate = require('./deprecate');

var _deprecate2 = _interopRequireDefault(_deprecate);

function createRandomKey(length) {
  return Math.random().toString(36).substr(2, length);
}

function locationsAreEqual(a, b) {
  return a.pathname === b.pathname && a.search === b.search &&
  //a.action === b.action && // Different action !== location change.
  a.key === b.key && _deepEqual2['default'](a.state, b.state);
}

var DefaultKeyLength = 6;

function createHistory() {
  var options = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];
  var getCurrentLocation = options.getCurrentLocation;
  var finishTransition = options.finishTransition;
  var saveState = options.saveState;
  var go = options.go;
  var getUserConfirmation = options.getUserConfirmation;
  var keyLength = options.keyLength;

  if (typeof keyLength !== 'number') keyLength = DefaultKeyLength;

  var transitionHooks = [];

  function listenBefore(hook) {
    transitionHooks.push(hook);

    return function () {
      transitionHooks = transitionHooks.filter(function (item) {
        return item !== hook;
      });
    };
  }

  var allKeys = [];
  var changeListeners = [];
  var location = undefined;

  function getCurrent() {
    if (pendingLocation && pendingLocation.action === _Actions.POP) {
      return allKeys.indexOf(pendingLocation.key);
    } else if (location) {
      return allKeys.indexOf(location.key);
    } else {
      return -1;
    }
  }

  function updateLocation(newLocation) {
    var current = getCurrent();

    location = newLocation;

    if (location.action === _Actions.PUSH) {
      allKeys = [].concat(allKeys.slice(0, current + 1), [location.key]);
    } else if (location.action === _Actions.REPLACE) {
      allKeys[current] = location.key;
    }

    changeListeners.forEach(function (listener) {
      listener(location);
    });
  }

  function listen(listener) {
    changeListeners.push(listener);

    if (location) {
      listener(location);
    } else {
      var _location = getCurrentLocation();
      allKeys = [_location.key];
      updateLocation(_location);
    }

    return function () {
      changeListeners = changeListeners.filter(function (item) {
        return item !== listener;
      });
    };
  }

  function confirmTransitionTo(location, callback) {
    _AsyncUtils.loopAsync(transitionHooks.length, function (index, next, done) {
      _runTransitionHook2['default'](transitionHooks[index], location, function (result) {
        if (result != null) {
          done(result);
        } else {
          next();
        }
      });
    }, function (message) {
      if (getUserConfirmation && typeof message === 'string') {
        getUserConfirmation(message, function (ok) {
          callback(ok !== false);
        });
      } else {
        callback(message !== false);
      }
    });
  }

  var pendingLocation = undefined;

  function transitionTo(nextLocation) {
    if (location && locationsAreEqual(location, nextLocation)) return; // Nothing to do.

    pendingLocation = nextLocation;

    confirmTransitionTo(nextLocation, function (ok) {
      if (pendingLocation !== nextLocation) return; // Transition was interrupted.

      if (ok) {
        // treat PUSH to current path like REPLACE to be consistent with browsers
        if (nextLocation.action === _Actions.PUSH) {
          var prevPath = createPath(location);
          var nextPath = createPath(nextLocation);

          if (nextPath === prevPath && _deepEqual2['default'](location.state, nextLocation.state)) nextLocation.action = _Actions.REPLACE;
        }

        if (finishTransition(nextLocation) !== false) updateLocation(nextLocation);
      } else if (location && nextLocation.action === _Actions.POP) {
        var prevIndex = allKeys.indexOf(location.key);
        var nextIndex = allKeys.indexOf(nextLocation.key);

        if (prevIndex !== -1 && nextIndex !== -1) go(prevIndex - nextIndex); // Restore the URL.
      }
    });
  }

  function push(location) {
    transitionTo(createLocation(location, _Actions.PUSH, createKey()));
  }

  function replace(location) {
    transitionTo(createLocation(location, _Actions.REPLACE, createKey()));
  }

  function goBack() {
    go(-1);
  }

  function goForward() {
    go(1);
  }

  function createKey() {
    return createRandomKey(keyLength);
  }

  function createPath(location) {
    if (location == null || typeof location === 'string') return location;

    var pathname = location.pathname;
    var search = location.search;
    var hash = location.hash;

    var result = pathname;

    if (search) result += search;

    if (hash) result += hash;

    return result;
  }

  function createHref(location) {
    return createPath(location);
  }

  function createLocation(location, action) {
    var key = arguments.length <= 2 || arguments[2] === undefined ? createKey() : arguments[2];

    if (typeof action === 'object') {
      process.env.NODE_ENV !== 'production' ? _warning2['default'](false, 'The state (2nd) argument to history.createLocation is deprecated; use a ' + 'location descriptor instead') : undefined;

      if (typeof location === 'string') location = _PathUtils.parsePath(location);

      location = _extends({}, location, { state: action });

      action = key;
      key = arguments[3] || createKey();
    }

    return _createLocation3['default'](location, action, key);
  }

  // deprecated
  function setState(state) {
    if (location) {
      updateLocationState(location, state);
      updateLocation(location);
    } else {
      updateLocationState(getCurrentLocation(), state);
    }
  }

  function updateLocationState(location, state) {
    location.state = _extends({}, location.state, state);
    saveState(location.key, location.state);
  }

  // deprecated
  function registerTransitionHook(hook) {
    if (transitionHooks.indexOf(hook) === -1) transitionHooks.push(hook);
  }

  // deprecated
  function unregisterTransitionHook(hook) {
    transitionHooks = transitionHooks.filter(function (item) {
      return item !== hook;
    });
  }

  // deprecated
  function pushState(state, path) {
    if (typeof path === 'string') path = _PathUtils.parsePath(path);

    push(_extends({ state: state }, path));
  }

  // deprecated
  function replaceState(state, path) {
    if (typeof path === 'string') path = _PathUtils.parsePath(path);

    replace(_extends({ state: state }, path));
  }

  return {
    listenBefore: listenBefore,
    listen: listen,
    transitionTo: transitionTo,
    push: push,
    replace: replace,
    go: go,
    goBack: goBack,
    goForward: goForward,
    createKey: createKey,
    createPath: createPath,
    createHref: createHref,
    createLocation: createLocation,

    setState: _deprecate2['default'](setState, 'setState is deprecated; use location.key to save state instead'),
    registerTransitionHook: _deprecate2['default'](registerTransitionHook, 'registerTransitionHook is deprecated; use listenBefore instead'),
    unregisterTransitionHook: _deprecate2['default'](unregisterTransitionHook, 'unregisterTransitionHook is deprecated; use the callback returned from listenBefore instead'),
    pushState: _deprecate2['default'](pushState, 'pushState is deprecated; use push instead'),
    replaceState: _deprecate2['default'](replaceState, 'replaceState is deprecated; use replace instead')
  };
}

exports['default'] = createHistory;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./Actions":8,"./AsyncUtils":9,"./PathUtils":13,"./createLocation":18,"./deprecate":20,"./runTransitionHook":21,"_process":1,"deep-equal":3,"warning":24}],18:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _Actions = require('./Actions');

var _PathUtils = require('./PathUtils');

function createLocation() {
  var location = arguments.length <= 0 || arguments[0] === undefined ? '/' : arguments[0];
  var action = arguments.length <= 1 || arguments[1] === undefined ? _Actions.POP : arguments[1];
  var key = arguments.length <= 2 || arguments[2] === undefined ? null : arguments[2];

  var _fourthArg = arguments.length <= 3 || arguments[3] === undefined ? null : arguments[3];

  if (typeof location === 'string') location = _PathUtils.parsePath(location);

  if (typeof action === 'object') {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](false, 'The state (2nd) argument to createLocation is deprecated; use a ' + 'location descriptor instead') : undefined;

    location = _extends({}, location, { state: action });

    action = key || _Actions.POP;
    key = _fourthArg;
  }

  var pathname = location.pathname || '/';
  var search = location.search || '';
  var hash = location.hash || '';
  var state = location.state || null;

  return {
    pathname: pathname,
    search: search,
    hash: hash,
    state: state,
    action: action,
    key: key
  };
}

exports['default'] = createLocation;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./Actions":8,"./PathUtils":13,"_process":1,"warning":24}],19:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _PathUtils = require('./PathUtils');

var _Actions = require('./Actions');

var _createHistory = require('./createHistory');

var _createHistory2 = _interopRequireDefault(_createHistory);

function createStateStorage(entries) {
  return entries.filter(function (entry) {
    return entry.state;
  }).reduce(function (memo, entry) {
    memo[entry.key] = entry.state;
    return memo;
  }, {});
}

function createMemoryHistory() {
  var options = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];

  if (Array.isArray(options)) {
    options = { entries: options };
  } else if (typeof options === 'string') {
    options = { entries: [options] };
  }

  var history = _createHistory2['default'](_extends({}, options, {
    getCurrentLocation: getCurrentLocation,
    finishTransition: finishTransition,
    saveState: saveState,
    go: go
  }));

  var _options = options;
  var entries = _options.entries;
  var current = _options.current;

  if (typeof entries === 'string') {
    entries = [entries];
  } else if (!Array.isArray(entries)) {
    entries = ['/'];
  }

  entries = entries.map(function (entry) {
    var key = history.createKey();

    if (typeof entry === 'string') return { pathname: entry, key: key };

    if (typeof entry === 'object' && entry) return _extends({}, entry, { key: key });

    !false ? process.env.NODE_ENV !== 'production' ? _invariant2['default'](false, 'Unable to create history entry from %s', entry) : _invariant2['default'](false) : undefined;
  });

  if (current == null) {
    current = entries.length - 1;
  } else {
    !(current >= 0 && current < entries.length) ? process.env.NODE_ENV !== 'production' ? _invariant2['default'](false, 'Current index must be >= 0 and < %s, was %s', entries.length, current) : _invariant2['default'](false) : undefined;
  }

  var storage = createStateStorage(entries);

  function saveState(key, state) {
    storage[key] = state;
  }

  function readState(key) {
    return storage[key];
  }

  function getCurrentLocation() {
    var entry = entries[current];
    var basename = entry.basename;
    var pathname = entry.pathname;
    var search = entry.search;

    var path = (basename || '') + pathname + (search || '');

    var key = undefined,
        state = undefined;
    if (entry.key) {
      key = entry.key;
      state = readState(key);
    } else {
      key = history.createKey();
      state = null;
      entry.key = key;
    }

    var location = _PathUtils.parsePath(path);

    return history.createLocation(_extends({}, location, { state: state }), undefined, key);
  }

  function canGo(n) {
    var index = current + n;
    return index >= 0 && index < entries.length;
  }

  function go(n) {
    if (n) {
      if (!canGo(n)) {
        process.env.NODE_ENV !== 'production' ? _warning2['default'](false, 'Cannot go(%s) there is not enough history', n) : undefined;
        return;
      }

      current += n;

      var currentLocation = getCurrentLocation();

      // change action to POP
      history.transitionTo(_extends({}, currentLocation, { action: _Actions.POP }));
    }
  }

  function finishTransition(location) {
    switch (location.action) {
      case _Actions.PUSH:
        current += 1;

        // if we are not on the top of stack
        // remove rest and push new
        if (current < entries.length) entries.splice(current);

        entries.push(location);
        saveState(location.key, location.state);
        break;
      case _Actions.REPLACE:
        entries[current] = location;
        saveState(location.key, location.state);
        break;
    }
  }

  return history;
}

exports['default'] = createMemoryHistory;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./Actions":8,"./PathUtils":13,"./createHistory":17,"_process":1,"invariant":26,"warning":24}],20:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

function deprecate(fn, message) {
  return function () {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](false, '[history] ' + message) : undefined;
    return fn.apply(this, arguments);
  };
}

exports['default'] = deprecate;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"_process":1,"warning":24}],21:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

function runTransitionHook(hook, location, callback) {
  var result = hook(location, callback);

  if (hook.length < 2) {
    // Assume the hook runs synchronously and automatically
    // call the callback with the return value.
    callback(result);
  } else {
    process.env.NODE_ENV !== 'production' ? _warning2['default'](result === undefined, 'You should not "return" in a transition hook with a callback argument; call the callback instead') : undefined;
  }
}

exports['default'] = runTransitionHook;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"_process":1,"warning":24}],22:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _ExecutionEnvironment = require('./ExecutionEnvironment');

var _PathUtils = require('./PathUtils');

var _runTransitionHook = require('./runTransitionHook');

var _runTransitionHook2 = _interopRequireDefault(_runTransitionHook);

var _deprecate = require('./deprecate');

var _deprecate2 = _interopRequireDefault(_deprecate);

function useBasename(createHistory) {
  return function () {
    var options = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];

    var history = createHistory(options);

    var basename = options.basename;

    var checkedBaseHref = false;

    function checkBaseHref() {
      if (checkedBaseHref) {
        return;
      }

      // Automatically use the value of <base href> in HTML
      // documents as basename if it's not explicitly given.
      if (basename == null && _ExecutionEnvironment.canUseDOM) {
        var base = document.getElementsByTagName('base')[0];
        var baseHref = base && base.getAttribute('href');

        if (baseHref != null) {
          basename = baseHref;

          process.env.NODE_ENV !== 'production' ? _warning2['default'](false, 'Automatically setting basename using <base href> is deprecated and will ' + 'be removed in the next major release. The semantics of <base href> are ' + 'subtly different from basename. Please pass the basename explicitly in ' + 'the options to createHistory') : undefined;
        }
      }

      checkedBaseHref = true;
    }

    function addBasename(location) {
      checkBaseHref();

      if (basename && location.basename == null) {
        if (location.pathname.indexOf(basename) === 0) {
          location.pathname = location.pathname.substring(basename.length);
          location.basename = basename;

          if (location.pathname === '') location.pathname = '/';
        } else {
          location.basename = '';
        }
      }

      return location;
    }

    function prependBasename(location) {
      checkBaseHref();

      if (!basename) return location;

      if (typeof location === 'string') location = _PathUtils.parsePath(location);

      var pname = location.pathname;
      var normalizedBasename = basename.slice(-1) === '/' ? basename : basename + '/';
      var normalizedPathname = pname.charAt(0) === '/' ? pname.slice(1) : pname;
      var pathname = normalizedBasename + normalizedPathname;

      return _extends({}, location, {
        pathname: pathname
      });
    }

    // Override all read methods with basename-aware versions.
    function listenBefore(hook) {
      return history.listenBefore(function (location, callback) {
        _runTransitionHook2['default'](hook, addBasename(location), callback);
      });
    }

    function listen(listener) {
      return history.listen(function (location) {
        listener(addBasename(location));
      });
    }

    // Override all write methods with basename-aware versions.
    function push(location) {
      history.push(prependBasename(location));
    }

    function replace(location) {
      history.replace(prependBasename(location));
    }

    function createPath(location) {
      return history.createPath(prependBasename(location));
    }

    function createHref(location) {
      return history.createHref(prependBasename(location));
    }

    function createLocation(location) {
      for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
        args[_key - 1] = arguments[_key];
      }

      return addBasename(history.createLocation.apply(history, [prependBasename(location)].concat(args)));
    }

    // deprecated
    function pushState(state, path) {
      if (typeof path === 'string') path = _PathUtils.parsePath(path);

      push(_extends({ state: state }, path));
    }

    // deprecated
    function replaceState(state, path) {
      if (typeof path === 'string') path = _PathUtils.parsePath(path);

      replace(_extends({ state: state }, path));
    }

    return _extends({}, history, {
      listenBefore: listenBefore,
      listen: listen,
      push: push,
      replace: replace,
      createPath: createPath,
      createHref: createHref,
      createLocation: createLocation,

      pushState: _deprecate2['default'](pushState, 'pushState is deprecated; use push instead'),
      replaceState: _deprecate2['default'](replaceState, 'replaceState is deprecated; use replace instead')
    });
  };
}

exports['default'] = useBasename;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./ExecutionEnvironment":12,"./PathUtils":13,"./deprecate":20,"./runTransitionHook":21,"_process":1,"warning":24}],23:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _queryString = require('query-string');

var _runTransitionHook = require('./runTransitionHook');

var _runTransitionHook2 = _interopRequireDefault(_runTransitionHook);

var _PathUtils = require('./PathUtils');

var _deprecate = require('./deprecate');

var _deprecate2 = _interopRequireDefault(_deprecate);

var SEARCH_BASE_KEY = '$searchBase';

function defaultStringifyQuery(query) {
  return _queryString.stringify(query).replace(/%20/g, '+');
}

var defaultParseQueryString = _queryString.parse;

function isNestedObject(object) {
  for (var p in object) {
    if (Object.prototype.hasOwnProperty.call(object, p) && typeof object[p] === 'object' && !Array.isArray(object[p]) && object[p] !== null) return true;
  }return false;
}

/**
 * Returns a new createHistory function that may be used to create
 * history objects that know how to handle URL queries.
 */
function useQueries(createHistory) {
  return function () {
    var options = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];

    var history = createHistory(options);

    var stringifyQuery = options.stringifyQuery;
    var parseQueryString = options.parseQueryString;

    if (typeof stringifyQuery !== 'function') stringifyQuery = defaultStringifyQuery;

    if (typeof parseQueryString !== 'function') parseQueryString = defaultParseQueryString;

    function addQuery(location) {
      if (location.query == null) {
        var search = location.search;

        location.query = parseQueryString(search.substring(1));
        location[SEARCH_BASE_KEY] = { search: search, searchBase: '' };
      }

      // TODO: Instead of all the book-keeping here, this should just strip the
      // stringified query from the search.

      return location;
    }

    function appendQuery(location, query) {
      var _extends2;

      var searchBaseSpec = location[SEARCH_BASE_KEY];
      var queryString = query ? stringifyQuery(query) : '';
      if (!searchBaseSpec && !queryString) {
        return location;
      }

      process.env.NODE_ENV !== 'production' ? _warning2['default'](stringifyQuery !== defaultStringifyQuery || !isNestedObject(query), 'useQueries does not stringify nested query objects by default; ' + 'use a custom stringifyQuery function') : undefined;

      if (typeof location === 'string') location = _PathUtils.parsePath(location);

      var searchBase = undefined;
      if (searchBaseSpec && location.search === searchBaseSpec.search) {
        searchBase = searchBaseSpec.searchBase;
      } else {
        searchBase = location.search || '';
      }

      var search = searchBase;
      if (queryString) {
        search += (search ? '&' : '?') + queryString;
      }

      return _extends({}, location, (_extends2 = {
        search: search
      }, _extends2[SEARCH_BASE_KEY] = { search: search, searchBase: searchBase }, _extends2));
    }

    // Override all read methods with query-aware versions.
    function listenBefore(hook) {
      return history.listenBefore(function (location, callback) {
        _runTransitionHook2['default'](hook, addQuery(location), callback);
      });
    }

    function listen(listener) {
      return history.listen(function (location) {
        listener(addQuery(location));
      });
    }

    // Override all write methods with query-aware versions.
    function push(location) {
      history.push(appendQuery(location, location.query));
    }

    function replace(location) {
      history.replace(appendQuery(location, location.query));
    }

    function createPath(location, query) {
      process.env.NODE_ENV !== 'production' ? _warning2['default'](!query, 'the query argument to createPath is deprecated; use a location descriptor instead') : undefined;

      return history.createPath(appendQuery(location, query || location.query));
    }

    function createHref(location, query) {
      process.env.NODE_ENV !== 'production' ? _warning2['default'](!query, 'the query argument to createHref is deprecated; use a location descriptor instead') : undefined;

      return history.createHref(appendQuery(location, query || location.query));
    }

    function createLocation(location) {
      for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
        args[_key - 1] = arguments[_key];
      }

      var fullLocation = history.createLocation.apply(history, [appendQuery(location, location.query)].concat(args));
      if (location.query) {
        fullLocation.query = location.query;
      }
      return addQuery(fullLocation);
    }

    // deprecated
    function pushState(state, path, query) {
      if (typeof path === 'string') path = _PathUtils.parsePath(path);

      push(_extends({ state: state }, path, { query: query }));
    }

    // deprecated
    function replaceState(state, path, query) {
      if (typeof path === 'string') path = _PathUtils.parsePath(path);

      replace(_extends({ state: state }, path, { query: query }));
    }

    return _extends({}, history, {
      listenBefore: listenBefore,
      listen: listen,
      push: push,
      replace: replace,
      createPath: createPath,
      createHref: createHref,
      createLocation: createLocation,

      pushState: _deprecate2['default'](pushState, 'pushState is deprecated; use push instead'),
      replaceState: _deprecate2['default'](replaceState, 'replaceState is deprecated; use replace instead')
    });
  };
}

exports['default'] = useQueries;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./PathUtils":13,"./deprecate":20,"./runTransitionHook":21,"_process":1,"query-string":33,"warning":24}],24:[function(require,module,exports){
(function (process){
/**
 * Copyright 2014-2015, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

'use strict';

/**
 * Similar to invariant but only logs a warning if the condition is not met.
 * This can be used to log issues in development environments in critical
 * paths. Removing the logging code for production environments will keep the
 * same logic and follow the same code paths.
 */

var warning = function() {};

if (process.env.NODE_ENV !== 'production') {
  warning = function(condition, format, args) {
    var len = arguments.length;
    args = new Array(len > 2 ? len - 2 : 0);
    for (var key = 2; key < len; key++) {
      args[key - 2] = arguments[key];
    }
    if (format === undefined) {
      throw new Error(
        '`warning(condition, format, ...args)` requires a warning ' +
        'message argument'
      );
    }

    if (format.length < 10 || (/^[s\W]*$/).test(format)) {
      throw new Error(
        'The warning format should be able to uniquely identify this ' +
        'warning. Please, use a more descriptive format than: ' + format
      );
    }

    if (!condition) {
      var argIndex = 0;
      var message = 'Warning: ' +
        format.replace(/%s/g, function() {
          return args[argIndex++];
        });
      if (typeof console !== 'undefined') {
        console.error(message);
      }
      try {
        // This error was thrown as a convenience so that you can use this stack
        // to find the callsite that caused this warning to fire.
        throw new Error(message);
      } catch(x) {}
    }
  };
}

module.exports = warning;

}).call(this,require('_process'))

},{"_process":1}],25:[function(require,module,exports){
/**
 * Copyright 2015, Yahoo! Inc.
 * Copyrights licensed under the New BSD License. See the accompanying LICENSE file for terms.
 */
'use strict';

var REACT_STATICS = {
    childContextTypes: true,
    contextTypes: true,
    defaultProps: true,
    displayName: true,
    getDefaultProps: true,
    mixins: true,
    propTypes: true,
    type: true
};

var KNOWN_STATICS = {
    name: true,
    length: true,
    prototype: true,
    caller: true,
    arguments: true,
    arity: true
};

var isGetOwnPropertySymbolsAvailable = typeof Object.getOwnPropertySymbols === 'function';

module.exports = function hoistNonReactStatics(targetComponent, sourceComponent, customStatics) {
    if (typeof sourceComponent !== 'string') { // don't hoist over string (html) components
        var keys = Object.getOwnPropertyNames(sourceComponent);

        /* istanbul ignore else */
        if (isGetOwnPropertySymbolsAvailable) {
            keys = keys.concat(Object.getOwnPropertySymbols(sourceComponent));
        }

        for (var i = 0; i < keys.length; ++i) {
            if (!REACT_STATICS[keys[i]] && !KNOWN_STATICS[keys[i]] && (!customStatics || !customStatics[keys[i]])) {
                try {
                    targetComponent[keys[i]] = sourceComponent[keys[i]];
                } catch (error) {

                }
            }
        }
    }

    return targetComponent;
};

},{}],26:[function(require,module,exports){
(function (process){
/**
 * Copyright 2013-2015, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

'use strict';

/**
 * Use invariant() to assert state which your program assumes to be true.
 *
 * Provide sprintf-style format (only %s is supported) and arguments
 * to provide information about what broke and what you were
 * expecting.
 *
 * The invariant message will be stripped in production, but the invariant
 * will remain to ensure logic does not differ in production.
 */

var invariant = function(condition, format, a, b, c, d, e, f) {
  if (process.env.NODE_ENV !== 'production') {
    if (format === undefined) {
      throw new Error('invariant requires an error message argument');
    }
  }

  if (!condition) {
    var error;
    if (format === undefined) {
      error = new Error(
        'Minified exception occurred; use the non-minified dev environment ' +
        'for the full error message and additional helpful warnings.'
      );
    } else {
      var args = [a, b, c, d, e, f];
      var argIndex = 0;
      error = new Error(
        format.replace(/%s/g, function() { return args[argIndex++]; })
      );
      error.name = 'Invariant Violation';
    }

    error.framesToPop = 1; // we don't care about invariant's own frame
    throw error;
  }
};

module.exports = invariant;

}).call(this,require('_process'))

},{"_process":1}],27:[function(require,module,exports){
module.exports = isFunction

var toString = Object.prototype.toString

function isFunction (fn) {
  var string = toString.call(fn)
  return string === '[object Function]' ||
    (typeof fn === 'function' && string !== '[object RegExp]') ||
    (typeof window !== 'undefined' &&
     // IE8 and below
     (fn === window.setTimeout ||
      fn === window.alert ||
      fn === window.confirm ||
      fn === window.prompt))
};

},{}],28:[function(require,module,exports){
var overArg = require('./_overArg');

/** Built-in value references. */
var getPrototype = overArg(Object.getPrototypeOf, Object);

module.exports = getPrototype;

},{"./_overArg":29}],29:[function(require,module,exports){
/**
 * Creates a unary function that invokes `func` with its argument transformed.
 *
 * @private
 * @param {Function} func The function to wrap.
 * @param {Function} transform The argument transform.
 * @returns {Function} Returns the new function.
 */
function overArg(func, transform) {
  return function(arg) {
    return func(transform(arg));
  };
}

module.exports = overArg;

},{}],30:[function(require,module,exports){
/**
 * Checks if `value` is object-like. A value is object-like if it's not `null`
 * and has a `typeof` result of "object".
 *
 * @static
 * @memberOf _
 * @since 4.0.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
 * @example
 *
 * _.isObjectLike({});
 * // => true
 *
 * _.isObjectLike([1, 2, 3]);
 * // => true
 *
 * _.isObjectLike(_.noop);
 * // => false
 *
 * _.isObjectLike(null);
 * // => false
 */
function isObjectLike(value) {
  return value != null && typeof value == 'object';
}

module.exports = isObjectLike;

},{}],31:[function(require,module,exports){
var getPrototype = require('./_getPrototype'),
    isObjectLike = require('./isObjectLike');

/** `Object#toString` result references. */
var objectTag = '[object Object]';

/** Used for built-in method references. */
var funcProto = Function.prototype,
    objectProto = Object.prototype;

/** Used to resolve the decompiled source of functions. */
var funcToString = funcProto.toString;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/** Used to infer the `Object` constructor. */
var objectCtorString = funcToString.call(Object);

/**
 * Used to resolve the
 * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
 * of values.
 */
var objectToString = objectProto.toString;

/**
 * Checks if `value` is a plain object, that is, an object created by the
 * `Object` constructor or one with a `[[Prototype]]` of `null`.
 *
 * @static
 * @memberOf _
 * @since 0.8.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a plain object, else `false`.
 * @example
 *
 * function Foo() {
 *   this.a = 1;
 * }
 *
 * _.isPlainObject(new Foo);
 * // => false
 *
 * _.isPlainObject([1, 2, 3]);
 * // => false
 *
 * _.isPlainObject({ 'x': 0, 'y': 0 });
 * // => true
 *
 * _.isPlainObject(Object.create(null));
 * // => true
 */
function isPlainObject(value) {
  if (!isObjectLike(value) || objectToString.call(value) != objectTag) {
    return false;
  }
  var proto = getPrototype(value);
  if (proto === null) {
    return true;
  }
  var Ctor = hasOwnProperty.call(proto, 'constructor') && proto.constructor;
  return (typeof Ctor == 'function' &&
    Ctor instanceof Ctor && funcToString.call(Ctor) == objectCtorString);
}

module.exports = isPlainObject;

},{"./_getPrototype":28,"./isObjectLike":30}],32:[function(require,module,exports){
var trim = require('trim')
  , forEach = require('for-each')
  , isArray = function(arg) {
      return Object.prototype.toString.call(arg) === '[object Array]';
    }

module.exports = function (headers) {
  if (!headers)
    return {}

  var result = {}

  forEach(
      trim(headers).split('\n')
    , function (row) {
        var index = row.indexOf(':')
          , key = trim(row.slice(0, index)).toLowerCase()
          , value = trim(row.slice(index + 1))

        if (typeof(result[key]) === 'undefined') {
          result[key] = value
        } else if (isArray(result[key])) {
          result[key].push(value)
        } else {
          result[key] = [ result[key], value ]
        }
      }
  )

  return result
}
},{"for-each":6,"trim":91}],33:[function(require,module,exports){
'use strict';
var strictUriEncode = require('strict-uri-encode');

exports.extract = function (str) {
	return str.split('?')[1] || '';
};

exports.parse = function (str) {
	if (typeof str !== 'string') {
		return {};
	}

	str = str.trim().replace(/^(\?|#|&)/, '');

	if (!str) {
		return {};
	}

	return str.split('&').reduce(function (ret, param) {
		var parts = param.replace(/\+/g, ' ').split('=');
		// Firefox (pre 40) decodes `%3D` to `=`
		// https://github.com/sindresorhus/query-string/pull/37
		var key = parts.shift();
		var val = parts.length > 0 ? parts.join('=') : undefined;

		key = decodeURIComponent(key);

		// missing `=` should be `null`:
		// http://w3.org/TR/2012/WD-url-20120524/#collect-url-parameters
		val = val === undefined ? null : decodeURIComponent(val);

		if (!ret.hasOwnProperty(key)) {
			ret[key] = val;
		} else if (Array.isArray(ret[key])) {
			ret[key].push(val);
		} else {
			ret[key] = [ret[key], val];
		}

		return ret;
	}, {});
};

exports.stringify = function (obj) {
	return obj ? Object.keys(obj).sort().map(function (key) {
		var val = obj[key];

		if (val === undefined) {
			return '';
		}

		if (val === null) {
			return key;
		}

		if (Array.isArray(val)) {
			return val.slice().sort().map(function (val2) {
				return strictUriEncode(key) + '=' + strictUriEncode(val2);
			}).join('&');
		}

		return strictUriEncode(key) + '=' + strictUriEncode(val);
	}).filter(function (x) {
		return x.length > 0;
	}).join('&') : '';
};

},{"strict-uri-encode":87}],34:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports["default"] = undefined;

var _react = require('react');

var _storeShape = require('../utils/storeShape');

var _storeShape2 = _interopRequireDefault(_storeShape);

var _warning = require('../utils/warning');

var _warning2 = _interopRequireDefault(_warning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var didWarnAboutReceivingStore = false;
function warnAboutReceivingStore() {
  if (didWarnAboutReceivingStore) {
    return;
  }
  didWarnAboutReceivingStore = true;

  (0, _warning2["default"])('<Provider> does not support changing `store` on the fly. ' + 'It is most likely that you see this error because you updated to ' + 'Redux 2.x and React Redux 2.x which no longer hot reload reducers ' + 'automatically. See https://github.com/reactjs/react-redux/releases/' + 'tag/v2.0.0 for the migration instructions.');
}

var Provider = function (_Component) {
  _inherits(Provider, _Component);

  Provider.prototype.getChildContext = function getChildContext() {
    return { store: this.store };
  };

  function Provider(props, context) {
    _classCallCheck(this, Provider);

    var _this = _possibleConstructorReturn(this, _Component.call(this, props, context));

    _this.store = props.store;
    return _this;
  }

  Provider.prototype.render = function render() {
    var children = this.props.children;

    return _react.Children.only(children);
  };

  return Provider;
}(_react.Component);

exports["default"] = Provider;

if (process.env.NODE_ENV !== 'production') {
  Provider.prototype.componentWillReceiveProps = function (nextProps) {
    var store = this.store;
    var nextStore = nextProps.store;

    if (store !== nextStore) {
      warnAboutReceivingStore();
    }
  };
}

Provider.propTypes = {
  store: _storeShape2["default"].isRequired,
  children: _react.PropTypes.element.isRequired
};
Provider.childContextTypes = {
  store: _storeShape2["default"].isRequired
};
}).call(this,require('_process'))

},{"../utils/storeShape":38,"../utils/warning":39,"_process":1,"react":"react"}],35:[function(require,module,exports){
(function (process){
'use strict';

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.__esModule = true;
exports["default"] = connect;

var _react = require('react');

var _storeShape = require('../utils/storeShape');

var _storeShape2 = _interopRequireDefault(_storeShape);

var _shallowEqual = require('../utils/shallowEqual');

var _shallowEqual2 = _interopRequireDefault(_shallowEqual);

var _wrapActionCreators = require('../utils/wrapActionCreators');

var _wrapActionCreators2 = _interopRequireDefault(_wrapActionCreators);

var _warning = require('../utils/warning');

var _warning2 = _interopRequireDefault(_warning);

var _isPlainObject = require('lodash/isPlainObject');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _hoistNonReactStatics = require('hoist-non-react-statics');

var _hoistNonReactStatics2 = _interopRequireDefault(_hoistNonReactStatics);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var defaultMapStateToProps = function defaultMapStateToProps(state) {
  return {};
}; // eslint-disable-line no-unused-vars
var defaultMapDispatchToProps = function defaultMapDispatchToProps(dispatch) {
  return { dispatch: dispatch };
};
var defaultMergeProps = function defaultMergeProps(stateProps, dispatchProps, parentProps) {
  return _extends({}, parentProps, stateProps, dispatchProps);
};

function getDisplayName(WrappedComponent) {
  return WrappedComponent.displayName || WrappedComponent.name || 'Component';
}

var errorObject = { value: null };
function tryCatch(fn, ctx) {
  try {
    return fn.apply(ctx);
  } catch (e) {
    errorObject.value = e;
    return errorObject;
  }
}

// Helps track hot reloading.
var nextVersion = 0;

function connect(mapStateToProps, mapDispatchToProps, mergeProps) {
  var options = arguments.length <= 3 || arguments[3] === undefined ? {} : arguments[3];

  var shouldSubscribe = Boolean(mapStateToProps);
  var mapState = mapStateToProps || defaultMapStateToProps;

  var mapDispatch = undefined;
  if (typeof mapDispatchToProps === 'function') {
    mapDispatch = mapDispatchToProps;
  } else if (!mapDispatchToProps) {
    mapDispatch = defaultMapDispatchToProps;
  } else {
    mapDispatch = (0, _wrapActionCreators2["default"])(mapDispatchToProps);
  }

  var finalMergeProps = mergeProps || defaultMergeProps;
  var _options$pure = options.pure;
  var pure = _options$pure === undefined ? true : _options$pure;
  var _options$withRef = options.withRef;
  var withRef = _options$withRef === undefined ? false : _options$withRef;

  var checkMergedEquals = pure && finalMergeProps !== defaultMergeProps;

  // Helps track hot reloading.
  var version = nextVersion++;

  return function wrapWithConnect(WrappedComponent) {
    var connectDisplayName = 'Connect(' + getDisplayName(WrappedComponent) + ')';

    function checkStateShape(props, methodName) {
      if (!(0, _isPlainObject2["default"])(props)) {
        (0, _warning2["default"])(methodName + '() in ' + connectDisplayName + ' must return a plain object. ' + ('Instead received ' + props + '.'));
      }
    }

    function computeMergedProps(stateProps, dispatchProps, parentProps) {
      var mergedProps = finalMergeProps(stateProps, dispatchProps, parentProps);
      if (process.env.NODE_ENV !== 'production') {
        checkStateShape(mergedProps, 'mergeProps');
      }
      return mergedProps;
    }

    var Connect = function (_Component) {
      _inherits(Connect, _Component);

      Connect.prototype.shouldComponentUpdate = function shouldComponentUpdate() {
        return !pure || this.haveOwnPropsChanged || this.hasStoreStateChanged;
      };

      function Connect(props, context) {
        _classCallCheck(this, Connect);

        var _this = _possibleConstructorReturn(this, _Component.call(this, props, context));

        _this.version = version;
        _this.store = props.store || context.store;

        (0, _invariant2["default"])(_this.store, 'Could not find "store" in either the context or ' + ('props of "' + connectDisplayName + '". ') + 'Either wrap the root component in a <Provider>, ' + ('or explicitly pass "store" as a prop to "' + connectDisplayName + '".'));

        var storeState = _this.store.getState();
        _this.state = { storeState: storeState };
        _this.clearCache();
        return _this;
      }

      Connect.prototype.computeStateProps = function computeStateProps(store, props) {
        if (!this.finalMapStateToProps) {
          return this.configureFinalMapState(store, props);
        }

        var state = store.getState();
        var stateProps = this.doStatePropsDependOnOwnProps ? this.finalMapStateToProps(state, props) : this.finalMapStateToProps(state);

        if (process.env.NODE_ENV !== 'production') {
          checkStateShape(stateProps, 'mapStateToProps');
        }
        return stateProps;
      };

      Connect.prototype.configureFinalMapState = function configureFinalMapState(store, props) {
        var mappedState = mapState(store.getState(), props);
        var isFactory = typeof mappedState === 'function';

        this.finalMapStateToProps = isFactory ? mappedState : mapState;
        this.doStatePropsDependOnOwnProps = this.finalMapStateToProps.length !== 1;

        if (isFactory) {
          return this.computeStateProps(store, props);
        }

        if (process.env.NODE_ENV !== 'production') {
          checkStateShape(mappedState, 'mapStateToProps');
        }
        return mappedState;
      };

      Connect.prototype.computeDispatchProps = function computeDispatchProps(store, props) {
        if (!this.finalMapDispatchToProps) {
          return this.configureFinalMapDispatch(store, props);
        }

        var dispatch = store.dispatch;

        var dispatchProps = this.doDispatchPropsDependOnOwnProps ? this.finalMapDispatchToProps(dispatch, props) : this.finalMapDispatchToProps(dispatch);

        if (process.env.NODE_ENV !== 'production') {
          checkStateShape(dispatchProps, 'mapDispatchToProps');
        }
        return dispatchProps;
      };

      Connect.prototype.configureFinalMapDispatch = function configureFinalMapDispatch(store, props) {
        var mappedDispatch = mapDispatch(store.dispatch, props);
        var isFactory = typeof mappedDispatch === 'function';

        this.finalMapDispatchToProps = isFactory ? mappedDispatch : mapDispatch;
        this.doDispatchPropsDependOnOwnProps = this.finalMapDispatchToProps.length !== 1;

        if (isFactory) {
          return this.computeDispatchProps(store, props);
        }

        if (process.env.NODE_ENV !== 'production') {
          checkStateShape(mappedDispatch, 'mapDispatchToProps');
        }
        return mappedDispatch;
      };

      Connect.prototype.updateStatePropsIfNeeded = function updateStatePropsIfNeeded() {
        var nextStateProps = this.computeStateProps(this.store, this.props);
        if (this.stateProps && (0, _shallowEqual2["default"])(nextStateProps, this.stateProps)) {
          return false;
        }

        this.stateProps = nextStateProps;
        return true;
      };

      Connect.prototype.updateDispatchPropsIfNeeded = function updateDispatchPropsIfNeeded() {
        var nextDispatchProps = this.computeDispatchProps(this.store, this.props);
        if (this.dispatchProps && (0, _shallowEqual2["default"])(nextDispatchProps, this.dispatchProps)) {
          return false;
        }

        this.dispatchProps = nextDispatchProps;
        return true;
      };

      Connect.prototype.updateMergedPropsIfNeeded = function updateMergedPropsIfNeeded() {
        var nextMergedProps = computeMergedProps(this.stateProps, this.dispatchProps, this.props);
        if (this.mergedProps && checkMergedEquals && (0, _shallowEqual2["default"])(nextMergedProps, this.mergedProps)) {
          return false;
        }

        this.mergedProps = nextMergedProps;
        return true;
      };

      Connect.prototype.isSubscribed = function isSubscribed() {
        return typeof this.unsubscribe === 'function';
      };

      Connect.prototype.trySubscribe = function trySubscribe() {
        if (shouldSubscribe && !this.unsubscribe) {
          this.unsubscribe = this.store.subscribe(this.handleChange.bind(this));
          this.handleChange();
        }
      };

      Connect.prototype.tryUnsubscribe = function tryUnsubscribe() {
        if (this.unsubscribe) {
          this.unsubscribe();
          this.unsubscribe = null;
        }
      };

      Connect.prototype.componentDidMount = function componentDidMount() {
        this.trySubscribe();
      };

      Connect.prototype.componentWillReceiveProps = function componentWillReceiveProps(nextProps) {
        if (!pure || !(0, _shallowEqual2["default"])(nextProps, this.props)) {
          this.haveOwnPropsChanged = true;
        }
      };

      Connect.prototype.componentWillUnmount = function componentWillUnmount() {
        this.tryUnsubscribe();
        this.clearCache();
      };

      Connect.prototype.clearCache = function clearCache() {
        this.dispatchProps = null;
        this.stateProps = null;
        this.mergedProps = null;
        this.haveOwnPropsChanged = true;
        this.hasStoreStateChanged = true;
        this.haveStatePropsBeenPrecalculated = false;
        this.statePropsPrecalculationError = null;
        this.renderedElement = null;
        this.finalMapDispatchToProps = null;
        this.finalMapStateToProps = null;
      };

      Connect.prototype.handleChange = function handleChange() {
        if (!this.unsubscribe) {
          return;
        }

        var storeState = this.store.getState();
        var prevStoreState = this.state.storeState;
        if (pure && prevStoreState === storeState) {
          return;
        }

        if (pure && !this.doStatePropsDependOnOwnProps) {
          var haveStatePropsChanged = tryCatch(this.updateStatePropsIfNeeded, this);
          if (!haveStatePropsChanged) {
            return;
          }
          if (haveStatePropsChanged === errorObject) {
            this.statePropsPrecalculationError = errorObject.value;
          }
          this.haveStatePropsBeenPrecalculated = true;
        }

        this.hasStoreStateChanged = true;
        this.setState({ storeState: storeState });
      };

      Connect.prototype.getWrappedInstance = function getWrappedInstance() {
        (0, _invariant2["default"])(withRef, 'To access the wrapped instance, you need to specify ' + '{ withRef: true } as the fourth argument of the connect() call.');

        return this.refs.wrappedInstance;
      };

      Connect.prototype.render = function render() {
        var haveOwnPropsChanged = this.haveOwnPropsChanged;
        var hasStoreStateChanged = this.hasStoreStateChanged;
        var haveStatePropsBeenPrecalculated = this.haveStatePropsBeenPrecalculated;
        var statePropsPrecalculationError = this.statePropsPrecalculationError;
        var renderedElement = this.renderedElement;

        this.haveOwnPropsChanged = false;
        this.hasStoreStateChanged = false;
        this.haveStatePropsBeenPrecalculated = false;
        this.statePropsPrecalculationError = null;

        if (statePropsPrecalculationError) {
          throw statePropsPrecalculationError;
        }

        var shouldUpdateStateProps = true;
        var shouldUpdateDispatchProps = true;
        if (pure && renderedElement) {
          shouldUpdateStateProps = hasStoreStateChanged || haveOwnPropsChanged && this.doStatePropsDependOnOwnProps;
          shouldUpdateDispatchProps = haveOwnPropsChanged && this.doDispatchPropsDependOnOwnProps;
        }

        var haveStatePropsChanged = false;
        var haveDispatchPropsChanged = false;
        if (haveStatePropsBeenPrecalculated) {
          haveStatePropsChanged = true;
        } else if (shouldUpdateStateProps) {
          haveStatePropsChanged = this.updateStatePropsIfNeeded();
        }
        if (shouldUpdateDispatchProps) {
          haveDispatchPropsChanged = this.updateDispatchPropsIfNeeded();
        }

        var haveMergedPropsChanged = true;
        if (haveStatePropsChanged || haveDispatchPropsChanged || haveOwnPropsChanged) {
          haveMergedPropsChanged = this.updateMergedPropsIfNeeded();
        } else {
          haveMergedPropsChanged = false;
        }

        if (!haveMergedPropsChanged && renderedElement) {
          return renderedElement;
        }

        if (withRef) {
          this.renderedElement = (0, _react.createElement)(WrappedComponent, _extends({}, this.mergedProps, {
            ref: 'wrappedInstance'
          }));
        } else {
          this.renderedElement = (0, _react.createElement)(WrappedComponent, this.mergedProps);
        }

        return this.renderedElement;
      };

      return Connect;
    }(_react.Component);

    Connect.displayName = connectDisplayName;
    Connect.WrappedComponent = WrappedComponent;
    Connect.contextTypes = {
      store: _storeShape2["default"]
    };
    Connect.propTypes = {
      store: _storeShape2["default"]
    };

    if (process.env.NODE_ENV !== 'production') {
      Connect.prototype.componentWillUpdate = function componentWillUpdate() {
        if (this.version === version) {
          return;
        }

        // We are hot reloading!
        this.version = version;
        this.trySubscribe();
        this.clearCache();
      };
    }

    return (0, _hoistNonReactStatics2["default"])(Connect, WrappedComponent);
  };
}
}).call(this,require('_process'))

},{"../utils/shallowEqual":37,"../utils/storeShape":38,"../utils/warning":39,"../utils/wrapActionCreators":40,"_process":1,"hoist-non-react-statics":25,"invariant":26,"lodash/isPlainObject":31,"react":"react"}],36:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.connect = exports.Provider = undefined;

var _Provider = require('./components/Provider');

var _Provider2 = _interopRequireDefault(_Provider);

var _connect = require('./components/connect');

var _connect2 = _interopRequireDefault(_connect);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

exports.Provider = _Provider2["default"];
exports.connect = _connect2["default"];
},{"./components/Provider":34,"./components/connect":35}],37:[function(require,module,exports){
"use strict";

exports.__esModule = true;
exports["default"] = shallowEqual;
function shallowEqual(objA, objB) {
  if (objA === objB) {
    return true;
  }

  var keysA = Object.keys(objA);
  var keysB = Object.keys(objB);

  if (keysA.length !== keysB.length) {
    return false;
  }

  // Test for A's keys different from B.
  var hasOwn = Object.prototype.hasOwnProperty;
  for (var i = 0; i < keysA.length; i++) {
    if (!hasOwn.call(objB, keysA[i]) || objA[keysA[i]] !== objB[keysA[i]]) {
      return false;
    }
  }

  return true;
}
},{}],38:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _react = require('react');

exports["default"] = _react.PropTypes.shape({
  subscribe: _react.PropTypes.func.isRequired,
  dispatch: _react.PropTypes.func.isRequired,
  getState: _react.PropTypes.func.isRequired
});
},{"react":"react"}],39:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports["default"] = warning;
/**
 * Prints a warning in the console if it exists.
 *
 * @param {String} message The warning message.
 * @returns {void}
 */
function warning(message) {
  /* eslint-disable no-console */
  if (typeof console !== 'undefined' && typeof console.error === 'function') {
    console.error(message);
  }
  /* eslint-enable no-console */
  try {
    // This error was thrown as a convenience so that you can use this stack
    // to find the callsite that caused this warning to fire.
    throw new Error(message);
    /* eslint-disable no-empty */
  } catch (e) {}
  /* eslint-enable no-empty */
}
},{}],40:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports["default"] = wrapActionCreators;

var _redux = require('redux');

function wrapActionCreators(actionCreators) {
  return function (dispatch) {
    return (0, _redux.bindActionCreators)(actionCreators, dispatch);
  };
}
},{"redux":85}],41:[function(require,module,exports){
"use strict";

exports.__esModule = true;
exports.loopAsync = loopAsync;
exports.mapAsync = mapAsync;
function loopAsync(turns, work, callback) {
  var currentTurn = 0,
      isDone = false;
  var sync = false,
      hasNext = false,
      doneArgs = void 0;

  function done() {
    isDone = true;
    if (sync) {
      // Iterate instead of recursing if possible.
      doneArgs = [].concat(Array.prototype.slice.call(arguments));
      return;
    }

    callback.apply(this, arguments);
  }

  function next() {
    if (isDone) {
      return;
    }

    hasNext = true;
    if (sync) {
      // Iterate instead of recursing if possible.
      return;
    }

    sync = true;

    while (!isDone && currentTurn < turns && hasNext) {
      hasNext = false;
      work.call(this, currentTurn++, next, done);
    }

    sync = false;

    if (isDone) {
      // This means the loop finished synchronously.
      callback.apply(this, doneArgs);
      return;
    }

    if (currentTurn >= turns && hasNext) {
      isDone = true;
      callback();
    }
  }

  next();
}

function mapAsync(array, work, callback) {
  var length = array.length;
  var values = [];

  if (length === 0) return callback(null, values);

  var isDone = false,
      doneCount = 0;

  function done(index, error, value) {
    if (isDone) return;

    if (error) {
      isDone = true;
      callback(error);
    } else {
      values[index] = value;

      isDone = ++doneCount === length;

      if (isDone) callback(null, values);
    }
  }

  array.forEach(function (item, index) {
    work(item, index, function (error, value) {
      done(index, error, value);
    });
  });
}
},{}],42:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _InternalPropTypes = require('./InternalPropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/**
 * A mixin that adds the "history" instance variable to components.
 */
var History = {

  contextTypes: {
    history: _InternalPropTypes.history
  },

  componentWillMount: function componentWillMount() {
    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'the `History` mixin is deprecated, please access `context.router` with your own `contextTypes`. http://tiny.cc/router-historymixin') : void 0;
    this.history = this.context.history;
  }
};

exports.default = History;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./InternalPropTypes":46,"./routerWarning":75,"_process":1}],43:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Link = require('./Link');

var _Link2 = _interopRequireDefault(_Link);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/**
 * An <IndexLink> is used to link to an <IndexRoute>.
 */
var IndexLink = _react2.default.createClass({
  displayName: 'IndexLink',
  render: function render() {
    return _react2.default.createElement(_Link2.default, _extends({}, this.props, { onlyActiveOnIndex: true }));
  }
});

exports.default = IndexLink;
module.exports = exports['default'];
},{"./Link":48,"react":"react"}],44:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _Redirect = require('./Redirect');

var _Redirect2 = _interopRequireDefault(_Redirect);

var _InternalPropTypes = require('./InternalPropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _React$PropTypes = _react2.default.PropTypes;
var string = _React$PropTypes.string;
var object = _React$PropTypes.object;

/**
 * An <IndexRedirect> is used to redirect from an indexRoute.
 */

var IndexRedirect = _react2.default.createClass({
  displayName: 'IndexRedirect',


  statics: {
    createRouteFromReactElement: function createRouteFromReactElement(element, parentRoute) {
      /* istanbul ignore else: sanity check */
      if (parentRoute) {
        parentRoute.indexRoute = _Redirect2.default.createRouteFromReactElement(element);
      } else {
        process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'An <IndexRedirect> does not make sense at the root of your route config') : void 0;
      }
    }
  },

  propTypes: {
    to: string.isRequired,
    query: object,
    state: object,
    onEnter: _InternalPropTypes.falsy,
    children: _InternalPropTypes.falsy
  },

  /* istanbul ignore next: sanity check */
  render: function render() {
    !false ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, '<IndexRedirect> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = IndexRedirect;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./InternalPropTypes":46,"./Redirect":51,"./routerWarning":75,"_process":1,"invariant":26,"react":"react"}],45:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _RouteUtils = require('./RouteUtils');

var _InternalPropTypes = require('./InternalPropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var func = _react2.default.PropTypes.func;

/**
 * An <IndexRoute> is used to specify its parent's <Route indexRoute> in
 * a JSX route config.
 */

var IndexRoute = _react2.default.createClass({
  displayName: 'IndexRoute',


  statics: {
    createRouteFromReactElement: function createRouteFromReactElement(element, parentRoute) {
      /* istanbul ignore else: sanity check */
      if (parentRoute) {
        parentRoute.indexRoute = (0, _RouteUtils.createRouteFromReactElement)(element);
      } else {
        process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'An <IndexRoute> does not make sense at the root of your route config') : void 0;
      }
    }
  },

  propTypes: {
    path: _InternalPropTypes.falsy,
    component: _InternalPropTypes.component,
    components: _InternalPropTypes.components,
    getComponent: func,
    getComponents: func
  },

  /* istanbul ignore next: sanity check */
  render: function render() {
    !false ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, '<IndexRoute> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = IndexRoute;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./InternalPropTypes":46,"./RouteUtils":54,"./routerWarning":75,"_process":1,"invariant":26,"react":"react"}],46:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.routes = exports.route = exports.components = exports.component = exports.history = undefined;
exports.falsy = falsy;

var _react = require('react');

var func = _react.PropTypes.func;
var object = _react.PropTypes.object;
var arrayOf = _react.PropTypes.arrayOf;
var oneOfType = _react.PropTypes.oneOfType;
var element = _react.PropTypes.element;
var shape = _react.PropTypes.shape;
var string = _react.PropTypes.string;
function falsy(props, propName, componentName) {
  if (props[propName]) return new Error('<' + componentName + '> should not have a "' + propName + '" prop');
}

var history = exports.history = shape({
  listen: func.isRequired,
  push: func.isRequired,
  replace: func.isRequired,
  go: func.isRequired,
  goBack: func.isRequired,
  goForward: func.isRequired
});

var component = exports.component = oneOfType([func, string]);
var components = exports.components = oneOfType([component, object]);
var route = exports.route = oneOfType([object, element]);
var routes = exports.routes = oneOfType([route, arrayOf(route)]);
},{"react":"react"}],47:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var object = _react2.default.PropTypes.object;

/**
 * The Lifecycle mixin adds the routerWillLeave lifecycle method to a
 * component that may be used to cancel a transition or prompt the user
 * for confirmation.
 *
 * On standard transitions, routerWillLeave receives a single argument: the
 * location we're transitioning to. To cancel the transition, return false.
 * To prompt the user for confirmation, return a prompt message (string).
 *
 * During the beforeunload event (assuming you're using the useBeforeUnload
 * history enhancer), routerWillLeave does not receive a location object
 * because it isn't possible for us to know the location we're transitioning
 * to. In this case routerWillLeave must return a prompt message to prevent
 * the user from closing the window/tab.
 */

var Lifecycle = {

  contextTypes: {
    history: object.isRequired,
    // Nested children receive the route as context, either
    // set by the route component using the RouteContext mixin
    // or by some other ancestor.
    route: object
  },

  propTypes: {
    // Route components receive the route object as a prop.
    route: object
  },

  componentDidMount: function componentDidMount() {
    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'the `Lifecycle` mixin is deprecated, please use `context.router.setRouteLeaveHook(route, hook)`. http://tiny.cc/router-lifecyclemixin') : void 0;
    !this.routerWillLeave ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'The Lifecycle mixin requires you to define a routerWillLeave method') : (0, _invariant2.default)(false) : void 0;

    var route = this.props.route || this.context.route;

    !route ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'The Lifecycle mixin must be used on either a) a <Route component> or ' + 'b) a descendant of a <Route component> that uses the RouteContext mixin') : (0, _invariant2.default)(false) : void 0;

    this._unlistenBeforeLeavingRoute = this.context.history.listenBeforeLeavingRoute(route, this.routerWillLeave);
  },
  componentWillUnmount: function componentWillUnmount() {
    if (this._unlistenBeforeLeavingRoute) this._unlistenBeforeLeavingRoute();
  }
};

exports.default = Lifecycle;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./routerWarning":75,"_process":1,"invariant":26,"react":"react"}],48:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _PropTypes = require('./PropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

var _React$PropTypes = _react2.default.PropTypes;
var bool = _React$PropTypes.bool;
var object = _React$PropTypes.object;
var string = _React$PropTypes.string;
var func = _React$PropTypes.func;
var oneOfType = _React$PropTypes.oneOfType;


function isLeftClickEvent(event) {
  return event.button === 0;
}

function isModifiedEvent(event) {
  return !!(event.metaKey || event.altKey || event.ctrlKey || event.shiftKey);
}

// TODO: De-duplicate against hasAnyProperties in createTransitionManager.
function isEmptyObject(object) {
  for (var p in object) {
    if (Object.prototype.hasOwnProperty.call(object, p)) return false;
  }return true;
}

function createLocationDescriptor(to, _ref) {
  var query = _ref.query;
  var hash = _ref.hash;
  var state = _ref.state;

  if (query || hash || state) {
    return { pathname: to, query: query, hash: hash, state: state };
  }

  return to;
}

/**
 * A <Link> is used to create an <a> element that links to a route.
 * When that route is active, the link gets the value of its
 * activeClassName prop.
 *
 * For example, assuming you have the following route:
 *
 *   <Route path="/posts/:postID" component={Post} />
 *
 * You could use the following component to link to that route:
 *
 *   <Link to={`/posts/${post.id}`} />
 *
 * Links may pass along location state and/or query string parameters
 * in the state/query props, respectively.
 *
 *   <Link ... query={{ show: true }} state={{ the: 'state' }} />
 */
var Link = _react2.default.createClass({
  displayName: 'Link',


  contextTypes: {
    router: _PropTypes.routerShape
  },

  propTypes: {
    to: oneOfType([string, object]),
    query: object,
    hash: string,
    state: object,
    activeStyle: object,
    activeClassName: string,
    onlyActiveOnIndex: bool.isRequired,
    onClick: func,
    target: string
  },

  getDefaultProps: function getDefaultProps() {
    return {
      onlyActiveOnIndex: false,
      style: {}
    };
  },
  handleClick: function handleClick(event) {
    if (this.props.onClick) this.props.onClick(event);

    if (event.defaultPrevented) return;

    !this.context.router ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, '<Link>s rendered outside of a router context cannot navigate.') : (0, _invariant2.default)(false) : void 0;

    if (isModifiedEvent(event) || !isLeftClickEvent(event)) return;

    // If target prop is set (e.g. to "_blank"), let browser handle link.
    /* istanbul ignore if: untestable with Karma */
    if (this.props.target) return;

    event.preventDefault();

    var _props = this.props;
    var to = _props.to;
    var query = _props.query;
    var hash = _props.hash;
    var state = _props.state;

    var location = createLocationDescriptor(to, { query: query, hash: hash, state: state });

    this.context.router.push(location);
  },
  render: function render() {
    var _props2 = this.props;
    var to = _props2.to;
    var query = _props2.query;
    var hash = _props2.hash;
    var state = _props2.state;
    var activeClassName = _props2.activeClassName;
    var activeStyle = _props2.activeStyle;
    var onlyActiveOnIndex = _props2.onlyActiveOnIndex;

    var props = _objectWithoutProperties(_props2, ['to', 'query', 'hash', 'state', 'activeClassName', 'activeStyle', 'onlyActiveOnIndex']);

    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(!(query || hash || state), 'the `query`, `hash`, and `state` props on `<Link>` are deprecated, use `<Link to={{ pathname, query, hash, state }}/>. http://tiny.cc/router-isActivedeprecated') : void 0;

    // Ignore if rendered outside the context of router, simplifies unit testing.
    var router = this.context.router;


    if (router) {
      // If user does not specify a `to` prop, return an empty anchor tag.
      if (to == null) {
        return _react2.default.createElement('a', props);
      }

      var location = createLocationDescriptor(to, { query: query, hash: hash, state: state });
      props.href = router.createHref(location);

      if (activeClassName || activeStyle != null && !isEmptyObject(activeStyle)) {
        if (router.isActive(location, onlyActiveOnIndex)) {
          if (activeClassName) {
            if (props.className) {
              props.className += ' ' + activeClassName;
            } else {
              props.className = activeClassName;
            }
          }

          if (activeStyle) props.style = _extends({}, props.style, activeStyle);
        }
      }
    }

    return _react2.default.createElement('a', _extends({}, props, { onClick: this.handleClick }));
  }
});

exports.default = Link;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./PropTypes":50,"./routerWarning":75,"_process":1,"invariant":26,"react":"react"}],49:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports.compilePattern = compilePattern;
exports.matchPattern = matchPattern;
exports.getParamNames = getParamNames;
exports.getParams = getParams;
exports.formatPattern = formatPattern;

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function escapeRegExp(string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function _compilePattern(pattern) {
  var regexpSource = '';
  var paramNames = [];
  var tokens = [];

  var match = void 0,
      lastIndex = 0,
      matcher = /:([a-zA-Z_$][a-zA-Z0-9_$]*)|\*\*|\*|\(|\)/g;
  while (match = matcher.exec(pattern)) {
    if (match.index !== lastIndex) {
      tokens.push(pattern.slice(lastIndex, match.index));
      regexpSource += escapeRegExp(pattern.slice(lastIndex, match.index));
    }

    if (match[1]) {
      regexpSource += '([^/]+)';
      paramNames.push(match[1]);
    } else if (match[0] === '**') {
      regexpSource += '(.*)';
      paramNames.push('splat');
    } else if (match[0] === '*') {
      regexpSource += '(.*?)';
      paramNames.push('splat');
    } else if (match[0] === '(') {
      regexpSource += '(?:';
    } else if (match[0] === ')') {
      regexpSource += ')?';
    }

    tokens.push(match[0]);

    lastIndex = matcher.lastIndex;
  }

  if (lastIndex !== pattern.length) {
    tokens.push(pattern.slice(lastIndex, pattern.length));
    regexpSource += escapeRegExp(pattern.slice(lastIndex, pattern.length));
  }

  return {
    pattern: pattern,
    regexpSource: regexpSource,
    paramNames: paramNames,
    tokens: tokens
  };
}

var CompiledPatternsCache = Object.create(null);

function compilePattern(pattern) {
  if (!CompiledPatternsCache[pattern]) CompiledPatternsCache[pattern] = _compilePattern(pattern);

  return CompiledPatternsCache[pattern];
}

/**
 * Attempts to match a pattern on the given pathname. Patterns may use
 * the following special characters:
 *
 * - :paramName     Matches a URL segment up to the next /, ?, or #. The
 *                  captured string is considered a "param"
 * - ()             Wraps a segment of the URL that is optional
 * - *              Consumes (non-greedy) all characters up to the next
 *                  character in the pattern, or to the end of the URL if
 *                  there is none
 * - **             Consumes (greedy) all characters up to the next character
 *                  in the pattern, or to the end of the URL if there is none
 *
 *  The function calls callback(error, matched) when finished.
 * The return value is an object with the following properties:
 *
 * - remainingPathname
 * - paramNames
 * - paramValues
 */
function matchPattern(pattern, pathname) {
  // Ensure pattern starts with leading slash for consistency with pathname.
  if (pattern.charAt(0) !== '/') {
    pattern = '/' + pattern;
  }

  var _compilePattern2 = compilePattern(pattern);

  var regexpSource = _compilePattern2.regexpSource;
  var paramNames = _compilePattern2.paramNames;
  var tokens = _compilePattern2.tokens;


  if (pattern.charAt(pattern.length - 1) !== '/') {
    regexpSource += '/?'; // Allow optional path separator at end.
  }

  // Special-case patterns like '*' for catch-all routes.
  if (tokens[tokens.length - 1] === '*') {
    regexpSource += '$';
  }

  var match = pathname.match(new RegExp('^' + regexpSource, 'i'));
  if (match == null) {
    return null;
  }

  var matchedPath = match[0];
  var remainingPathname = pathname.substr(matchedPath.length);

  if (remainingPathname) {
    // Require that the match ends at a path separator, if we didn't match
    // the full path, so any remaining pathname is a new path segment.
    if (matchedPath.charAt(matchedPath.length - 1) !== '/') {
      return null;
    }

    // If there is a remaining pathname, treat the path separator as part of
    // the remaining pathname for properly continuing the match.
    remainingPathname = '/' + remainingPathname;
  }

  return {
    remainingPathname: remainingPathname,
    paramNames: paramNames,
    paramValues: match.slice(1).map(function (v) {
      return v && decodeURIComponent(v);
    })
  };
}

function getParamNames(pattern) {
  return compilePattern(pattern).paramNames;
}

function getParams(pattern, pathname) {
  var match = matchPattern(pattern, pathname);
  if (!match) {
    return null;
  }

  var paramNames = match.paramNames;
  var paramValues = match.paramValues;

  var params = {};

  paramNames.forEach(function (paramName, index) {
    params[paramName] = paramValues[index];
  });

  return params;
}

/**
 * Returns a version of the given pattern with params interpolated. Throws
 * if there is a dynamic segment of the pattern for which there is no param.
 */
function formatPattern(pattern, params) {
  params = params || {};

  var _compilePattern3 = compilePattern(pattern);

  var tokens = _compilePattern3.tokens;

  var parenCount = 0,
      pathname = '',
      splatIndex = 0;

  var token = void 0,
      paramName = void 0,
      paramValue = void 0;
  for (var i = 0, len = tokens.length; i < len; ++i) {
    token = tokens[i];

    if (token === '*' || token === '**') {
      paramValue = Array.isArray(params.splat) ? params.splat[splatIndex++] : params.splat;

      !(paramValue != null || parenCount > 0) ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'Missing splat #%s for path "%s"', splatIndex, pattern) : (0, _invariant2.default)(false) : void 0;

      if (paramValue != null) pathname += encodeURI(paramValue);
    } else if (token === '(') {
      parenCount += 1;
    } else if (token === ')') {
      parenCount -= 1;
    } else if (token.charAt(0) === ':') {
      paramName = token.substring(1);
      paramValue = params[paramName];

      !(paramValue != null || parenCount > 0) ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'Missing "%s" parameter for path "%s"', paramName, pattern) : (0, _invariant2.default)(false) : void 0;

      if (paramValue != null) pathname += encodeURIComponent(paramValue);
    } else {
      pathname += token;
    }
  }

  return pathname.replace(/\/+/g, '/');
}
}).call(this,require('_process'))

},{"_process":1,"invariant":26}],50:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports.router = exports.routes = exports.route = exports.components = exports.component = exports.location = exports.history = exports.falsy = exports.locationShape = exports.routerShape = undefined;

var _react = require('react');

var _deprecateObjectProperties = require('./deprecateObjectProperties');

var _deprecateObjectProperties2 = _interopRequireDefault(_deprecateObjectProperties);

var _InternalPropTypes = require('./InternalPropTypes');

var InternalPropTypes = _interopRequireWildcard(_InternalPropTypes);

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var func = _react.PropTypes.func;
var object = _react.PropTypes.object;
var shape = _react.PropTypes.shape;
var string = _react.PropTypes.string;
var routerShape = exports.routerShape = shape({
  push: func.isRequired,
  replace: func.isRequired,
  go: func.isRequired,
  goBack: func.isRequired,
  goForward: func.isRequired,
  setRouteLeaveHook: func.isRequired,
  isActive: func.isRequired
});

var locationShape = exports.locationShape = shape({
  pathname: string.isRequired,
  search: string.isRequired,
  state: object,
  action: string.isRequired,
  key: string
});

// Deprecated stuff below:

var falsy = exports.falsy = InternalPropTypes.falsy;
var history = exports.history = InternalPropTypes.history;
var location = exports.location = locationShape;
var component = exports.component = InternalPropTypes.component;
var components = exports.components = InternalPropTypes.components;
var route = exports.route = InternalPropTypes.route;
var routes = exports.routes = InternalPropTypes.routes;
var router = exports.router = routerShape;

if (process.env.NODE_ENV !== 'production') {
  (function () {
    var deprecatePropType = function deprecatePropType(propType, message) {
      return function () {
        process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, message) : void 0;
        return propType.apply(undefined, arguments);
      };
    };

    var deprecateInternalPropType = function deprecateInternalPropType(propType) {
      return deprecatePropType(propType, 'This prop type is not intended for external use, and was previously exported by mistake. These internal prop types are deprecated for external use, and will be removed in a later version.');
    };

    var deprecateRenamedPropType = function deprecateRenamedPropType(propType, name) {
      return deprecatePropType(propType, 'The `' + name + '` prop type is now exported as `' + name + 'Shape` to avoid name conflicts. This export is deprecated and will be removed in a later version.');
    };

    exports.falsy = falsy = deprecateInternalPropType(falsy);
    exports.history = history = deprecateInternalPropType(history);
    exports.component = component = deprecateInternalPropType(component);
    exports.components = components = deprecateInternalPropType(components);
    exports.route = route = deprecateInternalPropType(route);
    exports.routes = routes = deprecateInternalPropType(routes);

    exports.location = location = deprecateRenamedPropType(location, 'location');
    exports.router = router = deprecateRenamedPropType(router, 'router');
  })();
}

var defaultExport = {
  falsy: falsy,
  history: history,
  location: location,
  component: component,
  components: components,
  route: route,
  // For some reason, routes was never here.
  router: router
};

if (process.env.NODE_ENV !== 'production') {
  defaultExport = (0, _deprecateObjectProperties2.default)(defaultExport, 'The default export from `react-router/lib/PropTypes` is deprecated. Please use the named exports instead.');
}

exports.default = defaultExport;
}).call(this,require('_process'))

},{"./InternalPropTypes":46,"./deprecateObjectProperties":66,"./routerWarning":75,"_process":1,"react":"react"}],51:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _RouteUtils = require('./RouteUtils');

var _PatternUtils = require('./PatternUtils');

var _InternalPropTypes = require('./InternalPropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _React$PropTypes = _react2.default.PropTypes;
var string = _React$PropTypes.string;
var object = _React$PropTypes.object;

/**
 * A <Redirect> is used to declare another URL path a client should
 * be sent to when they request a given URL.
 *
 * Redirects are placed alongside routes in the route configuration
 * and are traversed in the same manner.
 */

var Redirect = _react2.default.createClass({
  displayName: 'Redirect',


  statics: {
    createRouteFromReactElement: function createRouteFromReactElement(element) {
      var route = (0, _RouteUtils.createRouteFromReactElement)(element);

      if (route.from) route.path = route.from;

      route.onEnter = function (nextState, replace) {
        var location = nextState.location;
        var params = nextState.params;


        var pathname = void 0;
        if (route.to.charAt(0) === '/') {
          pathname = (0, _PatternUtils.formatPattern)(route.to, params);
        } else if (!route.to) {
          pathname = location.pathname;
        } else {
          var routeIndex = nextState.routes.indexOf(route);
          var parentPattern = Redirect.getRoutePattern(nextState.routes, routeIndex - 1);
          var pattern = parentPattern.replace(/\/*$/, '/') + route.to;
          pathname = (0, _PatternUtils.formatPattern)(pattern, params);
        }

        replace({
          pathname: pathname,
          query: route.query || location.query,
          state: route.state || location.state
        });
      };

      return route;
    },
    getRoutePattern: function getRoutePattern(routes, routeIndex) {
      var parentPattern = '';

      for (var i = routeIndex; i >= 0; i--) {
        var route = routes[i];
        var pattern = route.path || '';

        parentPattern = pattern.replace(/\/*$/, '/') + parentPattern;

        if (pattern.indexOf('/') === 0) break;
      }

      return '/' + parentPattern;
    }
  },

  propTypes: {
    path: string,
    from: string, // Alias for path
    to: string.isRequired,
    query: object,
    state: object,
    onEnter: _InternalPropTypes.falsy,
    children: _InternalPropTypes.falsy
  },

  /* istanbul ignore next: sanity check */
  render: function render() {
    !false ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, '<Redirect> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = Redirect;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./InternalPropTypes":46,"./PatternUtils":49,"./RouteUtils":54,"_process":1,"invariant":26,"react":"react"}],52:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _RouteUtils = require('./RouteUtils');

var _InternalPropTypes = require('./InternalPropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _React$PropTypes = _react2.default.PropTypes;
var string = _React$PropTypes.string;
var func = _React$PropTypes.func;

/**
 * A <Route> is used to declare which components are rendered to the
 * page when the URL matches a given pattern.
 *
 * Routes are arranged in a nested tree structure. When a new URL is
 * requested, the tree is searched depth-first to find a route whose
 * path matches the URL.  When one is found, all routes in the tree
 * that lead to it are considered "active" and their components are
 * rendered into the DOM, nested in the same order as in the tree.
 */

var Route = _react2.default.createClass({
  displayName: 'Route',


  statics: {
    createRouteFromReactElement: _RouteUtils.createRouteFromReactElement
  },

  propTypes: {
    path: string,
    component: _InternalPropTypes.component,
    components: _InternalPropTypes.components,
    getComponent: func,
    getComponents: func
  },

  /* istanbul ignore next: sanity check */
  render: function render() {
    !false ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, '<Route> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = Route;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./InternalPropTypes":46,"./RouteUtils":54,"_process":1,"invariant":26,"react":"react"}],53:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var object = _react2.default.PropTypes.object;

/**
 * The RouteContext mixin provides a convenient way for route
 * components to set the route in context. This is needed for
 * routes that render elements that want to use the Lifecycle
 * mixin to prevent transitions.
 */

var RouteContext = {

  propTypes: {
    route: object.isRequired
  },

  childContextTypes: {
    route: object.isRequired
  },

  getChildContext: function getChildContext() {
    return {
      route: this.props.route
    };
  },
  componentWillMount: function componentWillMount() {
    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'The `RouteContext` mixin is deprecated. You can provide `this.props.route` on context with your own `contextTypes`. http://tiny.cc/router-routecontextmixin') : void 0;
  }
};

exports.default = RouteContext;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./routerWarning":75,"_process":1,"react":"react"}],54:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.isReactChildren = isReactChildren;
exports.createRouteFromReactElement = createRouteFromReactElement;
exports.createRoutesFromReactChildren = createRoutesFromReactChildren;
exports.createRoutes = createRoutes;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function isValidChild(object) {
  return object == null || _react2.default.isValidElement(object);
}

function isReactChildren(object) {
  return isValidChild(object) || Array.isArray(object) && object.every(isValidChild);
}

function createRoute(defaultProps, props) {
  return _extends({}, defaultProps, props);
}

function createRouteFromReactElement(element) {
  var type = element.type;
  var route = createRoute(type.defaultProps, element.props);

  if (route.children) {
    var childRoutes = createRoutesFromReactChildren(route.children, route);

    if (childRoutes.length) route.childRoutes = childRoutes;

    delete route.children;
  }

  return route;
}

/**
 * Creates and returns a routes object from the given ReactChildren. JSX
 * provides a convenient way to visualize how routes in the hierarchy are
 * nested.
 *
 *   import { Route, createRoutesFromReactChildren } from 'react-router'
 *
 *   const routes = createRoutesFromReactChildren(
 *     <Route component={App}>
 *       <Route path="home" component={Dashboard}/>
 *       <Route path="news" component={NewsFeed}/>
 *     </Route>
 *   )
 *
 * Note: This method is automatically used when you provide <Route> children
 * to a <Router> component.
 */
function createRoutesFromReactChildren(children, parentRoute) {
  var routes = [];

  _react2.default.Children.forEach(children, function (element) {
    if (_react2.default.isValidElement(element)) {
      // Component classes may have a static create* method.
      if (element.type.createRouteFromReactElement) {
        var route = element.type.createRouteFromReactElement(element, parentRoute);

        if (route) routes.push(route);
      } else {
        routes.push(createRouteFromReactElement(element));
      }
    }
  });

  return routes;
}

/**
 * Creates and returns an array of routes from the given object which
 * may be a JSX route, a plain object route, or an array of either.
 */
function createRoutes(routes) {
  if (isReactChildren(routes)) {
    routes = createRoutesFromReactChildren(routes);
  } else if (routes && !Array.isArray(routes)) {
    routes = [routes];
  }

  return routes;
}
},{"react":"react"}],55:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createHashHistory = require('history/lib/createHashHistory');

var _createHashHistory2 = _interopRequireDefault(_createHashHistory);

var _useQueries = require('history/lib/useQueries');

var _useQueries2 = _interopRequireDefault(_useQueries);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _createTransitionManager = require('./createTransitionManager');

var _createTransitionManager2 = _interopRequireDefault(_createTransitionManager);

var _InternalPropTypes = require('./InternalPropTypes');

var _RouterContext = require('./RouterContext');

var _RouterContext2 = _interopRequireDefault(_RouterContext);

var _RouteUtils = require('./RouteUtils');

var _RouterUtils = require('./RouterUtils');

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

function isDeprecatedHistory(history) {
  return !history || !history.__v2_compatible__;
}

/* istanbul ignore next: sanity check */
function isUnsupportedHistory(history) {
  // v3 histories expose getCurrentLocation, but aren't currently supported.
  return history && history.getCurrentLocation;
}

var _React$PropTypes = _react2.default.PropTypes;
var func = _React$PropTypes.func;
var object = _React$PropTypes.object;

/**
 * A <Router> is a high-level API for automatically setting up
 * a router that renders a <RouterContext> with all the props
 * it needs each time the URL changes.
 */

var Router = _react2.default.createClass({
  displayName: 'Router',


  propTypes: {
    history: object,
    children: _InternalPropTypes.routes,
    routes: _InternalPropTypes.routes, // alias for children
    render: func,
    createElement: func,
    onError: func,
    onUpdate: func,

    // Deprecated:
    parseQueryString: func,
    stringifyQuery: func,

    // PRIVATE: For client-side rehydration of server match.
    matchContext: object
  },

  getDefaultProps: function getDefaultProps() {
    return {
      render: function render(props) {
        return _react2.default.createElement(_RouterContext2.default, props);
      }
    };
  },
  getInitialState: function getInitialState() {
    return {
      location: null,
      routes: null,
      params: null,
      components: null
    };
  },
  handleError: function handleError(error) {
    if (this.props.onError) {
      this.props.onError.call(this, error);
    } else {
      // Throw errors by default so we don't silently swallow them!
      throw error; // This error probably occurred in getChildRoutes or getComponents.
    }
  },
  componentWillMount: function componentWillMount() {
    var _this = this;

    var _props = this.props;
    var parseQueryString = _props.parseQueryString;
    var stringifyQuery = _props.stringifyQuery;

    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(!(parseQueryString || stringifyQuery), '`parseQueryString` and `stringifyQuery` are deprecated. Please create a custom history. http://tiny.cc/router-customquerystring') : void 0;

    var _createRouterObjects = this.createRouterObjects();

    var history = _createRouterObjects.history;
    var transitionManager = _createRouterObjects.transitionManager;
    var router = _createRouterObjects.router;


    this._unlisten = transitionManager.listen(function (error, state) {
      if (error) {
        _this.handleError(error);
      } else {
        _this.setState(state, _this.props.onUpdate);
      }
    });

    this.history = history;
    this.router = router;
  },
  createRouterObjects: function createRouterObjects() {
    var matchContext = this.props.matchContext;

    if (matchContext) {
      return matchContext;
    }

    var history = this.props.history;
    var _props2 = this.props;
    var routes = _props2.routes;
    var children = _props2.children;


    !!isUnsupportedHistory(history) ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'You have provided a history object created with history v3.x. ' + 'This version of React Router is not compatible with v3 history ' + 'objects. Please use history v2.x instead.') : (0, _invariant2.default)(false) : void 0;

    if (isDeprecatedHistory(history)) {
      history = this.wrapDeprecatedHistory(history);
    }

    var transitionManager = (0, _createTransitionManager2.default)(history, (0, _RouteUtils.createRoutes)(routes || children));
    var router = (0, _RouterUtils.createRouterObject)(history, transitionManager);
    var routingHistory = (0, _RouterUtils.createRoutingHistory)(history, transitionManager);

    return { history: routingHistory, transitionManager: transitionManager, router: router };
  },
  wrapDeprecatedHistory: function wrapDeprecatedHistory(history) {
    var _props3 = this.props;
    var parseQueryString = _props3.parseQueryString;
    var stringifyQuery = _props3.stringifyQuery;


    var createHistory = void 0;
    if (history) {
      process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'It appears you have provided a deprecated history object to `<Router/>`, please use a history provided by ' + 'React Router with `import { browserHistory } from \'react-router\'` or `import { hashHistory } from \'react-router\'`. ' + 'If you are using a custom history please create it with `useRouterHistory`, see http://tiny.cc/router-usinghistory for details.') : void 0;
      createHistory = function createHistory() {
        return history;
      };
    } else {
      process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, '`Router` no longer defaults the history prop to hash history. Please use the `hashHistory` singleton instead. http://tiny.cc/router-defaulthistory') : void 0;
      createHistory = _createHashHistory2.default;
    }

    return (0, _useQueries2.default)(createHistory)({ parseQueryString: parseQueryString, stringifyQuery: stringifyQuery });
  },


  /* istanbul ignore next: sanity check */
  componentWillReceiveProps: function componentWillReceiveProps(nextProps) {
    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(nextProps.history === this.props.history, 'You cannot change <Router history>; it will be ignored') : void 0;

    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)((nextProps.routes || nextProps.children) === (this.props.routes || this.props.children), 'You cannot change <Router routes>; it will be ignored') : void 0;
  },
  componentWillUnmount: function componentWillUnmount() {
    if (this._unlisten) this._unlisten();
  },
  render: function render() {
    var _state = this.state;
    var location = _state.location;
    var routes = _state.routes;
    var params = _state.params;
    var components = _state.components;
    var _props4 = this.props;
    var createElement = _props4.createElement;
    var render = _props4.render;

    var props = _objectWithoutProperties(_props4, ['createElement', 'render']);

    if (location == null) return null; // Async match

    // Only forward non-Router-specific props to routing context, as those are
    // the only ones that might be custom routing context props.
    Object.keys(Router.propTypes).forEach(function (propType) {
      return delete props[propType];
    });

    return render(_extends({}, props, {
      history: this.history,
      router: this.router,
      location: location,
      routes: routes,
      params: params,
      components: components,
      createElement: createElement
    }));
  }
});

exports.default = Router;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./InternalPropTypes":46,"./RouteUtils":54,"./RouterContext":56,"./RouterUtils":57,"./createTransitionManager":65,"./routerWarning":75,"_process":1,"history/lib/createHashHistory":16,"history/lib/useQueries":23,"invariant":26,"react":"react"}],56:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol ? "symbol" : typeof obj; };

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _deprecateObjectProperties = require('./deprecateObjectProperties');

var _deprecateObjectProperties2 = _interopRequireDefault(_deprecateObjectProperties);

var _getRouteParams = require('./getRouteParams');

var _getRouteParams2 = _interopRequireDefault(_getRouteParams);

var _RouteUtils = require('./RouteUtils');

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _React$PropTypes = _react2.default.PropTypes;
var array = _React$PropTypes.array;
var func = _React$PropTypes.func;
var object = _React$PropTypes.object;

/**
 * A <RouterContext> renders the component tree for a given router state
 * and sets the history object and the current location in context.
 */

var RouterContext = _react2.default.createClass({
  displayName: 'RouterContext',


  propTypes: {
    history: object,
    router: object.isRequired,
    location: object.isRequired,
    routes: array.isRequired,
    params: object.isRequired,
    components: array.isRequired,
    createElement: func.isRequired
  },

  getDefaultProps: function getDefaultProps() {
    return {
      createElement: _react2.default.createElement
    };
  },


  childContextTypes: {
    history: object,
    location: object.isRequired,
    router: object.isRequired
  },

  getChildContext: function getChildContext() {
    var _props = this.props;
    var router = _props.router;
    var history = _props.history;
    var location = _props.location;

    if (!router) {
      process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, '`<RouterContext>` expects a `router` rather than a `history`') : void 0;

      router = _extends({}, history, {
        setRouteLeaveHook: history.listenBeforeLeavingRoute
      });
      delete router.listenBeforeLeavingRoute;
    }

    if (process.env.NODE_ENV !== 'production') {
      location = (0, _deprecateObjectProperties2.default)(location, '`context.location` is deprecated, please use a route component\'s `props.location` instead. http://tiny.cc/router-accessinglocation');
    }

    return { history: history, location: location, router: router };
  },
  createElement: function createElement(component, props) {
    return component == null ? null : this.props.createElement(component, props);
  },
  render: function render() {
    var _this = this;

    var _props2 = this.props;
    var history = _props2.history;
    var location = _props2.location;
    var routes = _props2.routes;
    var params = _props2.params;
    var components = _props2.components;

    var element = null;

    if (components) {
      element = components.reduceRight(function (element, components, index) {
        if (components == null) return element; // Don't create new children; use the grandchildren.

        var route = routes[index];
        var routeParams = (0, _getRouteParams2.default)(route, params);
        var props = {
          history: history,
          location: location,
          params: params,
          route: route,
          routeParams: routeParams,
          routes: routes
        };

        if ((0, _RouteUtils.isReactChildren)(element)) {
          props.children = element;
        } else if (element) {
          for (var prop in element) {
            if (Object.prototype.hasOwnProperty.call(element, prop)) props[prop] = element[prop];
          }
        }

        if ((typeof components === 'undefined' ? 'undefined' : _typeof(components)) === 'object') {
          var elements = {};

          for (var key in components) {
            if (Object.prototype.hasOwnProperty.call(components, key)) {
              // Pass through the key as a prop to createElement to allow
              // custom createElement functions to know which named component
              // they're rendering, for e.g. matching up to fetched data.
              elements[key] = _this.createElement(components[key], _extends({
                key: key }, props));
            }
          }

          return elements;
        }

        return _this.createElement(components, props);
      }, element);
    }

    !(element === null || element === false || _react2.default.isValidElement(element)) ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'The root route must render a single element') : (0, _invariant2.default)(false) : void 0;

    return element;
  }
});

exports.default = RouterContext;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./RouteUtils":54,"./deprecateObjectProperties":66,"./getRouteParams":68,"./routerWarning":75,"_process":1,"invariant":26,"react":"react"}],57:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.createRouterObject = createRouterObject;
exports.createRoutingHistory = createRoutingHistory;

var _deprecateObjectProperties = require('./deprecateObjectProperties');

var _deprecateObjectProperties2 = _interopRequireDefault(_deprecateObjectProperties);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function createRouterObject(history, transitionManager) {
  return _extends({}, history, {
    setRouteLeaveHook: transitionManager.listenBeforeLeavingRoute,
    isActive: transitionManager.isActive
  });
}

// deprecated
function createRoutingHistory(history, transitionManager) {
  history = _extends({}, history, transitionManager);

  if (process.env.NODE_ENV !== 'production') {
    history = (0, _deprecateObjectProperties2.default)(history, '`props.history` and `context.history` are deprecated. Please use `context.router`. http://tiny.cc/router-contextchanges');
  }

  return history;
}
}).call(this,require('_process'))

},{"./deprecateObjectProperties":66,"_process":1}],58:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _RouterContext = require('./RouterContext');

var _RouterContext2 = _interopRequireDefault(_RouterContext);

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var RoutingContext = _react2.default.createClass({
  displayName: 'RoutingContext',
  componentWillMount: function componentWillMount() {
    process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, '`RoutingContext` has been renamed to `RouterContext`. Please use `import { RouterContext } from \'react-router\'`. http://tiny.cc/router-routercontext') : void 0;
  },
  render: function render() {
    return _react2.default.createElement(_RouterContext2.default, this.props);
  }
});

exports.default = RoutingContext;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./RouterContext":56,"./routerWarning":75,"_process":1,"react":"react"}],59:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports.runEnterHooks = runEnterHooks;
exports.runChangeHooks = runChangeHooks;
exports.runLeaveHooks = runLeaveHooks;

var _AsyncUtils = require('./AsyncUtils');

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function createTransitionHook(hook, route, asyncArity) {
  return function () {
    for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    hook.apply(route, args);

    if (hook.length < asyncArity) {
      var callback = args[args.length - 1];
      // Assume hook executes synchronously and
      // automatically call the callback.
      callback();
    }
  };
}

function getEnterHooks(routes) {
  return routes.reduce(function (hooks, route) {
    if (route.onEnter) hooks.push(createTransitionHook(route.onEnter, route, 3));

    return hooks;
  }, []);
}

function getChangeHooks(routes) {
  return routes.reduce(function (hooks, route) {
    if (route.onChange) hooks.push(createTransitionHook(route.onChange, route, 4));
    return hooks;
  }, []);
}

function runTransitionHooks(length, iter, callback) {
  if (!length) {
    callback();
    return;
  }

  var redirectInfo = void 0;
  function replace(location, deprecatedPathname, deprecatedQuery) {
    if (deprecatedPathname) {
      process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, '`replaceState(state, pathname, query) is deprecated; use `replace(location)` with a location descriptor instead. http://tiny.cc/router-isActivedeprecated') : void 0;
      redirectInfo = {
        pathname: deprecatedPathname,
        query: deprecatedQuery,
        state: location
      };

      return;
    }

    redirectInfo = location;
  }

  (0, _AsyncUtils.loopAsync)(length, function (index, next, done) {
    iter(index, replace, function (error) {
      if (error || redirectInfo) {
        done(error, redirectInfo); // No need to continue.
      } else {
        next();
      }
    });
  }, callback);
}

/**
 * Runs all onEnter hooks in the given array of routes in order
 * with onEnter(nextState, replace, callback) and calls
 * callback(error, redirectInfo) when finished. The first hook
 * to use replace short-circuits the loop.
 *
 * If a hook needs to run asynchronously, it may use the callback
 * function. However, doing so will cause the transition to pause,
 * which could lead to a non-responsive UI if the hook is slow.
 */
function runEnterHooks(routes, nextState, callback) {
  var hooks = getEnterHooks(routes);
  return runTransitionHooks(hooks.length, function (index, replace, next) {
    hooks[index](nextState, replace, next);
  }, callback);
}

/**
 * Runs all onChange hooks in the given array of routes in order
 * with onChange(prevState, nextState, replace, callback) and calls
 * callback(error, redirectInfo) when finished. The first hook
 * to use replace short-circuits the loop.
 *
 * If a hook needs to run asynchronously, it may use the callback
 * function. However, doing so will cause the transition to pause,
 * which could lead to a non-responsive UI if the hook is slow.
 */
function runChangeHooks(routes, state, nextState, callback) {
  var hooks = getChangeHooks(routes);
  return runTransitionHooks(hooks.length, function (index, replace, next) {
    hooks[index](state, nextState, replace, next);
  }, callback);
}

/**
 * Runs all onLeave hooks in the given array of routes in order.
 */
function runLeaveHooks(routes, prevState) {
  for (var i = 0, len = routes.length; i < len; ++i) {
    if (routes[i].onLeave) routes[i].onLeave.call(routes[i], prevState);
  }
}
}).call(this,require('_process'))

},{"./AsyncUtils":41,"./routerWarning":75,"_process":1}],60:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _RouterContext = require('./RouterContext');

var _RouterContext2 = _interopRequireDefault(_RouterContext);

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = function () {
  for (var _len = arguments.length, middlewares = Array(_len), _key = 0; _key < _len; _key++) {
    middlewares[_key] = arguments[_key];
  }

  if (process.env.NODE_ENV !== 'production') {
    middlewares.forEach(function (middleware, index) {
      process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(middleware.renderRouterContext || middleware.renderRouteComponent, 'The middleware specified at index ' + index + ' does not appear to be ' + 'a valid React Router middleware.') : void 0;
    });
  }

  var withContext = middlewares.map(function (middleware) {
    return middleware.renderRouterContext;
  }).filter(Boolean);
  var withComponent = middlewares.map(function (middleware) {
    return middleware.renderRouteComponent;
  }).filter(Boolean);

  var makeCreateElement = function makeCreateElement() {
    var baseCreateElement = arguments.length <= 0 || arguments[0] === undefined ? _react.createElement : arguments[0];
    return function (Component, props) {
      return withComponent.reduceRight(function (previous, renderRouteComponent) {
        return renderRouteComponent(previous, props);
      }, baseCreateElement(Component, props));
    };
  };

  return function (renderProps) {
    return withContext.reduceRight(function (previous, renderRouterContext) {
      return renderRouterContext(previous, renderProps);
    }, _react2.default.createElement(_RouterContext2.default, _extends({}, renderProps, {
      createElement: makeCreateElement(renderProps.createElement)
    })));
  };
};

module.exports = exports['default'];
}).call(this,require('_process'))

},{"./RouterContext":56,"./routerWarning":75,"_process":1,"react":"react"}],61:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _createBrowserHistory = require('history/lib/createBrowserHistory');

var _createBrowserHistory2 = _interopRequireDefault(_createBrowserHistory);

var _createRouterHistory = require('./createRouterHistory');

var _createRouterHistory2 = _interopRequireDefault(_createRouterHistory);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (0, _createRouterHistory2.default)(_createBrowserHistory2.default);
module.exports = exports['default'];
},{"./createRouterHistory":64,"history/lib/createBrowserHistory":14}],62:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _PatternUtils = require('./PatternUtils');

function routeParamsChanged(route, prevState, nextState) {
  if (!route.path) return false;

  var paramNames = (0, _PatternUtils.getParamNames)(route.path);

  return paramNames.some(function (paramName) {
    return prevState.params[paramName] !== nextState.params[paramName];
  });
}

/**
 * Returns an object of { leaveRoutes, changeRoutes, enterRoutes } determined by
 * the change from prevState to nextState. We leave routes if either
 * 1) they are not in the next state or 2) they are in the next state
 * but their params have changed (i.e. /users/123 => /users/456).
 *
 * leaveRoutes are ordered starting at the leaf route of the tree
 * we're leaving up to the common parent route. enterRoutes are ordered
 * from the top of the tree we're entering down to the leaf route.
 *
 * changeRoutes are any routes that didn't leave or enter during
 * the transition.
 */
function computeChangedRoutes(prevState, nextState) {
  var prevRoutes = prevState && prevState.routes;
  var nextRoutes = nextState.routes;

  var leaveRoutes = void 0,
      changeRoutes = void 0,
      enterRoutes = void 0;
  if (prevRoutes) {
    (function () {
      var parentIsLeaving = false;
      leaveRoutes = prevRoutes.filter(function (route) {
        if (parentIsLeaving) {
          return true;
        } else {
          var isLeaving = nextRoutes.indexOf(route) === -1 || routeParamsChanged(route, prevState, nextState);
          if (isLeaving) parentIsLeaving = true;
          return isLeaving;
        }
      });

      // onLeave hooks start at the leaf route.
      leaveRoutes.reverse();

      enterRoutes = [];
      changeRoutes = [];

      nextRoutes.forEach(function (route) {
        var isNew = prevRoutes.indexOf(route) === -1;
        var paramsChanged = leaveRoutes.indexOf(route) !== -1;

        if (isNew || paramsChanged) enterRoutes.push(route);else changeRoutes.push(route);
      });
    })();
  } else {
    leaveRoutes = [];
    changeRoutes = [];
    enterRoutes = nextRoutes;
  }

  return {
    leaveRoutes: leaveRoutes,
    changeRoutes: changeRoutes,
    enterRoutes: enterRoutes
  };
}

exports.default = computeChangedRoutes;
module.exports = exports['default'];
},{"./PatternUtils":49}],63:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.default = createMemoryHistory;

var _useQueries = require('history/lib/useQueries');

var _useQueries2 = _interopRequireDefault(_useQueries);

var _useBasename = require('history/lib/useBasename');

var _useBasename2 = _interopRequireDefault(_useBasename);

var _createMemoryHistory = require('history/lib/createMemoryHistory');

var _createMemoryHistory2 = _interopRequireDefault(_createMemoryHistory);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function createMemoryHistory(options) {
  // signatures and type checking differ between `useRoutes` and
  // `createMemoryHistory`, have to create `memoryHistory` first because
  // `useQueries` doesn't understand the signature
  var memoryHistory = (0, _createMemoryHistory2.default)(options);
  var createHistory = function createHistory() {
    return memoryHistory;
  };
  var history = (0, _useQueries2.default)((0, _useBasename2.default)(createHistory))(options);
  history.__v2_compatible__ = true;
  return history;
}
module.exports = exports['default'];
},{"history/lib/createMemoryHistory":19,"history/lib/useBasename":22,"history/lib/useQueries":23}],64:[function(require,module,exports){
'use strict';

exports.__esModule = true;

exports.default = function (createHistory) {
  var history = void 0;
  if (canUseDOM) history = (0, _useRouterHistory2.default)(createHistory)();
  return history;
};

var _useRouterHistory = require('./useRouterHistory');

var _useRouterHistory2 = _interopRequireDefault(_useRouterHistory);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var canUseDOM = !!(typeof window !== 'undefined' && window.document && window.document.createElement);

module.exports = exports['default'];
},{"./useRouterHistory":76}],65:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = createTransitionManager;

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _computeChangedRoutes2 = require('./computeChangedRoutes');

var _computeChangedRoutes3 = _interopRequireDefault(_computeChangedRoutes2);

var _TransitionUtils = require('./TransitionUtils');

var _isActive2 = require('./isActive');

var _isActive3 = _interopRequireDefault(_isActive2);

var _getComponents = require('./getComponents');

var _getComponents2 = _interopRequireDefault(_getComponents);

var _matchRoutes = require('./matchRoutes');

var _matchRoutes2 = _interopRequireDefault(_matchRoutes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function hasAnyProperties(object) {
  for (var p in object) {
    if (Object.prototype.hasOwnProperty.call(object, p)) return true;
  }return false;
}

function createTransitionManager(history, routes) {
  var state = {};

  // Signature should be (location, indexOnly), but needs to support (path,
  // query, indexOnly)
  function isActive(location) {
    var indexOnlyOrDeprecatedQuery = arguments.length <= 1 || arguments[1] === undefined ? false : arguments[1];
    var deprecatedIndexOnly = arguments.length <= 2 || arguments[2] === undefined ? null : arguments[2];

    var indexOnly = void 0;
    if (indexOnlyOrDeprecatedQuery && indexOnlyOrDeprecatedQuery !== true || deprecatedIndexOnly !== null) {
      process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, '`isActive(pathname, query, indexOnly) is deprecated; use `isActive(location, indexOnly)` with a location descriptor instead. http://tiny.cc/router-isActivedeprecated') : void 0;
      location = { pathname: location, query: indexOnlyOrDeprecatedQuery };
      indexOnly = deprecatedIndexOnly || false;
    } else {
      location = history.createLocation(location);
      indexOnly = indexOnlyOrDeprecatedQuery;
    }

    return (0, _isActive3.default)(location, indexOnly, state.location, state.routes, state.params);
  }

  var partialNextState = void 0;

  function match(location, callback) {
    if (partialNextState && partialNextState.location === location) {
      // Continue from where we left off.
      finishMatch(partialNextState, callback);
    } else {
      (0, _matchRoutes2.default)(routes, location, function (error, nextState) {
        if (error) {
          callback(error);
        } else if (nextState) {
          finishMatch(_extends({}, nextState, { location: location }), callback);
        } else {
          callback();
        }
      });
    }
  }

  function finishMatch(nextState, callback) {
    var _computeChangedRoutes = (0, _computeChangedRoutes3.default)(state, nextState);

    var leaveRoutes = _computeChangedRoutes.leaveRoutes;
    var changeRoutes = _computeChangedRoutes.changeRoutes;
    var enterRoutes = _computeChangedRoutes.enterRoutes;


    (0, _TransitionUtils.runLeaveHooks)(leaveRoutes, state);

    // Tear down confirmation hooks for left routes
    leaveRoutes.filter(function (route) {
      return enterRoutes.indexOf(route) === -1;
    }).forEach(removeListenBeforeHooksForRoute);

    // change and enter hooks are run in series
    (0, _TransitionUtils.runChangeHooks)(changeRoutes, state, nextState, function (error, redirectInfo) {
      if (error || redirectInfo) return handleErrorOrRedirect(error, redirectInfo);

      (0, _TransitionUtils.runEnterHooks)(enterRoutes, nextState, finishEnterHooks);
    });

    function finishEnterHooks(error, redirectInfo) {
      if (error || redirectInfo) return handleErrorOrRedirect(error, redirectInfo);

      // TODO: Fetch components after state is updated.
      (0, _getComponents2.default)(nextState, function (error, components) {
        if (error) {
          callback(error);
        } else {
          // TODO: Make match a pure function and have some other API
          // for "match and update state".
          callback(null, null, state = _extends({}, nextState, { components: components }));
        }
      });
    }

    function handleErrorOrRedirect(error, redirectInfo) {
      if (error) callback(error);else callback(null, redirectInfo);
    }
  }

  var RouteGuid = 1;

  function getRouteID(route) {
    var create = arguments.length <= 1 || arguments[1] === undefined ? true : arguments[1];

    return route.__id__ || create && (route.__id__ = RouteGuid++);
  }

  var RouteHooks = Object.create(null);

  function getRouteHooksForRoutes(routes) {
    return routes.reduce(function (hooks, route) {
      hooks.push.apply(hooks, RouteHooks[getRouteID(route)]);
      return hooks;
    }, []);
  }

  function transitionHook(location, callback) {
    (0, _matchRoutes2.default)(routes, location, function (error, nextState) {
      if (nextState == null) {
        // TODO: We didn't actually match anything, but hang
        // onto error/nextState so we don't have to matchRoutes
        // again in the listen callback.
        callback();
        return;
      }

      // Cache some state here so we don't have to
      // matchRoutes() again in the listen callback.
      partialNextState = _extends({}, nextState, { location: location });

      var hooks = getRouteHooksForRoutes((0, _computeChangedRoutes3.default)(state, partialNextState).leaveRoutes);

      var result = void 0;
      for (var i = 0, len = hooks.length; result == null && i < len; ++i) {
        // Passing the location arg here indicates to
        // the user that this is a transition hook.
        result = hooks[i](location);
      }

      callback(result);
    });
  }

  /* istanbul ignore next: untestable with Karma */
  function beforeUnloadHook() {
    // Synchronously check to see if any route hooks want
    // to prevent the current window/tab from closing.
    if (state.routes) {
      var hooks = getRouteHooksForRoutes(state.routes);

      var message = void 0;
      for (var i = 0, len = hooks.length; typeof message !== 'string' && i < len; ++i) {
        // Passing no args indicates to the user that this is a
        // beforeunload hook. We don't know the next location.
        message = hooks[i]();
      }

      return message;
    }
  }

  var unlistenBefore = void 0,
      unlistenBeforeUnload = void 0;

  function removeListenBeforeHooksForRoute(route) {
    var routeID = getRouteID(route, false);
    if (!routeID) {
      return;
    }

    delete RouteHooks[routeID];

    if (!hasAnyProperties(RouteHooks)) {
      // teardown transition & beforeunload hooks
      if (unlistenBefore) {
        unlistenBefore();
        unlistenBefore = null;
      }

      if (unlistenBeforeUnload) {
        unlistenBeforeUnload();
        unlistenBeforeUnload = null;
      }
    }
  }

  /**
   * Registers the given hook function to run before leaving the given route.
   *
   * During a normal transition, the hook function receives the next location
   * as its only argument and can return either a prompt message (string) to show the user,
   * to make sure they want to leave the page; or `false`, to prevent the transition.
   * Any other return value will have no effect.
   *
   * During the beforeunload event (in browsers) the hook receives no arguments.
   * In this case it must return a prompt message to prevent the transition.
   *
   * Returns a function that may be used to unbind the listener.
   */
  function listenBeforeLeavingRoute(route, hook) {
    // TODO: Warn if they register for a route that isn't currently
    // active. They're probably doing something wrong, like re-creating
    // route objects on every location change.
    var routeID = getRouteID(route);
    var hooks = RouteHooks[routeID];

    if (!hooks) {
      var thereWereNoRouteHooks = !hasAnyProperties(RouteHooks);

      RouteHooks[routeID] = [hook];

      if (thereWereNoRouteHooks) {
        // setup transition & beforeunload hooks
        unlistenBefore = history.listenBefore(transitionHook);

        if (history.listenBeforeUnload) unlistenBeforeUnload = history.listenBeforeUnload(beforeUnloadHook);
      }
    } else {
      if (hooks.indexOf(hook) === -1) {
        process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'adding multiple leave hooks for the same route is deprecated; manage multiple confirmations in your own code instead') : void 0;

        hooks.push(hook);
      }
    }

    return function () {
      var hooks = RouteHooks[routeID];

      if (hooks) {
        var newHooks = hooks.filter(function (item) {
          return item !== hook;
        });

        if (newHooks.length === 0) {
          removeListenBeforeHooksForRoute(route);
        } else {
          RouteHooks[routeID] = newHooks;
        }
      }
    };
  }

  /**
   * This is the API for stateful environments. As the location
   * changes, we update state and call the listener. We can also
   * gracefully handle errors and redirects.
   */
  function listen(listener) {
    // TODO: Only use a single history listener. Otherwise we'll
    // end up with multiple concurrent calls to match.
    return history.listen(function (location) {
      if (state.location === location) {
        listener(null, state);
      } else {
        match(location, function (error, redirectLocation, nextState) {
          if (error) {
            listener(error);
          } else if (redirectLocation) {
            history.replace(redirectLocation);
          } else if (nextState) {
            listener(null, nextState);
          } else {
            process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'Location "%s" did not match any routes', location.pathname + location.search + location.hash) : void 0;
          }
        });
      }
    });
  }

  return {
    isActive: isActive,
    match: match,
    listenBeforeLeavingRoute: listenBeforeLeavingRoute,
    listen: listen
  };
}

//export default useRoutes

module.exports = exports['default'];
}).call(this,require('_process'))

},{"./TransitionUtils":59,"./computeChangedRoutes":62,"./getComponents":67,"./isActive":71,"./matchRoutes":74,"./routerWarning":75,"_process":1}],66:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports.canUseMembrane = undefined;

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var canUseMembrane = exports.canUseMembrane = false;

// No-op by default.
var deprecateObjectProperties = function deprecateObjectProperties(object) {
  return object;
};

if (process.env.NODE_ENV !== 'production') {
  try {
    if (Object.defineProperty({}, 'x', {
      get: function get() {
        return true;
      }
    }).x) {
      exports.canUseMembrane = canUseMembrane = true;
    }
    /* eslint-disable no-empty */
  } catch (e) {}
  /* eslint-enable no-empty */

  if (canUseMembrane) {
    deprecateObjectProperties = function deprecateObjectProperties(object, message) {
      // Wrap the deprecated object in a membrane to warn on property access.
      var membrane = {};

      var _loop = function _loop(prop) {
        if (!Object.prototype.hasOwnProperty.call(object, prop)) {
          return 'continue';
        }

        if (typeof object[prop] === 'function') {
          // Can't use fat arrow here because of use of arguments below.
          membrane[prop] = function () {
            process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, message) : void 0;
            return object[prop].apply(object, arguments);
          };
          return 'continue';
        }

        // These properties are non-enumerable to prevent React dev tools from
        // seeing them and causing spurious warnings when accessing them. In
        // principle this could be done with a proxy, but support for the
        // ownKeys trap on proxies is not universal, even among browsers that
        // otherwise support proxies.
        Object.defineProperty(membrane, prop, {
          get: function get() {
            process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, message) : void 0;
            return object[prop];
          }
        });
      };

      for (var prop in object) {
        var _ret = _loop(prop);

        if (_ret === 'continue') continue;
      }

      return membrane;
    };
  }
}

exports.default = deprecateObjectProperties;
}).call(this,require('_process'))

},{"./routerWarning":75,"_process":1}],67:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _AsyncUtils = require('./AsyncUtils');

var _makeStateWithLocation = require('./makeStateWithLocation');

var _makeStateWithLocation2 = _interopRequireDefault(_makeStateWithLocation);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getComponentsForRoute(nextState, route, callback) {
  if (route.component || route.components) {
    callback(null, route.component || route.components);
    return;
  }

  var getComponent = route.getComponent || route.getComponents;
  if (!getComponent) {
    callback();
    return;
  }

  var location = nextState.location;

  var nextStateWithLocation = (0, _makeStateWithLocation2.default)(nextState, location);

  getComponent.call(route, nextStateWithLocation, callback);
}

/**
 * Asynchronously fetches all components needed for the given router
 * state and calls callback(error, components) when finished.
 *
 * Note: This operation may finish synchronously if no routes have an
 * asynchronous getComponents method.
 */
function getComponents(nextState, callback) {
  (0, _AsyncUtils.mapAsync)(nextState.routes, function (route, index, callback) {
    getComponentsForRoute(nextState, route, callback);
  }, callback);
}

exports.default = getComponents;
module.exports = exports['default'];
},{"./AsyncUtils":41,"./makeStateWithLocation":72}],68:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _PatternUtils = require('./PatternUtils');

/**
 * Extracts an object of params the given route cares about from
 * the given params object.
 */
function getRouteParams(route, params) {
  var routeParams = {};

  if (!route.path) return routeParams;

  (0, _PatternUtils.getParamNames)(route.path).forEach(function (p) {
    if (Object.prototype.hasOwnProperty.call(params, p)) {
      routeParams[p] = params[p];
    }
  });

  return routeParams;
}

exports.default = getRouteParams;
module.exports = exports['default'];
},{"./PatternUtils":49}],69:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _createHashHistory = require('history/lib/createHashHistory');

var _createHashHistory2 = _interopRequireDefault(_createHashHistory);

var _createRouterHistory = require('./createRouterHistory');

var _createRouterHistory2 = _interopRequireDefault(_createRouterHistory);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (0, _createRouterHistory2.default)(_createHashHistory2.default);
module.exports = exports['default'];
},{"./createRouterHistory":64,"history/lib/createHashHistory":16}],70:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.createMemoryHistory = exports.hashHistory = exports.browserHistory = exports.applyRouterMiddleware = exports.formatPattern = exports.useRouterHistory = exports.match = exports.routerShape = exports.locationShape = exports.PropTypes = exports.RoutingContext = exports.RouterContext = exports.createRoutes = exports.useRoutes = exports.RouteContext = exports.Lifecycle = exports.History = exports.Route = exports.Redirect = exports.IndexRoute = exports.IndexRedirect = exports.withRouter = exports.IndexLink = exports.Link = exports.Router = undefined;

var _RouteUtils = require('./RouteUtils');

Object.defineProperty(exports, 'createRoutes', {
  enumerable: true,
  get: function get() {
    return _RouteUtils.createRoutes;
  }
});

var _PropTypes2 = require('./PropTypes');

Object.defineProperty(exports, 'locationShape', {
  enumerable: true,
  get: function get() {
    return _PropTypes2.locationShape;
  }
});
Object.defineProperty(exports, 'routerShape', {
  enumerable: true,
  get: function get() {
    return _PropTypes2.routerShape;
  }
});

var _PatternUtils = require('./PatternUtils');

Object.defineProperty(exports, 'formatPattern', {
  enumerable: true,
  get: function get() {
    return _PatternUtils.formatPattern;
  }
});

var _Router2 = require('./Router');

var _Router3 = _interopRequireDefault(_Router2);

var _Link2 = require('./Link');

var _Link3 = _interopRequireDefault(_Link2);

var _IndexLink2 = require('./IndexLink');

var _IndexLink3 = _interopRequireDefault(_IndexLink2);

var _withRouter2 = require('./withRouter');

var _withRouter3 = _interopRequireDefault(_withRouter2);

var _IndexRedirect2 = require('./IndexRedirect');

var _IndexRedirect3 = _interopRequireDefault(_IndexRedirect2);

var _IndexRoute2 = require('./IndexRoute');

var _IndexRoute3 = _interopRequireDefault(_IndexRoute2);

var _Redirect2 = require('./Redirect');

var _Redirect3 = _interopRequireDefault(_Redirect2);

var _Route2 = require('./Route');

var _Route3 = _interopRequireDefault(_Route2);

var _History2 = require('./History');

var _History3 = _interopRequireDefault(_History2);

var _Lifecycle2 = require('./Lifecycle');

var _Lifecycle3 = _interopRequireDefault(_Lifecycle2);

var _RouteContext2 = require('./RouteContext');

var _RouteContext3 = _interopRequireDefault(_RouteContext2);

var _useRoutes2 = require('./useRoutes');

var _useRoutes3 = _interopRequireDefault(_useRoutes2);

var _RouterContext2 = require('./RouterContext');

var _RouterContext3 = _interopRequireDefault(_RouterContext2);

var _RoutingContext2 = require('./RoutingContext');

var _RoutingContext3 = _interopRequireDefault(_RoutingContext2);

var _PropTypes3 = _interopRequireDefault(_PropTypes2);

var _match2 = require('./match');

var _match3 = _interopRequireDefault(_match2);

var _useRouterHistory2 = require('./useRouterHistory');

var _useRouterHistory3 = _interopRequireDefault(_useRouterHistory2);

var _applyRouterMiddleware2 = require('./applyRouterMiddleware');

var _applyRouterMiddleware3 = _interopRequireDefault(_applyRouterMiddleware2);

var _browserHistory2 = require('./browserHistory');

var _browserHistory3 = _interopRequireDefault(_browserHistory2);

var _hashHistory2 = require('./hashHistory');

var _hashHistory3 = _interopRequireDefault(_hashHistory2);

var _createMemoryHistory2 = require('./createMemoryHistory');

var _createMemoryHistory3 = _interopRequireDefault(_createMemoryHistory2);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.Router = _Router3.default; /* components */

exports.Link = _Link3.default;
exports.IndexLink = _IndexLink3.default;
exports.withRouter = _withRouter3.default;

/* components (configuration) */

exports.IndexRedirect = _IndexRedirect3.default;
exports.IndexRoute = _IndexRoute3.default;
exports.Redirect = _Redirect3.default;
exports.Route = _Route3.default;

/* mixins */

exports.History = _History3.default;
exports.Lifecycle = _Lifecycle3.default;
exports.RouteContext = _RouteContext3.default;

/* utils */

exports.useRoutes = _useRoutes3.default;
exports.RouterContext = _RouterContext3.default;
exports.RoutingContext = _RoutingContext3.default;
exports.PropTypes = _PropTypes3.default;
exports.match = _match3.default;
exports.useRouterHistory = _useRouterHistory3.default;
exports.applyRouterMiddleware = _applyRouterMiddleware3.default;

/* histories */

exports.browserHistory = _browserHistory3.default;
exports.hashHistory = _hashHistory3.default;
exports.createMemoryHistory = _createMemoryHistory3.default;
},{"./History":42,"./IndexLink":43,"./IndexRedirect":44,"./IndexRoute":45,"./Lifecycle":47,"./Link":48,"./PatternUtils":49,"./PropTypes":50,"./Redirect":51,"./Route":52,"./RouteContext":53,"./RouteUtils":54,"./Router":55,"./RouterContext":56,"./RoutingContext":58,"./applyRouterMiddleware":60,"./browserHistory":61,"./createMemoryHistory":63,"./hashHistory":69,"./match":73,"./useRouterHistory":76,"./useRoutes":77,"./withRouter":78}],71:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol ? "symbol" : typeof obj; };

exports.default = isActive;

var _PatternUtils = require('./PatternUtils');

function deepEqual(a, b) {
  if (a == b) return true;

  if (a == null || b == null) return false;

  if (Array.isArray(a)) {
    return Array.isArray(b) && a.length === b.length && a.every(function (item, index) {
      return deepEqual(item, b[index]);
    });
  }

  if ((typeof a === 'undefined' ? 'undefined' : _typeof(a)) === 'object') {
    for (var p in a) {
      if (!Object.prototype.hasOwnProperty.call(a, p)) {
        continue;
      }

      if (a[p] === undefined) {
        if (b[p] !== undefined) {
          return false;
        }
      } else if (!Object.prototype.hasOwnProperty.call(b, p)) {
        return false;
      } else if (!deepEqual(a[p], b[p])) {
        return false;
      }
    }

    return true;
  }

  return String(a) === String(b);
}

/**
 * Returns true if the current pathname matches the supplied one, net of
 * leading and trailing slash normalization. This is sufficient for an
 * indexOnly route match.
 */
function pathIsActive(pathname, currentPathname) {
  // Normalize leading slash for consistency. Leading slash on pathname has
  // already been normalized in isActive. See caveat there.
  if (currentPathname.charAt(0) !== '/') {
    currentPathname = '/' + currentPathname;
  }

  // Normalize the end of both path names too. Maybe `/foo/` shouldn't show
  // `/foo` as active, but in this case, we would already have failed the
  // match.
  if (pathname.charAt(pathname.length - 1) !== '/') {
    pathname += '/';
  }
  if (currentPathname.charAt(currentPathname.length - 1) !== '/') {
    currentPathname += '/';
  }

  return currentPathname === pathname;
}

/**
 * Returns true if the given pathname matches the active routes and params.
 */
function routeIsActive(pathname, routes, params) {
  var remainingPathname = pathname,
      paramNames = [],
      paramValues = [];

  // for...of would work here but it's probably slower post-transpilation.
  for (var i = 0, len = routes.length; i < len; ++i) {
    var route = routes[i];
    var pattern = route.path || '';

    if (pattern.charAt(0) === '/') {
      remainingPathname = pathname;
      paramNames = [];
      paramValues = [];
    }

    if (remainingPathname !== null && pattern) {
      var matched = (0, _PatternUtils.matchPattern)(pattern, remainingPathname);
      if (matched) {
        remainingPathname = matched.remainingPathname;
        paramNames = [].concat(paramNames, matched.paramNames);
        paramValues = [].concat(paramValues, matched.paramValues);
      } else {
        remainingPathname = null;
      }

      if (remainingPathname === '') {
        // We have an exact match on the route. Just check that all the params
        // match.
        // FIXME: This doesn't work on repeated params.
        return paramNames.every(function (paramName, index) {
          return String(paramValues[index]) === String(params[paramName]);
        });
      }
    }
  }

  return false;
}

/**
 * Returns true if all key/value pairs in the given query are
 * currently active.
 */
function queryIsActive(query, activeQuery) {
  if (activeQuery == null) return query == null;

  if (query == null) return true;

  return deepEqual(query, activeQuery);
}

/**
 * Returns true if a <Link> to the given pathname/query combination is
 * currently active.
 */
function isActive(_ref, indexOnly, currentLocation, routes, params) {
  var pathname = _ref.pathname;
  var query = _ref.query;

  if (currentLocation == null) return false;

  // TODO: This is a bit ugly. It keeps around support for treating pathnames
  // without preceding slashes as absolute paths, but possibly also works
  // around the same quirks with basenames as in matchRoutes.
  if (pathname.charAt(0) !== '/') {
    pathname = '/' + pathname;
  }

  if (!pathIsActive(pathname, currentLocation.pathname)) {
    // The path check is necessary and sufficient for indexOnly, but otherwise
    // we still need to check the routes.
    if (indexOnly || !routeIsActive(pathname, routes, params)) {
      return false;
    }
  }

  return queryIsActive(query, currentLocation.query);
}
module.exports = exports['default'];
},{"./PatternUtils":49}],72:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = makeStateWithLocation;

var _deprecateObjectProperties = require('./deprecateObjectProperties');

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function makeStateWithLocation(state, location) {
  if (process.env.NODE_ENV !== 'production' && _deprecateObjectProperties.canUseMembrane) {
    var stateWithLocation = _extends({}, state);

    // I don't use deprecateObjectProperties here because I want to keep the
    // same code path between development and production, in that we just
    // assign extra properties to the copy of the state object in both cases.

    var _loop = function _loop(prop) {
      if (!Object.prototype.hasOwnProperty.call(location, prop)) {
        return 'continue';
      }

      Object.defineProperty(stateWithLocation, prop, {
        get: function get() {
          process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, 'Accessing location properties directly from the first argument to `getComponent`, `getComponents`, `getChildRoutes`, and `getIndexRoute` is deprecated. That argument is now the router state (`nextState` or `partialNextState`) rather than the location. To access the location, use `nextState.location` or `partialNextState.location`.') : void 0;
          return location[prop];
        }
      });
    };

    for (var prop in location) {
      var _ret = _loop(prop);

      if (_ret === 'continue') continue;
    }

    return stateWithLocation;
  }

  return _extends({}, state, location);
}
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./deprecateObjectProperties":66,"./routerWarning":75,"_process":1}],73:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _Actions = require('history/lib/Actions');

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _createMemoryHistory = require('./createMemoryHistory');

var _createMemoryHistory2 = _interopRequireDefault(_createMemoryHistory);

var _createTransitionManager = require('./createTransitionManager');

var _createTransitionManager2 = _interopRequireDefault(_createTransitionManager);

var _RouteUtils = require('./RouteUtils');

var _RouterUtils = require('./RouterUtils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

/**
 * A high-level API to be used for server-side rendering.
 *
 * This function matches a location to a set of routes and calls
 * callback(error, redirectLocation, renderProps) when finished.
 *
 * Note: You probably don't want to use this in a browser unless you're using
 * server-side rendering with async routes.
 */
function match(_ref, callback) {
  var history = _ref.history;
  var routes = _ref.routes;
  var location = _ref.location;

  var options = _objectWithoutProperties(_ref, ['history', 'routes', 'location']);

  !(history || location) ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'match needs a history or a location') : (0, _invariant2.default)(false) : void 0;

  history = history ? history : (0, _createMemoryHistory2.default)(options);
  var transitionManager = (0, _createTransitionManager2.default)(history, (0, _RouteUtils.createRoutes)(routes));

  var unlisten = void 0;

  if (location) {
    // Allow match({ location: '/the/path', ... })
    location = history.createLocation(location);
  } else {
    // Pick up the location from the history via synchronous history.listen
    // call if needed.
    unlisten = history.listen(function (historyLocation) {
      location = historyLocation;
    });
  }

  var router = (0, _RouterUtils.createRouterObject)(history, transitionManager);
  history = (0, _RouterUtils.createRoutingHistory)(history, transitionManager);

  transitionManager.match(location, function (error, redirectLocation, nextState) {
    callback(error, redirectLocation && router.createLocation(redirectLocation, _Actions.REPLACE), nextState && _extends({}, nextState, {
      history: history,
      router: router,
      matchContext: { history: history, transitionManager: transitionManager, router: router }
    }));

    // Defer removing the listener to here to prevent DOM histories from having
    // to unwind DOM event listeners unnecessarily, in case callback renders a
    // <Router> and attaches another history listener.
    if (unlisten) {
      unlisten();
    }
  });
}

exports.default = match;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./RouteUtils":54,"./RouterUtils":57,"./createMemoryHistory":63,"./createTransitionManager":65,"_process":1,"history/lib/Actions":8,"invariant":26}],74:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol ? "symbol" : typeof obj; };

exports.default = matchRoutes;

var _AsyncUtils = require('./AsyncUtils');

var _makeStateWithLocation = require('./makeStateWithLocation');

var _makeStateWithLocation2 = _interopRequireDefault(_makeStateWithLocation);

var _PatternUtils = require('./PatternUtils');

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _RouteUtils = require('./RouteUtils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getChildRoutes(route, location, paramNames, paramValues, callback) {
  if (route.childRoutes) {
    return [null, route.childRoutes];
  }
  if (!route.getChildRoutes) {
    return [];
  }

  var sync = true,
      result = void 0;

  var partialNextState = {
    location: location,
    params: createParams(paramNames, paramValues)
  };

  var partialNextStateWithLocation = (0, _makeStateWithLocation2.default)(partialNextState, location);

  route.getChildRoutes(partialNextStateWithLocation, function (error, childRoutes) {
    childRoutes = !error && (0, _RouteUtils.createRoutes)(childRoutes);
    if (sync) {
      result = [error, childRoutes];
      return;
    }

    callback(error, childRoutes);
  });

  sync = false;
  return result; // Might be undefined.
}

function getIndexRoute(route, location, paramNames, paramValues, callback) {
  if (route.indexRoute) {
    callback(null, route.indexRoute);
  } else if (route.getIndexRoute) {
    var partialNextState = {
      location: location,
      params: createParams(paramNames, paramValues)
    };

    var partialNextStateWithLocation = (0, _makeStateWithLocation2.default)(partialNextState, location);

    route.getIndexRoute(partialNextStateWithLocation, function (error, indexRoute) {
      callback(error, !error && (0, _RouteUtils.createRoutes)(indexRoute)[0]);
    });
  } else if (route.childRoutes) {
    (function () {
      var pathless = route.childRoutes.filter(function (childRoute) {
        return !childRoute.path;
      });

      (0, _AsyncUtils.loopAsync)(pathless.length, function (index, next, done) {
        getIndexRoute(pathless[index], location, paramNames, paramValues, function (error, indexRoute) {
          if (error || indexRoute) {
            var routes = [pathless[index]].concat(Array.isArray(indexRoute) ? indexRoute : [indexRoute]);
            done(error, routes);
          } else {
            next();
          }
        });
      }, function (err, routes) {
        callback(null, routes);
      });
    })();
  } else {
    callback();
  }
}

function assignParams(params, paramNames, paramValues) {
  return paramNames.reduce(function (params, paramName, index) {
    var paramValue = paramValues && paramValues[index];

    if (Array.isArray(params[paramName])) {
      params[paramName].push(paramValue);
    } else if (paramName in params) {
      params[paramName] = [params[paramName], paramValue];
    } else {
      params[paramName] = paramValue;
    }

    return params;
  }, params);
}

function createParams(paramNames, paramValues) {
  return assignParams({}, paramNames, paramValues);
}

function matchRouteDeep(route, location, remainingPathname, paramNames, paramValues, callback) {
  var pattern = route.path || '';

  if (pattern.charAt(0) === '/') {
    remainingPathname = location.pathname;
    paramNames = [];
    paramValues = [];
  }

  // Only try to match the path if the route actually has a pattern, and if
  // we're not just searching for potential nested absolute paths.
  if (remainingPathname !== null && pattern) {
    try {
      var matched = (0, _PatternUtils.matchPattern)(pattern, remainingPathname);
      if (matched) {
        remainingPathname = matched.remainingPathname;
        paramNames = [].concat(paramNames, matched.paramNames);
        paramValues = [].concat(paramValues, matched.paramValues);
      } else {
        remainingPathname = null;
      }
    } catch (error) {
      callback(error);
    }

    // By assumption, pattern is non-empty here, which is the prerequisite for
    // actually terminating a match.
    if (remainingPathname === '') {
      var _ret2 = function () {
        var match = {
          routes: [route],
          params: createParams(paramNames, paramValues)
        };

        getIndexRoute(route, location, paramNames, paramValues, function (error, indexRoute) {
          if (error) {
            callback(error);
          } else {
            if (Array.isArray(indexRoute)) {
              var _match$routes;

              process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(indexRoute.every(function (route) {
                return !route.path;
              }), 'Index routes should not have paths') : void 0;
              (_match$routes = match.routes).push.apply(_match$routes, indexRoute);
            } else if (indexRoute) {
              process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(!indexRoute.path, 'Index routes should not have paths') : void 0;
              match.routes.push(indexRoute);
            }

            callback(null, match);
          }
        });

        return {
          v: void 0
        };
      }();

      if ((typeof _ret2 === 'undefined' ? 'undefined' : _typeof(_ret2)) === "object") return _ret2.v;
    }
  }

  if (remainingPathname != null || route.childRoutes) {
    // Either a) this route matched at least some of the path or b)
    // we don't have to load this route's children asynchronously. In
    // either case continue checking for matches in the subtree.
    var onChildRoutes = function onChildRoutes(error, childRoutes) {
      if (error) {
        callback(error);
      } else if (childRoutes) {
        // Check the child routes to see if any of them match.
        matchRoutes(childRoutes, location, function (error, match) {
          if (error) {
            callback(error);
          } else if (match) {
            // A child route matched! Augment the match and pass it up the stack.
            match.routes.unshift(route);
            callback(null, match);
          } else {
            callback();
          }
        }, remainingPathname, paramNames, paramValues);
      } else {
        callback();
      }
    };

    var result = getChildRoutes(route, location, paramNames, paramValues, onChildRoutes);
    if (result) {
      onChildRoutes.apply(undefined, result);
    }
  } else {
    callback();
  }
}

/**
 * Asynchronously matches the given location to a set of routes and calls
 * callback(error, state) when finished. The state object will have the
 * following properties:
 *
 * - routes       An array of routes that matched, in hierarchical order
 * - params       An object of URL parameters
 *
 * Note: This operation may finish synchronously if no routes have an
 * asynchronous getChildRoutes method.
 */
function matchRoutes(routes, location, callback, remainingPathname) {
  var paramNames = arguments.length <= 4 || arguments[4] === undefined ? [] : arguments[4];
  var paramValues = arguments.length <= 5 || arguments[5] === undefined ? [] : arguments[5];

  if (remainingPathname === undefined) {
    // TODO: This is a little bit ugly, but it works around a quirk in history
    // that strips the leading slash from pathnames when using basenames with
    // trailing slashes.
    if (location.pathname.charAt(0) !== '/') {
      location = _extends({}, location, {
        pathname: '/' + location.pathname
      });
    }
    remainingPathname = location.pathname;
  }

  (0, _AsyncUtils.loopAsync)(routes.length, function (index, next, done) {
    matchRouteDeep(routes[index], location, remainingPathname, paramNames, paramValues, function (error, match) {
      if (error || match) {
        done(error, match);
      } else {
        next();
      }
    });
  }, callback);
}
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./AsyncUtils":41,"./PatternUtils":49,"./RouteUtils":54,"./makeStateWithLocation":72,"./routerWarning":75,"_process":1}],75:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.default = routerWarning;
exports._resetWarned = _resetWarned;

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var warned = {};

function routerWarning(falseToWarn, message) {
  // Only issue deprecation warnings once.
  if (message.indexOf('deprecated') !== -1) {
    if (warned[message]) {
      return;
    }

    warned[message] = true;
  }

  message = '[react-router] ' + message;

  for (var _len = arguments.length, args = Array(_len > 2 ? _len - 2 : 0), _key = 2; _key < _len; _key++) {
    args[_key - 2] = arguments[_key];
  }

  _warning2.default.apply(undefined, [falseToWarn, message].concat(args));
}

function _resetWarned() {
  warned = {};
}
},{"warning":92}],76:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.default = useRouterHistory;

var _useQueries = require('history/lib/useQueries');

var _useQueries2 = _interopRequireDefault(_useQueries);

var _useBasename = require('history/lib/useBasename');

var _useBasename2 = _interopRequireDefault(_useBasename);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function useRouterHistory(createHistory) {
  return function (options) {
    var history = (0, _useQueries2.default)((0, _useBasename2.default)(createHistory))(options);
    history.__v2_compatible__ = true;
    return history;
  };
}
module.exports = exports['default'];
},{"history/lib/useBasename":22,"history/lib/useQueries":23}],77:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _useQueries = require('history/lib/useQueries');

var _useQueries2 = _interopRequireDefault(_useQueries);

var _createTransitionManager = require('./createTransitionManager');

var _createTransitionManager2 = _interopRequireDefault(_createTransitionManager);

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

/**
 * Returns a new createHistory function that may be used to create
 * history objects that know about routing.
 *
 * Enhances history objects with the following methods:
 *
 * - listen((error, nextState) => {})
 * - listenBeforeLeavingRoute(route, (nextLocation) => {})
 * - match(location, (error, redirectLocation, nextState) => {})
 * - isActive(pathname, query, indexOnly=false)
 */
function useRoutes(createHistory) {
  process.env.NODE_ENV !== 'production' ? (0, _routerWarning2.default)(false, '`useRoutes` is deprecated. Please use `createTransitionManager` instead.') : void 0;

  return function () {
    var _ref = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];

    var routes = _ref.routes;

    var options = _objectWithoutProperties(_ref, ['routes']);

    var history = (0, _useQueries2.default)(createHistory)(options);
    var transitionManager = (0, _createTransitionManager2.default)(history, routes);
    return _extends({}, history, transitionManager);
  };
}

exports.default = useRoutes;
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./createTransitionManager":65,"./routerWarning":75,"_process":1,"history/lib/useQueries":23}],78:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = withRouter;

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _hoistNonReactStatics = require('hoist-non-react-statics');

var _hoistNonReactStatics2 = _interopRequireDefault(_hoistNonReactStatics);

var _PropTypes = require('./PropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getDisplayName(WrappedComponent) {
  return WrappedComponent.displayName || WrappedComponent.name || 'Component';
}

function withRouter(WrappedComponent, options) {
  var withRef = options && options.withRef;

  var WithRouter = _react2.default.createClass({
    displayName: 'WithRouter',

    contextTypes: { router: _PropTypes.routerShape },
    propTypes: { router: _PropTypes.routerShape },

    getWrappedInstance: function getWrappedInstance() {
      !withRef ? process.env.NODE_ENV !== 'production' ? (0, _invariant2.default)(false, 'To access the wrapped instance, you need to specify ' + '`{ withRef: true }` as the second argument of the withRouter() call.') : (0, _invariant2.default)(false) : void 0;

      return this.wrappedInstance;
    },
    render: function render() {
      var _this = this;

      var router = this.props.router || this.context.router;
      var props = _extends({}, this.props, { router: router });

      if (withRef) {
        props.ref = function (c) {
          _this.wrappedInstance = c;
        };
      }

      return _react2.default.createElement(WrappedComponent, props);
    }
  });

  WithRouter.displayName = 'withRouter(' + getDisplayName(WrappedComponent) + ')';
  WithRouter.WrappedComponent = WrappedComponent;

  return (0, _hoistNonReactStatics2.default)(WithRouter, WrappedComponent);
}
module.exports = exports['default'];
}).call(this,require('_process'))

},{"./PropTypes":50,"_process":1,"hoist-non-react-statics":25,"invariant":26,"react":"react"}],79:[function(require,module,exports){
'use strict';

function thunkMiddleware(_ref) {
  var dispatch = _ref.dispatch;
  var getState = _ref.getState;

  return function (next) {
    return function (action) {
      return typeof action === 'function' ? action(dispatch, getState) : next(action);
    };
  };
}

module.exports = thunkMiddleware;
},{}],80:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports['default'] = applyMiddleware;

var _compose = require('./compose');

var _compose2 = _interopRequireDefault(_compose);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

/**
 * Creates a store enhancer that applies middleware to the dispatch method
 * of the Redux store. This is handy for a variety of tasks, such as expressing
 * asynchronous actions in a concise manner, or logging every action payload.
 *
 * See `redux-thunk` package as an example of the Redux middleware.
 *
 * Because middleware is potentially asynchronous, this should be the first
 * store enhancer in the composition chain.
 *
 * Note that each middleware will be given the `dispatch` and `getState` functions
 * as named arguments.
 *
 * @param {...Function} middlewares The middleware chain to be applied.
 * @returns {Function} A store enhancer applying the middleware.
 */
function applyMiddleware() {
  for (var _len = arguments.length, middlewares = Array(_len), _key = 0; _key < _len; _key++) {
    middlewares[_key] = arguments[_key];
  }

  return function (createStore) {
    return function (reducer, preloadedState, enhancer) {
      var store = createStore(reducer, preloadedState, enhancer);
      var _dispatch = store.dispatch;
      var chain = [];

      var middlewareAPI = {
        getState: store.getState,
        dispatch: function dispatch(action) {
          return _dispatch(action);
        }
      };
      chain = middlewares.map(function (middleware) {
        return middleware(middlewareAPI);
      });
      _dispatch = _compose2['default'].apply(undefined, chain)(store.dispatch);

      return _extends({}, store, {
        dispatch: _dispatch
      });
    };
  };
}
},{"./compose":83}],81:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports['default'] = bindActionCreators;
function bindActionCreator(actionCreator, dispatch) {
  return function () {
    return dispatch(actionCreator.apply(undefined, arguments));
  };
}

/**
 * Turns an object whose values are action creators, into an object with the
 * same keys, but with every function wrapped into a `dispatch` call so they
 * may be invoked directly. This is just a convenience method, as you can call
 * `store.dispatch(MyActionCreators.doSomething())` yourself just fine.
 *
 * For convenience, you can also pass a single function as the first argument,
 * and get a function in return.
 *
 * @param {Function|Object} actionCreators An object whose values are action
 * creator functions. One handy way to obtain it is to use ES6 `import * as`
 * syntax. You may also pass a single function.
 *
 * @param {Function} dispatch The `dispatch` function available on your Redux
 * store.
 *
 * @returns {Function|Object} The object mimicking the original object, but with
 * every action creator wrapped into the `dispatch` call. If you passed a
 * function as `actionCreators`, the return value will also be a single
 * function.
 */
function bindActionCreators(actionCreators, dispatch) {
  if (typeof actionCreators === 'function') {
    return bindActionCreator(actionCreators, dispatch);
  }

  if (typeof actionCreators !== 'object' || actionCreators === null) {
    throw new Error('bindActionCreators expected an object or a function, instead received ' + (actionCreators === null ? 'null' : typeof actionCreators) + '. ' + 'Did you write "import ActionCreators from" instead of "import * as ActionCreators from"?');
  }

  var keys = Object.keys(actionCreators);
  var boundActionCreators = {};
  for (var i = 0; i < keys.length; i++) {
    var key = keys[i];
    var actionCreator = actionCreators[key];
    if (typeof actionCreator === 'function') {
      boundActionCreators[key] = bindActionCreator(actionCreator, dispatch);
    }
  }
  return boundActionCreators;
}
},{}],82:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports['default'] = combineReducers;

var _createStore = require('./createStore');

var _isPlainObject = require('lodash/isPlainObject');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _warning = require('./utils/warning');

var _warning2 = _interopRequireDefault(_warning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

function getUndefinedStateErrorMessage(key, action) {
  var actionType = action && action.type;
  var actionName = actionType && '"' + actionType.toString() + '"' || 'an action';

  return 'Given action ' + actionName + ', reducer "' + key + '" returned undefined. ' + 'To ignore an action, you must explicitly return the previous state.';
}

function getUnexpectedStateShapeWarningMessage(inputState, reducers, action, unexpectedKeyCache) {
  var reducerKeys = Object.keys(reducers);
  var argumentName = action && action.type === _createStore.ActionTypes.INIT ? 'preloadedState argument passed to createStore' : 'previous state received by the reducer';

  if (reducerKeys.length === 0) {
    return 'Store does not have a valid reducer. Make sure the argument passed ' + 'to combineReducers is an object whose values are reducers.';
  }

  if (!(0, _isPlainObject2['default'])(inputState)) {
    return 'The ' + argumentName + ' has unexpected type of "' + {}.toString.call(inputState).match(/\s([a-z|A-Z]+)/)[1] + '". Expected argument to be an object with the following ' + ('keys: "' + reducerKeys.join('", "') + '"');
  }

  var unexpectedKeys = Object.keys(inputState).filter(function (key) {
    return !reducers.hasOwnProperty(key) && !unexpectedKeyCache[key];
  });

  unexpectedKeys.forEach(function (key) {
    unexpectedKeyCache[key] = true;
  });

  if (unexpectedKeys.length > 0) {
    return 'Unexpected ' + (unexpectedKeys.length > 1 ? 'keys' : 'key') + ' ' + ('"' + unexpectedKeys.join('", "') + '" found in ' + argumentName + '. ') + 'Expected to find one of the known reducer keys instead: ' + ('"' + reducerKeys.join('", "') + '". Unexpected keys will be ignored.');
  }
}

function assertReducerSanity(reducers) {
  Object.keys(reducers).forEach(function (key) {
    var reducer = reducers[key];
    var initialState = reducer(undefined, { type: _createStore.ActionTypes.INIT });

    if (typeof initialState === 'undefined') {
      throw new Error('Reducer "' + key + '" returned undefined during initialization. ' + 'If the state passed to the reducer is undefined, you must ' + 'explicitly return the initial state. The initial state may ' + 'not be undefined.');
    }

    var type = '@@redux/PROBE_UNKNOWN_ACTION_' + Math.random().toString(36).substring(7).split('').join('.');
    if (typeof reducer(undefined, { type: type }) === 'undefined') {
      throw new Error('Reducer "' + key + '" returned undefined when probed with a random type. ' + ('Don\'t try to handle ' + _createStore.ActionTypes.INIT + ' or other actions in "redux/*" ') + 'namespace. They are considered private. Instead, you must return the ' + 'current state for any unknown actions, unless it is undefined, ' + 'in which case you must return the initial state, regardless of the ' + 'action type. The initial state may not be undefined.');
    }
  });
}

/**
 * Turns an object whose values are different reducer functions, into a single
 * reducer function. It will call every child reducer, and gather their results
 * into a single state object, whose keys correspond to the keys of the passed
 * reducer functions.
 *
 * @param {Object} reducers An object whose values correspond to different
 * reducer functions that need to be combined into one. One handy way to obtain
 * it is to use ES6 `import * as reducers` syntax. The reducers may never return
 * undefined for any action. Instead, they should return their initial state
 * if the state passed to them was undefined, and the current state for any
 * unrecognized action.
 *
 * @returns {Function} A reducer function that invokes every reducer inside the
 * passed object, and builds a state object with the same shape.
 */
function combineReducers(reducers) {
  var reducerKeys = Object.keys(reducers);
  var finalReducers = {};
  for (var i = 0; i < reducerKeys.length; i++) {
    var key = reducerKeys[i];

    if (process.env.NODE_ENV !== 'production') {
      if (typeof reducers[key] === 'undefined') {
        (0, _warning2['default'])('No reducer provided for key "' + key + '"');
      }
    }

    if (typeof reducers[key] === 'function') {
      finalReducers[key] = reducers[key];
    }
  }
  var finalReducerKeys = Object.keys(finalReducers);

  if (process.env.NODE_ENV !== 'production') {
    var unexpectedKeyCache = {};
  }

  var sanityError;
  try {
    assertReducerSanity(finalReducers);
  } catch (e) {
    sanityError = e;
  }

  return function combination() {
    var state = arguments.length <= 0 || arguments[0] === undefined ? {} : arguments[0];
    var action = arguments[1];

    if (sanityError) {
      throw sanityError;
    }

    if (process.env.NODE_ENV !== 'production') {
      var warningMessage = getUnexpectedStateShapeWarningMessage(state, finalReducers, action, unexpectedKeyCache);
      if (warningMessage) {
        (0, _warning2['default'])(warningMessage);
      }
    }

    var hasChanged = false;
    var nextState = {};
    for (var i = 0; i < finalReducerKeys.length; i++) {
      var key = finalReducerKeys[i];
      var reducer = finalReducers[key];
      var previousStateForKey = state[key];
      var nextStateForKey = reducer(previousStateForKey, action);
      if (typeof nextStateForKey === 'undefined') {
        var errorMessage = getUndefinedStateErrorMessage(key, action);
        throw new Error(errorMessage);
      }
      nextState[key] = nextStateForKey;
      hasChanged = hasChanged || nextStateForKey !== previousStateForKey;
    }
    return hasChanged ? nextState : state;
  };
}
}).call(this,require('_process'))

},{"./createStore":84,"./utils/warning":86,"_process":1,"lodash/isPlainObject":31}],83:[function(require,module,exports){
"use strict";

exports.__esModule = true;
exports["default"] = compose;
/**
 * Composes single-argument functions from right to left. The rightmost
 * function can take multiple arguments as it provides the signature for
 * the resulting composite function.
 *
 * @param {...Function} funcs The functions to compose.
 * @returns {Function} A function obtained by composing the argument functions
 * from right to left. For example, compose(f, g, h) is identical to doing
 * (...args) => f(g(h(...args))).
 */

function compose() {
  for (var _len = arguments.length, funcs = Array(_len), _key = 0; _key < _len; _key++) {
    funcs[_key] = arguments[_key];
  }

  if (funcs.length === 0) {
    return function (arg) {
      return arg;
    };
  }

  if (funcs.length === 1) {
    return funcs[0];
  }

  var last = funcs[funcs.length - 1];
  var rest = funcs.slice(0, -1);
  return function () {
    return rest.reduceRight(function (composed, f) {
      return f(composed);
    }, last.apply(undefined, arguments));
  };
}
},{}],84:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.ActionTypes = undefined;
exports['default'] = createStore;

var _isPlainObject = require('lodash/isPlainObject');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _symbolObservable = require('symbol-observable');

var _symbolObservable2 = _interopRequireDefault(_symbolObservable);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

/**
 * These are private action types reserved by Redux.
 * For any unknown actions, you must return the current state.
 * If the current state is undefined, you must return the initial state.
 * Do not reference these action types directly in your code.
 */
var ActionTypes = exports.ActionTypes = {
  INIT: '@@redux/INIT'
};

/**
 * Creates a Redux store that holds the state tree.
 * The only way to change the data in the store is to call `dispatch()` on it.
 *
 * There should only be a single store in your app. To specify how different
 * parts of the state tree respond to actions, you may combine several reducers
 * into a single reducer function by using `combineReducers`.
 *
 * @param {Function} reducer A function that returns the next state tree, given
 * the current state tree and the action to handle.
 *
 * @param {any} [preloadedState] The initial state. You may optionally specify it
 * to hydrate the state from the server in universal apps, or to restore a
 * previously serialized user session.
 * If you use `combineReducers` to produce the root reducer function, this must be
 * an object with the same shape as `combineReducers` keys.
 *
 * @param {Function} enhancer The store enhancer. You may optionally specify it
 * to enhance the store with third-party capabilities such as middleware,
 * time travel, persistence, etc. The only store enhancer that ships with Redux
 * is `applyMiddleware()`.
 *
 * @returns {Store} A Redux store that lets you read the state, dispatch actions
 * and subscribe to changes.
 */
function createStore(reducer, preloadedState, enhancer) {
  var _ref2;

  if (typeof preloadedState === 'function' && typeof enhancer === 'undefined') {
    enhancer = preloadedState;
    preloadedState = undefined;
  }

  if (typeof enhancer !== 'undefined') {
    if (typeof enhancer !== 'function') {
      throw new Error('Expected the enhancer to be a function.');
    }

    return enhancer(createStore)(reducer, preloadedState);
  }

  if (typeof reducer !== 'function') {
    throw new Error('Expected the reducer to be a function.');
  }

  var currentReducer = reducer;
  var currentState = preloadedState;
  var currentListeners = [];
  var nextListeners = currentListeners;
  var isDispatching = false;

  function ensureCanMutateNextListeners() {
    if (nextListeners === currentListeners) {
      nextListeners = currentListeners.slice();
    }
  }

  /**
   * Reads the state tree managed by the store.
   *
   * @returns {any} The current state tree of your application.
   */
  function getState() {
    return currentState;
  }

  /**
   * Adds a change listener. It will be called any time an action is dispatched,
   * and some part of the state tree may potentially have changed. You may then
   * call `getState()` to read the current state tree inside the callback.
   *
   * You may call `dispatch()` from a change listener, with the following
   * caveats:
   *
   * 1. The subscriptions are snapshotted just before every `dispatch()` call.
   * If you subscribe or unsubscribe while the listeners are being invoked, this
   * will not have any effect on the `dispatch()` that is currently in progress.
   * However, the next `dispatch()` call, whether nested or not, will use a more
   * recent snapshot of the subscription list.
   *
   * 2. The listener should not expect to see all state changes, as the state
   * might have been updated multiple times during a nested `dispatch()` before
   * the listener is called. It is, however, guaranteed that all subscribers
   * registered before the `dispatch()` started will be called with the latest
   * state by the time it exits.
   *
   * @param {Function} listener A callback to be invoked on every dispatch.
   * @returns {Function} A function to remove this change listener.
   */
  function subscribe(listener) {
    if (typeof listener !== 'function') {
      throw new Error('Expected listener to be a function.');
    }

    var isSubscribed = true;

    ensureCanMutateNextListeners();
    nextListeners.push(listener);

    return function unsubscribe() {
      if (!isSubscribed) {
        return;
      }

      isSubscribed = false;

      ensureCanMutateNextListeners();
      var index = nextListeners.indexOf(listener);
      nextListeners.splice(index, 1);
    };
  }

  /**
   * Dispatches an action. It is the only way to trigger a state change.
   *
   * The `reducer` function, used to create the store, will be called with the
   * current state tree and the given `action`. Its return value will
   * be considered the **next** state of the tree, and the change listeners
   * will be notified.
   *
   * The base implementation only supports plain object actions. If you want to
   * dispatch a Promise, an Observable, a thunk, or something else, you need to
   * wrap your store creating function into the corresponding middleware. For
   * example, see the documentation for the `redux-thunk` package. Even the
   * middleware will eventually dispatch plain object actions using this method.
   *
   * @param {Object} action A plain object representing “what changed”. It is
   * a good idea to keep actions serializable so you can record and replay user
   * sessions, or use the time travelling `redux-devtools`. An action must have
   * a `type` property which may not be `undefined`. It is a good idea to use
   * string constants for action types.
   *
   * @returns {Object} For convenience, the same action object you dispatched.
   *
   * Note that, if you use a custom middleware, it may wrap `dispatch()` to
   * return something else (for example, a Promise you can await).
   */
  function dispatch(action) {
    if (!(0, _isPlainObject2['default'])(action)) {
      throw new Error('Actions must be plain objects. ' + 'Use custom middleware for async actions.');
    }

    if (typeof action.type === 'undefined') {
      throw new Error('Actions may not have an undefined "type" property. ' + 'Have you misspelled a constant?');
    }

    if (isDispatching) {
      throw new Error('Reducers may not dispatch actions.');
    }

    try {
      isDispatching = true;
      currentState = currentReducer(currentState, action);
    } finally {
      isDispatching = false;
    }

    var listeners = currentListeners = nextListeners;
    for (var i = 0; i < listeners.length; i++) {
      listeners[i]();
    }

    return action;
  }

  /**
   * Replaces the reducer currently used by the store to calculate the state.
   *
   * You might need this if your app implements code splitting and you want to
   * load some of the reducers dynamically. You might also need this if you
   * implement a hot reloading mechanism for Redux.
   *
   * @param {Function} nextReducer The reducer for the store to use instead.
   * @returns {void}
   */
  function replaceReducer(nextReducer) {
    if (typeof nextReducer !== 'function') {
      throw new Error('Expected the nextReducer to be a function.');
    }

    currentReducer = nextReducer;
    dispatch({ type: ActionTypes.INIT });
  }

  /**
   * Interoperability point for observable/reactive libraries.
   * @returns {observable} A minimal observable of state changes.
   * For more information, see the observable proposal:
   * https://github.com/zenparsing/es-observable
   */
  function observable() {
    var _ref;

    var outerSubscribe = subscribe;
    return _ref = {
      /**
       * The minimal observable subscription method.
       * @param {Object} observer Any object that can be used as an observer.
       * The observer object should have a `next` method.
       * @returns {subscription} An object with an `unsubscribe` method that can
       * be used to unsubscribe the observable from the store, and prevent further
       * emission of values from the observable.
       */
      subscribe: function subscribe(observer) {
        if (typeof observer !== 'object') {
          throw new TypeError('Expected the observer to be an object.');
        }

        function observeState() {
          if (observer.next) {
            observer.next(getState());
          }
        }

        observeState();
        var unsubscribe = outerSubscribe(observeState);
        return { unsubscribe: unsubscribe };
      }
    }, _ref[_symbolObservable2['default']] = function () {
      return this;
    }, _ref;
  }

  // When a store is created, an "INIT" action is dispatched so that every
  // reducer returns their initial state. This effectively populates
  // the initial state tree.
  dispatch({ type: ActionTypes.INIT });

  return _ref2 = {
    dispatch: dispatch,
    subscribe: subscribe,
    getState: getState,
    replaceReducer: replaceReducer
  }, _ref2[_symbolObservable2['default']] = observable, _ref2;
}
},{"lodash/isPlainObject":31,"symbol-observable":88}],85:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports.compose = exports.applyMiddleware = exports.bindActionCreators = exports.combineReducers = exports.createStore = undefined;

var _createStore = require('./createStore');

var _createStore2 = _interopRequireDefault(_createStore);

var _combineReducers = require('./combineReducers');

var _combineReducers2 = _interopRequireDefault(_combineReducers);

var _bindActionCreators = require('./bindActionCreators');

var _bindActionCreators2 = _interopRequireDefault(_bindActionCreators);

var _applyMiddleware = require('./applyMiddleware');

var _applyMiddleware2 = _interopRequireDefault(_applyMiddleware);

var _compose = require('./compose');

var _compose2 = _interopRequireDefault(_compose);

var _warning = require('./utils/warning');

var _warning2 = _interopRequireDefault(_warning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

/*
* This is a dummy function to check if the function name has been altered by minification.
* If the function has been minified and NODE_ENV !== 'production', warn the user.
*/
function isCrushed() {}

if (process.env.NODE_ENV !== 'production' && typeof isCrushed.name === 'string' && isCrushed.name !== 'isCrushed') {
  (0, _warning2['default'])('You are currently using minified code outside of NODE_ENV === \'production\'. ' + 'This means that you are running a slower development build of Redux. ' + 'You can use loose-envify (https://github.com/zertosh/loose-envify) for browserify ' + 'or DefinePlugin for webpack (http://stackoverflow.com/questions/30030031) ' + 'to ensure you have the correct code for your production build.');
}

exports.createStore = _createStore2['default'];
exports.combineReducers = _combineReducers2['default'];
exports.bindActionCreators = _bindActionCreators2['default'];
exports.applyMiddleware = _applyMiddleware2['default'];
exports.compose = _compose2['default'];
}).call(this,require('_process'))

},{"./applyMiddleware":80,"./bindActionCreators":81,"./combineReducers":82,"./compose":83,"./createStore":84,"./utils/warning":86,"_process":1}],86:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports['default'] = warning;
/**
 * Prints a warning in the console if it exists.
 *
 * @param {String} message The warning message.
 * @returns {void}
 */
function warning(message) {
  /* eslint-disable no-console */
  if (typeof console !== 'undefined' && typeof console.error === 'function') {
    console.error(message);
  }
  /* eslint-enable no-console */
  try {
    // This error was thrown as a convenience so that if you enable
    // "break on all exceptions" in your console,
    // it would pause the execution at this line.
    throw new Error(message);
    /* eslint-disable no-empty */
  } catch (e) {}
  /* eslint-enable no-empty */
}
},{}],87:[function(require,module,exports){
'use strict';
module.exports = function (str) {
	return encodeURIComponent(str).replace(/[!'()*]/g, function (c) {
		return '%' + c.charCodeAt(0).toString(16).toUpperCase();
	});
};

},{}],88:[function(require,module,exports){
module.exports = require('./lib/index');

},{"./lib/index":89}],89:[function(require,module,exports){
(function (global){
'use strict';

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _ponyfill = require('./ponyfill');

var _ponyfill2 = _interopRequireDefault(_ponyfill);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var root = undefined; /* global window */

if (typeof global !== 'undefined') {
	root = global;
} else if (typeof window !== 'undefined') {
	root = window;
}

var result = (0, _ponyfill2['default'])(root);
exports['default'] = result;
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"./ponyfill":90}],90:[function(require,module,exports){
'use strict';

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports['default'] = symbolObservablePonyfill;
function symbolObservablePonyfill(root) {
	var result;
	var _Symbol = root.Symbol;

	if (typeof _Symbol === 'function') {
		if (_Symbol.observable) {
			result = _Symbol.observable;
		} else {
			result = _Symbol('observable');
			_Symbol.observable = result;
		}
	} else {
		result = '@@observable';
	}

	return result;
};
},{}],91:[function(require,module,exports){

exports = module.exports = trim;

function trim(str){
  return str.replace(/^\s*|\s*$/g, '');
}

exports.left = function(str){
  return str.replace(/^\s*/, '');
};

exports.right = function(str){
  return str.replace(/\s*$/, '');
};

},{}],92:[function(require,module,exports){
(function (process){
/**
 * Copyright 2014-2015, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

'use strict';

/**
 * Similar to invariant but only logs a warning if the condition is not met.
 * This can be used to log issues in development environments in critical
 * paths. Removing the logging code for production environments will keep the
 * same logic and follow the same code paths.
 */

var warning = function() {};

if (process.env.NODE_ENV !== 'production') {
  warning = function(condition, format, args) {
    var len = arguments.length;
    args = new Array(len > 2 ? len - 2 : 0);
    for (var key = 2; key < len; key++) {
      args[key - 2] = arguments[key];
    }
    if (format === undefined) {
      throw new Error(
        '`warning(condition, format, ...args)` requires a warning ' +
        'message argument'
      );
    }

    if (format.length < 10 || (/^[s\W]*$/).test(format)) {
      throw new Error(
        'The warning format should be able to uniquely identify this ' +
        'warning. Please, use a more descriptive format than: ' + format
      );
    }

    if (!condition) {
      var argIndex = 0;
      var message = 'Warning: ' +
        format.replace(/%s/g, function() {
          return args[argIndex++];
        });
      if (typeof console !== 'undefined') {
        console.error(message);
      }
      try {
        // This error was thrown as a convenience so that you can use this stack
        // to find the callsite that caused this warning to fire.
        throw new Error(message);
      } catch(x) {}
    }
  };
}

module.exports = warning;

}).call(this,require('_process'))

},{"_process":1}],93:[function(require,module,exports){
var window              = require('global');
var MockXMLHttpRequest  = require('./lib/MockXMLHttpRequest');
var real                = window.XMLHttpRequest;
var mock                = MockXMLHttpRequest;

/**
 * Mock utility
 */
module.exports = {

	XMLHttpRequest: MockXMLHttpRequest,

	/**
	 * Replace the native XHR with the mocked XHR
	 * @returns {exports}
	 */
	setup: function() {
		window.XMLHttpRequest = mock;
		MockXMLHttpRequest.handlers = [];
		return this;
	},

	/**
	 * Replace the mocked XHR with the native XHR and remove any handlers
	 * @returns {exports}
	 */
	teardown: function() {
		MockXMLHttpRequest.handlers = [];
		window.XMLHttpRequest = real;
		return this;
	},

	/**
	 * Mock a request
	 * @param   {string}    [method]
	 * @param   {string}    [url]
	 * @param   {Function}  fn
	 * @returns {exports}
	 */
	mock: function(method, url, fn) {
		var handler;
		if (arguments.length === 3) {
			handler = function(req, res) {
				if (req.method() === method && req.url() === url) {
					return fn(req, res);
				}
				return false;
			};
		} else {
			handler = method;
		}

		MockXMLHttpRequest.addHandler(handler);

		return this;
	},

	/**
	 * Mock a GET request
	 * @param   {String}    url
	 * @param   {Function}  fn
	 * @returns {exports}
	 */
	get: function(url, fn) {
		return this.mock('GET', url, fn);
	},

	/**
	 * Mock a POST request
	 * @param   {String}    url
	 * @param   {Function}  fn
	 * @returns {exports}
	 */
	post: function(url, fn) {
		return this.mock('POST', url, fn);
	},

	/**
	 * Mock a PUT request
	 * @param   {String}    url
	 * @param   {Function}  fn
	 * @returns {exports}
	 */
	put: function(url, fn) {
		return this.mock('PUT', url, fn);
	},

	/**
	 * Mock a PATCH request
	 * @param   {String}    url
	 * @param   {Function}  fn
	 * @returns {exports}
	 */
	patch: function(url, fn) {
		return this.mock('PATCH', url, fn);
	},

	/**
	 * Mock a DELETE request
	 * @param   {String}    url
	 * @param   {Function}  fn
	 * @returns {exports}
	 */
	delete: function(url, fn) {
		return this.mock('DELETE', url, fn);
	}

};

},{"./lib/MockXMLHttpRequest":96,"global":7}],94:[function(require,module,exports){

/**
 * The mocked request data
 * @constructor
 */
function MockRequest(xhr) {
  this._method    = xhr.method;
  this._url       = xhr.url;
  this._headers   = {};
  this.headers(xhr._requestHeaders);
  this.body(xhr.data);
}

/**
 * Get/set the HTTP method
 * @returns {string}
 */
MockRequest.prototype.method = function() {
  return this._method;
};

/**
 * Get/set the HTTP URL
 * @returns {string}
 */
MockRequest.prototype.url = function() {
  return this._url;
};

/**
 * Get/set a HTTP header
 * @param   {string} name
 * @param   {string} [value]
 * @returns {string|undefined|MockRequest}
 */
MockRequest.prototype.header = function(name, value) {
  if (arguments.length === 2) {
    this._headers[name.toLowerCase()] = value;
    return this;
  } else {
    return this._headers[name.toLowerCase()] || null;
  }
};

/**
 * Get/set all of the HTTP headers
 * @param   {Object} [headers]
 * @returns {Object|MockRequest}
 */
MockRequest.prototype.headers = function(headers) {
  if (arguments.length) {
    for (var name in headers) {
      if (headers.hasOwnProperty(name)) {
        this.header(name, headers[name]);
      }
    }
    return this;
  } else {
    return this._headers;
  }
};

/**
 * Get/set the HTTP body
 * @param   {string} [body]
 * @returns {string|MockRequest}
 */
MockRequest.prototype.body = function(body) {
  if (arguments.length) {
    this._body = body;
    return this;
  } else {
    return this._body;
  }
};

module.exports = MockRequest;

},{}],95:[function(require,module,exports){

/**
 * The mocked response data
 * @constructor
 */
function MockResponse() {
  this._status      = 200;
  this._headers     = {};
  this._body        = '';
  this._timeout     = false;
}

/**
 * Get/set the HTTP status
 * @param   {number} [code]
 * @returns {number|MockResponse}
 */
MockResponse.prototype.status = function(code) {
  if (arguments.length) {
    this._status = code;
    return this;
  } else {
    return this._status;
  }
};

/**
 * Get/set a HTTP header
 * @param   {string} name
 * @param   {string} [value]
 * @returns {string|undefined|MockResponse}
 */
MockResponse.prototype.header = function(name, value) {
  if (arguments.length === 2) {
    this._headers[name.toLowerCase()] = value;
    return this;
  } else {
    return this._headers[name.toLowerCase()] || null;
  }
};

/**
 * Get/set all of the HTTP headers
 * @param   {Object} [headers]
 * @returns {Object|MockResponse}
 */
MockResponse.prototype.headers = function(headers) {
  if (arguments.length) {
    for (var name in headers) {
      if (headers.hasOwnProperty(name)) {
        this.header(name, headers[name]);
      }
    }
    return this;
  } else {
    return this._headers;
  }
};

/**
 * Get/set the HTTP body
 * @param   {string} [body]
 * @returns {string|MockResponse}
 */
MockResponse.prototype.body = function(body) {
  if (arguments.length) {
    this._body = body;
    return this;
  } else {
    return this._body;
  }
};

/**
 * Get/set the HTTP timeout
 * @param   {boolean|number} [timeout]
 * @returns {boolean|number|MockResponse}
 */
MockResponse.prototype.timeout = function(timeout) {
  if (arguments.length) {
    this._timeout = timeout;
    return this;
  } else {
    return this._timeout;
  }
};

module.exports = MockResponse;

},{}],96:[function(require,module,exports){
var MockRequest   = require('./MockRequest');
var MockResponse  = require('./MockResponse');

var notImplementedError = new Error('This feature hasn\'t been implmented yet. Please submit an Issue or Pull Request on Github.');

//https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest
//https://xhr.spec.whatwg.org/
//http://www.w3.org/TR/2006/WD-XMLHttpRequest-20060405/

MockXMLHttpRequest.STATE_UNSENT             = 0;
MockXMLHttpRequest.STATE_OPENED             = 1;
MockXMLHttpRequest.STATE_HEADERS_RECEIVED   = 2;
MockXMLHttpRequest.STATE_LOADING            = 3;
MockXMLHttpRequest.STATE_DONE               = 4;

/**
 * The request handlers
 * @private
 * @type {Array}
 */
MockXMLHttpRequest.handlers = [];

/**
 * Add a request handler
 * @param   {function(MockRequest, MockResponse)} fn
 * @returns {MockXMLHttpRequest}
 */
MockXMLHttpRequest.addHandler = function(fn) {
  MockXMLHttpRequest.handlers.push(fn);
  return this;
};

/**
 * Remove a request handler
 * @param   {function(MockRequest, MockResponse)} fn
 * @returns {MockXMLHttpRequest}
 */
MockXMLHttpRequest.removeHandler = function(fn) {
  throw notImplementedError;
};

/**
 * Handle a request
 * @param   {MockRequest} request
 * @returns {MockResponse|null}
 */
MockXMLHttpRequest.handle = function(request) {

  for (var i=0; i<MockXMLHttpRequest.handlers.length; ++i) {

    //get the generator to create a response to the request
    var response = MockXMLHttpRequest.handlers[i](request, new MockResponse());

    if (response) {
      return response;
    }

  }

  return null;
};

/**
 * Mock XMLHttpRequest
 * @constructor
 */
function MockXMLHttpRequest() {
  this.reset();
  this.timeout = 0;
}

/**
 * Reset the response values
 * @private
 */
MockXMLHttpRequest.prototype.reset = function() {

  this._requestHeaders  = {};
  this._responseHeaders = {};

  this.status       = 0;
  this.statusText   = '';

  this.response     = null;
  this.responseType = null;
  this.responseText = null;
  this.responseXML  = null;

  this.readyState   = MockXMLHttpRequest.STATE_UNSENT;
};

/**
 * Trigger an event
 * @param   {String} event
 * @returns {MockXMLHttpRequest}
 */
MockXMLHttpRequest.prototype.trigger = function(event) {

  if (this.onreadystatechange) {
    this.onreadystatechange();
  }

  if (this['on'+event]) {
    this['on'+event]();
  }

  //iterate over the listeners

  return this;
};

MockXMLHttpRequest.prototype.open = function(method, url, async, user, password) {
  this.reset();
  this.method   = method;
  this.url      = url;
  this.async    = async;
  this.user     = user;
  this.password = password;
  this.data     = null;
  this.readyState = MockXMLHttpRequest.STATE_OPENED;
};

MockXMLHttpRequest.prototype.setRequestHeader = function(name, value) {
  this._requestHeaders[name] = value;
};

MockXMLHttpRequest.prototype.overrideMimeType = function(mime) {
  throw notImplementedError;
};

MockXMLHttpRequest.prototype.send = function(data) {
  var self = this;
  this.data = data;

  self.readyState = MockXMLHttpRequest.STATE_LOADING;

  self._sendTimeout = setTimeout(function() {

    var response = MockXMLHttpRequest.handle(new MockRequest(self));

    if (response && response instanceof MockResponse) {

      var timeout = response.timeout();

      if (timeout) {

        //trigger a timeout event because the request timed out - wait for the timeout time because many libs like jquery and superagent use setTimeout to detect the error type
        self._sendTimeout = setTimeout(function() {
          self.readyState = MockXMLHttpRequest.STATE_DONE;
          self.trigger('timeout');
        }, typeof(timeout) === 'number' ? timeout : self.timeout+1);

      } else {

        //map the response to the XHR object
        self.status             = response.status();
        self._responseHeaders   = response.headers();
        self.responseType       = 'text';
        self.response           = response.body();
        self.responseText       = response.body(); //TODO: detect an object and return JSON, detect XML and return XML
        self.readyState         = MockXMLHttpRequest.STATE_DONE;

        //trigger a load event because the request was received
        self.trigger('load');

      }

    } else {

      //trigger an error because the request was not handled
      self.readyState = MockXMLHttpRequest.STATE_DONE;
      self.trigger('error');

    }

  }, 0);

};

MockXMLHttpRequest.prototype.abort = function() {
  clearTimeout(this._sendTimeout);

  if (this.readyState > MockXMLHttpRequest.STATE_UNSENT && this.readyState < MockXMLHttpRequest.STATE_DONE) {
    this.readyState = MockXMLHttpRequest.STATE_UNSENT;
    this.trigger('abort');
  }

};

MockXMLHttpRequest.prototype.getAllResponseHeaders = function() {

  if (this.readyState < MockXMLHttpRequest.STATE_HEADERS_RECEIVED) {
    return null;
  }

  var headers = '';
  for (var name in this._responseHeaders) {
    if (this._responseHeaders.hasOwnProperty(name)) {
      headers += name+': '+this._responseHeaders[name]+'\r\n';
    }
  }

  return headers;
};

MockXMLHttpRequest.prototype.getResponseHeader = function(name) {

  if (this.readyState < MockXMLHttpRequest.STATE_HEADERS_RECEIVED) {
    return null;
  }

  return this._responseHeaders[name.toLowerCase()] || null;
};

MockXMLHttpRequest.prototype.addEventListener = function(event, listener) {
  throw notImplementedError;
};

MockXMLHttpRequest.prototype.removeEventListener = function(event, listener) {
  throw notImplementedError;
};

module.exports = MockXMLHttpRequest;

},{"./MockRequest":94,"./MockResponse":95}],97:[function(require,module,exports){
"use strict";
var window = require("global/window")
var isFunction = require("is-function")
var parseHeaders = require("parse-headers")
var xtend = require("xtend")

module.exports = createXHR
createXHR.XMLHttpRequest = window.XMLHttpRequest || noop
createXHR.XDomainRequest = "withCredentials" in (new createXHR.XMLHttpRequest()) ? createXHR.XMLHttpRequest : window.XDomainRequest

forEachArray(["get", "put", "post", "patch", "head", "delete"], function(method) {
    createXHR[method === "delete" ? "del" : method] = function(uri, options, callback) {
        options = initParams(uri, options, callback)
        options.method = method.toUpperCase()
        return _createXHR(options)
    }
})

function forEachArray(array, iterator) {
    for (var i = 0; i < array.length; i++) {
        iterator(array[i])
    }
}

function isEmpty(obj){
    for(var i in obj){
        if(obj.hasOwnProperty(i)) return false
    }
    return true
}

function initParams(uri, options, callback) {
    var params = uri

    if (isFunction(options)) {
        callback = options
        if (typeof uri === "string") {
            params = {uri:uri}
        }
    } else {
        params = xtend(options, {uri: uri})
    }

    params.callback = callback
    return params
}

function createXHR(uri, options, callback) {
    options = initParams(uri, options, callback)
    return _createXHR(options)
}

function _createXHR(options) {
    if(typeof options.callback === "undefined"){
        throw new Error("callback argument missing")
    }

    var called = false
    var callback = function cbOnce(err, response, body){
        if(!called){
            called = true
            options.callback(err, response, body)
        }
    }

    function readystatechange() {
        if (xhr.readyState === 4) {
            loadFunc()
        }
    }

    function getBody() {
        // Chrome with requestType=blob throws errors arround when even testing access to responseText
        var body = undefined

        if (xhr.response) {
            body = xhr.response
        } else {
            body = xhr.responseText || getXml(xhr)
        }

        if (isJson) {
            try {
                body = JSON.parse(body)
            } catch (e) {}
        }

        return body
    }

    var failureResponse = {
                body: undefined,
                headers: {},
                statusCode: 0,
                method: method,
                url: uri,
                rawRequest: xhr
            }

    function errorFunc(evt) {
        clearTimeout(timeoutTimer)
        if(!(evt instanceof Error)){
            evt = new Error("" + (evt || "Unknown XMLHttpRequest Error") )
        }
        evt.statusCode = 0
        return callback(evt, failureResponse)
    }

    // will load the data & process the response in a special response object
    function loadFunc() {
        if (aborted) return
        var status
        clearTimeout(timeoutTimer)
        if(options.useXDR && xhr.status===undefined) {
            //IE8 CORS GET successful response doesn't have a status field, but body is fine
            status = 200
        } else {
            status = (xhr.status === 1223 ? 204 : xhr.status)
        }
        var response = failureResponse
        var err = null

        if (status !== 0){
            response = {
                body: getBody(),
                statusCode: status,
                method: method,
                headers: {},
                url: uri,
                rawRequest: xhr
            }
            if(xhr.getAllResponseHeaders){ //remember xhr can in fact be XDR for CORS in IE
                response.headers = parseHeaders(xhr.getAllResponseHeaders())
            }
        } else {
            err = new Error("Internal XMLHttpRequest Error")
        }
        return callback(err, response, response.body)
    }

    var xhr = options.xhr || null

    if (!xhr) {
        if (options.cors || options.useXDR) {
            xhr = new createXHR.XDomainRequest()
        }else{
            xhr = new createXHR.XMLHttpRequest()
        }
    }

    var key
    var aborted
    var uri = xhr.url = options.uri || options.url
    var method = xhr.method = options.method || "GET"
    var body = options.body || options.data || null
    var headers = xhr.headers = options.headers || {}
    var sync = !!options.sync
    var isJson = false
    var timeoutTimer

    if ("json" in options) {
        isJson = true
        headers["accept"] || headers["Accept"] || (headers["Accept"] = "application/json") //Don't override existing accept header declared by user
        if (method !== "GET" && method !== "HEAD") {
            headers["content-type"] || headers["Content-Type"] || (headers["Content-Type"] = "application/json") //Don't override existing accept header declared by user
            body = JSON.stringify(options.json)
        }
    }

    xhr.onreadystatechange = readystatechange
    xhr.onload = loadFunc
    xhr.onerror = errorFunc
    // IE9 must have onprogress be set to a unique function.
    xhr.onprogress = function () {
        // IE must die
    }
    xhr.ontimeout = errorFunc
    xhr.open(method, uri, !sync, options.username, options.password)
    //has to be after open
    if(!sync) {
        xhr.withCredentials = !!options.withCredentials
    }
    // Cannot set timeout with sync request
    // not setting timeout on the xhr object, because of old webkits etc. not handling that correctly
    // both npm's request and jquery 1.x use this kind of timeout, so this is being consistent
    if (!sync && options.timeout > 0 ) {
        timeoutTimer = setTimeout(function(){
            aborted=true//IE9 may still call readystatechange
            xhr.abort("timeout")
            var e = new Error("XMLHttpRequest timeout")
            e.code = "ETIMEDOUT"
            errorFunc(e)
        }, options.timeout )
    }

    if (xhr.setRequestHeader) {
        for(key in headers){
            if(headers.hasOwnProperty(key)){
                xhr.setRequestHeader(key, headers[key])
            }
        }
    } else if (options.headers && !isEmpty(options.headers)) {
        throw new Error("Headers cannot be set on an XDomainRequest object")
    }

    if ("responseType" in options) {
        xhr.responseType = options.responseType
    }

    if ("beforeSend" in options &&
        typeof options.beforeSend === "function"
    ) {
        options.beforeSend(xhr)
    }

    xhr.send(body)

    return xhr


}

function getXml(xhr) {
    if (xhr.responseType === "document") {
        return xhr.responseXML
    }
    var firefoxBugTakenEffect = xhr.status === 204 && xhr.responseXML && xhr.responseXML.documentElement.nodeName === "parsererror"
    if (xhr.responseType === "" && !firefoxBugTakenEffect) {
        return xhr.responseXML
    }

    return null
}

function noop() {}

},{"global/window":7,"is-function":27,"parse-headers":32,"xtend":98}],98:[function(require,module,exports){
module.exports = extend

var hasOwnProperty = Object.prototype.hasOwnProperty;

function extend() {
    var target = {}

    for (var i = 0; i < arguments.length; i++) {
        var source = arguments[i]

        for (var key in source) {
            if (hasOwnProperty.call(source, key)) {
                target[key] = source[key]
            }
        }
    }

    return target
}

},{}],99:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = actionsMaker;

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _mappingToJsonLdRml = require("./util/mappingToJsonLdRml");

var _mappingToJsonLdRml2 = _interopRequireDefault(_mappingToJsonLdRml);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/*eslint no-shadow: [2, { "allow": ["dispatch"] }]*/
function actionsMaker(navigateTo, dispatch) {
  //bind to variable so an action can trigger other actions
  var actions = {
    onToken: function onToken(token) {
      (0, _xhr2.default)("" + "/v2.1/system/users/me/vres", {
        headers: {
          "Authorization": token
        }
      }, function (err, resp, body) {
        var vreData = JSON.parse(body);
        dispatch({ type: "LOGIN", data: token, vreData: vreData });
        if (vreData.mine) {
          navigateTo("collectionsOverview");
        }
      });
    },
    onLoadMoreClick: function onLoadMoreClick(url, collection) {
      dispatch(function (dispatch, getState) {
        var state = getState();
        var payload = {
          headers: {
            "Authorization": state.userdata.userId
          }
        };
        _xhr2.default.get(url, payload, function (err, resp, body) {
          if (err) {
            dispatch({ type: "COLLECTION_ITEMS_LOADING_ERROR", collection: collection, error: err });
          } else {
            try {
              dispatch({ type: "COLLECTION_ITEMS_LOADING_SUCCEEDED", collection: collection, data: JSON.parse(body) });
            } catch (e) {
              dispatch({ type: "COLLECTION_ITEMS_LOADING_ERROR", collection: collection, error: e });
            }
          }
          dispatch({ type: "COLLECTION_ITEMS_LOADING_FINISHED", collection: collection });
        });
      });
    },
    onUploadFileSelect: function onUploadFileSelect(files) {
      var isReupload = arguments.length <= 1 || arguments[1] === undefined ? false : arguments[1];

      var file = files[0];
      var formData = new FormData();
      formData.append("file", file);
      dispatch({ type: "START_UPLOAD" });
      dispatch(function (dispatch, getState) {
        var state = getState();
        var payload = {
          body: formData,
          headers: {
            "Authorization": state.userdata.userId
          }
        };
        _xhr2.default.post("" + "/v2.1/bulk-upload", payload, function (err, resp) {
          var location = resp.headers.location;
          _xhr2.default.get(location, { headers: { "Authorization": state.userdata.userId } }, function (err, resp, body) {
            dispatch({ type: "FINISH_UPLOAD", data: JSON.parse(body), uploadedFileName: file.name });
            if (isReupload) {
              actions.onSelectCollection(state.importData.activeCollection);
            } else {
              navigateTo("mapArchetypes");
            }
          });
        });
      });
    },
    onContinueMapping: function onContinueMapping(vreId) {
      dispatch(function (dispatch, getState) {
        var state = getState();
        _xhr2.default.get(state.userdata.myVres[vreId].rmlUri, { headers: { "Authorization": state.userdata.userId } }, function (err, resp, body) {
          dispatch({ type: "FINISH_UPLOAD", data: JSON.parse(body) });
          navigateTo("mapArchetypes");
        });
      });
    },
    /*		onSaveMappings: function () {
          dispatch({type: "SAVE_STARTED"})
          dispatch(function (dispatch, getState) {
            var state = getState();
            var payload = {
              json: mappingToJsonLdRml(state.mappings, state.importData.vre),
              headers: {
                "Authorization": state.userdata.userId
              }
            };
    
            xhr.post(state.importData.saveMappingUrl, payload, function (err, resp) {
              if (err) {
                dispatch({type: "SAVE_HAD_ERROR"})
              } else {
                dispatch({type: "SAVE_SUCCEEDED"})
              }
              dispatch({type: "SAVE_FINISHED"})
            });
          });
        },*/

    onPublishData: function onPublishData() {
      dispatch({ type: "SAVE_STARTED" });
      dispatch({ type: "SAVE_FINISHED" });
      dispatch({ type: "PUBLISH_STARTED" });
      dispatch(function (dispatch, getState) {
        var state = getState();
        var jsonLd = (0, _mappingToJsonLdRml2.default)(state.mappings, state.importData.vre);
        console.log(jsonLd);
        var payload = {
          body: JSON.stringify(jsonLd),
          headers: {
            "Authorization": state.userdata.userId,
            "Content-type": "application/ld+json"
          }
        };

        _xhr2.default.post(state.importData.executeMappingUrl, payload, function (err, resp) {
          if (err) {
            dispatch({ type: "PUBLISH_HAD_ERROR" });
          } else {
            if (JSON.parse(resp.body).success) {
              dispatch({ type: "PUBLISH_SUCCEEDED" });
              actions.onToken(state.userdata.userId);
            } else {
              dispatch({ type: "PUBLISH_HAD_ERROR" });
              actions.onSelectCollection(state.importData.activeCollection);
            }
          }
          dispatch({ type: "PUBLISH_FINISHED" });
        });
      });
    },

    onSelectCollection: function onSelectCollection(collection) {
      dispatch({ type: "SET_ACTIVE_COLLECTION", collection: collection });
      dispatch(function (dispatch, getState) {
        var state = getState();
        var currentSheet = state.importData.sheets.find(function (x) {
          return x.collection === collection;
        });
        if (currentSheet.rows.length === 0 && currentSheet.nextUrl && !currentSheet.isLoading) {
          var payload = {
            headers: {
              "Authorization": state.userdata.userId
            }
          };
          dispatch({ type: "COLLECTION_ITEMS_LOADING" });
          _xhr2.default.get(currentSheet.nextUrl, payload, function (err, resp, body) {
            if (err) {
              dispatch({ type: "COLLECTION_ITEMS_LOADING_ERROR", collection: collection, error: err });
            } else {
              try {
                dispatch({ type: "COLLECTION_ITEMS_LOADING_SUCCEEDED", collection: collection, data: JSON.parse(body) });
              } catch (e) {
                dispatch({ type: "COLLECTION_ITEMS_LOADING_ERROR", collection: collection, error: e });
              }
            }
            dispatch({ type: "COLLECTION_ITEMS_LOADING_FINISHED", collection: collection });
          });
        }
      });
    },

    onMapCollectionArchetype: function onMapCollectionArchetype(collection, value) {
      return dispatch({ type: "MAP_COLLECTION_ARCHETYPE", collection: collection, value: value });
    },

    onConfirmCollectionArchetypeMappings: function onConfirmCollectionArchetypeMappings() {
      dispatch({ type: "CONFIRM_COLLECTION_ARCHETYPE_MAPPINGS" });
      dispatch(function (dispatch, getState) {
        var state = getState();
        actions.onSelectCollection(state.importData.activeCollection);
      });
      navigateTo("mapData");
    },

    onSetFieldMapping: function onSetFieldMapping(collection, propertyField, importedField) {
      return dispatch({ type: "SET_FIELD_MAPPING", collection: collection, propertyField: propertyField, importedField: importedField });
    },

    onClearFieldMapping: function onClearFieldMapping(collection, propertyField, clearIndex) {
      return dispatch({ type: "CLEAR_FIELD_MAPPING", collection: collection, propertyField: propertyField, clearIndex: clearIndex });
    },

    onSetDefaultValue: function onSetDefaultValue(collection, propertyField, value) {
      return dispatch({ type: "SET_DEFAULT_VALUE", collection: collection, propertyField: propertyField, value: value });
    },

    onConfirmFieldMappings: function onConfirmFieldMappings(collection, propertyField) {
      return dispatch({ type: "CONFIRM_FIELD_MAPPINGS", collection: collection, propertyField: propertyField });
    },

    onUnconfirmFieldMappings: function onUnconfirmFieldMappings(collection, propertyField) {
      return dispatch({ type: "UNCONFIRM_FIELD_MAPPINGS", collection: collection, propertyField: propertyField });
    },

    onSetValueMapping: function onSetValueMapping(collection, propertyField, timValue, mapValue) {
      return dispatch({ type: "SET_VALUE_MAPPING", collection: collection, propertyField: propertyField, timValue: timValue, mapValue: mapValue });
    },

    onIgnoreColumnToggle: function onIgnoreColumnToggle(collection, variableName) {
      return dispatch({ type: "TOGGLE_IGNORED_COLUMN", collection: collection, variableName: variableName });
    },

    onAddCustomProperty: function onAddCustomProperty(collection, propertyName, propertyType) {
      return dispatch({ type: "ADD_CUSTOM_PROPERTY", collection: collection, propertyField: propertyName, propertyType: propertyType });
    },

    onRemoveCustomProperty: function onRemoveCustomProperty(collection, propertyName) {
      return dispatch({ type: "REMOVE_CUSTOM_PROPERTY", collection: collection, propertyField: propertyName });
    },

    onCloseMessage: function onCloseMessage(messageId) {
      return dispatch({ type: "TOGGLE_MESSAGE", messageId: messageId });
    }
  };
  return actions;
}

},{"./util/mappingToJsonLdRml":139,"xhr":97}],100:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _propertyForm = require("./property-form/property-form");

var _propertyForm2 = _interopRequireDefault(_propertyForm);

var _addProperty = require("./property-form/add-property");

var _addProperty2 = _interopRequireDefault(_addProperty);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var CollectionForm = function (_React$Component) {
  _inherits(CollectionForm, _React$Component);

  function CollectionForm() {
    _classCallCheck(this, CollectionForm);

    return _possibleConstructorReturn(this, (CollectionForm.__proto__ || Object.getPrototypeOf(CollectionForm)).apply(this, arguments));
  }

  _createClass(CollectionForm, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var columns = _props.columns;
      var archetypeFields = _props.archetypeFields;
      var propertyMappings = _props.propertyMappings;
      var customPropertyMappings = _props.customPropertyMappings;
      var collectionName = _props.collectionName;
      var availableArchetypes = _props.availableArchetypes;
      var availableCollectionColumnsPerArchetype = _props.availableCollectionColumnsPerArchetype;
      var targetableVres = _props.targetableVres;
      var _props2 = this.props;
      var _onRemoveCustomProperty = _props2.onRemoveCustomProperty;
      var _onSetFieldMapping = _props2.onSetFieldMapping;
      var _onConfirmFieldMappings = _props2.onConfirmFieldMappings;
      var _onClearFieldMapping = _props2.onClearFieldMapping;
      var _onUnconfirmFieldMappings = _props2.onUnconfirmFieldMappings;
      var _onAddCustomProperty = _props2.onAddCustomProperty;


      var archeTypePropFields = archetypeFields.filter(function (af) {
        return af.type !== "relation";
      });

      var propertyForms = archeTypePropFields.map(function (af, i) {
        return _react2.default.createElement(_propertyForm2.default, { columns: columns, custom: false, key: i, name: af.name, type: af.type,
          propertyMapping: propertyMappings.find(function (m) {
            return m.property === af.name;
          }),
          onSetFieldMapping: function onSetFieldMapping(field, value) {
            return _onSetFieldMapping(collectionName, field, value);
          },
          onConfirmFieldMappings: function onConfirmFieldMappings(field) {
            return _onConfirmFieldMappings(collectionName, field);
          },
          onUnconfirmFieldMappings: function onUnconfirmFieldMappings(field) {
            return _onUnconfirmFieldMappings(collectionName, field);
          },
          onClearFieldMapping: function onClearFieldMapping(field, valueIdx) {
            return _onClearFieldMapping(collectionName, field, valueIdx);
          }
        });
      });

      var customPropertyForms = customPropertyMappings.map(function (customProp, i) {
        return _react2.default.createElement(_propertyForm2.default, { columns: columns, custom: true, key: i, name: customProp.name, type: customProp.type,
          archetypeFields: archetypeFields,
          availableCollectionColumnsPerArchetype: availableCollectionColumnsPerArchetype,
          propertyMapping: propertyMappings.find(function (m) {
            return m.property === customProp.name;
          }),
          onRemoveCustomProperty: function onRemoveCustomProperty(field) {
            return _onRemoveCustomProperty(collectionName, field);
          },
          onSetFieldMapping: function onSetFieldMapping(field, value) {
            return _onSetFieldMapping(collectionName, field, value);
          },
          onClearFieldMapping: function onClearFieldMapping(field, valueIdx) {
            return _onClearFieldMapping(collectionName, field, valueIdx);
          },
          onConfirmFieldMappings: function onConfirmFieldMappings(field) {
            return _onConfirmFieldMappings(collectionName, field);
          },
          onUnconfirmFieldMappings: function onUnconfirmFieldMappings(field) {
            return _onUnconfirmFieldMappings(collectionName, field);
          },
          targetableVres: targetableVres
        });
      });

      return _react2.default.createElement(
        "div",
        { className: "container basic-margin" },
        propertyForms,
        customPropertyForms,
        _react2.default.createElement(_addProperty2.default, {
          archetypeFields: archetypeFields,
          availableArchetypes: availableArchetypes,
          onAddCustomProperty: function onAddCustomProperty(name, type) {
            return _onAddCustomProperty(collectionName, name, type);
          } })
      );
    }
  }]);

  return CollectionForm;
}(_react2.default.Component);

exports.default = CollectionForm;

},{"./property-form/add-property":113,"./property-form/property-form":115,"react":"react"}],101:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var CollectionIndex = function (_React$Component) {
  _inherits(CollectionIndex, _React$Component);

  function CollectionIndex() {
    _classCallCheck(this, CollectionIndex);

    return _possibleConstructorReturn(this, (CollectionIndex.__proto__ || Object.getPrototypeOf(CollectionIndex)).apply(this, arguments));
  }

  _createClass(CollectionIndex, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var collectionTabs = _props.collectionTabs;
      var onSelectCollection = _props.onSelectCollection;


      return _react2.default.createElement(
        "div",
        { className: "container basic-margin" },
        _react2.default.createElement(
          "ul",
          { className: "nav nav-tabs", role: "tablist" },
          collectionTabs.map(function (collectionTab) {
            return _react2.default.createElement(
              "li",
              { key: collectionTab.collectionName, className: (0, _classnames2.default)({ active: collectionTab.active }) },
              _react2.default.createElement(
                "a",
                { onClick: function onClick() {
                    return onSelectCollection(collectionTab.collectionName);
                  },
                  style: { cursor: collectionTab.active ? "default" : "pointer" } },
                collectionTab.archetypeName,
                " ",
                collectionTab.complete ? _react2.default.createElement("span", { className: "glyphicon glyphicon-ok" }) : null,
                _react2.default.createElement(
                  "span",
                  { className: "excel-tab" },
                  _react2.default.createElement("img", { src: "images/icon-excel.svg", className: "excel-icon", alt: "" }),
                  " ",
                  collectionTab.collectionName
                )
              )
            );
          })
        )
      );
    }
  }]);

  return CollectionIndex;
}(_react2.default.Component);

exports.default = CollectionIndex;

},{"classnames":2,"react":"react"}],102:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _uploadButton = require("./upload-button");

var _uploadButton2 = _interopRequireDefault(_uploadButton);

var _datasetCards = require("./dataset-cards");

var _datasetCards2 = _interopRequireDefault(_datasetCards);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function CollectionOverview(props) {
  var onUploadFileSelect = props.onUploadFileSelect;
  var userId = props.userId;
  var isUploading = props.isUploading;
  var vres = props.vres;


  var uploadButton = _react2.default.createElement(_uploadButton2.default, {
    classNames: ["btn", "btn-lg", "btn-primary", "pull-right"],
    glyphicon: "glyphicon glyphicon-cloud-upload",
    isUploading: isUploading,
    label: "Upload new dataset",
    onUploadFileSelect: onUploadFileSelect });

  return _react2.default.createElement(
    "div",
    null,
    _react2.default.createElement(
      "div",
      { className: "container" },
      _react2.default.createElement(
        _datasetCards2.default,
        { userId: userId, caption: "My datasets", vres: vres },
        uploadButton
      )
    )
  );
}

exports.default = CollectionOverview;

},{"./dataset-cards":106,"./upload-button":126,"react":"react"}],103:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _headerCell = require("./table/header-cell");

var _headerCell2 = _interopRequireDefault(_headerCell);

var _dataRow = require("./table/data-row");

var _dataRow2 = _interopRequireDefault(_dataRow);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var CollectionTable = function (_React$Component) {
  _inherits(CollectionTable, _React$Component);

  function CollectionTable() {
    _classCallCheck(this, CollectionTable);

    return _possibleConstructorReturn(this, (CollectionTable.__proto__ || Object.getPrototypeOf(CollectionTable)).apply(this, arguments));
  }

  _createClass(CollectionTable, [{
    key: "render",
    value: function render() {
      var _this2 = this;

      var onIgnoreColumnToggle = this.props.onIgnoreColumnToggle;
      var _props = this.props;
      var rows = _props.rows;
      var headers = _props.headers;
      var nextUrl = _props.nextUrl;


      return _react2.default.createElement(
        "div",
        { className: "table-responsive" },
        _react2.default.createElement(
          "table",
          { className: "table table-bordered table-obtrusive" },
          _react2.default.createElement(
            "thead",
            null,
            _react2.default.createElement(
              "tr",
              null,
              headers.map(function (header) {
                return _react2.default.createElement(_headerCell2.default, { key: header.name, header: header.name, onIgnoreColumnToggle: onIgnoreColumnToggle,
                  isIgnored: header.isIgnored, isConfirmed: header.isConfirmed });
              })
            )
          ),
          _react2.default.createElement(
            "tbody",
            null,
            rows.map(function (row, i) {
              return _react2.default.createElement(_dataRow2.default, { key: i, row: row });
            })
          )
        ),
        _react2.default.createElement(
          "button",
          { onClick: function onClick() {
              return _this2.props.onLoadMoreClick && _this2.props.onLoadMoreClick(nextUrl);
            },
            disabled: !nextUrl,
            className: "btn btn-default pull-right" },
          "more..."
        )
      );
    }
  }]);

  return CollectionTable;
}(_react2.default.Component);

exports.default = CollectionTable;

},{"./table/data-row":124,"./table/header-cell":125,"react":"react"}],104:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _collectionTable = require("./collection-table");

var _collectionTable2 = _interopRequireDefault(_collectionTable);

var _collectionForm = require("./collection-form");

var _collectionForm2 = _interopRequireDefault(_collectionForm);

var _collectionIndex = require("./collection-index");

var _collectionIndex2 = _interopRequireDefault(_collectionIndex);

var _message = require("./message");

var _message2 = _interopRequireDefault(_message);

var _uploadButton = require("./upload-button");

var _uploadButton2 = _interopRequireDefault(_uploadButton);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ConnectData = function (_React$Component) {
  _inherits(ConnectData, _React$Component);

  function ConnectData() {
    _classCallCheck(this, ConnectData);

    return _possibleConstructorReturn(this, (ConnectData.__proto__ || Object.getPrototypeOf(ConnectData)).apply(this, arguments));
  }

  _createClass(ConnectData, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var activeCollection = _props.activeCollection;
      var uploadedFileName = _props.uploadedFileName;
      var collectionTabs = _props.collectionTabs;
      var rows = _props.rows;
      var headers = _props.headers;
      var archetypeFields = _props.archetypeFields;
      var availableArchetypes = _props.availableArchetypes;
      var propertyMappings = _props.propertyMappings;
      var customPropertyMappings = _props.customPropertyMappings;
      var availableCollectionColumnsPerArchetype = _props.availableCollectionColumnsPerArchetype;
      var nextUrl = _props.nextUrl;
      var publishing = _props.publishing;
      var publishErrors = _props.publishErrors;
      var showCollectionsAreConnectedMessage = _props.showCollectionsAreConnectedMessage;
      var sheets = _props.sheets;
      var targetableVres = _props.targetableVres;
      var _props2 = this.props;
      var _onIgnoreColumnToggle = _props2.onIgnoreColumnToggle;
      var onSelectCollection = _props2.onSelectCollection;
      var onSetFieldMapping = _props2.onSetFieldMapping;
      var onRemoveCustomProperty = _props2.onRemoveCustomProperty;
      var onConfirmFieldMappings = _props2.onConfirmFieldMappings;
      var onUnconfirmFieldMappings = _props2.onUnconfirmFieldMappings;
      var onAddCustomProperty = _props2.onAddCustomProperty;
      var onClearFieldMapping = _props2.onClearFieldMapping;
      var _onLoadMoreClick = _props2.onLoadMoreClick;
      var onPublishData = _props2.onPublishData;
      var onUploadFileSelect = _props2.onUploadFileSelect;
      var _onCloseMessage = _props2.onCloseMessage;


      var allMappingsAreComplete = collectionTabs.filter(function (tab) {
        return tab.complete;
      }).length === collectionTabs.length;

      var publishFailedMessage = publishErrors ? _react2.default.createElement(
        _message2.default,
        { alertLevel: "danger", dismissible: false },
        _react2.default.createElement(_uploadButton2.default, { classNames: ["btn", "btn-danger", "pull-right", "btn-xs"], label: "Re-upload",
          onUploadFileSelect: onUploadFileSelect }),
        _react2.default.createElement("span", { className: "glyphicon glyphicon-exclamation-sign" }),
        " ",
        "Publish failed. Please fix the mappings or re-upload the data."
      ) : null;

      var collectionsAreConnectedMessage = sheets && showCollectionsAreConnectedMessage ? _react2.default.createElement(
        _message2.default,
        { alertLevel: "info", dismissible: true, onCloseMessage: function onCloseMessage() {
            return _onCloseMessage("showCollectionsAreConnectedMessage");
          } },
        sheets.map(function (sheet) {
          return _react2.default.createElement(
            "em",
            { key: sheet.collection },
            sheet.collection
          );
        }).reduce(function (accu, elem) {
          return accu === null ? [elem] : [].concat(_toConsumableArray(accu), [' and ', elem]);
        }, null),
        " from ",
        _react2.default.createElement(
          "em",
          null,
          uploadedFileName
        ),
        " are connected to the Timbuctoo Archetypes."
      ) : null;

      return _react2.default.createElement(
        "div",
        null,
        _react2.default.createElement(
          "div",
          { className: "container basic-margin" },
          _react2.default.createElement(
            "h2",
            { className: "small-margin" },
            "Upload and connect your dataset"
          ),
          collectionsAreConnectedMessage,
          publishFailedMessage,
          _react2.default.createElement(
            "p",
            null,
            "Connect the excel columns to the properties of the Archetypes"
          )
        ),
        _react2.default.createElement(_collectionIndex2.default, { collectionTabs: collectionTabs, onSelectCollection: onSelectCollection }),
        _react2.default.createElement(_collectionForm2.default, { columns: headers,
          collectionName: activeCollection,
          archetypeFields: archetypeFields,
          availableArchetypes: availableArchetypes,
          availableCollectionColumnsPerArchetype: availableCollectionColumnsPerArchetype,
          propertyMappings: propertyMappings,
          customPropertyMappings: customPropertyMappings,
          onSetFieldMapping: onSetFieldMapping,
          onClearFieldMapping: onClearFieldMapping,
          onRemoveCustomProperty: onRemoveCustomProperty,
          onConfirmFieldMappings: onConfirmFieldMappings,
          onUnconfirmFieldMappings: onUnconfirmFieldMappings,
          onAddCustomProperty: onAddCustomProperty,
          targetableVres: targetableVres }),
        _react2.default.createElement(
          "div",
          { className: "container big-margin" },
          _react2.default.createElement(
            "button",
            { onClick: onPublishData, className: "btn btn-warning btn-lg pull-right", type: "button", disabled: !allMappingsAreComplete || publishing },
            publishing ? "Publishing" : "Publish dataset"
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "container big-margin" },
          _react2.default.createElement(
            "p",
            { className: "from-excel" },
            _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
            " ",
            _react2.default.createElement(
              "em",
              null,
              activeCollection
            ),
            " from ",
            uploadedFileName
          ),
          _react2.default.createElement(_collectionTable2.default, {
            rows: rows,
            headers: headers,
            nextUrl: nextUrl,
            onIgnoreColumnToggle: function onIgnoreColumnToggle(header) {
              return _onIgnoreColumnToggle(activeCollection, header);
            },
            onLoadMoreClick: function onLoadMoreClick(url) {
              return _onLoadMoreClick(url, activeCollection);
            } })
        )
      );
    }
  }]);

  return ConnectData;
}(_react2.default.Component);

exports.default = ConnectData;

},{"./collection-form":100,"./collection-index":101,"./collection-table":103,"./message":111,"./upload-button":126,"react":"react"}],105:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _selectField = require("./fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

var _message = require("./message");

var _message2 = _interopRequireDefault(_message);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ConnectToArchetype = function (_React$Component) {
  _inherits(ConnectToArchetype, _React$Component);

  function ConnectToArchetype() {
    _classCallCheck(this, ConnectToArchetype);

    return _possibleConstructorReturn(this, (ConnectToArchetype.__proto__ || Object.getPrototypeOf(ConnectToArchetype)).apply(this, arguments));
  }

  _createClass(ConnectToArchetype, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var onMapCollectionArchetype = _props.onMapCollectionArchetype;
      var onConfirmCollectionArchetypeMappings = _props.onConfirmCollectionArchetypeMappings;
      var _onCloseMessage = _props.onCloseMessage;
      var sheets = _props.sheets;
      var archetype = _props.archetype;
      var mappings = _props.mappings;
      var showFileIsUploadedMessage = _props.showFileIsUploadedMessage;
      var uploadedFileName = _props.uploadedFileName;


      var collectionsAreMapped = Object.keys(mappings.collections).length > 0 && Object.keys(mappings.collections).map(function (key) {
        return mappings.collections[key].archetypeName;
      }).indexOf(null) < 0;

      var fileIsUploadedMessage = showFileIsUploadedMessage ? _react2.default.createElement(
        _message2.default,
        { alertLevel: "info", dismissible: true, onCloseMessage: function onCloseMessage() {
            return _onCloseMessage("showFileIsUploadedMessage");
          } },
        _react2.default.createElement(
          "em",
          null,
          uploadedFileName
        ),
        " is uploaded."
      ) : null;

      return _react2.default.createElement(
        "div",
        null,
        _react2.default.createElement(
          "div",
          { className: "container basic-margin" },
          _react2.default.createElement(
            "h2",
            { className: "small-margin" },
            "Upload and connect your dataset"
          ),
          fileIsUploadedMessage,
          _react2.default.createElement(
            "p",
            null,
            "We found ",
            sheets.length,
            " collections in the file. Connect the tabs to the Timbuctoo Archetypes."
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "container basic-margin" },
          sheets.map(function (sheet) {
            return _react2.default.createElement(
              "div",
              { className: "row", key: sheet.collection },
              _react2.default.createElement(
                "div",
                { className: "col-md-2" },
                _react2.default.createElement(
                  "span",
                  { className: "from-excel" },
                  _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
                  " ",
                  sheet.collection
                )
              ),
              _react2.default.createElement(
                "div",
                { className: "col-md-8" },
                _react2.default.createElement(
                  _selectField2.default,
                  {
                    onChange: function onChange(value) {
                      return onMapCollectionArchetype(sheet.collection, value);
                    },
                    onClear: function onClear() {
                      return onMapCollectionArchetype(sheet.collection, null);
                    },
                    value: mappings.collections[sheet.collection].archetypeName },
                  _react2.default.createElement(
                    "span",
                    { type: "placeholder" },
                    "Connect ",
                    _react2.default.createElement(
                      "em",
                      null,
                      sheet.collection
                    ),
                    " to a Timbuctoo archetype."
                  ),
                  Object.keys(archetype).filter(function (domain) {
                    return domain !== "relations";
                  }).sort().map(function (option) {
                    return _react2.default.createElement(
                      "span",
                      { key: option, value: option },
                      option
                    );
                  })
                )
              ),
              mappings.collections[sheet.collection].archetypeName ? _react2.default.createElement(
                "div",
                { className: "col-sm-1 hi-success", key: sheet.collection },
                _react2.default.createElement("span", { className: "glyphicon glyphicon-ok pull-right" })
              ) : null
            );
          })
        ),
        _react2.default.createElement(
          "div",
          { className: "container basic-margin" },
          _react2.default.createElement(
            "button",
            { onClick: onConfirmCollectionArchetypeMappings, type: "button", className: "btn btn-success", disabled: !collectionsAreMapped },
            "Connect"
          )
        )
      );
    }
  }]);

  return ConnectToArchetype;
}(_react2.default.Component);

exports.default = ConnectToArchetype;

},{"./fields/select-field":108,"./message":111,"react":"react"}],106:[function(require,module,exports){
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var vres = props.vres;
  var caption = props.caption;
  var userId = props.userId;


  return _react2.default.createElement(
    'div',
    { className: 'container' },
    _react2.default.createElement(
      'div',
      { className: 'basic-margin' },
      props.children,
      _react2.default.createElement(
        'h3',
        null,
        caption
      )
    ),
    _react2.default.createElement(
      'div',
      { className: 'big-margin' },
      Object.keys(vres).map(function (vre) {
        return _react2.default.createElement(_datasetCard2.default, { key: vre, userId: userId, vreId: vres[vre].name, caption: vres[vre].name.replace(/^[a-z0-9]+_/, "") });
      })
    )
  );
};

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _datasetCard = require('./datasetCard.jsx');

var _datasetCard2 = _interopRequireDefault(_datasetCard);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

;

},{"./datasetCard.jsx":107,"react":"react"}],107:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var TIMBUCTOO_SEARCH_URL = globals && globals.env ? globals.env.TIMBUCTOO_SEARCH_URL : "/";

function DataSetCard(props) {
  return _react2.default.createElement(
    "div",
    { className: "card-dataset" },
    _react2.default.createElement(
      "a",
      { className: "card-dataset btn btn-default explore",
        href: TIMBUCTOO_SEARCH_URL + "?vreId=" + props.vreId, target: "_blank" },
      "Explore",
      _react2.default.createElement("br", null),
      _react2.default.createElement(
        "strong",
        null,
        props.caption
      )
    ),
    props.userId ? _react2.default.createElement(
      "a",
      { className: "card-dataset btn btn-default",
        href: "" + "/static/edit-gui/?vreId=" + props.vreId + "&hsid=" + props.userId, target: "_blank" },
      _react2.default.createElement("span", { className: "glyphicon glyphicon-pencil" }),
      " ",
      "Edit"
    ) : null
  );
}

exports.default = DataSetCard;

},{"react":"react"}],108:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectField = function (_React$Component) {
  _inherits(SelectField, _React$Component);

  function SelectField(props) {
    _classCallCheck(this, SelectField);

    var _this = _possibleConstructorReturn(this, (SelectField.__proto__ || Object.getPrototypeOf(SelectField)).call(this, props));

    _this.state = {
      isOpen: false
    };
    _this.documentClickListener = _this.handleDocumentClick.bind(_this);
    return _this;
  }

  _createClass(SelectField, [{
    key: "componentDidMount",
    value: function componentDidMount() {
      document.addEventListener("click", this.documentClickListener, false);
    }
  }, {
    key: "componentWillUnmount",
    value: function componentWillUnmount() {
      document.removeEventListener("click", this.documentClickListener, false);
    }
  }, {
    key: "toggleSelect",
    value: function toggleSelect() {
      if (this.state.isOpen) {
        this.setState({ isOpen: false });
      } else {
        this.setState({ isOpen: true });
      }
    }
  }, {
    key: "handleDocumentClick",
    value: function handleDocumentClick(ev) {
      var isOpen = this.state.isOpen;

      if (isOpen && !_reactDom2.default.findDOMNode(this).contains(ev.target)) {
        this.setState({
          isOpen: false
        });
      }
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var _props = this.props;
      var onChange = _props.onChange;
      var onClear = _props.onClear;
      var value = _props.value;


      var selectedOption = _react2.default.Children.toArray(this.props.children).filter(function (opt) {
        return opt.props.value === value;
      });
      var placeholder = _react2.default.Children.toArray(this.props.children).filter(function (opt) {
        return opt.props.type === "placeholder";
      });
      var otherOptions = _react2.default.Children.toArray(this.props.children).filter(function (opt) {
        return opt.props.value && opt.props.value !== value;
      });

      return _react2.default.createElement(
        "div",
        { className: (0, _classnames2.default)("dropdown", { open: this.state.isOpen }) },
        _react2.default.createElement(
          "button",
          { className: "btn btn-blank dropdown-toggle", onClick: this.toggleSelect.bind(this) },
          selectedOption.length ? selectedOption : placeholder,
          " ",
          _react2.default.createElement("span", { className: "caret" })
        ),
        _react2.default.createElement(
          "ul",
          { className: "dropdown-menu" },
          value ? _react2.default.createElement(
            "li",
            null,
            _react2.default.createElement(
              "a",
              { onClick: function onClick() {
                  onClear();_this2.toggleSelect();
                } },
              "- clear -"
            )
          ) : null,
          otherOptions.map(function (option, i) {
            return _react2.default.createElement(
              "li",
              { key: i },
              _react2.default.createElement(
                "a",
                { style: { cursor: "pointer" }, onClick: function onClick() {
                    onChange(option.props.value);_this2.toggleSelect();
                  } },
                option
              )
            );
          })
        )
      );
    }
  }]);

  return SelectField;
}(_react2.default.Component);

SelectField.propTypes = {
  onChange: _react2.default.PropTypes.func,
  onClear: _react2.default.PropTypes.func,
  value: _react2.default.PropTypes.string
};

exports.default = SelectField;

},{"classnames":2,"react":"react","react-dom":"react-dom"}],109:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _uploadButton = require("./upload-button");

var _uploadButton2 = _interopRequireDefault(_uploadButton);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// (renames from UploadSplashScreen)
function FirstUpload(props) {
  var onUploadFileSelect = props.onUploadFileSelect;
  var userId = props.userId;
  var isUploading = props.isUploading;


  var sampleSheet = props.exampleSheetUrl ? _react2.default.createElement(
    "p",
    null,
    "Don't have a dataset handy? Here’s an ",
    _react2.default.createElement(
      "a",
      { href: props.exampleSheetUrl },
      "example excel sheet"
    ),
    "."
  ) : null;

  var uploadButton = _react2.default.createElement(_uploadButton2.default, {
    classNames: ["btn", "btn-lg", "btn-primary"],
    glyphicon: "glyphicon glyphicon-cloud-upload",
    isUploading: isUploading,
    label: "Browse",
    onUploadFileSelect: onUploadFileSelect });

  return _react2.default.createElement(
    "div",
    { className: "container" },
    _react2.default.createElement(
      "div",
      { className: "jumbotron first-upload upload-bg" },
      _react2.default.createElement(
        "h2",
        null,
        "Upload your first dataset"
      ),
      sampleSheet,
      userId ? uploadButton : _react2.default.createElement(
        "form",
        { action: "https://secure.huygens.knaw.nl/saml2/login", method: "POST" },
        _react2.default.createElement("input", { name: "hsurl", type: "hidden", value: window.location.href }),
        _react2.default.createElement(
          "p",
          null,
          "Most university accounts will work."
        ),
        _react2.default.createElement(
          "button",
          { className: "btn btn-primary btn-lg", type: "submit" },
          _react2.default.createElement("span", { className: "glyphicon glyphicon-log-in" }),
          " Log in to upload"
        )
      )
    )
  );
}

exports.default = FirstUpload;

},{"./upload-button":126,"react":"react"}],110:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function Footer(props) {
  var hiLogo = _react2.default.createElement(
    "div",
    { className: "col-sm-1 col-md-1" },
    _react2.default.createElement("img", { className: "hi-logo", src: "images/logo-huygens-ing.svg" })
  );

  var clariahLogo = _react2.default.createElement(
    "div",
    { className: "col-sm-1 col-md-1" },
    _react2.default.createElement("img", { className: "logo", src: "images/logo-clariah.svg" })
  );

  var footerBody = _react2.default.Children.count(props.children) > 0 ? _react2.default.Children.map(props.children, function (child, i) {
    return _react2.default.createElement(
      "div",
      { className: "white-bar" },
      _react2.default.createElement(
        "div",
        { className: "container" },
        i === _react2.default.Children.count(props.children) - 1 ? _react2.default.createElement(
          "div",
          { className: "row" },
          hiLogo,
          _react2.default.createElement(
            "div",
            { className: "col-sm-10 col-md-10 text-center" },
            child
          ),
          clariahLogo
        ) : _react2.default.createElement(
          "div",
          { className: "row" },
          _react2.default.createElement(
            "div",
            { className: "col-sm-12 col-md-12 text-center" },
            child
          )
        )
      )
    );
  }) : _react2.default.createElement(
    "div",
    { className: "white-bar" },
    _react2.default.createElement(
      "div",
      { className: "container" },
      _react2.default.createElement(
        "div",
        { className: "row" },
        hiLogo,
        _react2.default.createElement("div", { className: "col-sm-10 col-md-10 text-center" }),
        clariahLogo
      )
    )
  );

  return _react2.default.createElement(
    "footer",
    { className: "footer" },
    footerBody
  );
}

exports.default = Footer;

},{"react":"react"}],111:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var dismissible = props.dismissible;
  var alertLevel = props.alertLevel;
  var onCloseMessage = props.onCloseMessage;

  var dismissButton = dismissible ? _react2.default.createElement(
    "button",
    { type: "button", className: "close", onClick: onCloseMessage },
    _react2.default.createElement(
      "span",
      null,
      "×"
    )
  ) : null;

  return _react2.default.createElement(
    "div",
    { className: (0, _classnames2.default)("alert", "alert-" + alertLevel, { "alert-dismissible": dismissible }), role: "alert" },
    dismissButton,
    props.children
  );
};

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

;

},{"classnames":2,"react":"react"}],112:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _datasetCards = require("./dataset-cards");

var _datasetCards2 = _interopRequireDefault(_datasetCards);

var _footer = require("./footer");

var _footer2 = _interopRequireDefault(_footer);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var FOOTER_HEIGHT = 81;

function Page(props) {
  return _react2.default.createElement(
    "div",
    { className: "page" },
    _react2.default.createElement(
      "div",
      { className: "basic-margin hi-Green container-fluid" },
      _react2.default.createElement(
        "nav",
        { className: "navbar " },
        _react2.default.createElement(
          "div",
          { className: "container" },
          _react2.default.createElement(
            "div",
            { className: "navbar-header" },
            " ",
            _react2.default.createElement(
              "a",
              { className: "navbar-brand", href: "#" },
              _react2.default.createElement("img", { src: "images/logo-timbuctoo.svg", className: "logo", alt: "timbuctoo" })
            ),
            " "
          ),
          _react2.default.createElement(
            "div",
            { id: "navbar", className: "navbar-collapse collapse" },
            _react2.default.createElement(
              "ul",
              { className: "nav navbar-nav navbar-right" },
              props.username ? _react2.default.createElement(
                "li",
                null,
                _react2.default.createElement(
                  "a",
                  { href: props.userlocation || '#' },
                  _react2.default.createElement("span", { className: "glyphicon glyphicon-user" }),
                  " ",
                  props.username
                )
              ) : null
            )
          )
        )
      )
    ),
    _react2.default.createElement(
      "div",
      { style: { marginBottom: FOOTER_HEIGHT + "px" } },
      props.children,
      props.vres && props.showDatasets ? _react2.default.createElement(
        "div",
        { className: "container" },
        _react2.default.createElement(_datasetCards2.default, { caption: "Explore all datasets", vres: props.vres })
      ) : null
    ),
    _react2.default.createElement(_footer2.default, null)
  );
}

exports.default = Page;

},{"./dataset-cards":106,"./footer":110,"react":"react"}],113:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _selectField = require("../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var AddProperty = function (_React$Component) {
  _inherits(AddProperty, _React$Component);

  function AddProperty(props) {
    _classCallCheck(this, AddProperty);

    var _this = _possibleConstructorReturn(this, (AddProperty.__proto__ || Object.getPrototypeOf(AddProperty)).call(this, props));

    _this.state = {
      newName: "",
      newType: null
    };
    return _this;
  }

  _createClass(AddProperty, [{
    key: "render",
    value: function render() {
      var _this2 = this;

      var _state = this.state;
      var newName = _state.newName;
      var newType = _state.newType;
      var _props = this.props;
      var onAddCustomProperty = _props.onAddCustomProperty;
      var archetypeFields = _props.archetypeFields;
      var availableArchetypes = _props.availableArchetypes;


      var relationTypeOptions = archetypeFields.filter(function (prop) {
        return prop.type === "relation";
      }).filter(function (prop) {
        return newType === "relation-to-existing" || availableArchetypes.indexOf(prop.relation.targetCollection) > -1;
      }).map(function (prop) {
        return _react2.default.createElement(
          "span",
          { key: prop.name, value: prop.name },
          prop.name
        );
      });

      return _react2.default.createElement(
        "div",
        { className: "row small-margin" },
        _react2.default.createElement(
          "div",
          { className: "col-sm-2" },
          newType === "relation" || newType === "relation-to-existing" ? _react2.default.createElement(
            _selectField2.default,
            {
              value: newName,
              onChange: function onChange(value) {
                return _this2.setState({ newName: value });
              },
              onClear: function onClear() {
                return _this2.setState({ newName: "" });
              } },
            _react2.default.createElement(
              "span",
              { type: "placeholder" },
              "Choose a relation type..."
            ),
            relationTypeOptions
          ) : _react2.default.createElement("input", { className: "form-control",
            onChange: function onChange(ev) {
              return _this2.setState({ newName: ev.target.value });
            },
            placeholder: "Property name",
            value: newName,
            disabled: newType !== "text" })
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-8" },
          _react2.default.createElement(
            "span",
            null,
            _react2.default.createElement(
              _selectField2.default,
              {
                value: newType,
                onChange: function onChange(value) {
                  return _this2.setState({ newType: value, newName: value === "relation" ? null : newName });
                },
                onClear: function onClear() {
                  return _this2.setState({ newType: null });
                } },
              _react2.default.createElement(
                "span",
                { type: "placeholder" },
                "Choose a type..."
              ),
              _react2.default.createElement(
                "span",
                { value: "text" },
                "Text"
              ),
              _react2.default.createElement(
                "span",
                { value: "relation" },
                "Relation"
              ),
              _react2.default.createElement(
                "span",
                { value: "relation-to-existing" },
                "Relation to existing Timbuctoo collection"
              )
            )
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-2" },
          _react2.default.createElement(
            "button",
            { className: "pull-right btn btn-default", disabled: !(newName && newType),
              onClick: function onClick() {
                _this2.setState({ newName: null, newType: null });
                onAddCustomProperty(newName, newType);
              } },
            "Add property"
          )
        )
      );
    }
  }]);

  return AddProperty;
}(_react2.default.Component);

AddProperty.propTypes = {
  importData: _react2.default.PropTypes.object,
  mappings: _react2.default.PropTypes.object,
  onAddCustomProperty: _react2.default.PropTypes.func
};

exports.default = AddProperty;

},{"../fields/select-field":108,"react":"react"}],114:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _selectField = require("../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ColumnSelect = function (_React$Component) {
  _inherits(ColumnSelect, _React$Component);

  function ColumnSelect() {
    _classCallCheck(this, ColumnSelect);

    return _possibleConstructorReturn(this, (ColumnSelect.__proto__ || Object.getPrototypeOf(ColumnSelect)).apply(this, arguments));
  }

  _createClass(ColumnSelect, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var columns = _props.columns;
      var propertyMapping = _props.propertyMapping;
      var onColumnSelect = _props.onColumnSelect;
      var onClearColumn = _props.onClearColumn;
      var placeholder = _props.placeholder;


      var selectedColumn = propertyMapping && propertyMapping.variable[0] ? propertyMapping.variable[0].variableName : null;

      return propertyMapping && propertyMapping.confirmed ? _react2.default.createElement(
        "div",
        { className: "from-excel pad-6-12" },
        _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
        " ",
        selectedColumn
      ) : _react2.default.createElement(
        _selectField2.default,
        { value: selectedColumn,
          onChange: function onChange(column) {
            return onColumnSelect([{ variableName: column }]);
          },
          onClear: function onClear() {
            return onClearColumn(0);
          } },
        _react2.default.createElement(
          "span",
          { type: "placeholder", className: "from-excel" },
          _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
          " ",
          placeholder || "Select an excel column"
        ),
        columns.filter(function (col) {
          return col.name === selectedColumn || !col.isConfirmed && !col.isIgnored;
        }).map(function (col) {
          return _react2.default.createElement(
            "span",
            { key: col.name, value: col.name, className: "from-excel" },
            _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
            " ",
            col.name
          );
        })
      );
    }
  }]);

  return ColumnSelect;
}(_react2.default.Component);

ColumnSelect.propTypes = {
  collectionData: _react2.default.PropTypes.object,
  mappings: _react2.default.PropTypes.object,
  name: _react2.default.PropTypes.string,
  onClearFieldMapping: _react2.default.PropTypes.func,
  onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = ColumnSelect;

},{"../fields/select-field":108,"react":"react"}],115:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _text = require("./text");

var _text2 = _interopRequireDefault(_text);

var _relation = require("./relation");

var _relation2 = _interopRequireDefault(_relation);

var _relationToExisting = require("./relation-to-existing");

var _relationToExisting2 = _interopRequireDefault(_relationToExisting);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var typeMap = {
  text: function text(props) {
    return _react2.default.createElement(_text2.default, props);
  },
  datable: function datable(props) {
    return _react2.default.createElement(_text2.default, props);
  },
  relation: function relation(props) {
    return _react2.default.createElement(_relation2.default, props);
  },
  "relation-to-existing": function relationToExisting(props) {
    return _react2.default.createElement(_relationToExisting2.default, props);
  }
};

var PropertyForm = function (_React$Component) {
  _inherits(PropertyForm, _React$Component);

  function PropertyForm() {
    _classCallCheck(this, PropertyForm);

    return _possibleConstructorReturn(this, (PropertyForm.__proto__ || Object.getPrototypeOf(PropertyForm)).apply(this, arguments));
  }

  _createClass(PropertyForm, [{
    key: "canConfirm",
    value: function canConfirm(variable) {
      var type = this.props.type;

      if (!variable || variable.length === 0) {
        return false;
      }
      if (type === "relation") {
        return variable[0].variableName && variable[0].targetCollection && variable[0].targetVariableName;
      } else if (type === "relation-to-existing") {
        return variable[0].variableName && variable[0].targetExistingTimbuctooVre;
      }
      return variable.filter(function (m) {
        return m.variableName;
      }).length === variable.length;
    }
  }, {
    key: "render",
    value: function render() {
      var _props = this.props;
      var name = _props.name;
      var type = _props.type;
      var custom = _props.custom;
      var propertyMapping = _props.propertyMapping;
      var _props2 = this.props;
      var onRemoveCustomProperty = _props2.onRemoveCustomProperty;
      var onConfirmFieldMappings = _props2.onConfirmFieldMappings;
      var onUnconfirmFieldMappings = _props2.onUnconfirmFieldMappings;
      var onSetFieldMapping = _props2.onSetFieldMapping;
      var onClearFieldMapping = _props2.onClearFieldMapping;


      var confirmed = propertyMapping && propertyMapping.confirmed;

      var confirmButton = propertyMapping && this.canConfirm(propertyMapping.variable) && !confirmed ? _react2.default.createElement(
        "button",
        { className: "btn btn-success", onClick: function onClick() {
            return onConfirmFieldMappings(name);
          } },
        "Confirm"
      ) : confirmed ? _react2.default.createElement(
        "button",
        { className: "btn btn-blank", onClick: function onClick() {
            return onUnconfirmFieldMappings(name);
          } },
        _react2.default.createElement("span", { className: "hi-success glyphicon glyphicon-ok" })
      ) : null;

      var formComponent = typeMap[type](_extends({}, this.props, {
        onColumnSelect: function onColumnSelect(value) {
          return onSetFieldMapping(name, value);
        },
        onClearColumn: function onClearColumn(valueIdx) {
          return onClearFieldMapping(name, valueIdx);
        }
      }));

      return _react2.default.createElement(
        "div",
        { className: "row small-margin" },
        _react2.default.createElement(
          "div",
          { className: "col-sm-2 pad-6-12" },
          _react2.default.createElement(
            "strong",
            null,
            name
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-2 pad-6-12" },
          _react2.default.createElement(
            "span",
            { className: "pull-right" },
            "(",
            type,
            ")"
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-5" },
          formComponent
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-1" },
          custom ? _react2.default.createElement(
            "button",
            { className: "btn btn-blank pull-right", type: "button", onClick: function onClick() {
                return onRemoveCustomProperty(name);
              } },
            _react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
          ) : null
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-2 hi-success" },
          _react2.default.createElement(
            "span",
            { className: "pull-right" },
            confirmButton
          )
        )
      );
    }
  }]);

  return PropertyForm;
}(_react2.default.Component);

exports.default = PropertyForm;

},{"./relation":117,"./relation-to-existing":116,"./text":118,"react":"react"}],116:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _columnSelect = require("./column-select");

var _columnSelect2 = _interopRequireDefault(_columnSelect);

var _selectField = require("../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Form = function (_React$Component) {
  _inherits(Form, _React$Component);

  function Form() {
    _classCallCheck(this, Form);

    return _possibleConstructorReturn(this, (Form.__proto__ || Object.getPrototypeOf(Form)).apply(this, arguments));
  }

  _createClass(Form, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var _onColumnSelect = _props.onColumnSelect;
      var onClearColumn = _props.onClearColumn;
      var propertyMapping = _props.propertyMapping;
      var targetableVres = _props.targetableVres;


      var relationInfo = propertyMapping && propertyMapping.variable[0] ? propertyMapping.variable[0] : {};

      var sourceColumnProps = _extends({}, this.props, {
        placeholder: "Select a source column...",
        onColumnSelect: function onColumnSelect(value) {
          return _onColumnSelect([_extends({}, relationInfo, { variableName: value[0].variableName })]);
        }
      });

      return _react2.default.createElement(
        "div",
        null,
        _react2.default.createElement(_columnSelect2.default, sourceColumnProps),
        _react2.default.createElement(
          _selectField2.default,
          { value: relationInfo.targetExistingTimbuctooVre || null,
            onChange: function onChange(value) {
              return _onColumnSelect([_extends({}, relationInfo, { targetExistingTimbuctooVre: value })]);
            },
            onClear: function onClear() {
              return onClearColumn(0);
            } },
          _react2.default.createElement(
            "span",
            { type: "placeholder", className: "from-excel" },
            _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
            " Select a target dataset..."
          ),
          targetableVres.map(function (dataset) {
            return _react2.default.createElement(
              "span",
              { key: dataset, value: dataset, className: "from-excel" },
              _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
              " ",
              dataset
            );
          })
        )
      );
    }
  }]);

  return Form;
}(_react2.default.Component);

Form.propTypes = {
  collectionData: _react2.default.PropTypes.object,
  mappings: _react2.default.PropTypes.object,
  name: _react2.default.PropTypes.string,
  onClearFieldMapping: _react2.default.PropTypes.func,
  onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"../fields/select-field":108,"./column-select":114,"react":"react"}],117:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _columnSelect = require("./column-select");

var _columnSelect2 = _interopRequireDefault(_columnSelect);

var _selectField = require("../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Form = function (_React$Component) {
  _inherits(Form, _React$Component);

  function Form() {
    _classCallCheck(this, Form);

    return _possibleConstructorReturn(this, (Form.__proto__ || Object.getPrototypeOf(Form)).apply(this, arguments));
  }

  _createClass(Form, [{
    key: "render",
    value: function render() {
      var _this2 = this;

      var _props = this.props;
      var _onColumnSelect = _props.onColumnSelect;
      var onClearColumn = _props.onClearColumn;
      var propertyMapping = _props.propertyMapping;
      var availableCollectionColumnsPerArchetype = _props.availableCollectionColumnsPerArchetype;
      var archetypeFields = _props.archetypeFields;


      var relationInfo = propertyMapping && propertyMapping.variable[0] ? propertyMapping.variable[0] : {};

      var targetArchetypeCollection = archetypeFields.find(function (af) {
        return af.name === _this2.props.name;
      }).relation.targetCollection;
      var targetCollectionColumns = availableCollectionColumnsPerArchetype[targetArchetypeCollection];
      var targetCollections = targetCollectionColumns.map(function (t) {
        return t.collectionName;
      });

      var sourceColumnProps = _extends({}, this.props, {
        placeholder: "Select a source column...",
        onColumnSelect: function onColumnSelect(value) {
          return _onColumnSelect([_extends({}, relationInfo, { variableName: value[0].variableName })]);
        }
      });

      var targetColumnProps = relationInfo.targetCollection ? _extends({}, this.props, {
        placeholder: "Select a target column...",
        propertyMapping: { variable: [{ variableName: relationInfo.targetVariableName }] },
        columns: targetCollectionColumns.find(function (t) {
          return t.collectionName === relationInfo.targetCollection;
        }).columns.map(function (col) {
          return { name: col };
        }),
        onColumnSelect: function onColumnSelect(value) {
          return _onColumnSelect([_extends({}, relationInfo, { targetVariableName: value[0].variableName })]);
        }
      }) : null;

      return _react2.default.createElement(
        "div",
        null,
        _react2.default.createElement(_columnSelect2.default, sourceColumnProps),
        _react2.default.createElement(
          _selectField2.default,
          { value: relationInfo.targetCollection || null,
            onChange: function onChange(value) {
              return _onColumnSelect([_extends({}, relationInfo, { targetCollection: value })]);
            },
            onClear: function onClear() {
              return onClearColumn(0);
            } },
          _react2.default.createElement(
            "span",
            { type: "placeholder", className: "from-excel" },
            _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
            " Select a target sheet..."
          ),
          targetCollections.map(function (collection) {
            return _react2.default.createElement(
              "span",
              { key: collection, value: collection, className: "from-excel" },
              _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
              " ",
              collection
            );
          })
        ),
        targetColumnProps ? _react2.default.createElement(_columnSelect2.default, targetColumnProps) : null
      );
    }
  }]);

  return Form;
}(_react2.default.Component);

Form.propTypes = {
  collectionData: _react2.default.PropTypes.object,
  mappings: _react2.default.PropTypes.object,
  name: _react2.default.PropTypes.string,
  onClearFieldMapping: _react2.default.PropTypes.func,
  onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"../fields/select-field":108,"./column-select":114,"react":"react"}],118:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _columnSelect = require("./column-select");

var _columnSelect2 = _interopRequireDefault(_columnSelect);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Form = function (_React$Component) {
  _inherits(Form, _React$Component);

  function Form() {
    _classCallCheck(this, Form);

    return _possibleConstructorReturn(this, (Form.__proto__ || Object.getPrototypeOf(Form)).apply(this, arguments));
  }

  _createClass(Form, [{
    key: "render",
    value: function render() {
      return _react2.default.createElement(_columnSelect2.default, this.props);
    }
  }]);

  return Form;
}(_react2.default.Component);

Form.propTypes = {
  collectionData: _react2.default.PropTypes.object,
  mappings: _react2.default.PropTypes.object,
  name: _react2.default.PropTypes.string,
  onClearFieldMapping: _react2.default.PropTypes.func,
  onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"./column-select":114,"react":"react"}],119:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (appState) {
  return {
    isUploading: appState.importData.isUploading,
    userId: appState.userdata.userId,
    vres: appState.userdata.myVres || {}
  };
};

},{}],120:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
// Helper function for react-redux connect
function getConfirmedCols(props, variables) {
  var mappings = props.mappings;
  var activeCollection = props.importData.activeCollection;


  return variables.map(function (value, i) {
    return { value: value, index: i };
  }).filter(function (colSpec) {
    return mappings.collections[activeCollection].mappings.filter(function (m) {
      return m.confirmed;
    }).map(function (m) {
      return m.variable.map(function (v) {
        return v.variableName;
      });
    }).reduce(function (a, b) {
      return a.concat(b);
    }, []).indexOf(colSpec.value) > -1;
  }).map(function (colSpec) {
    return colSpec.index;
  });
}

// Helper function for react-redux connect
function mappingsAreComplete(props, sheet) {
  var mappings = props.mappings;


  var confirmedColCount = mappings.collections[sheet.collection].mappings.filter(function (m) {
    return m.confirmed;
  }).map(function (m) {
    return m.variable.map(function (v) {
      return v.variableName;
    });
  }).reduce(function (a, b) {
    return a.concat(b);
  }, []).filter(function (x, idx, self) {
    return self.indexOf(x) === idx;
  }).length;

  return confirmedColCount + mappings.collections[sheet.collection].ignoredColumns.length === sheet.variables.length;
}

function getTargetableVres(mine, vres) {
  var myVres = Object.keys(mine || {}).map(function (key) {
    return mine[key];
  }).filter(function (vre) {
    return vre.published;
  }).map(function (vre) {
    return vre.name;
  });
  var publicVres = Object.keys(vres || {}).map(function (key) {
    return vres[key].name;
  });

  return myVres.concat(publicVres);
}

// Moves to react-redux connect
function transformProps(props) {
  var _props$importData = props.importData;
  var sheets = _props$importData.sheets;
  var activeCollection = _props$importData.activeCollection;
  var uploadedFileName = _props$importData.uploadedFileName;
  var publishErrors = _props$importData.publishErrors;
  var _props$userdata = props.userdata;
  var myVres = _props$userdata.myVres;
  var vres = _props$userdata.vres;
  var mappings = props.mappings;
  var archetype = props.archetype;

  var collectionData = sheets.find(function (sheet) {
    return sheet.collection === activeCollection;
  });
  var rows = collectionData.rows;
  var variables = collectionData.variables;


  var confirmedCols = getConfirmedCols(props, variables);
  var ignoredColumns = mappings.collections[activeCollection].ignoredColumns;


  var availableArchetypes = Object.keys(mappings.collections).map(function (key) {
    return mappings.collections[key].archetypeName;
  });

  return {
    sheets: sheets,
    activeCollection: activeCollection,
    uploadedFileName: uploadedFileName,
    collectionTabs: sheets.map(function (sheet) {
      return {
        collectionName: sheet.collection,
        archetypeName: mappings.collections[sheet.collection].archetypeName,
        active: activeCollection === sheet.collection,
        complete: mappingsAreComplete(props, sheet)
      };
    }),
    rows: rows.map(function (row) {
      return row.map(function (cell, i) {
        return {
          value: cell.value,
          error: cell.error || null,
          ignored: ignoredColumns.indexOf(variables[i]) > -1
        };
      });
    }),
    headers: variables.map(function (variable, i) {
      return {
        name: variable,
        isConfirmed: ignoredColumns.indexOf(i) < 0 && confirmedCols.indexOf(i) > -1,
        isIgnored: ignoredColumns.indexOf(variable) > -1
      };
    }),
    nextUrl: sheets.find(function (sheet) {
      return sheet.collection === activeCollection;
    }).nextUrl,
    archetypeFields: archetype[mappings.collections[activeCollection].archetypeName],
    propertyMappings: mappings.collections[activeCollection].mappings,
    customPropertyMappings: mappings.collections[activeCollection].customProperties,
    availableArchetypes: availableArchetypes,
    publishing: mappings.publishing,
    availableCollectionColumnsPerArchetype: availableArchetypes.map(function (archetypeName) {
      return {
        key: archetypeName,
        values: Object.keys(mappings.collections).filter(function (collectionName) {
          return mappings.collections[collectionName].archetypeName === archetypeName;
        }).map(function (collectionName) {
          return {
            collectionName: collectionName,
            columns: sheets.find(function (sheet) {
              return sheet.collection === collectionName;
            }).variables
          };
        })
      };
    }).reduce(function (archetypeToCollectionColumnMapping, currentArchetypeAndCols) {
      archetypeToCollectionColumnMapping[currentArchetypeAndCols.key] = currentArchetypeAndCols.values;
      return archetypeToCollectionColumnMapping;
    }, {}),
    publishErrors: publishErrors,
    showCollectionsAreConnectedMessage: props.messages.showCollectionsAreConnectedMessage,
    targetableVres: getTargetableVres(myVres, vres)
  };
}

exports.default = transformProps;

},{}],121:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (appState) {
  return {
    mappings: appState.mappings,
    sheets: appState.importData.sheets,
    uploadedFileName: appState.importData.uploadedFileName,
    archetype: appState.archetype,
    onMapCollectionArchetype: appState.onMapCollectionArchetype,
    onConfirmCollectionArchetypeMappings: appState.onConfirmCollectionArchetypeMappings,
    showFileIsUploadedMessage: appState.messages.showFileIsUploadedMessage
  };
};

},{}],122:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (appState) {
  return {
    userId: appState.userdata.userId,
    isUploading: appState.importData.isUploading
  };
};

},{}],123:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _router = require("../../router");

exports.default = function (appState, ownProps) {
  var pathname = ownProps.location.pathname;

  return {
    username: appState.userdata.userId,
    vres: appState.userdata.vres,
    showDatasets: pathname === "/" || pathname === _router.urls.collectionsOverview()
  };
};

},{"../../router":134}],124:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var DataRow = function (_React$Component) {
  _inherits(DataRow, _React$Component);

  function DataRow() {
    _classCallCheck(this, DataRow);

    return _possibleConstructorReturn(this, (DataRow.__proto__ || Object.getPrototypeOf(DataRow)).apply(this, arguments));
  }

  _createClass(DataRow, [{
    key: "render",
    value: function render() {
      var row = this.props.row;

      return _react2.default.createElement(
        "tr",
        null,
        row.map(function (cell, i) {
          return _react2.default.createElement(
            "td",
            { className: (0, _classnames2.default)({
                ignored: cell.ignored,
                danger: cell.error ? true : false
              }), key: i },
            cell.value,
            cell.error ? _react2.default.createElement("span", { className: "pull-right glyphicon glyphicon-exclamation-sign", style: { cursor: "pointer" }, title: cell.error }) : null
          );
        })
      );
    }
  }]);

  return DataRow;
}(_react2.default.Component);

DataRow.propTypes = {
  row: _react2.default.PropTypes.array
};

exports.default = DataRow;

},{"classnames":2,"react":"react"}],125:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var HeaderCell = function (_React$Component) {
  _inherits(HeaderCell, _React$Component);

  function HeaderCell() {
    _classCallCheck(this, HeaderCell);

    return _possibleConstructorReturn(this, (HeaderCell.__proto__ || Object.getPrototypeOf(HeaderCell)).apply(this, arguments));
  }

  _createClass(HeaderCell, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var header = _props.header;
      var isConfirmed = _props.isConfirmed;
      var isIgnored = _props.isIgnored;
      var onIgnoreColumnToggle = _props.onIgnoreColumnToggle;


      return _react2.default.createElement(
        "th",
        { className: (0, _classnames2.default)({
            success: isConfirmed,
            info: !isConfirmed && !isIgnored,
            danger: !isConfirmed && isIgnored
          }) },
        header,
        _react2.default.createElement("a", { style: { cursor: "pointer" }, className: (0, _classnames2.default)("pull-right", "glyphicon", {
            "glyphicon-ok-sign": isConfirmed,
            "glyphicon-question-sign": !isConfirmed && !isIgnored,
            "glyphicon-remove": !isConfirmed && isIgnored
          }), onClick: function onClick() {
            return !isConfirmed ? onIgnoreColumnToggle(header) : null;
          } })
      );
    }
  }]);

  return HeaderCell;
}(_react2.default.Component);

HeaderCell.propTypes = {
  header: _react2.default.PropTypes.string,
  isConfirmed: _react2.default.PropTypes.bool,
  isIgnored: _react2.default.PropTypes.bool,
  onIgnoreColumnToggle: _react2.default.PropTypes.func
};

exports.default = HeaderCell;

},{"classnames":2,"react":"react"}],126:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var UploadButton = function (_React$Component) {
  _inherits(UploadButton, _React$Component);

  function UploadButton() {
    _classCallCheck(this, UploadButton);

    return _possibleConstructorReturn(this, (UploadButton.__proto__ || Object.getPrototypeOf(UploadButton)).apply(this, arguments));
  }

  _createClass(UploadButton, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var classNames = _props.classNames;
      var isUploading = _props.isUploading;
      var onUploadFileSelect = _props.onUploadFileSelect;
      var glyphicon = _props.glyphicon;
      var label = _props.label;

      return _react2.default.createElement(
        "form",
        null,
        _react2.default.createElement(
          "label",
          { className: _classnames2.default.apply(undefined, _toConsumableArray(classNames).concat([{ disabled: isUploading }])) },
          _react2.default.createElement("span", { className: glyphicon }),
          " ",
          isUploading ? "Uploading..." : label,
          _react2.default.createElement("input", {
            disabled: isUploading,
            onChange: function onChange(e) {
              return onUploadFileSelect(e.target.files);
            },
            style: { display: "none" },
            type: "file" })
        )
      );
    }
  }]);

  return UploadButton;
}(_react2.default.Component);

exports.default = UploadButton;

},{"classnames":2,"react":"react"}],127:[function(require,module,exports){
"use strict";

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _xhrMock = require("xhr-mock");

var _xhrMock2 = _interopRequireDefault(_xhrMock);

var _servermocks = require("./servermocks");

var _servermocks2 = _interopRequireDefault(_servermocks);

var _router = require("./router");

var _router2 = _interopRequireDefault(_router);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

if ("false" === "true") {
	console.log("Using mock server!");
	var orig = window.XMLHttpRequest;
	_xhrMock2.default.setup(); //mock window.XMLHttpRequest usages
	var mock = window.XMLHttpRequest;
	window.XMLHttpRequest = orig;
	_xhr2.default.XMLHttpRequest = mock;
	_xhr2.default.XDomainRequest = mock;
	(0, _servermocks2.default)(_xhrMock2.default, orig);
}
var actions = (0, _actions2.default)(_router.navigateTo, _store2.default.dispatch.bind(_store2.default));

function checkTokenInUrl(state) {
	var path = window.location.search.substr(1);
	var params = path.split('&');

	for (var i in params) {
		var _params$i$split = params[i].split('=');

		var _params$i$split2 = _slicedToArray(_params$i$split, 2);

		var key = _params$i$split2[0];
		var value = _params$i$split2[1];

		if (key === 'hsid' && !state.userdata.userId) {
			actions.onToken(value);

			break;
		}
	}
}

document.addEventListener("DOMContentLoaded", function () {
	var state = _store2.default.getState();
	checkTokenInUrl(state);
	if (!state.archetype || Object.keys(state.archetype).length === 0) {
		(0, _xhr2.default)("" + "/v2.1/metadata/Admin", function (err, resp) {
			_store2.default.dispatch({ type: "SET_ARCHETYPE_METADATA", data: JSON.parse(resp.body) });
		});
	}
	_reactDom2.default.render(_router2.default, document.getElementById("app"));
});

},{"./actions":99,"./router":134,"./servermocks":135,"./store":136,"react":"react","react-dom":"react-dom","xhr":97,"xhr-mock":93}],128:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

exports.default = function () {
	var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
	var action = arguments[1];

	switch (action.type) {
		case "SET_ARCHETYPE_METADATA":
			return action.data;
	}

	return state;
};

var _persist = require("../util/persist");

var initialState = (0, _persist.getItem)("archetype") || {};

},{"../util/persist":140}],129:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol ? "symbol" : typeof obj; };

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
	var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
	var action = arguments[1];

	var _ret = function () {
		switch (action.type) {
			case "START_UPLOAD":
				return {
					v: _extends({}, initialState, { isUploading: true })
				};
			case "FINISH_UPLOAD":
				return {
					v: _extends({}, state, {
						isUploading: false,
						publishErrors: false,
						uploadedFileName: action.uploadedFileName,
						sheets: action.data.collections.map(function (sheet) {
							return {
								collection: sheet.name,
								variables: sheet.variables,
								rows: [],
								nextUrl: sheet.data,
								nextUrlWithErrors: sheet.dataWithErrors
							};
						}),
						activeCollection: action.data.collections[0].name,
						vre: action.data.vre,
						saveMappingUrl: action.data.saveMapping,
						executeMappingUrl: action.data.executeMapping
					})
				};
			case "COLLECTION_ITEMS_LOADING_SUCCEEDED":
				var sheetIdx = findIndex(state.sheets, function (sheet) {
					return sheet.collection === action.collection;
				});
				return {
					v: _extends({}, state, {
						sheets: [].concat(_toConsumableArray(state.sheets.slice(0, sheetIdx)), [_extends({}, state.sheets[sheetIdx], {
							rows: addRows(state.sheets[sheetIdx].rows, action.data.items, state.sheets[sheetIdx].variables),
							nextUrl: action.data._next
						})], _toConsumableArray(state.sheets.slice(sheetIdx + 1)))
					})
				};

			case "COLLECTION_ITEMS_LOADING_FINISHED":
				var result = _extends({}, state);
				result.sheets = result.sheets.slice();
				result.sheets.forEach(function (sheet, i) {
					if (sheet.collection === action.collection) {
						result.sheets[i] = _extends({}, sheet, {
							isLoading: false
						});
					}
				});

				return {
					v: result
				};
			case "SET_ACTIVE_COLLECTION":
				return {
					v: _extends({}, state, { activeCollection: action.collection })
				};
			case "PUBLISH_HAD_ERROR":
				// clear the sheets to force reload
				return {
					v: _extends({}, state, {
						publishErrors: true,
						sheets: state.sheets.map(function (sheet) {
							return _extends({}, sheet, {
								rows: [],
								nextUrl: sheet.nextUrlWithErrors
							});
						})
					})
				};
			case "PUBLISH_SUCCEEDED":
				// clear the sheets to force reload
				return {
					v: _extends({}, state, {
						publishErrors: false,
						sheets: state.sheets.map(function (sheet) {
							return _extends({}, sheet, {
								rows: [],
								nextUrl: sheet.nextUrlWithErrors
							});
						})
					})
				};
		}
	}();

	if ((typeof _ret === "undefined" ? "undefined" : _typeof(_ret)) === "object") return _ret.v;
	return state;
};

var _persist = require("../util/persist");

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var initialState = (0, _persist.getItem)("importData") || {
	isUploading: false,
	sheets: null,
	activeCollection: null,
	publishErrors: false,
	uploadedFileName: undefined
};

function findIndex(arr, f) {
	var length = arr.length;
	for (var i = 0; i < length; i++) {
		if (f(arr[i], i, arr)) {
			return i;
		}
	}
	return -1;
}

function sheetRowFromDictToArray(rowdict, arrayOfVariableNames, mappingErrors) {
	return arrayOfVariableNames.map(function (name) {
		return {
			value: rowdict[name],
			error: mappingErrors[name] || undefined
		};
	});
}

function addRows(curRows, newRows, arrayOfVariableNames) {
	return curRows.concat(newRows.map(function (item) {
		return sheetRowFromDictToArray(item.values || {}, arrayOfVariableNames, item.errors || {});
	}));
}

},{"../util/persist":140}],130:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _importData = require("./import-data");

var _importData2 = _interopRequireDefault(_importData);

var _archetype = require("./archetype");

var _archetype2 = _interopRequireDefault(_archetype);

var _mappings = require("./mappings");

var _mappings2 = _interopRequireDefault(_mappings);

var _userdata = require("./userdata");

var _userdata2 = _interopRequireDefault(_userdata);

var _messages = require("./messages");

var _messages2 = _interopRequireDefault(_messages);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = {
	messages: _messages2.default,
	importData: _importData2.default,
	archetype: _archetype2.default,
	mappings: _mappings2.default,
	userdata: _userdata2.default
};

},{"./archetype":128,"./import-data":129,"./mappings":131,"./messages":132,"./userdata":133}],131:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
  var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
  var action = arguments[1];

  switch (action.type) {
    case "START_UPLOAD":
      return initialState;
    case "FINISH_UPLOAD":
      return Object.keys(state.collections).length > 0 ? state : _extends({}, state, { collections: action.data.collections.reduce(scaffoldCollectionMappings, {}) });

    case "MAP_COLLECTION_ARCHETYPE":
      return mapCollectionArchetype(state, action);

    case "CONFIRM_COLLECTION_ARCHETYPE_MAPPINGS":
      return _extends({}, state, { confirmed: true });

    case "SET_FIELD_MAPPING":
      return upsertFieldMapping(state, action);

    case "CLEAR_FIELD_MAPPING":
      return clearFieldMapping(state, action);

    case "SET_DEFAULT_VALUE":
      return setDefaultValue(state, action);

    case "CONFIRM_FIELD_MAPPINGS":
      return setFieldConfirmation(state, action, true);

    case "UNCONFIRM_FIELD_MAPPINGS":
      return setFieldConfirmation(state, action, false);

    case "SET_VALUE_MAPPING":
      return setValueMapping(state, action);

    case "TOGGLE_IGNORED_COLUMN":
      return toggleIgnoredColumn(state, action);

    case "ADD_CUSTOM_PROPERTY":
      return addCustomProperty(state, action);

    case "REMOVE_CUSTOM_PROPERTY":
      return removeCustomProperty(state, action);

    case "PUBLISH_STARTED":
      return _extends({}, state, {
        publishing: true
      });
    case "PUBLISH_FINISHED":
      return _extends({}, state, {
        publishing: false
      });
  }
  return state;
};

var _setIn = require("../util/set-in");

var _setIn2 = _interopRequireDefault(_setIn);

var _getIn = require("../util/get-in");

var _getIn2 = _interopRequireDefault(_getIn);

var _persist = require("../util/persist");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

var newVariableDesc = function newVariableDesc(property, variableSpec) {
  return {
    property: property,
    variable: variableSpec,
    defaultValue: [],
    confirmed: false,
    valueMappings: {}
  };
};

function scaffoldCollectionMappings(init, sheet) {
  return _extends(init, _defineProperty({}, sheet.name, {
    archetypeName: null,
    mappings: [],
    ignoredColumns: [],
    customProperties: []
  }));
}

var initialState = (0, _persist.getItem)("mappings") || {
  collections: {},
  confirmed: false,
  publishing: false
};

var getMappingIndex = function getMappingIndex(state, action) {
  return state.collections[action.collection].mappings.map(function (m, i) {
    return { index: i, m: m };
  }).filter(function (mSpec) {
    return mSpec.m.property === action.propertyField;
  }).reduce(function (prev, cur) {
    return cur.index;
  }, -1);
};

var mapCollectionArchetype = function mapCollectionArchetype(state, action) {
  var newCollections = (0, _setIn2.default)([action.collection, "archetypeName"], action.value, state.collections);
  newCollections = (0, _setIn2.default)([action.collection, "mappings"], [], newCollections);

  return _extends({}, state, { collections: newCollections });
};

var upsertFieldMapping = function upsertFieldMapping(state, action) {
  var foundIdx = getMappingIndex(state, action);
  var newCollections = (0, _setIn2.default)([action.collection, "mappings", foundIdx < 0 ? (0, _getIn2.default)([action.collection, "mappings"], state.collections).length : foundIdx], newVariableDesc(action.propertyField, action.importedField), state.collections);

  return _extends({}, state, { collections: newCollections });
};

var clearFieldMapping = function clearFieldMapping(state, action) {
  var foundIdx = getMappingIndex(state, action);
  if (foundIdx < 0) {
    return state;
  }

  var current = (0, _getIn2.default)([action.collection, "mappings", foundIdx, "variable"], state.collections).filter(function (m, i) {
    return i !== action.clearIndex;
  });

  var newCollections = void 0;
  if (current.length > 0) {
    newCollections = (0, _setIn2.default)([action.collection, "mappings", foundIdx, "variable"], current, state.collections);
  } else {
    var newMappings = (0, _getIn2.default)([action.collection, "mappings"], state.collections).filter(function (m, i) {
      return i !== foundIdx;
    });
    newCollections = (0, _setIn2.default)([action.collection, "mappings"], newMappings, state.collections);
  }

  return _extends({}, state, { collections: newCollections });
};

var setDefaultValue = function setDefaultValue(state, action) {
  var foundIdx = getMappingIndex(state, action);
  if (foundIdx > -1) {
    var newCollections = (0, _setIn2.default)([action.collection, "mappings", foundIdx, "defaultValue"], action.value, state.collections);
    return _extends({}, state, { collections: newCollections });
  }

  return state;
};

var setFieldConfirmation = function setFieldConfirmation(state, action, value) {
  var current = ((0, _getIn2.default)([action.collection, "mappings"], state.collections) || []).map(function (vm) {
    return _extends({}, vm, { confirmed: action.propertyField === vm.property ? value : vm.confirmed });
  });
  var newCollections = (0, _setIn2.default)([action.collection, "mappings"], current, state.collections);

  if (value === true) {
    (function () {
      var confirmedVariableNames = current.map(function (m) {
        return m.variable.map(function (v) {
          return v.variableName;
        });
      }).reduce(function (a, b) {
        return a.concat(b);
      });
      var newIgnoredColums = (0, _getIn2.default)([action.collection, "ignoredColumns"], state.collections).filter(function (ic) {
        return confirmedVariableNames.indexOf(ic) < 0;
      });
      newCollections = (0, _setIn2.default)([action.collection, "ignoredColumns"], newIgnoredColums, newCollections);
    })();
  }

  return _extends({}, state, { collections: newCollections });
};

var setValueMapping = function setValueMapping(state, action) {
  var foundIdx = getMappingIndex(state, action);

  if (foundIdx > -1) {
    var newCollections = (0, _setIn2.default)([action.collection, "mappings", foundIdx, "valueMappings", action.timValue], action.mapValue, state.collections);
    return _extends({}, state, { collections: newCollections });
  }
  return state;
};

var toggleIgnoredColumn = function toggleIgnoredColumn(state, action) {
  var current = (0, _getIn2.default)([action.collection, "ignoredColumns"], state.collections);

  if (current.indexOf(action.variableName) < 0) {
    current.push(action.variableName);
  } else {
    current = current.filter(function (c) {
      return c !== action.variableName;
    });
  }

  return _extends({}, state, { collections: (0, _setIn2.default)([action.collection, "ignoredColumns"], current, state.collections) });
};

var addCustomProperty = function addCustomProperty(state, action) {
  var current = (0, _getIn2.default)([action.collection, "customProperties"], state.collections);
  var newCollections = (0, _setIn2.default)([action.collection, "customProperties", current.length], { name: action.propertyField, type: action.propertyType }, state.collections);

  return _extends({}, state, { collections: newCollections });
};

var removeCustomProperty = function removeCustomProperty(state, action) {
  var foundIdx = getMappingIndex(state, action);

  var current = (0, _getIn2.default)([action.collection, "customProperties"], state.collections).filter(function (cp) {
    return cp.name !== action.propertyField;
  });

  var newCollections = (0, _setIn2.default)([action.collection, "customProperties"], current, state.collections);

  if (foundIdx > -1) {
    var newMappings = (0, _getIn2.default)([action.collection, "mappings"], state.collections).filter(function (m, i) {
      return i !== foundIdx;
    });
    newCollections = (0, _setIn2.default)([action.collection, "mappings"], newMappings, newCollections);
  }

  return _extends({}, state, { collections: newCollections });
};

},{"../util/get-in":138,"../util/persist":140,"../util/set-in":141}],132:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
  var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
  var action = arguments[1];

  switch (action.type) {
    case "TOGGLE_MESSAGE":
      var newState = _extends({}, state);
      newState[action.messageId] = !state[action.messageId];
      return newState;
  }

  return state;
};

var initialState = {
  showFileIsUploadedMessage: true,
  showCollectionsAreConnectedMessage: true
};

},{}],133:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function () {
  var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
  var action = arguments[1];

  switch (action.type) {
    case "LOGIN":
      return {
        userId: action.data,
        myVres: action.vreData ? action.vreData.mine : {},
        vres: action.vreData ? action.vreData.public : {}
      };
  }

  return state;
};

var initialState = {
  userId: undefined,
  myVres: undefined,
  vres: {}
};

},{}],134:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.urls = undefined;
exports.navigateTo = navigateTo;

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactRouter = require("react-router");

var _reactRedux = require("react-redux");

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var _connectData = require("./components/react-redux-connect/connect-data");

var _connectData2 = _interopRequireDefault(_connectData);

var _page = require("./components/react-redux-connect/page");

var _page2 = _interopRequireDefault(_page);

var _firstUpload = require("./components/react-redux-connect/first-upload");

var _firstUpload2 = _interopRequireDefault(_firstUpload);

var _collectionOverview = require("./components/react-redux-connect/collection-overview");

var _collectionOverview2 = _interopRequireDefault(_collectionOverview);

var _connectToArchetype = require("./components/react-redux-connect/connect-to-archetype");

var _connectToArchetype2 = _interopRequireDefault(_connectToArchetype);

var _firstUpload3 = require("./components/firstUpload.jsx");

var _firstUpload4 = _interopRequireDefault(_firstUpload3);

var _page3 = require("./components/page.jsx");

var _page4 = _interopRequireDefault(_page3);

var _collectionOverview3 = require("./components/collection-overview");

var _collectionOverview4 = _interopRequireDefault(_collectionOverview3);

var _connectToArchetype3 = require("./components/connect-to-archetype");

var _connectToArchetype4 = _interopRequireDefault(_connectToArchetype3);

var _connectData3 = require("./components/connect-data");

var _connectData4 = _interopRequireDefault(_connectData3);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var urls = {
  mapData: function mapData() {
    return "/mapdata";
  },
  mapArchetypes: function mapArchetypes() {
    return "/maparchetypes";
  },
  collectionsOverview: function collectionsOverview() {
    return "/collections-overview";
  }
};

function navigateTo(key, args) {
  _reactRouter.hashHistory.push(urls[key].apply(null, args));
}

var connectComponent = function connectComponent(stateToProps) {
  return (0, _reactRedux.connect)(stateToProps, function (dispatch) {
    return (0, _actions2.default)(navigateTo, dispatch);
  });
};

var router = _react2.default.createElement(
  _reactRedux.Provider,
  { store: _store2.default },
  _react2.default.createElement(
    _reactRouter.Router,
    { history: _reactRouter.hashHistory },
    _react2.default.createElement(
      _reactRouter.Route,
      { path: "/", component: connectComponent(_page2.default)(_page4.default) },
      _react2.default.createElement(_reactRouter.IndexRoute, { component: connectComponent(_firstUpload2.default)(_firstUpload4.default) }),
      _react2.default.createElement(_reactRouter.Route, { path: urls.collectionsOverview(), components: connectComponent(_collectionOverview2.default)(_collectionOverview4.default) }),
      _react2.default.createElement(_reactRouter.Route, { path: urls.mapArchetypes(), components: connectComponent(_connectToArchetype2.default)(_connectToArchetype4.default) }),
      _react2.default.createElement(_reactRouter.Route, { path: urls.mapData(), components: connectComponent(_connectData2.default)(_connectData4.default) })
    )
  )
);

exports.default = router;
exports.urls = urls;

},{"./actions":99,"./components/collection-overview":102,"./components/connect-data":104,"./components/connect-to-archetype":105,"./components/firstUpload.jsx":109,"./components/page.jsx":112,"./components/react-redux-connect/collection-overview":119,"./components/react-redux-connect/connect-data":120,"./components/react-redux-connect/connect-to-archetype":121,"./components/react-redux-connect/first-upload":122,"./components/react-redux-connect/page":123,"./store":136,"react":"react","react-redux":36,"react-router":70}],135:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = setupMocks;
function setupMocks(xhrmock, orig) {
  xhrmock.get("http://test.repository.huygens.knaw.nl/v2.1/metadata/Admin", function (req, resp) {
    return resp.status(200).body("{\n          \"persons\": [\n            {\n              \"name\": \"name\",\n              \"type\": \"text\"\n            },\n            {\n              \"name\": \"hasWritten\",\n              \"type\": \"relation\",\n              \"quicksearch\": \"/v2.1/domain/documents/autocomplete\",\n              \"relation\": {\n                \"direction\": \"OUT\",\n                \"outName\": \"hasWritten\",\n                \"inName\": \"wasWrittenBy\",\n                \"targetCollection\": \"documents\",\n                \"relationCollection\": \"relations\",\n                \"relationTypeId\": \"bba10d37-86cc-4f1f-ba2d-016af2b21aa4\"\n              }\n            },\n            {\n              \"name\": \"isRelatedTo\",\n              \"type\": \"relation\",\n              \"quicksearch\": \"/v2.1/domain/persons/autocomplete\",\n              \"relation\": {\n                \"direction\": \"OUT\",\n                \"outName\": \"isRelatedTo\",\n                \"inName\": \"isRelatedTo\",\n                \"targetCollection\": \"persons\",\n                \"relationCollection\": \"relations\",\n                \"relationTypeId\": \"cba10d37-86cc-4f1f-ba2d-016af2b21aa5\"\n              }\n            }\n          ],\n          \"documents\": [\n            {\n              \"name\": \"name\",\n              \"type\": \"text\"\n            }\n          ]\n        }");
  }).get("http://test.repository.huygens.knaw.nl/v2.1/system/users/me/vres", function (req, resp) {
    console.log("fetch-my-vres");
    return resp.status(200).body("{\n          \"mine\": {\n            \"migrant_steekproef_masterdb (6).xlsx\": {\n              \"name\": \"migrant_steekproef_masterdb (6).xlsx\",\n              \"published\": true\n            },\n            \"testerdetest\": {\n              \"name\": \"testerdetest\",\n              \"published\": false,\n              \"rmlUri\": \"<<The get raw data url that the server provides>>\"\n            }\n          },\n          \"public\": {\n            \"WomenWriters\": {\n              \"name\": \"WomenWriters\"\n            }\n          }\n        }");
  }).post("http://test.repository.huygens.knaw.nl/v2.1/bulk-upload", function (req, resp) {
    console.log("bulk-upload");
    return resp.status(200).header("Location", "<<The get raw data url that the server provides>>");
  }).post("<<The save mapping url that the server provides>>", function (req, resp) {
    console.log("save mapping", req.body());
    return resp.status(204);
  }).post("<<The execute mapping url that the server provides>>", function (req, resp) {
    console.log("execute mapping with failures", req.body());
    return resp.status(200).body(JSON.stringify({
      success: false
    }));
  }).get("<<The get raw data url that the server provides>>", function (req, resp) {
    console.log("get raw data");
    return resp.status(200).body(JSON.stringify({
      vre: "thevrename",
      saveMapping: "<<The save mapping url that the server provides>>",
      executeMapping: "<<The execute mapping url that the server provides>>",
      collections: [{
        name: "mockpersons",
        variables: ["ID", "Voornaam", "tussenvoegsel", "Achternaam", "GeschrevenDocument", "Genoemd in", "Is getrouwd met"],
        data: "<<url for person data>>",
        dataWithErrors: "<<url for person data with errors>>"
      }, {
        name: "mockdocuments",
        variables: ["titel", "datum", "referentie", "url"],
        data: "<<url for document data>>",
        dataWithErrors: "<<url for document data with errors>>"
      }]
    }));
  }).get("<<url for person data>>", function (req, resp) {
    console.log("get person items data");
    return resp.status(200).body(JSON.stringify({
      "_next": "<<more data>>",
      "items": [{
        values: {
          "ID": "1",
          "Voornaam": "Voornaam",
          "tussenvoegsel": "tussenvoegsel",
          "Achternaam": "Achternaam",
          "GeschrevenDocument": "GeschrevenDocument",
          "Genoemd in": "Genoemd in",
          "Is getrouwd met": "Is getrouwd met"
        },
        errors: {}
      }, {
        values: {
          "ID": "2",
          "Voornaam": "Voornaam",
          "tussenvoegsel": "tussenvoegsel",
          "Achternaam": "Achternaam",
          "GeschrevenDocument": "GeschrevenDocument",
          "Genoemd in": "Genoemd in",
          "Is getrouwd met": "Is getrouwd met"
        },
        errors: {}
      }]
    }));
  }).get("<<url for person data with errors>>", function (req, resp) {
    console.log("get person items data with errors");
    return resp.status(200).body(JSON.stringify({
      "_next": "<<more data>>",
      "items": [{
        values: {
          "ID": "1",
          "Voornaam": "Voornaam",
          "tussenvoegsel": "tussenvoegsel",
          "Achternaam": "Achternaam",
          "GeschrevenDocument": "GeschrevenDocument",
          "Genoemd in": "Genoemd in",
          "Is getrouwd met": "Is getrouwd met"
        },
        errors: {
          "Voornaam": "will not do",
          "Achternaam": "also failed"
        }
      }]
    }));
  }).get("<<more data>>", function (req, resp) {
    console.log("get person items data");
    return resp.status(200).body(JSON.stringify({
      "items": [{
        values: {
          "ID": "3",
          "Voornaam": "Voornaam",
          "tussenvoegsel": "tussenvoegsel",
          "Achternaam": "Achternaam",
          "GeschrevenDocument": "GeschrevenDocument",
          "Genoemd in": "Genoemd in",
          "Is getrouwd met": "Is getrouwd met"
        },
        errors: {}
      }, {
        values: {
          "ID": "4",
          "Voornaam": "Voornaam",
          "tussenvoegsel": "tussenvoegsel",
          "Achternaam": "Achternaam",
          "GeschrevenDocument": "GeschrevenDocument",
          "Genoemd in": "Genoemd in",
          "Is getrouwd met": "Is getrouwd met"
        },
        errors: {}
      }]
    }));
  }).get("<<url for document data>>", function (req, resp) {
    console.log("get document items data");
    return resp.status(200).body(JSON.stringify({
      "name": "someCollection",
      "variables": ["tim_id", "var1", "var2"],
      "items": [{
        values: {
          "tim_id": "1",
          "titel": "titel",
          "datum": "datum",
          "referentie": "referentie",
          "url": "url"
        },
        errors: {}
      }, {
        values: {
          "tim_id": "2",
          "titel": "titel",
          "datum": "datum",
          "referentie": "referentie",
          "url": "url"
        },
        errors: {}
      }]
    }));
  }).get("<<url for document data with errors>>", function (req, resp) {
    console.log("get document items data with errors");
    return resp.status(200).body(JSON.stringify({
      "name": "someCollection",
      "variables": ["tim_id", "var1", "var2"],
      "items": []
    }));
  }).mock(function (req, resp) {
    console.error("unmocked request", req.url(), req, resp);
  });
}

},{}],136:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _redux = require("redux");

var _reduxThunk = require("redux-thunk");

var _reduxThunk2 = _interopRequireDefault(_reduxThunk);

var _persist = require("./util/persist");

var _reducers = require("./reducers");

var _reducers2 = _interopRequireDefault(_reducers);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var store = (0, _redux.createStore)((0, _redux.combineReducers)(_reducers2.default), (0, _redux.compose)((0, _redux.applyMiddleware)(_reduxThunk2.default), window.devToolsExtension ? window.devToolsExtension() : function (f) {
  return f;
}));

// window.onbeforeunload = () => persist(store.getState());

exports.default = store;

},{"./reducers":130,"./util/persist":140,"redux":85,"redux-thunk":79}],137:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol ? "symbol" : typeof obj; };

function deepClone9(obj) {
    var i, len, ret;

    if ((typeof obj === "undefined" ? "undefined" : _typeof(obj)) !== "object" || obj === null) {
        return obj;
    }

    if (Array.isArray(obj)) {
        ret = [];
        len = obj.length;
        for (i = 0; i < len; i++) {
            ret.push(_typeof(obj[i]) === "object" && obj[i] !== null ? deepClone9(obj[i]) : obj[i]);
        }
    } else {
        ret = {};
        for (i in obj) {
            if (obj.hasOwnProperty(i)) {
                ret[i] = _typeof(obj[i]) === "object" && obj[i] !== null ? deepClone9(obj[i]) : obj[i];
            }
        }
    }
    return ret;
}

exports.default = deepClone9;

},{}],138:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _cloneDeep = require("./clone-deep");

var _cloneDeep2 = _interopRequireDefault(_cloneDeep);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _getIn = function _getIn(path, data) {
	return data ? path.length === 0 ? data : _getIn(path, data[path.shift()]) : null;
};

var getIn = function getIn(path, data) {
	return _getIn((0, _cloneDeep2.default)(path), data);
};

exports.default = getIn;

},{"./clone-deep":137}],139:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = mappingToJsonLdRml;
function mappingToJsonLdRml(mapping, vre) {
  return {
    "@context": {
      "@vocab": "http://www.w3.org/ns/r2rml#",
      "rml": "http://semweb.mmlab.be/ns/rml#",
      "tim": "http://timbuctoo.com/mapping#",
      "http://www.w3.org/2000/01/rdf-schema#subClassOf": {
        "@type": "@id"
      },
      "predicate": {
        "@type": "@id"
      },
      "termType": {
        "@type": "@id"
      },
      "parentTriplesMap": {
        "@type": "@id"
      },
      "class": {
        "@type": "@id"
      }
    },
    "@graph": Object.keys(mapping.collections).map(function (key) {
      return mapSheet(key, mapping.collections[key], vre);
    })
  };
}

function makeMapName(vre, localName) {
  return "http://timbuctoo.com/mapping/" + vre + "/" + localName;
}

function mapSheet(key, sheet, vre) {
  //FIXME: move logicalSource and subjectMap under the control of the server
  return {
    "@id": makeMapName(vre, key),
    "http://www.w3.org/2000/01/rdf-schema#subClassOf": "http://timbuctoo.com/" + sheet.archetypeName.replace(/s$/, ""),
    "rml:logicalSource": {
      "rml:source": {
        "tim:rawCollection": key,
        "tim:vreName": vre
      }
    },
    "subjectMap": {
      "class": makeMapName(vre, key),
      "template": makeMapName(vre, key) + "/{tim_id}"
    },
    "predicateObjectMap": sheet.mappings.map(makePredicateObjectMap.bind(null, vre))
  };
}

function makePredicateObjectMap(vre, mapping) {
  var property = mapping.property;
  var variable = mapping.variable[0];
  if (variable.targetCollection) {
    return {
      "objectMap": {
        "joinCondition": {
          "child": variable.variableName,
          "parent": variable.targetVariableName
        },
        "parentTriplesMap": makeMapName(vre, variable.targetCollection)
      },
      "predicate": "http://timbuctoo.com/" + property
    };
  } else if (variable.targetExistingTimbuctooVre) {
    return {
      "objectMap": {
        "column": variable.variableName,
        "termType": "http://www.w3.org/ns/r2rml#IRI"
      },
      "predicate": "http://timbuctoo.com/" + property,
      "http://timbuctoo.com/mapping/existingTimbuctooVre": variable.targetExistingTimbuctooVre
    };
  } else {
    return {
      "objectMap": {
        "column": variable.variableName
      },
      "predicate": "http://timbuctoo.com/" + property
    };
  }
}

},{}],140:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
var persistDisabled = false;

var persist = function persist(state) {
	if (persistDisabled) {
		return;
	}
	for (var key in state) {
		localStorage.setItem(key, JSON.stringify(state[key]));
	}
};

var getItem = function getItem(key) {
	if (localStorage.getItem(key)) {
		return JSON.parse(localStorage.getItem(key));
	}
	return null;
};

var disablePersist = function disablePersist() {
	localStorage.clear();
	persistDisabled = true;
};
window.disablePersist = disablePersist;

exports.persist = persist;
exports.getItem = getItem;
exports.disablePersist = disablePersist;

},{}],141:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _cloneDeep = require("./clone-deep");

var _cloneDeep2 = _interopRequireDefault(_cloneDeep);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// Do either of these:
//  a) Set a value by reference if deref is not null
//  b) Set a value directly in to data object if deref is null
var setEither = function setEither(data, deref, key, val) {
	(deref || data)[key] = val;
	return data;
};

// Set a nested value in data (not unlike immutablejs, but a clone of data is expected for proper immutability)
var _setIn = function _setIn(path, value, data) {
	var deref = arguments.length <= 3 || arguments[3] === undefined ? null : arguments[3];
	return path.length > 1 ? _setIn(path, value, data, deref ? deref[path.shift()] : data[path.shift()]) : setEither(data, deref, path[0], value);
};

var setIn = function setIn(path, value, data) {
	return _setIn((0, _cloneDeep2.default)(path), value, (0, _cloneDeep2.default)(data));
};

exports.default = setIn;

},{"./clone-deep":137}]},{},[127])(127)
});
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvcHJvY2Vzcy9icm93c2VyLmpzIiwibm9kZV9tb2R1bGVzL2NsYXNzbmFtZXMvaW5kZXguanMiLCJub2RlX21vZHVsZXMvZGVlcC1lcXVhbC9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9kZWVwLWVxdWFsL2xpYi9pc19hcmd1bWVudHMuanMiLCJub2RlX21vZHVsZXMvZGVlcC1lcXVhbC9saWIva2V5cy5qcyIsIm5vZGVfbW9kdWxlcy9mb3ItZWFjaC9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9nbG9iYWwvd2luZG93LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL0FjdGlvbnMuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvQXN5bmNVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9ET01TdGF0ZVN0b3JhZ2UuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvRE9NVXRpbHMuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvRXhlY3V0aW9uRW52aXJvbm1lbnQuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvUGF0aFV0aWxzLmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2NyZWF0ZUJyb3dzZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2NyZWF0ZURPTUhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvY3JlYXRlSGFzaEhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvY3JlYXRlSGlzdG9yeS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9jcmVhdGVMb2NhdGlvbi5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9jcmVhdGVNZW1vcnlIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2RlcHJlY2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9ydW5UcmFuc2l0aW9uSG9vay5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi91c2VCYXNlbmFtZS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi91c2VRdWVyaWVzLmpzIiwibm9kZV9tb2R1bGVzL2hpc3Rvcnkvbm9kZV9tb2R1bGVzL3dhcm5pbmcvYnJvd3Nlci5qcyIsIm5vZGVfbW9kdWxlcy9ob2lzdC1ub24tcmVhY3Qtc3RhdGljcy9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9pbnZhcmlhbnQvYnJvd3Nlci5qcyIsIm5vZGVfbW9kdWxlcy9pcy1mdW5jdGlvbi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX2dldFByb3RvdHlwZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX292ZXJBcmcuanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL2lzT2JqZWN0TGlrZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvaXNQbGFpbk9iamVjdC5qcyIsIm5vZGVfbW9kdWxlcy9wYXJzZS1oZWFkZXJzL3BhcnNlLWhlYWRlcnMuanMiLCJub2RlX21vZHVsZXMvcXVlcnktc3RyaW5nL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi9jb21wb25lbnRzL1Byb3ZpZGVyLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi9jb21wb25lbnRzL2Nvbm5lY3QuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi91dGlscy9zaGFsbG93RXF1YWwuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3N0b3JlU2hhcGUuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3dhcm5pbmcuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3dyYXBBY3Rpb25DcmVhdG9ycy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0FzeW5jVXRpbHMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9IaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvSW5kZXhMaW5rLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvSW5kZXhSZWRpcmVjdC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0luZGV4Um91dGUuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9JbnRlcm5hbFByb3BUeXBlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0xpZmVjeWNsZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0xpbmsuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9QYXR0ZXJuVXRpbHMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9Qcm9wVHlwZXMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9SZWRpcmVjdC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvUm91dGVDb250ZXh0LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvUm91dGVVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlci5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlckNvbnRleHQuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9Sb3V0ZXJVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRpbmdDb250ZXh0LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvVHJhbnNpdGlvblV0aWxzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvYXBwbHlSb3V0ZXJNaWRkbGV3YXJlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvYnJvd3Nlckhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9jb21wdXRlQ2hhbmdlZFJvdXRlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2NyZWF0ZU1lbW9yeUhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9jcmVhdGVSb3V0ZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvZ2V0Q29tcG9uZW50cy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2dldFJvdXRlUGFyYW1zLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvaGFzaEhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2lzQWN0aXZlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvbWFrZVN0YXRlV2l0aExvY2F0aW9uLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvbWF0Y2guanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9tYXRjaFJvdXRlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL3JvdXRlcldhcm5pbmcuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi91c2VSb3V0ZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvdXNlUm91dGVzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvd2l0aFJvdXRlci5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC10aHVuay9saWIvaW5kZXguanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2FwcGx5TWlkZGxld2FyZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvYmluZEFjdGlvbkNyZWF0b3JzLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9jb21iaW5lUmVkdWNlcnMuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NvbXBvc2UuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NyZWF0ZVN0b3JlLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvdXRpbHMvd2FybmluZy5qcyIsIm5vZGVfbW9kdWxlcy9zdHJpY3QtdXJpLWVuY29kZS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9saWIvaW5kZXguanMiLCJub2RlX21vZHVsZXMvc3ltYm9sLW9ic2VydmFibGUvbGliL3BvbnlmaWxsLmpzIiwibm9kZV9tb2R1bGVzL3RyaW0vaW5kZXguanMiLCJub2RlX21vZHVsZXMvd2FybmluZy9icm93c2VyLmpzIiwibm9kZV9tb2R1bGVzL3hoci1tb2NrL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3hoci1tb2NrL2xpYi9Nb2NrUmVxdWVzdC5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9saWIvTW9ja1Jlc3BvbnNlLmpzIiwibm9kZV9tb2R1bGVzL3hoci1tb2NrL2xpYi9Nb2NrWE1MSHR0cFJlcXVlc3QuanMiLCJub2RlX21vZHVsZXMveGhyL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3h0ZW5kL2ltbXV0YWJsZS5qcyIsInNyYy9hY3Rpb25zLmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1mb3JtLmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1pbmRleC5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tb3ZlcnZpZXcuanMiLCJzcmMvY29tcG9uZW50cy9jb2xsZWN0aW9uLXRhYmxlLmpzIiwic3JjL2NvbXBvbmVudHMvY29ubmVjdC1kYXRhLmpzIiwic3JjL2NvbXBvbmVudHMvY29ubmVjdC10by1hcmNoZXR5cGUuanMiLCJzcmMvY29tcG9uZW50cy9kYXRhc2V0LWNhcmRzLmpzIiwic3JjL2NvbXBvbmVudHMvZGF0YXNldENhcmQuanN4Iiwic3JjL2NvbXBvbmVudHMvZmllbGRzL3NlbGVjdC1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2ZpcnN0VXBsb2FkLmpzeCIsInNyYy9jb21wb25lbnRzL2Zvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL21lc3NhZ2UuanMiLCJzcmMvY29tcG9uZW50cy9wYWdlLmpzeCIsInNyYy9jb21wb25lbnRzL3Byb3BlcnR5LWZvcm0vYWRkLXByb3BlcnR5LmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9jb2x1bW4tc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9wcm9wZXJ0eS1mb3JtLmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9yZWxhdGlvbi10by1leGlzdGluZy5qcyIsInNyYy9jb21wb25lbnRzL3Byb3BlcnR5LWZvcm0vcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL3RleHQuanMiLCJzcmMvY29tcG9uZW50cy9yZWFjdC1yZWR1eC1jb25uZWN0L2NvbGxlY3Rpb24tb3ZlcnZpZXcuanMiLCJzcmMvY29tcG9uZW50cy9yZWFjdC1yZWR1eC1jb25uZWN0L2Nvbm5lY3QtZGF0YS5qcyIsInNyYy9jb21wb25lbnRzL3JlYWN0LXJlZHV4LWNvbm5lY3QvY29ubmVjdC10by1hcmNoZXR5cGUuanMiLCJzcmMvY29tcG9uZW50cy9yZWFjdC1yZWR1eC1jb25uZWN0L2ZpcnN0LXVwbG9hZC5qcyIsInNyYy9jb21wb25lbnRzL3JlYWN0LXJlZHV4LWNvbm5lY3QvcGFnZS5qcyIsInNyYy9jb21wb25lbnRzL3RhYmxlL2RhdGEtcm93LmpzIiwic3JjL2NvbXBvbmVudHMvdGFibGUvaGVhZGVyLWNlbGwuanMiLCJzcmMvY29tcG9uZW50cy91cGxvYWQtYnV0dG9uLmpzIiwic3JjL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL2FyY2hldHlwZS5qcyIsInNyYy9yZWR1Y2Vycy9pbXBvcnQtZGF0YS5qcyIsInNyYy9yZWR1Y2Vycy9pbmRleC5qcyIsInNyYy9yZWR1Y2Vycy9tYXBwaW5ncy5qcyIsInNyYy9yZWR1Y2Vycy9tZXNzYWdlcy5qcyIsInNyYy9yZWR1Y2Vycy91c2VyZGF0YS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc2VydmVybW9ja3MuanMiLCJzcmMvc3RvcmUuanMiLCJzcmMvdXRpbC9jbG9uZS1kZWVwLmpzIiwic3JjL3V0aWwvZ2V0LWluLmpzIiwic3JjL3V0aWwvbWFwcGluZ1RvSnNvbkxkUm1sLmpzIiwic3JjL3V0aWwvcGVyc2lzdC5qcyIsInNyYy91dGlsL3NldC1pbi5qcyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTtBQ0FBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3BMQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNoREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM5RkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3BCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNUQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUM5Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUNUQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM5QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ3pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ3hFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDMUVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDOUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNuTEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDdkNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNyUEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQy9SQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ3pKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNsQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ3ZCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQzdKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQy9LQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzVEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ2xEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ25EQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNOQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDN0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwRUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUNsRUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQzdFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUN4WUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNoQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN6QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNWQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdkJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNYQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDdkZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUM1QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQzNCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDOURBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzNEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUMvQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNuRUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUMvS0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNuTkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNwR0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ3JHQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDeERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQzVGQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQy9OQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDM0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDL0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUM3QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUN6SEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDeERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDNUVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUNsQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUM3U0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDMUVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3pCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDM0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ3ZKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNoREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNsRkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzFQQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbkNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ3RCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDL0RBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDYkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDekRBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDN0lBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDckNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDcFFBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDN0NBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3hCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNOQTtBQUNBOzs7QUNEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ3JCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3RCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ2RBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDNURBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVHQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDN0VBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDeEZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL05BO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7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QUEsUUFDYixPQUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIi8vIHNoaW0gZm9yIHVzaW5nIHByb2Nlc3MgaW4gYnJvd3NlclxudmFyIHByb2Nlc3MgPSBtb2R1bGUuZXhwb3J0cyA9IHt9O1xuXG4vLyBjYWNoZWQgZnJvbSB3aGF0ZXZlciBnbG9iYWwgaXMgcHJlc2VudCBzbyB0aGF0IHRlc3QgcnVubmVycyB0aGF0IHN0dWIgaXRcbi8vIGRvbid0IGJyZWFrIHRoaW5ncy4gIEJ1dCB3ZSBuZWVkIHRvIHdyYXAgaXQgaW4gYSB0cnkgY2F0Y2ggaW4gY2FzZSBpdCBpc1xuLy8gd3JhcHBlZCBpbiBzdHJpY3QgbW9kZSBjb2RlIHdoaWNoIGRvZXNuJ3QgZGVmaW5lIGFueSBnbG9iYWxzLiAgSXQncyBpbnNpZGUgYVxuLy8gZnVuY3Rpb24gYmVjYXVzZSB0cnkvY2F0Y2hlcyBkZW9wdGltaXplIGluIGNlcnRhaW4gZW5naW5lcy5cblxudmFyIGNhY2hlZFNldFRpbWVvdXQ7XG52YXIgY2FjaGVkQ2xlYXJUaW1lb3V0O1xuXG5mdW5jdGlvbiBkZWZhdWx0U2V0VGltb3V0KCkge1xuICAgIHRocm93IG5ldyBFcnJvcignc2V0VGltZW91dCBoYXMgbm90IGJlZW4gZGVmaW5lZCcpO1xufVxuZnVuY3Rpb24gZGVmYXVsdENsZWFyVGltZW91dCAoKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdjbGVhclRpbWVvdXQgaGFzIG5vdCBiZWVuIGRlZmluZWQnKTtcbn1cbihmdW5jdGlvbiAoKSB7XG4gICAgdHJ5IHtcbiAgICAgICAgaWYgKHR5cGVvZiBzZXRUaW1lb3V0ID09PSAnZnVuY3Rpb24nKSB7XG4gICAgICAgICAgICBjYWNoZWRTZXRUaW1lb3V0ID0gc2V0VGltZW91dDtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGNhY2hlZFNldFRpbWVvdXQgPSBkZWZhdWx0U2V0VGltb3V0O1xuICAgICAgICB9XG4gICAgfSBjYXRjaCAoZSkge1xuICAgICAgICBjYWNoZWRTZXRUaW1lb3V0ID0gZGVmYXVsdFNldFRpbW91dDtcbiAgICB9XG4gICAgdHJ5IHtcbiAgICAgICAgaWYgKHR5cGVvZiBjbGVhclRpbWVvdXQgPT09ICdmdW5jdGlvbicpIHtcbiAgICAgICAgICAgIGNhY2hlZENsZWFyVGltZW91dCA9IGNsZWFyVGltZW91dDtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGNhY2hlZENsZWFyVGltZW91dCA9IGRlZmF1bHRDbGVhclRpbWVvdXQ7XG4gICAgICAgIH1cbiAgICB9IGNhdGNoIChlKSB7XG4gICAgICAgIGNhY2hlZENsZWFyVGltZW91dCA9IGRlZmF1bHRDbGVhclRpbWVvdXQ7XG4gICAgfVxufSAoKSlcbmZ1bmN0aW9uIHJ1blRpbWVvdXQoZnVuKSB7XG4gICAgaWYgKGNhY2hlZFNldFRpbWVvdXQgPT09IHNldFRpbWVvdXQpIHtcbiAgICAgICAgLy9ub3JtYWwgZW52aXJvbWVudHMgaW4gc2FuZSBzaXR1YXRpb25zXG4gICAgICAgIHJldHVybiBzZXRUaW1lb3V0KGZ1biwgMCk7XG4gICAgfVxuICAgIC8vIGlmIHNldFRpbWVvdXQgd2Fzbid0IGF2YWlsYWJsZSBidXQgd2FzIGxhdHRlciBkZWZpbmVkXG4gICAgaWYgKChjYWNoZWRTZXRUaW1lb3V0ID09PSBkZWZhdWx0U2V0VGltb3V0IHx8ICFjYWNoZWRTZXRUaW1lb3V0KSAmJiBzZXRUaW1lb3V0KSB7XG4gICAgICAgIGNhY2hlZFNldFRpbWVvdXQgPSBzZXRUaW1lb3V0O1xuICAgICAgICByZXR1cm4gc2V0VGltZW91dChmdW4sIDApO1xuICAgIH1cbiAgICB0cnkge1xuICAgICAgICAvLyB3aGVuIHdoZW4gc29tZWJvZHkgaGFzIHNjcmV3ZWQgd2l0aCBzZXRUaW1lb3V0IGJ1dCBubyBJLkUuIG1hZGRuZXNzXG4gICAgICAgIHJldHVybiBjYWNoZWRTZXRUaW1lb3V0KGZ1biwgMCk7XG4gICAgfSBjYXRjaChlKXtcbiAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgIC8vIFdoZW4gd2UgYXJlIGluIEkuRS4gYnV0IHRoZSBzY3JpcHQgaGFzIGJlZW4gZXZhbGVkIHNvIEkuRS4gZG9lc24ndCB0cnVzdCB0aGUgZ2xvYmFsIG9iamVjdCB3aGVuIGNhbGxlZCBub3JtYWxseVxuICAgICAgICAgICAgcmV0dXJuIGNhY2hlZFNldFRpbWVvdXQuY2FsbChudWxsLCBmdW4sIDApO1xuICAgICAgICB9IGNhdGNoKGUpe1xuICAgICAgICAgICAgLy8gc2FtZSBhcyBhYm92ZSBidXQgd2hlbiBpdCdzIGEgdmVyc2lvbiBvZiBJLkUuIHRoYXQgbXVzdCBoYXZlIHRoZSBnbG9iYWwgb2JqZWN0IGZvciAndGhpcycsIGhvcGZ1bGx5IG91ciBjb250ZXh0IGNvcnJlY3Qgb3RoZXJ3aXNlIGl0IHdpbGwgdGhyb3cgYSBnbG9iYWwgZXJyb3JcbiAgICAgICAgICAgIHJldHVybiBjYWNoZWRTZXRUaW1lb3V0LmNhbGwodGhpcywgZnVuLCAwKTtcbiAgICAgICAgfVxuICAgIH1cblxuXG59XG5mdW5jdGlvbiBydW5DbGVhclRpbWVvdXQobWFya2VyKSB7XG4gICAgaWYgKGNhY2hlZENsZWFyVGltZW91dCA9PT0gY2xlYXJUaW1lb3V0KSB7XG4gICAgICAgIC8vbm9ybWFsIGVudmlyb21lbnRzIGluIHNhbmUgc2l0dWF0aW9uc1xuICAgICAgICByZXR1cm4gY2xlYXJUaW1lb3V0KG1hcmtlcik7XG4gICAgfVxuICAgIC8vIGlmIGNsZWFyVGltZW91dCB3YXNuJ3QgYXZhaWxhYmxlIGJ1dCB3YXMgbGF0dGVyIGRlZmluZWRcbiAgICBpZiAoKGNhY2hlZENsZWFyVGltZW91dCA9PT0gZGVmYXVsdENsZWFyVGltZW91dCB8fCAhY2FjaGVkQ2xlYXJUaW1lb3V0KSAmJiBjbGVhclRpbWVvdXQpIHtcbiAgICAgICAgY2FjaGVkQ2xlYXJUaW1lb3V0ID0gY2xlYXJUaW1lb3V0O1xuICAgICAgICByZXR1cm4gY2xlYXJUaW1lb3V0KG1hcmtlcik7XG4gICAgfVxuICAgIHRyeSB7XG4gICAgICAgIC8vIHdoZW4gd2hlbiBzb21lYm9keSBoYXMgc2NyZXdlZCB3aXRoIHNldFRpbWVvdXQgYnV0IG5vIEkuRS4gbWFkZG5lc3NcbiAgICAgICAgcmV0dXJuIGNhY2hlZENsZWFyVGltZW91dChtYXJrZXIpO1xuICAgIH0gY2F0Y2ggKGUpe1xuICAgICAgICB0cnkge1xuICAgICAgICAgICAgLy8gV2hlbiB3ZSBhcmUgaW4gSS5FLiBidXQgdGhlIHNjcmlwdCBoYXMgYmVlbiBldmFsZWQgc28gSS5FLiBkb2Vzbid0ICB0cnVzdCB0aGUgZ2xvYmFsIG9iamVjdCB3aGVuIGNhbGxlZCBub3JtYWxseVxuICAgICAgICAgICAgcmV0dXJuIGNhY2hlZENsZWFyVGltZW91dC5jYWxsKG51bGwsIG1hcmtlcik7XG4gICAgICAgIH0gY2F0Y2ggKGUpe1xuICAgICAgICAgICAgLy8gc2FtZSBhcyBhYm92ZSBidXQgd2hlbiBpdCdzIGEgdmVyc2lvbiBvZiBJLkUuIHRoYXQgbXVzdCBoYXZlIHRoZSBnbG9iYWwgb2JqZWN0IGZvciAndGhpcycsIGhvcGZ1bGx5IG91ciBjb250ZXh0IGNvcnJlY3Qgb3RoZXJ3aXNlIGl0IHdpbGwgdGhyb3cgYSBnbG9iYWwgZXJyb3IuXG4gICAgICAgICAgICAvLyBTb21lIHZlcnNpb25zIG9mIEkuRS4gaGF2ZSBkaWZmZXJlbnQgcnVsZXMgZm9yIGNsZWFyVGltZW91dCB2cyBzZXRUaW1lb3V0XG4gICAgICAgICAgICByZXR1cm4gY2FjaGVkQ2xlYXJUaW1lb3V0LmNhbGwodGhpcywgbWFya2VyKTtcbiAgICAgICAgfVxuICAgIH1cblxuXG5cbn1cbnZhciBxdWV1ZSA9IFtdO1xudmFyIGRyYWluaW5nID0gZmFsc2U7XG52YXIgY3VycmVudFF1ZXVlO1xudmFyIHF1ZXVlSW5kZXggPSAtMTtcblxuZnVuY3Rpb24gY2xlYW5VcE5leHRUaWNrKCkge1xuICAgIGlmICghZHJhaW5pbmcgfHwgIWN1cnJlbnRRdWV1ZSkge1xuICAgICAgICByZXR1cm47XG4gICAgfVxuICAgIGRyYWluaW5nID0gZmFsc2U7XG4gICAgaWYgKGN1cnJlbnRRdWV1ZS5sZW5ndGgpIHtcbiAgICAgICAgcXVldWUgPSBjdXJyZW50UXVldWUuY29uY2F0KHF1ZXVlKTtcbiAgICB9IGVsc2Uge1xuICAgICAgICBxdWV1ZUluZGV4ID0gLTE7XG4gICAgfVxuICAgIGlmIChxdWV1ZS5sZW5ndGgpIHtcbiAgICAgICAgZHJhaW5RdWV1ZSgpO1xuICAgIH1cbn1cblxuZnVuY3Rpb24gZHJhaW5RdWV1ZSgpIHtcbiAgICBpZiAoZHJhaW5pbmcpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgIH1cbiAgICB2YXIgdGltZW91dCA9IHJ1blRpbWVvdXQoY2xlYW5VcE5leHRUaWNrKTtcbiAgICBkcmFpbmluZyA9IHRydWU7XG5cbiAgICB2YXIgbGVuID0gcXVldWUubGVuZ3RoO1xuICAgIHdoaWxlKGxlbikge1xuICAgICAgICBjdXJyZW50UXVldWUgPSBxdWV1ZTtcbiAgICAgICAgcXVldWUgPSBbXTtcbiAgICAgICAgd2hpbGUgKCsrcXVldWVJbmRleCA8IGxlbikge1xuICAgICAgICAgICAgaWYgKGN1cnJlbnRRdWV1ZSkge1xuICAgICAgICAgICAgICAgIGN1cnJlbnRRdWV1ZVtxdWV1ZUluZGV4XS5ydW4oKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgICBxdWV1ZUluZGV4ID0gLTE7XG4gICAgICAgIGxlbiA9IHF1ZXVlLmxlbmd0aDtcbiAgICB9XG4gICAgY3VycmVudFF1ZXVlID0gbnVsbDtcbiAgICBkcmFpbmluZyA9IGZhbHNlO1xuICAgIHJ1bkNsZWFyVGltZW91dCh0aW1lb3V0KTtcbn1cblxucHJvY2Vzcy5uZXh0VGljayA9IGZ1bmN0aW9uIChmdW4pIHtcbiAgICB2YXIgYXJncyA9IG5ldyBBcnJheShhcmd1bWVudHMubGVuZ3RoIC0gMSk7XG4gICAgaWYgKGFyZ3VtZW50cy5sZW5ndGggPiAxKSB7XG4gICAgICAgIGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7XG4gICAgICAgICAgICBhcmdzW2kgLSAxXSA9IGFyZ3VtZW50c1tpXTtcbiAgICAgICAgfVxuICAgIH1cbiAgICBxdWV1ZS5wdXNoKG5ldyBJdGVtKGZ1biwgYXJncykpO1xuICAgIGlmIChxdWV1ZS5sZW5ndGggPT09IDEgJiYgIWRyYWluaW5nKSB7XG4gICAgICAgIHJ1blRpbWVvdXQoZHJhaW5RdWV1ZSk7XG4gICAgfVxufTtcblxuLy8gdjggbGlrZXMgcHJlZGljdGlibGUgb2JqZWN0c1xuZnVuY3Rpb24gSXRlbShmdW4sIGFycmF5KSB7XG4gICAgdGhpcy5mdW4gPSBmdW47XG4gICAgdGhpcy5hcnJheSA9IGFycmF5O1xufVxuSXRlbS5wcm90b3R5cGUucnVuID0gZnVuY3Rpb24gKCkge1xuICAgIHRoaXMuZnVuLmFwcGx5KG51bGwsIHRoaXMuYXJyYXkpO1xufTtcbnByb2Nlc3MudGl0bGUgPSAnYnJvd3Nlcic7XG5wcm9jZXNzLmJyb3dzZXIgPSB0cnVlO1xucHJvY2Vzcy5lbnYgPSB7fTtcbnByb2Nlc3MuYXJndiA9IFtdO1xucHJvY2Vzcy52ZXJzaW9uID0gJyc7IC8vIGVtcHR5IHN0cmluZyB0byBhdm9pZCByZWdleHAgaXNzdWVzXG5wcm9jZXNzLnZlcnNpb25zID0ge307XG5cbmZ1bmN0aW9uIG5vb3AoKSB7fVxuXG5wcm9jZXNzLm9uID0gbm9vcDtcbnByb2Nlc3MuYWRkTGlzdGVuZXIgPSBub29wO1xucHJvY2Vzcy5vbmNlID0gbm9vcDtcbnByb2Nlc3Mub2ZmID0gbm9vcDtcbnByb2Nlc3MucmVtb3ZlTGlzdGVuZXIgPSBub29wO1xucHJvY2Vzcy5yZW1vdmVBbGxMaXN0ZW5lcnMgPSBub29wO1xucHJvY2Vzcy5lbWl0ID0gbm9vcDtcblxucHJvY2Vzcy5iaW5kaW5nID0gZnVuY3Rpb24gKG5hbWUpIHtcbiAgICB0aHJvdyBuZXcgRXJyb3IoJ3Byb2Nlc3MuYmluZGluZyBpcyBub3Qgc3VwcG9ydGVkJyk7XG59O1xuXG5wcm9jZXNzLmN3ZCA9IGZ1bmN0aW9uICgpIHsgcmV0dXJuICcvJyB9O1xucHJvY2Vzcy5jaGRpciA9IGZ1bmN0aW9uIChkaXIpIHtcbiAgICB0aHJvdyBuZXcgRXJyb3IoJ3Byb2Nlc3MuY2hkaXIgaXMgbm90IHN1cHBvcnRlZCcpO1xufTtcbnByb2Nlc3MudW1hc2sgPSBmdW5jdGlvbigpIHsgcmV0dXJuIDA7IH07XG4iLCIvKiFcbiAgQ29weXJpZ2h0IChjKSAyMDE2IEplZCBXYXRzb24uXG4gIExpY2Vuc2VkIHVuZGVyIHRoZSBNSVQgTGljZW5zZSAoTUlUKSwgc2VlXG4gIGh0dHA6Ly9qZWR3YXRzb24uZ2l0aHViLmlvL2NsYXNzbmFtZXNcbiovXG4vKiBnbG9iYWwgZGVmaW5lICovXG5cbihmdW5jdGlvbiAoKSB7XG5cdCd1c2Ugc3RyaWN0JztcblxuXHR2YXIgaGFzT3duID0ge30uaGFzT3duUHJvcGVydHk7XG5cblx0ZnVuY3Rpb24gY2xhc3NOYW1lcyAoKSB7XG5cdFx0dmFyIGNsYXNzZXMgPSBbXTtcblxuXHRcdGZvciAodmFyIGkgPSAwOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7XG5cdFx0XHR2YXIgYXJnID0gYXJndW1lbnRzW2ldO1xuXHRcdFx0aWYgKCFhcmcpIGNvbnRpbnVlO1xuXG5cdFx0XHR2YXIgYXJnVHlwZSA9IHR5cGVvZiBhcmc7XG5cblx0XHRcdGlmIChhcmdUeXBlID09PSAnc3RyaW5nJyB8fCBhcmdUeXBlID09PSAnbnVtYmVyJykge1xuXHRcdFx0XHRjbGFzc2VzLnB1c2goYXJnKTtcblx0XHRcdH0gZWxzZSBpZiAoQXJyYXkuaXNBcnJheShhcmcpKSB7XG5cdFx0XHRcdGNsYXNzZXMucHVzaChjbGFzc05hbWVzLmFwcGx5KG51bGwsIGFyZykpO1xuXHRcdFx0fSBlbHNlIGlmIChhcmdUeXBlID09PSAnb2JqZWN0Jykge1xuXHRcdFx0XHRmb3IgKHZhciBrZXkgaW4gYXJnKSB7XG5cdFx0XHRcdFx0aWYgKGhhc093bi5jYWxsKGFyZywga2V5KSAmJiBhcmdba2V5XSkge1xuXHRcdFx0XHRcdFx0Y2xhc3Nlcy5wdXNoKGtleSk7XG5cdFx0XHRcdFx0fVxuXHRcdFx0XHR9XG5cdFx0XHR9XG5cdFx0fVxuXG5cdFx0cmV0dXJuIGNsYXNzZXMuam9pbignICcpO1xuXHR9XG5cblx0aWYgKHR5cGVvZiBtb2R1bGUgIT09ICd1bmRlZmluZWQnICYmIG1vZHVsZS5leHBvcnRzKSB7XG5cdFx0bW9kdWxlLmV4cG9ydHMgPSBjbGFzc05hbWVzO1xuXHR9IGVsc2UgaWYgKHR5cGVvZiBkZWZpbmUgPT09ICdmdW5jdGlvbicgJiYgdHlwZW9mIGRlZmluZS5hbWQgPT09ICdvYmplY3QnICYmIGRlZmluZS5hbWQpIHtcblx0XHQvLyByZWdpc3RlciBhcyAnY2xhc3NuYW1lcycsIGNvbnNpc3RlbnQgd2l0aCBucG0gcGFja2FnZSBuYW1lXG5cdFx0ZGVmaW5lKCdjbGFzc25hbWVzJywgW10sIGZ1bmN0aW9uICgpIHtcblx0XHRcdHJldHVybiBjbGFzc05hbWVzO1xuXHRcdH0pO1xuXHR9IGVsc2Uge1xuXHRcdHdpbmRvdy5jbGFzc05hbWVzID0gY2xhc3NOYW1lcztcblx0fVxufSgpKTtcbiIsInZhciBwU2xpY2UgPSBBcnJheS5wcm90b3R5cGUuc2xpY2U7XG52YXIgb2JqZWN0S2V5cyA9IHJlcXVpcmUoJy4vbGliL2tleXMuanMnKTtcbnZhciBpc0FyZ3VtZW50cyA9IHJlcXVpcmUoJy4vbGliL2lzX2FyZ3VtZW50cy5qcycpO1xuXG52YXIgZGVlcEVxdWFsID0gbW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiAoYWN0dWFsLCBleHBlY3RlZCwgb3B0cykge1xuICBpZiAoIW9wdHMpIG9wdHMgPSB7fTtcbiAgLy8gNy4xLiBBbGwgaWRlbnRpY2FsIHZhbHVlcyBhcmUgZXF1aXZhbGVudCwgYXMgZGV0ZXJtaW5lZCBieSA9PT0uXG4gIGlmIChhY3R1YWwgPT09IGV4cGVjdGVkKSB7XG4gICAgcmV0dXJuIHRydWU7XG5cbiAgfSBlbHNlIGlmIChhY3R1YWwgaW5zdGFuY2VvZiBEYXRlICYmIGV4cGVjdGVkIGluc3RhbmNlb2YgRGF0ZSkge1xuICAgIHJldHVybiBhY3R1YWwuZ2V0VGltZSgpID09PSBleHBlY3RlZC5nZXRUaW1lKCk7XG5cbiAgLy8gNy4zLiBPdGhlciBwYWlycyB0aGF0IGRvIG5vdCBib3RoIHBhc3MgdHlwZW9mIHZhbHVlID09ICdvYmplY3QnLFxuICAvLyBlcXVpdmFsZW5jZSBpcyBkZXRlcm1pbmVkIGJ5ID09LlxuICB9IGVsc2UgaWYgKCFhY3R1YWwgfHwgIWV4cGVjdGVkIHx8IHR5cGVvZiBhY3R1YWwgIT0gJ29iamVjdCcgJiYgdHlwZW9mIGV4cGVjdGVkICE9ICdvYmplY3QnKSB7XG4gICAgcmV0dXJuIG9wdHMuc3RyaWN0ID8gYWN0dWFsID09PSBleHBlY3RlZCA6IGFjdHVhbCA9PSBleHBlY3RlZDtcblxuICAvLyA3LjQuIEZvciBhbGwgb3RoZXIgT2JqZWN0IHBhaXJzLCBpbmNsdWRpbmcgQXJyYXkgb2JqZWN0cywgZXF1aXZhbGVuY2UgaXNcbiAgLy8gZGV0ZXJtaW5lZCBieSBoYXZpbmcgdGhlIHNhbWUgbnVtYmVyIG9mIG93bmVkIHByb3BlcnRpZXMgKGFzIHZlcmlmaWVkXG4gIC8vIHdpdGggT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKSwgdGhlIHNhbWUgc2V0IG9mIGtleXNcbiAgLy8gKGFsdGhvdWdoIG5vdCBuZWNlc3NhcmlseSB0aGUgc2FtZSBvcmRlciksIGVxdWl2YWxlbnQgdmFsdWVzIGZvciBldmVyeVxuICAvLyBjb3JyZXNwb25kaW5nIGtleSwgYW5kIGFuIGlkZW50aWNhbCAncHJvdG90eXBlJyBwcm9wZXJ0eS4gTm90ZTogdGhpc1xuICAvLyBhY2NvdW50cyBmb3IgYm90aCBuYW1lZCBhbmQgaW5kZXhlZCBwcm9wZXJ0aWVzIG9uIEFycmF5cy5cbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gb2JqRXF1aXYoYWN0dWFsLCBleHBlY3RlZCwgb3B0cyk7XG4gIH1cbn1cblxuZnVuY3Rpb24gaXNVbmRlZmluZWRPck51bGwodmFsdWUpIHtcbiAgcmV0dXJuIHZhbHVlID09PSBudWxsIHx8IHZhbHVlID09PSB1bmRlZmluZWQ7XG59XG5cbmZ1bmN0aW9uIGlzQnVmZmVyICh4KSB7XG4gIGlmICgheCB8fCB0eXBlb2YgeCAhPT0gJ29iamVjdCcgfHwgdHlwZW9mIHgubGVuZ3RoICE9PSAnbnVtYmVyJykgcmV0dXJuIGZhbHNlO1xuICBpZiAodHlwZW9mIHguY29weSAhPT0gJ2Z1bmN0aW9uJyB8fCB0eXBlb2YgeC5zbGljZSAhPT0gJ2Z1bmN0aW9uJykge1xuICAgIHJldHVybiBmYWxzZTtcbiAgfVxuICBpZiAoeC5sZW5ndGggPiAwICYmIHR5cGVvZiB4WzBdICE9PSAnbnVtYmVyJykgcmV0dXJuIGZhbHNlO1xuICByZXR1cm4gdHJ1ZTtcbn1cblxuZnVuY3Rpb24gb2JqRXF1aXYoYSwgYiwgb3B0cykge1xuICB2YXIgaSwga2V5O1xuICBpZiAoaXNVbmRlZmluZWRPck51bGwoYSkgfHwgaXNVbmRlZmluZWRPck51bGwoYikpXG4gICAgcmV0dXJuIGZhbHNlO1xuICAvLyBhbiBpZGVudGljYWwgJ3Byb3RvdHlwZScgcHJvcGVydHkuXG4gIGlmIChhLnByb3RvdHlwZSAhPT0gYi5wcm90b3R5cGUpIHJldHVybiBmYWxzZTtcbiAgLy9+fn5JJ3ZlIG1hbmFnZWQgdG8gYnJlYWsgT2JqZWN0LmtleXMgdGhyb3VnaCBzY3Jld3kgYXJndW1lbnRzIHBhc3NpbmcuXG4gIC8vICAgQ29udmVydGluZyB0byBhcnJheSBzb2x2ZXMgdGhlIHByb2JsZW0uXG4gIGlmIChpc0FyZ3VtZW50cyhhKSkge1xuICAgIGlmICghaXNBcmd1bWVudHMoYikpIHtcbiAgICAgIHJldHVybiBmYWxzZTtcbiAgICB9XG4gICAgYSA9IHBTbGljZS5jYWxsKGEpO1xuICAgIGIgPSBwU2xpY2UuY2FsbChiKTtcbiAgICByZXR1cm4gZGVlcEVxdWFsKGEsIGIsIG9wdHMpO1xuICB9XG4gIGlmIChpc0J1ZmZlcihhKSkge1xuICAgIGlmICghaXNCdWZmZXIoYikpIHtcbiAgICAgIHJldHVybiBmYWxzZTtcbiAgICB9XG4gICAgaWYgKGEubGVuZ3RoICE9PSBiLmxlbmd0aCkgcmV0dXJuIGZhbHNlO1xuICAgIGZvciAoaSA9IDA7IGkgPCBhLmxlbmd0aDsgaSsrKSB7XG4gICAgICBpZiAoYVtpXSAhPT0gYltpXSkgcmV0dXJuIGZhbHNlO1xuICAgIH1cbiAgICByZXR1cm4gdHJ1ZTtcbiAgfVxuICB0cnkge1xuICAgIHZhciBrYSA9IG9iamVjdEtleXMoYSksXG4gICAgICAgIGtiID0gb2JqZWN0S2V5cyhiKTtcbiAgfSBjYXRjaCAoZSkgey8vaGFwcGVucyB3aGVuIG9uZSBpcyBhIHN0cmluZyBsaXRlcmFsIGFuZCB0aGUgb3RoZXIgaXNuJ3RcbiAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgLy8gaGF2aW5nIHRoZSBzYW1lIG51bWJlciBvZiBvd25lZCBwcm9wZXJ0aWVzIChrZXlzIGluY29ycG9yYXRlc1xuICAvLyBoYXNPd25Qcm9wZXJ0eSlcbiAgaWYgKGthLmxlbmd0aCAhPSBrYi5sZW5ndGgpXG4gICAgcmV0dXJuIGZhbHNlO1xuICAvL3RoZSBzYW1lIHNldCBvZiBrZXlzIChhbHRob3VnaCBub3QgbmVjZXNzYXJpbHkgdGhlIHNhbWUgb3JkZXIpLFxuICBrYS5zb3J0KCk7XG4gIGtiLnNvcnQoKTtcbiAgLy9+fn5jaGVhcCBrZXkgdGVzdFxuICBmb3IgKGkgPSBrYS5sZW5ndGggLSAxOyBpID49IDA7IGktLSkge1xuICAgIGlmIChrYVtpXSAhPSBrYltpXSlcbiAgICAgIHJldHVybiBmYWxzZTtcbiAgfVxuICAvL2VxdWl2YWxlbnQgdmFsdWVzIGZvciBldmVyeSBjb3JyZXNwb25kaW5nIGtleSwgYW5kXG4gIC8vfn5+cG9zc2libHkgZXhwZW5zaXZlIGRlZXAgdGVzdFxuICBmb3IgKGkgPSBrYS5sZW5ndGggLSAxOyBpID49IDA7IGktLSkge1xuICAgIGtleSA9IGthW2ldO1xuICAgIGlmICghZGVlcEVxdWFsKGFba2V5XSwgYltrZXldLCBvcHRzKSkgcmV0dXJuIGZhbHNlO1xuICB9XG4gIHJldHVybiB0eXBlb2YgYSA9PT0gdHlwZW9mIGI7XG59XG4iLCJ2YXIgc3VwcG9ydHNBcmd1bWVudHNDbGFzcyA9IChmdW5jdGlvbigpe1xuICByZXR1cm4gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKGFyZ3VtZW50cylcbn0pKCkgPT0gJ1tvYmplY3QgQXJndW1lbnRzXSc7XG5cbmV4cG9ydHMgPSBtb2R1bGUuZXhwb3J0cyA9IHN1cHBvcnRzQXJndW1lbnRzQ2xhc3MgPyBzdXBwb3J0ZWQgOiB1bnN1cHBvcnRlZDtcblxuZXhwb3J0cy5zdXBwb3J0ZWQgPSBzdXBwb3J0ZWQ7XG5mdW5jdGlvbiBzdXBwb3J0ZWQob2JqZWN0KSB7XG4gIHJldHVybiBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwob2JqZWN0KSA9PSAnW29iamVjdCBBcmd1bWVudHNdJztcbn07XG5cbmV4cG9ydHMudW5zdXBwb3J0ZWQgPSB1bnN1cHBvcnRlZDtcbmZ1bmN0aW9uIHVuc3VwcG9ydGVkKG9iamVjdCl7XG4gIHJldHVybiBvYmplY3QgJiZcbiAgICB0eXBlb2Ygb2JqZWN0ID09ICdvYmplY3QnICYmXG4gICAgdHlwZW9mIG9iamVjdC5sZW5ndGggPT0gJ251bWJlcicgJiZcbiAgICBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqZWN0LCAnY2FsbGVlJykgJiZcbiAgICAhT2JqZWN0LnByb3RvdHlwZS5wcm9wZXJ0eUlzRW51bWVyYWJsZS5jYWxsKG9iamVjdCwgJ2NhbGxlZScpIHx8XG4gICAgZmFsc2U7XG59O1xuIiwiZXhwb3J0cyA9IG1vZHVsZS5leHBvcnRzID0gdHlwZW9mIE9iamVjdC5rZXlzID09PSAnZnVuY3Rpb24nXG4gID8gT2JqZWN0LmtleXMgOiBzaGltO1xuXG5leHBvcnRzLnNoaW0gPSBzaGltO1xuZnVuY3Rpb24gc2hpbSAob2JqKSB7XG4gIHZhciBrZXlzID0gW107XG4gIGZvciAodmFyIGtleSBpbiBvYmopIGtleXMucHVzaChrZXkpO1xuICByZXR1cm4ga2V5cztcbn1cbiIsInZhciBpc0Z1bmN0aW9uID0gcmVxdWlyZSgnaXMtZnVuY3Rpb24nKVxuXG5tb2R1bGUuZXhwb3J0cyA9IGZvckVhY2hcblxudmFyIHRvU3RyaW5nID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZ1xudmFyIGhhc093blByb3BlcnR5ID0gT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eVxuXG5mdW5jdGlvbiBmb3JFYWNoKGxpc3QsIGl0ZXJhdG9yLCBjb250ZXh0KSB7XG4gICAgaWYgKCFpc0Z1bmN0aW9uKGl0ZXJhdG9yKSkge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdpdGVyYXRvciBtdXN0IGJlIGEgZnVuY3Rpb24nKVxuICAgIH1cblxuICAgIGlmIChhcmd1bWVudHMubGVuZ3RoIDwgMykge1xuICAgICAgICBjb250ZXh0ID0gdGhpc1xuICAgIH1cbiAgICBcbiAgICBpZiAodG9TdHJpbmcuY2FsbChsaXN0KSA9PT0gJ1tvYmplY3QgQXJyYXldJylcbiAgICAgICAgZm9yRWFjaEFycmF5KGxpc3QsIGl0ZXJhdG9yLCBjb250ZXh0KVxuICAgIGVsc2UgaWYgKHR5cGVvZiBsaXN0ID09PSAnc3RyaW5nJylcbiAgICAgICAgZm9yRWFjaFN0cmluZyhsaXN0LCBpdGVyYXRvciwgY29udGV4dClcbiAgICBlbHNlXG4gICAgICAgIGZvckVhY2hPYmplY3QobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpXG59XG5cbmZ1bmN0aW9uIGZvckVhY2hBcnJheShhcnJheSwgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gYXJyYXkubGVuZ3RoOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgaWYgKGhhc093blByb3BlcnR5LmNhbGwoYXJyYXksIGkpKSB7XG4gICAgICAgICAgICBpdGVyYXRvci5jYWxsKGNvbnRleHQsIGFycmF5W2ldLCBpLCBhcnJheSlcbiAgICAgICAgfVxuICAgIH1cbn1cblxuZnVuY3Rpb24gZm9yRWFjaFN0cmluZyhzdHJpbmcsIGl0ZXJhdG9yLCBjb250ZXh0KSB7XG4gICAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IHN0cmluZy5sZW5ndGg7IGkgPCBsZW47IGkrKykge1xuICAgICAgICAvLyBubyBzdWNoIHRoaW5nIGFzIGEgc3BhcnNlIHN0cmluZy5cbiAgICAgICAgaXRlcmF0b3IuY2FsbChjb250ZXh0LCBzdHJpbmcuY2hhckF0KGkpLCBpLCBzdHJpbmcpXG4gICAgfVxufVxuXG5mdW5jdGlvbiBmb3JFYWNoT2JqZWN0KG9iamVjdCwgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBmb3IgKHZhciBrIGluIG9iamVjdCkge1xuICAgICAgICBpZiAoaGFzT3duUHJvcGVydHkuY2FsbChvYmplY3QsIGspKSB7XG4gICAgICAgICAgICBpdGVyYXRvci5jYWxsKGNvbnRleHQsIG9iamVjdFtrXSwgaywgb2JqZWN0KVxuICAgICAgICB9XG4gICAgfVxufVxuIiwiaWYgKHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICBtb2R1bGUuZXhwb3J0cyA9IHdpbmRvdztcbn0gZWxzZSBpZiAodHlwZW9mIGdsb2JhbCAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgIG1vZHVsZS5leHBvcnRzID0gZ2xvYmFsO1xufSBlbHNlIGlmICh0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIil7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSBzZWxmO1xufSBlbHNlIHtcbiAgICBtb2R1bGUuZXhwb3J0cyA9IHt9O1xufVxuIiwiLyoqXG4gKiBJbmRpY2F0ZXMgdGhhdCBuYXZpZ2F0aW9uIHdhcyBjYXVzZWQgYnkgYSBjYWxsIHRvIGhpc3RvcnkucHVzaC5cbiAqL1xuJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xudmFyIFBVU0ggPSAnUFVTSCc7XG5cbmV4cG9ydHMuUFVTSCA9IFBVU0g7XG4vKipcbiAqIEluZGljYXRlcyB0aGF0IG5hdmlnYXRpb24gd2FzIGNhdXNlZCBieSBhIGNhbGwgdG8gaGlzdG9yeS5yZXBsYWNlLlxuICovXG52YXIgUkVQTEFDRSA9ICdSRVBMQUNFJztcblxuZXhwb3J0cy5SRVBMQUNFID0gUkVQTEFDRTtcbi8qKlxuICogSW5kaWNhdGVzIHRoYXQgbmF2aWdhdGlvbiB3YXMgY2F1c2VkIGJ5IHNvbWUgb3RoZXIgYWN0aW9uIHN1Y2hcbiAqIGFzIHVzaW5nIGEgYnJvd3NlcidzIGJhY2svZm9yd2FyZCBidXR0b25zIGFuZC9vciBtYW51YWxseSBtYW5pcHVsYXRpbmdcbiAqIHRoZSBVUkwgaW4gYSBicm93c2VyJ3MgbG9jYXRpb24gYmFyLiBUaGlzIGlzIHRoZSBkZWZhdWx0LlxuICpcbiAqIFNlZSBodHRwczovL2RldmVsb3Blci5tb3ppbGxhLm9yZy9lbi1VUy9kb2NzL1dlYi9BUEkvV2luZG93RXZlbnRIYW5kbGVycy9vbnBvcHN0YXRlXG4gKiBmb3IgbW9yZSBpbmZvcm1hdGlvbi5cbiAqL1xudmFyIFBPUCA9ICdQT1AnO1xuXG5leHBvcnRzLlBPUCA9IFBPUDtcbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IHtcbiAgUFVTSDogUFVTSCxcbiAgUkVQTEFDRTogUkVQTEFDRSxcbiAgUE9QOiBQT1Bcbn07IiwiXCJ1c2Ugc3RyaWN0XCI7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG52YXIgX3NsaWNlID0gQXJyYXkucHJvdG90eXBlLnNsaWNlO1xuZXhwb3J0cy5sb29wQXN5bmMgPSBsb29wQXN5bmM7XG5cbmZ1bmN0aW9uIGxvb3BBc3luYyh0dXJucywgd29yaywgY2FsbGJhY2spIHtcbiAgdmFyIGN1cnJlbnRUdXJuID0gMCxcbiAgICAgIGlzRG9uZSA9IGZhbHNlO1xuICB2YXIgc3luYyA9IGZhbHNlLFxuICAgICAgaGFzTmV4dCA9IGZhbHNlLFxuICAgICAgZG9uZUFyZ3MgPSB1bmRlZmluZWQ7XG5cbiAgZnVuY3Rpb24gZG9uZSgpIHtcbiAgICBpc0RvbmUgPSB0cnVlO1xuICAgIGlmIChzeW5jKSB7XG4gICAgICAvLyBJdGVyYXRlIGluc3RlYWQgb2YgcmVjdXJzaW5nIGlmIHBvc3NpYmxlLlxuICAgICAgZG9uZUFyZ3MgPSBbXS5jb25jYXQoX3NsaWNlLmNhbGwoYXJndW1lbnRzKSk7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgY2FsbGJhY2suYXBwbHkodGhpcywgYXJndW1lbnRzKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIG5leHQoKSB7XG4gICAgaWYgKGlzRG9uZSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGhhc05leHQgPSB0cnVlO1xuICAgIGlmIChzeW5jKSB7XG4gICAgICAvLyBJdGVyYXRlIGluc3RlYWQgb2YgcmVjdXJzaW5nIGlmIHBvc3NpYmxlLlxuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIHN5bmMgPSB0cnVlO1xuXG4gICAgd2hpbGUgKCFpc0RvbmUgJiYgY3VycmVudFR1cm4gPCB0dXJucyAmJiBoYXNOZXh0KSB7XG4gICAgICBoYXNOZXh0ID0gZmFsc2U7XG4gICAgICB3b3JrLmNhbGwodGhpcywgY3VycmVudFR1cm4rKywgbmV4dCwgZG9uZSk7XG4gICAgfVxuXG4gICAgc3luYyA9IGZhbHNlO1xuXG4gICAgaWYgKGlzRG9uZSkge1xuICAgICAgLy8gVGhpcyBtZWFucyB0aGUgbG9vcCBmaW5pc2hlZCBzeW5jaHJvbm91c2x5LlxuICAgICAgY2FsbGJhY2suYXBwbHkodGhpcywgZG9uZUFyZ3MpO1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGlmIChjdXJyZW50VHVybiA+PSB0dXJucyAmJiBoYXNOZXh0KSB7XG4gICAgICBpc0RvbmUgPSB0cnVlO1xuICAgICAgY2FsbGJhY2soKTtcbiAgICB9XG4gIH1cblxuICBuZXh0KCk7XG59IiwiLyplc2xpbnQtZGlzYWJsZSBuby1lbXB0eSAqL1xuJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5zYXZlU3RhdGUgPSBzYXZlU3RhdGU7XG5leHBvcnRzLnJlYWRTdGF0ZSA9IHJlYWRTdGF0ZTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIEtleVByZWZpeCA9ICdAQEhpc3RvcnkvJztcbnZhciBRdW90YUV4Y2VlZGVkRXJyb3JzID0gWydRdW90YUV4Y2VlZGVkRXJyb3InLCAnUVVPVEFfRVhDRUVERURfRVJSJ107XG5cbnZhciBTZWN1cml0eUVycm9yID0gJ1NlY3VyaXR5RXJyb3InO1xuXG5mdW5jdGlvbiBjcmVhdGVLZXkoa2V5KSB7XG4gIHJldHVybiBLZXlQcmVmaXggKyBrZXk7XG59XG5cbmZ1bmN0aW9uIHNhdmVTdGF0ZShrZXksIHN0YXRlKSB7XG4gIHRyeSB7XG4gICAgaWYgKHN0YXRlID09IG51bGwpIHtcbiAgICAgIHdpbmRvdy5zZXNzaW9uU3RvcmFnZS5yZW1vdmVJdGVtKGNyZWF0ZUtleShrZXkpKTtcbiAgICB9IGVsc2Uge1xuICAgICAgd2luZG93LnNlc3Npb25TdG9yYWdlLnNldEl0ZW0oY3JlYXRlS2V5KGtleSksIEpTT04uc3RyaW5naWZ5KHN0YXRlKSk7XG4gICAgfVxuICB9IGNhdGNoIChlcnJvcikge1xuICAgIGlmIChlcnJvci5uYW1lID09PSBTZWN1cml0eUVycm9yKSB7XG4gICAgICAvLyBCbG9ja2luZyBjb29raWVzIGluIENocm9tZS9GaXJlZm94L1NhZmFyaSB0aHJvd3MgU2VjdXJpdHlFcnJvciBvbiBhbnlcbiAgICAgIC8vIGF0dGVtcHQgdG8gYWNjZXNzIHdpbmRvdy5zZXNzaW9uU3RvcmFnZS5cbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ1toaXN0b3J5XSBVbmFibGUgdG8gc2F2ZSBzdGF0ZTsgc2Vzc2lvblN0b3JhZ2UgaXMgbm90IGF2YWlsYWJsZSBkdWUgdG8gc2VjdXJpdHkgc2V0dGluZ3MnKSA6IHVuZGVmaW5lZDtcblxuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGlmIChRdW90YUV4Y2VlZGVkRXJyb3JzLmluZGV4T2YoZXJyb3IubmFtZSkgPj0gMCAmJiB3aW5kb3cuc2Vzc2lvblN0b3JhZ2UubGVuZ3RoID09PSAwKSB7XG4gICAgICAvLyBTYWZhcmkgXCJwcml2YXRlIG1vZGVcIiB0aHJvd3MgUXVvdGFFeGNlZWRlZEVycm9yLlxuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnW2hpc3RvcnldIFVuYWJsZSB0byBzYXZlIHN0YXRlOyBzZXNzaW9uU3RvcmFnZSBpcyBub3QgYXZhaWxhYmxlIGluIFNhZmFyaSBwcml2YXRlIG1vZGUnKSA6IHVuZGVmaW5lZDtcblxuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIHRocm93IGVycm9yO1xuICB9XG59XG5cbmZ1bmN0aW9uIHJlYWRTdGF0ZShrZXkpIHtcbiAgdmFyIGpzb24gPSB1bmRlZmluZWQ7XG4gIHRyeSB7XG4gICAganNvbiA9IHdpbmRvdy5zZXNzaW9uU3RvcmFnZS5nZXRJdGVtKGNyZWF0ZUtleShrZXkpKTtcbiAgfSBjYXRjaCAoZXJyb3IpIHtcbiAgICBpZiAoZXJyb3IubmFtZSA9PT0gU2VjdXJpdHlFcnJvcikge1xuICAgICAgLy8gQmxvY2tpbmcgY29va2llcyBpbiBDaHJvbWUvRmlyZWZveC9TYWZhcmkgdGhyb3dzIFNlY3VyaXR5RXJyb3Igb24gYW55XG4gICAgICAvLyBhdHRlbXB0IHRvIGFjY2VzcyB3aW5kb3cuc2Vzc2lvblN0b3JhZ2UuXG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdbaGlzdG9yeV0gVW5hYmxlIHRvIHJlYWQgc3RhdGU7IHNlc3Npb25TdG9yYWdlIGlzIG5vdCBhdmFpbGFibGUgZHVlIHRvIHNlY3VyaXR5IHNldHRpbmdzJykgOiB1bmRlZmluZWQ7XG5cbiAgICAgIHJldHVybiBudWxsO1xuICAgIH1cbiAgfVxuXG4gIGlmIChqc29uKSB7XG4gICAgdHJ5IHtcbiAgICAgIHJldHVybiBKU09OLnBhcnNlKGpzb24pO1xuICAgIH0gY2F0Y2ggKGVycm9yKSB7XG4gICAgICAvLyBJZ25vcmUgaW52YWxpZCBKU09OLlxuICAgIH1cbiAgfVxuXG4gIHJldHVybiBudWxsO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuYWRkRXZlbnRMaXN0ZW5lciA9IGFkZEV2ZW50TGlzdGVuZXI7XG5leHBvcnRzLnJlbW92ZUV2ZW50TGlzdGVuZXIgPSByZW1vdmVFdmVudExpc3RlbmVyO1xuZXhwb3J0cy5nZXRIYXNoUGF0aCA9IGdldEhhc2hQYXRoO1xuZXhwb3J0cy5yZXBsYWNlSGFzaFBhdGggPSByZXBsYWNlSGFzaFBhdGg7XG5leHBvcnRzLmdldFdpbmRvd1BhdGggPSBnZXRXaW5kb3dQYXRoO1xuZXhwb3J0cy5nbyA9IGdvO1xuZXhwb3J0cy5nZXRVc2VyQ29uZmlybWF0aW9uID0gZ2V0VXNlckNvbmZpcm1hdGlvbjtcbmV4cG9ydHMuc3VwcG9ydHNIaXN0b3J5ID0gc3VwcG9ydHNIaXN0b3J5O1xuZXhwb3J0cy5zdXBwb3J0c0dvV2l0aG91dFJlbG9hZFVzaW5nSGFzaCA9IHN1cHBvcnRzR29XaXRob3V0UmVsb2FkVXNpbmdIYXNoO1xuXG5mdW5jdGlvbiBhZGRFdmVudExpc3RlbmVyKG5vZGUsIGV2ZW50LCBsaXN0ZW5lcikge1xuICBpZiAobm9kZS5hZGRFdmVudExpc3RlbmVyKSB7XG4gICAgbm9kZS5hZGRFdmVudExpc3RlbmVyKGV2ZW50LCBsaXN0ZW5lciwgZmFsc2UpO1xuICB9IGVsc2Uge1xuICAgIG5vZGUuYXR0YWNoRXZlbnQoJ29uJyArIGV2ZW50LCBsaXN0ZW5lcik7XG4gIH1cbn1cblxuZnVuY3Rpb24gcmVtb3ZlRXZlbnRMaXN0ZW5lcihub2RlLCBldmVudCwgbGlzdGVuZXIpIHtcbiAgaWYgKG5vZGUucmVtb3ZlRXZlbnRMaXN0ZW5lcikge1xuICAgIG5vZGUucmVtb3ZlRXZlbnRMaXN0ZW5lcihldmVudCwgbGlzdGVuZXIsIGZhbHNlKTtcbiAgfSBlbHNlIHtcbiAgICBub2RlLmRldGFjaEV2ZW50KCdvbicgKyBldmVudCwgbGlzdGVuZXIpO1xuICB9XG59XG5cbmZ1bmN0aW9uIGdldEhhc2hQYXRoKCkge1xuICAvLyBXZSBjYW4ndCB1c2Ugd2luZG93LmxvY2F0aW9uLmhhc2ggaGVyZSBiZWNhdXNlIGl0J3Mgbm90XG4gIC8vIGNvbnNpc3RlbnQgYWNyb3NzIGJyb3dzZXJzIC0gRmlyZWZveCB3aWxsIHByZS1kZWNvZGUgaXQhXG4gIHJldHVybiB3aW5kb3cubG9jYXRpb24uaHJlZi5zcGxpdCgnIycpWzFdIHx8ICcnO1xufVxuXG5mdW5jdGlvbiByZXBsYWNlSGFzaFBhdGgocGF0aCkge1xuICB3aW5kb3cubG9jYXRpb24ucmVwbGFjZSh3aW5kb3cubG9jYXRpb24ucGF0aG5hbWUgKyB3aW5kb3cubG9jYXRpb24uc2VhcmNoICsgJyMnICsgcGF0aCk7XG59XG5cbmZ1bmN0aW9uIGdldFdpbmRvd1BhdGgoKSB7XG4gIHJldHVybiB3aW5kb3cubG9jYXRpb24ucGF0aG5hbWUgKyB3aW5kb3cubG9jYXRpb24uc2VhcmNoICsgd2luZG93LmxvY2F0aW9uLmhhc2g7XG59XG5cbmZ1bmN0aW9uIGdvKG4pIHtcbiAgaWYgKG4pIHdpbmRvdy5oaXN0b3J5LmdvKG4pO1xufVxuXG5mdW5jdGlvbiBnZXRVc2VyQ29uZmlybWF0aW9uKG1lc3NhZ2UsIGNhbGxiYWNrKSB7XG4gIGNhbGxiYWNrKHdpbmRvdy5jb25maXJtKG1lc3NhZ2UpKTtcbn1cblxuLyoqXG4gKiBSZXR1cm5zIHRydWUgaWYgdGhlIEhUTUw1IGhpc3RvcnkgQVBJIGlzIHN1cHBvcnRlZC4gVGFrZW4gZnJvbSBNb2Rlcm5penIuXG4gKlxuICogaHR0cHM6Ly9naXRodWIuY29tL01vZGVybml6ci9Nb2Rlcm5penIvYmxvYi9tYXN0ZXIvTElDRU5TRVxuICogaHR0cHM6Ly9naXRodWIuY29tL01vZGVybml6ci9Nb2Rlcm5penIvYmxvYi9tYXN0ZXIvZmVhdHVyZS1kZXRlY3RzL2hpc3RvcnkuanNcbiAqIGNoYW5nZWQgdG8gYXZvaWQgZmFsc2UgbmVnYXRpdmVzIGZvciBXaW5kb3dzIFBob25lczogaHR0cHM6Ly9naXRodWIuY29tL3JhY2t0L3JlYWN0LXJvdXRlci9pc3N1ZXMvNTg2XG4gKi9cblxuZnVuY3Rpb24gc3VwcG9ydHNIaXN0b3J5KCkge1xuICB2YXIgdWEgPSBuYXZpZ2F0b3IudXNlckFnZW50O1xuICBpZiAoKHVhLmluZGV4T2YoJ0FuZHJvaWQgMi4nKSAhPT0gLTEgfHwgdWEuaW5kZXhPZignQW5kcm9pZCA0LjAnKSAhPT0gLTEpICYmIHVhLmluZGV4T2YoJ01vYmlsZSBTYWZhcmknKSAhPT0gLTEgJiYgdWEuaW5kZXhPZignQ2hyb21lJykgPT09IC0xICYmIHVhLmluZGV4T2YoJ1dpbmRvd3MgUGhvbmUnKSA9PT0gLTEpIHtcbiAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgcmV0dXJuIHdpbmRvdy5oaXN0b3J5ICYmICdwdXNoU3RhdGUnIGluIHdpbmRvdy5oaXN0b3J5O1xufVxuXG4vKipcbiAqIFJldHVybnMgZmFsc2UgaWYgdXNpbmcgZ28obikgd2l0aCBoYXNoIGhpc3RvcnkgY2F1c2VzIGEgZnVsbCBwYWdlIHJlbG9hZC5cbiAqL1xuXG5mdW5jdGlvbiBzdXBwb3J0c0dvV2l0aG91dFJlbG9hZFVzaW5nSGFzaCgpIHtcbiAgdmFyIHVhID0gbmF2aWdhdG9yLnVzZXJBZ2VudDtcbiAgcmV0dXJuIHVhLmluZGV4T2YoJ0ZpcmVmb3gnKSA9PT0gLTE7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xudmFyIGNhblVzZURPTSA9ICEhKHR5cGVvZiB3aW5kb3cgIT09ICd1bmRlZmluZWQnICYmIHdpbmRvdy5kb2N1bWVudCAmJiB3aW5kb3cuZG9jdW1lbnQuY3JlYXRlRWxlbWVudCk7XG5leHBvcnRzLmNhblVzZURPTSA9IGNhblVzZURPTTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmV4dHJhY3RQYXRoID0gZXh0cmFjdFBhdGg7XG5leHBvcnRzLnBhcnNlUGF0aCA9IHBhcnNlUGF0aDtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gZXh0cmFjdFBhdGgoc3RyaW5nKSB7XG4gIHZhciBtYXRjaCA9IHN0cmluZy5tYXRjaCgvXmh0dHBzPzpcXC9cXC9bXlxcL10qLyk7XG5cbiAgaWYgKG1hdGNoID09IG51bGwpIHJldHVybiBzdHJpbmc7XG5cbiAgcmV0dXJuIHN0cmluZy5zdWJzdHJpbmcobWF0Y2hbMF0ubGVuZ3RoKTtcbn1cblxuZnVuY3Rpb24gcGFyc2VQYXRoKHBhdGgpIHtcbiAgdmFyIHBhdGhuYW1lID0gZXh0cmFjdFBhdGgocGF0aCk7XG4gIHZhciBzZWFyY2ggPSAnJztcbiAgdmFyIGhhc2ggPSAnJztcblxuICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10ocGF0aCA9PT0gcGF0aG5hbWUsICdBIHBhdGggbXVzdCBiZSBwYXRobmFtZSArIHNlYXJjaCArIGhhc2ggb25seSwgbm90IGEgZnVsbHkgcXVhbGlmaWVkIFVSTCBsaWtlIFwiJXNcIicsIHBhdGgpIDogdW5kZWZpbmVkO1xuXG4gIHZhciBoYXNoSW5kZXggPSBwYXRobmFtZS5pbmRleE9mKCcjJyk7XG4gIGlmIChoYXNoSW5kZXggIT09IC0xKSB7XG4gICAgaGFzaCA9IHBhdGhuYW1lLnN1YnN0cmluZyhoYXNoSW5kZXgpO1xuICAgIHBhdGhuYW1lID0gcGF0aG5hbWUuc3Vic3RyaW5nKDAsIGhhc2hJbmRleCk7XG4gIH1cblxuICB2YXIgc2VhcmNoSW5kZXggPSBwYXRobmFtZS5pbmRleE9mKCc/Jyk7XG4gIGlmIChzZWFyY2hJbmRleCAhPT0gLTEpIHtcbiAgICBzZWFyY2ggPSBwYXRobmFtZS5zdWJzdHJpbmcoc2VhcmNoSW5kZXgpO1xuICAgIHBhdGhuYW1lID0gcGF0aG5hbWUuc3Vic3RyaW5nKDAsIHNlYXJjaEluZGV4KTtcbiAgfVxuXG4gIGlmIChwYXRobmFtZSA9PT0gJycpIHBhdGhuYW1lID0gJy8nO1xuXG4gIHJldHVybiB7XG4gICAgcGF0aG5hbWU6IHBhdGhuYW1lLFxuICAgIHNlYXJjaDogc2VhcmNoLFxuICAgIGhhc2g6IGhhc2hcbiAgfTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9BY3Rpb25zID0gcmVxdWlyZSgnLi9BY3Rpb25zJyk7XG5cbnZhciBfUGF0aFV0aWxzID0gcmVxdWlyZSgnLi9QYXRoVXRpbHMnKTtcblxudmFyIF9FeGVjdXRpb25FbnZpcm9ubWVudCA9IHJlcXVpcmUoJy4vRXhlY3V0aW9uRW52aXJvbm1lbnQnKTtcblxudmFyIF9ET01VdGlscyA9IHJlcXVpcmUoJy4vRE9NVXRpbHMnKTtcblxudmFyIF9ET01TdGF0ZVN0b3JhZ2UgPSByZXF1aXJlKCcuL0RPTVN0YXRlU3RvcmFnZScpO1xuXG52YXIgX2NyZWF0ZURPTUhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZURPTUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVET01IaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZURPTUhpc3RvcnkpO1xuXG4vKipcbiAqIENyZWF0ZXMgYW5kIHJldHVybnMgYSBoaXN0b3J5IG9iamVjdCB0aGF0IHVzZXMgSFRNTDUncyBoaXN0b3J5IEFQSVxuICogKHB1c2hTdGF0ZSwgcmVwbGFjZVN0YXRlLCBhbmQgdGhlIHBvcHN0YXRlIGV2ZW50KSB0byBtYW5hZ2UgaGlzdG9yeS5cbiAqIFRoaXMgaXMgdGhlIHJlY29tbWVuZGVkIG1ldGhvZCBvZiBtYW5hZ2luZyBoaXN0b3J5IGluIGJyb3dzZXJzIGJlY2F1c2VcbiAqIGl0IHByb3ZpZGVzIHRoZSBjbGVhbmVzdCBVUkxzLlxuICpcbiAqIE5vdGU6IEluIGJyb3dzZXJzIHRoYXQgZG8gbm90IHN1cHBvcnQgdGhlIEhUTUw1IGhpc3RvcnkgQVBJIGZ1bGxcbiAqIHBhZ2UgcmVsb2FkcyB3aWxsIGJlIHVzZWQgdG8gcHJlc2VydmUgVVJMcy5cbiAqL1xuZnVuY3Rpb24gY3JlYXRlQnJvd3Nlckhpc3RvcnkoKSB7XG4gIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8ge30gOiBhcmd1bWVudHNbMF07XG5cbiAgIV9FeGVjdXRpb25FbnZpcm9ubWVudC5jYW5Vc2VET00gPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSwgJ0Jyb3dzZXIgaGlzdG9yeSBuZWVkcyBhIERPTScpIDogX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSkgOiB1bmRlZmluZWQ7XG5cbiAgdmFyIGZvcmNlUmVmcmVzaCA9IG9wdGlvbnMuZm9yY2VSZWZyZXNoO1xuXG4gIHZhciBpc1N1cHBvcnRlZCA9IF9ET01VdGlscy5zdXBwb3J0c0hpc3RvcnkoKTtcbiAgdmFyIHVzZVJlZnJlc2ggPSAhaXNTdXBwb3J0ZWQgfHwgZm9yY2VSZWZyZXNoO1xuXG4gIGZ1bmN0aW9uIGdldEN1cnJlbnRMb2NhdGlvbihoaXN0b3J5U3RhdGUpIHtcbiAgICB0cnkge1xuICAgICAgaGlzdG9yeVN0YXRlID0gaGlzdG9yeVN0YXRlIHx8IHdpbmRvdy5oaXN0b3J5LnN0YXRlIHx8IHt9O1xuICAgIH0gY2F0Y2ggKGUpIHtcbiAgICAgIGhpc3RvcnlTdGF0ZSA9IHt9O1xuICAgIH1cblxuICAgIHZhciBwYXRoID0gX0RPTVV0aWxzLmdldFdpbmRvd1BhdGgoKTtcbiAgICB2YXIgX2hpc3RvcnlTdGF0ZSA9IGhpc3RvcnlTdGF0ZTtcbiAgICB2YXIga2V5ID0gX2hpc3RvcnlTdGF0ZS5rZXk7XG5cbiAgICB2YXIgc3RhdGUgPSB1bmRlZmluZWQ7XG4gICAgaWYgKGtleSkge1xuICAgICAgc3RhdGUgPSBfRE9NU3RhdGVTdG9yYWdlLnJlYWRTdGF0ZShrZXkpO1xuICAgIH0gZWxzZSB7XG4gICAgICBzdGF0ZSA9IG51bGw7XG4gICAgICBrZXkgPSBoaXN0b3J5LmNyZWF0ZUtleSgpO1xuXG4gICAgICBpZiAoaXNTdXBwb3J0ZWQpIHdpbmRvdy5oaXN0b3J5LnJlcGxhY2VTdGF0ZShfZXh0ZW5kcyh7fSwgaGlzdG9yeVN0YXRlLCB7IGtleToga2V5IH0pLCBudWxsKTtcbiAgICB9XG5cbiAgICB2YXIgbG9jYXRpb24gPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uKF9leHRlbmRzKHt9LCBsb2NhdGlvbiwgeyBzdGF0ZTogc3RhdGUgfSksIHVuZGVmaW5lZCwga2V5KTtcbiAgfVxuXG4gIGZ1bmN0aW9uIHN0YXJ0UG9wU3RhdGVMaXN0ZW5lcihfcmVmKSB7XG4gICAgdmFyIHRyYW5zaXRpb25UbyA9IF9yZWYudHJhbnNpdGlvblRvO1xuXG4gICAgZnVuY3Rpb24gcG9wU3RhdGVMaXN0ZW5lcihldmVudCkge1xuICAgICAgaWYgKGV2ZW50LnN0YXRlID09PSB1bmRlZmluZWQpIHJldHVybjsgLy8gSWdub3JlIGV4dHJhbmVvdXMgcG9wc3RhdGUgZXZlbnRzIGluIFdlYktpdC5cblxuICAgICAgdHJhbnNpdGlvblRvKGdldEN1cnJlbnRMb2NhdGlvbihldmVudC5zdGF0ZSkpO1xuICAgIH1cblxuICAgIF9ET01VdGlscy5hZGRFdmVudExpc3RlbmVyKHdpbmRvdywgJ3BvcHN0YXRlJywgcG9wU3RhdGVMaXN0ZW5lcik7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgX0RPTVV0aWxzLnJlbW92ZUV2ZW50TGlzdGVuZXIod2luZG93LCAncG9wc3RhdGUnLCBwb3BTdGF0ZUxpc3RlbmVyKTtcbiAgICB9O1xuICB9XG5cbiAgZnVuY3Rpb24gZmluaXNoVHJhbnNpdGlvbihsb2NhdGlvbikge1xuICAgIHZhciBiYXNlbmFtZSA9IGxvY2F0aW9uLmJhc2VuYW1lO1xuICAgIHZhciBwYXRobmFtZSA9IGxvY2F0aW9uLnBhdGhuYW1lO1xuICAgIHZhciBzZWFyY2ggPSBsb2NhdGlvbi5zZWFyY2g7XG4gICAgdmFyIGhhc2ggPSBsb2NhdGlvbi5oYXNoO1xuICAgIHZhciBzdGF0ZSA9IGxvY2F0aW9uLnN0YXRlO1xuICAgIHZhciBhY3Rpb24gPSBsb2NhdGlvbi5hY3Rpb247XG4gICAgdmFyIGtleSA9IGxvY2F0aW9uLmtleTtcblxuICAgIGlmIChhY3Rpb24gPT09IF9BY3Rpb25zLlBPUCkgcmV0dXJuOyAvLyBOb3RoaW5nIHRvIGRvLlxuXG4gICAgX0RPTVN0YXRlU3RvcmFnZS5zYXZlU3RhdGUoa2V5LCBzdGF0ZSk7XG5cbiAgICB2YXIgcGF0aCA9IChiYXNlbmFtZSB8fCAnJykgKyBwYXRobmFtZSArIHNlYXJjaCArIGhhc2g7XG4gICAgdmFyIGhpc3RvcnlTdGF0ZSA9IHtcbiAgICAgIGtleToga2V5XG4gICAgfTtcblxuICAgIGlmIChhY3Rpb24gPT09IF9BY3Rpb25zLlBVU0gpIHtcbiAgICAgIGlmICh1c2VSZWZyZXNoKSB7XG4gICAgICAgIHdpbmRvdy5sb2NhdGlvbi5ocmVmID0gcGF0aDtcbiAgICAgICAgcmV0dXJuIGZhbHNlOyAvLyBQcmV2ZW50IGxvY2F0aW9uIHVwZGF0ZS5cbiAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgd2luZG93Lmhpc3RvcnkucHVzaFN0YXRlKGhpc3RvcnlTdGF0ZSwgbnVsbCwgcGF0aCk7XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgLy8gUkVQTEFDRVxuICAgICAgaWYgKHVzZVJlZnJlc2gpIHtcbiAgICAgICAgd2luZG93LmxvY2F0aW9uLnJlcGxhY2UocGF0aCk7XG4gICAgICAgIHJldHVybiBmYWxzZTsgLy8gUHJldmVudCBsb2NhdGlvbiB1cGRhdGUuXG4gICAgICB9IGVsc2Uge1xuICAgICAgICAgIHdpbmRvdy5oaXN0b3J5LnJlcGxhY2VTdGF0ZShoaXN0b3J5U3RhdGUsIG51bGwsIHBhdGgpO1xuICAgICAgICB9XG4gICAgfVxuICB9XG5cbiAgdmFyIGhpc3RvcnkgPSBfY3JlYXRlRE9NSGlzdG9yeTJbJ2RlZmF1bHQnXShfZXh0ZW5kcyh7fSwgb3B0aW9ucywge1xuICAgIGdldEN1cnJlbnRMb2NhdGlvbjogZ2V0Q3VycmVudExvY2F0aW9uLFxuICAgIGZpbmlzaFRyYW5zaXRpb246IGZpbmlzaFRyYW5zaXRpb24sXG4gICAgc2F2ZVN0YXRlOiBfRE9NU3RhdGVTdG9yYWdlLnNhdmVTdGF0ZVxuICB9KSk7XG5cbiAgdmFyIGxpc3RlbmVyQ291bnQgPSAwLFxuICAgICAgc3RvcFBvcFN0YXRlTGlzdGVuZXIgPSB1bmRlZmluZWQ7XG5cbiAgZnVuY3Rpb24gbGlzdGVuQmVmb3JlKGxpc3RlbmVyKSB7XG4gICAgaWYgKCsrbGlzdGVuZXJDb3VudCA9PT0gMSkgc3RvcFBvcFN0YXRlTGlzdGVuZXIgPSBzdGFydFBvcFN0YXRlTGlzdGVuZXIoaGlzdG9yeSk7XG5cbiAgICB2YXIgdW5saXN0ZW4gPSBoaXN0b3J5Lmxpc3RlbkJlZm9yZShsaXN0ZW5lcik7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgdW5saXN0ZW4oKTtcblxuICAgICAgaWYgKC0tbGlzdGVuZXJDb3VudCA9PT0gMCkgc3RvcFBvcFN0YXRlTGlzdGVuZXIoKTtcbiAgICB9O1xuICB9XG5cbiAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgaWYgKCsrbGlzdGVuZXJDb3VudCA9PT0gMSkgc3RvcFBvcFN0YXRlTGlzdGVuZXIgPSBzdGFydFBvcFN0YXRlTGlzdGVuZXIoaGlzdG9yeSk7XG5cbiAgICB2YXIgdW5saXN0ZW4gPSBoaXN0b3J5Lmxpc3RlbihsaXN0ZW5lcik7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgdW5saXN0ZW4oKTtcblxuICAgICAgaWYgKC0tbGlzdGVuZXJDb3VudCA9PT0gMCkgc3RvcFBvcFN0YXRlTGlzdGVuZXIoKTtcbiAgICB9O1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiByZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spIHtcbiAgICBpZiAoKytsaXN0ZW5lckNvdW50ID09PSAxKSBzdG9wUG9wU3RhdGVMaXN0ZW5lciA9IHN0YXJ0UG9wU3RhdGVMaXN0ZW5lcihoaXN0b3J5KTtcblxuICAgIGhpc3RvcnkucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spIHtcbiAgICBoaXN0b3J5LnVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKTtcblxuICAgIGlmICgtLWxpc3RlbmVyQ291bnQgPT09IDApIHN0b3BQb3BTdGF0ZUxpc3RlbmVyKCk7XG4gIH1cblxuICByZXR1cm4gX2V4dGVuZHMoe30sIGhpc3RvcnksIHtcbiAgICBsaXN0ZW5CZWZvcmU6IGxpc3RlbkJlZm9yZSxcbiAgICBsaXN0ZW46IGxpc3RlbixcbiAgICByZWdpc3RlclRyYW5zaXRpb25Ib29rOiByZWdpc3RlclRyYW5zaXRpb25Ib29rLFxuICAgIHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vazogdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rXG4gIH0pO1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBjcmVhdGVCcm93c2VySGlzdG9yeTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX0V4ZWN1dGlvbkVudmlyb25tZW50ID0gcmVxdWlyZSgnLi9FeGVjdXRpb25FbnZpcm9ubWVudCcpO1xuXG52YXIgX0RPTVV0aWxzID0gcmVxdWlyZSgnLi9ET01VdGlscycpO1xuXG52YXIgX2NyZWF0ZUhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZUhpc3RvcnkpO1xuXG5mdW5jdGlvbiBjcmVhdGVET01IaXN0b3J5KG9wdGlvbnMpIHtcbiAgdmFyIGhpc3RvcnkgPSBfY3JlYXRlSGlzdG9yeTJbJ2RlZmF1bHQnXShfZXh0ZW5kcyh7XG4gICAgZ2V0VXNlckNvbmZpcm1hdGlvbjogX0RPTVV0aWxzLmdldFVzZXJDb25maXJtYXRpb25cbiAgfSwgb3B0aW9ucywge1xuICAgIGdvOiBfRE9NVXRpbHMuZ29cbiAgfSkpO1xuXG4gIGZ1bmN0aW9uIGxpc3RlbihsaXN0ZW5lcikge1xuICAgICFfRXhlY3V0aW9uRW52aXJvbm1lbnQuY2FuVXNlRE9NID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UsICdET00gaGlzdG9yeSBuZWVkcyBhIERPTScpIDogX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSkgOiB1bmRlZmluZWQ7XG5cbiAgICByZXR1cm4gaGlzdG9yeS5saXN0ZW4obGlzdGVuZXIpO1xuICB9XG5cbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB7XG4gICAgbGlzdGVuOiBsaXN0ZW5cbiAgfSk7XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGNyZWF0ZURPTUhpc3Rvcnk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfQWN0aW9ucyA9IHJlcXVpcmUoJy4vQWN0aW9ucycpO1xuXG52YXIgX1BhdGhVdGlscyA9IHJlcXVpcmUoJy4vUGF0aFV0aWxzJyk7XG5cbnZhciBfRXhlY3V0aW9uRW52aXJvbm1lbnQgPSByZXF1aXJlKCcuL0V4ZWN1dGlvbkVudmlyb25tZW50Jyk7XG5cbnZhciBfRE9NVXRpbHMgPSByZXF1aXJlKCcuL0RPTVV0aWxzJyk7XG5cbnZhciBfRE9NU3RhdGVTdG9yYWdlID0gcmVxdWlyZSgnLi9ET01TdGF0ZVN0b3JhZ2UnKTtcblxudmFyIF9jcmVhdGVET01IaXN0b3J5ID0gcmVxdWlyZSgnLi9jcmVhdGVET01IaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlRE9NSGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVET01IaXN0b3J5KTtcblxuZnVuY3Rpb24gaXNBYnNvbHV0ZVBhdGgocGF0aCkge1xuICByZXR1cm4gdHlwZW9mIHBhdGggPT09ICdzdHJpbmcnICYmIHBhdGguY2hhckF0KDApID09PSAnLyc7XG59XG5cbmZ1bmN0aW9uIGVuc3VyZVNsYXNoKCkge1xuICB2YXIgcGF0aCA9IF9ET01VdGlscy5nZXRIYXNoUGF0aCgpO1xuXG4gIGlmIChpc0Fic29sdXRlUGF0aChwYXRoKSkgcmV0dXJuIHRydWU7XG5cbiAgX0RPTVV0aWxzLnJlcGxhY2VIYXNoUGF0aCgnLycgKyBwYXRoKTtcblxuICByZXR1cm4gZmFsc2U7XG59XG5cbmZ1bmN0aW9uIGFkZFF1ZXJ5U3RyaW5nVmFsdWVUb1BhdGgocGF0aCwga2V5LCB2YWx1ZSkge1xuICByZXR1cm4gcGF0aCArIChwYXRoLmluZGV4T2YoJz8nKSA9PT0gLTEgPyAnPycgOiAnJicpICsgKGtleSArICc9JyArIHZhbHVlKTtcbn1cblxuZnVuY3Rpb24gc3RyaXBRdWVyeVN0cmluZ1ZhbHVlRnJvbVBhdGgocGF0aCwga2V5KSB7XG4gIHJldHVybiBwYXRoLnJlcGxhY2UobmV3IFJlZ0V4cCgnWz8mXT8nICsga2V5ICsgJz1bYS16QS1aMC05XSsnKSwgJycpO1xufVxuXG5mdW5jdGlvbiBnZXRRdWVyeVN0cmluZ1ZhbHVlRnJvbVBhdGgocGF0aCwga2V5KSB7XG4gIHZhciBtYXRjaCA9IHBhdGgubWF0Y2gobmV3IFJlZ0V4cCgnXFxcXD8uKj9cXFxcYicgKyBrZXkgKyAnPSguKz8pXFxcXGInKSk7XG4gIHJldHVybiBtYXRjaCAmJiBtYXRjaFsxXTtcbn1cblxudmFyIERlZmF1bHRRdWVyeUtleSA9ICdfayc7XG5cbmZ1bmN0aW9uIGNyZWF0ZUhhc2hIaXN0b3J5KCkge1xuICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuXG4gICFfRXhlY3V0aW9uRW52aXJvbm1lbnQuY2FuVXNlRE9NID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UsICdIYXNoIGhpc3RvcnkgbmVlZHMgYSBET00nKSA6IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UpIDogdW5kZWZpbmVkO1xuXG4gIHZhciBxdWVyeUtleSA9IG9wdGlvbnMucXVlcnlLZXk7XG5cbiAgaWYgKHF1ZXJ5S2V5ID09PSB1bmRlZmluZWQgfHwgISFxdWVyeUtleSkgcXVlcnlLZXkgPSB0eXBlb2YgcXVlcnlLZXkgPT09ICdzdHJpbmcnID8gcXVlcnlLZXkgOiBEZWZhdWx0UXVlcnlLZXk7XG5cbiAgZnVuY3Rpb24gZ2V0Q3VycmVudExvY2F0aW9uKCkge1xuICAgIHZhciBwYXRoID0gX0RPTVV0aWxzLmdldEhhc2hQYXRoKCk7XG5cbiAgICB2YXIga2V5ID0gdW5kZWZpbmVkLFxuICAgICAgICBzdGF0ZSA9IHVuZGVmaW5lZDtcbiAgICBpZiAocXVlcnlLZXkpIHtcbiAgICAgIGtleSA9IGdldFF1ZXJ5U3RyaW5nVmFsdWVGcm9tUGF0aChwYXRoLCBxdWVyeUtleSk7XG4gICAgICBwYXRoID0gc3RyaXBRdWVyeVN0cmluZ1ZhbHVlRnJvbVBhdGgocGF0aCwgcXVlcnlLZXkpO1xuXG4gICAgICBpZiAoa2V5KSB7XG4gICAgICAgIHN0YXRlID0gX0RPTVN0YXRlU3RvcmFnZS5yZWFkU3RhdGUoa2V5KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHN0YXRlID0gbnVsbDtcbiAgICAgICAga2V5ID0gaGlzdG9yeS5jcmVhdGVLZXkoKTtcbiAgICAgICAgX0RPTVV0aWxzLnJlcGxhY2VIYXNoUGF0aChhZGRRdWVyeVN0cmluZ1ZhbHVlVG9QYXRoKHBhdGgsIHF1ZXJ5S2V5LCBrZXkpKTtcbiAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAga2V5ID0gc3RhdGUgPSBudWxsO1xuICAgIH1cblxuICAgIHZhciBsb2NhdGlvbiA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgcmV0dXJuIGhpc3RvcnkuY3JlYXRlTG9jYXRpb24oX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7IHN0YXRlOiBzdGF0ZSB9KSwgdW5kZWZpbmVkLCBrZXkpO1xuICB9XG5cbiAgZnVuY3Rpb24gc3RhcnRIYXNoQ2hhbmdlTGlzdGVuZXIoX3JlZikge1xuICAgIHZhciB0cmFuc2l0aW9uVG8gPSBfcmVmLnRyYW5zaXRpb25UbztcblxuICAgIGZ1bmN0aW9uIGhhc2hDaGFuZ2VMaXN0ZW5lcigpIHtcbiAgICAgIGlmICghZW5zdXJlU2xhc2goKSkgcmV0dXJuOyAvLyBBbHdheXMgbWFrZSBzdXJlIGhhc2hlcyBhcmUgcHJlY2VlZGVkIHdpdGggYSAvLlxuXG4gICAgICB0cmFuc2l0aW9uVG8oZ2V0Q3VycmVudExvY2F0aW9uKCkpO1xuICAgIH1cblxuICAgIGVuc3VyZVNsYXNoKCk7XG4gICAgX0RPTVV0aWxzLmFkZEV2ZW50TGlzdGVuZXIod2luZG93LCAnaGFzaGNoYW5nZScsIGhhc2hDaGFuZ2VMaXN0ZW5lcik7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgX0RPTVV0aWxzLnJlbW92ZUV2ZW50TGlzdGVuZXIod2luZG93LCAnaGFzaGNoYW5nZScsIGhhc2hDaGFuZ2VMaXN0ZW5lcik7XG4gICAgfTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGZpbmlzaFRyYW5zaXRpb24obG9jYXRpb24pIHtcbiAgICB2YXIgYmFzZW5hbWUgPSBsb2NhdGlvbi5iYXNlbmFtZTtcbiAgICB2YXIgcGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZTtcbiAgICB2YXIgc2VhcmNoID0gbG9jYXRpb24uc2VhcmNoO1xuICAgIHZhciBzdGF0ZSA9IGxvY2F0aW9uLnN0YXRlO1xuICAgIHZhciBhY3Rpb24gPSBsb2NhdGlvbi5hY3Rpb247XG4gICAgdmFyIGtleSA9IGxvY2F0aW9uLmtleTtcblxuICAgIGlmIChhY3Rpb24gPT09IF9BY3Rpb25zLlBPUCkgcmV0dXJuOyAvLyBOb3RoaW5nIHRvIGRvLlxuXG4gICAgdmFyIHBhdGggPSAoYmFzZW5hbWUgfHwgJycpICsgcGF0aG5hbWUgKyBzZWFyY2g7XG5cbiAgICBpZiAocXVlcnlLZXkpIHtcbiAgICAgIHBhdGggPSBhZGRRdWVyeVN0cmluZ1ZhbHVlVG9QYXRoKHBhdGgsIHF1ZXJ5S2V5LCBrZXkpO1xuICAgICAgX0RPTVN0YXRlU3RvcmFnZS5zYXZlU3RhdGUoa2V5LCBzdGF0ZSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIC8vIERyb3Aga2V5IGFuZCBzdGF0ZS5cbiAgICAgIGxvY2F0aW9uLmtleSA9IGxvY2F0aW9uLnN0YXRlID0gbnVsbDtcbiAgICB9XG5cbiAgICB2YXIgY3VycmVudEhhc2ggPSBfRE9NVXRpbHMuZ2V0SGFzaFBhdGgoKTtcblxuICAgIGlmIChhY3Rpb24gPT09IF9BY3Rpb25zLlBVU0gpIHtcbiAgICAgIGlmIChjdXJyZW50SGFzaCAhPT0gcGF0aCkge1xuICAgICAgICB3aW5kb3cubG9jYXRpb24uaGFzaCA9IHBhdGg7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdZb3UgY2Fubm90IFBVU0ggdGhlIHNhbWUgcGF0aCB1c2luZyBoYXNoIGhpc3RvcnknKSA6IHVuZGVmaW5lZDtcbiAgICAgIH1cbiAgICB9IGVsc2UgaWYgKGN1cnJlbnRIYXNoICE9PSBwYXRoKSB7XG4gICAgICAvLyBSRVBMQUNFXG4gICAgICBfRE9NVXRpbHMucmVwbGFjZUhhc2hQYXRoKHBhdGgpO1xuICAgIH1cbiAgfVxuXG4gIHZhciBoaXN0b3J5ID0gX2NyZWF0ZURPTUhpc3RvcnkyWydkZWZhdWx0J10oX2V4dGVuZHMoe30sIG9wdGlvbnMsIHtcbiAgICBnZXRDdXJyZW50TG9jYXRpb246IGdldEN1cnJlbnRMb2NhdGlvbixcbiAgICBmaW5pc2hUcmFuc2l0aW9uOiBmaW5pc2hUcmFuc2l0aW9uLFxuICAgIHNhdmVTdGF0ZTogX0RPTVN0YXRlU3RvcmFnZS5zYXZlU3RhdGVcbiAgfSkpO1xuXG4gIHZhciBsaXN0ZW5lckNvdW50ID0gMCxcbiAgICAgIHN0b3BIYXNoQ2hhbmdlTGlzdGVuZXIgPSB1bmRlZmluZWQ7XG5cbiAgZnVuY3Rpb24gbGlzdGVuQmVmb3JlKGxpc3RlbmVyKSB7XG4gICAgaWYgKCsrbGlzdGVuZXJDb3VudCA9PT0gMSkgc3RvcEhhc2hDaGFuZ2VMaXN0ZW5lciA9IHN0YXJ0SGFzaENoYW5nZUxpc3RlbmVyKGhpc3RvcnkpO1xuXG4gICAgdmFyIHVubGlzdGVuID0gaGlzdG9yeS5saXN0ZW5CZWZvcmUobGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIHVubGlzdGVuKCk7XG5cbiAgICAgIGlmICgtLWxpc3RlbmVyQ291bnQgPT09IDApIHN0b3BIYXNoQ2hhbmdlTGlzdGVuZXIoKTtcbiAgICB9O1xuICB9XG5cbiAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgaWYgKCsrbGlzdGVuZXJDb3VudCA9PT0gMSkgc3RvcEhhc2hDaGFuZ2VMaXN0ZW5lciA9IHN0YXJ0SGFzaENoYW5nZUxpc3RlbmVyKGhpc3RvcnkpO1xuXG4gICAgdmFyIHVubGlzdGVuID0gaGlzdG9yeS5saXN0ZW4obGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIHVubGlzdGVuKCk7XG5cbiAgICAgIGlmICgtLWxpc3RlbmVyQ291bnQgPT09IDApIHN0b3BIYXNoQ2hhbmdlTGlzdGVuZXIoKTtcbiAgICB9O1xuICB9XG5cbiAgZnVuY3Rpb24gcHVzaChsb2NhdGlvbikge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShxdWVyeUtleSB8fCBsb2NhdGlvbi5zdGF0ZSA9PSBudWxsLCAnWW91IGNhbm5vdCB1c2Ugc3RhdGUgd2l0aG91dCBhIHF1ZXJ5S2V5IGl0IHdpbGwgYmUgZHJvcHBlZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgaGlzdG9yeS5wdXNoKGxvY2F0aW9uKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIHJlcGxhY2UobG9jYXRpb24pIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10ocXVlcnlLZXkgfHwgbG9jYXRpb24uc3RhdGUgPT0gbnVsbCwgJ1lvdSBjYW5ub3QgdXNlIHN0YXRlIHdpdGhvdXQgYSBxdWVyeUtleSBpdCB3aWxsIGJlIGRyb3BwZWQnKSA6IHVuZGVmaW5lZDtcblxuICAgIGhpc3RvcnkucmVwbGFjZShsb2NhdGlvbik7XG4gIH1cblxuICB2YXIgZ29Jc1N1cHBvcnRlZFdpdGhvdXRSZWxvYWQgPSBfRE9NVXRpbHMuc3VwcG9ydHNHb1dpdGhvdXRSZWxvYWRVc2luZ0hhc2goKTtcblxuICBmdW5jdGlvbiBnbyhuKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGdvSXNTdXBwb3J0ZWRXaXRob3V0UmVsb2FkLCAnSGFzaCBoaXN0b3J5IGdvKG4pIGNhdXNlcyBhIGZ1bGwgcGFnZSByZWxvYWQgaW4gdGhpcyBicm93c2VyJykgOiB1bmRlZmluZWQ7XG5cbiAgICBoaXN0b3J5LmdvKG4pO1xuICB9XG5cbiAgZnVuY3Rpb24gY3JlYXRlSHJlZihwYXRoKSB7XG4gICAgcmV0dXJuICcjJyArIGhpc3RvcnkuY3JlYXRlSHJlZihwYXRoKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gcmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKSB7XG4gICAgaWYgKCsrbGlzdGVuZXJDb3VudCA9PT0gMSkgc3RvcEhhc2hDaGFuZ2VMaXN0ZW5lciA9IHN0YXJ0SGFzaENoYW5nZUxpc3RlbmVyKGhpc3RvcnkpO1xuXG4gICAgaGlzdG9yeS5yZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vaykge1xuICAgIGhpc3RvcnkudW5yZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spO1xuXG4gICAgaWYgKC0tbGlzdGVuZXJDb3VudCA9PT0gMCkgc3RvcEhhc2hDaGFuZ2VMaXN0ZW5lcigpO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiBwdXNoU3RhdGUoc3RhdGUsIHBhdGgpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10ocXVlcnlLZXkgfHwgc3RhdGUgPT0gbnVsbCwgJ1lvdSBjYW5ub3QgdXNlIHN0YXRlIHdpdGhvdXQgYSBxdWVyeUtleSBpdCB3aWxsIGJlIGRyb3BwZWQnKSA6IHVuZGVmaW5lZDtcblxuICAgIGhpc3RvcnkucHVzaFN0YXRlKHN0YXRlLCBwYXRoKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gcmVwbGFjZVN0YXRlKHN0YXRlLCBwYXRoKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKHF1ZXJ5S2V5IHx8IHN0YXRlID09IG51bGwsICdZb3UgY2Fubm90IHVzZSBzdGF0ZSB3aXRob3V0IGEgcXVlcnlLZXkgaXQgd2lsbCBiZSBkcm9wcGVkJykgOiB1bmRlZmluZWQ7XG5cbiAgICBoaXN0b3J5LnJlcGxhY2VTdGF0ZShzdGF0ZSwgcGF0aCk7XG4gIH1cblxuICByZXR1cm4gX2V4dGVuZHMoe30sIGhpc3RvcnksIHtcbiAgICBsaXN0ZW5CZWZvcmU6IGxpc3RlbkJlZm9yZSxcbiAgICBsaXN0ZW46IGxpc3RlbixcbiAgICBwdXNoOiBwdXNoLFxuICAgIHJlcGxhY2U6IHJlcGxhY2UsXG4gICAgZ286IGdvLFxuICAgIGNyZWF0ZUhyZWY6IGNyZWF0ZUhyZWYsXG5cbiAgICByZWdpc3RlclRyYW5zaXRpb25Ib29rOiByZWdpc3RlclRyYW5zaXRpb25Ib29rLCAvLyBkZXByZWNhdGVkIC0gd2FybmluZyBpcyBpbiBjcmVhdGVIaXN0b3J5XG4gICAgdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rOiB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2ssIC8vIGRlcHJlY2F0ZWQgLSB3YXJuaW5nIGlzIGluIGNyZWF0ZUhpc3RvcnlcbiAgICBwdXNoU3RhdGU6IHB1c2hTdGF0ZSwgLy8gZGVwcmVjYXRlZCAtIHdhcm5pbmcgaXMgaW4gY3JlYXRlSGlzdG9yeVxuICAgIHJlcGxhY2VTdGF0ZTogcmVwbGFjZVN0YXRlIC8vIGRlcHJlY2F0ZWQgLSB3YXJuaW5nIGlzIGluIGNyZWF0ZUhpc3RvcnlcbiAgfSk7XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGNyZWF0ZUhhc2hIaXN0b3J5O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG52YXIgX2RlZXBFcXVhbCA9IHJlcXVpcmUoJ2RlZXAtZXF1YWwnKTtcblxudmFyIF9kZWVwRXF1YWwyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZGVlcEVxdWFsKTtcblxudmFyIF9QYXRoVXRpbHMgPSByZXF1aXJlKCcuL1BhdGhVdGlscycpO1xuXG52YXIgX0FzeW5jVXRpbHMgPSByZXF1aXJlKCcuL0FzeW5jVXRpbHMnKTtcblxudmFyIF9BY3Rpb25zID0gcmVxdWlyZSgnLi9BY3Rpb25zJyk7XG5cbnZhciBfY3JlYXRlTG9jYXRpb24yID0gcmVxdWlyZSgnLi9jcmVhdGVMb2NhdGlvbicpO1xuXG52YXIgX2NyZWF0ZUxvY2F0aW9uMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZUxvY2F0aW9uMik7XG5cbnZhciBfcnVuVHJhbnNpdGlvbkhvb2sgPSByZXF1aXJlKCcuL3J1blRyYW5zaXRpb25Ib29rJyk7XG5cbnZhciBfcnVuVHJhbnNpdGlvbkhvb2syID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcnVuVHJhbnNpdGlvbkhvb2spO1xuXG52YXIgX2RlcHJlY2F0ZSA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlJyk7XG5cbnZhciBfZGVwcmVjYXRlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2RlcHJlY2F0ZSk7XG5cbmZ1bmN0aW9uIGNyZWF0ZVJhbmRvbUtleShsZW5ndGgpIHtcbiAgcmV0dXJuIE1hdGgucmFuZG9tKCkudG9TdHJpbmcoMzYpLnN1YnN0cigyLCBsZW5ndGgpO1xufVxuXG5mdW5jdGlvbiBsb2NhdGlvbnNBcmVFcXVhbChhLCBiKSB7XG4gIHJldHVybiBhLnBhdGhuYW1lID09PSBiLnBhdGhuYW1lICYmIGEuc2VhcmNoID09PSBiLnNlYXJjaCAmJlxuICAvL2EuYWN0aW9uID09PSBiLmFjdGlvbiAmJiAvLyBEaWZmZXJlbnQgYWN0aW9uICE9PSBsb2NhdGlvbiBjaGFuZ2UuXG4gIGEua2V5ID09PSBiLmtleSAmJiBfZGVlcEVxdWFsMlsnZGVmYXVsdCddKGEuc3RhdGUsIGIuc3RhdGUpO1xufVxuXG52YXIgRGVmYXVsdEtleUxlbmd0aCA9IDY7XG5cbmZ1bmN0aW9uIGNyZWF0ZUhpc3RvcnkoKSB7XG4gIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8ge30gOiBhcmd1bWVudHNbMF07XG4gIHZhciBnZXRDdXJyZW50TG9jYXRpb24gPSBvcHRpb25zLmdldEN1cnJlbnRMb2NhdGlvbjtcbiAgdmFyIGZpbmlzaFRyYW5zaXRpb24gPSBvcHRpb25zLmZpbmlzaFRyYW5zaXRpb247XG4gIHZhciBzYXZlU3RhdGUgPSBvcHRpb25zLnNhdmVTdGF0ZTtcbiAgdmFyIGdvID0gb3B0aW9ucy5nbztcbiAgdmFyIGdldFVzZXJDb25maXJtYXRpb24gPSBvcHRpb25zLmdldFVzZXJDb25maXJtYXRpb247XG4gIHZhciBrZXlMZW5ndGggPSBvcHRpb25zLmtleUxlbmd0aDtcblxuICBpZiAodHlwZW9mIGtleUxlbmd0aCAhPT0gJ251bWJlcicpIGtleUxlbmd0aCA9IERlZmF1bHRLZXlMZW5ndGg7XG5cbiAgdmFyIHRyYW5zaXRpb25Ib29rcyA9IFtdO1xuXG4gIGZ1bmN0aW9uIGxpc3RlbkJlZm9yZShob29rKSB7XG4gICAgdHJhbnNpdGlvbkhvb2tzLnB1c2goaG9vayk7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgdHJhbnNpdGlvbkhvb2tzID0gdHJhbnNpdGlvbkhvb2tzLmZpbHRlcihmdW5jdGlvbiAoaXRlbSkge1xuICAgICAgICByZXR1cm4gaXRlbSAhPT0gaG9vaztcbiAgICAgIH0pO1xuICAgIH07XG4gIH1cblxuICB2YXIgYWxsS2V5cyA9IFtdO1xuICB2YXIgY2hhbmdlTGlzdGVuZXJzID0gW107XG4gIHZhciBsb2NhdGlvbiA9IHVuZGVmaW5lZDtcblxuICBmdW5jdGlvbiBnZXRDdXJyZW50KCkge1xuICAgIGlmIChwZW5kaW5nTG9jYXRpb24gJiYgcGVuZGluZ0xvY2F0aW9uLmFjdGlvbiA9PT0gX0FjdGlvbnMuUE9QKSB7XG4gICAgICByZXR1cm4gYWxsS2V5cy5pbmRleE9mKHBlbmRpbmdMb2NhdGlvbi5rZXkpO1xuICAgIH0gZWxzZSBpZiAobG9jYXRpb24pIHtcbiAgICAgIHJldHVybiBhbGxLZXlzLmluZGV4T2YobG9jYXRpb24ua2V5KTtcbiAgICB9IGVsc2Uge1xuICAgICAgcmV0dXJuIC0xO1xuICAgIH1cbiAgfVxuXG4gIGZ1bmN0aW9uIHVwZGF0ZUxvY2F0aW9uKG5ld0xvY2F0aW9uKSB7XG4gICAgdmFyIGN1cnJlbnQgPSBnZXRDdXJyZW50KCk7XG5cbiAgICBsb2NhdGlvbiA9IG5ld0xvY2F0aW9uO1xuXG4gICAgaWYgKGxvY2F0aW9uLmFjdGlvbiA9PT0gX0FjdGlvbnMuUFVTSCkge1xuICAgICAgYWxsS2V5cyA9IFtdLmNvbmNhdChhbGxLZXlzLnNsaWNlKDAsIGN1cnJlbnQgKyAxKSwgW2xvY2F0aW9uLmtleV0pO1xuICAgIH0gZWxzZSBpZiAobG9jYXRpb24uYWN0aW9uID09PSBfQWN0aW9ucy5SRVBMQUNFKSB7XG4gICAgICBhbGxLZXlzW2N1cnJlbnRdID0gbG9jYXRpb24ua2V5O1xuICAgIH1cblxuICAgIGNoYW5nZUxpc3RlbmVycy5mb3JFYWNoKGZ1bmN0aW9uIChsaXN0ZW5lcikge1xuICAgICAgbGlzdGVuZXIobG9jYXRpb24pO1xuICAgIH0pO1xuICB9XG5cbiAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgY2hhbmdlTGlzdGVuZXJzLnB1c2gobGlzdGVuZXIpO1xuXG4gICAgaWYgKGxvY2F0aW9uKSB7XG4gICAgICBsaXN0ZW5lcihsb2NhdGlvbik7XG4gICAgfSBlbHNlIHtcbiAgICAgIHZhciBfbG9jYXRpb24gPSBnZXRDdXJyZW50TG9jYXRpb24oKTtcbiAgICAgIGFsbEtleXMgPSBbX2xvY2F0aW9uLmtleV07XG4gICAgICB1cGRhdGVMb2NhdGlvbihfbG9jYXRpb24pO1xuICAgIH1cblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICBjaGFuZ2VMaXN0ZW5lcnMgPSBjaGFuZ2VMaXN0ZW5lcnMuZmlsdGVyKGZ1bmN0aW9uIChpdGVtKSB7XG4gICAgICAgIHJldHVybiBpdGVtICE9PSBsaXN0ZW5lcjtcbiAgICAgIH0pO1xuICAgIH07XG4gIH1cblxuICBmdW5jdGlvbiBjb25maXJtVHJhbnNpdGlvblRvKGxvY2F0aW9uLCBjYWxsYmFjaykge1xuICAgIF9Bc3luY1V0aWxzLmxvb3BBc3luYyh0cmFuc2l0aW9uSG9va3MubGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIG5leHQsIGRvbmUpIHtcbiAgICAgIF9ydW5UcmFuc2l0aW9uSG9vazJbJ2RlZmF1bHQnXSh0cmFuc2l0aW9uSG9va3NbaW5kZXhdLCBsb2NhdGlvbiwgZnVuY3Rpb24gKHJlc3VsdCkge1xuICAgICAgICBpZiAocmVzdWx0ICE9IG51bGwpIHtcbiAgICAgICAgICBkb25lKHJlc3VsdCk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgbmV4dCgpO1xuICAgICAgICB9XG4gICAgICB9KTtcbiAgICB9LCBmdW5jdGlvbiAobWVzc2FnZSkge1xuICAgICAgaWYgKGdldFVzZXJDb25maXJtYXRpb24gJiYgdHlwZW9mIG1lc3NhZ2UgPT09ICdzdHJpbmcnKSB7XG4gICAgICAgIGdldFVzZXJDb25maXJtYXRpb24obWVzc2FnZSwgZnVuY3Rpb24gKG9rKSB7XG4gICAgICAgICAgY2FsbGJhY2sob2sgIT09IGZhbHNlKTtcbiAgICAgICAgfSk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBjYWxsYmFjayhtZXNzYWdlICE9PSBmYWxzZSk7XG4gICAgICB9XG4gICAgfSk7XG4gIH1cblxuICB2YXIgcGVuZGluZ0xvY2F0aW9uID0gdW5kZWZpbmVkO1xuXG4gIGZ1bmN0aW9uIHRyYW5zaXRpb25UbyhuZXh0TG9jYXRpb24pIHtcbiAgICBpZiAobG9jYXRpb24gJiYgbG9jYXRpb25zQXJlRXF1YWwobG9jYXRpb24sIG5leHRMb2NhdGlvbikpIHJldHVybjsgLy8gTm90aGluZyB0byBkby5cblxuICAgIHBlbmRpbmdMb2NhdGlvbiA9IG5leHRMb2NhdGlvbjtcblxuICAgIGNvbmZpcm1UcmFuc2l0aW9uVG8obmV4dExvY2F0aW9uLCBmdW5jdGlvbiAob2spIHtcbiAgICAgIGlmIChwZW5kaW5nTG9jYXRpb24gIT09IG5leHRMb2NhdGlvbikgcmV0dXJuOyAvLyBUcmFuc2l0aW9uIHdhcyBpbnRlcnJ1cHRlZC5cblxuICAgICAgaWYgKG9rKSB7XG4gICAgICAgIC8vIHRyZWF0IFBVU0ggdG8gY3VycmVudCBwYXRoIGxpa2UgUkVQTEFDRSB0byBiZSBjb25zaXN0ZW50IHdpdGggYnJvd3NlcnNcbiAgICAgICAgaWYgKG5leHRMb2NhdGlvbi5hY3Rpb24gPT09IF9BY3Rpb25zLlBVU0gpIHtcbiAgICAgICAgICB2YXIgcHJldlBhdGggPSBjcmVhdGVQYXRoKGxvY2F0aW9uKTtcbiAgICAgICAgICB2YXIgbmV4dFBhdGggPSBjcmVhdGVQYXRoKG5leHRMb2NhdGlvbik7XG5cbiAgICAgICAgICBpZiAobmV4dFBhdGggPT09IHByZXZQYXRoICYmIF9kZWVwRXF1YWwyWydkZWZhdWx0J10obG9jYXRpb24uc3RhdGUsIG5leHRMb2NhdGlvbi5zdGF0ZSkpIG5leHRMb2NhdGlvbi5hY3Rpb24gPSBfQWN0aW9ucy5SRVBMQUNFO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKGZpbmlzaFRyYW5zaXRpb24obmV4dExvY2F0aW9uKSAhPT0gZmFsc2UpIHVwZGF0ZUxvY2F0aW9uKG5leHRMb2NhdGlvbik7XG4gICAgICB9IGVsc2UgaWYgKGxvY2F0aW9uICYmIG5leHRMb2NhdGlvbi5hY3Rpb24gPT09IF9BY3Rpb25zLlBPUCkge1xuICAgICAgICB2YXIgcHJldkluZGV4ID0gYWxsS2V5cy5pbmRleE9mKGxvY2F0aW9uLmtleSk7XG4gICAgICAgIHZhciBuZXh0SW5kZXggPSBhbGxLZXlzLmluZGV4T2YobmV4dExvY2F0aW9uLmtleSk7XG5cbiAgICAgICAgaWYgKHByZXZJbmRleCAhPT0gLTEgJiYgbmV4dEluZGV4ICE9PSAtMSkgZ28ocHJldkluZGV4IC0gbmV4dEluZGV4KTsgLy8gUmVzdG9yZSB0aGUgVVJMLlxuICAgICAgfVxuICAgIH0pO1xuICB9XG5cbiAgZnVuY3Rpb24gcHVzaChsb2NhdGlvbikge1xuICAgIHRyYW5zaXRpb25UbyhjcmVhdGVMb2NhdGlvbihsb2NhdGlvbiwgX0FjdGlvbnMuUFVTSCwgY3JlYXRlS2V5KCkpKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIHJlcGxhY2UobG9jYXRpb24pIHtcbiAgICB0cmFuc2l0aW9uVG8oY3JlYXRlTG9jYXRpb24obG9jYXRpb24sIF9BY3Rpb25zLlJFUExBQ0UsIGNyZWF0ZUtleSgpKSk7XG4gIH1cblxuICBmdW5jdGlvbiBnb0JhY2soKSB7XG4gICAgZ28oLTEpO1xuICB9XG5cbiAgZnVuY3Rpb24gZ29Gb3J3YXJkKCkge1xuICAgIGdvKDEpO1xuICB9XG5cbiAgZnVuY3Rpb24gY3JlYXRlS2V5KCkge1xuICAgIHJldHVybiBjcmVhdGVSYW5kb21LZXkoa2V5TGVuZ3RoKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNyZWF0ZVBhdGgobG9jYXRpb24pIHtcbiAgICBpZiAobG9jYXRpb24gPT0gbnVsbCB8fCB0eXBlb2YgbG9jYXRpb24gPT09ICdzdHJpbmcnKSByZXR1cm4gbG9jYXRpb247XG5cbiAgICB2YXIgcGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZTtcbiAgICB2YXIgc2VhcmNoID0gbG9jYXRpb24uc2VhcmNoO1xuICAgIHZhciBoYXNoID0gbG9jYXRpb24uaGFzaDtcblxuICAgIHZhciByZXN1bHQgPSBwYXRobmFtZTtcblxuICAgIGlmIChzZWFyY2gpIHJlc3VsdCArPSBzZWFyY2g7XG5cbiAgICBpZiAoaGFzaCkgcmVzdWx0ICs9IGhhc2g7XG5cbiAgICByZXR1cm4gcmVzdWx0O1xuICB9XG5cbiAgZnVuY3Rpb24gY3JlYXRlSHJlZihsb2NhdGlvbikge1xuICAgIHJldHVybiBjcmVhdGVQYXRoKGxvY2F0aW9uKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNyZWF0ZUxvY2F0aW9uKGxvY2F0aW9uLCBhY3Rpb24pIHtcbiAgICB2YXIga2V5ID0gYXJndW1lbnRzLmxlbmd0aCA8PSAyIHx8IGFyZ3VtZW50c1syXSA9PT0gdW5kZWZpbmVkID8gY3JlYXRlS2V5KCkgOiBhcmd1bWVudHNbMl07XG5cbiAgICBpZiAodHlwZW9mIGFjdGlvbiA9PT0gJ29iamVjdCcpIHtcbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ1RoZSBzdGF0ZSAoMm5kKSBhcmd1bWVudCB0byBoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uIGlzIGRlcHJlY2F0ZWQ7IHVzZSBhICcgKyAnbG9jYXRpb24gZGVzY3JpcHRvciBpbnN0ZWFkJykgOiB1bmRlZmluZWQ7XG5cbiAgICAgIGlmICh0eXBlb2YgbG9jYXRpb24gPT09ICdzdHJpbmcnKSBsb2NhdGlvbiA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKGxvY2F0aW9uKTtcblxuICAgICAgbG9jYXRpb24gPSBfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIHsgc3RhdGU6IGFjdGlvbiB9KTtcblxuICAgICAgYWN0aW9uID0ga2V5O1xuICAgICAga2V5ID0gYXJndW1lbnRzWzNdIHx8IGNyZWF0ZUtleSgpO1xuICAgIH1cblxuICAgIHJldHVybiBfY3JlYXRlTG9jYXRpb24zWydkZWZhdWx0J10obG9jYXRpb24sIGFjdGlvbiwga2V5KTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gc2V0U3RhdGUoc3RhdGUpIHtcbiAgICBpZiAobG9jYXRpb24pIHtcbiAgICAgIHVwZGF0ZUxvY2F0aW9uU3RhdGUobG9jYXRpb24sIHN0YXRlKTtcbiAgICAgIHVwZGF0ZUxvY2F0aW9uKGxvY2F0aW9uKTtcbiAgICB9IGVsc2Uge1xuICAgICAgdXBkYXRlTG9jYXRpb25TdGF0ZShnZXRDdXJyZW50TG9jYXRpb24oKSwgc3RhdGUpO1xuICAgIH1cbiAgfVxuXG4gIGZ1bmN0aW9uIHVwZGF0ZUxvY2F0aW9uU3RhdGUobG9jYXRpb24sIHN0YXRlKSB7XG4gICAgbG9jYXRpb24uc3RhdGUgPSBfZXh0ZW5kcyh7fSwgbG9jYXRpb24uc3RhdGUsIHN0YXRlKTtcbiAgICBzYXZlU3RhdGUobG9jYXRpb24ua2V5LCBsb2NhdGlvbi5zdGF0ZSk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vaykge1xuICAgIGlmICh0cmFuc2l0aW9uSG9va3MuaW5kZXhPZihob29rKSA9PT0gLTEpIHRyYW5zaXRpb25Ib29rcy5wdXNoKGhvb2spO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vaykge1xuICAgIHRyYW5zaXRpb25Ib29rcyA9IHRyYW5zaXRpb25Ib29rcy5maWx0ZXIoZnVuY3Rpb24gKGl0ZW0pIHtcbiAgICAgIHJldHVybiBpdGVtICE9PSBob29rO1xuICAgIH0pO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiBwdXNoU3RhdGUoc3RhdGUsIHBhdGgpIHtcbiAgICBpZiAodHlwZW9mIHBhdGggPT09ICdzdHJpbmcnKSBwYXRoID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICBwdXNoKF9leHRlbmRzKHsgc3RhdGU6IHN0YXRlIH0sIHBhdGgpKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gcmVwbGFjZVN0YXRlKHN0YXRlLCBwYXRoKSB7XG4gICAgaWYgKHR5cGVvZiBwYXRoID09PSAnc3RyaW5nJykgcGF0aCA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgcmVwbGFjZShfZXh0ZW5kcyh7IHN0YXRlOiBzdGF0ZSB9LCBwYXRoKSk7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIGxpc3RlbkJlZm9yZTogbGlzdGVuQmVmb3JlLFxuICAgIGxpc3RlbjogbGlzdGVuLFxuICAgIHRyYW5zaXRpb25UbzogdHJhbnNpdGlvblRvLFxuICAgIHB1c2g6IHB1c2gsXG4gICAgcmVwbGFjZTogcmVwbGFjZSxcbiAgICBnbzogZ28sXG4gICAgZ29CYWNrOiBnb0JhY2ssXG4gICAgZ29Gb3J3YXJkOiBnb0ZvcndhcmQsXG4gICAgY3JlYXRlS2V5OiBjcmVhdGVLZXksXG4gICAgY3JlYXRlUGF0aDogY3JlYXRlUGF0aCxcbiAgICBjcmVhdGVIcmVmOiBjcmVhdGVIcmVmLFxuICAgIGNyZWF0ZUxvY2F0aW9uOiBjcmVhdGVMb2NhdGlvbixcblxuICAgIHNldFN0YXRlOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHNldFN0YXRlLCAnc2V0U3RhdGUgaXMgZGVwcmVjYXRlZDsgdXNlIGxvY2F0aW9uLmtleSB0byBzYXZlIHN0YXRlIGluc3RlYWQnKSxcbiAgICByZWdpc3RlclRyYW5zaXRpb25Ib29rOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2ssICdyZWdpc3RlclRyYW5zaXRpb25Ib29rIGlzIGRlcHJlY2F0ZWQ7IHVzZSBsaXN0ZW5CZWZvcmUgaW5zdGVhZCcpLFxuICAgIHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vazogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXSh1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2ssICd1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2sgaXMgZGVwcmVjYXRlZDsgdXNlIHRoZSBjYWxsYmFjayByZXR1cm5lZCBmcm9tIGxpc3RlbkJlZm9yZSBpbnN0ZWFkJyksXG4gICAgcHVzaFN0YXRlOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHB1c2hTdGF0ZSwgJ3B1c2hTdGF0ZSBpcyBkZXByZWNhdGVkOyB1c2UgcHVzaCBpbnN0ZWFkJyksXG4gICAgcmVwbGFjZVN0YXRlOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHJlcGxhY2VTdGF0ZSwgJ3JlcGxhY2VTdGF0ZSBpcyBkZXByZWNhdGVkOyB1c2UgcmVwbGFjZSBpbnN0ZWFkJylcbiAgfTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gY3JlYXRlSGlzdG9yeTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIF9BY3Rpb25zID0gcmVxdWlyZSgnLi9BY3Rpb25zJyk7XG5cbnZhciBfUGF0aFV0aWxzID0gcmVxdWlyZSgnLi9QYXRoVXRpbHMnKTtcblxuZnVuY3Rpb24gY3JlYXRlTG9jYXRpb24oKSB7XG4gIHZhciBsb2NhdGlvbiA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/ICcvJyA6IGFyZ3VtZW50c1swXTtcbiAgdmFyIGFjdGlvbiA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMSB8fCBhcmd1bWVudHNbMV0gPT09IHVuZGVmaW5lZCA/IF9BY3Rpb25zLlBPUCA6IGFyZ3VtZW50c1sxXTtcbiAgdmFyIGtleSA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMiB8fCBhcmd1bWVudHNbMl0gPT09IHVuZGVmaW5lZCA/IG51bGwgOiBhcmd1bWVudHNbMl07XG5cbiAgdmFyIF9mb3VydGhBcmcgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDMgfHwgYXJndW1lbnRzWzNdID09PSB1bmRlZmluZWQgPyBudWxsIDogYXJndW1lbnRzWzNdO1xuXG4gIGlmICh0eXBlb2YgbG9jYXRpb24gPT09ICdzdHJpbmcnKSBsb2NhdGlvbiA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKGxvY2F0aW9uKTtcblxuICBpZiAodHlwZW9mIGFjdGlvbiA9PT0gJ29iamVjdCcpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdUaGUgc3RhdGUgKDJuZCkgYXJndW1lbnQgdG8gY3JlYXRlTG9jYXRpb24gaXMgZGVwcmVjYXRlZDsgdXNlIGEgJyArICdsb2NhdGlvbiBkZXNjcmlwdG9yIGluc3RlYWQnKSA6IHVuZGVmaW5lZDtcblxuICAgIGxvY2F0aW9uID0gX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7IHN0YXRlOiBhY3Rpb24gfSk7XG5cbiAgICBhY3Rpb24gPSBrZXkgfHwgX0FjdGlvbnMuUE9QO1xuICAgIGtleSA9IF9mb3VydGhBcmc7XG4gIH1cblxuICB2YXIgcGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZSB8fCAnLyc7XG4gIHZhciBzZWFyY2ggPSBsb2NhdGlvbi5zZWFyY2ggfHwgJyc7XG4gIHZhciBoYXNoID0gbG9jYXRpb24uaGFzaCB8fCAnJztcbiAgdmFyIHN0YXRlID0gbG9jYXRpb24uc3RhdGUgfHwgbnVsbDtcblxuICByZXR1cm4ge1xuICAgIHBhdGhuYW1lOiBwYXRobmFtZSxcbiAgICBzZWFyY2g6IHNlYXJjaCxcbiAgICBoYXNoOiBoYXNoLFxuICAgIHN0YXRlOiBzdGF0ZSxcbiAgICBhY3Rpb246IGFjdGlvbixcbiAgICBrZXk6IGtleVxuICB9O1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBjcmVhdGVMb2NhdGlvbjtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9QYXRoVXRpbHMgPSByZXF1aXJlKCcuL1BhdGhVdGlscycpO1xuXG52YXIgX0FjdGlvbnMgPSByZXF1aXJlKCcuL0FjdGlvbnMnKTtcblxudmFyIF9jcmVhdGVIaXN0b3J5ID0gcmVxdWlyZSgnLi9jcmVhdGVIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlSGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVIaXN0b3J5KTtcblxuZnVuY3Rpb24gY3JlYXRlU3RhdGVTdG9yYWdlKGVudHJpZXMpIHtcbiAgcmV0dXJuIGVudHJpZXMuZmlsdGVyKGZ1bmN0aW9uIChlbnRyeSkge1xuICAgIHJldHVybiBlbnRyeS5zdGF0ZTtcbiAgfSkucmVkdWNlKGZ1bmN0aW9uIChtZW1vLCBlbnRyeSkge1xuICAgIG1lbW9bZW50cnkua2V5XSA9IGVudHJ5LnN0YXRlO1xuICAgIHJldHVybiBtZW1vO1xuICB9LCB7fSk7XG59XG5cbmZ1bmN0aW9uIGNyZWF0ZU1lbW9yeUhpc3RvcnkoKSB7XG4gIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8ge30gOiBhcmd1bWVudHNbMF07XG5cbiAgaWYgKEFycmF5LmlzQXJyYXkob3B0aW9ucykpIHtcbiAgICBvcHRpb25zID0geyBlbnRyaWVzOiBvcHRpb25zIH07XG4gIH0gZWxzZSBpZiAodHlwZW9mIG9wdGlvbnMgPT09ICdzdHJpbmcnKSB7XG4gICAgb3B0aW9ucyA9IHsgZW50cmllczogW29wdGlvbnNdIH07XG4gIH1cblxuICB2YXIgaGlzdG9yeSA9IF9jcmVhdGVIaXN0b3J5MlsnZGVmYXVsdCddKF9leHRlbmRzKHt9LCBvcHRpb25zLCB7XG4gICAgZ2V0Q3VycmVudExvY2F0aW9uOiBnZXRDdXJyZW50TG9jYXRpb24sXG4gICAgZmluaXNoVHJhbnNpdGlvbjogZmluaXNoVHJhbnNpdGlvbixcbiAgICBzYXZlU3RhdGU6IHNhdmVTdGF0ZSxcbiAgICBnbzogZ29cbiAgfSkpO1xuXG4gIHZhciBfb3B0aW9ucyA9IG9wdGlvbnM7XG4gIHZhciBlbnRyaWVzID0gX29wdGlvbnMuZW50cmllcztcbiAgdmFyIGN1cnJlbnQgPSBfb3B0aW9ucy5jdXJyZW50O1xuXG4gIGlmICh0eXBlb2YgZW50cmllcyA9PT0gJ3N0cmluZycpIHtcbiAgICBlbnRyaWVzID0gW2VudHJpZXNdO1xuICB9IGVsc2UgaWYgKCFBcnJheS5pc0FycmF5KGVudHJpZXMpKSB7XG4gICAgZW50cmllcyA9IFsnLyddO1xuICB9XG5cbiAgZW50cmllcyA9IGVudHJpZXMubWFwKGZ1bmN0aW9uIChlbnRyeSkge1xuICAgIHZhciBrZXkgPSBoaXN0b3J5LmNyZWF0ZUtleSgpO1xuXG4gICAgaWYgKHR5cGVvZiBlbnRyeSA9PT0gJ3N0cmluZycpIHJldHVybiB7IHBhdGhuYW1lOiBlbnRyeSwga2V5OiBrZXkgfTtcblxuICAgIGlmICh0eXBlb2YgZW50cnkgPT09ICdvYmplY3QnICYmIGVudHJ5KSByZXR1cm4gX2V4dGVuZHMoe30sIGVudHJ5LCB7IGtleToga2V5IH0pO1xuXG4gICAgIWZhbHNlID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UsICdVbmFibGUgdG8gY3JlYXRlIGhpc3RvcnkgZW50cnkgZnJvbSAlcycsIGVudHJ5KSA6IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UpIDogdW5kZWZpbmVkO1xuICB9KTtcblxuICBpZiAoY3VycmVudCA9PSBudWxsKSB7XG4gICAgY3VycmVudCA9IGVudHJpZXMubGVuZ3RoIC0gMTtcbiAgfSBlbHNlIHtcbiAgICAhKGN1cnJlbnQgPj0gMCAmJiBjdXJyZW50IDwgZW50cmllcy5sZW5ndGgpID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UsICdDdXJyZW50IGluZGV4IG11c3QgYmUgPj0gMCBhbmQgPCAlcywgd2FzICVzJywgZW50cmllcy5sZW5ndGgsIGN1cnJlbnQpIDogX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSkgOiB1bmRlZmluZWQ7XG4gIH1cblxuICB2YXIgc3RvcmFnZSA9IGNyZWF0ZVN0YXRlU3RvcmFnZShlbnRyaWVzKTtcblxuICBmdW5jdGlvbiBzYXZlU3RhdGUoa2V5LCBzdGF0ZSkge1xuICAgIHN0b3JhZ2Vba2V5XSA9IHN0YXRlO1xuICB9XG5cbiAgZnVuY3Rpb24gcmVhZFN0YXRlKGtleSkge1xuICAgIHJldHVybiBzdG9yYWdlW2tleV07XG4gIH1cblxuICBmdW5jdGlvbiBnZXRDdXJyZW50TG9jYXRpb24oKSB7XG4gICAgdmFyIGVudHJ5ID0gZW50cmllc1tjdXJyZW50XTtcbiAgICB2YXIgYmFzZW5hbWUgPSBlbnRyeS5iYXNlbmFtZTtcbiAgICB2YXIgcGF0aG5hbWUgPSBlbnRyeS5wYXRobmFtZTtcbiAgICB2YXIgc2VhcmNoID0gZW50cnkuc2VhcmNoO1xuXG4gICAgdmFyIHBhdGggPSAoYmFzZW5hbWUgfHwgJycpICsgcGF0aG5hbWUgKyAoc2VhcmNoIHx8ICcnKTtcblxuICAgIHZhciBrZXkgPSB1bmRlZmluZWQsXG4gICAgICAgIHN0YXRlID0gdW5kZWZpbmVkO1xuICAgIGlmIChlbnRyeS5rZXkpIHtcbiAgICAgIGtleSA9IGVudHJ5LmtleTtcbiAgICAgIHN0YXRlID0gcmVhZFN0YXRlKGtleSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIGtleSA9IGhpc3RvcnkuY3JlYXRlS2V5KCk7XG4gICAgICBzdGF0ZSA9IG51bGw7XG4gICAgICBlbnRyeS5rZXkgPSBrZXk7XG4gICAgfVxuXG4gICAgdmFyIGxvY2F0aW9uID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbihfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIHsgc3RhdGU6IHN0YXRlIH0pLCB1bmRlZmluZWQsIGtleSk7XG4gIH1cblxuICBmdW5jdGlvbiBjYW5HbyhuKSB7XG4gICAgdmFyIGluZGV4ID0gY3VycmVudCArIG47XG4gICAgcmV0dXJuIGluZGV4ID49IDAgJiYgaW5kZXggPCBlbnRyaWVzLmxlbmd0aDtcbiAgfVxuXG4gIGZ1bmN0aW9uIGdvKG4pIHtcbiAgICBpZiAobikge1xuICAgICAgaWYgKCFjYW5HbyhuKSkge1xuICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdDYW5ub3QgZ28oJXMpIHRoZXJlIGlzIG5vdCBlbm91Z2ggaGlzdG9yeScsIG4pIDogdW5kZWZpbmVkO1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG5cbiAgICAgIGN1cnJlbnQgKz0gbjtcblxuICAgICAgdmFyIGN1cnJlbnRMb2NhdGlvbiA9IGdldEN1cnJlbnRMb2NhdGlvbigpO1xuXG4gICAgICAvLyBjaGFuZ2UgYWN0aW9uIHRvIFBPUFxuICAgICAgaGlzdG9yeS50cmFuc2l0aW9uVG8oX2V4dGVuZHMoe30sIGN1cnJlbnRMb2NhdGlvbiwgeyBhY3Rpb246IF9BY3Rpb25zLlBPUCB9KSk7XG4gICAgfVxuICB9XG5cbiAgZnVuY3Rpb24gZmluaXNoVHJhbnNpdGlvbihsb2NhdGlvbikge1xuICAgIHN3aXRjaCAobG9jYXRpb24uYWN0aW9uKSB7XG4gICAgICBjYXNlIF9BY3Rpb25zLlBVU0g6XG4gICAgICAgIGN1cnJlbnQgKz0gMTtcblxuICAgICAgICAvLyBpZiB3ZSBhcmUgbm90IG9uIHRoZSB0b3Agb2Ygc3RhY2tcbiAgICAgICAgLy8gcmVtb3ZlIHJlc3QgYW5kIHB1c2ggbmV3XG4gICAgICAgIGlmIChjdXJyZW50IDwgZW50cmllcy5sZW5ndGgpIGVudHJpZXMuc3BsaWNlKGN1cnJlbnQpO1xuXG4gICAgICAgIGVudHJpZXMucHVzaChsb2NhdGlvbik7XG4gICAgICAgIHNhdmVTdGF0ZShsb2NhdGlvbi5rZXksIGxvY2F0aW9uLnN0YXRlKTtcbiAgICAgICAgYnJlYWs7XG4gICAgICBjYXNlIF9BY3Rpb25zLlJFUExBQ0U6XG4gICAgICAgIGVudHJpZXNbY3VycmVudF0gPSBsb2NhdGlvbjtcbiAgICAgICAgc2F2ZVN0YXRlKGxvY2F0aW9uLmtleSwgbG9jYXRpb24uc3RhdGUpO1xuICAgICAgICBicmVhaztcbiAgICB9XG4gIH1cblxuICByZXR1cm4gaGlzdG9yeTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gY3JlYXRlTWVtb3J5SGlzdG9yeTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gZGVwcmVjYXRlKGZuLCBtZXNzYWdlKSB7XG4gIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnW2hpc3RvcnldICcgKyBtZXNzYWdlKSA6IHVuZGVmaW5lZDtcbiAgICByZXR1cm4gZm4uYXBwbHkodGhpcywgYXJndW1lbnRzKTtcbiAgfTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gZGVwcmVjYXRlO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBydW5UcmFuc2l0aW9uSG9vayhob29rLCBsb2NhdGlvbiwgY2FsbGJhY2spIHtcbiAgdmFyIHJlc3VsdCA9IGhvb2sobG9jYXRpb24sIGNhbGxiYWNrKTtcblxuICBpZiAoaG9vay5sZW5ndGggPCAyKSB7XG4gICAgLy8gQXNzdW1lIHRoZSBob29rIHJ1bnMgc3luY2hyb25vdXNseSBhbmQgYXV0b21hdGljYWxseVxuICAgIC8vIGNhbGwgdGhlIGNhbGxiYWNrIHdpdGggdGhlIHJldHVybiB2YWx1ZS5cbiAgICBjYWxsYmFjayhyZXN1bHQpO1xuICB9IGVsc2Uge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShyZXN1bHQgPT09IHVuZGVmaW5lZCwgJ1lvdSBzaG91bGQgbm90IFwicmV0dXJuXCIgaW4gYSB0cmFuc2l0aW9uIGhvb2sgd2l0aCBhIGNhbGxiYWNrIGFyZ3VtZW50OyBjYWxsIHRoZSBjYWxsYmFjayBpbnN0ZWFkJykgOiB1bmRlZmluZWQ7XG4gIH1cbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gcnVuVHJhbnNpdGlvbkhvb2s7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfRXhlY3V0aW9uRW52aXJvbm1lbnQgPSByZXF1aXJlKCcuL0V4ZWN1dGlvbkVudmlyb25tZW50Jyk7XG5cbnZhciBfUGF0aFV0aWxzID0gcmVxdWlyZSgnLi9QYXRoVXRpbHMnKTtcblxudmFyIF9ydW5UcmFuc2l0aW9uSG9vayA9IHJlcXVpcmUoJy4vcnVuVHJhbnNpdGlvbkhvb2snKTtcblxudmFyIF9ydW5UcmFuc2l0aW9uSG9vazIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9ydW5UcmFuc2l0aW9uSG9vayk7XG5cbnZhciBfZGVwcmVjYXRlID0gcmVxdWlyZSgnLi9kZXByZWNhdGUnKTtcblxudmFyIF9kZXByZWNhdGUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZGVwcmVjYXRlKTtcblxuZnVuY3Rpb24gdXNlQmFzZW5hbWUoY3JlYXRlSGlzdG9yeSkge1xuICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8ge30gOiBhcmd1bWVudHNbMF07XG5cbiAgICB2YXIgaGlzdG9yeSA9IGNyZWF0ZUhpc3Rvcnkob3B0aW9ucyk7XG5cbiAgICB2YXIgYmFzZW5hbWUgPSBvcHRpb25zLmJhc2VuYW1lO1xuXG4gICAgdmFyIGNoZWNrZWRCYXNlSHJlZiA9IGZhbHNlO1xuXG4gICAgZnVuY3Rpb24gY2hlY2tCYXNlSHJlZigpIHtcbiAgICAgIGlmIChjaGVja2VkQmFzZUhyZWYpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuXG4gICAgICAvLyBBdXRvbWF0aWNhbGx5IHVzZSB0aGUgdmFsdWUgb2YgPGJhc2UgaHJlZj4gaW4gSFRNTFxuICAgICAgLy8gZG9jdW1lbnRzIGFzIGJhc2VuYW1lIGlmIGl0J3Mgbm90IGV4cGxpY2l0bHkgZ2l2ZW4uXG4gICAgICBpZiAoYmFzZW5hbWUgPT0gbnVsbCAmJiBfRXhlY3V0aW9uRW52aXJvbm1lbnQuY2FuVXNlRE9NKSB7XG4gICAgICAgIHZhciBiYXNlID0gZG9jdW1lbnQuZ2V0RWxlbWVudHNCeVRhZ05hbWUoJ2Jhc2UnKVswXTtcbiAgICAgICAgdmFyIGJhc2VIcmVmID0gYmFzZSAmJiBiYXNlLmdldEF0dHJpYnV0ZSgnaHJlZicpO1xuXG4gICAgICAgIGlmIChiYXNlSHJlZiAhPSBudWxsKSB7XG4gICAgICAgICAgYmFzZW5hbWUgPSBiYXNlSHJlZjtcblxuICAgICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ0F1dG9tYXRpY2FsbHkgc2V0dGluZyBiYXNlbmFtZSB1c2luZyA8YmFzZSBocmVmPiBpcyBkZXByZWNhdGVkIGFuZCB3aWxsICcgKyAnYmUgcmVtb3ZlZCBpbiB0aGUgbmV4dCBtYWpvciByZWxlYXNlLiBUaGUgc2VtYW50aWNzIG9mIDxiYXNlIGhyZWY+IGFyZSAnICsgJ3N1YnRseSBkaWZmZXJlbnQgZnJvbSBiYXNlbmFtZS4gUGxlYXNlIHBhc3MgdGhlIGJhc2VuYW1lIGV4cGxpY2l0bHkgaW4gJyArICd0aGUgb3B0aW9ucyB0byBjcmVhdGVIaXN0b3J5JykgOiB1bmRlZmluZWQ7XG4gICAgICAgIH1cbiAgICAgIH1cblxuICAgICAgY2hlY2tlZEJhc2VIcmVmID0gdHJ1ZTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBhZGRCYXNlbmFtZShsb2NhdGlvbikge1xuICAgICAgY2hlY2tCYXNlSHJlZigpO1xuXG4gICAgICBpZiAoYmFzZW5hbWUgJiYgbG9jYXRpb24uYmFzZW5hbWUgPT0gbnVsbCkge1xuICAgICAgICBpZiAobG9jYXRpb24ucGF0aG5hbWUuaW5kZXhPZihiYXNlbmFtZSkgPT09IDApIHtcbiAgICAgICAgICBsb2NhdGlvbi5wYXRobmFtZSA9IGxvY2F0aW9uLnBhdGhuYW1lLnN1YnN0cmluZyhiYXNlbmFtZS5sZW5ndGgpO1xuICAgICAgICAgIGxvY2F0aW9uLmJhc2VuYW1lID0gYmFzZW5hbWU7XG5cbiAgICAgICAgICBpZiAobG9jYXRpb24ucGF0aG5hbWUgPT09ICcnKSBsb2NhdGlvbi5wYXRobmFtZSA9ICcvJztcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBsb2NhdGlvbi5iYXNlbmFtZSA9ICcnO1xuICAgICAgICB9XG4gICAgICB9XG5cbiAgICAgIHJldHVybiBsb2NhdGlvbjtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBwcmVwZW5kQmFzZW5hbWUobG9jYXRpb24pIHtcbiAgICAgIGNoZWNrQmFzZUhyZWYoKTtcblxuICAgICAgaWYgKCFiYXNlbmFtZSkgcmV0dXJuIGxvY2F0aW9uO1xuXG4gICAgICBpZiAodHlwZW9mIGxvY2F0aW9uID09PSAnc3RyaW5nJykgbG9jYXRpb24gPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChsb2NhdGlvbik7XG5cbiAgICAgIHZhciBwbmFtZSA9IGxvY2F0aW9uLnBhdGhuYW1lO1xuICAgICAgdmFyIG5vcm1hbGl6ZWRCYXNlbmFtZSA9IGJhc2VuYW1lLnNsaWNlKC0xKSA9PT0gJy8nID8gYmFzZW5hbWUgOiBiYXNlbmFtZSArICcvJztcbiAgICAgIHZhciBub3JtYWxpemVkUGF0aG5hbWUgPSBwbmFtZS5jaGFyQXQoMCkgPT09ICcvJyA/IHBuYW1lLnNsaWNlKDEpIDogcG5hbWU7XG4gICAgICB2YXIgcGF0aG5hbWUgPSBub3JtYWxpemVkQmFzZW5hbWUgKyBub3JtYWxpemVkUGF0aG5hbWU7XG5cbiAgICAgIHJldHVybiBfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIHtcbiAgICAgICAgcGF0aG5hbWU6IHBhdGhuYW1lXG4gICAgICB9KTtcbiAgICB9XG5cbiAgICAvLyBPdmVycmlkZSBhbGwgcmVhZCBtZXRob2RzIHdpdGggYmFzZW5hbWUtYXdhcmUgdmVyc2lvbnMuXG4gICAgZnVuY3Rpb24gbGlzdGVuQmVmb3JlKGhvb2spIHtcbiAgICAgIHJldHVybiBoaXN0b3J5Lmxpc3RlbkJlZm9yZShmdW5jdGlvbiAobG9jYXRpb24sIGNhbGxiYWNrKSB7XG4gICAgICAgIF9ydW5UcmFuc2l0aW9uSG9vazJbJ2RlZmF1bHQnXShob29rLCBhZGRCYXNlbmFtZShsb2NhdGlvbiksIGNhbGxiYWNrKTtcbiAgICAgIH0pO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGxpc3RlbihsaXN0ZW5lcikge1xuICAgICAgcmV0dXJuIGhpc3RvcnkubGlzdGVuKGZ1bmN0aW9uIChsb2NhdGlvbikge1xuICAgICAgICBsaXN0ZW5lcihhZGRCYXNlbmFtZShsb2NhdGlvbikpO1xuICAgICAgfSk7XG4gICAgfVxuXG4gICAgLy8gT3ZlcnJpZGUgYWxsIHdyaXRlIG1ldGhvZHMgd2l0aCBiYXNlbmFtZS1hd2FyZSB2ZXJzaW9ucy5cbiAgICBmdW5jdGlvbiBwdXNoKGxvY2F0aW9uKSB7XG4gICAgICBoaXN0b3J5LnB1c2gocHJlcGVuZEJhc2VuYW1lKGxvY2F0aW9uKSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gcmVwbGFjZShsb2NhdGlvbikge1xuICAgICAgaGlzdG9yeS5yZXBsYWNlKHByZXBlbmRCYXNlbmFtZShsb2NhdGlvbikpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGNyZWF0ZVBhdGgobG9jYXRpb24pIHtcbiAgICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZVBhdGgocHJlcGVuZEJhc2VuYW1lKGxvY2F0aW9uKSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gY3JlYXRlSHJlZihsb2NhdGlvbikge1xuICAgICAgcmV0dXJuIGhpc3RvcnkuY3JlYXRlSHJlZihwcmVwZW5kQmFzZW5hbWUobG9jYXRpb24pKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjcmVhdGVMb2NhdGlvbihsb2NhdGlvbikge1xuICAgICAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIGFyZ3MgPSBBcnJheShfbGVuID4gMSA/IF9sZW4gLSAxIDogMCksIF9rZXkgPSAxOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgICAgIGFyZ3NbX2tleSAtIDFdID0gYXJndW1lbnRzW19rZXldO1xuICAgICAgfVxuXG4gICAgICByZXR1cm4gYWRkQmFzZW5hbWUoaGlzdG9yeS5jcmVhdGVMb2NhdGlvbi5hcHBseShoaXN0b3J5LCBbcHJlcGVuZEJhc2VuYW1lKGxvY2F0aW9uKV0uY29uY2F0KGFyZ3MpKSk7XG4gICAgfVxuXG4gICAgLy8gZGVwcmVjYXRlZFxuICAgIGZ1bmN0aW9uIHB1c2hTdGF0ZShzdGF0ZSwgcGF0aCkge1xuICAgICAgaWYgKHR5cGVvZiBwYXRoID09PSAnc3RyaW5nJykgcGF0aCA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgICBwdXNoKF9leHRlbmRzKHsgc3RhdGU6IHN0YXRlIH0sIHBhdGgpKTtcbiAgICB9XG5cbiAgICAvLyBkZXByZWNhdGVkXG4gICAgZnVuY3Rpb24gcmVwbGFjZVN0YXRlKHN0YXRlLCBwYXRoKSB7XG4gICAgICBpZiAodHlwZW9mIHBhdGggPT09ICdzdHJpbmcnKSBwYXRoID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICAgIHJlcGxhY2UoX2V4dGVuZHMoeyBzdGF0ZTogc3RhdGUgfSwgcGF0aCkpO1xuICAgIH1cblxuICAgIHJldHVybiBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwge1xuICAgICAgbGlzdGVuQmVmb3JlOiBsaXN0ZW5CZWZvcmUsXG4gICAgICBsaXN0ZW46IGxpc3RlbixcbiAgICAgIHB1c2g6IHB1c2gsXG4gICAgICByZXBsYWNlOiByZXBsYWNlLFxuICAgICAgY3JlYXRlUGF0aDogY3JlYXRlUGF0aCxcbiAgICAgIGNyZWF0ZUhyZWY6IGNyZWF0ZUhyZWYsXG4gICAgICBjcmVhdGVMb2NhdGlvbjogY3JlYXRlTG9jYXRpb24sXG5cbiAgICAgIHB1c2hTdGF0ZTogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShwdXNoU3RhdGUsICdwdXNoU3RhdGUgaXMgZGVwcmVjYXRlZDsgdXNlIHB1c2ggaW5zdGVhZCcpLFxuICAgICAgcmVwbGFjZVN0YXRlOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHJlcGxhY2VTdGF0ZSwgJ3JlcGxhY2VTdGF0ZSBpcyBkZXByZWNhdGVkOyB1c2UgcmVwbGFjZSBpbnN0ZWFkJylcbiAgICB9KTtcbiAgfTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gdXNlQmFzZW5hbWU7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfcXVlcnlTdHJpbmcgPSByZXF1aXJlKCdxdWVyeS1zdHJpbmcnKTtcblxudmFyIF9ydW5UcmFuc2l0aW9uSG9vayA9IHJlcXVpcmUoJy4vcnVuVHJhbnNpdGlvbkhvb2snKTtcblxudmFyIF9ydW5UcmFuc2l0aW9uSG9vazIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9ydW5UcmFuc2l0aW9uSG9vayk7XG5cbnZhciBfUGF0aFV0aWxzID0gcmVxdWlyZSgnLi9QYXRoVXRpbHMnKTtcblxudmFyIF9kZXByZWNhdGUgPSByZXF1aXJlKCcuL2RlcHJlY2F0ZScpO1xuXG52YXIgX2RlcHJlY2F0ZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9kZXByZWNhdGUpO1xuXG52YXIgU0VBUkNIX0JBU0VfS0VZID0gJyRzZWFyY2hCYXNlJztcblxuZnVuY3Rpb24gZGVmYXVsdFN0cmluZ2lmeVF1ZXJ5KHF1ZXJ5KSB7XG4gIHJldHVybiBfcXVlcnlTdHJpbmcuc3RyaW5naWZ5KHF1ZXJ5KS5yZXBsYWNlKC8lMjAvZywgJysnKTtcbn1cblxudmFyIGRlZmF1bHRQYXJzZVF1ZXJ5U3RyaW5nID0gX3F1ZXJ5U3RyaW5nLnBhcnNlO1xuXG5mdW5jdGlvbiBpc05lc3RlZE9iamVjdChvYmplY3QpIHtcbiAgZm9yICh2YXIgcCBpbiBvYmplY3QpIHtcbiAgICBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iamVjdCwgcCkgJiYgdHlwZW9mIG9iamVjdFtwXSA9PT0gJ29iamVjdCcgJiYgIUFycmF5LmlzQXJyYXkob2JqZWN0W3BdKSAmJiBvYmplY3RbcF0gIT09IG51bGwpIHJldHVybiB0cnVlO1xuICB9cmV0dXJuIGZhbHNlO1xufVxuXG4vKipcbiAqIFJldHVybnMgYSBuZXcgY3JlYXRlSGlzdG9yeSBmdW5jdGlvbiB0aGF0IG1heSBiZSB1c2VkIHRvIGNyZWF0ZVxuICogaGlzdG9yeSBvYmplY3RzIHRoYXQga25vdyBob3cgdG8gaGFuZGxlIFVSTCBxdWVyaWVzLlxuICovXG5mdW5jdGlvbiB1c2VRdWVyaWVzKGNyZWF0ZUhpc3RvcnkpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuXG4gICAgdmFyIGhpc3RvcnkgPSBjcmVhdGVIaXN0b3J5KG9wdGlvbnMpO1xuXG4gICAgdmFyIHN0cmluZ2lmeVF1ZXJ5ID0gb3B0aW9ucy5zdHJpbmdpZnlRdWVyeTtcbiAgICB2YXIgcGFyc2VRdWVyeVN0cmluZyA9IG9wdGlvbnMucGFyc2VRdWVyeVN0cmluZztcblxuICAgIGlmICh0eXBlb2Ygc3RyaW5naWZ5UXVlcnkgIT09ICdmdW5jdGlvbicpIHN0cmluZ2lmeVF1ZXJ5ID0gZGVmYXVsdFN0cmluZ2lmeVF1ZXJ5O1xuXG4gICAgaWYgKHR5cGVvZiBwYXJzZVF1ZXJ5U3RyaW5nICE9PSAnZnVuY3Rpb24nKSBwYXJzZVF1ZXJ5U3RyaW5nID0gZGVmYXVsdFBhcnNlUXVlcnlTdHJpbmc7XG5cbiAgICBmdW5jdGlvbiBhZGRRdWVyeShsb2NhdGlvbikge1xuICAgICAgaWYgKGxvY2F0aW9uLnF1ZXJ5ID09IG51bGwpIHtcbiAgICAgICAgdmFyIHNlYXJjaCA9IGxvY2F0aW9uLnNlYXJjaDtcblxuICAgICAgICBsb2NhdGlvbi5xdWVyeSA9IHBhcnNlUXVlcnlTdHJpbmcoc2VhcmNoLnN1YnN0cmluZygxKSk7XG4gICAgICAgIGxvY2F0aW9uW1NFQVJDSF9CQVNFX0tFWV0gPSB7IHNlYXJjaDogc2VhcmNoLCBzZWFyY2hCYXNlOiAnJyB9O1xuICAgICAgfVxuXG4gICAgICAvLyBUT0RPOiBJbnN0ZWFkIG9mIGFsbCB0aGUgYm9vay1rZWVwaW5nIGhlcmUsIHRoaXMgc2hvdWxkIGp1c3Qgc3RyaXAgdGhlXG4gICAgICAvLyBzdHJpbmdpZmllZCBxdWVyeSBmcm9tIHRoZSBzZWFyY2guXG5cbiAgICAgIHJldHVybiBsb2NhdGlvbjtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBhcHBlbmRRdWVyeShsb2NhdGlvbiwgcXVlcnkpIHtcbiAgICAgIHZhciBfZXh0ZW5kczI7XG5cbiAgICAgIHZhciBzZWFyY2hCYXNlU3BlYyA9IGxvY2F0aW9uW1NFQVJDSF9CQVNFX0tFWV07XG4gICAgICB2YXIgcXVlcnlTdHJpbmcgPSBxdWVyeSA/IHN0cmluZ2lmeVF1ZXJ5KHF1ZXJ5KSA6ICcnO1xuICAgICAgaWYgKCFzZWFyY2hCYXNlU3BlYyAmJiAhcXVlcnlTdHJpbmcpIHtcbiAgICAgICAgcmV0dXJuIGxvY2F0aW9uO1xuICAgICAgfVxuXG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oc3RyaW5naWZ5UXVlcnkgIT09IGRlZmF1bHRTdHJpbmdpZnlRdWVyeSB8fCAhaXNOZXN0ZWRPYmplY3QocXVlcnkpLCAndXNlUXVlcmllcyBkb2VzIG5vdCBzdHJpbmdpZnkgbmVzdGVkIHF1ZXJ5IG9iamVjdHMgYnkgZGVmYXVsdDsgJyArICd1c2UgYSBjdXN0b20gc3RyaW5naWZ5UXVlcnkgZnVuY3Rpb24nKSA6IHVuZGVmaW5lZDtcblxuICAgICAgaWYgKHR5cGVvZiBsb2NhdGlvbiA9PT0gJ3N0cmluZycpIGxvY2F0aW9uID0gX1BhdGhVdGlscy5wYXJzZVBhdGgobG9jYXRpb24pO1xuXG4gICAgICB2YXIgc2VhcmNoQmFzZSA9IHVuZGVmaW5lZDtcbiAgICAgIGlmIChzZWFyY2hCYXNlU3BlYyAmJiBsb2NhdGlvbi5zZWFyY2ggPT09IHNlYXJjaEJhc2VTcGVjLnNlYXJjaCkge1xuICAgICAgICBzZWFyY2hCYXNlID0gc2VhcmNoQmFzZVNwZWMuc2VhcmNoQmFzZTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHNlYXJjaEJhc2UgPSBsb2NhdGlvbi5zZWFyY2ggfHwgJyc7XG4gICAgICB9XG5cbiAgICAgIHZhciBzZWFyY2ggPSBzZWFyY2hCYXNlO1xuICAgICAgaWYgKHF1ZXJ5U3RyaW5nKSB7XG4gICAgICAgIHNlYXJjaCArPSAoc2VhcmNoID8gJyYnIDogJz8nKSArIHF1ZXJ5U3RyaW5nO1xuICAgICAgfVxuXG4gICAgICByZXR1cm4gX2V4dGVuZHMoe30sIGxvY2F0aW9uLCAoX2V4dGVuZHMyID0ge1xuICAgICAgICBzZWFyY2g6IHNlYXJjaFxuICAgICAgfSwgX2V4dGVuZHMyW1NFQVJDSF9CQVNFX0tFWV0gPSB7IHNlYXJjaDogc2VhcmNoLCBzZWFyY2hCYXNlOiBzZWFyY2hCYXNlIH0sIF9leHRlbmRzMikpO1xuICAgIH1cblxuICAgIC8vIE92ZXJyaWRlIGFsbCByZWFkIG1ldGhvZHMgd2l0aCBxdWVyeS1hd2FyZSB2ZXJzaW9ucy5cbiAgICBmdW5jdGlvbiBsaXN0ZW5CZWZvcmUoaG9vaykge1xuICAgICAgcmV0dXJuIGhpc3RvcnkubGlzdGVuQmVmb3JlKGZ1bmN0aW9uIChsb2NhdGlvbiwgY2FsbGJhY2spIHtcbiAgICAgICAgX3J1blRyYW5zaXRpb25Ib29rMlsnZGVmYXVsdCddKGhvb2ssIGFkZFF1ZXJ5KGxvY2F0aW9uKSwgY2FsbGJhY2spO1xuICAgICAgfSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgICByZXR1cm4gaGlzdG9yeS5saXN0ZW4oZnVuY3Rpb24gKGxvY2F0aW9uKSB7XG4gICAgICAgIGxpc3RlbmVyKGFkZFF1ZXJ5KGxvY2F0aW9uKSk7XG4gICAgICB9KTtcbiAgICB9XG5cbiAgICAvLyBPdmVycmlkZSBhbGwgd3JpdGUgbWV0aG9kcyB3aXRoIHF1ZXJ5LWF3YXJlIHZlcnNpb25zLlxuICAgIGZ1bmN0aW9uIHB1c2gobG9jYXRpb24pIHtcbiAgICAgIGhpc3RvcnkucHVzaChhcHBlbmRRdWVyeShsb2NhdGlvbiwgbG9jYXRpb24ucXVlcnkpKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiByZXBsYWNlKGxvY2F0aW9uKSB7XG4gICAgICBoaXN0b3J5LnJlcGxhY2UoYXBwZW5kUXVlcnkobG9jYXRpb24sIGxvY2F0aW9uLnF1ZXJ5KSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gY3JlYXRlUGF0aChsb2NhdGlvbiwgcXVlcnkpIHtcbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXSghcXVlcnksICd0aGUgcXVlcnkgYXJndW1lbnQgdG8gY3JlYXRlUGF0aCBpcyBkZXByZWNhdGVkOyB1c2UgYSBsb2NhdGlvbiBkZXNjcmlwdG9yIGluc3RlYWQnKSA6IHVuZGVmaW5lZDtcblxuICAgICAgcmV0dXJuIGhpc3RvcnkuY3JlYXRlUGF0aChhcHBlbmRRdWVyeShsb2NhdGlvbiwgcXVlcnkgfHwgbG9jYXRpb24ucXVlcnkpKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjcmVhdGVIcmVmKGxvY2F0aW9uLCBxdWVyeSkge1xuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKCFxdWVyeSwgJ3RoZSBxdWVyeSBhcmd1bWVudCB0byBjcmVhdGVIcmVmIGlzIGRlcHJlY2F0ZWQ7IHVzZSBhIGxvY2F0aW9uIGRlc2NyaXB0b3IgaW5zdGVhZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVIcmVmKGFwcGVuZFF1ZXJ5KGxvY2F0aW9uLCBxdWVyeSB8fCBsb2NhdGlvbi5xdWVyeSkpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGNyZWF0ZUxvY2F0aW9uKGxvY2F0aW9uKSB7XG4gICAgICBmb3IgKHZhciBfbGVuID0gYXJndW1lbnRzLmxlbmd0aCwgYXJncyA9IEFycmF5KF9sZW4gPiAxID8gX2xlbiAtIDEgOiAwKSwgX2tleSA9IDE7IF9rZXkgPCBfbGVuOyBfa2V5KyspIHtcbiAgICAgICAgYXJnc1tfa2V5IC0gMV0gPSBhcmd1bWVudHNbX2tleV07XG4gICAgICB9XG5cbiAgICAgIHZhciBmdWxsTG9jYXRpb24gPSBoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uLmFwcGx5KGhpc3RvcnksIFthcHBlbmRRdWVyeShsb2NhdGlvbiwgbG9jYXRpb24ucXVlcnkpXS5jb25jYXQoYXJncykpO1xuICAgICAgaWYgKGxvY2F0aW9uLnF1ZXJ5KSB7XG4gICAgICAgIGZ1bGxMb2NhdGlvbi5xdWVyeSA9IGxvY2F0aW9uLnF1ZXJ5O1xuICAgICAgfVxuICAgICAgcmV0dXJuIGFkZFF1ZXJ5KGZ1bGxMb2NhdGlvbik7XG4gICAgfVxuXG4gICAgLy8gZGVwcmVjYXRlZFxuICAgIGZ1bmN0aW9uIHB1c2hTdGF0ZShzdGF0ZSwgcGF0aCwgcXVlcnkpIHtcbiAgICAgIGlmICh0eXBlb2YgcGF0aCA9PT0gJ3N0cmluZycpIHBhdGggPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgICAgcHVzaChfZXh0ZW5kcyh7IHN0YXRlOiBzdGF0ZSB9LCBwYXRoLCB7IHF1ZXJ5OiBxdWVyeSB9KSk7XG4gICAgfVxuXG4gICAgLy8gZGVwcmVjYXRlZFxuICAgIGZ1bmN0aW9uIHJlcGxhY2VTdGF0ZShzdGF0ZSwgcGF0aCwgcXVlcnkpIHtcbiAgICAgIGlmICh0eXBlb2YgcGF0aCA9PT0gJ3N0cmluZycpIHBhdGggPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgICAgcmVwbGFjZShfZXh0ZW5kcyh7IHN0YXRlOiBzdGF0ZSB9LCBwYXRoLCB7IHF1ZXJ5OiBxdWVyeSB9KSk7XG4gICAgfVxuXG4gICAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB7XG4gICAgICBsaXN0ZW5CZWZvcmU6IGxpc3RlbkJlZm9yZSxcbiAgICAgIGxpc3RlbjogbGlzdGVuLFxuICAgICAgcHVzaDogcHVzaCxcbiAgICAgIHJlcGxhY2U6IHJlcGxhY2UsXG4gICAgICBjcmVhdGVQYXRoOiBjcmVhdGVQYXRoLFxuICAgICAgY3JlYXRlSHJlZjogY3JlYXRlSHJlZixcbiAgICAgIGNyZWF0ZUxvY2F0aW9uOiBjcmVhdGVMb2NhdGlvbixcblxuICAgICAgcHVzaFN0YXRlOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHB1c2hTdGF0ZSwgJ3B1c2hTdGF0ZSBpcyBkZXByZWNhdGVkOyB1c2UgcHVzaCBpbnN0ZWFkJyksXG4gICAgICByZXBsYWNlU3RhdGU6IF9kZXByZWNhdGUyWydkZWZhdWx0J10ocmVwbGFjZVN0YXRlLCAncmVwbGFjZVN0YXRlIGlzIGRlcHJlY2F0ZWQ7IHVzZSByZXBsYWNlIGluc3RlYWQnKVxuICAgIH0pO1xuICB9O1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSB1c2VRdWVyaWVzO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiLyoqXG4gKiBDb3B5cmlnaHQgMjAxNC0yMDE1LCBGYWNlYm9vaywgSW5jLlxuICogQWxsIHJpZ2h0cyByZXNlcnZlZC5cbiAqXG4gKiBUaGlzIHNvdXJjZSBjb2RlIGlzIGxpY2Vuc2VkIHVuZGVyIHRoZSBCU0Qtc3R5bGUgbGljZW5zZSBmb3VuZCBpbiB0aGVcbiAqIExJQ0VOU0UgZmlsZSBpbiB0aGUgcm9vdCBkaXJlY3Rvcnkgb2YgdGhpcyBzb3VyY2UgdHJlZS4gQW4gYWRkaXRpb25hbCBncmFudFxuICogb2YgcGF0ZW50IHJpZ2h0cyBjYW4gYmUgZm91bmQgaW4gdGhlIFBBVEVOVFMgZmlsZSBpbiB0aGUgc2FtZSBkaXJlY3RvcnkuXG4gKi9cblxuJ3VzZSBzdHJpY3QnO1xuXG4vKipcbiAqIFNpbWlsYXIgdG8gaW52YXJpYW50IGJ1dCBvbmx5IGxvZ3MgYSB3YXJuaW5nIGlmIHRoZSBjb25kaXRpb24gaXMgbm90IG1ldC5cbiAqIFRoaXMgY2FuIGJlIHVzZWQgdG8gbG9nIGlzc3VlcyBpbiBkZXZlbG9wbWVudCBlbnZpcm9ubWVudHMgaW4gY3JpdGljYWxcbiAqIHBhdGhzLiBSZW1vdmluZyB0aGUgbG9nZ2luZyBjb2RlIGZvciBwcm9kdWN0aW9uIGVudmlyb25tZW50cyB3aWxsIGtlZXAgdGhlXG4gKiBzYW1lIGxvZ2ljIGFuZCBmb2xsb3cgdGhlIHNhbWUgY29kZSBwYXRocy5cbiAqL1xuXG52YXIgd2FybmluZyA9IGZ1bmN0aW9uKCkge307XG5cbmlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gIHdhcm5pbmcgPSBmdW5jdGlvbihjb25kaXRpb24sIGZvcm1hdCwgYXJncykge1xuICAgIHZhciBsZW4gPSBhcmd1bWVudHMubGVuZ3RoO1xuICAgIGFyZ3MgPSBuZXcgQXJyYXkobGVuID4gMiA/IGxlbiAtIDIgOiAwKTtcbiAgICBmb3IgKHZhciBrZXkgPSAyOyBrZXkgPCBsZW47IGtleSsrKSB7XG4gICAgICBhcmdzW2tleSAtIDJdID0gYXJndW1lbnRzW2tleV07XG4gICAgfVxuICAgIGlmIChmb3JtYXQgPT09IHVuZGVmaW5lZCkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKFxuICAgICAgICAnYHdhcm5pbmcoY29uZGl0aW9uLCBmb3JtYXQsIC4uLmFyZ3MpYCByZXF1aXJlcyBhIHdhcm5pbmcgJyArXG4gICAgICAgICdtZXNzYWdlIGFyZ3VtZW50J1xuICAgICAgKTtcbiAgICB9XG5cbiAgICBpZiAoZm9ybWF0Lmxlbmd0aCA8IDEwIHx8ICgvXltzXFxXXSokLykudGVzdChmb3JtYXQpKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgICdUaGUgd2FybmluZyBmb3JtYXQgc2hvdWxkIGJlIGFibGUgdG8gdW5pcXVlbHkgaWRlbnRpZnkgdGhpcyAnICtcbiAgICAgICAgJ3dhcm5pbmcuIFBsZWFzZSwgdXNlIGEgbW9yZSBkZXNjcmlwdGl2ZSBmb3JtYXQgdGhhbjogJyArIGZvcm1hdFxuICAgICAgKTtcbiAgICB9XG5cbiAgICBpZiAoIWNvbmRpdGlvbikge1xuICAgICAgdmFyIGFyZ0luZGV4ID0gMDtcbiAgICAgIHZhciBtZXNzYWdlID0gJ1dhcm5pbmc6ICcgK1xuICAgICAgICBmb3JtYXQucmVwbGFjZSgvJXMvZywgZnVuY3Rpb24oKSB7XG4gICAgICAgICAgcmV0dXJuIGFyZ3NbYXJnSW5kZXgrK107XG4gICAgICAgIH0pO1xuICAgICAgaWYgKHR5cGVvZiBjb25zb2xlICE9PSAndW5kZWZpbmVkJykge1xuICAgICAgICBjb25zb2xlLmVycm9yKG1lc3NhZ2UpO1xuICAgICAgfVxuICAgICAgdHJ5IHtcbiAgICAgICAgLy8gVGhpcyBlcnJvciB3YXMgdGhyb3duIGFzIGEgY29udmVuaWVuY2Ugc28gdGhhdCB5b3UgY2FuIHVzZSB0aGlzIHN0YWNrXG4gICAgICAgIC8vIHRvIGZpbmQgdGhlIGNhbGxzaXRlIHRoYXQgY2F1c2VkIHRoaXMgd2FybmluZyB0byBmaXJlLlxuICAgICAgICB0aHJvdyBuZXcgRXJyb3IobWVzc2FnZSk7XG4gICAgICB9IGNhdGNoKHgpIHt9XG4gICAgfVxuICB9O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IHdhcm5pbmc7XG4iLCIvKipcbiAqIENvcHlyaWdodCAyMDE1LCBZYWhvbyEgSW5jLlxuICogQ29weXJpZ2h0cyBsaWNlbnNlZCB1bmRlciB0aGUgTmV3IEJTRCBMaWNlbnNlLiBTZWUgdGhlIGFjY29tcGFueWluZyBMSUNFTlNFIGZpbGUgZm9yIHRlcm1zLlxuICovXG4ndXNlIHN0cmljdCc7XG5cbnZhciBSRUFDVF9TVEFUSUNTID0ge1xuICAgIGNoaWxkQ29udGV4dFR5cGVzOiB0cnVlLFxuICAgIGNvbnRleHRUeXBlczogdHJ1ZSxcbiAgICBkZWZhdWx0UHJvcHM6IHRydWUsXG4gICAgZGlzcGxheU5hbWU6IHRydWUsXG4gICAgZ2V0RGVmYXVsdFByb3BzOiB0cnVlLFxuICAgIG1peGluczogdHJ1ZSxcbiAgICBwcm9wVHlwZXM6IHRydWUsXG4gICAgdHlwZTogdHJ1ZVxufTtcblxudmFyIEtOT1dOX1NUQVRJQ1MgPSB7XG4gICAgbmFtZTogdHJ1ZSxcbiAgICBsZW5ndGg6IHRydWUsXG4gICAgcHJvdG90eXBlOiB0cnVlLFxuICAgIGNhbGxlcjogdHJ1ZSxcbiAgICBhcmd1bWVudHM6IHRydWUsXG4gICAgYXJpdHk6IHRydWVcbn07XG5cbnZhciBpc0dldE93blByb3BlcnR5U3ltYm9sc0F2YWlsYWJsZSA9IHR5cGVvZiBPYmplY3QuZ2V0T3duUHJvcGVydHlTeW1ib2xzID09PSAnZnVuY3Rpb24nO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uIGhvaXN0Tm9uUmVhY3RTdGF0aWNzKHRhcmdldENvbXBvbmVudCwgc291cmNlQ29tcG9uZW50LCBjdXN0b21TdGF0aWNzKSB7XG4gICAgaWYgKHR5cGVvZiBzb3VyY2VDb21wb25lbnQgIT09ICdzdHJpbmcnKSB7IC8vIGRvbid0IGhvaXN0IG92ZXIgc3RyaW5nIChodG1sKSBjb21wb25lbnRzXG4gICAgICAgIHZhciBrZXlzID0gT2JqZWN0LmdldE93blByb3BlcnR5TmFtZXMoc291cmNlQ29tcG9uZW50KTtcblxuICAgICAgICAvKiBpc3RhbmJ1bCBpZ25vcmUgZWxzZSAqL1xuICAgICAgICBpZiAoaXNHZXRPd25Qcm9wZXJ0eVN5bWJvbHNBdmFpbGFibGUpIHtcbiAgICAgICAgICAgIGtleXMgPSBrZXlzLmNvbmNhdChPYmplY3QuZ2V0T3duUHJvcGVydHlTeW1ib2xzKHNvdXJjZUNvbXBvbmVudCkpO1xuICAgICAgICB9XG5cbiAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBrZXlzLmxlbmd0aDsgKytpKSB7XG4gICAgICAgICAgICBpZiAoIVJFQUNUX1NUQVRJQ1Nba2V5c1tpXV0gJiYgIUtOT1dOX1NUQVRJQ1Nba2V5c1tpXV0gJiYgKCFjdXN0b21TdGF0aWNzIHx8ICFjdXN0b21TdGF0aWNzW2tleXNbaV1dKSkge1xuICAgICAgICAgICAgICAgIHRyeSB7XG4gICAgICAgICAgICAgICAgICAgIHRhcmdldENvbXBvbmVudFtrZXlzW2ldXSA9IHNvdXJjZUNvbXBvbmVudFtrZXlzW2ldXTtcbiAgICAgICAgICAgICAgICB9IGNhdGNoIChlcnJvcikge1xuXG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIHRhcmdldENvbXBvbmVudDtcbn07XG4iLCIvKipcbiAqIENvcHlyaWdodCAyMDEzLTIwMTUsIEZhY2Vib29rLCBJbmMuXG4gKiBBbGwgcmlnaHRzIHJlc2VydmVkLlxuICpcbiAqIFRoaXMgc291cmNlIGNvZGUgaXMgbGljZW5zZWQgdW5kZXIgdGhlIEJTRC1zdHlsZSBsaWNlbnNlIGZvdW5kIGluIHRoZVxuICogTElDRU5TRSBmaWxlIGluIHRoZSByb290IGRpcmVjdG9yeSBvZiB0aGlzIHNvdXJjZSB0cmVlLiBBbiBhZGRpdGlvbmFsIGdyYW50XG4gKiBvZiBwYXRlbnQgcmlnaHRzIGNhbiBiZSBmb3VuZCBpbiB0aGUgUEFURU5UUyBmaWxlIGluIHRoZSBzYW1lIGRpcmVjdG9yeS5cbiAqL1xuXG4ndXNlIHN0cmljdCc7XG5cbi8qKlxuICogVXNlIGludmFyaWFudCgpIHRvIGFzc2VydCBzdGF0ZSB3aGljaCB5b3VyIHByb2dyYW0gYXNzdW1lcyB0byBiZSB0cnVlLlxuICpcbiAqIFByb3ZpZGUgc3ByaW50Zi1zdHlsZSBmb3JtYXQgKG9ubHkgJXMgaXMgc3VwcG9ydGVkKSBhbmQgYXJndW1lbnRzXG4gKiB0byBwcm92aWRlIGluZm9ybWF0aW9uIGFib3V0IHdoYXQgYnJva2UgYW5kIHdoYXQgeW91IHdlcmVcbiAqIGV4cGVjdGluZy5cbiAqXG4gKiBUaGUgaW52YXJpYW50IG1lc3NhZ2Ugd2lsbCBiZSBzdHJpcHBlZCBpbiBwcm9kdWN0aW9uLCBidXQgdGhlIGludmFyaWFudFxuICogd2lsbCByZW1haW4gdG8gZW5zdXJlIGxvZ2ljIGRvZXMgbm90IGRpZmZlciBpbiBwcm9kdWN0aW9uLlxuICovXG5cbnZhciBpbnZhcmlhbnQgPSBmdW5jdGlvbihjb25kaXRpb24sIGZvcm1hdCwgYSwgYiwgYywgZCwgZSwgZikge1xuICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgIGlmIChmb3JtYXQgPT09IHVuZGVmaW5lZCkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdpbnZhcmlhbnQgcmVxdWlyZXMgYW4gZXJyb3IgbWVzc2FnZSBhcmd1bWVudCcpO1xuICAgIH1cbiAgfVxuXG4gIGlmICghY29uZGl0aW9uKSB7XG4gICAgdmFyIGVycm9yO1xuICAgIGlmIChmb3JtYXQgPT09IHVuZGVmaW5lZCkge1xuICAgICAgZXJyb3IgPSBuZXcgRXJyb3IoXG4gICAgICAgICdNaW5pZmllZCBleGNlcHRpb24gb2NjdXJyZWQ7IHVzZSB0aGUgbm9uLW1pbmlmaWVkIGRldiBlbnZpcm9ubWVudCAnICtcbiAgICAgICAgJ2ZvciB0aGUgZnVsbCBlcnJvciBtZXNzYWdlIGFuZCBhZGRpdGlvbmFsIGhlbHBmdWwgd2FybmluZ3MuJ1xuICAgICAgKTtcbiAgICB9IGVsc2Uge1xuICAgICAgdmFyIGFyZ3MgPSBbYSwgYiwgYywgZCwgZSwgZl07XG4gICAgICB2YXIgYXJnSW5kZXggPSAwO1xuICAgICAgZXJyb3IgPSBuZXcgRXJyb3IoXG4gICAgICAgIGZvcm1hdC5yZXBsYWNlKC8lcy9nLCBmdW5jdGlvbigpIHsgcmV0dXJuIGFyZ3NbYXJnSW5kZXgrK107IH0pXG4gICAgICApO1xuICAgICAgZXJyb3IubmFtZSA9ICdJbnZhcmlhbnQgVmlvbGF0aW9uJztcbiAgICB9XG5cbiAgICBlcnJvci5mcmFtZXNUb1BvcCA9IDE7IC8vIHdlIGRvbid0IGNhcmUgYWJvdXQgaW52YXJpYW50J3Mgb3duIGZyYW1lXG4gICAgdGhyb3cgZXJyb3I7XG4gIH1cbn07XG5cbm1vZHVsZS5leHBvcnRzID0gaW52YXJpYW50O1xuIiwibW9kdWxlLmV4cG9ydHMgPSBpc0Z1bmN0aW9uXG5cbnZhciB0b1N0cmluZyA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmdcblxuZnVuY3Rpb24gaXNGdW5jdGlvbiAoZm4pIHtcbiAgdmFyIHN0cmluZyA9IHRvU3RyaW5nLmNhbGwoZm4pXG4gIHJldHVybiBzdHJpbmcgPT09ICdbb2JqZWN0IEZ1bmN0aW9uXScgfHxcbiAgICAodHlwZW9mIGZuID09PSAnZnVuY3Rpb24nICYmIHN0cmluZyAhPT0gJ1tvYmplY3QgUmVnRXhwXScpIHx8XG4gICAgKHR5cGVvZiB3aW5kb3cgIT09ICd1bmRlZmluZWQnICYmXG4gICAgIC8vIElFOCBhbmQgYmVsb3dcbiAgICAgKGZuID09PSB3aW5kb3cuc2V0VGltZW91dCB8fFxuICAgICAgZm4gPT09IHdpbmRvdy5hbGVydCB8fFxuICAgICAgZm4gPT09IHdpbmRvdy5jb25maXJtIHx8XG4gICAgICBmbiA9PT0gd2luZG93LnByb21wdCkpXG59O1xuIiwidmFyIG92ZXJBcmcgPSByZXF1aXJlKCcuL19vdmVyQXJnJyk7XG5cbi8qKiBCdWlsdC1pbiB2YWx1ZSByZWZlcmVuY2VzLiAqL1xudmFyIGdldFByb3RvdHlwZSA9IG92ZXJBcmcoT2JqZWN0LmdldFByb3RvdHlwZU9mLCBPYmplY3QpO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGdldFByb3RvdHlwZTtcbiIsIi8qKlxuICogQ3JlYXRlcyBhIHVuYXJ5IGZ1bmN0aW9uIHRoYXQgaW52b2tlcyBgZnVuY2Agd2l0aCBpdHMgYXJndW1lbnQgdHJhbnNmb3JtZWQuXG4gKlxuICogQHByaXZhdGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGZ1bmMgVGhlIGZ1bmN0aW9uIHRvIHdyYXAuXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSB0cmFuc2Zvcm0gVGhlIGFyZ3VtZW50IHRyYW5zZm9ybS5cbiAqIEByZXR1cm5zIHtGdW5jdGlvbn0gUmV0dXJucyB0aGUgbmV3IGZ1bmN0aW9uLlxuICovXG5mdW5jdGlvbiBvdmVyQXJnKGZ1bmMsIHRyYW5zZm9ybSkge1xuICByZXR1cm4gZnVuY3Rpb24oYXJnKSB7XG4gICAgcmV0dXJuIGZ1bmModHJhbnNmb3JtKGFyZykpO1xuICB9O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IG92ZXJBcmc7XG4iLCIvKipcbiAqIENoZWNrcyBpZiBgdmFsdWVgIGlzIG9iamVjdC1saWtlLiBBIHZhbHVlIGlzIG9iamVjdC1saWtlIGlmIGl0J3Mgbm90IGBudWxsYFxuICogYW5kIGhhcyBhIGB0eXBlb2ZgIHJlc3VsdCBvZiBcIm9iamVjdFwiLlxuICpcbiAqIEBzdGF0aWNcbiAqIEBtZW1iZXJPZiBfXG4gKiBAc2luY2UgNC4wLjBcbiAqIEBjYXRlZ29yeSBMYW5nXG4gKiBAcGFyYW0geyp9IHZhbHVlIFRoZSB2YWx1ZSB0byBjaGVjay5cbiAqIEByZXR1cm5zIHtib29sZWFufSBSZXR1cm5zIGB0cnVlYCBpZiBgdmFsdWVgIGlzIG9iamVjdC1saWtlLCBlbHNlIGBmYWxzZWAuXG4gKiBAZXhhbXBsZVxuICpcbiAqIF8uaXNPYmplY3RMaWtlKHt9KTtcbiAqIC8vID0+IHRydWVcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZShbMSwgMiwgM10pO1xuICogLy8gPT4gdHJ1ZVxuICpcbiAqIF8uaXNPYmplY3RMaWtlKF8ubm9vcCk7XG4gKiAvLyA9PiBmYWxzZVxuICpcbiAqIF8uaXNPYmplY3RMaWtlKG51bGwpO1xuICogLy8gPT4gZmFsc2VcbiAqL1xuZnVuY3Rpb24gaXNPYmplY3RMaWtlKHZhbHVlKSB7XG4gIHJldHVybiB2YWx1ZSAhPSBudWxsICYmIHR5cGVvZiB2YWx1ZSA9PSAnb2JqZWN0Jztcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBpc09iamVjdExpa2U7XG4iLCJ2YXIgZ2V0UHJvdG90eXBlID0gcmVxdWlyZSgnLi9fZ2V0UHJvdG90eXBlJyksXG4gICAgaXNPYmplY3RMaWtlID0gcmVxdWlyZSgnLi9pc09iamVjdExpa2UnKTtcblxuLyoqIGBPYmplY3QjdG9TdHJpbmdgIHJlc3VsdCByZWZlcmVuY2VzLiAqL1xudmFyIG9iamVjdFRhZyA9ICdbb2JqZWN0IE9iamVjdF0nO1xuXG4vKiogVXNlZCBmb3IgYnVpbHQtaW4gbWV0aG9kIHJlZmVyZW5jZXMuICovXG52YXIgZnVuY1Byb3RvID0gRnVuY3Rpb24ucHJvdG90eXBlLFxuICAgIG9iamVjdFByb3RvID0gT2JqZWN0LnByb3RvdHlwZTtcblxuLyoqIFVzZWQgdG8gcmVzb2x2ZSB0aGUgZGVjb21waWxlZCBzb3VyY2Ugb2YgZnVuY3Rpb25zLiAqL1xudmFyIGZ1bmNUb1N0cmluZyA9IGZ1bmNQcm90by50b1N0cmluZztcblxuLyoqIFVzZWQgdG8gY2hlY2sgb2JqZWN0cyBmb3Igb3duIHByb3BlcnRpZXMuICovXG52YXIgaGFzT3duUHJvcGVydHkgPSBvYmplY3RQcm90by5oYXNPd25Qcm9wZXJ0eTtcblxuLyoqIFVzZWQgdG8gaW5mZXIgdGhlIGBPYmplY3RgIGNvbnN0cnVjdG9yLiAqL1xudmFyIG9iamVjdEN0b3JTdHJpbmcgPSBmdW5jVG9TdHJpbmcuY2FsbChPYmplY3QpO1xuXG4vKipcbiAqIFVzZWQgdG8gcmVzb2x2ZSB0aGVcbiAqIFtgdG9TdHJpbmdUYWdgXShodHRwOi8vZWNtYS1pbnRlcm5hdGlvbmFsLm9yZy9lY21hLTI2Mi83LjAvI3NlYy1vYmplY3QucHJvdG90eXBlLnRvc3RyaW5nKVxuICogb2YgdmFsdWVzLlxuICovXG52YXIgb2JqZWN0VG9TdHJpbmcgPSBvYmplY3RQcm90by50b1N0cmluZztcblxuLyoqXG4gKiBDaGVja3MgaWYgYHZhbHVlYCBpcyBhIHBsYWluIG9iamVjdCwgdGhhdCBpcywgYW4gb2JqZWN0IGNyZWF0ZWQgYnkgdGhlXG4gKiBgT2JqZWN0YCBjb25zdHJ1Y3RvciBvciBvbmUgd2l0aCBhIGBbW1Byb3RvdHlwZV1dYCBvZiBgbnVsbGAuXG4gKlxuICogQHN0YXRpY1xuICogQG1lbWJlck9mIF9cbiAqIEBzaW5jZSAwLjguMFxuICogQGNhdGVnb3J5IExhbmdcbiAqIEBwYXJhbSB7Kn0gdmFsdWUgVGhlIHZhbHVlIHRvIGNoZWNrLlxuICogQHJldHVybnMge2Jvb2xlYW59IFJldHVybnMgYHRydWVgIGlmIGB2YWx1ZWAgaXMgYSBwbGFpbiBvYmplY3QsIGVsc2UgYGZhbHNlYC5cbiAqIEBleGFtcGxlXG4gKlxuICogZnVuY3Rpb24gRm9vKCkge1xuICogICB0aGlzLmEgPSAxO1xuICogfVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdChuZXcgRm9vKTtcbiAqIC8vID0+IGZhbHNlXG4gKlxuICogXy5pc1BsYWluT2JqZWN0KFsxLCAyLCAzXSk7XG4gKiAvLyA9PiBmYWxzZVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdCh7ICd4JzogMCwgJ3knOiAwIH0pO1xuICogLy8gPT4gdHJ1ZVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdChPYmplY3QuY3JlYXRlKG51bGwpKTtcbiAqIC8vID0+IHRydWVcbiAqL1xuZnVuY3Rpb24gaXNQbGFpbk9iamVjdCh2YWx1ZSkge1xuICBpZiAoIWlzT2JqZWN0TGlrZSh2YWx1ZSkgfHwgb2JqZWN0VG9TdHJpbmcuY2FsbCh2YWx1ZSkgIT0gb2JqZWN0VGFnKSB7XG4gICAgcmV0dXJuIGZhbHNlO1xuICB9XG4gIHZhciBwcm90byA9IGdldFByb3RvdHlwZSh2YWx1ZSk7XG4gIGlmIChwcm90byA9PT0gbnVsbCkge1xuICAgIHJldHVybiB0cnVlO1xuICB9XG4gIHZhciBDdG9yID0gaGFzT3duUHJvcGVydHkuY2FsbChwcm90bywgJ2NvbnN0cnVjdG9yJykgJiYgcHJvdG8uY29uc3RydWN0b3I7XG4gIHJldHVybiAodHlwZW9mIEN0b3IgPT0gJ2Z1bmN0aW9uJyAmJlxuICAgIEN0b3IgaW5zdGFuY2VvZiBDdG9yICYmIGZ1bmNUb1N0cmluZy5jYWxsKEN0b3IpID09IG9iamVjdEN0b3JTdHJpbmcpO1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGlzUGxhaW5PYmplY3Q7XG4iLCJ2YXIgdHJpbSA9IHJlcXVpcmUoJ3RyaW0nKVxuICAsIGZvckVhY2ggPSByZXF1aXJlKCdmb3ItZWFjaCcpXG4gICwgaXNBcnJheSA9IGZ1bmN0aW9uKGFyZykge1xuICAgICAgcmV0dXJuIE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbChhcmcpID09PSAnW29iamVjdCBBcnJheV0nO1xuICAgIH1cblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiAoaGVhZGVycykge1xuICBpZiAoIWhlYWRlcnMpXG4gICAgcmV0dXJuIHt9XG5cbiAgdmFyIHJlc3VsdCA9IHt9XG5cbiAgZm9yRWFjaChcbiAgICAgIHRyaW0oaGVhZGVycykuc3BsaXQoJ1xcbicpXG4gICAgLCBmdW5jdGlvbiAocm93KSB7XG4gICAgICAgIHZhciBpbmRleCA9IHJvdy5pbmRleE9mKCc6JylcbiAgICAgICAgICAsIGtleSA9IHRyaW0ocm93LnNsaWNlKDAsIGluZGV4KSkudG9Mb3dlckNhc2UoKVxuICAgICAgICAgICwgdmFsdWUgPSB0cmltKHJvdy5zbGljZShpbmRleCArIDEpKVxuXG4gICAgICAgIGlmICh0eXBlb2YocmVzdWx0W2tleV0pID09PSAndW5kZWZpbmVkJykge1xuICAgICAgICAgIHJlc3VsdFtrZXldID0gdmFsdWVcbiAgICAgICAgfSBlbHNlIGlmIChpc0FycmF5KHJlc3VsdFtrZXldKSkge1xuICAgICAgICAgIHJlc3VsdFtrZXldLnB1c2godmFsdWUpXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgcmVzdWx0W2tleV0gPSBbIHJlc3VsdFtrZXldLCB2YWx1ZSBdXG4gICAgICAgIH1cbiAgICAgIH1cbiAgKVxuXG4gIHJldHVybiByZXN1bHRcbn0iLCIndXNlIHN0cmljdCc7XG52YXIgc3RyaWN0VXJpRW5jb2RlID0gcmVxdWlyZSgnc3RyaWN0LXVyaS1lbmNvZGUnKTtcblxuZXhwb3J0cy5leHRyYWN0ID0gZnVuY3Rpb24gKHN0cikge1xuXHRyZXR1cm4gc3RyLnNwbGl0KCc/JylbMV0gfHwgJyc7XG59O1xuXG5leHBvcnRzLnBhcnNlID0gZnVuY3Rpb24gKHN0cikge1xuXHRpZiAodHlwZW9mIHN0ciAhPT0gJ3N0cmluZycpIHtcblx0XHRyZXR1cm4ge307XG5cdH1cblxuXHRzdHIgPSBzdHIudHJpbSgpLnJlcGxhY2UoL14oXFw/fCN8JikvLCAnJyk7XG5cblx0aWYgKCFzdHIpIHtcblx0XHRyZXR1cm4ge307XG5cdH1cblxuXHRyZXR1cm4gc3RyLnNwbGl0KCcmJykucmVkdWNlKGZ1bmN0aW9uIChyZXQsIHBhcmFtKSB7XG5cdFx0dmFyIHBhcnRzID0gcGFyYW0ucmVwbGFjZSgvXFwrL2csICcgJykuc3BsaXQoJz0nKTtcblx0XHQvLyBGaXJlZm94IChwcmUgNDApIGRlY29kZXMgYCUzRGAgdG8gYD1gXG5cdFx0Ly8gaHR0cHM6Ly9naXRodWIuY29tL3NpbmRyZXNvcmh1cy9xdWVyeS1zdHJpbmcvcHVsbC8zN1xuXHRcdHZhciBrZXkgPSBwYXJ0cy5zaGlmdCgpO1xuXHRcdHZhciB2YWwgPSBwYXJ0cy5sZW5ndGggPiAwID8gcGFydHMuam9pbignPScpIDogdW5kZWZpbmVkO1xuXG5cdFx0a2V5ID0gZGVjb2RlVVJJQ29tcG9uZW50KGtleSk7XG5cblx0XHQvLyBtaXNzaW5nIGA9YCBzaG91bGQgYmUgYG51bGxgOlxuXHRcdC8vIGh0dHA6Ly93My5vcmcvVFIvMjAxMi9XRC11cmwtMjAxMjA1MjQvI2NvbGxlY3QtdXJsLXBhcmFtZXRlcnNcblx0XHR2YWwgPSB2YWwgPT09IHVuZGVmaW5lZCA/IG51bGwgOiBkZWNvZGVVUklDb21wb25lbnQodmFsKTtcblxuXHRcdGlmICghcmV0Lmhhc093blByb3BlcnR5KGtleSkpIHtcblx0XHRcdHJldFtrZXldID0gdmFsO1xuXHRcdH0gZWxzZSBpZiAoQXJyYXkuaXNBcnJheShyZXRba2V5XSkpIHtcblx0XHRcdHJldFtrZXldLnB1c2godmFsKTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0cmV0W2tleV0gPSBbcmV0W2tleV0sIHZhbF07XG5cdFx0fVxuXG5cdFx0cmV0dXJuIHJldDtcblx0fSwge30pO1xufTtcblxuZXhwb3J0cy5zdHJpbmdpZnkgPSBmdW5jdGlvbiAob2JqKSB7XG5cdHJldHVybiBvYmogPyBPYmplY3Qua2V5cyhvYmopLnNvcnQoKS5tYXAoZnVuY3Rpb24gKGtleSkge1xuXHRcdHZhciB2YWwgPSBvYmpba2V5XTtcblxuXHRcdGlmICh2YWwgPT09IHVuZGVmaW5lZCkge1xuXHRcdFx0cmV0dXJuICcnO1xuXHRcdH1cblxuXHRcdGlmICh2YWwgPT09IG51bGwpIHtcblx0XHRcdHJldHVybiBrZXk7XG5cdFx0fVxuXG5cdFx0aWYgKEFycmF5LmlzQXJyYXkodmFsKSkge1xuXHRcdFx0cmV0dXJuIHZhbC5zbGljZSgpLnNvcnQoKS5tYXAoZnVuY3Rpb24gKHZhbDIpIHtcblx0XHRcdFx0cmV0dXJuIHN0cmljdFVyaUVuY29kZShrZXkpICsgJz0nICsgc3RyaWN0VXJpRW5jb2RlKHZhbDIpO1xuXHRcdFx0fSkuam9pbignJicpO1xuXHRcdH1cblxuXHRcdHJldHVybiBzdHJpY3RVcmlFbmNvZGUoa2V5KSArICc9JyArIHN0cmljdFVyaUVuY29kZSh2YWwpO1xuXHR9KS5maWx0ZXIoZnVuY3Rpb24gKHgpIHtcblx0XHRyZXR1cm4geC5sZW5ndGggPiAwO1xuXHR9KS5qb2luKCcmJykgOiAnJztcbn07XG4iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IHVuZGVmaW5lZDtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfc3RvcmVTaGFwZSA9IHJlcXVpcmUoJy4uL3V0aWxzL3N0b3JlU2hhcGUnKTtcblxudmFyIF9zdG9yZVNoYXBlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3N0b3JlU2hhcGUpO1xuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCcuLi91dGlscy93YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIF9jbGFzc0NhbGxDaGVjayhpbnN0YW5jZSwgQ29uc3RydWN0b3IpIHsgaWYgKCEoaW5zdGFuY2UgaW5zdGFuY2VvZiBDb25zdHJ1Y3RvcikpIHsgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTsgfSB9XG5cbmZ1bmN0aW9uIF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHNlbGYsIGNhbGwpIHsgaWYgKCFzZWxmKSB7IHRocm93IG5ldyBSZWZlcmVuY2VFcnJvcihcInRoaXMgaGFzbid0IGJlZW4gaW5pdGlhbGlzZWQgLSBzdXBlcigpIGhhc24ndCBiZWVuIGNhbGxlZFwiKTsgfSByZXR1cm4gY2FsbCAmJiAodHlwZW9mIGNhbGwgPT09IFwib2JqZWN0XCIgfHwgdHlwZW9mIGNhbGwgPT09IFwiZnVuY3Rpb25cIikgPyBjYWxsIDogc2VsZjsgfVxuXG5mdW5jdGlvbiBfaW5oZXJpdHMoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIHsgaWYgKHR5cGVvZiBzdXBlckNsYXNzICE9PSBcImZ1bmN0aW9uXCIgJiYgc3VwZXJDbGFzcyAhPT0gbnVsbCkgeyB0aHJvdyBuZXcgVHlwZUVycm9yKFwiU3VwZXIgZXhwcmVzc2lvbiBtdXN0IGVpdGhlciBiZSBudWxsIG9yIGEgZnVuY3Rpb24sIG5vdCBcIiArIHR5cGVvZiBzdXBlckNsYXNzKTsgfSBzdWJDbGFzcy5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKHN1cGVyQ2xhc3MgJiYgc3VwZXJDbGFzcy5wcm90b3R5cGUsIHsgY29uc3RydWN0b3I6IHsgdmFsdWU6IHN1YkNsYXNzLCBlbnVtZXJhYmxlOiBmYWxzZSwgd3JpdGFibGU6IHRydWUsIGNvbmZpZ3VyYWJsZTogdHJ1ZSB9IH0pOyBpZiAoc3VwZXJDbGFzcykgT2JqZWN0LnNldFByb3RvdHlwZU9mID8gT2JqZWN0LnNldFByb3RvdHlwZU9mKHN1YkNsYXNzLCBzdXBlckNsYXNzKSA6IHN1YkNsYXNzLl9fcHJvdG9fXyA9IHN1cGVyQ2xhc3M7IH1cblxudmFyIGRpZFdhcm5BYm91dFJlY2VpdmluZ1N0b3JlID0gZmFsc2U7XG5mdW5jdGlvbiB3YXJuQWJvdXRSZWNlaXZpbmdTdG9yZSgpIHtcbiAgaWYgKGRpZFdhcm5BYm91dFJlY2VpdmluZ1N0b3JlKSB7XG4gICAgcmV0dXJuO1xuICB9XG4gIGRpZFdhcm5BYm91dFJlY2VpdmluZ1N0b3JlID0gdHJ1ZTtcblxuICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkoJzxQcm92aWRlcj4gZG9lcyBub3Qgc3VwcG9ydCBjaGFuZ2luZyBgc3RvcmVgIG9uIHRoZSBmbHkuICcgKyAnSXQgaXMgbW9zdCBsaWtlbHkgdGhhdCB5b3Ugc2VlIHRoaXMgZXJyb3IgYmVjYXVzZSB5b3UgdXBkYXRlZCB0byAnICsgJ1JlZHV4IDIueCBhbmQgUmVhY3QgUmVkdXggMi54IHdoaWNoIG5vIGxvbmdlciBob3QgcmVsb2FkIHJlZHVjZXJzICcgKyAnYXV0b21hdGljYWxseS4gU2VlIGh0dHBzOi8vZ2l0aHViLmNvbS9yZWFjdGpzL3JlYWN0LXJlZHV4L3JlbGVhc2VzLycgKyAndGFnL3YyLjAuMCBmb3IgdGhlIG1pZ3JhdGlvbiBpbnN0cnVjdGlvbnMuJyk7XG59XG5cbnZhciBQcm92aWRlciA9IGZ1bmN0aW9uIChfQ29tcG9uZW50KSB7XG4gIF9pbmhlcml0cyhQcm92aWRlciwgX0NvbXBvbmVudCk7XG5cbiAgUHJvdmlkZXIucHJvdG90eXBlLmdldENoaWxkQ29udGV4dCA9IGZ1bmN0aW9uIGdldENoaWxkQ29udGV4dCgpIHtcbiAgICByZXR1cm4geyBzdG9yZTogdGhpcy5zdG9yZSB9O1xuICB9O1xuXG4gIGZ1bmN0aW9uIFByb3ZpZGVyKHByb3BzLCBjb250ZXh0KSB7XG4gICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIFByb3ZpZGVyKTtcblxuICAgIHZhciBfdGhpcyA9IF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHRoaXMsIF9Db21wb25lbnQuY2FsbCh0aGlzLCBwcm9wcywgY29udGV4dCkpO1xuXG4gICAgX3RoaXMuc3RvcmUgPSBwcm9wcy5zdG9yZTtcbiAgICByZXR1cm4gX3RoaXM7XG4gIH1cblxuICBQcm92aWRlci5wcm90b3R5cGUucmVuZGVyID0gZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHZhciBjaGlsZHJlbiA9IHRoaXMucHJvcHMuY2hpbGRyZW47XG5cbiAgICByZXR1cm4gX3JlYWN0LkNoaWxkcmVuLm9ubHkoY2hpbGRyZW4pO1xuICB9O1xuXG4gIHJldHVybiBQcm92aWRlcjtcbn0oX3JlYWN0LkNvbXBvbmVudCk7XG5cbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gUHJvdmlkZXI7XG5cbmlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gIFByb3ZpZGVyLnByb3RvdHlwZS5jb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzID0gZnVuY3Rpb24gKG5leHRQcm9wcykge1xuICAgIHZhciBzdG9yZSA9IHRoaXMuc3RvcmU7XG4gICAgdmFyIG5leHRTdG9yZSA9IG5leHRQcm9wcy5zdG9yZTtcblxuICAgIGlmIChzdG9yZSAhPT0gbmV4dFN0b3JlKSB7XG4gICAgICB3YXJuQWJvdXRSZWNlaXZpbmdTdG9yZSgpO1xuICAgIH1cbiAgfTtcbn1cblxuUHJvdmlkZXIucHJvcFR5cGVzID0ge1xuICBzdG9yZTogX3N0b3JlU2hhcGUyW1wiZGVmYXVsdFwiXS5pc1JlcXVpcmVkLFxuICBjaGlsZHJlbjogX3JlYWN0LlByb3BUeXBlcy5lbGVtZW50LmlzUmVxdWlyZWRcbn07XG5Qcm92aWRlci5jaGlsZENvbnRleHRUeXBlcyA9IHtcbiAgc3RvcmU6IF9zdG9yZVNoYXBlMltcImRlZmF1bHRcIl0uaXNSZXF1aXJlZFxufTsiLCIndXNlIHN0cmljdCc7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGNvbm5lY3Q7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3N0b3JlU2hhcGUgPSByZXF1aXJlKCcuLi91dGlscy9zdG9yZVNoYXBlJyk7XG5cbnZhciBfc3RvcmVTaGFwZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zdG9yZVNoYXBlKTtcblxudmFyIF9zaGFsbG93RXF1YWwgPSByZXF1aXJlKCcuLi91dGlscy9zaGFsbG93RXF1YWwnKTtcblxudmFyIF9zaGFsbG93RXF1YWwyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc2hhbGxvd0VxdWFsKTtcblxudmFyIF93cmFwQWN0aW9uQ3JlYXRvcnMgPSByZXF1aXJlKCcuLi91dGlscy93cmFwQWN0aW9uQ3JlYXRvcnMnKTtcblxudmFyIF93cmFwQWN0aW9uQ3JlYXRvcnMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd3JhcEFjdGlvbkNyZWF0b3JzKTtcblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnLi4vdXRpbHMvd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdCA9IHJlcXVpcmUoJ2xvZGFzaC9pc1BsYWluT2JqZWN0Jyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pc1BsYWluT2JqZWN0KTtcblxudmFyIF9ob2lzdE5vblJlYWN0U3RhdGljcyA9IHJlcXVpcmUoJ2hvaXN0LW5vbi1yZWFjdC1zdGF0aWNzJyk7XG5cbnZhciBfaG9pc3ROb25SZWFjdFN0YXRpY3MyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaG9pc3ROb25SZWFjdFN0YXRpY3MpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3RvcikgeyBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkgeyB0aHJvdyBuZXcgVHlwZUVycm9yKFwiQ2Fubm90IGNhbGwgYSBjbGFzcyBhcyBhIGZ1bmN0aW9uXCIpOyB9IH1cblxuZnVuY3Rpb24gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4oc2VsZiwgY2FsbCkgeyBpZiAoIXNlbGYpIHsgdGhyb3cgbmV3IFJlZmVyZW5jZUVycm9yKFwidGhpcyBoYXNuJ3QgYmVlbiBpbml0aWFsaXNlZCAtIHN1cGVyKCkgaGFzbid0IGJlZW4gY2FsbGVkXCIpOyB9IHJldHVybiBjYWxsICYmICh0eXBlb2YgY2FsbCA9PT0gXCJvYmplY3RcIiB8fCB0eXBlb2YgY2FsbCA9PT0gXCJmdW5jdGlvblwiKSA/IGNhbGwgOiBzZWxmOyB9XG5cbmZ1bmN0aW9uIF9pbmhlcml0cyhzdWJDbGFzcywgc3VwZXJDbGFzcykgeyBpZiAodHlwZW9mIHN1cGVyQ2xhc3MgIT09IFwiZnVuY3Rpb25cIiAmJiBzdXBlckNsYXNzICE9PSBudWxsKSB7IHRocm93IG5ldyBUeXBlRXJyb3IoXCJTdXBlciBleHByZXNzaW9uIG11c3QgZWl0aGVyIGJlIG51bGwgb3IgYSBmdW5jdGlvbiwgbm90IFwiICsgdHlwZW9mIHN1cGVyQ2xhc3MpOyB9IHN1YkNsYXNzLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoc3VwZXJDbGFzcyAmJiBzdXBlckNsYXNzLnByb3RvdHlwZSwgeyBjb25zdHJ1Y3RvcjogeyB2YWx1ZTogc3ViQ2xhc3MsIGVudW1lcmFibGU6IGZhbHNlLCB3cml0YWJsZTogdHJ1ZSwgY29uZmlndXJhYmxlOiB0cnVlIH0gfSk7IGlmIChzdXBlckNsYXNzKSBPYmplY3Quc2V0UHJvdG90eXBlT2YgPyBPYmplY3Quc2V0UHJvdG90eXBlT2Yoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIDogc3ViQ2xhc3MuX19wcm90b19fID0gc3VwZXJDbGFzczsgfVxuXG52YXIgZGVmYXVsdE1hcFN0YXRlVG9Qcm9wcyA9IGZ1bmN0aW9uIGRlZmF1bHRNYXBTdGF0ZVRvUHJvcHMoc3RhdGUpIHtcbiAgcmV0dXJuIHt9O1xufTsgLy8gZXNsaW50LWRpc2FibGUtbGluZSBuby11bnVzZWQtdmFyc1xudmFyIGRlZmF1bHRNYXBEaXNwYXRjaFRvUHJvcHMgPSBmdW5jdGlvbiBkZWZhdWx0TWFwRGlzcGF0Y2hUb1Byb3BzKGRpc3BhdGNoKSB7XG4gIHJldHVybiB7IGRpc3BhdGNoOiBkaXNwYXRjaCB9O1xufTtcbnZhciBkZWZhdWx0TWVyZ2VQcm9wcyA9IGZ1bmN0aW9uIGRlZmF1bHRNZXJnZVByb3BzKHN0YXRlUHJvcHMsIGRpc3BhdGNoUHJvcHMsIHBhcmVudFByb3BzKSB7XG4gIHJldHVybiBfZXh0ZW5kcyh7fSwgcGFyZW50UHJvcHMsIHN0YXRlUHJvcHMsIGRpc3BhdGNoUHJvcHMpO1xufTtcblxuZnVuY3Rpb24gZ2V0RGlzcGxheU5hbWUoV3JhcHBlZENvbXBvbmVudCkge1xuICByZXR1cm4gV3JhcHBlZENvbXBvbmVudC5kaXNwbGF5TmFtZSB8fCBXcmFwcGVkQ29tcG9uZW50Lm5hbWUgfHwgJ0NvbXBvbmVudCc7XG59XG5cbnZhciBlcnJvck9iamVjdCA9IHsgdmFsdWU6IG51bGwgfTtcbmZ1bmN0aW9uIHRyeUNhdGNoKGZuLCBjdHgpIHtcbiAgdHJ5IHtcbiAgICByZXR1cm4gZm4uYXBwbHkoY3R4KTtcbiAgfSBjYXRjaCAoZSkge1xuICAgIGVycm9yT2JqZWN0LnZhbHVlID0gZTtcbiAgICByZXR1cm4gZXJyb3JPYmplY3Q7XG4gIH1cbn1cblxuLy8gSGVscHMgdHJhY2sgaG90IHJlbG9hZGluZy5cbnZhciBuZXh0VmVyc2lvbiA9IDA7XG5cbmZ1bmN0aW9uIGNvbm5lY3QobWFwU3RhdGVUb1Byb3BzLCBtYXBEaXNwYXRjaFRvUHJvcHMsIG1lcmdlUHJvcHMpIHtcbiAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDMgfHwgYXJndW1lbnRzWzNdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1szXTtcblxuICB2YXIgc2hvdWxkU3Vic2NyaWJlID0gQm9vbGVhbihtYXBTdGF0ZVRvUHJvcHMpO1xuICB2YXIgbWFwU3RhdGUgPSBtYXBTdGF0ZVRvUHJvcHMgfHwgZGVmYXVsdE1hcFN0YXRlVG9Qcm9wcztcblxuICB2YXIgbWFwRGlzcGF0Y2ggPSB1bmRlZmluZWQ7XG4gIGlmICh0eXBlb2YgbWFwRGlzcGF0Y2hUb1Byb3BzID09PSAnZnVuY3Rpb24nKSB7XG4gICAgbWFwRGlzcGF0Y2ggPSBtYXBEaXNwYXRjaFRvUHJvcHM7XG4gIH0gZWxzZSBpZiAoIW1hcERpc3BhdGNoVG9Qcm9wcykge1xuICAgIG1hcERpc3BhdGNoID0gZGVmYXVsdE1hcERpc3BhdGNoVG9Qcm9wcztcbiAgfSBlbHNlIHtcbiAgICBtYXBEaXNwYXRjaCA9ICgwLCBfd3JhcEFjdGlvbkNyZWF0b3JzMltcImRlZmF1bHRcIl0pKG1hcERpc3BhdGNoVG9Qcm9wcyk7XG4gIH1cblxuICB2YXIgZmluYWxNZXJnZVByb3BzID0gbWVyZ2VQcm9wcyB8fCBkZWZhdWx0TWVyZ2VQcm9wcztcbiAgdmFyIF9vcHRpb25zJHB1cmUgPSBvcHRpb25zLnB1cmU7XG4gIHZhciBwdXJlID0gX29wdGlvbnMkcHVyZSA9PT0gdW5kZWZpbmVkID8gdHJ1ZSA6IF9vcHRpb25zJHB1cmU7XG4gIHZhciBfb3B0aW9ucyR3aXRoUmVmID0gb3B0aW9ucy53aXRoUmVmO1xuICB2YXIgd2l0aFJlZiA9IF9vcHRpb25zJHdpdGhSZWYgPT09IHVuZGVmaW5lZCA/IGZhbHNlIDogX29wdGlvbnMkd2l0aFJlZjtcblxuICB2YXIgY2hlY2tNZXJnZWRFcXVhbHMgPSBwdXJlICYmIGZpbmFsTWVyZ2VQcm9wcyAhPT0gZGVmYXVsdE1lcmdlUHJvcHM7XG5cbiAgLy8gSGVscHMgdHJhY2sgaG90IHJlbG9hZGluZy5cbiAgdmFyIHZlcnNpb24gPSBuZXh0VmVyc2lvbisrO1xuXG4gIHJldHVybiBmdW5jdGlvbiB3cmFwV2l0aENvbm5lY3QoV3JhcHBlZENvbXBvbmVudCkge1xuICAgIHZhciBjb25uZWN0RGlzcGxheU5hbWUgPSAnQ29ubmVjdCgnICsgZ2V0RGlzcGxheU5hbWUoV3JhcHBlZENvbXBvbmVudCkgKyAnKSc7XG5cbiAgICBmdW5jdGlvbiBjaGVja1N0YXRlU2hhcGUocHJvcHMsIG1ldGhvZE5hbWUpIHtcbiAgICAgIGlmICghKDAsIF9pc1BsYWluT2JqZWN0MltcImRlZmF1bHRcIl0pKHByb3BzKSkge1xuICAgICAgICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkobWV0aG9kTmFtZSArICcoKSBpbiAnICsgY29ubmVjdERpc3BsYXlOYW1lICsgJyBtdXN0IHJldHVybiBhIHBsYWluIG9iamVjdC4gJyArICgnSW5zdGVhZCByZWNlaXZlZCAnICsgcHJvcHMgKyAnLicpKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjb21wdXRlTWVyZ2VkUHJvcHMoc3RhdGVQcm9wcywgZGlzcGF0Y2hQcm9wcywgcGFyZW50UHJvcHMpIHtcbiAgICAgIHZhciBtZXJnZWRQcm9wcyA9IGZpbmFsTWVyZ2VQcm9wcyhzdGF0ZVByb3BzLCBkaXNwYXRjaFByb3BzLCBwYXJlbnRQcm9wcyk7XG4gICAgICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgICBjaGVja1N0YXRlU2hhcGUobWVyZ2VkUHJvcHMsICdtZXJnZVByb3BzJyk7XG4gICAgICB9XG4gICAgICByZXR1cm4gbWVyZ2VkUHJvcHM7XG4gICAgfVxuXG4gICAgdmFyIENvbm5lY3QgPSBmdW5jdGlvbiAoX0NvbXBvbmVudCkge1xuICAgICAgX2luaGVyaXRzKENvbm5lY3QsIF9Db21wb25lbnQpO1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5zaG91bGRDb21wb25lbnRVcGRhdGUgPSBmdW5jdGlvbiBzaG91bGRDb21wb25lbnRVcGRhdGUoKSB7XG4gICAgICAgIHJldHVybiAhcHVyZSB8fCB0aGlzLmhhdmVPd25Qcm9wc0NoYW5nZWQgfHwgdGhpcy5oYXNTdG9yZVN0YXRlQ2hhbmdlZDtcbiAgICAgIH07XG5cbiAgICAgIGZ1bmN0aW9uIENvbm5lY3QocHJvcHMsIGNvbnRleHQpIHtcbiAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENvbm5lY3QpO1xuXG4gICAgICAgIHZhciBfdGhpcyA9IF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHRoaXMsIF9Db21wb25lbnQuY2FsbCh0aGlzLCBwcm9wcywgY29udGV4dCkpO1xuXG4gICAgICAgIF90aGlzLnZlcnNpb24gPSB2ZXJzaW9uO1xuICAgICAgICBfdGhpcy5zdG9yZSA9IHByb3BzLnN0b3JlIHx8IGNvbnRleHQuc3RvcmU7XG5cbiAgICAgICAgKDAsIF9pbnZhcmlhbnQyW1wiZGVmYXVsdFwiXSkoX3RoaXMuc3RvcmUsICdDb3VsZCBub3QgZmluZCBcInN0b3JlXCIgaW4gZWl0aGVyIHRoZSBjb250ZXh0IG9yICcgKyAoJ3Byb3BzIG9mIFwiJyArIGNvbm5lY3REaXNwbGF5TmFtZSArICdcIi4gJykgKyAnRWl0aGVyIHdyYXAgdGhlIHJvb3QgY29tcG9uZW50IGluIGEgPFByb3ZpZGVyPiwgJyArICgnb3IgZXhwbGljaXRseSBwYXNzIFwic3RvcmVcIiBhcyBhIHByb3AgdG8gXCInICsgY29ubmVjdERpc3BsYXlOYW1lICsgJ1wiLicpKTtcblxuICAgICAgICB2YXIgc3RvcmVTdGF0ZSA9IF90aGlzLnN0b3JlLmdldFN0YXRlKCk7XG4gICAgICAgIF90aGlzLnN0YXRlID0geyBzdG9yZVN0YXRlOiBzdG9yZVN0YXRlIH07XG4gICAgICAgIF90aGlzLmNsZWFyQ2FjaGUoKTtcbiAgICAgICAgcmV0dXJuIF90aGlzO1xuICAgICAgfVxuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jb21wdXRlU3RhdGVQcm9wcyA9IGZ1bmN0aW9uIGNvbXB1dGVTdGF0ZVByb3BzKHN0b3JlLCBwcm9wcykge1xuICAgICAgICBpZiAoIXRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5jb25maWd1cmVGaW5hbE1hcFN0YXRlKHN0b3JlLCBwcm9wcyk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgc3RhdGUgPSBzdG9yZS5nZXRTdGF0ZSgpO1xuICAgICAgICB2YXIgc3RhdGVQcm9wcyA9IHRoaXMuZG9TdGF0ZVByb3BzRGVwZW5kT25Pd25Qcm9wcyA/IHRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMoc3RhdGUsIHByb3BzKSA6IHRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMoc3RhdGUpO1xuXG4gICAgICAgIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICAgICAgY2hlY2tTdGF0ZVNoYXBlKHN0YXRlUHJvcHMsICdtYXBTdGF0ZVRvUHJvcHMnKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gc3RhdGVQcm9wcztcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbmZpZ3VyZUZpbmFsTWFwU3RhdGUgPSBmdW5jdGlvbiBjb25maWd1cmVGaW5hbE1hcFN0YXRlKHN0b3JlLCBwcm9wcykge1xuICAgICAgICB2YXIgbWFwcGVkU3RhdGUgPSBtYXBTdGF0ZShzdG9yZS5nZXRTdGF0ZSgpLCBwcm9wcyk7XG4gICAgICAgIHZhciBpc0ZhY3RvcnkgPSB0eXBlb2YgbWFwcGVkU3RhdGUgPT09ICdmdW5jdGlvbic7XG5cbiAgICAgICAgdGhpcy5maW5hbE1hcFN0YXRlVG9Qcm9wcyA9IGlzRmFjdG9yeSA/IG1hcHBlZFN0YXRlIDogbWFwU3RhdGU7XG4gICAgICAgIHRoaXMuZG9TdGF0ZVByb3BzRGVwZW5kT25Pd25Qcm9wcyA9IHRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMubGVuZ3RoICE9PSAxO1xuXG4gICAgICAgIGlmIChpc0ZhY3RvcnkpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5jb21wdXRlU3RhdGVQcm9wcyhzdG9yZSwgcHJvcHMpO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgICAgICBjaGVja1N0YXRlU2hhcGUobWFwcGVkU3RhdGUsICdtYXBTdGF0ZVRvUHJvcHMnKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gbWFwcGVkU3RhdGU7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jb21wdXRlRGlzcGF0Y2hQcm9wcyA9IGZ1bmN0aW9uIGNvbXB1dGVEaXNwYXRjaFByb3BzKHN0b3JlLCBwcm9wcykge1xuICAgICAgICBpZiAoIXRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5jb25maWd1cmVGaW5hbE1hcERpc3BhdGNoKHN0b3JlLCBwcm9wcyk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgZGlzcGF0Y2ggPSBzdG9yZS5kaXNwYXRjaDtcblxuICAgICAgICB2YXIgZGlzcGF0Y2hQcm9wcyA9IHRoaXMuZG9EaXNwYXRjaFByb3BzRGVwZW5kT25Pd25Qcm9wcyA/IHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMoZGlzcGF0Y2gsIHByb3BzKSA6IHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMoZGlzcGF0Y2gpO1xuXG4gICAgICAgIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICAgICAgY2hlY2tTdGF0ZVNoYXBlKGRpc3BhdGNoUHJvcHMsICdtYXBEaXNwYXRjaFRvUHJvcHMnKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gZGlzcGF0Y2hQcm9wcztcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbmZpZ3VyZUZpbmFsTWFwRGlzcGF0Y2ggPSBmdW5jdGlvbiBjb25maWd1cmVGaW5hbE1hcERpc3BhdGNoKHN0b3JlLCBwcm9wcykge1xuICAgICAgICB2YXIgbWFwcGVkRGlzcGF0Y2ggPSBtYXBEaXNwYXRjaChzdG9yZS5kaXNwYXRjaCwgcHJvcHMpO1xuICAgICAgICB2YXIgaXNGYWN0b3J5ID0gdHlwZW9mIG1hcHBlZERpc3BhdGNoID09PSAnZnVuY3Rpb24nO1xuXG4gICAgICAgIHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMgPSBpc0ZhY3RvcnkgPyBtYXBwZWREaXNwYXRjaCA6IG1hcERpc3BhdGNoO1xuICAgICAgICB0aGlzLmRvRGlzcGF0Y2hQcm9wc0RlcGVuZE9uT3duUHJvcHMgPSB0aGlzLmZpbmFsTWFwRGlzcGF0Y2hUb1Byb3BzLmxlbmd0aCAhPT0gMTtcblxuICAgICAgICBpZiAoaXNGYWN0b3J5KSB7XG4gICAgICAgICAgcmV0dXJuIHRoaXMuY29tcHV0ZURpc3BhdGNoUHJvcHMoc3RvcmUsIHByb3BzKTtcbiAgICAgICAgfVxuXG4gICAgICAgIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICAgICAgY2hlY2tTdGF0ZVNoYXBlKG1hcHBlZERpc3BhdGNoLCAnbWFwRGlzcGF0Y2hUb1Byb3BzJyk7XG4gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIG1hcHBlZERpc3BhdGNoO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUudXBkYXRlU3RhdGVQcm9wc0lmTmVlZGVkID0gZnVuY3Rpb24gdXBkYXRlU3RhdGVQcm9wc0lmTmVlZGVkKCkge1xuICAgICAgICB2YXIgbmV4dFN0YXRlUHJvcHMgPSB0aGlzLmNvbXB1dGVTdGF0ZVByb3BzKHRoaXMuc3RvcmUsIHRoaXMucHJvcHMpO1xuICAgICAgICBpZiAodGhpcy5zdGF0ZVByb3BzICYmICgwLCBfc2hhbGxvd0VxdWFsMltcImRlZmF1bHRcIl0pKG5leHRTdGF0ZVByb3BzLCB0aGlzLnN0YXRlUHJvcHMpKSB7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5zdGF0ZVByb3BzID0gbmV4dFN0YXRlUHJvcHM7XG4gICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUudXBkYXRlRGlzcGF0Y2hQcm9wc0lmTmVlZGVkID0gZnVuY3Rpb24gdXBkYXRlRGlzcGF0Y2hQcm9wc0lmTmVlZGVkKCkge1xuICAgICAgICB2YXIgbmV4dERpc3BhdGNoUHJvcHMgPSB0aGlzLmNvbXB1dGVEaXNwYXRjaFByb3BzKHRoaXMuc3RvcmUsIHRoaXMucHJvcHMpO1xuICAgICAgICBpZiAodGhpcy5kaXNwYXRjaFByb3BzICYmICgwLCBfc2hhbGxvd0VxdWFsMltcImRlZmF1bHRcIl0pKG5leHREaXNwYXRjaFByb3BzLCB0aGlzLmRpc3BhdGNoUHJvcHMpKSB7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5kaXNwYXRjaFByb3BzID0gbmV4dERpc3BhdGNoUHJvcHM7XG4gICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUudXBkYXRlTWVyZ2VkUHJvcHNJZk5lZWRlZCA9IGZ1bmN0aW9uIHVwZGF0ZU1lcmdlZFByb3BzSWZOZWVkZWQoKSB7XG4gICAgICAgIHZhciBuZXh0TWVyZ2VkUHJvcHMgPSBjb21wdXRlTWVyZ2VkUHJvcHModGhpcy5zdGF0ZVByb3BzLCB0aGlzLmRpc3BhdGNoUHJvcHMsIHRoaXMucHJvcHMpO1xuICAgICAgICBpZiAodGhpcy5tZXJnZWRQcm9wcyAmJiBjaGVja01lcmdlZEVxdWFscyAmJiAoMCwgX3NoYWxsb3dFcXVhbDJbXCJkZWZhdWx0XCJdKShuZXh0TWVyZ2VkUHJvcHMsIHRoaXMubWVyZ2VkUHJvcHMpKSB7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5tZXJnZWRQcm9wcyA9IG5leHRNZXJnZWRQcm9wcztcbiAgICAgICAgcmV0dXJuIHRydWU7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5pc1N1YnNjcmliZWQgPSBmdW5jdGlvbiBpc1N1YnNjcmliZWQoKSB7XG4gICAgICAgIHJldHVybiB0eXBlb2YgdGhpcy51bnN1YnNjcmliZSA9PT0gJ2Z1bmN0aW9uJztcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLnRyeVN1YnNjcmliZSA9IGZ1bmN0aW9uIHRyeVN1YnNjcmliZSgpIHtcbiAgICAgICAgaWYgKHNob3VsZFN1YnNjcmliZSAmJiAhdGhpcy51bnN1YnNjcmliZSkge1xuICAgICAgICAgIHRoaXMudW5zdWJzY3JpYmUgPSB0aGlzLnN0b3JlLnN1YnNjcmliZSh0aGlzLmhhbmRsZUNoYW5nZS5iaW5kKHRoaXMpKTtcbiAgICAgICAgICB0aGlzLmhhbmRsZUNoYW5nZSgpO1xuICAgICAgICB9XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS50cnlVbnN1YnNjcmliZSA9IGZ1bmN0aW9uIHRyeVVuc3Vic2NyaWJlKCkge1xuICAgICAgICBpZiAodGhpcy51bnN1YnNjcmliZSkge1xuICAgICAgICAgIHRoaXMudW5zdWJzY3JpYmUoKTtcbiAgICAgICAgICB0aGlzLnVuc3Vic2NyaWJlID0gbnVsbDtcbiAgICAgICAgfVxuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY29tcG9uZW50RGlkTW91bnQgPSBmdW5jdGlvbiBjb21wb25lbnREaWRNb3VudCgpIHtcbiAgICAgICAgdGhpcy50cnlTdWJzY3JpYmUoKTtcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMgPSBmdW5jdGlvbiBjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuICAgICAgICBpZiAoIXB1cmUgfHwgISgwLCBfc2hhbGxvd0VxdWFsMltcImRlZmF1bHRcIl0pKG5leHRQcm9wcywgdGhpcy5wcm9wcykpIHtcbiAgICAgICAgICB0aGlzLmhhdmVPd25Qcm9wc0NoYW5nZWQgPSB0cnVlO1xuICAgICAgICB9XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jb21wb25lbnRXaWxsVW5tb3VudCA9IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgICAgICB0aGlzLnRyeVVuc3Vic2NyaWJlKCk7XG4gICAgICAgIHRoaXMuY2xlYXJDYWNoZSgpO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY2xlYXJDYWNoZSA9IGZ1bmN0aW9uIGNsZWFyQ2FjaGUoKSB7XG4gICAgICAgIHRoaXMuZGlzcGF0Y2hQcm9wcyA9IG51bGw7XG4gICAgICAgIHRoaXMuc3RhdGVQcm9wcyA9IG51bGw7XG4gICAgICAgIHRoaXMubWVyZ2VkUHJvcHMgPSBudWxsO1xuICAgICAgICB0aGlzLmhhdmVPd25Qcm9wc0NoYW5nZWQgPSB0cnVlO1xuICAgICAgICB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkID0gdHJ1ZTtcbiAgICAgICAgdGhpcy5oYXZlU3RhdGVQcm9wc0JlZW5QcmVjYWxjdWxhdGVkID0gZmFsc2U7XG4gICAgICAgIHRoaXMuc3RhdGVQcm9wc1ByZWNhbGN1bGF0aW9uRXJyb3IgPSBudWxsO1xuICAgICAgICB0aGlzLnJlbmRlcmVkRWxlbWVudCA9IG51bGw7XG4gICAgICAgIHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMgPSBudWxsO1xuICAgICAgICB0aGlzLmZpbmFsTWFwU3RhdGVUb1Byb3BzID0gbnVsbDtcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmhhbmRsZUNoYW5nZSA9IGZ1bmN0aW9uIGhhbmRsZUNoYW5nZSgpIHtcbiAgICAgICAgaWYgKCF0aGlzLnVuc3Vic2NyaWJlKSB7XG4gICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIHN0b3JlU3RhdGUgPSB0aGlzLnN0b3JlLmdldFN0YXRlKCk7XG4gICAgICAgIHZhciBwcmV2U3RvcmVTdGF0ZSA9IHRoaXMuc3RhdGUuc3RvcmVTdGF0ZTtcbiAgICAgICAgaWYgKHB1cmUgJiYgcHJldlN0b3JlU3RhdGUgPT09IHN0b3JlU3RhdGUpIHtcbiAgICAgICAgICByZXR1cm47XG4gICAgICAgIH1cblxuICAgICAgICBpZiAocHVyZSAmJiAhdGhpcy5kb1N0YXRlUHJvcHNEZXBlbmRPbk93blByb3BzKSB7XG4gICAgICAgICAgdmFyIGhhdmVTdGF0ZVByb3BzQ2hhbmdlZCA9IHRyeUNhdGNoKHRoaXMudXBkYXRlU3RhdGVQcm9wc0lmTmVlZGVkLCB0aGlzKTtcbiAgICAgICAgICBpZiAoIWhhdmVTdGF0ZVByb3BzQ2hhbmdlZCkge1xuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICAgIH1cbiAgICAgICAgICBpZiAoaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID09PSBlcnJvck9iamVjdCkge1xuICAgICAgICAgICAgdGhpcy5zdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvciA9IGVycm9yT2JqZWN0LnZhbHVlO1xuICAgICAgICAgIH1cbiAgICAgICAgICB0aGlzLmhhdmVTdGF0ZVByb3BzQmVlblByZWNhbGN1bGF0ZWQgPSB0cnVlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5oYXNTdG9yZVN0YXRlQ2hhbmdlZCA9IHRydWU7XG4gICAgICAgIHRoaXMuc2V0U3RhdGUoeyBzdG9yZVN0YXRlOiBzdG9yZVN0YXRlIH0pO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuZ2V0V3JhcHBlZEluc3RhbmNlID0gZnVuY3Rpb24gZ2V0V3JhcHBlZEluc3RhbmNlKCkge1xuICAgICAgICAoMCwgX2ludmFyaWFudDJbXCJkZWZhdWx0XCJdKSh3aXRoUmVmLCAnVG8gYWNjZXNzIHRoZSB3cmFwcGVkIGluc3RhbmNlLCB5b3UgbmVlZCB0byBzcGVjaWZ5ICcgKyAneyB3aXRoUmVmOiB0cnVlIH0gYXMgdGhlIGZvdXJ0aCBhcmd1bWVudCBvZiB0aGUgY29ubmVjdCgpIGNhbGwuJyk7XG5cbiAgICAgICAgcmV0dXJuIHRoaXMucmVmcy53cmFwcGVkSW5zdGFuY2U7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5yZW5kZXIgPSBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgICAgIHZhciBoYXZlT3duUHJvcHNDaGFuZ2VkID0gdGhpcy5oYXZlT3duUHJvcHNDaGFuZ2VkO1xuICAgICAgICB2YXIgaGFzU3RvcmVTdGF0ZUNoYW5nZWQgPSB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkO1xuICAgICAgICB2YXIgaGF2ZVN0YXRlUHJvcHNCZWVuUHJlY2FsY3VsYXRlZCA9IHRoaXMuaGF2ZVN0YXRlUHJvcHNCZWVuUHJlY2FsY3VsYXRlZDtcbiAgICAgICAgdmFyIHN0YXRlUHJvcHNQcmVjYWxjdWxhdGlvbkVycm9yID0gdGhpcy5zdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvcjtcbiAgICAgICAgdmFyIHJlbmRlcmVkRWxlbWVudCA9IHRoaXMucmVuZGVyZWRFbGVtZW50O1xuXG4gICAgICAgIHRoaXMuaGF2ZU93blByb3BzQ2hhbmdlZCA9IGZhbHNlO1xuICAgICAgICB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkID0gZmFsc2U7XG4gICAgICAgIHRoaXMuaGF2ZVN0YXRlUHJvcHNCZWVuUHJlY2FsY3VsYXRlZCA9IGZhbHNlO1xuICAgICAgICB0aGlzLnN0YXRlUHJvcHNQcmVjYWxjdWxhdGlvbkVycm9yID0gbnVsbDtcblxuICAgICAgICBpZiAoc3RhdGVQcm9wc1ByZWNhbGN1bGF0aW9uRXJyb3IpIHtcbiAgICAgICAgICB0aHJvdyBzdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvcjtcbiAgICAgICAgfVxuXG4gICAgICAgIHZhciBzaG91bGRVcGRhdGVTdGF0ZVByb3BzID0gdHJ1ZTtcbiAgICAgICAgdmFyIHNob3VsZFVwZGF0ZURpc3BhdGNoUHJvcHMgPSB0cnVlO1xuICAgICAgICBpZiAocHVyZSAmJiByZW5kZXJlZEVsZW1lbnQpIHtcbiAgICAgICAgICBzaG91bGRVcGRhdGVTdGF0ZVByb3BzID0gaGFzU3RvcmVTdGF0ZUNoYW5nZWQgfHwgaGF2ZU93blByb3BzQ2hhbmdlZCAmJiB0aGlzLmRvU3RhdGVQcm9wc0RlcGVuZE9uT3duUHJvcHM7XG4gICAgICAgICAgc2hvdWxkVXBkYXRlRGlzcGF0Y2hQcm9wcyA9IGhhdmVPd25Qcm9wc0NoYW5nZWQgJiYgdGhpcy5kb0Rpc3BhdGNoUHJvcHNEZXBlbmRPbk93blByb3BzO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIGhhdmVTdGF0ZVByb3BzQ2hhbmdlZCA9IGZhbHNlO1xuICAgICAgICB2YXIgaGF2ZURpc3BhdGNoUHJvcHNDaGFuZ2VkID0gZmFsc2U7XG4gICAgICAgIGlmIChoYXZlU3RhdGVQcm9wc0JlZW5QcmVjYWxjdWxhdGVkKSB7XG4gICAgICAgICAgaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID0gdHJ1ZTtcbiAgICAgICAgfSBlbHNlIGlmIChzaG91bGRVcGRhdGVTdGF0ZVByb3BzKSB7XG4gICAgICAgICAgaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID0gdGhpcy51cGRhdGVTdGF0ZVByb3BzSWZOZWVkZWQoKTtcbiAgICAgICAgfVxuICAgICAgICBpZiAoc2hvdWxkVXBkYXRlRGlzcGF0Y2hQcm9wcykge1xuICAgICAgICAgIGhhdmVEaXNwYXRjaFByb3BzQ2hhbmdlZCA9IHRoaXMudXBkYXRlRGlzcGF0Y2hQcm9wc0lmTmVlZGVkKCk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgaGF2ZU1lcmdlZFByb3BzQ2hhbmdlZCA9IHRydWU7XG4gICAgICAgIGlmIChoYXZlU3RhdGVQcm9wc0NoYW5nZWQgfHwgaGF2ZURpc3BhdGNoUHJvcHNDaGFuZ2VkIHx8IGhhdmVPd25Qcm9wc0NoYW5nZWQpIHtcbiAgICAgICAgICBoYXZlTWVyZ2VkUHJvcHNDaGFuZ2VkID0gdGhpcy51cGRhdGVNZXJnZWRQcm9wc0lmTmVlZGVkKCk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgaGF2ZU1lcmdlZFByb3BzQ2hhbmdlZCA9IGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKCFoYXZlTWVyZ2VkUHJvcHNDaGFuZ2VkICYmIHJlbmRlcmVkRWxlbWVudCkge1xuICAgICAgICAgIHJldHVybiByZW5kZXJlZEVsZW1lbnQ7XG4gICAgICAgIH1cblxuICAgICAgICBpZiAod2l0aFJlZikge1xuICAgICAgICAgIHRoaXMucmVuZGVyZWRFbGVtZW50ID0gKDAsIF9yZWFjdC5jcmVhdGVFbGVtZW50KShXcmFwcGVkQ29tcG9uZW50LCBfZXh0ZW5kcyh7fSwgdGhpcy5tZXJnZWRQcm9wcywge1xuICAgICAgICAgICAgcmVmOiAnd3JhcHBlZEluc3RhbmNlJ1xuICAgICAgICAgIH0pKTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB0aGlzLnJlbmRlcmVkRWxlbWVudCA9ICgwLCBfcmVhY3QuY3JlYXRlRWxlbWVudCkoV3JhcHBlZENvbXBvbmVudCwgdGhpcy5tZXJnZWRQcm9wcyk7XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gdGhpcy5yZW5kZXJlZEVsZW1lbnQ7XG4gICAgICB9O1xuXG4gICAgICByZXR1cm4gQ29ubmVjdDtcbiAgICB9KF9yZWFjdC5Db21wb25lbnQpO1xuXG4gICAgQ29ubmVjdC5kaXNwbGF5TmFtZSA9IGNvbm5lY3REaXNwbGF5TmFtZTtcbiAgICBDb25uZWN0LldyYXBwZWRDb21wb25lbnQgPSBXcmFwcGVkQ29tcG9uZW50O1xuICAgIENvbm5lY3QuY29udGV4dFR5cGVzID0ge1xuICAgICAgc3RvcmU6IF9zdG9yZVNoYXBlMltcImRlZmF1bHRcIl1cbiAgICB9O1xuICAgIENvbm5lY3QucHJvcFR5cGVzID0ge1xuICAgICAgc3RvcmU6IF9zdG9yZVNoYXBlMltcImRlZmF1bHRcIl1cbiAgICB9O1xuXG4gICAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbXBvbmVudFdpbGxVcGRhdGUgPSBmdW5jdGlvbiBjb21wb25lbnRXaWxsVXBkYXRlKCkge1xuICAgICAgICBpZiAodGhpcy52ZXJzaW9uID09PSB2ZXJzaW9uKSB7XG4gICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG5cbiAgICAgICAgLy8gV2UgYXJlIGhvdCByZWxvYWRpbmchXG4gICAgICAgIHRoaXMudmVyc2lvbiA9IHZlcnNpb247XG4gICAgICAgIHRoaXMudHJ5U3Vic2NyaWJlKCk7XG4gICAgICAgIHRoaXMuY2xlYXJDYWNoZSgpO1xuICAgICAgfTtcbiAgICB9XG5cbiAgICByZXR1cm4gKDAsIF9ob2lzdE5vblJlYWN0U3RhdGljczJbXCJkZWZhdWx0XCJdKShDb25uZWN0LCBXcmFwcGVkQ29tcG9uZW50KTtcbiAgfTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmNvbm5lY3QgPSBleHBvcnRzLlByb3ZpZGVyID0gdW5kZWZpbmVkO1xuXG52YXIgX1Byb3ZpZGVyID0gcmVxdWlyZSgnLi9jb21wb25lbnRzL1Byb3ZpZGVyJyk7XG5cbnZhciBfUHJvdmlkZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUHJvdmlkZXIpO1xuXG52YXIgX2Nvbm5lY3QgPSByZXF1aXJlKCcuL2NvbXBvbmVudHMvY29ubmVjdCcpO1xuXG52YXIgX2Nvbm5lY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29ubmVjdCk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG5leHBvcnRzLlByb3ZpZGVyID0gX1Byb3ZpZGVyMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmNvbm5lY3QgPSBfY29ubmVjdDJbXCJkZWZhdWx0XCJdOyIsIlwidXNlIHN0cmljdFwiO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBzaGFsbG93RXF1YWw7XG5mdW5jdGlvbiBzaGFsbG93RXF1YWwob2JqQSwgb2JqQikge1xuICBpZiAob2JqQSA9PT0gb2JqQikge1xuICAgIHJldHVybiB0cnVlO1xuICB9XG5cbiAgdmFyIGtleXNBID0gT2JqZWN0LmtleXMob2JqQSk7XG4gIHZhciBrZXlzQiA9IE9iamVjdC5rZXlzKG9iakIpO1xuXG4gIGlmIChrZXlzQS5sZW5ndGggIT09IGtleXNCLmxlbmd0aCkge1xuICAgIHJldHVybiBmYWxzZTtcbiAgfVxuXG4gIC8vIFRlc3QgZm9yIEEncyBrZXlzIGRpZmZlcmVudCBmcm9tIEIuXG4gIHZhciBoYXNPd24gPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5O1xuICBmb3IgKHZhciBpID0gMDsgaSA8IGtleXNBLmxlbmd0aDsgaSsrKSB7XG4gICAgaWYgKCFoYXNPd24uY2FsbChvYmpCLCBrZXlzQVtpXSkgfHwgb2JqQVtrZXlzQVtpXV0gIT09IG9iakJba2V5c0FbaV1dKSB7XG4gICAgICByZXR1cm4gZmFsc2U7XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIHRydWU7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBfcmVhY3QuUHJvcFR5cGVzLnNoYXBlKHtcbiAgc3Vic2NyaWJlOiBfcmVhY3QuUHJvcFR5cGVzLmZ1bmMuaXNSZXF1aXJlZCxcbiAgZGlzcGF0Y2g6IF9yZWFjdC5Qcm9wVHlwZXMuZnVuYy5pc1JlcXVpcmVkLFxuICBnZXRTdGF0ZTogX3JlYWN0LlByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWRcbn0pOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gd2FybmluZztcbi8qKlxuICogUHJpbnRzIGEgd2FybmluZyBpbiB0aGUgY29uc29sZSBpZiBpdCBleGlzdHMuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd9IG1lc3NhZ2UgVGhlIHdhcm5pbmcgbWVzc2FnZS5cbiAqIEByZXR1cm5zIHt2b2lkfVxuICovXG5mdW5jdGlvbiB3YXJuaW5nKG1lc3NhZ2UpIHtcbiAgLyogZXNsaW50LWRpc2FibGUgbm8tY29uc29sZSAqL1xuICBpZiAodHlwZW9mIGNvbnNvbGUgIT09ICd1bmRlZmluZWQnICYmIHR5cGVvZiBjb25zb2xlLmVycm9yID09PSAnZnVuY3Rpb24nKSB7XG4gICAgY29uc29sZS5lcnJvcihtZXNzYWdlKTtcbiAgfVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWNvbnNvbGUgKi9cbiAgdHJ5IHtcbiAgICAvLyBUaGlzIGVycm9yIHdhcyB0aHJvd24gYXMgYSBjb252ZW5pZW5jZSBzbyB0aGF0IHlvdSBjYW4gdXNlIHRoaXMgc3RhY2tcbiAgICAvLyB0byBmaW5kIHRoZSBjYWxsc2l0ZSB0aGF0IGNhdXNlZCB0aGlzIHdhcm5pbmcgdG8gZmlyZS5cbiAgICB0aHJvdyBuZXcgRXJyb3IobWVzc2FnZSk7XG4gICAgLyogZXNsaW50LWRpc2FibGUgbm8tZW1wdHkgKi9cbiAgfSBjYXRjaCAoZSkge31cbiAgLyogZXNsaW50LWVuYWJsZSBuby1lbXB0eSAqL1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gd3JhcEFjdGlvbkNyZWF0b3JzO1xuXG52YXIgX3JlZHV4ID0gcmVxdWlyZSgncmVkdXgnKTtcblxuZnVuY3Rpb24gd3JhcEFjdGlvbkNyZWF0b3JzKGFjdGlvbkNyZWF0b3JzKSB7XG4gIHJldHVybiBmdW5jdGlvbiAoZGlzcGF0Y2gpIHtcbiAgICByZXR1cm4gKDAsIF9yZWR1eC5iaW5kQWN0aW9uQ3JlYXRvcnMpKGFjdGlvbkNyZWF0b3JzLCBkaXNwYXRjaCk7XG4gIH07XG59IiwiXCJ1c2Ugc3RyaWN0XCI7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmxvb3BBc3luYyA9IGxvb3BBc3luYztcbmV4cG9ydHMubWFwQXN5bmMgPSBtYXBBc3luYztcbmZ1bmN0aW9uIGxvb3BBc3luYyh0dXJucywgd29yaywgY2FsbGJhY2spIHtcbiAgdmFyIGN1cnJlbnRUdXJuID0gMCxcbiAgICAgIGlzRG9uZSA9IGZhbHNlO1xuICB2YXIgc3luYyA9IGZhbHNlLFxuICAgICAgaGFzTmV4dCA9IGZhbHNlLFxuICAgICAgZG9uZUFyZ3MgPSB2b2lkIDA7XG5cbiAgZnVuY3Rpb24gZG9uZSgpIHtcbiAgICBpc0RvbmUgPSB0cnVlO1xuICAgIGlmIChzeW5jKSB7XG4gICAgICAvLyBJdGVyYXRlIGluc3RlYWQgb2YgcmVjdXJzaW5nIGlmIHBvc3NpYmxlLlxuICAgICAgZG9uZUFyZ3MgPSBbXS5jb25jYXQoQXJyYXkucHJvdG90eXBlLnNsaWNlLmNhbGwoYXJndW1lbnRzKSk7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgY2FsbGJhY2suYXBwbHkodGhpcywgYXJndW1lbnRzKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIG5leHQoKSB7XG4gICAgaWYgKGlzRG9uZSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGhhc05leHQgPSB0cnVlO1xuICAgIGlmIChzeW5jKSB7XG4gICAgICAvLyBJdGVyYXRlIGluc3RlYWQgb2YgcmVjdXJzaW5nIGlmIHBvc3NpYmxlLlxuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIHN5bmMgPSB0cnVlO1xuXG4gICAgd2hpbGUgKCFpc0RvbmUgJiYgY3VycmVudFR1cm4gPCB0dXJucyAmJiBoYXNOZXh0KSB7XG4gICAgICBoYXNOZXh0ID0gZmFsc2U7XG4gICAgICB3b3JrLmNhbGwodGhpcywgY3VycmVudFR1cm4rKywgbmV4dCwgZG9uZSk7XG4gICAgfVxuXG4gICAgc3luYyA9IGZhbHNlO1xuXG4gICAgaWYgKGlzRG9uZSkge1xuICAgICAgLy8gVGhpcyBtZWFucyB0aGUgbG9vcCBmaW5pc2hlZCBzeW5jaHJvbm91c2x5LlxuICAgICAgY2FsbGJhY2suYXBwbHkodGhpcywgZG9uZUFyZ3MpO1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGlmIChjdXJyZW50VHVybiA+PSB0dXJucyAmJiBoYXNOZXh0KSB7XG4gICAgICBpc0RvbmUgPSB0cnVlO1xuICAgICAgY2FsbGJhY2soKTtcbiAgICB9XG4gIH1cblxuICBuZXh0KCk7XG59XG5cbmZ1bmN0aW9uIG1hcEFzeW5jKGFycmF5LCB3b3JrLCBjYWxsYmFjaykge1xuICB2YXIgbGVuZ3RoID0gYXJyYXkubGVuZ3RoO1xuICB2YXIgdmFsdWVzID0gW107XG5cbiAgaWYgKGxlbmd0aCA9PT0gMCkgcmV0dXJuIGNhbGxiYWNrKG51bGwsIHZhbHVlcyk7XG5cbiAgdmFyIGlzRG9uZSA9IGZhbHNlLFxuICAgICAgZG9uZUNvdW50ID0gMDtcblxuICBmdW5jdGlvbiBkb25lKGluZGV4LCBlcnJvciwgdmFsdWUpIHtcbiAgICBpZiAoaXNEb25lKSByZXR1cm47XG5cbiAgICBpZiAoZXJyb3IpIHtcbiAgICAgIGlzRG9uZSA9IHRydWU7XG4gICAgICBjYWxsYmFjayhlcnJvcik7XG4gICAgfSBlbHNlIHtcbiAgICAgIHZhbHVlc1tpbmRleF0gPSB2YWx1ZTtcblxuICAgICAgaXNEb25lID0gKytkb25lQ291bnQgPT09IGxlbmd0aDtcblxuICAgICAgaWYgKGlzRG9uZSkgY2FsbGJhY2sobnVsbCwgdmFsdWVzKTtcbiAgICB9XG4gIH1cblxuICBhcnJheS5mb3JFYWNoKGZ1bmN0aW9uIChpdGVtLCBpbmRleCkge1xuICAgIHdvcmsoaXRlbSwgaW5kZXgsIGZ1bmN0aW9uIChlcnJvciwgdmFsdWUpIHtcbiAgICAgIGRvbmUoaW5kZXgsIGVycm9yLCB2YWx1ZSk7XG4gICAgfSk7XG4gIH0pO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9JbnRlcm5hbFByb3BUeXBlcyA9IHJlcXVpcmUoJy4vSW50ZXJuYWxQcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuLyoqXG4gKiBBIG1peGluIHRoYXQgYWRkcyB0aGUgXCJoaXN0b3J5XCIgaW5zdGFuY2UgdmFyaWFibGUgdG8gY29tcG9uZW50cy5cbiAqL1xudmFyIEhpc3RvcnkgPSB7XG5cbiAgY29udGV4dFR5cGVzOiB7XG4gICAgaGlzdG9yeTogX0ludGVybmFsUHJvcFR5cGVzLmhpc3RvcnlcbiAgfSxcblxuICBjb21wb25lbnRXaWxsTW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxNb3VudCgpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ3RoZSBgSGlzdG9yeWAgbWl4aW4gaXMgZGVwcmVjYXRlZCwgcGxlYXNlIGFjY2VzcyBgY29udGV4dC5yb3V0ZXJgIHdpdGggeW91ciBvd24gYGNvbnRleHRUeXBlc2AuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1oaXN0b3J5bWl4aW4nKSA6IHZvaWQgMDtcbiAgICB0aGlzLmhpc3RvcnkgPSB0aGlzLmNvbnRleHQuaGlzdG9yeTtcbiAgfVxufTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gSGlzdG9yeTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX0xpbmsgPSByZXF1aXJlKCcuL0xpbmsnKTtcblxudmFyIF9MaW5rMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0xpbmspO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG4vKipcbiAqIEFuIDxJbmRleExpbms+IGlzIHVzZWQgdG8gbGluayB0byBhbiA8SW5kZXhSb3V0ZT4uXG4gKi9cbnZhciBJbmRleExpbmsgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ0luZGV4TGluaycsXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHJldHVybiBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudChfTGluazIuZGVmYXVsdCwgX2V4dGVuZHMoe30sIHRoaXMucHJvcHMsIHsgb25seUFjdGl2ZU9uSW5kZXg6IHRydWUgfSkpO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gSW5kZXhMaW5rO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfUmVkaXJlY3QgPSByZXF1aXJlKCcuL1JlZGlyZWN0Jyk7XG5cbnZhciBfUmVkaXJlY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUmVkaXJlY3QpO1xuXG52YXIgX0ludGVybmFsUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9JbnRlcm5hbFByb3BUeXBlcycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgc3RyaW5nID0gX1JlYWN0JFByb3BUeXBlcy5zdHJpbmc7XG52YXIgb2JqZWN0ID0gX1JlYWN0JFByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogQW4gPEluZGV4UmVkaXJlY3Q+IGlzIHVzZWQgdG8gcmVkaXJlY3QgZnJvbSBhbiBpbmRleFJvdXRlLlxuICovXG5cbnZhciBJbmRleFJlZGlyZWN0ID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdJbmRleFJlZGlyZWN0JyxcblxuXG4gIHN0YXRpY3M6IHtcbiAgICBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQ6IGZ1bmN0aW9uIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50LCBwYXJlbnRSb3V0ZSkge1xuICAgICAgLyogaXN0YW5idWwgaWdub3JlIGVsc2U6IHNhbml0eSBjaGVjayAqL1xuICAgICAgaWYgKHBhcmVudFJvdXRlKSB7XG4gICAgICAgIHBhcmVudFJvdXRlLmluZGV4Um91dGUgPSBfUmVkaXJlY3QyLmRlZmF1bHQuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdBbiA8SW5kZXhSZWRpcmVjdD4gZG9lcyBub3QgbWFrZSBzZW5zZSBhdCB0aGUgcm9vdCBvZiB5b3VyIHJvdXRlIGNvbmZpZycpIDogdm9pZCAwO1xuICAgICAgfVxuICAgIH1cbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICB0bzogc3RyaW5nLmlzUmVxdWlyZWQsXG4gICAgcXVlcnk6IG9iamVjdCxcbiAgICBzdGF0ZTogb2JqZWN0LFxuICAgIG9uRW50ZXI6IF9JbnRlcm5hbFByb3BUeXBlcy5mYWxzeSxcbiAgICBjaGlsZHJlbjogX0ludGVybmFsUHJvcFR5cGVzLmZhbHN5XG4gIH0sXG5cbiAgLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHNhbml0eSBjaGVjayAqL1xuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICAhZmFsc2UgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnPEluZGV4UmVkaXJlY3Q+IGVsZW1lbnRzIGFyZSBmb3Igcm91dGVyIGNvbmZpZ3VyYXRpb24gb25seSBhbmQgc2hvdWxkIG5vdCBiZSByZW5kZXJlZCcpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IEluZGV4UmVkaXJlY3Q7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbnZhciBfSW50ZXJuYWxQcm9wVHlwZXMgPSByZXF1aXJlKCcuL0ludGVybmFsUHJvcFR5cGVzJyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBmdW5jID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcy5mdW5jO1xuXG4vKipcbiAqIEFuIDxJbmRleFJvdXRlPiBpcyB1c2VkIHRvIHNwZWNpZnkgaXRzIHBhcmVudCdzIDxSb3V0ZSBpbmRleFJvdXRlPiBpblxuICogYSBKU1ggcm91dGUgY29uZmlnLlxuICovXG5cbnZhciBJbmRleFJvdXRlID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdJbmRleFJvdXRlJyxcblxuXG4gIHN0YXRpY3M6IHtcbiAgICBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQ6IGZ1bmN0aW9uIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50LCBwYXJlbnRSb3V0ZSkge1xuICAgICAgLyogaXN0YW5idWwgaWdub3JlIGVsc2U6IHNhbml0eSBjaGVjayAqL1xuICAgICAgaWYgKHBhcmVudFJvdXRlKSB7XG4gICAgICAgIHBhcmVudFJvdXRlLmluZGV4Um91dGUgPSAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KShlbGVtZW50KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnQW4gPEluZGV4Um91dGU+IGRvZXMgbm90IG1ha2Ugc2Vuc2UgYXQgdGhlIHJvb3Qgb2YgeW91ciByb3V0ZSBjb25maWcnKSA6IHZvaWQgMDtcbiAgICAgIH1cbiAgICB9XG4gIH0sXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgcGF0aDogX0ludGVybmFsUHJvcFR5cGVzLmZhbHN5LFxuICAgIGNvbXBvbmVudDogX0ludGVybmFsUHJvcFR5cGVzLmNvbXBvbmVudCxcbiAgICBjb21wb25lbnRzOiBfSW50ZXJuYWxQcm9wVHlwZXMuY29tcG9uZW50cyxcbiAgICBnZXRDb21wb25lbnQ6IGZ1bmMsXG4gICAgZ2V0Q29tcG9uZW50czogZnVuY1xuICB9LFxuXG4gIC8qIGlzdGFuYnVsIGlnbm9yZSBuZXh0OiBzYW5pdHkgY2hlY2sgKi9cbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgIWZhbHNlID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJzxJbmRleFJvdXRlPiBlbGVtZW50cyBhcmUgZm9yIHJvdXRlciBjb25maWd1cmF0aW9uIG9ubHkgYW5kIHNob3VsZCBub3QgYmUgcmVuZGVyZWQnKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBJbmRleFJvdXRlO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5yb3V0ZXMgPSBleHBvcnRzLnJvdXRlID0gZXhwb3J0cy5jb21wb25lbnRzID0gZXhwb3J0cy5jb21wb25lbnQgPSBleHBvcnRzLmhpc3RvcnkgPSB1bmRlZmluZWQ7XG5leHBvcnRzLmZhbHN5ID0gZmFsc3k7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgZnVuYyA9IF9yZWFjdC5Qcm9wVHlwZXMuZnVuYztcbnZhciBvYmplY3QgPSBfcmVhY3QuUHJvcFR5cGVzLm9iamVjdDtcbnZhciBhcnJheU9mID0gX3JlYWN0LlByb3BUeXBlcy5hcnJheU9mO1xudmFyIG9uZU9mVHlwZSA9IF9yZWFjdC5Qcm9wVHlwZXMub25lT2ZUeXBlO1xudmFyIGVsZW1lbnQgPSBfcmVhY3QuUHJvcFR5cGVzLmVsZW1lbnQ7XG52YXIgc2hhcGUgPSBfcmVhY3QuUHJvcFR5cGVzLnNoYXBlO1xudmFyIHN0cmluZyA9IF9yZWFjdC5Qcm9wVHlwZXMuc3RyaW5nO1xuZnVuY3Rpb24gZmFsc3kocHJvcHMsIHByb3BOYW1lLCBjb21wb25lbnROYW1lKSB7XG4gIGlmIChwcm9wc1twcm9wTmFtZV0pIHJldHVybiBuZXcgRXJyb3IoJzwnICsgY29tcG9uZW50TmFtZSArICc+IHNob3VsZCBub3QgaGF2ZSBhIFwiJyArIHByb3BOYW1lICsgJ1wiIHByb3AnKTtcbn1cblxudmFyIGhpc3RvcnkgPSBleHBvcnRzLmhpc3RvcnkgPSBzaGFwZSh7XG4gIGxpc3RlbjogZnVuYy5pc1JlcXVpcmVkLFxuICBwdXNoOiBmdW5jLmlzUmVxdWlyZWQsXG4gIHJlcGxhY2U6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ286IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ29CYWNrOiBmdW5jLmlzUmVxdWlyZWQsXG4gIGdvRm9yd2FyZDogZnVuYy5pc1JlcXVpcmVkXG59KTtcblxudmFyIGNvbXBvbmVudCA9IGV4cG9ydHMuY29tcG9uZW50ID0gb25lT2ZUeXBlKFtmdW5jLCBzdHJpbmddKTtcbnZhciBjb21wb25lbnRzID0gZXhwb3J0cy5jb21wb25lbnRzID0gb25lT2ZUeXBlKFtjb21wb25lbnQsIG9iamVjdF0pO1xudmFyIHJvdXRlID0gZXhwb3J0cy5yb3V0ZSA9IG9uZU9mVHlwZShbb2JqZWN0LCBlbGVtZW50XSk7XG52YXIgcm91dGVzID0gZXhwb3J0cy5yb3V0ZXMgPSBvbmVPZlR5cGUoW3JvdXRlLCBhcnJheU9mKHJvdXRlKV0pOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgb2JqZWN0ID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogVGhlIExpZmVjeWNsZSBtaXhpbiBhZGRzIHRoZSByb3V0ZXJXaWxsTGVhdmUgbGlmZWN5Y2xlIG1ldGhvZCB0byBhXG4gKiBjb21wb25lbnQgdGhhdCBtYXkgYmUgdXNlZCB0byBjYW5jZWwgYSB0cmFuc2l0aW9uIG9yIHByb21wdCB0aGUgdXNlclxuICogZm9yIGNvbmZpcm1hdGlvbi5cbiAqXG4gKiBPbiBzdGFuZGFyZCB0cmFuc2l0aW9ucywgcm91dGVyV2lsbExlYXZlIHJlY2VpdmVzIGEgc2luZ2xlIGFyZ3VtZW50OiB0aGVcbiAqIGxvY2F0aW9uIHdlJ3JlIHRyYW5zaXRpb25pbmcgdG8uIFRvIGNhbmNlbCB0aGUgdHJhbnNpdGlvbiwgcmV0dXJuIGZhbHNlLlxuICogVG8gcHJvbXB0IHRoZSB1c2VyIGZvciBjb25maXJtYXRpb24sIHJldHVybiBhIHByb21wdCBtZXNzYWdlIChzdHJpbmcpLlxuICpcbiAqIER1cmluZyB0aGUgYmVmb3JldW5sb2FkIGV2ZW50IChhc3N1bWluZyB5b3UncmUgdXNpbmcgdGhlIHVzZUJlZm9yZVVubG9hZFxuICogaGlzdG9yeSBlbmhhbmNlciksIHJvdXRlcldpbGxMZWF2ZSBkb2VzIG5vdCByZWNlaXZlIGEgbG9jYXRpb24gb2JqZWN0XG4gKiBiZWNhdXNlIGl0IGlzbid0IHBvc3NpYmxlIGZvciB1cyB0byBrbm93IHRoZSBsb2NhdGlvbiB3ZSdyZSB0cmFuc2l0aW9uaW5nXG4gKiB0by4gSW4gdGhpcyBjYXNlIHJvdXRlcldpbGxMZWF2ZSBtdXN0IHJldHVybiBhIHByb21wdCBtZXNzYWdlIHRvIHByZXZlbnRcbiAqIHRoZSB1c2VyIGZyb20gY2xvc2luZyB0aGUgd2luZG93L3RhYi5cbiAqL1xuXG52YXIgTGlmZWN5Y2xlID0ge1xuXG4gIGNvbnRleHRUeXBlczoge1xuICAgIGhpc3Rvcnk6IG9iamVjdC5pc1JlcXVpcmVkLFxuICAgIC8vIE5lc3RlZCBjaGlsZHJlbiByZWNlaXZlIHRoZSByb3V0ZSBhcyBjb250ZXh0LCBlaXRoZXJcbiAgICAvLyBzZXQgYnkgdGhlIHJvdXRlIGNvbXBvbmVudCB1c2luZyB0aGUgUm91dGVDb250ZXh0IG1peGluXG4gICAgLy8gb3IgYnkgc29tZSBvdGhlciBhbmNlc3Rvci5cbiAgICByb3V0ZTogb2JqZWN0XG4gIH0sXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgLy8gUm91dGUgY29tcG9uZW50cyByZWNlaXZlIHRoZSByb3V0ZSBvYmplY3QgYXMgYSBwcm9wLlxuICAgIHJvdXRlOiBvYmplY3RcbiAgfSxcblxuICBjb21wb25lbnREaWRNb3VudDogZnVuY3Rpb24gY29tcG9uZW50RGlkTW91bnQoKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICd0aGUgYExpZmVjeWNsZWAgbWl4aW4gaXMgZGVwcmVjYXRlZCwgcGxlYXNlIHVzZSBgY29udGV4dC5yb3V0ZXIuc2V0Um91dGVMZWF2ZUhvb2socm91dGUsIGhvb2spYC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWxpZmVjeWNsZW1peGluJykgOiB2b2lkIDA7XG4gICAgIXRoaXMucm91dGVyV2lsbExlYXZlID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJ1RoZSBMaWZlY3ljbGUgbWl4aW4gcmVxdWlyZXMgeW91IHRvIGRlZmluZSBhIHJvdXRlcldpbGxMZWF2ZSBtZXRob2QnKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICB2YXIgcm91dGUgPSB0aGlzLnByb3BzLnJvdXRlIHx8IHRoaXMuY29udGV4dC5yb3V0ZTtcblxuICAgICFyb3V0ZSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdUaGUgTGlmZWN5Y2xlIG1peGluIG11c3QgYmUgdXNlZCBvbiBlaXRoZXIgYSkgYSA8Um91dGUgY29tcG9uZW50PiBvciAnICsgJ2IpIGEgZGVzY2VuZGFudCBvZiBhIDxSb3V0ZSBjb21wb25lbnQ+IHRoYXQgdXNlcyB0aGUgUm91dGVDb250ZXh0IG1peGluJykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuXG4gICAgdGhpcy5fdW5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUgPSB0aGlzLmNvbnRleHQuaGlzdG9yeS5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUocm91dGUsIHRoaXMucm91dGVyV2lsbExlYXZlKTtcbiAgfSxcbiAgY29tcG9uZW50V2lsbFVubW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgIGlmICh0aGlzLl91bmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZSkgdGhpcy5fdW5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUoKTtcbiAgfVxufTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gTGlmZWN5Y2xlO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9Qcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKG9iaiwga2V5cykgeyB2YXIgdGFyZ2V0ID0ge307IGZvciAodmFyIGkgaW4gb2JqKSB7IGlmIChrZXlzLmluZGV4T2YoaSkgPj0gMCkgY29udGludWU7IGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwgaSkpIGNvbnRpbnVlOyB0YXJnZXRbaV0gPSBvYmpbaV07IH0gcmV0dXJuIHRhcmdldDsgfVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgYm9vbCA9IF9SZWFjdCRQcm9wVHlwZXMuYm9vbDtcbnZhciBvYmplY3QgPSBfUmVhY3QkUHJvcFR5cGVzLm9iamVjdDtcbnZhciBzdHJpbmcgPSBfUmVhY3QkUHJvcFR5cGVzLnN0cmluZztcbnZhciBmdW5jID0gX1JlYWN0JFByb3BUeXBlcy5mdW5jO1xudmFyIG9uZU9mVHlwZSA9IF9SZWFjdCRQcm9wVHlwZXMub25lT2ZUeXBlO1xuXG5cbmZ1bmN0aW9uIGlzTGVmdENsaWNrRXZlbnQoZXZlbnQpIHtcbiAgcmV0dXJuIGV2ZW50LmJ1dHRvbiA9PT0gMDtcbn1cblxuZnVuY3Rpb24gaXNNb2RpZmllZEV2ZW50KGV2ZW50KSB7XG4gIHJldHVybiAhIShldmVudC5tZXRhS2V5IHx8IGV2ZW50LmFsdEtleSB8fCBldmVudC5jdHJsS2V5IHx8IGV2ZW50LnNoaWZ0S2V5KTtcbn1cblxuLy8gVE9ETzogRGUtZHVwbGljYXRlIGFnYWluc3QgaGFzQW55UHJvcGVydGllcyBpbiBjcmVhdGVUcmFuc2l0aW9uTWFuYWdlci5cbmZ1bmN0aW9uIGlzRW1wdHlPYmplY3Qob2JqZWN0KSB7XG4gIGZvciAodmFyIHAgaW4gb2JqZWN0KSB7XG4gICAgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmplY3QsIHApKSByZXR1cm4gZmFsc2U7XG4gIH1yZXR1cm4gdHJ1ZTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlTG9jYXRpb25EZXNjcmlwdG9yKHRvLCBfcmVmKSB7XG4gIHZhciBxdWVyeSA9IF9yZWYucXVlcnk7XG4gIHZhciBoYXNoID0gX3JlZi5oYXNoO1xuICB2YXIgc3RhdGUgPSBfcmVmLnN0YXRlO1xuXG4gIGlmIChxdWVyeSB8fCBoYXNoIHx8IHN0YXRlKSB7XG4gICAgcmV0dXJuIHsgcGF0aG5hbWU6IHRvLCBxdWVyeTogcXVlcnksIGhhc2g6IGhhc2gsIHN0YXRlOiBzdGF0ZSB9O1xuICB9XG5cbiAgcmV0dXJuIHRvO1xufVxuXG4vKipcbiAqIEEgPExpbms+IGlzIHVzZWQgdG8gY3JlYXRlIGFuIDxhPiBlbGVtZW50IHRoYXQgbGlua3MgdG8gYSByb3V0ZS5cbiAqIFdoZW4gdGhhdCByb3V0ZSBpcyBhY3RpdmUsIHRoZSBsaW5rIGdldHMgdGhlIHZhbHVlIG9mIGl0c1xuICogYWN0aXZlQ2xhc3NOYW1lIHByb3AuXG4gKlxuICogRm9yIGV4YW1wbGUsIGFzc3VtaW5nIHlvdSBoYXZlIHRoZSBmb2xsb3dpbmcgcm91dGU6XG4gKlxuICogICA8Um91dGUgcGF0aD1cIi9wb3N0cy86cG9zdElEXCIgY29tcG9uZW50PXtQb3N0fSAvPlxuICpcbiAqIFlvdSBjb3VsZCB1c2UgdGhlIGZvbGxvd2luZyBjb21wb25lbnQgdG8gbGluayB0byB0aGF0IHJvdXRlOlxuICpcbiAqICAgPExpbmsgdG89e2AvcG9zdHMvJHtwb3N0LmlkfWB9IC8+XG4gKlxuICogTGlua3MgbWF5IHBhc3MgYWxvbmcgbG9jYXRpb24gc3RhdGUgYW5kL29yIHF1ZXJ5IHN0cmluZyBwYXJhbWV0ZXJzXG4gKiBpbiB0aGUgc3RhdGUvcXVlcnkgcHJvcHMsIHJlc3BlY3RpdmVseS5cbiAqXG4gKiAgIDxMaW5rIC4uLiBxdWVyeT17eyBzaG93OiB0cnVlIH19IHN0YXRlPXt7IHRoZTogJ3N0YXRlJyB9fSAvPlxuICovXG52YXIgTGluayA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnTGluaycsXG5cblxuICBjb250ZXh0VHlwZXM6IHtcbiAgICByb3V0ZXI6IF9Qcm9wVHlwZXMucm91dGVyU2hhcGVcbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICB0bzogb25lT2ZUeXBlKFtzdHJpbmcsIG9iamVjdF0pLFxuICAgIHF1ZXJ5OiBvYmplY3QsXG4gICAgaGFzaDogc3RyaW5nLFxuICAgIHN0YXRlOiBvYmplY3QsXG4gICAgYWN0aXZlU3R5bGU6IG9iamVjdCxcbiAgICBhY3RpdmVDbGFzc05hbWU6IHN0cmluZyxcbiAgICBvbmx5QWN0aXZlT25JbmRleDogYm9vbC5pc1JlcXVpcmVkLFxuICAgIG9uQ2xpY2s6IGZ1bmMsXG4gICAgdGFyZ2V0OiBzdHJpbmdcbiAgfSxcblxuICBnZXREZWZhdWx0UHJvcHM6IGZ1bmN0aW9uIGdldERlZmF1bHRQcm9wcygpIHtcbiAgICByZXR1cm4ge1xuICAgICAgb25seUFjdGl2ZU9uSW5kZXg6IGZhbHNlLFxuICAgICAgc3R5bGU6IHt9XG4gICAgfTtcbiAgfSxcbiAgaGFuZGxlQ2xpY2s6IGZ1bmN0aW9uIGhhbmRsZUNsaWNrKGV2ZW50KSB7XG4gICAgaWYgKHRoaXMucHJvcHMub25DbGljaykgdGhpcy5wcm9wcy5vbkNsaWNrKGV2ZW50KTtcblxuICAgIGlmIChldmVudC5kZWZhdWx0UHJldmVudGVkKSByZXR1cm47XG5cbiAgICAhdGhpcy5jb250ZXh0LnJvdXRlciA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICc8TGluaz5zIHJlbmRlcmVkIG91dHNpZGUgb2YgYSByb3V0ZXIgY29udGV4dCBjYW5ub3QgbmF2aWdhdGUuJykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuXG4gICAgaWYgKGlzTW9kaWZpZWRFdmVudChldmVudCkgfHwgIWlzTGVmdENsaWNrRXZlbnQoZXZlbnQpKSByZXR1cm47XG5cbiAgICAvLyBJZiB0YXJnZXQgcHJvcCBpcyBzZXQgKGUuZy4gdG8gXCJfYmxhbmtcIiksIGxldCBicm93c2VyIGhhbmRsZSBsaW5rLlxuICAgIC8qIGlzdGFuYnVsIGlnbm9yZSBpZjogdW50ZXN0YWJsZSB3aXRoIEthcm1hICovXG4gICAgaWYgKHRoaXMucHJvcHMudGFyZ2V0KSByZXR1cm47XG5cbiAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xuXG4gICAgdmFyIF9wcm9wcyA9IHRoaXMucHJvcHM7XG4gICAgdmFyIHRvID0gX3Byb3BzLnRvO1xuICAgIHZhciBxdWVyeSA9IF9wcm9wcy5xdWVyeTtcbiAgICB2YXIgaGFzaCA9IF9wcm9wcy5oYXNoO1xuICAgIHZhciBzdGF0ZSA9IF9wcm9wcy5zdGF0ZTtcblxuICAgIHZhciBsb2NhdGlvbiA9IGNyZWF0ZUxvY2F0aW9uRGVzY3JpcHRvcih0bywgeyBxdWVyeTogcXVlcnksIGhhc2g6IGhhc2gsIHN0YXRlOiBzdGF0ZSB9KTtcblxuICAgIHRoaXMuY29udGV4dC5yb3V0ZXIucHVzaChsb2NhdGlvbik7XG4gIH0sXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHZhciBfcHJvcHMyID0gdGhpcy5wcm9wcztcbiAgICB2YXIgdG8gPSBfcHJvcHMyLnRvO1xuICAgIHZhciBxdWVyeSA9IF9wcm9wczIucXVlcnk7XG4gICAgdmFyIGhhc2ggPSBfcHJvcHMyLmhhc2g7XG4gICAgdmFyIHN0YXRlID0gX3Byb3BzMi5zdGF0ZTtcbiAgICB2YXIgYWN0aXZlQ2xhc3NOYW1lID0gX3Byb3BzMi5hY3RpdmVDbGFzc05hbWU7XG4gICAgdmFyIGFjdGl2ZVN0eWxlID0gX3Byb3BzMi5hY3RpdmVTdHlsZTtcbiAgICB2YXIgb25seUFjdGl2ZU9uSW5kZXggPSBfcHJvcHMyLm9ubHlBY3RpdmVPbkluZGV4O1xuXG4gICAgdmFyIHByb3BzID0gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKF9wcm9wczIsIFsndG8nLCAncXVlcnknLCAnaGFzaCcsICdzdGF0ZScsICdhY3RpdmVDbGFzc05hbWUnLCAnYWN0aXZlU3R5bGUnLCAnb25seUFjdGl2ZU9uSW5kZXgnXSk7XG5cbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KSghKHF1ZXJ5IHx8IGhhc2ggfHwgc3RhdGUpLCAndGhlIGBxdWVyeWAsIGBoYXNoYCwgYW5kIGBzdGF0ZWAgcHJvcHMgb24gYDxMaW5rPmAgYXJlIGRlcHJlY2F0ZWQsIHVzZSBgPExpbmsgdG89e3sgcGF0aG5hbWUsIHF1ZXJ5LCBoYXNoLCBzdGF0ZSB9fS8+LiBodHRwOi8vdGlueS5jYy9yb3V0ZXItaXNBY3RpdmVkZXByZWNhdGVkJykgOiB2b2lkIDA7XG5cbiAgICAvLyBJZ25vcmUgaWYgcmVuZGVyZWQgb3V0c2lkZSB0aGUgY29udGV4dCBvZiByb3V0ZXIsIHNpbXBsaWZpZXMgdW5pdCB0ZXN0aW5nLlxuICAgIHZhciByb3V0ZXIgPSB0aGlzLmNvbnRleHQucm91dGVyO1xuXG5cbiAgICBpZiAocm91dGVyKSB7XG4gICAgICAvLyBJZiB1c2VyIGRvZXMgbm90IHNwZWNpZnkgYSBgdG9gIHByb3AsIHJldHVybiBhbiBlbXB0eSBhbmNob3IgdGFnLlxuICAgICAgaWYgKHRvID09IG51bGwpIHtcbiAgICAgICAgcmV0dXJuIF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50KCdhJywgcHJvcHMpO1xuICAgICAgfVxuXG4gICAgICB2YXIgbG9jYXRpb24gPSBjcmVhdGVMb2NhdGlvbkRlc2NyaXB0b3IodG8sIHsgcXVlcnk6IHF1ZXJ5LCBoYXNoOiBoYXNoLCBzdGF0ZTogc3RhdGUgfSk7XG4gICAgICBwcm9wcy5ocmVmID0gcm91dGVyLmNyZWF0ZUhyZWYobG9jYXRpb24pO1xuXG4gICAgICBpZiAoYWN0aXZlQ2xhc3NOYW1lIHx8IGFjdGl2ZVN0eWxlICE9IG51bGwgJiYgIWlzRW1wdHlPYmplY3QoYWN0aXZlU3R5bGUpKSB7XG4gICAgICAgIGlmIChyb3V0ZXIuaXNBY3RpdmUobG9jYXRpb24sIG9ubHlBY3RpdmVPbkluZGV4KSkge1xuICAgICAgICAgIGlmIChhY3RpdmVDbGFzc05hbWUpIHtcbiAgICAgICAgICAgIGlmIChwcm9wcy5jbGFzc05hbWUpIHtcbiAgICAgICAgICAgICAgcHJvcHMuY2xhc3NOYW1lICs9ICcgJyArIGFjdGl2ZUNsYXNzTmFtZTtcbiAgICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICAgIHByb3BzLmNsYXNzTmFtZSA9IGFjdGl2ZUNsYXNzTmFtZTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9XG5cbiAgICAgICAgICBpZiAoYWN0aXZlU3R5bGUpIHByb3BzLnN0eWxlID0gX2V4dGVuZHMoe30sIHByb3BzLnN0eWxlLCBhY3RpdmVTdHlsZSk7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9XG5cbiAgICByZXR1cm4gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUVsZW1lbnQoJ2EnLCBfZXh0ZW5kcyh7fSwgcHJvcHMsIHsgb25DbGljazogdGhpcy5oYW5kbGVDbGljayB9KSk7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBMaW5rO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5jb21waWxlUGF0dGVybiA9IGNvbXBpbGVQYXR0ZXJuO1xuZXhwb3J0cy5tYXRjaFBhdHRlcm4gPSBtYXRjaFBhdHRlcm47XG5leHBvcnRzLmdldFBhcmFtTmFtZXMgPSBnZXRQYXJhbU5hbWVzO1xuZXhwb3J0cy5nZXRQYXJhbXMgPSBnZXRQYXJhbXM7XG5leHBvcnRzLmZvcm1hdFBhdHRlcm4gPSBmb3JtYXRQYXR0ZXJuO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBlc2NhcGVSZWdFeHAoc3RyaW5nKSB7XG4gIHJldHVybiBzdHJpbmcucmVwbGFjZSgvWy4qKz9eJHt9KCl8W1xcXVxcXFxdL2csICdcXFxcJCYnKTtcbn1cblxuZnVuY3Rpb24gX2NvbXBpbGVQYXR0ZXJuKHBhdHRlcm4pIHtcbiAgdmFyIHJlZ2V4cFNvdXJjZSA9ICcnO1xuICB2YXIgcGFyYW1OYW1lcyA9IFtdO1xuICB2YXIgdG9rZW5zID0gW107XG5cbiAgdmFyIG1hdGNoID0gdm9pZCAwLFxuICAgICAgbGFzdEluZGV4ID0gMCxcbiAgICAgIG1hdGNoZXIgPSAvOihbYS16QS1aXyRdW2EtekEtWjAtOV8kXSopfFxcKlxcKnxcXCp8XFwofFxcKS9nO1xuICB3aGlsZSAobWF0Y2ggPSBtYXRjaGVyLmV4ZWMocGF0dGVybikpIHtcbiAgICBpZiAobWF0Y2guaW5kZXggIT09IGxhc3RJbmRleCkge1xuICAgICAgdG9rZW5zLnB1c2gocGF0dGVybi5zbGljZShsYXN0SW5kZXgsIG1hdGNoLmluZGV4KSk7XG4gICAgICByZWdleHBTb3VyY2UgKz0gZXNjYXBlUmVnRXhwKHBhdHRlcm4uc2xpY2UobGFzdEluZGV4LCBtYXRjaC5pbmRleCkpO1xuICAgIH1cblxuICAgIGlmIChtYXRjaFsxXSkge1xuICAgICAgcmVnZXhwU291cmNlICs9ICcoW14vXSspJztcbiAgICAgIHBhcmFtTmFtZXMucHVzaChtYXRjaFsxXSk7XG4gICAgfSBlbHNlIGlmIChtYXRjaFswXSA9PT0gJyoqJykge1xuICAgICAgcmVnZXhwU291cmNlICs9ICcoLiopJztcbiAgICAgIHBhcmFtTmFtZXMucHVzaCgnc3BsYXQnKTtcbiAgICB9IGVsc2UgaWYgKG1hdGNoWzBdID09PSAnKicpIHtcbiAgICAgIHJlZ2V4cFNvdXJjZSArPSAnKC4qPyknO1xuICAgICAgcGFyYW1OYW1lcy5wdXNoKCdzcGxhdCcpO1xuICAgIH0gZWxzZSBpZiAobWF0Y2hbMF0gPT09ICcoJykge1xuICAgICAgcmVnZXhwU291cmNlICs9ICcoPzonO1xuICAgIH0gZWxzZSBpZiAobWF0Y2hbMF0gPT09ICcpJykge1xuICAgICAgcmVnZXhwU291cmNlICs9ICcpPyc7XG4gICAgfVxuXG4gICAgdG9rZW5zLnB1c2gobWF0Y2hbMF0pO1xuXG4gICAgbGFzdEluZGV4ID0gbWF0Y2hlci5sYXN0SW5kZXg7XG4gIH1cblxuICBpZiAobGFzdEluZGV4ICE9PSBwYXR0ZXJuLmxlbmd0aCkge1xuICAgIHRva2Vucy5wdXNoKHBhdHRlcm4uc2xpY2UobGFzdEluZGV4LCBwYXR0ZXJuLmxlbmd0aCkpO1xuICAgIHJlZ2V4cFNvdXJjZSArPSBlc2NhcGVSZWdFeHAocGF0dGVybi5zbGljZShsYXN0SW5kZXgsIHBhdHRlcm4ubGVuZ3RoKSk7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIHBhdHRlcm46IHBhdHRlcm4sXG4gICAgcmVnZXhwU291cmNlOiByZWdleHBTb3VyY2UsXG4gICAgcGFyYW1OYW1lczogcGFyYW1OYW1lcyxcbiAgICB0b2tlbnM6IHRva2Vuc1xuICB9O1xufVxuXG52YXIgQ29tcGlsZWRQYXR0ZXJuc0NhY2hlID0gT2JqZWN0LmNyZWF0ZShudWxsKTtcblxuZnVuY3Rpb24gY29tcGlsZVBhdHRlcm4ocGF0dGVybikge1xuICBpZiAoIUNvbXBpbGVkUGF0dGVybnNDYWNoZVtwYXR0ZXJuXSkgQ29tcGlsZWRQYXR0ZXJuc0NhY2hlW3BhdHRlcm5dID0gX2NvbXBpbGVQYXR0ZXJuKHBhdHRlcm4pO1xuXG4gIHJldHVybiBDb21waWxlZFBhdHRlcm5zQ2FjaGVbcGF0dGVybl07XG59XG5cbi8qKlxuICogQXR0ZW1wdHMgdG8gbWF0Y2ggYSBwYXR0ZXJuIG9uIHRoZSBnaXZlbiBwYXRobmFtZS4gUGF0dGVybnMgbWF5IHVzZVxuICogdGhlIGZvbGxvd2luZyBzcGVjaWFsIGNoYXJhY3RlcnM6XG4gKlxuICogLSA6cGFyYW1OYW1lICAgICBNYXRjaGVzIGEgVVJMIHNlZ21lbnQgdXAgdG8gdGhlIG5leHQgLywgPywgb3IgIy4gVGhlXG4gKiAgICAgICAgICAgICAgICAgIGNhcHR1cmVkIHN0cmluZyBpcyBjb25zaWRlcmVkIGEgXCJwYXJhbVwiXG4gKiAtICgpICAgICAgICAgICAgIFdyYXBzIGEgc2VnbWVudCBvZiB0aGUgVVJMIHRoYXQgaXMgb3B0aW9uYWxcbiAqIC0gKiAgICAgICAgICAgICAgQ29uc3VtZXMgKG5vbi1ncmVlZHkpIGFsbCBjaGFyYWN0ZXJzIHVwIHRvIHRoZSBuZXh0XG4gKiAgICAgICAgICAgICAgICAgIGNoYXJhY3RlciBpbiB0aGUgcGF0dGVybiwgb3IgdG8gdGhlIGVuZCBvZiB0aGUgVVJMIGlmXG4gKiAgICAgICAgICAgICAgICAgIHRoZXJlIGlzIG5vbmVcbiAqIC0gKiogICAgICAgICAgICAgQ29uc3VtZXMgKGdyZWVkeSkgYWxsIGNoYXJhY3RlcnMgdXAgdG8gdGhlIG5leHQgY2hhcmFjdGVyXG4gKiAgICAgICAgICAgICAgICAgIGluIHRoZSBwYXR0ZXJuLCBvciB0byB0aGUgZW5kIG9mIHRoZSBVUkwgaWYgdGhlcmUgaXMgbm9uZVxuICpcbiAqICBUaGUgZnVuY3Rpb24gY2FsbHMgY2FsbGJhY2soZXJyb3IsIG1hdGNoZWQpIHdoZW4gZmluaXNoZWQuXG4gKiBUaGUgcmV0dXJuIHZhbHVlIGlzIGFuIG9iamVjdCB3aXRoIHRoZSBmb2xsb3dpbmcgcHJvcGVydGllczpcbiAqXG4gKiAtIHJlbWFpbmluZ1BhdGhuYW1lXG4gKiAtIHBhcmFtTmFtZXNcbiAqIC0gcGFyYW1WYWx1ZXNcbiAqL1xuZnVuY3Rpb24gbWF0Y2hQYXR0ZXJuKHBhdHRlcm4sIHBhdGhuYW1lKSB7XG4gIC8vIEVuc3VyZSBwYXR0ZXJuIHN0YXJ0cyB3aXRoIGxlYWRpbmcgc2xhc2ggZm9yIGNvbnNpc3RlbmN5IHdpdGggcGF0aG5hbWUuXG4gIGlmIChwYXR0ZXJuLmNoYXJBdCgwKSAhPT0gJy8nKSB7XG4gICAgcGF0dGVybiA9ICcvJyArIHBhdHRlcm47XG4gIH1cblxuICB2YXIgX2NvbXBpbGVQYXR0ZXJuMiA9IGNvbXBpbGVQYXR0ZXJuKHBhdHRlcm4pO1xuXG4gIHZhciByZWdleHBTb3VyY2UgPSBfY29tcGlsZVBhdHRlcm4yLnJlZ2V4cFNvdXJjZTtcbiAgdmFyIHBhcmFtTmFtZXMgPSBfY29tcGlsZVBhdHRlcm4yLnBhcmFtTmFtZXM7XG4gIHZhciB0b2tlbnMgPSBfY29tcGlsZVBhdHRlcm4yLnRva2VucztcblxuXG4gIGlmIChwYXR0ZXJuLmNoYXJBdChwYXR0ZXJuLmxlbmd0aCAtIDEpICE9PSAnLycpIHtcbiAgICByZWdleHBTb3VyY2UgKz0gJy8/JzsgLy8gQWxsb3cgb3B0aW9uYWwgcGF0aCBzZXBhcmF0b3IgYXQgZW5kLlxuICB9XG5cbiAgLy8gU3BlY2lhbC1jYXNlIHBhdHRlcm5zIGxpa2UgJyonIGZvciBjYXRjaC1hbGwgcm91dGVzLlxuICBpZiAodG9rZW5zW3Rva2Vucy5sZW5ndGggLSAxXSA9PT0gJyonKSB7XG4gICAgcmVnZXhwU291cmNlICs9ICckJztcbiAgfVxuXG4gIHZhciBtYXRjaCA9IHBhdGhuYW1lLm1hdGNoKG5ldyBSZWdFeHAoJ14nICsgcmVnZXhwU291cmNlLCAnaScpKTtcbiAgaWYgKG1hdGNoID09IG51bGwpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuXG4gIHZhciBtYXRjaGVkUGF0aCA9IG1hdGNoWzBdO1xuICB2YXIgcmVtYWluaW5nUGF0aG5hbWUgPSBwYXRobmFtZS5zdWJzdHIobWF0Y2hlZFBhdGgubGVuZ3RoKTtcblxuICBpZiAocmVtYWluaW5nUGF0aG5hbWUpIHtcbiAgICAvLyBSZXF1aXJlIHRoYXQgdGhlIG1hdGNoIGVuZHMgYXQgYSBwYXRoIHNlcGFyYXRvciwgaWYgd2UgZGlkbid0IG1hdGNoXG4gICAgLy8gdGhlIGZ1bGwgcGF0aCwgc28gYW55IHJlbWFpbmluZyBwYXRobmFtZSBpcyBhIG5ldyBwYXRoIHNlZ21lbnQuXG4gICAgaWYgKG1hdGNoZWRQYXRoLmNoYXJBdChtYXRjaGVkUGF0aC5sZW5ndGggLSAxKSAhPT0gJy8nKSB7XG4gICAgICByZXR1cm4gbnVsbDtcbiAgICB9XG5cbiAgICAvLyBJZiB0aGVyZSBpcyBhIHJlbWFpbmluZyBwYXRobmFtZSwgdHJlYXQgdGhlIHBhdGggc2VwYXJhdG9yIGFzIHBhcnQgb2ZcbiAgICAvLyB0aGUgcmVtYWluaW5nIHBhdGhuYW1lIGZvciBwcm9wZXJseSBjb250aW51aW5nIHRoZSBtYXRjaC5cbiAgICByZW1haW5pbmdQYXRobmFtZSA9ICcvJyArIHJlbWFpbmluZ1BhdGhuYW1lO1xuICB9XG5cbiAgcmV0dXJuIHtcbiAgICByZW1haW5pbmdQYXRobmFtZTogcmVtYWluaW5nUGF0aG5hbWUsXG4gICAgcGFyYW1OYW1lczogcGFyYW1OYW1lcyxcbiAgICBwYXJhbVZhbHVlczogbWF0Y2guc2xpY2UoMSkubWFwKGZ1bmN0aW9uICh2KSB7XG4gICAgICByZXR1cm4gdiAmJiBkZWNvZGVVUklDb21wb25lbnQodik7XG4gICAgfSlcbiAgfTtcbn1cblxuZnVuY3Rpb24gZ2V0UGFyYW1OYW1lcyhwYXR0ZXJuKSB7XG4gIHJldHVybiBjb21waWxlUGF0dGVybihwYXR0ZXJuKS5wYXJhbU5hbWVzO1xufVxuXG5mdW5jdGlvbiBnZXRQYXJhbXMocGF0dGVybiwgcGF0aG5hbWUpIHtcbiAgdmFyIG1hdGNoID0gbWF0Y2hQYXR0ZXJuKHBhdHRlcm4sIHBhdGhuYW1lKTtcbiAgaWYgKCFtYXRjaCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG5cbiAgdmFyIHBhcmFtTmFtZXMgPSBtYXRjaC5wYXJhbU5hbWVzO1xuICB2YXIgcGFyYW1WYWx1ZXMgPSBtYXRjaC5wYXJhbVZhbHVlcztcblxuICB2YXIgcGFyYW1zID0ge307XG5cbiAgcGFyYW1OYW1lcy5mb3JFYWNoKGZ1bmN0aW9uIChwYXJhbU5hbWUsIGluZGV4KSB7XG4gICAgcGFyYW1zW3BhcmFtTmFtZV0gPSBwYXJhbVZhbHVlc1tpbmRleF07XG4gIH0pO1xuXG4gIHJldHVybiBwYXJhbXM7XG59XG5cbi8qKlxuICogUmV0dXJucyBhIHZlcnNpb24gb2YgdGhlIGdpdmVuIHBhdHRlcm4gd2l0aCBwYXJhbXMgaW50ZXJwb2xhdGVkLiBUaHJvd3NcbiAqIGlmIHRoZXJlIGlzIGEgZHluYW1pYyBzZWdtZW50IG9mIHRoZSBwYXR0ZXJuIGZvciB3aGljaCB0aGVyZSBpcyBubyBwYXJhbS5cbiAqL1xuZnVuY3Rpb24gZm9ybWF0UGF0dGVybihwYXR0ZXJuLCBwYXJhbXMpIHtcbiAgcGFyYW1zID0gcGFyYW1zIHx8IHt9O1xuXG4gIHZhciBfY29tcGlsZVBhdHRlcm4zID0gY29tcGlsZVBhdHRlcm4ocGF0dGVybik7XG5cbiAgdmFyIHRva2VucyA9IF9jb21waWxlUGF0dGVybjMudG9rZW5zO1xuXG4gIHZhciBwYXJlbkNvdW50ID0gMCxcbiAgICAgIHBhdGhuYW1lID0gJycsXG4gICAgICBzcGxhdEluZGV4ID0gMDtcblxuICB2YXIgdG9rZW4gPSB2b2lkIDAsXG4gICAgICBwYXJhbU5hbWUgPSB2b2lkIDAsXG4gICAgICBwYXJhbVZhbHVlID0gdm9pZCAwO1xuICBmb3IgKHZhciBpID0gMCwgbGVuID0gdG9rZW5zLmxlbmd0aDsgaSA8IGxlbjsgKytpKSB7XG4gICAgdG9rZW4gPSB0b2tlbnNbaV07XG5cbiAgICBpZiAodG9rZW4gPT09ICcqJyB8fCB0b2tlbiA9PT0gJyoqJykge1xuICAgICAgcGFyYW1WYWx1ZSA9IEFycmF5LmlzQXJyYXkocGFyYW1zLnNwbGF0KSA/IHBhcmFtcy5zcGxhdFtzcGxhdEluZGV4KytdIDogcGFyYW1zLnNwbGF0O1xuXG4gICAgICAhKHBhcmFtVmFsdWUgIT0gbnVsbCB8fCBwYXJlbkNvdW50ID4gMCkgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnTWlzc2luZyBzcGxhdCAjJXMgZm9yIHBhdGggXCIlc1wiJywgc3BsYXRJbmRleCwgcGF0dGVybikgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuXG4gICAgICBpZiAocGFyYW1WYWx1ZSAhPSBudWxsKSBwYXRobmFtZSArPSBlbmNvZGVVUkkocGFyYW1WYWx1ZSk7XG4gICAgfSBlbHNlIGlmICh0b2tlbiA9PT0gJygnKSB7XG4gICAgICBwYXJlbkNvdW50ICs9IDE7XG4gICAgfSBlbHNlIGlmICh0b2tlbiA9PT0gJyknKSB7XG4gICAgICBwYXJlbkNvdW50IC09IDE7XG4gICAgfSBlbHNlIGlmICh0b2tlbi5jaGFyQXQoMCkgPT09ICc6Jykge1xuICAgICAgcGFyYW1OYW1lID0gdG9rZW4uc3Vic3RyaW5nKDEpO1xuICAgICAgcGFyYW1WYWx1ZSA9IHBhcmFtc1twYXJhbU5hbWVdO1xuXG4gICAgICAhKHBhcmFtVmFsdWUgIT0gbnVsbCB8fCBwYXJlbkNvdW50ID4gMCkgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnTWlzc2luZyBcIiVzXCIgcGFyYW1ldGVyIGZvciBwYXRoIFwiJXNcIicsIHBhcmFtTmFtZSwgcGF0dGVybikgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuXG4gICAgICBpZiAocGFyYW1WYWx1ZSAhPSBudWxsKSBwYXRobmFtZSArPSBlbmNvZGVVUklDb21wb25lbnQocGFyYW1WYWx1ZSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHBhdGhuYW1lICs9IHRva2VuO1xuICAgIH1cbiAgfVxuXG4gIHJldHVybiBwYXRobmFtZS5yZXBsYWNlKC9cXC8rL2csICcvJyk7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5yb3V0ZXIgPSBleHBvcnRzLnJvdXRlcyA9IGV4cG9ydHMucm91dGUgPSBleHBvcnRzLmNvbXBvbmVudHMgPSBleHBvcnRzLmNvbXBvbmVudCA9IGV4cG9ydHMubG9jYXRpb24gPSBleHBvcnRzLmhpc3RvcnkgPSBleHBvcnRzLmZhbHN5ID0gZXhwb3J0cy5sb2NhdGlvblNoYXBlID0gZXhwb3J0cy5yb3V0ZXJTaGFwZSA9IHVuZGVmaW5lZDtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcycpO1xuXG52YXIgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyk7XG5cbnZhciBfSW50ZXJuYWxQcm9wVHlwZXMgPSByZXF1aXJlKCcuL0ludGVybmFsUHJvcFR5cGVzJyk7XG5cbnZhciBJbnRlcm5hbFByb3BUeXBlcyA9IF9pbnRlcm9wUmVxdWlyZVdpbGRjYXJkKF9JbnRlcm5hbFByb3BUeXBlcyk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZVdpbGRjYXJkKG9iaikgeyBpZiAob2JqICYmIG9iai5fX2VzTW9kdWxlKSB7IHJldHVybiBvYmo7IH0gZWxzZSB7IHZhciBuZXdPYmogPSB7fTsgaWYgKG9iaiAhPSBudWxsKSB7IGZvciAodmFyIGtleSBpbiBvYmopIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmosIGtleSkpIG5ld09ialtrZXldID0gb2JqW2tleV07IH0gfSBuZXdPYmouZGVmYXVsdCA9IG9iajsgcmV0dXJuIG5ld09iajsgfSB9XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBmdW5jID0gX3JlYWN0LlByb3BUeXBlcy5mdW5jO1xudmFyIG9iamVjdCA9IF9yZWFjdC5Qcm9wVHlwZXMub2JqZWN0O1xudmFyIHNoYXBlID0gX3JlYWN0LlByb3BUeXBlcy5zaGFwZTtcbnZhciBzdHJpbmcgPSBfcmVhY3QuUHJvcFR5cGVzLnN0cmluZztcbnZhciByb3V0ZXJTaGFwZSA9IGV4cG9ydHMucm91dGVyU2hhcGUgPSBzaGFwZSh7XG4gIHB1c2g6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgcmVwbGFjZTogZnVuYy5pc1JlcXVpcmVkLFxuICBnbzogZnVuYy5pc1JlcXVpcmVkLFxuICBnb0JhY2s6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ29Gb3J3YXJkOiBmdW5jLmlzUmVxdWlyZWQsXG4gIHNldFJvdXRlTGVhdmVIb29rOiBmdW5jLmlzUmVxdWlyZWQsXG4gIGlzQWN0aXZlOiBmdW5jLmlzUmVxdWlyZWRcbn0pO1xuXG52YXIgbG9jYXRpb25TaGFwZSA9IGV4cG9ydHMubG9jYXRpb25TaGFwZSA9IHNoYXBlKHtcbiAgcGF0aG5hbWU6IHN0cmluZy5pc1JlcXVpcmVkLFxuICBzZWFyY2g6IHN0cmluZy5pc1JlcXVpcmVkLFxuICBzdGF0ZTogb2JqZWN0LFxuICBhY3Rpb246IHN0cmluZy5pc1JlcXVpcmVkLFxuICBrZXk6IHN0cmluZ1xufSk7XG5cbi8vIERlcHJlY2F0ZWQgc3R1ZmYgYmVsb3c6XG5cbnZhciBmYWxzeSA9IGV4cG9ydHMuZmFsc3kgPSBJbnRlcm5hbFByb3BUeXBlcy5mYWxzeTtcbnZhciBoaXN0b3J5ID0gZXhwb3J0cy5oaXN0b3J5ID0gSW50ZXJuYWxQcm9wVHlwZXMuaGlzdG9yeTtcbnZhciBsb2NhdGlvbiA9IGV4cG9ydHMubG9jYXRpb24gPSBsb2NhdGlvblNoYXBlO1xudmFyIGNvbXBvbmVudCA9IGV4cG9ydHMuY29tcG9uZW50ID0gSW50ZXJuYWxQcm9wVHlwZXMuY29tcG9uZW50O1xudmFyIGNvbXBvbmVudHMgPSBleHBvcnRzLmNvbXBvbmVudHMgPSBJbnRlcm5hbFByb3BUeXBlcy5jb21wb25lbnRzO1xudmFyIHJvdXRlID0gZXhwb3J0cy5yb3V0ZSA9IEludGVybmFsUHJvcFR5cGVzLnJvdXRlO1xudmFyIHJvdXRlcyA9IGV4cG9ydHMucm91dGVzID0gSW50ZXJuYWxQcm9wVHlwZXMucm91dGVzO1xudmFyIHJvdXRlciA9IGV4cG9ydHMucm91dGVyID0gcm91dGVyU2hhcGU7XG5cbmlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gIChmdW5jdGlvbiAoKSB7XG4gICAgdmFyIGRlcHJlY2F0ZVByb3BUeXBlID0gZnVuY3Rpb24gZGVwcmVjYXRlUHJvcFR5cGUocHJvcFR5cGUsIG1lc3NhZ2UpIHtcbiAgICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCBtZXNzYWdlKSA6IHZvaWQgMDtcbiAgICAgICAgcmV0dXJuIHByb3BUeXBlLmFwcGx5KHVuZGVmaW5lZCwgYXJndW1lbnRzKTtcbiAgICAgIH07XG4gICAgfTtcblxuICAgIHZhciBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlID0gZnVuY3Rpb24gZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZShwcm9wVHlwZSkge1xuICAgICAgcmV0dXJuIGRlcHJlY2F0ZVByb3BUeXBlKHByb3BUeXBlLCAnVGhpcyBwcm9wIHR5cGUgaXMgbm90IGludGVuZGVkIGZvciBleHRlcm5hbCB1c2UsIGFuZCB3YXMgcHJldmlvdXNseSBleHBvcnRlZCBieSBtaXN0YWtlLiBUaGVzZSBpbnRlcm5hbCBwcm9wIHR5cGVzIGFyZSBkZXByZWNhdGVkIGZvciBleHRlcm5hbCB1c2UsIGFuZCB3aWxsIGJlIHJlbW92ZWQgaW4gYSBsYXRlciB2ZXJzaW9uLicpO1xuICAgIH07XG5cbiAgICB2YXIgZGVwcmVjYXRlUmVuYW1lZFByb3BUeXBlID0gZnVuY3Rpb24gZGVwcmVjYXRlUmVuYW1lZFByb3BUeXBlKHByb3BUeXBlLCBuYW1lKSB7XG4gICAgICByZXR1cm4gZGVwcmVjYXRlUHJvcFR5cGUocHJvcFR5cGUsICdUaGUgYCcgKyBuYW1lICsgJ2AgcHJvcCB0eXBlIGlzIG5vdyBleHBvcnRlZCBhcyBgJyArIG5hbWUgKyAnU2hhcGVgIHRvIGF2b2lkIG5hbWUgY29uZmxpY3RzLiBUaGlzIGV4cG9ydCBpcyBkZXByZWNhdGVkIGFuZCB3aWxsIGJlIHJlbW92ZWQgaW4gYSBsYXRlciB2ZXJzaW9uLicpO1xuICAgIH07XG5cbiAgICBleHBvcnRzLmZhbHN5ID0gZmFsc3kgPSBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlKGZhbHN5KTtcbiAgICBleHBvcnRzLmhpc3RvcnkgPSBoaXN0b3J5ID0gZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZShoaXN0b3J5KTtcbiAgICBleHBvcnRzLmNvbXBvbmVudCA9IGNvbXBvbmVudCA9IGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUoY29tcG9uZW50KTtcbiAgICBleHBvcnRzLmNvbXBvbmVudHMgPSBjb21wb25lbnRzID0gZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZShjb21wb25lbnRzKTtcbiAgICBleHBvcnRzLnJvdXRlID0gcm91dGUgPSBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlKHJvdXRlKTtcbiAgICBleHBvcnRzLnJvdXRlcyA9IHJvdXRlcyA9IGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUocm91dGVzKTtcblxuICAgIGV4cG9ydHMubG9jYXRpb24gPSBsb2NhdGlvbiA9IGRlcHJlY2F0ZVJlbmFtZWRQcm9wVHlwZShsb2NhdGlvbiwgJ2xvY2F0aW9uJyk7XG4gICAgZXhwb3J0cy5yb3V0ZXIgPSByb3V0ZXIgPSBkZXByZWNhdGVSZW5hbWVkUHJvcFR5cGUocm91dGVyLCAncm91dGVyJyk7XG4gIH0pKCk7XG59XG5cbnZhciBkZWZhdWx0RXhwb3J0ID0ge1xuICBmYWxzeTogZmFsc3ksXG4gIGhpc3Rvcnk6IGhpc3RvcnksXG4gIGxvY2F0aW9uOiBsb2NhdGlvbixcbiAgY29tcG9uZW50OiBjb21wb25lbnQsXG4gIGNvbXBvbmVudHM6IGNvbXBvbmVudHMsXG4gIHJvdXRlOiByb3V0ZSxcbiAgLy8gRm9yIHNvbWUgcmVhc29uLCByb3V0ZXMgd2FzIG5ldmVyIGhlcmUuXG4gIHJvdXRlcjogcm91dGVyXG59O1xuXG5pZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICBkZWZhdWx0RXhwb3J0ID0gKDAsIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzMi5kZWZhdWx0KShkZWZhdWx0RXhwb3J0LCAnVGhlIGRlZmF1bHQgZXhwb3J0IGZyb20gYHJlYWN0LXJvdXRlci9saWIvUHJvcFR5cGVzYCBpcyBkZXByZWNhdGVkLiBQbGVhc2UgdXNlIHRoZSBuYW1lZCBleHBvcnRzIGluc3RlYWQuJyk7XG59XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGRlZmF1bHRFeHBvcnQ7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfUm91dGVVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVVdGlscycpO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbnZhciBfSW50ZXJuYWxQcm9wVHlwZXMgPSByZXF1aXJlKCcuL0ludGVybmFsUHJvcFR5cGVzJyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBfUmVhY3QkUHJvcFR5cGVzID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcztcbnZhciBzdHJpbmcgPSBfUmVhY3QkUHJvcFR5cGVzLnN0cmluZztcbnZhciBvYmplY3QgPSBfUmVhY3QkUHJvcFR5cGVzLm9iamVjdDtcblxuLyoqXG4gKiBBIDxSZWRpcmVjdD4gaXMgdXNlZCB0byBkZWNsYXJlIGFub3RoZXIgVVJMIHBhdGggYSBjbGllbnQgc2hvdWxkXG4gKiBiZSBzZW50IHRvIHdoZW4gdGhleSByZXF1ZXN0IGEgZ2l2ZW4gVVJMLlxuICpcbiAqIFJlZGlyZWN0cyBhcmUgcGxhY2VkIGFsb25nc2lkZSByb3V0ZXMgaW4gdGhlIHJvdXRlIGNvbmZpZ3VyYXRpb25cbiAqIGFuZCBhcmUgdHJhdmVyc2VkIGluIHRoZSBzYW1lIG1hbm5lci5cbiAqL1xuXG52YXIgUmVkaXJlY3QgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ1JlZGlyZWN0JyxcblxuXG4gIHN0YXRpY3M6IHtcbiAgICBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQ6IGZ1bmN0aW9uIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50KSB7XG4gICAgICB2YXIgcm91dGUgPSAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KShlbGVtZW50KTtcblxuICAgICAgaWYgKHJvdXRlLmZyb20pIHJvdXRlLnBhdGggPSByb3V0ZS5mcm9tO1xuXG4gICAgICByb3V0ZS5vbkVudGVyID0gZnVuY3Rpb24gKG5leHRTdGF0ZSwgcmVwbGFjZSkge1xuICAgICAgICB2YXIgbG9jYXRpb24gPSBuZXh0U3RhdGUubG9jYXRpb247XG4gICAgICAgIHZhciBwYXJhbXMgPSBuZXh0U3RhdGUucGFyYW1zO1xuXG5cbiAgICAgICAgdmFyIHBhdGhuYW1lID0gdm9pZCAwO1xuICAgICAgICBpZiAocm91dGUudG8uY2hhckF0KDApID09PSAnLycpIHtcbiAgICAgICAgICBwYXRobmFtZSA9ICgwLCBfUGF0dGVyblV0aWxzLmZvcm1hdFBhdHRlcm4pKHJvdXRlLnRvLCBwYXJhbXMpO1xuICAgICAgICB9IGVsc2UgaWYgKCFyb3V0ZS50bykge1xuICAgICAgICAgIHBhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgdmFyIHJvdXRlSW5kZXggPSBuZXh0U3RhdGUucm91dGVzLmluZGV4T2Yocm91dGUpO1xuICAgICAgICAgIHZhciBwYXJlbnRQYXR0ZXJuID0gUmVkaXJlY3QuZ2V0Um91dGVQYXR0ZXJuKG5leHRTdGF0ZS5yb3V0ZXMsIHJvdXRlSW5kZXggLSAxKTtcbiAgICAgICAgICB2YXIgcGF0dGVybiA9IHBhcmVudFBhdHRlcm4ucmVwbGFjZSgvXFwvKiQvLCAnLycpICsgcm91dGUudG87XG4gICAgICAgICAgcGF0aG5hbWUgPSAoMCwgX1BhdHRlcm5VdGlscy5mb3JtYXRQYXR0ZXJuKShwYXR0ZXJuLCBwYXJhbXMpO1xuICAgICAgICB9XG5cbiAgICAgICAgcmVwbGFjZSh7XG4gICAgICAgICAgcGF0aG5hbWU6IHBhdGhuYW1lLFxuICAgICAgICAgIHF1ZXJ5OiByb3V0ZS5xdWVyeSB8fCBsb2NhdGlvbi5xdWVyeSxcbiAgICAgICAgICBzdGF0ZTogcm91dGUuc3RhdGUgfHwgbG9jYXRpb24uc3RhdGVcbiAgICAgICAgfSk7XG4gICAgICB9O1xuXG4gICAgICByZXR1cm4gcm91dGU7XG4gICAgfSxcbiAgICBnZXRSb3V0ZVBhdHRlcm46IGZ1bmN0aW9uIGdldFJvdXRlUGF0dGVybihyb3V0ZXMsIHJvdXRlSW5kZXgpIHtcbiAgICAgIHZhciBwYXJlbnRQYXR0ZXJuID0gJyc7XG5cbiAgICAgIGZvciAodmFyIGkgPSByb3V0ZUluZGV4OyBpID49IDA7IGktLSkge1xuICAgICAgICB2YXIgcm91dGUgPSByb3V0ZXNbaV07XG4gICAgICAgIHZhciBwYXR0ZXJuID0gcm91dGUucGF0aCB8fCAnJztcblxuICAgICAgICBwYXJlbnRQYXR0ZXJuID0gcGF0dGVybi5yZXBsYWNlKC9cXC8qJC8sICcvJykgKyBwYXJlbnRQYXR0ZXJuO1xuXG4gICAgICAgIGlmIChwYXR0ZXJuLmluZGV4T2YoJy8nKSA9PT0gMCkgYnJlYWs7XG4gICAgICB9XG5cbiAgICAgIHJldHVybiAnLycgKyBwYXJlbnRQYXR0ZXJuO1xuICAgIH1cbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICBwYXRoOiBzdHJpbmcsXG4gICAgZnJvbTogc3RyaW5nLCAvLyBBbGlhcyBmb3IgcGF0aFxuICAgIHRvOiBzdHJpbmcuaXNSZXF1aXJlZCxcbiAgICBxdWVyeTogb2JqZWN0LFxuICAgIHN0YXRlOiBvYmplY3QsXG4gICAgb25FbnRlcjogX0ludGVybmFsUHJvcFR5cGVzLmZhbHN5LFxuICAgIGNoaWxkcmVuOiBfSW50ZXJuYWxQcm9wVHlwZXMuZmFsc3lcbiAgfSxcblxuICAvKiBpc3RhbmJ1bCBpZ25vcmUgbmV4dDogc2FuaXR5IGNoZWNrICovXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgICFmYWxzZSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICc8UmVkaXJlY3Q+IGVsZW1lbnRzIGFyZSBmb3Igcm91dGVyIGNvbmZpZ3VyYXRpb24gb25seSBhbmQgc2hvdWxkIG5vdCBiZSByZW5kZXJlZCcpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IFJlZGlyZWN0O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfUm91dGVVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVVdGlscycpO1xuXG52YXIgX0ludGVybmFsUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9JbnRlcm5hbFByb3BUeXBlcycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgc3RyaW5nID0gX1JlYWN0JFByb3BUeXBlcy5zdHJpbmc7XG52YXIgZnVuYyA9IF9SZWFjdCRQcm9wVHlwZXMuZnVuYztcblxuLyoqXG4gKiBBIDxSb3V0ZT4gaXMgdXNlZCB0byBkZWNsYXJlIHdoaWNoIGNvbXBvbmVudHMgYXJlIHJlbmRlcmVkIHRvIHRoZVxuICogcGFnZSB3aGVuIHRoZSBVUkwgbWF0Y2hlcyBhIGdpdmVuIHBhdHRlcm4uXG4gKlxuICogUm91dGVzIGFyZSBhcnJhbmdlZCBpbiBhIG5lc3RlZCB0cmVlIHN0cnVjdHVyZS4gV2hlbiBhIG5ldyBVUkwgaXNcbiAqIHJlcXVlc3RlZCwgdGhlIHRyZWUgaXMgc2VhcmNoZWQgZGVwdGgtZmlyc3QgdG8gZmluZCBhIHJvdXRlIHdob3NlXG4gKiBwYXRoIG1hdGNoZXMgdGhlIFVSTC4gIFdoZW4gb25lIGlzIGZvdW5kLCBhbGwgcm91dGVzIGluIHRoZSB0cmVlXG4gKiB0aGF0IGxlYWQgdG8gaXQgYXJlIGNvbnNpZGVyZWQgXCJhY3RpdmVcIiBhbmQgdGhlaXIgY29tcG9uZW50cyBhcmVcbiAqIHJlbmRlcmVkIGludG8gdGhlIERPTSwgbmVzdGVkIGluIHRoZSBzYW1lIG9yZGVyIGFzIGluIHRoZSB0cmVlLlxuICovXG5cbnZhciBSb3V0ZSA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnUm91dGUnLFxuXG5cbiAgc3RhdGljczoge1xuICAgIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudDogX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50XG4gIH0sXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgcGF0aDogc3RyaW5nLFxuICAgIGNvbXBvbmVudDogX0ludGVybmFsUHJvcFR5cGVzLmNvbXBvbmVudCxcbiAgICBjb21wb25lbnRzOiBfSW50ZXJuYWxQcm9wVHlwZXMuY29tcG9uZW50cyxcbiAgICBnZXRDb21wb25lbnQ6IGZ1bmMsXG4gICAgZ2V0Q29tcG9uZW50czogZnVuY1xuICB9LFxuXG4gIC8qIGlzdGFuYnVsIGlnbm9yZSBuZXh0OiBzYW5pdHkgY2hlY2sgKi9cbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgIWZhbHNlID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJzxSb3V0ZT4gZWxlbWVudHMgYXJlIGZvciByb3V0ZXIgY29uZmlndXJhdGlvbiBvbmx5IGFuZCBzaG91bGQgbm90IGJlIHJlbmRlcmVkJykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gUm91dGU7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIG9iamVjdCA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXMub2JqZWN0O1xuXG4vKipcbiAqIFRoZSBSb3V0ZUNvbnRleHQgbWl4aW4gcHJvdmlkZXMgYSBjb252ZW5pZW50IHdheSBmb3Igcm91dGVcbiAqIGNvbXBvbmVudHMgdG8gc2V0IHRoZSByb3V0ZSBpbiBjb250ZXh0LiBUaGlzIGlzIG5lZWRlZCBmb3JcbiAqIHJvdXRlcyB0aGF0IHJlbmRlciBlbGVtZW50cyB0aGF0IHdhbnQgdG8gdXNlIHRoZSBMaWZlY3ljbGVcbiAqIG1peGluIHRvIHByZXZlbnQgdHJhbnNpdGlvbnMuXG4gKi9cblxudmFyIFJvdXRlQ29udGV4dCA9IHtcblxuICBwcm9wVHlwZXM6IHtcbiAgICByb3V0ZTogb2JqZWN0LmlzUmVxdWlyZWRcbiAgfSxcblxuICBjaGlsZENvbnRleHRUeXBlczoge1xuICAgIHJvdXRlOiBvYmplY3QuaXNSZXF1aXJlZFxuICB9LFxuXG4gIGdldENoaWxkQ29udGV4dDogZnVuY3Rpb24gZ2V0Q2hpbGRDb250ZXh0KCkge1xuICAgIHJldHVybiB7XG4gICAgICByb3V0ZTogdGhpcy5wcm9wcy5yb3V0ZVxuICAgIH07XG4gIH0sXG4gIGNvbXBvbmVudFdpbGxNb3VudDogZnVuY3Rpb24gY29tcG9uZW50V2lsbE1vdW50KCkge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnVGhlIGBSb3V0ZUNvbnRleHRgIG1peGluIGlzIGRlcHJlY2F0ZWQuIFlvdSBjYW4gcHJvdmlkZSBgdGhpcy5wcm9wcy5yb3V0ZWAgb24gY29udGV4dCB3aXRoIHlvdXIgb3duIGBjb250ZXh0VHlwZXNgLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItcm91dGVjb250ZXh0bWl4aW4nKSA6IHZvaWQgMDtcbiAgfVxufTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gUm91dGVDb250ZXh0O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5leHBvcnRzLmlzUmVhY3RDaGlsZHJlbiA9IGlzUmVhY3RDaGlsZHJlbjtcbmV4cG9ydHMuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50ID0gY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50O1xuZXhwb3J0cy5jcmVhdGVSb3V0ZXNGcm9tUmVhY3RDaGlsZHJlbiA9IGNyZWF0ZVJvdXRlc0Zyb21SZWFjdENoaWxkcmVuO1xuZXhwb3J0cy5jcmVhdGVSb3V0ZXMgPSBjcmVhdGVSb3V0ZXM7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gaXNWYWxpZENoaWxkKG9iamVjdCkge1xuICByZXR1cm4gb2JqZWN0ID09IG51bGwgfHwgX3JlYWN0Mi5kZWZhdWx0LmlzVmFsaWRFbGVtZW50KG9iamVjdCk7XG59XG5cbmZ1bmN0aW9uIGlzUmVhY3RDaGlsZHJlbihvYmplY3QpIHtcbiAgcmV0dXJuIGlzVmFsaWRDaGlsZChvYmplY3QpIHx8IEFycmF5LmlzQXJyYXkob2JqZWN0KSAmJiBvYmplY3QuZXZlcnkoaXNWYWxpZENoaWxkKTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlUm91dGUoZGVmYXVsdFByb3BzLCBwcm9wcykge1xuICByZXR1cm4gX2V4dGVuZHMoe30sIGRlZmF1bHRQcm9wcywgcHJvcHMpO1xufVxuXG5mdW5jdGlvbiBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQoZWxlbWVudCkge1xuICB2YXIgdHlwZSA9IGVsZW1lbnQudHlwZTtcbiAgdmFyIHJvdXRlID0gY3JlYXRlUm91dGUodHlwZS5kZWZhdWx0UHJvcHMsIGVsZW1lbnQucHJvcHMpO1xuXG4gIGlmIChyb3V0ZS5jaGlsZHJlbikge1xuICAgIHZhciBjaGlsZFJvdXRlcyA9IGNyZWF0ZVJvdXRlc0Zyb21SZWFjdENoaWxkcmVuKHJvdXRlLmNoaWxkcmVuLCByb3V0ZSk7XG5cbiAgICBpZiAoY2hpbGRSb3V0ZXMubGVuZ3RoKSByb3V0ZS5jaGlsZFJvdXRlcyA9IGNoaWxkUm91dGVzO1xuXG4gICAgZGVsZXRlIHJvdXRlLmNoaWxkcmVuO1xuICB9XG5cbiAgcmV0dXJuIHJvdXRlO1xufVxuXG4vKipcbiAqIENyZWF0ZXMgYW5kIHJldHVybnMgYSByb3V0ZXMgb2JqZWN0IGZyb20gdGhlIGdpdmVuIFJlYWN0Q2hpbGRyZW4uIEpTWFxuICogcHJvdmlkZXMgYSBjb252ZW5pZW50IHdheSB0byB2aXN1YWxpemUgaG93IHJvdXRlcyBpbiB0aGUgaGllcmFyY2h5IGFyZVxuICogbmVzdGVkLlxuICpcbiAqICAgaW1wb3J0IHsgUm91dGUsIGNyZWF0ZVJvdXRlc0Zyb21SZWFjdENoaWxkcmVuIH0gZnJvbSAncmVhY3Qtcm91dGVyJ1xuICpcbiAqICAgY29uc3Qgcm91dGVzID0gY3JlYXRlUm91dGVzRnJvbVJlYWN0Q2hpbGRyZW4oXG4gKiAgICAgPFJvdXRlIGNvbXBvbmVudD17QXBwfT5cbiAqICAgICAgIDxSb3V0ZSBwYXRoPVwiaG9tZVwiIGNvbXBvbmVudD17RGFzaGJvYXJkfS8+XG4gKiAgICAgICA8Um91dGUgcGF0aD1cIm5ld3NcIiBjb21wb25lbnQ9e05ld3NGZWVkfS8+XG4gKiAgICAgPC9Sb3V0ZT5cbiAqICAgKVxuICpcbiAqIE5vdGU6IFRoaXMgbWV0aG9kIGlzIGF1dG9tYXRpY2FsbHkgdXNlZCB3aGVuIHlvdSBwcm92aWRlIDxSb3V0ZT4gY2hpbGRyZW5cbiAqIHRvIGEgPFJvdXRlcj4gY29tcG9uZW50LlxuICovXG5mdW5jdGlvbiBjcmVhdGVSb3V0ZXNGcm9tUmVhY3RDaGlsZHJlbihjaGlsZHJlbiwgcGFyZW50Um91dGUpIHtcbiAgdmFyIHJvdXRlcyA9IFtdO1xuXG4gIF9yZWFjdDIuZGVmYXVsdC5DaGlsZHJlbi5mb3JFYWNoKGNoaWxkcmVuLCBmdW5jdGlvbiAoZWxlbWVudCkge1xuICAgIGlmIChfcmVhY3QyLmRlZmF1bHQuaXNWYWxpZEVsZW1lbnQoZWxlbWVudCkpIHtcbiAgICAgIC8vIENvbXBvbmVudCBjbGFzc2VzIG1heSBoYXZlIGEgc3RhdGljIGNyZWF0ZSogbWV0aG9kLlxuICAgICAgaWYgKGVsZW1lbnQudHlwZS5jcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQpIHtcbiAgICAgICAgdmFyIHJvdXRlID0gZWxlbWVudC50eXBlLmNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50LCBwYXJlbnRSb3V0ZSk7XG5cbiAgICAgICAgaWYgKHJvdXRlKSByb3V0ZXMucHVzaChyb3V0ZSk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByb3V0ZXMucHVzaChjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQoZWxlbWVudCkpO1xuICAgICAgfVxuICAgIH1cbiAgfSk7XG5cbiAgcmV0dXJuIHJvdXRlcztcbn1cblxuLyoqXG4gKiBDcmVhdGVzIGFuZCByZXR1cm5zIGFuIGFycmF5IG9mIHJvdXRlcyBmcm9tIHRoZSBnaXZlbiBvYmplY3Qgd2hpY2hcbiAqIG1heSBiZSBhIEpTWCByb3V0ZSwgYSBwbGFpbiBvYmplY3Qgcm91dGUsIG9yIGFuIGFycmF5IG9mIGVpdGhlci5cbiAqL1xuZnVuY3Rpb24gY3JlYXRlUm91dGVzKHJvdXRlcykge1xuICBpZiAoaXNSZWFjdENoaWxkcmVuKHJvdXRlcykpIHtcbiAgICByb3V0ZXMgPSBjcmVhdGVSb3V0ZXNGcm9tUmVhY3RDaGlsZHJlbihyb3V0ZXMpO1xuICB9IGVsc2UgaWYgKHJvdXRlcyAmJiAhQXJyYXkuaXNBcnJheShyb3V0ZXMpKSB7XG4gICAgcm91dGVzID0gW3JvdXRlc107XG4gIH1cblxuICByZXR1cm4gcm91dGVzO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxudmFyIF9jcmVhdGVIYXNoSGlzdG9yeSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL2NyZWF0ZUhhc2hIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlSGFzaEhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlSGFzaEhpc3RvcnkpO1xuXG52YXIgX3VzZVF1ZXJpZXMgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi91c2VRdWVyaWVzJyk7XG5cbnZhciBfdXNlUXVlcmllczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF91c2VRdWVyaWVzKTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyID0gcmVxdWlyZSgnLi9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcicpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyKTtcblxudmFyIF9JbnRlcm5hbFByb3BUeXBlcyA9IHJlcXVpcmUoJy4vSW50ZXJuYWxQcm9wVHlwZXMnKTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0ID0gcmVxdWlyZSgnLi9Sb3V0ZXJDb250ZXh0Jyk7XG5cbnZhciBfUm91dGVyQ29udGV4dDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0ZXJDb250ZXh0KTtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbnZhciBfUm91dGVyVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlclV0aWxzJyk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIF9vYmplY3RXaXRob3V0UHJvcGVydGllcyhvYmosIGtleXMpIHsgdmFyIHRhcmdldCA9IHt9OyBmb3IgKHZhciBpIGluIG9iaikgeyBpZiAoa2V5cy5pbmRleE9mKGkpID49IDApIGNvbnRpbnVlOyBpZiAoIU9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmosIGkpKSBjb250aW51ZTsgdGFyZ2V0W2ldID0gb2JqW2ldOyB9IHJldHVybiB0YXJnZXQ7IH1cblxuZnVuY3Rpb24gaXNEZXByZWNhdGVkSGlzdG9yeShoaXN0b3J5KSB7XG4gIHJldHVybiAhaGlzdG9yeSB8fCAhaGlzdG9yeS5fX3YyX2NvbXBhdGlibGVfXztcbn1cblxuLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHNhbml0eSBjaGVjayAqL1xuZnVuY3Rpb24gaXNVbnN1cHBvcnRlZEhpc3RvcnkoaGlzdG9yeSkge1xuICAvLyB2MyBoaXN0b3JpZXMgZXhwb3NlIGdldEN1cnJlbnRMb2NhdGlvbiwgYnV0IGFyZW4ndCBjdXJyZW50bHkgc3VwcG9ydGVkLlxuICByZXR1cm4gaGlzdG9yeSAmJiBoaXN0b3J5LmdldEN1cnJlbnRMb2NhdGlvbjtcbn1cblxudmFyIF9SZWFjdCRQcm9wVHlwZXMgPSBfcmVhY3QyLmRlZmF1bHQuUHJvcFR5cGVzO1xudmFyIGZ1bmMgPSBfUmVhY3QkUHJvcFR5cGVzLmZ1bmM7XG52YXIgb2JqZWN0ID0gX1JlYWN0JFByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogQSA8Um91dGVyPiBpcyBhIGhpZ2gtbGV2ZWwgQVBJIGZvciBhdXRvbWF0aWNhbGx5IHNldHRpbmcgdXBcbiAqIGEgcm91dGVyIHRoYXQgcmVuZGVycyBhIDxSb3V0ZXJDb250ZXh0PiB3aXRoIGFsbCB0aGUgcHJvcHNcbiAqIGl0IG5lZWRzIGVhY2ggdGltZSB0aGUgVVJMIGNoYW5nZXMuXG4gKi9cblxudmFyIFJvdXRlciA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnUm91dGVyJyxcblxuXG4gIHByb3BUeXBlczoge1xuICAgIGhpc3Rvcnk6IG9iamVjdCxcbiAgICBjaGlsZHJlbjogX0ludGVybmFsUHJvcFR5cGVzLnJvdXRlcyxcbiAgICByb3V0ZXM6IF9JbnRlcm5hbFByb3BUeXBlcy5yb3V0ZXMsIC8vIGFsaWFzIGZvciBjaGlsZHJlblxuICAgIHJlbmRlcjogZnVuYyxcbiAgICBjcmVhdGVFbGVtZW50OiBmdW5jLFxuICAgIG9uRXJyb3I6IGZ1bmMsXG4gICAgb25VcGRhdGU6IGZ1bmMsXG5cbiAgICAvLyBEZXByZWNhdGVkOlxuICAgIHBhcnNlUXVlcnlTdHJpbmc6IGZ1bmMsXG4gICAgc3RyaW5naWZ5UXVlcnk6IGZ1bmMsXG5cbiAgICAvLyBQUklWQVRFOiBGb3IgY2xpZW50LXNpZGUgcmVoeWRyYXRpb24gb2Ygc2VydmVyIG1hdGNoLlxuICAgIG1hdGNoQ29udGV4dDogb2JqZWN0XG4gIH0sXG5cbiAgZ2V0RGVmYXVsdFByb3BzOiBmdW5jdGlvbiBnZXREZWZhdWx0UHJvcHMoKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKHByb3BzKSB7XG4gICAgICAgIHJldHVybiBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudChfUm91dGVyQ29udGV4dDIuZGVmYXVsdCwgcHJvcHMpO1xuICAgICAgfVxuICAgIH07XG4gIH0sXG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24gZ2V0SW5pdGlhbFN0YXRlKCkge1xuICAgIHJldHVybiB7XG4gICAgICBsb2NhdGlvbjogbnVsbCxcbiAgICAgIHJvdXRlczogbnVsbCxcbiAgICAgIHBhcmFtczogbnVsbCxcbiAgICAgIGNvbXBvbmVudHM6IG51bGxcbiAgICB9O1xuICB9LFxuICBoYW5kbGVFcnJvcjogZnVuY3Rpb24gaGFuZGxlRXJyb3IoZXJyb3IpIHtcbiAgICBpZiAodGhpcy5wcm9wcy5vbkVycm9yKSB7XG4gICAgICB0aGlzLnByb3BzLm9uRXJyb3IuY2FsbCh0aGlzLCBlcnJvcik7XG4gICAgfSBlbHNlIHtcbiAgICAgIC8vIFRocm93IGVycm9ycyBieSBkZWZhdWx0IHNvIHdlIGRvbid0IHNpbGVudGx5IHN3YWxsb3cgdGhlbSFcbiAgICAgIHRocm93IGVycm9yOyAvLyBUaGlzIGVycm9yIHByb2JhYmx5IG9jY3VycmVkIGluIGdldENoaWxkUm91dGVzIG9yIGdldENvbXBvbmVudHMuXG4gICAgfVxuICB9LFxuICBjb21wb25lbnRXaWxsTW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxNb3VudCgpIHtcbiAgICB2YXIgX3RoaXMgPSB0aGlzO1xuXG4gICAgdmFyIF9wcm9wcyA9IHRoaXMucHJvcHM7XG4gICAgdmFyIHBhcnNlUXVlcnlTdHJpbmcgPSBfcHJvcHMucGFyc2VRdWVyeVN0cmluZztcbiAgICB2YXIgc3RyaW5naWZ5UXVlcnkgPSBfcHJvcHMuc3RyaW5naWZ5UXVlcnk7XG5cbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KSghKHBhcnNlUXVlcnlTdHJpbmcgfHwgc3RyaW5naWZ5UXVlcnkpLCAnYHBhcnNlUXVlcnlTdHJpbmdgIGFuZCBgc3RyaW5naWZ5UXVlcnlgIGFyZSBkZXByZWNhdGVkLiBQbGVhc2UgY3JlYXRlIGEgY3VzdG9tIGhpc3RvcnkuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1jdXN0b21xdWVyeXN0cmluZycpIDogdm9pZCAwO1xuXG4gICAgdmFyIF9jcmVhdGVSb3V0ZXJPYmplY3RzID0gdGhpcy5jcmVhdGVSb3V0ZXJPYmplY3RzKCk7XG5cbiAgICB2YXIgaGlzdG9yeSA9IF9jcmVhdGVSb3V0ZXJPYmplY3RzLmhpc3Rvcnk7XG4gICAgdmFyIHRyYW5zaXRpb25NYW5hZ2VyID0gX2NyZWF0ZVJvdXRlck9iamVjdHMudHJhbnNpdGlvbk1hbmFnZXI7XG4gICAgdmFyIHJvdXRlciA9IF9jcmVhdGVSb3V0ZXJPYmplY3RzLnJvdXRlcjtcblxuXG4gICAgdGhpcy5fdW5saXN0ZW4gPSB0cmFuc2l0aW9uTWFuYWdlci5saXN0ZW4oZnVuY3Rpb24gKGVycm9yLCBzdGF0ZSkge1xuICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgIF90aGlzLmhhbmRsZUVycm9yKGVycm9yKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIF90aGlzLnNldFN0YXRlKHN0YXRlLCBfdGhpcy5wcm9wcy5vblVwZGF0ZSk7XG4gICAgICB9XG4gICAgfSk7XG5cbiAgICB0aGlzLmhpc3RvcnkgPSBoaXN0b3J5O1xuICAgIHRoaXMucm91dGVyID0gcm91dGVyO1xuICB9LFxuICBjcmVhdGVSb3V0ZXJPYmplY3RzOiBmdW5jdGlvbiBjcmVhdGVSb3V0ZXJPYmplY3RzKCkge1xuICAgIHZhciBtYXRjaENvbnRleHQgPSB0aGlzLnByb3BzLm1hdGNoQ29udGV4dDtcblxuICAgIGlmIChtYXRjaENvbnRleHQpIHtcbiAgICAgIHJldHVybiBtYXRjaENvbnRleHQ7XG4gICAgfVxuXG4gICAgdmFyIGhpc3RvcnkgPSB0aGlzLnByb3BzLmhpc3Rvcnk7XG4gICAgdmFyIF9wcm9wczIgPSB0aGlzLnByb3BzO1xuICAgIHZhciByb3V0ZXMgPSBfcHJvcHMyLnJvdXRlcztcbiAgICB2YXIgY2hpbGRyZW4gPSBfcHJvcHMyLmNoaWxkcmVuO1xuXG5cbiAgICAhIWlzVW5zdXBwb3J0ZWRIaXN0b3J5KGhpc3RvcnkpID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJ1lvdSBoYXZlIHByb3ZpZGVkIGEgaGlzdG9yeSBvYmplY3QgY3JlYXRlZCB3aXRoIGhpc3RvcnkgdjMueC4gJyArICdUaGlzIHZlcnNpb24gb2YgUmVhY3QgUm91dGVyIGlzIG5vdCBjb21wYXRpYmxlIHdpdGggdjMgaGlzdG9yeSAnICsgJ29iamVjdHMuIFBsZWFzZSB1c2UgaGlzdG9yeSB2Mi54IGluc3RlYWQuJykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuXG4gICAgaWYgKGlzRGVwcmVjYXRlZEhpc3RvcnkoaGlzdG9yeSkpIHtcbiAgICAgIGhpc3RvcnkgPSB0aGlzLndyYXBEZXByZWNhdGVkSGlzdG9yeShoaXN0b3J5KTtcbiAgICB9XG5cbiAgICB2YXIgdHJhbnNpdGlvbk1hbmFnZXIgPSAoMCwgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyMi5kZWZhdWx0KShoaXN0b3J5LCAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVzKShyb3V0ZXMgfHwgY2hpbGRyZW4pKTtcbiAgICB2YXIgcm91dGVyID0gKDAsIF9Sb3V0ZXJVdGlscy5jcmVhdGVSb3V0ZXJPYmplY3QpKGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyKTtcbiAgICB2YXIgcm91dGluZ0hpc3RvcnkgPSAoMCwgX1JvdXRlclV0aWxzLmNyZWF0ZVJvdXRpbmdIaXN0b3J5KShoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG5cbiAgICByZXR1cm4geyBoaXN0b3J5OiByb3V0aW5nSGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXI6IHRyYW5zaXRpb25NYW5hZ2VyLCByb3V0ZXI6IHJvdXRlciB9O1xuICB9LFxuICB3cmFwRGVwcmVjYXRlZEhpc3Rvcnk6IGZ1bmN0aW9uIHdyYXBEZXByZWNhdGVkSGlzdG9yeShoaXN0b3J5KSB7XG4gICAgdmFyIF9wcm9wczMgPSB0aGlzLnByb3BzO1xuICAgIHZhciBwYXJzZVF1ZXJ5U3RyaW5nID0gX3Byb3BzMy5wYXJzZVF1ZXJ5U3RyaW5nO1xuICAgIHZhciBzdHJpbmdpZnlRdWVyeSA9IF9wcm9wczMuc3RyaW5naWZ5UXVlcnk7XG5cblxuICAgIHZhciBjcmVhdGVIaXN0b3J5ID0gdm9pZCAwO1xuICAgIGlmIChoaXN0b3J5KSB7XG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ0l0IGFwcGVhcnMgeW91IGhhdmUgcHJvdmlkZWQgYSBkZXByZWNhdGVkIGhpc3Rvcnkgb2JqZWN0IHRvIGA8Um91dGVyLz5gLCBwbGVhc2UgdXNlIGEgaGlzdG9yeSBwcm92aWRlZCBieSAnICsgJ1JlYWN0IFJvdXRlciB3aXRoIGBpbXBvcnQgeyBicm93c2VySGlzdG9yeSB9IGZyb20gXFwncmVhY3Qtcm91dGVyXFwnYCBvciBgaW1wb3J0IHsgaGFzaEhpc3RvcnkgfSBmcm9tIFxcJ3JlYWN0LXJvdXRlclxcJ2AuICcgKyAnSWYgeW91IGFyZSB1c2luZyBhIGN1c3RvbSBoaXN0b3J5IHBsZWFzZSBjcmVhdGUgaXQgd2l0aCBgdXNlUm91dGVySGlzdG9yeWAsIHNlZSBodHRwOi8vdGlueS5jYy9yb3V0ZXItdXNpbmdoaXN0b3J5IGZvciBkZXRhaWxzLicpIDogdm9pZCAwO1xuICAgICAgY3JlYXRlSGlzdG9yeSA9IGZ1bmN0aW9uIGNyZWF0ZUhpc3RvcnkoKSB7XG4gICAgICAgIHJldHVybiBoaXN0b3J5O1xuICAgICAgfTtcbiAgICB9IGVsc2Uge1xuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdgUm91dGVyYCBubyBsb25nZXIgZGVmYXVsdHMgdGhlIGhpc3RvcnkgcHJvcCB0byBoYXNoIGhpc3RvcnkuIFBsZWFzZSB1c2UgdGhlIGBoYXNoSGlzdG9yeWAgc2luZ2xldG9uIGluc3RlYWQuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1kZWZhdWx0aGlzdG9yeScpIDogdm9pZCAwO1xuICAgICAgY3JlYXRlSGlzdG9yeSA9IF9jcmVhdGVIYXNoSGlzdG9yeTIuZGVmYXVsdDtcbiAgICB9XG5cbiAgICByZXR1cm4gKDAsIF91c2VRdWVyaWVzMi5kZWZhdWx0KShjcmVhdGVIaXN0b3J5KSh7IHBhcnNlUXVlcnlTdHJpbmc6IHBhcnNlUXVlcnlTdHJpbmcsIHN0cmluZ2lmeVF1ZXJ5OiBzdHJpbmdpZnlRdWVyeSB9KTtcbiAgfSxcblxuXG4gIC8qIGlzdGFuYnVsIGlnbm9yZSBuZXh0OiBzYW5pdHkgY2hlY2sgKi9cbiAgY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wczogZnVuY3Rpb24gY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShuZXh0UHJvcHMuaGlzdG9yeSA9PT0gdGhpcy5wcm9wcy5oaXN0b3J5LCAnWW91IGNhbm5vdCBjaGFuZ2UgPFJvdXRlciBoaXN0b3J5PjsgaXQgd2lsbCBiZSBpZ25vcmVkJykgOiB2b2lkIDA7XG5cbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KSgobmV4dFByb3BzLnJvdXRlcyB8fCBuZXh0UHJvcHMuY2hpbGRyZW4pID09PSAodGhpcy5wcm9wcy5yb3V0ZXMgfHwgdGhpcy5wcm9wcy5jaGlsZHJlbiksICdZb3UgY2Fubm90IGNoYW5nZSA8Um91dGVyIHJvdXRlcz47IGl0IHdpbGwgYmUgaWdub3JlZCcpIDogdm9pZCAwO1xuICB9LFxuICBjb21wb25lbnRXaWxsVW5tb3VudDogZnVuY3Rpb24gY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgaWYgKHRoaXMuX3VubGlzdGVuKSB0aGlzLl91bmxpc3RlbigpO1xuICB9LFxuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICB2YXIgX3N0YXRlID0gdGhpcy5zdGF0ZTtcbiAgICB2YXIgbG9jYXRpb24gPSBfc3RhdGUubG9jYXRpb247XG4gICAgdmFyIHJvdXRlcyA9IF9zdGF0ZS5yb3V0ZXM7XG4gICAgdmFyIHBhcmFtcyA9IF9zdGF0ZS5wYXJhbXM7XG4gICAgdmFyIGNvbXBvbmVudHMgPSBfc3RhdGUuY29tcG9uZW50cztcbiAgICB2YXIgX3Byb3BzNCA9IHRoaXMucHJvcHM7XG4gICAgdmFyIGNyZWF0ZUVsZW1lbnQgPSBfcHJvcHM0LmNyZWF0ZUVsZW1lbnQ7XG4gICAgdmFyIHJlbmRlciA9IF9wcm9wczQucmVuZGVyO1xuXG4gICAgdmFyIHByb3BzID0gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKF9wcm9wczQsIFsnY3JlYXRlRWxlbWVudCcsICdyZW5kZXInXSk7XG5cbiAgICBpZiAobG9jYXRpb24gPT0gbnVsbCkgcmV0dXJuIG51bGw7IC8vIEFzeW5jIG1hdGNoXG5cbiAgICAvLyBPbmx5IGZvcndhcmQgbm9uLVJvdXRlci1zcGVjaWZpYyBwcm9wcyB0byByb3V0aW5nIGNvbnRleHQsIGFzIHRob3NlIGFyZVxuICAgIC8vIHRoZSBvbmx5IG9uZXMgdGhhdCBtaWdodCBiZSBjdXN0b20gcm91dGluZyBjb250ZXh0IHByb3BzLlxuICAgIE9iamVjdC5rZXlzKFJvdXRlci5wcm9wVHlwZXMpLmZvckVhY2goZnVuY3Rpb24gKHByb3BUeXBlKSB7XG4gICAgICByZXR1cm4gZGVsZXRlIHByb3BzW3Byb3BUeXBlXTtcbiAgICB9KTtcblxuICAgIHJldHVybiByZW5kZXIoX2V4dGVuZHMoe30sIHByb3BzLCB7XG4gICAgICBoaXN0b3J5OiB0aGlzLmhpc3RvcnksXG4gICAgICByb3V0ZXI6IHRoaXMucm91dGVyLFxuICAgICAgbG9jYXRpb246IGxvY2F0aW9uLFxuICAgICAgcm91dGVzOiByb3V0ZXMsXG4gICAgICBwYXJhbXM6IHBhcmFtcyxcbiAgICAgIGNvbXBvbmVudHM6IGNvbXBvbmVudHMsXG4gICAgICBjcmVhdGVFbGVtZW50OiBjcmVhdGVFbGVtZW50XG4gICAgfSkpO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gUm91dGVyO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3R5cGVvZiA9IHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiB0eXBlb2YgU3ltYm9sLml0ZXJhdG9yID09PSBcInN5bWJvbFwiID8gZnVuY3Rpb24gKG9iaikgeyByZXR1cm4gdHlwZW9mIG9iajsgfSA6IGZ1bmN0aW9uIChvYmopIHsgcmV0dXJuIG9iaiAmJiB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgb2JqLmNvbnN0cnVjdG9yID09PSBTeW1ib2wgPyBcInN5bWJvbFwiIDogdHlwZW9mIG9iajsgfTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMgPSByZXF1aXJlKCcuL2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMnKTtcblxudmFyIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMpO1xuXG52YXIgX2dldFJvdXRlUGFyYW1zID0gcmVxdWlyZSgnLi9nZXRSb3V0ZVBhcmFtcycpO1xuXG52YXIgX2dldFJvdXRlUGFyYW1zMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2dldFJvdXRlUGFyYW1zKTtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBfUmVhY3QkUHJvcFR5cGVzID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcztcbnZhciBhcnJheSA9IF9SZWFjdCRQcm9wVHlwZXMuYXJyYXk7XG52YXIgZnVuYyA9IF9SZWFjdCRQcm9wVHlwZXMuZnVuYztcbnZhciBvYmplY3QgPSBfUmVhY3QkUHJvcFR5cGVzLm9iamVjdDtcblxuLyoqXG4gKiBBIDxSb3V0ZXJDb250ZXh0PiByZW5kZXJzIHRoZSBjb21wb25lbnQgdHJlZSBmb3IgYSBnaXZlbiByb3V0ZXIgc3RhdGVcbiAqIGFuZCBzZXRzIHRoZSBoaXN0b3J5IG9iamVjdCBhbmQgdGhlIGN1cnJlbnQgbG9jYXRpb24gaW4gY29udGV4dC5cbiAqL1xuXG52YXIgUm91dGVyQ29udGV4dCA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnUm91dGVyQ29udGV4dCcsXG5cblxuICBwcm9wVHlwZXM6IHtcbiAgICBoaXN0b3J5OiBvYmplY3QsXG4gICAgcm91dGVyOiBvYmplY3QuaXNSZXF1aXJlZCxcbiAgICBsb2NhdGlvbjogb2JqZWN0LmlzUmVxdWlyZWQsXG4gICAgcm91dGVzOiBhcnJheS5pc1JlcXVpcmVkLFxuICAgIHBhcmFtczogb2JqZWN0LmlzUmVxdWlyZWQsXG4gICAgY29tcG9uZW50czogYXJyYXkuaXNSZXF1aXJlZCxcbiAgICBjcmVhdGVFbGVtZW50OiBmdW5jLmlzUmVxdWlyZWRcbiAgfSxcblxuICBnZXREZWZhdWx0UHJvcHM6IGZ1bmN0aW9uIGdldERlZmF1bHRQcm9wcygpIHtcbiAgICByZXR1cm4ge1xuICAgICAgY3JlYXRlRWxlbWVudDogX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUVsZW1lbnRcbiAgICB9O1xuICB9LFxuXG5cbiAgY2hpbGRDb250ZXh0VHlwZXM6IHtcbiAgICBoaXN0b3J5OiBvYmplY3QsXG4gICAgbG9jYXRpb246IG9iamVjdC5pc1JlcXVpcmVkLFxuICAgIHJvdXRlcjogb2JqZWN0LmlzUmVxdWlyZWRcbiAgfSxcblxuICBnZXRDaGlsZENvbnRleHQ6IGZ1bmN0aW9uIGdldENoaWxkQ29udGV4dCgpIHtcbiAgICB2YXIgX3Byb3BzID0gdGhpcy5wcm9wcztcbiAgICB2YXIgcm91dGVyID0gX3Byb3BzLnJvdXRlcjtcbiAgICB2YXIgaGlzdG9yeSA9IF9wcm9wcy5oaXN0b3J5O1xuICAgIHZhciBsb2NhdGlvbiA9IF9wcm9wcy5sb2NhdGlvbjtcblxuICAgIGlmICghcm91dGVyKSB7XG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2A8Um91dGVyQ29udGV4dD5gIGV4cGVjdHMgYSBgcm91dGVyYCByYXRoZXIgdGhhbiBhIGBoaXN0b3J5YCcpIDogdm9pZCAwO1xuXG4gICAgICByb3V0ZXIgPSBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwge1xuICAgICAgICBzZXRSb3V0ZUxlYXZlSG9vazogaGlzdG9yeS5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGVcbiAgICAgIH0pO1xuICAgICAgZGVsZXRlIHJvdXRlci5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGU7XG4gICAgfVxuXG4gICAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgIGxvY2F0aW9uID0gKDAsIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzMi5kZWZhdWx0KShsb2NhdGlvbiwgJ2Bjb250ZXh0LmxvY2F0aW9uYCBpcyBkZXByZWNhdGVkLCBwbGVhc2UgdXNlIGEgcm91dGUgY29tcG9uZW50XFwncyBgcHJvcHMubG9jYXRpb25gIGluc3RlYWQuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1hY2Nlc3Npbmdsb2NhdGlvbicpO1xuICAgIH1cblxuICAgIHJldHVybiB7IGhpc3Rvcnk6IGhpc3RvcnksIGxvY2F0aW9uOiBsb2NhdGlvbiwgcm91dGVyOiByb3V0ZXIgfTtcbiAgfSxcbiAgY3JlYXRlRWxlbWVudDogZnVuY3Rpb24gY3JlYXRlRWxlbWVudChjb21wb25lbnQsIHByb3BzKSB7XG4gICAgcmV0dXJuIGNvbXBvbmVudCA9PSBudWxsID8gbnVsbCA6IHRoaXMucHJvcHMuY3JlYXRlRWxlbWVudChjb21wb25lbnQsIHByb3BzKTtcbiAgfSxcbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgdmFyIF90aGlzID0gdGhpcztcblxuICAgIHZhciBfcHJvcHMyID0gdGhpcy5wcm9wcztcbiAgICB2YXIgaGlzdG9yeSA9IF9wcm9wczIuaGlzdG9yeTtcbiAgICB2YXIgbG9jYXRpb24gPSBfcHJvcHMyLmxvY2F0aW9uO1xuICAgIHZhciByb3V0ZXMgPSBfcHJvcHMyLnJvdXRlcztcbiAgICB2YXIgcGFyYW1zID0gX3Byb3BzMi5wYXJhbXM7XG4gICAgdmFyIGNvbXBvbmVudHMgPSBfcHJvcHMyLmNvbXBvbmVudHM7XG5cbiAgICB2YXIgZWxlbWVudCA9IG51bGw7XG5cbiAgICBpZiAoY29tcG9uZW50cykge1xuICAgICAgZWxlbWVudCA9IGNvbXBvbmVudHMucmVkdWNlUmlnaHQoZnVuY3Rpb24gKGVsZW1lbnQsIGNvbXBvbmVudHMsIGluZGV4KSB7XG4gICAgICAgIGlmIChjb21wb25lbnRzID09IG51bGwpIHJldHVybiBlbGVtZW50OyAvLyBEb24ndCBjcmVhdGUgbmV3IGNoaWxkcmVuOyB1c2UgdGhlIGdyYW5kY2hpbGRyZW4uXG5cbiAgICAgICAgdmFyIHJvdXRlID0gcm91dGVzW2luZGV4XTtcbiAgICAgICAgdmFyIHJvdXRlUGFyYW1zID0gKDAsIF9nZXRSb3V0ZVBhcmFtczIuZGVmYXVsdCkocm91dGUsIHBhcmFtcyk7XG4gICAgICAgIHZhciBwcm9wcyA9IHtcbiAgICAgICAgICBoaXN0b3J5OiBoaXN0b3J5LFxuICAgICAgICAgIGxvY2F0aW9uOiBsb2NhdGlvbixcbiAgICAgICAgICBwYXJhbXM6IHBhcmFtcyxcbiAgICAgICAgICByb3V0ZTogcm91dGUsXG4gICAgICAgICAgcm91dGVQYXJhbXM6IHJvdXRlUGFyYW1zLFxuICAgICAgICAgIHJvdXRlczogcm91dGVzXG4gICAgICAgIH07XG5cbiAgICAgICAgaWYgKCgwLCBfUm91dGVVdGlscy5pc1JlYWN0Q2hpbGRyZW4pKGVsZW1lbnQpKSB7XG4gICAgICAgICAgcHJvcHMuY2hpbGRyZW4gPSBlbGVtZW50O1xuICAgICAgICB9IGVsc2UgaWYgKGVsZW1lbnQpIHtcbiAgICAgICAgICBmb3IgKHZhciBwcm9wIGluIGVsZW1lbnQpIHtcbiAgICAgICAgICAgIGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoZWxlbWVudCwgcHJvcCkpIHByb3BzW3Byb3BdID0gZWxlbWVudFtwcm9wXTtcbiAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICBpZiAoKHR5cGVvZiBjb21wb25lbnRzID09PSAndW5kZWZpbmVkJyA/ICd1bmRlZmluZWQnIDogX3R5cGVvZihjb21wb25lbnRzKSkgPT09ICdvYmplY3QnKSB7XG4gICAgICAgICAgdmFyIGVsZW1lbnRzID0ge307XG5cbiAgICAgICAgICBmb3IgKHZhciBrZXkgaW4gY29tcG9uZW50cykge1xuICAgICAgICAgICAgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChjb21wb25lbnRzLCBrZXkpKSB7XG4gICAgICAgICAgICAgIC8vIFBhc3MgdGhyb3VnaCB0aGUga2V5IGFzIGEgcHJvcCB0byBjcmVhdGVFbGVtZW50IHRvIGFsbG93XG4gICAgICAgICAgICAgIC8vIGN1c3RvbSBjcmVhdGVFbGVtZW50IGZ1bmN0aW9ucyB0byBrbm93IHdoaWNoIG5hbWVkIGNvbXBvbmVudFxuICAgICAgICAgICAgICAvLyB0aGV5J3JlIHJlbmRlcmluZywgZm9yIGUuZy4gbWF0Y2hpbmcgdXAgdG8gZmV0Y2hlZCBkYXRhLlxuICAgICAgICAgICAgICBlbGVtZW50c1trZXldID0gX3RoaXMuY3JlYXRlRWxlbWVudChjb21wb25lbnRzW2tleV0sIF9leHRlbmRzKHtcbiAgICAgICAgICAgICAgICBrZXk6IGtleSB9LCBwcm9wcykpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH1cblxuICAgICAgICAgIHJldHVybiBlbGVtZW50cztcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBfdGhpcy5jcmVhdGVFbGVtZW50KGNvbXBvbmVudHMsIHByb3BzKTtcbiAgICAgIH0sIGVsZW1lbnQpO1xuICAgIH1cblxuICAgICEoZWxlbWVudCA9PT0gbnVsbCB8fCBlbGVtZW50ID09PSBmYWxzZSB8fCBfcmVhY3QyLmRlZmF1bHQuaXNWYWxpZEVsZW1lbnQoZWxlbWVudCkpID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJ1RoZSByb290IHJvdXRlIG11c3QgcmVuZGVyIGEgc2luZ2xlIGVsZW1lbnQnKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICByZXR1cm4gZWxlbWVudDtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IFJvdXRlckNvbnRleHQ7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHMuY3JlYXRlUm91dGVyT2JqZWN0ID0gY3JlYXRlUm91dGVyT2JqZWN0O1xuZXhwb3J0cy5jcmVhdGVSb3V0aW5nSGlzdG9yeSA9IGNyZWF0ZVJvdXRpbmdIaXN0b3J5O1xuXG52YXIgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMgPSByZXF1aXJlKCcuL2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMnKTtcblxudmFyIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBjcmVhdGVSb3V0ZXJPYmplY3QoaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXIpIHtcbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB7XG4gICAgc2V0Um91dGVMZWF2ZUhvb2s6IHRyYW5zaXRpb25NYW5hZ2VyLmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZSxcbiAgICBpc0FjdGl2ZTogdHJhbnNpdGlvbk1hbmFnZXIuaXNBY3RpdmVcbiAgfSk7XG59XG5cbi8vIGRlcHJlY2F0ZWRcbmZ1bmN0aW9uIGNyZWF0ZVJvdXRpbmdIaXN0b3J5KGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyKSB7XG4gIGhpc3RvcnkgPSBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXIpO1xuXG4gIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgaGlzdG9yeSA9ICgwLCBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllczIuZGVmYXVsdCkoaGlzdG9yeSwgJ2Bwcm9wcy5oaXN0b3J5YCBhbmQgYGNvbnRleHQuaGlzdG9yeWAgYXJlIGRlcHJlY2F0ZWQuIFBsZWFzZSB1c2UgYGNvbnRleHQucm91dGVyYC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWNvbnRleHRjaGFuZ2VzJyk7XG4gIH1cblxuICByZXR1cm4gaGlzdG9yeTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0ID0gcmVxdWlyZSgnLi9Sb3V0ZXJDb250ZXh0Jyk7XG5cbnZhciBfUm91dGVyQ29udGV4dDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0ZXJDb250ZXh0KTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIFJvdXRpbmdDb250ZXh0ID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdSb3V0aW5nQ29udGV4dCcsXG4gIGNvbXBvbmVudFdpbGxNb3VudDogZnVuY3Rpb24gY29tcG9uZW50V2lsbE1vdW50KCkge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnYFJvdXRpbmdDb250ZXh0YCBoYXMgYmVlbiByZW5hbWVkIHRvIGBSb3V0ZXJDb250ZXh0YC4gUGxlYXNlIHVzZSBgaW1wb3J0IHsgUm91dGVyQ29udGV4dCB9IGZyb20gXFwncmVhY3Qtcm91dGVyXFwnYC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLXJvdXRlcmNvbnRleHQnKSA6IHZvaWQgMDtcbiAgfSxcbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgcmV0dXJuIF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50KF9Sb3V0ZXJDb250ZXh0Mi5kZWZhdWx0LCB0aGlzLnByb3BzKTtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IFJvdXRpbmdDb250ZXh0O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5ydW5FbnRlckhvb2tzID0gcnVuRW50ZXJIb29rcztcbmV4cG9ydHMucnVuQ2hhbmdlSG9va3MgPSBydW5DaGFuZ2VIb29rcztcbmV4cG9ydHMucnVuTGVhdmVIb29rcyA9IHJ1bkxlYXZlSG9va3M7XG5cbnZhciBfQXN5bmNVdGlscyA9IHJlcXVpcmUoJy4vQXN5bmNVdGlscycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBjcmVhdGVUcmFuc2l0aW9uSG9vayhob29rLCByb3V0ZSwgYXN5bmNBcml0eSkge1xuICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBhcmdzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgICBhcmdzW19rZXldID0gYXJndW1lbnRzW19rZXldO1xuICAgIH1cblxuICAgIGhvb2suYXBwbHkocm91dGUsIGFyZ3MpO1xuXG4gICAgaWYgKGhvb2subGVuZ3RoIDwgYXN5bmNBcml0eSkge1xuICAgICAgdmFyIGNhbGxiYWNrID0gYXJnc1thcmdzLmxlbmd0aCAtIDFdO1xuICAgICAgLy8gQXNzdW1lIGhvb2sgZXhlY3V0ZXMgc3luY2hyb25vdXNseSBhbmRcbiAgICAgIC8vIGF1dG9tYXRpY2FsbHkgY2FsbCB0aGUgY2FsbGJhY2suXG4gICAgICBjYWxsYmFjaygpO1xuICAgIH1cbiAgfTtcbn1cblxuZnVuY3Rpb24gZ2V0RW50ZXJIb29rcyhyb3V0ZXMpIHtcbiAgcmV0dXJuIHJvdXRlcy5yZWR1Y2UoZnVuY3Rpb24gKGhvb2tzLCByb3V0ZSkge1xuICAgIGlmIChyb3V0ZS5vbkVudGVyKSBob29rcy5wdXNoKGNyZWF0ZVRyYW5zaXRpb25Ib29rKHJvdXRlLm9uRW50ZXIsIHJvdXRlLCAzKSk7XG5cbiAgICByZXR1cm4gaG9va3M7XG4gIH0sIFtdKTtcbn1cblxuZnVuY3Rpb24gZ2V0Q2hhbmdlSG9va3Mocm91dGVzKSB7XG4gIHJldHVybiByb3V0ZXMucmVkdWNlKGZ1bmN0aW9uIChob29rcywgcm91dGUpIHtcbiAgICBpZiAocm91dGUub25DaGFuZ2UpIGhvb2tzLnB1c2goY3JlYXRlVHJhbnNpdGlvbkhvb2socm91dGUub25DaGFuZ2UsIHJvdXRlLCA0KSk7XG4gICAgcmV0dXJuIGhvb2tzO1xuICB9LCBbXSk7XG59XG5cbmZ1bmN0aW9uIHJ1blRyYW5zaXRpb25Ib29rcyhsZW5ndGgsIGl0ZXIsIGNhbGxiYWNrKSB7XG4gIGlmICghbGVuZ3RoKSB7XG4gICAgY2FsbGJhY2soKTtcbiAgICByZXR1cm47XG4gIH1cblxuICB2YXIgcmVkaXJlY3RJbmZvID0gdm9pZCAwO1xuICBmdW5jdGlvbiByZXBsYWNlKGxvY2F0aW9uLCBkZXByZWNhdGVkUGF0aG5hbWUsIGRlcHJlY2F0ZWRRdWVyeSkge1xuICAgIGlmIChkZXByZWNhdGVkUGF0aG5hbWUpIHtcbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnYHJlcGxhY2VTdGF0ZShzdGF0ZSwgcGF0aG5hbWUsIHF1ZXJ5KSBpcyBkZXByZWNhdGVkOyB1c2UgYHJlcGxhY2UobG9jYXRpb24pYCB3aXRoIGEgbG9jYXRpb24gZGVzY3JpcHRvciBpbnN0ZWFkLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItaXNBY3RpdmVkZXByZWNhdGVkJykgOiB2b2lkIDA7XG4gICAgICByZWRpcmVjdEluZm8gPSB7XG4gICAgICAgIHBhdGhuYW1lOiBkZXByZWNhdGVkUGF0aG5hbWUsXG4gICAgICAgIHF1ZXJ5OiBkZXByZWNhdGVkUXVlcnksXG4gICAgICAgIHN0YXRlOiBsb2NhdGlvblxuICAgICAgfTtcblxuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIHJlZGlyZWN0SW5mbyA9IGxvY2F0aW9uO1xuICB9XG5cbiAgKDAsIF9Bc3luY1V0aWxzLmxvb3BBc3luYykobGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIG5leHQsIGRvbmUpIHtcbiAgICBpdGVyKGluZGV4LCByZXBsYWNlLCBmdW5jdGlvbiAoZXJyb3IpIHtcbiAgICAgIGlmIChlcnJvciB8fCByZWRpcmVjdEluZm8pIHtcbiAgICAgICAgZG9uZShlcnJvciwgcmVkaXJlY3RJbmZvKTsgLy8gTm8gbmVlZCB0byBjb250aW51ZS5cbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIG5leHQoKTtcbiAgICAgIH1cbiAgICB9KTtcbiAgfSwgY2FsbGJhY2spO1xufVxuXG4vKipcbiAqIFJ1bnMgYWxsIG9uRW50ZXIgaG9va3MgaW4gdGhlIGdpdmVuIGFycmF5IG9mIHJvdXRlcyBpbiBvcmRlclxuICogd2l0aCBvbkVudGVyKG5leHRTdGF0ZSwgcmVwbGFjZSwgY2FsbGJhY2spIGFuZCBjYWxsc1xuICogY2FsbGJhY2soZXJyb3IsIHJlZGlyZWN0SW5mbykgd2hlbiBmaW5pc2hlZC4gVGhlIGZpcnN0IGhvb2tcbiAqIHRvIHVzZSByZXBsYWNlIHNob3J0LWNpcmN1aXRzIHRoZSBsb29wLlxuICpcbiAqIElmIGEgaG9vayBuZWVkcyB0byBydW4gYXN5bmNocm9ub3VzbHksIGl0IG1heSB1c2UgdGhlIGNhbGxiYWNrXG4gKiBmdW5jdGlvbi4gSG93ZXZlciwgZG9pbmcgc28gd2lsbCBjYXVzZSB0aGUgdHJhbnNpdGlvbiB0byBwYXVzZSxcbiAqIHdoaWNoIGNvdWxkIGxlYWQgdG8gYSBub24tcmVzcG9uc2l2ZSBVSSBpZiB0aGUgaG9vayBpcyBzbG93LlxuICovXG5mdW5jdGlvbiBydW5FbnRlckhvb2tzKHJvdXRlcywgbmV4dFN0YXRlLCBjYWxsYmFjaykge1xuICB2YXIgaG9va3MgPSBnZXRFbnRlckhvb2tzKHJvdXRlcyk7XG4gIHJldHVybiBydW5UcmFuc2l0aW9uSG9va3MoaG9va3MubGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIHJlcGxhY2UsIG5leHQpIHtcbiAgICBob29rc1tpbmRleF0obmV4dFN0YXRlLCByZXBsYWNlLCBuZXh0KTtcbiAgfSwgY2FsbGJhY2spO1xufVxuXG4vKipcbiAqIFJ1bnMgYWxsIG9uQ2hhbmdlIGhvb2tzIGluIHRoZSBnaXZlbiBhcnJheSBvZiByb3V0ZXMgaW4gb3JkZXJcbiAqIHdpdGggb25DaGFuZ2UocHJldlN0YXRlLCBuZXh0U3RhdGUsIHJlcGxhY2UsIGNhbGxiYWNrKSBhbmQgY2FsbHNcbiAqIGNhbGxiYWNrKGVycm9yLCByZWRpcmVjdEluZm8pIHdoZW4gZmluaXNoZWQuIFRoZSBmaXJzdCBob29rXG4gKiB0byB1c2UgcmVwbGFjZSBzaG9ydC1jaXJjdWl0cyB0aGUgbG9vcC5cbiAqXG4gKiBJZiBhIGhvb2sgbmVlZHMgdG8gcnVuIGFzeW5jaHJvbm91c2x5LCBpdCBtYXkgdXNlIHRoZSBjYWxsYmFja1xuICogZnVuY3Rpb24uIEhvd2V2ZXIsIGRvaW5nIHNvIHdpbGwgY2F1c2UgdGhlIHRyYW5zaXRpb24gdG8gcGF1c2UsXG4gKiB3aGljaCBjb3VsZCBsZWFkIHRvIGEgbm9uLXJlc3BvbnNpdmUgVUkgaWYgdGhlIGhvb2sgaXMgc2xvdy5cbiAqL1xuZnVuY3Rpb24gcnVuQ2hhbmdlSG9va3Mocm91dGVzLCBzdGF0ZSwgbmV4dFN0YXRlLCBjYWxsYmFjaykge1xuICB2YXIgaG9va3MgPSBnZXRDaGFuZ2VIb29rcyhyb3V0ZXMpO1xuICByZXR1cm4gcnVuVHJhbnNpdGlvbkhvb2tzKGhvb2tzLmxlbmd0aCwgZnVuY3Rpb24gKGluZGV4LCByZXBsYWNlLCBuZXh0KSB7XG4gICAgaG9va3NbaW5kZXhdKHN0YXRlLCBuZXh0U3RhdGUsIHJlcGxhY2UsIG5leHQpO1xuICB9LCBjYWxsYmFjayk7XG59XG5cbi8qKlxuICogUnVucyBhbGwgb25MZWF2ZSBob29rcyBpbiB0aGUgZ2l2ZW4gYXJyYXkgb2Ygcm91dGVzIGluIG9yZGVyLlxuICovXG5mdW5jdGlvbiBydW5MZWF2ZUhvb2tzKHJvdXRlcywgcHJldlN0YXRlKSB7XG4gIGZvciAodmFyIGkgPSAwLCBsZW4gPSByb3V0ZXMubGVuZ3RoOyBpIDwgbGVuOyArK2kpIHtcbiAgICBpZiAocm91dGVzW2ldLm9uTGVhdmUpIHJvdXRlc1tpXS5vbkxlYXZlLmNhbGwocm91dGVzW2ldLCBwcmV2U3RhdGUpO1xuICB9XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfUm91dGVyQ29udGV4dCA9IHJlcXVpcmUoJy4vUm91dGVyQ29udGV4dCcpO1xuXG52YXIgX1JvdXRlckNvbnRleHQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUm91dGVyQ29udGV4dCk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGZ1bmN0aW9uICgpIHtcbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIG1pZGRsZXdhcmVzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgbWlkZGxld2FyZXNbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgIG1pZGRsZXdhcmVzLmZvckVhY2goZnVuY3Rpb24gKG1pZGRsZXdhcmUsIGluZGV4KSB7XG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShtaWRkbGV3YXJlLnJlbmRlclJvdXRlckNvbnRleHQgfHwgbWlkZGxld2FyZS5yZW5kZXJSb3V0ZUNvbXBvbmVudCwgJ1RoZSBtaWRkbGV3YXJlIHNwZWNpZmllZCBhdCBpbmRleCAnICsgaW5kZXggKyAnIGRvZXMgbm90IGFwcGVhciB0byBiZSAnICsgJ2EgdmFsaWQgUmVhY3QgUm91dGVyIG1pZGRsZXdhcmUuJykgOiB2b2lkIDA7XG4gICAgfSk7XG4gIH1cblxuICB2YXIgd2l0aENvbnRleHQgPSBtaWRkbGV3YXJlcy5tYXAoZnVuY3Rpb24gKG1pZGRsZXdhcmUpIHtcbiAgICByZXR1cm4gbWlkZGxld2FyZS5yZW5kZXJSb3V0ZXJDb250ZXh0O1xuICB9KS5maWx0ZXIoQm9vbGVhbik7XG4gIHZhciB3aXRoQ29tcG9uZW50ID0gbWlkZGxld2FyZXMubWFwKGZ1bmN0aW9uIChtaWRkbGV3YXJlKSB7XG4gICAgcmV0dXJuIG1pZGRsZXdhcmUucmVuZGVyUm91dGVDb21wb25lbnQ7XG4gIH0pLmZpbHRlcihCb29sZWFuKTtcblxuICB2YXIgbWFrZUNyZWF0ZUVsZW1lbnQgPSBmdW5jdGlvbiBtYWtlQ3JlYXRlRWxlbWVudCgpIHtcbiAgICB2YXIgYmFzZUNyZWF0ZUVsZW1lbnQgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyBfcmVhY3QuY3JlYXRlRWxlbWVudCA6IGFyZ3VtZW50c1swXTtcbiAgICByZXR1cm4gZnVuY3Rpb24gKENvbXBvbmVudCwgcHJvcHMpIHtcbiAgICAgIHJldHVybiB3aXRoQ29tcG9uZW50LnJlZHVjZVJpZ2h0KGZ1bmN0aW9uIChwcmV2aW91cywgcmVuZGVyUm91dGVDb21wb25lbnQpIHtcbiAgICAgICAgcmV0dXJuIHJlbmRlclJvdXRlQ29tcG9uZW50KHByZXZpb3VzLCBwcm9wcyk7XG4gICAgICB9LCBiYXNlQ3JlYXRlRWxlbWVudChDb21wb25lbnQsIHByb3BzKSk7XG4gICAgfTtcbiAgfTtcblxuICByZXR1cm4gZnVuY3Rpb24gKHJlbmRlclByb3BzKSB7XG4gICAgcmV0dXJuIHdpdGhDb250ZXh0LnJlZHVjZVJpZ2h0KGZ1bmN0aW9uIChwcmV2aW91cywgcmVuZGVyUm91dGVyQ29udGV4dCkge1xuICAgICAgcmV0dXJuIHJlbmRlclJvdXRlckNvbnRleHQocHJldmlvdXMsIHJlbmRlclByb3BzKTtcbiAgICB9LCBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudChfUm91dGVyQ29udGV4dDIuZGVmYXVsdCwgX2V4dGVuZHMoe30sIHJlbmRlclByb3BzLCB7XG4gICAgICBjcmVhdGVFbGVtZW50OiBtYWtlQ3JlYXRlRWxlbWVudChyZW5kZXJQcm9wcy5jcmVhdGVFbGVtZW50KVxuICAgIH0pKSk7XG4gIH07XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfY3JlYXRlQnJvd3Nlckhpc3RvcnkgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi9jcmVhdGVCcm93c2VySGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZUJyb3dzZXJIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZUJyb3dzZXJIaXN0b3J5KTtcblxudmFyIF9jcmVhdGVSb3V0ZXJIaXN0b3J5ID0gcmVxdWlyZSgnLi9jcmVhdGVSb3V0ZXJIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlUm91dGVySGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVSb3V0ZXJIaXN0b3J5KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZXhwb3J0cy5kZWZhdWx0ID0gKDAsIF9jcmVhdGVSb3V0ZXJIaXN0b3J5Mi5kZWZhdWx0KShfY3JlYXRlQnJvd3Nlckhpc3RvcnkyLmRlZmF1bHQpO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbmZ1bmN0aW9uIHJvdXRlUGFyYW1zQ2hhbmdlZChyb3V0ZSwgcHJldlN0YXRlLCBuZXh0U3RhdGUpIHtcbiAgaWYgKCFyb3V0ZS5wYXRoKSByZXR1cm4gZmFsc2U7XG5cbiAgdmFyIHBhcmFtTmFtZXMgPSAoMCwgX1BhdHRlcm5VdGlscy5nZXRQYXJhbU5hbWVzKShyb3V0ZS5wYXRoKTtcblxuICByZXR1cm4gcGFyYW1OYW1lcy5zb21lKGZ1bmN0aW9uIChwYXJhbU5hbWUpIHtcbiAgICByZXR1cm4gcHJldlN0YXRlLnBhcmFtc1twYXJhbU5hbWVdICE9PSBuZXh0U3RhdGUucGFyYW1zW3BhcmFtTmFtZV07XG4gIH0pO1xufVxuXG4vKipcbiAqIFJldHVybnMgYW4gb2JqZWN0IG9mIHsgbGVhdmVSb3V0ZXMsIGNoYW5nZVJvdXRlcywgZW50ZXJSb3V0ZXMgfSBkZXRlcm1pbmVkIGJ5XG4gKiB0aGUgY2hhbmdlIGZyb20gcHJldlN0YXRlIHRvIG5leHRTdGF0ZS4gV2UgbGVhdmUgcm91dGVzIGlmIGVpdGhlclxuICogMSkgdGhleSBhcmUgbm90IGluIHRoZSBuZXh0IHN0YXRlIG9yIDIpIHRoZXkgYXJlIGluIHRoZSBuZXh0IHN0YXRlXG4gKiBidXQgdGhlaXIgcGFyYW1zIGhhdmUgY2hhbmdlZCAoaS5lLiAvdXNlcnMvMTIzID0+IC91c2Vycy80NTYpLlxuICpcbiAqIGxlYXZlUm91dGVzIGFyZSBvcmRlcmVkIHN0YXJ0aW5nIGF0IHRoZSBsZWFmIHJvdXRlIG9mIHRoZSB0cmVlXG4gKiB3ZSdyZSBsZWF2aW5nIHVwIHRvIHRoZSBjb21tb24gcGFyZW50IHJvdXRlLiBlbnRlclJvdXRlcyBhcmUgb3JkZXJlZFxuICogZnJvbSB0aGUgdG9wIG9mIHRoZSB0cmVlIHdlJ3JlIGVudGVyaW5nIGRvd24gdG8gdGhlIGxlYWYgcm91dGUuXG4gKlxuICogY2hhbmdlUm91dGVzIGFyZSBhbnkgcm91dGVzIHRoYXQgZGlkbid0IGxlYXZlIG9yIGVudGVyIGR1cmluZ1xuICogdGhlIHRyYW5zaXRpb24uXG4gKi9cbmZ1bmN0aW9uIGNvbXB1dGVDaGFuZ2VkUm91dGVzKHByZXZTdGF0ZSwgbmV4dFN0YXRlKSB7XG4gIHZhciBwcmV2Um91dGVzID0gcHJldlN0YXRlICYmIHByZXZTdGF0ZS5yb3V0ZXM7XG4gIHZhciBuZXh0Um91dGVzID0gbmV4dFN0YXRlLnJvdXRlcztcblxuICB2YXIgbGVhdmVSb3V0ZXMgPSB2b2lkIDAsXG4gICAgICBjaGFuZ2VSb3V0ZXMgPSB2b2lkIDAsXG4gICAgICBlbnRlclJvdXRlcyA9IHZvaWQgMDtcbiAgaWYgKHByZXZSb3V0ZXMpIHtcbiAgICAoZnVuY3Rpb24gKCkge1xuICAgICAgdmFyIHBhcmVudElzTGVhdmluZyA9IGZhbHNlO1xuICAgICAgbGVhdmVSb3V0ZXMgPSBwcmV2Um91dGVzLmZpbHRlcihmdW5jdGlvbiAocm91dGUpIHtcbiAgICAgICAgaWYgKHBhcmVudElzTGVhdmluZykge1xuICAgICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHZhciBpc0xlYXZpbmcgPSBuZXh0Um91dGVzLmluZGV4T2Yocm91dGUpID09PSAtMSB8fCByb3V0ZVBhcmFtc0NoYW5nZWQocm91dGUsIHByZXZTdGF0ZSwgbmV4dFN0YXRlKTtcbiAgICAgICAgICBpZiAoaXNMZWF2aW5nKSBwYXJlbnRJc0xlYXZpbmcgPSB0cnVlO1xuICAgICAgICAgIHJldHVybiBpc0xlYXZpbmc7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuXG4gICAgICAvLyBvbkxlYXZlIGhvb2tzIHN0YXJ0IGF0IHRoZSBsZWFmIHJvdXRlLlxuICAgICAgbGVhdmVSb3V0ZXMucmV2ZXJzZSgpO1xuXG4gICAgICBlbnRlclJvdXRlcyA9IFtdO1xuICAgICAgY2hhbmdlUm91dGVzID0gW107XG5cbiAgICAgIG5leHRSb3V0ZXMuZm9yRWFjaChmdW5jdGlvbiAocm91dGUpIHtcbiAgICAgICAgdmFyIGlzTmV3ID0gcHJldlJvdXRlcy5pbmRleE9mKHJvdXRlKSA9PT0gLTE7XG4gICAgICAgIHZhciBwYXJhbXNDaGFuZ2VkID0gbGVhdmVSb3V0ZXMuaW5kZXhPZihyb3V0ZSkgIT09IC0xO1xuXG4gICAgICAgIGlmIChpc05ldyB8fCBwYXJhbXNDaGFuZ2VkKSBlbnRlclJvdXRlcy5wdXNoKHJvdXRlKTtlbHNlIGNoYW5nZVJvdXRlcy5wdXNoKHJvdXRlKTtcbiAgICAgIH0pO1xuICAgIH0pKCk7XG4gIH0gZWxzZSB7XG4gICAgbGVhdmVSb3V0ZXMgPSBbXTtcbiAgICBjaGFuZ2VSb3V0ZXMgPSBbXTtcbiAgICBlbnRlclJvdXRlcyA9IG5leHRSb3V0ZXM7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIGxlYXZlUm91dGVzOiBsZWF2ZVJvdXRlcyxcbiAgICBjaGFuZ2VSb3V0ZXM6IGNoYW5nZVJvdXRlcyxcbiAgICBlbnRlclJvdXRlczogZW50ZXJSb3V0ZXNcbiAgfTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gY29tcHV0ZUNoYW5nZWRSb3V0ZXM7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmRlZmF1bHQgPSBjcmVhdGVNZW1vcnlIaXN0b3J5O1xuXG52YXIgX3VzZVF1ZXJpZXMgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi91c2VRdWVyaWVzJyk7XG5cbnZhciBfdXNlUXVlcmllczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF91c2VRdWVyaWVzKTtcblxudmFyIF91c2VCYXNlbmFtZSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL3VzZUJhc2VuYW1lJyk7XG5cbnZhciBfdXNlQmFzZW5hbWUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlQmFzZW5hbWUpO1xuXG52YXIgX2NyZWF0ZU1lbW9yeUhpc3RvcnkgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi9jcmVhdGVNZW1vcnlIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlTWVtb3J5SGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVNZW1vcnlIaXN0b3J5KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gY3JlYXRlTWVtb3J5SGlzdG9yeShvcHRpb25zKSB7XG4gIC8vIHNpZ25hdHVyZXMgYW5kIHR5cGUgY2hlY2tpbmcgZGlmZmVyIGJldHdlZW4gYHVzZVJvdXRlc2AgYW5kXG4gIC8vIGBjcmVhdGVNZW1vcnlIaXN0b3J5YCwgaGF2ZSB0byBjcmVhdGUgYG1lbW9yeUhpc3RvcnlgIGZpcnN0IGJlY2F1c2VcbiAgLy8gYHVzZVF1ZXJpZXNgIGRvZXNuJ3QgdW5kZXJzdGFuZCB0aGUgc2lnbmF0dXJlXG4gIHZhciBtZW1vcnlIaXN0b3J5ID0gKDAsIF9jcmVhdGVNZW1vcnlIaXN0b3J5Mi5kZWZhdWx0KShvcHRpb25zKTtcbiAgdmFyIGNyZWF0ZUhpc3RvcnkgPSBmdW5jdGlvbiBjcmVhdGVIaXN0b3J5KCkge1xuICAgIHJldHVybiBtZW1vcnlIaXN0b3J5O1xuICB9O1xuICB2YXIgaGlzdG9yeSA9ICgwLCBfdXNlUXVlcmllczIuZGVmYXVsdCkoKDAsIF91c2VCYXNlbmFtZTIuZGVmYXVsdCkoY3JlYXRlSGlzdG9yeSkpKG9wdGlvbnMpO1xuICBoaXN0b3J5Ll9fdjJfY29tcGF0aWJsZV9fID0gdHJ1ZTtcbiAgcmV0dXJuIGhpc3Rvcnk7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGZ1bmN0aW9uIChjcmVhdGVIaXN0b3J5KSB7XG4gIHZhciBoaXN0b3J5ID0gdm9pZCAwO1xuICBpZiAoY2FuVXNlRE9NKSBoaXN0b3J5ID0gKDAsIF91c2VSb3V0ZXJIaXN0b3J5Mi5kZWZhdWx0KShjcmVhdGVIaXN0b3J5KSgpO1xuICByZXR1cm4gaGlzdG9yeTtcbn07XG5cbnZhciBfdXNlUm91dGVySGlzdG9yeSA9IHJlcXVpcmUoJy4vdXNlUm91dGVySGlzdG9yeScpO1xuXG52YXIgX3VzZVJvdXRlckhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUm91dGVySGlzdG9yeSk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBjYW5Vc2VET00gPSAhISh0eXBlb2Ygd2luZG93ICE9PSAndW5kZWZpbmVkJyAmJiB3aW5kb3cuZG9jdW1lbnQgJiYgd2luZG93LmRvY3VtZW50LmNyZWF0ZUVsZW1lbnQpO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGNyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX2NvbXB1dGVDaGFuZ2VkUm91dGVzMiA9IHJlcXVpcmUoJy4vY29tcHV0ZUNoYW5nZWRSb3V0ZXMnKTtcblxudmFyIF9jb21wdXRlQ2hhbmdlZFJvdXRlczMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jb21wdXRlQ2hhbmdlZFJvdXRlczIpO1xuXG52YXIgX1RyYW5zaXRpb25VdGlscyA9IHJlcXVpcmUoJy4vVHJhbnNpdGlvblV0aWxzJyk7XG5cbnZhciBfaXNBY3RpdmUyID0gcmVxdWlyZSgnLi9pc0FjdGl2ZScpO1xuXG52YXIgX2lzQWN0aXZlMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2lzQWN0aXZlMik7XG5cbnZhciBfZ2V0Q29tcG9uZW50cyA9IHJlcXVpcmUoJy4vZ2V0Q29tcG9uZW50cycpO1xuXG52YXIgX2dldENvbXBvbmVudHMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZ2V0Q29tcG9uZW50cyk7XG5cbnZhciBfbWF0Y2hSb3V0ZXMgPSByZXF1aXJlKCcuL21hdGNoUm91dGVzJyk7XG5cbnZhciBfbWF0Y2hSb3V0ZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfbWF0Y2hSb3V0ZXMpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBoYXNBbnlQcm9wZXJ0aWVzKG9iamVjdCkge1xuICBmb3IgKHZhciBwIGluIG9iamVjdCkge1xuICAgIGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqZWN0LCBwKSkgcmV0dXJuIHRydWU7XG4gIH1yZXR1cm4gZmFsc2U7XG59XG5cbmZ1bmN0aW9uIGNyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyKGhpc3RvcnksIHJvdXRlcykge1xuICB2YXIgc3RhdGUgPSB7fTtcblxuICAvLyBTaWduYXR1cmUgc2hvdWxkIGJlIChsb2NhdGlvbiwgaW5kZXhPbmx5KSwgYnV0IG5lZWRzIHRvIHN1cHBvcnQgKHBhdGgsXG4gIC8vIHF1ZXJ5LCBpbmRleE9ubHkpXG4gIGZ1bmN0aW9uIGlzQWN0aXZlKGxvY2F0aW9uKSB7XG4gICAgdmFyIGluZGV4T25seU9yRGVwcmVjYXRlZFF1ZXJ5ID0gYXJndW1lbnRzLmxlbmd0aCA8PSAxIHx8IGFyZ3VtZW50c1sxXSA9PT0gdW5kZWZpbmVkID8gZmFsc2UgOiBhcmd1bWVudHNbMV07XG4gICAgdmFyIGRlcHJlY2F0ZWRJbmRleE9ubHkgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDIgfHwgYXJndW1lbnRzWzJdID09PSB1bmRlZmluZWQgPyBudWxsIDogYXJndW1lbnRzWzJdO1xuXG4gICAgdmFyIGluZGV4T25seSA9IHZvaWQgMDtcbiAgICBpZiAoaW5kZXhPbmx5T3JEZXByZWNhdGVkUXVlcnkgJiYgaW5kZXhPbmx5T3JEZXByZWNhdGVkUXVlcnkgIT09IHRydWUgfHwgZGVwcmVjYXRlZEluZGV4T25seSAhPT0gbnVsbCkge1xuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdgaXNBY3RpdmUocGF0aG5hbWUsIHF1ZXJ5LCBpbmRleE9ubHkpIGlzIGRlcHJlY2F0ZWQ7IHVzZSBgaXNBY3RpdmUobG9jYXRpb24sIGluZGV4T25seSlgIHdpdGggYSBsb2NhdGlvbiBkZXNjcmlwdG9yIGluc3RlYWQuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1pc0FjdGl2ZWRlcHJlY2F0ZWQnKSA6IHZvaWQgMDtcbiAgICAgIGxvY2F0aW9uID0geyBwYXRobmFtZTogbG9jYXRpb24sIHF1ZXJ5OiBpbmRleE9ubHlPckRlcHJlY2F0ZWRRdWVyeSB9O1xuICAgICAgaW5kZXhPbmx5ID0gZGVwcmVjYXRlZEluZGV4T25seSB8fCBmYWxzZTtcbiAgICB9IGVsc2Uge1xuICAgICAgbG9jYXRpb24gPSBoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uKGxvY2F0aW9uKTtcbiAgICAgIGluZGV4T25seSA9IGluZGV4T25seU9yRGVwcmVjYXRlZFF1ZXJ5O1xuICAgIH1cblxuICAgIHJldHVybiAoMCwgX2lzQWN0aXZlMy5kZWZhdWx0KShsb2NhdGlvbiwgaW5kZXhPbmx5LCBzdGF0ZS5sb2NhdGlvbiwgc3RhdGUucm91dGVzLCBzdGF0ZS5wYXJhbXMpO1xuICB9XG5cbiAgdmFyIHBhcnRpYWxOZXh0U3RhdGUgPSB2b2lkIDA7XG5cbiAgZnVuY3Rpb24gbWF0Y2gobG9jYXRpb24sIGNhbGxiYWNrKSB7XG4gICAgaWYgKHBhcnRpYWxOZXh0U3RhdGUgJiYgcGFydGlhbE5leHRTdGF0ZS5sb2NhdGlvbiA9PT0gbG9jYXRpb24pIHtcbiAgICAgIC8vIENvbnRpbnVlIGZyb20gd2hlcmUgd2UgbGVmdCBvZmYuXG4gICAgICBmaW5pc2hNYXRjaChwYXJ0aWFsTmV4dFN0YXRlLCBjYWxsYmFjayk7XG4gICAgfSBlbHNlIHtcbiAgICAgICgwLCBfbWF0Y2hSb3V0ZXMyLmRlZmF1bHQpKHJvdXRlcywgbG9jYXRpb24sIGZ1bmN0aW9uIChlcnJvciwgbmV4dFN0YXRlKSB7XG4gICAgICAgIGlmIChlcnJvcikge1xuICAgICAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICAgICAgfSBlbHNlIGlmIChuZXh0U3RhdGUpIHtcbiAgICAgICAgICBmaW5pc2hNYXRjaChfZXh0ZW5kcyh7fSwgbmV4dFN0YXRlLCB7IGxvY2F0aW9uOiBsb2NhdGlvbiB9KSwgY2FsbGJhY2spO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIGNhbGxiYWNrKCk7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIGZ1bmN0aW9uIGZpbmlzaE1hdGNoKG5leHRTdGF0ZSwgY2FsbGJhY2spIHtcbiAgICB2YXIgX2NvbXB1dGVDaGFuZ2VkUm91dGVzID0gKDAsIF9jb21wdXRlQ2hhbmdlZFJvdXRlczMuZGVmYXVsdCkoc3RhdGUsIG5leHRTdGF0ZSk7XG5cbiAgICB2YXIgbGVhdmVSb3V0ZXMgPSBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMubGVhdmVSb3V0ZXM7XG4gICAgdmFyIGNoYW5nZVJvdXRlcyA9IF9jb21wdXRlQ2hhbmdlZFJvdXRlcy5jaGFuZ2VSb3V0ZXM7XG4gICAgdmFyIGVudGVyUm91dGVzID0gX2NvbXB1dGVDaGFuZ2VkUm91dGVzLmVudGVyUm91dGVzO1xuXG5cbiAgICAoMCwgX1RyYW5zaXRpb25VdGlscy5ydW5MZWF2ZUhvb2tzKShsZWF2ZVJvdXRlcywgc3RhdGUpO1xuXG4gICAgLy8gVGVhciBkb3duIGNvbmZpcm1hdGlvbiBob29rcyBmb3IgbGVmdCByb3V0ZXNcbiAgICBsZWF2ZVJvdXRlcy5maWx0ZXIoZnVuY3Rpb24gKHJvdXRlKSB7XG4gICAgICByZXR1cm4gZW50ZXJSb3V0ZXMuaW5kZXhPZihyb3V0ZSkgPT09IC0xO1xuICAgIH0pLmZvckVhY2gocmVtb3ZlTGlzdGVuQmVmb3JlSG9va3NGb3JSb3V0ZSk7XG5cbiAgICAvLyBjaGFuZ2UgYW5kIGVudGVyIGhvb2tzIGFyZSBydW4gaW4gc2VyaWVzXG4gICAgKDAsIF9UcmFuc2l0aW9uVXRpbHMucnVuQ2hhbmdlSG9va3MpKGNoYW5nZVJvdXRlcywgc3RhdGUsIG5leHRTdGF0ZSwgZnVuY3Rpb24gKGVycm9yLCByZWRpcmVjdEluZm8pIHtcbiAgICAgIGlmIChlcnJvciB8fCByZWRpcmVjdEluZm8pIHJldHVybiBoYW5kbGVFcnJvck9yUmVkaXJlY3QoZXJyb3IsIHJlZGlyZWN0SW5mbyk7XG5cbiAgICAgICgwLCBfVHJhbnNpdGlvblV0aWxzLnJ1bkVudGVySG9va3MpKGVudGVyUm91dGVzLCBuZXh0U3RhdGUsIGZpbmlzaEVudGVySG9va3MpO1xuICAgIH0pO1xuXG4gICAgZnVuY3Rpb24gZmluaXNoRW50ZXJIb29rcyhlcnJvciwgcmVkaXJlY3RJbmZvKSB7XG4gICAgICBpZiAoZXJyb3IgfHwgcmVkaXJlY3RJbmZvKSByZXR1cm4gaGFuZGxlRXJyb3JPclJlZGlyZWN0KGVycm9yLCByZWRpcmVjdEluZm8pO1xuXG4gICAgICAvLyBUT0RPOiBGZXRjaCBjb21wb25lbnRzIGFmdGVyIHN0YXRlIGlzIHVwZGF0ZWQuXG4gICAgICAoMCwgX2dldENvbXBvbmVudHMyLmRlZmF1bHQpKG5leHRTdGF0ZSwgZnVuY3Rpb24gKGVycm9yLCBjb21wb25lbnRzKSB7XG4gICAgICAgIGlmIChlcnJvcikge1xuICAgICAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAvLyBUT0RPOiBNYWtlIG1hdGNoIGEgcHVyZSBmdW5jdGlvbiBhbmQgaGF2ZSBzb21lIG90aGVyIEFQSVxuICAgICAgICAgIC8vIGZvciBcIm1hdGNoIGFuZCB1cGRhdGUgc3RhdGVcIi5cbiAgICAgICAgICBjYWxsYmFjayhudWxsLCBudWxsLCBzdGF0ZSA9IF9leHRlbmRzKHt9LCBuZXh0U3RhdGUsIHsgY29tcG9uZW50czogY29tcG9uZW50cyB9KSk7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGhhbmRsZUVycm9yT3JSZWRpcmVjdChlcnJvciwgcmVkaXJlY3RJbmZvKSB7XG4gICAgICBpZiAoZXJyb3IpIGNhbGxiYWNrKGVycm9yKTtlbHNlIGNhbGxiYWNrKG51bGwsIHJlZGlyZWN0SW5mbyk7XG4gICAgfVxuICB9XG5cbiAgdmFyIFJvdXRlR3VpZCA9IDE7XG5cbiAgZnVuY3Rpb24gZ2V0Um91dGVJRChyb3V0ZSkge1xuICAgIHZhciBjcmVhdGUgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDEgfHwgYXJndW1lbnRzWzFdID09PSB1bmRlZmluZWQgPyB0cnVlIDogYXJndW1lbnRzWzFdO1xuXG4gICAgcmV0dXJuIHJvdXRlLl9faWRfXyB8fCBjcmVhdGUgJiYgKHJvdXRlLl9faWRfXyA9IFJvdXRlR3VpZCsrKTtcbiAgfVxuXG4gIHZhciBSb3V0ZUhvb2tzID0gT2JqZWN0LmNyZWF0ZShudWxsKTtcblxuICBmdW5jdGlvbiBnZXRSb3V0ZUhvb2tzRm9yUm91dGVzKHJvdXRlcykge1xuICAgIHJldHVybiByb3V0ZXMucmVkdWNlKGZ1bmN0aW9uIChob29rcywgcm91dGUpIHtcbiAgICAgIGhvb2tzLnB1c2guYXBwbHkoaG9va3MsIFJvdXRlSG9va3NbZ2V0Um91dGVJRChyb3V0ZSldKTtcbiAgICAgIHJldHVybiBob29rcztcbiAgICB9LCBbXSk7XG4gIH1cblxuICBmdW5jdGlvbiB0cmFuc2l0aW9uSG9vayhsb2NhdGlvbiwgY2FsbGJhY2spIHtcbiAgICAoMCwgX21hdGNoUm91dGVzMi5kZWZhdWx0KShyb3V0ZXMsIGxvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIG5leHRTdGF0ZSkge1xuICAgICAgaWYgKG5leHRTdGF0ZSA9PSBudWxsKSB7XG4gICAgICAgIC8vIFRPRE86IFdlIGRpZG4ndCBhY3R1YWxseSBtYXRjaCBhbnl0aGluZywgYnV0IGhhbmdcbiAgICAgICAgLy8gb250byBlcnJvci9uZXh0U3RhdGUgc28gd2UgZG9uJ3QgaGF2ZSB0byBtYXRjaFJvdXRlc1xuICAgICAgICAvLyBhZ2FpbiBpbiB0aGUgbGlzdGVuIGNhbGxiYWNrLlxuICAgICAgICBjYWxsYmFjaygpO1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG5cbiAgICAgIC8vIENhY2hlIHNvbWUgc3RhdGUgaGVyZSBzbyB3ZSBkb24ndCBoYXZlIHRvXG4gICAgICAvLyBtYXRjaFJvdXRlcygpIGFnYWluIGluIHRoZSBsaXN0ZW4gY2FsbGJhY2suXG4gICAgICBwYXJ0aWFsTmV4dFN0YXRlID0gX2V4dGVuZHMoe30sIG5leHRTdGF0ZSwgeyBsb2NhdGlvbjogbG9jYXRpb24gfSk7XG5cbiAgICAgIHZhciBob29rcyA9IGdldFJvdXRlSG9va3NGb3JSb3V0ZXMoKDAsIF9jb21wdXRlQ2hhbmdlZFJvdXRlczMuZGVmYXVsdCkoc3RhdGUsIHBhcnRpYWxOZXh0U3RhdGUpLmxlYXZlUm91dGVzKTtcblxuICAgICAgdmFyIHJlc3VsdCA9IHZvaWQgMDtcbiAgICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBob29rcy5sZW5ndGg7IHJlc3VsdCA9PSBudWxsICYmIGkgPCBsZW47ICsraSkge1xuICAgICAgICAvLyBQYXNzaW5nIHRoZSBsb2NhdGlvbiBhcmcgaGVyZSBpbmRpY2F0ZXMgdG9cbiAgICAgICAgLy8gdGhlIHVzZXIgdGhhdCB0aGlzIGlzIGEgdHJhbnNpdGlvbiBob29rLlxuICAgICAgICByZXN1bHQgPSBob29rc1tpXShsb2NhdGlvbik7XG4gICAgICB9XG5cbiAgICAgIGNhbGxiYWNrKHJlc3VsdCk7XG4gICAgfSk7XG4gIH1cblxuICAvKiBpc3RhbmJ1bCBpZ25vcmUgbmV4dDogdW50ZXN0YWJsZSB3aXRoIEthcm1hICovXG4gIGZ1bmN0aW9uIGJlZm9yZVVubG9hZEhvb2soKSB7XG4gICAgLy8gU3luY2hyb25vdXNseSBjaGVjayB0byBzZWUgaWYgYW55IHJvdXRlIGhvb2tzIHdhbnRcbiAgICAvLyB0byBwcmV2ZW50IHRoZSBjdXJyZW50IHdpbmRvdy90YWIgZnJvbSBjbG9zaW5nLlxuICAgIGlmIChzdGF0ZS5yb3V0ZXMpIHtcbiAgICAgIHZhciBob29rcyA9IGdldFJvdXRlSG9va3NGb3JSb3V0ZXMoc3RhdGUucm91dGVzKTtcblxuICAgICAgdmFyIG1lc3NhZ2UgPSB2b2lkIDA7XG4gICAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gaG9va3MubGVuZ3RoOyB0eXBlb2YgbWVzc2FnZSAhPT0gJ3N0cmluZycgJiYgaSA8IGxlbjsgKytpKSB7XG4gICAgICAgIC8vIFBhc3Npbmcgbm8gYXJncyBpbmRpY2F0ZXMgdG8gdGhlIHVzZXIgdGhhdCB0aGlzIGlzIGFcbiAgICAgICAgLy8gYmVmb3JldW5sb2FkIGhvb2suIFdlIGRvbid0IGtub3cgdGhlIG5leHQgbG9jYXRpb24uXG4gICAgICAgIG1lc3NhZ2UgPSBob29rc1tpXSgpO1xuICAgICAgfVxuXG4gICAgICByZXR1cm4gbWVzc2FnZTtcbiAgICB9XG4gIH1cblxuICB2YXIgdW5saXN0ZW5CZWZvcmUgPSB2b2lkIDAsXG4gICAgICB1bmxpc3RlbkJlZm9yZVVubG9hZCA9IHZvaWQgMDtcblxuICBmdW5jdGlvbiByZW1vdmVMaXN0ZW5CZWZvcmVIb29rc0ZvclJvdXRlKHJvdXRlKSB7XG4gICAgdmFyIHJvdXRlSUQgPSBnZXRSb3V0ZUlEKHJvdXRlLCBmYWxzZSk7XG4gICAgaWYgKCFyb3V0ZUlEKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgZGVsZXRlIFJvdXRlSG9va3Nbcm91dGVJRF07XG5cbiAgICBpZiAoIWhhc0FueVByb3BlcnRpZXMoUm91dGVIb29rcykpIHtcbiAgICAgIC8vIHRlYXJkb3duIHRyYW5zaXRpb24gJiBiZWZvcmV1bmxvYWQgaG9va3NcbiAgICAgIGlmICh1bmxpc3RlbkJlZm9yZSkge1xuICAgICAgICB1bmxpc3RlbkJlZm9yZSgpO1xuICAgICAgICB1bmxpc3RlbkJlZm9yZSA9IG51bGw7XG4gICAgICB9XG5cbiAgICAgIGlmICh1bmxpc3RlbkJlZm9yZVVubG9hZCkge1xuICAgICAgICB1bmxpc3RlbkJlZm9yZVVubG9hZCgpO1xuICAgICAgICB1bmxpc3RlbkJlZm9yZVVubG9hZCA9IG51bGw7XG4gICAgICB9XG4gICAgfVxuICB9XG5cbiAgLyoqXG4gICAqIFJlZ2lzdGVycyB0aGUgZ2l2ZW4gaG9vayBmdW5jdGlvbiB0byBydW4gYmVmb3JlIGxlYXZpbmcgdGhlIGdpdmVuIHJvdXRlLlxuICAgKlxuICAgKiBEdXJpbmcgYSBub3JtYWwgdHJhbnNpdGlvbiwgdGhlIGhvb2sgZnVuY3Rpb24gcmVjZWl2ZXMgdGhlIG5leHQgbG9jYXRpb25cbiAgICogYXMgaXRzIG9ubHkgYXJndW1lbnQgYW5kIGNhbiByZXR1cm4gZWl0aGVyIGEgcHJvbXB0IG1lc3NhZ2UgKHN0cmluZykgdG8gc2hvdyB0aGUgdXNlcixcbiAgICogdG8gbWFrZSBzdXJlIHRoZXkgd2FudCB0byBsZWF2ZSB0aGUgcGFnZTsgb3IgYGZhbHNlYCwgdG8gcHJldmVudCB0aGUgdHJhbnNpdGlvbi5cbiAgICogQW55IG90aGVyIHJldHVybiB2YWx1ZSB3aWxsIGhhdmUgbm8gZWZmZWN0LlxuICAgKlxuICAgKiBEdXJpbmcgdGhlIGJlZm9yZXVubG9hZCBldmVudCAoaW4gYnJvd3NlcnMpIHRoZSBob29rIHJlY2VpdmVzIG5vIGFyZ3VtZW50cy5cbiAgICogSW4gdGhpcyBjYXNlIGl0IG11c3QgcmV0dXJuIGEgcHJvbXB0IG1lc3NhZ2UgdG8gcHJldmVudCB0aGUgdHJhbnNpdGlvbi5cbiAgICpcbiAgICogUmV0dXJucyBhIGZ1bmN0aW9uIHRoYXQgbWF5IGJlIHVzZWQgdG8gdW5iaW5kIHRoZSBsaXN0ZW5lci5cbiAgICovXG4gIGZ1bmN0aW9uIGxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZShyb3V0ZSwgaG9vaykge1xuICAgIC8vIFRPRE86IFdhcm4gaWYgdGhleSByZWdpc3RlciBmb3IgYSByb3V0ZSB0aGF0IGlzbid0IGN1cnJlbnRseVxuICAgIC8vIGFjdGl2ZS4gVGhleSdyZSBwcm9iYWJseSBkb2luZyBzb21ldGhpbmcgd3JvbmcsIGxpa2UgcmUtY3JlYXRpbmdcbiAgICAvLyByb3V0ZSBvYmplY3RzIG9uIGV2ZXJ5IGxvY2F0aW9uIGNoYW5nZS5cbiAgICB2YXIgcm91dGVJRCA9IGdldFJvdXRlSUQocm91dGUpO1xuICAgIHZhciBob29rcyA9IFJvdXRlSG9va3Nbcm91dGVJRF07XG5cbiAgICBpZiAoIWhvb2tzKSB7XG4gICAgICB2YXIgdGhlcmVXZXJlTm9Sb3V0ZUhvb2tzID0gIWhhc0FueVByb3BlcnRpZXMoUm91dGVIb29rcyk7XG5cbiAgICAgIFJvdXRlSG9va3Nbcm91dGVJRF0gPSBbaG9va107XG5cbiAgICAgIGlmICh0aGVyZVdlcmVOb1JvdXRlSG9va3MpIHtcbiAgICAgICAgLy8gc2V0dXAgdHJhbnNpdGlvbiAmIGJlZm9yZXVubG9hZCBob29rc1xuICAgICAgICB1bmxpc3RlbkJlZm9yZSA9IGhpc3RvcnkubGlzdGVuQmVmb3JlKHRyYW5zaXRpb25Ib29rKTtcblxuICAgICAgICBpZiAoaGlzdG9yeS5saXN0ZW5CZWZvcmVVbmxvYWQpIHVubGlzdGVuQmVmb3JlVW5sb2FkID0gaGlzdG9yeS5saXN0ZW5CZWZvcmVVbmxvYWQoYmVmb3JlVW5sb2FkSG9vayk7XG4gICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgIGlmIChob29rcy5pbmRleE9mKGhvb2spID09PSAtMSkge1xuICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2FkZGluZyBtdWx0aXBsZSBsZWF2ZSBob29rcyBmb3IgdGhlIHNhbWUgcm91dGUgaXMgZGVwcmVjYXRlZDsgbWFuYWdlIG11bHRpcGxlIGNvbmZpcm1hdGlvbnMgaW4geW91ciBvd24gY29kZSBpbnN0ZWFkJykgOiB2b2lkIDA7XG5cbiAgICAgICAgaG9va3MucHVzaChob29rKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgdmFyIGhvb2tzID0gUm91dGVIb29rc1tyb3V0ZUlEXTtcblxuICAgICAgaWYgKGhvb2tzKSB7XG4gICAgICAgIHZhciBuZXdIb29rcyA9IGhvb2tzLmZpbHRlcihmdW5jdGlvbiAoaXRlbSkge1xuICAgICAgICAgIHJldHVybiBpdGVtICE9PSBob29rO1xuICAgICAgICB9KTtcblxuICAgICAgICBpZiAobmV3SG9va3MubGVuZ3RoID09PSAwKSB7XG4gICAgICAgICAgcmVtb3ZlTGlzdGVuQmVmb3JlSG9va3NGb3JSb3V0ZShyb3V0ZSk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgUm91dGVIb29rc1tyb3V0ZUlEXSA9IG5ld0hvb2tzO1xuICAgICAgICB9XG4gICAgICB9XG4gICAgfTtcbiAgfVxuXG4gIC8qKlxuICAgKiBUaGlzIGlzIHRoZSBBUEkgZm9yIHN0YXRlZnVsIGVudmlyb25tZW50cy4gQXMgdGhlIGxvY2F0aW9uXG4gICAqIGNoYW5nZXMsIHdlIHVwZGF0ZSBzdGF0ZSBhbmQgY2FsbCB0aGUgbGlzdGVuZXIuIFdlIGNhbiBhbHNvXG4gICAqIGdyYWNlZnVsbHkgaGFuZGxlIGVycm9ycyBhbmQgcmVkaXJlY3RzLlxuICAgKi9cbiAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgLy8gVE9ETzogT25seSB1c2UgYSBzaW5nbGUgaGlzdG9yeSBsaXN0ZW5lci4gT3RoZXJ3aXNlIHdlJ2xsXG4gICAgLy8gZW5kIHVwIHdpdGggbXVsdGlwbGUgY29uY3VycmVudCBjYWxscyB0byBtYXRjaC5cbiAgICByZXR1cm4gaGlzdG9yeS5saXN0ZW4oZnVuY3Rpb24gKGxvY2F0aW9uKSB7XG4gICAgICBpZiAoc3RhdGUubG9jYXRpb24gPT09IGxvY2F0aW9uKSB7XG4gICAgICAgIGxpc3RlbmVyKG51bGwsIHN0YXRlKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIG1hdGNoKGxvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIHJlZGlyZWN0TG9jYXRpb24sIG5leHRTdGF0ZSkge1xuICAgICAgICAgIGlmIChlcnJvcikge1xuICAgICAgICAgICAgbGlzdGVuZXIoZXJyb3IpO1xuICAgICAgICAgIH0gZWxzZSBpZiAocmVkaXJlY3RMb2NhdGlvbikge1xuICAgICAgICAgICAgaGlzdG9yeS5yZXBsYWNlKHJlZGlyZWN0TG9jYXRpb24pO1xuICAgICAgICAgIH0gZWxzZSBpZiAobmV4dFN0YXRlKSB7XG4gICAgICAgICAgICBsaXN0ZW5lcihudWxsLCBuZXh0U3RhdGUpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ0xvY2F0aW9uIFwiJXNcIiBkaWQgbm90IG1hdGNoIGFueSByb3V0ZXMnLCBsb2NhdGlvbi5wYXRobmFtZSArIGxvY2F0aW9uLnNlYXJjaCArIGxvY2F0aW9uLmhhc2gpIDogdm9pZCAwO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9XG4gICAgfSk7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIGlzQWN0aXZlOiBpc0FjdGl2ZSxcbiAgICBtYXRjaDogbWF0Y2gsXG4gICAgbGlzdGVuQmVmb3JlTGVhdmluZ1JvdXRlOiBsaXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUsXG4gICAgbGlzdGVuOiBsaXN0ZW5cbiAgfTtcbn1cblxuLy9leHBvcnQgZGVmYXVsdCB1c2VSb3V0ZXNcblxubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5jYW5Vc2VNZW1icmFuZSA9IHVuZGVmaW5lZDtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIGNhblVzZU1lbWJyYW5lID0gZXhwb3J0cy5jYW5Vc2VNZW1icmFuZSA9IGZhbHNlO1xuXG4vLyBOby1vcCBieSBkZWZhdWx0LlxudmFyIGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMgPSBmdW5jdGlvbiBkZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzKG9iamVjdCkge1xuICByZXR1cm4gb2JqZWN0O1xufTtcblxuaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgdHJ5IHtcbiAgICBpZiAoT2JqZWN0LmRlZmluZVByb3BlcnR5KHt9LCAneCcsIHtcbiAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICByZXR1cm4gdHJ1ZTtcbiAgICAgIH1cbiAgICB9KS54KSB7XG4gICAgICBleHBvcnRzLmNhblVzZU1lbWJyYW5lID0gY2FuVXNlTWVtYnJhbmUgPSB0cnVlO1xuICAgIH1cbiAgICAvKiBlc2xpbnQtZGlzYWJsZSBuby1lbXB0eSAqL1xuICB9IGNhdGNoIChlKSB7fVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWVtcHR5ICovXG5cbiAgaWYgKGNhblVzZU1lbWJyYW5lKSB7XG4gICAgZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IGZ1bmN0aW9uIGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMob2JqZWN0LCBtZXNzYWdlKSB7XG4gICAgICAvLyBXcmFwIHRoZSBkZXByZWNhdGVkIG9iamVjdCBpbiBhIG1lbWJyYW5lIHRvIHdhcm4gb24gcHJvcGVydHkgYWNjZXNzLlxuICAgICAgdmFyIG1lbWJyYW5lID0ge307XG5cbiAgICAgIHZhciBfbG9vcCA9IGZ1bmN0aW9uIF9sb29wKHByb3ApIHtcbiAgICAgICAgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqZWN0LCBwcm9wKSkge1xuICAgICAgICAgIHJldHVybiAnY29udGludWUnO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKHR5cGVvZiBvYmplY3RbcHJvcF0gPT09ICdmdW5jdGlvbicpIHtcbiAgICAgICAgICAvLyBDYW4ndCB1c2UgZmF0IGFycm93IGhlcmUgYmVjYXVzZSBvZiB1c2Ugb2YgYXJndW1lbnRzIGJlbG93LlxuICAgICAgICAgIG1lbWJyYW5lW3Byb3BdID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsIG1lc3NhZ2UpIDogdm9pZCAwO1xuICAgICAgICAgICAgcmV0dXJuIG9iamVjdFtwcm9wXS5hcHBseShvYmplY3QsIGFyZ3VtZW50cyk7XG4gICAgICAgICAgfTtcbiAgICAgICAgICByZXR1cm4gJ2NvbnRpbnVlJztcbiAgICAgICAgfVxuXG4gICAgICAgIC8vIFRoZXNlIHByb3BlcnRpZXMgYXJlIG5vbi1lbnVtZXJhYmxlIHRvIHByZXZlbnQgUmVhY3QgZGV2IHRvb2xzIGZyb21cbiAgICAgICAgLy8gc2VlaW5nIHRoZW0gYW5kIGNhdXNpbmcgc3B1cmlvdXMgd2FybmluZ3Mgd2hlbiBhY2Nlc3NpbmcgdGhlbS4gSW5cbiAgICAgICAgLy8gcHJpbmNpcGxlIHRoaXMgY291bGQgYmUgZG9uZSB3aXRoIGEgcHJveHksIGJ1dCBzdXBwb3J0IGZvciB0aGVcbiAgICAgICAgLy8gb3duS2V5cyB0cmFwIG9uIHByb3hpZXMgaXMgbm90IHVuaXZlcnNhbCwgZXZlbiBhbW9uZyBicm93c2VycyB0aGF0XG4gICAgICAgIC8vIG90aGVyd2lzZSBzdXBwb3J0IHByb3hpZXMuXG4gICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eShtZW1icmFuZSwgcHJvcCwge1xuICAgICAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsIG1lc3NhZ2UpIDogdm9pZCAwO1xuICAgICAgICAgICAgcmV0dXJuIG9iamVjdFtwcm9wXTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuICAgICAgfTtcblxuICAgICAgZm9yICh2YXIgcHJvcCBpbiBvYmplY3QpIHtcbiAgICAgICAgdmFyIF9yZXQgPSBfbG9vcChwcm9wKTtcblxuICAgICAgICBpZiAoX3JldCA9PT0gJ2NvbnRpbnVlJykgY29udGludWU7XG4gICAgICB9XG5cbiAgICAgIHJldHVybiBtZW1icmFuZTtcbiAgICB9O1xuICB9XG59XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXM7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX0FzeW5jVXRpbHMgPSByZXF1aXJlKCcuL0FzeW5jVXRpbHMnKTtcblxudmFyIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24gPSByZXF1aXJlKCcuL21ha2VTdGF0ZVdpdGhMb2NhdGlvbicpO1xuXG52YXIgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9tYWtlU3RhdGVXaXRoTG9jYXRpb24pO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBnZXRDb21wb25lbnRzRm9yUm91dGUobmV4dFN0YXRlLCByb3V0ZSwgY2FsbGJhY2spIHtcbiAgaWYgKHJvdXRlLmNvbXBvbmVudCB8fCByb3V0ZS5jb21wb25lbnRzKSB7XG4gICAgY2FsbGJhY2sobnVsbCwgcm91dGUuY29tcG9uZW50IHx8IHJvdXRlLmNvbXBvbmVudHMpO1xuICAgIHJldHVybjtcbiAgfVxuXG4gIHZhciBnZXRDb21wb25lbnQgPSByb3V0ZS5nZXRDb21wb25lbnQgfHwgcm91dGUuZ2V0Q29tcG9uZW50cztcbiAgaWYgKCFnZXRDb21wb25lbnQpIHtcbiAgICBjYWxsYmFjaygpO1xuICAgIHJldHVybjtcbiAgfVxuXG4gIHZhciBsb2NhdGlvbiA9IG5leHRTdGF0ZS5sb2NhdGlvbjtcblxuICB2YXIgbmV4dFN0YXRlV2l0aExvY2F0aW9uID0gKDAsIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24yLmRlZmF1bHQpKG5leHRTdGF0ZSwgbG9jYXRpb24pO1xuXG4gIGdldENvbXBvbmVudC5jYWxsKHJvdXRlLCBuZXh0U3RhdGVXaXRoTG9jYXRpb24sIGNhbGxiYWNrKTtcbn1cblxuLyoqXG4gKiBBc3luY2hyb25vdXNseSBmZXRjaGVzIGFsbCBjb21wb25lbnRzIG5lZWRlZCBmb3IgdGhlIGdpdmVuIHJvdXRlclxuICogc3RhdGUgYW5kIGNhbGxzIGNhbGxiYWNrKGVycm9yLCBjb21wb25lbnRzKSB3aGVuIGZpbmlzaGVkLlxuICpcbiAqIE5vdGU6IFRoaXMgb3BlcmF0aW9uIG1heSBmaW5pc2ggc3luY2hyb25vdXNseSBpZiBubyByb3V0ZXMgaGF2ZSBhblxuICogYXN5bmNocm9ub3VzIGdldENvbXBvbmVudHMgbWV0aG9kLlxuICovXG5mdW5jdGlvbiBnZXRDb21wb25lbnRzKG5leHRTdGF0ZSwgY2FsbGJhY2spIHtcbiAgKDAsIF9Bc3luY1V0aWxzLm1hcEFzeW5jKShuZXh0U3RhdGUucm91dGVzLCBmdW5jdGlvbiAocm91dGUsIGluZGV4LCBjYWxsYmFjaykge1xuICAgIGdldENvbXBvbmVudHNGb3JSb3V0ZShuZXh0U3RhdGUsIHJvdXRlLCBjYWxsYmFjayk7XG4gIH0sIGNhbGxiYWNrKTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gZ2V0Q29tcG9uZW50cztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9QYXR0ZXJuVXRpbHMgPSByZXF1aXJlKCcuL1BhdHRlcm5VdGlscycpO1xuXG4vKipcbiAqIEV4dHJhY3RzIGFuIG9iamVjdCBvZiBwYXJhbXMgdGhlIGdpdmVuIHJvdXRlIGNhcmVzIGFib3V0IGZyb21cbiAqIHRoZSBnaXZlbiBwYXJhbXMgb2JqZWN0LlxuICovXG5mdW5jdGlvbiBnZXRSb3V0ZVBhcmFtcyhyb3V0ZSwgcGFyYW1zKSB7XG4gIHZhciByb3V0ZVBhcmFtcyA9IHt9O1xuXG4gIGlmICghcm91dGUucGF0aCkgcmV0dXJuIHJvdXRlUGFyYW1zO1xuXG4gICgwLCBfUGF0dGVyblV0aWxzLmdldFBhcmFtTmFtZXMpKHJvdXRlLnBhdGgpLmZvckVhY2goZnVuY3Rpb24gKHApIHtcbiAgICBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHBhcmFtcywgcCkpIHtcbiAgICAgIHJvdXRlUGFyYW1zW3BdID0gcGFyYW1zW3BdO1xuICAgIH1cbiAgfSk7XG5cbiAgcmV0dXJuIHJvdXRlUGFyYW1zO1xufVxuXG5leHBvcnRzLmRlZmF1bHQgPSBnZXRSb3V0ZVBhcmFtcztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9jcmVhdGVIYXNoSGlzdG9yeSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL2NyZWF0ZUhhc2hIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlSGFzaEhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlSGFzaEhpc3RvcnkpO1xuXG52YXIgX2NyZWF0ZVJvdXRlckhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZVJvdXRlckhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVSb3V0ZXJIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVJvdXRlckhpc3RvcnkpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5leHBvcnRzLmRlZmF1bHQgPSAoMCwgX2NyZWF0ZVJvdXRlckhpc3RvcnkyLmRlZmF1bHQpKF9jcmVhdGVIYXNoSGlzdG9yeTIuZGVmYXVsdCk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmNyZWF0ZU1lbW9yeUhpc3RvcnkgPSBleHBvcnRzLmhhc2hIaXN0b3J5ID0gZXhwb3J0cy5icm93c2VySGlzdG9yeSA9IGV4cG9ydHMuYXBwbHlSb3V0ZXJNaWRkbGV3YXJlID0gZXhwb3J0cy5mb3JtYXRQYXR0ZXJuID0gZXhwb3J0cy51c2VSb3V0ZXJIaXN0b3J5ID0gZXhwb3J0cy5tYXRjaCA9IGV4cG9ydHMucm91dGVyU2hhcGUgPSBleHBvcnRzLmxvY2F0aW9uU2hhcGUgPSBleHBvcnRzLlByb3BUeXBlcyA9IGV4cG9ydHMuUm91dGluZ0NvbnRleHQgPSBleHBvcnRzLlJvdXRlckNvbnRleHQgPSBleHBvcnRzLmNyZWF0ZVJvdXRlcyA9IGV4cG9ydHMudXNlUm91dGVzID0gZXhwb3J0cy5Sb3V0ZUNvbnRleHQgPSBleHBvcnRzLkxpZmVjeWNsZSA9IGV4cG9ydHMuSGlzdG9yeSA9IGV4cG9ydHMuUm91dGUgPSBleHBvcnRzLlJlZGlyZWN0ID0gZXhwb3J0cy5JbmRleFJvdXRlID0gZXhwb3J0cy5JbmRleFJlZGlyZWN0ID0gZXhwb3J0cy53aXRoUm91dGVyID0gZXhwb3J0cy5JbmRleExpbmsgPSBleHBvcnRzLkxpbmsgPSBleHBvcnRzLlJvdXRlciA9IHVuZGVmaW5lZDtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbk9iamVjdC5kZWZpbmVQcm9wZXJ0eShleHBvcnRzLCAnY3JlYXRlUm91dGVzJywge1xuICBlbnVtZXJhYmxlOiB0cnVlLFxuICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICByZXR1cm4gX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVzO1xuICB9XG59KTtcblxudmFyIF9Qcm9wVHlwZXMyID0gcmVxdWlyZSgnLi9Qcm9wVHlwZXMnKTtcblxuT2JqZWN0LmRlZmluZVByb3BlcnR5KGV4cG9ydHMsICdsb2NhdGlvblNoYXBlJywge1xuICBlbnVtZXJhYmxlOiB0cnVlLFxuICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICByZXR1cm4gX1Byb3BUeXBlczIubG9jYXRpb25TaGFwZTtcbiAgfVxufSk7XG5PYmplY3QuZGVmaW5lUHJvcGVydHkoZXhwb3J0cywgJ3JvdXRlclNoYXBlJywge1xuICBlbnVtZXJhYmxlOiB0cnVlLFxuICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICByZXR1cm4gX1Byb3BUeXBlczIucm91dGVyU2hhcGU7XG4gIH1cbn0pO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbk9iamVjdC5kZWZpbmVQcm9wZXJ0eShleHBvcnRzLCAnZm9ybWF0UGF0dGVybicsIHtcbiAgZW51bWVyYWJsZTogdHJ1ZSxcbiAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgcmV0dXJuIF9QYXR0ZXJuVXRpbHMuZm9ybWF0UGF0dGVybjtcbiAgfVxufSk7XG5cbnZhciBfUm91dGVyMiA9IHJlcXVpcmUoJy4vUm91dGVyJyk7XG5cbnZhciBfUm91dGVyMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlcjIpO1xuXG52YXIgX0xpbmsyID0gcmVxdWlyZSgnLi9MaW5rJyk7XG5cbnZhciBfTGluazMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9MaW5rMik7XG5cbnZhciBfSW5kZXhMaW5rMiA9IHJlcXVpcmUoJy4vSW5kZXhMaW5rJyk7XG5cbnZhciBfSW5kZXhMaW5rMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0luZGV4TGluazIpO1xuXG52YXIgX3dpdGhSb3V0ZXIyID0gcmVxdWlyZSgnLi93aXRoUm91dGVyJyk7XG5cbnZhciBfd2l0aFJvdXRlcjMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93aXRoUm91dGVyMik7XG5cbnZhciBfSW5kZXhSZWRpcmVjdDIgPSByZXF1aXJlKCcuL0luZGV4UmVkaXJlY3QnKTtcblxudmFyIF9JbmRleFJlZGlyZWN0MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0luZGV4UmVkaXJlY3QyKTtcblxudmFyIF9JbmRleFJvdXRlMiA9IHJlcXVpcmUoJy4vSW5kZXhSb3V0ZScpO1xuXG52YXIgX0luZGV4Um91dGUzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfSW5kZXhSb3V0ZTIpO1xuXG52YXIgX1JlZGlyZWN0MiA9IHJlcXVpcmUoJy4vUmVkaXJlY3QnKTtcblxudmFyIF9SZWRpcmVjdDMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9SZWRpcmVjdDIpO1xuXG52YXIgX1JvdXRlMiA9IHJlcXVpcmUoJy4vUm91dGUnKTtcblxudmFyIF9Sb3V0ZTMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0ZTIpO1xuXG52YXIgX0hpc3RvcnkyID0gcmVxdWlyZSgnLi9IaXN0b3J5Jyk7XG5cbnZhciBfSGlzdG9yeTMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9IaXN0b3J5Mik7XG5cbnZhciBfTGlmZWN5Y2xlMiA9IHJlcXVpcmUoJy4vTGlmZWN5Y2xlJyk7XG5cbnZhciBfTGlmZWN5Y2xlMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0xpZmVjeWNsZTIpO1xuXG52YXIgX1JvdXRlQ29udGV4dDIgPSByZXF1aXJlKCcuL1JvdXRlQ29udGV4dCcpO1xuXG52YXIgX1JvdXRlQ29udGV4dDMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0ZUNvbnRleHQyKTtcblxudmFyIF91c2VSb3V0ZXMyID0gcmVxdWlyZSgnLi91c2VSb3V0ZXMnKTtcblxudmFyIF91c2VSb3V0ZXMzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUm91dGVzMik7XG5cbnZhciBfUm91dGVyQ29udGV4dDIgPSByZXF1aXJlKCcuL1JvdXRlckNvbnRleHQnKTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlckNvbnRleHQyKTtcblxudmFyIF9Sb3V0aW5nQ29udGV4dDIgPSByZXF1aXJlKCcuL1JvdXRpbmdDb250ZXh0Jyk7XG5cbnZhciBfUm91dGluZ0NvbnRleHQzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUm91dGluZ0NvbnRleHQyKTtcblxudmFyIF9Qcm9wVHlwZXMzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUHJvcFR5cGVzMik7XG5cbnZhciBfbWF0Y2gyID0gcmVxdWlyZSgnLi9tYXRjaCcpO1xuXG52YXIgX21hdGNoMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX21hdGNoMik7XG5cbnZhciBfdXNlUm91dGVySGlzdG9yeTIgPSByZXF1aXJlKCcuL3VzZVJvdXRlckhpc3RvcnknKTtcblxudmFyIF91c2VSb3V0ZXJIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZVJvdXRlckhpc3RvcnkyKTtcblxudmFyIF9hcHBseVJvdXRlck1pZGRsZXdhcmUyID0gcmVxdWlyZSgnLi9hcHBseVJvdXRlck1pZGRsZXdhcmUnKTtcblxudmFyIF9hcHBseVJvdXRlck1pZGRsZXdhcmUzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfYXBwbHlSb3V0ZXJNaWRkbGV3YXJlMik7XG5cbnZhciBfYnJvd3Nlckhpc3RvcnkyID0gcmVxdWlyZSgnLi9icm93c2VySGlzdG9yeScpO1xuXG52YXIgX2Jyb3dzZXJIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2Jyb3dzZXJIaXN0b3J5Mik7XG5cbnZhciBfaGFzaEhpc3RvcnkyID0gcmVxdWlyZSgnLi9oYXNoSGlzdG9yeScpO1xuXG52YXIgX2hhc2hIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2hhc2hIaXN0b3J5Mik7XG5cbnZhciBfY3JlYXRlTWVtb3J5SGlzdG9yeTIgPSByZXF1aXJlKCcuL2NyZWF0ZU1lbW9yeUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVNZW1vcnlIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZU1lbW9yeUhpc3RvcnkyKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZXhwb3J0cy5Sb3V0ZXIgPSBfUm91dGVyMy5kZWZhdWx0OyAvKiBjb21wb25lbnRzICovXG5cbmV4cG9ydHMuTGluayA9IF9MaW5rMy5kZWZhdWx0O1xuZXhwb3J0cy5JbmRleExpbmsgPSBfSW5kZXhMaW5rMy5kZWZhdWx0O1xuZXhwb3J0cy53aXRoUm91dGVyID0gX3dpdGhSb3V0ZXIzLmRlZmF1bHQ7XG5cbi8qIGNvbXBvbmVudHMgKGNvbmZpZ3VyYXRpb24pICovXG5cbmV4cG9ydHMuSW5kZXhSZWRpcmVjdCA9IF9JbmRleFJlZGlyZWN0My5kZWZhdWx0O1xuZXhwb3J0cy5JbmRleFJvdXRlID0gX0luZGV4Um91dGUzLmRlZmF1bHQ7XG5leHBvcnRzLlJlZGlyZWN0ID0gX1JlZGlyZWN0My5kZWZhdWx0O1xuZXhwb3J0cy5Sb3V0ZSA9IF9Sb3V0ZTMuZGVmYXVsdDtcblxuLyogbWl4aW5zICovXG5cbmV4cG9ydHMuSGlzdG9yeSA9IF9IaXN0b3J5My5kZWZhdWx0O1xuZXhwb3J0cy5MaWZlY3ljbGUgPSBfTGlmZWN5Y2xlMy5kZWZhdWx0O1xuZXhwb3J0cy5Sb3V0ZUNvbnRleHQgPSBfUm91dGVDb250ZXh0My5kZWZhdWx0O1xuXG4vKiB1dGlscyAqL1xuXG5leHBvcnRzLnVzZVJvdXRlcyA9IF91c2VSb3V0ZXMzLmRlZmF1bHQ7XG5leHBvcnRzLlJvdXRlckNvbnRleHQgPSBfUm91dGVyQ29udGV4dDMuZGVmYXVsdDtcbmV4cG9ydHMuUm91dGluZ0NvbnRleHQgPSBfUm91dGluZ0NvbnRleHQzLmRlZmF1bHQ7XG5leHBvcnRzLlByb3BUeXBlcyA9IF9Qcm9wVHlwZXMzLmRlZmF1bHQ7XG5leHBvcnRzLm1hdGNoID0gX21hdGNoMy5kZWZhdWx0O1xuZXhwb3J0cy51c2VSb3V0ZXJIaXN0b3J5ID0gX3VzZVJvdXRlckhpc3RvcnkzLmRlZmF1bHQ7XG5leHBvcnRzLmFwcGx5Um91dGVyTWlkZGxld2FyZSA9IF9hcHBseVJvdXRlck1pZGRsZXdhcmUzLmRlZmF1bHQ7XG5cbi8qIGhpc3RvcmllcyAqL1xuXG5leHBvcnRzLmJyb3dzZXJIaXN0b3J5ID0gX2Jyb3dzZXJIaXN0b3J5My5kZWZhdWx0O1xuZXhwb3J0cy5oYXNoSGlzdG9yeSA9IF9oYXNoSGlzdG9yeTMuZGVmYXVsdDtcbmV4cG9ydHMuY3JlYXRlTWVtb3J5SGlzdG9yeSA9IF9jcmVhdGVNZW1vcnlIaXN0b3J5My5kZWZhdWx0OyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF90eXBlb2YgPSB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgdHlwZW9mIFN5bWJvbC5pdGVyYXRvciA9PT0gXCJzeW1ib2xcIiA/IGZ1bmN0aW9uIChvYmopIHsgcmV0dXJuIHR5cGVvZiBvYmo7IH0gOiBmdW5jdGlvbiAob2JqKSB7IHJldHVybiBvYmogJiYgdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIG9iai5jb25zdHJ1Y3RvciA9PT0gU3ltYm9sID8gXCJzeW1ib2xcIiA6IHR5cGVvZiBvYmo7IH07XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGlzQWN0aXZlO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbmZ1bmN0aW9uIGRlZXBFcXVhbChhLCBiKSB7XG4gIGlmIChhID09IGIpIHJldHVybiB0cnVlO1xuXG4gIGlmIChhID09IG51bGwgfHwgYiA9PSBudWxsKSByZXR1cm4gZmFsc2U7XG5cbiAgaWYgKEFycmF5LmlzQXJyYXkoYSkpIHtcbiAgICByZXR1cm4gQXJyYXkuaXNBcnJheShiKSAmJiBhLmxlbmd0aCA9PT0gYi5sZW5ndGggJiYgYS5ldmVyeShmdW5jdGlvbiAoaXRlbSwgaW5kZXgpIHtcbiAgICAgIHJldHVybiBkZWVwRXF1YWwoaXRlbSwgYltpbmRleF0pO1xuICAgIH0pO1xuICB9XG5cbiAgaWYgKCh0eXBlb2YgYSA9PT0gJ3VuZGVmaW5lZCcgPyAndW5kZWZpbmVkJyA6IF90eXBlb2YoYSkpID09PSAnb2JqZWN0Jykge1xuICAgIGZvciAodmFyIHAgaW4gYSkge1xuICAgICAgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoYSwgcCkpIHtcbiAgICAgICAgY29udGludWU7XG4gICAgICB9XG5cbiAgICAgIGlmIChhW3BdID09PSB1bmRlZmluZWQpIHtcbiAgICAgICAgaWYgKGJbcF0gIT09IHVuZGVmaW5lZCkge1xuICAgICAgICAgIHJldHVybiBmYWxzZTtcbiAgICAgICAgfVxuICAgICAgfSBlbHNlIGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKGIsIHApKSB7XG4gICAgICAgIHJldHVybiBmYWxzZTtcbiAgICAgIH0gZWxzZSBpZiAoIWRlZXBFcXVhbChhW3BdLCBiW3BdKSkge1xuICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIHRydWU7XG4gIH1cblxuICByZXR1cm4gU3RyaW5nKGEpID09PSBTdHJpbmcoYik7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIHRoZSBjdXJyZW50IHBhdGhuYW1lIG1hdGNoZXMgdGhlIHN1cHBsaWVkIG9uZSwgbmV0IG9mXG4gKiBsZWFkaW5nIGFuZCB0cmFpbGluZyBzbGFzaCBub3JtYWxpemF0aW9uLiBUaGlzIGlzIHN1ZmZpY2llbnQgZm9yIGFuXG4gKiBpbmRleE9ubHkgcm91dGUgbWF0Y2guXG4gKi9cbmZ1bmN0aW9uIHBhdGhJc0FjdGl2ZShwYXRobmFtZSwgY3VycmVudFBhdGhuYW1lKSB7XG4gIC8vIE5vcm1hbGl6ZSBsZWFkaW5nIHNsYXNoIGZvciBjb25zaXN0ZW5jeS4gTGVhZGluZyBzbGFzaCBvbiBwYXRobmFtZSBoYXNcbiAgLy8gYWxyZWFkeSBiZWVuIG5vcm1hbGl6ZWQgaW4gaXNBY3RpdmUuIFNlZSBjYXZlYXQgdGhlcmUuXG4gIGlmIChjdXJyZW50UGF0aG5hbWUuY2hhckF0KDApICE9PSAnLycpIHtcbiAgICBjdXJyZW50UGF0aG5hbWUgPSAnLycgKyBjdXJyZW50UGF0aG5hbWU7XG4gIH1cblxuICAvLyBOb3JtYWxpemUgdGhlIGVuZCBvZiBib3RoIHBhdGggbmFtZXMgdG9vLiBNYXliZSBgL2Zvby9gIHNob3VsZG4ndCBzaG93XG4gIC8vIGAvZm9vYCBhcyBhY3RpdmUsIGJ1dCBpbiB0aGlzIGNhc2UsIHdlIHdvdWxkIGFscmVhZHkgaGF2ZSBmYWlsZWQgdGhlXG4gIC8vIG1hdGNoLlxuICBpZiAocGF0aG5hbWUuY2hhckF0KHBhdGhuYW1lLmxlbmd0aCAtIDEpICE9PSAnLycpIHtcbiAgICBwYXRobmFtZSArPSAnLyc7XG4gIH1cbiAgaWYgKGN1cnJlbnRQYXRobmFtZS5jaGFyQXQoY3VycmVudFBhdGhuYW1lLmxlbmd0aCAtIDEpICE9PSAnLycpIHtcbiAgICBjdXJyZW50UGF0aG5hbWUgKz0gJy8nO1xuICB9XG5cbiAgcmV0dXJuIGN1cnJlbnRQYXRobmFtZSA9PT0gcGF0aG5hbWU7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIHRoZSBnaXZlbiBwYXRobmFtZSBtYXRjaGVzIHRoZSBhY3RpdmUgcm91dGVzIGFuZCBwYXJhbXMuXG4gKi9cbmZ1bmN0aW9uIHJvdXRlSXNBY3RpdmUocGF0aG5hbWUsIHJvdXRlcywgcGFyYW1zKSB7XG4gIHZhciByZW1haW5pbmdQYXRobmFtZSA9IHBhdGhuYW1lLFxuICAgICAgcGFyYW1OYW1lcyA9IFtdLFxuICAgICAgcGFyYW1WYWx1ZXMgPSBbXTtcblxuICAvLyBmb3IuLi5vZiB3b3VsZCB3b3JrIGhlcmUgYnV0IGl0J3MgcHJvYmFibHkgc2xvd2VyIHBvc3QtdHJhbnNwaWxhdGlvbi5cbiAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IHJvdXRlcy5sZW5ndGg7IGkgPCBsZW47ICsraSkge1xuICAgIHZhciByb3V0ZSA9IHJvdXRlc1tpXTtcbiAgICB2YXIgcGF0dGVybiA9IHJvdXRlLnBhdGggfHwgJyc7XG5cbiAgICBpZiAocGF0dGVybi5jaGFyQXQoMCkgPT09ICcvJykge1xuICAgICAgcmVtYWluaW5nUGF0aG5hbWUgPSBwYXRobmFtZTtcbiAgICAgIHBhcmFtTmFtZXMgPSBbXTtcbiAgICAgIHBhcmFtVmFsdWVzID0gW107XG4gICAgfVxuXG4gICAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lICE9PSBudWxsICYmIHBhdHRlcm4pIHtcbiAgICAgIHZhciBtYXRjaGVkID0gKDAsIF9QYXR0ZXJuVXRpbHMubWF0Y2hQYXR0ZXJuKShwYXR0ZXJuLCByZW1haW5pbmdQYXRobmFtZSk7XG4gICAgICBpZiAobWF0Y2hlZCkge1xuICAgICAgICByZW1haW5pbmdQYXRobmFtZSA9IG1hdGNoZWQucmVtYWluaW5nUGF0aG5hbWU7XG4gICAgICAgIHBhcmFtTmFtZXMgPSBbXS5jb25jYXQocGFyYW1OYW1lcywgbWF0Y2hlZC5wYXJhbU5hbWVzKTtcbiAgICAgICAgcGFyYW1WYWx1ZXMgPSBbXS5jb25jYXQocGFyYW1WYWx1ZXMsIG1hdGNoZWQucGFyYW1WYWx1ZXMpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcmVtYWluaW5nUGF0aG5hbWUgPSBudWxsO1xuICAgICAgfVxuXG4gICAgICBpZiAocmVtYWluaW5nUGF0aG5hbWUgPT09ICcnKSB7XG4gICAgICAgIC8vIFdlIGhhdmUgYW4gZXhhY3QgbWF0Y2ggb24gdGhlIHJvdXRlLiBKdXN0IGNoZWNrIHRoYXQgYWxsIHRoZSBwYXJhbXNcbiAgICAgICAgLy8gbWF0Y2guXG4gICAgICAgIC8vIEZJWE1FOiBUaGlzIGRvZXNuJ3Qgd29yayBvbiByZXBlYXRlZCBwYXJhbXMuXG4gICAgICAgIHJldHVybiBwYXJhbU5hbWVzLmV2ZXJ5KGZ1bmN0aW9uIChwYXJhbU5hbWUsIGluZGV4KSB7XG4gICAgICAgICAgcmV0dXJuIFN0cmluZyhwYXJhbVZhbHVlc1tpbmRleF0pID09PSBTdHJpbmcocGFyYW1zW3BhcmFtTmFtZV0pO1xuICAgICAgICB9KTtcbiAgICAgIH1cbiAgICB9XG4gIH1cblxuICByZXR1cm4gZmFsc2U7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIGFsbCBrZXkvdmFsdWUgcGFpcnMgaW4gdGhlIGdpdmVuIHF1ZXJ5IGFyZVxuICogY3VycmVudGx5IGFjdGl2ZS5cbiAqL1xuZnVuY3Rpb24gcXVlcnlJc0FjdGl2ZShxdWVyeSwgYWN0aXZlUXVlcnkpIHtcbiAgaWYgKGFjdGl2ZVF1ZXJ5ID09IG51bGwpIHJldHVybiBxdWVyeSA9PSBudWxsO1xuXG4gIGlmIChxdWVyeSA9PSBudWxsKSByZXR1cm4gdHJ1ZTtcblxuICByZXR1cm4gZGVlcEVxdWFsKHF1ZXJ5LCBhY3RpdmVRdWVyeSk7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIGEgPExpbms+IHRvIHRoZSBnaXZlbiBwYXRobmFtZS9xdWVyeSBjb21iaW5hdGlvbiBpc1xuICogY3VycmVudGx5IGFjdGl2ZS5cbiAqL1xuZnVuY3Rpb24gaXNBY3RpdmUoX3JlZiwgaW5kZXhPbmx5LCBjdXJyZW50TG9jYXRpb24sIHJvdXRlcywgcGFyYW1zKSB7XG4gIHZhciBwYXRobmFtZSA9IF9yZWYucGF0aG5hbWU7XG4gIHZhciBxdWVyeSA9IF9yZWYucXVlcnk7XG5cbiAgaWYgKGN1cnJlbnRMb2NhdGlvbiA9PSBudWxsKSByZXR1cm4gZmFsc2U7XG5cbiAgLy8gVE9ETzogVGhpcyBpcyBhIGJpdCB1Z2x5LiBJdCBrZWVwcyBhcm91bmQgc3VwcG9ydCBmb3IgdHJlYXRpbmcgcGF0aG5hbWVzXG4gIC8vIHdpdGhvdXQgcHJlY2VkaW5nIHNsYXNoZXMgYXMgYWJzb2x1dGUgcGF0aHMsIGJ1dCBwb3NzaWJseSBhbHNvIHdvcmtzXG4gIC8vIGFyb3VuZCB0aGUgc2FtZSBxdWlya3Mgd2l0aCBiYXNlbmFtZXMgYXMgaW4gbWF0Y2hSb3V0ZXMuXG4gIGlmIChwYXRobmFtZS5jaGFyQXQoMCkgIT09ICcvJykge1xuICAgIHBhdGhuYW1lID0gJy8nICsgcGF0aG5hbWU7XG4gIH1cblxuICBpZiAoIXBhdGhJc0FjdGl2ZShwYXRobmFtZSwgY3VycmVudExvY2F0aW9uLnBhdGhuYW1lKSkge1xuICAgIC8vIFRoZSBwYXRoIGNoZWNrIGlzIG5lY2Vzc2FyeSBhbmQgc3VmZmljaWVudCBmb3IgaW5kZXhPbmx5LCBidXQgb3RoZXJ3aXNlXG4gICAgLy8gd2Ugc3RpbGwgbmVlZCB0byBjaGVjayB0aGUgcm91dGVzLlxuICAgIGlmIChpbmRleE9ubHkgfHwgIXJvdXRlSXNBY3RpdmUocGF0aG5hbWUsIHJvdXRlcywgcGFyYW1zKSkge1xuICAgICAgcmV0dXJuIGZhbHNlO1xuICAgIH1cbiAgfVxuXG4gIHJldHVybiBxdWVyeUlzQWN0aXZlKHF1ZXJ5LCBjdXJyZW50TG9jYXRpb24ucXVlcnkpO1xufVxubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5leHBvcnRzLmRlZmF1bHQgPSBtYWtlU3RhdGVXaXRoTG9jYXRpb247XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBtYWtlU3RhdGVXaXRoTG9jYXRpb24oc3RhdGUsIGxvY2F0aW9uKSB7XG4gIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nICYmIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzLmNhblVzZU1lbWJyYW5lKSB7XG4gICAgdmFyIHN0YXRlV2l0aExvY2F0aW9uID0gX2V4dGVuZHMoe30sIHN0YXRlKTtcblxuICAgIC8vIEkgZG9uJ3QgdXNlIGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMgaGVyZSBiZWNhdXNlIEkgd2FudCB0byBrZWVwIHRoZVxuICAgIC8vIHNhbWUgY29kZSBwYXRoIGJldHdlZW4gZGV2ZWxvcG1lbnQgYW5kIHByb2R1Y3Rpb24sIGluIHRoYXQgd2UganVzdFxuICAgIC8vIGFzc2lnbiBleHRyYSBwcm9wZXJ0aWVzIHRvIHRoZSBjb3B5IG9mIHRoZSBzdGF0ZSBvYmplY3QgaW4gYm90aCBjYXNlcy5cblxuICAgIHZhciBfbG9vcCA9IGZ1bmN0aW9uIF9sb29wKHByb3ApIHtcbiAgICAgIGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKGxvY2F0aW9uLCBwcm9wKSkge1xuICAgICAgICByZXR1cm4gJ2NvbnRpbnVlJztcbiAgICAgIH1cblxuICAgICAgT2JqZWN0LmRlZmluZVByb3BlcnR5KHN0YXRlV2l0aExvY2F0aW9uLCBwcm9wLCB7XG4gICAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnQWNjZXNzaW5nIGxvY2F0aW9uIHByb3BlcnRpZXMgZGlyZWN0bHkgZnJvbSB0aGUgZmlyc3QgYXJndW1lbnQgdG8gYGdldENvbXBvbmVudGAsIGBnZXRDb21wb25lbnRzYCwgYGdldENoaWxkUm91dGVzYCwgYW5kIGBnZXRJbmRleFJvdXRlYCBpcyBkZXByZWNhdGVkLiBUaGF0IGFyZ3VtZW50IGlzIG5vdyB0aGUgcm91dGVyIHN0YXRlIChgbmV4dFN0YXRlYCBvciBgcGFydGlhbE5leHRTdGF0ZWApIHJhdGhlciB0aGFuIHRoZSBsb2NhdGlvbi4gVG8gYWNjZXNzIHRoZSBsb2NhdGlvbiwgdXNlIGBuZXh0U3RhdGUubG9jYXRpb25gIG9yIGBwYXJ0aWFsTmV4dFN0YXRlLmxvY2F0aW9uYC4nKSA6IHZvaWQgMDtcbiAgICAgICAgICByZXR1cm4gbG9jYXRpb25bcHJvcF07XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH07XG5cbiAgICBmb3IgKHZhciBwcm9wIGluIGxvY2F0aW9uKSB7XG4gICAgICB2YXIgX3JldCA9IF9sb29wKHByb3ApO1xuXG4gICAgICBpZiAoX3JldCA9PT0gJ2NvbnRpbnVlJykgY29udGludWU7XG4gICAgfVxuXG4gICAgcmV0dXJuIHN0YXRlV2l0aExvY2F0aW9uO1xuICB9XG5cbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBzdGF0ZSwgbG9jYXRpb24pO1xufVxubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX0FjdGlvbnMgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi9BY3Rpb25zJyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfY3JlYXRlTWVtb3J5SGlzdG9yeSA9IHJlcXVpcmUoJy4vY3JlYXRlTWVtb3J5SGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZU1lbW9yeUhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlTWVtb3J5SGlzdG9yeSk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIgPSByZXF1aXJlKCcuL2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyJyk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxudmFyIF9Sb3V0ZXJVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVyVXRpbHMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKG9iaiwga2V5cykgeyB2YXIgdGFyZ2V0ID0ge307IGZvciAodmFyIGkgaW4gb2JqKSB7IGlmIChrZXlzLmluZGV4T2YoaSkgPj0gMCkgY29udGludWU7IGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwgaSkpIGNvbnRpbnVlOyB0YXJnZXRbaV0gPSBvYmpbaV07IH0gcmV0dXJuIHRhcmdldDsgfVxuXG4vKipcbiAqIEEgaGlnaC1sZXZlbCBBUEkgdG8gYmUgdXNlZCBmb3Igc2VydmVyLXNpZGUgcmVuZGVyaW5nLlxuICpcbiAqIFRoaXMgZnVuY3Rpb24gbWF0Y2hlcyBhIGxvY2F0aW9uIHRvIGEgc2V0IG9mIHJvdXRlcyBhbmQgY2FsbHNcbiAqIGNhbGxiYWNrKGVycm9yLCByZWRpcmVjdExvY2F0aW9uLCByZW5kZXJQcm9wcykgd2hlbiBmaW5pc2hlZC5cbiAqXG4gKiBOb3RlOiBZb3UgcHJvYmFibHkgZG9uJ3Qgd2FudCB0byB1c2UgdGhpcyBpbiBhIGJyb3dzZXIgdW5sZXNzIHlvdSdyZSB1c2luZ1xuICogc2VydmVyLXNpZGUgcmVuZGVyaW5nIHdpdGggYXN5bmMgcm91dGVzLlxuICovXG5mdW5jdGlvbiBtYXRjaChfcmVmLCBjYWxsYmFjaykge1xuICB2YXIgaGlzdG9yeSA9IF9yZWYuaGlzdG9yeTtcbiAgdmFyIHJvdXRlcyA9IF9yZWYucm91dGVzO1xuICB2YXIgbG9jYXRpb24gPSBfcmVmLmxvY2F0aW9uO1xuXG4gIHZhciBvcHRpb25zID0gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKF9yZWYsIFsnaGlzdG9yeScsICdyb3V0ZXMnLCAnbG9jYXRpb24nXSk7XG5cbiAgIShoaXN0b3J5IHx8IGxvY2F0aW9uKSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdtYXRjaCBuZWVkcyBhIGhpc3Rvcnkgb3IgYSBsb2NhdGlvbicpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICBoaXN0b3J5ID0gaGlzdG9yeSA/IGhpc3RvcnkgOiAoMCwgX2NyZWF0ZU1lbW9yeUhpc3RvcnkyLmRlZmF1bHQpKG9wdGlvbnMpO1xuICB2YXIgdHJhbnNpdGlvbk1hbmFnZXIgPSAoMCwgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyMi5kZWZhdWx0KShoaXN0b3J5LCAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVzKShyb3V0ZXMpKTtcblxuICB2YXIgdW5saXN0ZW4gPSB2b2lkIDA7XG5cbiAgaWYgKGxvY2F0aW9uKSB7XG4gICAgLy8gQWxsb3cgbWF0Y2goeyBsb2NhdGlvbjogJy90aGUvcGF0aCcsIC4uLiB9KVxuICAgIGxvY2F0aW9uID0gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbihsb2NhdGlvbik7XG4gIH0gZWxzZSB7XG4gICAgLy8gUGljayB1cCB0aGUgbG9jYXRpb24gZnJvbSB0aGUgaGlzdG9yeSB2aWEgc3luY2hyb25vdXMgaGlzdG9yeS5saXN0ZW5cbiAgICAvLyBjYWxsIGlmIG5lZWRlZC5cbiAgICB1bmxpc3RlbiA9IGhpc3RvcnkubGlzdGVuKGZ1bmN0aW9uIChoaXN0b3J5TG9jYXRpb24pIHtcbiAgICAgIGxvY2F0aW9uID0gaGlzdG9yeUxvY2F0aW9uO1xuICAgIH0pO1xuICB9XG5cbiAgdmFyIHJvdXRlciA9ICgwLCBfUm91dGVyVXRpbHMuY3JlYXRlUm91dGVyT2JqZWN0KShoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG4gIGhpc3RvcnkgPSAoMCwgX1JvdXRlclV0aWxzLmNyZWF0ZVJvdXRpbmdIaXN0b3J5KShoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG5cbiAgdHJhbnNpdGlvbk1hbmFnZXIubWF0Y2gobG9jYXRpb24sIGZ1bmN0aW9uIChlcnJvciwgcmVkaXJlY3RMb2NhdGlvbiwgbmV4dFN0YXRlKSB7XG4gICAgY2FsbGJhY2soZXJyb3IsIHJlZGlyZWN0TG9jYXRpb24gJiYgcm91dGVyLmNyZWF0ZUxvY2F0aW9uKHJlZGlyZWN0TG9jYXRpb24sIF9BY3Rpb25zLlJFUExBQ0UpLCBuZXh0U3RhdGUgJiYgX2V4dGVuZHMoe30sIG5leHRTdGF0ZSwge1xuICAgICAgaGlzdG9yeTogaGlzdG9yeSxcbiAgICAgIHJvdXRlcjogcm91dGVyLFxuICAgICAgbWF0Y2hDb250ZXh0OiB7IGhpc3Rvcnk6IGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyOiB0cmFuc2l0aW9uTWFuYWdlciwgcm91dGVyOiByb3V0ZXIgfVxuICAgIH0pKTtcblxuICAgIC8vIERlZmVyIHJlbW92aW5nIHRoZSBsaXN0ZW5lciB0byBoZXJlIHRvIHByZXZlbnQgRE9NIGhpc3RvcmllcyBmcm9tIGhhdmluZ1xuICAgIC8vIHRvIHVud2luZCBET00gZXZlbnQgbGlzdGVuZXJzIHVubmVjZXNzYXJpbHksIGluIGNhc2UgY2FsbGJhY2sgcmVuZGVycyBhXG4gICAgLy8gPFJvdXRlcj4gYW5kIGF0dGFjaGVzIGFub3RoZXIgaGlzdG9yeSBsaXN0ZW5lci5cbiAgICBpZiAodW5saXN0ZW4pIHtcbiAgICAgIHVubGlzdGVuKCk7XG4gICAgfVxuICB9KTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gbWF0Y2g7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfdHlwZW9mID0gdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIHR5cGVvZiBTeW1ib2wuaXRlcmF0b3IgPT09IFwic3ltYm9sXCIgPyBmdW5jdGlvbiAob2JqKSB7IHJldHVybiB0eXBlb2Ygb2JqOyB9IDogZnVuY3Rpb24gKG9iaikgeyByZXR1cm4gb2JqICYmIHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiBvYmouY29uc3RydWN0b3IgPT09IFN5bWJvbCA/IFwic3ltYm9sXCIgOiB0eXBlb2Ygb2JqOyB9O1xuXG5leHBvcnRzLmRlZmF1bHQgPSBtYXRjaFJvdXRlcztcblxudmFyIF9Bc3luY1V0aWxzID0gcmVxdWlyZSgnLi9Bc3luY1V0aWxzJyk7XG5cbnZhciBfbWFrZVN0YXRlV2l0aExvY2F0aW9uID0gcmVxdWlyZSgnLi9tYWtlU3RhdGVXaXRoTG9jYXRpb24nKTtcblxudmFyIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24yID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfbWFrZVN0YXRlV2l0aExvY2F0aW9uKTtcblxudmFyIF9QYXR0ZXJuVXRpbHMgPSByZXF1aXJlKCcuL1BhdHRlcm5VdGlscycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gZ2V0Q2hpbGRSb3V0ZXMocm91dGUsIGxvY2F0aW9uLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgY2FsbGJhY2spIHtcbiAgaWYgKHJvdXRlLmNoaWxkUm91dGVzKSB7XG4gICAgcmV0dXJuIFtudWxsLCByb3V0ZS5jaGlsZFJvdXRlc107XG4gIH1cbiAgaWYgKCFyb3V0ZS5nZXRDaGlsZFJvdXRlcykge1xuICAgIHJldHVybiBbXTtcbiAgfVxuXG4gIHZhciBzeW5jID0gdHJ1ZSxcbiAgICAgIHJlc3VsdCA9IHZvaWQgMDtcblxuICB2YXIgcGFydGlhbE5leHRTdGF0ZSA9IHtcbiAgICBsb2NhdGlvbjogbG9jYXRpb24sXG4gICAgcGFyYW1zOiBjcmVhdGVQYXJhbXMocGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpXG4gIH07XG5cbiAgdmFyIHBhcnRpYWxOZXh0U3RhdGVXaXRoTG9jYXRpb24gPSAoMCwgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbjIuZGVmYXVsdCkocGFydGlhbE5leHRTdGF0ZSwgbG9jYXRpb24pO1xuXG4gIHJvdXRlLmdldENoaWxkUm91dGVzKHBhcnRpYWxOZXh0U3RhdGVXaXRoTG9jYXRpb24sIGZ1bmN0aW9uIChlcnJvciwgY2hpbGRSb3V0ZXMpIHtcbiAgICBjaGlsZFJvdXRlcyA9ICFlcnJvciAmJiAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVzKShjaGlsZFJvdXRlcyk7XG4gICAgaWYgKHN5bmMpIHtcbiAgICAgIHJlc3VsdCA9IFtlcnJvciwgY2hpbGRSb3V0ZXNdO1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGNhbGxiYWNrKGVycm9yLCBjaGlsZFJvdXRlcyk7XG4gIH0pO1xuXG4gIHN5bmMgPSBmYWxzZTtcbiAgcmV0dXJuIHJlc3VsdDsgLy8gTWlnaHQgYmUgdW5kZWZpbmVkLlxufVxuXG5mdW5jdGlvbiBnZXRJbmRleFJvdXRlKHJvdXRlLCBsb2NhdGlvbiwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMsIGNhbGxiYWNrKSB7XG4gIGlmIChyb3V0ZS5pbmRleFJvdXRlKSB7XG4gICAgY2FsbGJhY2sobnVsbCwgcm91dGUuaW5kZXhSb3V0ZSk7XG4gIH0gZWxzZSBpZiAocm91dGUuZ2V0SW5kZXhSb3V0ZSkge1xuICAgIHZhciBwYXJ0aWFsTmV4dFN0YXRlID0ge1xuICAgICAgbG9jYXRpb246IGxvY2F0aW9uLFxuICAgICAgcGFyYW1zOiBjcmVhdGVQYXJhbXMocGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpXG4gICAgfTtcblxuICAgIHZhciBwYXJ0aWFsTmV4dFN0YXRlV2l0aExvY2F0aW9uID0gKDAsIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24yLmRlZmF1bHQpKHBhcnRpYWxOZXh0U3RhdGUsIGxvY2F0aW9uKTtcblxuICAgIHJvdXRlLmdldEluZGV4Um91dGUocGFydGlhbE5leHRTdGF0ZVdpdGhMb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCBpbmRleFJvdXRlKSB7XG4gICAgICBjYWxsYmFjayhlcnJvciwgIWVycm9yICYmICgwLCBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZXMpKGluZGV4Um91dGUpWzBdKTtcbiAgICB9KTtcbiAgfSBlbHNlIGlmIChyb3V0ZS5jaGlsZFJvdXRlcykge1xuICAgIChmdW5jdGlvbiAoKSB7XG4gICAgICB2YXIgcGF0aGxlc3MgPSByb3V0ZS5jaGlsZFJvdXRlcy5maWx0ZXIoZnVuY3Rpb24gKGNoaWxkUm91dGUpIHtcbiAgICAgICAgcmV0dXJuICFjaGlsZFJvdXRlLnBhdGg7XG4gICAgICB9KTtcblxuICAgICAgKDAsIF9Bc3luY1V0aWxzLmxvb3BBc3luYykocGF0aGxlc3MubGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIG5leHQsIGRvbmUpIHtcbiAgICAgICAgZ2V0SW5kZXhSb3V0ZShwYXRobGVzc1tpbmRleF0sIGxvY2F0aW9uLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgZnVuY3Rpb24gKGVycm9yLCBpbmRleFJvdXRlKSB7XG4gICAgICAgICAgaWYgKGVycm9yIHx8IGluZGV4Um91dGUpIHtcbiAgICAgICAgICAgIHZhciByb3V0ZXMgPSBbcGF0aGxlc3NbaW5kZXhdXS5jb25jYXQoQXJyYXkuaXNBcnJheShpbmRleFJvdXRlKSA/IGluZGV4Um91dGUgOiBbaW5kZXhSb3V0ZV0pO1xuICAgICAgICAgICAgZG9uZShlcnJvciwgcm91dGVzKTtcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgbmV4dCgpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9LCBmdW5jdGlvbiAoZXJyLCByb3V0ZXMpIHtcbiAgICAgICAgY2FsbGJhY2sobnVsbCwgcm91dGVzKTtcbiAgICAgIH0pO1xuICAgIH0pKCk7XG4gIH0gZWxzZSB7XG4gICAgY2FsbGJhY2soKTtcbiAgfVxufVxuXG5mdW5jdGlvbiBhc3NpZ25QYXJhbXMocGFyYW1zLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcykge1xuICByZXR1cm4gcGFyYW1OYW1lcy5yZWR1Y2UoZnVuY3Rpb24gKHBhcmFtcywgcGFyYW1OYW1lLCBpbmRleCkge1xuICAgIHZhciBwYXJhbVZhbHVlID0gcGFyYW1WYWx1ZXMgJiYgcGFyYW1WYWx1ZXNbaW5kZXhdO1xuXG4gICAgaWYgKEFycmF5LmlzQXJyYXkocGFyYW1zW3BhcmFtTmFtZV0pKSB7XG4gICAgICBwYXJhbXNbcGFyYW1OYW1lXS5wdXNoKHBhcmFtVmFsdWUpO1xuICAgIH0gZWxzZSBpZiAocGFyYW1OYW1lIGluIHBhcmFtcykge1xuICAgICAgcGFyYW1zW3BhcmFtTmFtZV0gPSBbcGFyYW1zW3BhcmFtTmFtZV0sIHBhcmFtVmFsdWVdO1xuICAgIH0gZWxzZSB7XG4gICAgICBwYXJhbXNbcGFyYW1OYW1lXSA9IHBhcmFtVmFsdWU7XG4gICAgfVxuXG4gICAgcmV0dXJuIHBhcmFtcztcbiAgfSwgcGFyYW1zKTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlUGFyYW1zKHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKSB7XG4gIHJldHVybiBhc3NpZ25QYXJhbXMoe30sIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKTtcbn1cblxuZnVuY3Rpb24gbWF0Y2hSb3V0ZURlZXAocm91dGUsIGxvY2F0aW9uLCByZW1haW5pbmdQYXRobmFtZSwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMsIGNhbGxiYWNrKSB7XG4gIHZhciBwYXR0ZXJuID0gcm91dGUucGF0aCB8fCAnJztcblxuICBpZiAocGF0dGVybi5jaGFyQXQoMCkgPT09ICcvJykge1xuICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gICAgcGFyYW1OYW1lcyA9IFtdO1xuICAgIHBhcmFtVmFsdWVzID0gW107XG4gIH1cblxuICAvLyBPbmx5IHRyeSB0byBtYXRjaCB0aGUgcGF0aCBpZiB0aGUgcm91dGUgYWN0dWFsbHkgaGFzIGEgcGF0dGVybiwgYW5kIGlmXG4gIC8vIHdlJ3JlIG5vdCBqdXN0IHNlYXJjaGluZyBmb3IgcG90ZW50aWFsIG5lc3RlZCBhYnNvbHV0ZSBwYXRocy5cbiAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lICE9PSBudWxsICYmIHBhdHRlcm4pIHtcbiAgICB0cnkge1xuICAgICAgdmFyIG1hdGNoZWQgPSAoMCwgX1BhdHRlcm5VdGlscy5tYXRjaFBhdHRlcm4pKHBhdHRlcm4sIHJlbWFpbmluZ1BhdGhuYW1lKTtcbiAgICAgIGlmIChtYXRjaGVkKSB7XG4gICAgICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbWF0Y2hlZC5yZW1haW5pbmdQYXRobmFtZTtcbiAgICAgICAgcGFyYW1OYW1lcyA9IFtdLmNvbmNhdChwYXJhbU5hbWVzLCBtYXRjaGVkLnBhcmFtTmFtZXMpO1xuICAgICAgICBwYXJhbVZhbHVlcyA9IFtdLmNvbmNhdChwYXJhbVZhbHVlcywgbWF0Y2hlZC5wYXJhbVZhbHVlcyk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZW1haW5pbmdQYXRobmFtZSA9IG51bGw7XG4gICAgICB9XG4gICAgfSBjYXRjaCAoZXJyb3IpIHtcbiAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICB9XG5cbiAgICAvLyBCeSBhc3N1bXB0aW9uLCBwYXR0ZXJuIGlzIG5vbi1lbXB0eSBoZXJlLCB3aGljaCBpcyB0aGUgcHJlcmVxdWlzaXRlIGZvclxuICAgIC8vIGFjdHVhbGx5IHRlcm1pbmF0aW5nIGEgbWF0Y2guXG4gICAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lID09PSAnJykge1xuICAgICAgdmFyIF9yZXQyID0gZnVuY3Rpb24gKCkge1xuICAgICAgICB2YXIgbWF0Y2ggPSB7XG4gICAgICAgICAgcm91dGVzOiBbcm91dGVdLFxuICAgICAgICAgIHBhcmFtczogY3JlYXRlUGFyYW1zKHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKVxuICAgICAgICB9O1xuXG4gICAgICAgIGdldEluZGV4Um91dGUocm91dGUsIGxvY2F0aW9uLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgZnVuY3Rpb24gKGVycm9yLCBpbmRleFJvdXRlKSB7XG4gICAgICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgICAgICBjYWxsYmFjayhlcnJvcik7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGlmIChBcnJheS5pc0FycmF5KGluZGV4Um91dGUpKSB7XG4gICAgICAgICAgICAgIHZhciBfbWF0Y2gkcm91dGVzO1xuXG4gICAgICAgICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGluZGV4Um91dGUuZXZlcnkoZnVuY3Rpb24gKHJvdXRlKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuICFyb3V0ZS5wYXRoO1xuICAgICAgICAgICAgICB9KSwgJ0luZGV4IHJvdXRlcyBzaG91bGQgbm90IGhhdmUgcGF0aHMnKSA6IHZvaWQgMDtcbiAgICAgICAgICAgICAgKF9tYXRjaCRyb3V0ZXMgPSBtYXRjaC5yb3V0ZXMpLnB1c2guYXBwbHkoX21hdGNoJHJvdXRlcywgaW5kZXhSb3V0ZSk7XG4gICAgICAgICAgICB9IGVsc2UgaWYgKGluZGV4Um91dGUpIHtcbiAgICAgICAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoIWluZGV4Um91dGUucGF0aCwgJ0luZGV4IHJvdXRlcyBzaG91bGQgbm90IGhhdmUgcGF0aHMnKSA6IHZvaWQgMDtcbiAgICAgICAgICAgICAgbWF0Y2gucm91dGVzLnB1c2goaW5kZXhSb3V0ZSk7XG4gICAgICAgICAgICB9XG5cbiAgICAgICAgICAgIGNhbGxiYWNrKG51bGwsIG1hdGNoKTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuXG4gICAgICAgIHJldHVybiB7XG4gICAgICAgICAgdjogdm9pZCAwXG4gICAgICAgIH07XG4gICAgICB9KCk7XG5cbiAgICAgIGlmICgodHlwZW9mIF9yZXQyID09PSAndW5kZWZpbmVkJyA/ICd1bmRlZmluZWQnIDogX3R5cGVvZihfcmV0MikpID09PSBcIm9iamVjdFwiKSByZXR1cm4gX3JldDIudjtcbiAgICB9XG4gIH1cblxuICBpZiAocmVtYWluaW5nUGF0aG5hbWUgIT0gbnVsbCB8fCByb3V0ZS5jaGlsZFJvdXRlcykge1xuICAgIC8vIEVpdGhlciBhKSB0aGlzIHJvdXRlIG1hdGNoZWQgYXQgbGVhc3Qgc29tZSBvZiB0aGUgcGF0aCBvciBiKVxuICAgIC8vIHdlIGRvbid0IGhhdmUgdG8gbG9hZCB0aGlzIHJvdXRlJ3MgY2hpbGRyZW4gYXN5bmNocm9ub3VzbHkuIEluXG4gICAgLy8gZWl0aGVyIGNhc2UgY29udGludWUgY2hlY2tpbmcgZm9yIG1hdGNoZXMgaW4gdGhlIHN1YnRyZWUuXG4gICAgdmFyIG9uQ2hpbGRSb3V0ZXMgPSBmdW5jdGlvbiBvbkNoaWxkUm91dGVzKGVycm9yLCBjaGlsZFJvdXRlcykge1xuICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICAgIH0gZWxzZSBpZiAoY2hpbGRSb3V0ZXMpIHtcbiAgICAgICAgLy8gQ2hlY2sgdGhlIGNoaWxkIHJvdXRlcyB0byBzZWUgaWYgYW55IG9mIHRoZW0gbWF0Y2guXG4gICAgICAgIG1hdGNoUm91dGVzKGNoaWxkUm91dGVzLCBsb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCBtYXRjaCkge1xuICAgICAgICAgIGlmIChlcnJvcikge1xuICAgICAgICAgICAgY2FsbGJhY2soZXJyb3IpO1xuICAgICAgICAgIH0gZWxzZSBpZiAobWF0Y2gpIHtcbiAgICAgICAgICAgIC8vIEEgY2hpbGQgcm91dGUgbWF0Y2hlZCEgQXVnbWVudCB0aGUgbWF0Y2ggYW5kIHBhc3MgaXQgdXAgdGhlIHN0YWNrLlxuICAgICAgICAgICAgbWF0Y2gucm91dGVzLnVuc2hpZnQocm91dGUpO1xuICAgICAgICAgICAgY2FsbGJhY2sobnVsbCwgbWF0Y2gpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBjYWxsYmFjaygpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSwgcmVtYWluaW5nUGF0aG5hbWUsIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIGNhbGxiYWNrKCk7XG4gICAgICB9XG4gICAgfTtcblxuICAgIHZhciByZXN1bHQgPSBnZXRDaGlsZFJvdXRlcyhyb3V0ZSwgbG9jYXRpb24sIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzLCBvbkNoaWxkUm91dGVzKTtcbiAgICBpZiAocmVzdWx0KSB7XG4gICAgICBvbkNoaWxkUm91dGVzLmFwcGx5KHVuZGVmaW5lZCwgcmVzdWx0KTtcbiAgICB9XG4gIH0gZWxzZSB7XG4gICAgY2FsbGJhY2soKTtcbiAgfVxufVxuXG4vKipcbiAqIEFzeW5jaHJvbm91c2x5IG1hdGNoZXMgdGhlIGdpdmVuIGxvY2F0aW9uIHRvIGEgc2V0IG9mIHJvdXRlcyBhbmQgY2FsbHNcbiAqIGNhbGxiYWNrKGVycm9yLCBzdGF0ZSkgd2hlbiBmaW5pc2hlZC4gVGhlIHN0YXRlIG9iamVjdCB3aWxsIGhhdmUgdGhlXG4gKiBmb2xsb3dpbmcgcHJvcGVydGllczpcbiAqXG4gKiAtIHJvdXRlcyAgICAgICBBbiBhcnJheSBvZiByb3V0ZXMgdGhhdCBtYXRjaGVkLCBpbiBoaWVyYXJjaGljYWwgb3JkZXJcbiAqIC0gcGFyYW1zICAgICAgIEFuIG9iamVjdCBvZiBVUkwgcGFyYW1ldGVyc1xuICpcbiAqIE5vdGU6IFRoaXMgb3BlcmF0aW9uIG1heSBmaW5pc2ggc3luY2hyb25vdXNseSBpZiBubyByb3V0ZXMgaGF2ZSBhblxuICogYXN5bmNocm9ub3VzIGdldENoaWxkUm91dGVzIG1ldGhvZC5cbiAqL1xuZnVuY3Rpb24gbWF0Y2hSb3V0ZXMocm91dGVzLCBsb2NhdGlvbiwgY2FsbGJhY2ssIHJlbWFpbmluZ1BhdGhuYW1lKSB7XG4gIHZhciBwYXJhbU5hbWVzID0gYXJndW1lbnRzLmxlbmd0aCA8PSA0IHx8IGFyZ3VtZW50c1s0XSA9PT0gdW5kZWZpbmVkID8gW10gOiBhcmd1bWVudHNbNF07XG4gIHZhciBwYXJhbVZhbHVlcyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gNSB8fCBhcmd1bWVudHNbNV0gPT09IHVuZGVmaW5lZCA/IFtdIDogYXJndW1lbnRzWzVdO1xuXG4gIGlmIChyZW1haW5pbmdQYXRobmFtZSA9PT0gdW5kZWZpbmVkKSB7XG4gICAgLy8gVE9ETzogVGhpcyBpcyBhIGxpdHRsZSBiaXQgdWdseSwgYnV0IGl0IHdvcmtzIGFyb3VuZCBhIHF1aXJrIGluIGhpc3RvcnlcbiAgICAvLyB0aGF0IHN0cmlwcyB0aGUgbGVhZGluZyBzbGFzaCBmcm9tIHBhdGhuYW1lcyB3aGVuIHVzaW5nIGJhc2VuYW1lcyB3aXRoXG4gICAgLy8gdHJhaWxpbmcgc2xhc2hlcy5cbiAgICBpZiAobG9jYXRpb24ucGF0aG5hbWUuY2hhckF0KDApICE9PSAnLycpIHtcbiAgICAgIGxvY2F0aW9uID0gX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7XG4gICAgICAgIHBhdGhuYW1lOiAnLycgKyBsb2NhdGlvbi5wYXRobmFtZVxuICAgICAgfSk7XG4gICAgfVxuICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gIH1cblxuICAoMCwgX0FzeW5jVXRpbHMubG9vcEFzeW5jKShyb3V0ZXMubGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIG5leHQsIGRvbmUpIHtcbiAgICBtYXRjaFJvdXRlRGVlcChyb3V0ZXNbaW5kZXhdLCBsb2NhdGlvbiwgcmVtYWluaW5nUGF0aG5hbWUsIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzLCBmdW5jdGlvbiAoZXJyb3IsIG1hdGNoKSB7XG4gICAgICBpZiAoZXJyb3IgfHwgbWF0Y2gpIHtcbiAgICAgICAgZG9uZShlcnJvciwgbWF0Y2gpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgbmV4dCgpO1xuICAgICAgfVxuICAgIH0pO1xuICB9LCBjYWxsYmFjayk7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmRlZmF1bHQgPSByb3V0ZXJXYXJuaW5nO1xuZXhwb3J0cy5fcmVzZXRXYXJuZWQgPSBfcmVzZXRXYXJuZWQ7XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgd2FybmVkID0ge307XG5cbmZ1bmN0aW9uIHJvdXRlcldhcm5pbmcoZmFsc2VUb1dhcm4sIG1lc3NhZ2UpIHtcbiAgLy8gT25seSBpc3N1ZSBkZXByZWNhdGlvbiB3YXJuaW5ncyBvbmNlLlxuICBpZiAobWVzc2FnZS5pbmRleE9mKCdkZXByZWNhdGVkJykgIT09IC0xKSB7XG4gICAgaWYgKHdhcm5lZFttZXNzYWdlXSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIHdhcm5lZFttZXNzYWdlXSA9IHRydWU7XG4gIH1cblxuICBtZXNzYWdlID0gJ1tyZWFjdC1yb3V0ZXJdICcgKyBtZXNzYWdlO1xuXG4gIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBhcmdzID0gQXJyYXkoX2xlbiA+IDIgPyBfbGVuIC0gMiA6IDApLCBfa2V5ID0gMjsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgIGFyZ3NbX2tleSAtIDJdID0gYXJndW1lbnRzW19rZXldO1xuICB9XG5cbiAgX3dhcm5pbmcyLmRlZmF1bHQuYXBwbHkodW5kZWZpbmVkLCBbZmFsc2VUb1dhcm4sIG1lc3NhZ2VdLmNvbmNhdChhcmdzKSk7XG59XG5cbmZ1bmN0aW9uIF9yZXNldFdhcm5lZCgpIHtcbiAgd2FybmVkID0ge307XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5kZWZhdWx0ID0gdXNlUm91dGVySGlzdG9yeTtcblxudmFyIF91c2VRdWVyaWVzID0gcmVxdWlyZSgnaGlzdG9yeS9saWIvdXNlUXVlcmllcycpO1xuXG52YXIgX3VzZVF1ZXJpZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUXVlcmllcyk7XG5cbnZhciBfdXNlQmFzZW5hbWUgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi91c2VCYXNlbmFtZScpO1xuXG52YXIgX3VzZUJhc2VuYW1lMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZUJhc2VuYW1lKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gdXNlUm91dGVySGlzdG9yeShjcmVhdGVIaXN0b3J5KSB7XG4gIHJldHVybiBmdW5jdGlvbiAob3B0aW9ucykge1xuICAgIHZhciBoaXN0b3J5ID0gKDAsIF91c2VRdWVyaWVzMi5kZWZhdWx0KSgoMCwgX3VzZUJhc2VuYW1lMi5kZWZhdWx0KShjcmVhdGVIaXN0b3J5KSkob3B0aW9ucyk7XG4gICAgaGlzdG9yeS5fX3YyX2NvbXBhdGlibGVfXyA9IHRydWU7XG4gICAgcmV0dXJuIGhpc3Rvcnk7XG4gIH07XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfdXNlUXVlcmllcyA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL3VzZVF1ZXJpZXMnKTtcblxudmFyIF91c2VRdWVyaWVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZVF1ZXJpZXMpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyID0gcmVxdWlyZSgnLi9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcicpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKG9iaiwga2V5cykgeyB2YXIgdGFyZ2V0ID0ge307IGZvciAodmFyIGkgaW4gb2JqKSB7IGlmIChrZXlzLmluZGV4T2YoaSkgPj0gMCkgY29udGludWU7IGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwgaSkpIGNvbnRpbnVlOyB0YXJnZXRbaV0gPSBvYmpbaV07IH0gcmV0dXJuIHRhcmdldDsgfVxuXG4vKipcbiAqIFJldHVybnMgYSBuZXcgY3JlYXRlSGlzdG9yeSBmdW5jdGlvbiB0aGF0IG1heSBiZSB1c2VkIHRvIGNyZWF0ZVxuICogaGlzdG9yeSBvYmplY3RzIHRoYXQga25vdyBhYm91dCByb3V0aW5nLlxuICpcbiAqIEVuaGFuY2VzIGhpc3Rvcnkgb2JqZWN0cyB3aXRoIHRoZSBmb2xsb3dpbmcgbWV0aG9kczpcbiAqXG4gKiAtIGxpc3RlbigoZXJyb3IsIG5leHRTdGF0ZSkgPT4ge30pXG4gKiAtIGxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZShyb3V0ZSwgKG5leHRMb2NhdGlvbikgPT4ge30pXG4gKiAtIG1hdGNoKGxvY2F0aW9uLCAoZXJyb3IsIHJlZGlyZWN0TG9jYXRpb24sIG5leHRTdGF0ZSkgPT4ge30pXG4gKiAtIGlzQWN0aXZlKHBhdGhuYW1lLCBxdWVyeSwgaW5kZXhPbmx5PWZhbHNlKVxuICovXG5mdW5jdGlvbiB1c2VSb3V0ZXMoY3JlYXRlSGlzdG9yeSkge1xuICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2B1c2VSb3V0ZXNgIGlzIGRlcHJlY2F0ZWQuIFBsZWFzZSB1c2UgYGNyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyYCBpbnN0ZWFkLicpIDogdm9pZCAwO1xuXG4gIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgdmFyIF9yZWYgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcblxuICAgIHZhciByb3V0ZXMgPSBfcmVmLnJvdXRlcztcblxuICAgIHZhciBvcHRpb25zID0gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKF9yZWYsIFsncm91dGVzJ10pO1xuXG4gICAgdmFyIGhpc3RvcnkgPSAoMCwgX3VzZVF1ZXJpZXMyLmRlZmF1bHQpKGNyZWF0ZUhpc3RvcnkpKG9wdGlvbnMpO1xuICAgIHZhciB0cmFuc2l0aW9uTWFuYWdlciA9ICgwLCBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIyLmRlZmF1bHQpKGhpc3RvcnksIHJvdXRlcyk7XG4gICAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG4gIH07XG59XG5cbmV4cG9ydHMuZGVmYXVsdCA9IHVzZVJvdXRlcztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gd2l0aFJvdXRlcjtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2hvaXN0Tm9uUmVhY3RTdGF0aWNzID0gcmVxdWlyZSgnaG9pc3Qtbm9uLXJlYWN0LXN0YXRpY3MnKTtcblxudmFyIF9ob2lzdE5vblJlYWN0U3RhdGljczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9ob2lzdE5vblJlYWN0U3RhdGljcyk7XG5cbnZhciBfUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9Qcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gZ2V0RGlzcGxheU5hbWUoV3JhcHBlZENvbXBvbmVudCkge1xuICByZXR1cm4gV3JhcHBlZENvbXBvbmVudC5kaXNwbGF5TmFtZSB8fCBXcmFwcGVkQ29tcG9uZW50Lm5hbWUgfHwgJ0NvbXBvbmVudCc7XG59XG5cbmZ1bmN0aW9uIHdpdGhSb3V0ZXIoV3JhcHBlZENvbXBvbmVudCwgb3B0aW9ucykge1xuICB2YXIgd2l0aFJlZiA9IG9wdGlvbnMgJiYgb3B0aW9ucy53aXRoUmVmO1xuXG4gIHZhciBXaXRoUm91dGVyID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgICBkaXNwbGF5TmFtZTogJ1dpdGhSb3V0ZXInLFxuXG4gICAgY29udGV4dFR5cGVzOiB7IHJvdXRlcjogX1Byb3BUeXBlcy5yb3V0ZXJTaGFwZSB9LFxuICAgIHByb3BUeXBlczogeyByb3V0ZXI6IF9Qcm9wVHlwZXMucm91dGVyU2hhcGUgfSxcblxuICAgIGdldFdyYXBwZWRJbnN0YW5jZTogZnVuY3Rpb24gZ2V0V3JhcHBlZEluc3RhbmNlKCkge1xuICAgICAgIXdpdGhSZWYgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnVG8gYWNjZXNzIHRoZSB3cmFwcGVkIGluc3RhbmNlLCB5b3UgbmVlZCB0byBzcGVjaWZ5ICcgKyAnYHsgd2l0aFJlZjogdHJ1ZSB9YCBhcyB0aGUgc2Vjb25kIGFyZ3VtZW50IG9mIHRoZSB3aXRoUm91dGVyKCkgY2FsbC4nKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICAgIHJldHVybiB0aGlzLndyYXBwZWRJbnN0YW5jZTtcbiAgICB9LFxuICAgIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgICAgdmFyIF90aGlzID0gdGhpcztcblxuICAgICAgdmFyIHJvdXRlciA9IHRoaXMucHJvcHMucm91dGVyIHx8IHRoaXMuY29udGV4dC5yb3V0ZXI7XG4gICAgICB2YXIgcHJvcHMgPSBfZXh0ZW5kcyh7fSwgdGhpcy5wcm9wcywgeyByb3V0ZXI6IHJvdXRlciB9KTtcblxuICAgICAgaWYgKHdpdGhSZWYpIHtcbiAgICAgICAgcHJvcHMucmVmID0gZnVuY3Rpb24gKGMpIHtcbiAgICAgICAgICBfdGhpcy53cmFwcGVkSW5zdGFuY2UgPSBjO1xuICAgICAgICB9O1xuICAgICAgfVxuXG4gICAgICByZXR1cm4gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUVsZW1lbnQoV3JhcHBlZENvbXBvbmVudCwgcHJvcHMpO1xuICAgIH1cbiAgfSk7XG5cbiAgV2l0aFJvdXRlci5kaXNwbGF5TmFtZSA9ICd3aXRoUm91dGVyKCcgKyBnZXREaXNwbGF5TmFtZShXcmFwcGVkQ29tcG9uZW50KSArICcpJztcbiAgV2l0aFJvdXRlci5XcmFwcGVkQ29tcG9uZW50ID0gV3JhcHBlZENvbXBvbmVudDtcblxuICByZXR1cm4gKDAsIF9ob2lzdE5vblJlYWN0U3RhdGljczIuZGVmYXVsdCkoV2l0aFJvdXRlciwgV3JhcHBlZENvbXBvbmVudCk7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmZ1bmN0aW9uIHRodW5rTWlkZGxld2FyZShfcmVmKSB7XG4gIHZhciBkaXNwYXRjaCA9IF9yZWYuZGlzcGF0Y2g7XG4gIHZhciBnZXRTdGF0ZSA9IF9yZWYuZ2V0U3RhdGU7XG5cbiAgcmV0dXJuIGZ1bmN0aW9uIChuZXh0KSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChhY3Rpb24pIHtcbiAgICAgIHJldHVybiB0eXBlb2YgYWN0aW9uID09PSAnZnVuY3Rpb24nID8gYWN0aW9uKGRpc3BhdGNoLCBnZXRTdGF0ZSkgOiBuZXh0KGFjdGlvbik7XG4gICAgfTtcbiAgfTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSB0aHVua01pZGRsZXdhcmU7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5leHBvcnRzWydkZWZhdWx0J10gPSBhcHBseU1pZGRsZXdhcmU7XG5cbnZhciBfY29tcG9zZSA9IHJlcXVpcmUoJy4vY29tcG9zZScpO1xuXG52YXIgX2NvbXBvc2UyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29tcG9zZSk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxuLyoqXG4gKiBDcmVhdGVzIGEgc3RvcmUgZW5oYW5jZXIgdGhhdCBhcHBsaWVzIG1pZGRsZXdhcmUgdG8gdGhlIGRpc3BhdGNoIG1ldGhvZFxuICogb2YgdGhlIFJlZHV4IHN0b3JlLiBUaGlzIGlzIGhhbmR5IGZvciBhIHZhcmlldHkgb2YgdGFza3MsIHN1Y2ggYXMgZXhwcmVzc2luZ1xuICogYXN5bmNocm9ub3VzIGFjdGlvbnMgaW4gYSBjb25jaXNlIG1hbm5lciwgb3IgbG9nZ2luZyBldmVyeSBhY3Rpb24gcGF5bG9hZC5cbiAqXG4gKiBTZWUgYHJlZHV4LXRodW5rYCBwYWNrYWdlIGFzIGFuIGV4YW1wbGUgb2YgdGhlIFJlZHV4IG1pZGRsZXdhcmUuXG4gKlxuICogQmVjYXVzZSBtaWRkbGV3YXJlIGlzIHBvdGVudGlhbGx5IGFzeW5jaHJvbm91cywgdGhpcyBzaG91bGQgYmUgdGhlIGZpcnN0XG4gKiBzdG9yZSBlbmhhbmNlciBpbiB0aGUgY29tcG9zaXRpb24gY2hhaW4uXG4gKlxuICogTm90ZSB0aGF0IGVhY2ggbWlkZGxld2FyZSB3aWxsIGJlIGdpdmVuIHRoZSBgZGlzcGF0Y2hgIGFuZCBgZ2V0U3RhdGVgIGZ1bmN0aW9uc1xuICogYXMgbmFtZWQgYXJndW1lbnRzLlxuICpcbiAqIEBwYXJhbSB7Li4uRnVuY3Rpb259IG1pZGRsZXdhcmVzIFRoZSBtaWRkbGV3YXJlIGNoYWluIHRvIGJlIGFwcGxpZWQuXG4gKiBAcmV0dXJucyB7RnVuY3Rpb259IEEgc3RvcmUgZW5oYW5jZXIgYXBwbHlpbmcgdGhlIG1pZGRsZXdhcmUuXG4gKi9cbmZ1bmN0aW9uIGFwcGx5TWlkZGxld2FyZSgpIHtcbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIG1pZGRsZXdhcmVzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgbWlkZGxld2FyZXNbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICByZXR1cm4gZnVuY3Rpb24gKGNyZWF0ZVN0b3JlKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChyZWR1Y2VyLCBwcmVsb2FkZWRTdGF0ZSwgZW5oYW5jZXIpIHtcbiAgICAgIHZhciBzdG9yZSA9IGNyZWF0ZVN0b3JlKHJlZHVjZXIsIHByZWxvYWRlZFN0YXRlLCBlbmhhbmNlcik7XG4gICAgICB2YXIgX2Rpc3BhdGNoID0gc3RvcmUuZGlzcGF0Y2g7XG4gICAgICB2YXIgY2hhaW4gPSBbXTtcblxuICAgICAgdmFyIG1pZGRsZXdhcmVBUEkgPSB7XG4gICAgICAgIGdldFN0YXRlOiBzdG9yZS5nZXRTdGF0ZSxcbiAgICAgICAgZGlzcGF0Y2g6IGZ1bmN0aW9uIGRpc3BhdGNoKGFjdGlvbikge1xuICAgICAgICAgIHJldHVybiBfZGlzcGF0Y2goYWN0aW9uKTtcbiAgICAgICAgfVxuICAgICAgfTtcbiAgICAgIGNoYWluID0gbWlkZGxld2FyZXMubWFwKGZ1bmN0aW9uIChtaWRkbGV3YXJlKSB7XG4gICAgICAgIHJldHVybiBtaWRkbGV3YXJlKG1pZGRsZXdhcmVBUEkpO1xuICAgICAgfSk7XG4gICAgICBfZGlzcGF0Y2ggPSBfY29tcG9zZTJbJ2RlZmF1bHQnXS5hcHBseSh1bmRlZmluZWQsIGNoYWluKShzdG9yZS5kaXNwYXRjaCk7XG5cbiAgICAgIHJldHVybiBfZXh0ZW5kcyh7fSwgc3RvcmUsIHtcbiAgICAgICAgZGlzcGF0Y2g6IF9kaXNwYXRjaFxuICAgICAgfSk7XG4gICAgfTtcbiAgfTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzWydkZWZhdWx0J10gPSBiaW5kQWN0aW9uQ3JlYXRvcnM7XG5mdW5jdGlvbiBiaW5kQWN0aW9uQ3JlYXRvcihhY3Rpb25DcmVhdG9yLCBkaXNwYXRjaCkge1xuICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgIHJldHVybiBkaXNwYXRjaChhY3Rpb25DcmVhdG9yLmFwcGx5KHVuZGVmaW5lZCwgYXJndW1lbnRzKSk7XG4gIH07XG59XG5cbi8qKlxuICogVHVybnMgYW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgYWN0aW9uIGNyZWF0b3JzLCBpbnRvIGFuIG9iamVjdCB3aXRoIHRoZVxuICogc2FtZSBrZXlzLCBidXQgd2l0aCBldmVyeSBmdW5jdGlvbiB3cmFwcGVkIGludG8gYSBgZGlzcGF0Y2hgIGNhbGwgc28gdGhleVxuICogbWF5IGJlIGludm9rZWQgZGlyZWN0bHkuIFRoaXMgaXMganVzdCBhIGNvbnZlbmllbmNlIG1ldGhvZCwgYXMgeW91IGNhbiBjYWxsXG4gKiBgc3RvcmUuZGlzcGF0Y2goTXlBY3Rpb25DcmVhdG9ycy5kb1NvbWV0aGluZygpKWAgeW91cnNlbGYganVzdCBmaW5lLlxuICpcbiAqIEZvciBjb252ZW5pZW5jZSwgeW91IGNhbiBhbHNvIHBhc3MgYSBzaW5nbGUgZnVuY3Rpb24gYXMgdGhlIGZpcnN0IGFyZ3VtZW50LFxuICogYW5kIGdldCBhIGZ1bmN0aW9uIGluIHJldHVybi5cbiAqXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufE9iamVjdH0gYWN0aW9uQ3JlYXRvcnMgQW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgYWN0aW9uXG4gKiBjcmVhdG9yIGZ1bmN0aW9ucy4gT25lIGhhbmR5IHdheSB0byBvYnRhaW4gaXQgaXMgdG8gdXNlIEVTNiBgaW1wb3J0ICogYXNgXG4gKiBzeW50YXguIFlvdSBtYXkgYWxzbyBwYXNzIGEgc2luZ2xlIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGRpc3BhdGNoIFRoZSBgZGlzcGF0Y2hgIGZ1bmN0aW9uIGF2YWlsYWJsZSBvbiB5b3VyIFJlZHV4XG4gKiBzdG9yZS5cbiAqXG4gKiBAcmV0dXJucyB7RnVuY3Rpb258T2JqZWN0fSBUaGUgb2JqZWN0IG1pbWlja2luZyB0aGUgb3JpZ2luYWwgb2JqZWN0LCBidXQgd2l0aFxuICogZXZlcnkgYWN0aW9uIGNyZWF0b3Igd3JhcHBlZCBpbnRvIHRoZSBgZGlzcGF0Y2hgIGNhbGwuIElmIHlvdSBwYXNzZWQgYVxuICogZnVuY3Rpb24gYXMgYGFjdGlvbkNyZWF0b3JzYCwgdGhlIHJldHVybiB2YWx1ZSB3aWxsIGFsc28gYmUgYSBzaW5nbGVcbiAqIGZ1bmN0aW9uLlxuICovXG5mdW5jdGlvbiBiaW5kQWN0aW9uQ3JlYXRvcnMoYWN0aW9uQ3JlYXRvcnMsIGRpc3BhdGNoKSB7XG4gIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvcnMgPT09ICdmdW5jdGlvbicpIHtcbiAgICByZXR1cm4gYmluZEFjdGlvbkNyZWF0b3IoYWN0aW9uQ3JlYXRvcnMsIGRpc3BhdGNoKTtcbiAgfVxuXG4gIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvcnMgIT09ICdvYmplY3QnIHx8IGFjdGlvbkNyZWF0b3JzID09PSBudWxsKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdiaW5kQWN0aW9uQ3JlYXRvcnMgZXhwZWN0ZWQgYW4gb2JqZWN0IG9yIGEgZnVuY3Rpb24sIGluc3RlYWQgcmVjZWl2ZWQgJyArIChhY3Rpb25DcmVhdG9ycyA9PT0gbnVsbCA/ICdudWxsJyA6IHR5cGVvZiBhY3Rpb25DcmVhdG9ycykgKyAnLiAnICsgJ0RpZCB5b3Ugd3JpdGUgXCJpbXBvcnQgQWN0aW9uQ3JlYXRvcnMgZnJvbVwiIGluc3RlYWQgb2YgXCJpbXBvcnQgKiBhcyBBY3Rpb25DcmVhdG9ycyBmcm9tXCI/Jyk7XG4gIH1cblxuICB2YXIga2V5cyA9IE9iamVjdC5rZXlzKGFjdGlvbkNyZWF0b3JzKTtcbiAgdmFyIGJvdW5kQWN0aW9uQ3JlYXRvcnMgPSB7fTtcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBrZXlzLmxlbmd0aDsgaSsrKSB7XG4gICAgdmFyIGtleSA9IGtleXNbaV07XG4gICAgdmFyIGFjdGlvbkNyZWF0b3IgPSBhY3Rpb25DcmVhdG9yc1trZXldO1xuICAgIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvciA9PT0gJ2Z1bmN0aW9uJykge1xuICAgICAgYm91bmRBY3Rpb25DcmVhdG9yc1trZXldID0gYmluZEFjdGlvbkNyZWF0b3IoYWN0aW9uQ3JlYXRvciwgZGlzcGF0Y2gpO1xuICAgIH1cbiAgfVxuICByZXR1cm4gYm91bmRBY3Rpb25DcmVhdG9ycztcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzWydkZWZhdWx0J10gPSBjb21iaW5lUmVkdWNlcnM7XG5cbnZhciBfY3JlYXRlU3RvcmUgPSByZXF1aXJlKCcuL2NyZWF0ZVN0b3JlJyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdCA9IHJlcXVpcmUoJ2xvZGFzaC9pc1BsYWluT2JqZWN0Jyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pc1BsYWluT2JqZWN0KTtcblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnLi91dGlscy93YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG5mdW5jdGlvbiBnZXRVbmRlZmluZWRTdGF0ZUVycm9yTWVzc2FnZShrZXksIGFjdGlvbikge1xuICB2YXIgYWN0aW9uVHlwZSA9IGFjdGlvbiAmJiBhY3Rpb24udHlwZTtcbiAgdmFyIGFjdGlvbk5hbWUgPSBhY3Rpb25UeXBlICYmICdcIicgKyBhY3Rpb25UeXBlLnRvU3RyaW5nKCkgKyAnXCInIHx8ICdhbiBhY3Rpb24nO1xuXG4gIHJldHVybiAnR2l2ZW4gYWN0aW9uICcgKyBhY3Rpb25OYW1lICsgJywgcmVkdWNlciBcIicgKyBrZXkgKyAnXCIgcmV0dXJuZWQgdW5kZWZpbmVkLiAnICsgJ1RvIGlnbm9yZSBhbiBhY3Rpb24sIHlvdSBtdXN0IGV4cGxpY2l0bHkgcmV0dXJuIHRoZSBwcmV2aW91cyBzdGF0ZS4nO1xufVxuXG5mdW5jdGlvbiBnZXRVbmV4cGVjdGVkU3RhdGVTaGFwZVdhcm5pbmdNZXNzYWdlKGlucHV0U3RhdGUsIHJlZHVjZXJzLCBhY3Rpb24sIHVuZXhwZWN0ZWRLZXlDYWNoZSkge1xuICB2YXIgcmVkdWNlcktleXMgPSBPYmplY3Qua2V5cyhyZWR1Y2Vycyk7XG4gIHZhciBhcmd1bWVudE5hbWUgPSBhY3Rpb24gJiYgYWN0aW9uLnR5cGUgPT09IF9jcmVhdGVTdG9yZS5BY3Rpb25UeXBlcy5JTklUID8gJ3ByZWxvYWRlZFN0YXRlIGFyZ3VtZW50IHBhc3NlZCB0byBjcmVhdGVTdG9yZScgOiAncHJldmlvdXMgc3RhdGUgcmVjZWl2ZWQgYnkgdGhlIHJlZHVjZXInO1xuXG4gIGlmIChyZWR1Y2VyS2V5cy5sZW5ndGggPT09IDApIHtcbiAgICByZXR1cm4gJ1N0b3JlIGRvZXMgbm90IGhhdmUgYSB2YWxpZCByZWR1Y2VyLiBNYWtlIHN1cmUgdGhlIGFyZ3VtZW50IHBhc3NlZCAnICsgJ3RvIGNvbWJpbmVSZWR1Y2VycyBpcyBhbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGFyZSByZWR1Y2Vycy4nO1xuICB9XG5cbiAgaWYgKCEoMCwgX2lzUGxhaW5PYmplY3QyWydkZWZhdWx0J10pKGlucHV0U3RhdGUpKSB7XG4gICAgcmV0dXJuICdUaGUgJyArIGFyZ3VtZW50TmFtZSArICcgaGFzIHVuZXhwZWN0ZWQgdHlwZSBvZiBcIicgKyB7fS50b1N0cmluZy5jYWxsKGlucHV0U3RhdGUpLm1hdGNoKC9cXHMoW2EtenxBLVpdKykvKVsxXSArICdcIi4gRXhwZWN0ZWQgYXJndW1lbnQgdG8gYmUgYW4gb2JqZWN0IHdpdGggdGhlIGZvbGxvd2luZyAnICsgKCdrZXlzOiBcIicgKyByZWR1Y2VyS2V5cy5qb2luKCdcIiwgXCInKSArICdcIicpO1xuICB9XG5cbiAgdmFyIHVuZXhwZWN0ZWRLZXlzID0gT2JqZWN0LmtleXMoaW5wdXRTdGF0ZSkuZmlsdGVyKGZ1bmN0aW9uIChrZXkpIHtcbiAgICByZXR1cm4gIXJlZHVjZXJzLmhhc093blByb3BlcnR5KGtleSkgJiYgIXVuZXhwZWN0ZWRLZXlDYWNoZVtrZXldO1xuICB9KTtcblxuICB1bmV4cGVjdGVkS2V5cy5mb3JFYWNoKGZ1bmN0aW9uIChrZXkpIHtcbiAgICB1bmV4cGVjdGVkS2V5Q2FjaGVba2V5XSA9IHRydWU7XG4gIH0pO1xuXG4gIGlmICh1bmV4cGVjdGVkS2V5cy5sZW5ndGggPiAwKSB7XG4gICAgcmV0dXJuICdVbmV4cGVjdGVkICcgKyAodW5leHBlY3RlZEtleXMubGVuZ3RoID4gMSA/ICdrZXlzJyA6ICdrZXknKSArICcgJyArICgnXCInICsgdW5leHBlY3RlZEtleXMuam9pbignXCIsIFwiJykgKyAnXCIgZm91bmQgaW4gJyArIGFyZ3VtZW50TmFtZSArICcuICcpICsgJ0V4cGVjdGVkIHRvIGZpbmQgb25lIG9mIHRoZSBrbm93biByZWR1Y2VyIGtleXMgaW5zdGVhZDogJyArICgnXCInICsgcmVkdWNlcktleXMuam9pbignXCIsIFwiJykgKyAnXCIuIFVuZXhwZWN0ZWQga2V5cyB3aWxsIGJlIGlnbm9yZWQuJyk7XG4gIH1cbn1cblxuZnVuY3Rpb24gYXNzZXJ0UmVkdWNlclNhbml0eShyZWR1Y2Vycykge1xuICBPYmplY3Qua2V5cyhyZWR1Y2VycykuZm9yRWFjaChmdW5jdGlvbiAoa2V5KSB7XG4gICAgdmFyIHJlZHVjZXIgPSByZWR1Y2Vyc1trZXldO1xuICAgIHZhciBpbml0aWFsU3RhdGUgPSByZWR1Y2VyKHVuZGVmaW5lZCwgeyB0eXBlOiBfY3JlYXRlU3RvcmUuQWN0aW9uVHlwZXMuSU5JVCB9KTtcblxuICAgIGlmICh0eXBlb2YgaW5pdGlhbFN0YXRlID09PSAndW5kZWZpbmVkJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdSZWR1Y2VyIFwiJyArIGtleSArICdcIiByZXR1cm5lZCB1bmRlZmluZWQgZHVyaW5nIGluaXRpYWxpemF0aW9uLiAnICsgJ0lmIHRoZSBzdGF0ZSBwYXNzZWQgdG8gdGhlIHJlZHVjZXIgaXMgdW5kZWZpbmVkLCB5b3UgbXVzdCAnICsgJ2V4cGxpY2l0bHkgcmV0dXJuIHRoZSBpbml0aWFsIHN0YXRlLiBUaGUgaW5pdGlhbCBzdGF0ZSBtYXkgJyArICdub3QgYmUgdW5kZWZpbmVkLicpO1xuICAgIH1cblxuICAgIHZhciB0eXBlID0gJ0BAcmVkdXgvUFJPQkVfVU5LTk9XTl9BQ1RJT05fJyArIE1hdGgucmFuZG9tKCkudG9TdHJpbmcoMzYpLnN1YnN0cmluZyg3KS5zcGxpdCgnJykuam9pbignLicpO1xuICAgIGlmICh0eXBlb2YgcmVkdWNlcih1bmRlZmluZWQsIHsgdHlwZTogdHlwZSB9KSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignUmVkdWNlciBcIicgKyBrZXkgKyAnXCIgcmV0dXJuZWQgdW5kZWZpbmVkIHdoZW4gcHJvYmVkIHdpdGggYSByYW5kb20gdHlwZS4gJyArICgnRG9uXFwndCB0cnkgdG8gaGFuZGxlICcgKyBfY3JlYXRlU3RvcmUuQWN0aW9uVHlwZXMuSU5JVCArICcgb3Igb3RoZXIgYWN0aW9ucyBpbiBcInJlZHV4LypcIiAnKSArICduYW1lc3BhY2UuIFRoZXkgYXJlIGNvbnNpZGVyZWQgcHJpdmF0ZS4gSW5zdGVhZCwgeW91IG11c3QgcmV0dXJuIHRoZSAnICsgJ2N1cnJlbnQgc3RhdGUgZm9yIGFueSB1bmtub3duIGFjdGlvbnMsIHVubGVzcyBpdCBpcyB1bmRlZmluZWQsICcgKyAnaW4gd2hpY2ggY2FzZSB5b3UgbXVzdCByZXR1cm4gdGhlIGluaXRpYWwgc3RhdGUsIHJlZ2FyZGxlc3Mgb2YgdGhlICcgKyAnYWN0aW9uIHR5cGUuIFRoZSBpbml0aWFsIHN0YXRlIG1heSBub3QgYmUgdW5kZWZpbmVkLicpO1xuICAgIH1cbiAgfSk7XG59XG5cbi8qKlxuICogVHVybnMgYW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgZGlmZmVyZW50IHJlZHVjZXIgZnVuY3Rpb25zLCBpbnRvIGEgc2luZ2xlXG4gKiByZWR1Y2VyIGZ1bmN0aW9uLiBJdCB3aWxsIGNhbGwgZXZlcnkgY2hpbGQgcmVkdWNlciwgYW5kIGdhdGhlciB0aGVpciByZXN1bHRzXG4gKiBpbnRvIGEgc2luZ2xlIHN0YXRlIG9iamVjdCwgd2hvc2Uga2V5cyBjb3JyZXNwb25kIHRvIHRoZSBrZXlzIG9mIHRoZSBwYXNzZWRcbiAqIHJlZHVjZXIgZnVuY3Rpb25zLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSByZWR1Y2VycyBBbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGNvcnJlc3BvbmQgdG8gZGlmZmVyZW50XG4gKiByZWR1Y2VyIGZ1bmN0aW9ucyB0aGF0IG5lZWQgdG8gYmUgY29tYmluZWQgaW50byBvbmUuIE9uZSBoYW5keSB3YXkgdG8gb2J0YWluXG4gKiBpdCBpcyB0byB1c2UgRVM2IGBpbXBvcnQgKiBhcyByZWR1Y2Vyc2Agc3ludGF4LiBUaGUgcmVkdWNlcnMgbWF5IG5ldmVyIHJldHVyblxuICogdW5kZWZpbmVkIGZvciBhbnkgYWN0aW9uLiBJbnN0ZWFkLCB0aGV5IHNob3VsZCByZXR1cm4gdGhlaXIgaW5pdGlhbCBzdGF0ZVxuICogaWYgdGhlIHN0YXRlIHBhc3NlZCB0byB0aGVtIHdhcyB1bmRlZmluZWQsIGFuZCB0aGUgY3VycmVudCBzdGF0ZSBmb3IgYW55XG4gKiB1bnJlY29nbml6ZWQgYWN0aW9uLlxuICpcbiAqIEByZXR1cm5zIHtGdW5jdGlvbn0gQSByZWR1Y2VyIGZ1bmN0aW9uIHRoYXQgaW52b2tlcyBldmVyeSByZWR1Y2VyIGluc2lkZSB0aGVcbiAqIHBhc3NlZCBvYmplY3QsIGFuZCBidWlsZHMgYSBzdGF0ZSBvYmplY3Qgd2l0aCB0aGUgc2FtZSBzaGFwZS5cbiAqL1xuZnVuY3Rpb24gY29tYmluZVJlZHVjZXJzKHJlZHVjZXJzKSB7XG4gIHZhciByZWR1Y2VyS2V5cyA9IE9iamVjdC5rZXlzKHJlZHVjZXJzKTtcbiAgdmFyIGZpbmFsUmVkdWNlcnMgPSB7fTtcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCByZWR1Y2VyS2V5cy5sZW5ndGg7IGkrKykge1xuICAgIHZhciBrZXkgPSByZWR1Y2VyS2V5c1tpXTtcblxuICAgIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICBpZiAodHlwZW9mIHJlZHVjZXJzW2tleV0gPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICAgICgwLCBfd2FybmluZzJbJ2RlZmF1bHQnXSkoJ05vIHJlZHVjZXIgcHJvdmlkZWQgZm9yIGtleSBcIicgKyBrZXkgKyAnXCInKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICBpZiAodHlwZW9mIHJlZHVjZXJzW2tleV0gPT09ICdmdW5jdGlvbicpIHtcbiAgICAgIGZpbmFsUmVkdWNlcnNba2V5XSA9IHJlZHVjZXJzW2tleV07XG4gICAgfVxuICB9XG4gIHZhciBmaW5hbFJlZHVjZXJLZXlzID0gT2JqZWN0LmtleXMoZmluYWxSZWR1Y2Vycyk7XG5cbiAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICB2YXIgdW5leHBlY3RlZEtleUNhY2hlID0ge307XG4gIH1cblxuICB2YXIgc2FuaXR5RXJyb3I7XG4gIHRyeSB7XG4gICAgYXNzZXJ0UmVkdWNlclNhbml0eShmaW5hbFJlZHVjZXJzKTtcbiAgfSBjYXRjaCAoZSkge1xuICAgIHNhbml0eUVycm9yID0gZTtcbiAgfVxuXG4gIHJldHVybiBmdW5jdGlvbiBjb21iaW5hdGlvbigpIHtcbiAgICB2YXIgc3RhdGUgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcbiAgICB2YXIgYWN0aW9uID0gYXJndW1lbnRzWzFdO1xuXG4gICAgaWYgKHNhbml0eUVycm9yKSB7XG4gICAgICB0aHJvdyBzYW5pdHlFcnJvcjtcbiAgICB9XG5cbiAgICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgdmFyIHdhcm5pbmdNZXNzYWdlID0gZ2V0VW5leHBlY3RlZFN0YXRlU2hhcGVXYXJuaW5nTWVzc2FnZShzdGF0ZSwgZmluYWxSZWR1Y2VycywgYWN0aW9uLCB1bmV4cGVjdGVkS2V5Q2FjaGUpO1xuICAgICAgaWYgKHdhcm5pbmdNZXNzYWdlKSB7XG4gICAgICAgICgwLCBfd2FybmluZzJbJ2RlZmF1bHQnXSkod2FybmluZ01lc3NhZ2UpO1xuICAgICAgfVxuICAgIH1cblxuICAgIHZhciBoYXNDaGFuZ2VkID0gZmFsc2U7XG4gICAgdmFyIG5leHRTdGF0ZSA9IHt9O1xuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgZmluYWxSZWR1Y2VyS2V5cy5sZW5ndGg7IGkrKykge1xuICAgICAgdmFyIGtleSA9IGZpbmFsUmVkdWNlcktleXNbaV07XG4gICAgICB2YXIgcmVkdWNlciA9IGZpbmFsUmVkdWNlcnNba2V5XTtcbiAgICAgIHZhciBwcmV2aW91c1N0YXRlRm9yS2V5ID0gc3RhdGVba2V5XTtcbiAgICAgIHZhciBuZXh0U3RhdGVGb3JLZXkgPSByZWR1Y2VyKHByZXZpb3VzU3RhdGVGb3JLZXksIGFjdGlvbik7XG4gICAgICBpZiAodHlwZW9mIG5leHRTdGF0ZUZvcktleSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgICAgdmFyIGVycm9yTWVzc2FnZSA9IGdldFVuZGVmaW5lZFN0YXRlRXJyb3JNZXNzYWdlKGtleSwgYWN0aW9uKTtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKGVycm9yTWVzc2FnZSk7XG4gICAgICB9XG4gICAgICBuZXh0U3RhdGVba2V5XSA9IG5leHRTdGF0ZUZvcktleTtcbiAgICAgIGhhc0NoYW5nZWQgPSBoYXNDaGFuZ2VkIHx8IG5leHRTdGF0ZUZvcktleSAhPT0gcHJldmlvdXNTdGF0ZUZvcktleTtcbiAgICB9XG4gICAgcmV0dXJuIGhhc0NoYW5nZWQgPyBuZXh0U3RhdGUgOiBzdGF0ZTtcbiAgfTtcbn0iLCJcInVzZSBzdHJpY3RcIjtcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gY29tcG9zZTtcbi8qKlxuICogQ29tcG9zZXMgc2luZ2xlLWFyZ3VtZW50IGZ1bmN0aW9ucyBmcm9tIHJpZ2h0IHRvIGxlZnQuIFRoZSByaWdodG1vc3RcbiAqIGZ1bmN0aW9uIGNhbiB0YWtlIG11bHRpcGxlIGFyZ3VtZW50cyBhcyBpdCBwcm92aWRlcyB0aGUgc2lnbmF0dXJlIGZvclxuICogdGhlIHJlc3VsdGluZyBjb21wb3NpdGUgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHsuLi5GdW5jdGlvbn0gZnVuY3MgVGhlIGZ1bmN0aW9ucyB0byBjb21wb3NlLlxuICogQHJldHVybnMge0Z1bmN0aW9ufSBBIGZ1bmN0aW9uIG9idGFpbmVkIGJ5IGNvbXBvc2luZyB0aGUgYXJndW1lbnQgZnVuY3Rpb25zXG4gKiBmcm9tIHJpZ2h0IHRvIGxlZnQuIEZvciBleGFtcGxlLCBjb21wb3NlKGYsIGcsIGgpIGlzIGlkZW50aWNhbCB0byBkb2luZ1xuICogKC4uLmFyZ3MpID0+IGYoZyhoKC4uLmFyZ3MpKSkuXG4gKi9cblxuZnVuY3Rpb24gY29tcG9zZSgpIHtcbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIGZ1bmNzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgZnVuY3NbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICBpZiAoZnVuY3MubGVuZ3RoID09PSAwKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChhcmcpIHtcbiAgICAgIHJldHVybiBhcmc7XG4gICAgfTtcbiAgfVxuXG4gIGlmIChmdW5jcy5sZW5ndGggPT09IDEpIHtcbiAgICByZXR1cm4gZnVuY3NbMF07XG4gIH1cblxuICB2YXIgbGFzdCA9IGZ1bmNzW2Z1bmNzLmxlbmd0aCAtIDFdO1xuICB2YXIgcmVzdCA9IGZ1bmNzLnNsaWNlKDAsIC0xKTtcbiAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICByZXR1cm4gcmVzdC5yZWR1Y2VSaWdodChmdW5jdGlvbiAoY29tcG9zZWQsIGYpIHtcbiAgICAgIHJldHVybiBmKGNvbXBvc2VkKTtcbiAgICB9LCBsYXN0LmFwcGx5KHVuZGVmaW5lZCwgYXJndW1lbnRzKSk7XG4gIH07XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5BY3Rpb25UeXBlcyA9IHVuZGVmaW5lZDtcbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGNyZWF0ZVN0b3JlO1xuXG52YXIgX2lzUGxhaW5PYmplY3QgPSByZXF1aXJlKCdsb2Rhc2gvaXNQbGFpbk9iamVjdCcpO1xuXG52YXIgX2lzUGxhaW5PYmplY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaXNQbGFpbk9iamVjdCk7XG5cbnZhciBfc3ltYm9sT2JzZXJ2YWJsZSA9IHJlcXVpcmUoJ3N5bWJvbC1vYnNlcnZhYmxlJyk7XG5cbnZhciBfc3ltYm9sT2JzZXJ2YWJsZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zeW1ib2xPYnNlcnZhYmxlKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG4vKipcbiAqIFRoZXNlIGFyZSBwcml2YXRlIGFjdGlvbiB0eXBlcyByZXNlcnZlZCBieSBSZWR1eC5cbiAqIEZvciBhbnkgdW5rbm93biBhY3Rpb25zLCB5b3UgbXVzdCByZXR1cm4gdGhlIGN1cnJlbnQgc3RhdGUuXG4gKiBJZiB0aGUgY3VycmVudCBzdGF0ZSBpcyB1bmRlZmluZWQsIHlvdSBtdXN0IHJldHVybiB0aGUgaW5pdGlhbCBzdGF0ZS5cbiAqIERvIG5vdCByZWZlcmVuY2UgdGhlc2UgYWN0aW9uIHR5cGVzIGRpcmVjdGx5IGluIHlvdXIgY29kZS5cbiAqL1xudmFyIEFjdGlvblR5cGVzID0gZXhwb3J0cy5BY3Rpb25UeXBlcyA9IHtcbiAgSU5JVDogJ0BAcmVkdXgvSU5JVCdcbn07XG5cbi8qKlxuICogQ3JlYXRlcyBhIFJlZHV4IHN0b3JlIHRoYXQgaG9sZHMgdGhlIHN0YXRlIHRyZWUuXG4gKiBUaGUgb25seSB3YXkgdG8gY2hhbmdlIHRoZSBkYXRhIGluIHRoZSBzdG9yZSBpcyB0byBjYWxsIGBkaXNwYXRjaCgpYCBvbiBpdC5cbiAqXG4gKiBUaGVyZSBzaG91bGQgb25seSBiZSBhIHNpbmdsZSBzdG9yZSBpbiB5b3VyIGFwcC4gVG8gc3BlY2lmeSBob3cgZGlmZmVyZW50XG4gKiBwYXJ0cyBvZiB0aGUgc3RhdGUgdHJlZSByZXNwb25kIHRvIGFjdGlvbnMsIHlvdSBtYXkgY29tYmluZSBzZXZlcmFsIHJlZHVjZXJzXG4gKiBpbnRvIGEgc2luZ2xlIHJlZHVjZXIgZnVuY3Rpb24gYnkgdXNpbmcgYGNvbWJpbmVSZWR1Y2Vyc2AuXG4gKlxuICogQHBhcmFtIHtGdW5jdGlvbn0gcmVkdWNlciBBIGZ1bmN0aW9uIHRoYXQgcmV0dXJucyB0aGUgbmV4dCBzdGF0ZSB0cmVlLCBnaXZlblxuICogdGhlIGN1cnJlbnQgc3RhdGUgdHJlZSBhbmQgdGhlIGFjdGlvbiB0byBoYW5kbGUuXG4gKlxuICogQHBhcmFtIHthbnl9IFtwcmVsb2FkZWRTdGF0ZV0gVGhlIGluaXRpYWwgc3RhdGUuIFlvdSBtYXkgb3B0aW9uYWxseSBzcGVjaWZ5IGl0XG4gKiB0byBoeWRyYXRlIHRoZSBzdGF0ZSBmcm9tIHRoZSBzZXJ2ZXIgaW4gdW5pdmVyc2FsIGFwcHMsIG9yIHRvIHJlc3RvcmUgYVxuICogcHJldmlvdXNseSBzZXJpYWxpemVkIHVzZXIgc2Vzc2lvbi5cbiAqIElmIHlvdSB1c2UgYGNvbWJpbmVSZWR1Y2Vyc2AgdG8gcHJvZHVjZSB0aGUgcm9vdCByZWR1Y2VyIGZ1bmN0aW9uLCB0aGlzIG11c3QgYmVcbiAqIGFuIG9iamVjdCB3aXRoIHRoZSBzYW1lIHNoYXBlIGFzIGBjb21iaW5lUmVkdWNlcnNgIGtleXMuXG4gKlxuICogQHBhcmFtIHtGdW5jdGlvbn0gZW5oYW5jZXIgVGhlIHN0b3JlIGVuaGFuY2VyLiBZb3UgbWF5IG9wdGlvbmFsbHkgc3BlY2lmeSBpdFxuICogdG8gZW5oYW5jZSB0aGUgc3RvcmUgd2l0aCB0aGlyZC1wYXJ0eSBjYXBhYmlsaXRpZXMgc3VjaCBhcyBtaWRkbGV3YXJlLFxuICogdGltZSB0cmF2ZWwsIHBlcnNpc3RlbmNlLCBldGMuIFRoZSBvbmx5IHN0b3JlIGVuaGFuY2VyIHRoYXQgc2hpcHMgd2l0aCBSZWR1eFxuICogaXMgYGFwcGx5TWlkZGxld2FyZSgpYC5cbiAqXG4gKiBAcmV0dXJucyB7U3RvcmV9IEEgUmVkdXggc3RvcmUgdGhhdCBsZXRzIHlvdSByZWFkIHRoZSBzdGF0ZSwgZGlzcGF0Y2ggYWN0aW9uc1xuICogYW5kIHN1YnNjcmliZSB0byBjaGFuZ2VzLlxuICovXG5mdW5jdGlvbiBjcmVhdGVTdG9yZShyZWR1Y2VyLCBwcmVsb2FkZWRTdGF0ZSwgZW5oYW5jZXIpIHtcbiAgdmFyIF9yZWYyO1xuXG4gIGlmICh0eXBlb2YgcHJlbG9hZGVkU3RhdGUgPT09ICdmdW5jdGlvbicgJiYgdHlwZW9mIGVuaGFuY2VyID09PSAndW5kZWZpbmVkJykge1xuICAgIGVuaGFuY2VyID0gcHJlbG9hZGVkU3RhdGU7XG4gICAgcHJlbG9hZGVkU3RhdGUgPSB1bmRlZmluZWQ7XG4gIH1cblxuICBpZiAodHlwZW9mIGVuaGFuY2VyICE9PSAndW5kZWZpbmVkJykge1xuICAgIGlmICh0eXBlb2YgZW5oYW5jZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgdGhlIGVuaGFuY2VyIHRvIGJlIGEgZnVuY3Rpb24uJyk7XG4gICAgfVxuXG4gICAgcmV0dXJuIGVuaGFuY2VyKGNyZWF0ZVN0b3JlKShyZWR1Y2VyLCBwcmVsb2FkZWRTdGF0ZSk7XG4gIH1cblxuICBpZiAodHlwZW9mIHJlZHVjZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICB0aHJvdyBuZXcgRXJyb3IoJ0V4cGVjdGVkIHRoZSByZWR1Y2VyIHRvIGJlIGEgZnVuY3Rpb24uJyk7XG4gIH1cblxuICB2YXIgY3VycmVudFJlZHVjZXIgPSByZWR1Y2VyO1xuICB2YXIgY3VycmVudFN0YXRlID0gcHJlbG9hZGVkU3RhdGU7XG4gIHZhciBjdXJyZW50TGlzdGVuZXJzID0gW107XG4gIHZhciBuZXh0TGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycztcbiAgdmFyIGlzRGlzcGF0Y2hpbmcgPSBmYWxzZTtcblxuICBmdW5jdGlvbiBlbnN1cmVDYW5NdXRhdGVOZXh0TGlzdGVuZXJzKCkge1xuICAgIGlmIChuZXh0TGlzdGVuZXJzID09PSBjdXJyZW50TGlzdGVuZXJzKSB7XG4gICAgICBuZXh0TGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycy5zbGljZSgpO1xuICAgIH1cbiAgfVxuXG4gIC8qKlxuICAgKiBSZWFkcyB0aGUgc3RhdGUgdHJlZSBtYW5hZ2VkIGJ5IHRoZSBzdG9yZS5cbiAgICpcbiAgICogQHJldHVybnMge2FueX0gVGhlIGN1cnJlbnQgc3RhdGUgdHJlZSBvZiB5b3VyIGFwcGxpY2F0aW9uLlxuICAgKi9cbiAgZnVuY3Rpb24gZ2V0U3RhdGUoKSB7XG4gICAgcmV0dXJuIGN1cnJlbnRTdGF0ZTtcbiAgfVxuXG4gIC8qKlxuICAgKiBBZGRzIGEgY2hhbmdlIGxpc3RlbmVyLiBJdCB3aWxsIGJlIGNhbGxlZCBhbnkgdGltZSBhbiBhY3Rpb24gaXMgZGlzcGF0Y2hlZCxcbiAgICogYW5kIHNvbWUgcGFydCBvZiB0aGUgc3RhdGUgdHJlZSBtYXkgcG90ZW50aWFsbHkgaGF2ZSBjaGFuZ2VkLiBZb3UgbWF5IHRoZW5cbiAgICogY2FsbCBgZ2V0U3RhdGUoKWAgdG8gcmVhZCB0aGUgY3VycmVudCBzdGF0ZSB0cmVlIGluc2lkZSB0aGUgY2FsbGJhY2suXG4gICAqXG4gICAqIFlvdSBtYXkgY2FsbCBgZGlzcGF0Y2goKWAgZnJvbSBhIGNoYW5nZSBsaXN0ZW5lciwgd2l0aCB0aGUgZm9sbG93aW5nXG4gICAqIGNhdmVhdHM6XG4gICAqXG4gICAqIDEuIFRoZSBzdWJzY3JpcHRpb25zIGFyZSBzbmFwc2hvdHRlZCBqdXN0IGJlZm9yZSBldmVyeSBgZGlzcGF0Y2goKWAgY2FsbC5cbiAgICogSWYgeW91IHN1YnNjcmliZSBvciB1bnN1YnNjcmliZSB3aGlsZSB0aGUgbGlzdGVuZXJzIGFyZSBiZWluZyBpbnZva2VkLCB0aGlzXG4gICAqIHdpbGwgbm90IGhhdmUgYW55IGVmZmVjdCBvbiB0aGUgYGRpc3BhdGNoKClgIHRoYXQgaXMgY3VycmVudGx5IGluIHByb2dyZXNzLlxuICAgKiBIb3dldmVyLCB0aGUgbmV4dCBgZGlzcGF0Y2goKWAgY2FsbCwgd2hldGhlciBuZXN0ZWQgb3Igbm90LCB3aWxsIHVzZSBhIG1vcmVcbiAgICogcmVjZW50IHNuYXBzaG90IG9mIHRoZSBzdWJzY3JpcHRpb24gbGlzdC5cbiAgICpcbiAgICogMi4gVGhlIGxpc3RlbmVyIHNob3VsZCBub3QgZXhwZWN0IHRvIHNlZSBhbGwgc3RhdGUgY2hhbmdlcywgYXMgdGhlIHN0YXRlXG4gICAqIG1pZ2h0IGhhdmUgYmVlbiB1cGRhdGVkIG11bHRpcGxlIHRpbWVzIGR1cmluZyBhIG5lc3RlZCBgZGlzcGF0Y2goKWAgYmVmb3JlXG4gICAqIHRoZSBsaXN0ZW5lciBpcyBjYWxsZWQuIEl0IGlzLCBob3dldmVyLCBndWFyYW50ZWVkIHRoYXQgYWxsIHN1YnNjcmliZXJzXG4gICAqIHJlZ2lzdGVyZWQgYmVmb3JlIHRoZSBgZGlzcGF0Y2goKWAgc3RhcnRlZCB3aWxsIGJlIGNhbGxlZCB3aXRoIHRoZSBsYXRlc3RcbiAgICogc3RhdGUgYnkgdGhlIHRpbWUgaXQgZXhpdHMuXG4gICAqXG4gICAqIEBwYXJhbSB7RnVuY3Rpb259IGxpc3RlbmVyIEEgY2FsbGJhY2sgdG8gYmUgaW52b2tlZCBvbiBldmVyeSBkaXNwYXRjaC5cbiAgICogQHJldHVybnMge0Z1bmN0aW9ufSBBIGZ1bmN0aW9uIHRvIHJlbW92ZSB0aGlzIGNoYW5nZSBsaXN0ZW5lci5cbiAgICovXG4gIGZ1bmN0aW9uIHN1YnNjcmliZShsaXN0ZW5lcikge1xuICAgIGlmICh0eXBlb2YgbGlzdGVuZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgbGlzdGVuZXIgdG8gYmUgYSBmdW5jdGlvbi4nKTtcbiAgICB9XG5cbiAgICB2YXIgaXNTdWJzY3JpYmVkID0gdHJ1ZTtcblxuICAgIGVuc3VyZUNhbk11dGF0ZU5leHRMaXN0ZW5lcnMoKTtcbiAgICBuZXh0TGlzdGVuZXJzLnB1c2gobGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uIHVuc3Vic2NyaWJlKCkge1xuICAgICAgaWYgKCFpc1N1YnNjcmliZWQpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuXG4gICAgICBpc1N1YnNjcmliZWQgPSBmYWxzZTtcblxuICAgICAgZW5zdXJlQ2FuTXV0YXRlTmV4dExpc3RlbmVycygpO1xuICAgICAgdmFyIGluZGV4ID0gbmV4dExpc3RlbmVycy5pbmRleE9mKGxpc3RlbmVyKTtcbiAgICAgIG5leHRMaXN0ZW5lcnMuc3BsaWNlKGluZGV4LCAxKTtcbiAgICB9O1xuICB9XG5cbiAgLyoqXG4gICAqIERpc3BhdGNoZXMgYW4gYWN0aW9uLiBJdCBpcyB0aGUgb25seSB3YXkgdG8gdHJpZ2dlciBhIHN0YXRlIGNoYW5nZS5cbiAgICpcbiAgICogVGhlIGByZWR1Y2VyYCBmdW5jdGlvbiwgdXNlZCB0byBjcmVhdGUgdGhlIHN0b3JlLCB3aWxsIGJlIGNhbGxlZCB3aXRoIHRoZVxuICAgKiBjdXJyZW50IHN0YXRlIHRyZWUgYW5kIHRoZSBnaXZlbiBgYWN0aW9uYC4gSXRzIHJldHVybiB2YWx1ZSB3aWxsXG4gICAqIGJlIGNvbnNpZGVyZWQgdGhlICoqbmV4dCoqIHN0YXRlIG9mIHRoZSB0cmVlLCBhbmQgdGhlIGNoYW5nZSBsaXN0ZW5lcnNcbiAgICogd2lsbCBiZSBub3RpZmllZC5cbiAgICpcbiAgICogVGhlIGJhc2UgaW1wbGVtZW50YXRpb24gb25seSBzdXBwb3J0cyBwbGFpbiBvYmplY3QgYWN0aW9ucy4gSWYgeW91IHdhbnQgdG9cbiAgICogZGlzcGF0Y2ggYSBQcm9taXNlLCBhbiBPYnNlcnZhYmxlLCBhIHRodW5rLCBvciBzb21ldGhpbmcgZWxzZSwgeW91IG5lZWQgdG9cbiAgICogd3JhcCB5b3VyIHN0b3JlIGNyZWF0aW5nIGZ1bmN0aW9uIGludG8gdGhlIGNvcnJlc3BvbmRpbmcgbWlkZGxld2FyZS4gRm9yXG4gICAqIGV4YW1wbGUsIHNlZSB0aGUgZG9jdW1lbnRhdGlvbiBmb3IgdGhlIGByZWR1eC10aHVua2AgcGFja2FnZS4gRXZlbiB0aGVcbiAgICogbWlkZGxld2FyZSB3aWxsIGV2ZW50dWFsbHkgZGlzcGF0Y2ggcGxhaW4gb2JqZWN0IGFjdGlvbnMgdXNpbmcgdGhpcyBtZXRob2QuXG4gICAqXG4gICAqIEBwYXJhbSB7T2JqZWN0fSBhY3Rpb24gQSBwbGFpbiBvYmplY3QgcmVwcmVzZW50aW5nIOKAnHdoYXQgY2hhbmdlZOKAnS4gSXQgaXNcbiAgICogYSBnb29kIGlkZWEgdG8ga2VlcCBhY3Rpb25zIHNlcmlhbGl6YWJsZSBzbyB5b3UgY2FuIHJlY29yZCBhbmQgcmVwbGF5IHVzZXJcbiAgICogc2Vzc2lvbnMsIG9yIHVzZSB0aGUgdGltZSB0cmF2ZWxsaW5nIGByZWR1eC1kZXZ0b29sc2AuIEFuIGFjdGlvbiBtdXN0IGhhdmVcbiAgICogYSBgdHlwZWAgcHJvcGVydHkgd2hpY2ggbWF5IG5vdCBiZSBgdW5kZWZpbmVkYC4gSXQgaXMgYSBnb29kIGlkZWEgdG8gdXNlXG4gICAqIHN0cmluZyBjb25zdGFudHMgZm9yIGFjdGlvbiB0eXBlcy5cbiAgICpcbiAgICogQHJldHVybnMge09iamVjdH0gRm9yIGNvbnZlbmllbmNlLCB0aGUgc2FtZSBhY3Rpb24gb2JqZWN0IHlvdSBkaXNwYXRjaGVkLlxuICAgKlxuICAgKiBOb3RlIHRoYXQsIGlmIHlvdSB1c2UgYSBjdXN0b20gbWlkZGxld2FyZSwgaXQgbWF5IHdyYXAgYGRpc3BhdGNoKClgIHRvXG4gICAqIHJldHVybiBzb21ldGhpbmcgZWxzZSAoZm9yIGV4YW1wbGUsIGEgUHJvbWlzZSB5b3UgY2FuIGF3YWl0KS5cbiAgICovXG4gIGZ1bmN0aW9uIGRpc3BhdGNoKGFjdGlvbikge1xuICAgIGlmICghKDAsIF9pc1BsYWluT2JqZWN0MlsnZGVmYXVsdCddKShhY3Rpb24pKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0FjdGlvbnMgbXVzdCBiZSBwbGFpbiBvYmplY3RzLiAnICsgJ1VzZSBjdXN0b20gbWlkZGxld2FyZSBmb3IgYXN5bmMgYWN0aW9ucy4nKTtcbiAgICB9XG5cbiAgICBpZiAodHlwZW9mIGFjdGlvbi50eXBlID09PSAndW5kZWZpbmVkJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdBY3Rpb25zIG1heSBub3QgaGF2ZSBhbiB1bmRlZmluZWQgXCJ0eXBlXCIgcHJvcGVydHkuICcgKyAnSGF2ZSB5b3UgbWlzc3BlbGxlZCBhIGNvbnN0YW50PycpO1xuICAgIH1cblxuICAgIGlmIChpc0Rpc3BhdGNoaW5nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1JlZHVjZXJzIG1heSBub3QgZGlzcGF0Y2ggYWN0aW9ucy4nKTtcbiAgICB9XG5cbiAgICB0cnkge1xuICAgICAgaXNEaXNwYXRjaGluZyA9IHRydWU7XG4gICAgICBjdXJyZW50U3RhdGUgPSBjdXJyZW50UmVkdWNlcihjdXJyZW50U3RhdGUsIGFjdGlvbik7XG4gICAgfSBmaW5hbGx5IHtcbiAgICAgIGlzRGlzcGF0Y2hpbmcgPSBmYWxzZTtcbiAgICB9XG5cbiAgICB2YXIgbGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycyA9IG5leHRMaXN0ZW5lcnM7XG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBsaXN0ZW5lcnMubGVuZ3RoOyBpKyspIHtcbiAgICAgIGxpc3RlbmVyc1tpXSgpO1xuICAgIH1cblxuICAgIHJldHVybiBhY3Rpb247XG4gIH1cblxuICAvKipcbiAgICogUmVwbGFjZXMgdGhlIHJlZHVjZXIgY3VycmVudGx5IHVzZWQgYnkgdGhlIHN0b3JlIHRvIGNhbGN1bGF0ZSB0aGUgc3RhdGUuXG4gICAqXG4gICAqIFlvdSBtaWdodCBuZWVkIHRoaXMgaWYgeW91ciBhcHAgaW1wbGVtZW50cyBjb2RlIHNwbGl0dGluZyBhbmQgeW91IHdhbnQgdG9cbiAgICogbG9hZCBzb21lIG9mIHRoZSByZWR1Y2VycyBkeW5hbWljYWxseS4gWW91IG1pZ2h0IGFsc28gbmVlZCB0aGlzIGlmIHlvdVxuICAgKiBpbXBsZW1lbnQgYSBob3QgcmVsb2FkaW5nIG1lY2hhbmlzbSBmb3IgUmVkdXguXG4gICAqXG4gICAqIEBwYXJhbSB7RnVuY3Rpb259IG5leHRSZWR1Y2VyIFRoZSByZWR1Y2VyIGZvciB0aGUgc3RvcmUgdG8gdXNlIGluc3RlYWQuXG4gICAqIEByZXR1cm5zIHt2b2lkfVxuICAgKi9cbiAgZnVuY3Rpb24gcmVwbGFjZVJlZHVjZXIobmV4dFJlZHVjZXIpIHtcbiAgICBpZiAodHlwZW9mIG5leHRSZWR1Y2VyICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0V4cGVjdGVkIHRoZSBuZXh0UmVkdWNlciB0byBiZSBhIGZ1bmN0aW9uLicpO1xuICAgIH1cblxuICAgIGN1cnJlbnRSZWR1Y2VyID0gbmV4dFJlZHVjZXI7XG4gICAgZGlzcGF0Y2goeyB0eXBlOiBBY3Rpb25UeXBlcy5JTklUIH0pO1xuICB9XG5cbiAgLyoqXG4gICAqIEludGVyb3BlcmFiaWxpdHkgcG9pbnQgZm9yIG9ic2VydmFibGUvcmVhY3RpdmUgbGlicmFyaWVzLlxuICAgKiBAcmV0dXJucyB7b2JzZXJ2YWJsZX0gQSBtaW5pbWFsIG9ic2VydmFibGUgb2Ygc3RhdGUgY2hhbmdlcy5cbiAgICogRm9yIG1vcmUgaW5mb3JtYXRpb24sIHNlZSB0aGUgb2JzZXJ2YWJsZSBwcm9wb3NhbDpcbiAgICogaHR0cHM6Ly9naXRodWIuY29tL3plbnBhcnNpbmcvZXMtb2JzZXJ2YWJsZVxuICAgKi9cbiAgZnVuY3Rpb24gb2JzZXJ2YWJsZSgpIHtcbiAgICB2YXIgX3JlZjtcblxuICAgIHZhciBvdXRlclN1YnNjcmliZSA9IHN1YnNjcmliZTtcbiAgICByZXR1cm4gX3JlZiA9IHtcbiAgICAgIC8qKlxuICAgICAgICogVGhlIG1pbmltYWwgb2JzZXJ2YWJsZSBzdWJzY3JpcHRpb24gbWV0aG9kLlxuICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9ic2VydmVyIEFueSBvYmplY3QgdGhhdCBjYW4gYmUgdXNlZCBhcyBhbiBvYnNlcnZlci5cbiAgICAgICAqIFRoZSBvYnNlcnZlciBvYmplY3Qgc2hvdWxkIGhhdmUgYSBgbmV4dGAgbWV0aG9kLlxuICAgICAgICogQHJldHVybnMge3N1YnNjcmlwdGlvbn0gQW4gb2JqZWN0IHdpdGggYW4gYHVuc3Vic2NyaWJlYCBtZXRob2QgdGhhdCBjYW5cbiAgICAgICAqIGJlIHVzZWQgdG8gdW5zdWJzY3JpYmUgdGhlIG9ic2VydmFibGUgZnJvbSB0aGUgc3RvcmUsIGFuZCBwcmV2ZW50IGZ1cnRoZXJcbiAgICAgICAqIGVtaXNzaW9uIG9mIHZhbHVlcyBmcm9tIHRoZSBvYnNlcnZhYmxlLlxuICAgICAgICovXG4gICAgICBzdWJzY3JpYmU6IGZ1bmN0aW9uIHN1YnNjcmliZShvYnNlcnZlcikge1xuICAgICAgICBpZiAodHlwZW9mIG9ic2VydmVyICE9PSAnb2JqZWN0Jykge1xuICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ0V4cGVjdGVkIHRoZSBvYnNlcnZlciB0byBiZSBhbiBvYmplY3QuJyk7XG4gICAgICAgIH1cblxuICAgICAgICBmdW5jdGlvbiBvYnNlcnZlU3RhdGUoKSB7XG4gICAgICAgICAgaWYgKG9ic2VydmVyLm5leHQpIHtcbiAgICAgICAgICAgIG9ic2VydmVyLm5leHQoZ2V0U3RhdGUoKSk7XG4gICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgb2JzZXJ2ZVN0YXRlKCk7XG4gICAgICAgIHZhciB1bnN1YnNjcmliZSA9IG91dGVyU3Vic2NyaWJlKG9ic2VydmVTdGF0ZSk7XG4gICAgICAgIHJldHVybiB7IHVuc3Vic2NyaWJlOiB1bnN1YnNjcmliZSB9O1xuICAgICAgfVxuICAgIH0sIF9yZWZbX3N5bWJvbE9ic2VydmFibGUyWydkZWZhdWx0J11dID0gZnVuY3Rpb24gKCkge1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfSwgX3JlZjtcbiAgfVxuXG4gIC8vIFdoZW4gYSBzdG9yZSBpcyBjcmVhdGVkLCBhbiBcIklOSVRcIiBhY3Rpb24gaXMgZGlzcGF0Y2hlZCBzbyB0aGF0IGV2ZXJ5XG4gIC8vIHJlZHVjZXIgcmV0dXJucyB0aGVpciBpbml0aWFsIHN0YXRlLiBUaGlzIGVmZmVjdGl2ZWx5IHBvcHVsYXRlc1xuICAvLyB0aGUgaW5pdGlhbCBzdGF0ZSB0cmVlLlxuICBkaXNwYXRjaCh7IHR5cGU6IEFjdGlvblR5cGVzLklOSVQgfSk7XG5cbiAgcmV0dXJuIF9yZWYyID0ge1xuICAgIGRpc3BhdGNoOiBkaXNwYXRjaCxcbiAgICBzdWJzY3JpYmU6IHN1YnNjcmliZSxcbiAgICBnZXRTdGF0ZTogZ2V0U3RhdGUsXG4gICAgcmVwbGFjZVJlZHVjZXI6IHJlcGxhY2VSZWR1Y2VyXG4gIH0sIF9yZWYyW19zeW1ib2xPYnNlcnZhYmxlMlsnZGVmYXVsdCddXSA9IG9ic2VydmFibGUsIF9yZWYyO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuY29tcG9zZSA9IGV4cG9ydHMuYXBwbHlNaWRkbGV3YXJlID0gZXhwb3J0cy5iaW5kQWN0aW9uQ3JlYXRvcnMgPSBleHBvcnRzLmNvbWJpbmVSZWR1Y2VycyA9IGV4cG9ydHMuY3JlYXRlU3RvcmUgPSB1bmRlZmluZWQ7XG5cbnZhciBfY3JlYXRlU3RvcmUgPSByZXF1aXJlKCcuL2NyZWF0ZVN0b3JlJyk7XG5cbnZhciBfY3JlYXRlU3RvcmUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlU3RvcmUpO1xuXG52YXIgX2NvbWJpbmVSZWR1Y2VycyA9IHJlcXVpcmUoJy4vY29tYmluZVJlZHVjZXJzJyk7XG5cbnZhciBfY29tYmluZVJlZHVjZXJzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NvbWJpbmVSZWR1Y2Vycyk7XG5cbnZhciBfYmluZEFjdGlvbkNyZWF0b3JzID0gcmVxdWlyZSgnLi9iaW5kQWN0aW9uQ3JlYXRvcnMnKTtcblxudmFyIF9iaW5kQWN0aW9uQ3JlYXRvcnMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfYmluZEFjdGlvbkNyZWF0b3JzKTtcblxudmFyIF9hcHBseU1pZGRsZXdhcmUgPSByZXF1aXJlKCcuL2FwcGx5TWlkZGxld2FyZScpO1xuXG52YXIgX2FwcGx5TWlkZGxld2FyZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9hcHBseU1pZGRsZXdhcmUpO1xuXG52YXIgX2NvbXBvc2UgPSByZXF1aXJlKCcuL2NvbXBvc2UnKTtcblxudmFyIF9jb21wb3NlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NvbXBvc2UpO1xuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCcuL3V0aWxzL3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbi8qXG4qIFRoaXMgaXMgYSBkdW1teSBmdW5jdGlvbiB0byBjaGVjayBpZiB0aGUgZnVuY3Rpb24gbmFtZSBoYXMgYmVlbiBhbHRlcmVkIGJ5IG1pbmlmaWNhdGlvbi5cbiogSWYgdGhlIGZ1bmN0aW9uIGhhcyBiZWVuIG1pbmlmaWVkIGFuZCBOT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nLCB3YXJuIHRoZSB1c2VyLlxuKi9cbmZ1bmN0aW9uIGlzQ3J1c2hlZCgpIHt9XG5cbmlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nICYmIHR5cGVvZiBpc0NydXNoZWQubmFtZSA9PT0gJ3N0cmluZycgJiYgaXNDcnVzaGVkLm5hbWUgIT09ICdpc0NydXNoZWQnKSB7XG4gICgwLCBfd2FybmluZzJbJ2RlZmF1bHQnXSkoJ1lvdSBhcmUgY3VycmVudGx5IHVzaW5nIG1pbmlmaWVkIGNvZGUgb3V0c2lkZSBvZiBOT0RFX0VOViA9PT0gXFwncHJvZHVjdGlvblxcJy4gJyArICdUaGlzIG1lYW5zIHRoYXQgeW91IGFyZSBydW5uaW5nIGEgc2xvd2VyIGRldmVsb3BtZW50IGJ1aWxkIG9mIFJlZHV4LiAnICsgJ1lvdSBjYW4gdXNlIGxvb3NlLWVudmlmeSAoaHR0cHM6Ly9naXRodWIuY29tL3plcnRvc2gvbG9vc2UtZW52aWZ5KSBmb3IgYnJvd3NlcmlmeSAnICsgJ29yIERlZmluZVBsdWdpbiBmb3Igd2VicGFjayAoaHR0cDovL3N0YWNrb3ZlcmZsb3cuY29tL3F1ZXN0aW9ucy8zMDAzMDAzMSkgJyArICd0byBlbnN1cmUgeW91IGhhdmUgdGhlIGNvcnJlY3QgY29kZSBmb3IgeW91ciBwcm9kdWN0aW9uIGJ1aWxkLicpO1xufVxuXG5leHBvcnRzLmNyZWF0ZVN0b3JlID0gX2NyZWF0ZVN0b3JlMlsnZGVmYXVsdCddO1xuZXhwb3J0cy5jb21iaW5lUmVkdWNlcnMgPSBfY29tYmluZVJlZHVjZXJzMlsnZGVmYXVsdCddO1xuZXhwb3J0cy5iaW5kQWN0aW9uQ3JlYXRvcnMgPSBfYmluZEFjdGlvbkNyZWF0b3JzMlsnZGVmYXVsdCddO1xuZXhwb3J0cy5hcHBseU1pZGRsZXdhcmUgPSBfYXBwbHlNaWRkbGV3YXJlMlsnZGVmYXVsdCddO1xuZXhwb3J0cy5jb21wb3NlID0gX2NvbXBvc2UyWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1snZGVmYXVsdCddID0gd2FybmluZztcbi8qKlxuICogUHJpbnRzIGEgd2FybmluZyBpbiB0aGUgY29uc29sZSBpZiBpdCBleGlzdHMuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd9IG1lc3NhZ2UgVGhlIHdhcm5pbmcgbWVzc2FnZS5cbiAqIEByZXR1cm5zIHt2b2lkfVxuICovXG5mdW5jdGlvbiB3YXJuaW5nKG1lc3NhZ2UpIHtcbiAgLyogZXNsaW50LWRpc2FibGUgbm8tY29uc29sZSAqL1xuICBpZiAodHlwZW9mIGNvbnNvbGUgIT09ICd1bmRlZmluZWQnICYmIHR5cGVvZiBjb25zb2xlLmVycm9yID09PSAnZnVuY3Rpb24nKSB7XG4gICAgY29uc29sZS5lcnJvcihtZXNzYWdlKTtcbiAgfVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWNvbnNvbGUgKi9cbiAgdHJ5IHtcbiAgICAvLyBUaGlzIGVycm9yIHdhcyB0aHJvd24gYXMgYSBjb252ZW5pZW5jZSBzbyB0aGF0IGlmIHlvdSBlbmFibGVcbiAgICAvLyBcImJyZWFrIG9uIGFsbCBleGNlcHRpb25zXCIgaW4geW91ciBjb25zb2xlLFxuICAgIC8vIGl0IHdvdWxkIHBhdXNlIHRoZSBleGVjdXRpb24gYXQgdGhpcyBsaW5lLlxuICAgIHRocm93IG5ldyBFcnJvcihtZXNzYWdlKTtcbiAgICAvKiBlc2xpbnQtZGlzYWJsZSBuby1lbXB0eSAqL1xuICB9IGNhdGNoIChlKSB7fVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWVtcHR5ICovXG59IiwiJ3VzZSBzdHJpY3QnO1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiAoc3RyKSB7XG5cdHJldHVybiBlbmNvZGVVUklDb21wb25lbnQoc3RyKS5yZXBsYWNlKC9bIScoKSpdL2csIGZ1bmN0aW9uIChjKSB7XG5cdFx0cmV0dXJuICclJyArIGMuY2hhckNvZGVBdCgwKS50b1N0cmluZygxNikudG9VcHBlckNhc2UoKTtcblx0fSk7XG59O1xuIiwibW9kdWxlLmV4cG9ydHMgPSByZXF1aXJlKCcuL2xpYi9pbmRleCcpO1xuIiwiJ3VzZSBzdHJpY3QnO1xuXG5PYmplY3QuZGVmaW5lUHJvcGVydHkoZXhwb3J0cywgXCJfX2VzTW9kdWxlXCIsIHtcblx0dmFsdWU6IHRydWVcbn0pO1xuXG52YXIgX3BvbnlmaWxsID0gcmVxdWlyZSgnLi9wb255ZmlsbCcpO1xuXG52YXIgX3BvbnlmaWxsMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3BvbnlmaWxsKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgcm9vdCA9IHVuZGVmaW5lZDsgLyogZ2xvYmFsIHdpbmRvdyAqL1xuXG5pZiAodHlwZW9mIGdsb2JhbCAhPT0gJ3VuZGVmaW5lZCcpIHtcblx0cm9vdCA9IGdsb2JhbDtcbn0gZWxzZSBpZiAodHlwZW9mIHdpbmRvdyAhPT0gJ3VuZGVmaW5lZCcpIHtcblx0cm9vdCA9IHdpbmRvdztcbn1cblxudmFyIHJlc3VsdCA9ICgwLCBfcG9ueWZpbGwyWydkZWZhdWx0J10pKHJvb3QpO1xuZXhwb3J0c1snZGVmYXVsdCddID0gcmVzdWx0OyIsIid1c2Ugc3RyaWN0JztcblxuT2JqZWN0LmRlZmluZVByb3BlcnR5KGV4cG9ydHMsIFwiX19lc01vZHVsZVwiLCB7XG5cdHZhbHVlOiB0cnVlXG59KTtcbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IHN5bWJvbE9ic2VydmFibGVQb255ZmlsbDtcbmZ1bmN0aW9uIHN5bWJvbE9ic2VydmFibGVQb255ZmlsbChyb290KSB7XG5cdHZhciByZXN1bHQ7XG5cdHZhciBfU3ltYm9sID0gcm9vdC5TeW1ib2w7XG5cblx0aWYgKHR5cGVvZiBfU3ltYm9sID09PSAnZnVuY3Rpb24nKSB7XG5cdFx0aWYgKF9TeW1ib2wub2JzZXJ2YWJsZSkge1xuXHRcdFx0cmVzdWx0ID0gX1N5bWJvbC5vYnNlcnZhYmxlO1xuXHRcdH0gZWxzZSB7XG5cdFx0XHRyZXN1bHQgPSBfU3ltYm9sKCdvYnNlcnZhYmxlJyk7XG5cdFx0XHRfU3ltYm9sLm9ic2VydmFibGUgPSByZXN1bHQ7XG5cdFx0fVxuXHR9IGVsc2Uge1xuXHRcdHJlc3VsdCA9ICdAQG9ic2VydmFibGUnO1xuXHR9XG5cblx0cmV0dXJuIHJlc3VsdDtcbn07IiwiXG5leHBvcnRzID0gbW9kdWxlLmV4cG9ydHMgPSB0cmltO1xuXG5mdW5jdGlvbiB0cmltKHN0cil7XG4gIHJldHVybiBzdHIucmVwbGFjZSgvXlxccyp8XFxzKiQvZywgJycpO1xufVxuXG5leHBvcnRzLmxlZnQgPSBmdW5jdGlvbihzdHIpe1xuICByZXR1cm4gc3RyLnJlcGxhY2UoL15cXHMqLywgJycpO1xufTtcblxuZXhwb3J0cy5yaWdodCA9IGZ1bmN0aW9uKHN0cil7XG4gIHJldHVybiBzdHIucmVwbGFjZSgvXFxzKiQvLCAnJyk7XG59O1xuIiwiLyoqXG4gKiBDb3B5cmlnaHQgMjAxNC0yMDE1LCBGYWNlYm9vaywgSW5jLlxuICogQWxsIHJpZ2h0cyByZXNlcnZlZC5cbiAqXG4gKiBUaGlzIHNvdXJjZSBjb2RlIGlzIGxpY2Vuc2VkIHVuZGVyIHRoZSBCU0Qtc3R5bGUgbGljZW5zZSBmb3VuZCBpbiB0aGVcbiAqIExJQ0VOU0UgZmlsZSBpbiB0aGUgcm9vdCBkaXJlY3Rvcnkgb2YgdGhpcyBzb3VyY2UgdHJlZS4gQW4gYWRkaXRpb25hbCBncmFudFxuICogb2YgcGF0ZW50IHJpZ2h0cyBjYW4gYmUgZm91bmQgaW4gdGhlIFBBVEVOVFMgZmlsZSBpbiB0aGUgc2FtZSBkaXJlY3RvcnkuXG4gKi9cblxuJ3VzZSBzdHJpY3QnO1xuXG4vKipcbiAqIFNpbWlsYXIgdG8gaW52YXJpYW50IGJ1dCBvbmx5IGxvZ3MgYSB3YXJuaW5nIGlmIHRoZSBjb25kaXRpb24gaXMgbm90IG1ldC5cbiAqIFRoaXMgY2FuIGJlIHVzZWQgdG8gbG9nIGlzc3VlcyBpbiBkZXZlbG9wbWVudCBlbnZpcm9ubWVudHMgaW4gY3JpdGljYWxcbiAqIHBhdGhzLiBSZW1vdmluZyB0aGUgbG9nZ2luZyBjb2RlIGZvciBwcm9kdWN0aW9uIGVudmlyb25tZW50cyB3aWxsIGtlZXAgdGhlXG4gKiBzYW1lIGxvZ2ljIGFuZCBmb2xsb3cgdGhlIHNhbWUgY29kZSBwYXRocy5cbiAqL1xuXG52YXIgd2FybmluZyA9IGZ1bmN0aW9uKCkge307XG5cbmlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gIHdhcm5pbmcgPSBmdW5jdGlvbihjb25kaXRpb24sIGZvcm1hdCwgYXJncykge1xuICAgIHZhciBsZW4gPSBhcmd1bWVudHMubGVuZ3RoO1xuICAgIGFyZ3MgPSBuZXcgQXJyYXkobGVuID4gMiA/IGxlbiAtIDIgOiAwKTtcbiAgICBmb3IgKHZhciBrZXkgPSAyOyBrZXkgPCBsZW47IGtleSsrKSB7XG4gICAgICBhcmdzW2tleSAtIDJdID0gYXJndW1lbnRzW2tleV07XG4gICAgfVxuICAgIGlmIChmb3JtYXQgPT09IHVuZGVmaW5lZCkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKFxuICAgICAgICAnYHdhcm5pbmcoY29uZGl0aW9uLCBmb3JtYXQsIC4uLmFyZ3MpYCByZXF1aXJlcyBhIHdhcm5pbmcgJyArXG4gICAgICAgICdtZXNzYWdlIGFyZ3VtZW50J1xuICAgICAgKTtcbiAgICB9XG5cbiAgICBpZiAoZm9ybWF0Lmxlbmd0aCA8IDEwIHx8ICgvXltzXFxXXSokLykudGVzdChmb3JtYXQpKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgICdUaGUgd2FybmluZyBmb3JtYXQgc2hvdWxkIGJlIGFibGUgdG8gdW5pcXVlbHkgaWRlbnRpZnkgdGhpcyAnICtcbiAgICAgICAgJ3dhcm5pbmcuIFBsZWFzZSwgdXNlIGEgbW9yZSBkZXNjcmlwdGl2ZSBmb3JtYXQgdGhhbjogJyArIGZvcm1hdFxuICAgICAgKTtcbiAgICB9XG5cbiAgICBpZiAoIWNvbmRpdGlvbikge1xuICAgICAgdmFyIGFyZ0luZGV4ID0gMDtcbiAgICAgIHZhciBtZXNzYWdlID0gJ1dhcm5pbmc6ICcgK1xuICAgICAgICBmb3JtYXQucmVwbGFjZSgvJXMvZywgZnVuY3Rpb24oKSB7XG4gICAgICAgICAgcmV0dXJuIGFyZ3NbYXJnSW5kZXgrK107XG4gICAgICAgIH0pO1xuICAgICAgaWYgKHR5cGVvZiBjb25zb2xlICE9PSAndW5kZWZpbmVkJykge1xuICAgICAgICBjb25zb2xlLmVycm9yKG1lc3NhZ2UpO1xuICAgICAgfVxuICAgICAgdHJ5IHtcbiAgICAgICAgLy8gVGhpcyBlcnJvciB3YXMgdGhyb3duIGFzIGEgY29udmVuaWVuY2Ugc28gdGhhdCB5b3UgY2FuIHVzZSB0aGlzIHN0YWNrXG4gICAgICAgIC8vIHRvIGZpbmQgdGhlIGNhbGxzaXRlIHRoYXQgY2F1c2VkIHRoaXMgd2FybmluZyB0byBmaXJlLlxuICAgICAgICB0aHJvdyBuZXcgRXJyb3IobWVzc2FnZSk7XG4gICAgICB9IGNhdGNoKHgpIHt9XG4gICAgfVxuICB9O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IHdhcm5pbmc7XG4iLCJ2YXIgd2luZG93ICAgICAgICAgICAgICA9IHJlcXVpcmUoJ2dsb2JhbCcpO1xudmFyIE1vY2tYTUxIdHRwUmVxdWVzdCAgPSByZXF1aXJlKCcuL2xpYi9Nb2NrWE1MSHR0cFJlcXVlc3QnKTtcbnZhciByZWFsICAgICAgICAgICAgICAgID0gd2luZG93LlhNTEh0dHBSZXF1ZXN0O1xudmFyIG1vY2sgICAgICAgICAgICAgICAgPSBNb2NrWE1MSHR0cFJlcXVlc3Q7XG5cbi8qKlxuICogTW9jayB1dGlsaXR5XG4gKi9cbm1vZHVsZS5leHBvcnRzID0ge1xuXG5cdFhNTEh0dHBSZXF1ZXN0OiBNb2NrWE1MSHR0cFJlcXVlc3QsXG5cblx0LyoqXG5cdCAqIFJlcGxhY2UgdGhlIG5hdGl2ZSBYSFIgd2l0aCB0aGUgbW9ja2VkIFhIUlxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHNldHVwOiBmdW5jdGlvbigpIHtcblx0XHR3aW5kb3cuWE1MSHR0cFJlcXVlc3QgPSBtb2NrO1xuXHRcdE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVycyA9IFtdO1xuXHRcdHJldHVybiB0aGlzO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBSZXBsYWNlIHRoZSBtb2NrZWQgWEhSIHdpdGggdGhlIG5hdGl2ZSBYSFIgYW5kIHJlbW92ZSBhbnkgaGFuZGxlcnNcblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHR0ZWFyZG93bjogZnVuY3Rpb24oKSB7XG5cdFx0TW9ja1hNTEh0dHBSZXF1ZXN0LmhhbmRsZXJzID0gW107XG5cdFx0d2luZG93LlhNTEh0dHBSZXF1ZXN0ID0gcmVhbDtcblx0XHRyZXR1cm4gdGhpcztcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge3N0cmluZ30gICAgW21ldGhvZF1cblx0ICogQHBhcmFtICAge3N0cmluZ30gICAgW3VybF1cblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRtb2NrOiBmdW5jdGlvbihtZXRob2QsIHVybCwgZm4pIHtcblx0XHR2YXIgaGFuZGxlcjtcblx0XHRpZiAoYXJndW1lbnRzLmxlbmd0aCA9PT0gMykge1xuXHRcdFx0aGFuZGxlciA9IGZ1bmN0aW9uKHJlcSwgcmVzKSB7XG5cdFx0XHRcdGlmIChyZXEubWV0aG9kKCkgPT09IG1ldGhvZCAmJiByZXEudXJsKCkgPT09IHVybCkge1xuXHRcdFx0XHRcdHJldHVybiBmbihyZXEsIHJlcyk7XG5cdFx0XHRcdH1cblx0XHRcdFx0cmV0dXJuIGZhbHNlO1xuXHRcdFx0fTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0aGFuZGxlciA9IG1ldGhvZDtcblx0XHR9XG5cblx0XHRNb2NrWE1MSHR0cFJlcXVlc3QuYWRkSGFuZGxlcihoYW5kbGVyKTtcblxuXHRcdHJldHVybiB0aGlzO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgR0VUIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge1N0cmluZ30gICAgdXJsXG5cdCAqIEBwYXJhbSAgIHtGdW5jdGlvbn0gIGZuXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0Z2V0OiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnR0VUJywgdXJsLCBmbik7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBQT1NUIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge1N0cmluZ30gICAgdXJsXG5cdCAqIEBwYXJhbSAgIHtGdW5jdGlvbn0gIGZuXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0cG9zdDogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ1BPU1QnLCB1cmwsIGZuKTtcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIFBVVCByZXF1ZXN0XG5cdCAqIEBwYXJhbSAgIHtTdHJpbmd9ICAgIHVybFxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHB1dDogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ1BVVCcsIHVybCwgZm4pO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgUEFUQ0ggcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRwYXRjaDogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ1BBVENIJywgdXJsLCBmbik7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBERUxFVEUgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRkZWxldGU6IGZ1bmN0aW9uKHVybCwgZm4pIHtcblx0XHRyZXR1cm4gdGhpcy5tb2NrKCdERUxFVEUnLCB1cmwsIGZuKTtcblx0fVxuXG59O1xuIiwiXG4vKipcbiAqIFRoZSBtb2NrZWQgcmVxdWVzdCBkYXRhXG4gKiBAY29uc3RydWN0b3JcbiAqL1xuZnVuY3Rpb24gTW9ja1JlcXVlc3QoeGhyKSB7XG4gIHRoaXMuX21ldGhvZCAgICA9IHhoci5tZXRob2Q7XG4gIHRoaXMuX3VybCAgICAgICA9IHhoci51cmw7XG4gIHRoaXMuX2hlYWRlcnMgICA9IHt9O1xuICB0aGlzLmhlYWRlcnMoeGhyLl9yZXF1ZXN0SGVhZGVycyk7XG4gIHRoaXMuYm9keSh4aHIuZGF0YSk7XG59XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBtZXRob2RcbiAqIEByZXR1cm5zIHtzdHJpbmd9XG4gKi9cbk1vY2tSZXF1ZXN0LnByb3RvdHlwZS5tZXRob2QgPSBmdW5jdGlvbigpIHtcbiAgcmV0dXJuIHRoaXMuX21ldGhvZDtcbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBVUkxcbiAqIEByZXR1cm5zIHtzdHJpbmd9XG4gKi9cbk1vY2tSZXF1ZXN0LnByb3RvdHlwZS51cmwgPSBmdW5jdGlvbigpIHtcbiAgcmV0dXJuIHRoaXMuX3VybDtcbn07XG5cbi8qKlxuICogR2V0L3NldCBhIEhUVFAgaGVhZGVyXG4gKiBAcGFyYW0gICB7c3RyaW5nfSBuYW1lXG4gKiBAcGFyYW0gICB7c3RyaW5nfSBbdmFsdWVdXG4gKiBAcmV0dXJucyB7c3RyaW5nfHVuZGVmaW5lZHxNb2NrUmVxdWVzdH1cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLmhlYWRlciA9IGZ1bmN0aW9uKG5hbWUsIHZhbHVlKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoID09PSAyKSB7XG4gICAgdGhpcy5faGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldID0gdmFsdWU7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSB8fCBudWxsO1xuICB9XG59O1xuXG4vKipcbiAqIEdldC9zZXQgYWxsIG9mIHRoZSBIVFRQIGhlYWRlcnNcbiAqIEBwYXJhbSAgIHtPYmplY3R9IFtoZWFkZXJzXVxuICogQHJldHVybnMge09iamVjdHxNb2NrUmVxdWVzdH1cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLmhlYWRlcnMgPSBmdW5jdGlvbihoZWFkZXJzKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgZm9yICh2YXIgbmFtZSBpbiBoZWFkZXJzKSB7XG4gICAgICBpZiAoaGVhZGVycy5oYXNPd25Qcm9wZXJ0eShuYW1lKSkge1xuICAgICAgICB0aGlzLmhlYWRlcihuYW1lLCBoZWFkZXJzW25hbWVdKTtcbiAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnM7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBib2R5XG4gKiBAcGFyYW0gICB7c3RyaW5nfSBbYm9keV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8TW9ja1JlcXVlc3R9XG4gKi9cbk1vY2tSZXF1ZXN0LnByb3RvdHlwZS5ib2R5ID0gZnVuY3Rpb24oYm9keSkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCkge1xuICAgIHRoaXMuX2JvZHkgPSBib2R5O1xuICAgIHJldHVybiB0aGlzO1xuICB9IGVsc2Uge1xuICAgIHJldHVybiB0aGlzLl9ib2R5O1xuICB9XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IE1vY2tSZXF1ZXN0O1xuIiwiXG4vKipcbiAqIFRoZSBtb2NrZWQgcmVzcG9uc2UgZGF0YVxuICogQGNvbnN0cnVjdG9yXG4gKi9cbmZ1bmN0aW9uIE1vY2tSZXNwb25zZSgpIHtcbiAgdGhpcy5fc3RhdHVzICAgICAgPSAyMDA7XG4gIHRoaXMuX2hlYWRlcnMgICAgID0ge307XG4gIHRoaXMuX2JvZHkgICAgICAgID0gJyc7XG4gIHRoaXMuX3RpbWVvdXQgICAgID0gZmFsc2U7XG59XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBzdGF0dXNcbiAqIEBwYXJhbSAgIHtudW1iZXJ9IFtjb2RlXVxuICogQHJldHVybnMge251bWJlcnxNb2NrUmVzcG9uc2V9XG4gKi9cbk1vY2tSZXNwb25zZS5wcm90b3R5cGUuc3RhdHVzID0gZnVuY3Rpb24oY29kZSkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCkge1xuICAgIHRoaXMuX3N0YXR1cyA9IGNvZGU7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX3N0YXR1cztcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IGEgSFRUUCBoZWFkZXJcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IG5hbWVcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFt2YWx1ZV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8dW5kZWZpbmVkfE1vY2tSZXNwb25zZX1cbiAqL1xuTW9ja1Jlc3BvbnNlLnByb3RvdHlwZS5oZWFkZXIgPSBmdW5jdGlvbihuYW1lLCB2YWx1ZSkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCA9PT0gMikge1xuICAgIHRoaXMuX2hlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSA9IHZhbHVlO1xuICAgIHJldHVybiB0aGlzO1xuICB9IGVsc2Uge1xuICAgIHJldHVybiB0aGlzLl9oZWFkZXJzW25hbWUudG9Mb3dlckNhc2UoKV0gfHwgbnVsbDtcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IGFsbCBvZiB0aGUgSFRUUCBoZWFkZXJzXG4gKiBAcGFyYW0gICB7T2JqZWN0fSBbaGVhZGVyc11cbiAqIEByZXR1cm5zIHtPYmplY3R8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLmhlYWRlcnMgPSBmdW5jdGlvbihoZWFkZXJzKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgZm9yICh2YXIgbmFtZSBpbiBoZWFkZXJzKSB7XG4gICAgICBpZiAoaGVhZGVycy5oYXNPd25Qcm9wZXJ0eShuYW1lKSkge1xuICAgICAgICB0aGlzLmhlYWRlcihuYW1lLCBoZWFkZXJzW25hbWVdKTtcbiAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnM7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBib2R5XG4gKiBAcGFyYW0gICB7c3RyaW5nfSBbYm9keV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLmJvZHkgPSBmdW5jdGlvbihib2R5KSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fYm9keSA9IGJvZHk7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2JvZHk7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCB0aW1lb3V0XG4gKiBAcGFyYW0gICB7Ym9vbGVhbnxudW1iZXJ9IFt0aW1lb3V0XVxuICogQHJldHVybnMge2Jvb2xlYW58bnVtYmVyfE1vY2tSZXNwb25zZX1cbiAqL1xuTW9ja1Jlc3BvbnNlLnByb3RvdHlwZS50aW1lb3V0ID0gZnVuY3Rpb24odGltZW91dCkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCkge1xuICAgIHRoaXMuX3RpbWVvdXQgPSB0aW1lb3V0O1xuICAgIHJldHVybiB0aGlzO1xuICB9IGVsc2Uge1xuICAgIHJldHVybiB0aGlzLl90aW1lb3V0O1xuICB9XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IE1vY2tSZXNwb25zZTtcbiIsInZhciBNb2NrUmVxdWVzdCAgID0gcmVxdWlyZSgnLi9Nb2NrUmVxdWVzdCcpO1xudmFyIE1vY2tSZXNwb25zZSAgPSByZXF1aXJlKCcuL01vY2tSZXNwb25zZScpO1xuXG52YXIgbm90SW1wbGVtZW50ZWRFcnJvciA9IG5ldyBFcnJvcignVGhpcyBmZWF0dXJlIGhhc25cXCd0IGJlZW4gaW1wbG1lbnRlZCB5ZXQuIFBsZWFzZSBzdWJtaXQgYW4gSXNzdWUgb3IgUHVsbCBSZXF1ZXN0IG9uIEdpdGh1Yi4nKTtcblxuLy9odHRwczovL2RldmVsb3Blci5tb3ppbGxhLm9yZy9lbi1VUy9kb2NzL1dlYi9BUEkvWE1MSHR0cFJlcXVlc3Rcbi8vaHR0cHM6Ly94aHIuc3BlYy53aGF0d2cub3JnL1xuLy9odHRwOi8vd3d3LnczLm9yZy9UUi8yMDA2L1dELVhNTEh0dHBSZXF1ZXN0LTIwMDYwNDA1L1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UICAgICAgICAgICAgID0gMDtcbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9PUEVORUQgICAgICAgICAgICAgPSAxO1xuTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0hFQURFUlNfUkVDRUlWRUQgICA9IDI7XG5Nb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfTE9BRElORyAgICAgICAgICAgID0gMztcbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FICAgICAgICAgICAgICAgPSA0O1xuXG4vKipcbiAqIFRoZSByZXF1ZXN0IGhhbmRsZXJzXG4gKiBAcHJpdmF0ZVxuICogQHR5cGUge0FycmF5fVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMgPSBbXTtcblxuLyoqXG4gKiBBZGQgYSByZXF1ZXN0IGhhbmRsZXJcbiAqIEBwYXJhbSAgIHtmdW5jdGlvbihNb2NrUmVxdWVzdCwgTW9ja1Jlc3BvbnNlKX0gZm5cbiAqIEByZXR1cm5zIHtNb2NrWE1MSHR0cFJlcXVlc3R9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5hZGRIYW5kbGVyID0gZnVuY3Rpb24oZm4pIHtcbiAgTW9ja1hNTEh0dHBSZXF1ZXN0LmhhbmRsZXJzLnB1c2goZm4pO1xuICByZXR1cm4gdGhpcztcbn07XG5cbi8qKlxuICogUmVtb3ZlIGEgcmVxdWVzdCBoYW5kbGVyXG4gKiBAcGFyYW0gICB7ZnVuY3Rpb24oTW9ja1JlcXVlc3QsIE1vY2tSZXNwb25zZSl9IGZuXG4gKiBAcmV0dXJucyB7TW9ja1hNTEh0dHBSZXF1ZXN0fVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QucmVtb3ZlSGFuZGxlciA9IGZ1bmN0aW9uKGZuKSB7XG4gIHRocm93IG5vdEltcGxlbWVudGVkRXJyb3I7XG59O1xuXG4vKipcbiAqIEhhbmRsZSBhIHJlcXVlc3RcbiAqIEBwYXJhbSAgIHtNb2NrUmVxdWVzdH0gcmVxdWVzdFxuICogQHJldHVybnMge01vY2tSZXNwb25zZXxudWxsfVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlID0gZnVuY3Rpb24ocmVxdWVzdCkge1xuXG4gIGZvciAodmFyIGk9MDsgaTxNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMubGVuZ3RoOyArK2kpIHtcblxuICAgIC8vZ2V0IHRoZSBnZW5lcmF0b3IgdG8gY3JlYXRlIGEgcmVzcG9uc2UgdG8gdGhlIHJlcXVlc3RcbiAgICB2YXIgcmVzcG9uc2UgPSBNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnNbaV0ocmVxdWVzdCwgbmV3IE1vY2tSZXNwb25zZSgpKTtcblxuICAgIGlmIChyZXNwb25zZSkge1xuICAgICAgcmV0dXJuIHJlc3BvbnNlO1xuICAgIH1cblxuICB9XG5cbiAgcmV0dXJuIG51bGw7XG59O1xuXG4vKipcbiAqIE1vY2sgWE1MSHR0cFJlcXVlc3RcbiAqIEBjb25zdHJ1Y3RvclxuICovXG5mdW5jdGlvbiBNb2NrWE1MSHR0cFJlcXVlc3QoKSB7XG4gIHRoaXMucmVzZXQoKTtcbiAgdGhpcy50aW1lb3V0ID0gMDtcbn1cblxuLyoqXG4gKiBSZXNldCB0aGUgcmVzcG9uc2UgdmFsdWVzXG4gKiBAcHJpdmF0ZVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnJlc2V0ID0gZnVuY3Rpb24oKSB7XG5cbiAgdGhpcy5fcmVxdWVzdEhlYWRlcnMgID0ge307XG4gIHRoaXMuX3Jlc3BvbnNlSGVhZGVycyA9IHt9O1xuXG4gIHRoaXMuc3RhdHVzICAgICAgID0gMDtcbiAgdGhpcy5zdGF0dXNUZXh0ICAgPSAnJztcblxuICB0aGlzLnJlc3BvbnNlICAgICA9IG51bGw7XG4gIHRoaXMucmVzcG9uc2VUeXBlID0gbnVsbDtcbiAgdGhpcy5yZXNwb25zZVRleHQgPSBudWxsO1xuICB0aGlzLnJlc3BvbnNlWE1MICA9IG51bGw7XG5cbiAgdGhpcy5yZWFkeVN0YXRlICAgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UO1xufTtcblxuLyoqXG4gKiBUcmlnZ2VyIGFuIGV2ZW50XG4gKiBAcGFyYW0gICB7U3RyaW5nfSBldmVudFxuICogQHJldHVybnMge01vY2tYTUxIdHRwUmVxdWVzdH1cbiAqL1xuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS50cmlnZ2VyID0gZnVuY3Rpb24oZXZlbnQpIHtcblxuICBpZiAodGhpcy5vbnJlYWR5c3RhdGVjaGFuZ2UpIHtcbiAgICB0aGlzLm9ucmVhZHlzdGF0ZWNoYW5nZSgpO1xuICB9XG5cbiAgaWYgKHRoaXNbJ29uJytldmVudF0pIHtcbiAgICB0aGlzWydvbicrZXZlbnRdKCk7XG4gIH1cblxuICAvL2l0ZXJhdGUgb3ZlciB0aGUgbGlzdGVuZXJzXG5cbiAgcmV0dXJuIHRoaXM7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLm9wZW4gPSBmdW5jdGlvbihtZXRob2QsIHVybCwgYXN5bmMsIHVzZXIsIHBhc3N3b3JkKSB7XG4gIHRoaXMucmVzZXQoKTtcbiAgdGhpcy5tZXRob2QgICA9IG1ldGhvZDtcbiAgdGhpcy51cmwgICAgICA9IHVybDtcbiAgdGhpcy5hc3luYyAgICA9IGFzeW5jO1xuICB0aGlzLnVzZXIgICAgID0gdXNlcjtcbiAgdGhpcy5wYXNzd29yZCA9IHBhc3N3b3JkO1xuICB0aGlzLmRhdGEgICAgID0gbnVsbDtcbiAgdGhpcy5yZWFkeVN0YXRlID0gTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX09QRU5FRDtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUuc2V0UmVxdWVzdEhlYWRlciA9IGZ1bmN0aW9uKG5hbWUsIHZhbHVlKSB7XG4gIHRoaXMuX3JlcXVlc3RIZWFkZXJzW25hbWVdID0gdmFsdWU7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLm92ZXJyaWRlTWltZVR5cGUgPSBmdW5jdGlvbihtaW1lKSB7XG4gIHRocm93IG5vdEltcGxlbWVudGVkRXJyb3I7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnNlbmQgPSBmdW5jdGlvbihkYXRhKSB7XG4gIHZhciBzZWxmID0gdGhpcztcbiAgdGhpcy5kYXRhID0gZGF0YTtcblxuICBzZWxmLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfTE9BRElORztcblxuICBzZWxmLl9zZW5kVGltZW91dCA9IHNldFRpbWVvdXQoZnVuY3Rpb24oKSB7XG5cbiAgICB2YXIgcmVzcG9uc2UgPSBNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlKG5ldyBNb2NrUmVxdWVzdChzZWxmKSk7XG5cbiAgICBpZiAocmVzcG9uc2UgJiYgcmVzcG9uc2UgaW5zdGFuY2VvZiBNb2NrUmVzcG9uc2UpIHtcblxuICAgICAgdmFyIHRpbWVvdXQgPSByZXNwb25zZS50aW1lb3V0KCk7XG5cbiAgICAgIGlmICh0aW1lb3V0KSB7XG5cbiAgICAgICAgLy90cmlnZ2VyIGEgdGltZW91dCBldmVudCBiZWNhdXNlIHRoZSByZXF1ZXN0IHRpbWVkIG91dCAtIHdhaXQgZm9yIHRoZSB0aW1lb3V0IHRpbWUgYmVjYXVzZSBtYW55IGxpYnMgbGlrZSBqcXVlcnkgYW5kIHN1cGVyYWdlbnQgdXNlIHNldFRpbWVvdXQgdG8gZGV0ZWN0IHRoZSBlcnJvciB0eXBlXG4gICAgICAgIHNlbGYuX3NlbmRUaW1lb3V0ID0gc2V0VGltZW91dChmdW5jdGlvbigpIHtcbiAgICAgICAgICBzZWxmLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfRE9ORTtcbiAgICAgICAgICBzZWxmLnRyaWdnZXIoJ3RpbWVvdXQnKTtcbiAgICAgICAgfSwgdHlwZW9mKHRpbWVvdXQpID09PSAnbnVtYmVyJyA/IHRpbWVvdXQgOiBzZWxmLnRpbWVvdXQrMSk7XG5cbiAgICAgIH0gZWxzZSB7XG5cbiAgICAgICAgLy9tYXAgdGhlIHJlc3BvbnNlIHRvIHRoZSBYSFIgb2JqZWN0XG4gICAgICAgIHNlbGYuc3RhdHVzICAgICAgICAgICAgID0gcmVzcG9uc2Uuc3RhdHVzKCk7XG4gICAgICAgIHNlbGYuX3Jlc3BvbnNlSGVhZGVycyAgID0gcmVzcG9uc2UuaGVhZGVycygpO1xuICAgICAgICBzZWxmLnJlc3BvbnNlVHlwZSAgICAgICA9ICd0ZXh0JztcbiAgICAgICAgc2VsZi5yZXNwb25zZSAgICAgICAgICAgPSByZXNwb25zZS5ib2R5KCk7XG4gICAgICAgIHNlbGYucmVzcG9uc2VUZXh0ICAgICAgID0gcmVzcG9uc2UuYm9keSgpOyAvL1RPRE86IGRldGVjdCBhbiBvYmplY3QgYW5kIHJldHVybiBKU09OLCBkZXRlY3QgWE1MIGFuZCByZXR1cm4gWE1MXG4gICAgICAgIHNlbGYucmVhZHlTdGF0ZSAgICAgICAgID0gTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0RPTkU7XG5cbiAgICAgICAgLy90cmlnZ2VyIGEgbG9hZCBldmVudCBiZWNhdXNlIHRoZSByZXF1ZXN0IHdhcyByZWNlaXZlZFxuICAgICAgICBzZWxmLnRyaWdnZXIoJ2xvYWQnKTtcblxuICAgICAgfVxuXG4gICAgfSBlbHNlIHtcblxuICAgICAgLy90cmlnZ2VyIGFuIGVycm9yIGJlY2F1c2UgdGhlIHJlcXVlc3Qgd2FzIG5vdCBoYW5kbGVkXG4gICAgICBzZWxmLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfRE9ORTtcbiAgICAgIHNlbGYudHJpZ2dlcignZXJyb3InKTtcblxuICAgIH1cblxuICB9LCAwKTtcblxufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5hYm9ydCA9IGZ1bmN0aW9uKCkge1xuICBjbGVhclRpbWVvdXQodGhpcy5fc2VuZFRpbWVvdXQpO1xuXG4gIGlmICh0aGlzLnJlYWR5U3RhdGUgPiBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UICYmIHRoaXMucmVhZHlTdGF0ZSA8IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FKSB7XG4gICAgdGhpcy5yZWFkeVN0YXRlID0gTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX1VOU0VOVDtcbiAgICB0aGlzLnRyaWdnZXIoJ2Fib3J0Jyk7XG4gIH1cblxufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5nZXRBbGxSZXNwb25zZUhlYWRlcnMgPSBmdW5jdGlvbigpIHtcblxuICBpZiAodGhpcy5yZWFkeVN0YXRlIDwgTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0hFQURFUlNfUkVDRUlWRUQpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuXG4gIHZhciBoZWFkZXJzID0gJyc7XG4gIGZvciAodmFyIG5hbWUgaW4gdGhpcy5fcmVzcG9uc2VIZWFkZXJzKSB7XG4gICAgaWYgKHRoaXMuX3Jlc3BvbnNlSGVhZGVycy5oYXNPd25Qcm9wZXJ0eShuYW1lKSkge1xuICAgICAgaGVhZGVycyArPSBuYW1lKyc6ICcrdGhpcy5fcmVzcG9uc2VIZWFkZXJzW25hbWVdKydcXHJcXG4nO1xuICAgIH1cbiAgfVxuXG4gIHJldHVybiBoZWFkZXJzO1xufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5nZXRSZXNwb25zZUhlYWRlciA9IGZ1bmN0aW9uKG5hbWUpIHtcblxuICBpZiAodGhpcy5yZWFkeVN0YXRlIDwgTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0hFQURFUlNfUkVDRUlWRUQpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuXG4gIHJldHVybiB0aGlzLl9yZXNwb25zZUhlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSB8fCBudWxsO1xufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5hZGRFdmVudExpc3RlbmVyID0gZnVuY3Rpb24oZXZlbnQsIGxpc3RlbmVyKSB7XG4gIHRocm93IG5vdEltcGxlbWVudGVkRXJyb3I7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnJlbW92ZUV2ZW50TGlzdGVuZXIgPSBmdW5jdGlvbihldmVudCwgbGlzdGVuZXIpIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbm1vZHVsZS5leHBvcnRzID0gTW9ja1hNTEh0dHBSZXF1ZXN0O1xuIiwiXCJ1c2Ugc3RyaWN0XCI7XG52YXIgd2luZG93ID0gcmVxdWlyZShcImdsb2JhbC93aW5kb3dcIilcbnZhciBpc0Z1bmN0aW9uID0gcmVxdWlyZShcImlzLWZ1bmN0aW9uXCIpXG52YXIgcGFyc2VIZWFkZXJzID0gcmVxdWlyZShcInBhcnNlLWhlYWRlcnNcIilcbnZhciB4dGVuZCA9IHJlcXVpcmUoXCJ4dGVuZFwiKVxuXG5tb2R1bGUuZXhwb3J0cyA9IGNyZWF0ZVhIUlxuY3JlYXRlWEhSLlhNTEh0dHBSZXF1ZXN0ID0gd2luZG93LlhNTEh0dHBSZXF1ZXN0IHx8IG5vb3BcbmNyZWF0ZVhIUi5YRG9tYWluUmVxdWVzdCA9IFwid2l0aENyZWRlbnRpYWxzXCIgaW4gKG5ldyBjcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QoKSkgPyBjcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QgOiB3aW5kb3cuWERvbWFpblJlcXVlc3RcblxuZm9yRWFjaEFycmF5KFtcImdldFwiLCBcInB1dFwiLCBcInBvc3RcIiwgXCJwYXRjaFwiLCBcImhlYWRcIiwgXCJkZWxldGVcIl0sIGZ1bmN0aW9uKG1ldGhvZCkge1xuICAgIGNyZWF0ZVhIUlttZXRob2QgPT09IFwiZGVsZXRlXCIgPyBcImRlbFwiIDogbWV0aG9kXSA9IGZ1bmN0aW9uKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICAgICAgb3B0aW9ucyA9IGluaXRQYXJhbXModXJpLCBvcHRpb25zLCBjYWxsYmFjaylcbiAgICAgICAgb3B0aW9ucy5tZXRob2QgPSBtZXRob2QudG9VcHBlckNhc2UoKVxuICAgICAgICByZXR1cm4gX2NyZWF0ZVhIUihvcHRpb25zKVxuICAgIH1cbn0pXG5cbmZ1bmN0aW9uIGZvckVhY2hBcnJheShhcnJheSwgaXRlcmF0b3IpIHtcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGFycmF5Lmxlbmd0aDsgaSsrKSB7XG4gICAgICAgIGl0ZXJhdG9yKGFycmF5W2ldKVxuICAgIH1cbn1cblxuZnVuY3Rpb24gaXNFbXB0eShvYmope1xuICAgIGZvcih2YXIgaSBpbiBvYmope1xuICAgICAgICBpZihvYmouaGFzT3duUHJvcGVydHkoaSkpIHJldHVybiBmYWxzZVxuICAgIH1cbiAgICByZXR1cm4gdHJ1ZVxufVxuXG5mdW5jdGlvbiBpbml0UGFyYW1zKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICB2YXIgcGFyYW1zID0gdXJpXG5cbiAgICBpZiAoaXNGdW5jdGlvbihvcHRpb25zKSkge1xuICAgICAgICBjYWxsYmFjayA9IG9wdGlvbnNcbiAgICAgICAgaWYgKHR5cGVvZiB1cmkgPT09IFwic3RyaW5nXCIpIHtcbiAgICAgICAgICAgIHBhcmFtcyA9IHt1cmk6dXJpfVxuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcGFyYW1zID0geHRlbmQob3B0aW9ucywge3VyaTogdXJpfSlcbiAgICB9XG5cbiAgICBwYXJhbXMuY2FsbGJhY2sgPSBjYWxsYmFja1xuICAgIHJldHVybiBwYXJhbXNcbn1cblxuZnVuY3Rpb24gY3JlYXRlWEhSKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICBvcHRpb25zID0gaW5pdFBhcmFtcyh1cmksIG9wdGlvbnMsIGNhbGxiYWNrKVxuICAgIHJldHVybiBfY3JlYXRlWEhSKG9wdGlvbnMpXG59XG5cbmZ1bmN0aW9uIF9jcmVhdGVYSFIob3B0aW9ucykge1xuICAgIGlmKHR5cGVvZiBvcHRpb25zLmNhbGxiYWNrID09PSBcInVuZGVmaW5lZFwiKXtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKFwiY2FsbGJhY2sgYXJndW1lbnQgbWlzc2luZ1wiKVxuICAgIH1cblxuICAgIHZhciBjYWxsZWQgPSBmYWxzZVxuICAgIHZhciBjYWxsYmFjayA9IGZ1bmN0aW9uIGNiT25jZShlcnIsIHJlc3BvbnNlLCBib2R5KXtcbiAgICAgICAgaWYoIWNhbGxlZCl7XG4gICAgICAgICAgICBjYWxsZWQgPSB0cnVlXG4gICAgICAgICAgICBvcHRpb25zLmNhbGxiYWNrKGVyciwgcmVzcG9uc2UsIGJvZHkpXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICBmdW5jdGlvbiByZWFkeXN0YXRlY2hhbmdlKCkge1xuICAgICAgICBpZiAoeGhyLnJlYWR5U3RhdGUgPT09IDQpIHtcbiAgICAgICAgICAgIGxvYWRGdW5jKClcbiAgICAgICAgfVxuICAgIH1cblxuICAgIGZ1bmN0aW9uIGdldEJvZHkoKSB7XG4gICAgICAgIC8vIENocm9tZSB3aXRoIHJlcXVlc3RUeXBlPWJsb2IgdGhyb3dzIGVycm9ycyBhcnJvdW5kIHdoZW4gZXZlbiB0ZXN0aW5nIGFjY2VzcyB0byByZXNwb25zZVRleHRcbiAgICAgICAgdmFyIGJvZHkgPSB1bmRlZmluZWRcblxuICAgICAgICBpZiAoeGhyLnJlc3BvbnNlKSB7XG4gICAgICAgICAgICBib2R5ID0geGhyLnJlc3BvbnNlXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBib2R5ID0geGhyLnJlc3BvbnNlVGV4dCB8fCBnZXRYbWwoeGhyKVxuICAgICAgICB9XG5cbiAgICAgICAgaWYgKGlzSnNvbikge1xuICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICBib2R5ID0gSlNPTi5wYXJzZShib2R5KVxuICAgICAgICAgICAgfSBjYXRjaCAoZSkge31cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBib2R5XG4gICAgfVxuXG4gICAgdmFyIGZhaWx1cmVSZXNwb25zZSA9IHtcbiAgICAgICAgICAgICAgICBib2R5OiB1bmRlZmluZWQsXG4gICAgICAgICAgICAgICAgaGVhZGVyczoge30sXG4gICAgICAgICAgICAgICAgc3RhdHVzQ29kZTogMCxcbiAgICAgICAgICAgICAgICBtZXRob2Q6IG1ldGhvZCxcbiAgICAgICAgICAgICAgICB1cmw6IHVyaSxcbiAgICAgICAgICAgICAgICByYXdSZXF1ZXN0OiB4aHJcbiAgICAgICAgICAgIH1cblxuICAgIGZ1bmN0aW9uIGVycm9yRnVuYyhldnQpIHtcbiAgICAgICAgY2xlYXJUaW1lb3V0KHRpbWVvdXRUaW1lcilcbiAgICAgICAgaWYoIShldnQgaW5zdGFuY2VvZiBFcnJvcikpe1xuICAgICAgICAgICAgZXZ0ID0gbmV3IEVycm9yKFwiXCIgKyAoZXZ0IHx8IFwiVW5rbm93biBYTUxIdHRwUmVxdWVzdCBFcnJvclwiKSApXG4gICAgICAgIH1cbiAgICAgICAgZXZ0LnN0YXR1c0NvZGUgPSAwXG4gICAgICAgIHJldHVybiBjYWxsYmFjayhldnQsIGZhaWx1cmVSZXNwb25zZSlcbiAgICB9XG5cbiAgICAvLyB3aWxsIGxvYWQgdGhlIGRhdGEgJiBwcm9jZXNzIHRoZSByZXNwb25zZSBpbiBhIHNwZWNpYWwgcmVzcG9uc2Ugb2JqZWN0XG4gICAgZnVuY3Rpb24gbG9hZEZ1bmMoKSB7XG4gICAgICAgIGlmIChhYm9ydGVkKSByZXR1cm5cbiAgICAgICAgdmFyIHN0YXR1c1xuICAgICAgICBjbGVhclRpbWVvdXQodGltZW91dFRpbWVyKVxuICAgICAgICBpZihvcHRpb25zLnVzZVhEUiAmJiB4aHIuc3RhdHVzPT09dW5kZWZpbmVkKSB7XG4gICAgICAgICAgICAvL0lFOCBDT1JTIEdFVCBzdWNjZXNzZnVsIHJlc3BvbnNlIGRvZXNuJ3QgaGF2ZSBhIHN0YXR1cyBmaWVsZCwgYnV0IGJvZHkgaXMgZmluZVxuICAgICAgICAgICAgc3RhdHVzID0gMjAwXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBzdGF0dXMgPSAoeGhyLnN0YXR1cyA9PT0gMTIyMyA/IDIwNCA6IHhoci5zdGF0dXMpXG4gICAgICAgIH1cbiAgICAgICAgdmFyIHJlc3BvbnNlID0gZmFpbHVyZVJlc3BvbnNlXG4gICAgICAgIHZhciBlcnIgPSBudWxsXG5cbiAgICAgICAgaWYgKHN0YXR1cyAhPT0gMCl7XG4gICAgICAgICAgICByZXNwb25zZSA9IHtcbiAgICAgICAgICAgICAgICBib2R5OiBnZXRCb2R5KCksXG4gICAgICAgICAgICAgICAgc3RhdHVzQ29kZTogc3RhdHVzLFxuICAgICAgICAgICAgICAgIG1ldGhvZDogbWV0aG9kLFxuICAgICAgICAgICAgICAgIGhlYWRlcnM6IHt9LFxuICAgICAgICAgICAgICAgIHVybDogdXJpLFxuICAgICAgICAgICAgICAgIHJhd1JlcXVlc3Q6IHhoclxuICAgICAgICAgICAgfVxuICAgICAgICAgICAgaWYoeGhyLmdldEFsbFJlc3BvbnNlSGVhZGVycyl7IC8vcmVtZW1iZXIgeGhyIGNhbiBpbiBmYWN0IGJlIFhEUiBmb3IgQ09SUyBpbiBJRVxuICAgICAgICAgICAgICAgIHJlc3BvbnNlLmhlYWRlcnMgPSBwYXJzZUhlYWRlcnMoeGhyLmdldEFsbFJlc3BvbnNlSGVhZGVycygpKVxuICAgICAgICAgICAgfVxuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgZXJyID0gbmV3IEVycm9yKFwiSW50ZXJuYWwgWE1MSHR0cFJlcXVlc3QgRXJyb3JcIilcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gY2FsbGJhY2soZXJyLCByZXNwb25zZSwgcmVzcG9uc2UuYm9keSlcbiAgICB9XG5cbiAgICB2YXIgeGhyID0gb3B0aW9ucy54aHIgfHwgbnVsbFxuXG4gICAgaWYgKCF4aHIpIHtcbiAgICAgICAgaWYgKG9wdGlvbnMuY29ycyB8fCBvcHRpb25zLnVzZVhEUikge1xuICAgICAgICAgICAgeGhyID0gbmV3IGNyZWF0ZVhIUi5YRG9tYWluUmVxdWVzdCgpXG4gICAgICAgIH1lbHNle1xuICAgICAgICAgICAgeGhyID0gbmV3IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCgpXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIga2V5XG4gICAgdmFyIGFib3J0ZWRcbiAgICB2YXIgdXJpID0geGhyLnVybCA9IG9wdGlvbnMudXJpIHx8IG9wdGlvbnMudXJsXG4gICAgdmFyIG1ldGhvZCA9IHhoci5tZXRob2QgPSBvcHRpb25zLm1ldGhvZCB8fCBcIkdFVFwiXG4gICAgdmFyIGJvZHkgPSBvcHRpb25zLmJvZHkgfHwgb3B0aW9ucy5kYXRhIHx8IG51bGxcbiAgICB2YXIgaGVhZGVycyA9IHhoci5oZWFkZXJzID0gb3B0aW9ucy5oZWFkZXJzIHx8IHt9XG4gICAgdmFyIHN5bmMgPSAhIW9wdGlvbnMuc3luY1xuICAgIHZhciBpc0pzb24gPSBmYWxzZVxuICAgIHZhciB0aW1lb3V0VGltZXJcblxuICAgIGlmIChcImpzb25cIiBpbiBvcHRpb25zKSB7XG4gICAgICAgIGlzSnNvbiA9IHRydWVcbiAgICAgICAgaGVhZGVyc1tcImFjY2VwdFwiXSB8fCBoZWFkZXJzW1wiQWNjZXB0XCJdIHx8IChoZWFkZXJzW1wiQWNjZXB0XCJdID0gXCJhcHBsaWNhdGlvbi9qc29uXCIpIC8vRG9uJ3Qgb3ZlcnJpZGUgZXhpc3RpbmcgYWNjZXB0IGhlYWRlciBkZWNsYXJlZCBieSB1c2VyXG4gICAgICAgIGlmIChtZXRob2QgIT09IFwiR0VUXCIgJiYgbWV0aG9kICE9PSBcIkhFQURcIikge1xuICAgICAgICAgICAgaGVhZGVyc1tcImNvbnRlbnQtdHlwZVwiXSB8fCBoZWFkZXJzW1wiQ29udGVudC1UeXBlXCJdIHx8IChoZWFkZXJzW1wiQ29udGVudC1UeXBlXCJdID0gXCJhcHBsaWNhdGlvbi9qc29uXCIpIC8vRG9uJ3Qgb3ZlcnJpZGUgZXhpc3RpbmcgYWNjZXB0IGhlYWRlciBkZWNsYXJlZCBieSB1c2VyXG4gICAgICAgICAgICBib2R5ID0gSlNPTi5zdHJpbmdpZnkob3B0aW9ucy5qc29uKVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgeGhyLm9ucmVhZHlzdGF0ZWNoYW5nZSA9IHJlYWR5c3RhdGVjaGFuZ2VcbiAgICB4aHIub25sb2FkID0gbG9hZEZ1bmNcbiAgICB4aHIub25lcnJvciA9IGVycm9yRnVuY1xuICAgIC8vIElFOSBtdXN0IGhhdmUgb25wcm9ncmVzcyBiZSBzZXQgdG8gYSB1bmlxdWUgZnVuY3Rpb24uXG4gICAgeGhyLm9ucHJvZ3Jlc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIC8vIElFIG11c3QgZGllXG4gICAgfVxuICAgIHhoci5vbnRpbWVvdXQgPSBlcnJvckZ1bmNcbiAgICB4aHIub3BlbihtZXRob2QsIHVyaSwgIXN5bmMsIG9wdGlvbnMudXNlcm5hbWUsIG9wdGlvbnMucGFzc3dvcmQpXG4gICAgLy9oYXMgdG8gYmUgYWZ0ZXIgb3BlblxuICAgIGlmKCFzeW5jKSB7XG4gICAgICAgIHhoci53aXRoQ3JlZGVudGlhbHMgPSAhIW9wdGlvbnMud2l0aENyZWRlbnRpYWxzXG4gICAgfVxuICAgIC8vIENhbm5vdCBzZXQgdGltZW91dCB3aXRoIHN5bmMgcmVxdWVzdFxuICAgIC8vIG5vdCBzZXR0aW5nIHRpbWVvdXQgb24gdGhlIHhociBvYmplY3QsIGJlY2F1c2Ugb2Ygb2xkIHdlYmtpdHMgZXRjLiBub3QgaGFuZGxpbmcgdGhhdCBjb3JyZWN0bHlcbiAgICAvLyBib3RoIG5wbSdzIHJlcXVlc3QgYW5kIGpxdWVyeSAxLnggdXNlIHRoaXMga2luZCBvZiB0aW1lb3V0LCBzbyB0aGlzIGlzIGJlaW5nIGNvbnNpc3RlbnRcbiAgICBpZiAoIXN5bmMgJiYgb3B0aW9ucy50aW1lb3V0ID4gMCApIHtcbiAgICAgICAgdGltZW91dFRpbWVyID0gc2V0VGltZW91dChmdW5jdGlvbigpe1xuICAgICAgICAgICAgYWJvcnRlZD10cnVlLy9JRTkgbWF5IHN0aWxsIGNhbGwgcmVhZHlzdGF0ZWNoYW5nZVxuICAgICAgICAgICAgeGhyLmFib3J0KFwidGltZW91dFwiKVxuICAgICAgICAgICAgdmFyIGUgPSBuZXcgRXJyb3IoXCJYTUxIdHRwUmVxdWVzdCB0aW1lb3V0XCIpXG4gICAgICAgICAgICBlLmNvZGUgPSBcIkVUSU1FRE9VVFwiXG4gICAgICAgICAgICBlcnJvckZ1bmMoZSlcbiAgICAgICAgfSwgb3B0aW9ucy50aW1lb3V0IClcbiAgICB9XG5cbiAgICBpZiAoeGhyLnNldFJlcXVlc3RIZWFkZXIpIHtcbiAgICAgICAgZm9yKGtleSBpbiBoZWFkZXJzKXtcbiAgICAgICAgICAgIGlmKGhlYWRlcnMuaGFzT3duUHJvcGVydHkoa2V5KSl7XG4gICAgICAgICAgICAgICAgeGhyLnNldFJlcXVlc3RIZWFkZXIoa2V5LCBoZWFkZXJzW2tleV0pXG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9IGVsc2UgaWYgKG9wdGlvbnMuaGVhZGVycyAmJiAhaXNFbXB0eShvcHRpb25zLmhlYWRlcnMpKSB7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcihcIkhlYWRlcnMgY2Fubm90IGJlIHNldCBvbiBhbiBYRG9tYWluUmVxdWVzdCBvYmplY3RcIilcbiAgICB9XG5cbiAgICBpZiAoXCJyZXNwb25zZVR5cGVcIiBpbiBvcHRpb25zKSB7XG4gICAgICAgIHhoci5yZXNwb25zZVR5cGUgPSBvcHRpb25zLnJlc3BvbnNlVHlwZVxuICAgIH1cblxuICAgIGlmIChcImJlZm9yZVNlbmRcIiBpbiBvcHRpb25zICYmXG4gICAgICAgIHR5cGVvZiBvcHRpb25zLmJlZm9yZVNlbmQgPT09IFwiZnVuY3Rpb25cIlxuICAgICkge1xuICAgICAgICBvcHRpb25zLmJlZm9yZVNlbmQoeGhyKVxuICAgIH1cblxuICAgIHhoci5zZW5kKGJvZHkpXG5cbiAgICByZXR1cm4geGhyXG5cblxufVxuXG5mdW5jdGlvbiBnZXRYbWwoeGhyKSB7XG4gICAgaWYgKHhoci5yZXNwb25zZVR5cGUgPT09IFwiZG9jdW1lbnRcIikge1xuICAgICAgICByZXR1cm4geGhyLnJlc3BvbnNlWE1MXG4gICAgfVxuICAgIHZhciBmaXJlZm94QnVnVGFrZW5FZmZlY3QgPSB4aHIuc3RhdHVzID09PSAyMDQgJiYgeGhyLnJlc3BvbnNlWE1MICYmIHhoci5yZXNwb25zZVhNTC5kb2N1bWVudEVsZW1lbnQubm9kZU5hbWUgPT09IFwicGFyc2VyZXJyb3JcIlxuICAgIGlmICh4aHIucmVzcG9uc2VUeXBlID09PSBcIlwiICYmICFmaXJlZm94QnVnVGFrZW5FZmZlY3QpIHtcbiAgICAgICAgcmV0dXJuIHhoci5yZXNwb25zZVhNTFxuICAgIH1cblxuICAgIHJldHVybiBudWxsXG59XG5cbmZ1bmN0aW9uIG5vb3AoKSB7fVxuIiwibW9kdWxlLmV4cG9ydHMgPSBleHRlbmRcblxudmFyIGhhc093blByb3BlcnR5ID0gT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eTtcblxuZnVuY3Rpb24gZXh0ZW5kKCkge1xuICAgIHZhciB0YXJnZXQgPSB7fVxuXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXVxuXG4gICAgICAgIGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHtcbiAgICAgICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkge1xuICAgICAgICAgICAgICAgIHRhcmdldFtrZXldID0gc291cmNlW2tleV1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH1cblxuICAgIHJldHVybiB0YXJnZXRcbn1cbiIsIi8qZXNsaW50IG5vLXNoYWRvdzogWzIsIHsgXCJhbGxvd1wiOiBbXCJkaXNwYXRjaFwiXSB9XSovXG5pbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcbmltcG9ydCBtYXBwaW5nVG9Kc29uTGRSbWwgZnJvbSBcIi4vdXRpbC9tYXBwaW5nVG9Kc29uTGRSbWxcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24gYWN0aW9uc01ha2VyKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSB7XG4gIC8vYmluZCB0byB2YXJpYWJsZSBzbyBhbiBhY3Rpb24gY2FuIHRyaWdnZXIgb3RoZXIgYWN0aW9uc1xuICBsZXQgYWN0aW9ucyA9IHtcbiAgICBvblRva2VuOiBmdW5jdGlvbiAodG9rZW4pIHtcbiAgICAgIHhocihwcm9jZXNzLmVudi5zZXJ2ZXIgKyBcIi92Mi4xL3N5c3RlbS91c2Vycy9tZS92cmVzXCIsIHtcbiAgICAgICAgaGVhZGVyczoge1xuICAgICAgICAgIFwiQXV0aG9yaXphdGlvblwiOiB0b2tlblxuICAgICAgICB9XG4gICAgICB9LCAoZXJyLCByZXNwLCBib2R5KSA9PiB7XG4gICAgICAgIGNvbnN0IHZyZURhdGEgPSBKU09OLnBhcnNlKGJvZHkpO1xuICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJMT0dJTlwiLCBkYXRhOiB0b2tlbiwgdnJlRGF0YTogdnJlRGF0YX0pO1xuICAgICAgICBpZiAodnJlRGF0YS5taW5lKSB7XG4gICAgICAgICAgbmF2aWdhdGVUbyhcImNvbGxlY3Rpb25zT3ZlcnZpZXdcIik7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH0sXG4gICAgb25Mb2FkTW9yZUNsaWNrOiBmdW5jdGlvbiAodXJsLCBjb2xsZWN0aW9uKSB7XG4gICAgICBkaXNwYXRjaCgoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG4gICAgICAgIHZhciBzdGF0ZSA9IGdldFN0YXRlKCk7XG4gICAgICAgIHZhciBwYXlsb2FkID0ge1xuICAgICAgICAgIGhlYWRlcnM6IHtcbiAgICAgICAgICAgIFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcbiAgICAgICAgICB9XG4gICAgICAgIH07XG4gICAgICAgIHhoci5nZXQodXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwLCBib2R5KSB7XG4gICAgICAgICAgaWYgKGVycikge1xuICAgICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlcnIgfSlcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX1NVQ0NFRURFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBkYXRhOiBKU09OLnBhcnNlKGJvZHkpfSk7XG4gICAgICAgICAgICB9IGNhdGNoKGUpIHtcbiAgICAgICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlIH0pXG4gICAgICAgICAgICB9XG4gICAgICAgICAgfVxuICAgICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR19GSU5JU0hFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9ufSlcbiAgICAgICAgfSk7XG4gICAgICB9KVxuXG4gICAgfSxcbiAgICBvblVwbG9hZEZpbGVTZWxlY3Q6IGZ1bmN0aW9uIChmaWxlcywgaXNSZXVwbG9hZCA9IGZhbHNlKSB7XG4gICAgICBsZXQgZmlsZSA9IGZpbGVzWzBdO1xuICAgICAgbGV0IGZvcm1EYXRhID0gbmV3IEZvcm1EYXRhKCk7XG4gICAgICBmb3JtRGF0YS5hcHBlbmQoXCJmaWxlXCIsIGZpbGUpO1xuICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiU1RBUlRfVVBMT0FEXCJ9KVxuICAgICAgZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuICAgICAgICB2YXIgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuICAgICAgICB2YXIgcGF5bG9hZCA9IHtcbiAgICAgICAgICBib2R5OiBmb3JtRGF0YSxcbiAgICAgICAgICBoZWFkZXJzOiB7XG4gICAgICAgICAgICBcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkXG4gICAgICAgICAgfVxuICAgICAgICB9O1xuICAgICAgICB4aHIucG9zdChwcm9jZXNzLmVudi5zZXJ2ZXIgKyBcIi92Mi4xL2J1bGstdXBsb2FkXCIsIHBheWxvYWQsIGZ1bmN0aW9uIChlcnIsIHJlc3ApIHtcbiAgICAgICAgICBsZXQgbG9jYXRpb24gPSByZXNwLmhlYWRlcnMubG9jYXRpb247XG4gICAgICAgICAgeGhyLmdldChsb2NhdGlvbiwge2hlYWRlcnM6IHtcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkfX0sIGZ1bmN0aW9uIChlcnIsIHJlc3AsIGJvZHkpIHtcbiAgICAgICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkZJTklTSF9VUExPQURcIiwgZGF0YTogSlNPTi5wYXJzZShib2R5KSwgdXBsb2FkZWRGaWxlTmFtZTogZmlsZS5uYW1lfSk7XG4gICAgICAgICAgICBpZiAoaXNSZXVwbG9hZCkge1xuICAgICAgICAgICAgICBhY3Rpb25zLm9uU2VsZWN0Q29sbGVjdGlvbihzdGF0ZS5pbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24pO1xuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgbmF2aWdhdGVUbyhcIm1hcEFyY2hldHlwZXNcIik7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgfSk7XG4gICAgICAgIH0pO1xuICAgICAgfSk7XG4gICAgfSxcbiAgICBvbkNvbnRpbnVlTWFwcGluZzogZnVuY3Rpb24gKHZyZUlkKSB7XG4gICAgICBkaXNwYXRjaChmdW5jdGlvbihkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcbiAgICAgICAgdmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcbiAgICAgICAgeGhyLmdldChzdGF0ZS51c2VyZGF0YS5teVZyZXNbdnJlSWRdLnJtbFVyaSwge2hlYWRlcnM6IHtcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkfX0sIGZ1bmN0aW9uIChlcnIsIHJlc3AsIGJvZHkpIHtcbiAgICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJGSU5JU0hfVVBMT0FEXCIsIGRhdGE6IEpTT04ucGFyc2UoYm9keSl9KTtcbiAgICAgICAgICBuYXZpZ2F0ZVRvKFwibWFwQXJjaGV0eXBlc1wiKTtcbiAgICAgICAgfSk7XG4gICAgICB9KTtcbiAgICB9LFxuLypcdFx0b25TYXZlTWFwcGluZ3M6IGZ1bmN0aW9uICgpIHtcbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlNBVkVfU1RBUlRFRFwifSlcbiAgICAgIGRpc3BhdGNoKGZ1bmN0aW9uIChkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcbiAgICAgICAgdmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcbiAgICAgICAgdmFyIHBheWxvYWQgPSB7XG4gICAgICAgICAganNvbjogbWFwcGluZ1RvSnNvbkxkUm1sKHN0YXRlLm1hcHBpbmdzLCBzdGF0ZS5pbXBvcnREYXRhLnZyZSksXG4gICAgICAgICAgaGVhZGVyczoge1xuICAgICAgICAgICAgXCJBdXRob3JpemF0aW9uXCI6IHN0YXRlLnVzZXJkYXRhLnVzZXJJZFxuICAgICAgICAgIH1cbiAgICAgICAgfTtcblxuICAgICAgICB4aHIucG9zdChzdGF0ZS5pbXBvcnREYXRhLnNhdmVNYXBwaW5nVXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwKSB7XG4gICAgICAgICAgaWYgKGVycikge1xuICAgICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9IQURfRVJST1JcIn0pXG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlNBVkVfU1VDQ0VFREVEXCJ9KVxuICAgICAgICAgIH1cbiAgICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJTQVZFX0ZJTklTSEVEXCJ9KVxuICAgICAgICB9KTtcbiAgICAgIH0pO1xuICAgIH0sKi9cblxuICAgIG9uUHVibGlzaERhdGE6IGZ1bmN0aW9uICgpe1xuICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9TVEFSVEVEXCJ9KTtcbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlNBVkVfRklOSVNIRURcIn0pO1xuICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9TVEFSVEVEXCJ9KTtcbiAgICAgIGRpc3BhdGNoKGZ1bmN0aW9uIChkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcbiAgICAgICAgdmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcbiAgICAgICAgdmFyIGpzb25MZCA9IG1hcHBpbmdUb0pzb25MZFJtbChzdGF0ZS5tYXBwaW5ncywgc3RhdGUuaW1wb3J0RGF0YS52cmUpO1xuICAgICAgICBjb25zb2xlLmxvZyhqc29uTGQpO1xuICAgICAgICB2YXIgcGF5bG9hZCA9IHtcbiAgICAgICAgICBib2R5OiBKU09OLnN0cmluZ2lmeShqc29uTGQpLFxuICAgICAgICAgIGhlYWRlcnM6IHtcbiAgICAgICAgICAgIFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWQsXG4gICAgICAgICAgICBcIkNvbnRlbnQtdHlwZVwiOiBcImFwcGxpY2F0aW9uL2xkK2pzb25cIlxuICAgICAgICAgIH1cbiAgICAgICAgfTtcblxuICAgICAgICB4aHIucG9zdChzdGF0ZS5pbXBvcnREYXRhLmV4ZWN1dGVNYXBwaW5nVXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwKSB7XG4gICAgICAgICAgaWYgKGVycikge1xuICAgICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9IQURfRVJST1JcIn0pXG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGlmIChKU09OLnBhcnNlKHJlc3AuYm9keSkuc3VjY2Vzcykge1xuICAgICAgICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX1NVQ0NFRURFRFwifSk7XG4gICAgICAgICAgICAgIGFjdGlvbnMub25Ub2tlbihzdGF0ZS51c2VyZGF0YS51c2VySWQpO1xuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9IQURfRVJST1JcIn0pO1xuICAgICAgICAgICAgICBhY3Rpb25zLm9uU2VsZWN0Q29sbGVjdGlvbihzdGF0ZS5pbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24pO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH1cbiAgICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX0ZJTklTSEVEXCJ9KTtcbiAgICAgICAgfSk7XG4gICAgICB9KTtcbiAgICB9LFxuXG4gICAgb25TZWxlY3RDb2xsZWN0aW9uOiAoY29sbGVjdGlvbikgPT4ge1xuICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0FDVElWRV9DT0xMRUNUSU9OXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb259KTtcbiAgICAgIGRpc3BhdGNoKGZ1bmN0aW9uIChkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcbiAgICAgICAgdmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcbiAgICAgICAgdmFyIGN1cnJlbnRTaGVldCA9IHN0YXRlLmltcG9ydERhdGEuc2hlZXRzLmZpbmQoeCA9PiB4LmNvbGxlY3Rpb24gPT09IGNvbGxlY3Rpb24pO1xuICAgICAgICBpZiAoY3VycmVudFNoZWV0LnJvd3MubGVuZ3RoID09PSAwICYmIGN1cnJlbnRTaGVldC5uZXh0VXJsICYmICFjdXJyZW50U2hlZXQuaXNMb2FkaW5nKSB7XG4gICAgICAgICAgdmFyIHBheWxvYWQgPSB7XG4gICAgICAgICAgICBoZWFkZXJzOiB7XG4gICAgICAgICAgICAgIFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9O1xuICAgICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR1wiIH0pXG4gICAgICAgICAgeGhyLmdldChjdXJyZW50U2hlZXQubmV4dFVybCwgcGF5bG9hZCwgZnVuY3Rpb24gKGVyciwgcmVzcCwgYm9keSkge1xuICAgICAgICAgICAgaWYgKGVycikge1xuICAgICAgICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRVJST1JcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgZXJyb3I6IGVyciB9KVxuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfU1VDQ0VFREVEXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGRhdGE6IEpTT04ucGFyc2UoYm9keSl9KTtcbiAgICAgICAgICAgICAgfSBjYXRjaChlKSB7XG4gICAgICAgICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlIH0pXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR19GSU5JU0hFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9ufSlcbiAgICAgICAgICB9KTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfSxcblxuICAgIG9uTWFwQ29sbGVjdGlvbkFyY2hldHlwZTogKGNvbGxlY3Rpb24sIHZhbHVlKSA9PlxuICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiTUFQX0NPTExFQ1RJT05fQVJDSEVUWVBFXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHZhbHVlOiB2YWx1ZX0pLFxuXG4gICAgb25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzOiAoKSA9PiB7XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJDT05GSVJNX0NPTExFQ1RJT05fQVJDSEVUWVBFX01BUFBJTkdTXCJ9KVxuICAgICAgZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuICAgICAgICBsZXQgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuICAgICAgICBhY3Rpb25zLm9uU2VsZWN0Q29sbGVjdGlvbihzdGF0ZS5pbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24pO1xuICAgICAgfSlcbiAgICAgIG5hdmlnYXRlVG8oXCJtYXBEYXRhXCIpO1xuICAgIH0sXG5cbiAgICBvblNldEZpZWxkTWFwcGluZzogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQsIGltcG9ydGVkRmllbGQpID0+XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJTRVRfRklFTERfTUFQUElOR1wiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eUZpZWxkLCBpbXBvcnRlZEZpZWxkOiBpbXBvcnRlZEZpZWxkfSksXG5cbiAgICBvbkNsZWFyRmllbGRNYXBwaW5nOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCwgY2xlYXJJbmRleCkgPT5cbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkNMRUFSX0ZJRUxEX01BUFBJTkdcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgY2xlYXJJbmRleDogY2xlYXJJbmRleH0pLFxuXG4gICAgb25TZXREZWZhdWx0VmFsdWU6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkLCB2YWx1ZSkgPT5cbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9ERUZBVUxUX1ZBTFVFXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQ6IHByb3BlcnR5RmllbGQsIHZhbHVlOiB2YWx1ZX0pLFxuXG4gICAgb25Db25maXJtRmllbGRNYXBwaW5nczogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQpID0+XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJDT05GSVJNX0ZJRUxEX01BUFBJTkdTXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQ6IHByb3BlcnR5RmllbGR9KSxcblxuICAgIG9uVW5jb25maXJtRmllbGRNYXBwaW5nczogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQpID0+XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJVTkNPTkZJUk1fRklFTERfTUFQUElOR1NcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZH0pLFxuXG4gICAgb25TZXRWYWx1ZU1hcHBpbmc6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkLCB0aW1WYWx1ZSwgbWFwVmFsdWUpID0+XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJTRVRfVkFMVUVfTUFQUElOR1wiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eUZpZWxkLCB0aW1WYWx1ZTogdGltVmFsdWUsIG1hcFZhbHVlOiBtYXBWYWx1ZX0pLFxuXG4gICAgb25JZ25vcmVDb2x1bW5Ub2dnbGU6IChjb2xsZWN0aW9uLCB2YXJpYWJsZU5hbWUpID0+XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJUT0dHTEVfSUdOT1JFRF9DT0xVTU5cIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgdmFyaWFibGVOYW1lOiB2YXJpYWJsZU5hbWV9KSxcblxuICAgIG9uQWRkQ3VzdG9tUHJvcGVydHk6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eU5hbWUsIHByb3BlcnR5VHlwZSkgPT5cbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkFERF9DVVNUT01fUFJPUEVSVFlcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlOYW1lLCBwcm9wZXJ0eVR5cGU6IHByb3BlcnR5VHlwZX0pLFxuXG4gICAgb25SZW1vdmVDdXN0b21Qcm9wZXJ0eTogKGNvbGxlY3Rpb24sIHByb3BlcnR5TmFtZSkgPT5cbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlJFTU9WRV9DVVNUT01fUFJPUEVSVFlcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlOYW1lfSksXG5cbiAgICBvbkNsb3NlTWVzc2FnZTogKG1lc3NhZ2VJZCkgPT5cbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlRPR0dMRV9NRVNTQUdFXCIsIG1lc3NhZ2VJZDogbWVzc2FnZUlkfSlcbiAgfTtcbiAgcmV0dXJuIGFjdGlvbnM7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUHJvcGVydHlGb3JtIGZyb20gXCIuL3Byb3BlcnR5LWZvcm0vcHJvcGVydHktZm9ybVwiO1xuaW1wb3J0IEFkZFByb3BlcnR5IGZyb20gXCIuL3Byb3BlcnR5LWZvcm0vYWRkLXByb3BlcnR5XCI7XG5cbmNsYXNzIENvbGxlY3Rpb25Gb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3Qge1xuICAgICAgY29sdW1ucyxcbiAgICAgIGFyY2hldHlwZUZpZWxkcyxcbiAgICAgIHByb3BlcnR5TWFwcGluZ3MsXG4gICAgICBjdXN0b21Qcm9wZXJ0eU1hcHBpbmdzLFxuICAgICAgY29sbGVjdGlvbk5hbWUsXG4gICAgICBhdmFpbGFibGVBcmNoZXR5cGVzLFxuICAgICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGUsXG4gICAgICB0YXJnZXRhYmxlVnJlc1xuICAgIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qge1xuICAgICAgb25SZW1vdmVDdXN0b21Qcm9wZXJ0eSxcbiAgICAgIG9uU2V0RmllbGRNYXBwaW5nLFxuICAgICAgb25Db25maXJtRmllbGRNYXBwaW5ncyxcbiAgICAgIG9uQ2xlYXJGaWVsZE1hcHBpbmcsXG4gICAgICBvblVuY29uZmlybUZpZWxkTWFwcGluZ3MsXG4gICAgICBvbkFkZEN1c3RvbVByb3BlcnR5XG4gICAgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBhcmNoZVR5cGVQcm9wRmllbGRzID0gYXJjaGV0eXBlRmllbGRzLmZpbHRlcigoYWYpID0+IGFmLnR5cGUgIT09IFwicmVsYXRpb25cIik7XG5cbiAgICBjb25zdCBwcm9wZXJ0eUZvcm1zID0gYXJjaGVUeXBlUHJvcEZpZWxkc1xuICAgICAgLm1hcCgoYWYsIGkpID0+IChcbiAgICAgICAgPFByb3BlcnR5Rm9ybSBjb2x1bW5zPXtjb2x1bW5zfSBjdXN0b209e2ZhbHNlfSBrZXk9e2l9IG5hbWU9e2FmLm5hbWV9IHR5cGU9e2FmLnR5cGV9XG4gICAgICAgICAgICAgICAgICAgICAgcHJvcGVydHlNYXBwaW5nPXtwcm9wZXJ0eU1hcHBpbmdzLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IGFmLm5hbWUpfVxuICAgICAgICAgICAgICAgICAgICAgIG9uU2V0RmllbGRNYXBwaW5nPXsoZmllbGQsIHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhjb2xsZWN0aW9uTmFtZSwgZmllbGQsIHZhbHVlKX1cbiAgICAgICAgICAgICAgICAgICAgICBvbkNvbmZpcm1GaWVsZE1hcHBpbmdzPXsoZmllbGQpID0+IG9uQ29uZmlybUZpZWxkTWFwcGluZ3MoY29sbGVjdGlvbk5hbWUsIGZpZWxkKX1cbiAgICAgICAgICAgICAgICAgICAgICBvblVuY29uZmlybUZpZWxkTWFwcGluZ3M9eyhmaWVsZCkgPT4gb25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzKGNvbGxlY3Rpb25OYW1lLCBmaWVsZCl9XG4gICAgICAgICAgICAgICAgICAgICAgb25DbGVhckZpZWxkTWFwcGluZz17KGZpZWxkLCB2YWx1ZUlkeCkgPT4gb25DbGVhckZpZWxkTWFwcGluZyhjb2xsZWN0aW9uTmFtZSwgZmllbGQsIHZhbHVlSWR4KSB9XG4gICAgICAgIC8+XG4gICAgICApKTtcblxuICAgIGNvbnN0IGN1c3RvbVByb3BlcnR5Rm9ybXMgPSBjdXN0b21Qcm9wZXJ0eU1hcHBpbmdzXG4gICAgICAubWFwKChjdXN0b21Qcm9wLCBpKSA9PiAoXG4gICAgICAgIDxQcm9wZXJ0eUZvcm0gY29sdW1ucz17Y29sdW1uc30gY3VzdG9tPXt0cnVlfSBrZXk9e2l9IG5hbWU9e2N1c3RvbVByb3AubmFtZX0gdHlwZT17Y3VzdG9tUHJvcC50eXBlfVxuICAgICAgICAgICAgICAgICAgICAgIGFyY2hldHlwZUZpZWxkcz17YXJjaGV0eXBlRmllbGRzfVxuICAgICAgICAgICAgICAgICAgICAgIGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlPXthdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZX1cbiAgICAgICAgICAgICAgICAgICAgICBwcm9wZXJ0eU1hcHBpbmc9e3Byb3BlcnR5TWFwcGluZ3MuZmluZCgobSkgPT4gbS5wcm9wZXJ0eSA9PT0gY3VzdG9tUHJvcC5uYW1lKX1cbiAgICAgICAgICAgICAgICAgICAgICBvblJlbW92ZUN1c3RvbVByb3BlcnR5PXsoZmllbGQpID0+IG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHkoY29sbGVjdGlvbk5hbWUsIGZpZWxkKX1cbiAgICAgICAgICAgICAgICAgICAgICBvblNldEZpZWxkTWFwcGluZz17KGZpZWxkLCB2YWx1ZSkgPT4gb25TZXRGaWVsZE1hcHBpbmcoY29sbGVjdGlvbk5hbWUsIGZpZWxkLCB2YWx1ZSl9XG4gICAgICAgICAgICAgICAgICAgICAgb25DbGVhckZpZWxkTWFwcGluZz17KGZpZWxkLCB2YWx1ZUlkeCkgPT4gb25DbGVhckZpZWxkTWFwcGluZyhjb2xsZWN0aW9uTmFtZSwgZmllbGQsIHZhbHVlSWR4KSB9XG4gICAgICAgICAgICAgICAgICAgICAgb25Db25maXJtRmllbGRNYXBwaW5ncz17KGZpZWxkKSA9PiBvbkNvbmZpcm1GaWVsZE1hcHBpbmdzKGNvbGxlY3Rpb25OYW1lLCBmaWVsZCl9XG4gICAgICAgICAgICAgICAgICAgICAgb25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzPXsoZmllbGQpID0+IG9uVW5jb25maXJtRmllbGRNYXBwaW5ncyhjb2xsZWN0aW9uTmFtZSwgZmllbGQpfVxuICAgICAgICAgICAgICAgICAgICAgIHRhcmdldGFibGVWcmVzPXt0YXJnZXRhYmxlVnJlc31cbiAgICAgICAgLz5cbiAgICAgICkpO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICB7cHJvcGVydHlGb3Jtc31cbiAgICAgICAge2N1c3RvbVByb3BlcnR5Rm9ybXN9XG4gICAgICAgIDxBZGRQcm9wZXJ0eVxuICAgICAgICAgIGFyY2hldHlwZUZpZWxkcz17YXJjaGV0eXBlRmllbGRzfVxuICAgICAgICAgIGF2YWlsYWJsZUFyY2hldHlwZXM9e2F2YWlsYWJsZUFyY2hldHlwZXN9XG4gICAgICAgICAgb25BZGRDdXN0b21Qcm9wZXJ0eT17KG5hbWUsIHR5cGUpID0+IG9uQWRkQ3VzdG9tUHJvcGVydHkoY29sbGVjdGlvbk5hbWUsIG5hbWUsIHR5cGUpfSAvPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgQ29sbGVjdGlvbkluZGV4IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBjb2xsZWN0aW9uVGFicywgb25TZWxlY3RDb2xsZWN0aW9uIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwibmF2IG5hdi10YWJzXCIgcm9sZT1cInRhYmxpc3RcIj5cbiAgICAgICAgICB7Y29sbGVjdGlvblRhYnMubWFwKChjb2xsZWN0aW9uVGFiKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtjb2xsZWN0aW9uVGFiLmNvbGxlY3Rpb25OYW1lfSBjbGFzc05hbWU9e2N4KHthY3RpdmU6IGNvbGxlY3Rpb25UYWIuYWN0aXZlfSl9PlxuICAgICAgICAgICAgICA8YSBvbkNsaWNrPXsoKSA9PiBvblNlbGVjdENvbGxlY3Rpb24oY29sbGVjdGlvblRhYi5jb2xsZWN0aW9uTmFtZSl9XG4gICAgICAgICAgICAgICAgIHN0eWxlPXt7Y3Vyc29yOiBjb2xsZWN0aW9uVGFiLmFjdGl2ZSA/IFwiZGVmYXVsdFwiIDogXCJwb2ludGVyXCJ9fT5cbiAgICAgICAgICAgICAgICB7Y29sbGVjdGlvblRhYi5hcmNoZXR5cGVOYW1lfXtcIiBcIn1cbiAgICAgICAgICAgICAgICB7Y29sbGVjdGlvblRhYi5jb21wbGV0ZSA/IDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tb2tcIiAvPiA6IG51bGx9XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZXhjZWwtdGFiXCI+PGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBjbGFzc05hbWU9XCJleGNlbC1pY29uXCIgYWx0PVwiXCIvPiB7Y29sbGVjdGlvblRhYi5jb2xsZWN0aW9uTmFtZX08L3NwYW4+XG4gICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uSW5kZXg7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFVwbG9hZEJ1dHRvbiBmcm9tIFwiLi91cGxvYWQtYnV0dG9uXCI7XG5pbXBvcnQgRGF0YXNldENhcmRzIGZyb20gXCIuL2RhdGFzZXQtY2FyZHNcIlxuXG5mdW5jdGlvbiBDb2xsZWN0aW9uT3ZlcnZpZXcocHJvcHMpIHtcbiAgY29uc3QgeyBvblVwbG9hZEZpbGVTZWxlY3QsIHVzZXJJZCwgaXNVcGxvYWRpbmcsIHZyZXMgfSA9IHByb3BzO1xuXG4gIGNvbnN0IHVwbG9hZEJ1dHRvbiA9IChcbiAgICA8VXBsb2FkQnV0dG9uXG4gICAgICBjbGFzc05hbWVzPXtbXCJidG5cIiwgXCJidG4tbGdcIiwgXCJidG4tcHJpbWFyeVwiLCBcInB1bGwtcmlnaHRcIl19XG4gICAgICBnbHlwaGljb249XCJnbHlwaGljb24gZ2x5cGhpY29uLWNsb3VkLXVwbG9hZFwiXG4gICAgICBpc1VwbG9hZGluZz17aXNVcGxvYWRpbmd9XG4gICAgICBsYWJlbD1cIlVwbG9hZCBuZXcgZGF0YXNldFwiXG4gICAgICBvblVwbG9hZEZpbGVTZWxlY3Q9e29uVXBsb2FkRmlsZVNlbGVjdH0gLz5cbiAgKTtcblxuICByZXR1cm4gKFxuICAgIDxkaXY+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICA8RGF0YXNldENhcmRzIHVzZXJJZD17dXNlcklkfSBjYXB0aW9uPVwiTXkgZGF0YXNldHNcIiB2cmVzPXt2cmVzfT5cbiAgICAgICAgICB7dXBsb2FkQnV0dG9ufVxuICAgICAgICA8L0RhdGFzZXRDYXJkcz5cbiAgICAgIDwvZGl2PlxuICAgIDwvZGl2PlxuICApO1xufVxuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uT3ZlcnZpZXc7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IEhlYWRlckNlbGwgZnJvbSBcIi4vdGFibGUvaGVhZGVyLWNlbGxcIjtcbmltcG9ydCBEYXRhUm93IGZyb20gXCIuL3RhYmxlL2RhdGEtcm93XCI7XG5cbmNsYXNzIENvbGxlY3Rpb25UYWJsZSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uSWdub3JlQ29sdW1uVG9nZ2xlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgcm93cywgaGVhZGVycywgbmV4dFVybCB9ID0gdGhpcy5wcm9wcztcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cInRhYmxlLXJlc3BvbnNpdmVcIj5cbiAgICAgICAgPHRhYmxlIGNsYXNzTmFtZT1cInRhYmxlIHRhYmxlLWJvcmRlcmVkIHRhYmxlLW9idHJ1c2l2ZVwiPlxuICAgICAgICAgIDx0aGVhZD5cbiAgICAgICAgICAgIDx0cj5cbiAgICAgICAgICAgICAge2hlYWRlcnMubWFwKChoZWFkZXIpID0+IChcbiAgICAgICAgICAgICAgICA8SGVhZGVyQ2VsbCBrZXk9e2hlYWRlci5uYW1lfSBoZWFkZXI9e2hlYWRlci5uYW1lfSBvbklnbm9yZUNvbHVtblRvZ2dsZT17b25JZ25vcmVDb2x1bW5Ub2dnbGV9XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgaXNJZ25vcmVkPXtoZWFkZXIuaXNJZ25vcmVkfSBpc0NvbmZpcm1lZD17aGVhZGVyLmlzQ29uZmlybWVkfSAvPlxuICAgICAgICAgICAgICApKX1cbiAgICAgICAgICAgIDwvdHI+XG4gICAgICAgICAgPC90aGVhZD5cbiAgICAgICAgICA8dGJvZHk+XG4gICAgICAgICAgICB7cm93cy5tYXAoKHJvdywgaSkgPT4gPERhdGFSb3cga2V5PXtpfSByb3c9e3Jvd30gLz4pfVxuICAgICAgICAgIDwvdGJvZHk+XG4gICAgICAgIDwvdGFibGU+XG4gICAgICAgIDxidXR0b24gb25DbGljaz17KCkgPT4gdGhpcy5wcm9wcy5vbkxvYWRNb3JlQ2xpY2sgJiYgdGhpcy5wcm9wcy5vbkxvYWRNb3JlQ2xpY2sobmV4dFVybCl9XG4gICAgICAgICAgICAgICAgZGlzYWJsZWQ9eyFuZXh0VXJsfVxuICAgICAgICAgICAgICAgIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdCBwdWxsLXJpZ2h0XCI+bW9yZS4uLjwvYnV0dG9uPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFibGU7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IENvbGxlY3Rpb25UYWJsZSBmcm9tIFwiLi9jb2xsZWN0aW9uLXRhYmxlXCI7XG5pbXBvcnQgQ29sbGVjdGlvbkZvcm0gZnJvbSBcIi4vY29sbGVjdGlvbi1mb3JtXCI7XG5pbXBvcnQgQ29sbGVjdGlvbkluZGV4IGZyb20gXCIuL2NvbGxlY3Rpb24taW5kZXhcIjtcbmltcG9ydCBNZXNzYWdlIGZyb20gXCIuL21lc3NhZ2VcIjtcbmltcG9ydCBVcGxvYWRCdXR0b24gZnJvbSBcIi4vdXBsb2FkLWJ1dHRvblwiO1xuXG5jbGFzcyBDb25uZWN0RGF0YSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHtcbiAgICAgIGFjdGl2ZUNvbGxlY3Rpb24sXG4gICAgICB1cGxvYWRlZEZpbGVOYW1lLFxuICAgICAgY29sbGVjdGlvblRhYnMsXG4gICAgICByb3dzLFxuICAgICAgaGVhZGVycyxcbiAgICAgIGFyY2hldHlwZUZpZWxkcyxcbiAgICAgIGF2YWlsYWJsZUFyY2hldHlwZXMsXG4gICAgICBwcm9wZXJ0eU1hcHBpbmdzLFxuICAgICAgY3VzdG9tUHJvcGVydHlNYXBwaW5ncyxcbiAgICAgIGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlLFxuICAgICAgbmV4dFVybCxcbiAgICAgIHB1Ymxpc2hpbmcsXG4gICAgICBwdWJsaXNoRXJyb3JzLFxuICAgICAgc2hvd0NvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZSxcbiAgICAgIHNoZWV0cyxcbiAgICAgIHRhcmdldGFibGVWcmVzXG5cbiAgICB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHtcbiAgICAgIG9uSWdub3JlQ29sdW1uVG9nZ2xlLFxuICAgICAgb25TZWxlY3RDb2xsZWN0aW9uLFxuICAgICAgb25TZXRGaWVsZE1hcHBpbmcsXG4gICAgICBvblJlbW92ZUN1c3RvbVByb3BlcnR5LFxuICAgICAgb25Db25maXJtRmllbGRNYXBwaW5ncyxcbiAgICAgIG9uVW5jb25maXJtRmllbGRNYXBwaW5ncyxcbiAgICAgIG9uQWRkQ3VzdG9tUHJvcGVydHksXG4gICAgICBvbkNsZWFyRmllbGRNYXBwaW5nLFxuICAgICAgb25Mb2FkTW9yZUNsaWNrLFxuICAgICAgb25QdWJsaXNoRGF0YSxcbiAgICAgIG9uVXBsb2FkRmlsZVNlbGVjdCxcbiAgICAgIG9uQ2xvc2VNZXNzYWdlXG4gICAgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBhbGxNYXBwaW5nc0FyZUNvbXBsZXRlID0gY29sbGVjdGlvblRhYnMuZmlsdGVyKCh0YWIpID0+IHRhYi5jb21wbGV0ZSkubGVuZ3RoID09PSBjb2xsZWN0aW9uVGFicy5sZW5ndGg7XG5cbiAgICBjb25zdCBwdWJsaXNoRmFpbGVkTWVzc2FnZSA9IHB1Ymxpc2hFcnJvcnMgPyAoXG4gICAgICA8TWVzc2FnZSBhbGVydExldmVsPVwiZGFuZ2VyXCIgZGlzbWlzc2libGU9e2ZhbHNlfT5cbiAgICAgICAgPFVwbG9hZEJ1dHRvbiBjbGFzc05hbWVzPXtbXCJidG5cIiwgXCJidG4tZGFuZ2VyXCIsIFwicHVsbC1yaWdodFwiLCBcImJ0bi14c1wiXX0gbGFiZWw9XCJSZS11cGxvYWRcIlxuICAgICAgICAgICAgICAgICAgICAgIG9uVXBsb2FkRmlsZVNlbGVjdD17b25VcGxvYWRGaWxlU2VsZWN0fSAvPlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWV4Y2xhbWF0aW9uLXNpZ25cIiAvPntcIiBcIn1cbiAgICAgICAgUHVibGlzaCBmYWlsZWQuIFBsZWFzZSBmaXggdGhlIG1hcHBpbmdzIG9yIHJlLXVwbG9hZCB0aGUgZGF0YS5cbiAgICAgIDwvTWVzc2FnZT5cbiAgICApIDogbnVsbDtcblxuICAgIGNvbnN0IGNvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZSA9IHNoZWV0cyAmJiBzaG93Q29sbGVjdGlvbnNBcmVDb25uZWN0ZWRNZXNzYWdlID9cbiAgICAgIDxNZXNzYWdlIGFsZXJ0TGV2ZWw9XCJpbmZvXCIgZGlzbWlzc2libGU9e3RydWV9IG9uQ2xvc2VNZXNzYWdlPXsoKSA9PiBvbkNsb3NlTWVzc2FnZShcInNob3dDb2xsZWN0aW9uc0FyZUNvbm5lY3RlZE1lc3NhZ2VcIil9PlxuICAgICAgICB7c2hlZXRzLm1hcCgoc2hlZXQpID0+IDxlbSBrZXk9e3NoZWV0LmNvbGxlY3Rpb259PntzaGVldC5jb2xsZWN0aW9ufTwvZW0+KVxuICAgICAgICAgIC5yZWR1Y2UoKGFjY3UsIGVsZW0pID0+IGFjY3UgPT09IG51bGwgPyBbZWxlbV0gOiBbLi4uYWNjdSwgJyBhbmQgJywgZWxlbV0sIG51bGwpXG4gICAgICAgIH0gZnJvbSA8ZW0+e3VwbG9hZGVkRmlsZU5hbWV9PC9lbT4gYXJlIGNvbm5lY3RlZCB0byB0aGUgVGltYnVjdG9vIEFyY2hldHlwZXMuXG4gICAgICA8L01lc3NhZ2U+IDogbnVsbDtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICA8aDIgY2xhc3NOYW1lPVwic21hbGwtbWFyZ2luXCI+VXBsb2FkIGFuZCBjb25uZWN0IHlvdXIgZGF0YXNldDwvaDI+XG4gICAgICAgICAge2NvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZX1cbiAgICAgICAgICB7cHVibGlzaEZhaWxlZE1lc3NhZ2V9XG4gICAgICAgICAgPHA+Q29ubmVjdCB0aGUgZXhjZWwgY29sdW1ucyB0byB0aGUgcHJvcGVydGllcyBvZiB0aGUgQXJjaGV0eXBlczwvcD5cbiAgICAgICAgPC9kaXY+XG5cbiAgICAgICAgPENvbGxlY3Rpb25JbmRleCBjb2xsZWN0aW9uVGFicz17Y29sbGVjdGlvblRhYnN9IG9uU2VsZWN0Q29sbGVjdGlvbj17b25TZWxlY3RDb2xsZWN0aW9ufSAvPlxuXG4gICAgICAgIDxDb2xsZWN0aW9uRm9ybSBjb2x1bW5zPXtoZWFkZXJzfVxuICAgICAgICAgICAgICAgICAgICAgICAgY29sbGVjdGlvbk5hbWU9e2FjdGl2ZUNvbGxlY3Rpb259XG4gICAgICAgICAgICAgICAgICAgICAgICBhcmNoZXR5cGVGaWVsZHM9e2FyY2hldHlwZUZpZWxkc31cbiAgICAgICAgICAgICAgICAgICAgICAgIGF2YWlsYWJsZUFyY2hldHlwZXM9e2F2YWlsYWJsZUFyY2hldHlwZXN9XG4gICAgICAgICAgICAgICAgICAgICAgICBhdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZT17YXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGV9XG4gICAgICAgICAgICAgICAgICAgICAgICBwcm9wZXJ0eU1hcHBpbmdzPXtwcm9wZXJ0eU1hcHBpbmdzfVxuICAgICAgICAgICAgICAgICAgICAgICAgY3VzdG9tUHJvcGVydHlNYXBwaW5ncz17Y3VzdG9tUHJvcGVydHlNYXBwaW5nc31cbiAgICAgICAgICAgICAgICAgICAgICAgIG9uU2V0RmllbGRNYXBwaW5nPXtvblNldEZpZWxkTWFwcGluZ31cbiAgICAgICAgICAgICAgICAgICAgICAgIG9uQ2xlYXJGaWVsZE1hcHBpbmc9e29uQ2xlYXJGaWVsZE1hcHBpbmd9XG4gICAgICAgICAgICAgICAgICAgICAgICBvblJlbW92ZUN1c3RvbVByb3BlcnR5PXtvblJlbW92ZUN1c3RvbVByb3BlcnR5fVxuICAgICAgICAgICAgICAgICAgICAgICAgb25Db25maXJtRmllbGRNYXBwaW5ncz17b25Db25maXJtRmllbGRNYXBwaW5nc31cbiAgICAgICAgICAgICAgICAgICAgICAgIG9uVW5jb25maXJtRmllbGRNYXBwaW5ncz17b25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzfVxuICAgICAgICAgICAgICAgICAgICAgICAgb25BZGRDdXN0b21Qcm9wZXJ0eT17b25BZGRDdXN0b21Qcm9wZXJ0eX1cbiAgICAgICAgICAgICAgICAgICAgICAgIHRhcmdldGFibGVWcmVzPXt0YXJnZXRhYmxlVnJlc30gLz5cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiaWctbWFyZ2luXCI+XG4gICAgICAgICAgPGJ1dHRvbiBvbkNsaWNrPXtvblB1Ymxpc2hEYXRhfSBjbGFzc05hbWU9XCJidG4gYnRuLXdhcm5pbmcgYnRuLWxnIHB1bGwtcmlnaHRcIiB0eXBlPVwiYnV0dG9uXCIgZGlzYWJsZWQ9eyFhbGxNYXBwaW5nc0FyZUNvbXBsZXRlIHx8IHB1Ymxpc2hpbmd9PlxuICAgICAgICAgICAge3B1Ymxpc2hpbmcgPyBcIlB1Ymxpc2hpbmdcIiA6ICBcIlB1Ymxpc2ggZGF0YXNldFwifVxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICA8L2Rpdj5cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiaWctbWFyZ2luXCI+XG4gICAgICAgICAgPHAgY2xhc3NOYW1lPVwiZnJvbS1leGNlbFwiPlxuICAgICAgICAgICAgPGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBhbHQ9XCJcIi8+e1wiIFwifVxuICAgICAgICAgICAgPGVtPnthY3RpdmVDb2xsZWN0aW9ufTwvZW0+IGZyb20ge3VwbG9hZGVkRmlsZU5hbWV9XG4gICAgICAgICAgPC9wPlxuXG4gICAgICAgICAgPENvbGxlY3Rpb25UYWJsZVxuICAgICAgICAgICAgcm93cz17cm93c31cbiAgICAgICAgICAgIGhlYWRlcnM9e2hlYWRlcnN9XG4gICAgICAgICAgICBuZXh0VXJsPXtuZXh0VXJsfVxuICAgICAgICAgICAgb25JZ25vcmVDb2x1bW5Ub2dnbGU9eyhoZWFkZXIpID0+IG9uSWdub3JlQ29sdW1uVG9nZ2xlKGFjdGl2ZUNvbGxlY3Rpb24sIGhlYWRlcil9XG4gICAgICAgICAgICBvbkxvYWRNb3JlQ2xpY2s9eyh1cmwpID0+IG9uTG9hZE1vcmVDbGljayh1cmwsIGFjdGl2ZUNvbGxlY3Rpb24pfSAvPlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBDb25uZWN0RGF0YTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuaW1wb3J0IE1lc3NhZ2UgZnJvbSBcIi4vbWVzc2FnZVwiO1xuXG5jbGFzcyBDb25uZWN0VG9BcmNoZXR5cGUgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbk1hcENvbGxlY3Rpb25BcmNoZXR5cGUsIG9uQ29uZmlybUNvbGxlY3Rpb25BcmNoZXR5cGVNYXBwaW5ncywgb25DbG9zZU1lc3NhZ2UsXG4gICAgICBzaGVldHMsIGFyY2hldHlwZSwgbWFwcGluZ3MsIHNob3dGaWxlSXNVcGxvYWRlZE1lc3NhZ2UsIHVwbG9hZGVkRmlsZU5hbWUgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBjb2xsZWN0aW9uc0FyZU1hcHBlZCA9IE9iamVjdC5rZXlzKG1hcHBpbmdzLmNvbGxlY3Rpb25zKS5sZW5ndGggPiAwICYmXG4gICAgICBPYmplY3Qua2V5cyhtYXBwaW5ncy5jb2xsZWN0aW9ucykubWFwKChrZXkpID0+IG1hcHBpbmdzLmNvbGxlY3Rpb25zW2tleV0uYXJjaGV0eXBlTmFtZSkuaW5kZXhPZihudWxsKSA8IDA7XG5cbiAgICBjb25zdCBmaWxlSXNVcGxvYWRlZE1lc3NhZ2UgPSBzaG93RmlsZUlzVXBsb2FkZWRNZXNzYWdlID8gKFxuICAgICAgPE1lc3NhZ2UgYWxlcnRMZXZlbD1cImluZm9cIiBkaXNtaXNzaWJsZT17dHJ1ZX0gb25DbG9zZU1lc3NhZ2U9eygpID0+IG9uQ2xvc2VNZXNzYWdlKFwic2hvd0ZpbGVJc1VwbG9hZGVkTWVzc2FnZVwiKX0+XG4gICAgICAgIDxlbT57dXBsb2FkZWRGaWxlTmFtZX08L2VtPiBpcyB1cGxvYWRlZC5cbiAgICAgIDwvTWVzc2FnZT5cbiAgICApIDogbnVsbDtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICA8aDIgY2xhc3NOYW1lPVwic21hbGwtbWFyZ2luXCI+VXBsb2FkIGFuZCBjb25uZWN0IHlvdXIgZGF0YXNldDwvaDI+XG4gICAgICAgICAge2ZpbGVJc1VwbG9hZGVkTWVzc2FnZX1cbiAgICAgICAgICA8cD5XZSBmb3VuZCB7c2hlZXRzLmxlbmd0aH0gY29sbGVjdGlvbnMgaW4gdGhlIGZpbGUuIENvbm5lY3QgdGhlIHRhYnMgdG8gdGhlIFRpbWJ1Y3RvbyBBcmNoZXR5cGVzLjwvcD5cbiAgICAgICAgPC9kaXY+XG5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAge3NoZWV0cy5tYXAoKHNoZWV0KSA9PiAoXG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInJvd1wiIGtleT17c2hlZXQuY29sbGVjdGlvbn0+XG4gICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLW1kLTJcIj5cbiAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+XG4gICAgICAgICAgICAgICAgICA8aW1nIHNyYz1cImltYWdlcy9pY29uLWV4Y2VsLnN2Z1wiIGFsdD1cIlwiLz4ge3NoZWV0LmNvbGxlY3Rpb259XG4gICAgICAgICAgICAgICAgICA8L3NwYW4+XG4gICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1tZC04XCI+XG4gICAgICAgICAgICAgICAgPFNlbGVjdEZpZWxkXG4gICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbHVlKSA9PiBvbk1hcENvbGxlY3Rpb25BcmNoZXR5cGUoc2hlZXQuY29sbGVjdGlvbiwgdmFsdWUpfVxuICAgICAgICAgICAgICAgICAgb25DbGVhcj17KCkgPT4gb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHNoZWV0LmNvbGxlY3Rpb24sIG51bGwpIH1cbiAgICAgICAgICAgICAgICAgIHZhbHVlPXttYXBwaW5ncy5jb2xsZWN0aW9uc1tzaGVldC5jb2xsZWN0aW9uXS5hcmNoZXR5cGVOYW1lfT5cbiAgICAgICAgICAgICAgICAgICAgPHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG4gICAgICAgICAgICAgICAgICAgICAgQ29ubmVjdCA8ZW0+e3NoZWV0LmNvbGxlY3Rpb259PC9lbT4gdG8gYSBUaW1idWN0b28gYXJjaGV0eXBlLlxuICAgICAgICAgICAgICAgICAgICA8L3NwYW4+XG4gICAgICAgICAgICAgICAgICAgIHtPYmplY3Qua2V5cyhhcmNoZXR5cGUpLmZpbHRlcigoZG9tYWluKSA9PiBkb21haW4gIT09IFwicmVsYXRpb25zXCIpLnNvcnQoKS5tYXAoKG9wdGlvbikgPT4gKFxuICAgICAgICAgICAgICAgICAgICAgIDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cbiAgICAgICAgICAgICAgICAgICAgKSl9XG4gICAgICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgIHsgbWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0uYXJjaGV0eXBlTmFtZSA/IChcbiAgICAgICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgaGktc3VjY2Vzc1wiIGtleT17c2hlZXQuY29sbGVjdGlvbn0+XG4gICAgICAgICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tb2sgcHVsbC1yaWdodFwiLz5cbiAgICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICAgICkgOiBudWxsXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICkpfVxuXG4gICAgICAgIDwvZGl2PlxuXG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gb25DbGljaz17b25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzfSB0eXBlPVwiYnV0dG9uXCIgY2xhc3NOYW1lPVwiYnRuIGJ0bi1zdWNjZXNzXCIgZGlzYWJsZWQ9eyFjb2xsZWN0aW9uc0FyZU1hcHBlZH0+XG4gICAgICAgICAgICBDb25uZWN0XG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IENvbm5lY3RUb0FyY2hldHlwZTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IERhdGFTZXRDYXJkIGZyb20gJy4vZGF0YXNldENhcmQuanN4JztcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyB2cmVzLCBjYXB0aW9uLCB1c2VySWQgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICAgICAgPGgzPntjYXB0aW9ufTwvaDM+XG4gICAgICA8L2Rpdj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmlnLW1hcmdpblwiPlxuICAgICAgICB7IE9iamVjdC5rZXlzKHZyZXMpLm1hcCgodnJlKSA9PiA8RGF0YVNldENhcmQga2V5PXt2cmV9IHVzZXJJZD17dXNlcklkfSB2cmVJZD17dnJlc1t2cmVdLm5hbWV9IGNhcHRpb249e3ZyZXNbdnJlXS5uYW1lLnJlcGxhY2UoL15bYS16MC05XStfLywgXCJcIil9IC8+KSB9XG4gICAgIDwvZGl2PlxuICAgIDwvZGl2PlxuICApXG59OyIsImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmNvbnN0IFRJTUJVQ1RPT19TRUFSQ0hfVVJMID0gZ2xvYmFscyAmJiBnbG9iYWxzLmVudiA/XG4gIGdsb2JhbHMuZW52LlRJTUJVQ1RPT19TRUFSQ0hfVVJMIDogXCIvXCI7XG5cbmZ1bmN0aW9uIERhdGFTZXRDYXJkKHByb3BzKSB7XG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjYXJkLWRhdGFzZXRcIj5cbiAgICAgIDxhIGNsYXNzTmFtZT1cImNhcmQtZGF0YXNldCBidG4gYnRuLWRlZmF1bHQgZXhwbG9yZVwiXG4gICAgICAgICBocmVmPXtgJHtUSU1CVUNUT09fU0VBUkNIX1VSTH0/dnJlSWQ9JHtwcm9wcy52cmVJZH1gfSB0YXJnZXQ9XCJfYmxhbmtcIj5cbiAgICAgICAgRXhwbG9yZTxiciAvPlxuICAgICAgICA8c3Ryb25nPlxuICAgICAgICAgICAge3Byb3BzLmNhcHRpb259XG4gICAgICAgIDwvc3Ryb25nPlxuICAgICAgPC9hPlxuICAgICAge3Byb3BzLnVzZXJJZFxuICAgICAgICA/ICg8YSBjbGFzc05hbWU9XCJjYXJkLWRhdGFzZXQgYnRuIGJ0bi1kZWZhdWx0XCJcbiAgICAgICAgICAgICAgaHJlZj17YCR7cHJvY2Vzcy5lbnYuc2VydmVyfS9zdGF0aWMvZWRpdC1ndWkvP3ZyZUlkPSR7cHJvcHMudnJlSWR9JmhzaWQ9JHtwcm9wcy51c2VySWR9YH0gdGFyZ2V0PVwiX2JsYW5rXCI+XG4gICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiIC8+e1wiIFwifVxuICAgICAgICAgICAgRWRpdFxuICAgICAgICAgIDwvYT4pXG4gICAgICAgIDogbnVsbH1cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgRGF0YVNldENhcmQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgaXNPcGVuOiBmYWxzZVxuICAgIH07XG4gICAgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50KCkge1xuICAgIGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG4gIH1cblxuICB0b2dnbGVTZWxlY3QoKSB7XG4gICAgaWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG4gICAgfVxuICB9XG5cbiAgaGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuICAgIGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuICAgIGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe1xuICAgICAgICBpc09wZW46IGZhbHNlXG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbkNoYW5nZSwgb25DbGVhciwgdmFsdWUgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBzZWxlY3RlZE9wdGlvbiA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSA9PT0gdmFsdWUpO1xuICAgIGNvbnN0IHBsYWNlaG9sZGVyID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnR5cGUgPT09IFwicGxhY2Vob2xkZXJcIik7XG4gICAgY29uc3Qgb3RoZXJPcHRpb25zID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnZhbHVlICYmIG9wdC5wcm9wcy52YWx1ZSAhPT0gdmFsdWUpO1xuXG4gICAgcmV0dXJuIChcblxuICAgICAgPGRpdiBjbGFzc05hbWU9e2N4KFwiZHJvcGRvd25cIiwge29wZW46IHRoaXMuc3RhdGUuaXNPcGVufSl9PlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgZHJvcGRvd24tdG9nZ2xlXCIgb25DbGljaz17dGhpcy50b2dnbGVTZWxlY3QuYmluZCh0aGlzKX0+XG4gICAgICAgICAge3NlbGVjdGVkT3B0aW9uLmxlbmd0aCA/IHNlbGVjdGVkT3B0aW9uIDogcGxhY2Vob2xkZXJ9IDxzcGFuIGNsYXNzTmFtZT1cImNhcmV0XCIgLz5cbiAgICAgICAgPC9idXR0b24+XG5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cImRyb3Bkb3duLW1lbnVcIj5cbiAgICAgICAgICB7IHZhbHVlID8gKFxuICAgICAgICAgICAgPGxpPlxuICAgICAgICAgICAgICA8YSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2xlYXIoKTsgdGhpcy50b2dnbGVTZWxlY3QoKTt9fT5cbiAgICAgICAgICAgICAgICAtIGNsZWFyIC1cbiAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApIDogbnVsbH1cbiAgICAgICAgICB7b3RoZXJPcHRpb25zLm1hcCgob3B0aW9uLCBpKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtpfT5cbiAgICAgICAgICAgICAgPGEgc3R5bGU9e3tjdXJzb3I6IFwicG9pbnRlclwifX0gb25DbGljaz17KCkgPT4geyBvbkNoYW5nZShvcHRpb24ucHJvcHMudmFsdWUpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpOyB9fT57b3B0aW9ufTwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cblNlbGVjdEZpZWxkLnByb3BUeXBlcyA9IHtcbiAgb25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICBvbkNsZWFyOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgdmFsdWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmdcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFNlbGVjdEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFVwbG9hZEJ1dHRvbiBmcm9tIFwiLi91cGxvYWQtYnV0dG9uXCI7XG5cbi8vIChyZW5hbWVzIGZyb20gVXBsb2FkU3BsYXNoU2NyZWVuKVxuZnVuY3Rpb24gRmlyc3RVcGxvYWQocHJvcHMpIHtcbiAgY29uc3QgeyBvblVwbG9hZEZpbGVTZWxlY3QsIHVzZXJJZCwgaXNVcGxvYWRpbmcgfSA9IHByb3BzO1xuXG4gIGNvbnN0IHNhbXBsZVNoZWV0ID0gcHJvcHMuZXhhbXBsZVNoZWV0VXJsID9cbiAgICA8cD5Eb24ndCBoYXZlIGEgZGF0YXNldCBoYW5keT8gSGVyZeKAmXMgYW4gPGEgaHJlZj17cHJvcHMuZXhhbXBsZVNoZWV0VXJsfT5leGFtcGxlIGV4Y2VsIHNoZWV0PC9hPi48L3A+IDogbnVsbDtcblxuICBjb25zdCB1cGxvYWRCdXR0b24gPSAoXG4gICAgPFVwbG9hZEJ1dHRvblxuICAgICAgY2xhc3NOYW1lcz17W1wiYnRuXCIsIFwiYnRuLWxnXCIsIFwiYnRuLXByaW1hcnlcIl19XG4gICAgICBnbHlwaGljb249XCJnbHlwaGljb24gZ2x5cGhpY29uLWNsb3VkLXVwbG9hZFwiXG4gICAgICBpc1VwbG9hZGluZz17aXNVcGxvYWRpbmd9XG4gICAgICBsYWJlbD1cIkJyb3dzZVwiXG4gICAgICBvblVwbG9hZEZpbGVTZWxlY3Q9e29uVXBsb2FkRmlsZVNlbGVjdH0gLz5cbiAgKTtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImp1bWJvdHJvbiBmaXJzdC11cGxvYWQgdXBsb2FkLWJnXCI+XG4gICAgICAgIDxoMj5VcGxvYWQgeW91ciBmaXJzdCBkYXRhc2V0PC9oMj5cbiAgICAgICAgICAge3NhbXBsZVNoZWV0fVxuICAgICAgICAgICB7dXNlcklkID8gdXBsb2FkQnV0dG9uIDogKFxuICAgICAgICAgICA8Zm9ybSBhY3Rpb249XCJodHRwczovL3NlY3VyZS5odXlnZW5zLmtuYXcubmwvc2FtbDIvbG9naW5cIiBtZXRob2Q9XCJQT1NUXCI+XG4gICAgICAgICAgICAgPGlucHV0IG5hbWU9XCJoc3VybFwiICB0eXBlPVwiaGlkZGVuXCIgdmFsdWU9e3dpbmRvdy5sb2NhdGlvbi5ocmVmfSAvPlxuICAgICAgICAgICAgIDxwPk1vc3QgdW5pdmVyc2l0eSBhY2NvdW50cyB3aWxsIHdvcmsuPC9wPlxuICAgICAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tcHJpbWFyeSBidG4tbGdcIiB0eXBlPVwic3VibWl0XCI+XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1sb2ctaW5cIiAvPiBMb2cgaW4gdG8gdXBsb2FkXG4gICAgICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgICA8L2Zvcm0+KSB9XG4gICAgICA8L2Rpdj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgRmlyc3RVcGxvYWQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmZ1bmN0aW9uIEZvb3Rlcihwcm9wcykge1xuICBjb25zdCBoaUxvZ28gPSAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMSBjb2wtbWQtMVwiPlxuICAgICAgPGltZyBjbGFzc05hbWU9XCJoaS1sb2dvXCIgc3JjPVwiaW1hZ2VzL2xvZ28taHV5Z2Vucy1pbmcuc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBjbGFyaWFoTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1jbGFyaWFoLnN2Z1wiIC8+XG4gICAgPC9kaXY+XG4gICk7XG5cbiAgY29uc3QgZm9vdGVyQm9keSA9IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSA+IDAgP1xuICAgIFJlYWN0LkNoaWxkcmVuLm1hcChwcm9wcy5jaGlsZHJlbiwgKGNoaWxkLCBpKSA9PiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIHtpID09PSBSZWFjdC5DaGlsZHJlbi5jb3VudChwcm9wcy5jaGlsZHJlbikgLSAxXG4gICAgICAgICAgICA/ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPntoaUxvZ299PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+e2NoaWxkfTwvZGl2PntjbGFyaWFoTG9nb308L2Rpdj4pXG4gICAgICAgICAgICA6ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPjxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEyIGNvbC1tZC0xMiB0ZXh0LWNlbnRlclwiPntjaGlsZH08L2Rpdj48L2Rpdj4pXG4gICAgICAgICAgfVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICkpIDogKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJ3aGl0ZS1iYXJcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPlxuICAgICAgICAgICAge2hpTG9nb31cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEwIGNvbC1tZC0xMCB0ZXh0LWNlbnRlclwiPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICB7Y2xhcmlhaExvZ299XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcblxuXG4gIHJldHVybiAoXG4gICAgPGZvb3RlciBjbGFzc05hbWU9XCJmb290ZXJcIj5cbiAgICAgIHtmb290ZXJCb2R5fVxuICAgIDwvZm9vdGVyPlxuICApXG59XG5cbmV4cG9ydCBkZWZhdWx0IEZvb3RlcjsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBkaXNtaXNzaWJsZSwgYWxlcnRMZXZlbCwgb25DbG9zZU1lc3NhZ2V9ID0gcHJvcHM7XG4gIGNvbnN0IGRpc21pc3NCdXR0b24gPSBkaXNtaXNzaWJsZVxuICAgID8gPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3NOYW1lPVwiY2xvc2VcIiBvbkNsaWNrPXtvbkNsb3NlTWVzc2FnZX0+PHNwYW4+JnRpbWVzOzwvc3Bhbj48L2J1dHRvbj5cbiAgICA6IG51bGw7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT17Y3goXCJhbGVydFwiLCBgYWxlcnQtJHthbGVydExldmVsfWAsIHtcImFsZXJ0LWRpc21pc3NpYmxlXCI6IGRpc21pc3NpYmxlfSl9IHJvbGU9XCJhbGVydFwiPlxuICAgICAge2Rpc21pc3NCdXR0b259XG4gICAgICB7cHJvcHMuY2hpbGRyZW59XG4gICAgPC9kaXY+XG4gIClcbn07IiwiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBEYXRhc2V0Q2FyZHMgZnJvbSBcIi4vZGF0YXNldC1jYXJkc1wiO1xuaW1wb3J0IEZvb3RlciBmcm9tIFwiLi9mb290ZXJcIjtcblxuY29uc3QgRk9PVEVSX0hFSUdIVCA9IDgxO1xuXG5mdW5jdGlvbiBQYWdlKHByb3BzKSB7XG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJwYWdlXCI+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpbiBoaS1HcmVlbiBjb250YWluZXItZmx1aWRcIj5cbiAgICAgICAgPG5hdiBjbGFzc05hbWU9XCJuYXZiYXIgXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwibmF2YmFyLWhlYWRlclwiPiA8YSBjbGFzc05hbWU9XCJuYXZiYXItYnJhbmRcIiBocmVmPVwiI1wiPjxpbWcgc3JjPVwiaW1hZ2VzL2xvZ28tdGltYnVjdG9vLnN2Z1wiIGNsYXNzTmFtZT1cImxvZ29cIiBhbHQ9XCJ0aW1idWN0b29cIi8+PC9hPiA8L2Rpdj5cbiAgICAgICAgICAgIDxkaXYgaWQ9XCJuYXZiYXJcIiBjbGFzc05hbWU9XCJuYXZiYXItY29sbGFwc2UgY29sbGFwc2VcIj5cbiAgICAgICAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXZiYXItbmF2IG5hdmJhci1yaWdodFwiPlxuICAgICAgICAgICAgICAgIHtwcm9wcy51c2VybmFtZSA/IDxsaT48YSBocmVmPXtwcm9wcy51c2VybG9jYXRpb24gfHwgJyMnfT48c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXVzZXJcIi8+IHtwcm9wcy51c2VybmFtZX08L2E+PC9saT4gOiBudWxsfVxuICAgICAgICAgICAgICA8L3VsPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvbmF2PlxuICAgICAgPC9kaXY+XG4gICAgICA8ZGl2ICBzdHlsZT17e21hcmdpbkJvdHRvbTogYCR7Rk9PVEVSX0hFSUdIVH1weGB9fT5cbiAgICAgICAge3Byb3BzLmNoaWxkcmVufVxuICAgICAgICB7cHJvcHMudnJlcyAmJiBwcm9wcy5zaG93RGF0YXNldHMgPyAoXG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICAgIDxEYXRhc2V0Q2FyZHMgY2FwdGlvbj1cIkV4cGxvcmUgYWxsIGRhdGFzZXRzXCIgdnJlcz17cHJvcHMudnJlc30gLz5cbiAgICAgICAgICA8L2Rpdj4pIDogbnVsbH1cbiAgICAgIDwvZGl2PlxuICAgICAgPEZvb3RlciAvPlxuICAgIDwvZGl2PlxuICApO1xufVxuXG5leHBvcnQgZGVmYXVsdCBQYWdlO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEFkZFByb3BlcnR5IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBjb25zdHJ1Y3Rvcihwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBuZXdOYW1lOiBcIlwiLFxuICAgICAgbmV3VHlwZTogbnVsbFxuICAgIH07XG4gIH1cblxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG5ld05hbWUsIG5ld1R5cGUgfSA9IHRoaXMuc3RhdGU7XG4gICAgY29uc3QgeyBvbkFkZEN1c3RvbVByb3BlcnR5LCBhcmNoZXR5cGVGaWVsZHMsIGF2YWlsYWJsZUFyY2hldHlwZXMgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCByZWxhdGlvblR5cGVPcHRpb25zID0gYXJjaGV0eXBlRmllbGRzXG4gICAgICAuZmlsdGVyKChwcm9wKSA9PiBwcm9wLnR5cGUgPT09IFwicmVsYXRpb25cIilcbiAgICAgIC5maWx0ZXIoKHByb3ApID0+IG5ld1R5cGUgPT09IFwicmVsYXRpb24tdG8tZXhpc3RpbmdcIiB8fCBhdmFpbGFibGVBcmNoZXR5cGVzLmluZGV4T2YocHJvcC5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uKSA+IC0xKVxuICAgICAgLm1hcCgocHJvcCkgPT4gPHNwYW4ga2V5PXtwcm9wLm5hbWV9IHZhbHVlPXtwcm9wLm5hbWV9Pntwcm9wLm5hbWV9PC9zcGFuPik7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJyb3cgc21hbGwtbWFyZ2luXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTJcIj5cbiAgICAgICAgICB7bmV3VHlwZSA9PT0gXCJyZWxhdGlvblwiIHx8IG5ld1R5cGUgPT09IFwicmVsYXRpb24tdG8tZXhpc3RpbmdcIlxuICAgICAgICAgICAgPyAoXG4gICAgICAgICAgICAgIDxTZWxlY3RGaWVsZFxuICAgICAgICAgICAgICAgIHZhbHVlPXtuZXdOYW1lfVxuICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsodmFsdWUpID0+IHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IHZhbHVlfSl9XG4gICAgICAgICAgICAgICAgb25DbGVhcj17KCkgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3TmFtZTogXCJcIn0pfT5cbiAgICAgICAgICAgICAgICA8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIj5DaG9vc2UgYSByZWxhdGlvbiB0eXBlLi4uPC9zcGFuPlxuICAgICAgICAgICAgICAgIHtyZWxhdGlvblR5cGVPcHRpb25zfVxuICAgICAgICAgICAgICA8L1NlbGVjdEZpZWxkPlxuICAgICAgICAgICAgKSA6IChcbiAgICAgICAgICAgICAgPGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG4gICAgICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3TmFtZTogZXYudGFyZ2V0LnZhbHVlfSl9XG4gICAgICAgICAgICAgICAgICAgICAgcGxhY2Vob2xkZXI9XCJQcm9wZXJ0eSBuYW1lXCJcbiAgICAgICAgICAgICAgICAgICAgICB2YWx1ZT17bmV3TmFtZX1cbiAgICAgICAgICAgICAgICAgICAgICBkaXNhYmxlZD17bmV3VHlwZSAhPT0gXCJ0ZXh0XCJ9Lz5cbiAgICAgICAgICAgIClcbiAgICAgICAgICB9XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS04XCIgPlxuICAgICAgICAgIDxzcGFuPlxuICAgICAgICAgICAgPFNlbGVjdEZpZWxkXG4gICAgICAgICAgICAgIHZhbHVlPXtuZXdUeXBlfVxuICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbHVlKSA9PiB0aGlzLnNldFN0YXRlKHtuZXdUeXBlOiB2YWx1ZSwgbmV3TmFtZTogdmFsdWUgPT09IFwicmVsYXRpb25cIiA/IG51bGwgOiBuZXdOYW1lfSl9XG4gICAgICAgICAgICAgIG9uQ2xlYXI9eygpID0+IHRoaXMuc2V0U3RhdGUoe25ld1R5cGU6IG51bGx9KX0+XG4gICAgICAgICAgICAgIDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPkNob29zZSBhIHR5cGUuLi48L3NwYW4+XG4gICAgICAgICAgICAgIDxzcGFuIHZhbHVlPVwidGV4dFwiPlRleHQ8L3NwYW4+XG4gICAgICAgICAgICAgIDxzcGFuIHZhbHVlPVwicmVsYXRpb25cIj5SZWxhdGlvbjwvc3Bhbj5cbiAgICAgICAgICAgICAgPHNwYW4gdmFsdWU9XCJyZWxhdGlvbi10by1leGlzdGluZ1wiPlJlbGF0aW9uIHRvIGV4aXN0aW5nIFRpbWJ1Y3RvbyBjb2xsZWN0aW9uPC9zcGFuPlxuICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICA8L3NwYW4+XG4gICAgICAgIDwvZGl2PlxuXG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTJcIj5cblxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwicHVsbC1yaWdodCBidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17IShuZXdOYW1lICYmIG5ld1R5cGUpfVxuICAgICAgICAgICAgICAgICAgb25DbGljaz17KCkgPT4ge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnNldFN0YXRlKHtuZXdOYW1lOiBudWxsLCBuZXdUeXBlOiBudWxsfSk7XG4gICAgICAgICAgICAgICAgICAgIG9uQWRkQ3VzdG9tUHJvcGVydHkobmV3TmFtZSwgbmV3VHlwZSk7XG4gICAgICAgICAgICAgICAgICB9fT5cbiAgICAgICAgICAgIEFkZCBwcm9wZXJ0eVxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufVxuXG5BZGRQcm9wZXJ0eS5wcm9wVHlwZXMgPSB7XG4gIGltcG9ydERhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuICBvbkFkZEN1c3RvbVByb3BlcnR5OiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgQWRkUHJvcGVydHk7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBDb2x1bW5TZWxlY3QgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgY29sdW1ucywgcHJvcGVydHlNYXBwaW5nLCBvbkNvbHVtblNlbGVjdCwgb25DbGVhckNvbHVtbiwgcGxhY2Vob2xkZXIgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBzZWxlY3RlZENvbHVtbiA9IHByb3BlcnR5TWFwcGluZyAmJiBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGVbMF0gPyBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGVbMF0udmFyaWFibGVOYW1lIDogbnVsbDtcblxuICAgIHJldHVybiBwcm9wZXJ0eU1hcHBpbmcgJiYgcHJvcGVydHlNYXBwaW5nLmNvbmZpcm1lZCA/IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiZnJvbS1leGNlbCBwYWQtNi0xMlwiID48aW1nIHNyYz1cImltYWdlcy9pY29uLWV4Y2VsLnN2Z1wiIGFsdD1cIlwiLz4ge3NlbGVjdGVkQ29sdW1ufTwvZGl2PlxuICAgICkgOiAoXG4gICAgICA8U2VsZWN0RmllbGQgdmFsdWU9e3NlbGVjdGVkQ29sdW1ufVxuICAgICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsoY29sdW1uKSA9PiBvbkNvbHVtblNlbGVjdChbe3ZhcmlhYmxlTmFtZTogY29sdW1ufV0pfVxuICAgICAgICAgICAgICAgICAgIG9uQ2xlYXI9eygpID0+IG9uQ2xlYXJDb2x1bW4oMCl9PlxuICAgICAgICA8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIiBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+PGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBhbHQ9XCJcIi8+IHtwbGFjZWhvbGRlciB8fCBcIlNlbGVjdCBhbiBleGNlbCBjb2x1bW5cIn08L3NwYW4+XG4gICAgICAgIHtjb2x1bW5zLmZpbHRlcigoY29sKSA9PiBjb2wubmFtZSA9PT0gc2VsZWN0ZWRDb2x1bW4gfHwgKCFjb2wuaXNDb25maXJtZWQgJiYgIWNvbC5pc0lnbm9yZWQpICkubWFwKChjb2wpID0+IChcbiAgICAgICAgICA8c3BhbiBrZXk9e2NvbC5uYW1lfSB2YWx1ZT17Y29sLm5hbWV9IGNsYXNzTmFtZT1cImZyb20tZXhjZWxcIj48aW1nIHNyYz1cImltYWdlcy9pY29uLWV4Y2VsLnN2Z1wiIGFsdD1cIlwiLz4ge2NvbC5uYW1lfTwvc3Bhbj5cbiAgICAgICAgKSl9XG4gICAgICA8L1NlbGVjdEZpZWxkPlxuICAgICk7XG4gIH1cbn1cblxuQ29sdW1uU2VsZWN0LnByb3BUeXBlcyA9IHtcbiAgY29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuICBuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBvbkNsZWFyRmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgb25TZXRGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2x1bW5TZWxlY3Q7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmltcG9ydCBUZXh0IGZyb20gXCIuL3RleHRcIjtcbmltcG9ydCBSZWxhdGlvbiBmcm9tIFwiLi9yZWxhdGlvblwiO1xuaW1wb3J0IFJlbGF0aW9uVG9FeGlzdGluZyBmcm9tIFwiLi9yZWxhdGlvbi10by1leGlzdGluZ1wiO1xuXG5jb25zdCB0eXBlTWFwID0ge1xuICB0ZXh0OiAocHJvcHMpID0+IDxUZXh0IHsuLi5wcm9wc30gLz4sXG4gIGRhdGFibGU6IChwcm9wcykgPT4gPFRleHQgey4uLnByb3BzfSAvPixcbiAgcmVsYXRpb246IChwcm9wcykgPT4gPFJlbGF0aW9uIHsuLi5wcm9wc30gLz4sXG4gIFwicmVsYXRpb24tdG8tZXhpc3RpbmdcIjogKHByb3BzKSA9PiA8UmVsYXRpb25Ub0V4aXN0aW5nIHsuLi5wcm9wc30gLz5cbn07XG5cbmNsYXNzIFByb3BlcnR5Rm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgY2FuQ29uZmlybSh2YXJpYWJsZSkge1xuICAgIGNvbnN0IHsgdHlwZSB9ID0gdGhpcy5wcm9wcztcbiAgICBpZiAoIXZhcmlhYmxlIHx8IHZhcmlhYmxlLmxlbmd0aCA9PT0gMCkgeyByZXR1cm4gZmFsc2U7IH1cbiAgICBpZiAodHlwZSA9PT0gXCJyZWxhdGlvblwiKSB7XG4gICAgICByZXR1cm4gdmFyaWFibGVbMF0udmFyaWFibGVOYW1lICYmIHZhcmlhYmxlWzBdLnRhcmdldENvbGxlY3Rpb24gJiYgdmFyaWFibGVbMF0udGFyZ2V0VmFyaWFibGVOYW1lO1xuICAgIH0gZWxzZSBpZiAodHlwZSA9PT0gXCJyZWxhdGlvbi10by1leGlzdGluZ1wiKSB7XG4gICAgICByZXR1cm4gdmFyaWFibGVbMF0udmFyaWFibGVOYW1lICYmIHZhcmlhYmxlWzBdLnRhcmdldEV4aXN0aW5nVGltYnVjdG9vVnJlO1xuICAgIH1cbiAgICByZXR1cm4gdmFyaWFibGUuZmlsdGVyKChtKSA9PiBtLnZhcmlhYmxlTmFtZSkubGVuZ3RoID09PSB2YXJpYWJsZS5sZW5ndGg7XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBuYW1lLCB0eXBlLCBjdXN0b20sIHByb3BlcnR5TWFwcGluZyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHksIG9uQ29uZmlybUZpZWxkTWFwcGluZ3MsXG4gICAgICBvblVuY29uZmlybUZpZWxkTWFwcGluZ3MsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgY29uZmlybWVkID0gcHJvcGVydHlNYXBwaW5nICYmIHByb3BlcnR5TWFwcGluZy5jb25maXJtZWQ7XG5cbiAgICBjb25zdCBjb25maXJtQnV0dG9uID0gcHJvcGVydHlNYXBwaW5nICYmIHRoaXMuY2FuQ29uZmlybShwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGUpICYmICFjb25maXJtZWQgP1xuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLXN1Y2Nlc3NcIiBvbkNsaWNrPXsoKSA9PiBvbkNvbmZpcm1GaWVsZE1hcHBpbmdzKG5hbWUpfT5Db25maXJtPC9idXR0b24+IDogY29uZmlybWVkID9cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFua1wiIG9uQ2xpY2s9eygpID0+IG9uVW5jb25maXJtRmllbGRNYXBwaW5ncyhuYW1lKX0+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImhpLXN1Y2Nlc3MgZ2x5cGhpY29uIGdseXBoaWNvbi1va1wiIC8+PC9idXR0b24+IDogbnVsbDtcblxuICAgIGNvbnN0IGZvcm1Db21wb25lbnQgPSB0eXBlTWFwW3R5cGVdKHtcbiAgICAgIC4uLnRoaXMucHJvcHMsXG4gICAgICBvbkNvbHVtblNlbGVjdDogKHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhuYW1lLCB2YWx1ZSksXG4gICAgICBvbkNsZWFyQ29sdW1uOiAodmFsdWVJZHgpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcobmFtZSwgdmFsdWVJZHgpXG4gICAgfSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJyb3cgc21hbGwtbWFyZ2luXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTIgcGFkLTYtMTJcIj48c3Ryb25nPntuYW1lfTwvc3Ryb25nPjwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0yIHBhZC02LTEyXCIgPjxzcGFuIGNsYXNzTmFtZT1cInB1bGwtcmlnaHRcIj4oe3R5cGV9KTwvc3Bhbj48L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNVwiPlxuICAgICAgICAgIHtmb3JtQ29tcG9uZW50fVxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMVwiPlxuICAgICAgICAgIHsgY3VzdG9tXG4gICAgICAgICAgICA/ICg8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgcHVsbC1yaWdodFwiIHR5cGU9XCJidXR0b25cIiBvbkNsaWNrPXsoKSA9PiBvblJlbW92ZUN1c3RvbVByb3BlcnR5KG5hbWUpfT5cbiAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIvPlxuICAgICAgICAgIDwvYnV0dG9uPilcbiAgICAgICAgICAgIDogbnVsbCB9XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0yIGhpLXN1Y2Nlc3NcIj5cblxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cInB1bGwtcmlnaHRcIj5cbiAgICAgICAgICAgIHtjb25maXJtQnV0dG9ufVxuICAgICAgICAgIDwvc3Bhbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IFByb3BlcnR5Rm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQ29sdW1uU2VsZWN0IGZyb20gXCIuL2NvbHVtbi1zZWxlY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5cbmNsYXNzIEZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25Db2x1bW5TZWxlY3QsIG9uQ2xlYXJDb2x1bW4sIHByb3BlcnR5TWFwcGluZywgdGFyZ2V0YWJsZVZyZXMgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCByZWxhdGlvbkluZm8gPSBwcm9wZXJ0eU1hcHBpbmcgJiYgcHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlWzBdXG4gICAgICA/IHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXVxuICAgICAgOiB7fTtcblxuICAgIGNvbnN0IHNvdXJjZUNvbHVtblByb3BzID0ge1xuICAgICAgLi4udGhpcy5wcm9wcyxcbiAgICAgIHBsYWNlaG9sZGVyOiBcIlNlbGVjdCBhIHNvdXJjZSBjb2x1bW4uLi5cIixcbiAgICAgIG9uQ29sdW1uU2VsZWN0OiAodmFsdWUpID0+IG9uQ29sdW1uU2VsZWN0KFt7Li4ucmVsYXRpb25JbmZvLCB2YXJpYWJsZU5hbWU6IHZhbHVlWzBdLnZhcmlhYmxlTmFtZX1dKVxuICAgIH07XG5cblxuXG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdj5cbiAgICAgICAgPENvbHVtblNlbGVjdCB7Li4uc291cmNlQ29sdW1uUHJvcHN9IC8+XG4gICAgICAgIDxTZWxlY3RGaWVsZCB2YWx1ZT17cmVsYXRpb25JbmZvLnRhcmdldEV4aXN0aW5nVGltYnVjdG9vVnJlIHx8IG51bGx9XG4gICAgICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbHVlKSA9PiBvbkNvbHVtblNlbGVjdChbey4uLnJlbGF0aW9uSW5mbywgdGFyZ2V0RXhpc3RpbmdUaW1idWN0b29WcmU6IHZhbHVlfV0pfVxuICAgICAgICAgICAgICAgICAgICAgb25DbGVhcj17KCkgPT4gb25DbGVhckNvbHVtbigwKX0+XG5cbiAgICAgICAgICA8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIiBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+PGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBhbHQ9XCJcIi8+IFNlbGVjdCBhIHRhcmdldCBkYXRhc2V0Li4uPC9zcGFuPlxuICAgICAgICAgIHt0YXJnZXRhYmxlVnJlcy5tYXAoKGRhdGFzZXQpID0+IChcbiAgICAgICAgICAgIDxzcGFuIGtleT17ZGF0YXNldH0gdmFsdWU9e2RhdGFzZXR9IGNsYXNzTmFtZT1cImZyb20tZXhjZWxcIj48aW1nIHNyYz1cImltYWdlcy9pY29uLWV4Y2VsLnN2Z1wiIGFsdD1cIlwiLz4ge2RhdGFzZXR9PC9zcGFuPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L1NlbGVjdEZpZWxkPlxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbkZvcm0ucHJvcFR5cGVzID0ge1xuICBjb2xsZWN0aW9uRGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcbiAgbWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG9uQ2xlYXJGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICBvblNldEZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQ29sdW1uU2VsZWN0IGZyb20gXCIuL2NvbHVtbi1zZWxlY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5cbmNsYXNzIEZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25Db2x1bW5TZWxlY3QsIG9uQ2xlYXJDb2x1bW4sIHByb3BlcnR5TWFwcGluZywgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGUsIGFyY2hldHlwZUZpZWxkcyB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHJlbGF0aW9uSW5mbyA9IHByb3BlcnR5TWFwcGluZyAmJiBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGVbMF1cbiAgICAgID8gcHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlWzBdXG4gICAgICA6IHt9O1xuXG4gICAgY29uc3QgdGFyZ2V0QXJjaGV0eXBlQ29sbGVjdGlvbiA9IGFyY2hldHlwZUZpZWxkcy5maW5kKChhZikgPT4gYWYubmFtZSA9PT0gdGhpcy5wcm9wcy5uYW1lKS5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uO1xuICAgIGNvbnN0IHRhcmdldENvbGxlY3Rpb25Db2x1bW5zID0gYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGVbdGFyZ2V0QXJjaGV0eXBlQ29sbGVjdGlvbl07XG4gICAgY29uc3QgdGFyZ2V0Q29sbGVjdGlvbnMgPSB0YXJnZXRDb2xsZWN0aW9uQ29sdW1ucy5tYXAoKHQpID0+IHQuY29sbGVjdGlvbk5hbWUpO1xuXG4gICAgY29uc3Qgc291cmNlQ29sdW1uUHJvcHMgPSB7XG4gICAgICAuLi50aGlzLnByb3BzLFxuICAgICAgcGxhY2Vob2xkZXI6IFwiU2VsZWN0IGEgc291cmNlIGNvbHVtbi4uLlwiLFxuICAgICAgb25Db2x1bW5TZWxlY3Q6ICh2YWx1ZSkgPT4gb25Db2x1bW5TZWxlY3QoW3suLi5yZWxhdGlvbkluZm8sIHZhcmlhYmxlTmFtZTogdmFsdWVbMF0udmFyaWFibGVOYW1lfV0pXG4gICAgfTtcblxuICAgIGNvbnN0IHRhcmdldENvbHVtblByb3BzID0gcmVsYXRpb25JbmZvLnRhcmdldENvbGxlY3Rpb24gPyB7XG4gICAgICAuLi50aGlzLnByb3BzLFxuICAgICAgcGxhY2Vob2xkZXI6IFwiU2VsZWN0IGEgdGFyZ2V0IGNvbHVtbi4uLlwiLFxuICAgICAgcHJvcGVydHlNYXBwaW5nOiB7dmFyaWFibGU6IFt7dmFyaWFibGVOYW1lOiByZWxhdGlvbkluZm8udGFyZ2V0VmFyaWFibGVOYW1lIH1dfSxcbiAgICAgIGNvbHVtbnM6IHRhcmdldENvbGxlY3Rpb25Db2x1bW5zLmZpbmQoKHQpID0+IHQuY29sbGVjdGlvbk5hbWUgPT09IHJlbGF0aW9uSW5mby50YXJnZXRDb2xsZWN0aW9uKS5jb2x1bW5zXG4gICAgICAgIC5tYXAoKGNvbCkgPT4gKHtuYW1lOiBjb2x9KSksXG4gICAgICBvbkNvbHVtblNlbGVjdDogKHZhbHVlKSA9PiBvbkNvbHVtblNlbGVjdChbey4uLnJlbGF0aW9uSW5mbywgdGFyZ2V0VmFyaWFibGVOYW1lOiB2YWx1ZVswXS52YXJpYWJsZU5hbWV9XSlcbiAgICB9IDogbnVsbDtcblxuXG5cblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2PlxuICAgICAgICA8Q29sdW1uU2VsZWN0IHsuLi5zb3VyY2VDb2x1bW5Qcm9wc30gLz5cbiAgICAgICAgPFNlbGVjdEZpZWxkIHZhbHVlPXtyZWxhdGlvbkluZm8udGFyZ2V0Q29sbGVjdGlvbiB8fCBudWxsfVxuICAgICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25Db2x1bW5TZWxlY3QoW3suLi5yZWxhdGlvbkluZm8sIHRhcmdldENvbGxlY3Rpb246IHZhbHVlfV0pfVxuICAgICAgICAgICAgICAgICAgICAgb25DbGVhcj17KCkgPT4gb25DbGVhckNvbHVtbigwKX0+XG5cbiAgICAgICAgICA8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIiBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+PGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBhbHQ9XCJcIi8+IFNlbGVjdCBhIHRhcmdldCBzaGVldC4uLjwvc3Bhbj5cbiAgICAgICAgICB7dGFyZ2V0Q29sbGVjdGlvbnMubWFwKChjb2xsZWN0aW9uKSA9PiAoXG4gICAgICAgICAgICA8c3BhbiBrZXk9e2NvbGxlY3Rpb259IHZhbHVlPXtjb2xsZWN0aW9ufSBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+PGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBhbHQ9XCJcIi8+IHtjb2xsZWN0aW9ufTwvc3Bhbj5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAge3RhcmdldENvbHVtblByb3BzXG4gICAgICAgICAgPyA8Q29sdW1uU2VsZWN0IHsuLi50YXJnZXRDb2x1bW5Qcm9wc30gLz5cbiAgICAgICAgICA6IG51bGxcbiAgICAgICAgfVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbkZvcm0ucHJvcFR5cGVzID0ge1xuICBjb2xsZWN0aW9uRGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcbiAgbWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG9uQ2xlYXJGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICBvblNldEZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQ29sdW1uU2VsZWN0IGZyb20gXCIuL2NvbHVtbi1zZWxlY3RcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG4gIHJlbmRlcigpIHtcbiAgICByZXR1cm4gPENvbHVtblNlbGVjdCB7Li4udGhpcy5wcm9wc30gLz5cbiAgfVxufVxuXG5Gb3JtLnByb3BUeXBlcyA9IHtcbiAgY29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuICBuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBvbkNsZWFyRmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgb25TZXRGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGb3JtO1xuIiwiZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oYXBwU3RhdGUpIHtcbiAgcmV0dXJuIHtcbiAgICBpc1VwbG9hZGluZzogYXBwU3RhdGUuaW1wb3J0RGF0YS5pc1VwbG9hZGluZyxcbiAgICB1c2VySWQ6IGFwcFN0YXRlLnVzZXJkYXRhLnVzZXJJZCxcbiAgICB2cmVzOiBhcHBTdGF0ZS51c2VyZGF0YS5teVZyZXMgfHwge31cbiAgfVxufSIsIi8vIEhlbHBlciBmdW5jdGlvbiBmb3IgcmVhY3QtcmVkdXggY29ubmVjdFxuZnVuY3Rpb24gZ2V0Q29uZmlybWVkQ29scyhwcm9wcywgdmFyaWFibGVzKSB7XG4gIGNvbnN0IHsgbWFwcGluZ3MsIGltcG9ydERhdGE6IHsgYWN0aXZlQ29sbGVjdGlvbiB9IH0gPSBwcm9wcztcblxuICByZXR1cm4gdmFyaWFibGVzXG4gICAgLm1hcCgodmFsdWUsIGkpID0+ICh7dmFsdWU6IHZhbHVlLCBpbmRleDogaX0pKVxuICAgIC5maWx0ZXIoKGNvbFNwZWMpID0+IG1hcHBpbmdzLmNvbGxlY3Rpb25zW2FjdGl2ZUNvbGxlY3Rpb25dLm1hcHBpbmdzXG4gICAgICAuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcbiAgICAgIC5tYXAoKG0pID0+IG0udmFyaWFibGUubWFwKCh2KSA9PiB2LnZhcmlhYmxlTmFtZSkpXG4gICAgICAucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pXG4gICAgICAuaW5kZXhPZihjb2xTcGVjLnZhbHVlKSA+IC0xXG4gICAgKS5tYXAoKGNvbFNwZWMpID0+IGNvbFNwZWMuaW5kZXgpO1xufVxuXG4vLyBIZWxwZXIgZnVuY3Rpb24gZm9yIHJlYWN0LXJlZHV4IGNvbm5lY3RcbmZ1bmN0aW9uIG1hcHBpbmdzQXJlQ29tcGxldGUocHJvcHMsIHNoZWV0KSB7XG4gIGNvbnN0IHsgbWFwcGluZ3MgfSA9IHByb3BzO1xuXG4gIGNvbnN0IGNvbmZpcm1lZENvbENvdW50ID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0ubWFwcGluZ3NcbiAgICAuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcbiAgICAubWFwKChtKSA9PiBtLnZhcmlhYmxlLm1hcCgodikgPT4gdi52YXJpYWJsZU5hbWUpKVxuICAgIC5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSlcbiAgICAuZmlsdGVyKCh4LCBpZHgsIHNlbGYpID0+IHNlbGYuaW5kZXhPZih4KSA9PT0gaWR4KVxuICAgIC5sZW5ndGg7XG5cbiAgcmV0dXJuIGNvbmZpcm1lZENvbENvdW50ICsgbWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0uaWdub3JlZENvbHVtbnMubGVuZ3RoID09PSBzaGVldC52YXJpYWJsZXMubGVuZ3RoO1xufVxuXG5mdW5jdGlvbiBnZXRUYXJnZXRhYmxlVnJlcyhtaW5lLCB2cmVzKSB7XG4gIGNvbnN0IG15VnJlcyA9IE9iamVjdC5rZXlzKG1pbmUgfHwge30pXG4gICAgLm1hcCgoa2V5KSA9PiBtaW5lW2tleV0pXG4gICAgLmZpbHRlcigodnJlKSA9PiB2cmUucHVibGlzaGVkKVxuICAgIC5tYXAoKHZyZSkgPT4gdnJlLm5hbWUpO1xuICBjb25zdCBwdWJsaWNWcmVzID0gT2JqZWN0LmtleXModnJlcyB8fCB7fSlcbiAgICAubWFwKChrZXkpID0+IHZyZXNba2V5XS5uYW1lKTtcblxuICByZXR1cm4gbXlWcmVzLmNvbmNhdChwdWJsaWNWcmVzKTtcbn1cblxuLy8gTW92ZXMgdG8gcmVhY3QtcmVkdXggY29ubmVjdFxuZnVuY3Rpb24gdHJhbnNmb3JtUHJvcHMocHJvcHMpIHtcblxuICBjb25zdCB7XG4gICAgaW1wb3J0RGF0YTogeyBzaGVldHMsIGFjdGl2ZUNvbGxlY3Rpb24sIHVwbG9hZGVkRmlsZU5hbWUsIHB1Ymxpc2hFcnJvcnMgfSxcbiAgICB1c2VyZGF0YTogeyBteVZyZXMsIHZyZXMgfSxcbiAgICBtYXBwaW5ncyxcbiAgICBhcmNoZXR5cGVcbiAgfSA9IHByb3BzO1xuICBjb25zdCBjb2xsZWN0aW9uRGF0YSA9IHNoZWV0cy5maW5kKChzaGVldCkgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aXZlQ29sbGVjdGlvbik7XG4gIGNvbnN0IHsgcm93cywgdmFyaWFibGVzIH0gPSBjb2xsZWN0aW9uRGF0YTtcblxuICBjb25zdCBjb25maXJtZWRDb2xzID0gZ2V0Q29uZmlybWVkQ29scyhwcm9wcywgdmFyaWFibGVzKTtcbiAgY29uc3QgeyBpZ25vcmVkQ29sdW1ucyB9ID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbYWN0aXZlQ29sbGVjdGlvbl07XG5cbiAgY29uc3QgYXZhaWxhYmxlQXJjaGV0eXBlcyA9IE9iamVjdC5rZXlzKG1hcHBpbmdzLmNvbGxlY3Rpb25zKS5tYXAoKGtleSkgPT4gbWFwcGluZ3MuY29sbGVjdGlvbnNba2V5XS5hcmNoZXR5cGVOYW1lKTtcblxuICByZXR1cm4ge1xuICAgIHNoZWV0czogc2hlZXRzLFxuICAgIGFjdGl2ZUNvbGxlY3Rpb246IGFjdGl2ZUNvbGxlY3Rpb24sXG4gICAgdXBsb2FkZWRGaWxlTmFtZTogdXBsb2FkZWRGaWxlTmFtZSxcbiAgICBjb2xsZWN0aW9uVGFiczogc2hlZXRzLm1hcCgoc2hlZXQpID0+ICh7XG4gICAgICBjb2xsZWN0aW9uTmFtZTogc2hlZXQuY29sbGVjdGlvbixcbiAgICAgIGFyY2hldHlwZU5hbWU6IG1hcHBpbmdzLmNvbGxlY3Rpb25zW3NoZWV0LmNvbGxlY3Rpb25dLmFyY2hldHlwZU5hbWUsXG4gICAgICBhY3RpdmU6IGFjdGl2ZUNvbGxlY3Rpb24gPT09IHNoZWV0LmNvbGxlY3Rpb24sXG4gICAgICBjb21wbGV0ZTogbWFwcGluZ3NBcmVDb21wbGV0ZShwcm9wcywgc2hlZXQpXG4gICAgfSkpLFxuICAgIHJvd3M6IHJvd3MubWFwKChyb3cpID0+IHJvdy5tYXAoKGNlbGwsIGkpID0+ICh7XG4gICAgICB2YWx1ZTogY2VsbC52YWx1ZSxcbiAgICAgIGVycm9yOiBjZWxsLmVycm9yIHx8IG51bGwsXG4gICAgICBpZ25vcmVkOiBpZ25vcmVkQ29sdW1ucy5pbmRleE9mKHZhcmlhYmxlc1tpXSkgPiAtMVxuICAgIH0pKSksXG4gICAgaGVhZGVyczogdmFyaWFibGVzLm1hcCgodmFyaWFibGUsIGkpID0+ICh7XG4gICAgICBuYW1lOiB2YXJpYWJsZSxcbiAgICAgIGlzQ29uZmlybWVkOiBpZ25vcmVkQ29sdW1ucy5pbmRleE9mKGkpIDwgMCAmJiBjb25maXJtZWRDb2xzLmluZGV4T2YoaSkgPiAtMSxcbiAgICAgIGlzSWdub3JlZDogaWdub3JlZENvbHVtbnMuaW5kZXhPZih2YXJpYWJsZSkgPiAtMVxuICAgIH0pKSxcbiAgICBuZXh0VXJsOiBzaGVldHMuZmluZCgoc2hlZXQpID0+IHNoZWV0LmNvbGxlY3Rpb24gPT09IGFjdGl2ZUNvbGxlY3Rpb24pLm5leHRVcmwsXG4gICAgYXJjaGV0eXBlRmllbGRzOiBhcmNoZXR5cGVbbWFwcGluZ3MuY29sbGVjdGlvbnNbYWN0aXZlQ29sbGVjdGlvbl0uYXJjaGV0eXBlTmFtZV0sXG4gICAgcHJvcGVydHlNYXBwaW5nczogbWFwcGluZ3MuY29sbGVjdGlvbnNbYWN0aXZlQ29sbGVjdGlvbl0ubWFwcGluZ3MsXG4gICAgY3VzdG9tUHJvcGVydHlNYXBwaW5nczogbWFwcGluZ3MuY29sbGVjdGlvbnNbYWN0aXZlQ29sbGVjdGlvbl0uY3VzdG9tUHJvcGVydGllcyxcbiAgICBhdmFpbGFibGVBcmNoZXR5cGVzOiBhdmFpbGFibGVBcmNoZXR5cGVzLFxuICAgIHB1Ymxpc2hpbmc6IG1hcHBpbmdzLnB1Ymxpc2hpbmcsXG4gICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGU6IGF2YWlsYWJsZUFyY2hldHlwZXMubWFwKChhcmNoZXR5cGVOYW1lKSA9PiAoe1xuICAgICAga2V5OiBhcmNoZXR5cGVOYW1lLFxuICAgICAgdmFsdWVzOiBPYmplY3Qua2V5cyhtYXBwaW5ncy5jb2xsZWN0aW9ucylcbiAgICAgICAgLmZpbHRlcigoY29sbGVjdGlvbk5hbWUpID0+IG1hcHBpbmdzLmNvbGxlY3Rpb25zW2NvbGxlY3Rpb25OYW1lXS5hcmNoZXR5cGVOYW1lID09PSBhcmNoZXR5cGVOYW1lKVxuICAgICAgICAubWFwKChjb2xsZWN0aW9uTmFtZSkgPT4gKHtcbiAgICAgICAgICBjb2xsZWN0aW9uTmFtZTogY29sbGVjdGlvbk5hbWUsXG4gICAgICAgICAgY29sdW1uczogc2hlZXRzLmZpbmQoKHNoZWV0KSA9PiBzaGVldC5jb2xsZWN0aW9uID09PSBjb2xsZWN0aW9uTmFtZSkudmFyaWFibGVzXG4gICAgICAgIH0pKVxuICAgIH0pKS5yZWR1Y2UoKGFyY2hldHlwZVRvQ29sbGVjdGlvbkNvbHVtbk1hcHBpbmcsIGN1cnJlbnRBcmNoZXR5cGVBbmRDb2xzKSA9PiB7XG4gICAgICBhcmNoZXR5cGVUb0NvbGxlY3Rpb25Db2x1bW5NYXBwaW5nW2N1cnJlbnRBcmNoZXR5cGVBbmRDb2xzLmtleV0gPSBjdXJyZW50QXJjaGV0eXBlQW5kQ29scy52YWx1ZXM7XG4gICAgICByZXR1cm4gYXJjaGV0eXBlVG9Db2xsZWN0aW9uQ29sdW1uTWFwcGluZztcbiAgICB9LCB7fSksXG4gICAgcHVibGlzaEVycm9yczogcHVibGlzaEVycm9ycyxcbiAgICBzaG93Q29sbGVjdGlvbnNBcmVDb25uZWN0ZWRNZXNzYWdlOiAgcHJvcHMubWVzc2FnZXMuc2hvd0NvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZSxcbiAgICB0YXJnZXRhYmxlVnJlczogZ2V0VGFyZ2V0YWJsZVZyZXMobXlWcmVzLCB2cmVzKVxuICB9O1xufVxuXG5leHBvcnQgZGVmYXVsdCB0cmFuc2Zvcm1Qcm9wczsiLCJleHBvcnQgZGVmYXVsdCAoYXBwU3RhdGUpID0+ICh7XG4gIG1hcHBpbmdzOiBhcHBTdGF0ZS5tYXBwaW5ncyxcbiAgc2hlZXRzOiBhcHBTdGF0ZS5pbXBvcnREYXRhLnNoZWV0cyxcbiAgdXBsb2FkZWRGaWxlTmFtZTogYXBwU3RhdGUuaW1wb3J0RGF0YS51cGxvYWRlZEZpbGVOYW1lLFxuICBhcmNoZXR5cGU6IGFwcFN0YXRlLmFyY2hldHlwZSxcbiAgb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlOiBhcHBTdGF0ZS5vbk1hcENvbGxlY3Rpb25BcmNoZXR5cGUsXG4gIG9uQ29uZmlybUNvbGxlY3Rpb25BcmNoZXR5cGVNYXBwaW5nczogYXBwU3RhdGUub25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzLFxuICBzaG93RmlsZUlzVXBsb2FkZWRNZXNzYWdlOiBhcHBTdGF0ZS5tZXNzYWdlcy5zaG93RmlsZUlzVXBsb2FkZWRNZXNzYWdlXG59KTsiLCJleHBvcnQgZGVmYXVsdCBmdW5jdGlvbihhcHBTdGF0ZSkge1xuIHJldHVybiB7XG4gICB1c2VySWQ6IGFwcFN0YXRlLnVzZXJkYXRhLnVzZXJJZCxcbiAgIGlzVXBsb2FkaW5nOiBhcHBTdGF0ZS5pbXBvcnREYXRhLmlzVXBsb2FkaW5nXG4gfVxufSIsImltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vcm91dGVyXCI7XG5cbmV4cG9ydCBkZWZhdWx0IChhcHBTdGF0ZSwgb3duUHJvcHMpID0+IHtcbiAgY29uc3QgeyBsb2NhdGlvbjogeyBwYXRobmFtZSB9IH0gPSBvd25Qcm9wcztcbiAgcmV0dXJuIHtcbiAgICB1c2VybmFtZTogYXBwU3RhdGUudXNlcmRhdGEudXNlcklkLFxuICAgIHZyZXM6IGFwcFN0YXRlLnVzZXJkYXRhLnZyZXMsXG4gICAgc2hvd0RhdGFzZXRzOiBwYXRobmFtZSA9PT0gXCIvXCIgfHwgcGF0aG5hbWUgPT09IHVybHMuY29sbGVjdGlvbnNPdmVydmlldygpXG4gIH07XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgRGF0YVJvdyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgcm93IH0gPSB0aGlzLnByb3BzO1xuICAgIHJldHVybiAoXG4gICAgICA8dHI+XG4gICAgICAgIHtyb3cubWFwKChjZWxsLCBpKSA9PiAoXG4gICAgICAgICAgPHRkIGNsYXNzTmFtZT17Y3goe1xuICAgICAgICAgICAgaWdub3JlZDogY2VsbC5pZ25vcmVkLFxuICAgICAgICAgICAgZGFuZ2VyOiBjZWxsLmVycm9yID8gdHJ1ZSA6IGZhbHNlXG4gICAgICAgICAgfSl9IGtleT17aX0+XG4gICAgICAgICAgICB7Y2VsbC52YWx1ZX1cbiAgICAgICAgICAgIHtjZWxsLmVycm9yID8gPHNwYW4gY2xhc3NOYW1lPVwicHVsbC1yaWdodCBnbHlwaGljb24gZ2x5cGhpY29uLWV4Y2xhbWF0aW9uLXNpZ25cIiBzdHlsZT17e2N1cnNvcjogXCJwb2ludGVyXCJ9fSB0aXRsZT17Y2VsbC5lcnJvcn0gLz4gOiBudWxsfVxuICAgICAgICAgIDwvdGQ+XG4gICAgICAgICkpfVxuICAgICAgPC90cj5cbiAgICApO1xuICB9XG59XG5cbkRhdGFSb3cucHJvcFR5cGVzID0ge1xuICByb3c6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IERhdGFSb3c7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIEhlYWRlckNlbGwgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IGhlYWRlciwgaXNDb25maXJtZWQsIGlzSWdub3JlZCwgb25JZ25vcmVDb2x1bW5Ub2dnbGUgfSA9IHRoaXMucHJvcHM7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPHRoIGNsYXNzTmFtZT17Y3goe1xuICAgICAgICBzdWNjZXNzOiBpc0NvbmZpcm1lZCxcbiAgICAgICAgaW5mbzogIWlzQ29uZmlybWVkICYmICFpc0lnbm9yZWQsXG4gICAgICAgIGRhbmdlcjogIWlzQ29uZmlybWVkICYmIGlzSWdub3JlZFxuICAgICAgfSl9PlxuICAgICAgICB7aGVhZGVyfVxuICAgICAgICA8YSBzdHlsZT17e2N1cnNvcjogXCJwb2ludGVyXCJ9fSBjbGFzc05hbWU9e2N4KFwicHVsbC1yaWdodFwiLCBcImdseXBoaWNvblwiLCB7XG4gICAgICAgICAgXCJnbHlwaGljb24tb2stc2lnblwiOiBpc0NvbmZpcm1lZCxcbiAgICAgICAgICBcImdseXBoaWNvbi1xdWVzdGlvbi1zaWduXCI6ICFpc0NvbmZpcm1lZCAmJiAhaXNJZ25vcmVkLFxuICAgICAgICAgIFwiZ2x5cGhpY29uLXJlbW92ZVwiOiAhaXNDb25maXJtZWQgJiYgaXNJZ25vcmVkXG4gICAgICAgIH0pfSBvbkNsaWNrPXsoKSA9PiAhaXNDb25maXJtZWQgPyBvbklnbm9yZUNvbHVtblRvZ2dsZShoZWFkZXIpIDogbnVsbCB9ID5cbiAgICAgICAgPC9hPlxuICAgICAgPC90aD5cbiAgICApO1xuICB9XG59XG5cbkhlYWRlckNlbGwucHJvcFR5cGVzID0ge1xuICBoZWFkZXI6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIGlzQ29uZmlybWVkOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbCxcbiAgaXNJZ25vcmVkOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbCxcbiAgb25JZ25vcmVDb2x1bW5Ub2dnbGU6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJDZWxsOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5jbGFzcyBVcGxvYWRCdXR0b24gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IGNsYXNzTmFtZXMsIGlzVXBsb2FkaW5nLCBvblVwbG9hZEZpbGVTZWxlY3QsIGdseXBoaWNvbiwgbGFiZWwgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIChcbiAgICAgIDxmb3JtPlxuICAgICAgICA8bGFiZWwgY2xhc3NOYW1lPXtjeCguLi5jbGFzc05hbWVzLCB7ZGlzYWJsZWQ6IGlzVXBsb2FkaW5nfSl9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT17Z2x5cGhpY29ufT48L3NwYW4+XG4gICAgICAgICAge1wiIFwifVxuICAgICAgICAgIHtpc1VwbG9hZGluZyA/IFwiVXBsb2FkaW5nLi4uXCIgOiBsYWJlbCB9XG4gICAgICAgICAgPGlucHV0XG4gICAgICAgICAgICBkaXNhYmxlZD17aXNVcGxvYWRpbmd9XG4gICAgICAgICAgICBvbkNoYW5nZT17ZSA9PiBvblVwbG9hZEZpbGVTZWxlY3QoZS50YXJnZXQuZmlsZXMpfVxuICAgICAgICAgICAgc3R5bGU9e3tkaXNwbGF5OiBcIm5vbmVcIn19XG4gICAgICAgICAgICB0eXBlPVwiZmlsZVwiIC8+XG4gICAgICAgIDwvbGFiZWw+XG4gICAgICA8L2Zvcm0+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBVcGxvYWRCdXR0b247IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5pbXBvcnQgeGhybW9jayBmcm9tIFwieGhyLW1vY2tcIjtcbmltcG9ydCBzZXR1cE1vY2tzIGZyb20gXCIuL3NlcnZlcm1vY2tzXCI7XG5pbXBvcnQgcm91dGVyIGZyb20gXCIuL3JvdXRlclwiO1xuaW1wb3J0IGFjdGlvbnNNYWtlciBmcm9tIFwiLi9hY3Rpb25zXCI7XG5pbXBvcnQgeyBuYXZpZ2F0ZVRvIH0gZnJvbSBcIi4vcm91dGVyXCI7XG5cblxuaWYgKHByb2Nlc3MuZW52LlVTRV9NT0NLID09PSBcInRydWVcIikge1xuXHRjb25zb2xlLmxvZyhcIlVzaW5nIG1vY2sgc2VydmVyIVwiKVxuXHR2YXIgb3JpZyA9IHdpbmRvdy5YTUxIdHRwUmVxdWVzdDtcblx0eGhybW9jay5zZXR1cCgpOyAvL21vY2sgd2luZG93LlhNTEh0dHBSZXF1ZXN0IHVzYWdlc1xuXHR2YXIgbW9jayA9IHdpbmRvdy5YTUxIdHRwUmVxdWVzdDtcblx0d2luZG93LlhNTEh0dHBSZXF1ZXN0ID0gb3JpZztcblx0eGhyLlhNTEh0dHBSZXF1ZXN0ID0gbW9jaztcblx0eGhyLlhEb21haW5SZXF1ZXN0ID0gbW9jaztcblx0c2V0dXBNb2Nrcyh4aHJtb2NrLCBvcmlnKTtcbn1cbmxldCBhY3Rpb25zID0gYWN0aW9uc01ha2VyKG5hdmlnYXRlVG8sIHN0b3JlLmRpc3BhdGNoLmJpbmQoc3RvcmUpKTtcblxuZnVuY3Rpb24gY2hlY2tUb2tlbkluVXJsKHN0YXRlKSB7XG5cdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdGxldCBwYXJhbXMgPSBwYXRoLnNwbGl0KCcmJyk7XG5cblx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoJz0nKTtcblx0XHRpZihrZXkgPT09ICdoc2lkJyAmJiAhc3RhdGUudXNlcmRhdGEudXNlcklkKSB7XG5cdFx0XHRhY3Rpb25zLm9uVG9rZW4odmFsdWUpO1xuXG5cblx0XHRcdGJyZWFrO1xuXHRcdH1cblx0fVxufVxuXG5kb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiRE9NQ29udGVudExvYWRlZFwiLCAoKSA9PiB7XG5cdGxldCBzdGF0ZSA9IHN0b3JlLmdldFN0YXRlKCk7XG5cdGNoZWNrVG9rZW5JblVybChzdGF0ZSk7XG5cdGlmICghc3RhdGUuYXJjaGV0eXBlIHx8IE9iamVjdC5rZXlzKHN0YXRlLmFyY2hldHlwZSkubGVuZ3RoID09PSAwKSB7XG5cdFx0eGhyKHByb2Nlc3MuZW52LnNlcnZlciArIFwiL3YyLjEvbWV0YWRhdGEvQWRtaW5cIiwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0FSQ0hFVFlQRV9NRVRBREFUQVwiLCBkYXRhOiBKU09OLnBhcnNlKHJlc3AuYm9keSl9KTtcblx0XHR9KTtcblx0fVxuXHRSZWFjdERPTS5yZW5kZXIoXG5cdFx0cm91dGVyLFxuXHRcdGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKFwiYXBwXCIpXG5cdClcbn0pO1xuIiwiaW1wb3J0IHsgZ2V0SXRlbSB9IGZyb20gXCIuLi91dGlsL3BlcnNpc3RcIjtcblxuY29uc3QgaW5pdGlhbFN0YXRlID0gZ2V0SXRlbShcImFyY2hldHlwZVwiKSB8fCB7fTtcblxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9BUkNIRVRZUEVfTUVUQURBVEFcIjpcblx0XHRcdHJldHVybiBhY3Rpb24uZGF0YTtcblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn0iLCJpbXBvcnQgeyBnZXRJdGVtIH0gZnJvbSBcIi4uL3V0aWwvcGVyc2lzdFwiO1xuXG5jb25zdCBpbml0aWFsU3RhdGUgPSBnZXRJdGVtKFwiaW1wb3J0RGF0YVwiKSB8fCB7XG5cdGlzVXBsb2FkaW5nOiBmYWxzZSxcblx0c2hlZXRzOiBudWxsLFxuXHRhY3RpdmVDb2xsZWN0aW9uOiBudWxsLFxuXHRwdWJsaXNoRXJyb3JzOiBmYWxzZSxcblx0dXBsb2FkZWRGaWxlTmFtZTogdW5kZWZpbmVkXG59O1xuXG5mdW5jdGlvbiBmaW5kSW5kZXgoYXJyLCBmKSB7XG5cdGxldCBsZW5ndGggPSBhcnIubGVuZ3RoO1xuXHRmb3IgKHZhciBpID0gMDsgaSA8IGxlbmd0aDsgaSsrKSB7XG4gICAgaWYgKGYoYXJyW2ldLCBpLCBhcnIpKSB7XG4gICAgICByZXR1cm4gaTtcbiAgICB9XG4gIH1cblx0cmV0dXJuIC0xO1xufVxuXG5mdW5jdGlvbiBzaGVldFJvd0Zyb21EaWN0VG9BcnJheShyb3dkaWN0LCBhcnJheU9mVmFyaWFibGVOYW1lcywgbWFwcGluZ0Vycm9ycykge1xuXHRyZXR1cm4gYXJyYXlPZlZhcmlhYmxlTmFtZXMubWFwKG5hbWUgPT4gKHtcblx0XHR2YWx1ZTogcm93ZGljdFtuYW1lXSxcblx0XHRlcnJvcjogbWFwcGluZ0Vycm9yc1tuYW1lXSB8fCB1bmRlZmluZWRcblx0fSkpO1xufVxuXG5mdW5jdGlvbiBhZGRSb3dzKGN1clJvd3MsIG5ld1Jvd3MsIGFycmF5T2ZWYXJpYWJsZU5hbWVzKSB7XG5cdHJldHVybiBjdXJSb3dzLmNvbmNhdChcblx0XHRuZXdSb3dzLm1hcChpdGVtID0+IHNoZWV0Um93RnJvbURpY3RUb0FycmF5KGl0ZW0udmFsdWVzIHx8IHt9LCBhcnJheU9mVmFyaWFibGVOYW1lcywgaXRlbS5lcnJvcnMgfHwge30pKVxuXHQpO1xufVxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNUQVJUX1VQTE9BRFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5pbml0aWFsU3RhdGUsIGlzVXBsb2FkaW5nOiB0cnVlfTtcblx0XHRjYXNlIFwiRklOSVNIX1VQTE9BRFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSxcblx0XHRcdFx0aXNVcGxvYWRpbmc6IGZhbHNlLFxuXHRcdFx0XHRwdWJsaXNoRXJyb3JzOiBmYWxzZSxcblx0XHRcdFx0dXBsb2FkZWRGaWxlTmFtZTogYWN0aW9uLnVwbG9hZGVkRmlsZU5hbWUsXG5cdFx0XHRcdHNoZWV0czogYWN0aW9uLmRhdGEuY29sbGVjdGlvbnMubWFwKHNoZWV0ID0+ICh7XG5cdFx0XHRcdFx0Y29sbGVjdGlvbjogc2hlZXQubmFtZSxcblx0XHRcdFx0XHR2YXJpYWJsZXM6IHNoZWV0LnZhcmlhYmxlcyxcblx0XHRcdFx0XHRyb3dzOiBbXSxcblx0XHRcdFx0XHRuZXh0VXJsOiBzaGVldC5kYXRhLFxuXHRcdFx0XHRcdG5leHRVcmxXaXRoRXJyb3JzOiBzaGVldC5kYXRhV2l0aEVycm9yc1xuXHRcdFx0XHR9KSksXG5cdFx0XHRcdGFjdGl2ZUNvbGxlY3Rpb246IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zWzBdLm5hbWUsXG5cdFx0XHRcdHZyZTogYWN0aW9uLmRhdGEudnJlLFxuXHRcdFx0XHRzYXZlTWFwcGluZ1VybDogYWN0aW9uLmRhdGEuc2F2ZU1hcHBpbmcsXG5cdFx0XHRcdGV4ZWN1dGVNYXBwaW5nVXJsOiBhY3Rpb24uZGF0YS5leGVjdXRlTWFwcGluZ1xuXHRcdFx0fTtcblx0XHRjYXNlIFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX1NVQ0NFRURFRFwiOlxuXHRcdFx0bGV0IHNoZWV0SWR4ID0gZmluZEluZGV4KHN0YXRlLnNoZWV0cywgc2hlZXQgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aW9uLmNvbGxlY3Rpb24pO1xuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdHNoZWV0czogW1xuXHRcdFx0XHRcdC4uLnN0YXRlLnNoZWV0cy5zbGljZSgwLCBzaGVldElkeCksXG5cdFx0XHRcdFx0e1xuXHRcdFx0XHRcdFx0Li4uc3RhdGUuc2hlZXRzW3NoZWV0SWR4XSxcblx0XHRcdFx0XHRcdHJvd3M6IGFkZFJvd3Moc3RhdGUuc2hlZXRzW3NoZWV0SWR4XS5yb3dzLCBhY3Rpb24uZGF0YS5pdGVtcywgc3RhdGUuc2hlZXRzW3NoZWV0SWR4XS52YXJpYWJsZXMpLFxuXHRcdFx0XHRcdFx0bmV4dFVybDogYWN0aW9uLmRhdGEuX25leHRcblx0XHRcdFx0XHR9LFxuXHRcdFx0XHRcdC4uLnN0YXRlLnNoZWV0cy5zbGljZShzaGVldElkeCArIDEpXG5cdFx0XHRcdF1cblx0XHRcdH07XG5cblx0XHRjYXNlIFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0ZJTklTSEVEXCI6XG5cdFx0XHRjb25zdCByZXN1bHQgPSB7Li4uc3RhdGV9O1xuXHRcdFx0cmVzdWx0LnNoZWV0cyA9IHJlc3VsdC5zaGVldHMuc2xpY2UoKTtcblx0XHRcdHJlc3VsdC5zaGVldHNcblx0XHRcdFx0LmZvckVhY2goKHNoZWV0LCBpKSA9PiB7XG5cdFx0XHRcdFx0aWYgKHNoZWV0LmNvbGxlY3Rpb24gPT09IGFjdGlvbi5jb2xsZWN0aW9uKSB7XG5cdFx0XHRcdFx0XHRyZXN1bHQuc2hlZXRzW2ldID0ge1xuXHRcdFx0XHRcdFx0XHQuLi5zaGVldCxcblx0XHRcdFx0XHRcdFx0aXNMb2FkaW5nOiBmYWxzZVxuXHRcdFx0XHRcdFx0fTtcblx0XHRcdFx0XHR9XG5cdFx0XHRcdH0pO1xuXG5cdFx0XHRyZXR1cm4gcmVzdWx0O1xuXHRcdGNhc2UgXCJTRVRfQUNUSVZFX0NPTExFQ1RJT05cIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIGFjdGl2ZUNvbGxlY3Rpb246IGFjdGlvbi5jb2xsZWN0aW9ufTtcblx0XHRjYXNlIFwiUFVCTElTSF9IQURfRVJST1JcIjpcblx0XHRcdC8vIGNsZWFyIHRoZSBzaGVldHMgdG8gZm9yY2UgcmVsb2FkXG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0cHVibGlzaEVycm9yczogdHJ1ZSxcblx0XHRcdFx0c2hlZXRzOiBzdGF0ZS5zaGVldHMubWFwKChzaGVldCkgPT4gKHtcblx0XHRcdFx0XHQuLi5zaGVldCxcblx0XHRcdFx0XHRyb3dzOiBbXSxcblx0XHRcdFx0XHRuZXh0VXJsOiBzaGVldC5uZXh0VXJsV2l0aEVycm9yc1xuXHRcdFx0XHR9KSlcblx0XHRcdH07XG5cdFx0Y2FzZSBcIlBVQkxJU0hfU1VDQ0VFREVEXCI6XG5cdFx0XHQvLyBjbGVhciB0aGUgc2hlZXRzIHRvIGZvcmNlIHJlbG9hZFxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdHB1Ymxpc2hFcnJvcnM6IGZhbHNlLFxuXHRcdFx0XHRzaGVldHM6IHN0YXRlLnNoZWV0cy5tYXAoKHNoZWV0KSA9PiAoe1xuXHRcdFx0XHRcdC4uLnNoZWV0LFxuXHRcdFx0XHRcdHJvd3M6IFtdLFxuXHRcdFx0XHRcdG5leHRVcmw6IHNoZWV0Lm5leHRVcmxXaXRoRXJyb3JzXG5cdFx0XHRcdH0pKVxuXHRcdFx0fTtcblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn1cbiIsImltcG9ydCBpbXBvcnREYXRhIGZyb20gXCIuL2ltcG9ydC1kYXRhXCI7XG5pbXBvcnQgYXJjaGV0eXBlIGZyb20gXCIuL2FyY2hldHlwZVwiO1xuaW1wb3J0IG1hcHBpbmdzIGZyb20gXCIuL21hcHBpbmdzXCI7XG5pbXBvcnQgdXNlcmRhdGEgZnJvbSBcIi4vdXNlcmRhdGFcIjtcbmltcG9ydCBtZXNzYWdlcyBmcm9tIFwiLi9tZXNzYWdlc1wiO1xuXG5leHBvcnQgZGVmYXVsdCB7XG5cdG1lc3NhZ2VzOiBtZXNzYWdlcyxcblx0aW1wb3J0RGF0YTogaW1wb3J0RGF0YSxcblx0YXJjaGV0eXBlOiBhcmNoZXR5cGUsXG5cdG1hcHBpbmdzOiBtYXBwaW5ncyxcblx0dXNlcmRhdGE6IHVzZXJkYXRhXG59O1xuIiwiaW1wb3J0IHNldEluIGZyb20gXCIuLi91dGlsL3NldC1pblwiO1xuaW1wb3J0IGdldEluIGZyb20gXCIuLi91dGlsL2dldC1pblwiO1xuaW1wb3J0IHsgZ2V0SXRlbSB9IGZyb20gXCIuLi91dGlsL3BlcnNpc3RcIjtcblxuY29uc3QgbmV3VmFyaWFibGVEZXNjID0gKHByb3BlcnR5LCB2YXJpYWJsZVNwZWMpID0+ICh7XG4gIHByb3BlcnR5OiBwcm9wZXJ0eSxcbiAgdmFyaWFibGU6IHZhcmlhYmxlU3BlYyxcbiAgZGVmYXVsdFZhbHVlOiBbXSxcbiAgY29uZmlybWVkOiBmYWxzZSxcbiAgdmFsdWVNYXBwaW5nczoge31cbn0pO1xuXG5mdW5jdGlvbiBzY2FmZm9sZENvbGxlY3Rpb25NYXBwaW5ncyhpbml0LCBzaGVldCkge1xuICByZXR1cm4gT2JqZWN0LmFzc2lnbihpbml0LCB7XG4gICAgW3NoZWV0Lm5hbWVdOiB7XG4gICAgICBhcmNoZXR5cGVOYW1lOiBudWxsLFxuICAgICAgbWFwcGluZ3M6IFtdLFxuICAgICAgaWdub3JlZENvbHVtbnM6IFtdLFxuICAgICAgY3VzdG9tUHJvcGVydGllczogW11cbiAgICB9XG4gIH0pO1xufVxuXG5jb25zdCBpbml0aWFsU3RhdGUgPSBnZXRJdGVtKFwibWFwcGluZ3NcIikgfHwge1xuICBjb2xsZWN0aW9uczoge30sXG4gIGNvbmZpcm1lZDogZmFsc2UsXG4gIHB1Ymxpc2hpbmc6IGZhbHNlXG59O1xuXG5jb25zdCBnZXRNYXBwaW5nSW5kZXggPSAoc3RhdGUsIGFjdGlvbikgPT5cbiAgc3RhdGUuY29sbGVjdGlvbnNbYWN0aW9uLmNvbGxlY3Rpb25dLm1hcHBpbmdzXG4gICAgLm1hcCgobSwgaSkgPT4gKHtpbmRleDogaSwgbTogbX0pKVxuICAgIC5maWx0ZXIoKG1TcGVjKSA9PiBtU3BlYy5tLnByb3BlcnR5ID09PSBhY3Rpb24ucHJvcGVydHlGaWVsZClcbiAgICAucmVkdWNlKChwcmV2LCBjdXIpID0+IGN1ci5pbmRleCwgLTEpO1xuXG5jb25zdCBtYXBDb2xsZWN0aW9uQXJjaGV0eXBlID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcbiAgbGV0IG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImFyY2hldHlwZU5hbWVcIl0sIGFjdGlvbi52YWx1ZSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuICBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgW10sIG5ld0NvbGxlY3Rpb25zKTtcblxuICByZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuY29uc3QgdXBzZXJ0RmllbGRNYXBwaW5nID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcbiAgY29uc3QgZm91bmRJZHggPSBnZXRNYXBwaW5nSW5kZXgoc3RhdGUsIGFjdGlvbik7XG4gIGNvbnN0IG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCIsIGZvdW5kSWR4IDwgMCA/IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpLmxlbmd0aCA6IGZvdW5kSWR4XSxcbiAgICBuZXdWYXJpYWJsZURlc2MoYWN0aW9uLnByb3BlcnR5RmllbGQsIGFjdGlvbi5pbXBvcnRlZEZpZWxkKSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXG5cbiAgcmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmNvbnN0IGNsZWFyRmllbGRNYXBwaW5nID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcbiAgY29uc3QgZm91bmRJZHggPSBnZXRNYXBwaW5nSW5kZXgoc3RhdGUsIGFjdGlvbik7XG4gIGlmIChmb3VuZElkeCA8IDApIHsgcmV0dXJuIHN0YXRlOyB9XG5cbiAgY29uc3QgY3VycmVudCA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiLCBmb3VuZElkeCwgXCJ2YXJpYWJsZVwiXSwgc3RhdGUuY29sbGVjdGlvbnMpXG4gICAgLmZpbHRlcigobSwgaSkgPT4gaSAhPT0gYWN0aW9uLmNsZWFySW5kZXgpO1xuXG4gIGxldCBuZXdDb2xsZWN0aW9ucztcbiAgaWYgKGN1cnJlbnQubGVuZ3RoID4gMCkge1xuICAgIG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCIsIGZvdW5kSWR4LCBcInZhcmlhYmxlXCJdLCBjdXJyZW50LCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG4gIH0gZWxzZSB7XG4gICAgY29uc3QgbmV3TWFwcGluZ3MgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKVxuICAgICAgLmZpbHRlcigobSwgaSkgPT4gaSAhPT0gZm91bmRJZHgpO1xuICAgIG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBuZXdNYXBwaW5ncywgc3RhdGUuY29sbGVjdGlvbnMpO1xuICB9XG5cblxuICByZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuY29uc3Qgc2V0RGVmYXVsdFZhbHVlID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcbiAgY29uc3QgZm91bmRJZHggPSBnZXRNYXBwaW5nSW5kZXgoc3RhdGUsIGFjdGlvbik7XG4gIGlmIChmb3VuZElkeCA+IC0xKSB7XG4gICAgY29uc3QgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHgsIFwiZGVmYXVsdFZhbHVlXCJdLCBhY3Rpb24udmFsdWUsIHN0YXRlLmNvbGxlY3Rpb25zKTtcbiAgICByZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufTtcblxuY29uc3Qgc2V0RmllbGRDb25maXJtYXRpb24gPSAoc3RhdGUsIGFjdGlvbiwgdmFsdWUpID0+IHtcbiAgY29uc3QgY3VycmVudCA9IChnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKSB8fCBbXSlcbiAgICAubWFwKCh2bSkgPT4gKHsuLi52bSwgY29uZmlybWVkOiBhY3Rpb24ucHJvcGVydHlGaWVsZCA9PT0gdm0ucHJvcGVydHkgPyB2YWx1ZSA6IHZtLmNvbmZpcm1lZH0pKTtcbiAgbGV0IG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBjdXJyZW50LCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cbiAgaWYgKHZhbHVlID09PSB0cnVlKSB7XG4gICAgY29uc3QgY29uZmlybWVkVmFyaWFibGVOYW1lcyA9IGN1cnJlbnQubWFwKChtKSA9PiBtLnZhcmlhYmxlLm1hcCgodikgPT4gdi52YXJpYWJsZU5hbWUpKS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpKTtcbiAgICBjb25zdCBuZXdJZ25vcmVkQ29sdW1zID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImlnbm9yZWRDb2x1bW5zXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucylcbiAgICAgIC5maWx0ZXIoKGljKSA9PiBjb25maXJtZWRWYXJpYWJsZU5hbWVzLmluZGV4T2YoaWMpIDwgMCk7XG4gICAgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIG5ld0lnbm9yZWRDb2x1bXMsIG5ld0NvbGxlY3Rpb25zKTtcbiAgfVxuXG4gIHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCBzZXRWYWx1ZU1hcHBpbmcgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuICBjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblxuICBpZiAoZm91bmRJZHggPiAtMSkge1xuICAgIGNvbnN0IG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCIsIGZvdW5kSWR4LCBcInZhbHVlTWFwcGluZ3NcIiwgYWN0aW9uLnRpbVZhbHVlXSxcbiAgICAgIGFjdGlvbi5tYXBWYWx1ZSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuICAgIHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG4gIH1cbiAgcmV0dXJuIHN0YXRlO1xufTtcblxuY29uc3QgdG9nZ2xlSWdub3JlZENvbHVtbiA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG4gIGxldCBjdXJyZW50ID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImlnbm9yZWRDb2x1bW5zXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cbiAgaWYgKGN1cnJlbnQuaW5kZXhPZihhY3Rpb24udmFyaWFibGVOYW1lKSA8IDApIHtcbiAgICBjdXJyZW50LnB1c2goYWN0aW9uLnZhcmlhYmxlTmFtZSk7XG4gIH0gZWxzZSB7XG4gICAgY3VycmVudCA9IGN1cnJlbnQuZmlsdGVyKChjKSA9PiBjICE9PSBhY3Rpb24udmFyaWFibGVOYW1lKTtcbiAgfVxuXG4gIHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIGN1cnJlbnQsIHN0YXRlLmNvbGxlY3Rpb25zKSB9O1xufTtcblxuY29uc3QgYWRkQ3VzdG9tUHJvcGVydHkgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuICBjb25zdCBjdXJyZW50ID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImN1c3RvbVByb3BlcnRpZXNcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKTtcbiAgY29uc3QgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiY3VzdG9tUHJvcGVydGllc1wiLCBjdXJyZW50Lmxlbmd0aF0sIHtuYW1lOiBhY3Rpb24ucHJvcGVydHlGaWVsZCwgdHlwZTogYWN0aW9uLnByb3BlcnR5VHlwZX0sIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuICByZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuY29uc3QgcmVtb3ZlQ3VzdG9tUHJvcGVydHkgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuICBjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblxuICBjb25zdCBjdXJyZW50ID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImN1c3RvbVByb3BlcnRpZXNcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKVxuICAgIC5maWx0ZXIoKGNwKSA9PiBjcC5uYW1lICE9PSBhY3Rpb24ucHJvcGVydHlGaWVsZCk7XG5cbiAgbGV0IG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImN1c3RvbVByb3BlcnRpZXNcIl0sIGN1cnJlbnQsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuICBpZiAoZm91bmRJZHggPiAtMSkge1xuICAgIGNvbnN0IG5ld01hcHBpbmdzID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucylcbiAgICAgIC5maWx0ZXIoKG0sIGkpID0+IGkgIT09IGZvdW5kSWR4KTtcbiAgICBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgbmV3TWFwcGluZ3MsIG5ld0NvbGxlY3Rpb25zKTtcbiAgfVxuXG4gIHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuICBzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG4gICAgICAgIGNhc2UgXCJTVEFSVF9VUExPQURcIjpcbiAgICAgIHJldHVybiBpbml0aWFsU3RhdGU7XG4gICAgY2FzZSBcIkZJTklTSF9VUExPQURcIjpcbiAgICAgIHJldHVybiBPYmplY3Qua2V5cyhzdGF0ZS5jb2xsZWN0aW9ucykubGVuZ3RoID4gMCA/XG4gICAgICAgIHN0YXRlIDpcbiAgICAgICAgey4uLnN0YXRlLCBjb2xsZWN0aW9uczogYWN0aW9uLmRhdGEuY29sbGVjdGlvbnMucmVkdWNlKHNjYWZmb2xkQ29sbGVjdGlvbk1hcHBpbmdzLCB7fSl9O1xuXG4gICAgY2FzZSBcIk1BUF9DT0xMRUNUSU9OX0FSQ0hFVFlQRVwiOlxuICAgICAgcmV0dXJuIG1hcENvbGxlY3Rpb25BcmNoZXR5cGUoc3RhdGUsIGFjdGlvbik7XG5cbiAgICBjYXNlIFwiQ09ORklSTV9DT0xMRUNUSU9OX0FSQ0hFVFlQRV9NQVBQSU5HU1wiOlxuICAgICAgcmV0dXJuIHsuLi5zdGF0ZSwgY29uZmlybWVkOiB0cnVlfTtcblxuICAgIGNhc2UgXCJTRVRfRklFTERfTUFQUElOR1wiOlxuICAgICAgcmV0dXJuIHVwc2VydEZpZWxkTWFwcGluZyhzdGF0ZSwgYWN0aW9uKTtcblxuICAgIGNhc2UgXCJDTEVBUl9GSUVMRF9NQVBQSU5HXCI6XG4gICAgICByZXR1cm4gY2xlYXJGaWVsZE1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG5cbiAgICBjYXNlIFwiU0VUX0RFRkFVTFRfVkFMVUVcIjpcbiAgICAgIHJldHVybiBzZXREZWZhdWx0VmFsdWUoc3RhdGUsIGFjdGlvbik7XG5cbiAgICBjYXNlIFwiQ09ORklSTV9GSUVMRF9NQVBQSU5HU1wiOlxuICAgICAgcmV0dXJuIHNldEZpZWxkQ29uZmlybWF0aW9uKHN0YXRlLCBhY3Rpb24sIHRydWUpO1xuXG4gICAgY2FzZSBcIlVOQ09ORklSTV9GSUVMRF9NQVBQSU5HU1wiOlxuICAgICAgcmV0dXJuIHNldEZpZWxkQ29uZmlybWF0aW9uKHN0YXRlLCBhY3Rpb24sIGZhbHNlKTtcblxuICAgIGNhc2UgXCJTRVRfVkFMVUVfTUFQUElOR1wiOlxuICAgICAgcmV0dXJuIHNldFZhbHVlTWFwcGluZyhzdGF0ZSwgYWN0aW9uKTtcblxuICAgIGNhc2UgXCJUT0dHTEVfSUdOT1JFRF9DT0xVTU5cIjpcbiAgICAgIHJldHVybiB0b2dnbGVJZ25vcmVkQ29sdW1uKHN0YXRlLCBhY3Rpb24pO1xuXG4gICAgY2FzZSBcIkFERF9DVVNUT01fUFJPUEVSVFlcIjpcbiAgICAgIHJldHVybiBhZGRDdXN0b21Qcm9wZXJ0eShzdGF0ZSwgYWN0aW9uKTtcblxuICAgIGNhc2UgXCJSRU1PVkVfQ1VTVE9NX1BST1BFUlRZXCI6XG4gICAgICByZXR1cm4gcmVtb3ZlQ3VzdG9tUHJvcGVydHkoc3RhdGUsIGFjdGlvbik7XG5cbiAgICBjYXNlIFwiUFVCTElTSF9TVEFSVEVEXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICAuLi5zdGF0ZSxcbiAgICAgICAgcHVibGlzaGluZzogdHJ1ZVxuICAgICAgfTtcbiAgICBjYXNlIFwiUFVCTElTSF9GSU5JU0hFRFwiOlxuICAgICAgcmV0dXJuIHtcbiAgICAgICAgLi4uc3RhdGUsXG4gICAgICAgIHB1Ymxpc2hpbmc6IGZhbHNlXG4gICAgICB9O1xuICB9XG4gIHJldHVybiBzdGF0ZTtcbn1cbiIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgc2hvd0ZpbGVJc1VwbG9hZGVkTWVzc2FnZTogdHJ1ZSxcbiAgc2hvd0NvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZTogdHJ1ZVxufTtcblxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuICBzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG4gICAgY2FzZSBcIlRPR0dMRV9NRVNTQUdFXCI6XG4gICAgICBjb25zdCBuZXdTdGF0ZSA9IHsuLi5zdGF0ZX07XG4gICAgICBuZXdTdGF0ZVthY3Rpb24ubWVzc2FnZUlkXSA9ICFzdGF0ZVthY3Rpb24ubWVzc2FnZUlkXVxuICAgICAgcmV0dXJuIG5ld1N0YXRlO1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufSIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgdXNlcklkOiB1bmRlZmluZWQsXG4gIG15VnJlczogdW5kZWZpbmVkLFxuICB2cmVzOiB7fVxufTtcblxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuICBzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG4gICAgY2FzZSBcIkxPR0lOXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICB1c2VySWQ6IGFjdGlvbi5kYXRhLFxuICAgICAgICBteVZyZXM6IGFjdGlvbi52cmVEYXRhID8gYWN0aW9uLnZyZURhdGEubWluZSA6IHt9LFxuICAgICAgICB2cmVzOiBhY3Rpb24udnJlRGF0YSA/IGFjdGlvbi52cmVEYXRhLnB1YmxpYyA6IHt9XG4gICAgICB9O1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufSIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCB7IFJvdXRlciwgUm91dGUsIGhhc2hIaXN0b3J5LCBJbmRleFJvdXRlIH0gZnJvbSAncmVhY3Qtcm91dGVyJ1xuaW1wb3J0IHsgUHJvdmlkZXIsIGNvbm5lY3QgfSBmcm9tIFwicmVhY3QtcmVkdXhcIlxuaW1wb3J0IHN0b3JlIGZyb20gXCIuL3N0b3JlXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9hY3Rpb25zXCI7XG5cbmltcG9ydCBzdGF0ZVRvTWFwRGF0YVByb3BzIGZyb20gXCIuL2NvbXBvbmVudHMvcmVhY3QtcmVkdXgtY29ubmVjdC9jb25uZWN0LWRhdGFcIjtcbmltcG9ydCBzdGF0ZVRvUGFnZVByb3BzIGZyb20gXCIuL2NvbXBvbmVudHMvcmVhY3QtcmVkdXgtY29ubmVjdC9wYWdlXCI7XG5pbXBvcnQgc3RhdGVUb0ZpcnN0VXBsb2FkUHJvcHMgZnJvbSBcIi4vY29tcG9uZW50cy9yZWFjdC1yZWR1eC1jb25uZWN0L2ZpcnN0LXVwbG9hZFwiO1xuaW1wb3J0IHN0YXRlVG9Db2xsZWN0aW9uT3ZlcnZpZXcgZnJvbSBcIi4vY29tcG9uZW50cy9yZWFjdC1yZWR1eC1jb25uZWN0L2NvbGxlY3Rpb24tb3ZlcnZpZXdcIjtcbmltcG9ydCBzdGF0ZVRvTWFwQXJjaGV0eXBlcyBmcm9tIFwiLi9jb21wb25lbnRzL3JlYWN0LXJlZHV4LWNvbm5lY3QvY29ubmVjdC10by1hcmNoZXR5cGVcIjtcblxuaW1wb3J0IEZpcnN0VXBsb2FkIGZyb20gXCIuL2NvbXBvbmVudHMvZmlyc3RVcGxvYWQuanN4XCI7XG5pbXBvcnQgUGFnZSBmcm9tIFwiLi9jb21wb25lbnRzL3BhZ2UuanN4XCI7XG5pbXBvcnQgQ29sbGVjdGlvbk92ZXJ2aWV3IGZyb20gXCIuL2NvbXBvbmVudHMvY29sbGVjdGlvbi1vdmVydmlld1wiO1xuaW1wb3J0IENvbm5lY3RUb0FyY2hldHlwZSBmcm9tIFwiLi9jb21wb25lbnRzL2Nvbm5lY3QtdG8tYXJjaGV0eXBlXCI7XG5pbXBvcnQgQ29ubmVjdERhdGEgZnJvbSBcIi4vY29tcG9uZW50cy9jb25uZWN0LWRhdGFcIjtcblxuXG52YXIgdXJscyA9IHtcbiAgbWFwRGF0YSgpIHtcbiAgICByZXR1cm4gXCIvbWFwZGF0YVwiO1xuICB9LFxuICBtYXBBcmNoZXR5cGVzKCkge1xuICAgIHJldHVybiBcIi9tYXBhcmNoZXR5cGVzXCI7XG4gIH0sXG4gIGNvbGxlY3Rpb25zT3ZlcnZpZXcoKSB7XG4gICAgcmV0dXJuIFwiL2NvbGxlY3Rpb25zLW92ZXJ2aWV3XCI7XG4gIH1cbn07XG5cbmV4cG9ydCBmdW5jdGlvbiBuYXZpZ2F0ZVRvKGtleSwgYXJncykge1xuICBoYXNoSGlzdG9yeS5wdXNoKHVybHNba2V5XS5hcHBseShudWxsLCBhcmdzKSk7XG59XG5cbmNvbnN0IGNvbm5lY3RDb21wb25lbnQgPSAoc3RhdGVUb1Byb3BzKSA9PiBjb25uZWN0KHN0YXRlVG9Qcm9wcywgZGlzcGF0Y2ggPT4gYWN0aW9ucyhuYXZpZ2F0ZVRvLCBkaXNwYXRjaCkpO1xuXG5jb25zdCByb3V0ZXIgPSAoXG4gIDxQcm92aWRlciBzdG9yZT17c3RvcmV9PlxuICAgIDxSb3V0ZXIgaGlzdG9yeT17aGFzaEhpc3Rvcnl9PlxuICAgICAgPFJvdXRlIHBhdGg9XCIvXCIgY29tcG9uZW50PXtjb25uZWN0Q29tcG9uZW50KHN0YXRlVG9QYWdlUHJvcHMpKFBhZ2UpfT5cbiAgICAgICAgPEluZGV4Um91dGUgY29tcG9uZW50PXtjb25uZWN0Q29tcG9uZW50KHN0YXRlVG9GaXJzdFVwbG9hZFByb3BzKShGaXJzdFVwbG9hZCl9Lz5cbiAgICAgICAgPFJvdXRlIHBhdGg9e3VybHMuY29sbGVjdGlvbnNPdmVydmlldygpfSBjb21wb25lbnRzPXtjb25uZWN0Q29tcG9uZW50KHN0YXRlVG9Db2xsZWN0aW9uT3ZlcnZpZXcpKENvbGxlY3Rpb25PdmVydmlldyl9IC8+XG4gICAgICAgIDxSb3V0ZSBwYXRoPXt1cmxzLm1hcEFyY2hldHlwZXMoKX0gY29tcG9uZW50cz17Y29ubmVjdENvbXBvbmVudChzdGF0ZVRvTWFwQXJjaGV0eXBlcykoQ29ubmVjdFRvQXJjaGV0eXBlKX0gLz5cbiAgICAgICAgPFJvdXRlIHBhdGg9e3VybHMubWFwRGF0YSgpfSBjb21wb25lbnRzPXtjb25uZWN0Q29tcG9uZW50KHN0YXRlVG9NYXBEYXRhUHJvcHMpKENvbm5lY3REYXRhKX0gLz5cbiAgICAgIDwvUm91dGU+XG4gICAgPC9Sb3V0ZXI+XG4gIDwvUHJvdmlkZXI+XG4pO1xuXG5leHBvcnQgZGVmYXVsdCByb3V0ZXI7XG5leHBvcnQgeyB1cmxzIH07IiwiZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24gc2V0dXBNb2Nrcyh4aHJtb2NrLCBvcmlnKSB7XG4gIHhocm1vY2tcbiAgICAuZ2V0KFwiaHR0cDovL3Rlc3QucmVwb3NpdG9yeS5odXlnZW5zLmtuYXcubmwvdjIuMS9tZXRhZGF0YS9BZG1pblwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoYHtcbiAgICAgICAgICBcInBlcnNvbnNcIjogW1xuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJuYW1lXCIsXG4gICAgICAgICAgICAgIFwidHlwZVwiOiBcInRleHRcIlxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwiaGFzV3JpdHRlblwiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJyZWxhdGlvblwiLFxuICAgICAgICAgICAgICBcInF1aWNrc2VhcmNoXCI6IFwiL3YyLjEvZG9tYWluL2RvY3VtZW50cy9hdXRvY29tcGxldGVcIixcbiAgICAgICAgICAgICAgXCJyZWxhdGlvblwiOiB7XG4gICAgICAgICAgICAgICAgXCJkaXJlY3Rpb25cIjogXCJPVVRcIixcbiAgICAgICAgICAgICAgICBcIm91dE5hbWVcIjogXCJoYXNXcml0dGVuXCIsXG4gICAgICAgICAgICAgICAgXCJpbk5hbWVcIjogXCJ3YXNXcml0dGVuQnlcIixcbiAgICAgICAgICAgICAgICBcInRhcmdldENvbGxlY3Rpb25cIjogXCJkb2N1bWVudHNcIixcbiAgICAgICAgICAgICAgICBcInJlbGF0aW9uQ29sbGVjdGlvblwiOiBcInJlbGF0aW9uc1wiLFxuICAgICAgICAgICAgICAgIFwicmVsYXRpb25UeXBlSWRcIjogXCJiYmExMGQzNy04NmNjLTRmMWYtYmEyZC0wMTZhZjJiMjFhYTRcIlxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJpc1JlbGF0ZWRUb1wiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJyZWxhdGlvblwiLFxuICAgICAgICAgICAgICBcInF1aWNrc2VhcmNoXCI6IFwiL3YyLjEvZG9tYWluL3BlcnNvbnMvYXV0b2NvbXBsZXRlXCIsXG4gICAgICAgICAgICAgIFwicmVsYXRpb25cIjoge1xuICAgICAgICAgICAgICAgIFwiZGlyZWN0aW9uXCI6IFwiT1VUXCIsXG4gICAgICAgICAgICAgICAgXCJvdXROYW1lXCI6IFwiaXNSZWxhdGVkVG9cIixcbiAgICAgICAgICAgICAgICBcImluTmFtZVwiOiBcImlzUmVsYXRlZFRvXCIsXG4gICAgICAgICAgICAgICAgXCJ0YXJnZXRDb2xsZWN0aW9uXCI6IFwicGVyc29uc1wiLFxuICAgICAgICAgICAgICAgIFwicmVsYXRpb25Db2xsZWN0aW9uXCI6IFwicmVsYXRpb25zXCIsXG4gICAgICAgICAgICAgICAgXCJyZWxhdGlvblR5cGVJZFwiOiBcImNiYTEwZDM3LTg2Y2MtNGYxZi1iYTJkLTAxNmFmMmIyMWFhNVwiXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgICBdLFxuICAgICAgICAgIFwiZG9jdW1lbnRzXCI6IFtcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwibmFtZVwiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJ0ZXh0XCJcbiAgICAgICAgICAgIH1cbiAgICAgICAgICBdXG4gICAgICAgIH1gKTtcbiAgICB9KVxuICAgIC5nZXQoXCJodHRwOi8vdGVzdC5yZXBvc2l0b3J5Lmh1eWdlbnMua25hdy5ubC92Mi4xL3N5c3RlbS91c2Vycy9tZS92cmVzXCIsIGZ1bmN0aW9uKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJmZXRjaC1teS12cmVzXCIpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5ib2R5KGB7XG4gICAgICAgICAgXCJtaW5lXCI6IHtcbiAgICAgICAgICAgIFwibWlncmFudF9zdGVla3Byb2VmX21hc3RlcmRiICg2KS54bHN4XCI6IHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwibWlncmFudF9zdGVla3Byb2VmX21hc3RlcmRiICg2KS54bHN4XCIsXG4gICAgICAgICAgICAgIFwicHVibGlzaGVkXCI6IHRydWVcbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBcInRlc3RlcmRldGVzdFwiOiB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcInRlc3RlcmRldGVzdFwiLFxuICAgICAgICAgICAgICBcInB1Ymxpc2hlZFwiOiBmYWxzZSxcbiAgICAgICAgICAgICAgXCJybWxVcmlcIjogXCI8PFRoZSBnZXQgcmF3IGRhdGEgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCJcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9LFxuICAgICAgICAgIFwicHVibGljXCI6IHtcbiAgICAgICAgICAgIFwiV29tZW5Xcml0ZXJzXCI6IHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwiV29tZW5Xcml0ZXJzXCJcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9XG4gICAgICAgIH1gKTtcbiAgICB9KVxuICAgIC5wb3N0KFwiaHR0cDovL3Rlc3QucmVwb3NpdG9yeS5odXlnZW5zLmtuYXcubmwvdjIuMS9idWxrLXVwbG9hZFwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImJ1bGstdXBsb2FkXCIpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5oZWFkZXIoXCJMb2NhdGlvblwiLCBcIjw8VGhlIGdldCByYXcgZGF0YSB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIik7XG4gICAgfSlcbiAgICAucG9zdChcIjw8VGhlIHNhdmUgbWFwcGluZyB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJzYXZlIG1hcHBpbmdcIiwgcmVxLmJvZHkoKSk7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwNCk7XG4gICAgfSlcbiAgICAucG9zdChcIjw8VGhlIGV4ZWN1dGUgbWFwcGluZyB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJleGVjdXRlIG1hcHBpbmcgd2l0aCBmYWlsdXJlc1wiLCByZXEuYm9keSgpKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShKU09OLnN0cmluZ2lmeSh7XG4gICAgICAgICAgc3VjY2VzczogZmFsc2VcbiAgICAgICAgfSkpO1xuICAgIH0pXG4gICAgLmdldChcIjw8VGhlIGdldCByYXcgZGF0YSB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgcmF3IGRhdGFcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgIHZyZTogXCJ0aGV2cmVuYW1lXCIsXG4gICAgICAgICAgc2F2ZU1hcHBpbmc6IFwiPDxUaGUgc2F2ZSBtYXBwaW5nIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiLFxuICAgICAgICAgIGV4ZWN1dGVNYXBwaW5nOiBcIjw8VGhlIGV4ZWN1dGUgbWFwcGluZyB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIixcbiAgICAgICAgICBjb2xsZWN0aW9uczogW1xuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBuYW1lOiBcIm1vY2twZXJzb25zXCIsXG4gICAgICAgICAgICAgIHZhcmlhYmxlczogW1wiSURcIiwgXCJWb29ybmFhbVwiLCBcInR1c3NlbnZvZWdzZWxcIiwgXCJBY2h0ZXJuYWFtXCIsIFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsIFwiR2Vub2VtZCBpblwiLCBcIklzIGdldHJvdXdkIG1ldFwiXSxcbiAgICAgICAgICAgICAgZGF0YTogXCI8PHVybCBmb3IgcGVyc29uIGRhdGE+PlwiLFxuICAgICAgICAgICAgICBkYXRhV2l0aEVycm9yczogXCI8PHVybCBmb3IgcGVyc29uIGRhdGEgd2l0aCBlcnJvcnM+PlwiXG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBuYW1lOiBcIm1vY2tkb2N1bWVudHNcIixcbiAgICAgICAgICAgICAgdmFyaWFibGVzOiBbXCJ0aXRlbFwiLCBcImRhdHVtXCIsIFwicmVmZXJlbnRpZVwiLCBcInVybFwiXSxcbiAgICAgICAgICAgICAgZGF0YTogXCI8PHVybCBmb3IgZG9jdW1lbnQgZGF0YT4+XCIsXG4gICAgICAgICAgICAgIGRhdGFXaXRoRXJyb3JzOiBcIjw8dXJsIGZvciBkb2N1bWVudCBkYXRhIHdpdGggZXJyb3JzPj5cIlxuICAgICAgICAgICAgfVxuICAgICAgICAgIF1cbiAgICAgICAgfSkpO1xuICAgIH0pXG4gICAgLmdldChcIjw8dXJsIGZvciBwZXJzb24gZGF0YT4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZ2V0IHBlcnNvbiBpdGVtcyBkYXRhXCIpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5ib2R5KEpTT04uc3RyaW5naWZ5KHtcbiAgICAgICAgICBcIl9uZXh0XCI6IFwiPDxtb3JlIGRhdGE+PlwiLFxuICAgICAgICAgICAgXCJpdGVtc1wiOiBbXG4gICAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgICB2YWx1ZXM6IHtcbiAgICAgICAgICAgICAgICAgIFwiSURcIjogXCIxXCIsXG4gICAgICAgICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwiVm9vcm5hYW1cIixcbiAgICAgICAgICAgICAgICAgIFwidHVzc2Vudm9lZ3NlbFwiOiBcInR1c3NlbnZvZWdzZWxcIixcbiAgICAgICAgICAgICAgICAgIFwiQWNodGVybmFhbVwiOiBcIkFjaHRlcm5hYW1cIixcbiAgICAgICAgICAgICAgICAgIFwiR2VzY2hyZXZlbkRvY3VtZW50XCI6IFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsXG4gICAgICAgICAgICAgICAgICBcIkdlbm9lbWQgaW5cIjogXCJHZW5vZW1kIGluXCIsXG4gICAgICAgICAgICAgICAgICBcIklzIGdldHJvdXdkIG1ldFwiOiBcIklzIGdldHJvdXdkIG1ldFwiLFxuICAgICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgICAgZXJyb3JzOiB7fVxuICAgICAgICAgICAgICB9LFxuICAgICAgICAgICAgICB7XG4gICAgICAgICAgICAgICAgdmFsdWVzOiB7XG4gICAgICAgICAgICAgICAgICBcIklEXCI6IFwiMlwiLFxuICAgICAgICAgICAgICAgICAgXCJWb29ybmFhbVwiOiBcIlZvb3JuYWFtXCIsXG4gICAgICAgICAgICAgICAgICBcInR1c3NlbnZvZWdzZWxcIjogXCJ0dXNzZW52b2Vnc2VsXCIsXG4gICAgICAgICAgICAgICAgICBcIkFjaHRlcm5hYW1cIjogXCJBY2h0ZXJuYWFtXCIsXG4gICAgICAgICAgICAgICAgICBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiOiBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiLFxuICAgICAgICAgICAgICAgICAgXCJHZW5vZW1kIGluXCI6IFwiR2Vub2VtZCBpblwiLFxuICAgICAgICAgICAgICAgICAgXCJJcyBnZXRyb3V3ZCBtZXRcIjogXCJJcyBnZXRyb3V3ZCBtZXRcIixcbiAgICAgICAgICAgICAgICB9LFxuICAgICAgICAgICAgICAgIGVycm9yczoge31cbiAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgXVxuICAgICAgICB9KSk7XG4gICAgfSlcbiAgICAuZ2V0KFwiPDx1cmwgZm9yIHBlcnNvbiBkYXRhIHdpdGggZXJyb3JzPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgcGVyc29uIGl0ZW1zIGRhdGEgd2l0aCBlcnJvcnNcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgIFwiX25leHRcIjogXCI8PG1vcmUgZGF0YT4+XCIsXG4gICAgICAgICAgXCJpdGVtc1wiOiBbe1xuICAgICAgICAgICAgdmFsdWVzOiB7XG4gICAgICAgICAgICAgIFwiSURcIjogXCIxXCIsXG4gICAgICAgICAgICAgIFwiVm9vcm5hYW1cIjogXCJWb29ybmFhbVwiLFxuICAgICAgICAgICAgICBcInR1c3NlbnZvZWdzZWxcIjogXCJ0dXNzZW52b2Vnc2VsXCIsXG4gICAgICAgICAgICAgIFwiQWNodGVybmFhbVwiOiBcIkFjaHRlcm5hYW1cIixcbiAgICAgICAgICAgICAgXCJHZXNjaHJldmVuRG9jdW1lbnRcIjogXCJHZXNjaHJldmVuRG9jdW1lbnRcIixcbiAgICAgICAgICAgICAgXCJHZW5vZW1kIGluXCI6IFwiR2Vub2VtZCBpblwiLFxuICAgICAgICAgICAgICBcIklzIGdldHJvdXdkIG1ldFwiOiBcIklzIGdldHJvdXdkIG1ldFwiLFxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIGVycm9yczoge1xuICAgICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwid2lsbCBub3QgZG9cIixcbiAgICAgICAgICAgICAgXCJBY2h0ZXJuYWFtXCI6IFwiYWxzbyBmYWlsZWRcIlxuICAgICAgICAgICAgfVxuICAgICAgICAgIH1dXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5nZXQoXCI8PG1vcmUgZGF0YT4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZ2V0IHBlcnNvbiBpdGVtcyBkYXRhXCIpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5ib2R5KEpTT04uc3RyaW5naWZ5KHtcbiAgICAgICAgICBcIml0ZW1zXCI6IFtcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgdmFsdWVzOiB7XG4gICAgICAgICAgICAgICAgXCJJRFwiOiBcIjNcIixcbiAgICAgICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwiVm9vcm5hYW1cIixcbiAgICAgICAgICAgICAgICBcInR1c3NlbnZvZWdzZWxcIjogXCJ0dXNzZW52b2Vnc2VsXCIsXG4gICAgICAgICAgICAgICAgXCJBY2h0ZXJuYWFtXCI6IFwiQWNodGVybmFhbVwiLFxuICAgICAgICAgICAgICAgIFwiR2VzY2hyZXZlbkRvY3VtZW50XCI6IFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsXG4gICAgICAgICAgICAgICAgXCJHZW5vZW1kIGluXCI6IFwiR2Vub2VtZCBpblwiLFxuICAgICAgICAgICAgICAgIFwiSXMgZ2V0cm91d2QgbWV0XCI6IFwiSXMgZ2V0cm91d2QgbWV0XCIsXG4gICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgIGVycm9yczoge31cbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICB7XG4gICAgICAgICAgICAgIHZhbHVlczoge1xuICAgICAgICAgICAgICAgIFwiSURcIjogXCI0XCIsXG4gICAgICAgICAgICAgICAgXCJWb29ybmFhbVwiOiBcIlZvb3JuYWFtXCIsXG4gICAgICAgICAgICAgICAgXCJ0dXNzZW52b2Vnc2VsXCI6IFwidHVzc2Vudm9lZ3NlbFwiLFxuICAgICAgICAgICAgICAgIFwiQWNodGVybmFhbVwiOiBcIkFjaHRlcm5hYW1cIixcbiAgICAgICAgICAgICAgICBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiOiBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiLFxuICAgICAgICAgICAgICAgIFwiR2Vub2VtZCBpblwiOiBcIkdlbm9lbWQgaW5cIixcbiAgICAgICAgICAgICAgICBcIklzIGdldHJvdXdkIG1ldFwiOiBcIklzIGdldHJvdXdkIG1ldFwiLFxuICAgICAgICAgICAgICB9LFxuICAgICAgICAgICAgICBlcnJvcnM6IHt9XG4gICAgICAgICAgICB9XG4gICAgICAgICAgXVxuICAgICAgICB9KSk7XG4gICAgfSlcbiAgICAuZ2V0KFwiPDx1cmwgZm9yIGRvY3VtZW50IGRhdGE+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImdldCBkb2N1bWVudCBpdGVtcyBkYXRhXCIpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5ib2R5KEpTT04uc3RyaW5naWZ5KHtcbiAgICAgICAgICAgIFwibmFtZVwiOiBcInNvbWVDb2xsZWN0aW9uXCIsXG4gICAgICAgICAgICBcInZhcmlhYmxlc1wiOiBbXCJ0aW1faWRcIiwgXCJ2YXIxXCIsIFwidmFyMlwiXSxcbiAgICAgICAgICAgIFwiaXRlbXNcIjogW1xuICAgICAgICAgICAgICB7XG4gICAgICAgICAgICAgICAgdmFsdWVzOiB7XG4gICAgICAgICAgICAgICAgICBcInRpbV9pZFwiOiBcIjFcIixcbiAgICAgICAgICAgICAgICAgIFwidGl0ZWxcIjogXCJ0aXRlbFwiLFxuICAgICAgICAgICAgICAgICAgXCJkYXR1bVwiOiBcImRhdHVtXCIsXG4gICAgICAgICAgICAgICAgICBcInJlZmVyZW50aWVcIjogXCJyZWZlcmVudGllXCIsXG4gICAgICAgICAgICAgICAgICBcInVybFwiOiBcInVybFwiLFxuICAgICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgICAgZXJyb3JzOiB7fVxuICAgICAgICAgICAgICB9LFxuICAgICAgICAgICAgICB7XG4gICAgICAgICAgICAgICAgdmFsdWVzOiB7XG4gICAgICAgICAgICAgICAgICBcInRpbV9pZFwiOiBcIjJcIixcbiAgICAgICAgICAgICAgICAgIFwidGl0ZWxcIjogXCJ0aXRlbFwiLFxuICAgICAgICAgICAgICAgICAgXCJkYXR1bVwiOiBcImRhdHVtXCIsXG4gICAgICAgICAgICAgICAgICBcInJlZmVyZW50aWVcIjogXCJyZWZlcmVudGllXCIsXG4gICAgICAgICAgICAgICAgICBcInVybFwiOiBcInVybFwiLFxuICAgICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgICAgZXJyb3JzOiB7fVxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICBdXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5nZXQoXCI8PHVybCBmb3IgZG9jdW1lbnQgZGF0YSB3aXRoIGVycm9ycz4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZ2V0IGRvY3VtZW50IGl0ZW1zIGRhdGEgd2l0aCBlcnJvcnNcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgIFwibmFtZVwiOiBcInNvbWVDb2xsZWN0aW9uXCIsXG4gICAgICAgICAgXCJ2YXJpYWJsZXNcIjogW1widGltX2lkXCIsIFwidmFyMVwiLCBcInZhcjJcIl0sXG4gICAgICAgICAgXCJpdGVtc1wiOiBbXVxuICAgICAgICB9KSk7XG4gICAgfSlcbiAgICAubW9jayhmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmVycm9yKFwidW5tb2NrZWQgcmVxdWVzdFwiLCByZXEudXJsKCksIHJlcSwgcmVzcCk7XG4gICAgfSk7XG59XG4iLCJpbXBvcnQge2NyZWF0ZVN0b3JlLCBhcHBseU1pZGRsZXdhcmUsIGNvbWJpbmVSZWR1Y2VycywgY29tcG9zZX0gZnJvbSBcInJlZHV4XCI7XG5pbXBvcnQgdGh1bmtNaWRkbGV3YXJlIGZyb20gXCJyZWR1eC10aHVua1wiO1xuXG5pbXBvcnQgeyBwZXJzaXN0IH0gZnJvbSBcIi4vdXRpbC9wZXJzaXN0XCI7XG5pbXBvcnQgcmVkdWNlcnMgZnJvbSBcIi4vcmVkdWNlcnNcIjtcblxubGV0IHN0b3JlID0gY3JlYXRlU3RvcmUoXG4gIGNvbWJpbmVSZWR1Y2VycyhyZWR1Y2VycyksXG4gIGNvbXBvc2UoXG4gICAgYXBwbHlNaWRkbGV3YXJlKFxuICAgICAgdGh1bmtNaWRkbGV3YXJlXG4gICAgKSxcbiAgICB3aW5kb3cuZGV2VG9vbHNFeHRlbnNpb24gPyB3aW5kb3cuZGV2VG9vbHNFeHRlbnNpb24oKSA6IGYgPT4gZlxuICApXG4pO1xuXG4vLyB3aW5kb3cub25iZWZvcmV1bmxvYWQgPSAoKSA9PiBwZXJzaXN0KHN0b3JlLmdldFN0YXRlKCkpO1xuXG5leHBvcnQgZGVmYXVsdCBzdG9yZTtcbiIsImZ1bmN0aW9uIGRlZXBDbG9uZTkob2JqKSB7XG4gICAgdmFyIGksIGxlbiwgcmV0O1xuXG4gICAgaWYgKHR5cGVvZiBvYmogIT09IFwib2JqZWN0XCIgfHwgb2JqID09PSBudWxsKSB7XG4gICAgICAgIHJldHVybiBvYmo7XG4gICAgfVxuXG4gICAgaWYgKEFycmF5LmlzQXJyYXkob2JqKSkge1xuICAgICAgICByZXQgPSBbXTtcbiAgICAgICAgbGVuID0gb2JqLmxlbmd0aDtcbiAgICAgICAgZm9yIChpID0gMDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgICAgICByZXQucHVzaCggKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXSApO1xuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcmV0ID0ge307XG4gICAgICAgIGZvciAoaSBpbiBvYmopIHtcbiAgICAgICAgICAgIGlmIChvYmouaGFzT3duUHJvcGVydHkoaSkpIHtcbiAgICAgICAgICAgICAgICByZXRbaV0gPSAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuICAgIHJldHVybiByZXQ7XG59XG5cbmV4cG9ydCBkZWZhdWx0IGRlZXBDbG9uZTk7IiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuY29uc3QgX2dldEluID0gKHBhdGgsIGRhdGEpID0+XG5cdGRhdGEgP1xuXHRcdHBhdGgubGVuZ3RoID09PSAwID8gZGF0YSA6IF9nZXRJbihwYXRoLCBkYXRhW3BhdGguc2hpZnQoKV0pIDpcblx0XHRudWxsO1xuXG5cblxuY29uc3QgZ2V0SW4gPSAocGF0aCwgZGF0YSkgPT5cblx0X2dldEluKGNsb25lKHBhdGgpLCBkYXRhKTtcblxuXG5leHBvcnQgZGVmYXVsdCBnZXRJbjsiLCJleHBvcnQgZGVmYXVsdCBmdW5jdGlvbiBtYXBwaW5nVG9Kc29uTGRSbWwobWFwcGluZywgdnJlKSB7XG4gIHJldHVybiB7XG4gIFx0XCJAY29udGV4dFwiOiB7XG4gIFx0XHRcIkB2b2NhYlwiOiBcImh0dHA6Ly93d3cudzMub3JnL25zL3Iycm1sI1wiLFxuICBcdFx0XCJybWxcIjogXCJodHRwOi8vc2Vtd2ViLm1tbGFiLmJlL25zL3JtbCNcIixcbiAgXHRcdFwidGltXCI6IFwiaHR0cDovL3RpbWJ1Y3Rvby5jb20vbWFwcGluZyNcIixcbiAgICAgIFwiaHR0cDovL3d3dy53My5vcmcvMjAwMC8wMS9yZGYtc2NoZW1hI3N1YkNsYXNzT2ZcIjoge1xuICAgICAgICBcIkB0eXBlXCI6IFwiQGlkXCJcbiAgICAgIH0sXG4gIFx0XHRcInByZWRpY2F0ZVwiOiB7XG4gIFx0XHRcdFwiQHR5cGVcIjogXCJAaWRcIlxuICBcdFx0fSxcbiAgICAgIFwidGVybVR5cGVcIjoge1xuICAgICAgICBcIkB0eXBlXCI6IFwiQGlkXCJcbiAgICAgIH0sXG4gICAgICBcInBhcmVudFRyaXBsZXNNYXBcIjoge1xuICAgICAgICBcIkB0eXBlXCI6IFwiQGlkXCJcbiAgICAgIH0sXG4gICAgICBcImNsYXNzXCI6IHtcbiAgICAgICAgXCJAdHlwZVwiOiBcIkBpZFwiXG4gICAgICB9XG4gIFx0fSxcbiAgXHRcIkBncmFwaFwiOiBPYmplY3Qua2V5cyhtYXBwaW5nLmNvbGxlY3Rpb25zKS5tYXAoa2V5ID0+IG1hcFNoZWV0KGtleSwgbWFwcGluZy5jb2xsZWN0aW9uc1trZXldLCB2cmUpKVxuICB9O1xufVxuXG5mdW5jdGlvbiBtYWtlTWFwTmFtZSh2cmUsIGxvY2FsTmFtZSkge1xuICByZXR1cm4gYGh0dHA6Ly90aW1idWN0b28uY29tL21hcHBpbmcvJHt2cmV9LyR7bG9jYWxOYW1lfWA7XG59XG5cbmZ1bmN0aW9uIG1hcFNoZWV0KGtleSwgc2hlZXQsIHZyZSkge1xuICAvL0ZJWE1FOiBtb3ZlIGxvZ2ljYWxTb3VyY2UgYW5kIHN1YmplY3RNYXAgdW5kZXIgdGhlIGNvbnRyb2wgb2YgdGhlIHNlcnZlclxuICByZXR1cm4ge1xuICAgIFwiQGlkXCI6IG1ha2VNYXBOYW1lKHZyZSwga2V5KSxcbiAgICBcImh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDEvcmRmLXNjaGVtYSNzdWJDbGFzc09mXCI6IGBodHRwOi8vdGltYnVjdG9vLmNvbS8ke3NoZWV0LmFyY2hldHlwZU5hbWUucmVwbGFjZSgvcyQvLCBcIlwiKX1gLFxuICAgIFwicm1sOmxvZ2ljYWxTb3VyY2VcIjoge1xuXHRcdFx0XCJybWw6c291cmNlXCI6IHtcblx0XHRcdFx0XCJ0aW06cmF3Q29sbGVjdGlvblwiOiBrZXksXG5cdFx0XHRcdFwidGltOnZyZU5hbWVcIjogdnJlXG5cdFx0XHR9XG5cdFx0fSxcbiAgICBcInN1YmplY3RNYXBcIjoge1xuXHRcdFx0XCJjbGFzc1wiOiBtYWtlTWFwTmFtZSh2cmUsIGtleSksXG5cdFx0XHRcInRlbXBsYXRlXCI6IGAke21ha2VNYXBOYW1lKHZyZSwga2V5KX0ve3RpbV9pZH1gXG5cdFx0fSxcbiAgICBcInByZWRpY2F0ZU9iamVjdE1hcFwiOiBzaGVldC5tYXBwaW5ncy5tYXAobWFrZVByZWRpY2F0ZU9iamVjdE1hcC5iaW5kKG51bGwsIHZyZSkpXG4gIH07XG59XG5cbmZ1bmN0aW9uIG1ha2VQcmVkaWNhdGVPYmplY3RNYXAodnJlLCBtYXBwaW5nKSB7XG4gIGxldCBwcm9wZXJ0eSA9IG1hcHBpbmcucHJvcGVydHk7XG4gIGxldCB2YXJpYWJsZSA9IG1hcHBpbmcudmFyaWFibGVbMF07XG4gIGlmICh2YXJpYWJsZS50YXJnZXRDb2xsZWN0aW9uKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIFwib2JqZWN0TWFwXCI6IHtcbiAgICAgICAgXCJqb2luQ29uZGl0aW9uXCI6IHtcbiAgICAgICAgICBcImNoaWxkXCI6IHZhcmlhYmxlLnZhcmlhYmxlTmFtZSxcbiAgICAgICAgICBcInBhcmVudFwiOiB2YXJpYWJsZS50YXJnZXRWYXJpYWJsZU5hbWVcbiAgICAgICAgfSxcbiAgICAgICAgXCJwYXJlbnRUcmlwbGVzTWFwXCI6IG1ha2VNYXBOYW1lKHZyZSwgdmFyaWFibGUudGFyZ2V0Q29sbGVjdGlvbilcbiAgICAgIH0sXG4gICAgICBcInByZWRpY2F0ZVwiOiBgaHR0cDovL3RpbWJ1Y3Rvby5jb20vJHtwcm9wZXJ0eX1gXG4gICAgfVxuICB9IGVsc2UgaWYgKHZhcmlhYmxlLnRhcmdldEV4aXN0aW5nVGltYnVjdG9vVnJlKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIFwib2JqZWN0TWFwXCI6IHtcbiAgICAgICAgXCJjb2x1bW5cIjogdmFyaWFibGUudmFyaWFibGVOYW1lLFxuICAgICAgICBcInRlcm1UeXBlXCI6IFwiaHR0cDovL3d3dy53My5vcmcvbnMvcjJybWwjSVJJXCJcbiAgICAgIH0sXG4gICAgICBcInByZWRpY2F0ZVwiOiBgaHR0cDovL3RpbWJ1Y3Rvby5jb20vJHtwcm9wZXJ0eX1gLFxuICAgICAgXCJodHRwOi8vdGltYnVjdG9vLmNvbS9tYXBwaW5nL2V4aXN0aW5nVGltYnVjdG9vVnJlXCI6IHZhcmlhYmxlLnRhcmdldEV4aXN0aW5nVGltYnVjdG9vVnJlXG4gICAgfVxuICB9IGVsc2Uge1xuICAgIHJldHVybiB7XG4gICAgICBcIm9iamVjdE1hcFwiOiB7XG4gICAgICAgIFwiY29sdW1uXCI6IHZhcmlhYmxlLnZhcmlhYmxlTmFtZVxuICAgICAgfSxcbiAgICAgIFwicHJlZGljYXRlXCI6IGBodHRwOi8vdGltYnVjdG9vLmNvbS8ke3Byb3BlcnR5fWBcbiAgICB9XG4gIH1cbn1cbiIsImxldCBwZXJzaXN0RGlzYWJsZWQgPSBmYWxzZTtcblxuY29uc3QgcGVyc2lzdCA9IChzdGF0ZSkgPT4ge1xuXHRpZiAoIHBlcnNpc3REaXNhYmxlZCApIHsgcmV0dXJuOyB9XG5cdGZvciAobGV0IGtleSBpbiBzdGF0ZSkge1xuXHRcdGxvY2FsU3RvcmFnZS5zZXRJdGVtKGtleSwgSlNPTi5zdHJpbmdpZnkoc3RhdGVba2V5XSkpO1xuXHR9XG59O1xuXG5jb25zdCBnZXRJdGVtID0gKGtleSkgPT4ge1xuXHRpZiAobG9jYWxTdG9yYWdlLmdldEl0ZW0oa2V5KSkge1xuXHRcdHJldHVybiBKU09OLnBhcnNlKGxvY2FsU3RvcmFnZS5nZXRJdGVtKGtleSkpO1xuXHR9XG5cdHJldHVybiBudWxsO1xufTtcblxuY29uc3QgZGlzYWJsZVBlcnNpc3QgPSAoKSA9PiB7XG5cdGxvY2FsU3RvcmFnZS5jbGVhcigpO1xuXHRwZXJzaXN0RGlzYWJsZWQgPSB0cnVlO1xufTtcbndpbmRvdy5kaXNhYmxlUGVyc2lzdCA9IGRpc2FibGVQZXJzaXN0O1xuXG5leHBvcnQgeyBwZXJzaXN0LCBnZXRJdGVtLCBkaXNhYmxlUGVyc2lzdCB9O1xuIiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuLy8gRG8gZWl0aGVyIG9mIHRoZXNlOlxuLy8gIGEpIFNldCBhIHZhbHVlIGJ5IHJlZmVyZW5jZSBpZiBkZXJlZiBpcyBub3QgbnVsbFxuLy8gIGIpIFNldCBhIHZhbHVlIGRpcmVjdGx5IGluIHRvIGRhdGEgb2JqZWN0IGlmIGRlcmVmIGlzIG51bGxcbmNvbnN0IHNldEVpdGhlciA9IChkYXRhLCBkZXJlZiwga2V5LCB2YWwpID0+IHtcblx0KGRlcmVmIHx8IGRhdGEpW2tleV0gPSB2YWw7XG5cdHJldHVybiBkYXRhO1xufTtcblxuLy8gU2V0IGEgbmVzdGVkIHZhbHVlIGluIGRhdGEgKG5vdCB1bmxpa2UgaW1tdXRhYmxlanMsIGJ1dCBhIGNsb25lIG9mIGRhdGEgaXMgZXhwZWN0ZWQgZm9yIHByb3BlciBpbW11dGFiaWxpdHkpXG5jb25zdCBfc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID0gbnVsbCkgPT5cblx0cGF0aC5sZW5ndGggPiAxID9cblx0XHRfc2V0SW4ocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID8gZGVyZWZbcGF0aC5zaGlmdCgpXSA6IGRhdGFbcGF0aC5zaGlmdCgpXSkgOlxuXHRcdHNldEVpdGhlcihkYXRhLCBkZXJlZiwgcGF0aFswXSwgdmFsdWUpO1xuXG5jb25zdCBzZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSkgPT5cblx0X3NldEluKGNsb25lKHBhdGgpLCB2YWx1ZSwgY2xvbmUoZGF0YSkpO1xuXG5leHBvcnQgZGVmYXVsdCBzZXRJbjsiXX0=
