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

if ("production" !== 'production') {
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

},{}],25:[function(require,module,exports){
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
  if ("production" !== 'production') {
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

},{}],27:[function(require,module,exports){
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

if ("production" !== 'production') {
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
},{"../utils/storeShape":39,"../utils/warning":40,"react":"react"}],36:[function(require,module,exports){
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
      if ("production" !== 'production') {
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

        if ("production" !== 'production') {
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

        if ("production" !== 'production') {
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

        if ("production" !== 'production') {
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

        if ("production" !== 'production') {
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

    if ("production" !== 'production') {
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
},{"../utils/shallowEqual":38,"../utils/storeShape":39,"../utils/warning":40,"../utils/wrapActionCreators":41,"hoist-non-react-statics":25,"invariant":26,"lodash/isPlainObject":32,"react":"react"}],37:[function(require,module,exports){
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
    "production" !== 'production' ? (0, _routerWarning2.default)(false, 'the `History` mixin is deprecated, please access `context.router` with your own `contextTypes`. http://tiny.cc/router-historymixin') : void 0;
    this.history = this.context.history;
  }
};

exports.default = History;
module.exports = exports['default'];
},{"./InternalPropTypes":47,"./routerWarning":76}],44:[function(require,module,exports){
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
        "production" !== 'production' ? (0, _routerWarning2.default)(false, 'An <IndexRedirect> does not make sense at the root of your route config') : void 0;
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
    !false ? "production" !== 'production' ? (0, _invariant2.default)(false, '<IndexRedirect> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = IndexRedirect;
module.exports = exports['default'];
},{"./InternalPropTypes":47,"./Redirect":52,"./routerWarning":76,"invariant":26,"react":"react"}],46:[function(require,module,exports){
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
        "production" !== 'production' ? (0, _routerWarning2.default)(false, 'An <IndexRoute> does not make sense at the root of your route config') : void 0;
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
    !false ? "production" !== 'production' ? (0, _invariant2.default)(false, '<IndexRoute> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = IndexRoute;
module.exports = exports['default'];
},{"./InternalPropTypes":47,"./RouteUtils":55,"./routerWarning":76,"invariant":26,"react":"react"}],47:[function(require,module,exports){
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
    "production" !== 'production' ? (0, _routerWarning2.default)(false, 'the `Lifecycle` mixin is deprecated, please use `context.router.setRouteLeaveHook(route, hook)`. http://tiny.cc/router-lifecyclemixin') : void 0;
    !this.routerWillLeave ? "production" !== 'production' ? (0, _invariant2.default)(false, 'The Lifecycle mixin requires you to define a routerWillLeave method') : (0, _invariant2.default)(false) : void 0;

    var route = this.props.route || this.context.route;

    !route ? "production" !== 'production' ? (0, _invariant2.default)(false, 'The Lifecycle mixin must be used on either a) a <Route component> or ' + 'b) a descendant of a <Route component> that uses the RouteContext mixin') : (0, _invariant2.default)(false) : void 0;

    this._unlistenBeforeLeavingRoute = this.context.history.listenBeforeLeavingRoute(route, this.routerWillLeave);
  },
  componentWillUnmount: function componentWillUnmount() {
    if (this._unlistenBeforeLeavingRoute) this._unlistenBeforeLeavingRoute();
  }
};

exports.default = Lifecycle;
module.exports = exports['default'];
},{"./routerWarning":76,"invariant":26,"react":"react"}],49:[function(require,module,exports){
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

    !this.context.router ? "production" !== 'production' ? (0, _invariant2.default)(false, '<Link>s rendered outside of a router context cannot navigate.') : (0, _invariant2.default)(false) : void 0;

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

    "production" !== 'production' ? (0, _routerWarning2.default)(!(query || hash || state), 'the `query`, `hash`, and `state` props on `<Link>` are deprecated, use `<Link to={{ pathname, query, hash, state }}/>. http://tiny.cc/router-isActivedeprecated') : void 0;

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
},{"./PropTypes":51,"./routerWarning":76,"invariant":26,"react":"react"}],50:[function(require,module,exports){
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

      !(paramValue != null || parenCount > 0) ? "production" !== 'production' ? (0, _invariant2.default)(false, 'Missing splat #%s for path "%s"', splatIndex, pattern) : (0, _invariant2.default)(false) : void 0;

      if (paramValue != null) pathname += encodeURI(paramValue);
    } else if (token === '(') {
      parenCount += 1;
    } else if (token === ')') {
      parenCount -= 1;
    } else if (token.charAt(0) === ':') {
      paramName = token.substring(1);
      paramValue = params[paramName];

      !(paramValue != null || parenCount > 0) ? "production" !== 'production' ? (0, _invariant2.default)(false, 'Missing "%s" parameter for path "%s"', paramName, pattern) : (0, _invariant2.default)(false) : void 0;

      if (paramValue != null) pathname += encodeURIComponent(paramValue);
    } else {
      pathname += token;
    }
  }

  return pathname.replace(/\/+/g, '/');
}
},{"invariant":26}],51:[function(require,module,exports){
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

if ("production" !== 'production') {
  (function () {
    var deprecatePropType = function deprecatePropType(propType, message) {
      return function () {
        "production" !== 'production' ? (0, _routerWarning2.default)(false, message) : void 0;
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

if ("production" !== 'production') {
  defaultExport = (0, _deprecateObjectProperties2.default)(defaultExport, 'The default export from `react-router/lib/PropTypes` is deprecated. Please use the named exports instead.');
}

exports.default = defaultExport;
},{"./InternalPropTypes":47,"./deprecateObjectProperties":67,"./routerWarning":76,"react":"react"}],52:[function(require,module,exports){
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
    !false ? "production" !== 'production' ? (0, _invariant2.default)(false, '<Redirect> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = Redirect;
module.exports = exports['default'];
},{"./InternalPropTypes":47,"./PatternUtils":50,"./RouteUtils":55,"invariant":26,"react":"react"}],53:[function(require,module,exports){
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
    !false ? "production" !== 'production' ? (0, _invariant2.default)(false, '<Route> elements are for router configuration only and should not be rendered') : (0, _invariant2.default)(false) : void 0;
  }
});

exports.default = Route;
module.exports = exports['default'];
},{"./InternalPropTypes":47,"./RouteUtils":55,"invariant":26,"react":"react"}],54:[function(require,module,exports){
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
    "production" !== 'production' ? (0, _routerWarning2.default)(false, 'The `RouteContext` mixin is deprecated. You can provide `this.props.route` on context with your own `contextTypes`. http://tiny.cc/router-routecontextmixin') : void 0;
  }
};

exports.default = RouteContext;
module.exports = exports['default'];
},{"./routerWarning":76,"react":"react"}],55:[function(require,module,exports){
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

    "production" !== 'production' ? (0, _routerWarning2.default)(!(parseQueryString || stringifyQuery), '`parseQueryString` and `stringifyQuery` are deprecated. Please create a custom history. http://tiny.cc/router-customquerystring') : void 0;

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


    !!isUnsupportedHistory(history) ? "production" !== 'production' ? (0, _invariant2.default)(false, 'You have provided a history object created with history v3.x. ' + 'This version of React Router is not compatible with v3 history ' + 'objects. Please use history v2.x instead.') : (0, _invariant2.default)(false) : void 0;

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
      "production" !== 'production' ? (0, _routerWarning2.default)(false, 'It appears you have provided a deprecated history object to `<Router/>`, please use a history provided by ' + 'React Router with `import { browserHistory } from \'react-router\'` or `import { hashHistory } from \'react-router\'`. ' + 'If you are using a custom history please create it with `useRouterHistory`, see http://tiny.cc/router-usinghistory for details.') : void 0;
      createHistory = function createHistory() {
        return history;
      };
    } else {
      "production" !== 'production' ? (0, _routerWarning2.default)(false, '`Router` no longer defaults the history prop to hash history. Please use the `hashHistory` singleton instead. http://tiny.cc/router-defaulthistory') : void 0;
      createHistory = _createHashHistory2.default;
    }

    return (0, _useQueries2.default)(createHistory)({ parseQueryString: parseQueryString, stringifyQuery: stringifyQuery });
  },


  /* istanbul ignore next: sanity check */
  componentWillReceiveProps: function componentWillReceiveProps(nextProps) {
    "production" !== 'production' ? (0, _routerWarning2.default)(nextProps.history === this.props.history, 'You cannot change <Router history>; it will be ignored') : void 0;

    "production" !== 'production' ? (0, _routerWarning2.default)((nextProps.routes || nextProps.children) === (this.props.routes || this.props.children), 'You cannot change <Router routes>; it will be ignored') : void 0;
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
},{"./InternalPropTypes":47,"./RouteUtils":55,"./RouterContext":57,"./RouterUtils":58,"./createTransitionManager":66,"./routerWarning":76,"history/lib/createHashHistory":16,"history/lib/useQueries":23,"invariant":26,"react":"react"}],57:[function(require,module,exports){
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
      "production" !== 'production' ? (0, _routerWarning2.default)(false, '`<RouterContext>` expects a `router` rather than a `history`') : void 0;

      router = _extends({}, history, {
        setRouteLeaveHook: history.listenBeforeLeavingRoute
      });
      delete router.listenBeforeLeavingRoute;
    }

    if ("production" !== 'production') {
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

    !(element === null || element === false || _react2.default.isValidElement(element)) ? "production" !== 'production' ? (0, _invariant2.default)(false, 'The root route must render a single element') : (0, _invariant2.default)(false) : void 0;

    return element;
  }
});

exports.default = RouterContext;
module.exports = exports['default'];
},{"./RouteUtils":55,"./deprecateObjectProperties":67,"./getRouteParams":69,"./routerWarning":76,"invariant":26,"react":"react"}],58:[function(require,module,exports){
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

  if ("production" !== 'production') {
    history = (0, _deprecateObjectProperties2.default)(history, '`props.history` and `context.history` are deprecated. Please use `context.router`. http://tiny.cc/router-contextchanges');
  }

  return history;
}
},{"./deprecateObjectProperties":67}],59:[function(require,module,exports){
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
    "production" !== 'production' ? (0, _routerWarning2.default)(false, '`RoutingContext` has been renamed to `RouterContext`. Please use `import { RouterContext } from \'react-router\'`. http://tiny.cc/router-routercontext') : void 0;
  },
  render: function render() {
    return _react2.default.createElement(_RouterContext2.default, this.props);
  }
});

exports.default = RoutingContext;
module.exports = exports['default'];
},{"./RouterContext":57,"./routerWarning":76,"react":"react"}],60:[function(require,module,exports){
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
      "production" !== 'production' ? (0, _routerWarning2.default)(false, '`replaceState(state, pathname, query) is deprecated; use `replace(location)` with a location descriptor instead. http://tiny.cc/router-isActivedeprecated') : void 0;
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
},{"./AsyncUtils":42,"./routerWarning":76}],61:[function(require,module,exports){
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
      "production" !== 'production' ? (0, _routerWarning2.default)(false, '`isActive(pathname, query, indexOnly) is deprecated; use `isActive(location, indexOnly)` with a location descriptor instead. http://tiny.cc/router-isActivedeprecated') : void 0;
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
        "production" !== 'production' ? (0, _routerWarning2.default)(false, 'adding multiple leave hooks for the same route is deprecated; manage multiple confirmations in your own code instead') : void 0;

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
            "production" !== 'production' ? (0, _routerWarning2.default)(false, 'Location "%s" did not match any routes', location.pathname + location.search + location.hash) : void 0;
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
},{"./TransitionUtils":60,"./computeChangedRoutes":63,"./getComponents":68,"./isActive":72,"./matchRoutes":75,"./routerWarning":76,"history/lib/Actions":8}],67:[function(require,module,exports){
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

if ("production" !== 'production') {
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
            "production" !== 'production' ? (0, _routerWarning2.default)(false, message) : void 0;
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
            "production" !== 'production' ? (0, _routerWarning2.default)(false, message) : void 0;
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
},{"./routerWarning":76}],68:[function(require,module,exports){
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
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = makeStateWithLocation;

var _deprecateObjectProperties = require('./deprecateObjectProperties');

var _routerWarning = require('./routerWarning');

var _routerWarning2 = _interopRequireDefault(_routerWarning);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function makeStateWithLocation(state, location) {
  if ("production" !== 'production' && _deprecateObjectProperties.canUseMembrane) {
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
          "production" !== 'production' ? (0, _routerWarning2.default)(false, 'Accessing location properties directly from the first argument to `getComponent`, `getComponents`, `getChildRoutes`, and `getIndexRoute` is deprecated. That argument is now the router state (`nextState` or `partialNextState`) rather than the location. To access the location, use `nextState.location` or `partialNextState.location`.') : void 0;
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
},{"./deprecateObjectProperties":67,"./routerWarning":76}],74:[function(require,module,exports){
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

  !(history || location) ? "production" !== 'production' ? (0, _invariant2.default)(false, 'match needs a history or a location') : (0, _invariant2.default)(false) : void 0;

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
},{"./RouteUtils":55,"./RouterUtils":58,"./createMemoryHistory":64,"./createTransitionManager":66,"invariant":26}],75:[function(require,module,exports){
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

              "production" !== 'production' ? (0, _routerWarning2.default)(indexRoute.every(function (route) {
                return !route.path;
              }), 'Index routes should not have paths') : void 0;
              (_match$routes = match.routes).push.apply(_match$routes, indexRoute);
            } else if (indexRoute) {
              "production" !== 'production' ? (0, _routerWarning2.default)(!indexRoute.path, 'Index routes should not have paths') : void 0;
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
},{"./AsyncUtils":42,"./PatternUtils":50,"./RouteUtils":55,"./makeStateWithLocation":73,"./routerWarning":76}],76:[function(require,module,exports){
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
  "production" !== 'production' ? (0, _routerWarning2.default)(false, '`useRoutes` is deprecated. Please use `createTransitionManager` instead.') : void 0;

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
},{"./createTransitionManager":66,"./routerWarning":76,"history/lib/useQueries":23}],79:[function(require,module,exports){
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

    if ("production" !== 'production') {
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
},{"./createStore":85,"./utils/warning":87,"lodash/isPlainObject":32}],84:[function(require,module,exports){
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

if ("production" !== 'production' && typeof isCrushed.name === 'string' && isCrushed.name !== 'isCrushed') {
  (0, _warning2["default"])('You are currently using minified code outside of NODE_ENV === \'production\'. ' + 'This means that you are running a slower development build of Redux. ' + 'You can use loose-envify (https://github.com/zertosh/loose-envify) for browserify ' + 'or DefinePlugin for webpack (http://stackoverflow.com/questions/30030031) ' + 'to ensure you have the correct code for your production build.');
}

exports.createStore = _createStore2["default"];
exports.combineReducers = _combineReducers2["default"];
exports.bindActionCreators = _bindActionCreators2["default"];
exports.applyMiddleware = _applyMiddleware2["default"];
exports.compose = _compose2["default"];
},{"./applyMiddleware":81,"./bindActionCreators":82,"./combineReducers":83,"./compose":84,"./createStore":85,"./utils/warning":87}],87:[function(require,module,exports){
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
arguments[4][24][0].apply(exports,arguments)
},{"dup":24}],93:[function(require,module,exports){
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

},{"global/window":7,"is-function":27,"parse-headers":33,"xtend":94}],94:[function(require,module,exports){
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

},{}],95:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var isBasicProperty = function isBasicProperty(predicateObjectMap) {
  return ["text", "select", "multiselect", "datable", "names"].indexOf(predicateObjectMap.propertyType) > -1;
};

var columnMapIsComplete = function columnMapIsComplete(predicateObjectMap) {
  return predicateObjectMap.objectMap && typeof predicateObjectMap.objectMap.column !== "undefined" && predicateObjectMap.objectMap.column !== null;
};

var joinConditionMapIsComplete = function joinConditionMapIsComplete(predicateObjectMap) {
  return predicateObjectMap.objectMap && predicateObjectMap.objectMap.parentTriplesMap && predicateObjectMap.objectMap.joinCondition && typeof predicateObjectMap.objectMap.joinCondition.parent !== "undefined" && typeof predicateObjectMap.objectMap.joinCondition.child !== "undefined";
};

var propertyMappingIsComplete = function propertyMappingIsComplete(predicateObjectMap) {
  if (typeof predicateObjectMap === "undefined") {
    return false;
  }

  if (isBasicProperty(predicateObjectMap)) {
    return columnMapIsComplete(predicateObjectMap);
  }

  if (predicateObjectMap.propertyType === "relation") {
    return joinConditionMapIsComplete(predicateObjectMap);
  }

  return false;
};

var getColumnValue = function getColumnValue(predicateObjectMap) {
  if (!predicateObjectMap) {
    return null;
  }

  if (isBasicProperty(predicateObjectMap)) {
    return predicateObjectMap.objectMap && predicateObjectMap.objectMap.column ? predicateObjectMap.objectMap.column : null;
  }

  if (predicateObjectMap.propertyType === "relation") {
    return predicateObjectMap.objectMap && predicateObjectMap.objectMap.joinCondition && predicateObjectMap.objectMap.joinCondition.child ? predicateObjectMap.objectMap.joinCondition.child : null;
  }

  return null;
};

exports.propertyMappingIsComplete = propertyMappingIsComplete;
exports.isBasicProperty = isBasicProperty;
exports.getColumnValue = getColumnValue;

},{}],96:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = actionsMaker;

var _upload = require("./actions/upload");

var _fetchBulkuploadedMetadata = require("./actions/fetch-bulkuploaded-metadata");

var _selectCollection = require("./actions/select-collection");

var _predicateObjectMappings = require("./actions/predicate-object-mappings");

var _publishMappings = require("./actions/publish-mappings");

function actionsMaker(navigateTo, dispatch) {
  return {
    onUploadFileSelect: (0, _upload.onUploadFileSelect)(navigateTo, dispatch),

    // loading import data
    onSelectCollection: function onSelectCollection(collection) {
      return dispatch((0, _selectCollection.selectCollection)(collection));
    },
    onLoadMoreClick: function onLoadMoreClick(nextUrl, collection) {
      return dispatch((0, _selectCollection.selectCollection)(collection, nextUrl));
    },
    onFetchBulkUploadedMetadata: function onFetchBulkUploadedMetadata(vreId, mappingsFromUrl) {
      return dispatch((0, _fetchBulkuploadedMetadata.fetchBulkUploadedMetadata)(vreId, mappingsFromUrl));
    },

    // Closing informative messages
    onCloseMessage: function onCloseMessage(messageId) {
      return dispatch({ type: "TOGGLE_MESSAGE", messageId: messageId });
    },

    // Mapping collections archetypes
    onMapCollectionArchetype: function onMapCollectionArchetype(collection, value) {
      return dispatch({ type: "MAP_COLLECTION_ARCHETYPE", collection: collection, value: value });
    },

    // Connecting data
    onIgnoreColumnToggle: function onIgnoreColumnToggle(collection, variableName) {
      return dispatch({ type: "TOGGLE_IGNORED_COLUMN", collection: collection, variableName: variableName });
    },

    onAddPredicateObjectMap: function onAddPredicateObjectMap(predicateName, objectName, propertyType) {
      return dispatch((0, _predicateObjectMappings.addPredicateObjectMap)(predicateName, objectName, propertyType));
    },

    onRemovePredicateObjectMap: function onRemovePredicateObjectMap(predicateName, objectName) {
      return dispatch((0, _predicateObjectMappings.removePredicateObjectMap)(predicateName, objectName));
    },

    onAddCustomProperty: function onAddCustomProperty(name, type) {
      return dispatch((0, _predicateObjectMappings.addCustomProperty)(name, type));
    },

    onRemoveCustomProperty: function onRemoveCustomProperty(index) {
      return dispatch((0, _predicateObjectMappings.removeCustomProperty)(index));
    },

    onPublishData: function onPublishData() {
      return dispatch((0, _publishMappings.publishMappings)(navigateTo));
    }
  };
}

},{"./actions/fetch-bulkuploaded-metadata":97,"./actions/predicate-object-mappings":99,"./actions/publish-mappings":100,"./actions/select-collection":101,"./actions/upload":102}],97:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.fetchBulkUploadedMetadata = undefined;

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _selectCollection = require("./select-collection");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var fetchBulkUploadedMetadata = function fetchBulkUploadedMetadata(vreId, mappingsFromUrl) {
  return function (dispatch, getState) {
    var location = "" + "/v2.1/bulk-upload/" + vreId;
    _xhr2.default.get(location, { headers: { "Authorization": getState().userdata.userId } }, function (err, resp, body) {
      var responseData = JSON.parse(body);
      dispatch({ type: "FINISH_UPLOAD", data: responseData });

      if (responseData.collections && responseData.collections.length) {
        dispatch((0, _selectCollection.selectCollection)(responseData.collections[0].name));
      }

      if (mappingsFromUrl) {
        dispatch({ type: "MAP_COLLECTION_ARCHETYPES", data: mappingsFromUrl });
      }
    });
  };
};

exports.fetchBulkUploadedMetadata = fetchBulkUploadedMetadata;

},{"./select-collection":101,"xhr":93}],98:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.fetchMyVres = undefined;

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var fetchMyVres = function fetchMyVres(token, callback) {
  return function (dispatch) {
    (0, _xhr2.default)("" + "/v2.1/system/users/me/vres", {
      headers: {
        "Authorization": token
      }
    }, function (err, resp, body) {
      var vreData = JSON.parse(body);
      dispatch({ type: "LOGIN", data: token, vreData: vreData });
      callback(vreData);
    });
  };
};

exports.fetchMyVres = fetchMyVres;

},{"xhr":93}],99:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.removeCustomProperty = exports.addCustomProperty = exports.removePredicateObjectMap = exports.addPredicateObjectMap = undefined;

var _propertyMappings = require("../accessors/property-mappings");

var addPredicateObjectMap = function addPredicateObjectMap(predicate, object, propertyType) {
  return function (dispatch, getState) {
    var _getState = getState();

    var subjectCollection = _getState.activeCollection.name;


    dispatch({
      type: "SET_PREDICATE_OBJECT_MAPPING",
      subjectCollection: subjectCollection,
      predicate: predicate,
      object: object,
      propertyType: propertyType
    });
  };
};

var removePredicateObjectMap = function removePredicateObjectMap(predicate, object) {
  return function (dispatch, getState) {
    var _getState2 = getState();

    var subjectCollection = _getState2.activeCollection.name;


    dispatch({
      type: "REMOVE_PREDICATE_OBJECT_MAPPING",
      subjectCollection: subjectCollection,
      predicate: predicate,
      object: object
    });
  };
};

var addCustomProperty = function addCustomProperty(name, type) {
  return function (dispatch, getState) {
    var _getState3 = getState();

    var collectionName = _getState3.activeCollection.name;


    dispatch({
      type: "ADD_CUSTOM_PROPERTY",
      collection: collectionName,
      propertyName: name,
      propertyType: type
    });
  };
};

var removeCustomProperty = function removeCustomProperty(index) {
  return function (dispatch, getState) {
    var _getState4 = getState();

    var collectionName = _getState4.activeCollection.name;
    var allPredicateObjectMappings = _getState4.predicateObjectMappings;
    var customProperties = _getState4.customProperties;


    var predicateObjectMappings = allPredicateObjectMappings[collectionName] || [];
    var customProperty = customProperties[collectionName][index];

    var predicateObjectMapping = predicateObjectMappings.find(function (pom) {
      return pom.predicate === customProperty.propertyName;
    });

    if (predicateObjectMapping) {
      dispatch({
        type: "REMOVE_PREDICATE_OBJECT_MAPPING",
        subjectCollection: collectionName,
        predicate: customProperty.propertyName,
        object: (0, _propertyMappings.getColumnValue)(predicateObjectMapping)
      });
    }
    dispatch({
      type: "REMOVE_CUSTOM_PROPERTY",
      collection: collectionName,
      index: index
    });
  };
};

exports.addPredicateObjectMap = addPredicateObjectMap;
exports.removePredicateObjectMap = removePredicateObjectMap;
exports.addCustomProperty = addCustomProperty;
exports.removeCustomProperty = removeCustomProperty;

},{"../accessors/property-mappings":95}],100:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.publishMappings = undefined;

var _generateRmlMapping = require("../util/generate-rml-mapping");

var _generateRmlMapping2 = _interopRequireDefault(_generateRmlMapping);

var _fetchMyVres = require("./fetch-my-vres");

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _selectCollection = require("./select-collection");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var publishMappings = function publishMappings(navigateTo) {
  return function (dispatch, getState) {
    var _getState = getState();

    var _getState$importData = _getState.importData;
    var vre = _getState$importData.vre;
    var executeMappingUrl = _getState$importData.executeMappingUrl;
    var collections = _getState.mappings.collections;
    var userId = _getState.userdata.userId;
    var predicateObjectMappings = _getState.predicateObjectMappings;
    var activeCollection = _getState.activeCollection;


    var jsonLd = (0, _generateRmlMapping2.default)(vre, collections, predicateObjectMappings);

    console.log(JSON.stringify(jsonLd, null, 2));

    dispatch({ type: "PUBLISH_START" });
    (0, _xhr2.default)({
      url: executeMappingUrl,
      method: "POST",
      headers: {
        "Authorization": userId,
        "Content-type": "application/ld+json"
      },
      data: JSON.stringify(jsonLd)
    }, function (err, resp, body) {
      if (err) {
        dispatch({ type: "PUBLISH_HAD_ERROR" });
      } else {
        var _JSON$parse = JSON.parse(body);

        var success = _JSON$parse.success;

        if (success) {
          dispatch({ type: "PUBLISH_SUCCEEDED" });
          dispatch((0, _fetchMyVres.fetchMyVres)(userId, function () {
            return navigateTo("root");
          }));
        } else {
          dispatch({ type: "PUBLISH_HAD_ERROR" });
          dispatch((0, _selectCollection.selectCollection)(activeCollection.name, null, true));
        }
      }
      dispatch({ type: "PUBLISH_FINISHED" });
    });

    /*  const req = new XMLHttpRequest();
      req.open('POST', executeMappingUrl, true);
      req.setRequestHeader("Authorization", userId);
      req.setRequestHeader("Content-type", "application/ld+json");
    
      dispatch({type: "PUBLISH_START"});
    
      let pos = 0;
      req.onreadystatechange = function handleData() {
        if (req.readyState != null && (req.readyState < 3 || req.status != 200)) {
          return
        }
        let newPart = req.responseText.substr(pos);
        pos = req.responseText.length;
        newPart.split("\n").forEach(line => {
          dispatch({type: "PUBLISH_STATUS_UPDATE", data: line});
        });
      };
    
      req.onload = function () {
        if (req.status > 400) {
          dispatch({type: "PUBLISH_HAD_ERROR"})
        } else {
          dispatch(function (dispatch, getState) {
            var state = getState();
            if (state.importData.publishErrorCount === 0) {
              dispatch({type: "PUBLISH_SUCCEEDED"});
              dispatch(fetchMyVres(userId, () => navigateTo("root")));
            } else {
              dispatch({type: "PUBLISH_HAD_ERROR"});
            }
          });
        }
        dispatch({type: "PUBLISH_FINISHED"});
      };
      req.send(JSON.stringify(jsonLd));*/
  };
};

exports.publishMappings = publishMappings;

},{"../util/generate-rml-mapping":149,"./fetch-my-vres":98,"./select-collection":101,"xhr":93}],101:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.selectCollection = undefined;

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var selectCollection = function selectCollection(collection) {
  var altUrl = arguments.length <= 1 || arguments[1] === undefined ? null : arguments[1];
  var onlyErrors = arguments.length <= 2 || arguments[2] === undefined ? false : arguments[2];
  return function (dispatch, getState) {
    var _getState = getState();

    var collections = _getState.importData.collections;
    var userId = _getState.userdata.userId;

    var selectedCollection = collections.find(function (col) {
      return col.name === collection;
    });

    if (userId && collections && selectedCollection && selectedCollection.dataUrl) {
      dispatch({ type: "ACTIVE_COLLECTION_PENDING" });
      _xhr2.default.get((altUrl || selectedCollection.dataUrl) + (onlyErrors ? "?onlyErrors=true" : ""), {
        headers: { "Authorization": userId }
      }, function (err, resp, body) {
        if (err) {
          dispatch({ type: "ACTIVE_COLLECTION_FETCH_ERROR", collection: collection, error: err });
        } else {
          try {
            dispatch({ type: "RECEIVE_ACTIVE_COLLECTION", collection: collection, data: JSON.parse(body) });
          } catch (e) {
            dispatch({ type: "ACTIVE_COLLECTION_FETCH_ERROR", collection: collection, error: e });
          }
        }
        dispatch({ type: "ACTIVE_COLLECTION_DONE" });
      });
    }
  };
};

exports.selectCollection = selectCollection;

},{"xhr":93}],102:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.onUploadFileSelect = undefined;

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _selectCollection = require("./select-collection");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var onUploadFileSelect = function onUploadFileSelect(navigateTo, dispatch) {
  return function (files) {
    var isReupload = arguments.length <= 1 || arguments[1] === undefined ? false : arguments[1];

    var file = files[0];
    var formData = new FormData();
    formData.append("file", file);
    dispatch({ type: "START_UPLOAD" });
    dispatch(function (dispatch, getState) {
      var state = getState();
      var req = new XMLHttpRequest();
      req.open('POST', "" + "/v2.1/bulk-upload", true);
      req.setRequestHeader("Authorization", state.userdata.userId);
      var pos = 0;
      req.onreadystatechange = function handleData() {
        if (req.readyState != null && (req.readyState < 3 || req.status != 200)) {
          return;
        }
        var newPart = req.responseText.substr(pos);
        pos = req.responseText.length;
        newPart.split("\n").forEach(function (line, idx) {
          if (idx % 21 === 0) {
            dispatch({ type: "UPLOAD_STATUS_UPDATE", data: line });
          }
        });
      };
      req.onload = function () {
        var location = req.getResponseHeader("location");
        _xhr2.default.get(location, { headers: { "Authorization": state.userdata.userId } }, function (err, resp, body) {
          var responseData = JSON.parse(body);
          dispatch({ type: "FINISH_UPLOAD", data: responseData, uploadedFileName: file.name });
          if (isReupload) {} else {
            navigateTo("mapArchetypes", [responseData.vre]);
          }
          if (responseData.collections && responseData.collections.length) {
            dispatch((0, _selectCollection.selectCollection)(responseData.collections[0].name));
          }
        });
      };
      req.send(formData);
    });
  };
};

exports.onUploadFileSelect = onUploadFileSelect;

},{"./select-collection":101,"xhr":93}],103:[function(require,module,exports){
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
    key: "onEnter",
    value: function onEnter(newName, newType) {
      if (newType !== null) {
        this.setState({ newName: null, newType: null });
        this.props.onAddCustomProperty(newName, newType);
      }
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var _state = this.state;
      var newName = _state.newName;
      var newType = _state.newType;
      var onAddCustomProperty = this.props.onAddCustomProperty;


      return _react2.default.createElement(
        "div",
        { className: "row small-margin" },
        _react2.default.createElement(
          "div",
          { className: "col-sm-2 pad-6-12" },
          _react2.default.createElement(
            "strong",
            null,
            "Add a new property"
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-6" },
          _react2.default.createElement(
            "span",
            null,
            _react2.default.createElement(
              _selectField2.default,
              {
                value: newType,
                onChange: function onChange(value) {
                  return _this2.setState({ newType: value, newName: newName });
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
                { value: "datable" },
                "Datable"
              )
            )
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-2" },
          _react2.default.createElement("input", { className: "form-control",
            onChange: function onChange(ev) {
              return _this2.setState({ newName: ev.target.value });
            },
            onKeyPress: function onKeyPress(ev) {
              return ev.key === "Enter" ? _this2.onEnter(newName, newType) : false;
            },
            placeholder: "Property name",
            value: newName })
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

exports.default = AddProperty;

},{"../fields/select-field":117,"react":"react"}],104:[function(require,module,exports){
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

var AddRelation = function (_React$Component) {
  _inherits(AddRelation, _React$Component);

  function AddRelation(props) {
    _classCallCheck(this, AddRelation);

    var _this = _possibleConstructorReturn(this, Object.getPrototypeOf(AddRelation).call(this, props));

    _this.state = {
      newName: ""
    };
    return _this;
  }

  _createClass(AddRelation, [{
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
          { className: "col-sm-2 pad-6-12" },
          _react2.default.createElement(
            "strong",
            null,
            "Add a relation"
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-8" },
          _react2.default.createElement(
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
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-2" },
          _react2.default.createElement(
            "button",
            { className: "pull-right btn btn-default", disabled: !newName,
              onClick: function onClick() {
                _this2.setState({ newName: null });
                onAddCustomProperty(newName, "relation");
              } },
            "Add relation"
          )
        )
      );
    }
  }]);

  return AddRelation;
}(_react2.default.Component);

exports.default = AddRelation;

},{"../fields/select-field":117,"react":"react"}],105:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _propertyForm = require("./property-form");

var _propertyForm2 = _interopRequireDefault(_propertyForm);

var _addProperty = require("./add-property");

var _addProperty2 = _interopRequireDefault(_addProperty);

var _addRelation = require("./add-relation");

var _addRelation2 = _interopRequireDefault(_addRelation);

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
      var onAddPredicateObjectMap = _props.onAddPredicateObjectMap;
      var onRemovePredicateObjectMap = _props.onRemovePredicateObjectMap;
      var onAddCustomProperty = _props.onAddCustomProperty;
      var onRemoveCustomProperty = _props.onRemoveCustomProperty;
      var _props2 = this.props;
      var archetypeFields = _props2.archetypeFields;
      var availableArchetypes = _props2.availableArchetypes;
      var columns = _props2.columns;
      var ignoredColumns = _props2.ignoredColumns;
      var availableCollectionColumnsPerArchetype = _props2.availableCollectionColumnsPerArchetype;
      var targetableVres = _props2.targetableVres;
      var _props3 = this.props;
      var predicateObjectMappings = _props3.predicateObjectMappings;
      var customProperties = _props3.customProperties;


      var archeTypePropFields = archetypeFields.filter(function (af) {
        return af.type !== "relation";
      });

      var propertyForms = archeTypePropFields.map(function (af, i) {
        return _react2.default.createElement(_propertyForm2.default, { key: i, name: af.name, type: af.type, custom: false,
          columns: columns, ignoredColumns: ignoredColumns,
          predicateObjectMap: predicateObjectMappings.find(function (pom) {
            return pom.predicate === af.name;
          }),
          predicateObjectMappings: predicateObjectMappings,
          onAddPredicateObjectMap: onAddPredicateObjectMap,
          onRemovePredicateObjectMap: onRemovePredicateObjectMap });
      });

      var customPropertyForms = customProperties.map(function (customProp, i) {
        return _react2.default.createElement(_propertyForm2.default, { key: i, name: customProp.propertyName, type: customProp.propertyType, custom: true, customIndex: i,
          columns: columns, ignoredColumns: ignoredColumns,
          predicateObjectMap: predicateObjectMappings.find(function (pom) {
            return pom.predicate === customProp.propertyName;
          }),
          predicateObjectMappings: predicateObjectMappings,
          onAddPredicateObjectMap: onAddPredicateObjectMap,
          onRemovePredicateObjectMap: onRemovePredicateObjectMap,
          onRemoveCustomProperty: onRemoveCustomProperty,
          availableCollectionColumnsPerArchetype: availableCollectionColumnsPerArchetype,
          relationTypeInfo: archetypeFields.find(function (af) {
            return af.name === customProp.propertyName;
          }),
          targetableVres: targetableVres
        });
      });
      return _react2.default.createElement(
        "div",
        { className: "container basic-margin" },
        propertyForms,
        customPropertyForms,
        _react2.default.createElement(_addProperty2.default, { onAddCustomProperty: onAddCustomProperty }),
        _react2.default.createElement(_addRelation2.default, {
          archetypeFields: archetypeFields,
          availableArchetypes: availableArchetypes,
          onAddCustomProperty: onAddCustomProperty })
      );
    }
  }]);

  return CollectionForm;
}(_react2.default.Component);

exports.default = CollectionForm;

},{"./add-property":103,"./add-relation":104,"./property-form":108,"react":"react"}],106:[function(require,module,exports){
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
      var selectedColumn = _props.selectedColumn;
      var onColumnSelect = _props.onColumnSelect;
      var onClearColumn = _props.onClearColumn;
      var placeholder = _props.placeholder;
      var ignoredColumns = _props.ignoredColumns;
      var valuePrefix = _props.valuePrefix;


      return _react2.default.createElement(
        _selectField2.default,
        { value: selectedColumn, style: { display: "inline-block" },
          valuePrefix: valuePrefix,
          onChange: function onChange(column) {
            return onColumnSelect(column);
          },
          onClear: function onClear() {
            return onClearColumn(selectedColumn);
          } },
        _react2.default.createElement(
          "span",
          { type: "placeholder", className: "from-excel" },
          _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
          " ",
          placeholder || "Select an excel column"
        ),
        columns.filter(function (column) {
          return ignoredColumns.indexOf(column) < 0;
        }).map(function (column) {
          return _react2.default.createElement(
            "span",
            { key: column, value: column, className: "from-excel" },
            _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
            " ",
            valuePrefix && column === selectedColumn ? valuePrefix : "",
            column
          );
        })
      );
    }
  }]);

  return ColumnSelect;
}(_react2.default.Component);

exports.default = ColumnSelect;

},{"../fields/select-field":117,"react":"react"}],107:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _columnSelect = require("./column-select");

var _columnSelect2 = _interopRequireDefault(_columnSelect);

var _camel2label = require("../../util/camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var _propertyMappings = require("../../accessors/property-mappings");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var getObjectForPredicate = function getObjectForPredicate(predicateObjectMappings, predicate) {
  return predicateObjectMappings.filter(function (pom) {
    return pom.predicate === predicate;
  }).map(function (pom) {
    return (0, _propertyMappings.getColumnValue)(pom);
  })[0];
};

var NamesForm = function (_React$Component) {
  _inherits(NamesForm, _React$Component);

  function NamesForm() {
    _classCallCheck(this, NamesForm);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(NamesForm).apply(this, arguments));
  }

  _createClass(NamesForm, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var columns = _props.columns;
      var predicateObjectMappings = _props.predicateObjectMappings;
      var _onColumnSelect = _props.onColumnSelect;
      var _onClearColumn = _props.onClearColumn;
      var ignoredColumns = _props.ignoredColumns;


      var formRows = ["forename", "surname", "nameLink", "genName", "roleName"].map(function (predicate) {
        return _react2.default.createElement(
          "div",
          { key: predicate, className: "row" },
          _react2.default.createElement(
            "span",
            { style: { display: "inline-block", paddingLeft: "12px", width: "92px" } },
            (0, _camel2label2.default)(predicate)
          ),
          _react2.default.createElement(_columnSelect2.default, { columns: columns, ignoredColumns: ignoredColumns,
            selectedColumn: getObjectForPredicate(predicateObjectMappings, predicate),
            onColumnSelect: function onColumnSelect(value) {
              return _onColumnSelect(value, predicate);
            },
            onClearColumn: function onClearColumn(value) {
              return _onClearColumn(value, predicate);
            }
          })
        );
      });

      return _react2.default.createElement(
        "div",
        null,
        formRows
      );
    }
  }]);

  return NamesForm;
}(_react2.default.Component);

exports.default = NamesForm;

},{"../../accessors/property-mappings":95,"../../util/camel2label":147,"./column-select":106,"react":"react"}],108:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _columnSelect = require("./column-select");

var _columnSelect2 = _interopRequireDefault(_columnSelect);

var _namesForm = require("./names-form");

var _namesForm2 = _interopRequireDefault(_namesForm);

var _relationForm = require("./relation-form");

var _relationForm2 = _interopRequireDefault(_relationForm);

var _propertyMappings = require("../../accessors/property-mappings");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var typeMap = {
  text: function text(props) {
    return _react2.default.createElement(_columnSelect2.default, props);
  },
  datable: function datable(props) {
    return _react2.default.createElement(_columnSelect2.default, props);
  },
  select: function select(props) {
    return _react2.default.createElement(_columnSelect2.default, props);
  },
  names: function names(props) {
    return _react2.default.createElement(_namesForm2.default, props);
  },
  relation: function relation(props) {
    return _react2.default.createElement(_relationForm2.default, props);
  },
  "relation-to-existing": function relationToExisting(props) {
    return _react2.default.createElement(RelationToExistingForm, props);
  },
  multiselect: function multiselect(props) {
    return _react2.default.createElement(_columnSelect2.default, props);
  }
};

var isCompleteForNames = function isCompleteForNames(type, predicateObjectMappings) {
  return type === "names" && predicateObjectMappings.filter(function (pom) {
    return ["forename", "surname", "nameLink", "genName", "roleName"].indexOf(pom.predicate) > -1;
  }).filter(function (pom) {
    return (0, _propertyMappings.propertyMappingIsComplete)(pom);
  }).length > 0;
};

var PropertyForm = function (_React$Component) {
  _inherits(PropertyForm, _React$Component);

  function PropertyForm() {
    _classCallCheck(this, PropertyForm);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(PropertyForm).apply(this, arguments));
  }

  _createClass(PropertyForm, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var onAddPredicateObjectMap = _props.onAddPredicateObjectMap;
      var onRemovePredicateObjectMap = _props.onRemovePredicateObjectMap;
      var onRemoveCustomProperty = _props.onRemoveCustomProperty;
      var availableCollectionColumnsPerArchetype = _props.availableCollectionColumnsPerArchetype;
      var relationTypeInfo = _props.relationTypeInfo;
      var targetableVres = _props.targetableVres;
      var _props2 = this.props;
      var predicateName = _props2.name;
      var type = _props2.type;
      var custom = _props2.custom;
      var customIndex = _props2.customIndex;
      var columns = _props2.columns;
      var ignoredColumns = _props2.ignoredColumns;
      var predicateObjectMap = _props2.predicateObjectMap;
      var predicateObjectMappings = _props2.predicateObjectMappings;


      var formComponent = typeMap[type] ? typeMap[type]({
        columns: columns,
        ignoredColumns: ignoredColumns,
        selectedColumn: (0, _propertyMappings.getColumnValue)(predicateObjectMap),
        predicateObjectMap: predicateObjectMap,
        predicateObjectMappings: predicateObjectMappings,
        availableCollectionColumnsPerArchetype: availableCollectionColumnsPerArchetype,
        relationTypeInfo: relationTypeInfo,
        targetableVres: targetableVres,
        onColumnSelect: function onColumnSelect(value, predicate) {
          return onAddPredicateObjectMap(predicate || predicateName, value, type);
        },
        onClearColumn: function onClearColumn(value, predicate) {
          return onRemovePredicateObjectMap(predicate || predicateName, value);
        }
      }) : _react2.default.createElement(
        "span",
        null,
        "type not yet supported: ",
        _react2.default.createElement(
          "span",
          { style: { color: "red" } },
          type
        )
      );

      var unConfirmButton = (0, _propertyMappings.propertyMappingIsComplete)(predicateObjectMap) || isCompleteForNames(type, predicateObjectMappings) ? _react2.default.createElement(
        "button",
        { className: "btn btn-blank", onClick: function onClick() {
            return onRemovePredicateObjectMap(predicateName, (0, _propertyMappings.getColumnValue)(predicateObjectMap));
          } },
        _react2.default.createElement("span", { className: "hi-success glyphicon glyphicon-ok" })
      ) : null;

      return _react2.default.createElement(
        "div",
        { className: "row small-margin" },
        _react2.default.createElement(
          "div",
          { className: "col-sm-2 pad-6-12" },
          _react2.default.createElement(
            "strong",
            null,
            predicateName
          ),
          _react2.default.createElement(
            "span",
            { className: "pull-right", style: { fontSize: "0.7em" } },
            "(",
            type,
            ")"
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-8" },
          formComponent
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-1" },
          custom ? _react2.default.createElement(
            "button",
            { className: "btn btn-blank pull-right", type: "button", onClick: function onClick() {
                return onRemoveCustomProperty(customIndex);
              } },
            _react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
          ) : null
        ),
        _react2.default.createElement(
          "div",
          { className: "col-sm-1 hi-success" },
          unConfirmButton
        )
      );
    }
  }]);

  return PropertyForm;
}(_react2.default.Component);

exports.default = PropertyForm;

},{"../../accessors/property-mappings":95,"./column-select":106,"./names-form":107,"./relation-form":109,"react":"react"}],109:[function(require,module,exports){
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

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var getSelectedTargetColumn = function getSelectedTargetColumn(objectMap) {
  return objectMap.joinCondition && objectMap.joinCondition.parent && objectMap.parentTriplesMap ? objectMap.parentTriplesMap + "!" + objectMap.joinCondition.parent : null;
};

var RelationForm = function (_React$Component) {
  _inherits(RelationForm, _React$Component);

  function RelationForm() {
    _classCallCheck(this, RelationForm);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(RelationForm).apply(this, arguments));
  }

  _createClass(RelationForm, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var _onColumnSelect = _props.onColumnSelect;
      var optionalPredicateObjectMap = _props.predicateObjectMap;
      var availableCollectionColumnsPerArchetype = _props.availableCollectionColumnsPerArchetype;
      var relationTypeInfo = _props.relationTypeInfo;


      var objectMap = (optionalPredicateObjectMap || {}).objectMap || {};

      var sourceColumnProps = _extends({}, this.props, {
        valuePrefix: "(source) ",
        placeholder: "Select a source column...",
        onColumnSelect: function onColumnSelect(value) {
          return _onColumnSelect(_extends({}, objectMap || {}, {
            joinCondition: _extends({}, (objectMap || {}).joinCondition || {}, {
              child: value
            })
          }));
        }
      });

      var targetCollectionColumns = availableCollectionColumnsPerArchetype[relationTypeInfo.relation.targetCollection].map(function (targetCollectionCols) {
        return targetCollectionCols.columns.map(function (column) {
          return targetCollectionCols.collectionName + "!" + column;
        });
      }).reduce(function (a, b) {
        return a.concat(b);
      });

      var targetColumnProps = {
        valuePrefix: "(target) ",
        columns: targetCollectionColumns,
        selectedColumn: getSelectedTargetColumn(objectMap),
        ignoredColumns: [],
        placeholder: "Select a target column...",
        onColumnSelect: function onColumnSelect(value) {
          return _onColumnSelect(_extends({}, objectMap || {}, {
            joinCondition: _extends({}, (objectMap || {}).joinCondition || {}, {
              parent: value.split("!")[1]
            }),
            parentTriplesMap: value.split("!")[0]
          }));
        }
      };

      return _react2.default.createElement(
        "div",
        null,
        _react2.default.createElement(_columnSelect2.default, sourceColumnProps),
        _react2.default.createElement(_columnSelect2.default, targetColumnProps)
      );
    }
  }]);

  return RelationForm;
}(_react2.default.Component);

exports.default = RelationForm;

},{"./column-select":106,"react":"react"}],110:[function(require,module,exports){
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

var _firstUpload = require("./firstUpload");

var _firstUpload2 = _interopRequireDefault(_firstUpload);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function CollectionOverview(props) {
  var onUploadFileSelect = props.onUploadFileSelect;
  var userId = props.userId;
  var uploadStatus = props.uploadStatus;
  var vres = props.vres;
  var searchGuiUrl = props.searchGuiUrl;
  var onContinueMapping = props.onContinueMapping;


  var uploadButton = _react2.default.createElement(_uploadButton2.default, {
    classNames: ["btn", "btn-lg", "btn-primary", "pull-right"],
    glyphicon: "glyphicon glyphicon-cloud-upload",
    uploadStatus: uploadStatus,
    label: "Upload new dataset",
    onUploadFileSelect: onUploadFileSelect });

  return vres && Object.keys(vres).length > 0 ? _react2.default.createElement(
    "div",
    null,
    _react2.default.createElement(
      "div",
      { className: "container" },
      _react2.default.createElement(
        _datasetCards2.default,
        { userId: userId, caption: "My datasets", vres: vres, mine: true, searchGuiUrl: searchGuiUrl,
          onContinueMapping: onContinueMapping },
        uploadButton
      )
    )
  ) : _react2.default.createElement(_firstUpload2.default, props);
}

exports.default = CollectionOverview;

},{"./dataset-cards":115,"./firstUpload":118,"./upload-button":124,"react":"react"}],111:[function(require,module,exports){
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

},{"./table/data-row":122,"./table/header-cell":123,"react":"react"}],112:[function(require,module,exports){
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

var CollectionTabs = function (_React$Component) {
  _inherits(CollectionTabs, _React$Component);

  function CollectionTabs() {
    _classCallCheck(this, CollectionTabs);

    return _possibleConstructorReturn(this, Object.getPrototypeOf(CollectionTabs).apply(this, arguments));
  }

  _createClass(CollectionTabs, [{
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
                    return collectionTab.active ? false : onSelectCollection(collectionTab.collectionName);
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

  return CollectionTabs;
}(_react2.default.Component);

exports.default = CollectionTabs;

},{"classnames":2,"react":"react"}],113:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _collectionTabs = require("./collection-tabs");

var _collectionTabs2 = _interopRequireDefault(_collectionTabs);

var _message = require("./message");

var _message2 = _interopRequireDefault(_message);

var _collectionTable = require("./collection-table");

var _collectionTable2 = _interopRequireDefault(_collectionTable);

var _collectionForm = require("./collection-form/collection-form");

var _collectionForm2 = _interopRequireDefault(_collectionForm);

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

    return _possibleConstructorReturn(this, Object.getPrototypeOf(ConnectData).apply(this, arguments));
  }

  _createClass(ConnectData, [{
    key: "componentWillReceiveProps",
    value: function componentWillReceiveProps(nextProps) {
      var _props = this.props;
      var onFetchBulkUploadedMetadata = _props.onFetchBulkUploadedMetadata;
      var serializedArchetypeMappings = _props.params.serializedArchetypeMappings;
      // Triggers fetch data from server based on vreId from route.

      if (this.props.params.vreId !== nextProps.params.vreId) {
        onFetchBulkUploadedMetadata(nextProps.params.vreId, JSON.parse(decodeURIComponent(serializedArchetypeMappings)));
      }
    }
  }, {
    key: "componentDidMount",
    value: function componentDidMount() {
      var _props2 = this.props;
      var onFetchBulkUploadedMetadata = _props2.onFetchBulkUploadedMetadata;
      var tabs = _props2.tabs;
      var vre = _props2.vre;
      var vreId = _props2.vreId;
      var serializedArchetypeMappings = _props2.params.serializedArchetypeMappings;

      if (tabs.length === 0 || vre !== vreId) {
        onFetchBulkUploadedMetadata(vreId, JSON.parse(decodeURIComponent(serializedArchetypeMappings)));
      }
    }
  }, {
    key: "render",
    value: function render() {
      var _props3 = this.props;
      var _onCloseMessage = _props3.onCloseMessage;
      var onSelectCollection = _props3.onSelectCollection;
      var _onLoadMoreClick = _props3.onLoadMoreClick;
      var _onIgnoreColumnToggle = _props3.onIgnoreColumnToggle;
      var onPublishData = _props3.onPublishData;
      var onUploadFileSelect = _props3.onUploadFileSelect;
      var _props4 = this.props;
      var onAddPredicateObjectMap = _props4.onAddPredicateObjectMap;
      var onRemovePredicateObjectMap = _props4.onRemovePredicateObjectMap;
      var onAddCustomProperty = _props4.onAddCustomProperty;
      var onRemoveCustomProperty = _props4.onRemoveCustomProperty;
      var _props5 = this.props;
      var vreId = _props5.params.vreId;
      var vre = _props5.vre;
      var tabs = _props5.tabs;
      var showCollectionsAreConnectedMessage = _props5.showCollectionsAreConnectedMessage;
      var uploadedFilename = _props5.uploadedFilename;
      var publishEnabled = _props5.publishEnabled;
      var publishStatus = _props5.publishStatus;
      var publishErrors = _props5.publishErrors;
      var uploadStatus = _props5.uploadStatus;
      var availableArchetypes = _props5.availableArchetypes;
      var customProperties = _props5.customProperties;
      var availableCollectionColumnsPerArchetype = _props5.availableCollectionColumnsPerArchetype;
      var rmlPreviewData = _props5.rmlPreviewData;
      var targetableVres = _props5.targetableVres;

      // table view properties

      var _props6 = this.props;
      var rows = _props6.rows;
      var headers = _props6.headers;
      var nextUrl = _props6.nextUrl;
      var activeCollection = _props6.activeCollection;

      // form view properties

      var _props7 = this.props;
      var archetypeFields = _props7.archetypeFields;
      var columns = _props7.columns;
      var ignoredColumns = _props7.ignoredColumns;
      var predicateObjectMappings = _props7.predicateObjectMappings;


      if (!archetypeFields || tabs.length === 0 || vre !== vreId) {
        return null;
      }

      var rmlPreviewBlock = rmlPreviewData ? _react2.default.createElement(
        "div",
        { style: { position: "absolute", zIndex: "10", width: "100%", top: "90px" } },
        _react2.default.createElement(
          "pre",
          { style: { width: "80%", margin: "0 auto", backgroundColor: "#ddd" } },
          JSON.stringify(rmlPreviewData, null, 2)
        )
      ) : null;

      var publishFailedMessage = publishErrors ? _react2.default.createElement(
        _message2.default,
        { alertLevel: "danger", dismissible: false },
        _react2.default.createElement(_uploadButton2.default, { classNames: ["btn", "btn-danger", "pull-right", "btn-xs"], label: "Re-upload",
          onUploadFileSelect: onUploadFileSelect, uploadStatus: uploadStatus }),
        _react2.default.createElement("span", { className: "glyphicon glyphicon-exclamation-sign" }),
        " ",
        "Publish failed. Please fix the mappings or re-upload the data."
      ) : null;

      var collectionsAreConnectedMessage = showCollectionsAreConnectedMessage && uploadedFilename ? _react2.default.createElement(
        _message2.default,
        { alertLevel: "info", dismissible: true, onCloseMessage: function onCloseMessage() {
            return _onCloseMessage("showCollectionsAreConnectedMessage");
          } },
        tabs.map(function (tab) {
          return _react2.default.createElement(
            "em",
            { key: tab.collectionName },
            tab.collectionName
          );
        }).reduce(function (accu, elem) {
          return accu === null ? [elem] : [].concat(_toConsumableArray(accu), [' and ', elem]);
        }, null),
        " from ",
        _react2.default.createElement(
          "em",
          null,
          uploadedFilename
        ),
        " ",
        tabs.length === 1 ? "is" : "are",
        " connected to the Timbuctoo Archetypes."
      ) : null;

      return _react2.default.createElement(
        "div",
        null,
        rmlPreviewBlock,
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
        _react2.default.createElement(_collectionTabs2.default, { collectionTabs: tabs, onSelectCollection: onSelectCollection }),
        _react2.default.createElement(_collectionForm2.default, { archetypeFields: archetypeFields, columns: columns, ignoredColumns: ignoredColumns,
          availableArchetypes: availableArchetypes,
          availableCollectionColumnsPerArchetype: availableCollectionColumnsPerArchetype,
          customProperties: customProperties,
          onAddCustomProperty: onAddCustomProperty,
          onRemoveCustomProperty: onRemoveCustomProperty,
          predicateObjectMappings: predicateObjectMappings,
          onAddPredicateObjectMap: onAddPredicateObjectMap,
          onRemovePredicateObjectMap: onRemovePredicateObjectMap,
          targetableVres: targetableVres }),
        _react2.default.createElement(
          "div",
          { className: "container big-margin" },
          _react2.default.createElement(
            "button",
            { onClick: onPublishData, className: "btn btn-warning btn-lg pull-right", type: "button", disabled: !publishEnabled },
            publishStatus
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
            " ",
            uploadedFilename ? "from " + uploadedFilename : ""
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

},{"./collection-form/collection-form":105,"./collection-table":111,"./collection-tabs":112,"./message":120,"./upload-button":124,"react":"react"}],114:[function(require,module,exports){
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

var _router = require("../router");

var _reactRouter = require("react-router");

var _collectionTable = require("./collection-table");

var _collectionTable2 = _interopRequireDefault(_collectionTable);

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
    key: "componentWillReceiveProps",
    value: function componentWillReceiveProps(nextProps) {
      var onFetchBulkUploadedMetadata = this.props.onFetchBulkUploadedMetadata;
      // Triggers fetch data from server based on vreId from route.

      if (this.props.params.vreId !== nextProps.params.vreId) {
        onFetchBulkUploadedMetadata(nextProps.params.vreId);
      }
    }
  }, {
    key: "componentDidMount",
    value: function componentDidMount() {
      var _props = this.props;
      var onFetchBulkUploadedMetadata = _props.onFetchBulkUploadedMetadata;
      var collections = _props.collections;
      var vre = _props.vre;
      var vreId = _props.vreId;

      if (!collections || vre !== vreId) {
        onFetchBulkUploadedMetadata(vreId);
      }
    }
  }, {
    key: "render",
    value: function render() {
      var _props2 = this.props;
      var vreId = _props2.vreId;
      var vre = _props2.vre;
      var archetype = _props2.archetype;
      var collections = _props2.collections;
      var mappings = _props2.mappings;

      // actions

      var _props3 = this.props;
      var _onCloseMessage = _props3.onCloseMessage;
      var onMapCollectionArchetype = _props3.onMapCollectionArchetype;
      var onSelectCollection = _props3.onSelectCollection;
      var _onLoadMoreClick = _props3.onLoadMoreClick;
      // messages

      var _props4 = this.props;
      var showFileIsUploadedMessage = _props4.showFileIsUploadedMessage;
      var uploadedFileName = _props4.uploadedFileName;
      // table view properties

      var _props5 = this.props;
      var rows = _props5.rows;
      var headers = _props5.headers;
      var nextUrl = _props5.nextUrl;
      var activeCollection = _props5.activeCollection;


      if (!collections || vre !== vreId) {
        return null;
      }

      var collectionsAreMapped = Object.keys(mappings.collections).length > 0 && Object.keys(mappings.collections).map(function (key) {
        return mappings.collections[key].archetypeName;
      }).indexOf(null) < 0;

      var fileIsUploadedMessage = showFileIsUploadedMessage && uploadedFileName ? _react2.default.createElement(
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
            collections.length,
            " collections in the file. Connect the tabs to the Timbuctoo Archetypes."
          )
        ),
        _react2.default.createElement(
          "div",
          { className: "container basic-margin" },
          collections.map(function (sheet) {
            return _react2.default.createElement(
              "div",
              { className: "row", key: sheet.name },
              _react2.default.createElement(
                "div",
                { className: "col-md-2" },
                _react2.default.createElement(
                  "a",
                  { className: "from-excel", style: { cursor: "pointer" },
                    onClick: function onClick() {
                      return sheet.name === activeCollection ? false : onSelectCollection(sheet.name);
                    } },
                  _react2.default.createElement("img", { src: "images/icon-excel.svg", alt: "" }),
                  " ",
                  sheet.name,
                  " ",
                  sheet.name === activeCollection ? "*" : ""
                )
              ),
              _react2.default.createElement(
                "div",
                { className: "col-md-8" },
                _react2.default.createElement(
                  _selectField2.default,
                  {
                    onChange: function onChange(value) {
                      return onMapCollectionArchetype(sheet.name, value);
                    },
                    onClear: function onClear() {
                      return onMapCollectionArchetype(sheet.name, null);
                    },
                    value: mappings.collections[sheet.name].archetypeName },
                  _react2.default.createElement(
                    "span",
                    { type: "placeholder" },
                    "Connect ",
                    _react2.default.createElement(
                      "em",
                      null,
                      sheet.name
                    ),
                    " to a Timbuctoo archetype."
                  ),
                  Object.keys(archetype).filter(function (domain) {
                    return domain !== "relations";
                  }).sort().map(function (option) {
                    return _react2.default.createElement(
                      "span",
                      { key: option, value: option },
                      option,
                      _react2.default.createElement("br", null),
                      _react2.default.createElement(
                        "span",
                        { style: { color: "#666", fontSize: "0.6em" } },
                        "Properties: ",
                        archetype[option].filter(function (prop) {
                          return prop.type !== "relation";
                        }).map(function (prop) {
                          return prop.name + " (" + prop.type + ")";
                        }).join(", ")
                      )
                    );
                  })
                )
              ),
              mappings.collections[sheet.name].archetypeName ? _react2.default.createElement(
                "div",
                { className: "col-sm-1 hi-success", key: sheet.name },
                _react2.default.createElement("span", { className: "glyphicon glyphicon-ok pull-right" })
              ) : null
            );
          })
        ),
        _react2.default.createElement(
          "div",
          { className: "container basic-margin" },
          collectionsAreMapped ? _react2.default.createElement(
            _reactRouter.Link,
            { to: _router.urls.mapData(vre, mappings.collections), className: "btn btn-success" },
            "Connect"
          ) : _react2.default.createElement(
            "button",
            { disabled: true, className: "btn btn-success" },
            "Connect"
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
            " ",
            uploadedFileName ? "from " + uploadedFileName : ""
          ),
          _react2.default.createElement(_collectionTable2.default, {
            rows: rows,
            headers: headers,
            nextUrl: nextUrl,
            onIgnoreColumnToggle: null,
            onLoadMoreClick: function onLoadMoreClick(url) {
              return _onLoadMoreClick(url, activeCollection);
            } })
        )
      );
    }
  }]);

  return ConnectToArchetype;
}(_react2.default.Component);

exports.default = ConnectToArchetype;

},{"../router":144,"./collection-table":111,"./fields/select-field":117,"./message":120,"react":"react","react-router":71}],115:[function(require,module,exports){
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var vres = props.vres;
  var caption = props.caption;
  var userId = props.userId;
  var searchGuiUrl = props.searchGuiUrl;
  var mine = props.mine;
  var onContinueMapping = props.onContinueMapping;


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
        return _react2.default.createElement(_datasetCard2.default, { key: vre, mine: mine, published: vres[vre].published, searchGuiUrl: searchGuiUrl,
          onContinueMapping: onContinueMapping,
          userId: userId, vreId: vres[vre].name, caption: vres[vre].name.replace(/^[a-z0-9]+_/, "") });
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

},{"./datasetCard.jsx":116,"react":"react"}],116:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactRouter = require("react-router");

var _router = require("../router");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function DataSetCard(props) {
  var searchUrl = props.searchGuiUrl;

  if (props.mine && !props.published) {
    return _react2.default.createElement(
      "div",
      { className: "card-dataset" },
      _react2.default.createElement(
        _reactRouter.Link,
        { className: "card-dataset btn btn-default explore", to: _router.urls.mapArchetypes(props.vreId) },
        "Finish mapping",
        _react2.default.createElement("br", null),
        _react2.default.createElement(
          "strong",
          { title: props.caption, style: { display: "inline-block", overflow: "hidden", width: "90%", whiteSpace: "nowrap", textOverflow: "ellipsis" } },
          props.caption.replace(/^[^_]+_+/, "")
        )
      )
    );
  }

  return _react2.default.createElement(
    "div",
    { className: "card-dataset" },
    _react2.default.createElement(
      "a",
      { className: "card-dataset btn btn-default explore",
        href: searchUrl + "?vreId=" + props.vreId, target: "_blank" },
      "Explore",
      _react2.default.createElement("br", null),
      _react2.default.createElement(
        "strong",
        { title: props.caption, style: { display: "inline-block", overflow: "hidden", width: "90%", whiteSpace: "nowrap", textOverflow: "ellipsis" } },
        props.caption.replace(/^[^_]+_+/, "")
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

},{"../router":144,"react":"react","react-router":71}],117:[function(require,module,exports){
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
        { className: (0, _classnames2.default)("dropdown", { open: this.state.isOpen }), style: this.props.style || {} },
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

},{"classnames":2,"react":"react","react-dom":"react-dom"}],118:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _uploadButton = require("./upload-button");

var _uploadButton2 = _interopRequireDefault(_uploadButton);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function FirstUpload(props) {
  var onUploadFileSelect = props.onUploadFileSelect;
  var userId = props.userId;
  var uploadStatus = props.uploadStatus;


  var sampleSheet = props.exampleSheetUrl ? _react2.default.createElement(
    "p",
    null,
    "Don't have a dataset handy? Here\u2019s an ",
    _react2.default.createElement(
      "a",
      { href: props.exampleSheetUrl },
      "example excel sheet"
    ),
    "."
  ) : null;

  var uploadButton = _react2.default.createElement(_uploadButton2.default, {
    uploadStatus: uploadStatus,
    classNames: ["btn", "btn-lg", "btn-primary"],
    glyphicon: "glyphicon glyphicon-cloud-upload",
    label: "Browse",
    onUploadFileSelect: onUploadFileSelect });

  console.log(userId);
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

},{"./upload-button":124,"react":"react"}],119:[function(require,module,exports){
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

},{"react":"react"}],120:[function(require,module,exports){
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
      "\xD7"
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

},{"classnames":2,"react":"react"}],121:[function(require,module,exports){
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
        _react2.default.createElement(_datasetCards2.default, { caption: "Explore all datasets", vres: props.vres, searchGuiUrl: props.searchGuiUrl })
      ) : null
    ),
    _react2.default.createElement(_footer2.default, null)
  );
}

exports.default = Page;

},{"./dataset-cards":115,"./footer":119,"react":"react"}],122:[function(require,module,exports){
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

},{"classnames":2,"react":"react"}],123:[function(require,module,exports){
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
        onIgnoreColumnToggle !== null ? _react2.default.createElement("a", { style: { cursor: "pointer" }, className: (0, _classnames2.default)("pull-right", "glyphicon", {
            "glyphicon-ok-sign": isConfirmed,
            "glyphicon-question-sign": !isConfirmed && !isIgnored,
            "glyphicon-remove": !isConfirmed && isIgnored
          }), onClick: function onClick() {
            return !isConfirmed ? onIgnoreColumnToggle(header) : null;
          } }) : null
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

},{"classnames":2,"react":"react"}],124:[function(require,module,exports){
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
      var uploadStatus = _props.uploadStatus;
      var onUploadFileSelect = _props.onUploadFileSelect;
      var glyphicon = _props.glyphicon;
      var label = _props.label;

      return _react2.default.createElement(
        "form",
        null,
        _react2.default.createElement(
          "label",
          { className: _classnames2.default.apply(undefined, _toConsumableArray(classNames).concat([{ disabled: !!uploadStatus }])) },
          _react2.default.createElement("span", { className: glyphicon }),
          " ",
          uploadStatus || label,
          _react2.default.createElement("input", {
            disabled: !!uploadStatus,
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

},{"classnames":2,"react":"react"}],125:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (appState) {
  return {
    userId: appState.userdata.userId,
    vres: appState.userdata.myVres || {},
    searchGuiUrl: appState.datasets.searchGuiUrl,
    uploadStatus: appState.importData.uploadStatus
  };
};

},{}],126:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _table = require("./transformers/table");

var _tabs = require("./transformers/tabs");

var _generateRmlMapping = require("../util/generate-rml-mapping");

var _generateRmlMapping2 = _interopRequireDefault(_generateRmlMapping);

var _uniq = require("../util/uniq");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

function getTargetableVres(mine, vres, activeVre) {
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

  return myVres.concat(publicVres).reduce(_uniq.uniq, []).filter(function (vre) {
    return vre !== activeVre;
  });
}

exports.default = function (appState, routed) {
  var collections = appState.importData.collections;
  var mappings = appState.mappings;
  var activeCollection = appState.activeCollection;
  var archetype = appState.archetype;
  var customProperties = appState.customProperties;
  var allPredicateObjectMappings = appState.predicateObjectMappings;
  var myVres = appState.userdata.myVres;
  var publicVres = appState.datasets.publicVres;


  var predicateObjectMappings = allPredicateObjectMappings[activeCollection.name] || [];

  var archetypeFields = mappings.collections[activeCollection.name] ? archetype[mappings.collections[activeCollection.name].archetypeName] : [];

  var columnHeaders = (0, _table.transformCollectionColumns)(collections, activeCollection, mappings, predicateObjectMappings);

  var collectionTabs = (0, _tabs.transformCollectionTabs)(collections, mappings, activeCollection, allPredicateObjectMappings);

  var availableArchetypes = Object.keys(mappings.collections).map(function (key) {
    return mappings.collections[key].archetypeName;
  });

  var availableCollectionColumnsPerArchetype = availableArchetypes.map(function (archetypeName) {
    return {
      key: archetypeName,
      values: Object.keys(mappings.collections).filter(function (collectionName) {
        return mappings.collections[collectionName].archetypeName === archetypeName;
      }).map(function (collectionName) {
        return {
          collectionName: collectionName,
          columns: collections.find(function (coll) {
            return coll.name === collectionName;
          }).variables
        };
      })
    };
  }).reduce(function (accum, cur) {
    return _extends({}, accum, _defineProperty({}, cur.key, cur.values));
  }, {});

  return {
    // from router
    vreId: routed.params.vreId,
    // transformed for view
    tabs: collectionTabs,

    // messages
    showCollectionsAreConnectedMessage: appState.messages.showCollectionsAreConnectedMessage,

    // from active collection for table
    activeCollection: activeCollection.name,
    rows: (0, _table.transformCollectionRows)(collections, activeCollection, mappings),
    headers: columnHeaders,
    nextUrl: activeCollection.nextUrl,

    // from import data
    uploadStatus: appState.importData.uploadStatus,
    uploadedFilename: appState.importData.uploadedFileName,
    vre: appState.importData.vre,

    // form data
    archetypeFields: archetypeFields,
    availableArchetypes: availableArchetypes,
    availableCollectionColumnsPerArchetype: availableCollectionColumnsPerArchetype,
    columns: (0, _table.getColumnInfo)(collections, activeCollection, mappings).columns,
    ignoredColumns: (0, _table.getColumnInfo)(collections, activeCollection, mappings).ignoredColumns,
    predicateObjectMappings: predicateObjectMappings,
    publishErrors: appState.importData.publishErrors,
    publishEnabled: !appState.importData.publishing && collectionTabs.every(function (tab) {
      return tab.complete;
    }),
    publishStatus: appState.importData.publishStatus || "Publish dataset",
    customProperties: customProperties[activeCollection.name] || [],
    targetableVres: getTargetableVres(myVres, publicVres, appState.importData.vre),

    // ctrl-shift-F4
    rmlPreviewData: appState.previewRml.showRMLPreview ? (0, _generateRmlMapping2.default)(appState.importData.vre, appState.mappings.collections, allPredicateObjectMappings) : null
  };
};

},{"../util/generate-rml-mapping":149,"../util/uniq":152,"./transformers/table":130,"./transformers/tabs":131}],127:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _table = require("./transformers/table");

exports.default = function (appState, routed) {
  var collections = appState.importData.collections;
  var activeCollection = appState.activeCollection;
  var mappings = appState.mappings;


  return {
    vreId: routed.params.vreId,
    collections: appState.importData.collections,
    uploadedFileName: appState.importData.uploadedFileName,
    archetype: appState.archetype,
    mappings: appState.mappings,
    showFileIsUploadedMessage: appState.messages.showFileIsUploadedMessage,
    vre: appState.importData.vre,

    // from active collection for table
    activeCollection: activeCollection.name,
    rows: (0, _table.transformCollectionRows)(collections, activeCollection),
    headers: (0, _table.transformCollectionColumns)(collections, activeCollection, mappings),
    nextUrl: activeCollection.nextUrl

  };
};

},{"./transformers/table":130}],128:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (appState) {
  return {
    userId: appState.userdata.userId,
    uploadStatus: appState.importData.uploadStatus
  };
};

},{}],129:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (state, routed) {
  var pathname = routed.location.pathname;


  return {
    username: state.userdata.userId,
    vres: state.datasets.publicVres.filter(function (vre) {
      return vre.name !== "Admin" && vre.name !== "Base";
    }),
    searchGuiUrl: state.datasets.searchGuiUrl,
    showDatasets: pathname === "/" /* || pathname === urls.collectionsOverview(),*/
  };
};

},{}],130:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getColumnInfo = exports.transformCollectionRows = exports.transformCollectionColumns = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _propertyMappings = require("../../accessors/property-mappings");

var sheetRowFromDictToArray = function sheetRowFromDictToArray(rowdict, arrayOfVariableNames, mappingErrors) {
  return arrayOfVariableNames.map(function (name) {
    return {
      value: rowdict[name],
      error: mappingErrors[name] || null
    };
  });
};

var getColumnInfo = function getColumnInfo(collections, activeCollection, mappings) {
  var collectionInfo = (collections || []).find(function (coll) {
    return coll.name === activeCollection.name;
  });
  var columns = collectionInfo ? collectionInfo.variables : null;
  var ignoredColumns = mappings && mappings.collections[activeCollection.name] ? mappings.collections[activeCollection.name].ignoredColumns : [];

  return { columns: columns, ignoredColumns: ignoredColumns };
};

var transformCollectionRows = function transformCollectionRows(collections, activeCollection, mappings) {
  var _getColumnInfo = getColumnInfo(collections, activeCollection, mappings);

  var columns = _getColumnInfo.columns;
  var ignoredColumns = _getColumnInfo.ignoredColumns;

  return activeCollection.name && columns ? activeCollection.rows.map(function (row) {
    return sheetRowFromDictToArray(row.values, columns, row.errors).map(function (cell, colIdx) {
      return _extends({}, cell, { ignored: ignoredColumns.indexOf(columns[colIdx]) > -1
      });
    });
  }) : [];
};

var transformCollectionColumns = function transformCollectionColumns(collections, activeCollection, mappings) {
  var predicateObjectMappings = arguments.length <= 3 || arguments[3] === undefined ? [] : arguments[3];

  var _getColumnInfo2 = getColumnInfo(collections, activeCollection, mappings);

  var columns = _getColumnInfo2.columns;
  var ignoredColumns = _getColumnInfo2.ignoredColumns;

  return (columns || []).map(function (column, i) {
    return {
      name: column,
      isConfirmed: (0, _propertyMappings.propertyMappingIsComplete)(predicateObjectMappings.find(function (pom) {
        return (0, _propertyMappings.getColumnValue)(pom) === column;
      })),
      isIgnored: ignoredColumns.indexOf(column) > -1
    };
  });
};

exports.transformCollectionColumns = transformCollectionColumns;
exports.transformCollectionRows = transformCollectionRows;
exports.getColumnInfo = getColumnInfo;

},{"../../accessors/property-mappings":95}],131:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.transformCollectionTabs = undefined;

var _propertyMappings = require("../../accessors/property-mappings");

var _uniq = require("../../util/uniq");

var mappingsAreComplete = function mappingsAreComplete(allColumns, ignoredColumns, predicateObjectMappings) {
  var evaluateColumns = allColumns.filter(function (col) {
    return ignoredColumns.indexOf(col) < 0;
  });
  var mappedColumns = predicateObjectMappings.filter(function (pom) {
    return (0, _propertyMappings.propertyMappingIsComplete)(pom);
  }).map(function (pom) {
    return (0, _propertyMappings.getColumnValue)(pom);
  }).reduce(_uniq.uniq, []);

  return evaluateColumns.length <= mappedColumns.length;
};

var transformCollectionTabs = function transformCollectionTabs(collections, mappings, activeCollection, predicateObjectMappings) {
  return (collections || []).filter(function (collection) {
    return typeof mappings.collections[collection.name] !== "undefined";
  }).map(function (collection) {
    return {
      collectionName: collection.name,
      archetypeName: mappings.collections[collection.name].archetypeName,
      active: activeCollection.name === collection.name,
      complete: mappingsAreComplete(collection.variables, mappings.collections[collection.name].ignoredColumns, predicateObjectMappings[collection.name] || [])
    };
  });
};

exports.transformCollectionTabs = transformCollectionTabs;

},{"../../accessors/property-mappings":95,"../../util/uniq":152}],132:[function(require,module,exports){
"use strict";

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _router = require("./router");

var _router2 = _interopRequireDefault(_router);

var _token = require("./token");

var _token2 = _interopRequireDefault(_token);

var _fetchMyVres = require("./actions/fetch-my-vres");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

_xhr2.default.get("" + "/v2.1/javascript-globals", function (err, res) {
  var globals = JSON.parse(res.body);
  _store2.default.dispatch({ type: "SET_SEARCH_URL", data: globals.env.TIMBUCTOO_SEARCH_URL });
});

_xhr2.default.get("" + "/v2.1/system/vres", function (err, resp, body) {
  _store2.default.dispatch({ type: "SET_PUBLIC_VRES", payload: JSON.parse(body) });
});

var initialRender = function initialRender() {
  return _reactDom2.default.render(_router2.default, document.getElementById("app"));
};

document.addEventListener("DOMContentLoaded", function () {

  (0, _xhr2.default)("" + "/v2.1/metadata/Admin", function (err, resp) {

    _store2.default.dispatch({ type: "SET_ARCHETYPE_METADATA", data: JSON.parse(resp.body) });
    var token = (0, _token2.default)();
    if (token) {
      _store2.default.dispatch((0, _fetchMyVres.fetchMyVres)(token, function () {
        return initialRender();
      }));
    } else {
      initialRender();
    }
  });
});

var comboMap = {
  ctrl: false,
  shift: false,
  f4: false
};

var keyMap = {
  17: "ctrl",
  16: "shift",
  115: "f4"
};

document.addEventListener("keydown", function (ev) {
  if (keyMap[ev.keyCode]) {
    comboMap[keyMap[ev.keyCode]] = true;
  }

  if (Object.keys(comboMap).map(function (k) {
    return comboMap[k];
  }).filter(function (isPressed) {
    return isPressed;
  }).length === 3) {
    _store2.default.dispatch({ type: "PREVIEW_RML" });
  }

  if (ev.keyCode === 27) {
    _store2.default.dispatch({ type: "HIDE_RML_PREVIEW" });
  }
});

document.addEventListener("keyup", function (ev) {
  if (keyMap[ev.keyCode]) {
    comboMap[keyMap[ev.keyCode]] = false;
  }
});

},{"./actions/fetch-my-vres":98,"./router":144,"./store":145,"./token":146,"react":"react","react-dom":"react-dom","xhr":93}],133:[function(require,module,exports){
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
    case "PUBLISH_START":
      return _extends({}, initialState);
    case "RECEIVE_ACTIVE_COLLECTION":
      return _extends({}, state, {
        name: action.data.name,
        nextUrl: action.data._next,
        rows: action.data.name !== state.name ? action.data.items : state.rows.concat(action.data.items)
      });
  }

  return state;
};

var initialState = {
  name: null,
  nextUrl: null,
  rows: []
};

},{}],134:[function(require,module,exports){
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

var initialState = {};

},{}],135:[function(require,module,exports){
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
    case "LOGIN":
      return initialState;
    case "ADD_CUSTOM_PROPERTY":
      return addCustomProperty(state, action);
    case "REMOVE_CUSTOM_PROPERTY":
      return removeCustomProperty(state, action);
  }

  return state;
};

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

var initialState = {};

var addCustomProperty = function addCustomProperty(state, action) {
  var collectionCustomProperties = state[action.collection] || [];

  var customProperty = {
    propertyType: action.propertyType,
    propertyName: action.propertyName
  };

  return _extends({}, state, _defineProperty({}, action.collection, collectionCustomProperties.concat(customProperty)));
};

var removeCustomProperty = function removeCustomProperty(state, action) {
  var collectionCustomProperties = state[action.collection] || [];

  return _extends({}, state, _defineProperty({}, action.collection, collectionCustomProperties.filter(function (prop, idx) {
    return idx !== action.index;
  })));
};

},{}],136:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
  var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
  var action = arguments[1];

  switch (action.type) {
    case "SET_SEARCH_URL":
      return _extends({}, state, {
        searchGuiUrl: action.data
      });
    case "SET_PUBLIC_VRES":
      return _extends({}, state, {
        publicVres: action.payload
      });
  }

  return state;
};

var initialState = {
  searchGuiUrl: undefined,
  publicVres: []
};

},{}],137:[function(require,module,exports){
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
      return _extends({}, initialState, { uploadStatus: "transfering file" });
    case "UPLOAD_STATUS_UPDATE":
      if (action.data) {
        var failures = state.failures || 0;
        var currentSheet = state.currentSheet || "";
        var rows = state.rows || 0;
        var prevRows = state.prevRows || 0;
        if (action.data.substr(0, "failure: ".length) === "failure: ") {
          failures += 1;
        } else if (action.data.substr(0, "sheet: ".length) === "sheet: ") {
          currentSheet = action.data.substr("sheet: ".length);
          prevRows = rows;
        } else {
          rows = action.data * 1 - prevRows;
        }
        var uploadStatus = "processing " + currentSheet + " (row " + rows + (failures > 0 ? ", " + failures + " failures" : "") + ")";
        return _extends({}, state, {
          failures: failures,
          rows: rows,
          currentSheet: currentSheet,
          uploadStatus: uploadStatus
        });
      }
      return state;
    case "FINISH_UPLOAD":
      return _extends({}, state, {
        uploadStatus: undefined,
        failures: 0,
        currentSheet: "",
        rows: undefined,
        publishErrors: false,
        uploadedFileName: action.uploadedFileName,
        vre: action.data.vre,
        executeMappingUrl: action.data.executeMapping,
        collections: action.data.collections.map(function (col) {
          return _extends({}, col, {
            dataUrl: col.data,
            dataUrlWithErrors: col.dataWithErrors
          });
        })
      });

    case "PUBLISH_START":
      return _extends({}, state, {
        publishing: true
      });

    case "PUBLISH_STATUS_UPDATE":
      var publishErrorCount = state.publishErrorCount + (action.data === "F" ? 1 : 0);
      var tripleCount = action.data === "F" ? state.tripleCount : action.data;
      var publishStatus = "Publishing " + tripleCount + " triples" + (publishErrorCount > 0 ? " (" + publishErrorCount + " errors)" : "");
      return _extends({}, state, {
        publishErrorCount: publishErrorCount,
        tripleCount: tripleCount,
        publishStatus: publishStatus
      });
    case "PUBLISH_HAD_ERROR":
      // clear the sheets to force reload
      return _extends({}, state, {
        publishErrors: true,
        collections: state.collections.map(function (col) {
          return _extends({}, col, {
            dataUrl: col.data,
            dataUrlWithErrors: col.dataWithErrors
          });
        })
      });
    case "PUBLISH_SUCCEEDED":
      // clear the sheets to force reload
      return _extends({}, state, {
        publishStatus: undefined,
        publishEnabled: true,
        publishErrors: false,
        collections: state.collections.map(function (col) {
          return _extends({}, col, {
            dataUrl: col.data,
            dataUrlWithErrors: col.dataWithErrors
          });
        })
      });
    case "PUBLISH_FINISHED":
      // clear the sheets to force reload
      return _extends({}, state, {
        publishStatus: undefined,
        publishEnabled: true,
        publishErrorCount: 0,
        tripleCount: 0,
        publishing: false
      });
  }

  return state;
};

var initialState = {
  isUploading: false,
  publishing: false,
  publishEnabled: true,
  publishStatus: undefined,
  publishErrorCount: 0,
  tripleCount: 0
};

},{}],138:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _redux = require("redux");

var _messages = require("./messages");

var _messages2 = _interopRequireDefault(_messages);

var _datasets = require("./datasets");

var _datasets2 = _interopRequireDefault(_datasets);

var _userdata = require("./userdata");

var _userdata2 = _interopRequireDefault(_userdata);

var _importData = require("./import-data");

var _importData2 = _interopRequireDefault(_importData);

var _archetype = require("./archetype");

var _archetype2 = _interopRequireDefault(_archetype);

var _mappings = require("./mappings");

var _mappings2 = _interopRequireDefault(_mappings);

var _activeCollection = require("./active-collection");

var _activeCollection2 = _interopRequireDefault(_activeCollection);

var _predicateObjectMappings = require("./predicate-object-mappings");

var _predicateObjectMappings2 = _interopRequireDefault(_predicateObjectMappings);

var _customProperties = require("./custom-properties");

var _customProperties2 = _interopRequireDefault(_customProperties);

var _previewRml = require("./preview-rml");

var _previewRml2 = _interopRequireDefault(_previewRml);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (0, _redux.combineReducers)({
  messages: _messages2.default,
  datasets: _datasets2.default,
  userdata: _userdata2.default,
  importData: _importData2.default,
  archetype: _archetype2.default,
  mappings: _mappings2.default,
  activeCollection: _activeCollection2.default,
  predicateObjectMappings: _predicateObjectMappings2.default,
  customProperties: _customProperties2.default,
  previewRml: _previewRml2.default
});

},{"./active-collection":133,"./archetype":134,"./custom-properties":135,"./datasets":136,"./import-data":137,"./mappings":139,"./messages":140,"./predicate-object-mappings":141,"./preview-rml":142,"./userdata":143,"redux":86}],139:[function(require,module,exports){
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
      return _extends({}, state, {
        collections: action.data.collections.reduce(scaffoldCollectionMappings, {})
      });

    case "MAP_COLLECTION_ARCHETYPE":
      return mapCollectionArchetype(state, action);

    case "MAP_COLLECTION_ARCHETYPES":
      return _extends({}, state, {
        collections: action.data
      });

    case "TOGGLE_IGNORED_COLUMN":
      return toggleIgnoredColumn(state, action);

  }
  return state;
};

var _setIn = require("../util/set-in");

var _setIn2 = _interopRequireDefault(_setIn);

var _getIn = require("../util/get-in");

var _getIn2 = _interopRequireDefault(_getIn);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

var initialState = {
  collections: {},
  confirmed: false,
  publishing: false
};

function scaffoldCollectionMappings(init, sheet) {
  return _extends(init, _defineProperty({}, sheet.name, {
    archetypeName: null,
    ignoredColumns: []
  }));
}

var mapCollectionArchetype = function mapCollectionArchetype(state, action) {
  var newCollections = (0, _setIn2.default)([action.collection, "archetypeName"], action.value, state.collections);

  return _extends({}, state, { collections: newCollections });
};

var toggleIgnoredColumn = function toggleIgnoredColumn(state, action) {
  var current = (0, _getIn2.default)([action.collection, "ignoredColumns"], state.collections);

  if (current.indexOf(action.variableName) < 0) {
    return _extends({}, state, {
      collections: (0, _setIn2.default)([action.collection, "ignoredColumns"], current.concat(action.variableName), state.collections)
    });
  } else {
    return _extends({}, state, {
      collections: (0, _setIn2.default)([action.collection, "ignoredColumns"], current.filter(function (c) {
        return c !== action.variableName;
      }), state.collections)
    });
  }
};

},{"../util/get-in":150,"../util/set-in":151}],140:[function(require,module,exports){
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
    case "FINISH_UPLOAD":
      return initialState;
  }

  return state;
};

var initialState = {
  showFileIsUploadedMessage: true,
  showCollectionsAreConnectedMessage: true
};

},{}],141:[function(require,module,exports){
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
    case "LOGIN":
      return initialState;
    case "SET_PREDICATE_OBJECT_MAPPING":
      return setPredicateObjectMapping(state, action);
    case "REMOVE_PREDICATE_OBJECT_MAPPING":
      return removePredicateObjectMapping(state, action);
  }

  return state;
};

var _propertyMappings = require("../accessors/property-mappings");

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

var initialState = {};

function setBasicPredicateObjectMap(action, collectionPredicateObjectMappings) {
  var predicateObjectMap = {
    predicate: action.predicate,
    objectMap: {
      column: action.object
    },
    propertyType: action.propertyType
  };

  return collectionPredicateObjectMappings.filter(function (predObjMap) {
    return predObjMap.predicate !== action.predicate;
  }).concat(predicateObjectMap);
}

function setRelationPredicateObjectMap(action, collectionPredicateObjectMappings) {
  var predicateObjectMap = {
    predicate: action.predicate,
    objectMap: action.object,
    propertyType: action.propertyType,
    dataset: action.dataset
  };

  return collectionPredicateObjectMappings.filter(function (predObjMap) {
    return predObjMap.predicate !== action.predicate;
  }).concat(predicateObjectMap);
}

var setPredicateObjectMapping = function setPredicateObjectMapping(state, action) {
  var collectionPredicateObjectMappings = state[action.subjectCollection] || [];
  var newCollectionPredicateObjectMappings = action.propertyType === "relation" ? setRelationPredicateObjectMap(action, collectionPredicateObjectMappings) : setBasicPredicateObjectMap(action, collectionPredicateObjectMappings);

  return _extends({}, state, _defineProperty({}, action.subjectCollection, newCollectionPredicateObjectMappings));
};

var removePredicateObjectMapping = function removePredicateObjectMapping(state, action) {
  var collectionPredicateObjectMappings = state[action.subjectCollection] || [];

  return action.predicate === "names" ? _extends({}, state, _defineProperty({}, action.subjectCollection, collectionPredicateObjectMappings.filter(function (pom) {
    return !(pom.propertyType === "names" && ["forename", "surname", "nameLink", "genName", "roleName"].indexOf(pom.predicate) > -1);
  }))) : _extends({}, state, _defineProperty({}, action.subjectCollection, collectionPredicateObjectMappings.filter(function (pom) {
    return !(pom.predicate === action.predicate && (0, _propertyMappings.getColumnValue)(pom) === action.object);
  })));
};

},{"../accessors/property-mappings":95}],142:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
  var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
  var action = arguments[1];

  switch (action.type) {
    case "PREVIEW_RML":
      return _extends({}, state, {
        showRMLPreview: true
      });
    case "HIDE_RML_PREVIEW":
      return _extends({}, state, {
        showRMLPreview: false
      });
  }

  return state;
};

var initialState = {
  showRMLPreview: false
};

},{}],143:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
  var state = arguments.length <= 0 || arguments[0] === undefined ? initialState : arguments[0];
  var action = arguments[1];

  switch (action.type) {
    case "LOGIN":
      return _extends({}, state, {
        userId: action.data,
        myVres: action.vreData ? action.vreData.mine : null
      });
  }

  return state;
};

var initialState = {
  userId: undefined,
  myVres: undefined
};

},{}],144:[function(require,module,exports){
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

var _token = require("./token");

var _token2 = _interopRequireDefault(_token);

var _pageConnector = require("./connectors/page-connector");

var _pageConnector2 = _interopRequireDefault(_pageConnector);

var _page = require("./components/page.jsx");

var _page2 = _interopRequireDefault(_page);

var _firstUpload = require("./connectors/first-upload");

var _firstUpload2 = _interopRequireDefault(_firstUpload);

var _firstUpload3 = require("./components/firstUpload.js");

var _firstUpload4 = _interopRequireDefault(_firstUpload3);

var _collectionOverview = require("./connectors/collection-overview");

var _collectionOverview2 = _interopRequireDefault(_collectionOverview);

var _collectionOverview3 = require("./components/collection-overview");

var _collectionOverview4 = _interopRequireDefault(_collectionOverview3);

var _connectToArchetype = require("./connectors/connect-to-archetype");

var _connectToArchetype2 = _interopRequireDefault(_connectToArchetype);

var _connectToArchetype3 = require("./components/connect-to-archetype");

var _connectToArchetype4 = _interopRequireDefault(_connectToArchetype3);

var _connectData = require("./connectors/connect-data");

var _connectData2 = _interopRequireDefault(_connectData);

var _connectData3 = require("./components/connect-data");

var _connectData4 = _interopRequireDefault(_connectData3);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var serializeArchetypeMappings = function serializeArchetypeMappings(collections) {
  return encodeURIComponent(JSON.stringify(collections));
};

var urls = {
  root: function root() {
    return "/";
  },
  mapData: function mapData(vreId, mappings) {
    return vreId && mappings ? "/mapdata/" + vreId + "/" + serializeArchetypeMappings(mappings) : "/mapdata/:vreId/:serializedArchetypeMappings";
  },
  mapArchetypes: function mapArchetypes(vreId) {
    return vreId ? "/maparchetypes/" + vreId : "/maparchetypes/:vreId";
  }
};

function navigateTo(key, args) {
  _reactRouter.hashHistory.push(urls[key].apply(null, args));
}

var defaultConnect = (0, _reactRedux.connect)(function (state) {
  return state;
}, function (dispatch) {
  return (0, _actions2.default)(navigateTo, dispatch);
});

var connectComponent = function connectComponent(stateToProps) {
  return (0, _reactRedux.connect)(stateToProps, function (dispatch) {
    return (0, _actions2.default)(navigateTo, dispatch);
  });
};

var filterAuthorized = function filterAuthorized(redirectTo) {
  return function (nextState, replace) {
    if (!(0, _token2.default)()) {
      replace(redirectTo);
    }
  };
};

exports.default = _react2.default.createElement(
  _reactRedux.Provider,
  { store: _store2.default },
  _react2.default.createElement(
    _reactRouter.Router,
    { history: _reactRouter.hashHistory },
    _react2.default.createElement(
      _reactRouter.Route,
      { path: "/", component: connectComponent(_pageConnector2.default)(_page2.default) },
      _react2.default.createElement(_reactRouter.IndexRoute, { component: connectComponent(_collectionOverview2.default)(_collectionOverview4.default) }),
      _react2.default.createElement(_reactRouter.Route, { onEnter: filterAuthorized("/"),
        path: urls.mapArchetypes(), components: connectComponent(_connectToArchetype2.default)(_connectToArchetype4.default) }),
      _react2.default.createElement(_reactRouter.Route, { onEnter: filterAuthorized("/"),
        path: urls.mapData(), components: connectComponent(_connectData2.default)(_connectData4.default) })
    )
  )
);
exports.urls = urls;

},{"./actions":96,"./components/collection-overview":110,"./components/connect-data":113,"./components/connect-to-archetype":114,"./components/firstUpload.js":118,"./components/page.jsx":121,"./connectors/collection-overview":125,"./connectors/connect-data":126,"./connectors/connect-to-archetype":127,"./connectors/first-upload":128,"./connectors/page-connector":129,"./store":145,"./token":146,"react":"react","react-redux":37,"react-router":71}],145:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _redux = require("redux");

var _reduxThunk = require("redux-thunk");

var _reduxThunk2 = _interopRequireDefault(_reduxThunk);

var _reducers = require("./reducers");

var _reducers2 = _interopRequireDefault(_reducers);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var logger = function logger() {
  return function (next) {
    return function (action) {
      if (action.hasOwnProperty("type")) {
        console.log("[REDUX]", action.type, action);
      }

      return next(action);
    };
  };
};

var createStoreWithMiddleware = (0, _redux.applyMiddleware)( /*logger,*/_reduxThunk2.default)(_redux.createStore);
exports.default = createStoreWithMiddleware(_reducers2.default);

},{"./reducers":138,"redux":86,"redux-thunk":80}],146:[function(require,module,exports){
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

exports.default = function () {
  var path = window.location.search.substr(1);
  var params = path.split('&');

  for (var i in params) {
    var _params$i$split = params[i].split('=');

    var _params$i$split2 = _slicedToArray(_params$i$split, 2);

    var key = _params$i$split2[0];
    var value = _params$i$split2[1];

    if (key === 'hsid') {
      return value;
    }
  }
  return null;
};

},{}],147:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (camelCase) {
  return camelCase.replace(/([A-Z0-9])/g, function (match) {
    return " " + match.toLowerCase();
  }).trim().replace(/^./, function (match) {
    return match.toUpperCase();
  }).replace(/_/g, " ");
};

},{}],148:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

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

},{}],149:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _propertyMappings = require("../accessors/property-mappings");

var _uniq = require("./uniq");

var defaultNamespace = "http://timbuctoo.huygens.knaw.nl/";

var nameSpaces = {
  surname: "http://www.tei-c.org/ns/1.0/",
  forename: "http://www.tei-c.org/ns/1.0/",
  roleName: "http://www.tei-c.org/ns/1.0/",
  nameLink: "http://www.tei-c.org/ns/1.0/",
  genName: "http://www.tei-c.org/ns/1.0/"
};

var rmlTemplate = {
  "@context": {
    "@vocab": "http://www.w3.org/ns/r2rml#",
    "rml": "http://semweb.mmlab.be/ns/rml#",
    "tim": "http://timbuctoo.huygens.knaw.nl/mapping#",
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
    },
    "object": {
      "@type": "@id"
    }
  }
};

var getNameSpaceFor = function getNameSpaceFor(predicate) {
  return typeof nameSpaces[predicate] === "undefined" ? defaultNamespace : nameSpaces[predicate];
};

var makeMapName = function makeMapName(vre, localName) {
  return "http://timbuctoo.huygens.knaw.nl/mapping/" + vre + "/" + localName;
};

var mapBasicProperty = function mapBasicProperty(predicateObjectMap) {
  return {
    "objectMap": {
      "column": predicateObjectMap.objectMap.column
    },
    "predicate": "" + getNameSpaceFor(predicateObjectMap.predicate) + predicateObjectMap.predicate
  };
};

var mapRelationProperty = function mapRelationProperty(vre, predicateObjectMap) {
  return {
    "objectMap": {
      "joinCondition": predicateObjectMap.objectMap.joinCondition,
      "parentTriplesMap": "http://timbuctoo.huygens.knaw.nl/mapping/" + vre + "/" + predicateObjectMap.objectMap.parentTriplesMap
    },
    "predicate": "" + getNameSpaceFor(predicateObjectMap.predicate) + predicateObjectMap.predicate
  };
};

var makePredicateObjectMap = function makePredicateObjectMap(vre, predicateObjectMap) {
  if ((0, _propertyMappings.isBasicProperty)(predicateObjectMap)) {
    return mapBasicProperty(predicateObjectMap);
  }

  if (predicateObjectMap.propertyType === "relation") {
    return mapRelationProperty(vre, predicateObjectMap);
  }

  return null;
};

var mapCollection = function mapCollection(vre, archetypeName, collectionName, predicateObjectMaps) {
  return {
    "@id": makeMapName(vre, collectionName),
    "http://www.w3.org/2000/01/rdf-schema#subClassOf": "http://timbuctoo.huygens.knaw.nl/" + archetypeName.replace(/s$/, ""),
    "rml:logicalSource": {
      "rml:source": {
        "tim:rawCollection": collectionName,
        "tim:vreName": vre
      }
    },
    "subjectMap": {
      "template": makeMapName(vre, collectionName) + "/{tim_id}"
    },
    "predicateObjectMap": [{ "object": makeMapName(vre, collectionName), "predicate": "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" }].concat(predicateObjectMaps.map(function (pom) {
      return makePredicateObjectMap(vre, pom);
    }).filter(function (pom) {
      return pom !== null;
    }))
  };
};

exports.default = function (vre, collectionMappings, predicateObjectMappings) {
  return _extends({}, rmlTemplate, {
    "@graph": Object.keys(predicateObjectMappings).map(function (collectionName) {
      return mapCollection(vre, collectionMappings[collectionName].archetypeName, collectionName, predicateObjectMappings[collectionName]);
    })
  });
};

},{"../accessors/property-mappings":95,"./uniq":152}],150:[function(require,module,exports){
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

},{"./clone-deep":148}],151:[function(require,module,exports){
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

},{"./clone-deep":148}],152:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var uniq = function uniq(accum, cur) {
  return accum.indexOf(cur) < 0 ? accum.concat(cur) : accum;
};

exports.uniq = uniq;

},{}]},{},[132])(132)
});
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvcHJvY2Vzcy9icm93c2VyLmpzIiwibm9kZV9tb2R1bGVzL2NsYXNzbmFtZXMvaW5kZXguanMiLCJub2RlX21vZHVsZXMvZGVlcC1lcXVhbC9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9kZWVwLWVxdWFsL2xpYi9pc19hcmd1bWVudHMuanMiLCJub2RlX21vZHVsZXMvZGVlcC1lcXVhbC9saWIva2V5cy5qcyIsIm5vZGVfbW9kdWxlcy9mb3ItZWFjaC9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9nbG9iYWwvd2luZG93LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL0FjdGlvbnMuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvQXN5bmNVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9ET01TdGF0ZVN0b3JhZ2UuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvRE9NVXRpbHMuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvRXhlY3V0aW9uRW52aXJvbm1lbnQuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvUGF0aFV0aWxzLmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2NyZWF0ZUJyb3dzZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2NyZWF0ZURPTUhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvY3JlYXRlSGFzaEhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvaGlzdG9yeS9saWIvY3JlYXRlSGlzdG9yeS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9jcmVhdGVMb2NhdGlvbi5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9jcmVhdGVNZW1vcnlIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL2hpc3RvcnkvbGliL2RlcHJlY2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi9ydW5UcmFuc2l0aW9uSG9vay5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi91c2VCYXNlbmFtZS5qcyIsIm5vZGVfbW9kdWxlcy9oaXN0b3J5L2xpYi91c2VRdWVyaWVzLmpzIiwibm9kZV9tb2R1bGVzL2hpc3Rvcnkvbm9kZV9tb2R1bGVzL3dhcm5pbmcvYnJvd3Nlci5qcyIsIm5vZGVfbW9kdWxlcy9ob2lzdC1ub24tcmVhY3Qtc3RhdGljcy9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9pbnZhcmlhbnQvYnJvd3Nlci5qcyIsIm5vZGVfbW9kdWxlcy9pcy1mdW5jdGlvbi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX2dldFByb3RvdHlwZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX2lzSG9zdE9iamVjdC5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX292ZXJBcmcuanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL2lzT2JqZWN0TGlrZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvaXNQbGFpbk9iamVjdC5qcyIsIm5vZGVfbW9kdWxlcy9wYXJzZS1oZWFkZXJzL3BhcnNlLWhlYWRlcnMuanMiLCJub2RlX21vZHVsZXMvcXVlcnktc3RyaW5nL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi9jb21wb25lbnRzL1Byb3ZpZGVyLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi9jb21wb25lbnRzL2Nvbm5lY3QuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJlZHV4L2xpYi91dGlscy9zaGFsbG93RXF1YWwuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3N0b3JlU2hhcGUuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3dhcm5pbmcuanMiLCJub2RlX21vZHVsZXMvcmVhY3QtcmVkdXgvbGliL3V0aWxzL3dyYXBBY3Rpb25DcmVhdG9ycy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0FzeW5jVXRpbHMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9IaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvSW5kZXhMaW5rLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvSW5kZXhSZWRpcmVjdC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0luZGV4Um91dGUuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9JbnRlcm5hbFByb3BUeXBlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0xpZmVjeWNsZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL0xpbmsuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9QYXR0ZXJuVXRpbHMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9Qcm9wVHlwZXMuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9SZWRpcmVjdC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvUm91dGVDb250ZXh0LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvUm91dGVVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlci5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRlckNvbnRleHQuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9Sb3V0ZXJVdGlscy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL1JvdXRpbmdDb250ZXh0LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvVHJhbnNpdGlvblV0aWxzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvYXBwbHlSb3V0ZXJNaWRkbGV3YXJlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvYnJvd3Nlckhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9jb21wdXRlQ2hhbmdlZFJvdXRlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2NyZWF0ZU1lbW9yeUhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9jcmVhdGVSb3V0ZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvZ2V0Q29tcG9uZW50cy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2dldFJvdXRlUGFyYW1zLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvaGFzaEhpc3RvcnkuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL2lzQWN0aXZlLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvbWFrZVN0YXRlV2l0aExvY2F0aW9uLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvbWF0Y2guanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi9tYXRjaFJvdXRlcy5qcyIsIm5vZGVfbW9kdWxlcy9yZWFjdC1yb3V0ZXIvbGliL3JvdXRlcldhcm5pbmcuanMiLCJub2RlX21vZHVsZXMvcmVhY3Qtcm91dGVyL2xpYi91c2VSb3V0ZXJIaXN0b3J5LmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvdXNlUm91dGVzLmpzIiwibm9kZV9tb2R1bGVzL3JlYWN0LXJvdXRlci9saWIvd2l0aFJvdXRlci5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC10aHVuay9saWIvaW5kZXguanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2FwcGx5TWlkZGxld2FyZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvYmluZEFjdGlvbkNyZWF0b3JzLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9jb21iaW5lUmVkdWNlcnMuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NvbXBvc2UuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NyZWF0ZVN0b3JlLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvdXRpbHMvd2FybmluZy5qcyIsIm5vZGVfbW9kdWxlcy9zdHJpY3QtdXJpLWVuY29kZS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9wb255ZmlsbC5qcyIsIm5vZGVfbW9kdWxlcy90cmltL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3hoci9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy94dGVuZC9pbW11dGFibGUuanMiLCJzcmMvYWNjZXNzb3JzL3Byb3BlcnR5LW1hcHBpbmdzLmpzIiwic3JjL2FjdGlvbnMuanMiLCJzcmMvYWN0aW9ucy9mZXRjaC1idWxrdXBsb2FkZWQtbWV0YWRhdGEuanMiLCJzcmMvYWN0aW9ucy9mZXRjaC1teS12cmVzLmpzIiwic3JjL2FjdGlvbnMvcHJlZGljYXRlLW9iamVjdC1tYXBwaW5ncy5qcyIsInNyYy9hY3Rpb25zL3B1Ymxpc2gtbWFwcGluZ3MuanMiLCJzcmMvYWN0aW9ucy9zZWxlY3QtY29sbGVjdGlvbi5qcyIsInNyYy9hY3Rpb25zL3VwbG9hZC5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tZm9ybS9hZGQtcHJvcGVydHkuanMiLCJzcmMvY29tcG9uZW50cy9jb2xsZWN0aW9uLWZvcm0vYWRkLXJlbGF0aW9uLmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1mb3JtL2NvbGxlY3Rpb24tZm9ybS5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tZm9ybS9jb2x1bW4tc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1mb3JtL25hbWVzLWZvcm0uanMiLCJzcmMvY29tcG9uZW50cy9jb2xsZWN0aW9uLWZvcm0vcHJvcGVydHktZm9ybS5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tZm9ybS9yZWxhdGlvbi1mb3JtLmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1vdmVydmlldy5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tdGFibGUuanMiLCJzcmMvY29tcG9uZW50cy9jb2xsZWN0aW9uLXRhYnMuanMiLCJzcmMvY29tcG9uZW50cy9jb25uZWN0LWRhdGEuanMiLCJzcmMvY29tcG9uZW50cy9jb25uZWN0LXRvLWFyY2hldHlwZS5qcyIsInNyYy9jb21wb25lbnRzL2RhdGFzZXQtY2FyZHMuanMiLCJzcmMvY29tcG9uZW50cy9kYXRhc2V0Q2FyZC5qc3giLCJzcmMvY29tcG9uZW50cy9maWVsZHMvc2VsZWN0LWZpZWxkLmpzIiwic3JjL2NvbXBvbmVudHMvZmlyc3RVcGxvYWQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvY29tcG9uZW50cy90YWJsZS9kYXRhLXJvdy5qcyIsInNyYy9jb21wb25lbnRzL3RhYmxlL2hlYWRlci1jZWxsLmpzIiwic3JjL2NvbXBvbmVudHMvdXBsb2FkLWJ1dHRvbi5qcyIsInNyYy9jb25uZWN0b3JzL2NvbGxlY3Rpb24tb3ZlcnZpZXcuanMiLCJzcmMvY29ubmVjdG9ycy9jb25uZWN0LWRhdGEuanMiLCJzcmMvY29ubmVjdG9ycy9jb25uZWN0LXRvLWFyY2hldHlwZS5qcyIsInNyYy9jb25uZWN0b3JzL2ZpcnN0LXVwbG9hZC5qcyIsInNyYy9jb25uZWN0b3JzL3BhZ2UtY29ubmVjdG9yLmpzIiwic3JjL2Nvbm5lY3RvcnMvdHJhbnNmb3JtZXJzL3RhYmxlLmpzIiwic3JjL2Nvbm5lY3RvcnMvdHJhbnNmb3JtZXJzL3RhYnMuanMiLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvYWN0aXZlLWNvbGxlY3Rpb24uanMiLCJzcmMvcmVkdWNlcnMvYXJjaGV0eXBlLmpzIiwic3JjL3JlZHVjZXJzL2N1c3RvbS1wcm9wZXJ0aWVzLmpzIiwic3JjL3JlZHVjZXJzL2RhdGFzZXRzLmpzIiwic3JjL3JlZHVjZXJzL2ltcG9ydC1kYXRhLmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21hcHBpbmdzLmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3ByZWRpY2F0ZS1vYmplY3QtbWFwcGluZ3MuanMiLCJzcmMvcmVkdWNlcnMvcHJldmlldy1ybWwuanMiLCJzcmMvcmVkdWNlcnMvdXNlcmRhdGEuanMiLCJzcmMvcm91dGVyLmpzIiwic3JjL3N0b3JlLmpzIiwic3JjL3Rva2VuLmpzIiwic3JjL3V0aWwvY2FtZWwybGFiZWwuanMiLCJzcmMvdXRpbC9jbG9uZS1kZWVwLmpzIiwic3JjL3V0aWwvZ2VuZXJhdGUtcm1sLW1hcHBpbmcuanMiLCJzcmMvdXRpbC9nZXQtaW4uanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiLCJzcmMvdXRpbC91bmlxLmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FDQUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNoS0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDaERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDVEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDOUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDVEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUN6REE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUN4RUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzFFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUNKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQzlDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDbkxBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ3ZDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDclBBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUMvUkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7OztBQ2xEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUN6SkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7O0FDbEJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUN2QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7Ozs7QUM3SkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQy9LQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1REE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2xEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNuREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDZkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDTkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3BCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDN0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3JFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM5QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbEVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM3RUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3hZQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2hCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3pCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ1ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ1hBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3ZGQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMzQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzlEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDM0RBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbkVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMxS0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNuTkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwR0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3JHQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDeERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVGQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQy9OQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDM0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM3QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN6SEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNqREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDZkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1RUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMvQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbEJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDblRBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMxRUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDN0NBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDekJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMzSkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2SkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDaERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNoRkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMxUEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ25DQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3RCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3ZDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzlIQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDclFBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQ05BO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUNKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ25CQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUNkQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtB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YTtBQUFBLENBQWY7O0FBS0EsSUFBTSxRQUFRLFNBQVIsS0FBUSxDQUFDLElBQUQsRUFBTyxLQUFQLEVBQWMsSUFBZDtBQUFBLFFBQ2IsT0FBTyx5QkFBTSxJQUFOLENBQVAsRUFBb0IsS0FBcEIsRUFBMkIseUJBQU0sSUFBTixDQUEzQixDQURhO0FBQUEsQ0FBZDs7a0JBR2UsSzs7Ozs7Ozs7QUNuQmYsSUFBTSxPQUFPLFNBQVAsSUFBTyxDQUFDLEtBQUQsRUFBUSxHQUFSO0FBQUEsU0FBZ0IsTUFBTSxPQUFOLENBQWMsR0FBZCxJQUFxQixDQUFyQixHQUF5QixNQUFNLE1BQU4sQ0FBYSxHQUFiLENBQXpCLEdBQTZDLEtBQTdEO0FBQUEsQ0FBYjs7UUFFUyxJLEdBQUEsSSIsImZpbGUiOiJnZW5lcmF0ZWQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlc0NvbnRlbnQiOlsiKGZ1bmN0aW9uIGUodCxuLHIpe2Z1bmN0aW9uIHMobyx1KXtpZighbltvXSl7aWYoIXRbb10pe3ZhciBhPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7aWYoIXUmJmEpcmV0dXJuIGEobywhMCk7aWYoaSlyZXR1cm4gaShvLCEwKTt2YXIgZj1uZXcgRXJyb3IoXCJDYW5ub3QgZmluZCBtb2R1bGUgJ1wiK28rXCInXCIpO3Rocm93IGYuY29kZT1cIk1PRFVMRV9OT1RfRk9VTkRcIixmfXZhciBsPW5bb109e2V4cG9ydHM6e319O3Rbb11bMF0uY2FsbChsLmV4cG9ydHMsZnVuY3Rpb24oZSl7dmFyIG49dFtvXVsxXVtlXTtyZXR1cm4gcyhuP246ZSl9LGwsbC5leHBvcnRzLGUsdCxuLHIpfXJldHVybiBuW29dLmV4cG9ydHN9dmFyIGk9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtmb3IodmFyIG89MDtvPHIubGVuZ3RoO28rKylzKHJbb10pO3JldHVybiBzfSkiLCIvLyBzaGltIGZvciB1c2luZyBwcm9jZXNzIGluIGJyb3dzZXJcbnZhciBwcm9jZXNzID0gbW9kdWxlLmV4cG9ydHMgPSB7fTtcblxuLy8gY2FjaGVkIGZyb20gd2hhdGV2ZXIgZ2xvYmFsIGlzIHByZXNlbnQgc28gdGhhdCB0ZXN0IHJ1bm5lcnMgdGhhdCBzdHViIGl0XG4vLyBkb24ndCBicmVhayB0aGluZ3MuICBCdXQgd2UgbmVlZCB0byB3cmFwIGl0IGluIGEgdHJ5IGNhdGNoIGluIGNhc2UgaXQgaXNcbi8vIHdyYXBwZWQgaW4gc3RyaWN0IG1vZGUgY29kZSB3aGljaCBkb2Vzbid0IGRlZmluZSBhbnkgZ2xvYmFscy4gIEl0J3MgaW5zaWRlIGFcbi8vIGZ1bmN0aW9uIGJlY2F1c2UgdHJ5L2NhdGNoZXMgZGVvcHRpbWl6ZSBpbiBjZXJ0YWluIGVuZ2luZXMuXG5cbnZhciBjYWNoZWRTZXRUaW1lb3V0O1xudmFyIGNhY2hlZENsZWFyVGltZW91dDtcblxuKGZ1bmN0aW9uICgpIHtcbiAgICB0cnkge1xuICAgICAgICBjYWNoZWRTZXRUaW1lb3V0ID0gc2V0VGltZW91dDtcbiAgICB9IGNhdGNoIChlKSB7XG4gICAgICAgIGNhY2hlZFNldFRpbWVvdXQgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ3NldFRpbWVvdXQgaXMgbm90IGRlZmluZWQnKTtcbiAgICAgICAgfVxuICAgIH1cbiAgICB0cnkge1xuICAgICAgICBjYWNoZWRDbGVhclRpbWVvdXQgPSBjbGVhclRpbWVvdXQ7XG4gICAgfSBjYXRjaCAoZSkge1xuICAgICAgICBjYWNoZWRDbGVhclRpbWVvdXQgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ2NsZWFyVGltZW91dCBpcyBub3QgZGVmaW5lZCcpO1xuICAgICAgICB9XG4gICAgfVxufSAoKSlcbmZ1bmN0aW9uIHJ1blRpbWVvdXQoZnVuKSB7XG4gICAgaWYgKGNhY2hlZFNldFRpbWVvdXQgPT09IHNldFRpbWVvdXQpIHtcbiAgICAgICAgLy9ub3JtYWwgZW52aXJvbWVudHMgaW4gc2FuZSBzaXR1YXRpb25zXG4gICAgICAgIHJldHVybiBzZXRUaW1lb3V0KGZ1biwgMCk7XG4gICAgfVxuICAgIHRyeSB7XG4gICAgICAgIC8vIHdoZW4gd2hlbiBzb21lYm9keSBoYXMgc2NyZXdlZCB3aXRoIHNldFRpbWVvdXQgYnV0IG5vIEkuRS4gbWFkZG5lc3NcbiAgICAgICAgcmV0dXJuIGNhY2hlZFNldFRpbWVvdXQoZnVuLCAwKTtcbiAgICB9IGNhdGNoKGUpe1xuICAgICAgICB0cnkge1xuICAgICAgICAgICAgLy8gV2hlbiB3ZSBhcmUgaW4gSS5FLiBidXQgdGhlIHNjcmlwdCBoYXMgYmVlbiBldmFsZWQgc28gSS5FLiBkb2Vzbid0IHRydXN0IHRoZSBnbG9iYWwgb2JqZWN0IHdoZW4gY2FsbGVkIG5vcm1hbGx5XG4gICAgICAgICAgICByZXR1cm4gY2FjaGVkU2V0VGltZW91dC5jYWxsKG51bGwsIGZ1biwgMCk7XG4gICAgICAgIH0gY2F0Y2goZSl7XG4gICAgICAgICAgICAvLyBzYW1lIGFzIGFib3ZlIGJ1dCB3aGVuIGl0J3MgYSB2ZXJzaW9uIG9mIEkuRS4gdGhhdCBtdXN0IGhhdmUgdGhlIGdsb2JhbCBvYmplY3QgZm9yICd0aGlzJywgaG9wZnVsbHkgb3VyIGNvbnRleHQgY29ycmVjdCBvdGhlcndpc2UgaXQgd2lsbCB0aHJvdyBhIGdsb2JhbCBlcnJvclxuICAgICAgICAgICAgcmV0dXJuIGNhY2hlZFNldFRpbWVvdXQuY2FsbCh0aGlzLCBmdW4sIDApO1xuICAgICAgICB9XG4gICAgfVxuXG5cbn1cbmZ1bmN0aW9uIHJ1bkNsZWFyVGltZW91dChtYXJrZXIpIHtcbiAgICBpZiAoY2FjaGVkQ2xlYXJUaW1lb3V0ID09PSBjbGVhclRpbWVvdXQpIHtcbiAgICAgICAgLy9ub3JtYWwgZW52aXJvbWVudHMgaW4gc2FuZSBzaXR1YXRpb25zXG4gICAgICAgIHJldHVybiBjbGVhclRpbWVvdXQobWFya2VyKTtcbiAgICB9XG4gICAgdHJ5IHtcbiAgICAgICAgLy8gd2hlbiB3aGVuIHNvbWVib2R5IGhhcyBzY3Jld2VkIHdpdGggc2V0VGltZW91dCBidXQgbm8gSS5FLiBtYWRkbmVzc1xuICAgICAgICByZXR1cm4gY2FjaGVkQ2xlYXJUaW1lb3V0KG1hcmtlcik7XG4gICAgfSBjYXRjaCAoZSl7XG4gICAgICAgIHRyeSB7XG4gICAgICAgICAgICAvLyBXaGVuIHdlIGFyZSBpbiBJLkUuIGJ1dCB0aGUgc2NyaXB0IGhhcyBiZWVuIGV2YWxlZCBzbyBJLkUuIGRvZXNuJ3QgIHRydXN0IHRoZSBnbG9iYWwgb2JqZWN0IHdoZW4gY2FsbGVkIG5vcm1hbGx5XG4gICAgICAgICAgICByZXR1cm4gY2FjaGVkQ2xlYXJUaW1lb3V0LmNhbGwobnVsbCwgbWFya2VyKTtcbiAgICAgICAgfSBjYXRjaCAoZSl7XG4gICAgICAgICAgICAvLyBzYW1lIGFzIGFib3ZlIGJ1dCB3aGVuIGl0J3MgYSB2ZXJzaW9uIG9mIEkuRS4gdGhhdCBtdXN0IGhhdmUgdGhlIGdsb2JhbCBvYmplY3QgZm9yICd0aGlzJywgaG9wZnVsbHkgb3VyIGNvbnRleHQgY29ycmVjdCBvdGhlcndpc2UgaXQgd2lsbCB0aHJvdyBhIGdsb2JhbCBlcnJvci5cbiAgICAgICAgICAgIC8vIFNvbWUgdmVyc2lvbnMgb2YgSS5FLiBoYXZlIGRpZmZlcmVudCBydWxlcyBmb3IgY2xlYXJUaW1lb3V0IHZzIHNldFRpbWVvdXRcbiAgICAgICAgICAgIHJldHVybiBjYWNoZWRDbGVhclRpbWVvdXQuY2FsbCh0aGlzLCBtYXJrZXIpO1xuICAgICAgICB9XG4gICAgfVxuXG5cblxufVxudmFyIHF1ZXVlID0gW107XG52YXIgZHJhaW5pbmcgPSBmYWxzZTtcbnZhciBjdXJyZW50UXVldWU7XG52YXIgcXVldWVJbmRleCA9IC0xO1xuXG5mdW5jdGlvbiBjbGVhblVwTmV4dFRpY2soKSB7XG4gICAgaWYgKCFkcmFpbmluZyB8fCAhY3VycmVudFF1ZXVlKSB7XG4gICAgICAgIHJldHVybjtcbiAgICB9XG4gICAgZHJhaW5pbmcgPSBmYWxzZTtcbiAgICBpZiAoY3VycmVudFF1ZXVlLmxlbmd0aCkge1xuICAgICAgICBxdWV1ZSA9IGN1cnJlbnRRdWV1ZS5jb25jYXQocXVldWUpO1xuICAgIH0gZWxzZSB7XG4gICAgICAgIHF1ZXVlSW5kZXggPSAtMTtcbiAgICB9XG4gICAgaWYgKHF1ZXVlLmxlbmd0aCkge1xuICAgICAgICBkcmFpblF1ZXVlKCk7XG4gICAgfVxufVxuXG5mdW5jdGlvbiBkcmFpblF1ZXVlKCkge1xuICAgIGlmIChkcmFpbmluZykge1xuICAgICAgICByZXR1cm47XG4gICAgfVxuICAgIHZhciB0aW1lb3V0ID0gcnVuVGltZW91dChjbGVhblVwTmV4dFRpY2spO1xuICAgIGRyYWluaW5nID0gdHJ1ZTtcblxuICAgIHZhciBsZW4gPSBxdWV1ZS5sZW5ndGg7XG4gICAgd2hpbGUobGVuKSB7XG4gICAgICAgIGN1cnJlbnRRdWV1ZSA9IHF1ZXVlO1xuICAgICAgICBxdWV1ZSA9IFtdO1xuICAgICAgICB3aGlsZSAoKytxdWV1ZUluZGV4IDwgbGVuKSB7XG4gICAgICAgICAgICBpZiAoY3VycmVudFF1ZXVlKSB7XG4gICAgICAgICAgICAgICAgY3VycmVudFF1ZXVlW3F1ZXVlSW5kZXhdLnJ1bigpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICAgIHF1ZXVlSW5kZXggPSAtMTtcbiAgICAgICAgbGVuID0gcXVldWUubGVuZ3RoO1xuICAgIH1cbiAgICBjdXJyZW50UXVldWUgPSBudWxsO1xuICAgIGRyYWluaW5nID0gZmFsc2U7XG4gICAgcnVuQ2xlYXJUaW1lb3V0KHRpbWVvdXQpO1xufVxuXG5wcm9jZXNzLm5leHRUaWNrID0gZnVuY3Rpb24gKGZ1bikge1xuICAgIHZhciBhcmdzID0gbmV3IEFycmF5KGFyZ3VtZW50cy5sZW5ndGggLSAxKTtcbiAgICBpZiAoYXJndW1lbnRzLmxlbmd0aCA+IDEpIHtcbiAgICAgICAgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgICAgIGFyZ3NbaSAtIDFdID0gYXJndW1lbnRzW2ldO1xuICAgICAgICB9XG4gICAgfVxuICAgIHF1ZXVlLnB1c2gobmV3IEl0ZW0oZnVuLCBhcmdzKSk7XG4gICAgaWYgKHF1ZXVlLmxlbmd0aCA9PT0gMSAmJiAhZHJhaW5pbmcpIHtcbiAgICAgICAgcnVuVGltZW91dChkcmFpblF1ZXVlKTtcbiAgICB9XG59O1xuXG4vLyB2OCBsaWtlcyBwcmVkaWN0aWJsZSBvYmplY3RzXG5mdW5jdGlvbiBJdGVtKGZ1biwgYXJyYXkpIHtcbiAgICB0aGlzLmZ1biA9IGZ1bjtcbiAgICB0aGlzLmFycmF5ID0gYXJyYXk7XG59XG5JdGVtLnByb3RvdHlwZS5ydW4gPSBmdW5jdGlvbiAoKSB7XG4gICAgdGhpcy5mdW4uYXBwbHkobnVsbCwgdGhpcy5hcnJheSk7XG59O1xucHJvY2Vzcy50aXRsZSA9ICdicm93c2VyJztcbnByb2Nlc3MuYnJvd3NlciA9IHRydWU7XG5wcm9jZXNzLmVudiA9IHt9O1xucHJvY2Vzcy5hcmd2ID0gW107XG5wcm9jZXNzLnZlcnNpb24gPSAnJzsgLy8gZW1wdHkgc3RyaW5nIHRvIGF2b2lkIHJlZ2V4cCBpc3N1ZXNcbnByb2Nlc3MudmVyc2lvbnMgPSB7fTtcblxuZnVuY3Rpb24gbm9vcCgpIHt9XG5cbnByb2Nlc3Mub24gPSBub29wO1xucHJvY2Vzcy5hZGRMaXN0ZW5lciA9IG5vb3A7XG5wcm9jZXNzLm9uY2UgPSBub29wO1xucHJvY2Vzcy5vZmYgPSBub29wO1xucHJvY2Vzcy5yZW1vdmVMaXN0ZW5lciA9IG5vb3A7XG5wcm9jZXNzLnJlbW92ZUFsbExpc3RlbmVycyA9IG5vb3A7XG5wcm9jZXNzLmVtaXQgPSBub29wO1xuXG5wcm9jZXNzLmJpbmRpbmcgPSBmdW5jdGlvbiAobmFtZSkge1xuICAgIHRocm93IG5ldyBFcnJvcigncHJvY2Vzcy5iaW5kaW5nIGlzIG5vdCBzdXBwb3J0ZWQnKTtcbn07XG5cbnByb2Nlc3MuY3dkID0gZnVuY3Rpb24gKCkgeyByZXR1cm4gJy8nIH07XG5wcm9jZXNzLmNoZGlyID0gZnVuY3Rpb24gKGRpcikge1xuICAgIHRocm93IG5ldyBFcnJvcigncHJvY2Vzcy5jaGRpciBpcyBub3Qgc3VwcG9ydGVkJyk7XG59O1xucHJvY2Vzcy51bWFzayA9IGZ1bmN0aW9uKCkgeyByZXR1cm4gMDsgfTtcbiIsIi8qIVxuICBDb3B5cmlnaHQgKGMpIDIwMTYgSmVkIFdhdHNvbi5cbiAgTGljZW5zZWQgdW5kZXIgdGhlIE1JVCBMaWNlbnNlIChNSVQpLCBzZWVcbiAgaHR0cDovL2plZHdhdHNvbi5naXRodWIuaW8vY2xhc3NuYW1lc1xuKi9cbi8qIGdsb2JhbCBkZWZpbmUgKi9cblxuKGZ1bmN0aW9uICgpIHtcblx0J3VzZSBzdHJpY3QnO1xuXG5cdHZhciBoYXNPd24gPSB7fS5oYXNPd25Qcm9wZXJ0eTtcblxuXHRmdW5jdGlvbiBjbGFzc05hbWVzICgpIHtcblx0XHR2YXIgY2xhc3NlcyA9IFtdO1xuXG5cdFx0Zm9yICh2YXIgaSA9IDA7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcblx0XHRcdHZhciBhcmcgPSBhcmd1bWVudHNbaV07XG5cdFx0XHRpZiAoIWFyZykgY29udGludWU7XG5cblx0XHRcdHZhciBhcmdUeXBlID0gdHlwZW9mIGFyZztcblxuXHRcdFx0aWYgKGFyZ1R5cGUgPT09ICdzdHJpbmcnIHx8IGFyZ1R5cGUgPT09ICdudW1iZXInKSB7XG5cdFx0XHRcdGNsYXNzZXMucHVzaChhcmcpO1xuXHRcdFx0fSBlbHNlIGlmIChBcnJheS5pc0FycmF5KGFyZykpIHtcblx0XHRcdFx0Y2xhc3Nlcy5wdXNoKGNsYXNzTmFtZXMuYXBwbHkobnVsbCwgYXJnKSk7XG5cdFx0XHR9IGVsc2UgaWYgKGFyZ1R5cGUgPT09ICdvYmplY3QnKSB7XG5cdFx0XHRcdGZvciAodmFyIGtleSBpbiBhcmcpIHtcblx0XHRcdFx0XHRpZiAoaGFzT3duLmNhbGwoYXJnLCBrZXkpICYmIGFyZ1trZXldKSB7XG5cdFx0XHRcdFx0XHRjbGFzc2VzLnB1c2goa2V5KTtcblx0XHRcdFx0XHR9XG5cdFx0XHRcdH1cblx0XHRcdH1cblx0XHR9XG5cblx0XHRyZXR1cm4gY2xhc3Nlcy5qb2luKCcgJyk7XG5cdH1cblxuXHRpZiAodHlwZW9mIG1vZHVsZSAhPT0gJ3VuZGVmaW5lZCcgJiYgbW9kdWxlLmV4cG9ydHMpIHtcblx0XHRtb2R1bGUuZXhwb3J0cyA9IGNsYXNzTmFtZXM7XG5cdH0gZWxzZSBpZiAodHlwZW9mIGRlZmluZSA9PT0gJ2Z1bmN0aW9uJyAmJiB0eXBlb2YgZGVmaW5lLmFtZCA9PT0gJ29iamVjdCcgJiYgZGVmaW5lLmFtZCkge1xuXHRcdC8vIHJlZ2lzdGVyIGFzICdjbGFzc25hbWVzJywgY29uc2lzdGVudCB3aXRoIG5wbSBwYWNrYWdlIG5hbWVcblx0XHRkZWZpbmUoJ2NsYXNzbmFtZXMnLCBbXSwgZnVuY3Rpb24gKCkge1xuXHRcdFx0cmV0dXJuIGNsYXNzTmFtZXM7XG5cdFx0fSk7XG5cdH0gZWxzZSB7XG5cdFx0d2luZG93LmNsYXNzTmFtZXMgPSBjbGFzc05hbWVzO1xuXHR9XG59KCkpO1xuIiwidmFyIHBTbGljZSA9IEFycmF5LnByb3RvdHlwZS5zbGljZTtcbnZhciBvYmplY3RLZXlzID0gcmVxdWlyZSgnLi9saWIva2V5cy5qcycpO1xudmFyIGlzQXJndW1lbnRzID0gcmVxdWlyZSgnLi9saWIvaXNfYXJndW1lbnRzLmpzJyk7XG5cbnZhciBkZWVwRXF1YWwgPSBtb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uIChhY3R1YWwsIGV4cGVjdGVkLCBvcHRzKSB7XG4gIGlmICghb3B0cykgb3B0cyA9IHt9O1xuICAvLyA3LjEuIEFsbCBpZGVudGljYWwgdmFsdWVzIGFyZSBlcXVpdmFsZW50LCBhcyBkZXRlcm1pbmVkIGJ5ID09PS5cbiAgaWYgKGFjdHVhbCA9PT0gZXhwZWN0ZWQpIHtcbiAgICByZXR1cm4gdHJ1ZTtcblxuICB9IGVsc2UgaWYgKGFjdHVhbCBpbnN0YW5jZW9mIERhdGUgJiYgZXhwZWN0ZWQgaW5zdGFuY2VvZiBEYXRlKSB7XG4gICAgcmV0dXJuIGFjdHVhbC5nZXRUaW1lKCkgPT09IGV4cGVjdGVkLmdldFRpbWUoKTtcblxuICAvLyA3LjMuIE90aGVyIHBhaXJzIHRoYXQgZG8gbm90IGJvdGggcGFzcyB0eXBlb2YgdmFsdWUgPT0gJ29iamVjdCcsXG4gIC8vIGVxdWl2YWxlbmNlIGlzIGRldGVybWluZWQgYnkgPT0uXG4gIH0gZWxzZSBpZiAoIWFjdHVhbCB8fCAhZXhwZWN0ZWQgfHwgdHlwZW9mIGFjdHVhbCAhPSAnb2JqZWN0JyAmJiB0eXBlb2YgZXhwZWN0ZWQgIT0gJ29iamVjdCcpIHtcbiAgICByZXR1cm4gb3B0cy5zdHJpY3QgPyBhY3R1YWwgPT09IGV4cGVjdGVkIDogYWN0dWFsID09IGV4cGVjdGVkO1xuXG4gIC8vIDcuNC4gRm9yIGFsbCBvdGhlciBPYmplY3QgcGFpcnMsIGluY2x1ZGluZyBBcnJheSBvYmplY3RzLCBlcXVpdmFsZW5jZSBpc1xuICAvLyBkZXRlcm1pbmVkIGJ5IGhhdmluZyB0aGUgc2FtZSBudW1iZXIgb2Ygb3duZWQgcHJvcGVydGllcyAoYXMgdmVyaWZpZWRcbiAgLy8gd2l0aCBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwpLCB0aGUgc2FtZSBzZXQgb2Yga2V5c1xuICAvLyAoYWx0aG91Z2ggbm90IG5lY2Vzc2FyaWx5IHRoZSBzYW1lIG9yZGVyKSwgZXF1aXZhbGVudCB2YWx1ZXMgZm9yIGV2ZXJ5XG4gIC8vIGNvcnJlc3BvbmRpbmcga2V5LCBhbmQgYW4gaWRlbnRpY2FsICdwcm90b3R5cGUnIHByb3BlcnR5LiBOb3RlOiB0aGlzXG4gIC8vIGFjY291bnRzIGZvciBib3RoIG5hbWVkIGFuZCBpbmRleGVkIHByb3BlcnRpZXMgb24gQXJyYXlzLlxuICB9IGVsc2Uge1xuICAgIHJldHVybiBvYmpFcXVpdihhY3R1YWwsIGV4cGVjdGVkLCBvcHRzKTtcbiAgfVxufVxuXG5mdW5jdGlvbiBpc1VuZGVmaW5lZE9yTnVsbCh2YWx1ZSkge1xuICByZXR1cm4gdmFsdWUgPT09IG51bGwgfHwgdmFsdWUgPT09IHVuZGVmaW5lZDtcbn1cblxuZnVuY3Rpb24gaXNCdWZmZXIgKHgpIHtcbiAgaWYgKCF4IHx8IHR5cGVvZiB4ICE9PSAnb2JqZWN0JyB8fCB0eXBlb2YgeC5sZW5ndGggIT09ICdudW1iZXInKSByZXR1cm4gZmFsc2U7XG4gIGlmICh0eXBlb2YgeC5jb3B5ICE9PSAnZnVuY3Rpb24nIHx8IHR5cGVvZiB4LnNsaWNlICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgcmV0dXJuIGZhbHNlO1xuICB9XG4gIGlmICh4Lmxlbmd0aCA+IDAgJiYgdHlwZW9mIHhbMF0gIT09ICdudW1iZXInKSByZXR1cm4gZmFsc2U7XG4gIHJldHVybiB0cnVlO1xufVxuXG5mdW5jdGlvbiBvYmpFcXVpdihhLCBiLCBvcHRzKSB7XG4gIHZhciBpLCBrZXk7XG4gIGlmIChpc1VuZGVmaW5lZE9yTnVsbChhKSB8fCBpc1VuZGVmaW5lZE9yTnVsbChiKSlcbiAgICByZXR1cm4gZmFsc2U7XG4gIC8vIGFuIGlkZW50aWNhbCAncHJvdG90eXBlJyBwcm9wZXJ0eS5cbiAgaWYgKGEucHJvdG90eXBlICE9PSBiLnByb3RvdHlwZSkgcmV0dXJuIGZhbHNlO1xuICAvL35+fkkndmUgbWFuYWdlZCB0byBicmVhayBPYmplY3Qua2V5cyB0aHJvdWdoIHNjcmV3eSBhcmd1bWVudHMgcGFzc2luZy5cbiAgLy8gICBDb252ZXJ0aW5nIHRvIGFycmF5IHNvbHZlcyB0aGUgcHJvYmxlbS5cbiAgaWYgKGlzQXJndW1lbnRzKGEpKSB7XG4gICAgaWYgKCFpc0FyZ3VtZW50cyhiKSkge1xuICAgICAgcmV0dXJuIGZhbHNlO1xuICAgIH1cbiAgICBhID0gcFNsaWNlLmNhbGwoYSk7XG4gICAgYiA9IHBTbGljZS5jYWxsKGIpO1xuICAgIHJldHVybiBkZWVwRXF1YWwoYSwgYiwgb3B0cyk7XG4gIH1cbiAgaWYgKGlzQnVmZmVyKGEpKSB7XG4gICAgaWYgKCFpc0J1ZmZlcihiKSkge1xuICAgICAgcmV0dXJuIGZhbHNlO1xuICAgIH1cbiAgICBpZiAoYS5sZW5ndGggIT09IGIubGVuZ3RoKSByZXR1cm4gZmFsc2U7XG4gICAgZm9yIChpID0gMDsgaSA8IGEubGVuZ3RoOyBpKyspIHtcbiAgICAgIGlmIChhW2ldICE9PSBiW2ldKSByZXR1cm4gZmFsc2U7XG4gICAgfVxuICAgIHJldHVybiB0cnVlO1xuICB9XG4gIHRyeSB7XG4gICAgdmFyIGthID0gb2JqZWN0S2V5cyhhKSxcbiAgICAgICAga2IgPSBvYmplY3RLZXlzKGIpO1xuICB9IGNhdGNoIChlKSB7Ly9oYXBwZW5zIHdoZW4gb25lIGlzIGEgc3RyaW5nIGxpdGVyYWwgYW5kIHRoZSBvdGhlciBpc24ndFxuICAgIHJldHVybiBmYWxzZTtcbiAgfVxuICAvLyBoYXZpbmcgdGhlIHNhbWUgbnVtYmVyIG9mIG93bmVkIHByb3BlcnRpZXMgKGtleXMgaW5jb3Jwb3JhdGVzXG4gIC8vIGhhc093blByb3BlcnR5KVxuICBpZiAoa2EubGVuZ3RoICE9IGtiLmxlbmd0aClcbiAgICByZXR1cm4gZmFsc2U7XG4gIC8vdGhlIHNhbWUgc2V0IG9mIGtleXMgKGFsdGhvdWdoIG5vdCBuZWNlc3NhcmlseSB0aGUgc2FtZSBvcmRlciksXG4gIGthLnNvcnQoKTtcbiAga2Iuc29ydCgpO1xuICAvL35+fmNoZWFwIGtleSB0ZXN0XG4gIGZvciAoaSA9IGthLmxlbmd0aCAtIDE7IGkgPj0gMDsgaS0tKSB7XG4gICAgaWYgKGthW2ldICE9IGtiW2ldKVxuICAgICAgcmV0dXJuIGZhbHNlO1xuICB9XG4gIC8vZXF1aXZhbGVudCB2YWx1ZXMgZm9yIGV2ZXJ5IGNvcnJlc3BvbmRpbmcga2V5LCBhbmRcbiAgLy9+fn5wb3NzaWJseSBleHBlbnNpdmUgZGVlcCB0ZXN0XG4gIGZvciAoaSA9IGthLmxlbmd0aCAtIDE7IGkgPj0gMDsgaS0tKSB7XG4gICAga2V5ID0ga2FbaV07XG4gICAgaWYgKCFkZWVwRXF1YWwoYVtrZXldLCBiW2tleV0sIG9wdHMpKSByZXR1cm4gZmFsc2U7XG4gIH1cbiAgcmV0dXJuIHR5cGVvZiBhID09PSB0eXBlb2YgYjtcbn1cbiIsInZhciBzdXBwb3J0c0FyZ3VtZW50c0NsYXNzID0gKGZ1bmN0aW9uKCl7XG4gIHJldHVybiBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwoYXJndW1lbnRzKVxufSkoKSA9PSAnW29iamVjdCBBcmd1bWVudHNdJztcblxuZXhwb3J0cyA9IG1vZHVsZS5leHBvcnRzID0gc3VwcG9ydHNBcmd1bWVudHNDbGFzcyA/IHN1cHBvcnRlZCA6IHVuc3VwcG9ydGVkO1xuXG5leHBvcnRzLnN1cHBvcnRlZCA9IHN1cHBvcnRlZDtcbmZ1bmN0aW9uIHN1cHBvcnRlZChvYmplY3QpIHtcbiAgcmV0dXJuIE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbChvYmplY3QpID09ICdbb2JqZWN0IEFyZ3VtZW50c10nO1xufTtcblxuZXhwb3J0cy51bnN1cHBvcnRlZCA9IHVuc3VwcG9ydGVkO1xuZnVuY3Rpb24gdW5zdXBwb3J0ZWQob2JqZWN0KXtcbiAgcmV0dXJuIG9iamVjdCAmJlxuICAgIHR5cGVvZiBvYmplY3QgPT0gJ29iamVjdCcgJiZcbiAgICB0eXBlb2Ygb2JqZWN0Lmxlbmd0aCA9PSAnbnVtYmVyJyAmJlxuICAgIE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmplY3QsICdjYWxsZWUnKSAmJlxuICAgICFPYmplY3QucHJvdG90eXBlLnByb3BlcnR5SXNFbnVtZXJhYmxlLmNhbGwob2JqZWN0LCAnY2FsbGVlJykgfHxcbiAgICBmYWxzZTtcbn07XG4iLCJleHBvcnRzID0gbW9kdWxlLmV4cG9ydHMgPSB0eXBlb2YgT2JqZWN0LmtleXMgPT09ICdmdW5jdGlvbidcbiAgPyBPYmplY3Qua2V5cyA6IHNoaW07XG5cbmV4cG9ydHMuc2hpbSA9IHNoaW07XG5mdW5jdGlvbiBzaGltIChvYmopIHtcbiAgdmFyIGtleXMgPSBbXTtcbiAgZm9yICh2YXIga2V5IGluIG9iaikga2V5cy5wdXNoKGtleSk7XG4gIHJldHVybiBrZXlzO1xufVxuIiwidmFyIGlzRnVuY3Rpb24gPSByZXF1aXJlKCdpcy1mdW5jdGlvbicpXG5cbm1vZHVsZS5leHBvcnRzID0gZm9yRWFjaFxuXG52YXIgdG9TdHJpbmcgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nXG52YXIgaGFzT3duUHJvcGVydHkgPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5XG5cbmZ1bmN0aW9uIGZvckVhY2gobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBpZiAoIWlzRnVuY3Rpb24oaXRlcmF0b3IpKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ2l0ZXJhdG9yIG11c3QgYmUgYSBmdW5jdGlvbicpXG4gICAgfVxuXG4gICAgaWYgKGFyZ3VtZW50cy5sZW5ndGggPCAzKSB7XG4gICAgICAgIGNvbnRleHQgPSB0aGlzXG4gICAgfVxuICAgIFxuICAgIGlmICh0b1N0cmluZy5jYWxsKGxpc3QpID09PSAnW29iamVjdCBBcnJheV0nKVxuICAgICAgICBmb3JFYWNoQXJyYXkobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpXG4gICAgZWxzZSBpZiAodHlwZW9mIGxpc3QgPT09ICdzdHJpbmcnKVxuICAgICAgICBmb3JFYWNoU3RyaW5nKGxpc3QsIGl0ZXJhdG9yLCBjb250ZXh0KVxuICAgIGVsc2VcbiAgICAgICAgZm9yRWFjaE9iamVjdChsaXN0LCBpdGVyYXRvciwgY29udGV4dClcbn1cblxuZnVuY3Rpb24gZm9yRWFjaEFycmF5KGFycmF5LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBhcnJheS5sZW5ndGg7IGkgPCBsZW47IGkrKykge1xuICAgICAgICBpZiAoaGFzT3duUHJvcGVydHkuY2FsbChhcnJheSwgaSkpIHtcbiAgICAgICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgYXJyYXlbaV0sIGksIGFycmF5KVxuICAgICAgICB9XG4gICAgfVxufVxuXG5mdW5jdGlvbiBmb3JFYWNoU3RyaW5nKHN0cmluZywgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gc3RyaW5nLmxlbmd0aDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgIC8vIG5vIHN1Y2ggdGhpbmcgYXMgYSBzcGFyc2Ugc3RyaW5nLlxuICAgICAgICBpdGVyYXRvci5jYWxsKGNvbnRleHQsIHN0cmluZy5jaGFyQXQoaSksIGksIHN0cmluZylcbiAgICB9XG59XG5cbmZ1bmN0aW9uIGZvckVhY2hPYmplY3Qob2JqZWN0LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGsgaW4gb2JqZWN0KSB7XG4gICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKG9iamVjdCwgaykpIHtcbiAgICAgICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgb2JqZWN0W2tdLCBrLCBvYmplY3QpXG4gICAgICAgIH1cbiAgICB9XG59XG4iLCJpZiAodHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgIG1vZHVsZS5leHBvcnRzID0gd2luZG93O1xufSBlbHNlIGlmICh0eXBlb2YgZ2xvYmFsICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSBnbG9iYWw7XG59IGVsc2UgaWYgKHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiKXtcbiAgICBtb2R1bGUuZXhwb3J0cyA9IHNlbGY7XG59IGVsc2Uge1xuICAgIG1vZHVsZS5leHBvcnRzID0ge307XG59XG4iLCIvKipcbiAqIEluZGljYXRlcyB0aGF0IG5hdmlnYXRpb24gd2FzIGNhdXNlZCBieSBhIGNhbGwgdG8gaGlzdG9yeS5wdXNoLlxuICovXG4ndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG52YXIgUFVTSCA9ICdQVVNIJztcblxuZXhwb3J0cy5QVVNIID0gUFVTSDtcbi8qKlxuICogSW5kaWNhdGVzIHRoYXQgbmF2aWdhdGlvbiB3YXMgY2F1c2VkIGJ5IGEgY2FsbCB0byBoaXN0b3J5LnJlcGxhY2UuXG4gKi9cbnZhciBSRVBMQUNFID0gJ1JFUExBQ0UnO1xuXG5leHBvcnRzLlJFUExBQ0UgPSBSRVBMQUNFO1xuLyoqXG4gKiBJbmRpY2F0ZXMgdGhhdCBuYXZpZ2F0aW9uIHdhcyBjYXVzZWQgYnkgc29tZSBvdGhlciBhY3Rpb24gc3VjaFxuICogYXMgdXNpbmcgYSBicm93c2VyJ3MgYmFjay9mb3J3YXJkIGJ1dHRvbnMgYW5kL29yIG1hbnVhbGx5IG1hbmlwdWxhdGluZ1xuICogdGhlIFVSTCBpbiBhIGJyb3dzZXIncyBsb2NhdGlvbiBiYXIuIFRoaXMgaXMgdGhlIGRlZmF1bHQuXG4gKlxuICogU2VlIGh0dHBzOi8vZGV2ZWxvcGVyLm1vemlsbGEub3JnL2VuLVVTL2RvY3MvV2ViL0FQSS9XaW5kb3dFdmVudEhhbmRsZXJzL29ucG9wc3RhdGVcbiAqIGZvciBtb3JlIGluZm9ybWF0aW9uLlxuICovXG52YXIgUE9QID0gJ1BPUCc7XG5cbmV4cG9ydHMuUE9QID0gUE9QO1xuZXhwb3J0c1snZGVmYXVsdCddID0ge1xuICBQVVNIOiBQVVNILFxuICBSRVBMQUNFOiBSRVBMQUNFLFxuICBQT1A6IFBPUFxufTsiLCJcInVzZSBzdHJpY3RcIjtcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbnZhciBfc2xpY2UgPSBBcnJheS5wcm90b3R5cGUuc2xpY2U7XG5leHBvcnRzLmxvb3BBc3luYyA9IGxvb3BBc3luYztcblxuZnVuY3Rpb24gbG9vcEFzeW5jKHR1cm5zLCB3b3JrLCBjYWxsYmFjaykge1xuICB2YXIgY3VycmVudFR1cm4gPSAwLFxuICAgICAgaXNEb25lID0gZmFsc2U7XG4gIHZhciBzeW5jID0gZmFsc2UsXG4gICAgICBoYXNOZXh0ID0gZmFsc2UsXG4gICAgICBkb25lQXJncyA9IHVuZGVmaW5lZDtcblxuICBmdW5jdGlvbiBkb25lKCkge1xuICAgIGlzRG9uZSA9IHRydWU7XG4gICAgaWYgKHN5bmMpIHtcbiAgICAgIC8vIEl0ZXJhdGUgaW5zdGVhZCBvZiByZWN1cnNpbmcgaWYgcG9zc2libGUuXG4gICAgICBkb25lQXJncyA9IFtdLmNvbmNhdChfc2xpY2UuY2FsbChhcmd1bWVudHMpKTtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBjYWxsYmFjay5hcHBseSh0aGlzLCBhcmd1bWVudHMpO1xuICB9XG5cbiAgZnVuY3Rpb24gbmV4dCgpIHtcbiAgICBpZiAoaXNEb25lKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgaGFzTmV4dCA9IHRydWU7XG4gICAgaWYgKHN5bmMpIHtcbiAgICAgIC8vIEl0ZXJhdGUgaW5zdGVhZCBvZiByZWN1cnNpbmcgaWYgcG9zc2libGUuXG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgc3luYyA9IHRydWU7XG5cbiAgICB3aGlsZSAoIWlzRG9uZSAmJiBjdXJyZW50VHVybiA8IHR1cm5zICYmIGhhc05leHQpIHtcbiAgICAgIGhhc05leHQgPSBmYWxzZTtcbiAgICAgIHdvcmsuY2FsbCh0aGlzLCBjdXJyZW50VHVybisrLCBuZXh0LCBkb25lKTtcbiAgICB9XG5cbiAgICBzeW5jID0gZmFsc2U7XG5cbiAgICBpZiAoaXNEb25lKSB7XG4gICAgICAvLyBUaGlzIG1lYW5zIHRoZSBsb29wIGZpbmlzaGVkIHN5bmNocm9ub3VzbHkuXG4gICAgICBjYWxsYmFjay5hcHBseSh0aGlzLCBkb25lQXJncyk7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgaWYgKGN1cnJlbnRUdXJuID49IHR1cm5zICYmIGhhc05leHQpIHtcbiAgICAgIGlzRG9uZSA9IHRydWU7XG4gICAgICBjYWxsYmFjaygpO1xuICAgIH1cbiAgfVxuXG4gIG5leHQoKTtcbn0iLCIvKmVzbGludC1kaXNhYmxlIG5vLWVtcHR5ICovXG4ndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLnNhdmVTdGF0ZSA9IHNhdmVTdGF0ZTtcbmV4cG9ydHMucmVhZFN0YXRlID0gcmVhZFN0YXRlO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG52YXIgS2V5UHJlZml4ID0gJ0BASGlzdG9yeS8nO1xudmFyIFF1b3RhRXhjZWVkZWRFcnJvcnMgPSBbJ1F1b3RhRXhjZWVkZWRFcnJvcicsICdRVU9UQV9FWENFRURFRF9FUlInXTtcblxudmFyIFNlY3VyaXR5RXJyb3IgPSAnU2VjdXJpdHlFcnJvcic7XG5cbmZ1bmN0aW9uIGNyZWF0ZUtleShrZXkpIHtcbiAgcmV0dXJuIEtleVByZWZpeCArIGtleTtcbn1cblxuZnVuY3Rpb24gc2F2ZVN0YXRlKGtleSwgc3RhdGUpIHtcbiAgdHJ5IHtcbiAgICBpZiAoc3RhdGUgPT0gbnVsbCkge1xuICAgICAgd2luZG93LnNlc3Npb25TdG9yYWdlLnJlbW92ZUl0ZW0oY3JlYXRlS2V5KGtleSkpO1xuICAgIH0gZWxzZSB7XG4gICAgICB3aW5kb3cuc2Vzc2lvblN0b3JhZ2Uuc2V0SXRlbShjcmVhdGVLZXkoa2V5KSwgSlNPTi5zdHJpbmdpZnkoc3RhdGUpKTtcbiAgICB9XG4gIH0gY2F0Y2ggKGVycm9yKSB7XG4gICAgaWYgKGVycm9yLm5hbWUgPT09IFNlY3VyaXR5RXJyb3IpIHtcbiAgICAgIC8vIEJsb2NraW5nIGNvb2tpZXMgaW4gQ2hyb21lL0ZpcmVmb3gvU2FmYXJpIHRocm93cyBTZWN1cml0eUVycm9yIG9uIGFueVxuICAgICAgLy8gYXR0ZW1wdCB0byBhY2Nlc3Mgd2luZG93LnNlc3Npb25TdG9yYWdlLlxuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnW2hpc3RvcnldIFVuYWJsZSB0byBzYXZlIHN0YXRlOyBzZXNzaW9uU3RvcmFnZSBpcyBub3QgYXZhaWxhYmxlIGR1ZSB0byBzZWN1cml0eSBzZXR0aW5ncycpIDogdW5kZWZpbmVkO1xuXG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgaWYgKFF1b3RhRXhjZWVkZWRFcnJvcnMuaW5kZXhPZihlcnJvci5uYW1lKSA+PSAwICYmIHdpbmRvdy5zZXNzaW9uU3RvcmFnZS5sZW5ndGggPT09IDApIHtcbiAgICAgIC8vIFNhZmFyaSBcInByaXZhdGUgbW9kZVwiIHRocm93cyBRdW90YUV4Y2VlZGVkRXJyb3IuXG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdbaGlzdG9yeV0gVW5hYmxlIHRvIHNhdmUgc3RhdGU7IHNlc3Npb25TdG9yYWdlIGlzIG5vdCBhdmFpbGFibGUgaW4gU2FmYXJpIHByaXZhdGUgbW9kZScpIDogdW5kZWZpbmVkO1xuXG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgdGhyb3cgZXJyb3I7XG4gIH1cbn1cblxuZnVuY3Rpb24gcmVhZFN0YXRlKGtleSkge1xuICB2YXIganNvbiA9IHVuZGVmaW5lZDtcbiAgdHJ5IHtcbiAgICBqc29uID0gd2luZG93LnNlc3Npb25TdG9yYWdlLmdldEl0ZW0oY3JlYXRlS2V5KGtleSkpO1xuICB9IGNhdGNoIChlcnJvcikge1xuICAgIGlmIChlcnJvci5uYW1lID09PSBTZWN1cml0eUVycm9yKSB7XG4gICAgICAvLyBCbG9ja2luZyBjb29raWVzIGluIENocm9tZS9GaXJlZm94L1NhZmFyaSB0aHJvd3MgU2VjdXJpdHlFcnJvciBvbiBhbnlcbiAgICAgIC8vIGF0dGVtcHQgdG8gYWNjZXNzIHdpbmRvdy5zZXNzaW9uU3RvcmFnZS5cbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ1toaXN0b3J5XSBVbmFibGUgdG8gcmVhZCBzdGF0ZTsgc2Vzc2lvblN0b3JhZ2UgaXMgbm90IGF2YWlsYWJsZSBkdWUgdG8gc2VjdXJpdHkgc2V0dGluZ3MnKSA6IHVuZGVmaW5lZDtcblxuICAgICAgcmV0dXJuIG51bGw7XG4gICAgfVxuICB9XG5cbiAgaWYgKGpzb24pIHtcbiAgICB0cnkge1xuICAgICAgcmV0dXJuIEpTT04ucGFyc2UoanNvbik7XG4gICAgfSBjYXRjaCAoZXJyb3IpIHtcbiAgICAgIC8vIElnbm9yZSBpbnZhbGlkIEpTT04uXG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIG51bGw7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5hZGRFdmVudExpc3RlbmVyID0gYWRkRXZlbnRMaXN0ZW5lcjtcbmV4cG9ydHMucmVtb3ZlRXZlbnRMaXN0ZW5lciA9IHJlbW92ZUV2ZW50TGlzdGVuZXI7XG5leHBvcnRzLmdldEhhc2hQYXRoID0gZ2V0SGFzaFBhdGg7XG5leHBvcnRzLnJlcGxhY2VIYXNoUGF0aCA9IHJlcGxhY2VIYXNoUGF0aDtcbmV4cG9ydHMuZ2V0V2luZG93UGF0aCA9IGdldFdpbmRvd1BhdGg7XG5leHBvcnRzLmdvID0gZ287XG5leHBvcnRzLmdldFVzZXJDb25maXJtYXRpb24gPSBnZXRVc2VyQ29uZmlybWF0aW9uO1xuZXhwb3J0cy5zdXBwb3J0c0hpc3RvcnkgPSBzdXBwb3J0c0hpc3Rvcnk7XG5leHBvcnRzLnN1cHBvcnRzR29XaXRob3V0UmVsb2FkVXNpbmdIYXNoID0gc3VwcG9ydHNHb1dpdGhvdXRSZWxvYWRVc2luZ0hhc2g7XG5cbmZ1bmN0aW9uIGFkZEV2ZW50TGlzdGVuZXIobm9kZSwgZXZlbnQsIGxpc3RlbmVyKSB7XG4gIGlmIChub2RlLmFkZEV2ZW50TGlzdGVuZXIpIHtcbiAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIoZXZlbnQsIGxpc3RlbmVyLCBmYWxzZSk7XG4gIH0gZWxzZSB7XG4gICAgbm9kZS5hdHRhY2hFdmVudCgnb24nICsgZXZlbnQsIGxpc3RlbmVyKTtcbiAgfVxufVxuXG5mdW5jdGlvbiByZW1vdmVFdmVudExpc3RlbmVyKG5vZGUsIGV2ZW50LCBsaXN0ZW5lcikge1xuICBpZiAobm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKSB7XG4gICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKGV2ZW50LCBsaXN0ZW5lciwgZmFsc2UpO1xuICB9IGVsc2Uge1xuICAgIG5vZGUuZGV0YWNoRXZlbnQoJ29uJyArIGV2ZW50LCBsaXN0ZW5lcik7XG4gIH1cbn1cblxuZnVuY3Rpb24gZ2V0SGFzaFBhdGgoKSB7XG4gIC8vIFdlIGNhbid0IHVzZSB3aW5kb3cubG9jYXRpb24uaGFzaCBoZXJlIGJlY2F1c2UgaXQncyBub3RcbiAgLy8gY29uc2lzdGVudCBhY3Jvc3MgYnJvd3NlcnMgLSBGaXJlZm94IHdpbGwgcHJlLWRlY29kZSBpdCFcbiAgcmV0dXJuIHdpbmRvdy5sb2NhdGlvbi5ocmVmLnNwbGl0KCcjJylbMV0gfHwgJyc7XG59XG5cbmZ1bmN0aW9uIHJlcGxhY2VIYXNoUGF0aChwYXRoKSB7XG4gIHdpbmRvdy5sb2NhdGlvbi5yZXBsYWNlKHdpbmRvdy5sb2NhdGlvbi5wYXRobmFtZSArIHdpbmRvdy5sb2NhdGlvbi5zZWFyY2ggKyAnIycgKyBwYXRoKTtcbn1cblxuZnVuY3Rpb24gZ2V0V2luZG93UGF0aCgpIHtcbiAgcmV0dXJuIHdpbmRvdy5sb2NhdGlvbi5wYXRobmFtZSArIHdpbmRvdy5sb2NhdGlvbi5zZWFyY2ggKyB3aW5kb3cubG9jYXRpb24uaGFzaDtcbn1cblxuZnVuY3Rpb24gZ28obikge1xuICBpZiAobikgd2luZG93Lmhpc3RvcnkuZ28obik7XG59XG5cbmZ1bmN0aW9uIGdldFVzZXJDb25maXJtYXRpb24obWVzc2FnZSwgY2FsbGJhY2spIHtcbiAgY2FsbGJhY2sod2luZG93LmNvbmZpcm0obWVzc2FnZSkpO1xufVxuXG4vKipcbiAqIFJldHVybnMgdHJ1ZSBpZiB0aGUgSFRNTDUgaGlzdG9yeSBBUEkgaXMgc3VwcG9ydGVkLiBUYWtlbiBmcm9tIE1vZGVybml6ci5cbiAqXG4gKiBodHRwczovL2dpdGh1Yi5jb20vTW9kZXJuaXpyL01vZGVybml6ci9ibG9iL21hc3Rlci9MSUNFTlNFXG4gKiBodHRwczovL2dpdGh1Yi5jb20vTW9kZXJuaXpyL01vZGVybml6ci9ibG9iL21hc3Rlci9mZWF0dXJlLWRldGVjdHMvaGlzdG9yeS5qc1xuICogY2hhbmdlZCB0byBhdm9pZCBmYWxzZSBuZWdhdGl2ZXMgZm9yIFdpbmRvd3MgUGhvbmVzOiBodHRwczovL2dpdGh1Yi5jb20vcmFja3QvcmVhY3Qtcm91dGVyL2lzc3Vlcy81ODZcbiAqL1xuXG5mdW5jdGlvbiBzdXBwb3J0c0hpc3RvcnkoKSB7XG4gIHZhciB1YSA9IG5hdmlnYXRvci51c2VyQWdlbnQ7XG4gIGlmICgodWEuaW5kZXhPZignQW5kcm9pZCAyLicpICE9PSAtMSB8fCB1YS5pbmRleE9mKCdBbmRyb2lkIDQuMCcpICE9PSAtMSkgJiYgdWEuaW5kZXhPZignTW9iaWxlIFNhZmFyaScpICE9PSAtMSAmJiB1YS5pbmRleE9mKCdDaHJvbWUnKSA9PT0gLTEgJiYgdWEuaW5kZXhPZignV2luZG93cyBQaG9uZScpID09PSAtMSkge1xuICAgIHJldHVybiBmYWxzZTtcbiAgfVxuICByZXR1cm4gd2luZG93Lmhpc3RvcnkgJiYgJ3B1c2hTdGF0ZScgaW4gd2luZG93Lmhpc3Rvcnk7XG59XG5cbi8qKlxuICogUmV0dXJucyBmYWxzZSBpZiB1c2luZyBnbyhuKSB3aXRoIGhhc2ggaGlzdG9yeSBjYXVzZXMgYSBmdWxsIHBhZ2UgcmVsb2FkLlxuICovXG5cbmZ1bmN0aW9uIHN1cHBvcnRzR29XaXRob3V0UmVsb2FkVXNpbmdIYXNoKCkge1xuICB2YXIgdWEgPSBuYXZpZ2F0b3IudXNlckFnZW50O1xuICByZXR1cm4gdWEuaW5kZXhPZignRmlyZWZveCcpID09PSAtMTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG52YXIgY2FuVXNlRE9NID0gISEodHlwZW9mIHdpbmRvdyAhPT0gJ3VuZGVmaW5lZCcgJiYgd2luZG93LmRvY3VtZW50ICYmIHdpbmRvdy5kb2N1bWVudC5jcmVhdGVFbGVtZW50KTtcbmV4cG9ydHMuY2FuVXNlRE9NID0gY2FuVXNlRE9NOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuZXh0cmFjdFBhdGggPSBleHRyYWN0UGF0aDtcbmV4cG9ydHMucGFyc2VQYXRoID0gcGFyc2VQYXRoO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBleHRyYWN0UGF0aChzdHJpbmcpIHtcbiAgdmFyIG1hdGNoID0gc3RyaW5nLm1hdGNoKC9eaHR0cHM/OlxcL1xcL1teXFwvXSovKTtcblxuICBpZiAobWF0Y2ggPT0gbnVsbCkgcmV0dXJuIHN0cmluZztcblxuICByZXR1cm4gc3RyaW5nLnN1YnN0cmluZyhtYXRjaFswXS5sZW5ndGgpO1xufVxuXG5mdW5jdGlvbiBwYXJzZVBhdGgocGF0aCkge1xuICB2YXIgcGF0aG5hbWUgPSBleHRyYWN0UGF0aChwYXRoKTtcbiAgdmFyIHNlYXJjaCA9ICcnO1xuICB2YXIgaGFzaCA9ICcnO1xuXG4gIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShwYXRoID09PSBwYXRobmFtZSwgJ0EgcGF0aCBtdXN0IGJlIHBhdGhuYW1lICsgc2VhcmNoICsgaGFzaCBvbmx5LCBub3QgYSBmdWxseSBxdWFsaWZpZWQgVVJMIGxpa2UgXCIlc1wiJywgcGF0aCkgOiB1bmRlZmluZWQ7XG5cbiAgdmFyIGhhc2hJbmRleCA9IHBhdGhuYW1lLmluZGV4T2YoJyMnKTtcbiAgaWYgKGhhc2hJbmRleCAhPT0gLTEpIHtcbiAgICBoYXNoID0gcGF0aG5hbWUuc3Vic3RyaW5nKGhhc2hJbmRleCk7XG4gICAgcGF0aG5hbWUgPSBwYXRobmFtZS5zdWJzdHJpbmcoMCwgaGFzaEluZGV4KTtcbiAgfVxuXG4gIHZhciBzZWFyY2hJbmRleCA9IHBhdGhuYW1lLmluZGV4T2YoJz8nKTtcbiAgaWYgKHNlYXJjaEluZGV4ICE9PSAtMSkge1xuICAgIHNlYXJjaCA9IHBhdGhuYW1lLnN1YnN0cmluZyhzZWFyY2hJbmRleCk7XG4gICAgcGF0aG5hbWUgPSBwYXRobmFtZS5zdWJzdHJpbmcoMCwgc2VhcmNoSW5kZXgpO1xuICB9XG5cbiAgaWYgKHBhdGhuYW1lID09PSAnJykgcGF0aG5hbWUgPSAnLyc7XG5cbiAgcmV0dXJuIHtcbiAgICBwYXRobmFtZTogcGF0aG5hbWUsXG4gICAgc2VhcmNoOiBzZWFyY2gsXG4gICAgaGFzaDogaGFzaFxuICB9O1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX0FjdGlvbnMgPSByZXF1aXJlKCcuL0FjdGlvbnMnKTtcblxudmFyIF9QYXRoVXRpbHMgPSByZXF1aXJlKCcuL1BhdGhVdGlscycpO1xuXG52YXIgX0V4ZWN1dGlvbkVudmlyb25tZW50ID0gcmVxdWlyZSgnLi9FeGVjdXRpb25FbnZpcm9ubWVudCcpO1xuXG52YXIgX0RPTVV0aWxzID0gcmVxdWlyZSgnLi9ET01VdGlscycpO1xuXG52YXIgX0RPTVN0YXRlU3RvcmFnZSA9IHJlcXVpcmUoJy4vRE9NU3RhdGVTdG9yYWdlJyk7XG5cbnZhciBfY3JlYXRlRE9NSGlzdG9yeSA9IHJlcXVpcmUoJy4vY3JlYXRlRE9NSGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZURPTUhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlRE9NSGlzdG9yeSk7XG5cbi8qKlxuICogQ3JlYXRlcyBhbmQgcmV0dXJucyBhIGhpc3Rvcnkgb2JqZWN0IHRoYXQgdXNlcyBIVE1MNSdzIGhpc3RvcnkgQVBJXG4gKiAocHVzaFN0YXRlLCByZXBsYWNlU3RhdGUsIGFuZCB0aGUgcG9wc3RhdGUgZXZlbnQpIHRvIG1hbmFnZSBoaXN0b3J5LlxuICogVGhpcyBpcyB0aGUgcmVjb21tZW5kZWQgbWV0aG9kIG9mIG1hbmFnaW5nIGhpc3RvcnkgaW4gYnJvd3NlcnMgYmVjYXVzZVxuICogaXQgcHJvdmlkZXMgdGhlIGNsZWFuZXN0IFVSTHMuXG4gKlxuICogTm90ZTogSW4gYnJvd3NlcnMgdGhhdCBkbyBub3Qgc3VwcG9ydCB0aGUgSFRNTDUgaGlzdG9yeSBBUEkgZnVsbFxuICogcGFnZSByZWxvYWRzIHdpbGwgYmUgdXNlZCB0byBwcmVzZXJ2ZSBVUkxzLlxuICovXG5mdW5jdGlvbiBjcmVhdGVCcm93c2VySGlzdG9yeSgpIHtcbiAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcblxuICAhX0V4ZWN1dGlvbkVudmlyb25tZW50LmNhblVzZURPTSA/IHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlLCAnQnJvd3NlciBoaXN0b3J5IG5lZWRzIGEgRE9NJykgOiBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlKSA6IHVuZGVmaW5lZDtcblxuICB2YXIgZm9yY2VSZWZyZXNoID0gb3B0aW9ucy5mb3JjZVJlZnJlc2g7XG5cbiAgdmFyIGlzU3VwcG9ydGVkID0gX0RPTVV0aWxzLnN1cHBvcnRzSGlzdG9yeSgpO1xuICB2YXIgdXNlUmVmcmVzaCA9ICFpc1N1cHBvcnRlZCB8fCBmb3JjZVJlZnJlc2g7XG5cbiAgZnVuY3Rpb24gZ2V0Q3VycmVudExvY2F0aW9uKGhpc3RvcnlTdGF0ZSkge1xuICAgIHRyeSB7XG4gICAgICBoaXN0b3J5U3RhdGUgPSBoaXN0b3J5U3RhdGUgfHwgd2luZG93Lmhpc3Rvcnkuc3RhdGUgfHwge307XG4gICAgfSBjYXRjaCAoZSkge1xuICAgICAgaGlzdG9yeVN0YXRlID0ge307XG4gICAgfVxuXG4gICAgdmFyIHBhdGggPSBfRE9NVXRpbHMuZ2V0V2luZG93UGF0aCgpO1xuICAgIHZhciBfaGlzdG9yeVN0YXRlID0gaGlzdG9yeVN0YXRlO1xuICAgIHZhciBrZXkgPSBfaGlzdG9yeVN0YXRlLmtleTtcblxuICAgIHZhciBzdGF0ZSA9IHVuZGVmaW5lZDtcbiAgICBpZiAoa2V5KSB7XG4gICAgICBzdGF0ZSA9IF9ET01TdGF0ZVN0b3JhZ2UucmVhZFN0YXRlKGtleSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHN0YXRlID0gbnVsbDtcbiAgICAgIGtleSA9IGhpc3RvcnkuY3JlYXRlS2V5KCk7XG5cbiAgICAgIGlmIChpc1N1cHBvcnRlZCkgd2luZG93Lmhpc3RvcnkucmVwbGFjZVN0YXRlKF9leHRlbmRzKHt9LCBoaXN0b3J5U3RhdGUsIHsga2V5OiBrZXkgfSksIG51bGwpO1xuICAgIH1cblxuICAgIHZhciBsb2NhdGlvbiA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgcmV0dXJuIGhpc3RvcnkuY3JlYXRlTG9jYXRpb24oX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7IHN0YXRlOiBzdGF0ZSB9KSwgdW5kZWZpbmVkLCBrZXkpO1xuICB9XG5cbiAgZnVuY3Rpb24gc3RhcnRQb3BTdGF0ZUxpc3RlbmVyKF9yZWYpIHtcbiAgICB2YXIgdHJhbnNpdGlvblRvID0gX3JlZi50cmFuc2l0aW9uVG87XG5cbiAgICBmdW5jdGlvbiBwb3BTdGF0ZUxpc3RlbmVyKGV2ZW50KSB7XG4gICAgICBpZiAoZXZlbnQuc3RhdGUgPT09IHVuZGVmaW5lZCkgcmV0dXJuOyAvLyBJZ25vcmUgZXh0cmFuZW91cyBwb3BzdGF0ZSBldmVudHMgaW4gV2ViS2l0LlxuXG4gICAgICB0cmFuc2l0aW9uVG8oZ2V0Q3VycmVudExvY2F0aW9uKGV2ZW50LnN0YXRlKSk7XG4gICAgfVxuXG4gICAgX0RPTVV0aWxzLmFkZEV2ZW50TGlzdGVuZXIod2luZG93LCAncG9wc3RhdGUnLCBwb3BTdGF0ZUxpc3RlbmVyKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICBfRE9NVXRpbHMucmVtb3ZlRXZlbnRMaXN0ZW5lcih3aW5kb3csICdwb3BzdGF0ZScsIHBvcFN0YXRlTGlzdGVuZXIpO1xuICAgIH07XG4gIH1cblxuICBmdW5jdGlvbiBmaW5pc2hUcmFuc2l0aW9uKGxvY2F0aW9uKSB7XG4gICAgdmFyIGJhc2VuYW1lID0gbG9jYXRpb24uYmFzZW5hbWU7XG4gICAgdmFyIHBhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gICAgdmFyIHNlYXJjaCA9IGxvY2F0aW9uLnNlYXJjaDtcbiAgICB2YXIgaGFzaCA9IGxvY2F0aW9uLmhhc2g7XG4gICAgdmFyIHN0YXRlID0gbG9jYXRpb24uc3RhdGU7XG4gICAgdmFyIGFjdGlvbiA9IGxvY2F0aW9uLmFjdGlvbjtcbiAgICB2YXIga2V5ID0gbG9jYXRpb24ua2V5O1xuXG4gICAgaWYgKGFjdGlvbiA9PT0gX0FjdGlvbnMuUE9QKSByZXR1cm47IC8vIE5vdGhpbmcgdG8gZG8uXG5cbiAgICBfRE9NU3RhdGVTdG9yYWdlLnNhdmVTdGF0ZShrZXksIHN0YXRlKTtcblxuICAgIHZhciBwYXRoID0gKGJhc2VuYW1lIHx8ICcnKSArIHBhdGhuYW1lICsgc2VhcmNoICsgaGFzaDtcbiAgICB2YXIgaGlzdG9yeVN0YXRlID0ge1xuICAgICAga2V5OiBrZXlcbiAgICB9O1xuXG4gICAgaWYgKGFjdGlvbiA9PT0gX0FjdGlvbnMuUFVTSCkge1xuICAgICAgaWYgKHVzZVJlZnJlc2gpIHtcbiAgICAgICAgd2luZG93LmxvY2F0aW9uLmhyZWYgPSBwYXRoO1xuICAgICAgICByZXR1cm4gZmFsc2U7IC8vIFByZXZlbnQgbG9jYXRpb24gdXBkYXRlLlxuICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB3aW5kb3cuaGlzdG9yeS5wdXNoU3RhdGUoaGlzdG9yeVN0YXRlLCBudWxsLCBwYXRoKTtcbiAgICAgICAgfVxuICAgIH0gZWxzZSB7XG4gICAgICAvLyBSRVBMQUNFXG4gICAgICBpZiAodXNlUmVmcmVzaCkge1xuICAgICAgICB3aW5kb3cubG9jYXRpb24ucmVwbGFjZShwYXRoKTtcbiAgICAgICAgcmV0dXJuIGZhbHNlOyAvLyBQcmV2ZW50IGxvY2F0aW9uIHVwZGF0ZS5cbiAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgd2luZG93Lmhpc3RvcnkucmVwbGFjZVN0YXRlKGhpc3RvcnlTdGF0ZSwgbnVsbCwgcGF0aCk7XG4gICAgICAgIH1cbiAgICB9XG4gIH1cblxuICB2YXIgaGlzdG9yeSA9IF9jcmVhdGVET01IaXN0b3J5MlsnZGVmYXVsdCddKF9leHRlbmRzKHt9LCBvcHRpb25zLCB7XG4gICAgZ2V0Q3VycmVudExvY2F0aW9uOiBnZXRDdXJyZW50TG9jYXRpb24sXG4gICAgZmluaXNoVHJhbnNpdGlvbjogZmluaXNoVHJhbnNpdGlvbixcbiAgICBzYXZlU3RhdGU6IF9ET01TdGF0ZVN0b3JhZ2Uuc2F2ZVN0YXRlXG4gIH0pKTtcblxuICB2YXIgbGlzdGVuZXJDb3VudCA9IDAsXG4gICAgICBzdG9wUG9wU3RhdGVMaXN0ZW5lciA9IHVuZGVmaW5lZDtcblxuICBmdW5jdGlvbiBsaXN0ZW5CZWZvcmUobGlzdGVuZXIpIHtcbiAgICBpZiAoKytsaXN0ZW5lckNvdW50ID09PSAxKSBzdG9wUG9wU3RhdGVMaXN0ZW5lciA9IHN0YXJ0UG9wU3RhdGVMaXN0ZW5lcihoaXN0b3J5KTtcblxuICAgIHZhciB1bmxpc3RlbiA9IGhpc3RvcnkubGlzdGVuQmVmb3JlKGxpc3RlbmVyKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICB1bmxpc3RlbigpO1xuXG4gICAgICBpZiAoLS1saXN0ZW5lckNvdW50ID09PSAwKSBzdG9wUG9wU3RhdGVMaXN0ZW5lcigpO1xuICAgIH07XG4gIH1cblxuICBmdW5jdGlvbiBsaXN0ZW4obGlzdGVuZXIpIHtcbiAgICBpZiAoKytsaXN0ZW5lckNvdW50ID09PSAxKSBzdG9wUG9wU3RhdGVMaXN0ZW5lciA9IHN0YXJ0UG9wU3RhdGVMaXN0ZW5lcihoaXN0b3J5KTtcblxuICAgIHZhciB1bmxpc3RlbiA9IGhpc3RvcnkubGlzdGVuKGxpc3RlbmVyKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICB1bmxpc3RlbigpO1xuXG4gICAgICBpZiAoLS1saXN0ZW5lckNvdW50ID09PSAwKSBzdG9wUG9wU3RhdGVMaXN0ZW5lcigpO1xuICAgIH07XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vaykge1xuICAgIGlmICgrK2xpc3RlbmVyQ291bnQgPT09IDEpIHN0b3BQb3BTdGF0ZUxpc3RlbmVyID0gc3RhcnRQb3BTdGF0ZUxpc3RlbmVyKGhpc3RvcnkpO1xuXG4gICAgaGlzdG9yeS5yZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vaykge1xuICAgIGhpc3RvcnkudW5yZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spO1xuXG4gICAgaWYgKC0tbGlzdGVuZXJDb3VudCA9PT0gMCkgc3RvcFBvcFN0YXRlTGlzdGVuZXIoKTtcbiAgfVxuXG4gIHJldHVybiBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwge1xuICAgIGxpc3RlbkJlZm9yZTogbGlzdGVuQmVmb3JlLFxuICAgIGxpc3RlbjogbGlzdGVuLFxuICAgIHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2s6IHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2ssXG4gICAgdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rOiB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2tcbiAgfSk7XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGNyZWF0ZUJyb3dzZXJIaXN0b3J5O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfRXhlY3V0aW9uRW52aXJvbm1lbnQgPSByZXF1aXJlKCcuL0V4ZWN1dGlvbkVudmlyb25tZW50Jyk7XG5cbnZhciBfRE9NVXRpbHMgPSByZXF1aXJlKCcuL0RPTVV0aWxzJyk7XG5cbnZhciBfY3JlYXRlSGlzdG9yeSA9IHJlcXVpcmUoJy4vY3JlYXRlSGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZUhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlSGlzdG9yeSk7XG5cbmZ1bmN0aW9uIGNyZWF0ZURPTUhpc3Rvcnkob3B0aW9ucykge1xuICB2YXIgaGlzdG9yeSA9IF9jcmVhdGVIaXN0b3J5MlsnZGVmYXVsdCddKF9leHRlbmRzKHtcbiAgICBnZXRVc2VyQ29uZmlybWF0aW9uOiBfRE9NVXRpbHMuZ2V0VXNlckNvbmZpcm1hdGlvblxuICB9LCBvcHRpb25zLCB7XG4gICAgZ286IF9ET01VdGlscy5nb1xuICB9KSk7XG5cbiAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgIV9FeGVjdXRpb25FbnZpcm9ubWVudC5jYW5Vc2VET00gPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSwgJ0RPTSBoaXN0b3J5IG5lZWRzIGEgRE9NJykgOiBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlKSA6IHVuZGVmaW5lZDtcblxuICAgIHJldHVybiBoaXN0b3J5Lmxpc3RlbihsaXN0ZW5lcik7XG4gIH1cblxuICByZXR1cm4gX2V4dGVuZHMoe30sIGhpc3RvcnksIHtcbiAgICBsaXN0ZW46IGxpc3RlblxuICB9KTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gY3JlYXRlRE9NSGlzdG9yeTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9BY3Rpb25zID0gcmVxdWlyZSgnLi9BY3Rpb25zJyk7XG5cbnZhciBfUGF0aFV0aWxzID0gcmVxdWlyZSgnLi9QYXRoVXRpbHMnKTtcblxudmFyIF9FeGVjdXRpb25FbnZpcm9ubWVudCA9IHJlcXVpcmUoJy4vRXhlY3V0aW9uRW52aXJvbm1lbnQnKTtcblxudmFyIF9ET01VdGlscyA9IHJlcXVpcmUoJy4vRE9NVXRpbHMnKTtcblxudmFyIF9ET01TdGF0ZVN0b3JhZ2UgPSByZXF1aXJlKCcuL0RPTVN0YXRlU3RvcmFnZScpO1xuXG52YXIgX2NyZWF0ZURPTUhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZURPTUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVET01IaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZURPTUhpc3RvcnkpO1xuXG5mdW5jdGlvbiBpc0Fic29sdXRlUGF0aChwYXRoKSB7XG4gIHJldHVybiB0eXBlb2YgcGF0aCA9PT0gJ3N0cmluZycgJiYgcGF0aC5jaGFyQXQoMCkgPT09ICcvJztcbn1cblxuZnVuY3Rpb24gZW5zdXJlU2xhc2goKSB7XG4gIHZhciBwYXRoID0gX0RPTVV0aWxzLmdldEhhc2hQYXRoKCk7XG5cbiAgaWYgKGlzQWJzb2x1dGVQYXRoKHBhdGgpKSByZXR1cm4gdHJ1ZTtcblxuICBfRE9NVXRpbHMucmVwbGFjZUhhc2hQYXRoKCcvJyArIHBhdGgpO1xuXG4gIHJldHVybiBmYWxzZTtcbn1cblxuZnVuY3Rpb24gYWRkUXVlcnlTdHJpbmdWYWx1ZVRvUGF0aChwYXRoLCBrZXksIHZhbHVlKSB7XG4gIHJldHVybiBwYXRoICsgKHBhdGguaW5kZXhPZignPycpID09PSAtMSA/ICc/JyA6ICcmJykgKyAoa2V5ICsgJz0nICsgdmFsdWUpO1xufVxuXG5mdW5jdGlvbiBzdHJpcFF1ZXJ5U3RyaW5nVmFsdWVGcm9tUGF0aChwYXRoLCBrZXkpIHtcbiAgcmV0dXJuIHBhdGgucmVwbGFjZShuZXcgUmVnRXhwKCdbPyZdPycgKyBrZXkgKyAnPVthLXpBLVowLTldKycpLCAnJyk7XG59XG5cbmZ1bmN0aW9uIGdldFF1ZXJ5U3RyaW5nVmFsdWVGcm9tUGF0aChwYXRoLCBrZXkpIHtcbiAgdmFyIG1hdGNoID0gcGF0aC5tYXRjaChuZXcgUmVnRXhwKCdcXFxcPy4qP1xcXFxiJyArIGtleSArICc9KC4rPylcXFxcYicpKTtcbiAgcmV0dXJuIG1hdGNoICYmIG1hdGNoWzFdO1xufVxuXG52YXIgRGVmYXVsdFF1ZXJ5S2V5ID0gJ19rJztcblxuZnVuY3Rpb24gY3JlYXRlSGFzaEhpc3RvcnkoKSB7XG4gIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8ge30gOiBhcmd1bWVudHNbMF07XG5cbiAgIV9FeGVjdXRpb25FbnZpcm9ubWVudC5jYW5Vc2VET00gPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSwgJ0hhc2ggaGlzdG9yeSBuZWVkcyBhIERPTScpIDogX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSkgOiB1bmRlZmluZWQ7XG5cbiAgdmFyIHF1ZXJ5S2V5ID0gb3B0aW9ucy5xdWVyeUtleTtcblxuICBpZiAocXVlcnlLZXkgPT09IHVuZGVmaW5lZCB8fCAhIXF1ZXJ5S2V5KSBxdWVyeUtleSA9IHR5cGVvZiBxdWVyeUtleSA9PT0gJ3N0cmluZycgPyBxdWVyeUtleSA6IERlZmF1bHRRdWVyeUtleTtcblxuICBmdW5jdGlvbiBnZXRDdXJyZW50TG9jYXRpb24oKSB7XG4gICAgdmFyIHBhdGggPSBfRE9NVXRpbHMuZ2V0SGFzaFBhdGgoKTtcblxuICAgIHZhciBrZXkgPSB1bmRlZmluZWQsXG4gICAgICAgIHN0YXRlID0gdW5kZWZpbmVkO1xuICAgIGlmIChxdWVyeUtleSkge1xuICAgICAga2V5ID0gZ2V0UXVlcnlTdHJpbmdWYWx1ZUZyb21QYXRoKHBhdGgsIHF1ZXJ5S2V5KTtcbiAgICAgIHBhdGggPSBzdHJpcFF1ZXJ5U3RyaW5nVmFsdWVGcm9tUGF0aChwYXRoLCBxdWVyeUtleSk7XG5cbiAgICAgIGlmIChrZXkpIHtcbiAgICAgICAgc3RhdGUgPSBfRE9NU3RhdGVTdG9yYWdlLnJlYWRTdGF0ZShrZXkpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgc3RhdGUgPSBudWxsO1xuICAgICAgICBrZXkgPSBoaXN0b3J5LmNyZWF0ZUtleSgpO1xuICAgICAgICBfRE9NVXRpbHMucmVwbGFjZUhhc2hQYXRoKGFkZFF1ZXJ5U3RyaW5nVmFsdWVUb1BhdGgocGF0aCwgcXVlcnlLZXksIGtleSkpO1xuICAgICAgfVxuICAgIH0gZWxzZSB7XG4gICAgICBrZXkgPSBzdGF0ZSA9IG51bGw7XG4gICAgfVxuXG4gICAgdmFyIGxvY2F0aW9uID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbihfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIHsgc3RhdGU6IHN0YXRlIH0pLCB1bmRlZmluZWQsIGtleSk7XG4gIH1cblxuICBmdW5jdGlvbiBzdGFydEhhc2hDaGFuZ2VMaXN0ZW5lcihfcmVmKSB7XG4gICAgdmFyIHRyYW5zaXRpb25UbyA9IF9yZWYudHJhbnNpdGlvblRvO1xuXG4gICAgZnVuY3Rpb24gaGFzaENoYW5nZUxpc3RlbmVyKCkge1xuICAgICAgaWYgKCFlbnN1cmVTbGFzaCgpKSByZXR1cm47IC8vIEFsd2F5cyBtYWtlIHN1cmUgaGFzaGVzIGFyZSBwcmVjZWVkZWQgd2l0aCBhIC8uXG5cbiAgICAgIHRyYW5zaXRpb25UbyhnZXRDdXJyZW50TG9jYXRpb24oKSk7XG4gICAgfVxuXG4gICAgZW5zdXJlU2xhc2goKTtcbiAgICBfRE9NVXRpbHMuYWRkRXZlbnRMaXN0ZW5lcih3aW5kb3csICdoYXNoY2hhbmdlJywgaGFzaENoYW5nZUxpc3RlbmVyKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICBfRE9NVXRpbHMucmVtb3ZlRXZlbnRMaXN0ZW5lcih3aW5kb3csICdoYXNoY2hhbmdlJywgaGFzaENoYW5nZUxpc3RlbmVyKTtcbiAgICB9O1xuICB9XG5cbiAgZnVuY3Rpb24gZmluaXNoVHJhbnNpdGlvbihsb2NhdGlvbikge1xuICAgIHZhciBiYXNlbmFtZSA9IGxvY2F0aW9uLmJhc2VuYW1lO1xuICAgIHZhciBwYXRobmFtZSA9IGxvY2F0aW9uLnBhdGhuYW1lO1xuICAgIHZhciBzZWFyY2ggPSBsb2NhdGlvbi5zZWFyY2g7XG4gICAgdmFyIHN0YXRlID0gbG9jYXRpb24uc3RhdGU7XG4gICAgdmFyIGFjdGlvbiA9IGxvY2F0aW9uLmFjdGlvbjtcbiAgICB2YXIga2V5ID0gbG9jYXRpb24ua2V5O1xuXG4gICAgaWYgKGFjdGlvbiA9PT0gX0FjdGlvbnMuUE9QKSByZXR1cm47IC8vIE5vdGhpbmcgdG8gZG8uXG5cbiAgICB2YXIgcGF0aCA9IChiYXNlbmFtZSB8fCAnJykgKyBwYXRobmFtZSArIHNlYXJjaDtcblxuICAgIGlmIChxdWVyeUtleSkge1xuICAgICAgcGF0aCA9IGFkZFF1ZXJ5U3RyaW5nVmFsdWVUb1BhdGgocGF0aCwgcXVlcnlLZXksIGtleSk7XG4gICAgICBfRE9NU3RhdGVTdG9yYWdlLnNhdmVTdGF0ZShrZXksIHN0YXRlKTtcbiAgICB9IGVsc2Uge1xuICAgICAgLy8gRHJvcCBrZXkgYW5kIHN0YXRlLlxuICAgICAgbG9jYXRpb24ua2V5ID0gbG9jYXRpb24uc3RhdGUgPSBudWxsO1xuICAgIH1cblxuICAgIHZhciBjdXJyZW50SGFzaCA9IF9ET01VdGlscy5nZXRIYXNoUGF0aCgpO1xuXG4gICAgaWYgKGFjdGlvbiA9PT0gX0FjdGlvbnMuUFVTSCkge1xuICAgICAgaWYgKGN1cnJlbnRIYXNoICE9PSBwYXRoKSB7XG4gICAgICAgIHdpbmRvdy5sb2NhdGlvbi5oYXNoID0gcGF0aDtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ1lvdSBjYW5ub3QgUFVTSCB0aGUgc2FtZSBwYXRoIHVzaW5nIGhhc2ggaGlzdG9yeScpIDogdW5kZWZpbmVkO1xuICAgICAgfVxuICAgIH0gZWxzZSBpZiAoY3VycmVudEhhc2ggIT09IHBhdGgpIHtcbiAgICAgIC8vIFJFUExBQ0VcbiAgICAgIF9ET01VdGlscy5yZXBsYWNlSGFzaFBhdGgocGF0aCk7XG4gICAgfVxuICB9XG5cbiAgdmFyIGhpc3RvcnkgPSBfY3JlYXRlRE9NSGlzdG9yeTJbJ2RlZmF1bHQnXShfZXh0ZW5kcyh7fSwgb3B0aW9ucywge1xuICAgIGdldEN1cnJlbnRMb2NhdGlvbjogZ2V0Q3VycmVudExvY2F0aW9uLFxuICAgIGZpbmlzaFRyYW5zaXRpb246IGZpbmlzaFRyYW5zaXRpb24sXG4gICAgc2F2ZVN0YXRlOiBfRE9NU3RhdGVTdG9yYWdlLnNhdmVTdGF0ZVxuICB9KSk7XG5cbiAgdmFyIGxpc3RlbmVyQ291bnQgPSAwLFxuICAgICAgc3RvcEhhc2hDaGFuZ2VMaXN0ZW5lciA9IHVuZGVmaW5lZDtcblxuICBmdW5jdGlvbiBsaXN0ZW5CZWZvcmUobGlzdGVuZXIpIHtcbiAgICBpZiAoKytsaXN0ZW5lckNvdW50ID09PSAxKSBzdG9wSGFzaENoYW5nZUxpc3RlbmVyID0gc3RhcnRIYXNoQ2hhbmdlTGlzdGVuZXIoaGlzdG9yeSk7XG5cbiAgICB2YXIgdW5saXN0ZW4gPSBoaXN0b3J5Lmxpc3RlbkJlZm9yZShsaXN0ZW5lcik7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgdW5saXN0ZW4oKTtcblxuICAgICAgaWYgKC0tbGlzdGVuZXJDb3VudCA9PT0gMCkgc3RvcEhhc2hDaGFuZ2VMaXN0ZW5lcigpO1xuICAgIH07XG4gIH1cblxuICBmdW5jdGlvbiBsaXN0ZW4obGlzdGVuZXIpIHtcbiAgICBpZiAoKytsaXN0ZW5lckNvdW50ID09PSAxKSBzdG9wSGFzaENoYW5nZUxpc3RlbmVyID0gc3RhcnRIYXNoQ2hhbmdlTGlzdGVuZXIoaGlzdG9yeSk7XG5cbiAgICB2YXIgdW5saXN0ZW4gPSBoaXN0b3J5Lmxpc3RlbihsaXN0ZW5lcik7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgICAgdW5saXN0ZW4oKTtcblxuICAgICAgaWYgKC0tbGlzdGVuZXJDb3VudCA9PT0gMCkgc3RvcEhhc2hDaGFuZ2VMaXN0ZW5lcigpO1xuICAgIH07XG4gIH1cblxuICBmdW5jdGlvbiBwdXNoKGxvY2F0aW9uKSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKHF1ZXJ5S2V5IHx8IGxvY2F0aW9uLnN0YXRlID09IG51bGwsICdZb3UgY2Fubm90IHVzZSBzdGF0ZSB3aXRob3V0IGEgcXVlcnlLZXkgaXQgd2lsbCBiZSBkcm9wcGVkJykgOiB1bmRlZmluZWQ7XG5cbiAgICBoaXN0b3J5LnB1c2gobG9jYXRpb24pO1xuICB9XG5cbiAgZnVuY3Rpb24gcmVwbGFjZShsb2NhdGlvbikge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShxdWVyeUtleSB8fCBsb2NhdGlvbi5zdGF0ZSA9PSBudWxsLCAnWW91IGNhbm5vdCB1c2Ugc3RhdGUgd2l0aG91dCBhIHF1ZXJ5S2V5IGl0IHdpbGwgYmUgZHJvcHBlZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgaGlzdG9yeS5yZXBsYWNlKGxvY2F0aW9uKTtcbiAgfVxuXG4gIHZhciBnb0lzU3VwcG9ydGVkV2l0aG91dFJlbG9hZCA9IF9ET01VdGlscy5zdXBwb3J0c0dvV2l0aG91dFJlbG9hZFVzaW5nSGFzaCgpO1xuXG4gIGZ1bmN0aW9uIGdvKG4pIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZ29Jc1N1cHBvcnRlZFdpdGhvdXRSZWxvYWQsICdIYXNoIGhpc3RvcnkgZ28obikgY2F1c2VzIGEgZnVsbCBwYWdlIHJlbG9hZCBpbiB0aGlzIGJyb3dzZXInKSA6IHVuZGVmaW5lZDtcblxuICAgIGhpc3RvcnkuZ28obik7XG4gIH1cblxuICBmdW5jdGlvbiBjcmVhdGVIcmVmKHBhdGgpIHtcbiAgICByZXR1cm4gJyMnICsgaGlzdG9yeS5jcmVhdGVIcmVmKHBhdGgpO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiByZWdpc3RlclRyYW5zaXRpb25Ib29rKGhvb2spIHtcbiAgICBpZiAoKytsaXN0ZW5lckNvdW50ID09PSAxKSBzdG9wSGFzaENoYW5nZUxpc3RlbmVyID0gc3RhcnRIYXNoQ2hhbmdlTGlzdGVuZXIoaGlzdG9yeSk7XG5cbiAgICBoaXN0b3J5LnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vayk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKSB7XG4gICAgaGlzdG9yeS51bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2soaG9vayk7XG5cbiAgICBpZiAoLS1saXN0ZW5lckNvdW50ID09PSAwKSBzdG9wSGFzaENoYW5nZUxpc3RlbmVyKCk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHB1c2hTdGF0ZShzdGF0ZSwgcGF0aCkge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShxdWVyeUtleSB8fCBzdGF0ZSA9PSBudWxsLCAnWW91IGNhbm5vdCB1c2Ugc3RhdGUgd2l0aG91dCBhIHF1ZXJ5S2V5IGl0IHdpbGwgYmUgZHJvcHBlZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgaGlzdG9yeS5wdXNoU3RhdGUoc3RhdGUsIHBhdGgpO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiByZXBsYWNlU3RhdGUoc3RhdGUsIHBhdGgpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10ocXVlcnlLZXkgfHwgc3RhdGUgPT0gbnVsbCwgJ1lvdSBjYW5ub3QgdXNlIHN0YXRlIHdpdGhvdXQgYSBxdWVyeUtleSBpdCB3aWxsIGJlIGRyb3BwZWQnKSA6IHVuZGVmaW5lZDtcblxuICAgIGhpc3RvcnkucmVwbGFjZVN0YXRlKHN0YXRlLCBwYXRoKTtcbiAgfVxuXG4gIHJldHVybiBfZXh0ZW5kcyh7fSwgaGlzdG9yeSwge1xuICAgIGxpc3RlbkJlZm9yZTogbGlzdGVuQmVmb3JlLFxuICAgIGxpc3RlbjogbGlzdGVuLFxuICAgIHB1c2g6IHB1c2gsXG4gICAgcmVwbGFjZTogcmVwbGFjZSxcbiAgICBnbzogZ28sXG4gICAgY3JlYXRlSHJlZjogY3JlYXRlSHJlZixcblxuICAgIHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2s6IHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2ssIC8vIGRlcHJlY2F0ZWQgLSB3YXJuaW5nIGlzIGluIGNyZWF0ZUhpc3RvcnlcbiAgICB1bnJlZ2lzdGVyVHJhbnNpdGlvbkhvb2s6IHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vaywgLy8gZGVwcmVjYXRlZCAtIHdhcm5pbmcgaXMgaW4gY3JlYXRlSGlzdG9yeVxuICAgIHB1c2hTdGF0ZTogcHVzaFN0YXRlLCAvLyBkZXByZWNhdGVkIC0gd2FybmluZyBpcyBpbiBjcmVhdGVIaXN0b3J5XG4gICAgcmVwbGFjZVN0YXRlOiByZXBsYWNlU3RhdGUgLy8gZGVwcmVjYXRlZCAtIHdhcm5pbmcgaXMgaW4gY3JlYXRlSGlzdG9yeVxuICB9KTtcbn1cblxuZXhwb3J0c1snZGVmYXVsdCddID0gY3JlYXRlSGFzaEhpc3Rvcnk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfZGVlcEVxdWFsID0gcmVxdWlyZSgnZGVlcC1lcXVhbCcpO1xuXG52YXIgX2RlZXBFcXVhbDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9kZWVwRXF1YWwpO1xuXG52YXIgX1BhdGhVdGlscyA9IHJlcXVpcmUoJy4vUGF0aFV0aWxzJyk7XG5cbnZhciBfQXN5bmNVdGlscyA9IHJlcXVpcmUoJy4vQXN5bmNVdGlscycpO1xuXG52YXIgX0FjdGlvbnMgPSByZXF1aXJlKCcuL0FjdGlvbnMnKTtcblxudmFyIF9jcmVhdGVMb2NhdGlvbjIgPSByZXF1aXJlKCcuL2NyZWF0ZUxvY2F0aW9uJyk7XG5cbnZhciBfY3JlYXRlTG9jYXRpb24zID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlTG9jYXRpb24yKTtcblxudmFyIF9ydW5UcmFuc2l0aW9uSG9vayA9IHJlcXVpcmUoJy4vcnVuVHJhbnNpdGlvbkhvb2snKTtcblxudmFyIF9ydW5UcmFuc2l0aW9uSG9vazIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9ydW5UcmFuc2l0aW9uSG9vayk7XG5cbnZhciBfZGVwcmVjYXRlID0gcmVxdWlyZSgnLi9kZXByZWNhdGUnKTtcblxudmFyIF9kZXByZWNhdGUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZGVwcmVjYXRlKTtcblxuZnVuY3Rpb24gY3JlYXRlUmFuZG9tS2V5KGxlbmd0aCkge1xuICByZXR1cm4gTWF0aC5yYW5kb20oKS50b1N0cmluZygzNikuc3Vic3RyKDIsIGxlbmd0aCk7XG59XG5cbmZ1bmN0aW9uIGxvY2F0aW9uc0FyZUVxdWFsKGEsIGIpIHtcbiAgcmV0dXJuIGEucGF0aG5hbWUgPT09IGIucGF0aG5hbWUgJiYgYS5zZWFyY2ggPT09IGIuc2VhcmNoICYmXG4gIC8vYS5hY3Rpb24gPT09IGIuYWN0aW9uICYmIC8vIERpZmZlcmVudCBhY3Rpb24gIT09IGxvY2F0aW9uIGNoYW5nZS5cbiAgYS5rZXkgPT09IGIua2V5ICYmIF9kZWVwRXF1YWwyWydkZWZhdWx0J10oYS5zdGF0ZSwgYi5zdGF0ZSk7XG59XG5cbnZhciBEZWZhdWx0S2V5TGVuZ3RoID0gNjtcblxuZnVuY3Rpb24gY3JlYXRlSGlzdG9yeSgpIHtcbiAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcbiAgdmFyIGdldEN1cnJlbnRMb2NhdGlvbiA9IG9wdGlvbnMuZ2V0Q3VycmVudExvY2F0aW9uO1xuICB2YXIgZmluaXNoVHJhbnNpdGlvbiA9IG9wdGlvbnMuZmluaXNoVHJhbnNpdGlvbjtcbiAgdmFyIHNhdmVTdGF0ZSA9IG9wdGlvbnMuc2F2ZVN0YXRlO1xuICB2YXIgZ28gPSBvcHRpb25zLmdvO1xuICB2YXIgZ2V0VXNlckNvbmZpcm1hdGlvbiA9IG9wdGlvbnMuZ2V0VXNlckNvbmZpcm1hdGlvbjtcbiAgdmFyIGtleUxlbmd0aCA9IG9wdGlvbnMua2V5TGVuZ3RoO1xuXG4gIGlmICh0eXBlb2Yga2V5TGVuZ3RoICE9PSAnbnVtYmVyJykga2V5TGVuZ3RoID0gRGVmYXVsdEtleUxlbmd0aDtcblxuICB2YXIgdHJhbnNpdGlvbkhvb2tzID0gW107XG5cbiAgZnVuY3Rpb24gbGlzdGVuQmVmb3JlKGhvb2spIHtcbiAgICB0cmFuc2l0aW9uSG9va3MucHVzaChob29rKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgICB0cmFuc2l0aW9uSG9va3MgPSB0cmFuc2l0aW9uSG9va3MuZmlsdGVyKGZ1bmN0aW9uIChpdGVtKSB7XG4gICAgICAgIHJldHVybiBpdGVtICE9PSBob29rO1xuICAgICAgfSk7XG4gICAgfTtcbiAgfVxuXG4gIHZhciBhbGxLZXlzID0gW107XG4gIHZhciBjaGFuZ2VMaXN0ZW5lcnMgPSBbXTtcbiAgdmFyIGxvY2F0aW9uID0gdW5kZWZpbmVkO1xuXG4gIGZ1bmN0aW9uIGdldEN1cnJlbnQoKSB7XG4gICAgaWYgKHBlbmRpbmdMb2NhdGlvbiAmJiBwZW5kaW5nTG9jYXRpb24uYWN0aW9uID09PSBfQWN0aW9ucy5QT1ApIHtcbiAgICAgIHJldHVybiBhbGxLZXlzLmluZGV4T2YocGVuZGluZ0xvY2F0aW9uLmtleSk7XG4gICAgfSBlbHNlIGlmIChsb2NhdGlvbikge1xuICAgICAgcmV0dXJuIGFsbEtleXMuaW5kZXhPZihsb2NhdGlvbi5rZXkpO1xuICAgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gLTE7XG4gICAgfVxuICB9XG5cbiAgZnVuY3Rpb24gdXBkYXRlTG9jYXRpb24obmV3TG9jYXRpb24pIHtcbiAgICB2YXIgY3VycmVudCA9IGdldEN1cnJlbnQoKTtcblxuICAgIGxvY2F0aW9uID0gbmV3TG9jYXRpb247XG5cbiAgICBpZiAobG9jYXRpb24uYWN0aW9uID09PSBfQWN0aW9ucy5QVVNIKSB7XG4gICAgICBhbGxLZXlzID0gW10uY29uY2F0KGFsbEtleXMuc2xpY2UoMCwgY3VycmVudCArIDEpLCBbbG9jYXRpb24ua2V5XSk7XG4gICAgfSBlbHNlIGlmIChsb2NhdGlvbi5hY3Rpb24gPT09IF9BY3Rpb25zLlJFUExBQ0UpIHtcbiAgICAgIGFsbEtleXNbY3VycmVudF0gPSBsb2NhdGlvbi5rZXk7XG4gICAgfVxuXG4gICAgY2hhbmdlTGlzdGVuZXJzLmZvckVhY2goZnVuY3Rpb24gKGxpc3RlbmVyKSB7XG4gICAgICBsaXN0ZW5lcihsb2NhdGlvbik7XG4gICAgfSk7XG4gIH1cblxuICBmdW5jdGlvbiBsaXN0ZW4obGlzdGVuZXIpIHtcbiAgICBjaGFuZ2VMaXN0ZW5lcnMucHVzaChsaXN0ZW5lcik7XG5cbiAgICBpZiAobG9jYXRpb24pIHtcbiAgICAgIGxpc3RlbmVyKGxvY2F0aW9uKTtcbiAgICB9IGVsc2Uge1xuICAgICAgdmFyIF9sb2NhdGlvbiA9IGdldEN1cnJlbnRMb2NhdGlvbigpO1xuICAgICAgYWxsS2V5cyA9IFtfbG9jYXRpb24ua2V5XTtcbiAgICAgIHVwZGF0ZUxvY2F0aW9uKF9sb2NhdGlvbik7XG4gICAgfVxuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIGNoYW5nZUxpc3RlbmVycyA9IGNoYW5nZUxpc3RlbmVycy5maWx0ZXIoZnVuY3Rpb24gKGl0ZW0pIHtcbiAgICAgICAgcmV0dXJuIGl0ZW0gIT09IGxpc3RlbmVyO1xuICAgICAgfSk7XG4gICAgfTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNvbmZpcm1UcmFuc2l0aW9uVG8obG9jYXRpb24sIGNhbGxiYWNrKSB7XG4gICAgX0FzeW5jVXRpbHMubG9vcEFzeW5jKHRyYW5zaXRpb25Ib29rcy5sZW5ndGgsIGZ1bmN0aW9uIChpbmRleCwgbmV4dCwgZG9uZSkge1xuICAgICAgX3J1blRyYW5zaXRpb25Ib29rMlsnZGVmYXVsdCddKHRyYW5zaXRpb25Ib29rc1tpbmRleF0sIGxvY2F0aW9uLCBmdW5jdGlvbiAocmVzdWx0KSB7XG4gICAgICAgIGlmIChyZXN1bHQgIT0gbnVsbCkge1xuICAgICAgICAgIGRvbmUocmVzdWx0KTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBuZXh0KCk7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH0sIGZ1bmN0aW9uIChtZXNzYWdlKSB7XG4gICAgICBpZiAoZ2V0VXNlckNvbmZpcm1hdGlvbiAmJiB0eXBlb2YgbWVzc2FnZSA9PT0gJ3N0cmluZycpIHtcbiAgICAgICAgZ2V0VXNlckNvbmZpcm1hdGlvbihtZXNzYWdlLCBmdW5jdGlvbiAob2spIHtcbiAgICAgICAgICBjYWxsYmFjayhvayAhPT0gZmFsc2UpO1xuICAgICAgICB9KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIGNhbGxiYWNrKG1lc3NhZ2UgIT09IGZhbHNlKTtcbiAgICAgIH1cbiAgICB9KTtcbiAgfVxuXG4gIHZhciBwZW5kaW5nTG9jYXRpb24gPSB1bmRlZmluZWQ7XG5cbiAgZnVuY3Rpb24gdHJhbnNpdGlvblRvKG5leHRMb2NhdGlvbikge1xuICAgIGlmIChsb2NhdGlvbiAmJiBsb2NhdGlvbnNBcmVFcXVhbChsb2NhdGlvbiwgbmV4dExvY2F0aW9uKSkgcmV0dXJuOyAvLyBOb3RoaW5nIHRvIGRvLlxuXG4gICAgcGVuZGluZ0xvY2F0aW9uID0gbmV4dExvY2F0aW9uO1xuXG4gICAgY29uZmlybVRyYW5zaXRpb25UbyhuZXh0TG9jYXRpb24sIGZ1bmN0aW9uIChvaykge1xuICAgICAgaWYgKHBlbmRpbmdMb2NhdGlvbiAhPT0gbmV4dExvY2F0aW9uKSByZXR1cm47IC8vIFRyYW5zaXRpb24gd2FzIGludGVycnVwdGVkLlxuXG4gICAgICBpZiAob2spIHtcbiAgICAgICAgLy8gdHJlYXQgUFVTSCB0byBjdXJyZW50IHBhdGggbGlrZSBSRVBMQUNFIHRvIGJlIGNvbnNpc3RlbnQgd2l0aCBicm93c2Vyc1xuICAgICAgICBpZiAobmV4dExvY2F0aW9uLmFjdGlvbiA9PT0gX0FjdGlvbnMuUFVTSCkge1xuICAgICAgICAgIHZhciBwcmV2UGF0aCA9IGNyZWF0ZVBhdGgobG9jYXRpb24pO1xuICAgICAgICAgIHZhciBuZXh0UGF0aCA9IGNyZWF0ZVBhdGgobmV4dExvY2F0aW9uKTtcblxuICAgICAgICAgIGlmIChuZXh0UGF0aCA9PT0gcHJldlBhdGggJiYgX2RlZXBFcXVhbDJbJ2RlZmF1bHQnXShsb2NhdGlvbi5zdGF0ZSwgbmV4dExvY2F0aW9uLnN0YXRlKSkgbmV4dExvY2F0aW9uLmFjdGlvbiA9IF9BY3Rpb25zLlJFUExBQ0U7XG4gICAgICAgIH1cblxuICAgICAgICBpZiAoZmluaXNoVHJhbnNpdGlvbihuZXh0TG9jYXRpb24pICE9PSBmYWxzZSkgdXBkYXRlTG9jYXRpb24obmV4dExvY2F0aW9uKTtcbiAgICAgIH0gZWxzZSBpZiAobG9jYXRpb24gJiYgbmV4dExvY2F0aW9uLmFjdGlvbiA9PT0gX0FjdGlvbnMuUE9QKSB7XG4gICAgICAgIHZhciBwcmV2SW5kZXggPSBhbGxLZXlzLmluZGV4T2YobG9jYXRpb24ua2V5KTtcbiAgICAgICAgdmFyIG5leHRJbmRleCA9IGFsbEtleXMuaW5kZXhPZihuZXh0TG9jYXRpb24ua2V5KTtcblxuICAgICAgICBpZiAocHJldkluZGV4ICE9PSAtMSAmJiBuZXh0SW5kZXggIT09IC0xKSBnbyhwcmV2SW5kZXggLSBuZXh0SW5kZXgpOyAvLyBSZXN0b3JlIHRoZSBVUkwuXG4gICAgICB9XG4gICAgfSk7XG4gIH1cblxuICBmdW5jdGlvbiBwdXNoKGxvY2F0aW9uKSB7XG4gICAgdHJhbnNpdGlvblRvKGNyZWF0ZUxvY2F0aW9uKGxvY2F0aW9uLCBfQWN0aW9ucy5QVVNILCBjcmVhdGVLZXkoKSkpO1xuICB9XG5cbiAgZnVuY3Rpb24gcmVwbGFjZShsb2NhdGlvbikge1xuICAgIHRyYW5zaXRpb25UbyhjcmVhdGVMb2NhdGlvbihsb2NhdGlvbiwgX0FjdGlvbnMuUkVQTEFDRSwgY3JlYXRlS2V5KCkpKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGdvQmFjaygpIHtcbiAgICBnbygtMSk7XG4gIH1cblxuICBmdW5jdGlvbiBnb0ZvcndhcmQoKSB7XG4gICAgZ28oMSk7XG4gIH1cblxuICBmdW5jdGlvbiBjcmVhdGVLZXkoKSB7XG4gICAgcmV0dXJuIGNyZWF0ZVJhbmRvbUtleShrZXlMZW5ndGgpO1xuICB9XG5cbiAgZnVuY3Rpb24gY3JlYXRlUGF0aChsb2NhdGlvbikge1xuICAgIGlmIChsb2NhdGlvbiA9PSBudWxsIHx8IHR5cGVvZiBsb2NhdGlvbiA9PT0gJ3N0cmluZycpIHJldHVybiBsb2NhdGlvbjtcblxuICAgIHZhciBwYXRobmFtZSA9IGxvY2F0aW9uLnBhdGhuYW1lO1xuICAgIHZhciBzZWFyY2ggPSBsb2NhdGlvbi5zZWFyY2g7XG4gICAgdmFyIGhhc2ggPSBsb2NhdGlvbi5oYXNoO1xuXG4gICAgdmFyIHJlc3VsdCA9IHBhdGhuYW1lO1xuXG4gICAgaWYgKHNlYXJjaCkgcmVzdWx0ICs9IHNlYXJjaDtcblxuICAgIGlmIChoYXNoKSByZXN1bHQgKz0gaGFzaDtcblxuICAgIHJldHVybiByZXN1bHQ7XG4gIH1cblxuICBmdW5jdGlvbiBjcmVhdGVIcmVmKGxvY2F0aW9uKSB7XG4gICAgcmV0dXJuIGNyZWF0ZVBhdGgobG9jYXRpb24pO1xuICB9XG5cbiAgZnVuY3Rpb24gY3JlYXRlTG9jYXRpb24obG9jYXRpb24sIGFjdGlvbikge1xuICAgIHZhciBrZXkgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDIgfHwgYXJndW1lbnRzWzJdID09PSB1bmRlZmluZWQgPyBjcmVhdGVLZXkoKSA6IGFyZ3VtZW50c1syXTtcblxuICAgIGlmICh0eXBlb2YgYWN0aW9uID09PSAnb2JqZWN0Jykge1xuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnVGhlIHN0YXRlICgybmQpIGFyZ3VtZW50IHRvIGhpc3RvcnkuY3JlYXRlTG9jYXRpb24gaXMgZGVwcmVjYXRlZDsgdXNlIGEgJyArICdsb2NhdGlvbiBkZXNjcmlwdG9yIGluc3RlYWQnKSA6IHVuZGVmaW5lZDtcblxuICAgICAgaWYgKHR5cGVvZiBsb2NhdGlvbiA9PT0gJ3N0cmluZycpIGxvY2F0aW9uID0gX1BhdGhVdGlscy5wYXJzZVBhdGgobG9jYXRpb24pO1xuXG4gICAgICBsb2NhdGlvbiA9IF9leHRlbmRzKHt9LCBsb2NhdGlvbiwgeyBzdGF0ZTogYWN0aW9uIH0pO1xuXG4gICAgICBhY3Rpb24gPSBrZXk7XG4gICAgICBrZXkgPSBhcmd1bWVudHNbM10gfHwgY3JlYXRlS2V5KCk7XG4gICAgfVxuXG4gICAgcmV0dXJuIF9jcmVhdGVMb2NhdGlvbjNbJ2RlZmF1bHQnXShsb2NhdGlvbiwgYWN0aW9uLCBrZXkpO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiBzZXRTdGF0ZShzdGF0ZSkge1xuICAgIGlmIChsb2NhdGlvbikge1xuICAgICAgdXBkYXRlTG9jYXRpb25TdGF0ZShsb2NhdGlvbiwgc3RhdGUpO1xuICAgICAgdXBkYXRlTG9jYXRpb24obG9jYXRpb24pO1xuICAgIH0gZWxzZSB7XG4gICAgICB1cGRhdGVMb2NhdGlvblN0YXRlKGdldEN1cnJlbnRMb2NhdGlvbigpLCBzdGF0ZSk7XG4gICAgfVxuICB9XG5cbiAgZnVuY3Rpb24gdXBkYXRlTG9jYXRpb25TdGF0ZShsb2NhdGlvbiwgc3RhdGUpIHtcbiAgICBsb2NhdGlvbi5zdGF0ZSA9IF9leHRlbmRzKHt9LCBsb2NhdGlvbi5zdGF0ZSwgc3RhdGUpO1xuICAgIHNhdmVTdGF0ZShsb2NhdGlvbi5rZXksIGxvY2F0aW9uLnN0YXRlKTtcbiAgfVxuXG4gIC8vIGRlcHJlY2F0ZWRcbiAgZnVuY3Rpb24gcmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKSB7XG4gICAgaWYgKHRyYW5zaXRpb25Ib29rcy5pbmRleE9mKGhvb2spID09PSAtMSkgdHJhbnNpdGlvbkhvb2tzLnB1c2goaG9vayk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayhob29rKSB7XG4gICAgdHJhbnNpdGlvbkhvb2tzID0gdHJhbnNpdGlvbkhvb2tzLmZpbHRlcihmdW5jdGlvbiAoaXRlbSkge1xuICAgICAgcmV0dXJuIGl0ZW0gIT09IGhvb2s7XG4gICAgfSk7XG4gIH1cblxuICAvLyBkZXByZWNhdGVkXG4gIGZ1bmN0aW9uIHB1c2hTdGF0ZShzdGF0ZSwgcGF0aCkge1xuICAgIGlmICh0eXBlb2YgcGF0aCA9PT0gJ3N0cmluZycpIHBhdGggPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgIHB1c2goX2V4dGVuZHMoeyBzdGF0ZTogc3RhdGUgfSwgcGF0aCkpO1xuICB9XG5cbiAgLy8gZGVwcmVjYXRlZFxuICBmdW5jdGlvbiByZXBsYWNlU3RhdGUoc3RhdGUsIHBhdGgpIHtcbiAgICBpZiAodHlwZW9mIHBhdGggPT09ICdzdHJpbmcnKSBwYXRoID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICByZXBsYWNlKF9leHRlbmRzKHsgc3RhdGU6IHN0YXRlIH0sIHBhdGgpKTtcbiAgfVxuXG4gIHJldHVybiB7XG4gICAgbGlzdGVuQmVmb3JlOiBsaXN0ZW5CZWZvcmUsXG4gICAgbGlzdGVuOiBsaXN0ZW4sXG4gICAgdHJhbnNpdGlvblRvOiB0cmFuc2l0aW9uVG8sXG4gICAgcHVzaDogcHVzaCxcbiAgICByZXBsYWNlOiByZXBsYWNlLFxuICAgIGdvOiBnbyxcbiAgICBnb0JhY2s6IGdvQmFjayxcbiAgICBnb0ZvcndhcmQ6IGdvRm9yd2FyZCxcbiAgICBjcmVhdGVLZXk6IGNyZWF0ZUtleSxcbiAgICBjcmVhdGVQYXRoOiBjcmVhdGVQYXRoLFxuICAgIGNyZWF0ZUhyZWY6IGNyZWF0ZUhyZWYsXG4gICAgY3JlYXRlTG9jYXRpb246IGNyZWF0ZUxvY2F0aW9uLFxuXG4gICAgc2V0U3RhdGU6IF9kZXByZWNhdGUyWydkZWZhdWx0J10oc2V0U3RhdGUsICdzZXRTdGF0ZSBpcyBkZXByZWNhdGVkOyB1c2UgbG9jYXRpb24ua2V5IHRvIHNhdmUgc3RhdGUgaW5zdGVhZCcpLFxuICAgIHJlZ2lzdGVyVHJhbnNpdGlvbkhvb2s6IF9kZXByZWNhdGUyWydkZWZhdWx0J10ocmVnaXN0ZXJUcmFuc2l0aW9uSG9vaywgJ3JlZ2lzdGVyVHJhbnNpdGlvbkhvb2sgaXMgZGVwcmVjYXRlZDsgdXNlIGxpc3RlbkJlZm9yZSBpbnN0ZWFkJyksXG4gICAgdW5yZWdpc3RlclRyYW5zaXRpb25Ib29rOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHVucmVnaXN0ZXJUcmFuc2l0aW9uSG9vaywgJ3VucmVnaXN0ZXJUcmFuc2l0aW9uSG9vayBpcyBkZXByZWNhdGVkOyB1c2UgdGhlIGNhbGxiYWNrIHJldHVybmVkIGZyb20gbGlzdGVuQmVmb3JlIGluc3RlYWQnKSxcbiAgICBwdXNoU3RhdGU6IF9kZXByZWNhdGUyWydkZWZhdWx0J10ocHVzaFN0YXRlLCAncHVzaFN0YXRlIGlzIGRlcHJlY2F0ZWQ7IHVzZSBwdXNoIGluc3RlYWQnKSxcbiAgICByZXBsYWNlU3RhdGU6IF9kZXByZWNhdGUyWydkZWZhdWx0J10ocmVwbGFjZVN0YXRlLCAncmVwbGFjZVN0YXRlIGlzIGRlcHJlY2F0ZWQ7IHVzZSByZXBsYWNlIGluc3RlYWQnKVxuICB9O1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBjcmVhdGVIaXN0b3J5O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG52YXIgX0FjdGlvbnMgPSByZXF1aXJlKCcuL0FjdGlvbnMnKTtcblxudmFyIF9QYXRoVXRpbHMgPSByZXF1aXJlKCcuL1BhdGhVdGlscycpO1xuXG5mdW5jdGlvbiBjcmVhdGVMb2NhdGlvbigpIHtcbiAgdmFyIGxvY2F0aW9uID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8gJy8nIDogYXJndW1lbnRzWzBdO1xuICB2YXIgYWN0aW9uID0gYXJndW1lbnRzLmxlbmd0aCA8PSAxIHx8IGFyZ3VtZW50c1sxXSA9PT0gdW5kZWZpbmVkID8gX0FjdGlvbnMuUE9QIDogYXJndW1lbnRzWzFdO1xuICB2YXIga2V5ID0gYXJndW1lbnRzLmxlbmd0aCA8PSAyIHx8IGFyZ3VtZW50c1syXSA9PT0gdW5kZWZpbmVkID8gbnVsbCA6IGFyZ3VtZW50c1syXTtcblxuICB2YXIgX2ZvdXJ0aEFyZyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMyB8fCBhcmd1bWVudHNbM10gPT09IHVuZGVmaW5lZCA/IG51bGwgOiBhcmd1bWVudHNbM107XG5cbiAgaWYgKHR5cGVvZiBsb2NhdGlvbiA9PT0gJ3N0cmluZycpIGxvY2F0aW9uID0gX1BhdGhVdGlscy5wYXJzZVBhdGgobG9jYXRpb24pO1xuXG4gIGlmICh0eXBlb2YgYWN0aW9uID09PSAnb2JqZWN0Jykge1xuICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ1RoZSBzdGF0ZSAoMm5kKSBhcmd1bWVudCB0byBjcmVhdGVMb2NhdGlvbiBpcyBkZXByZWNhdGVkOyB1c2UgYSAnICsgJ2xvY2F0aW9uIGRlc2NyaXB0b3IgaW5zdGVhZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgbG9jYXRpb24gPSBfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIHsgc3RhdGU6IGFjdGlvbiB9KTtcblxuICAgIGFjdGlvbiA9IGtleSB8fCBfQWN0aW9ucy5QT1A7XG4gICAga2V5ID0gX2ZvdXJ0aEFyZztcbiAgfVxuXG4gIHZhciBwYXRobmFtZSA9IGxvY2F0aW9uLnBhdGhuYW1lIHx8ICcvJztcbiAgdmFyIHNlYXJjaCA9IGxvY2F0aW9uLnNlYXJjaCB8fCAnJztcbiAgdmFyIGhhc2ggPSBsb2NhdGlvbi5oYXNoIHx8ICcnO1xuICB2YXIgc3RhdGUgPSBsb2NhdGlvbi5zdGF0ZSB8fCBudWxsO1xuXG4gIHJldHVybiB7XG4gICAgcGF0aG5hbWU6IHBhdGhuYW1lLFxuICAgIHNlYXJjaDogc2VhcmNoLFxuICAgIGhhc2g6IGhhc2gsXG4gICAgc3RhdGU6IHN0YXRlLFxuICAgIGFjdGlvbjogYWN0aW9uLFxuICAgIGtleToga2V5XG4gIH07XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IGNyZWF0ZUxvY2F0aW9uO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX1BhdGhVdGlscyA9IHJlcXVpcmUoJy4vUGF0aFV0aWxzJyk7XG5cbnZhciBfQWN0aW9ucyA9IHJlcXVpcmUoJy4vQWN0aW9ucycpO1xuXG52YXIgX2NyZWF0ZUhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZUhpc3RvcnkpO1xuXG5mdW5jdGlvbiBjcmVhdGVTdGF0ZVN0b3JhZ2UoZW50cmllcykge1xuICByZXR1cm4gZW50cmllcy5maWx0ZXIoZnVuY3Rpb24gKGVudHJ5KSB7XG4gICAgcmV0dXJuIGVudHJ5LnN0YXRlO1xuICB9KS5yZWR1Y2UoZnVuY3Rpb24gKG1lbW8sIGVudHJ5KSB7XG4gICAgbWVtb1tlbnRyeS5rZXldID0gZW50cnkuc3RhdGU7XG4gICAgcmV0dXJuIG1lbW87XG4gIH0sIHt9KTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlTWVtb3J5SGlzdG9yeSgpIHtcbiAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcblxuICBpZiAoQXJyYXkuaXNBcnJheShvcHRpb25zKSkge1xuICAgIG9wdGlvbnMgPSB7IGVudHJpZXM6IG9wdGlvbnMgfTtcbiAgfSBlbHNlIGlmICh0eXBlb2Ygb3B0aW9ucyA9PT0gJ3N0cmluZycpIHtcbiAgICBvcHRpb25zID0geyBlbnRyaWVzOiBbb3B0aW9uc10gfTtcbiAgfVxuXG4gIHZhciBoaXN0b3J5ID0gX2NyZWF0ZUhpc3RvcnkyWydkZWZhdWx0J10oX2V4dGVuZHMoe30sIG9wdGlvbnMsIHtcbiAgICBnZXRDdXJyZW50TG9jYXRpb246IGdldEN1cnJlbnRMb2NhdGlvbixcbiAgICBmaW5pc2hUcmFuc2l0aW9uOiBmaW5pc2hUcmFuc2l0aW9uLFxuICAgIHNhdmVTdGF0ZTogc2F2ZVN0YXRlLFxuICAgIGdvOiBnb1xuICB9KSk7XG5cbiAgdmFyIF9vcHRpb25zID0gb3B0aW9ucztcbiAgdmFyIGVudHJpZXMgPSBfb3B0aW9ucy5lbnRyaWVzO1xuICB2YXIgY3VycmVudCA9IF9vcHRpb25zLmN1cnJlbnQ7XG5cbiAgaWYgKHR5cGVvZiBlbnRyaWVzID09PSAnc3RyaW5nJykge1xuICAgIGVudHJpZXMgPSBbZW50cmllc107XG4gIH0gZWxzZSBpZiAoIUFycmF5LmlzQXJyYXkoZW50cmllcykpIHtcbiAgICBlbnRyaWVzID0gWycvJ107XG4gIH1cblxuICBlbnRyaWVzID0gZW50cmllcy5tYXAoZnVuY3Rpb24gKGVudHJ5KSB7XG4gICAgdmFyIGtleSA9IGhpc3RvcnkuY3JlYXRlS2V5KCk7XG5cbiAgICBpZiAodHlwZW9mIGVudHJ5ID09PSAnc3RyaW5nJykgcmV0dXJuIHsgcGF0aG5hbWU6IGVudHJ5LCBrZXk6IGtleSB9O1xuXG4gICAgaWYgKHR5cGVvZiBlbnRyeSA9PT0gJ29iamVjdCcgJiYgZW50cnkpIHJldHVybiBfZXh0ZW5kcyh7fSwgZW50cnksIHsga2V5OiBrZXkgfSk7XG5cbiAgICAhZmFsc2UgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSwgJ1VuYWJsZSB0byBjcmVhdGUgaGlzdG9yeSBlbnRyeSBmcm9tICVzJywgZW50cnkpIDogX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSkgOiB1bmRlZmluZWQ7XG4gIH0pO1xuXG4gIGlmIChjdXJyZW50ID09IG51bGwpIHtcbiAgICBjdXJyZW50ID0gZW50cmllcy5sZW5ndGggLSAxO1xuICB9IGVsc2Uge1xuICAgICEoY3VycmVudCA+PSAwICYmIGN1cnJlbnQgPCBlbnRyaWVzLmxlbmd0aCkgPyBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX2ludmFyaWFudDJbJ2RlZmF1bHQnXShmYWxzZSwgJ0N1cnJlbnQgaW5kZXggbXVzdCBiZSA+PSAwIGFuZCA8ICVzLCB3YXMgJXMnLCBlbnRyaWVzLmxlbmd0aCwgY3VycmVudCkgOiBfaW52YXJpYW50MlsnZGVmYXVsdCddKGZhbHNlKSA6IHVuZGVmaW5lZDtcbiAgfVxuXG4gIHZhciBzdG9yYWdlID0gY3JlYXRlU3RhdGVTdG9yYWdlKGVudHJpZXMpO1xuXG4gIGZ1bmN0aW9uIHNhdmVTdGF0ZShrZXksIHN0YXRlKSB7XG4gICAgc3RvcmFnZVtrZXldID0gc3RhdGU7XG4gIH1cblxuICBmdW5jdGlvbiByZWFkU3RhdGUoa2V5KSB7XG4gICAgcmV0dXJuIHN0b3JhZ2Vba2V5XTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGdldEN1cnJlbnRMb2NhdGlvbigpIHtcbiAgICB2YXIgZW50cnkgPSBlbnRyaWVzW2N1cnJlbnRdO1xuICAgIHZhciBiYXNlbmFtZSA9IGVudHJ5LmJhc2VuYW1lO1xuICAgIHZhciBwYXRobmFtZSA9IGVudHJ5LnBhdGhuYW1lO1xuICAgIHZhciBzZWFyY2ggPSBlbnRyeS5zZWFyY2g7XG5cbiAgICB2YXIgcGF0aCA9IChiYXNlbmFtZSB8fCAnJykgKyBwYXRobmFtZSArIChzZWFyY2ggfHwgJycpO1xuXG4gICAgdmFyIGtleSA9IHVuZGVmaW5lZCxcbiAgICAgICAgc3RhdGUgPSB1bmRlZmluZWQ7XG4gICAgaWYgKGVudHJ5LmtleSkge1xuICAgICAga2V5ID0gZW50cnkua2V5O1xuICAgICAgc3RhdGUgPSByZWFkU3RhdGUoa2V5KTtcbiAgICB9IGVsc2Uge1xuICAgICAga2V5ID0gaGlzdG9yeS5jcmVhdGVLZXkoKTtcbiAgICAgIHN0YXRlID0gbnVsbDtcbiAgICAgIGVudHJ5LmtleSA9IGtleTtcbiAgICB9XG5cbiAgICB2YXIgbG9jYXRpb24gPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uKF9leHRlbmRzKHt9LCBsb2NhdGlvbiwgeyBzdGF0ZTogc3RhdGUgfSksIHVuZGVmaW5lZCwga2V5KTtcbiAgfVxuXG4gIGZ1bmN0aW9uIGNhbkdvKG4pIHtcbiAgICB2YXIgaW5kZXggPSBjdXJyZW50ICsgbjtcbiAgICByZXR1cm4gaW5kZXggPj0gMCAmJiBpbmRleCA8IGVudHJpZXMubGVuZ3RoO1xuICB9XG5cbiAgZnVuY3Rpb24gZ28obikge1xuICAgIGlmIChuKSB7XG4gICAgICBpZiAoIWNhbkdvKG4pKSB7XG4gICAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShmYWxzZSwgJ0Nhbm5vdCBnbyglcykgdGhlcmUgaXMgbm90IGVub3VnaCBoaXN0b3J5JywgbikgOiB1bmRlZmluZWQ7XG4gICAgICAgIHJldHVybjtcbiAgICAgIH1cblxuICAgICAgY3VycmVudCArPSBuO1xuXG4gICAgICB2YXIgY3VycmVudExvY2F0aW9uID0gZ2V0Q3VycmVudExvY2F0aW9uKCk7XG5cbiAgICAgIC8vIGNoYW5nZSBhY3Rpb24gdG8gUE9QXG4gICAgICBoaXN0b3J5LnRyYW5zaXRpb25UbyhfZXh0ZW5kcyh7fSwgY3VycmVudExvY2F0aW9uLCB7IGFjdGlvbjogX0FjdGlvbnMuUE9QIH0pKTtcbiAgICB9XG4gIH1cblxuICBmdW5jdGlvbiBmaW5pc2hUcmFuc2l0aW9uKGxvY2F0aW9uKSB7XG4gICAgc3dpdGNoIChsb2NhdGlvbi5hY3Rpb24pIHtcbiAgICAgIGNhc2UgX0FjdGlvbnMuUFVTSDpcbiAgICAgICAgY3VycmVudCArPSAxO1xuXG4gICAgICAgIC8vIGlmIHdlIGFyZSBub3Qgb24gdGhlIHRvcCBvZiBzdGFja1xuICAgICAgICAvLyByZW1vdmUgcmVzdCBhbmQgcHVzaCBuZXdcbiAgICAgICAgaWYgKGN1cnJlbnQgPCBlbnRyaWVzLmxlbmd0aCkgZW50cmllcy5zcGxpY2UoY3VycmVudCk7XG5cbiAgICAgICAgZW50cmllcy5wdXNoKGxvY2F0aW9uKTtcbiAgICAgICAgc2F2ZVN0YXRlKGxvY2F0aW9uLmtleSwgbG9jYXRpb24uc3RhdGUpO1xuICAgICAgICBicmVhaztcbiAgICAgIGNhc2UgX0FjdGlvbnMuUkVQTEFDRTpcbiAgICAgICAgZW50cmllc1tjdXJyZW50XSA9IGxvY2F0aW9uO1xuICAgICAgICBzYXZlU3RhdGUobG9jYXRpb24ua2V5LCBsb2NhdGlvbi5zdGF0ZSk7XG4gICAgICAgIGJyZWFrO1xuICAgIH1cbiAgfVxuXG4gIHJldHVybiBoaXN0b3J5O1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBjcmVhdGVNZW1vcnlIaXN0b3J5O1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyAnZGVmYXVsdCc6IG9iaiB9OyB9XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBkZXByZWNhdGUoZm4sIG1lc3NhZ2UpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oZmFsc2UsICdbaGlzdG9yeV0gJyArIG1lc3NhZ2UpIDogdW5kZWZpbmVkO1xuICAgIHJldHVybiBmbi5hcHBseSh0aGlzLCBhcmd1bWVudHMpO1xuICB9O1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBkZXByZWNhdGU7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7ICdkZWZhdWx0Jzogb2JqIH07IH1cblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbmZ1bmN0aW9uIHJ1blRyYW5zaXRpb25Ib29rKGhvb2ssIGxvY2F0aW9uLCBjYWxsYmFjaykge1xuICB2YXIgcmVzdWx0ID0gaG9vayhsb2NhdGlvbiwgY2FsbGJhY2spO1xuXG4gIGlmIChob29rLmxlbmd0aCA8IDIpIHtcbiAgICAvLyBBc3N1bWUgdGhlIGhvb2sgcnVucyBzeW5jaHJvbm91c2x5IGFuZCBhdXRvbWF0aWNhbGx5XG4gICAgLy8gY2FsbCB0aGUgY2FsbGJhY2sgd2l0aCB0aGUgcmV0dXJuIHZhbHVlLlxuICAgIGNhbGxiYWNrKHJlc3VsdCk7XG4gIH0gZWxzZSB7XG4gICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKHJlc3VsdCA9PT0gdW5kZWZpbmVkLCAnWW91IHNob3VsZCBub3QgXCJyZXR1cm5cIiBpbiBhIHRyYW5zaXRpb24gaG9vayB3aXRoIGEgY2FsbGJhY2sgYXJndW1lbnQ7IGNhbGwgdGhlIGNhbGxiYWNrIGluc3RlYWQnKSA6IHVuZGVmaW5lZDtcbiAgfVxufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSBydW5UcmFuc2l0aW9uSG9vaztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIF9FeGVjdXRpb25FbnZpcm9ubWVudCA9IHJlcXVpcmUoJy4vRXhlY3V0aW9uRW52aXJvbm1lbnQnKTtcblxudmFyIF9QYXRoVXRpbHMgPSByZXF1aXJlKCcuL1BhdGhVdGlscycpO1xuXG52YXIgX3J1blRyYW5zaXRpb25Ib29rID0gcmVxdWlyZSgnLi9ydW5UcmFuc2l0aW9uSG9vaycpO1xuXG52YXIgX3J1blRyYW5zaXRpb25Ib29rMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3J1blRyYW5zaXRpb25Ib29rKTtcblxudmFyIF9kZXByZWNhdGUgPSByZXF1aXJlKCcuL2RlcHJlY2F0ZScpO1xuXG52YXIgX2RlcHJlY2F0ZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9kZXByZWNhdGUpO1xuXG5mdW5jdGlvbiB1c2VCYXNlbmFtZShjcmVhdGVIaXN0b3J5KSB7XG4gIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcblxuICAgIHZhciBoaXN0b3J5ID0gY3JlYXRlSGlzdG9yeShvcHRpb25zKTtcblxuICAgIHZhciBiYXNlbmFtZSA9IG9wdGlvbnMuYmFzZW5hbWU7XG5cbiAgICB2YXIgY2hlY2tlZEJhc2VIcmVmID0gZmFsc2U7XG5cbiAgICBmdW5jdGlvbiBjaGVja0Jhc2VIcmVmKCkge1xuICAgICAgaWYgKGNoZWNrZWRCYXNlSHJlZikge1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG5cbiAgICAgIC8vIEF1dG9tYXRpY2FsbHkgdXNlIHRoZSB2YWx1ZSBvZiA8YmFzZSBocmVmPiBpbiBIVE1MXG4gICAgICAvLyBkb2N1bWVudHMgYXMgYmFzZW5hbWUgaWYgaXQncyBub3QgZXhwbGljaXRseSBnaXZlbi5cbiAgICAgIGlmIChiYXNlbmFtZSA9PSBudWxsICYmIF9FeGVjdXRpb25FbnZpcm9ubWVudC5jYW5Vc2VET00pIHtcbiAgICAgICAgdmFyIGJhc2UgPSBkb2N1bWVudC5nZXRFbGVtZW50c0J5VGFnTmFtZSgnYmFzZScpWzBdO1xuICAgICAgICB2YXIgYmFzZUhyZWYgPSBiYXNlICYmIGJhc2UuZ2V0QXR0cmlidXRlKCdocmVmJyk7XG5cbiAgICAgICAgaWYgKGJhc2VIcmVmICE9IG51bGwpIHtcbiAgICAgICAgICBiYXNlbmFtZSA9IGJhc2VIcmVmO1xuXG4gICAgICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKGZhbHNlLCAnQXV0b21hdGljYWxseSBzZXR0aW5nIGJhc2VuYW1lIHVzaW5nIDxiYXNlIGhyZWY+IGlzIGRlcHJlY2F0ZWQgYW5kIHdpbGwgJyArICdiZSByZW1vdmVkIGluIHRoZSBuZXh0IG1ham9yIHJlbGVhc2UuIFRoZSBzZW1hbnRpY3Mgb2YgPGJhc2UgaHJlZj4gYXJlICcgKyAnc3VidGx5IGRpZmZlcmVudCBmcm9tIGJhc2VuYW1lLiBQbGVhc2UgcGFzcyB0aGUgYmFzZW5hbWUgZXhwbGljaXRseSBpbiAnICsgJ3RoZSBvcHRpb25zIHRvIGNyZWF0ZUhpc3RvcnknKSA6IHVuZGVmaW5lZDtcbiAgICAgICAgfVxuICAgICAgfVxuXG4gICAgICBjaGVja2VkQmFzZUhyZWYgPSB0cnVlO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGFkZEJhc2VuYW1lKGxvY2F0aW9uKSB7XG4gICAgICBjaGVja0Jhc2VIcmVmKCk7XG5cbiAgICAgIGlmIChiYXNlbmFtZSAmJiBsb2NhdGlvbi5iYXNlbmFtZSA9PSBudWxsKSB7XG4gICAgICAgIGlmIChsb2NhdGlvbi5wYXRobmFtZS5pbmRleE9mKGJhc2VuYW1lKSA9PT0gMCkge1xuICAgICAgICAgIGxvY2F0aW9uLnBhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWUuc3Vic3RyaW5nKGJhc2VuYW1lLmxlbmd0aCk7XG4gICAgICAgICAgbG9jYXRpb24uYmFzZW5hbWUgPSBiYXNlbmFtZTtcblxuICAgICAgICAgIGlmIChsb2NhdGlvbi5wYXRobmFtZSA9PT0gJycpIGxvY2F0aW9uLnBhdGhuYW1lID0gJy8nO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIGxvY2F0aW9uLmJhc2VuYW1lID0gJyc7XG4gICAgICAgIH1cbiAgICAgIH1cblxuICAgICAgcmV0dXJuIGxvY2F0aW9uO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIHByZXBlbmRCYXNlbmFtZShsb2NhdGlvbikge1xuICAgICAgY2hlY2tCYXNlSHJlZigpO1xuXG4gICAgICBpZiAoIWJhc2VuYW1lKSByZXR1cm4gbG9jYXRpb247XG5cbiAgICAgIGlmICh0eXBlb2YgbG9jYXRpb24gPT09ICdzdHJpbmcnKSBsb2NhdGlvbiA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKGxvY2F0aW9uKTtcblxuICAgICAgdmFyIHBuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gICAgICB2YXIgbm9ybWFsaXplZEJhc2VuYW1lID0gYmFzZW5hbWUuc2xpY2UoLTEpID09PSAnLycgPyBiYXNlbmFtZSA6IGJhc2VuYW1lICsgJy8nO1xuICAgICAgdmFyIG5vcm1hbGl6ZWRQYXRobmFtZSA9IHBuYW1lLmNoYXJBdCgwKSA9PT0gJy8nID8gcG5hbWUuc2xpY2UoMSkgOiBwbmFtZTtcbiAgICAgIHZhciBwYXRobmFtZSA9IG5vcm1hbGl6ZWRCYXNlbmFtZSArIG5vcm1hbGl6ZWRQYXRobmFtZTtcblxuICAgICAgcmV0dXJuIF9leHRlbmRzKHt9LCBsb2NhdGlvbiwge1xuICAgICAgICBwYXRobmFtZTogcGF0aG5hbWVcbiAgICAgIH0pO1xuICAgIH1cblxuICAgIC8vIE92ZXJyaWRlIGFsbCByZWFkIG1ldGhvZHMgd2l0aCBiYXNlbmFtZS1hd2FyZSB2ZXJzaW9ucy5cbiAgICBmdW5jdGlvbiBsaXN0ZW5CZWZvcmUoaG9vaykge1xuICAgICAgcmV0dXJuIGhpc3RvcnkubGlzdGVuQmVmb3JlKGZ1bmN0aW9uIChsb2NhdGlvbiwgY2FsbGJhY2spIHtcbiAgICAgICAgX3J1blRyYW5zaXRpb25Ib29rMlsnZGVmYXVsdCddKGhvb2ssIGFkZEJhc2VuYW1lKGxvY2F0aW9uKSwgY2FsbGJhY2spO1xuICAgICAgfSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gbGlzdGVuKGxpc3RlbmVyKSB7XG4gICAgICByZXR1cm4gaGlzdG9yeS5saXN0ZW4oZnVuY3Rpb24gKGxvY2F0aW9uKSB7XG4gICAgICAgIGxpc3RlbmVyKGFkZEJhc2VuYW1lKGxvY2F0aW9uKSk7XG4gICAgICB9KTtcbiAgICB9XG5cbiAgICAvLyBPdmVycmlkZSBhbGwgd3JpdGUgbWV0aG9kcyB3aXRoIGJhc2VuYW1lLWF3YXJlIHZlcnNpb25zLlxuICAgIGZ1bmN0aW9uIHB1c2gobG9jYXRpb24pIHtcbiAgICAgIGhpc3RvcnkucHVzaChwcmVwZW5kQmFzZW5hbWUobG9jYXRpb24pKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiByZXBsYWNlKGxvY2F0aW9uKSB7XG4gICAgICBoaXN0b3J5LnJlcGxhY2UocHJlcGVuZEJhc2VuYW1lKGxvY2F0aW9uKSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gY3JlYXRlUGF0aChsb2NhdGlvbikge1xuICAgICAgcmV0dXJuIGhpc3RvcnkuY3JlYXRlUGF0aChwcmVwZW5kQmFzZW5hbWUobG9jYXRpb24pKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjcmVhdGVIcmVmKGxvY2F0aW9uKSB7XG4gICAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVIcmVmKHByZXBlbmRCYXNlbmFtZShsb2NhdGlvbikpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGNyZWF0ZUxvY2F0aW9uKGxvY2F0aW9uKSB7XG4gICAgICBmb3IgKHZhciBfbGVuID0gYXJndW1lbnRzLmxlbmd0aCwgYXJncyA9IEFycmF5KF9sZW4gPiAxID8gX2xlbiAtIDEgOiAwKSwgX2tleSA9IDE7IF9rZXkgPCBfbGVuOyBfa2V5KyspIHtcbiAgICAgICAgYXJnc1tfa2V5IC0gMV0gPSBhcmd1bWVudHNbX2tleV07XG4gICAgICB9XG5cbiAgICAgIHJldHVybiBhZGRCYXNlbmFtZShoaXN0b3J5LmNyZWF0ZUxvY2F0aW9uLmFwcGx5KGhpc3RvcnksIFtwcmVwZW5kQmFzZW5hbWUobG9jYXRpb24pXS5jb25jYXQoYXJncykpKTtcbiAgICB9XG5cbiAgICAvLyBkZXByZWNhdGVkXG4gICAgZnVuY3Rpb24gcHVzaFN0YXRlKHN0YXRlLCBwYXRoKSB7XG4gICAgICBpZiAodHlwZW9mIHBhdGggPT09ICdzdHJpbmcnKSBwYXRoID0gX1BhdGhVdGlscy5wYXJzZVBhdGgocGF0aCk7XG5cbiAgICAgIHB1c2goX2V4dGVuZHMoeyBzdGF0ZTogc3RhdGUgfSwgcGF0aCkpO1xuICAgIH1cblxuICAgIC8vIGRlcHJlY2F0ZWRcbiAgICBmdW5jdGlvbiByZXBsYWNlU3RhdGUoc3RhdGUsIHBhdGgpIHtcbiAgICAgIGlmICh0eXBlb2YgcGF0aCA9PT0gJ3N0cmluZycpIHBhdGggPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChwYXRoKTtcblxuICAgICAgcmVwbGFjZShfZXh0ZW5kcyh7IHN0YXRlOiBzdGF0ZSB9LCBwYXRoKSk7XG4gICAgfVxuXG4gICAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB7XG4gICAgICBsaXN0ZW5CZWZvcmU6IGxpc3RlbkJlZm9yZSxcbiAgICAgIGxpc3RlbjogbGlzdGVuLFxuICAgICAgcHVzaDogcHVzaCxcbiAgICAgIHJlcGxhY2U6IHJlcGxhY2UsXG4gICAgICBjcmVhdGVQYXRoOiBjcmVhdGVQYXRoLFxuICAgICAgY3JlYXRlSHJlZjogY3JlYXRlSHJlZixcbiAgICAgIGNyZWF0ZUxvY2F0aW9uOiBjcmVhdGVMb2NhdGlvbixcblxuICAgICAgcHVzaFN0YXRlOiBfZGVwcmVjYXRlMlsnZGVmYXVsdCddKHB1c2hTdGF0ZSwgJ3B1c2hTdGF0ZSBpcyBkZXByZWNhdGVkOyB1c2UgcHVzaCBpbnN0ZWFkJyksXG4gICAgICByZXBsYWNlU3RhdGU6IF9kZXByZWNhdGUyWydkZWZhdWx0J10ocmVwbGFjZVN0YXRlLCAncmVwbGFjZVN0YXRlIGlzIGRlcHJlY2F0ZWQ7IHVzZSByZXBsYWNlIGluc3RlYWQnKVxuICAgIH0pO1xuICB9O1xufVxuXG5leHBvcnRzWydkZWZhdWx0J10gPSB1c2VCYXNlbmFtZTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgJ2RlZmF1bHQnOiBvYmogfTsgfVxuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCd3YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxudmFyIF9xdWVyeVN0cmluZyA9IHJlcXVpcmUoJ3F1ZXJ5LXN0cmluZycpO1xuXG52YXIgX3J1blRyYW5zaXRpb25Ib29rID0gcmVxdWlyZSgnLi9ydW5UcmFuc2l0aW9uSG9vaycpO1xuXG52YXIgX3J1blRyYW5zaXRpb25Ib29rMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3J1blRyYW5zaXRpb25Ib29rKTtcblxudmFyIF9QYXRoVXRpbHMgPSByZXF1aXJlKCcuL1BhdGhVdGlscycpO1xuXG52YXIgX2RlcHJlY2F0ZSA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlJyk7XG5cbnZhciBfZGVwcmVjYXRlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2RlcHJlY2F0ZSk7XG5cbnZhciBTRUFSQ0hfQkFTRV9LRVkgPSAnJHNlYXJjaEJhc2UnO1xuXG5mdW5jdGlvbiBkZWZhdWx0U3RyaW5naWZ5UXVlcnkocXVlcnkpIHtcbiAgcmV0dXJuIF9xdWVyeVN0cmluZy5zdHJpbmdpZnkocXVlcnkpLnJlcGxhY2UoLyUyMC9nLCAnKycpO1xufVxuXG52YXIgZGVmYXVsdFBhcnNlUXVlcnlTdHJpbmcgPSBfcXVlcnlTdHJpbmcucGFyc2U7XG5cbmZ1bmN0aW9uIGlzTmVzdGVkT2JqZWN0KG9iamVjdCkge1xuICBmb3IgKHZhciBwIGluIG9iamVjdCkge1xuICAgIGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqZWN0LCBwKSAmJiB0eXBlb2Ygb2JqZWN0W3BdID09PSAnb2JqZWN0JyAmJiAhQXJyYXkuaXNBcnJheShvYmplY3RbcF0pICYmIG9iamVjdFtwXSAhPT0gbnVsbCkgcmV0dXJuIHRydWU7XG4gIH1yZXR1cm4gZmFsc2U7XG59XG5cbi8qKlxuICogUmV0dXJucyBhIG5ldyBjcmVhdGVIaXN0b3J5IGZ1bmN0aW9uIHRoYXQgbWF5IGJlIHVzZWQgdG8gY3JlYXRlXG4gKiBoaXN0b3J5IG9iamVjdHMgdGhhdCBrbm93IGhvdyB0byBoYW5kbGUgVVJMIHF1ZXJpZXMuXG4gKi9cbmZ1bmN0aW9uIHVzZVF1ZXJpZXMoY3JlYXRlSGlzdG9yeSkge1xuICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8ge30gOiBhcmd1bWVudHNbMF07XG5cbiAgICB2YXIgaGlzdG9yeSA9IGNyZWF0ZUhpc3Rvcnkob3B0aW9ucyk7XG5cbiAgICB2YXIgc3RyaW5naWZ5UXVlcnkgPSBvcHRpb25zLnN0cmluZ2lmeVF1ZXJ5O1xuICAgIHZhciBwYXJzZVF1ZXJ5U3RyaW5nID0gb3B0aW9ucy5wYXJzZVF1ZXJ5U3RyaW5nO1xuXG4gICAgaWYgKHR5cGVvZiBzdHJpbmdpZnlRdWVyeSAhPT0gJ2Z1bmN0aW9uJykgc3RyaW5naWZ5UXVlcnkgPSBkZWZhdWx0U3RyaW5naWZ5UXVlcnk7XG5cbiAgICBpZiAodHlwZW9mIHBhcnNlUXVlcnlTdHJpbmcgIT09ICdmdW5jdGlvbicpIHBhcnNlUXVlcnlTdHJpbmcgPSBkZWZhdWx0UGFyc2VRdWVyeVN0cmluZztcblxuICAgIGZ1bmN0aW9uIGFkZFF1ZXJ5KGxvY2F0aW9uKSB7XG4gICAgICBpZiAobG9jYXRpb24ucXVlcnkgPT0gbnVsbCkge1xuICAgICAgICB2YXIgc2VhcmNoID0gbG9jYXRpb24uc2VhcmNoO1xuXG4gICAgICAgIGxvY2F0aW9uLnF1ZXJ5ID0gcGFyc2VRdWVyeVN0cmluZyhzZWFyY2guc3Vic3RyaW5nKDEpKTtcbiAgICAgICAgbG9jYXRpb25bU0VBUkNIX0JBU0VfS0VZXSA9IHsgc2VhcmNoOiBzZWFyY2gsIHNlYXJjaEJhc2U6ICcnIH07XG4gICAgICB9XG5cbiAgICAgIC8vIFRPRE86IEluc3RlYWQgb2YgYWxsIHRoZSBib29rLWtlZXBpbmcgaGVyZSwgdGhpcyBzaG91bGQganVzdCBzdHJpcCB0aGVcbiAgICAgIC8vIHN0cmluZ2lmaWVkIHF1ZXJ5IGZyb20gdGhlIHNlYXJjaC5cblxuICAgICAgcmV0dXJuIGxvY2F0aW9uO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGFwcGVuZFF1ZXJ5KGxvY2F0aW9uLCBxdWVyeSkge1xuICAgICAgdmFyIF9leHRlbmRzMjtcblxuICAgICAgdmFyIHNlYXJjaEJhc2VTcGVjID0gbG9jYXRpb25bU0VBUkNIX0JBU0VfS0VZXTtcbiAgICAgIHZhciBxdWVyeVN0cmluZyA9IHF1ZXJ5ID8gc3RyaW5naWZ5UXVlcnkocXVlcnkpIDogJyc7XG4gICAgICBpZiAoIXNlYXJjaEJhc2VTcGVjICYmICFxdWVyeVN0cmluZykge1xuICAgICAgICByZXR1cm4gbG9jYXRpb247XG4gICAgICB9XG5cbiAgICAgIHByb2Nlc3MuZW52Lk5PREVfRU5WICE9PSAncHJvZHVjdGlvbicgPyBfd2FybmluZzJbJ2RlZmF1bHQnXShzdHJpbmdpZnlRdWVyeSAhPT0gZGVmYXVsdFN0cmluZ2lmeVF1ZXJ5IHx8ICFpc05lc3RlZE9iamVjdChxdWVyeSksICd1c2VRdWVyaWVzIGRvZXMgbm90IHN0cmluZ2lmeSBuZXN0ZWQgcXVlcnkgb2JqZWN0cyBieSBkZWZhdWx0OyAnICsgJ3VzZSBhIGN1c3RvbSBzdHJpbmdpZnlRdWVyeSBmdW5jdGlvbicpIDogdW5kZWZpbmVkO1xuXG4gICAgICBpZiAodHlwZW9mIGxvY2F0aW9uID09PSAnc3RyaW5nJykgbG9jYXRpb24gPSBfUGF0aFV0aWxzLnBhcnNlUGF0aChsb2NhdGlvbik7XG5cbiAgICAgIHZhciBzZWFyY2hCYXNlID0gdW5kZWZpbmVkO1xuICAgICAgaWYgKHNlYXJjaEJhc2VTcGVjICYmIGxvY2F0aW9uLnNlYXJjaCA9PT0gc2VhcmNoQmFzZVNwZWMuc2VhcmNoKSB7XG4gICAgICAgIHNlYXJjaEJhc2UgPSBzZWFyY2hCYXNlU3BlYy5zZWFyY2hCYXNlO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgc2VhcmNoQmFzZSA9IGxvY2F0aW9uLnNlYXJjaCB8fCAnJztcbiAgICAgIH1cblxuICAgICAgdmFyIHNlYXJjaCA9IHNlYXJjaEJhc2U7XG4gICAgICBpZiAocXVlcnlTdHJpbmcpIHtcbiAgICAgICAgc2VhcmNoICs9IChzZWFyY2ggPyAnJicgOiAnPycpICsgcXVlcnlTdHJpbmc7XG4gICAgICB9XG5cbiAgICAgIHJldHVybiBfZXh0ZW5kcyh7fSwgbG9jYXRpb24sIChfZXh0ZW5kczIgPSB7XG4gICAgICAgIHNlYXJjaDogc2VhcmNoXG4gICAgICB9LCBfZXh0ZW5kczJbU0VBUkNIX0JBU0VfS0VZXSA9IHsgc2VhcmNoOiBzZWFyY2gsIHNlYXJjaEJhc2U6IHNlYXJjaEJhc2UgfSwgX2V4dGVuZHMyKSk7XG4gICAgfVxuXG4gICAgLy8gT3ZlcnJpZGUgYWxsIHJlYWQgbWV0aG9kcyB3aXRoIHF1ZXJ5LWF3YXJlIHZlcnNpb25zLlxuICAgIGZ1bmN0aW9uIGxpc3RlbkJlZm9yZShob29rKSB7XG4gICAgICByZXR1cm4gaGlzdG9yeS5saXN0ZW5CZWZvcmUoZnVuY3Rpb24gKGxvY2F0aW9uLCBjYWxsYmFjaykge1xuICAgICAgICBfcnVuVHJhbnNpdGlvbkhvb2syWydkZWZhdWx0J10oaG9vaywgYWRkUXVlcnkobG9jYXRpb24pLCBjYWxsYmFjayk7XG4gICAgICB9KTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBsaXN0ZW4obGlzdGVuZXIpIHtcbiAgICAgIHJldHVybiBoaXN0b3J5Lmxpc3RlbihmdW5jdGlvbiAobG9jYXRpb24pIHtcbiAgICAgICAgbGlzdGVuZXIoYWRkUXVlcnkobG9jYXRpb24pKTtcbiAgICAgIH0pO1xuICAgIH1cblxuICAgIC8vIE92ZXJyaWRlIGFsbCB3cml0ZSBtZXRob2RzIHdpdGggcXVlcnktYXdhcmUgdmVyc2lvbnMuXG4gICAgZnVuY3Rpb24gcHVzaChsb2NhdGlvbikge1xuICAgICAgaGlzdG9yeS5wdXNoKGFwcGVuZFF1ZXJ5KGxvY2F0aW9uLCBsb2NhdGlvbi5xdWVyeSkpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIHJlcGxhY2UobG9jYXRpb24pIHtcbiAgICAgIGhpc3RvcnkucmVwbGFjZShhcHBlbmRRdWVyeShsb2NhdGlvbiwgbG9jYXRpb24ucXVlcnkpKTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjcmVhdGVQYXRoKGxvY2F0aW9uLCBxdWVyeSkge1xuICAgICAgcHJvY2Vzcy5lbnYuTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJyA/IF93YXJuaW5nMlsnZGVmYXVsdCddKCFxdWVyeSwgJ3RoZSBxdWVyeSBhcmd1bWVudCB0byBjcmVhdGVQYXRoIGlzIGRlcHJlY2F0ZWQ7IHVzZSBhIGxvY2F0aW9uIGRlc2NyaXB0b3IgaW5zdGVhZCcpIDogdW5kZWZpbmVkO1xuXG4gICAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVQYXRoKGFwcGVuZFF1ZXJ5KGxvY2F0aW9uLCBxdWVyeSB8fCBsb2NhdGlvbi5xdWVyeSkpO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIGNyZWF0ZUhyZWYobG9jYXRpb24sIHF1ZXJ5KSB7XG4gICAgICBwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nID8gX3dhcm5pbmcyWydkZWZhdWx0J10oIXF1ZXJ5LCAndGhlIHF1ZXJ5IGFyZ3VtZW50IHRvIGNyZWF0ZUhyZWYgaXMgZGVwcmVjYXRlZDsgdXNlIGEgbG9jYXRpb24gZGVzY3JpcHRvciBpbnN0ZWFkJykgOiB1bmRlZmluZWQ7XG5cbiAgICAgIHJldHVybiBoaXN0b3J5LmNyZWF0ZUhyZWYoYXBwZW5kUXVlcnkobG9jYXRpb24sIHF1ZXJ5IHx8IGxvY2F0aW9uLnF1ZXJ5KSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gY3JlYXRlTG9jYXRpb24obG9jYXRpb24pIHtcbiAgICAgIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBhcmdzID0gQXJyYXkoX2xlbiA+IDEgPyBfbGVuIC0gMSA6IDApLCBfa2V5ID0gMTsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgICAgICBhcmdzW19rZXkgLSAxXSA9IGFyZ3VtZW50c1tfa2V5XTtcbiAgICAgIH1cblxuICAgICAgdmFyIGZ1bGxMb2NhdGlvbiA9IGhpc3RvcnkuY3JlYXRlTG9jYXRpb24uYXBwbHkoaGlzdG9yeSwgW2FwcGVuZFF1ZXJ5KGxvY2F0aW9uLCBsb2NhdGlvbi5xdWVyeSldLmNvbmNhdChhcmdzKSk7XG4gICAgICBpZiAobG9jYXRpb24ucXVlcnkpIHtcbiAgICAgICAgZnVsbExvY2F0aW9uLnF1ZXJ5ID0gbG9jYXRpb24ucXVlcnk7XG4gICAgICB9XG4gICAgICByZXR1cm4gYWRkUXVlcnkoZnVsbExvY2F0aW9uKTtcbiAgICB9XG5cbiAgICAvLyBkZXByZWNhdGVkXG4gICAgZnVuY3Rpb24gcHVzaFN0YXRlKHN0YXRlLCBwYXRoLCBxdWVyeSkge1xuICAgICAgaWYgKHR5cGVvZiBwYXRoID09PSAnc3RyaW5nJykgcGF0aCA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgICBwdXNoKF9leHRlbmRzKHsgc3RhdGU6IHN0YXRlIH0sIHBhdGgsIHsgcXVlcnk6IHF1ZXJ5IH0pKTtcbiAgICB9XG5cbiAgICAvLyBkZXByZWNhdGVkXG4gICAgZnVuY3Rpb24gcmVwbGFjZVN0YXRlKHN0YXRlLCBwYXRoLCBxdWVyeSkge1xuICAgICAgaWYgKHR5cGVvZiBwYXRoID09PSAnc3RyaW5nJykgcGF0aCA9IF9QYXRoVXRpbHMucGFyc2VQYXRoKHBhdGgpO1xuXG4gICAgICByZXBsYWNlKF9leHRlbmRzKHsgc3RhdGU6IHN0YXRlIH0sIHBhdGgsIHsgcXVlcnk6IHF1ZXJ5IH0pKTtcbiAgICB9XG5cbiAgICByZXR1cm4gX2V4dGVuZHMoe30sIGhpc3RvcnksIHtcbiAgICAgIGxpc3RlbkJlZm9yZTogbGlzdGVuQmVmb3JlLFxuICAgICAgbGlzdGVuOiBsaXN0ZW4sXG4gICAgICBwdXNoOiBwdXNoLFxuICAgICAgcmVwbGFjZTogcmVwbGFjZSxcbiAgICAgIGNyZWF0ZVBhdGg6IGNyZWF0ZVBhdGgsXG4gICAgICBjcmVhdGVIcmVmOiBjcmVhdGVIcmVmLFxuICAgICAgY3JlYXRlTG9jYXRpb246IGNyZWF0ZUxvY2F0aW9uLFxuXG4gICAgICBwdXNoU3RhdGU6IF9kZXByZWNhdGUyWydkZWZhdWx0J10ocHVzaFN0YXRlLCAncHVzaFN0YXRlIGlzIGRlcHJlY2F0ZWQ7IHVzZSBwdXNoIGluc3RlYWQnKSxcbiAgICAgIHJlcGxhY2VTdGF0ZTogX2RlcHJlY2F0ZTJbJ2RlZmF1bHQnXShyZXBsYWNlU3RhdGUsICdyZXBsYWNlU3RhdGUgaXMgZGVwcmVjYXRlZDsgdXNlIHJlcGxhY2UgaW5zdGVhZCcpXG4gICAgfSk7XG4gIH07XG59XG5cbmV4cG9ydHNbJ2RlZmF1bHQnXSA9IHVzZVF1ZXJpZXM7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIvKipcbiAqIENvcHlyaWdodCAyMDE0LTIwMTUsIEZhY2Vib29rLCBJbmMuXG4gKiBBbGwgcmlnaHRzIHJlc2VydmVkLlxuICpcbiAqIFRoaXMgc291cmNlIGNvZGUgaXMgbGljZW5zZWQgdW5kZXIgdGhlIEJTRC1zdHlsZSBsaWNlbnNlIGZvdW5kIGluIHRoZVxuICogTElDRU5TRSBmaWxlIGluIHRoZSByb290IGRpcmVjdG9yeSBvZiB0aGlzIHNvdXJjZSB0cmVlLiBBbiBhZGRpdGlvbmFsIGdyYW50XG4gKiBvZiBwYXRlbnQgcmlnaHRzIGNhbiBiZSBmb3VuZCBpbiB0aGUgUEFURU5UUyBmaWxlIGluIHRoZSBzYW1lIGRpcmVjdG9yeS5cbiAqL1xuXG4ndXNlIHN0cmljdCc7XG5cbi8qKlxuICogU2ltaWxhciB0byBpbnZhcmlhbnQgYnV0IG9ubHkgbG9ncyBhIHdhcm5pbmcgaWYgdGhlIGNvbmRpdGlvbiBpcyBub3QgbWV0LlxuICogVGhpcyBjYW4gYmUgdXNlZCB0byBsb2cgaXNzdWVzIGluIGRldmVsb3BtZW50IGVudmlyb25tZW50cyBpbiBjcml0aWNhbFxuICogcGF0aHMuIFJlbW92aW5nIHRoZSBsb2dnaW5nIGNvZGUgZm9yIHByb2R1Y3Rpb24gZW52aXJvbm1lbnRzIHdpbGwga2VlcCB0aGVcbiAqIHNhbWUgbG9naWMgYW5kIGZvbGxvdyB0aGUgc2FtZSBjb2RlIHBhdGhzLlxuICovXG5cbnZhciB3YXJuaW5nID0gZnVuY3Rpb24oKSB7fTtcblxuaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicpIHtcbiAgd2FybmluZyA9IGZ1bmN0aW9uKGNvbmRpdGlvbiwgZm9ybWF0LCBhcmdzKSB7XG4gICAgdmFyIGxlbiA9IGFyZ3VtZW50cy5sZW5ndGg7XG4gICAgYXJncyA9IG5ldyBBcnJheShsZW4gPiAyID8gbGVuIC0gMiA6IDApO1xuICAgIGZvciAodmFyIGtleSA9IDI7IGtleSA8IGxlbjsga2V5KyspIHtcbiAgICAgIGFyZ3Nba2V5IC0gMl0gPSBhcmd1bWVudHNba2V5XTtcbiAgICB9XG4gICAgaWYgKGZvcm1hdCA9PT0gdW5kZWZpbmVkKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgICdgd2FybmluZyhjb25kaXRpb24sIGZvcm1hdCwgLi4uYXJncylgIHJlcXVpcmVzIGEgd2FybmluZyAnICtcbiAgICAgICAgJ21lc3NhZ2UgYXJndW1lbnQnXG4gICAgICApO1xuICAgIH1cblxuICAgIGlmIChmb3JtYXQubGVuZ3RoIDwgMTAgfHwgKC9eW3NcXFddKiQvKS50ZXN0KGZvcm1hdCkpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihcbiAgICAgICAgJ1RoZSB3YXJuaW5nIGZvcm1hdCBzaG91bGQgYmUgYWJsZSB0byB1bmlxdWVseSBpZGVudGlmeSB0aGlzICcgK1xuICAgICAgICAnd2FybmluZy4gUGxlYXNlLCB1c2UgYSBtb3JlIGRlc2NyaXB0aXZlIGZvcm1hdCB0aGFuOiAnICsgZm9ybWF0XG4gICAgICApO1xuICAgIH1cblxuICAgIGlmICghY29uZGl0aW9uKSB7XG4gICAgICB2YXIgYXJnSW5kZXggPSAwO1xuICAgICAgdmFyIG1lc3NhZ2UgPSAnV2FybmluZzogJyArXG4gICAgICAgIGZvcm1hdC5yZXBsYWNlKC8lcy9nLCBmdW5jdGlvbigpIHtcbiAgICAgICAgICByZXR1cm4gYXJnc1thcmdJbmRleCsrXTtcbiAgICAgICAgfSk7XG4gICAgICBpZiAodHlwZW9mIGNvbnNvbGUgIT09ICd1bmRlZmluZWQnKSB7XG4gICAgICAgIGNvbnNvbGUuZXJyb3IobWVzc2FnZSk7XG4gICAgICB9XG4gICAgICB0cnkge1xuICAgICAgICAvLyBUaGlzIGVycm9yIHdhcyB0aHJvd24gYXMgYSBjb252ZW5pZW5jZSBzbyB0aGF0IHlvdSBjYW4gdXNlIHRoaXMgc3RhY2tcbiAgICAgICAgLy8gdG8gZmluZCB0aGUgY2FsbHNpdGUgdGhhdCBjYXVzZWQgdGhpcyB3YXJuaW5nIHRvIGZpcmUuXG4gICAgICAgIHRocm93IG5ldyBFcnJvcihtZXNzYWdlKTtcbiAgICAgIH0gY2F0Y2goeCkge31cbiAgICB9XG4gIH07XG59XG5cbm1vZHVsZS5leHBvcnRzID0gd2FybmluZztcbiIsIi8qKlxuICogQ29weXJpZ2h0IDIwMTUsIFlhaG9vISBJbmMuXG4gKiBDb3B5cmlnaHRzIGxpY2Vuc2VkIHVuZGVyIHRoZSBOZXcgQlNEIExpY2Vuc2UuIFNlZSB0aGUgYWNjb21wYW55aW5nIExJQ0VOU0UgZmlsZSBmb3IgdGVybXMuXG4gKi9cbid1c2Ugc3RyaWN0JztcblxudmFyIFJFQUNUX1NUQVRJQ1MgPSB7XG4gICAgY2hpbGRDb250ZXh0VHlwZXM6IHRydWUsXG4gICAgY29udGV4dFR5cGVzOiB0cnVlLFxuICAgIGRlZmF1bHRQcm9wczogdHJ1ZSxcbiAgICBkaXNwbGF5TmFtZTogdHJ1ZSxcbiAgICBnZXREZWZhdWx0UHJvcHM6IHRydWUsXG4gICAgbWl4aW5zOiB0cnVlLFxuICAgIHByb3BUeXBlczogdHJ1ZSxcbiAgICB0eXBlOiB0cnVlXG59O1xuXG52YXIgS05PV05fU1RBVElDUyA9IHtcbiAgICBuYW1lOiB0cnVlLFxuICAgIGxlbmd0aDogdHJ1ZSxcbiAgICBwcm90b3R5cGU6IHRydWUsXG4gICAgY2FsbGVyOiB0cnVlLFxuICAgIGFyZ3VtZW50czogdHJ1ZSxcbiAgICBhcml0eTogdHJ1ZVxufTtcblxudmFyIGlzR2V0T3duUHJvcGVydHlTeW1ib2xzQXZhaWxhYmxlID0gdHlwZW9mIE9iamVjdC5nZXRPd25Qcm9wZXJ0eVN5bWJvbHMgPT09ICdmdW5jdGlvbic7XG5cbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24gaG9pc3ROb25SZWFjdFN0YXRpY3ModGFyZ2V0Q29tcG9uZW50LCBzb3VyY2VDb21wb25lbnQsIGN1c3RvbVN0YXRpY3MpIHtcbiAgICBpZiAodHlwZW9mIHNvdXJjZUNvbXBvbmVudCAhPT0gJ3N0cmluZycpIHsgLy8gZG9uJ3QgaG9pc3Qgb3ZlciBzdHJpbmcgKGh0bWwpIGNvbXBvbmVudHNcbiAgICAgICAgdmFyIGtleXMgPSBPYmplY3QuZ2V0T3duUHJvcGVydHlOYW1lcyhzb3VyY2VDb21wb25lbnQpO1xuXG4gICAgICAgIC8qIGlzdGFuYnVsIGlnbm9yZSBlbHNlICovXG4gICAgICAgIGlmIChpc0dldE93blByb3BlcnR5U3ltYm9sc0F2YWlsYWJsZSkge1xuICAgICAgICAgICAga2V5cyA9IGtleXMuY29uY2F0KE9iamVjdC5nZXRPd25Qcm9wZXJ0eVN5bWJvbHMoc291cmNlQ29tcG9uZW50KSk7XG4gICAgICAgIH1cblxuICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IGtleXMubGVuZ3RoOyArK2kpIHtcbiAgICAgICAgICAgIGlmICghUkVBQ1RfU1RBVElDU1trZXlzW2ldXSAmJiAhS05PV05fU1RBVElDU1trZXlzW2ldXSAmJiAoIWN1c3RvbVN0YXRpY3MgfHwgIWN1c3RvbVN0YXRpY3Nba2V5c1tpXV0pKSB7XG4gICAgICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICAgICAgdGFyZ2V0Q29tcG9uZW50W2tleXNbaV1dID0gc291cmNlQ29tcG9uZW50W2tleXNbaV1dO1xuICAgICAgICAgICAgICAgIH0gY2F0Y2ggKGVycm9yKSB7XG5cbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9XG5cbiAgICByZXR1cm4gdGFyZ2V0Q29tcG9uZW50O1xufTtcbiIsIi8qKlxuICogQ29weXJpZ2h0IDIwMTMtMjAxNSwgRmFjZWJvb2ssIEluYy5cbiAqIEFsbCByaWdodHMgcmVzZXJ2ZWQuXG4gKlxuICogVGhpcyBzb3VyY2UgY29kZSBpcyBsaWNlbnNlZCB1bmRlciB0aGUgQlNELXN0eWxlIGxpY2Vuc2UgZm91bmQgaW4gdGhlXG4gKiBMSUNFTlNFIGZpbGUgaW4gdGhlIHJvb3QgZGlyZWN0b3J5IG9mIHRoaXMgc291cmNlIHRyZWUuIEFuIGFkZGl0aW9uYWwgZ3JhbnRcbiAqIG9mIHBhdGVudCByaWdodHMgY2FuIGJlIGZvdW5kIGluIHRoZSBQQVRFTlRTIGZpbGUgaW4gdGhlIHNhbWUgZGlyZWN0b3J5LlxuICovXG5cbid1c2Ugc3RyaWN0JztcblxuLyoqXG4gKiBVc2UgaW52YXJpYW50KCkgdG8gYXNzZXJ0IHN0YXRlIHdoaWNoIHlvdXIgcHJvZ3JhbSBhc3N1bWVzIHRvIGJlIHRydWUuXG4gKlxuICogUHJvdmlkZSBzcHJpbnRmLXN0eWxlIGZvcm1hdCAob25seSAlcyBpcyBzdXBwb3J0ZWQpIGFuZCBhcmd1bWVudHNcbiAqIHRvIHByb3ZpZGUgaW5mb3JtYXRpb24gYWJvdXQgd2hhdCBicm9rZSBhbmQgd2hhdCB5b3Ugd2VyZVxuICogZXhwZWN0aW5nLlxuICpcbiAqIFRoZSBpbnZhcmlhbnQgbWVzc2FnZSB3aWxsIGJlIHN0cmlwcGVkIGluIHByb2R1Y3Rpb24sIGJ1dCB0aGUgaW52YXJpYW50XG4gKiB3aWxsIHJlbWFpbiB0byBlbnN1cmUgbG9naWMgZG9lcyBub3QgZGlmZmVyIGluIHByb2R1Y3Rpb24uXG4gKi9cblxudmFyIGludmFyaWFudCA9IGZ1bmN0aW9uKGNvbmRpdGlvbiwgZm9ybWF0LCBhLCBiLCBjLCBkLCBlLCBmKSB7XG4gIGlmIChcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgaWYgKGZvcm1hdCA9PT0gdW5kZWZpbmVkKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ2ludmFyaWFudCByZXF1aXJlcyBhbiBlcnJvciBtZXNzYWdlIGFyZ3VtZW50Jyk7XG4gICAgfVxuICB9XG5cbiAgaWYgKCFjb25kaXRpb24pIHtcbiAgICB2YXIgZXJyb3I7XG4gICAgaWYgKGZvcm1hdCA9PT0gdW5kZWZpbmVkKSB7XG4gICAgICBlcnJvciA9IG5ldyBFcnJvcihcbiAgICAgICAgJ01pbmlmaWVkIGV4Y2VwdGlvbiBvY2N1cnJlZDsgdXNlIHRoZSBub24tbWluaWZpZWQgZGV2IGVudmlyb25tZW50ICcgK1xuICAgICAgICAnZm9yIHRoZSBmdWxsIGVycm9yIG1lc3NhZ2UgYW5kIGFkZGl0aW9uYWwgaGVscGZ1bCB3YXJuaW5ncy4nXG4gICAgICApO1xuICAgIH0gZWxzZSB7XG4gICAgICB2YXIgYXJncyA9IFthLCBiLCBjLCBkLCBlLCBmXTtcbiAgICAgIHZhciBhcmdJbmRleCA9IDA7XG4gICAgICBlcnJvciA9IG5ldyBFcnJvcihcbiAgICAgICAgZm9ybWF0LnJlcGxhY2UoLyVzL2csIGZ1bmN0aW9uKCkgeyByZXR1cm4gYXJnc1thcmdJbmRleCsrXTsgfSlcbiAgICAgICk7XG4gICAgICBlcnJvci5uYW1lID0gJ0ludmFyaWFudCBWaW9sYXRpb24nO1xuICAgIH1cblxuICAgIGVycm9yLmZyYW1lc1RvUG9wID0gMTsgLy8gd2UgZG9uJ3QgY2FyZSBhYm91dCBpbnZhcmlhbnQncyBvd24gZnJhbWVcbiAgICB0aHJvdyBlcnJvcjtcbiAgfVxufTtcblxubW9kdWxlLmV4cG9ydHMgPSBpbnZhcmlhbnQ7XG4iLCJtb2R1bGUuZXhwb3J0cyA9IGlzRnVuY3Rpb25cblxudmFyIHRvU3RyaW5nID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZ1xuXG5mdW5jdGlvbiBpc0Z1bmN0aW9uIChmbikge1xuICB2YXIgc3RyaW5nID0gdG9TdHJpbmcuY2FsbChmbilcbiAgcmV0dXJuIHN0cmluZyA9PT0gJ1tvYmplY3QgRnVuY3Rpb25dJyB8fFxuICAgICh0eXBlb2YgZm4gPT09ICdmdW5jdGlvbicgJiYgc3RyaW5nICE9PSAnW29iamVjdCBSZWdFeHBdJykgfHxcbiAgICAodHlwZW9mIHdpbmRvdyAhPT0gJ3VuZGVmaW5lZCcgJiZcbiAgICAgLy8gSUU4IGFuZCBiZWxvd1xuICAgICAoZm4gPT09IHdpbmRvdy5zZXRUaW1lb3V0IHx8XG4gICAgICBmbiA9PT0gd2luZG93LmFsZXJ0IHx8XG4gICAgICBmbiA9PT0gd2luZG93LmNvbmZpcm0gfHxcbiAgICAgIGZuID09PSB3aW5kb3cucHJvbXB0KSlcbn07XG4iLCJ2YXIgb3ZlckFyZyA9IHJlcXVpcmUoJy4vX292ZXJBcmcnKTtcblxuLyoqIEJ1aWx0LWluIHZhbHVlIHJlZmVyZW5jZXMuICovXG52YXIgZ2V0UHJvdG90eXBlID0gb3ZlckFyZyhPYmplY3QuZ2V0UHJvdG90eXBlT2YsIE9iamVjdCk7XG5cbm1vZHVsZS5leHBvcnRzID0gZ2V0UHJvdG90eXBlO1xuIiwiLyoqXG4gKiBDaGVja3MgaWYgYHZhbHVlYCBpcyBhIGhvc3Qgb2JqZWN0IGluIElFIDwgOS5cbiAqXG4gKiBAcHJpdmF0ZVxuICogQHBhcmFtIHsqfSB2YWx1ZSBUaGUgdmFsdWUgdG8gY2hlY2suXG4gKiBAcmV0dXJucyB7Ym9vbGVhbn0gUmV0dXJucyBgdHJ1ZWAgaWYgYHZhbHVlYCBpcyBhIGhvc3Qgb2JqZWN0LCBlbHNlIGBmYWxzZWAuXG4gKi9cbmZ1bmN0aW9uIGlzSG9zdE9iamVjdCh2YWx1ZSkge1xuICAvLyBNYW55IGhvc3Qgb2JqZWN0cyBhcmUgYE9iamVjdGAgb2JqZWN0cyB0aGF0IGNhbiBjb2VyY2UgdG8gc3RyaW5nc1xuICAvLyBkZXNwaXRlIGhhdmluZyBpbXByb3Blcmx5IGRlZmluZWQgYHRvU3RyaW5nYCBtZXRob2RzLlxuICB2YXIgcmVzdWx0ID0gZmFsc2U7XG4gIGlmICh2YWx1ZSAhPSBudWxsICYmIHR5cGVvZiB2YWx1ZS50b1N0cmluZyAhPSAnZnVuY3Rpb24nKSB7XG4gICAgdHJ5IHtcbiAgICAgIHJlc3VsdCA9ICEhKHZhbHVlICsgJycpO1xuICAgIH0gY2F0Y2ggKGUpIHt9XG4gIH1cbiAgcmV0dXJuIHJlc3VsdDtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBpc0hvc3RPYmplY3Q7XG4iLCIvKipcbiAqIENyZWF0ZXMgYSB1bmFyeSBmdW5jdGlvbiB0aGF0IGludm9rZXMgYGZ1bmNgIHdpdGggaXRzIGFyZ3VtZW50IHRyYW5zZm9ybWVkLlxuICpcbiAqIEBwcml2YXRlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBmdW5jIFRoZSBmdW5jdGlvbiB0byB3cmFwLlxuICogQHBhcmFtIHtGdW5jdGlvbn0gdHJhbnNmb3JtIFRoZSBhcmd1bWVudCB0cmFuc2Zvcm0uXG4gKiBAcmV0dXJucyB7RnVuY3Rpb259IFJldHVybnMgdGhlIG5ldyBmdW5jdGlvbi5cbiAqL1xuZnVuY3Rpb24gb3ZlckFyZyhmdW5jLCB0cmFuc2Zvcm0pIHtcbiAgcmV0dXJuIGZ1bmN0aW9uKGFyZykge1xuICAgIHJldHVybiBmdW5jKHRyYW5zZm9ybShhcmcpKTtcbiAgfTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBvdmVyQXJnO1xuIiwiLyoqXG4gKiBDaGVja3MgaWYgYHZhbHVlYCBpcyBvYmplY3QtbGlrZS4gQSB2YWx1ZSBpcyBvYmplY3QtbGlrZSBpZiBpdCdzIG5vdCBgbnVsbGBcbiAqIGFuZCBoYXMgYSBgdHlwZW9mYCByZXN1bHQgb2YgXCJvYmplY3RcIi5cbiAqXG4gKiBAc3RhdGljXG4gKiBAbWVtYmVyT2YgX1xuICogQHNpbmNlIDQuMC4wXG4gKiBAY2F0ZWdvcnkgTGFuZ1xuICogQHBhcmFtIHsqfSB2YWx1ZSBUaGUgdmFsdWUgdG8gY2hlY2suXG4gKiBAcmV0dXJucyB7Ym9vbGVhbn0gUmV0dXJucyBgdHJ1ZWAgaWYgYHZhbHVlYCBpcyBvYmplY3QtbGlrZSwgZWxzZSBgZmFsc2VgLlxuICogQGV4YW1wbGVcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZSh7fSk7XG4gKiAvLyA9PiB0cnVlXG4gKlxuICogXy5pc09iamVjdExpa2UoWzEsIDIsIDNdKTtcbiAqIC8vID0+IHRydWVcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZShfLm5vb3ApO1xuICogLy8gPT4gZmFsc2VcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZShudWxsKTtcbiAqIC8vID0+IGZhbHNlXG4gKi9cbmZ1bmN0aW9uIGlzT2JqZWN0TGlrZSh2YWx1ZSkge1xuICByZXR1cm4gISF2YWx1ZSAmJiB0eXBlb2YgdmFsdWUgPT0gJ29iamVjdCc7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gaXNPYmplY3RMaWtlO1xuIiwidmFyIGdldFByb3RvdHlwZSA9IHJlcXVpcmUoJy4vX2dldFByb3RvdHlwZScpLFxuICAgIGlzSG9zdE9iamVjdCA9IHJlcXVpcmUoJy4vX2lzSG9zdE9iamVjdCcpLFxuICAgIGlzT2JqZWN0TGlrZSA9IHJlcXVpcmUoJy4vaXNPYmplY3RMaWtlJyk7XG5cbi8qKiBgT2JqZWN0I3RvU3RyaW5nYCByZXN1bHQgcmVmZXJlbmNlcy4gKi9cbnZhciBvYmplY3RUYWcgPSAnW29iamVjdCBPYmplY3RdJztcblxuLyoqIFVzZWQgZm9yIGJ1aWx0LWluIG1ldGhvZCByZWZlcmVuY2VzLiAqL1xudmFyIG9iamVjdFByb3RvID0gT2JqZWN0LnByb3RvdHlwZTtcblxuLyoqIFVzZWQgdG8gcmVzb2x2ZSB0aGUgZGVjb21waWxlZCBzb3VyY2Ugb2YgZnVuY3Rpb25zLiAqL1xudmFyIGZ1bmNUb1N0cmluZyA9IEZ1bmN0aW9uLnByb3RvdHlwZS50b1N0cmluZztcblxuLyoqIFVzZWQgdG8gY2hlY2sgb2JqZWN0cyBmb3Igb3duIHByb3BlcnRpZXMuICovXG52YXIgaGFzT3duUHJvcGVydHkgPSBvYmplY3RQcm90by5oYXNPd25Qcm9wZXJ0eTtcblxuLyoqIFVzZWQgdG8gaW5mZXIgdGhlIGBPYmplY3RgIGNvbnN0cnVjdG9yLiAqL1xudmFyIG9iamVjdEN0b3JTdHJpbmcgPSBmdW5jVG9TdHJpbmcuY2FsbChPYmplY3QpO1xuXG4vKipcbiAqIFVzZWQgdG8gcmVzb2x2ZSB0aGVcbiAqIFtgdG9TdHJpbmdUYWdgXShodHRwOi8vZWNtYS1pbnRlcm5hdGlvbmFsLm9yZy9lY21hLTI2Mi83LjAvI3NlYy1vYmplY3QucHJvdG90eXBlLnRvc3RyaW5nKVxuICogb2YgdmFsdWVzLlxuICovXG52YXIgb2JqZWN0VG9TdHJpbmcgPSBvYmplY3RQcm90by50b1N0cmluZztcblxuLyoqXG4gKiBDaGVja3MgaWYgYHZhbHVlYCBpcyBhIHBsYWluIG9iamVjdCwgdGhhdCBpcywgYW4gb2JqZWN0IGNyZWF0ZWQgYnkgdGhlXG4gKiBgT2JqZWN0YCBjb25zdHJ1Y3RvciBvciBvbmUgd2l0aCBhIGBbW1Byb3RvdHlwZV1dYCBvZiBgbnVsbGAuXG4gKlxuICogQHN0YXRpY1xuICogQG1lbWJlck9mIF9cbiAqIEBzaW5jZSAwLjguMFxuICogQGNhdGVnb3J5IExhbmdcbiAqIEBwYXJhbSB7Kn0gdmFsdWUgVGhlIHZhbHVlIHRvIGNoZWNrLlxuICogQHJldHVybnMge2Jvb2xlYW59IFJldHVybnMgYHRydWVgIGlmIGB2YWx1ZWAgaXMgYSBwbGFpbiBvYmplY3QsIGVsc2UgYGZhbHNlYC5cbiAqIEBleGFtcGxlXG4gKlxuICogZnVuY3Rpb24gRm9vKCkge1xuICogICB0aGlzLmEgPSAxO1xuICogfVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdChuZXcgRm9vKTtcbiAqIC8vID0+IGZhbHNlXG4gKlxuICogXy5pc1BsYWluT2JqZWN0KFsxLCAyLCAzXSk7XG4gKiAvLyA9PiBmYWxzZVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdCh7ICd4JzogMCwgJ3knOiAwIH0pO1xuICogLy8gPT4gdHJ1ZVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdChPYmplY3QuY3JlYXRlKG51bGwpKTtcbiAqIC8vID0+IHRydWVcbiAqL1xuZnVuY3Rpb24gaXNQbGFpbk9iamVjdCh2YWx1ZSkge1xuICBpZiAoIWlzT2JqZWN0TGlrZSh2YWx1ZSkgfHxcbiAgICAgIG9iamVjdFRvU3RyaW5nLmNhbGwodmFsdWUpICE9IG9iamVjdFRhZyB8fCBpc0hvc3RPYmplY3QodmFsdWUpKSB7XG4gICAgcmV0dXJuIGZhbHNlO1xuICB9XG4gIHZhciBwcm90byA9IGdldFByb3RvdHlwZSh2YWx1ZSk7XG4gIGlmIChwcm90byA9PT0gbnVsbCkge1xuICAgIHJldHVybiB0cnVlO1xuICB9XG4gIHZhciBDdG9yID0gaGFzT3duUHJvcGVydHkuY2FsbChwcm90bywgJ2NvbnN0cnVjdG9yJykgJiYgcHJvdG8uY29uc3RydWN0b3I7XG4gIHJldHVybiAodHlwZW9mIEN0b3IgPT0gJ2Z1bmN0aW9uJyAmJlxuICAgIEN0b3IgaW5zdGFuY2VvZiBDdG9yICYmIGZ1bmNUb1N0cmluZy5jYWxsKEN0b3IpID09IG9iamVjdEN0b3JTdHJpbmcpO1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGlzUGxhaW5PYmplY3Q7XG4iLCJ2YXIgdHJpbSA9IHJlcXVpcmUoJ3RyaW0nKVxuICAsIGZvckVhY2ggPSByZXF1aXJlKCdmb3ItZWFjaCcpXG4gICwgaXNBcnJheSA9IGZ1bmN0aW9uKGFyZykge1xuICAgICAgcmV0dXJuIE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbChhcmcpID09PSAnW29iamVjdCBBcnJheV0nO1xuICAgIH1cblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiAoaGVhZGVycykge1xuICBpZiAoIWhlYWRlcnMpXG4gICAgcmV0dXJuIHt9XG5cbiAgdmFyIHJlc3VsdCA9IHt9XG5cbiAgZm9yRWFjaChcbiAgICAgIHRyaW0oaGVhZGVycykuc3BsaXQoJ1xcbicpXG4gICAgLCBmdW5jdGlvbiAocm93KSB7XG4gICAgICAgIHZhciBpbmRleCA9IHJvdy5pbmRleE9mKCc6JylcbiAgICAgICAgICAsIGtleSA9IHRyaW0ocm93LnNsaWNlKDAsIGluZGV4KSkudG9Mb3dlckNhc2UoKVxuICAgICAgICAgICwgdmFsdWUgPSB0cmltKHJvdy5zbGljZShpbmRleCArIDEpKVxuXG4gICAgICAgIGlmICh0eXBlb2YocmVzdWx0W2tleV0pID09PSAndW5kZWZpbmVkJykge1xuICAgICAgICAgIHJlc3VsdFtrZXldID0gdmFsdWVcbiAgICAgICAgfSBlbHNlIGlmIChpc0FycmF5KHJlc3VsdFtrZXldKSkge1xuICAgICAgICAgIHJlc3VsdFtrZXldLnB1c2godmFsdWUpXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgcmVzdWx0W2tleV0gPSBbIHJlc3VsdFtrZXldLCB2YWx1ZSBdXG4gICAgICAgIH1cbiAgICAgIH1cbiAgKVxuXG4gIHJldHVybiByZXN1bHRcbn0iLCIndXNlIHN0cmljdCc7XG52YXIgc3RyaWN0VXJpRW5jb2RlID0gcmVxdWlyZSgnc3RyaWN0LXVyaS1lbmNvZGUnKTtcblxuZXhwb3J0cy5leHRyYWN0ID0gZnVuY3Rpb24gKHN0cikge1xuXHRyZXR1cm4gc3RyLnNwbGl0KCc/JylbMV0gfHwgJyc7XG59O1xuXG5leHBvcnRzLnBhcnNlID0gZnVuY3Rpb24gKHN0cikge1xuXHRpZiAodHlwZW9mIHN0ciAhPT0gJ3N0cmluZycpIHtcblx0XHRyZXR1cm4ge307XG5cdH1cblxuXHRzdHIgPSBzdHIudHJpbSgpLnJlcGxhY2UoL14oXFw/fCN8JikvLCAnJyk7XG5cblx0aWYgKCFzdHIpIHtcblx0XHRyZXR1cm4ge307XG5cdH1cblxuXHRyZXR1cm4gc3RyLnNwbGl0KCcmJykucmVkdWNlKGZ1bmN0aW9uIChyZXQsIHBhcmFtKSB7XG5cdFx0dmFyIHBhcnRzID0gcGFyYW0ucmVwbGFjZSgvXFwrL2csICcgJykuc3BsaXQoJz0nKTtcblx0XHQvLyBGaXJlZm94IChwcmUgNDApIGRlY29kZXMgYCUzRGAgdG8gYD1gXG5cdFx0Ly8gaHR0cHM6Ly9naXRodWIuY29tL3NpbmRyZXNvcmh1cy9xdWVyeS1zdHJpbmcvcHVsbC8zN1xuXHRcdHZhciBrZXkgPSBwYXJ0cy5zaGlmdCgpO1xuXHRcdHZhciB2YWwgPSBwYXJ0cy5sZW5ndGggPiAwID8gcGFydHMuam9pbignPScpIDogdW5kZWZpbmVkO1xuXG5cdFx0a2V5ID0gZGVjb2RlVVJJQ29tcG9uZW50KGtleSk7XG5cblx0XHQvLyBtaXNzaW5nIGA9YCBzaG91bGQgYmUgYG51bGxgOlxuXHRcdC8vIGh0dHA6Ly93My5vcmcvVFIvMjAxMi9XRC11cmwtMjAxMjA1MjQvI2NvbGxlY3QtdXJsLXBhcmFtZXRlcnNcblx0XHR2YWwgPSB2YWwgPT09IHVuZGVmaW5lZCA/IG51bGwgOiBkZWNvZGVVUklDb21wb25lbnQodmFsKTtcblxuXHRcdGlmICghcmV0Lmhhc093blByb3BlcnR5KGtleSkpIHtcblx0XHRcdHJldFtrZXldID0gdmFsO1xuXHRcdH0gZWxzZSBpZiAoQXJyYXkuaXNBcnJheShyZXRba2V5XSkpIHtcblx0XHRcdHJldFtrZXldLnB1c2godmFsKTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0cmV0W2tleV0gPSBbcmV0W2tleV0sIHZhbF07XG5cdFx0fVxuXG5cdFx0cmV0dXJuIHJldDtcblx0fSwge30pO1xufTtcblxuZXhwb3J0cy5zdHJpbmdpZnkgPSBmdW5jdGlvbiAob2JqKSB7XG5cdHJldHVybiBvYmogPyBPYmplY3Qua2V5cyhvYmopLnNvcnQoKS5tYXAoZnVuY3Rpb24gKGtleSkge1xuXHRcdHZhciB2YWwgPSBvYmpba2V5XTtcblxuXHRcdGlmICh2YWwgPT09IHVuZGVmaW5lZCkge1xuXHRcdFx0cmV0dXJuICcnO1xuXHRcdH1cblxuXHRcdGlmICh2YWwgPT09IG51bGwpIHtcblx0XHRcdHJldHVybiBrZXk7XG5cdFx0fVxuXG5cdFx0aWYgKEFycmF5LmlzQXJyYXkodmFsKSkge1xuXHRcdFx0cmV0dXJuIHZhbC5zbGljZSgpLnNvcnQoKS5tYXAoZnVuY3Rpb24gKHZhbDIpIHtcblx0XHRcdFx0cmV0dXJuIHN0cmljdFVyaUVuY29kZShrZXkpICsgJz0nICsgc3RyaWN0VXJpRW5jb2RlKHZhbDIpO1xuXHRcdFx0fSkuam9pbignJicpO1xuXHRcdH1cblxuXHRcdHJldHVybiBzdHJpY3RVcmlFbmNvZGUoa2V5KSArICc9JyArIHN0cmljdFVyaUVuY29kZSh2YWwpO1xuXHR9KS5maWx0ZXIoZnVuY3Rpb24gKHgpIHtcblx0XHRyZXR1cm4geC5sZW5ndGggPiAwO1xuXHR9KS5qb2luKCcmJykgOiAnJztcbn07XG4iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IHVuZGVmaW5lZDtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfc3RvcmVTaGFwZSA9IHJlcXVpcmUoJy4uL3V0aWxzL3N0b3JlU2hhcGUnKTtcblxudmFyIF9zdG9yZVNoYXBlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3N0b3JlU2hhcGUpO1xuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCcuLi91dGlscy93YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIF9jbGFzc0NhbGxDaGVjayhpbnN0YW5jZSwgQ29uc3RydWN0b3IpIHsgaWYgKCEoaW5zdGFuY2UgaW5zdGFuY2VvZiBDb25zdHJ1Y3RvcikpIHsgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTsgfSB9XG5cbmZ1bmN0aW9uIF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHNlbGYsIGNhbGwpIHsgaWYgKCFzZWxmKSB7IHRocm93IG5ldyBSZWZlcmVuY2VFcnJvcihcInRoaXMgaGFzbid0IGJlZW4gaW5pdGlhbGlzZWQgLSBzdXBlcigpIGhhc24ndCBiZWVuIGNhbGxlZFwiKTsgfSByZXR1cm4gY2FsbCAmJiAodHlwZW9mIGNhbGwgPT09IFwib2JqZWN0XCIgfHwgdHlwZW9mIGNhbGwgPT09IFwiZnVuY3Rpb25cIikgPyBjYWxsIDogc2VsZjsgfVxuXG5mdW5jdGlvbiBfaW5oZXJpdHMoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIHsgaWYgKHR5cGVvZiBzdXBlckNsYXNzICE9PSBcImZ1bmN0aW9uXCIgJiYgc3VwZXJDbGFzcyAhPT0gbnVsbCkgeyB0aHJvdyBuZXcgVHlwZUVycm9yKFwiU3VwZXIgZXhwcmVzc2lvbiBtdXN0IGVpdGhlciBiZSBudWxsIG9yIGEgZnVuY3Rpb24sIG5vdCBcIiArIHR5cGVvZiBzdXBlckNsYXNzKTsgfSBzdWJDbGFzcy5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKHN1cGVyQ2xhc3MgJiYgc3VwZXJDbGFzcy5wcm90b3R5cGUsIHsgY29uc3RydWN0b3I6IHsgdmFsdWU6IHN1YkNsYXNzLCBlbnVtZXJhYmxlOiBmYWxzZSwgd3JpdGFibGU6IHRydWUsIGNvbmZpZ3VyYWJsZTogdHJ1ZSB9IH0pOyBpZiAoc3VwZXJDbGFzcykgT2JqZWN0LnNldFByb3RvdHlwZU9mID8gT2JqZWN0LnNldFByb3RvdHlwZU9mKHN1YkNsYXNzLCBzdXBlckNsYXNzKSA6IHN1YkNsYXNzLl9fcHJvdG9fXyA9IHN1cGVyQ2xhc3M7IH1cblxudmFyIGRpZFdhcm5BYm91dFJlY2VpdmluZ1N0b3JlID0gZmFsc2U7XG5mdW5jdGlvbiB3YXJuQWJvdXRSZWNlaXZpbmdTdG9yZSgpIHtcbiAgaWYgKGRpZFdhcm5BYm91dFJlY2VpdmluZ1N0b3JlKSB7XG4gICAgcmV0dXJuO1xuICB9XG4gIGRpZFdhcm5BYm91dFJlY2VpdmluZ1N0b3JlID0gdHJ1ZTtcblxuICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkoJzxQcm92aWRlcj4gZG9lcyBub3Qgc3VwcG9ydCBjaGFuZ2luZyBgc3RvcmVgIG9uIHRoZSBmbHkuICcgKyAnSXQgaXMgbW9zdCBsaWtlbHkgdGhhdCB5b3Ugc2VlIHRoaXMgZXJyb3IgYmVjYXVzZSB5b3UgdXBkYXRlZCB0byAnICsgJ1JlZHV4IDIueCBhbmQgUmVhY3QgUmVkdXggMi54IHdoaWNoIG5vIGxvbmdlciBob3QgcmVsb2FkIHJlZHVjZXJzICcgKyAnYXV0b21hdGljYWxseS4gU2VlIGh0dHBzOi8vZ2l0aHViLmNvbS9yZWFjdGpzL3JlYWN0LXJlZHV4L3JlbGVhc2VzLycgKyAndGFnL3YyLjAuMCBmb3IgdGhlIG1pZ3JhdGlvbiBpbnN0cnVjdGlvbnMuJyk7XG59XG5cbnZhciBQcm92aWRlciA9IGZ1bmN0aW9uIChfQ29tcG9uZW50KSB7XG4gIF9pbmhlcml0cyhQcm92aWRlciwgX0NvbXBvbmVudCk7XG5cbiAgUHJvdmlkZXIucHJvdG90eXBlLmdldENoaWxkQ29udGV4dCA9IGZ1bmN0aW9uIGdldENoaWxkQ29udGV4dCgpIHtcbiAgICByZXR1cm4geyBzdG9yZTogdGhpcy5zdG9yZSB9O1xuICB9O1xuXG4gIGZ1bmN0aW9uIFByb3ZpZGVyKHByb3BzLCBjb250ZXh0KSB7XG4gICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIFByb3ZpZGVyKTtcblxuICAgIHZhciBfdGhpcyA9IF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHRoaXMsIF9Db21wb25lbnQuY2FsbCh0aGlzLCBwcm9wcywgY29udGV4dCkpO1xuXG4gICAgX3RoaXMuc3RvcmUgPSBwcm9wcy5zdG9yZTtcbiAgICByZXR1cm4gX3RoaXM7XG4gIH1cblxuICBQcm92aWRlci5wcm90b3R5cGUucmVuZGVyID0gZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHZhciBjaGlsZHJlbiA9IHRoaXMucHJvcHMuY2hpbGRyZW47XG5cbiAgICByZXR1cm4gX3JlYWN0LkNoaWxkcmVuLm9ubHkoY2hpbGRyZW4pO1xuICB9O1xuXG4gIHJldHVybiBQcm92aWRlcjtcbn0oX3JlYWN0LkNvbXBvbmVudCk7XG5cbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gUHJvdmlkZXI7XG5cbmlmIChcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gIFByb3ZpZGVyLnByb3RvdHlwZS5jb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzID0gZnVuY3Rpb24gKG5leHRQcm9wcykge1xuICAgIHZhciBzdG9yZSA9IHRoaXMuc3RvcmU7XG4gICAgdmFyIG5leHRTdG9yZSA9IG5leHRQcm9wcy5zdG9yZTtcblxuICAgIGlmIChzdG9yZSAhPT0gbmV4dFN0b3JlKSB7XG4gICAgICB3YXJuQWJvdXRSZWNlaXZpbmdTdG9yZSgpO1xuICAgIH1cbiAgfTtcbn1cblxuUHJvdmlkZXIucHJvcFR5cGVzID0ge1xuICBzdG9yZTogX3N0b3JlU2hhcGUyW1wiZGVmYXVsdFwiXS5pc1JlcXVpcmVkLFxuICBjaGlsZHJlbjogX3JlYWN0LlByb3BUeXBlcy5lbGVtZW50LmlzUmVxdWlyZWRcbn07XG5Qcm92aWRlci5jaGlsZENvbnRleHRUeXBlcyA9IHtcbiAgc3RvcmU6IF9zdG9yZVNoYXBlMltcImRlZmF1bHRcIl0uaXNSZXF1aXJlZFxufTsiLCIndXNlIHN0cmljdCc7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGNvbm5lY3Q7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3N0b3JlU2hhcGUgPSByZXF1aXJlKCcuLi91dGlscy9zdG9yZVNoYXBlJyk7XG5cbnZhciBfc3RvcmVTaGFwZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zdG9yZVNoYXBlKTtcblxudmFyIF9zaGFsbG93RXF1YWwgPSByZXF1aXJlKCcuLi91dGlscy9zaGFsbG93RXF1YWwnKTtcblxudmFyIF9zaGFsbG93RXF1YWwyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc2hhbGxvd0VxdWFsKTtcblxudmFyIF93cmFwQWN0aW9uQ3JlYXRvcnMgPSByZXF1aXJlKCcuLi91dGlscy93cmFwQWN0aW9uQ3JlYXRvcnMnKTtcblxudmFyIF93cmFwQWN0aW9uQ3JlYXRvcnMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd3JhcEFjdGlvbkNyZWF0b3JzKTtcblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnLi4vdXRpbHMvd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdCA9IHJlcXVpcmUoJ2xvZGFzaC9pc1BsYWluT2JqZWN0Jyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pc1BsYWluT2JqZWN0KTtcblxudmFyIF9ob2lzdE5vblJlYWN0U3RhdGljcyA9IHJlcXVpcmUoJ2hvaXN0LW5vbi1yZWFjdC1zdGF0aWNzJyk7XG5cbnZhciBfaG9pc3ROb25SZWFjdFN0YXRpY3MyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaG9pc3ROb25SZWFjdFN0YXRpY3MpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3RvcikgeyBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkgeyB0aHJvdyBuZXcgVHlwZUVycm9yKFwiQ2Fubm90IGNhbGwgYSBjbGFzcyBhcyBhIGZ1bmN0aW9uXCIpOyB9IH1cblxuZnVuY3Rpb24gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4oc2VsZiwgY2FsbCkgeyBpZiAoIXNlbGYpIHsgdGhyb3cgbmV3IFJlZmVyZW5jZUVycm9yKFwidGhpcyBoYXNuJ3QgYmVlbiBpbml0aWFsaXNlZCAtIHN1cGVyKCkgaGFzbid0IGJlZW4gY2FsbGVkXCIpOyB9IHJldHVybiBjYWxsICYmICh0eXBlb2YgY2FsbCA9PT0gXCJvYmplY3RcIiB8fCB0eXBlb2YgY2FsbCA9PT0gXCJmdW5jdGlvblwiKSA/IGNhbGwgOiBzZWxmOyB9XG5cbmZ1bmN0aW9uIF9pbmhlcml0cyhzdWJDbGFzcywgc3VwZXJDbGFzcykgeyBpZiAodHlwZW9mIHN1cGVyQ2xhc3MgIT09IFwiZnVuY3Rpb25cIiAmJiBzdXBlckNsYXNzICE9PSBudWxsKSB7IHRocm93IG5ldyBUeXBlRXJyb3IoXCJTdXBlciBleHByZXNzaW9uIG11c3QgZWl0aGVyIGJlIG51bGwgb3IgYSBmdW5jdGlvbiwgbm90IFwiICsgdHlwZW9mIHN1cGVyQ2xhc3MpOyB9IHN1YkNsYXNzLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoc3VwZXJDbGFzcyAmJiBzdXBlckNsYXNzLnByb3RvdHlwZSwgeyBjb25zdHJ1Y3RvcjogeyB2YWx1ZTogc3ViQ2xhc3MsIGVudW1lcmFibGU6IGZhbHNlLCB3cml0YWJsZTogdHJ1ZSwgY29uZmlndXJhYmxlOiB0cnVlIH0gfSk7IGlmIChzdXBlckNsYXNzKSBPYmplY3Quc2V0UHJvdG90eXBlT2YgPyBPYmplY3Quc2V0UHJvdG90eXBlT2Yoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIDogc3ViQ2xhc3MuX19wcm90b19fID0gc3VwZXJDbGFzczsgfVxuXG52YXIgZGVmYXVsdE1hcFN0YXRlVG9Qcm9wcyA9IGZ1bmN0aW9uIGRlZmF1bHRNYXBTdGF0ZVRvUHJvcHMoc3RhdGUpIHtcbiAgcmV0dXJuIHt9O1xufTsgLy8gZXNsaW50LWRpc2FibGUtbGluZSBuby11bnVzZWQtdmFyc1xudmFyIGRlZmF1bHRNYXBEaXNwYXRjaFRvUHJvcHMgPSBmdW5jdGlvbiBkZWZhdWx0TWFwRGlzcGF0Y2hUb1Byb3BzKGRpc3BhdGNoKSB7XG4gIHJldHVybiB7IGRpc3BhdGNoOiBkaXNwYXRjaCB9O1xufTtcbnZhciBkZWZhdWx0TWVyZ2VQcm9wcyA9IGZ1bmN0aW9uIGRlZmF1bHRNZXJnZVByb3BzKHN0YXRlUHJvcHMsIGRpc3BhdGNoUHJvcHMsIHBhcmVudFByb3BzKSB7XG4gIHJldHVybiBfZXh0ZW5kcyh7fSwgcGFyZW50UHJvcHMsIHN0YXRlUHJvcHMsIGRpc3BhdGNoUHJvcHMpO1xufTtcblxuZnVuY3Rpb24gZ2V0RGlzcGxheU5hbWUoV3JhcHBlZENvbXBvbmVudCkge1xuICByZXR1cm4gV3JhcHBlZENvbXBvbmVudC5kaXNwbGF5TmFtZSB8fCBXcmFwcGVkQ29tcG9uZW50Lm5hbWUgfHwgJ0NvbXBvbmVudCc7XG59XG5cbnZhciBlcnJvck9iamVjdCA9IHsgdmFsdWU6IG51bGwgfTtcbmZ1bmN0aW9uIHRyeUNhdGNoKGZuLCBjdHgpIHtcbiAgdHJ5IHtcbiAgICByZXR1cm4gZm4uYXBwbHkoY3R4KTtcbiAgfSBjYXRjaCAoZSkge1xuICAgIGVycm9yT2JqZWN0LnZhbHVlID0gZTtcbiAgICByZXR1cm4gZXJyb3JPYmplY3Q7XG4gIH1cbn1cblxuLy8gSGVscHMgdHJhY2sgaG90IHJlbG9hZGluZy5cbnZhciBuZXh0VmVyc2lvbiA9IDA7XG5cbmZ1bmN0aW9uIGNvbm5lY3QobWFwU3RhdGVUb1Byb3BzLCBtYXBEaXNwYXRjaFRvUHJvcHMsIG1lcmdlUHJvcHMpIHtcbiAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDMgfHwgYXJndW1lbnRzWzNdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1szXTtcblxuICB2YXIgc2hvdWxkU3Vic2NyaWJlID0gQm9vbGVhbihtYXBTdGF0ZVRvUHJvcHMpO1xuICB2YXIgbWFwU3RhdGUgPSBtYXBTdGF0ZVRvUHJvcHMgfHwgZGVmYXVsdE1hcFN0YXRlVG9Qcm9wcztcblxuICB2YXIgbWFwRGlzcGF0Y2ggPSB1bmRlZmluZWQ7XG4gIGlmICh0eXBlb2YgbWFwRGlzcGF0Y2hUb1Byb3BzID09PSAnZnVuY3Rpb24nKSB7XG4gICAgbWFwRGlzcGF0Y2ggPSBtYXBEaXNwYXRjaFRvUHJvcHM7XG4gIH0gZWxzZSBpZiAoIW1hcERpc3BhdGNoVG9Qcm9wcykge1xuICAgIG1hcERpc3BhdGNoID0gZGVmYXVsdE1hcERpc3BhdGNoVG9Qcm9wcztcbiAgfSBlbHNlIHtcbiAgICBtYXBEaXNwYXRjaCA9ICgwLCBfd3JhcEFjdGlvbkNyZWF0b3JzMltcImRlZmF1bHRcIl0pKG1hcERpc3BhdGNoVG9Qcm9wcyk7XG4gIH1cblxuICB2YXIgZmluYWxNZXJnZVByb3BzID0gbWVyZ2VQcm9wcyB8fCBkZWZhdWx0TWVyZ2VQcm9wcztcbiAgdmFyIF9vcHRpb25zJHB1cmUgPSBvcHRpb25zLnB1cmU7XG4gIHZhciBwdXJlID0gX29wdGlvbnMkcHVyZSA9PT0gdW5kZWZpbmVkID8gdHJ1ZSA6IF9vcHRpb25zJHB1cmU7XG4gIHZhciBfb3B0aW9ucyR3aXRoUmVmID0gb3B0aW9ucy53aXRoUmVmO1xuICB2YXIgd2l0aFJlZiA9IF9vcHRpb25zJHdpdGhSZWYgPT09IHVuZGVmaW5lZCA/IGZhbHNlIDogX29wdGlvbnMkd2l0aFJlZjtcblxuICB2YXIgY2hlY2tNZXJnZWRFcXVhbHMgPSBwdXJlICYmIGZpbmFsTWVyZ2VQcm9wcyAhPT0gZGVmYXVsdE1lcmdlUHJvcHM7XG5cbiAgLy8gSGVscHMgdHJhY2sgaG90IHJlbG9hZGluZy5cbiAgdmFyIHZlcnNpb24gPSBuZXh0VmVyc2lvbisrO1xuXG4gIHJldHVybiBmdW5jdGlvbiB3cmFwV2l0aENvbm5lY3QoV3JhcHBlZENvbXBvbmVudCkge1xuICAgIHZhciBjb25uZWN0RGlzcGxheU5hbWUgPSAnQ29ubmVjdCgnICsgZ2V0RGlzcGxheU5hbWUoV3JhcHBlZENvbXBvbmVudCkgKyAnKSc7XG5cbiAgICBmdW5jdGlvbiBjaGVja1N0YXRlU2hhcGUocHJvcHMsIG1ldGhvZE5hbWUpIHtcbiAgICAgIGlmICghKDAsIF9pc1BsYWluT2JqZWN0MltcImRlZmF1bHRcIl0pKHByb3BzKSkge1xuICAgICAgICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkobWV0aG9kTmFtZSArICcoKSBpbiAnICsgY29ubmVjdERpc3BsYXlOYW1lICsgJyBtdXN0IHJldHVybiBhIHBsYWluIG9iamVjdC4gJyArICgnSW5zdGVhZCByZWNlaXZlZCAnICsgcHJvcHMgKyAnLicpKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICBmdW5jdGlvbiBjb21wdXRlTWVyZ2VkUHJvcHMoc3RhdGVQcm9wcywgZGlzcGF0Y2hQcm9wcywgcGFyZW50UHJvcHMpIHtcbiAgICAgIHZhciBtZXJnZWRQcm9wcyA9IGZpbmFsTWVyZ2VQcm9wcyhzdGF0ZVByb3BzLCBkaXNwYXRjaFByb3BzLCBwYXJlbnRQcm9wcyk7XG4gICAgICBpZiAoXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgICBjaGVja1N0YXRlU2hhcGUobWVyZ2VkUHJvcHMsICdtZXJnZVByb3BzJyk7XG4gICAgICB9XG4gICAgICByZXR1cm4gbWVyZ2VkUHJvcHM7XG4gICAgfVxuXG4gICAgdmFyIENvbm5lY3QgPSBmdW5jdGlvbiAoX0NvbXBvbmVudCkge1xuICAgICAgX2luaGVyaXRzKENvbm5lY3QsIF9Db21wb25lbnQpO1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5zaG91bGRDb21wb25lbnRVcGRhdGUgPSBmdW5jdGlvbiBzaG91bGRDb21wb25lbnRVcGRhdGUoKSB7XG4gICAgICAgIHJldHVybiAhcHVyZSB8fCB0aGlzLmhhdmVPd25Qcm9wc0NoYW5nZWQgfHwgdGhpcy5oYXNTdG9yZVN0YXRlQ2hhbmdlZDtcbiAgICAgIH07XG5cbiAgICAgIGZ1bmN0aW9uIENvbm5lY3QocHJvcHMsIGNvbnRleHQpIHtcbiAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENvbm5lY3QpO1xuXG4gICAgICAgIHZhciBfdGhpcyA9IF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHRoaXMsIF9Db21wb25lbnQuY2FsbCh0aGlzLCBwcm9wcywgY29udGV4dCkpO1xuXG4gICAgICAgIF90aGlzLnZlcnNpb24gPSB2ZXJzaW9uO1xuICAgICAgICBfdGhpcy5zdG9yZSA9IHByb3BzLnN0b3JlIHx8IGNvbnRleHQuc3RvcmU7XG5cbiAgICAgICAgKDAsIF9pbnZhcmlhbnQyW1wiZGVmYXVsdFwiXSkoX3RoaXMuc3RvcmUsICdDb3VsZCBub3QgZmluZCBcInN0b3JlXCIgaW4gZWl0aGVyIHRoZSBjb250ZXh0IG9yICcgKyAoJ3Byb3BzIG9mIFwiJyArIGNvbm5lY3REaXNwbGF5TmFtZSArICdcIi4gJykgKyAnRWl0aGVyIHdyYXAgdGhlIHJvb3QgY29tcG9uZW50IGluIGEgPFByb3ZpZGVyPiwgJyArICgnb3IgZXhwbGljaXRseSBwYXNzIFwic3RvcmVcIiBhcyBhIHByb3AgdG8gXCInICsgY29ubmVjdERpc3BsYXlOYW1lICsgJ1wiLicpKTtcblxuICAgICAgICB2YXIgc3RvcmVTdGF0ZSA9IF90aGlzLnN0b3JlLmdldFN0YXRlKCk7XG4gICAgICAgIF90aGlzLnN0YXRlID0geyBzdG9yZVN0YXRlOiBzdG9yZVN0YXRlIH07XG4gICAgICAgIF90aGlzLmNsZWFyQ2FjaGUoKTtcbiAgICAgICAgcmV0dXJuIF90aGlzO1xuICAgICAgfVxuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jb21wdXRlU3RhdGVQcm9wcyA9IGZ1bmN0aW9uIGNvbXB1dGVTdGF0ZVByb3BzKHN0b3JlLCBwcm9wcykge1xuICAgICAgICBpZiAoIXRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5jb25maWd1cmVGaW5hbE1hcFN0YXRlKHN0b3JlLCBwcm9wcyk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgc3RhdGUgPSBzdG9yZS5nZXRTdGF0ZSgpO1xuICAgICAgICB2YXIgc3RhdGVQcm9wcyA9IHRoaXMuZG9TdGF0ZVByb3BzRGVwZW5kT25Pd25Qcm9wcyA/IHRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMoc3RhdGUsIHByb3BzKSA6IHRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMoc3RhdGUpO1xuXG4gICAgICAgIGlmIChcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICAgICAgY2hlY2tTdGF0ZVNoYXBlKHN0YXRlUHJvcHMsICdtYXBTdGF0ZVRvUHJvcHMnKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gc3RhdGVQcm9wcztcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbmZpZ3VyZUZpbmFsTWFwU3RhdGUgPSBmdW5jdGlvbiBjb25maWd1cmVGaW5hbE1hcFN0YXRlKHN0b3JlLCBwcm9wcykge1xuICAgICAgICB2YXIgbWFwcGVkU3RhdGUgPSBtYXBTdGF0ZShzdG9yZS5nZXRTdGF0ZSgpLCBwcm9wcyk7XG4gICAgICAgIHZhciBpc0ZhY3RvcnkgPSB0eXBlb2YgbWFwcGVkU3RhdGUgPT09ICdmdW5jdGlvbic7XG5cbiAgICAgICAgdGhpcy5maW5hbE1hcFN0YXRlVG9Qcm9wcyA9IGlzRmFjdG9yeSA/IG1hcHBlZFN0YXRlIDogbWFwU3RhdGU7XG4gICAgICAgIHRoaXMuZG9TdGF0ZVByb3BzRGVwZW5kT25Pd25Qcm9wcyA9IHRoaXMuZmluYWxNYXBTdGF0ZVRvUHJvcHMubGVuZ3RoICE9PSAxO1xuXG4gICAgICAgIGlmIChpc0ZhY3RvcnkpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5jb21wdXRlU3RhdGVQcm9wcyhzdG9yZSwgcHJvcHMpO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgICAgICBjaGVja1N0YXRlU2hhcGUobWFwcGVkU3RhdGUsICdtYXBTdGF0ZVRvUHJvcHMnKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gbWFwcGVkU3RhdGU7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jb21wdXRlRGlzcGF0Y2hQcm9wcyA9IGZ1bmN0aW9uIGNvbXB1dGVEaXNwYXRjaFByb3BzKHN0b3JlLCBwcm9wcykge1xuICAgICAgICBpZiAoIXRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5jb25maWd1cmVGaW5hbE1hcERpc3BhdGNoKHN0b3JlLCBwcm9wcyk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgZGlzcGF0Y2ggPSBzdG9yZS5kaXNwYXRjaDtcblxuICAgICAgICB2YXIgZGlzcGF0Y2hQcm9wcyA9IHRoaXMuZG9EaXNwYXRjaFByb3BzRGVwZW5kT25Pd25Qcm9wcyA/IHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMoZGlzcGF0Y2gsIHByb3BzKSA6IHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMoZGlzcGF0Y2gpO1xuXG4gICAgICAgIGlmIChcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICAgICAgY2hlY2tTdGF0ZVNoYXBlKGRpc3BhdGNoUHJvcHMsICdtYXBEaXNwYXRjaFRvUHJvcHMnKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gZGlzcGF0Y2hQcm9wcztcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbmZpZ3VyZUZpbmFsTWFwRGlzcGF0Y2ggPSBmdW5jdGlvbiBjb25maWd1cmVGaW5hbE1hcERpc3BhdGNoKHN0b3JlLCBwcm9wcykge1xuICAgICAgICB2YXIgbWFwcGVkRGlzcGF0Y2ggPSBtYXBEaXNwYXRjaChzdG9yZS5kaXNwYXRjaCwgcHJvcHMpO1xuICAgICAgICB2YXIgaXNGYWN0b3J5ID0gdHlwZW9mIG1hcHBlZERpc3BhdGNoID09PSAnZnVuY3Rpb24nO1xuXG4gICAgICAgIHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMgPSBpc0ZhY3RvcnkgPyBtYXBwZWREaXNwYXRjaCA6IG1hcERpc3BhdGNoO1xuICAgICAgICB0aGlzLmRvRGlzcGF0Y2hQcm9wc0RlcGVuZE9uT3duUHJvcHMgPSB0aGlzLmZpbmFsTWFwRGlzcGF0Y2hUb1Byb3BzLmxlbmd0aCAhPT0gMTtcblxuICAgICAgICBpZiAoaXNGYWN0b3J5KSB7XG4gICAgICAgICAgcmV0dXJuIHRoaXMuY29tcHV0ZURpc3BhdGNoUHJvcHMoc3RvcmUsIHByb3BzKTtcbiAgICAgICAgfVxuXG4gICAgICAgIGlmIChcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICAgICAgY2hlY2tTdGF0ZVNoYXBlKG1hcHBlZERpc3BhdGNoLCAnbWFwRGlzcGF0Y2hUb1Byb3BzJyk7XG4gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIG1hcHBlZERpc3BhdGNoO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUudXBkYXRlU3RhdGVQcm9wc0lmTmVlZGVkID0gZnVuY3Rpb24gdXBkYXRlU3RhdGVQcm9wc0lmTmVlZGVkKCkge1xuICAgICAgICB2YXIgbmV4dFN0YXRlUHJvcHMgPSB0aGlzLmNvbXB1dGVTdGF0ZVByb3BzKHRoaXMuc3RvcmUsIHRoaXMucHJvcHMpO1xuICAgICAgICBpZiAodGhpcy5zdGF0ZVByb3BzICYmICgwLCBfc2hhbGxvd0VxdWFsMltcImRlZmF1bHRcIl0pKG5leHRTdGF0ZVByb3BzLCB0aGlzLnN0YXRlUHJvcHMpKSB7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5zdGF0ZVByb3BzID0gbmV4dFN0YXRlUHJvcHM7XG4gICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUudXBkYXRlRGlzcGF0Y2hQcm9wc0lmTmVlZGVkID0gZnVuY3Rpb24gdXBkYXRlRGlzcGF0Y2hQcm9wc0lmTmVlZGVkKCkge1xuICAgICAgICB2YXIgbmV4dERpc3BhdGNoUHJvcHMgPSB0aGlzLmNvbXB1dGVEaXNwYXRjaFByb3BzKHRoaXMuc3RvcmUsIHRoaXMucHJvcHMpO1xuICAgICAgICBpZiAodGhpcy5kaXNwYXRjaFByb3BzICYmICgwLCBfc2hhbGxvd0VxdWFsMltcImRlZmF1bHRcIl0pKG5leHREaXNwYXRjaFByb3BzLCB0aGlzLmRpc3BhdGNoUHJvcHMpKSB7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5kaXNwYXRjaFByb3BzID0gbmV4dERpc3BhdGNoUHJvcHM7XG4gICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUudXBkYXRlTWVyZ2VkUHJvcHNJZk5lZWRlZCA9IGZ1bmN0aW9uIHVwZGF0ZU1lcmdlZFByb3BzSWZOZWVkZWQoKSB7XG4gICAgICAgIHZhciBuZXh0TWVyZ2VkUHJvcHMgPSBjb21wdXRlTWVyZ2VkUHJvcHModGhpcy5zdGF0ZVByb3BzLCB0aGlzLmRpc3BhdGNoUHJvcHMsIHRoaXMucHJvcHMpO1xuICAgICAgICBpZiAodGhpcy5tZXJnZWRQcm9wcyAmJiBjaGVja01lcmdlZEVxdWFscyAmJiAoMCwgX3NoYWxsb3dFcXVhbDJbXCJkZWZhdWx0XCJdKShuZXh0TWVyZ2VkUHJvcHMsIHRoaXMubWVyZ2VkUHJvcHMpKSB7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5tZXJnZWRQcm9wcyA9IG5leHRNZXJnZWRQcm9wcztcbiAgICAgICAgcmV0dXJuIHRydWU7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5pc1N1YnNjcmliZWQgPSBmdW5jdGlvbiBpc1N1YnNjcmliZWQoKSB7XG4gICAgICAgIHJldHVybiB0eXBlb2YgdGhpcy51bnN1YnNjcmliZSA9PT0gJ2Z1bmN0aW9uJztcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLnRyeVN1YnNjcmliZSA9IGZ1bmN0aW9uIHRyeVN1YnNjcmliZSgpIHtcbiAgICAgICAgaWYgKHNob3VsZFN1YnNjcmliZSAmJiAhdGhpcy51bnN1YnNjcmliZSkge1xuICAgICAgICAgIHRoaXMudW5zdWJzY3JpYmUgPSB0aGlzLnN0b3JlLnN1YnNjcmliZSh0aGlzLmhhbmRsZUNoYW5nZS5iaW5kKHRoaXMpKTtcbiAgICAgICAgICB0aGlzLmhhbmRsZUNoYW5nZSgpO1xuICAgICAgICB9XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS50cnlVbnN1YnNjcmliZSA9IGZ1bmN0aW9uIHRyeVVuc3Vic2NyaWJlKCkge1xuICAgICAgICBpZiAodGhpcy51bnN1YnNjcmliZSkge1xuICAgICAgICAgIHRoaXMudW5zdWJzY3JpYmUoKTtcbiAgICAgICAgICB0aGlzLnVuc3Vic2NyaWJlID0gbnVsbDtcbiAgICAgICAgfVxuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY29tcG9uZW50RGlkTW91bnQgPSBmdW5jdGlvbiBjb21wb25lbnREaWRNb3VudCgpIHtcbiAgICAgICAgdGhpcy50cnlTdWJzY3JpYmUoKTtcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMgPSBmdW5jdGlvbiBjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuICAgICAgICBpZiAoIXB1cmUgfHwgISgwLCBfc2hhbGxvd0VxdWFsMltcImRlZmF1bHRcIl0pKG5leHRQcm9wcywgdGhpcy5wcm9wcykpIHtcbiAgICAgICAgICB0aGlzLmhhdmVPd25Qcm9wc0NoYW5nZWQgPSB0cnVlO1xuICAgICAgICB9XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5jb21wb25lbnRXaWxsVW5tb3VudCA9IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgICAgICB0aGlzLnRyeVVuc3Vic2NyaWJlKCk7XG4gICAgICAgIHRoaXMuY2xlYXJDYWNoZSgpO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuY2xlYXJDYWNoZSA9IGZ1bmN0aW9uIGNsZWFyQ2FjaGUoKSB7XG4gICAgICAgIHRoaXMuZGlzcGF0Y2hQcm9wcyA9IG51bGw7XG4gICAgICAgIHRoaXMuc3RhdGVQcm9wcyA9IG51bGw7XG4gICAgICAgIHRoaXMubWVyZ2VkUHJvcHMgPSBudWxsO1xuICAgICAgICB0aGlzLmhhdmVPd25Qcm9wc0NoYW5nZWQgPSB0cnVlO1xuICAgICAgICB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkID0gdHJ1ZTtcbiAgICAgICAgdGhpcy5oYXZlU3RhdGVQcm9wc0JlZW5QcmVjYWxjdWxhdGVkID0gZmFsc2U7XG4gICAgICAgIHRoaXMuc3RhdGVQcm9wc1ByZWNhbGN1bGF0aW9uRXJyb3IgPSBudWxsO1xuICAgICAgICB0aGlzLnJlbmRlcmVkRWxlbWVudCA9IG51bGw7XG4gICAgICAgIHRoaXMuZmluYWxNYXBEaXNwYXRjaFRvUHJvcHMgPSBudWxsO1xuICAgICAgICB0aGlzLmZpbmFsTWFwU3RhdGVUb1Byb3BzID0gbnVsbDtcbiAgICAgIH07XG5cbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmhhbmRsZUNoYW5nZSA9IGZ1bmN0aW9uIGhhbmRsZUNoYW5nZSgpIHtcbiAgICAgICAgaWYgKCF0aGlzLnVuc3Vic2NyaWJlKSB7XG4gICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIHN0b3JlU3RhdGUgPSB0aGlzLnN0b3JlLmdldFN0YXRlKCk7XG4gICAgICAgIHZhciBwcmV2U3RvcmVTdGF0ZSA9IHRoaXMuc3RhdGUuc3RvcmVTdGF0ZTtcbiAgICAgICAgaWYgKHB1cmUgJiYgcHJldlN0b3JlU3RhdGUgPT09IHN0b3JlU3RhdGUpIHtcbiAgICAgICAgICByZXR1cm47XG4gICAgICAgIH1cblxuICAgICAgICBpZiAocHVyZSAmJiAhdGhpcy5kb1N0YXRlUHJvcHNEZXBlbmRPbk93blByb3BzKSB7XG4gICAgICAgICAgdmFyIGhhdmVTdGF0ZVByb3BzQ2hhbmdlZCA9IHRyeUNhdGNoKHRoaXMudXBkYXRlU3RhdGVQcm9wc0lmTmVlZGVkLCB0aGlzKTtcbiAgICAgICAgICBpZiAoIWhhdmVTdGF0ZVByb3BzQ2hhbmdlZCkge1xuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICAgIH1cbiAgICAgICAgICBpZiAoaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID09PSBlcnJvck9iamVjdCkge1xuICAgICAgICAgICAgdGhpcy5zdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvciA9IGVycm9yT2JqZWN0LnZhbHVlO1xuICAgICAgICAgIH1cbiAgICAgICAgICB0aGlzLmhhdmVTdGF0ZVByb3BzQmVlblByZWNhbGN1bGF0ZWQgPSB0cnVlO1xuICAgICAgICB9XG5cbiAgICAgICAgdGhpcy5oYXNTdG9yZVN0YXRlQ2hhbmdlZCA9IHRydWU7XG4gICAgICAgIHRoaXMuc2V0U3RhdGUoeyBzdG9yZVN0YXRlOiBzdG9yZVN0YXRlIH0pO1xuICAgICAgfTtcblxuICAgICAgQ29ubmVjdC5wcm90b3R5cGUuZ2V0V3JhcHBlZEluc3RhbmNlID0gZnVuY3Rpb24gZ2V0V3JhcHBlZEluc3RhbmNlKCkge1xuICAgICAgICAoMCwgX2ludmFyaWFudDJbXCJkZWZhdWx0XCJdKSh3aXRoUmVmLCAnVG8gYWNjZXNzIHRoZSB3cmFwcGVkIGluc3RhbmNlLCB5b3UgbmVlZCB0byBzcGVjaWZ5ICcgKyAneyB3aXRoUmVmOiB0cnVlIH0gYXMgdGhlIGZvdXJ0aCBhcmd1bWVudCBvZiB0aGUgY29ubmVjdCgpIGNhbGwuJyk7XG5cbiAgICAgICAgcmV0dXJuIHRoaXMucmVmcy53cmFwcGVkSW5zdGFuY2U7XG4gICAgICB9O1xuXG4gICAgICBDb25uZWN0LnByb3RvdHlwZS5yZW5kZXIgPSBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgICAgIHZhciBoYXZlT3duUHJvcHNDaGFuZ2VkID0gdGhpcy5oYXZlT3duUHJvcHNDaGFuZ2VkO1xuICAgICAgICB2YXIgaGFzU3RvcmVTdGF0ZUNoYW5nZWQgPSB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkO1xuICAgICAgICB2YXIgaGF2ZVN0YXRlUHJvcHNCZWVuUHJlY2FsY3VsYXRlZCA9IHRoaXMuaGF2ZVN0YXRlUHJvcHNCZWVuUHJlY2FsY3VsYXRlZDtcbiAgICAgICAgdmFyIHN0YXRlUHJvcHNQcmVjYWxjdWxhdGlvbkVycm9yID0gdGhpcy5zdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvcjtcbiAgICAgICAgdmFyIHJlbmRlcmVkRWxlbWVudCA9IHRoaXMucmVuZGVyZWRFbGVtZW50O1xuXG4gICAgICAgIHRoaXMuaGF2ZU93blByb3BzQ2hhbmdlZCA9IGZhbHNlO1xuICAgICAgICB0aGlzLmhhc1N0b3JlU3RhdGVDaGFuZ2VkID0gZmFsc2U7XG4gICAgICAgIHRoaXMuaGF2ZVN0YXRlUHJvcHNCZWVuUHJlY2FsY3VsYXRlZCA9IGZhbHNlO1xuICAgICAgICB0aGlzLnN0YXRlUHJvcHNQcmVjYWxjdWxhdGlvbkVycm9yID0gbnVsbDtcblxuICAgICAgICBpZiAoc3RhdGVQcm9wc1ByZWNhbGN1bGF0aW9uRXJyb3IpIHtcbiAgICAgICAgICB0aHJvdyBzdGF0ZVByb3BzUHJlY2FsY3VsYXRpb25FcnJvcjtcbiAgICAgICAgfVxuXG4gICAgICAgIHZhciBzaG91bGRVcGRhdGVTdGF0ZVByb3BzID0gdHJ1ZTtcbiAgICAgICAgdmFyIHNob3VsZFVwZGF0ZURpc3BhdGNoUHJvcHMgPSB0cnVlO1xuICAgICAgICBpZiAocHVyZSAmJiByZW5kZXJlZEVsZW1lbnQpIHtcbiAgICAgICAgICBzaG91bGRVcGRhdGVTdGF0ZVByb3BzID0gaGFzU3RvcmVTdGF0ZUNoYW5nZWQgfHwgaGF2ZU93blByb3BzQ2hhbmdlZCAmJiB0aGlzLmRvU3RhdGVQcm9wc0RlcGVuZE9uT3duUHJvcHM7XG4gICAgICAgICAgc2hvdWxkVXBkYXRlRGlzcGF0Y2hQcm9wcyA9IGhhdmVPd25Qcm9wc0NoYW5nZWQgJiYgdGhpcy5kb0Rpc3BhdGNoUHJvcHNEZXBlbmRPbk93blByb3BzO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIGhhdmVTdGF0ZVByb3BzQ2hhbmdlZCA9IGZhbHNlO1xuICAgICAgICB2YXIgaGF2ZURpc3BhdGNoUHJvcHNDaGFuZ2VkID0gZmFsc2U7XG4gICAgICAgIGlmIChoYXZlU3RhdGVQcm9wc0JlZW5QcmVjYWxjdWxhdGVkKSB7XG4gICAgICAgICAgaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID0gdHJ1ZTtcbiAgICAgICAgfSBlbHNlIGlmIChzaG91bGRVcGRhdGVTdGF0ZVByb3BzKSB7XG4gICAgICAgICAgaGF2ZVN0YXRlUHJvcHNDaGFuZ2VkID0gdGhpcy51cGRhdGVTdGF0ZVByb3BzSWZOZWVkZWQoKTtcbiAgICAgICAgfVxuICAgICAgICBpZiAoc2hvdWxkVXBkYXRlRGlzcGF0Y2hQcm9wcykge1xuICAgICAgICAgIGhhdmVEaXNwYXRjaFByb3BzQ2hhbmdlZCA9IHRoaXMudXBkYXRlRGlzcGF0Y2hQcm9wc0lmTmVlZGVkKCk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgaGF2ZU1lcmdlZFByb3BzQ2hhbmdlZCA9IHRydWU7XG4gICAgICAgIGlmIChoYXZlU3RhdGVQcm9wc0NoYW5nZWQgfHwgaGF2ZURpc3BhdGNoUHJvcHNDaGFuZ2VkIHx8IGhhdmVPd25Qcm9wc0NoYW5nZWQpIHtcbiAgICAgICAgICBoYXZlTWVyZ2VkUHJvcHNDaGFuZ2VkID0gdGhpcy51cGRhdGVNZXJnZWRQcm9wc0lmTmVlZGVkKCk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgaGF2ZU1lcmdlZFByb3BzQ2hhbmdlZCA9IGZhbHNlO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKCFoYXZlTWVyZ2VkUHJvcHNDaGFuZ2VkICYmIHJlbmRlcmVkRWxlbWVudCkge1xuICAgICAgICAgIHJldHVybiByZW5kZXJlZEVsZW1lbnQ7XG4gICAgICAgIH1cblxuICAgICAgICBpZiAod2l0aFJlZikge1xuICAgICAgICAgIHRoaXMucmVuZGVyZWRFbGVtZW50ID0gKDAsIF9yZWFjdC5jcmVhdGVFbGVtZW50KShXcmFwcGVkQ29tcG9uZW50LCBfZXh0ZW5kcyh7fSwgdGhpcy5tZXJnZWRQcm9wcywge1xuICAgICAgICAgICAgcmVmOiAnd3JhcHBlZEluc3RhbmNlJ1xuICAgICAgICAgIH0pKTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB0aGlzLnJlbmRlcmVkRWxlbWVudCA9ICgwLCBfcmVhY3QuY3JlYXRlRWxlbWVudCkoV3JhcHBlZENvbXBvbmVudCwgdGhpcy5tZXJnZWRQcm9wcyk7XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gdGhpcy5yZW5kZXJlZEVsZW1lbnQ7XG4gICAgICB9O1xuXG4gICAgICByZXR1cm4gQ29ubmVjdDtcbiAgICB9KF9yZWFjdC5Db21wb25lbnQpO1xuXG4gICAgQ29ubmVjdC5kaXNwbGF5TmFtZSA9IGNvbm5lY3REaXNwbGF5TmFtZTtcbiAgICBDb25uZWN0LldyYXBwZWRDb21wb25lbnQgPSBXcmFwcGVkQ29tcG9uZW50O1xuICAgIENvbm5lY3QuY29udGV4dFR5cGVzID0ge1xuICAgICAgc3RvcmU6IF9zdG9yZVNoYXBlMltcImRlZmF1bHRcIl1cbiAgICB9O1xuICAgIENvbm5lY3QucHJvcFR5cGVzID0ge1xuICAgICAgc3RvcmU6IF9zdG9yZVNoYXBlMltcImRlZmF1bHRcIl1cbiAgICB9O1xuXG4gICAgaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgIENvbm5lY3QucHJvdG90eXBlLmNvbXBvbmVudFdpbGxVcGRhdGUgPSBmdW5jdGlvbiBjb21wb25lbnRXaWxsVXBkYXRlKCkge1xuICAgICAgICBpZiAodGhpcy52ZXJzaW9uID09PSB2ZXJzaW9uKSB7XG4gICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG5cbiAgICAgICAgLy8gV2UgYXJlIGhvdCByZWxvYWRpbmchXG4gICAgICAgIHRoaXMudmVyc2lvbiA9IHZlcnNpb247XG4gICAgICAgIHRoaXMudHJ5U3Vic2NyaWJlKCk7XG4gICAgICAgIHRoaXMuY2xlYXJDYWNoZSgpO1xuICAgICAgfTtcbiAgICB9XG5cbiAgICByZXR1cm4gKDAsIF9ob2lzdE5vblJlYWN0U3RhdGljczJbXCJkZWZhdWx0XCJdKShDb25uZWN0LCBXcmFwcGVkQ29tcG9uZW50KTtcbiAgfTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmNvbm5lY3QgPSBleHBvcnRzLlByb3ZpZGVyID0gdW5kZWZpbmVkO1xuXG52YXIgX1Byb3ZpZGVyID0gcmVxdWlyZSgnLi9jb21wb25lbnRzL1Byb3ZpZGVyJyk7XG5cbnZhciBfUHJvdmlkZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUHJvdmlkZXIpO1xuXG52YXIgX2Nvbm5lY3QgPSByZXF1aXJlKCcuL2NvbXBvbmVudHMvY29ubmVjdCcpO1xuXG52YXIgX2Nvbm5lY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29ubmVjdCk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG5leHBvcnRzLlByb3ZpZGVyID0gX1Byb3ZpZGVyMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmNvbm5lY3QgPSBfY29ubmVjdDJbXCJkZWZhdWx0XCJdOyIsIlwidXNlIHN0cmljdFwiO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBzaGFsbG93RXF1YWw7XG5mdW5jdGlvbiBzaGFsbG93RXF1YWwob2JqQSwgb2JqQikge1xuICBpZiAob2JqQSA9PT0gb2JqQikge1xuICAgIHJldHVybiB0cnVlO1xuICB9XG5cbiAgdmFyIGtleXNBID0gT2JqZWN0LmtleXMob2JqQSk7XG4gIHZhciBrZXlzQiA9IE9iamVjdC5rZXlzKG9iakIpO1xuXG4gIGlmIChrZXlzQS5sZW5ndGggIT09IGtleXNCLmxlbmd0aCkge1xuICAgIHJldHVybiBmYWxzZTtcbiAgfVxuXG4gIC8vIFRlc3QgZm9yIEEncyBrZXlzIGRpZmZlcmVudCBmcm9tIEIuXG4gIHZhciBoYXNPd24gPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5O1xuICBmb3IgKHZhciBpID0gMDsgaSA8IGtleXNBLmxlbmd0aDsgaSsrKSB7XG4gICAgaWYgKCFoYXNPd24uY2FsbChvYmpCLCBrZXlzQVtpXSkgfHwgb2JqQVtrZXlzQVtpXV0gIT09IG9iakJba2V5c0FbaV1dKSB7XG4gICAgICByZXR1cm4gZmFsc2U7XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIHRydWU7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBfcmVhY3QuUHJvcFR5cGVzLnNoYXBlKHtcbiAgc3Vic2NyaWJlOiBfcmVhY3QuUHJvcFR5cGVzLmZ1bmMuaXNSZXF1aXJlZCxcbiAgZGlzcGF0Y2g6IF9yZWFjdC5Qcm9wVHlwZXMuZnVuYy5pc1JlcXVpcmVkLFxuICBnZXRTdGF0ZTogX3JlYWN0LlByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWRcbn0pOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gd2FybmluZztcbi8qKlxuICogUHJpbnRzIGEgd2FybmluZyBpbiB0aGUgY29uc29sZSBpZiBpdCBleGlzdHMuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd9IG1lc3NhZ2UgVGhlIHdhcm5pbmcgbWVzc2FnZS5cbiAqIEByZXR1cm5zIHt2b2lkfVxuICovXG5mdW5jdGlvbiB3YXJuaW5nKG1lc3NhZ2UpIHtcbiAgLyogZXNsaW50LWRpc2FibGUgbm8tY29uc29sZSAqL1xuICBpZiAodHlwZW9mIGNvbnNvbGUgIT09ICd1bmRlZmluZWQnICYmIHR5cGVvZiBjb25zb2xlLmVycm9yID09PSAnZnVuY3Rpb24nKSB7XG4gICAgY29uc29sZS5lcnJvcihtZXNzYWdlKTtcbiAgfVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWNvbnNvbGUgKi9cbiAgdHJ5IHtcbiAgICAvLyBUaGlzIGVycm9yIHdhcyB0aHJvd24gYXMgYSBjb252ZW5pZW5jZSBzbyB0aGF0IHlvdSBjYW4gdXNlIHRoaXMgc3RhY2tcbiAgICAvLyB0byBmaW5kIHRoZSBjYWxsc2l0ZSB0aGF0IGNhdXNlZCB0aGlzIHdhcm5pbmcgdG8gZmlyZS5cbiAgICB0aHJvdyBuZXcgRXJyb3IobWVzc2FnZSk7XG4gICAgLyogZXNsaW50LWRpc2FibGUgbm8tZW1wdHkgKi9cbiAgfSBjYXRjaCAoZSkge31cbiAgLyogZXNsaW50LWVuYWJsZSBuby1lbXB0eSAqL1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gd3JhcEFjdGlvbkNyZWF0b3JzO1xuXG52YXIgX3JlZHV4ID0gcmVxdWlyZSgncmVkdXgnKTtcblxuZnVuY3Rpb24gd3JhcEFjdGlvbkNyZWF0b3JzKGFjdGlvbkNyZWF0b3JzKSB7XG4gIHJldHVybiBmdW5jdGlvbiAoZGlzcGF0Y2gpIHtcbiAgICByZXR1cm4gKDAsIF9yZWR1eC5iaW5kQWN0aW9uQ3JlYXRvcnMpKGFjdGlvbkNyZWF0b3JzLCBkaXNwYXRjaCk7XG4gIH07XG59IiwiXCJ1c2Ugc3RyaWN0XCI7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmxvb3BBc3luYyA9IGxvb3BBc3luYztcbmV4cG9ydHMubWFwQXN5bmMgPSBtYXBBc3luYztcbmZ1bmN0aW9uIGxvb3BBc3luYyh0dXJucywgd29yaywgY2FsbGJhY2spIHtcbiAgdmFyIGN1cnJlbnRUdXJuID0gMCxcbiAgICAgIGlzRG9uZSA9IGZhbHNlO1xuICB2YXIgc3luYyA9IGZhbHNlLFxuICAgICAgaGFzTmV4dCA9IGZhbHNlLFxuICAgICAgZG9uZUFyZ3MgPSB2b2lkIDA7XG5cbiAgZnVuY3Rpb24gZG9uZSgpIHtcbiAgICBpc0RvbmUgPSB0cnVlO1xuICAgIGlmIChzeW5jKSB7XG4gICAgICAvLyBJdGVyYXRlIGluc3RlYWQgb2YgcmVjdXJzaW5nIGlmIHBvc3NpYmxlLlxuICAgICAgZG9uZUFyZ3MgPSBbXS5jb25jYXQoQXJyYXkucHJvdG90eXBlLnNsaWNlLmNhbGwoYXJndW1lbnRzKSk7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgY2FsbGJhY2suYXBwbHkodGhpcywgYXJndW1lbnRzKTtcbiAgfVxuXG4gIGZ1bmN0aW9uIG5leHQoKSB7XG4gICAgaWYgKGlzRG9uZSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGhhc05leHQgPSB0cnVlO1xuICAgIGlmIChzeW5jKSB7XG4gICAgICAvLyBJdGVyYXRlIGluc3RlYWQgb2YgcmVjdXJzaW5nIGlmIHBvc3NpYmxlLlxuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIHN5bmMgPSB0cnVlO1xuXG4gICAgd2hpbGUgKCFpc0RvbmUgJiYgY3VycmVudFR1cm4gPCB0dXJucyAmJiBoYXNOZXh0KSB7XG4gICAgICBoYXNOZXh0ID0gZmFsc2U7XG4gICAgICB3b3JrLmNhbGwodGhpcywgY3VycmVudFR1cm4rKywgbmV4dCwgZG9uZSk7XG4gICAgfVxuXG4gICAgc3luYyA9IGZhbHNlO1xuXG4gICAgaWYgKGlzRG9uZSkge1xuICAgICAgLy8gVGhpcyBtZWFucyB0aGUgbG9vcCBmaW5pc2hlZCBzeW5jaHJvbm91c2x5LlxuICAgICAgY2FsbGJhY2suYXBwbHkodGhpcywgZG9uZUFyZ3MpO1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGlmIChjdXJyZW50VHVybiA+PSB0dXJucyAmJiBoYXNOZXh0KSB7XG4gICAgICBpc0RvbmUgPSB0cnVlO1xuICAgICAgY2FsbGJhY2soKTtcbiAgICB9XG4gIH1cblxuICBuZXh0KCk7XG59XG5cbmZ1bmN0aW9uIG1hcEFzeW5jKGFycmF5LCB3b3JrLCBjYWxsYmFjaykge1xuICB2YXIgbGVuZ3RoID0gYXJyYXkubGVuZ3RoO1xuICB2YXIgdmFsdWVzID0gW107XG5cbiAgaWYgKGxlbmd0aCA9PT0gMCkgcmV0dXJuIGNhbGxiYWNrKG51bGwsIHZhbHVlcyk7XG5cbiAgdmFyIGlzRG9uZSA9IGZhbHNlLFxuICAgICAgZG9uZUNvdW50ID0gMDtcblxuICBmdW5jdGlvbiBkb25lKGluZGV4LCBlcnJvciwgdmFsdWUpIHtcbiAgICBpZiAoaXNEb25lKSByZXR1cm47XG5cbiAgICBpZiAoZXJyb3IpIHtcbiAgICAgIGlzRG9uZSA9IHRydWU7XG4gICAgICBjYWxsYmFjayhlcnJvcik7XG4gICAgfSBlbHNlIHtcbiAgICAgIHZhbHVlc1tpbmRleF0gPSB2YWx1ZTtcblxuICAgICAgaXNEb25lID0gKytkb25lQ291bnQgPT09IGxlbmd0aDtcblxuICAgICAgaWYgKGlzRG9uZSkgY2FsbGJhY2sobnVsbCwgdmFsdWVzKTtcbiAgICB9XG4gIH1cblxuICBhcnJheS5mb3JFYWNoKGZ1bmN0aW9uIChpdGVtLCBpbmRleCkge1xuICAgIHdvcmsoaXRlbSwgaW5kZXgsIGZ1bmN0aW9uIChlcnJvciwgdmFsdWUpIHtcbiAgICAgIGRvbmUoaW5kZXgsIGVycm9yLCB2YWx1ZSk7XG4gICAgfSk7XG4gIH0pO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9JbnRlcm5hbFByb3BUeXBlcyA9IHJlcXVpcmUoJy4vSW50ZXJuYWxQcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuLyoqXG4gKiBBIG1peGluIHRoYXQgYWRkcyB0aGUgXCJoaXN0b3J5XCIgaW5zdGFuY2UgdmFyaWFibGUgdG8gY29tcG9uZW50cy5cbiAqL1xudmFyIEhpc3RvcnkgPSB7XG5cbiAgY29udGV4dFR5cGVzOiB7XG4gICAgaGlzdG9yeTogX0ludGVybmFsUHJvcFR5cGVzLmhpc3RvcnlcbiAgfSxcblxuICBjb21wb25lbnRXaWxsTW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxNb3VudCgpIHtcbiAgICBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ3RoZSBgSGlzdG9yeWAgbWl4aW4gaXMgZGVwcmVjYXRlZCwgcGxlYXNlIGFjY2VzcyBgY29udGV4dC5yb3V0ZXJgIHdpdGggeW91ciBvd24gYGNvbnRleHRUeXBlc2AuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1oaXN0b3J5bWl4aW4nKSA6IHZvaWQgMDtcbiAgICB0aGlzLmhpc3RvcnkgPSB0aGlzLmNvbnRleHQuaGlzdG9yeTtcbiAgfVxufTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gSGlzdG9yeTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX0xpbmsgPSByZXF1aXJlKCcuL0xpbmsnKTtcblxudmFyIF9MaW5rMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0xpbmspO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG4vKipcbiAqIEFuIDxJbmRleExpbms+IGlzIHVzZWQgdG8gbGluayB0byBhbiA8SW5kZXhSb3V0ZT4uXG4gKi9cbnZhciBJbmRleExpbmsgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ0luZGV4TGluaycsXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHJldHVybiBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudChfTGluazIuZGVmYXVsdCwgX2V4dGVuZHMoe30sIHRoaXMucHJvcHMsIHsgb25seUFjdGl2ZU9uSW5kZXg6IHRydWUgfSkpO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gSW5kZXhMaW5rO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfUmVkaXJlY3QgPSByZXF1aXJlKCcuL1JlZGlyZWN0Jyk7XG5cbnZhciBfUmVkaXJlY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUmVkaXJlY3QpO1xuXG52YXIgX0ludGVybmFsUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9JbnRlcm5hbFByb3BUeXBlcycpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgc3RyaW5nID0gX1JlYWN0JFByb3BUeXBlcy5zdHJpbmc7XG52YXIgb2JqZWN0ID0gX1JlYWN0JFByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogQW4gPEluZGV4UmVkaXJlY3Q+IGlzIHVzZWQgdG8gcmVkaXJlY3QgZnJvbSBhbiBpbmRleFJvdXRlLlxuICovXG5cbnZhciBJbmRleFJlZGlyZWN0ID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdJbmRleFJlZGlyZWN0JyxcblxuXG4gIHN0YXRpY3M6IHtcbiAgICBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQ6IGZ1bmN0aW9uIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50LCBwYXJlbnRSb3V0ZSkge1xuICAgICAgLyogaXN0YW5idWwgaWdub3JlIGVsc2U6IHNhbml0eSBjaGVjayAqL1xuICAgICAgaWYgKHBhcmVudFJvdXRlKSB7XG4gICAgICAgIHBhcmVudFJvdXRlLmluZGV4Um91dGUgPSBfUmVkaXJlY3QyLmRlZmF1bHQuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdBbiA8SW5kZXhSZWRpcmVjdD4gZG9lcyBub3QgbWFrZSBzZW5zZSBhdCB0aGUgcm9vdCBvZiB5b3VyIHJvdXRlIGNvbmZpZycpIDogdm9pZCAwO1xuICAgICAgfVxuICAgIH1cbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICB0bzogc3RyaW5nLmlzUmVxdWlyZWQsXG4gICAgcXVlcnk6IG9iamVjdCxcbiAgICBzdGF0ZTogb2JqZWN0LFxuICAgIG9uRW50ZXI6IF9JbnRlcm5hbFByb3BUeXBlcy5mYWxzeSxcbiAgICBjaGlsZHJlbjogX0ludGVybmFsUHJvcFR5cGVzLmZhbHN5XG4gIH0sXG5cbiAgLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHNhbml0eSBjaGVjayAqL1xuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICAhZmFsc2UgPyBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnPEluZGV4UmVkaXJlY3Q+IGVsZW1lbnRzIGFyZSBmb3Igcm91dGVyIGNvbmZpZ3VyYXRpb24gb25seSBhbmQgc2hvdWxkIG5vdCBiZSByZW5kZXJlZCcpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IEluZGV4UmVkaXJlY3Q7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbnZhciBfSW50ZXJuYWxQcm9wVHlwZXMgPSByZXF1aXJlKCcuL0ludGVybmFsUHJvcFR5cGVzJyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBmdW5jID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcy5mdW5jO1xuXG4vKipcbiAqIEFuIDxJbmRleFJvdXRlPiBpcyB1c2VkIHRvIHNwZWNpZnkgaXRzIHBhcmVudCdzIDxSb3V0ZSBpbmRleFJvdXRlPiBpblxuICogYSBKU1ggcm91dGUgY29uZmlnLlxuICovXG5cbnZhciBJbmRleFJvdXRlID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdJbmRleFJvdXRlJyxcblxuXG4gIHN0YXRpY3M6IHtcbiAgICBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQ6IGZ1bmN0aW9uIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50LCBwYXJlbnRSb3V0ZSkge1xuICAgICAgLyogaXN0YW5idWwgaWdub3JlIGVsc2U6IHNhbml0eSBjaGVjayAqL1xuICAgICAgaWYgKHBhcmVudFJvdXRlKSB7XG4gICAgICAgIHBhcmVudFJvdXRlLmluZGV4Um91dGUgPSAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KShlbGVtZW50KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnQW4gPEluZGV4Um91dGU+IGRvZXMgbm90IG1ha2Ugc2Vuc2UgYXQgdGhlIHJvb3Qgb2YgeW91ciByb3V0ZSBjb25maWcnKSA6IHZvaWQgMDtcbiAgICAgIH1cbiAgICB9XG4gIH0sXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgcGF0aDogX0ludGVybmFsUHJvcFR5cGVzLmZhbHN5LFxuICAgIGNvbXBvbmVudDogX0ludGVybmFsUHJvcFR5cGVzLmNvbXBvbmVudCxcbiAgICBjb21wb25lbnRzOiBfSW50ZXJuYWxQcm9wVHlwZXMuY29tcG9uZW50cyxcbiAgICBnZXRDb21wb25lbnQ6IGZ1bmMsXG4gICAgZ2V0Q29tcG9uZW50czogZnVuY1xuICB9LFxuXG4gIC8qIGlzdGFuYnVsIGlnbm9yZSBuZXh0OiBzYW5pdHkgY2hlY2sgKi9cbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgIWZhbHNlID8gXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJzxJbmRleFJvdXRlPiBlbGVtZW50cyBhcmUgZm9yIHJvdXRlciBjb25maWd1cmF0aW9uIG9ubHkgYW5kIHNob3VsZCBub3QgYmUgcmVuZGVyZWQnKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBJbmRleFJvdXRlO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5yb3V0ZXMgPSBleHBvcnRzLnJvdXRlID0gZXhwb3J0cy5jb21wb25lbnRzID0gZXhwb3J0cy5jb21wb25lbnQgPSBleHBvcnRzLmhpc3RvcnkgPSB1bmRlZmluZWQ7XG5leHBvcnRzLmZhbHN5ID0gZmFsc3k7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgZnVuYyA9IF9yZWFjdC5Qcm9wVHlwZXMuZnVuYztcbnZhciBvYmplY3QgPSBfcmVhY3QuUHJvcFR5cGVzLm9iamVjdDtcbnZhciBhcnJheU9mID0gX3JlYWN0LlByb3BUeXBlcy5hcnJheU9mO1xudmFyIG9uZU9mVHlwZSA9IF9yZWFjdC5Qcm9wVHlwZXMub25lT2ZUeXBlO1xudmFyIGVsZW1lbnQgPSBfcmVhY3QuUHJvcFR5cGVzLmVsZW1lbnQ7XG52YXIgc2hhcGUgPSBfcmVhY3QuUHJvcFR5cGVzLnNoYXBlO1xudmFyIHN0cmluZyA9IF9yZWFjdC5Qcm9wVHlwZXMuc3RyaW5nO1xuZnVuY3Rpb24gZmFsc3kocHJvcHMsIHByb3BOYW1lLCBjb21wb25lbnROYW1lKSB7XG4gIGlmIChwcm9wc1twcm9wTmFtZV0pIHJldHVybiBuZXcgRXJyb3IoJzwnICsgY29tcG9uZW50TmFtZSArICc+IHNob3VsZCBub3QgaGF2ZSBhIFwiJyArIHByb3BOYW1lICsgJ1wiIHByb3AnKTtcbn1cblxudmFyIGhpc3RvcnkgPSBleHBvcnRzLmhpc3RvcnkgPSBzaGFwZSh7XG4gIGxpc3RlbjogZnVuYy5pc1JlcXVpcmVkLFxuICBwdXNoOiBmdW5jLmlzUmVxdWlyZWQsXG4gIHJlcGxhY2U6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ286IGZ1bmMuaXNSZXF1aXJlZCxcbiAgZ29CYWNrOiBmdW5jLmlzUmVxdWlyZWQsXG4gIGdvRm9yd2FyZDogZnVuYy5pc1JlcXVpcmVkXG59KTtcblxudmFyIGNvbXBvbmVudCA9IGV4cG9ydHMuY29tcG9uZW50ID0gb25lT2ZUeXBlKFtmdW5jLCBzdHJpbmddKTtcbnZhciBjb21wb25lbnRzID0gZXhwb3J0cy5jb21wb25lbnRzID0gb25lT2ZUeXBlKFtjb21wb25lbnQsIG9iamVjdF0pO1xudmFyIHJvdXRlID0gZXhwb3J0cy5yb3V0ZSA9IG9uZU9mVHlwZShbb2JqZWN0LCBlbGVtZW50XSk7XG52YXIgcm91dGVzID0gZXhwb3J0cy5yb3V0ZXMgPSBvbmVPZlR5cGUoW3JvdXRlLCBhcnJheU9mKHJvdXRlKV0pOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgb2JqZWN0ID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogVGhlIExpZmVjeWNsZSBtaXhpbiBhZGRzIHRoZSByb3V0ZXJXaWxsTGVhdmUgbGlmZWN5Y2xlIG1ldGhvZCB0byBhXG4gKiBjb21wb25lbnQgdGhhdCBtYXkgYmUgdXNlZCB0byBjYW5jZWwgYSB0cmFuc2l0aW9uIG9yIHByb21wdCB0aGUgdXNlclxuICogZm9yIGNvbmZpcm1hdGlvbi5cbiAqXG4gKiBPbiBzdGFuZGFyZCB0cmFuc2l0aW9ucywgcm91dGVyV2lsbExlYXZlIHJlY2VpdmVzIGEgc2luZ2xlIGFyZ3VtZW50OiB0aGVcbiAqIGxvY2F0aW9uIHdlJ3JlIHRyYW5zaXRpb25pbmcgdG8uIFRvIGNhbmNlbCB0aGUgdHJhbnNpdGlvbiwgcmV0dXJuIGZhbHNlLlxuICogVG8gcHJvbXB0IHRoZSB1c2VyIGZvciBjb25maXJtYXRpb24sIHJldHVybiBhIHByb21wdCBtZXNzYWdlIChzdHJpbmcpLlxuICpcbiAqIER1cmluZyB0aGUgYmVmb3JldW5sb2FkIGV2ZW50IChhc3N1bWluZyB5b3UncmUgdXNpbmcgdGhlIHVzZUJlZm9yZVVubG9hZFxuICogaGlzdG9yeSBlbmhhbmNlciksIHJvdXRlcldpbGxMZWF2ZSBkb2VzIG5vdCByZWNlaXZlIGEgbG9jYXRpb24gb2JqZWN0XG4gKiBiZWNhdXNlIGl0IGlzbid0IHBvc3NpYmxlIGZvciB1cyB0byBrbm93IHRoZSBsb2NhdGlvbiB3ZSdyZSB0cmFuc2l0aW9uaW5nXG4gKiB0by4gSW4gdGhpcyBjYXNlIHJvdXRlcldpbGxMZWF2ZSBtdXN0IHJldHVybiBhIHByb21wdCBtZXNzYWdlIHRvIHByZXZlbnRcbiAqIHRoZSB1c2VyIGZyb20gY2xvc2luZyB0aGUgd2luZG93L3RhYi5cbiAqL1xuXG52YXIgTGlmZWN5Y2xlID0ge1xuXG4gIGNvbnRleHRUeXBlczoge1xuICAgIGhpc3Rvcnk6IG9iamVjdC5pc1JlcXVpcmVkLFxuICAgIC8vIE5lc3RlZCBjaGlsZHJlbiByZWNlaXZlIHRoZSByb3V0ZSBhcyBjb250ZXh0LCBlaXRoZXJcbiAgICAvLyBzZXQgYnkgdGhlIHJvdXRlIGNvbXBvbmVudCB1c2luZyB0aGUgUm91dGVDb250ZXh0IG1peGluXG4gICAgLy8gb3IgYnkgc29tZSBvdGhlciBhbmNlc3Rvci5cbiAgICByb3V0ZTogb2JqZWN0XG4gIH0sXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgLy8gUm91dGUgY29tcG9uZW50cyByZWNlaXZlIHRoZSByb3V0ZSBvYmplY3QgYXMgYSBwcm9wLlxuICAgIHJvdXRlOiBvYmplY3RcbiAgfSxcblxuICBjb21wb25lbnREaWRNb3VudDogZnVuY3Rpb24gY29tcG9uZW50RGlkTW91bnQoKSB7XG4gICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICd0aGUgYExpZmVjeWNsZWAgbWl4aW4gaXMgZGVwcmVjYXRlZCwgcGxlYXNlIHVzZSBgY29udGV4dC5yb3V0ZXIuc2V0Um91dGVMZWF2ZUhvb2socm91dGUsIGhvb2spYC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWxpZmVjeWNsZW1peGluJykgOiB2b2lkIDA7XG4gICAgIXRoaXMucm91dGVyV2lsbExlYXZlID8gXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJ1RoZSBMaWZlY3ljbGUgbWl4aW4gcmVxdWlyZXMgeW91IHRvIGRlZmluZSBhIHJvdXRlcldpbGxMZWF2ZSBtZXRob2QnKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICB2YXIgcm91dGUgPSB0aGlzLnByb3BzLnJvdXRlIHx8IHRoaXMuY29udGV4dC5yb3V0ZTtcblxuICAgICFyb3V0ZSA/IFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdUaGUgTGlmZWN5Y2xlIG1peGluIG11c3QgYmUgdXNlZCBvbiBlaXRoZXIgYSkgYSA8Um91dGUgY29tcG9uZW50PiBvciAnICsgJ2IpIGEgZGVzY2VuZGFudCBvZiBhIDxSb3V0ZSBjb21wb25lbnQ+IHRoYXQgdXNlcyB0aGUgUm91dGVDb250ZXh0IG1peGluJykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuXG4gICAgdGhpcy5fdW5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUgPSB0aGlzLmNvbnRleHQuaGlzdG9yeS5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUocm91dGUsIHRoaXMucm91dGVyV2lsbExlYXZlKTtcbiAgfSxcbiAgY29tcG9uZW50V2lsbFVubW91bnQ6IGZ1bmN0aW9uIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgIGlmICh0aGlzLl91bmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZSkgdGhpcy5fdW5saXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUoKTtcbiAgfVxufTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gTGlmZWN5Y2xlO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbnZhciBfUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9Qcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKG9iaiwga2V5cykgeyB2YXIgdGFyZ2V0ID0ge307IGZvciAodmFyIGkgaW4gb2JqKSB7IGlmIChrZXlzLmluZGV4T2YoaSkgPj0gMCkgY29udGludWU7IGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwgaSkpIGNvbnRpbnVlOyB0YXJnZXRbaV0gPSBvYmpbaV07IH0gcmV0dXJuIHRhcmdldDsgfVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgYm9vbCA9IF9SZWFjdCRQcm9wVHlwZXMuYm9vbDtcbnZhciBvYmplY3QgPSBfUmVhY3QkUHJvcFR5cGVzLm9iamVjdDtcbnZhciBzdHJpbmcgPSBfUmVhY3QkUHJvcFR5cGVzLnN0cmluZztcbnZhciBmdW5jID0gX1JlYWN0JFByb3BUeXBlcy5mdW5jO1xudmFyIG9uZU9mVHlwZSA9IF9SZWFjdCRQcm9wVHlwZXMub25lT2ZUeXBlO1xuXG5cbmZ1bmN0aW9uIGlzTGVmdENsaWNrRXZlbnQoZXZlbnQpIHtcbiAgcmV0dXJuIGV2ZW50LmJ1dHRvbiA9PT0gMDtcbn1cblxuZnVuY3Rpb24gaXNNb2RpZmllZEV2ZW50KGV2ZW50KSB7XG4gIHJldHVybiAhIShldmVudC5tZXRhS2V5IHx8IGV2ZW50LmFsdEtleSB8fCBldmVudC5jdHJsS2V5IHx8IGV2ZW50LnNoaWZ0S2V5KTtcbn1cblxuLy8gVE9ETzogRGUtZHVwbGljYXRlIGFnYWluc3QgaGFzQW55UHJvcGVydGllcyBpbiBjcmVhdGVUcmFuc2l0aW9uTWFuYWdlci5cbmZ1bmN0aW9uIGlzRW1wdHlPYmplY3Qob2JqZWN0KSB7XG4gIGZvciAodmFyIHAgaW4gb2JqZWN0KSB7XG4gICAgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmplY3QsIHApKSByZXR1cm4gZmFsc2U7XG4gIH1yZXR1cm4gdHJ1ZTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlTG9jYXRpb25EZXNjcmlwdG9yKHRvLCBfcmVmKSB7XG4gIHZhciBxdWVyeSA9IF9yZWYucXVlcnk7XG4gIHZhciBoYXNoID0gX3JlZi5oYXNoO1xuICB2YXIgc3RhdGUgPSBfcmVmLnN0YXRlO1xuXG4gIGlmIChxdWVyeSB8fCBoYXNoIHx8IHN0YXRlKSB7XG4gICAgcmV0dXJuIHsgcGF0aG5hbWU6IHRvLCBxdWVyeTogcXVlcnksIGhhc2g6IGhhc2gsIHN0YXRlOiBzdGF0ZSB9O1xuICB9XG5cbiAgcmV0dXJuIHRvO1xufVxuXG4vKipcbiAqIEEgPExpbms+IGlzIHVzZWQgdG8gY3JlYXRlIGFuIDxhPiBlbGVtZW50IHRoYXQgbGlua3MgdG8gYSByb3V0ZS5cbiAqIFdoZW4gdGhhdCByb3V0ZSBpcyBhY3RpdmUsIHRoZSBsaW5rIGdldHMgdGhlIHZhbHVlIG9mIGl0c1xuICogYWN0aXZlQ2xhc3NOYW1lIHByb3AuXG4gKlxuICogRm9yIGV4YW1wbGUsIGFzc3VtaW5nIHlvdSBoYXZlIHRoZSBmb2xsb3dpbmcgcm91dGU6XG4gKlxuICogICA8Um91dGUgcGF0aD1cIi9wb3N0cy86cG9zdElEXCIgY29tcG9uZW50PXtQb3N0fSAvPlxuICpcbiAqIFlvdSBjb3VsZCB1c2UgdGhlIGZvbGxvd2luZyBjb21wb25lbnQgdG8gbGluayB0byB0aGF0IHJvdXRlOlxuICpcbiAqICAgPExpbmsgdG89e2AvcG9zdHMvJHtwb3N0LmlkfWB9IC8+XG4gKlxuICogTGlua3MgbWF5IHBhc3MgYWxvbmcgbG9jYXRpb24gc3RhdGUgYW5kL29yIHF1ZXJ5IHN0cmluZyBwYXJhbWV0ZXJzXG4gKiBpbiB0aGUgc3RhdGUvcXVlcnkgcHJvcHMsIHJlc3BlY3RpdmVseS5cbiAqXG4gKiAgIDxMaW5rIC4uLiBxdWVyeT17eyBzaG93OiB0cnVlIH19IHN0YXRlPXt7IHRoZTogJ3N0YXRlJyB9fSAvPlxuICovXG52YXIgTGluayA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnTGluaycsXG5cblxuICBjb250ZXh0VHlwZXM6IHtcbiAgICByb3V0ZXI6IF9Qcm9wVHlwZXMucm91dGVyU2hhcGVcbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICB0bzogb25lT2ZUeXBlKFtzdHJpbmcsIG9iamVjdF0pLmlzUmVxdWlyZWQsXG4gICAgcXVlcnk6IG9iamVjdCxcbiAgICBoYXNoOiBzdHJpbmcsXG4gICAgc3RhdGU6IG9iamVjdCxcbiAgICBhY3RpdmVTdHlsZTogb2JqZWN0LFxuICAgIGFjdGl2ZUNsYXNzTmFtZTogc3RyaW5nLFxuICAgIG9ubHlBY3RpdmVPbkluZGV4OiBib29sLmlzUmVxdWlyZWQsXG4gICAgb25DbGljazogZnVuYyxcbiAgICB0YXJnZXQ6IHN0cmluZ1xuICB9LFxuXG4gIGdldERlZmF1bHRQcm9wczogZnVuY3Rpb24gZ2V0RGVmYXVsdFByb3BzKCkge1xuICAgIHJldHVybiB7XG4gICAgICBvbmx5QWN0aXZlT25JbmRleDogZmFsc2UsXG4gICAgICBzdHlsZToge31cbiAgICB9O1xuICB9LFxuICBoYW5kbGVDbGljazogZnVuY3Rpb24gaGFuZGxlQ2xpY2soZXZlbnQpIHtcbiAgICBpZiAodGhpcy5wcm9wcy5vbkNsaWNrKSB0aGlzLnByb3BzLm9uQ2xpY2soZXZlbnQpO1xuXG4gICAgaWYgKGV2ZW50LmRlZmF1bHRQcmV2ZW50ZWQpIHJldHVybjtcblxuICAgICF0aGlzLmNvbnRleHQucm91dGVyID8gXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJzxMaW5rPnMgcmVuZGVyZWQgb3V0c2lkZSBvZiBhIHJvdXRlciBjb250ZXh0IGNhbm5vdCBuYXZpZ2F0ZS4nKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICBpZiAoaXNNb2RpZmllZEV2ZW50KGV2ZW50KSB8fCAhaXNMZWZ0Q2xpY2tFdmVudChldmVudCkpIHJldHVybjtcblxuICAgIC8vIElmIHRhcmdldCBwcm9wIGlzIHNldCAoZS5nLiB0byBcIl9ibGFua1wiKSwgbGV0IGJyb3dzZXIgaGFuZGxlIGxpbmsuXG4gICAgLyogaXN0YW5idWwgaWdub3JlIGlmOiB1bnRlc3RhYmxlIHdpdGggS2FybWEgKi9cbiAgICBpZiAodGhpcy5wcm9wcy50YXJnZXQpIHJldHVybjtcblxuICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XG5cbiAgICB2YXIgX3Byb3BzID0gdGhpcy5wcm9wcztcbiAgICB2YXIgdG8gPSBfcHJvcHMudG87XG4gICAgdmFyIHF1ZXJ5ID0gX3Byb3BzLnF1ZXJ5O1xuICAgIHZhciBoYXNoID0gX3Byb3BzLmhhc2g7XG4gICAgdmFyIHN0YXRlID0gX3Byb3BzLnN0YXRlO1xuXG4gICAgdmFyIGxvY2F0aW9uID0gY3JlYXRlTG9jYXRpb25EZXNjcmlwdG9yKHRvLCB7IHF1ZXJ5OiBxdWVyeSwgaGFzaDogaGFzaCwgc3RhdGU6IHN0YXRlIH0pO1xuXG4gICAgdGhpcy5jb250ZXh0LnJvdXRlci5wdXNoKGxvY2F0aW9uKTtcbiAgfSxcbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgdmFyIF9wcm9wczIgPSB0aGlzLnByb3BzO1xuICAgIHZhciB0byA9IF9wcm9wczIudG87XG4gICAgdmFyIHF1ZXJ5ID0gX3Byb3BzMi5xdWVyeTtcbiAgICB2YXIgaGFzaCA9IF9wcm9wczIuaGFzaDtcbiAgICB2YXIgc3RhdGUgPSBfcHJvcHMyLnN0YXRlO1xuICAgIHZhciBhY3RpdmVDbGFzc05hbWUgPSBfcHJvcHMyLmFjdGl2ZUNsYXNzTmFtZTtcbiAgICB2YXIgYWN0aXZlU3R5bGUgPSBfcHJvcHMyLmFjdGl2ZVN0eWxlO1xuICAgIHZhciBvbmx5QWN0aXZlT25JbmRleCA9IF9wcm9wczIub25seUFjdGl2ZU9uSW5kZXg7XG5cbiAgICB2YXIgcHJvcHMgPSBfb2JqZWN0V2l0aG91dFByb3BlcnRpZXMoX3Byb3BzMiwgWyd0bycsICdxdWVyeScsICdoYXNoJywgJ3N0YXRlJywgJ2FjdGl2ZUNsYXNzTmFtZScsICdhY3RpdmVTdHlsZScsICdvbmx5QWN0aXZlT25JbmRleCddKTtcblxuICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKCEocXVlcnkgfHwgaGFzaCB8fCBzdGF0ZSksICd0aGUgYHF1ZXJ5YCwgYGhhc2hgLCBhbmQgYHN0YXRlYCBwcm9wcyBvbiBgPExpbms+YCBhcmUgZGVwcmVjYXRlZCwgdXNlIGA8TGluayB0bz17eyBwYXRobmFtZSwgcXVlcnksIGhhc2gsIHN0YXRlIH19Lz4uIGh0dHA6Ly90aW55LmNjL3JvdXRlci1pc0FjdGl2ZWRlcHJlY2F0ZWQnKSA6IHZvaWQgMDtcblxuICAgIC8vIElnbm9yZSBpZiByZW5kZXJlZCBvdXRzaWRlIHRoZSBjb250ZXh0IG9mIHJvdXRlciwgc2ltcGxpZmllcyB1bml0IHRlc3RpbmcuXG4gICAgdmFyIHJvdXRlciA9IHRoaXMuY29udGV4dC5yb3V0ZXI7XG5cblxuICAgIGlmIChyb3V0ZXIpIHtcbiAgICAgIHZhciBsb2NhdGlvbiA9IGNyZWF0ZUxvY2F0aW9uRGVzY3JpcHRvcih0bywgeyBxdWVyeTogcXVlcnksIGhhc2g6IGhhc2gsIHN0YXRlOiBzdGF0ZSB9KTtcbiAgICAgIHByb3BzLmhyZWYgPSByb3V0ZXIuY3JlYXRlSHJlZihsb2NhdGlvbik7XG5cbiAgICAgIGlmIChhY3RpdmVDbGFzc05hbWUgfHwgYWN0aXZlU3R5bGUgIT0gbnVsbCAmJiAhaXNFbXB0eU9iamVjdChhY3RpdmVTdHlsZSkpIHtcbiAgICAgICAgaWYgKHJvdXRlci5pc0FjdGl2ZShsb2NhdGlvbiwgb25seUFjdGl2ZU9uSW5kZXgpKSB7XG4gICAgICAgICAgaWYgKGFjdGl2ZUNsYXNzTmFtZSkge1xuICAgICAgICAgICAgaWYgKHByb3BzLmNsYXNzTmFtZSkge1xuICAgICAgICAgICAgICBwcm9wcy5jbGFzc05hbWUgKz0gJyAnICsgYWN0aXZlQ2xhc3NOYW1lO1xuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgcHJvcHMuY2xhc3NOYW1lID0gYWN0aXZlQ2xhc3NOYW1lO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH1cblxuICAgICAgICAgIGlmIChhY3RpdmVTdHlsZSkgcHJvcHMuc3R5bGUgPSBfZXh0ZW5kcyh7fSwgcHJvcHMuc3R5bGUsIGFjdGl2ZVN0eWxlKTtcbiAgICAgICAgfVxuICAgICAgfVxuICAgIH1cblxuICAgIHJldHVybiBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudCgnYScsIF9leHRlbmRzKHt9LCBwcm9wcywgeyBvbkNsaWNrOiB0aGlzLmhhbmRsZUNsaWNrIH0pKTtcbiAgfVxufSk7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IExpbms7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmNvbXBpbGVQYXR0ZXJuID0gY29tcGlsZVBhdHRlcm47XG5leHBvcnRzLm1hdGNoUGF0dGVybiA9IG1hdGNoUGF0dGVybjtcbmV4cG9ydHMuZ2V0UGFyYW1OYW1lcyA9IGdldFBhcmFtTmFtZXM7XG5leHBvcnRzLmdldFBhcmFtcyA9IGdldFBhcmFtcztcbmV4cG9ydHMuZm9ybWF0UGF0dGVybiA9IGZvcm1hdFBhdHRlcm47XG5cbnZhciBfaW52YXJpYW50ID0gcmVxdWlyZSgnaW52YXJpYW50Jyk7XG5cbnZhciBfaW52YXJpYW50MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2ludmFyaWFudCk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIGVzY2FwZVJlZ0V4cChzdHJpbmcpIHtcbiAgcmV0dXJuIHN0cmluZy5yZXBsYWNlKC9bLiorP14ke30oKXxbXFxdXFxcXF0vZywgJ1xcXFwkJicpO1xufVxuXG5mdW5jdGlvbiBfY29tcGlsZVBhdHRlcm4ocGF0dGVybikge1xuICB2YXIgcmVnZXhwU291cmNlID0gJyc7XG4gIHZhciBwYXJhbU5hbWVzID0gW107XG4gIHZhciB0b2tlbnMgPSBbXTtcblxuICB2YXIgbWF0Y2ggPSB2b2lkIDAsXG4gICAgICBsYXN0SW5kZXggPSAwLFxuICAgICAgbWF0Y2hlciA9IC86KFthLXpBLVpfJF1bYS16QS1aMC05XyRdKil8XFwqXFwqfFxcKnxcXCh8XFwpL2c7XG4gIHdoaWxlIChtYXRjaCA9IG1hdGNoZXIuZXhlYyhwYXR0ZXJuKSkge1xuICAgIGlmIChtYXRjaC5pbmRleCAhPT0gbGFzdEluZGV4KSB7XG4gICAgICB0b2tlbnMucHVzaChwYXR0ZXJuLnNsaWNlKGxhc3RJbmRleCwgbWF0Y2guaW5kZXgpKTtcbiAgICAgIHJlZ2V4cFNvdXJjZSArPSBlc2NhcGVSZWdFeHAocGF0dGVybi5zbGljZShsYXN0SW5kZXgsIG1hdGNoLmluZGV4KSk7XG4gICAgfVxuXG4gICAgaWYgKG1hdGNoWzFdKSB7XG4gICAgICByZWdleHBTb3VyY2UgKz0gJyhbXi9dKyknO1xuICAgICAgcGFyYW1OYW1lcy5wdXNoKG1hdGNoWzFdKTtcbiAgICB9IGVsc2UgaWYgKG1hdGNoWzBdID09PSAnKionKSB7XG4gICAgICByZWdleHBTb3VyY2UgKz0gJyguKiknO1xuICAgICAgcGFyYW1OYW1lcy5wdXNoKCdzcGxhdCcpO1xuICAgIH0gZWxzZSBpZiAobWF0Y2hbMF0gPT09ICcqJykge1xuICAgICAgcmVnZXhwU291cmNlICs9ICcoLio/KSc7XG4gICAgICBwYXJhbU5hbWVzLnB1c2goJ3NwbGF0Jyk7XG4gICAgfSBlbHNlIGlmIChtYXRjaFswXSA9PT0gJygnKSB7XG4gICAgICByZWdleHBTb3VyY2UgKz0gJyg/Oic7XG4gICAgfSBlbHNlIGlmIChtYXRjaFswXSA9PT0gJyknKSB7XG4gICAgICByZWdleHBTb3VyY2UgKz0gJyk/JztcbiAgICB9XG5cbiAgICB0b2tlbnMucHVzaChtYXRjaFswXSk7XG5cbiAgICBsYXN0SW5kZXggPSBtYXRjaGVyLmxhc3RJbmRleDtcbiAgfVxuXG4gIGlmIChsYXN0SW5kZXggIT09IHBhdHRlcm4ubGVuZ3RoKSB7XG4gICAgdG9rZW5zLnB1c2gocGF0dGVybi5zbGljZShsYXN0SW5kZXgsIHBhdHRlcm4ubGVuZ3RoKSk7XG4gICAgcmVnZXhwU291cmNlICs9IGVzY2FwZVJlZ0V4cChwYXR0ZXJuLnNsaWNlKGxhc3RJbmRleCwgcGF0dGVybi5sZW5ndGgpKTtcbiAgfVxuXG4gIHJldHVybiB7XG4gICAgcGF0dGVybjogcGF0dGVybixcbiAgICByZWdleHBTb3VyY2U6IHJlZ2V4cFNvdXJjZSxcbiAgICBwYXJhbU5hbWVzOiBwYXJhbU5hbWVzLFxuICAgIHRva2VuczogdG9rZW5zXG4gIH07XG59XG5cbnZhciBDb21waWxlZFBhdHRlcm5zQ2FjaGUgPSBPYmplY3QuY3JlYXRlKG51bGwpO1xuXG5mdW5jdGlvbiBjb21waWxlUGF0dGVybihwYXR0ZXJuKSB7XG4gIGlmICghQ29tcGlsZWRQYXR0ZXJuc0NhY2hlW3BhdHRlcm5dKSBDb21waWxlZFBhdHRlcm5zQ2FjaGVbcGF0dGVybl0gPSBfY29tcGlsZVBhdHRlcm4ocGF0dGVybik7XG5cbiAgcmV0dXJuIENvbXBpbGVkUGF0dGVybnNDYWNoZVtwYXR0ZXJuXTtcbn1cblxuLyoqXG4gKiBBdHRlbXB0cyB0byBtYXRjaCBhIHBhdHRlcm4gb24gdGhlIGdpdmVuIHBhdGhuYW1lLiBQYXR0ZXJucyBtYXkgdXNlXG4gKiB0aGUgZm9sbG93aW5nIHNwZWNpYWwgY2hhcmFjdGVyczpcbiAqXG4gKiAtIDpwYXJhbU5hbWUgICAgIE1hdGNoZXMgYSBVUkwgc2VnbWVudCB1cCB0byB0aGUgbmV4dCAvLCA/LCBvciAjLiBUaGVcbiAqICAgICAgICAgICAgICAgICAgY2FwdHVyZWQgc3RyaW5nIGlzIGNvbnNpZGVyZWQgYSBcInBhcmFtXCJcbiAqIC0gKCkgICAgICAgICAgICAgV3JhcHMgYSBzZWdtZW50IG9mIHRoZSBVUkwgdGhhdCBpcyBvcHRpb25hbFxuICogLSAqICAgICAgICAgICAgICBDb25zdW1lcyAobm9uLWdyZWVkeSkgYWxsIGNoYXJhY3RlcnMgdXAgdG8gdGhlIG5leHRcbiAqICAgICAgICAgICAgICAgICAgY2hhcmFjdGVyIGluIHRoZSBwYXR0ZXJuLCBvciB0byB0aGUgZW5kIG9mIHRoZSBVUkwgaWZcbiAqICAgICAgICAgICAgICAgICAgdGhlcmUgaXMgbm9uZVxuICogLSAqKiAgICAgICAgICAgICBDb25zdW1lcyAoZ3JlZWR5KSBhbGwgY2hhcmFjdGVycyB1cCB0byB0aGUgbmV4dCBjaGFyYWN0ZXJcbiAqICAgICAgICAgICAgICAgICAgaW4gdGhlIHBhdHRlcm4sIG9yIHRvIHRoZSBlbmQgb2YgdGhlIFVSTCBpZiB0aGVyZSBpcyBub25lXG4gKlxuICogIFRoZSBmdW5jdGlvbiBjYWxscyBjYWxsYmFjayhlcnJvciwgbWF0Y2hlZCkgd2hlbiBmaW5pc2hlZC5cbiAqIFRoZSByZXR1cm4gdmFsdWUgaXMgYW4gb2JqZWN0IHdpdGggdGhlIGZvbGxvd2luZyBwcm9wZXJ0aWVzOlxuICpcbiAqIC0gcmVtYWluaW5nUGF0aG5hbWVcbiAqIC0gcGFyYW1OYW1lc1xuICogLSBwYXJhbVZhbHVlc1xuICovXG5mdW5jdGlvbiBtYXRjaFBhdHRlcm4ocGF0dGVybiwgcGF0aG5hbWUpIHtcbiAgLy8gRW5zdXJlIHBhdHRlcm4gc3RhcnRzIHdpdGggbGVhZGluZyBzbGFzaCBmb3IgY29uc2lzdGVuY3kgd2l0aCBwYXRobmFtZS5cbiAgaWYgKHBhdHRlcm4uY2hhckF0KDApICE9PSAnLycpIHtcbiAgICBwYXR0ZXJuID0gJy8nICsgcGF0dGVybjtcbiAgfVxuXG4gIHZhciBfY29tcGlsZVBhdHRlcm4yID0gY29tcGlsZVBhdHRlcm4ocGF0dGVybik7XG5cbiAgdmFyIHJlZ2V4cFNvdXJjZSA9IF9jb21waWxlUGF0dGVybjIucmVnZXhwU291cmNlO1xuICB2YXIgcGFyYW1OYW1lcyA9IF9jb21waWxlUGF0dGVybjIucGFyYW1OYW1lcztcbiAgdmFyIHRva2VucyA9IF9jb21waWxlUGF0dGVybjIudG9rZW5zO1xuXG5cbiAgaWYgKHBhdHRlcm4uY2hhckF0KHBhdHRlcm4ubGVuZ3RoIC0gMSkgIT09ICcvJykge1xuICAgIHJlZ2V4cFNvdXJjZSArPSAnLz8nOyAvLyBBbGxvdyBvcHRpb25hbCBwYXRoIHNlcGFyYXRvciBhdCBlbmQuXG4gIH1cblxuICAvLyBTcGVjaWFsLWNhc2UgcGF0dGVybnMgbGlrZSAnKicgZm9yIGNhdGNoLWFsbCByb3V0ZXMuXG4gIGlmICh0b2tlbnNbdG9rZW5zLmxlbmd0aCAtIDFdID09PSAnKicpIHtcbiAgICByZWdleHBTb3VyY2UgKz0gJyQnO1xuICB9XG5cbiAgdmFyIG1hdGNoID0gcGF0aG5hbWUubWF0Y2gobmV3IFJlZ0V4cCgnXicgKyByZWdleHBTb3VyY2UsICdpJykpO1xuICBpZiAobWF0Y2ggPT0gbnVsbCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG5cbiAgdmFyIG1hdGNoZWRQYXRoID0gbWF0Y2hbMF07XG4gIHZhciByZW1haW5pbmdQYXRobmFtZSA9IHBhdGhuYW1lLnN1YnN0cihtYXRjaGVkUGF0aC5sZW5ndGgpO1xuXG4gIGlmIChyZW1haW5pbmdQYXRobmFtZSkge1xuICAgIC8vIFJlcXVpcmUgdGhhdCB0aGUgbWF0Y2ggZW5kcyBhdCBhIHBhdGggc2VwYXJhdG9yLCBpZiB3ZSBkaWRuJ3QgbWF0Y2hcbiAgICAvLyB0aGUgZnVsbCBwYXRoLCBzbyBhbnkgcmVtYWluaW5nIHBhdGhuYW1lIGlzIGEgbmV3IHBhdGggc2VnbWVudC5cbiAgICBpZiAobWF0Y2hlZFBhdGguY2hhckF0KG1hdGNoZWRQYXRoLmxlbmd0aCAtIDEpICE9PSAnLycpIHtcbiAgICAgIHJldHVybiBudWxsO1xuICAgIH1cblxuICAgIC8vIElmIHRoZXJlIGlzIGEgcmVtYWluaW5nIHBhdGhuYW1lLCB0cmVhdCB0aGUgcGF0aCBzZXBhcmF0b3IgYXMgcGFydCBvZlxuICAgIC8vIHRoZSByZW1haW5pbmcgcGF0aG5hbWUgZm9yIHByb3Blcmx5IGNvbnRpbnVpbmcgdGhlIG1hdGNoLlxuICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gJy8nICsgcmVtYWluaW5nUGF0aG5hbWU7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIHJlbWFpbmluZ1BhdGhuYW1lOiByZW1haW5pbmdQYXRobmFtZSxcbiAgICBwYXJhbU5hbWVzOiBwYXJhbU5hbWVzLFxuICAgIHBhcmFtVmFsdWVzOiBtYXRjaC5zbGljZSgxKS5tYXAoZnVuY3Rpb24gKHYpIHtcbiAgICAgIHJldHVybiB2ICYmIGRlY29kZVVSSUNvbXBvbmVudCh2KTtcbiAgICB9KVxuICB9O1xufVxuXG5mdW5jdGlvbiBnZXRQYXJhbU5hbWVzKHBhdHRlcm4pIHtcbiAgcmV0dXJuIGNvbXBpbGVQYXR0ZXJuKHBhdHRlcm4pLnBhcmFtTmFtZXM7XG59XG5cbmZ1bmN0aW9uIGdldFBhcmFtcyhwYXR0ZXJuLCBwYXRobmFtZSkge1xuICB2YXIgbWF0Y2ggPSBtYXRjaFBhdHRlcm4ocGF0dGVybiwgcGF0aG5hbWUpO1xuICBpZiAoIW1hdGNoKSB7XG4gICAgcmV0dXJuIG51bGw7XG4gIH1cblxuICB2YXIgcGFyYW1OYW1lcyA9IG1hdGNoLnBhcmFtTmFtZXM7XG4gIHZhciBwYXJhbVZhbHVlcyA9IG1hdGNoLnBhcmFtVmFsdWVzO1xuXG4gIHZhciBwYXJhbXMgPSB7fTtcblxuICBwYXJhbU5hbWVzLmZvckVhY2goZnVuY3Rpb24gKHBhcmFtTmFtZSwgaW5kZXgpIHtcbiAgICBwYXJhbXNbcGFyYW1OYW1lXSA9IHBhcmFtVmFsdWVzW2luZGV4XTtcbiAgfSk7XG5cbiAgcmV0dXJuIHBhcmFtcztcbn1cblxuLyoqXG4gKiBSZXR1cm5zIGEgdmVyc2lvbiBvZiB0aGUgZ2l2ZW4gcGF0dGVybiB3aXRoIHBhcmFtcyBpbnRlcnBvbGF0ZWQuIFRocm93c1xuICogaWYgdGhlcmUgaXMgYSBkeW5hbWljIHNlZ21lbnQgb2YgdGhlIHBhdHRlcm4gZm9yIHdoaWNoIHRoZXJlIGlzIG5vIHBhcmFtLlxuICovXG5mdW5jdGlvbiBmb3JtYXRQYXR0ZXJuKHBhdHRlcm4sIHBhcmFtcykge1xuICBwYXJhbXMgPSBwYXJhbXMgfHwge307XG5cbiAgdmFyIF9jb21waWxlUGF0dGVybjMgPSBjb21waWxlUGF0dGVybihwYXR0ZXJuKTtcblxuICB2YXIgdG9rZW5zID0gX2NvbXBpbGVQYXR0ZXJuMy50b2tlbnM7XG5cbiAgdmFyIHBhcmVuQ291bnQgPSAwLFxuICAgICAgcGF0aG5hbWUgPSAnJyxcbiAgICAgIHNwbGF0SW5kZXggPSAwO1xuXG4gIHZhciB0b2tlbiA9IHZvaWQgMCxcbiAgICAgIHBhcmFtTmFtZSA9IHZvaWQgMCxcbiAgICAgIHBhcmFtVmFsdWUgPSB2b2lkIDA7XG4gIGZvciAodmFyIGkgPSAwLCBsZW4gPSB0b2tlbnMubGVuZ3RoOyBpIDwgbGVuOyArK2kpIHtcbiAgICB0b2tlbiA9IHRva2Vuc1tpXTtcblxuICAgIGlmICh0b2tlbiA9PT0gJyonIHx8IHRva2VuID09PSAnKionKSB7XG4gICAgICBwYXJhbVZhbHVlID0gQXJyYXkuaXNBcnJheShwYXJhbXMuc3BsYXQpID8gcGFyYW1zLnNwbGF0W3NwbGF0SW5kZXgrK10gOiBwYXJhbXMuc3BsYXQ7XG5cbiAgICAgICEocGFyYW1WYWx1ZSAhPSBudWxsIHx8IHBhcmVuQ291bnQgPiAwKSA/IFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdNaXNzaW5nIHNwbGF0ICMlcyBmb3IgcGF0aCBcIiVzXCInLCBzcGxhdEluZGV4LCBwYXR0ZXJuKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICAgIGlmIChwYXJhbVZhbHVlICE9IG51bGwpIHBhdGhuYW1lICs9IGVuY29kZVVSSShwYXJhbVZhbHVlKTtcbiAgICB9IGVsc2UgaWYgKHRva2VuID09PSAnKCcpIHtcbiAgICAgIHBhcmVuQ291bnQgKz0gMTtcbiAgICB9IGVsc2UgaWYgKHRva2VuID09PSAnKScpIHtcbiAgICAgIHBhcmVuQ291bnQgLT0gMTtcbiAgICB9IGVsc2UgaWYgKHRva2VuLmNoYXJBdCgwKSA9PT0gJzonKSB7XG4gICAgICBwYXJhbU5hbWUgPSB0b2tlbi5zdWJzdHJpbmcoMSk7XG4gICAgICBwYXJhbVZhbHVlID0gcGFyYW1zW3BhcmFtTmFtZV07XG5cbiAgICAgICEocGFyYW1WYWx1ZSAhPSBudWxsIHx8IHBhcmVuQ291bnQgPiAwKSA/IFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UsICdNaXNzaW5nIFwiJXNcIiBwYXJhbWV0ZXIgZm9yIHBhdGggXCIlc1wiJywgcGFyYW1OYW1lLCBwYXR0ZXJuKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICAgIGlmIChwYXJhbVZhbHVlICE9IG51bGwpIHBhdGhuYW1lICs9IGVuY29kZVVSSUNvbXBvbmVudChwYXJhbVZhbHVlKTtcbiAgICB9IGVsc2Uge1xuICAgICAgcGF0aG5hbWUgKz0gdG9rZW47XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIHBhdGhuYW1lLnJlcGxhY2UoL1xcLysvZywgJy8nKTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLnJvdXRlciA9IGV4cG9ydHMucm91dGVzID0gZXhwb3J0cy5yb3V0ZSA9IGV4cG9ydHMuY29tcG9uZW50cyA9IGV4cG9ydHMuY29tcG9uZW50ID0gZXhwb3J0cy5sb2NhdGlvbiA9IGV4cG9ydHMuaGlzdG9yeSA9IGV4cG9ydHMuZmFsc3kgPSBleHBvcnRzLmxvY2F0aW9uU2hhcGUgPSBleHBvcnRzLnJvdXRlclNoYXBlID0gdW5kZWZpbmVkO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzID0gcmVxdWlyZSgnLi9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzJyk7XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzKTtcblxudmFyIF9JbnRlcm5hbFByb3BUeXBlcyA9IHJlcXVpcmUoJy4vSW50ZXJuYWxQcm9wVHlwZXMnKTtcblxudmFyIEludGVybmFsUHJvcFR5cGVzID0gX2ludGVyb3BSZXF1aXJlV2lsZGNhcmQoX0ludGVybmFsUHJvcFR5cGVzKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlV2lsZGNhcmQob2JqKSB7IGlmIChvYmogJiYgb2JqLl9fZXNNb2R1bGUpIHsgcmV0dXJuIG9iajsgfSBlbHNlIHsgdmFyIG5ld09iaiA9IHt9OyBpZiAob2JqICE9IG51bGwpIHsgZm9yICh2YXIga2V5IGluIG9iaikgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwga2V5KSkgbmV3T2JqW2tleV0gPSBvYmpba2V5XTsgfSB9IG5ld09iai5kZWZhdWx0ID0gb2JqOyByZXR1cm4gbmV3T2JqOyB9IH1cblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIGZ1bmMgPSBfcmVhY3QuUHJvcFR5cGVzLmZ1bmM7XG52YXIgb2JqZWN0ID0gX3JlYWN0LlByb3BUeXBlcy5vYmplY3Q7XG52YXIgc2hhcGUgPSBfcmVhY3QuUHJvcFR5cGVzLnNoYXBlO1xudmFyIHN0cmluZyA9IF9yZWFjdC5Qcm9wVHlwZXMuc3RyaW5nO1xudmFyIHJvdXRlclNoYXBlID0gZXhwb3J0cy5yb3V0ZXJTaGFwZSA9IHNoYXBlKHtcbiAgcHVzaDogZnVuYy5pc1JlcXVpcmVkLFxuICByZXBsYWNlOiBmdW5jLmlzUmVxdWlyZWQsXG4gIGdvOiBmdW5jLmlzUmVxdWlyZWQsXG4gIGdvQmFjazogZnVuYy5pc1JlcXVpcmVkLFxuICBnb0ZvcndhcmQ6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgc2V0Um91dGVMZWF2ZUhvb2s6IGZ1bmMuaXNSZXF1aXJlZCxcbiAgaXNBY3RpdmU6IGZ1bmMuaXNSZXF1aXJlZFxufSk7XG5cbnZhciBsb2NhdGlvblNoYXBlID0gZXhwb3J0cy5sb2NhdGlvblNoYXBlID0gc2hhcGUoe1xuICBwYXRobmFtZTogc3RyaW5nLmlzUmVxdWlyZWQsXG4gIHNlYXJjaDogc3RyaW5nLmlzUmVxdWlyZWQsXG4gIHN0YXRlOiBvYmplY3QsXG4gIGFjdGlvbjogc3RyaW5nLmlzUmVxdWlyZWQsXG4gIGtleTogc3RyaW5nXG59KTtcblxuLy8gRGVwcmVjYXRlZCBzdHVmZiBiZWxvdzpcblxudmFyIGZhbHN5ID0gZXhwb3J0cy5mYWxzeSA9IEludGVybmFsUHJvcFR5cGVzLmZhbHN5O1xudmFyIGhpc3RvcnkgPSBleHBvcnRzLmhpc3RvcnkgPSBJbnRlcm5hbFByb3BUeXBlcy5oaXN0b3J5O1xudmFyIGxvY2F0aW9uID0gZXhwb3J0cy5sb2NhdGlvbiA9IGxvY2F0aW9uU2hhcGU7XG52YXIgY29tcG9uZW50ID0gZXhwb3J0cy5jb21wb25lbnQgPSBJbnRlcm5hbFByb3BUeXBlcy5jb21wb25lbnQ7XG52YXIgY29tcG9uZW50cyA9IGV4cG9ydHMuY29tcG9uZW50cyA9IEludGVybmFsUHJvcFR5cGVzLmNvbXBvbmVudHM7XG52YXIgcm91dGUgPSBleHBvcnRzLnJvdXRlID0gSW50ZXJuYWxQcm9wVHlwZXMucm91dGU7XG52YXIgcm91dGVzID0gZXhwb3J0cy5yb3V0ZXMgPSBJbnRlcm5hbFByb3BUeXBlcy5yb3V0ZXM7XG52YXIgcm91dGVyID0gZXhwb3J0cy5yb3V0ZXIgPSByb3V0ZXJTaGFwZTtcblxuaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicpIHtcbiAgKGZ1bmN0aW9uICgpIHtcbiAgICB2YXIgZGVwcmVjYXRlUHJvcFR5cGUgPSBmdW5jdGlvbiBkZXByZWNhdGVQcm9wVHlwZShwcm9wVHlwZSwgbWVzc2FnZSkge1xuICAgICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsIG1lc3NhZ2UpIDogdm9pZCAwO1xuICAgICAgICByZXR1cm4gcHJvcFR5cGUuYXBwbHkodW5kZWZpbmVkLCBhcmd1bWVudHMpO1xuICAgICAgfTtcbiAgICB9O1xuXG4gICAgdmFyIGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUgPSBmdW5jdGlvbiBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlKHByb3BUeXBlKSB7XG4gICAgICByZXR1cm4gZGVwcmVjYXRlUHJvcFR5cGUocHJvcFR5cGUsICdUaGlzIHByb3AgdHlwZSBpcyBub3QgaW50ZW5kZWQgZm9yIGV4dGVybmFsIHVzZSwgYW5kIHdhcyBwcmV2aW91c2x5IGV4cG9ydGVkIGJ5IG1pc3Rha2UuIFRoZXNlIGludGVybmFsIHByb3AgdHlwZXMgYXJlIGRlcHJlY2F0ZWQgZm9yIGV4dGVybmFsIHVzZSwgYW5kIHdpbGwgYmUgcmVtb3ZlZCBpbiBhIGxhdGVyIHZlcnNpb24uJyk7XG4gICAgfTtcblxuICAgIHZhciBkZXByZWNhdGVSZW5hbWVkUHJvcFR5cGUgPSBmdW5jdGlvbiBkZXByZWNhdGVSZW5hbWVkUHJvcFR5cGUocHJvcFR5cGUsIG5hbWUpIHtcbiAgICAgIHJldHVybiBkZXByZWNhdGVQcm9wVHlwZShwcm9wVHlwZSwgJ1RoZSBgJyArIG5hbWUgKyAnYCBwcm9wIHR5cGUgaXMgbm93IGV4cG9ydGVkIGFzIGAnICsgbmFtZSArICdTaGFwZWAgdG8gYXZvaWQgbmFtZSBjb25mbGljdHMuIFRoaXMgZXhwb3J0IGlzIGRlcHJlY2F0ZWQgYW5kIHdpbGwgYmUgcmVtb3ZlZCBpbiBhIGxhdGVyIHZlcnNpb24uJyk7XG4gICAgfTtcblxuICAgIGV4cG9ydHMuZmFsc3kgPSBmYWxzeSA9IGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUoZmFsc3kpO1xuICAgIGV4cG9ydHMuaGlzdG9yeSA9IGhpc3RvcnkgPSBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlKGhpc3RvcnkpO1xuICAgIGV4cG9ydHMuY29tcG9uZW50ID0gY29tcG9uZW50ID0gZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZShjb21wb25lbnQpO1xuICAgIGV4cG9ydHMuY29tcG9uZW50cyA9IGNvbXBvbmVudHMgPSBkZXByZWNhdGVJbnRlcm5hbFByb3BUeXBlKGNvbXBvbmVudHMpO1xuICAgIGV4cG9ydHMucm91dGUgPSByb3V0ZSA9IGRlcHJlY2F0ZUludGVybmFsUHJvcFR5cGUocm91dGUpO1xuICAgIGV4cG9ydHMucm91dGVzID0gcm91dGVzID0gZGVwcmVjYXRlSW50ZXJuYWxQcm9wVHlwZShyb3V0ZXMpO1xuXG4gICAgZXhwb3J0cy5sb2NhdGlvbiA9IGxvY2F0aW9uID0gZGVwcmVjYXRlUmVuYW1lZFByb3BUeXBlKGxvY2F0aW9uLCAnbG9jYXRpb24nKTtcbiAgICBleHBvcnRzLnJvdXRlciA9IHJvdXRlciA9IGRlcHJlY2F0ZVJlbmFtZWRQcm9wVHlwZShyb3V0ZXIsICdyb3V0ZXInKTtcbiAgfSkoKTtcbn1cblxudmFyIGRlZmF1bHRFeHBvcnQgPSB7XG4gIGZhbHN5OiBmYWxzeSxcbiAgaGlzdG9yeTogaGlzdG9yeSxcbiAgbG9jYXRpb246IGxvY2F0aW9uLFxuICBjb21wb25lbnQ6IGNvbXBvbmVudCxcbiAgY29tcG9uZW50czogY29tcG9uZW50cyxcbiAgcm91dGU6IHJvdXRlLFxuICAvLyBGb3Igc29tZSByZWFzb24sIHJvdXRlcyB3YXMgbmV2ZXIgaGVyZS5cbiAgcm91dGVyOiByb3V0ZXJcbn07XG5cbmlmIChcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gIGRlZmF1bHRFeHBvcnQgPSAoMCwgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMyLmRlZmF1bHQpKGRlZmF1bHRFeHBvcnQsICdUaGUgZGVmYXVsdCBleHBvcnQgZnJvbSBgcmVhY3Qtcm91dGVyL2xpYi9Qcm9wVHlwZXNgIGlzIGRlcHJlY2F0ZWQuIFBsZWFzZSB1c2UgdGhlIG5hbWVkIGV4cG9ydHMgaW5zdGVhZC4nKTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gZGVmYXVsdEV4cG9ydDsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbnZhciBfUGF0dGVyblV0aWxzID0gcmVxdWlyZSgnLi9QYXR0ZXJuVXRpbHMnKTtcblxudmFyIF9JbnRlcm5hbFByb3BUeXBlcyA9IHJlcXVpcmUoJy4vSW50ZXJuYWxQcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIF9SZWFjdCRQcm9wVHlwZXMgPSBfcmVhY3QyLmRlZmF1bHQuUHJvcFR5cGVzO1xudmFyIHN0cmluZyA9IF9SZWFjdCRQcm9wVHlwZXMuc3RyaW5nO1xudmFyIG9iamVjdCA9IF9SZWFjdCRQcm9wVHlwZXMub2JqZWN0O1xuXG4vKipcbiAqIEEgPFJlZGlyZWN0PiBpcyB1c2VkIHRvIGRlY2xhcmUgYW5vdGhlciBVUkwgcGF0aCBhIGNsaWVudCBzaG91bGRcbiAqIGJlIHNlbnQgdG8gd2hlbiB0aGV5IHJlcXVlc3QgYSBnaXZlbiBVUkwuXG4gKlxuICogUmVkaXJlY3RzIGFyZSBwbGFjZWQgYWxvbmdzaWRlIHJvdXRlcyBpbiB0aGUgcm91dGUgY29uZmlndXJhdGlvblxuICogYW5kIGFyZSB0cmF2ZXJzZWQgaW4gdGhlIHNhbWUgbWFubmVyLlxuICovXG5cbnZhciBSZWRpcmVjdCA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gIGRpc3BsYXlOYW1lOiAnUmVkaXJlY3QnLFxuXG5cbiAgc3RhdGljczoge1xuICAgIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudDogZnVuY3Rpb24gY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQpIHtcbiAgICAgIHZhciByb3V0ZSA9ICgwLCBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQpKGVsZW1lbnQpO1xuXG4gICAgICBpZiAocm91dGUuZnJvbSkgcm91dGUucGF0aCA9IHJvdXRlLmZyb207XG5cbiAgICAgIHJvdXRlLm9uRW50ZXIgPSBmdW5jdGlvbiAobmV4dFN0YXRlLCByZXBsYWNlKSB7XG4gICAgICAgIHZhciBsb2NhdGlvbiA9IG5leHRTdGF0ZS5sb2NhdGlvbjtcbiAgICAgICAgdmFyIHBhcmFtcyA9IG5leHRTdGF0ZS5wYXJhbXM7XG5cblxuICAgICAgICB2YXIgcGF0aG5hbWUgPSB2b2lkIDA7XG4gICAgICAgIGlmIChyb3V0ZS50by5jaGFyQXQoMCkgPT09ICcvJykge1xuICAgICAgICAgIHBhdGhuYW1lID0gKDAsIF9QYXR0ZXJuVXRpbHMuZm9ybWF0UGF0dGVybikocm91dGUudG8sIHBhcmFtcyk7XG4gICAgICAgIH0gZWxzZSBpZiAoIXJvdXRlLnRvKSB7XG4gICAgICAgICAgcGF0aG5hbWUgPSBsb2NhdGlvbi5wYXRobmFtZTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB2YXIgcm91dGVJbmRleCA9IG5leHRTdGF0ZS5yb3V0ZXMuaW5kZXhPZihyb3V0ZSk7XG4gICAgICAgICAgdmFyIHBhcmVudFBhdHRlcm4gPSBSZWRpcmVjdC5nZXRSb3V0ZVBhdHRlcm4obmV4dFN0YXRlLnJvdXRlcywgcm91dGVJbmRleCAtIDEpO1xuICAgICAgICAgIHZhciBwYXR0ZXJuID0gcGFyZW50UGF0dGVybi5yZXBsYWNlKC9cXC8qJC8sICcvJykgKyByb3V0ZS50bztcbiAgICAgICAgICBwYXRobmFtZSA9ICgwLCBfUGF0dGVyblV0aWxzLmZvcm1hdFBhdHRlcm4pKHBhdHRlcm4sIHBhcmFtcyk7XG4gICAgICAgIH1cblxuICAgICAgICByZXBsYWNlKHtcbiAgICAgICAgICBwYXRobmFtZTogcGF0aG5hbWUsXG4gICAgICAgICAgcXVlcnk6IHJvdXRlLnF1ZXJ5IHx8IGxvY2F0aW9uLnF1ZXJ5LFxuICAgICAgICAgIHN0YXRlOiByb3V0ZS5zdGF0ZSB8fCBsb2NhdGlvbi5zdGF0ZVxuICAgICAgICB9KTtcbiAgICAgIH07XG5cbiAgICAgIHJldHVybiByb3V0ZTtcbiAgICB9LFxuICAgIGdldFJvdXRlUGF0dGVybjogZnVuY3Rpb24gZ2V0Um91dGVQYXR0ZXJuKHJvdXRlcywgcm91dGVJbmRleCkge1xuICAgICAgdmFyIHBhcmVudFBhdHRlcm4gPSAnJztcblxuICAgICAgZm9yICh2YXIgaSA9IHJvdXRlSW5kZXg7IGkgPj0gMDsgaS0tKSB7XG4gICAgICAgIHZhciByb3V0ZSA9IHJvdXRlc1tpXTtcbiAgICAgICAgdmFyIHBhdHRlcm4gPSByb3V0ZS5wYXRoIHx8ICcnO1xuXG4gICAgICAgIHBhcmVudFBhdHRlcm4gPSBwYXR0ZXJuLnJlcGxhY2UoL1xcLyokLywgJy8nKSArIHBhcmVudFBhdHRlcm47XG5cbiAgICAgICAgaWYgKHBhdHRlcm4uaW5kZXhPZignLycpID09PSAwKSBicmVhaztcbiAgICAgIH1cblxuICAgICAgcmV0dXJuICcvJyArIHBhcmVudFBhdHRlcm47XG4gICAgfVxuICB9LFxuXG4gIHByb3BUeXBlczoge1xuICAgIHBhdGg6IHN0cmluZyxcbiAgICBmcm9tOiBzdHJpbmcsIC8vIEFsaWFzIGZvciBwYXRoXG4gICAgdG86IHN0cmluZy5pc1JlcXVpcmVkLFxuICAgIHF1ZXJ5OiBvYmplY3QsXG4gICAgc3RhdGU6IG9iamVjdCxcbiAgICBvbkVudGVyOiBfSW50ZXJuYWxQcm9wVHlwZXMuZmFsc3ksXG4gICAgY2hpbGRyZW46IF9JbnRlcm5hbFByb3BUeXBlcy5mYWxzeVxuICB9LFxuXG4gIC8qIGlzdGFuYnVsIGlnbm9yZSBuZXh0OiBzYW5pdHkgY2hlY2sgKi9cbiAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIoKSB7XG4gICAgIWZhbHNlID8gXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSwgJzxSZWRpcmVjdD4gZWxlbWVudHMgYXJlIGZvciByb3V0ZXIgY29uZmlndXJhdGlvbiBvbmx5IGFuZCBzaG91bGQgbm90IGJlIHJlbmRlcmVkJykgOiAoMCwgX2ludmFyaWFudDIuZGVmYXVsdCkoZmFsc2UpIDogdm9pZCAwO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gUmVkaXJlY3Q7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9pbnZhcmlhbnQgPSByZXF1aXJlKCdpbnZhcmlhbnQnKTtcblxudmFyIF9pbnZhcmlhbnQyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaW52YXJpYW50KTtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbnZhciBfSW50ZXJuYWxQcm9wVHlwZXMgPSByZXF1aXJlKCcuL0ludGVybmFsUHJvcFR5cGVzJyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBfUmVhY3QkUHJvcFR5cGVzID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcztcbnZhciBzdHJpbmcgPSBfUmVhY3QkUHJvcFR5cGVzLnN0cmluZztcbnZhciBmdW5jID0gX1JlYWN0JFByb3BUeXBlcy5mdW5jO1xuXG4vKipcbiAqIEEgPFJvdXRlPiBpcyB1c2VkIHRvIGRlY2xhcmUgd2hpY2ggY29tcG9uZW50cyBhcmUgcmVuZGVyZWQgdG8gdGhlXG4gKiBwYWdlIHdoZW4gdGhlIFVSTCBtYXRjaGVzIGEgZ2l2ZW4gcGF0dGVybi5cbiAqXG4gKiBSb3V0ZXMgYXJlIGFycmFuZ2VkIGluIGEgbmVzdGVkIHRyZWUgc3RydWN0dXJlLiBXaGVuIGEgbmV3IFVSTCBpc1xuICogcmVxdWVzdGVkLCB0aGUgdHJlZSBpcyBzZWFyY2hlZCBkZXB0aC1maXJzdCB0byBmaW5kIGEgcm91dGUgd2hvc2VcbiAqIHBhdGggbWF0Y2hlcyB0aGUgVVJMLiAgV2hlbiBvbmUgaXMgZm91bmQsIGFsbCByb3V0ZXMgaW4gdGhlIHRyZWVcbiAqIHRoYXQgbGVhZCB0byBpdCBhcmUgY29uc2lkZXJlZCBcImFjdGl2ZVwiIGFuZCB0aGVpciBjb21wb25lbnRzIGFyZVxuICogcmVuZGVyZWQgaW50byB0aGUgRE9NLCBuZXN0ZWQgaW4gdGhlIHNhbWUgb3JkZXIgYXMgaW4gdGhlIHRyZWUuXG4gKi9cblxudmFyIFJvdXRlID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdSb3V0ZScsXG5cblxuICBzdGF0aWNzOiB7XG4gICAgY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50OiBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnRcbiAgfSxcblxuICBwcm9wVHlwZXM6IHtcbiAgICBwYXRoOiBzdHJpbmcsXG4gICAgY29tcG9uZW50OiBfSW50ZXJuYWxQcm9wVHlwZXMuY29tcG9uZW50LFxuICAgIGNvbXBvbmVudHM6IF9JbnRlcm5hbFByb3BUeXBlcy5jb21wb25lbnRzLFxuICAgIGdldENvbXBvbmVudDogZnVuYyxcbiAgICBnZXRDb21wb25lbnRzOiBmdW5jXG4gIH0sXG5cbiAgLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHNhbml0eSBjaGVjayAqL1xuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICAhZmFsc2UgPyBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnPFJvdXRlPiBlbGVtZW50cyBhcmUgZm9yIHJvdXRlciBjb25maWd1cmF0aW9uIG9ubHkgYW5kIHNob3VsZCBub3QgYmUgcmVuZGVyZWQnKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBSb3V0ZTtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgb2JqZWN0ID0gX3JlYWN0Mi5kZWZhdWx0LlByb3BUeXBlcy5vYmplY3Q7XG5cbi8qKlxuICogVGhlIFJvdXRlQ29udGV4dCBtaXhpbiBwcm92aWRlcyBhIGNvbnZlbmllbnQgd2F5IGZvciByb3V0ZVxuICogY29tcG9uZW50cyB0byBzZXQgdGhlIHJvdXRlIGluIGNvbnRleHQuIFRoaXMgaXMgbmVlZGVkIGZvclxuICogcm91dGVzIHRoYXQgcmVuZGVyIGVsZW1lbnRzIHRoYXQgd2FudCB0byB1c2UgdGhlIExpZmVjeWNsZVxuICogbWl4aW4gdG8gcHJldmVudCB0cmFuc2l0aW9ucy5cbiAqL1xuXG52YXIgUm91dGVDb250ZXh0ID0ge1xuXG4gIHByb3BUeXBlczoge1xuICAgIHJvdXRlOiBvYmplY3QuaXNSZXF1aXJlZFxuICB9LFxuXG4gIGNoaWxkQ29udGV4dFR5cGVzOiB7XG4gICAgcm91dGU6IG9iamVjdC5pc1JlcXVpcmVkXG4gIH0sXG5cbiAgZ2V0Q2hpbGRDb250ZXh0OiBmdW5jdGlvbiBnZXRDaGlsZENvbnRleHQoKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIHJvdXRlOiB0aGlzLnByb3BzLnJvdXRlXG4gICAgfTtcbiAgfSxcbiAgY29tcG9uZW50V2lsbE1vdW50OiBmdW5jdGlvbiBjb21wb25lbnRXaWxsTW91bnQoKSB7XG4gICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdUaGUgYFJvdXRlQ29udGV4dGAgbWl4aW4gaXMgZGVwcmVjYXRlZC4gWW91IGNhbiBwcm92aWRlIGB0aGlzLnByb3BzLnJvdXRlYCBvbiBjb250ZXh0IHdpdGggeW91ciBvd24gYGNvbnRleHRUeXBlc2AuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1yb3V0ZWNvbnRleHRtaXhpbicpIDogdm9pZCAwO1xuICB9XG59O1xuXG5leHBvcnRzLmRlZmF1bHQgPSBSb3V0ZUNvbnRleHQ7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHMuaXNSZWFjdENoaWxkcmVuID0gaXNSZWFjdENoaWxkcmVuO1xuZXhwb3J0cy5jcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQgPSBjcmVhdGVSb3V0ZUZyb21SZWFjdEVsZW1lbnQ7XG5leHBvcnRzLmNyZWF0ZVJvdXRlc0Zyb21SZWFjdENoaWxkcmVuID0gY3JlYXRlUm91dGVzRnJvbVJlYWN0Q2hpbGRyZW47XG5leHBvcnRzLmNyZWF0ZVJvdXRlcyA9IGNyZWF0ZVJvdXRlcztcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBpc1ZhbGlkQ2hpbGQob2JqZWN0KSB7XG4gIHJldHVybiBvYmplY3QgPT0gbnVsbCB8fCBfcmVhY3QyLmRlZmF1bHQuaXNWYWxpZEVsZW1lbnQob2JqZWN0KTtcbn1cblxuZnVuY3Rpb24gaXNSZWFjdENoaWxkcmVuKG9iamVjdCkge1xuICByZXR1cm4gaXNWYWxpZENoaWxkKG9iamVjdCkgfHwgQXJyYXkuaXNBcnJheShvYmplY3QpICYmIG9iamVjdC5ldmVyeShpc1ZhbGlkQ2hpbGQpO1xufVxuXG5mdW5jdGlvbiBjcmVhdGVSb3V0ZShkZWZhdWx0UHJvcHMsIHByb3BzKSB7XG4gIHJldHVybiBfZXh0ZW5kcyh7fSwgZGVmYXVsdFByb3BzLCBwcm9wcyk7XG59XG5cbmZ1bmN0aW9uIGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50KSB7XG4gIHZhciB0eXBlID0gZWxlbWVudC50eXBlO1xuICB2YXIgcm91dGUgPSBjcmVhdGVSb3V0ZSh0eXBlLmRlZmF1bHRQcm9wcywgZWxlbWVudC5wcm9wcyk7XG5cbiAgaWYgKHJvdXRlLmNoaWxkcmVuKSB7XG4gICAgdmFyIGNoaWxkUm91dGVzID0gY3JlYXRlUm91dGVzRnJvbVJlYWN0Q2hpbGRyZW4ocm91dGUuY2hpbGRyZW4sIHJvdXRlKTtcblxuICAgIGlmIChjaGlsZFJvdXRlcy5sZW5ndGgpIHJvdXRlLmNoaWxkUm91dGVzID0gY2hpbGRSb3V0ZXM7XG5cbiAgICBkZWxldGUgcm91dGUuY2hpbGRyZW47XG4gIH1cblxuICByZXR1cm4gcm91dGU7XG59XG5cbi8qKlxuICogQ3JlYXRlcyBhbmQgcmV0dXJucyBhIHJvdXRlcyBvYmplY3QgZnJvbSB0aGUgZ2l2ZW4gUmVhY3RDaGlsZHJlbi4gSlNYXG4gKiBwcm92aWRlcyBhIGNvbnZlbmllbnQgd2F5IHRvIHZpc3VhbGl6ZSBob3cgcm91dGVzIGluIHRoZSBoaWVyYXJjaHkgYXJlXG4gKiBuZXN0ZWQuXG4gKlxuICogICBpbXBvcnQgeyBSb3V0ZSwgY3JlYXRlUm91dGVzRnJvbVJlYWN0Q2hpbGRyZW4gfSBmcm9tICdyZWFjdC1yb3V0ZXInXG4gKlxuICogICBjb25zdCByb3V0ZXMgPSBjcmVhdGVSb3V0ZXNGcm9tUmVhY3RDaGlsZHJlbihcbiAqICAgICA8Um91dGUgY29tcG9uZW50PXtBcHB9PlxuICogICAgICAgPFJvdXRlIHBhdGg9XCJob21lXCIgY29tcG9uZW50PXtEYXNoYm9hcmR9Lz5cbiAqICAgICAgIDxSb3V0ZSBwYXRoPVwibmV3c1wiIGNvbXBvbmVudD17TmV3c0ZlZWR9Lz5cbiAqICAgICA8L1JvdXRlPlxuICogICApXG4gKlxuICogTm90ZTogVGhpcyBtZXRob2QgaXMgYXV0b21hdGljYWxseSB1c2VkIHdoZW4geW91IHByb3ZpZGUgPFJvdXRlPiBjaGlsZHJlblxuICogdG8gYSA8Um91dGVyPiBjb21wb25lbnQuXG4gKi9cbmZ1bmN0aW9uIGNyZWF0ZVJvdXRlc0Zyb21SZWFjdENoaWxkcmVuKGNoaWxkcmVuLCBwYXJlbnRSb3V0ZSkge1xuICB2YXIgcm91dGVzID0gW107XG5cbiAgX3JlYWN0Mi5kZWZhdWx0LkNoaWxkcmVuLmZvckVhY2goY2hpbGRyZW4sIGZ1bmN0aW9uIChlbGVtZW50KSB7XG4gICAgaWYgKF9yZWFjdDIuZGVmYXVsdC5pc1ZhbGlkRWxlbWVudChlbGVtZW50KSkge1xuICAgICAgLy8gQ29tcG9uZW50IGNsYXNzZXMgbWF5IGhhdmUgYSBzdGF0aWMgY3JlYXRlKiBtZXRob2QuXG4gICAgICBpZiAoZWxlbWVudC50eXBlLmNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudCkge1xuICAgICAgICB2YXIgcm91dGUgPSBlbGVtZW50LnR5cGUuY3JlYXRlUm91dGVGcm9tUmVhY3RFbGVtZW50KGVsZW1lbnQsIHBhcmVudFJvdXRlKTtcblxuICAgICAgICBpZiAocm91dGUpIHJvdXRlcy5wdXNoKHJvdXRlKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJvdXRlcy5wdXNoKGNyZWF0ZVJvdXRlRnJvbVJlYWN0RWxlbWVudChlbGVtZW50KSk7XG4gICAgICB9XG4gICAgfVxuICB9KTtcblxuICByZXR1cm4gcm91dGVzO1xufVxuXG4vKipcbiAqIENyZWF0ZXMgYW5kIHJldHVybnMgYW4gYXJyYXkgb2Ygcm91dGVzIGZyb20gdGhlIGdpdmVuIG9iamVjdCB3aGljaFxuICogbWF5IGJlIGEgSlNYIHJvdXRlLCBhIHBsYWluIG9iamVjdCByb3V0ZSwgb3IgYW4gYXJyYXkgb2YgZWl0aGVyLlxuICovXG5mdW5jdGlvbiBjcmVhdGVSb3V0ZXMocm91dGVzKSB7XG4gIGlmIChpc1JlYWN0Q2hpbGRyZW4ocm91dGVzKSkge1xuICAgIHJvdXRlcyA9IGNyZWF0ZVJvdXRlc0Zyb21SZWFjdENoaWxkcmVuKHJvdXRlcyk7XG4gIH0gZWxzZSBpZiAocm91dGVzICYmICFBcnJheS5pc0FycmF5KHJvdXRlcykpIHtcbiAgICByb3V0ZXMgPSBbcm91dGVzXTtcbiAgfVxuXG4gIHJldHVybiByb3V0ZXM7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX2NyZWF0ZUhhc2hIaXN0b3J5ID0gcmVxdWlyZSgnaGlzdG9yeS9saWIvY3JlYXRlSGFzaEhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVIYXNoSGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVIYXNoSGlzdG9yeSk7XG5cbnZhciBfdXNlUXVlcmllcyA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL3VzZVF1ZXJpZXMnKTtcblxudmFyIF91c2VRdWVyaWVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZVF1ZXJpZXMpO1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIgPSByZXF1aXJlKCcuL2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyJyk7XG5cbnZhciBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIpO1xuXG52YXIgX0ludGVybmFsUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9JbnRlcm5hbFByb3BUeXBlcycpO1xuXG52YXIgX1JvdXRlckNvbnRleHQgPSByZXF1aXJlKCcuL1JvdXRlckNvbnRleHQnKTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlckNvbnRleHQpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxudmFyIF9Sb3V0ZXJVdGlscyA9IHJlcXVpcmUoJy4vUm91dGVyVXRpbHMnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKG9iaiwga2V5cykgeyB2YXIgdGFyZ2V0ID0ge307IGZvciAodmFyIGkgaW4gb2JqKSB7IGlmIChrZXlzLmluZGV4T2YoaSkgPj0gMCkgY29udGludWU7IGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwgaSkpIGNvbnRpbnVlOyB0YXJnZXRbaV0gPSBvYmpbaV07IH0gcmV0dXJuIHRhcmdldDsgfVxuXG5mdW5jdGlvbiBpc0RlcHJlY2F0ZWRIaXN0b3J5KGhpc3RvcnkpIHtcbiAgcmV0dXJuICFoaXN0b3J5IHx8ICFoaXN0b3J5Ll9fdjJfY29tcGF0aWJsZV9fO1xufVxuXG4vKiBpc3RhbmJ1bCBpZ25vcmUgbmV4dDogc2FuaXR5IGNoZWNrICovXG5mdW5jdGlvbiBpc1Vuc3VwcG9ydGVkSGlzdG9yeShoaXN0b3J5KSB7XG4gIC8vIHYzIGhpc3RvcmllcyBleHBvc2UgZ2V0Q3VycmVudExvY2F0aW9uLCBidXQgYXJlbid0IGN1cnJlbnRseSBzdXBwb3J0ZWQuXG4gIHJldHVybiBoaXN0b3J5ICYmIGhpc3RvcnkuZ2V0Q3VycmVudExvY2F0aW9uO1xufVxuXG52YXIgX1JlYWN0JFByb3BUeXBlcyA9IF9yZWFjdDIuZGVmYXVsdC5Qcm9wVHlwZXM7XG52YXIgZnVuYyA9IF9SZWFjdCRQcm9wVHlwZXMuZnVuYztcbnZhciBvYmplY3QgPSBfUmVhY3QkUHJvcFR5cGVzLm9iamVjdDtcblxuLyoqXG4gKiBBIDxSb3V0ZXI+IGlzIGEgaGlnaC1sZXZlbCBBUEkgZm9yIGF1dG9tYXRpY2FsbHkgc2V0dGluZyB1cFxuICogYSByb3V0ZXIgdGhhdCByZW5kZXJzIGEgPFJvdXRlckNvbnRleHQ+IHdpdGggYWxsIHRoZSBwcm9wc1xuICogaXQgbmVlZHMgZWFjaCB0aW1lIHRoZSBVUkwgY2hhbmdlcy5cbiAqL1xuXG52YXIgUm91dGVyID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdSb3V0ZXInLFxuXG5cbiAgcHJvcFR5cGVzOiB7XG4gICAgaGlzdG9yeTogb2JqZWN0LFxuICAgIGNoaWxkcmVuOiBfSW50ZXJuYWxQcm9wVHlwZXMucm91dGVzLFxuICAgIHJvdXRlczogX0ludGVybmFsUHJvcFR5cGVzLnJvdXRlcywgLy8gYWxpYXMgZm9yIGNoaWxkcmVuXG4gICAgcmVuZGVyOiBmdW5jLFxuICAgIGNyZWF0ZUVsZW1lbnQ6IGZ1bmMsXG4gICAgb25FcnJvcjogZnVuYyxcbiAgICBvblVwZGF0ZTogZnVuYyxcblxuICAgIC8vIERlcHJlY2F0ZWQ6XG4gICAgcGFyc2VRdWVyeVN0cmluZzogZnVuYyxcbiAgICBzdHJpbmdpZnlRdWVyeTogZnVuYyxcblxuICAgIC8vIFBSSVZBVEU6IEZvciBjbGllbnQtc2lkZSByZWh5ZHJhdGlvbiBvZiBzZXJ2ZXIgbWF0Y2guXG4gICAgbWF0Y2hDb250ZXh0OiBvYmplY3RcbiAgfSxcblxuICBnZXREZWZhdWx0UHJvcHM6IGZ1bmN0aW9uIGdldERlZmF1bHRQcm9wcygpIHtcbiAgICByZXR1cm4ge1xuICAgICAgcmVuZGVyOiBmdW5jdGlvbiByZW5kZXIocHJvcHMpIHtcbiAgICAgICAgcmV0dXJuIF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVFbGVtZW50KF9Sb3V0ZXJDb250ZXh0Mi5kZWZhdWx0LCBwcm9wcyk7XG4gICAgICB9XG4gICAgfTtcbiAgfSxcbiAgZ2V0SW5pdGlhbFN0YXRlOiBmdW5jdGlvbiBnZXRJbml0aWFsU3RhdGUoKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIGxvY2F0aW9uOiBudWxsLFxuICAgICAgcm91dGVzOiBudWxsLFxuICAgICAgcGFyYW1zOiBudWxsLFxuICAgICAgY29tcG9uZW50czogbnVsbFxuICAgIH07XG4gIH0sXG4gIGhhbmRsZUVycm9yOiBmdW5jdGlvbiBoYW5kbGVFcnJvcihlcnJvcikge1xuICAgIGlmICh0aGlzLnByb3BzLm9uRXJyb3IpIHtcbiAgICAgIHRoaXMucHJvcHMub25FcnJvci5jYWxsKHRoaXMsIGVycm9yKTtcbiAgICB9IGVsc2Uge1xuICAgICAgLy8gVGhyb3cgZXJyb3JzIGJ5IGRlZmF1bHQgc28gd2UgZG9uJ3Qgc2lsZW50bHkgc3dhbGxvdyB0aGVtIVxuICAgICAgdGhyb3cgZXJyb3I7IC8vIFRoaXMgZXJyb3IgcHJvYmFibHkgb2NjdXJyZWQgaW4gZ2V0Q2hpbGRSb3V0ZXMgb3IgZ2V0Q29tcG9uZW50cy5cbiAgICB9XG4gIH0sXG4gIGNvbXBvbmVudFdpbGxNb3VudDogZnVuY3Rpb24gY29tcG9uZW50V2lsbE1vdW50KCkge1xuICAgIHZhciBfdGhpcyA9IHRoaXM7XG5cbiAgICB2YXIgX3Byb3BzID0gdGhpcy5wcm9wcztcbiAgICB2YXIgcGFyc2VRdWVyeVN0cmluZyA9IF9wcm9wcy5wYXJzZVF1ZXJ5U3RyaW5nO1xuICAgIHZhciBzdHJpbmdpZnlRdWVyeSA9IF9wcm9wcy5zdHJpbmdpZnlRdWVyeTtcblxuICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKCEocGFyc2VRdWVyeVN0cmluZyB8fCBzdHJpbmdpZnlRdWVyeSksICdgcGFyc2VRdWVyeVN0cmluZ2AgYW5kIGBzdHJpbmdpZnlRdWVyeWAgYXJlIGRlcHJlY2F0ZWQuIFBsZWFzZSBjcmVhdGUgYSBjdXN0b20gaGlzdG9yeS4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWN1c3RvbXF1ZXJ5c3RyaW5nJykgOiB2b2lkIDA7XG5cbiAgICB2YXIgX2NyZWF0ZVJvdXRlck9iamVjdHMgPSB0aGlzLmNyZWF0ZVJvdXRlck9iamVjdHMoKTtcblxuICAgIHZhciBoaXN0b3J5ID0gX2NyZWF0ZVJvdXRlck9iamVjdHMuaGlzdG9yeTtcbiAgICB2YXIgdHJhbnNpdGlvbk1hbmFnZXIgPSBfY3JlYXRlUm91dGVyT2JqZWN0cy50cmFuc2l0aW9uTWFuYWdlcjtcbiAgICB2YXIgcm91dGVyID0gX2NyZWF0ZVJvdXRlck9iamVjdHMucm91dGVyO1xuXG5cbiAgICB0aGlzLl91bmxpc3RlbiA9IHRyYW5zaXRpb25NYW5hZ2VyLmxpc3RlbihmdW5jdGlvbiAoZXJyb3IsIHN0YXRlKSB7XG4gICAgICBpZiAoZXJyb3IpIHtcbiAgICAgICAgX3RoaXMuaGFuZGxlRXJyb3IoZXJyb3IpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgX3RoaXMuc2V0U3RhdGUoc3RhdGUsIF90aGlzLnByb3BzLm9uVXBkYXRlKTtcbiAgICAgIH1cbiAgICB9KTtcblxuICAgIHRoaXMuaGlzdG9yeSA9IGhpc3Rvcnk7XG4gICAgdGhpcy5yb3V0ZXIgPSByb3V0ZXI7XG4gIH0sXG4gIGNyZWF0ZVJvdXRlck9iamVjdHM6IGZ1bmN0aW9uIGNyZWF0ZVJvdXRlck9iamVjdHMoKSB7XG4gICAgdmFyIG1hdGNoQ29udGV4dCA9IHRoaXMucHJvcHMubWF0Y2hDb250ZXh0O1xuXG4gICAgaWYgKG1hdGNoQ29udGV4dCkge1xuICAgICAgcmV0dXJuIG1hdGNoQ29udGV4dDtcbiAgICB9XG5cbiAgICB2YXIgaGlzdG9yeSA9IHRoaXMucHJvcHMuaGlzdG9yeTtcbiAgICB2YXIgX3Byb3BzMiA9IHRoaXMucHJvcHM7XG4gICAgdmFyIHJvdXRlcyA9IF9wcm9wczIucm91dGVzO1xuICAgIHZhciBjaGlsZHJlbiA9IF9wcm9wczIuY2hpbGRyZW47XG5cblxuICAgICEhaXNVbnN1cHBvcnRlZEhpc3RvcnkoaGlzdG9yeSkgPyBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnWW91IGhhdmUgcHJvdmlkZWQgYSBoaXN0b3J5IG9iamVjdCBjcmVhdGVkIHdpdGggaGlzdG9yeSB2My54LiAnICsgJ1RoaXMgdmVyc2lvbiBvZiBSZWFjdCBSb3V0ZXIgaXMgbm90IGNvbXBhdGlibGUgd2l0aCB2MyBoaXN0b3J5ICcgKyAnb2JqZWN0cy4gUGxlYXNlIHVzZSBoaXN0b3J5IHYyLnggaW5zdGVhZC4nKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgICBpZiAoaXNEZXByZWNhdGVkSGlzdG9yeShoaXN0b3J5KSkge1xuICAgICAgaGlzdG9yeSA9IHRoaXMud3JhcERlcHJlY2F0ZWRIaXN0b3J5KGhpc3RvcnkpO1xuICAgIH1cblxuICAgIHZhciB0cmFuc2l0aW9uTWFuYWdlciA9ICgwLCBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIyLmRlZmF1bHQpKGhpc3RvcnksICgwLCBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZXMpKHJvdXRlcyB8fCBjaGlsZHJlbikpO1xuICAgIHZhciByb3V0ZXIgPSAoMCwgX1JvdXRlclV0aWxzLmNyZWF0ZVJvdXRlck9iamVjdCkoaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXIpO1xuICAgIHZhciByb3V0aW5nSGlzdG9yeSA9ICgwLCBfUm91dGVyVXRpbHMuY3JlYXRlUm91dGluZ0hpc3RvcnkpKGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyKTtcblxuICAgIHJldHVybiB7IGhpc3Rvcnk6IHJvdXRpbmdIaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcjogdHJhbnNpdGlvbk1hbmFnZXIsIHJvdXRlcjogcm91dGVyIH07XG4gIH0sXG4gIHdyYXBEZXByZWNhdGVkSGlzdG9yeTogZnVuY3Rpb24gd3JhcERlcHJlY2F0ZWRIaXN0b3J5KGhpc3RvcnkpIHtcbiAgICB2YXIgX3Byb3BzMyA9IHRoaXMucHJvcHM7XG4gICAgdmFyIHBhcnNlUXVlcnlTdHJpbmcgPSBfcHJvcHMzLnBhcnNlUXVlcnlTdHJpbmc7XG4gICAgdmFyIHN0cmluZ2lmeVF1ZXJ5ID0gX3Byb3BzMy5zdHJpbmdpZnlRdWVyeTtcblxuXG4gICAgdmFyIGNyZWF0ZUhpc3RvcnkgPSB2b2lkIDA7XG4gICAgaWYgKGhpc3RvcnkpIHtcbiAgICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnSXQgYXBwZWFycyB5b3UgaGF2ZSBwcm92aWRlZCBhIGRlcHJlY2F0ZWQgaGlzdG9yeSBvYmplY3QgdG8gYDxSb3V0ZXIvPmAsIHBsZWFzZSB1c2UgYSBoaXN0b3J5IHByb3ZpZGVkIGJ5ICcgKyAnUmVhY3QgUm91dGVyIHdpdGggYGltcG9ydCB7IGJyb3dzZXJIaXN0b3J5IH0gZnJvbSBcXCdyZWFjdC1yb3V0ZXJcXCdgIG9yIGBpbXBvcnQgeyBoYXNoSGlzdG9yeSB9IGZyb20gXFwncmVhY3Qtcm91dGVyXFwnYC4gJyArICdJZiB5b3UgYXJlIHVzaW5nIGEgY3VzdG9tIGhpc3RvcnkgcGxlYXNlIGNyZWF0ZSBpdCB3aXRoIGB1c2VSb3V0ZXJIaXN0b3J5YCwgc2VlIGh0dHA6Ly90aW55LmNjL3JvdXRlci11c2luZ2hpc3RvcnkgZm9yIGRldGFpbHMuJykgOiB2b2lkIDA7XG4gICAgICBjcmVhdGVIaXN0b3J5ID0gZnVuY3Rpb24gY3JlYXRlSGlzdG9yeSgpIHtcbiAgICAgICAgcmV0dXJuIGhpc3Rvcnk7XG4gICAgICB9O1xuICAgIH0gZWxzZSB7XG4gICAgICBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2BSb3V0ZXJgIG5vIGxvbmdlciBkZWZhdWx0cyB0aGUgaGlzdG9yeSBwcm9wIHRvIGhhc2ggaGlzdG9yeS4gUGxlYXNlIHVzZSB0aGUgYGhhc2hIaXN0b3J5YCBzaW5nbGV0b24gaW5zdGVhZC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWRlZmF1bHRoaXN0b3J5JykgOiB2b2lkIDA7XG4gICAgICBjcmVhdGVIaXN0b3J5ID0gX2NyZWF0ZUhhc2hIaXN0b3J5Mi5kZWZhdWx0O1xuICAgIH1cblxuICAgIHJldHVybiAoMCwgX3VzZVF1ZXJpZXMyLmRlZmF1bHQpKGNyZWF0ZUhpc3RvcnkpKHsgcGFyc2VRdWVyeVN0cmluZzogcGFyc2VRdWVyeVN0cmluZywgc3RyaW5naWZ5UXVlcnk6IHN0cmluZ2lmeVF1ZXJ5IH0pO1xuICB9LFxuXG5cbiAgLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHNhbml0eSBjaGVjayAqL1xuICBjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzOiBmdW5jdGlvbiBjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKG5leHRQcm9wcy5oaXN0b3J5ID09PSB0aGlzLnByb3BzLmhpc3RvcnksICdZb3UgY2Fubm90IGNoYW5nZSA8Um91dGVyIGhpc3Rvcnk+OyBpdCB3aWxsIGJlIGlnbm9yZWQnKSA6IHZvaWQgMDtcblxuICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKChuZXh0UHJvcHMucm91dGVzIHx8IG5leHRQcm9wcy5jaGlsZHJlbikgPT09ICh0aGlzLnByb3BzLnJvdXRlcyB8fCB0aGlzLnByb3BzLmNoaWxkcmVuKSwgJ1lvdSBjYW5ub3QgY2hhbmdlIDxSb3V0ZXIgcm91dGVzPjsgaXQgd2lsbCBiZSBpZ25vcmVkJykgOiB2b2lkIDA7XG4gIH0sXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50OiBmdW5jdGlvbiBjb21wb25lbnRXaWxsVW5tb3VudCgpIHtcbiAgICBpZiAodGhpcy5fdW5saXN0ZW4pIHRoaXMuX3VubGlzdGVuKCk7XG4gIH0sXG4gIHJlbmRlcjogZnVuY3Rpb24gcmVuZGVyKCkge1xuICAgIHZhciBfc3RhdGUgPSB0aGlzLnN0YXRlO1xuICAgIHZhciBsb2NhdGlvbiA9IF9zdGF0ZS5sb2NhdGlvbjtcbiAgICB2YXIgcm91dGVzID0gX3N0YXRlLnJvdXRlcztcbiAgICB2YXIgcGFyYW1zID0gX3N0YXRlLnBhcmFtcztcbiAgICB2YXIgY29tcG9uZW50cyA9IF9zdGF0ZS5jb21wb25lbnRzO1xuICAgIHZhciBfcHJvcHM0ID0gdGhpcy5wcm9wcztcbiAgICB2YXIgY3JlYXRlRWxlbWVudCA9IF9wcm9wczQuY3JlYXRlRWxlbWVudDtcbiAgICB2YXIgcmVuZGVyID0gX3Byb3BzNC5yZW5kZXI7XG5cbiAgICB2YXIgcHJvcHMgPSBfb2JqZWN0V2l0aG91dFByb3BlcnRpZXMoX3Byb3BzNCwgWydjcmVhdGVFbGVtZW50JywgJ3JlbmRlciddKTtcblxuICAgIGlmIChsb2NhdGlvbiA9PSBudWxsKSByZXR1cm4gbnVsbDsgLy8gQXN5bmMgbWF0Y2hcblxuICAgIC8vIE9ubHkgZm9yd2FyZCBub24tUm91dGVyLXNwZWNpZmljIHByb3BzIHRvIHJvdXRpbmcgY29udGV4dCwgYXMgdGhvc2UgYXJlXG4gICAgLy8gdGhlIG9ubHkgb25lcyB0aGF0IG1pZ2h0IGJlIGN1c3RvbSByb3V0aW5nIGNvbnRleHQgcHJvcHMuXG4gICAgT2JqZWN0LmtleXMoUm91dGVyLnByb3BUeXBlcykuZm9yRWFjaChmdW5jdGlvbiAocHJvcFR5cGUpIHtcbiAgICAgIHJldHVybiBkZWxldGUgcHJvcHNbcHJvcFR5cGVdO1xuICAgIH0pO1xuXG4gICAgcmV0dXJuIHJlbmRlcihfZXh0ZW5kcyh7fSwgcHJvcHMsIHtcbiAgICAgIGhpc3Rvcnk6IHRoaXMuaGlzdG9yeSxcbiAgICAgIHJvdXRlcjogdGhpcy5yb3V0ZXIsXG4gICAgICBsb2NhdGlvbjogbG9jYXRpb24sXG4gICAgICByb3V0ZXM6IHJvdXRlcyxcbiAgICAgIHBhcmFtczogcGFyYW1zLFxuICAgICAgY29tcG9uZW50czogY29tcG9uZW50cyxcbiAgICAgIGNyZWF0ZUVsZW1lbnQ6IGNyZWF0ZUVsZW1lbnRcbiAgICB9KSk7XG4gIH1cbn0pO1xuXG5leHBvcnRzLmRlZmF1bHQgPSBSb3V0ZXI7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfdHlwZW9mID0gdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIHR5cGVvZiBTeW1ib2wuaXRlcmF0b3IgPT09IFwic3ltYm9sXCIgPyBmdW5jdGlvbiAob2JqKSB7IHJldHVybiB0eXBlb2Ygb2JqOyB9IDogZnVuY3Rpb24gKG9iaikgeyByZXR1cm4gb2JqICYmIHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiBvYmouY29uc3RydWN0b3IgPT09IFN5bWJvbCA/IFwic3ltYm9sXCIgOiB0eXBlb2Ygb2JqOyB9O1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX3JlYWN0ID0gcmVxdWlyZSgncmVhY3QnKTtcblxudmFyIF9yZWFjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yZWFjdCk7XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcycpO1xuXG52YXIgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyk7XG5cbnZhciBfZ2V0Um91dGVQYXJhbXMgPSByZXF1aXJlKCcuL2dldFJvdXRlUGFyYW1zJyk7XG5cbnZhciBfZ2V0Um91dGVQYXJhbXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZ2V0Um91dGVQYXJhbXMpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIF9SZWFjdCRQcm9wVHlwZXMgPSBfcmVhY3QyLmRlZmF1bHQuUHJvcFR5cGVzO1xudmFyIGFycmF5ID0gX1JlYWN0JFByb3BUeXBlcy5hcnJheTtcbnZhciBmdW5jID0gX1JlYWN0JFByb3BUeXBlcy5mdW5jO1xudmFyIG9iamVjdCA9IF9SZWFjdCRQcm9wVHlwZXMub2JqZWN0O1xuXG4vKipcbiAqIEEgPFJvdXRlckNvbnRleHQ+IHJlbmRlcnMgdGhlIGNvbXBvbmVudCB0cmVlIGZvciBhIGdpdmVuIHJvdXRlciBzdGF0ZVxuICogYW5kIHNldHMgdGhlIGhpc3Rvcnkgb2JqZWN0IGFuZCB0aGUgY3VycmVudCBsb2NhdGlvbiBpbiBjb250ZXh0LlxuICovXG5cbnZhciBSb3V0ZXJDb250ZXh0ID0gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUNsYXNzKHtcbiAgZGlzcGxheU5hbWU6ICdSb3V0ZXJDb250ZXh0JyxcblxuXG4gIHByb3BUeXBlczoge1xuICAgIGhpc3Rvcnk6IG9iamVjdCxcbiAgICByb3V0ZXI6IG9iamVjdC5pc1JlcXVpcmVkLFxuICAgIGxvY2F0aW9uOiBvYmplY3QuaXNSZXF1aXJlZCxcbiAgICByb3V0ZXM6IGFycmF5LmlzUmVxdWlyZWQsXG4gICAgcGFyYW1zOiBvYmplY3QuaXNSZXF1aXJlZCxcbiAgICBjb21wb25lbnRzOiBhcnJheS5pc1JlcXVpcmVkLFxuICAgIGNyZWF0ZUVsZW1lbnQ6IGZ1bmMuaXNSZXF1aXJlZFxuICB9LFxuXG4gIGdldERlZmF1bHRQcm9wczogZnVuY3Rpb24gZ2V0RGVmYXVsdFByb3BzKCkge1xuICAgIHJldHVybiB7XG4gICAgICBjcmVhdGVFbGVtZW50OiBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudFxuICAgIH07XG4gIH0sXG5cblxuICBjaGlsZENvbnRleHRUeXBlczoge1xuICAgIGhpc3Rvcnk6IG9iamVjdCxcbiAgICBsb2NhdGlvbjogb2JqZWN0LmlzUmVxdWlyZWQsXG4gICAgcm91dGVyOiBvYmplY3QuaXNSZXF1aXJlZFxuICB9LFxuXG4gIGdldENoaWxkQ29udGV4dDogZnVuY3Rpb24gZ2V0Q2hpbGRDb250ZXh0KCkge1xuICAgIHZhciBfcHJvcHMgPSB0aGlzLnByb3BzO1xuICAgIHZhciByb3V0ZXIgPSBfcHJvcHMucm91dGVyO1xuICAgIHZhciBoaXN0b3J5ID0gX3Byb3BzLmhpc3Rvcnk7XG4gICAgdmFyIGxvY2F0aW9uID0gX3Byb3BzLmxvY2F0aW9uO1xuXG4gICAgaWYgKCFyb3V0ZXIpIHtcbiAgICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnYDxSb3V0ZXJDb250ZXh0PmAgZXhwZWN0cyBhIGByb3V0ZXJgIHJhdGhlciB0aGFuIGEgYGhpc3RvcnlgJykgOiB2b2lkIDA7XG5cbiAgICAgIHJvdXRlciA9IF9leHRlbmRzKHt9LCBoaXN0b3J5LCB7XG4gICAgICAgIHNldFJvdXRlTGVhdmVIb29rOiBoaXN0b3J5Lmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZVxuICAgICAgfSk7XG4gICAgICBkZWxldGUgcm91dGVyLmxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZTtcbiAgICB9XG5cbiAgICBpZiAoXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgbG9jYXRpb24gPSAoMCwgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMyLmRlZmF1bHQpKGxvY2F0aW9uLCAnYGNvbnRleHQubG9jYXRpb25gIGlzIGRlcHJlY2F0ZWQsIHBsZWFzZSB1c2UgYSByb3V0ZSBjb21wb25lbnRcXCdzIGBwcm9wcy5sb2NhdGlvbmAgaW5zdGVhZC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWFjY2Vzc2luZ2xvY2F0aW9uJyk7XG4gICAgfVxuXG4gICAgcmV0dXJuIHsgaGlzdG9yeTogaGlzdG9yeSwgbG9jYXRpb246IGxvY2F0aW9uLCByb3V0ZXI6IHJvdXRlciB9O1xuICB9LFxuICBjcmVhdGVFbGVtZW50OiBmdW5jdGlvbiBjcmVhdGVFbGVtZW50KGNvbXBvbmVudCwgcHJvcHMpIHtcbiAgICByZXR1cm4gY29tcG9uZW50ID09IG51bGwgPyBudWxsIDogdGhpcy5wcm9wcy5jcmVhdGVFbGVtZW50KGNvbXBvbmVudCwgcHJvcHMpO1xuICB9LFxuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICB2YXIgX3RoaXMgPSB0aGlzO1xuXG4gICAgdmFyIF9wcm9wczIgPSB0aGlzLnByb3BzO1xuICAgIHZhciBoaXN0b3J5ID0gX3Byb3BzMi5oaXN0b3J5O1xuICAgIHZhciBsb2NhdGlvbiA9IF9wcm9wczIubG9jYXRpb247XG4gICAgdmFyIHJvdXRlcyA9IF9wcm9wczIucm91dGVzO1xuICAgIHZhciBwYXJhbXMgPSBfcHJvcHMyLnBhcmFtcztcbiAgICB2YXIgY29tcG9uZW50cyA9IF9wcm9wczIuY29tcG9uZW50cztcblxuICAgIHZhciBlbGVtZW50ID0gbnVsbDtcblxuICAgIGlmIChjb21wb25lbnRzKSB7XG4gICAgICBlbGVtZW50ID0gY29tcG9uZW50cy5yZWR1Y2VSaWdodChmdW5jdGlvbiAoZWxlbWVudCwgY29tcG9uZW50cywgaW5kZXgpIHtcbiAgICAgICAgaWYgKGNvbXBvbmVudHMgPT0gbnVsbCkgcmV0dXJuIGVsZW1lbnQ7IC8vIERvbid0IGNyZWF0ZSBuZXcgY2hpbGRyZW47IHVzZSB0aGUgZ3JhbmRjaGlsZHJlbi5cblxuICAgICAgICB2YXIgcm91dGUgPSByb3V0ZXNbaW5kZXhdO1xuICAgICAgICB2YXIgcm91dGVQYXJhbXMgPSAoMCwgX2dldFJvdXRlUGFyYW1zMi5kZWZhdWx0KShyb3V0ZSwgcGFyYW1zKTtcbiAgICAgICAgdmFyIHByb3BzID0ge1xuICAgICAgICAgIGhpc3Rvcnk6IGhpc3RvcnksXG4gICAgICAgICAgbG9jYXRpb246IGxvY2F0aW9uLFxuICAgICAgICAgIHBhcmFtczogcGFyYW1zLFxuICAgICAgICAgIHJvdXRlOiByb3V0ZSxcbiAgICAgICAgICByb3V0ZVBhcmFtczogcm91dGVQYXJhbXMsXG4gICAgICAgICAgcm91dGVzOiByb3V0ZXNcbiAgICAgICAgfTtcblxuICAgICAgICBpZiAoKDAsIF9Sb3V0ZVV0aWxzLmlzUmVhY3RDaGlsZHJlbikoZWxlbWVudCkpIHtcbiAgICAgICAgICBwcm9wcy5jaGlsZHJlbiA9IGVsZW1lbnQ7XG4gICAgICAgIH0gZWxzZSBpZiAoZWxlbWVudCkge1xuICAgICAgICAgIGZvciAodmFyIHByb3AgaW4gZWxlbWVudCkge1xuICAgICAgICAgICAgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChlbGVtZW50LCBwcm9wKSkgcHJvcHNbcHJvcF0gPSBlbGVtZW50W3Byb3BdO1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIGlmICgodHlwZW9mIGNvbXBvbmVudHMgPT09ICd1bmRlZmluZWQnID8gJ3VuZGVmaW5lZCcgOiBfdHlwZW9mKGNvbXBvbmVudHMpKSA9PT0gJ29iamVjdCcpIHtcbiAgICAgICAgICB2YXIgZWxlbWVudHMgPSB7fTtcblxuICAgICAgICAgIGZvciAodmFyIGtleSBpbiBjb21wb25lbnRzKSB7XG4gICAgICAgICAgICBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKGNvbXBvbmVudHMsIGtleSkpIHtcbiAgICAgICAgICAgICAgLy8gUGFzcyB0aHJvdWdoIHRoZSBrZXkgYXMgYSBwcm9wIHRvIGNyZWF0ZUVsZW1lbnQgdG8gYWxsb3dcbiAgICAgICAgICAgICAgLy8gY3VzdG9tIGNyZWF0ZUVsZW1lbnQgZnVuY3Rpb25zIHRvIGtub3cgd2hpY2ggbmFtZWQgY29tcG9uZW50XG4gICAgICAgICAgICAgIC8vIHRoZXkncmUgcmVuZGVyaW5nLCBmb3IgZS5nLiBtYXRjaGluZyB1cCB0byBmZXRjaGVkIGRhdGEuXG4gICAgICAgICAgICAgIGVsZW1lbnRzW2tleV0gPSBfdGhpcy5jcmVhdGVFbGVtZW50KGNvbXBvbmVudHNba2V5XSwgX2V4dGVuZHMoe1xuICAgICAgICAgICAgICAgIGtleToga2V5IH0sIHByb3BzKSk7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgfVxuXG4gICAgICAgICAgcmV0dXJuIGVsZW1lbnRzO1xuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIF90aGlzLmNyZWF0ZUVsZW1lbnQoY29tcG9uZW50cywgcHJvcHMpO1xuICAgICAgfSwgZWxlbWVudCk7XG4gICAgfVxuXG4gICAgIShlbGVtZW50ID09PSBudWxsIHx8IGVsZW1lbnQgPT09IGZhbHNlIHx8IF9yZWFjdDIuZGVmYXVsdC5pc1ZhbGlkRWxlbWVudChlbGVtZW50KSkgPyBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnVGhlIHJvb3Qgcm91dGUgbXVzdCByZW5kZXIgYSBzaW5nbGUgZWxlbWVudCcpIDogKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlKSA6IHZvaWQgMDtcblxuICAgIHJldHVybiBlbGVtZW50O1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gUm91dGVyQ29udGV4dDtcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZXhwb3J0cy5jcmVhdGVSb3V0ZXJPYmplY3QgPSBjcmVhdGVSb3V0ZXJPYmplY3Q7XG5leHBvcnRzLmNyZWF0ZVJvdXRpbmdIaXN0b3J5ID0gY3JlYXRlUm91dGluZ0hpc3Rvcnk7XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcycpO1xuXG52YXIgX2RlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIGNyZWF0ZVJvdXRlck9iamVjdChoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcikge1xuICByZXR1cm4gX2V4dGVuZHMoe30sIGhpc3RvcnksIHtcbiAgICBzZXRSb3V0ZUxlYXZlSG9vazogdHJhbnNpdGlvbk1hbmFnZXIubGlzdGVuQmVmb3JlTGVhdmluZ1JvdXRlLFxuICAgIGlzQWN0aXZlOiB0cmFuc2l0aW9uTWFuYWdlci5pc0FjdGl2ZVxuICB9KTtcbn1cblxuLy8gZGVwcmVjYXRlZFxuZnVuY3Rpb24gY3JlYXRlUm91dGluZ0hpc3RvcnkoaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXIpIHtcbiAgaGlzdG9yeSA9IF9leHRlbmRzKHt9LCBoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG5cbiAgaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICBoaXN0b3J5ID0gKDAsIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzMi5kZWZhdWx0KShoaXN0b3J5LCAnYHByb3BzLmhpc3RvcnlgIGFuZCBgY29udGV4dC5oaXN0b3J5YCBhcmUgZGVwcmVjYXRlZC4gUGxlYXNlIHVzZSBgY29udGV4dC5yb3V0ZXJgLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItY29udGV4dGNoYW5nZXMnKTtcbiAgfVxuXG4gIHJldHVybiBoaXN0b3J5O1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX1JvdXRlckNvbnRleHQgPSByZXF1aXJlKCcuL1JvdXRlckNvbnRleHQnKTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlckNvbnRleHQpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgUm91dGluZ0NvbnRleHQgPSBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlQ2xhc3Moe1xuICBkaXNwbGF5TmFtZTogJ1JvdXRpbmdDb250ZXh0JyxcbiAgY29tcG9uZW50V2lsbE1vdW50OiBmdW5jdGlvbiBjb21wb25lbnRXaWxsTW91bnQoKSB7XG4gICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdgUm91dGluZ0NvbnRleHRgIGhhcyBiZWVuIHJlbmFtZWQgdG8gYFJvdXRlckNvbnRleHRgLiBQbGVhc2UgdXNlIGBpbXBvcnQgeyBSb3V0ZXJDb250ZXh0IH0gZnJvbSBcXCdyZWFjdC1yb3V0ZXJcXCdgLiBodHRwOi8vdGlueS5jYy9yb3V0ZXItcm91dGVyY29udGV4dCcpIDogdm9pZCAwO1xuICB9LFxuICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICByZXR1cm4gX3JlYWN0Mi5kZWZhdWx0LmNyZWF0ZUVsZW1lbnQoX1JvdXRlckNvbnRleHQyLmRlZmF1bHQsIHRoaXMucHJvcHMpO1xuICB9XG59KTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gUm91dGluZ0NvbnRleHQ7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLnJ1bkVudGVySG9va3MgPSBydW5FbnRlckhvb2tzO1xuZXhwb3J0cy5ydW5DaGFuZ2VIb29rcyA9IHJ1bkNoYW5nZUhvb2tzO1xuZXhwb3J0cy5ydW5MZWF2ZUhvb2tzID0gcnVuTGVhdmVIb29rcztcblxudmFyIF9Bc3luY1V0aWxzID0gcmVxdWlyZSgnLi9Bc3luY1V0aWxzJyk7XG5cbnZhciBfcm91dGVyV2FybmluZyA9IHJlcXVpcmUoJy4vcm91dGVyV2FybmluZycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcm91dGVyV2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIGNyZWF0ZVRyYW5zaXRpb25Ib29rKGhvb2ssIHJvdXRlLCBhc3luY0FyaXR5KSB7XG4gIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIGFyZ3MgPSBBcnJheShfbGVuKSwgX2tleSA9IDA7IF9rZXkgPCBfbGVuOyBfa2V5KyspIHtcbiAgICAgIGFyZ3NbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gICAgfVxuXG4gICAgaG9vay5hcHBseShyb3V0ZSwgYXJncyk7XG5cbiAgICBpZiAoaG9vay5sZW5ndGggPCBhc3luY0FyaXR5KSB7XG4gICAgICB2YXIgY2FsbGJhY2sgPSBhcmdzW2FyZ3MubGVuZ3RoIC0gMV07XG4gICAgICAvLyBBc3N1bWUgaG9vayBleGVjdXRlcyBzeW5jaHJvbm91c2x5IGFuZFxuICAgICAgLy8gYXV0b21hdGljYWxseSBjYWxsIHRoZSBjYWxsYmFjay5cbiAgICAgIGNhbGxiYWNrKCk7XG4gICAgfVxuICB9O1xufVxuXG5mdW5jdGlvbiBnZXRFbnRlckhvb2tzKHJvdXRlcykge1xuICByZXR1cm4gcm91dGVzLnJlZHVjZShmdW5jdGlvbiAoaG9va3MsIHJvdXRlKSB7XG4gICAgaWYgKHJvdXRlLm9uRW50ZXIpIGhvb2tzLnB1c2goY3JlYXRlVHJhbnNpdGlvbkhvb2socm91dGUub25FbnRlciwgcm91dGUsIDMpKTtcblxuICAgIHJldHVybiBob29rcztcbiAgfSwgW10pO1xufVxuXG5mdW5jdGlvbiBnZXRDaGFuZ2VIb29rcyhyb3V0ZXMpIHtcbiAgcmV0dXJuIHJvdXRlcy5yZWR1Y2UoZnVuY3Rpb24gKGhvb2tzLCByb3V0ZSkge1xuICAgIGlmIChyb3V0ZS5vbkNoYW5nZSkgaG9va3MucHVzaChjcmVhdGVUcmFuc2l0aW9uSG9vayhyb3V0ZS5vbkNoYW5nZSwgcm91dGUsIDQpKTtcbiAgICByZXR1cm4gaG9va3M7XG4gIH0sIFtdKTtcbn1cblxuZnVuY3Rpb24gcnVuVHJhbnNpdGlvbkhvb2tzKGxlbmd0aCwgaXRlciwgY2FsbGJhY2spIHtcbiAgaWYgKCFsZW5ndGgpIHtcbiAgICBjYWxsYmFjaygpO1xuICAgIHJldHVybjtcbiAgfVxuXG4gIHZhciByZWRpcmVjdEluZm8gPSB2b2lkIDA7XG4gIGZ1bmN0aW9uIHJlcGxhY2UobG9jYXRpb24sIGRlcHJlY2F0ZWRQYXRobmFtZSwgZGVwcmVjYXRlZFF1ZXJ5KSB7XG4gICAgaWYgKGRlcHJlY2F0ZWRQYXRobmFtZSkge1xuICAgICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdgcmVwbGFjZVN0YXRlKHN0YXRlLCBwYXRobmFtZSwgcXVlcnkpIGlzIGRlcHJlY2F0ZWQ7IHVzZSBgcmVwbGFjZShsb2NhdGlvbilgIHdpdGggYSBsb2NhdGlvbiBkZXNjcmlwdG9yIGluc3RlYWQuIGh0dHA6Ly90aW55LmNjL3JvdXRlci1pc0FjdGl2ZWRlcHJlY2F0ZWQnKSA6IHZvaWQgMDtcbiAgICAgIHJlZGlyZWN0SW5mbyA9IHtcbiAgICAgICAgcGF0aG5hbWU6IGRlcHJlY2F0ZWRQYXRobmFtZSxcbiAgICAgICAgcXVlcnk6IGRlcHJlY2F0ZWRRdWVyeSxcbiAgICAgICAgc3RhdGU6IGxvY2F0aW9uXG4gICAgICB9O1xuXG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgcmVkaXJlY3RJbmZvID0gbG9jYXRpb247XG4gIH1cblxuICAoMCwgX0FzeW5jVXRpbHMubG9vcEFzeW5jKShsZW5ndGgsIGZ1bmN0aW9uIChpbmRleCwgbmV4dCwgZG9uZSkge1xuICAgIGl0ZXIoaW5kZXgsIHJlcGxhY2UsIGZ1bmN0aW9uIChlcnJvcikge1xuICAgICAgaWYgKGVycm9yIHx8IHJlZGlyZWN0SW5mbykge1xuICAgICAgICBkb25lKGVycm9yLCByZWRpcmVjdEluZm8pOyAvLyBObyBuZWVkIHRvIGNvbnRpbnVlLlxuICAgICAgfSBlbHNlIHtcbiAgICAgICAgbmV4dCgpO1xuICAgICAgfVxuICAgIH0pO1xuICB9LCBjYWxsYmFjayk7XG59XG5cbi8qKlxuICogUnVucyBhbGwgb25FbnRlciBob29rcyBpbiB0aGUgZ2l2ZW4gYXJyYXkgb2Ygcm91dGVzIGluIG9yZGVyXG4gKiB3aXRoIG9uRW50ZXIobmV4dFN0YXRlLCByZXBsYWNlLCBjYWxsYmFjaykgYW5kIGNhbGxzXG4gKiBjYWxsYmFjayhlcnJvciwgcmVkaXJlY3RJbmZvKSB3aGVuIGZpbmlzaGVkLiBUaGUgZmlyc3QgaG9va1xuICogdG8gdXNlIHJlcGxhY2Ugc2hvcnQtY2lyY3VpdHMgdGhlIGxvb3AuXG4gKlxuICogSWYgYSBob29rIG5lZWRzIHRvIHJ1biBhc3luY2hyb25vdXNseSwgaXQgbWF5IHVzZSB0aGUgY2FsbGJhY2tcbiAqIGZ1bmN0aW9uLiBIb3dldmVyLCBkb2luZyBzbyB3aWxsIGNhdXNlIHRoZSB0cmFuc2l0aW9uIHRvIHBhdXNlLFxuICogd2hpY2ggY291bGQgbGVhZCB0byBhIG5vbi1yZXNwb25zaXZlIFVJIGlmIHRoZSBob29rIGlzIHNsb3cuXG4gKi9cbmZ1bmN0aW9uIHJ1bkVudGVySG9va3Mocm91dGVzLCBuZXh0U3RhdGUsIGNhbGxiYWNrKSB7XG4gIHZhciBob29rcyA9IGdldEVudGVySG9va3Mocm91dGVzKTtcbiAgcmV0dXJuIHJ1blRyYW5zaXRpb25Ib29rcyhob29rcy5sZW5ndGgsIGZ1bmN0aW9uIChpbmRleCwgcmVwbGFjZSwgbmV4dCkge1xuICAgIGhvb2tzW2luZGV4XShuZXh0U3RhdGUsIHJlcGxhY2UsIG5leHQpO1xuICB9LCBjYWxsYmFjayk7XG59XG5cbi8qKlxuICogUnVucyBhbGwgb25DaGFuZ2UgaG9va3MgaW4gdGhlIGdpdmVuIGFycmF5IG9mIHJvdXRlcyBpbiBvcmRlclxuICogd2l0aCBvbkNoYW5nZShwcmV2U3RhdGUsIG5leHRTdGF0ZSwgcmVwbGFjZSwgY2FsbGJhY2spIGFuZCBjYWxsc1xuICogY2FsbGJhY2soZXJyb3IsIHJlZGlyZWN0SW5mbykgd2hlbiBmaW5pc2hlZC4gVGhlIGZpcnN0IGhvb2tcbiAqIHRvIHVzZSByZXBsYWNlIHNob3J0LWNpcmN1aXRzIHRoZSBsb29wLlxuICpcbiAqIElmIGEgaG9vayBuZWVkcyB0byBydW4gYXN5bmNocm9ub3VzbHksIGl0IG1heSB1c2UgdGhlIGNhbGxiYWNrXG4gKiBmdW5jdGlvbi4gSG93ZXZlciwgZG9pbmcgc28gd2lsbCBjYXVzZSB0aGUgdHJhbnNpdGlvbiB0byBwYXVzZSxcbiAqIHdoaWNoIGNvdWxkIGxlYWQgdG8gYSBub24tcmVzcG9uc2l2ZSBVSSBpZiB0aGUgaG9vayBpcyBzbG93LlxuICovXG5mdW5jdGlvbiBydW5DaGFuZ2VIb29rcyhyb3V0ZXMsIHN0YXRlLCBuZXh0U3RhdGUsIGNhbGxiYWNrKSB7XG4gIHZhciBob29rcyA9IGdldENoYW5nZUhvb2tzKHJvdXRlcyk7XG4gIHJldHVybiBydW5UcmFuc2l0aW9uSG9va3MoaG9va3MubGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIHJlcGxhY2UsIG5leHQpIHtcbiAgICBob29rc1tpbmRleF0oc3RhdGUsIG5leHRTdGF0ZSwgcmVwbGFjZSwgbmV4dCk7XG4gIH0sIGNhbGxiYWNrKTtcbn1cblxuLyoqXG4gKiBSdW5zIGFsbCBvbkxlYXZlIGhvb2tzIGluIHRoZSBnaXZlbiBhcnJheSBvZiByb3V0ZXMgaW4gb3JkZXIuXG4gKi9cbmZ1bmN0aW9uIHJ1bkxlYXZlSG9va3Mocm91dGVzLCBwcmV2U3RhdGUpIHtcbiAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IHJvdXRlcy5sZW5ndGg7IGkgPCBsZW47ICsraSkge1xuICAgIGlmIChyb3V0ZXNbaV0ub25MZWF2ZSkgcm91dGVzW2ldLm9uTGVhdmUuY2FsbChyb3V0ZXNbaV0sIHByZXZTdGF0ZSk7XG4gIH1cbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfcmVhY3QgPSByZXF1aXJlKCdyZWFjdCcpO1xuXG52YXIgX3JlYWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JlYWN0KTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0ID0gcmVxdWlyZSgnLi9Sb3V0ZXJDb250ZXh0Jyk7XG5cbnZhciBfUm91dGVyQ29udGV4dDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0ZXJDb250ZXh0KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZXhwb3J0cy5kZWZhdWx0ID0gZnVuY3Rpb24gKCkge1xuICBmb3IgKHZhciBfbGVuID0gYXJndW1lbnRzLmxlbmd0aCwgbWlkZGxld2FyZXMgPSBBcnJheShfbGVuKSwgX2tleSA9IDA7IF9rZXkgPCBfbGVuOyBfa2V5KyspIHtcbiAgICBtaWRkbGV3YXJlc1tfa2V5XSA9IGFyZ3VtZW50c1tfa2V5XTtcbiAgfVxuXG4gIHZhciB3aXRoQ29udGV4dCA9IG1pZGRsZXdhcmVzLm1hcChmdW5jdGlvbiAobSkge1xuICAgIHJldHVybiBtLnJlbmRlclJvdXRlckNvbnRleHQ7XG4gIH0pLmZpbHRlcihmdW5jdGlvbiAoZikge1xuICAgIHJldHVybiBmO1xuICB9KTtcbiAgdmFyIHdpdGhDb21wb25lbnQgPSBtaWRkbGV3YXJlcy5tYXAoZnVuY3Rpb24gKG0pIHtcbiAgICByZXR1cm4gbS5yZW5kZXJSb3V0ZUNvbXBvbmVudDtcbiAgfSkuZmlsdGVyKGZ1bmN0aW9uIChmKSB7XG4gICAgcmV0dXJuIGY7XG4gIH0pO1xuICB2YXIgbWFrZUNyZWF0ZUVsZW1lbnQgPSBmdW5jdGlvbiBtYWtlQ3JlYXRlRWxlbWVudCgpIHtcbiAgICB2YXIgYmFzZUNyZWF0ZUVsZW1lbnQgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyBfcmVhY3QuY3JlYXRlRWxlbWVudCA6IGFyZ3VtZW50c1swXTtcbiAgICByZXR1cm4gZnVuY3Rpb24gKENvbXBvbmVudCwgcHJvcHMpIHtcbiAgICAgIHJldHVybiB3aXRoQ29tcG9uZW50LnJlZHVjZVJpZ2h0KGZ1bmN0aW9uIChwcmV2aW91cywgcmVuZGVyUm91dGVDb21wb25lbnQpIHtcbiAgICAgICAgcmV0dXJuIHJlbmRlclJvdXRlQ29tcG9uZW50KHByZXZpb3VzLCBwcm9wcyk7XG4gICAgICB9LCBiYXNlQ3JlYXRlRWxlbWVudChDb21wb25lbnQsIHByb3BzKSk7XG4gICAgfTtcbiAgfTtcblxuICByZXR1cm4gZnVuY3Rpb24gKHJlbmRlclByb3BzKSB7XG4gICAgcmV0dXJuIHdpdGhDb250ZXh0LnJlZHVjZVJpZ2h0KGZ1bmN0aW9uIChwcmV2aW91cywgcmVuZGVyUm91dGVyQ29udGV4dCkge1xuICAgICAgcmV0dXJuIHJlbmRlclJvdXRlckNvbnRleHQocHJldmlvdXMsIHJlbmRlclByb3BzKTtcbiAgICB9LCBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudChfUm91dGVyQ29udGV4dDIuZGVmYXVsdCwgX2V4dGVuZHMoe30sIHJlbmRlclByb3BzLCB7XG4gICAgICBjcmVhdGVFbGVtZW50OiBtYWtlQ3JlYXRlRWxlbWVudChyZW5kZXJQcm9wcy5jcmVhdGVFbGVtZW50KVxuICAgIH0pKSk7XG4gIH07XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfY3JlYXRlQnJvd3Nlckhpc3RvcnkgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi9jcmVhdGVCcm93c2VySGlzdG9yeScpO1xuXG52YXIgX2NyZWF0ZUJyb3dzZXJIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZUJyb3dzZXJIaXN0b3J5KTtcblxudmFyIF9jcmVhdGVSb3V0ZXJIaXN0b3J5ID0gcmVxdWlyZSgnLi9jcmVhdGVSb3V0ZXJIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlUm91dGVySGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVSb3V0ZXJIaXN0b3J5KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZXhwb3J0cy5kZWZhdWx0ID0gKDAsIF9jcmVhdGVSb3V0ZXJIaXN0b3J5Mi5kZWZhdWx0KShfY3JlYXRlQnJvd3Nlckhpc3RvcnkyLmRlZmF1bHQpO1xubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbmZ1bmN0aW9uIHJvdXRlUGFyYW1zQ2hhbmdlZChyb3V0ZSwgcHJldlN0YXRlLCBuZXh0U3RhdGUpIHtcbiAgaWYgKCFyb3V0ZS5wYXRoKSByZXR1cm4gZmFsc2U7XG5cbiAgdmFyIHBhcmFtTmFtZXMgPSAoMCwgX1BhdHRlcm5VdGlscy5nZXRQYXJhbU5hbWVzKShyb3V0ZS5wYXRoKTtcblxuICByZXR1cm4gcGFyYW1OYW1lcy5zb21lKGZ1bmN0aW9uIChwYXJhbU5hbWUpIHtcbiAgICByZXR1cm4gcHJldlN0YXRlLnBhcmFtc1twYXJhbU5hbWVdICE9PSBuZXh0U3RhdGUucGFyYW1zW3BhcmFtTmFtZV07XG4gIH0pO1xufVxuXG4vKipcbiAqIFJldHVybnMgYW4gb2JqZWN0IG9mIHsgbGVhdmVSb3V0ZXMsIGNoYW5nZVJvdXRlcywgZW50ZXJSb3V0ZXMgfSBkZXRlcm1pbmVkIGJ5XG4gKiB0aGUgY2hhbmdlIGZyb20gcHJldlN0YXRlIHRvIG5leHRTdGF0ZS4gV2UgbGVhdmUgcm91dGVzIGlmIGVpdGhlclxuICogMSkgdGhleSBhcmUgbm90IGluIHRoZSBuZXh0IHN0YXRlIG9yIDIpIHRoZXkgYXJlIGluIHRoZSBuZXh0IHN0YXRlXG4gKiBidXQgdGhlaXIgcGFyYW1zIGhhdmUgY2hhbmdlZCAoaS5lLiAvdXNlcnMvMTIzID0+IC91c2Vycy80NTYpLlxuICpcbiAqIGxlYXZlUm91dGVzIGFyZSBvcmRlcmVkIHN0YXJ0aW5nIGF0IHRoZSBsZWFmIHJvdXRlIG9mIHRoZSB0cmVlXG4gKiB3ZSdyZSBsZWF2aW5nIHVwIHRvIHRoZSBjb21tb24gcGFyZW50IHJvdXRlLiBlbnRlclJvdXRlcyBhcmUgb3JkZXJlZFxuICogZnJvbSB0aGUgdG9wIG9mIHRoZSB0cmVlIHdlJ3JlIGVudGVyaW5nIGRvd24gdG8gdGhlIGxlYWYgcm91dGUuXG4gKlxuICogY2hhbmdlUm91dGVzIGFyZSBhbnkgcm91dGVzIHRoYXQgZGlkbid0IGxlYXZlIG9yIGVudGVyIGR1cmluZ1xuICogdGhlIHRyYW5zaXRpb24uXG4gKi9cbmZ1bmN0aW9uIGNvbXB1dGVDaGFuZ2VkUm91dGVzKHByZXZTdGF0ZSwgbmV4dFN0YXRlKSB7XG4gIHZhciBwcmV2Um91dGVzID0gcHJldlN0YXRlICYmIHByZXZTdGF0ZS5yb3V0ZXM7XG4gIHZhciBuZXh0Um91dGVzID0gbmV4dFN0YXRlLnJvdXRlcztcblxuICB2YXIgbGVhdmVSb3V0ZXMgPSB2b2lkIDAsXG4gICAgICBjaGFuZ2VSb3V0ZXMgPSB2b2lkIDAsXG4gICAgICBlbnRlclJvdXRlcyA9IHZvaWQgMDtcbiAgaWYgKHByZXZSb3V0ZXMpIHtcbiAgICAoZnVuY3Rpb24gKCkge1xuICAgICAgdmFyIHBhcmVudElzTGVhdmluZyA9IGZhbHNlO1xuICAgICAgbGVhdmVSb3V0ZXMgPSBwcmV2Um91dGVzLmZpbHRlcihmdW5jdGlvbiAocm91dGUpIHtcbiAgICAgICAgaWYgKHBhcmVudElzTGVhdmluZykge1xuICAgICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHZhciBpc0xlYXZpbmcgPSBuZXh0Um91dGVzLmluZGV4T2Yocm91dGUpID09PSAtMSB8fCByb3V0ZVBhcmFtc0NoYW5nZWQocm91dGUsIHByZXZTdGF0ZSwgbmV4dFN0YXRlKTtcbiAgICAgICAgICBpZiAoaXNMZWF2aW5nKSBwYXJlbnRJc0xlYXZpbmcgPSB0cnVlO1xuICAgICAgICAgIHJldHVybiBpc0xlYXZpbmc7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuXG4gICAgICAvLyBvbkxlYXZlIGhvb2tzIHN0YXJ0IGF0IHRoZSBsZWFmIHJvdXRlLlxuICAgICAgbGVhdmVSb3V0ZXMucmV2ZXJzZSgpO1xuXG4gICAgICBlbnRlclJvdXRlcyA9IFtdO1xuICAgICAgY2hhbmdlUm91dGVzID0gW107XG5cbiAgICAgIG5leHRSb3V0ZXMuZm9yRWFjaChmdW5jdGlvbiAocm91dGUpIHtcbiAgICAgICAgdmFyIGlzTmV3ID0gcHJldlJvdXRlcy5pbmRleE9mKHJvdXRlKSA9PT0gLTE7XG4gICAgICAgIHZhciBwYXJhbXNDaGFuZ2VkID0gbGVhdmVSb3V0ZXMuaW5kZXhPZihyb3V0ZSkgIT09IC0xO1xuXG4gICAgICAgIGlmIChpc05ldyB8fCBwYXJhbXNDaGFuZ2VkKSBlbnRlclJvdXRlcy5wdXNoKHJvdXRlKTtlbHNlIGNoYW5nZVJvdXRlcy5wdXNoKHJvdXRlKTtcbiAgICAgIH0pO1xuICAgIH0pKCk7XG4gIH0gZWxzZSB7XG4gICAgbGVhdmVSb3V0ZXMgPSBbXTtcbiAgICBjaGFuZ2VSb3V0ZXMgPSBbXTtcbiAgICBlbnRlclJvdXRlcyA9IG5leHRSb3V0ZXM7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIGxlYXZlUm91dGVzOiBsZWF2ZVJvdXRlcyxcbiAgICBjaGFuZ2VSb3V0ZXM6IGNoYW5nZVJvdXRlcyxcbiAgICBlbnRlclJvdXRlczogZW50ZXJSb3V0ZXNcbiAgfTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gY29tcHV0ZUNoYW5nZWRSb3V0ZXM7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmRlZmF1bHQgPSBjcmVhdGVNZW1vcnlIaXN0b3J5O1xuXG52YXIgX3VzZVF1ZXJpZXMgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi91c2VRdWVyaWVzJyk7XG5cbnZhciBfdXNlUXVlcmllczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF91c2VRdWVyaWVzKTtcblxudmFyIF91c2VCYXNlbmFtZSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL3VzZUJhc2VuYW1lJyk7XG5cbnZhciBfdXNlQmFzZW5hbWUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlQmFzZW5hbWUpO1xuXG52YXIgX2NyZWF0ZU1lbW9yeUhpc3RvcnkgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi9jcmVhdGVNZW1vcnlIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlTWVtb3J5SGlzdG9yeTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVNZW1vcnlIaXN0b3J5KTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gY3JlYXRlTWVtb3J5SGlzdG9yeShvcHRpb25zKSB7XG4gIC8vIHNpZ25hdHVyZXMgYW5kIHR5cGUgY2hlY2tpbmcgZGlmZmVyIGJldHdlZW4gYHVzZVJvdXRlc2AgYW5kXG4gIC8vIGBjcmVhdGVNZW1vcnlIaXN0b3J5YCwgaGF2ZSB0byBjcmVhdGUgYG1lbW9yeUhpc3RvcnlgIGZpcnN0IGJlY2F1c2VcbiAgLy8gYHVzZVF1ZXJpZXNgIGRvZXNuJ3QgdW5kZXJzdGFuZCB0aGUgc2lnbmF0dXJlXG4gIHZhciBtZW1vcnlIaXN0b3J5ID0gKDAsIF9jcmVhdGVNZW1vcnlIaXN0b3J5Mi5kZWZhdWx0KShvcHRpb25zKTtcbiAgdmFyIGNyZWF0ZUhpc3RvcnkgPSBmdW5jdGlvbiBjcmVhdGVIaXN0b3J5KCkge1xuICAgIHJldHVybiBtZW1vcnlIaXN0b3J5O1xuICB9O1xuICB2YXIgaGlzdG9yeSA9ICgwLCBfdXNlUXVlcmllczIuZGVmYXVsdCkoKDAsIF91c2VCYXNlbmFtZTIuZGVmYXVsdCkoY3JlYXRlSGlzdG9yeSkpKG9wdGlvbnMpO1xuICBoaXN0b3J5Ll9fdjJfY29tcGF0aWJsZV9fID0gdHJ1ZTtcbiAgcmV0dXJuIGhpc3Rvcnk7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGZ1bmN0aW9uIChjcmVhdGVIaXN0b3J5KSB7XG4gIHZhciBoaXN0b3J5ID0gdm9pZCAwO1xuICBpZiAoY2FuVXNlRE9NKSBoaXN0b3J5ID0gKDAsIF91c2VSb3V0ZXJIaXN0b3J5Mi5kZWZhdWx0KShjcmVhdGVIaXN0b3J5KSgpO1xuICByZXR1cm4gaGlzdG9yeTtcbn07XG5cbnZhciBfdXNlUm91dGVySGlzdG9yeSA9IHJlcXVpcmUoJy4vdXNlUm91dGVySGlzdG9yeScpO1xuXG52YXIgX3VzZVJvdXRlckhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUm91dGVySGlzdG9yeSk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbnZhciBjYW5Vc2VET00gPSAhISh0eXBlb2Ygd2luZG93ICE9PSAndW5kZWZpbmVkJyAmJiB3aW5kb3cuZG9jdW1lbnQgJiYgd2luZG93LmRvY3VtZW50LmNyZWF0ZUVsZW1lbnQpO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGNyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX0FjdGlvbnMgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi9BY3Rpb25zJyk7XG5cbnZhciBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMyID0gcmVxdWlyZSgnLi9jb21wdXRlQ2hhbmdlZFJvdXRlcycpO1xuXG52YXIgX2NvbXB1dGVDaGFuZ2VkUm91dGVzMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NvbXB1dGVDaGFuZ2VkUm91dGVzMik7XG5cbnZhciBfVHJhbnNpdGlvblV0aWxzID0gcmVxdWlyZSgnLi9UcmFuc2l0aW9uVXRpbHMnKTtcblxudmFyIF9pc0FjdGl2ZTIgPSByZXF1aXJlKCcuL2lzQWN0aXZlJyk7XG5cbnZhciBfaXNBY3RpdmUzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaXNBY3RpdmUyKTtcblxudmFyIF9nZXRDb21wb25lbnRzID0gcmVxdWlyZSgnLi9nZXRDb21wb25lbnRzJyk7XG5cbnZhciBfZ2V0Q29tcG9uZW50czIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9nZXRDb21wb25lbnRzKTtcblxudmFyIF9tYXRjaFJvdXRlcyA9IHJlcXVpcmUoJy4vbWF0Y2hSb3V0ZXMnKTtcblxudmFyIF9tYXRjaFJvdXRlczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9tYXRjaFJvdXRlcyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIGhhc0FueVByb3BlcnRpZXMob2JqZWN0KSB7XG4gIGZvciAodmFyIHAgaW4gb2JqZWN0KSB7XG4gICAgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmplY3QsIHApKSByZXR1cm4gdHJ1ZTtcbiAgfXJldHVybiBmYWxzZTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIoaGlzdG9yeSwgcm91dGVzKSB7XG4gIHZhciBzdGF0ZSA9IHt9O1xuXG4gIC8vIFNpZ25hdHVyZSBzaG91bGQgYmUgKGxvY2F0aW9uLCBpbmRleE9ubHkpLCBidXQgbmVlZHMgdG8gc3VwcG9ydCAocGF0aCxcbiAgLy8gcXVlcnksIGluZGV4T25seSlcbiAgZnVuY3Rpb24gaXNBY3RpdmUobG9jYXRpb24pIHtcbiAgICB2YXIgaW5kZXhPbmx5T3JEZXByZWNhdGVkUXVlcnkgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDEgfHwgYXJndW1lbnRzWzFdID09PSB1bmRlZmluZWQgPyBmYWxzZSA6IGFyZ3VtZW50c1sxXTtcbiAgICB2YXIgZGVwcmVjYXRlZEluZGV4T25seSA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMiB8fCBhcmd1bWVudHNbMl0gPT09IHVuZGVmaW5lZCA/IG51bGwgOiBhcmd1bWVudHNbMl07XG5cbiAgICB2YXIgaW5kZXhPbmx5ID0gdm9pZCAwO1xuICAgIGlmIChpbmRleE9ubHlPckRlcHJlY2F0ZWRRdWVyeSAmJiBpbmRleE9ubHlPckRlcHJlY2F0ZWRRdWVyeSAhPT0gdHJ1ZSB8fCBkZXByZWNhdGVkSW5kZXhPbmx5ICE9PSBudWxsKSB7XG4gICAgICBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2Bpc0FjdGl2ZShwYXRobmFtZSwgcXVlcnksIGluZGV4T25seSkgaXMgZGVwcmVjYXRlZDsgdXNlIGBpc0FjdGl2ZShsb2NhdGlvbiwgaW5kZXhPbmx5KWAgd2l0aCBhIGxvY2F0aW9uIGRlc2NyaXB0b3IgaW5zdGVhZC4gaHR0cDovL3RpbnkuY2Mvcm91dGVyLWlzQWN0aXZlZGVwcmVjYXRlZCcpIDogdm9pZCAwO1xuICAgICAgbG9jYXRpb24gPSB7IHBhdGhuYW1lOiBsb2NhdGlvbiwgcXVlcnk6IGluZGV4T25seU9yRGVwcmVjYXRlZFF1ZXJ5IH07XG4gICAgICBpbmRleE9ubHkgPSBkZXByZWNhdGVkSW5kZXhPbmx5IHx8IGZhbHNlO1xuICAgIH0gZWxzZSB7XG4gICAgICBsb2NhdGlvbiA9IGhpc3RvcnkuY3JlYXRlTG9jYXRpb24obG9jYXRpb24pO1xuICAgICAgaW5kZXhPbmx5ID0gaW5kZXhPbmx5T3JEZXByZWNhdGVkUXVlcnk7XG4gICAgfVxuXG4gICAgcmV0dXJuICgwLCBfaXNBY3RpdmUzLmRlZmF1bHQpKGxvY2F0aW9uLCBpbmRleE9ubHksIHN0YXRlLmxvY2F0aW9uLCBzdGF0ZS5yb3V0ZXMsIHN0YXRlLnBhcmFtcyk7XG4gIH1cblxuICBmdW5jdGlvbiBjcmVhdGVMb2NhdGlvbkZyb21SZWRpcmVjdEluZm8obG9jYXRpb24pIHtcbiAgICByZXR1cm4gaGlzdG9yeS5jcmVhdGVMb2NhdGlvbihsb2NhdGlvbiwgX0FjdGlvbnMuUkVQTEFDRSk7XG4gIH1cblxuICB2YXIgcGFydGlhbE5leHRTdGF0ZSA9IHZvaWQgMDtcblxuICBmdW5jdGlvbiBtYXRjaChsb2NhdGlvbiwgY2FsbGJhY2spIHtcbiAgICBpZiAocGFydGlhbE5leHRTdGF0ZSAmJiBwYXJ0aWFsTmV4dFN0YXRlLmxvY2F0aW9uID09PSBsb2NhdGlvbikge1xuICAgICAgLy8gQ29udGludWUgZnJvbSB3aGVyZSB3ZSBsZWZ0IG9mZi5cbiAgICAgIGZpbmlzaE1hdGNoKHBhcnRpYWxOZXh0U3RhdGUsIGNhbGxiYWNrKTtcbiAgICB9IGVsc2Uge1xuICAgICAgKDAsIF9tYXRjaFJvdXRlczIuZGVmYXVsdCkocm91dGVzLCBsb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCBuZXh0U3RhdGUpIHtcbiAgICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgICAgY2FsbGJhY2soZXJyb3IpO1xuICAgICAgICB9IGVsc2UgaWYgKG5leHRTdGF0ZSkge1xuICAgICAgICAgIGZpbmlzaE1hdGNoKF9leHRlbmRzKHt9LCBuZXh0U3RhdGUsIHsgbG9jYXRpb246IGxvY2F0aW9uIH0pLCBjYWxsYmFjayk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgY2FsbGJhY2soKTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgZnVuY3Rpb24gZmluaXNoTWF0Y2gobmV4dFN0YXRlLCBjYWxsYmFjaykge1xuICAgIHZhciBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMgPSAoMCwgX2NvbXB1dGVDaGFuZ2VkUm91dGVzMy5kZWZhdWx0KShzdGF0ZSwgbmV4dFN0YXRlKTtcblxuICAgIHZhciBsZWF2ZVJvdXRlcyA9IF9jb21wdXRlQ2hhbmdlZFJvdXRlcy5sZWF2ZVJvdXRlcztcbiAgICB2YXIgY2hhbmdlUm91dGVzID0gX2NvbXB1dGVDaGFuZ2VkUm91dGVzLmNoYW5nZVJvdXRlcztcbiAgICB2YXIgZW50ZXJSb3V0ZXMgPSBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMuZW50ZXJSb3V0ZXM7XG5cblxuICAgICgwLCBfVHJhbnNpdGlvblV0aWxzLnJ1bkxlYXZlSG9va3MpKGxlYXZlUm91dGVzLCBzdGF0ZSk7XG5cbiAgICAvLyBUZWFyIGRvd24gY29uZmlybWF0aW9uIGhvb2tzIGZvciBsZWZ0IHJvdXRlc1xuICAgIGxlYXZlUm91dGVzLmZpbHRlcihmdW5jdGlvbiAocm91dGUpIHtcbiAgICAgIHJldHVybiBlbnRlclJvdXRlcy5pbmRleE9mKHJvdXRlKSA9PT0gLTE7XG4gICAgfSkuZm9yRWFjaChyZW1vdmVMaXN0ZW5CZWZvcmVIb29rc0ZvclJvdXRlKTtcblxuICAgIC8vIGNoYW5nZSBhbmQgZW50ZXIgaG9va3MgYXJlIHJ1biBpbiBzZXJpZXNcbiAgICAoMCwgX1RyYW5zaXRpb25VdGlscy5ydW5DaGFuZ2VIb29rcykoY2hhbmdlUm91dGVzLCBzdGF0ZSwgbmV4dFN0YXRlLCBmdW5jdGlvbiAoZXJyb3IsIHJlZGlyZWN0SW5mbykge1xuICAgICAgaWYgKGVycm9yIHx8IHJlZGlyZWN0SW5mbykgcmV0dXJuIGhhbmRsZUVycm9yT3JSZWRpcmVjdChlcnJvciwgcmVkaXJlY3RJbmZvKTtcblxuICAgICAgKDAsIF9UcmFuc2l0aW9uVXRpbHMucnVuRW50ZXJIb29rcykoZW50ZXJSb3V0ZXMsIG5leHRTdGF0ZSwgZmluaXNoRW50ZXJIb29rcyk7XG4gICAgfSk7XG5cbiAgICBmdW5jdGlvbiBmaW5pc2hFbnRlckhvb2tzKGVycm9yLCByZWRpcmVjdEluZm8pIHtcbiAgICAgIGlmIChlcnJvciB8fCByZWRpcmVjdEluZm8pIHJldHVybiBoYW5kbGVFcnJvck9yUmVkaXJlY3QoZXJyb3IsIHJlZGlyZWN0SW5mbyk7XG5cbiAgICAgIC8vIFRPRE86IEZldGNoIGNvbXBvbmVudHMgYWZ0ZXIgc3RhdGUgaXMgdXBkYXRlZC5cbiAgICAgICgwLCBfZ2V0Q29tcG9uZW50czIuZGVmYXVsdCkobmV4dFN0YXRlLCBmdW5jdGlvbiAoZXJyb3IsIGNvbXBvbmVudHMpIHtcbiAgICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgICAgY2FsbGJhY2soZXJyb3IpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIC8vIFRPRE86IE1ha2UgbWF0Y2ggYSBwdXJlIGZ1bmN0aW9uIGFuZCBoYXZlIHNvbWUgb3RoZXIgQVBJXG4gICAgICAgICAgLy8gZm9yIFwibWF0Y2ggYW5kIHVwZGF0ZSBzdGF0ZVwiLlxuICAgICAgICAgIGNhbGxiYWNrKG51bGwsIG51bGwsIHN0YXRlID0gX2V4dGVuZHMoe30sIG5leHRTdGF0ZSwgeyBjb21wb25lbnRzOiBjb21wb25lbnRzIH0pKTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gaGFuZGxlRXJyb3JPclJlZGlyZWN0KGVycm9yLCByZWRpcmVjdEluZm8pIHtcbiAgICAgIGlmIChlcnJvcikgY2FsbGJhY2soZXJyb3IpO2Vsc2UgY2FsbGJhY2sobnVsbCwgY3JlYXRlTG9jYXRpb25Gcm9tUmVkaXJlY3RJbmZvKHJlZGlyZWN0SW5mbykpO1xuICAgIH1cbiAgfVxuXG4gIHZhciBSb3V0ZUd1aWQgPSAxO1xuXG4gIGZ1bmN0aW9uIGdldFJvdXRlSUQocm91dGUpIHtcbiAgICB2YXIgY3JlYXRlID0gYXJndW1lbnRzLmxlbmd0aCA8PSAxIHx8IGFyZ3VtZW50c1sxXSA9PT0gdW5kZWZpbmVkID8gdHJ1ZSA6IGFyZ3VtZW50c1sxXTtcblxuICAgIHJldHVybiByb3V0ZS5fX2lkX18gfHwgY3JlYXRlICYmIChyb3V0ZS5fX2lkX18gPSBSb3V0ZUd1aWQrKyk7XG4gIH1cblxuICB2YXIgUm91dGVIb29rcyA9IE9iamVjdC5jcmVhdGUobnVsbCk7XG5cbiAgZnVuY3Rpb24gZ2V0Um91dGVIb29rc0ZvclJvdXRlcyhyb3V0ZXMpIHtcbiAgICByZXR1cm4gcm91dGVzLnJlZHVjZShmdW5jdGlvbiAoaG9va3MsIHJvdXRlKSB7XG4gICAgICBob29rcy5wdXNoLmFwcGx5KGhvb2tzLCBSb3V0ZUhvb2tzW2dldFJvdXRlSUQocm91dGUpXSk7XG4gICAgICByZXR1cm4gaG9va3M7XG4gICAgfSwgW10pO1xuICB9XG5cbiAgZnVuY3Rpb24gdHJhbnNpdGlvbkhvb2sobG9jYXRpb24sIGNhbGxiYWNrKSB7XG4gICAgKDAsIF9tYXRjaFJvdXRlczIuZGVmYXVsdCkocm91dGVzLCBsb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCBuZXh0U3RhdGUpIHtcbiAgICAgIGlmIChuZXh0U3RhdGUgPT0gbnVsbCkge1xuICAgICAgICAvLyBUT0RPOiBXZSBkaWRuJ3QgYWN0dWFsbHkgbWF0Y2ggYW55dGhpbmcsIGJ1dCBoYW5nXG4gICAgICAgIC8vIG9udG8gZXJyb3IvbmV4dFN0YXRlIHNvIHdlIGRvbid0IGhhdmUgdG8gbWF0Y2hSb3V0ZXNcbiAgICAgICAgLy8gYWdhaW4gaW4gdGhlIGxpc3RlbiBjYWxsYmFjay5cbiAgICAgICAgY2FsbGJhY2soKTtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuXG4gICAgICAvLyBDYWNoZSBzb21lIHN0YXRlIGhlcmUgc28gd2UgZG9uJ3QgaGF2ZSB0b1xuICAgICAgLy8gbWF0Y2hSb3V0ZXMoKSBhZ2FpbiBpbiB0aGUgbGlzdGVuIGNhbGxiYWNrLlxuICAgICAgcGFydGlhbE5leHRTdGF0ZSA9IF9leHRlbmRzKHt9LCBuZXh0U3RhdGUsIHsgbG9jYXRpb246IGxvY2F0aW9uIH0pO1xuXG4gICAgICB2YXIgaG9va3MgPSBnZXRSb3V0ZUhvb2tzRm9yUm91dGVzKCgwLCBfY29tcHV0ZUNoYW5nZWRSb3V0ZXMzLmRlZmF1bHQpKHN0YXRlLCBwYXJ0aWFsTmV4dFN0YXRlKS5sZWF2ZVJvdXRlcyk7XG5cbiAgICAgIHZhciByZXN1bHQgPSB2b2lkIDA7XG4gICAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gaG9va3MubGVuZ3RoOyByZXN1bHQgPT0gbnVsbCAmJiBpIDwgbGVuOyArK2kpIHtcbiAgICAgICAgLy8gUGFzc2luZyB0aGUgbG9jYXRpb24gYXJnIGhlcmUgaW5kaWNhdGVzIHRvXG4gICAgICAgIC8vIHRoZSB1c2VyIHRoYXQgdGhpcyBpcyBhIHRyYW5zaXRpb24gaG9vay5cbiAgICAgICAgcmVzdWx0ID0gaG9va3NbaV0obG9jYXRpb24pO1xuICAgICAgfVxuXG4gICAgICBjYWxsYmFjayhyZXN1bHQpO1xuICAgIH0pO1xuICB9XG5cbiAgLyogaXN0YW5idWwgaWdub3JlIG5leHQ6IHVudGVzdGFibGUgd2l0aCBLYXJtYSAqL1xuICBmdW5jdGlvbiBiZWZvcmVVbmxvYWRIb29rKCkge1xuICAgIC8vIFN5bmNocm9ub3VzbHkgY2hlY2sgdG8gc2VlIGlmIGFueSByb3V0ZSBob29rcyB3YW50XG4gICAgLy8gdG8gcHJldmVudCB0aGUgY3VycmVudCB3aW5kb3cvdGFiIGZyb20gY2xvc2luZy5cbiAgICBpZiAoc3RhdGUucm91dGVzKSB7XG4gICAgICB2YXIgaG9va3MgPSBnZXRSb3V0ZUhvb2tzRm9yUm91dGVzKHN0YXRlLnJvdXRlcyk7XG5cbiAgICAgIHZhciBtZXNzYWdlID0gdm9pZCAwO1xuICAgICAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IGhvb2tzLmxlbmd0aDsgdHlwZW9mIG1lc3NhZ2UgIT09ICdzdHJpbmcnICYmIGkgPCBsZW47ICsraSkge1xuICAgICAgICAvLyBQYXNzaW5nIG5vIGFyZ3MgaW5kaWNhdGVzIHRvIHRoZSB1c2VyIHRoYXQgdGhpcyBpcyBhXG4gICAgICAgIC8vIGJlZm9yZXVubG9hZCBob29rLiBXZSBkb24ndCBrbm93IHRoZSBuZXh0IGxvY2F0aW9uLlxuICAgICAgICBtZXNzYWdlID0gaG9va3NbaV0oKTtcbiAgICAgIH1cblxuICAgICAgcmV0dXJuIG1lc3NhZ2U7XG4gICAgfVxuICB9XG5cbiAgdmFyIHVubGlzdGVuQmVmb3JlID0gdm9pZCAwLFxuICAgICAgdW5saXN0ZW5CZWZvcmVVbmxvYWQgPSB2b2lkIDA7XG5cbiAgZnVuY3Rpb24gcmVtb3ZlTGlzdGVuQmVmb3JlSG9va3NGb3JSb3V0ZShyb3V0ZSkge1xuICAgIHZhciByb3V0ZUlEID0gZ2V0Um91dGVJRChyb3V0ZSwgZmFsc2UpO1xuICAgIGlmICghcm91dGVJRCkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGRlbGV0ZSBSb3V0ZUhvb2tzW3JvdXRlSURdO1xuXG4gICAgaWYgKCFoYXNBbnlQcm9wZXJ0aWVzKFJvdXRlSG9va3MpKSB7XG4gICAgICAvLyB0ZWFyZG93biB0cmFuc2l0aW9uICYgYmVmb3JldW5sb2FkIGhvb2tzXG4gICAgICBpZiAodW5saXN0ZW5CZWZvcmUpIHtcbiAgICAgICAgdW5saXN0ZW5CZWZvcmUoKTtcbiAgICAgICAgdW5saXN0ZW5CZWZvcmUgPSBudWxsO1xuICAgICAgfVxuXG4gICAgICBpZiAodW5saXN0ZW5CZWZvcmVVbmxvYWQpIHtcbiAgICAgICAgdW5saXN0ZW5CZWZvcmVVbmxvYWQoKTtcbiAgICAgICAgdW5saXN0ZW5CZWZvcmVVbmxvYWQgPSBudWxsO1xuICAgICAgfVxuICAgIH1cbiAgfVxuXG4gIC8qKlxuICAgKiBSZWdpc3RlcnMgdGhlIGdpdmVuIGhvb2sgZnVuY3Rpb24gdG8gcnVuIGJlZm9yZSBsZWF2aW5nIHRoZSBnaXZlbiByb3V0ZS5cbiAgICpcbiAgICogRHVyaW5nIGEgbm9ybWFsIHRyYW5zaXRpb24sIHRoZSBob29rIGZ1bmN0aW9uIHJlY2VpdmVzIHRoZSBuZXh0IGxvY2F0aW9uXG4gICAqIGFzIGl0cyBvbmx5IGFyZ3VtZW50IGFuZCBjYW4gcmV0dXJuIGVpdGhlciBhIHByb21wdCBtZXNzYWdlIChzdHJpbmcpIHRvIHNob3cgdGhlIHVzZXIsXG4gICAqIHRvIG1ha2Ugc3VyZSB0aGV5IHdhbnQgdG8gbGVhdmUgdGhlIHBhZ2U7IG9yIGBmYWxzZWAsIHRvIHByZXZlbnQgdGhlIHRyYW5zaXRpb24uXG4gICAqIEFueSBvdGhlciByZXR1cm4gdmFsdWUgd2lsbCBoYXZlIG5vIGVmZmVjdC5cbiAgICpcbiAgICogRHVyaW5nIHRoZSBiZWZvcmV1bmxvYWQgZXZlbnQgKGluIGJyb3dzZXJzKSB0aGUgaG9vayByZWNlaXZlcyBubyBhcmd1bWVudHMuXG4gICAqIEluIHRoaXMgY2FzZSBpdCBtdXN0IHJldHVybiBhIHByb21wdCBtZXNzYWdlIHRvIHByZXZlbnQgdGhlIHRyYW5zaXRpb24uXG4gICAqXG4gICAqIFJldHVybnMgYSBmdW5jdGlvbiB0aGF0IG1heSBiZSB1c2VkIHRvIHVuYmluZCB0aGUgbGlzdGVuZXIuXG4gICAqL1xuICBmdW5jdGlvbiBsaXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUocm91dGUsIGhvb2spIHtcbiAgICAvLyBUT0RPOiBXYXJuIGlmIHRoZXkgcmVnaXN0ZXIgZm9yIGEgcm91dGUgdGhhdCBpc24ndCBjdXJyZW50bHlcbiAgICAvLyBhY3RpdmUuIFRoZXkncmUgcHJvYmFibHkgZG9pbmcgc29tZXRoaW5nIHdyb25nLCBsaWtlIHJlLWNyZWF0aW5nXG4gICAgLy8gcm91dGUgb2JqZWN0cyBvbiBldmVyeSBsb2NhdGlvbiBjaGFuZ2UuXG4gICAgdmFyIHJvdXRlSUQgPSBnZXRSb3V0ZUlEKHJvdXRlKTtcbiAgICB2YXIgaG9va3MgPSBSb3V0ZUhvb2tzW3JvdXRlSURdO1xuXG4gICAgaWYgKCFob29rcykge1xuICAgICAgdmFyIHRoZXJlV2VyZU5vUm91dGVIb29rcyA9ICFoYXNBbnlQcm9wZXJ0aWVzKFJvdXRlSG9va3MpO1xuXG4gICAgICBSb3V0ZUhvb2tzW3JvdXRlSURdID0gW2hvb2tdO1xuXG4gICAgICBpZiAodGhlcmVXZXJlTm9Sb3V0ZUhvb2tzKSB7XG4gICAgICAgIC8vIHNldHVwIHRyYW5zaXRpb24gJiBiZWZvcmV1bmxvYWQgaG9va3NcbiAgICAgICAgdW5saXN0ZW5CZWZvcmUgPSBoaXN0b3J5Lmxpc3RlbkJlZm9yZSh0cmFuc2l0aW9uSG9vayk7XG5cbiAgICAgICAgaWYgKGhpc3RvcnkubGlzdGVuQmVmb3JlVW5sb2FkKSB1bmxpc3RlbkJlZm9yZVVubG9hZCA9IGhpc3RvcnkubGlzdGVuQmVmb3JlVW5sb2FkKGJlZm9yZVVubG9hZEhvb2spO1xuICAgICAgfVxuICAgIH0gZWxzZSB7XG4gICAgICBpZiAoaG9va3MuaW5kZXhPZihob29rKSA9PT0gLTEpIHtcbiAgICAgICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsICdhZGRpbmcgbXVsdGlwbGUgbGVhdmUgaG9va3MgZm9yIHRoZSBzYW1lIHJvdXRlIGlzIGRlcHJlY2F0ZWQ7IG1hbmFnZSBtdWx0aXBsZSBjb25maXJtYXRpb25zIGluIHlvdXIgb3duIGNvZGUgaW5zdGVhZCcpIDogdm9pZCAwO1xuXG4gICAgICAgIGhvb2tzLnB1c2goaG9vayk7XG4gICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICAgIHZhciBob29rcyA9IFJvdXRlSG9va3Nbcm91dGVJRF07XG5cbiAgICAgIGlmIChob29rcykge1xuICAgICAgICB2YXIgbmV3SG9va3MgPSBob29rcy5maWx0ZXIoZnVuY3Rpb24gKGl0ZW0pIHtcbiAgICAgICAgICByZXR1cm4gaXRlbSAhPT0gaG9vaztcbiAgICAgICAgfSk7XG5cbiAgICAgICAgaWYgKG5ld0hvb2tzLmxlbmd0aCA9PT0gMCkge1xuICAgICAgICAgIHJlbW92ZUxpc3RlbkJlZm9yZUhvb2tzRm9yUm91dGUocm91dGUpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIFJvdXRlSG9va3Nbcm91dGVJRF0gPSBuZXdIb29rcztcbiAgICAgICAgfVxuICAgICAgfVxuICAgIH07XG4gIH1cblxuICAvKipcbiAgICogVGhpcyBpcyB0aGUgQVBJIGZvciBzdGF0ZWZ1bCBlbnZpcm9ubWVudHMuIEFzIHRoZSBsb2NhdGlvblxuICAgKiBjaGFuZ2VzLCB3ZSB1cGRhdGUgc3RhdGUgYW5kIGNhbGwgdGhlIGxpc3RlbmVyLiBXZSBjYW4gYWxzb1xuICAgKiBncmFjZWZ1bGx5IGhhbmRsZSBlcnJvcnMgYW5kIHJlZGlyZWN0cy5cbiAgICovXG4gIGZ1bmN0aW9uIGxpc3RlbihsaXN0ZW5lcikge1xuICAgIC8vIFRPRE86IE9ubHkgdXNlIGEgc2luZ2xlIGhpc3RvcnkgbGlzdGVuZXIuIE90aGVyd2lzZSB3ZSdsbFxuICAgIC8vIGVuZCB1cCB3aXRoIG11bHRpcGxlIGNvbmN1cnJlbnQgY2FsbHMgdG8gbWF0Y2guXG4gICAgcmV0dXJuIGhpc3RvcnkubGlzdGVuKGZ1bmN0aW9uIChsb2NhdGlvbikge1xuICAgICAgaWYgKHN0YXRlLmxvY2F0aW9uID09PSBsb2NhdGlvbikge1xuICAgICAgICBsaXN0ZW5lcihudWxsLCBzdGF0ZSk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBtYXRjaChsb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCByZWRpcmVjdExvY2F0aW9uLCBuZXh0U3RhdGUpIHtcbiAgICAgICAgICBpZiAoZXJyb3IpIHtcbiAgICAgICAgICAgIGxpc3RlbmVyKGVycm9yKTtcbiAgICAgICAgICB9IGVsc2UgaWYgKHJlZGlyZWN0TG9jYXRpb24pIHtcbiAgICAgICAgICAgIGhpc3RvcnkudHJhbnNpdGlvblRvKHJlZGlyZWN0TG9jYXRpb24pO1xuICAgICAgICAgIH0gZWxzZSBpZiAobmV4dFN0YXRlKSB7XG4gICAgICAgICAgICBsaXN0ZW5lcihudWxsLCBuZXh0U3RhdGUpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ0xvY2F0aW9uIFwiJXNcIiBkaWQgbm90IG1hdGNoIGFueSByb3V0ZXMnLCBsb2NhdGlvbi5wYXRobmFtZSArIGxvY2F0aW9uLnNlYXJjaCArIGxvY2F0aW9uLmhhc2gpIDogdm9pZCAwO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9XG4gICAgfSk7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIGlzQWN0aXZlOiBpc0FjdGl2ZSxcbiAgICBtYXRjaDogbWF0Y2gsXG4gICAgbGlzdGVuQmVmb3JlTGVhdmluZ1JvdXRlOiBsaXN0ZW5CZWZvcmVMZWF2aW5nUm91dGUsXG4gICAgbGlzdGVuOiBsaXN0ZW5cbiAgfTtcbn1cblxuLy9leHBvcnQgZGVmYXVsdCB1c2VSb3V0ZXNcblxubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5jYW5Vc2VNZW1icmFuZSA9IHVuZGVmaW5lZDtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxudmFyIGNhblVzZU1lbWJyYW5lID0gZXhwb3J0cy5jYW5Vc2VNZW1icmFuZSA9IGZhbHNlO1xuXG4vLyBOby1vcCBieSBkZWZhdWx0LlxudmFyIGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMgPSBmdW5jdGlvbiBkZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzKG9iamVjdCkge1xuICByZXR1cm4gb2JqZWN0O1xufTtcblxuaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicpIHtcbiAgdHJ5IHtcbiAgICBpZiAoT2JqZWN0LmRlZmluZVByb3BlcnR5KHt9LCAneCcsIHtcbiAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICByZXR1cm4gdHJ1ZTtcbiAgICAgIH1cbiAgICB9KS54KSB7XG4gICAgICBleHBvcnRzLmNhblVzZU1lbWJyYW5lID0gY2FuVXNlTWVtYnJhbmUgPSB0cnVlO1xuICAgIH1cbiAgICAvKiBlc2xpbnQtZGlzYWJsZSBuby1lbXB0eSAqL1xuICB9IGNhdGNoIChlKSB7fVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWVtcHR5ICovXG5cbiAgaWYgKGNhblVzZU1lbWJyYW5lKSB7XG4gICAgZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IGZ1bmN0aW9uIGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMob2JqZWN0LCBtZXNzYWdlKSB7XG4gICAgICAvLyBXcmFwIHRoZSBkZXByZWNhdGVkIG9iamVjdCBpbiBhIG1lbWJyYW5lIHRvIHdhcm4gb24gcHJvcGVydHkgYWNjZXNzLlxuICAgICAgdmFyIG1lbWJyYW5lID0ge307XG5cbiAgICAgIHZhciBfbG9vcCA9IGZ1bmN0aW9uIF9sb29wKHByb3ApIHtcbiAgICAgICAgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwob2JqZWN0LCBwcm9wKSkge1xuICAgICAgICAgIHJldHVybiAnY29udGludWUnO1xuICAgICAgICB9XG5cbiAgICAgICAgaWYgKHR5cGVvZiBvYmplY3RbcHJvcF0gPT09ICdmdW5jdGlvbicpIHtcbiAgICAgICAgICAvLyBDYW4ndCB1c2UgZmF0IGFycm93IGhlcmUgYmVjYXVzZSBvZiB1c2Ugb2YgYXJndW1lbnRzIGJlbG93LlxuICAgICAgICAgIG1lbWJyYW5lW3Byb3BdID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAgICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsIG1lc3NhZ2UpIDogdm9pZCAwO1xuICAgICAgICAgICAgcmV0dXJuIG9iamVjdFtwcm9wXS5hcHBseShvYmplY3QsIGFyZ3VtZW50cyk7XG4gICAgICAgICAgfTtcbiAgICAgICAgICByZXR1cm4gJ2NvbnRpbnVlJztcbiAgICAgICAgfVxuXG4gICAgICAgIC8vIFRoZXNlIHByb3BlcnRpZXMgYXJlIG5vbi1lbnVtZXJhYmxlIHRvIHByZXZlbnQgUmVhY3QgZGV2IHRvb2xzIGZyb21cbiAgICAgICAgLy8gc2VlaW5nIHRoZW0gYW5kIGNhdXNpbmcgc3B1cmlvdXMgd2FybmluZ3Mgd2hlbiBhY2Nlc3NpbmcgdGhlbS4gSW5cbiAgICAgICAgLy8gcHJpbmNpcGxlIHRoaXMgY291bGQgYmUgZG9uZSB3aXRoIGEgcHJveHksIGJ1dCBzdXBwb3J0IGZvciB0aGVcbiAgICAgICAgLy8gb3duS2V5cyB0cmFwIG9uIHByb3hpZXMgaXMgbm90IHVuaXZlcnNhbCwgZXZlbiBhbW9uZyBicm93c2VycyB0aGF0XG4gICAgICAgIC8vIG90aGVyd2lzZSBzdXBwb3J0IHByb3hpZXMuXG4gICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eShtZW1icmFuZSwgcHJvcCwge1xuICAgICAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICAgICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoZmFsc2UsIG1lc3NhZ2UpIDogdm9pZCAwO1xuICAgICAgICAgICAgcmV0dXJuIG9iamVjdFtwcm9wXTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuICAgICAgfTtcblxuICAgICAgZm9yICh2YXIgcHJvcCBpbiBvYmplY3QpIHtcbiAgICAgICAgdmFyIF9yZXQgPSBfbG9vcChwcm9wKTtcblxuICAgICAgICBpZiAoX3JldCA9PT0gJ2NvbnRpbnVlJykgY29udGludWU7XG4gICAgICB9XG5cbiAgICAgIHJldHVybiBtZW1icmFuZTtcbiAgICB9O1xuICB9XG59XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXM7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX0FzeW5jVXRpbHMgPSByZXF1aXJlKCcuL0FzeW5jVXRpbHMnKTtcblxudmFyIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24gPSByZXF1aXJlKCcuL21ha2VTdGF0ZVdpdGhMb2NhdGlvbicpO1xuXG52YXIgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9tYWtlU3RhdGVXaXRoTG9jYXRpb24pO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBnZXRDb21wb25lbnRzRm9yUm91dGUobmV4dFN0YXRlLCByb3V0ZSwgY2FsbGJhY2spIHtcbiAgaWYgKHJvdXRlLmNvbXBvbmVudCB8fCByb3V0ZS5jb21wb25lbnRzKSB7XG4gICAgY2FsbGJhY2sobnVsbCwgcm91dGUuY29tcG9uZW50IHx8IHJvdXRlLmNvbXBvbmVudHMpO1xuICAgIHJldHVybjtcbiAgfVxuXG4gIHZhciBnZXRDb21wb25lbnQgPSByb3V0ZS5nZXRDb21wb25lbnQgfHwgcm91dGUuZ2V0Q29tcG9uZW50cztcbiAgaWYgKCFnZXRDb21wb25lbnQpIHtcbiAgICBjYWxsYmFjaygpO1xuICAgIHJldHVybjtcbiAgfVxuXG4gIHZhciBsb2NhdGlvbiA9IG5leHRTdGF0ZS5sb2NhdGlvbjtcblxuICB2YXIgbmV4dFN0YXRlV2l0aExvY2F0aW9uID0gKDAsIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24yLmRlZmF1bHQpKG5leHRTdGF0ZSwgbG9jYXRpb24pO1xuXG4gIGdldENvbXBvbmVudC5jYWxsKHJvdXRlLCBuZXh0U3RhdGVXaXRoTG9jYXRpb24sIGNhbGxiYWNrKTtcbn1cblxuLyoqXG4gKiBBc3luY2hyb25vdXNseSBmZXRjaGVzIGFsbCBjb21wb25lbnRzIG5lZWRlZCBmb3IgdGhlIGdpdmVuIHJvdXRlclxuICogc3RhdGUgYW5kIGNhbGxzIGNhbGxiYWNrKGVycm9yLCBjb21wb25lbnRzKSB3aGVuIGZpbmlzaGVkLlxuICpcbiAqIE5vdGU6IFRoaXMgb3BlcmF0aW9uIG1heSBmaW5pc2ggc3luY2hyb25vdXNseSBpZiBubyByb3V0ZXMgaGF2ZSBhblxuICogYXN5bmNocm9ub3VzIGdldENvbXBvbmVudHMgbWV0aG9kLlxuICovXG5mdW5jdGlvbiBnZXRDb21wb25lbnRzKG5leHRTdGF0ZSwgY2FsbGJhY2spIHtcbiAgKDAsIF9Bc3luY1V0aWxzLm1hcEFzeW5jKShuZXh0U3RhdGUucm91dGVzLCBmdW5jdGlvbiAocm91dGUsIGluZGV4LCBjYWxsYmFjaykge1xuICAgIGdldENvbXBvbmVudHNGb3JSb3V0ZShuZXh0U3RhdGUsIHJvdXRlLCBjYWxsYmFjayk7XG4gIH0sIGNhbGxiYWNrKTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gZ2V0Q29tcG9uZW50cztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9QYXR0ZXJuVXRpbHMgPSByZXF1aXJlKCcuL1BhdHRlcm5VdGlscycpO1xuXG4vKipcbiAqIEV4dHJhY3RzIGFuIG9iamVjdCBvZiBwYXJhbXMgdGhlIGdpdmVuIHJvdXRlIGNhcmVzIGFib3V0IGZyb21cbiAqIHRoZSBnaXZlbiBwYXJhbXMgb2JqZWN0LlxuICovXG5mdW5jdGlvbiBnZXRSb3V0ZVBhcmFtcyhyb3V0ZSwgcGFyYW1zKSB7XG4gIHZhciByb3V0ZVBhcmFtcyA9IHt9O1xuXG4gIGlmICghcm91dGUucGF0aCkgcmV0dXJuIHJvdXRlUGFyYW1zO1xuXG4gICgwLCBfUGF0dGVyblV0aWxzLmdldFBhcmFtTmFtZXMpKHJvdXRlLnBhdGgpLmZvckVhY2goZnVuY3Rpb24gKHApIHtcbiAgICBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHBhcmFtcywgcCkpIHtcbiAgICAgIHJvdXRlUGFyYW1zW3BdID0gcGFyYW1zW3BdO1xuICAgIH1cbiAgfSk7XG5cbiAgcmV0dXJuIHJvdXRlUGFyYW1zO1xufVxuXG5leHBvcnRzLmRlZmF1bHQgPSBnZXRSb3V0ZVBhcmFtcztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9jcmVhdGVIYXNoSGlzdG9yeSA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL2NyZWF0ZUhhc2hIaXN0b3J5Jyk7XG5cbnZhciBfY3JlYXRlSGFzaEhpc3RvcnkyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlSGFzaEhpc3RvcnkpO1xuXG52YXIgX2NyZWF0ZVJvdXRlckhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZVJvdXRlckhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVSb3V0ZXJIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVJvdXRlckhpc3RvcnkpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5leHBvcnRzLmRlZmF1bHQgPSAoMCwgX2NyZWF0ZVJvdXRlckhpc3RvcnkyLmRlZmF1bHQpKF9jcmVhdGVIYXNoSGlzdG9yeTIuZGVmYXVsdCk7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmNyZWF0ZU1lbW9yeUhpc3RvcnkgPSBleHBvcnRzLmhhc2hIaXN0b3J5ID0gZXhwb3J0cy5icm93c2VySGlzdG9yeSA9IGV4cG9ydHMuYXBwbHlSb3V0ZXJNaWRkbGV3YXJlID0gZXhwb3J0cy5mb3JtYXRQYXR0ZXJuID0gZXhwb3J0cy51c2VSb3V0ZXJIaXN0b3J5ID0gZXhwb3J0cy5tYXRjaCA9IGV4cG9ydHMucm91dGVyU2hhcGUgPSBleHBvcnRzLmxvY2F0aW9uU2hhcGUgPSBleHBvcnRzLlByb3BUeXBlcyA9IGV4cG9ydHMuUm91dGluZ0NvbnRleHQgPSBleHBvcnRzLlJvdXRlckNvbnRleHQgPSBleHBvcnRzLmNyZWF0ZVJvdXRlcyA9IGV4cG9ydHMudXNlUm91dGVzID0gZXhwb3J0cy5Sb3V0ZUNvbnRleHQgPSBleHBvcnRzLkxpZmVjeWNsZSA9IGV4cG9ydHMuSGlzdG9yeSA9IGV4cG9ydHMuUm91dGUgPSBleHBvcnRzLlJlZGlyZWN0ID0gZXhwb3J0cy5JbmRleFJvdXRlID0gZXhwb3J0cy5JbmRleFJlZGlyZWN0ID0gZXhwb3J0cy53aXRoUm91dGVyID0gZXhwb3J0cy5JbmRleExpbmsgPSBleHBvcnRzLkxpbmsgPSBleHBvcnRzLlJvdXRlciA9IHVuZGVmaW5lZDtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbk9iamVjdC5kZWZpbmVQcm9wZXJ0eShleHBvcnRzLCAnY3JlYXRlUm91dGVzJywge1xuICBlbnVtZXJhYmxlOiB0cnVlLFxuICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICByZXR1cm4gX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVzO1xuICB9XG59KTtcblxudmFyIF9Qcm9wVHlwZXMyID0gcmVxdWlyZSgnLi9Qcm9wVHlwZXMnKTtcblxuT2JqZWN0LmRlZmluZVByb3BlcnR5KGV4cG9ydHMsICdsb2NhdGlvblNoYXBlJywge1xuICBlbnVtZXJhYmxlOiB0cnVlLFxuICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICByZXR1cm4gX1Byb3BUeXBlczIubG9jYXRpb25TaGFwZTtcbiAgfVxufSk7XG5PYmplY3QuZGVmaW5lUHJvcGVydHkoZXhwb3J0cywgJ3JvdXRlclNoYXBlJywge1xuICBlbnVtZXJhYmxlOiB0cnVlLFxuICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICByZXR1cm4gX1Byb3BUeXBlczIucm91dGVyU2hhcGU7XG4gIH1cbn0pO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbk9iamVjdC5kZWZpbmVQcm9wZXJ0eShleHBvcnRzLCAnZm9ybWF0UGF0dGVybicsIHtcbiAgZW51bWVyYWJsZTogdHJ1ZSxcbiAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgcmV0dXJuIF9QYXR0ZXJuVXRpbHMuZm9ybWF0UGF0dGVybjtcbiAgfVxufSk7XG5cbnZhciBfUm91dGVyMiA9IHJlcXVpcmUoJy4vUm91dGVyJyk7XG5cbnZhciBfUm91dGVyMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlcjIpO1xuXG52YXIgX0xpbmsyID0gcmVxdWlyZSgnLi9MaW5rJyk7XG5cbnZhciBfTGluazMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9MaW5rMik7XG5cbnZhciBfSW5kZXhMaW5rMiA9IHJlcXVpcmUoJy4vSW5kZXhMaW5rJyk7XG5cbnZhciBfSW5kZXhMaW5rMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0luZGV4TGluazIpO1xuXG52YXIgX3dpdGhSb3V0ZXIyID0gcmVxdWlyZSgnLi93aXRoUm91dGVyJyk7XG5cbnZhciBfd2l0aFJvdXRlcjMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93aXRoUm91dGVyMik7XG5cbnZhciBfSW5kZXhSZWRpcmVjdDIgPSByZXF1aXJlKCcuL0luZGV4UmVkaXJlY3QnKTtcblxudmFyIF9JbmRleFJlZGlyZWN0MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0luZGV4UmVkaXJlY3QyKTtcblxudmFyIF9JbmRleFJvdXRlMiA9IHJlcXVpcmUoJy4vSW5kZXhSb3V0ZScpO1xuXG52YXIgX0luZGV4Um91dGUzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfSW5kZXhSb3V0ZTIpO1xuXG52YXIgX1JlZGlyZWN0MiA9IHJlcXVpcmUoJy4vUmVkaXJlY3QnKTtcblxudmFyIF9SZWRpcmVjdDMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9SZWRpcmVjdDIpO1xuXG52YXIgX1JvdXRlMiA9IHJlcXVpcmUoJy4vUm91dGUnKTtcblxudmFyIF9Sb3V0ZTMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0ZTIpO1xuXG52YXIgX0hpc3RvcnkyID0gcmVxdWlyZSgnLi9IaXN0b3J5Jyk7XG5cbnZhciBfSGlzdG9yeTMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9IaXN0b3J5Mik7XG5cbnZhciBfTGlmZWN5Y2xlMiA9IHJlcXVpcmUoJy4vTGlmZWN5Y2xlJyk7XG5cbnZhciBfTGlmZWN5Y2xlMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX0xpZmVjeWNsZTIpO1xuXG52YXIgX1JvdXRlQ29udGV4dDIgPSByZXF1aXJlKCcuL1JvdXRlQ29udGV4dCcpO1xuXG52YXIgX1JvdXRlQ29udGV4dDMgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9Sb3V0ZUNvbnRleHQyKTtcblxudmFyIF91c2VSb3V0ZXMyID0gcmVxdWlyZSgnLi91c2VSb3V0ZXMnKTtcblxudmFyIF91c2VSb3V0ZXMzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUm91dGVzMik7XG5cbnZhciBfUm91dGVyQ29udGV4dDIgPSByZXF1aXJlKCcuL1JvdXRlckNvbnRleHQnKTtcblxudmFyIF9Sb3V0ZXJDb250ZXh0MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX1JvdXRlckNvbnRleHQyKTtcblxudmFyIF9Sb3V0aW5nQ29udGV4dDIgPSByZXF1aXJlKCcuL1JvdXRpbmdDb250ZXh0Jyk7XG5cbnZhciBfUm91dGluZ0NvbnRleHQzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUm91dGluZ0NvbnRleHQyKTtcblxudmFyIF9Qcm9wVHlwZXMzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfUHJvcFR5cGVzMik7XG5cbnZhciBfbWF0Y2gyID0gcmVxdWlyZSgnLi9tYXRjaCcpO1xuXG52YXIgX21hdGNoMyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX21hdGNoMik7XG5cbnZhciBfdXNlUm91dGVySGlzdG9yeTIgPSByZXF1aXJlKCcuL3VzZVJvdXRlckhpc3RvcnknKTtcblxudmFyIF91c2VSb3V0ZXJIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZVJvdXRlckhpc3RvcnkyKTtcblxudmFyIF9hcHBseVJvdXRlck1pZGRsZXdhcmUyID0gcmVxdWlyZSgnLi9hcHBseVJvdXRlck1pZGRsZXdhcmUnKTtcblxudmFyIF9hcHBseVJvdXRlck1pZGRsZXdhcmUzID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfYXBwbHlSb3V0ZXJNaWRkbGV3YXJlMik7XG5cbnZhciBfYnJvd3Nlckhpc3RvcnkyID0gcmVxdWlyZSgnLi9icm93c2VySGlzdG9yeScpO1xuXG52YXIgX2Jyb3dzZXJIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2Jyb3dzZXJIaXN0b3J5Mik7XG5cbnZhciBfaGFzaEhpc3RvcnkyID0gcmVxdWlyZSgnLi9oYXNoSGlzdG9yeScpO1xuXG52YXIgX2hhc2hIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2hhc2hIaXN0b3J5Mik7XG5cbnZhciBfY3JlYXRlTWVtb3J5SGlzdG9yeTIgPSByZXF1aXJlKCcuL2NyZWF0ZU1lbW9yeUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVNZW1vcnlIaXN0b3J5MyA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZU1lbW9yeUhpc3RvcnkyKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZXhwb3J0cy5Sb3V0ZXIgPSBfUm91dGVyMy5kZWZhdWx0OyAvKiBjb21wb25lbnRzICovXG5cbmV4cG9ydHMuTGluayA9IF9MaW5rMy5kZWZhdWx0O1xuZXhwb3J0cy5JbmRleExpbmsgPSBfSW5kZXhMaW5rMy5kZWZhdWx0O1xuZXhwb3J0cy53aXRoUm91dGVyID0gX3dpdGhSb3V0ZXIzLmRlZmF1bHQ7XG5cbi8qIGNvbXBvbmVudHMgKGNvbmZpZ3VyYXRpb24pICovXG5cbmV4cG9ydHMuSW5kZXhSZWRpcmVjdCA9IF9JbmRleFJlZGlyZWN0My5kZWZhdWx0O1xuZXhwb3J0cy5JbmRleFJvdXRlID0gX0luZGV4Um91dGUzLmRlZmF1bHQ7XG5leHBvcnRzLlJlZGlyZWN0ID0gX1JlZGlyZWN0My5kZWZhdWx0O1xuZXhwb3J0cy5Sb3V0ZSA9IF9Sb3V0ZTMuZGVmYXVsdDtcblxuLyogbWl4aW5zICovXG5cbmV4cG9ydHMuSGlzdG9yeSA9IF9IaXN0b3J5My5kZWZhdWx0O1xuZXhwb3J0cy5MaWZlY3ljbGUgPSBfTGlmZWN5Y2xlMy5kZWZhdWx0O1xuZXhwb3J0cy5Sb3V0ZUNvbnRleHQgPSBfUm91dGVDb250ZXh0My5kZWZhdWx0O1xuXG4vKiB1dGlscyAqL1xuXG5leHBvcnRzLnVzZVJvdXRlcyA9IF91c2VSb3V0ZXMzLmRlZmF1bHQ7XG5leHBvcnRzLlJvdXRlckNvbnRleHQgPSBfUm91dGVyQ29udGV4dDMuZGVmYXVsdDtcbmV4cG9ydHMuUm91dGluZ0NvbnRleHQgPSBfUm91dGluZ0NvbnRleHQzLmRlZmF1bHQ7XG5leHBvcnRzLlByb3BUeXBlcyA9IF9Qcm9wVHlwZXMzLmRlZmF1bHQ7XG5leHBvcnRzLm1hdGNoID0gX21hdGNoMy5kZWZhdWx0O1xuZXhwb3J0cy51c2VSb3V0ZXJIaXN0b3J5ID0gX3VzZVJvdXRlckhpc3RvcnkzLmRlZmF1bHQ7XG5leHBvcnRzLmFwcGx5Um91dGVyTWlkZGxld2FyZSA9IF9hcHBseVJvdXRlck1pZGRsZXdhcmUzLmRlZmF1bHQ7XG5cbi8qIGhpc3RvcmllcyAqL1xuXG5leHBvcnRzLmJyb3dzZXJIaXN0b3J5ID0gX2Jyb3dzZXJIaXN0b3J5My5kZWZhdWx0O1xuZXhwb3J0cy5oYXNoSGlzdG9yeSA9IF9oYXNoSGlzdG9yeTMuZGVmYXVsdDtcbmV4cG9ydHMuY3JlYXRlTWVtb3J5SGlzdG9yeSA9IF9jcmVhdGVNZW1vcnlIaXN0b3J5My5kZWZhdWx0OyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF90eXBlb2YgPSB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgdHlwZW9mIFN5bWJvbC5pdGVyYXRvciA9PT0gXCJzeW1ib2xcIiA/IGZ1bmN0aW9uIChvYmopIHsgcmV0dXJuIHR5cGVvZiBvYmo7IH0gOiBmdW5jdGlvbiAob2JqKSB7IHJldHVybiBvYmogJiYgdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIG9iai5jb25zdHJ1Y3RvciA9PT0gU3ltYm9sID8gXCJzeW1ib2xcIiA6IHR5cGVvZiBvYmo7IH07XG5cbmV4cG9ydHMuZGVmYXVsdCA9IGlzQWN0aXZlO1xuXG52YXIgX1BhdHRlcm5VdGlscyA9IHJlcXVpcmUoJy4vUGF0dGVyblV0aWxzJyk7XG5cbmZ1bmN0aW9uIGRlZXBFcXVhbChhLCBiKSB7XG4gIGlmIChhID09IGIpIHJldHVybiB0cnVlO1xuXG4gIGlmIChhID09IG51bGwgfHwgYiA9PSBudWxsKSByZXR1cm4gZmFsc2U7XG5cbiAgaWYgKEFycmF5LmlzQXJyYXkoYSkpIHtcbiAgICByZXR1cm4gQXJyYXkuaXNBcnJheShiKSAmJiBhLmxlbmd0aCA9PT0gYi5sZW5ndGggJiYgYS5ldmVyeShmdW5jdGlvbiAoaXRlbSwgaW5kZXgpIHtcbiAgICAgIHJldHVybiBkZWVwRXF1YWwoaXRlbSwgYltpbmRleF0pO1xuICAgIH0pO1xuICB9XG5cbiAgaWYgKCh0eXBlb2YgYSA9PT0gJ3VuZGVmaW5lZCcgPyAndW5kZWZpbmVkJyA6IF90eXBlb2YoYSkpID09PSAnb2JqZWN0Jykge1xuICAgIGZvciAodmFyIHAgaW4gYSkge1xuICAgICAgaWYgKCFPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoYSwgcCkpIHtcbiAgICAgICAgY29udGludWU7XG4gICAgICB9XG5cbiAgICAgIGlmIChhW3BdID09PSB1bmRlZmluZWQpIHtcbiAgICAgICAgaWYgKGJbcF0gIT09IHVuZGVmaW5lZCkge1xuICAgICAgICAgIHJldHVybiBmYWxzZTtcbiAgICAgICAgfVxuICAgICAgfSBlbHNlIGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKGIsIHApKSB7XG4gICAgICAgIHJldHVybiBmYWxzZTtcbiAgICAgIH0gZWxzZSBpZiAoIWRlZXBFcXVhbChhW3BdLCBiW3BdKSkge1xuICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIHRydWU7XG4gIH1cblxuICByZXR1cm4gU3RyaW5nKGEpID09PSBTdHJpbmcoYik7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIHRoZSBjdXJyZW50IHBhdGhuYW1lIG1hdGNoZXMgdGhlIHN1cHBsaWVkIG9uZSwgbmV0IG9mXG4gKiBsZWFkaW5nIGFuZCB0cmFpbGluZyBzbGFzaCBub3JtYWxpemF0aW9uLiBUaGlzIGlzIHN1ZmZpY2llbnQgZm9yIGFuXG4gKiBpbmRleE9ubHkgcm91dGUgbWF0Y2guXG4gKi9cbmZ1bmN0aW9uIHBhdGhJc0FjdGl2ZShwYXRobmFtZSwgY3VycmVudFBhdGhuYW1lKSB7XG4gIC8vIE5vcm1hbGl6ZSBsZWFkaW5nIHNsYXNoIGZvciBjb25zaXN0ZW5jeS4gTGVhZGluZyBzbGFzaCBvbiBwYXRobmFtZSBoYXNcbiAgLy8gYWxyZWFkeSBiZWVuIG5vcm1hbGl6ZWQgaW4gaXNBY3RpdmUuIFNlZSBjYXZlYXQgdGhlcmUuXG4gIGlmIChjdXJyZW50UGF0aG5hbWUuY2hhckF0KDApICE9PSAnLycpIHtcbiAgICBjdXJyZW50UGF0aG5hbWUgPSAnLycgKyBjdXJyZW50UGF0aG5hbWU7XG4gIH1cblxuICAvLyBOb3JtYWxpemUgdGhlIGVuZCBvZiBib3RoIHBhdGggbmFtZXMgdG9vLiBNYXliZSBgL2Zvby9gIHNob3VsZG4ndCBzaG93XG4gIC8vIGAvZm9vYCBhcyBhY3RpdmUsIGJ1dCBpbiB0aGlzIGNhc2UsIHdlIHdvdWxkIGFscmVhZHkgaGF2ZSBmYWlsZWQgdGhlXG4gIC8vIG1hdGNoLlxuICBpZiAocGF0aG5hbWUuY2hhckF0KHBhdGhuYW1lLmxlbmd0aCAtIDEpICE9PSAnLycpIHtcbiAgICBwYXRobmFtZSArPSAnLyc7XG4gIH1cbiAgaWYgKGN1cnJlbnRQYXRobmFtZS5jaGFyQXQoY3VycmVudFBhdGhuYW1lLmxlbmd0aCAtIDEpICE9PSAnLycpIHtcbiAgICBjdXJyZW50UGF0aG5hbWUgKz0gJy8nO1xuICB9XG5cbiAgcmV0dXJuIGN1cnJlbnRQYXRobmFtZSA9PT0gcGF0aG5hbWU7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIHRoZSBnaXZlbiBwYXRobmFtZSBtYXRjaGVzIHRoZSBhY3RpdmUgcm91dGVzIGFuZCBwYXJhbXMuXG4gKi9cbmZ1bmN0aW9uIHJvdXRlSXNBY3RpdmUocGF0aG5hbWUsIHJvdXRlcywgcGFyYW1zKSB7XG4gIHZhciByZW1haW5pbmdQYXRobmFtZSA9IHBhdGhuYW1lLFxuICAgICAgcGFyYW1OYW1lcyA9IFtdLFxuICAgICAgcGFyYW1WYWx1ZXMgPSBbXTtcblxuICAvLyBmb3IuLi5vZiB3b3VsZCB3b3JrIGhlcmUgYnV0IGl0J3MgcHJvYmFibHkgc2xvd2VyIHBvc3QtdHJhbnNwaWxhdGlvbi5cbiAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IHJvdXRlcy5sZW5ndGg7IGkgPCBsZW47ICsraSkge1xuICAgIHZhciByb3V0ZSA9IHJvdXRlc1tpXTtcbiAgICB2YXIgcGF0dGVybiA9IHJvdXRlLnBhdGggfHwgJyc7XG5cbiAgICBpZiAocGF0dGVybi5jaGFyQXQoMCkgPT09ICcvJykge1xuICAgICAgcmVtYWluaW5nUGF0aG5hbWUgPSBwYXRobmFtZTtcbiAgICAgIHBhcmFtTmFtZXMgPSBbXTtcbiAgICAgIHBhcmFtVmFsdWVzID0gW107XG4gICAgfVxuXG4gICAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lICE9PSBudWxsICYmIHBhdHRlcm4pIHtcbiAgICAgIHZhciBtYXRjaGVkID0gKDAsIF9QYXR0ZXJuVXRpbHMubWF0Y2hQYXR0ZXJuKShwYXR0ZXJuLCByZW1haW5pbmdQYXRobmFtZSk7XG4gICAgICBpZiAobWF0Y2hlZCkge1xuICAgICAgICByZW1haW5pbmdQYXRobmFtZSA9IG1hdGNoZWQucmVtYWluaW5nUGF0aG5hbWU7XG4gICAgICAgIHBhcmFtTmFtZXMgPSBbXS5jb25jYXQocGFyYW1OYW1lcywgbWF0Y2hlZC5wYXJhbU5hbWVzKTtcbiAgICAgICAgcGFyYW1WYWx1ZXMgPSBbXS5jb25jYXQocGFyYW1WYWx1ZXMsIG1hdGNoZWQucGFyYW1WYWx1ZXMpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcmVtYWluaW5nUGF0aG5hbWUgPSBudWxsO1xuICAgICAgfVxuXG4gICAgICBpZiAocmVtYWluaW5nUGF0aG5hbWUgPT09ICcnKSB7XG4gICAgICAgIC8vIFdlIGhhdmUgYW4gZXhhY3QgbWF0Y2ggb24gdGhlIHJvdXRlLiBKdXN0IGNoZWNrIHRoYXQgYWxsIHRoZSBwYXJhbXNcbiAgICAgICAgLy8gbWF0Y2guXG4gICAgICAgIC8vIEZJWE1FOiBUaGlzIGRvZXNuJ3Qgd29yayBvbiByZXBlYXRlZCBwYXJhbXMuXG4gICAgICAgIHJldHVybiBwYXJhbU5hbWVzLmV2ZXJ5KGZ1bmN0aW9uIChwYXJhbU5hbWUsIGluZGV4KSB7XG4gICAgICAgICAgcmV0dXJuIFN0cmluZyhwYXJhbVZhbHVlc1tpbmRleF0pID09PSBTdHJpbmcocGFyYW1zW3BhcmFtTmFtZV0pO1xuICAgICAgICB9KTtcbiAgICAgIH1cbiAgICB9XG4gIH1cblxuICByZXR1cm4gZmFsc2U7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIGFsbCBrZXkvdmFsdWUgcGFpcnMgaW4gdGhlIGdpdmVuIHF1ZXJ5IGFyZVxuICogY3VycmVudGx5IGFjdGl2ZS5cbiAqL1xuZnVuY3Rpb24gcXVlcnlJc0FjdGl2ZShxdWVyeSwgYWN0aXZlUXVlcnkpIHtcbiAgaWYgKGFjdGl2ZVF1ZXJ5ID09IG51bGwpIHJldHVybiBxdWVyeSA9PSBudWxsO1xuXG4gIGlmIChxdWVyeSA9PSBudWxsKSByZXR1cm4gdHJ1ZTtcblxuICByZXR1cm4gZGVlcEVxdWFsKHF1ZXJ5LCBhY3RpdmVRdWVyeSk7XG59XG5cbi8qKlxuICogUmV0dXJucyB0cnVlIGlmIGEgPExpbms+IHRvIHRoZSBnaXZlbiBwYXRobmFtZS9xdWVyeSBjb21iaW5hdGlvbiBpc1xuICogY3VycmVudGx5IGFjdGl2ZS5cbiAqL1xuZnVuY3Rpb24gaXNBY3RpdmUoX3JlZiwgaW5kZXhPbmx5LCBjdXJyZW50TG9jYXRpb24sIHJvdXRlcywgcGFyYW1zKSB7XG4gIHZhciBwYXRobmFtZSA9IF9yZWYucGF0aG5hbWU7XG4gIHZhciBxdWVyeSA9IF9yZWYucXVlcnk7XG5cbiAgaWYgKGN1cnJlbnRMb2NhdGlvbiA9PSBudWxsKSByZXR1cm4gZmFsc2U7XG5cbiAgLy8gVE9ETzogVGhpcyBpcyBhIGJpdCB1Z2x5LiBJdCBrZWVwcyBhcm91bmQgc3VwcG9ydCBmb3IgdHJlYXRpbmcgcGF0aG5hbWVzXG4gIC8vIHdpdGhvdXQgcHJlY2VkaW5nIHNsYXNoZXMgYXMgYWJzb2x1dGUgcGF0aHMsIGJ1dCBwb3NzaWJseSBhbHNvIHdvcmtzXG4gIC8vIGFyb3VuZCB0aGUgc2FtZSBxdWlya3Mgd2l0aCBiYXNlbmFtZXMgYXMgaW4gbWF0Y2hSb3V0ZXMuXG4gIGlmIChwYXRobmFtZS5jaGFyQXQoMCkgIT09ICcvJykge1xuICAgIHBhdGhuYW1lID0gJy8nICsgcGF0aG5hbWU7XG4gIH1cblxuICBpZiAoIXBhdGhJc0FjdGl2ZShwYXRobmFtZSwgY3VycmVudExvY2F0aW9uLnBhdGhuYW1lKSkge1xuICAgIC8vIFRoZSBwYXRoIGNoZWNrIGlzIG5lY2Vzc2FyeSBhbmQgc3VmZmljaWVudCBmb3IgaW5kZXhPbmx5LCBidXQgb3RoZXJ3aXNlXG4gICAgLy8gd2Ugc3RpbGwgbmVlZCB0byBjaGVjayB0aGUgcm91dGVzLlxuICAgIGlmIChpbmRleE9ubHkgfHwgIXJvdXRlSXNBY3RpdmUocGF0aG5hbWUsIHJvdXRlcywgcGFyYW1zKSkge1xuICAgICAgcmV0dXJuIGZhbHNlO1xuICAgIH1cbiAgfVxuXG4gIHJldHVybiBxdWVyeUlzQWN0aXZlKHF1ZXJ5LCBjdXJyZW50TG9jYXRpb24ucXVlcnkpO1xufVxubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5leHBvcnRzLmRlZmF1bHQgPSBtYWtlU3RhdGVXaXRoTG9jYXRpb247XG5cbnZhciBfZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcyA9IHJlcXVpcmUoJy4vZGVwcmVjYXRlT2JqZWN0UHJvcGVydGllcycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG5mdW5jdGlvbiBtYWtlU3RhdGVXaXRoTG9jYXRpb24oc3RhdGUsIGxvY2F0aW9uKSB7XG4gIGlmIChcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nICYmIF9kZXByZWNhdGVPYmplY3RQcm9wZXJ0aWVzLmNhblVzZU1lbWJyYW5lKSB7XG4gICAgdmFyIHN0YXRlV2l0aExvY2F0aW9uID0gX2V4dGVuZHMoe30sIHN0YXRlKTtcblxuICAgIC8vIEkgZG9uJ3QgdXNlIGRlcHJlY2F0ZU9iamVjdFByb3BlcnRpZXMgaGVyZSBiZWNhdXNlIEkgd2FudCB0byBrZWVwIHRoZVxuICAgIC8vIHNhbWUgY29kZSBwYXRoIGJldHdlZW4gZGV2ZWxvcG1lbnQgYW5kIHByb2R1Y3Rpb24sIGluIHRoYXQgd2UganVzdFxuICAgIC8vIGFzc2lnbiBleHRyYSBwcm9wZXJ0aWVzIHRvIHRoZSBjb3B5IG9mIHRoZSBzdGF0ZSBvYmplY3QgaW4gYm90aCBjYXNlcy5cblxuICAgIHZhciBfbG9vcCA9IGZ1bmN0aW9uIF9sb29wKHByb3ApIHtcbiAgICAgIGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKGxvY2F0aW9uLCBwcm9wKSkge1xuICAgICAgICByZXR1cm4gJ2NvbnRpbnVlJztcbiAgICAgIH1cblxuICAgICAgT2JqZWN0LmRlZmluZVByb3BlcnR5KHN0YXRlV2l0aExvY2F0aW9uLCBwcm9wLCB7XG4gICAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGZhbHNlLCAnQWNjZXNzaW5nIGxvY2F0aW9uIHByb3BlcnRpZXMgZGlyZWN0bHkgZnJvbSB0aGUgZmlyc3QgYXJndW1lbnQgdG8gYGdldENvbXBvbmVudGAsIGBnZXRDb21wb25lbnRzYCwgYGdldENoaWxkUm91dGVzYCwgYW5kIGBnZXRJbmRleFJvdXRlYCBpcyBkZXByZWNhdGVkLiBUaGF0IGFyZ3VtZW50IGlzIG5vdyB0aGUgcm91dGVyIHN0YXRlIChgbmV4dFN0YXRlYCBvciBgcGFydGlhbE5leHRTdGF0ZWApIHJhdGhlciB0aGFuIHRoZSBsb2NhdGlvbi4gVG8gYWNjZXNzIHRoZSBsb2NhdGlvbiwgdXNlIGBuZXh0U3RhdGUubG9jYXRpb25gIG9yIGBwYXJ0aWFsTmV4dFN0YXRlLmxvY2F0aW9uYC4nKSA6IHZvaWQgMDtcbiAgICAgICAgICByZXR1cm4gbG9jYXRpb25bcHJvcF07XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH07XG5cbiAgICBmb3IgKHZhciBwcm9wIGluIGxvY2F0aW9uKSB7XG4gICAgICB2YXIgX3JldCA9IF9sb29wKHByb3ApO1xuXG4gICAgICBpZiAoX3JldCA9PT0gJ2NvbnRpbnVlJykgY29udGludWU7XG4gICAgfVxuXG4gICAgcmV0dXJuIHN0YXRlV2l0aExvY2F0aW9uO1xuICB9XG5cbiAgcmV0dXJuIF9leHRlbmRzKHt9LCBzdGF0ZSwgbG9jYXRpb24pO1xufVxubW9kdWxlLmV4cG9ydHMgPSBleHBvcnRzWydkZWZhdWx0J107IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG52YXIgX2ludmFyaWFudCA9IHJlcXVpcmUoJ2ludmFyaWFudCcpO1xuXG52YXIgX2ludmFyaWFudDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pbnZhcmlhbnQpO1xuXG52YXIgX2NyZWF0ZU1lbW9yeUhpc3RvcnkgPSByZXF1aXJlKCcuL2NyZWF0ZU1lbW9yeUhpc3RvcnknKTtcblxudmFyIF9jcmVhdGVNZW1vcnlIaXN0b3J5MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZU1lbW9yeUhpc3RvcnkpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyID0gcmVxdWlyZSgnLi9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcicpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyKTtcblxudmFyIF9Sb3V0ZVV0aWxzID0gcmVxdWlyZSgnLi9Sb3V0ZVV0aWxzJyk7XG5cbnZhciBfUm91dGVyVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlclV0aWxzJyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IGRlZmF1bHQ6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIF9vYmplY3RXaXRob3V0UHJvcGVydGllcyhvYmosIGtleXMpIHsgdmFyIHRhcmdldCA9IHt9OyBmb3IgKHZhciBpIGluIG9iaikgeyBpZiAoa2V5cy5pbmRleE9mKGkpID49IDApIGNvbnRpbnVlOyBpZiAoIU9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChvYmosIGkpKSBjb250aW51ZTsgdGFyZ2V0W2ldID0gb2JqW2ldOyB9IHJldHVybiB0YXJnZXQ7IH1cblxuLyoqXG4gKiBBIGhpZ2gtbGV2ZWwgQVBJIHRvIGJlIHVzZWQgZm9yIHNlcnZlci1zaWRlIHJlbmRlcmluZy5cbiAqXG4gKiBUaGlzIGZ1bmN0aW9uIG1hdGNoZXMgYSBsb2NhdGlvbiB0byBhIHNldCBvZiByb3V0ZXMgYW5kIGNhbGxzXG4gKiBjYWxsYmFjayhlcnJvciwgcmVkaXJlY3RMb2NhdGlvbiwgcmVuZGVyUHJvcHMpIHdoZW4gZmluaXNoZWQuXG4gKlxuICogTm90ZTogWW91IHByb2JhYmx5IGRvbid0IHdhbnQgdG8gdXNlIHRoaXMgaW4gYSBicm93c2VyIHVubGVzcyB5b3UncmUgdXNpbmdcbiAqIHNlcnZlci1zaWRlIHJlbmRlcmluZyB3aXRoIGFzeW5jIHJvdXRlcy5cbiAqL1xuZnVuY3Rpb24gbWF0Y2goX3JlZiwgY2FsbGJhY2spIHtcbiAgdmFyIGhpc3RvcnkgPSBfcmVmLmhpc3Rvcnk7XG4gIHZhciByb3V0ZXMgPSBfcmVmLnJvdXRlcztcbiAgdmFyIGxvY2F0aW9uID0gX3JlZi5sb2NhdGlvbjtcblxuICB2YXIgb3B0aW9ucyA9IF9vYmplY3RXaXRob3V0UHJvcGVydGllcyhfcmVmLCBbJ2hpc3RvcnknLCAncm91dGVzJywgJ2xvY2F0aW9uJ10pO1xuXG4gICEoaGlzdG9yeSB8fCBsb2NhdGlvbikgPyBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9pbnZhcmlhbnQyLmRlZmF1bHQpKGZhbHNlLCAnbWF0Y2ggbmVlZHMgYSBoaXN0b3J5IG9yIGEgbG9jYXRpb24nKSA6ICgwLCBfaW52YXJpYW50Mi5kZWZhdWx0KShmYWxzZSkgOiB2b2lkIDA7XG5cbiAgaGlzdG9yeSA9IGhpc3RvcnkgPyBoaXN0b3J5IDogKDAsIF9jcmVhdGVNZW1vcnlIaXN0b3J5Mi5kZWZhdWx0KShvcHRpb25zKTtcbiAgdmFyIHRyYW5zaXRpb25NYW5hZ2VyID0gKDAsIF9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcjIuZGVmYXVsdCkoaGlzdG9yeSwgKDAsIF9Sb3V0ZVV0aWxzLmNyZWF0ZVJvdXRlcykocm91dGVzKSk7XG5cbiAgdmFyIHVubGlzdGVuID0gdm9pZCAwO1xuXG4gIGlmIChsb2NhdGlvbikge1xuICAgIC8vIEFsbG93IG1hdGNoKHsgbG9jYXRpb246ICcvdGhlL3BhdGgnLCAuLi4gfSlcbiAgICBsb2NhdGlvbiA9IGhpc3RvcnkuY3JlYXRlTG9jYXRpb24obG9jYXRpb24pO1xuICB9IGVsc2Uge1xuICAgIC8vIFBpY2sgdXAgdGhlIGxvY2F0aW9uIGZyb20gdGhlIGhpc3RvcnkgdmlhIHN5bmNocm9ub3VzIGhpc3RvcnkubGlzdGVuXG4gICAgLy8gY2FsbCBpZiBuZWVkZWQuXG4gICAgdW5saXN0ZW4gPSBoaXN0b3J5Lmxpc3RlbihmdW5jdGlvbiAoaGlzdG9yeUxvY2F0aW9uKSB7XG4gICAgICBsb2NhdGlvbiA9IGhpc3RvcnlMb2NhdGlvbjtcbiAgICB9KTtcbiAgfVxuXG4gIHZhciByb3V0ZXIgPSAoMCwgX1JvdXRlclV0aWxzLmNyZWF0ZVJvdXRlck9iamVjdCkoaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXIpO1xuICBoaXN0b3J5ID0gKDAsIF9Sb3V0ZXJVdGlscy5jcmVhdGVSb3V0aW5nSGlzdG9yeSkoaGlzdG9yeSwgdHJhbnNpdGlvbk1hbmFnZXIpO1xuXG4gIHRyYW5zaXRpb25NYW5hZ2VyLm1hdGNoKGxvY2F0aW9uLCBmdW5jdGlvbiAoZXJyb3IsIHJlZGlyZWN0TG9jYXRpb24sIG5leHRTdGF0ZSkge1xuICAgIGNhbGxiYWNrKGVycm9yLCByZWRpcmVjdExvY2F0aW9uLCBuZXh0U3RhdGUgJiYgX2V4dGVuZHMoe30sIG5leHRTdGF0ZSwge1xuICAgICAgaGlzdG9yeTogaGlzdG9yeSxcbiAgICAgIHJvdXRlcjogcm91dGVyLFxuICAgICAgbWF0Y2hDb250ZXh0OiB7IGhpc3Rvcnk6IGhpc3RvcnksIHRyYW5zaXRpb25NYW5hZ2VyOiB0cmFuc2l0aW9uTWFuYWdlciwgcm91dGVyOiByb3V0ZXIgfVxuICAgIH0pKTtcblxuICAgIC8vIERlZmVyIHJlbW92aW5nIHRoZSBsaXN0ZW5lciB0byBoZXJlIHRvIHByZXZlbnQgRE9NIGhpc3RvcmllcyBmcm9tIGhhdmluZ1xuICAgIC8vIHRvIHVud2luZCBET00gZXZlbnQgbGlzdGVuZXJzIHVubmVjZXNzYXJpbHksIGluIGNhc2UgY2FsbGJhY2sgcmVuZGVycyBhXG4gICAgLy8gPFJvdXRlcj4gYW5kIGF0dGFjaGVzIGFub3RoZXIgaGlzdG9yeSBsaXN0ZW5lci5cbiAgICBpZiAodW5saXN0ZW4pIHtcbiAgICAgIHVubGlzdGVuKCk7XG4gICAgfVxuICB9KTtcbn1cblxuZXhwb3J0cy5kZWZhdWx0ID0gbWF0Y2g7XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfdHlwZW9mID0gdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIHR5cGVvZiBTeW1ib2wuaXRlcmF0b3IgPT09IFwic3ltYm9sXCIgPyBmdW5jdGlvbiAob2JqKSB7IHJldHVybiB0eXBlb2Ygb2JqOyB9IDogZnVuY3Rpb24gKG9iaikgeyByZXR1cm4gb2JqICYmIHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiBvYmouY29uc3RydWN0b3IgPT09IFN5bWJvbCA/IFwic3ltYm9sXCIgOiB0eXBlb2Ygb2JqOyB9O1xuXG5leHBvcnRzLmRlZmF1bHQgPSBtYXRjaFJvdXRlcztcblxudmFyIF9Bc3luY1V0aWxzID0gcmVxdWlyZSgnLi9Bc3luY1V0aWxzJyk7XG5cbnZhciBfbWFrZVN0YXRlV2l0aExvY2F0aW9uID0gcmVxdWlyZSgnLi9tYWtlU3RhdGVXaXRoTG9jYXRpb24nKTtcblxudmFyIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24yID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfbWFrZVN0YXRlV2l0aExvY2F0aW9uKTtcblxudmFyIF9QYXR0ZXJuVXRpbHMgPSByZXF1aXJlKCcuL1BhdHRlcm5VdGlscycpO1xuXG52YXIgX3JvdXRlcldhcm5pbmcgPSByZXF1aXJlKCcuL3JvdXRlcldhcm5pbmcnKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3JvdXRlcldhcm5pbmcpO1xuXG52YXIgX1JvdXRlVXRpbHMgPSByZXF1aXJlKCcuL1JvdXRlVXRpbHMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gZ2V0Q2hpbGRSb3V0ZXMocm91dGUsIGxvY2F0aW9uLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgY2FsbGJhY2spIHtcbiAgaWYgKHJvdXRlLmNoaWxkUm91dGVzKSB7XG4gICAgcmV0dXJuIFtudWxsLCByb3V0ZS5jaGlsZFJvdXRlc107XG4gIH1cbiAgaWYgKCFyb3V0ZS5nZXRDaGlsZFJvdXRlcykge1xuICAgIHJldHVybiBbXTtcbiAgfVxuXG4gIHZhciBzeW5jID0gdHJ1ZSxcbiAgICAgIHJlc3VsdCA9IHZvaWQgMDtcblxuICB2YXIgcGFydGlhbE5leHRTdGF0ZSA9IHtcbiAgICBsb2NhdGlvbjogbG9jYXRpb24sXG4gICAgcGFyYW1zOiBjcmVhdGVQYXJhbXMocGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpXG4gIH07XG5cbiAgdmFyIHBhcnRpYWxOZXh0U3RhdGVXaXRoTG9jYXRpb24gPSAoMCwgX21ha2VTdGF0ZVdpdGhMb2NhdGlvbjIuZGVmYXVsdCkocGFydGlhbE5leHRTdGF0ZSwgbG9jYXRpb24pO1xuXG4gIHJvdXRlLmdldENoaWxkUm91dGVzKHBhcnRpYWxOZXh0U3RhdGVXaXRoTG9jYXRpb24sIGZ1bmN0aW9uIChlcnJvciwgY2hpbGRSb3V0ZXMpIHtcbiAgICBjaGlsZFJvdXRlcyA9ICFlcnJvciAmJiAoMCwgX1JvdXRlVXRpbHMuY3JlYXRlUm91dGVzKShjaGlsZFJvdXRlcyk7XG4gICAgaWYgKHN5bmMpIHtcbiAgICAgIHJlc3VsdCA9IFtlcnJvciwgY2hpbGRSb3V0ZXNdO1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGNhbGxiYWNrKGVycm9yLCBjaGlsZFJvdXRlcyk7XG4gIH0pO1xuXG4gIHN5bmMgPSBmYWxzZTtcbiAgcmV0dXJuIHJlc3VsdDsgLy8gTWlnaHQgYmUgdW5kZWZpbmVkLlxufVxuXG5mdW5jdGlvbiBnZXRJbmRleFJvdXRlKHJvdXRlLCBsb2NhdGlvbiwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMsIGNhbGxiYWNrKSB7XG4gIGlmIChyb3V0ZS5pbmRleFJvdXRlKSB7XG4gICAgY2FsbGJhY2sobnVsbCwgcm91dGUuaW5kZXhSb3V0ZSk7XG4gIH0gZWxzZSBpZiAocm91dGUuZ2V0SW5kZXhSb3V0ZSkge1xuICAgIHZhciBwYXJ0aWFsTmV4dFN0YXRlID0ge1xuICAgICAgbG9jYXRpb246IGxvY2F0aW9uLFxuICAgICAgcGFyYW1zOiBjcmVhdGVQYXJhbXMocGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMpXG4gICAgfTtcblxuICAgIHZhciBwYXJ0aWFsTmV4dFN0YXRlV2l0aExvY2F0aW9uID0gKDAsIF9tYWtlU3RhdGVXaXRoTG9jYXRpb24yLmRlZmF1bHQpKHBhcnRpYWxOZXh0U3RhdGUsIGxvY2F0aW9uKTtcblxuICAgIHJvdXRlLmdldEluZGV4Um91dGUocGFydGlhbE5leHRTdGF0ZVdpdGhMb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCBpbmRleFJvdXRlKSB7XG4gICAgICBjYWxsYmFjayhlcnJvciwgIWVycm9yICYmICgwLCBfUm91dGVVdGlscy5jcmVhdGVSb3V0ZXMpKGluZGV4Um91dGUpWzBdKTtcbiAgICB9KTtcbiAgfSBlbHNlIGlmIChyb3V0ZS5jaGlsZFJvdXRlcykge1xuICAgIChmdW5jdGlvbiAoKSB7XG4gICAgICB2YXIgcGF0aGxlc3MgPSByb3V0ZS5jaGlsZFJvdXRlcy5maWx0ZXIoZnVuY3Rpb24gKGNoaWxkUm91dGUpIHtcbiAgICAgICAgcmV0dXJuICFjaGlsZFJvdXRlLnBhdGg7XG4gICAgICB9KTtcblxuICAgICAgKDAsIF9Bc3luY1V0aWxzLmxvb3BBc3luYykocGF0aGxlc3MubGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIG5leHQsIGRvbmUpIHtcbiAgICAgICAgZ2V0SW5kZXhSb3V0ZShwYXRobGVzc1tpbmRleF0sIGxvY2F0aW9uLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgZnVuY3Rpb24gKGVycm9yLCBpbmRleFJvdXRlKSB7XG4gICAgICAgICAgaWYgKGVycm9yIHx8IGluZGV4Um91dGUpIHtcbiAgICAgICAgICAgIHZhciByb3V0ZXMgPSBbcGF0aGxlc3NbaW5kZXhdXS5jb25jYXQoQXJyYXkuaXNBcnJheShpbmRleFJvdXRlKSA/IGluZGV4Um91dGUgOiBbaW5kZXhSb3V0ZV0pO1xuICAgICAgICAgICAgZG9uZShlcnJvciwgcm91dGVzKTtcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgbmV4dCgpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9LCBmdW5jdGlvbiAoZXJyLCByb3V0ZXMpIHtcbiAgICAgICAgY2FsbGJhY2sobnVsbCwgcm91dGVzKTtcbiAgICAgIH0pO1xuICAgIH0pKCk7XG4gIH0gZWxzZSB7XG4gICAgY2FsbGJhY2soKTtcbiAgfVxufVxuXG5mdW5jdGlvbiBhc3NpZ25QYXJhbXMocGFyYW1zLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcykge1xuICByZXR1cm4gcGFyYW1OYW1lcy5yZWR1Y2UoZnVuY3Rpb24gKHBhcmFtcywgcGFyYW1OYW1lLCBpbmRleCkge1xuICAgIHZhciBwYXJhbVZhbHVlID0gcGFyYW1WYWx1ZXMgJiYgcGFyYW1WYWx1ZXNbaW5kZXhdO1xuXG4gICAgaWYgKEFycmF5LmlzQXJyYXkocGFyYW1zW3BhcmFtTmFtZV0pKSB7XG4gICAgICBwYXJhbXNbcGFyYW1OYW1lXS5wdXNoKHBhcmFtVmFsdWUpO1xuICAgIH0gZWxzZSBpZiAocGFyYW1OYW1lIGluIHBhcmFtcykge1xuICAgICAgcGFyYW1zW3BhcmFtTmFtZV0gPSBbcGFyYW1zW3BhcmFtTmFtZV0sIHBhcmFtVmFsdWVdO1xuICAgIH0gZWxzZSB7XG4gICAgICBwYXJhbXNbcGFyYW1OYW1lXSA9IHBhcmFtVmFsdWU7XG4gICAgfVxuXG4gICAgcmV0dXJuIHBhcmFtcztcbiAgfSwgcGFyYW1zKTtcbn1cblxuZnVuY3Rpb24gY3JlYXRlUGFyYW1zKHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKSB7XG4gIHJldHVybiBhc3NpZ25QYXJhbXMoe30sIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKTtcbn1cblxuZnVuY3Rpb24gbWF0Y2hSb3V0ZURlZXAocm91dGUsIGxvY2F0aW9uLCByZW1haW5pbmdQYXRobmFtZSwgcGFyYW1OYW1lcywgcGFyYW1WYWx1ZXMsIGNhbGxiYWNrKSB7XG4gIHZhciBwYXR0ZXJuID0gcm91dGUucGF0aCB8fCAnJztcblxuICBpZiAocGF0dGVybi5jaGFyQXQoMCkgPT09ICcvJykge1xuICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gICAgcGFyYW1OYW1lcyA9IFtdO1xuICAgIHBhcmFtVmFsdWVzID0gW107XG4gIH1cblxuICAvLyBPbmx5IHRyeSB0byBtYXRjaCB0aGUgcGF0aCBpZiB0aGUgcm91dGUgYWN0dWFsbHkgaGFzIGEgcGF0dGVybiwgYW5kIGlmXG4gIC8vIHdlJ3JlIG5vdCBqdXN0IHNlYXJjaGluZyBmb3IgcG90ZW50aWFsIG5lc3RlZCBhYnNvbHV0ZSBwYXRocy5cbiAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lICE9PSBudWxsICYmIHBhdHRlcm4pIHtcbiAgICB0cnkge1xuICAgICAgdmFyIG1hdGNoZWQgPSAoMCwgX1BhdHRlcm5VdGlscy5tYXRjaFBhdHRlcm4pKHBhdHRlcm4sIHJlbWFpbmluZ1BhdGhuYW1lKTtcbiAgICAgIGlmIChtYXRjaGVkKSB7XG4gICAgICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbWF0Y2hlZC5yZW1haW5pbmdQYXRobmFtZTtcbiAgICAgICAgcGFyYW1OYW1lcyA9IFtdLmNvbmNhdChwYXJhbU5hbWVzLCBtYXRjaGVkLnBhcmFtTmFtZXMpO1xuICAgICAgICBwYXJhbVZhbHVlcyA9IFtdLmNvbmNhdChwYXJhbVZhbHVlcywgbWF0Y2hlZC5wYXJhbVZhbHVlcyk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZW1haW5pbmdQYXRobmFtZSA9IG51bGw7XG4gICAgICB9XG4gICAgfSBjYXRjaCAoZXJyb3IpIHtcbiAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICB9XG5cbiAgICAvLyBCeSBhc3N1bXB0aW9uLCBwYXR0ZXJuIGlzIG5vbi1lbXB0eSBoZXJlLCB3aGljaCBpcyB0aGUgcHJlcmVxdWlzaXRlIGZvclxuICAgIC8vIGFjdHVhbGx5IHRlcm1pbmF0aW5nIGEgbWF0Y2guXG4gICAgaWYgKHJlbWFpbmluZ1BhdGhuYW1lID09PSAnJykge1xuICAgICAgdmFyIF9yZXQyID0gZnVuY3Rpb24gKCkge1xuICAgICAgICB2YXIgbWF0Y2ggPSB7XG4gICAgICAgICAgcm91dGVzOiBbcm91dGVdLFxuICAgICAgICAgIHBhcmFtczogY3JlYXRlUGFyYW1zKHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKVxuICAgICAgICB9O1xuXG4gICAgICAgIGdldEluZGV4Um91dGUocm91dGUsIGxvY2F0aW9uLCBwYXJhbU5hbWVzLCBwYXJhbVZhbHVlcywgZnVuY3Rpb24gKGVycm9yLCBpbmRleFJvdXRlKSB7XG4gICAgICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgICAgICBjYWxsYmFjayhlcnJvcik7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGlmIChBcnJheS5pc0FycmF5KGluZGV4Um91dGUpKSB7XG4gICAgICAgICAgICAgIHZhciBfbWF0Y2gkcm91dGVzO1xuXG4gICAgICAgICAgICAgIFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgPyAoMCwgX3JvdXRlcldhcm5pbmcyLmRlZmF1bHQpKGluZGV4Um91dGUuZXZlcnkoZnVuY3Rpb24gKHJvdXRlKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuICFyb3V0ZS5wYXRoO1xuICAgICAgICAgICAgICB9KSwgJ0luZGV4IHJvdXRlcyBzaG91bGQgbm90IGhhdmUgcGF0aHMnKSA6IHZvaWQgMDtcbiAgICAgICAgICAgICAgKF9tYXRjaCRyb3V0ZXMgPSBtYXRjaC5yb3V0ZXMpLnB1c2guYXBwbHkoX21hdGNoJHJvdXRlcywgaW5kZXhSb3V0ZSk7XG4gICAgICAgICAgICB9IGVsc2UgaWYgKGluZGV4Um91dGUpIHtcbiAgICAgICAgICAgICAgXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyA/ICgwLCBfcm91dGVyV2FybmluZzIuZGVmYXVsdCkoIWluZGV4Um91dGUucGF0aCwgJ0luZGV4IHJvdXRlcyBzaG91bGQgbm90IGhhdmUgcGF0aHMnKSA6IHZvaWQgMDtcbiAgICAgICAgICAgICAgbWF0Y2gucm91dGVzLnB1c2goaW5kZXhSb3V0ZSk7XG4gICAgICAgICAgICB9XG5cbiAgICAgICAgICAgIGNhbGxiYWNrKG51bGwsIG1hdGNoKTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuXG4gICAgICAgIHJldHVybiB7XG4gICAgICAgICAgdjogdm9pZCAwXG4gICAgICAgIH07XG4gICAgICB9KCk7XG5cbiAgICAgIGlmICgodHlwZW9mIF9yZXQyID09PSAndW5kZWZpbmVkJyA/ICd1bmRlZmluZWQnIDogX3R5cGVvZihfcmV0MikpID09PSBcIm9iamVjdFwiKSByZXR1cm4gX3JldDIudjtcbiAgICB9XG4gIH1cblxuICBpZiAocmVtYWluaW5nUGF0aG5hbWUgIT0gbnVsbCB8fCByb3V0ZS5jaGlsZFJvdXRlcykge1xuICAgIC8vIEVpdGhlciBhKSB0aGlzIHJvdXRlIG1hdGNoZWQgYXQgbGVhc3Qgc29tZSBvZiB0aGUgcGF0aCBvciBiKVxuICAgIC8vIHdlIGRvbid0IGhhdmUgdG8gbG9hZCB0aGlzIHJvdXRlJ3MgY2hpbGRyZW4gYXN5bmNocm9ub3VzbHkuIEluXG4gICAgLy8gZWl0aGVyIGNhc2UgY29udGludWUgY2hlY2tpbmcgZm9yIG1hdGNoZXMgaW4gdGhlIHN1YnRyZWUuXG4gICAgdmFyIG9uQ2hpbGRSb3V0ZXMgPSBmdW5jdGlvbiBvbkNoaWxkUm91dGVzKGVycm9yLCBjaGlsZFJvdXRlcykge1xuICAgICAgaWYgKGVycm9yKSB7XG4gICAgICAgIGNhbGxiYWNrKGVycm9yKTtcbiAgICAgIH0gZWxzZSBpZiAoY2hpbGRSb3V0ZXMpIHtcbiAgICAgICAgLy8gQ2hlY2sgdGhlIGNoaWxkIHJvdXRlcyB0byBzZWUgaWYgYW55IG9mIHRoZW0gbWF0Y2guXG4gICAgICAgIG1hdGNoUm91dGVzKGNoaWxkUm91dGVzLCBsb2NhdGlvbiwgZnVuY3Rpb24gKGVycm9yLCBtYXRjaCkge1xuICAgICAgICAgIGlmIChlcnJvcikge1xuICAgICAgICAgICAgY2FsbGJhY2soZXJyb3IpO1xuICAgICAgICAgIH0gZWxzZSBpZiAobWF0Y2gpIHtcbiAgICAgICAgICAgIC8vIEEgY2hpbGQgcm91dGUgbWF0Y2hlZCEgQXVnbWVudCB0aGUgbWF0Y2ggYW5kIHBhc3MgaXQgdXAgdGhlIHN0YWNrLlxuICAgICAgICAgICAgbWF0Y2gucm91dGVzLnVuc2hpZnQocm91dGUpO1xuICAgICAgICAgICAgY2FsbGJhY2sobnVsbCwgbWF0Y2gpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBjYWxsYmFjaygpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSwgcmVtYWluaW5nUGF0aG5hbWUsIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIGNhbGxiYWNrKCk7XG4gICAgICB9XG4gICAgfTtcblxuICAgIHZhciByZXN1bHQgPSBnZXRDaGlsZFJvdXRlcyhyb3V0ZSwgbG9jYXRpb24sIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzLCBvbkNoaWxkUm91dGVzKTtcbiAgICBpZiAocmVzdWx0KSB7XG4gICAgICBvbkNoaWxkUm91dGVzLmFwcGx5KHVuZGVmaW5lZCwgcmVzdWx0KTtcbiAgICB9XG4gIH0gZWxzZSB7XG4gICAgY2FsbGJhY2soKTtcbiAgfVxufVxuXG4vKipcbiAqIEFzeW5jaHJvbm91c2x5IG1hdGNoZXMgdGhlIGdpdmVuIGxvY2F0aW9uIHRvIGEgc2V0IG9mIHJvdXRlcyBhbmQgY2FsbHNcbiAqIGNhbGxiYWNrKGVycm9yLCBzdGF0ZSkgd2hlbiBmaW5pc2hlZC4gVGhlIHN0YXRlIG9iamVjdCB3aWxsIGhhdmUgdGhlXG4gKiBmb2xsb3dpbmcgcHJvcGVydGllczpcbiAqXG4gKiAtIHJvdXRlcyAgICAgICBBbiBhcnJheSBvZiByb3V0ZXMgdGhhdCBtYXRjaGVkLCBpbiBoaWVyYXJjaGljYWwgb3JkZXJcbiAqIC0gcGFyYW1zICAgICAgIEFuIG9iamVjdCBvZiBVUkwgcGFyYW1ldGVyc1xuICpcbiAqIE5vdGU6IFRoaXMgb3BlcmF0aW9uIG1heSBmaW5pc2ggc3luY2hyb25vdXNseSBpZiBubyByb3V0ZXMgaGF2ZSBhblxuICogYXN5bmNocm9ub3VzIGdldENoaWxkUm91dGVzIG1ldGhvZC5cbiAqL1xuZnVuY3Rpb24gbWF0Y2hSb3V0ZXMocm91dGVzLCBsb2NhdGlvbiwgY2FsbGJhY2ssIHJlbWFpbmluZ1BhdGhuYW1lKSB7XG4gIHZhciBwYXJhbU5hbWVzID0gYXJndW1lbnRzLmxlbmd0aCA8PSA0IHx8IGFyZ3VtZW50c1s0XSA9PT0gdW5kZWZpbmVkID8gW10gOiBhcmd1bWVudHNbNF07XG4gIHZhciBwYXJhbVZhbHVlcyA9IGFyZ3VtZW50cy5sZW5ndGggPD0gNSB8fCBhcmd1bWVudHNbNV0gPT09IHVuZGVmaW5lZCA/IFtdIDogYXJndW1lbnRzWzVdO1xuXG4gIGlmIChyZW1haW5pbmdQYXRobmFtZSA9PT0gdW5kZWZpbmVkKSB7XG4gICAgLy8gVE9ETzogVGhpcyBpcyBhIGxpdHRsZSBiaXQgdWdseSwgYnV0IGl0IHdvcmtzIGFyb3VuZCBhIHF1aXJrIGluIGhpc3RvcnlcbiAgICAvLyB0aGF0IHN0cmlwcyB0aGUgbGVhZGluZyBzbGFzaCBmcm9tIHBhdGhuYW1lcyB3aGVuIHVzaW5nIGJhc2VuYW1lcyB3aXRoXG4gICAgLy8gdHJhaWxpbmcgc2xhc2hlcy5cbiAgICBpZiAobG9jYXRpb24ucGF0aG5hbWUuY2hhckF0KDApICE9PSAnLycpIHtcbiAgICAgIGxvY2F0aW9uID0gX2V4dGVuZHMoe30sIGxvY2F0aW9uLCB7XG4gICAgICAgIHBhdGhuYW1lOiAnLycgKyBsb2NhdGlvbi5wYXRobmFtZVxuICAgICAgfSk7XG4gICAgfVxuICAgIHJlbWFpbmluZ1BhdGhuYW1lID0gbG9jYXRpb24ucGF0aG5hbWU7XG4gIH1cblxuICAoMCwgX0FzeW5jVXRpbHMubG9vcEFzeW5jKShyb3V0ZXMubGVuZ3RoLCBmdW5jdGlvbiAoaW5kZXgsIG5leHQsIGRvbmUpIHtcbiAgICBtYXRjaFJvdXRlRGVlcChyb3V0ZXNbaW5kZXhdLCBsb2NhdGlvbiwgcmVtYWluaW5nUGF0aG5hbWUsIHBhcmFtTmFtZXMsIHBhcmFtVmFsdWVzLCBmdW5jdGlvbiAoZXJyb3IsIG1hdGNoKSB7XG4gICAgICBpZiAoZXJyb3IgfHwgbWF0Y2gpIHtcbiAgICAgICAgZG9uZShlcnJvciwgbWF0Y2gpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgbmV4dCgpO1xuICAgICAgfVxuICAgIH0pO1xuICB9LCBjYWxsYmFjayk7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmRlZmF1bHQgPSByb3V0ZXJXYXJuaW5nO1xuZXhwb3J0cy5fcmVzZXRXYXJuZWQgPSBfcmVzZXRXYXJuZWQ7XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJ3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBkZWZhdWx0OiBvYmogfTsgfVxuXG52YXIgd2FybmVkID0ge307XG5cbmZ1bmN0aW9uIHJvdXRlcldhcm5pbmcoZmFsc2VUb1dhcm4sIG1lc3NhZ2UpIHtcbiAgLy8gT25seSBpc3N1ZSBkZXByZWNhdGlvbiB3YXJuaW5ncyBvbmNlLlxuICBpZiAobWVzc2FnZS5pbmRleE9mKCdkZXByZWNhdGVkJykgIT09IC0xKSB7XG4gICAgaWYgKHdhcm5lZFttZXNzYWdlXSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIHdhcm5lZFttZXNzYWdlXSA9IHRydWU7XG4gIH1cblxuICBtZXNzYWdlID0gJ1tyZWFjdC1yb3V0ZXJdICcgKyBtZXNzYWdlO1xuXG4gIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBhcmdzID0gQXJyYXkoX2xlbiA+IDIgPyBfbGVuIC0gMiA6IDApLCBfa2V5ID0gMjsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgIGFyZ3NbX2tleSAtIDJdID0gYXJndW1lbnRzW19rZXldO1xuICB9XG5cbiAgX3dhcm5pbmcyLmRlZmF1bHQuYXBwbHkodW5kZWZpbmVkLCBbZmFsc2VUb1dhcm4sIG1lc3NhZ2VdLmNvbmNhdChhcmdzKSk7XG59XG5cbmZ1bmN0aW9uIF9yZXNldFdhcm5lZCgpIHtcbiAgd2FybmVkID0ge307XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5kZWZhdWx0ID0gdXNlUm91dGVySGlzdG9yeTtcblxudmFyIF91c2VRdWVyaWVzID0gcmVxdWlyZSgnaGlzdG9yeS9saWIvdXNlUXVlcmllcycpO1xuXG52YXIgX3VzZVF1ZXJpZXMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdXNlUXVlcmllcyk7XG5cbnZhciBfdXNlQmFzZW5hbWUgPSByZXF1aXJlKCdoaXN0b3J5L2xpYi91c2VCYXNlbmFtZScpO1xuXG52YXIgX3VzZUJhc2VuYW1lMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZUJhc2VuYW1lKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gdXNlUm91dGVySGlzdG9yeShjcmVhdGVIaXN0b3J5KSB7XG4gIHJldHVybiBmdW5jdGlvbiAob3B0aW9ucykge1xuICAgIHZhciBoaXN0b3J5ID0gKDAsIF91c2VRdWVyaWVzMi5kZWZhdWx0KSgoMCwgX3VzZUJhc2VuYW1lMi5kZWZhdWx0KShjcmVhdGVIaXN0b3J5KSkob3B0aW9ucyk7XG4gICAgaGlzdG9yeS5fX3YyX2NvbXBhdGlibGVfXyA9IHRydWU7XG4gICAgcmV0dXJuIGhpc3Rvcnk7XG4gIH07XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbnZhciBfdXNlUXVlcmllcyA9IHJlcXVpcmUoJ2hpc3RvcnkvbGliL3VzZVF1ZXJpZXMnKTtcblxudmFyIF91c2VRdWVyaWVzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3VzZVF1ZXJpZXMpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyID0gcmVxdWlyZSgnLi9jcmVhdGVUcmFuc2l0aW9uTWFuYWdlcicpO1xuXG52YXIgX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyKTtcblxudmFyIF9yb3V0ZXJXYXJuaW5nID0gcmVxdWlyZSgnLi9yb3V0ZXJXYXJuaW5nJyk7XG5cbnZhciBfcm91dGVyV2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9yb3V0ZXJXYXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKG9iaiwga2V5cykgeyB2YXIgdGFyZ2V0ID0ge307IGZvciAodmFyIGkgaW4gb2JqKSB7IGlmIChrZXlzLmluZGV4T2YoaSkgPj0gMCkgY29udGludWU7IGlmICghT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKG9iaiwgaSkpIGNvbnRpbnVlOyB0YXJnZXRbaV0gPSBvYmpbaV07IH0gcmV0dXJuIHRhcmdldDsgfVxuXG4vKipcbiAqIFJldHVybnMgYSBuZXcgY3JlYXRlSGlzdG9yeSBmdW5jdGlvbiB0aGF0IG1heSBiZSB1c2VkIHRvIGNyZWF0ZVxuICogaGlzdG9yeSBvYmplY3RzIHRoYXQga25vdyBhYm91dCByb3V0aW5nLlxuICpcbiAqIEVuaGFuY2VzIGhpc3Rvcnkgb2JqZWN0cyB3aXRoIHRoZSBmb2xsb3dpbmcgbWV0aG9kczpcbiAqXG4gKiAtIGxpc3RlbigoZXJyb3IsIG5leHRTdGF0ZSkgPT4ge30pXG4gKiAtIGxpc3RlbkJlZm9yZUxlYXZpbmdSb3V0ZShyb3V0ZSwgKG5leHRMb2NhdGlvbikgPT4ge30pXG4gKiAtIG1hdGNoKGxvY2F0aW9uLCAoZXJyb3IsIHJlZGlyZWN0TG9jYXRpb24sIG5leHRTdGF0ZSkgPT4ge30pXG4gKiAtIGlzQWN0aXZlKHBhdGhuYW1lLCBxdWVyeSwgaW5kZXhPbmx5PWZhbHNlKVxuICovXG5mdW5jdGlvbiB1c2VSb3V0ZXMoY3JlYXRlSGlzdG9yeSkge1xuICBcInByb2R1Y3Rpb25cIiAhPT0gJ3Byb2R1Y3Rpb24nID8gKDAsIF9yb3V0ZXJXYXJuaW5nMi5kZWZhdWx0KShmYWxzZSwgJ2B1c2VSb3V0ZXNgIGlzIGRlcHJlY2F0ZWQuIFBsZWFzZSB1c2UgYGNyZWF0ZVRyYW5zaXRpb25NYW5hZ2VyYCBpbnN0ZWFkLicpIDogdm9pZCAwO1xuXG4gIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgdmFyIF9yZWYgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcblxuICAgIHZhciByb3V0ZXMgPSBfcmVmLnJvdXRlcztcblxuICAgIHZhciBvcHRpb25zID0gX29iamVjdFdpdGhvdXRQcm9wZXJ0aWVzKF9yZWYsIFsncm91dGVzJ10pO1xuXG4gICAgdmFyIGhpc3RvcnkgPSAoMCwgX3VzZVF1ZXJpZXMyLmRlZmF1bHQpKGNyZWF0ZUhpc3RvcnkpKG9wdGlvbnMpO1xuICAgIHZhciB0cmFuc2l0aW9uTWFuYWdlciA9ICgwLCBfY3JlYXRlVHJhbnNpdGlvbk1hbmFnZXIyLmRlZmF1bHQpKGhpc3RvcnksIHJvdXRlcyk7XG4gICAgcmV0dXJuIF9leHRlbmRzKHt9LCBoaXN0b3J5LCB0cmFuc2l0aW9uTWFuYWdlcik7XG4gIH07XG59XG5cbmV4cG9ydHMuZGVmYXVsdCA9IHVzZVJvdXRlcztcbm1vZHVsZS5leHBvcnRzID0gZXhwb3J0c1snZGVmYXVsdCddOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZXhwb3J0cy5kZWZhdWx0ID0gd2l0aFJvdXRlcjtcblxudmFyIF9yZWFjdCA9IHJlcXVpcmUoJ3JlYWN0Jyk7XG5cbnZhciBfcmVhY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfcmVhY3QpO1xuXG52YXIgX2hvaXN0Tm9uUmVhY3RTdGF0aWNzID0gcmVxdWlyZSgnaG9pc3Qtbm9uLXJlYWN0LXN0YXRpY3MnKTtcblxudmFyIF9ob2lzdE5vblJlYWN0U3RhdGljczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9ob2lzdE5vblJlYWN0U3RhdGljcyk7XG5cbnZhciBfUHJvcFR5cGVzID0gcmVxdWlyZSgnLi9Qcm9wVHlwZXMnKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgZGVmYXVsdDogb2JqIH07IH1cblxuZnVuY3Rpb24gZ2V0RGlzcGxheU5hbWUoV3JhcHBlZENvbXBvbmVudCkge1xuICByZXR1cm4gV3JhcHBlZENvbXBvbmVudC5kaXNwbGF5TmFtZSB8fCBXcmFwcGVkQ29tcG9uZW50Lm5hbWUgfHwgJ0NvbXBvbmVudCc7XG59XG5cbmZ1bmN0aW9uIHdpdGhSb3V0ZXIoV3JhcHBlZENvbXBvbmVudCkge1xuICB2YXIgV2l0aFJvdXRlciA9IF9yZWFjdDIuZGVmYXVsdC5jcmVhdGVDbGFzcyh7XG4gICAgZGlzcGxheU5hbWU6ICdXaXRoUm91dGVyJyxcblxuICAgIGNvbnRleHRUeXBlczogeyByb3V0ZXI6IF9Qcm9wVHlwZXMucm91dGVyU2hhcGUgfSxcbiAgICByZW5kZXI6IGZ1bmN0aW9uIHJlbmRlcigpIHtcbiAgICAgIHJldHVybiBfcmVhY3QyLmRlZmF1bHQuY3JlYXRlRWxlbWVudChXcmFwcGVkQ29tcG9uZW50LCBfZXh0ZW5kcyh7fSwgdGhpcy5wcm9wcywgeyByb3V0ZXI6IHRoaXMuY29udGV4dC5yb3V0ZXIgfSkpO1xuICAgIH1cbiAgfSk7XG5cbiAgV2l0aFJvdXRlci5kaXNwbGF5TmFtZSA9ICd3aXRoUm91dGVyKCcgKyBnZXREaXNwbGF5TmFtZShXcmFwcGVkQ29tcG9uZW50KSArICcpJztcbiAgV2l0aFJvdXRlci5XcmFwcGVkQ29tcG9uZW50ID0gV3JhcHBlZENvbXBvbmVudDtcblxuICByZXR1cm4gKDAsIF9ob2lzdE5vblJlYWN0U3RhdGljczIuZGVmYXVsdCkoV2l0aFJvdXRlciwgV3JhcHBlZENvbXBvbmVudCk7XG59XG5tb2R1bGUuZXhwb3J0cyA9IGV4cG9ydHNbJ2RlZmF1bHQnXTsiLCIndXNlIHN0cmljdCc7XG5cbmZ1bmN0aW9uIHRodW5rTWlkZGxld2FyZShfcmVmKSB7XG4gIHZhciBkaXNwYXRjaCA9IF9yZWYuZGlzcGF0Y2g7XG4gIHZhciBnZXRTdGF0ZSA9IF9yZWYuZ2V0U3RhdGU7XG5cbiAgcmV0dXJuIGZ1bmN0aW9uIChuZXh0KSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChhY3Rpb24pIHtcbiAgICAgIHJldHVybiB0eXBlb2YgYWN0aW9uID09PSAnZnVuY3Rpb24nID8gYWN0aW9uKGRpc3BhdGNoLCBnZXRTdGF0ZSkgOiBuZXh0KGFjdGlvbik7XG4gICAgfTtcbiAgfTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSB0aHVua01pZGRsZXdhcmU7IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuXG52YXIgX2V4dGVuZHMgPSBPYmplY3QuYXNzaWduIHx8IGZ1bmN0aW9uICh0YXJnZXQpIHsgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHsgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXTsgZm9yICh2YXIga2V5IGluIHNvdXJjZSkgeyBpZiAoT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkgeyB0YXJnZXRba2V5XSA9IHNvdXJjZVtrZXldOyB9IH0gfSByZXR1cm4gdGFyZ2V0OyB9O1xuXG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGFwcGx5TWlkZGxld2FyZTtcblxudmFyIF9jb21wb3NlID0gcmVxdWlyZSgnLi9jb21wb3NlJyk7XG5cbnZhciBfY29tcG9zZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jb21wb3NlKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbi8qKlxuICogQ3JlYXRlcyBhIHN0b3JlIGVuaGFuY2VyIHRoYXQgYXBwbGllcyBtaWRkbGV3YXJlIHRvIHRoZSBkaXNwYXRjaCBtZXRob2RcbiAqIG9mIHRoZSBSZWR1eCBzdG9yZS4gVGhpcyBpcyBoYW5keSBmb3IgYSB2YXJpZXR5IG9mIHRhc2tzLCBzdWNoIGFzIGV4cHJlc3NpbmdcbiAqIGFzeW5jaHJvbm91cyBhY3Rpb25zIGluIGEgY29uY2lzZSBtYW5uZXIsIG9yIGxvZ2dpbmcgZXZlcnkgYWN0aW9uIHBheWxvYWQuXG4gKlxuICogU2VlIGByZWR1eC10aHVua2AgcGFja2FnZSBhcyBhbiBleGFtcGxlIG9mIHRoZSBSZWR1eCBtaWRkbGV3YXJlLlxuICpcbiAqIEJlY2F1c2UgbWlkZGxld2FyZSBpcyBwb3RlbnRpYWxseSBhc3luY2hyb25vdXMsIHRoaXMgc2hvdWxkIGJlIHRoZSBmaXJzdFxuICogc3RvcmUgZW5oYW5jZXIgaW4gdGhlIGNvbXBvc2l0aW9uIGNoYWluLlxuICpcbiAqIE5vdGUgdGhhdCBlYWNoIG1pZGRsZXdhcmUgd2lsbCBiZSBnaXZlbiB0aGUgYGRpc3BhdGNoYCBhbmQgYGdldFN0YXRlYCBmdW5jdGlvbnNcbiAqIGFzIG5hbWVkIGFyZ3VtZW50cy5cbiAqXG4gKiBAcGFyYW0gey4uLkZ1bmN0aW9ufSBtaWRkbGV3YXJlcyBUaGUgbWlkZGxld2FyZSBjaGFpbiB0byBiZSBhcHBsaWVkLlxuICogQHJldHVybnMge0Z1bmN0aW9ufSBBIHN0b3JlIGVuaGFuY2VyIGFwcGx5aW5nIHRoZSBtaWRkbGV3YXJlLlxuICovXG5mdW5jdGlvbiBhcHBseU1pZGRsZXdhcmUoKSB7XG4gIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBtaWRkbGV3YXJlcyA9IEFycmF5KF9sZW4pLCBfa2V5ID0gMDsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgIG1pZGRsZXdhcmVzW19rZXldID0gYXJndW1lbnRzW19rZXldO1xuICB9XG5cbiAgcmV0dXJuIGZ1bmN0aW9uIChjcmVhdGVTdG9yZSkge1xuICAgIHJldHVybiBmdW5jdGlvbiAocmVkdWNlciwgaW5pdGlhbFN0YXRlLCBlbmhhbmNlcikge1xuICAgICAgdmFyIHN0b3JlID0gY3JlYXRlU3RvcmUocmVkdWNlciwgaW5pdGlhbFN0YXRlLCBlbmhhbmNlcik7XG4gICAgICB2YXIgX2Rpc3BhdGNoID0gc3RvcmUuZGlzcGF0Y2g7XG4gICAgICB2YXIgY2hhaW4gPSBbXTtcblxuICAgICAgdmFyIG1pZGRsZXdhcmVBUEkgPSB7XG4gICAgICAgIGdldFN0YXRlOiBzdG9yZS5nZXRTdGF0ZSxcbiAgICAgICAgZGlzcGF0Y2g6IGZ1bmN0aW9uIGRpc3BhdGNoKGFjdGlvbikge1xuICAgICAgICAgIHJldHVybiBfZGlzcGF0Y2goYWN0aW9uKTtcbiAgICAgICAgfVxuICAgICAgfTtcbiAgICAgIGNoYWluID0gbWlkZGxld2FyZXMubWFwKGZ1bmN0aW9uIChtaWRkbGV3YXJlKSB7XG4gICAgICAgIHJldHVybiBtaWRkbGV3YXJlKG1pZGRsZXdhcmVBUEkpO1xuICAgICAgfSk7XG4gICAgICBfZGlzcGF0Y2ggPSBfY29tcG9zZTJbXCJkZWZhdWx0XCJdLmFwcGx5KHVuZGVmaW5lZCwgY2hhaW4pKHN0b3JlLmRpc3BhdGNoKTtcblxuICAgICAgcmV0dXJuIF9leHRlbmRzKHt9LCBzdG9yZSwge1xuICAgICAgICBkaXNwYXRjaDogX2Rpc3BhdGNoXG4gICAgICB9KTtcbiAgICB9O1xuICB9O1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gYmluZEFjdGlvbkNyZWF0b3JzO1xuZnVuY3Rpb24gYmluZEFjdGlvbkNyZWF0b3IoYWN0aW9uQ3JlYXRvciwgZGlzcGF0Y2gpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uICgpIHtcbiAgICByZXR1cm4gZGlzcGF0Y2goYWN0aW9uQ3JlYXRvci5hcHBseSh1bmRlZmluZWQsIGFyZ3VtZW50cykpO1xuICB9O1xufVxuXG4vKipcbiAqIFR1cm5zIGFuIG9iamVjdCB3aG9zZSB2YWx1ZXMgYXJlIGFjdGlvbiBjcmVhdG9ycywgaW50byBhbiBvYmplY3Qgd2l0aCB0aGVcbiAqIHNhbWUga2V5cywgYnV0IHdpdGggZXZlcnkgZnVuY3Rpb24gd3JhcHBlZCBpbnRvIGEgYGRpc3BhdGNoYCBjYWxsIHNvIHRoZXlcbiAqIG1heSBiZSBpbnZva2VkIGRpcmVjdGx5LiBUaGlzIGlzIGp1c3QgYSBjb252ZW5pZW5jZSBtZXRob2QsIGFzIHlvdSBjYW4gY2FsbFxuICogYHN0b3JlLmRpc3BhdGNoKE15QWN0aW9uQ3JlYXRvcnMuZG9Tb21ldGhpbmcoKSlgIHlvdXJzZWxmIGp1c3QgZmluZS5cbiAqXG4gKiBGb3IgY29udmVuaWVuY2UsIHlvdSBjYW4gYWxzbyBwYXNzIGEgc2luZ2xlIGZ1bmN0aW9uIGFzIHRoZSBmaXJzdCBhcmd1bWVudCxcbiAqIGFuZCBnZXQgYSBmdW5jdGlvbiBpbiByZXR1cm4uXG4gKlxuICogQHBhcmFtIHtGdW5jdGlvbnxPYmplY3R9IGFjdGlvbkNyZWF0b3JzIEFuIG9iamVjdCB3aG9zZSB2YWx1ZXMgYXJlIGFjdGlvblxuICogY3JlYXRvciBmdW5jdGlvbnMuIE9uZSBoYW5keSB3YXkgdG8gb2J0YWluIGl0IGlzIHRvIHVzZSBFUzYgYGltcG9ydCAqIGFzYFxuICogc3ludGF4LiBZb3UgbWF5IGFsc28gcGFzcyBhIHNpbmdsZSBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBkaXNwYXRjaCBUaGUgYGRpc3BhdGNoYCBmdW5jdGlvbiBhdmFpbGFibGUgb24geW91ciBSZWR1eFxuICogc3RvcmUuXG4gKlxuICogQHJldHVybnMge0Z1bmN0aW9ufE9iamVjdH0gVGhlIG9iamVjdCBtaW1pY2tpbmcgdGhlIG9yaWdpbmFsIG9iamVjdCwgYnV0IHdpdGhcbiAqIGV2ZXJ5IGFjdGlvbiBjcmVhdG9yIHdyYXBwZWQgaW50byB0aGUgYGRpc3BhdGNoYCBjYWxsLiBJZiB5b3UgcGFzc2VkIGFcbiAqIGZ1bmN0aW9uIGFzIGBhY3Rpb25DcmVhdG9yc2AsIHRoZSByZXR1cm4gdmFsdWUgd2lsbCBhbHNvIGJlIGEgc2luZ2xlXG4gKiBmdW5jdGlvbi5cbiAqL1xuZnVuY3Rpb24gYmluZEFjdGlvbkNyZWF0b3JzKGFjdGlvbkNyZWF0b3JzLCBkaXNwYXRjaCkge1xuICBpZiAodHlwZW9mIGFjdGlvbkNyZWF0b3JzID09PSAnZnVuY3Rpb24nKSB7XG4gICAgcmV0dXJuIGJpbmRBY3Rpb25DcmVhdG9yKGFjdGlvbkNyZWF0b3JzLCBkaXNwYXRjaCk7XG4gIH1cblxuICBpZiAodHlwZW9mIGFjdGlvbkNyZWF0b3JzICE9PSAnb2JqZWN0JyB8fCBhY3Rpb25DcmVhdG9ycyA9PT0gbnVsbCkge1xuICAgIHRocm93IG5ldyBFcnJvcignYmluZEFjdGlvbkNyZWF0b3JzIGV4cGVjdGVkIGFuIG9iamVjdCBvciBhIGZ1bmN0aW9uLCBpbnN0ZWFkIHJlY2VpdmVkICcgKyAoYWN0aW9uQ3JlYXRvcnMgPT09IG51bGwgPyAnbnVsbCcgOiB0eXBlb2YgYWN0aW9uQ3JlYXRvcnMpICsgJy4gJyArICdEaWQgeW91IHdyaXRlIFwiaW1wb3J0IEFjdGlvbkNyZWF0b3JzIGZyb21cIiBpbnN0ZWFkIG9mIFwiaW1wb3J0ICogYXMgQWN0aW9uQ3JlYXRvcnMgZnJvbVwiPycpO1xuICB9XG5cbiAgdmFyIGtleXMgPSBPYmplY3Qua2V5cyhhY3Rpb25DcmVhdG9ycyk7XG4gIHZhciBib3VuZEFjdGlvbkNyZWF0b3JzID0ge307XG4gIGZvciAodmFyIGkgPSAwOyBpIDwga2V5cy5sZW5ndGg7IGkrKykge1xuICAgIHZhciBrZXkgPSBrZXlzW2ldO1xuICAgIHZhciBhY3Rpb25DcmVhdG9yID0gYWN0aW9uQ3JlYXRvcnNba2V5XTtcbiAgICBpZiAodHlwZW9mIGFjdGlvbkNyZWF0b3IgPT09ICdmdW5jdGlvbicpIHtcbiAgICAgIGJvdW5kQWN0aW9uQ3JlYXRvcnNba2V5XSA9IGJpbmRBY3Rpb25DcmVhdG9yKGFjdGlvbkNyZWF0b3IsIGRpc3BhdGNoKTtcbiAgICB9XG4gIH1cbiAgcmV0dXJuIGJvdW5kQWN0aW9uQ3JlYXRvcnM7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBjb21iaW5lUmVkdWNlcnM7XG5cbnZhciBfY3JlYXRlU3RvcmUgPSByZXF1aXJlKCcuL2NyZWF0ZVN0b3JlJyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdCA9IHJlcXVpcmUoJ2xvZGFzaC9pc1BsYWluT2JqZWN0Jyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pc1BsYWluT2JqZWN0KTtcblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnLi91dGlscy93YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbmZ1bmN0aW9uIGdldFVuZGVmaW5lZFN0YXRlRXJyb3JNZXNzYWdlKGtleSwgYWN0aW9uKSB7XG4gIHZhciBhY3Rpb25UeXBlID0gYWN0aW9uICYmIGFjdGlvbi50eXBlO1xuICB2YXIgYWN0aW9uTmFtZSA9IGFjdGlvblR5cGUgJiYgJ1wiJyArIGFjdGlvblR5cGUudG9TdHJpbmcoKSArICdcIicgfHwgJ2FuIGFjdGlvbic7XG5cbiAgcmV0dXJuICdHaXZlbiBhY3Rpb24gJyArIGFjdGlvbk5hbWUgKyAnLCByZWR1Y2VyIFwiJyArIGtleSArICdcIiByZXR1cm5lZCB1bmRlZmluZWQuICcgKyAnVG8gaWdub3JlIGFuIGFjdGlvbiwgeW91IG11c3QgZXhwbGljaXRseSByZXR1cm4gdGhlIHByZXZpb3VzIHN0YXRlLic7XG59XG5cbmZ1bmN0aW9uIGdldFVuZXhwZWN0ZWRTdGF0ZVNoYXBlV2FybmluZ01lc3NhZ2UoaW5wdXRTdGF0ZSwgcmVkdWNlcnMsIGFjdGlvbikge1xuICB2YXIgcmVkdWNlcktleXMgPSBPYmplY3Qua2V5cyhyZWR1Y2Vycyk7XG4gIHZhciBhcmd1bWVudE5hbWUgPSBhY3Rpb24gJiYgYWN0aW9uLnR5cGUgPT09IF9jcmVhdGVTdG9yZS5BY3Rpb25UeXBlcy5JTklUID8gJ2luaXRpYWxTdGF0ZSBhcmd1bWVudCBwYXNzZWQgdG8gY3JlYXRlU3RvcmUnIDogJ3ByZXZpb3VzIHN0YXRlIHJlY2VpdmVkIGJ5IHRoZSByZWR1Y2VyJztcblxuICBpZiAocmVkdWNlcktleXMubGVuZ3RoID09PSAwKSB7XG4gICAgcmV0dXJuICdTdG9yZSBkb2VzIG5vdCBoYXZlIGEgdmFsaWQgcmVkdWNlci4gTWFrZSBzdXJlIHRoZSBhcmd1bWVudCBwYXNzZWQgJyArICd0byBjb21iaW5lUmVkdWNlcnMgaXMgYW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgcmVkdWNlcnMuJztcbiAgfVxuXG4gIGlmICghKDAsIF9pc1BsYWluT2JqZWN0MltcImRlZmF1bHRcIl0pKGlucHV0U3RhdGUpKSB7XG4gICAgcmV0dXJuICdUaGUgJyArIGFyZ3VtZW50TmFtZSArICcgaGFzIHVuZXhwZWN0ZWQgdHlwZSBvZiBcIicgKyB7fS50b1N0cmluZy5jYWxsKGlucHV0U3RhdGUpLm1hdGNoKC9cXHMoW2EtenxBLVpdKykvKVsxXSArICdcIi4gRXhwZWN0ZWQgYXJndW1lbnQgdG8gYmUgYW4gb2JqZWN0IHdpdGggdGhlIGZvbGxvd2luZyAnICsgKCdrZXlzOiBcIicgKyByZWR1Y2VyS2V5cy5qb2luKCdcIiwgXCInKSArICdcIicpO1xuICB9XG5cbiAgdmFyIHVuZXhwZWN0ZWRLZXlzID0gT2JqZWN0LmtleXMoaW5wdXRTdGF0ZSkuZmlsdGVyKGZ1bmN0aW9uIChrZXkpIHtcbiAgICByZXR1cm4gIXJlZHVjZXJzLmhhc093blByb3BlcnR5KGtleSk7XG4gIH0pO1xuXG4gIGlmICh1bmV4cGVjdGVkS2V5cy5sZW5ndGggPiAwKSB7XG4gICAgcmV0dXJuICdVbmV4cGVjdGVkICcgKyAodW5leHBlY3RlZEtleXMubGVuZ3RoID4gMSA/ICdrZXlzJyA6ICdrZXknKSArICcgJyArICgnXCInICsgdW5leHBlY3RlZEtleXMuam9pbignXCIsIFwiJykgKyAnXCIgZm91bmQgaW4gJyArIGFyZ3VtZW50TmFtZSArICcuICcpICsgJ0V4cGVjdGVkIHRvIGZpbmQgb25lIG9mIHRoZSBrbm93biByZWR1Y2VyIGtleXMgaW5zdGVhZDogJyArICgnXCInICsgcmVkdWNlcktleXMuam9pbignXCIsIFwiJykgKyAnXCIuIFVuZXhwZWN0ZWQga2V5cyB3aWxsIGJlIGlnbm9yZWQuJyk7XG4gIH1cbn1cblxuZnVuY3Rpb24gYXNzZXJ0UmVkdWNlclNhbml0eShyZWR1Y2Vycykge1xuICBPYmplY3Qua2V5cyhyZWR1Y2VycykuZm9yRWFjaChmdW5jdGlvbiAoa2V5KSB7XG4gICAgdmFyIHJlZHVjZXIgPSByZWR1Y2Vyc1trZXldO1xuICAgIHZhciBpbml0aWFsU3RhdGUgPSByZWR1Y2VyKHVuZGVmaW5lZCwgeyB0eXBlOiBfY3JlYXRlU3RvcmUuQWN0aW9uVHlwZXMuSU5JVCB9KTtcblxuICAgIGlmICh0eXBlb2YgaW5pdGlhbFN0YXRlID09PSAndW5kZWZpbmVkJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdSZWR1Y2VyIFwiJyArIGtleSArICdcIiByZXR1cm5lZCB1bmRlZmluZWQgZHVyaW5nIGluaXRpYWxpemF0aW9uLiAnICsgJ0lmIHRoZSBzdGF0ZSBwYXNzZWQgdG8gdGhlIHJlZHVjZXIgaXMgdW5kZWZpbmVkLCB5b3UgbXVzdCAnICsgJ2V4cGxpY2l0bHkgcmV0dXJuIHRoZSBpbml0aWFsIHN0YXRlLiBUaGUgaW5pdGlhbCBzdGF0ZSBtYXkgJyArICdub3QgYmUgdW5kZWZpbmVkLicpO1xuICAgIH1cblxuICAgIHZhciB0eXBlID0gJ0BAcmVkdXgvUFJPQkVfVU5LTk9XTl9BQ1RJT05fJyArIE1hdGgucmFuZG9tKCkudG9TdHJpbmcoMzYpLnN1YnN0cmluZyg3KS5zcGxpdCgnJykuam9pbignLicpO1xuICAgIGlmICh0eXBlb2YgcmVkdWNlcih1bmRlZmluZWQsIHsgdHlwZTogdHlwZSB9KSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignUmVkdWNlciBcIicgKyBrZXkgKyAnXCIgcmV0dXJuZWQgdW5kZWZpbmVkIHdoZW4gcHJvYmVkIHdpdGggYSByYW5kb20gdHlwZS4gJyArICgnRG9uXFwndCB0cnkgdG8gaGFuZGxlICcgKyBfY3JlYXRlU3RvcmUuQWN0aW9uVHlwZXMuSU5JVCArICcgb3Igb3RoZXIgYWN0aW9ucyBpbiBcInJlZHV4LypcIiAnKSArICduYW1lc3BhY2UuIFRoZXkgYXJlIGNvbnNpZGVyZWQgcHJpdmF0ZS4gSW5zdGVhZCwgeW91IG11c3QgcmV0dXJuIHRoZSAnICsgJ2N1cnJlbnQgc3RhdGUgZm9yIGFueSB1bmtub3duIGFjdGlvbnMsIHVubGVzcyBpdCBpcyB1bmRlZmluZWQsICcgKyAnaW4gd2hpY2ggY2FzZSB5b3UgbXVzdCByZXR1cm4gdGhlIGluaXRpYWwgc3RhdGUsIHJlZ2FyZGxlc3Mgb2YgdGhlICcgKyAnYWN0aW9uIHR5cGUuIFRoZSBpbml0aWFsIHN0YXRlIG1heSBub3QgYmUgdW5kZWZpbmVkLicpO1xuICAgIH1cbiAgfSk7XG59XG5cbi8qKlxuICogVHVybnMgYW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgZGlmZmVyZW50IHJlZHVjZXIgZnVuY3Rpb25zLCBpbnRvIGEgc2luZ2xlXG4gKiByZWR1Y2VyIGZ1bmN0aW9uLiBJdCB3aWxsIGNhbGwgZXZlcnkgY2hpbGQgcmVkdWNlciwgYW5kIGdhdGhlciB0aGVpciByZXN1bHRzXG4gKiBpbnRvIGEgc2luZ2xlIHN0YXRlIG9iamVjdCwgd2hvc2Uga2V5cyBjb3JyZXNwb25kIHRvIHRoZSBrZXlzIG9mIHRoZSBwYXNzZWRcbiAqIHJlZHVjZXIgZnVuY3Rpb25zLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSByZWR1Y2VycyBBbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGNvcnJlc3BvbmQgdG8gZGlmZmVyZW50XG4gKiByZWR1Y2VyIGZ1bmN0aW9ucyB0aGF0IG5lZWQgdG8gYmUgY29tYmluZWQgaW50byBvbmUuIE9uZSBoYW5keSB3YXkgdG8gb2J0YWluXG4gKiBpdCBpcyB0byB1c2UgRVM2IGBpbXBvcnQgKiBhcyByZWR1Y2Vyc2Agc3ludGF4LiBUaGUgcmVkdWNlcnMgbWF5IG5ldmVyIHJldHVyblxuICogdW5kZWZpbmVkIGZvciBhbnkgYWN0aW9uLiBJbnN0ZWFkLCB0aGV5IHNob3VsZCByZXR1cm4gdGhlaXIgaW5pdGlhbCBzdGF0ZVxuICogaWYgdGhlIHN0YXRlIHBhc3NlZCB0byB0aGVtIHdhcyB1bmRlZmluZWQsIGFuZCB0aGUgY3VycmVudCBzdGF0ZSBmb3IgYW55XG4gKiB1bnJlY29nbml6ZWQgYWN0aW9uLlxuICpcbiAqIEByZXR1cm5zIHtGdW5jdGlvbn0gQSByZWR1Y2VyIGZ1bmN0aW9uIHRoYXQgaW52b2tlcyBldmVyeSByZWR1Y2VyIGluc2lkZSB0aGVcbiAqIHBhc3NlZCBvYmplY3QsIGFuZCBidWlsZHMgYSBzdGF0ZSBvYmplY3Qgd2l0aCB0aGUgc2FtZSBzaGFwZS5cbiAqL1xuZnVuY3Rpb24gY29tYmluZVJlZHVjZXJzKHJlZHVjZXJzKSB7XG4gIHZhciByZWR1Y2VyS2V5cyA9IE9iamVjdC5rZXlzKHJlZHVjZXJzKTtcbiAgdmFyIGZpbmFsUmVkdWNlcnMgPSB7fTtcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCByZWR1Y2VyS2V5cy5sZW5ndGg7IGkrKykge1xuICAgIHZhciBrZXkgPSByZWR1Y2VyS2V5c1tpXTtcbiAgICBpZiAodHlwZW9mIHJlZHVjZXJzW2tleV0gPT09ICdmdW5jdGlvbicpIHtcbiAgICAgIGZpbmFsUmVkdWNlcnNba2V5XSA9IHJlZHVjZXJzW2tleV07XG4gICAgfVxuICB9XG4gIHZhciBmaW5hbFJlZHVjZXJLZXlzID0gT2JqZWN0LmtleXMoZmluYWxSZWR1Y2Vycyk7XG5cbiAgdmFyIHNhbml0eUVycm9yO1xuICB0cnkge1xuICAgIGFzc2VydFJlZHVjZXJTYW5pdHkoZmluYWxSZWR1Y2Vycyk7XG4gIH0gY2F0Y2ggKGUpIHtcbiAgICBzYW5pdHlFcnJvciA9IGU7XG4gIH1cblxuICByZXR1cm4gZnVuY3Rpb24gY29tYmluYXRpb24oKSB7XG4gICAgdmFyIHN0YXRlID0gYXJndW1lbnRzLmxlbmd0aCA8PSAwIHx8IGFyZ3VtZW50c1swXSA9PT0gdW5kZWZpbmVkID8ge30gOiBhcmd1bWVudHNbMF07XG4gICAgdmFyIGFjdGlvbiA9IGFyZ3VtZW50c1sxXTtcblxuICAgIGlmIChzYW5pdHlFcnJvcikge1xuICAgICAgdGhyb3cgc2FuaXR5RXJyb3I7XG4gICAgfVxuXG4gICAgaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicpIHtcbiAgICAgIHZhciB3YXJuaW5nTWVzc2FnZSA9IGdldFVuZXhwZWN0ZWRTdGF0ZVNoYXBlV2FybmluZ01lc3NhZ2Uoc3RhdGUsIGZpbmFsUmVkdWNlcnMsIGFjdGlvbik7XG4gICAgICBpZiAod2FybmluZ01lc3NhZ2UpIHtcbiAgICAgICAgKDAsIF93YXJuaW5nMltcImRlZmF1bHRcIl0pKHdhcm5pbmdNZXNzYWdlKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIgaGFzQ2hhbmdlZCA9IGZhbHNlO1xuICAgIHZhciBuZXh0U3RhdGUgPSB7fTtcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGZpbmFsUmVkdWNlcktleXMubGVuZ3RoOyBpKyspIHtcbiAgICAgIHZhciBrZXkgPSBmaW5hbFJlZHVjZXJLZXlzW2ldO1xuICAgICAgdmFyIHJlZHVjZXIgPSBmaW5hbFJlZHVjZXJzW2tleV07XG4gICAgICB2YXIgcHJldmlvdXNTdGF0ZUZvcktleSA9IHN0YXRlW2tleV07XG4gICAgICB2YXIgbmV4dFN0YXRlRm9yS2V5ID0gcmVkdWNlcihwcmV2aW91c1N0YXRlRm9yS2V5LCBhY3Rpb24pO1xuICAgICAgaWYgKHR5cGVvZiBuZXh0U3RhdGVGb3JLZXkgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICAgIHZhciBlcnJvck1lc3NhZ2UgPSBnZXRVbmRlZmluZWRTdGF0ZUVycm9yTWVzc2FnZShrZXksIGFjdGlvbik7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcihlcnJvck1lc3NhZ2UpO1xuICAgICAgfVxuICAgICAgbmV4dFN0YXRlW2tleV0gPSBuZXh0U3RhdGVGb3JLZXk7XG4gICAgICBoYXNDaGFuZ2VkID0gaGFzQ2hhbmdlZCB8fCBuZXh0U3RhdGVGb3JLZXkgIT09IHByZXZpb3VzU3RhdGVGb3JLZXk7XG4gICAgfVxuICAgIHJldHVybiBoYXNDaGFuZ2VkID8gbmV4dFN0YXRlIDogc3RhdGU7XG4gIH07XG59IiwiXCJ1c2Ugc3RyaWN0XCI7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGNvbXBvc2U7XG4vKipcbiAqIENvbXBvc2VzIHNpbmdsZS1hcmd1bWVudCBmdW5jdGlvbnMgZnJvbSByaWdodCB0byBsZWZ0LiBUaGUgcmlnaHRtb3N0XG4gKiBmdW5jdGlvbiBjYW4gdGFrZSBtdWx0aXBsZSBhcmd1bWVudHMgYXMgaXQgcHJvdmlkZXMgdGhlIHNpZ25hdHVyZSBmb3JcbiAqIHRoZSByZXN1bHRpbmcgY29tcG9zaXRlIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7Li4uRnVuY3Rpb259IGZ1bmNzIFRoZSBmdW5jdGlvbnMgdG8gY29tcG9zZS5cbiAqIEByZXR1cm5zIHtGdW5jdGlvbn0gQSBmdW5jdGlvbiBvYnRhaW5lZCBieSBjb21wb3NpbmcgdGhlIGFyZ3VtZW50IGZ1bmN0aW9uc1xuICogZnJvbSByaWdodCB0byBsZWZ0LiBGb3IgZXhhbXBsZSwgY29tcG9zZShmLCBnLCBoKSBpcyBpZGVudGljYWwgdG8gZG9pbmdcbiAqICguLi5hcmdzKSA9PiBmKGcoaCguLi5hcmdzKSkpLlxuICovXG5cbmZ1bmN0aW9uIGNvbXBvc2UoKSB7XG4gIGZvciAodmFyIF9sZW4gPSBhcmd1bWVudHMubGVuZ3RoLCBmdW5jcyA9IEFycmF5KF9sZW4pLCBfa2V5ID0gMDsgX2tleSA8IF9sZW47IF9rZXkrKykge1xuICAgIGZ1bmNzW19rZXldID0gYXJndW1lbnRzW19rZXldO1xuICB9XG5cbiAgaWYgKGZ1bmNzLmxlbmd0aCA9PT0gMCkge1xuICAgIHJldHVybiBmdW5jdGlvbiAoYXJnKSB7XG4gICAgICByZXR1cm4gYXJnO1xuICAgIH07XG4gIH0gZWxzZSB7XG4gICAgdmFyIF9yZXQgPSBmdW5jdGlvbiAoKSB7XG4gICAgICB2YXIgbGFzdCA9IGZ1bmNzW2Z1bmNzLmxlbmd0aCAtIDFdO1xuICAgICAgdmFyIHJlc3QgPSBmdW5jcy5zbGljZSgwLCAtMSk7XG4gICAgICByZXR1cm4ge1xuICAgICAgICB2OiBmdW5jdGlvbiB2KCkge1xuICAgICAgICAgIHJldHVybiByZXN0LnJlZHVjZVJpZ2h0KGZ1bmN0aW9uIChjb21wb3NlZCwgZikge1xuICAgICAgICAgICAgcmV0dXJuIGYoY29tcG9zZWQpO1xuICAgICAgICAgIH0sIGxhc3QuYXBwbHkodW5kZWZpbmVkLCBhcmd1bWVudHMpKTtcbiAgICAgICAgfVxuICAgICAgfTtcbiAgICB9KCk7XG5cbiAgICBpZiAodHlwZW9mIF9yZXQgPT09IFwib2JqZWN0XCIpIHJldHVybiBfcmV0LnY7XG4gIH1cbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLkFjdGlvblR5cGVzID0gdW5kZWZpbmVkO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBjcmVhdGVTdG9yZTtcblxudmFyIF9pc1BsYWluT2JqZWN0ID0gcmVxdWlyZSgnbG9kYXNoL2lzUGxhaW5PYmplY3QnKTtcblxudmFyIF9pc1BsYWluT2JqZWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2lzUGxhaW5PYmplY3QpO1xuXG52YXIgX3N5bWJvbE9ic2VydmFibGUgPSByZXF1aXJlKCdzeW1ib2wtb2JzZXJ2YWJsZScpO1xuXG52YXIgX3N5bWJvbE9ic2VydmFibGUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc3ltYm9sT2JzZXJ2YWJsZSk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG4vKipcbiAqIFRoZXNlIGFyZSBwcml2YXRlIGFjdGlvbiB0eXBlcyByZXNlcnZlZCBieSBSZWR1eC5cbiAqIEZvciBhbnkgdW5rbm93biBhY3Rpb25zLCB5b3UgbXVzdCByZXR1cm4gdGhlIGN1cnJlbnQgc3RhdGUuXG4gKiBJZiB0aGUgY3VycmVudCBzdGF0ZSBpcyB1bmRlZmluZWQsIHlvdSBtdXN0IHJldHVybiB0aGUgaW5pdGlhbCBzdGF0ZS5cbiAqIERvIG5vdCByZWZlcmVuY2UgdGhlc2UgYWN0aW9uIHR5cGVzIGRpcmVjdGx5IGluIHlvdXIgY29kZS5cbiAqL1xudmFyIEFjdGlvblR5cGVzID0gZXhwb3J0cy5BY3Rpb25UeXBlcyA9IHtcbiAgSU5JVDogJ0BAcmVkdXgvSU5JVCdcbn07XG5cbi8qKlxuICogQ3JlYXRlcyBhIFJlZHV4IHN0b3JlIHRoYXQgaG9sZHMgdGhlIHN0YXRlIHRyZWUuXG4gKiBUaGUgb25seSB3YXkgdG8gY2hhbmdlIHRoZSBkYXRhIGluIHRoZSBzdG9yZSBpcyB0byBjYWxsIGBkaXNwYXRjaCgpYCBvbiBpdC5cbiAqXG4gKiBUaGVyZSBzaG91bGQgb25seSBiZSBhIHNpbmdsZSBzdG9yZSBpbiB5b3VyIGFwcC4gVG8gc3BlY2lmeSBob3cgZGlmZmVyZW50XG4gKiBwYXJ0cyBvZiB0aGUgc3RhdGUgdHJlZSByZXNwb25kIHRvIGFjdGlvbnMsIHlvdSBtYXkgY29tYmluZSBzZXZlcmFsIHJlZHVjZXJzXG4gKiBpbnRvIGEgc2luZ2xlIHJlZHVjZXIgZnVuY3Rpb24gYnkgdXNpbmcgYGNvbWJpbmVSZWR1Y2Vyc2AuXG4gKlxuICogQHBhcmFtIHtGdW5jdGlvbn0gcmVkdWNlciBBIGZ1bmN0aW9uIHRoYXQgcmV0dXJucyB0aGUgbmV4dCBzdGF0ZSB0cmVlLCBnaXZlblxuICogdGhlIGN1cnJlbnQgc3RhdGUgdHJlZSBhbmQgdGhlIGFjdGlvbiB0byBoYW5kbGUuXG4gKlxuICogQHBhcmFtIHthbnl9IFtpbml0aWFsU3RhdGVdIFRoZSBpbml0aWFsIHN0YXRlLiBZb3UgbWF5IG9wdGlvbmFsbHkgc3BlY2lmeSBpdFxuICogdG8gaHlkcmF0ZSB0aGUgc3RhdGUgZnJvbSB0aGUgc2VydmVyIGluIHVuaXZlcnNhbCBhcHBzLCBvciB0byByZXN0b3JlIGFcbiAqIHByZXZpb3VzbHkgc2VyaWFsaXplZCB1c2VyIHNlc3Npb24uXG4gKiBJZiB5b3UgdXNlIGBjb21iaW5lUmVkdWNlcnNgIHRvIHByb2R1Y2UgdGhlIHJvb3QgcmVkdWNlciBmdW5jdGlvbiwgdGhpcyBtdXN0IGJlXG4gKiBhbiBvYmplY3Qgd2l0aCB0aGUgc2FtZSBzaGFwZSBhcyBgY29tYmluZVJlZHVjZXJzYCBrZXlzLlxuICpcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGVuaGFuY2VyIFRoZSBzdG9yZSBlbmhhbmNlci4gWW91IG1heSBvcHRpb25hbGx5IHNwZWNpZnkgaXRcbiAqIHRvIGVuaGFuY2UgdGhlIHN0b3JlIHdpdGggdGhpcmQtcGFydHkgY2FwYWJpbGl0aWVzIHN1Y2ggYXMgbWlkZGxld2FyZSxcbiAqIHRpbWUgdHJhdmVsLCBwZXJzaXN0ZW5jZSwgZXRjLiBUaGUgb25seSBzdG9yZSBlbmhhbmNlciB0aGF0IHNoaXBzIHdpdGggUmVkdXhcbiAqIGlzIGBhcHBseU1pZGRsZXdhcmUoKWAuXG4gKlxuICogQHJldHVybnMge1N0b3JlfSBBIFJlZHV4IHN0b3JlIHRoYXQgbGV0cyB5b3UgcmVhZCB0aGUgc3RhdGUsIGRpc3BhdGNoIGFjdGlvbnNcbiAqIGFuZCBzdWJzY3JpYmUgdG8gY2hhbmdlcy5cbiAqL1xuZnVuY3Rpb24gY3JlYXRlU3RvcmUocmVkdWNlciwgaW5pdGlhbFN0YXRlLCBlbmhhbmNlcikge1xuICB2YXIgX3JlZjI7XG5cbiAgaWYgKHR5cGVvZiBpbml0aWFsU3RhdGUgPT09ICdmdW5jdGlvbicgJiYgdHlwZW9mIGVuaGFuY2VyID09PSAndW5kZWZpbmVkJykge1xuICAgIGVuaGFuY2VyID0gaW5pdGlhbFN0YXRlO1xuICAgIGluaXRpYWxTdGF0ZSA9IHVuZGVmaW5lZDtcbiAgfVxuXG4gIGlmICh0eXBlb2YgZW5oYW5jZXIgIT09ICd1bmRlZmluZWQnKSB7XG4gICAgaWYgKHR5cGVvZiBlbmhhbmNlciAhPT0gJ2Z1bmN0aW9uJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdFeHBlY3RlZCB0aGUgZW5oYW5jZXIgdG8gYmUgYSBmdW5jdGlvbi4nKTtcbiAgICB9XG5cbiAgICByZXR1cm4gZW5oYW5jZXIoY3JlYXRlU3RvcmUpKHJlZHVjZXIsIGluaXRpYWxTdGF0ZSk7XG4gIH1cblxuICBpZiAodHlwZW9mIHJlZHVjZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICB0aHJvdyBuZXcgRXJyb3IoJ0V4cGVjdGVkIHRoZSByZWR1Y2VyIHRvIGJlIGEgZnVuY3Rpb24uJyk7XG4gIH1cblxuICB2YXIgY3VycmVudFJlZHVjZXIgPSByZWR1Y2VyO1xuICB2YXIgY3VycmVudFN0YXRlID0gaW5pdGlhbFN0YXRlO1xuICB2YXIgY3VycmVudExpc3RlbmVycyA9IFtdO1xuICB2YXIgbmV4dExpc3RlbmVycyA9IGN1cnJlbnRMaXN0ZW5lcnM7XG4gIHZhciBpc0Rpc3BhdGNoaW5nID0gZmFsc2U7XG5cbiAgZnVuY3Rpb24gZW5zdXJlQ2FuTXV0YXRlTmV4dExpc3RlbmVycygpIHtcbiAgICBpZiAobmV4dExpc3RlbmVycyA9PT0gY3VycmVudExpc3RlbmVycykge1xuICAgICAgbmV4dExpc3RlbmVycyA9IGN1cnJlbnRMaXN0ZW5lcnMuc2xpY2UoKTtcbiAgICB9XG4gIH1cblxuICAvKipcbiAgICogUmVhZHMgdGhlIHN0YXRlIHRyZWUgbWFuYWdlZCBieSB0aGUgc3RvcmUuXG4gICAqXG4gICAqIEByZXR1cm5zIHthbnl9IFRoZSBjdXJyZW50IHN0YXRlIHRyZWUgb2YgeW91ciBhcHBsaWNhdGlvbi5cbiAgICovXG4gIGZ1bmN0aW9uIGdldFN0YXRlKCkge1xuICAgIHJldHVybiBjdXJyZW50U3RhdGU7XG4gIH1cblxuICAvKipcbiAgICogQWRkcyBhIGNoYW5nZSBsaXN0ZW5lci4gSXQgd2lsbCBiZSBjYWxsZWQgYW55IHRpbWUgYW4gYWN0aW9uIGlzIGRpc3BhdGNoZWQsXG4gICAqIGFuZCBzb21lIHBhcnQgb2YgdGhlIHN0YXRlIHRyZWUgbWF5IHBvdGVudGlhbGx5IGhhdmUgY2hhbmdlZC4gWW91IG1heSB0aGVuXG4gICAqIGNhbGwgYGdldFN0YXRlKClgIHRvIHJlYWQgdGhlIGN1cnJlbnQgc3RhdGUgdHJlZSBpbnNpZGUgdGhlIGNhbGxiYWNrLlxuICAgKlxuICAgKiBZb3UgbWF5IGNhbGwgYGRpc3BhdGNoKClgIGZyb20gYSBjaGFuZ2UgbGlzdGVuZXIsIHdpdGggdGhlIGZvbGxvd2luZ1xuICAgKiBjYXZlYXRzOlxuICAgKlxuICAgKiAxLiBUaGUgc3Vic2NyaXB0aW9ucyBhcmUgc25hcHNob3R0ZWQganVzdCBiZWZvcmUgZXZlcnkgYGRpc3BhdGNoKClgIGNhbGwuXG4gICAqIElmIHlvdSBzdWJzY3JpYmUgb3IgdW5zdWJzY3JpYmUgd2hpbGUgdGhlIGxpc3RlbmVycyBhcmUgYmVpbmcgaW52b2tlZCwgdGhpc1xuICAgKiB3aWxsIG5vdCBoYXZlIGFueSBlZmZlY3Qgb24gdGhlIGBkaXNwYXRjaCgpYCB0aGF0IGlzIGN1cnJlbnRseSBpbiBwcm9ncmVzcy5cbiAgICogSG93ZXZlciwgdGhlIG5leHQgYGRpc3BhdGNoKClgIGNhbGwsIHdoZXRoZXIgbmVzdGVkIG9yIG5vdCwgd2lsbCB1c2UgYSBtb3JlXG4gICAqIHJlY2VudCBzbmFwc2hvdCBvZiB0aGUgc3Vic2NyaXB0aW9uIGxpc3QuXG4gICAqXG4gICAqIDIuIFRoZSBsaXN0ZW5lciBzaG91bGQgbm90IGV4cGVjdCB0byBzZWUgYWxsIHN0YXRlIGNoYW5nZXMsIGFzIHRoZSBzdGF0ZVxuICAgKiBtaWdodCBoYXZlIGJlZW4gdXBkYXRlZCBtdWx0aXBsZSB0aW1lcyBkdXJpbmcgYSBuZXN0ZWQgYGRpc3BhdGNoKClgIGJlZm9yZVxuICAgKiB0aGUgbGlzdGVuZXIgaXMgY2FsbGVkLiBJdCBpcywgaG93ZXZlciwgZ3VhcmFudGVlZCB0aGF0IGFsbCBzdWJzY3JpYmVyc1xuICAgKiByZWdpc3RlcmVkIGJlZm9yZSB0aGUgYGRpc3BhdGNoKClgIHN0YXJ0ZWQgd2lsbCBiZSBjYWxsZWQgd2l0aCB0aGUgbGF0ZXN0XG4gICAqIHN0YXRlIGJ5IHRoZSB0aW1lIGl0IGV4aXRzLlxuICAgKlxuICAgKiBAcGFyYW0ge0Z1bmN0aW9ufSBsaXN0ZW5lciBBIGNhbGxiYWNrIHRvIGJlIGludm9rZWQgb24gZXZlcnkgZGlzcGF0Y2guXG4gICAqIEByZXR1cm5zIHtGdW5jdGlvbn0gQSBmdW5jdGlvbiB0byByZW1vdmUgdGhpcyBjaGFuZ2UgbGlzdGVuZXIuXG4gICAqL1xuICBmdW5jdGlvbiBzdWJzY3JpYmUobGlzdGVuZXIpIHtcbiAgICBpZiAodHlwZW9mIGxpc3RlbmVyICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0V4cGVjdGVkIGxpc3RlbmVyIHRvIGJlIGEgZnVuY3Rpb24uJyk7XG4gICAgfVxuXG4gICAgdmFyIGlzU3Vic2NyaWJlZCA9IHRydWU7XG5cbiAgICBlbnN1cmVDYW5NdXRhdGVOZXh0TGlzdGVuZXJzKCk7XG4gICAgbmV4dExpc3RlbmVycy5wdXNoKGxpc3RlbmVyKTtcblxuICAgIHJldHVybiBmdW5jdGlvbiB1bnN1YnNjcmliZSgpIHtcbiAgICAgIGlmICghaXNTdWJzY3JpYmVkKSB7XG4gICAgICAgIHJldHVybjtcbiAgICAgIH1cblxuICAgICAgaXNTdWJzY3JpYmVkID0gZmFsc2U7XG5cbiAgICAgIGVuc3VyZUNhbk11dGF0ZU5leHRMaXN0ZW5lcnMoKTtcbiAgICAgIHZhciBpbmRleCA9IG5leHRMaXN0ZW5lcnMuaW5kZXhPZihsaXN0ZW5lcik7XG4gICAgICBuZXh0TGlzdGVuZXJzLnNwbGljZShpbmRleCwgMSk7XG4gICAgfTtcbiAgfVxuXG4gIC8qKlxuICAgKiBEaXNwYXRjaGVzIGFuIGFjdGlvbi4gSXQgaXMgdGhlIG9ubHkgd2F5IHRvIHRyaWdnZXIgYSBzdGF0ZSBjaGFuZ2UuXG4gICAqXG4gICAqIFRoZSBgcmVkdWNlcmAgZnVuY3Rpb24sIHVzZWQgdG8gY3JlYXRlIHRoZSBzdG9yZSwgd2lsbCBiZSBjYWxsZWQgd2l0aCB0aGVcbiAgICogY3VycmVudCBzdGF0ZSB0cmVlIGFuZCB0aGUgZ2l2ZW4gYGFjdGlvbmAuIEl0cyByZXR1cm4gdmFsdWUgd2lsbFxuICAgKiBiZSBjb25zaWRlcmVkIHRoZSAqKm5leHQqKiBzdGF0ZSBvZiB0aGUgdHJlZSwgYW5kIHRoZSBjaGFuZ2UgbGlzdGVuZXJzXG4gICAqIHdpbGwgYmUgbm90aWZpZWQuXG4gICAqXG4gICAqIFRoZSBiYXNlIGltcGxlbWVudGF0aW9uIG9ubHkgc3VwcG9ydHMgcGxhaW4gb2JqZWN0IGFjdGlvbnMuIElmIHlvdSB3YW50IHRvXG4gICAqIGRpc3BhdGNoIGEgUHJvbWlzZSwgYW4gT2JzZXJ2YWJsZSwgYSB0aHVuaywgb3Igc29tZXRoaW5nIGVsc2UsIHlvdSBuZWVkIHRvXG4gICAqIHdyYXAgeW91ciBzdG9yZSBjcmVhdGluZyBmdW5jdGlvbiBpbnRvIHRoZSBjb3JyZXNwb25kaW5nIG1pZGRsZXdhcmUuIEZvclxuICAgKiBleGFtcGxlLCBzZWUgdGhlIGRvY3VtZW50YXRpb24gZm9yIHRoZSBgcmVkdXgtdGh1bmtgIHBhY2thZ2UuIEV2ZW4gdGhlXG4gICAqIG1pZGRsZXdhcmUgd2lsbCBldmVudHVhbGx5IGRpc3BhdGNoIHBsYWluIG9iamVjdCBhY3Rpb25zIHVzaW5nIHRoaXMgbWV0aG9kLlxuICAgKlxuICAgKiBAcGFyYW0ge09iamVjdH0gYWN0aW9uIEEgcGxhaW4gb2JqZWN0IHJlcHJlc2VudGluZyDigJx3aGF0IGNoYW5nZWTigJ0uIEl0IGlzXG4gICAqIGEgZ29vZCBpZGVhIHRvIGtlZXAgYWN0aW9ucyBzZXJpYWxpemFibGUgc28geW91IGNhbiByZWNvcmQgYW5kIHJlcGxheSB1c2VyXG4gICAqIHNlc3Npb25zLCBvciB1c2UgdGhlIHRpbWUgdHJhdmVsbGluZyBgcmVkdXgtZGV2dG9vbHNgLiBBbiBhY3Rpb24gbXVzdCBoYXZlXG4gICAqIGEgYHR5cGVgIHByb3BlcnR5IHdoaWNoIG1heSBub3QgYmUgYHVuZGVmaW5lZGAuIEl0IGlzIGEgZ29vZCBpZGVhIHRvIHVzZVxuICAgKiBzdHJpbmcgY29uc3RhbnRzIGZvciBhY3Rpb24gdHlwZXMuXG4gICAqXG4gICAqIEByZXR1cm5zIHtPYmplY3R9IEZvciBjb252ZW5pZW5jZSwgdGhlIHNhbWUgYWN0aW9uIG9iamVjdCB5b3UgZGlzcGF0Y2hlZC5cbiAgICpcbiAgICogTm90ZSB0aGF0LCBpZiB5b3UgdXNlIGEgY3VzdG9tIG1pZGRsZXdhcmUsIGl0IG1heSB3cmFwIGBkaXNwYXRjaCgpYCB0b1xuICAgKiByZXR1cm4gc29tZXRoaW5nIGVsc2UgKGZvciBleGFtcGxlLCBhIFByb21pc2UgeW91IGNhbiBhd2FpdCkuXG4gICAqL1xuICBmdW5jdGlvbiBkaXNwYXRjaChhY3Rpb24pIHtcbiAgICBpZiAoISgwLCBfaXNQbGFpbk9iamVjdDJbXCJkZWZhdWx0XCJdKShhY3Rpb24pKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0FjdGlvbnMgbXVzdCBiZSBwbGFpbiBvYmplY3RzLiAnICsgJ1VzZSBjdXN0b20gbWlkZGxld2FyZSBmb3IgYXN5bmMgYWN0aW9ucy4nKTtcbiAgICB9XG5cbiAgICBpZiAodHlwZW9mIGFjdGlvbi50eXBlID09PSAndW5kZWZpbmVkJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdBY3Rpb25zIG1heSBub3QgaGF2ZSBhbiB1bmRlZmluZWQgXCJ0eXBlXCIgcHJvcGVydHkuICcgKyAnSGF2ZSB5b3UgbWlzc3BlbGxlZCBhIGNvbnN0YW50PycpO1xuICAgIH1cblxuICAgIGlmIChpc0Rpc3BhdGNoaW5nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1JlZHVjZXJzIG1heSBub3QgZGlzcGF0Y2ggYWN0aW9ucy4nKTtcbiAgICB9XG5cbiAgICB0cnkge1xuICAgICAgaXNEaXNwYXRjaGluZyA9IHRydWU7XG4gICAgICBjdXJyZW50U3RhdGUgPSBjdXJyZW50UmVkdWNlcihjdXJyZW50U3RhdGUsIGFjdGlvbik7XG4gICAgfSBmaW5hbGx5IHtcbiAgICAgIGlzRGlzcGF0Y2hpbmcgPSBmYWxzZTtcbiAgICB9XG5cbiAgICB2YXIgbGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycyA9IG5leHRMaXN0ZW5lcnM7XG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBsaXN0ZW5lcnMubGVuZ3RoOyBpKyspIHtcbiAgICAgIGxpc3RlbmVyc1tpXSgpO1xuICAgIH1cblxuICAgIHJldHVybiBhY3Rpb247XG4gIH1cblxuICAvKipcbiAgICogUmVwbGFjZXMgdGhlIHJlZHVjZXIgY3VycmVudGx5IHVzZWQgYnkgdGhlIHN0b3JlIHRvIGNhbGN1bGF0ZSB0aGUgc3RhdGUuXG4gICAqXG4gICAqIFlvdSBtaWdodCBuZWVkIHRoaXMgaWYgeW91ciBhcHAgaW1wbGVtZW50cyBjb2RlIHNwbGl0dGluZyBhbmQgeW91IHdhbnQgdG9cbiAgICogbG9hZCBzb21lIG9mIHRoZSByZWR1Y2VycyBkeW5hbWljYWxseS4gWW91IG1pZ2h0IGFsc28gbmVlZCB0aGlzIGlmIHlvdVxuICAgKiBpbXBsZW1lbnQgYSBob3QgcmVsb2FkaW5nIG1lY2hhbmlzbSBmb3IgUmVkdXguXG4gICAqXG4gICAqIEBwYXJhbSB7RnVuY3Rpb259IG5leHRSZWR1Y2VyIFRoZSByZWR1Y2VyIGZvciB0aGUgc3RvcmUgdG8gdXNlIGluc3RlYWQuXG4gICAqIEByZXR1cm5zIHt2b2lkfVxuICAgKi9cbiAgZnVuY3Rpb24gcmVwbGFjZVJlZHVjZXIobmV4dFJlZHVjZXIpIHtcbiAgICBpZiAodHlwZW9mIG5leHRSZWR1Y2VyICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0V4cGVjdGVkIHRoZSBuZXh0UmVkdWNlciB0byBiZSBhIGZ1bmN0aW9uLicpO1xuICAgIH1cblxuICAgIGN1cnJlbnRSZWR1Y2VyID0gbmV4dFJlZHVjZXI7XG4gICAgZGlzcGF0Y2goeyB0eXBlOiBBY3Rpb25UeXBlcy5JTklUIH0pO1xuICB9XG5cbiAgLyoqXG4gICAqIEludGVyb3BlcmFiaWxpdHkgcG9pbnQgZm9yIG9ic2VydmFibGUvcmVhY3RpdmUgbGlicmFyaWVzLlxuICAgKiBAcmV0dXJucyB7b2JzZXJ2YWJsZX0gQSBtaW5pbWFsIG9ic2VydmFibGUgb2Ygc3RhdGUgY2hhbmdlcy5cbiAgICogRm9yIG1vcmUgaW5mb3JtYXRpb24sIHNlZSB0aGUgb2JzZXJ2YWJsZSBwcm9wb3NhbDpcbiAgICogaHR0cHM6Ly9naXRodWIuY29tL3plbnBhcnNpbmcvZXMtb2JzZXJ2YWJsZVxuICAgKi9cbiAgZnVuY3Rpb24gb2JzZXJ2YWJsZSgpIHtcbiAgICB2YXIgX3JlZjtcblxuICAgIHZhciBvdXRlclN1YnNjcmliZSA9IHN1YnNjcmliZTtcbiAgICByZXR1cm4gX3JlZiA9IHtcbiAgICAgIC8qKlxuICAgICAgICogVGhlIG1pbmltYWwgb2JzZXJ2YWJsZSBzdWJzY3JpcHRpb24gbWV0aG9kLlxuICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9ic2VydmVyIEFueSBvYmplY3QgdGhhdCBjYW4gYmUgdXNlZCBhcyBhbiBvYnNlcnZlci5cbiAgICAgICAqIFRoZSBvYnNlcnZlciBvYmplY3Qgc2hvdWxkIGhhdmUgYSBgbmV4dGAgbWV0aG9kLlxuICAgICAgICogQHJldHVybnMge3N1YnNjcmlwdGlvbn0gQW4gb2JqZWN0IHdpdGggYW4gYHVuc3Vic2NyaWJlYCBtZXRob2QgdGhhdCBjYW5cbiAgICAgICAqIGJlIHVzZWQgdG8gdW5zdWJzY3JpYmUgdGhlIG9ic2VydmFibGUgZnJvbSB0aGUgc3RvcmUsIGFuZCBwcmV2ZW50IGZ1cnRoZXJcbiAgICAgICAqIGVtaXNzaW9uIG9mIHZhbHVlcyBmcm9tIHRoZSBvYnNlcnZhYmxlLlxuICAgICAgICovXG5cbiAgICAgIHN1YnNjcmliZTogZnVuY3Rpb24gc3Vic2NyaWJlKG9ic2VydmVyKSB7XG4gICAgICAgIGlmICh0eXBlb2Ygb2JzZXJ2ZXIgIT09ICdvYmplY3QnKSB7XG4gICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignRXhwZWN0ZWQgdGhlIG9ic2VydmVyIHRvIGJlIGFuIG9iamVjdC4nKTtcbiAgICAgICAgfVxuXG4gICAgICAgIGZ1bmN0aW9uIG9ic2VydmVTdGF0ZSgpIHtcbiAgICAgICAgICBpZiAob2JzZXJ2ZXIubmV4dCkge1xuICAgICAgICAgICAgb2JzZXJ2ZXIubmV4dChnZXRTdGF0ZSgpKTtcbiAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICBvYnNlcnZlU3RhdGUoKTtcbiAgICAgICAgdmFyIHVuc3Vic2NyaWJlID0gb3V0ZXJTdWJzY3JpYmUob2JzZXJ2ZVN0YXRlKTtcbiAgICAgICAgcmV0dXJuIHsgdW5zdWJzY3JpYmU6IHVuc3Vic2NyaWJlIH07XG4gICAgICB9XG4gICAgfSwgX3JlZltfc3ltYm9sT2JzZXJ2YWJsZTJbXCJkZWZhdWx0XCJdXSA9IGZ1bmN0aW9uICgpIHtcbiAgICAgIHJldHVybiB0aGlzO1xuICAgIH0sIF9yZWY7XG4gIH1cblxuICAvLyBXaGVuIGEgc3RvcmUgaXMgY3JlYXRlZCwgYW4gXCJJTklUXCIgYWN0aW9uIGlzIGRpc3BhdGNoZWQgc28gdGhhdCBldmVyeVxuICAvLyByZWR1Y2VyIHJldHVybnMgdGhlaXIgaW5pdGlhbCBzdGF0ZS4gVGhpcyBlZmZlY3RpdmVseSBwb3B1bGF0ZXNcbiAgLy8gdGhlIGluaXRpYWwgc3RhdGUgdHJlZS5cbiAgZGlzcGF0Y2goeyB0eXBlOiBBY3Rpb25UeXBlcy5JTklUIH0pO1xuXG4gIHJldHVybiBfcmVmMiA9IHtcbiAgICBkaXNwYXRjaDogZGlzcGF0Y2gsXG4gICAgc3Vic2NyaWJlOiBzdWJzY3JpYmUsXG4gICAgZ2V0U3RhdGU6IGdldFN0YXRlLFxuICAgIHJlcGxhY2VSZWR1Y2VyOiByZXBsYWNlUmVkdWNlclxuICB9LCBfcmVmMltfc3ltYm9sT2JzZXJ2YWJsZTJbXCJkZWZhdWx0XCJdXSA9IG9ic2VydmFibGUsIF9yZWYyO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuY29tcG9zZSA9IGV4cG9ydHMuYXBwbHlNaWRkbGV3YXJlID0gZXhwb3J0cy5iaW5kQWN0aW9uQ3JlYXRvcnMgPSBleHBvcnRzLmNvbWJpbmVSZWR1Y2VycyA9IGV4cG9ydHMuY3JlYXRlU3RvcmUgPSB1bmRlZmluZWQ7XG5cbnZhciBfY3JlYXRlU3RvcmUgPSByZXF1aXJlKCcuL2NyZWF0ZVN0b3JlJyk7XG5cbnZhciBfY3JlYXRlU3RvcmUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY3JlYXRlU3RvcmUpO1xuXG52YXIgX2NvbWJpbmVSZWR1Y2VycyA9IHJlcXVpcmUoJy4vY29tYmluZVJlZHVjZXJzJyk7XG5cbnZhciBfY29tYmluZVJlZHVjZXJzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NvbWJpbmVSZWR1Y2Vycyk7XG5cbnZhciBfYmluZEFjdGlvbkNyZWF0b3JzID0gcmVxdWlyZSgnLi9iaW5kQWN0aW9uQ3JlYXRvcnMnKTtcblxudmFyIF9iaW5kQWN0aW9uQ3JlYXRvcnMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfYmluZEFjdGlvbkNyZWF0b3JzKTtcblxudmFyIF9hcHBseU1pZGRsZXdhcmUgPSByZXF1aXJlKCcuL2FwcGx5TWlkZGxld2FyZScpO1xuXG52YXIgX2FwcGx5TWlkZGxld2FyZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9hcHBseU1pZGRsZXdhcmUpO1xuXG52YXIgX2NvbXBvc2UgPSByZXF1aXJlKCcuL2NvbXBvc2UnKTtcblxudmFyIF9jb21wb3NlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NvbXBvc2UpO1xuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCcuL3V0aWxzL3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuLypcbiogVGhpcyBpcyBhIGR1bW15IGZ1bmN0aW9uIHRvIGNoZWNrIGlmIHRoZSBmdW5jdGlvbiBuYW1lIGhhcyBiZWVuIGFsdGVyZWQgYnkgbWluaWZpY2F0aW9uLlxuKiBJZiB0aGUgZnVuY3Rpb24gaGFzIGJlZW4gbWluaWZpZWQgYW5kIE5PREVfRU5WICE9PSAncHJvZHVjdGlvbicsIHdhcm4gdGhlIHVzZXIuXG4qL1xuZnVuY3Rpb24gaXNDcnVzaGVkKCkge31cblxuaWYgKFwicHJvZHVjdGlvblwiICE9PSAncHJvZHVjdGlvbicgJiYgdHlwZW9mIGlzQ3J1c2hlZC5uYW1lID09PSAnc3RyaW5nJyAmJiBpc0NydXNoZWQubmFtZSAhPT0gJ2lzQ3J1c2hlZCcpIHtcbiAgKDAsIF93YXJuaW5nMltcImRlZmF1bHRcIl0pKCdZb3UgYXJlIGN1cnJlbnRseSB1c2luZyBtaW5pZmllZCBjb2RlIG91dHNpZGUgb2YgTk9ERV9FTlYgPT09IFxcJ3Byb2R1Y3Rpb25cXCcuICcgKyAnVGhpcyBtZWFucyB0aGF0IHlvdSBhcmUgcnVubmluZyBhIHNsb3dlciBkZXZlbG9wbWVudCBidWlsZCBvZiBSZWR1eC4gJyArICdZb3UgY2FuIHVzZSBsb29zZS1lbnZpZnkgKGh0dHBzOi8vZ2l0aHViLmNvbS96ZXJ0b3NoL2xvb3NlLWVudmlmeSkgZm9yIGJyb3dzZXJpZnkgJyArICdvciBEZWZpbmVQbHVnaW4gZm9yIHdlYnBhY2sgKGh0dHA6Ly9zdGFja292ZXJmbG93LmNvbS9xdWVzdGlvbnMvMzAwMzAwMzEpICcgKyAndG8gZW5zdXJlIHlvdSBoYXZlIHRoZSBjb3JyZWN0IGNvZGUgZm9yIHlvdXIgcHJvZHVjdGlvbiBidWlsZC4nKTtcbn1cblxuZXhwb3J0cy5jcmVhdGVTdG9yZSA9IF9jcmVhdGVTdG9yZTJbXCJkZWZhdWx0XCJdO1xuZXhwb3J0cy5jb21iaW5lUmVkdWNlcnMgPSBfY29tYmluZVJlZHVjZXJzMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmJpbmRBY3Rpb25DcmVhdG9ycyA9IF9iaW5kQWN0aW9uQ3JlYXRvcnMyW1wiZGVmYXVsdFwiXTtcbmV4cG9ydHMuYXBwbHlNaWRkbGV3YXJlID0gX2FwcGx5TWlkZGxld2FyZTJbXCJkZWZhdWx0XCJdO1xuZXhwb3J0cy5jb21wb3NlID0gX2NvbXBvc2UyW1wiZGVmYXVsdFwiXTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IHdhcm5pbmc7XG4vKipcbiAqIFByaW50cyBhIHdhcm5pbmcgaW4gdGhlIGNvbnNvbGUgaWYgaXQgZXhpc3RzLlxuICpcbiAqIEBwYXJhbSB7U3RyaW5nfSBtZXNzYWdlIFRoZSB3YXJuaW5nIG1lc3NhZ2UuXG4gKiBAcmV0dXJucyB7dm9pZH1cbiAqL1xuZnVuY3Rpb24gd2FybmluZyhtZXNzYWdlKSB7XG4gIC8qIGVzbGludC1kaXNhYmxlIG5vLWNvbnNvbGUgKi9cbiAgaWYgKHR5cGVvZiBjb25zb2xlICE9PSAndW5kZWZpbmVkJyAmJiB0eXBlb2YgY29uc29sZS5lcnJvciA9PT0gJ2Z1bmN0aW9uJykge1xuICAgIGNvbnNvbGUuZXJyb3IobWVzc2FnZSk7XG4gIH1cbiAgLyogZXNsaW50LWVuYWJsZSBuby1jb25zb2xlICovXG4gIHRyeSB7XG4gICAgLy8gVGhpcyBlcnJvciB3YXMgdGhyb3duIGFzIGEgY29udmVuaWVuY2Ugc28gdGhhdCBpZiB5b3UgZW5hYmxlXG4gICAgLy8gXCJicmVhayBvbiBhbGwgZXhjZXB0aW9uc1wiIGluIHlvdXIgY29uc29sZSxcbiAgICAvLyBpdCB3b3VsZCBwYXVzZSB0aGUgZXhlY3V0aW9uIGF0IHRoaXMgbGluZS5cbiAgICB0aHJvdyBuZXcgRXJyb3IobWVzc2FnZSk7XG4gICAgLyogZXNsaW50LWRpc2FibGUgbm8tZW1wdHkgKi9cbiAgfSBjYXRjaCAoZSkge31cbiAgLyogZXNsaW50LWVuYWJsZSBuby1lbXB0eSAqL1xufSIsIid1c2Ugc3RyaWN0Jztcbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24gKHN0cikge1xuXHRyZXR1cm4gZW5jb2RlVVJJQ29tcG9uZW50KHN0cikucmVwbGFjZSgvWyEnKCkqXS9nLCBmdW5jdGlvbiAoYykge1xuXHRcdHJldHVybiAnJScgKyBjLmNoYXJDb2RlQXQoMCkudG9TdHJpbmcoMTYpLnRvVXBwZXJDYXNlKCk7XG5cdH0pO1xufTtcbiIsIi8qIGdsb2JhbCB3aW5kb3cgKi9cbid1c2Ugc3RyaWN0JztcblxubW9kdWxlLmV4cG9ydHMgPSByZXF1aXJlKCcuL3BvbnlmaWxsJykoZ2xvYmFsIHx8IHdpbmRvdyB8fCB0aGlzKTtcbiIsIid1c2Ugc3RyaWN0JztcblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiBzeW1ib2xPYnNlcnZhYmxlUG9ueWZpbGwocm9vdCkge1xuXHR2YXIgcmVzdWx0O1xuXHR2YXIgU3ltYm9sID0gcm9vdC5TeW1ib2w7XG5cblx0aWYgKHR5cGVvZiBTeW1ib2wgPT09ICdmdW5jdGlvbicpIHtcblx0XHRpZiAoU3ltYm9sLm9ic2VydmFibGUpIHtcblx0XHRcdHJlc3VsdCA9IFN5bWJvbC5vYnNlcnZhYmxlO1xuXHRcdH0gZWxzZSB7XG5cdFx0XHRyZXN1bHQgPSBTeW1ib2woJ29ic2VydmFibGUnKTtcblx0XHRcdFN5bWJvbC5vYnNlcnZhYmxlID0gcmVzdWx0O1xuXHRcdH1cblx0fSBlbHNlIHtcblx0XHRyZXN1bHQgPSAnQEBvYnNlcnZhYmxlJztcblx0fVxuXG5cdHJldHVybiByZXN1bHQ7XG59O1xuIiwiXG5leHBvcnRzID0gbW9kdWxlLmV4cG9ydHMgPSB0cmltO1xuXG5mdW5jdGlvbiB0cmltKHN0cil7XG4gIHJldHVybiBzdHIucmVwbGFjZSgvXlxccyp8XFxzKiQvZywgJycpO1xufVxuXG5leHBvcnRzLmxlZnQgPSBmdW5jdGlvbihzdHIpe1xuICByZXR1cm4gc3RyLnJlcGxhY2UoL15cXHMqLywgJycpO1xufTtcblxuZXhwb3J0cy5yaWdodCA9IGZ1bmN0aW9uKHN0cil7XG4gIHJldHVybiBzdHIucmVwbGFjZSgvXFxzKiQvLCAnJyk7XG59O1xuIiwiXCJ1c2Ugc3RyaWN0XCI7XG52YXIgd2luZG93ID0gcmVxdWlyZShcImdsb2JhbC93aW5kb3dcIilcbnZhciBpc0Z1bmN0aW9uID0gcmVxdWlyZShcImlzLWZ1bmN0aW9uXCIpXG52YXIgcGFyc2VIZWFkZXJzID0gcmVxdWlyZShcInBhcnNlLWhlYWRlcnNcIilcbnZhciB4dGVuZCA9IHJlcXVpcmUoXCJ4dGVuZFwiKVxuXG5tb2R1bGUuZXhwb3J0cyA9IGNyZWF0ZVhIUlxuY3JlYXRlWEhSLlhNTEh0dHBSZXF1ZXN0ID0gd2luZG93LlhNTEh0dHBSZXF1ZXN0IHx8IG5vb3BcbmNyZWF0ZVhIUi5YRG9tYWluUmVxdWVzdCA9IFwid2l0aENyZWRlbnRpYWxzXCIgaW4gKG5ldyBjcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QoKSkgPyBjcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QgOiB3aW5kb3cuWERvbWFpblJlcXVlc3RcblxuZm9yRWFjaEFycmF5KFtcImdldFwiLCBcInB1dFwiLCBcInBvc3RcIiwgXCJwYXRjaFwiLCBcImhlYWRcIiwgXCJkZWxldGVcIl0sIGZ1bmN0aW9uKG1ldGhvZCkge1xuICAgIGNyZWF0ZVhIUlttZXRob2QgPT09IFwiZGVsZXRlXCIgPyBcImRlbFwiIDogbWV0aG9kXSA9IGZ1bmN0aW9uKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICAgICAgb3B0aW9ucyA9IGluaXRQYXJhbXModXJpLCBvcHRpb25zLCBjYWxsYmFjaylcbiAgICAgICAgb3B0aW9ucy5tZXRob2QgPSBtZXRob2QudG9VcHBlckNhc2UoKVxuICAgICAgICByZXR1cm4gX2NyZWF0ZVhIUihvcHRpb25zKVxuICAgIH1cbn0pXG5cbmZ1bmN0aW9uIGZvckVhY2hBcnJheShhcnJheSwgaXRlcmF0b3IpIHtcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGFycmF5Lmxlbmd0aDsgaSsrKSB7XG4gICAgICAgIGl0ZXJhdG9yKGFycmF5W2ldKVxuICAgIH1cbn1cblxuZnVuY3Rpb24gaXNFbXB0eShvYmope1xuICAgIGZvcih2YXIgaSBpbiBvYmope1xuICAgICAgICBpZihvYmouaGFzT3duUHJvcGVydHkoaSkpIHJldHVybiBmYWxzZVxuICAgIH1cbiAgICByZXR1cm4gdHJ1ZVxufVxuXG5mdW5jdGlvbiBpbml0UGFyYW1zKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICB2YXIgcGFyYW1zID0gdXJpXG5cbiAgICBpZiAoaXNGdW5jdGlvbihvcHRpb25zKSkge1xuICAgICAgICBjYWxsYmFjayA9IG9wdGlvbnNcbiAgICAgICAgaWYgKHR5cGVvZiB1cmkgPT09IFwic3RyaW5nXCIpIHtcbiAgICAgICAgICAgIHBhcmFtcyA9IHt1cmk6dXJpfVxuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcGFyYW1zID0geHRlbmQob3B0aW9ucywge3VyaTogdXJpfSlcbiAgICB9XG5cbiAgICBwYXJhbXMuY2FsbGJhY2sgPSBjYWxsYmFja1xuICAgIHJldHVybiBwYXJhbXNcbn1cblxuZnVuY3Rpb24gY3JlYXRlWEhSKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICBvcHRpb25zID0gaW5pdFBhcmFtcyh1cmksIG9wdGlvbnMsIGNhbGxiYWNrKVxuICAgIHJldHVybiBfY3JlYXRlWEhSKG9wdGlvbnMpXG59XG5cbmZ1bmN0aW9uIF9jcmVhdGVYSFIob3B0aW9ucykge1xuICAgIGlmKHR5cGVvZiBvcHRpb25zLmNhbGxiYWNrID09PSBcInVuZGVmaW5lZFwiKXtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKFwiY2FsbGJhY2sgYXJndW1lbnQgbWlzc2luZ1wiKVxuICAgIH1cblxuICAgIHZhciBjYWxsZWQgPSBmYWxzZVxuICAgIHZhciBjYWxsYmFjayA9IGZ1bmN0aW9uIGNiT25jZShlcnIsIHJlc3BvbnNlLCBib2R5KXtcbiAgICAgICAgaWYoIWNhbGxlZCl7XG4gICAgICAgICAgICBjYWxsZWQgPSB0cnVlXG4gICAgICAgICAgICBvcHRpb25zLmNhbGxiYWNrKGVyciwgcmVzcG9uc2UsIGJvZHkpXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICBmdW5jdGlvbiByZWFkeXN0YXRlY2hhbmdlKCkge1xuICAgICAgICBpZiAoeGhyLnJlYWR5U3RhdGUgPT09IDQpIHtcbiAgICAgICAgICAgIGxvYWRGdW5jKClcbiAgICAgICAgfVxuICAgIH1cblxuICAgIGZ1bmN0aW9uIGdldEJvZHkoKSB7XG4gICAgICAgIC8vIENocm9tZSB3aXRoIHJlcXVlc3RUeXBlPWJsb2IgdGhyb3dzIGVycm9ycyBhcnJvdW5kIHdoZW4gZXZlbiB0ZXN0aW5nIGFjY2VzcyB0byByZXNwb25zZVRleHRcbiAgICAgICAgdmFyIGJvZHkgPSB1bmRlZmluZWRcblxuICAgICAgICBpZiAoeGhyLnJlc3BvbnNlKSB7XG4gICAgICAgICAgICBib2R5ID0geGhyLnJlc3BvbnNlXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBib2R5ID0geGhyLnJlc3BvbnNlVGV4dCB8fCBnZXRYbWwoeGhyKVxuICAgICAgICB9XG5cbiAgICAgICAgaWYgKGlzSnNvbikge1xuICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICBib2R5ID0gSlNPTi5wYXJzZShib2R5KVxuICAgICAgICAgICAgfSBjYXRjaCAoZSkge31cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBib2R5XG4gICAgfVxuXG4gICAgdmFyIGZhaWx1cmVSZXNwb25zZSA9IHtcbiAgICAgICAgICAgICAgICBib2R5OiB1bmRlZmluZWQsXG4gICAgICAgICAgICAgICAgaGVhZGVyczoge30sXG4gICAgICAgICAgICAgICAgc3RhdHVzQ29kZTogMCxcbiAgICAgICAgICAgICAgICBtZXRob2Q6IG1ldGhvZCxcbiAgICAgICAgICAgICAgICB1cmw6IHVyaSxcbiAgICAgICAgICAgICAgICByYXdSZXF1ZXN0OiB4aHJcbiAgICAgICAgICAgIH1cblxuICAgIGZ1bmN0aW9uIGVycm9yRnVuYyhldnQpIHtcbiAgICAgICAgY2xlYXJUaW1lb3V0KHRpbWVvdXRUaW1lcilcbiAgICAgICAgaWYoIShldnQgaW5zdGFuY2VvZiBFcnJvcikpe1xuICAgICAgICAgICAgZXZ0ID0gbmV3IEVycm9yKFwiXCIgKyAoZXZ0IHx8IFwiVW5rbm93biBYTUxIdHRwUmVxdWVzdCBFcnJvclwiKSApXG4gICAgICAgIH1cbiAgICAgICAgZXZ0LnN0YXR1c0NvZGUgPSAwXG4gICAgICAgIHJldHVybiBjYWxsYmFjayhldnQsIGZhaWx1cmVSZXNwb25zZSlcbiAgICB9XG5cbiAgICAvLyB3aWxsIGxvYWQgdGhlIGRhdGEgJiBwcm9jZXNzIHRoZSByZXNwb25zZSBpbiBhIHNwZWNpYWwgcmVzcG9uc2Ugb2JqZWN0XG4gICAgZnVuY3Rpb24gbG9hZEZ1bmMoKSB7XG4gICAgICAgIGlmIChhYm9ydGVkKSByZXR1cm5cbiAgICAgICAgdmFyIHN0YXR1c1xuICAgICAgICBjbGVhclRpbWVvdXQodGltZW91dFRpbWVyKVxuICAgICAgICBpZihvcHRpb25zLnVzZVhEUiAmJiB4aHIuc3RhdHVzPT09dW5kZWZpbmVkKSB7XG4gICAgICAgICAgICAvL0lFOCBDT1JTIEdFVCBzdWNjZXNzZnVsIHJlc3BvbnNlIGRvZXNuJ3QgaGF2ZSBhIHN0YXR1cyBmaWVsZCwgYnV0IGJvZHkgaXMgZmluZVxuICAgICAgICAgICAgc3RhdHVzID0gMjAwXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBzdGF0dXMgPSAoeGhyLnN0YXR1cyA9PT0gMTIyMyA/IDIwNCA6IHhoci5zdGF0dXMpXG4gICAgICAgIH1cbiAgICAgICAgdmFyIHJlc3BvbnNlID0gZmFpbHVyZVJlc3BvbnNlXG4gICAgICAgIHZhciBlcnIgPSBudWxsXG5cbiAgICAgICAgaWYgKHN0YXR1cyAhPT0gMCl7XG4gICAgICAgICAgICByZXNwb25zZSA9IHtcbiAgICAgICAgICAgICAgICBib2R5OiBnZXRCb2R5KCksXG4gICAgICAgICAgICAgICAgc3RhdHVzQ29kZTogc3RhdHVzLFxuICAgICAgICAgICAgICAgIG1ldGhvZDogbWV0aG9kLFxuICAgICAgICAgICAgICAgIGhlYWRlcnM6IHt9LFxuICAgICAgICAgICAgICAgIHVybDogdXJpLFxuICAgICAgICAgICAgICAgIHJhd1JlcXVlc3Q6IHhoclxuICAgICAgICAgICAgfVxuICAgICAgICAgICAgaWYoeGhyLmdldEFsbFJlc3BvbnNlSGVhZGVycyl7IC8vcmVtZW1iZXIgeGhyIGNhbiBpbiBmYWN0IGJlIFhEUiBmb3IgQ09SUyBpbiBJRVxuICAgICAgICAgICAgICAgIHJlc3BvbnNlLmhlYWRlcnMgPSBwYXJzZUhlYWRlcnMoeGhyLmdldEFsbFJlc3BvbnNlSGVhZGVycygpKVxuICAgICAgICAgICAgfVxuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgZXJyID0gbmV3IEVycm9yKFwiSW50ZXJuYWwgWE1MSHR0cFJlcXVlc3QgRXJyb3JcIilcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gY2FsbGJhY2soZXJyLCByZXNwb25zZSwgcmVzcG9uc2UuYm9keSlcbiAgICB9XG5cbiAgICB2YXIgeGhyID0gb3B0aW9ucy54aHIgfHwgbnVsbFxuXG4gICAgaWYgKCF4aHIpIHtcbiAgICAgICAgaWYgKG9wdGlvbnMuY29ycyB8fCBvcHRpb25zLnVzZVhEUikge1xuICAgICAgICAgICAgeGhyID0gbmV3IGNyZWF0ZVhIUi5YRG9tYWluUmVxdWVzdCgpXG4gICAgICAgIH1lbHNle1xuICAgICAgICAgICAgeGhyID0gbmV3IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCgpXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIga2V5XG4gICAgdmFyIGFib3J0ZWRcbiAgICB2YXIgdXJpID0geGhyLnVybCA9IG9wdGlvbnMudXJpIHx8IG9wdGlvbnMudXJsXG4gICAgdmFyIG1ldGhvZCA9IHhoci5tZXRob2QgPSBvcHRpb25zLm1ldGhvZCB8fCBcIkdFVFwiXG4gICAgdmFyIGJvZHkgPSBvcHRpb25zLmJvZHkgfHwgb3B0aW9ucy5kYXRhIHx8IG51bGxcbiAgICB2YXIgaGVhZGVycyA9IHhoci5oZWFkZXJzID0gb3B0aW9ucy5oZWFkZXJzIHx8IHt9XG4gICAgdmFyIHN5bmMgPSAhIW9wdGlvbnMuc3luY1xuICAgIHZhciBpc0pzb24gPSBmYWxzZVxuICAgIHZhciB0aW1lb3V0VGltZXJcblxuICAgIGlmIChcImpzb25cIiBpbiBvcHRpb25zKSB7XG4gICAgICAgIGlzSnNvbiA9IHRydWVcbiAgICAgICAgaGVhZGVyc1tcImFjY2VwdFwiXSB8fCBoZWFkZXJzW1wiQWNjZXB0XCJdIHx8IChoZWFkZXJzW1wiQWNjZXB0XCJdID0gXCJhcHBsaWNhdGlvbi9qc29uXCIpIC8vRG9uJ3Qgb3ZlcnJpZGUgZXhpc3RpbmcgYWNjZXB0IGhlYWRlciBkZWNsYXJlZCBieSB1c2VyXG4gICAgICAgIGlmIChtZXRob2QgIT09IFwiR0VUXCIgJiYgbWV0aG9kICE9PSBcIkhFQURcIikge1xuICAgICAgICAgICAgaGVhZGVyc1tcImNvbnRlbnQtdHlwZVwiXSB8fCBoZWFkZXJzW1wiQ29udGVudC1UeXBlXCJdIHx8IChoZWFkZXJzW1wiQ29udGVudC1UeXBlXCJdID0gXCJhcHBsaWNhdGlvbi9qc29uXCIpIC8vRG9uJ3Qgb3ZlcnJpZGUgZXhpc3RpbmcgYWNjZXB0IGhlYWRlciBkZWNsYXJlZCBieSB1c2VyXG4gICAgICAgICAgICBib2R5ID0gSlNPTi5zdHJpbmdpZnkob3B0aW9ucy5qc29uKVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgeGhyLm9ucmVhZHlzdGF0ZWNoYW5nZSA9IHJlYWR5c3RhdGVjaGFuZ2VcbiAgICB4aHIub25sb2FkID0gbG9hZEZ1bmNcbiAgICB4aHIub25lcnJvciA9IGVycm9yRnVuY1xuICAgIC8vIElFOSBtdXN0IGhhdmUgb25wcm9ncmVzcyBiZSBzZXQgdG8gYSB1bmlxdWUgZnVuY3Rpb24uXG4gICAgeGhyLm9ucHJvZ3Jlc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIC8vIElFIG11c3QgZGllXG4gICAgfVxuICAgIHhoci5vbnRpbWVvdXQgPSBlcnJvckZ1bmNcbiAgICB4aHIub3BlbihtZXRob2QsIHVyaSwgIXN5bmMsIG9wdGlvbnMudXNlcm5hbWUsIG9wdGlvbnMucGFzc3dvcmQpXG4gICAgLy9oYXMgdG8gYmUgYWZ0ZXIgb3BlblxuICAgIGlmKCFzeW5jKSB7XG4gICAgICAgIHhoci53aXRoQ3JlZGVudGlhbHMgPSAhIW9wdGlvbnMud2l0aENyZWRlbnRpYWxzXG4gICAgfVxuICAgIC8vIENhbm5vdCBzZXQgdGltZW91dCB3aXRoIHN5bmMgcmVxdWVzdFxuICAgIC8vIG5vdCBzZXR0aW5nIHRpbWVvdXQgb24gdGhlIHhociBvYmplY3QsIGJlY2F1c2Ugb2Ygb2xkIHdlYmtpdHMgZXRjLiBub3QgaGFuZGxpbmcgdGhhdCBjb3JyZWN0bHlcbiAgICAvLyBib3RoIG5wbSdzIHJlcXVlc3QgYW5kIGpxdWVyeSAxLnggdXNlIHRoaXMga2luZCBvZiB0aW1lb3V0LCBzbyB0aGlzIGlzIGJlaW5nIGNvbnNpc3RlbnRcbiAgICBpZiAoIXN5bmMgJiYgb3B0aW9ucy50aW1lb3V0ID4gMCApIHtcbiAgICAgICAgdGltZW91dFRpbWVyID0gc2V0VGltZW91dChmdW5jdGlvbigpe1xuICAgICAgICAgICAgYWJvcnRlZD10cnVlLy9JRTkgbWF5IHN0aWxsIGNhbGwgcmVhZHlzdGF0ZWNoYW5nZVxuICAgICAgICAgICAgeGhyLmFib3J0KFwidGltZW91dFwiKVxuICAgICAgICAgICAgdmFyIGUgPSBuZXcgRXJyb3IoXCJYTUxIdHRwUmVxdWVzdCB0aW1lb3V0XCIpXG4gICAgICAgICAgICBlLmNvZGUgPSBcIkVUSU1FRE9VVFwiXG4gICAgICAgICAgICBlcnJvckZ1bmMoZSlcbiAgICAgICAgfSwgb3B0aW9ucy50aW1lb3V0IClcbiAgICB9XG5cbiAgICBpZiAoeGhyLnNldFJlcXVlc3RIZWFkZXIpIHtcbiAgICAgICAgZm9yKGtleSBpbiBoZWFkZXJzKXtcbiAgICAgICAgICAgIGlmKGhlYWRlcnMuaGFzT3duUHJvcGVydHkoa2V5KSl7XG4gICAgICAgICAgICAgICAgeGhyLnNldFJlcXVlc3RIZWFkZXIoa2V5LCBoZWFkZXJzW2tleV0pXG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9IGVsc2UgaWYgKG9wdGlvbnMuaGVhZGVycyAmJiAhaXNFbXB0eShvcHRpb25zLmhlYWRlcnMpKSB7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcihcIkhlYWRlcnMgY2Fubm90IGJlIHNldCBvbiBhbiBYRG9tYWluUmVxdWVzdCBvYmplY3RcIilcbiAgICB9XG5cbiAgICBpZiAoXCJyZXNwb25zZVR5cGVcIiBpbiBvcHRpb25zKSB7XG4gICAgICAgIHhoci5yZXNwb25zZVR5cGUgPSBvcHRpb25zLnJlc3BvbnNlVHlwZVxuICAgIH1cblxuICAgIGlmIChcImJlZm9yZVNlbmRcIiBpbiBvcHRpb25zICYmXG4gICAgICAgIHR5cGVvZiBvcHRpb25zLmJlZm9yZVNlbmQgPT09IFwiZnVuY3Rpb25cIlxuICAgICkge1xuICAgICAgICBvcHRpb25zLmJlZm9yZVNlbmQoeGhyKVxuICAgIH1cblxuICAgIHhoci5zZW5kKGJvZHkpXG5cbiAgICByZXR1cm4geGhyXG5cblxufVxuXG5mdW5jdGlvbiBnZXRYbWwoeGhyKSB7XG4gICAgaWYgKHhoci5yZXNwb25zZVR5cGUgPT09IFwiZG9jdW1lbnRcIikge1xuICAgICAgICByZXR1cm4geGhyLnJlc3BvbnNlWE1MXG4gICAgfVxuICAgIHZhciBmaXJlZm94QnVnVGFrZW5FZmZlY3QgPSB4aHIuc3RhdHVzID09PSAyMDQgJiYgeGhyLnJlc3BvbnNlWE1MICYmIHhoci5yZXNwb25zZVhNTC5kb2N1bWVudEVsZW1lbnQubm9kZU5hbWUgPT09IFwicGFyc2VyZXJyb3JcIlxuICAgIGlmICh4aHIucmVzcG9uc2VUeXBlID09PSBcIlwiICYmICFmaXJlZm94QnVnVGFrZW5FZmZlY3QpIHtcbiAgICAgICAgcmV0dXJuIHhoci5yZXNwb25zZVhNTFxuICAgIH1cblxuICAgIHJldHVybiBudWxsXG59XG5cbmZ1bmN0aW9uIG5vb3AoKSB7fVxuIiwibW9kdWxlLmV4cG9ydHMgPSBleHRlbmRcblxudmFyIGhhc093blByb3BlcnR5ID0gT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eTtcblxuZnVuY3Rpb24gZXh0ZW5kKCkge1xuICAgIHZhciB0YXJnZXQgPSB7fVxuXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXVxuXG4gICAgICAgIGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHtcbiAgICAgICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkge1xuICAgICAgICAgICAgICAgIHRhcmdldFtrZXldID0gc291cmNlW2tleV1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH1cblxuICAgIHJldHVybiB0YXJnZXRcbn1cbiIsImNvbnN0IGlzQmFzaWNQcm9wZXJ0eSA9IChwcmVkaWNhdGVPYmplY3RNYXApID0+XG4gIFtcInRleHRcIiwgXCJzZWxlY3RcIiwgXCJtdWx0aXNlbGVjdFwiLCBcImRhdGFibGVcIiwgXCJuYW1lc1wiXS5pbmRleE9mKHByZWRpY2F0ZU9iamVjdE1hcC5wcm9wZXJ0eVR5cGUpID4gLTE7XG5cbmNvbnN0IGNvbHVtbk1hcElzQ29tcGxldGUgPSAocHJlZGljYXRlT2JqZWN0TWFwKSA9PlxuICBwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwICYmXG4gIHR5cGVvZiBwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwLmNvbHVtbiAhPT0gXCJ1bmRlZmluZWRcIiAmJlxuICBwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwLmNvbHVtbiAhPT0gbnVsbDtcblxuY29uc3Qgam9pbkNvbmRpdGlvbk1hcElzQ29tcGxldGUgPSAocHJlZGljYXRlT2JqZWN0TWFwKSA9PlxuICBwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwICYmXG4gICAgcHJlZGljYXRlT2JqZWN0TWFwLm9iamVjdE1hcC5wYXJlbnRUcmlwbGVzTWFwICYmXG4gICAgcHJlZGljYXRlT2JqZWN0TWFwLm9iamVjdE1hcC5qb2luQ29uZGl0aW9uICYmXG4gICAgdHlwZW9mIHByZWRpY2F0ZU9iamVjdE1hcC5vYmplY3RNYXAuam9pbkNvbmRpdGlvbi5wYXJlbnQgIT09IFwidW5kZWZpbmVkXCIgJiZcbiAgICB0eXBlb2YgcHJlZGljYXRlT2JqZWN0TWFwLm9iamVjdE1hcC5qb2luQ29uZGl0aW9uLmNoaWxkICE9PSBcInVuZGVmaW5lZFwiO1xuXG5jb25zdCBwcm9wZXJ0eU1hcHBpbmdJc0NvbXBsZXRlID0gKHByZWRpY2F0ZU9iamVjdE1hcCkgPT4ge1xuICBpZiAodHlwZW9mIHByZWRpY2F0ZU9iamVjdE1hcCA9PT0gXCJ1bmRlZmluZWRcIikgeyByZXR1cm4gZmFsc2U7IH1cblxuICBpZiAoaXNCYXNpY1Byb3BlcnR5KHByZWRpY2F0ZU9iamVjdE1hcCkpIHtcbiAgICByZXR1cm4gY29sdW1uTWFwSXNDb21wbGV0ZShwcmVkaWNhdGVPYmplY3RNYXApO1xuICB9XG5cbiAgaWYgKHByZWRpY2F0ZU9iamVjdE1hcC5wcm9wZXJ0eVR5cGUgPT09IFwicmVsYXRpb25cIikge1xuICAgIHJldHVybiBqb2luQ29uZGl0aW9uTWFwSXNDb21wbGV0ZShwcmVkaWNhdGVPYmplY3RNYXApO1xuICB9XG5cbiAgcmV0dXJuIGZhbHNlO1xufTtcblxuY29uc3QgZ2V0Q29sdW1uVmFsdWUgPSAocHJlZGljYXRlT2JqZWN0TWFwKSA9PiB7XG4gIGlmICghcHJlZGljYXRlT2JqZWN0TWFwKSB7XG4gICAgcmV0dXJuIG51bGw7XG4gIH1cblxuICBpZiAoaXNCYXNpY1Byb3BlcnR5KHByZWRpY2F0ZU9iamVjdE1hcCkpIHtcbiAgICByZXR1cm4gcHJlZGljYXRlT2JqZWN0TWFwLm9iamVjdE1hcCAmJiBwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwLmNvbHVtbiA/IHByZWRpY2F0ZU9iamVjdE1hcC5vYmplY3RNYXAuY29sdW1uIDogbnVsbDtcbiAgfVxuXG4gIGlmIChwcmVkaWNhdGVPYmplY3RNYXAucHJvcGVydHlUeXBlID09PSBcInJlbGF0aW9uXCIpIHtcbiAgICByZXR1cm4gcHJlZGljYXRlT2JqZWN0TWFwLm9iamVjdE1hcCAmJlxuICAgICAgcHJlZGljYXRlT2JqZWN0TWFwLm9iamVjdE1hcC5qb2luQ29uZGl0aW9uICYmXG4gICAgICBwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwLmpvaW5Db25kaXRpb24uY2hpbGQgPyBwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwLmpvaW5Db25kaXRpb24uY2hpbGQgOiBudWxsO1xuICB9XG5cbiAgcmV0dXJuIG51bGw7XG59O1xuXG5leHBvcnQgeyBwcm9wZXJ0eU1hcHBpbmdJc0NvbXBsZXRlLCBpc0Jhc2ljUHJvcGVydHksIGdldENvbHVtblZhbHVlIH0iLCJpbXBvcnQgeyBvblVwbG9hZEZpbGVTZWxlY3QgfSBmcm9tIFwiLi9hY3Rpb25zL3VwbG9hZFwiXG5pbXBvcnQgeyBmZXRjaEJ1bGtVcGxvYWRlZE1ldGFkYXRhIH0gZnJvbSBcIi4vYWN0aW9ucy9mZXRjaC1idWxrdXBsb2FkZWQtbWV0YWRhdGFcIjtcbmltcG9ydCB7IHNlbGVjdENvbGxlY3Rpb24gfSBmcm9tIFwiLi9hY3Rpb25zL3NlbGVjdC1jb2xsZWN0aW9uXCI7XG5pbXBvcnQge1xuICBhZGRQcmVkaWNhdGVPYmplY3RNYXAsXG4gIHJlbW92ZVByZWRpY2F0ZU9iamVjdE1hcCxcbiAgYWRkQ3VzdG9tUHJvcGVydHksXG4gIHJlbW92ZUN1c3RvbVByb3BlcnR5LFxufSBmcm9tIFwiLi9hY3Rpb25zL3ByZWRpY2F0ZS1vYmplY3QtbWFwcGluZ3NcIjtcblxuaW1wb3J0IHsgcHVibGlzaE1hcHBpbmdzIH0gZnJvbSBcIi4vYWN0aW9ucy9wdWJsaXNoLW1hcHBpbmdzXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uIGFjdGlvbnNNYWtlcihuYXZpZ2F0ZVRvLCBkaXNwYXRjaCkge1xuICByZXR1cm4ge1xuICAgIG9uVXBsb2FkRmlsZVNlbGVjdDogb25VcGxvYWRGaWxlU2VsZWN0KG5hdmlnYXRlVG8sIGRpc3BhdGNoKSxcblxuICAgIC8vIGxvYWRpbmcgaW1wb3J0IGRhdGFcbiAgICBvblNlbGVjdENvbGxlY3Rpb246IChjb2xsZWN0aW9uKSA9PiBkaXNwYXRjaChzZWxlY3RDb2xsZWN0aW9uKGNvbGxlY3Rpb24pKSxcbiAgICBvbkxvYWRNb3JlQ2xpY2s6IChuZXh0VXJsLCBjb2xsZWN0aW9uKSA9PiBkaXNwYXRjaChzZWxlY3RDb2xsZWN0aW9uKGNvbGxlY3Rpb24sIG5leHRVcmwpKSxcbiAgICBvbkZldGNoQnVsa1VwbG9hZGVkTWV0YWRhdGE6ICh2cmVJZCwgbWFwcGluZ3NGcm9tVXJsKSA9PiBkaXNwYXRjaChmZXRjaEJ1bGtVcGxvYWRlZE1ldGFkYXRhKHZyZUlkLCBtYXBwaW5nc0Zyb21VcmwpKSxcblxuICAgIC8vIENsb3NpbmcgaW5mb3JtYXRpdmUgbWVzc2FnZXNcbiAgICBvbkNsb3NlTWVzc2FnZTogKG1lc3NhZ2VJZCkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiVE9HR0xFX01FU1NBR0VcIiwgbWVzc2FnZUlkOiBtZXNzYWdlSWR9KSxcblxuICAgIC8vIE1hcHBpbmcgY29sbGVjdGlvbnMgYXJjaGV0eXBlc1xuICAgIG9uTWFwQ29sbGVjdGlvbkFyY2hldHlwZTogKGNvbGxlY3Rpb24sIHZhbHVlKSA9PlxuICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiTUFQX0NPTExFQ1RJT05fQVJDSEVUWVBFXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHZhbHVlOiB2YWx1ZX0pLFxuXG5cbiAgICAvLyBDb25uZWN0aW5nIGRhdGFcbiAgICBvbklnbm9yZUNvbHVtblRvZ2dsZTogKGNvbGxlY3Rpb24sIHZhcmlhYmxlTmFtZSkgPT5cbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlRPR0dMRV9JR05PUkVEX0NPTFVNTlwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCB2YXJpYWJsZU5hbWU6IHZhcmlhYmxlTmFtZX0pLFxuXG4gICAgb25BZGRQcmVkaWNhdGVPYmplY3RNYXA6IChwcmVkaWNhdGVOYW1lLCBvYmplY3ROYW1lLCBwcm9wZXJ0eVR5cGUpID0+XG4gICAgICBkaXNwYXRjaChhZGRQcmVkaWNhdGVPYmplY3RNYXAocHJlZGljYXRlTmFtZSwgb2JqZWN0TmFtZSwgcHJvcGVydHlUeXBlKSksXG5cbiAgICBvblJlbW92ZVByZWRpY2F0ZU9iamVjdE1hcDogKHByZWRpY2F0ZU5hbWUsIG9iamVjdE5hbWUpID0+IGRpc3BhdGNoKHJlbW92ZVByZWRpY2F0ZU9iamVjdE1hcChwcmVkaWNhdGVOYW1lLCBvYmplY3ROYW1lKSksXG5cbiAgICBvbkFkZEN1c3RvbVByb3BlcnR5OiAobmFtZSwgdHlwZSkgPT4gZGlzcGF0Y2goYWRkQ3VzdG9tUHJvcGVydHkobmFtZSwgdHlwZSkpLFxuXG4gICAgb25SZW1vdmVDdXN0b21Qcm9wZXJ0eTogKGluZGV4KSA9PiBkaXNwYXRjaChyZW1vdmVDdXN0b21Qcm9wZXJ0eShpbmRleCkpLFxuXG4gICAgb25QdWJsaXNoRGF0YTogKCkgPT4gZGlzcGF0Y2gocHVibGlzaE1hcHBpbmdzKG5hdmlnYXRlVG8pKVxuICB9O1xufVxuIiwiaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5pbXBvcnQgeyBzZWxlY3RDb2xsZWN0aW9uIH0gZnJvbSBcIi4vc2VsZWN0LWNvbGxlY3Rpb25cIlxuXG5jb25zdCBmZXRjaEJ1bGtVcGxvYWRlZE1ldGFkYXRhID0gKHZyZUlkLCBtYXBwaW5nc0Zyb21VcmwpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpICA9PiB7XG4gIGxldCBsb2NhdGlvbiA9IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9idWxrLXVwbG9hZC8ke3ZyZUlkfWA7XG4gIHhoci5nZXQobG9jYXRpb24sIHtoZWFkZXJzOiB7XCJBdXRob3JpemF0aW9uXCI6IGdldFN0YXRlKCkudXNlcmRhdGEudXNlcklkfX0sIGZ1bmN0aW9uIChlcnIsIHJlc3AsIGJvZHkpIHtcbiAgICBjb25zdCByZXNwb25zZURhdGEgPSBKU09OLnBhcnNlKGJvZHkpO1xuICAgIGRpc3BhdGNoKHt0eXBlOiBcIkZJTklTSF9VUExPQURcIiwgZGF0YTogcmVzcG9uc2VEYXRhfSk7XG5cbiAgICBpZiAocmVzcG9uc2VEYXRhLmNvbGxlY3Rpb25zICYmIHJlc3BvbnNlRGF0YS5jb2xsZWN0aW9ucy5sZW5ndGgpIHtcbiAgICAgIGRpc3BhdGNoKHNlbGVjdENvbGxlY3Rpb24ocmVzcG9uc2VEYXRhLmNvbGxlY3Rpb25zWzBdLm5hbWUpKTtcbiAgICB9XG5cbiAgICBpZiAobWFwcGluZ3NGcm9tVXJsKSB7XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJNQVBfQ09MTEVDVElPTl9BUkNIRVRZUEVTXCIsIGRhdGE6IG1hcHBpbmdzRnJvbVVybH0pO1xuXG4gICAgfVxuICB9KTtcbn07XG5cbmV4cG9ydCB7IGZldGNoQnVsa1VwbG9hZGVkTWV0YWRhdGEgfTsiLCJpbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcblxuY29uc3QgZmV0Y2hNeVZyZXMgPSAodG9rZW4sIGNhbGxiYWNrKSA9PiAoZGlzcGF0Y2gpID0+IHtcbiAgeGhyKHByb2Nlc3MuZW52LnNlcnZlciArIFwiL3YyLjEvc3lzdGVtL3VzZXJzL21lL3ZyZXNcIiwge1xuICAgIGhlYWRlcnM6IHtcbiAgICAgIFwiQXV0aG9yaXphdGlvblwiOiB0b2tlblxuICAgIH1cbiAgfSwgKGVyciwgcmVzcCwgYm9keSkgPT4ge1xuICAgIGNvbnN0IHZyZURhdGEgPSBKU09OLnBhcnNlKGJvZHkpO1xuICAgIGRpc3BhdGNoKHt0eXBlOiBcIkxPR0lOXCIsIGRhdGE6IHRva2VuLCB2cmVEYXRhOiB2cmVEYXRhfSk7XG4gICAgY2FsbGJhY2sodnJlRGF0YSk7XG4gIH0pO1xufTtcblxuZXhwb3J0IHsgZmV0Y2hNeVZyZXMgfSIsImltcG9ydCB7Z2V0Q29sdW1uVmFsdWV9IGZyb20gXCIuLi9hY2Nlc3NvcnMvcHJvcGVydHktbWFwcGluZ3NcIjtcbmNvbnN0IGFkZFByZWRpY2F0ZU9iamVjdE1hcCA9IChwcmVkaWNhdGUsIG9iamVjdCwgcHJvcGVydHlUeXBlKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG4gIGNvbnN0IHthY3RpdmVDb2xsZWN0aW9uOiB7IG5hbWUgOiBzdWJqZWN0Q29sbGVjdGlvbiB9fSA9IGdldFN0YXRlKCk7XG5cbiAgZGlzcGF0Y2goe1xuICAgIHR5cGU6IFwiU0VUX1BSRURJQ0FURV9PQkpFQ1RfTUFQUElOR1wiLFxuICAgIHN1YmplY3RDb2xsZWN0aW9uOiBzdWJqZWN0Q29sbGVjdGlvbixcbiAgICBwcmVkaWNhdGU6IHByZWRpY2F0ZSxcbiAgICBvYmplY3Q6IG9iamVjdCxcbiAgICBwcm9wZXJ0eVR5cGU6IHByb3BlcnR5VHlwZVxuICB9KVxufTtcblxuY29uc3QgcmVtb3ZlUHJlZGljYXRlT2JqZWN0TWFwID0gKHByZWRpY2F0ZSwgb2JqZWN0KSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG4gIGNvbnN0IHthY3RpdmVDb2xsZWN0aW9uOiB7IG5hbWUgOiBzdWJqZWN0Q29sbGVjdGlvbiB9fSA9IGdldFN0YXRlKCk7XG5cbiAgZGlzcGF0Y2goe1xuICAgIHR5cGU6IFwiUkVNT1ZFX1BSRURJQ0FURV9PQkpFQ1RfTUFQUElOR1wiLFxuICAgIHN1YmplY3RDb2xsZWN0aW9uOiBzdWJqZWN0Q29sbGVjdGlvbixcbiAgICBwcmVkaWNhdGU6IHByZWRpY2F0ZSxcbiAgICBvYmplY3Q6IG9iamVjdFxuICB9KTtcbn07XG5cblxuY29uc3QgYWRkQ3VzdG9tUHJvcGVydHkgPSAobmFtZSwgdHlwZSkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuICBjb25zdCB7IGFjdGl2ZUNvbGxlY3Rpb246IHsgbmFtZTogY29sbGVjdGlvbk5hbWUgfX0gPSBnZXRTdGF0ZSgpO1xuXG4gIGRpc3BhdGNoKHtcbiAgICB0eXBlOiBcIkFERF9DVVNUT01fUFJPUEVSVFlcIixcbiAgICBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uTmFtZSxcbiAgICBwcm9wZXJ0eU5hbWU6IG5hbWUsXG4gICAgcHJvcGVydHlUeXBlOiB0eXBlXG4gIH0pO1xufTtcblxuY29uc3QgcmVtb3ZlQ3VzdG9tUHJvcGVydHkgPSAoaW5kZXgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcbiAgY29uc3Qge1xuICAgIGFjdGl2ZUNvbGxlY3Rpb246IHsgbmFtZTogY29sbGVjdGlvbk5hbWUgfSxcbiAgICBwcmVkaWNhdGVPYmplY3RNYXBwaW5nczogYWxsUHJlZGljYXRlT2JqZWN0TWFwcGluZ3MsXG4gICAgY3VzdG9tUHJvcGVydGllczogY3VzdG9tUHJvcGVydGllc1xuICB9ID0gZ2V0U3RhdGUoKTtcblxuICBjb25zdCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncyA9IGFsbFByZWRpY2F0ZU9iamVjdE1hcHBpbmdzW2NvbGxlY3Rpb25OYW1lXSB8fCBbXTtcbiAgY29uc3QgY3VzdG9tUHJvcGVydHkgPSBjdXN0b21Qcm9wZXJ0aWVzW2NvbGxlY3Rpb25OYW1lXVtpbmRleF07XG5cbiAgY29uc3QgcHJlZGljYXRlT2JqZWN0TWFwcGluZyA9IHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzLmZpbmQoKHBvbSkgPT4gcG9tLnByZWRpY2F0ZSA9PT0gY3VzdG9tUHJvcGVydHkucHJvcGVydHlOYW1lKTtcblxuICBpZiAocHJlZGljYXRlT2JqZWN0TWFwcGluZykge1xuICAgIGRpc3BhdGNoKHtcbiAgICAgIHR5cGU6IFwiUkVNT1ZFX1BSRURJQ0FURV9PQkpFQ1RfTUFQUElOR1wiLFxuICAgICAgc3ViamVjdENvbGxlY3Rpb246IGNvbGxlY3Rpb25OYW1lLFxuICAgICAgcHJlZGljYXRlOiBjdXN0b21Qcm9wZXJ0eS5wcm9wZXJ0eU5hbWUsXG4gICAgICBvYmplY3Q6IGdldENvbHVtblZhbHVlKHByZWRpY2F0ZU9iamVjdE1hcHBpbmcpXG4gICAgfSk7XG4gIH1cbiAgZGlzcGF0Y2goe1xuICAgIHR5cGU6IFwiUkVNT1ZFX0NVU1RPTV9QUk9QRVJUWVwiLFxuICAgIGNvbGxlY3Rpb246IGNvbGxlY3Rpb25OYW1lLFxuICAgIGluZGV4OiBpbmRleFxuICB9KVxufTtcblxuXG5leHBvcnQgeyBhZGRQcmVkaWNhdGVPYmplY3RNYXAsIHJlbW92ZVByZWRpY2F0ZU9iamVjdE1hcCwgYWRkQ3VzdG9tUHJvcGVydHksIHJlbW92ZUN1c3RvbVByb3BlcnR5IH1cbiIsImltcG9ydCBnZW5lcmF0ZVJtbE1hcHBpbmcgZnJvbSBcIi4uL3V0aWwvZ2VuZXJhdGUtcm1sLW1hcHBpbmdcIjtcbmltcG9ydCB7ZmV0Y2hNeVZyZXN9IGZyb20gXCIuL2ZldGNoLW15LXZyZXNcIjtcbmltcG9ydCB4aHIgZnJvbSBcInhoclwiXG5pbXBvcnQge3NlbGVjdENvbGxlY3Rpb259IGZyb20gXCIuL3NlbGVjdC1jb2xsZWN0aW9uXCI7XG5cbmNvbnN0IHB1Ymxpc2hNYXBwaW5ncyA9IChuYXZpZ2F0ZVRvKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG4gIGNvbnN0IHtcbiAgICBpbXBvcnREYXRhOiB7IHZyZSwgZXhlY3V0ZU1hcHBpbmdVcmwgfSxcbiAgICBtYXBwaW5nczogeyBjb2xsZWN0aW9ucyB9LFxuICAgIHVzZXJkYXRhOiB7IHVzZXJJZCB9LFxuICAgIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzLFxuICAgIGFjdGl2ZUNvbGxlY3Rpb25cbiAgfSA9IGdldFN0YXRlKCk7XG5cbiAgY29uc3QganNvbkxkID0gZ2VuZXJhdGVSbWxNYXBwaW5nKHZyZSwgY29sbGVjdGlvbnMsIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzKTtcblxuICBjb25zb2xlLmxvZyhKU09OLnN0cmluZ2lmeShqc29uTGQsIG51bGwsIDIpKTtcblxuICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX1NUQVJUXCJ9KTtcbiAgeGhyKHtcbiAgICB1cmw6IGV4ZWN1dGVNYXBwaW5nVXJsLFxuICAgIG1ldGhvZDogXCJQT1NUXCIsXG4gICAgaGVhZGVyczoge1xuICAgICAgXCJBdXRob3JpemF0aW9uXCI6IHVzZXJJZCxcbiAgICAgIFwiQ29udGVudC10eXBlXCI6IFwiYXBwbGljYXRpb24vbGQranNvblwiXG4gICAgfSxcbiAgICBkYXRhOiBKU09OLnN0cmluZ2lmeShqc29uTGQpXG4gIH0sIChlcnIsIHJlc3AsIGJvZHkpID0+IHtcbiAgICBpZiAoZXJyKSB7XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX0hBRF9FUlJPUlwifSlcbiAgICB9IGVsc2Uge1xuICAgICAgY29uc3QgeyBzdWNjZXNzIH0gPSBKU09OLnBhcnNlKGJvZHkpO1xuICAgICAgaWYgKHN1Y2Nlc3MpIHtcbiAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9TVUNDRUVERURcIn0pO1xuICAgICAgICBkaXNwYXRjaChmZXRjaE15VnJlcyh1c2VySWQsICgpID0+IG5hdmlnYXRlVG8oXCJyb290XCIpKSk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX0hBRF9FUlJPUlwifSk7XG4gICAgICAgIGRpc3BhdGNoKHNlbGVjdENvbGxlY3Rpb24oYWN0aXZlQ29sbGVjdGlvbi5uYW1lLCBudWxsLCB0cnVlKSk7XG4gICAgICB9XG4gICAgfVxuICAgIGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfRklOSVNIRURcIn0pO1xuICB9KTtcblxuXG4vKiAgY29uc3QgcmVxID0gbmV3IFhNTEh0dHBSZXF1ZXN0KCk7XG4gIHJlcS5vcGVuKCdQT1NUJywgZXhlY3V0ZU1hcHBpbmdVcmwsIHRydWUpO1xuICByZXEuc2V0UmVxdWVzdEhlYWRlcihcIkF1dGhvcml6YXRpb25cIiwgdXNlcklkKTtcbiAgcmVxLnNldFJlcXVlc3RIZWFkZXIoXCJDb250ZW50LXR5cGVcIiwgXCJhcHBsaWNhdGlvbi9sZCtqc29uXCIpO1xuXG4gIGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfU1RBUlRcIn0pO1xuXG4gIGxldCBwb3MgPSAwO1xuICByZXEub25yZWFkeXN0YXRlY2hhbmdlID0gZnVuY3Rpb24gaGFuZGxlRGF0YSgpIHtcbiAgICBpZiAocmVxLnJlYWR5U3RhdGUgIT0gbnVsbCAmJiAocmVxLnJlYWR5U3RhdGUgPCAzIHx8IHJlcS5zdGF0dXMgIT0gMjAwKSkge1xuICAgICAgcmV0dXJuXG4gICAgfVxuICAgIGxldCBuZXdQYXJ0ID0gcmVxLnJlc3BvbnNlVGV4dC5zdWJzdHIocG9zKTtcbiAgICBwb3MgPSByZXEucmVzcG9uc2VUZXh0Lmxlbmd0aDtcbiAgICBuZXdQYXJ0LnNwbGl0KFwiXFxuXCIpLmZvckVhY2gobGluZSA9PiB7XG4gICAgICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX1NUQVRVU19VUERBVEVcIiwgZGF0YTogbGluZX0pO1xuICAgIH0pO1xuICB9O1xuXG4gIHJlcS5vbmxvYWQgPSBmdW5jdGlvbiAoKSB7XG4gICAgaWYgKHJlcS5zdGF0dXMgPiA0MDApIHtcbiAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfSEFEX0VSUk9SXCJ9KVxuICAgIH0gZWxzZSB7XG4gICAgICBkaXNwYXRjaChmdW5jdGlvbiAoZGlzcGF0Y2gsIGdldFN0YXRlKSB7XG4gICAgICAgIHZhciBzdGF0ZSA9IGdldFN0YXRlKCk7XG4gICAgICAgIGlmIChzdGF0ZS5pbXBvcnREYXRhLnB1Ymxpc2hFcnJvckNvdW50ID09PSAwKSB7XG4gICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiUFVCTElTSF9TVUNDRUVERURcIn0pO1xuICAgICAgICAgIGRpc3BhdGNoKGZldGNoTXlWcmVzKHVzZXJJZCwgKCkgPT4gbmF2aWdhdGVUbyhcInJvb3RcIikpKTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX0hBRF9FUlJPUlwifSk7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH1cbiAgICBkaXNwYXRjaCh7dHlwZTogXCJQVUJMSVNIX0ZJTklTSEVEXCJ9KTtcbiAgfTtcbiAgcmVxLnNlbmQoSlNPTi5zdHJpbmdpZnkoanNvbkxkKSk7Ki9cbn07XG5cbmV4cG9ydCB7IHB1Ymxpc2hNYXBwaW5ncyB9XG4iLCJpbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcblxuY29uc3Qgc2VsZWN0Q29sbGVjdGlvbiA9IChjb2xsZWN0aW9uLCBhbHRVcmwgPSBudWxsLCBvbmx5RXJyb3JzID0gZmFsc2UpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcbiAgY29uc3QgeyBpbXBvcnREYXRhOiB7IGNvbGxlY3Rpb25zIH0sIHVzZXJkYXRhOiB7IHVzZXJJZCB9fSA9IGdldFN0YXRlKCk7XG4gIGNvbnN0IHNlbGVjdGVkQ29sbGVjdGlvbiA9IGNvbGxlY3Rpb25zLmZpbmQoKGNvbCkgPT4gY29sLm5hbWUgPT09IGNvbGxlY3Rpb24pO1xuXG4gIGlmICh1c2VySWQgJiYgY29sbGVjdGlvbnMgJiYgc2VsZWN0ZWRDb2xsZWN0aW9uICYmIHNlbGVjdGVkQ29sbGVjdGlvbi5kYXRhVXJsKSB7XG4gICAgZGlzcGF0Y2goe3R5cGU6IFwiQUNUSVZFX0NPTExFQ1RJT05fUEVORElOR1wifSk7XG4gICAgeGhyLmdldCgoYWx0VXJsIHx8IHNlbGVjdGVkQ29sbGVjdGlvbi5kYXRhVXJsKSArIChvbmx5RXJyb3JzID8gXCI/b25seUVycm9ycz10cnVlXCIgOiBcIlwiKSwge1xuICAgICAgaGVhZGVyczogeyBcIkF1dGhvcml6YXRpb25cIjogdXNlcklkIH1cbiAgICB9LCAoZXJyLCByZXNwLCBib2R5KSA9PiB7XG4gICAgICBpZiAoZXJyKSB7XG4gICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkFDVElWRV9DT0xMRUNUSU9OX0ZFVENIX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlcnJ9KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHRyeSB7XG4gICAgICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9BQ1RJVkVfQ09MTEVDVElPTlwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBkYXRhOiBKU09OLnBhcnNlKGJvZHkpfSk7XG4gICAgICAgIH0gY2F0Y2goZSkge1xuICAgICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkFDVElWRV9DT0xMRUNUSU9OX0ZFVENIX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlfSlcbiAgICAgICAgfVxuICAgICAgfVxuICAgICAgZGlzcGF0Y2goe3R5cGU6IFwiQUNUSVZFX0NPTExFQ1RJT05fRE9ORVwifSk7XG4gICAgfSk7XG4gIH1cbn07XG5cblxuZXhwb3J0IHsgc2VsZWN0Q29sbGVjdGlvbiB9IiwiaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5pbXBvcnQgeyBzZWxlY3RDb2xsZWN0aW9uIH0gZnJvbSBcIi4vc2VsZWN0LWNvbGxlY3Rpb25cIjtcblxuY29uc3Qgb25VcGxvYWRGaWxlU2VsZWN0ID0gKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSA9PiAoZmlsZXMsIGlzUmV1cGxvYWQgPSBmYWxzZSkgPT4ge1xuICBsZXQgZmlsZSA9IGZpbGVzWzBdO1xuICBsZXQgZm9ybURhdGEgPSBuZXcgRm9ybURhdGEoKTtcbiAgZm9ybURhdGEuYXBwZW5kKFwiZmlsZVwiLCBmaWxlKTtcbiAgZGlzcGF0Y2goe3R5cGU6IFwiU1RBUlRfVVBMT0FEXCJ9KTtcbiAgZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuICAgIHZhciBzdGF0ZSA9IGdldFN0YXRlKCk7XG4gICAgdmFyIHJlcSA9IG5ldyBYTUxIdHRwUmVxdWVzdCgpO1xuICAgIHJlcS5vcGVuKCdQT1NUJywgcHJvY2Vzcy5lbnYuc2VydmVyICsgXCIvdjIuMS9idWxrLXVwbG9hZFwiLCB0cnVlKTtcbiAgICByZXEuc2V0UmVxdWVzdEhlYWRlcihcIkF1dGhvcml6YXRpb25cIiwgc3RhdGUudXNlcmRhdGEudXNlcklkKTtcbiAgICB2YXIgcG9zID0gMDtcbiAgICByZXEub25yZWFkeXN0YXRlY2hhbmdlID0gZnVuY3Rpb24gaGFuZGxlRGF0YSgpIHtcbiAgICAgIGlmIChyZXEucmVhZHlTdGF0ZSAhPSBudWxsICYmIChyZXEucmVhZHlTdGF0ZSA8IDMgfHwgcmVxLnN0YXR1cyAhPSAyMDApKSB7XG4gICAgICAgIHJldHVyblxuICAgICAgfVxuICAgICAgdmFyIG5ld1BhcnQgPSByZXEucmVzcG9uc2VUZXh0LnN1YnN0cihwb3MpO1xuICAgICAgcG9zID0gcmVxLnJlc3BvbnNlVGV4dC5sZW5ndGg7XG4gICAgICBuZXdQYXJ0LnNwbGl0KFwiXFxuXCIpLmZvckVhY2goKGxpbmUsIGlkeCkgPT4ge1xuICAgICAgICBpZiAoaWR4ICUgMjEgPT09IDApIHsgZGlzcGF0Y2goe3R5cGU6IFwiVVBMT0FEX1NUQVRVU19VUERBVEVcIiwgZGF0YTogbGluZX0pOyB9XG4gICAgICB9KTtcbiAgICB9O1xuICAgIHJlcS5vbmxvYWQgPSBmdW5jdGlvbiAoKSB7XG4gICAgICBsZXQgbG9jYXRpb24gPSByZXEuZ2V0UmVzcG9uc2VIZWFkZXIoXCJsb2NhdGlvblwiKTtcbiAgICAgIHhoci5nZXQobG9jYXRpb24sIHtoZWFkZXJzOiB7XCJBdXRob3JpemF0aW9uXCI6IHN0YXRlLnVzZXJkYXRhLnVzZXJJZH19LCBmdW5jdGlvbiAoZXJyLCByZXNwLCBib2R5KSB7XG4gICAgICAgIGNvbnN0IHJlc3BvbnNlRGF0YSA9IEpTT04ucGFyc2UoYm9keSk7XG4gICAgICAgIGRpc3BhdGNoKHt0eXBlOiBcIkZJTklTSF9VUExPQURcIiwgZGF0YTogcmVzcG9uc2VEYXRhLCB1cGxvYWRlZEZpbGVOYW1lOiBmaWxlLm5hbWV9KTtcbiAgICAgICAgaWYgKGlzUmV1cGxvYWQpIHtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBuYXZpZ2F0ZVRvKFwibWFwQXJjaGV0eXBlc1wiLCBbcmVzcG9uc2VEYXRhLnZyZV0pO1xuICAgICAgICB9XG4gICAgICAgIGlmIChyZXNwb25zZURhdGEuY29sbGVjdGlvbnMgJiYgcmVzcG9uc2VEYXRhLmNvbGxlY3Rpb25zLmxlbmd0aCkge1xuICAgICAgICAgIGRpc3BhdGNoKHNlbGVjdENvbGxlY3Rpb24ocmVzcG9uc2VEYXRhLmNvbGxlY3Rpb25zWzBdLm5hbWUpKTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfTtcbiAgICByZXEuc2VuZChmb3JtRGF0YSk7XG4gIH0pO1xufVxuXG5leHBvcnQgeyBvblVwbG9hZEZpbGVTZWxlY3QgfTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgQWRkUHJvcGVydHkgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIG5ld05hbWU6IFwiXCIsXG4gICAgICBuZXdUeXBlOiBudWxsXG4gICAgfTtcbiAgfVxuXG5cbiAgb25FbnRlcihuZXdOYW1lLCBuZXdUeXBlKSB7XG4gICAgaWYgKG5ld1R5cGUgIT09IG51bGwpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IG51bGwsIG5ld1R5cGU6IG51bGx9KTtcbiAgICAgIHRoaXMucHJvcHMub25BZGRDdXN0b21Qcm9wZXJ0eShuZXdOYW1lLCBuZXdUeXBlKTtcbiAgICB9XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBuZXdOYW1lLCBuZXdUeXBlIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IHsgb25BZGRDdXN0b21Qcm9wZXJ0eSB9ID0gdGhpcy5wcm9wcztcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cInJvdyBzbWFsbC1tYXJnaW5cIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMiBwYWQtNi0xMlwiPlxuICAgICAgICAgIDxzdHJvbmc+QWRkIGEgbmV3IHByb3BlcnR5PC9zdHJvbmc+XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02XCIgPlxuICAgICAgICAgIDxzcGFuPlxuICAgICAgICAgICAgPFNlbGVjdEZpZWxkXG4gICAgICAgICAgICAgIHZhbHVlPXtuZXdUeXBlfVxuICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbHVlKSA9PiB0aGlzLnNldFN0YXRlKHtuZXdUeXBlOiB2YWx1ZSwgbmV3TmFtZTogbmV3TmFtZX0pfVxuICAgICAgICAgICAgICBvbkNsZWFyPXsoKSA9PiB0aGlzLnNldFN0YXRlKHtuZXdUeXBlOiBudWxsfSl9PlxuICAgICAgICAgICAgICA8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIj5DaG9vc2UgYSB0eXBlLi4uPC9zcGFuPlxuICAgICAgICAgICAgICA8c3BhbiB2YWx1ZT1cInRleHRcIj5UZXh0PC9zcGFuPlxuICAgICAgICAgICAgICA8c3BhbiB2YWx1ZT1cImRhdGFibGVcIj5EYXRhYmxlPC9zcGFuPlxuICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICA8L3NwYW4+XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0yXCI+XG4gICAgICAgICAgPGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG4gICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdOYW1lOiBldi50YXJnZXQudmFsdWV9KX1cbiAgICAgICAgICAgICAgICAgIG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyB0aGlzLm9uRW50ZXIobmV3TmFtZSwgbmV3VHlwZSkgOiBmYWxzZX1cbiAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyPVwiUHJvcGVydHkgbmFtZVwiXG4gICAgICAgICAgICAgICAgICB2YWx1ZT17bmV3TmFtZX0gLz5cbiAgICAgICAgPC9kaXY+XG5cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0yXCI+XG5cbiAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cInB1bGwtcmlnaHQgYnRuIGJ0bi1kZWZhdWx0XCIgZGlzYWJsZWQ9eyEobmV3TmFtZSAmJiBuZXdUeXBlKX1cbiAgICAgICAgICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5zZXRTdGF0ZSh7bmV3TmFtZTogbnVsbCwgbmV3VHlwZTogbnVsbH0pO1xuICAgICAgICAgICAgICAgICAgICBvbkFkZEN1c3RvbVByb3BlcnR5KG5ld05hbWUsIG5ld1R5cGUpO1xuICAgICAgICAgICAgICAgICAgfX0+XG4gICAgICAgICAgICBBZGQgcHJvcGVydHlcbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApXG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgQWRkUHJvcGVydHk7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgQWRkUmVsYXRpb24gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIG5ld05hbWU6IFwiXCIsXG4gICAgfTtcbiAgfVxuXG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgbmV3TmFtZSwgbmV3VHlwZSB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCB7IG9uQWRkQ3VzdG9tUHJvcGVydHksIGFyY2hldHlwZUZpZWxkcywgYXZhaWxhYmxlQXJjaGV0eXBlcyB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHJlbGF0aW9uVHlwZU9wdGlvbnMgPSBhcmNoZXR5cGVGaWVsZHNcbiAgICAgIC5maWx0ZXIoKHByb3ApID0+IHByb3AudHlwZSA9PT0gXCJyZWxhdGlvblwiKVxuICAgICAgLmZpbHRlcigocHJvcCkgPT4gYXZhaWxhYmxlQXJjaGV0eXBlcy5pbmRleE9mKHByb3AucmVsYXRpb24udGFyZ2V0Q29sbGVjdGlvbikgPiAtMSlcbiAgICAgIC5tYXAoKHByb3ApID0+IDxzcGFuIGtleT17cHJvcC5uYW1lfSB2YWx1ZT17cHJvcC5uYW1lfT57cHJvcC5uYW1lfTwvc3Bhbj4pO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwicm93IHNtYWxsLW1hcmdpblwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0yIHBhZC02LTEyXCI+XG4gICAgICAgICAgPHN0cm9uZz5BZGQgYSByZWxhdGlvbjwvc3Ryb25nPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tOFwiID5cbiAgICAgICAgICAgIDxTZWxlY3RGaWVsZFxuICAgICAgICAgICAgICB2YWx1ZT17bmV3TmFtZX1cbiAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWx1ZSkgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3TmFtZTogdmFsdWV9KX1cbiAgICAgICAgICAgICAgb25DbGVhcj17KCkgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3TmFtZTogXCJcIn0pfT5cbiAgICAgICAgICAgICAgPHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+Q2hvb3NlIGEgcmVsYXRpb24gdHlwZS4uLjwvc3Bhbj5cbiAgICAgICAgICAgICAge3JlbGF0aW9uVHlwZU9wdGlvbnN9XG4gICAgICAgICAgICA8L1NlbGVjdEZpZWxkPlxuICAgICAgICA8L2Rpdj5cblxuXG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTJcIj5cblxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwicHVsbC1yaWdodCBidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17IW5ld05hbWV9XG4gICAgICAgICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IG51bGx9KTtcbiAgICAgICAgICAgICAgICAgICAgb25BZGRDdXN0b21Qcm9wZXJ0eShuZXdOYW1lLCBcInJlbGF0aW9uXCIpO1xuICAgICAgICAgICAgICAgICAgfX0+XG4gICAgICAgICAgICBBZGQgcmVsYXRpb25cbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApXG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgQWRkUmVsYXRpb247XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUHJvcGVydHlGb3JtIGZyb20gXCIuL3Byb3BlcnR5LWZvcm1cIjtcbmltcG9ydCBBZGRQcm9wZXJ0eSBmcm9tIFwiLi9hZGQtcHJvcGVydHlcIjtcbmltcG9ydCBBZGRSZWxhdGlvbiBmcm9tIFwiLi9hZGQtcmVsYXRpb25cIjtcblxuY2xhc3MgQ29sbGVjdGlvbkZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uQWRkUHJlZGljYXRlT2JqZWN0TWFwLCBvblJlbW92ZVByZWRpY2F0ZU9iamVjdE1hcCxcbiAgICAgIG9uQWRkQ3VzdG9tUHJvcGVydHksIG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHkgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCB7XG4gICAgICBhcmNoZXR5cGVGaWVsZHMsXG4gICAgICBhdmFpbGFibGVBcmNoZXR5cGVzLFxuICAgICAgY29sdW1ucyxcbiAgICAgIGlnbm9yZWRDb2x1bW5zLFxuICAgICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGUsXG4gICAgICB0YXJnZXRhYmxlVnJlc1xuICAgIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgeyBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncywgY3VzdG9tUHJvcGVydGllcyB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IGFyY2hlVHlwZVByb3BGaWVsZHMgPSBhcmNoZXR5cGVGaWVsZHMuZmlsdGVyKChhZikgPT4gYWYudHlwZSAhPT0gXCJyZWxhdGlvblwiKTtcblxuICAgIGNvbnN0IHByb3BlcnR5Rm9ybXMgPSBhcmNoZVR5cGVQcm9wRmllbGRzXG4gICAgICAubWFwKChhZiwgaSkgPT4gKFxuICAgICAgICA8UHJvcGVydHlGb3JtIGtleT17aX0gbmFtZT17YWYubmFtZX0gdHlwZT17YWYudHlwZX0gY3VzdG9tPXtmYWxzZX1cbiAgICAgICAgICAgICAgICAgICAgICBjb2x1bW5zPXtjb2x1bW5zfSBpZ25vcmVkQ29sdW1ucz17aWdub3JlZENvbHVtbnN9XG4gICAgICAgICAgICAgICAgICAgICAgcHJlZGljYXRlT2JqZWN0TWFwPXtwcmVkaWNhdGVPYmplY3RNYXBwaW5ncy5maW5kKChwb20pID0+IHBvbS5wcmVkaWNhdGUgPT09IGFmLm5hbWUpfVxuICAgICAgICAgICAgICAgICAgICAgIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzPXtwcmVkaWNhdGVPYmplY3RNYXBwaW5nc31cbiAgICAgICAgICAgICAgICAgICAgICBvbkFkZFByZWRpY2F0ZU9iamVjdE1hcD17b25BZGRQcmVkaWNhdGVPYmplY3RNYXB9XG4gICAgICAgICAgICAgICAgICAgICAgb25SZW1vdmVQcmVkaWNhdGVPYmplY3RNYXA9e29uUmVtb3ZlUHJlZGljYXRlT2JqZWN0TWFwfSAvPlxuICAgICAgKSk7XG5cbiAgICBjb25zdCBjdXN0b21Qcm9wZXJ0eUZvcm1zID0gY3VzdG9tUHJvcGVydGllc1xuICAgICAgLm1hcCgoY3VzdG9tUHJvcCwgaSkgPT4gKFxuICAgICAgICA8UHJvcGVydHlGb3JtIGtleT17aX0gbmFtZT17Y3VzdG9tUHJvcC5wcm9wZXJ0eU5hbWV9IHR5cGU9e2N1c3RvbVByb3AucHJvcGVydHlUeXBlfSBjdXN0b209e3RydWV9IGN1c3RvbUluZGV4PXtpfVxuICAgICAgICAgICAgICAgICAgICAgIGNvbHVtbnM9e2NvbHVtbnN9IGlnbm9yZWRDb2x1bW5zPXtpZ25vcmVkQ29sdW1uc31cbiAgICAgICAgICAgICAgICAgICAgICBwcmVkaWNhdGVPYmplY3RNYXA9e3ByZWRpY2F0ZU9iamVjdE1hcHBpbmdzLmZpbmQoKHBvbSkgPT4gcG9tLnByZWRpY2F0ZSA9PT0gY3VzdG9tUHJvcC5wcm9wZXJ0eU5hbWUpfVxuICAgICAgICAgICAgICAgICAgICAgIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzPXtwcmVkaWNhdGVPYmplY3RNYXBwaW5nc31cbiAgICAgICAgICAgICAgICAgICAgICBvbkFkZFByZWRpY2F0ZU9iamVjdE1hcD17b25BZGRQcmVkaWNhdGVPYmplY3RNYXB9XG4gICAgICAgICAgICAgICAgICAgICAgb25SZW1vdmVQcmVkaWNhdGVPYmplY3RNYXA9e29uUmVtb3ZlUHJlZGljYXRlT2JqZWN0TWFwfVxuICAgICAgICAgICAgICAgICAgICAgIG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHk9e29uUmVtb3ZlQ3VzdG9tUHJvcGVydHl9XG4gICAgICAgICAgICAgICAgICAgICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGU9e2F2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlfVxuICAgICAgICAgICAgICAgICAgICAgIHJlbGF0aW9uVHlwZUluZm89e2FyY2hldHlwZUZpZWxkcy5maW5kKChhZikgPT4gYWYubmFtZSA9PT0gY3VzdG9tUHJvcC5wcm9wZXJ0eU5hbWUpfVxuICAgICAgICAgICAgICAgICAgICAgIHRhcmdldGFibGVWcmVzPXt0YXJnZXRhYmxlVnJlc31cbiAgICAgICAgLz5cbiAgICAgICkpO1xuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAge3Byb3BlcnR5Rm9ybXN9XG4gICAgICAgIHtjdXN0b21Qcm9wZXJ0eUZvcm1zfVxuICAgICAgICA8QWRkUHJvcGVydHkgb25BZGRDdXN0b21Qcm9wZXJ0eT17b25BZGRDdXN0b21Qcm9wZXJ0eX0gLz5cbiAgICAgICAgPEFkZFJlbGF0aW9uXG4gICAgICAgICAgYXJjaGV0eXBlRmllbGRzPXthcmNoZXR5cGVGaWVsZHN9XG4gICAgICAgICAgYXZhaWxhYmxlQXJjaGV0eXBlcz17YXZhaWxhYmxlQXJjaGV0eXBlc31cbiAgICAgICAgICBvbkFkZEN1c3RvbVByb3BlcnR5PXtvbkFkZEN1c3RvbVByb3BlcnR5fSAvPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBDb2x1bW5TZWxlY3QgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgY29sdW1ucywgc2VsZWN0ZWRDb2x1bW4sIG9uQ29sdW1uU2VsZWN0LCBvbkNsZWFyQ29sdW1uLCBwbGFjZWhvbGRlciwgaWdub3JlZENvbHVtbnMsIHZhbHVlUHJlZml4IH0gPSB0aGlzLnByb3BzO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxTZWxlY3RGaWVsZCB2YWx1ZT17c2VsZWN0ZWRDb2x1bW59IHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIn19XG4gICAgICAgICAgICAgICAgICAgdmFsdWVQcmVmaXg9e3ZhbHVlUHJlZml4fVxuICAgICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsoY29sdW1uKSA9PiBvbkNvbHVtblNlbGVjdChjb2x1bW4pfVxuICAgICAgICAgICAgICAgICAgIG9uQ2xlYXI9eygpID0+IG9uQ2xlYXJDb2x1bW4oc2VsZWN0ZWRDb2x1bW4pfT5cblxuICAgICAgICA8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIiBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+XG4gICAgICAgICAgPGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBhbHQ9XCJcIi8+IHtwbGFjZWhvbGRlciB8fCBcIlNlbGVjdCBhbiBleGNlbCBjb2x1bW5cIn1cbiAgICAgICAgPC9zcGFuPlxuXG4gICAgICAgIHtjb2x1bW5zLmZpbHRlcigoY29sdW1uKSA9PiBpZ25vcmVkQ29sdW1ucy5pbmRleE9mKGNvbHVtbikgPCAwKS5tYXAoKGNvbHVtbikgPT4gKFxuICAgICAgICAgIDxzcGFuIGtleT17Y29sdW1ufSB2YWx1ZT17Y29sdW1ufSBjbGFzc05hbWU9XCJmcm9tLWV4Y2VsXCI+XG4gICAgICAgICAgICA8aW1nIHNyYz1cImltYWdlcy9pY29uLWV4Y2VsLnN2Z1wiIGFsdD1cIlwiLz57XCIgXCJ9XG4gICAgICAgICAgICB7dmFsdWVQcmVmaXggJiYgY29sdW1uID09PSBzZWxlY3RlZENvbHVtbiA/IHZhbHVlUHJlZml4IDogXCJcIn1cbiAgICAgICAgICAgIHtjb2x1bW59XG4gICAgICAgICAgPC9zcGFuPlxuICAgICAgICApKX1cbiAgICAgIDwvU2VsZWN0RmllbGQ+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBDb2x1bW5TZWxlY3Q7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQ29sdW1uU2VsZWN0IGZyb20gXCIuL2NvbHVtbi1zZWxlY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi4vLi4vdXRpbC9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IHtnZXRDb2x1bW5WYWx1ZX0gZnJvbSBcIi4uLy4uL2FjY2Vzc29ycy9wcm9wZXJ0eS1tYXBwaW5nc1wiO1xuXG5cbmNvbnN0IGdldE9iamVjdEZvclByZWRpY2F0ZSA9IChwcmVkaWNhdGVPYmplY3RNYXBwaW5ncywgcHJlZGljYXRlKSA9PlxuICBwcmVkaWNhdGVPYmplY3RNYXBwaW5nc1xuICAgIC5maWx0ZXIoKHBvbSkgPT4gcG9tLnByZWRpY2F0ZSA9PT0gcHJlZGljYXRlKVxuICAgIC5tYXAoKHBvbSkgPT4gZ2V0Q29sdW1uVmFsdWUocG9tKSlbMF07XG5cbmNsYXNzIE5hbWVzRm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBjb2x1bW5zLCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncywgb25Db2x1bW5TZWxlY3QsIG9uQ2xlYXJDb2x1bW4sIGlnbm9yZWRDb2x1bW5zIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgZm9ybVJvd3MgPSBbXCJmb3JlbmFtZVwiLCBcInN1cm5hbWVcIiwgXCJuYW1lTGlua1wiLCBcImdlbk5hbWVcIiwgXCJyb2xlTmFtZVwiXVxuICAgICAgLm1hcCgocHJlZGljYXRlKSA9PiAoXG4gICAgICAgIDxkaXYga2V5PXtwcmVkaWNhdGV9IGNsYXNzTmFtZT1cInJvd1wiPlxuICAgICAgICAgIDxzcGFuIHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgcGFkZGluZ0xlZnQ6IFwiMTJweFwiLCB3aWR0aDogXCI5MnB4XCJ9fT5cbiAgICAgICAgICAgIHtjYW1lbDJsYWJlbChwcmVkaWNhdGUpfVxuICAgICAgICAgIDwvc3Bhbj5cbiAgICAgICAgICA8Q29sdW1uU2VsZWN0IGNvbHVtbnM9e2NvbHVtbnN9IGlnbm9yZWRDb2x1bW5zPXtpZ25vcmVkQ29sdW1uc31cbiAgICAgICAgICAgICAgICAgICAgICAgIHNlbGVjdGVkQ29sdW1uPXtnZXRPYmplY3RGb3JQcmVkaWNhdGUocHJlZGljYXRlT2JqZWN0TWFwcGluZ3MsIHByZWRpY2F0ZSl9XG4gICAgICAgICAgICAgICAgICAgICAgICBvbkNvbHVtblNlbGVjdD17KHZhbHVlKSA9PiBvbkNvbHVtblNlbGVjdCh2YWx1ZSwgcHJlZGljYXRlKX1cbiAgICAgICAgICAgICAgICAgICAgICAgIG9uQ2xlYXJDb2x1bW49eyh2YWx1ZSkgPT4gb25DbGVhckNvbHVtbih2YWx1ZSwgcHJlZGljYXRlKX1cbiAgICAgICAgICAvPlxuICAgICAgICA8L2Rpdj4pXG4gICAgICApO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXY+XG4gICAgICAgIHtmb3JtUm93c31cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgTmFtZXNGb3JtO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5pbXBvcnQgQ29sdW1uU2VsZWN0IGZyb20gXCIuL2NvbHVtbi1zZWxlY3RcIjtcbmltcG9ydCBOYW1lc0Zvcm0gZnJvbSBcIi4vbmFtZXMtZm9ybVwiO1xuaW1wb3J0IFJlbGF0aW9uRm9ybSBmcm9tIFwiLi9yZWxhdGlvbi1mb3JtXCI7XG5pbXBvcnQgeyBwcm9wZXJ0eU1hcHBpbmdJc0NvbXBsZXRlIH0gZnJvbSBcIi4uLy4uL2FjY2Vzc29ycy9wcm9wZXJ0eS1tYXBwaW5nc1wiXG5pbXBvcnQgeyBnZXRDb2x1bW5WYWx1ZSB9IGZyb20gXCIuLi8uLi9hY2Nlc3NvcnMvcHJvcGVydHktbWFwcGluZ3NcIjtcblxuY29uc3QgdHlwZU1hcCA9IHtcbiAgdGV4dDogKHByb3BzKSA9PiA8Q29sdW1uU2VsZWN0IHsuLi5wcm9wc30gLz4sXG4gIGRhdGFibGU6IChwcm9wcykgPT4gPENvbHVtblNlbGVjdCB7Li4ucHJvcHN9IC8+LFxuICBzZWxlY3Q6IChwcm9wcykgPT4gPENvbHVtblNlbGVjdCB7Li4ucHJvcHN9IC8+LFxuICBuYW1lczogKHByb3BzKSA9PiA8TmFtZXNGb3JtIHsuLi5wcm9wc30gLz4sXG4gIHJlbGF0aW9uOiAocHJvcHMpID0+IDxSZWxhdGlvbkZvcm0gey4uLnByb3BzfSAvPixcbiAgXCJyZWxhdGlvbi10by1leGlzdGluZ1wiOiAocHJvcHMpID0+IDxSZWxhdGlvblRvRXhpc3RpbmdGb3JtIHsuLi5wcm9wc30gLz4sXG4gIG11bHRpc2VsZWN0OiAocHJvcHMpID0+IDxDb2x1bW5TZWxlY3Qgey4uLnByb3BzfSAvPixcbn07XG5cbmNvbnN0IGlzQ29tcGxldGVGb3JOYW1lcyA9ICh0eXBlLCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncykgPT5cbiAgdHlwZSA9PT0gXCJuYW1lc1wiICYmIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzXG4gICAgLmZpbHRlcigocG9tKSA9PiBbXCJmb3JlbmFtZVwiLCBcInN1cm5hbWVcIiwgXCJuYW1lTGlua1wiLCBcImdlbk5hbWVcIiwgXCJyb2xlTmFtZVwiXS5pbmRleE9mKHBvbS5wcmVkaWNhdGUpID4gLTEpXG4gICAgLmZpbHRlcigocG9tKSA9PiBwcm9wZXJ0eU1hcHBpbmdJc0NvbXBsZXRlKHBvbSkpXG4gICAgLmxlbmd0aCA+IDA7XG5cbmNsYXNzIFByb3BlcnR5Rm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgcmVuZGVyKCkge1xuXG4gICAgY29uc3QgeyBvbkFkZFByZWRpY2F0ZU9iamVjdE1hcCwgb25SZW1vdmVQcmVkaWNhdGVPYmplY3RNYXAsIG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHksXG4gICAgICBhdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZSwgcmVsYXRpb25UeXBlSW5mbywgdGFyZ2V0YWJsZVZyZXMgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCB7IG5hbWU6IHByZWRpY2F0ZU5hbWUsIHR5cGUsIGN1c3RvbSwgY3VzdG9tSW5kZXgsIGNvbHVtbnMsIGlnbm9yZWRDb2x1bW5zLCBwcmVkaWNhdGVPYmplY3RNYXAsIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgZm9ybUNvbXBvbmVudCA9IHR5cGVNYXBbdHlwZV1cbiAgICAgID8gdHlwZU1hcFt0eXBlXSh7XG4gICAgICAgIGNvbHVtbnM6IGNvbHVtbnMsXG4gICAgICAgIGlnbm9yZWRDb2x1bW5zOiBpZ25vcmVkQ29sdW1ucyxcbiAgICAgICAgc2VsZWN0ZWRDb2x1bW46IGdldENvbHVtblZhbHVlKHByZWRpY2F0ZU9iamVjdE1hcCksXG4gICAgICAgIHByZWRpY2F0ZU9iamVjdE1hcDogcHJlZGljYXRlT2JqZWN0TWFwLFxuICAgICAgICBwcmVkaWNhdGVPYmplY3RNYXBwaW5nczogcHJlZGljYXRlT2JqZWN0TWFwcGluZ3MsXG4gICAgICAgIGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlOiBhdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZSxcbiAgICAgICAgcmVsYXRpb25UeXBlSW5mbzogcmVsYXRpb25UeXBlSW5mbyxcbiAgICAgICAgdGFyZ2V0YWJsZVZyZXM6IHRhcmdldGFibGVWcmVzLFxuICAgICAgICBvbkNvbHVtblNlbGVjdDogKHZhbHVlLCBwcmVkaWNhdGUpID0+IG9uQWRkUHJlZGljYXRlT2JqZWN0TWFwKHByZWRpY2F0ZSB8fCBwcmVkaWNhdGVOYW1lLCB2YWx1ZSwgdHlwZSksXG4gICAgICAgIG9uQ2xlYXJDb2x1bW46ICh2YWx1ZSwgcHJlZGljYXRlKSA9PiBvblJlbW92ZVByZWRpY2F0ZU9iamVjdE1hcChwcmVkaWNhdGUgfHwgcHJlZGljYXRlTmFtZSwgdmFsdWUpXG4gICAgICB9KVxuICAgICAgOiA8c3Bhbj50eXBlIG5vdCB5ZXQgc3VwcG9ydGVkOiA8c3BhbiBzdHlsZT17e2NvbG9yOiBcInJlZFwifX0+e3R5cGV9PC9zcGFuPjwvc3Bhbj47XG5cbiAgICBjb25zdCB1bkNvbmZpcm1CdXR0b24gPSBwcm9wZXJ0eU1hcHBpbmdJc0NvbXBsZXRlKHByZWRpY2F0ZU9iamVjdE1hcCkgfHwgaXNDb21wbGV0ZUZvck5hbWVzKHR5cGUsIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzKVxuICAgICAgPyAoPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rXCIgb25DbGljaz17KCkgPT4gb25SZW1vdmVQcmVkaWNhdGVPYmplY3RNYXAocHJlZGljYXRlTmFtZSwgZ2V0Q29sdW1uVmFsdWUocHJlZGljYXRlT2JqZWN0TWFwKSl9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImhpLXN1Y2Nlc3MgZ2x5cGhpY29uIGdseXBoaWNvbi1va1wiIC8+XG4gICAgICAgIDwvYnV0dG9uPikgOiBudWxsO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwicm93IHNtYWxsLW1hcmdpblwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0yIHBhZC02LTEyXCI+XG4gICAgICAgICAgPHN0cm9uZz57cHJlZGljYXRlTmFtZX08L3N0cm9uZz5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0XCIgc3R5bGU9e3tmb250U2l6ZTogXCIwLjdlbVwifX0+KHt0eXBlfSk8L3NwYW4+XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS04XCI+XG4gICAgICAgICAge2Zvcm1Db21wb25lbnR9XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xXCI+XG4gICAgICAgICAgeyBjdXN0b21cbiAgICAgICAgICAgID8gKDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBwdWxsLXJpZ2h0XCIgdHlwZT1cImJ1dHRvblwiIG9uQ2xpY2s9eygpID0+IG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHkoY3VzdG9tSW5kZXgpfT5cbiAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIvPlxuICAgICAgICAgIDwvYnV0dG9uPilcbiAgICAgICAgICAgIDogbnVsbCB9XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGhpLXN1Y2Nlc3NcIj5cbiAgICAgICAgICB7dW5Db25maXJtQnV0dG9ufVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgUHJvcGVydHlGb3JtOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBDb2x1bW5TZWxlY3QgZnJvbSBcIi4vY29sdW1uLXNlbGVjdFwiO1xuXG5jb25zdCBnZXRTZWxlY3RlZFRhcmdldENvbHVtbiA9IChvYmplY3RNYXApID0+XG4gIG9iamVjdE1hcC5qb2luQ29uZGl0aW9uICYmIG9iamVjdE1hcC5qb2luQ29uZGl0aW9uLnBhcmVudCAmJiBvYmplY3RNYXAucGFyZW50VHJpcGxlc01hcFxuICAgID8gYCR7b2JqZWN0TWFwLnBhcmVudFRyaXBsZXNNYXB9ISR7b2JqZWN0TWFwLmpvaW5Db25kaXRpb24ucGFyZW50fWBcbiAgICA6IG51bGw7XG5cbmNsYXNzIFJlbGF0aW9uRm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25Db2x1bW5TZWxlY3QsIHByZWRpY2F0ZU9iamVjdE1hcDogb3B0aW9uYWxQcmVkaWNhdGVPYmplY3RNYXAsIGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlLCByZWxhdGlvblR5cGVJbmZvIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qgb2JqZWN0TWFwID0gKG9wdGlvbmFsUHJlZGljYXRlT2JqZWN0TWFwIHx8IHt9KS5vYmplY3RNYXAgfHwge307XG5cbiAgICBjb25zdCBzb3VyY2VDb2x1bW5Qcm9wcyA9IHtcbiAgICAgIC4uLnRoaXMucHJvcHMsXG4gICAgICB2YWx1ZVByZWZpeDogXCIoc291cmNlKSBcIixcbiAgICAgIHBsYWNlaG9sZGVyOiBcIlNlbGVjdCBhIHNvdXJjZSBjb2x1bW4uLi5cIixcbiAgICAgIG9uQ29sdW1uU2VsZWN0OiAodmFsdWUpID0+IG9uQ29sdW1uU2VsZWN0KHtcbiAgICAgICAgLi4uKG9iamVjdE1hcCB8fCB7fSksXG4gICAgICAgIGpvaW5Db25kaXRpb246IHtcbiAgICAgICAgICAuLi4oKG9iamVjdE1hcCB8fCB7fSkuam9pbkNvbmRpdGlvbiB8fCB7fSksXG4gICAgICAgICAgY2hpbGQ6IHZhbHVlXG4gICAgICAgIH1cbiAgICAgIH0pXG4gICAgfTtcblxuICAgIGNvbnN0IHRhcmdldENvbGxlY3Rpb25Db2x1bW5zID0gYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGVbcmVsYXRpb25UeXBlSW5mby5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uXVxuICAgICAgLm1hcCgodGFyZ2V0Q29sbGVjdGlvbkNvbHMpID0+IHRhcmdldENvbGxlY3Rpb25Db2xzLmNvbHVtbnMubWFwKChjb2x1bW4pID0+IGAke3RhcmdldENvbGxlY3Rpb25Db2xzLmNvbGxlY3Rpb25OYW1lfSEke2NvbHVtbn1gKSlcbiAgICAgIC5yZWR1Y2UoKGEsYikgPT4gYS5jb25jYXQoYikpO1xuXG4gICAgY29uc3QgdGFyZ2V0Q29sdW1uUHJvcHMgPSB7XG4gICAgICB2YWx1ZVByZWZpeDogXCIodGFyZ2V0KSBcIixcbiAgICAgIGNvbHVtbnM6IHRhcmdldENvbGxlY3Rpb25Db2x1bW5zLFxuICAgICAgc2VsZWN0ZWRDb2x1bW46IGdldFNlbGVjdGVkVGFyZ2V0Q29sdW1uKG9iamVjdE1hcCksXG4gICAgICBpZ25vcmVkQ29sdW1uczogW10sXG4gICAgICBwbGFjZWhvbGRlcjogXCJTZWxlY3QgYSB0YXJnZXQgY29sdW1uLi4uXCIsXG4gICAgICBvbkNvbHVtblNlbGVjdDogKHZhbHVlKSA9PiBvbkNvbHVtblNlbGVjdCh7XG4gICAgICAgIC4uLihvYmplY3RNYXAgfHwge30pLFxuICAgICAgICBqb2luQ29uZGl0aW9uOiB7XG4gICAgICAgICAgLi4uKChvYmplY3RNYXAgfHwge30pLmpvaW5Db25kaXRpb24gfHwge30pLFxuICAgICAgICAgIHBhcmVudDogdmFsdWUuc3BsaXQoXCIhXCIpWzFdXG4gICAgICAgIH0sXG4gICAgICAgIHBhcmVudFRyaXBsZXNNYXA6IHZhbHVlLnNwbGl0KFwiIVwiKVswXVxuICAgICAgfSlcbiAgICB9O1xuXG5cblxuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXY+XG4gICAgICAgIDxDb2x1bW5TZWxlY3Qgey4uLnNvdXJjZUNvbHVtblByb3BzfSAvPlxuICAgICAgICA8Q29sdW1uU2VsZWN0IHsuLi50YXJnZXRDb2x1bW5Qcm9wc30gLz5cblxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IFJlbGF0aW9uRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgVXBsb2FkQnV0dG9uIGZyb20gXCIuL3VwbG9hZC1idXR0b25cIjtcbmltcG9ydCBEYXRhc2V0Q2FyZHMgZnJvbSBcIi4vZGF0YXNldC1jYXJkc1wiXG5pbXBvcnQgRmlyc3RVcGxvYWQgZnJvbSBcIi4vZmlyc3RVcGxvYWRcIjtcblxuZnVuY3Rpb24gQ29sbGVjdGlvbk92ZXJ2aWV3KHByb3BzKSB7XG4gIGNvbnN0IHsgb25VcGxvYWRGaWxlU2VsZWN0LCB1c2VySWQsIHVwbG9hZFN0YXR1cywgdnJlcywgc2VhcmNoR3VpVXJsLCBvbkNvbnRpbnVlTWFwcGluZyB9ID0gcHJvcHM7XG5cbiAgY29uc3QgdXBsb2FkQnV0dG9uID0gKFxuICAgIDxVcGxvYWRCdXR0b25cbiAgICAgIGNsYXNzTmFtZXM9e1tcImJ0blwiLCBcImJ0bi1sZ1wiLCBcImJ0bi1wcmltYXJ5XCIsIFwicHVsbC1yaWdodFwiXX1cbiAgICAgIGdseXBoaWNvbj1cImdseXBoaWNvbiBnbHlwaGljb24tY2xvdWQtdXBsb2FkXCJcbiAgICAgIHVwbG9hZFN0YXR1cz17dXBsb2FkU3RhdHVzfVxuICAgICAgbGFiZWw9XCJVcGxvYWQgbmV3IGRhdGFzZXRcIlxuICAgICAgb25VcGxvYWRGaWxlU2VsZWN0PXtvblVwbG9hZEZpbGVTZWxlY3R9IC8+XG4gICk7XG5cbiAgcmV0dXJuIHZyZXMgJiYgT2JqZWN0LmtleXModnJlcykubGVuZ3RoID4gMFxuICAgID8gKFxuICAgICAgPGRpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICA8RGF0YXNldENhcmRzIHVzZXJJZD17dXNlcklkfSBjYXB0aW9uPVwiTXkgZGF0YXNldHNcIiB2cmVzPXt2cmVzfSBtaW5lPXt0cnVlfSBzZWFyY2hHdWlVcmw9e3NlYXJjaEd1aVVybH1cbiAgICAgICAgICAgIG9uQ29udGludWVNYXBwaW5nPXtvbkNvbnRpbnVlTWFwcGluZ30+XG4gICAgICAgICAgICB7dXBsb2FkQnV0dG9ufVxuICAgICAgICAgIDwvRGF0YXNldENhcmRzPlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICApIDogKFxuICAgIDxGaXJzdFVwbG9hZCB7Li4ucHJvcHN9IC8+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25PdmVydmlldzsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgSGVhZGVyQ2VsbCBmcm9tIFwiLi90YWJsZS9oZWFkZXItY2VsbFwiO1xuaW1wb3J0IERhdGFSb3cgZnJvbSBcIi4vdGFibGUvZGF0YS1yb3dcIjtcblxuY2xhc3MgQ29sbGVjdGlvblRhYmxlIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25JZ25vcmVDb2x1bW5Ub2dnbGUgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyByb3dzLCBoZWFkZXJzLCBuZXh0VXJsIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwidGFibGUtcmVzcG9uc2l2ZVwiPlxuICAgICAgICA8dGFibGUgY2xhc3NOYW1lPVwidGFibGUgdGFibGUtYm9yZGVyZWQgdGFibGUtb2J0cnVzaXZlXCI+XG4gICAgICAgICAgPHRoZWFkPlxuICAgICAgICAgICAgPHRyPlxuICAgICAgICAgICAgICB7aGVhZGVycy5tYXAoKGhlYWRlcikgPT4gKFxuICAgICAgICAgICAgICAgIDxIZWFkZXJDZWxsIGtleT17aGVhZGVyLm5hbWV9IGhlYWRlcj17aGVhZGVyLm5hbWV9IG9uSWdub3JlQ29sdW1uVG9nZ2xlPXtvbklnbm9yZUNvbHVtblRvZ2dsZX1cbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBpc0lnbm9yZWQ9e2hlYWRlci5pc0lnbm9yZWR9IGlzQ29uZmlybWVkPXtoZWFkZXIuaXNDb25maXJtZWR9IC8+XG4gICAgICAgICAgICAgICkpfVxuICAgICAgICAgICAgPC90cj5cbiAgICAgICAgICA8L3RoZWFkPlxuICAgICAgICAgIDx0Ym9keT5cbiAgICAgICAgICAgIHtyb3dzLm1hcCgocm93LCBpKSA9PiA8RGF0YVJvdyBrZXk9e2l9IHJvdz17cm93fSAvPil9XG4gICAgICAgICAgPC90Ym9keT5cbiAgICAgICAgPC90YWJsZT5cbiAgICAgICAgPGJ1dHRvbiBvbkNsaWNrPXsoKSA9PiB0aGlzLnByb3BzLm9uTG9hZE1vcmVDbGljayAmJiB0aGlzLnByb3BzLm9uTG9hZE1vcmVDbGljayhuZXh0VXJsKX1cbiAgICAgICAgICAgICAgICBkaXNhYmxlZD17IW5leHRVcmx9XG4gICAgICAgICAgICAgICAgY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0IHB1bGwtcmlnaHRcIj5tb3JlLi4uPC9idXR0b24+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25UYWJsZTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgQ29sbGVjdGlvblRhYnMgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IGNvbGxlY3Rpb25UYWJzLCBvblNlbGVjdENvbGxlY3Rpb24gfSA9IHRoaXMucHJvcHM7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2LXRhYnNcIiByb2xlPVwidGFibGlzdFwiPlxuICAgICAgICAgIHtjb2xsZWN0aW9uVGFicy5tYXAoKGNvbGxlY3Rpb25UYWIpID0+IChcbiAgICAgICAgICAgIDxsaSBrZXk9e2NvbGxlY3Rpb25UYWIuY29sbGVjdGlvbk5hbWV9IGNsYXNzTmFtZT17Y3goe2FjdGl2ZTogY29sbGVjdGlvblRhYi5hY3RpdmV9KX0+XG4gICAgICAgICAgICAgIDxhIG9uQ2xpY2s9eygpID0+IGNvbGxlY3Rpb25UYWIuYWN0aXZlID8gZmFsc2UgOiBvblNlbGVjdENvbGxlY3Rpb24oY29sbGVjdGlvblRhYi5jb2xsZWN0aW9uTmFtZSl9XG4gICAgICAgICAgICAgICAgIHN0eWxlPXt7Y3Vyc29yOiBjb2xsZWN0aW9uVGFiLmFjdGl2ZSA/IFwiZGVmYXVsdFwiIDogXCJwb2ludGVyXCJ9fT5cbiAgICAgICAgICAgICAgICB7Y29sbGVjdGlvblRhYi5hcmNoZXR5cGVOYW1lfXtcIiBcIn1cbiAgICAgICAgICAgICAgICB7Y29sbGVjdGlvblRhYi5jb21wbGV0ZSA/IDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tb2tcIiAvPiA6IG51bGx9XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZXhjZWwtdGFiXCI+PGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBjbGFzc05hbWU9XCJleGNlbC1pY29uXCIgYWx0PVwiXCIvPiB7Y29sbGVjdGlvblRhYi5jb2xsZWN0aW9uTmFtZX08L3NwYW4+XG4gICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFiczsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQ29sbGVjdGlvblRhYnMgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJzXCI7XG5pbXBvcnQgTWVzc2FnZSBmcm9tIFwiLi9tZXNzYWdlXCI7XG5pbXBvcnQgQ29sbGVjdGlvblRhYmxlIGZyb20gXCIuL2NvbGxlY3Rpb24tdGFibGVcIlxuaW1wb3J0IENvbGxlY3Rpb25Gb3JtIGZyb20gXCIuL2NvbGxlY3Rpb24tZm9ybS9jb2xsZWN0aW9uLWZvcm1cIjtcbmltcG9ydCBVcGxvYWRCdXR0b24gZnJvbSBcIi4vdXBsb2FkLWJ1dHRvblwiO1xuXG5jbGFzcyBDb25uZWN0RGF0YSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcbiAgICBjb25zdCB7IG9uRmV0Y2hCdWxrVXBsb2FkZWRNZXRhZGF0YSwgcGFyYW1zOiB7IHNlcmlhbGl6ZWRBcmNoZXR5cGVNYXBwaW5ncyB9IH0gPSB0aGlzLnByb3BzO1xuICAgIC8vIFRyaWdnZXJzIGZldGNoIGRhdGEgZnJvbSBzZXJ2ZXIgYmFzZWQgb24gdnJlSWQgZnJvbSByb3V0ZS5cbiAgICBpZiAodGhpcy5wcm9wcy5wYXJhbXMudnJlSWQgIT09IG5leHRQcm9wcy5wYXJhbXMudnJlSWQpIHtcbiAgICAgIG9uRmV0Y2hCdWxrVXBsb2FkZWRNZXRhZGF0YShuZXh0UHJvcHMucGFyYW1zLnZyZUlkLCBKU09OLnBhcnNlKGRlY29kZVVSSUNvbXBvbmVudChzZXJpYWxpemVkQXJjaGV0eXBlTWFwcGluZ3MpKSk7XG4gICAgfVxuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQoKSB7XG4gICAgY29uc3QgeyBvbkZldGNoQnVsa1VwbG9hZGVkTWV0YWRhdGEsIHRhYnMsIHZyZSwgdnJlSWQsIHBhcmFtczogeyBzZXJpYWxpemVkQXJjaGV0eXBlTWFwcGluZ3MgfSAgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKHRhYnMubGVuZ3RoID09PSAwIHx8IHZyZSAhPT0gdnJlSWQpIHtcbiAgICAgIG9uRmV0Y2hCdWxrVXBsb2FkZWRNZXRhZGF0YSh2cmVJZCwgSlNPTi5wYXJzZShkZWNvZGVVUklDb21wb25lbnQoc2VyaWFsaXplZEFyY2hldHlwZU1hcHBpbmdzKSkpO1xuICAgIH1cbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uQ2xvc2VNZXNzYWdlLCBvblNlbGVjdENvbGxlY3Rpb24sIG9uTG9hZE1vcmVDbGljaywgb25JZ25vcmVDb2x1bW5Ub2dnbGUsIG9uUHVibGlzaERhdGEsIG9uVXBsb2FkRmlsZVNlbGVjdCB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHsgb25BZGRQcmVkaWNhdGVPYmplY3RNYXAsIG9uUmVtb3ZlUHJlZGljYXRlT2JqZWN0TWFwLCBvbkFkZEN1c3RvbVByb3BlcnR5LCBvblJlbW92ZUN1c3RvbVByb3BlcnR5IH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qge1xuICAgICAgcGFyYW1zOiB7IHZyZUlkIH0sXG4gICAgICB2cmUsXG4gICAgICB0YWJzLFxuICAgICAgc2hvd0NvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZSxcbiAgICAgIHVwbG9hZGVkRmlsZW5hbWUsXG4gICAgICBwdWJsaXNoRW5hYmxlZCxcbiAgICAgIHB1Ymxpc2hTdGF0dXMsXG4gICAgICBwdWJsaXNoRXJyb3JzLFxuICAgICAgdXBsb2FkU3RhdHVzLFxuICAgICAgYXZhaWxhYmxlQXJjaGV0eXBlcyxcbiAgICAgIGN1c3RvbVByb3BlcnRpZXMsXG4gICAgICBhdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZSxcbiAgICAgIHJtbFByZXZpZXdEYXRhLFxuICAgICAgdGFyZ2V0YWJsZVZyZXNcbiAgICB9ID0gdGhpcy5wcm9wcztcblxuICAgIC8vIHRhYmxlIHZpZXcgcHJvcGVydGllc1xuICAgIGNvbnN0IHsgcm93cywgaGVhZGVycywgbmV4dFVybCwgYWN0aXZlQ29sbGVjdGlvbiB9ID0gdGhpcy5wcm9wcztcblxuICAgIC8vIGZvcm0gdmlldyBwcm9wZXJ0aWVzXG4gICAgY29uc3QgeyBhcmNoZXR5cGVGaWVsZHMsIGNvbHVtbnMsIGlnbm9yZWRDb2x1bW5zLCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncyB9ID0gdGhpcy5wcm9wcztcblxuICAgIGlmICghYXJjaGV0eXBlRmllbGRzIHx8IHRhYnMubGVuZ3RoID09PSAwIHx8IHZyZSAhPT0gdnJlSWQpIHsgcmV0dXJuIG51bGw7IH1cblxuXG4gICAgY29uc3Qgcm1sUHJldmlld0Jsb2NrID0gcm1sUHJldmlld0RhdGEgPyAoXG4gICAgICA8ZGl2IHN0eWxlPXt7cG9zaXRpb246IFwiYWJzb2x1dGVcIiwgekluZGV4OiBcIjEwXCIsIHdpZHRoOiBcIjEwMCVcIiwgdG9wOiBcIjkwcHhcIn19PlxuICAgICAgICA8cHJlIHN0eWxlPXt7d2lkdGg6IFwiODAlXCIsIG1hcmdpbjogXCIwIGF1dG9cIiwgYmFja2dyb3VuZENvbG9yOiBcIiNkZGRcIn19PlxuICAgICAgICAgIHtKU09OLnN0cmluZ2lmeShybWxQcmV2aWV3RGF0YSwgbnVsbCwgMil9XG4gICAgICAgIDwvcHJlPlxuICAgICAgPC9kaXY+XG4gICAgKSA6IG51bGw7XG5cbiAgICBjb25zdCBwdWJsaXNoRmFpbGVkTWVzc2FnZSA9IHB1Ymxpc2hFcnJvcnMgPyAoXG4gICAgICA8TWVzc2FnZSBhbGVydExldmVsPVwiZGFuZ2VyXCIgZGlzbWlzc2libGU9e2ZhbHNlfT5cbiAgICAgICAgPFVwbG9hZEJ1dHRvbiBjbGFzc05hbWVzPXtbXCJidG5cIiwgXCJidG4tZGFuZ2VyXCIsIFwicHVsbC1yaWdodFwiLCBcImJ0bi14c1wiXX0gbGFiZWw9XCJSZS11cGxvYWRcIlxuICAgICAgICAgICAgICAgICAgICAgIG9uVXBsb2FkRmlsZVNlbGVjdD17b25VcGxvYWRGaWxlU2VsZWN0fSB1cGxvYWRTdGF0dXM9e3VwbG9hZFN0YXR1c30gLz5cbiAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgLz57XCIgXCJ9XG4gICAgICAgIFB1Ymxpc2ggZmFpbGVkLiBQbGVhc2UgZml4IHRoZSBtYXBwaW5ncyBvciByZS11cGxvYWQgdGhlIGRhdGEuXG4gICAgICA8L01lc3NhZ2U+XG4gICAgKSA6IG51bGw7XG5cbiAgICBjb25zdCBjb2xsZWN0aW9uc0FyZUNvbm5lY3RlZE1lc3NhZ2UgPSBzaG93Q29sbGVjdGlvbnNBcmVDb25uZWN0ZWRNZXNzYWdlICYmIHVwbG9hZGVkRmlsZW5hbWUgP1xuICAgICAgPE1lc3NhZ2UgYWxlcnRMZXZlbD1cImluZm9cIiBkaXNtaXNzaWJsZT17dHJ1ZX0gb25DbG9zZU1lc3NhZ2U9eygpID0+IG9uQ2xvc2VNZXNzYWdlKFwic2hvd0NvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZVwiKX0+XG4gICAgICAgIHt0YWJzLm1hcCgodGFiKSA9PiA8ZW0ga2V5PXt0YWIuY29sbGVjdGlvbk5hbWV9Pnt0YWIuY29sbGVjdGlvbk5hbWV9PC9lbT4pXG4gICAgICAgICAgLnJlZHVjZSgoYWNjdSwgZWxlbSkgPT4gYWNjdSA9PT0gbnVsbCA/IFtlbGVtXSA6IFsuLi5hY2N1LCAnIGFuZCAnLCBlbGVtXSwgbnVsbClcbiAgICAgICAgfSBmcm9tIDxlbT57dXBsb2FkZWRGaWxlbmFtZX08L2VtPiB7dGFicy5sZW5ndGggPT09IDEgPyBcImlzXCIgOiBcImFyZVwiIH0gY29ubmVjdGVkIHRvIHRoZSBUaW1idWN0b28gQXJjaGV0eXBlcy5cbiAgICAgIDwvTWVzc2FnZT4gOiBudWxsO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXY+XG4gICAgICAgIHtybWxQcmV2aWV3QmxvY2t9XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgIDxoMiBjbGFzc05hbWU9XCJzbWFsbC1tYXJnaW5cIj5VcGxvYWQgYW5kIGNvbm5lY3QgeW91ciBkYXRhc2V0PC9oMj5cbiAgICAgICAgICB7Y29sbGVjdGlvbnNBcmVDb25uZWN0ZWRNZXNzYWdlfVxuICAgICAgICAgIHtwdWJsaXNoRmFpbGVkTWVzc2FnZX1cbiAgICAgICAgICA8cD5Db25uZWN0IHRoZSBleGNlbCBjb2x1bW5zIHRvIHRoZSBwcm9wZXJ0aWVzIG9mIHRoZSBBcmNoZXR5cGVzPC9wPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPENvbGxlY3Rpb25UYWJzIGNvbGxlY3Rpb25UYWJzPXt0YWJzfSBvblNlbGVjdENvbGxlY3Rpb249e29uU2VsZWN0Q29sbGVjdGlvbn0gLz5cbiAgICAgICAgPENvbGxlY3Rpb25Gb3JtIGFyY2hldHlwZUZpZWxkcz17YXJjaGV0eXBlRmllbGRzfSBjb2x1bW5zPXtjb2x1bW5zfSBpZ25vcmVkQ29sdW1ucz17aWdub3JlZENvbHVtbnN9XG4gICAgICAgICAgICAgICAgICAgICAgICBhdmFpbGFibGVBcmNoZXR5cGVzPXthdmFpbGFibGVBcmNoZXR5cGVzfVxuICAgICAgICAgICAgICAgICAgICAgICAgYXZhaWxhYmxlQ29sbGVjdGlvbkNvbHVtbnNQZXJBcmNoZXR5cGU9e2F2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlfVxuICAgICAgICAgICAgICAgICAgICAgICAgY3VzdG9tUHJvcGVydGllcz17Y3VzdG9tUHJvcGVydGllc31cbiAgICAgICAgICAgICAgICAgICAgICAgIG9uQWRkQ3VzdG9tUHJvcGVydHk9e29uQWRkQ3VzdG9tUHJvcGVydHl9XG4gICAgICAgICAgICAgICAgICAgICAgICBvblJlbW92ZUN1c3RvbVByb3BlcnR5PXtvblJlbW92ZUN1c3RvbVByb3BlcnR5fVxuICAgICAgICAgICAgICAgICAgICAgICAgcHJlZGljYXRlT2JqZWN0TWFwcGluZ3M9e3ByZWRpY2F0ZU9iamVjdE1hcHBpbmdzfVxuICAgICAgICAgICAgICAgICAgICAgICAgb25BZGRQcmVkaWNhdGVPYmplY3RNYXA9e29uQWRkUHJlZGljYXRlT2JqZWN0TWFwfVxuICAgICAgICAgICAgICAgICAgICAgICAgb25SZW1vdmVQcmVkaWNhdGVPYmplY3RNYXA9e29uUmVtb3ZlUHJlZGljYXRlT2JqZWN0TWFwfVxuICAgICAgICAgICAgICAgICAgICAgICAgdGFyZ2V0YWJsZVZyZXM9e3RhcmdldGFibGVWcmVzfSAvPlxuXG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJpZy1tYXJnaW5cIj5cbiAgICAgICAgICA8YnV0dG9uIG9uQ2xpY2s9e29uUHVibGlzaERhdGF9IGNsYXNzTmFtZT1cImJ0biBidG4td2FybmluZyBidG4tbGcgcHVsbC1yaWdodFwiIHR5cGU9XCJidXR0b25cIiBkaXNhYmxlZD17IXB1Ymxpc2hFbmFibGVkfT5cbiAgICAgICAgICAgIHtwdWJsaXNoU3RhdHVzfVxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICA8L2Rpdj5cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiaWctbWFyZ2luXCI+XG4gICAgICAgICAgPHAgY2xhc3NOYW1lPVwiZnJvbS1leGNlbFwiPlxuICAgICAgICAgICAgPGltZyBzcmM9XCJpbWFnZXMvaWNvbi1leGNlbC5zdmdcIiBhbHQ9XCJcIi8+e1wiIFwifVxuICAgICAgICAgICAgPGVtPnthY3RpdmVDb2xsZWN0aW9ufTwvZW0+IHt1cGxvYWRlZEZpbGVuYW1lID8gYGZyb20gJHt1cGxvYWRlZEZpbGVuYW1lfWAgOiBcIlwifVxuICAgICAgICAgIDwvcD5cblxuICAgICAgICAgIDxDb2xsZWN0aW9uVGFibGVcbiAgICAgICAgICAgIHJvd3M9e3Jvd3N9XG4gICAgICAgICAgICBoZWFkZXJzPXtoZWFkZXJzfVxuICAgICAgICAgICAgbmV4dFVybD17bmV4dFVybH1cbiAgICAgICAgICAgIG9uSWdub3JlQ29sdW1uVG9nZ2xlPXsoaGVhZGVyKSA9PiBvbklnbm9yZUNvbHVtblRvZ2dsZShhY3RpdmVDb2xsZWN0aW9uLCBoZWFkZXIpfVxuICAgICAgICAgICAgb25Mb2FkTW9yZUNsaWNrPXsodXJsKSA9PiBvbkxvYWRNb3JlQ2xpY2sodXJsLCBhY3RpdmVDb2xsZWN0aW9uKX0gLz5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IENvbm5lY3REYXRhOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5pbXBvcnQgTWVzc2FnZSBmcm9tIFwiLi9tZXNzYWdlXCI7XG5pbXBvcnQgeyB1cmxzIH0gZnJvbSBcIi4uL3JvdXRlclwiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCBDb2xsZWN0aW9uVGFibGUgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJsZVwiO1xuXG5jbGFzcyBDb25uZWN0VG9BcmNoZXR5cGUgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cbiAgY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcbiAgICBjb25zdCB7IG9uRmV0Y2hCdWxrVXBsb2FkZWRNZXRhZGF0YSB9ID0gdGhpcy5wcm9wcztcbiAgICAvLyBUcmlnZ2VycyBmZXRjaCBkYXRhIGZyb20gc2VydmVyIGJhc2VkIG9uIHZyZUlkIGZyb20gcm91dGUuXG4gICAgaWYgKHRoaXMucHJvcHMucGFyYW1zLnZyZUlkICE9PSBuZXh0UHJvcHMucGFyYW1zLnZyZUlkKSB7XG4gICAgICBvbkZldGNoQnVsa1VwbG9hZGVkTWV0YWRhdGEobmV4dFByb3BzLnBhcmFtcy52cmVJZCk7XG4gICAgfVxuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQoKSB7XG4gICAgY29uc3QgeyBvbkZldGNoQnVsa1VwbG9hZGVkTWV0YWRhdGEsIGNvbGxlY3Rpb25zLCB2cmUsIHZyZUlkIH0gPSB0aGlzLnByb3BzO1xuICAgIGlmICghY29sbGVjdGlvbnMgfHwgdnJlICE9PSB2cmVJZCkge1xuICAgICAgb25GZXRjaEJ1bGtVcGxvYWRlZE1ldGFkYXRhKHZyZUlkKTtcbiAgICB9XG4gIH1cblxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7XG4gICAgICB2cmVJZCwgLy8gZnJvbSBwYXJhbXNcbiAgICAgIHZyZSwgLy8gZnJvbSBzZXJ2ZXIgcmVzcG9uc2VcbiAgICAgIGFyY2hldHlwZSxcbiAgICAgIGNvbGxlY3Rpb25zLFxuICAgICAgbWFwcGluZ3MsXG4gICAgfSA9IHRoaXMucHJvcHM7XG5cbiAgICAvLyBhY3Rpb25zXG4gICAgY29uc3QgeyBvbkNsb3NlTWVzc2FnZSwgb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlLCBvblNlbGVjdENvbGxlY3Rpb24sIG9uTG9hZE1vcmVDbGljayB9ID0gdGhpcy5wcm9wcztcbiAgICAvLyBtZXNzYWdlc1xuICAgIGNvbnN0IHsgc2hvd0ZpbGVJc1VwbG9hZGVkTWVzc2FnZSwgdXBsb2FkZWRGaWxlTmFtZSB9ID0gdGhpcy5wcm9wcztcbiAgICAvLyB0YWJsZSB2aWV3IHByb3BlcnRpZXNcbiAgICBjb25zdCB7IHJvd3MsIGhlYWRlcnMsIG5leHRVcmwsIGFjdGl2ZUNvbGxlY3Rpb24gfSA9IHRoaXMucHJvcHM7XG5cbiAgICBpZiAoIWNvbGxlY3Rpb25zIHx8IHZyZSAhPT0gdnJlSWQpIHsgcmV0dXJuIG51bGw7IH1cblxuICAgIGNvbnN0IGNvbGxlY3Rpb25zQXJlTWFwcGVkID0gT2JqZWN0LmtleXMobWFwcGluZ3MuY29sbGVjdGlvbnMpLmxlbmd0aCA+IDAgJiZcbiAgICAgIE9iamVjdC5rZXlzKG1hcHBpbmdzLmNvbGxlY3Rpb25zKS5tYXAoKGtleSkgPT4gbWFwcGluZ3MuY29sbGVjdGlvbnNba2V5XS5hcmNoZXR5cGVOYW1lKS5pbmRleE9mKG51bGwpIDwgMDtcblxuICAgIGNvbnN0IGZpbGVJc1VwbG9hZGVkTWVzc2FnZSA9IHNob3dGaWxlSXNVcGxvYWRlZE1lc3NhZ2UgJiYgdXBsb2FkZWRGaWxlTmFtZSA/IChcbiAgICAgIDxNZXNzYWdlIGFsZXJ0TGV2ZWw9XCJpbmZvXCIgZGlzbWlzc2libGU9e3RydWV9IG9uQ2xvc2VNZXNzYWdlPXsoKSA9PiBvbkNsb3NlTWVzc2FnZShcInNob3dGaWxlSXNVcGxvYWRlZE1lc3NhZ2VcIil9PlxuICAgICAgICA8ZW0+e3VwbG9hZGVkRmlsZU5hbWV9PC9lbT4gaXMgdXBsb2FkZWQuXG4gICAgICA8L01lc3NhZ2U+XG4gICAgKSA6IG51bGw7XG5cblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICA8aDIgY2xhc3NOYW1lPVwic21hbGwtbWFyZ2luXCI+VXBsb2FkIGFuZCBjb25uZWN0IHlvdXIgZGF0YXNldDwvaDI+XG4gICAgICAgICAge2ZpbGVJc1VwbG9hZGVkTWVzc2FnZX1cbiAgICAgICAgICA8cD5XZSBmb3VuZCB7Y29sbGVjdGlvbnMubGVuZ3RofSBjb2xsZWN0aW9ucyBpbiB0aGUgZmlsZS4gQ29ubmVjdCB0aGUgdGFicyB0byB0aGUgVGltYnVjdG9vIEFyY2hldHlwZXMuPC9wPlxuICAgICAgICA8L2Rpdj5cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICB7Y29sbGVjdGlvbnMubWFwKChzaGVldCkgPT4gKFxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJyb3dcIiBrZXk9e3NoZWV0Lm5hbWV9PlxuICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1tZC0yXCI+XG4gICAgICAgICAgICAgICAgPGEgY2xhc3NOYW1lPVwiZnJvbS1leGNlbFwiIHN0eWxlPXt7Y3Vyc29yOiBcInBvaW50ZXJcIn19XG4gICAgICAgICAgICAgICAgICAgb25DbGljaz17KCkgPT4gc2hlZXQubmFtZSA9PT0gYWN0aXZlQ29sbGVjdGlvbiA/IGZhbHNlIDogb25TZWxlY3RDb2xsZWN0aW9uKHNoZWV0Lm5hbWUpfT5cbiAgICAgICAgICAgICAgICAgIDxpbWcgc3JjPVwiaW1hZ2VzL2ljb24tZXhjZWwuc3ZnXCIgYWx0PVwiXCIvPiB7c2hlZXQubmFtZX0ge3NoZWV0Lm5hbWUgPT09IGFjdGl2ZUNvbGxlY3Rpb24gPyBcIipcIiA6IFwiXCJ9XG4gICAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtbWQtOFwiPlxuICAgICAgICAgICAgICAgIDxTZWxlY3RGaWVsZFxuICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHNoZWV0Lm5hbWUsIHZhbHVlKX1cbiAgICAgICAgICAgICAgICAgIG9uQ2xlYXI9eygpID0+IG9uTWFwQ29sbGVjdGlvbkFyY2hldHlwZShzaGVldC5uYW1lLCBudWxsKSB9XG4gICAgICAgICAgICAgICAgICB2YWx1ZT17bWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQubmFtZV0uYXJjaGV0eXBlTmFtZX0+XG4gICAgICAgICAgICAgICAgICAgIDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPlxuICAgICAgICAgICAgICAgICAgICAgIENvbm5lY3QgPGVtPntzaGVldC5uYW1lfTwvZW0+IHRvIGEgVGltYnVjdG9vIGFyY2hldHlwZS5cbiAgICAgICAgICAgICAgICAgICAgPC9zcGFuPlxuICAgICAgICAgICAgICAgICAge09iamVjdC5rZXlzKGFyY2hldHlwZSkuZmlsdGVyKChkb21haW4pID0+IGRvbWFpbiAhPT0gXCJyZWxhdGlvbnNcIikuc29ydCgpLm1hcCgob3B0aW9uKSA9PiAoXG4gICAgICAgICAgICAgICAgICAgIDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufVxuICAgICAgICAgICAgICAgICAgICAgIDxiciAvPjxzcGFuIHN0eWxlPXt7Y29sb3I6IFwiIzY2NlwiLCBmb250U2l6ZTogXCIwLjZlbVwifX0+XG4gICAgICAgICAgICAgICAgICAgICAgICBQcm9wZXJ0aWVzOiB7YXJjaGV0eXBlW29wdGlvbl1cbiAgICAgICAgICAgICAgICAgICAgICAgICAgLmZpbHRlcigocHJvcCkgPT4gcHJvcC50eXBlICE9PSBcInJlbGF0aW9uXCIpXG4gICAgICAgICAgICAgICAgICAgICAgICAgIC5tYXAoKHByb3ApID0+IGAke3Byb3AubmFtZX0gKCR7cHJvcC50eXBlfSlgKS5qb2luKFwiLCBcIil9XG4gICAgICAgICAgICAgICAgICAgICAgPC9zcGFuPlxuICAgICAgICAgICAgICAgICAgICA8L3NwYW4+XG4gICAgICAgICAgICAgICAgICApKX1cbiAgICAgICAgICAgICAgICA8L1NlbGVjdEZpZWxkPlxuICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgICAgeyBtYXBwaW5ncy5jb2xsZWN0aW9uc1tzaGVldC5uYW1lXS5hcmNoZXR5cGVOYW1lID8gKFxuICAgICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgaGktc3VjY2Vzc1wiIGtleT17c2hlZXQubmFtZX0+XG4gICAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLW9rIHB1bGwtcmlnaHRcIi8+XG4gICAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgICkgOiBudWxsXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICkpfVxuXG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICB7IGNvbGxlY3Rpb25zQXJlTWFwcGVkID9cbiAgICAgICAgICAgIDxMaW5rIHRvPXt1cmxzLm1hcERhdGEodnJlLCBtYXBwaW5ncy5jb2xsZWN0aW9ucyl9IGNsYXNzTmFtZT1cImJ0biBidG4tc3VjY2Vzc1wiPlxuICAgICAgICAgICAgICBDb25uZWN0XG4gICAgICAgICAgICA8L0xpbms+XG4gICAgICAgICAgICA6XG4gICAgICAgICAgICA8YnV0dG9uIGRpc2FibGVkPXt0cnVlfSBjbGFzc05hbWU9XCJidG4gYnRuLXN1Y2Nlc3NcIj5cbiAgICAgICAgICAgICAgQ29ubmVjdFxuICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgfVxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmlnLW1hcmdpblwiPlxuICAgICAgICAgIDxwIGNsYXNzTmFtZT1cImZyb20tZXhjZWxcIj5cbiAgICAgICAgICAgIDxpbWcgc3JjPVwiaW1hZ2VzL2ljb24tZXhjZWwuc3ZnXCIgYWx0PVwiXCIvPntcIiBcIn1cbiAgICAgICAgICAgIDxlbT57YWN0aXZlQ29sbGVjdGlvbn08L2VtPiB7dXBsb2FkZWRGaWxlTmFtZSA/IGBmcm9tICR7dXBsb2FkZWRGaWxlTmFtZX1gIDogXCJcIn1cbiAgICAgICAgICA8L3A+XG5cbiAgICAgICAgICA8Q29sbGVjdGlvblRhYmxlXG4gICAgICAgICAgICByb3dzPXtyb3dzfVxuICAgICAgICAgICAgaGVhZGVycz17aGVhZGVyc31cbiAgICAgICAgICAgIG5leHRVcmw9e25leHRVcmx9XG4gICAgICAgICAgICBvbklnbm9yZUNvbHVtblRvZ2dsZT17bnVsbH1cbiAgICAgICAgICAgIG9uTG9hZE1vcmVDbGljaz17KHVybCkgPT4gb25Mb2FkTW9yZUNsaWNrKHVybCwgYWN0aXZlQ29sbGVjdGlvbil9IC8+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IENvbm5lY3RUb0FyY2hldHlwZTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IERhdGFTZXRDYXJkIGZyb20gJy4vZGF0YXNldENhcmQuanN4JztcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyB2cmVzLCBjYXB0aW9uLCB1c2VySWQsIHNlYXJjaEd1aVVybCwgbWluZSwgb25Db250aW51ZU1hcHBpbmcgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICAgICAgPGgzPntjYXB0aW9ufTwvaDM+XG4gICAgICA8L2Rpdj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmlnLW1hcmdpblwiPlxuICAgICAgICB7IE9iamVjdC5rZXlzKHZyZXMpLm1hcCgodnJlKSA9PiAoXG4gICAgICAgICAgPERhdGFTZXRDYXJkIGtleT17dnJlfSBtaW5lPXttaW5lfSBwdWJsaXNoZWQ9e3ZyZXNbdnJlXS5wdWJsaXNoZWR9IHNlYXJjaEd1aVVybD17c2VhcmNoR3VpVXJsfVxuICAgICAgICAgICAgICAgICAgICAgICBvbkNvbnRpbnVlTWFwcGluZz17b25Db250aW51ZU1hcHBpbmd9XG4gICAgICAgICAgICAgICAgICAgICAgIHVzZXJJZD17dXNlcklkfSB2cmVJZD17dnJlc1t2cmVdLm5hbWV9IGNhcHRpb249e3ZyZXNbdnJlXS5uYW1lLnJlcGxhY2UoL15bYS16MC05XStfLywgXCJcIil9IC8+XG4gICAgICAgICkpfVxuICAgICA8L2Rpdj5cbiAgICA8L2Rpdj5cbiAgKVxufTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vcm91dGVyXCI7XG5cblxuZnVuY3Rpb24gRGF0YVNldENhcmQocHJvcHMpIHtcbiAgdmFyIHNlYXJjaFVybCA9IHByb3BzLnNlYXJjaEd1aVVybDtcblxuICBpZiAocHJvcHMubWluZSAmJiAhcHJvcHMucHVibGlzaGVkKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY2FyZC1kYXRhc2V0XCI+XG4gICAgICAgIDxMaW5rIGNsYXNzTmFtZT1cImNhcmQtZGF0YXNldCBidG4gYnRuLWRlZmF1bHQgZXhwbG9yZVwiIHRvPXt1cmxzLm1hcEFyY2hldHlwZXMocHJvcHMudnJlSWQpfT5cbiAgICAgICAgICBGaW5pc2ggbWFwcGluZzxiciAvPlxuICAgICAgICAgIDxzdHJvbmcgdGl0bGU9e3Byb3BzLmNhcHRpb259IHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgb3ZlcmZsb3c6IFwiaGlkZGVuXCIsIHdpZHRoOiBcIjkwJVwiLCB3aGl0ZVNwYWNlOiBcIm5vd3JhcFwiLCB0ZXh0T3ZlcmZsb3c6IFwiZWxsaXBzaXNcIn19PlxuICAgICAgICAgICAge3Byb3BzLmNhcHRpb24ucmVwbGFjZSgvXlteX10rXysvLCBcIlwiKX1cbiAgICAgICAgICA8L3N0cm9uZz5cbiAgICAgICAgPC9MaW5rPlxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNhcmQtZGF0YXNldFwiPlxuICAgICAgPGEgY2xhc3NOYW1lPVwiY2FyZC1kYXRhc2V0IGJ0biBidG4tZGVmYXVsdCBleHBsb3JlXCJcbiAgICAgICAgIGhyZWY9e2Ake3NlYXJjaFVybH0/dnJlSWQ9JHtwcm9wcy52cmVJZH1gfSB0YXJnZXQ9XCJfYmxhbmtcIj5cbiAgICAgICAgRXhwbG9yZTxiciAvPlxuICAgICAgICA8c3Ryb25nIHRpdGxlPXtwcm9wcy5jYXB0aW9ufSBzdHlsZT17e2Rpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIG92ZXJmbG93OiBcImhpZGRlblwiLCB3aWR0aDogXCI5MCVcIiwgd2hpdGVTcGFjZTogXCJub3dyYXBcIiwgdGV4dE92ZXJmbG93OiBcImVsbGlwc2lzXCJ9fT5cbiAgICAgICAgICAgIHtwcm9wcy5jYXB0aW9uLnJlcGxhY2UoL15bXl9dK18rLywgXCJcIil9XG4gICAgICAgIDwvc3Ryb25nPlxuICAgICAgPC9hPlxuICAgICAge3Byb3BzLnVzZXJJZFxuICAgICAgICA/ICg8YSBjbGFzc05hbWU9XCJjYXJkLWRhdGFzZXQgYnRuIGJ0bi1kZWZhdWx0XCJcbiAgICAgICAgICAgICAgaHJlZj17YCR7cHJvY2Vzcy5lbnYuc2VydmVyfS9zdGF0aWMvZWRpdC1ndWkvP3ZyZUlkPSR7cHJvcHMudnJlSWR9JmhzaWQ9JHtwcm9wcy51c2VySWR9YH0gdGFyZ2V0PVwiX2JsYW5rXCI+XG4gICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiIC8+e1wiIFwifVxuICAgICAgICAgICAgRWRpdFxuICAgICAgICAgIDwvYT4pXG4gICAgICAgIDogbnVsbH1cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgRGF0YVNldENhcmQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgaXNPcGVuOiBmYWxzZVxuICAgIH07XG4gICAgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50KCkge1xuICAgIGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG4gIH1cblxuICB0b2dnbGVTZWxlY3QoKSB7XG4gICAgaWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG4gICAgfVxuICB9XG5cbiAgaGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuICAgIGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuICAgIGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe1xuICAgICAgICBpc09wZW46IGZhbHNlXG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbkNoYW5nZSwgb25DbGVhciwgdmFsdWUgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBzZWxlY3RlZE9wdGlvbiA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSA9PT0gdmFsdWUpO1xuICAgIGNvbnN0IHBsYWNlaG9sZGVyID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnR5cGUgPT09IFwicGxhY2Vob2xkZXJcIik7XG4gICAgY29uc3Qgb3RoZXJPcHRpb25zID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnZhbHVlICYmIG9wdC5wcm9wcy52YWx1ZSAhPT0gdmFsdWUpO1xuXG4gICAgcmV0dXJuIChcblxuICAgICAgPGRpdiBjbGFzc05hbWU9e2N4KFwiZHJvcGRvd25cIiwge29wZW46IHRoaXMuc3RhdGUuaXNPcGVufSl9IHN0eWxlPXt0aGlzLnByb3BzLnN0eWxlIHx8IHt9fT5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGRyb3Bkb3duLXRvZ2dsZVwiIG9uQ2xpY2s9e3RoaXMudG9nZ2xlU2VsZWN0LmJpbmQodGhpcyl9PlxuICAgICAgICAgIHtzZWxlY3RlZE9wdGlvbi5sZW5ndGggPyBzZWxlY3RlZE9wdGlvbiA6IHBsYWNlaG9sZGVyfSA8c3BhbiBjbGFzc05hbWU9XCJjYXJldFwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuXG4gICAgICAgIDx1bCBjbGFzc05hbWU9XCJkcm9wZG93bi1tZW51XCI+XG4gICAgICAgICAgeyB2YWx1ZSA/IChcbiAgICAgICAgICAgIDxsaT5cbiAgICAgICAgICAgICAgPGEgb25DbGljaz17KCkgPT4geyBvbkNsZWFyKCk7IHRoaXMudG9nZ2xlU2VsZWN0KCk7fX0+XG4gICAgICAgICAgICAgICAgLSBjbGVhciAtXG4gICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSA6IG51bGx9XG4gICAgICAgICAge290aGVyT3B0aW9ucy5tYXAoKG9wdGlvbiwgaSkgPT4gKFxuICAgICAgICAgICAgPGxpIGtleT17aX0+XG4gICAgICAgICAgICAgIDxhIHN0eWxlPXt7Y3Vyc29yOiBcInBvaW50ZXJcIn19IG9uQ2xpY2s9eygpID0+IHsgb25DaGFuZ2Uob3B0aW9uLnByb3BzLnZhbHVlKTsgdGhpcy50b2dnbGVTZWxlY3QoKTsgfX0+e29wdGlvbn08L2E+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5TZWxlY3RGaWVsZC5wcm9wVHlwZXMgPSB7XG4gIG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgb25DbGVhcjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIHZhbHVlOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBVcGxvYWRCdXR0b24gZnJvbSBcIi4vdXBsb2FkLWJ1dHRvblwiO1xuXG5mdW5jdGlvbiBGaXJzdFVwbG9hZChwcm9wcykge1xuICBjb25zdCB7IG9uVXBsb2FkRmlsZVNlbGVjdCwgdXNlcklkLCB1cGxvYWRTdGF0dXMgfSA9IHByb3BzO1xuXG4gIGNvbnN0IHNhbXBsZVNoZWV0ID0gcHJvcHMuZXhhbXBsZVNoZWV0VXJsID9cbiAgICA8cD5Eb24ndCBoYXZlIGEgZGF0YXNldCBoYW5keT8gSGVyZeKAmXMgYW4gPGEgaHJlZj17cHJvcHMuZXhhbXBsZVNoZWV0VXJsfT5leGFtcGxlIGV4Y2VsIHNoZWV0PC9hPi48L3A+IDogbnVsbDtcblxuICBjb25zdCB1cGxvYWRCdXR0b24gPSAoXG4gICAgPFVwbG9hZEJ1dHRvblxuICAgICAgdXBsb2FkU3RhdHVzPXt1cGxvYWRTdGF0dXN9XG4gICAgICBjbGFzc05hbWVzPXtbXCJidG5cIiwgXCJidG4tbGdcIiwgXCJidG4tcHJpbWFyeVwiXX1cbiAgICAgIGdseXBoaWNvbj1cImdseXBoaWNvbiBnbHlwaGljb24tY2xvdWQtdXBsb2FkXCJcbiAgICAgIGxhYmVsPVwiQnJvd3NlXCJcbiAgICAgIG9uVXBsb2FkRmlsZVNlbGVjdD17b25VcGxvYWRGaWxlU2VsZWN0fSAvPlxuICApO1xuXG4gIGNvbnNvbGUubG9nKHVzZXJJZCk7XG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwianVtYm90cm9uIGZpcnN0LXVwbG9hZCB1cGxvYWQtYmdcIj5cbiAgICAgICAgPGgyPlVwbG9hZCB5b3VyIGZpcnN0IGRhdGFzZXQ8L2gyPlxuICAgICAgICB7c2FtcGxlU2hlZXR9XG4gICAgICAgIHt1c2VySWQgPyB1cGxvYWRCdXR0b24gOiAoXG4gICAgICAgICAgPGZvcm0gYWN0aW9uPVwiaHR0cHM6Ly9zZWN1cmUuaHV5Z2Vucy5rbmF3Lm5sL3NhbWwyL2xvZ2luXCIgbWV0aG9kPVwiUE9TVFwiPlxuICAgICAgICAgICAgPGlucHV0IG5hbWU9XCJoc3VybFwiICB0eXBlPVwiaGlkZGVuXCIgdmFsdWU9e3dpbmRvdy5sb2NhdGlvbi5ocmVmfSAvPlxuICAgICAgICAgICAgPHA+TW9zdCB1bml2ZXJzaXR5IGFjY291bnRzIHdpbGwgd29yay48L3A+XG4gICAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tcHJpbWFyeSBidG4tbGdcIiB0eXBlPVwic3VibWl0XCI+XG4gICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tbG9nLWluXCIgLz4gTG9nIGluIHRvIHVwbG9hZFxuICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgPC9mb3JtPikgfVxuICAgICAgPC9kaXY+XG4gICAgPC9kaXY+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IEZpcnN0VXBsb2FkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5mdW5jdGlvbiBGb290ZXIocHJvcHMpIHtcbiAgY29uc3QgaGlMb2dvID0gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgY29sLW1kLTFcIj5cbiAgICAgIDxpbWcgY2xhc3NOYW1lPVwiaGktbG9nb1wiIHNyYz1cImltYWdlcy9sb2dvLWh1eWdlbnMtaW5nLnN2Z1wiIC8+XG4gICAgPC9kaXY+XG4gICk7XG5cbiAgY29uc3QgY2xhcmlhaExvZ28gPSAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMSBjb2wtbWQtMVwiPlxuICAgICAgPGltZyBjbGFzc05hbWU9XCJsb2dvXCIgc3JjPVwiaW1hZ2VzL2xvZ28tY2xhcmlhaC5zdmdcIiAvPlxuICAgIDwvZGl2PlxuICApO1xuXG4gIGNvbnN0IGZvb3RlckJvZHkgPSBSZWFjdC5DaGlsZHJlbi5jb3VudChwcm9wcy5jaGlsZHJlbikgPiAwID9cbiAgICBSZWFjdC5DaGlsZHJlbi5tYXAocHJvcHMuY2hpbGRyZW4sIChjaGlsZCwgaSkgPT4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJ3aGl0ZS1iYXJcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICB7aSA9PT0gUmVhY3QuQ2hpbGRyZW4uY291bnQocHJvcHMuY2hpbGRyZW4pIC0gMVxuICAgICAgICAgICAgPyAoPGRpdiBjbGFzc05hbWU9XCJyb3dcIj57aGlMb2dvfTxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEwIGNvbC1tZC0xMCB0ZXh0LWNlbnRlclwiPntjaGlsZH08L2Rpdj57Y2xhcmlhaExvZ299PC9kaXY+KVxuICAgICAgICAgICAgOiAoPGRpdiBjbGFzc05hbWU9XCJyb3dcIj48ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMiBjb2wtbWQtMTIgdGV4dC1jZW50ZXJcIj57Y2hpbGR9PC9kaXY+PC9kaXY+KVxuICAgICAgICAgIH1cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApKSA6IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwid2hpdGUtYmFyXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJyb3dcIj5cbiAgICAgICAgICAgIHtoaUxvZ299XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMCBjb2wtbWQtMTAgdGV4dC1jZW50ZXJcIj5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAge2NsYXJpYWhMb2dvfVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG5cblxuICByZXR1cm4gKFxuICAgIDxmb290ZXIgY2xhc3NOYW1lPVwiZm9vdGVyXCI+XG4gICAgICB7Zm9vdGVyQm9keX1cbiAgICA8L2Zvb3Rlcj5cbiAgKVxufVxuXG5leHBvcnQgZGVmYXVsdCBGb290ZXI7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgZGlzbWlzc2libGUsIGFsZXJ0TGV2ZWwsIG9uQ2xvc2VNZXNzYWdlfSA9IHByb3BzO1xuICBjb25zdCBkaXNtaXNzQnV0dG9uID0gZGlzbWlzc2libGVcbiAgICA/IDxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzTmFtZT1cImNsb3NlXCIgb25DbGljaz17b25DbG9zZU1lc3NhZ2V9PjxzcGFuPiZ0aW1lczs8L3NwYW4+PC9idXR0b24+XG4gICAgOiBudWxsO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9e2N4KFwiYWxlcnRcIiwgYGFsZXJ0LSR7YWxlcnRMZXZlbH1gLCB7XCJhbGVydC1kaXNtaXNzaWJsZVwiOiBkaXNtaXNzaWJsZX0pfSByb2xlPVwiYWxlcnRcIj5cbiAgICAgIHtkaXNtaXNzQnV0dG9ufVxuICAgICAge3Byb3BzLmNoaWxkcmVufVxuICAgIDwvZGl2PlxuICApXG59OyIsImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgRGF0YXNldENhcmRzIGZyb20gXCIuL2RhdGFzZXQtY2FyZHNcIjtcbmltcG9ydCBGb290ZXIgZnJvbSBcIi4vZm9vdGVyXCI7XG5cbmNvbnN0IEZPT1RFUl9IRUlHSFQgPSA4MTtcblxuZnVuY3Rpb24gUGFnZShwcm9wcykge1xuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwicGFnZVwiPlxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW4gaGktR3JlZW4gY29udGFpbmVyLWZsdWlkXCI+XG4gICAgICAgIDxuYXYgY2xhc3NOYW1lPVwibmF2YmFyIFwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cIm5hdmJhci1oZWFkZXJcIj4gPGEgY2xhc3NOYW1lPVwibmF2YmFyLWJyYW5kXCIgaHJlZj1cIiNcIj48aW1nIHNyYz1cImltYWdlcy9sb2dvLXRpbWJ1Y3Rvby5zdmdcIiBjbGFzc05hbWU9XCJsb2dvXCIgYWx0PVwidGltYnVjdG9vXCIvPjwvYT4gPC9kaXY+XG4gICAgICAgICAgICA8ZGl2IGlkPVwibmF2YmFyXCIgY2xhc3NOYW1lPVwibmF2YmFyLWNvbGxhcHNlIGNvbGxhcHNlXCI+XG4gICAgICAgICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2YmFyLW5hdiBuYXZiYXItcmlnaHRcIj5cbiAgICAgICAgICAgICAgICB7cHJvcHMudXNlcm5hbWUgPyA8bGk+PGEgaHJlZj17cHJvcHMudXNlcmxvY2F0aW9uIHx8ICcjJ30+PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi11c2VyXCIvPiB7cHJvcHMudXNlcm5hbWV9PC9hPjwvbGk+IDogbnVsbH1cbiAgICAgICAgICAgICAgPC91bD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L25hdj5cbiAgICAgIDwvZGl2PlxuICAgICAgPGRpdiAgc3R5bGU9e3ttYXJnaW5Cb3R0b206IGAke0ZPT1RFUl9IRUlHSFR9cHhgfX0+XG4gICAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICAgICAge3Byb3BzLnZyZXMgJiYgcHJvcHMuc2hvd0RhdGFzZXRzID8gKFxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgICA8RGF0YXNldENhcmRzIGNhcHRpb249XCJFeHBsb3JlIGFsbCBkYXRhc2V0c1wiIHZyZXM9e3Byb3BzLnZyZXN9IHNlYXJjaEd1aVVybD17cHJvcHMuc2VhcmNoR3VpVXJsfSAvPlxuICAgICAgICAgIDwvZGl2PikgOiBudWxsfVxuICAgICAgPC9kaXY+XG4gICAgICA8Rm9vdGVyIC8+XG4gICAgPC9kaXY+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IFBhZ2U7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgRGF0YVJvdyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgcm93IH0gPSB0aGlzLnByb3BzO1xuICAgIHJldHVybiAoXG4gICAgICA8dHI+XG4gICAgICAgIHtyb3cubWFwKChjZWxsLCBpKSA9PiAoXG4gICAgICAgICAgPHRkIGNsYXNzTmFtZT17Y3goe1xuICAgICAgICAgICAgaWdub3JlZDogY2VsbC5pZ25vcmVkLFxuICAgICAgICAgICAgZGFuZ2VyOiBjZWxsLmVycm9yID8gdHJ1ZSA6IGZhbHNlXG4gICAgICAgICAgfSl9IGtleT17aX0+XG4gICAgICAgICAgICB7Y2VsbC52YWx1ZX1cbiAgICAgICAgICAgIHtjZWxsLmVycm9yID8gPHNwYW4gY2xhc3NOYW1lPVwicHVsbC1yaWdodCBnbHlwaGljb24gZ2x5cGhpY29uLWV4Y2xhbWF0aW9uLXNpZ25cIiBzdHlsZT17e2N1cnNvcjogXCJwb2ludGVyXCJ9fSB0aXRsZT17Y2VsbC5lcnJvcn0gLz4gOiBudWxsfVxuICAgICAgICAgIDwvdGQ+XG4gICAgICAgICkpfVxuICAgICAgPC90cj5cbiAgICApO1xuICB9XG59XG5cbkRhdGFSb3cucHJvcFR5cGVzID0ge1xuICByb3c6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IERhdGFSb3c7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIEhlYWRlckNlbGwgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IGhlYWRlciwgaXNDb25maXJtZWQsIGlzSWdub3JlZCwgb25JZ25vcmVDb2x1bW5Ub2dnbGUgfSA9IHRoaXMucHJvcHM7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPHRoIGNsYXNzTmFtZT17Y3goe1xuICAgICAgICBzdWNjZXNzOiBpc0NvbmZpcm1lZCxcbiAgICAgICAgaW5mbzogIWlzQ29uZmlybWVkICYmICFpc0lnbm9yZWQsXG4gICAgICAgIGRhbmdlcjogIWlzQ29uZmlybWVkICYmIGlzSWdub3JlZFxuICAgICAgfSl9PlxuICAgICAgICB7aGVhZGVyfVxuICAgICAgICB7IG9uSWdub3JlQ29sdW1uVG9nZ2xlICE9PSBudWxsID8gKFxuICAgICAgICAgIDxhIHN0eWxlPXt7Y3Vyc29yOiBcInBvaW50ZXJcIn19IGNsYXNzTmFtZT17Y3goXCJwdWxsLXJpZ2h0XCIsIFwiZ2x5cGhpY29uXCIsIHtcbiAgICAgICAgICAgIFwiZ2x5cGhpY29uLW9rLXNpZ25cIjogaXNDb25maXJtZWQsXG4gICAgICAgICAgICBcImdseXBoaWNvbi1xdWVzdGlvbi1zaWduXCI6ICFpc0NvbmZpcm1lZCAmJiAhaXNJZ25vcmVkLFxuICAgICAgICAgICAgXCJnbHlwaGljb24tcmVtb3ZlXCI6ICFpc0NvbmZpcm1lZCAmJiBpc0lnbm9yZWRcbiAgICAgICAgICB9KX0gb25DbGljaz17KCkgPT4gIWlzQ29uZmlybWVkID8gb25JZ25vcmVDb2x1bW5Ub2dnbGUoaGVhZGVyKSA6IG51bGwgfSA+XG4gICAgICAgICAgPC9hPlxuICAgICAgICApIDogbnVsbCB9XG4gICAgICA8L3RoPlxuICAgICk7XG4gIH1cbn1cblxuSGVhZGVyQ2VsbC5wcm9wVHlwZXMgPSB7XG4gIGhlYWRlcjogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcbiAgaXNDb25maXJtZWQ6IFJlYWN0LlByb3BUeXBlcy5ib29sLFxuICBpc0lnbm9yZWQ6IFJlYWN0LlByb3BUeXBlcy5ib29sLFxuICBvbklnbm9yZUNvbHVtblRvZ2dsZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEhlYWRlckNlbGw7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFVwbG9hZEJ1dHRvbiBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgY2xhc3NOYW1lcywgdXBsb2FkU3RhdHVzLCBvblVwbG9hZEZpbGVTZWxlY3QsIGdseXBoaWNvbiwgbGFiZWwgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIChcbiAgICAgIDxmb3JtPlxuICAgICAgICA8bGFiZWwgY2xhc3NOYW1lPXtjeCguLi5jbGFzc05hbWVzLCB7ZGlzYWJsZWQ6ICEhdXBsb2FkU3RhdHVzfSl9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT17Z2x5cGhpY29ufT48L3NwYW4+XG4gICAgICAgICAge1wiIFwifVxuICAgICAgICAgIHt1cGxvYWRTdGF0dXMgfHwgbGFiZWx9XG4gICAgICAgICAgPGlucHV0XG4gICAgICAgICAgICBkaXNhYmxlZD17ISF1cGxvYWRTdGF0dXN9XG4gICAgICAgICAgICBvbkNoYW5nZT17ZSA9PiBvblVwbG9hZEZpbGVTZWxlY3QoZS50YXJnZXQuZmlsZXMpfVxuICAgICAgICAgICAgc3R5bGU9e3tkaXNwbGF5OiBcIm5vbmVcIn19XG4gICAgICAgICAgICB0eXBlPVwiZmlsZVwiIC8+XG4gICAgICAgIDwvbGFiZWw+XG4gICAgICA8L2Zvcm0+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBVcGxvYWRCdXR0b247IiwiZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oYXBwU3RhdGUpIHtcbiAgcmV0dXJuIHtcbiAgICB1c2VySWQ6IGFwcFN0YXRlLnVzZXJkYXRhLnVzZXJJZCxcbiAgICB2cmVzOiBhcHBTdGF0ZS51c2VyZGF0YS5teVZyZXMgfHwge30sXG4gICAgc2VhcmNoR3VpVXJsOiBhcHBTdGF0ZS5kYXRhc2V0cy5zZWFyY2hHdWlVcmwsXG4gICAgdXBsb2FkU3RhdHVzOiBhcHBTdGF0ZS5pbXBvcnREYXRhLnVwbG9hZFN0YXR1c1xuICB9XG59IiwiaW1wb3J0IHsgdHJhbnNmb3JtQ29sbGVjdGlvblJvd3MsIHRyYW5zZm9ybUNvbGxlY3Rpb25Db2x1bW5zLCBnZXRDb2x1bW5JbmZvIH0gZnJvbSBcIi4vdHJhbnNmb3JtZXJzL3RhYmxlXCI7XG5pbXBvcnQgeyB0cmFuc2Zvcm1Db2xsZWN0aW9uVGFicyB9IGZyb20gXCIuL3RyYW5zZm9ybWVycy90YWJzXCJcbmltcG9ydCBnZW5lcmF0ZVJtbE1hcHBpbmcgZnJvbSBcIi4uL3V0aWwvZ2VuZXJhdGUtcm1sLW1hcHBpbmdcIjtcbmltcG9ydCB7dW5pcX0gZnJvbSBcIi4uL3V0aWwvdW5pcVwiO1xuXG5mdW5jdGlvbiBnZXRUYXJnZXRhYmxlVnJlcyhtaW5lLCB2cmVzLCBhY3RpdmVWcmUpIHtcbiAgY29uc3QgbXlWcmVzID0gT2JqZWN0LmtleXMobWluZSB8fCB7fSlcbiAgICAubWFwKChrZXkpID0+IG1pbmVba2V5XSlcbiAgICAuZmlsdGVyKCh2cmUpID0+IHZyZS5wdWJsaXNoZWQpXG4gICAgLm1hcCgodnJlKSA9PiB2cmUubmFtZSk7XG4gIGNvbnN0IHB1YmxpY1ZyZXMgPSBPYmplY3Qua2V5cyh2cmVzIHx8IHt9KVxuICAgIC5tYXAoKGtleSkgPT4gdnJlc1trZXldLm5hbWUpO1xuXG4gIHJldHVybiBteVZyZXMuY29uY2F0KHB1YmxpY1ZyZXMpLnJlZHVjZSh1bmlxLCBbXSkuZmlsdGVyKHZyZSA9PiB2cmUgIT09IGFjdGl2ZVZyZSk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IChhcHBTdGF0ZSwgcm91dGVkKSA9PiB7XG5cbiAgY29uc3QgeyBjb2xsZWN0aW9ucyB9ID0gYXBwU3RhdGUuaW1wb3J0RGF0YTtcbiAgY29uc3QgeyBtYXBwaW5ncywgYWN0aXZlQ29sbGVjdGlvbiwgYXJjaGV0eXBlLCBjdXN0b21Qcm9wZXJ0aWVzLFxuICAgIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzIDogYWxsUHJlZGljYXRlT2JqZWN0TWFwcGluZ3MgfSA9IGFwcFN0YXRlO1xuXG4gIGNvbnN0IHsgdXNlcmRhdGE6IHsgbXlWcmVzIH0sIGRhdGFzZXRzOiB7IHB1YmxpY1ZyZXMgfX0gPSBhcHBTdGF0ZTtcblxuICBjb25zdCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncyA9IGFsbFByZWRpY2F0ZU9iamVjdE1hcHBpbmdzW2FjdGl2ZUNvbGxlY3Rpb24ubmFtZV0gfHwgW107XG5cbiAgY29uc3QgYXJjaGV0eXBlRmllbGRzID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbYWN0aXZlQ29sbGVjdGlvbi5uYW1lXSA/XG4gICAgYXJjaGV0eXBlW21hcHBpbmdzLmNvbGxlY3Rpb25zW2FjdGl2ZUNvbGxlY3Rpb24ubmFtZV0uYXJjaGV0eXBlTmFtZV0gOiBbXTtcblxuICBjb25zdCBjb2x1bW5IZWFkZXJzID0gdHJhbnNmb3JtQ29sbGVjdGlvbkNvbHVtbnMoY29sbGVjdGlvbnMsIGFjdGl2ZUNvbGxlY3Rpb24sIG1hcHBpbmdzLCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncyk7XG5cbiAgY29uc3QgY29sbGVjdGlvblRhYnMgPSB0cmFuc2Zvcm1Db2xsZWN0aW9uVGFicyhjb2xsZWN0aW9ucywgbWFwcGluZ3MsIGFjdGl2ZUNvbGxlY3Rpb24sIGFsbFByZWRpY2F0ZU9iamVjdE1hcHBpbmdzKTtcblxuICBjb25zdCBhdmFpbGFibGVBcmNoZXR5cGVzID0gT2JqZWN0LmtleXMobWFwcGluZ3MuY29sbGVjdGlvbnMpLm1hcCgoa2V5KSA9PiBtYXBwaW5ncy5jb2xsZWN0aW9uc1trZXldLmFyY2hldHlwZU5hbWUpO1xuXG4gIGNvbnN0IGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlID0gYXZhaWxhYmxlQXJjaGV0eXBlcy5tYXAoKGFyY2hldHlwZU5hbWUpID0+ICh7XG4gICAga2V5OiBhcmNoZXR5cGVOYW1lLFxuICAgIHZhbHVlczogT2JqZWN0LmtleXMobWFwcGluZ3MuY29sbGVjdGlvbnMpXG4gICAgICAuZmlsdGVyKChjb2xsZWN0aW9uTmFtZSkgPT4gbWFwcGluZ3MuY29sbGVjdGlvbnNbY29sbGVjdGlvbk5hbWVdLmFyY2hldHlwZU5hbWUgPT09IGFyY2hldHlwZU5hbWUpXG4gICAgICAubWFwKChjb2xsZWN0aW9uTmFtZSkgPT4gKHtcbiAgICAgICAgY29sbGVjdGlvbk5hbWU6IGNvbGxlY3Rpb25OYW1lLFxuICAgICAgICBjb2x1bW5zOiBjb2xsZWN0aW9ucy5maW5kKChjb2xsKSA9PiBjb2xsLm5hbWUgPT09IGNvbGxlY3Rpb25OYW1lKS52YXJpYWJsZXNcbiAgICAgIH0pKVxuICB9KSkucmVkdWNlKChhY2N1bSwgY3VyKSA9PiAoey4uLmFjY3VtLCBbY3VyLmtleV06IGN1ci52YWx1ZXN9KSwge30pO1xuXG5cbiAgcmV0dXJuIHtcbiAgICAvLyBmcm9tIHJvdXRlclxuICAgIHZyZUlkOiByb3V0ZWQucGFyYW1zLnZyZUlkLFxuICAgIC8vIHRyYW5zZm9ybWVkIGZvciB2aWV3XG4gICAgdGFiczogY29sbGVjdGlvblRhYnMsXG5cbiAgICAvLyBtZXNzYWdlc1xuICAgIHNob3dDb2xsZWN0aW9uc0FyZUNvbm5lY3RlZE1lc3NhZ2U6IGFwcFN0YXRlLm1lc3NhZ2VzLnNob3dDb2xsZWN0aW9uc0FyZUNvbm5lY3RlZE1lc3NhZ2UsXG5cbiAgICAvLyBmcm9tIGFjdGl2ZSBjb2xsZWN0aW9uIGZvciB0YWJsZVxuICAgIGFjdGl2ZUNvbGxlY3Rpb246IGFjdGl2ZUNvbGxlY3Rpb24ubmFtZSxcbiAgICByb3dzOiB0cmFuc2Zvcm1Db2xsZWN0aW9uUm93cyhjb2xsZWN0aW9ucywgYWN0aXZlQ29sbGVjdGlvbiwgbWFwcGluZ3MpLFxuICAgIGhlYWRlcnM6IGNvbHVtbkhlYWRlcnMsXG4gICAgbmV4dFVybDogYWN0aXZlQ29sbGVjdGlvbi5uZXh0VXJsLFxuXG4gICAgLy8gZnJvbSBpbXBvcnQgZGF0YVxuICAgIHVwbG9hZFN0YXR1czogYXBwU3RhdGUuaW1wb3J0RGF0YS51cGxvYWRTdGF0dXMsXG4gICAgdXBsb2FkZWRGaWxlbmFtZTogYXBwU3RhdGUuaW1wb3J0RGF0YS51cGxvYWRlZEZpbGVOYW1lLFxuICAgIHZyZTogYXBwU3RhdGUuaW1wb3J0RGF0YS52cmUsXG5cbiAgICAvLyBmb3JtIGRhdGFcbiAgICBhcmNoZXR5cGVGaWVsZHM6IGFyY2hldHlwZUZpZWxkcyxcbiAgICBhdmFpbGFibGVBcmNoZXR5cGVzOiBhdmFpbGFibGVBcmNoZXR5cGVzLFxuICAgIGF2YWlsYWJsZUNvbGxlY3Rpb25Db2x1bW5zUGVyQXJjaGV0eXBlOiBhdmFpbGFibGVDb2xsZWN0aW9uQ29sdW1uc1BlckFyY2hldHlwZSxcbiAgICBjb2x1bW5zOiBnZXRDb2x1bW5JbmZvKGNvbGxlY3Rpb25zLCBhY3RpdmVDb2xsZWN0aW9uLCBtYXBwaW5ncykuY29sdW1ucyxcbiAgICBpZ25vcmVkQ29sdW1uczogZ2V0Q29sdW1uSW5mbyhjb2xsZWN0aW9ucywgYWN0aXZlQ29sbGVjdGlvbiwgbWFwcGluZ3MpLmlnbm9yZWRDb2x1bW5zLFxuICAgIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzOiBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncyxcbiAgICBwdWJsaXNoRXJyb3JzOiBhcHBTdGF0ZS5pbXBvcnREYXRhLnB1Ymxpc2hFcnJvcnMsXG4gICAgcHVibGlzaEVuYWJsZWQ6ICFhcHBTdGF0ZS5pbXBvcnREYXRhLnB1Ymxpc2hpbmcgJiYgY29sbGVjdGlvblRhYnMuZXZlcnkodGFiID0+IHRhYi5jb21wbGV0ZSksXG4gICAgcHVibGlzaFN0YXR1czogYXBwU3RhdGUuaW1wb3J0RGF0YS5wdWJsaXNoU3RhdHVzIHx8IFwiUHVibGlzaCBkYXRhc2V0XCIsXG4gICAgY3VzdG9tUHJvcGVydGllczogY3VzdG9tUHJvcGVydGllc1thY3RpdmVDb2xsZWN0aW9uLm5hbWVdIHx8IFtdLFxuICAgIHRhcmdldGFibGVWcmVzOiBnZXRUYXJnZXRhYmxlVnJlcyhteVZyZXMsIHB1YmxpY1ZyZXMsIGFwcFN0YXRlLmltcG9ydERhdGEudnJlKSxcblxuICAgIC8vIGN0cmwtc2hpZnQtRjRcbiAgICBybWxQcmV2aWV3RGF0YTpcbiAgICAgIGFwcFN0YXRlLnByZXZpZXdSbWwuc2hvd1JNTFByZXZpZXcgP1xuICAgICAgICBnZW5lcmF0ZVJtbE1hcHBpbmcoYXBwU3RhdGUuaW1wb3J0RGF0YS52cmUsIGFwcFN0YXRlLm1hcHBpbmdzLmNvbGxlY3Rpb25zLCBhbGxQcmVkaWNhdGVPYmplY3RNYXBwaW5ncylcbiAgICAgICAgOiBudWxsXG4gIH07XG59IiwiaW1wb3J0IHsgdHJhbnNmb3JtQ29sbGVjdGlvblJvd3MsIHRyYW5zZm9ybUNvbGxlY3Rpb25Db2x1bW5zIH0gZnJvbSBcIi4vdHJhbnNmb3JtZXJzL3RhYmxlXCI7XG5cbmV4cG9ydCBkZWZhdWx0IChhcHBTdGF0ZSwgcm91dGVkKSA9PiB7XG4gIGNvbnN0IHsgaW1wb3J0RGF0YTogeyBjb2xsZWN0aW9ucyB9fSA9IGFwcFN0YXRlO1xuICBjb25zdCB7IGFjdGl2ZUNvbGxlY3Rpb24sIG1hcHBpbmdzIH0gPSBhcHBTdGF0ZTtcblxuICByZXR1cm4ge1xuICAgIHZyZUlkOiByb3V0ZWQucGFyYW1zLnZyZUlkLFxuICAgIGNvbGxlY3Rpb25zOiBhcHBTdGF0ZS5pbXBvcnREYXRhLmNvbGxlY3Rpb25zLFxuICAgIHVwbG9hZGVkRmlsZU5hbWU6IGFwcFN0YXRlLmltcG9ydERhdGEudXBsb2FkZWRGaWxlTmFtZSxcbiAgICBhcmNoZXR5cGU6IGFwcFN0YXRlLmFyY2hldHlwZSxcbiAgICBtYXBwaW5nczogYXBwU3RhdGUubWFwcGluZ3MsXG4gICAgc2hvd0ZpbGVJc1VwbG9hZGVkTWVzc2FnZTogYXBwU3RhdGUubWVzc2FnZXMuc2hvd0ZpbGVJc1VwbG9hZGVkTWVzc2FnZSxcbiAgICB2cmU6IGFwcFN0YXRlLmltcG9ydERhdGEudnJlLFxuXG4gICAgLy8gZnJvbSBhY3RpdmUgY29sbGVjdGlvbiBmb3IgdGFibGVcbiAgICBhY3RpdmVDb2xsZWN0aW9uOiBhY3RpdmVDb2xsZWN0aW9uLm5hbWUsXG4gICAgcm93czogdHJhbnNmb3JtQ29sbGVjdGlvblJvd3MoY29sbGVjdGlvbnMsIGFjdGl2ZUNvbGxlY3Rpb24pLFxuICAgIGhlYWRlcnM6IHRyYW5zZm9ybUNvbGxlY3Rpb25Db2x1bW5zKGNvbGxlY3Rpb25zLCBhY3RpdmVDb2xsZWN0aW9uLCBtYXBwaW5ncyksXG4gICAgbmV4dFVybDogYWN0aXZlQ29sbGVjdGlvbi5uZXh0VXJsLFxuXG4gIH07XG59IiwiZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oYXBwU3RhdGUpIHtcbiByZXR1cm4ge1xuICAgdXNlcklkOiBhcHBTdGF0ZS51c2VyZGF0YS51c2VySWQsXG4gICB1cGxvYWRTdGF0dXM6IGFwcFN0YXRlLmltcG9ydERhdGEudXBsb2FkU3RhdHVzXG4gfVxufSIsImV4cG9ydCBkZWZhdWx0IChzdGF0ZSwgcm91dGVkKSA9PiB7XG4gIGNvbnN0IHsgbG9jYXRpb246IHsgcGF0aG5hbWUgfX0gPSByb3V0ZWQ7XG5cbiAgcmV0dXJuIHtcbiAgICB1c2VybmFtZTogc3RhdGUudXNlcmRhdGEudXNlcklkLFxuICAgIHZyZXM6IHN0YXRlLmRhdGFzZXRzLnB1YmxpY1ZyZXMuZmlsdGVyKCh2cmUpID0+IHZyZS5uYW1lICE9PSBcIkFkbWluXCIgJiYgdnJlLm5hbWUgIT09IFwiQmFzZVwiKSxcbiAgICBzZWFyY2hHdWlVcmw6IHN0YXRlLmRhdGFzZXRzLnNlYXJjaEd1aVVybCxcbiAgICBzaG93RGF0YXNldHM6IHBhdGhuYW1lID09PSBcIi9cIiAvKiB8fCBwYXRobmFtZSA9PT0gdXJscy5jb2xsZWN0aW9uc092ZXJ2aWV3KCksKi9cbiAgfVxufSIsImltcG9ydCB7IHByb3BlcnR5TWFwcGluZ0lzQ29tcGxldGUgfSBmcm9tIFwiLi4vLi4vYWNjZXNzb3JzL3Byb3BlcnR5LW1hcHBpbmdzXCJcbmltcG9ydCB7Z2V0Q29sdW1uVmFsdWV9IGZyb20gXCIuLi8uLi9hY2Nlc3NvcnMvcHJvcGVydHktbWFwcGluZ3NcIjtcblxuY29uc3Qgc2hlZXRSb3dGcm9tRGljdFRvQXJyYXkgPSAocm93ZGljdCwgYXJyYXlPZlZhcmlhYmxlTmFtZXMsIG1hcHBpbmdFcnJvcnMpID0+XG4gIGFycmF5T2ZWYXJpYWJsZU5hbWVzLm1hcChuYW1lID0+ICh7XG4gICAgdmFsdWU6IHJvd2RpY3RbbmFtZV0sXG4gICAgZXJyb3I6IG1hcHBpbmdFcnJvcnNbbmFtZV0gfHwgbnVsbFxuICB9KSk7XG5cblxuY29uc3QgZ2V0Q29sdW1uSW5mbyA9IChjb2xsZWN0aW9ucywgYWN0aXZlQ29sbGVjdGlvbiwgbWFwcGluZ3MpID0+IHtcbiAgY29uc3QgY29sbGVjdGlvbkluZm8gPSAoY29sbGVjdGlvbnMgfHwgW10pLmZpbmQoKGNvbGwpID0+IGNvbGwubmFtZSA9PT0gYWN0aXZlQ29sbGVjdGlvbi5uYW1lKTtcbiAgY29uc3QgY29sdW1ucyA9IGNvbGxlY3Rpb25JbmZvID8gY29sbGVjdGlvbkluZm8udmFyaWFibGVzIDogbnVsbDtcbiAgY29uc3QgaWdub3JlZENvbHVtbnMgPSBtYXBwaW5ncyAmJiBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uLm5hbWVdID9cbiAgICBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uLm5hbWVdLmlnbm9yZWRDb2x1bW5zIDogW107XG5cblxuICByZXR1cm4ge2NvbHVtbnM6IGNvbHVtbnMsIGlnbm9yZWRDb2x1bW5zOiBpZ25vcmVkQ29sdW1uc307XG59O1xuXG5jb25zdCB0cmFuc2Zvcm1Db2xsZWN0aW9uUm93cyA9IChjb2xsZWN0aW9ucywgYWN0aXZlQ29sbGVjdGlvbiwgbWFwcGluZ3MpID0+IHtcbiAgY29uc3QgeyBjb2x1bW5zLCBpZ25vcmVkQ29sdW1ucyAgfSA9IGdldENvbHVtbkluZm8oY29sbGVjdGlvbnMsIGFjdGl2ZUNvbGxlY3Rpb24sIG1hcHBpbmdzKTtcbiAgcmV0dXJuIGFjdGl2ZUNvbGxlY3Rpb24ubmFtZSAmJiBjb2x1bW5zXG4gICAgPyBhY3RpdmVDb2xsZWN0aW9uLnJvd3NcbiAgICAubWFwKChyb3cpID0+XG4gICAgICBzaGVldFJvd0Zyb21EaWN0VG9BcnJheShyb3cudmFsdWVzLCBjb2x1bW5zLCByb3cuZXJyb3JzKVxuICAgICAgICAubWFwKChjZWxsLCBjb2xJZHgpID0+ICh7XG4gICAgICAgICAgLi4uY2VsbCwgaWdub3JlZDogaWdub3JlZENvbHVtbnMuaW5kZXhPZihjb2x1bW5zW2NvbElkeF0pID4gLTFcbiAgICAgICAgfSkpXG4gICAgKVxuICAgIDogW107XG59O1xuXG5jb25zdCB0cmFuc2Zvcm1Db2xsZWN0aW9uQ29sdW1ucyA9IChjb2xsZWN0aW9ucywgYWN0aXZlQ29sbGVjdGlvbiwgbWFwcGluZ3MsIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzID0gW10pID0+IHtcbiAgY29uc3QgeyBjb2x1bW5zLCBpZ25vcmVkQ29sdW1ucyAgfSA9IGdldENvbHVtbkluZm8oY29sbGVjdGlvbnMsIGFjdGl2ZUNvbGxlY3Rpb24sIG1hcHBpbmdzKTtcbiAgcmV0dXJuIChjb2x1bW5zIHx8IFtdKS5tYXAoKGNvbHVtbiwgaSkgPT4gKHtcbiAgICBuYW1lOiBjb2x1bW4sXG4gICAgaXNDb25maXJtZWQ6IHByb3BlcnR5TWFwcGluZ0lzQ29tcGxldGUocHJlZGljYXRlT2JqZWN0TWFwcGluZ3MuZmluZCgocG9tKSA9PiBnZXRDb2x1bW5WYWx1ZShwb20pID09PSBjb2x1bW4pKSxcbiAgICBpc0lnbm9yZWQ6IGlnbm9yZWRDb2x1bW5zLmluZGV4T2YoY29sdW1uKSA+IC0xXG4gIH0pKTtcbn07XG5cbmV4cG9ydCB7XG4gIHRyYW5zZm9ybUNvbGxlY3Rpb25Db2x1bW5zLFxuICB0cmFuc2Zvcm1Db2xsZWN0aW9uUm93cyxcbiAgZ2V0Q29sdW1uSW5mb1xufSIsImltcG9ydCB7cHJvcGVydHlNYXBwaW5nSXNDb21wbGV0ZX0gZnJvbSBcIi4uLy4uL2FjY2Vzc29ycy9wcm9wZXJ0eS1tYXBwaW5nc1wiO1xuaW1wb3J0IHt1bmlxfSBmcm9tIFwiLi4vLi4vdXRpbC91bmlxXCI7XG5pbXBvcnQge2dldENvbHVtblZhbHVlfSBmcm9tIFwiLi4vLi4vYWNjZXNzb3JzL3Byb3BlcnR5LW1hcHBpbmdzXCI7XG5cbmNvbnN0IG1hcHBpbmdzQXJlQ29tcGxldGUgPSAoYWxsQ29sdW1ucywgaWdub3JlZENvbHVtbnMsIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzKSA9PiB7XG4gIGNvbnN0IGV2YWx1YXRlQ29sdW1ucyA9IGFsbENvbHVtbnMuZmlsdGVyKChjb2wpID0+IGlnbm9yZWRDb2x1bW5zLmluZGV4T2YoY29sKSA8IDApO1xuICBjb25zdCBtYXBwZWRDb2x1bW5zID0gcHJlZGljYXRlT2JqZWN0TWFwcGluZ3NcbiAgICAuZmlsdGVyKChwb20pID0+IHByb3BlcnR5TWFwcGluZ0lzQ29tcGxldGUocG9tKSlcbiAgICAubWFwKChwb20pID0+IGdldENvbHVtblZhbHVlKHBvbSkpXG4gICAgLnJlZHVjZSh1bmlxLCBbXSk7XG5cbiAgcmV0dXJuIGV2YWx1YXRlQ29sdW1ucy5sZW5ndGggPD0gbWFwcGVkQ29sdW1ucy5sZW5ndGg7XG59O1xuXG5jb25zdCB0cmFuc2Zvcm1Db2xsZWN0aW9uVGFicyA9IChjb2xsZWN0aW9ucywgbWFwcGluZ3MsIGFjdGl2ZUNvbGxlY3Rpb24sIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzKSA9PlxuICAoY29sbGVjdGlvbnMgfHwgW10pXG4gICAgLmZpbHRlcigoY29sbGVjdGlvbikgPT4gdHlwZW9mIG1hcHBpbmdzLmNvbGxlY3Rpb25zW2NvbGxlY3Rpb24ubmFtZV0gIT09IFwidW5kZWZpbmVkXCIpXG4gICAgLm1hcCgoY29sbGVjdGlvbikgPT4gKHtcbiAgICAgIGNvbGxlY3Rpb25OYW1lOiBjb2xsZWN0aW9uLm5hbWUsXG4gICAgICBhcmNoZXR5cGVOYW1lOiBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uLm5hbWVdLmFyY2hldHlwZU5hbWUsXG4gICAgICBhY3RpdmU6IGFjdGl2ZUNvbGxlY3Rpb24ubmFtZSA9PT0gY29sbGVjdGlvbi5uYW1lLFxuICAgICAgY29tcGxldGU6IG1hcHBpbmdzQXJlQ29tcGxldGUoXG4gICAgICAgIGNvbGxlY3Rpb24udmFyaWFibGVzLFxuICAgICAgICBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uLm5hbWVdLmlnbm9yZWRDb2x1bW5zLFxuICAgICAgICBwcmVkaWNhdGVPYmplY3RNYXBwaW5nc1tjb2xsZWN0aW9uLm5hbWVdIHx8IFtdXG4gICAgICApXG4gICAgfSkpO1xuXG5leHBvcnQgeyB0cmFuc2Zvcm1Db2xsZWN0aW9uVGFicyB9IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5pbXBvcnQgcm91dGVyIGZyb20gXCIuL3JvdXRlclwiO1xuaW1wb3J0IGdldFRva2VuIGZyb20gXCIuL3Rva2VuXCJcbmltcG9ydCB7ZmV0Y2hNeVZyZXN9IGZyb20gXCIuL2FjdGlvbnMvZmV0Y2gtbXktdnJlc1wiO1xuXG54aHIuZ2V0KHByb2Nlc3MuZW52LnNlcnZlciArIFwiL3YyLjEvamF2YXNjcmlwdC1nbG9iYWxzXCIsIChlcnIsIHJlcykgPT4ge1xuICB2YXIgZ2xvYmFscyA9IEpTT04ucGFyc2UocmVzLmJvZHkpO1xuICBzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTRVRfU0VBUkNIX1VSTFwiLCBkYXRhOiBnbG9iYWxzLmVudi5USU1CVUNUT09fU0VBUkNIX1VSTH0pO1xufSk7XG5cbnhoci5nZXQocHJvY2Vzcy5lbnYuc2VydmVyICsgXCIvdjIuMS9zeXN0ZW0vdnJlc1wiLCAoZXJyLCByZXNwLCBib2R5KSA9PiB7XG4gIHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QVUJMSUNfVlJFU1wiLCBwYXlsb2FkOiBKU09OLnBhcnNlKGJvZHkpfSk7XG59KTtcblxuY29uc3QgaW5pdGlhbFJlbmRlciA9ICgpID0+IFJlYWN0RE9NLnJlbmRlcihyb3V0ZXIsIGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKFwiYXBwXCIpKTtcblxuZG9jdW1lbnQuYWRkRXZlbnRMaXN0ZW5lcihcIkRPTUNvbnRlbnRMb2FkZWRcIiwgKCkgPT4ge1xuXG4gIHhocihwcm9jZXNzLmVudi5zZXJ2ZXIgKyBcIi92Mi4xL21ldGFkYXRhL0FkbWluXCIsIChlcnIsIHJlc3ApID0+IHtcblxuICAgIHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNFVF9BUkNIRVRZUEVfTUVUQURBVEFcIiwgZGF0YTogSlNPTi5wYXJzZShyZXNwLmJvZHkpfSk7XG4gICAgY29uc3QgdG9rZW4gPSBnZXRUb2tlbigpO1xuICAgIGlmICh0b2tlbikge1xuICAgICAgc3RvcmUuZGlzcGF0Y2goZmV0Y2hNeVZyZXModG9rZW4sICgpID0+IGluaXRpYWxSZW5kZXIoKSkpO1xuICAgIH0gZWxzZSB7XG4gICAgICBpbml0aWFsUmVuZGVyKCk7XG4gICAgfVxuICB9KTtcbn0pO1xuXG5sZXQgY29tYm9NYXAgPSB7XG4gIGN0cmw6IGZhbHNlLFxuICBzaGlmdDogZmFsc2UsXG4gIGY0OiBmYWxzZVxufTtcblxuY29uc3Qga2V5TWFwID0ge1xuICAxNzogXCJjdHJsXCIsXG4gIDE2OiBcInNoaWZ0XCIsXG4gIDExNTogXCJmNFwiXG59O1xuXG5kb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwia2V5ZG93blwiLCAoZXYpID0+IHtcbiAgaWYgKGtleU1hcFtldi5rZXlDb2RlXSkge1xuICAgIGNvbWJvTWFwW2tleU1hcFtldi5rZXlDb2RlXV0gPSB0cnVlO1xuICB9XG5cbiAgaWYgKE9iamVjdC5rZXlzKGNvbWJvTWFwKS5tYXAoayA9PiBjb21ib01hcFtrXSkuZmlsdGVyKGlzUHJlc3NlZCA9PiBpc1ByZXNzZWQpLmxlbmd0aCA9PT0gMykge1xuICAgIHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlBSRVZJRVdfUk1MXCJ9KTtcbiAgfVxuXG4gIGlmIChldi5rZXlDb2RlID09PSAyNykge1xuICAgIHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkhJREVfUk1MX1BSRVZJRVdcIn0pO1xuICB9XG59KTtcblxuZG9jdW1lbnQuYWRkRXZlbnRMaXN0ZW5lcihcImtleXVwXCIsIChldikgPT4ge1xuICBpZiAoa2V5TWFwW2V2LmtleUNvZGVdKSB7XG4gICAgY29tYm9NYXBba2V5TWFwW2V2LmtleUNvZGVdXSA9IGZhbHNlO1xuICB9XG59KTsiLCJjb25zdCBpbml0aWFsU3RhdGUgPSB7XG4gIG5hbWU6IG51bGwsXG4gIG5leHRVcmw6IG51bGwsXG4gIHJvd3M6IFtdLFxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcbiAgc3dpdGNoIChhY3Rpb24udHlwZSkge1xuICAgIGNhc2UgXCJGSU5JU0hfVVBMT0FEXCI6XG4gICAgY2FzZSBcIlBVQkxJU0hfU1RBUlRcIjpcbiAgICAgIHJldHVybiB7Li4uaW5pdGlhbFN0YXRlfTtcbiAgICBjYXNlIFwiUkVDRUlWRV9BQ1RJVkVfQ09MTEVDVElPTlwiOlxuICAgICAgcmV0dXJuIHtcbiAgICAgICAgLi4uc3RhdGUsXG4gICAgICAgIG5hbWU6IGFjdGlvbi5kYXRhLm5hbWUsXG4gICAgICAgIG5leHRVcmw6IGFjdGlvbi5kYXRhLl9uZXh0LFxuICAgICAgICByb3dzOiBhY3Rpb24uZGF0YS5uYW1lICE9PSBzdGF0ZS5uYW1lXG4gICAgICAgICAgPyBhY3Rpb24uZGF0YS5pdGVtc1xuICAgICAgICAgIDogc3RhdGUucm93cy5jb25jYXQoYWN0aW9uLmRhdGEuaXRlbXMpXG4gICAgICB9O1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufSIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHt9O1xuXG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX0FSQ0hFVFlQRV9NRVRBREFUQVwiOlxuXHRcdFx0cmV0dXJuIGFjdGlvbi5kYXRhO1xuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufSIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHsgfTtcblxuY29uc3QgYWRkQ3VzdG9tUHJvcGVydHkgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuICBjb25zdCBjb2xsZWN0aW9uQ3VzdG9tUHJvcGVydGllcyA9IHN0YXRlW2FjdGlvbi5jb2xsZWN0aW9uXSB8fCBbXTtcblxuICBjb25zdCBjdXN0b21Qcm9wZXJ0eSA9IHtcbiAgICBwcm9wZXJ0eVR5cGU6IGFjdGlvbi5wcm9wZXJ0eVR5cGUsXG4gICAgcHJvcGVydHlOYW1lOiBhY3Rpb24ucHJvcGVydHlOYW1lLFxuICB9O1xuXG4gIHJldHVybiB7XG4gICAgLi4uc3RhdGUsXG4gICAgW2FjdGlvbi5jb2xsZWN0aW9uXTogY29sbGVjdGlvbkN1c3RvbVByb3BlcnRpZXMuY29uY2F0KGN1c3RvbVByb3BlcnR5KVxuICB9O1xufTtcblxuY29uc3QgcmVtb3ZlQ3VzdG9tUHJvcGVydHkgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuICBjb25zdCBjb2xsZWN0aW9uQ3VzdG9tUHJvcGVydGllcyA9IHN0YXRlW2FjdGlvbi5jb2xsZWN0aW9uXSB8fCBbXTtcblxuICByZXR1cm4ge1xuICAgIC4uLnN0YXRlLFxuICAgIFthY3Rpb24uY29sbGVjdGlvbl06IGNvbGxlY3Rpb25DdXN0b21Qcm9wZXJ0aWVzLmZpbHRlcigocHJvcCwgaWR4KSA9PiBpZHggIT09IGFjdGlvbi5pbmRleClcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcbiAgc3dpdGNoIChhY3Rpb24udHlwZSkge1xuICAgIGNhc2UgXCJGSU5JU0hfVVBMT0FEXCI6XG4gICAgY2FzZSBcIkxPR0lOXCI6XG4gICAgICByZXR1cm4gaW5pdGlhbFN0YXRlO1xuICAgIGNhc2UgXCJBRERfQ1VTVE9NX1BST1BFUlRZXCI6XG4gICAgICByZXR1cm4gYWRkQ3VzdG9tUHJvcGVydHkoc3RhdGUsIGFjdGlvbik7XG4gICAgY2FzZSBcIlJFTU9WRV9DVVNUT01fUFJPUEVSVFlcIjpcbiAgICAgIHJldHVybiByZW1vdmVDdXN0b21Qcm9wZXJ0eShzdGF0ZSwgYWN0aW9uKTtcbiAgfVxuXG4gIHJldHVybiBzdGF0ZTtcbn0iLCJjb25zdCBpbml0aWFsU3RhdGUgPSB7XG4gIHNlYXJjaEd1aVVybDogdW5kZWZpbmVkLFxuICBwdWJsaWNWcmVzOiBbXVxufTtcblxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuICBzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG4gICAgY2FzZSBcIlNFVF9TRUFSQ0hfVVJMXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICAuLi5zdGF0ZSxcbiAgICAgICAgc2VhcmNoR3VpVXJsOiBhY3Rpb24uZGF0YVxuICAgICAgfTtcbiAgICBjYXNlIFwiU0VUX1BVQkxJQ19WUkVTXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICAuLi5zdGF0ZSxcbiAgICAgICAgcHVibGljVnJlczogYWN0aW9uLnBheWxvYWRcbiAgICAgIH1cbiAgfVxuXG4gIHJldHVybiBzdGF0ZTtcbn0iLCJjb25zdCBpbml0aWFsU3RhdGUgPSB7XG4gIGlzVXBsb2FkaW5nOiBmYWxzZSxcbiAgcHVibGlzaGluZzogZmFsc2UsXG4gIHB1Ymxpc2hFbmFibGVkOiB0cnVlLFxuICBwdWJsaXNoU3RhdHVzOiB1bmRlZmluZWQsXG4gIHB1Ymxpc2hFcnJvckNvdW50OiAwLFxuICB0cmlwbGVDb3VudDogMFxufTtcblxuXG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG4gIHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcbiAgICBjYXNlIFwiU1RBUlRfVVBMT0FEXCI6XG4gICAgICByZXR1cm4gey4uLmluaXRpYWxTdGF0ZSwgdXBsb2FkU3RhdHVzOiBcInRyYW5zZmVyaW5nIGZpbGVcIn07XG4gICAgY2FzZSBcIlVQTE9BRF9TVEFUVVNfVVBEQVRFXCI6XG4gICAgICBpZiAoYWN0aW9uLmRhdGEpIHtcbiAgICAgICAgdmFyIGZhaWx1cmVzID0gc3RhdGUuZmFpbHVyZXMgfHwgMDtcbiAgICAgICAgdmFyIGN1cnJlbnRTaGVldCA9IHN0YXRlLmN1cnJlbnRTaGVldCB8fCBcIlwiO1xuICAgICAgICB2YXIgcm93cyA9IHN0YXRlLnJvd3MgfHwgMDtcbiAgICAgICAgdmFyIHByZXZSb3dzID0gc3RhdGUucHJldlJvd3MgfHwgMDtcbiAgICAgICAgaWYgKGFjdGlvbi5kYXRhLnN1YnN0cigwLCBcImZhaWx1cmU6IFwiLmxlbmd0aCkgPT09IFwiZmFpbHVyZTogXCIpIHtcbiAgICAgICAgICBmYWlsdXJlcyArPSAxO1xuICAgICAgICB9IGVsc2UgaWYgKGFjdGlvbi5kYXRhLnN1YnN0cigwLCBcInNoZWV0OiBcIi5sZW5ndGgpID09PSBcInNoZWV0OiBcIikge1xuICAgICAgICAgIGN1cnJlbnRTaGVldCA9IGFjdGlvbi5kYXRhLnN1YnN0cihcInNoZWV0OiBcIi5sZW5ndGgpO1xuICAgICAgICAgIHByZXZSb3dzID0gcm93cztcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICByb3dzID0gYWN0aW9uLmRhdGEqMSAtIHByZXZSb3dzO1xuICAgICAgICB9XG4gICAgICAgIHZhciB1cGxvYWRTdGF0dXMgPSBcInByb2Nlc3NpbmcgXCIgKyBjdXJyZW50U2hlZXQgKyBcIiAocm93IFwiICsgcm93cyArIChmYWlsdXJlcyA+IDAgPyBcIiwgXCIgKyBmYWlsdXJlcyArIFwiIGZhaWx1cmVzXCIgOiBcIlwiKSArIFwiKVwiO1xuICAgICAgICByZXR1cm4gey4uLnN0YXRlLFxuICAgICAgICAgIGZhaWx1cmVzLFxuICAgICAgICAgIHJvd3MsXG4gICAgICAgICAgY3VycmVudFNoZWV0LFxuICAgICAgICAgIHVwbG9hZFN0YXR1czogdXBsb2FkU3RhdHVzXG4gICAgICAgIH07XG4gICAgICB9XG4gICAgICByZXR1cm4gc3RhdGU7XG4gICAgY2FzZSBcIkZJTklTSF9VUExPQURcIjpcbiAgICAgIHJldHVybiB7Li4uc3RhdGUsXG4gICAgICAgIHVwbG9hZFN0YXR1czogdW5kZWZpbmVkLFxuICAgICAgICBmYWlsdXJlczogMCxcbiAgICAgICAgY3VycmVudFNoZWV0OiBcIlwiLFxuICAgICAgICByb3dzOiB1bmRlZmluZWQsXG4gICAgICAgIHB1Ymxpc2hFcnJvcnM6IGZhbHNlLFxuICAgICAgICB1cGxvYWRlZEZpbGVOYW1lOiBhY3Rpb24udXBsb2FkZWRGaWxlTmFtZSxcbiAgICAgICAgdnJlOiBhY3Rpb24uZGF0YS52cmUsXG4gICAgICAgIGV4ZWN1dGVNYXBwaW5nVXJsOiBhY3Rpb24uZGF0YS5leGVjdXRlTWFwcGluZyxcbiAgICAgICAgY29sbGVjdGlvbnM6IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zLm1hcCgoY29sKSA9PiAoe1xuICAgICAgICAgIC4uLmNvbCxcbiAgICAgICAgICBkYXRhVXJsOiBjb2wuZGF0YSxcbiAgICAgICAgICBkYXRhVXJsV2l0aEVycm9yczogY29sLmRhdGFXaXRoRXJyb3JzXG4gICAgICAgIH0pKVxuICAgICAgfTtcblxuICAgIGNhc2UgXCJQVUJMSVNIX1NUQVJUXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICAuLi5zdGF0ZSxcbiAgICAgICAgcHVibGlzaGluZzogdHJ1ZVxuICAgICAgfTtcblxuICAgIGNhc2UgXCJQVUJMSVNIX1NUQVRVU19VUERBVEVcIjpcbiAgICAgIHZhciBwdWJsaXNoRXJyb3JDb3VudCA9IHN0YXRlLnB1Ymxpc2hFcnJvckNvdW50ICsgKGFjdGlvbi5kYXRhID09PSBcIkZcIiA/IDEgOiAwKTtcbiAgICAgIHZhciB0cmlwbGVDb3VudCA9IGFjdGlvbi5kYXRhID09PSBcIkZcIiA/IHN0YXRlLnRyaXBsZUNvdW50IDogYWN0aW9uLmRhdGE7XG4gICAgICB2YXIgcHVibGlzaFN0YXR1cyA9IFwiUHVibGlzaGluZyBcIiArIHRyaXBsZUNvdW50ICsgXCIgdHJpcGxlc1wiICsgKHB1Ymxpc2hFcnJvckNvdW50ID4gMCA/IFwiIChcIiArIHB1Ymxpc2hFcnJvckNvdW50ICsgXCIgZXJyb3JzKVwiIDogXCJcIik7XG4gICAgICByZXR1cm4ge1xuICAgICAgICAuLi5zdGF0ZSxcbiAgICAgICAgcHVibGlzaEVycm9yQ291bnQsXG4gICAgICAgIHRyaXBsZUNvdW50LFxuICAgICAgICBwdWJsaXNoU3RhdHVzXG4gICAgICB9O1xuICAgIGNhc2UgXCJQVUJMSVNIX0hBRF9FUlJPUlwiOlxuICAgICAgLy8gY2xlYXIgdGhlIHNoZWV0cyB0byBmb3JjZSByZWxvYWRcbiAgICAgIHJldHVybiB7XG4gICAgICAgIC4uLnN0YXRlLFxuICAgICAgICBwdWJsaXNoRXJyb3JzOiB0cnVlLFxuICAgICAgICBjb2xsZWN0aW9uczogc3RhdGUuY29sbGVjdGlvbnMubWFwKChjb2wpID0+ICh7XG4gICAgICAgICAgLi4uY29sLFxuICAgICAgICAgIGRhdGFVcmw6IGNvbC5kYXRhLFxuICAgICAgICAgIGRhdGFVcmxXaXRoRXJyb3JzOiBjb2wuZGF0YVdpdGhFcnJvcnNcbiAgICAgICAgfSkpXG4gICAgICB9O1xuICAgIGNhc2UgXCJQVUJMSVNIX1NVQ0NFRURFRFwiOlxuICAgICAgLy8gY2xlYXIgdGhlIHNoZWV0cyB0byBmb3JjZSByZWxvYWRcbiAgICAgIHJldHVybiB7XG4gICAgICAgIC4uLnN0YXRlLFxuICAgICAgICBwdWJsaXNoU3RhdHVzOiB1bmRlZmluZWQsXG4gICAgICAgIHB1Ymxpc2hFbmFibGVkOiB0cnVlLFxuICAgICAgICBwdWJsaXNoRXJyb3JzOiBmYWxzZSxcbiAgICAgICAgY29sbGVjdGlvbnM6IHN0YXRlLmNvbGxlY3Rpb25zLm1hcCgoY29sKSA9PiAoe1xuICAgICAgICAgIC4uLmNvbCxcbiAgICAgICAgICBkYXRhVXJsOiBjb2wuZGF0YSxcbiAgICAgICAgICBkYXRhVXJsV2l0aEVycm9yczogY29sLmRhdGFXaXRoRXJyb3JzXG4gICAgICAgIH0pKVxuICAgICAgfTtcbiAgICBjYXNlIFwiUFVCTElTSF9GSU5JU0hFRFwiOlxuICAgICAgLy8gY2xlYXIgdGhlIHNoZWV0cyB0byBmb3JjZSByZWxvYWRcbiAgICAgIHJldHVybiB7XG4gICAgICAgIC4uLnN0YXRlLFxuICAgICAgICBwdWJsaXNoU3RhdHVzOiB1bmRlZmluZWQsXG4gICAgICAgIHB1Ymxpc2hFbmFibGVkOiB0cnVlLFxuICAgICAgICBwdWJsaXNoRXJyb3JDb3VudDogMCxcbiAgICAgICAgdHJpcGxlQ291bnQ6IDAsXG4gICAgICAgIHB1Ymxpc2hpbmc6IGZhbHNlXG4gICAgICB9O1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufVxuIiwiaW1wb3J0IHtjb21iaW5lUmVkdWNlcnN9IGZyb20gXCJyZWR1eFwiO1xuXG5pbXBvcnQgbWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXNcIjtcbmltcG9ydCBkYXRhc2V0cyBmcm9tIFwiLi9kYXRhc2V0c1wiO1xuaW1wb3J0IHVzZXJkYXRhIGZyb20gXCIuL3VzZXJkYXRhXCI7XG5pbXBvcnQgaW1wb3J0RGF0YSBmcm9tIFwiLi9pbXBvcnQtZGF0YVwiO1xuaW1wb3J0IGFyY2hldHlwZSBmcm9tIFwiLi9hcmNoZXR5cGVcIjtcbmltcG9ydCBtYXBwaW5ncyBmcm9tIFwiLi9tYXBwaW5nc1wiO1xuaW1wb3J0IGFjdGl2ZUNvbGxlY3Rpb24gZnJvbSBcIi4vYWN0aXZlLWNvbGxlY3Rpb25cIjtcbmltcG9ydCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncyBmcm9tIFwiLi9wcmVkaWNhdGUtb2JqZWN0LW1hcHBpbmdzXCI7XG5pbXBvcnQgY3VzdG9tUHJvcGVydGllcyBmcm9tIFwiLi9jdXN0b20tcHJvcGVydGllc1wiO1xuaW1wb3J0IHByZXZpZXdSbWwgZnJvbSBcIi4vcHJldmlldy1ybWxcIjtcblxuZXhwb3J0IGRlZmF1bHQgY29tYmluZVJlZHVjZXJzKHtcbiAgbWVzc2FnZXM6IG1lc3NhZ2VzLFxuICBkYXRhc2V0czogZGF0YXNldHMsXG4gIHVzZXJkYXRhOiB1c2VyZGF0YSxcbiAgaW1wb3J0RGF0YTogaW1wb3J0RGF0YSxcbiAgYXJjaGV0eXBlOiBhcmNoZXR5cGUsXG4gIG1hcHBpbmdzOiBtYXBwaW5ncyxcbiAgYWN0aXZlQ29sbGVjdGlvbjogIGFjdGl2ZUNvbGxlY3Rpb24sXG4gIHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzOiBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncyxcbiAgY3VzdG9tUHJvcGVydGllczogY3VzdG9tUHJvcGVydGllcyxcbiAgcHJldmlld1JtbDogcHJldmlld1JtbFxufSk7XG4iLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5pbXBvcnQgZ2V0SW4gZnJvbSBcIi4uL3V0aWwvZ2V0LWluXCI7XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgY29sbGVjdGlvbnM6IHt9LFxuICBjb25maXJtZWQ6IGZhbHNlLFxuICBwdWJsaXNoaW5nOiBmYWxzZVxufTtcblxuZnVuY3Rpb24gc2NhZmZvbGRDb2xsZWN0aW9uTWFwcGluZ3MoaW5pdCwgc2hlZXQpIHtcbiAgcmV0dXJuIE9iamVjdC5hc3NpZ24oaW5pdCwge1xuICAgIFtzaGVldC5uYW1lXToge1xuICAgICAgYXJjaGV0eXBlTmFtZTogbnVsbCxcbiAgICAgIGlnbm9yZWRDb2x1bW5zOiBbXVxuICAgIH1cbiAgfSk7XG59XG5cbmNvbnN0IG1hcENvbGxlY3Rpb25BcmNoZXR5cGUgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuICBjb25zdCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJhcmNoZXR5cGVOYW1lXCJdLCBhY3Rpb24udmFsdWUsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuICByZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuY29uc3QgdG9nZ2xlSWdub3JlZENvbHVtbiA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG4gIGNvbnN0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuICBpZiAoY3VycmVudC5pbmRleE9mKGFjdGlvbi52YXJpYWJsZU5hbWUpIDwgMCkge1xuICAgIHJldHVybiB7XG4gICAgICAuLi5zdGF0ZSxcbiAgICAgIGNvbGxlY3Rpb25zOiBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIGN1cnJlbnQuY29uY2F0KGFjdGlvbi52YXJpYWJsZU5hbWUpLCBzdGF0ZS5jb2xsZWN0aW9ucylcbiAgICB9O1xuICB9IGVsc2Uge1xuICAgIHJldHVybiB7XG4gICAgICAuLi5zdGF0ZSxcbiAgICAgIGNvbGxlY3Rpb25zOiBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIGN1cnJlbnQuZmlsdGVyKChjKSA9PiBjICE9PSBhY3Rpb24udmFyaWFibGVOYW1lKSwgc3RhdGUuY29sbGVjdGlvbnMpXG4gICAgfTtcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcbiAgc3dpdGNoIChhY3Rpb24udHlwZSkge1xuICAgIGNhc2UgXCJTVEFSVF9VUExPQURcIjpcbiAgICAgIHJldHVybiBpbml0aWFsU3RhdGU7XG5cbiAgICBjYXNlIFwiRklOSVNIX1VQTE9BRFwiOlxuICAgICAgcmV0dXJuIHtcbiAgICAgICAgLi4uc3RhdGUsXG4gICAgICAgIGNvbGxlY3Rpb25zOiBhY3Rpb24uZGF0YS5jb2xsZWN0aW9ucy5yZWR1Y2Uoc2NhZmZvbGRDb2xsZWN0aW9uTWFwcGluZ3MsIHt9KVxuICAgICAgfTtcblxuICAgIGNhc2UgXCJNQVBfQ09MTEVDVElPTl9BUkNIRVRZUEVcIjpcbiAgICAgIHJldHVybiBtYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHN0YXRlLCBhY3Rpb24pO1xuXG4gICAgY2FzZSBcIk1BUF9DT0xMRUNUSU9OX0FSQ0hFVFlQRVNcIjpcbiAgICAgIHJldHVybiB7XG4gICAgICAgIC4uLnN0YXRlLFxuICAgICAgICBjb2xsZWN0aW9uczogYWN0aW9uLmRhdGFcbiAgICAgIH07XG5cbiAgICBjYXNlIFwiVE9HR0xFX0lHTk9SRURfQ09MVU1OXCI6XG4gICAgICByZXR1cm4gdG9nZ2xlSWdub3JlZENvbHVtbihzdGF0ZSwgYWN0aW9uKTtcblxuICB9XG4gIHJldHVybiBzdGF0ZTtcbn1cbiIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgc2hvd0ZpbGVJc1VwbG9hZGVkTWVzc2FnZTogdHJ1ZSxcbiAgc2hvd0NvbGxlY3Rpb25zQXJlQ29ubmVjdGVkTWVzc2FnZTogdHJ1ZVxufTtcblxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuICBzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG4gICAgY2FzZSBcIlRPR0dMRV9NRVNTQUdFXCI6XG4gICAgICBjb25zdCBuZXdTdGF0ZSA9IHsuLi5zdGF0ZX07XG4gICAgICBuZXdTdGF0ZVthY3Rpb24ubWVzc2FnZUlkXSA9ICFzdGF0ZVthY3Rpb24ubWVzc2FnZUlkXVxuICAgICAgcmV0dXJuIG5ld1N0YXRlO1xuICAgIGNhc2UgXCJGSU5JU0hfVVBMT0FEXCI6XG4gICAgICByZXR1cm4gaW5pdGlhbFN0YXRlO1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufSIsImltcG9ydCB7Z2V0Q29sdW1uVmFsdWV9IGZyb20gXCIuLi9hY2Nlc3NvcnMvcHJvcGVydHktbWFwcGluZ3NcIjtcbmNvbnN0IGluaXRpYWxTdGF0ZSA9IHsgfTtcblxuZnVuY3Rpb24gc2V0QmFzaWNQcmVkaWNhdGVPYmplY3RNYXAoYWN0aW9uLCBjb2xsZWN0aW9uUHJlZGljYXRlT2JqZWN0TWFwcGluZ3MpIHtcbiAgY29uc3QgcHJlZGljYXRlT2JqZWN0TWFwID0ge1xuICAgIHByZWRpY2F0ZTogYWN0aW9uLnByZWRpY2F0ZSxcbiAgICBvYmplY3RNYXA6IHtcbiAgICAgIGNvbHVtbjogYWN0aW9uLm9iamVjdFxuICAgIH0sXG4gICAgcHJvcGVydHlUeXBlOiBhY3Rpb24ucHJvcGVydHlUeXBlXG4gIH07XG5cbiAgcmV0dXJuIGNvbGxlY3Rpb25QcmVkaWNhdGVPYmplY3RNYXBwaW5nc1xuICAgIC5maWx0ZXIoKHByZWRPYmpNYXApID0+IHByZWRPYmpNYXAucHJlZGljYXRlICE9PSBhY3Rpb24ucHJlZGljYXRlKVxuICAgIC5jb25jYXQocHJlZGljYXRlT2JqZWN0TWFwKTtcbn1cblxuXG5mdW5jdGlvbiBzZXRSZWxhdGlvblByZWRpY2F0ZU9iamVjdE1hcChhY3Rpb24sIGNvbGxlY3Rpb25QcmVkaWNhdGVPYmplY3RNYXBwaW5ncykge1xuICBjb25zdCBwcmVkaWNhdGVPYmplY3RNYXAgPSB7XG4gICAgcHJlZGljYXRlOiBhY3Rpb24ucHJlZGljYXRlLFxuICAgIG9iamVjdE1hcDogYWN0aW9uLm9iamVjdCxcbiAgICBwcm9wZXJ0eVR5cGU6IGFjdGlvbi5wcm9wZXJ0eVR5cGUsXG4gICAgZGF0YXNldDogYWN0aW9uLmRhdGFzZXRcbiAgfTtcblxuICByZXR1cm4gY29sbGVjdGlvblByZWRpY2F0ZU9iamVjdE1hcHBpbmdzXG4gICAgLmZpbHRlcigocHJlZE9iak1hcCkgPT4gcHJlZE9iak1hcC5wcmVkaWNhdGUgIT09IGFjdGlvbi5wcmVkaWNhdGUpXG4gICAgLmNvbmNhdChwcmVkaWNhdGVPYmplY3RNYXApO1xufVxuXG5cbmNvbnN0IHNldFByZWRpY2F0ZU9iamVjdE1hcHBpbmcgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuICBjb25zdCBjb2xsZWN0aW9uUHJlZGljYXRlT2JqZWN0TWFwcGluZ3MgPSBzdGF0ZVthY3Rpb24uc3ViamVjdENvbGxlY3Rpb25dIHx8IFtdO1xuICBjb25zdCBuZXdDb2xsZWN0aW9uUHJlZGljYXRlT2JqZWN0TWFwcGluZ3MgPVxuICAgIGFjdGlvbi5wcm9wZXJ0eVR5cGUgPT09IFwicmVsYXRpb25cIlxuICAgICAgPyBzZXRSZWxhdGlvblByZWRpY2F0ZU9iamVjdE1hcChhY3Rpb24sIGNvbGxlY3Rpb25QcmVkaWNhdGVPYmplY3RNYXBwaW5ncylcbiAgICAgIDogc2V0QmFzaWNQcmVkaWNhdGVPYmplY3RNYXAoYWN0aW9uLCBjb2xsZWN0aW9uUHJlZGljYXRlT2JqZWN0TWFwcGluZ3MpO1xuXG4gIHJldHVybiB7XG4gICAgLi4uc3RhdGUsXG4gICAgW2FjdGlvbi5zdWJqZWN0Q29sbGVjdGlvbl06IG5ld0NvbGxlY3Rpb25QcmVkaWNhdGVPYmplY3RNYXBwaW5nc1xuICB9O1xufTtcblxuY29uc3QgcmVtb3ZlUHJlZGljYXRlT2JqZWN0TWFwcGluZyA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG4gIGNvbnN0IGNvbGxlY3Rpb25QcmVkaWNhdGVPYmplY3RNYXBwaW5ncyA9IHN0YXRlW2FjdGlvbi5zdWJqZWN0Q29sbGVjdGlvbl0gfHwgW107XG5cbiAgcmV0dXJuIGFjdGlvbi5wcmVkaWNhdGUgPT09IFwibmFtZXNcIiA/ICB7XG4gICAgLi4uc3RhdGUsXG4gICAgW2FjdGlvbi5zdWJqZWN0Q29sbGVjdGlvbl06IGNvbGxlY3Rpb25QcmVkaWNhdGVPYmplY3RNYXBwaW5nc1xuICAgICAgLmZpbHRlcigocG9tKSA9PiAhKHBvbS5wcm9wZXJ0eVR5cGUgPT09IFwibmFtZXNcIiAmJiBbXCJmb3JlbmFtZVwiLCBcInN1cm5hbWVcIiwgXCJuYW1lTGlua1wiLCBcImdlbk5hbWVcIiwgXCJyb2xlTmFtZVwiXS5pbmRleE9mKHBvbS5wcmVkaWNhdGUpID4gLTEpKVxuICB9IDoge1xuICAgIC4uLnN0YXRlLFxuICAgIFthY3Rpb24uc3ViamVjdENvbGxlY3Rpb25dOiBjb2xsZWN0aW9uUHJlZGljYXRlT2JqZWN0TWFwcGluZ3NcbiAgICAgIC5maWx0ZXIoKHBvbSkgPT4gIShwb20ucHJlZGljYXRlID09PSBhY3Rpb24ucHJlZGljYXRlICYmIGdldENvbHVtblZhbHVlKHBvbSkgPT09IGFjdGlvbi5vYmplY3QpKVxuICB9O1xufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcbiAgc3dpdGNoIChhY3Rpb24udHlwZSkge1xuICAgIGNhc2UgXCJGSU5JU0hfVVBMT0FEXCI6XG4gICAgY2FzZSBcIkxPR0lOXCI6XG4gICAgICByZXR1cm4gaW5pdGlhbFN0YXRlO1xuICAgIGNhc2UgXCJTRVRfUFJFRElDQVRFX09CSkVDVF9NQVBQSU5HXCI6XG4gICAgICByZXR1cm4gc2V0UHJlZGljYXRlT2JqZWN0TWFwcGluZyhzdGF0ZSwgYWN0aW9uKTtcbiAgICBjYXNlIFwiUkVNT1ZFX1BSRURJQ0FURV9PQkpFQ1RfTUFQUElOR1wiOlxuICAgICAgcmV0dXJuIHJlbW92ZVByZWRpY2F0ZU9iamVjdE1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG4gIH1cblxuICByZXR1cm4gc3RhdGU7XG59IiwiY29uc3QgaW5pdGlhbFN0YXRlID0ge1xuICBzaG93Uk1MUHJldmlldzogZmFsc2Vcbn07XG5cblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcbiAgc3dpdGNoIChhY3Rpb24udHlwZSkge1xuICAgIGNhc2UgXCJQUkVWSUVXX1JNTFwiOlxuICAgICAgcmV0dXJuIHtcbiAgICAgICAgLi4uc3RhdGUsXG4gICAgICAgIHNob3dSTUxQcmV2aWV3OiB0cnVlXG4gICAgICB9O1xuICAgIGNhc2UgXCJISURFX1JNTF9QUkVWSUVXXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICAuLi5zdGF0ZSxcbiAgICAgICAgc2hvd1JNTFByZXZpZXc6IGZhbHNlXG4gICAgICB9O1xuICB9XG5cbiAgcmV0dXJuIHN0YXRlO1xufSIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgdXNlcklkOiB1bmRlZmluZWQsXG4gIG15VnJlczogdW5kZWZpbmVkLFxufTtcblxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuICBzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG4gICAgY2FzZSBcIkxPR0lOXCI6XG4gICAgICByZXR1cm4ge1xuICAgICAgICAuLi5zdGF0ZSxcbiAgICAgICAgdXNlcklkOiBhY3Rpb24uZGF0YSxcbiAgICAgICAgbXlWcmVzOiBhY3Rpb24udnJlRGF0YSA/IGFjdGlvbi52cmVEYXRhLm1pbmUgOiBudWxsLFxuICAgICAgfTtcbiAgfVxuXG4gIHJldHVybiBzdGF0ZTtcbn0iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQge1JvdXRlciwgUm91dGUsIEluZGV4Um91dGUsIGhhc2hIaXN0b3J5fSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQge1Byb3ZpZGVyLCBjb25uZWN0fSBmcm9tIFwicmVhY3QtcmVkdXhcIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vYWN0aW9uc1wiO1xuaW1wb3J0IGdldFRva2VuIGZyb20gXCIuL3Rva2VuXCI7XG5cbmltcG9ydCBwYWdlQ29ubmVjdG9yIGZyb20gXCIuL2Nvbm5lY3RvcnMvcGFnZS1jb25uZWN0b3JcIjtcbmltcG9ydCBQYWdlIGZyb20gXCIuL2NvbXBvbmVudHMvcGFnZS5qc3hcIjtcblxuaW1wb3J0IGZpcnN0VXBsb2FkQ29ubmVjdG9yIGZyb20gXCIuL2Nvbm5lY3RvcnMvZmlyc3QtdXBsb2FkXCI7XG5pbXBvcnQgRmlyc3RVcGxvYWQgZnJvbSBcIi4vY29tcG9uZW50cy9maXJzdFVwbG9hZC5qc1wiO1xuXG5pbXBvcnQgY29sbGVjdGlvbk92ZXJ2aWV3Q29ubmVjdG9yIGZyb20gXCIuL2Nvbm5lY3RvcnMvY29sbGVjdGlvbi1vdmVydmlld1wiO1xuaW1wb3J0IENvbGxlY3Rpb25PdmVydmlldyBmcm9tIFwiLi9jb21wb25lbnRzL2NvbGxlY3Rpb24tb3ZlcnZpZXdcIjtcblxuaW1wb3J0IGNvbm5lY3RBcmNoZXR5cGVDb25uZWN0b3IgZnJvbSBcIi4vY29ubmVjdG9ycy9jb25uZWN0LXRvLWFyY2hldHlwZVwiO1xuaW1wb3J0IENvbm5lY3RUb0FyY2hldHlwZSBmcm9tIFwiLi9jb21wb25lbnRzL2Nvbm5lY3QtdG8tYXJjaGV0eXBlXCI7XG5cbmltcG9ydCBjb25uZWN0RGF0YUNvbm5lY3RvciBmcm9tIFwiLi9jb25uZWN0b3JzL2Nvbm5lY3QtZGF0YVwiO1xuaW1wb3J0IENvbm5lY3REYXRhIGZyb20gXCIuL2NvbXBvbmVudHMvY29ubmVjdC1kYXRhXCI7XG5cbmNvbnN0IHNlcmlhbGl6ZUFyY2hldHlwZU1hcHBpbmdzID0gKGNvbGxlY3Rpb25zKSA9PiB7XG4gIHJldHVybiBlbmNvZGVVUklDb21wb25lbnQoSlNPTi5zdHJpbmdpZnkoY29sbGVjdGlvbnMpKTtcbn07XG5cblxudmFyIHVybHMgPSB7XG4gIHJvb3QoKSB7XG4gICAgcmV0dXJuIFwiL1wiO1xuICB9LFxuICBtYXBEYXRhKHZyZUlkLCBtYXBwaW5ncykge1xuICAgIHJldHVybiB2cmVJZCAmJiBtYXBwaW5nc1xuICAgICAgPyBgL21hcGRhdGEvJHt2cmVJZH0vJHtzZXJpYWxpemVBcmNoZXR5cGVNYXBwaW5ncyhtYXBwaW5ncyl9YFxuICAgICAgOiBcIi9tYXBkYXRhLzp2cmVJZC86c2VyaWFsaXplZEFyY2hldHlwZU1hcHBpbmdzXCI7XG4gIH0sXG4gIG1hcEFyY2hldHlwZXModnJlSWQpIHtcbiAgICByZXR1cm4gdnJlSWQgPyBgL21hcGFyY2hldHlwZXMvJHt2cmVJZH1gIDogXCIvbWFwYXJjaGV0eXBlcy86dnJlSWRcIjtcbiAgfVxufTtcblxuZXhwb3J0IGZ1bmN0aW9uIG5hdmlnYXRlVG8oa2V5LCBhcmdzKSB7XG4gIGhhc2hIaXN0b3J5LnB1c2godXJsc1trZXldLmFwcGx5KG51bGwsIGFyZ3MpKTtcbn1cblxuY29uc3QgZGVmYXVsdENvbm5lY3QgPSBjb25uZWN0KChzdGF0ZSkgPT4gc3RhdGUsIGRpc3BhdGNoID0+IGFjdGlvbnMobmF2aWdhdGVUbywgZGlzcGF0Y2gpKTtcblxuY29uc3QgY29ubmVjdENvbXBvbmVudCA9IChzdGF0ZVRvUHJvcHMpID0+IGNvbm5lY3Qoc3RhdGVUb1Byb3BzLCBkaXNwYXRjaCA9PiBhY3Rpb25zKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSk7XG5cblxuY29uc3QgZmlsdGVyQXV0aG9yaXplZCA9IChyZWRpcmVjdFRvKSA9PiAobmV4dFN0YXRlLCByZXBsYWNlKSA9PiB7XG4gIGlmICghZ2V0VG9rZW4oKSkge1xuICAgIHJlcGxhY2UocmVkaXJlY3RUbyk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IChcbiAgPFByb3ZpZGVyIHN0b3JlPXtzdG9yZX0+XG4gICAgPFJvdXRlciBoaXN0b3J5PXtoYXNoSGlzdG9yeX0+XG4gICAgICA8Um91dGUgcGF0aD1cIi9cIiBjb21wb25lbnQ9e2Nvbm5lY3RDb21wb25lbnQocGFnZUNvbm5lY3RvcikoUGFnZSl9PlxuICAgICAgICA8SW5kZXhSb3V0ZSBjb21wb25lbnQ9e2Nvbm5lY3RDb21wb25lbnQoY29sbGVjdGlvbk92ZXJ2aWV3Q29ubmVjdG9yKShDb2xsZWN0aW9uT3ZlcnZpZXcpfS8+XG4gICAgICAgIDxSb3V0ZSBvbkVudGVyPXtmaWx0ZXJBdXRob3JpemVkKFwiL1wiKX1cbiAgICAgICAgICBwYXRoPXt1cmxzLm1hcEFyY2hldHlwZXMoKX0gY29tcG9uZW50cz17Y29ubmVjdENvbXBvbmVudChjb25uZWN0QXJjaGV0eXBlQ29ubmVjdG9yKShDb25uZWN0VG9BcmNoZXR5cGUpfSAvPlxuICAgICAgICA8Um91dGUgb25FbnRlcj17ZmlsdGVyQXV0aG9yaXplZChcIi9cIil9XG4gICAgICAgICAgICAgICBwYXRoPXt1cmxzLm1hcERhdGEoKX0gY29tcG9uZW50cz17Y29ubmVjdENvbXBvbmVudChjb25uZWN0RGF0YUNvbm5lY3RvcikoQ29ubmVjdERhdGEpfSAvPlxuXG4gICAgICA8L1JvdXRlPlxuICAgIDwvUm91dGVyPlxuICA8L1Byb3ZpZGVyPlxuKTtcblxuZXhwb3J0IHsgdXJscyB9OyIsImltcG9ydCB7Y3JlYXRlU3RvcmUsIGFwcGx5TWlkZGxld2FyZX0gZnJvbSBcInJlZHV4XCI7XG5pbXBvcnQgdGh1bmtNaWRkbGV3YXJlIGZyb20gXCJyZWR1eC10aHVua1wiO1xuXG5pbXBvcnQgcmVkdWNlcnMgZnJvbSBcIi4vcmVkdWNlcnNcIjtcblxuY29uc3QgbG9nZ2VyID0gKCkgPT4gbmV4dCA9PiBhY3Rpb24gPT4ge1xuICBpZiAoYWN0aW9uLmhhc093blByb3BlcnR5KFwidHlwZVwiKSkge1xuICAgIGNvbnNvbGUubG9nKFwiW1JFRFVYXVwiLCBhY3Rpb24udHlwZSwgYWN0aW9uKTtcbiAgfVxuXG4gIHJldHVybiBuZXh0KGFjdGlvbik7XG59O1xuXG5sZXQgY3JlYXRlU3RvcmVXaXRoTWlkZGxld2FyZSA9IGFwcGx5TWlkZGxld2FyZSgvKmxvZ2dlciwqLyB0aHVua01pZGRsZXdhcmUpKGNyZWF0ZVN0b3JlKTtcbmV4cG9ydCBkZWZhdWx0IGNyZWF0ZVN0b3JlV2l0aE1pZGRsZXdhcmUocmVkdWNlcnMpO1xuIiwiZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oKSB7XG4gIGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG4gIGxldCBwYXJhbXMgPSBwYXRoLnNwbGl0KCcmJyk7XG5cbiAgZm9yKGxldCBpIGluIHBhcmFtcykge1xuICAgIGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoJz0nKTtcbiAgICBpZihrZXkgPT09ICdoc2lkJykge1xuICAgICAgcmV0dXJuIHZhbHVlO1xuICAgIH1cbiAgfVxuICByZXR1cm4gbnVsbDtcbn1cbiIsImV4cG9ydCBkZWZhdWx0IChjYW1lbENhc2UpID0+IGNhbWVsQ2FzZVxuICAucmVwbGFjZSgvKFtBLVowLTldKS9nLCAobWF0Y2gpID0+IGAgJHttYXRjaC50b0xvd2VyQ2FzZSgpfWApXG4gIC50cmltKClcbiAgLnJlcGxhY2UoL14uLywgKG1hdGNoKSA9PiBtYXRjaC50b1VwcGVyQ2FzZSgpKVxuICAucmVwbGFjZSgvXy9nLCBcIiBcIik7XG4iLCJmdW5jdGlvbiBkZWVwQ2xvbmU5KG9iaikge1xuICAgIHZhciBpLCBsZW4sIHJldDtcblxuICAgIGlmICh0eXBlb2Ygb2JqICE9PSBcIm9iamVjdFwiIHx8IG9iaiA9PT0gbnVsbCkge1xuICAgICAgICByZXR1cm4gb2JqO1xuICAgIH1cblxuICAgIGlmIChBcnJheS5pc0FycmF5KG9iaikpIHtcbiAgICAgICAgcmV0ID0gW107XG4gICAgICAgIGxlbiA9IG9iai5sZW5ndGg7XG4gICAgICAgIGZvciAoaSA9IDA7IGkgPCBsZW47IGkrKykge1xuICAgICAgICAgICAgcmV0LnB1c2goICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV0gKTtcbiAgICAgICAgfVxuICAgIH0gZWxzZSB7XG4gICAgICAgIHJldCA9IHt9O1xuICAgICAgICBmb3IgKGkgaW4gb2JqKSB7XG4gICAgICAgICAgICBpZiAob2JqLmhhc093blByb3BlcnR5KGkpKSB7XG4gICAgICAgICAgICAgICAgcmV0W2ldID0gKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gcmV0O1xufVxuXG5leHBvcnQgZGVmYXVsdCBkZWVwQ2xvbmU5OyIsImltcG9ydCB7aXNCYXNpY1Byb3BlcnR5fSBmcm9tIFwiLi4vYWNjZXNzb3JzL3Byb3BlcnR5LW1hcHBpbmdzXCI7XG5pbXBvcnQge3VuaXF9IGZyb20gXCIuL3VuaXFcIjtcblxuY29uc3QgZGVmYXVsdE5hbWVzcGFjZSA9IFwiaHR0cDovL3RpbWJ1Y3Rvby5odXlnZW5zLmtuYXcubmwvXCI7XG5cbmNvbnN0IG5hbWVTcGFjZXMgPSB7XG4gIHN1cm5hbWU6IFwiaHR0cDovL3d3dy50ZWktYy5vcmcvbnMvMS4wL1wiLFxuICBmb3JlbmFtZTogXCJodHRwOi8vd3d3LnRlaS1jLm9yZy9ucy8xLjAvXCIsXG4gIHJvbGVOYW1lOiBcImh0dHA6Ly93d3cudGVpLWMub3JnL25zLzEuMC9cIixcbiAgbmFtZUxpbms6IFwiaHR0cDovL3d3dy50ZWktYy5vcmcvbnMvMS4wL1wiLFxuICBnZW5OYW1lOiBcImh0dHA6Ly93d3cudGVpLWMub3JnL25zLzEuMC9cIixcbn07XG5cbmNvbnN0IHJtbFRlbXBsYXRlID0gIHtcbiAgXCJAY29udGV4dFwiOiB7XG4gICAgXCJAdm9jYWJcIjogXCJodHRwOi8vd3d3LnczLm9yZy9ucy9yMnJtbCNcIixcbiAgICBcInJtbFwiOiBcImh0dHA6Ly9zZW13ZWIubW1sYWIuYmUvbnMvcm1sI1wiLFxuICAgIFwidGltXCI6IFwiaHR0cDovL3RpbWJ1Y3Rvby5odXlnZW5zLmtuYXcubmwvbWFwcGluZyNcIixcbiAgICBcImh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDEvcmRmLXNjaGVtYSNzdWJDbGFzc09mXCI6IHtcbiAgICAgIFwiQHR5cGVcIjogXCJAaWRcIlxuICAgIH0sXG4gICAgICBcInByZWRpY2F0ZVwiOiB7XG4gICAgICBcIkB0eXBlXCI6IFwiQGlkXCJcbiAgICB9LFxuICAgICAgXCJ0ZXJtVHlwZVwiOiB7XG4gICAgICBcIkB0eXBlXCI6IFwiQGlkXCJcbiAgICB9LFxuICAgICAgXCJwYXJlbnRUcmlwbGVzTWFwXCI6IHtcbiAgICAgIFwiQHR5cGVcIjogXCJAaWRcIlxuICAgIH0sXG4gICAgICBcImNsYXNzXCI6IHtcbiAgICAgIFwiQHR5cGVcIjogXCJAaWRcIlxuICAgIH0sXG4gICAgICBcIm9iamVjdFwiOiB7XG4gICAgICBcIkB0eXBlXCI6IFwiQGlkXCJcbiAgICB9XG4gIH1cbn07XG5cbmNvbnN0IGdldE5hbWVTcGFjZUZvciA9IChwcmVkaWNhdGUpID0+XG4gIHR5cGVvZiBuYW1lU3BhY2VzW3ByZWRpY2F0ZV0gID09PSBcInVuZGVmaW5lZFwiID8gZGVmYXVsdE5hbWVzcGFjZSA6IG5hbWVTcGFjZXNbcHJlZGljYXRlXTtcblxuY29uc3QgbWFrZU1hcE5hbWUgPSAodnJlLCBsb2NhbE5hbWUpID0+IGBodHRwOi8vdGltYnVjdG9vLmh1eWdlbnMua25hdy5ubC9tYXBwaW5nLyR7dnJlfS8ke2xvY2FsTmFtZX1gO1xuXG5jb25zdCBtYXBCYXNpY1Byb3BlcnR5ID0gKHByZWRpY2F0ZU9iamVjdE1hcCkgPT4gKHtcbiAgXCJvYmplY3RNYXBcIjoge1xuICAgIFwiY29sdW1uXCI6IHByZWRpY2F0ZU9iamVjdE1hcC5vYmplY3RNYXAuY29sdW1uXG4gIH0sXG4gIFwicHJlZGljYXRlXCI6IGAke2dldE5hbWVTcGFjZUZvcihwcmVkaWNhdGVPYmplY3RNYXAucHJlZGljYXRlKX0ke3ByZWRpY2F0ZU9iamVjdE1hcC5wcmVkaWNhdGV9YFxufSk7XG5cbmNvbnN0IG1hcFJlbGF0aW9uUHJvcGVydHkgPSAodnJlLCBwcmVkaWNhdGVPYmplY3RNYXApID0+ICh7XG4gIFwib2JqZWN0TWFwXCI6IHtcbiAgICBcImpvaW5Db25kaXRpb25cIjogcHJlZGljYXRlT2JqZWN0TWFwLm9iamVjdE1hcC5qb2luQ29uZGl0aW9uLFxuICAgIFwicGFyZW50VHJpcGxlc01hcFwiOiBgaHR0cDovL3RpbWJ1Y3Rvby5odXlnZW5zLmtuYXcubmwvbWFwcGluZy8ke3ZyZX0vJHtwcmVkaWNhdGVPYmplY3RNYXAub2JqZWN0TWFwLnBhcmVudFRyaXBsZXNNYXB9YFxuICB9LFxuICBcInByZWRpY2F0ZVwiOiBgJHtnZXROYW1lU3BhY2VGb3IocHJlZGljYXRlT2JqZWN0TWFwLnByZWRpY2F0ZSl9JHtwcmVkaWNhdGVPYmplY3RNYXAucHJlZGljYXRlfWBcbn0pO1xuXG5jb25zdCBtYWtlUHJlZGljYXRlT2JqZWN0TWFwID0gKHZyZSwgcHJlZGljYXRlT2JqZWN0TWFwKSA9PiB7XG4gIGlmIChpc0Jhc2ljUHJvcGVydHkocHJlZGljYXRlT2JqZWN0TWFwKSkge1xuICAgIHJldHVybiBtYXBCYXNpY1Byb3BlcnR5KHByZWRpY2F0ZU9iamVjdE1hcCk7XG4gIH1cblxuICBpZiAocHJlZGljYXRlT2JqZWN0TWFwLnByb3BlcnR5VHlwZSA9PT0gXCJyZWxhdGlvblwiKSB7XG4gICAgcmV0dXJuIG1hcFJlbGF0aW9uUHJvcGVydHkodnJlLCBwcmVkaWNhdGVPYmplY3RNYXApO1xuICB9XG5cbiAgcmV0dXJuIG51bGw7XG59O1xuXG5jb25zdCBtYXBDb2xsZWN0aW9uID0gKHZyZSwgYXJjaGV0eXBlTmFtZSwgY29sbGVjdGlvbk5hbWUsIHByZWRpY2F0ZU9iamVjdE1hcHMpID0+ICh7XG4gIFwiQGlkXCI6IG1ha2VNYXBOYW1lKHZyZSwgY29sbGVjdGlvbk5hbWUpLFxuICBcImh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDEvcmRmLXNjaGVtYSNzdWJDbGFzc09mXCI6IGBodHRwOi8vdGltYnVjdG9vLmh1eWdlbnMua25hdy5ubC8ke2FyY2hldHlwZU5hbWUucmVwbGFjZSgvcyQvLCBcIlwiKX1gLFxuICBcInJtbDpsb2dpY2FsU291cmNlXCI6IHtcbiAgICBcInJtbDpzb3VyY2VcIjoge1xuICAgICAgXCJ0aW06cmF3Q29sbGVjdGlvblwiOiBjb2xsZWN0aW9uTmFtZSxcbiAgICAgIFwidGltOnZyZU5hbWVcIjogdnJlXG4gICAgfVxuICB9LFxuICBcInN1YmplY3RNYXBcIjoge1xuICAgIFwidGVtcGxhdGVcIjogYCR7bWFrZU1hcE5hbWUodnJlLCBjb2xsZWN0aW9uTmFtZSl9L3t0aW1faWR9YFxuICB9LFxuICBcInByZWRpY2F0ZU9iamVjdE1hcFwiOiBbXG4gICAge1wib2JqZWN0XCI6IG1ha2VNYXBOYW1lKHZyZSwgY29sbGVjdGlvbk5hbWUpLCBcInByZWRpY2F0ZVwiOiBcImh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyN0eXBlXCJ9XG4gIF0uY29uY2F0KHByZWRpY2F0ZU9iamVjdE1hcHMubWFwKChwb20pID0+IG1ha2VQcmVkaWNhdGVPYmplY3RNYXAodnJlLCBwb20pKS5maWx0ZXIoKHBvbSkgPT4gcG9tICE9PSBudWxsKSApXG59KTtcblxuZXhwb3J0IGRlZmF1bHQgKHZyZSwgY29sbGVjdGlvbk1hcHBpbmdzLCBwcmVkaWNhdGVPYmplY3RNYXBwaW5ncykgPT4ge1xuICByZXR1cm4ge1xuICAgIC4uLnJtbFRlbXBsYXRlLFxuICAgIFwiQGdyYXBoXCI6IE9iamVjdC5rZXlzKHByZWRpY2F0ZU9iamVjdE1hcHBpbmdzKVxuICAgICAgLm1hcCgoY29sbGVjdGlvbk5hbWUpID0+IG1hcENvbGxlY3Rpb24odnJlLCBjb2xsZWN0aW9uTWFwcGluZ3NbY29sbGVjdGlvbk5hbWVdLmFyY2hldHlwZU5hbWUsIGNvbGxlY3Rpb25OYW1lLCBwcmVkaWNhdGVPYmplY3RNYXBwaW5nc1tjb2xsZWN0aW9uTmFtZV0pKVxuICB9O1xufVxuIiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuY29uc3QgX2dldEluID0gKHBhdGgsIGRhdGEpID0+XG5cdGRhdGEgP1xuXHRcdHBhdGgubGVuZ3RoID09PSAwID8gZGF0YSA6IF9nZXRJbihwYXRoLCBkYXRhW3BhdGguc2hpZnQoKV0pIDpcblx0XHRudWxsO1xuXG5cblxuY29uc3QgZ2V0SW4gPSAocGF0aCwgZGF0YSkgPT5cblx0X2dldEluKGNsb25lKHBhdGgpLCBkYXRhKTtcblxuXG5leHBvcnQgZGVmYXVsdCBnZXRJbjsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4vY2xvbmUtZGVlcFwiO1xuXG4vLyBEbyBlaXRoZXIgb2YgdGhlc2U6XG4vLyAgYSkgU2V0IGEgdmFsdWUgYnkgcmVmZXJlbmNlIGlmIGRlcmVmIGlzIG5vdCBudWxsXG4vLyAgYikgU2V0IGEgdmFsdWUgZGlyZWN0bHkgaW4gdG8gZGF0YSBvYmplY3QgaWYgZGVyZWYgaXMgbnVsbFxuY29uc3Qgc2V0RWl0aGVyID0gKGRhdGEsIGRlcmVmLCBrZXksIHZhbCkgPT4ge1xuXHQoZGVyZWYgfHwgZGF0YSlba2V5XSA9IHZhbDtcblx0cmV0dXJuIGRhdGE7XG59O1xuXG4vLyBTZXQgYSBuZXN0ZWQgdmFsdWUgaW4gZGF0YSAobm90IHVubGlrZSBpbW11dGFibGVqcywgYnV0IGEgY2xvbmUgb2YgZGF0YSBpcyBleHBlY3RlZCBmb3IgcHJvcGVyIGltbXV0YWJpbGl0eSlcbmNvbnN0IF9zZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPSBudWxsKSA9PlxuXHRwYXRoLmxlbmd0aCA+IDEgP1xuXHRcdF9zZXRJbihwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPyBkZXJlZltwYXRoLnNoaWZ0KCldIDogZGF0YVtwYXRoLnNoaWZ0KCldKSA6XG5cdFx0c2V0RWl0aGVyKGRhdGEsIGRlcmVmLCBwYXRoWzBdLCB2YWx1ZSk7XG5cbmNvbnN0IHNldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhKSA9PlxuXHRfc2V0SW4oY2xvbmUocGF0aCksIHZhbHVlLCBjbG9uZShkYXRhKSk7XG5cbmV4cG9ydCBkZWZhdWx0IHNldEluOyIsImNvbnN0IHVuaXEgPSAoYWNjdW0sIGN1cikgPT4gYWNjdW0uaW5kZXhPZihjdXIpIDwgMCA/IGFjY3VtLmNvbmNhdChjdXIpIDogYWNjdW07XG5cbmV4cG9ydCB7IHVuaXEgfTsiXX0=
