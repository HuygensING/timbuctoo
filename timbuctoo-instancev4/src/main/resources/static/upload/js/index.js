(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.ExcelImportMock = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
// shim for using process in browser
var process = module.exports = {};

// cached from whatever global is present so that test runners that stub it
// don't break things.  But we need to wrap it in a try catch in case it is
// wrapped in strict mode code which doesn't define any globals.  It's inside a
// function because try/catches deoptimize in certain engines.

var cachedSetTimeout;
var cachedClearTimeout;

(function () {
    try {
        cachedSetTimeout = setTimeout;
    } catch (e) {
        cachedSetTimeout = function () {
            throw new Error('setTimeout is not defined');
        }
    }
    try {
        cachedClearTimeout = clearTimeout;
    } catch (e) {
        cachedClearTimeout = function () {
            throw new Error('clearTimeout is not defined');
        }
    }
} ())
function runTimeout(fun) {
    if (cachedSetTimeout === setTimeout) {
        //normal enviroments in sane situations
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

},{"./PathUtils":13,"./deprecate":20,"./runTransitionHook":21,"_process":1,"query-string":34,"warning":24}],24:[function(require,module,exports){
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

},{"./_overArg":30}],29:[function(require,module,exports){
/**
 * Checks if `value` is a host object in IE < 9.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a host object, else `false`.
 */
function isHostObject(value) {
  // Many host objects are `Object` objects that can coerce to strings
  // despite having improperly defined `toString` methods.
  var result = false;
  if (value != null && typeof value.toString != 'function') {
    try {
      result = !!(value + '');
    } catch (e) {}
  }
  return result;
}

module.exports = isHostObject;

},{}],30:[function(require,module,exports){
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

},{}],31:[function(require,module,exports){
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
  return !!value && typeof value == 'object';
}

module.exports = isObjectLike;

},{}],32:[function(require,module,exports){
var getPrototype = require('./_getPrototype'),
    isHostObject = require('./_isHostObject'),
    isObjectLike = require('./isObjectLike');

/** `Object#toString` result references. */
var objectTag = '[object Object]';

/** Used for built-in method references. */
var objectProto = Object.prototype;

/** Used to resolve the decompiled source of functions. */
var funcToString = Function.prototype.toString;

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
  if (!isObjectLike(value) ||
      objectToString.call(value) != objectTag || isHostObject(value)) {
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

},{"./_getPrototype":28,"./_isHostObject":29,"./isObjectLike":31}],33:[function(require,module,exports){
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
},{"for-each":6,"trim":91}],34:[function(require,module,exports){
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

},{"strict-uri-encode":88}],35:[function(require,module,exports){
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

},{"../utils/storeShape":39,"../utils/warning":40,"_process":1,"react":"react"}],36:[function(require,module,exports){
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

},{"../utils/shallowEqual":38,"../utils/storeShape":39,"../utils/warning":40,"../utils/wrapActionCreators":41,"_process":1,"hoist-non-react-statics":25,"invariant":26,"lodash/isPlainObject":32,"react":"react"}],37:[function(require,module,exports){
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
},{"./components/Provider":35,"./components/connect":36}],38:[function(require,module,exports){
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
},{}],39:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _react = require('react');

exports["default"] = _react.PropTypes.shape({
  subscribe: _react.PropTypes.func.isRequired,
  dispatch: _react.PropTypes.func.isRequired,
  getState: _react.PropTypes.func.isRequired
});
},{"react":"react"}],40:[function(require,module,exports){
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
},{}],41:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports["default"] = wrapActionCreators;

var _redux = require('redux');

function wrapActionCreators(actionCreators) {
  return function (dispatch) {
    return (0, _redux.bindActionCreators)(actionCreators, dispatch);
  };
}
},{"redux":86}],42:[function(require,module,exports){
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
},{}],43:[function(require,module,exports){
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

},{"./InternalPropTypes":47,"./routerWarning":76,"_process":1}],44:[function(require,module,exports){
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
},{"./Link":49,"react":"react"}],45:[function(require,module,exports){
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

},{"./InternalPropTypes":47,"./Redirect":52,"./routerWarning":76,"_process":1,"invariant":26,"react":"react"}],46:[function(require,module,exports){
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

},{"./InternalPropTypes":47,"./RouteUtils":55,"./routerWarning":76,"_process":1,"invariant":26,"react":"react"}],47:[function(require,module,exports){
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
},{"react":"react"}],48:[function(require,module,exports){
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

},{"./routerWarning":76,"_process":1,"invariant":26,"react":"react"}],49:[function(require,module,exports){
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
    to: oneOfType([string, object]).isRequired,
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

},{"./PropTypes":51,"./routerWarning":76,"_process":1,"invariant":26,"react":"react"}],50:[function(require,module,exports){
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

},{"_process":1,"invariant":26}],51:[function(require,module,exports){
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

},{"./InternalPropTypes":47,"./deprecateObjectProperties":67,"./routerWarning":76,"_process":1,"react":"react"}],52:[function(require,module,exports){
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

},{"./InternalPropTypes":47,"./PatternUtils":50,"./RouteUtils":55,"_process":1,"invariant":26,"react":"react"}],53:[function(require,module,exports){
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

},{"./InternalPropTypes":47,"./RouteUtils":55,"_process":1,"invariant":26,"react":"react"}],54:[function(require,module,exports){
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

},{"./routerWarning":76,"_process":1,"react":"react"}],55:[function(require,module,exports){
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
},{"react":"react"}],56:[function(require,module,exports){
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

},{"./InternalPropTypes":47,"./RouteUtils":55,"./RouterContext":57,"./RouterUtils":58,"./createTransitionManager":66,"./routerWarning":76,"_process":1,"history/lib/createHashHistory":16,"history/lib/useQueries":23,"invariant":26,"react":"react"}],57:[function(require,module,exports){
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

},{"./RouteUtils":55,"./deprecateObjectProperties":67,"./getRouteParams":69,"./routerWarning":76,"_process":1,"invariant":26,"react":"react"}],58:[function(require,module,exports){
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

},{"./deprecateObjectProperties":67,"_process":1}],59:[function(require,module,exports){
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

},{"./RouterContext":57,"./routerWarning":76,"_process":1,"react":"react"}],60:[function(require,module,exports){
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

},{"./AsyncUtils":42,"./routerWarning":76,"_process":1}],61:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _RouterContext = require('./RouterContext');

var _RouterContext2 = _interopRequireDefault(_RouterContext);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = function () {
  for (var _len = arguments.length, middlewares = Array(_len), _key = 0; _key < _len; _key++) {
    middlewares[_key] = arguments[_key];
  }

  var withContext = middlewares.map(function (m) {
    return m.renderRouterContext;
  }).filter(function (f) {
    return f;
  });
  var withComponent = middlewares.map(function (m) {
    return m.renderRouteComponent;
  }).filter(function (f) {
    return f;
  });
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
},{"./RouterContext":57,"react":"react"}],62:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _createBrowserHistory = require('history/lib/createBrowserHistory');

var _createBrowserHistory2 = _interopRequireDefault(_createBrowserHistory);

var _createRouterHistory = require('./createRouterHistory');

var _createRouterHistory2 = _interopRequireDefault(_createRouterHistory);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (0, _createRouterHistory2.default)(_createBrowserHistory2.default);
module.exports = exports['default'];
},{"./createRouterHistory":65,"history/lib/createBrowserHistory":14}],63:[function(require,module,exports){
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
},{"./PatternUtils":50}],64:[function(require,module,exports){
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
},{"history/lib/createMemoryHistory":19,"history/lib/useBasename":22,"history/lib/useQueries":23}],65:[function(require,module,exports){
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
},{"./useRouterHistory":77}],66:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = createTransitionManager;

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

var _Actions = require('history/lib/Actions');

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

  function createLocationFromRedirectInfo(location) {
    return history.createLocation(location, _Actions.REPLACE);
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
      if (error) callback(error);else callback(null, createLocationFromRedirectInfo(redirectInfo));
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
            history.transitionTo(redirectLocation);
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

},{"./TransitionUtils":60,"./computeChangedRoutes":63,"./getComponents":68,"./isActive":72,"./matchRoutes":75,"./routerWarning":76,"_process":1,"history/lib/Actions":8}],67:[function(require,module,exports){
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

},{"./routerWarning":76,"_process":1}],68:[function(require,module,exports){
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
},{"./AsyncUtils":42,"./makeStateWithLocation":73}],69:[function(require,module,exports){
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
},{"./PatternUtils":50}],70:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _createHashHistory = require('history/lib/createHashHistory');

var _createHashHistory2 = _interopRequireDefault(_createHashHistory);

var _createRouterHistory = require('./createRouterHistory');

var _createRouterHistory2 = _interopRequireDefault(_createRouterHistory);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (0, _createRouterHistory2.default)(_createHashHistory2.default);
module.exports = exports['default'];
},{"./createRouterHistory":65,"history/lib/createHashHistory":16}],71:[function(require,module,exports){
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
},{"./History":43,"./IndexLink":44,"./IndexRedirect":45,"./IndexRoute":46,"./Lifecycle":48,"./Link":49,"./PatternUtils":50,"./PropTypes":51,"./Redirect":52,"./Route":53,"./RouteContext":54,"./RouteUtils":55,"./Router":56,"./RouterContext":57,"./RoutingContext":59,"./applyRouterMiddleware":61,"./browserHistory":62,"./createMemoryHistory":64,"./hashHistory":70,"./match":74,"./useRouterHistory":77,"./useRoutes":78,"./withRouter":79}],72:[function(require,module,exports){
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
},{"./PatternUtils":50}],73:[function(require,module,exports){
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

},{"./deprecateObjectProperties":67,"./routerWarning":76,"_process":1}],74:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

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
    callback(error, redirectLocation, nextState && _extends({}, nextState, {
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

},{"./RouteUtils":55,"./RouterUtils":58,"./createMemoryHistory":64,"./createTransitionManager":66,"_process":1,"invariant":26}],75:[function(require,module,exports){
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

},{"./AsyncUtils":42,"./PatternUtils":50,"./RouteUtils":55,"./makeStateWithLocation":73,"./routerWarning":76,"_process":1}],76:[function(require,module,exports){
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
},{"warning":92}],77:[function(require,module,exports){
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
},{"history/lib/useBasename":22,"history/lib/useQueries":23}],78:[function(require,module,exports){
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

},{"./createTransitionManager":66,"./routerWarning":76,"_process":1,"history/lib/useQueries":23}],79:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = withRouter;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _hoistNonReactStatics = require('hoist-non-react-statics');

var _hoistNonReactStatics2 = _interopRequireDefault(_hoistNonReactStatics);

var _PropTypes = require('./PropTypes');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getDisplayName(WrappedComponent) {
  return WrappedComponent.displayName || WrappedComponent.name || 'Component';
}

function withRouter(WrappedComponent) {
  var WithRouter = _react2.default.createClass({
    displayName: 'WithRouter',

    contextTypes: { router: _PropTypes.routerShape },
    render: function render() {
      return _react2.default.createElement(WrappedComponent, _extends({}, this.props, { router: this.context.router }));
    }
  });

  WithRouter.displayName = 'withRouter(' + getDisplayName(WrappedComponent) + ')';
  WithRouter.WrappedComponent = WrappedComponent;

  return (0, _hoistNonReactStatics2.default)(WithRouter, WrappedComponent);
}
module.exports = exports['default'];
},{"./PropTypes":51,"hoist-non-react-statics":25,"react":"react"}],80:[function(require,module,exports){
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
},{}],81:[function(require,module,exports){
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports["default"] = applyMiddleware;

var _compose = require('./compose');

var _compose2 = _interopRequireDefault(_compose);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

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
    return function (reducer, initialState, enhancer) {
      var store = createStore(reducer, initialState, enhancer);
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
      _dispatch = _compose2["default"].apply(undefined, chain)(store.dispatch);

      return _extends({}, store, {
        dispatch: _dispatch
      });
    };
  };
}
},{"./compose":84}],82:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports["default"] = bindActionCreators;
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
},{}],83:[function(require,module,exports){
(function (process){
'use strict';

exports.__esModule = true;
exports["default"] = combineReducers;

var _createStore = require('./createStore');

var _isPlainObject = require('lodash/isPlainObject');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _warning = require('./utils/warning');

var _warning2 = _interopRequireDefault(_warning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function getUndefinedStateErrorMessage(key, action) {
  var actionType = action && action.type;
  var actionName = actionType && '"' + actionType.toString() + '"' || 'an action';

  return 'Given action ' + actionName + ', reducer "' + key + '" returned undefined. ' + 'To ignore an action, you must explicitly return the previous state.';
}

function getUnexpectedStateShapeWarningMessage(inputState, reducers, action) {
  var reducerKeys = Object.keys(reducers);
  var argumentName = action && action.type === _createStore.ActionTypes.INIT ? 'initialState argument passed to createStore' : 'previous state received by the reducer';

  if (reducerKeys.length === 0) {
    return 'Store does not have a valid reducer. Make sure the argument passed ' + 'to combineReducers is an object whose values are reducers.';
  }

  if (!(0, _isPlainObject2["default"])(inputState)) {
    return 'The ' + argumentName + ' has unexpected type of "' + {}.toString.call(inputState).match(/\s([a-z|A-Z]+)/)[1] + '". Expected argument to be an object with the following ' + ('keys: "' + reducerKeys.join('", "') + '"');
  }

  var unexpectedKeys = Object.keys(inputState).filter(function (key) {
    return !reducers.hasOwnProperty(key);
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
    if (typeof reducers[key] === 'function') {
      finalReducers[key] = reducers[key];
    }
  }
  var finalReducerKeys = Object.keys(finalReducers);

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
      var warningMessage = getUnexpectedStateShapeWarningMessage(state, finalReducers, action);
      if (warningMessage) {
        (0, _warning2["default"])(warningMessage);
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

},{"./createStore":85,"./utils/warning":87,"_process":1,"lodash/isPlainObject":32}],84:[function(require,module,exports){
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
  } else {
    var _ret = function () {
      var last = funcs[funcs.length - 1];
      var rest = funcs.slice(0, -1);
      return {
        v: function v() {
          return rest.reduceRight(function (composed, f) {
            return f(composed);
          }, last.apply(undefined, arguments));
        }
      };
    }();

    if (typeof _ret === "object") return _ret.v;
  }
}
},{}],85:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.ActionTypes = undefined;
exports["default"] = createStore;

var _isPlainObject = require('lodash/isPlainObject');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _symbolObservable = require('symbol-observable');

var _symbolObservable2 = _interopRequireDefault(_symbolObservable);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

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
 * @param {any} [initialState] The initial state. You may optionally specify it
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
function createStore(reducer, initialState, enhancer) {
  var _ref2;

  if (typeof initialState === 'function' && typeof enhancer === 'undefined') {
    enhancer = initialState;
    initialState = undefined;
  }

  if (typeof enhancer !== 'undefined') {
    if (typeof enhancer !== 'function') {
      throw new Error('Expected the enhancer to be a function.');
    }

    return enhancer(createStore)(reducer, initialState);
  }

  if (typeof reducer !== 'function') {
    throw new Error('Expected the reducer to be a function.');
  }

  var currentReducer = reducer;
  var currentState = initialState;
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
    if (!(0, _isPlainObject2["default"])(action)) {
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
    }, _ref[_symbolObservable2["default"]] = function () {
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
  }, _ref2[_symbolObservable2["default"]] = observable, _ref2;
}
},{"lodash/isPlainObject":32,"symbol-observable":89}],86:[function(require,module,exports){
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

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

/*
* This is a dummy function to check if the function name has been altered by minification.
* If the function has been minified and NODE_ENV !== 'production', warn the user.
*/
function isCrushed() {}

if (process.env.NODE_ENV !== 'production' && typeof isCrushed.name === 'string' && isCrushed.name !== 'isCrushed') {
  (0, _warning2["default"])('You are currently using minified code outside of NODE_ENV === \'production\'. ' + 'This means that you are running a slower development build of Redux. ' + 'You can use loose-envify (https://github.com/zertosh/loose-envify) for browserify ' + 'or DefinePlugin for webpack (http://stackoverflow.com/questions/30030031) ' + 'to ensure you have the correct code for your production build.');
}

exports.createStore = _createStore2["default"];
exports.combineReducers = _combineReducers2["default"];
exports.bindActionCreators = _bindActionCreators2["default"];
exports.applyMiddleware = _applyMiddleware2["default"];
exports.compose = _compose2["default"];
}).call(this,require('_process'))

},{"./applyMiddleware":81,"./bindActionCreators":82,"./combineReducers":83,"./compose":84,"./createStore":85,"./utils/warning":87,"_process":1}],87:[function(require,module,exports){
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
    // This error was thrown as a convenience so that if you enable
    // "break on all exceptions" in your console,
    // it would pause the execution at this line.
    throw new Error(message);
    /* eslint-disable no-empty */
  } catch (e) {}
  /* eslint-enable no-empty */
}
},{}],88:[function(require,module,exports){
'use strict';
module.exports = function (str) {
	return encodeURIComponent(str).replace(/[!'()*]/g, function (c) {
		return '%' + c.charCodeAt(0).toString(16).toUpperCase();
	});
};

},{}],89:[function(require,module,exports){
(function (global){
/* global window */
'use strict';

module.exports = require('./ponyfill')(global || window || this);

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"./ponyfill":90}],90:[function(require,module,exports){
'use strict';

module.exports = function symbolObservablePonyfill(root) {
	var result;
	var Symbol = root.Symbol;

	if (typeof Symbol === 'function') {
		if (Symbol.observable) {
			result = Symbol.observable;
		} else {
			result = Symbol('observable');
			Symbol.observable = result;
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

},{"global/window":7,"is-function":27,"parse-headers":33,"xtend":98}],98:[function(require,module,exports){
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
						dispatch({ type: "FINISH_UPLOAD", data: JSON.parse(body) });
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
				var payload = {
					body: JSON.stringify((0, _mappingToJsonLdRml2.default)(state.mappings, state.importData.vre)),
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
		}
	};
	return actions;
}

},{"./util/mappingToJsonLdRml":134,"xhr":97}],100:[function(require,module,exports){
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _page = require('./page.jsx');

var _page2 = _interopRequireDefault(_page);

var _firstUpload = require('./firstUpload.jsx');

var _firstUpload2 = _interopRequireDefault(_firstUpload);

var _connectToArchetype = require('./connect-to-archetype');

var _connectToArchetype2 = _interopRequireDefault(_connectToArchetype);

var _connectData = require('./connect-data');

var _connectData2 = _interopRequireDefault(_connectData);

var _message = require('./message');

var _message2 = _interopRequireDefault(_message);

var _uploadButton = require('./upload-button');

var _uploadButton2 = _interopRequireDefault(_uploadButton);

var _collectionOverview = require('./collection-overview');

var _collectionOverview2 = _interopRequireDefault(_collectionOverview);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

// FIXME: replace each if block with an own component mapped by the router.
var App = function (_React$Component) {
  _inherits(App, _React$Component);

  function App() {
    _classCallCheck(this, App);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(App).apply(this, arguments));
  }

  _createClass(App, [{
    key: 'render',
    value: function render() {
      var hasVres = this.props.userdata.myVres && Object.keys(this.props.userdata.myVres).length > 0;
      var _props = this.props;
      var pathname = _props.location.pathname;
      var _props$importData = _props.importData;
      var sheets = _props$importData.sheets;
      var uploadedFileName = _props$importData.uploadedFileName;
      var onUploadFileSelect = _props.onUploadFileSelect;
      var _onCloseMessage = _props.onCloseMessage;


      var fileIsUploadedMessage = this.props.showFileIsUploadedMessage ? _react2.default.createElement(
        _message2.default,
        { alertLevel: 'info', dismissible: true, onCloseMessage: function onCloseMessage() {
            return _onCloseMessage("showFileIsUploadedMessage");
          } },
        _react2.default.createElement(
          'em',
          null,
          uploadedFileName
        ),
        ' is uploaded.'
      ) : null;

      var publishFailedMessage = this.props.importData.publishErrors ? _react2.default.createElement(
        _message2.default,
        { alertLevel: 'danger', dismissible: false },
        _react2.default.createElement(_uploadButton2.default, { classNames: ["btn", "btn-danger", "pull-right", "btn-xs"], label: 'Re-upload',
          onUploadFileSelect: onUploadFileSelect }),
        _react2.default.createElement('span', { className: 'glyphicon glyphicon-exclamation-sign' }),
        " ",
        'Publish failed. Please fix the mappings or re-upload the data.'
      ) : null;

      var collectionsAreConnectedMessage = this.props.showCollectionsAreConnectedMessage ? _react2.default.createElement(
        _message2.default,
        { alertLevel: 'info', dismissible: true, onCloseMessage: function onCloseMessage() {
            return _onCloseMessage("showCollectionsAreConnectedMessage");
          } },
        sheets.map(function (sheet) {
          return _react2.default.createElement(
            'em',
            { key: sheet.collection },
            sheet.collection
          );
        }).reduce(function (accu, elem) {
          return accu === null ? [elem] : [].concat(_toConsumableArray(accu), [' and ', elem]);
        }, null),
        ' from ',
        _react2.default.createElement(
          'em',
          null,
          uploadedFileName
        ),
        ' are connected to the Timbuctoo Archetypes.'
      ) : null;

      if (pathname === "/" || pathname === "/collections-overview") {
        return _react2.default.createElement(
          _page2.default,
          { username: this.props.userdata.userId, vres: this.props.userdata.vres, showDatasets: true },
          hasVres ? _react2.default.createElement(_collectionOverview2.default, {
            onUploadFileSelect: this.props.onUploadFileSelect,
            isUploading: this.props.importData.isUploading,
            userId: this.props.userdata.userId,
            vres: this.props.userdata.myVres
          }) : _react2.default.createElement(_firstUpload2.default, {
            __MockOnLogin: this.props.onLogin,
            onUploadFileSelect: this.props.onUploadFileSelect,
            isUploading: this.props.importData.isUploading,
            userId: this.props.userdata.userId
          })
        );
      } else if (pathname === "/maparchetypes") {
        return _react2.default.createElement(
          _page2.default,
          { username: this.props.userdata.userId, vres: this.props.userdata.vres, showDatasets: false },
          _react2.default.createElement(
            'div',
            { className: 'container basic-margin' },
            _react2.default.createElement(
              'h2',
              { className: 'small-margin' },
              'Upload and connect your dataset'
            ),
            fileIsUploadedMessage,
            _react2.default.createElement(
              'p',
              null,
              'We found ',
              sheets.length,
              ' collections in the file. Connect the tabs to the Timbuctoo Archetypes.'
            )
          ),
          _react2.default.createElement(_connectToArchetype2.default, {
            mappings: this.props.mappings,
            sheets: this.props.importData.sheets,
            uploadedFileName: this.props.importData.uploadedFileName,
            archetype: this.props.archetype,
            onMapCollectionArchetype: this.props.onMapCollectionArchetype,
            onConfirmCollectionArchetypeMappings: this.props.onConfirmCollectionArchetypeMappings
          })
        );
      } else if (pathname === "/mapdata") {
        return _react2.default.createElement(
          _page2.default,
          { username: this.props.userdata.userId, vres: this.props.userdata.vres, showDatasets: false },
          _react2.default.createElement(
            'div',
            { className: 'container basic-margin' },
            _react2.default.createElement(
              'h2',
              { className: 'small-margin' },
              'Upload and connect your dataset'
            ),
            collectionsAreConnectedMessage,
            publishFailedMessage,
            _react2.default.createElement(
              'p',
              null,
              'Connect the excel columns to the properties of the Archetypes'
            )
          ),
          _react2.default.createElement(_connectData2.default, this.props)
        );
      } else {
        return _react2.default.createElement('div', null);
      }
    }
  }]);

  return App;
}(_react2.default.Component);

exports.default = App;

},{"./collection-overview":103,"./connect-data":105,"./connect-to-archetype":106,"./firstUpload.jsx":110,"./message":112,"./page.jsx":113,"./upload-button":122,"react":"react"}],101:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(CollectionForm).apply(this, arguments));
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
          }
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

},{"./property-form/add-property":114,"./property-form/property-form":116,"react":"react"}],102:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(CollectionIndex).apply(this, arguments));
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

},{"classnames":2,"react":"react"}],103:[function(require,module,exports){
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

},{"./dataset-cards":107,"./upload-button":122,"react":"react"}],104:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(CollectionTable).apply(this, arguments));
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

},{"./table/data-row":120,"./table/header-cell":121,"react":"react"}],105:[function(require,module,exports){
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

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ConnectData = function (_React$Component) {
  _inherits(ConnectData, _React$Component);

  function ConnectData() {
    _classCallCheck(this, ConnectData);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(ConnectData).apply(this, arguments));
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


      var allMappingsAreComplete = collectionTabs.filter(function (tab) {
        return tab.complete;
      }).length === collectionTabs.length;

      return _react2.default.createElement(
        "div",
        null,
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
          onAddCustomProperty: onAddCustomProperty }),
        _react2.default.createElement(
          "div",
          { className: "container big-margin" },
          _react2.default.createElement(
            "button",
            { onClick: onPublishData, className: "btn btn-warning btn-lg pull-right", type: "button", disabled: !allMappingsAreComplete },
            "Publish dataset"
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

},{"./collection-form":101,"./collection-index":102,"./collection-table":104,"react":"react"}],106:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _selectField = require("./fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ConnectToArchetype = function (_React$Component) {
  _inherits(ConnectToArchetype, _React$Component);

  function ConnectToArchetype() {
    _classCallCheck(this, ConnectToArchetype);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(ConnectToArchetype).apply(this, arguments));
  }

  _createClass(ConnectToArchetype, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var onMapCollectionArchetype = _props.onMapCollectionArchetype;
      var onConfirmCollectionArchetypeMappings = _props.onConfirmCollectionArchetypeMappings;
      var sheets = _props.sheets;
      var archetype = _props.archetype;
      var mappings = _props.mappings;
      var uploadedFileName = _props.uploadedFileName;


      var collectionsAreMapped = Object.keys(mappings.collections).length > 0 && Object.keys(mappings.collections).map(function (key) {
        return mappings.collections[key].archetypeName;
      }).indexOf(null) < 0;

      return _react2.default.createElement(
        "div",
        null,
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

},{"./fields/select-field":109,"react":"react"}],107:[function(require,module,exports){
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

},{"./datasetCard.jsx":108,"react":"react"}],108:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function DataSetCard(props) {
  return _react2.default.createElement(
    "div",
    { className: "card-dataset" },
    _react2.default.createElement(
      "a",
      { className: "card-dataset btn btn-default explore",
        href: "" + "/static/query-gui/?vreId=" + props.vreId, target: "_blank" },
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

},{"react":"react"}],109:[function(require,module,exports){
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

    var _this = _possibleConstructorReturn(this, Object.getPrototypeOf(SelectField).call(this, props));

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

},{"classnames":2,"react":"react","react-dom":"react-dom"}],110:[function(require,module,exports){
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

},{"./upload-button":122,"react":"react"}],111:[function(require,module,exports){
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

},{"react":"react"}],112:[function(require,module,exports){
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

},{"classnames":2,"react":"react"}],113:[function(require,module,exports){
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

},{"./dataset-cards":107,"./footer":111,"react":"react"}],114:[function(require,module,exports){
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

    var _this = _possibleConstructorReturn(this, Object.getPrototypeOf(AddProperty).call(this, props));

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
        return availableArchetypes.indexOf(prop.relation.targetCollection) > -1;
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
          newType === "relation" ? _react2.default.createElement(
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
          { className: "col-sm-2" },
          _react2.default.createElement(
            "span",
            { className: "pull-right" },
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
              )
            )
          )
        ),
        _react2.default.createElement("div", { className: "col-sm-6" }),
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

},{"../fields/select-field":109,"react":"react"}],115:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(ColumnSelect).apply(this, arguments));
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

},{"../fields/select-field":109,"react":"react"}],116:[function(require,module,exports){
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
  }
};

var PropertyForm = function (_React$Component) {
  _inherits(PropertyForm, _React$Component);

  function PropertyForm() {
    _classCallCheck(this, PropertyForm);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(PropertyForm).apply(this, arguments));
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

},{"./relation":117,"./text":118,"react":"react"}],117:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(Form).apply(this, arguments));
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

      console.log(targetCollectionColumns);

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

},{"../fields/select-field":109,"./column-select":115,"react":"react"}],118:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(Form).apply(this, arguments));
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

},{"./column-select":115,"react":"react"}],119:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

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

// Moves to react-redux connect
function transformProps(props) {
  var _props$importData = props.importData;
  var sheets = _props$importData.sheets;
  var activeCollection = _props$importData.activeCollection;
  var uploadedFileName = _props$importData.uploadedFileName;
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

  return _extends({}, props, {
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
    }, {})
  });
}

exports.default = transformProps;

},{}],120:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(DataRow).apply(this, arguments));
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

},{"classnames":2,"react":"react"}],121:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(HeaderCell).apply(this, arguments));
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

},{"classnames":2,"react":"react"}],122:[function(require,module,exports){
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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(UploadButton).apply(this, arguments));
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

},{"classnames":2,"react":"react"}],123:[function(require,module,exports){
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

},{"./actions":99,"./router":129,"./servermocks":130,"./store":131,"react":"react","react-dom":"react-dom","xhr":97,"xhr-mock":93}],124:[function(require,module,exports){
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

},{"../util/persist":135}],125:[function(require,module,exports){
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
					v: _extends({}, state, { isUploading: true })
				};
			case "FINISH_UPLOAD":
				return {
					v: _extends({}, state, {
						isUploading: false,
						publishErrors: false,
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
	publishErrors: false
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

},{"../util/persist":135}],126:[function(require,module,exports){
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

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = {
	importData: _importData2.default,
	archetype: _archetype2.default,
	mappings: _mappings2.default,
	userdata: _userdata2.default
};

},{"./archetype":124,"./import-data":125,"./mappings":127,"./userdata":128}],127:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
	var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
	var action = arguments[1];

	switch (action.type) {
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
	confirmed: false
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

},{"../util/get-in":133,"../util/persist":135,"../util/set-in":136}],128:[function(require,module,exports){
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

},{}],129:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.navigateTo = navigateTo;

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _connectData = require("./components/react-redux-connect/connect-data");

var _connectData2 = _interopRequireDefault(_connectData);

var _app = require("./components/app.jsx");

var _app2 = _interopRequireDefault(_app);

var _reactRouter = require("react-router");

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var _reactRedux = require("react-redux");

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

// import {UploadSplashScreen, ArchetypeMappings, DatasheetMappings, CollectionsOverview} from "./components";

function navigateTo(key, args) {
  _reactRouter.hashHistory.push(urls[key].apply(null, args));
}

//The current code isn't written with this in mind. I've only added the connect
//function later. Feel free to use your own mapStateToProps for your own
//component
var makeContainerComponent = (0, _reactRedux.connect)(function (state) {
  return state;
}, function (dispatch) {
  return (0, _actions2.default)(navigateTo, dispatch);
});

var makeMapDataContainer = (0, _reactRedux.connect)(_connectData2.default, function (dispatch) {
  return (0, _actions2.default)(navigateTo, dispatch);
});

var router = _react2.default.createElement(
  _reactRedux.Provider,
  { store: _store2.default },
  _react2.default.createElement(
    _reactRouter.Router,
    { history: _reactRouter.hashHistory },
    _react2.default.createElement(_reactRouter.Route, { path: "/", component: makeContainerComponent(_app2.default) }),
    _react2.default.createElement(_reactRouter.Route, { path: urls.collectionsOverview(), component: makeContainerComponent(_app2.default) }),
    _react2.default.createElement(_reactRouter.Route, { path: urls.mapArchetypes(), component: makeContainerComponent(_app2.default) }),
    _react2.default.createElement(_reactRouter.Route, { path: urls.mapData(), component: makeMapDataContainer(_app2.default) })
  )
);

exports.default = router;

},{"./actions":99,"./components/app.jsx":100,"./components/react-redux-connect/connect-data":119,"./store":131,"react":"react","react-redux":37,"react-router":71}],130:[function(require,module,exports){
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
    return resp.status(200).body("{\n          \"mine\": {\n            \"migrant_steekproef_masterdb (6).xlsx\": {\n              \"name\": \"migrant_steekproef_masterdb (6).xlsx\",\n              \"published\": true\n            },\n            \"testerdetest\": {\n              \"name\": \"testerdetest\",\n              \"published\": false,\n              \"rmlUri\": \"<<The get raw data url that the server provides>>\"\n            }\n          },\n          \"public\": {\n            \"WomenWriters\": {\n              \"name\": \"WomenWriters\"\n            },\n            \"CharterPortaal\": {\n              \"name\": \"CharterPortaal\"\n            },\n            \"EuropeseMigratie\": {\n              \"name\": \"EuropeseMigratie\"\n            },\n            \"ckcc\": {\n              \"name\": \"ckcc\"\n            },\n            \"DutchCaribbean\": {\n              \"name\": \"DutchCaribbean\"\n            },\n            \"cwrs\": {\n              \"name\": \"cwrs\"\n            },\n            \"cwno\": {\n              \"name\": \"cwno\"\n            },\n            \"Admin\": {\n              \"name\": \"Admin\"\n            },\n            \"cnw\": {\n              \"name\": \"cnw\"\n            },\n            \"Base\": {\n              \"name\": \"Base\"\n            }\n          }\n        }");
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

},{}],131:[function(require,module,exports){
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

},{"./reducers":126,"./util/persist":135,"redux":86,"redux-thunk":80}],132:[function(require,module,exports){
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

},{}],133:[function(require,module,exports){
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

},{"./clone-deep":132}],134:[function(require,module,exports){
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
  } else {
    return {
      "objectMap": {
        "column": variable.variableName
      },
      "predicate": "http://timbuctoo.com/" + property
    };
  }
}

},{}],135:[function(require,module,exports){
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

},{}],136:[function(require,module,exports){
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

},{"./clone-deep":132}]},{},[123])(123)
});
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvcHJvY2Vzcy9icm93c2VyLmpzIiwibm9kZV9tb2R1bGVzL2NsYXNzbmFtZXMvaW5kZXguanMiLCJub2RlX21vZHVsZXMvZGVlcC1lcXVhbC9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9kZWVwLWVxdWFsL2xpYi9pc19hcmd1bWVudHMuanMiLCJub2RlX21vZHVsZXMvZGVlcC1lcXVhbC9saWIva2V5cy5qcyIsIm5vZGVfbW9kdWxlcy9mb3ItZWFjaC9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9nbG9iYWwvd2luZG93LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL0FjdGlvbnMuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvQXN5bmNVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9ET01TdGF0ZVN0b3JhZ2UuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvRE9NVXRpbHMuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvRXhlY3V0aW9uRW52aXJvbm1lbnQuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvUGF0aFV0aWxzLmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2NyZWF0ZUJyb3dzZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2NyZWF0ZURPTUhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvY3JlYXRlSGFzaEhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvY3JlYXRlSGlzdG9yeS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9jcmVhdGVMb2NhdGlvbi5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9jcmVhdGVNZW1vcnlIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2RlcHJlY2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9ydW5UcmFuc2l0aW9uSG9vay5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi91c2VCYXNlbmFtZS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi91c2VRdWVyaWVzLmpzIiwibm9kZV9tb2R1bGVzL2hpc3Rvcnkvbm9kZV9tb2R1bGVzL3dhcm5pbmcvYnJvd3Nlci5qcyIsIm5vZGVfbW9kdWxlcy9ob2lzdC1ub24tcmVhY3Qtc3RhdGljcy9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9pbnZhcmlhbnQvYnJvd3Nlci5qcyIsIm5vZGVfbW9kdWxlcy9pcy1mdW5jdGlvbi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX2dldFByb3RvdHlwZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX2lzSG9zdE9iamVjdC5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX292ZXJBcmcuanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL2lzT2JqZWN0TGlrZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvaXNQbGFpbk9iamVjdC5qcyIsIm5vZGVfbW9kdWxlcy9wYXJzZS1oZWFkZXJzL3BhcnNlLWhlYWRlcnMuanMiLCJub2RlX21vZHVsZXMvcXVlcnktc3RyaW5nL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi9jb21wb25lbnRzL1Byb3ZpZGVyLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi9jb21wb25lbnRzL2Nvbm5lY3QuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi91dGlscy9zaGFsbG93RXF1YWwuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3N0b3JlU2hhcGUuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3dhcm5pbmcuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3dyYXBBY3Rpb25DcmVhdG9ycy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0FzeW5jVXRpbHMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9IaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvSW5kZXhMaW5rLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvSW5kZXhSZWRpcmVjdC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0luZGV4Um91dGUuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9JbnRlcm5hbFByb3BUeXBlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0xpZmVjeWNsZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0xpbmsuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9QYXR0ZXJuVXRpbHMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9Qcm9wVHlwZXMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9SZWRpcmVjdC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvUm91dGVDb250ZXh0LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvUm91dGVVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlci5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlckNvbnRleHQuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9Sb3V0ZXJVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRpbmdDb250ZXh0LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvVHJhbnNpdGlvblV0aWxzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvYXBwbHlSb3V0ZXJNaWRkbGV3YXJlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvYnJvd3Nlckhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9jb21wdXRlQ2hhbmdlZFJvdXRlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2NyZWF0ZU1lbW9yeUhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9jcmVhdGVSb3V0ZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvZ2V0Q29tcG9uZW50cy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2dldFJvdXRlUGFyYW1zLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvaGFzaEhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2lzQWN0aXZlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvbWFrZVN0YXRlV2l0aExvY2F0aW9uLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvbWF0Y2guanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9tYXRjaFJvdXRlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL3JvdXRlcldhcm5pbmcuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi91c2VSb3V0ZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvdXNlUm91dGVzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvd2l0aFJvdXRlci5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC10aHVuay9saWIvaW5kZXguanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2FwcGx5TWlkZGxld2FyZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvYmluZEFjdGlvbkNyZWF0b3JzLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9jb21iaW5lUmVkdWNlcnMuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NvbXBvc2UuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NyZWF0ZVN0b3JlLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvdXRpbHMvd2FybmluZy5qcyIsIm5vZGVfbW9kdWxlcy9zdHJpY3QtdXJpLWVuY29kZS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9wb255ZmlsbC5qcyIsIm5vZGVfbW9kdWxlcy90cmltL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3dhcm5pbmcvYnJvd3Nlci5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9saWIvTW9ja1JlcXVlc3QuanMiLCJub2RlX21vZHVsZXMveGhyLW1vY2svbGliL01vY2tSZXNwb25zZS5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9saWIvTW9ja1hNTEh0dHBSZXF1ZXN0LmpzIiwibm9kZV9tb2R1bGVzL3hoci9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy94dGVuZC9pbW11dGFibGUuanMiLCJzcmMvYWN0aW9ucy5qcyIsInNyYy9jb21wb25lbnRzL2FwcC5qc3giLCJzcmMvY29tcG9uZW50cy9jb2xsZWN0aW9uLWZvcm0uanMiLCJzcmMvY29tcG9uZW50cy9jb2xsZWN0aW9uLWluZGV4LmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1vdmVydmlldy5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tdGFibGUuanMiLCJzcmMvY29tcG9uZW50cy9jb25uZWN0LWRhdGEuanMiLCJzcmMvY29tcG9uZW50cy9jb25uZWN0LXRvLWFyY2hldHlwZS5qcyIsInNyYy9jb21wb25lbnRzL2RhdGFzZXQtY2FyZHMuanMiLCJzcmMvY29tcG9uZW50cy9kYXRhc2V0Q2FyZC5qc3giLCJzcmMvY29tcG9uZW50cy9maWVsZHMvc2VsZWN0LWZpZWxkLmpzIiwic3JjL2NvbXBvbmVudHMvZmlyc3RVcGxvYWQuanN4Iiwic3JjL2NvbXBvbmVudHMvZm9vdGVyLmpzIiwic3JjL2NvbXBvbmVudHMvbWVzc2FnZS5qcyIsInNyYy9jb21wb25lbnRzL3BhZ2UuanN4Iiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9hZGQtcHJvcGVydHkuanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL2NvbHVtbi1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL3Byb3BlcnR5LWZvcm0uanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL3JlbGF0aW9uLmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS90ZXh0LmpzIiwic3JjL2NvbXBvbmVudHMvcmVhY3QtcmVkdXgtY29ubmVjdC9jb25uZWN0LWRhdGEuanMiLCJzcmMvY29tcG9uZW50cy90YWJsZS9kYXRhLXJvdy5qcyIsInNyYy9jb21wb25lbnRzL3RhYmxlL2hlYWRlci1jZWxsLmpzIiwic3JjL2NvbXBvbmVudHMvdXBsb2FkLWJ1dHRvbi5qcyIsInNyYy9pbmRleC5qcyIsInNyYy9yZWR1Y2Vycy9hcmNoZXR5cGUuanMiLCJzcmMvcmVkdWNlcnMvaW1wb3J0LWRhdGEuanMiLCJzcmMvcmVkdWNlcnMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvbWFwcGluZ3MuanMiLCJzcmMvcmVkdWNlcnMvdXNlcmRhdGEuanMiLCJzcmMvcm91dGVyLmpzIiwic3JjL3NlcnZlcm1vY2tzLmpzIiwic3JjL3N0b3JlLmpzIiwic3JjL3V0aWwvY2xvbmUtZGVlcC5qcyIsInNyYy91dGlsL2dldC1pbi5qcyIsInNyYy91dGlsL21hcHBpbmdUb0pzb25MZFJtbC5qcyIsInNyYy91dGlsL3BlcnNpc3QuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2hLQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNoREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM5RkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3BCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNUQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUM5Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUNUQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM5QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ3pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ3hFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDMUVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDOUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNuTEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDdkNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNyUEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQy9SQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ3pKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNsQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ3ZCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQzdKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQy9LQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzVEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ2xEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ25EQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNOQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDcEJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM3QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDckVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzlCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDbEVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUM3RUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDeFlBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDaEJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDekJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDVkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3ZCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDWEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ3ZGQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDNUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUMzQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQzlEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUMzREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDL0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDbkVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUMxS0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNuTkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNwR0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ3JHQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDeERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQzVGQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQy9OQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDM0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDL0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUM3QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ3pIQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQy9CQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDbEJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDblRBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzFFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM3Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN6QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDZkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzNKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUN2SkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDaERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUNoRkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQzFQQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbkNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ3RCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUNsREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdkNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDYkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDekRBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDOUhBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3ZDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDclFBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDN0NBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3hCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDTkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbkJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDZEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUM1REE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDNUdBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM3RUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4RkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMvTkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFD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Ozs7OztBQUVBO0FBQ0E7QUFDQTtBQUNBLElBQU0sWUFBWSxTQUFaLFNBQVksQ0FBQyxJQUFELEVBQU8sS0FBUCxFQUFjLEdBQWQsRUFBbUIsR0FBbkIsRUFBMkI7QUFDNUMsRUFBQyxTQUFTLElBQVYsRUFBZ0IsR0FBaEIsSUFBdUIsR0FBdkI7QUFDQSxRQUFPLElBQVA7QUFDQSxDQUhEOztBQUtBO0FBQ0EsSUFBTSxTQUFTLFNBQVQsTUFBUyxDQUFDLElBQUQsRUFBTyxLQUFQLEVBQWMsSUFBZDtBQUFBLEtBQW9CLEtBQXBCLHlEQUE0QixJQUE1QjtBQUFBLFFBQ2QsS0FBSyxNQUFMLEdBQWMsQ0FBZCxHQUNDLE9BQU8sSUFBUCxFQUFhLEtBQWIsRUFBb0IsSUFBcEIsRUFBMEIsUUFBUSxNQUFNLEtBQUssS0FBTCxFQUFOLENBQVIsR0FBOEIsS0FBSyxLQUFLLEtBQUwsRUFBTCxDQUF4RCxDQURELEdBRUMsVUFBVSxJQUFWLEVBQWdCLEtBQWhCLEVBQXVCLEtBQUssQ0FBTCxDQUF2QixFQUFnQyxLQUFoQyxDQUhhO0FBQUEsQ0FBZjs7QUFLQSxJQUFNLFFBQVEsU0FBUixLQUFRLENBQUMsSUFBRCxFQUFPLEtBQVAsRUFBYyxJQUFkO0FBQUEsUUFDYixPQUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIi8vIHNoaW0gZm9yIHVzaW5nIHByb2Nlc3MgaW4gYnJvd3NlclxudmFyIHByb2Nlc3MgPSBtb2R1bGUuZXhwb3J0cyA9IHt9O1xuXG4vLyBjYWNoZWQgZnJvbSB3aGF0ZXZlciBnbG9iYWwgaXMgcHJlc2VudCBzbyB0aGF0IHRlc3QgcnVubmVycyB0aGF0IHN0dWIgaXRcbi8vIGRvbid0IGJyZWFrIHRoaW5ncy4gIEJ1dCB3ZSBuZWVkIHRvIHdyYXAgaXQgaW4gYSB0cnkgY2F0Y2ggaW4gY2FzZSBpdCBpc1xuLy8gd3JhcHBlZCBpbiBzdHJpY3QgbW9kZSBjb2RlIHdoaWNoIGRvZXNuJ3QgZGVmaW5lIGFueSBnbG9iYWxzLiAgSXQncyBpbnNpZGUgYVxuLy8gZnVuY3Rpb24gYmVjYXVzZSB0cnkvY2F0Y2hlcyBkZW9wdGltaXplIGluIGNlcnRhaW4gZW5naW5lcy5cblxudmFyIGNhY2hlZFNldFRpbWVvdXQ7XG52YXIgY2FjaGVkQ2xlYXJUaW1lb3V0O1xuXG4oZnVuY3Rpb24gKCkge1xuICAgIHRyeSB7XG4gICAgICAgIGNhY2hlZFNldFRpbWVvdXQgPSBzZXRUaW1lb3V0O1xuICAgIH0gY2F0Y2ggKGUpIHtcbiAgICAgICAgY2FjaGVkU2V0VGltZW91dCA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignc2V0VGltZW91dCBpcyBub3QgZGVmaW5lZCcpO1xuICAgICAgICB9XG4gICAgfVxuICAgIHRyeSB7XG4gICAgICAgIGNhY2hlZENsZWFyVGltZW91dCA9IGNsZWFyVGltZW91dDtcbiAgICB9IGNhdGNoIChlKSB7XG4gICAgICAgIGNhY2hlZENsZWFyVGltZW91dCA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignY2xlYXJUaW1lb3V0IGlzIG5vdCBkZWZpbmVkJyk7XG4gICAgICAgIH1cbiAgICB9XG59ICgpKVxuZnVuY3Rpb24gcnVuVGltZW91dChmdW4pIHtcbiAgICBpZiAoY2FjaGVkU2V0VGltZW91dCA9PT0gc2V0VGltZW91dCkge1xuICAgICAgICAvL25vcm1hbCBlbnZpcm9tZW50cyBpbiBzYW5lIHNpdHVhdGlvbnNcbiAgICAgICAgcmV0dXJuIHNldFRpbWVvdXQoZnVuLCAwKTtcbiAgICB9XG4gICAgdHJ5IHtcbiAgICAgICAgLy8gd2hlbiB3aGVuIHNvbWVib2R5IGhhcyBzY3Jld2VkIHdpdGggc2V0VGltZW91dCBidXQgbm8gSS5FLiBtYWRkbmVzc1xuICAgICAgICByZXR1cm4gY2FjaGVkU2V0VGltZW91dChmdW4sIDApO1xuICAgIH0gY2F0Y2goZSl7XG4gICAgICAgIHRyeSB7XG4gICAgICAgICAgICAvLyBXaGVuIHdlIGFyZSBpbiBJLkUuIGJ1dCB0aGUgc2NyaXB0IGhhcyBiZWVuIGV2YWxlZCBzbyBJLkUuIGRvZXNuJ3QgdHJ1c3QgdGhlIGdsb2JhbCBvYmplY3Qgd2hlbiBjYWxsZWQgbm9ybWFsbHlcbiAgICAgICAgICAgIHJldHVybiBjYWNoZWRTZXRUaW1lb3V0LmNhbGwobnVsbCwgZnVuLCAwKTtcbiAgICAgICAgfSBjYXRjaChlKXtcbiAgICAgICAgICAgIC8vIHNhbWUgYXMgYWJvdmUgYnV0IHdoZW4gaXQncyBhIHZlcnNpb24gb2YgSS5FLiB0aGF0IG11c3QgaGF2ZSB0aGUgZ2xvYmFsIG9iamVjdCBmb3IgJ3RoaXMnLCBob3BmdWxseSBvdXIgY29udGV4dCBjb3JyZWN0IG90aGVyd2lzZSBpdCB3aWxsIHRocm93IGEgZ2xvYmFsIGVycm9yXG4gICAgICAgICAgICByZXR1cm4gY2FjaGVkU2V0VGltZW91dC5jYWxsKHRoaXMsIGZ1biwgMCk7XG4gICAgICAgIH1cbiAgICB9XG5cblxufVxuZnVuY3Rpb24gcnVuQ2xlYXJUaW1lb3V0KG1hcmtlcikge1xuICAgIGlmIChjYWNoZWRDbGVhclRpbWVvdXQgPT09IGNsZWFyVGltZW91dCkge1xuICAgICAgICAvL25vcm1hbCBlbnZpcm9tZW50cyBpbiBzYW5lIHNpdHVhdGlvbnNcbiAgICAgICAgcmV0dXJuIGNsZWFyVGltZW91dChtYXJrZXIpO1xuICAgIH1cbiAgICB0cnkge1xuICAgICAgICAvLyB3aGVuIHdoZW4gc29tZWJvZHkgaGFzIHNjcmV3ZWQgd2l0aCBzZXRUaW1lb3V0IGJ1dCBubyBJLkUuIG1hZGRuZXNzXG4gICAgICAgIHJldHVybiBjYWNoZWRDbGVhclRpbWVvdXQobWFya2VyKTtcbiAgICB9IGNhdGNoIChlKXtcbiAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgIC8vIFdoZW4gd2UgYXJlIGluIEkuRS4gYnV0IHRoZSBzY3JpcHQgaGFzIGJlZW4gZXZhbGVkIHNvIEkuRS4gZG9lc24ndCAgdHJ1c3QgdGhlIGdsb2JhbCBvYmplY3Qgd2hlbiBjYWxsZWQgbm9ybWFsbHlcbiAgICAgICAgICAgIHJldHVybiBjYWNoZWRDbGVhclRpbWVvdXQuY2FsbChudWxsLCBtYXJrZXIpO1xuICAgICAgICB9IGNhdGNoIChlKXtcbiAgICAgICAgICAgIC8vIHNhbWUgYXMgYWJvdmUgYnV0IHdoZW4gaXQncyBhIHZlcnNpb24gb2YgSS5FLiB0aGF0IG11c3QgaGF2ZSB0aGUgZ2xvYmFsIG9iamVjdCBmb3IgJ3RoaXMnLCBob3BmdWxseSBvdXIgY29udGV4dCBjb3JyZWN0IG90aGVyd2lzZSBpdCB3aWxsIHRocm93IGEgZ2xvYmFsIGVycm9yLlxuICAgICAgICAgICAgLy8gU29tZSB2ZXJzaW9ucyBvZiBJLkUuIGhhdmUgZGlmZmVyZW50IHJ1bGVzIGZvciBjbGVhclRpbWVvdXQgdnMgc2V0VGltZW91dFxuICAgICAgICAgICAgcmV0dXJuIGNhY2hlZENsZWFyVGltZW91dC5jYWxsKHRoaXMsIG1hcmtlcik7XG4gICAgICAgIH1cbiAgICB9XG5cblxuXG59XG52YXIgcXVldWUgPSBbXTtcbnZhciBkcmFpbmluZyA9IGZhbHNlO1xudmFyIGN1cnJlbnRRdWV1ZTtcbnZhciBxdWV1ZUluZGV4ID0gLTE7XG5cbmZ1bmN0aW9uIGNsZWFuVXBOZXh0VGljaygpIHtcbiAgICBpZiAoIWRyYWluaW5nIHx8ICFjdXJyZW50UXVldWUpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgIH1cbiAgICBkcmFpbmluZyA9IGZhbHNlO1xuICAgIGlmIChjdXJyZW50UXVldWUubGVuZ3RoKSB7XG4gICAgICAgIHF1ZXVlID0gY3VycmVudFF1ZXVlLmNvbmNhdChxdWV1ZSk7XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcXVldWVJbmRleCA9IC0xO1xuICAgIH1cbiAgICBpZiAocXVldWUubGVuZ3RoKSB7XG4gICAgICAgIGRyYWluUXVldWUoKTtcbiAgICB9XG59XG5cbmZ1bmN0aW9uIGRyYWluUXVldWUoKSB7XG4gICAgaWYgKGRyYWluaW5nKSB7XG4gICAgICAgIHJldHVybjtcbiAgICB9XG4gICAgdmFyIHRpbWVvdXQgPSBydW5UaW1lb3V0KGNsZWFuVXBOZXh0VGljayk7XG4gICAgZHJhaW5pbmcgPSB0cnVlO1xuXG4gICAgdmFyIGxlbiA9IHF1ZXVlLmxlbmd0aDtcbiAgICB3aGlsZShsZW4pIHtcbiAgICAgICAgY3VycmVudFF1ZXVlID0gcXVldWU7XG4gICAgICAgIHF1ZXVlID0gW107XG4gICAgICAgIHdoaWxlICgrK3F1ZXVlSW5kZXggPCBsZW4pIHtcbiAgICAgICAgICAgIGlmIChjdXJyZW50UXVldWUpIHtcbiAgICAgICAgICAgICAgICBjdXJyZW50UXVldWVbcXVldWVJbmRleF0ucnVuKCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICAgICAgcXVldWVJbmRleCA9IC0xO1xuICAgICAgICBsZW4gPSBxdWV1ZS5sZW5ndGg7XG4gICAgfVxuICAgIGN1cnJlbnRRdWV1ZSA9IG51bGw7XG4gICAgZHJhaW5pbmcgPSBmYWxzZTtcbiAgICBydW5DbGVhclRpbWVvdXQodGltZW91dCk7XG59XG5cbnByb2Nlc3MubmV4dFRpY2sgPSBmdW5jdGlvbiAoZnVuKSB7XG4gICAgdmFyIGFyZ3MgPSBuZXcgQXJyYXkoYXJndW1lbnRzLmxlbmd0aCAtIDEpO1xuICAgIGlmIChhcmd1bWVudHMubGVuZ3RoID4gMSkge1xuICAgICAgICBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykge1xuICAgICAgICAgICAgYXJnc1tpIC0gMV0gPSBhcmd1bWVudHNbaV07XG4gICAgICAgIH1cbiAgICB9XG4gICAgcXVldWUucHVzaChuZXcgSXRlbShmdW4sIGFyZ3MpKTtcbiAgICBpZiAocXVldWUubGVuZ3RoID09PSAxICYmICFkcmFpbmluZykge1xuICAgICAgICBydW5UaW1lb3V0KGRyYWluUXVldWUpO1xuICAgIH1cbn07XG5cbi8vIHY4IGxpa2VzIHByZWRpY3RpYmxlIG9iamVjdHNcbmZ1bmN0aW9uIEl0ZW0oZnVuLCBhcnJheSkge1xuICAgIHRoaXMuZnVuID0gZnVuO1xuICAgIHRoaXMuYXJyYXkgPSBhcnJheTtcbn1cbkl0ZW0ucHJvdG90eXBlLnJ1biA9IGZ1bmN0aW9uICgpIHtcbiAgICB0aGlzLmZ1bi5hcHBseShudWxsLCB0aGlzLmFycmF5KTtcbn07XG5wcm9jZXNzLnRpdGxlID0gJ2Jyb3dzZXInO1xucHJvY2Vzcy5icm93c2VyID0gdHJ1ZTtcbnByb2Nlc3MuZW52ID0ge307XG5wcm9jZXNzLmFyZ3YgPSBbXTtcbnByb2Nlc3MudmVyc2lvbiA9ICcnOyAvLyBlbXB0eSBzdHJpbmcgdG8gYXZvaWQgcmVnZXhwIGlzc3Vlc1xucHJvY2Vzcy52ZXJzaW9ucyA9IHt9O1xuXG5mdW5jdGlvbiBub29wKCkge31cblxucHJvY2Vzcy5vbiA9IG5vb3A7XG5wcm9jZXNzLmFkZExpc3RlbmVyID0gbm9vcDtcbnByb2Nlc3Mub25jZSA9IG5vb3A7XG5wcm9jZXNzLm9mZiA9IG5vb3A7XG5wcm9jZXNzLnJlbW92ZUxpc3RlbmVyID0gbm9vcDtcbnByb2Nlc3MucmVtb3ZlQWxsTGlzdGVuZXJzID0gbm9vcDtcbnByb2Nlc3MuZW1pdCA9IG5vb3A7XG5cbnByb2Nlc3MuYmluZGluZyA9IGZ1bmN0aW9uIChuYW1lKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdwcm9jZXNzLmJpbmRpbmcgaXMgbm90IHN1cHBvcnRlZCcpO1xufTtcblxucHJvY2Vzcy5jd2QgPSBmdW5jdGlvbiAoKSB7IHJldHVybiAnLycgfTtcbnByb2Nlc3MuY2hkaXIgPSBmdW5jdGlvbiAoZGlyKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdwcm9jZXNzLmNoZGlyIGlzIG5vdCBzdXBwb3J0ZWQnKTtcbn07XG5wcm9jZXNzLnVtYXNrID0gZnVuY3Rpb24oKSB7IHJldHVybiAwOyB9O1xuIiwiLyohXG4gIENvcHlyaWdodCAoYykgMjAxNiBKZWQgV2F0c29uLlxuICBMaWNlbnNlZCB1bmRlciB0aGUgTUlUIExpY2Vuc2UgKE1JVCksIHNlZVxuICBodHRwOi8vamVkd2F0c29uLmdpdGh1Yi5pby9jbGFzc25hbWVzXG4qL1xuLyogZ2xvYmFsIGRlZmluZSAqL1xuXG4oZnVuY3Rpb24gKCkge1xuXHQndXNlIHN0cmljdCc7XG5cblx0dmFyIGhhc093biA9IHt9Lmhhc093blByb3BlcnR5O1xuXG5cdGZ1bmN0aW9uIGNsYXNzTmFtZXMgKCkge1xuXHRcdHZhciBjbGFzc2VzID0gW107XG5cblx0XHRmb3IgKHZhciBpID0gMDsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykge1xuXHRcdFx0dmFyIGFyZyA9IGFyZ3VtZW50c1tpXTtcblx0XHRcdGlmICghYXJnKSBjb250aW51ZTtcblxuXHRcdFx0dmFyIGFyZ1R5cGUgPSB0eXBlb2YgYXJnO1xuXG5cdFx0XHRpZiAoYXJnVHlwZSA9PT0gJ3N0cmluZycgfHwgYXJnVHlwZSA9PT0gJ251bWJlcicpIHtcblx0XHRcdFx0Y2xhc3Nlcy5wdXNoKGFyZyk7XG5cdFx0XHR9IGVsc2UgaWYgKEFycmF5LmlzQXJyYXkoYXJnKSkge1xuXHRcdFx0XHRjbGFzc2VzLnB1c2goY2xhc3NOYW1lcy5hcHBseShudWxsLCBhcmcpKTtcblx0XHRcdH0gZWxzZSBpZiAoYXJnVHlwZSA9PT0gJ29iamVjdCcpIHtcblx0XHRcdFx0Zm9yICh2YXIga2V5IGluIGFyZykge1xuXHRcdFx0XHRcdGlmIChoYXNPd24uY2FsbChhcmcsIGtleSkgJiYgYXJnW2tleV0pIHtcblx0XHRcdFx0XHRcdGNsYXNzZXMucHVzaChrZXkpO1xuXHRcdFx0XHRcdH1cblx0XHRcdFx0fVxuXHRcdFx0fVxuXHRcdH1cblxuXHRcdHJldHVybiBjbGFzc2VzLmpvaW4oJyAnKTtcblx0fVxuXG5cdGlmICh0eXBlb2YgbW9kdWxlICE9PSAndW5kZWZpbmVkJyAmJiBtb2R1bGUuZXhwb3J0cykge1xuXHRcdG1vZHVsZS5leHBvcnRzID0gY2xhc3NOYW1lcztcblx0fSBlbHNlIGlmICh0eXBlb2YgZGVmaW5lID09PSAnZnVuY3Rpb24nICYmIHR5cGVvZiBkZWZpbmUuYW1kID09PSAnb2JqZWN0JyAmJiBkZWZpbmUuYW1kKSB7XG5cdFx0Ly8gcmVnaXN0ZXIgYXMgJ2NsYXNzbmFtZXMnLCBjb25zaXN0ZW50IHdpdGggbnBtIHBhY2thZ2UgbmFtZVxuXHRcdGRlZmluZSgnY2xhc3NuYW1lcycsIFtdLCBmdW5jdGlvbiAoKSB7XG5cdFx0XHRyZXR1cm4gY2xhc3NOYW1lcztcblx0XHR9KTtcblx0fSBlbHNlIHtcblx0XHR3aW5kb3cuY2xhc3NOYW1lcyA9IGNsYXNzTmFtZXM7XG5cdH1cbn0oKSk7XG4iLCJ2YXIgcFNsaWNlID0gQXJyYXkucHJvdG90eXBlLnNsaWNlO1xudmFyIG9iamVjdEtleXMgPSByZXF1aXJlKCcuL2xpYi9rZXlzLmpzJyk7XG52YXIgaXNBcmd1bWVudHMgPSByZXF1aXJlKCcuL2xpYi9pc19hcmd1bWVudHMuanMnKTtcblxudmFyIGRlZXBFcXVhbCA9IG1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24gKGFjdHVhbCwgZXhwZWN0ZWQsIG9wdHMpIHtcbiAgaWYgKCFvcHRzKSBvcHRzID0ge307XG4gIC8vIDcuMS4gQWxsIGlkZW50aWNhbCB2YWx1ZXMgYXJlIGVxdWl2YWxlbnQsIGFzIGRldGVybWluZWQgYnkgPT09LlxuICBpZiAoYWN0dWFsID09PSBleHBlY3RlZCkge1xuICAgIHJldHVybiB0cnVlO1xuXG4gIH0gZWxzZSBpZiAoYWN0dWFsIGluc3RhbmNlb2YgRGF0ZSAmJiBleHBlY3RlZCBpbnN0YW5jZW9mIERhdGUpIHtcbiAgICByZXR1cm4gYWN0dWFsLmdldFRpbWUoKSA9PT0gZXhwZWN0ZWQuZ2V0VGltZSgpO1xuXG4gIC8vIDcuMy4gT3RoZXIgcGFpcnMgdGhhdCBkbyBub3QgYm90aCBwYXNzIHR5cGVvZiB2YWx1ZSA9PSAnb2JqZWN0JyxcbiAgLy8gZXF1aXZhbGVuY2UgaXMgZGV0ZXJtaW5lZCBieSA9PS5cbiAgfSBlbHNlIGlmICghYWN0dWFsIHx8ICFleHBlY3RlZCB8fCB0eXBlb2YgYWN0dWFsICE9ICdvYmplY3QnICYmIHR5cGVvZiBleHBlY3RlZCAhPSAnb2JqZWN0Jykge1xuICAgIHJldHVybiBvcHRzLnN0cmljdCA/IGFjdHVhbCA9PT0gZXhwZWN0ZWQgOiBhY3R1YWwgPT0gZXhwZWN0ZWQ7XG5cbiAgLy8gNy40LiBGb3IgYWxsIG90aGVyIE9iamVjdCBwYWlycywgaW5jbHVkaW5nIEFycmF5IG9iamVjdHMsIGVxdWl2YWxlbmNlIGlzXG4gIC8vIGRldGVybWluZWQgYnkgaGF2aW5nIHRoZSBzYW1lIG51bWJlciBvZiBvd25lZCBwcm9wZXJ0aWVzIChhcyB2ZXJpZmllZFxuICAvLyB3aXRoIE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbCksIHRoZSBzYW1lIHNldCBvZiBrZXlzXG4gIC8vIChhbHRob3VnaCBub3QgbmVjZXNzYXJpbHkgdGhlIHNhbWUgb3JkZXIpLCBlcXVpdmFsZW50IHZhbHVlcyBmb3IgZXZlcnlcbiAgLy8gY29ycmVzcG9uZGluZyBrZXksIGFuZCBhbiBpZGVudGljYWwgJ3Byb3RvdHlwZScgcHJvcGVydHkuIE5vdGU6IHRoaXNcbiAgLy8gYWNjb3VudHMgZm9yIGJvdGggbmFtZWQgYW5kIGluZGV4ZWQgcHJvcGVydGllcyBvbiBBcnJheXMuXG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIG9iakVxdWl2KGFjdHVhbCwgZXhwZWN0ZWQsIG9wdHMpO1xuICB9XG59XG5cbmZ1bmN0aW9uIGlzVW5kZWZpbmVkT3JOdWxsKHZhbHVlKSB7XG4gIHJldHVybiB2YWx1ZSA9PT0gbnVsbCB8fCB2YWx1ZSA9PT0gdW5kZWZpbmVkO1xufVxuXG5mdW5jdGlvbiBpc0J1ZmZlciAoeCkge1xuICBpZiAoIXggfHwgdHlwZW9mIHggIT09ICdvYmplY3QnIHx8IHR5cGVvZiB4Lmxlbmd0aCAhPT0gJ251bWJlcicpIHJldHVybiBmYWxzZTtcbiAgaWYgKHR5cGVvZiB4LmNvcHkgIT09ICdmdW5jdGlvbicgfHwgdHlwZW9mIHguc2xpY2UgIT09ICdmdW5jdGlvbicpIHtcbiAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgaWYgKHgubGVuZ3RoID4gMCAmJiB0eXBlb2YgeFswXSAhPT0gJ251bWJlcicpIHJldHVybiBmYWxzZTtcbiAgcmV0dXJuIHRydWU7XG59XG5cbmZ1bmN0aW9uIG9iakVxdWl2KGEsIGIsIG9wdHMpIHtcbiAgdmFyIGksIGtleTtcbiAgaWYgKGlzVW5kZWZpbmVkT3JOdWxsKGEpIHx8IGlzVW5kZWZpbmVkT3JOdWxsKGIpKVxuICAgIHJldHVybiBmYWxzZTtcbiAgLy8gYW4gaWRlbnRpY2FsICdwcm90b3R5cGUnIHByb3BlcnR5LlxuICBpZiAoYS5wcm90b3R5cGUgIT09IGIucHJvdG90eXBlKSByZXR1cm4gZmFsc2U7XG4gIC8vfn5+SSd2ZSBtYW5hZ2VkIHRvIGJyZWFrIE9iamVjdC5rZXlzIHRocm91Z2ggc2NyZXd5IGFyZ3VtZW50cyBwYXNzaW5nLlxuICAvLyAgIENvbnZlcnRpbmcgdG8gYXJyYXkgc29sdmVzIHRoZSBwcm9ibGVtLlxuICBpZiAoaXNBcmd1bWVudHMoYSkpIHtcbiAgICBpZiAoIWlzQXJndW1lbnRzKGIpKSB7XG4gICAgICByZXR1cm4gZmFsc2U7XG4gICAgfVxuICAgIGEgPSBwU2xpY2UuY2FsbChhKTtcbiAgICBiID0gcFNsaWNlLmNhbGwoYik7XG4gICAgcmV0dXJuIGRlZXBFcXVhbChhLCBiLCBvcHRzKTtcbiAgfVxuICBpZiAoaXNCdWZmZXIoYSkpIHtcbiAgICBpZiAoIWlzQnVmZmVyKGIpKSB7XG4gICAgICByZXR1cm4gZmFsc2U7XG4gICAgfVxuICAgIGlmIChhLmxlbmd0aCAhPT0gYi5sZW5ndGgpIHJldHVybiBmYWxzZTtcbiAgICBmb3IgKGkgPSAwOyBpIDwgYS5sZW5ndGg7IGkrKykge1xuICAgICAgaWYgKGFbaV0gIT09IGJbaV0pIHJldHVybiBmYWxzZTtcbiAgICB9XG4gICAgcmV0dXJuIHRydWU7XG4gIH1cbiAgdHJ5IHtcbiAgICB2YXIga2EgPSBvYmplY3RLZXlzKGEpLFxuICAgICAgICBrYiA9IG9iamVjdEtleXMoYik7XG4gIH0gY2F0Y2ggKGUpIHsvL2hhcHBlbnMgd2hlbiBvbmUgaXMgYSBzdHJpbmcgbGl0ZXJhbCBhbmQgdGhlIG90aGVyIGlzbid0XG4gICAgcmV0dXJuIGZhbHNlO1xuICB9XG4gIC8vIGhhdmluZyB0aGUgc2FtZSBudW1iZXIgb2Ygb3duZWQgcHJvcGVydGllcyAoa2V5cyBpbmNvcnBvcmF0ZXNcbiAgLy8gaGFzT3duUHJvcGVydHkpXG4gIGlmIChrYS5sZW5ndGggIT0ga2IubGVuZ3RoKVxuICAgIHJldHVybiBmYWxzZTtcbiAgLy90aGUgc2FtZSBzZXQgb2Yga2V5cyAoYWx0aG91Z2ggbm90IG5lY2Vzc2FyaWx5IHRoZSBzYW1lIG9yZGVyKSxcbiAga2Euc29ydCgpO1xuICBrYi5zb3J0KCk7XG4gIC8vfn5+Y2hlYXAga2V5IHRlc3RcbiAgZm9yIChpID0ga2EubGVuZ3RoIC0gMTsgaSA+PSAwOyBpLS0pIHtcbiAgICBpZiAoa2FbaV0gIT0ga2JbaV0pXG4gICAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgLy9lcXVpdmFsZW50IHZhbHVlcyBmb3IgZXZlcnkgY29ycmVzcG9uZGluZyBrZXksIGFuZFxuICAvL35+fnBvc3NpYmx5IGV4cGVuc2l2ZSBkZWVwIHRlc3RcbiAgZm9yIChpID0ga2EubGVuZ3RoIC0gMTsgaSA+PSAwOyBpLS0pIHtcbiAgICBrZXkgPSBrYVtpXTtcbiAgICBpZiAoIWRlZXBFcXVhbChhW2tleV0sIGJba2V5XSwgb3B0cykpIHJldHVybiBmYWxzZTtcbiAgfVxuICByZXR1cm4gdHlwZW9mIGEgPT09IHR5cGVvZiBiO1xufVxuIiwidmFyIHN1cHBvcnRzQXJndW1lbnRzQ2xhc3MgPSAoZnVuY3Rpb24oKXtcbiAgcmV0dXJuIE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbChhcmd1bWVudHMpXG59KSgpID09ICdbb2JqZWN0IEFyZ3VtZW50c10nO1xuXG5leHBvcnRzID0gbW9kdWxlLmV4cG9ydHMgPSBzdXBwb3J0c0FyZ3VtZW50c0NsYXNzID8gc3VwcG9ydGVkIDogdW5zdXBwb3J0ZWQ7XG5cbmV4cG9ydHMuc3VwcG9ydGVkID0gc3VwcG9ydGVkO1xuZnVuY3Rpb24gc3VwcG9ydGVkKG9iamVjdCkge1xuICByZXR1cm4gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKG9iamVjdCkgPT0gJ1tvYmplY3QgQXJndW1lbnRzXSc7XG59O1xuXG5leHBvcnRzLnVuc3VwcG9ydGVkID0gdW5zdXBwb3J0ZWQ7XG5mdW5jdGlvbiB1bnN1cHBvcnRlZChvYmplY3Qpe1xuICByZXR1cm4gb2JqZWN0ICYmXG4gICAgdHlwZW9mIG9iamVjdCA9PSAnb2JqZWN0JyAmJlxuICAgIHR5cGVvZiBvYmplY3QubGVuZ3RoID09ICdudW1iZXInICYmXG4gICAgT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iamVjdCwgJ2NhbGxlZScpICYmXG4gICAgIU9iamVjdC5wcm90b3R5cGUucHJvcGVydHlJc0VudW1lcmFibGUuY2FsbChvYmplY3QsICdjYWxsZWUnKSB8fFxuICAgIGZhbHNlO1xufTtcbiIsImV4cG9ydHMgPSBtb2R1bGUuZXhwb3J0cyA9IHR5cGVvZiBPYmplY3Qua2V5cyA9PT0gJ2Z1bmN0aW9uJ1xuICA/IE9iamVjdC5rZXlzIDogc2hpbTtcblxuZXhwb3J0cy5zaGltID0gc2hpbTtcbmZ1bmN0aW9uIHNoaW0gKG9iaikge1xuICB2YXIga2V5cyA9IFtdO1xuICBmb3IgKHZhciBrZXkgaW4gb2JqKSBrZXlzLnB1c2goa2V5KTtcbiAgcmV0dXJuIGtleXM7XG59XG4iLCJ2YXIgaXNGdW5jdGlvbiA9IHJlcXVpcmUoJ2lzLWZ1bmN0aW9uJylcblxubW9kdWxlLmV4cG9ydHMgPSBmb3JFYWNoXG5cbnZhciB0b1N0cmluZyA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmdcbnZhciBoYXNPd25Qcm9wZXJ0eSA9IE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHlcblxuZnVuY3Rpb24gZm9yRWFjaChsaXN0LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGlmICghaXNGdW5jdGlvbihpdGVyYXRvcikpIHtcbiAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignaXRlcmF0b3IgbXVzdCBiZSBhIGZ1bmN0aW9uJylcbiAgICB9XG5cbiAgICBpZiAoYXJndW1lbnRzLmxlbmd0aCA8IDMpIHtcbiAgICAgICAgY29udGV4dCA9IHRoaXNcbiAgICB9XG4gICAgXG4gICAgaWYgKHRvU3RyaW5nLmNhbGwobGlzdCkgPT09ICdbb2JqZWN0IEFycmF5XScpXG4gICAgICAgIGZvckVhY2hBcnJheShsaXN0LCBpdGVyYXRvciwgY29udGV4dClcbiAgICBlbHNlIGlmICh0eXBlb2YgbGlzdCA9PT0gJ3N0cmluZycpXG4gICAgICAgIGZvckVhY2hTdHJpbmcobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpXG4gICAgZWxzZVxuICAgICAgICBmb3JFYWNoT2JqZWN0KGxpc3QsIGl0ZXJhdG9yLCBjb250ZXh0KVxufVxuXG5mdW5jdGlvbiBmb3JFYWNoQXJyYXkoYXJyYXksIGl0ZXJhdG9yLCBjb250ZXh0KSB7XG4gICAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IGFycmF5Lmxlbmd0aDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKGFycmF5LCBpKSkge1xuICAgICAgICAgICAgaXRlcmF0b3IuY2FsbChjb250ZXh0LCBhcnJheVtpXSwgaSwgYXJyYXkpXG4gICAgICAgIH1cbiAgICB9XG59XG5cbmZ1bmN0aW9uIGZvckVhY2hTdHJpbmcoc3RyaW5nLCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBzdHJpbmcubGVuZ3RoOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgLy8gbm8gc3VjaCB0aGluZyBhcyBhIHNwYXJzZSBzdHJpbmcuXG4gICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgc3RyaW5nLmNoYXJBdChpKSwgaSwgc3RyaW5nKVxuICAgIH1cbn1cblxuZnVuY3Rpb24gZm9yRWFjaE9iamVjdChvYmplY3QsIGl0ZXJhdG9yLCBjb250ZXh0KSB7XG4gICAgZm9yICh2YXIgayBpbiBvYmplY3QpIHtcbiAgICAgICAgaWYgKGhhc093blByb3BlcnR5LmNhbGwob2JqZWN0LCBrKSkge1xuICAgICAgICAgICAgaXRlcmF0b3IuY2FsbChjb250ZXh0LCBvYmplY3Rba10sIGssIG9iamVjdClcbiAgICAgICAgfVxuICAgIH1cbn1cbiIsImlmICh0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSB3aW5kb3c7XG59IGVsc2UgaWYgKHR5cGVvZiBnbG9iYWwgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICBtb2R1bGUuZXhwb3J0cyA9IGdsb2JhbDtcbn0gZWxzZSBpZiAodHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIpe1xuICAgIG1vZHVsZS5leHBvcnRzID0gc2VsZjtcbn0gZWxzZSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSB7fTtcbn1cbiIsIi8qKlxuICogSW5kaWNhdGVzIHRoYXQgbmF2aWdhdGlvbiB3YXMgY2F1c2VkIGJ5IGEgY2FsbCB0byBoaXN0b3J5LnB1c2guXG4gKi9cbid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbnZhciBQVVNIID0gJ1BVU0gnO1xuXG5leHBvcnRzLlBVU0ggPSBQVVNIO1xuLyoqXG4gKiBJbmRpY2F0ZXMgdGhhdCBuYXZpZ2F0aW9uIHdhcyBjYXVzZWQgYnkgYSBjYWxsIHRvIGhpc3RvcnkucmVwbGFjZS5cbiAqL1xudmFyIFJFUExBQ0UgPSAnUkVQTEFDRSc7XG5cbmV4cG9ydHMuUkVQTEFDRSA9IFJFUExBQ0U7XG4vKipcbiAqIEluZGljYXRlcyB0aGF0IG5hdmlnYXRpb24gd2FzIGNhdXNlZCBieSBzb21lIG90aGVyIGFjdGlvbiBzdWNoXG4gKiBhcyB1c2luZyBhIGJyb3dzZXIncyBiYWNrL2ZvcndhcmQgYnV0dG9ucyBhbmQvb3IgbWFudWFsbHkgbWFuaXB1bGF0aW5nXG4gKiB0aGUgVVJMIGluIGEgYnJvd3NlcidzIGxvY2F0aW9uIGJhci4gVGhpcyBpcyB0aGUgZGVmYXVsdC5cbiAqXG4gKiBTZWUgaHR0cHM6Ly9kZXZlbG9wZXIubW96aWxsYS5vcmcvZW4tVVMvZG9jcy9XZWIvQVBJL1dpbmRvd0V2ZW50SGFuZGxlcnMvb25wb3BzdGF0ZVxuICogZm9yIG1vcmUgaW5mb3JtYXRpb24uXG4gKi9cbnZhciBQT1AgPSAnUE9QJztcblxuZXhwb3J0cy5QT1AgPSBQT1A7XG5leHBvcnRzWydkZWZhdWx0J10gPSB7XG4gIFBVU0g6IFBVU0gsXG4gIFJFUExBQ0U6IFJFUExBQ0UsXG4gIFBPUDogUE9QXG59OyIsIlwidXNlIHN0cmljdFwiO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xudmFyIF9zbGljZSA9IEFycmF5LnByb3RvdHlwZS5zbGljZTtcbmV4cG9ydHMubG9vcEFzeW5jID0gbG9vcEFzeW5jO1xuXG5mdW5jdGlvbiBsb29wQXN5bmModHVybnMsIHdvcmssIGNhbGxiYWNrKSB7XG4gIHZhciBjdXJyZW50VHVybiA9IDAsXG4gICAgICBpc0RvbmUgPSBmYWxzZTtcbiAgdmFyIHN5bmMgPSBmYWxzZSxcbiAgICAgIGhhc05leHQgPSBmYWxzZSxcbiAgICAgIGRvbmVBcmdzID0gdW5kZWZpbmVkO1xuXG4gIGZ1bmN0aW9uIGRvbmUoKSB7XG4gICAgaXNEb25lID0gdHJ1ZTtcbiAgICBpZiAoc3luYykge1xuICAgICAgLy8gSXRlcmF0ZSBpbnN0ZWFkIG9mIHJlY3Vyc2luZyBpZiBwb3NzaWJsZS5cbiAgICAgIGRvbmVBcmdzID0gW10uY29uY2F0KF9zbGljZS5jYWxsKGFyZ3VtZW50cykpO1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGNhbGxiYWNrLmFwcGx5KHRoaXMsIGFyZ3VtZW50cyk7XG4gIH1cblxuICBmdW5jdGlvbiBuZXh0KCkge1xuICAgIGlmIChpc0RvbmUpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBoYXNOZXh0ID0gdHJ1ZTtcbiAgICBpZiAoc3luYykge1xuICAgICAgLy8gSXRlcmF0ZSBpbnN0ZWFkIG9mIHJlY3Vyc2luZyBpZiBwb3NzaWJsZS5cbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBzeW5jID0gdHJ1ZTtcblxuICAgIHdoaWxlICghaXNEb25lICYmIGN1cnJlbnRUdXJuIDwgdHVybnMgJiYgaGFzTmV4dCkge1xuICAgICAgaGFzTmV4dCA9IGZhbHNlO1xuICAgICAgd29yay5jYWxsKHRoaXMsIGN1cnJlbnRUdXJuKyssIG5leHQsIGRvbmUpO1xuICAgIH1cblxuICAgIHN5bmMgPSBmYWxzZTtcblxuICAgIGlmIChpc0RvbmUpIHtcbiAgICAgIC8vIFRoaXMgbWVhbnMgdGhlIGxvb3AgZmluaXNoZWQgc3luY2hyb25vdXNseS5cbiAgICAgIGNhbGxiYWNrLmFwcGx5KHRoaXMsIGRvbmVBcmdzKTtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBpZiAoY3VycmVudFR1cm4gPj0gdHVybnMgJiYgaGFzTmV4dCkge1xuICAgICAgaXNEb25lID0gdHJ1ZTtcbiAgICAgIGNhbGxiYWNrKCk7XG4gICAgfVxuICB9XG5cbiAgbmV4dCgpO1xufSIsIi8qZXNsaW50LWRpc2FibGUgbm8tZW1wdHkgKi9cbid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuc2F2ZVN0YXRlID0gc2F2ZVN0YXRlO1xuZXhwb3J0cy5yZWFkU3RhdGUgPSByZWFkU3RhdGU7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBLZXlQcmVmaXggPSAnQEBIaXN0b3J5Lyc7XG52YXIgUXVvdGFFeGNlZWRlZEVycm9ycyA9IFsnUXVvdGFFeGNlZWRlZEVycm9yJywgJ1FVT1RBX0VYQ0VFREVEX0VSUiddO1xuXG52YXIgU2VjdXJpdHlFcnJvciA9ICdTZWN1cml0eUVycm9yJztcblxuZnVuY3Rpb24gY3JlYXRlS2V5KGtleSkge1xuICByZXR1cm4gS2V5UHJlZml4ICsga2V5O1xufVxuXG5mdW5jdGlvbiBzYXZlU3RhdGUoa2V5LCBzdGF0ZSkge1xuICB0cnkge1xuICAgIGlmIChzdGF0ZSA9PSBudWxsKSB7XG4gICAgICB3aW5kb3cuc2Vzc2lvblN0b3JhZ2UucmVtb3ZlSXRlbShjcmVhdGVLZXkoa2V5KSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHdpbmRvdy5zZXNzaW9uU3RvcmFnZS5zZXRJdGVtKGNyZWF0ZUtleShrZXkpLCBKU09OLnN0cmluZ2lmeShzdGF0ZSkpO1xuICAgIH1cbiAgfSBjYXRjaCAoZXJyb3IpIHtcbiAgICBpZiAoZXJyb3IubmFtZSA9PT0gU2VjdXJpdHlFcnJvcikge1xuICAgICAgLy8gQmxvY2tpbmcgY29va2llcyBpbiBDaHJvbWUvRmlyZWZveC9TYWZhcmkgdGhyb3dzIFNlY3VyaXR5RXJyb3Igb24gYW55XG4gICAgICAvLyBhdHRlbXB0IHRvIGFjY2VzcyB3aW5kb3cuc2Vzc2lvblN0b3JhZ2UuXG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdbaGlzdG9yeV0gVW5hYmxlIHRvIHNhdmUgc3RhdGU7IHNlc3Npb25TdG9yYWdlIGlzIG5vdCBhdmFpbGFibGUgZHVlIHRvIHNlY3VyaXR5IHNldHRpbmdzJykgOiB1bmRlZmluZWQ7XG5cbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBpZiAoUXVvdGFFeGNlZWRlZEVycm9ycy5pbmRleE9mKGVycm9yLm5hbWUpID49IDAgJiYgd2luZG93LnNlc3Npb25TdG9yYWdlLmxlbmd0aCA9PT0gMCkge1xuICAgICAgLy8gU2FmYXJpIFwicHJpdmF0ZSBtb2RlXCIgdGhyb3dzIFF1b3RhRXhjZWVkZWRFcnJvci5cbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ1toaXN0b3J5XSBVbmFibGUgdG8gc2F2ZSBzdGF0ZTsgc2Vzc2lvblN0b3JhZ2UgaXMgbm90IGF2YWlsYWJsZSBpbiBTYWZhcmkgcHJpdmF0ZSBtb2RlJykgOiB1bmRlZmluZWQ7XG5cbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICB0aHJvdyBlcnJvcjtcbiAgfVxufVxuXG5mdW5jdGlvbiByZWFkU3RhdGUoa2V5KSB7XG4gIHZhciBqc29uID0gdW5kZWZpbmVkO1xuICB0cnkge1xuICAgIGpzb24gPSB3aW5kb3cuc2Vzc2lvblN0b3JhZ2UuZ2V0SXRlbShjcmVhdGVLZXkoa2V5KSk7XG4gIH0gY2F0Y2ggKGVycm9yKSB7XG4gICAgaWYgKGVycm9yLm5hbWUgPT09IFNlY3VyaXR5RXJyb3IpIHtcbiAgICAgIC8vIEJsb2NraW5nIGNvb2tpZXMgaW4gQ2hyb21lL0ZpcmVmb3gvU2FmYXJpIHRocm93cyBTZWN1cml0eUVycm9yIG9uIGFueVxuICAgICAgLy8gYXR0ZW1wdCB0byBhY2Nlc3Mgd2luZG93LnNlc3Npb25TdG9yYWdlLlxuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnW2hpc3RvcnldIFVuYWJsZSB0byByZWFkIHN0YXRlOyBzZXNzaW9uU3RvcmFnZSBpcyBub3QgYXZhaWxhYmxlIGR1ZSB0byBzZWN1cml0eSBzZXR0aW5ncycpIDogdW5kZWZpbmVkO1xuXG4gICAgICByZXR1cm4gbnVsbDtcbiAgICB9XG4gIH1cblxuICBpZiAoanNvbikge1xuICAgIHRyeSB7XG4gICAgICByZXR1cm4gSlNPTi5wYXJzZShqc29uKTtcbiAgICB9IGNhdGNoIChlcnJvcikge1xuICAgICAgLy8gSWdub3JlIGludmFsaWQgSlNPTi5cbiAgICB9XG4gIH1cblxuICByZXR1cm4gbnVsbDtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmFkZEV2ZW50TGlzdGVuZXIgPSBhZGRFdmVudExpc3RlbmVyO1xuZXhwb3J0cy5yZW1vdmVFdmVudExpc3RlbmVyID0gcmVtb3ZlRXZlbnRMaXN0ZW5lcjtcbmV4cG9ydHMuZ2V0SGFzaFBhdGggPSBnZXRIYXNoUGF0aDtcbmV4cG9ydHMucmVwbGFjZUhhc2hQYXRoID0gcmVwbGFjZUhhc2hQYXRoO1xuZXhwb3J0cy5nZXRXaW5kb3dQYXRoID0gZ2V0V2luZG93UGF0aDtcbmV4cG9ydHMuZ28gPSBnbztcbmV4cG9ydHMuZ2V0VXNlckNvbmZpcm1hdGlvbiA9IGdldFVzZXJDb25maXJtYXRpb247XG5leHBvcnRzLnN1cHBvcnRzSGlzdG9yeSA9IHN1cHBvcnRzSGlzdG9yeTtcbmV4cG9ydHMuc3VwcG9ydHNHb1dpdGhvdXRSZWxvYWRVc2luZ0hhc2ggPSBzdXBwb3J0c0dvV2l0aG91dFJlbG9hZFVzaW5nSGFzaDtcblxuZnVuY3Rpb24gYWRkRXZlbnRMaXN0ZW5lcihub2RlLCBldmVudCwgbGlzdGVuZXIpIHtcbiAgaWYgKG5vZGUuYWRkRXZlbnRMaXN0ZW5lcikge1xuICAgIG5vZGUuYWRkRXZlbnRMaXN0ZW5lcihldmVudCwgbGlzdGVuZXIsIGZhbHNlKTtcbiAgfSBlbHNlIHtcbiAgICBub2RlLmF0dGFjaEV2ZW50KCdvbicgKyBldmVudCwgbGlzdGVuZXIpO1xuICB9XG59XG5cbmZ1bmN0aW9uIHJlbW92ZUV2ZW50TGlzdGVuZXIobm9kZSwgZXZlbnQsIGxpc3RlbmVyKSB7XG4gIGlmIChub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIpIHtcbiAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIoZXZlbnQsIGxpc3RlbmVyLCBmYWxzZSk7XG4gIH0gZWxzZSB7XG4gICAgbm9kZS5kZXRhY2hFdmVudCgnb24nICsgZXZlbnQsIGxpc3RlbmVyKTtcbiAgfVxufVxuXG5mdW5jdGlvbiBnZXRIYXNoUGF0aCgpIHtcbiAgLy8gV2UgY2FuJ3QgdXNlIHdpbmRvdy5sb2NhdGlvbi5oYXNoIGhlcmUgYmVjYXVzZSBpdCdzIG5vdFxuICAvLyBjb25zaXN0ZW50IGFjcm9zcyBicm93c2VycyAtIEZpcmVmb3ggd2lsbCBwcmUtZGVjb2RlIGl0IVxuICByZXR1cm4gd2luZG93LmxvY2F0aW9uLmhyZWYuc3BsaXQoJyMnKVsxXSB8fCAnJztcbn1cblxuZnVuY3Rpb24gcmVwbGFjZUhhc2hQYXRoKHBhdGgpIHtcbiAgd2luZG93LmxvY2F0aW9uLnJlcGxhY2Uod2luZG93LmxvY2F0aW9uLnBhdGhuYW1lICsgd2luZG93LmxvY2F0aW9uLnNlYXJjaCArICcjJyArIHBhdGgpO1xufVxuXG5mdW5jdGlvbiBnZXRXaW5kb3dQYXRoKCkge1xuICByZXR1cm4gd2luZG93LmxvY2F0aW9uLnBhdGhuYW1lICsgd2luZG93LmxvY2F0aW9uLnNlYXJjaCArIHdpbmRvdy5sb2NhdGlvbi5oYXNoO1xufVxuXG5mdW5jdGlvbiBnbyhuKSB7XG4gIGlmIChuKSB3aW5kb3cuaGlzdG9yeS5nbyhuKTtcbn1cblxuZnVuY3Rpb24gZ2V0VXNlckNvbmZpcm1hdGlvbihtZXNzYWdlLCBjYWxsYmFjaykge1xuICBjYWxsYmFjayh3aW5kb3cuY29uZmlybShtZXNzYWdlKSk7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIHRoZSBIVE1MNSBoaXN0b3J5IEFQSSBpcyBzdXBwb3J0ZWQuIFRha2VuIGZyb20gTW9kZXJuaXpyLlxuICpcbiAqIGh0dHBzOi8vZ2l0aHViLmNvbS9Nb2Rlcm5penIvTW9kZXJuaXpyL2Jsb2IvbWFzdGVyL0xJQ0VOU0VcbiAqIGh0dHBzOi8vZ2l0aHViLmNvbS9Nb2Rlcm5penIvTW9kZXJuaXpyL2Jsb2IvbWFzdGVyL2ZlYXR1cmUtZGV0ZWN0cy9oaXN0b3J5LmpzXG4gKiBjaGFuZ2VkIHRvIGF2b2lkIGZhbHNlIG5lZ2F0aXZlcyBmb3IgV2luZG93cyBQaG9uZXM6IGh0dHBzOi8vZ2l0aHViLmNvbS9yYWNrdC9yZWFjdC1yb3V0ZXIvaXNzdWVzLzU4NlxuICovXG5cbmZ1bmN0aW9uIHN1cHBvcnRzSGlzdG9yeSgpIHtcbiAgdmFyIHVhID0gbmF2aWdhdG9yLnVzZXJBZ2VudDtcbiAgaWYgKCh1YS5pbmRleE9mKCdBbmRyb2lkIDIuJykgIT09IC0xIHx8IHVhLmluZGV4T2YoJ0FuZHJvaWQgNC4wJykgIT09IC0xKSAmJiB1YS5pbmRleE9mKCdNb2JpbGUgU2FmYXJpJykgIT09IC0xICYmIHVhLmluZGV4T2YoJ0Nocm9tZScpID09PSAtMSAmJiB1YS5pbmRleE9mKCdXaW5kb3dzIFBob25lJykgPT09IC0xKSB7XG4gICAgcmV0dXJuIGZhbHNlO1xuICB9XG4gIHJldHVybiB3aW5kb3cuaGlzdG9yeSAmJiAncHVzaFN0YXRlJyBpbiB3aW5kb3cuaGlzdG9yeTtcbn1cblxuLyoqXG4gKiBSZXR1cm5zIGZhbHNlIGlmIHVzaW5nIGdvKG4pIHdpdGggaGFzaCBoaXN0b3J5IGNhdXNlcyBhIGZ1bGwgcGFnZSByZWxvYWQuXG4gKi9cblxuZnVuY3Rpb24gc3VwcG9ydHNHb1dpdGhvdXRSZWxvYWRVc2luZ0hhc2goKSB7XG4gIHZhciB1YSA9IG5hdmlnYXRvci51c2VyQWdlbnQ7XG4gIHJldHVybiB1YS5pbmRleE9mKCdGaXJlZm94JykgPT09IC0xO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbnZhciBjYW5Vc2VET00gPSAhISh0eXBlb2Ygd2luZG93ICE9PSAndW5kZWZpbmVkJyAmJiB3aW5kb3cuZG9jdW1lbnQgJiYgd2luZG93LmRvY3VtZW50LmNyZWF0ZUVsZW1lbnQpO1xuZXhwb3J0cy5jYW5Vc2VET00gPSBjYW5Vc2VET007IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5leHRyYWN0UGF0aCA9IGV4dHJhY3RQYXRoO1xuZXhwb3J0cy5wYXJzZVBhdGggPSBwYXJzZVBhdGg7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbmZ1bmN0aW9uIGV4dHJhY3RQYXRoKHN0cmluZykge1xuICB2YXIgbWF0Y2ggPSBzdHJpbmcubWF0Y2goL15odHRwcz86XFwvXFwvW15cXC9dKi8pO1xuXG4gIGlmIChtYXRjaCA9PSBudWxsKSByZXR1cm4gc3RyaW5nO1xuXG4gIHJldHVybiBzdHJpbmcuc3Vic3RyaW5nKG1hdGNoWzBdLmxlbmd0aCk7XG59XG5cbmZ1bmN0aW9uIHBhcnNlUGF0aChwYXRoKSB7XG4gIHZhciBwYXRobmFtZSA9IGV4dHJhY3RQYXRoKHBhdGgpO1xuICB2YXIgc2VhcmNoID0gJyc7XG4gIHZhciBoYXNoID0gJyc7XG5cbiAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKHBhdGggPT09IHBhdGhuYW1lLCAnQSBwYXRoIG11c3QgYmUgcGF0aG5hbWUgKyBzZWFyY2ggKyBoYXNoIG9ubHksIG5vdCBhIGZ1bGx5IHF1YWxpZmllZCBVUkwgbGlrZSBcIiVzXCInLCBwYXRoKSA6IHVuZGVmaW5lZDtcblxuICB2YXIgaGFzaEluZGV4ID0gcGF0aG5hbWUuaW5kZXhPZignIycpO1xuICBpZiAoaGFzaEluZGV4ICE9PSAtMSkge1xuICAgIGhhc2ggPSBwYXRobmFtZS5zdWJzdHJpbmcoaGFzaEluZGV4KTtcbiAgICBwYXRobmFtZSA9IHBhdGhuYW1lLnN1YnN0cmluZygwLCBoYXNoSW5kZXgpO1xuICB9XG5cbiAgdmFyIHNlYXJjaEluZGV4ID0gcGF0aG5hbWUuaW5kZXhPZignPycpO1xuICBpZiAoc2VhcmNoSW5kZXggIT09IC0xKSB7XG4gICAgc2VhcmNoID0gcGF0aG5hbWUuc3Vic3RyaW5nKHNlYXJjaEluZGV4KTtcbiAgICBwYXRobmFtZSA9IHBhdGhuYW1lLnN1YnN0cmluZygwLCBzZWFyY2hJbmRleCk7XG4gIH1cblxuICBpZiAocGF0aG5hbWUgPT09ICcnKSBwYXRobmFtZSA9ICcvJztcblxuICByZXR1cm4ge1xuICAgIHBhdGhuYW1lOiBwYXRobmFtZSxcbiAgICBzZWFyY2g6IHNlYXJjaCxcbiAgICBoYXNoOiBoYXNoXG4gIH07XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfQWN0aW9ucyA9IHJlcXVpcmUoJy4vQWN0aW9ucycpO1xuXG52YXIgX1BhdGhVdGlscyA9IHJlcXVpcmUoJy4vUGF0aFV0aWxzJyk7XG5cbnZhciBfRXhlY3V0aW9uRW52aXJvbm1lbnQgPSByZXF1aXJlKCcuL0V4ZWN1dGlvbkVudmlyb25tZW50Jyk7XG5cbnZhciBfRE9NVXRpbHMgPSByZXF1aXJlKCcuL0RPTVV0aWxzJyk7XG5cbnZhciBfRE9NU3RhdGVTdG9yYWdlID0gcmVxdWlyZSgnLi9ET01TdGF0ZVN0b3JhZ2UnKTtcblxudmFyIF9jcmVhdGVET01IaXN0b3J5ID0gcmVxdWlyZSgnLi9jcmVhdGVET01IaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlRE9NSGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVET01IaXN0b3J5KTtcblxuLyoqXG4gKiBDcmVhdGVzIGFuZCByZXR1cm5zIGEgaGlzdG9yeSBvYmplY3QgdGhhdCB1c2VzIEhUTUw1J3MgaGlzdG9yeSBBUElcbiAqIChwdXNoU3RhdGUsIHJlcGxhY2VTdGF0ZSwgYW5kIHRoZSBwb3BzdGF0ZSBldmVudCkgdG8gbWFuYWdlIGhpc3RvcnkuXG4gKiBUaGlzIGlzIHRoZSByZWNvbW1lbmRlZCBtZXRob2Qgb2YgbWFuYWdpbmcgaGlzdG9yeSBpbiBicm93c2VycyBiZWNhdXNlXG4gKiBpdCBwcm92aWRlcyB0aGUgY2xlYW5lc3QgVVJMcy5cbiAqXG4gKiBOb3RlOiBJbiBicm93c2VycyB0aGF0IGRvIG5vdCBzdXBwb3J0IHRoZSBIVE1MNSBoaXN0b3J5IEFQSSBmdWxsXG4gKiBwYWdlIHJlbG9hZHMgd2lsbCBiZSB1c2VkIHRvIHByZXNlcnZlIFVSTHMuXG4gKi9cbmZ1bmN0aW9uIGNyZWF0ZUJyb3dzZXJIaXN0b3J5KCkge1xuICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuXG4gICFfRXhlY3V0aW9uRW52aXJvbm1lbnQuY2FuVXNlRE9NID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UsICdCcm93c2VyIGhpc3RvcnkgbmVlZHMgYSBET00nKSA6IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UpIDogdW5kZWZpbmVkO1xuXG4gIHZhciBmb3JjZVJlZnJlc2ggPSBvcHRpb25zLmZvcmNlUmVmcmVzaDtcblxuICB2YXIgaXNTdXBwb3J0ZWQgPSBfRE9NVXRpbHMuc3VwcG9ydHNIaXN0b3J5KCk7XG4gIHZhciB1c2VSZWZyZXNoID0gIWlzU3VwcG9ydGVkIHx8IGZvcmNlUmVmcmVzaDtcblxuICBmdW5jdGlvbiBnZXRDdXJyZW50TG9jYXRpb24oaGlzdG9yeVN0YXRlKSB7XG4gICAgdHJ5IHtcbiAgICAgIGhpc3RvcnlTdGF0ZSA9IGhpc3RvcnlTdGF0ZSB8fCB3aW5kb3cuaGlzdG9yeS5zdGF0ZSB8fCB7fTtcbiAgICB9IGNhdGNoIChlKSB7XG4gICAgICBoaXN0b3J5U3RhdGUgPSB7fTtcbiAgICB9XG5cbiAgICB2YXIgcGF0aCA9IF9ET01VdGlscy5nZXRXaW5kb3dQYXRoKCk7XG4gICAgdmFyIF9oaXN0b3J5U3RhdGUgPSBoaXN0b3J5U3RhdGU7XG4gICAgdmFyIGtleSA9IF9oaXN0b3J5U3RhdGUua2V5O1xuXG4gICAgdmFyIHN0YXRlID0gdW5kZWZpbmVkO1xuICAgIGlmIChrZXkpIHtcbiAgICAgIHN0YXRlID0gX0RPTVN0YXRlU3RvcmFnZS5yZWFkU3RhdGUoa2V5KTtcbiAgICB9IGVsc2Uge1xuICAgICAgc3RhdGUgPSBudWxsO1xuICAgICAga2V5ID0gaGlzdG9yeS5jcmVhdGVLZXkoKTtcblxuICAgICAgaWYgKGlzU3VwcG9ydGVkKSB3aW5kb3cuaGlzdG9yeS5yZXBsYWNlU3RhdGUoX2V4dGVuZHMoe30sIGhpc3RvcnlTdGF0ZSwgeyBrZXk6IGtleSB9KSwgbnVsbCk7XG4gICAgfVxuXG4gICAgdmFyIGxvY2F0aW9uID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbihfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIHsgc3RhdGU6IHN0YXRlIH0pLCB1bmRlZmluZWQsIGtleSk7XG4gIH1cblxuICBmdW5jdGlvbiBzdGFydFBvcFN0YXRlTGlzdGVuZXIoX3JlZikge1xuICAgIHZhciB0cmFuc2l0aW9uVG8gPSBfcmVmLnRyYW5zaXRpb25UbztcblxuICAgIGZ1bmN0aW9uIHBvcFN0YXRlTGlzdGVuZXIoZXZlbnQpIHtcbiAgICAgIGlmIChldmVudC5zdGF0ZSA9PT0gdW5kZWZpbmVkKSByZXR1cm47IC8vIElnbm9yZSBleHRyYW5lb3VzIHBvcHN0YXRlIGV2ZW50cyBpbiBXZWJLaXQuXG5cbiAgICAgIHRyYW5zaXRpb25UbyhnZXRDdXJyZW50TG9jYXRpb24oZXZlbnQuc3RhdGUpKTtcbiAgICB9XG5cbiAgICBfRE9NVXRpbHMuYWRkRXZlbnRMaXN0ZW5lcih3aW5kb3csICdwb3BzdGF0ZScsIHBvcFN0YXRlTGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIF9ET01VdGlscy5yZW1vdmVFdmVudExpc3RlbmVyKHdpbmRvdywgJ3BvcHN0YXRlJywgcG9wU3RhdGVMaXN0ZW5lcik7XG4gICAgfTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGZpbmlzaFRyYW5zaXRpb24obG9jYXRpb24pIHtcbiAgICB2YXIgYmFzZW5hbWUgPSBsb2NhdGlvbi5iYXNlbmFtZTtcbiAgICB2YXIgcGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZTtcbiAgICB2YXIgc2VhcmNoID0gbG9jYXRpb24uc2VhcmNoO1xuICAgIHZhciBoYXNoID0gbG9jYXRpb24uaGFzaDtcbiAgICB2YXIgc3RhdGUgPSBsb2NhdGlvbi5zdGF0ZTtcbiAgICB2YXIgYWN0aW9uID0gbG9jYXRpb24uYWN0aW9uO1xuICAgIHZhciBrZXkgPSBsb2NhdGlvbi5rZXk7XG5cbiAgICBpZiAoYWN0aW9uID09PSBfQWN0aW9ucy5QT1ApIHJldHVybjsgLy8gTm90aGluZyB0byBkby5cblxuICAgIF9ET01TdGF0ZVN0b3JhZ2Uuc2F2ZVN0YXRlKGtleSwgc3RhdGUpO1xuXG4gICAgdmFyIHBhdGggPSAoYmFzZW5hbWUgfHwgJycpICsgcGF0aG5hbWUgKyBzZWFyY2ggKyBoYXNoO1xuICAgIHZhciBoaXN0b3J5U3RhdGUgPSB7XG4gICAgICBrZXk6IGtleVxuICAgIH07XG5cbiAgICBpZiAoYWN0aW9uID09PSBfQWN0aW9ucy5QVVNIKSB7XG4gICAgICBpZiAodXNlUmVmcmVzaCkge1xuICAgICAgICB3aW5kb3cubG9jYXRpb24uaHJlZiA9IHBhdGg7XG4gICAgICAgIHJldHVybiBmYWxzZTsgLy8gUHJldmVudCBsb2NhdGlvbiB1cGRhdGUuXG4gICAgICB9IGVsc2Uge1xuICAgICAgICAgIHdpbmRvdy5oaXN0b3J5LnB1c2hTdGF0ZShoaXN0b3J5U3RhdGUsIG51bGwsIHBhdGgpO1xuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgIC8vIFJFUExBQ0VcbiAgICAgIGlmICh1c2VSZWZyZXNoKSB7XG4gICAgICAgIHdpbmRvdy5sb2NhdGlvbi5yZXBsYWNlKHBhdGgpO1xuICAgICAgICByZXR1cm4gZmFsc2U7IC8vIFByZXZlbnQgbG9jYXRpb24gdXBkYXRlLlxuICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB3aW5kb3cuaGlzdG9yeS5yZXBsYWNlU3RhdGUoaGlzdG9yeVN0YXRlLCBudWxsLCBwYXRoKTtcbiAgICAgICAgfVxuICAgIH1cbiAgfVxuXG4gIHZhciBoaXN0b3J5ID0gX2NyZWF0ZURPTUhpc3RvcnkyWydkZWZhdWx0J10oX2V4dGVuZHMoe30sIG9wdGlvbnMsIHtcbiAgICBnZXRDdXJyZW50TG9jYXRpb246IGdldEN1cnJlbnRMb2NhdGlvbixcbiAgICBmaW5pc2hUcmFuc2l0aW9uOiBmaW5pc2hUcmFuc2l0aW9uLFxuICAgIHNhdmVTdGF0ZTogX0RPTVN0YXRlU3RvcmFnZS5zYXZlU3RhdGVcbiAgfSkpO1xuXG4gIHZhciBsaXN0ZW5lckNvdW50ID0gMCxcbiAgICAgIHN0b3BQb3BTdGF0ZUxpc3RlbmVyID0gdW5kZWZpbmVkO1xuXG4gIGZ1bmN0aW9uIGxpc3RlbkJlZm9yZShsaXN0ZW5lcikge1xuICAgIGlmICgrK2xpc3RlbmVyQ291bnQgPT09IDEpIHN0b3BQb3BTdGF0ZUxpc3RlbmVyID0gc3RhcnRQb3BTdGF0ZUxpc3RlbmVyKGhpc3RvcnkpO1xuXG4gICAgdmFyIHVubGlzdGVuID0gaGlzdG9yeS5saXN0ZW5CZWZvcmUobGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIHVubGlzdGVuKCk7XG5cbiAgICAgIGlmICgtLWxpc3RlbmVyQ291bnQgPT09IDApIHN0b3BQb3BTdGF0ZUxpc3RlbmVyKCk7XG4gICAgfTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGxpc3RlbihsaXN0ZW5lcikge1xuICAgIGlmICgrK2xpc3RlbmVyQ291bnQgPT09IDEpIHN0b3BQb3BTdGF0ZUxpc3RlbmVyID0gc3RhcnRQb3BTdGF0ZUxpc3RlbmVyKGhpc3RvcnkpO1xuXG4gICAgdmFyIHVubGlzdGVuID0gaGlzdG9yeS5saXN0ZW4obGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIHVubGlzdGVuKCk7XG5cbiAgICAgIGlmICgtLWxpc3RlbmVyQ291bnQgPT09IDApIHN0b3BQb3BTdGF0ZUxpc3RlbmVyKCk7XG4gICAgfTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gcmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKSB7XG4gICAgaWYgKCsrbGlzdGVuZXJDb3VudCA9PT0gMSkgc3RvcFBvcFN0YXRlTGlzdGVuZXIgPSBzdGFydFBvcFN0YXRlTGlzdGVuZXIoaGlzdG9yeSk7XG5cbiAgICBoaXN0b3J5LnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vayk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKSB7XG4gICAgaGlzdG9yeS51bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vayk7XG5cbiAgICBpZiAoLS1saXN0ZW5lckNvdW50ID09PSAwKSBzdG9wUG9wU3RhdGVMaXN0ZW5lcigpO1xuICB9XG5cbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB7XG4gICAgbGlzdGVuQmVmb3JlOiBsaXN0ZW5CZWZvcmUsXG4gICAgbGlzdGVuOiBsaXN0ZW4sXG4gICAgcmVnaXN0ZXJUcmFuc2l0aW9uSG9vazogcmVnaXN0ZXJUcmFuc2l0aW9uSG9vayxcbiAgICB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2s6IHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9va1xuICB9KTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gY3JlYXRlQnJvd3Nlckhpc3Rvcnk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9FeGVjdXRpb25FbnZpcm9ubWVudCA9IHJlcXVpcmUoJy4vRXhlY3V0aW9uRW52aXJvbm1lbnQnKTtcblxudmFyIF9ET01VdGlscyA9IHJlcXVpcmUoJy4vRE9NVXRpbHMnKTtcblxudmFyIF9jcmVhdGVIaXN0b3J5ID0gcmVxdWlyZSgnLi9jcmVhdGVIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlSGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVIaXN0b3J5KTtcblxuZnVuY3Rpb24gY3JlYXRlRE9NSGlzdG9yeShvcHRpb25zKSB7XG4gIHZhciBoaXN0b3J5ID0gX2NyZWF0ZUhpc3RvcnkyWydkZWZhdWx0J10oX2V4dGVuZHMoe1xuICAgIGdldFVzZXJDb25maXJtYXRpb246IF9ET01VdGlscy5nZXRVc2VyQ29uZmlybWF0aW9uXG4gIH0sIG9wdGlvbnMsIHtcbiAgICBnbzogX0RPTVV0aWxzLmdvXG4gIH0pKTtcblxuICBmdW5jdGlvbiBsaXN0ZW4obGlzdGVuZXIpIHtcbiAgICAhX0V4ZWN1dGlvbkVudmlyb25tZW50LmNhblVzZURPTSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlLCAnRE9NIGhpc3RvcnkgbmVlZHMgYSBET00nKSA6IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UpIDogdW5kZWZpbmVkO1xuXG4gICAgcmV0dXJuIGhpc3RvcnkubGlzdGVuKGxpc3RlbmVyKTtcbiAgfVxuXG4gIHJldHVybiBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwge1xuICAgIGxpc3RlbjogbGlzdGVuXG4gIH0pO1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBjcmVhdGVET01IaXN0b3J5O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX0FjdGlvbnMgPSByZXF1aXJlKCcuL0FjdGlvbnMnKTtcblxudmFyIF9QYXRoVXRpbHMgPSByZXF1aXJlKCcuL1BhdGhVdGlscycpO1xuXG52YXIgX0V4ZWN1dGlvbkVudmlyb25tZW50ID0gcmVxdWlyZSgnLi9FeGVjdXRpb25FbnZpcm9ubWVudCcpO1xuXG52YXIgX0RPTVV0aWxzID0gcmVxdWlyZSgnLi9ET01VdGlscycpO1xuXG52YXIgX0RPTVN0YXRlU3RvcmFnZSA9IHJlcXVpcmUoJy4vRE9NU3RhdGVTdG9yYWdlJyk7XG5cbnZhciBfY3JlYXRlRE9NSGlzdG9yeSA9IHJlcXVpcmUoJy4vY3JlYXRlRE9NSGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZURPTUhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlRE9NSGlzdG9yeSk7XG5cbmZ1bmN0aW9uIGlzQWJzb2x1dGVQYXRoKHBhdGgpIHtcbiAgcmV0dXJuIHR5cGVvZiBwYXRoID09PSAnc3RyaW5nJyAmJiBwYXRoLmNoYXJBdCgwKSA9PT0gJy8nO1xufVxuXG5mdW5jdGlvbiBlbnN1cmVTbGFzaCgpIHtcbiAgdmFyIHBhdGggPSBfRE9NVXRpbHMuZ2V0SGFzaFBhdGgoKTtcblxuICBpZiAoaXNBYnNvbHV0ZVBhdGgocGF0aCkpIHJldHVybiB0cnVlO1xuXG4gIF9ET01VdGlscy5yZXBsYWNlSGFzaFBhdGgoJy8nICsgcGF0aCk7XG5cbiAgcmV0dXJuIGZhbHNlO1xufVxuXG5mdW5jdGlvbiBhZGRRdWVyeVN0cmluZ1ZhbHVlVG9QYXRoKHBhdGgsIGtleSwgdmFsdWUpIHtcbiAgcmV0dXJuIHBhdGggKyAocGF0aC5pbmRleE9mKCc/JykgPT09IC0xID8gJz8nIDogJyYnKSArIChrZXkgKyAnPScgKyB2YWx1ZSk7XG59XG5cbmZ1bmN0aW9uIHN0cmlwUXVlcnlTdHJpbmdWYWx1ZUZyb21QYXRoKHBhdGgsIGtleSkge1xuICByZXR1cm4gcGF0aC5yZXBsYWNlKG5ldyBSZWdFeHAoJ1s/Jl0/JyArIGtleSArICc9W2EtekEtWjAtOV0rJyksICcnKTtcbn1cblxuZnVuY3Rpb24gZ2V0UXVlcnlTdHJpbmdWYWx1ZUZyb21QYXRoKHBhdGgsIGtleSkge1xuICB2YXIgbWF0Y2ggPSBwYXRoLm1hdGNoKG5ldyBSZWdFeHAoJ1xcXFw/Lio/XFxcXGInICsga2V5ICsgJz0oLis/KVxcXFxiJykpO1xuICByZXR1cm4gbWF0Y2ggJiYgbWF0Y2hbMV07XG59XG5cbnZhciBEZWZhdWx0UXVlcnlLZXkgPSAnX2snO1xuXG5mdW5jdGlvbiBjcmVhdGVIYXNoSGlzdG9yeSgpIHtcbiAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcblxuICAhX0V4ZWN1dGlvbkVudmlyb25tZW50LmNhblVzZURPTSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlLCAnSGFzaCBoaXN0b3J5IG5lZWRzIGEgRE9NJykgOiBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlKSA6IHVuZGVmaW5lZDtcblxuICB2YXIgcXVlcnlLZXkgPSBvcHRpb25zLnF1ZXJ5S2V5O1xuXG4gIGlmIChxdWVyeUtleSA9PT0gdW5kZWZpbmVkIHx8ICEhcXVlcnlLZXkpIHF1ZXJ5S2V5ID0gdHlwZW9mIHF1ZXJ5S2V5ID09PSAnc3RyaW5nJyA/IHF1ZXJ5S2V5IDogRGVmYXVsdFF1ZXJ5S2V5O1xuXG4gIGZ1bmN0aW9uIGdldEN1cnJlbnRMb2NhdGlvbigpIHtcbiAgICB2YXIgcGF0aCA9IF9ET01VdGlscy5nZXRIYXNoUGF0aCgpO1xuXG4gICAgdmFyIGtleSA9IHVuZGVmaW5lZCxcbiAgICAgICAgc3RhdGUgPSB1bmRlZmluZWQ7XG4gICAgaWYgKHF1ZXJ5S2V5KSB7XG4gICAgICBrZXkgPSBnZXRRdWVyeVN0cmluZ1ZhbHVlRnJvbVBhdGgocGF0aCwgcXVlcnlLZXkpO1xuICAgICAgcGF0aCA9IHN0cmlwUXVlcnlTdHJpbmdWYWx1ZUZyb21QYXRoKHBhdGgsIHF1ZXJ5S2V5KTtcblxuICAgICAgaWYgKGtleSkge1xuICAgICAgICBzdGF0ZSA9IF9ET01TdGF0ZVN0b3JhZ2UucmVhZFN0YXRlKGtleSk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBzdGF0ZSA9IG51bGw7XG4gICAgICAgIGtleSA9IGhpc3RvcnkuY3JlYXRlS2V5KCk7XG4gICAgICAgIF9ET01VdGlscy5yZXBsYWNlSGFzaFBhdGgoYWRkUXVlcnlTdHJpbmdWYWx1ZVRvUGF0aChwYXRoLCBxdWVyeUtleSwga2V5KSk7XG4gICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgIGtleSA9IHN0YXRlID0gbnVsbDtcbiAgICB9XG5cbiAgICB2YXIgbG9jYXRpb24gPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uKF9leHRlbmRzKHt9LCBsb2NhdGlvbiwgeyBzdGF0ZTogc3RhdGUgfSksIHVuZGVmaW5lZCwga2V5KTtcbiAgfVxuXG4gIGZ1bmN0aW9uIHN0YXJ0SGFzaENoYW5nZUxpc3RlbmVyKF9yZWYpIHtcbiAgICB2YXIgdHJhbnNpdGlvblRvID0gX3JlZi50cmFuc2l0aW9uVG87XG5cbiAgICBmdW5jdGlvbiBoYXNoQ2hhbmdlTGlzdGVuZXIoKSB7XG4gICAgICBpZiAoIWVuc3VyZVNsYXNoKCkpIHJldHVybjsgLy8gQWx3YXlzIG1ha2Ugc3VyZSBoYXNoZXMgYXJlIHByZWNlZWRlZCB3aXRoIGEgLy5cblxuICAgICAgdHJhbnNpdGlvblRvKGdldEN1cnJlbnRMb2NhdGlvbigpKTtcbiAgICB9XG5cbiAgICBlbnN1cmVTbGFzaCgpO1xuICAgIF9ET01VdGlscy5hZGRFdmVudExpc3RlbmVyKHdpbmRvdywgJ2hhc2hjaGFuZ2UnLCBoYXNoQ2hhbmdlTGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIF9ET01VdGlscy5yZW1vdmVFdmVudExpc3RlbmVyKHdpbmRvdywgJ2hhc2hjaGFuZ2UnLCBoYXNoQ2hhbmdlTGlzdGVuZXIpO1xuICAgIH07XG4gIH1cblxuICBmdW5jdGlvbiBmaW5pc2hUcmFuc2l0aW9uKGxvY2F0aW9uKSB7XG4gICAgdmFyIGJhc2VuYW1lID0gbG9jYXRpb24uYmFzZW5hbWU7XG4gICAgdmFyIHBhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gICAgdmFyIHNlYXJjaCA9IGxvY2F0aW9uLnNlYXJjaDtcbiAgICB2YXIgc3RhdGUgPSBsb2NhdGlvbi5zdGF0ZTtcbiAgICB2YXIgYWN0aW9uID0gbG9jYXRpb24uYWN0aW9uO1xuICAgIHZhciBrZXkgPSBsb2NhdGlvbi5rZXk7XG5cbiAgICBpZiAoYWN0aW9uID09PSBfQWN0aW9ucy5QT1ApIHJldHVybjsgLy8gTm90aGluZyB0byBkby5cblxuICAgIHZhciBwYXRoID0gKGJhc2VuYW1lIHx8ICcnKSArIHBhdGhuYW1lICsgc2VhcmNoO1xuXG4gICAgaWYgKHF1ZXJ5S2V5KSB7XG4gICAgICBwYXRoID0gYWRkUXVlcnlTdHJpbmdWYWx1ZVRvUGF0aChwYXRoLCBxdWVyeUtleSwga2V5KTtcbiAgICAgIF9ET01TdGF0ZVN0b3JhZ2Uuc2F2ZVN0YXRlKGtleSwgc3RhdGUpO1xuICAgIH0gZWxzZSB7XG4gICAgICAvLyBEcm9wIGtleSBhbmQgc3RhdGUuXG4gICAgICBsb2NhdGlvbi5rZXkgPSBsb2NhdGlvbi5zdGF0ZSA9IG51bGw7XG4gICAgfVxuXG4gICAgdmFyIGN1cnJlbnRIYXNoID0gX0RPTVV0aWxzLmdldEhhc2hQYXRoKCk7XG5cbiAgICBpZiAoYWN0aW9uID09PSBfQWN0aW9ucy5QVVNIKSB7XG4gICAgICBpZiAoY3VycmVudEhhc2ggIT09IHBhdGgpIHtcbiAgICAgICAgd2luZG93LmxvY2F0aW9uLmhhc2ggPSBwYXRoO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnWW91IGNhbm5vdCBQVVNIIHRoZSBzYW1lIHBhdGggdXNpbmcgaGFzaCBoaXN0b3J5JykgOiB1bmRlZmluZWQ7XG4gICAgICB9XG4gICAgfSBlbHNlIGlmIChjdXJyZW50SGFzaCAhPT0gcGF0aCkge1xuICAgICAgLy8gUkVQTEFDRVxuICAgICAgX0RPTVV0aWxzLnJlcGxhY2VIYXNoUGF0aChwYXRoKTtcbiAgICB9XG4gIH1cblxuICB2YXIgaGlzdG9yeSA9IF9jcmVhdGVET01IaXN0b3J5MlsnZGVmYXVsdCddKF9leHRlbmRzKHt9LCBvcHRpb25zLCB7XG4gICAgZ2V0Q3VycmVudExvY2F0aW9uOiBnZXRDdXJyZW50TG9jYXRpb24sXG4gICAgZmluaXNoVHJhbnNpdGlvbjogZmluaXNoVHJhbnNpdGlvbixcbiAgICBzYXZlU3RhdGU6IF9ET01TdGF0ZVN0b3JhZ2Uuc2F2ZVN0YXRlXG4gIH0pKTtcblxuICB2YXIgbGlzdGVuZXJDb3VudCA9IDAsXG4gICAgICBzdG9wSGFzaENoYW5nZUxpc3RlbmVyID0gdW5kZWZpbmVkO1xuXG4gIGZ1bmN0aW9uIGxpc3RlbkJlZm9yZShsaXN0ZW5lcikge1xuICAgIGlmICgrK2xpc3RlbmVyQ291bnQgPT09IDEpIHN0b3BIYXNoQ2hhbmdlTGlzdGVuZXIgPSBzdGFydEhhc2hDaGFuZ2VMaXN0ZW5lcihoaXN0b3J5KTtcblxuICAgIHZhciB1bmxpc3RlbiA9IGhpc3RvcnkubGlzdGVuQmVmb3JlKGxpc3RlbmVyKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICB1bmxpc3RlbigpO1xuXG4gICAgICBpZiAoLS1saXN0ZW5lckNvdW50ID09PSAwKSBzdG9wSGFzaENoYW5nZUxpc3RlbmVyKCk7XG4gICAgfTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGxpc3RlbihsaXN0ZW5lcikge1xuICAgIGlmICgrK2xpc3RlbmVyQ291bnQgPT09IDEpIHN0b3BIYXNoQ2hhbmdlTGlzdGVuZXIgPSBzdGFydEhhc2hDaGFuZ2VMaXN0ZW5lcihoaXN0b3J5KTtcblxuICAgIHZhciB1bmxpc3RlbiA9IGhpc3RvcnkubGlzdGVuKGxpc3RlbmVyKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICB1bmxpc3RlbigpO1xuXG4gICAgICBpZiAoLS1saXN0ZW5lckNvdW50ID09PSAwKSBzdG9wSGFzaENoYW5nZUxpc3RlbmVyKCk7XG4gICAgfTtcbiAgfVxuXG4gIGZ1bmN0aW9uIHB1c2gobG9jYXRpb24pIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10ocXVlcnlLZXkgfHwgbG9jYXRpb24uc3RhdGUgPT0gbnVsbCwgJ1lvdSBjYW5ub3QgdXNlIHN0YXRlIHdpdGhvdXQgYSBxdWVyeUtleSBpdCB3aWxsIGJlIGRyb3BwZWQnKSA6IHVuZGVmaW5lZDtcblxuICAgIGhpc3RvcnkucHVzaChsb2NhdGlvbik7XG4gIH1cblxuICBmdW5jdGlvbiByZXBsYWNlKGxvY2F0aW9uKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKHF1ZXJ5S2V5IHx8IGxvY2F0aW9uLnN0YXRlID09IG51bGwsICdZb3UgY2Fubm90IHVzZSBzdGF0ZSB3aXRob3V0IGEgcXVlcnlLZXkgaXQgd2lsbCBiZSBkcm9wcGVkJykgOiB1bmRlZmluZWQ7XG5cbiAgICBoaXN0b3J5LnJlcGxhY2UobG9jYXRpb24pO1xuICB9XG5cbiAgdmFyIGdvSXNTdXBwb3J0ZWRXaXRob3V0UmVsb2FkID0gX0RPTVV0aWxzLnN1cHBvcnRzR29XaXRob3V0UmVsb2FkVXNpbmdIYXNoKCk7XG5cbiAgZnVuY3Rpb24gZ28obikge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShnb0lzU3VwcG9ydGVkV2l0aG91dFJlbG9hZCwgJ0hhc2ggaGlzdG9yeSBnbyhuKSBjYXVzZXMgYSBmdWxsIHBhZ2UgcmVsb2FkIGluIHRoaXMgYnJvd3NlcicpIDogdW5kZWZpbmVkO1xuXG4gICAgaGlzdG9yeS5nbyhuKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNyZWF0ZUhyZWYocGF0aCkge1xuICAgIHJldHVybiAnIycgKyBoaXN0b3J5LmNyZWF0ZUhyZWYocGF0aCk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vaykge1xuICAgIGlmICgrK2xpc3RlbmVyQ291bnQgPT09IDEpIHN0b3BIYXNoQ2hhbmdlTGlzdGVuZXIgPSBzdGFydEhhc2hDaGFuZ2VMaXN0ZW5lcihoaXN0b3J5KTtcblxuICAgIGhpc3RvcnkucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spIHtcbiAgICBoaXN0b3J5LnVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKTtcblxuICAgIGlmICgtLWxpc3RlbmVyQ291bnQgPT09IDApIHN0b3BIYXNoQ2hhbmdlTGlzdGVuZXIoKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gcHVzaFN0YXRlKHN0YXRlLCBwYXRoKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKHF1ZXJ5S2V5IHx8IHN0YXRlID09IG51bGwsICdZb3UgY2Fubm90IHVzZSBzdGF0ZSB3aXRob3V0IGEgcXVlcnlLZXkgaXQgd2lsbCBiZSBkcm9wcGVkJykgOiB1bmRlZmluZWQ7XG5cbiAgICBoaXN0b3J5LnB1c2hTdGF0ZShzdGF0ZSwgcGF0aCk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHJlcGxhY2VTdGF0ZShzdGF0ZSwgcGF0aCkge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShxdWVyeUtleSB8fCBzdGF0ZSA9PSBudWxsLCAnWW91IGNhbm5vdCB1c2Ugc3RhdGUgd2l0aG91dCBhIHF1ZXJ5S2V5IGl0IHdpbGwgYmUgZHJvcHBlZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgaGlzdG9yeS5yZXBsYWNlU3RhdGUoc3RhdGUsIHBhdGgpO1xuICB9XG5cbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB7XG4gICAgbGlzdGVuQmVmb3JlOiBsaXN0ZW5CZWZvcmUsXG4gICAgbGlzdGVuOiBsaXN0ZW4sXG4gICAgcHVzaDogcHVzaCxcbiAgICByZXBsYWNlOiByZXBsYWNlLFxuICAgIGdvOiBnbyxcbiAgICBjcmVhdGVIcmVmOiBjcmVhdGVIcmVmLFxuXG4gICAgcmVnaXN0ZXJUcmFuc2l0aW9uSG9vazogcmVnaXN0ZXJUcmFuc2l0aW9uSG9vaywgLy8gZGVwcmVjYXRlZCAtIHdhcm5pbmcgaXMgaW4gY3JlYXRlSGlzdG9yeVxuICAgIHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vazogdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rLCAvLyBkZXByZWNhdGVkIC0gd2FybmluZyBpcyBpbiBjcmVhdGVIaXN0b3J5XG4gICAgcHVzaFN0YXRlOiBwdXNoU3RhdGUsIC8vIGRlcHJlY2F0ZWQgLSB3YXJuaW5nIGlzIGluIGNyZWF0ZUhpc3RvcnlcbiAgICByZXBsYWNlU3RhdGU6IHJlcGxhY2VTdGF0ZSAvLyBkZXByZWNhdGVkIC0gd2FybmluZyBpcyBpbiBjcmVhdGVIaXN0b3J5XG4gIH0pO1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBjcmVhdGVIYXNoSGlzdG9yeTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIF9kZWVwRXF1YWwgPSByZXF1aXJlKCdkZWVwLWVxdWFsJyk7XG5cbnZhciBfZGVlcEVxdWFsMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2RlZXBFcXVhbCk7XG5cbnZhciBfUGF0aFV0aWxzID0gcmVxdWlyZSgnLi9QYXRoVXRpbHMnKTtcblxudmFyIF9Bc3luY1V0aWxzID0gcmVxdWlyZSgnLi9Bc3luY1V0aWxzJyk7XG5cbnZhciBfQWN0aW9ucyA9IHJlcXVpcmUoJy4vQWN0aW9ucycpO1xuXG52YXIgX2NyZWF0ZUxvY2F0aW9uMiA9IHJlcXVpcmUoJy4vY3JlYXRlTG9jYXRpb24nKTtcblxudmFyIF9jcmVhdGVMb2NhdGlvbjMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVMb2NhdGlvbjIpO1xuXG52YXIgX3J1blRyYW5zaXRpb25Ib29rID0gcmVxdWlyZSgnLi9ydW5UcmFuc2l0aW9uSG9vaycpO1xuXG52YXIgX3J1blRyYW5zaXRpb25Ib29rMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3J1blRyYW5zaXRpb25Ib29rKTtcblxudmFyIF9kZXByZWNhdGUgPSByZXF1aXJlKCcuL2RlcHJlY2F0ZScpO1xuXG52YXIgX2RlcHJlY2F0ZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9kZXByZWNhdGUpO1xuXG5mdW5jdGlvbiBjcmVhdGVSYW5kb21LZXkobGVuZ3RoKSB7XG4gIHJldHVybiBNYXRoLnJhbmRvbSgpLnRvU3RyaW5nKDM2KS5zdWJzdHIoMiwgbGVuZ3RoKTtcbn1cblxuZnVuY3Rpb24gbG9jYXRpb25zQXJlRXF1YWwoYSwgYikge1xuICByZXR1cm4gYS5wYXRobmFtZSA9PT0gYi5wYXRobmFtZSAmJiBhLnNlYXJjaCA9PT0gYi5zZWFyY2ggJiZcbiAgLy9hLmFjdGlvbiA9PT0gYi5hY3Rpb24gJiYgLy8gRGlmZmVyZW50IGFjdGlvbiAhPT0gbG9jYXRpb24gY2hhbmdlLlxuICBhLmtleSA9PT0gYi5rZXkgJiYgX2RlZXBFcXVhbDJbJ2RlZmF1bHQnXShhLnN0YXRlLCBiLnN0YXRlKTtcbn1cblxudmFyIERlZmF1bHRLZXlMZW5ndGggPSA2O1xuXG5mdW5jdGlvbiBjcmVhdGVIaXN0b3J5KCkge1xuICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuICB2YXIgZ2V0Q3VycmVudExvY2F0aW9uID0gb3B0aW9ucy5nZXRDdXJyZW50TG9jYXRpb247XG4gIHZhciBmaW5pc2hUcmFuc2l0aW9uID0gb3B0aW9ucy5maW5pc2hUcmFuc2l0aW9uO1xuICB2YXIgc2F2ZVN0YXRlID0gb3B0aW9ucy5zYXZlU3RhdGU7XG4gIHZhciBnbyA9IG9wdGlvbnMuZ287XG4gIHZhciBnZXRVc2VyQ29uZmlybWF0aW9uID0gb3B0aW9ucy5nZXRVc2VyQ29uZmlybWF0aW9uO1xuICB2YXIga2V5TGVuZ3RoID0gb3B0aW9ucy5rZXlMZW5ndGg7XG5cbiAgaWYgKHR5cGVvZiBrZXlMZW5ndGggIT09ICdudW1iZXInKSBrZXlMZW5ndGggPSBEZWZhdWx0S2V5TGVuZ3RoO1xuXG4gIHZhciB0cmFuc2l0aW9uSG9va3MgPSBbXTtcblxuICBmdW5jdGlvbiBsaXN0ZW5CZWZvcmUoaG9vaykge1xuICAgIHRyYW5zaXRpb25Ib29rcy5wdXNoKGhvb2spO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIHRyYW5zaXRpb25Ib29rcyA9IHRyYW5zaXRpb25Ib29rcy5maWx0ZXIoZnVuY3Rpb24gKGl0ZW0pIHtcbiAgICAgICAgcmV0dXJuIGl0ZW0gIT09IGhvb2s7XG4gICAgICB9KTtcbiAgICB9O1xuICB9XG5cbiAgdmFyIGFsbEtleXMgPSBbXTtcbiAgdmFyIGNoYW5nZUxpc3RlbmVycyA9IFtdO1xuICB2YXIgbG9jYXRpb24gPSB1bmRlZmluZWQ7XG5cbiAgZnVuY3Rpb24gZ2V0Q3VycmVudCgpIHtcbiAgICBpZiAocGVuZGluZ0xvY2F0aW9uICYmIHBlbmRpbmdMb2NhdGlvbi5hY3Rpb24gPT09IF9BY3Rpb25zLlBPUCkge1xuICAgICAgcmV0dXJuIGFsbEtleXMuaW5kZXhPZihwZW5kaW5nTG9jYXRpb24ua2V5KTtcbiAgICB9IGVsc2UgaWYgKGxvY2F0aW9uKSB7XG4gICAgICByZXR1cm4gYWxsS2V5cy5pbmRleE9mKGxvY2F0aW9uLmtleSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiAtMTtcbiAgICB9XG4gIH1cblxuICBmdW5jdGlvbiB1cGRhdGVMb2NhdGlvbihuZXdMb2NhdGlvbikge1xuICAgIHZhciBjdXJyZW50ID0gZ2V0Q3VycmVudCgpO1xuXG4gICAgbG9jYXRpb24gPSBuZXdMb2NhdGlvbjtcblxuICAgIGlmIChsb2NhdGlvbi5hY3Rpb24gPT09IF9BY3Rpb25zLlBVU0gpIHtcbiAgICAgIGFsbEtleXMgPSBbXS5jb25jYXQoYWxsS2V5cy5zbGljZSgwLCBjdXJyZW50ICsgMSksIFtsb2NhdGlvbi5rZXldKTtcbiAgICB9IGVsc2UgaWYgKGxvY2F0aW9uLmFjdGlvbiA9PT0gX0FjdGlvbnMuUkVQTEFDRSkge1xuICAgICAgYWxsS2V5c1tjdXJyZW50XSA9IGxvY2F0aW9uLmtleTtcbiAgICB9XG5cbiAgICBjaGFuZ2VMaXN0ZW5lcnMuZm9yRWFjaChmdW5jdGlvbiAobGlzdGVuZXIpIHtcbiAgICAgIGxpc3RlbmVyKGxvY2F0aW9uKTtcbiAgICB9KTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGxpc3RlbihsaXN0ZW5lcikge1xuICAgIGNoYW5nZUxpc3RlbmVycy5wdXNoKGxpc3RlbmVyKTtcblxuICAgIGlmIChsb2NhdGlvbikge1xuICAgICAgbGlzdGVuZXIobG9jYXRpb24pO1xuICAgIH0gZWxzZSB7XG4gICAgICB2YXIgX2xvY2F0aW9uID0gZ2V0Q3VycmVudExvY2F0aW9uKCk7XG4gICAgICBhbGxLZXlzID0gW19sb2NhdGlvbi5rZXldO1xuICAgICAgdXBkYXRlTG9jYXRpb24oX2xvY2F0aW9uKTtcbiAgICB9XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgY2hhbmdlTGlzdGVuZXJzID0gY2hhbmdlTGlzdGVuZXJzLmZpbHRlcihmdW5jdGlvbiAoaXRlbSkge1xuICAgICAgICByZXR1cm4gaXRlbSAhPT0gbGlzdGVuZXI7XG4gICAgICB9KTtcbiAgICB9O1xuICB9XG5cbiAgZnVuY3Rpb24gY29uZmlybVRyYW5zaXRpb25Ubyhsb2NhdGlvbiwgY2FsbGJhY2spIHtcbiAgICBfQXN5bmNVdGlscy5sb29wQXN5bmModHJhbnNpdGlvbkhvb2tzLmxlbmd0aCwgZnVuY3Rpb24gKGluZGV4LCBuZXh0LCBkb25lKSB7XG4gICAgICBfcnVuVHJhbnNpdGlvbkhvb2syWydkZWZhdWx0J10odHJhbnNpdGlvbkhvb2tzW2luZGV4XSwgbG9jYXRpb24sIGZ1bmN0aW9uIChyZXN1bHQpIHtcbiAgICAgICAgaWYgKHJlc3VsdCAhPSBudWxsKSB7XG4gICAgICAgICAgZG9uZShyZXN1bHQpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIG5leHQoKTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfSwgZnVuY3Rpb24gKG1lc3NhZ2UpIHtcbiAgICAgIGlmIChnZXRVc2VyQ29uZmlybWF0aW9uICYmIHR5cGVvZiBtZXNzYWdlID09PSAnc3RyaW5nJykge1xuICAgICAgICBnZXRVc2VyQ29uZmlybWF0aW9uKG1lc3NhZ2UsIGZ1bmN0aW9uIChvaykge1xuICAgICAgICAgIGNhbGxiYWNrKG9rICE9PSBmYWxzZSk7XG4gICAgICAgIH0pO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgY2FsbGJhY2sobWVzc2FnZSAhPT0gZmFsc2UpO1xuICAgICAgfVxuICAgIH0pO1xuICB9XG5cbiAgdmFyIHBlbmRpbmdMb2NhdGlvbiA9IHVuZGVmaW5lZDtcblxuICBmdW5jdGlvbiB0cmFuc2l0aW9uVG8obmV4dExvY2F0aW9uKSB7XG4gICAgaWYgKGxvY2F0aW9uICYmIGxvY2F0aW9uc0FyZUVxdWFsKGxvY2F0aW9uLCBuZXh0TG9jYXRpb24pKSByZXR1cm47IC8vIE5vdGhpbmcgdG8gZG8uXG5cbiAgICBwZW5kaW5nTG9jYXRpb24gPSBuZXh0TG9jYXRpb247XG5cbiAgICBjb25maXJtVHJhbnNpdGlvblRvKG5leHRMb2NhdGlvbiwgZnVuY3Rpb24gKG9rKSB7XG4gICAgICBpZiAocGVuZGluZ0xvY2F0aW9uICE9PSBuZXh0TG9jYXRpb24pIHJldHVybjsgLy8gVHJhbnNpdGlvbiB3YXMgaW50ZXJydXB0ZWQuXG5cbiAgICAgIGlmIChvaykge1xuICAgICAgICAvLyB0cmVhdCBQVVNIIHRvIGN1cnJlbnQgcGF0aCBsaWtlIFJFUExBQ0UgdG8gYmUgY29uc2lzdGVudCB3aXRoIGJyb3dzZXJzXG4gICAgICAgIGlmIChuZXh0TG9jYXRpb24uYWN0aW9uID09PSBfQWN0aW9ucy5QVVNIKSB7XG4gICAgICAgICAgdmFyIHByZXZQYXRoID0gY3JlYXRlUGF0aChsb2NhdGlvbik7XG4gICAgICAgICAgdmFyIG5leHRQYXRoID0gY3JlYXRlUGF0aChuZXh0TG9jYXRpb24pO1xuXG4gICAgICAgICAgaWYgKG5leHRQYXRoID09PSBwcmV2UGF0aCAmJiBfZGVlcEVxdWFsMlsnZGVmYXVsdCddKGxvY2F0aW9uLnN0YXRlLCBuZXh0TG9jYXRpb24uc3RhdGUpKSBuZXh0TG9jYXRpb24uYWN0aW9uID0gX0FjdGlvbnMuUkVQTEFDRTtcbiAgICAgICAgfVxuXG4gICAgICAgIGlmIChmaW5pc2hUcmFuc2l0aW9uKG5leHRMb2NhdGlvbikgIT09IGZhbHNlKSB1cGRhdGVMb2NhdGlvbihuZXh0TG9jYXRpb24pO1xuICAgICAgfSBlbHNlIGlmIChsb2NhdGlvbiAmJiBuZXh0TG9jYXRpb24uYWN0aW9uID09PSBfQWN0aW9ucy5QT1ApIHtcbiAgICAgICAgdmFyIHByZXZJbmRleCA9IGFsbEtleXMuaW5kZXhPZihsb2NhdGlvbi5rZXkpO1xuICAgICAgICB2YXIgbmV4dEluZGV4ID0gYWxsS2V5cy5pbmRleE9mKG5leHRMb2NhdGlvbi5rZXkpO1xuXG4gICAgICAgIGlmIChwcmV2SW5kZXggIT09IC0xICYmIG5leHRJbmRleCAhPT0gLTEpIGdvKHByZXZJbmRleCAtIG5leHRJbmRleCk7IC8vIFJlc3RvcmUgdGhlIFVSTC5cbiAgICAgIH1cbiAgICB9KTtcbiAgfVxuXG4gIGZ1bmN0aW9uIHB1c2gobG9jYXRpb24pIHtcbiAgICB0cmFuc2l0aW9uVG8oY3JlYXRlTG9jYXRpb24obG9jYXRpb24sIF9BY3Rpb25zLlBVU0gsIGNyZWF0ZUtleSgpKSk7XG4gIH1cblxuICBmdW5jdGlvbiByZXBsYWNlKGxvY2F0aW9uKSB7XG4gICAgdHJhbnNpdGlvblRvKGNyZWF0ZUxvY2F0aW9uKGxvY2F0aW9uLCBfQWN0aW9ucy5SRVBMQUNFLCBjcmVhdGVLZXkoKSkpO1xuICB9XG5cbiAgZnVuY3Rpb24gZ29CYWNrKCkge1xuICAgIGdvKC0xKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGdvRm9yd2FyZCgpIHtcbiAgICBnbygxKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNyZWF0ZUtleSgpIHtcbiAgICByZXR1cm4gY3JlYXRlUmFuZG9tS2V5KGtleUxlbmd0aCk7XG4gIH1cblxuICBmdW5jdGlvbiBjcmVhdGVQYXRoKGxvY2F0aW9uKSB7XG4gICAgaWYgKGxvY2F0aW9uID09IG51bGwgfHwgdHlwZW9mIGxvY2F0aW9uID09PSAnc3RyaW5nJykgcmV0dXJuIGxvY2F0aW9uO1xuXG4gICAgdmFyIHBhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gICAgdmFyIHNlYXJjaCA9IGxvY2F0aW9uLnNlYXJjaDtcbiAgICB2YXIgaGFzaCA9IGxvY2F0aW9uLmhhc2g7XG5cbiAgICB2YXIgcmVzdWx0ID0gcGF0aG5hbWU7XG5cbiAgICBpZiAoc2VhcmNoKSByZXN1bHQgKz0gc2VhcmNoO1xuXG4gICAgaWYgKGhhc2gpIHJlc3VsdCArPSBoYXNoO1xuXG4gICAgcmV0dXJuIHJlc3VsdDtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNyZWF0ZUhyZWYobG9jYXRpb24pIHtcbiAgICByZXR1cm4gY3JlYXRlUGF0aChsb2NhdGlvbik7XG4gIH1cblxuICBmdW5jdGlvbiBjcmVhdGVMb2NhdGlvbihsb2NhdGlvbiwgYWN0aW9uKSB7XG4gICAgdmFyIGtleSA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMiB8fCBhcmd1bWVudHNbMl0gPT09IHVuZGVmaW5lZCA/IGNyZWF0ZUtleSgpIDogYXJndW1lbnRzWzJdO1xuXG4gICAgaWYgKHR5cGVvZiBhY3Rpb24gPT09ICdvYmplY3QnKSB7XG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdUaGUgc3RhdGUgKDJuZCkgYXJndW1lbnQgdG8gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbiBpcyBkZXByZWNhdGVkOyB1c2UgYSAnICsgJ2xvY2F0aW9uIGRlc2NyaXB0b3IgaW5zdGVhZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgICBpZiAodHlwZW9mIGxvY2F0aW9uID09PSAnc3RyaW5nJykgbG9jYXRpb24gPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChsb2NhdGlvbik7XG5cbiAgICAgIGxvY2F0aW9uID0gX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7IHN0YXRlOiBhY3Rpb24gfSk7XG5cbiAgICAgIGFjdGlvbiA9IGtleTtcbiAgICAgIGtleSA9IGFyZ3VtZW50c1szXSB8fCBjcmVhdGVLZXkoKTtcbiAgICB9XG5cbiAgICByZXR1cm4gX2NyZWF0ZUxvY2F0aW9uM1snZGVmYXVsdCddKGxvY2F0aW9uLCBhY3Rpb24sIGtleSk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHNldFN0YXRlKHN0YXRlKSB7XG4gICAgaWYgKGxvY2F0aW9uKSB7XG4gICAgICB1cGRhdGVMb2NhdGlvblN0YXRlKGxvY2F0aW9uLCBzdGF0ZSk7XG4gICAgICB1cGRhdGVMb2NhdGlvbihsb2NhdGlvbik7XG4gICAgfSBlbHNlIHtcbiAgICAgIHVwZGF0ZUxvY2F0aW9uU3RhdGUoZ2V0Q3VycmVudExvY2F0aW9uKCksIHN0YXRlKTtcbiAgICB9XG4gIH1cblxuICBmdW5jdGlvbiB1cGRhdGVMb2NhdGlvblN0YXRlKGxvY2F0aW9uLCBzdGF0ZSkge1xuICAgIGxvY2F0aW9uLnN0YXRlID0gX2V4dGVuZHMoe30sIGxvY2F0aW9uLnN0YXRlLCBzdGF0ZSk7XG4gICAgc2F2ZVN0YXRlKGxvY2F0aW9uLmtleSwgbG9jYXRpb24uc3RhdGUpO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiByZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spIHtcbiAgICBpZiAodHJhbnNpdGlvbkhvb2tzLmluZGV4T2YoaG9vaykgPT09IC0xKSB0cmFuc2l0aW9uSG9va3MucHVzaChob29rKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spIHtcbiAgICB0cmFuc2l0aW9uSG9va3MgPSB0cmFuc2l0aW9uSG9va3MuZmlsdGVyKGZ1bmN0aW9uIChpdGVtKSB7XG4gICAgICByZXR1cm4gaXRlbSAhPT0gaG9vaztcbiAgICB9KTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gcHVzaFN0YXRlKHN0YXRlLCBwYXRoKSB7XG4gICAgaWYgKHR5cGVvZiBwYXRoID09PSAnc3RyaW5nJykgcGF0aCA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgcHVzaChfZXh0ZW5kcyh7IHN0YXRlOiBzdGF0ZSB9LCBwYXRoKSk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHJlcGxhY2VTdGF0ZShzdGF0ZSwgcGF0aCkge1xuICAgIGlmICh0eXBlb2YgcGF0aCA9PT0gJ3N0cmluZycpIHBhdGggPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgIHJlcGxhY2UoX2V4dGVuZHMoeyBzdGF0ZTogc3RhdGUgfSwgcGF0aCkpO1xuICB9XG5cbiAgcmV0dXJuIHtcbiAgICBsaXN0ZW5CZWZvcmU6IGxpc3RlbkJlZm9yZSxcbiAgICBsaXN0ZW46IGxpc3RlbixcbiAgICB0cmFuc2l0aW9uVG86IHRyYW5zaXRpb25UbyxcbiAgICBwdXNoOiBwdXNoLFxuICAgIHJlcGxhY2U6IHJlcGxhY2UsXG4gICAgZ286IGdvLFxuICAgIGdvQmFjazogZ29CYWNrLFxuICAgIGdvRm9yd2FyZDogZ29Gb3J3YXJkLFxuICAgIGNyZWF0ZUtleTogY3JlYXRlS2V5LFxuICAgIGNyZWF0ZVBhdGg6IGNyZWF0ZVBhdGgsXG4gICAgY3JlYXRlSHJlZjogY3JlYXRlSHJlZixcbiAgICBjcmVhdGVMb2NhdGlvbjogY3JlYXRlTG9jYXRpb24sXG5cbiAgICBzZXRTdGF0ZTogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShzZXRTdGF0ZSwgJ3NldFN0YXRlIGlzIGRlcHJlY2F0ZWQ7IHVzZSBsb2NhdGlvbi5rZXkgdG8gc2F2ZSBzdGF0ZSBpbnN0ZWFkJyksXG4gICAgcmVnaXN0ZXJUcmFuc2l0aW9uSG9vazogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShyZWdpc3RlclRyYW5zaXRpb25Ib29rLCAncmVnaXN0ZXJUcmFuc2l0aW9uSG9vayBpcyBkZXByZWNhdGVkOyB1c2UgbGlzdGVuQmVmb3JlIGluc3RlYWQnKSxcbiAgICB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2s6IF9kZXByZWNhdGUyWydkZWZhdWx0J10odW5yZWdpc3RlclRyYW5zaXRpb25Ib29rLCAndW5yZWdpc3RlclRyYW5zaXRpb25Ib29rIGlzIGRlcHJlY2F0ZWQ7IHVzZSB0aGUgY2FsbGJhY2sgcmV0dXJuZWQgZnJvbSBsaXN0ZW5CZWZvcmUgaW5zdGVhZCcpLFxuICAgIHB1c2hTdGF0ZTogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShwdXNoU3RhdGUsICdwdXNoU3RhdGUgaXMgZGVwcmVjYXRlZDsgdXNlIHB1c2ggaW5zdGVhZCcpLFxuICAgIHJlcGxhY2VTdGF0ZTogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShyZXBsYWNlU3RhdGUsICdyZXBsYWNlU3RhdGUgaXMgZGVwcmVjYXRlZDsgdXNlIHJlcGxhY2UgaW5zdGVhZCcpXG4gIH07XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGNyZWF0ZUhpc3Rvcnk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfQWN0aW9ucyA9IHJlcXVpcmUoJy4vQWN0aW9ucycpO1xuXG52YXIgX1BhdGhVdGlscyA9IHJlcXVpcmUoJy4vUGF0aFV0aWxzJyk7XG5cbmZ1bmN0aW9uIGNyZWF0ZUxvY2F0aW9uKCkge1xuICB2YXIgbG9jYXRpb24gPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyAnLycgOiBhcmd1bWVudHNbMF07XG4gIHZhciBhY3Rpb24gPSBhcmd1bWVudHMubGVuZ3RoIDw9IDEgfHwgYXJndW1lbnRzWzFdID09PSB1bmRlZmluZWQgPyBfQWN0aW9ucy5QT1AgOiBhcmd1bWVudHNbMV07XG4gIHZhciBrZXkgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDIgfHwgYXJndW1lbnRzWzJdID09PSB1bmRlZmluZWQgPyBudWxsIDogYXJndW1lbnRzWzJdO1xuXG4gIHZhciBfZm91cnRoQXJnID0gYXJndW1lbnRzLmxlbmd0aCA8PSAzIHx8IGFyZ3VtZW50c1szXSA9PT0gdW5kZWZpbmVkID8gbnVsbCA6IGFyZ3VtZW50c1szXTtcblxuICBpZiAodHlwZW9mIGxvY2F0aW9uID09PSAnc3RyaW5nJykgbG9jYXRpb24gPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChsb2NhdGlvbik7XG5cbiAgaWYgKHR5cGVvZiBhY3Rpb24gPT09ICdvYmplY3QnKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnVGhlIHN0YXRlICgybmQpIGFyZ3VtZW50IHRvIGNyZWF0ZUxvY2F0aW9uIGlzIGRlcHJlY2F0ZWQ7IHVzZSBhICcgKyAnbG9jYXRpb24gZGVzY3JpcHRvciBpbnN0ZWFkJykgOiB1bmRlZmluZWQ7XG5cbiAgICBsb2NhdGlvbiA9IF9leHRlbmRzKHt9LCBsb2NhdGlvbiwgeyBzdGF0ZTogYWN0aW9uIH0pO1xuXG4gICAgYWN0aW9uID0ga2V5IHx8IF9BY3Rpb25zLlBPUDtcbiAgICBrZXkgPSBfZm91cnRoQXJnO1xuICB9XG5cbiAgdmFyIHBhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWUgfHwgJy8nO1xuICB2YXIgc2VhcmNoID0gbG9jYXRpb24uc2VhcmNoIHx8ICcnO1xuICB2YXIgaGFzaCA9IGxvY2F0aW9uLmhhc2ggfHwgJyc7XG4gIHZhciBzdGF0ZSA9IGxvY2F0aW9uLnN0YXRlIHx8IG51bGw7XG5cbiAgcmV0dXJuIHtcbiAgICBwYXRobmFtZTogcGF0aG5hbWUsXG4gICAgc2VhcmNoOiBzZWFyY2gsXG4gICAgaGFzaDogaGFzaCxcbiAgICBzdGF0ZTogc3RhdGUsXG4gICAgYWN0aW9uOiBhY3Rpb24sXG4gICAga2V5OiBrZXlcbiAgfTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gY3JlYXRlTG9jYXRpb247XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfUGF0aFV0aWxzID0gcmVxdWlyZSgnLi9QYXRoVXRpbHMnKTtcblxudmFyIF9BY3Rpb25zID0gcmVxdWlyZSgnLi9BY3Rpb25zJyk7XG5cbnZhciBfY3JlYXRlSGlzdG9yeSA9IHJlcXVpcmUoJy4vY3JlYXRlSGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZUhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlSGlzdG9yeSk7XG5cbmZ1bmN0aW9uIGNyZWF0ZVN0YXRlU3RvcmFnZShlbnRyaWVzKSB7XG4gIHJldHVybiBlbnRyaWVzLmZpbHRlcihmdW5jdGlvbiAoZW50cnkpIHtcbiAgICByZXR1cm4gZW50cnkuc3RhdGU7XG4gIH0pLnJlZHVjZShmdW5jdGlvbiAobWVtbywgZW50cnkpIHtcbiAgICBtZW1vW2VudHJ5LmtleV0gPSBlbnRyeS5zdGF0ZTtcbiAgICByZXR1cm4gbWVtbztcbiAgfSwge30pO1xufVxuXG5mdW5jdGlvbiBjcmVhdGVNZW1vcnlIaXN0b3J5KCkge1xuICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuXG4gIGlmIChBcnJheS5pc0FycmF5KG9wdGlvbnMpKSB7XG4gICAgb3B0aW9ucyA9IHsgZW50cmllczogb3B0aW9ucyB9O1xuICB9IGVsc2UgaWYgKHR5cGVvZiBvcHRpb25zID09PSAnc3RyaW5nJykge1xuICAgIG9wdGlvbnMgPSB7IGVudHJpZXM6IFtvcHRpb25zXSB9O1xuICB9XG5cbiAgdmFyIGhpc3RvcnkgPSBfY3JlYXRlSGlzdG9yeTJbJ2RlZmF1bHQnXShfZXh0ZW5kcyh7fSwgb3B0aW9ucywge1xuICAgIGdldEN1cnJlbnRMb2NhdGlvbjogZ2V0Q3VycmVudExvY2F0aW9uLFxuICAgIGZpbmlzaFRyYW5zaXRpb246IGZpbmlzaFRyYW5zaXRpb24sXG4gICAgc2F2ZVN0YXRlOiBzYXZlU3RhdGUsXG4gICAgZ286IGdvXG4gIH0pKTtcblxuICB2YXIgX29wdGlvbnMgPSBvcHRpb25zO1xuICB2YXIgZW50cmllcyA9IF9vcHRpb25zLmVudHJpZXM7XG4gIHZhciBjdXJyZW50ID0gX29wdGlvbnMuY3VycmVudDtcblxuICBpZiAodHlwZW9mIGVudHJpZXMgPT09ICdzdHJpbmcnKSB7XG4gICAgZW50cmllcyA9IFtlbnRyaWVzXTtcbiAgfSBlbHNlIGlmICghQXJyYXkuaXNBcnJheShlbnRyaWVzKSkge1xuICAgIGVudHJpZXMgPSBbJy8nXTtcbiAgfVxuXG4gIGVudHJpZXMgPSBlbnRyaWVzLm1hcChmdW5jdGlvbiAoZW50cnkpIHtcbiAgICB2YXIga2V5ID0gaGlzdG9yeS5jcmVhdGVLZXkoKTtcblxuICAgIGlmICh0eXBlb2YgZW50cnkgPT09ICdzdHJpbmcnKSByZXR1cm4geyBwYXRobmFtZTogZW50cnksIGtleToga2V5IH07XG5cbiAgICBpZiAodHlwZW9mIGVudHJ5ID09PSAnb2JqZWN0JyAmJiBlbnRyeSkgcmV0dXJuIF9leHRlbmRzKHt9LCBlbnRyeSwgeyBrZXk6IGtleSB9KTtcblxuICAgICFmYWxzZSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlLCAnVW5hYmxlIHRvIGNyZWF0ZSBoaXN0b3J5IGVudHJ5IGZyb20gJXMnLCBlbnRyeSkgOiBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlKSA6IHVuZGVmaW5lZDtcbiAgfSk7XG5cbiAgaWYgKGN1cnJlbnQgPT0gbnVsbCkge1xuICAgIGN1cnJlbnQgPSBlbnRyaWVzLmxlbmd0aCAtIDE7XG4gIH0gZWxzZSB7XG4gICAgIShjdXJyZW50ID49IDAgJiYgY3VycmVudCA8IGVudHJpZXMubGVuZ3RoKSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlLCAnQ3VycmVudCBpbmRleCBtdXN0IGJlID49IDAgYW5kIDwgJXMsIHdhcyAlcycsIGVudHJpZXMubGVuZ3RoLCBjdXJyZW50KSA6IF9pbnZhcmlhbnQyWydkZWZhdWx0J10oZmFsc2UpIDogdW5kZWZpbmVkO1xuICB9XG5cbiAgdmFyIHN0b3JhZ2UgPSBjcmVhdGVTdGF0ZVN0b3JhZ2UoZW50cmllcyk7XG5cbiAgZnVuY3Rpb24gc2F2ZVN0YXRlKGtleSwgc3RhdGUpIHtcbiAgICBzdG9yYWdlW2tleV0gPSBzdGF0ZTtcbiAgfVxuXG4gIGZ1bmN0aW9uIHJlYWRTdGF0ZShrZXkpIHtcbiAgICByZXR1cm4gc3RvcmFnZVtrZXldO1xuICB9XG5cbiAgZnVuY3Rpb24gZ2V0Q3VycmVudExvY2F0aW9uKCkge1xuICAgIHZhciBlbnRyeSA9IGVudHJpZXNbY3VycmVudF07XG4gICAgdmFyIGJhc2VuYW1lID0gZW50cnkuYmFzZW5hbWU7XG4gICAgdmFyIHBhdGhuYW1lID0gZW50cnkucGF0aG5hbWU7XG4gICAgdmFyIHNlYXJjaCA9IGVudHJ5LnNlYXJjaDtcblxuICAgIHZhciBwYXRoID0gKGJhc2VuYW1lIHx8ICcnKSArIHBhdGhuYW1lICsgKHNlYXJjaCB8fCAnJyk7XG5cbiAgICB2YXIga2V5ID0gdW5kZWZpbmVkLFxuICAgICAgICBzdGF0ZSA9IHVuZGVmaW5lZDtcbiAgICBpZiAoZW50cnkua2V5KSB7XG4gICAgICBrZXkgPSBlbnRyeS5rZXk7XG4gICAgICBzdGF0ZSA9IHJlYWRTdGF0ZShrZXkpO1xuICAgIH0gZWxzZSB7XG4gICAgICBrZXkgPSBoaXN0b3J5LmNyZWF0ZUtleSgpO1xuICAgICAgc3RhdGUgPSBudWxsO1xuICAgICAgZW50cnkua2V5ID0ga2V5O1xuICAgIH1cblxuICAgIHZhciBsb2NhdGlvbiA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgcmV0dXJuIGhpc3RvcnkuY3JlYXRlTG9jYXRpb24oX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7IHN0YXRlOiBzdGF0ZSB9KSwgdW5kZWZpbmVkLCBrZXkpO1xuICB9XG5cbiAgZnVuY3Rpb24gY2FuR28obikge1xuICAgIHZhciBpbmRleCA9IGN1cnJlbnQgKyBuO1xuICAgIHJldHVybiBpbmRleCA+PSAwICYmIGluZGV4IDwgZW50cmllcy5sZW5ndGg7XG4gIH1cblxuICBmdW5jdGlvbiBnbyhuKSB7XG4gICAgaWYgKG4pIHtcbiAgICAgIGlmICghY2FuR28obikpIHtcbiAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnQ2Fubm90IGdvKCVzKSB0aGVyZSBpcyBub3QgZW5vdWdoIGhpc3RvcnknLCBuKSA6IHVuZGVmaW5lZDtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuXG4gICAgICBjdXJyZW50ICs9IG47XG5cbiAgICAgIHZhciBjdXJyZW50TG9jYXRpb24gPSBnZXRDdXJyZW50TG9jYXRpb24oKTtcblxuICAgICAgLy8gY2hhbmdlIGFjdGlvbiB0byBQT1BcbiAgICAgIGhpc3RvcnkudHJhbnNpdGlvblRvKF9leHRlbmRzKHt9LCBjdXJyZW50TG9jYXRpb24sIHsgYWN0aW9uOiBfQWN0aW9ucy5QT1AgfSkpO1xuICAgIH1cbiAgfVxuXG4gIGZ1bmN0aW9uIGZpbmlzaFRyYW5zaXRpb24obG9jYXRpb24pIHtcbiAgICBzd2l0Y2ggKGxvY2F0aW9uLmFjdGlvbikge1xuICAgICAgY2FzZSBfQWN0aW9ucy5QVVNIOlxuICAgICAgICBjdXJyZW50ICs9IDE7XG5cbiAgICAgICAgLy8gaWYgd2UgYXJlIG5vdCBvbiB0aGUgdG9wIG9mIHN0YWNrXG4gICAgICAgIC8vIHJlbW92ZSByZXN0IGFuZCBwdXNoIG5ld1xuICAgICAgICBpZiAoY3VycmVudCA8IGVudHJpZXMubGVuZ3RoKSBlbnRyaWVzLnNwbGljZShjdXJyZW50KTtcblxuICAgICAgICBlbnRyaWVzLnB1c2gobG9jYXRpb24pO1xuICAgICAgICBzYXZlU3RhdGUobG9jYXRpb24ua2V5LCBsb2NhdGlvbi5zdGF0ZSk7XG4gICAgICAgIGJyZWFrO1xuICAgICAgY2FzZSBfQWN0aW9ucy5SRVBMQUNFOlxuICAgICAgICBlbnRyaWVzW2N1cnJlbnRdID0gbG9jYXRpb247XG4gICAgICAgIHNhdmVTdGF0ZShsb2NhdGlvbi5rZXksIGxvY2F0aW9uLnN0YXRlKTtcbiAgICAgICAgYnJlYWs7XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIGhpc3Rvcnk7XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGNyZWF0ZU1lbW9yeUhpc3Rvcnk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbmZ1bmN0aW9uIGRlcHJlY2F0ZShmbiwgbWVzc2FnZSkge1xuICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ1toaXN0b3J5XSAnICsgbWVzc2FnZSkgOiB1bmRlZmluZWQ7XG4gICAgcmV0dXJuIGZuLmFwcGx5KHRoaXMsIGFyZ3VtZW50cyk7XG4gIH07XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGRlcHJlY2F0ZTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gcnVuVHJhbnNpdGlvbkhvb2soaG9vaywgbG9jYXRpb24sIGNhbGxiYWNrKSB7XG4gIHZhciByZXN1bHQgPSBob29rKGxvY2F0aW9uLCBjYWxsYmFjayk7XG5cbiAgaWYgKGhvb2subGVuZ3RoIDwgMikge1xuICAgIC8vIEFzc3VtZSB0aGUgaG9vayBydW5zIHN5bmNocm9ub3VzbHkgYW5kIGF1dG9tYXRpY2FsbHlcbiAgICAvLyBjYWxsIHRoZSBjYWxsYmFjayB3aXRoIHRoZSByZXR1cm4gdmFsdWUuXG4gICAgY2FsbGJhY2socmVzdWx0KTtcbiAgfSBlbHNlIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10ocmVzdWx0ID09PSB1bmRlZmluZWQsICdZb3Ugc2hvdWxkIG5vdCBcInJldHVyblwiIGluIGEgdHJhbnNpdGlvbiBob29rIHdpdGggYSBjYWxsYmFjayBhcmd1bWVudDsgY2FsbCB0aGUgY2FsbGJhY2sgaW5zdGVhZCcpIDogdW5kZWZpbmVkO1xuICB9XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IHJ1blRyYW5zaXRpb25Ib29rO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG52YXIgX0V4ZWN1dGlvbkVudmlyb25tZW50ID0gcmVxdWlyZSgnLi9FeGVjdXRpb25FbnZpcm9ubWVudCcpO1xuXG52YXIgX1BhdGhVdGlscyA9IHJlcXVpcmUoJy4vUGF0aFV0aWxzJyk7XG5cbnZhciBfcnVuVHJhbnNpdGlvbkhvb2sgPSByZXF1aXJlKCcuL3J1blRyYW5zaXRpb25Ib29rJyk7XG5cbnZhciBfcnVuVHJhbnNpdGlvbkhvb2syID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcnVuVHJhbnNpdGlvbkhvb2spO1xuXG52YXIgX2RlcHJlY2F0ZSA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlJyk7XG5cbnZhciBfZGVwcmVjYXRlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2RlcHJlY2F0ZSk7XG5cbmZ1bmN0aW9uIHVzZUJhc2VuYW1lKGNyZWF0ZUhpc3RvcnkpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuXG4gICAgdmFyIGhpc3RvcnkgPSBjcmVhdGVIaXN0b3J5KG9wdGlvbnMpO1xuXG4gICAgdmFyIGJhc2VuYW1lID0gb3B0aW9ucy5iYXNlbmFtZTtcblxuICAgIHZhciBjaGVja2VkQmFzZUhyZWYgPSBmYWxzZTtcblxuICAgIGZ1bmN0aW9uIGNoZWNrQmFzZUhyZWYoKSB7XG4gICAgICBpZiAoY2hlY2tlZEJhc2VIcmVmKSB7XG4gICAgICAgIHJldHVybjtcbiAgICAgIH1cblxuICAgICAgLy8gQXV0b21hdGljYWxseSB1c2UgdGhlIHZhbHVlIG9mIDxiYXNlIGhyZWY+IGluIEhUTUxcbiAgICAgIC8vIGRvY3VtZW50cyBhcyBiYXNlbmFtZSBpZiBpdCdzIG5vdCBleHBsaWNpdGx5IGdpdmVuLlxuICAgICAgaWYgKGJhc2VuYW1lID09IG51bGwgJiYgX0V4ZWN1dGlvbkVudmlyb25tZW50LmNhblVzZURPTSkge1xuICAgICAgICB2YXIgYmFzZSA9IGRvY3VtZW50LmdldEVsZW1lbnRzQnlUYWdOYW1lKCdiYXNlJylbMF07XG4gICAgICAgIHZhciBiYXNlSHJlZiA9IGJhc2UgJiYgYmFzZS5nZXRBdHRyaWJ1dGUoJ2hyZWYnKTtcblxuICAgICAgICBpZiAoYmFzZUhyZWYgIT0gbnVsbCkge1xuICAgICAgICAgIGJhc2VuYW1lID0gYmFzZUhyZWY7XG5cbiAgICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdBdXRvbWF0aWNhbGx5IHNldHRpbmcgYmFzZW5hbWUgdXNpbmcgPGJhc2UgaHJlZj4gaXMgZGVwcmVjYXRlZCBhbmQgd2lsbCAnICsgJ2JlIHJlbW92ZWQgaW4gdGhlIG5leHQgbWFqb3IgcmVsZWFzZS4gVGhlIHNlbWFudGljcyBvZiA8YmFzZSBocmVmPiBhcmUgJyArICdzdWJ0bHkgZGlmZmVyZW50IGZyb20gYmFzZW5hbWUuIFBsZWFzZSBwYXNzIHRoZSBiYXNlbmFtZSBleHBsaWNpdGx5IGluICcgKyAndGhlIG9wdGlvbnMgdG8gY3JlYXRlSGlzdG9yeScpIDogdW5kZWZpbmVkO1xuICAgICAgICB9XG4gICAgICB9XG5cbiAgICAgIGNoZWNrZWRCYXNlSHJlZiA9IHRydWU7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gYWRkQmFzZW5hbWUobG9jYXRpb24pIHtcbiAgICAgIGNoZWNrQmFzZUhyZWYoKTtcblxuICAgICAgaWYgKGJhc2VuYW1lICYmIGxvY2F0aW9uLmJhc2VuYW1lID09IG51bGwpIHtcbiAgICAgICAgaWYgKGxvY2F0aW9uLnBhdGhuYW1lLmluZGV4T2YoYmFzZW5hbWUpID09PSAwKSB7XG4gICAgICAgICAgbG9jYXRpb24ucGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZS5zdWJzdHJpbmcoYmFzZW5hbWUubGVuZ3RoKTtcbiAgICAgICAgICBsb2NhdGlvbi5iYXNlbmFtZSA9IGJhc2VuYW1lO1xuXG4gICAgICAgICAgaWYgKGxvY2F0aW9uLnBhdGhuYW1lID09PSAnJykgbG9jYXRpb24ucGF0aG5hbWUgPSAnLyc7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgbG9jYXRpb24uYmFzZW5hbWUgPSAnJztcbiAgICAgICAgfVxuICAgICAgfVxuXG4gICAgICByZXR1cm4gbG9jYXRpb247XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gcHJlcGVuZEJhc2VuYW1lKGxvY2F0aW9uKSB7XG4gICAgICBjaGVja0Jhc2VIcmVmKCk7XG5cbiAgICAgIGlmICghYmFzZW5hbWUpIHJldHVybiBsb2NhdGlvbjtcblxuICAgICAgaWYgKHR5cGVvZiBsb2NhdGlvbiA9PT0gJ3N0cmluZycpIGxvY2F0aW9uID0gX1BhdGhVdGlscy5wYXJzZVBhdGgobG9jYXRpb24pO1xuXG4gICAgICB2YXIgcG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZTtcbiAgICAgIHZhciBub3JtYWxpemVkQmFzZW5hbWUgPSBiYXNlbmFtZS5zbGljZSgtMSkgPT09ICcvJyA/IGJhc2VuYW1lIDogYmFzZW5hbWUgKyAnLyc7XG4gICAgICB2YXIgbm9ybWFsaXplZFBhdGhuYW1lID0gcG5hbWUuY2hhckF0KDApID09PSAnLycgPyBwbmFtZS5zbGljZSgxKSA6IHBuYW1lO1xuICAgICAgdmFyIHBhdGhuYW1lID0gbm9ybWFsaXplZEJhc2VuYW1lICsgbm9ybWFsaXplZFBhdGhuYW1lO1xuXG4gICAgICByZXR1cm4gX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7XG4gICAgICAgIHBhdGhuYW1lOiBwYXRobmFtZVxuICAgICAgfSk7XG4gICAgfVxuXG4gICAgLy8gT3ZlcnJpZGUgYWxsIHJlYWQgbWV0aG9kcyB3aXRoIGJhc2VuYW1lLWF3YXJlIHZlcnNpb25zLlxuICAgIGZ1bmN0aW9uIGxpc3RlbkJlZm9yZShob29rKSB7XG4gICAgICByZXR1cm4gaGlzdG9yeS5saXN0ZW5CZWZvcmUoZnVuY3Rpb24gKGxvY2F0aW9uLCBjYWxsYmFjaykge1xuICAgICAgICBfcnVuVHJhbnNpdGlvbkhvb2syWydkZWZhdWx0J10oaG9vaywgYWRkQmFzZW5hbWUobG9jYXRpb24pLCBjYWxsYmFjayk7XG4gICAgICB9KTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBsaXN0ZW4obGlzdGVuZXIpIHtcbiAgICAgIHJldHVybiBoaXN0b3J5Lmxpc3RlbihmdW5jdGlvbiAobG9jYXRpb24pIHtcbiAgICAgICAgbGlzdGVuZXIoYWRkQmFzZW5hbWUobG9jYXRpb24pKTtcbiAgICAgIH0pO1xuICAgIH1cblxuICAgIC8vIE92ZXJyaWRlIGFsbCB3cml0ZSBtZXRob2RzIHdpdGggYmFzZW5hbWUtYXdhcmUgdmVyc2lvbnMuXG4gICAgZnVuY3Rpb24gcHVzaChsb2NhdGlvbikge1xuICAgICAgaGlzdG9yeS5wdXNoKHByZXBlbmRCYXNlbmFtZShsb2NhdGlvbikpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIHJlcGxhY2UobG9jYXRpb24pIHtcbiAgICAgIGhpc3RvcnkucmVwbGFjZShwcmVwZW5kQmFzZW5hbWUobG9jYXRpb24pKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjcmVhdGVQYXRoKGxvY2F0aW9uKSB7XG4gICAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVQYXRoKHByZXBlbmRCYXNlbmFtZShsb2NhdGlvbikpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGNyZWF0ZUhyZWYobG9jYXRpb24pIHtcbiAgICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZUhyZWYocHJlcGVuZEJhc2VuYW1lKGxvY2F0aW9uKSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gY3JlYXRlTG9jYXRpb24obG9jYXRpb24pIHtcbiAgICAgIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBhcmdzID0gQXJyYXkoX2xlbiA+IDEgPyBfbGVuIC0gMSA6IDApLCBfa2V5ID0gMTsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgICAgICBhcmdzW19rZXkgLSAxXSA9IGFyZ3VtZW50c1tfa2V5XTtcbiAgICAgIH1cblxuICAgICAgcmV0dXJuIGFkZEJhc2VuYW1lKGhpc3RvcnkuY3JlYXRlTG9jYXRpb24uYXBwbHkoaGlzdG9yeSwgW3ByZXBlbmRCYXNlbmFtZShsb2NhdGlvbildLmNvbmNhdChhcmdzKSkpO1xuICAgIH1cblxuICAgIC8vIGRlcHJlY2F0ZWRcbiAgICBmdW5jdGlvbiBwdXNoU3RhdGUoc3RhdGUsIHBhdGgpIHtcbiAgICAgIGlmICh0eXBlb2YgcGF0aCA9PT0gJ3N0cmluZycpIHBhdGggPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgICAgcHVzaChfZXh0ZW5kcyh7IHN0YXRlOiBzdGF0ZSB9LCBwYXRoKSk7XG4gICAgfVxuXG4gICAgLy8gZGVwcmVjYXRlZFxuICAgIGZ1bmN0aW9uIHJlcGxhY2VTdGF0ZShzdGF0ZSwgcGF0aCkge1xuICAgICAgaWYgKHR5cGVvZiBwYXRoID09PSAnc3RyaW5nJykgcGF0aCA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgICByZXBsYWNlKF9leHRlbmRzKHsgc3RhdGU6IHN0YXRlIH0sIHBhdGgpKTtcbiAgICB9XG5cbiAgICByZXR1cm4gX2V4dGVuZHMoe30sIGhpc3RvcnksIHtcbiAgICAgIGxpc3RlbkJlZm9yZTogbGlzdGVuQmVmb3JlLFxuICAgICAgbGlzdGVuOiBsaXN0ZW4sXG4gICAgICBwdXNoOiBwdXNoLFxuICAgICAgcmVwbGFjZTogcmVwbGFjZSxcbiAgICAgIGNyZWF0ZVBhdGg6IGNyZWF0ZVBhdGgsXG4gICAgICBjcmVhdGVIcmVmOiBjcmVhdGVIcmVmLFxuICAgICAgY3JlYXRlTG9jYXRpb246IGNyZWF0ZUxvY2F0aW9uLFxuXG4gICAgICBwdXNoU3RhdGU6IF9kZXByZWNhdGUyWydkZWZhdWx0J10ocHVzaFN0YXRlLCAncHVzaFN0YXRlIGlzIGRlcHJlY2F0ZWQ7IHVzZSBwdXNoIGluc3RlYWQnKSxcbiAgICAgIHJlcGxhY2VTdGF0ZTogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShyZXBsYWNlU3RhdGUsICdyZXBsYWNlU3RhdGUgaXMgZGVwcmVjYXRlZDsgdXNlIHJlcGxhY2UgaW5zdGVhZCcpXG4gICAgfSk7XG4gIH07XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IHVzZUJhc2VuYW1lO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG52YXIgX3F1ZXJ5U3RyaW5nID0gcmVxdWlyZSgncXVlcnktc3RyaW5nJyk7XG5cbnZhciBfcnVuVHJhbnNpdGlvbkhvb2sgPSByZXF1aXJlKCcuL3J1blRyYW5zaXRpb25Ib29rJyk7XG5cbnZhciBfcnVuVHJhbnNpdGlvbkhvb2syID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcnVuVHJhbnNpdGlvbkhvb2spO1xuXG52YXIgX1BhdGhVdGlscyA9IHJlcXVpcmUoJy4vUGF0aFV0aWxzJyk7XG5cbnZhciBfZGVwcmVjYXRlID0gcmVxdWlyZSgnLi9kZXByZWNhdGUnKTtcblxudmFyIF9kZXByZWNhdGUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZGVwcmVjYXRlKTtcblxudmFyIFNFQVJDSF9CQVNFX0tFWSA9ICckc2VhcmNoQmFzZSc7XG5cbmZ1bmN0aW9uIGRlZmF1bHRTdHJpbmdpZnlRdWVyeShxdWVyeSkge1xuICByZXR1cm4gX3F1ZXJ5U3RyaW5nLnN0cmluZ2lmeShxdWVyeSkucmVwbGFjZSgvJTIwL2csICcrJyk7XG59XG5cbnZhciBkZWZhdWx0UGFyc2VRdWVyeVN0cmluZyA9IF9xdWVyeVN0cmluZy5wYXJzZTtcblxuZnVuY3Rpb24gaXNOZXN0ZWRPYmplY3Qob2JqZWN0KSB7XG4gIGZvciAodmFyIHAgaW4gb2JqZWN0KSB7XG4gICAgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmplY3QsIHApICYmIHR5cGVvZiBvYmplY3RbcF0gPT09ICdvYmplY3QnICYmICFBcnJheS5pc0FycmF5KG9iamVjdFtwXSkgJiYgb2JqZWN0W3BdICE9PSBudWxsKSByZXR1cm4gdHJ1ZTtcbiAgfXJldHVybiBmYWxzZTtcbn1cblxuLyoqXG4gKiBSZXR1cm5zIGEgbmV3IGNyZWF0ZUhpc3RvcnkgZnVuY3Rpb24gdGhhdCBtYXkgYmUgdXNlZCB0byBjcmVhdGVcbiAqIGhpc3Rvcnkgb2JqZWN0cyB0aGF0IGtub3cgaG93IHRvIGhhbmRsZSBVUkwgcXVlcmllcy5cbiAqL1xuZnVuY3Rpb24gdXNlUXVlcmllcyhjcmVhdGVIaXN0b3J5KSB7XG4gIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcblxuICAgIHZhciBoaXN0b3J5ID0gY3JlYXRlSGlzdG9yeShvcHRpb25zKTtcblxuICAgIHZhciBzdHJpbmdpZnlRdWVyeSA9IG9wdGlvbnMuc3RyaW5naWZ5UXVlcnk7XG4gICAgdmFyIHBhcnNlUXVlcnlTdHJpbmcgPSBvcHRpb25zLnBhcnNlUXVlcnlTdHJpbmc7XG5cbiAgICBpZiAodHlwZW9mIHN0cmluZ2lmeVF1ZXJ5ICE9PSAnZnVuY3Rpb24nKSBzdHJpbmdpZnlRdWVyeSA9IGRlZmF1bHRTdHJpbmdpZnlRdWVyeTtcblxuICAgIGlmICh0eXBlb2YgcGFyc2VRdWVyeVN0cmluZyAhPT0gJ2Z1bmN0aW9uJykgcGFyc2VRdWVyeVN0cmluZyA9IGRlZmF1bHRQYXJzZVF1ZXJ5U3RyaW5nO1xuXG4gICAgZnVuY3Rpb24gYWRkUXVlcnkobG9jYXRpb24pIHtcbiAgICAgIGlmIChsb2NhdGlvbi5xdWVyeSA9PSBudWxsKSB7XG4gICAgICAgIHZhciBzZWFyY2ggPSBsb2NhdGlvbi5zZWFyY2g7XG5cbiAgICAgICAgbG9jYXRpb24ucXVlcnkgPSBwYXJzZVF1ZXJ5U3RyaW5nKHNlYXJjaC5zdWJzdHJpbmcoMSkpO1xuICAgICAgICBsb2NhdGlvbltTRUFSQ0hfQkFTRV9LRVldID0geyBzZWFyY2g6IHNlYXJjaCwgc2VhcmNoQmFzZTogJycgfTtcbiAgICAgIH1cblxuICAgICAgLy8gVE9ETzogSW5zdGVhZCBvZiBhbGwgdGhlIGJvb2sta2VlcGluZyBoZXJlLCB0aGlzIHNob3VsZCBqdXN0IHN0cmlwIHRoZVxuICAgICAgLy8gc3RyaW5naWZpZWQgcXVlcnkgZnJvbSB0aGUgc2VhcmNoLlxuXG4gICAgICByZXR1cm4gbG9jYXRpb247XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gYXBwZW5kUXVlcnkobG9jYXRpb24sIHF1ZXJ5KSB7XG4gICAgICB2YXIgX2V4dGVuZHMyO1xuXG4gICAgICB2YXIgc2VhcmNoQmFzZVNwZWMgPSBsb2NhdGlvbltTRUFSQ0hfQkFTRV9LRVldO1xuICAgICAgdmFyIHF1ZXJ5U3RyaW5nID0gcXVlcnkgPyBzdHJpbmdpZnlRdWVyeShxdWVyeSkgOiAnJztcbiAgICAgIGlmICghc2VhcmNoQmFzZVNwZWMgJiYgIXF1ZXJ5U3RyaW5nKSB7XG4gICAgICAgIHJldHVybiBsb2NhdGlvbjtcbiAgICAgIH1cblxuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKHN0cmluZ2lmeVF1ZXJ5ICE9PSBkZWZhdWx0U3RyaW5naWZ5UXVlcnkgfHwgIWlzTmVzdGVkT2JqZWN0KHF1ZXJ5KSwgJ3VzZVF1ZXJpZXMgZG9lcyBub3Qgc3RyaW5naWZ5IG5lc3RlZCBxdWVyeSBvYmplY3RzIGJ5IGRlZmF1bHQ7ICcgKyAndXNlIGEgY3VzdG9tIHN0cmluZ2lmeVF1ZXJ5IGZ1bmN0aW9uJykgOiB1bmRlZmluZWQ7XG5cbiAgICAgIGlmICh0eXBlb2YgbG9jYXRpb24gPT09ICdzdHJpbmcnKSBsb2NhdGlvbiA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKGxvY2F0aW9uKTtcblxuICAgICAgdmFyIHNlYXJjaEJhc2UgPSB1bmRlZmluZWQ7XG4gICAgICBpZiAoc2VhcmNoQmFzZVNwZWMgJiYgbG9jYXRpb24uc2VhcmNoID09PSBzZWFyY2hCYXNlU3BlYy5zZWFyY2gpIHtcbiAgICAgICAgc2VhcmNoQmFzZSA9IHNlYXJjaEJhc2VTcGVjLnNlYXJjaEJhc2U7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBzZWFyY2hCYXNlID0gbG9jYXRpb24uc2VhcmNoIHx8ICcnO1xuICAgICAgfVxuXG4gICAgICB2YXIgc2VhcmNoID0gc2VhcmNoQmFzZTtcbiAgICAgIGlmIChxdWVyeVN0cmluZykge1xuICAgICAgICBzZWFyY2ggKz0gKHNlYXJjaCA/ICcmJyA6ICc/JykgKyBxdWVyeVN0cmluZztcbiAgICAgIH1cblxuICAgICAgcmV0dXJuIF9leHRlbmRzKHt9LCBsb2NhdGlvbiwgKF9leHRlbmRzMiA9IHtcbiAgICAgICAgc2VhcmNoOiBzZWFyY2hcbiAgICAgIH0sIF9leHRlbmRzMltTRUFSQ0hfQkFTRV9LRVldID0geyBzZWFyY2g6IHNlYXJjaCwgc2VhcmNoQmFzZTogc2VhcmNoQmFzZSB9LCBfZXh0ZW5kczIpKTtcbiAgICB9XG5cbiAgICAvLyBPdmVycmlkZSBhbGwgcmVhZCBtZXRob2RzIHdpdGggcXVlcnktYXdhcmUgdmVyc2lvbnMuXG4gICAgZnVuY3Rpb24gbGlzdGVuQmVmb3JlKGhvb2spIHtcbiAgICAgIHJldHVybiBoaXN0b3J5Lmxpc3RlbkJlZm9yZShmdW5jdGlvbiAobG9jYXRpb24sIGNhbGxiYWNrKSB7XG4gICAgICAgIF9ydW5UcmFuc2l0aW9uSG9vazJbJ2RlZmF1bHQnXShob29rLCBhZGRRdWVyeShsb2NhdGlvbiksIGNhbGxiYWNrKTtcbiAgICAgIH0pO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGxpc3RlbihsaXN0ZW5lcikge1xuICAgICAgcmV0dXJuIGhpc3RvcnkubGlzdGVuKGZ1bmN0aW9uIChsb2NhdGlvbikge1xuICAgICAgICBsaXN0ZW5lcihhZGRRdWVyeShsb2NhdGlvbikpO1xuICAgICAgfSk7XG4gICAgfVxuXG4gICAgLy8gT3ZlcnJpZGUgYWxsIHdyaXRlIG1ldGhvZHMgd2l0aCBxdWVyeS1hd2FyZSB2ZXJzaW9ucy5cbiAgICBmdW5jdGlvbiBwdXNoKGxvY2F0aW9uKSB7XG4gICAgICBoaXN0b3J5LnB1c2goYXBwZW5kUXVlcnkobG9jYXRpb24sIGxvY2F0aW9uLnF1ZXJ5KSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gcmVwbGFjZShsb2NhdGlvbikge1xuICAgICAgaGlzdG9yeS5yZXBsYWNlKGFwcGVuZFF1ZXJ5KGxvY2F0aW9uLCBsb2NhdGlvbi5xdWVyeSkpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGNyZWF0ZVBhdGgobG9jYXRpb24sIHF1ZXJ5KSB7XG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oIXF1ZXJ5LCAndGhlIHF1ZXJ5IGFyZ3VtZW50IHRvIGNyZWF0ZVBhdGggaXMgZGVwcmVjYXRlZDsgdXNlIGEgbG9jYXRpb24gZGVzY3JpcHRvciBpbnN0ZWFkJykgOiB1bmRlZmluZWQ7XG5cbiAgICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZVBhdGgoYXBwZW5kUXVlcnkobG9jYXRpb24sIHF1ZXJ5IHx8IGxvY2F0aW9uLnF1ZXJ5KSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gY3JlYXRlSHJlZihsb2NhdGlvbiwgcXVlcnkpIHtcbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXSghcXVlcnksICd0aGUgcXVlcnkgYXJndW1lbnQgdG8gY3JlYXRlSHJlZiBpcyBkZXByZWNhdGVkOyB1c2UgYSBsb2NhdGlvbiBkZXNjcmlwdG9yIGluc3RlYWQnKSA6IHVuZGVmaW5lZDtcblxuICAgICAgcmV0dXJuIGhpc3RvcnkuY3JlYXRlSHJlZihhcHBlbmRRdWVyeShsb2NhdGlvbiwgcXVlcnkgfHwgbG9jYXRpb24ucXVlcnkpKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjcmVhdGVMb2NhdGlvbihsb2NhdGlvbikge1xuICAgICAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIGFyZ3MgPSBBcnJheShfbGVuID4gMSA/IF9sZW4gLSAxIDogMCksIF9rZXkgPSAxOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgICAgIGFyZ3NbX2tleSAtIDFdID0gYXJndW1lbnRzW19rZXldO1xuICAgICAgfVxuXG4gICAgICB2YXIgZnVsbExvY2F0aW9uID0gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbi5hcHBseShoaXN0b3J5LCBbYXBwZW5kUXVlcnkobG9jYXRpb24sIGxvY2F0aW9uLnF1ZXJ5KV0uY29uY2F0KGFyZ3MpKTtcbiAgICAgIGlmIChsb2NhdGlvbi5xdWVyeSkge1xuICAgICAgICBmdWxsTG9jYXRpb24ucXVlcnkgPSBsb2NhdGlvbi5xdWVyeTtcbiAgICAgIH1cbiAgICAgIHJldHVybiBhZGRRdWVyeShmdWxsTG9jYXRpb24pO1xuICAgIH1cblxuICAgIC8vIGRlcHJlY2F0ZWRcbiAgICBmdW5jdGlvbiBwdXNoU3RhdGUoc3RhdGUsIHBhdGgsIHF1ZXJ5KSB7XG4gICAgICBpZiAodHlwZW9mIHBhdGggPT09ICdzdHJpbmcnKSBwYXRoID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICAgIHB1c2goX2V4dGVuZHMoeyBzdGF0ZTogc3RhdGUgfSwgcGF0aCwgeyBxdWVyeTogcXVlcnkgfSkpO1xuICAgIH1cblxuICAgIC8vIGRlcHJlY2F0ZWRcbiAgICBmdW5jdGlvbiByZXBsYWNlU3RhdGUoc3RhdGUsIHBhdGgsIHF1ZXJ5KSB7XG4gICAgICBpZiAodHlwZW9mIHBhdGggPT09ICdzdHJpbmcnKSBwYXRoID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICAgIHJlcGxhY2UoX2V4dGVuZHMoeyBzdGF0ZTogc3RhdGUgfSwgcGF0aCwgeyBxdWVyeTogcXVlcnkgfSkpO1xuICAgIH1cblxuICAgIHJldHVybiBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwge1xuICAgICAgbGlzdGVuQmVmb3JlOiBsaXN0ZW5CZWZvcmUsXG4gICAgICBsaXN0ZW46IGxpc3RlbixcbiAgICAgIHB1c2g6IHB1c2gsXG4gICAgICByZXBsYWNlOiByZXBsYWNlLFxuICAgICAgY3JlYXRlUGF0aDogY3JlYXRlUGF0aCxcbiAgICAgIGNyZWF0ZUhyZWY6IGNyZWF0ZUhyZWYsXG4gICAgICBjcmVhdGVMb2NhdGlvbjogY3JlYXRlTG9jYXRpb24sXG5cbiAgICAgIHB1c2hTdGF0ZTogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShwdXNoU3RhdGUsICdwdXNoU3RhdGUgaXMgZGVwcmVjYXRlZDsgdXNlIHB1c2ggaW5zdGVhZCcpLFxuICAgICAgcmVwbGFjZVN0YXRlOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHJlcGxhY2VTdGF0ZSwgJ3JlcGxhY2VTdGF0ZSBpcyBkZXByZWNhdGVkOyB1c2UgcmVwbGFjZSBpbnN0ZWFkJylcbiAgICB9KTtcbiAgfTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gdXNlUXVlcmllcztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIi8qKlxuICogQ29weXJpZ2h0IDIwMTQtMjAxNSwgRmFjZWJvb2ssIEluYy5cbiAqIEFsbCByaWdodHMgcmVzZXJ2ZWQuXG4gKlxuICogVGhpcyBzb3VyY2UgY29kZSBpcyBsaWNlbnNlZCB1bmRlciB0aGUgQlNELXN0eWxlIGxpY2Vuc2UgZm91bmQgaW4gdGhlXG4gKiBMSUNFTlNFIGZpbGUgaW4gdGhlIHJvb3QgZGlyZWN0b3J5IG9mIHRoaXMgc291cmNlIHRyZWUuIEFuIGFkZGl0aW9uYWwgZ3JhbnRcbiAqIG9mIHBhdGVudCByaWdodHMgY2FuIGJlIGZvdW5kIGluIHRoZSBQQVRFTlRTIGZpbGUgaW4gdGhlIHNhbWUgZGlyZWN0b3J5LlxuICovXG5cbid1c2Ugc3RyaWN0JztcblxuLyoqXG4gKiBTaW1pbGFyIHRvIGludmFyaWFudCBidXQgb25seSBsb2dzIGEgd2FybmluZyBpZiB0aGUgY29uZGl0aW9uIGlzIG5vdCBtZXQuXG4gKiBUaGlzIGNhbiBiZSB1c2VkIHRvIGxvZyBpc3N1ZXMgaW4gZGV2ZWxvcG1lbnQgZW52aXJvbm1lbnRzIGluIGNyaXRpY2FsXG4gKiBwYXRocy4gUmVtb3ZpbmcgdGhlIGxvZ2dpbmcgY29kZSBmb3IgcHJvZHVjdGlvbiBlbnZpcm9ubWVudHMgd2lsbCBrZWVwIHRoZVxuICogc2FtZSBsb2dpYyBhbmQgZm9sbG93IHRoZSBzYW1lIGNvZGUgcGF0aHMuXG4gKi9cblxudmFyIHdhcm5pbmcgPSBmdW5jdGlvbigpIHt9O1xuXG5pZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICB3YXJuaW5nID0gZnVuY3Rpb24oY29uZGl0aW9uLCBmb3JtYXQsIGFyZ3MpIHtcbiAgICB2YXIgbGVuID0gYXJndW1lbnRzLmxlbmd0aDtcbiAgICBhcmdzID0gbmV3IEFycmF5KGxlbiA+IDIgPyBsZW4gLSAyIDogMCk7XG4gICAgZm9yICh2YXIga2V5ID0gMjsga2V5IDwgbGVuOyBrZXkrKykge1xuICAgICAgYXJnc1trZXkgLSAyXSA9IGFyZ3VtZW50c1trZXldO1xuICAgIH1cbiAgICBpZiAoZm9ybWF0ID09PSB1bmRlZmluZWQpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihcbiAgICAgICAgJ2B3YXJuaW5nKGNvbmRpdGlvbiwgZm9ybWF0LCAuLi5hcmdzKWAgcmVxdWlyZXMgYSB3YXJuaW5nICcgK1xuICAgICAgICAnbWVzc2FnZSBhcmd1bWVudCdcbiAgICAgICk7XG4gICAgfVxuXG4gICAgaWYgKGZvcm1hdC5sZW5ndGggPCAxMCB8fCAoL15bc1xcV10qJC8pLnRlc3QoZm9ybWF0KSkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKFxuICAgICAgICAnVGhlIHdhcm5pbmcgZm9ybWF0IHNob3VsZCBiZSBhYmxlIHRvIHVuaXF1ZWx5IGlkZW50aWZ5IHRoaXMgJyArXG4gICAgICAgICd3YXJuaW5nLiBQbGVhc2UsIHVzZSBhIG1vcmUgZGVzY3JpcHRpdmUgZm9ybWF0IHRoYW46ICcgKyBmb3JtYXRcbiAgICAgICk7XG4gICAgfVxuXG4gICAgaWYgKCFjb25kaXRpb24pIHtcbiAgICAgIHZhciBhcmdJbmRleCA9IDA7XG4gICAgICB2YXIgbWVzc2FnZSA9ICdXYXJuaW5nOiAnICtcbiAgICAgICAgZm9ybWF0LnJlcGxhY2UoLyVzL2csIGZ1bmN0aW9uKCkge1xuICAgICAgICAgIHJldHVybiBhcmdzW2FyZ0luZGV4KytdO1xuICAgICAgICB9KTtcbiAgICAgIGlmICh0eXBlb2YgY29uc29sZSAhPT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgICAgY29uc29sZS5lcnJvcihtZXNzYWdlKTtcbiAgICAgIH1cbiAgICAgIHRyeSB7XG4gICAgICAgIC8vIFRoaXMgZXJyb3Igd2FzIHRocm93biBhcyBhIGNvbnZlbmllbmNlIHNvIHRoYXQgeW91IGNhbiB1c2UgdGhpcyBzdGFja1xuICAgICAgICAvLyB0byBmaW5kIHRoZSBjYWxsc2l0ZSB0aGF0IGNhdXNlZCB0aGlzIHdhcm5pbmcgdG8gZmlyZS5cbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKG1lc3NhZ2UpO1xuICAgICAgfSBjYXRjaCh4KSB7fVxuICAgIH1cbiAgfTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSB3YXJuaW5nO1xuIiwiLyoqXG4gKiBDb3B5cmlnaHQgMjAxNSwgWWFob28hIEluYy5cbiAqIENvcHlyaWdodHMgbGljZW5zZWQgdW5kZXIgdGhlIE5ldyBCU0QgTGljZW5zZS4gU2VlIHRoZSBhY2NvbXBhbnlpbmcgTElDRU5TRSBmaWxlIGZvciB0ZXJtcy5cbiAqL1xuJ3VzZSBzdHJpY3QnO1xuXG52YXIgUkVBQ1RfU1RBVElDUyA9IHtcbiAgICBjaGlsZENvbnRleHRUeXBlczogdHJ1ZSxcbiAgICBjb250ZXh0VHlwZXM6IHRydWUsXG4gICAgZGVmYXVsdFByb3BzOiB0cnVlLFxuICAgIGRpc3BsYXlOYW1lOiB0cnVlLFxuICAgIGdldERlZmF1bHRQcm9wczogdHJ1ZSxcbiAgICBtaXhpbnM6IHRydWUsXG4gICAgcHJvcFR5cGVzOiB0cnVlLFxuICAgIHR5cGU6IHRydWVcbn07XG5cbnZhciBLTk9XTl9TVEFUSUNTID0ge1xuICAgIG5hbWU6IHRydWUsXG4gICAgbGVuZ3RoOiB0cnVlLFxuICAgIHByb3RvdHlwZTogdHJ1ZSxcbiAgICBjYWxsZXI6IHRydWUsXG4gICAgYXJndW1lbnRzOiB0cnVlLFxuICAgIGFyaXR5OiB0cnVlXG59O1xuXG52YXIgaXNHZXRPd25Qcm9wZXJ0eVN5bWJvbHNBdmFpbGFibGUgPSB0eXBlb2YgT2JqZWN0LmdldE93blByb3BlcnR5U3ltYm9scyA9PT0gJ2Z1bmN0aW9uJztcblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiBob2lzdE5vblJlYWN0U3RhdGljcyh0YXJnZXRDb21wb25lbnQsIHNvdXJjZUNvbXBvbmVudCwgY3VzdG9tU3RhdGljcykge1xuICAgIGlmICh0eXBlb2Ygc291cmNlQ29tcG9uZW50ICE9PSAnc3RyaW5nJykgeyAvLyBkb24ndCBob2lzdCBvdmVyIHN0cmluZyAoaHRtbCkgY29tcG9uZW50c1xuICAgICAgICB2YXIga2V5cyA9IE9iamVjdC5nZXRPd25Qcm9wZXJ0eU5hbWVzKHNvdXJjZUNvbXBvbmVudCk7XG5cbiAgICAgICAgLyogaXN0YW5idWwgaWdub3JlIGVsc2UgKi9cbiAgICAgICAgaWYgKGlzR2V0T3duUHJvcGVydHlTeW1ib2xzQXZhaWxhYmxlKSB7XG4gICAgICAgICAgICBrZXlzID0ga2V5cy5jb25jYXQoT2JqZWN0LmdldE93blByb3BlcnR5U3ltYm9scyhzb3VyY2VDb21wb25lbnQpKTtcbiAgICAgICAgfVxuXG4gICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwga2V5cy5sZW5ndGg7ICsraSkge1xuICAgICAgICAgICAgaWYgKCFSRUFDVF9TVEFUSUNTW2tleXNbaV1dICYmICFLTk9XTl9TVEFUSUNTW2tleXNbaV1dICYmICghY3VzdG9tU3RhdGljcyB8fCAhY3VzdG9tU3RhdGljc1trZXlzW2ldXSkpIHtcbiAgICAgICAgICAgICAgICB0cnkge1xuICAgICAgICAgICAgICAgICAgICB0YXJnZXRDb21wb25lbnRba2V5c1tpXV0gPSBzb3VyY2VDb21wb25lbnRba2V5c1tpXV07XG4gICAgICAgICAgICAgICAgfSBjYXRjaCAoZXJyb3IpIHtcblxuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH1cblxuICAgIHJldHVybiB0YXJnZXRDb21wb25lbnQ7XG59O1xuIiwiLyoqXG4gKiBDb3B5cmlnaHQgMjAxMy0yMDE1LCBGYWNlYm9vaywgSW5jLlxuICogQWxsIHJpZ2h0cyByZXNlcnZlZC5cbiAqXG4gKiBUaGlzIHNvdXJjZSBjb2RlIGlzIGxpY2Vuc2VkIHVuZGVyIHRoZSBCU0Qtc3R5bGUgbGljZW5zZSBmb3VuZCBpbiB0aGVcbiAqIExJQ0VOU0UgZmlsZSBpbiB0aGUgcm9vdCBkaXJlY3Rvcnkgb2YgdGhpcyBzb3VyY2UgdHJlZS4gQW4gYWRkaXRpb25hbCBncmFudFxuICogb2YgcGF0ZW50IHJpZ2h0cyBjYW4gYmUgZm91bmQgaW4gdGhlIFBBVEVOVFMgZmlsZSBpbiB0aGUgc2FtZSBkaXJlY3RvcnkuXG4gKi9cblxuJ3VzZSBzdHJpY3QnO1xuXG4vKipcbiAqIFVzZSBpbnZhcmlhbnQoKSB0byBhc3NlcnQgc3RhdGUgd2hpY2ggeW91ciBwcm9ncmFtIGFzc3VtZXMgdG8gYmUgdHJ1ZS5cbiAqXG4gKiBQcm92aWRlIHNwcmludGYtc3R5bGUgZm9ybWF0IChvbmx5ICVzIGlzIHN1cHBvcnRlZCkgYW5kIGFyZ3VtZW50c1xuICogdG8gcHJvdmlkZSBpbmZvcm1hdGlvbiBhYm91dCB3aGF0IGJyb2tlIGFuZCB3aGF0IHlvdSB3ZXJlXG4gKiBleHBlY3RpbmcuXG4gKlxuICogVGhlIGludmFyaWFudCBtZXNzYWdlIHdpbGwgYmUgc3RyaXBwZWQgaW4gcHJvZHVjdGlvbiwgYnV0IHRoZSBpbnZhcmlhbnRcbiAqIHdpbGwgcmVtYWluIHRvIGVuc3VyZSBsb2dpYyBkb2VzIG5vdCBkaWZmZXIgaW4gcHJvZHVjdGlvbi5cbiAqL1xuXG52YXIgaW52YXJpYW50ID0gZnVuY3Rpb24oY29uZGl0aW9uLCBmb3JtYXQsIGEsIGIsIGMsIGQsIGUsIGYpIHtcbiAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICBpZiAoZm9ybWF0ID09PSB1bmRlZmluZWQpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignaW52YXJpYW50IHJlcXVpcmVzIGFuIGVycm9yIG1lc3NhZ2UgYXJndW1lbnQnKTtcbiAgICB9XG4gIH1cblxuICBpZiAoIWNvbmRpdGlvbikge1xuICAgIHZhciBlcnJvcjtcbiAgICBpZiAoZm9ybWF0ID09PSB1bmRlZmluZWQpIHtcbiAgICAgIGVycm9yID0gbmV3IEVycm9yKFxuICAgICAgICAnTWluaWZpZWQgZXhjZXB0aW9uIG9jY3VycmVkOyB1c2UgdGhlIG5vbi1taW5pZmllZCBkZXYgZW52aXJvbm1lbnQgJyArXG4gICAgICAgICdmb3IgdGhlIGZ1bGwgZXJyb3IgbWVzc2FnZSBhbmQgYWRkaXRpb25hbCBoZWxwZnVsIHdhcm5pbmdzLidcbiAgICAgICk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHZhciBhcmdzID0gW2EsIGIsIGMsIGQsIGUsIGZdO1xuICAgICAgdmFyIGFyZ0luZGV4ID0gMDtcbiAgICAgIGVycm9yID0gbmV3IEVycm9yKFxuICAgICAgICBmb3JtYXQucmVwbGFjZSgvJXMvZywgZnVuY3Rpb24oKSB7IHJldHVybiBhcmdzW2FyZ0luZGV4KytdOyB9KVxuICAgICAgKTtcbiAgICAgIGVycm9yLm5hbWUgPSAnSW52YXJpYW50IFZpb2xhdGlvbic7XG4gICAgfVxuXG4gICAgZXJyb3IuZnJhbWVzVG9Qb3AgPSAxOyAvLyB3ZSBkb24ndCBjYXJlIGFib3V0IGludmFyaWFudCdzIG93biBmcmFtZVxuICAgIHRocm93IGVycm9yO1xuICB9XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IGludmFyaWFudDtcbiIsIm1vZHVsZS5leHBvcnRzID0gaXNGdW5jdGlvblxuXG52YXIgdG9TdHJpbmcgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nXG5cbmZ1bmN0aW9uIGlzRnVuY3Rpb24gKGZuKSB7XG4gIHZhciBzdHJpbmcgPSB0b1N0cmluZy5jYWxsKGZuKVxuICByZXR1cm4gc3RyaW5nID09PSAnW29iamVjdCBGdW5jdGlvbl0nIHx8XG4gICAgKHR5cGVvZiBmbiA9PT0gJ2Z1bmN0aW9uJyAmJiBzdHJpbmcgIT09ICdbb2JqZWN0IFJlZ0V4cF0nKSB8fFxuICAgICh0eXBlb2Ygd2luZG93ICE9PSAndW5kZWZpbmVkJyAmJlxuICAgICAvLyBJRTggYW5kIGJlbG93XG4gICAgIChmbiA9PT0gd2luZG93LnNldFRpbWVvdXQgfHxcbiAgICAgIGZuID09PSB3aW5kb3cuYWxlcnQgfHxcbiAgICAgIGZuID09PSB3aW5kb3cuY29uZmlybSB8fFxuICAgICAgZm4gPT09IHdpbmRvdy5wcm9tcHQpKVxufTtcbiIsInZhciBvdmVyQXJnID0gcmVxdWlyZSgnLi9fb3ZlckFyZycpO1xuXG4vKiogQnVpbHQtaW4gdmFsdWUgcmVmZXJlbmNlcy4gKi9cbnZhciBnZXRQcm90b3R5cGUgPSBvdmVyQXJnKE9iamVjdC5nZXRQcm90b3R5cGVPZiwgT2JqZWN0KTtcblxubW9kdWxlLmV4cG9ydHMgPSBnZXRQcm90b3R5cGU7XG4iLCIvKipcbiAqIENoZWNrcyBpZiBgdmFsdWVgIGlzIGEgaG9zdCBvYmplY3QgaW4gSUUgPCA5LlxuICpcbiAqIEBwcml2YXRlXG4gKiBAcGFyYW0geyp9IHZhbHVlIFRoZSB2YWx1ZSB0byBjaGVjay5cbiAqIEByZXR1cm5zIHtib29sZWFufSBSZXR1cm5zIGB0cnVlYCBpZiBgdmFsdWVgIGlzIGEgaG9zdCBvYmplY3QsIGVsc2UgYGZhbHNlYC5cbiAqL1xuZnVuY3Rpb24gaXNIb3N0T2JqZWN0KHZhbHVlKSB7XG4gIC8vIE1hbnkgaG9zdCBvYmplY3RzIGFyZSBgT2JqZWN0YCBvYmplY3RzIHRoYXQgY2FuIGNvZXJjZSB0byBzdHJpbmdzXG4gIC8vIGRlc3BpdGUgaGF2aW5nIGltcHJvcGVybHkgZGVmaW5lZCBgdG9TdHJpbmdgIG1ldGhvZHMuXG4gIHZhciByZXN1bHQgPSBmYWxzZTtcbiAgaWYgKHZhbHVlICE9IG51bGwgJiYgdHlwZW9mIHZhbHVlLnRvU3RyaW5nICE9ICdmdW5jdGlvbicpIHtcbiAgICB0cnkge1xuICAgICAgcmVzdWx0ID0gISEodmFsdWUgKyAnJyk7XG4gICAgfSBjYXRjaCAoZSkge31cbiAgfVxuICByZXR1cm4gcmVzdWx0O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGlzSG9zdE9iamVjdDtcbiIsIi8qKlxuICogQ3JlYXRlcyBhIHVuYXJ5IGZ1bmN0aW9uIHRoYXQgaW52b2tlcyBgZnVuY2Agd2l0aCBpdHMgYXJndW1lbnQgdHJhbnNmb3JtZWQuXG4gKlxuICogQHByaXZhdGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGZ1bmMgVGhlIGZ1bmN0aW9uIHRvIHdyYXAuXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSB0cmFuc2Zvcm0gVGhlIGFyZ3VtZW50IHRyYW5zZm9ybS5cbiAqIEByZXR1cm5zIHtGdW5jdGlvbn0gUmV0dXJucyB0aGUgbmV3IGZ1bmN0aW9uLlxuICovXG5mdW5jdGlvbiBvdmVyQXJnKGZ1bmMsIHRyYW5zZm9ybSkge1xuICByZXR1cm4gZnVuY3Rpb24oYXJnKSB7XG4gICAgcmV0dXJuIGZ1bmModHJhbnNmb3JtKGFyZykpO1xuICB9O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IG92ZXJBcmc7XG4iLCIvKipcbiAqIENoZWNrcyBpZiBgdmFsdWVgIGlzIG9iamVjdC1saWtlLiBBIHZhbHVlIGlzIG9iamVjdC1saWtlIGlmIGl0J3Mgbm90IGBudWxsYFxuICogYW5kIGhhcyBhIGB0eXBlb2ZgIHJlc3VsdCBvZiBcIm9iamVjdFwiLlxuICpcbiAqIEBzdGF0aWNcbiAqIEBtZW1iZXJPZiBfXG4gKiBAc2luY2UgNC4wLjBcbiAqIEBjYXRlZ29yeSBMYW5nXG4gKiBAcGFyYW0geyp9IHZhbHVlIFRoZSB2YWx1ZSB0byBjaGVjay5cbiAqIEByZXR1cm5zIHtib29sZWFufSBSZXR1cm5zIGB0cnVlYCBpZiBgdmFsdWVgIGlzIG9iamVjdC1saWtlLCBlbHNlIGBmYWxzZWAuXG4gKiBAZXhhbXBsZVxuICpcbiAqIF8uaXNPYmplY3RMaWtlKHt9KTtcbiAqIC8vID0+IHRydWVcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZShbMSwgMiwgM10pO1xuICogLy8gPT4gdHJ1ZVxuICpcbiAqIF8uaXNPYmplY3RMaWtlKF8ubm9vcCk7XG4gKiAvLyA9PiBmYWxzZVxuICpcbiAqIF8uaXNPYmplY3RMaWtlKG51bGwpO1xuICogLy8gPT4gZmFsc2VcbiAqL1xuZnVuY3Rpb24gaXNPYmplY3RMaWtlKHZhbHVlKSB7XG4gIHJldHVybiAhIXZhbHVlICYmIHR5cGVvZiB2YWx1ZSA9PSAnb2JqZWN0Jztcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBpc09iamVjdExpa2U7XG4iLCJ2YXIgZ2V0UHJvdG90eXBlID0gcmVxdWlyZSgnLi9fZ2V0UHJvdG90eXBlJyksXG4gICAgaXNIb3N0T2JqZWN0ID0gcmVxdWlyZSgnLi9faXNIb3N0T2JqZWN0JyksXG4gICAgaXNPYmplY3RMaWtlID0gcmVxdWlyZSgnLi9pc09iamVjdExpa2UnKTtcblxuLyoqIGBPYmplY3QjdG9TdHJpbmdgIHJlc3VsdCByZWZlcmVuY2VzLiAqL1xudmFyIG9iamVjdFRhZyA9ICdbb2JqZWN0IE9iamVjdF0nO1xuXG4vKiogVXNlZCBmb3IgYnVpbHQtaW4gbWV0aG9kIHJlZmVyZW5jZXMuICovXG52YXIgb2JqZWN0UHJvdG8gPSBPYmplY3QucHJvdG90eXBlO1xuXG4vKiogVXNlZCB0byByZXNvbHZlIHRoZSBkZWNvbXBpbGVkIHNvdXJjZSBvZiBmdW5jdGlvbnMuICovXG52YXIgZnVuY1RvU3RyaW5nID0gRnVuY3Rpb24ucHJvdG90eXBlLnRvU3RyaW5nO1xuXG4vKiogVXNlZCB0byBjaGVjayBvYmplY3RzIGZvciBvd24gcHJvcGVydGllcy4gKi9cbnZhciBoYXNPd25Qcm9wZXJ0eSA9IG9iamVjdFByb3RvLmhhc093blByb3BlcnR5O1xuXG4vKiogVXNlZCB0byBpbmZlciB0aGUgYE9iamVjdGAgY29uc3RydWN0b3IuICovXG52YXIgb2JqZWN0Q3RvclN0cmluZyA9IGZ1bmNUb1N0cmluZy5jYWxsKE9iamVjdCk7XG5cbi8qKlxuICogVXNlZCB0byByZXNvbHZlIHRoZVxuICogW2B0b1N0cmluZ1RhZ2BdKGh0dHA6Ly9lY21hLWludGVybmF0aW9uYWwub3JnL2VjbWEtMjYyLzcuMC8jc2VjLW9iamVjdC5wcm90b3R5cGUudG9zdHJpbmcpXG4gKiBvZiB2YWx1ZXMuXG4gKi9cbnZhciBvYmplY3RUb1N0cmluZyA9IG9iamVjdFByb3RvLnRvU3RyaW5nO1xuXG4vKipcbiAqIENoZWNrcyBpZiBgdmFsdWVgIGlzIGEgcGxhaW4gb2JqZWN0LCB0aGF0IGlzLCBhbiBvYmplY3QgY3JlYXRlZCBieSB0aGVcbiAqIGBPYmplY3RgIGNvbnN0cnVjdG9yIG9yIG9uZSB3aXRoIGEgYFtbUHJvdG90eXBlXV1gIG9mIGBudWxsYC5cbiAqXG4gKiBAc3RhdGljXG4gKiBAbWVtYmVyT2YgX1xuICogQHNpbmNlIDAuOC4wXG4gKiBAY2F0ZWdvcnkgTGFuZ1xuICogQHBhcmFtIHsqfSB2YWx1ZSBUaGUgdmFsdWUgdG8gY2hlY2suXG4gKiBAcmV0dXJucyB7Ym9vbGVhbn0gUmV0dXJucyBgdHJ1ZWAgaWYgYHZhbHVlYCBpcyBhIHBsYWluIG9iamVjdCwgZWxzZSBgZmFsc2VgLlxuICogQGV4YW1wbGVcbiAqXG4gKiBmdW5jdGlvbiBGb28oKSB7XG4gKiAgIHRoaXMuYSA9IDE7XG4gKiB9XG4gKlxuICogXy5pc1BsYWluT2JqZWN0KG5ldyBGb28pO1xuICogLy8gPT4gZmFsc2VcbiAqXG4gKiBfLmlzUGxhaW5PYmplY3QoWzEsIDIsIDNdKTtcbiAqIC8vID0+IGZhbHNlXG4gKlxuICogXy5pc1BsYWluT2JqZWN0KHsgJ3gnOiAwLCAneSc6IDAgfSk7XG4gKiAvLyA9PiB0cnVlXG4gKlxuICogXy5pc1BsYWluT2JqZWN0KE9iamVjdC5jcmVhdGUobnVsbCkpO1xuICogLy8gPT4gdHJ1ZVxuICovXG5mdW5jdGlvbiBpc1BsYWluT2JqZWN0KHZhbHVlKSB7XG4gIGlmICghaXNPYmplY3RMaWtlKHZhbHVlKSB8fFxuICAgICAgb2JqZWN0VG9TdHJpbmcuY2FsbCh2YWx1ZSkgIT0gb2JqZWN0VGFnIHx8IGlzSG9zdE9iamVjdCh2YWx1ZSkpIHtcbiAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgdmFyIHByb3RvID0gZ2V0UHJvdG90eXBlKHZhbHVlKTtcbiAgaWYgKHByb3RvID09PSBudWxsKSB7XG4gICAgcmV0dXJuIHRydWU7XG4gIH1cbiAgdmFyIEN0b3IgPSBoYXNPd25Qcm9wZXJ0eS5jYWxsKHByb3RvLCAnY29uc3RydWN0b3InKSAmJiBwcm90by5jb25zdHJ1Y3RvcjtcbiAgcmV0dXJuICh0eXBlb2YgQ3RvciA9PSAnZnVuY3Rpb24nICYmXG4gICAgQ3RvciBpbnN0YW5jZW9mIEN0b3IgJiYgZnVuY1RvU3RyaW5nLmNhbGwoQ3RvcikgPT0gb2JqZWN0Q3RvclN0cmluZyk7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gaXNQbGFpbk9iamVjdDtcbiIsInZhciB0cmltID0gcmVxdWlyZSgndHJpbScpXG4gICwgZm9yRWFjaCA9IHJlcXVpcmUoJ2Zvci1lYWNoJylcbiAgLCBpc0FycmF5ID0gZnVuY3Rpb24oYXJnKSB7XG4gICAgICByZXR1cm4gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKGFyZykgPT09ICdbb2JqZWN0IEFycmF5XSc7XG4gICAgfVxuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uIChoZWFkZXJzKSB7XG4gIGlmICghaGVhZGVycylcbiAgICByZXR1cm4ge31cblxuICB2YXIgcmVzdWx0ID0ge31cblxuICBmb3JFYWNoKFxuICAgICAgdHJpbShoZWFkZXJzKS5zcGxpdCgnXFxuJylcbiAgICAsIGZ1bmN0aW9uIChyb3cpIHtcbiAgICAgICAgdmFyIGluZGV4ID0gcm93LmluZGV4T2YoJzonKVxuICAgICAgICAgICwga2V5ID0gdHJpbShyb3cuc2xpY2UoMCwgaW5kZXgpKS50b0xvd2VyQ2FzZSgpXG4gICAgICAgICAgLCB2YWx1ZSA9IHRyaW0ocm93LnNsaWNlKGluZGV4ICsgMSkpXG5cbiAgICAgICAgaWYgKHR5cGVvZihyZXN1bHRba2V5XSkgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICAgICAgcmVzdWx0W2tleV0gPSB2YWx1ZVxuICAgICAgICB9IGVsc2UgaWYgKGlzQXJyYXkocmVzdWx0W2tleV0pKSB7XG4gICAgICAgICAgcmVzdWx0W2tleV0ucHVzaCh2YWx1ZSlcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICByZXN1bHRba2V5XSA9IFsgcmVzdWx0W2tleV0sIHZhbHVlIF1cbiAgICAgICAgfVxuICAgICAgfVxuICApXG5cbiAgcmV0dXJuIHJlc3VsdFxufSIsIid1c2Ugc3RyaWN0JztcbnZhciBzdHJpY3RVcmlFbmNvZGUgPSByZXF1aXJlKCdzdHJpY3QtdXJpLWVuY29kZScpO1xuXG5leHBvcnRzLmV4dHJhY3QgPSBmdW5jdGlvbiAoc3RyKSB7XG5cdHJldHVybiBzdHIuc3BsaXQoJz8nKVsxXSB8fCAnJztcbn07XG5cbmV4cG9ydHMucGFyc2UgPSBmdW5jdGlvbiAoc3RyKSB7XG5cdGlmICh0eXBlb2Ygc3RyICE9PSAnc3RyaW5nJykge1xuXHRcdHJldHVybiB7fTtcblx0fVxuXG5cdHN0ciA9IHN0ci50cmltKCkucmVwbGFjZSgvXihcXD98I3wmKS8sICcnKTtcblxuXHRpZiAoIXN0cikge1xuXHRcdHJldHVybiB7fTtcblx0fVxuXG5cdHJldHVybiBzdHIuc3BsaXQoJyYnKS5yZWR1Y2UoZnVuY3Rpb24gKHJldCwgcGFyYW0pIHtcblx0XHR2YXIgcGFydHMgPSBwYXJhbS5yZXBsYWNlKC9cXCsvZywgJyAnKS5zcGxpdCgnPScpO1xuXHRcdC8vIEZpcmVmb3ggKHByZSA0MCkgZGVjb2RlcyBgJTNEYCB0byBgPWBcblx0XHQvLyBodHRwczovL2dpdGh1Yi5jb20vc2luZHJlc29yaHVzL3F1ZXJ5LXN0cmluZy9wdWxsLzM3XG5cdFx0dmFyIGtleSA9IHBhcnRzLnNoaWZ0KCk7XG5cdFx0dmFyIHZhbCA9IHBhcnRzLmxlbmd0aCA+IDAgPyBwYXJ0cy5qb2luKCc9JykgOiB1bmRlZmluZWQ7XG5cblx0XHRrZXkgPSBkZWNvZGVVUklDb21wb25lbnQoa2V5KTtcblxuXHRcdC8vIG1pc3NpbmcgYD1gIHNob3VsZCBiZSBgbnVsbGA6XG5cdFx0Ly8gaHR0cDovL3czLm9yZy9UUi8yMDEyL1dELXVybC0yMDEyMDUyNC8jY29sbGVjdC11cmwtcGFyYW1ldGVyc1xuXHRcdHZhbCA9IHZhbCA9PT0gdW5kZWZpbmVkID8gbnVsbCA6IGRlY29kZVVSSUNvbXBvbmVudCh2YWwpO1xuXG5cdFx0aWYgKCFyZXQuaGFzT3duUHJvcGVydHkoa2V5KSkge1xuXHRcdFx0cmV0W2tleV0gPSB2YWw7XG5cdFx0fSBlbHNlIGlmIChBcnJheS5pc0FycmF5KHJldFtrZXldKSkge1xuXHRcdFx0cmV0W2tleV0ucHVzaCh2YWwpO1xuXHRcdH0gZWxzZSB7XG5cdFx0XHRyZXRba2V5XSA9IFtyZXRba2V5XSwgdmFsXTtcblx0XHR9XG5cblx0XHRyZXR1cm4gcmV0O1xuXHR9LCB7fSk7XG59O1xuXG5leHBvcnRzLnN0cmluZ2lmeSA9IGZ1bmN0aW9uIChvYmopIHtcblx0cmV0dXJuIG9iaiA/IE9iamVjdC5rZXlzKG9iaikuc29ydCgpLm1hcChmdW5jdGlvbiAoa2V5KSB7XG5cdFx0dmFyIHZhbCA9IG9ialtrZXldO1xuXG5cdFx0aWYgKHZhbCA9PT0gdW5kZWZpbmVkKSB7XG5cdFx0XHRyZXR1cm4gJyc7XG5cdFx0fVxuXG5cdFx0aWYgKHZhbCA9PT0gbnVsbCkge1xuXHRcdFx0cmV0dXJuIGtleTtcblx0XHR9XG5cblx0XHRpZiAoQXJyYXkuaXNBcnJheSh2YWwpKSB7XG5cdFx0XHRyZXR1cm4gdmFsLnNsaWNlKCkuc29ydCgpLm1hcChmdW5jdGlvbiAodmFsMikge1xuXHRcdFx0XHRyZXR1cm4gc3RyaWN0VXJpRW5jb2RlKGtleSkgKyAnPScgKyBzdHJpY3RVcmlFbmNvZGUodmFsMik7XG5cdFx0XHR9KS5qb2luKCcmJyk7XG5cdFx0fVxuXG5cdFx0cmV0dXJuIHN0cmljdFVyaUVuY29kZShrZXkpICsgJz0nICsgc3RyaWN0VXJpRW5jb2RlKHZhbCk7XG5cdH0pLmZpbHRlcihmdW5jdGlvbiAoeCkge1xuXHRcdHJldHVybiB4Lmxlbmd0aCA+IDA7XG5cdH0pLmpvaW4oJyYnKSA6ICcnO1xufTtcbiIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gdW5kZWZpbmVkO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9zdG9yZVNoYXBlID0gcmVxdWlyZSgnLi4vdXRpbHMvc3RvcmVTaGFwZScpO1xuXG52YXIgX3N0b3JlU2hhcGUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc3RvcmVTaGFwZSk7XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJy4uL3V0aWxzL3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3RvcikgeyBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkgeyB0aHJvdyBuZXcgVHlwZUVycm9yKFwiQ2Fubm90IGNhbGwgYSBjbGFzcyBhcyBhIGZ1bmN0aW9uXCIpOyB9IH1cblxuZnVuY3Rpb24gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4oc2VsZiwgY2FsbCkgeyBpZiAoIXNlbGYpIHsgdGhyb3cgbmV3IFJlZmVyZW5jZUVycm9yKFwidGhpcyBoYXNuJ3QgYmVlbiBpbml0aWFsaXNlZCAtIHN1cGVyKCkgaGFzbid0IGJlZW4gY2FsbGVkXCIpOyB9IHJldHVybiBjYWxsICYmICh0eXBlb2YgY2FsbCA9PT0gXCJvYmplY3RcIiB8fCB0eXBlb2YgY2FsbCA9PT0gXCJmdW5jdGlvblwiKSA/IGNhbGwgOiBzZWxmOyB9XG5cbmZ1bmN0aW9uIF9pbmhlcml0cyhzdWJDbGFzcywgc3VwZXJDbGFzcykgeyBpZiAodHlwZW9mIHN1cGVyQ2xhc3MgIT09IFwiZnVuY3Rpb25cIiAmJiBzdXBlckNsYXNzICE9PSBudWxsKSB7IHRocm93IG5ldyBUeXBlRXJyb3IoXCJTdXBlciBleHByZXNzaW9uIG11c3QgZWl0aGVyIGJlIG51bGwgb3IgYSBmdW5jdGlvbiwgbm90IFwiICsgdHlwZW9mIHN1cGVyQ2xhc3MpOyB9IHN1YkNsYXNzLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoc3VwZXJDbGFzcyAmJiBzdXBlckNsYXNzLnByb3RvdHlwZSwgeyBjb25zdHJ1Y3RvcjogeyB2YWx1ZTogc3ViQ2xhc3MsIGVudW1lcmFibGU6IGZhbHNlLCB3cml0YWJsZTogdHJ1ZSwgY29uZmlndXJhYmxlOiB0cnVlIH0gfSk7IGlmIChzdXBlckNsYXNzKSBPYmplY3Quc2V0UHJvdG90eXBlT2YgPyBPYmplY3Quc2V0UHJvdG90eXBlT2Yoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIDogc3ViQ2xhc3MuX19wcm90b19fID0gc3VwZXJDbGFzczsgfVxuXG52YXIgZGlkV2FybkFib3V0UmVjZWl2aW5nU3RvcmUgPSBmYWxzZTtcbmZ1bmN0aW9uIHdhcm5BYm91dFJlY2VpdmluZ1N0b3JlKCkge1xuICBpZiAoZGlkV2FybkFib3V0UmVjZWl2aW5nU3RvcmUpIHtcbiAgICByZXR1cm47XG4gIH1cbiAgZGlkV2FybkFib3V0UmVjZWl2aW5nU3RvcmUgPSB0cnVlO1xuXG4gICgwLCBfd2FybmluZzJbXCJkZWZhdWx0XCJdKSgnPFByb3ZpZGVyPiBkb2VzIG5vdCBzdXBwb3J0IGNoYW5naW5nIGBzdG9yZWAgb24gdGhlIGZseS4gJyArICdJdCBpcyBtb3N0IGxpa2VseSB0aGF0IHlvdSBzZWUgdGhpcyBlcnJvciBiZWNhdXNlIHlvdSB1cGRhdGVkIHRvICcgKyAnUmVkdXggMi54IGFuZCBSZWFjdCBSZWR1eCAyLnggd2hpY2ggbm8gbG9uZ2VyIGhvdCByZWxvYWQgcmVkdWNlcnMgJyArICdhdXRvbWF0aWNhbGx5LiBTZWUgaHR0cHM6Ly9naXRodWIuY29tL3JlYWN0anMvcmVhY3QtcmVkdXgvcmVsZWFzZXMvJyArICd0YWcvdjIuMC4wIGZvciB0aGUgbWlncmF0aW9uIGluc3RydWN0aW9ucy4nKTtcbn1cblxudmFyIFByb3ZpZGVyID0gZnVuY3Rpb24gKF9Db21wb25lbnQpIHtcbiAgX2luaGVyaXRzKFByb3ZpZGVyLCBfQ29tcG9uZW50KTtcblxuICBQcm92aWRlci5wcm90b3R5cGUuZ2V0Q2hpbGRDb250ZXh0ID0gZnVuY3Rpb24gZ2V0Q2hpbGRDb250ZXh0KCkge1xuICAgIHJldHVybiB7IHN0b3JlOiB0aGlzLnN0b3JlIH07XG4gIH07XG5cbiAgZnVuY3Rpb24gUHJvdmlkZXIocHJvcHMsIGNvbnRleHQpIHtcbiAgICBfY2xhc3NDYWxsQ2hlY2sodGhpcywgUHJvdmlkZXIpO1xuXG4gICAgdmFyIF90aGlzID0gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4odGhpcywgX0NvbXBvbmVudC5jYWxsKHRoaXMsIHByb3BzLCBjb250ZXh0KSk7XG5cbiAgICBfdGhpcy5zdG9yZSA9IHByb3BzLnN0b3JlO1xuICAgIHJldHVybiBfdGhpcztcbiAgfVxuXG4gIFByb3ZpZGVyLnByb3RvdHlwZS5yZW5kZXIgPSBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgdmFyIGNoaWxkcmVuID0gdGhpcy5wcm9wcy5jaGlsZHJlbjtcblxuICAgIHJldHVybiBfcmVhY3QuQ2hpbGRyZW4ub25seShjaGlsZHJlbik7XG4gIH07XG5cbiAgcmV0dXJuIFByb3ZpZGVyO1xufShfcmVhY3QuQ29tcG9uZW50KTtcblxuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBQcm92aWRlcjtcblxuaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgUHJvdmlkZXIucHJvdG90eXBlLmNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMgPSBmdW5jdGlvbiAobmV4dFByb3BzKSB7XG4gICAgdmFyIHN0b3JlID0gdGhpcy5zdG9yZTtcbiAgICB2YXIgbmV4dFN0b3JlID0gbmV4dFByb3BzLnN0b3JlO1xuXG4gICAgaWYgKHN0b3JlICE9PSBuZXh0U3RvcmUpIHtcbiAgICAgIHdhcm5BYm91dFJlY2VpdmluZ1N0b3JlKCk7XG4gICAgfVxuICB9O1xufVxuXG5Qcm92aWRlci5wcm9wVHlwZXMgPSB7XG4gIHN0b3JlOiBfc3RvcmVTaGFwZTJbXCJkZWZhdWx0XCJdLmlzUmVxdWlyZWQsXG4gIGNoaWxkcmVuOiBfcmVhY3QuUHJvcFR5cGVzLmVsZW1lbnQuaXNSZXF1aXJlZFxufTtcblByb3ZpZGVyLmNoaWxkQ29udGV4dFR5cGVzID0ge1xuICBzdG9yZTogX3N0b3JlU2hhcGUyW1wiZGVmYXVsdFwiXS5pc1JlcXVpcmVkXG59OyIsIid1c2Ugc3RyaWN0JztcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gY29ubmVjdDtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfc3RvcmVTaGFwZSA9IHJlcXVpcmUoJy4uL3V0aWxzL3N0b3JlU2hhcGUnKTtcblxudmFyIF9zdG9yZVNoYXBlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3N0b3JlU2hhcGUpO1xuXG52YXIgX3NoYWxsb3dFcXVhbCA9IHJlcXVpcmUoJy4uL3V0aWxzL3NoYWxsb3dFcXVhbCcpO1xuXG52YXIgX3NoYWxsb3dFcXVhbDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zaGFsbG93RXF1YWwpO1xuXG52YXIgX3dyYXBBY3Rpb25DcmVhdG9ycyA9IHJlcXVpcmUoJy4uL3V0aWxzL3dyYXBBY3Rpb25DcmVhdG9ycycpO1xuXG52YXIgX3dyYXBBY3Rpb25DcmVhdG9yczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93cmFwQWN0aW9uQ3JlYXRvcnMpO1xuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCcuLi91dGlscy93YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIF9pc1BsYWluT2JqZWN0ID0gcmVxdWlyZSgnbG9kYXNoL2lzUGxhaW5PYmplY3QnKTtcblxudmFyIF9pc1BsYWluT2JqZWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2lzUGxhaW5PYmplY3QpO1xuXG52YXIgX2hvaXN0Tm9uUmVhY3RTdGF0aWNzID0gcmVxdWlyZSgnaG9pc3Qtbm9uLXJlYWN0LXN0YXRpY3MnKTtcblxudmFyIF9ob2lzdE5vblJlYWN0U3RhdGljczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9ob2lzdE5vblJlYWN0U3RhdGljcyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG5mdW5jdGlvbiBfY2xhc3NDYWxsQ2hlY2soaW5zdGFuY2UsIENvbnN0cnVjdG9yKSB7IGlmICghKGluc3RhbmNlIGluc3RhbmNlb2YgQ29uc3RydWN0b3IpKSB7IHRocm93IG5ldyBUeXBlRXJyb3IoXCJDYW5ub3QgY2FsbCBhIGNsYXNzIGFzIGEgZnVuY3Rpb25cIik7IH0gfVxuXG5mdW5jdGlvbiBfcG9zc2libGVDb25zdHJ1Y3RvclJldHVybihzZWxmLCBjYWxsKSB7IGlmICghc2VsZikgeyB0aHJvdyBuZXcgUmVmZXJlbmNlRXJyb3IoXCJ0aGlzIGhhc24ndCBiZWVuIGluaXRpYWxpc2VkIC0gc3VwZXIoKSBoYXNuJ3QgYmVlbiBjYWxsZWRcIik7IH0gcmV0dXJuIGNhbGwgJiYgKHR5cGVvZiBjYWxsID09PSBcIm9iamVjdFwiIHx8IHR5cGVvZiBjYWxsID09PSBcImZ1bmN0aW9uXCIpID8gY2FsbCA6IHNlbGY7IH1cblxuZnVuY3Rpb24gX2luaGVyaXRzKHN1YkNsYXNzLCBzdXBlckNsYXNzKSB7IGlmICh0eXBlb2Ygc3VwZXJDbGFzcyAhPT0gXCJmdW5jdGlvblwiICYmIHN1cGVyQ2xhc3MgIT09IG51bGwpIHsgdGhyb3cgbmV3IFR5cGVFcnJvcihcIlN1cGVyIGV4cHJlc3Npb24gbXVzdCBlaXRoZXIgYmUgbnVsbCBvciBhIGZ1bmN0aW9uLCBub3QgXCIgKyB0eXBlb2Ygc3VwZXJDbGFzcyk7IH0gc3ViQ2xhc3MucHJvdG90eXBlID0gT2JqZWN0LmNyZWF0ZShzdXBlckNsYXNzICYmIHN1cGVyQ2xhc3MucHJvdG90eXBlLCB7IGNvbnN0cnVjdG9yOiB7IHZhbHVlOiBzdWJDbGFzcywgZW51bWVyYWJsZTogZmFsc2UsIHdyaXRhYmxlOiB0cnVlLCBjb25maWd1cmFibGU6IHRydWUgfSB9KTsgaWYgKHN1cGVyQ2xhc3MpIE9iamVjdC5zZXRQcm90b3R5cGVPZiA/IE9iamVjdC5zZXRQcm90b3R5cGVPZihzdWJDbGFzcywgc3VwZXJDbGFzcykgOiBzdWJDbGFzcy5fX3Byb3RvX18gPSBzdXBlckNsYXNzOyB9XG5cbnZhciBkZWZhdWx0TWFwU3RhdGVUb1Byb3BzID0gZnVuY3Rpb24gZGVmYXVsdE1hcFN0YXRlVG9Qcm9wcyhzdGF0ZSkge1xuICByZXR1cm4ge307XG59OyAvLyBlc2xpbnQtZGlzYWJsZS1saW5lIG5vLXVudXNlZC12YXJzXG52YXIgZGVmYXVsdE1hcERpc3BhdGNoVG9Qcm9wcyA9IGZ1bmN0aW9uIGRlZmF1bHRNYXBEaXNwYXRjaFRvUHJvcHMoZGlzcGF0Y2gpIHtcbiAgcmV0dXJuIHsgZGlzcGF0Y2g6IGRpc3BhdGNoIH07XG59O1xudmFyIGRlZmF1bHRNZXJnZVByb3BzID0gZnVuY3Rpb24gZGVmYXVsdE1lcmdlUHJvcHMoc3RhdGVQcm9wcywgZGlzcGF0Y2hQcm9wcywgcGFyZW50UHJvcHMpIHtcbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBwYXJlbnRQcm9wcywgc3RhdGVQcm9wcywgZGlzcGF0Y2hQcm9wcyk7XG59O1xuXG5mdW5jdGlvbiBnZXREaXNwbGF5TmFtZShXcmFwcGVkQ29tcG9uZW50KSB7XG4gIHJldHVybiBXcmFwcGVkQ29tcG9uZW50LmRpc3BsYXlOYW1lIHx8IFdyYXBwZWRDb21wb25lbnQubmFtZSB8fCAnQ29tcG9uZW50Jztcbn1cblxudmFyIGVycm9yT2JqZWN0ID0geyB2YWx1ZTogbnVsbCB9O1xuZnVuY3Rpb24gdHJ5Q2F0Y2goZm4sIGN0eCkge1xuICB0cnkge1xuICAgIHJldHVybiBmbi5hcHBseShjdHgpO1xuICB9IGNhdGNoIChlKSB7XG4gICAgZXJyb3JPYmplY3QudmFsdWUgPSBlO1xuICAgIHJldHVybiBlcnJvck9iamVjdDtcbiAgfVxufVxuXG4vLyBIZWxwcyB0cmFjayBob3QgcmVsb2FkaW5nLlxudmFyIG5leHRWZXJzaW9uID0gMDtcblxuZnVuY3Rpb24gY29ubmVjdChtYXBTdGF0ZVRvUHJvcHMsIG1hcERpc3BhdGNoVG9Qcm9wcywgbWVyZ2VQcm9wcykge1xuICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMyB8fCBhcmd1bWVudHNbM10gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzNdO1xuXG4gIHZhciBzaG91bGRTdWJzY3JpYmUgPSBCb29sZWFuKG1hcFN0YXRlVG9Qcm9wcyk7XG4gIHZhciBtYXBTdGF0ZSA9IG1hcFN0YXRlVG9Qcm9wcyB8fCBkZWZhdWx0TWFwU3RhdGVUb1Byb3BzO1xuXG4gIHZhciBtYXBEaXNwYXRjaCA9IHVuZGVmaW5lZDtcbiAgaWYgKHR5cGVvZiBtYXBEaXNwYXRjaFRvUHJvcHMgPT09ICdmdW5jdGlvbicpIHtcbiAgICBtYXBEaXNwYXRjaCA9IG1hcERpc3BhdGNoVG9Qcm9wcztcbiAgfSBlbHNlIGlmICghbWFwRGlzcGF0Y2hUb1Byb3BzKSB7XG4gICAgbWFwRGlzcGF0Y2ggPSBkZWZhdWx0TWFwRGlzcGF0Y2hUb1Byb3BzO1xuICB9IGVsc2Uge1xuICAgIG1hcERpc3BhdGNoID0gKDAsIF93cmFwQWN0aW9uQ3JlYXRvcnMyW1wiZGVmYXVsdFwiXSkobWFwRGlzcGF0Y2hUb1Byb3BzKTtcbiAgfVxuXG4gIHZhciBmaW5hbE1lcmdlUHJvcHMgPSBtZXJnZVByb3BzIHx8IGRlZmF1bHRNZXJnZVByb3BzO1xuICB2YXIgX29wdGlvbnMkcHVyZSA9IG9wdGlvbnMucHVyZTtcbiAgdmFyIHB1cmUgPSBfb3B0aW9ucyRwdXJlID09PSB1bmRlZmluZWQgPyB0cnVlIDogX29wdGlvbnMkcHVyZTtcbiAgdmFyIF9vcHRpb25zJHdpdGhSZWYgPSBvcHRpb25zLndpdGhSZWY7XG4gIHZhciB3aXRoUmVmID0gX29wdGlvbnMkd2l0aFJlZiA9PT0gdW5kZWZpbmVkID8gZmFsc2UgOiBfb3B0aW9ucyR3aXRoUmVmO1xuXG4gIHZhciBjaGVja01lcmdlZEVxdWFscyA9IHB1cmUgJiYgZmluYWxNZXJnZVByb3BzICE9PSBkZWZhdWx0TWVyZ2VQcm9wcztcblxuICAvLyBIZWxwcyB0cmFjayBob3QgcmVsb2FkaW5nLlxuICB2YXIgdmVyc2lvbiA9IG5leHRWZXJzaW9uKys7XG5cbiAgcmV0dXJuIGZ1bmN0aW9uIHdyYXBXaXRoQ29ubmVjdChXcmFwcGVkQ29tcG9uZW50KSB7XG4gICAgdmFyIGNvbm5lY3REaXNwbGF5TmFtZSA9ICdDb25uZWN0KCcgKyBnZXREaXNwbGF5TmFtZShXcmFwcGVkQ29tcG9uZW50KSArICcpJztcblxuICAgIGZ1bmN0aW9uIGNoZWNrU3RhdGVTaGFwZShwcm9wcywgbWV0aG9kTmFtZSkge1xuICAgICAgaWYgKCEoMCwgX2lzUGxhaW5PYmplY3QyW1wiZGVmYXVsdFwiXSkocHJvcHMpKSB7XG4gICAgICAgICgwLCBfd2FybmluZzJbXCJkZWZhdWx0XCJdKShtZXRob2ROYW1lICsgJygpIGluICcgKyBjb25uZWN0RGlzcGxheU5hbWUgKyAnIG11c3QgcmV0dXJuIGEgcGxhaW4gb2JqZWN0LiAnICsgKCdJbnN0ZWFkIHJlY2VpdmVkICcgKyBwcm9wcyArICcuJykpO1xuICAgICAgfVxuICAgIH1cblxuICAgIGZ1bmN0aW9uIGNvbXB1dGVNZXJnZWRQcm9wcyhzdGF0ZVByb3BzLCBkaXNwYXRjaFByb3BzLCBwYXJlbnRQcm9wcykge1xuICAgICAgdmFyIG1lcmdlZFByb3BzID0gZmluYWxNZXJnZVByb3BzKHN0YXRlUHJvcHMsIGRpc3BhdGNoUHJvcHMsIHBhcmVudFByb3BzKTtcbiAgICAgIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICAgIGNoZWNrU3RhdGVTaGFwZShtZXJnZWRQcm9wcywgJ21lcmdlUHJvcHMnKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiBtZXJnZWRQcm9wcztcbiAgICB9XG5cbiAgICB2YXIgQ29ubmVjdCA9IGZ1bmN0aW9uIChfQ29tcG9uZW50KSB7XG4gICAgICBfaW5oZXJpdHMoQ29ubmVjdCwgX0NvbXBvbmVudCk7XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLnNob3VsZENvbXBvbmVudFVwZGF0ZSA9IGZ1bmN0aW9uIHNob3VsZENvbXBvbmVudFVwZGF0ZSgpIHtcbiAgICAgICAgcmV0dXJuICFwdXJlIHx8IHRoaXMuaGF2ZU93blByb3BzQ2hhbmdlZCB8fCB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkO1xuICAgICAgfTtcblxuICAgICAgZnVuY3Rpb24gQ29ubmVjdChwcm9wcywgY29udGV4dCkge1xuICAgICAgICBfY2xhc3NDYWxsQ2hlY2sodGhpcywgQ29ubmVjdCk7XG5cbiAgICAgICAgdmFyIF90aGlzID0gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4odGhpcywgX0NvbXBvbmVudC5jYWxsKHRoaXMsIHByb3BzLCBjb250ZXh0KSk7XG5cbiAgICAgICAgX3RoaXMudmVyc2lvbiA9IHZlcnNpb247XG4gICAgICAgIF90aGlzLnN0b3JlID0gcHJvcHMuc3RvcmUgfHwgY29udGV4dC5zdG9yZTtcblxuICAgICAgICAoMCwgX2ludmFyaWFudDJbXCJkZWZhdWx0XCJdKShfdGhpcy5zdG9yZSwgJ0NvdWxkIG5vdCBmaW5kIFwic3RvcmVcIiBpbiBlaXRoZXIgdGhlIGNvbnRleHQgb3IgJyArICgncHJvcHMgb2YgXCInICsgY29ubmVjdERpc3BsYXlOYW1lICsgJ1wiLiAnKSArICdFaXRoZXIgd3JhcCB0aGUgcm9vdCBjb21wb25lbnQgaW4gYSA8UHJvdmlkZXI+LCAnICsgKCdvciBleHBsaWNpdGx5IHBhc3MgXCJzdG9yZVwiIGFzIGEgcHJvcCB0byBcIicgKyBjb25uZWN0RGlzcGxheU5hbWUgKyAnXCIuJykpO1xuXG4gICAgICAgIHZhciBzdG9yZVN0YXRlID0gX3RoaXMuc3RvcmUuZ2V0U3RhdGUoKTtcbiAgICAgICAgX3RoaXMuc3RhdGUgPSB7IHN0b3JlU3RhdGU6IHN0b3JlU3RhdGUgfTtcbiAgICAgICAgX3RoaXMuY2xlYXJDYWNoZSgpO1xuICAgICAgICByZXR1cm4gX3RoaXM7XG4gICAgICB9XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbXB1dGVTdGF0ZVByb3BzID0gZnVuY3Rpb24gY29tcHV0ZVN0YXRlUHJvcHMoc3RvcmUsIHByb3BzKSB7XG4gICAgICAgIGlmICghdGhpcy5maW5hbE1hcFN0YXRlVG9Qcm9wcykge1xuICAgICAgICAgIHJldHVybiB0aGlzLmNvbmZpZ3VyZUZpbmFsTWFwU3RhdGUoc3RvcmUsIHByb3BzKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHZhciBzdGF0ZSA9IHN0b3JlLmdldFN0YXRlKCk7XG4gICAgICAgIHZhciBzdGF0ZVByb3BzID0gdGhpcy5kb1N0YXRlUHJvcHNEZXBlbmRPbk93blByb3BzID8gdGhpcy5maW5hbE1hcFN0YXRlVG9Qcm9wcyhzdGF0ZSwgcHJvcHMpIDogdGhpcy5maW5hbE1hcFN0YXRlVG9Qcm9wcyhzdGF0ZSk7XG5cbiAgICAgICAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgICAgICBjaGVja1N0YXRlU2hhcGUoc3RhdGVQcm9wcywgJ21hcFN0YXRlVG9Qcm9wcycpO1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiBzdGF0ZVByb3BzO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY29uZmlndXJlRmluYWxNYXBTdGF0ZSA9IGZ1bmN0aW9uIGNvbmZpZ3VyZUZpbmFsTWFwU3RhdGUoc3RvcmUsIHByb3BzKSB7XG4gICAgICAgIHZhciBtYXBwZWRTdGF0ZSA9IG1hcFN0YXRlKHN0b3JlLmdldFN0YXRlKCksIHByb3BzKTtcbiAgICAgICAgdmFyIGlzRmFjdG9yeSA9IHR5cGVvZiBtYXBwZWRTdGF0ZSA9PT0gJ2Z1bmN0aW9uJztcblxuICAgICAgICB0aGlzLmZpbmFsTWFwU3RhdGVUb1Byb3BzID0gaXNGYWN0b3J5ID8gbWFwcGVkU3RhdGUgOiBtYXBTdGF0ZTtcbiAgICAgICAgdGhpcy5kb1N0YXRlUHJvcHNEZXBlbmRPbk93blByb3BzID0gdGhpcy5maW5hbE1hcFN0YXRlVG9Qcm9wcy5sZW5ndGggIT09IDE7XG5cbiAgICAgICAgaWYgKGlzRmFjdG9yeSkge1xuICAgICAgICAgIHJldHVybiB0aGlzLmNvbXB1dGVTdGF0ZVByb3BzKHN0b3JlLCBwcm9wcyk7XG4gICAgICAgIH1cblxuICAgICAgICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgICAgIGNoZWNrU3RhdGVTaGFwZShtYXBwZWRTdGF0ZSwgJ21hcFN0YXRlVG9Qcm9wcycpO1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiBtYXBwZWRTdGF0ZTtcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbXB1dGVEaXNwYXRjaFByb3BzID0gZnVuY3Rpb24gY29tcHV0ZURpc3BhdGNoUHJvcHMoc3RvcmUsIHByb3BzKSB7XG4gICAgICAgIGlmICghdGhpcy5maW5hbE1hcERpc3BhdGNoVG9Qcm9wcykge1xuICAgICAgICAgIHJldHVybiB0aGlzLmNvbmZpZ3VyZUZpbmFsTWFwRGlzcGF0Y2goc3RvcmUsIHByb3BzKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHZhciBkaXNwYXRjaCA9IHN0b3JlLmRpc3BhdGNoO1xuXG4gICAgICAgIHZhciBkaXNwYXRjaFByb3BzID0gdGhpcy5kb0Rpc3BhdGNoUHJvcHNEZXBlbmRPbk93blByb3BzID8gdGhpcy5maW5hbE1hcERpc3BhdGNoVG9Qcm9wcyhkaXNwYXRjaCwgcHJvcHMpIDogdGhpcy5maW5hbE1hcERpc3BhdGNoVG9Qcm9wcyhkaXNwYXRjaCk7XG5cbiAgICAgICAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgICAgICBjaGVja1N0YXRlU2hhcGUoZGlzcGF0Y2hQcm9wcywgJ21hcERpc3BhdGNoVG9Qcm9wcycpO1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiBkaXNwYXRjaFByb3BzO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY29uZmlndXJlRmluYWxNYXBEaXNwYXRjaCA9IGZ1bmN0aW9uIGNvbmZpZ3VyZUZpbmFsTWFwRGlzcGF0Y2goc3RvcmUsIHByb3BzKSB7XG4gICAgICAgIHZhciBtYXBwZWREaXNwYXRjaCA9IG1hcERpc3BhdGNoKHN0b3JlLmRpc3BhdGNoLCBwcm9wcyk7XG4gICAgICAgIHZhciBpc0ZhY3RvcnkgPSB0eXBlb2YgbWFwcGVkRGlzcGF0Y2ggPT09ICdmdW5jdGlvbic7XG5cbiAgICAgICAgdGhpcy5maW5hbE1hcERpc3BhdGNoVG9Qcm9wcyA9IGlzRmFjdG9yeSA/IG1hcHBlZERpc3BhdGNoIDogbWFwRGlzcGF0Y2g7XG4gICAgICAgIHRoaXMuZG9EaXNwYXRjaFByb3BzRGVwZW5kT25Pd25Qcm9wcyA9IHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMubGVuZ3RoICE9PSAxO1xuXG4gICAgICAgIGlmIChpc0ZhY3RvcnkpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5jb21wdXRlRGlzcGF0Y2hQcm9wcyhzdG9yZSwgcHJvcHMpO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgICAgICBjaGVja1N0YXRlU2hhcGUobWFwcGVkRGlzcGF0Y2gsICdtYXBEaXNwYXRjaFRvUHJvcHMnKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gbWFwcGVkRGlzcGF0Y2g7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS51cGRhdGVTdGF0ZVByb3BzSWZOZWVkZWQgPSBmdW5jdGlvbiB1cGRhdGVTdGF0ZVByb3BzSWZOZWVkZWQoKSB7XG4gICAgICAgIHZhciBuZXh0U3RhdGVQcm9wcyA9IHRoaXMuY29tcHV0ZVN0YXRlUHJvcHModGhpcy5zdG9yZSwgdGhpcy5wcm9wcyk7XG4gICAgICAgIGlmICh0aGlzLnN0YXRlUHJvcHMgJiYgKDAsIF9zaGFsbG93RXF1YWwyW1wiZGVmYXVsdFwiXSkobmV4dFN0YXRlUHJvcHMsIHRoaXMuc3RhdGVQcm9wcykpIHtcbiAgICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICAgIH1cblxuICAgICAgICB0aGlzLnN0YXRlUHJvcHMgPSBuZXh0U3RhdGVQcm9wcztcbiAgICAgICAgcmV0dXJuIHRydWU7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS51cGRhdGVEaXNwYXRjaFByb3BzSWZOZWVkZWQgPSBmdW5jdGlvbiB1cGRhdGVEaXNwYXRjaFByb3BzSWZOZWVkZWQoKSB7XG4gICAgICAgIHZhciBuZXh0RGlzcGF0Y2hQcm9wcyA9IHRoaXMuY29tcHV0ZURpc3BhdGNoUHJvcHModGhpcy5zdG9yZSwgdGhpcy5wcm9wcyk7XG4gICAgICAgIGlmICh0aGlzLmRpc3BhdGNoUHJvcHMgJiYgKDAsIF9zaGFsbG93RXF1YWwyW1wiZGVmYXVsdFwiXSkobmV4dERpc3BhdGNoUHJvcHMsIHRoaXMuZGlzcGF0Y2hQcm9wcykpIHtcbiAgICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICAgIH1cblxuICAgICAgICB0aGlzLmRpc3BhdGNoUHJvcHMgPSBuZXh0RGlzcGF0Y2hQcm9wcztcbiAgICAgICAgcmV0dXJuIHRydWU7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS51cGRhdGVNZXJnZWRQcm9wc0lmTmVlZGVkID0gZnVuY3Rpb24gdXBkYXRlTWVyZ2VkUHJvcHNJZk5lZWRlZCgpIHtcbiAgICAgICAgdmFyIG5leHRNZXJnZWRQcm9wcyA9IGNvbXB1dGVNZXJnZWRQcm9wcyh0aGlzLnN0YXRlUHJvcHMsIHRoaXMuZGlzcGF0Y2hQcm9wcywgdGhpcy5wcm9wcyk7XG4gICAgICAgIGlmICh0aGlzLm1lcmdlZFByb3BzICYmIGNoZWNrTWVyZ2VkRXF1YWxzICYmICgwLCBfc2hhbGxvd0VxdWFsMltcImRlZmF1bHRcIl0pKG5leHRNZXJnZWRQcm9wcywgdGhpcy5tZXJnZWRQcm9wcykpIHtcbiAgICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICAgIH1cblxuICAgICAgICB0aGlzLm1lcmdlZFByb3BzID0gbmV4dE1lcmdlZFByb3BzO1xuICAgICAgICByZXR1cm4gdHJ1ZTtcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmlzU3Vic2NyaWJlZCA9IGZ1bmN0aW9uIGlzU3Vic2NyaWJlZCgpIHtcbiAgICAgICAgcmV0dXJuIHR5cGVvZiB0aGlzLnVuc3Vic2NyaWJlID09PSAnZnVuY3Rpb24nO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUudHJ5U3Vic2NyaWJlID0gZnVuY3Rpb24gdHJ5U3Vic2NyaWJlKCkge1xuICAgICAgICBpZiAoc2hvdWxkU3Vic2NyaWJlICYmICF0aGlzLnVuc3Vic2NyaWJlKSB7XG4gICAgICAgICAgdGhpcy51bnN1YnNjcmliZSA9IHRoaXMuc3RvcmUuc3Vic2NyaWJlKHRoaXMuaGFuZGxlQ2hhbmdlLmJpbmQodGhpcykpO1xuICAgICAgICAgIHRoaXMuaGFuZGxlQ2hhbmdlKCk7XG4gICAgICAgIH1cbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLnRyeVVuc3Vic2NyaWJlID0gZnVuY3Rpb24gdHJ5VW5zdWJzY3JpYmUoKSB7XG4gICAgICAgIGlmICh0aGlzLnVuc3Vic2NyaWJlKSB7XG4gICAgICAgICAgdGhpcy51bnN1YnNjcmliZSgpO1xuICAgICAgICAgIHRoaXMudW5zdWJzY3JpYmUgPSBudWxsO1xuICAgICAgICB9XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jb21wb25lbnREaWRNb3VudCA9IGZ1bmN0aW9uIGNvbXBvbmVudERpZE1vdW50KCkge1xuICAgICAgICB0aGlzLnRyeVN1YnNjcmliZSgpO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyA9IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG4gICAgICAgIGlmICghcHVyZSB8fCAhKDAsIF9zaGFsbG93RXF1YWwyW1wiZGVmYXVsdFwiXSkobmV4dFByb3BzLCB0aGlzLnByb3BzKSkge1xuICAgICAgICAgIHRoaXMuaGF2ZU93blByb3BzQ2hhbmdlZCA9IHRydWU7XG4gICAgICAgIH1cbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbXBvbmVudFdpbGxVbm1vdW50ID0gZnVuY3Rpb24gY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgICAgIHRoaXMudHJ5VW5zdWJzY3JpYmUoKTtcbiAgICAgICAgdGhpcy5jbGVhckNhY2hlKCk7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jbGVhckNhY2hlID0gZnVuY3Rpb24gY2xlYXJDYWNoZSgpIHtcbiAgICAgICAgdGhpcy5kaXNwYXRjaFByb3BzID0gbnVsbDtcbiAgICAgICAgdGhpcy5zdGF0ZVByb3BzID0gbnVsbDtcbiAgICAgICAgdGhpcy5tZXJnZWRQcm9wcyA9IG51bGw7XG4gICAgICAgIHRoaXMuaGF2ZU93blByb3BzQ2hhbmdlZCA9IHRydWU7XG4gICAgICAgIHRoaXMuaGFzU3RvcmVTdGF0ZUNoYW5nZWQgPSB0cnVlO1xuICAgICAgICB0aGlzLmhhdmVTdGF0ZVByb3BzQmVlblByZWNhbGN1bGF0ZWQgPSBmYWxzZTtcbiAgICAgICAgdGhpcy5zdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvciA9IG51bGw7XG4gICAgICAgIHRoaXMucmVuZGVyZWRFbGVtZW50ID0gbnVsbDtcbiAgICAgICAgdGhpcy5maW5hbE1hcERpc3BhdGNoVG9Qcm9wcyA9IG51bGw7XG4gICAgICAgIHRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMgPSBudWxsO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuaGFuZGxlQ2hhbmdlID0gZnVuY3Rpb24gaGFuZGxlQ2hhbmdlKCkge1xuICAgICAgICBpZiAoIXRoaXMudW5zdWJzY3JpYmUpIHtcbiAgICAgICAgICByZXR1cm47XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgc3RvcmVTdGF0ZSA9IHRoaXMuc3RvcmUuZ2V0U3RhdGUoKTtcbiAgICAgICAgdmFyIHByZXZTdG9yZVN0YXRlID0gdGhpcy5zdGF0ZS5zdG9yZVN0YXRlO1xuICAgICAgICBpZiAocHVyZSAmJiBwcmV2U3RvcmVTdGF0ZSA9PT0gc3RvcmVTdGF0ZSkge1xuICAgICAgICAgIHJldHVybjtcbiAgICAgICAgfVxuXG4gICAgICAgIGlmIChwdXJlICYmICF0aGlzLmRvU3RhdGVQcm9wc0RlcGVuZE9uT3duUHJvcHMpIHtcbiAgICAgICAgICB2YXIgaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID0gdHJ5Q2F0Y2godGhpcy51cGRhdGVTdGF0ZVByb3BzSWZOZWVkZWQsIHRoaXMpO1xuICAgICAgICAgIGlmICghaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkKSB7XG4gICAgICAgICAgICByZXR1cm47XG4gICAgICAgICAgfVxuICAgICAgICAgIGlmIChoYXZlU3RhdGVQcm9wc0NoYW5nZWQgPT09IGVycm9yT2JqZWN0KSB7XG4gICAgICAgICAgICB0aGlzLnN0YXRlUHJvcHNQcmVjYWxjdWxhdGlvbkVycm9yID0gZXJyb3JPYmplY3QudmFsdWU7XG4gICAgICAgICAgfVxuICAgICAgICAgIHRoaXMuaGF2ZVN0YXRlUHJvcHNCZWVuUHJlY2FsY3VsYXRlZCA9IHRydWU7XG4gICAgICAgIH1cblxuICAgICAgICB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkID0gdHJ1ZTtcbiAgICAgICAgdGhpcy5zZXRTdGF0ZSh7IHN0b3JlU3RhdGU6IHN0b3JlU3RhdGUgfSk7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5nZXRXcmFwcGVkSW5zdGFuY2UgPSBmdW5jdGlvbiBnZXRXcmFwcGVkSW5zdGFuY2UoKSB7XG4gICAgICAgICgwLCBfaW52YXJpYW50MltcImRlZmF1bHRcIl0pKHdpdGhSZWYsICdUbyBhY2Nlc3MgdGhlIHdyYXBwZWQgaW5zdGFuY2UsIHlvdSBuZWVkIHRvIHNwZWNpZnkgJyArICd7IHdpdGhSZWY6IHRydWUgfSBhcyB0aGUgZm91cnRoIGFyZ3VtZW50IG9mIHRoZSBjb25uZWN0KCkgY2FsbC4nKTtcblxuICAgICAgICByZXR1cm4gdGhpcy5yZWZzLndyYXBwZWRJbnN0YW5jZTtcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLnJlbmRlciA9IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICAgICAgdmFyIGhhdmVPd25Qcm9wc0NoYW5nZWQgPSB0aGlzLmhhdmVPd25Qcm9wc0NoYW5nZWQ7XG4gICAgICAgIHZhciBoYXNTdG9yZVN0YXRlQ2hhbmdlZCA9IHRoaXMuaGFzU3RvcmVTdGF0ZUNoYW5nZWQ7XG4gICAgICAgIHZhciBoYXZlU3RhdGVQcm9wc0JlZW5QcmVjYWxjdWxhdGVkID0gdGhpcy5oYXZlU3RhdGVQcm9wc0JlZW5QcmVjYWxjdWxhdGVkO1xuICAgICAgICB2YXIgc3RhdGVQcm9wc1ByZWNhbGN1bGF0aW9uRXJyb3IgPSB0aGlzLnN0YXRlUHJvcHNQcmVjYWxjdWxhdGlvbkVycm9yO1xuICAgICAgICB2YXIgcmVuZGVyZWRFbGVtZW50ID0gdGhpcy5yZW5kZXJlZEVsZW1lbnQ7XG5cbiAgICAgICAgdGhpcy5oYXZlT3duUHJvcHNDaGFuZ2VkID0gZmFsc2U7XG4gICAgICAgIHRoaXMuaGFzU3RvcmVTdGF0ZUNoYW5nZWQgPSBmYWxzZTtcbiAgICAgICAgdGhpcy5oYXZlU3RhdGVQcm9wc0JlZW5QcmVjYWxjdWxhdGVkID0gZmFsc2U7XG4gICAgICAgIHRoaXMuc3RhdGVQcm9wc1ByZWNhbGN1bGF0aW9uRXJyb3IgPSBudWxsO1xuXG4gICAgICAgIGlmIChzdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvcikge1xuICAgICAgICAgIHRocm93IHN0YXRlUHJvcHNQcmVjYWxjdWxhdGlvbkVycm9yO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIHNob3VsZFVwZGF0ZVN0YXRlUHJvcHMgPSB0cnVlO1xuICAgICAgICB2YXIgc2hvdWxkVXBkYXRlRGlzcGF0Y2hQcm9wcyA9IHRydWU7XG4gICAgICAgIGlmIChwdXJlICYmIHJlbmRlcmVkRWxlbWVudCkge1xuICAgICAgICAgIHNob3VsZFVwZGF0ZVN0YXRlUHJvcHMgPSBoYXNTdG9yZVN0YXRlQ2hhbmdlZCB8fCBoYXZlT3duUHJvcHNDaGFuZ2VkICYmIHRoaXMuZG9TdGF0ZVByb3BzRGVwZW5kT25Pd25Qcm9wcztcbiAgICAgICAgICBzaG91bGRVcGRhdGVEaXNwYXRjaFByb3BzID0gaGF2ZU93blByb3BzQ2hhbmdlZCAmJiB0aGlzLmRvRGlzcGF0Y2hQcm9wc0RlcGVuZE9uT3duUHJvcHM7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID0gZmFsc2U7XG4gICAgICAgIHZhciBoYXZlRGlzcGF0Y2hQcm9wc0NoYW5nZWQgPSBmYWxzZTtcbiAgICAgICAgaWYgKGhhdmVTdGF0ZVByb3BzQmVlblByZWNhbGN1bGF0ZWQpIHtcbiAgICAgICAgICBoYXZlU3RhdGVQcm9wc0NoYW5nZWQgPSB0cnVlO1xuICAgICAgICB9IGVsc2UgaWYgKHNob3VsZFVwZGF0ZVN0YXRlUHJvcHMpIHtcbiAgICAgICAgICBoYXZlU3RhdGVQcm9wc0NoYW5nZWQgPSB0aGlzLnVwZGF0ZVN0YXRlUHJvcHNJZk5lZWRlZCgpO1xuICAgICAgICB9XG4gICAgICAgIGlmIChzaG91bGRVcGRhdGVEaXNwYXRjaFByb3BzKSB7XG4gICAgICAgICAgaGF2ZURpc3BhdGNoUHJvcHNDaGFuZ2VkID0gdGhpcy51cGRhdGVEaXNwYXRjaFByb3BzSWZOZWVkZWQoKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHZhciBoYXZlTWVyZ2VkUHJvcHNDaGFuZ2VkID0gdHJ1ZTtcbiAgICAgICAgaWYgKGhhdmVTdGF0ZVByb3BzQ2hhbmdlZCB8fCBoYXZlRGlzcGF0Y2hQcm9wc0NoYW5nZWQgfHwgaGF2ZU93blByb3BzQ2hhbmdlZCkge1xuICAgICAgICAgIGhhdmVNZXJnZWRQcm9wc0NoYW5nZWQgPSB0aGlzLnVwZGF0ZU1lcmdlZFByb3BzSWZOZWVkZWQoKTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBoYXZlTWVyZ2VkUHJvcHNDaGFuZ2VkID0gZmFsc2U7XG4gICAgICAgIH1cblxuICAgICAgICBpZiAoIWhhdmVNZXJnZWRQcm9wc0NoYW5nZWQgJiYgcmVuZGVyZWRFbGVtZW50KSB7XG4gICAgICAgICAgcmV0dXJuIHJlbmRlcmVkRWxlbWVudDtcbiAgICAgICAgfVxuXG4gICAgICAgIGlmICh3aXRoUmVmKSB7XG4gICAgICAgICAgdGhpcy5yZW5kZXJlZEVsZW1lbnQgPSAoMCwgX3JlYWN0LmNyZWF0ZUVsZW1lbnQpKFdyYXBwZWRDb21wb25lbnQsIF9leHRlbmRzKHt9LCB0aGlzLm1lcmdlZFByb3BzLCB7XG4gICAgICAgICAgICByZWY6ICd3cmFwcGVkSW5zdGFuY2UnXG4gICAgICAgICAgfSkpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHRoaXMucmVuZGVyZWRFbGVtZW50ID0gKDAsIF9yZWFjdC5jcmVhdGVFbGVtZW50KShXcmFwcGVkQ29tcG9uZW50LCB0aGlzLm1lcmdlZFByb3BzKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiB0aGlzLnJlbmRlcmVkRWxlbWVudDtcbiAgICAgIH07XG5cbiAgICAgIHJldHVybiBDb25uZWN0O1xuICAgIH0oX3JlYWN0LkNvbXBvbmVudCk7XG5cbiAgICBDb25uZWN0LmRpc3BsYXlOYW1lID0gY29ubmVjdERpc3BsYXlOYW1lO1xuICAgIENvbm5lY3QuV3JhcHBlZENvbXBvbmVudCA9IFdyYXBwZWRDb21wb25lbnQ7XG4gICAgQ29ubmVjdC5jb250ZXh0VHlwZXMgPSB7XG4gICAgICBzdG9yZTogX3N0b3JlU2hhcGUyW1wiZGVmYXVsdFwiXVxuICAgIH07XG4gICAgQ29ubmVjdC5wcm9wVHlwZXMgPSB7XG4gICAgICBzdG9yZTogX3N0b3JlU2hhcGUyW1wiZGVmYXVsdFwiXVxuICAgIH07XG5cbiAgICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY29tcG9uZW50V2lsbFVwZGF0ZSA9IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxVcGRhdGUoKSB7XG4gICAgICAgIGlmICh0aGlzLnZlcnNpb24gPT09IHZlcnNpb24pIHtcbiAgICAgICAgICByZXR1cm47XG4gICAgICAgIH1cblxuICAgICAgICAvLyBXZSBhcmUgaG90IHJlbG9hZGluZyFcbiAgICAgICAgdGhpcy52ZXJzaW9uID0gdmVyc2lvbjtcbiAgICAgICAgdGhpcy50cnlTdWJzY3JpYmUoKTtcbiAgICAgICAgdGhpcy5jbGVhckNhY2hlKCk7XG4gICAgICB9O1xuICAgIH1cblxuICAgIHJldHVybiAoMCwgX2hvaXN0Tm9uUmVhY3RTdGF0aWNzMltcImRlZmF1bHRcIl0pKENvbm5lY3QsIFdyYXBwZWRDb21wb25lbnQpO1xuICB9O1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuY29ubmVjdCA9IGV4cG9ydHMuUHJvdmlkZXIgPSB1bmRlZmluZWQ7XG5cbnZhciBfUHJvdmlkZXIgPSByZXF1aXJlKCcuL2NvbXBvbmVudHMvUHJvdmlkZXInKTtcblxudmFyIF9Qcm92aWRlcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Qcm92aWRlcik7XG5cbnZhciBfY29ubmVjdCA9IHJlcXVpcmUoJy4vY29tcG9uZW50cy9jb25uZWN0Jyk7XG5cbnZhciBfY29ubmVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jb25uZWN0KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbmV4cG9ydHMuUHJvdmlkZXIgPSBfUHJvdmlkZXIyW1wiZGVmYXVsdFwiXTtcbmV4cG9ydHMuY29ubmVjdCA9IF9jb25uZWN0MltcImRlZmF1bHRcIl07IiwiXCJ1c2Ugc3RyaWN0XCI7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IHNoYWxsb3dFcXVhbDtcbmZ1bmN0aW9uIHNoYWxsb3dFcXVhbChvYmpBLCBvYmpCKSB7XG4gIGlmIChvYmpBID09PSBvYmpCKSB7XG4gICAgcmV0dXJuIHRydWU7XG4gIH1cblxuICB2YXIga2V5c0EgPSBPYmplY3Qua2V5cyhvYmpBKTtcbiAgdmFyIGtleXNCID0gT2JqZWN0LmtleXMob2JqQik7XG5cbiAgaWYgKGtleXNBLmxlbmd0aCAhPT0ga2V5c0IubGVuZ3RoKSB7XG4gICAgcmV0dXJuIGZhbHNlO1xuICB9XG5cbiAgLy8gVGVzdCBmb3IgQSdzIGtleXMgZGlmZmVyZW50IGZyb20gQi5cbiAgdmFyIGhhc093biA9IE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHk7XG4gIGZvciAodmFyIGkgPSAwOyBpIDwga2V5c0EubGVuZ3RoOyBpKyspIHtcbiAgICBpZiAoIWhhc093bi5jYWxsKG9iakIsIGtleXNBW2ldKSB8fCBvYmpBW2tleXNBW2ldXSAhPT0gb2JqQltrZXlzQVtpXV0pIHtcbiAgICAgIHJldHVybiBmYWxzZTtcbiAgICB9XG4gIH1cblxuICByZXR1cm4gdHJ1ZTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IF9yZWFjdC5Qcm9wVHlwZXMuc2hhcGUoe1xuICBzdWJzY3JpYmU6IF9yZWFjdC5Qcm9wVHlwZXMuZnVuYy5pc1JlcXVpcmVkLFxuICBkaXNwYXRjaDogX3JlYWN0LlByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWQsXG4gIGdldFN0YXRlOiBfcmVhY3QuUHJvcFR5cGVzLmZ1bmMuaXNSZXF1aXJlZFxufSk7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSB3YXJuaW5nO1xuLyoqXG4gKiBQcmludHMgYSB3YXJuaW5nIGluIHRoZSBjb25zb2xlIGlmIGl0IGV4aXN0cy5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ30gbWVzc2FnZSBUaGUgd2FybmluZyBtZXNzYWdlLlxuICogQHJldHVybnMge3ZvaWR9XG4gKi9cbmZ1bmN0aW9uIHdhcm5pbmcobWVzc2FnZSkge1xuICAvKiBlc2xpbnQtZGlzYWJsZSBuby1jb25zb2xlICovXG4gIGlmICh0eXBlb2YgY29uc29sZSAhPT0gJ3VuZGVmaW5lZCcgJiYgdHlwZW9mIGNvbnNvbGUuZXJyb3IgPT09ICdmdW5jdGlvbicpIHtcbiAgICBjb25zb2xlLmVycm9yKG1lc3NhZ2UpO1xuICB9XG4gIC8qIGVzbGludC1lbmFibGUgbm8tY29uc29sZSAqL1xuICB0cnkge1xuICAgIC8vIFRoaXMgZXJyb3Igd2FzIHRocm93biBhcyBhIGNvbnZlbmllbmNlIHNvIHRoYXQgeW91IGNhbiB1c2UgdGhpcyBzdGFja1xuICAgIC8vIHRvIGZpbmQgdGhlIGNhbGxzaXRlIHRoYXQgY2F1c2VkIHRoaXMgd2FybmluZyB0byBmaXJlLlxuICAgIHRocm93IG5ldyBFcnJvcihtZXNzYWdlKTtcbiAgICAvKiBlc2xpbnQtZGlzYWJsZSBuby1lbXB0eSAqL1xuICB9IGNhdGNoIChlKSB7fVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWVtcHR5ICovXG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSB3cmFwQWN0aW9uQ3JlYXRvcnM7XG5cbnZhciBfcmVkdXggPSByZXF1aXJlKCdyZWR1eCcpO1xuXG5mdW5jdGlvbiB3cmFwQWN0aW9uQ3JlYXRvcnMoYWN0aW9uQ3JlYXRvcnMpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uIChkaXNwYXRjaCkge1xuICAgIHJldHVybiAoMCwgX3JlZHV4LmJpbmRBY3Rpb25DcmVhdG9ycykoYWN0aW9uQ3JlYXRvcnMsIGRpc3BhdGNoKTtcbiAgfTtcbn0iLCJcInVzZSBzdHJpY3RcIjtcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMubG9vcEFzeW5jID0gbG9vcEFzeW5jO1xuZXhwb3J0cy5tYXBBc3luYyA9IG1hcEFzeW5jO1xuZnVuY3Rpb24gbG9vcEFzeW5jKHR1cm5zLCB3b3JrLCBjYWxsYmFjaykge1xuICB2YXIgY3VycmVudFR1cm4gPSAwLFxuICAgICAgaXNEb25lID0gZmFsc2U7XG4gIHZhciBzeW5jID0gZmFsc2UsXG4gICAgICBoYXNOZXh0ID0gZmFsc2UsXG4gICAgICBkb25lQXJncyA9IHZvaWQgMDtcblxuICBmdW5jdGlvbiBkb25lKCkge1xuICAgIGlzRG9uZSA9IHRydWU7XG4gICAgaWYgKHN5bmMpIHtcbiAgICAgIC8vIEl0ZXJhdGUgaW5zdGVhZCBvZiByZWN1cnNpbmcgaWYgcG9zc2libGUuXG4gICAgICBkb25lQXJncyA9IFtdLmNvbmNhdChBcnJheS5wcm90b3R5cGUuc2xpY2UuY2FsbChhcmd1bWVudHMpKTtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBjYWxsYmFjay5hcHBseSh0aGlzLCBhcmd1bWVudHMpO1xuICB9XG5cbiAgZnVuY3Rpb24gbmV4dCgpIHtcbiAgICBpZiAoaXNEb25lKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgaGFzTmV4dCA9IHRydWU7XG4gICAgaWYgKHN5bmMpIHtcbiAgICAgIC8vIEl0ZXJhdGUgaW5zdGVhZCBvZiByZWN1cnNpbmcgaWYgcG9zc2libGUuXG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgc3luYyA9IHRydWU7XG5cbiAgICB3aGlsZSAoIWlzRG9uZSAmJiBjdXJyZW50VHVybiA8IHR1cm5zICYmIGhhc05leHQpIHtcbiAgICAgIGhhc05leHQgPSBmYWxzZTtcbiAgICAgIHdvcmsuY2FsbCh0aGlzLCBjdXJyZW50VHVybisrLCBuZXh0LCBkb25lKTtcbiAgICB9XG5cbiAgICBzeW5jID0gZmFsc2U7XG5cbiAgICBpZiAoaXNEb25lKSB7XG4gICAgICAvLyBUaGlzIG1lYW5zIHRoZSBsb29wIGZpbmlzaGVkIHN5bmNocm9ub3VzbHkuXG4gICAgICBjYWxsYmFjay5hcHBseSh0aGlzLCBkb25lQXJncyk7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgaWYgKGN1cnJlbnRUdXJuID49IHR1cm5zICYmIGhhc05leHQpIHtcbiAgICAgIGlzRG9uZSA9IHRydWU7XG4gICAgICBjYWxsYmFjaygpO1xuICAgIH1cbiAgfVxuXG4gIG5leHQoKTtcbn1cblxuZnVuY3Rpb24gbWFwQXN5bmMoYXJyYXksIHdvcmssIGNhbGxiYWNrKSB7XG4gIHZhciBsZW5ndGggPSBhcnJheS5sZW5ndGg7XG4gIHZhciB2YWx1ZXMgPSBbXTtcblxuICBpZiAobGVuZ3RoID09PSAwKSByZXR1cm4gY2FsbGJhY2sobnVsbCwgdmFsdWVzKTtcblxuICB2YXIgaXNEb25lID0gZmFsc2UsXG4gICAgICBkb25lQ291bnQgPSAwO1xuXG4gIGZ1bmN0aW9uIGRvbmUoaW5kZXgsIGVycm9yLCB2YWx1ZSkge1xuICAgIGlmIChpc0RvbmUpIHJldHVybjtcblxuICAgIGlmIChlcnJvcikge1xuICAgICAgaXNEb25lID0gdHJ1ZTtcbiAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICB9IGVsc2Uge1xuICAgICAgdmFsdWVzW2luZGV4XSA9IHZhbHVlO1xuXG4gICAgICBpc0RvbmUgPSArK2RvbmVDb3VudCA9PT0gbGVuZ3RoO1xuXG4gICAgICBpZiAoaXNEb25lKSBjYWxsYmFjayhudWxsLCB2YWx1ZXMpO1xuICAgIH1cbiAgfVxuXG4gIGFycmF5LmZvckVhY2goZnVuY3Rpb24gKGl0ZW0sIGluZGV4KSB7XG4gICAgd29yayhpdGVtLCBpbmRleCwgZnVuY3Rpb24gKGVycm9yLCB2YWx1ZSkge1xuICAgICAgZG9uZShpbmRleCwgZXJyb3IsIHZhbHVlKTtcbiAgICB9KTtcbiAgfSk7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX0ludGVybmFsUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9JbnRlcm5hbFByb3BUeXBlcycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG4vKipcbiAqIEEgbWl4aW4gdGhhdCBhZGRzIHRoZSBcImhpc3RvcnlcIiBpbnN0YW5jZSB2YXJpYWJsZSB0byBjb21wb25lbnRzLlxuICovXG52YXIgSGlzdG9yeSA9IHtcblxuICBjb250ZXh0VHlwZXM6IHtcbiAgICBoaXN0b3J5OiBfSW50ZXJuYWxQcm9wVHlwZXMuaGlzdG9yeVxuICB9LFxuXG4gIGNvbXBvbmVudFdpbGxNb3VudDogZnVuY3Rpb24gY29tcG9uZW50V2lsbE1vdW50KCkge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAndGhlIGBIaXN0b3J5YCBtaXhpbiBpcyBkZXByZWNhdGVkLCBwbGVhc2UgYWNjZXNzIGBjb250ZXh0LnJvdXRlcmAgd2l0aCB5b3VyIG93biBgY29udGV4dFR5cGVzYC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWhpc3RvcnltaXhpbicpIDogdm9pZCAwO1xuICAgIHRoaXMuaGlzdG9yeSA9IHRoaXMuY29udGV4dC5oaXN0b3J5O1xuICB9XG59O1xuXG5leHBvcnRzLmRlZmF1bHQgPSBIaXN0b3J5O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfTGluayA9IHJlcXVpcmUoJy4vTGluaycpO1xuXG52YXIgX0xpbmsyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfTGluayk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbi8qKlxuICogQW4gPEluZGV4TGluaz4gaXMgdXNlZCB0byBsaW5rIHRvIGFuIDxJbmRleFJvdXRlPi5cbiAqL1xudmFyIEluZGV4TGluayA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnSW5kZXhMaW5rJyxcbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgcmV0dXJuIF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50KF9MaW5rMi5kZWZhdWx0LCBfZXh0ZW5kcyh7fSwgdGhpcy5wcm9wcywgeyBvbmx5QWN0aXZlT25JbmRleDogdHJ1ZSB9KSk7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBJbmRleExpbms7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9SZWRpcmVjdCA9IHJlcXVpcmUoJy4vUmVkaXJlY3QnKTtcblxudmFyIF9SZWRpcmVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9SZWRpcmVjdCk7XG5cbnZhciBfSW50ZXJuYWxQcm9wVHlwZXMgPSByZXF1aXJlKCcuL0ludGVybmFsUHJvcFR5cGVzJyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBfUmVhY3QkUHJvcFR5cGVzID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcztcbnZhciBzdHJpbmcgPSBfUmVhY3QkUHJvcFR5cGVzLnN0cmluZztcbnZhciBvYmplY3QgPSBfUmVhY3QkUHJvcFR5cGVzLm9iamVjdDtcblxuLyoqXG4gKiBBbiA8SW5kZXhSZWRpcmVjdD4gaXMgdXNlZCB0byByZWRpcmVjdCBmcm9tIGFuIGluZGV4Um91dGUuXG4gKi9cblxudmFyIEluZGV4UmVkaXJlY3QgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ0luZGV4UmVkaXJlY3QnLFxuXG5cbiAgc3RhdGljczoge1xuICAgIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudDogZnVuY3Rpb24gY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQsIHBhcmVudFJvdXRlKSB7XG4gICAgICAvKiBpc3RhbmJ1bCBpZ25vcmUgZWxzZTogc2FuaXR5IGNoZWNrICovXG4gICAgICBpZiAocGFyZW50Um91dGUpIHtcbiAgICAgICAgcGFyZW50Um91dGUuaW5kZXhSb3V0ZSA9IF9SZWRpcmVjdDIuZGVmYXVsdC5jcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQoZWxlbWVudCk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ0FuIDxJbmRleFJlZGlyZWN0PiBkb2VzIG5vdCBtYWtlIHNlbnNlIGF0IHRoZSByb290IG9mIHlvdXIgcm91dGUgY29uZmlnJykgOiB2b2lkIDA7XG4gICAgICB9XG4gICAgfVxuICB9LFxuXG4gIHByb3BUeXBlczoge1xuICAgIHRvOiBzdHJpbmcuaXNSZXF1aXJlZCxcbiAgICBxdWVyeTogb2JqZWN0LFxuICAgIHN0YXRlOiBvYmplY3QsXG4gICAgb25FbnRlcjogX0ludGVybmFsUHJvcFR5cGVzLmZhbHN5LFxuICAgIGNoaWxkcmVuOiBfSW50ZXJuYWxQcm9wVHlwZXMuZmFsc3lcbiAgfSxcblxuICAvKiBpc3RhbmJ1bCBpZ25vcmUgbmV4dDogc2FuaXR5IGNoZWNrICovXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgICFmYWxzZSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICc8SW5kZXhSZWRpcmVjdD4gZWxlbWVudHMgYXJlIGZvciByb3V0ZXIgY29uZmlndXJhdGlvbiBvbmx5IGFuZCBzaG91bGQgbm90IGJlIHJlbmRlcmVkJykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gSW5kZXhSZWRpcmVjdDtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxudmFyIF9JbnRlcm5hbFByb3BUeXBlcyA9IHJlcXVpcmUoJy4vSW50ZXJuYWxQcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIGZ1bmMgPSBfcmVhY3QyLmRlZmF1bHQuUHJvcFR5cGVzLmZ1bmM7XG5cbi8qKlxuICogQW4gPEluZGV4Um91dGU+IGlzIHVzZWQgdG8gc3BlY2lmeSBpdHMgcGFyZW50J3MgPFJvdXRlIGluZGV4Um91dGU+IGluXG4gKiBhIEpTWCByb3V0ZSBjb25maWcuXG4gKi9cblxudmFyIEluZGV4Um91dGUgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ0luZGV4Um91dGUnLFxuXG5cbiAgc3RhdGljczoge1xuICAgIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudDogZnVuY3Rpb24gY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQsIHBhcmVudFJvdXRlKSB7XG4gICAgICAvKiBpc3RhbmJ1bCBpZ25vcmUgZWxzZTogc2FuaXR5IGNoZWNrICovXG4gICAgICBpZiAocGFyZW50Um91dGUpIHtcbiAgICAgICAgcGFyZW50Um91dGUuaW5kZXhSb3V0ZSA9ICgwLCBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQpKGVsZW1lbnQpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdBbiA8SW5kZXhSb3V0ZT4gZG9lcyBub3QgbWFrZSBzZW5zZSBhdCB0aGUgcm9vdCBvZiB5b3VyIHJvdXRlIGNvbmZpZycpIDogdm9pZCAwO1xuICAgICAgfVxuICAgIH1cbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICBwYXRoOiBfSW50ZXJuYWxQcm9wVHlwZXMuZmFsc3ksXG4gICAgY29tcG9uZW50OiBfSW50ZXJuYWxQcm9wVHlwZXMuY29tcG9uZW50LFxuICAgIGNvbXBvbmVudHM6IF9JbnRlcm5hbFByb3BUeXBlcy5jb21wb25lbnRzLFxuICAgIGdldENvbXBvbmVudDogZnVuYyxcbiAgICBnZXRDb21wb25lbnRzOiBmdW5jXG4gIH0sXG5cbiAgLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHNhbml0eSBjaGVjayAqL1xuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICAhZmFsc2UgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnPEluZGV4Um91dGU+IGVsZW1lbnRzIGFyZSBmb3Igcm91dGVyIGNvbmZpZ3VyYXRpb24gb25seSBhbmQgc2hvdWxkIG5vdCBiZSByZW5kZXJlZCcpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IEluZGV4Um91dGU7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLnJvdXRlcyA9IGV4cG9ydHMucm91dGUgPSBleHBvcnRzLmNvbXBvbmVudHMgPSBleHBvcnRzLmNvbXBvbmVudCA9IGV4cG9ydHMuaGlzdG9yeSA9IHVuZGVmaW5lZDtcbmV4cG9ydHMuZmFsc3kgPSBmYWxzeTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBmdW5jID0gX3JlYWN0LlByb3BUeXBlcy5mdW5jO1xudmFyIG9iamVjdCA9IF9yZWFjdC5Qcm9wVHlwZXMub2JqZWN0O1xudmFyIGFycmF5T2YgPSBfcmVhY3QuUHJvcFR5cGVzLmFycmF5T2Y7XG52YXIgb25lT2ZUeXBlID0gX3JlYWN0LlByb3BUeXBlcy5vbmVPZlR5cGU7XG52YXIgZWxlbWVudCA9IF9yZWFjdC5Qcm9wVHlwZXMuZWxlbWVudDtcbnZhciBzaGFwZSA9IF9yZWFjdC5Qcm9wVHlwZXMuc2hhcGU7XG52YXIgc3RyaW5nID0gX3JlYWN0LlByb3BUeXBlcy5zdHJpbmc7XG5mdW5jdGlvbiBmYWxzeShwcm9wcywgcHJvcE5hbWUsIGNvbXBvbmVudE5hbWUpIHtcbiAgaWYgKHByb3BzW3Byb3BOYW1lXSkgcmV0dXJuIG5ldyBFcnJvcignPCcgKyBjb21wb25lbnROYW1lICsgJz4gc2hvdWxkIG5vdCBoYXZlIGEgXCInICsgcHJvcE5hbWUgKyAnXCIgcHJvcCcpO1xufVxuXG52YXIgaGlzdG9yeSA9IGV4cG9ydHMuaGlzdG9yeSA9IHNoYXBlKHtcbiAgbGlzdGVuOiBmdW5jLmlzUmVxdWlyZWQsXG4gIHB1c2g6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgcmVwbGFjZTogZnVuYy5pc1JlcXVpcmVkLFxuICBnbzogZnVuYy5pc1JlcXVpcmVkLFxuICBnb0JhY2s6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ29Gb3J3YXJkOiBmdW5jLmlzUmVxdWlyZWRcbn0pO1xuXG52YXIgY29tcG9uZW50ID0gZXhwb3J0cy5jb21wb25lbnQgPSBvbmVPZlR5cGUoW2Z1bmMsIHN0cmluZ10pO1xudmFyIGNvbXBvbmVudHMgPSBleHBvcnRzLmNvbXBvbmVudHMgPSBvbmVPZlR5cGUoW2NvbXBvbmVudCwgb2JqZWN0XSk7XG52YXIgcm91dGUgPSBleHBvcnRzLnJvdXRlID0gb25lT2ZUeXBlKFtvYmplY3QsIGVsZW1lbnRdKTtcbnZhciByb3V0ZXMgPSBleHBvcnRzLnJvdXRlcyA9IG9uZU9mVHlwZShbcm91dGUsIGFycmF5T2Yocm91dGUpXSk7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBvYmplY3QgPSBfcmVhY3QyLmRlZmF1bHQuUHJvcFR5cGVzLm9iamVjdDtcblxuLyoqXG4gKiBUaGUgTGlmZWN5Y2xlIG1peGluIGFkZHMgdGhlIHJvdXRlcldpbGxMZWF2ZSBsaWZlY3ljbGUgbWV0aG9kIHRvIGFcbiAqIGNvbXBvbmVudCB0aGF0IG1heSBiZSB1c2VkIHRvIGNhbmNlbCBhIHRyYW5zaXRpb24gb3IgcHJvbXB0IHRoZSB1c2VyXG4gKiBmb3IgY29uZmlybWF0aW9uLlxuICpcbiAqIE9uIHN0YW5kYXJkIHRyYW5zaXRpb25zLCByb3V0ZXJXaWxsTGVhdmUgcmVjZWl2ZXMgYSBzaW5nbGUgYXJndW1lbnQ6IHRoZVxuICogbG9jYXRpb24gd2UncmUgdHJhbnNpdGlvbmluZyB0by4gVG8gY2FuY2VsIHRoZSB0cmFuc2l0aW9uLCByZXR1cm4gZmFsc2UuXG4gKiBUbyBwcm9tcHQgdGhlIHVzZXIgZm9yIGNvbmZpcm1hdGlvbiwgcmV0dXJuIGEgcHJvbXB0IG1lc3NhZ2UgKHN0cmluZykuXG4gKlxuICogRHVyaW5nIHRoZSBiZWZvcmV1bmxvYWQgZXZlbnQgKGFzc3VtaW5nIHlvdSdyZSB1c2luZyB0aGUgdXNlQmVmb3JlVW5sb2FkXG4gKiBoaXN0b3J5IGVuaGFuY2VyKSwgcm91dGVyV2lsbExlYXZlIGRvZXMgbm90IHJlY2VpdmUgYSBsb2NhdGlvbiBvYmplY3RcbiAqIGJlY2F1c2UgaXQgaXNuJ3QgcG9zc2libGUgZm9yIHVzIHRvIGtub3cgdGhlIGxvY2F0aW9uIHdlJ3JlIHRyYW5zaXRpb25pbmdcbiAqIHRvLiBJbiB0aGlzIGNhc2Ugcm91dGVyV2lsbExlYXZlIG11c3QgcmV0dXJuIGEgcHJvbXB0IG1lc3NhZ2UgdG8gcHJldmVudFxuICogdGhlIHVzZXIgZnJvbSBjbG9zaW5nIHRoZSB3aW5kb3cvdGFiLlxuICovXG5cbnZhciBMaWZlY3ljbGUgPSB7XG5cbiAgY29udGV4dFR5cGVzOiB7XG4gICAgaGlzdG9yeTogb2JqZWN0LmlzUmVxdWlyZWQsXG4gICAgLy8gTmVzdGVkIGNoaWxkcmVuIHJlY2VpdmUgdGhlIHJvdXRlIGFzIGNvbnRleHQsIGVpdGhlclxuICAgIC8vIHNldCBieSB0aGUgcm91dGUgY29tcG9uZW50IHVzaW5nIHRoZSBSb3V0ZUNvbnRleHQgbWl4aW5cbiAgICAvLyBvciBieSBzb21lIG90aGVyIGFuY2VzdG9yLlxuICAgIHJvdXRlOiBvYmplY3RcbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICAvLyBSb3V0ZSBjb21wb25lbnRzIHJlY2VpdmUgdGhlIHJvdXRlIG9iamVjdCBhcyBhIHByb3AuXG4gICAgcm91dGU6IG9iamVjdFxuICB9LFxuXG4gIGNvbXBvbmVudERpZE1vdW50OiBmdW5jdGlvbiBjb21wb25lbnREaWRNb3VudCgpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ3RoZSBgTGlmZWN5Y2xlYCBtaXhpbiBpcyBkZXByZWNhdGVkLCBwbGVhc2UgdXNlIGBjb250ZXh0LnJvdXRlci5zZXRSb3V0ZUxlYXZlSG9vayhyb3V0ZSwgaG9vaylgLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItbGlmZWN5Y2xlbWl4aW4nKSA6IHZvaWQgMDtcbiAgICAhdGhpcy5yb3V0ZXJXaWxsTGVhdmUgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnVGhlIExpZmVjeWNsZSBtaXhpbiByZXF1aXJlcyB5b3UgdG8gZGVmaW5lIGEgcm91dGVyV2lsbExlYXZlIG1ldGhvZCcpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICAgIHZhciByb3V0ZSA9IHRoaXMucHJvcHMucm91dGUgfHwgdGhpcy5jb250ZXh0LnJvdXRlO1xuXG4gICAgIXJvdXRlID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJ1RoZSBMaWZlY3ljbGUgbWl4aW4gbXVzdCBiZSB1c2VkIG9uIGVpdGhlciBhKSBhIDxSb3V0ZSBjb21wb25lbnQ+IG9yICcgKyAnYikgYSBkZXNjZW5kYW50IG9mIGEgPFJvdXRlIGNvbXBvbmVudD4gdGhhdCB1c2VzIHRoZSBSb3V0ZUNvbnRleHQgbWl4aW4nKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICB0aGlzLl91bmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZSA9IHRoaXMuY29udGV4dC5oaXN0b3J5Lmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZShyb3V0ZSwgdGhpcy5yb3V0ZXJXaWxsTGVhdmUpO1xuICB9LFxuICBjb21wb25lbnRXaWxsVW5tb3VudDogZnVuY3Rpb24gY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgaWYgKHRoaXMuX3VubGlzdGVuQmVmb3JlTGVhdmluZ1JvdXRlKSB0aGlzLl91bmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZSgpO1xuICB9XG59O1xuXG5leHBvcnRzLmRlZmF1bHQgPSBMaWZlY3ljbGU7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9Qcm9wVHlwZXMgPSByZXF1aXJlKCcuL1Byb3BUeXBlcycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBfb2JqZWN0V2l0aG91dFByb3BlcnRpZXMob2JqLCBrZXlzKSB7IHZhciB0YXJnZXQgPSB7fTsgZm9yICh2YXIgaSBpbiBvYmopIHsgaWYgKGtleXMuaW5kZXhPZihpKSA+PSAwKSBjb250aW51ZTsgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqLCBpKSkgY29udGludWU7IHRhcmdldFtpXSA9IG9ialtpXTsgfSByZXR1cm4gdGFyZ2V0OyB9XG5cbnZhciBfUmVhY3QkUHJvcFR5cGVzID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcztcbnZhciBib29sID0gX1JlYWN0JFByb3BUeXBlcy5ib29sO1xudmFyIG9iamVjdCA9IF9SZWFjdCRQcm9wVHlwZXMub2JqZWN0O1xudmFyIHN0cmluZyA9IF9SZWFjdCRQcm9wVHlwZXMuc3RyaW5nO1xudmFyIGZ1bmMgPSBfUmVhY3QkUHJvcFR5cGVzLmZ1bmM7XG52YXIgb25lT2ZUeXBlID0gX1JlYWN0JFByb3BUeXBlcy5vbmVPZlR5cGU7XG5cblxuZnVuY3Rpb24gaXNMZWZ0Q2xpY2tFdmVudChldmVudCkge1xuICByZXR1cm4gZXZlbnQuYnV0dG9uID09PSAwO1xufVxuXG5mdW5jdGlvbiBpc01vZGlmaWVkRXZlbnQoZXZlbnQpIHtcbiAgcmV0dXJuICEhKGV2ZW50Lm1ldGFLZXkgfHwgZXZlbnQuYWx0S2V5IHx8IGV2ZW50LmN0cmxLZXkgfHwgZXZlbnQuc2hpZnRLZXkpO1xufVxuXG4vLyBUT0RPOiBEZS1kdXBsaWNhdGUgYWdhaW5zdCBoYXNBbnlQcm9wZXJ0aWVzIGluIGNyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyLlxuZnVuY3Rpb24gaXNFbXB0eU9iamVjdChvYmplY3QpIHtcbiAgZm9yICh2YXIgcCBpbiBvYmplY3QpIHtcbiAgICBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iamVjdCwgcCkpIHJldHVybiBmYWxzZTtcbiAgfXJldHVybiB0cnVlO1xufVxuXG5mdW5jdGlvbiBjcmVhdGVMb2NhdGlvbkRlc2NyaXB0b3IodG8sIF9yZWYpIHtcbiAgdmFyIHF1ZXJ5ID0gX3JlZi5xdWVyeTtcbiAgdmFyIGhhc2ggPSBfcmVmLmhhc2g7XG4gIHZhciBzdGF0ZSA9IF9yZWYuc3RhdGU7XG5cbiAgaWYgKHF1ZXJ5IHx8IGhhc2ggfHwgc3RhdGUpIHtcbiAgICByZXR1cm4geyBwYXRobmFtZTogdG8sIHF1ZXJ5OiBxdWVyeSwgaGFzaDogaGFzaCwgc3RhdGU6IHN0YXRlIH07XG4gIH1cblxuICByZXR1cm4gdG87XG59XG5cbi8qKlxuICogQSA8TGluaz4gaXMgdXNlZCB0byBjcmVhdGUgYW4gPGE+IGVsZW1lbnQgdGhhdCBsaW5rcyB0byBhIHJvdXRlLlxuICogV2hlbiB0aGF0IHJvdXRlIGlzIGFjdGl2ZSwgdGhlIGxpbmsgZ2V0cyB0aGUgdmFsdWUgb2YgaXRzXG4gKiBhY3RpdmVDbGFzc05hbWUgcHJvcC5cbiAqXG4gKiBGb3IgZXhhbXBsZSwgYXNzdW1pbmcgeW91IGhhdmUgdGhlIGZvbGxvd2luZyByb3V0ZTpcbiAqXG4gKiAgIDxSb3V0ZSBwYXRoPVwiL3Bvc3RzLzpwb3N0SURcIiBjb21wb25lbnQ9e1Bvc3R9IC8+XG4gKlxuICogWW91IGNvdWxkIHVzZSB0aGUgZm9sbG93aW5nIGNvbXBvbmVudCB0byBsaW5rIHRvIHRoYXQgcm91dGU6XG4gKlxuICogICA8TGluayB0bz17YC9wb3N0cy8ke3Bvc3QuaWR9YH0gLz5cbiAqXG4gKiBMaW5rcyBtYXkgcGFzcyBhbG9uZyBsb2NhdGlvbiBzdGF0ZSBhbmQvb3IgcXVlcnkgc3RyaW5nIHBhcmFtZXRlcnNcbiAqIGluIHRoZSBzdGF0ZS9xdWVyeSBwcm9wcywgcmVzcGVjdGl2ZWx5LlxuICpcbiAqICAgPExpbmsgLi4uIHF1ZXJ5PXt7IHNob3c6IHRydWUgfX0gc3RhdGU9e3sgdGhlOiAnc3RhdGUnIH19IC8+XG4gKi9cbnZhciBMaW5rID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdMaW5rJyxcblxuXG4gIGNvbnRleHRUeXBlczoge1xuICAgIHJvdXRlcjogX1Byb3BUeXBlcy5yb3V0ZXJTaGFwZVxuICB9LFxuXG4gIHByb3BUeXBlczoge1xuICAgIHRvOiBvbmVPZlR5cGUoW3N0cmluZywgb2JqZWN0XSkuaXNSZXF1aXJlZCxcbiAgICBxdWVyeTogb2JqZWN0LFxuICAgIGhhc2g6IHN0cmluZyxcbiAgICBzdGF0ZTogb2JqZWN0LFxuICAgIGFjdGl2ZVN0eWxlOiBvYmplY3QsXG4gICAgYWN0aXZlQ2xhc3NOYW1lOiBzdHJpbmcsXG4gICAgb25seUFjdGl2ZU9uSW5kZXg6IGJvb2wuaXNSZXF1aXJlZCxcbiAgICBvbkNsaWNrOiBmdW5jLFxuICAgIHRhcmdldDogc3RyaW5nXG4gIH0sXG5cbiAgZ2V0RGVmYXVsdFByb3BzOiBmdW5jdGlvbiBnZXREZWZhdWx0UHJvcHMoKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIG9ubHlBY3RpdmVPbkluZGV4OiBmYWxzZSxcbiAgICAgIHN0eWxlOiB7fVxuICAgIH07XG4gIH0sXG4gIGhhbmRsZUNsaWNrOiBmdW5jdGlvbiBoYW5kbGVDbGljayhldmVudCkge1xuICAgIGlmICh0aGlzLnByb3BzLm9uQ2xpY2spIHRoaXMucHJvcHMub25DbGljayhldmVudCk7XG5cbiAgICBpZiAoZXZlbnQuZGVmYXVsdFByZXZlbnRlZCkgcmV0dXJuO1xuXG4gICAgIXRoaXMuY29udGV4dC5yb3V0ZXIgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnPExpbms+cyByZW5kZXJlZCBvdXRzaWRlIG9mIGEgcm91dGVyIGNvbnRleHQgY2Fubm90IG5hdmlnYXRlLicpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICAgIGlmIChpc01vZGlmaWVkRXZlbnQoZXZlbnQpIHx8ICFpc0xlZnRDbGlja0V2ZW50KGV2ZW50KSkgcmV0dXJuO1xuXG4gICAgLy8gSWYgdGFyZ2V0IHByb3AgaXMgc2V0IChlLmcuIHRvIFwiX2JsYW5rXCIpLCBsZXQgYnJvd3NlciBoYW5kbGUgbGluay5cbiAgICAvKiBpc3RhbmJ1bCBpZ25vcmUgaWY6IHVudGVzdGFibGUgd2l0aCBLYXJtYSAqL1xuICAgIGlmICh0aGlzLnByb3BzLnRhcmdldCkgcmV0dXJuO1xuXG4gICAgZXZlbnQucHJldmVudERlZmF1bHQoKTtcblxuICAgIHZhciBfcHJvcHMgPSB0aGlzLnByb3BzO1xuICAgIHZhciB0byA9IF9wcm9wcy50bztcbiAgICB2YXIgcXVlcnkgPSBfcHJvcHMucXVlcnk7XG4gICAgdmFyIGhhc2ggPSBfcHJvcHMuaGFzaDtcbiAgICB2YXIgc3RhdGUgPSBfcHJvcHMuc3RhdGU7XG5cbiAgICB2YXIgbG9jYXRpb24gPSBjcmVhdGVMb2NhdGlvbkRlc2NyaXB0b3IodG8sIHsgcXVlcnk6IHF1ZXJ5LCBoYXNoOiBoYXNoLCBzdGF0ZTogc3RhdGUgfSk7XG5cbiAgICB0aGlzLmNvbnRleHQucm91dGVyLnB1c2gobG9jYXRpb24pO1xuICB9LFxuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICB2YXIgX3Byb3BzMiA9IHRoaXMucHJvcHM7XG4gICAgdmFyIHRvID0gX3Byb3BzMi50bztcbiAgICB2YXIgcXVlcnkgPSBfcHJvcHMyLnF1ZXJ5O1xuICAgIHZhciBoYXNoID0gX3Byb3BzMi5oYXNoO1xuICAgIHZhciBzdGF0ZSA9IF9wcm9wczIuc3RhdGU7XG4gICAgdmFyIGFjdGl2ZUNsYXNzTmFtZSA9IF9wcm9wczIuYWN0aXZlQ2xhc3NOYW1lO1xuICAgIHZhciBhY3RpdmVTdHlsZSA9IF9wcm9wczIuYWN0aXZlU3R5bGU7XG4gICAgdmFyIG9ubHlBY3RpdmVPbkluZGV4ID0gX3Byb3BzMi5vbmx5QWN0aXZlT25JbmRleDtcblxuICAgIHZhciBwcm9wcyA9IF9vYmplY3RXaXRob3V0UHJvcGVydGllcyhfcHJvcHMyLCBbJ3RvJywgJ3F1ZXJ5JywgJ2hhc2gnLCAnc3RhdGUnLCAnYWN0aXZlQ2xhc3NOYW1lJywgJ2FjdGl2ZVN0eWxlJywgJ29ubHlBY3RpdmVPbkluZGV4J10pO1xuXG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoIShxdWVyeSB8fCBoYXNoIHx8IHN0YXRlKSwgJ3RoZSBgcXVlcnlgLCBgaGFzaGAsIGFuZCBgc3RhdGVgIHByb3BzIG9uIGA8TGluaz5gIGFyZSBkZXByZWNhdGVkLCB1c2UgYDxMaW5rIHRvPXt7IHBhdGhuYW1lLCBxdWVyeSwgaGFzaCwgc3RhdGUgfX0vPi4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWlzQWN0aXZlZGVwcmVjYXRlZCcpIDogdm9pZCAwO1xuXG4gICAgLy8gSWdub3JlIGlmIHJlbmRlcmVkIG91dHNpZGUgdGhlIGNvbnRleHQgb2Ygcm91dGVyLCBzaW1wbGlmaWVzIHVuaXQgdGVzdGluZy5cbiAgICB2YXIgcm91dGVyID0gdGhpcy5jb250ZXh0LnJvdXRlcjtcblxuXG4gICAgaWYgKHJvdXRlcikge1xuICAgICAgdmFyIGxvY2F0aW9uID0gY3JlYXRlTG9jYXRpb25EZXNjcmlwdG9yKHRvLCB7IHF1ZXJ5OiBxdWVyeSwgaGFzaDogaGFzaCwgc3RhdGU6IHN0YXRlIH0pO1xuICAgICAgcHJvcHMuaHJlZiA9IHJvdXRlci5jcmVhdGVIcmVmKGxvY2F0aW9uKTtcblxuICAgICAgaWYgKGFjdGl2ZUNsYXNzTmFtZSB8fCBhY3RpdmVTdHlsZSAhPSBudWxsICYmICFpc0VtcHR5T2JqZWN0KGFjdGl2ZVN0eWxlKSkge1xuICAgICAgICBpZiAocm91dGVyLmlzQWN0aXZlKGxvY2F0aW9uLCBvbmx5QWN0aXZlT25JbmRleCkpIHtcbiAgICAgICAgICBpZiAoYWN0aXZlQ2xhc3NOYW1lKSB7XG4gICAgICAgICAgICBpZiAocHJvcHMuY2xhc3NOYW1lKSB7XG4gICAgICAgICAgICAgIHByb3BzLmNsYXNzTmFtZSArPSAnICcgKyBhY3RpdmVDbGFzc05hbWU7XG4gICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICBwcm9wcy5jbGFzc05hbWUgPSBhY3RpdmVDbGFzc05hbWU7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgfVxuXG4gICAgICAgICAgaWYgKGFjdGl2ZVN0eWxlKSBwcm9wcy5zdHlsZSA9IF9leHRlbmRzKHt9LCBwcm9wcy5zdHlsZSwgYWN0aXZlU3R5bGUpO1xuICAgICAgICB9XG4gICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50KCdhJywgX2V4dGVuZHMoe30sIHByb3BzLCB7IG9uQ2xpY2s6IHRoaXMuaGFuZGxlQ2xpY2sgfSkpO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gTGluaztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuY29tcGlsZVBhdHRlcm4gPSBjb21waWxlUGF0dGVybjtcbmV4cG9ydHMubWF0Y2hQYXR0ZXJuID0gbWF0Y2hQYXR0ZXJuO1xuZXhwb3J0cy5nZXRQYXJhbU5hbWVzID0gZ2V0UGFyYW1OYW1lcztcbmV4cG9ydHMuZ2V0UGFyYW1zID0gZ2V0UGFyYW1zO1xuZXhwb3J0cy5mb3JtYXRQYXR0ZXJuID0gZm9ybWF0UGF0dGVybjtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gZXNjYXBlUmVnRXhwKHN0cmluZykge1xuICByZXR1cm4gc3RyaW5nLnJlcGxhY2UoL1suKis/XiR7fSgpfFtcXF1cXFxcXS9nLCAnXFxcXCQmJyk7XG59XG5cbmZ1bmN0aW9uIF9jb21waWxlUGF0dGVybihwYXR0ZXJuKSB7XG4gIHZhciByZWdleHBTb3VyY2UgPSAnJztcbiAgdmFyIHBhcmFtTmFtZXMgPSBbXTtcbiAgdmFyIHRva2VucyA9IFtdO1xuXG4gIHZhciBtYXRjaCA9IHZvaWQgMCxcbiAgICAgIGxhc3RJbmRleCA9IDAsXG4gICAgICBtYXRjaGVyID0gLzooW2EtekEtWl8kXVthLXpBLVowLTlfJF0qKXxcXCpcXCp8XFwqfFxcKHxcXCkvZztcbiAgd2hpbGUgKG1hdGNoID0gbWF0Y2hlci5leGVjKHBhdHRlcm4pKSB7XG4gICAgaWYgKG1hdGNoLmluZGV4ICE9PSBsYXN0SW5kZXgpIHtcbiAgICAgIHRva2Vucy5wdXNoKHBhdHRlcm4uc2xpY2UobGFzdEluZGV4LCBtYXRjaC5pbmRleCkpO1xuICAgICAgcmVnZXhwU291cmNlICs9IGVzY2FwZVJlZ0V4cChwYXR0ZXJuLnNsaWNlKGxhc3RJbmRleCwgbWF0Y2guaW5kZXgpKTtcbiAgICB9XG5cbiAgICBpZiAobWF0Y2hbMV0pIHtcbiAgICAgIHJlZ2V4cFNvdXJjZSArPSAnKFteL10rKSc7XG4gICAgICBwYXJhbU5hbWVzLnB1c2gobWF0Y2hbMV0pO1xuICAgIH0gZWxzZSBpZiAobWF0Y2hbMF0gPT09ICcqKicpIHtcbiAgICAgIHJlZ2V4cFNvdXJjZSArPSAnKC4qKSc7XG4gICAgICBwYXJhbU5hbWVzLnB1c2goJ3NwbGF0Jyk7XG4gICAgfSBlbHNlIGlmIChtYXRjaFswXSA9PT0gJyonKSB7XG4gICAgICByZWdleHBTb3VyY2UgKz0gJyguKj8pJztcbiAgICAgIHBhcmFtTmFtZXMucHVzaCgnc3BsYXQnKTtcbiAgICB9IGVsc2UgaWYgKG1hdGNoWzBdID09PSAnKCcpIHtcbiAgICAgIHJlZ2V4cFNvdXJjZSArPSAnKD86JztcbiAgICB9IGVsc2UgaWYgKG1hdGNoWzBdID09PSAnKScpIHtcbiAgICAgIHJlZ2V4cFNvdXJjZSArPSAnKT8nO1xuICAgIH1cblxuICAgIHRva2Vucy5wdXNoKG1hdGNoWzBdKTtcblxuICAgIGxhc3RJbmRleCA9IG1hdGNoZXIubGFzdEluZGV4O1xuICB9XG5cbiAgaWYgKGxhc3RJbmRleCAhPT0gcGF0dGVybi5sZW5ndGgpIHtcbiAgICB0b2tlbnMucHVzaChwYXR0ZXJuLnNsaWNlKGxhc3RJbmRleCwgcGF0dGVybi5sZW5ndGgpKTtcbiAgICByZWdleHBTb3VyY2UgKz0gZXNjYXBlUmVnRXhwKHBhdHRlcm4uc2xpY2UobGFzdEluZGV4LCBwYXR0ZXJuLmxlbmd0aCkpO1xuICB9XG5cbiAgcmV0dXJuIHtcbiAgICBwYXR0ZXJuOiBwYXR0ZXJuLFxuICAgIHJlZ2V4cFNvdXJjZTogcmVnZXhwU291cmNlLFxuICAgIHBhcmFtTmFtZXM6IHBhcmFtTmFtZXMsXG4gICAgdG9rZW5zOiB0b2tlbnNcbiAgfTtcbn1cblxudmFyIENvbXBpbGVkUGF0dGVybnNDYWNoZSA9IE9iamVjdC5jcmVhdGUobnVsbCk7XG5cbmZ1bmN0aW9uIGNvbXBpbGVQYXR0ZXJuKHBhdHRlcm4pIHtcbiAgaWYgKCFDb21waWxlZFBhdHRlcm5zQ2FjaGVbcGF0dGVybl0pIENvbXBpbGVkUGF0dGVybnNDYWNoZVtwYXR0ZXJuXSA9IF9jb21waWxlUGF0dGVybihwYXR0ZXJuKTtcblxuICByZXR1cm4gQ29tcGlsZWRQYXR0ZXJuc0NhY2hlW3BhdHRlcm5dO1xufVxuXG4vKipcbiAqIEF0dGVtcHRzIHRvIG1hdGNoIGEgcGF0dGVybiBvbiB0aGUgZ2l2ZW4gcGF0aG5hbWUuIFBhdHRlcm5zIG1heSB1c2VcbiAqIHRoZSBmb2xsb3dpbmcgc3BlY2lhbCBjaGFyYWN0ZXJzOlxuICpcbiAqIC0gOnBhcmFtTmFtZSAgICAgTWF0Y2hlcyBhIFVSTCBzZWdtZW50IHVwIHRvIHRoZSBuZXh0IC8sID8sIG9yICMuIFRoZVxuICogICAgICAgICAgICAgICAgICBjYXB0dXJlZCBzdHJpbmcgaXMgY29uc2lkZXJlZCBhIFwicGFyYW1cIlxuICogLSAoKSAgICAgICAgICAgICBXcmFwcyBhIHNlZ21lbnQgb2YgdGhlIFVSTCB0aGF0IGlzIG9wdGlvbmFsXG4gKiAtICogICAgICAgICAgICAgIENvbnN1bWVzIChub24tZ3JlZWR5KSBhbGwgY2hhcmFjdGVycyB1cCB0byB0aGUgbmV4dFxuICogICAgICAgICAgICAgICAgICBjaGFyYWN0ZXIgaW4gdGhlIHBhdHRlcm4sIG9yIHRvIHRoZSBlbmQgb2YgdGhlIFVSTCBpZlxuICogICAgICAgICAgICAgICAgICB0aGVyZSBpcyBub25lXG4gKiAtICoqICAgICAgICAgICAgIENvbnN1bWVzIChncmVlZHkpIGFsbCBjaGFyYWN0ZXJzIHVwIHRvIHRoZSBuZXh0IGNoYXJhY3RlclxuICogICAgICAgICAgICAgICAgICBpbiB0aGUgcGF0dGVybiwgb3IgdG8gdGhlIGVuZCBvZiB0aGUgVVJMIGlmIHRoZXJlIGlzIG5vbmVcbiAqXG4gKiAgVGhlIGZ1bmN0aW9uIGNhbGxzIGNhbGxiYWNrKGVycm9yLCBtYXRjaGVkKSB3aGVuIGZpbmlzaGVkLlxuICogVGhlIHJldHVybiB2YWx1ZSBpcyBhbiBvYmplY3Qgd2l0aCB0aGUgZm9sbG93aW5nIHByb3BlcnRpZXM6XG4gKlxuICogLSByZW1haW5pbmdQYXRobmFtZVxuICogLSBwYXJhbU5hbWVzXG4gKiAtIHBhcmFtVmFsdWVzXG4gKi9cbmZ1bmN0aW9uIG1hdGNoUGF0dGVybihwYXR0ZXJuLCBwYXRobmFtZSkge1xuICAvLyBFbnN1cmUgcGF0dGVybiBzdGFydHMgd2l0aCBsZWFkaW5nIHNsYXNoIGZvciBjb25zaXN0ZW5jeSB3aXRoIHBhdGhuYW1lLlxuICBpZiAocGF0dGVybi5jaGFyQXQoMCkgIT09ICcvJykge1xuICAgIHBhdHRlcm4gPSAnLycgKyBwYXR0ZXJuO1xuICB9XG5cbiAgdmFyIF9jb21waWxlUGF0dGVybjIgPSBjb21waWxlUGF0dGVybihwYXR0ZXJuKTtcblxuICB2YXIgcmVnZXhwU291cmNlID0gX2NvbXBpbGVQYXR0ZXJuMi5yZWdleHBTb3VyY2U7XG4gIHZhciBwYXJhbU5hbWVzID0gX2NvbXBpbGVQYXR0ZXJuMi5wYXJhbU5hbWVzO1xuICB2YXIgdG9rZW5zID0gX2NvbXBpbGVQYXR0ZXJuMi50b2tlbnM7XG5cblxuICBpZiAocGF0dGVybi5jaGFyQXQocGF0dGVybi5sZW5ndGggLSAxKSAhPT0gJy8nKSB7XG4gICAgcmVnZXhwU291cmNlICs9ICcvPyc7IC8vIEFsbG93IG9wdGlvbmFsIHBhdGggc2VwYXJhdG9yIGF0IGVuZC5cbiAgfVxuXG4gIC8vIFNwZWNpYWwtY2FzZSBwYXR0ZXJucyBsaWtlICcqJyBmb3IgY2F0Y2gtYWxsIHJvdXRlcy5cbiAgaWYgKHRva2Vuc1t0b2tlbnMubGVuZ3RoIC0gMV0gPT09ICcqJykge1xuICAgIHJlZ2V4cFNvdXJjZSArPSAnJCc7XG4gIH1cblxuICB2YXIgbWF0Y2ggPSBwYXRobmFtZS5tYXRjaChuZXcgUmVnRXhwKCdeJyArIHJlZ2V4cFNvdXJjZSwgJ2knKSk7XG4gIGlmIChtYXRjaCA9PSBudWxsKSB7XG4gICAgcmV0dXJuIG51bGw7XG4gIH1cblxuICB2YXIgbWF0Y2hlZFBhdGggPSBtYXRjaFswXTtcbiAgdmFyIHJlbWFpbmluZ1BhdGhuYW1lID0gcGF0aG5hbWUuc3Vic3RyKG1hdGNoZWRQYXRoLmxlbmd0aCk7XG5cbiAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lKSB7XG4gICAgLy8gUmVxdWlyZSB0aGF0IHRoZSBtYXRjaCBlbmRzIGF0IGEgcGF0aCBzZXBhcmF0b3IsIGlmIHdlIGRpZG4ndCBtYXRjaFxuICAgIC8vIHRoZSBmdWxsIHBhdGgsIHNvIGFueSByZW1haW5pbmcgcGF0aG5hbWUgaXMgYSBuZXcgcGF0aCBzZWdtZW50LlxuICAgIGlmIChtYXRjaGVkUGF0aC5jaGFyQXQobWF0Y2hlZFBhdGgubGVuZ3RoIC0gMSkgIT09ICcvJykge1xuICAgICAgcmV0dXJuIG51bGw7XG4gICAgfVxuXG4gICAgLy8gSWYgdGhlcmUgaXMgYSByZW1haW5pbmcgcGF0aG5hbWUsIHRyZWF0IHRoZSBwYXRoIHNlcGFyYXRvciBhcyBwYXJ0IG9mXG4gICAgLy8gdGhlIHJlbWFpbmluZyBwYXRobmFtZSBmb3IgcHJvcGVybHkgY29udGludWluZyB0aGUgbWF0Y2guXG4gICAgcmVtYWluaW5nUGF0aG5hbWUgPSAnLycgKyByZW1haW5pbmdQYXRobmFtZTtcbiAgfVxuXG4gIHJldHVybiB7XG4gICAgcmVtYWluaW5nUGF0aG5hbWU6IHJlbWFpbmluZ1BhdGhuYW1lLFxuICAgIHBhcmFtTmFtZXM6IHBhcmFtTmFtZXMsXG4gICAgcGFyYW1WYWx1ZXM6IG1hdGNoLnNsaWNlKDEpLm1hcChmdW5jdGlvbiAodikge1xuICAgICAgcmV0dXJuIHYgJiYgZGVjb2RlVVJJQ29tcG9uZW50KHYpO1xuICAgIH0pXG4gIH07XG59XG5cbmZ1bmN0aW9uIGdldFBhcmFtTmFtZXMocGF0dGVybikge1xuICByZXR1cm4gY29tcGlsZVBhdHRlcm4ocGF0dGVybikucGFyYW1OYW1lcztcbn1cblxuZnVuY3Rpb24gZ2V0UGFyYW1zKHBhdHRlcm4sIHBhdGhuYW1lKSB7XG4gIHZhciBtYXRjaCA9IG1hdGNoUGF0dGVybihwYXR0ZXJuLCBwYXRobmFtZSk7XG4gIGlmICghbWF0Y2gpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuXG4gIHZhciBwYXJhbU5hbWVzID0gbWF0Y2gucGFyYW1OYW1lcztcbiAgdmFyIHBhcmFtVmFsdWVzID0gbWF0Y2gucGFyYW1WYWx1ZXM7XG5cbiAgdmFyIHBhcmFtcyA9IHt9O1xuXG4gIHBhcmFtTmFtZXMuZm9yRWFjaChmdW5jdGlvbiAocGFyYW1OYW1lLCBpbmRleCkge1xuICAgIHBhcmFtc1twYXJhbU5hbWVdID0gcGFyYW1WYWx1ZXNbaW5kZXhdO1xuICB9KTtcblxuICByZXR1cm4gcGFyYW1zO1xufVxuXG4vKipcbiAqIFJldHVybnMgYSB2ZXJzaW9uIG9mIHRoZSBnaXZlbiBwYXR0ZXJuIHdpdGggcGFyYW1zIGludGVycG9sYXRlZC4gVGhyb3dzXG4gKiBpZiB0aGVyZSBpcyBhIGR5bmFtaWMgc2VnbWVudCBvZiB0aGUgcGF0dGVybiBmb3Igd2hpY2ggdGhlcmUgaXMgbm8gcGFyYW0uXG4gKi9cbmZ1bmN0aW9uIGZvcm1hdFBhdHRlcm4ocGF0dGVybiwgcGFyYW1zKSB7XG4gIHBhcmFtcyA9IHBhcmFtcyB8fCB7fTtcblxuICB2YXIgX2NvbXBpbGVQYXR0ZXJuMyA9IGNvbXBpbGVQYXR0ZXJuKHBhdHRlcm4pO1xuXG4gIHZhciB0b2tlbnMgPSBfY29tcGlsZVBhdHRlcm4zLnRva2VucztcblxuICB2YXIgcGFyZW5Db3VudCA9IDAsXG4gICAgICBwYXRobmFtZSA9ICcnLFxuICAgICAgc3BsYXRJbmRleCA9IDA7XG5cbiAgdmFyIHRva2VuID0gdm9pZCAwLFxuICAgICAgcGFyYW1OYW1lID0gdm9pZCAwLFxuICAgICAgcGFyYW1WYWx1ZSA9IHZvaWQgMDtcbiAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IHRva2Vucy5sZW5ndGg7IGkgPCBsZW47ICsraSkge1xuICAgIHRva2VuID0gdG9rZW5zW2ldO1xuXG4gICAgaWYgKHRva2VuID09PSAnKicgfHwgdG9rZW4gPT09ICcqKicpIHtcbiAgICAgIHBhcmFtVmFsdWUgPSBBcnJheS5pc0FycmF5KHBhcmFtcy5zcGxhdCkgPyBwYXJhbXMuc3BsYXRbc3BsYXRJbmRleCsrXSA6IHBhcmFtcy5zcGxhdDtcblxuICAgICAgIShwYXJhbVZhbHVlICE9IG51bGwgfHwgcGFyZW5Db3VudCA+IDApID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJ01pc3Npbmcgc3BsYXQgIyVzIGZvciBwYXRoIFwiJXNcIicsIHNwbGF0SW5kZXgsIHBhdHRlcm4pIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICAgICAgaWYgKHBhcmFtVmFsdWUgIT0gbnVsbCkgcGF0aG5hbWUgKz0gZW5jb2RlVVJJKHBhcmFtVmFsdWUpO1xuICAgIH0gZWxzZSBpZiAodG9rZW4gPT09ICcoJykge1xuICAgICAgcGFyZW5Db3VudCArPSAxO1xuICAgIH0gZWxzZSBpZiAodG9rZW4gPT09ICcpJykge1xuICAgICAgcGFyZW5Db3VudCAtPSAxO1xuICAgIH0gZWxzZSBpZiAodG9rZW4uY2hhckF0KDApID09PSAnOicpIHtcbiAgICAgIHBhcmFtTmFtZSA9IHRva2VuLnN1YnN0cmluZygxKTtcbiAgICAgIHBhcmFtVmFsdWUgPSBwYXJhbXNbcGFyYW1OYW1lXTtcblxuICAgICAgIShwYXJhbVZhbHVlICE9IG51bGwgfHwgcGFyZW5Db3VudCA+IDApID8gcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJ01pc3NpbmcgXCIlc1wiIHBhcmFtZXRlciBmb3IgcGF0aCBcIiVzXCInLCBwYXJhbU5hbWUsIHBhdHRlcm4pIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICAgICAgaWYgKHBhcmFtVmFsdWUgIT0gbnVsbCkgcGF0aG5hbWUgKz0gZW5jb2RlVVJJQ29tcG9uZW50KHBhcmFtVmFsdWUpO1xuICAgIH0gZWxzZSB7XG4gICAgICBwYXRobmFtZSArPSB0b2tlbjtcbiAgICB9XG4gIH1cblxuICByZXR1cm4gcGF0aG5hbWUucmVwbGFjZSgvXFwvKy9nLCAnLycpO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMucm91dGVyID0gZXhwb3J0cy5yb3V0ZXMgPSBleHBvcnRzLnJvdXRlID0gZXhwb3J0cy5jb21wb25lbnRzID0gZXhwb3J0cy5jb21wb25lbnQgPSBleHBvcnRzLmxvY2F0aW9uID0gZXhwb3J0cy5oaXN0b3J5ID0gZXhwb3J0cy5mYWxzeSA9IGV4cG9ydHMubG9jYXRpb25TaGFwZSA9IGV4cG9ydHMucm91dGVyU2hhcGUgPSB1bmRlZmluZWQ7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMgPSByZXF1aXJlKCcuL2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMnKTtcblxudmFyIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMpO1xuXG52YXIgX0ludGVybmFsUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9JbnRlcm5hbFByb3BUeXBlcycpO1xuXG52YXIgSW50ZXJuYWxQcm9wVHlwZXMgPSBfaW50ZXJvcFJlcXVpcmVXaWxkY2FyZChfSW50ZXJuYWxQcm9wVHlwZXMpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVXaWxkY2FyZChvYmopIHsgaWYgKG9iaiAmJiBvYmouX19lc01vZHVsZSkgeyByZXR1cm4gb2JqOyB9IGVsc2UgeyB2YXIgbmV3T2JqID0ge307IGlmIChvYmogIT0gbnVsbCkgeyBmb3IgKHZhciBrZXkgaW4gb2JqKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqLCBrZXkpKSBuZXdPYmpba2V5XSA9IG9ialtrZXldOyB9IH0gbmV3T2JqLmRlZmF1bHQgPSBvYmo7IHJldHVybiBuZXdPYmo7IH0gfVxuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgZnVuYyA9IF9yZWFjdC5Qcm9wVHlwZXMuZnVuYztcbnZhciBvYmplY3QgPSBfcmVhY3QuUHJvcFR5cGVzLm9iamVjdDtcbnZhciBzaGFwZSA9IF9yZWFjdC5Qcm9wVHlwZXMuc2hhcGU7XG52YXIgc3RyaW5nID0gX3JlYWN0LlByb3BUeXBlcy5zdHJpbmc7XG52YXIgcm91dGVyU2hhcGUgPSBleHBvcnRzLnJvdXRlclNoYXBlID0gc2hhcGUoe1xuICBwdXNoOiBmdW5jLmlzUmVxdWlyZWQsXG4gIHJlcGxhY2U6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ286IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ29CYWNrOiBmdW5jLmlzUmVxdWlyZWQsXG4gIGdvRm9yd2FyZDogZnVuYy5pc1JlcXVpcmVkLFxuICBzZXRSb3V0ZUxlYXZlSG9vazogZnVuYy5pc1JlcXVpcmVkLFxuICBpc0FjdGl2ZTogZnVuYy5pc1JlcXVpcmVkXG59KTtcblxudmFyIGxvY2F0aW9uU2hhcGUgPSBleHBvcnRzLmxvY2F0aW9uU2hhcGUgPSBzaGFwZSh7XG4gIHBhdGhuYW1lOiBzdHJpbmcuaXNSZXF1aXJlZCxcbiAgc2VhcmNoOiBzdHJpbmcuaXNSZXF1aXJlZCxcbiAgc3RhdGU6IG9iamVjdCxcbiAgYWN0aW9uOiBzdHJpbmcuaXNSZXF1aXJlZCxcbiAga2V5OiBzdHJpbmdcbn0pO1xuXG4vLyBEZXByZWNhdGVkIHN0dWZmIGJlbG93OlxuXG52YXIgZmFsc3kgPSBleHBvcnRzLmZhbHN5ID0gSW50ZXJuYWxQcm9wVHlwZXMuZmFsc3k7XG52YXIgaGlzdG9yeSA9IGV4cG9ydHMuaGlzdG9yeSA9IEludGVybmFsUHJvcFR5cGVzLmhpc3Rvcnk7XG52YXIgbG9jYXRpb24gPSBleHBvcnRzLmxvY2F0aW9uID0gbG9jYXRpb25TaGFwZTtcbnZhciBjb21wb25lbnQgPSBleHBvcnRzLmNvbXBvbmVudCA9IEludGVybmFsUHJvcFR5cGVzLmNvbXBvbmVudDtcbnZhciBjb21wb25lbnRzID0gZXhwb3J0cy5jb21wb25lbnRzID0gSW50ZXJuYWxQcm9wVHlwZXMuY29tcG9uZW50cztcbnZhciByb3V0ZSA9IGV4cG9ydHMucm91dGUgPSBJbnRlcm5hbFByb3BUeXBlcy5yb3V0ZTtcbnZhciByb3V0ZXMgPSBleHBvcnRzLnJvdXRlcyA9IEludGVybmFsUHJvcFR5cGVzLnJvdXRlcztcbnZhciByb3V0ZXIgPSBleHBvcnRzLnJvdXRlciA9IHJvdXRlclNoYXBlO1xuXG5pZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAoZnVuY3Rpb24gKCkge1xuICAgIHZhciBkZXByZWNhdGVQcm9wVHlwZSA9IGZ1bmN0aW9uIGRlcHJlY2F0ZVByb3BUeXBlKHByb3BUeXBlLCBtZXNzYWdlKSB7XG4gICAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgbWVzc2FnZSkgOiB2b2lkIDA7XG4gICAgICAgIHJldHVybiBwcm9wVHlwZS5hcHBseSh1bmRlZmluZWQsIGFyZ3VtZW50cyk7XG4gICAgICB9O1xuICAgIH07XG5cbiAgICB2YXIgZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZSA9IGZ1bmN0aW9uIGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUocHJvcFR5cGUpIHtcbiAgICAgIHJldHVybiBkZXByZWNhdGVQcm9wVHlwZShwcm9wVHlwZSwgJ1RoaXMgcHJvcCB0eXBlIGlzIG5vdCBpbnRlbmRlZCBmb3IgZXh0ZXJuYWwgdXNlLCBhbmQgd2FzIHByZXZpb3VzbHkgZXhwb3J0ZWQgYnkgbWlzdGFrZS4gVGhlc2UgaW50ZXJuYWwgcHJvcCB0eXBlcyBhcmUgZGVwcmVjYXRlZCBmb3IgZXh0ZXJuYWwgdXNlLCBhbmQgd2lsbCBiZSByZW1vdmVkIGluIGEgbGF0ZXIgdmVyc2lvbi4nKTtcbiAgICB9O1xuXG4gICAgdmFyIGRlcHJlY2F0ZVJlbmFtZWRQcm9wVHlwZSA9IGZ1bmN0aW9uIGRlcHJlY2F0ZVJlbmFtZWRQcm9wVHlwZShwcm9wVHlwZSwgbmFtZSkge1xuICAgICAgcmV0dXJuIGRlcHJlY2F0ZVByb3BUeXBlKHByb3BUeXBlLCAnVGhlIGAnICsgbmFtZSArICdgIHByb3AgdHlwZSBpcyBub3cgZXhwb3J0ZWQgYXMgYCcgKyBuYW1lICsgJ1NoYXBlYCB0byBhdm9pZCBuYW1lIGNvbmZsaWN0cy4gVGhpcyBleHBvcnQgaXMgZGVwcmVjYXRlZCBhbmQgd2lsbCBiZSByZW1vdmVkIGluIGEgbGF0ZXIgdmVyc2lvbi4nKTtcbiAgICB9O1xuXG4gICAgZXhwb3J0cy5mYWxzeSA9IGZhbHN5ID0gZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZShmYWxzeSk7XG4gICAgZXhwb3J0cy5oaXN0b3J5ID0gaGlzdG9yeSA9IGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUoaGlzdG9yeSk7XG4gICAgZXhwb3J0cy5jb21wb25lbnQgPSBjb21wb25lbnQgPSBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlKGNvbXBvbmVudCk7XG4gICAgZXhwb3J0cy5jb21wb25lbnRzID0gY29tcG9uZW50cyA9IGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUoY29tcG9uZW50cyk7XG4gICAgZXhwb3J0cy5yb3V0ZSA9IHJvdXRlID0gZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZShyb3V0ZSk7XG4gICAgZXhwb3J0cy5yb3V0ZXMgPSByb3V0ZXMgPSBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlKHJvdXRlcyk7XG5cbiAgICBleHBvcnRzLmxvY2F0aW9uID0gbG9jYXRpb24gPSBkZXByZWNhdGVSZW5hbWVkUHJvcFR5cGUobG9jYXRpb24sICdsb2NhdGlvbicpO1xuICAgIGV4cG9ydHMucm91dGVyID0gcm91dGVyID0gZGVwcmVjYXRlUmVuYW1lZFByb3BUeXBlKHJvdXRlciwgJ3JvdXRlcicpO1xuICB9KSgpO1xufVxuXG52YXIgZGVmYXVsdEV4cG9ydCA9IHtcbiAgZmFsc3k6IGZhbHN5LFxuICBoaXN0b3J5OiBoaXN0b3J5LFxuICBsb2NhdGlvbjogbG9jYXRpb24sXG4gIGNvbXBvbmVudDogY29tcG9uZW50LFxuICBjb21wb25lbnRzOiBjb21wb25lbnRzLFxuICByb3V0ZTogcm91dGUsXG4gIC8vIEZvciBzb21lIHJlYXNvbiwgcm91dGVzIHdhcyBuZXZlciBoZXJlLlxuICByb3V0ZXI6IHJvdXRlclxufTtcblxuaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgZGVmYXVsdEV4cG9ydCA9ICgwLCBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllczIuZGVmYXVsdCkoZGVmYXVsdEV4cG9ydCwgJ1RoZSBkZWZhdWx0IGV4cG9ydCBmcm9tIGByZWFjdC1yb3V0ZXIvbGliL1Byb3BUeXBlc2AgaXMgZGVwcmVjYXRlZC4gUGxlYXNlIHVzZSB0aGUgbmFtZWQgZXhwb3J0cyBpbnN0ZWFkLicpO1xufVxuXG5leHBvcnRzLmRlZmF1bHQgPSBkZWZhdWx0RXhwb3J0OyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxudmFyIF9QYXR0ZXJuVXRpbHMgPSByZXF1aXJlKCcuL1BhdHRlcm5VdGlscycpO1xuXG52YXIgX0ludGVybmFsUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9JbnRlcm5hbFByb3BUeXBlcycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgc3RyaW5nID0gX1JlYWN0JFByb3BUeXBlcy5zdHJpbmc7XG52YXIgb2JqZWN0ID0gX1JlYWN0JFByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogQSA8UmVkaXJlY3Q+IGlzIHVzZWQgdG8gZGVjbGFyZSBhbm90aGVyIFVSTCBwYXRoIGEgY2xpZW50IHNob3VsZFxuICogYmUgc2VudCB0byB3aGVuIHRoZXkgcmVxdWVzdCBhIGdpdmVuIFVSTC5cbiAqXG4gKiBSZWRpcmVjdHMgYXJlIHBsYWNlZCBhbG9uZ3NpZGUgcm91dGVzIGluIHRoZSByb3V0ZSBjb25maWd1cmF0aW9uXG4gKiBhbmQgYXJlIHRyYXZlcnNlZCBpbiB0aGUgc2FtZSBtYW5uZXIuXG4gKi9cblxudmFyIFJlZGlyZWN0ID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdSZWRpcmVjdCcsXG5cblxuICBzdGF0aWNzOiB7XG4gICAgY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50OiBmdW5jdGlvbiBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQoZWxlbWVudCkge1xuICAgICAgdmFyIHJvdXRlID0gKDAsIF9Sb3V0ZVV0aWxzLmNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudCkoZWxlbWVudCk7XG5cbiAgICAgIGlmIChyb3V0ZS5mcm9tKSByb3V0ZS5wYXRoID0gcm91dGUuZnJvbTtcblxuICAgICAgcm91dGUub25FbnRlciA9IGZ1bmN0aW9uIChuZXh0U3RhdGUsIHJlcGxhY2UpIHtcbiAgICAgICAgdmFyIGxvY2F0aW9uID0gbmV4dFN0YXRlLmxvY2F0aW9uO1xuICAgICAgICB2YXIgcGFyYW1zID0gbmV4dFN0YXRlLnBhcmFtcztcblxuXG4gICAgICAgIHZhciBwYXRobmFtZSA9IHZvaWQgMDtcbiAgICAgICAgaWYgKHJvdXRlLnRvLmNoYXJBdCgwKSA9PT0gJy8nKSB7XG4gICAgICAgICAgcGF0aG5hbWUgPSAoMCwgX1BhdHRlcm5VdGlscy5mb3JtYXRQYXR0ZXJuKShyb3V0ZS50bywgcGFyYW1zKTtcbiAgICAgICAgfSBlbHNlIGlmICghcm91dGUudG8pIHtcbiAgICAgICAgICBwYXRobmFtZSA9IGxvY2F0aW9uLnBhdGhuYW1lO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHZhciByb3V0ZUluZGV4ID0gbmV4dFN0YXRlLnJvdXRlcy5pbmRleE9mKHJvdXRlKTtcbiAgICAgICAgICB2YXIgcGFyZW50UGF0dGVybiA9IFJlZGlyZWN0LmdldFJvdXRlUGF0dGVybihuZXh0U3RhdGUucm91dGVzLCByb3V0ZUluZGV4IC0gMSk7XG4gICAgICAgICAgdmFyIHBhdHRlcm4gPSBwYXJlbnRQYXR0ZXJuLnJlcGxhY2UoL1xcLyokLywgJy8nKSArIHJvdXRlLnRvO1xuICAgICAgICAgIHBhdGhuYW1lID0gKDAsIF9QYXR0ZXJuVXRpbHMuZm9ybWF0UGF0dGVybikocGF0dGVybiwgcGFyYW1zKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHJlcGxhY2Uoe1xuICAgICAgICAgIHBhdGhuYW1lOiBwYXRobmFtZSxcbiAgICAgICAgICBxdWVyeTogcm91dGUucXVlcnkgfHwgbG9jYXRpb24ucXVlcnksXG4gICAgICAgICAgc3RhdGU6IHJvdXRlLnN0YXRlIHx8IGxvY2F0aW9uLnN0YXRlXG4gICAgICAgIH0pO1xuICAgICAgfTtcblxuICAgICAgcmV0dXJuIHJvdXRlO1xuICAgIH0sXG4gICAgZ2V0Um91dGVQYXR0ZXJuOiBmdW5jdGlvbiBnZXRSb3V0ZVBhdHRlcm4ocm91dGVzLCByb3V0ZUluZGV4KSB7XG4gICAgICB2YXIgcGFyZW50UGF0dGVybiA9ICcnO1xuXG4gICAgICBmb3IgKHZhciBpID0gcm91dGVJbmRleDsgaSA+PSAwOyBpLS0pIHtcbiAgICAgICAgdmFyIHJvdXRlID0gcm91dGVzW2ldO1xuICAgICAgICB2YXIgcGF0dGVybiA9IHJvdXRlLnBhdGggfHwgJyc7XG5cbiAgICAgICAgcGFyZW50UGF0dGVybiA9IHBhdHRlcm4ucmVwbGFjZSgvXFwvKiQvLCAnLycpICsgcGFyZW50UGF0dGVybjtcblxuICAgICAgICBpZiAocGF0dGVybi5pbmRleE9mKCcvJykgPT09IDApIGJyZWFrO1xuICAgICAgfVxuXG4gICAgICByZXR1cm4gJy8nICsgcGFyZW50UGF0dGVybjtcbiAgICB9XG4gIH0sXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgcGF0aDogc3RyaW5nLFxuICAgIGZyb206IHN0cmluZywgLy8gQWxpYXMgZm9yIHBhdGhcbiAgICB0bzogc3RyaW5nLmlzUmVxdWlyZWQsXG4gICAgcXVlcnk6IG9iamVjdCxcbiAgICBzdGF0ZTogb2JqZWN0LFxuICAgIG9uRW50ZXI6IF9JbnRlcm5hbFByb3BUeXBlcy5mYWxzeSxcbiAgICBjaGlsZHJlbjogX0ludGVybmFsUHJvcFR5cGVzLmZhbHN5XG4gIH0sXG5cbiAgLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHNhbml0eSBjaGVjayAqL1xuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICAhZmFsc2UgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnPFJlZGlyZWN0PiBlbGVtZW50cyBhcmUgZm9yIHJvdXRlciBjb25maWd1cmF0aW9uIG9ubHkgYW5kIHNob3VsZCBub3QgYmUgcmVuZGVyZWQnKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBSZWRpcmVjdDtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxudmFyIF9JbnRlcm5hbFByb3BUeXBlcyA9IHJlcXVpcmUoJy4vSW50ZXJuYWxQcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIF9SZWFjdCRQcm9wVHlwZXMgPSBfcmVhY3QyLmRlZmF1bHQuUHJvcFR5cGVzO1xudmFyIHN0cmluZyA9IF9SZWFjdCRQcm9wVHlwZXMuc3RyaW5nO1xudmFyIGZ1bmMgPSBfUmVhY3QkUHJvcFR5cGVzLmZ1bmM7XG5cbi8qKlxuICogQSA8Um91dGU+IGlzIHVzZWQgdG8gZGVjbGFyZSB3aGljaCBjb21wb25lbnRzIGFyZSByZW5kZXJlZCB0byB0aGVcbiAqIHBhZ2Ugd2hlbiB0aGUgVVJMIG1hdGNoZXMgYSBnaXZlbiBwYXR0ZXJuLlxuICpcbiAqIFJvdXRlcyBhcmUgYXJyYW5nZWQgaW4gYSBuZXN0ZWQgdHJlZSBzdHJ1Y3R1cmUuIFdoZW4gYSBuZXcgVVJMIGlzXG4gKiByZXF1ZXN0ZWQsIHRoZSB0cmVlIGlzIHNlYXJjaGVkIGRlcHRoLWZpcnN0IHRvIGZpbmQgYSByb3V0ZSB3aG9zZVxuICogcGF0aCBtYXRjaGVzIHRoZSBVUkwuICBXaGVuIG9uZSBpcyBmb3VuZCwgYWxsIHJvdXRlcyBpbiB0aGUgdHJlZVxuICogdGhhdCBsZWFkIHRvIGl0IGFyZSBjb25zaWRlcmVkIFwiYWN0aXZlXCIgYW5kIHRoZWlyIGNvbXBvbmVudHMgYXJlXG4gKiByZW5kZXJlZCBpbnRvIHRoZSBET00sIG5lc3RlZCBpbiB0aGUgc2FtZSBvcmRlciBhcyBpbiB0aGUgdHJlZS5cbiAqL1xuXG52YXIgUm91dGUgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ1JvdXRlJyxcblxuXG4gIHN0YXRpY3M6IHtcbiAgICBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQ6IF9Sb3V0ZVV0aWxzLmNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudFxuICB9LFxuXG4gIHByb3BUeXBlczoge1xuICAgIHBhdGg6IHN0cmluZyxcbiAgICBjb21wb25lbnQ6IF9JbnRlcm5hbFByb3BUeXBlcy5jb21wb25lbnQsXG4gICAgY29tcG9uZW50czogX0ludGVybmFsUHJvcFR5cGVzLmNvbXBvbmVudHMsXG4gICAgZ2V0Q29tcG9uZW50OiBmdW5jLFxuICAgIGdldENvbXBvbmVudHM6IGZ1bmNcbiAgfSxcblxuICAvKiBpc3RhbmJ1bCBpZ25vcmUgbmV4dDogc2FuaXR5IGNoZWNrICovXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgICFmYWxzZSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICc8Um91dGU+IGVsZW1lbnRzIGFyZSBmb3Igcm91dGVyIGNvbmZpZ3VyYXRpb24gb25seSBhbmQgc2hvdWxkIG5vdCBiZSByZW5kZXJlZCcpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IFJvdXRlO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBvYmplY3QgPSBfcmVhY3QyLmRlZmF1bHQuUHJvcFR5cGVzLm9iamVjdDtcblxuLyoqXG4gKiBUaGUgUm91dGVDb250ZXh0IG1peGluIHByb3ZpZGVzIGEgY29udmVuaWVudCB3YXkgZm9yIHJvdXRlXG4gKiBjb21wb25lbnRzIHRvIHNldCB0aGUgcm91dGUgaW4gY29udGV4dC4gVGhpcyBpcyBuZWVkZWQgZm9yXG4gKiByb3V0ZXMgdGhhdCByZW5kZXIgZWxlbWVudHMgdGhhdCB3YW50IHRvIHVzZSB0aGUgTGlmZWN5Y2xlXG4gKiBtaXhpbiB0byBwcmV2ZW50IHRyYW5zaXRpb25zLlxuICovXG5cbnZhciBSb3V0ZUNvbnRleHQgPSB7XG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgcm91dGU6IG9iamVjdC5pc1JlcXVpcmVkXG4gIH0sXG5cbiAgY2hpbGRDb250ZXh0VHlwZXM6IHtcbiAgICByb3V0ZTogb2JqZWN0LmlzUmVxdWlyZWRcbiAgfSxcblxuICBnZXRDaGlsZENvbnRleHQ6IGZ1bmN0aW9uIGdldENoaWxkQ29udGV4dCgpIHtcbiAgICByZXR1cm4ge1xuICAgICAgcm91dGU6IHRoaXMucHJvcHMucm91dGVcbiAgICB9O1xuICB9LFxuICBjb21wb25lbnRXaWxsTW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxNb3VudCgpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ1RoZSBgUm91dGVDb250ZXh0YCBtaXhpbiBpcyBkZXByZWNhdGVkLiBZb3UgY2FuIHByb3ZpZGUgYHRoaXMucHJvcHMucm91dGVgIG9uIGNvbnRleHQgd2l0aCB5b3VyIG93biBgY29udGV4dFR5cGVzYC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLXJvdXRlY29udGV4dG1peGluJykgOiB2b2lkIDA7XG4gIH1cbn07XG5cbmV4cG9ydHMuZGVmYXVsdCA9IFJvdXRlQ29udGV4dDtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZXhwb3J0cy5pc1JlYWN0Q2hpbGRyZW4gPSBpc1JlYWN0Q2hpbGRyZW47XG5leHBvcnRzLmNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudCA9IGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudDtcbmV4cG9ydHMuY3JlYXRlUm91dGVzRnJvbVJlYWN0Q2hpbGRyZW4gPSBjcmVhdGVSb3V0ZXNGcm9tUmVhY3RDaGlsZHJlbjtcbmV4cG9ydHMuY3JlYXRlUm91dGVzID0gY3JlYXRlUm91dGVzO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIGlzVmFsaWRDaGlsZChvYmplY3QpIHtcbiAgcmV0dXJuIG9iamVjdCA9PSBudWxsIHx8IF9yZWFjdDIuZGVmYXVsdC5pc1ZhbGlkRWxlbWVudChvYmplY3QpO1xufVxuXG5mdW5jdGlvbiBpc1JlYWN0Q2hpbGRyZW4ob2JqZWN0KSB7XG4gIHJldHVybiBpc1ZhbGlkQ2hpbGQob2JqZWN0KSB8fCBBcnJheS5pc0FycmF5KG9iamVjdCkgJiYgb2JqZWN0LmV2ZXJ5KGlzVmFsaWRDaGlsZCk7XG59XG5cbmZ1bmN0aW9uIGNyZWF0ZVJvdXRlKGRlZmF1bHRQcm9wcywgcHJvcHMpIHtcbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBkZWZhdWx0UHJvcHMsIHByb3BzKTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQpIHtcbiAgdmFyIHR5cGUgPSBlbGVtZW50LnR5cGU7XG4gIHZhciByb3V0ZSA9IGNyZWF0ZVJvdXRlKHR5cGUuZGVmYXVsdFByb3BzLCBlbGVtZW50LnByb3BzKTtcblxuICBpZiAocm91dGUuY2hpbGRyZW4pIHtcbiAgICB2YXIgY2hpbGRSb3V0ZXMgPSBjcmVhdGVSb3V0ZXNGcm9tUmVhY3RDaGlsZHJlbihyb3V0ZS5jaGlsZHJlbiwgcm91dGUpO1xuXG4gICAgaWYgKGNoaWxkUm91dGVzLmxlbmd0aCkgcm91dGUuY2hpbGRSb3V0ZXMgPSBjaGlsZFJvdXRlcztcblxuICAgIGRlbGV0ZSByb3V0ZS5jaGlsZHJlbjtcbiAgfVxuXG4gIHJldHVybiByb3V0ZTtcbn1cblxuLyoqXG4gKiBDcmVhdGVzIGFuZCByZXR1cm5zIGEgcm91dGVzIG9iamVjdCBmcm9tIHRoZSBnaXZlbiBSZWFjdENoaWxkcmVuLiBKU1hcbiAqIHByb3ZpZGVzIGEgY29udmVuaWVudCB3YXkgdG8gdmlzdWFsaXplIGhvdyByb3V0ZXMgaW4gdGhlIGhpZXJhcmNoeSBhcmVcbiAqIG5lc3RlZC5cbiAqXG4gKiAgIGltcG9ydCB7IFJvdXRlLCBjcmVhdGVSb3V0ZXNGcm9tUmVhY3RDaGlsZHJlbiB9IGZyb20gJ3JlYWN0LXJvdXRlcidcbiAqXG4gKiAgIGNvbnN0IHJvdXRlcyA9IGNyZWF0ZVJvdXRlc0Zyb21SZWFjdENoaWxkcmVuKFxuICogICAgIDxSb3V0ZSBjb21wb25lbnQ9e0FwcH0+XG4gKiAgICAgICA8Um91dGUgcGF0aD1cImhvbWVcIiBjb21wb25lbnQ9e0Rhc2hib2FyZH0vPlxuICogICAgICAgPFJvdXRlIHBhdGg9XCJuZXdzXCIgY29tcG9uZW50PXtOZXdzRmVlZH0vPlxuICogICAgIDwvUm91dGU+XG4gKiAgIClcbiAqXG4gKiBOb3RlOiBUaGlzIG1ldGhvZCBpcyBhdXRvbWF0aWNhbGx5IHVzZWQgd2hlbiB5b3UgcHJvdmlkZSA8Um91dGU+IGNoaWxkcmVuXG4gKiB0byBhIDxSb3V0ZXI+IGNvbXBvbmVudC5cbiAqL1xuZnVuY3Rpb24gY3JlYXRlUm91dGVzRnJvbVJlYWN0Q2hpbGRyZW4oY2hpbGRyZW4sIHBhcmVudFJvdXRlKSB7XG4gIHZhciByb3V0ZXMgPSBbXTtcblxuICBfcmVhY3QyLmRlZmF1bHQuQ2hpbGRyZW4uZm9yRWFjaChjaGlsZHJlbiwgZnVuY3Rpb24gKGVsZW1lbnQpIHtcbiAgICBpZiAoX3JlYWN0Mi5kZWZhdWx0LmlzVmFsaWRFbGVtZW50KGVsZW1lbnQpKSB7XG4gICAgICAvLyBDb21wb25lbnQgY2xhc3NlcyBtYXkgaGF2ZSBhIHN0YXRpYyBjcmVhdGUqIG1ldGhvZC5cbiAgICAgIGlmIChlbGVtZW50LnR5cGUuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KSB7XG4gICAgICAgIHZhciByb3V0ZSA9IGVsZW1lbnQudHlwZS5jcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQoZWxlbWVudCwgcGFyZW50Um91dGUpO1xuXG4gICAgICAgIGlmIChyb3V0ZSkgcm91dGVzLnB1c2gocm91dGUpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcm91dGVzLnB1c2goY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQpKTtcbiAgICAgIH1cbiAgICB9XG4gIH0pO1xuXG4gIHJldHVybiByb3V0ZXM7XG59XG5cbi8qKlxuICogQ3JlYXRlcyBhbmQgcmV0dXJucyBhbiBhcnJheSBvZiByb3V0ZXMgZnJvbSB0aGUgZ2l2ZW4gb2JqZWN0IHdoaWNoXG4gKiBtYXkgYmUgYSBKU1ggcm91dGUsIGEgcGxhaW4gb2JqZWN0IHJvdXRlLCBvciBhbiBhcnJheSBvZiBlaXRoZXIuXG4gKi9cbmZ1bmN0aW9uIGNyZWF0ZVJvdXRlcyhyb3V0ZXMpIHtcbiAgaWYgKGlzUmVhY3RDaGlsZHJlbihyb3V0ZXMpKSB7XG4gICAgcm91dGVzID0gY3JlYXRlUm91dGVzRnJvbVJlYWN0Q2hpbGRyZW4ocm91dGVzKTtcbiAgfSBlbHNlIGlmIChyb3V0ZXMgJiYgIUFycmF5LmlzQXJyYXkocm91dGVzKSkge1xuICAgIHJvdXRlcyA9IFtyb3V0ZXNdO1xuICB9XG5cbiAgcmV0dXJuIHJvdXRlcztcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfY3JlYXRlSGFzaEhpc3RvcnkgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi9jcmVhdGVIYXNoSGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZUhhc2hIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZUhhc2hIaXN0b3J5KTtcblxudmFyIF91c2VRdWVyaWVzID0gcmVxdWlyZSgnaGlzdG9yeS9saWIvdXNlUXVlcmllcycpO1xuXG52YXIgX3VzZVF1ZXJpZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUXVlcmllcyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlciA9IHJlcXVpcmUoJy4vY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXInKTtcblxudmFyIF9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcik7XG5cbnZhciBfSW50ZXJuYWxQcm9wVHlwZXMgPSByZXF1aXJlKCcuL0ludGVybmFsUHJvcFR5cGVzJyk7XG5cbnZhciBfUm91dGVyQ29udGV4dCA9IHJlcXVpcmUoJy4vUm91dGVyQ29udGV4dCcpO1xuXG52YXIgX1JvdXRlckNvbnRleHQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUm91dGVyQ29udGV4dCk7XG5cbnZhciBfUm91dGVVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVVdGlscycpO1xuXG52YXIgX1JvdXRlclV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZXJVdGlscycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBfb2JqZWN0V2l0aG91dFByb3BlcnRpZXMob2JqLCBrZXlzKSB7IHZhciB0YXJnZXQgPSB7fTsgZm9yICh2YXIgaSBpbiBvYmopIHsgaWYgKGtleXMuaW5kZXhPZihpKSA+PSAwKSBjb250aW51ZTsgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqLCBpKSkgY29udGludWU7IHRhcmdldFtpXSA9IG9ialtpXTsgfSByZXR1cm4gdGFyZ2V0OyB9XG5cbmZ1bmN0aW9uIGlzRGVwcmVjYXRlZEhpc3RvcnkoaGlzdG9yeSkge1xuICByZXR1cm4gIWhpc3RvcnkgfHwgIWhpc3RvcnkuX192Ml9jb21wYXRpYmxlX187XG59XG5cbi8qIGlzdGFuYnVsIGlnbm9yZSBuZXh0OiBzYW5pdHkgY2hlY2sgKi9cbmZ1bmN0aW9uIGlzVW5zdXBwb3J0ZWRIaXN0b3J5KGhpc3RvcnkpIHtcbiAgLy8gdjMgaGlzdG9yaWVzIGV4cG9zZSBnZXRDdXJyZW50TG9jYXRpb24sIGJ1dCBhcmVuJ3QgY3VycmVudGx5IHN1cHBvcnRlZC5cbiAgcmV0dXJuIGhpc3RvcnkgJiYgaGlzdG9yeS5nZXRDdXJyZW50TG9jYXRpb247XG59XG5cbnZhciBfUmVhY3QkUHJvcFR5cGVzID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcztcbnZhciBmdW5jID0gX1JlYWN0JFByb3BUeXBlcy5mdW5jO1xudmFyIG9iamVjdCA9IF9SZWFjdCRQcm9wVHlwZXMub2JqZWN0O1xuXG4vKipcbiAqIEEgPFJvdXRlcj4gaXMgYSBoaWdoLWxldmVsIEFQSSBmb3IgYXV0b21hdGljYWxseSBzZXR0aW5nIHVwXG4gKiBhIHJvdXRlciB0aGF0IHJlbmRlcnMgYSA8Um91dGVyQ29udGV4dD4gd2l0aCBhbGwgdGhlIHByb3BzXG4gKiBpdCBuZWVkcyBlYWNoIHRpbWUgdGhlIFVSTCBjaGFuZ2VzLlxuICovXG5cbnZhciBSb3V0ZXIgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ1JvdXRlcicsXG5cblxuICBwcm9wVHlwZXM6IHtcbiAgICBoaXN0b3J5OiBvYmplY3QsXG4gICAgY2hpbGRyZW46IF9JbnRlcm5hbFByb3BUeXBlcy5yb3V0ZXMsXG4gICAgcm91dGVzOiBfSW50ZXJuYWxQcm9wVHlwZXMucm91dGVzLCAvLyBhbGlhcyBmb3IgY2hpbGRyZW5cbiAgICByZW5kZXI6IGZ1bmMsXG4gICAgY3JlYXRlRWxlbWVudDogZnVuYyxcbiAgICBvbkVycm9yOiBmdW5jLFxuICAgIG9uVXBkYXRlOiBmdW5jLFxuXG4gICAgLy8gRGVwcmVjYXRlZDpcbiAgICBwYXJzZVF1ZXJ5U3RyaW5nOiBmdW5jLFxuICAgIHN0cmluZ2lmeVF1ZXJ5OiBmdW5jLFxuXG4gICAgLy8gUFJJVkFURTogRm9yIGNsaWVudC1zaWRlIHJlaHlkcmF0aW9uIG9mIHNlcnZlciBtYXRjaC5cbiAgICBtYXRjaENvbnRleHQ6IG9iamVjdFxuICB9LFxuXG4gIGdldERlZmF1bHRQcm9wczogZnVuY3Rpb24gZ2V0RGVmYXVsdFByb3BzKCkge1xuICAgIHJldHVybiB7XG4gICAgICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcihwcm9wcykge1xuICAgICAgICByZXR1cm4gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUVsZW1lbnQoX1JvdXRlckNvbnRleHQyLmRlZmF1bHQsIHByb3BzKTtcbiAgICAgIH1cbiAgICB9O1xuICB9LFxuICBnZXRJbml0aWFsU3RhdGU6IGZ1bmN0aW9uIGdldEluaXRpYWxTdGF0ZSgpIHtcbiAgICByZXR1cm4ge1xuICAgICAgbG9jYXRpb246IG51bGwsXG4gICAgICByb3V0ZXM6IG51bGwsXG4gICAgICBwYXJhbXM6IG51bGwsXG4gICAgICBjb21wb25lbnRzOiBudWxsXG4gICAgfTtcbiAgfSxcbiAgaGFuZGxlRXJyb3I6IGZ1bmN0aW9uIGhhbmRsZUVycm9yKGVycm9yKSB7XG4gICAgaWYgKHRoaXMucHJvcHMub25FcnJvcikge1xuICAgICAgdGhpcy5wcm9wcy5vbkVycm9yLmNhbGwodGhpcywgZXJyb3IpO1xuICAgIH0gZWxzZSB7XG4gICAgICAvLyBUaHJvdyBlcnJvcnMgYnkgZGVmYXVsdCBzbyB3ZSBkb24ndCBzaWxlbnRseSBzd2FsbG93IHRoZW0hXG4gICAgICB0aHJvdyBlcnJvcjsgLy8gVGhpcyBlcnJvciBwcm9iYWJseSBvY2N1cnJlZCBpbiBnZXRDaGlsZFJvdXRlcyBvciBnZXRDb21wb25lbnRzLlxuICAgIH1cbiAgfSxcbiAgY29tcG9uZW50V2lsbE1vdW50OiBmdW5jdGlvbiBjb21wb25lbnRXaWxsTW91bnQoKSB7XG4gICAgdmFyIF90aGlzID0gdGhpcztcblxuICAgIHZhciBfcHJvcHMgPSB0aGlzLnByb3BzO1xuICAgIHZhciBwYXJzZVF1ZXJ5U3RyaW5nID0gX3Byb3BzLnBhcnNlUXVlcnlTdHJpbmc7XG4gICAgdmFyIHN0cmluZ2lmeVF1ZXJ5ID0gX3Byb3BzLnN0cmluZ2lmeVF1ZXJ5O1xuXG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoIShwYXJzZVF1ZXJ5U3RyaW5nIHx8IHN0cmluZ2lmeVF1ZXJ5KSwgJ2BwYXJzZVF1ZXJ5U3RyaW5nYCBhbmQgYHN0cmluZ2lmeVF1ZXJ5YCBhcmUgZGVwcmVjYXRlZC4gUGxlYXNlIGNyZWF0ZSBhIGN1c3RvbSBoaXN0b3J5LiBodHRwOi8vdGlueS5jYy9yb3V0ZXItY3VzdG9tcXVlcnlzdHJpbmcnKSA6IHZvaWQgMDtcblxuICAgIHZhciBfY3JlYXRlUm91dGVyT2JqZWN0cyA9IHRoaXMuY3JlYXRlUm91dGVyT2JqZWN0cygpO1xuXG4gICAgdmFyIGhpc3RvcnkgPSBfY3JlYXRlUm91dGVyT2JqZWN0cy5oaXN0b3J5O1xuICAgIHZhciB0cmFuc2l0aW9uTWFuYWdlciA9IF9jcmVhdGVSb3V0ZXJPYmplY3RzLnRyYW5zaXRpb25NYW5hZ2VyO1xuICAgIHZhciByb3V0ZXIgPSBfY3JlYXRlUm91dGVyT2JqZWN0cy5yb3V0ZXI7XG5cblxuICAgIHRoaXMuX3VubGlzdGVuID0gdHJhbnNpdGlvbk1hbmFnZXIubGlzdGVuKGZ1bmN0aW9uIChlcnJvciwgc3RhdGUpIHtcbiAgICAgIGlmIChlcnJvcikge1xuICAgICAgICBfdGhpcy5oYW5kbGVFcnJvcihlcnJvcik7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBfdGhpcy5zZXRTdGF0ZShzdGF0ZSwgX3RoaXMucHJvcHMub25VcGRhdGUpO1xuICAgICAgfVxuICAgIH0pO1xuXG4gICAgdGhpcy5oaXN0b3J5ID0gaGlzdG9yeTtcbiAgICB0aGlzLnJvdXRlciA9IHJvdXRlcjtcbiAgfSxcbiAgY3JlYXRlUm91dGVyT2JqZWN0czogZnVuY3Rpb24gY3JlYXRlUm91dGVyT2JqZWN0cygpIHtcbiAgICB2YXIgbWF0Y2hDb250ZXh0ID0gdGhpcy5wcm9wcy5tYXRjaENvbnRleHQ7XG5cbiAgICBpZiAobWF0Y2hDb250ZXh0KSB7XG4gICAgICByZXR1cm4gbWF0Y2hDb250ZXh0O1xuICAgIH1cblxuICAgIHZhciBoaXN0b3J5ID0gdGhpcy5wcm9wcy5oaXN0b3J5O1xuICAgIHZhciBfcHJvcHMyID0gdGhpcy5wcm9wcztcbiAgICB2YXIgcm91dGVzID0gX3Byb3BzMi5yb3V0ZXM7XG4gICAgdmFyIGNoaWxkcmVuID0gX3Byb3BzMi5jaGlsZHJlbjtcblxuXG4gICAgISFpc1Vuc3VwcG9ydGVkSGlzdG9yeShoaXN0b3J5KSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdZb3UgaGF2ZSBwcm92aWRlZCBhIGhpc3Rvcnkgb2JqZWN0IGNyZWF0ZWQgd2l0aCBoaXN0b3J5IHYzLnguICcgKyAnVGhpcyB2ZXJzaW9uIG9mIFJlYWN0IFJvdXRlciBpcyBub3QgY29tcGF0aWJsZSB3aXRoIHYzIGhpc3RvcnkgJyArICdvYmplY3RzLiBQbGVhc2UgdXNlIGhpc3RvcnkgdjIueCBpbnN0ZWFkLicpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICAgIGlmIChpc0RlcHJlY2F0ZWRIaXN0b3J5KGhpc3RvcnkpKSB7XG4gICAgICBoaXN0b3J5ID0gdGhpcy53cmFwRGVwcmVjYXRlZEhpc3RvcnkoaGlzdG9yeSk7XG4gICAgfVxuXG4gICAgdmFyIHRyYW5zaXRpb25NYW5hZ2VyID0gKDAsIF9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcjIuZGVmYXVsdCkoaGlzdG9yeSwgKDAsIF9Sb3V0ZVV0aWxzLmNyZWF0ZVJvdXRlcykocm91dGVzIHx8IGNoaWxkcmVuKSk7XG4gICAgdmFyIHJvdXRlciA9ICgwLCBfUm91dGVyVXRpbHMuY3JlYXRlUm91dGVyT2JqZWN0KShoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG4gICAgdmFyIHJvdXRpbmdIaXN0b3J5ID0gKDAsIF9Sb3V0ZXJVdGlscy5jcmVhdGVSb3V0aW5nSGlzdG9yeSkoaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXIpO1xuXG4gICAgcmV0dXJuIHsgaGlzdG9yeTogcm91dGluZ0hpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyOiB0cmFuc2l0aW9uTWFuYWdlciwgcm91dGVyOiByb3V0ZXIgfTtcbiAgfSxcbiAgd3JhcERlcHJlY2F0ZWRIaXN0b3J5OiBmdW5jdGlvbiB3cmFwRGVwcmVjYXRlZEhpc3RvcnkoaGlzdG9yeSkge1xuICAgIHZhciBfcHJvcHMzID0gdGhpcy5wcm9wcztcbiAgICB2YXIgcGFyc2VRdWVyeVN0cmluZyA9IF9wcm9wczMucGFyc2VRdWVyeVN0cmluZztcbiAgICB2YXIgc3RyaW5naWZ5UXVlcnkgPSBfcHJvcHMzLnN0cmluZ2lmeVF1ZXJ5O1xuXG5cbiAgICB2YXIgY3JlYXRlSGlzdG9yeSA9IHZvaWQgMDtcbiAgICBpZiAoaGlzdG9yeSkge1xuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdJdCBhcHBlYXJzIHlvdSBoYXZlIHByb3ZpZGVkIGEgZGVwcmVjYXRlZCBoaXN0b3J5IG9iamVjdCB0byBgPFJvdXRlci8+YCwgcGxlYXNlIHVzZSBhIGhpc3RvcnkgcHJvdmlkZWQgYnkgJyArICdSZWFjdCBSb3V0ZXIgd2l0aCBgaW1wb3J0IHsgYnJvd3Nlckhpc3RvcnkgfSBmcm9tIFxcJ3JlYWN0LXJvdXRlclxcJ2Agb3IgYGltcG9ydCB7IGhhc2hIaXN0b3J5IH0gZnJvbSBcXCdyZWFjdC1yb3V0ZXJcXCdgLiAnICsgJ0lmIHlvdSBhcmUgdXNpbmcgYSBjdXN0b20gaGlzdG9yeSBwbGVhc2UgY3JlYXRlIGl0IHdpdGggYHVzZVJvdXRlckhpc3RvcnlgLCBzZWUgaHR0cDovL3RpbnkuY2Mvcm91dGVyLXVzaW5naGlzdG9yeSBmb3IgZGV0YWlscy4nKSA6IHZvaWQgMDtcbiAgICAgIGNyZWF0ZUhpc3RvcnkgPSBmdW5jdGlvbiBjcmVhdGVIaXN0b3J5KCkge1xuICAgICAgICByZXR1cm4gaGlzdG9yeTtcbiAgICAgIH07XG4gICAgfSBlbHNlIHtcbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnYFJvdXRlcmAgbm8gbG9uZ2VyIGRlZmF1bHRzIHRoZSBoaXN0b3J5IHByb3AgdG8gaGFzaCBoaXN0b3J5LiBQbGVhc2UgdXNlIHRoZSBgaGFzaEhpc3RvcnlgIHNpbmdsZXRvbiBpbnN0ZWFkLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItZGVmYXVsdGhpc3RvcnknKSA6IHZvaWQgMDtcbiAgICAgIGNyZWF0ZUhpc3RvcnkgPSBfY3JlYXRlSGFzaEhpc3RvcnkyLmRlZmF1bHQ7XG4gICAgfVxuXG4gICAgcmV0dXJuICgwLCBfdXNlUXVlcmllczIuZGVmYXVsdCkoY3JlYXRlSGlzdG9yeSkoeyBwYXJzZVF1ZXJ5U3RyaW5nOiBwYXJzZVF1ZXJ5U3RyaW5nLCBzdHJpbmdpZnlRdWVyeTogc3RyaW5naWZ5UXVlcnkgfSk7XG4gIH0sXG5cblxuICAvKiBpc3RhbmJ1bCBpZ25vcmUgbmV4dDogc2FuaXR5IGNoZWNrICovXG4gIGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHM6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkobmV4dFByb3BzLmhpc3RvcnkgPT09IHRoaXMucHJvcHMuaGlzdG9yeSwgJ1lvdSBjYW5ub3QgY2hhbmdlIDxSb3V0ZXIgaGlzdG9yeT47IGl0IHdpbGwgYmUgaWdub3JlZCcpIDogdm9pZCAwO1xuXG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoKG5leHRQcm9wcy5yb3V0ZXMgfHwgbmV4dFByb3BzLmNoaWxkcmVuKSA9PT0gKHRoaXMucHJvcHMucm91dGVzIHx8IHRoaXMucHJvcHMuY2hpbGRyZW4pLCAnWW91IGNhbm5vdCBjaGFuZ2UgPFJvdXRlciByb3V0ZXM+OyBpdCB3aWxsIGJlIGlnbm9yZWQnKSA6IHZvaWQgMDtcbiAgfSxcbiAgY29tcG9uZW50V2lsbFVubW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgIGlmICh0aGlzLl91bmxpc3RlbikgdGhpcy5fdW5saXN0ZW4oKTtcbiAgfSxcbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgdmFyIF9zdGF0ZSA9IHRoaXMuc3RhdGU7XG4gICAgdmFyIGxvY2F0aW9uID0gX3N0YXRlLmxvY2F0aW9uO1xuICAgIHZhciByb3V0ZXMgPSBfc3RhdGUucm91dGVzO1xuICAgIHZhciBwYXJhbXMgPSBfc3RhdGUucGFyYW1zO1xuICAgIHZhciBjb21wb25lbnRzID0gX3N0YXRlLmNvbXBvbmVudHM7XG4gICAgdmFyIF9wcm9wczQgPSB0aGlzLnByb3BzO1xuICAgIHZhciBjcmVhdGVFbGVtZW50ID0gX3Byb3BzNC5jcmVhdGVFbGVtZW50O1xuICAgIHZhciByZW5kZXIgPSBfcHJvcHM0LnJlbmRlcjtcblxuICAgIHZhciBwcm9wcyA9IF9vYmplY3RXaXRob3V0UHJvcGVydGllcyhfcHJvcHM0LCBbJ2NyZWF0ZUVsZW1lbnQnLCAncmVuZGVyJ10pO1xuXG4gICAgaWYgKGxvY2F0aW9uID09IG51bGwpIHJldHVybiBudWxsOyAvLyBBc3luYyBtYXRjaFxuXG4gICAgLy8gT25seSBmb3J3YXJkIG5vbi1Sb3V0ZXItc3BlY2lmaWMgcHJvcHMgdG8gcm91dGluZyBjb250ZXh0LCBhcyB0aG9zZSBhcmVcbiAgICAvLyB0aGUgb25seSBvbmVzIHRoYXQgbWlnaHQgYmUgY3VzdG9tIHJvdXRpbmcgY29udGV4dCBwcm9wcy5cbiAgICBPYmplY3Qua2V5cyhSb3V0ZXIucHJvcFR5cGVzKS5mb3JFYWNoKGZ1bmN0aW9uIChwcm9wVHlwZSkge1xuICAgICAgcmV0dXJuIGRlbGV0ZSBwcm9wc1twcm9wVHlwZV07XG4gICAgfSk7XG5cbiAgICByZXR1cm4gcmVuZGVyKF9leHRlbmRzKHt9LCBwcm9wcywge1xuICAgICAgaGlzdG9yeTogdGhpcy5oaXN0b3J5LFxuICAgICAgcm91dGVyOiB0aGlzLnJvdXRlcixcbiAgICAgIGxvY2F0aW9uOiBsb2NhdGlvbixcbiAgICAgIHJvdXRlczogcm91dGVzLFxuICAgICAgcGFyYW1zOiBwYXJhbXMsXG4gICAgICBjb21wb25lbnRzOiBjb21wb25lbnRzLFxuICAgICAgY3JlYXRlRWxlbWVudDogY3JlYXRlRWxlbWVudFxuICAgIH0pKTtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IFJvdXRlcjtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF90eXBlb2YgPSB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgdHlwZW9mIFN5bWJvbC5pdGVyYXRvciA9PT0gXCJzeW1ib2xcIiA/IGZ1bmN0aW9uIChvYmopIHsgcmV0dXJuIHR5cGVvZiBvYmo7IH0gOiBmdW5jdGlvbiAob2JqKSB7IHJldHVybiBvYmogJiYgdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIG9iai5jb25zdHJ1Y3RvciA9PT0gU3ltYm9sID8gXCJzeW1ib2xcIiA6IHR5cGVvZiBvYmo7IH07XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzID0gcmVxdWlyZSgnLi9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzJyk7XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzKTtcblxudmFyIF9nZXRSb3V0ZVBhcmFtcyA9IHJlcXVpcmUoJy4vZ2V0Um91dGVQYXJhbXMnKTtcblxudmFyIF9nZXRSb3V0ZVBhcmFtczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9nZXRSb3V0ZVBhcmFtcyk7XG5cbnZhciBfUm91dGVVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVVdGlscycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgYXJyYXkgPSBfUmVhY3QkUHJvcFR5cGVzLmFycmF5O1xudmFyIGZ1bmMgPSBfUmVhY3QkUHJvcFR5cGVzLmZ1bmM7XG52YXIgb2JqZWN0ID0gX1JlYWN0JFByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogQSA8Um91dGVyQ29udGV4dD4gcmVuZGVycyB0aGUgY29tcG9uZW50IHRyZWUgZm9yIGEgZ2l2ZW4gcm91dGVyIHN0YXRlXG4gKiBhbmQgc2V0cyB0aGUgaGlzdG9yeSBvYmplY3QgYW5kIHRoZSBjdXJyZW50IGxvY2F0aW9uIGluIGNvbnRleHQuXG4gKi9cblxudmFyIFJvdXRlckNvbnRleHQgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ1JvdXRlckNvbnRleHQnLFxuXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgaGlzdG9yeTogb2JqZWN0LFxuICAgIHJvdXRlcjogb2JqZWN0LmlzUmVxdWlyZWQsXG4gICAgbG9jYXRpb246IG9iamVjdC5pc1JlcXVpcmVkLFxuICAgIHJvdXRlczogYXJyYXkuaXNSZXF1aXJlZCxcbiAgICBwYXJhbXM6IG9iamVjdC5pc1JlcXVpcmVkLFxuICAgIGNvbXBvbmVudHM6IGFycmF5LmlzUmVxdWlyZWQsXG4gICAgY3JlYXRlRWxlbWVudDogZnVuYy5pc1JlcXVpcmVkXG4gIH0sXG5cbiAgZ2V0RGVmYXVsdFByb3BzOiBmdW5jdGlvbiBnZXREZWZhdWx0UHJvcHMoKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIGNyZWF0ZUVsZW1lbnQ6IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50XG4gICAgfTtcbiAgfSxcblxuXG4gIGNoaWxkQ29udGV4dFR5cGVzOiB7XG4gICAgaGlzdG9yeTogb2JqZWN0LFxuICAgIGxvY2F0aW9uOiBvYmplY3QuaXNSZXF1aXJlZCxcbiAgICByb3V0ZXI6IG9iamVjdC5pc1JlcXVpcmVkXG4gIH0sXG5cbiAgZ2V0Q2hpbGRDb250ZXh0OiBmdW5jdGlvbiBnZXRDaGlsZENvbnRleHQoKSB7XG4gICAgdmFyIF9wcm9wcyA9IHRoaXMucHJvcHM7XG4gICAgdmFyIHJvdXRlciA9IF9wcm9wcy5yb3V0ZXI7XG4gICAgdmFyIGhpc3RvcnkgPSBfcHJvcHMuaGlzdG9yeTtcbiAgICB2YXIgbG9jYXRpb24gPSBfcHJvcHMubG9jYXRpb247XG5cbiAgICBpZiAoIXJvdXRlcikge1xuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdgPFJvdXRlckNvbnRleHQ+YCBleHBlY3RzIGEgYHJvdXRlcmAgcmF0aGVyIHRoYW4gYSBgaGlzdG9yeWAnKSA6IHZvaWQgMDtcblxuICAgICAgcm91dGVyID0gX2V4dGVuZHMoe30sIGhpc3RvcnksIHtcbiAgICAgICAgc2V0Um91dGVMZWF2ZUhvb2s6IGhpc3RvcnkubGlzdGVuQmVmb3JlTGVhdmluZ1JvdXRlXG4gICAgICB9KTtcbiAgICAgIGRlbGV0ZSByb3V0ZXIubGlzdGVuQmVmb3JlTGVhdmluZ1JvdXRlO1xuICAgIH1cblxuICAgIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICBsb2NhdGlvbiA9ICgwLCBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllczIuZGVmYXVsdCkobG9jYXRpb24sICdgY29udGV4dC5sb2NhdGlvbmAgaXMgZGVwcmVjYXRlZCwgcGxlYXNlIHVzZSBhIHJvdXRlIGNvbXBvbmVudFxcJ3MgYHByb3BzLmxvY2F0aW9uYCBpbnN0ZWFkLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItYWNjZXNzaW5nbG9jYXRpb24nKTtcbiAgICB9XG5cbiAgICByZXR1cm4geyBoaXN0b3J5OiBoaXN0b3J5LCBsb2NhdGlvbjogbG9jYXRpb24sIHJvdXRlcjogcm91dGVyIH07XG4gIH0sXG4gIGNyZWF0ZUVsZW1lbnQ6IGZ1bmN0aW9uIGNyZWF0ZUVsZW1lbnQoY29tcG9uZW50LCBwcm9wcykge1xuICAgIHJldHVybiBjb21wb25lbnQgPT0gbnVsbCA/IG51bGwgOiB0aGlzLnByb3BzLmNyZWF0ZUVsZW1lbnQoY29tcG9uZW50LCBwcm9wcyk7XG4gIH0sXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHZhciBfdGhpcyA9IHRoaXM7XG5cbiAgICB2YXIgX3Byb3BzMiA9IHRoaXMucHJvcHM7XG4gICAgdmFyIGhpc3RvcnkgPSBfcHJvcHMyLmhpc3Rvcnk7XG4gICAgdmFyIGxvY2F0aW9uID0gX3Byb3BzMi5sb2NhdGlvbjtcbiAgICB2YXIgcm91dGVzID0gX3Byb3BzMi5yb3V0ZXM7XG4gICAgdmFyIHBhcmFtcyA9IF9wcm9wczIucGFyYW1zO1xuICAgIHZhciBjb21wb25lbnRzID0gX3Byb3BzMi5jb21wb25lbnRzO1xuXG4gICAgdmFyIGVsZW1lbnQgPSBudWxsO1xuXG4gICAgaWYgKGNvbXBvbmVudHMpIHtcbiAgICAgIGVsZW1lbnQgPSBjb21wb25lbnRzLnJlZHVjZVJpZ2h0KGZ1bmN0aW9uIChlbGVtZW50LCBjb21wb25lbnRzLCBpbmRleCkge1xuICAgICAgICBpZiAoY29tcG9uZW50cyA9PSBudWxsKSByZXR1cm4gZWxlbWVudDsgLy8gRG9uJ3QgY3JlYXRlIG5ldyBjaGlsZHJlbjsgdXNlIHRoZSBncmFuZGNoaWxkcmVuLlxuXG4gICAgICAgIHZhciByb3V0ZSA9IHJvdXRlc1tpbmRleF07XG4gICAgICAgIHZhciByb3V0ZVBhcmFtcyA9ICgwLCBfZ2V0Um91dGVQYXJhbXMyLmRlZmF1bHQpKHJvdXRlLCBwYXJhbXMpO1xuICAgICAgICB2YXIgcHJvcHMgPSB7XG4gICAgICAgICAgaGlzdG9yeTogaGlzdG9yeSxcbiAgICAgICAgICBsb2NhdGlvbjogbG9jYXRpb24sXG4gICAgICAgICAgcGFyYW1zOiBwYXJhbXMsXG4gICAgICAgICAgcm91dGU6IHJvdXRlLFxuICAgICAgICAgIHJvdXRlUGFyYW1zOiByb3V0ZVBhcmFtcyxcbiAgICAgICAgICByb3V0ZXM6IHJvdXRlc1xuICAgICAgICB9O1xuXG4gICAgICAgIGlmICgoMCwgX1JvdXRlVXRpbHMuaXNSZWFjdENoaWxkcmVuKShlbGVtZW50KSkge1xuICAgICAgICAgIHByb3BzLmNoaWxkcmVuID0gZWxlbWVudDtcbiAgICAgICAgfSBlbHNlIGlmIChlbGVtZW50KSB7XG4gICAgICAgICAgZm9yICh2YXIgcHJvcCBpbiBlbGVtZW50KSB7XG4gICAgICAgICAgICBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKGVsZW1lbnQsIHByb3ApKSBwcm9wc1twcm9wXSA9IGVsZW1lbnRbcHJvcF07XG4gICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgaWYgKCh0eXBlb2YgY29tcG9uZW50cyA9PT0gJ3VuZGVmaW5lZCcgPyAndW5kZWZpbmVkJyA6IF90eXBlb2YoY29tcG9uZW50cykpID09PSAnb2JqZWN0Jykge1xuICAgICAgICAgIHZhciBlbGVtZW50cyA9IHt9O1xuXG4gICAgICAgICAgZm9yICh2YXIga2V5IGluIGNvbXBvbmVudHMpIHtcbiAgICAgICAgICAgIGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoY29tcG9uZW50cywga2V5KSkge1xuICAgICAgICAgICAgICAvLyBQYXNzIHRocm91Z2ggdGhlIGtleSBhcyBhIHByb3AgdG8gY3JlYXRlRWxlbWVudCB0byBhbGxvd1xuICAgICAgICAgICAgICAvLyBjdXN0b20gY3JlYXRlRWxlbWVudCBmdW5jdGlvbnMgdG8ga25vdyB3aGljaCBuYW1lZCBjb21wb25lbnRcbiAgICAgICAgICAgICAgLy8gdGhleSdyZSByZW5kZXJpbmcsIGZvciBlLmcuIG1hdGNoaW5nIHVwIHRvIGZldGNoZWQgZGF0YS5cbiAgICAgICAgICAgICAgZWxlbWVudHNba2V5XSA9IF90aGlzLmNyZWF0ZUVsZW1lbnQoY29tcG9uZW50c1trZXldLCBfZXh0ZW5kcyh7XG4gICAgICAgICAgICAgICAga2V5OiBrZXkgfSwgcHJvcHMpKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9XG5cbiAgICAgICAgICByZXR1cm4gZWxlbWVudHM7XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gX3RoaXMuY3JlYXRlRWxlbWVudChjb21wb25lbnRzLCBwcm9wcyk7XG4gICAgICB9LCBlbGVtZW50KTtcbiAgICB9XG5cbiAgICAhKGVsZW1lbnQgPT09IG51bGwgfHwgZWxlbWVudCA9PT0gZmFsc2UgfHwgX3JlYWN0Mi5kZWZhdWx0LmlzVmFsaWRFbGVtZW50KGVsZW1lbnQpKSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdUaGUgcm9vdCByb3V0ZSBtdXN0IHJlbmRlciBhIHNpbmdsZSBlbGVtZW50JykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuXG4gICAgcmV0dXJuIGVsZW1lbnQ7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBSb3V0ZXJDb250ZXh0O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5leHBvcnRzLmNyZWF0ZVJvdXRlck9iamVjdCA9IGNyZWF0ZVJvdXRlck9iamVjdDtcbmV4cG9ydHMuY3JlYXRlUm91dGluZ0hpc3RvcnkgPSBjcmVhdGVSb3V0aW5nSGlzdG9yeTtcblxudmFyIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzID0gcmVxdWlyZSgnLi9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzJyk7XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gY3JlYXRlUm91dGVyT2JqZWN0KGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyKSB7XG4gIHJldHVybiBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwge1xuICAgIHNldFJvdXRlTGVhdmVIb29rOiB0cmFuc2l0aW9uTWFuYWdlci5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUsXG4gICAgaXNBY3RpdmU6IHRyYW5zaXRpb25NYW5hZ2VyLmlzQWN0aXZlXG4gIH0pO1xufVxuXG4vLyBkZXByZWNhdGVkXG5mdW5jdGlvbiBjcmVhdGVSb3V0aW5nSGlzdG9yeShoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcikge1xuICBoaXN0b3J5ID0gX2V4dGVuZHMoe30sIGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyKTtcblxuICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgIGhpc3RvcnkgPSAoMCwgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMyLmRlZmF1bHQpKGhpc3RvcnksICdgcHJvcHMuaGlzdG9yeWAgYW5kIGBjb250ZXh0Lmhpc3RvcnlgIGFyZSBkZXByZWNhdGVkLiBQbGVhc2UgdXNlIGBjb250ZXh0LnJvdXRlcmAuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1jb250ZXh0Y2hhbmdlcycpO1xuICB9XG5cbiAgcmV0dXJuIGhpc3Rvcnk7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfUm91dGVyQ29udGV4dCA9IHJlcXVpcmUoJy4vUm91dGVyQ29udGV4dCcpO1xuXG52YXIgX1JvdXRlckNvbnRleHQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUm91dGVyQ29udGV4dCk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBSb3V0aW5nQ29udGV4dCA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnUm91dGluZ0NvbnRleHQnLFxuICBjb21wb25lbnRXaWxsTW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxNb3VudCgpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2BSb3V0aW5nQ29udGV4dGAgaGFzIGJlZW4gcmVuYW1lZCB0byBgUm91dGVyQ29udGV4dGAuIFBsZWFzZSB1c2UgYGltcG9ydCB7IFJvdXRlckNvbnRleHQgfSBmcm9tIFxcJ3JlYWN0LXJvdXRlclxcJ2AuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1yb3V0ZXJjb250ZXh0JykgOiB2b2lkIDA7XG4gIH0sXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHJldHVybiBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudChfUm91dGVyQ29udGV4dDIuZGVmYXVsdCwgdGhpcy5wcm9wcyk7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBSb3V0aW5nQ29udGV4dDtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMucnVuRW50ZXJIb29rcyA9IHJ1bkVudGVySG9va3M7XG5leHBvcnRzLnJ1bkNoYW5nZUhvb2tzID0gcnVuQ2hhbmdlSG9va3M7XG5leHBvcnRzLnJ1bkxlYXZlSG9va3MgPSBydW5MZWF2ZUhvb2tzO1xuXG52YXIgX0FzeW5jVXRpbHMgPSByZXF1aXJlKCcuL0FzeW5jVXRpbHMnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gY3JlYXRlVHJhbnNpdGlvbkhvb2soaG9vaywgcm91dGUsIGFzeW5jQXJpdHkpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICBmb3IgKHZhciBfbGVuID0gYXJndW1lbnRzLmxlbmd0aCwgYXJncyA9IEFycmF5KF9sZW4pLCBfa2V5ID0gMDsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgICAgYXJnc1tfa2V5XSA9IGFyZ3VtZW50c1tfa2V5XTtcbiAgICB9XG5cbiAgICBob29rLmFwcGx5KHJvdXRlLCBhcmdzKTtcblxuICAgIGlmIChob29rLmxlbmd0aCA8IGFzeW5jQXJpdHkpIHtcbiAgICAgIHZhciBjYWxsYmFjayA9IGFyZ3NbYXJncy5sZW5ndGggLSAxXTtcbiAgICAgIC8vIEFzc3VtZSBob29rIGV4ZWN1dGVzIHN5bmNocm9ub3VzbHkgYW5kXG4gICAgICAvLyBhdXRvbWF0aWNhbGx5IGNhbGwgdGhlIGNhbGxiYWNrLlxuICAgICAgY2FsbGJhY2soKTtcbiAgICB9XG4gIH07XG59XG5cbmZ1bmN0aW9uIGdldEVudGVySG9va3Mocm91dGVzKSB7XG4gIHJldHVybiByb3V0ZXMucmVkdWNlKGZ1bmN0aW9uIChob29rcywgcm91dGUpIHtcbiAgICBpZiAocm91dGUub25FbnRlcikgaG9va3MucHVzaChjcmVhdGVUcmFuc2l0aW9uSG9vayhyb3V0ZS5vbkVudGVyLCByb3V0ZSwgMykpO1xuXG4gICAgcmV0dXJuIGhvb2tzO1xuICB9LCBbXSk7XG59XG5cbmZ1bmN0aW9uIGdldENoYW5nZUhvb2tzKHJvdXRlcykge1xuICByZXR1cm4gcm91dGVzLnJlZHVjZShmdW5jdGlvbiAoaG9va3MsIHJvdXRlKSB7XG4gICAgaWYgKHJvdXRlLm9uQ2hhbmdlKSBob29rcy5wdXNoKGNyZWF0ZVRyYW5zaXRpb25Ib29rKHJvdXRlLm9uQ2hhbmdlLCByb3V0ZSwgNCkpO1xuICAgIHJldHVybiBob29rcztcbiAgfSwgW10pO1xufVxuXG5mdW5jdGlvbiBydW5UcmFuc2l0aW9uSG9va3MobGVuZ3RoLCBpdGVyLCBjYWxsYmFjaykge1xuICBpZiAoIWxlbmd0aCkge1xuICAgIGNhbGxiYWNrKCk7XG4gICAgcmV0dXJuO1xuICB9XG5cbiAgdmFyIHJlZGlyZWN0SW5mbyA9IHZvaWQgMDtcbiAgZnVuY3Rpb24gcmVwbGFjZShsb2NhdGlvbiwgZGVwcmVjYXRlZFBhdGhuYW1lLCBkZXByZWNhdGVkUXVlcnkpIHtcbiAgICBpZiAoZGVwcmVjYXRlZFBhdGhuYW1lKSB7XG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2ByZXBsYWNlU3RhdGUoc3RhdGUsIHBhdGhuYW1lLCBxdWVyeSkgaXMgZGVwcmVjYXRlZDsgdXNlIGByZXBsYWNlKGxvY2F0aW9uKWAgd2l0aCBhIGxvY2F0aW9uIGRlc2NyaXB0b3IgaW5zdGVhZC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWlzQWN0aXZlZGVwcmVjYXRlZCcpIDogdm9pZCAwO1xuICAgICAgcmVkaXJlY3RJbmZvID0ge1xuICAgICAgICBwYXRobmFtZTogZGVwcmVjYXRlZFBhdGhuYW1lLFxuICAgICAgICBxdWVyeTogZGVwcmVjYXRlZFF1ZXJ5LFxuICAgICAgICBzdGF0ZTogbG9jYXRpb25cbiAgICAgIH07XG5cbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICByZWRpcmVjdEluZm8gPSBsb2NhdGlvbjtcbiAgfVxuXG4gICgwLCBfQXN5bmNVdGlscy5sb29wQXN5bmMpKGxlbmd0aCwgZnVuY3Rpb24gKGluZGV4LCBuZXh0LCBkb25lKSB7XG4gICAgaXRlcihpbmRleCwgcmVwbGFjZSwgZnVuY3Rpb24gKGVycm9yKSB7XG4gICAgICBpZiAoZXJyb3IgfHwgcmVkaXJlY3RJbmZvKSB7XG4gICAgICAgIGRvbmUoZXJyb3IsIHJlZGlyZWN0SW5mbyk7IC8vIE5vIG5lZWQgdG8gY29udGludWUuXG4gICAgICB9IGVsc2Uge1xuICAgICAgICBuZXh0KCk7XG4gICAgICB9XG4gICAgfSk7XG4gIH0sIGNhbGxiYWNrKTtcbn1cblxuLyoqXG4gKiBSdW5zIGFsbCBvbkVudGVyIGhvb2tzIGluIHRoZSBnaXZlbiBhcnJheSBvZiByb3V0ZXMgaW4gb3JkZXJcbiAqIHdpdGggb25FbnRlcihuZXh0U3RhdGUsIHJlcGxhY2UsIGNhbGxiYWNrKSBhbmQgY2FsbHNcbiAqIGNhbGxiYWNrKGVycm9yLCByZWRpcmVjdEluZm8pIHdoZW4gZmluaXNoZWQuIFRoZSBmaXJzdCBob29rXG4gKiB0byB1c2UgcmVwbGFjZSBzaG9ydC1jaXJjdWl0cyB0aGUgbG9vcC5cbiAqXG4gKiBJZiBhIGhvb2sgbmVlZHMgdG8gcnVuIGFzeW5jaHJvbm91c2x5LCBpdCBtYXkgdXNlIHRoZSBjYWxsYmFja1xuICogZnVuY3Rpb24uIEhvd2V2ZXIsIGRvaW5nIHNvIHdpbGwgY2F1c2UgdGhlIHRyYW5zaXRpb24gdG8gcGF1c2UsXG4gKiB3aGljaCBjb3VsZCBsZWFkIHRvIGEgbm9uLXJlc3BvbnNpdmUgVUkgaWYgdGhlIGhvb2sgaXMgc2xvdy5cbiAqL1xuZnVuY3Rpb24gcnVuRW50ZXJIb29rcyhyb3V0ZXMsIG5leHRTdGF0ZSwgY2FsbGJhY2spIHtcbiAgdmFyIGhvb2tzID0gZ2V0RW50ZXJIb29rcyhyb3V0ZXMpO1xuICByZXR1cm4gcnVuVHJhbnNpdGlvbkhvb2tzKGhvb2tzLmxlbmd0aCwgZnVuY3Rpb24gKGluZGV4LCByZXBsYWNlLCBuZXh0KSB7XG4gICAgaG9va3NbaW5kZXhdKG5leHRTdGF0ZSwgcmVwbGFjZSwgbmV4dCk7XG4gIH0sIGNhbGxiYWNrKTtcbn1cblxuLyoqXG4gKiBSdW5zIGFsbCBvbkNoYW5nZSBob29rcyBpbiB0aGUgZ2l2ZW4gYXJyYXkgb2Ygcm91dGVzIGluIG9yZGVyXG4gKiB3aXRoIG9uQ2hhbmdlKHByZXZTdGF0ZSwgbmV4dFN0YXRlLCByZXBsYWNlLCBjYWxsYmFjaykgYW5kIGNhbGxzXG4gKiBjYWxsYmFjayhlcnJvciwgcmVkaXJlY3RJbmZvKSB3aGVuIGZpbmlzaGVkLiBUaGUgZmlyc3QgaG9va1xuICogdG8gdXNlIHJlcGxhY2Ugc2hvcnQtY2lyY3VpdHMgdGhlIGxvb3AuXG4gKlxuICogSWYgYSBob29rIG5lZWRzIHRvIHJ1biBhc3luY2hyb25vdXNseSwgaXQgbWF5IHVzZSB0aGUgY2FsbGJhY2tcbiAqIGZ1bmN0aW9uLiBIb3dldmVyLCBkb2luZyBzbyB3aWxsIGNhdXNlIHRoZSB0cmFuc2l0aW9uIHRvIHBhdXNlLFxuICogd2hpY2ggY291bGQgbGVhZCB0byBhIG5vbi1yZXNwb25zaXZlIFVJIGlmIHRoZSBob29rIGlzIHNsb3cuXG4gKi9cbmZ1bmN0aW9uIHJ1bkNoYW5nZUhvb2tzKHJvdXRlcywgc3RhdGUsIG5leHRTdGF0ZSwgY2FsbGJhY2spIHtcbiAgdmFyIGhvb2tzID0gZ2V0Q2hhbmdlSG9va3Mocm91dGVzKTtcbiAgcmV0dXJuIHJ1blRyYW5zaXRpb25Ib29rcyhob29rcy5sZW5ndGgsIGZ1bmN0aW9uIChpbmRleCwgcmVwbGFjZSwgbmV4dCkge1xuICAgIGhvb2tzW2luZGV4XShzdGF0ZSwgbmV4dFN0YXRlLCByZXBsYWNlLCBuZXh0KTtcbiAgfSwgY2FsbGJhY2spO1xufVxuXG4vKipcbiAqIFJ1bnMgYWxsIG9uTGVhdmUgaG9va3MgaW4gdGhlIGdpdmVuIGFycmF5IG9mIHJvdXRlcyBpbiBvcmRlci5cbiAqL1xuZnVuY3Rpb24gcnVuTGVhdmVIb29rcyhyb3V0ZXMsIHByZXZTdGF0ZSkge1xuICBmb3IgKHZhciBpID0gMCwgbGVuID0gcm91dGVzLmxlbmd0aDsgaSA8IGxlbjsgKytpKSB7XG4gICAgaWYgKHJvdXRlc1tpXS5vbkxlYXZlKSByb3V0ZXNbaV0ub25MZWF2ZS5jYWxsKHJvdXRlc1tpXSwgcHJldlN0YXRlKTtcbiAgfVxufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX1JvdXRlckNvbnRleHQgPSByZXF1aXJlKCcuL1JvdXRlckNvbnRleHQnKTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlckNvbnRleHQpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5leHBvcnRzLmRlZmF1bHQgPSBmdW5jdGlvbiAoKSB7XG4gIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBtaWRkbGV3YXJlcyA9IEFycmF5KF9sZW4pLCBfa2V5ID0gMDsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgIG1pZGRsZXdhcmVzW19rZXldID0gYXJndW1lbnRzW19rZXldO1xuICB9XG5cbiAgdmFyIHdpdGhDb250ZXh0ID0gbWlkZGxld2FyZXMubWFwKGZ1bmN0aW9uIChtKSB7XG4gICAgcmV0dXJuIG0ucmVuZGVyUm91dGVyQ29udGV4dDtcbiAgfSkuZmlsdGVyKGZ1bmN0aW9uIChmKSB7XG4gICAgcmV0dXJuIGY7XG4gIH0pO1xuICB2YXIgd2l0aENvbXBvbmVudCA9IG1pZGRsZXdhcmVzLm1hcChmdW5jdGlvbiAobSkge1xuICAgIHJldHVybiBtLnJlbmRlclJvdXRlQ29tcG9uZW50O1xuICB9KS5maWx0ZXIoZnVuY3Rpb24gKGYpIHtcbiAgICByZXR1cm4gZjtcbiAgfSk7XG4gIHZhciBtYWtlQ3JlYXRlRWxlbWVudCA9IGZ1bmN0aW9uIG1ha2VDcmVhdGVFbGVtZW50KCkge1xuICAgIHZhciBiYXNlQ3JlYXRlRWxlbWVudCA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IF9yZWFjdC5jcmVhdGVFbGVtZW50IDogYXJndW1lbnRzWzBdO1xuICAgIHJldHVybiBmdW5jdGlvbiAoQ29tcG9uZW50LCBwcm9wcykge1xuICAgICAgcmV0dXJuIHdpdGhDb21wb25lbnQucmVkdWNlUmlnaHQoZnVuY3Rpb24gKHByZXZpb3VzLCByZW5kZXJSb3V0ZUNvbXBvbmVudCkge1xuICAgICAgICByZXR1cm4gcmVuZGVyUm91dGVDb21wb25lbnQocHJldmlvdXMsIHByb3BzKTtcbiAgICAgIH0sIGJhc2VDcmVhdGVFbGVtZW50KENvbXBvbmVudCwgcHJvcHMpKTtcbiAgICB9O1xuICB9O1xuXG4gIHJldHVybiBmdW5jdGlvbiAocmVuZGVyUHJvcHMpIHtcbiAgICByZXR1cm4gd2l0aENvbnRleHQucmVkdWNlUmlnaHQoZnVuY3Rpb24gKHByZXZpb3VzLCByZW5kZXJSb3V0ZXJDb250ZXh0KSB7XG4gICAgICByZXR1cm4gcmVuZGVyUm91dGVyQ29udGV4dChwcmV2aW91cywgcmVuZGVyUHJvcHMpO1xuICAgIH0sIF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50KF9Sb3V0ZXJDb250ZXh0Mi5kZWZhdWx0LCBfZXh0ZW5kcyh7fSwgcmVuZGVyUHJvcHMsIHtcbiAgICAgIGNyZWF0ZUVsZW1lbnQ6IG1ha2VDcmVhdGVFbGVtZW50KHJlbmRlclByb3BzLmNyZWF0ZUVsZW1lbnQpXG4gICAgfSkpKTtcbiAgfTtcbn07XG5cbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9jcmVhdGVCcm93c2VySGlzdG9yeSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL2NyZWF0ZUJyb3dzZXJIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlQnJvd3Nlckhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlQnJvd3Nlckhpc3RvcnkpO1xuXG52YXIgX2NyZWF0ZVJvdXRlckhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZVJvdXRlckhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVSb3V0ZXJIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVJvdXRlckhpc3RvcnkpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5leHBvcnRzLmRlZmF1bHQgPSAoMCwgX2NyZWF0ZVJvdXRlckhpc3RvcnkyLmRlZmF1bHQpKF9jcmVhdGVCcm93c2VySGlzdG9yeTIuZGVmYXVsdCk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfUGF0dGVyblV0aWxzID0gcmVxdWlyZSgnLi9QYXR0ZXJuVXRpbHMnKTtcblxuZnVuY3Rpb24gcm91dGVQYXJhbXNDaGFuZ2VkKHJvdXRlLCBwcmV2U3RhdGUsIG5leHRTdGF0ZSkge1xuICBpZiAoIXJvdXRlLnBhdGgpIHJldHVybiBmYWxzZTtcblxuICB2YXIgcGFyYW1OYW1lcyA9ICgwLCBfUGF0dGVyblV0aWxzLmdldFBhcmFtTmFtZXMpKHJvdXRlLnBhdGgpO1xuXG4gIHJldHVybiBwYXJhbU5hbWVzLnNvbWUoZnVuY3Rpb24gKHBhcmFtTmFtZSkge1xuICAgIHJldHVybiBwcmV2U3RhdGUucGFyYW1zW3BhcmFtTmFtZV0gIT09IG5leHRTdGF0ZS5wYXJhbXNbcGFyYW1OYW1lXTtcbiAgfSk7XG59XG5cbi8qKlxuICogUmV0dXJucyBhbiBvYmplY3Qgb2YgeyBsZWF2ZVJvdXRlcywgY2hhbmdlUm91dGVzLCBlbnRlclJvdXRlcyB9IGRldGVybWluZWQgYnlcbiAqIHRoZSBjaGFuZ2UgZnJvbSBwcmV2U3RhdGUgdG8gbmV4dFN0YXRlLiBXZSBsZWF2ZSByb3V0ZXMgaWYgZWl0aGVyXG4gKiAxKSB0aGV5IGFyZSBub3QgaW4gdGhlIG5leHQgc3RhdGUgb3IgMikgdGhleSBhcmUgaW4gdGhlIG5leHQgc3RhdGVcbiAqIGJ1dCB0aGVpciBwYXJhbXMgaGF2ZSBjaGFuZ2VkIChpLmUuIC91c2Vycy8xMjMgPT4gL3VzZXJzLzQ1NikuXG4gKlxuICogbGVhdmVSb3V0ZXMgYXJlIG9yZGVyZWQgc3RhcnRpbmcgYXQgdGhlIGxlYWYgcm91dGUgb2YgdGhlIHRyZWVcbiAqIHdlJ3JlIGxlYXZpbmcgdXAgdG8gdGhlIGNvbW1vbiBwYXJlbnQgcm91dGUuIGVudGVyUm91dGVzIGFyZSBvcmRlcmVkXG4gKiBmcm9tIHRoZSB0b3Agb2YgdGhlIHRyZWUgd2UncmUgZW50ZXJpbmcgZG93biB0byB0aGUgbGVhZiByb3V0ZS5cbiAqXG4gKiBjaGFuZ2VSb3V0ZXMgYXJlIGFueSByb3V0ZXMgdGhhdCBkaWRuJ3QgbGVhdmUgb3IgZW50ZXIgZHVyaW5nXG4gKiB0aGUgdHJhbnNpdGlvbi5cbiAqL1xuZnVuY3Rpb24gY29tcHV0ZUNoYW5nZWRSb3V0ZXMocHJldlN0YXRlLCBuZXh0U3RhdGUpIHtcbiAgdmFyIHByZXZSb3V0ZXMgPSBwcmV2U3RhdGUgJiYgcHJldlN0YXRlLnJvdXRlcztcbiAgdmFyIG5leHRSb3V0ZXMgPSBuZXh0U3RhdGUucm91dGVzO1xuXG4gIHZhciBsZWF2ZVJvdXRlcyA9IHZvaWQgMCxcbiAgICAgIGNoYW5nZVJvdXRlcyA9IHZvaWQgMCxcbiAgICAgIGVudGVyUm91dGVzID0gdm9pZCAwO1xuICBpZiAocHJldlJvdXRlcykge1xuICAgIChmdW5jdGlvbiAoKSB7XG4gICAgICB2YXIgcGFyZW50SXNMZWF2aW5nID0gZmFsc2U7XG4gICAgICBsZWF2ZVJvdXRlcyA9IHByZXZSb3V0ZXMuZmlsdGVyKGZ1bmN0aW9uIChyb3V0ZSkge1xuICAgICAgICBpZiAocGFyZW50SXNMZWF2aW5nKSB7XG4gICAgICAgICAgcmV0dXJuIHRydWU7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgdmFyIGlzTGVhdmluZyA9IG5leHRSb3V0ZXMuaW5kZXhPZihyb3V0ZSkgPT09IC0xIHx8IHJvdXRlUGFyYW1zQ2hhbmdlZChyb3V0ZSwgcHJldlN0YXRlLCBuZXh0U3RhdGUpO1xuICAgICAgICAgIGlmIChpc0xlYXZpbmcpIHBhcmVudElzTGVhdmluZyA9IHRydWU7XG4gICAgICAgICAgcmV0dXJuIGlzTGVhdmluZztcbiAgICAgICAgfVxuICAgICAgfSk7XG5cbiAgICAgIC8vIG9uTGVhdmUgaG9va3Mgc3RhcnQgYXQgdGhlIGxlYWYgcm91dGUuXG4gICAgICBsZWF2ZVJvdXRlcy5yZXZlcnNlKCk7XG5cbiAgICAgIGVudGVyUm91dGVzID0gW107XG4gICAgICBjaGFuZ2VSb3V0ZXMgPSBbXTtcblxuICAgICAgbmV4dFJvdXRlcy5mb3JFYWNoKGZ1bmN0aW9uIChyb3V0ZSkge1xuICAgICAgICB2YXIgaXNOZXcgPSBwcmV2Um91dGVzLmluZGV4T2Yocm91dGUpID09PSAtMTtcbiAgICAgICAgdmFyIHBhcmFtc0NoYW5nZWQgPSBsZWF2ZVJvdXRlcy5pbmRleE9mKHJvdXRlKSAhPT0gLTE7XG5cbiAgICAgICAgaWYgKGlzTmV3IHx8IHBhcmFtc0NoYW5nZWQpIGVudGVyUm91dGVzLnB1c2gocm91dGUpO2Vsc2UgY2hhbmdlUm91dGVzLnB1c2gocm91dGUpO1xuICAgICAgfSk7XG4gICAgfSkoKTtcbiAgfSBlbHNlIHtcbiAgICBsZWF2ZVJvdXRlcyA9IFtdO1xuICAgIGNoYW5nZVJvdXRlcyA9IFtdO1xuICAgIGVudGVyUm91dGVzID0gbmV4dFJvdXRlcztcbiAgfVxuXG4gIHJldHVybiB7XG4gICAgbGVhdmVSb3V0ZXM6IGxlYXZlUm91dGVzLFxuICAgIGNoYW5nZVJvdXRlczogY2hhbmdlUm91dGVzLFxuICAgIGVudGVyUm91dGVzOiBlbnRlclJvdXRlc1xuICB9O1xufVxuXG5leHBvcnRzLmRlZmF1bHQgPSBjb21wdXRlQ2hhbmdlZFJvdXRlcztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuZGVmYXVsdCA9IGNyZWF0ZU1lbW9yeUhpc3Rvcnk7XG5cbnZhciBfdXNlUXVlcmllcyA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL3VzZVF1ZXJpZXMnKTtcblxudmFyIF91c2VRdWVyaWVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZVF1ZXJpZXMpO1xuXG52YXIgX3VzZUJhc2VuYW1lID0gcmVxdWlyZSgnaGlzdG9yeS9saWIvdXNlQmFzZW5hbWUnKTtcblxudmFyIF91c2VCYXNlbmFtZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF91c2VCYXNlbmFtZSk7XG5cbnZhciBfY3JlYXRlTWVtb3J5SGlzdG9yeSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL2NyZWF0ZU1lbW9yeUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVNZW1vcnlIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZU1lbW9yeUhpc3RvcnkpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBjcmVhdGVNZW1vcnlIaXN0b3J5KG9wdGlvbnMpIHtcbiAgLy8gc2lnbmF0dXJlcyBhbmQgdHlwZSBjaGVja2luZyBkaWZmZXIgYmV0d2VlbiBgdXNlUm91dGVzYCBhbmRcbiAgLy8gYGNyZWF0ZU1lbW9yeUhpc3RvcnlgLCBoYXZlIHRvIGNyZWF0ZSBgbWVtb3J5SGlzdG9yeWAgZmlyc3QgYmVjYXVzZVxuICAvLyBgdXNlUXVlcmllc2AgZG9lc24ndCB1bmRlcnN0YW5kIHRoZSBzaWduYXR1cmVcbiAgdmFyIG1lbW9yeUhpc3RvcnkgPSAoMCwgX2NyZWF0ZU1lbW9yeUhpc3RvcnkyLmRlZmF1bHQpKG9wdGlvbnMpO1xuICB2YXIgY3JlYXRlSGlzdG9yeSA9IGZ1bmN0aW9uIGNyZWF0ZUhpc3RvcnkoKSB7XG4gICAgcmV0dXJuIG1lbW9yeUhpc3Rvcnk7XG4gIH07XG4gIHZhciBoaXN0b3J5ID0gKDAsIF91c2VRdWVyaWVzMi5kZWZhdWx0KSgoMCwgX3VzZUJhc2VuYW1lMi5kZWZhdWx0KShjcmVhdGVIaXN0b3J5KSkob3B0aW9ucyk7XG4gIGhpc3RvcnkuX192Ml9jb21wYXRpYmxlX18gPSB0cnVlO1xuICByZXR1cm4gaGlzdG9yeTtcbn1cbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gZnVuY3Rpb24gKGNyZWF0ZUhpc3RvcnkpIHtcbiAgdmFyIGhpc3RvcnkgPSB2b2lkIDA7XG4gIGlmIChjYW5Vc2VET00pIGhpc3RvcnkgPSAoMCwgX3VzZVJvdXRlckhpc3RvcnkyLmRlZmF1bHQpKGNyZWF0ZUhpc3RvcnkpKCk7XG4gIHJldHVybiBoaXN0b3J5O1xufTtcblxudmFyIF91c2VSb3V0ZXJIaXN0b3J5ID0gcmVxdWlyZSgnLi91c2VSb3V0ZXJIaXN0b3J5Jyk7XG5cbnZhciBfdXNlUm91dGVySGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF91c2VSb3V0ZXJIaXN0b3J5KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIGNhblVzZURPTSA9ICEhKHR5cGVvZiB3aW5kb3cgIT09ICd1bmRlZmluZWQnICYmIHdpbmRvdy5kb2N1bWVudCAmJiB3aW5kb3cuZG9jdW1lbnQuY3JlYXRlRWxlbWVudCk7XG5cbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXI7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbnZhciBfQWN0aW9ucyA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL0FjdGlvbnMnKTtcblxudmFyIF9jb21wdXRlQ2hhbmdlZFJvdXRlczIgPSByZXF1aXJlKCcuL2NvbXB1dGVDaGFuZ2VkUm91dGVzJyk7XG5cbnZhciBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29tcHV0ZUNoYW5nZWRSb3V0ZXMyKTtcblxudmFyIF9UcmFuc2l0aW9uVXRpbHMgPSByZXF1aXJlKCcuL1RyYW5zaXRpb25VdGlscycpO1xuXG52YXIgX2lzQWN0aXZlMiA9IHJlcXVpcmUoJy4vaXNBY3RpdmUnKTtcblxudmFyIF9pc0FjdGl2ZTMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pc0FjdGl2ZTIpO1xuXG52YXIgX2dldENvbXBvbmVudHMgPSByZXF1aXJlKCcuL2dldENvbXBvbmVudHMnKTtcblxudmFyIF9nZXRDb21wb25lbnRzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2dldENvbXBvbmVudHMpO1xuXG52YXIgX21hdGNoUm91dGVzID0gcmVxdWlyZSgnLi9tYXRjaFJvdXRlcycpO1xuXG52YXIgX21hdGNoUm91dGVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX21hdGNoUm91dGVzKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gaGFzQW55UHJvcGVydGllcyhvYmplY3QpIHtcbiAgZm9yICh2YXIgcCBpbiBvYmplY3QpIHtcbiAgICBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iamVjdCwgcCkpIHJldHVybiB0cnVlO1xuICB9cmV0dXJuIGZhbHNlO1xufVxuXG5mdW5jdGlvbiBjcmVhdGVUcmFuc2l0aW9uTWFuYWdlcihoaXN0b3J5LCByb3V0ZXMpIHtcbiAgdmFyIHN0YXRlID0ge307XG5cbiAgLy8gU2lnbmF0dXJlIHNob3VsZCBiZSAobG9jYXRpb24sIGluZGV4T25seSksIGJ1dCBuZWVkcyB0byBzdXBwb3J0IChwYXRoLFxuICAvLyBxdWVyeSwgaW5kZXhPbmx5KVxuICBmdW5jdGlvbiBpc0FjdGl2ZShsb2NhdGlvbikge1xuICAgIHZhciBpbmRleE9ubHlPckRlcHJlY2F0ZWRRdWVyeSA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMSB8fCBhcmd1bWVudHNbMV0gPT09IHVuZGVmaW5lZCA/IGZhbHNlIDogYXJndW1lbnRzWzFdO1xuICAgIHZhciBkZXByZWNhdGVkSW5kZXhPbmx5ID0gYXJndW1lbnRzLmxlbmd0aCA8PSAyIHx8IGFyZ3VtZW50c1syXSA9PT0gdW5kZWZpbmVkID8gbnVsbCA6IGFyZ3VtZW50c1syXTtcblxuICAgIHZhciBpbmRleE9ubHkgPSB2b2lkIDA7XG4gICAgaWYgKGluZGV4T25seU9yRGVwcmVjYXRlZFF1ZXJ5ICYmIGluZGV4T25seU9yRGVwcmVjYXRlZFF1ZXJ5ICE9PSB0cnVlIHx8IGRlcHJlY2F0ZWRJbmRleE9ubHkgIT09IG51bGwpIHtcbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnYGlzQWN0aXZlKHBhdGhuYW1lLCBxdWVyeSwgaW5kZXhPbmx5KSBpcyBkZXByZWNhdGVkOyB1c2UgYGlzQWN0aXZlKGxvY2F0aW9uLCBpbmRleE9ubHkpYCB3aXRoIGEgbG9jYXRpb24gZGVzY3JpcHRvciBpbnN0ZWFkLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItaXNBY3RpdmVkZXByZWNhdGVkJykgOiB2b2lkIDA7XG4gICAgICBsb2NhdGlvbiA9IHsgcGF0aG5hbWU6IGxvY2F0aW9uLCBxdWVyeTogaW5kZXhPbmx5T3JEZXByZWNhdGVkUXVlcnkgfTtcbiAgICAgIGluZGV4T25seSA9IGRlcHJlY2F0ZWRJbmRleE9ubHkgfHwgZmFsc2U7XG4gICAgfSBlbHNlIHtcbiAgICAgIGxvY2F0aW9uID0gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbihsb2NhdGlvbik7XG4gICAgICBpbmRleE9ubHkgPSBpbmRleE9ubHlPckRlcHJlY2F0ZWRRdWVyeTtcbiAgICB9XG5cbiAgICByZXR1cm4gKDAsIF9pc0FjdGl2ZTMuZGVmYXVsdCkobG9jYXRpb24sIGluZGV4T25seSwgc3RhdGUubG9jYXRpb24sIHN0YXRlLnJvdXRlcywgc3RhdGUucGFyYW1zKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNyZWF0ZUxvY2F0aW9uRnJvbVJlZGlyZWN0SW5mbyhsb2NhdGlvbikge1xuICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uKGxvY2F0aW9uLCBfQWN0aW9ucy5SRVBMQUNFKTtcbiAgfVxuXG4gIHZhciBwYXJ0aWFsTmV4dFN0YXRlID0gdm9pZCAwO1xuXG4gIGZ1bmN0aW9uIG1hdGNoKGxvY2F0aW9uLCBjYWxsYmFjaykge1xuICAgIGlmIChwYXJ0aWFsTmV4dFN0YXRlICYmIHBhcnRpYWxOZXh0U3RhdGUubG9jYXRpb24gPT09IGxvY2F0aW9uKSB7XG4gICAgICAvLyBDb250aW51ZSBmcm9tIHdoZXJlIHdlIGxlZnQgb2ZmLlxuICAgICAgZmluaXNoTWF0Y2gocGFydGlhbE5leHRTdGF0ZSwgY2FsbGJhY2spO1xuICAgIH0gZWxzZSB7XG4gICAgICAoMCwgX21hdGNoUm91dGVzMi5kZWZhdWx0KShyb3V0ZXMsIGxvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIG5leHRTdGF0ZSkge1xuICAgICAgICBpZiAoZXJyb3IpIHtcbiAgICAgICAgICBjYWxsYmFjayhlcnJvcik7XG4gICAgICAgIH0gZWxzZSBpZiAobmV4dFN0YXRlKSB7XG4gICAgICAgICAgZmluaXNoTWF0Y2goX2V4dGVuZHMoe30sIG5leHRTdGF0ZSwgeyBsb2NhdGlvbjogbG9jYXRpb24gfSksIGNhbGxiYWNrKTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBjYWxsYmFjaygpO1xuICAgICAgICB9XG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICBmdW5jdGlvbiBmaW5pc2hNYXRjaChuZXh0U3RhdGUsIGNhbGxiYWNrKSB7XG4gICAgdmFyIF9jb21wdXRlQ2hhbmdlZFJvdXRlcyA9ICgwLCBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMzLmRlZmF1bHQpKHN0YXRlLCBuZXh0U3RhdGUpO1xuXG4gICAgdmFyIGxlYXZlUm91dGVzID0gX2NvbXB1dGVDaGFuZ2VkUm91dGVzLmxlYXZlUm91dGVzO1xuICAgIHZhciBjaGFuZ2VSb3V0ZXMgPSBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMuY2hhbmdlUm91dGVzO1xuICAgIHZhciBlbnRlclJvdXRlcyA9IF9jb21wdXRlQ2hhbmdlZFJvdXRlcy5lbnRlclJvdXRlcztcblxuXG4gICAgKDAsIF9UcmFuc2l0aW9uVXRpbHMucnVuTGVhdmVIb29rcykobGVhdmVSb3V0ZXMsIHN0YXRlKTtcblxuICAgIC8vIFRlYXIgZG93biBjb25maXJtYXRpb24gaG9va3MgZm9yIGxlZnQgcm91dGVzXG4gICAgbGVhdmVSb3V0ZXMuZmlsdGVyKGZ1bmN0aW9uIChyb3V0ZSkge1xuICAgICAgcmV0dXJuIGVudGVyUm91dGVzLmluZGV4T2Yocm91dGUpID09PSAtMTtcbiAgICB9KS5mb3JFYWNoKHJlbW92ZUxpc3RlbkJlZm9yZUhvb2tzRm9yUm91dGUpO1xuXG4gICAgLy8gY2hhbmdlIGFuZCBlbnRlciBob29rcyBhcmUgcnVuIGluIHNlcmllc1xuICAgICgwLCBfVHJhbnNpdGlvblV0aWxzLnJ1bkNoYW5nZUhvb2tzKShjaGFuZ2VSb3V0ZXMsIHN0YXRlLCBuZXh0U3RhdGUsIGZ1bmN0aW9uIChlcnJvciwgcmVkaXJlY3RJbmZvKSB7XG4gICAgICBpZiAoZXJyb3IgfHwgcmVkaXJlY3RJbmZvKSByZXR1cm4gaGFuZGxlRXJyb3JPclJlZGlyZWN0KGVycm9yLCByZWRpcmVjdEluZm8pO1xuXG4gICAgICAoMCwgX1RyYW5zaXRpb25VdGlscy5ydW5FbnRlckhvb2tzKShlbnRlclJvdXRlcywgbmV4dFN0YXRlLCBmaW5pc2hFbnRlckhvb2tzKTtcbiAgICB9KTtcblxuICAgIGZ1bmN0aW9uIGZpbmlzaEVudGVySG9va3MoZXJyb3IsIHJlZGlyZWN0SW5mbykge1xuICAgICAgaWYgKGVycm9yIHx8IHJlZGlyZWN0SW5mbykgcmV0dXJuIGhhbmRsZUVycm9yT3JSZWRpcmVjdChlcnJvciwgcmVkaXJlY3RJbmZvKTtcblxuICAgICAgLy8gVE9ETzogRmV0Y2ggY29tcG9uZW50cyBhZnRlciBzdGF0ZSBpcyB1cGRhdGVkLlxuICAgICAgKDAsIF9nZXRDb21wb25lbnRzMi5kZWZhdWx0KShuZXh0U3RhdGUsIGZ1bmN0aW9uIChlcnJvciwgY29tcG9uZW50cykge1xuICAgICAgICBpZiAoZXJyb3IpIHtcbiAgICAgICAgICBjYWxsYmFjayhlcnJvcik7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgLy8gVE9ETzogTWFrZSBtYXRjaCBhIHB1cmUgZnVuY3Rpb24gYW5kIGhhdmUgc29tZSBvdGhlciBBUElcbiAgICAgICAgICAvLyBmb3IgXCJtYXRjaCBhbmQgdXBkYXRlIHN0YXRlXCIuXG4gICAgICAgICAgY2FsbGJhY2sobnVsbCwgbnVsbCwgc3RhdGUgPSBfZXh0ZW5kcyh7fSwgbmV4dFN0YXRlLCB7IGNvbXBvbmVudHM6IGNvbXBvbmVudHMgfSkpO1xuICAgICAgICB9XG4gICAgICB9KTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBoYW5kbGVFcnJvck9yUmVkaXJlY3QoZXJyb3IsIHJlZGlyZWN0SW5mbykge1xuICAgICAgaWYgKGVycm9yKSBjYWxsYmFjayhlcnJvcik7ZWxzZSBjYWxsYmFjayhudWxsLCBjcmVhdGVMb2NhdGlvbkZyb21SZWRpcmVjdEluZm8ocmVkaXJlY3RJbmZvKSk7XG4gICAgfVxuICB9XG5cbiAgdmFyIFJvdXRlR3VpZCA9IDE7XG5cbiAgZnVuY3Rpb24gZ2V0Um91dGVJRChyb3V0ZSkge1xuICAgIHZhciBjcmVhdGUgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDEgfHwgYXJndW1lbnRzWzFdID09PSB1bmRlZmluZWQgPyB0cnVlIDogYXJndW1lbnRzWzFdO1xuXG4gICAgcmV0dXJuIHJvdXRlLl9faWRfXyB8fCBjcmVhdGUgJiYgKHJvdXRlLl9faWRfXyA9IFJvdXRlR3VpZCsrKTtcbiAgfVxuXG4gIHZhciBSb3V0ZUhvb2tzID0gT2JqZWN0LmNyZWF0ZShudWxsKTtcblxuICBmdW5jdGlvbiBnZXRSb3V0ZUhvb2tzRm9yUm91dGVzKHJvdXRlcykge1xuICAgIHJldHVybiByb3V0ZXMucmVkdWNlKGZ1bmN0aW9uIChob29rcywgcm91dGUpIHtcbiAgICAgIGhvb2tzLnB1c2guYXBwbHkoaG9va3MsIFJvdXRlSG9va3NbZ2V0Um91dGVJRChyb3V0ZSldKTtcbiAgICAgIHJldHVybiBob29rcztcbiAgICB9LCBbXSk7XG4gIH1cblxuICBmdW5jdGlvbiB0cmFuc2l0aW9uSG9vayhsb2NhdGlvbiwgY2FsbGJhY2spIHtcbiAgICAoMCwgX21hdGNoUm91dGVzMi5kZWZhdWx0KShyb3V0ZXMsIGxvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIG5leHRTdGF0ZSkge1xuICAgICAgaWYgKG5leHRTdGF0ZSA9PSBudWxsKSB7XG4gICAgICAgIC8vIFRPRE86IFdlIGRpZG4ndCBhY3R1YWxseSBtYXRjaCBhbnl0aGluZywgYnV0IGhhbmdcbiAgICAgICAgLy8gb250byBlcnJvci9uZXh0U3RhdGUgc28gd2UgZG9uJ3QgaGF2ZSB0byBtYXRjaFJvdXRlc1xuICAgICAgICAvLyBhZ2FpbiBpbiB0aGUgbGlzdGVuIGNhbGxiYWNrLlxuICAgICAgICBjYWxsYmFjaygpO1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG5cbiAgICAgIC8vIENhY2hlIHNvbWUgc3RhdGUgaGVyZSBzbyB3ZSBkb24ndCBoYXZlIHRvXG4gICAgICAvLyBtYXRjaFJvdXRlcygpIGFnYWluIGluIHRoZSBsaXN0ZW4gY2FsbGJhY2suXG4gICAgICBwYXJ0aWFsTmV4dFN0YXRlID0gX2V4dGVuZHMoe30sIG5leHRTdGF0ZSwgeyBsb2NhdGlvbjogbG9jYXRpb24gfSk7XG5cbiAgICAgIHZhciBob29rcyA9IGdldFJvdXRlSG9va3NGb3JSb3V0ZXMoKDAsIF9jb21wdXRlQ2hhbmdlZFJvdXRlczMuZGVmYXVsdCkoc3RhdGUsIHBhcnRpYWxOZXh0U3RhdGUpLmxlYXZlUm91dGVzKTtcblxuICAgICAgdmFyIHJlc3VsdCA9IHZvaWQgMDtcbiAgICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBob29rcy5sZW5ndGg7IHJlc3VsdCA9PSBudWxsICYmIGkgPCBsZW47ICsraSkge1xuICAgICAgICAvLyBQYXNzaW5nIHRoZSBsb2NhdGlvbiBhcmcgaGVyZSBpbmRpY2F0ZXMgdG9cbiAgICAgICAgLy8gdGhlIHVzZXIgdGhhdCB0aGlzIGlzIGEgdHJhbnNpdGlvbiBob29rLlxuICAgICAgICByZXN1bHQgPSBob29rc1tpXShsb2NhdGlvbik7XG4gICAgICB9XG5cbiAgICAgIGNhbGxiYWNrKHJlc3VsdCk7XG4gICAgfSk7XG4gIH1cblxuICAvKiBpc3RhbmJ1bCBpZ25vcmUgbmV4dDogdW50ZXN0YWJsZSB3aXRoIEthcm1hICovXG4gIGZ1bmN0aW9uIGJlZm9yZVVubG9hZEhvb2soKSB7XG4gICAgLy8gU3luY2hyb25vdXNseSBjaGVjayB0byBzZWUgaWYgYW55IHJvdXRlIGhvb2tzIHdhbnRcbiAgICAvLyB0byBwcmV2ZW50IHRoZSBjdXJyZW50IHdpbmRvdy90YWIgZnJvbSBjbG9zaW5nLlxuICAgIGlmIChzdGF0ZS5yb3V0ZXMpIHtcbiAgICAgIHZhciBob29rcyA9IGdldFJvdXRlSG9va3NGb3JSb3V0ZXMoc3RhdGUucm91dGVzKTtcblxuICAgICAgdmFyIG1lc3NhZ2UgPSB2b2lkIDA7XG4gICAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gaG9va3MubGVuZ3RoOyB0eXBlb2YgbWVzc2FnZSAhPT0gJ3N0cmluZycgJiYgaSA8IGxlbjsgKytpKSB7XG4gICAgICAgIC8vIFBhc3Npbmcgbm8gYXJncyBpbmRpY2F0ZXMgdG8gdGhlIHVzZXIgdGhhdCB0aGlzIGlzIGFcbiAgICAgICAgLy8gYmVmb3JldW5sb2FkIGhvb2suIFdlIGRvbid0IGtub3cgdGhlIG5leHQgbG9jYXRpb24uXG4gICAgICAgIG1lc3NhZ2UgPSBob29rc1tpXSgpO1xuICAgICAgfVxuXG4gICAgICByZXR1cm4gbWVzc2FnZTtcbiAgICB9XG4gIH1cblxuICB2YXIgdW5saXN0ZW5CZWZvcmUgPSB2b2lkIDAsXG4gICAgICB1bmxpc3RlbkJlZm9yZVVubG9hZCA9IHZvaWQgMDtcblxuICBmdW5jdGlvbiByZW1vdmVMaXN0ZW5CZWZvcmVIb29rc0ZvclJvdXRlKHJvdXRlKSB7XG4gICAgdmFyIHJvdXRlSUQgPSBnZXRSb3V0ZUlEKHJvdXRlLCBmYWxzZSk7XG4gICAgaWYgKCFyb3V0ZUlEKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgZGVsZXRlIFJvdXRlSG9va3Nbcm91dGVJRF07XG5cbiAgICBpZiAoIWhhc0FueVByb3BlcnRpZXMoUm91dGVIb29rcykpIHtcbiAgICAgIC8vIHRlYXJkb3duIHRyYW5zaXRpb24gJiBiZWZvcmV1bmxvYWQgaG9va3NcbiAgICAgIGlmICh1bmxpc3RlbkJlZm9yZSkge1xuICAgICAgICB1bmxpc3RlbkJlZm9yZSgpO1xuICAgICAgICB1bmxpc3RlbkJlZm9yZSA9IG51bGw7XG4gICAgICB9XG5cbiAgICAgIGlmICh1bmxpc3RlbkJlZm9yZVVubG9hZCkge1xuICAgICAgICB1bmxpc3RlbkJlZm9yZVVubG9hZCgpO1xuICAgICAgICB1bmxpc3RlbkJlZm9yZVVubG9hZCA9IG51bGw7XG4gICAgICB9XG4gICAgfVxuICB9XG5cbiAgLyoqXG4gICAqIFJlZ2lzdGVycyB0aGUgZ2l2ZW4gaG9vayBmdW5jdGlvbiB0byBydW4gYmVmb3JlIGxlYXZpbmcgdGhlIGdpdmVuIHJvdXRlLlxuICAgKlxuICAgKiBEdXJpbmcgYSBub3JtYWwgdHJhbnNpdGlvbiwgdGhlIGhvb2sgZnVuY3Rpb24gcmVjZWl2ZXMgdGhlIG5leHQgbG9jYXRpb25cbiAgICogYXMgaXRzIG9ubHkgYXJndW1lbnQgYW5kIGNhbiByZXR1cm4gZWl0aGVyIGEgcHJvbXB0IG1lc3NhZ2UgKHN0cmluZykgdG8gc2hvdyB0aGUgdXNlcixcbiAgICogdG8gbWFrZSBzdXJlIHRoZXkgd2FudCB0byBsZWF2ZSB0aGUgcGFnZTsgb3IgYGZhbHNlYCwgdG8gcHJldmVudCB0aGUgdHJhbnNpdGlvbi5cbiAgICogQW55IG90aGVyIHJldHVybiB2YWx1ZSB3aWxsIGhhdmUgbm8gZWZmZWN0LlxuICAgKlxuICAgKiBEdXJpbmcgdGhlIGJlZm9yZXVubG9hZCBldmVudCAoaW4gYnJvd3NlcnMpIHRoZSBob29rIHJlY2VpdmVzIG5vIGFyZ3VtZW50cy5cbiAgICogSW4gdGhpcyBjYXNlIGl0IG11c3QgcmV0dXJuIGEgcHJvbXB0IG1lc3NhZ2UgdG8gcHJldmVudCB0aGUgdHJhbnNpdGlvbi5cbiAgICpcbiAgICogUmV0dXJucyBhIGZ1bmN0aW9uIHRoYXQgbWF5IGJlIHVzZWQgdG8gdW5iaW5kIHRoZSBsaXN0ZW5lci5cbiAgICovXG4gIGZ1bmN0aW9uIGxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZShyb3V0ZSwgaG9vaykge1xuICAgIC8vIFRPRE86IFdhcm4gaWYgdGhleSByZWdpc3RlciBmb3IgYSByb3V0ZSB0aGF0IGlzbid0IGN1cnJlbnRseVxuICAgIC8vIGFjdGl2ZS4gVGhleSdyZSBwcm9iYWJseSBkb2luZyBzb21ldGhpbmcgd3JvbmcsIGxpa2UgcmUtY3JlYXRpbmdcbiAgICAvLyByb3V0ZSBvYmplY3RzIG9uIGV2ZXJ5IGxvY2F0aW9uIGNoYW5nZS5cbiAgICB2YXIgcm91dGVJRCA9IGdldFJvdXRlSUQocm91dGUpO1xuICAgIHZhciBob29rcyA9IFJvdXRlSG9va3Nbcm91dGVJRF07XG5cbiAgICBpZiAoIWhvb2tzKSB7XG4gICAgICB2YXIgdGhlcmVXZXJlTm9Sb3V0ZUhvb2tzID0gIWhhc0FueVByb3BlcnRpZXMoUm91dGVIb29rcyk7XG5cbiAgICAgIFJvdXRlSG9va3Nbcm91dGVJRF0gPSBbaG9va107XG5cbiAgICAgIGlmICh0aGVyZVdlcmVOb1JvdXRlSG9va3MpIHtcbiAgICAgICAgLy8gc2V0dXAgdHJhbnNpdGlvbiAmIGJlZm9yZXVubG9hZCBob29rc1xuICAgICAgICB1bmxpc3RlbkJlZm9yZSA9IGhpc3RvcnkubGlzdGVuQmVmb3JlKHRyYW5zaXRpb25Ib29rKTtcblxuICAgICAgICBpZiAoaGlzdG9yeS5saXN0ZW5CZWZvcmVVbmxvYWQpIHVubGlzdGVuQmVmb3JlVW5sb2FkID0gaGlzdG9yeS5saXN0ZW5CZWZvcmVVbmxvYWQoYmVmb3JlVW5sb2FkSG9vayk7XG4gICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgIGlmIChob29rcy5pbmRleE9mKGhvb2spID09PSAtMSkge1xuICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2FkZGluZyBtdWx0aXBsZSBsZWF2ZSBob29rcyBmb3IgdGhlIHNhbWUgcm91dGUgaXMgZGVwcmVjYXRlZDsgbWFuYWdlIG11bHRpcGxlIGNvbmZpcm1hdGlvbnMgaW4geW91ciBvd24gY29kZSBpbnN0ZWFkJykgOiB2b2lkIDA7XG5cbiAgICAgICAgaG9va3MucHVzaChob29rKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgdmFyIGhvb2tzID0gUm91dGVIb29rc1tyb3V0ZUlEXTtcblxuICAgICAgaWYgKGhvb2tzKSB7XG4gICAgICAgIHZhciBuZXdIb29rcyA9IGhvb2tzLmZpbHRlcihmdW5jdGlvbiAoaXRlbSkge1xuICAgICAgICAgIHJldHVybiBpdGVtICE9PSBob29rO1xuICAgICAgICB9KTtcblxuICAgICAgICBpZiAobmV3SG9va3MubGVuZ3RoID09PSAwKSB7XG4gICAgICAgICAgcmVtb3ZlTGlzdGVuQmVmb3JlSG9va3NGb3JSb3V0ZShyb3V0ZSk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgUm91dGVIb29rc1tyb3V0ZUlEXSA9IG5ld0hvb2tzO1xuICAgICAgICB9XG4gICAgICB9XG4gICAgfTtcbiAgfVxuXG4gIC8qKlxuICAgKiBUaGlzIGlzIHRoZSBBUEkgZm9yIHN0YXRlZnVsIGVudmlyb25tZW50cy4gQXMgdGhlIGxvY2F0aW9uXG4gICAqIGNoYW5nZXMsIHdlIHVwZGF0ZSBzdGF0ZSBhbmQgY2FsbCB0aGUgbGlzdGVuZXIuIFdlIGNhbiBhbHNvXG4gICAqIGdyYWNlZnVsbHkgaGFuZGxlIGVycm9ycyBhbmQgcmVkaXJlY3RzLlxuICAgKi9cbiAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgLy8gVE9ETzogT25seSB1c2UgYSBzaW5nbGUgaGlzdG9yeSBsaXN0ZW5lci4gT3RoZXJ3aXNlIHdlJ2xsXG4gICAgLy8gZW5kIHVwIHdpdGggbXVsdGlwbGUgY29uY3VycmVudCBjYWxscyB0byBtYXRjaC5cbiAgICByZXR1cm4gaGlzdG9yeS5saXN0ZW4oZnVuY3Rpb24gKGxvY2F0aW9uKSB7XG4gICAgICBpZiAoc3RhdGUubG9jYXRpb24gPT09IGxvY2F0aW9uKSB7XG4gICAgICAgIGxpc3RlbmVyKG51bGwsIHN0YXRlKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIG1hdGNoKGxvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIHJlZGlyZWN0TG9jYXRpb24sIG5leHRTdGF0ZSkge1xuICAgICAgICAgIGlmIChlcnJvcikge1xuICAgICAgICAgICAgbGlzdGVuZXIoZXJyb3IpO1xuICAgICAgICAgIH0gZWxzZSBpZiAocmVkaXJlY3RMb2NhdGlvbikge1xuICAgICAgICAgICAgaGlzdG9yeS50cmFuc2l0aW9uVG8ocmVkaXJlY3RMb2NhdGlvbik7XG4gICAgICAgICAgfSBlbHNlIGlmIChuZXh0U3RhdGUpIHtcbiAgICAgICAgICAgIGxpc3RlbmVyKG51bGwsIG5leHRTdGF0ZSk7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnTG9jYXRpb24gXCIlc1wiIGRpZCBub3QgbWF0Y2ggYW55IHJvdXRlcycsIGxvY2F0aW9uLnBhdGhuYW1lICsgbG9jYXRpb24uc2VhcmNoICsgbG9jYXRpb24uaGFzaCkgOiB2b2lkIDA7XG4gICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgIH1cbiAgICB9KTtcbiAgfVxuXG4gIHJldHVybiB7XG4gICAgaXNBY3RpdmU6IGlzQWN0aXZlLFxuICAgIG1hdGNoOiBtYXRjaCxcbiAgICBsaXN0ZW5CZWZvcmVMZWF2aW5nUm91dGU6IGxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZSxcbiAgICBsaXN0ZW46IGxpc3RlblxuICB9O1xufVxuXG4vL2V4cG9ydCBkZWZhdWx0IHVzZVJvdXRlc1xuXG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmNhblVzZU1lbWJyYW5lID0gdW5kZWZpbmVkO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgY2FuVXNlTWVtYnJhbmUgPSBleHBvcnRzLmNhblVzZU1lbWJyYW5lID0gZmFsc2U7XG5cbi8vIE5vLW9wIGJ5IGRlZmF1bHQuXG52YXIgZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IGZ1bmN0aW9uIGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMob2JqZWN0KSB7XG4gIHJldHVybiBvYmplY3Q7XG59O1xuXG5pZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICB0cnkge1xuICAgIGlmIChPYmplY3QuZGVmaW5lUHJvcGVydHkoe30sICd4Jywge1xuICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgfVxuICAgIH0pLngpIHtcbiAgICAgIGV4cG9ydHMuY2FuVXNlTWVtYnJhbmUgPSBjYW5Vc2VNZW1icmFuZSA9IHRydWU7XG4gICAgfVxuICAgIC8qIGVzbGludC1kaXNhYmxlIG5vLWVtcHR5ICovXG4gIH0gY2F0Y2ggKGUpIHt9XG4gIC8qIGVzbGludC1lbmFibGUgbm8tZW1wdHkgKi9cblxuICBpZiAoY2FuVXNlTWVtYnJhbmUpIHtcbiAgICBkZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzID0gZnVuY3Rpb24gZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyhvYmplY3QsIG1lc3NhZ2UpIHtcbiAgICAgIC8vIFdyYXAgdGhlIGRlcHJlY2F0ZWQgb2JqZWN0IGluIGEgbWVtYnJhbmUgdG8gd2FybiBvbiBwcm9wZXJ0eSBhY2Nlc3MuXG4gICAgICB2YXIgbWVtYnJhbmUgPSB7fTtcblxuICAgICAgdmFyIF9sb29wID0gZnVuY3Rpb24gX2xvb3AocHJvcCkge1xuICAgICAgICBpZiAoIU9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmplY3QsIHByb3ApKSB7XG4gICAgICAgICAgcmV0dXJuICdjb250aW51ZSc7XG4gICAgICAgIH1cblxuICAgICAgICBpZiAodHlwZW9mIG9iamVjdFtwcm9wXSA9PT0gJ2Z1bmN0aW9uJykge1xuICAgICAgICAgIC8vIENhbid0IHVzZSBmYXQgYXJyb3cgaGVyZSBiZWNhdXNlIG9mIHVzZSBvZiBhcmd1bWVudHMgYmVsb3cuXG4gICAgICAgICAgbWVtYnJhbmVbcHJvcF0gPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgbWVzc2FnZSkgOiB2b2lkIDA7XG4gICAgICAgICAgICByZXR1cm4gb2JqZWN0W3Byb3BdLmFwcGx5KG9iamVjdCwgYXJndW1lbnRzKTtcbiAgICAgICAgICB9O1xuICAgICAgICAgIHJldHVybiAnY29udGludWUnO1xuICAgICAgICB9XG5cbiAgICAgICAgLy8gVGhlc2UgcHJvcGVydGllcyBhcmUgbm9uLWVudW1lcmFibGUgdG8gcHJldmVudCBSZWFjdCBkZXYgdG9vbHMgZnJvbVxuICAgICAgICAvLyBzZWVpbmcgdGhlbSBhbmQgY2F1c2luZyBzcHVyaW91cyB3YXJuaW5ncyB3aGVuIGFjY2Vzc2luZyB0aGVtLiBJblxuICAgICAgICAvLyBwcmluY2lwbGUgdGhpcyBjb3VsZCBiZSBkb25lIHdpdGggYSBwcm94eSwgYnV0IHN1cHBvcnQgZm9yIHRoZVxuICAgICAgICAvLyBvd25LZXlzIHRyYXAgb24gcHJveGllcyBpcyBub3QgdW5pdmVyc2FsLCBldmVuIGFtb25nIGJyb3dzZXJzIHRoYXRcbiAgICAgICAgLy8gb3RoZXJ3aXNlIHN1cHBvcnQgcHJveGllcy5cbiAgICAgICAgT2JqZWN0LmRlZmluZVByb3BlcnR5KG1lbWJyYW5lLCBwcm9wLCB7XG4gICAgICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgbWVzc2FnZSkgOiB2b2lkIDA7XG4gICAgICAgICAgICByZXR1cm4gb2JqZWN0W3Byb3BdO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9O1xuXG4gICAgICBmb3IgKHZhciBwcm9wIGluIG9iamVjdCkge1xuICAgICAgICB2YXIgX3JldCA9IF9sb29wKHByb3ApO1xuXG4gICAgICAgIGlmIChfcmV0ID09PSAnY29udGludWUnKSBjb250aW51ZTtcbiAgICAgIH1cblxuICAgICAgcmV0dXJuIG1lbWJyYW5lO1xuICAgIH07XG4gIH1cbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllczsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfQXN5bmNVdGlscyA9IHJlcXVpcmUoJy4vQXN5bmNVdGlscycpO1xuXG52YXIgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbiA9IHJlcXVpcmUoJy4vbWFrZVN0YXRlV2l0aExvY2F0aW9uJyk7XG5cbnZhciBfbWFrZVN0YXRlV2l0aExvY2F0aW9uMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX21ha2VTdGF0ZVdpdGhMb2NhdGlvbik7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIGdldENvbXBvbmVudHNGb3JSb3V0ZShuZXh0U3RhdGUsIHJvdXRlLCBjYWxsYmFjaykge1xuICBpZiAocm91dGUuY29tcG9uZW50IHx8IHJvdXRlLmNvbXBvbmVudHMpIHtcbiAgICBjYWxsYmFjayhudWxsLCByb3V0ZS5jb21wb25lbnQgfHwgcm91dGUuY29tcG9uZW50cyk7XG4gICAgcmV0dXJuO1xuICB9XG5cbiAgdmFyIGdldENvbXBvbmVudCA9IHJvdXRlLmdldENvbXBvbmVudCB8fCByb3V0ZS5nZXRDb21wb25lbnRzO1xuICBpZiAoIWdldENvbXBvbmVudCkge1xuICAgIGNhbGxiYWNrKCk7XG4gICAgcmV0dXJuO1xuICB9XG5cbiAgdmFyIGxvY2F0aW9uID0gbmV4dFN0YXRlLmxvY2F0aW9uO1xuXG4gIHZhciBuZXh0U3RhdGVXaXRoTG9jYXRpb24gPSAoMCwgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbjIuZGVmYXVsdCkobmV4dFN0YXRlLCBsb2NhdGlvbik7XG5cbiAgZ2V0Q29tcG9uZW50LmNhbGwocm91dGUsIG5leHRTdGF0ZVdpdGhMb2NhdGlvbiwgY2FsbGJhY2spO1xufVxuXG4vKipcbiAqIEFzeW5jaHJvbm91c2x5IGZldGNoZXMgYWxsIGNvbXBvbmVudHMgbmVlZGVkIGZvciB0aGUgZ2l2ZW4gcm91dGVyXG4gKiBzdGF0ZSBhbmQgY2FsbHMgY2FsbGJhY2soZXJyb3IsIGNvbXBvbmVudHMpIHdoZW4gZmluaXNoZWQuXG4gKlxuICogTm90ZTogVGhpcyBvcGVyYXRpb24gbWF5IGZpbmlzaCBzeW5jaHJvbm91c2x5IGlmIG5vIHJvdXRlcyBoYXZlIGFuXG4gKiBhc3luY2hyb25vdXMgZ2V0Q29tcG9uZW50cyBtZXRob2QuXG4gKi9cbmZ1bmN0aW9uIGdldENvbXBvbmVudHMobmV4dFN0YXRlLCBjYWxsYmFjaykge1xuICAoMCwgX0FzeW5jVXRpbHMubWFwQXN5bmMpKG5leHRTdGF0ZS5yb3V0ZXMsIGZ1bmN0aW9uIChyb3V0ZSwgaW5kZXgsIGNhbGxiYWNrKSB7XG4gICAgZ2V0Q29tcG9uZW50c0ZvclJvdXRlKG5leHRTdGF0ZSwgcm91dGUsIGNhbGxiYWNrKTtcbiAgfSwgY2FsbGJhY2spO1xufVxuXG5leHBvcnRzLmRlZmF1bHQgPSBnZXRDb21wb25lbnRzO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbi8qKlxuICogRXh0cmFjdHMgYW4gb2JqZWN0IG9mIHBhcmFtcyB0aGUgZ2l2ZW4gcm91dGUgY2FyZXMgYWJvdXQgZnJvbVxuICogdGhlIGdpdmVuIHBhcmFtcyBvYmplY3QuXG4gKi9cbmZ1bmN0aW9uIGdldFJvdXRlUGFyYW1zKHJvdXRlLCBwYXJhbXMpIHtcbiAgdmFyIHJvdXRlUGFyYW1zID0ge307XG5cbiAgaWYgKCFyb3V0ZS5wYXRoKSByZXR1cm4gcm91dGVQYXJhbXM7XG5cbiAgKDAsIF9QYXR0ZXJuVXRpbHMuZ2V0UGFyYW1OYW1lcykocm91dGUucGF0aCkuZm9yRWFjaChmdW5jdGlvbiAocCkge1xuICAgIGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwocGFyYW1zLCBwKSkge1xuICAgICAgcm91dGVQYXJhbXNbcF0gPSBwYXJhbXNbcF07XG4gICAgfVxuICB9KTtcblxuICByZXR1cm4gcm91dGVQYXJhbXM7XG59XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGdldFJvdXRlUGFyYW1zO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2NyZWF0ZUhhc2hIaXN0b3J5ID0gcmVxdWlyZSgnaGlzdG9yeS9saWIvY3JlYXRlSGFzaEhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVIYXNoSGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVIYXNoSGlzdG9yeSk7XG5cbnZhciBfY3JlYXRlUm91dGVySGlzdG9yeSA9IHJlcXVpcmUoJy4vY3JlYXRlUm91dGVySGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZVJvdXRlckhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlUm91dGVySGlzdG9yeSk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmV4cG9ydHMuZGVmYXVsdCA9ICgwLCBfY3JlYXRlUm91dGVySGlzdG9yeTIuZGVmYXVsdCkoX2NyZWF0ZUhhc2hIaXN0b3J5Mi5kZWZhdWx0KTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuY3JlYXRlTWVtb3J5SGlzdG9yeSA9IGV4cG9ydHMuaGFzaEhpc3RvcnkgPSBleHBvcnRzLmJyb3dzZXJIaXN0b3J5ID0gZXhwb3J0cy5hcHBseVJvdXRlck1pZGRsZXdhcmUgPSBleHBvcnRzLmZvcm1hdFBhdHRlcm4gPSBleHBvcnRzLnVzZVJvdXRlckhpc3RvcnkgPSBleHBvcnRzLm1hdGNoID0gZXhwb3J0cy5yb3V0ZXJTaGFwZSA9IGV4cG9ydHMubG9jYXRpb25TaGFwZSA9IGV4cG9ydHMuUHJvcFR5cGVzID0gZXhwb3J0cy5Sb3V0aW5nQ29udGV4dCA9IGV4cG9ydHMuUm91dGVyQ29udGV4dCA9IGV4cG9ydHMuY3JlYXRlUm91dGVzID0gZXhwb3J0cy51c2VSb3V0ZXMgPSBleHBvcnRzLlJvdXRlQ29udGV4dCA9IGV4cG9ydHMuTGlmZWN5Y2xlID0gZXhwb3J0cy5IaXN0b3J5ID0gZXhwb3J0cy5Sb3V0ZSA9IGV4cG9ydHMuUmVkaXJlY3QgPSBleHBvcnRzLkluZGV4Um91dGUgPSBleHBvcnRzLkluZGV4UmVkaXJlY3QgPSBleHBvcnRzLndpdGhSb3V0ZXIgPSBleHBvcnRzLkluZGV4TGluayA9IGV4cG9ydHMuTGluayA9IGV4cG9ydHMuUm91dGVyID0gdW5kZWZpbmVkO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxuT2JqZWN0LmRlZmluZVByb3BlcnR5KGV4cG9ydHMsICdjcmVhdGVSb3V0ZXMnLCB7XG4gIGVudW1lcmFibGU6IHRydWUsXG4gIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgIHJldHVybiBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZXM7XG4gIH1cbn0pO1xuXG52YXIgX1Byb3BUeXBlczIgPSByZXF1aXJlKCcuL1Byb3BUeXBlcycpO1xuXG5PYmplY3QuZGVmaW5lUHJvcGVydHkoZXhwb3J0cywgJ2xvY2F0aW9uU2hhcGUnLCB7XG4gIGVudW1lcmFibGU6IHRydWUsXG4gIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgIHJldHVybiBfUHJvcFR5cGVzMi5sb2NhdGlvblNoYXBlO1xuICB9XG59KTtcbk9iamVjdC5kZWZpbmVQcm9wZXJ0eShleHBvcnRzLCAncm91dGVyU2hhcGUnLCB7XG4gIGVudW1lcmFibGU6IHRydWUsXG4gIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgIHJldHVybiBfUHJvcFR5cGVzMi5yb3V0ZXJTaGFwZTtcbiAgfVxufSk7XG5cbnZhciBfUGF0dGVyblV0aWxzID0gcmVxdWlyZSgnLi9QYXR0ZXJuVXRpbHMnKTtcblxuT2JqZWN0LmRlZmluZVByb3BlcnR5KGV4cG9ydHMsICdmb3JtYXRQYXR0ZXJuJywge1xuICBlbnVtZXJhYmxlOiB0cnVlLFxuICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICByZXR1cm4gX1BhdHRlcm5VdGlscy5mb3JtYXRQYXR0ZXJuO1xuICB9XG59KTtcblxudmFyIF9Sb3V0ZXIyID0gcmVxdWlyZSgnLi9Sb3V0ZXInKTtcblxudmFyIF9Sb3V0ZXIzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUm91dGVyMik7XG5cbnZhciBfTGluazIgPSByZXF1aXJlKCcuL0xpbmsnKTtcblxudmFyIF9MaW5rMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0xpbmsyKTtcblxudmFyIF9JbmRleExpbmsyID0gcmVxdWlyZSgnLi9JbmRleExpbmsnKTtcblxudmFyIF9JbmRleExpbmszID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfSW5kZXhMaW5rMik7XG5cbnZhciBfd2l0aFJvdXRlcjIgPSByZXF1aXJlKCcuL3dpdGhSb3V0ZXInKTtcblxudmFyIF93aXRoUm91dGVyMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dpdGhSb3V0ZXIyKTtcblxudmFyIF9JbmRleFJlZGlyZWN0MiA9IHJlcXVpcmUoJy4vSW5kZXhSZWRpcmVjdCcpO1xuXG52YXIgX0luZGV4UmVkaXJlY3QzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfSW5kZXhSZWRpcmVjdDIpO1xuXG52YXIgX0luZGV4Um91dGUyID0gcmVxdWlyZSgnLi9JbmRleFJvdXRlJyk7XG5cbnZhciBfSW5kZXhSb3V0ZTMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9JbmRleFJvdXRlMik7XG5cbnZhciBfUmVkaXJlY3QyID0gcmVxdWlyZSgnLi9SZWRpcmVjdCcpO1xuXG52YXIgX1JlZGlyZWN0MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JlZGlyZWN0Mik7XG5cbnZhciBfUm91dGUyID0gcmVxdWlyZSgnLi9Sb3V0ZScpO1xuXG52YXIgX1JvdXRlMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlMik7XG5cbnZhciBfSGlzdG9yeTIgPSByZXF1aXJlKCcuL0hpc3RvcnknKTtcblxudmFyIF9IaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0hpc3RvcnkyKTtcblxudmFyIF9MaWZlY3ljbGUyID0gcmVxdWlyZSgnLi9MaWZlY3ljbGUnKTtcblxudmFyIF9MaWZlY3ljbGUzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfTGlmZWN5Y2xlMik7XG5cbnZhciBfUm91dGVDb250ZXh0MiA9IHJlcXVpcmUoJy4vUm91dGVDb250ZXh0Jyk7XG5cbnZhciBfUm91dGVDb250ZXh0MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlQ29udGV4dDIpO1xuXG52YXIgX3VzZVJvdXRlczIgPSByZXF1aXJlKCcuL3VzZVJvdXRlcycpO1xuXG52YXIgX3VzZVJvdXRlczMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF91c2VSb3V0ZXMyKTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0MiA9IHJlcXVpcmUoJy4vUm91dGVyQ29udGV4dCcpO1xuXG52YXIgX1JvdXRlckNvbnRleHQzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUm91dGVyQ29udGV4dDIpO1xuXG52YXIgX1JvdXRpbmdDb250ZXh0MiA9IHJlcXVpcmUoJy4vUm91dGluZ0NvbnRleHQnKTtcblxudmFyIF9Sb3V0aW5nQ29udGV4dDMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0aW5nQ29udGV4dDIpO1xuXG52YXIgX1Byb3BUeXBlczMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Qcm9wVHlwZXMyKTtcblxudmFyIF9tYXRjaDIgPSByZXF1aXJlKCcuL21hdGNoJyk7XG5cbnZhciBfbWF0Y2gzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfbWF0Y2gyKTtcblxudmFyIF91c2VSb3V0ZXJIaXN0b3J5MiA9IHJlcXVpcmUoJy4vdXNlUm91dGVySGlzdG9yeScpO1xuXG52YXIgX3VzZVJvdXRlckhpc3RvcnkzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUm91dGVySGlzdG9yeTIpO1xuXG52YXIgX2FwcGx5Um91dGVyTWlkZGxld2FyZTIgPSByZXF1aXJlKCcuL2FwcGx5Um91dGVyTWlkZGxld2FyZScpO1xuXG52YXIgX2FwcGx5Um91dGVyTWlkZGxld2FyZTMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9hcHBseVJvdXRlck1pZGRsZXdhcmUyKTtcblxudmFyIF9icm93c2VySGlzdG9yeTIgPSByZXF1aXJlKCcuL2Jyb3dzZXJIaXN0b3J5Jyk7XG5cbnZhciBfYnJvd3Nlckhpc3RvcnkzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfYnJvd3Nlckhpc3RvcnkyKTtcblxudmFyIF9oYXNoSGlzdG9yeTIgPSByZXF1aXJlKCcuL2hhc2hIaXN0b3J5Jyk7XG5cbnZhciBfaGFzaEhpc3RvcnkzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaGFzaEhpc3RvcnkyKTtcblxudmFyIF9jcmVhdGVNZW1vcnlIaXN0b3J5MiA9IHJlcXVpcmUoJy4vY3JlYXRlTWVtb3J5SGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZU1lbW9yeUhpc3RvcnkzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlTWVtb3J5SGlzdG9yeTIpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5leHBvcnRzLlJvdXRlciA9IF9Sb3V0ZXIzLmRlZmF1bHQ7IC8qIGNvbXBvbmVudHMgKi9cblxuZXhwb3J0cy5MaW5rID0gX0xpbmszLmRlZmF1bHQ7XG5leHBvcnRzLkluZGV4TGluayA9IF9JbmRleExpbmszLmRlZmF1bHQ7XG5leHBvcnRzLndpdGhSb3V0ZXIgPSBfd2l0aFJvdXRlcjMuZGVmYXVsdDtcblxuLyogY29tcG9uZW50cyAoY29uZmlndXJhdGlvbikgKi9cblxuZXhwb3J0cy5JbmRleFJlZGlyZWN0ID0gX0luZGV4UmVkaXJlY3QzLmRlZmF1bHQ7XG5leHBvcnRzLkluZGV4Um91dGUgPSBfSW5kZXhSb3V0ZTMuZGVmYXVsdDtcbmV4cG9ydHMuUmVkaXJlY3QgPSBfUmVkaXJlY3QzLmRlZmF1bHQ7XG5leHBvcnRzLlJvdXRlID0gX1JvdXRlMy5kZWZhdWx0O1xuXG4vKiBtaXhpbnMgKi9cblxuZXhwb3J0cy5IaXN0b3J5ID0gX0hpc3RvcnkzLmRlZmF1bHQ7XG5leHBvcnRzLkxpZmVjeWNsZSA9IF9MaWZlY3ljbGUzLmRlZmF1bHQ7XG5leHBvcnRzLlJvdXRlQ29udGV4dCA9IF9Sb3V0ZUNvbnRleHQzLmRlZmF1bHQ7XG5cbi8qIHV0aWxzICovXG5cbmV4cG9ydHMudXNlUm91dGVzID0gX3VzZVJvdXRlczMuZGVmYXVsdDtcbmV4cG9ydHMuUm91dGVyQ29udGV4dCA9IF9Sb3V0ZXJDb250ZXh0My5kZWZhdWx0O1xuZXhwb3J0cy5Sb3V0aW5nQ29udGV4dCA9IF9Sb3V0aW5nQ29udGV4dDMuZGVmYXVsdDtcbmV4cG9ydHMuUHJvcFR5cGVzID0gX1Byb3BUeXBlczMuZGVmYXVsdDtcbmV4cG9ydHMubWF0Y2ggPSBfbWF0Y2gzLmRlZmF1bHQ7XG5leHBvcnRzLnVzZVJvdXRlckhpc3RvcnkgPSBfdXNlUm91dGVySGlzdG9yeTMuZGVmYXVsdDtcbmV4cG9ydHMuYXBwbHlSb3V0ZXJNaWRkbGV3YXJlID0gX2FwcGx5Um91dGVyTWlkZGxld2FyZTMuZGVmYXVsdDtcblxuLyogaGlzdG9yaWVzICovXG5cbmV4cG9ydHMuYnJvd3Nlckhpc3RvcnkgPSBfYnJvd3Nlckhpc3RvcnkzLmRlZmF1bHQ7XG5leHBvcnRzLmhhc2hIaXN0b3J5ID0gX2hhc2hIaXN0b3J5My5kZWZhdWx0O1xuZXhwb3J0cy5jcmVhdGVNZW1vcnlIaXN0b3J5ID0gX2NyZWF0ZU1lbW9yeUhpc3RvcnkzLmRlZmF1bHQ7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3R5cGVvZiA9IHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiB0eXBlb2YgU3ltYm9sLml0ZXJhdG9yID09PSBcInN5bWJvbFwiID8gZnVuY3Rpb24gKG9iaikgeyByZXR1cm4gdHlwZW9mIG9iajsgfSA6IGZ1bmN0aW9uIChvYmopIHsgcmV0dXJuIG9iaiAmJiB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgb2JqLmNvbnN0cnVjdG9yID09PSBTeW1ib2wgPyBcInN5bWJvbFwiIDogdHlwZW9mIG9iajsgfTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gaXNBY3RpdmU7XG5cbnZhciBfUGF0dGVyblV0aWxzID0gcmVxdWlyZSgnLi9QYXR0ZXJuVXRpbHMnKTtcblxuZnVuY3Rpb24gZGVlcEVxdWFsKGEsIGIpIHtcbiAgaWYgKGEgPT0gYikgcmV0dXJuIHRydWU7XG5cbiAgaWYgKGEgPT0gbnVsbCB8fCBiID09IG51bGwpIHJldHVybiBmYWxzZTtcblxuICBpZiAoQXJyYXkuaXNBcnJheShhKSkge1xuICAgIHJldHVybiBBcnJheS5pc0FycmF5KGIpICYmIGEubGVuZ3RoID09PSBiLmxlbmd0aCAmJiBhLmV2ZXJ5KGZ1bmN0aW9uIChpdGVtLCBpbmRleCkge1xuICAgICAgcmV0dXJuIGRlZXBFcXVhbChpdGVtLCBiW2luZGV4XSk7XG4gICAgfSk7XG4gIH1cblxuICBpZiAoKHR5cGVvZiBhID09PSAndW5kZWZpbmVkJyA/ICd1bmRlZmluZWQnIDogX3R5cGVvZihhKSkgPT09ICdvYmplY3QnKSB7XG4gICAgZm9yICh2YXIgcCBpbiBhKSB7XG4gICAgICBpZiAoIU9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChhLCBwKSkge1xuICAgICAgICBjb250aW51ZTtcbiAgICAgIH1cblxuICAgICAgaWYgKGFbcF0gPT09IHVuZGVmaW5lZCkge1xuICAgICAgICBpZiAoYltwXSAhPT0gdW5kZWZpbmVkKSB7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9XG4gICAgICB9IGVsc2UgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoYiwgcCkpIHtcbiAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgfSBlbHNlIGlmICghZGVlcEVxdWFsKGFbcF0sIGJbcF0pKSB7XG4gICAgICAgIHJldHVybiBmYWxzZTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICByZXR1cm4gdHJ1ZTtcbiAgfVxuXG4gIHJldHVybiBTdHJpbmcoYSkgPT09IFN0cmluZyhiKTtcbn1cblxuLyoqXG4gKiBSZXR1cm5zIHRydWUgaWYgdGhlIGN1cnJlbnQgcGF0aG5hbWUgbWF0Y2hlcyB0aGUgc3VwcGxpZWQgb25lLCBuZXQgb2ZcbiAqIGxlYWRpbmcgYW5kIHRyYWlsaW5nIHNsYXNoIG5vcm1hbGl6YXRpb24uIFRoaXMgaXMgc3VmZmljaWVudCBmb3IgYW5cbiAqIGluZGV4T25seSByb3V0ZSBtYXRjaC5cbiAqL1xuZnVuY3Rpb24gcGF0aElzQWN0aXZlKHBhdGhuYW1lLCBjdXJyZW50UGF0aG5hbWUpIHtcbiAgLy8gTm9ybWFsaXplIGxlYWRpbmcgc2xhc2ggZm9yIGNvbnNpc3RlbmN5LiBMZWFkaW5nIHNsYXNoIG9uIHBhdGhuYW1lIGhhc1xuICAvLyBhbHJlYWR5IGJlZW4gbm9ybWFsaXplZCBpbiBpc0FjdGl2ZS4gU2VlIGNhdmVhdCB0aGVyZS5cbiAgaWYgKGN1cnJlbnRQYXRobmFtZS5jaGFyQXQoMCkgIT09ICcvJykge1xuICAgIGN1cnJlbnRQYXRobmFtZSA9ICcvJyArIGN1cnJlbnRQYXRobmFtZTtcbiAgfVxuXG4gIC8vIE5vcm1hbGl6ZSB0aGUgZW5kIG9mIGJvdGggcGF0aCBuYW1lcyB0b28uIE1heWJlIGAvZm9vL2Agc2hvdWxkbid0IHNob3dcbiAgLy8gYC9mb29gIGFzIGFjdGl2ZSwgYnV0IGluIHRoaXMgY2FzZSwgd2Ugd291bGQgYWxyZWFkeSBoYXZlIGZhaWxlZCB0aGVcbiAgLy8gbWF0Y2guXG4gIGlmIChwYXRobmFtZS5jaGFyQXQocGF0aG5hbWUubGVuZ3RoIC0gMSkgIT09ICcvJykge1xuICAgIHBhdGhuYW1lICs9ICcvJztcbiAgfVxuICBpZiAoY3VycmVudFBhdGhuYW1lLmNoYXJBdChjdXJyZW50UGF0aG5hbWUubGVuZ3RoIC0gMSkgIT09ICcvJykge1xuICAgIGN1cnJlbnRQYXRobmFtZSArPSAnLyc7XG4gIH1cblxuICByZXR1cm4gY3VycmVudFBhdGhuYW1lID09PSBwYXRobmFtZTtcbn1cblxuLyoqXG4gKiBSZXR1cm5zIHRydWUgaWYgdGhlIGdpdmVuIHBhdGhuYW1lIG1hdGNoZXMgdGhlIGFjdGl2ZSByb3V0ZXMgYW5kIHBhcmFtcy5cbiAqL1xuZnVuY3Rpb24gcm91dGVJc0FjdGl2ZShwYXRobmFtZSwgcm91dGVzLCBwYXJhbXMpIHtcbiAgdmFyIHJlbWFpbmluZ1BhdGhuYW1lID0gcGF0aG5hbWUsXG4gICAgICBwYXJhbU5hbWVzID0gW10sXG4gICAgICBwYXJhbVZhbHVlcyA9IFtdO1xuXG4gIC8vIGZvci4uLm9mIHdvdWxkIHdvcmsgaGVyZSBidXQgaXQncyBwcm9iYWJseSBzbG93ZXIgcG9zdC10cmFuc3BpbGF0aW9uLlxuICBmb3IgKHZhciBpID0gMCwgbGVuID0gcm91dGVzLmxlbmd0aDsgaSA8IGxlbjsgKytpKSB7XG4gICAgdmFyIHJvdXRlID0gcm91dGVzW2ldO1xuICAgIHZhciBwYXR0ZXJuID0gcm91dGUucGF0aCB8fCAnJztcblxuICAgIGlmIChwYXR0ZXJuLmNoYXJBdCgwKSA9PT0gJy8nKSB7XG4gICAgICByZW1haW5pbmdQYXRobmFtZSA9IHBhdGhuYW1lO1xuICAgICAgcGFyYW1OYW1lcyA9IFtdO1xuICAgICAgcGFyYW1WYWx1ZXMgPSBbXTtcbiAgICB9XG5cbiAgICBpZiAocmVtYWluaW5nUGF0aG5hbWUgIT09IG51bGwgJiYgcGF0dGVybikge1xuICAgICAgdmFyIG1hdGNoZWQgPSAoMCwgX1BhdHRlcm5VdGlscy5tYXRjaFBhdHRlcm4pKHBhdHRlcm4sIHJlbWFpbmluZ1BhdGhuYW1lKTtcbiAgICAgIGlmIChtYXRjaGVkKSB7XG4gICAgICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbWF0Y2hlZC5yZW1haW5pbmdQYXRobmFtZTtcbiAgICAgICAgcGFyYW1OYW1lcyA9IFtdLmNvbmNhdChwYXJhbU5hbWVzLCBtYXRjaGVkLnBhcmFtTmFtZXMpO1xuICAgICAgICBwYXJhbVZhbHVlcyA9IFtdLmNvbmNhdChwYXJhbVZhbHVlcywgbWF0Y2hlZC5wYXJhbVZhbHVlcyk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZW1haW5pbmdQYXRobmFtZSA9IG51bGw7XG4gICAgICB9XG5cbiAgICAgIGlmIChyZW1haW5pbmdQYXRobmFtZSA9PT0gJycpIHtcbiAgICAgICAgLy8gV2UgaGF2ZSBhbiBleGFjdCBtYXRjaCBvbiB0aGUgcm91dGUuIEp1c3QgY2hlY2sgdGhhdCBhbGwgdGhlIHBhcmFtc1xuICAgICAgICAvLyBtYXRjaC5cbiAgICAgICAgLy8gRklYTUU6IFRoaXMgZG9lc24ndCB3b3JrIG9uIHJlcGVhdGVkIHBhcmFtcy5cbiAgICAgICAgcmV0dXJuIHBhcmFtTmFtZXMuZXZlcnkoZnVuY3Rpb24gKHBhcmFtTmFtZSwgaW5kZXgpIHtcbiAgICAgICAgICByZXR1cm4gU3RyaW5nKHBhcmFtVmFsdWVzW2luZGV4XSkgPT09IFN0cmluZyhwYXJhbXNbcGFyYW1OYW1lXSk7XG4gICAgICAgIH0pO1xuICAgICAgfVxuICAgIH1cbiAgfVxuXG4gIHJldHVybiBmYWxzZTtcbn1cblxuLyoqXG4gKiBSZXR1cm5zIHRydWUgaWYgYWxsIGtleS92YWx1ZSBwYWlycyBpbiB0aGUgZ2l2ZW4gcXVlcnkgYXJlXG4gKiBjdXJyZW50bHkgYWN0aXZlLlxuICovXG5mdW5jdGlvbiBxdWVyeUlzQWN0aXZlKHF1ZXJ5LCBhY3RpdmVRdWVyeSkge1xuICBpZiAoYWN0aXZlUXVlcnkgPT0gbnVsbCkgcmV0dXJuIHF1ZXJ5ID09IG51bGw7XG5cbiAgaWYgKHF1ZXJ5ID09IG51bGwpIHJldHVybiB0cnVlO1xuXG4gIHJldHVybiBkZWVwRXF1YWwocXVlcnksIGFjdGl2ZVF1ZXJ5KTtcbn1cblxuLyoqXG4gKiBSZXR1cm5zIHRydWUgaWYgYSA8TGluaz4gdG8gdGhlIGdpdmVuIHBhdGhuYW1lL3F1ZXJ5IGNvbWJpbmF0aW9uIGlzXG4gKiBjdXJyZW50bHkgYWN0aXZlLlxuICovXG5mdW5jdGlvbiBpc0FjdGl2ZShfcmVmLCBpbmRleE9ubHksIGN1cnJlbnRMb2NhdGlvbiwgcm91dGVzLCBwYXJhbXMpIHtcbiAgdmFyIHBhdGhuYW1lID0gX3JlZi5wYXRobmFtZTtcbiAgdmFyIHF1ZXJ5ID0gX3JlZi5xdWVyeTtcblxuICBpZiAoY3VycmVudExvY2F0aW9uID09IG51bGwpIHJldHVybiBmYWxzZTtcblxuICAvLyBUT0RPOiBUaGlzIGlzIGEgYml0IHVnbHkuIEl0IGtlZXBzIGFyb3VuZCBzdXBwb3J0IGZvciB0cmVhdGluZyBwYXRobmFtZXNcbiAgLy8gd2l0aG91dCBwcmVjZWRpbmcgc2xhc2hlcyBhcyBhYnNvbHV0ZSBwYXRocywgYnV0IHBvc3NpYmx5IGFsc28gd29ya3NcbiAgLy8gYXJvdW5kIHRoZSBzYW1lIHF1aXJrcyB3aXRoIGJhc2VuYW1lcyBhcyBpbiBtYXRjaFJvdXRlcy5cbiAgaWYgKHBhdGhuYW1lLmNoYXJBdCgwKSAhPT0gJy8nKSB7XG4gICAgcGF0aG5hbWUgPSAnLycgKyBwYXRobmFtZTtcbiAgfVxuXG4gIGlmICghcGF0aElzQWN0aXZlKHBhdGhuYW1lLCBjdXJyZW50TG9jYXRpb24ucGF0aG5hbWUpKSB7XG4gICAgLy8gVGhlIHBhdGggY2hlY2sgaXMgbmVjZXNzYXJ5IGFuZCBzdWZmaWNpZW50IGZvciBpbmRleE9ubHksIGJ1dCBvdGhlcndpc2VcbiAgICAvLyB3ZSBzdGlsbCBuZWVkIHRvIGNoZWNrIHRoZSByb3V0ZXMuXG4gICAgaWYgKGluZGV4T25seSB8fCAhcm91dGVJc0FjdGl2ZShwYXRobmFtZSwgcm91dGVzLCBwYXJhbXMpKSB7XG4gICAgICByZXR1cm4gZmFsc2U7XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIHF1ZXJ5SXNBY3RpdmUocXVlcnksIGN1cnJlbnRMb2NhdGlvbi5xdWVyeSk7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHMuZGVmYXVsdCA9IG1ha2VTdGF0ZVdpdGhMb2NhdGlvbjtcblxudmFyIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzID0gcmVxdWlyZSgnLi9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzJyk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIG1ha2VTdGF0ZVdpdGhMb2NhdGlvbihzdGF0ZSwgbG9jYXRpb24pIHtcbiAgaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgJiYgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMuY2FuVXNlTWVtYnJhbmUpIHtcbiAgICB2YXIgc3RhdGVXaXRoTG9jYXRpb24gPSBfZXh0ZW5kcyh7fSwgc3RhdGUpO1xuXG4gICAgLy8gSSBkb24ndCB1c2UgZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyBoZXJlIGJlY2F1c2UgSSB3YW50IHRvIGtlZXAgdGhlXG4gICAgLy8gc2FtZSBjb2RlIHBhdGggYmV0d2VlbiBkZXZlbG9wbWVudCBhbmQgcHJvZHVjdGlvbiwgaW4gdGhhdCB3ZSBqdXN0XG4gICAgLy8gYXNzaWduIGV4dHJhIHByb3BlcnRpZXMgdG8gdGhlIGNvcHkgb2YgdGhlIHN0YXRlIG9iamVjdCBpbiBib3RoIGNhc2VzLlxuXG4gICAgdmFyIF9sb29wID0gZnVuY3Rpb24gX2xvb3AocHJvcCkge1xuICAgICAgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwobG9jYXRpb24sIHByb3ApKSB7XG4gICAgICAgIHJldHVybiAnY29udGludWUnO1xuICAgICAgfVxuXG4gICAgICBPYmplY3QuZGVmaW5lUHJvcGVydHkoc3RhdGVXaXRoTG9jYXRpb24sIHByb3AsIHtcbiAgICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdBY2Nlc3NpbmcgbG9jYXRpb24gcHJvcGVydGllcyBkaXJlY3RseSBmcm9tIHRoZSBmaXJzdCBhcmd1bWVudCB0byBgZ2V0Q29tcG9uZW50YCwgYGdldENvbXBvbmVudHNgLCBgZ2V0Q2hpbGRSb3V0ZXNgLCBhbmQgYGdldEluZGV4Um91dGVgIGlzIGRlcHJlY2F0ZWQuIFRoYXQgYXJndW1lbnQgaXMgbm93IHRoZSByb3V0ZXIgc3RhdGUgKGBuZXh0U3RhdGVgIG9yIGBwYXJ0aWFsTmV4dFN0YXRlYCkgcmF0aGVyIHRoYW4gdGhlIGxvY2F0aW9uLiBUbyBhY2Nlc3MgdGhlIGxvY2F0aW9uLCB1c2UgYG5leHRTdGF0ZS5sb2NhdGlvbmAgb3IgYHBhcnRpYWxOZXh0U3RhdGUubG9jYXRpb25gLicpIDogdm9pZCAwO1xuICAgICAgICAgIHJldHVybiBsb2NhdGlvbltwcm9wXTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfTtcblxuICAgIGZvciAodmFyIHByb3AgaW4gbG9jYXRpb24pIHtcbiAgICAgIHZhciBfcmV0ID0gX2xvb3AocHJvcCk7XG5cbiAgICAgIGlmIChfcmV0ID09PSAnY29udGludWUnKSBjb250aW51ZTtcbiAgICB9XG5cbiAgICByZXR1cm4gc3RhdGVXaXRoTG9jYXRpb247XG4gIH1cblxuICByZXR1cm4gX2V4dGVuZHMoe30sIHN0YXRlLCBsb2NhdGlvbik7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfY3JlYXRlTWVtb3J5SGlzdG9yeSA9IHJlcXVpcmUoJy4vY3JlYXRlTWVtb3J5SGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZU1lbW9yeUhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlTWVtb3J5SGlzdG9yeSk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIgPSByZXF1aXJlKCcuL2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyJyk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxudmFyIF9Sb3V0ZXJVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVyVXRpbHMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKG9iaiwga2V5cykgeyB2YXIgdGFyZ2V0ID0ge307IGZvciAodmFyIGkgaW4gb2JqKSB7IGlmIChrZXlzLmluZGV4T2YoaSkgPj0gMCkgY29udGludWU7IGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwgaSkpIGNvbnRpbnVlOyB0YXJnZXRbaV0gPSBvYmpbaV07IH0gcmV0dXJuIHRhcmdldDsgfVxuXG4vKipcbiAqIEEgaGlnaC1sZXZlbCBBUEkgdG8gYmUgdXNlZCBmb3Igc2VydmVyLXNpZGUgcmVuZGVyaW5nLlxuICpcbiAqIFRoaXMgZnVuY3Rpb24gbWF0Y2hlcyBhIGxvY2F0aW9uIHRvIGEgc2V0IG9mIHJvdXRlcyBhbmQgY2FsbHNcbiAqIGNhbGxiYWNrKGVycm9yLCByZWRpcmVjdExvY2F0aW9uLCByZW5kZXJQcm9wcykgd2hlbiBmaW5pc2hlZC5cbiAqXG4gKiBOb3RlOiBZb3UgcHJvYmFibHkgZG9uJ3Qgd2FudCB0byB1c2UgdGhpcyBpbiBhIGJyb3dzZXIgdW5sZXNzIHlvdSdyZSB1c2luZ1xuICogc2VydmVyLXNpZGUgcmVuZGVyaW5nIHdpdGggYXN5bmMgcm91dGVzLlxuICovXG5mdW5jdGlvbiBtYXRjaChfcmVmLCBjYWxsYmFjaykge1xuICB2YXIgaGlzdG9yeSA9IF9yZWYuaGlzdG9yeTtcbiAgdmFyIHJvdXRlcyA9IF9yZWYucm91dGVzO1xuICB2YXIgbG9jYXRpb24gPSBfcmVmLmxvY2F0aW9uO1xuXG4gIHZhciBvcHRpb25zID0gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKF9yZWYsIFsnaGlzdG9yeScsICdyb3V0ZXMnLCAnbG9jYXRpb24nXSk7XG5cbiAgIShoaXN0b3J5IHx8IGxvY2F0aW9uKSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdtYXRjaCBuZWVkcyBhIGhpc3Rvcnkgb3IgYSBsb2NhdGlvbicpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICBoaXN0b3J5ID0gaGlzdG9yeSA/IGhpc3RvcnkgOiAoMCwgX2NyZWF0ZU1lbW9yeUhpc3RvcnkyLmRlZmF1bHQpKG9wdGlvbnMpO1xuICB2YXIgdHJhbnNpdGlvbk1hbmFnZXIgPSAoMCwgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyMi5kZWZhdWx0KShoaXN0b3J5LCAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVzKShyb3V0ZXMpKTtcblxuICB2YXIgdW5saXN0ZW4gPSB2b2lkIDA7XG5cbiAgaWYgKGxvY2F0aW9uKSB7XG4gICAgLy8gQWxsb3cgbWF0Y2goeyBsb2NhdGlvbjogJy90aGUvcGF0aCcsIC4uLiB9KVxuICAgIGxvY2F0aW9uID0gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbihsb2NhdGlvbik7XG4gIH0gZWxzZSB7XG4gICAgLy8gUGljayB1cCB0aGUgbG9jYXRpb24gZnJvbSB0aGUgaGlzdG9yeSB2aWEgc3luY2hyb25vdXMgaGlzdG9yeS5saXN0ZW5cbiAgICAvLyBjYWxsIGlmIG5lZWRlZC5cbiAgICB1bmxpc3RlbiA9IGhpc3RvcnkubGlzdGVuKGZ1bmN0aW9uIChoaXN0b3J5TG9jYXRpb24pIHtcbiAgICAgIGxvY2F0aW9uID0gaGlzdG9yeUxvY2F0aW9uO1xuICAgIH0pO1xuICB9XG5cbiAgdmFyIHJvdXRlciA9ICgwLCBfUm91dGVyVXRpbHMuY3JlYXRlUm91dGVyT2JqZWN0KShoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG4gIGhpc3RvcnkgPSAoMCwgX1JvdXRlclV0aWxzLmNyZWF0ZVJvdXRpbmdIaXN0b3J5KShoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG5cbiAgdHJhbnNpdGlvbk1hbmFnZXIubWF0Y2gobG9jYXRpb24sIGZ1bmN0aW9uIChlcnJvciwgcmVkaXJlY3RMb2NhdGlvbiwgbmV4dFN0YXRlKSB7XG4gICAgY2FsbGJhY2soZXJyb3IsIHJlZGlyZWN0TG9jYXRpb24sIG5leHRTdGF0ZSAmJiBfZXh0ZW5kcyh7fSwgbmV4dFN0YXRlLCB7XG4gICAgICBoaXN0b3J5OiBoaXN0b3J5LFxuICAgICAgcm91dGVyOiByb3V0ZXIsXG4gICAgICBtYXRjaENvbnRleHQ6IHsgaGlzdG9yeTogaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXI6IHRyYW5zaXRpb25NYW5hZ2VyLCByb3V0ZXI6IHJvdXRlciB9XG4gICAgfSkpO1xuXG4gICAgLy8gRGVmZXIgcmVtb3ZpbmcgdGhlIGxpc3RlbmVyIHRvIGhlcmUgdG8gcHJldmVudCBET00gaGlzdG9yaWVzIGZyb20gaGF2aW5nXG4gICAgLy8gdG8gdW53aW5kIERPTSBldmVudCBsaXN0ZW5lcnMgdW5uZWNlc3NhcmlseSwgaW4gY2FzZSBjYWxsYmFjayByZW5kZXJzIGFcbiAgICAvLyA8Um91dGVyPiBhbmQgYXR0YWNoZXMgYW5vdGhlciBoaXN0b3J5IGxpc3RlbmVyLlxuICAgIGlmICh1bmxpc3Rlbikge1xuICAgICAgdW5saXN0ZW4oKTtcbiAgICB9XG4gIH0pO1xufVxuXG5leHBvcnRzLmRlZmF1bHQgPSBtYXRjaDtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxudmFyIF90eXBlb2YgPSB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgdHlwZW9mIFN5bWJvbC5pdGVyYXRvciA9PT0gXCJzeW1ib2xcIiA/IGZ1bmN0aW9uIChvYmopIHsgcmV0dXJuIHR5cGVvZiBvYmo7IH0gOiBmdW5jdGlvbiAob2JqKSB7IHJldHVybiBvYmogJiYgdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIG9iai5jb25zdHJ1Y3RvciA9PT0gU3ltYm9sID8gXCJzeW1ib2xcIiA6IHR5cGVvZiBvYmo7IH07XG5cbmV4cG9ydHMuZGVmYXVsdCA9IG1hdGNoUm91dGVzO1xuXG52YXIgX0FzeW5jVXRpbHMgPSByZXF1aXJlKCcuL0FzeW5jVXRpbHMnKTtcblxudmFyIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24gPSByZXF1aXJlKCcuL21ha2VTdGF0ZVdpdGhMb2NhdGlvbicpO1xuXG52YXIgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9tYWtlU3RhdGVXaXRoTG9jYXRpb24pO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbnZhciBfUm91dGVVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVVdGlscycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBnZXRDaGlsZFJvdXRlcyhyb3V0ZSwgbG9jYXRpb24sIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzLCBjYWxsYmFjaykge1xuICBpZiAocm91dGUuY2hpbGRSb3V0ZXMpIHtcbiAgICByZXR1cm4gW251bGwsIHJvdXRlLmNoaWxkUm91dGVzXTtcbiAgfVxuICBpZiAoIXJvdXRlLmdldENoaWxkUm91dGVzKSB7XG4gICAgcmV0dXJuIFtdO1xuICB9XG5cbiAgdmFyIHN5bmMgPSB0cnVlLFxuICAgICAgcmVzdWx0ID0gdm9pZCAwO1xuXG4gIHZhciBwYXJ0aWFsTmV4dFN0YXRlID0ge1xuICAgIGxvY2F0aW9uOiBsb2NhdGlvbixcbiAgICBwYXJhbXM6IGNyZWF0ZVBhcmFtcyhwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcylcbiAgfTtcblxuICB2YXIgcGFydGlhbE5leHRTdGF0ZVdpdGhMb2NhdGlvbiA9ICgwLCBfbWFrZVN0YXRlV2l0aExvY2F0aW9uMi5kZWZhdWx0KShwYXJ0aWFsTmV4dFN0YXRlLCBsb2NhdGlvbik7XG5cbiAgcm91dGUuZ2V0Q2hpbGRSb3V0ZXMocGFydGlhbE5leHRTdGF0ZVdpdGhMb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCBjaGlsZFJvdXRlcykge1xuICAgIGNoaWxkUm91dGVzID0gIWVycm9yICYmICgwLCBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZXMpKGNoaWxkUm91dGVzKTtcbiAgICBpZiAoc3luYykge1xuICAgICAgcmVzdWx0ID0gW2Vycm9yLCBjaGlsZFJvdXRlc107XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgY2FsbGJhY2soZXJyb3IsIGNoaWxkUm91dGVzKTtcbiAgfSk7XG5cbiAgc3luYyA9IGZhbHNlO1xuICByZXR1cm4gcmVzdWx0OyAvLyBNaWdodCBiZSB1bmRlZmluZWQuXG59XG5cbmZ1bmN0aW9uIGdldEluZGV4Um91dGUocm91dGUsIGxvY2F0aW9uLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgY2FsbGJhY2spIHtcbiAgaWYgKHJvdXRlLmluZGV4Um91dGUpIHtcbiAgICBjYWxsYmFjayhudWxsLCByb3V0ZS5pbmRleFJvdXRlKTtcbiAgfSBlbHNlIGlmIChyb3V0ZS5nZXRJbmRleFJvdXRlKSB7XG4gICAgdmFyIHBhcnRpYWxOZXh0U3RhdGUgPSB7XG4gICAgICBsb2NhdGlvbjogbG9jYXRpb24sXG4gICAgICBwYXJhbXM6IGNyZWF0ZVBhcmFtcyhwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcylcbiAgICB9O1xuXG4gICAgdmFyIHBhcnRpYWxOZXh0U3RhdGVXaXRoTG9jYXRpb24gPSAoMCwgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbjIuZGVmYXVsdCkocGFydGlhbE5leHRTdGF0ZSwgbG9jYXRpb24pO1xuXG4gICAgcm91dGUuZ2V0SW5kZXhSb3V0ZShwYXJ0aWFsTmV4dFN0YXRlV2l0aExvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIGluZGV4Um91dGUpIHtcbiAgICAgIGNhbGxiYWNrKGVycm9yLCAhZXJyb3IgJiYgKDAsIF9Sb3V0ZVV0aWxzLmNyZWF0ZVJvdXRlcykoaW5kZXhSb3V0ZSlbMF0pO1xuICAgIH0pO1xuICB9IGVsc2UgaWYgKHJvdXRlLmNoaWxkUm91dGVzKSB7XG4gICAgKGZ1bmN0aW9uICgpIHtcbiAgICAgIHZhciBwYXRobGVzcyA9IHJvdXRlLmNoaWxkUm91dGVzLmZpbHRlcihmdW5jdGlvbiAoY2hpbGRSb3V0ZSkge1xuICAgICAgICByZXR1cm4gIWNoaWxkUm91dGUucGF0aDtcbiAgICAgIH0pO1xuXG4gICAgICAoMCwgX0FzeW5jVXRpbHMubG9vcEFzeW5jKShwYXRobGVzcy5sZW5ndGgsIGZ1bmN0aW9uIChpbmRleCwgbmV4dCwgZG9uZSkge1xuICAgICAgICBnZXRJbmRleFJvdXRlKHBhdGhsZXNzW2luZGV4XSwgbG9jYXRpb24sIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzLCBmdW5jdGlvbiAoZXJyb3IsIGluZGV4Um91dGUpIHtcbiAgICAgICAgICBpZiAoZXJyb3IgfHwgaW5kZXhSb3V0ZSkge1xuICAgICAgICAgICAgdmFyIHJvdXRlcyA9IFtwYXRobGVzc1tpbmRleF1dLmNvbmNhdChBcnJheS5pc0FycmF5KGluZGV4Um91dGUpID8gaW5kZXhSb3V0ZSA6IFtpbmRleFJvdXRlXSk7XG4gICAgICAgICAgICBkb25lKGVycm9yLCByb3V0ZXMpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBuZXh0KCk7XG4gICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgIH0sIGZ1bmN0aW9uIChlcnIsIHJvdXRlcykge1xuICAgICAgICBjYWxsYmFjayhudWxsLCByb3V0ZXMpO1xuICAgICAgfSk7XG4gICAgfSkoKTtcbiAgfSBlbHNlIHtcbiAgICBjYWxsYmFjaygpO1xuICB9XG59XG5cbmZ1bmN0aW9uIGFzc2lnblBhcmFtcyhwYXJhbXMsIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKSB7XG4gIHJldHVybiBwYXJhbU5hbWVzLnJlZHVjZShmdW5jdGlvbiAocGFyYW1zLCBwYXJhbU5hbWUsIGluZGV4KSB7XG4gICAgdmFyIHBhcmFtVmFsdWUgPSBwYXJhbVZhbHVlcyAmJiBwYXJhbVZhbHVlc1tpbmRleF07XG5cbiAgICBpZiAoQXJyYXkuaXNBcnJheShwYXJhbXNbcGFyYW1OYW1lXSkpIHtcbiAgICAgIHBhcmFtc1twYXJhbU5hbWVdLnB1c2gocGFyYW1WYWx1ZSk7XG4gICAgfSBlbHNlIGlmIChwYXJhbU5hbWUgaW4gcGFyYW1zKSB7XG4gICAgICBwYXJhbXNbcGFyYW1OYW1lXSA9IFtwYXJhbXNbcGFyYW1OYW1lXSwgcGFyYW1WYWx1ZV07XG4gICAgfSBlbHNlIHtcbiAgICAgIHBhcmFtc1twYXJhbU5hbWVdID0gcGFyYW1WYWx1ZTtcbiAgICB9XG5cbiAgICByZXR1cm4gcGFyYW1zO1xuICB9LCBwYXJhbXMpO1xufVxuXG5mdW5jdGlvbiBjcmVhdGVQYXJhbXMocGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpIHtcbiAgcmV0dXJuIGFzc2lnblBhcmFtcyh7fSwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpO1xufVxuXG5mdW5jdGlvbiBtYXRjaFJvdXRlRGVlcChyb3V0ZSwgbG9jYXRpb24sIHJlbWFpbmluZ1BhdGhuYW1lLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgY2FsbGJhY2spIHtcbiAgdmFyIHBhdHRlcm4gPSByb3V0ZS5wYXRoIHx8ICcnO1xuXG4gIGlmIChwYXR0ZXJuLmNoYXJBdCgwKSA9PT0gJy8nKSB7XG4gICAgcmVtYWluaW5nUGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZTtcbiAgICBwYXJhbU5hbWVzID0gW107XG4gICAgcGFyYW1WYWx1ZXMgPSBbXTtcbiAgfVxuXG4gIC8vIE9ubHkgdHJ5IHRvIG1hdGNoIHRoZSBwYXRoIGlmIHRoZSByb3V0ZSBhY3R1YWxseSBoYXMgYSBwYXR0ZXJuLCBhbmQgaWZcbiAgLy8gd2UncmUgbm90IGp1c3Qgc2VhcmNoaW5nIGZvciBwb3RlbnRpYWwgbmVzdGVkIGFic29sdXRlIHBhdGhzLlxuICBpZiAocmVtYWluaW5nUGF0aG5hbWUgIT09IG51bGwgJiYgcGF0dGVybikge1xuICAgIHRyeSB7XG4gICAgICB2YXIgbWF0Y2hlZCA9ICgwLCBfUGF0dGVyblV0aWxzLm1hdGNoUGF0dGVybikocGF0dGVybiwgcmVtYWluaW5nUGF0aG5hbWUpO1xuICAgICAgaWYgKG1hdGNoZWQpIHtcbiAgICAgICAgcmVtYWluaW5nUGF0aG5hbWUgPSBtYXRjaGVkLnJlbWFpbmluZ1BhdGhuYW1lO1xuICAgICAgICBwYXJhbU5hbWVzID0gW10uY29uY2F0KHBhcmFtTmFtZXMsIG1hdGNoZWQucGFyYW1OYW1lcyk7XG4gICAgICAgIHBhcmFtVmFsdWVzID0gW10uY29uY2F0KHBhcmFtVmFsdWVzLCBtYXRjaGVkLnBhcmFtVmFsdWVzKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbnVsbDtcbiAgICAgIH1cbiAgICB9IGNhdGNoIChlcnJvcikge1xuICAgICAgY2FsbGJhY2soZXJyb3IpO1xuICAgIH1cblxuICAgIC8vIEJ5IGFzc3VtcHRpb24sIHBhdHRlcm4gaXMgbm9uLWVtcHR5IGhlcmUsIHdoaWNoIGlzIHRoZSBwcmVyZXF1aXNpdGUgZm9yXG4gICAgLy8gYWN0dWFsbHkgdGVybWluYXRpbmcgYSBtYXRjaC5cbiAgICBpZiAocmVtYWluaW5nUGF0aG5hbWUgPT09ICcnKSB7XG4gICAgICB2YXIgX3JldDIgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIHZhciBtYXRjaCA9IHtcbiAgICAgICAgICByb3V0ZXM6IFtyb3V0ZV0sXG4gICAgICAgICAgcGFyYW1zOiBjcmVhdGVQYXJhbXMocGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpXG4gICAgICAgIH07XG5cbiAgICAgICAgZ2V0SW5kZXhSb3V0ZShyb3V0ZSwgbG9jYXRpb24sIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzLCBmdW5jdGlvbiAoZXJyb3IsIGluZGV4Um91dGUpIHtcbiAgICAgICAgICBpZiAoZXJyb3IpIHtcbiAgICAgICAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgaWYgKEFycmF5LmlzQXJyYXkoaW5kZXhSb3V0ZSkpIHtcbiAgICAgICAgICAgICAgdmFyIF9tYXRjaCRyb3V0ZXM7XG5cbiAgICAgICAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoaW5kZXhSb3V0ZS5ldmVyeShmdW5jdGlvbiAocm91dGUpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gIXJvdXRlLnBhdGg7XG4gICAgICAgICAgICAgIH0pLCAnSW5kZXggcm91dGVzIHNob3VsZCBub3QgaGF2ZSBwYXRocycpIDogdm9pZCAwO1xuICAgICAgICAgICAgICAoX21hdGNoJHJvdXRlcyA9IG1hdGNoLnJvdXRlcykucHVzaC5hcHBseShfbWF0Y2gkcm91dGVzLCBpbmRleFJvdXRlKTtcbiAgICAgICAgICAgIH0gZWxzZSBpZiAoaW5kZXhSb3V0ZSkge1xuICAgICAgICAgICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KSghaW5kZXhSb3V0ZS5wYXRoLCAnSW5kZXggcm91dGVzIHNob3VsZCBub3QgaGF2ZSBwYXRocycpIDogdm9pZCAwO1xuICAgICAgICAgICAgICBtYXRjaC5yb3V0ZXMucHVzaChpbmRleFJvdXRlKTtcbiAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgY2FsbGJhY2sobnVsbCwgbWF0Y2gpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG5cbiAgICAgICAgcmV0dXJuIHtcbiAgICAgICAgICB2OiB2b2lkIDBcbiAgICAgICAgfTtcbiAgICAgIH0oKTtcblxuICAgICAgaWYgKCh0eXBlb2YgX3JldDIgPT09ICd1bmRlZmluZWQnID8gJ3VuZGVmaW5lZCcgOiBfdHlwZW9mKF9yZXQyKSkgPT09IFwib2JqZWN0XCIpIHJldHVybiBfcmV0Mi52O1xuICAgIH1cbiAgfVxuXG4gIGlmIChyZW1haW5pbmdQYXRobmFtZSAhPSBudWxsIHx8IHJvdXRlLmNoaWxkUm91dGVzKSB7XG4gICAgLy8gRWl0aGVyIGEpIHRoaXMgcm91dGUgbWF0Y2hlZCBhdCBsZWFzdCBzb21lIG9mIHRoZSBwYXRoIG9yIGIpXG4gICAgLy8gd2UgZG9uJ3QgaGF2ZSB0byBsb2FkIHRoaXMgcm91dGUncyBjaGlsZHJlbiBhc3luY2hyb25vdXNseS4gSW5cbiAgICAvLyBlaXRoZXIgY2FzZSBjb250aW51ZSBjaGVja2luZyBmb3IgbWF0Y2hlcyBpbiB0aGUgc3VidHJlZS5cbiAgICB2YXIgb25DaGlsZFJvdXRlcyA9IGZ1bmN0aW9uIG9uQ2hpbGRSb3V0ZXMoZXJyb3IsIGNoaWxkUm91dGVzKSB7XG4gICAgICBpZiAoZXJyb3IpIHtcbiAgICAgICAgY2FsbGJhY2soZXJyb3IpO1xuICAgICAgfSBlbHNlIGlmIChjaGlsZFJvdXRlcykge1xuICAgICAgICAvLyBDaGVjayB0aGUgY2hpbGQgcm91dGVzIHRvIHNlZSBpZiBhbnkgb2YgdGhlbSBtYXRjaC5cbiAgICAgICAgbWF0Y2hSb3V0ZXMoY2hpbGRSb3V0ZXMsIGxvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIG1hdGNoKSB7XG4gICAgICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgICAgICBjYWxsYmFjayhlcnJvcik7XG4gICAgICAgICAgfSBlbHNlIGlmIChtYXRjaCkge1xuICAgICAgICAgICAgLy8gQSBjaGlsZCByb3V0ZSBtYXRjaGVkISBBdWdtZW50IHRoZSBtYXRjaCBhbmQgcGFzcyBpdCB1cCB0aGUgc3RhY2suXG4gICAgICAgICAgICBtYXRjaC5yb3V0ZXMudW5zaGlmdChyb3V0ZSk7XG4gICAgICAgICAgICBjYWxsYmFjayhudWxsLCBtYXRjaCk7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGNhbGxiYWNrKCk7XG4gICAgICAgICAgfVxuICAgICAgICB9LCByZW1haW5pbmdQYXRobmFtZSwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgY2FsbGJhY2soKTtcbiAgICAgIH1cbiAgICB9O1xuXG4gICAgdmFyIHJlc3VsdCA9IGdldENoaWxkUm91dGVzKHJvdXRlLCBsb2NhdGlvbiwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMsIG9uQ2hpbGRSb3V0ZXMpO1xuICAgIGlmIChyZXN1bHQpIHtcbiAgICAgIG9uQ2hpbGRSb3V0ZXMuYXBwbHkodW5kZWZpbmVkLCByZXN1bHQpO1xuICAgIH1cbiAgfSBlbHNlIHtcbiAgICBjYWxsYmFjaygpO1xuICB9XG59XG5cbi8qKlxuICogQXN5bmNocm9ub3VzbHkgbWF0Y2hlcyB0aGUgZ2l2ZW4gbG9jYXRpb24gdG8gYSBzZXQgb2Ygcm91dGVzIGFuZCBjYWxsc1xuICogY2FsbGJhY2soZXJyb3IsIHN0YXRlKSB3aGVuIGZpbmlzaGVkLiBUaGUgc3RhdGUgb2JqZWN0IHdpbGwgaGF2ZSB0aGVcbiAqIGZvbGxvd2luZyBwcm9wZXJ0aWVzOlxuICpcbiAqIC0gcm91dGVzICAgICAgIEFuIGFycmF5IG9mIHJvdXRlcyB0aGF0IG1hdGNoZWQsIGluIGhpZXJhcmNoaWNhbCBvcmRlclxuICogLSBwYXJhbXMgICAgICAgQW4gb2JqZWN0IG9mIFVSTCBwYXJhbWV0ZXJzXG4gKlxuICogTm90ZTogVGhpcyBvcGVyYXRpb24gbWF5IGZpbmlzaCBzeW5jaHJvbm91c2x5IGlmIG5vIHJvdXRlcyBoYXZlIGFuXG4gKiBhc3luY2hyb25vdXMgZ2V0Q2hpbGRSb3V0ZXMgbWV0aG9kLlxuICovXG5mdW5jdGlvbiBtYXRjaFJvdXRlcyhyb3V0ZXMsIGxvY2F0aW9uLCBjYWxsYmFjaywgcmVtYWluaW5nUGF0aG5hbWUpIHtcbiAgdmFyIHBhcmFtTmFtZXMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDQgfHwgYXJndW1lbnRzWzRdID09PSB1bmRlZmluZWQgPyBbXSA6IGFyZ3VtZW50c1s0XTtcbiAgdmFyIHBhcmFtVmFsdWVzID0gYXJndW1lbnRzLmxlbmd0aCA8PSA1IHx8IGFyZ3VtZW50c1s1XSA9PT0gdW5kZWZpbmVkID8gW10gOiBhcmd1bWVudHNbNV07XG5cbiAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lID09PSB1bmRlZmluZWQpIHtcbiAgICAvLyBUT0RPOiBUaGlzIGlzIGEgbGl0dGxlIGJpdCB1Z2x5LCBidXQgaXQgd29ya3MgYXJvdW5kIGEgcXVpcmsgaW4gaGlzdG9yeVxuICAgIC8vIHRoYXQgc3RyaXBzIHRoZSBsZWFkaW5nIHNsYXNoIGZyb20gcGF0aG5hbWVzIHdoZW4gdXNpbmcgYmFzZW5hbWVzIHdpdGhcbiAgICAvLyB0cmFpbGluZyBzbGFzaGVzLlxuICAgIGlmIChsb2NhdGlvbi5wYXRobmFtZS5jaGFyQXQoMCkgIT09ICcvJykge1xuICAgICAgbG9jYXRpb24gPSBfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIHtcbiAgICAgICAgcGF0aG5hbWU6ICcvJyArIGxvY2F0aW9uLnBhdGhuYW1lXG4gICAgICB9KTtcbiAgICB9XG4gICAgcmVtYWluaW5nUGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZTtcbiAgfVxuXG4gICgwLCBfQXN5bmNVdGlscy5sb29wQXN5bmMpKHJvdXRlcy5sZW5ndGgsIGZ1bmN0aW9uIChpbmRleCwgbmV4dCwgZG9uZSkge1xuICAgIG1hdGNoUm91dGVEZWVwKHJvdXRlc1tpbmRleF0sIGxvY2F0aW9uLCByZW1haW5pbmdQYXRobmFtZSwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMsIGZ1bmN0aW9uIChlcnJvciwgbWF0Y2gpIHtcbiAgICAgIGlmIChlcnJvciB8fCBtYXRjaCkge1xuICAgICAgICBkb25lKGVycm9yLCBtYXRjaCk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBuZXh0KCk7XG4gICAgICB9XG4gICAgfSk7XG4gIH0sIGNhbGxiYWNrKTtcbn1cbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuZGVmYXVsdCA9IHJvdXRlcldhcm5pbmc7XG5leHBvcnRzLl9yZXNldFdhcm5lZCA9IF9yZXNldFdhcm5lZDtcblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciB3YXJuZWQgPSB7fTtcblxuZnVuY3Rpb24gcm91dGVyV2FybmluZyhmYWxzZVRvV2FybiwgbWVzc2FnZSkge1xuICAvLyBPbmx5IGlzc3VlIGRlcHJlY2F0aW9uIHdhcm5pbmdzIG9uY2UuXG4gIGlmIChtZXNzYWdlLmluZGV4T2YoJ2RlcHJlY2F0ZWQnKSAhPT0gLTEpIHtcbiAgICBpZiAod2FybmVkW21lc3NhZ2VdKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgd2FybmVkW21lc3NhZ2VdID0gdHJ1ZTtcbiAgfVxuXG4gIG1lc3NhZ2UgPSAnW3JlYWN0LXJvdXRlcl0gJyArIG1lc3NhZ2U7XG5cbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIGFyZ3MgPSBBcnJheShfbGVuID4gMiA/IF9sZW4gLSAyIDogMCksIF9rZXkgPSAyOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgYXJnc1tfa2V5IC0gMl0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICBfd2FybmluZzIuZGVmYXVsdC5hcHBseSh1bmRlZmluZWQsIFtmYWxzZVRvV2FybiwgbWVzc2FnZV0uY29uY2F0KGFyZ3MpKTtcbn1cblxuZnVuY3Rpb24gX3Jlc2V0V2FybmVkKCkge1xuICB3YXJuZWQgPSB7fTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmRlZmF1bHQgPSB1c2VSb3V0ZXJIaXN0b3J5O1xuXG52YXIgX3VzZVF1ZXJpZXMgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi91c2VRdWVyaWVzJyk7XG5cbnZhciBfdXNlUXVlcmllczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF91c2VRdWVyaWVzKTtcblxudmFyIF91c2VCYXNlbmFtZSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL3VzZUJhc2VuYW1lJyk7XG5cbnZhciBfdXNlQmFzZW5hbWUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlQmFzZW5hbWUpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiB1c2VSb3V0ZXJIaXN0b3J5KGNyZWF0ZUhpc3RvcnkpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uIChvcHRpb25zKSB7XG4gICAgdmFyIGhpc3RvcnkgPSAoMCwgX3VzZVF1ZXJpZXMyLmRlZmF1bHQpKCgwLCBfdXNlQmFzZW5hbWUyLmRlZmF1bHQpKGNyZWF0ZUhpc3RvcnkpKShvcHRpb25zKTtcbiAgICBoaXN0b3J5Ll9fdjJfY29tcGF0aWJsZV9fID0gdHJ1ZTtcbiAgICByZXR1cm4gaGlzdG9yeTtcbiAgfTtcbn1cbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxudmFyIF91c2VRdWVyaWVzID0gcmVxdWlyZSgnaGlzdG9yeS9saWIvdXNlUXVlcmllcycpO1xuXG52YXIgX3VzZVF1ZXJpZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUXVlcmllcyk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIgPSByZXF1aXJlKCcuL2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyJyk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBfb2JqZWN0V2l0aG91dFByb3BlcnRpZXMob2JqLCBrZXlzKSB7IHZhciB0YXJnZXQgPSB7fTsgZm9yICh2YXIgaSBpbiBvYmopIHsgaWYgKGtleXMuaW5kZXhPZihpKSA+PSAwKSBjb250aW51ZTsgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqLCBpKSkgY29udGludWU7IHRhcmdldFtpXSA9IG9ialtpXTsgfSByZXR1cm4gdGFyZ2V0OyB9XG5cbi8qKlxuICogUmV0dXJucyBhIG5ldyBjcmVhdGVIaXN0b3J5IGZ1bmN0aW9uIHRoYXQgbWF5IGJlIHVzZWQgdG8gY3JlYXRlXG4gKiBoaXN0b3J5IG9iamVjdHMgdGhhdCBrbm93IGFib3V0IHJvdXRpbmcuXG4gKlxuICogRW5oYW5jZXMgaGlzdG9yeSBvYmplY3RzIHdpdGggdGhlIGZvbGxvd2luZyBtZXRob2RzOlxuICpcbiAqIC0gbGlzdGVuKChlcnJvciwgbmV4dFN0YXRlKSA9PiB7fSlcbiAqIC0gbGlzdGVuQmVmb3JlTGVhdmluZ1JvdXRlKHJvdXRlLCAobmV4dExvY2F0aW9uKSA9PiB7fSlcbiAqIC0gbWF0Y2gobG9jYXRpb24sIChlcnJvciwgcmVkaXJlY3RMb2NhdGlvbiwgbmV4dFN0YXRlKSA9PiB7fSlcbiAqIC0gaXNBY3RpdmUocGF0aG5hbWUsIHF1ZXJ5LCBpbmRleE9ubHk9ZmFsc2UpXG4gKi9cbmZ1bmN0aW9uIHVzZVJvdXRlcyhjcmVhdGVIaXN0b3J5KSB7XG4gIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnYHVzZVJvdXRlc2AgaXMgZGVwcmVjYXRlZC4gUGxlYXNlIHVzZSBgY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXJgIGluc3RlYWQuJykgOiB2b2lkIDA7XG5cbiAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICB2YXIgX3JlZiA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuXG4gICAgdmFyIHJvdXRlcyA9IF9yZWYucm91dGVzO1xuXG4gICAgdmFyIG9wdGlvbnMgPSBfb2JqZWN0V2l0aG91dFByb3BlcnRpZXMoX3JlZiwgWydyb3V0ZXMnXSk7XG5cbiAgICB2YXIgaGlzdG9yeSA9ICgwLCBfdXNlUXVlcmllczIuZGVmYXVsdCkoY3JlYXRlSGlzdG9yeSkob3B0aW9ucyk7XG4gICAgdmFyIHRyYW5zaXRpb25NYW5hZ2VyID0gKDAsIF9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcjIuZGVmYXVsdCkoaGlzdG9yeSwgcm91dGVzKTtcbiAgICByZXR1cm4gX2V4dGVuZHMoe30sIGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyKTtcbiAgfTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gdXNlUm91dGVzO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5leHBvcnRzLmRlZmF1bHQgPSB3aXRoUm91dGVyO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfaG9pc3ROb25SZWFjdFN0YXRpY3MgPSByZXF1aXJlKCdob2lzdC1ub24tcmVhY3Qtc3RhdGljcycpO1xuXG52YXIgX2hvaXN0Tm9uUmVhY3RTdGF0aWNzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2hvaXN0Tm9uUmVhY3RTdGF0aWNzKTtcblxudmFyIF9Qcm9wVHlwZXMgPSByZXF1aXJlKCcuL1Byb3BUeXBlcycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBnZXREaXNwbGF5TmFtZShXcmFwcGVkQ29tcG9uZW50KSB7XG4gIHJldHVybiBXcmFwcGVkQ29tcG9uZW50LmRpc3BsYXlOYW1lIHx8IFdyYXBwZWRDb21wb25lbnQubmFtZSB8fCAnQ29tcG9uZW50Jztcbn1cblxuZnVuY3Rpb24gd2l0aFJvdXRlcihXcmFwcGVkQ29tcG9uZW50KSB7XG4gIHZhciBXaXRoUm91dGVyID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgICBkaXNwbGF5TmFtZTogJ1dpdGhSb3V0ZXInLFxuXG4gICAgY29udGV4dFR5cGVzOiB7IHJvdXRlcjogX1Byb3BUeXBlcy5yb3V0ZXJTaGFwZSB9LFxuICAgIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgICAgcmV0dXJuIF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50KFdyYXBwZWRDb21wb25lbnQsIF9leHRlbmRzKHt9LCB0aGlzLnByb3BzLCB7IHJvdXRlcjogdGhpcy5jb250ZXh0LnJvdXRlciB9KSk7XG4gICAgfVxuICB9KTtcblxuICBXaXRoUm91dGVyLmRpc3BsYXlOYW1lID0gJ3dpdGhSb3V0ZXIoJyArIGdldERpc3BsYXlOYW1lKFdyYXBwZWRDb21wb25lbnQpICsgJyknO1xuICBXaXRoUm91dGVyLldyYXBwZWRDb21wb25lbnQgPSBXcmFwcGVkQ29tcG9uZW50O1xuXG4gIHJldHVybiAoMCwgX2hvaXN0Tm9uUmVhY3RTdGF0aWNzMi5kZWZhdWx0KShXaXRoUm91dGVyLCBXcmFwcGVkQ29tcG9uZW50KTtcbn1cbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZnVuY3Rpb24gdGh1bmtNaWRkbGV3YXJlKF9yZWYpIHtcbiAgdmFyIGRpc3BhdGNoID0gX3JlZi5kaXNwYXRjaDtcbiAgdmFyIGdldFN0YXRlID0gX3JlZi5nZXRTdGF0ZTtcblxuICByZXR1cm4gZnVuY3Rpb24gKG5leHQpIHtcbiAgICByZXR1cm4gZnVuY3Rpb24gKGFjdGlvbikge1xuICAgICAgcmV0dXJuIHR5cGVvZiBhY3Rpb24gPT09ICdmdW5jdGlvbicgPyBhY3Rpb24oZGlzcGF0Y2gsIGdldFN0YXRlKSA6IG5leHQoYWN0aW9uKTtcbiAgICB9O1xuICB9O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IHRodW5rTWlkZGxld2FyZTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gYXBwbHlNaWRkbGV3YXJlO1xuXG52YXIgX2NvbXBvc2UgPSByZXF1aXJlKCcuL2NvbXBvc2UnKTtcblxudmFyIF9jb21wb3NlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NvbXBvc2UpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuLyoqXG4gKiBDcmVhdGVzIGEgc3RvcmUgZW5oYW5jZXIgdGhhdCBhcHBsaWVzIG1pZGRsZXdhcmUgdG8gdGhlIGRpc3BhdGNoIG1ldGhvZFxuICogb2YgdGhlIFJlZHV4IHN0b3JlLiBUaGlzIGlzIGhhbmR5IGZvciBhIHZhcmlldHkgb2YgdGFza3MsIHN1Y2ggYXMgZXhwcmVzc2luZ1xuICogYXN5bmNocm9ub3VzIGFjdGlvbnMgaW4gYSBjb25jaXNlIG1hbm5lciwgb3IgbG9nZ2luZyBldmVyeSBhY3Rpb24gcGF5bG9hZC5cbiAqXG4gKiBTZWUgYHJlZHV4LXRodW5rYCBwYWNrYWdlIGFzIGFuIGV4YW1wbGUgb2YgdGhlIFJlZHV4IG1pZGRsZXdhcmUuXG4gKlxuICogQmVjYXVzZSBtaWRkbGV3YXJlIGlzIHBvdGVudGlhbGx5IGFzeW5jaHJvbm91cywgdGhpcyBzaG91bGQgYmUgdGhlIGZpcnN0XG4gKiBzdG9yZSBlbmhhbmNlciBpbiB0aGUgY29tcG9zaXRpb24gY2hhaW4uXG4gKlxuICogTm90ZSB0aGF0IGVhY2ggbWlkZGxld2FyZSB3aWxsIGJlIGdpdmVuIHRoZSBgZGlzcGF0Y2hgIGFuZCBgZ2V0U3RhdGVgIGZ1bmN0aW9uc1xuICogYXMgbmFtZWQgYXJndW1lbnRzLlxuICpcbiAqIEBwYXJhbSB7Li4uRnVuY3Rpb259IG1pZGRsZXdhcmVzIFRoZSBtaWRkbGV3YXJlIGNoYWluIHRvIGJlIGFwcGxpZWQuXG4gKiBAcmV0dXJucyB7RnVuY3Rpb259IEEgc3RvcmUgZW5oYW5jZXIgYXBwbHlpbmcgdGhlIG1pZGRsZXdhcmUuXG4gKi9cbmZ1bmN0aW9uIGFwcGx5TWlkZGxld2FyZSgpIHtcbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIG1pZGRsZXdhcmVzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgbWlkZGxld2FyZXNbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICByZXR1cm4gZnVuY3Rpb24gKGNyZWF0ZVN0b3JlKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChyZWR1Y2VyLCBpbml0aWFsU3RhdGUsIGVuaGFuY2VyKSB7XG4gICAgICB2YXIgc3RvcmUgPSBjcmVhdGVTdG9yZShyZWR1Y2VyLCBpbml0aWFsU3RhdGUsIGVuaGFuY2VyKTtcbiAgICAgIHZhciBfZGlzcGF0Y2ggPSBzdG9yZS5kaXNwYXRjaDtcbiAgICAgIHZhciBjaGFpbiA9IFtdO1xuXG4gICAgICB2YXIgbWlkZGxld2FyZUFQSSA9IHtcbiAgICAgICAgZ2V0U3RhdGU6IHN0b3JlLmdldFN0YXRlLFxuICAgICAgICBkaXNwYXRjaDogZnVuY3Rpb24gZGlzcGF0Y2goYWN0aW9uKSB7XG4gICAgICAgICAgcmV0dXJuIF9kaXNwYXRjaChhY3Rpb24pO1xuICAgICAgICB9XG4gICAgICB9O1xuICAgICAgY2hhaW4gPSBtaWRkbGV3YXJlcy5tYXAoZnVuY3Rpb24gKG1pZGRsZXdhcmUpIHtcbiAgICAgICAgcmV0dXJuIG1pZGRsZXdhcmUobWlkZGxld2FyZUFQSSk7XG4gICAgICB9KTtcbiAgICAgIF9kaXNwYXRjaCA9IF9jb21wb3NlMltcImRlZmF1bHRcIl0uYXBwbHkodW5kZWZpbmVkLCBjaGFpbikoc3RvcmUuZGlzcGF0Y2gpO1xuXG4gICAgICByZXR1cm4gX2V4dGVuZHMoe30sIHN0b3JlLCB7XG4gICAgICAgIGRpc3BhdGNoOiBfZGlzcGF0Y2hcbiAgICAgIH0pO1xuICAgIH07XG4gIH07XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBiaW5kQWN0aW9uQ3JlYXRvcnM7XG5mdW5jdGlvbiBiaW5kQWN0aW9uQ3JlYXRvcihhY3Rpb25DcmVhdG9yLCBkaXNwYXRjaCkge1xuICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgIHJldHVybiBkaXNwYXRjaChhY3Rpb25DcmVhdG9yLmFwcGx5KHVuZGVmaW5lZCwgYXJndW1lbnRzKSk7XG4gIH07XG59XG5cbi8qKlxuICogVHVybnMgYW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgYWN0aW9uIGNyZWF0b3JzLCBpbnRvIGFuIG9iamVjdCB3aXRoIHRoZVxuICogc2FtZSBrZXlzLCBidXQgd2l0aCBldmVyeSBmdW5jdGlvbiB3cmFwcGVkIGludG8gYSBgZGlzcGF0Y2hgIGNhbGwgc28gdGhleVxuICogbWF5IGJlIGludm9rZWQgZGlyZWN0bHkuIFRoaXMgaXMganVzdCBhIGNvbnZlbmllbmNlIG1ldGhvZCwgYXMgeW91IGNhbiBjYWxsXG4gKiBgc3RvcmUuZGlzcGF0Y2goTXlBY3Rpb25DcmVhdG9ycy5kb1NvbWV0aGluZygpKWAgeW91cnNlbGYganVzdCBmaW5lLlxuICpcbiAqIEZvciBjb252ZW5pZW5jZSwgeW91IGNhbiBhbHNvIHBhc3MgYSBzaW5nbGUgZnVuY3Rpb24gYXMgdGhlIGZpcnN0IGFyZ3VtZW50LFxuICogYW5kIGdldCBhIGZ1bmN0aW9uIGluIHJldHVybi5cbiAqXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufE9iamVjdH0gYWN0aW9uQ3JlYXRvcnMgQW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgYWN0aW9uXG4gKiBjcmVhdG9yIGZ1bmN0aW9ucy4gT25lIGhhbmR5IHdheSB0byBvYnRhaW4gaXQgaXMgdG8gdXNlIEVTNiBgaW1wb3J0ICogYXNgXG4gKiBzeW50YXguIFlvdSBtYXkgYWxzbyBwYXNzIGEgc2luZ2xlIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGRpc3BhdGNoIFRoZSBgZGlzcGF0Y2hgIGZ1bmN0aW9uIGF2YWlsYWJsZSBvbiB5b3VyIFJlZHV4XG4gKiBzdG9yZS5cbiAqXG4gKiBAcmV0dXJucyB7RnVuY3Rpb258T2JqZWN0fSBUaGUgb2JqZWN0IG1pbWlja2luZyB0aGUgb3JpZ2luYWwgb2JqZWN0LCBidXQgd2l0aFxuICogZXZlcnkgYWN0aW9uIGNyZWF0b3Igd3JhcHBlZCBpbnRvIHRoZSBgZGlzcGF0Y2hgIGNhbGwuIElmIHlvdSBwYXNzZWQgYVxuICogZnVuY3Rpb24gYXMgYGFjdGlvbkNyZWF0b3JzYCwgdGhlIHJldHVybiB2YWx1ZSB3aWxsIGFsc28gYmUgYSBzaW5nbGVcbiAqIGZ1bmN0aW9uLlxuICovXG5mdW5jdGlvbiBiaW5kQWN0aW9uQ3JlYXRvcnMoYWN0aW9uQ3JlYXRvcnMsIGRpc3BhdGNoKSB7XG4gIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvcnMgPT09ICdmdW5jdGlvbicpIHtcbiAgICByZXR1cm4gYmluZEFjdGlvbkNyZWF0b3IoYWN0aW9uQ3JlYXRvcnMsIGRpc3BhdGNoKTtcbiAgfVxuXG4gIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvcnMgIT09ICdvYmplY3QnIHx8IGFjdGlvbkNyZWF0b3JzID09PSBudWxsKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdiaW5kQWN0aW9uQ3JlYXRvcnMgZXhwZWN0ZWQgYW4gb2JqZWN0IG9yIGEgZnVuY3Rpb24sIGluc3RlYWQgcmVjZWl2ZWQgJyArIChhY3Rpb25DcmVhdG9ycyA9PT0gbnVsbCA/ICdudWxsJyA6IHR5cGVvZiBhY3Rpb25DcmVhdG9ycykgKyAnLiAnICsgJ0RpZCB5b3Ugd3JpdGUgXCJpbXBvcnQgQWN0aW9uQ3JlYXRvcnMgZnJvbVwiIGluc3RlYWQgb2YgXCJpbXBvcnQgKiBhcyBBY3Rpb25DcmVhdG9ycyBmcm9tXCI/Jyk7XG4gIH1cblxuICB2YXIga2V5cyA9IE9iamVjdC5rZXlzKGFjdGlvbkNyZWF0b3JzKTtcbiAgdmFyIGJvdW5kQWN0aW9uQ3JlYXRvcnMgPSB7fTtcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBrZXlzLmxlbmd0aDsgaSsrKSB7XG4gICAgdmFyIGtleSA9IGtleXNbaV07XG4gICAgdmFyIGFjdGlvbkNyZWF0b3IgPSBhY3Rpb25DcmVhdG9yc1trZXldO1xuICAgIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvciA9PT0gJ2Z1bmN0aW9uJykge1xuICAgICAgYm91bmRBY3Rpb25DcmVhdG9yc1trZXldID0gYmluZEFjdGlvbkNyZWF0b3IoYWN0aW9uQ3JlYXRvciwgZGlzcGF0Y2gpO1xuICAgIH1cbiAgfVxuICByZXR1cm4gYm91bmRBY3Rpb25DcmVhdG9ycztcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGNvbWJpbmVSZWR1Y2VycztcblxudmFyIF9jcmVhdGVTdG9yZSA9IHJlcXVpcmUoJy4vY3JlYXRlU3RvcmUnKTtcblxudmFyIF9pc1BsYWluT2JqZWN0ID0gcmVxdWlyZSgnbG9kYXNoL2lzUGxhaW5PYmplY3QnKTtcblxudmFyIF9pc1BsYWluT2JqZWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2lzUGxhaW5PYmplY3QpO1xuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCcuL3V0aWxzL3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuZnVuY3Rpb24gZ2V0VW5kZWZpbmVkU3RhdGVFcnJvck1lc3NhZ2Uoa2V5LCBhY3Rpb24pIHtcbiAgdmFyIGFjdGlvblR5cGUgPSBhY3Rpb24gJiYgYWN0aW9uLnR5cGU7XG4gIHZhciBhY3Rpb25OYW1lID0gYWN0aW9uVHlwZSAmJiAnXCInICsgYWN0aW9uVHlwZS50b1N0cmluZygpICsgJ1wiJyB8fCAnYW4gYWN0aW9uJztcblxuICByZXR1cm4gJ0dpdmVuIGFjdGlvbiAnICsgYWN0aW9uTmFtZSArICcsIHJlZHVjZXIgXCInICsga2V5ICsgJ1wiIHJldHVybmVkIHVuZGVmaW5lZC4gJyArICdUbyBpZ25vcmUgYW4gYWN0aW9uLCB5b3UgbXVzdCBleHBsaWNpdGx5IHJldHVybiB0aGUgcHJldmlvdXMgc3RhdGUuJztcbn1cblxuZnVuY3Rpb24gZ2V0VW5leHBlY3RlZFN0YXRlU2hhcGVXYXJuaW5nTWVzc2FnZShpbnB1dFN0YXRlLCByZWR1Y2VycywgYWN0aW9uKSB7XG4gIHZhciByZWR1Y2VyS2V5cyA9IE9iamVjdC5rZXlzKHJlZHVjZXJzKTtcbiAgdmFyIGFyZ3VtZW50TmFtZSA9IGFjdGlvbiAmJiBhY3Rpb24udHlwZSA9PT0gX2NyZWF0ZVN0b3JlLkFjdGlvblR5cGVzLklOSVQgPyAnaW5pdGlhbFN0YXRlIGFyZ3VtZW50IHBhc3NlZCB0byBjcmVhdGVTdG9yZScgOiAncHJldmlvdXMgc3RhdGUgcmVjZWl2ZWQgYnkgdGhlIHJlZHVjZXInO1xuXG4gIGlmIChyZWR1Y2VyS2V5cy5sZW5ndGggPT09IDApIHtcbiAgICByZXR1cm4gJ1N0b3JlIGRvZXMgbm90IGhhdmUgYSB2YWxpZCByZWR1Y2VyLiBNYWtlIHN1cmUgdGhlIGFyZ3VtZW50IHBhc3NlZCAnICsgJ3RvIGNvbWJpbmVSZWR1Y2VycyBpcyBhbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGFyZSByZWR1Y2Vycy4nO1xuICB9XG5cbiAgaWYgKCEoMCwgX2lzUGxhaW5PYmplY3QyW1wiZGVmYXVsdFwiXSkoaW5wdXRTdGF0ZSkpIHtcbiAgICByZXR1cm4gJ1RoZSAnICsgYXJndW1lbnROYW1lICsgJyBoYXMgdW5leHBlY3RlZCB0eXBlIG9mIFwiJyArIHt9LnRvU3RyaW5nLmNhbGwoaW5wdXRTdGF0ZSkubWF0Y2goL1xccyhbYS16fEEtWl0rKS8pWzFdICsgJ1wiLiBFeHBlY3RlZCBhcmd1bWVudCB0byBiZSBhbiBvYmplY3Qgd2l0aCB0aGUgZm9sbG93aW5nICcgKyAoJ2tleXM6IFwiJyArIHJlZHVjZXJLZXlzLmpvaW4oJ1wiLCBcIicpICsgJ1wiJyk7XG4gIH1cblxuICB2YXIgdW5leHBlY3RlZEtleXMgPSBPYmplY3Qua2V5cyhpbnB1dFN0YXRlKS5maWx0ZXIoZnVuY3Rpb24gKGtleSkge1xuICAgIHJldHVybiAhcmVkdWNlcnMuaGFzT3duUHJvcGVydHkoa2V5KTtcbiAgfSk7XG5cbiAgaWYgKHVuZXhwZWN0ZWRLZXlzLmxlbmd0aCA+IDApIHtcbiAgICByZXR1cm4gJ1VuZXhwZWN0ZWQgJyArICh1bmV4cGVjdGVkS2V5cy5sZW5ndGggPiAxID8gJ2tleXMnIDogJ2tleScpICsgJyAnICsgKCdcIicgKyB1bmV4cGVjdGVkS2V5cy5qb2luKCdcIiwgXCInKSArICdcIiBmb3VuZCBpbiAnICsgYXJndW1lbnROYW1lICsgJy4gJykgKyAnRXhwZWN0ZWQgdG8gZmluZCBvbmUgb2YgdGhlIGtub3duIHJlZHVjZXIga2V5cyBpbnN0ZWFkOiAnICsgKCdcIicgKyByZWR1Y2VyS2V5cy5qb2luKCdcIiwgXCInKSArICdcIi4gVW5leHBlY3RlZCBrZXlzIHdpbGwgYmUgaWdub3JlZC4nKTtcbiAgfVxufVxuXG5mdW5jdGlvbiBhc3NlcnRSZWR1Y2VyU2FuaXR5KHJlZHVjZXJzKSB7XG4gIE9iamVjdC5rZXlzKHJlZHVjZXJzKS5mb3JFYWNoKGZ1bmN0aW9uIChrZXkpIHtcbiAgICB2YXIgcmVkdWNlciA9IHJlZHVjZXJzW2tleV07XG4gICAgdmFyIGluaXRpYWxTdGF0ZSA9IHJlZHVjZXIodW5kZWZpbmVkLCB7IHR5cGU6IF9jcmVhdGVTdG9yZS5BY3Rpb25UeXBlcy5JTklUIH0pO1xuXG4gICAgaWYgKHR5cGVvZiBpbml0aWFsU3RhdGUgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1JlZHVjZXIgXCInICsga2V5ICsgJ1wiIHJldHVybmVkIHVuZGVmaW5lZCBkdXJpbmcgaW5pdGlhbGl6YXRpb24uICcgKyAnSWYgdGhlIHN0YXRlIHBhc3NlZCB0byB0aGUgcmVkdWNlciBpcyB1bmRlZmluZWQsIHlvdSBtdXN0ICcgKyAnZXhwbGljaXRseSByZXR1cm4gdGhlIGluaXRpYWwgc3RhdGUuIFRoZSBpbml0aWFsIHN0YXRlIG1heSAnICsgJ25vdCBiZSB1bmRlZmluZWQuJyk7XG4gICAgfVxuXG4gICAgdmFyIHR5cGUgPSAnQEByZWR1eC9QUk9CRV9VTktOT1dOX0FDVElPTl8nICsgTWF0aC5yYW5kb20oKS50b1N0cmluZygzNikuc3Vic3RyaW5nKDcpLnNwbGl0KCcnKS5qb2luKCcuJyk7XG4gICAgaWYgKHR5cGVvZiByZWR1Y2VyKHVuZGVmaW5lZCwgeyB0eXBlOiB0eXBlIH0pID09PSAndW5kZWZpbmVkJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdSZWR1Y2VyIFwiJyArIGtleSArICdcIiByZXR1cm5lZCB1bmRlZmluZWQgd2hlbiBwcm9iZWQgd2l0aCBhIHJhbmRvbSB0eXBlLiAnICsgKCdEb25cXCd0IHRyeSB0byBoYW5kbGUgJyArIF9jcmVhdGVTdG9yZS5BY3Rpb25UeXBlcy5JTklUICsgJyBvciBvdGhlciBhY3Rpb25zIGluIFwicmVkdXgvKlwiICcpICsgJ25hbWVzcGFjZS4gVGhleSBhcmUgY29uc2lkZXJlZCBwcml2YXRlLiBJbnN0ZWFkLCB5b3UgbXVzdCByZXR1cm4gdGhlICcgKyAnY3VycmVudCBzdGF0ZSBmb3IgYW55IHVua25vd24gYWN0aW9ucywgdW5sZXNzIGl0IGlzIHVuZGVmaW5lZCwgJyArICdpbiB3aGljaCBjYXNlIHlvdSBtdXN0IHJldHVybiB0aGUgaW5pdGlhbCBzdGF0ZSwgcmVnYXJkbGVzcyBvZiB0aGUgJyArICdhY3Rpb24gdHlwZS4gVGhlIGluaXRpYWwgc3RhdGUgbWF5IG5vdCBiZSB1bmRlZmluZWQuJyk7XG4gICAgfVxuICB9KTtcbn1cblxuLyoqXG4gKiBUdXJucyBhbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGFyZSBkaWZmZXJlbnQgcmVkdWNlciBmdW5jdGlvbnMsIGludG8gYSBzaW5nbGVcbiAqIHJlZHVjZXIgZnVuY3Rpb24uIEl0IHdpbGwgY2FsbCBldmVyeSBjaGlsZCByZWR1Y2VyLCBhbmQgZ2F0aGVyIHRoZWlyIHJlc3VsdHNcbiAqIGludG8gYSBzaW5nbGUgc3RhdGUgb2JqZWN0LCB3aG9zZSBrZXlzIGNvcnJlc3BvbmQgdG8gdGhlIGtleXMgb2YgdGhlIHBhc3NlZFxuICogcmVkdWNlciBmdW5jdGlvbnMuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHJlZHVjZXJzIEFuIG9iamVjdCB3aG9zZSB2YWx1ZXMgY29ycmVzcG9uZCB0byBkaWZmZXJlbnRcbiAqIHJlZHVjZXIgZnVuY3Rpb25zIHRoYXQgbmVlZCB0byBiZSBjb21iaW5lZCBpbnRvIG9uZS4gT25lIGhhbmR5IHdheSB0byBvYnRhaW5cbiAqIGl0IGlzIHRvIHVzZSBFUzYgYGltcG9ydCAqIGFzIHJlZHVjZXJzYCBzeW50YXguIFRoZSByZWR1Y2VycyBtYXkgbmV2ZXIgcmV0dXJuXG4gKiB1bmRlZmluZWQgZm9yIGFueSBhY3Rpb24uIEluc3RlYWQsIHRoZXkgc2hvdWxkIHJldHVybiB0aGVpciBpbml0aWFsIHN0YXRlXG4gKiBpZiB0aGUgc3RhdGUgcGFzc2VkIHRvIHRoZW0gd2FzIHVuZGVmaW5lZCwgYW5kIHRoZSBjdXJyZW50IHN0YXRlIGZvciBhbnlcbiAqIHVucmVjb2duaXplZCBhY3Rpb24uXG4gKlxuICogQHJldHVybnMge0Z1bmN0aW9ufSBBIHJlZHVjZXIgZnVuY3Rpb24gdGhhdCBpbnZva2VzIGV2ZXJ5IHJlZHVjZXIgaW5zaWRlIHRoZVxuICogcGFzc2VkIG9iamVjdCwgYW5kIGJ1aWxkcyBhIHN0YXRlIG9iamVjdCB3aXRoIHRoZSBzYW1lIHNoYXBlLlxuICovXG5mdW5jdGlvbiBjb21iaW5lUmVkdWNlcnMocmVkdWNlcnMpIHtcbiAgdmFyIHJlZHVjZXJLZXlzID0gT2JqZWN0LmtleXMocmVkdWNlcnMpO1xuICB2YXIgZmluYWxSZWR1Y2VycyA9IHt9O1xuICBmb3IgKHZhciBpID0gMDsgaSA8IHJlZHVjZXJLZXlzLmxlbmd0aDsgaSsrKSB7XG4gICAgdmFyIGtleSA9IHJlZHVjZXJLZXlzW2ldO1xuICAgIGlmICh0eXBlb2YgcmVkdWNlcnNba2V5XSA9PT0gJ2Z1bmN0aW9uJykge1xuICAgICAgZmluYWxSZWR1Y2Vyc1trZXldID0gcmVkdWNlcnNba2V5XTtcbiAgICB9XG4gIH1cbiAgdmFyIGZpbmFsUmVkdWNlcktleXMgPSBPYmplY3Qua2V5cyhmaW5hbFJlZHVjZXJzKTtcblxuICB2YXIgc2FuaXR5RXJyb3I7XG4gIHRyeSB7XG4gICAgYXNzZXJ0UmVkdWNlclNhbml0eShmaW5hbFJlZHVjZXJzKTtcbiAgfSBjYXRjaCAoZSkge1xuICAgIHNhbml0eUVycm9yID0gZTtcbiAgfVxuXG4gIHJldHVybiBmdW5jdGlvbiBjb21iaW5hdGlvbigpIHtcbiAgICB2YXIgc3RhdGUgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcbiAgICB2YXIgYWN0aW9uID0gYXJndW1lbnRzWzFdO1xuXG4gICAgaWYgKHNhbml0eUVycm9yKSB7XG4gICAgICB0aHJvdyBzYW5pdHlFcnJvcjtcbiAgICB9XG5cbiAgICBpZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgdmFyIHdhcm5pbmdNZXNzYWdlID0gZ2V0VW5leHBlY3RlZFN0YXRlU2hhcGVXYXJuaW5nTWVzc2FnZShzdGF0ZSwgZmluYWxSZWR1Y2VycywgYWN0aW9uKTtcbiAgICAgIGlmICh3YXJuaW5nTWVzc2FnZSkge1xuICAgICAgICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkod2FybmluZ01lc3NhZ2UpO1xuICAgICAgfVxuICAgIH1cblxuICAgIHZhciBoYXNDaGFuZ2VkID0gZmFsc2U7XG4gICAgdmFyIG5leHRTdGF0ZSA9IHt9O1xuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgZmluYWxSZWR1Y2VyS2V5cy5sZW5ndGg7IGkrKykge1xuICAgICAgdmFyIGtleSA9IGZpbmFsUmVkdWNlcktleXNbaV07XG4gICAgICB2YXIgcmVkdWNlciA9IGZpbmFsUmVkdWNlcnNba2V5XTtcbiAgICAgIHZhciBwcmV2aW91c1N0YXRlRm9yS2V5ID0gc3RhdGVba2V5XTtcbiAgICAgIHZhciBuZXh0U3RhdGVGb3JLZXkgPSByZWR1Y2VyKHByZXZpb3VzU3RhdGVGb3JLZXksIGFjdGlvbik7XG4gICAgICBpZiAodHlwZW9mIG5leHRTdGF0ZUZvcktleSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgICAgdmFyIGVycm9yTWVzc2FnZSA9IGdldFVuZGVmaW5lZFN0YXRlRXJyb3JNZXNzYWdlKGtleSwgYWN0aW9uKTtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKGVycm9yTWVzc2FnZSk7XG4gICAgICB9XG4gICAgICBuZXh0U3RhdGVba2V5XSA9IG5leHRTdGF0ZUZvcktleTtcbiAgICAgIGhhc0NoYW5nZWQgPSBoYXNDaGFuZ2VkIHx8IG5leHRTdGF0ZUZvcktleSAhPT0gcHJldmlvdXNTdGF0ZUZvcktleTtcbiAgICB9XG4gICAgcmV0dXJuIGhhc0NoYW5nZWQgPyBuZXh0U3RhdGUgOiBzdGF0ZTtcbiAgfTtcbn0iLCJcInVzZSBzdHJpY3RcIjtcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gY29tcG9zZTtcbi8qKlxuICogQ29tcG9zZXMgc2luZ2xlLWFyZ3VtZW50IGZ1bmN0aW9ucyBmcm9tIHJpZ2h0IHRvIGxlZnQuIFRoZSByaWdodG1vc3RcbiAqIGZ1bmN0aW9uIGNhbiB0YWtlIG11bHRpcGxlIGFyZ3VtZW50cyBhcyBpdCBwcm92aWRlcyB0aGUgc2lnbmF0dXJlIGZvclxuICogdGhlIHJlc3VsdGluZyBjb21wb3NpdGUgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHsuLi5GdW5jdGlvbn0gZnVuY3MgVGhlIGZ1bmN0aW9ucyB0byBjb21wb3NlLlxuICogQHJldHVybnMge0Z1bmN0aW9ufSBBIGZ1bmN0aW9uIG9idGFpbmVkIGJ5IGNvbXBvc2luZyB0aGUgYXJndW1lbnQgZnVuY3Rpb25zXG4gKiBmcm9tIHJpZ2h0IHRvIGxlZnQuIEZvciBleGFtcGxlLCBjb21wb3NlKGYsIGcsIGgpIGlzIGlkZW50aWNhbCB0byBkb2luZ1xuICogKC4uLmFyZ3MpID0+IGYoZyhoKC4uLmFyZ3MpKSkuXG4gKi9cblxuZnVuY3Rpb24gY29tcG9zZSgpIHtcbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIGZ1bmNzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgZnVuY3NbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICBpZiAoZnVuY3MubGVuZ3RoID09PSAwKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChhcmcpIHtcbiAgICAgIHJldHVybiBhcmc7XG4gICAgfTtcbiAgfSBlbHNlIHtcbiAgICB2YXIgX3JldCA9IGZ1bmN0aW9uICgpIHtcbiAgICAgIHZhciBsYXN0ID0gZnVuY3NbZnVuY3MubGVuZ3RoIC0gMV07XG4gICAgICB2YXIgcmVzdCA9IGZ1bmNzLnNsaWNlKDAsIC0xKTtcbiAgICAgIHJldHVybiB7XG4gICAgICAgIHY6IGZ1bmN0aW9uIHYoKSB7XG4gICAgICAgICAgcmV0dXJuIHJlc3QucmVkdWNlUmlnaHQoZnVuY3Rpb24gKGNvbXBvc2VkLCBmKSB7XG4gICAgICAgICAgICByZXR1cm4gZihjb21wb3NlZCk7XG4gICAgICAgICAgfSwgbGFzdC5hcHBseSh1bmRlZmluZWQsIGFyZ3VtZW50cykpO1xuICAgICAgICB9XG4gICAgICB9O1xuICAgIH0oKTtcblxuICAgIGlmICh0eXBlb2YgX3JldCA9PT0gXCJvYmplY3RcIikgcmV0dXJuIF9yZXQudjtcbiAgfVxufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuQWN0aW9uVHlwZXMgPSB1bmRlZmluZWQ7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGNyZWF0ZVN0b3JlO1xuXG52YXIgX2lzUGxhaW5PYmplY3QgPSByZXF1aXJlKCdsb2Rhc2gvaXNQbGFpbk9iamVjdCcpO1xuXG52YXIgX2lzUGxhaW5PYmplY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaXNQbGFpbk9iamVjdCk7XG5cbnZhciBfc3ltYm9sT2JzZXJ2YWJsZSA9IHJlcXVpcmUoJ3N5bWJvbC1vYnNlcnZhYmxlJyk7XG5cbnZhciBfc3ltYm9sT2JzZXJ2YWJsZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zeW1ib2xPYnNlcnZhYmxlKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbi8qKlxuICogVGhlc2UgYXJlIHByaXZhdGUgYWN0aW9uIHR5cGVzIHJlc2VydmVkIGJ5IFJlZHV4LlxuICogRm9yIGFueSB1bmtub3duIGFjdGlvbnMsIHlvdSBtdXN0IHJldHVybiB0aGUgY3VycmVudCBzdGF0ZS5cbiAqIElmIHRoZSBjdXJyZW50IHN0YXRlIGlzIHVuZGVmaW5lZCwgeW91IG11c3QgcmV0dXJuIHRoZSBpbml0aWFsIHN0YXRlLlxuICogRG8gbm90IHJlZmVyZW5jZSB0aGVzZSBhY3Rpb24gdHlwZXMgZGlyZWN0bHkgaW4geW91ciBjb2RlLlxuICovXG52YXIgQWN0aW9uVHlwZXMgPSBleHBvcnRzLkFjdGlvblR5cGVzID0ge1xuICBJTklUOiAnQEByZWR1eC9JTklUJ1xufTtcblxuLyoqXG4gKiBDcmVhdGVzIGEgUmVkdXggc3RvcmUgdGhhdCBob2xkcyB0aGUgc3RhdGUgdHJlZS5cbiAqIFRoZSBvbmx5IHdheSB0byBjaGFuZ2UgdGhlIGRhdGEgaW4gdGhlIHN0b3JlIGlzIHRvIGNhbGwgYGRpc3BhdGNoKClgIG9uIGl0LlxuICpcbiAqIFRoZXJlIHNob3VsZCBvbmx5IGJlIGEgc2luZ2xlIHN0b3JlIGluIHlvdXIgYXBwLiBUbyBzcGVjaWZ5IGhvdyBkaWZmZXJlbnRcbiAqIHBhcnRzIG9mIHRoZSBzdGF0ZSB0cmVlIHJlc3BvbmQgdG8gYWN0aW9ucywgeW91IG1heSBjb21iaW5lIHNldmVyYWwgcmVkdWNlcnNcbiAqIGludG8gYSBzaW5nbGUgcmVkdWNlciBmdW5jdGlvbiBieSB1c2luZyBgY29tYmluZVJlZHVjZXJzYC5cbiAqXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSByZWR1Y2VyIEEgZnVuY3Rpb24gdGhhdCByZXR1cm5zIHRoZSBuZXh0IHN0YXRlIHRyZWUsIGdpdmVuXG4gKiB0aGUgY3VycmVudCBzdGF0ZSB0cmVlIGFuZCB0aGUgYWN0aW9uIHRvIGhhbmRsZS5cbiAqXG4gKiBAcGFyYW0ge2FueX0gW2luaXRpYWxTdGF0ZV0gVGhlIGluaXRpYWwgc3RhdGUuIFlvdSBtYXkgb3B0aW9uYWxseSBzcGVjaWZ5IGl0XG4gKiB0byBoeWRyYXRlIHRoZSBzdGF0ZSBmcm9tIHRoZSBzZXJ2ZXIgaW4gdW5pdmVyc2FsIGFwcHMsIG9yIHRvIHJlc3RvcmUgYVxuICogcHJldmlvdXNseSBzZXJpYWxpemVkIHVzZXIgc2Vzc2lvbi5cbiAqIElmIHlvdSB1c2UgYGNvbWJpbmVSZWR1Y2Vyc2AgdG8gcHJvZHVjZSB0aGUgcm9vdCByZWR1Y2VyIGZ1bmN0aW9uLCB0aGlzIG11c3QgYmVcbiAqIGFuIG9iamVjdCB3aXRoIHRoZSBzYW1lIHNoYXBlIGFzIGBjb21iaW5lUmVkdWNlcnNgIGtleXMuXG4gKlxuICogQHBhcmFtIHtGdW5jdGlvbn0gZW5oYW5jZXIgVGhlIHN0b3JlIGVuaGFuY2VyLiBZb3UgbWF5IG9wdGlvbmFsbHkgc3BlY2lmeSBpdFxuICogdG8gZW5oYW5jZSB0aGUgc3RvcmUgd2l0aCB0aGlyZC1wYXJ0eSBjYXBhYmlsaXRpZXMgc3VjaCBhcyBtaWRkbGV3YXJlLFxuICogdGltZSB0cmF2ZWwsIHBlcnNpc3RlbmNlLCBldGMuIFRoZSBvbmx5IHN0b3JlIGVuaGFuY2VyIHRoYXQgc2hpcHMgd2l0aCBSZWR1eFxuICogaXMgYGFwcGx5TWlkZGxld2FyZSgpYC5cbiAqXG4gKiBAcmV0dXJucyB7U3RvcmV9IEEgUmVkdXggc3RvcmUgdGhhdCBsZXRzIHlvdSByZWFkIHRoZSBzdGF0ZSwgZGlzcGF0Y2ggYWN0aW9uc1xuICogYW5kIHN1YnNjcmliZSB0byBjaGFuZ2VzLlxuICovXG5mdW5jdGlvbiBjcmVhdGVTdG9yZShyZWR1Y2VyLCBpbml0aWFsU3RhdGUsIGVuaGFuY2VyKSB7XG4gIHZhciBfcmVmMjtcblxuICBpZiAodHlwZW9mIGluaXRpYWxTdGF0ZSA9PT0gJ2Z1bmN0aW9uJyAmJiB0eXBlb2YgZW5oYW5jZXIgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgZW5oYW5jZXIgPSBpbml0aWFsU3RhdGU7XG4gICAgaW5pdGlhbFN0YXRlID0gdW5kZWZpbmVkO1xuICB9XG5cbiAgaWYgKHR5cGVvZiBlbmhhbmNlciAhPT0gJ3VuZGVmaW5lZCcpIHtcbiAgICBpZiAodHlwZW9mIGVuaGFuY2VyICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0V4cGVjdGVkIHRoZSBlbmhhbmNlciB0byBiZSBhIGZ1bmN0aW9uLicpO1xuICAgIH1cblxuICAgIHJldHVybiBlbmhhbmNlcihjcmVhdGVTdG9yZSkocmVkdWNlciwgaW5pdGlhbFN0YXRlKTtcbiAgfVxuXG4gIGlmICh0eXBlb2YgcmVkdWNlciAhPT0gJ2Z1bmN0aW9uJykge1xuICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgdGhlIHJlZHVjZXIgdG8gYmUgYSBmdW5jdGlvbi4nKTtcbiAgfVxuXG4gIHZhciBjdXJyZW50UmVkdWNlciA9IHJlZHVjZXI7XG4gIHZhciBjdXJyZW50U3RhdGUgPSBpbml0aWFsU3RhdGU7XG4gIHZhciBjdXJyZW50TGlzdGVuZXJzID0gW107XG4gIHZhciBuZXh0TGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycztcbiAgdmFyIGlzRGlzcGF0Y2hpbmcgPSBmYWxzZTtcblxuICBmdW5jdGlvbiBlbnN1cmVDYW5NdXRhdGVOZXh0TGlzdGVuZXJzKCkge1xuICAgIGlmIChuZXh0TGlzdGVuZXJzID09PSBjdXJyZW50TGlzdGVuZXJzKSB7XG4gICAgICBuZXh0TGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycy5zbGljZSgpO1xuICAgIH1cbiAgfVxuXG4gIC8qKlxuICAgKiBSZWFkcyB0aGUgc3RhdGUgdHJlZSBtYW5hZ2VkIGJ5IHRoZSBzdG9yZS5cbiAgICpcbiAgICogQHJldHVybnMge2FueX0gVGhlIGN1cnJlbnQgc3RhdGUgdHJlZSBvZiB5b3VyIGFwcGxpY2F0aW9uLlxuICAgKi9cbiAgZnVuY3Rpb24gZ2V0U3RhdGUoKSB7XG4gICAgcmV0dXJuIGN1cnJlbnRTdGF0ZTtcbiAgfVxuXG4gIC8qKlxuICAgKiBBZGRzIGEgY2hhbmdlIGxpc3RlbmVyLiBJdCB3aWxsIGJlIGNhbGxlZCBhbnkgdGltZSBhbiBhY3Rpb24gaXMgZGlzcGF0Y2hlZCxcbiAgICogYW5kIHNvbWUgcGFydCBvZiB0aGUgc3RhdGUgdHJlZSBtYXkgcG90ZW50aWFsbHkgaGF2ZSBjaGFuZ2VkLiBZb3UgbWF5IHRoZW5cbiAgICogY2FsbCBgZ2V0U3RhdGUoKWAgdG8gcmVhZCB0aGUgY3VycmVudCBzdGF0ZSB0cmVlIGluc2lkZSB0aGUgY2FsbGJhY2suXG4gICAqXG4gICAqIFlvdSBtYXkgY2FsbCBgZGlzcGF0Y2goKWAgZnJvbSBhIGNoYW5nZSBsaXN0ZW5lciwgd2l0aCB0aGUgZm9sbG93aW5nXG4gICAqIGNhdmVhdHM6XG4gICAqXG4gICAqIDEuIFRoZSBzdWJzY3JpcHRpb25zIGFyZSBzbmFwc2hvdHRlZCBqdXN0IGJlZm9yZSBldmVyeSBgZGlzcGF0Y2goKWAgY2FsbC5cbiAgICogSWYgeW91IHN1YnNjcmliZSBvciB1bnN1YnNjcmliZSB3aGlsZSB0aGUgbGlzdGVuZXJzIGFyZSBiZWluZyBpbnZva2VkLCB0aGlzXG4gICAqIHdpbGwgbm90IGhhdmUgYW55IGVmZmVjdCBvbiB0aGUgYGRpc3BhdGNoKClgIHRoYXQgaXMgY3VycmVudGx5IGluIHByb2dyZXNzLlxuICAgKiBIb3dldmVyLCB0aGUgbmV4dCBgZGlzcGF0Y2goKWAgY2FsbCwgd2hldGhlciBuZXN0ZWQgb3Igbm90LCB3aWxsIHVzZSBhIG1vcmVcbiAgICogcmVjZW50IHNuYXBzaG90IG9mIHRoZSBzdWJzY3JpcHRpb24gbGlzdC5cbiAgICpcbiAgICogMi4gVGhlIGxpc3RlbmVyIHNob3VsZCBub3QgZXhwZWN0IHRvIHNlZSBhbGwgc3RhdGUgY2hhbmdlcywgYXMgdGhlIHN0YXRlXG4gICAqIG1pZ2h0IGhhdmUgYmVlbiB1cGRhdGVkIG11bHRpcGxlIHRpbWVzIGR1cmluZyBhIG5lc3RlZCBgZGlzcGF0Y2goKWAgYmVmb3JlXG4gICAqIHRoZSBsaXN0ZW5lciBpcyBjYWxsZWQuIEl0IGlzLCBob3dldmVyLCBndWFyYW50ZWVkIHRoYXQgYWxsIHN1YnNjcmliZXJzXG4gICAqIHJlZ2lzdGVyZWQgYmVmb3JlIHRoZSBgZGlzcGF0Y2goKWAgc3RhcnRlZCB3aWxsIGJlIGNhbGxlZCB3aXRoIHRoZSBsYXRlc3RcbiAgICogc3RhdGUgYnkgdGhlIHRpbWUgaXQgZXhpdHMuXG4gICAqXG4gICAqIEBwYXJhbSB7RnVuY3Rpb259IGxpc3RlbmVyIEEgY2FsbGJhY2sgdG8gYmUgaW52b2tlZCBvbiBldmVyeSBkaXNwYXRjaC5cbiAgICogQHJldHVybnMge0Z1bmN0aW9ufSBBIGZ1bmN0aW9uIHRvIHJlbW92ZSB0aGlzIGNoYW5nZSBsaXN0ZW5lci5cbiAgICovXG4gIGZ1bmN0aW9uIHN1YnNjcmliZShsaXN0ZW5lcikge1xuICAgIGlmICh0eXBlb2YgbGlzdGVuZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgbGlzdGVuZXIgdG8gYmUgYSBmdW5jdGlvbi4nKTtcbiAgICB9XG5cbiAgICB2YXIgaXNTdWJzY3JpYmVkID0gdHJ1ZTtcblxuICAgIGVuc3VyZUNhbk11dGF0ZU5leHRMaXN0ZW5lcnMoKTtcbiAgICBuZXh0TGlzdGVuZXJzLnB1c2gobGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uIHVuc3Vic2NyaWJlKCkge1xuICAgICAgaWYgKCFpc1N1YnNjcmliZWQpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuXG4gICAgICBpc1N1YnNjcmliZWQgPSBmYWxzZTtcblxuICAgICAgZW5zdXJlQ2FuTXV0YXRlTmV4dExpc3RlbmVycygpO1xuICAgICAgdmFyIGluZGV4ID0gbmV4dExpc3RlbmVycy5pbmRleE9mKGxpc3RlbmVyKTtcbiAgICAgIG5leHRMaXN0ZW5lcnMuc3BsaWNlKGluZGV4LCAxKTtcbiAgICB9O1xuICB9XG5cbiAgLyoqXG4gICAqIERpc3BhdGNoZXMgYW4gYWN0aW9uLiBJdCBpcyB0aGUgb25seSB3YXkgdG8gdHJpZ2dlciBhIHN0YXRlIGNoYW5nZS5cbiAgICpcbiAgICogVGhlIGByZWR1Y2VyYCBmdW5jdGlvbiwgdXNlZCB0byBjcmVhdGUgdGhlIHN0b3JlLCB3aWxsIGJlIGNhbGxlZCB3aXRoIHRoZVxuICAgKiBjdXJyZW50IHN0YXRlIHRyZWUgYW5kIHRoZSBnaXZlbiBgYWN0aW9uYC4gSXRzIHJldHVybiB2YWx1ZSB3aWxsXG4gICAqIGJlIGNvbnNpZGVyZWQgdGhlICoqbmV4dCoqIHN0YXRlIG9mIHRoZSB0cmVlLCBhbmQgdGhlIGNoYW5nZSBsaXN0ZW5lcnNcbiAgICogd2lsbCBiZSBub3RpZmllZC5cbiAgICpcbiAgICogVGhlIGJhc2UgaW1wbGVtZW50YXRpb24gb25seSBzdXBwb3J0cyBwbGFpbiBvYmplY3QgYWN0aW9ucy4gSWYgeW91IHdhbnQgdG9cbiAgICogZGlzcGF0Y2ggYSBQcm9taXNlLCBhbiBPYnNlcnZhYmxlLCBhIHRodW5rLCBvciBzb21ldGhpbmcgZWxzZSwgeW91IG5lZWQgdG9cbiAgICogd3JhcCB5b3VyIHN0b3JlIGNyZWF0aW5nIGZ1bmN0aW9uIGludG8gdGhlIGNvcnJlc3BvbmRpbmcgbWlkZGxld2FyZS4gRm9yXG4gICAqIGV4YW1wbGUsIHNlZSB0aGUgZG9jdW1lbnRhdGlvbiBmb3IgdGhlIGByZWR1eC10aHVua2AgcGFja2FnZS4gRXZlbiB0aGVcbiAgICogbWlkZGxld2FyZSB3aWxsIGV2ZW50dWFsbHkgZGlzcGF0Y2ggcGxhaW4gb2JqZWN0IGFjdGlvbnMgdXNpbmcgdGhpcyBtZXRob2QuXG4gICAqXG4gICAqIEBwYXJhbSB7T2JqZWN0fSBhY3Rpb24gQSBwbGFpbiBvYmplY3QgcmVwcmVzZW50aW5nIOKAnHdoYXQgY2hhbmdlZOKAnS4gSXQgaXNcbiAgICogYSBnb29kIGlkZWEgdG8ga2VlcCBhY3Rpb25zIHNlcmlhbGl6YWJsZSBzbyB5b3UgY2FuIHJlY29yZCBhbmQgcmVwbGF5IHVzZXJcbiAgICogc2Vzc2lvbnMsIG9yIHVzZSB0aGUgdGltZSB0cmF2ZWxsaW5nIGByZWR1eC1kZXZ0b29sc2AuIEFuIGFjdGlvbiBtdXN0IGhhdmVcbiAgICogYSBgdHlwZWAgcHJvcGVydHkgd2hpY2ggbWF5IG5vdCBiZSBgdW5kZWZpbmVkYC4gSXQgaXMgYSBnb29kIGlkZWEgdG8gdXNlXG4gICAqIHN0cmluZyBjb25zdGFudHMgZm9yIGFjdGlvbiB0eXBlcy5cbiAgICpcbiAgICogQHJldHVybnMge09iamVjdH0gRm9yIGNvbnZlbmllbmNlLCB0aGUgc2FtZSBhY3Rpb24gb2JqZWN0IHlvdSBkaXNwYXRjaGVkLlxuICAgKlxuICAgKiBOb3RlIHRoYXQsIGlmIHlvdSB1c2UgYSBjdXN0b20gbWlkZGxld2FyZSwgaXQgbWF5IHdyYXAgYGRpc3BhdGNoKClgIHRvXG4gICAqIHJldHVybiBzb21ldGhpbmcgZWxzZSAoZm9yIGV4YW1wbGUsIGEgUHJvbWlzZSB5b3UgY2FuIGF3YWl0KS5cbiAgICovXG4gIGZ1bmN0aW9uIGRpc3BhdGNoKGFjdGlvbikge1xuICAgIGlmICghKDAsIF9pc1BsYWluT2JqZWN0MltcImRlZmF1bHRcIl0pKGFjdGlvbikpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignQWN0aW9ucyBtdXN0IGJlIHBsYWluIG9iamVjdHMuICcgKyAnVXNlIGN1c3RvbSBtaWRkbGV3YXJlIGZvciBhc3luYyBhY3Rpb25zLicpO1xuICAgIH1cblxuICAgIGlmICh0eXBlb2YgYWN0aW9uLnR5cGUgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0FjdGlvbnMgbWF5IG5vdCBoYXZlIGFuIHVuZGVmaW5lZCBcInR5cGVcIiBwcm9wZXJ0eS4gJyArICdIYXZlIHlvdSBtaXNzcGVsbGVkIGEgY29uc3RhbnQ/Jyk7XG4gICAgfVxuXG4gICAgaWYgKGlzRGlzcGF0Y2hpbmcpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignUmVkdWNlcnMgbWF5IG5vdCBkaXNwYXRjaCBhY3Rpb25zLicpO1xuICAgIH1cblxuICAgIHRyeSB7XG4gICAgICBpc0Rpc3BhdGNoaW5nID0gdHJ1ZTtcbiAgICAgIGN1cnJlbnRTdGF0ZSA9IGN1cnJlbnRSZWR1Y2VyKGN1cnJlbnRTdGF0ZSwgYWN0aW9uKTtcbiAgICB9IGZpbmFsbHkge1xuICAgICAgaXNEaXNwYXRjaGluZyA9IGZhbHNlO1xuICAgIH1cblxuICAgIHZhciBsaXN0ZW5lcnMgPSBjdXJyZW50TGlzdGVuZXJzID0gbmV4dExpc3RlbmVycztcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGxpc3RlbmVycy5sZW5ndGg7IGkrKykge1xuICAgICAgbGlzdGVuZXJzW2ldKCk7XG4gICAgfVxuXG4gICAgcmV0dXJuIGFjdGlvbjtcbiAgfVxuXG4gIC8qKlxuICAgKiBSZXBsYWNlcyB0aGUgcmVkdWNlciBjdXJyZW50bHkgdXNlZCBieSB0aGUgc3RvcmUgdG8gY2FsY3VsYXRlIHRoZSBzdGF0ZS5cbiAgICpcbiAgICogWW91IG1pZ2h0IG5lZWQgdGhpcyBpZiB5b3VyIGFwcCBpbXBsZW1lbnRzIGNvZGUgc3BsaXR0aW5nIGFuZCB5b3Ugd2FudCB0b1xuICAgKiBsb2FkIHNvbWUgb2YgdGhlIHJlZHVjZXJzIGR5bmFtaWNhbGx5LiBZb3UgbWlnaHQgYWxzbyBuZWVkIHRoaXMgaWYgeW91XG4gICAqIGltcGxlbWVudCBhIGhvdCByZWxvYWRpbmcgbWVjaGFuaXNtIGZvciBSZWR1eC5cbiAgICpcbiAgICogQHBhcmFtIHtGdW5jdGlvbn0gbmV4dFJlZHVjZXIgVGhlIHJlZHVjZXIgZm9yIHRoZSBzdG9yZSB0byB1c2UgaW5zdGVhZC5cbiAgICogQHJldHVybnMge3ZvaWR9XG4gICAqL1xuICBmdW5jdGlvbiByZXBsYWNlUmVkdWNlcihuZXh0UmVkdWNlcikge1xuICAgIGlmICh0eXBlb2YgbmV4dFJlZHVjZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgdGhlIG5leHRSZWR1Y2VyIHRvIGJlIGEgZnVuY3Rpb24uJyk7XG4gICAgfVxuXG4gICAgY3VycmVudFJlZHVjZXIgPSBuZXh0UmVkdWNlcjtcbiAgICBkaXNwYXRjaCh7IHR5cGU6IEFjdGlvblR5cGVzLklOSVQgfSk7XG4gIH1cblxuICAvKipcbiAgICogSW50ZXJvcGVyYWJpbGl0eSBwb2ludCBmb3Igb2JzZXJ2YWJsZS9yZWFjdGl2ZSBsaWJyYXJpZXMuXG4gICAqIEByZXR1cm5zIHtvYnNlcnZhYmxlfSBBIG1pbmltYWwgb2JzZXJ2YWJsZSBvZiBzdGF0ZSBjaGFuZ2VzLlxuICAgKiBGb3IgbW9yZSBpbmZvcm1hdGlvbiwgc2VlIHRoZSBvYnNlcnZhYmxlIHByb3Bvc2FsOlxuICAgKiBodHRwczovL2dpdGh1Yi5jb20vemVucGFyc2luZy9lcy1vYnNlcnZhYmxlXG4gICAqL1xuICBmdW5jdGlvbiBvYnNlcnZhYmxlKCkge1xuICAgIHZhciBfcmVmO1xuXG4gICAgdmFyIG91dGVyU3Vic2NyaWJlID0gc3Vic2NyaWJlO1xuICAgIHJldHVybiBfcmVmID0ge1xuICAgICAgLyoqXG4gICAgICAgKiBUaGUgbWluaW1hbCBvYnNlcnZhYmxlIHN1YnNjcmlwdGlvbiBtZXRob2QuXG4gICAgICAgKiBAcGFyYW0ge09iamVjdH0gb2JzZXJ2ZXIgQW55IG9iamVjdCB0aGF0IGNhbiBiZSB1c2VkIGFzIGFuIG9ic2VydmVyLlxuICAgICAgICogVGhlIG9ic2VydmVyIG9iamVjdCBzaG91bGQgaGF2ZSBhIGBuZXh0YCBtZXRob2QuXG4gICAgICAgKiBAcmV0dXJucyB7c3Vic2NyaXB0aW9ufSBBbiBvYmplY3Qgd2l0aCBhbiBgdW5zdWJzY3JpYmVgIG1ldGhvZCB0aGF0IGNhblxuICAgICAgICogYmUgdXNlZCB0byB1bnN1YnNjcmliZSB0aGUgb2JzZXJ2YWJsZSBmcm9tIHRoZSBzdG9yZSwgYW5kIHByZXZlbnQgZnVydGhlclxuICAgICAgICogZW1pc3Npb24gb2YgdmFsdWVzIGZyb20gdGhlIG9ic2VydmFibGUuXG4gICAgICAgKi9cblxuICAgICAgc3Vic2NyaWJlOiBmdW5jdGlvbiBzdWJzY3JpYmUob2JzZXJ2ZXIpIHtcbiAgICAgICAgaWYgKHR5cGVvZiBvYnNlcnZlciAhPT0gJ29iamVjdCcpIHtcbiAgICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdFeHBlY3RlZCB0aGUgb2JzZXJ2ZXIgdG8gYmUgYW4gb2JqZWN0LicpO1xuICAgICAgICB9XG5cbiAgICAgICAgZnVuY3Rpb24gb2JzZXJ2ZVN0YXRlKCkge1xuICAgICAgICAgIGlmIChvYnNlcnZlci5uZXh0KSB7XG4gICAgICAgICAgICBvYnNlcnZlci5uZXh0KGdldFN0YXRlKCkpO1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIG9ic2VydmVTdGF0ZSgpO1xuICAgICAgICB2YXIgdW5zdWJzY3JpYmUgPSBvdXRlclN1YnNjcmliZShvYnNlcnZlU3RhdGUpO1xuICAgICAgICByZXR1cm4geyB1bnN1YnNjcmliZTogdW5zdWJzY3JpYmUgfTtcbiAgICAgIH1cbiAgICB9LCBfcmVmW19zeW1ib2xPYnNlcnZhYmxlMltcImRlZmF1bHRcIl1dID0gZnVuY3Rpb24gKCkge1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfSwgX3JlZjtcbiAgfVxuXG4gIC8vIFdoZW4gYSBzdG9yZSBpcyBjcmVhdGVkLCBhbiBcIklOSVRcIiBhY3Rpb24gaXMgZGlzcGF0Y2hlZCBzbyB0aGF0IGV2ZXJ5XG4gIC8vIHJlZHVjZXIgcmV0dXJucyB0aGVpciBpbml0aWFsIHN0YXRlLiBUaGlzIGVmZmVjdGl2ZWx5IHBvcHVsYXRlc1xuICAvLyB0aGUgaW5pdGlhbCBzdGF0ZSB0cmVlLlxuICBkaXNwYXRjaCh7IHR5cGU6IEFjdGlvblR5cGVzLklOSVQgfSk7XG5cbiAgcmV0dXJuIF9yZWYyID0ge1xuICAgIGRpc3BhdGNoOiBkaXNwYXRjaCxcbiAgICBzdWJzY3JpYmU6IHN1YnNjcmliZSxcbiAgICBnZXRTdGF0ZTogZ2V0U3RhdGUsXG4gICAgcmVwbGFjZVJlZHVjZXI6IHJlcGxhY2VSZWR1Y2VyXG4gIH0sIF9yZWYyW19zeW1ib2xPYnNlcnZhYmxlMltcImRlZmF1bHRcIl1dID0gb2JzZXJ2YWJsZSwgX3JlZjI7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5jb21wb3NlID0gZXhwb3J0cy5hcHBseU1pZGRsZXdhcmUgPSBleHBvcnRzLmJpbmRBY3Rpb25DcmVhdG9ycyA9IGV4cG9ydHMuY29tYmluZVJlZHVjZXJzID0gZXhwb3J0cy5jcmVhdGVTdG9yZSA9IHVuZGVmaW5lZDtcblxudmFyIF9jcmVhdGVTdG9yZSA9IHJlcXVpcmUoJy4vY3JlYXRlU3RvcmUnKTtcblxudmFyIF9jcmVhdGVTdG9yZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVTdG9yZSk7XG5cbnZhciBfY29tYmluZVJlZHVjZXJzID0gcmVxdWlyZSgnLi9jb21iaW5lUmVkdWNlcnMnKTtcblxudmFyIF9jb21iaW5lUmVkdWNlcnMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29tYmluZVJlZHVjZXJzKTtcblxudmFyIF9iaW5kQWN0aW9uQ3JlYXRvcnMgPSByZXF1aXJlKCcuL2JpbmRBY3Rpb25DcmVhdG9ycycpO1xuXG52YXIgX2JpbmRBY3Rpb25DcmVhdG9yczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9iaW5kQWN0aW9uQ3JlYXRvcnMpO1xuXG52YXIgX2FwcGx5TWlkZGxld2FyZSA9IHJlcXVpcmUoJy4vYXBwbHlNaWRkbGV3YXJlJyk7XG5cbnZhciBfYXBwbHlNaWRkbGV3YXJlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2FwcGx5TWlkZGxld2FyZSk7XG5cbnZhciBfY29tcG9zZSA9IHJlcXVpcmUoJy4vY29tcG9zZScpO1xuXG52YXIgX2NvbXBvc2UyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29tcG9zZSk7XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJy4vdXRpbHMvd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG4vKlxuKiBUaGlzIGlzIGEgZHVtbXkgZnVuY3Rpb24gdG8gY2hlY2sgaWYgdGhlIGZ1bmN0aW9uIG5hbWUgaGFzIGJlZW4gYWx0ZXJlZCBieSBtaW5pZmljYXRpb24uXG4qIElmIHRoZSBmdW5jdGlvbiBoYXMgYmVlbiBtaW5pZmllZCBhbmQgTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJywgd2FybiB0aGUgdXNlci5cbiovXG5mdW5jdGlvbiBpc0NydXNoZWQoKSB7fVxuXG5pZiAocHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyAmJiB0eXBlb2YgaXNDcnVzaGVkLm5hbWUgPT09ICdzdHJpbmcnICYmIGlzQ3J1c2hlZC5uYW1lICE9PSAnaXNDcnVzaGVkJykge1xuICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkoJ1lvdSBhcmUgY3VycmVudGx5IHVzaW5nIG1pbmlmaWVkIGNvZGUgb3V0c2lkZSBvZiBOT0RFX0VOViA9PT0gXFwncHJvZHVjdGlvblxcJy4gJyArICdUaGlzIG1lYW5zIHRoYXQgeW91IGFyZSBydW5uaW5nIGEgc2xvd2VyIGRldmVsb3BtZW50IGJ1aWxkIG9mIFJlZHV4LiAnICsgJ1lvdSBjYW4gdXNlIGxvb3NlLWVudmlmeSAoaHR0cHM6Ly9naXRodWIuY29tL3plcnRvc2gvbG9vc2UtZW52aWZ5KSBmb3IgYnJvd3NlcmlmeSAnICsgJ29yIERlZmluZVBsdWdpbiBmb3Igd2VicGFjayAoaHR0cDovL3N0YWNrb3ZlcmZsb3cuY29tL3F1ZXN0aW9ucy8zMDAzMDAzMSkgJyArICd0byBlbnN1cmUgeW91IGhhdmUgdGhlIGNvcnJlY3QgY29kZSBmb3IgeW91ciBwcm9kdWN0aW9uIGJ1aWxkLicpO1xufVxuXG5leHBvcnRzLmNyZWF0ZVN0b3JlID0gX2NyZWF0ZVN0b3JlMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmNvbWJpbmVSZWR1Y2VycyA9IF9jb21iaW5lUmVkdWNlcnMyW1wiZGVmYXVsdFwiXTtcbmV4cG9ydHMuYmluZEFjdGlvbkNyZWF0b3JzID0gX2JpbmRBY3Rpb25DcmVhdG9yczJbXCJkZWZhdWx0XCJdO1xuZXhwb3J0cy5hcHBseU1pZGRsZXdhcmUgPSBfYXBwbHlNaWRkbGV3YXJlMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmNvbXBvc2UgPSBfY29tcG9zZTJbXCJkZWZhdWx0XCJdOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gd2FybmluZztcbi8qKlxuICogUHJpbnRzIGEgd2FybmluZyBpbiB0aGUgY29uc29sZSBpZiBpdCBleGlzdHMuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd9IG1lc3NhZ2UgVGhlIHdhcm5pbmcgbWVzc2FnZS5cbiAqIEByZXR1cm5zIHt2b2lkfVxuICovXG5mdW5jdGlvbiB3YXJuaW5nKG1lc3NhZ2UpIHtcbiAgLyogZXNsaW50LWRpc2FibGUgbm8tY29uc29sZSAqL1xuICBpZiAodHlwZW9mIGNvbnNvbGUgIT09ICd1bmRlZmluZWQnICYmIHR5cGVvZiBjb25zb2xlLmVycm9yID09PSAnZnVuY3Rpb24nKSB7XG4gICAgY29uc29sZS5lcnJvcihtZXNzYWdlKTtcbiAgfVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWNvbnNvbGUgKi9cbiAgdHJ5IHtcbiAgICAvLyBUaGlzIGVycm9yIHdhcyB0aHJvd24gYXMgYSBjb252ZW5pZW5jZSBzbyB0aGF0IGlmIHlvdSBlbmFibGVcbiAgICAvLyBcImJyZWFrIG9uIGFsbCBleGNlcHRpb25zXCIgaW4geW91ciBjb25zb2xlLFxuICAgIC8vIGl0IHdvdWxkIHBhdXNlIHRoZSBleGVjdXRpb24gYXQgdGhpcyBsaW5lLlxuICAgIHRocm93IG5ldyBFcnJvcihtZXNzYWdlKTtcbiAgICAvKiBlc2xpbnQtZGlzYWJsZSBuby1lbXB0eSAqL1xuICB9IGNhdGNoIChlKSB7fVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWVtcHR5ICovXG59IiwiJ3VzZSBzdHJpY3QnO1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiAoc3RyKSB7XG5cdHJldHVybiBlbmNvZGVVUklDb21wb25lbnQoc3RyKS5yZXBsYWNlKC9bIScoKSpdL2csIGZ1bmN0aW9uIChjKSB7XG5cdFx0cmV0dXJuICclJyArIGMuY2hhckNvZGVBdCgwKS50b1N0cmluZygxNikudG9VcHBlckNhc2UoKTtcblx0fSk7XG59O1xuIiwiLyogZ2xvYmFsIHdpbmRvdyAqL1xuJ3VzZSBzdHJpY3QnO1xuXG5tb2R1bGUuZXhwb3J0cyA9IHJlcXVpcmUoJy4vcG9ueWZpbGwnKShnbG9iYWwgfHwgd2luZG93IHx8IHRoaXMpO1xuIiwiJ3VzZSBzdHJpY3QnO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uIHN5bWJvbE9ic2VydmFibGVQb255ZmlsbChyb290KSB7XG5cdHZhciByZXN1bHQ7XG5cdHZhciBTeW1ib2wgPSByb290LlN5bWJvbDtcblxuXHRpZiAodHlwZW9mIFN5bWJvbCA9PT0gJ2Z1bmN0aW9uJykge1xuXHRcdGlmIChTeW1ib2wub2JzZXJ2YWJsZSkge1xuXHRcdFx0cmVzdWx0ID0gU3ltYm9sLm9ic2VydmFibGU7XG5cdFx0fSBlbHNlIHtcblx0XHRcdHJlc3VsdCA9IFN5bWJvbCgnb2JzZXJ2YWJsZScpO1xuXHRcdFx0U3ltYm9sLm9ic2VydmFibGUgPSByZXN1bHQ7XG5cdFx0fVxuXHR9IGVsc2Uge1xuXHRcdHJlc3VsdCA9ICdAQG9ic2VydmFibGUnO1xuXHR9XG5cblx0cmV0dXJuIHJlc3VsdDtcbn07XG4iLCJcbmV4cG9ydHMgPSBtb2R1bGUuZXhwb3J0cyA9IHRyaW07XG5cbmZ1bmN0aW9uIHRyaW0oc3RyKXtcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9eXFxzKnxcXHMqJC9nLCAnJyk7XG59XG5cbmV4cG9ydHMubGVmdCA9IGZ1bmN0aW9uKHN0cil7XG4gIHJldHVybiBzdHIucmVwbGFjZSgvXlxccyovLCAnJyk7XG59O1xuXG5leHBvcnRzLnJpZ2h0ID0gZnVuY3Rpb24oc3RyKXtcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9cXHMqJC8sICcnKTtcbn07XG4iLCIvKipcbiAqIENvcHlyaWdodCAyMDE0LTIwMTUsIEZhY2Vib29rLCBJbmMuXG4gKiBBbGwgcmlnaHRzIHJlc2VydmVkLlxuICpcbiAqIFRoaXMgc291cmNlIGNvZGUgaXMgbGljZW5zZWQgdW5kZXIgdGhlIEJTRC1zdHlsZSBsaWNlbnNlIGZvdW5kIGluIHRoZVxuICogTElDRU5TRSBmaWxlIGluIHRoZSByb290IGRpcmVjdG9yeSBvZiB0aGlzIHNvdXJjZSB0cmVlLiBBbiBhZGRpdGlvbmFsIGdyYW50XG4gKiBvZiBwYXRlbnQgcmlnaHRzIGNhbiBiZSBmb3VuZCBpbiB0aGUgUEFURU5UUyBmaWxlIGluIHRoZSBzYW1lIGRpcmVjdG9yeS5cbiAqL1xuXG4ndXNlIHN0cmljdCc7XG5cbi8qKlxuICogU2ltaWxhciB0byBpbnZhcmlhbnQgYnV0IG9ubHkgbG9ncyBhIHdhcm5pbmcgaWYgdGhlIGNvbmRpdGlvbiBpcyBub3QgbWV0LlxuICogVGhpcyBjYW4gYmUgdXNlZCB0byBsb2cgaXNzdWVzIGluIGRldmVsb3BtZW50IGVudmlyb25tZW50cyBpbiBjcml0aWNhbFxuICogcGF0aHMuIFJlbW92aW5nIHRoZSBsb2dnaW5nIGNvZGUgZm9yIHByb2R1Y3Rpb24gZW52aXJvbm1lbnRzIHdpbGwga2VlcCB0aGVcbiAqIHNhbWUgbG9naWMgYW5kIGZvbGxvdyB0aGUgc2FtZSBjb2RlIHBhdGhzLlxuICovXG5cbnZhciB3YXJuaW5nID0gZnVuY3Rpb24oKSB7fTtcblxuaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicpIHtcbiAgd2FybmluZyA9IGZ1bmN0aW9uKGNvbmRpdGlvbiwgZm9ybWF0LCBhcmdzKSB7XG4gICAgdmFyIGxlbiA9IGFyZ3VtZW50cy5sZW5ndGg7XG4gICAgYXJncyA9IG5ldyBBcnJheShsZW4gPiAyID8gbGVuIC0gMiA6IDApO1xuICAgIGZvciAodmFyIGtleSA9IDI7IGtleSA8IGxlbjsga2V5KyspIHtcbiAgICAgIGFyZ3Nba2V5IC0gMl0gPSBhcmd1bWVudHNba2V5XTtcbiAgICB9XG4gICAgaWYgKGZvcm1hdCA9PT0gdW5kZWZpbmVkKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgICdgd2FybmluZyhjb25kaXRpb24sIGZvcm1hdCwgLi4uYXJncylgIHJlcXVpcmVzIGEgd2FybmluZyAnICtcbiAgICAgICAgJ21lc3NhZ2UgYXJndW1lbnQnXG4gICAgICApO1xuICAgIH1cblxuICAgIGlmIChmb3JtYXQubGVuZ3RoIDwgMTAgfHwgKC9eW3NcXFddKiQvKS50ZXN0KGZvcm1hdCkpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihcbiAgICAgICAgJ1RoZSB3YXJuaW5nIGZvcm1hdCBzaG91bGQgYmUgYWJsZSB0byB1bmlxdWVseSBpZGVudGlmeSB0aGlzICcgK1xuICAgICAgICAnd2FybmluZy4gUGxlYXNlLCB1c2UgYSBtb3JlIGRlc2NyaXB0aXZlIGZvcm1hdCB0aGFuOiAnICsgZm9ybWF0XG4gICAgICApO1xuICAgIH1cblxuICAgIGlmICghY29uZGl0aW9uKSB7XG4gICAgICB2YXIgYXJnSW5kZXggPSAwO1xuICAgICAgdmFyIG1lc3NhZ2UgPSAnV2FybmluZzogJyArXG4gICAgICAgIGZvcm1hdC5yZXBsYWNlKC8lcy9nLCBmdW5jdGlvbigpIHtcbiAgICAgICAgICByZXR1cm4gYXJnc1thcmdJbmRleCsrXTtcbiAgICAgICAgfSk7XG4gICAgICBpZiAodHlwZW9mIGNvbnNvbGUgIT09ICd1bmRlZmluZWQnKSB7XG4gICAgICAgIGNvbnNvbGUuZXJyb3IobWVzc2FnZSk7XG4gICAgICB9XG4gICAgICB0cnkge1xuICAgICAgICAvLyBUaGlzIGVycm9yIHdhcyB0aHJvd24gYXMgYSBjb252ZW5pZW5jZSBzbyB0aGF0IHlvdSBjYW4gdXNlIHRoaXMgc3RhY2tcbiAgICAgICAgLy8gdG8gZmluZCB0aGUgY2FsbHNpdGUgdGhhdCBjYXVzZWQgdGhpcyB3YXJuaW5nIHRvIGZpcmUuXG4gICAgICAgIHRocm93IG5ldyBFcnJvcihtZXNzYWdlKTtcbiAgICAgIH0gY2F0Y2goeCkge31cbiAgICB9XG4gIH07XG59XG5cbm1vZHVsZS5leHBvcnRzID0gd2FybmluZztcbiIsInZhciB3aW5kb3cgICAgICAgICAgICAgID0gcmVxdWlyZSgnZ2xvYmFsJyk7XG52YXIgTW9ja1hNTEh0dHBSZXF1ZXN0ICA9IHJlcXVpcmUoJy4vbGliL01vY2tYTUxIdHRwUmVxdWVzdCcpO1xudmFyIHJlYWwgICAgICAgICAgICAgICAgPSB3aW5kb3cuWE1MSHR0cFJlcXVlc3Q7XG52YXIgbW9jayAgICAgICAgICAgICAgICA9IE1vY2tYTUxIdHRwUmVxdWVzdDtcblxuLyoqXG4gKiBNb2NrIHV0aWxpdHlcbiAqL1xubW9kdWxlLmV4cG9ydHMgPSB7XG5cblx0WE1MSHR0cFJlcXVlc3Q6IE1vY2tYTUxIdHRwUmVxdWVzdCxcblxuXHQvKipcblx0ICogUmVwbGFjZSB0aGUgbmF0aXZlIFhIUiB3aXRoIHRoZSBtb2NrZWQgWEhSXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0c2V0dXA6IGZ1bmN0aW9uKCkge1xuXHRcdHdpbmRvdy5YTUxIdHRwUmVxdWVzdCA9IG1vY2s7XG5cdFx0TW9ja1hNTEh0dHBSZXF1ZXN0LmhhbmRsZXJzID0gW107XG5cdFx0cmV0dXJuIHRoaXM7XG5cdH0sXG5cblx0LyoqXG5cdCAqIFJlcGxhY2UgdGhlIG1vY2tlZCBYSFIgd2l0aCB0aGUgbmF0aXZlIFhIUiBhbmQgcmVtb3ZlIGFueSBoYW5kbGVyc1xuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHRlYXJkb3duOiBmdW5jdGlvbigpIHtcblx0XHRNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMgPSBbXTtcblx0XHR3aW5kb3cuWE1MSHR0cFJlcXVlc3QgPSByZWFsO1xuXHRcdHJldHVybiB0aGlzO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7c3RyaW5nfSAgICBbbWV0aG9kXVxuXHQgKiBAcGFyYW0gICB7c3RyaW5nfSAgICBbdXJsXVxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdG1vY2s6IGZ1bmN0aW9uKG1ldGhvZCwgdXJsLCBmbikge1xuXHRcdHZhciBoYW5kbGVyO1xuXHRcdGlmIChhcmd1bWVudHMubGVuZ3RoID09PSAzKSB7XG5cdFx0XHRoYW5kbGVyID0gZnVuY3Rpb24ocmVxLCByZXMpIHtcblx0XHRcdFx0aWYgKHJlcS5tZXRob2QoKSA9PT0gbWV0aG9kICYmIHJlcS51cmwoKSA9PT0gdXJsKSB7XG5cdFx0XHRcdFx0cmV0dXJuIGZuKHJlcSwgcmVzKTtcblx0XHRcdFx0fVxuXHRcdFx0XHRyZXR1cm4gZmFsc2U7XG5cdFx0XHR9O1xuXHRcdH0gZWxzZSB7XG5cdFx0XHRoYW5kbGVyID0gbWV0aG9kO1xuXHRcdH1cblxuXHRcdE1vY2tYTUxIdHRwUmVxdWVzdC5hZGRIYW5kbGVyKGhhbmRsZXIpO1xuXG5cdFx0cmV0dXJuIHRoaXM7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBHRVQgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRnZXQ6IGZ1bmN0aW9uKHVybCwgZm4pIHtcblx0XHRyZXR1cm4gdGhpcy5tb2NrKCdHRVQnLCB1cmwsIGZuKTtcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIFBPU1QgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRwb3N0OiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnUE9TVCcsIHVybCwgZm4pO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgUFVUIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge1N0cmluZ30gICAgdXJsXG5cdCAqIEBwYXJhbSAgIHtGdW5jdGlvbn0gIGZuXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0cHV0OiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnUFVUJywgdXJsLCBmbik7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBQQVRDSCByZXF1ZXN0XG5cdCAqIEBwYXJhbSAgIHtTdHJpbmd9ICAgIHVybFxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHBhdGNoOiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnUEFUQ0gnLCB1cmwsIGZuKTtcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIERFTEVURSByZXF1ZXN0XG5cdCAqIEBwYXJhbSAgIHtTdHJpbmd9ICAgIHVybFxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdGRlbGV0ZTogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ0RFTEVURScsIHVybCwgZm4pO1xuXHR9XG5cbn07XG4iLCJcbi8qKlxuICogVGhlIG1vY2tlZCByZXF1ZXN0IGRhdGFcbiAqIEBjb25zdHJ1Y3RvclxuICovXG5mdW5jdGlvbiBNb2NrUmVxdWVzdCh4aHIpIHtcbiAgdGhpcy5fbWV0aG9kICAgID0geGhyLm1ldGhvZDtcbiAgdGhpcy5fdXJsICAgICAgID0geGhyLnVybDtcbiAgdGhpcy5faGVhZGVycyAgID0ge307XG4gIHRoaXMuaGVhZGVycyh4aHIuX3JlcXVlc3RIZWFkZXJzKTtcbiAgdGhpcy5ib2R5KHhoci5kYXRhKTtcbn1cblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIG1ldGhvZFxuICogQHJldHVybnMge3N0cmluZ31cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLm1ldGhvZCA9IGZ1bmN0aW9uKCkge1xuICByZXR1cm4gdGhpcy5fbWV0aG9kO1xufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIFVSTFxuICogQHJldHVybnMge3N0cmluZ31cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLnVybCA9IGZ1bmN0aW9uKCkge1xuICByZXR1cm4gdGhpcy5fdXJsO1xufTtcblxuLyoqXG4gKiBHZXQvc2V0IGEgSFRUUCBoZWFkZXJcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IG5hbWVcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFt2YWx1ZV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8dW5kZWZpbmVkfE1vY2tSZXF1ZXN0fVxuICovXG5Nb2NrUmVxdWVzdC5wcm90b3R5cGUuaGVhZGVyID0gZnVuY3Rpb24obmFtZSwgdmFsdWUpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGggPT09IDIpIHtcbiAgICB0aGlzLl9oZWFkZXJzW25hbWUudG9Mb3dlckNhc2UoKV0gPSB2YWx1ZTtcbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5faGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldIHx8IG51bGw7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCBhbGwgb2YgdGhlIEhUVFAgaGVhZGVyc1xuICogQHBhcmFtICAge09iamVjdH0gW2hlYWRlcnNdXG4gKiBAcmV0dXJucyB7T2JqZWN0fE1vY2tSZXF1ZXN0fVxuICovXG5Nb2NrUmVxdWVzdC5wcm90b3R5cGUuaGVhZGVycyA9IGZ1bmN0aW9uKGhlYWRlcnMpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGgpIHtcbiAgICBmb3IgKHZhciBuYW1lIGluIGhlYWRlcnMpIHtcbiAgICAgIGlmIChoZWFkZXJzLmhhc093blByb3BlcnR5KG5hbWUpKSB7XG4gICAgICAgIHRoaXMuaGVhZGVyKG5hbWUsIGhlYWRlcnNbbmFtZV0pO1xuICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5faGVhZGVycztcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIGJvZHlcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFtib2R5XVxuICogQHJldHVybnMge3N0cmluZ3xNb2NrUmVxdWVzdH1cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLmJvZHkgPSBmdW5jdGlvbihib2R5KSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fYm9keSA9IGJvZHk7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2JvZHk7XG4gIH1cbn07XG5cbm1vZHVsZS5leHBvcnRzID0gTW9ja1JlcXVlc3Q7XG4iLCJcbi8qKlxuICogVGhlIG1vY2tlZCByZXNwb25zZSBkYXRhXG4gKiBAY29uc3RydWN0b3JcbiAqL1xuZnVuY3Rpb24gTW9ja1Jlc3BvbnNlKCkge1xuICB0aGlzLl9zdGF0dXMgICAgICA9IDIwMDtcbiAgdGhpcy5faGVhZGVycyAgICAgPSB7fTtcbiAgdGhpcy5fYm9keSAgICAgICAgPSAnJztcbiAgdGhpcy5fdGltZW91dCAgICAgPSBmYWxzZTtcbn1cblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIHN0YXR1c1xuICogQHBhcmFtICAge251bWJlcn0gW2NvZGVdXG4gKiBAcmV0dXJucyB7bnVtYmVyfE1vY2tSZXNwb25zZX1cbiAqL1xuTW9ja1Jlc3BvbnNlLnByb3RvdHlwZS5zdGF0dXMgPSBmdW5jdGlvbihjb2RlKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fc3RhdHVzID0gY29kZTtcbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5fc3RhdHVzO1xuICB9XG59O1xuXG4vKipcbiAqIEdldC9zZXQgYSBIVFRQIGhlYWRlclxuICogQHBhcmFtICAge3N0cmluZ30gbmFtZVxuICogQHBhcmFtICAge3N0cmluZ30gW3ZhbHVlXVxuICogQHJldHVybnMge3N0cmluZ3x1bmRlZmluZWR8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLmhlYWRlciA9IGZ1bmN0aW9uKG5hbWUsIHZhbHVlKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoID09PSAyKSB7XG4gICAgdGhpcy5faGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldID0gdmFsdWU7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSB8fCBudWxsO1xuICB9XG59O1xuXG4vKipcbiAqIEdldC9zZXQgYWxsIG9mIHRoZSBIVFRQIGhlYWRlcnNcbiAqIEBwYXJhbSAgIHtPYmplY3R9IFtoZWFkZXJzXVxuICogQHJldHVybnMge09iamVjdHxNb2NrUmVzcG9uc2V9XG4gKi9cbk1vY2tSZXNwb25zZS5wcm90b3R5cGUuaGVhZGVycyA9IGZ1bmN0aW9uKGhlYWRlcnMpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGgpIHtcbiAgICBmb3IgKHZhciBuYW1lIGluIGhlYWRlcnMpIHtcbiAgICAgIGlmIChoZWFkZXJzLmhhc093blByb3BlcnR5KG5hbWUpKSB7XG4gICAgICAgIHRoaXMuaGVhZGVyKG5hbWUsIGhlYWRlcnNbbmFtZV0pO1xuICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5faGVhZGVycztcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIGJvZHlcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFtib2R5XVxuICogQHJldHVybnMge3N0cmluZ3xNb2NrUmVzcG9uc2V9XG4gKi9cbk1vY2tSZXNwb25zZS5wcm90b3R5cGUuYm9keSA9IGZ1bmN0aW9uKGJvZHkpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGgpIHtcbiAgICB0aGlzLl9ib2R5ID0gYm9keTtcbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5fYm9keTtcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIHRpbWVvdXRcbiAqIEBwYXJhbSAgIHtib29sZWFufG51bWJlcn0gW3RpbWVvdXRdXG4gKiBAcmV0dXJucyB7Ym9vbGVhbnxudW1iZXJ8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLnRpbWVvdXQgPSBmdW5jdGlvbih0aW1lb3V0KSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fdGltZW91dCA9IHRpbWVvdXQ7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX3RpbWVvdXQ7XG4gIH1cbn07XG5cbm1vZHVsZS5leHBvcnRzID0gTW9ja1Jlc3BvbnNlO1xuIiwidmFyIE1vY2tSZXF1ZXN0ICAgPSByZXF1aXJlKCcuL01vY2tSZXF1ZXN0Jyk7XG52YXIgTW9ja1Jlc3BvbnNlICA9IHJlcXVpcmUoJy4vTW9ja1Jlc3BvbnNlJyk7XG5cbnZhciBub3RJbXBsZW1lbnRlZEVycm9yID0gbmV3IEVycm9yKCdUaGlzIGZlYXR1cmUgaGFzblxcJ3QgYmVlbiBpbXBsbWVudGVkIHlldC4gUGxlYXNlIHN1Ym1pdCBhbiBJc3N1ZSBvciBQdWxsIFJlcXVlc3Qgb24gR2l0aHViLicpO1xuXG4vL2h0dHBzOi8vZGV2ZWxvcGVyLm1vemlsbGEub3JnL2VuLVVTL2RvY3MvV2ViL0FQSS9YTUxIdHRwUmVxdWVzdFxuLy9odHRwczovL3hoci5zcGVjLndoYXR3Zy5vcmcvXG4vL2h0dHA6Ly93d3cudzMub3JnL1RSLzIwMDYvV0QtWE1MSHR0cFJlcXVlc3QtMjAwNjA0MDUvXG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9VTlNFTlQgICAgICAgICAgICAgPSAwO1xuTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX09QRU5FRCAgICAgICAgICAgICA9IDE7XG5Nb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfSEVBREVSU19SRUNFSVZFRCAgID0gMjtcbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9MT0FESU5HICAgICAgICAgICAgPSAzO1xuTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0RPTkUgICAgICAgICAgICAgICA9IDQ7XG5cbi8qKlxuICogVGhlIHJlcXVlc3QgaGFuZGxlcnNcbiAqIEBwcml2YXRlXG4gKiBAdHlwZSB7QXJyYXl9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVycyA9IFtdO1xuXG4vKipcbiAqIEFkZCBhIHJlcXVlc3QgaGFuZGxlclxuICogQHBhcmFtICAge2Z1bmN0aW9uKE1vY2tSZXF1ZXN0LCBNb2NrUmVzcG9uc2UpfSBmblxuICogQHJldHVybnMge01vY2tYTUxIdHRwUmVxdWVzdH1cbiAqL1xuTW9ja1hNTEh0dHBSZXF1ZXN0LmFkZEhhbmRsZXIgPSBmdW5jdGlvbihmbikge1xuICBNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMucHVzaChmbik7XG4gIHJldHVybiB0aGlzO1xufTtcblxuLyoqXG4gKiBSZW1vdmUgYSByZXF1ZXN0IGhhbmRsZXJcbiAqIEBwYXJhbSAgIHtmdW5jdGlvbihNb2NrUmVxdWVzdCwgTW9ja1Jlc3BvbnNlKX0gZm5cbiAqIEByZXR1cm5zIHtNb2NrWE1MSHR0cFJlcXVlc3R9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5yZW1vdmVIYW5kbGVyID0gZnVuY3Rpb24oZm4pIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbi8qKlxuICogSGFuZGxlIGEgcmVxdWVzdFxuICogQHBhcmFtICAge01vY2tSZXF1ZXN0fSByZXF1ZXN0XG4gKiBAcmV0dXJucyB7TW9ja1Jlc3BvbnNlfG51bGx9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGUgPSBmdW5jdGlvbihyZXF1ZXN0KSB7XG5cbiAgZm9yICh2YXIgaT0wOyBpPE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVycy5sZW5ndGg7ICsraSkge1xuXG4gICAgLy9nZXQgdGhlIGdlbmVyYXRvciB0byBjcmVhdGUgYSByZXNwb25zZSB0byB0aGUgcmVxdWVzdFxuICAgIHZhciByZXNwb25zZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVyc1tpXShyZXF1ZXN0LCBuZXcgTW9ja1Jlc3BvbnNlKCkpO1xuXG4gICAgaWYgKHJlc3BvbnNlKSB7XG4gICAgICByZXR1cm4gcmVzcG9uc2U7XG4gICAgfVxuXG4gIH1cblxuICByZXR1cm4gbnVsbDtcbn07XG5cbi8qKlxuICogTW9jayBYTUxIdHRwUmVxdWVzdFxuICogQGNvbnN0cnVjdG9yXG4gKi9cbmZ1bmN0aW9uIE1vY2tYTUxIdHRwUmVxdWVzdCgpIHtcbiAgdGhpcy5yZXNldCgpO1xuICB0aGlzLnRpbWVvdXQgPSAwO1xufVxuXG4vKipcbiAqIFJlc2V0IHRoZSByZXNwb25zZSB2YWx1ZXNcbiAqIEBwcml2YXRlXG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUucmVzZXQgPSBmdW5jdGlvbigpIHtcblxuICB0aGlzLl9yZXF1ZXN0SGVhZGVycyAgPSB7fTtcbiAgdGhpcy5fcmVzcG9uc2VIZWFkZXJzID0ge307XG5cbiAgdGhpcy5zdGF0dXMgICAgICAgPSAwO1xuICB0aGlzLnN0YXR1c1RleHQgICA9ICcnO1xuXG4gIHRoaXMucmVzcG9uc2UgICAgID0gbnVsbDtcbiAgdGhpcy5yZXNwb25zZVR5cGUgPSBudWxsO1xuICB0aGlzLnJlc3BvbnNlVGV4dCA9IG51bGw7XG4gIHRoaXMucmVzcG9uc2VYTUwgID0gbnVsbDtcblxuICB0aGlzLnJlYWR5U3RhdGUgICA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9VTlNFTlQ7XG59O1xuXG4vKipcbiAqIFRyaWdnZXIgYW4gZXZlbnRcbiAqIEBwYXJhbSAgIHtTdHJpbmd9IGV2ZW50XG4gKiBAcmV0dXJucyB7TW9ja1hNTEh0dHBSZXF1ZXN0fVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnRyaWdnZXIgPSBmdW5jdGlvbihldmVudCkge1xuXG4gIGlmICh0aGlzLm9ucmVhZHlzdGF0ZWNoYW5nZSkge1xuICAgIHRoaXMub25yZWFkeXN0YXRlY2hhbmdlKCk7XG4gIH1cblxuICBpZiAodGhpc1snb24nK2V2ZW50XSkge1xuICAgIHRoaXNbJ29uJytldmVudF0oKTtcbiAgfVxuXG4gIC8vaXRlcmF0ZSBvdmVyIHRoZSBsaXN0ZW5lcnNcblxuICByZXR1cm4gdGhpcztcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUub3BlbiA9IGZ1bmN0aW9uKG1ldGhvZCwgdXJsLCBhc3luYywgdXNlciwgcGFzc3dvcmQpIHtcbiAgdGhpcy5yZXNldCgpO1xuICB0aGlzLm1ldGhvZCAgID0gbWV0aG9kO1xuICB0aGlzLnVybCAgICAgID0gdXJsO1xuICB0aGlzLmFzeW5jICAgID0gYXN5bmM7XG4gIHRoaXMudXNlciAgICAgPSB1c2VyO1xuICB0aGlzLnBhc3N3b3JkID0gcGFzc3dvcmQ7XG4gIHRoaXMuZGF0YSAgICAgPSBudWxsO1xuICB0aGlzLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfT1BFTkVEO1xufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5zZXRSZXF1ZXN0SGVhZGVyID0gZnVuY3Rpb24obmFtZSwgdmFsdWUpIHtcbiAgdGhpcy5fcmVxdWVzdEhlYWRlcnNbbmFtZV0gPSB2YWx1ZTtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUub3ZlcnJpZGVNaW1lVHlwZSA9IGZ1bmN0aW9uKG1pbWUpIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUuc2VuZCA9IGZ1bmN0aW9uKGRhdGEpIHtcbiAgdmFyIHNlbGYgPSB0aGlzO1xuICB0aGlzLmRhdGEgPSBkYXRhO1xuXG4gIHNlbGYucmVhZHlTdGF0ZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9MT0FESU5HO1xuXG4gIHNlbGYuX3NlbmRUaW1lb3V0ID0gc2V0VGltZW91dChmdW5jdGlvbigpIHtcblxuICAgIHZhciByZXNwb25zZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGUobmV3IE1vY2tSZXF1ZXN0KHNlbGYpKTtcblxuICAgIGlmIChyZXNwb25zZSAmJiByZXNwb25zZSBpbnN0YW5jZW9mIE1vY2tSZXNwb25zZSkge1xuXG4gICAgICB2YXIgdGltZW91dCA9IHJlc3BvbnNlLnRpbWVvdXQoKTtcblxuICAgICAgaWYgKHRpbWVvdXQpIHtcblxuICAgICAgICAvL3RyaWdnZXIgYSB0aW1lb3V0IGV2ZW50IGJlY2F1c2UgdGhlIHJlcXVlc3QgdGltZWQgb3V0IC0gd2FpdCBmb3IgdGhlIHRpbWVvdXQgdGltZSBiZWNhdXNlIG1hbnkgbGlicyBsaWtlIGpxdWVyeSBhbmQgc3VwZXJhZ2VudCB1c2Ugc2V0VGltZW91dCB0byBkZXRlY3QgdGhlIGVycm9yIHR5cGVcbiAgICAgICAgc2VsZi5fc2VuZFRpbWVvdXQgPSBzZXRUaW1lb3V0KGZ1bmN0aW9uKCkge1xuICAgICAgICAgIHNlbGYucmVhZHlTdGF0ZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FO1xuICAgICAgICAgIHNlbGYudHJpZ2dlcigndGltZW91dCcpO1xuICAgICAgICB9LCB0eXBlb2YodGltZW91dCkgPT09ICdudW1iZXInID8gdGltZW91dCA6IHNlbGYudGltZW91dCsxKTtcblxuICAgICAgfSBlbHNlIHtcblxuICAgICAgICAvL21hcCB0aGUgcmVzcG9uc2UgdG8gdGhlIFhIUiBvYmplY3RcbiAgICAgICAgc2VsZi5zdGF0dXMgICAgICAgICAgICAgPSByZXNwb25zZS5zdGF0dXMoKTtcbiAgICAgICAgc2VsZi5fcmVzcG9uc2VIZWFkZXJzICAgPSByZXNwb25zZS5oZWFkZXJzKCk7XG4gICAgICAgIHNlbGYucmVzcG9uc2VUeXBlICAgICAgID0gJ3RleHQnO1xuICAgICAgICBzZWxmLnJlc3BvbnNlICAgICAgICAgICA9IHJlc3BvbnNlLmJvZHkoKTtcbiAgICAgICAgc2VsZi5yZXNwb25zZVRleHQgICAgICAgPSByZXNwb25zZS5ib2R5KCk7IC8vVE9ETzogZGV0ZWN0IGFuIG9iamVjdCBhbmQgcmV0dXJuIEpTT04sIGRldGVjdCBYTUwgYW5kIHJldHVybiBYTUxcbiAgICAgICAgc2VsZi5yZWFkeVN0YXRlICAgICAgICAgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfRE9ORTtcblxuICAgICAgICAvL3RyaWdnZXIgYSBsb2FkIGV2ZW50IGJlY2F1c2UgdGhlIHJlcXVlc3Qgd2FzIHJlY2VpdmVkXG4gICAgICAgIHNlbGYudHJpZ2dlcignbG9hZCcpO1xuXG4gICAgICB9XG5cbiAgICB9IGVsc2Uge1xuXG4gICAgICAvL3RyaWdnZXIgYW4gZXJyb3IgYmVjYXVzZSB0aGUgcmVxdWVzdCB3YXMgbm90IGhhbmRsZWRcbiAgICAgIHNlbGYucmVhZHlTdGF0ZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FO1xuICAgICAgc2VsZi50cmlnZ2VyKCdlcnJvcicpO1xuXG4gICAgfVxuXG4gIH0sIDApO1xuXG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmFib3J0ID0gZnVuY3Rpb24oKSB7XG4gIGNsZWFyVGltZW91dCh0aGlzLl9zZW5kVGltZW91dCk7XG5cbiAgaWYgKHRoaXMucmVhZHlTdGF0ZSA+IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9VTlNFTlQgJiYgdGhpcy5yZWFkeVN0YXRlIDwgTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0RPTkUpIHtcbiAgICB0aGlzLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UO1xuICAgIHRoaXMudHJpZ2dlcignYWJvcnQnKTtcbiAgfVxuXG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmdldEFsbFJlc3BvbnNlSGVhZGVycyA9IGZ1bmN0aW9uKCkge1xuXG4gIGlmICh0aGlzLnJlYWR5U3RhdGUgPCBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfSEVBREVSU19SRUNFSVZFRCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG5cbiAgdmFyIGhlYWRlcnMgPSAnJztcbiAgZm9yICh2YXIgbmFtZSBpbiB0aGlzLl9yZXNwb25zZUhlYWRlcnMpIHtcbiAgICBpZiAodGhpcy5fcmVzcG9uc2VIZWFkZXJzLmhhc093blByb3BlcnR5KG5hbWUpKSB7XG4gICAgICBoZWFkZXJzICs9IG5hbWUrJzogJyt0aGlzLl9yZXNwb25zZUhlYWRlcnNbbmFtZV0rJ1xcclxcbic7XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIGhlYWRlcnM7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmdldFJlc3BvbnNlSGVhZGVyID0gZnVuY3Rpb24obmFtZSkge1xuXG4gIGlmICh0aGlzLnJlYWR5U3RhdGUgPCBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfSEVBREVSU19SRUNFSVZFRCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG5cbiAgcmV0dXJuIHRoaXMuX3Jlc3BvbnNlSGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldIHx8IG51bGw7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmFkZEV2ZW50TGlzdGVuZXIgPSBmdW5jdGlvbihldmVudCwgbGlzdGVuZXIpIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUucmVtb3ZlRXZlbnRMaXN0ZW5lciA9IGZ1bmN0aW9uKGV2ZW50LCBsaXN0ZW5lcikge1xuICB0aHJvdyBub3RJbXBsZW1lbnRlZEVycm9yO1xufTtcblxubW9kdWxlLmV4cG9ydHMgPSBNb2NrWE1MSHR0cFJlcXVlc3Q7XG4iLCJcInVzZSBzdHJpY3RcIjtcbnZhciB3aW5kb3cgPSByZXF1aXJlKFwiZ2xvYmFsL3dpbmRvd1wiKVxudmFyIGlzRnVuY3Rpb24gPSByZXF1aXJlKFwiaXMtZnVuY3Rpb25cIilcbnZhciBwYXJzZUhlYWRlcnMgPSByZXF1aXJlKFwicGFyc2UtaGVhZGVyc1wiKVxudmFyIHh0ZW5kID0gcmVxdWlyZShcInh0ZW5kXCIpXG5cbm1vZHVsZS5leHBvcnRzID0gY3JlYXRlWEhSXG5jcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QgPSB3aW5kb3cuWE1MSHR0cFJlcXVlc3QgfHwgbm9vcFxuY3JlYXRlWEhSLlhEb21haW5SZXF1ZXN0ID0gXCJ3aXRoQ3JlZGVudGlhbHNcIiBpbiAobmV3IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCgpKSA/IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCA6IHdpbmRvdy5YRG9tYWluUmVxdWVzdFxuXG5mb3JFYWNoQXJyYXkoW1wiZ2V0XCIsIFwicHV0XCIsIFwicG9zdFwiLCBcInBhdGNoXCIsIFwiaGVhZFwiLCBcImRlbGV0ZVwiXSwgZnVuY3Rpb24obWV0aG9kKSB7XG4gICAgY3JlYXRlWEhSW21ldGhvZCA9PT0gXCJkZWxldGVcIiA/IFwiZGVsXCIgOiBtZXRob2RdID0gZnVuY3Rpb24odXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgICAgICBvcHRpb25zID0gaW5pdFBhcmFtcyh1cmksIG9wdGlvbnMsIGNhbGxiYWNrKVxuICAgICAgICBvcHRpb25zLm1ldGhvZCA9IG1ldGhvZC50b1VwcGVyQ2FzZSgpXG4gICAgICAgIHJldHVybiBfY3JlYXRlWEhSKG9wdGlvbnMpXG4gICAgfVxufSlcblxuZnVuY3Rpb24gZm9yRWFjaEFycmF5KGFycmF5LCBpdGVyYXRvcikge1xuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgYXJyYXkubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgaXRlcmF0b3IoYXJyYXlbaV0pXG4gICAgfVxufVxuXG5mdW5jdGlvbiBpc0VtcHR5KG9iail7XG4gICAgZm9yKHZhciBpIGluIG9iail7XG4gICAgICAgIGlmKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkgcmV0dXJuIGZhbHNlXG4gICAgfVxuICAgIHJldHVybiB0cnVlXG59XG5cbmZ1bmN0aW9uIGluaXRQYXJhbXModXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgIHZhciBwYXJhbXMgPSB1cmlcblxuICAgIGlmIChpc0Z1bmN0aW9uKG9wdGlvbnMpKSB7XG4gICAgICAgIGNhbGxiYWNrID0gb3B0aW9uc1xuICAgICAgICBpZiAodHlwZW9mIHVyaSA9PT0gXCJzdHJpbmdcIikge1xuICAgICAgICAgICAgcGFyYW1zID0ge3VyaTp1cml9XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICBwYXJhbXMgPSB4dGVuZChvcHRpb25zLCB7dXJpOiB1cml9KVxuICAgIH1cblxuICAgIHBhcmFtcy5jYWxsYmFjayA9IGNhbGxiYWNrXG4gICAgcmV0dXJuIHBhcmFtc1xufVxuXG5mdW5jdGlvbiBjcmVhdGVYSFIodXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgIG9wdGlvbnMgPSBpbml0UGFyYW1zKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spXG4gICAgcmV0dXJuIF9jcmVhdGVYSFIob3B0aW9ucylcbn1cblxuZnVuY3Rpb24gX2NyZWF0ZVhIUihvcHRpb25zKSB7XG4gICAgaWYodHlwZW9mIG9wdGlvbnMuY2FsbGJhY2sgPT09IFwidW5kZWZpbmVkXCIpe1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3IoXCJjYWxsYmFjayBhcmd1bWVudCBtaXNzaW5nXCIpXG4gICAgfVxuXG4gICAgdmFyIGNhbGxlZCA9IGZhbHNlXG4gICAgdmFyIGNhbGxiYWNrID0gZnVuY3Rpb24gY2JPbmNlKGVyciwgcmVzcG9uc2UsIGJvZHkpe1xuICAgICAgICBpZighY2FsbGVkKXtcbiAgICAgICAgICAgIGNhbGxlZCA9IHRydWVcbiAgICAgICAgICAgIG9wdGlvbnMuY2FsbGJhY2soZXJyLCByZXNwb25zZSwgYm9keSlcbiAgICAgICAgfVxuICAgIH1cblxuICAgIGZ1bmN0aW9uIHJlYWR5c3RhdGVjaGFuZ2UoKSB7XG4gICAgICAgIGlmICh4aHIucmVhZHlTdGF0ZSA9PT0gNCkge1xuICAgICAgICAgICAgbG9hZEZ1bmMoKVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gZ2V0Qm9keSgpIHtcbiAgICAgICAgLy8gQ2hyb21lIHdpdGggcmVxdWVzdFR5cGU9YmxvYiB0aHJvd3MgZXJyb3JzIGFycm91bmQgd2hlbiBldmVuIHRlc3RpbmcgYWNjZXNzIHRvIHJlc3BvbnNlVGV4dFxuICAgICAgICB2YXIgYm9keSA9IHVuZGVmaW5lZFxuXG4gICAgICAgIGlmICh4aHIucmVzcG9uc2UpIHtcbiAgICAgICAgICAgIGJvZHkgPSB4aHIucmVzcG9uc2VcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGJvZHkgPSB4aHIucmVzcG9uc2VUZXh0IHx8IGdldFhtbCh4aHIpXG4gICAgICAgIH1cblxuICAgICAgICBpZiAoaXNKc29uKSB7XG4gICAgICAgICAgICB0cnkge1xuICAgICAgICAgICAgICAgIGJvZHkgPSBKU09OLnBhcnNlKGJvZHkpXG4gICAgICAgICAgICB9IGNhdGNoIChlKSB7fVxuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGJvZHlcbiAgICB9XG5cbiAgICB2YXIgZmFpbHVyZVJlc3BvbnNlID0ge1xuICAgICAgICAgICAgICAgIGJvZHk6IHVuZGVmaW5lZCxcbiAgICAgICAgICAgICAgICBoZWFkZXJzOiB7fSxcbiAgICAgICAgICAgICAgICBzdGF0dXNDb2RlOiAwLFxuICAgICAgICAgICAgICAgIG1ldGhvZDogbWV0aG9kLFxuICAgICAgICAgICAgICAgIHVybDogdXJpLFxuICAgICAgICAgICAgICAgIHJhd1JlcXVlc3Q6IHhoclxuICAgICAgICAgICAgfVxuXG4gICAgZnVuY3Rpb24gZXJyb3JGdW5jKGV2dCkge1xuICAgICAgICBjbGVhclRpbWVvdXQodGltZW91dFRpbWVyKVxuICAgICAgICBpZighKGV2dCBpbnN0YW5jZW9mIEVycm9yKSl7XG4gICAgICAgICAgICBldnQgPSBuZXcgRXJyb3IoXCJcIiArIChldnQgfHwgXCJVbmtub3duIFhNTEh0dHBSZXF1ZXN0IEVycm9yXCIpIClcbiAgICAgICAgfVxuICAgICAgICBldnQuc3RhdHVzQ29kZSA9IDBcbiAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGV2dCwgZmFpbHVyZVJlc3BvbnNlKVxuICAgIH1cblxuICAgIC8vIHdpbGwgbG9hZCB0aGUgZGF0YSAmIHByb2Nlc3MgdGhlIHJlc3BvbnNlIGluIGEgc3BlY2lhbCByZXNwb25zZSBvYmplY3RcbiAgICBmdW5jdGlvbiBsb2FkRnVuYygpIHtcbiAgICAgICAgaWYgKGFib3J0ZWQpIHJldHVyblxuICAgICAgICB2YXIgc3RhdHVzXG4gICAgICAgIGNsZWFyVGltZW91dCh0aW1lb3V0VGltZXIpXG4gICAgICAgIGlmKG9wdGlvbnMudXNlWERSICYmIHhoci5zdGF0dXM9PT11bmRlZmluZWQpIHtcbiAgICAgICAgICAgIC8vSUU4IENPUlMgR0VUIHN1Y2Nlc3NmdWwgcmVzcG9uc2UgZG9lc24ndCBoYXZlIGEgc3RhdHVzIGZpZWxkLCBidXQgYm9keSBpcyBmaW5lXG4gICAgICAgICAgICBzdGF0dXMgPSAyMDBcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHN0YXR1cyA9ICh4aHIuc3RhdHVzID09PSAxMjIzID8gMjA0IDogeGhyLnN0YXR1cylcbiAgICAgICAgfVxuICAgICAgICB2YXIgcmVzcG9uc2UgPSBmYWlsdXJlUmVzcG9uc2VcbiAgICAgICAgdmFyIGVyciA9IG51bGxcblxuICAgICAgICBpZiAoc3RhdHVzICE9PSAwKXtcbiAgICAgICAgICAgIHJlc3BvbnNlID0ge1xuICAgICAgICAgICAgICAgIGJvZHk6IGdldEJvZHkoKSxcbiAgICAgICAgICAgICAgICBzdGF0dXNDb2RlOiBzdGF0dXMsXG4gICAgICAgICAgICAgICAgbWV0aG9kOiBtZXRob2QsXG4gICAgICAgICAgICAgICAgaGVhZGVyczoge30sXG4gICAgICAgICAgICAgICAgdXJsOiB1cmksXG4gICAgICAgICAgICAgICAgcmF3UmVxdWVzdDogeGhyXG4gICAgICAgICAgICB9XG4gICAgICAgICAgICBpZih4aHIuZ2V0QWxsUmVzcG9uc2VIZWFkZXJzKXsgLy9yZW1lbWJlciB4aHIgY2FuIGluIGZhY3QgYmUgWERSIGZvciBDT1JTIGluIElFXG4gICAgICAgICAgICAgICAgcmVzcG9uc2UuaGVhZGVycyA9IHBhcnNlSGVhZGVycyh4aHIuZ2V0QWxsUmVzcG9uc2VIZWFkZXJzKCkpXG4gICAgICAgICAgICB9XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBlcnIgPSBuZXcgRXJyb3IoXCJJbnRlcm5hbCBYTUxIdHRwUmVxdWVzdCBFcnJvclwiKVxuICAgICAgICB9XG4gICAgICAgIHJldHVybiBjYWxsYmFjayhlcnIsIHJlc3BvbnNlLCByZXNwb25zZS5ib2R5KVxuICAgIH1cblxuICAgIHZhciB4aHIgPSBvcHRpb25zLnhociB8fCBudWxsXG5cbiAgICBpZiAoIXhocikge1xuICAgICAgICBpZiAob3B0aW9ucy5jb3JzIHx8IG9wdGlvbnMudXNlWERSKSB7XG4gICAgICAgICAgICB4aHIgPSBuZXcgY3JlYXRlWEhSLlhEb21haW5SZXF1ZXN0KClcbiAgICAgICAgfWVsc2V7XG4gICAgICAgICAgICB4aHIgPSBuZXcgY3JlYXRlWEhSLlhNTEh0dHBSZXF1ZXN0KClcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBrZXlcbiAgICB2YXIgYWJvcnRlZFxuICAgIHZhciB1cmkgPSB4aHIudXJsID0gb3B0aW9ucy51cmkgfHwgb3B0aW9ucy51cmxcbiAgICB2YXIgbWV0aG9kID0geGhyLm1ldGhvZCA9IG9wdGlvbnMubWV0aG9kIHx8IFwiR0VUXCJcbiAgICB2YXIgYm9keSA9IG9wdGlvbnMuYm9keSB8fCBvcHRpb25zLmRhdGEgfHwgbnVsbFxuICAgIHZhciBoZWFkZXJzID0geGhyLmhlYWRlcnMgPSBvcHRpb25zLmhlYWRlcnMgfHwge31cbiAgICB2YXIgc3luYyA9ICEhb3B0aW9ucy5zeW5jXG4gICAgdmFyIGlzSnNvbiA9IGZhbHNlXG4gICAgdmFyIHRpbWVvdXRUaW1lclxuXG4gICAgaWYgKFwianNvblwiIGluIG9wdGlvbnMpIHtcbiAgICAgICAgaXNKc29uID0gdHJ1ZVxuICAgICAgICBoZWFkZXJzW1wiYWNjZXB0XCJdIHx8IGhlYWRlcnNbXCJBY2NlcHRcIl0gfHwgKGhlYWRlcnNbXCJBY2NlcHRcIl0gPSBcImFwcGxpY2F0aW9uL2pzb25cIikgLy9Eb24ndCBvdmVycmlkZSBleGlzdGluZyBhY2NlcHQgaGVhZGVyIGRlY2xhcmVkIGJ5IHVzZXJcbiAgICAgICAgaWYgKG1ldGhvZCAhPT0gXCJHRVRcIiAmJiBtZXRob2QgIT09IFwiSEVBRFwiKSB7XG4gICAgICAgICAgICBoZWFkZXJzW1wiY29udGVudC10eXBlXCJdIHx8IGhlYWRlcnNbXCJDb250ZW50LVR5cGVcIl0gfHwgKGhlYWRlcnNbXCJDb250ZW50LVR5cGVcIl0gPSBcImFwcGxpY2F0aW9uL2pzb25cIikgLy9Eb24ndCBvdmVycmlkZSBleGlzdGluZyBhY2NlcHQgaGVhZGVyIGRlY2xhcmVkIGJ5IHVzZXJcbiAgICAgICAgICAgIGJvZHkgPSBKU09OLnN0cmluZ2lmeShvcHRpb25zLmpzb24pXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB4aHIub25yZWFkeXN0YXRlY2hhbmdlID0gcmVhZHlzdGF0ZWNoYW5nZVxuICAgIHhoci5vbmxvYWQgPSBsb2FkRnVuY1xuICAgIHhoci5vbmVycm9yID0gZXJyb3JGdW5jXG4gICAgLy8gSUU5IG11c3QgaGF2ZSBvbnByb2dyZXNzIGJlIHNldCB0byBhIHVuaXF1ZSBmdW5jdGlvbi5cbiAgICB4aHIub25wcm9ncmVzcyA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgLy8gSUUgbXVzdCBkaWVcbiAgICB9XG4gICAgeGhyLm9udGltZW91dCA9IGVycm9yRnVuY1xuICAgIHhoci5vcGVuKG1ldGhvZCwgdXJpLCAhc3luYywgb3B0aW9ucy51c2VybmFtZSwgb3B0aW9ucy5wYXNzd29yZClcbiAgICAvL2hhcyB0byBiZSBhZnRlciBvcGVuXG4gICAgaWYoIXN5bmMpIHtcbiAgICAgICAgeGhyLndpdGhDcmVkZW50aWFscyA9ICEhb3B0aW9ucy53aXRoQ3JlZGVudGlhbHNcbiAgICB9XG4gICAgLy8gQ2Fubm90IHNldCB0aW1lb3V0IHdpdGggc3luYyByZXF1ZXN0XG4gICAgLy8gbm90IHNldHRpbmcgdGltZW91dCBvbiB0aGUgeGhyIG9iamVjdCwgYmVjYXVzZSBvZiBvbGQgd2Via2l0cyBldGMuIG5vdCBoYW5kbGluZyB0aGF0IGNvcnJlY3RseVxuICAgIC8vIGJvdGggbnBtJ3MgcmVxdWVzdCBhbmQganF1ZXJ5IDEueCB1c2UgdGhpcyBraW5kIG9mIHRpbWVvdXQsIHNvIHRoaXMgaXMgYmVpbmcgY29uc2lzdGVudFxuICAgIGlmICghc3luYyAmJiBvcHRpb25zLnRpbWVvdXQgPiAwICkge1xuICAgICAgICB0aW1lb3V0VGltZXIgPSBzZXRUaW1lb3V0KGZ1bmN0aW9uKCl7XG4gICAgICAgICAgICBhYm9ydGVkPXRydWUvL0lFOSBtYXkgc3RpbGwgY2FsbCByZWFkeXN0YXRlY2hhbmdlXG4gICAgICAgICAgICB4aHIuYWJvcnQoXCJ0aW1lb3V0XCIpXG4gICAgICAgICAgICB2YXIgZSA9IG5ldyBFcnJvcihcIlhNTEh0dHBSZXF1ZXN0IHRpbWVvdXRcIilcbiAgICAgICAgICAgIGUuY29kZSA9IFwiRVRJTUVET1VUXCJcbiAgICAgICAgICAgIGVycm9yRnVuYyhlKVxuICAgICAgICB9LCBvcHRpb25zLnRpbWVvdXQgKVxuICAgIH1cblxuICAgIGlmICh4aHIuc2V0UmVxdWVzdEhlYWRlcikge1xuICAgICAgICBmb3Ioa2V5IGluIGhlYWRlcnMpe1xuICAgICAgICAgICAgaWYoaGVhZGVycy5oYXNPd25Qcm9wZXJ0eShrZXkpKXtcbiAgICAgICAgICAgICAgICB4aHIuc2V0UmVxdWVzdEhlYWRlcihrZXksIGhlYWRlcnNba2V5XSlcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH0gZWxzZSBpZiAob3B0aW9ucy5oZWFkZXJzICYmICFpc0VtcHR5KG9wdGlvbnMuaGVhZGVycykpIHtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKFwiSGVhZGVycyBjYW5ub3QgYmUgc2V0IG9uIGFuIFhEb21haW5SZXF1ZXN0IG9iamVjdFwiKVxuICAgIH1cblxuICAgIGlmIChcInJlc3BvbnNlVHlwZVwiIGluIG9wdGlvbnMpIHtcbiAgICAgICAgeGhyLnJlc3BvbnNlVHlwZSA9IG9wdGlvbnMucmVzcG9uc2VUeXBlXG4gICAgfVxuXG4gICAgaWYgKFwiYmVmb3JlU2VuZFwiIGluIG9wdGlvbnMgJiZcbiAgICAgICAgdHlwZW9mIG9wdGlvbnMuYmVmb3JlU2VuZCA9PT0gXCJmdW5jdGlvblwiXG4gICAgKSB7XG4gICAgICAgIG9wdGlvbnMuYmVmb3JlU2VuZCh4aHIpXG4gICAgfVxuXG4gICAgeGhyLnNlbmQoYm9keSlcblxuICAgIHJldHVybiB4aHJcblxuXG59XG5cbmZ1bmN0aW9uIGdldFhtbCh4aHIpIHtcbiAgICBpZiAoeGhyLnJlc3BvbnNlVHlwZSA9PT0gXCJkb2N1bWVudFwiKSB7XG4gICAgICAgIHJldHVybiB4aHIucmVzcG9uc2VYTUxcbiAgICB9XG4gICAgdmFyIGZpcmVmb3hCdWdUYWtlbkVmZmVjdCA9IHhoci5zdGF0dXMgPT09IDIwNCAmJiB4aHIucmVzcG9uc2VYTUwgJiYgeGhyLnJlc3BvbnNlWE1MLmRvY3VtZW50RWxlbWVudC5ub2RlTmFtZSA9PT0gXCJwYXJzZXJlcnJvclwiXG4gICAgaWYgKHhoci5yZXNwb25zZVR5cGUgPT09IFwiXCIgJiYgIWZpcmVmb3hCdWdUYWtlbkVmZmVjdCkge1xuICAgICAgICByZXR1cm4geGhyLnJlc3BvbnNlWE1MXG4gICAgfVxuXG4gICAgcmV0dXJuIG51bGxcbn1cblxuZnVuY3Rpb24gbm9vcCgpIHt9XG4iLCJtb2R1bGUuZXhwb3J0cyA9IGV4dGVuZFxuXG52YXIgaGFzT3duUHJvcGVydHkgPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5O1xuXG5mdW5jdGlvbiBleHRlbmQoKSB7XG4gICAgdmFyIHRhcmdldCA9IHt9XG5cbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykge1xuICAgICAgICB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldXG5cbiAgICAgICAgZm9yICh2YXIga2V5IGluIHNvdXJjZSkge1xuICAgICAgICAgICAgaWYgKGhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7XG4gICAgICAgICAgICAgICAgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XVxuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIHRhcmdldFxufVxuIiwiLyplc2xpbnQgbm8tc2hhZG93OiBbMiwgeyBcImFsbG93XCI6IFtcImRpc3BhdGNoXCJdIH1dKi9cbmltcG9ydCB4aHIgZnJvbSBcInhoclwiO1xuaW1wb3J0IG1hcHBpbmdUb0pzb25MZFJtbCBmcm9tIFwiLi91dGlsL21hcHBpbmdUb0pzb25MZFJtbFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbiBhY3Rpb25zTWFrZXIobmF2aWdhdGVUbywgZGlzcGF0Y2gpIHtcblx0Ly9iaW5kIHRvIHZhcmlhYmxlIHNvIGFuIGFjdGlvbiBjYW4gdHJpZ2dlciBvdGhlciBhY3Rpb25zXG5cdGxldCBhY3Rpb25zID0ge1xuXHRcdG9uVG9rZW46IGZ1bmN0aW9uICh0b2tlbikge1xuXHRcdFx0eGhyKHByb2Nlc3MuZW52LnNlcnZlciArIFwiL3YyLjEvc3lzdGVtL3VzZXJzL21lL3ZyZXNcIiwge1xuXHRcdFx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcdFx0XCJBdXRob3JpemF0aW9uXCI6IHRva2VuXG5cdFx0XHRcdH1cblx0XHRcdH0sIChlcnIsIHJlc3AsIGJvZHkpID0+IHtcblx0XHRcdFx0Y29uc3QgdnJlRGF0YSA9IEpTT04ucGFyc2UoYm9keSk7XG5cdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkxPR0lOXCIsIGRhdGE6IHRva2VuLCB2cmVEYXRhOiB2cmVEYXRhfSk7XG5cdFx0XHRcdGlmICh2cmVEYXRhLm1pbmUpIHtcblx0XHRcdFx0XHRuYXZpZ2F0ZVRvKFwiY29sbGVjdGlvbnNPdmVydmlld1wiKTtcblx0XHRcdFx0fVxuXHRcdFx0fSk7XG5cdFx0fSxcblx0XHRvbkxvYWRNb3JlQ2xpY2s6IGZ1bmN0aW9uICh1cmwsIGNvbGxlY3Rpb24pIHtcblx0XHRcdGRpc3BhdGNoKChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0XHRcdFx0dmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdFx0dmFyIHBheWxvYWQgPSB7XG5cdFx0XHRcdFx0aGVhZGVyczoge1xuXHRcdFx0XHRcdFx0XCJBdXRob3JpemF0aW9uXCI6IHN0YXRlLnVzZXJkYXRhLnVzZXJJZFxuXHRcdFx0XHRcdH1cblx0XHRcdFx0fTtcblx0XHRcdFx0eGhyLmdldCh1cmwsIHBheWxvYWQsIGZ1bmN0aW9uIChlcnIsIHJlc3AsIGJvZHkpIHtcblx0XHRcdFx0XHRpZiAoZXJyKSB7XG5cdFx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRVJST1JcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgZXJyb3I6IGVyciB9KVxuXHRcdFx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdFx0XHR0cnkge1xuXHRcdFx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfU1VDQ0VFREVEXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGRhdGE6IEpTT04ucGFyc2UoYm9keSl9KTtcblx0XHRcdFx0XHRcdH0gY2F0Y2goZSkge1xuXHRcdFx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRVJST1JcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgZXJyb3I6IGUgfSlcblx0XHRcdFx0XHRcdH1cblx0XHRcdFx0XHR9XG5cdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0ZJTklTSEVEXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb259KVxuXHRcdFx0XHR9KTtcblx0XHRcdH0pXG5cblx0XHR9LFxuXHRcdG9uVXBsb2FkRmlsZVNlbGVjdDogZnVuY3Rpb24gKGZpbGVzLCBpc1JldXBsb2FkID0gZmFsc2UpIHtcblx0XHRcdGxldCBmaWxlID0gZmlsZXNbMF07XG5cdFx0XHRsZXQgZm9ybURhdGEgPSBuZXcgRm9ybURhdGEoKTtcblx0XHRcdGZvcm1EYXRhLmFwcGVuZChcImZpbGVcIiwgZmlsZSk7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTVEFSVF9VUExPQURcIn0pXG5cdFx0XHRkaXNwYXRjaChmdW5jdGlvbiAoZGlzcGF0Y2gsIGdldFN0YXRlKSB7XG5cdFx0XHRcdHZhciBzdGF0ZSA9IGdldFN0YXRlKCk7XG5cdFx0XHRcdHZhciBwYXlsb2FkID0ge1xuXHRcdFx0XHRcdGJvZHk6IGZvcm1EYXRhLFxuXHRcdFx0XHRcdGhlYWRlcnM6IHtcblx0XHRcdFx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcblx0XHRcdFx0XHR9XG5cdFx0XHRcdH07XG5cdFx0XHRcdHhoci5wb3N0KHByb2Nlc3MuZW52LnNlcnZlciArIFwiL3YyLjEvYnVsay11cGxvYWRcIiwgcGF5bG9hZCwgZnVuY3Rpb24gKGVyciwgcmVzcCkge1xuXHRcdFx0XHRcdGxldCBsb2NhdGlvbiA9IHJlc3AuaGVhZGVycy5sb2NhdGlvbjtcblx0XHRcdFx0XHR4aHIuZ2V0KGxvY2F0aW9uLCB7aGVhZGVyczoge1wiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWR9fSwgZnVuY3Rpb24gKGVyciwgcmVzcCwgYm9keSkge1xuXHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiRklOSVNIX1VQTE9BRFwiLCBkYXRhOiBKU09OLnBhcnNlKGJvZHkpfSk7XG5cdFx0XHRcdFx0XHRpZiAoaXNSZXVwbG9hZCkge1xuXHRcdFx0XHRcdFx0XHRhY3Rpb25zLm9uU2VsZWN0Q29sbGVjdGlvbihzdGF0ZS5pbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24pO1xuXHRcdFx0XHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0XHRcdFx0bmF2aWdhdGVUbyhcIm1hcEFyY2hldHlwZXNcIik7XG5cdFx0XHRcdFx0XHR9XG5cdFx0XHRcdFx0fSk7XG5cdFx0XHRcdH0pO1xuXHRcdFx0fSk7XG5cdFx0fSxcblx0XHRvbkNvbnRpbnVlTWFwcGluZzogZnVuY3Rpb24gKHZyZUlkKSB7XG5cdFx0XHRkaXNwYXRjaChmdW5jdGlvbihkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcblx0XHRcdFx0dmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdFx0eGhyLmdldChzdGF0ZS51c2VyZGF0YS5teVZyZXNbdnJlSWRdLnJtbFVyaSwge2hlYWRlcnM6IHtcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkfX0sIGZ1bmN0aW9uIChlcnIsIHJlc3AsIGJvZHkpIHtcblx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJGSU5JU0hfVVBMT0FEXCIsIGRhdGE6IEpTT04ucGFyc2UoYm9keSl9KTtcblx0XHRcdFx0XHRuYXZpZ2F0ZVRvKFwibWFwQXJjaGV0eXBlc1wiKTtcblx0XHRcdFx0fSk7XG5cdFx0XHR9KTtcblx0XHR9LFxuLypcdFx0b25TYXZlTWFwcGluZ3M6IGZ1bmN0aW9uICgpIHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNBVkVfU1RBUlRFRFwifSlcblx0XHRcdGRpc3BhdGNoKGZ1bmN0aW9uIChkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcblx0XHRcdFx0dmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdFx0dmFyIHBheWxvYWQgPSB7XG5cdFx0XHRcdFx0anNvbjogbWFwcGluZ1RvSnNvbkxkUm1sKHN0YXRlLm1hcHBpbmdzLCBzdGF0ZS5pbXBvcnREYXRhLnZyZSksXG5cdFx0XHRcdFx0aGVhZGVyczoge1xuXHRcdFx0XHRcdFx0XCJBdXRob3JpemF0aW9uXCI6IHN0YXRlLnVzZXJkYXRhLnVzZXJJZFxuXHRcdFx0XHRcdH1cblx0XHRcdFx0fTtcblxuXHRcdFx0XHR4aHIucG9zdChzdGF0ZS5pbXBvcnREYXRhLnNhdmVNYXBwaW5nVXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwKSB7XG5cdFx0XHRcdFx0aWYgKGVycikge1xuXHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9IQURfRVJST1JcIn0pXG5cdFx0XHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNBVkVfU1VDQ0VFREVEXCJ9KVxuXHRcdFx0XHRcdH1cblx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTQVZFX0ZJTklTSEVEXCJ9KVxuXHRcdFx0XHR9KTtcblx0XHRcdH0pO1xuXHRcdH0sKi9cblxuXHRcdG9uUHVibGlzaERhdGE6IGZ1bmN0aW9uICgpe1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9TVEFSVEVEXCJ9KTtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNBVkVfRklOSVNIRURcIn0pO1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9TVEFSVEVEXCJ9KTtcblx0XHRcdGRpc3BhdGNoKGZ1bmN0aW9uIChkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcblx0XHRcdFx0dmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdFx0dmFyIHBheWxvYWQgPSB7XG5cdFx0XHRcdFx0Ym9keTogSlNPTi5zdHJpbmdpZnkobWFwcGluZ1RvSnNvbkxkUm1sKHN0YXRlLm1hcHBpbmdzLCBzdGF0ZS5pbXBvcnREYXRhLnZyZSkpLFxuXHRcdFx0XHRcdGhlYWRlcnM6IHtcblx0XHRcdFx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWQsXG4gICAgICAgICAgICBcIkNvbnRlbnQtdHlwZVwiOiBcImFwcGxpY2F0aW9uL2xkK2pzb25cIlxuXHRcdFx0XHRcdH1cblx0XHRcdFx0fTtcblxuXHRcdFx0XHR4aHIucG9zdChzdGF0ZS5pbXBvcnREYXRhLmV4ZWN1dGVNYXBwaW5nVXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwKSB7XG5cdFx0XHRcdFx0aWYgKGVycikge1xuXHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9IQURfRVJST1JcIn0pXG5cdFx0XHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0XHRcdGlmIChKU09OLnBhcnNlKHJlc3AuYm9keSkuc3VjY2Vzcykge1xuXHRcdFx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX1NVQ0NFRURFRFwifSk7XG5cdFx0XHRcdFx0XHRcdGFjdGlvbnMub25Ub2tlbihzdGF0ZS51c2VyZGF0YS51c2VySWQpO1xuXHRcdFx0XHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9IQURfRVJST1JcIn0pO1xuXHRcdFx0XHRcdFx0XHRhY3Rpb25zLm9uU2VsZWN0Q29sbGVjdGlvbihzdGF0ZS5pbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24pO1xuXHRcdFx0XHRcdFx0fVxuXHRcdFx0XHRcdH1cblx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX0ZJTklTSEVEXCJ9KTtcblx0XHRcdFx0fSk7XG5cdFx0XHR9KTtcblx0XHR9LFxuXG5cdFx0b25TZWxlY3RDb2xsZWN0aW9uOiAoY29sbGVjdGlvbikgPT4ge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0FDVElWRV9DT0xMRUNUSU9OXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb259KTtcblx0XHRcdGRpc3BhdGNoKGZ1bmN0aW9uIChkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcblx0XHRcdFx0dmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdFx0dmFyIGN1cnJlbnRTaGVldCA9IHN0YXRlLmltcG9ydERhdGEuc2hlZXRzLmZpbmQoeCA9PiB4LmNvbGxlY3Rpb24gPT09IGNvbGxlY3Rpb24pO1xuXHRcdFx0XHRpZiAoY3VycmVudFNoZWV0LnJvd3MubGVuZ3RoID09PSAwICYmIGN1cnJlbnRTaGVldC5uZXh0VXJsICYmICFjdXJyZW50U2hlZXQuaXNMb2FkaW5nKSB7XG5cdFx0XHRcdFx0dmFyIHBheWxvYWQgPSB7XG5cdFx0XHRcdFx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcdFx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcblx0XHRcdFx0XHRcdH1cblx0XHRcdFx0XHR9O1xuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR1wiIH0pXG5cdFx0XHRcdFx0eGhyLmdldChjdXJyZW50U2hlZXQubmV4dFVybCwgcGF5bG9hZCwgZnVuY3Rpb24gKGVyciwgcmVzcCwgYm9keSkge1xuXHRcdFx0XHRcdFx0aWYgKGVycikge1xuXHRcdFx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRVJST1JcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgZXJyb3I6IGVyciB9KVxuXHRcdFx0XHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0XHRcdFx0dHJ5IHtcblx0XHRcdFx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfU1VDQ0VFREVEXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGRhdGE6IEpTT04ucGFyc2UoYm9keSl9KTtcblx0XHRcdFx0XHRcdFx0fSBjYXRjaChlKSB7XG5cdFx0XHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlIH0pXG5cdFx0XHRcdFx0XHRcdH1cblx0XHRcdFx0XHRcdH1cblx0XHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR19GSU5JU0hFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9ufSlcblx0XHRcdFx0XHR9KTtcblx0XHRcdFx0fVxuXHRcdFx0fSk7XG5cdFx0fSxcblxuXHRcdG9uTWFwQ29sbGVjdGlvbkFyY2hldHlwZTogKGNvbGxlY3Rpb24sIHZhbHVlKSA9PlxuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiTUFQX0NPTExFQ1RJT05fQVJDSEVUWVBFXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHZhbHVlOiB2YWx1ZX0pLFxuXG5cdFx0b25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzOiAoKSA9PiB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT05GSVJNX0NPTExFQ1RJT05fQVJDSEVUWVBFX01BUFBJTkdTXCJ9KVxuXHRcdFx0ZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuXHRcdFx0XHRsZXQgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuXHRcdFx0XHRhY3Rpb25zLm9uU2VsZWN0Q29sbGVjdGlvbihzdGF0ZS5pbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24pO1xuXHRcdFx0fSlcblx0XHRcdG5hdmlnYXRlVG8oXCJtYXBEYXRhXCIpO1xuXHRcdH0sXG5cblx0XHRvblNldEZpZWxkTWFwcGluZzogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQsIGltcG9ydGVkRmllbGQpID0+XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRklFTERfTUFQUElOR1wiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eUZpZWxkLCBpbXBvcnRlZEZpZWxkOiBpbXBvcnRlZEZpZWxkfSksXG5cblx0XHRvbkNsZWFyRmllbGRNYXBwaW5nOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCwgY2xlYXJJbmRleCkgPT5cblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkNMRUFSX0ZJRUxEX01BUFBJTkdcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgY2xlYXJJbmRleDogY2xlYXJJbmRleH0pLFxuXG5cdFx0b25TZXREZWZhdWx0VmFsdWU6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkLCB2YWx1ZSkgPT5cblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9ERUZBVUxUX1ZBTFVFXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQ6IHByb3BlcnR5RmllbGQsIHZhbHVlOiB2YWx1ZX0pLFxuXG5cdFx0b25Db25maXJtRmllbGRNYXBwaW5nczogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQpID0+XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT05GSVJNX0ZJRUxEX01BUFBJTkdTXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQ6IHByb3BlcnR5RmllbGR9KSxcblxuXHRcdG9uVW5jb25maXJtRmllbGRNYXBwaW5nczogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQpID0+XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJVTkNPTkZJUk1fRklFTERfTUFQUElOR1NcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZH0pLFxuXG5cdFx0b25TZXRWYWx1ZU1hcHBpbmc6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkLCB0aW1WYWx1ZSwgbWFwVmFsdWUpID0+XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfVkFMVUVfTUFQUElOR1wiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eUZpZWxkLCB0aW1WYWx1ZTogdGltVmFsdWUsIG1hcFZhbHVlOiBtYXBWYWx1ZX0pLFxuXG5cdFx0b25JZ25vcmVDb2x1bW5Ub2dnbGU6IChjb2xsZWN0aW9uLCB2YXJpYWJsZU5hbWUpID0+XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJUT0dHTEVfSUdOT1JFRF9DT0xVTU5cIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgdmFyaWFibGVOYW1lOiB2YXJpYWJsZU5hbWV9KSxcblxuXHRcdG9uQWRkQ3VzdG9tUHJvcGVydHk6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eU5hbWUsIHByb3BlcnR5VHlwZSkgPT5cblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkFERF9DVVNUT01fUFJPUEVSVFlcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlOYW1lLCBwcm9wZXJ0eVR5cGU6IHByb3BlcnR5VHlwZX0pLFxuXG5cdFx0b25SZW1vdmVDdXN0b21Qcm9wZXJ0eTogKGNvbGxlY3Rpb24sIHByb3BlcnR5TmFtZSkgPT5cblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlJFTU9WRV9DVVNUT01fUFJPUEVSVFlcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlOYW1lfSksXG5cdH07XG5cdHJldHVybiBhY3Rpb25zO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBQYWdlIGZyb20gJy4vcGFnZS5qc3gnXG5pbXBvcnQgRmlyc3RVcGxvYWQgZnJvbSAnLi9maXJzdFVwbG9hZC5qc3gnXG5pbXBvcnQgQ29ubmVjdFRvQXJjaGV0eXBlIGZyb20gXCIuL2Nvbm5lY3QtdG8tYXJjaGV0eXBlXCI7XG5pbXBvcnQgQ29ubmVjdERhdGEgZnJvbSBcIi4vY29ubmVjdC1kYXRhXCI7XG5pbXBvcnQgTWVzc2FnZSBmcm9tIFwiLi9tZXNzYWdlXCI7XG5pbXBvcnQgVXBsb2FkQnV0dG9uIGZyb20gXCIuL3VwbG9hZC1idXR0b25cIjtcbmltcG9ydCBDb2xsZWN0aW9uT3ZlcnZpZXcgZnJvbSBcIi4vY29sbGVjdGlvbi1vdmVydmlld1wiO1xuXG4vLyBGSVhNRTogcmVwbGFjZSBlYWNoIGlmIGJsb2NrIHdpdGggYW4gb3duIGNvbXBvbmVudCBtYXBwZWQgYnkgdGhlIHJvdXRlci5cbmNsYXNzIEFwcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IGhhc1ZyZXMgPSB0aGlzLnByb3BzLnVzZXJkYXRhLm15VnJlcyAmJiBPYmplY3Qua2V5cyh0aGlzLnByb3BzLnVzZXJkYXRhLm15VnJlcykubGVuZ3RoID4gMDtcbiAgICBjb25zdCB7IGxvY2F0aW9uOiB7IHBhdGhuYW1lIH0sIGltcG9ydERhdGE6IHsgc2hlZXRzLCB1cGxvYWRlZEZpbGVOYW1lIH0sIG9uVXBsb2FkRmlsZVNlbGVjdCwgb25DbG9zZU1lc3NhZ2UgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBmaWxlSXNVcGxvYWRlZE1lc3NhZ2UgPSB0aGlzLnByb3BzLnNob3dGaWxlSXNVcGxvYWRlZE1lc3NhZ2UgPyAoXG4gICAgICA8TWVzc2FnZSBhbGVydExldmVsPVwiaW5mb1wiIGRpc21pc3NpYmxlPXt0cnVlfSBvbkNsb3NlTWVzc2FnZT17KCkgPT4gb25DbG9zZU1lc3NhZ2UoXCJzaG93RmlsZUlzVXBsb2FkZWRNZXNzYWdlXCIpfT5cbiAgICAgICAgPGVtPnt1cGxvYWRlZEZpbGVOYW1lfTwvZW0+IGlzIHVwbG9hZGVkLlxuICAgICAgPC9NZXNzYWdlPlxuICAgICkgOiBudWxsO1xuXG4gICAgY29uc3QgcHVibGlzaEZhaWxlZE1lc3NhZ2UgPSB0aGlzLnByb3BzLmltcG9ydERhdGEucHVibGlzaEVycm9ycyA/IChcbiAgICAgIDxNZXNzYWdlIGFsZXJ0TGV2ZWw9XCJkYW5nZXJcIiBkaXNtaXNzaWJsZT17ZmFsc2V9PlxuICAgICAgICA8VXBsb2FkQnV0dG9uIGNsYXNzTmFtZXM9e1tcImJ0blwiLCBcImJ0bi1kYW5nZXJcIiwgXCJwdWxsLXJpZ2h0XCIsIFwiYnRuLXhzXCJdfSBsYWJlbD1cIlJlLXVwbG9hZFwiXG4gICAgICAgICAgICAgICAgICAgICAgb25VcGxvYWRGaWxlU2VsZWN0PXtvblVwbG9hZEZpbGVTZWxlY3R9IC8+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tZXhjbGFtYXRpb24tc2lnblwiIC8+e1wiIFwifVxuICAgICAgICBQdWJsaXNoIGZhaWxlZC4gUGxlYXNlIGZpeCB0aGUgbWFwcGluZ3Mgb3IgcmUtdXBsb2FkIHRoZSBkYXRhLlxuICAgICAgPC9NZXNzYWdlPlxuICAgICkgOiBudWxsO1xuXG4gICAgY29uc3QgY29sbGVjdGlvbnNBcmVDb25uZWN0ZWRNZXNzYWdlID0gdGhpcy5wcm9wcy5zaG93Q29sbGVjdGlvbnNBcmVDb25uZWN0ZWRNZXNzYWdlID9cbiAgICAgIDxNZXNzYWdlIGFsZXJ0TGV2ZWw9XCJpbmZvXCIgZGlzbWlzc2libGU9e3RydWV9IG9uQ2xvc2VNZXNzYWdlPXsoKSA9PiBvbkNsb3NlTWVzc2FnZShcInNob3dDb2xsZWN0aW9uc0FyZUNvbm5lY3RlZE1lc3NhZ2VcIil9PlxuICAgICAgICB7c2hlZXRzLm1hcCgoc2hlZXQpID0+IDxlbSBrZXk9e3NoZWV0LmNvbGxlY3Rpb259PntzaGVldC5jb2xsZWN0aW9ufTwvZW0+KVxuICAgICAgICAgIC5yZWR1Y2UoKGFjY3UsIGVsZW0pID0+IGFjY3UgPT09IG51bGwgPyBbZWxlbV0gOiBbLi4uYWNjdSwgJyBhbmQgJywgZWxlbV0sIG51bGwpXG4gICAgICAgIH0gZnJvbSA8ZW0+e3VwbG9hZGVkRmlsZU5hbWV9PC9lbT4gYXJlIGNvbm5lY3RlZCB0byB0aGUgVGltYnVjdG9vIEFyY2hldHlwZXMuXG4gICAgICA8L01lc3NhZ2U+IDogbnVsbDtcblxuICAgIGlmIChwYXRobmFtZSA9PT0gXCIvXCIgfHwgcGF0aG5hbWUgPT09IFwiL2NvbGxlY3Rpb25zLW92ZXJ2aWV3XCIpIHtcbiAgICAgIHJldHVybiAoXG4gICAgICAgIDxQYWdlIHVzZXJuYW1lPXt0aGlzLnByb3BzLnVzZXJkYXRhLnVzZXJJZH0gdnJlcz17dGhpcy5wcm9wcy51c2VyZGF0YS52cmVzfSBzaG93RGF0YXNldHM9e3RydWV9PlxuICAgICAgICAgIHtoYXNWcmVzXG4gICAgICAgICAgICA/IDxDb2xsZWN0aW9uT3ZlcnZpZXdcbiAgICAgICAgICAgICAgICBvblVwbG9hZEZpbGVTZWxlY3Q9e3RoaXMucHJvcHMub25VcGxvYWRGaWxlU2VsZWN0fVxuICAgICAgICAgICAgICAgIGlzVXBsb2FkaW5nPXt0aGlzLnByb3BzLmltcG9ydERhdGEuaXNVcGxvYWRpbmd9XG4gICAgICAgICAgICAgICAgdXNlcklkPXt0aGlzLnByb3BzLnVzZXJkYXRhLnVzZXJJZH1cbiAgICAgICAgICAgICAgICB2cmVzPXt0aGlzLnByb3BzLnVzZXJkYXRhLm15VnJlc31cbiAgICAgICAgICAgICAgLz5cbiAgICAgICAgICAgIDogPEZpcnN0VXBsb2FkXG4gICAgICAgICAgICAgICAgX19Nb2NrT25Mb2dpbj17dGhpcy5wcm9wcy5vbkxvZ2lufVxuICAgICAgICAgICAgICAgIG9uVXBsb2FkRmlsZVNlbGVjdD17dGhpcy5wcm9wcy5vblVwbG9hZEZpbGVTZWxlY3R9XG4gICAgICAgICAgICAgICAgaXNVcGxvYWRpbmc9e3RoaXMucHJvcHMuaW1wb3J0RGF0YS5pc1VwbG9hZGluZ31cbiAgICAgICAgICAgICAgICB1c2VySWQ9e3RoaXMucHJvcHMudXNlcmRhdGEudXNlcklkfVxuICAgICAgICAgICAgICAvPlxuICAgICAgICAgIH1cbiAgICAgICAgPC9QYWdlPlxuICAgICAgKTtcbiAgICB9IGVsc2UgaWYgKHBhdGhuYW1lID09PSBcIi9tYXBhcmNoZXR5cGVzXCIpIHtcbiAgICAgIHJldHVybiAoXG4gICAgICAgIDxQYWdlIHVzZXJuYW1lPXt0aGlzLnByb3BzLnVzZXJkYXRhLnVzZXJJZH0gdnJlcz17dGhpcy5wcm9wcy51c2VyZGF0YS52cmVzfSBzaG93RGF0YXNldHM9e2ZhbHNlfT5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICAgIDxoMiBjbGFzc05hbWU9XCJzbWFsbC1tYXJnaW5cIj5VcGxvYWQgYW5kIGNvbm5lY3QgeW91ciBkYXRhc2V0PC9oMj5cbiAgICAgICAgICAgIHtmaWxlSXNVcGxvYWRlZE1lc3NhZ2V9XG4gICAgICAgICAgICA8cD5XZSBmb3VuZCB7c2hlZXRzLmxlbmd0aH0gY29sbGVjdGlvbnMgaW4gdGhlIGZpbGUuIENvbm5lY3QgdGhlIHRhYnMgdG8gdGhlIFRpbWJ1Y3RvbyBBcmNoZXR5cGVzLjwvcD5cbiAgICAgICAgICA8L2Rpdj5cblxuICAgICAgICAgIDxDb25uZWN0VG9BcmNoZXR5cGVcbiAgICAgICAgICAgIG1hcHBpbmdzPXt0aGlzLnByb3BzLm1hcHBpbmdzfVxuICAgICAgICAgICAgc2hlZXRzPXt0aGlzLnByb3BzLmltcG9ydERhdGEuc2hlZXRzfVxuICAgICAgICAgICAgdXBsb2FkZWRGaWxlTmFtZT17dGhpcy5wcm9wcy5pbXBvcnREYXRhLnVwbG9hZGVkRmlsZU5hbWV9XG4gICAgICAgICAgICBhcmNoZXR5cGU9e3RoaXMucHJvcHMuYXJjaGV0eXBlfVxuICAgICAgICAgICAgb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlPXt0aGlzLnByb3BzLm9uTWFwQ29sbGVjdGlvbkFyY2hldHlwZX1cbiAgICAgICAgICAgIG9uQ29uZmlybUNvbGxlY3Rpb25BcmNoZXR5cGVNYXBwaW5ncz17dGhpcy5wcm9wcy5vbkNvbmZpcm1Db2xsZWN0aW9uQXJjaGV0eXBlTWFwcGluZ3N9XG4gICAgICAgICAgLz5cbiAgICAgICAgPC9QYWdlPlxuICAgICAgKTtcbiAgICB9IGVsc2UgaWYgKHBhdGhuYW1lID09PSBcIi9tYXBkYXRhXCIpIHtcbiAgICAgIHJldHVybiAoXG4gICAgICAgIDxQYWdlIHVzZXJuYW1lPXt0aGlzLnByb3BzLnVzZXJkYXRhLnVzZXJJZH0gdnJlcz17dGhpcy5wcm9wcy51c2VyZGF0YS52cmVzfSBzaG93RGF0YXNldHM9e2ZhbHNlfT5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICAgIDxoMiBjbGFzc05hbWU9XCJzbWFsbC1tYXJnaW5cIj5VcGxvYWQgYW5kIGNvbm5lY3QgeW91ciBkYXRhc2V0PC9oMj5cbiAgICAgICAgICAgIHtjb2xsZWN0aW9uc0FyZUNvbm5lY3RlZE1lc3NhZ2V9XG4gICAgICAgICAgICB7cHVibGlzaEZhaWxlZE1lc3NhZ2V9XG4gICAgICAgICAgICA8cD5Db25uZWN0IHRoZSBleGNlbCBjb2x1bW5zIHRvIHRoZSBwcm9wZXJ0aWVzIG9mIHRoZSBBcmNoZXR5cGVzPC9wPlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDxDb25uZWN0RGF0YVxuICAgICAgICAgICAgey4uLnRoaXMucHJvcHN9XG4gICAgICAgICAgLz5cbiAgICAgICAgPC9QYWdlPlxuICAgICAgKTsgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gKDxkaXYgLz4pO1xuICAgIH1cbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBBcHA7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUHJvcGVydHlGb3JtIGZyb20gXCIuL3Byb3BlcnR5LWZvcm0vcHJvcGVydHktZm9ybVwiO1xuaW1wb3J0IEFkZFByb3BlcnR5IGZyb20gXCIuL3Byb3BlcnR5LWZvcm0vYWRkLXByb3BlcnR5XCI7XG5cbmNsYXNzIENvbGxlY3Rpb25Gb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3Qge1xuICAgICAgY29sdW1ucyxcbiAgICAgIGFyY2hldHlwZUZpZWxkcyxcbiAgICAgIHByb3BlcnR5TWFwcGluZ3MsXG4gICAgICBjdXN0b21Qcm9wZXJ0eU1hcHBpbmdzLFxuICAgICAgY29sbGVjdGlvbk5hbWUsXG4gICAgICBhdmFpbGFibGVBcmNoZXR5cGVzLFxuICAgICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGVcbiAgICB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHtcbiAgICAgIG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHksXG4gICAgICBvblNldEZpZWxkTWFwcGluZyxcbiAgICAgIG9uQ29uZmlybUZpZWxkTWFwcGluZ3MsXG4gICAgICBvbkNsZWFyRmllbGRNYXBwaW5nLFxuICAgICAgb25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzLFxuICAgICAgb25BZGRDdXN0b21Qcm9wZXJ0eVxuICAgIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgYXJjaGVUeXBlUHJvcEZpZWxkcyA9IGFyY2hldHlwZUZpZWxkcy5maWx0ZXIoKGFmKSA9PiBhZi50eXBlICE9PSBcInJlbGF0aW9uXCIpO1xuXG4gICAgY29uc3QgcHJvcGVydHlGb3JtcyA9IGFyY2hlVHlwZVByb3BGaWVsZHNcbiAgICAgIC5tYXAoKGFmLCBpKSA9PiAoXG4gICAgICAgIDxQcm9wZXJ0eUZvcm0gY29sdW1ucz17Y29sdW1uc30gY3VzdG9tPXtmYWxzZX0ga2V5PXtpfSBuYW1lPXthZi5uYW1lfSB0eXBlPXthZi50eXBlfVxuICAgICAgICAgICAgICAgICAgICAgIHByb3BlcnR5TWFwcGluZz17cHJvcGVydHlNYXBwaW5ncy5maW5kKChtKSA9PiBtLnByb3BlcnR5ID09PSBhZi5uYW1lKX1cbiAgICAgICAgICAgICAgICAgICAgICBvblNldEZpZWxkTWFwcGluZz17KGZpZWxkLCB2YWx1ZSkgPT4gb25TZXRGaWVsZE1hcHBpbmcoY29sbGVjdGlvbk5hbWUsIGZpZWxkLCB2YWx1ZSl9XG4gICAgICAgICAgICAgICAgICAgICAgb25Db25maXJtRmllbGRNYXBwaW5ncz17KGZpZWxkKSA9PiBvbkNvbmZpcm1GaWVsZE1hcHBpbmdzKGNvbGxlY3Rpb25OYW1lLCBmaWVsZCl9XG4gICAgICAgICAgICAgICAgICAgICAgb25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzPXsoZmllbGQpID0+IG9uVW5jb25maXJtRmllbGRNYXBwaW5ncyhjb2xsZWN0aW9uTmFtZSwgZmllbGQpfVxuICAgICAgICAgICAgICAgICAgICAgIG9uQ2xlYXJGaWVsZE1hcHBpbmc9eyhmaWVsZCwgdmFsdWVJZHgpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbk5hbWUsIGZpZWxkLCB2YWx1ZUlkeCkgfVxuICAgICAgICAvPlxuICAgICAgKSk7XG5cbiAgICBjb25zdCBjdXN0b21Qcm9wZXJ0eUZvcm1zID0gY3VzdG9tUHJvcGVydHlNYXBwaW5nc1xuICAgICAgLm1hcCgoY3VzdG9tUHJvcCwgaSkgPT4gKFxuICAgICAgICA8UHJvcGVydHlGb3JtIGNvbHVtbnM9e2NvbHVtbnN9IGN1c3RvbT17dHJ1ZX0ga2V5PXtpfSBuYW1lPXtjdXN0b21Qcm9wLm5hbWV9IHR5cGU9e2N1c3RvbVByb3AudHlwZX1cbiAgICAgICAgICAgICAgICAgICAgICBhcmNoZXR5cGVGaWVsZHM9e2FyY2hldHlwZUZpZWxkc31cbiAgICAgICAgICAgICAgICAgICAgICBhdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZT17YXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGV9XG4gICAgICAgICAgICAgICAgICAgICAgcHJvcGVydHlNYXBwaW5nPXtwcm9wZXJ0eU1hcHBpbmdzLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IGN1c3RvbVByb3AubmFtZSl9XG4gICAgICAgICAgICAgICAgICAgICAgb25SZW1vdmVDdXN0b21Qcm9wZXJ0eT17KGZpZWxkKSA9PiBvblJlbW92ZUN1c3RvbVByb3BlcnR5KGNvbGxlY3Rpb25OYW1lLCBmaWVsZCl9XG4gICAgICAgICAgICAgICAgICAgICAgb25TZXRGaWVsZE1hcHBpbmc9eyhmaWVsZCwgdmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25OYW1lLCBmaWVsZCwgdmFsdWUpfVxuICAgICAgICAgICAgICAgICAgICAgIG9uQ2xlYXJGaWVsZE1hcHBpbmc9eyhmaWVsZCwgdmFsdWVJZHgpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbk5hbWUsIGZpZWxkLCB2YWx1ZUlkeCkgfVxuICAgICAgICAgICAgICAgICAgICAgIG9uQ29uZmlybUZpZWxkTWFwcGluZ3M9eyhmaWVsZCkgPT4gb25Db25maXJtRmllbGRNYXBwaW5ncyhjb2xsZWN0aW9uTmFtZSwgZmllbGQpfVxuICAgICAgICAgICAgICAgICAgICAgIG9uVW5jb25maXJtRmllbGRNYXBwaW5ncz17KGZpZWxkKSA9PiBvblVuY29uZmlybUZpZWxkTWFwcGluZ3MoY29sbGVjdGlvbk5hbWUsIGZpZWxkKX1cbiAgICAgICAgLz5cbiAgICAgICkpO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICB7cHJvcGVydHlGb3Jtc31cbiAgICAgICAge2N1c3RvbVByb3BlcnR5Rm9ybXN9XG4gICAgICAgIDxBZGRQcm9wZXJ0eVxuICAgICAgICAgIGFyY2hldHlwZUZpZWxkcz17YXJjaGV0eXBlRmllbGRzfVxuICAgICAgICAgIGF2YWlsYWJsZUFyY2hldHlwZXM9e2F2YWlsYWJsZUFyY2hldHlwZXN9XG4gICAgICAgICAgb25BZGRDdXN0b21Qcm9wZXJ0eT17KG5hbWUsIHR5cGUpID0+IG9uQWRkQ3VzdG9tUHJvcGVydHkoY29sbGVjdGlvbk5hbWUsIG5hbWUsIHR5cGUpfSAvPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgQ29sbGVjdGlvbkluZGV4IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBjb2xsZWN0aW9uVGFicywgb25TZWxlY3RDb2xsZWN0aW9uIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwibmF2IG5hdi10YWJzXCIgcm9sZT1cInRhYmxpc3RcIj5cbiAgICAgICAgICB7Y29sbGVjdGlvblRhYnMubWFwKChjb2xsZWN0aW9uVGFiKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtjb2xsZWN0aW9uVGFiLmNvbGxlY3Rpb25OYW1lfSBjbGFzc05hbWU9e2N4KHthY3RpdmU6IGNvbGxlY3Rpb25UYWIuYWN0aXZlfSl9PlxuICAgICAgICAgICAgICA8YSBvbkNsaWNrPXsoKSA9PiBvblNlbGVjdENvbGxlY3Rpb24oY29sbGVjdGlvblRhYi5jb2xsZWN0aW9uTmFtZSl9XG4gICAgICAgICAgICAgICAgIHN0eWxlPXt7Y3Vyc29yOiBjb2xsZWN0aW9uVGFiLmFjdGl2ZSA/IFwiZGVmYXVsdFwiIDogXCJwb2ludGVyXCJ9fT5cbiAgICAgICAgICAgICAgICB7Y29sbGVjdGlvblRhYi5hcmNoZXR5cGVOYW1lfXtcIiBcIn1cbiAgICAgICAgICAgICAgICB7Y29sbGVjdGlvblRhYi5jb21wbGV0ZSA/IDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tb2tcIiAvPiA6IG51bGx9XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZXhjZWwtdGFiXCI+PGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBjbGFzc05hbWU9XCJleGNlbC1pY29uXCIgYWx0PVwiXCIvPiB7Y29sbGVjdGlvblRhYi5jb2xsZWN0aW9uTmFtZX08L3NwYW4+XG4gICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uSW5kZXg7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFVwbG9hZEJ1dHRvbiBmcm9tIFwiLi91cGxvYWQtYnV0dG9uXCI7XG5pbXBvcnQgRGF0YXNldENhcmRzIGZyb20gXCIuL2RhdGFzZXQtY2FyZHNcIlxuXG5mdW5jdGlvbiBDb2xsZWN0aW9uT3ZlcnZpZXcocHJvcHMpIHtcbiAgY29uc3QgeyBvblVwbG9hZEZpbGVTZWxlY3QsIHVzZXJJZCwgaXNVcGxvYWRpbmcsIHZyZXMgfSA9IHByb3BzO1xuXG4gIGNvbnN0IHVwbG9hZEJ1dHRvbiA9IChcbiAgICA8VXBsb2FkQnV0dG9uXG4gICAgICBjbGFzc05hbWVzPXtbXCJidG5cIiwgXCJidG4tbGdcIiwgXCJidG4tcHJpbWFyeVwiLCBcInB1bGwtcmlnaHRcIl19XG4gICAgICBnbHlwaGljb249XCJnbHlwaGljb24gZ2x5cGhpY29uLWNsb3VkLXVwbG9hZFwiXG4gICAgICBpc1VwbG9hZGluZz17aXNVcGxvYWRpbmd9XG4gICAgICBsYWJlbD1cIlVwbG9hZCBuZXcgZGF0YXNldFwiXG4gICAgICBvblVwbG9hZEZpbGVTZWxlY3Q9e29uVXBsb2FkRmlsZVNlbGVjdH0gLz5cbiAgKTtcblxuICByZXR1cm4gKFxuICAgIDxkaXY+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICA8RGF0YXNldENhcmRzIHVzZXJJZD17dXNlcklkfSBjYXB0aW9uPVwiTXkgZGF0YXNldHNcIiB2cmVzPXt2cmVzfT5cbiAgICAgICAgICB7dXBsb2FkQnV0dG9ufVxuICAgICAgICA8L0RhdGFzZXRDYXJkcz5cbiAgICAgIDwvZGl2PlxuICAgIDwvZGl2PlxuICApO1xufVxuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uT3ZlcnZpZXc7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IEhlYWRlckNlbGwgZnJvbSBcIi4vdGFibGUvaGVhZGVyLWNlbGxcIjtcbmltcG9ydCBEYXRhUm93IGZyb20gXCIuL3RhYmxlL2RhdGEtcm93XCI7XG5cbmNsYXNzIENvbGxlY3Rpb25UYWJsZSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uSWdub3JlQ29sdW1uVG9nZ2xlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgcm93cywgaGVhZGVycywgbmV4dFVybCB9ID0gdGhpcy5wcm9wcztcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cInRhYmxlLXJlc3BvbnNpdmVcIj5cbiAgICAgICAgPHRhYmxlIGNsYXNzTmFtZT1cInRhYmxlIHRhYmxlLWJvcmRlcmVkIHRhYmxlLW9idHJ1c2l2ZVwiPlxuICAgICAgICAgIDx0aGVhZD5cbiAgICAgICAgICAgIDx0cj5cbiAgICAgICAgICAgICAge2hlYWRlcnMubWFwKChoZWFkZXIpID0+IChcbiAgICAgICAgICAgICAgICA8SGVhZGVyQ2VsbCBrZXk9e2hlYWRlci5uYW1lfSBoZWFkZXI9e2hlYWRlci5uYW1lfSBvbklnbm9yZUNvbHVtblRvZ2dsZT17b25JZ25vcmVDb2x1bW5Ub2dnbGV9XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgaXNJZ25vcmVkPXtoZWFkZXIuaXNJZ25vcmVkfSBpc0NvbmZpcm1lZD17aGVhZGVyLmlzQ29uZmlybWVkfSAvPlxuICAgICAgICAgICAgICApKX1cbiAgICAgICAgICAgIDwvdHI+XG4gICAgICAgICAgPC90aGVhZD5cbiAgICAgICAgICA8dGJvZHk+XG4gICAgICAgICAgICB7cm93cy5tYXAoKHJvdywgaSkgPT4gPERhdGFSb3cga2V5PXtpfSByb3c9e3Jvd30gLz4pfVxuICAgICAgICAgIDwvdGJvZHk+XG4gICAgICAgIDwvdGFibGU+XG4gICAgICAgIDxidXR0b24gb25DbGljaz17KCkgPT4gdGhpcy5wcm9wcy5vbkxvYWRNb3JlQ2xpY2sgJiYgdGhpcy5wcm9wcy5vbkxvYWRNb3JlQ2xpY2sobmV4dFVybCl9XG4gICAgICAgICAgICAgICAgZGlzYWJsZWQ9eyFuZXh0VXJsfVxuICAgICAgICAgICAgICAgIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdCBwdWxsLXJpZ2h0XCI+bW9yZS4uLjwvYnV0dG9uPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFibGU7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IENvbGxlY3Rpb25UYWJsZSBmcm9tIFwiLi9jb2xsZWN0aW9uLXRhYmxlXCI7XG5pbXBvcnQgQ29sbGVjdGlvbkZvcm0gZnJvbSBcIi4vY29sbGVjdGlvbi1mb3JtXCI7XG5pbXBvcnQgQ29sbGVjdGlvbkluZGV4IGZyb20gXCIuL2NvbGxlY3Rpb24taW5kZXhcIjtcblxuY2xhc3MgQ29ubmVjdERhdGEgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7XG4gICAgICBhY3RpdmVDb2xsZWN0aW9uLFxuICAgICAgdXBsb2FkZWRGaWxlTmFtZSxcbiAgICAgIGNvbGxlY3Rpb25UYWJzLFxuICAgICAgcm93cyxcbiAgICAgIGhlYWRlcnMsXG4gICAgICBhcmNoZXR5cGVGaWVsZHMsXG4gICAgICBhdmFpbGFibGVBcmNoZXR5cGVzLFxuICAgICAgcHJvcGVydHlNYXBwaW5ncyxcbiAgICAgIGN1c3RvbVByb3BlcnR5TWFwcGluZ3MsXG4gICAgICBhdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZSxcbiAgICAgIG5leHRVcmxcbiAgICB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHtcbiAgICAgIG9uSWdub3JlQ29sdW1uVG9nZ2xlLFxuICAgICAgb25TZWxlY3RDb2xsZWN0aW9uLFxuICAgICAgb25TZXRGaWVsZE1hcHBpbmcsXG4gICAgICBvblJlbW92ZUN1c3RvbVByb3BlcnR5LFxuICAgICAgb25Db25maXJtRmllbGRNYXBwaW5ncyxcbiAgICAgIG9uVW5jb25maXJtRmllbGRNYXBwaW5ncyxcbiAgICAgIG9uQWRkQ3VzdG9tUHJvcGVydHksXG4gICAgICBvbkNsZWFyRmllbGRNYXBwaW5nLFxuICAgICAgb25Mb2FkTW9yZUNsaWNrLFxuICAgICAgb25QdWJsaXNoRGF0YVxuICAgIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgYWxsTWFwcGluZ3NBcmVDb21wbGV0ZSA9IGNvbGxlY3Rpb25UYWJzLmZpbHRlcigodGFiKSA9PiB0YWIuY29tcGxldGUpLmxlbmd0aCA9PT0gY29sbGVjdGlvblRhYnMubGVuZ3RoO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXY+XG4gICAgICAgIDxDb2xsZWN0aW9uSW5kZXggY29sbGVjdGlvblRhYnM9e2NvbGxlY3Rpb25UYWJzfSBvblNlbGVjdENvbGxlY3Rpb249e29uU2VsZWN0Q29sbGVjdGlvbn0gLz5cblxuICAgICAgICA8Q29sbGVjdGlvbkZvcm0gY29sdW1ucz17aGVhZGVyc31cbiAgICAgICAgICAgICAgICAgICAgICAgIGNvbGxlY3Rpb25OYW1lPXthY3RpdmVDb2xsZWN0aW9ufVxuICAgICAgICAgICAgICAgICAgICAgICAgYXJjaGV0eXBlRmllbGRzPXthcmNoZXR5cGVGaWVsZHN9XG4gICAgICAgICAgICAgICAgICAgICAgICBhdmFpbGFibGVBcmNoZXR5cGVzPXthdmFpbGFibGVBcmNoZXR5cGVzfVxuICAgICAgICAgICAgICAgICAgICAgICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGU9e2F2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlfVxuICAgICAgICAgICAgICAgICAgICAgICAgcHJvcGVydHlNYXBwaW5ncz17cHJvcGVydHlNYXBwaW5nc31cbiAgICAgICAgICAgICAgICAgICAgICAgIGN1c3RvbVByb3BlcnR5TWFwcGluZ3M9e2N1c3RvbVByb3BlcnR5TWFwcGluZ3N9XG4gICAgICAgICAgICAgICAgICAgICAgICBvblNldEZpZWxkTWFwcGluZz17b25TZXRGaWVsZE1hcHBpbmd9XG4gICAgICAgICAgICAgICAgICAgICAgICBvbkNsZWFyRmllbGRNYXBwaW5nPXtvbkNsZWFyRmllbGRNYXBwaW5nfVxuICAgICAgICAgICAgICAgICAgICAgICAgb25SZW1vdmVDdXN0b21Qcm9wZXJ0eT17b25SZW1vdmVDdXN0b21Qcm9wZXJ0eX1cbiAgICAgICAgICAgICAgICAgICAgICAgIG9uQ29uZmlybUZpZWxkTWFwcGluZ3M9e29uQ29uZmlybUZpZWxkTWFwcGluZ3N9XG4gICAgICAgICAgICAgICAgICAgICAgICBvblVuY29uZmlybUZpZWxkTWFwcGluZ3M9e29uVW5jb25maXJtRmllbGRNYXBwaW5nc31cbiAgICAgICAgICAgICAgICAgICAgICAgIG9uQWRkQ3VzdG9tUHJvcGVydHk9e29uQWRkQ3VzdG9tUHJvcGVydHl9IC8+XG5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmlnLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gb25DbGljaz17b25QdWJsaXNoRGF0YX0gY2xhc3NOYW1lPVwiYnRuIGJ0bi13YXJuaW5nIGJ0bi1sZyBwdWxsLXJpZ2h0XCIgdHlwZT1cImJ1dHRvblwiIGRpc2FibGVkPXshYWxsTWFwcGluZ3NBcmVDb21wbGV0ZX0+XG4gICAgICAgICAgICBQdWJsaXNoIGRhdGFzZXRcbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmlnLW1hcmdpblwiPlxuICAgICAgICAgIDxwIGNsYXNzTmFtZT1cImZyb20tZXhjZWxcIj5cbiAgICAgICAgICAgIDxpbWcgc3JjPVwiaW1hZ2VzL2ljb24tZXhjZWwuc3ZnXCIgYWx0PVwiXCIvPntcIiBcIn1cbiAgICAgICAgICAgIDxlbT57YWN0aXZlQ29sbGVjdGlvbn08L2VtPiBmcm9tIHt1cGxvYWRlZEZpbGVOYW1lfVxuICAgICAgICAgIDwvcD5cblxuICAgICAgICAgIDxDb2xsZWN0aW9uVGFibGVcbiAgICAgICAgICAgIHJvd3M9e3Jvd3N9XG4gICAgICAgICAgICBoZWFkZXJzPXtoZWFkZXJzfVxuICAgICAgICAgICAgbmV4dFVybD17bmV4dFVybH1cbiAgICAgICAgICAgIG9uSWdub3JlQ29sdW1uVG9nZ2xlPXsoaGVhZGVyKSA9PiBvbklnbm9yZUNvbHVtblRvZ2dsZShhY3RpdmVDb2xsZWN0aW9uLCBoZWFkZXIpfVxuICAgICAgICAgICAgb25Mb2FkTW9yZUNsaWNrPXsodXJsKSA9PiBvbkxvYWRNb3JlQ2xpY2sodXJsLCBhY3RpdmVDb2xsZWN0aW9uKX0gLz5cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgQ29ubmVjdERhdGE7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgQ29ubmVjdFRvQXJjaGV0eXBlIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlLCBvbkNvbmZpcm1Db2xsZWN0aW9uQXJjaGV0eXBlTWFwcGluZ3MsXG4gICAgICBzaGVldHMsIGFyY2hldHlwZSwgbWFwcGluZ3MsIHVwbG9hZGVkRmlsZU5hbWV9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IGNvbGxlY3Rpb25zQXJlTWFwcGVkID0gT2JqZWN0LmtleXMobWFwcGluZ3MuY29sbGVjdGlvbnMpLmxlbmd0aCA+IDAgJiZcbiAgICAgIE9iamVjdC5rZXlzKG1hcHBpbmdzLmNvbGxlY3Rpb25zKS5tYXAoKGtleSkgPT4gbWFwcGluZ3MuY29sbGVjdGlvbnNba2V5XS5hcmNoZXR5cGVOYW1lKS5pbmRleE9mKG51bGwpIDwgMDtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2PlxuXG5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAge3NoZWV0cy5tYXAoKHNoZWV0KSA9PiAoXG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInJvd1wiIGtleT17c2hlZXQuY29sbGVjdGlvbn0+XG4gICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLW1kLTJcIj5cbiAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+XG4gICAgICAgICAgICAgICAgICA8aW1nIHNyYz1cImltYWdlcy9pY29uLWV4Y2VsLnN2Z1wiIGFsdD1cIlwiLz4ge3NoZWV0LmNvbGxlY3Rpb259XG4gICAgICAgICAgICAgICAgICA8L3NwYW4+XG4gICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1tZC04XCI+XG4gICAgICAgICAgICAgICAgPFNlbGVjdEZpZWxkXG4gICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbHVlKSA9PiBvbk1hcENvbGxlY3Rpb25BcmNoZXR5cGUoc2hlZXQuY29sbGVjdGlvbiwgdmFsdWUpfVxuICAgICAgICAgICAgICAgICAgb25DbGVhcj17KCkgPT4gb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHNoZWV0LmNvbGxlY3Rpb24sIG51bGwpIH1cbiAgICAgICAgICAgICAgICAgIHZhbHVlPXttYXBwaW5ncy5jb2xsZWN0aW9uc1tzaGVldC5jb2xsZWN0aW9uXS5hcmNoZXR5cGVOYW1lfT5cbiAgICAgICAgICAgICAgICAgICAgPHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG4gICAgICAgICAgICAgICAgICAgICAgQ29ubmVjdCA8ZW0+e3NoZWV0LmNvbGxlY3Rpb259PC9lbT4gdG8gYSBUaW1idWN0b28gYXJjaGV0eXBlLlxuICAgICAgICAgICAgICAgICAgICA8L3NwYW4+XG4gICAgICAgICAgICAgICAgICAgIHtPYmplY3Qua2V5cyhhcmNoZXR5cGUpLmZpbHRlcigoZG9tYWluKSA9PiBkb21haW4gIT09IFwicmVsYXRpb25zXCIpLnNvcnQoKS5tYXAoKG9wdGlvbikgPT4gKFxuICAgICAgICAgICAgICAgICAgICAgIDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cbiAgICAgICAgICAgICAgICAgICAgKSl9XG4gICAgICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgIHsgbWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0uYXJjaGV0eXBlTmFtZSA/IChcbiAgICAgICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgaGktc3VjY2Vzc1wiIGtleT17c2hlZXQuY29sbGVjdGlvbn0+XG4gICAgICAgICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tb2sgcHVsbC1yaWdodFwiLz5cbiAgICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICAgICkgOiBudWxsXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICkpfVxuXG4gICAgICAgIDwvZGl2PlxuXG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gb25DbGljaz17b25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzfSB0eXBlPVwiYnV0dG9uXCIgY2xhc3NOYW1lPVwiYnRuIGJ0bi1zdWNjZXNzXCIgZGlzYWJsZWQ9eyFjb2xsZWN0aW9uc0FyZU1hcHBlZH0+XG4gICAgICAgICAgICBDb25uZWN0XG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IENvbm5lY3RUb0FyY2hldHlwZTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IERhdGFTZXRDYXJkIGZyb20gJy4vZGF0YXNldENhcmQuanN4JztcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyB2cmVzLCBjYXB0aW9uLCB1c2VySWQgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICAgICAgPGgzPntjYXB0aW9ufTwvaDM+XG4gICAgICA8L2Rpdj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmlnLW1hcmdpblwiPlxuICAgICAgICB7IE9iamVjdC5rZXlzKHZyZXMpLm1hcCgodnJlKSA9PiA8RGF0YVNldENhcmQga2V5PXt2cmV9IHVzZXJJZD17dXNlcklkfSB2cmVJZD17dnJlc1t2cmVdLm5hbWV9IGNhcHRpb249e3ZyZXNbdnJlXS5uYW1lLnJlcGxhY2UoL15bYS16MC05XStfLywgXCJcIil9IC8+KSB9XG4gICAgIDwvZGl2PlxuICAgIDwvZGl2PlxuICApXG59OyIsImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmZ1bmN0aW9uIERhdGFTZXRDYXJkKHByb3BzKSB7XG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjYXJkLWRhdGFzZXRcIj5cbiAgICAgIDxhIGNsYXNzTmFtZT1cImNhcmQtZGF0YXNldCBidG4gYnRuLWRlZmF1bHQgZXhwbG9yZVwiXG4gICAgICAgICBocmVmPXtgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3N0YXRpYy9xdWVyeS1ndWkvP3ZyZUlkPSR7cHJvcHMudnJlSWR9YH0gdGFyZ2V0PVwiX2JsYW5rXCI+XG4gICAgICAgIEV4cGxvcmU8YnIgLz5cbiAgICAgICAgPHN0cm9uZz5cbiAgICAgICAgICAgIHtwcm9wcy5jYXB0aW9ufVxuICAgICAgICA8L3N0cm9uZz5cbiAgICAgIDwvYT5cbiAgICAgIHtwcm9wcy51c2VySWRcbiAgICAgICAgPyAoPGEgY2xhc3NOYW1lPVwiY2FyZC1kYXRhc2V0IGJ0biBidG4tZGVmYXVsdFwiXG4gICAgICAgICAgICAgIGhyZWY9e2Ake3Byb2Nlc3MuZW52LnNlcnZlcn0vc3RhdGljL2VkaXQtZ3VpLz92cmVJZD0ke3Byb3BzLnZyZUlkfSZoc2lkPSR7cHJvcHMudXNlcklkfWB9IHRhcmdldD1cIl9ibGFua1wiPlxuICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIiAvPntcIiBcIn1cbiAgICAgICAgICAgIEVkaXRcbiAgICAgICAgICA8L2E+KVxuICAgICAgICA6IG51bGx9XG4gICAgPC9kaXY+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IERhdGFTZXRDYXJkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5jbGFzcyBTZWxlY3RGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIGlzT3BlbjogZmFsc2VcbiAgICB9O1xuICAgIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyID0gdGhpcy5oYW5kbGVEb2N1bWVudENsaWNrLmJpbmQodGhpcyk7XG4gIH1cblxuICBjb21wb25lbnREaWRNb3VudCgpIHtcbiAgICBkb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiY2xpY2tcIiwgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIsIGZhbHNlKTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgIGRvY3VtZW50LnJlbW92ZUV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgdG9nZ2xlU2VsZWN0KCkge1xuICAgIGlmKHRoaXMuc3RhdGUuaXNPcGVuKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtpc09wZW46IGZhbHNlfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogdHJ1ZX0pO1xuICAgIH1cbiAgfVxuXG4gIGhhbmRsZURvY3VtZW50Q2xpY2soZXYpIHtcbiAgICBjb25zdCB7IGlzT3BlbiB9ID0gdGhpcy5zdGF0ZTtcbiAgICBpZiAoaXNPcGVuICYmICFSZWFjdERPTS5maW5kRE9NTm9kZSh0aGlzKS5jb250YWlucyhldi50YXJnZXQpKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtcbiAgICAgICAgaXNPcGVuOiBmYWxzZVxuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25DaGFuZ2UsIG9uQ2xlYXIsIHZhbHVlIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qgc2VsZWN0ZWRPcHRpb24gPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgPT09IHZhbHVlKTtcbiAgICBjb25zdCBwbGFjZWhvbGRlciA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy50eXBlID09PSBcInBsYWNlaG9sZGVyXCIpO1xuICAgIGNvbnN0IG90aGVyT3B0aW9ucyA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSAmJiBvcHQucHJvcHMudmFsdWUgIT09IHZhbHVlKTtcblxuICAgIHJldHVybiAoXG5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImRyb3Bkb3duXCIsIHtvcGVuOiB0aGlzLnN0YXRlLmlzT3Blbn0pfT5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGRyb3Bkb3duLXRvZ2dsZVwiIG9uQ2xpY2s9e3RoaXMudG9nZ2xlU2VsZWN0LmJpbmQodGhpcyl9PlxuICAgICAgICAgIHtzZWxlY3RlZE9wdGlvbi5sZW5ndGggPyBzZWxlY3RlZE9wdGlvbiA6IHBsYWNlaG9sZGVyfSA8c3BhbiBjbGFzc05hbWU9XCJjYXJldFwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuXG4gICAgICAgIDx1bCBjbGFzc05hbWU9XCJkcm9wZG93bi1tZW51XCI+XG4gICAgICAgICAgeyB2YWx1ZSA/IChcbiAgICAgICAgICAgIDxsaT5cbiAgICAgICAgICAgICAgPGEgb25DbGljaz17KCkgPT4geyBvbkNsZWFyKCk7IHRoaXMudG9nZ2xlU2VsZWN0KCk7fX0+XG4gICAgICAgICAgICAgICAgLSBjbGVhciAtXG4gICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSA6IG51bGx9XG4gICAgICAgICAge290aGVyT3B0aW9ucy5tYXAoKG9wdGlvbiwgaSkgPT4gKFxuICAgICAgICAgICAgPGxpIGtleT17aX0+XG4gICAgICAgICAgICAgIDxhIHN0eWxlPXt7Y3Vyc29yOiBcInBvaW50ZXJcIn19IG9uQ2xpY2s9eygpID0+IHsgb25DaGFuZ2Uob3B0aW9uLnByb3BzLnZhbHVlKTsgdGhpcy50b2dnbGVTZWxlY3QoKTsgfX0+e29wdGlvbn08L2E+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5TZWxlY3RGaWVsZC5wcm9wVHlwZXMgPSB7XG4gIG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgb25DbGVhcjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIHZhbHVlOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBVcGxvYWRCdXR0b24gZnJvbSBcIi4vdXBsb2FkLWJ1dHRvblwiO1xuXG4vLyAocmVuYW1lcyBmcm9tIFVwbG9hZFNwbGFzaFNjcmVlbilcbmZ1bmN0aW9uIEZpcnN0VXBsb2FkKHByb3BzKSB7XG4gIGNvbnN0IHsgb25VcGxvYWRGaWxlU2VsZWN0LCB1c2VySWQsIGlzVXBsb2FkaW5nIH0gPSBwcm9wcztcblxuICBjb25zdCBzYW1wbGVTaGVldCA9IHByb3BzLmV4YW1wbGVTaGVldFVybCA/XG4gICAgPHA+RG9uJ3QgaGF2ZSBhIGRhdGFzZXQgaGFuZHk/IEhlcmXigJlzIGFuIDxhIGhyZWY9e3Byb3BzLmV4YW1wbGVTaGVldFVybH0+ZXhhbXBsZSBleGNlbCBzaGVldDwvYT4uPC9wPiA6IG51bGw7XG5cbiAgY29uc3QgdXBsb2FkQnV0dG9uID0gKFxuICAgIDxVcGxvYWRCdXR0b25cbiAgICAgIGNsYXNzTmFtZXM9e1tcImJ0blwiLCBcImJ0bi1sZ1wiLCBcImJ0bi1wcmltYXJ5XCJdfVxuICAgICAgZ2x5cGhpY29uPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1jbG91ZC11cGxvYWRcIlxuICAgICAgaXNVcGxvYWRpbmc9e2lzVXBsb2FkaW5nfVxuICAgICAgbGFiZWw9XCJCcm93c2VcIlxuICAgICAgb25VcGxvYWRGaWxlU2VsZWN0PXtvblVwbG9hZEZpbGVTZWxlY3R9IC8+XG4gICk7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJqdW1ib3Ryb24gZmlyc3QtdXBsb2FkIHVwbG9hZC1iZ1wiPlxuICAgICAgICA8aDI+VXBsb2FkIHlvdXIgZmlyc3QgZGF0YXNldDwvaDI+XG4gICAgICAgICAgIHtzYW1wbGVTaGVldH1cbiAgICAgICAgICAge3VzZXJJZCA/IHVwbG9hZEJ1dHRvbiA6IChcbiAgICAgICAgICAgPGZvcm0gYWN0aW9uPVwiaHR0cHM6Ly9zZWN1cmUuaHV5Z2Vucy5rbmF3Lm5sL3NhbWwyL2xvZ2luXCIgbWV0aG9kPVwiUE9TVFwiPlxuICAgICAgICAgICAgIDxpbnB1dCBuYW1lPVwiaHN1cmxcIiAgdHlwZT1cImhpZGRlblwiIHZhbHVlPXt3aW5kb3cubG9jYXRpb24uaHJlZn0gLz5cbiAgICAgICAgICAgICA8cD5Nb3N0IHVuaXZlcnNpdHkgYWNjb3VudHMgd2lsbCB3b3JrLjwvcD5cbiAgICAgICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnkgYnRuLWxnXCIgdHlwZT1cInN1Ym1pdFwiPlxuICAgICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tbG9nLWluXCIgLz4gTG9nIGluIHRvIHVwbG9hZFxuICAgICAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICAgPC9mb3JtPikgfVxuICAgICAgPC9kaXY+XG4gICAgPC9kaXY+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IEZpcnN0VXBsb2FkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5mdW5jdGlvbiBGb290ZXIocHJvcHMpIHtcbiAgY29uc3QgaGlMb2dvID0gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgY29sLW1kLTFcIj5cbiAgICAgIDxpbWcgY2xhc3NOYW1lPVwiaGktbG9nb1wiIHNyYz1cImltYWdlcy9sb2dvLWh1eWdlbnMtaW5nLnN2Z1wiIC8+XG4gICAgPC9kaXY+XG4gICk7XG5cbiAgY29uc3QgY2xhcmlhaExvZ28gPSAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMSBjb2wtbWQtMVwiPlxuICAgICAgPGltZyBjbGFzc05hbWU9XCJsb2dvXCIgc3JjPVwiaW1hZ2VzL2xvZ28tY2xhcmlhaC5zdmdcIiAvPlxuICAgIDwvZGl2PlxuICApO1xuXG4gIGNvbnN0IGZvb3RlckJvZHkgPSBSZWFjdC5DaGlsZHJlbi5jb3VudChwcm9wcy5jaGlsZHJlbikgPiAwID9cbiAgICBSZWFjdC5DaGlsZHJlbi5tYXAocHJvcHMuY2hpbGRyZW4sIChjaGlsZCwgaSkgPT4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJ3aGl0ZS1iYXJcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICB7aSA9PT0gUmVhY3QuQ2hpbGRyZW4uY291bnQocHJvcHMuY2hpbGRyZW4pIC0gMVxuICAgICAgICAgICAgPyAoPGRpdiBjbGFzc05hbWU9XCJyb3dcIj57aGlMb2dvfTxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEwIGNvbC1tZC0xMCB0ZXh0LWNlbnRlclwiPntjaGlsZH08L2Rpdj57Y2xhcmlhaExvZ299PC9kaXY+KVxuICAgICAgICAgICAgOiAoPGRpdiBjbGFzc05hbWU9XCJyb3dcIj48ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMiBjb2wtbWQtMTIgdGV4dC1jZW50ZXJcIj57Y2hpbGR9PC9kaXY+PC9kaXY+KVxuICAgICAgICAgIH1cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApKSA6IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwid2hpdGUtYmFyXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJyb3dcIj5cbiAgICAgICAgICAgIHtoaUxvZ299XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMCBjb2wtbWQtMTAgdGV4dC1jZW50ZXJcIj5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAge2NsYXJpYWhMb2dvfVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG5cblxuICByZXR1cm4gKFxuICAgIDxmb290ZXIgY2xhc3NOYW1lPVwiZm9vdGVyXCI+XG4gICAgICB7Zm9vdGVyQm9keX1cbiAgICA8L2Zvb3Rlcj5cbiAgKVxufVxuXG5leHBvcnQgZGVmYXVsdCBGb290ZXI7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgZGlzbWlzc2libGUsIGFsZXJ0TGV2ZWwsIG9uQ2xvc2VNZXNzYWdlfSA9IHByb3BzO1xuICBjb25zdCBkaXNtaXNzQnV0dG9uID0gZGlzbWlzc2libGVcbiAgICA/IDxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzTmFtZT1cImNsb3NlXCIgb25DbGljaz17b25DbG9zZU1lc3NhZ2V9PjxzcGFuPiZ0aW1lczs8L3NwYW4+PC9idXR0b24+XG4gICAgOiBudWxsO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9e2N4KFwiYWxlcnRcIiwgYGFsZXJ0LSR7YWxlcnRMZXZlbH1gLCB7XCJhbGVydC1kaXNtaXNzaWJsZVwiOiBkaXNtaXNzaWJsZX0pfSByb2xlPVwiYWxlcnRcIj5cbiAgICAgIHtkaXNtaXNzQnV0dG9ufVxuICAgICAge3Byb3BzLmNoaWxkcmVufVxuICAgIDwvZGl2PlxuICApXG59OyIsImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgRGF0YXNldENhcmRzIGZyb20gXCIuL2RhdGFzZXQtY2FyZHNcIjtcbmltcG9ydCBGb290ZXIgZnJvbSBcIi4vZm9vdGVyXCI7XG5cbmNvbnN0IEZPT1RFUl9IRUlHSFQgPSA4MTtcblxuZnVuY3Rpb24gUGFnZShwcm9wcykge1xuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwicGFnZVwiPlxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW4gaGktR3JlZW4gY29udGFpbmVyLWZsdWlkXCI+XG4gICAgICAgIDxuYXYgY2xhc3NOYW1lPVwibmF2YmFyIFwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cIm5hdmJhci1oZWFkZXJcIj4gPGEgY2xhc3NOYW1lPVwibmF2YmFyLWJyYW5kXCIgaHJlZj1cIiNcIj48aW1nIHNyYz1cImltYWdlcy9sb2dvLXRpbWJ1Y3Rvby5zdmdcIiBjbGFzc05hbWU9XCJsb2dvXCIgYWx0PVwidGltYnVjdG9vXCIvPjwvYT4gPC9kaXY+XG4gICAgICAgICAgICA8ZGl2IGlkPVwibmF2YmFyXCIgY2xhc3NOYW1lPVwibmF2YmFyLWNvbGxhcHNlIGNvbGxhcHNlXCI+XG4gICAgICAgICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2YmFyLW5hdiBuYXZiYXItcmlnaHRcIj5cbiAgICAgICAgICAgICAgICB7cHJvcHMudXNlcm5hbWUgPyA8bGk+PGEgaHJlZj17cHJvcHMudXNlcmxvY2F0aW9uIHx8ICcjJ30+PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi11c2VyXCIvPiB7cHJvcHMudXNlcm5hbWV9PC9hPjwvbGk+IDogbnVsbH1cbiAgICAgICAgICAgICAgPC91bD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L25hdj5cbiAgICAgIDwvZGl2PlxuICAgICAgPGRpdiAgc3R5bGU9e3ttYXJnaW5Cb3R0b206IGAke0ZPT1RFUl9IRUlHSFR9cHhgfX0+XG4gICAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICAgICAge3Byb3BzLnZyZXMgJiYgcHJvcHMuc2hvd0RhdGFzZXRzID8gKFxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgICA8RGF0YXNldENhcmRzIGNhcHRpb249XCJFeHBsb3JlIGFsbCBkYXRhc2V0c1wiIHZyZXM9e3Byb3BzLnZyZXN9IC8+XG4gICAgICAgICAgPC9kaXY+KSA6IG51bGx9XG4gICAgICA8L2Rpdj5cbiAgICAgIDxGb290ZXIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgUGFnZTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBBZGRQcm9wZXJ0eSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgbmV3TmFtZTogXCJcIixcbiAgICAgIG5ld1R5cGU6IG51bGxcbiAgICB9O1xuICB9XG5cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBuZXdOYW1lLCBuZXdUeXBlIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IHsgb25BZGRDdXN0b21Qcm9wZXJ0eSwgYXJjaGV0eXBlRmllbGRzLCBhdmFpbGFibGVBcmNoZXR5cGVzIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgcmVsYXRpb25UeXBlT3B0aW9ucyA9IGFyY2hldHlwZUZpZWxkc1xuICAgICAgLmZpbHRlcigocHJvcCkgPT4gcHJvcC50eXBlID09PSBcInJlbGF0aW9uXCIpXG4gICAgICAuZmlsdGVyKChwcm9wKSA9PiBhdmFpbGFibGVBcmNoZXR5cGVzLmluZGV4T2YocHJvcC5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uKSA+IC0xKVxuICAgICAgLm1hcCgocHJvcCkgPT4gPHNwYW4ga2V5PXtwcm9wLm5hbWV9IHZhbHVlPXtwcm9wLm5hbWV9Pntwcm9wLm5hbWV9PC9zcGFuPik7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJyb3cgc21hbGwtbWFyZ2luXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTJcIj5cbiAgICAgICAgICB7bmV3VHlwZSA9PT0gXCJyZWxhdGlvblwiXG4gICAgICAgICAgICA/IChcbiAgICAgICAgICAgICAgPFNlbGVjdEZpZWxkXG4gICAgICAgICAgICAgICAgdmFsdWU9e25ld05hbWV9XG4gICAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWx1ZSkgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3TmFtZTogdmFsdWV9KX1cbiAgICAgICAgICAgICAgICBvbkNsZWFyPXsoKSA9PiB0aGlzLnNldFN0YXRlKHtuZXdOYW1lOiBcIlwifSl9PlxuICAgICAgICAgICAgICAgIDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPkNob29zZSBhIHJlbGF0aW9uIHR5cGUuLi48L3NwYW4+XG4gICAgICAgICAgICAgICAge3JlbGF0aW9uVHlwZU9wdGlvbnN9XG4gICAgICAgICAgICAgIDwvU2VsZWN0RmllbGQ+XG4gICAgICAgICAgICApIDogKFxuICAgICAgICAgICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdOYW1lOiBldi50YXJnZXQudmFsdWV9KX1cbiAgICAgICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcj1cIlByb3BlcnR5IG5hbWVcIlxuICAgICAgICAgICAgICAgICAgICAgIHZhbHVlPXtuZXdOYW1lfVxuICAgICAgICAgICAgICAgICAgICAgIGRpc2FibGVkPXtuZXdUeXBlICE9PSBcInRleHRcIn0vPlxuICAgICAgICAgICAgKVxuICAgICAgICAgIH1cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTJcIiA+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwicHVsbC1yaWdodFwiPlxuICAgICAgICAgICAgPFNlbGVjdEZpZWxkXG4gICAgICAgICAgICAgIHZhbHVlPXtuZXdUeXBlfVxuICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbHVlKSA9PiB0aGlzLnNldFN0YXRlKHtuZXdUeXBlOiB2YWx1ZSwgbmV3TmFtZTogdmFsdWUgPT09IFwicmVsYXRpb25cIiA/IG51bGwgOiBuZXdOYW1lfSl9XG4gICAgICAgICAgICAgIG9uQ2xlYXI9eygpID0+IHRoaXMuc2V0U3RhdGUoe25ld1R5cGU6IG51bGx9KX0+XG4gICAgICAgICAgICAgIDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPkNob29zZSBhIHR5cGUuLi48L3NwYW4+XG4gICAgICAgICAgICAgIDxzcGFuIHZhbHVlPVwidGV4dFwiPlRleHQ8L3NwYW4+XG4gICAgICAgICAgICAgIDxzcGFuIHZhbHVlPVwicmVsYXRpb25cIj5SZWxhdGlvbjwvc3Bhbj5cbiAgICAgICAgICAgIDwvU2VsZWN0RmllbGQ+XG4gICAgICAgICAgPC9zcGFuPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNlwiPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMlwiPlxuXG4gICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0IGJ0biBidG4tZGVmYXVsdFwiIGRpc2FibGVkPXshKG5ld05hbWUgJiYgbmV3VHlwZSl9XG4gICAgICAgICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IG51bGwsIG5ld1R5cGU6IG51bGx9KTtcbiAgICAgICAgICAgICAgICAgICAgb25BZGRDdXN0b21Qcm9wZXJ0eShuZXdOYW1lLCBuZXdUeXBlKTtcbiAgICAgICAgICAgICAgICAgIH19PlxuICAgICAgICAgICAgQWRkIHByb3BlcnR5XG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbkFkZFByb3BlcnR5LnByb3BUeXBlcyA9IHtcbiAgaW1wb3J0RGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcbiAgbWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG9uQWRkQ3VzdG9tUHJvcGVydHk6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBBZGRQcm9wZXJ0eTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5cbmNsYXNzIENvbHVtblNlbGVjdCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBjb2x1bW5zLCBwcm9wZXJ0eU1hcHBpbmcsIG9uQ29sdW1uU2VsZWN0LCBvbkNsZWFyQ29sdW1uLCBwbGFjZWhvbGRlciB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHNlbGVjdGVkQ29sdW1uID0gcHJvcGVydHlNYXBwaW5nICYmIHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXSA/IHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXS52YXJpYWJsZU5hbWUgOiBudWxsO1xuXG4gICAgcmV0dXJuIHByb3BlcnR5TWFwcGluZyAmJiBwcm9wZXJ0eU1hcHBpbmcuY29uZmlybWVkID8gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsIHBhZC02LTEyXCIgPjxpbWcgc3JjPVwiaW1hZ2VzL2ljb24tZXhjZWwuc3ZnXCIgYWx0PVwiXCIvPiB7c2VsZWN0ZWRDb2x1bW59PC9kaXY+XG4gICAgKSA6IChcbiAgICAgIDxTZWxlY3RGaWVsZCB2YWx1ZT17c2VsZWN0ZWRDb2x1bW59XG4gICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyhjb2x1bW4pID0+IG9uQ29sdW1uU2VsZWN0KFt7dmFyaWFibGVOYW1lOiBjb2x1bW59XSl9XG4gICAgICAgICAgICAgICAgICAgb25DbGVhcj17KCkgPT4gb25DbGVhckNvbHVtbigwKX0+XG4gICAgICAgIDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiIGNsYXNzTmFtZT1cImZyb20tZXhjZWxcIj48aW1nIHNyYz1cImltYWdlcy9pY29uLWV4Y2VsLnN2Z1wiIGFsdD1cIlwiLz4ge3BsYWNlaG9sZGVyIHx8IFwiU2VsZWN0IGFuIGV4Y2VsIGNvbHVtblwifTwvc3Bhbj5cbiAgICAgICAge2NvbHVtbnMuZmlsdGVyKChjb2wpID0+IGNvbC5uYW1lID09PSBzZWxlY3RlZENvbHVtbiB8fCAoIWNvbC5pc0NvbmZpcm1lZCAmJiAhY29sLmlzSWdub3JlZCkgKS5tYXAoKGNvbCkgPT4gKFxuICAgICAgICAgIDxzcGFuIGtleT17Y29sLm5hbWV9IHZhbHVlPXtjb2wubmFtZX0gY2xhc3NOYW1lPVwiZnJvbS1leGNlbFwiPjxpbWcgc3JjPVwiaW1hZ2VzL2ljb24tZXhjZWwuc3ZnXCIgYWx0PVwiXCIvPiB7Y29sLm5hbWV9PC9zcGFuPlxuICAgICAgICApKX1cbiAgICAgIDwvU2VsZWN0RmllbGQ+XG4gICAgKTtcbiAgfVxufVxuXG5Db2x1bW5TZWxlY3QucHJvcFR5cGVzID0ge1xuICBjb2xsZWN0aW9uRGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcbiAgbWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG9uQ2xlYXJGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICBvblNldEZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IENvbHVtblNlbGVjdDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuaW1wb3J0IFRleHQgZnJvbSBcIi4vdGV4dFwiO1xuaW1wb3J0IFJlbGF0aW9uIGZyb20gXCIuL3JlbGF0aW9uXCI7XG5cblxuY29uc3QgdHlwZU1hcCA9IHtcbiAgdGV4dDogKHByb3BzKSA9PiA8VGV4dCB7Li4ucHJvcHN9IC8+LFxuICBkYXRhYmxlOiAocHJvcHMpID0+IDxUZXh0IHsuLi5wcm9wc30gLz4sXG4gIHJlbGF0aW9uOiAocHJvcHMpID0+IDxSZWxhdGlvbiB7Li4ucHJvcHN9IC8+XG59O1xuXG5jbGFzcyBQcm9wZXJ0eUZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIGNhbkNvbmZpcm0odmFyaWFibGUpIHtcbiAgICBjb25zdCB7IHR5cGUgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKCF2YXJpYWJsZSB8fCB2YXJpYWJsZS5sZW5ndGggPT09IDApIHsgcmV0dXJuIGZhbHNlOyB9XG4gICAgaWYgKHR5cGUgPT09IFwicmVsYXRpb25cIikge1xuICAgICAgcmV0dXJuIHZhcmlhYmxlWzBdLnZhcmlhYmxlTmFtZSAmJiB2YXJpYWJsZVswXS50YXJnZXRDb2xsZWN0aW9uICYmIHZhcmlhYmxlWzBdLnRhcmdldFZhcmlhYmxlTmFtZTtcbiAgICB9XG4gICAgcmV0dXJuIHZhcmlhYmxlLmZpbHRlcigobSkgPT4gbS52YXJpYWJsZU5hbWUpLmxlbmd0aCA9PT0gdmFyaWFibGUubGVuZ3RoO1xuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgbmFtZSwgdHlwZSwgY3VzdG9tLCBwcm9wZXJ0eU1hcHBpbmcgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBvblJlbW92ZUN1c3RvbVByb3BlcnR5LCBvbkNvbmZpcm1GaWVsZE1hcHBpbmdzLFxuICAgICAgb25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzLCBvblNldEZpZWxkTWFwcGluZywgb25DbGVhckZpZWxkTWFwcGluZyB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IGNvbmZpcm1lZCA9IHByb3BlcnR5TWFwcGluZyAmJiBwcm9wZXJ0eU1hcHBpbmcuY29uZmlybWVkO1xuXG4gICAgY29uc3QgY29uZmlybUJ1dHRvbiA9IHByb3BlcnR5TWFwcGluZyAmJiB0aGlzLmNhbkNvbmZpcm0ocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlKSAmJiAhY29uZmlybWVkID9cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1zdWNjZXNzXCIgb25DbGljaz17KCkgPT4gb25Db25maXJtRmllbGRNYXBwaW5ncyhuYW1lKX0+Q29uZmlybTwvYnV0dG9uPiA6IGNvbmZpcm1lZCA/XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmtcIiBvbkNsaWNrPXsoKSA9PiBvblVuY29uZmlybUZpZWxkTWFwcGluZ3MobmFtZSl9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJoaS1zdWNjZXNzIGdseXBoaWNvbiBnbHlwaGljb24tb2tcIiAvPjwvYnV0dG9uPiA6IG51bGw7XG5cbiAgICBjb25zdCBmb3JtQ29tcG9uZW50ID0gdHlwZU1hcFt0eXBlXSh7XG4gICAgICAuLi50aGlzLnByb3BzLFxuICAgICAgb25Db2x1bW5TZWxlY3Q6ICh2YWx1ZSkgPT4gb25TZXRGaWVsZE1hcHBpbmcobmFtZSwgdmFsdWUpLFxuICAgICAgb25DbGVhckNvbHVtbjogKHZhbHVlSWR4KSA9PiBvbkNsZWFyRmllbGRNYXBwaW5nKG5hbWUsIHZhbHVlSWR4KVxuICAgIH0pO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwicm93IHNtYWxsLW1hcmdpblwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0yIHBhZC02LTEyXCI+PHN0cm9uZz57bmFtZX08L3N0cm9uZz48L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMiBwYWQtNi0xMlwiID48c3BhbiBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0XCI+KHt0eXBlfSk8L3NwYW4+PC9kaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTVcIj5cbiAgICAgICAgICB7Zm9ybUNvbXBvbmVudH1cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTFcIj5cbiAgICAgICAgICB7IGN1c3RvbVxuICAgICAgICAgICAgPyAoPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIHB1bGwtcmlnaHRcIiB0eXBlPVwiYnV0dG9uXCIgb25DbGljaz17KCkgPT4gb25SZW1vdmVDdXN0b21Qcm9wZXJ0eShuYW1lKX0+XG4gICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiLz5cbiAgICAgICAgICA8L2J1dHRvbj4pXG4gICAgICAgICAgICA6IG51bGwgfVxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMiBoaS1zdWNjZXNzXCI+XG5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0XCI+XG4gICAgICAgICAgICB7Y29uZmlybUJ1dHRvbn1cbiAgICAgICAgICA8L3NwYW4+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBQcm9wZXJ0eUZvcm07IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IENvbHVtblNlbGVjdCBmcm9tIFwiLi9jb2x1bW4tc2VsZWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uQ29sdW1uU2VsZWN0LCBvbkNsZWFyQ29sdW1uLCBwcm9wZXJ0eU1hcHBpbmcsIGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlLCBhcmNoZXR5cGVGaWVsZHMgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCByZWxhdGlvbkluZm8gPSBwcm9wZXJ0eU1hcHBpbmcgJiYgcHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlWzBdXG4gICAgICA/IHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXVxuICAgICAgOiB7fTtcblxuICAgIGNvbnN0IHRhcmdldEFyY2hldHlwZUNvbGxlY3Rpb24gPSBhcmNoZXR5cGVGaWVsZHMuZmluZCgoYWYpID0+IGFmLm5hbWUgPT09IHRoaXMucHJvcHMubmFtZSkucmVsYXRpb24udGFyZ2V0Q29sbGVjdGlvbjtcbiAgICBjb25zdCB0YXJnZXRDb2xsZWN0aW9uQ29sdW1ucyA9IGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlW3RhcmdldEFyY2hldHlwZUNvbGxlY3Rpb25dO1xuICAgIGNvbnN0IHRhcmdldENvbGxlY3Rpb25zID0gdGFyZ2V0Q29sbGVjdGlvbkNvbHVtbnMubWFwKCh0KSA9PiB0LmNvbGxlY3Rpb25OYW1lKTtcblxuICAgIGNvbnN0IHNvdXJjZUNvbHVtblByb3BzID0ge1xuICAgICAgLi4udGhpcy5wcm9wcyxcbiAgICAgIHBsYWNlaG9sZGVyOiBcIlNlbGVjdCBhIHNvdXJjZSBjb2x1bW4uLi5cIixcbiAgICAgIG9uQ29sdW1uU2VsZWN0OiAodmFsdWUpID0+IG9uQ29sdW1uU2VsZWN0KFt7Li4ucmVsYXRpb25JbmZvLCB2YXJpYWJsZU5hbWU6IHZhbHVlWzBdLnZhcmlhYmxlTmFtZX1dKVxuICAgIH07XG5cbiAgICBjb25zb2xlLmxvZyh0YXJnZXRDb2xsZWN0aW9uQ29sdW1ucyk7XG5cbiAgICBjb25zdCB0YXJnZXRDb2x1bW5Qcm9wcyA9IHJlbGF0aW9uSW5mby50YXJnZXRDb2xsZWN0aW9uID8ge1xuICAgICAgLi4udGhpcy5wcm9wcyxcbiAgICAgIHBsYWNlaG9sZGVyOiBcIlNlbGVjdCBhIHRhcmdldCBjb2x1bW4uLi5cIixcbiAgICAgIHByb3BlcnR5TWFwcGluZzoge3ZhcmlhYmxlOiBbe3ZhcmlhYmxlTmFtZTogcmVsYXRpb25JbmZvLnRhcmdldFZhcmlhYmxlTmFtZSB9XX0sXG4gICAgICBjb2x1bW5zOiB0YXJnZXRDb2xsZWN0aW9uQ29sdW1ucy5maW5kKCh0KSA9PiB0LmNvbGxlY3Rpb25OYW1lID09PSByZWxhdGlvbkluZm8udGFyZ2V0Q29sbGVjdGlvbikuY29sdW1uc1xuICAgICAgICAubWFwKChjb2wpID0+ICh7bmFtZTogY29sfSkpLFxuICAgICAgb25Db2x1bW5TZWxlY3Q6ICh2YWx1ZSkgPT4gb25Db2x1bW5TZWxlY3QoW3suLi5yZWxhdGlvbkluZm8sIHRhcmdldFZhcmlhYmxlTmFtZTogdmFsdWVbMF0udmFyaWFibGVOYW1lfV0pXG4gICAgfSA6IG51bGw7XG5cblxuXG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdj5cbiAgICAgICAgPENvbHVtblNlbGVjdCB7Li4uc291cmNlQ29sdW1uUHJvcHN9IC8+XG4gICAgICAgIDxTZWxlY3RGaWVsZCB2YWx1ZT17cmVsYXRpb25JbmZvLnRhcmdldENvbGxlY3Rpb24gfHwgbnVsbH1cbiAgICAgICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uQ29sdW1uU2VsZWN0KFt7Li4ucmVsYXRpb25JbmZvLCB0YXJnZXRDb2xsZWN0aW9uOiB2YWx1ZX1dKX1cbiAgICAgICAgICAgICAgICAgICAgIG9uQ2xlYXI9eygpID0+IG9uQ2xlYXJDb2x1bW4oMCl9PlxuXG4gICAgICAgICAgPHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCIgY2xhc3NOYW1lPVwiZnJvbS1leGNlbFwiPjxpbWcgc3JjPVwiaW1hZ2VzL2ljb24tZXhjZWwuc3ZnXCIgYWx0PVwiXCIvPiBTZWxlY3QgYSB0YXJnZXQgc2hlZXQuLi48L3NwYW4+XG4gICAgICAgICAge3RhcmdldENvbGxlY3Rpb25zLm1hcCgoY29sbGVjdGlvbikgPT4gKFxuICAgICAgICAgICAgPHNwYW4ga2V5PXtjb2xsZWN0aW9ufSB2YWx1ZT17Y29sbGVjdGlvbn0gY2xhc3NOYW1lPVwiZnJvbS1leGNlbFwiPjxpbWcgc3JjPVwiaW1hZ2VzL2ljb24tZXhjZWwuc3ZnXCIgYWx0PVwiXCIvPiB7Y29sbGVjdGlvbn08L3NwYW4+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvU2VsZWN0RmllbGQ+XG4gICAgICAgIHt0YXJnZXRDb2x1bW5Qcm9wc1xuICAgICAgICAgID8gPENvbHVtblNlbGVjdCB7Li4udGFyZ2V0Q29sdW1uUHJvcHN9IC8+XG4gICAgICAgICAgOiBudWxsXG4gICAgICAgIH1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufVxuXG5Gb3JtLnByb3BUeXBlcyA9IHtcbiAgY29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG4gIG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuICBuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBvbkNsZWFyRmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgb25TZXRGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGb3JtO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IENvbHVtblNlbGVjdCBmcm9tIFwiLi9jb2x1bW4tc2VsZWN0XCI7XG5cblxuY2xhc3MgRm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblxuICByZW5kZXIoKSB7XG4gICAgcmV0dXJuIDxDb2x1bW5TZWxlY3Qgey4uLnRoaXMucHJvcHN9IC8+XG4gIH1cbn1cblxuRm9ybS5wcm9wVHlwZXMgPSB7XG4gIGNvbGxlY3Rpb25EYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuICBtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcbiAgbmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcbiAgb25DbGVhckZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIG9uU2V0RmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRm9ybTtcbiIsIi8vIEhlbHBlciBmdW5jdGlvbiBmb3IgcmVhY3QtcmVkdXggY29ubmVjdFxuZnVuY3Rpb24gZ2V0Q29uZmlybWVkQ29scyhwcm9wcywgdmFyaWFibGVzKSB7XG4gIGNvbnN0IHsgbWFwcGluZ3MsIGltcG9ydERhdGE6IHsgYWN0aXZlQ29sbGVjdGlvbiB9IH0gPSBwcm9wcztcblxuICByZXR1cm4gdmFyaWFibGVzXG4gICAgLm1hcCgodmFsdWUsIGkpID0+ICh7dmFsdWU6IHZhbHVlLCBpbmRleDogaX0pKVxuICAgIC5maWx0ZXIoKGNvbFNwZWMpID0+IG1hcHBpbmdzLmNvbGxlY3Rpb25zW2FjdGl2ZUNvbGxlY3Rpb25dLm1hcHBpbmdzXG4gICAgICAuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcbiAgICAgIC5tYXAoKG0pID0+IG0udmFyaWFibGUubWFwKCh2KSA9PiB2LnZhcmlhYmxlTmFtZSkpXG4gICAgICAucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pXG4gICAgICAuaW5kZXhPZihjb2xTcGVjLnZhbHVlKSA+IC0xXG4gICAgKS5tYXAoKGNvbFNwZWMpID0+IGNvbFNwZWMuaW5kZXgpO1xufVxuXG4vLyBIZWxwZXIgZnVuY3Rpb24gZm9yIHJlYWN0LXJlZHV4IGNvbm5lY3RcbmZ1bmN0aW9uIG1hcHBpbmdzQXJlQ29tcGxldGUocHJvcHMsIHNoZWV0KSB7XG4gIGNvbnN0IHsgbWFwcGluZ3MgfSA9IHByb3BzO1xuXG4gIGNvbnN0IGNvbmZpcm1lZENvbENvdW50ID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0ubWFwcGluZ3NcbiAgICAuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcbiAgICAubWFwKChtKSA9PiBtLnZhcmlhYmxlLm1hcCgodikgPT4gdi52YXJpYWJsZU5hbWUpKVxuICAgIC5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSlcbiAgICAuZmlsdGVyKCh4LCBpZHgsIHNlbGYpID0+IHNlbGYuaW5kZXhPZih4KSA9PT0gaWR4KVxuICAgIC5sZW5ndGg7XG5cbiAgcmV0dXJuIGNvbmZpcm1lZENvbENvdW50ICsgbWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0uaWdub3JlZENvbHVtbnMubGVuZ3RoID09PSBzaGVldC52YXJpYWJsZXMubGVuZ3RoO1xufVxuXG4vLyBNb3ZlcyB0byByZWFjdC1yZWR1eCBjb25uZWN0XG5mdW5jdGlvbiB0cmFuc2Zvcm1Qcm9wcyhwcm9wcykge1xuXG4gIGNvbnN0IHsgaW1wb3J0RGF0YTogeyBzaGVldHMsIGFjdGl2ZUNvbGxlY3Rpb24sIHVwbG9hZGVkRmlsZU5hbWUgfSwgbWFwcGluZ3MsIGFyY2hldHlwZSB9ID0gcHJvcHM7XG4gIGNvbnN0IGNvbGxlY3Rpb25EYXRhID0gc2hlZXRzLmZpbmQoKHNoZWV0KSA9PiBzaGVldC5jb2xsZWN0aW9uID09PSBhY3RpdmVDb2xsZWN0aW9uKTtcbiAgY29uc3QgeyByb3dzLCB2YXJpYWJsZXMgfSA9IGNvbGxlY3Rpb25EYXRhO1xuXG4gIGNvbnN0IGNvbmZpcm1lZENvbHMgPSBnZXRDb25maXJtZWRDb2xzKHByb3BzLCB2YXJpYWJsZXMpO1xuICBjb25zdCB7IGlnbm9yZWRDb2x1bW5zIH0gPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXTtcblxuICBjb25zdCBhdmFpbGFibGVBcmNoZXR5cGVzID0gT2JqZWN0LmtleXMobWFwcGluZ3MuY29sbGVjdGlvbnMpLm1hcCgoa2V5KSA9PiBtYXBwaW5ncy5jb2xsZWN0aW9uc1trZXldLmFyY2hldHlwZU5hbWUpO1xuXG4gIHJldHVybiB7XG4gICAgLi4ucHJvcHMsXG4gICAgc2hlZXRzOiBzaGVldHMsXG4gICAgYWN0aXZlQ29sbGVjdGlvbjogYWN0aXZlQ29sbGVjdGlvbixcbiAgICB1cGxvYWRlZEZpbGVOYW1lOiB1cGxvYWRlZEZpbGVOYW1lLFxuICAgIGNvbGxlY3Rpb25UYWJzOiBzaGVldHMubWFwKChzaGVldCkgPT4gKHtcbiAgICAgIGNvbGxlY3Rpb25OYW1lOiBzaGVldC5jb2xsZWN0aW9uLFxuICAgICAgYXJjaGV0eXBlTmFtZTogbWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0uYXJjaGV0eXBlTmFtZSxcbiAgICAgIGFjdGl2ZTogYWN0aXZlQ29sbGVjdGlvbiA9PT0gc2hlZXQuY29sbGVjdGlvbixcbiAgICAgIGNvbXBsZXRlOiBtYXBwaW5nc0FyZUNvbXBsZXRlKHByb3BzLCBzaGVldClcbiAgICB9KSksXG4gICAgcm93czogcm93cy5tYXAoKHJvdykgPT4gcm93Lm1hcCgoY2VsbCwgaSkgPT4gKHtcbiAgICAgIHZhbHVlOiBjZWxsLnZhbHVlLFxuICAgICAgZXJyb3I6IGNlbGwuZXJyb3IgfHwgbnVsbCxcbiAgICAgIGlnbm9yZWQ6IGlnbm9yZWRDb2x1bW5zLmluZGV4T2YodmFyaWFibGVzW2ldKSA+IC0xXG4gICAgfSkpKSxcbiAgICBoZWFkZXJzOiB2YXJpYWJsZXMubWFwKCh2YXJpYWJsZSwgaSkgPT4gKHtcbiAgICAgIG5hbWU6IHZhcmlhYmxlLFxuICAgICAgaXNDb25maXJtZWQ6IGlnbm9yZWRDb2x1bW5zLmluZGV4T2YoaSkgPCAwICYmIGNvbmZpcm1lZENvbHMuaW5kZXhPZihpKSA+IC0xLFxuICAgICAgaXNJZ25vcmVkOiBpZ25vcmVkQ29sdW1ucy5pbmRleE9mKHZhcmlhYmxlKSA+IC0xXG4gICAgfSkpLFxuICAgIG5leHRVcmw6IHNoZWV0cy5maW5kKChzaGVldCkgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aXZlQ29sbGVjdGlvbikubmV4dFVybCxcbiAgICBhcmNoZXR5cGVGaWVsZHM6IGFyY2hldHlwZVttYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXS5hcmNoZXR5cGVOYW1lXSxcbiAgICBwcm9wZXJ0eU1hcHBpbmdzOiBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXS5tYXBwaW5ncyxcbiAgICBjdXN0b21Qcm9wZXJ0eU1hcHBpbmdzOiBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXS5jdXN0b21Qcm9wZXJ0aWVzLFxuICAgIGF2YWlsYWJsZUFyY2hldHlwZXM6IGF2YWlsYWJsZUFyY2hldHlwZXMsXG4gICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGU6IGF2YWlsYWJsZUFyY2hldHlwZXMubWFwKChhcmNoZXR5cGVOYW1lKSA9PiAoe1xuICAgICAga2V5OiBhcmNoZXR5cGVOYW1lLFxuICAgICAgdmFsdWVzOiBPYmplY3Qua2V5cyhtYXBwaW5ncy5jb2xsZWN0aW9ucylcbiAgICAgICAgLmZpbHRlcigoY29sbGVjdGlvbk5hbWUpID0+IG1hcHBpbmdzLmNvbGxlY3Rpb25zW2NvbGxlY3Rpb25OYW1lXS5hcmNoZXR5cGVOYW1lID09PSBhcmNoZXR5cGVOYW1lKVxuICAgICAgICAubWFwKChjb2xsZWN0aW9uTmFtZSkgPT4gKHtcbiAgICAgICAgICBjb2xsZWN0aW9uTmFtZTogY29sbGVjdGlvbk5hbWUsXG4gICAgICAgICAgY29sdW1uczogc2hlZXRzLmZpbmQoKHNoZWV0KSA9PiBzaGVldC5jb2xsZWN0aW9uID09PSBjb2xsZWN0aW9uTmFtZSkudmFyaWFibGVzXG4gICAgICAgIH0pKVxuICAgIH0pKS5yZWR1Y2UoKGFyY2hldHlwZVRvQ29sbGVjdGlvbkNvbHVtbk1hcHBpbmcsIGN1cnJlbnRBcmNoZXR5cGVBbmRDb2xzKSA9PiB7XG4gICAgICBhcmNoZXR5cGVUb0NvbGxlY3Rpb25Db2x1bW5NYXBwaW5nW2N1cnJlbnRBcmNoZXR5cGVBbmRDb2xzLmtleV0gPSBjdXJyZW50QXJjaGV0eXBlQW5kQ29scy52YWx1ZXM7XG4gICAgICByZXR1cm4gYXJjaGV0eXBlVG9Db2xsZWN0aW9uQ29sdW1uTWFwcGluZztcbiAgICB9LCB7fSlcbiAgfTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgdHJhbnNmb3JtUHJvcHM7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIERhdGFSb3cgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IHJvdyB9ID0gdGhpcy5wcm9wcztcbiAgICByZXR1cm4gKFxuICAgICAgPHRyPlxuICAgICAgICB7cm93Lm1hcCgoY2VsbCwgaSkgPT4gKFxuICAgICAgICAgIDx0ZCBjbGFzc05hbWU9e2N4KHtcbiAgICAgICAgICAgIGlnbm9yZWQ6IGNlbGwuaWdub3JlZCxcbiAgICAgICAgICAgIGRhbmdlcjogY2VsbC5lcnJvciA/IHRydWUgOiBmYWxzZVxuICAgICAgICAgIH0pfSBrZXk9e2l9PlxuICAgICAgICAgICAge2NlbGwudmFsdWV9XG4gICAgICAgICAgICB7Y2VsbC5lcnJvciA/IDxzcGFuIGNsYXNzTmFtZT1cInB1bGwtcmlnaHQgZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgc3R5bGU9e3tjdXJzb3I6IFwicG9pbnRlclwifX0gdGl0bGU9e2NlbGwuZXJyb3J9IC8+IDogbnVsbH1cbiAgICAgICAgICA8L3RkPlxuICAgICAgICApKX1cbiAgICAgIDwvdHI+XG4gICAgKTtcbiAgfVxufVxuXG5EYXRhUm93LnByb3BUeXBlcyA9IHtcbiAgcm93OiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXksXG59O1xuXG5leHBvcnQgZGVmYXVsdCBEYXRhUm93OyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5jbGFzcyBIZWFkZXJDZWxsIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBoZWFkZXIsIGlzQ29uZmlybWVkLCBpc0lnbm9yZWQsIG9uSWdub3JlQ29sdW1uVG9nZ2xlIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDx0aCBjbGFzc05hbWU9e2N4KHtcbiAgICAgICAgc3VjY2VzczogaXNDb25maXJtZWQsXG4gICAgICAgIGluZm86ICFpc0NvbmZpcm1lZCAmJiAhaXNJZ25vcmVkLFxuICAgICAgICBkYW5nZXI6ICFpc0NvbmZpcm1lZCAmJiBpc0lnbm9yZWRcbiAgICAgIH0pfT5cbiAgICAgICAge2hlYWRlcn1cbiAgICAgICAgPGEgc3R5bGU9e3tjdXJzb3I6IFwicG9pbnRlclwifX0gY2xhc3NOYW1lPXtjeChcInB1bGwtcmlnaHRcIiwgXCJnbHlwaGljb25cIiwge1xuICAgICAgICAgIFwiZ2x5cGhpY29uLW9rLXNpZ25cIjogaXNDb25maXJtZWQsXG4gICAgICAgICAgXCJnbHlwaGljb24tcXVlc3Rpb24tc2lnblwiOiAhaXNDb25maXJtZWQgJiYgIWlzSWdub3JlZCxcbiAgICAgICAgICBcImdseXBoaWNvbi1yZW1vdmVcIjogIWlzQ29uZmlybWVkICYmIGlzSWdub3JlZFxuICAgICAgICB9KX0gb25DbGljaz17KCkgPT4gIWlzQ29uZmlybWVkID8gb25JZ25vcmVDb2x1bW5Ub2dnbGUoaGVhZGVyKSA6IG51bGwgfSA+XG4gICAgICAgIDwvYT5cbiAgICAgIDwvdGg+XG4gICAgKTtcbiAgfVxufVxuXG5IZWFkZXJDZWxsLnByb3BUeXBlcyA9IHtcbiAgaGVhZGVyOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBpc0NvbmZpcm1lZDogUmVhY3QuUHJvcFR5cGVzLmJvb2wsXG4gIGlzSWdub3JlZDogUmVhY3QuUHJvcFR5cGVzLmJvb2wsXG4gIG9uSWdub3JlQ29sdW1uVG9nZ2xlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgSGVhZGVyQ2VsbDsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgVXBsb2FkQnV0dG9uIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBjbGFzc05hbWVzLCBpc1VwbG9hZGluZywgb25VcGxvYWRGaWxlU2VsZWN0LCBnbHlwaGljb24sIGxhYmVsIH0gPSB0aGlzLnByb3BzO1xuICAgIHJldHVybiAoXG4gICAgICA8Zm9ybT5cbiAgICAgICAgPGxhYmVsIGNsYXNzTmFtZT17Y3goLi4uY2xhc3NOYW1lcywge2Rpc2FibGVkOiBpc1VwbG9hZGluZ30pfT5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9e2dseXBoaWNvbn0+PC9zcGFuPlxuICAgICAgICAgIHtcIiBcIn1cbiAgICAgICAgICB7aXNVcGxvYWRpbmcgPyBcIlVwbG9hZGluZy4uLlwiIDogbGFiZWwgfVxuICAgICAgICAgIDxpbnB1dFxuICAgICAgICAgICAgZGlzYWJsZWQ9e2lzVXBsb2FkaW5nfVxuICAgICAgICAgICAgb25DaGFuZ2U9e2UgPT4gb25VcGxvYWRGaWxlU2VsZWN0KGUudGFyZ2V0LmZpbGVzKX1cbiAgICAgICAgICAgIHN0eWxlPXt7ZGlzcGxheTogXCJub25lXCJ9fVxuICAgICAgICAgICAgdHlwZT1cImZpbGVcIiAvPlxuICAgICAgICA8L2xhYmVsPlxuICAgICAgPC9mb3JtPlxuICAgICk7XG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgVXBsb2FkQnV0dG9uOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBSZWFjdERPTSBmcm9tIFwicmVhY3QtZG9tXCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCB4aHIgZnJvbSBcInhoclwiO1xuaW1wb3J0IHhocm1vY2sgZnJvbSBcInhoci1tb2NrXCI7XG5pbXBvcnQgc2V0dXBNb2NrcyBmcm9tIFwiLi9zZXJ2ZXJtb2Nrc1wiO1xuaW1wb3J0IHJvdXRlciBmcm9tIFwiLi9yb3V0ZXJcIjtcbmltcG9ydCBhY3Rpb25zTWFrZXIgZnJvbSBcIi4vYWN0aW9uc1wiO1xuaW1wb3J0IHsgbmF2aWdhdGVUbyB9IGZyb20gXCIuL3JvdXRlclwiO1xuXG5cbmlmIChwcm9jZXNzLmVudi5VU0VfTU9DSyA9PT0gXCJ0cnVlXCIpIHtcblx0Y29uc29sZS5sb2coXCJVc2luZyBtb2NrIHNlcnZlciFcIilcblx0dmFyIG9yaWcgPSB3aW5kb3cuWE1MSHR0cFJlcXVlc3Q7XG5cdHhocm1vY2suc2V0dXAoKTsgLy9tb2NrIHdpbmRvdy5YTUxIdHRwUmVxdWVzdCB1c2FnZXNcblx0dmFyIG1vY2sgPSB3aW5kb3cuWE1MSHR0cFJlcXVlc3Q7XG5cdHdpbmRvdy5YTUxIdHRwUmVxdWVzdCA9IG9yaWc7XG5cdHhoci5YTUxIdHRwUmVxdWVzdCA9IG1vY2s7XG5cdHhoci5YRG9tYWluUmVxdWVzdCA9IG1vY2s7XG5cdHNldHVwTW9ja3MoeGhybW9jaywgb3JpZyk7XG59XG5sZXQgYWN0aW9ucyA9IGFjdGlvbnNNYWtlcihuYXZpZ2F0ZVRvLCBzdG9yZS5kaXNwYXRjaC5iaW5kKHN0b3JlKSk7XG5cbmZ1bmN0aW9uIGNoZWNrVG9rZW5JblVybChzdGF0ZSkge1xuXHRsZXQgcGF0aCA9IHdpbmRvdy5sb2NhdGlvbi5zZWFyY2guc3Vic3RyKDEpO1xuXHRsZXQgcGFyYW1zID0gcGF0aC5zcGxpdCgnJicpO1xuXG5cdGZvcihsZXQgaSBpbiBwYXJhbXMpIHtcblx0XHRsZXQgW2tleSwgdmFsdWVdID0gcGFyYW1zW2ldLnNwbGl0KCc9Jyk7XG5cdFx0aWYoa2V5ID09PSAnaHNpZCcgJiYgIXN0YXRlLnVzZXJkYXRhLnVzZXJJZCkge1xuXHRcdFx0YWN0aW9ucy5vblRva2VuKHZhbHVlKTtcblxuXG5cdFx0XHRicmVhaztcblx0XHR9XG5cdH1cbn1cblxuZG9jdW1lbnQuYWRkRXZlbnRMaXN0ZW5lcihcIkRPTUNvbnRlbnRMb2FkZWRcIiwgKCkgPT4ge1xuXHRsZXQgc3RhdGUgPSBzdG9yZS5nZXRTdGF0ZSgpO1xuXHRjaGVja1Rva2VuSW5Vcmwoc3RhdGUpO1xuXHRpZiAoIXN0YXRlLmFyY2hldHlwZSB8fCBPYmplY3Qua2V5cyhzdGF0ZS5hcmNoZXR5cGUpLmxlbmd0aCA9PT0gMCkge1xuXHRcdHhocihwcm9jZXNzLmVudi5zZXJ2ZXIgKyBcIi92Mi4xL21ldGFkYXRhL0FkbWluXCIsIChlcnIsIHJlc3ApID0+IHtcblx0XHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNFVF9BUkNIRVRZUEVfTUVUQURBVEFcIiwgZGF0YTogSlNPTi5wYXJzZShyZXNwLmJvZHkpfSk7XG5cdFx0fSk7XG5cdH1cblx0UmVhY3RET00ucmVuZGVyKFxuXHRcdHJvdXRlcixcblx0XHRkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKVxuXHQpXG59KTtcbiIsImltcG9ydCB7IGdldEl0ZW0gfSBmcm9tIFwiLi4vdXRpbC9wZXJzaXN0XCI7XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IGdldEl0ZW0oXCJhcmNoZXR5cGVcIikgfHwge307XG5cblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfQVJDSEVUWVBFX01FVEFEQVRBXCI6XG5cdFx0XHRyZXR1cm4gYWN0aW9uLmRhdGE7XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwiaW1wb3J0IHsgZ2V0SXRlbSB9IGZyb20gXCIuLi91dGlsL3BlcnNpc3RcIjtcblxuY29uc3QgaW5pdGlhbFN0YXRlID0gZ2V0SXRlbShcImltcG9ydERhdGFcIikgfHwge1xuXHRpc1VwbG9hZGluZzogZmFsc2UsXG5cdHNoZWV0czogbnVsbCxcblx0YWN0aXZlQ29sbGVjdGlvbjogbnVsbCxcblx0cHVibGlzaEVycm9yczogZmFsc2Vcbn07XG5cbmZ1bmN0aW9uIGZpbmRJbmRleChhcnIsIGYpIHtcblx0bGV0IGxlbmd0aCA9IGFyci5sZW5ndGg7XG5cdGZvciAodmFyIGkgPSAwOyBpIDwgbGVuZ3RoOyBpKyspIHtcbiAgICBpZiAoZihhcnJbaV0sIGksIGFycikpIHtcbiAgICAgIHJldHVybiBpO1xuICAgIH1cbiAgfVxuXHRyZXR1cm4gLTE7XG59XG5cbmZ1bmN0aW9uIHNoZWV0Um93RnJvbURpY3RUb0FycmF5KHJvd2RpY3QsIGFycmF5T2ZWYXJpYWJsZU5hbWVzLCBtYXBwaW5nRXJyb3JzKSB7XG5cdHJldHVybiBhcnJheU9mVmFyaWFibGVOYW1lcy5tYXAobmFtZSA9PiAoe1xuXHRcdHZhbHVlOiByb3dkaWN0W25hbWVdLFxuXHRcdGVycm9yOiBtYXBwaW5nRXJyb3JzW25hbWVdIHx8IHVuZGVmaW5lZFxuXHR9KSk7XG59XG5cbmZ1bmN0aW9uIGFkZFJvd3MoY3VyUm93cywgbmV3Um93cywgYXJyYXlPZlZhcmlhYmxlTmFtZXMpIHtcblx0cmV0dXJuIGN1clJvd3MuY29uY2F0KFxuXHRcdG5ld1Jvd3MubWFwKGl0ZW0gPT4gc2hlZXRSb3dGcm9tRGljdFRvQXJyYXkoaXRlbS52YWx1ZXMgfHwge30sIGFycmF5T2ZWYXJpYWJsZU5hbWVzLCBpdGVtLmVycm9ycyB8fCB7fSkpXG5cdCk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU1RBUlRfVVBMT0FEXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCBpc1VwbG9hZGluZzogdHJ1ZX07XG5cdFx0Y2FzZSBcIkZJTklTSF9VUExPQURcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsXG5cdFx0XHRcdGlzVXBsb2FkaW5nOiBmYWxzZSxcblx0XHRcdFx0cHVibGlzaEVycm9yczogZmFsc2UsXG5cdFx0XHRcdHNoZWV0czogYWN0aW9uLmRhdGEuY29sbGVjdGlvbnMubWFwKHNoZWV0ID0+ICh7XG5cdFx0XHRcdFx0Y29sbGVjdGlvbjogc2hlZXQubmFtZSxcblx0XHRcdFx0XHR2YXJpYWJsZXM6IHNoZWV0LnZhcmlhYmxlcyxcblx0XHRcdFx0XHRyb3dzOiBbXSxcblx0XHRcdFx0XHRuZXh0VXJsOiBzaGVldC5kYXRhLFxuXHRcdFx0XHRcdG5leHRVcmxXaXRoRXJyb3JzOiBzaGVldC5kYXRhV2l0aEVycm9yc1xuXHRcdFx0XHR9KSksXG5cdFx0XHRcdGFjdGl2ZUNvbGxlY3Rpb246IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zWzBdLm5hbWUsXG5cdFx0XHRcdHZyZTogYWN0aW9uLmRhdGEudnJlLFxuXHRcdFx0XHRzYXZlTWFwcGluZ1VybDogYWN0aW9uLmRhdGEuc2F2ZU1hcHBpbmcsXG5cdFx0XHRcdGV4ZWN1dGVNYXBwaW5nVXJsOiBhY3Rpb24uZGF0YS5leGVjdXRlTWFwcGluZ1xuXHRcdFx0fTtcblx0XHRjYXNlIFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX1NVQ0NFRURFRFwiOlxuXHRcdFx0bGV0IHNoZWV0SWR4ID0gZmluZEluZGV4KHN0YXRlLnNoZWV0cywgc2hlZXQgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aW9uLmNvbGxlY3Rpb24pO1xuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdHNoZWV0czogW1xuXHRcdFx0XHRcdC4uLnN0YXRlLnNoZWV0cy5zbGljZSgwLCBzaGVldElkeCksXG5cdFx0XHRcdFx0e1xuXHRcdFx0XHRcdFx0Li4uc3RhdGUuc2hlZXRzW3NoZWV0SWR4XSxcblx0XHRcdFx0XHRcdHJvd3M6IGFkZFJvd3Moc3RhdGUuc2hlZXRzW3NoZWV0SWR4XS5yb3dzLCBhY3Rpb24uZGF0YS5pdGVtcywgc3RhdGUuc2hlZXRzW3NoZWV0SWR4XS52YXJpYWJsZXMpLFxuXHRcdFx0XHRcdFx0bmV4dFVybDogYWN0aW9uLmRhdGEuX25leHRcblx0XHRcdFx0XHR9LFxuXHRcdFx0XHRcdC4uLnN0YXRlLnNoZWV0cy5zbGljZShzaGVldElkeCArIDEpXG5cdFx0XHRcdF1cblx0XHRcdH07XG5cblx0XHRjYXNlIFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0ZJTklTSEVEXCI6XG5cdFx0XHRjb25zdCByZXN1bHQgPSB7Li4uc3RhdGV9O1xuXHRcdFx0cmVzdWx0LnNoZWV0cyA9IHJlc3VsdC5zaGVldHMuc2xpY2UoKTtcblx0XHRcdHJlc3VsdC5zaGVldHNcblx0XHRcdFx0LmZvckVhY2goKHNoZWV0LCBpKSA9PiB7XG5cdFx0XHRcdFx0aWYgKHNoZWV0LmNvbGxlY3Rpb24gPT09IGFjdGlvbi5jb2xsZWN0aW9uKSB7XG5cdFx0XHRcdFx0XHRyZXN1bHQuc2hlZXRzW2ldID0ge1xuXHRcdFx0XHRcdFx0XHQuLi5zaGVldCxcblx0XHRcdFx0XHRcdFx0aXNMb2FkaW5nOiBmYWxzZVxuXHRcdFx0XHRcdFx0fTtcblx0XHRcdFx0XHR9XG5cdFx0XHRcdH0pO1xuXG5cdFx0XHRyZXR1cm4gcmVzdWx0O1xuXHRcdGNhc2UgXCJTRVRfQUNUSVZFX0NPTExFQ1RJT05cIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIGFjdGl2ZUNvbGxlY3Rpb246IGFjdGlvbi5jb2xsZWN0aW9ufTtcblx0XHRjYXNlIFwiUFVCTElTSF9IQURfRVJST1JcIjpcblx0XHRcdC8vIGNsZWFyIHRoZSBzaGVldHMgdG8gZm9yY2UgcmVsb2FkXG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0cHVibGlzaEVycm9yczogdHJ1ZSxcblx0XHRcdFx0c2hlZXRzOiBzdGF0ZS5zaGVldHMubWFwKChzaGVldCkgPT4gKHtcblx0XHRcdFx0XHQuLi5zaGVldCxcblx0XHRcdFx0XHRyb3dzOiBbXSxcblx0XHRcdFx0XHRuZXh0VXJsOiBzaGVldC5uZXh0VXJsV2l0aEVycm9yc1xuXHRcdFx0XHR9KSlcblx0XHRcdH07XG5cdFx0Y2FzZSBcIlBVQkxJU0hfU1VDQ0VFREVEXCI6XG5cdFx0XHQvLyBjbGVhciB0aGUgc2hlZXRzIHRvIGZvcmNlIHJlbG9hZFxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdHB1Ymxpc2hFcnJvcnM6IGZhbHNlLFxuXHRcdFx0XHRzaGVldHM6IHN0YXRlLnNoZWV0cy5tYXAoKHNoZWV0KSA9PiAoe1xuXHRcdFx0XHRcdC4uLnNoZWV0LFxuXHRcdFx0XHRcdHJvd3M6IFtdLFxuXHRcdFx0XHRcdG5leHRVcmw6IHNoZWV0Lm5leHRVcmxXaXRoRXJyb3JzXG5cdFx0XHRcdH0pKVxuXHRcdFx0fTtcblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn1cbiIsImltcG9ydCBpbXBvcnREYXRhIGZyb20gXCIuL2ltcG9ydC1kYXRhXCI7XG5pbXBvcnQgYXJjaGV0eXBlIGZyb20gXCIuL2FyY2hldHlwZVwiO1xuaW1wb3J0IG1hcHBpbmdzIGZyb20gXCIuL21hcHBpbmdzXCI7XG5pbXBvcnQgdXNlcmRhdGEgZnJvbSBcIi4vdXNlcmRhdGFcIjtcblxuZXhwb3J0IGRlZmF1bHQge1xuXHRpbXBvcnREYXRhOiBpbXBvcnREYXRhLFxuXHRhcmNoZXR5cGU6IGFyY2hldHlwZSxcblx0bWFwcGluZ3M6IG1hcHBpbmdzLFxuXHR1c2VyZGF0YTogdXNlcmRhdGFcbn07XG4iLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5pbXBvcnQgZ2V0SW4gZnJvbSBcIi4uL3V0aWwvZ2V0LWluXCI7XG5pbXBvcnQgeyBnZXRJdGVtIH0gZnJvbSBcIi4uL3V0aWwvcGVyc2lzdFwiO1xuXG5jb25zdCBuZXdWYXJpYWJsZURlc2MgPSAocHJvcGVydHksIHZhcmlhYmxlU3BlYykgPT4gKHtcblx0cHJvcGVydHk6IHByb3BlcnR5LFxuXHR2YXJpYWJsZTogdmFyaWFibGVTcGVjLFxuXHRkZWZhdWx0VmFsdWU6IFtdLFxuXHRjb25maXJtZWQ6IGZhbHNlLFxuXHR2YWx1ZU1hcHBpbmdzOiB7fVxufSk7XG5cbmZ1bmN0aW9uIHNjYWZmb2xkQ29sbGVjdGlvbk1hcHBpbmdzKGluaXQsIHNoZWV0KSB7XG5cdHJldHVybiBPYmplY3QuYXNzaWduKGluaXQsIHtcblx0XHRbc2hlZXQubmFtZV06IHtcblx0XHRcdGFyY2hldHlwZU5hbWU6IG51bGwsXG5cdFx0XHRtYXBwaW5nczogW10sXG5cdFx0XHRpZ25vcmVkQ29sdW1uczogW10sXG5cdFx0XHRjdXN0b21Qcm9wZXJ0aWVzOiBbXVxuXHRcdH1cblx0fSk7XG59XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IGdldEl0ZW0oXCJtYXBwaW5nc1wiKSB8fCB7XG5cdGNvbGxlY3Rpb25zOiB7fSxcblx0Y29uZmlybWVkOiBmYWxzZVxufTtcblxuY29uc3QgZ2V0TWFwcGluZ0luZGV4ID0gKHN0YXRlLCBhY3Rpb24pID0+XG5cdHN0YXRlLmNvbGxlY3Rpb25zW2FjdGlvbi5jb2xsZWN0aW9uXS5tYXBwaW5nc1xuXHRcdC5tYXAoKG0sIGkpID0+ICh7aW5kZXg6IGksIG06IG19KSlcblx0XHQuZmlsdGVyKChtU3BlYykgPT4gbVNwZWMubS5wcm9wZXJ0eSA9PT0gYWN0aW9uLnByb3BlcnR5RmllbGQpXG5cdFx0LnJlZHVjZSgocHJldiwgY3VyKSA9PiBjdXIuaW5kZXgsIC0xKTtcblxuY29uc3QgbWFwQ29sbGVjdGlvbkFyY2hldHlwZSA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGxldCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJhcmNoZXR5cGVOYW1lXCJdLCBhY3Rpb24udmFsdWUsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblx0bmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIFtdLCBuZXdDb2xsZWN0aW9ucyk7XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmNvbnN0IHVwc2VydEZpZWxkTWFwcGluZyA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGZvdW5kSWR4ID0gZ2V0TWFwcGluZ0luZGV4KHN0YXRlLCBhY3Rpb24pO1xuXHRjb25zdCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiLCBmb3VuZElkeCA8IDAgPyBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKS5sZW5ndGggOiBmb3VuZElkeF0sXG5cdFx0bmV3VmFyaWFibGVEZXNjKGFjdGlvbi5wcm9wZXJ0eUZpZWxkLCBhY3Rpb24uaW1wb3J0ZWRGaWVsZCksIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuXG5cdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCBjbGVhckZpZWxkTWFwcGluZyA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGZvdW5kSWR4ID0gZ2V0TWFwcGluZ0luZGV4KHN0YXRlLCBhY3Rpb24pO1xuXHRpZiAoZm91bmRJZHggPCAwKSB7IHJldHVybiBzdGF0ZTsgfVxuXG5cdGNvbnN0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHgsIFwidmFyaWFibGVcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKVxuXHRcdC5maWx0ZXIoKG0sIGkpID0+IGkgIT09IGFjdGlvbi5jbGVhckluZGV4KTtcblxuXHRsZXQgbmV3Q29sbGVjdGlvbnM7XG5cdGlmIChjdXJyZW50Lmxlbmd0aCA+IDApIHtcblx0XHRuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiLCBmb3VuZElkeCwgXCJ2YXJpYWJsZVwiXSwgY3VycmVudCwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXHR9IGVsc2Uge1xuXHRcdGNvbnN0IG5ld01hcHBpbmdzID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucylcblx0XHRcdC5maWx0ZXIoKG0sIGkpID0+IGkgIT09IGZvdW5kSWR4KTtcblx0XHRuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgbmV3TWFwcGluZ3MsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblx0fVxuXG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmNvbnN0IHNldERlZmF1bHRWYWx1ZSA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGZvdW5kSWR4ID0gZ2V0TWFwcGluZ0luZGV4KHN0YXRlLCBhY3Rpb24pO1xuXHRpZiAoZm91bmRJZHggPiAtMSkge1xuXHRcdGNvbnN0IG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCIsIGZvdW5kSWR4LCBcImRlZmF1bHRWYWx1ZVwiXSwgYWN0aW9uLnZhbHVlLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdFx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn07XG5cbmNvbnN0IHNldEZpZWxkQ29uZmlybWF0aW9uID0gKHN0YXRlLCBhY3Rpb24sIHZhbHVlKSA9PiB7XG5cdGNvbnN0IGN1cnJlbnQgPSAoZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucykgfHwgW10pXG5cdFx0Lm1hcCgodm0pID0+ICh7Li4udm0sIGNvbmZpcm1lZDogYWN0aW9uLnByb3BlcnR5RmllbGQgPT09IHZtLnByb3BlcnR5ID8gdmFsdWUgOiB2bS5jb25maXJtZWR9KSk7XG5cdGxldCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgY3VycmVudCwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXG5cdGlmICh2YWx1ZSA9PT0gdHJ1ZSkge1xuXHRcdGNvbnN0IGNvbmZpcm1lZFZhcmlhYmxlTmFtZXMgPSBjdXJyZW50Lm1hcCgobSkgPT4gbS52YXJpYWJsZS5tYXAoKHYpID0+IHYudmFyaWFibGVOYW1lKSkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSk7XG5cdFx0Y29uc3QgbmV3SWdub3JlZENvbHVtcyA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJpZ25vcmVkQ29sdW1uc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpXG5cdFx0XHQuZmlsdGVyKChpYykgPT4gY29uZmlybWVkVmFyaWFibGVOYW1lcy5pbmRleE9mKGljKSA8IDApO1xuXHRcdG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImlnbm9yZWRDb2x1bW5zXCJdLCBuZXdJZ25vcmVkQ29sdW1zLCBuZXdDb2xsZWN0aW9ucyk7XG5cdH1cblxuXHRyZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuY29uc3Qgc2V0VmFsdWVNYXBwaW5nID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcblx0Y29uc3QgZm91bmRJZHggPSBnZXRNYXBwaW5nSW5kZXgoc3RhdGUsIGFjdGlvbik7XG5cblx0aWYgKGZvdW5kSWR4ID4gLTEpIHtcblx0XHRjb25zdCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiLCBmb3VuZElkeCwgXCJ2YWx1ZU1hcHBpbmdzXCIsIGFjdGlvbi50aW1WYWx1ZV0sXG5cdFx0XHRhY3Rpb24ubWFwVmFsdWUsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblx0XHRyZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xuXHR9XG5cdHJldHVybiBzdGF0ZTtcbn07XG5cbmNvbnN0IHRvZ2dsZUlnbm9yZWRDb2x1bW4gPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRsZXQgY3VycmVudCA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJpZ25vcmVkQ29sdW1uc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXG5cdGlmIChjdXJyZW50LmluZGV4T2YoYWN0aW9uLnZhcmlhYmxlTmFtZSkgPCAwKSB7XG5cdFx0Y3VycmVudC5wdXNoKGFjdGlvbi52YXJpYWJsZU5hbWUpO1xuXHR9IGVsc2Uge1xuXHRcdGN1cnJlbnQgPSBjdXJyZW50LmZpbHRlcigoYykgPT4gYyAhPT0gYWN0aW9uLnZhcmlhYmxlTmFtZSk7XG5cdH1cblxuXHRyZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImlnbm9yZWRDb2x1bW5zXCJdLCBjdXJyZW50LCBzdGF0ZS5jb2xsZWN0aW9ucykgfTtcbn07XG5cbmNvbnN0IGFkZEN1c3RvbVByb3BlcnR5ID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcblx0Y29uc3QgY3VycmVudCA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJjdXN0b21Qcm9wZXJ0aWVzXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdGNvbnN0IG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcImN1c3RvbVByb3BlcnRpZXNcIiwgY3VycmVudC5sZW5ndGhdLCB7bmFtZTogYWN0aW9uLnByb3BlcnR5RmllbGQsIHR5cGU6IGFjdGlvbi5wcm9wZXJ0eVR5cGV9LCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmNvbnN0IHJlbW92ZUN1c3RvbVByb3BlcnR5ID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcblx0Y29uc3QgZm91bmRJZHggPSBnZXRNYXBwaW5nSW5kZXgoc3RhdGUsIGFjdGlvbik7XG5cblx0Y29uc3QgY3VycmVudCA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJjdXN0b21Qcm9wZXJ0aWVzXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucylcblx0XHQuZmlsdGVyKChjcCkgPT4gY3AubmFtZSAhPT0gYWN0aW9uLnByb3BlcnR5RmllbGQpO1xuXG5cdGxldCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJjdXN0b21Qcm9wZXJ0aWVzXCJdLCBjdXJyZW50LCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cblx0aWYgKGZvdW5kSWR4ID4gLTEpIHtcblx0XHRjb25zdCBuZXdNYXBwaW5ncyA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpXG5cdFx0XHQuZmlsdGVyKChtLCBpKSA9PiBpICE9PSBmb3VuZElkeCk7XG5cdFx0bmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIG5ld01hcHBpbmdzLCBuZXdDb2xsZWN0aW9ucyk7XG5cdH1cblxuXHRyZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJGSU5JU0hfVVBMT0FEXCI6XG5cdFx0XHRyZXR1cm4gT2JqZWN0LmtleXMoc3RhdGUuY29sbGVjdGlvbnMpLmxlbmd0aCA+IDAgP1xuXHRcdFx0XHRzdGF0ZSA6XG5cdFx0XHRcdHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zLnJlZHVjZShzY2FmZm9sZENvbGxlY3Rpb25NYXBwaW5ncywge30pfTtcblxuXHRcdGNhc2UgXCJNQVBfQ09MTEVDVElPTl9BUkNIRVRZUEVcIjpcblx0XHRcdHJldHVybiBtYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIkNPTkZJUk1fQ09MTEVDVElPTl9BUkNIRVRZUEVfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIGNvbmZpcm1lZDogdHJ1ZX07XG5cblx0XHRjYXNlIFwiU0VUX0ZJRUxEX01BUFBJTkdcIjpcblx0XHRcdHJldHVybiB1cHNlcnRGaWVsZE1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiQ0xFQVJfRklFTERfTUFQUElOR1wiOlxuXHRcdFx0cmV0dXJuIGNsZWFyRmllbGRNYXBwaW5nKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIlNFVF9ERUZBVUxUX1ZBTFVFXCI6XG5cdFx0XHRyZXR1cm4gc2V0RGVmYXVsdFZhbHVlKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIkNPTkZJUk1fRklFTERfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiBzZXRGaWVsZENvbmZpcm1hdGlvbihzdGF0ZSwgYWN0aW9uLCB0cnVlKTtcblxuXHRcdGNhc2UgXCJVTkNPTkZJUk1fRklFTERfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiBzZXRGaWVsZENvbmZpcm1hdGlvbihzdGF0ZSwgYWN0aW9uLCBmYWxzZSk7XG5cblx0XHRjYXNlIFwiU0VUX1ZBTFVFX01BUFBJTkdcIjpcblx0XHRcdHJldHVybiBzZXRWYWx1ZU1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiVE9HR0xFX0lHTk9SRURfQ09MVU1OXCI6XG5cdFx0XHRyZXR1cm4gdG9nZ2xlSWdub3JlZENvbHVtbihzdGF0ZSwgYWN0aW9uKTtcblxuXHRcdGNhc2UgXCJBRERfQ1VTVE9NX1BST1BFUlRZXCI6XG5cdFx0XHRyZXR1cm4gYWRkQ3VzdG9tUHJvcGVydHkoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiUkVNT1ZFX0NVU1RPTV9QUk9QRVJUWVwiOlxuXHRcdFx0cmV0dXJuIHJlbW92ZUN1c3RvbVByb3BlcnR5KHN0YXRlLCBhY3Rpb24pO1xuXHR9XG5cdHJldHVybiBzdGF0ZTtcbn1cbiIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgdXNlcklkOiB1bmRlZmluZWQsXG4gIG15VnJlczogdW5kZWZpbmVkLFxuICB2cmVzOiB7fVxufTtcblxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuICBzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG4gICAgY2FzZSBcIkxPR0lOXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICB1c2VySWQ6IGFjdGlvbi5kYXRhLFxuICAgICAgICBteVZyZXM6IGFjdGlvbi52cmVEYXRhID8gYWN0aW9uLnZyZURhdGEubWluZSA6IHt9LFxuICAgICAgICB2cmVzOiBhY3Rpb24udnJlRGF0YSA/IGFjdGlvbi52cmVEYXRhLnB1YmxpYyA6IHt9XG4gICAgICB9O1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufSIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBzdGF0ZVRvTWFwRGF0YVByb3BzIGZyb20gXCIuL2NvbXBvbmVudHMvcmVhY3QtcmVkdXgtY29ubmVjdC9jb25uZWN0LWRhdGFcIjtcblxuLy8gaW1wb3J0IHtVcGxvYWRTcGxhc2hTY3JlZW4sIEFyY2hldHlwZU1hcHBpbmdzLCBEYXRhc2hlZXRNYXBwaW5ncywgQ29sbGVjdGlvbnNPdmVydmlld30gZnJvbSBcIi4vY29tcG9uZW50c1wiO1xuXG5pbXBvcnQgQXBwIGZyb20gXCIuL2NvbXBvbmVudHMvYXBwLmpzeFwiO1xuXG5pbXBvcnQgeyBSb3V0ZXIsIFJvdXRlLCBoYXNoSGlzdG9yeSB9IGZyb20gJ3JlYWN0LXJvdXRlcidcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vYWN0aW9uc1wiO1xuaW1wb3J0IHsgUHJvdmlkZXIsIGNvbm5lY3QgfSBmcm9tIFwicmVhY3QtcmVkdXhcIlxuXG52YXIgdXJscyA9IHtcbiAgbWFwRGF0YSgpIHtcbiAgICByZXR1cm4gXCIvbWFwZGF0YVwiO1xuICB9LFxuICBtYXBBcmNoZXR5cGVzKCkge1xuICAgIHJldHVybiBcIi9tYXBhcmNoZXR5cGVzXCI7XG4gIH0sXG4gIGNvbGxlY3Rpb25zT3ZlcnZpZXcoKSB7XG4gICAgcmV0dXJuIFwiL2NvbGxlY3Rpb25zLW92ZXJ2aWV3XCI7XG4gIH1cbn07XG5cbmV4cG9ydCBmdW5jdGlvbiBuYXZpZ2F0ZVRvKGtleSwgYXJncykge1xuICBoYXNoSGlzdG9yeS5wdXNoKHVybHNba2V5XS5hcHBseShudWxsLCBhcmdzKSk7XG59XG5cbi8vVGhlIGN1cnJlbnQgY29kZSBpc24ndCB3cml0dGVuIHdpdGggdGhpcyBpbiBtaW5kLiBJJ3ZlIG9ubHkgYWRkZWQgdGhlIGNvbm5lY3Rcbi8vZnVuY3Rpb24gbGF0ZXIuIEZlZWwgZnJlZSB0byB1c2UgeW91ciBvd24gbWFwU3RhdGVUb1Byb3BzIGZvciB5b3VyIG93blxuLy9jb21wb25lbnRcbmNvbnN0IG1ha2VDb250YWluZXJDb21wb25lbnQgPSBjb25uZWN0KHN0YXRlID0+IHN0YXRlLCBkaXNwYXRjaCA9PiBhY3Rpb25zKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSk7XG5cbmNvbnN0IG1ha2VNYXBEYXRhQ29udGFpbmVyID0gY29ubmVjdChzdGF0ZVRvTWFwRGF0YVByb3BzLCBkaXNwYXRjaCA9PiBhY3Rpb25zKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSk7XG5cbnZhciByb3V0ZXIgPSAoXG4gIDxQcm92aWRlciBzdG9yZT17c3RvcmV9PlxuICAgIDxSb3V0ZXIgaGlzdG9yeT17aGFzaEhpc3Rvcnl9PlxuICAgICAgPFJvdXRlIHBhdGg9XCIvXCIgY29tcG9uZW50PXttYWtlQ29udGFpbmVyQ29tcG9uZW50KEFwcCl9Lz5cbiAgICAgIDxSb3V0ZSBwYXRoPXt1cmxzLmNvbGxlY3Rpb25zT3ZlcnZpZXcoKX0gY29tcG9uZW50PXttYWtlQ29udGFpbmVyQ29tcG9uZW50KEFwcCl9Lz5cbiAgICAgIDxSb3V0ZSBwYXRoPXt1cmxzLm1hcEFyY2hldHlwZXMoKX0gY29tcG9uZW50PXttYWtlQ29udGFpbmVyQ29tcG9uZW50KEFwcCl9Lz5cbiAgICAgIDxSb3V0ZSBwYXRoPXt1cmxzLm1hcERhdGEoKX0gY29tcG9uZW50PXttYWtlTWFwRGF0YUNvbnRhaW5lcihBcHApfS8+XG5cbiAgICA8L1JvdXRlcj5cbiAgPC9Qcm92aWRlcj5cbik7XG5cbmV4cG9ydCBkZWZhdWx0IHJvdXRlcjtcbiIsImV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uIHNldHVwTW9ja3MoeGhybW9jaywgb3JpZykge1xuICB4aHJtb2NrXG4gICAgLmdldChcImh0dHA6Ly90ZXN0LnJlcG9zaXRvcnkuaHV5Z2Vucy5rbmF3Lm5sL3YyLjEvbWV0YWRhdGEvQWRtaW5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5ib2R5KGB7XG4gICAgICAgICAgXCJwZXJzb25zXCI6IFtcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwibmFtZVwiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJ0ZXh0XCJcbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcImhhc1dyaXR0ZW5cIixcbiAgICAgICAgICAgICAgXCJ0eXBlXCI6IFwicmVsYXRpb25cIixcbiAgICAgICAgICAgICAgXCJxdWlja3NlYXJjaFwiOiBcIi92Mi4xL2RvbWFpbi9kb2N1bWVudHMvYXV0b2NvbXBsZXRlXCIsXG4gICAgICAgICAgICAgIFwicmVsYXRpb25cIjoge1xuICAgICAgICAgICAgICAgIFwiZGlyZWN0aW9uXCI6IFwiT1VUXCIsXG4gICAgICAgICAgICAgICAgXCJvdXROYW1lXCI6IFwiaGFzV3JpdHRlblwiLFxuICAgICAgICAgICAgICAgIFwiaW5OYW1lXCI6IFwid2FzV3JpdHRlbkJ5XCIsXG4gICAgICAgICAgICAgICAgXCJ0YXJnZXRDb2xsZWN0aW9uXCI6IFwiZG9jdW1lbnRzXCIsXG4gICAgICAgICAgICAgICAgXCJyZWxhdGlvbkNvbGxlY3Rpb25cIjogXCJyZWxhdGlvbnNcIixcbiAgICAgICAgICAgICAgICBcInJlbGF0aW9uVHlwZUlkXCI6IFwiYmJhMTBkMzctODZjYy00ZjFmLWJhMmQtMDE2YWYyYjIxYWE0XCJcbiAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwiaXNSZWxhdGVkVG9cIixcbiAgICAgICAgICAgICAgXCJ0eXBlXCI6IFwicmVsYXRpb25cIixcbiAgICAgICAgICAgICAgXCJxdWlja3NlYXJjaFwiOiBcIi92Mi4xL2RvbWFpbi9wZXJzb25zL2F1dG9jb21wbGV0ZVwiLFxuICAgICAgICAgICAgICBcInJlbGF0aW9uXCI6IHtcbiAgICAgICAgICAgICAgICBcImRpcmVjdGlvblwiOiBcIk9VVFwiLFxuICAgICAgICAgICAgICAgIFwib3V0TmFtZVwiOiBcImlzUmVsYXRlZFRvXCIsXG4gICAgICAgICAgICAgICAgXCJpbk5hbWVcIjogXCJpc1JlbGF0ZWRUb1wiLFxuICAgICAgICAgICAgICAgIFwidGFyZ2V0Q29sbGVjdGlvblwiOiBcInBlcnNvbnNcIixcbiAgICAgICAgICAgICAgICBcInJlbGF0aW9uQ29sbGVjdGlvblwiOiBcInJlbGF0aW9uc1wiLFxuICAgICAgICAgICAgICAgIFwicmVsYXRpb25UeXBlSWRcIjogXCJjYmExMGQzNy04NmNjLTRmMWYtYmEyZC0wMTZhZjJiMjFhYTVcIlxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgICAgXSxcbiAgICAgICAgICBcImRvY3VtZW50c1wiOiBbXG4gICAgICAgICAgICB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcIm5hbWVcIixcbiAgICAgICAgICAgICAgXCJ0eXBlXCI6IFwidGV4dFwiXG4gICAgICAgICAgICB9XG4gICAgICAgICAgXVxuICAgICAgICB9YCk7XG4gICAgfSlcbiAgICAuZ2V0KFwiaHR0cDovL3Rlc3QucmVwb3NpdG9yeS5odXlnZW5zLmtuYXcubmwvdjIuMS9zeXN0ZW0vdXNlcnMvbWUvdnJlc1wiLCBmdW5jdGlvbihyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZmV0Y2gtbXktdnJlc1wiKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShge1xuICAgICAgICAgIFwibWluZVwiOiB7XG4gICAgICAgICAgICBcIm1pZ3JhbnRfc3RlZWtwcm9lZl9tYXN0ZXJkYiAoNikueGxzeFwiOiB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcIm1pZ3JhbnRfc3RlZWtwcm9lZl9tYXN0ZXJkYiAoNikueGxzeFwiLFxuICAgICAgICAgICAgICBcInB1Ymxpc2hlZFwiOiB0cnVlXG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgXCJ0ZXN0ZXJkZXRlc3RcIjoge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJ0ZXN0ZXJkZXRlc3RcIixcbiAgICAgICAgICAgICAgXCJwdWJsaXNoZWRcIjogZmFsc2UsXG4gICAgICAgICAgICAgIFwicm1sVXJpXCI6IFwiPDxUaGUgZ2V0IHJhdyBkYXRhIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiXG4gICAgICAgICAgICB9XG4gICAgICAgICAgfSxcbiAgICAgICAgICBcInB1YmxpY1wiOiB7XG4gICAgICAgICAgICBcIldvbWVuV3JpdGVyc1wiOiB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcIldvbWVuV3JpdGVyc1wiXG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgXCJDaGFydGVyUG9ydGFhbFwiOiB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcIkNoYXJ0ZXJQb3J0YWFsXCJcbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBcIkV1cm9wZXNlTWlncmF0aWVcIjoge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJFdXJvcGVzZU1pZ3JhdGllXCJcbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBcImNrY2NcIjoge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJja2NjXCJcbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBcIkR1dGNoQ2FyaWJiZWFuXCI6IHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwiRHV0Y2hDYXJpYmJlYW5cIlxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIFwiY3dyc1wiOiB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcImN3cnNcIlxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIFwiY3dub1wiOiB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcImN3bm9cIlxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIFwiQWRtaW5cIjoge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJBZG1pblwiXG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgXCJjbndcIjoge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJjbndcIlxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIFwiQmFzZVwiOiB7XG4gICAgICAgICAgICAgIFwibmFtZVwiOiBcIkJhc2VcIlxuICAgICAgICAgICAgfVxuICAgICAgICAgIH1cbiAgICAgICAgfWApO1xuICAgIH0pXG4gICAgLnBvc3QoXCJodHRwOi8vdGVzdC5yZXBvc2l0b3J5Lmh1eWdlbnMua25hdy5ubC92Mi4xL2J1bGstdXBsb2FkXCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiYnVsay11cGxvYWRcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmhlYWRlcihcIkxvY2F0aW9uXCIsIFwiPDxUaGUgZ2V0IHJhdyBkYXRhIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiKTtcbiAgICB9KVxuICAgIC5wb3N0KFwiPDxUaGUgc2F2ZSBtYXBwaW5nIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcInNhdmUgbWFwcGluZ1wiLCByZXEuYm9keSgpKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjA0KTtcbiAgICB9KVxuICAgIC5wb3N0KFwiPDxUaGUgZXhlY3V0ZSBtYXBwaW5nIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImV4ZWN1dGUgbWFwcGluZyB3aXRoIGZhaWx1cmVzXCIsIHJlcS5ib2R5KCkpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5ib2R5KEpTT04uc3RyaW5naWZ5KHtcbiAgICAgICAgICBzdWNjZXNzOiBmYWxzZVxuICAgICAgICB9KSk7XG4gICAgfSlcbiAgICAuZ2V0KFwiPDxUaGUgZ2V0IHJhdyBkYXRhIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImdldCByYXcgZGF0YVwiKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShKU09OLnN0cmluZ2lmeSh7XG4gICAgICAgICAgdnJlOiBcInRoZXZyZW5hbWVcIixcbiAgICAgICAgICBzYXZlTWFwcGluZzogXCI8PFRoZSBzYXZlIG1hcHBpbmcgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCIsXG4gICAgICAgICAgZXhlY3V0ZU1hcHBpbmc6IFwiPDxUaGUgZXhlY3V0ZSBtYXBwaW5nIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiLFxuICAgICAgICAgIGNvbGxlY3Rpb25zOiBbXG4gICAgICAgICAgICB7XG4gICAgICAgICAgICAgIG5hbWU6IFwibW9ja3BlcnNvbnNcIixcbiAgICAgICAgICAgICAgdmFyaWFibGVzOiBbXCJJRFwiLCBcIlZvb3JuYWFtXCIsIFwidHVzc2Vudm9lZ3NlbFwiLCBcIkFjaHRlcm5hYW1cIiwgXCJHZXNjaHJldmVuRG9jdW1lbnRcIiwgXCJHZW5vZW1kIGluXCIsIFwiSXMgZ2V0cm91d2QgbWV0XCJdLFxuICAgICAgICAgICAgICBkYXRhOiBcIjw8dXJsIGZvciBwZXJzb24gZGF0YT4+XCIsXG4gICAgICAgICAgICAgIGRhdGFXaXRoRXJyb3JzOiBcIjw8dXJsIGZvciBwZXJzb24gZGF0YSB3aXRoIGVycm9ycz4+XCJcbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICB7XG4gICAgICAgICAgICAgIG5hbWU6IFwibW9ja2RvY3VtZW50c1wiLFxuICAgICAgICAgICAgICB2YXJpYWJsZXM6IFtcInRpdGVsXCIsIFwiZGF0dW1cIiwgXCJyZWZlcmVudGllXCIsIFwidXJsXCJdLFxuICAgICAgICAgICAgICBkYXRhOiBcIjw8dXJsIGZvciBkb2N1bWVudCBkYXRhPj5cIixcbiAgICAgICAgICAgICAgZGF0YVdpdGhFcnJvcnM6IFwiPDx1cmwgZm9yIGRvY3VtZW50IGRhdGEgd2l0aCBlcnJvcnM+PlwiXG4gICAgICAgICAgICB9XG4gICAgICAgICAgXVxuICAgICAgICB9KSk7XG4gICAgfSlcbiAgICAuZ2V0KFwiPDx1cmwgZm9yIHBlcnNvbiBkYXRhPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgcGVyc29uIGl0ZW1zIGRhdGFcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgIFwiX25leHRcIjogXCI8PG1vcmUgZGF0YT4+XCIsXG4gICAgICAgICAgICBcIml0ZW1zXCI6IFtcbiAgICAgICAgICAgICAge1xuICAgICAgICAgICAgICAgIHZhbHVlczoge1xuICAgICAgICAgICAgICAgICAgXCJJRFwiOiBcIjFcIixcbiAgICAgICAgICAgICAgICAgIFwiVm9vcm5hYW1cIjogXCJWb29ybmFhbVwiLFxuICAgICAgICAgICAgICAgICAgXCJ0dXNzZW52b2Vnc2VsXCI6IFwidHVzc2Vudm9lZ3NlbFwiLFxuICAgICAgICAgICAgICAgICAgXCJBY2h0ZXJuYWFtXCI6IFwiQWNodGVybmFhbVwiLFxuICAgICAgICAgICAgICAgICAgXCJHZXNjaHJldmVuRG9jdW1lbnRcIjogXCJHZXNjaHJldmVuRG9jdW1lbnRcIixcbiAgICAgICAgICAgICAgICAgIFwiR2Vub2VtZCBpblwiOiBcIkdlbm9lbWQgaW5cIixcbiAgICAgICAgICAgICAgICAgIFwiSXMgZ2V0cm91d2QgbWV0XCI6IFwiSXMgZ2V0cm91d2QgbWV0XCIsXG4gICAgICAgICAgICAgICAgfSxcbiAgICAgICAgICAgICAgICBlcnJvcnM6IHt9XG4gICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgICB2YWx1ZXM6IHtcbiAgICAgICAgICAgICAgICAgIFwiSURcIjogXCIyXCIsXG4gICAgICAgICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwiVm9vcm5hYW1cIixcbiAgICAgICAgICAgICAgICAgIFwidHVzc2Vudm9lZ3NlbFwiOiBcInR1c3NlbnZvZWdzZWxcIixcbiAgICAgICAgICAgICAgICAgIFwiQWNodGVybmFhbVwiOiBcIkFjaHRlcm5hYW1cIixcbiAgICAgICAgICAgICAgICAgIFwiR2VzY2hyZXZlbkRvY3VtZW50XCI6IFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsXG4gICAgICAgICAgICAgICAgICBcIkdlbm9lbWQgaW5cIjogXCJHZW5vZW1kIGluXCIsXG4gICAgICAgICAgICAgICAgICBcIklzIGdldHJvdXdkIG1ldFwiOiBcIklzIGdldHJvdXdkIG1ldFwiLFxuICAgICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgICAgZXJyb3JzOiB7fVxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICBdXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5nZXQoXCI8PHVybCBmb3IgcGVyc29uIGRhdGEgd2l0aCBlcnJvcnM+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImdldCBwZXJzb24gaXRlbXMgZGF0YSB3aXRoIGVycm9yc1wiKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShKU09OLnN0cmluZ2lmeSh7XG4gICAgICAgICAgXCJfbmV4dFwiOiBcIjw8bW9yZSBkYXRhPj5cIixcbiAgICAgICAgICBcIml0ZW1zXCI6IFt7XG4gICAgICAgICAgICB2YWx1ZXM6IHtcbiAgICAgICAgICAgICAgXCJJRFwiOiBcIjFcIixcbiAgICAgICAgICAgICAgXCJWb29ybmFhbVwiOiBcIlZvb3JuYWFtXCIsXG4gICAgICAgICAgICAgIFwidHVzc2Vudm9lZ3NlbFwiOiBcInR1c3NlbnZvZWdzZWxcIixcbiAgICAgICAgICAgICAgXCJBY2h0ZXJuYWFtXCI6IFwiQWNodGVybmFhbVwiLFxuICAgICAgICAgICAgICBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiOiBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiLFxuICAgICAgICAgICAgICBcIkdlbm9lbWQgaW5cIjogXCJHZW5vZW1kIGluXCIsXG4gICAgICAgICAgICAgIFwiSXMgZ2V0cm91d2QgbWV0XCI6IFwiSXMgZ2V0cm91d2QgbWV0XCIsXG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgZXJyb3JzOiB7XG4gICAgICAgICAgICAgIFwiVm9vcm5hYW1cIjogXCJ3aWxsIG5vdCBkb1wiLFxuICAgICAgICAgICAgICBcIkFjaHRlcm5hYW1cIjogXCJhbHNvIGZhaWxlZFwiXG4gICAgICAgICAgICB9XG4gICAgICAgICAgfV1cbiAgICAgICAgfSkpO1xuICAgIH0pXG4gICAgLmdldChcIjw8bW9yZSBkYXRhPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgcGVyc29uIGl0ZW1zIGRhdGFcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgIFwiaXRlbXNcIjogW1xuICAgICAgICAgICAge1xuICAgICAgICAgICAgICB2YWx1ZXM6IHtcbiAgICAgICAgICAgICAgICBcIklEXCI6IFwiM1wiLFxuICAgICAgICAgICAgICAgIFwiVm9vcm5hYW1cIjogXCJWb29ybmFhbVwiLFxuICAgICAgICAgICAgICAgIFwidHVzc2Vudm9lZ3NlbFwiOiBcInR1c3NlbnZvZWdzZWxcIixcbiAgICAgICAgICAgICAgICBcIkFjaHRlcm5hYW1cIjogXCJBY2h0ZXJuYWFtXCIsXG4gICAgICAgICAgICAgICAgXCJHZXNjaHJldmVuRG9jdW1lbnRcIjogXCJHZXNjaHJldmVuRG9jdW1lbnRcIixcbiAgICAgICAgICAgICAgICBcIkdlbm9lbWQgaW5cIjogXCJHZW5vZW1kIGluXCIsXG4gICAgICAgICAgICAgICAgXCJJcyBnZXRyb3V3ZCBtZXRcIjogXCJJcyBnZXRyb3V3ZCBtZXRcIixcbiAgICAgICAgICAgICAgfSxcbiAgICAgICAgICAgICAgZXJyb3JzOiB7fVxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgdmFsdWVzOiB7XG4gICAgICAgICAgICAgICAgXCJJRFwiOiBcIjRcIixcbiAgICAgICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwiVm9vcm5hYW1cIixcbiAgICAgICAgICAgICAgICBcInR1c3NlbnZvZWdzZWxcIjogXCJ0dXNzZW52b2Vnc2VsXCIsXG4gICAgICAgICAgICAgICAgXCJBY2h0ZXJuYWFtXCI6IFwiQWNodGVybmFhbVwiLFxuICAgICAgICAgICAgICAgIFwiR2VzY2hyZXZlbkRvY3VtZW50XCI6IFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsXG4gICAgICAgICAgICAgICAgXCJHZW5vZW1kIGluXCI6IFwiR2Vub2VtZCBpblwiLFxuICAgICAgICAgICAgICAgIFwiSXMgZ2V0cm91d2QgbWV0XCI6IFwiSXMgZ2V0cm91d2QgbWV0XCIsXG4gICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgIGVycm9yczoge31cbiAgICAgICAgICAgIH1cbiAgICAgICAgICBdXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5nZXQoXCI8PHVybCBmb3IgZG9jdW1lbnQgZGF0YT4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZ2V0IGRvY3VtZW50IGl0ZW1zIGRhdGFcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgICAgXCJuYW1lXCI6IFwic29tZUNvbGxlY3Rpb25cIixcbiAgICAgICAgICAgIFwidmFyaWFibGVzXCI6IFtcInRpbV9pZFwiLCBcInZhcjFcIiwgXCJ2YXIyXCJdLFxuICAgICAgICAgICAgXCJpdGVtc1wiOiBbXG4gICAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgICB2YWx1ZXM6IHtcbiAgICAgICAgICAgICAgICAgIFwidGltX2lkXCI6IFwiMVwiLFxuICAgICAgICAgICAgICAgICAgXCJ0aXRlbFwiOiBcInRpdGVsXCIsXG4gICAgICAgICAgICAgICAgICBcImRhdHVtXCI6IFwiZGF0dW1cIixcbiAgICAgICAgICAgICAgICAgIFwicmVmZXJlbnRpZVwiOiBcInJlZmVyZW50aWVcIixcbiAgICAgICAgICAgICAgICAgIFwidXJsXCI6IFwidXJsXCIsXG4gICAgICAgICAgICAgICAgfSxcbiAgICAgICAgICAgICAgICBlcnJvcnM6IHt9XG4gICAgICAgICAgICAgIH0sXG4gICAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgICB2YWx1ZXM6IHtcbiAgICAgICAgICAgICAgICAgIFwidGltX2lkXCI6IFwiMlwiLFxuICAgICAgICAgICAgICAgICAgXCJ0aXRlbFwiOiBcInRpdGVsXCIsXG4gICAgICAgICAgICAgICAgICBcImRhdHVtXCI6IFwiZGF0dW1cIixcbiAgICAgICAgICAgICAgICAgIFwicmVmZXJlbnRpZVwiOiBcInJlZmVyZW50aWVcIixcbiAgICAgICAgICAgICAgICAgIFwidXJsXCI6IFwidXJsXCIsXG4gICAgICAgICAgICAgICAgfSxcbiAgICAgICAgICAgICAgICBlcnJvcnM6IHt9XG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIF1cbiAgICAgICAgfSkpO1xuICAgIH0pXG4gICAgLmdldChcIjw8dXJsIGZvciBkb2N1bWVudCBkYXRhIHdpdGggZXJyb3JzPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgZG9jdW1lbnQgaXRlbXMgZGF0YSB3aXRoIGVycm9yc1wiKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShKU09OLnN0cmluZ2lmeSh7XG4gICAgICAgICAgXCJuYW1lXCI6IFwic29tZUNvbGxlY3Rpb25cIixcbiAgICAgICAgICBcInZhcmlhYmxlc1wiOiBbXCJ0aW1faWRcIiwgXCJ2YXIxXCIsIFwidmFyMlwiXSxcbiAgICAgICAgICBcIml0ZW1zXCI6IFtdXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5tb2NrKGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUuZXJyb3IoXCJ1bm1vY2tlZCByZXF1ZXN0XCIsIHJlcS51cmwoKSwgcmVxLCByZXNwKTtcbiAgICB9KTtcbn1cbiIsImltcG9ydCB7Y3JlYXRlU3RvcmUsIGFwcGx5TWlkZGxld2FyZSwgY29tYmluZVJlZHVjZXJzLCBjb21wb3NlfSBmcm9tIFwicmVkdXhcIjtcbmltcG9ydCB0aHVua01pZGRsZXdhcmUgZnJvbSBcInJlZHV4LXRodW5rXCI7XG5cbmltcG9ydCB7IHBlcnNpc3QgfSBmcm9tIFwiLi91dGlsL3BlcnNpc3RcIjtcbmltcG9ydCByZWR1Y2VycyBmcm9tIFwiLi9yZWR1Y2Vyc1wiO1xuXG5sZXQgc3RvcmUgPSBjcmVhdGVTdG9yZShcbiAgY29tYmluZVJlZHVjZXJzKHJlZHVjZXJzKSxcbiAgY29tcG9zZShcbiAgICBhcHBseU1pZGRsZXdhcmUoXG4gICAgICB0aHVua01pZGRsZXdhcmVcbiAgICApLFxuICAgIHdpbmRvdy5kZXZUb29sc0V4dGVuc2lvbiA/IHdpbmRvdy5kZXZUb29sc0V4dGVuc2lvbigpIDogZiA9PiBmXG4gIClcbik7XG5cbi8vIHdpbmRvdy5vbmJlZm9yZXVubG9hZCA9ICgpID0+IHBlcnNpc3Qoc3RvcmUuZ2V0U3RhdGUoKSk7XG5cbmV4cG9ydCBkZWZhdWx0IHN0b3JlO1xuIiwiZnVuY3Rpb24gZGVlcENsb25lOShvYmopIHtcbiAgICB2YXIgaSwgbGVuLCByZXQ7XG5cbiAgICBpZiAodHlwZW9mIG9iaiAhPT0gXCJvYmplY3RcIiB8fCBvYmogPT09IG51bGwpIHtcbiAgICAgICAgcmV0dXJuIG9iajtcbiAgICB9XG5cbiAgICBpZiAoQXJyYXkuaXNBcnJheShvYmopKSB7XG4gICAgICAgIHJldCA9IFtdO1xuICAgICAgICBsZW4gPSBvYmoubGVuZ3RoO1xuICAgICAgICBmb3IgKGkgPSAwOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgICAgIHJldC5wdXNoKCAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldICk7XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICByZXQgPSB7fTtcbiAgICAgICAgZm9yIChpIGluIG9iaikge1xuICAgICAgICAgICAgaWYgKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkge1xuICAgICAgICAgICAgICAgIHJldFtpXSA9ICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV07XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHJldDtcbn1cblxuZXhwb3J0IGRlZmF1bHQgZGVlcENsb25lOTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4vY2xvbmUtZGVlcFwiO1xuXG5jb25zdCBfZ2V0SW4gPSAocGF0aCwgZGF0YSkgPT5cblx0ZGF0YSA/XG5cdFx0cGF0aC5sZW5ndGggPT09IDAgPyBkYXRhIDogX2dldEluKHBhdGgsIGRhdGFbcGF0aC5zaGlmdCgpXSkgOlxuXHRcdG51bGw7XG5cblxuXG5jb25zdCBnZXRJbiA9IChwYXRoLCBkYXRhKSA9PlxuXHRfZ2V0SW4oY2xvbmUocGF0aCksIGRhdGEpO1xuXG5cbmV4cG9ydCBkZWZhdWx0IGdldEluOyIsImV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uIG1hcHBpbmdUb0pzb25MZFJtbChtYXBwaW5nLCB2cmUpIHtcbiAgcmV0dXJuIHtcbiAgXHRcIkBjb250ZXh0XCI6IHtcbiAgXHRcdFwiQHZvY2FiXCI6IFwiaHR0cDovL3d3dy53My5vcmcvbnMvcjJybWwjXCIsXG4gIFx0XHRcInJtbFwiOiBcImh0dHA6Ly9zZW13ZWIubW1sYWIuYmUvbnMvcm1sI1wiLFxuICBcdFx0XCJ0aW1cIjogXCJodHRwOi8vdGltYnVjdG9vLmNvbS9tYXBwaW5nI1wiLFxuICAgICAgXCJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzAxL3JkZi1zY2hlbWEjc3ViQ2xhc3NPZlwiOiB7XG4gICAgICAgIFwiQHR5cGVcIjogXCJAaWRcIlxuICAgICAgfSxcbiAgXHRcdFwicHJlZGljYXRlXCI6IHtcbiAgXHRcdFx0XCJAdHlwZVwiOiBcIkBpZFwiXG4gIFx0XHR9LFxuICAgICAgXCJwYXJlbnRUcmlwbGVzTWFwXCI6IHtcbiAgICAgICAgXCJAdHlwZVwiOiBcIkBpZFwiXG4gICAgICB9LFxuICAgICAgXCJjbGFzc1wiOiB7XG4gICAgICAgIFwiQHR5cGVcIjogXCJAaWRcIlxuICAgICAgfVxuICBcdH0sXG4gIFx0XCJAZ3JhcGhcIjogT2JqZWN0LmtleXMobWFwcGluZy5jb2xsZWN0aW9ucykubWFwKGtleSA9PiBtYXBTaGVldChrZXksIG1hcHBpbmcuY29sbGVjdGlvbnNba2V5XSwgdnJlKSlcbiAgfTtcbn1cblxuZnVuY3Rpb24gbWFrZU1hcE5hbWUodnJlLCBsb2NhbE5hbWUpIHtcbiAgcmV0dXJuIGBodHRwOi8vdGltYnVjdG9vLmNvbS9tYXBwaW5nLyR7dnJlfS8ke2xvY2FsTmFtZX1gO1xufVxuXG5mdW5jdGlvbiBtYXBTaGVldChrZXksIHNoZWV0LCB2cmUpIHtcbiAgLy9GSVhNRTogbW92ZSBsb2dpY2FsU291cmNlIGFuZCBzdWJqZWN0TWFwIHVuZGVyIHRoZSBjb250cm9sIG9mIHRoZSBzZXJ2ZXJcbiAgcmV0dXJuIHtcbiAgICBcIkBpZFwiOiBtYWtlTWFwTmFtZSh2cmUsIGtleSksXG4gICAgXCJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzAxL3JkZi1zY2hlbWEjc3ViQ2xhc3NPZlwiOiBgaHR0cDovL3RpbWJ1Y3Rvby5jb20vJHtzaGVldC5hcmNoZXR5cGVOYW1lLnJlcGxhY2UoL3MkLywgXCJcIil9YCxcbiAgICBcInJtbDpsb2dpY2FsU291cmNlXCI6IHtcblx0XHRcdFwicm1sOnNvdXJjZVwiOiB7XG5cdFx0XHRcdFwidGltOnJhd0NvbGxlY3Rpb25cIjoga2V5LFxuXHRcdFx0XHRcInRpbTp2cmVOYW1lXCI6IHZyZVxuXHRcdFx0fVxuXHRcdH0sXG4gICAgXCJzdWJqZWN0TWFwXCI6IHtcblx0XHRcdFwiY2xhc3NcIjogbWFrZU1hcE5hbWUodnJlLCBrZXkpLFxuXHRcdFx0XCJ0ZW1wbGF0ZVwiOiBgJHttYWtlTWFwTmFtZSh2cmUsIGtleSl9L3t0aW1faWR9YFxuXHRcdH0sXG4gICAgXCJwcmVkaWNhdGVPYmplY3RNYXBcIjogc2hlZXQubWFwcGluZ3MubWFwKG1ha2VQcmVkaWNhdGVPYmplY3RNYXAuYmluZChudWxsLCB2cmUpKVxuICB9O1xufVxuXG5mdW5jdGlvbiBtYWtlUHJlZGljYXRlT2JqZWN0TWFwKHZyZSwgbWFwcGluZykge1xuICBsZXQgcHJvcGVydHkgPSBtYXBwaW5nLnByb3BlcnR5O1xuICBsZXQgdmFyaWFibGUgPSBtYXBwaW5nLnZhcmlhYmxlWzBdO1xuICBpZiAodmFyaWFibGUudGFyZ2V0Q29sbGVjdGlvbikge1xuICAgIHJldHVybiB7XG4gICAgICBcIm9iamVjdE1hcFwiOiB7XG4gICAgICAgIFwiam9pbkNvbmRpdGlvblwiOiB7XG4gICAgICAgICAgXCJjaGlsZFwiOiB2YXJpYWJsZS52YXJpYWJsZU5hbWUsXG4gICAgICAgICAgXCJwYXJlbnRcIjogdmFyaWFibGUudGFyZ2V0VmFyaWFibGVOYW1lXG4gICAgICAgIH0sXG4gICAgICAgIFwicGFyZW50VHJpcGxlc01hcFwiOiBtYWtlTWFwTmFtZSh2cmUsIHZhcmlhYmxlLnRhcmdldENvbGxlY3Rpb24pXG4gICAgICB9LFxuICAgICAgXCJwcmVkaWNhdGVcIjogYGh0dHA6Ly90aW1idWN0b28uY29tLyR7cHJvcGVydHl9YFxuICAgIH1cbiAgfSBlbHNlIHtcbiAgICByZXR1cm4ge1xuICAgICAgXCJvYmplY3RNYXBcIjoge1xuICAgICAgICBcImNvbHVtblwiOiB2YXJpYWJsZS52YXJpYWJsZU5hbWVcbiAgICAgIH0sXG4gICAgICBcInByZWRpY2F0ZVwiOiBgaHR0cDovL3RpbWJ1Y3Rvby5jb20vJHtwcm9wZXJ0eX1gXG4gICAgfVxuICB9XG59XG4iLCJsZXQgcGVyc2lzdERpc2FibGVkID0gZmFsc2U7XG5cbmNvbnN0IHBlcnNpc3QgPSAoc3RhdGUpID0+IHtcblx0aWYgKCBwZXJzaXN0RGlzYWJsZWQgKSB7IHJldHVybjsgfVxuXHRmb3IgKGxldCBrZXkgaW4gc3RhdGUpIHtcblx0XHRsb2NhbFN0b3JhZ2Uuc2V0SXRlbShrZXksIEpTT04uc3RyaW5naWZ5KHN0YXRlW2tleV0pKTtcblx0fVxufTtcblxuY29uc3QgZ2V0SXRlbSA9IChrZXkpID0+IHtcblx0aWYgKGxvY2FsU3RvcmFnZS5nZXRJdGVtKGtleSkpIHtcblx0XHRyZXR1cm4gSlNPTi5wYXJzZShsb2NhbFN0b3JhZ2UuZ2V0SXRlbShrZXkpKTtcblx0fVxuXHRyZXR1cm4gbnVsbDtcbn07XG5cbmNvbnN0IGRpc2FibGVQZXJzaXN0ID0gKCkgPT4ge1xuXHRsb2NhbFN0b3JhZ2UuY2xlYXIoKTtcblx0cGVyc2lzdERpc2FibGVkID0gdHJ1ZTtcbn07XG53aW5kb3cuZGlzYWJsZVBlcnNpc3QgPSBkaXNhYmxlUGVyc2lzdDtcblxuZXhwb3J0IHsgcGVyc2lzdCwgZ2V0SXRlbSwgZGlzYWJsZVBlcnNpc3QgfTtcbiIsImltcG9ydCBjbG9uZSBmcm9tIFwiLi9jbG9uZS1kZWVwXCI7XG5cbi8vIERvIGVpdGhlciBvZiB0aGVzZTpcbi8vICBhKSBTZXQgYSB2YWx1ZSBieSByZWZlcmVuY2UgaWYgZGVyZWYgaXMgbm90IG51bGxcbi8vICBiKSBTZXQgYSB2YWx1ZSBkaXJlY3RseSBpbiB0byBkYXRhIG9iamVjdCBpZiBkZXJlZiBpcyBudWxsXG5jb25zdCBzZXRFaXRoZXIgPSAoZGF0YSwgZGVyZWYsIGtleSwgdmFsKSA9PiB7XG5cdChkZXJlZiB8fCBkYXRhKVtrZXldID0gdmFsO1xuXHRyZXR1cm4gZGF0YTtcbn07XG5cbi8vIFNldCBhIG5lc3RlZCB2YWx1ZSBpbiBkYXRhIChub3QgdW5saWtlIGltbXV0YWJsZWpzLCBidXQgYSBjbG9uZSBvZiBkYXRhIGlzIGV4cGVjdGVkIGZvciBwcm9wZXIgaW1tdXRhYmlsaXR5KVxuY29uc3QgX3NldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA9IG51bGwpID0+XG5cdHBhdGgubGVuZ3RoID4gMSA/XG5cdFx0X3NldEluKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA/IGRlcmVmW3BhdGguc2hpZnQoKV0gOiBkYXRhW3BhdGguc2hpZnQoKV0pIDpcblx0XHRzZXRFaXRoZXIoZGF0YSwgZGVyZWYsIHBhdGhbMF0sIHZhbHVlKTtcblxuY29uc3Qgc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEpID0+XG5cdF9zZXRJbihjbG9uZShwYXRoKSwgdmFsdWUsIGNsb25lKGRhdGEpKTtcblxuZXhwb3J0IGRlZmF1bHQgc2V0SW47Il19
