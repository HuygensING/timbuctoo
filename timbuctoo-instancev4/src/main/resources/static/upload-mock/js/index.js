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
    var timeout = cachedSetTimeout.call(null, cleanUpNextTick);
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
    cachedClearTimeout.call(null, timeout);
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
        cachedSetTimeout.call(null, drainQueue, 0);
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

},{"is-function":5}],4:[function(require,module,exports){
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

},{}],5:[function(require,module,exports){
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

},{}],6:[function(require,module,exports){
'use strict';
var toString = Object.prototype.toString;

module.exports = function (x) {
	var prototype;
	return toString.call(x) === '[object Object]' && (prototype = Object.getPrototypeOf(x), prototype === null || prototype === Object.getPrototypeOf({}));
};

},{}],7:[function(require,module,exports){
var overArg = require('./_overArg');

/* Built-in method references for those with the same name as other `lodash` methods. */
var nativeGetPrototype = Object.getPrototypeOf;

/**
 * Gets the `[[Prototype]]` of `value`.
 *
 * @private
 * @param {*} value The value to query.
 * @returns {null|Object} Returns the `[[Prototype]]`.
 */
var getPrototype = overArg(nativeGetPrototype, Object);

module.exports = getPrototype;

},{"./_overArg":9}],8:[function(require,module,exports){
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

},{}],9:[function(require,module,exports){
/**
 * Creates a function that invokes `func` with its first argument transformed.
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

},{}],10:[function(require,module,exports){
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

},{}],11:[function(require,module,exports){
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
 * [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
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
 * @returns {boolean} Returns `true` if `value` is a plain object,
 *  else `false`.
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

},{"./_getPrototype":7,"./_isHostObject":8,"./isObjectLike":10}],12:[function(require,module,exports){
'use strict';
var isOptionObject = require('is-plain-obj');
var hasOwnProperty = Object.prototype.hasOwnProperty;
var propIsEnumerable = Object.propertyIsEnumerable;
var globalThis = this;
var defaultMergeOpts = {
	concatArrays: false
};

function getEnumerableOwnPropertyKeys(value) {
	var keys = [];

	for (var key in value) {
		if (hasOwnProperty.call(value, key)) {
			keys.push(key);
		}
	}

	if (Object.getOwnPropertySymbols) {
		var symbols = Object.getOwnPropertySymbols(value);

		for (var i = 0; i < symbols.length; i++) {
			if (propIsEnumerable.call(value, symbols[i])) {
				keys.push(symbols[i]);
			}
		}
	}

	return keys;
}

function clone(value) {
	if (Array.isArray(value)) {
		return cloneArray(value);
	}

	if (isOptionObject(value)) {
		return cloneOptionObject(value);
	}

	return value;
}

function cloneArray(array) {
	var result = array.slice(0, 0);

	getEnumerableOwnPropertyKeys(array).forEach(function (key) {
		result[key] = clone(array[key]);
	});

	return result;
}

function cloneOptionObject(obj) {
	var result = Object.getPrototypeOf(obj) === null ? Object.create(null) : {};

	getEnumerableOwnPropertyKeys(obj).forEach(function (key) {
		result[key] = clone(obj[key]);
	});

	return result;
}

/**
 * @param merged {already cloned}
 * @return {cloned Object}
 */
function mergeKeys(merged, source, keys, mergeOpts) {
	keys.forEach(function (key) {
		if (key in merged) {
			merged[key] = merge(merged[key], source[key], mergeOpts);
		} else {
			merged[key] = clone(source[key]);
		}
	});

	return merged;
}

/**
 * @param merged {already cloned}
 * @return {cloned Object}
 *
 * see [Array.prototype.concat ( ...arguments )](http://www.ecma-international.org/ecma-262/6.0/#sec-array.prototype.concat)
 */
function concatArrays(merged, source, mergeOpts) {
	var result = merged.slice(0, 0);
	var resultIndex = 0;

	[merged, source].forEach(function (array) {
		var indices = [];

		// result.concat(array) with cloning
		for (var k = 0; k < array.length; k++) {
			if (!hasOwnProperty.call(array, k)) {
				continue;
			}

			indices.push(String(k));

			if (array === merged) {
				// already cloned
				result[resultIndex++] = array[k];
			} else {
				result[resultIndex++] = clone(array[k]);
			}
		}

		// merge non-index keys
		result = mergeKeys(result, array, getEnumerableOwnPropertyKeys(array).filter(function (key) {
			return indices.indexOf(key) === -1;
		}), mergeOpts);
	});

	return result;
}

/**
 * @param merged {already cloned}
 * @return {cloned Object}
 */
function merge(merged, source, mergeOpts) {
	if (mergeOpts.concatArrays && Array.isArray(merged) && Array.isArray(source)) {
		return concatArrays(merged, source, mergeOpts);
	}

	if (!isOptionObject(source) || !isOptionObject(merged)) {
		return clone(source);
	}

	return mergeKeys(merged, source, getEnumerableOwnPropertyKeys(source), mergeOpts);
}

module.exports = function () {
	var mergeOpts = merge(clone(defaultMergeOpts), (this !== globalThis && this) || {}, defaultMergeOpts);
	var merged = {};

	for (var i = 0; i < arguments.length; i++) {
		var option = arguments[i];

		if (option === undefined) {
			continue;
		}

		if (!isOptionObject(option)) {
			throw new TypeError('`' + option + '` is not an Option Object');
		}

		merged = merge(merged, option, mergeOpts);
	}

	return merged;
};

},{"is-plain-obj":6}],13:[function(require,module,exports){
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
},{"for-each":3,"trim":24}],14:[function(require,module,exports){
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
},{}],15:[function(require,module,exports){
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
},{"./compose":18}],16:[function(require,module,exports){
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
},{}],17:[function(require,module,exports){
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

},{"./createStore":19,"./utils/warning":21,"_process":1,"lodash/isPlainObject":11}],18:[function(require,module,exports){
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
},{}],19:[function(require,module,exports){
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
},{"lodash/isPlainObject":11,"symbol-observable":22}],20:[function(require,module,exports){
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

},{"./applyMiddleware":15,"./bindActionCreators":16,"./combineReducers":17,"./compose":18,"./createStore":19,"./utils/warning":21,"_process":1}],21:[function(require,module,exports){
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
},{}],22:[function(require,module,exports){
(function (global){
/* global window */
'use strict';

module.exports = require('./ponyfill')(global || window || this);

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"./ponyfill":23}],23:[function(require,module,exports){
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

},{}],24:[function(require,module,exports){

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

},{}],25:[function(require,module,exports){
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

},{"./lib/MockXMLHttpRequest":28,"global":4}],26:[function(require,module,exports){

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

},{}],27:[function(require,module,exports){

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

},{}],28:[function(require,module,exports){
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

},{"./MockRequest":26,"./MockResponse":27}],29:[function(require,module,exports){
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

},{"global/window":4,"is-function":5,"parse-headers":13,"xtend":30}],30:[function(require,module,exports){
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

},{}],31:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _store = require("../store");

var _store2 = _interopRequireDefault(_store);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _mappingToJsonLdRml = require("../util/mappingToJsonLdRml");

var _mappingToJsonLdRml2 = _interopRequireDefault(_mappingToJsonLdRml);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var toJson;
if ("development" === "development") {
	toJson = function toJson(data) {
		return JSON.stringify(data, undefined, 2);
	};
} else {
	toJson = function toJson(data) {
		return JSON.stringify(data);
	};
}

var actions = {
	onLoadMoreClick: function onLoadMoreClick(url, collection) {
		_store2.default.dispatch(function (dispatch, getState) {
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
		var file = files[0];
		var formData = new FormData();
		formData.append("file", file);
		formData.append("vre", file.name);
		_store2.default.dispatch({ type: "START_UPLOAD" });
		_store2.default.dispatch(function (dispatch, getState) {
			var state = getState();
			var payload = {
				body: formData,
				headers: {
					"Authorization": state.userdata.userId
				}
			};
			_xhr2.default.post("" + "/v2.1/bulk-upload", payload, function (err, resp) {
				var location = resp.headers.location;
				_xhr2.default.get(location, function (err, resp, body) {
					dispatch({ type: "FINISH_UPLOAD", data: JSON.parse(body) });
				});
			});
		});
	},

	onSaveMappings: function onSaveMappings() {
		_store2.default.dispatch({ type: "SAVE_STARTED" });
		_store2.default.dispatch(function (dispatch, getState) {
			var state = getState();
			var payload = {
				json: (0, _mappingToJsonLdRml2.default)(state.mappings, state.importData.vre),
				headers: {
					"Authorization": state.userdata.userId
				}
			};

			_xhr2.default.post(state.importData.saveMappingUrl, payload, function (err, resp) {
				if (err) {
					dispatch({ type: "SAVE_HAD_ERROR" });
				} else {
					dispatch({ type: "SAVE_SUCCEEDED" });
				}
				dispatch({ type: "SAVE_FINISHED" });
			});
		});
	},

	onPublishData: function onPublishData() {
		_store2.default.dispatch({ type: "PUBLISH_STARTED" });
		_store2.default.dispatch(function (dispatch, getState) {
			var state = getState();
			var payload = {
				headers: {
					"Authorization": state.userdata.userId
				}
			};

			_xhr2.default.post(state.importData.executeMappingUrl, payload, function (err, resp) {
				if (err) {
					dispatch({ type: "PUBLISH_HAD_ERROR" });
				} else {
					dispatch({ type: "PUBLISH_SUCCEEDED" });
				}
				dispatch({ type: "PUBLISH_FINISHED" });
			});
		});
	},

	onSelectCollection: function onSelectCollection(collection) {
		_store2.default.dispatch({ type: "SET_ACTIVE_COLLECTION", collection: collection });
		_store2.default.dispatch(function (dispatch, getState) {
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
		return _store2.default.dispatch({ type: "MAP_COLLECTION_ARCHETYPE", collection: collection, value: value });
	},

	onConfirmCollectionArchetypeMappings: function onConfirmCollectionArchetypeMappings() {
		_store2.default.dispatch({ type: "CONFIRM_COLLECTION_ARCHETYPE_MAPPINGS" });
		_store2.default.dispatch(function (dispatch, getState) {
			var state = getState();
			actions.onSelectCollection(state.importData.activeCollection);
		});
	},

	onSetFieldMapping: function onSetFieldMapping(collection, propertyField, importedField) {
		return _store2.default.dispatch({ type: "SET_FIELD_MAPPING", collection: collection, propertyField: propertyField, importedField: importedField });
	},

	onClearFieldMapping: function onClearFieldMapping(collection, propertyField, clearIndex) {
		return _store2.default.dispatch({ type: "CLEAR_FIELD_MAPPING", collection: collection, propertyField: propertyField, clearIndex: clearIndex });
	},

	onSetDefaultValue: function onSetDefaultValue(collection, propertyField, value) {
		return _store2.default.dispatch({ type: "SET_DEFAULT_VALUE", collection: collection, propertyField: propertyField, value: value });
	},

	onConfirmFieldMappings: function onConfirmFieldMappings(collection, propertyField) {
		return _store2.default.dispatch({ type: "CONFIRM_FIELD_MAPPINGS", collection: collection, propertyField: propertyField });
	},

	onUnconfirmFieldMappings: function onUnconfirmFieldMappings(collection, propertyField) {
		return _store2.default.dispatch({ type: "UNCONFIRM_FIELD_MAPPINGS", collection: collection, propertyField: propertyField });
	},

	onSetValueMapping: function onSetValueMapping(collection, propertyField, timValue, mapValue) {
		return _store2.default.dispatch({ type: "SET_VALUE_MAPPING", collection: collection, propertyField: propertyField, timValue: timValue, mapValue: mapValue });
	},

	onIgnoreColumnToggle: function onIgnoreColumnToggle(collection, variableName) {
		return _store2.default.dispatch({ type: "TOGGLE_IGNORED_COLUMN", collection: collection, variableName: variableName });
	},

	onAddCustomProperty: function onAddCustomProperty(collection, propertyName, propertyType) {
		return _store2.default.dispatch({ type: "ADD_CUSTOM_PROPERTY", collection: collection, propertyField: propertyName, propertyType: propertyType });
	},

	onRemoveCustomProperty: function onRemoveCustomProperty(collection, propertyName) {
		return _store2.default.dispatch({ type: "REMOVE_CUSTOM_PROPERTY", collection: collection, propertyField: propertyName });
	}
};

exports.default = actions;

},{"../store":56,"../util/mappingToJsonLdRml":59,"xhr":29}],32:[function(require,module,exports){
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

var ArchetypeMappings = function (_React$Component) {
	_inherits(ArchetypeMappings, _React$Component);

	function ArchetypeMappings() {
		_classCallCheck(this, ArchetypeMappings);

		return _possibleConstructorReturn(this, Object.getPrototypeOf(ArchetypeMappings).apply(this, arguments));
	}

	_createClass(ArchetypeMappings, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var archetype = _props.archetype;
			var importData = _props.importData;
			var onMapCollectionArchetype = _props.onMapCollectionArchetype;
			var mappings = _props.mappings;
			var collectionsAreMapped = _props.collectionsAreMapped;
			var onConfirmCollectionArchetypeMappings = _props.onConfirmCollectionArchetypeMappings;

			return _react2.default.createElement(
				"div",
				{ className: "row centered-form center-block" },
				_react2.default.createElement(
					"div",
					{ className: "container col-md-12", style: { textAlign: "left" } },
					_react2.default.createElement(
						"main",
						null,
						_react2.default.createElement(
							"div",
							{ className: "panel panel-default col-md-6 col-md-offset-3" },
							_react2.default.createElement(
								"div",
								{ className: "panel-body" },
								"We found ",
								importData.sheets.length,
								" collections in the file.",
								_react2.default.createElement("br", null),
								"Connect the tabs to the timbuctoo archetypes"
							),
							_react2.default.createElement(
								"ul",
								{ className: "list-group" },
								importData.sheets.map(function (sheet, i) {
									return _react2.default.createElement(
										"li",
										{ className: "list-group-item", key: i },
										_react2.default.createElement(
											"label",
											null,
											i + 1,
											" ",
											sheet.collection
										),
										_react2.default.createElement(_selectField2.default, {
											onChange: function onChange(value) {
												return onMapCollectionArchetype(sheet.collection, value);
											},
											onClear: function onClear() {
												return onMapCollectionArchetype(sheet.collection, null);
											},
											options: Object.keys(archetype).filter(function (domain) {
												return domain !== "relations";
											}).sort(),
											placeholder: "Archetype for " + sheet.collection,
											value: mappings.collections[sheet.collection].archetypeName })
									);
								}),
								_react2.default.createElement(
									"li",
									{ className: "list-group-item" },
									_react2.default.createElement(
										"button",
										{ className: "btn btn-lg btn-success", disabled: !collectionsAreMapped, onClick: onConfirmCollectionArchetypeMappings },
										"Ok"
									)
								)
							)
						)
					)
				)
			);
		}
	}]);

	return ArchetypeMappings;
}(_react2.default.Component);

ArchetypeMappings.propTypes = {
	archetype: _react2.default.PropTypes.object,
	collectionsAreMapped: _react2.default.PropTypes.bool,
	importData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object,
	onConfirmCollectionArchetypeMappings: _react2.default.PropTypes.func,
	onMapCollectionArchetype: _react2.default.PropTypes.func
};

exports.default = ArchetypeMappings;

},{"./fields/select-field":37,"react":"react"}],33:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _addProperty = require("./property-form/add-property");

var _addProperty2 = _interopRequireDefault(_addProperty);

var _propertyForm = require("./property-form");

var _propertyForm2 = _interopRequireDefault(_propertyForm);

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
			var _this2 = this;

			var _props = this.props;
			var importData = _props.importData;
			var archetype = _props.archetype;
			var mappings = _props.mappings;
			var activeCollection = importData.activeCollection;
			var sheets = importData.sheets;


			var collectionData = sheets.find(function (sheet) {
				return sheet.collection === activeCollection;
			});
			var mappingData = mappings.collections[activeCollection];

			var archetypeName = mappings.collections[activeCollection].archetypeName;

			var archetypeFields = archetypeName ? archetype[archetypeName] : [];
			var archeTypePropFields = archetypeFields.filter(function (af) {
				return af.type !== "relation";
			});

			var propertyForms = archeTypePropFields.map(function (af, i) {
				return _react2.default.createElement(_propertyForm2.default, _extends({}, _this2.props, { collectionData: collectionData, mappingData: mappingData, custom: false, key: i, name: af.name, type: af.type }));
			});

			var customPropertyForms = mappings.collections[activeCollection].customProperties.map(function (cf, i) {
				return _react2.default.createElement(_propertyForm2.default, _extends({}, _this2.props, { collectionData: collectionData, mappingData: mappingData, custom: true, key: i, name: cf.name, type: cf.type }));
			});

			return _react2.default.createElement(
				"div",
				{ className: "panel panel-default" },
				_react2.default.createElement(
					"div",
					{ className: "panel-heading" },
					"Collection settings: ",
					activeCollection
				),
				_react2.default.createElement(
					"ul",
					{ className: "list-group" },
					propertyForms,
					customPropertyForms,
					_react2.default.createElement(_addProperty2.default, this.props)
				)
			);
		}
	}]);

	return CollectionForm;
}(_react2.default.Component);

CollectionForm.propTypes = {
	archetype: _react2.default.PropTypes.object,
	importData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object
};

exports.default = CollectionForm;

},{"./property-form":40,"./property-form/add-property":39,"react":"react"}],34:[function(require,module,exports){
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
		key: "mappingsAreComplete",
		value: function mappingsAreComplete(sheet) {
			var mappings = this.props.mappings;


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
	}, {
		key: "allMappingsAreIncomplete",
		value: function allMappingsAreIncomplete() {
			var _this2 = this;

			var importData = this.props.importData;
			var sheets = importData.sheets;

			return sheets.map(function (sheet) {
				return _this2.mappingsAreComplete(sheet);
			}).filter(function (result) {
				return result !== true;
			}).length === 0;
		}
	}, {
		key: "render",
		value: function render() {
			var _this3 = this;

			var _props = this.props;
			var onSaveMappings = _props.onSaveMappings;
			var onPublishData = _props.onPublishData;
			var importData = _props.importData;
			var onSelectCollection = _props.onSelectCollection;
			var sheets = importData.sheets;


			return _react2.default.createElement(
				"div",
				{ className: "panel panel-default" },
				_react2.default.createElement(
					"div",
					{ className: "panel-heading" },
					"Collections"
				),
				_react2.default.createElement(
					"div",
					{ className: "list-group" },
					sheets.map(function (sheet, i) {
						return _react2.default.createElement(
							"a",
							{
								className: (0, _classnames2.default)("list-group-item", { active: sheet.collection === importData.activeCollection }),
								key: i,
								onClick: function onClick() {
									return onSelectCollection(sheet.collection);
								}
							},
							_react2.default.createElement("span", { className: (0, _classnames2.default)("glyphicon", "pull-right", {
									"glyphicon-question-sign": !_this3.mappingsAreComplete(sheet),
									"glyphicon-ok-sign": _this3.mappingsAreComplete(sheet)
								}) }),
							sheet.collection
						);
					}),
					_react2.default.createElement(
						"li",
						{ className: "list-group-item" },
						_react2.default.createElement(
							"button",
							{ className: "btn btn-success", onClick: onSaveMappings },
							"Save"
						),
						" ",
						_react2.default.createElement(
							"button",
							{ className: "btn btn-success", onClick: onPublishData, disabled: !this.allMappingsAreIncomplete() },
							"Publish"
						)
					)
				)
			);
		}
	}]);

	return CollectionIndex;
}(_react2.default.Component);

CollectionIndex.propTypes = {
	onSaveMappings: _react2.default.PropTypes.func,
	onPublishData: _react2.default.PropTypes.func,
	importData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object,
	onSelectCollection: _react2.default.PropTypes.func
};

exports.default = CollectionIndex;

},{"classnames":2,"react":"react"}],35:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _dataRow = require("./table/data-row");

var _dataRow2 = _interopRequireDefault(_dataRow);

var _headerCell = require("./table/header-cell");

var _headerCell2 = _interopRequireDefault(_headerCell);

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

			var _props = this.props;
			var importData = _props.importData;
			var mappings = _props.mappings;
			var onIgnoreColumnToggle = _props.onIgnoreColumnToggle;
			var sheets = importData.sheets;
			var activeCollection = importData.activeCollection;

			var collectionData = sheets.find(function (sheet) {
				return sheet.collection === activeCollection;
			});

			var rows = collectionData.rows;
			var collection = collectionData.collection;
			var variables = collectionData.variables;


			var confirmedCols = variables.map(function (value, i) {
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

			var ignoredColumns = mappings.collections[activeCollection].ignoredColumns;


			return _react2.default.createElement(
				"div",
				null,
				_react2.default.createElement(
					"div",
					{ className: "panel panel-default" },
					_react2.default.createElement(
						"div",
						{ className: "panel-heading" },
						"Collection: ",
						collection
					),
					_react2.default.createElement(
						"table",
						{ className: "table table-bordered" },
						_react2.default.createElement(
							"thead",
							null,
							_react2.default.createElement(
								"tr",
								null,
								variables.map(function (header, i) {
									return _react2.default.createElement(_headerCell2.default, {
										activeCollection: activeCollection,
										header: header,
										isConfirmed: confirmedCols.indexOf(i) > -1,
										isIgnored: ignoredColumns.indexOf(header) > -1,
										key: i,
										onIgnoreColumnToggle: onIgnoreColumnToggle
									});
								})
							)
						),
						_react2.default.createElement(
							"tbody",
							null,
							rows.map(function (row, i) {
								return _react2.default.createElement(_dataRow2.default, {
									confirmedCols: confirmedCols,
									ignoredColumns: ignoredColumns,
									key: i,
									row: row,
									variables: variables
								});
							})
						)
					)
				),
				_react2.default.createElement(
					"button",
					{ onClick: function onClick() {
							return _this2.props.onLoadMoreClick && _this2.props.onLoadMoreClick(collectionData.nextUrl, collection);
						}, disabled: !collectionData.nextUrl, className: "btn btn-default", style: { color: "#333", backgroundColor: "#fff", "borderColor": "#ccc", float: "right" } },
					"more..."
				)
			);
		}
	}]);

	return CollectionTable;
}(_react2.default.Component);

CollectionTable.propTypes = {
	importData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object,
	onIgnoreColumnToggle: _react2.default.PropTypes.func
};

exports.default = CollectionTable;

},{"./table/data-row":46,"./table/header-cell":47,"react":"react"}],36:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _collectionIndex = require("./collection-index");

var _collectionIndex2 = _interopRequireDefault(_collectionIndex);

var _collectionTable = require("./collection-table");

var _collectionTable2 = _interopRequireDefault(_collectionTable);

var _collectionForm = require("./collection-form");

var _collectionForm2 = _interopRequireDefault(_collectionForm);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var DatasheetMappings = function (_React$Component) {
	_inherits(DatasheetMappings, _React$Component);

	function DatasheetMappings() {
		_classCallCheck(this, DatasheetMappings);

		return _possibleConstructorReturn(this, Object.getPrototypeOf(DatasheetMappings).apply(this, arguments));
	}

	_createClass(DatasheetMappings, [{
		key: "render",
		value: function render() {
			return _react2.default.createElement(
				"div",
				{ className: "row", style: { textAlign: "left" } },
				_react2.default.createElement(
					"div",
					{ className: "container col-md-12" },
					_react2.default.createElement(
						"nav",
						{ className: "col-sm-2" },
						_react2.default.createElement(_collectionIndex2.default, this.props)
					),
					_react2.default.createElement(
						"main",
						{ className: "col-sm-10" },
						_react2.default.createElement(_collectionForm2.default, this.props),
						_react2.default.createElement(_collectionTable2.default, this.props)
					)
				)
			);
		}
	}]);

	return DatasheetMappings;
}(_react2.default.Component);

exports.default = DatasheetMappings;

},{"./collection-form":33,"./collection-index":34,"./collection-table":35,"react":"react"}],37:[function(require,module,exports){
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
			var options = _props.options;
			var onChange = _props.onChange;
			var onClear = _props.onClear;
			var placeholder = _props.placeholder;
			var value = _props.value;


			return _react2.default.createElement(
				"span",
				{ className: (0, _classnames2.default)("dropdown", { open: this.state.isOpen }) },
				_react2.default.createElement(
					"button",
					{ className: "btn btn-default btn-sx dropdown-toggle",
						onClick: this.toggleSelect.bind(this),
						style: value ? { color: "#666" } : { color: "#aaa" } },
					value || placeholder,
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
					options.map(function (option, i) {
						return _react2.default.createElement(
							"li",
							{ key: i },
							_react2.default.createElement(
								"a",
								{ onClick: function onClick() {
										onChange(option);_this2.toggleSelect();
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
	options: _react2.default.PropTypes.array,
	placeholder: _react2.default.PropTypes.string,
	value: _react2.default.PropTypes.string
};

exports.default = SelectField;

},{"classnames":2,"react":"react","react-dom":"react-dom"}],38:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _uploadSplashScreen = require("./upload-splash-screen");

var _uploadSplashScreen2 = _interopRequireDefault(_uploadSplashScreen);

var _archetypeMappings = require("./archetype-mappings");

var _archetypeMappings2 = _interopRequireDefault(_archetypeMappings);

var _datasheetMappings = require("./datasheet-mappings");

var _datasheetMappings2 = _interopRequireDefault(_datasheetMappings);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var App = function (_React$Component) {
	_inherits(App, _React$Component);

	function App() {
		_classCallCheck(this, App);

		return _possibleConstructorReturn(this, Object.getPrototypeOf(App).apply(this, arguments));
	}

	_createClass(App, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var importData = _props.importData;
			var mappings = _props.mappings;

			var collectionsAreMapped = Object.keys(mappings.collections).length > 0 && Object.keys(mappings.collections).map(function (key) {
				return mappings.collections[key].archetypeName;
			}).indexOf(null) < 0;

			var datasheetMappings = importData.sheets && collectionsAreMapped && mappings.confirmed ? _react2.default.createElement(_datasheetMappings2.default, this.props) : null;

			var archetypeMappings = !datasheetMappings && importData.sheets ? _react2.default.createElement(_archetypeMappings2.default, _extends({}, this.props, { collectionsAreMapped: collectionsAreMapped })) : null;

			var uploadSplashScreen = !datasheetMappings && !archetypeMappings ? _react2.default.createElement(_uploadSplashScreen2.default, this.props) : null;

			return datasheetMappings || archetypeMappings || uploadSplashScreen;
		}
	}]);

	return App;
}(_react2.default.Component);

App.propTypes = {
	importData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object
};

exports.default = App;

},{"./archetype-mappings":32,"./datasheet-mappings":36,"./upload-splash-screen":48,"react":"react"}],39:[function(require,module,exports){
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
			newName: null,
			newType: null
		};
		return _this;
	}

	_createClass(AddProperty, [{
		key: "render",
		value: function render() {
			var _this2 = this;

			var _props = this.props;
			var importData = _props.importData;
			var allArchetypes = _props.archetype;
			var mappings = _props.mappings;
			var onAddCustomProperty = _props.onAddCustomProperty;
			var _state = this.state;
			var newType = _state.newType;
			var newName = _state.newName;
			var activeCollection = importData.activeCollection;
			var archetypeName = mappings.collections[activeCollection].archetypeName;

			var archetype = allArchetypes[archetypeName];

			var availableArchetypes = Object.keys(mappings.collections).map(function (key) {
				return mappings.collections[key].archetypeName;
			});
			var relationTypeOptions = archetype.filter(function (prop) {
				return prop.type === "relation";
			}).filter(function (prop) {
				return availableArchetypes.indexOf(prop.relation.targetCollection) > -1;
			}).map(function (prop) {
				return prop.name;
			});

			return _react2.default.createElement(
				"li",
				{ className: "list-group-item" },
				_react2.default.createElement(
					"label",
					null,
					_react2.default.createElement(
						"strong",
						null,
						"Add property"
					)
				),
				_react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return _this2.setState({ newType: value, newName: value === "relation" ? null : newName });
					},
					onClear: function onClear() {
						return _this2.setState({ newType: null });
					},
					options: ["text", "relation"],
					placeholder: "Choose a type...",
					value: newType }),
				" ",
				newType === "relation" ? _react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return _this2.setState({ newName: value });
					},
					onClear: function onClear() {
						return _this2.setState({ newName: null });
					},
					options: relationTypeOptions,
					placeholder: "Choose a type...",
					value: newName }) : _react2.default.createElement("input", { onChange: function onChange(ev) {
						return _this2.setState({ newName: ev.target.value });
					}, placeholder: "Property name", value: newName }),
				" ",
				_react2.default.createElement(
					"button",
					{ className: "btn btn-success", disabled: !(newName && newType),
						onClick: function onClick() {
							onAddCustomProperty(activeCollection, newName, newType);
							_this2.setState({ newName: null, newType: null });
						} },
					"Add"
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

},{"../fields/select-field":37,"react":"react"}],40:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _links = require("./links");

var _links2 = _interopRequireDefault(_links);

var _text = require("./text");

var _text2 = _interopRequireDefault(_text);

var _select = require("./select");

var _select2 = _interopRequireDefault(_select);

var _names = require("./names");

var _names2 = _interopRequireDefault(_names);

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
	names: function names(props) {
		return _react2.default.createElement(_names2.default, props);
	},
	links: function links(props) {
		return _react2.default.createElement(_links2.default, props);
	},
	select: function select(props) {
		return _react2.default.createElement(_select2.default, props);
	},
	multiselect: function multiselect(props) {
		return _react2.default.createElement(_select2.default, props);
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
			var custom = _props.custom;
			var name = _props.name;
			var collectionData = _props.collectionData;
			var type = _props.type;
			var mappings = _props.mappings;
			var onConfirmFieldMappings = _props.onConfirmFieldMappings;
			var onUnconfirmFieldMappings = _props.onUnconfirmFieldMappings;
			var onRemoveCustomProperty = _props.onRemoveCustomProperty;


			var mapping = mappings.collections[collectionData.collection].mappings;

			var propertyMapping = mapping.find(function (m) {
				return m.property === name;
			}) || {};
			var confirmed = propertyMapping.confirmed || false;

			var confirmButton = this.canConfirm(propertyMapping.variable || null) && !confirmed ? _react2.default.createElement(
				"button",
				{ className: "btn btn-success btn-sm", onClick: function onClick() {
						return onConfirmFieldMappings(collectionData.collection, name);
					} },
				"Confirm"
			) : confirmed ? _react2.default.createElement(
				"button",
				{ className: "btn btn-danger btn-sm", onClick: function onClick() {
						return onUnconfirmFieldMappings(collectionData.collection, name);
					} },
				"Unconfirm"
			) : null;

			var formComponent = typeMap[type](this.props);

			return _react2.default.createElement(
				"li",
				{ className: "list-group-item" },
				custom ? _react2.default.createElement(
					"a",
					{ className: "pull-right btn-danger btn-xs", onClick: function onClick() {
							return onRemoveCustomProperty(collectionData.collection, name);
						} },
					_react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
				) : null,
				_react2.default.createElement(
					"label",
					null,
					_react2.default.createElement(
						"strong",
						null,
						name
					),
					" (",
					type,
					")"
				),
				formComponent,
				" ",
				confirmButton
			);
		}
	}]);

	return PropertyForm;
}(_react2.default.Component);

PropertyForm.propTypes = {
	collectionData: _react2.default.PropTypes.object,
	custom: _react2.default.PropTypes.bool,
	mappings: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onConfirmFieldMappings: _react2.default.PropTypes.func,
	onRemoveCustomProperty: _react2.default.PropTypes.func,
	onUnconfirmFieldMappings: _react2.default.PropTypes.func,
	type: _react2.default.PropTypes.string
};

exports.default = PropertyForm;

},{"./links":41,"./names":42,"./relation":43,"./select":44,"./text":45,"react":"react"}],41:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

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
			var _props = this.props;
			var collectionData = _props.collectionData;
			var onSetFieldMapping = _props.onSetFieldMapping;
			var onClearFieldMapping = _props.onClearFieldMapping;
			var onSetDefaultValue = _props.onSetDefaultValue;
			var mappings = _props.mappings;
			var name = _props.name;


			var mapping = mappings.collections[collectionData.collection].mappings;
			var propertyMapping = mapping.find(function (m) {
				return m.property === name;
			}) || {};
			var selectedVariableUrl = (propertyMapping.variable || []).find(function (v) {
				return v.field === "url";
			}) || {};
			var defaultValueUrl = (propertyMapping.defaultValue || []).find(function (v) {
				return v.field === "url";
			}) || {};

			var selectedVariableLabel = (propertyMapping.variable || []).find(function (v) {
				return v.field === "label";
			}) || {};
			var defaultValueLabel = (propertyMapping.defaultValue || []).find(function (v) {
				return v.field === "label";
			}) || {};

			return _react2.default.createElement(
				"span",
				null,
				_react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetFieldMapping(collectionData.collection, name, [_extends({}, selectedVariableUrl), _extends({}, selectedVariableLabel, { field: "label", variableName: value })]);
					},
					onClear: function onClear() {
						return onClearFieldMapping(collectionData.collection, name, (propertyMapping.variable || []).map(function (v) {
							return v.field;
						}).indexOf("label"));
					},
					options: collectionData.variables, placeholder: "Select label column...",
					value: selectedVariableLabel.variableName || null }),
				" ",
				selectedVariableUrl.variableName && selectedVariableLabel.variableName ? _react2.default.createElement("input", { onChange: function onChange(ev) {
						return onSetDefaultValue(collectionData.collection, name, [_extends({}, defaultValueUrl), _extends({}, defaultValueLabel, { field: "label", value: ev.target.value })]);
					},
					placeholder: "Default value...", type: "text", value: defaultValueLabel.value || null }) : null,
				" ",
				_react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetFieldMapping(collectionData.collection, name, [_extends({}, selectedVariableUrl, { field: "url", variableName: value }), _extends({}, selectedVariableLabel)]);
					},
					onClear: function onClear() {
						return onClearFieldMapping(collectionData.collection, name, (propertyMapping.variable || []).map(function (v) {
							return v.field;
						}).indexOf("url"));
					},
					options: collectionData.variables, placeholder: "Select URL column...",
					value: selectedVariableUrl.variableName || null }),
				" ",
				selectedVariableUrl.variableName && selectedVariableLabel.variableName ? _react2.default.createElement("input", { onChange: function onChange(ev) {
						return onSetDefaultValue(collectionData.collection, name, [_extends({}, defaultValueUrl, { field: "url", value: ev.target.value }), _extends({}, defaultValueLabel)]);
					},
					placeholder: "Default value...", type: "text", value: defaultValueUrl.value || null }) : null
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
	onSetDefaultValue: _react2.default.PropTypes.func,
	onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"../fields/select-field":37,"react":"react"}],42:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _selectField = require("../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

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
		key: "onComponentChange",
		value: function onComponentChange(propertyMapping, mappingIndex, variableName) {
			var _props = this.props;
			var collectionData = _props.collectionData;
			var onSetFieldMapping = _props.onSetFieldMapping;
			var name = _props.name;

			var variableSpec = propertyMapping.variable.map(function (v, i) {
				return i === mappingIndex ? _extends({}, v, { variableName: variableName }) : v;
			});

			if (variableSpec.length > 0) {
				onSetFieldMapping(collectionData.collection, name, variableSpec);
			}
		}
	}, {
		key: "render",
		value: function render() {
			var _this2 = this;

			var _props2 = this.props;
			var collectionData = _props2.collectionData;
			var onSetFieldMapping = _props2.onSetFieldMapping;
			var onClearFieldMapping = _props2.onClearFieldMapping;
			var mappings = _props2.mappings;
			var name = _props2.name;
			var archetype = _props2.archetype;


			var mapping = mappings.collections[collectionData.collection].mappings;
			var propertyMapping = mapping.find(function (m) {
				return m.property === name;
			}) || {};
			var components = archetype[mappings.collections[collectionData.collection].archetypeName].find(function (a) {
				return a.name === name;
			}).options;

			return _react2.default.createElement(
				"span",
				null,
				propertyMapping.variable && propertyMapping.variable.length ? _react2.default.createElement(
					"div",
					{ style: { marginBottom: "12px" } },
					(propertyMapping.variable || []).map(function (v, i) {
						return _react2.default.createElement(
							"span",
							{ key: i, style: { display: "inline-block", margin: "8px 8px 0 0" } },
							_react2.default.createElement(
								"div",
								{ style: { marginBottom: "2px" } },
								_react2.default.createElement(
									"a",
									{ className: "pull-right btn-danger btn-xs", onClick: function onClick() {
											return onClearFieldMapping(collectionData.collection, name, i);
										} },
									_react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
								),
								v.component,
								" "
							),
							_react2.default.createElement(_selectField2.default, {
								onChange: function onChange(value) {
									return _this2.onComponentChange(propertyMapping, i, value);
								},
								onClear: function onClear() {
									return onClearFieldMapping(collectionData.collection, name, i);
								},
								options: collectionData.variables,
								placeholder: "Select column for " + v.component,
								value: v.variableName })
						);
					})
				) : null,
				_react2.default.createElement(_selectField2.default, { onChange: function onChange(value) {
						return onSetFieldMapping(collectionData.collection, name, [].concat(_toConsumableArray(propertyMapping.variable || []), [{ component: value }]));
					},
					options: components, placeholder: "Add name component...",
					value: null })
			);
		}
	}]);

	return Form;
}(_react2.default.Component);

Form.propTypes = {
	archetype: _react2.default.PropTypes.object,
	collectionData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onClearFieldMapping: _react2.default.PropTypes.func,
	onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"../fields/select-field":37,"react":"react"}],43:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

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

			var ownSheet = this.props.collectionData;
			var allSheets = this.props.importData.sheets;
			var allSheetMappings = this.props.mappings.collections;
			var mapping = this.props.mappingData.mappings.find(function (prop) {
				return prop.property === _this2.props.name;
			});
			//at one point the mapping does not yet exists, but a custom property reference containing the name does exist
			var customProperty = this.props.mappingData.customProperties.find(function (prop) {
				return prop.name === _this2.props.name;
			});
			var ownArchetype = this.props.archetype[this.props.mappingData.archetypeName];
			var onSetFieldMapping = this.props.onSetFieldMapping.bind(null, ownSheet.collection, this.props.name);
			var onClearFieldMapping = this.props.onClearFieldMapping.bind(null, ownSheet.collection, this.props.name);

			var relationInfo = mapping && mapping.variable && mapping.variable.length > 0 ? mapping.variable[0] : {};

			var propertyMetadata = ownArchetype.find(function (metadata) {
				return metadata.name === (mapping ? mapping.property : customProperty.name);
			});

			var availableSheets = propertyMetadata ? Object.keys(allSheetMappings).filter(function (key) {
				return allSheetMappings[key].archetypeName === propertyMetadata.relation.targetCollection;
			}) : [];

			var linkedSheet = relationInfo.targetCollection ? allSheets.find(function (sheet) {
				return sheet.collection === relationInfo.targetCollection;
			}) : null;

			return _react2.default.createElement(
				"span",
				null,
				_react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetFieldMapping([_extends({}, relationInfo, { variableName: value })]);
					},
					onClear: function onClear() {
						return onClearFieldMapping(0);
					},
					options: ownSheet.variables, placeholder: "Select source column...",
					value: relationInfo.variableName || null }),
				" ",
				_react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetFieldMapping([_extends({}, relationInfo, { targetCollection: value })]);
					},
					onClear: function onClear() {
						return onClearFieldMapping(0);
					},
					options: availableSheets, placeholder: "Select a target collection...",
					value: relationInfo.targetCollection || null }),
				" ",
				linkedSheet ? _react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetFieldMapping([_extends({}, relationInfo, { targetVariableName: value })]);
					},
					onClear: function onClear() {
						return onClearFieldMapping(0);
					},
					options: linkedSheet.variables, placeholder: "Select a target column...",
					value: relationInfo.targetVariableName || null }) : null
			);
		}
	}]);

	return Form;
}(_react2.default.Component);

Form.propTypes = {
	collectionData: _react2.default.PropTypes.object,
	importData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onClearFieldMapping: _react2.default.PropTypes.func,
	onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"../fields/select-field":37,"react":"react"}],44:[function(require,module,exports){
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

var Form = function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		return _possibleConstructorReturn(this, Object.getPrototypeOf(Form).apply(this, arguments));
	}

	_createClass(Form, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var collectionData = _props.collectionData;
			var onSetFieldMapping = _props.onSetFieldMapping;
			var onClearFieldMapping = _props.onClearFieldMapping;
			var onSetDefaultValue = _props.onSetDefaultValue;
			var onSetValueMapping = _props.onSetValueMapping;
			var mappings = _props.mappings;
			var name = _props.name;
			var archetype = _props.archetype;


			var mapping = mappings.collections[collectionData.collection].mappings;
			var propertyMapping = mapping.find(function (m) {
				return m.property === name;
			}) || {};
			var selectedVariable = propertyMapping.variable && propertyMapping.variable.length ? propertyMapping.variable[0] : {};
			var defaultValue = propertyMapping.defaultValue && propertyMapping.defaultValue.length ? propertyMapping.defaultValue[0] : {};
			var valueMappings = propertyMapping.valueMappings || {};
			var defaultOptions = (archetype[mappings.collections[collectionData.collection].archetypeName] || []).find(function (a) {
				return a.name === name;
			}).options || [];

			return _react2.default.createElement(
				"span",
				null,
				_react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetFieldMapping(collectionData.collection, name, [{ variableName: value }]);
					},
					onClear: function onClear() {
						return onClearFieldMapping(collectionData.collection, name, 0);
					},
					options: collectionData.variables, placeholder: "Select a column...",
					value: selectedVariable.variableName }),
				" ",
				selectedVariable.variableName ? _react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetDefaultValue(collectionData.collection, name, [{ value: value }]);
					},
					onClear: function onClear() {
						return onSetDefaultValue(collectionData.collection, name, [{ value: null }]);
					},
					options: defaultOptions, placeholder: "Select a default value...",
					value: defaultValue.value }) : null,
				selectedVariable.variableName ? _react2.default.createElement(
					"ul",
					{ className: "list-group", style: { marginTop: "12px", maxHeight: "275px", overflowY: "auto" } },
					_react2.default.createElement(
						"li",
						{ className: "list-group-item" },
						_react2.default.createElement(
							"strong",
							null,
							"Map import values to select options"
						),
						_react2.default.createElement(
							"p",
							null,
							"* Leave blank to match exact value"
						),
						" "
					),
					defaultOptions.map(function (selectOption, i) {
						return _react2.default.createElement(
							"li",
							{ className: "list-group-item", key: i },
							_react2.default.createElement(
								"label",
								null,
								selectOption
							),
							_react2.default.createElement("input", { onChange: function onChange(ev) {
									return onSetValueMapping(collectionData.collection, name, selectOption, ev.target.value);
								},
								type: "text", value: valueMappings[selectOption] || "" })
						);
					})
				) : null
			);
		}
	}]);

	return Form;
}(_react2.default.Component);

Form.propTypes = {
	archetype: _react2.default.PropTypes.object,
	collectionData: _react2.default.PropTypes.object,
	mappings: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onClearFieldMapping: _react2.default.PropTypes.func,
	onSetDefaultValue: _react2.default.PropTypes.func,
	onSetFieldMapping: _react2.default.PropTypes.func,
	onSetValueMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"../fields/select-field":37,"react":"react"}],45:[function(require,module,exports){
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

var Form = function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		return _possibleConstructorReturn(this, Object.getPrototypeOf(Form).apply(this, arguments));
	}

	_createClass(Form, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var collectionData = _props.collectionData;
			var onSetFieldMapping = _props.onSetFieldMapping;
			var onClearFieldMapping = _props.onClearFieldMapping;
			var onSetDefaultValue = _props.onSetDefaultValue;
			var mappings = _props.mappings;
			var name = _props.name;


			var mapping = mappings.collections[collectionData.collection].mappings;
			var propertyMapping = mapping.find(function (m) {
				return m.property === name;
			}) || {};
			var selectedVariable = propertyMapping.variable && propertyMapping.variable.length ? propertyMapping.variable[0] : {};
			var defaultValue = propertyMapping.defaultValue && propertyMapping.defaultValue.length ? propertyMapping.defaultValue[0] : {};

			return _react2.default.createElement(
				"span",
				null,
				_react2.default.createElement(_selectField2.default, {
					onChange: function onChange(value) {
						return onSetFieldMapping(collectionData.collection, name, [{ variableName: value }]);
					},
					onClear: function onClear() {
						return onClearFieldMapping(collectionData.collection, name, 0);
					},
					options: collectionData.variables, placeholder: "Select a column...",
					value: selectedVariable.variableName || null }),
				" ",
				_react2.default.createElement("input", { onChange: function onChange(ev) {
						return onSetDefaultValue(collectionData.collection, name, [{ value: ev.target.value }]);
					},
					placeholder: "Default value...", type: "text", value: defaultValue.value || null })
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
	onSetDefaultValue: _react2.default.PropTypes.func,
	onSetFieldMapping: _react2.default.PropTypes.func
};

exports.default = Form;

},{"../fields/select-field":37,"react":"react"}],46:[function(require,module,exports){
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
			var _props = this.props;
			var row = _props.row;
			var confirmedCols = _props.confirmedCols;
			var ignoredColumns = _props.ignoredColumns;
			var variables = _props.variables;


			return _react2.default.createElement(
				"tr",
				null,
				row.map(function (cell, i) {
					return _react2.default.createElement(
						"td",
						{ className: (0, _classnames2.default)({
								ignored: confirmedCols.indexOf(i) < 0 && ignoredColumns.indexOf(variables[i]) > -1
							}), key: i },
						cell
					);
				})
			);
		}
	}]);

	return DataRow;
}(_react2.default.Component);

DataRow.propTypes = {
	confirmedCols: _react2.default.PropTypes.array,
	ignoredColumns: _react2.default.PropTypes.array,
	row: _react2.default.PropTypes.array,
	variables: _react2.default.PropTypes.array
};

exports.default = DataRow;

},{"classnames":2,"react":"react"}],47:[function(require,module,exports){
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
			var activeCollection = _props.activeCollection;
			var onIgnoreColumnToggle = _props.onIgnoreColumnToggle;


			return _react2.default.createElement(
				"th",
				{ className: (0, _classnames2.default)({
						success: isConfirmed,
						info: !isConfirmed && !isIgnored,
						ignored: !isConfirmed && isIgnored
					}) },
				header,
				_react2.default.createElement("a", { className: (0, _classnames2.default)("pull-right", "glyphicon", {
						"glyphicon-ok-sign": isConfirmed,
						"glyphicon-question-sign": !isConfirmed && !isIgnored,
						"glyphicon-remove": !isConfirmed && isIgnored
					}), onClick: function onClick() {
						return !isConfirmed ? onIgnoreColumnToggle(activeCollection, header) : null;
					} })
			);
		}
	}]);

	return HeaderCell;
}(_react2.default.Component);

HeaderCell.propTypes = {
	activeCollection: _react2.default.PropTypes.string,
	header: _react2.default.PropTypes.string,
	isConfirmed: _react2.default.PropTypes.bool,
	isIgnored: _react2.default.PropTypes.bool,
	onIgnoreColumnToggle: _react2.default.PropTypes.func
};

exports.default = HeaderCell;

},{"classnames":2,"react":"react"}],48:[function(require,module,exports){
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

var UploadSplashScreen = function (_React$Component) {
	_inherits(UploadSplashScreen, _React$Component);

	function UploadSplashScreen() {
		_classCallCheck(this, UploadSplashScreen);

		return _possibleConstructorReturn(this, Object.getPrototypeOf(UploadSplashScreen).apply(this, arguments));
	}

	_createClass(UploadSplashScreen, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var onUploadFileSelect = _props.onUploadFileSelect;
			var userId = _props.userdata.userId;
			var onLogin = _props.onLogin;
			var isUploading = _props.importData.isUploading;


			var uploadButton = void 0;
			if (userId) {
				uploadButton = _react2.default.createElement(
					"div",
					null,
					_react2.default.createElement(
						"div",
						{ className: "login-sub-component lead" },
						_react2.default.createElement(
							"label",
							{ className: (0, _classnames2.default)("btn", "btn-lg", "btn-default", "underMargin", { disabled: isUploading }) },
							_react2.default.createElement("span", { className: "glyphicon glyphicon-cloud-upload" }),
							isUploading ? "Uploading..." : "Browse",
							_react2.default.createElement("input", {
								disabled: isUploading,
								type: "file",
								style: { display: "none" },
								onChange: function onChange(e) {
									return onUploadFileSelect(e.target.files);
								} })
						)
					),
					_react2.default.createElement(
						"p",
						{ className: "lead" },
						"Don't have a dataset handy? Here’s an ",
						_react2.default.createElement(
							"a",
							{ href: "/static/example.xlsx" },
							_react2.default.createElement(
								"em",
								null,
								"example excel sheet"
							)
						)
					)
				);
			} else {
				uploadButton = _react2.default.createElement(
					"div",
					null,
					_react2.default.createElement(
						"div",
						{ className: "lead" },
						_react2.default.createElement(
							"form",
							{ className: "login-sub-component", action: "https://secure.huygens.knaw.nl/saml2/login", method: "POST" },
							_react2.default.createElement("input", { name: "hsurl", type: "hidden", value: window.location.href }),
							_react2.default.createElement(
								"button",
								{ type: "submit", className: "btn btn-lg btn-default underMargin" },
								_react2.default.createElement("span", { className: "glyphicon glyphicon-log-in" }),
								" Log in"
							)
						)
					),
					_react2.default.createElement(
						"p",
						{ className: "lead" },
						"Most university accounts will work. You can also log in using google, twitter or facebook."
					)
				);
			}

			return _react2.default.createElement(
				"div",
				{ className: "site-wrapper-inner  fullsize_background" },
				_react2.default.createElement(
					"div",
					{ className: "cover-container white" },
					_react2.default.createElement(
						"div",
						{ className: "inner cover" },
						_react2.default.createElement(
							"h1",
							{ className: "cover-heading underMargin" },
							_react2.default.createElement("img", { alt: "timbuctoo", className: "logo", src: "images/logo_timbuctoo.svg" }),
							_react2.default.createElement("br", null),
							"TIMBUCTOO"
						),
						_react2.default.createElement(
							"p",
							{ className: "lead underMargin" },
							"Get your data stored and connected to the world.",
							_react2.default.createElement("br", null),
							"Start uploading your data."
						),
						uploadButton
					)
				)
			);
		}
	}]);

	return UploadSplashScreen;
}(_react2.default.Component);

UploadSplashScreen.propTypes = {
	onUpload: _react2.default.PropTypes.func,
	userdata: _react2.default.PropTypes.shape({
		userId: _react2.default.PropTypes.string
	}),
	importData: _react2.default.PropTypes.shape({
		isUploading: _react2.default.PropTypes.boolean
	})
};

exports.default = UploadSplashScreen;

},{"classnames":2,"react":"react"}],49:[function(require,module,exports){
"use strict";

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var _components = require("./components");

var _components2 = _interopRequireDefault(_components);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _xhrMock = require("xhr-mock");

var _xhrMock2 = _interopRequireDefault(_xhrMock);

var _servermocks = require("./servermocks");

var _servermocks2 = _interopRequireDefault(_servermocks);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

if ("false" === "true") {
	var orig = window.XMLHttpRequest;
	_xhrMock2.default.setup(); //mock window.XMLHttpRequest usages
	var mock = window.XMLHttpRequest;
	window.XMLHttpRequest = orig;
	_xhr2.default.XMLHttpRequest = mock;
	_xhr2.default.XDomainRequest = mock;
	(0, _servermocks2.default)(_xhrMock2.default, orig);
}

_store2.default.subscribe(function () {
	var state = _store2.default.getState();
	_reactDom2.default.render(_react2.default.createElement(_components2.default, _extends({}, state, _actions2.default)), document.getElementById("app"));
});

function checkTokenInUrl(state) {
	var path = window.location.search.substr(1);
	var params = path.split('&');

	for (var i in params) {
		var _params$i$split = params[i].split('=');

		var _params$i$split2 = _slicedToArray(_params$i$split, 2);

		var key = _params$i$split2[0];
		var value = _params$i$split2[1];

		if (key === 'hsid' && !state.userdata.userId) {
			_store2.default.dispatch({ type: "LOGIN", data: value });
			break;
		}
	}
}

document.addEventListener("DOMContentLoaded", function () {
	var state = _store2.default.getState();
	_reactDom2.default.render(_react2.default.createElement(_components2.default, _extends({}, state, _actions2.default)), document.getElementById("app"));
	checkTokenInUrl(state);

	if (!state.archetype || Object.keys(state.archetype).length === 0) {
		(0, _xhr2.default)("" + "/v2.1/metadata/Admin", function (err, resp) {
			_store2.default.dispatch({ type: "SET_ARCHETYPE_METADATA", data: JSON.parse(resp.body) });
		});
	}
});

},{"./actions":31,"./components":38,"./servermocks":55,"./store":56,"react":"react","react-dom":"react-dom","xhr":29,"xhr-mock":25}],50:[function(require,module,exports){
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

},{"../util/persist":60}],51:[function(require,module,exports){
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
			return _extends({}, state, { isUploading: true });
		case "FINISH_UPLOAD":
			return _extends({}, state, {
				sheets: action.data.collections.map(function (sheet) {
					return {
						collection: sheet.name,
						variables: sheet.variables,
						rows: [],
						nextUrl: sheet.data
					};
				}),
				activeCollection: action.data.collections[0].name,
				vre: action.data.vre,
				saveMappingUrl: action.data.saveMapping,
				executeMappingUrl: action.data.executeMapping
			});
		case "COLLECTION_ITEMS_LOADING_SUCCEEDED":
			var sheetIdx = findIndex(state.sheets, function (sheet) {
				return sheet.collection === action.collection;
			});
			var result = _extends({}, state, {
				sheets: [].concat(_toConsumableArray(state.sheets.slice(0, sheetIdx)), [_extends({}, state.sheets[sheetIdx], {
					rows: addRows(state.sheets[sheetIdx].rows, action.data.items, state.sheets[sheetIdx].variables),
					nextUrl: action.data.next
				})], _toConsumableArray(state.sheets.slice(sheetIdx + 1)))
			});

			return result;
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

			return result;
		case "SET_ACTIVE_COLLECTION":
			return _extends({}, state, { activeCollection: action.collection });
	}

	return state;
};

var _persist = require("../util/persist");

var _mergeOptions = require("merge-options");

var _mergeOptions2 = _interopRequireDefault(_mergeOptions);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var initialState = (0, _persist.getItem)("importData") || {
	isUploading: false,
	sheets: null,
	activeCollection: null
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

function sheetRowFromDictToArray(rowdict, arrayOfVariableNames) {
	return arrayOfVariableNames.map(function (name) {
		return rowdict[name];
	});
}

function addRows(curRows, newRows, arrayOfVariableNames) {
	return curRows.concat(newRows.map(function (item) {
		return sheetRowFromDictToArray(item, arrayOfVariableNames);
	}));
}

},{"../util/persist":60,"merge-options":12}],52:[function(require,module,exports){
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

},{"./archetype":50,"./import-data":51,"./mappings":53,"./userdata":54}],53:[function(require,module,exports){
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
			return _extends({}, state, { collections: action.data.collections.reduce(scaffoldCollectionMappings, {}) });

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

},{"../util/get-in":58,"../util/persist":60,"../util/set-in":61}],54:[function(require,module,exports){
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
				userId: action.data
			};
	}

	return state;
};

var initialState = {
	userId: undefined
};

},{}],55:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = setupMocks;
function setupMocks(xhrmock, orig) {
  xhrmock.get("http://test.repository.huygens.knaw.nl/v2.1/metadata/Admin", function (req, resp) {
    return resp.status(200).body("{\n          \"persons\": [\n            {\n              \"name\": \"name\",\n              \"type\": \"text\"\n            },\n            {\n              \"name\": \"hasWritten\",\n              \"type\": \"relation\",\n              \"quicksearch\": \"/v2.1/domain/documents/autocomplete\",\n              \"relation\": {\n                \"direction\": \"OUT\",\n                \"outName\": \"hasWritten\",\n                \"inName\": \"wasWrittenBy\",\n                \"targetCollection\": \"documents\",\n                \"relationCollection\": \"relations\",\n                \"relationTypeId\": \"bba10d37-86cc-4f1f-ba2d-016af2b21aa4\"\n              }\n            },\n            {\n              \"name\": \"isRelatedTo\",\n              \"type\": \"relation\",\n              \"quicksearch\": \"/v2.1/domain/persons/autocomplete\",\n              \"relation\": {\n                \"direction\": \"OUT\",\n                \"outName\": \"isRelatedTo\",\n                \"inName\": \"isRelatedTo\",\n                \"targetCollection\": \"persons\",\n                \"relationCollection\": \"relations\",\n                \"relationTypeId\": \"cba10d37-86cc-4f1f-ba2d-016af2b21aa5\"\n              }\n            }\n          ],\n          \"documents\": [\n            {\n              \"name\": \"name\",\n              \"type\": \"text\"\n            }\n          ]\n        }");
  }).post("http://test.repository.huygens.knaw.nl/v2.1/bulk-upload", function (req, resp) {
    console.log("bulk-upload");
    return resp.status(200).header("Location", "<<The get raw data url that the server provides>>");
  }).post("<<The save mapping url that the server provides>>", function (req, resp) {
    console.log("save mapping", req.body());
    return resp.status(204);
  }).post("<<The execute mapping url that the server provides>>", function (req, resp) {
    console.log("execute mapping", req.body());
    return resp.status(204);
  }).get("<<The get raw data url that the server provides>>", function (req, resp) {
    console.log("get raw data");
    return resp.status(200).body(JSON.stringify({
      vre: "thevrename",
      saveMapping: "<<The save mapping url that the server provides>>",
      executeMapping: "<<The execute mapping url that the server provides>>",
      collections: [{
        name: "mockpersons",
        variables: ["ID", "Voornaam", "tussenvoegsel", "Achternaam", "GeschrevenDocument", "Genoemd in", "Is getrouwd met"],
        data: "<<url for person data>>"
      }, {
        name: "mockdocuments",
        variables: ["titel", "datum", "referentie", "url"],
        data: "<<url for document data>>"
      }]
    }));
  }).get("<<url for person data>>", function (req, resp) {
    console.log("get person items data");
    return resp.status(200).body(JSON.stringify({
      "_next": "<<more data>>",
      "items": [{
        "tim_id": "1",
        "ID": "1",
        "Voornaam": "Voornaam",
        "tussenvoegsel": "tussenvoegsel",
        "Achternaam": "Achternaam",
        "GeschrevenDocument": "GeschrevenDocument",
        "Genoemd in": "Genoemd in",
        "Is getrouwd met": "Is getrouwd met"
      }, {
        "tim_id": "2",
        "ID": "2",
        "Voornaam": "Voornaam",
        "tussenvoegsel": "tussenvoegsel",
        "Achternaam": "Achternaam",
        "GeschrevenDocument": "GeschrevenDocument",
        "Genoemd in": "Genoemd in",
        "Is getrouwd met": "Is getrouwd met"
      }]
    }));
  }).get("<<more data>>", function (req, resp) {
    console.log("get person items data");
    return resp.status(200).body(JSON.stringify({
      "items": [{
        "ID": "3",
        "Voornaam": "Voornaam",
        "tussenvoegsel": "tussenvoegsel",
        "Achternaam": "Achternaam",
        "GeschrevenDocument": "GeschrevenDocument",
        "Genoemd in": "Genoemd in",
        "Is getrouwd met": "Is getrouwd met"
      }, {
        "ID": "4",
        "Voornaam": "Voornaam",
        "tussenvoegsel": "tussenvoegsel",
        "Achternaam": "Achternaam",
        "GeschrevenDocument": "GeschrevenDocument",
        "Genoemd in": "Genoemd in",
        "Is getrouwd met": "Is getrouwd met"
      }]
    }));
  }).get("<<url for document data>>", function (req, resp) {
    console.log("get person items data");
    return resp.status(200).body(JSON.stringify({
      "name": "someCollection",
      "variables": ["tim_id", "var1", "var2"],
      "items": [{
        "tim_id": "1",
        "titel": "titel",
        "datum": "datum",
        "referentie": "referentie",
        "url": "url"
      }, {
        "tim_id": "2",
        "titel": "titel",
        "datum": "datum",
        "referentie": "referentie",
        "url": "url"
      }]
    }));
  }).mock(function (req, resp) {
    console.error("unmocked request", req.url(), req, resp);
  });
}

},{}],56:[function(require,module,exports){
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

},{"./reducers":52,"./util/persist":60,"redux":20,"redux-thunk":14}],57:[function(require,module,exports){
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

},{}],58:[function(require,module,exports){
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

},{"./clone-deep":57}],59:[function(require,module,exports){
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
      "tim": "http://timbuctoo.com/mapping",
      "predicate": {
        "@type": "@id"
      }
    },
    "@graph": Object.keys(mapping.collections).map(function (key) {
      return mapSheet(key, mapping.collections[key], vre);
    })
  };
}
var s = {
  "archetypeName": "persons",
  "mappings": [{
    "property": "name",
    "variable": [{
      "variableName": "Voornaam"
    }],
    "defaultValue": [],
    "confirmed": true,
    "valueMappings": {}
  }, {
    "property": "Foo",
    "variable": [{
      "variableName": "Achternaam"
    }],
    "defaultValue": [],
    "confirmed": true,
    "valueMappings": {}
  }, {
    "property": "isRelatedTo",
    "variable": [{
      "variableName": "Voornaam",
      "targetCollection": "mockpersons",
      "targetVariableName": "Voornaam"
    }],
    "defaultValue": [],
    "confirmed": true,
    "valueMappings": {}
  }]
};

function makeMapName(localName) {
  return "tim:s/" + localName + "map";
}

function mapSheet(key, sheet, vre) {
  // console.log(JSON.stringify(sheet, undefined, 2));
  //FIXME: move logicalSource and subjectMap under the control of the server
  return {
    "@id": makeMapName(key),
    "rml:logicalSource": {
      "rml:source": {
        "tim:rawCollection": key,
        "tim:vreName": vre
      }
    },
    "subjectMap": {
      "class": "http://timbuctoo.com/" + vre + "/" + key,
      "template": "http://timbuctoo.com/" + vre + "/" + key + "/{tim_id}"
    },
    "predicateObjectMap": sheet.mappings.map(makePredicateObjectMap)
  };
}

function makePredicateObjectMap(mapping) {
  var property = mapping.property;
  var variable = mapping.variable[0];
  if (variable.targetCollection) {
    return {
      "objectMap": {
        "reference": {
          "joinCondition": {
            "child": variable.variableName,
            "parent": variable.targetVariableName
          },
          "parentTriplesMap": makeMapName(variable.targetCollection)
        }
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

},{}],60:[function(require,module,exports){
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

},{}],61:[function(require,module,exports){
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

},{"./clone-deep":57}]},{},[49])(49)
});
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvcHJvY2Vzcy9icm93c2VyLmpzIiwibm9kZV9tb2R1bGVzL2NsYXNzbmFtZXMvaW5kZXguanMiLCJub2RlX21vZHVsZXMvZm9yLWVhY2gvaW5kZXguanMiLCJub2RlX21vZHVsZXMvZ2xvYmFsL3dpbmRvdy5qcyIsIm5vZGVfbW9kdWxlcy9pcy1mdW5jdGlvbi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9pcy1wbGFpbi1vYmovaW5kZXguanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL19nZXRQcm90b3R5cGUuanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL19pc0hvc3RPYmplY3QuanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL19vdmVyQXJnLmpzIiwibm9kZV9tb2R1bGVzL2xvZGFzaC9pc09iamVjdExpa2UuanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL2lzUGxhaW5PYmplY3QuanMiLCJub2RlX21vZHVsZXMvbWVyZ2Utb3B0aW9ucy9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9wYXJzZS1oZWFkZXJzL3BhcnNlLWhlYWRlcnMuanMiLCJub2RlX21vZHVsZXMvcmVkdXgtdGh1bmsvbGliL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9hcHBseU1pZGRsZXdhcmUuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2JpbmRBY3Rpb25DcmVhdG9ycy5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvY29tYmluZVJlZHVjZXJzLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9jb21wb3NlLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9jcmVhdGVTdG9yZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvaW5kZXguanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL3V0aWxzL3dhcm5pbmcuanMiLCJub2RlX21vZHVsZXMvc3ltYm9sLW9ic2VydmFibGUvaW5kZXguanMiLCJub2RlX21vZHVsZXMvc3ltYm9sLW9ic2VydmFibGUvcG9ueWZpbGwuanMiLCJub2RlX21vZHVsZXMvdHJpbS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9saWIvTW9ja1JlcXVlc3QuanMiLCJub2RlX21vZHVsZXMveGhyLW1vY2svbGliL01vY2tSZXNwb25zZS5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9saWIvTW9ja1hNTEh0dHBSZXF1ZXN0LmpzIiwibm9kZV9tb2R1bGVzL3hoci9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy94dGVuZC9pbW11dGFibGUuanMiLCJzcmMvYWN0aW9ucy9pbmRleC5qcyIsInNyYy9jb21wb25lbnRzL2FyY2hldHlwZS1tYXBwaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tZm9ybS5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24taW5kZXguanMiLCJzcmMvY29tcG9uZW50cy9jb2xsZWN0aW9uLXRhYmxlLmpzIiwic3JjL2NvbXBvbmVudHMvZGF0YXNoZWV0LW1hcHBpbmdzLmpzIiwic3JjL2NvbXBvbmVudHMvZmllbGRzL3NlbGVjdC1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2luZGV4LmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9hZGQtcHJvcGVydHkuanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL2luZGV4LmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9saW5rcy5qcyIsInNyYy9jb21wb25lbnRzL3Byb3BlcnR5LWZvcm0vbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL3JlbGF0aW9uLmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL3RleHQuanMiLCJzcmMvY29tcG9uZW50cy90YWJsZS9kYXRhLXJvdy5qcyIsInNyYy9jb21wb25lbnRzL3RhYmxlL2hlYWRlci1jZWxsLmpzIiwic3JjL2NvbXBvbmVudHMvdXBsb2FkLXNwbGFzaC1zY3JlZW4uanMiLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvYXJjaGV0eXBlLmpzIiwic3JjL3JlZHVjZXJzL2ltcG9ydC1kYXRhLmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21hcHBpbmdzLmpzIiwic3JjL3JlZHVjZXJzL3VzZXJkYXRhLmpzIiwic3JjL3NlcnZlcm1vY2tzLmpzIiwic3JjL3N0b3JlLmpzIiwic3JjL3V0aWwvY2xvbmUtZGVlcC5qcyIsInNyYy91dGlsL2dldC1pbi5qcyIsInNyYy91dGlsL21hcHBpbmdUb0pzb25MZFJtbC5qcyIsInNyYy91dGlsL3BlcnNpc3QuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdkhBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2hEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUM5Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUNUQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ1BBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDZkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3RFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN6SkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDYkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDekRBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDbERBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDOUhBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3ZDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDclFBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDN0NBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUN4QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbkJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNkQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1R0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3hGQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQy9OQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFD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LEtBQUssS0FBTCxFQUFOLENBQVIsR0FBOEIsS0FBSyxLQUFLLEtBQUwsRUFBTCxDQUF4RCxDQURELEdBRUMsVUFBVSxJQUFWLEVBQWdCLEtBQWhCLEVBQXVCLEtBQUssQ0FBTCxDQUF2QixFQUFnQyxLQUFoQyxDQUhhO0FBQUEsQ0FBZjs7QUFLQSxJQUFNLFFBQVEsU0FBUixLQUFRLENBQUMsSUFBRCxFQUFPLEtBQVAsRUFBYyxJQUFkO0FBQUEsUUFDYixPQUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIi8vIHNoaW0gZm9yIHVzaW5nIHByb2Nlc3MgaW4gYnJvd3NlclxuXG52YXIgcHJvY2VzcyA9IG1vZHVsZS5leHBvcnRzID0ge307XG5cbi8vIGNhY2hlZCBmcm9tIHdoYXRldmVyIGdsb2JhbCBpcyBwcmVzZW50IHNvIHRoYXQgdGVzdCBydW5uZXJzIHRoYXQgc3R1YiBpdFxuLy8gZG9uJ3QgYnJlYWsgdGhpbmdzLiAgQnV0IHdlIG5lZWQgdG8gd3JhcCBpdCBpbiBhIHRyeSBjYXRjaCBpbiBjYXNlIGl0IGlzXG4vLyB3cmFwcGVkIGluIHN0cmljdCBtb2RlIGNvZGUgd2hpY2ggZG9lc24ndCBkZWZpbmUgYW55IGdsb2JhbHMuICBJdCdzIGluc2lkZSBhXG4vLyBmdW5jdGlvbiBiZWNhdXNlIHRyeS9jYXRjaGVzIGRlb3B0aW1pemUgaW4gY2VydGFpbiBlbmdpbmVzLlxuXG52YXIgY2FjaGVkU2V0VGltZW91dDtcbnZhciBjYWNoZWRDbGVhclRpbWVvdXQ7XG5cbihmdW5jdGlvbiAoKSB7XG4gIHRyeSB7XG4gICAgY2FjaGVkU2V0VGltZW91dCA9IHNldFRpbWVvdXQ7XG4gIH0gY2F0Y2ggKGUpIHtcbiAgICBjYWNoZWRTZXRUaW1lb3V0ID0gZnVuY3Rpb24gKCkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdzZXRUaW1lb3V0IGlzIG5vdCBkZWZpbmVkJyk7XG4gICAgfVxuICB9XG4gIHRyeSB7XG4gICAgY2FjaGVkQ2xlYXJUaW1lb3V0ID0gY2xlYXJUaW1lb3V0O1xuICB9IGNhdGNoIChlKSB7XG4gICAgY2FjaGVkQ2xlYXJUaW1lb3V0ID0gZnVuY3Rpb24gKCkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdjbGVhclRpbWVvdXQgaXMgbm90IGRlZmluZWQnKTtcbiAgICB9XG4gIH1cbn0gKCkpXG52YXIgcXVldWUgPSBbXTtcbnZhciBkcmFpbmluZyA9IGZhbHNlO1xudmFyIGN1cnJlbnRRdWV1ZTtcbnZhciBxdWV1ZUluZGV4ID0gLTE7XG5cbmZ1bmN0aW9uIGNsZWFuVXBOZXh0VGljaygpIHtcbiAgICBpZiAoIWRyYWluaW5nIHx8ICFjdXJyZW50UXVldWUpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgIH1cbiAgICBkcmFpbmluZyA9IGZhbHNlO1xuICAgIGlmIChjdXJyZW50UXVldWUubGVuZ3RoKSB7XG4gICAgICAgIHF1ZXVlID0gY3VycmVudFF1ZXVlLmNvbmNhdChxdWV1ZSk7XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcXVldWVJbmRleCA9IC0xO1xuICAgIH1cbiAgICBpZiAocXVldWUubGVuZ3RoKSB7XG4gICAgICAgIGRyYWluUXVldWUoKTtcbiAgICB9XG59XG5cbmZ1bmN0aW9uIGRyYWluUXVldWUoKSB7XG4gICAgaWYgKGRyYWluaW5nKSB7XG4gICAgICAgIHJldHVybjtcbiAgICB9XG4gICAgdmFyIHRpbWVvdXQgPSBjYWNoZWRTZXRUaW1lb3V0LmNhbGwobnVsbCwgY2xlYW5VcE5leHRUaWNrKTtcbiAgICBkcmFpbmluZyA9IHRydWU7XG5cbiAgICB2YXIgbGVuID0gcXVldWUubGVuZ3RoO1xuICAgIHdoaWxlKGxlbikge1xuICAgICAgICBjdXJyZW50UXVldWUgPSBxdWV1ZTtcbiAgICAgICAgcXVldWUgPSBbXTtcbiAgICAgICAgd2hpbGUgKCsrcXVldWVJbmRleCA8IGxlbikge1xuICAgICAgICAgICAgaWYgKGN1cnJlbnRRdWV1ZSkge1xuICAgICAgICAgICAgICAgIGN1cnJlbnRRdWV1ZVtxdWV1ZUluZGV4XS5ydW4oKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgICBxdWV1ZUluZGV4ID0gLTE7XG4gICAgICAgIGxlbiA9IHF1ZXVlLmxlbmd0aDtcbiAgICB9XG4gICAgY3VycmVudFF1ZXVlID0gbnVsbDtcbiAgICBkcmFpbmluZyA9IGZhbHNlO1xuICAgIGNhY2hlZENsZWFyVGltZW91dC5jYWxsKG51bGwsIHRpbWVvdXQpO1xufVxuXG5wcm9jZXNzLm5leHRUaWNrID0gZnVuY3Rpb24gKGZ1bikge1xuICAgIHZhciBhcmdzID0gbmV3IEFycmF5KGFyZ3VtZW50cy5sZW5ndGggLSAxKTtcbiAgICBpZiAoYXJndW1lbnRzLmxlbmd0aCA+IDEpIHtcbiAgICAgICAgZm9yICh2YXIgaSA9IDE7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgICAgIGFyZ3NbaSAtIDFdID0gYXJndW1lbnRzW2ldO1xuICAgICAgICB9XG4gICAgfVxuICAgIHF1ZXVlLnB1c2gobmV3IEl0ZW0oZnVuLCBhcmdzKSk7XG4gICAgaWYgKHF1ZXVlLmxlbmd0aCA9PT0gMSAmJiAhZHJhaW5pbmcpIHtcbiAgICAgICAgY2FjaGVkU2V0VGltZW91dC5jYWxsKG51bGwsIGRyYWluUXVldWUsIDApO1xuICAgIH1cbn07XG5cbi8vIHY4IGxpa2VzIHByZWRpY3RpYmxlIG9iamVjdHNcbmZ1bmN0aW9uIEl0ZW0oZnVuLCBhcnJheSkge1xuICAgIHRoaXMuZnVuID0gZnVuO1xuICAgIHRoaXMuYXJyYXkgPSBhcnJheTtcbn1cbkl0ZW0ucHJvdG90eXBlLnJ1biA9IGZ1bmN0aW9uICgpIHtcbiAgICB0aGlzLmZ1bi5hcHBseShudWxsLCB0aGlzLmFycmF5KTtcbn07XG5wcm9jZXNzLnRpdGxlID0gJ2Jyb3dzZXInO1xucHJvY2Vzcy5icm93c2VyID0gdHJ1ZTtcbnByb2Nlc3MuZW52ID0ge307XG5wcm9jZXNzLmFyZ3YgPSBbXTtcbnByb2Nlc3MudmVyc2lvbiA9ICcnOyAvLyBlbXB0eSBzdHJpbmcgdG8gYXZvaWQgcmVnZXhwIGlzc3Vlc1xucHJvY2Vzcy52ZXJzaW9ucyA9IHt9O1xuXG5mdW5jdGlvbiBub29wKCkge31cblxucHJvY2Vzcy5vbiA9IG5vb3A7XG5wcm9jZXNzLmFkZExpc3RlbmVyID0gbm9vcDtcbnByb2Nlc3Mub25jZSA9IG5vb3A7XG5wcm9jZXNzLm9mZiA9IG5vb3A7XG5wcm9jZXNzLnJlbW92ZUxpc3RlbmVyID0gbm9vcDtcbnByb2Nlc3MucmVtb3ZlQWxsTGlzdGVuZXJzID0gbm9vcDtcbnByb2Nlc3MuZW1pdCA9IG5vb3A7XG5cbnByb2Nlc3MuYmluZGluZyA9IGZ1bmN0aW9uIChuYW1lKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdwcm9jZXNzLmJpbmRpbmcgaXMgbm90IHN1cHBvcnRlZCcpO1xufTtcblxucHJvY2Vzcy5jd2QgPSBmdW5jdGlvbiAoKSB7IHJldHVybiAnLycgfTtcbnByb2Nlc3MuY2hkaXIgPSBmdW5jdGlvbiAoZGlyKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdwcm9jZXNzLmNoZGlyIGlzIG5vdCBzdXBwb3J0ZWQnKTtcbn07XG5wcm9jZXNzLnVtYXNrID0gZnVuY3Rpb24oKSB7IHJldHVybiAwOyB9O1xuIiwiLyohXG4gIENvcHlyaWdodCAoYykgMjAxNiBKZWQgV2F0c29uLlxuICBMaWNlbnNlZCB1bmRlciB0aGUgTUlUIExpY2Vuc2UgKE1JVCksIHNlZVxuICBodHRwOi8vamVkd2F0c29uLmdpdGh1Yi5pby9jbGFzc25hbWVzXG4qL1xuLyogZ2xvYmFsIGRlZmluZSAqL1xuXG4oZnVuY3Rpb24gKCkge1xuXHQndXNlIHN0cmljdCc7XG5cblx0dmFyIGhhc093biA9IHt9Lmhhc093blByb3BlcnR5O1xuXG5cdGZ1bmN0aW9uIGNsYXNzTmFtZXMgKCkge1xuXHRcdHZhciBjbGFzc2VzID0gW107XG5cblx0XHRmb3IgKHZhciBpID0gMDsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykge1xuXHRcdFx0dmFyIGFyZyA9IGFyZ3VtZW50c1tpXTtcblx0XHRcdGlmICghYXJnKSBjb250aW51ZTtcblxuXHRcdFx0dmFyIGFyZ1R5cGUgPSB0eXBlb2YgYXJnO1xuXG5cdFx0XHRpZiAoYXJnVHlwZSA9PT0gJ3N0cmluZycgfHwgYXJnVHlwZSA9PT0gJ251bWJlcicpIHtcblx0XHRcdFx0Y2xhc3Nlcy5wdXNoKGFyZyk7XG5cdFx0XHR9IGVsc2UgaWYgKEFycmF5LmlzQXJyYXkoYXJnKSkge1xuXHRcdFx0XHRjbGFzc2VzLnB1c2goY2xhc3NOYW1lcy5hcHBseShudWxsLCBhcmcpKTtcblx0XHRcdH0gZWxzZSBpZiAoYXJnVHlwZSA9PT0gJ29iamVjdCcpIHtcblx0XHRcdFx0Zm9yICh2YXIga2V5IGluIGFyZykge1xuXHRcdFx0XHRcdGlmIChoYXNPd24uY2FsbChhcmcsIGtleSkgJiYgYXJnW2tleV0pIHtcblx0XHRcdFx0XHRcdGNsYXNzZXMucHVzaChrZXkpO1xuXHRcdFx0XHRcdH1cblx0XHRcdFx0fVxuXHRcdFx0fVxuXHRcdH1cblxuXHRcdHJldHVybiBjbGFzc2VzLmpvaW4oJyAnKTtcblx0fVxuXG5cdGlmICh0eXBlb2YgbW9kdWxlICE9PSAndW5kZWZpbmVkJyAmJiBtb2R1bGUuZXhwb3J0cykge1xuXHRcdG1vZHVsZS5leHBvcnRzID0gY2xhc3NOYW1lcztcblx0fSBlbHNlIGlmICh0eXBlb2YgZGVmaW5lID09PSAnZnVuY3Rpb24nICYmIHR5cGVvZiBkZWZpbmUuYW1kID09PSAnb2JqZWN0JyAmJiBkZWZpbmUuYW1kKSB7XG5cdFx0Ly8gcmVnaXN0ZXIgYXMgJ2NsYXNzbmFtZXMnLCBjb25zaXN0ZW50IHdpdGggbnBtIHBhY2thZ2UgbmFtZVxuXHRcdGRlZmluZSgnY2xhc3NuYW1lcycsIFtdLCBmdW5jdGlvbiAoKSB7XG5cdFx0XHRyZXR1cm4gY2xhc3NOYW1lcztcblx0XHR9KTtcblx0fSBlbHNlIHtcblx0XHR3aW5kb3cuY2xhc3NOYW1lcyA9IGNsYXNzTmFtZXM7XG5cdH1cbn0oKSk7XG4iLCJ2YXIgaXNGdW5jdGlvbiA9IHJlcXVpcmUoJ2lzLWZ1bmN0aW9uJylcblxubW9kdWxlLmV4cG9ydHMgPSBmb3JFYWNoXG5cbnZhciB0b1N0cmluZyA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmdcbnZhciBoYXNPd25Qcm9wZXJ0eSA9IE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHlcblxuZnVuY3Rpb24gZm9yRWFjaChsaXN0LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGlmICghaXNGdW5jdGlvbihpdGVyYXRvcikpIHtcbiAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignaXRlcmF0b3IgbXVzdCBiZSBhIGZ1bmN0aW9uJylcbiAgICB9XG5cbiAgICBpZiAoYXJndW1lbnRzLmxlbmd0aCA8IDMpIHtcbiAgICAgICAgY29udGV4dCA9IHRoaXNcbiAgICB9XG4gICAgXG4gICAgaWYgKHRvU3RyaW5nLmNhbGwobGlzdCkgPT09ICdbb2JqZWN0IEFycmF5XScpXG4gICAgICAgIGZvckVhY2hBcnJheShsaXN0LCBpdGVyYXRvciwgY29udGV4dClcbiAgICBlbHNlIGlmICh0eXBlb2YgbGlzdCA9PT0gJ3N0cmluZycpXG4gICAgICAgIGZvckVhY2hTdHJpbmcobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpXG4gICAgZWxzZVxuICAgICAgICBmb3JFYWNoT2JqZWN0KGxpc3QsIGl0ZXJhdG9yLCBjb250ZXh0KVxufVxuXG5mdW5jdGlvbiBmb3JFYWNoQXJyYXkoYXJyYXksIGl0ZXJhdG9yLCBjb250ZXh0KSB7XG4gICAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IGFycmF5Lmxlbmd0aDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKGFycmF5LCBpKSkge1xuICAgICAgICAgICAgaXRlcmF0b3IuY2FsbChjb250ZXh0LCBhcnJheVtpXSwgaSwgYXJyYXkpXG4gICAgICAgIH1cbiAgICB9XG59XG5cbmZ1bmN0aW9uIGZvckVhY2hTdHJpbmcoc3RyaW5nLCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBzdHJpbmcubGVuZ3RoOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgLy8gbm8gc3VjaCB0aGluZyBhcyBhIHNwYXJzZSBzdHJpbmcuXG4gICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgc3RyaW5nLmNoYXJBdChpKSwgaSwgc3RyaW5nKVxuICAgIH1cbn1cblxuZnVuY3Rpb24gZm9yRWFjaE9iamVjdChvYmplY3QsIGl0ZXJhdG9yLCBjb250ZXh0KSB7XG4gICAgZm9yICh2YXIgayBpbiBvYmplY3QpIHtcbiAgICAgICAgaWYgKGhhc093blByb3BlcnR5LmNhbGwob2JqZWN0LCBrKSkge1xuICAgICAgICAgICAgaXRlcmF0b3IuY2FsbChjb250ZXh0LCBvYmplY3Rba10sIGssIG9iamVjdClcbiAgICAgICAgfVxuICAgIH1cbn1cbiIsImlmICh0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSB3aW5kb3c7XG59IGVsc2UgaWYgKHR5cGVvZiBnbG9iYWwgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICBtb2R1bGUuZXhwb3J0cyA9IGdsb2JhbDtcbn0gZWxzZSBpZiAodHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIpe1xuICAgIG1vZHVsZS5leHBvcnRzID0gc2VsZjtcbn0gZWxzZSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSB7fTtcbn1cbiIsIm1vZHVsZS5leHBvcnRzID0gaXNGdW5jdGlvblxuXG52YXIgdG9TdHJpbmcgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nXG5cbmZ1bmN0aW9uIGlzRnVuY3Rpb24gKGZuKSB7XG4gIHZhciBzdHJpbmcgPSB0b1N0cmluZy5jYWxsKGZuKVxuICByZXR1cm4gc3RyaW5nID09PSAnW29iamVjdCBGdW5jdGlvbl0nIHx8XG4gICAgKHR5cGVvZiBmbiA9PT0gJ2Z1bmN0aW9uJyAmJiBzdHJpbmcgIT09ICdbb2JqZWN0IFJlZ0V4cF0nKSB8fFxuICAgICh0eXBlb2Ygd2luZG93ICE9PSAndW5kZWZpbmVkJyAmJlxuICAgICAvLyBJRTggYW5kIGJlbG93XG4gICAgIChmbiA9PT0gd2luZG93LnNldFRpbWVvdXQgfHxcbiAgICAgIGZuID09PSB3aW5kb3cuYWxlcnQgfHxcbiAgICAgIGZuID09PSB3aW5kb3cuY29uZmlybSB8fFxuICAgICAgZm4gPT09IHdpbmRvdy5wcm9tcHQpKVxufTtcbiIsIid1c2Ugc3RyaWN0JztcbnZhciB0b1N0cmluZyA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmc7XG5cbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24gKHgpIHtcblx0dmFyIHByb3RvdHlwZTtcblx0cmV0dXJuIHRvU3RyaW5nLmNhbGwoeCkgPT09ICdbb2JqZWN0IE9iamVjdF0nICYmIChwcm90b3R5cGUgPSBPYmplY3QuZ2V0UHJvdG90eXBlT2YoeCksIHByb3RvdHlwZSA9PT0gbnVsbCB8fCBwcm90b3R5cGUgPT09IE9iamVjdC5nZXRQcm90b3R5cGVPZih7fSkpO1xufTtcbiIsInZhciBvdmVyQXJnID0gcmVxdWlyZSgnLi9fb3ZlckFyZycpO1xuXG4vKiBCdWlsdC1pbiBtZXRob2QgcmVmZXJlbmNlcyBmb3IgdGhvc2Ugd2l0aCB0aGUgc2FtZSBuYW1lIGFzIG90aGVyIGBsb2Rhc2hgIG1ldGhvZHMuICovXG52YXIgbmF0aXZlR2V0UHJvdG90eXBlID0gT2JqZWN0LmdldFByb3RvdHlwZU9mO1xuXG4vKipcbiAqIEdldHMgdGhlIGBbW1Byb3RvdHlwZV1dYCBvZiBgdmFsdWVgLlxuICpcbiAqIEBwcml2YXRlXG4gKiBAcGFyYW0geyp9IHZhbHVlIFRoZSB2YWx1ZSB0byBxdWVyeS5cbiAqIEByZXR1cm5zIHtudWxsfE9iamVjdH0gUmV0dXJucyB0aGUgYFtbUHJvdG90eXBlXV1gLlxuICovXG52YXIgZ2V0UHJvdG90eXBlID0gb3ZlckFyZyhuYXRpdmVHZXRQcm90b3R5cGUsIE9iamVjdCk7XG5cbm1vZHVsZS5leHBvcnRzID0gZ2V0UHJvdG90eXBlO1xuIiwiLyoqXG4gKiBDaGVja3MgaWYgYHZhbHVlYCBpcyBhIGhvc3Qgb2JqZWN0IGluIElFIDwgOS5cbiAqXG4gKiBAcHJpdmF0ZVxuICogQHBhcmFtIHsqfSB2YWx1ZSBUaGUgdmFsdWUgdG8gY2hlY2suXG4gKiBAcmV0dXJucyB7Ym9vbGVhbn0gUmV0dXJucyBgdHJ1ZWAgaWYgYHZhbHVlYCBpcyBhIGhvc3Qgb2JqZWN0LCBlbHNlIGBmYWxzZWAuXG4gKi9cbmZ1bmN0aW9uIGlzSG9zdE9iamVjdCh2YWx1ZSkge1xuICAvLyBNYW55IGhvc3Qgb2JqZWN0cyBhcmUgYE9iamVjdGAgb2JqZWN0cyB0aGF0IGNhbiBjb2VyY2UgdG8gc3RyaW5nc1xuICAvLyBkZXNwaXRlIGhhdmluZyBpbXByb3Blcmx5IGRlZmluZWQgYHRvU3RyaW5nYCBtZXRob2RzLlxuICB2YXIgcmVzdWx0ID0gZmFsc2U7XG4gIGlmICh2YWx1ZSAhPSBudWxsICYmIHR5cGVvZiB2YWx1ZS50b1N0cmluZyAhPSAnZnVuY3Rpb24nKSB7XG4gICAgdHJ5IHtcbiAgICAgIHJlc3VsdCA9ICEhKHZhbHVlICsgJycpO1xuICAgIH0gY2F0Y2ggKGUpIHt9XG4gIH1cbiAgcmV0dXJuIHJlc3VsdDtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBpc0hvc3RPYmplY3Q7XG4iLCIvKipcbiAqIENyZWF0ZXMgYSBmdW5jdGlvbiB0aGF0IGludm9rZXMgYGZ1bmNgIHdpdGggaXRzIGZpcnN0IGFyZ3VtZW50IHRyYW5zZm9ybWVkLlxuICpcbiAqIEBwcml2YXRlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBmdW5jIFRoZSBmdW5jdGlvbiB0byB3cmFwLlxuICogQHBhcmFtIHtGdW5jdGlvbn0gdHJhbnNmb3JtIFRoZSBhcmd1bWVudCB0cmFuc2Zvcm0uXG4gKiBAcmV0dXJucyB7RnVuY3Rpb259IFJldHVybnMgdGhlIG5ldyBmdW5jdGlvbi5cbiAqL1xuZnVuY3Rpb24gb3ZlckFyZyhmdW5jLCB0cmFuc2Zvcm0pIHtcbiAgcmV0dXJuIGZ1bmN0aW9uKGFyZykge1xuICAgIHJldHVybiBmdW5jKHRyYW5zZm9ybShhcmcpKTtcbiAgfTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBvdmVyQXJnO1xuIiwiLyoqXG4gKiBDaGVja3MgaWYgYHZhbHVlYCBpcyBvYmplY3QtbGlrZS4gQSB2YWx1ZSBpcyBvYmplY3QtbGlrZSBpZiBpdCdzIG5vdCBgbnVsbGBcbiAqIGFuZCBoYXMgYSBgdHlwZW9mYCByZXN1bHQgb2YgXCJvYmplY3RcIi5cbiAqXG4gKiBAc3RhdGljXG4gKiBAbWVtYmVyT2YgX1xuICogQHNpbmNlIDQuMC4wXG4gKiBAY2F0ZWdvcnkgTGFuZ1xuICogQHBhcmFtIHsqfSB2YWx1ZSBUaGUgdmFsdWUgdG8gY2hlY2suXG4gKiBAcmV0dXJucyB7Ym9vbGVhbn0gUmV0dXJucyBgdHJ1ZWAgaWYgYHZhbHVlYCBpcyBvYmplY3QtbGlrZSwgZWxzZSBgZmFsc2VgLlxuICogQGV4YW1wbGVcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZSh7fSk7XG4gKiAvLyA9PiB0cnVlXG4gKlxuICogXy5pc09iamVjdExpa2UoWzEsIDIsIDNdKTtcbiAqIC8vID0+IHRydWVcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZShfLm5vb3ApO1xuICogLy8gPT4gZmFsc2VcbiAqXG4gKiBfLmlzT2JqZWN0TGlrZShudWxsKTtcbiAqIC8vID0+IGZhbHNlXG4gKi9cbmZ1bmN0aW9uIGlzT2JqZWN0TGlrZSh2YWx1ZSkge1xuICByZXR1cm4gISF2YWx1ZSAmJiB0eXBlb2YgdmFsdWUgPT0gJ29iamVjdCc7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gaXNPYmplY3RMaWtlO1xuIiwidmFyIGdldFByb3RvdHlwZSA9IHJlcXVpcmUoJy4vX2dldFByb3RvdHlwZScpLFxuICAgIGlzSG9zdE9iamVjdCA9IHJlcXVpcmUoJy4vX2lzSG9zdE9iamVjdCcpLFxuICAgIGlzT2JqZWN0TGlrZSA9IHJlcXVpcmUoJy4vaXNPYmplY3RMaWtlJyk7XG5cbi8qKiBgT2JqZWN0I3RvU3RyaW5nYCByZXN1bHQgcmVmZXJlbmNlcy4gKi9cbnZhciBvYmplY3RUYWcgPSAnW29iamVjdCBPYmplY3RdJztcblxuLyoqIFVzZWQgZm9yIGJ1aWx0LWluIG1ldGhvZCByZWZlcmVuY2VzLiAqL1xudmFyIG9iamVjdFByb3RvID0gT2JqZWN0LnByb3RvdHlwZTtcblxuLyoqIFVzZWQgdG8gcmVzb2x2ZSB0aGUgZGVjb21waWxlZCBzb3VyY2Ugb2YgZnVuY3Rpb25zLiAqL1xudmFyIGZ1bmNUb1N0cmluZyA9IEZ1bmN0aW9uLnByb3RvdHlwZS50b1N0cmluZztcblxuLyoqIFVzZWQgdG8gY2hlY2sgb2JqZWN0cyBmb3Igb3duIHByb3BlcnRpZXMuICovXG52YXIgaGFzT3duUHJvcGVydHkgPSBvYmplY3RQcm90by5oYXNPd25Qcm9wZXJ0eTtcblxuLyoqIFVzZWQgdG8gaW5mZXIgdGhlIGBPYmplY3RgIGNvbnN0cnVjdG9yLiAqL1xudmFyIG9iamVjdEN0b3JTdHJpbmcgPSBmdW5jVG9TdHJpbmcuY2FsbChPYmplY3QpO1xuXG4vKipcbiAqIFVzZWQgdG8gcmVzb2x2ZSB0aGVcbiAqIFtgdG9TdHJpbmdUYWdgXShodHRwOi8vZWNtYS1pbnRlcm5hdGlvbmFsLm9yZy9lY21hLTI2Mi82LjAvI3NlYy1vYmplY3QucHJvdG90eXBlLnRvc3RyaW5nKVxuICogb2YgdmFsdWVzLlxuICovXG52YXIgb2JqZWN0VG9TdHJpbmcgPSBvYmplY3RQcm90by50b1N0cmluZztcblxuLyoqXG4gKiBDaGVja3MgaWYgYHZhbHVlYCBpcyBhIHBsYWluIG9iamVjdCwgdGhhdCBpcywgYW4gb2JqZWN0IGNyZWF0ZWQgYnkgdGhlXG4gKiBgT2JqZWN0YCBjb25zdHJ1Y3RvciBvciBvbmUgd2l0aCBhIGBbW1Byb3RvdHlwZV1dYCBvZiBgbnVsbGAuXG4gKlxuICogQHN0YXRpY1xuICogQG1lbWJlck9mIF9cbiAqIEBzaW5jZSAwLjguMFxuICogQGNhdGVnb3J5IExhbmdcbiAqIEBwYXJhbSB7Kn0gdmFsdWUgVGhlIHZhbHVlIHRvIGNoZWNrLlxuICogQHJldHVybnMge2Jvb2xlYW59IFJldHVybnMgYHRydWVgIGlmIGB2YWx1ZWAgaXMgYSBwbGFpbiBvYmplY3QsXG4gKiAgZWxzZSBgZmFsc2VgLlxuICogQGV4YW1wbGVcbiAqXG4gKiBmdW5jdGlvbiBGb28oKSB7XG4gKiAgIHRoaXMuYSA9IDE7XG4gKiB9XG4gKlxuICogXy5pc1BsYWluT2JqZWN0KG5ldyBGb28pO1xuICogLy8gPT4gZmFsc2VcbiAqXG4gKiBfLmlzUGxhaW5PYmplY3QoWzEsIDIsIDNdKTtcbiAqIC8vID0+IGZhbHNlXG4gKlxuICogXy5pc1BsYWluT2JqZWN0KHsgJ3gnOiAwLCAneSc6IDAgfSk7XG4gKiAvLyA9PiB0cnVlXG4gKlxuICogXy5pc1BsYWluT2JqZWN0KE9iamVjdC5jcmVhdGUobnVsbCkpO1xuICogLy8gPT4gdHJ1ZVxuICovXG5mdW5jdGlvbiBpc1BsYWluT2JqZWN0KHZhbHVlKSB7XG4gIGlmICghaXNPYmplY3RMaWtlKHZhbHVlKSB8fFxuICAgICAgb2JqZWN0VG9TdHJpbmcuY2FsbCh2YWx1ZSkgIT0gb2JqZWN0VGFnIHx8IGlzSG9zdE9iamVjdCh2YWx1ZSkpIHtcbiAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgdmFyIHByb3RvID0gZ2V0UHJvdG90eXBlKHZhbHVlKTtcbiAgaWYgKHByb3RvID09PSBudWxsKSB7XG4gICAgcmV0dXJuIHRydWU7XG4gIH1cbiAgdmFyIEN0b3IgPSBoYXNPd25Qcm9wZXJ0eS5jYWxsKHByb3RvLCAnY29uc3RydWN0b3InKSAmJiBwcm90by5jb25zdHJ1Y3RvcjtcbiAgcmV0dXJuICh0eXBlb2YgQ3RvciA9PSAnZnVuY3Rpb24nICYmXG4gICAgQ3RvciBpbnN0YW5jZW9mIEN0b3IgJiYgZnVuY1RvU3RyaW5nLmNhbGwoQ3RvcikgPT0gb2JqZWN0Q3RvclN0cmluZyk7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gaXNQbGFpbk9iamVjdDtcbiIsIid1c2Ugc3RyaWN0JztcbnZhciBpc09wdGlvbk9iamVjdCA9IHJlcXVpcmUoJ2lzLXBsYWluLW9iaicpO1xudmFyIGhhc093blByb3BlcnR5ID0gT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eTtcbnZhciBwcm9wSXNFbnVtZXJhYmxlID0gT2JqZWN0LnByb3BlcnR5SXNFbnVtZXJhYmxlO1xudmFyIGdsb2JhbFRoaXMgPSB0aGlzO1xudmFyIGRlZmF1bHRNZXJnZU9wdHMgPSB7XG5cdGNvbmNhdEFycmF5czogZmFsc2Vcbn07XG5cbmZ1bmN0aW9uIGdldEVudW1lcmFibGVPd25Qcm9wZXJ0eUtleXModmFsdWUpIHtcblx0dmFyIGtleXMgPSBbXTtcblxuXHRmb3IgKHZhciBrZXkgaW4gdmFsdWUpIHtcblx0XHRpZiAoaGFzT3duUHJvcGVydHkuY2FsbCh2YWx1ZSwga2V5KSkge1xuXHRcdFx0a2V5cy5wdXNoKGtleSk7XG5cdFx0fVxuXHR9XG5cblx0aWYgKE9iamVjdC5nZXRPd25Qcm9wZXJ0eVN5bWJvbHMpIHtcblx0XHR2YXIgc3ltYm9scyA9IE9iamVjdC5nZXRPd25Qcm9wZXJ0eVN5bWJvbHModmFsdWUpO1xuXG5cdFx0Zm9yICh2YXIgaSA9IDA7IGkgPCBzeW1ib2xzLmxlbmd0aDsgaSsrKSB7XG5cdFx0XHRpZiAocHJvcElzRW51bWVyYWJsZS5jYWxsKHZhbHVlLCBzeW1ib2xzW2ldKSkge1xuXHRcdFx0XHRrZXlzLnB1c2goc3ltYm9sc1tpXSk7XG5cdFx0XHR9XG5cdFx0fVxuXHR9XG5cblx0cmV0dXJuIGtleXM7XG59XG5cbmZ1bmN0aW9uIGNsb25lKHZhbHVlKSB7XG5cdGlmIChBcnJheS5pc0FycmF5KHZhbHVlKSkge1xuXHRcdHJldHVybiBjbG9uZUFycmF5KHZhbHVlKTtcblx0fVxuXG5cdGlmIChpc09wdGlvbk9iamVjdCh2YWx1ZSkpIHtcblx0XHRyZXR1cm4gY2xvbmVPcHRpb25PYmplY3QodmFsdWUpO1xuXHR9XG5cblx0cmV0dXJuIHZhbHVlO1xufVxuXG5mdW5jdGlvbiBjbG9uZUFycmF5KGFycmF5KSB7XG5cdHZhciByZXN1bHQgPSBhcnJheS5zbGljZSgwLCAwKTtcblxuXHRnZXRFbnVtZXJhYmxlT3duUHJvcGVydHlLZXlzKGFycmF5KS5mb3JFYWNoKGZ1bmN0aW9uIChrZXkpIHtcblx0XHRyZXN1bHRba2V5XSA9IGNsb25lKGFycmF5W2tleV0pO1xuXHR9KTtcblxuXHRyZXR1cm4gcmVzdWx0O1xufVxuXG5mdW5jdGlvbiBjbG9uZU9wdGlvbk9iamVjdChvYmopIHtcblx0dmFyIHJlc3VsdCA9IE9iamVjdC5nZXRQcm90b3R5cGVPZihvYmopID09PSBudWxsID8gT2JqZWN0LmNyZWF0ZShudWxsKSA6IHt9O1xuXG5cdGdldEVudW1lcmFibGVPd25Qcm9wZXJ0eUtleXMob2JqKS5mb3JFYWNoKGZ1bmN0aW9uIChrZXkpIHtcblx0XHRyZXN1bHRba2V5XSA9IGNsb25lKG9ialtrZXldKTtcblx0fSk7XG5cblx0cmV0dXJuIHJlc3VsdDtcbn1cblxuLyoqXG4gKiBAcGFyYW0gbWVyZ2VkIHthbHJlYWR5IGNsb25lZH1cbiAqIEByZXR1cm4ge2Nsb25lZCBPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIG1lcmdlS2V5cyhtZXJnZWQsIHNvdXJjZSwga2V5cywgbWVyZ2VPcHRzKSB7XG5cdGtleXMuZm9yRWFjaChmdW5jdGlvbiAoa2V5KSB7XG5cdFx0aWYgKGtleSBpbiBtZXJnZWQpIHtcblx0XHRcdG1lcmdlZFtrZXldID0gbWVyZ2UobWVyZ2VkW2tleV0sIHNvdXJjZVtrZXldLCBtZXJnZU9wdHMpO1xuXHRcdH0gZWxzZSB7XG5cdFx0XHRtZXJnZWRba2V5XSA9IGNsb25lKHNvdXJjZVtrZXldKTtcblx0XHR9XG5cdH0pO1xuXG5cdHJldHVybiBtZXJnZWQ7XG59XG5cbi8qKlxuICogQHBhcmFtIG1lcmdlZCB7YWxyZWFkeSBjbG9uZWR9XG4gKiBAcmV0dXJuIHtjbG9uZWQgT2JqZWN0fVxuICpcbiAqIHNlZSBbQXJyYXkucHJvdG90eXBlLmNvbmNhdCAoIC4uLmFyZ3VtZW50cyApXShodHRwOi8vd3d3LmVjbWEtaW50ZXJuYXRpb25hbC5vcmcvZWNtYS0yNjIvNi4wLyNzZWMtYXJyYXkucHJvdG90eXBlLmNvbmNhdClcbiAqL1xuZnVuY3Rpb24gY29uY2F0QXJyYXlzKG1lcmdlZCwgc291cmNlLCBtZXJnZU9wdHMpIHtcblx0dmFyIHJlc3VsdCA9IG1lcmdlZC5zbGljZSgwLCAwKTtcblx0dmFyIHJlc3VsdEluZGV4ID0gMDtcblxuXHRbbWVyZ2VkLCBzb3VyY2VdLmZvckVhY2goZnVuY3Rpb24gKGFycmF5KSB7XG5cdFx0dmFyIGluZGljZXMgPSBbXTtcblxuXHRcdC8vIHJlc3VsdC5jb25jYXQoYXJyYXkpIHdpdGggY2xvbmluZ1xuXHRcdGZvciAodmFyIGsgPSAwOyBrIDwgYXJyYXkubGVuZ3RoOyBrKyspIHtcblx0XHRcdGlmICghaGFzT3duUHJvcGVydHkuY2FsbChhcnJheSwgaykpIHtcblx0XHRcdFx0Y29udGludWU7XG5cdFx0XHR9XG5cblx0XHRcdGluZGljZXMucHVzaChTdHJpbmcoaykpO1xuXG5cdFx0XHRpZiAoYXJyYXkgPT09IG1lcmdlZCkge1xuXHRcdFx0XHQvLyBhbHJlYWR5IGNsb25lZFxuXHRcdFx0XHRyZXN1bHRbcmVzdWx0SW5kZXgrK10gPSBhcnJheVtrXTtcblx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdHJlc3VsdFtyZXN1bHRJbmRleCsrXSA9IGNsb25lKGFycmF5W2tdKTtcblx0XHRcdH1cblx0XHR9XG5cblx0XHQvLyBtZXJnZSBub24taW5kZXgga2V5c1xuXHRcdHJlc3VsdCA9IG1lcmdlS2V5cyhyZXN1bHQsIGFycmF5LCBnZXRFbnVtZXJhYmxlT3duUHJvcGVydHlLZXlzKGFycmF5KS5maWx0ZXIoZnVuY3Rpb24gKGtleSkge1xuXHRcdFx0cmV0dXJuIGluZGljZXMuaW5kZXhPZihrZXkpID09PSAtMTtcblx0XHR9KSwgbWVyZ2VPcHRzKTtcblx0fSk7XG5cblx0cmV0dXJuIHJlc3VsdDtcbn1cblxuLyoqXG4gKiBAcGFyYW0gbWVyZ2VkIHthbHJlYWR5IGNsb25lZH1cbiAqIEByZXR1cm4ge2Nsb25lZCBPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIG1lcmdlKG1lcmdlZCwgc291cmNlLCBtZXJnZU9wdHMpIHtcblx0aWYgKG1lcmdlT3B0cy5jb25jYXRBcnJheXMgJiYgQXJyYXkuaXNBcnJheShtZXJnZWQpICYmIEFycmF5LmlzQXJyYXkoc291cmNlKSkge1xuXHRcdHJldHVybiBjb25jYXRBcnJheXMobWVyZ2VkLCBzb3VyY2UsIG1lcmdlT3B0cyk7XG5cdH1cblxuXHRpZiAoIWlzT3B0aW9uT2JqZWN0KHNvdXJjZSkgfHwgIWlzT3B0aW9uT2JqZWN0KG1lcmdlZCkpIHtcblx0XHRyZXR1cm4gY2xvbmUoc291cmNlKTtcblx0fVxuXG5cdHJldHVybiBtZXJnZUtleXMobWVyZ2VkLCBzb3VyY2UsIGdldEVudW1lcmFibGVPd25Qcm9wZXJ0eUtleXMoc291cmNlKSwgbWVyZ2VPcHRzKTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiAoKSB7XG5cdHZhciBtZXJnZU9wdHMgPSBtZXJnZShjbG9uZShkZWZhdWx0TWVyZ2VPcHRzKSwgKHRoaXMgIT09IGdsb2JhbFRoaXMgJiYgdGhpcykgfHwge30sIGRlZmF1bHRNZXJnZU9wdHMpO1xuXHR2YXIgbWVyZ2VkID0ge307XG5cblx0Zm9yICh2YXIgaSA9IDA7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcblx0XHR2YXIgb3B0aW9uID0gYXJndW1lbnRzW2ldO1xuXG5cdFx0aWYgKG9wdGlvbiA9PT0gdW5kZWZpbmVkKSB7XG5cdFx0XHRjb250aW51ZTtcblx0XHR9XG5cblx0XHRpZiAoIWlzT3B0aW9uT2JqZWN0KG9wdGlvbikpIHtcblx0XHRcdHRocm93IG5ldyBUeXBlRXJyb3IoJ2AnICsgb3B0aW9uICsgJ2AgaXMgbm90IGFuIE9wdGlvbiBPYmplY3QnKTtcblx0XHR9XG5cblx0XHRtZXJnZWQgPSBtZXJnZShtZXJnZWQsIG9wdGlvbiwgbWVyZ2VPcHRzKTtcblx0fVxuXG5cdHJldHVybiBtZXJnZWQ7XG59O1xuIiwidmFyIHRyaW0gPSByZXF1aXJlKCd0cmltJylcbiAgLCBmb3JFYWNoID0gcmVxdWlyZSgnZm9yLWVhY2gnKVxuICAsIGlzQXJyYXkgPSBmdW5jdGlvbihhcmcpIHtcbiAgICAgIHJldHVybiBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwoYXJnKSA9PT0gJ1tvYmplY3QgQXJyYXldJztcbiAgICB9XG5cbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24gKGhlYWRlcnMpIHtcbiAgaWYgKCFoZWFkZXJzKVxuICAgIHJldHVybiB7fVxuXG4gIHZhciByZXN1bHQgPSB7fVxuXG4gIGZvckVhY2goXG4gICAgICB0cmltKGhlYWRlcnMpLnNwbGl0KCdcXG4nKVxuICAgICwgZnVuY3Rpb24gKHJvdykge1xuICAgICAgICB2YXIgaW5kZXggPSByb3cuaW5kZXhPZignOicpXG4gICAgICAgICAgLCBrZXkgPSB0cmltKHJvdy5zbGljZSgwLCBpbmRleCkpLnRvTG93ZXJDYXNlKClcbiAgICAgICAgICAsIHZhbHVlID0gdHJpbShyb3cuc2xpY2UoaW5kZXggKyAxKSlcblxuICAgICAgICBpZiAodHlwZW9mKHJlc3VsdFtrZXldKSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgICAgICByZXN1bHRba2V5XSA9IHZhbHVlXG4gICAgICAgIH0gZWxzZSBpZiAoaXNBcnJheShyZXN1bHRba2V5XSkpIHtcbiAgICAgICAgICByZXN1bHRba2V5XS5wdXNoKHZhbHVlKVxuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHJlc3VsdFtrZXldID0gWyByZXN1bHRba2V5XSwgdmFsdWUgXVxuICAgICAgICB9XG4gICAgICB9XG4gIClcblxuICByZXR1cm4gcmVzdWx0XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5mdW5jdGlvbiB0aHVua01pZGRsZXdhcmUoX3JlZikge1xuICB2YXIgZGlzcGF0Y2ggPSBfcmVmLmRpc3BhdGNoO1xuICB2YXIgZ2V0U3RhdGUgPSBfcmVmLmdldFN0YXRlO1xuXG4gIHJldHVybiBmdW5jdGlvbiAobmV4dCkge1xuICAgIHJldHVybiBmdW5jdGlvbiAoYWN0aW9uKSB7XG4gICAgICByZXR1cm4gdHlwZW9mIGFjdGlvbiA9PT0gJ2Z1bmN0aW9uJyA/IGFjdGlvbihkaXNwYXRjaCwgZ2V0U3RhdGUpIDogbmV4dChhY3Rpb24pO1xuICAgIH07XG4gIH07XG59XG5cbm1vZHVsZS5leHBvcnRzID0gdGh1bmtNaWRkbGV3YXJlOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcblxudmFyIF9leHRlbmRzID0gT2JqZWN0LmFzc2lnbiB8fCBmdW5jdGlvbiAodGFyZ2V0KSB7IGZvciAodmFyIGkgPSAxOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7IHZhciBzb3VyY2UgPSBhcmd1bWVudHNbaV07IGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHsgaWYgKE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHkuY2FsbChzb3VyY2UsIGtleSkpIHsgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XTsgfSB9IH0gcmV0dXJuIHRhcmdldDsgfTtcblxuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBhcHBseU1pZGRsZXdhcmU7XG5cbnZhciBfY29tcG9zZSA9IHJlcXVpcmUoJy4vY29tcG9zZScpO1xuXG52YXIgX2NvbXBvc2UyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29tcG9zZSk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG4vKipcbiAqIENyZWF0ZXMgYSBzdG9yZSBlbmhhbmNlciB0aGF0IGFwcGxpZXMgbWlkZGxld2FyZSB0byB0aGUgZGlzcGF0Y2ggbWV0aG9kXG4gKiBvZiB0aGUgUmVkdXggc3RvcmUuIFRoaXMgaXMgaGFuZHkgZm9yIGEgdmFyaWV0eSBvZiB0YXNrcywgc3VjaCBhcyBleHByZXNzaW5nXG4gKiBhc3luY2hyb25vdXMgYWN0aW9ucyBpbiBhIGNvbmNpc2UgbWFubmVyLCBvciBsb2dnaW5nIGV2ZXJ5IGFjdGlvbiBwYXlsb2FkLlxuICpcbiAqIFNlZSBgcmVkdXgtdGh1bmtgIHBhY2thZ2UgYXMgYW4gZXhhbXBsZSBvZiB0aGUgUmVkdXggbWlkZGxld2FyZS5cbiAqXG4gKiBCZWNhdXNlIG1pZGRsZXdhcmUgaXMgcG90ZW50aWFsbHkgYXN5bmNocm9ub3VzLCB0aGlzIHNob3VsZCBiZSB0aGUgZmlyc3RcbiAqIHN0b3JlIGVuaGFuY2VyIGluIHRoZSBjb21wb3NpdGlvbiBjaGFpbi5cbiAqXG4gKiBOb3RlIHRoYXQgZWFjaCBtaWRkbGV3YXJlIHdpbGwgYmUgZ2l2ZW4gdGhlIGBkaXNwYXRjaGAgYW5kIGBnZXRTdGF0ZWAgZnVuY3Rpb25zXG4gKiBhcyBuYW1lZCBhcmd1bWVudHMuXG4gKlxuICogQHBhcmFtIHsuLi5GdW5jdGlvbn0gbWlkZGxld2FyZXMgVGhlIG1pZGRsZXdhcmUgY2hhaW4gdG8gYmUgYXBwbGllZC5cbiAqIEByZXR1cm5zIHtGdW5jdGlvbn0gQSBzdG9yZSBlbmhhbmNlciBhcHBseWluZyB0aGUgbWlkZGxld2FyZS5cbiAqL1xuZnVuY3Rpb24gYXBwbHlNaWRkbGV3YXJlKCkge1xuICBmb3IgKHZhciBfbGVuID0gYXJndW1lbnRzLmxlbmd0aCwgbWlkZGxld2FyZXMgPSBBcnJheShfbGVuKSwgX2tleSA9IDA7IF9rZXkgPCBfbGVuOyBfa2V5KyspIHtcbiAgICBtaWRkbGV3YXJlc1tfa2V5XSA9IGFyZ3VtZW50c1tfa2V5XTtcbiAgfVxuXG4gIHJldHVybiBmdW5jdGlvbiAoY3JlYXRlU3RvcmUpIHtcbiAgICByZXR1cm4gZnVuY3Rpb24gKHJlZHVjZXIsIGluaXRpYWxTdGF0ZSwgZW5oYW5jZXIpIHtcbiAgICAgIHZhciBzdG9yZSA9IGNyZWF0ZVN0b3JlKHJlZHVjZXIsIGluaXRpYWxTdGF0ZSwgZW5oYW5jZXIpO1xuICAgICAgdmFyIF9kaXNwYXRjaCA9IHN0b3JlLmRpc3BhdGNoO1xuICAgICAgdmFyIGNoYWluID0gW107XG5cbiAgICAgIHZhciBtaWRkbGV3YXJlQVBJID0ge1xuICAgICAgICBnZXRTdGF0ZTogc3RvcmUuZ2V0U3RhdGUsXG4gICAgICAgIGRpc3BhdGNoOiBmdW5jdGlvbiBkaXNwYXRjaChhY3Rpb24pIHtcbiAgICAgICAgICByZXR1cm4gX2Rpc3BhdGNoKGFjdGlvbik7XG4gICAgICAgIH1cbiAgICAgIH07XG4gICAgICBjaGFpbiA9IG1pZGRsZXdhcmVzLm1hcChmdW5jdGlvbiAobWlkZGxld2FyZSkge1xuICAgICAgICByZXR1cm4gbWlkZGxld2FyZShtaWRkbGV3YXJlQVBJKTtcbiAgICAgIH0pO1xuICAgICAgX2Rpc3BhdGNoID0gX2NvbXBvc2UyW1wiZGVmYXVsdFwiXS5hcHBseSh1bmRlZmluZWQsIGNoYWluKShzdG9yZS5kaXNwYXRjaCk7XG5cbiAgICAgIHJldHVybiBfZXh0ZW5kcyh7fSwgc3RvcmUsIHtcbiAgICAgICAgZGlzcGF0Y2g6IF9kaXNwYXRjaFxuICAgICAgfSk7XG4gICAgfTtcbiAgfTtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGJpbmRBY3Rpb25DcmVhdG9ycztcbmZ1bmN0aW9uIGJpbmRBY3Rpb25DcmVhdG9yKGFjdGlvbkNyZWF0b3IsIGRpc3BhdGNoKSB7XG4gIHJldHVybiBmdW5jdGlvbiAoKSB7XG4gICAgcmV0dXJuIGRpc3BhdGNoKGFjdGlvbkNyZWF0b3IuYXBwbHkodW5kZWZpbmVkLCBhcmd1bWVudHMpKTtcbiAgfTtcbn1cblxuLyoqXG4gKiBUdXJucyBhbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGFyZSBhY3Rpb24gY3JlYXRvcnMsIGludG8gYW4gb2JqZWN0IHdpdGggdGhlXG4gKiBzYW1lIGtleXMsIGJ1dCB3aXRoIGV2ZXJ5IGZ1bmN0aW9uIHdyYXBwZWQgaW50byBhIGBkaXNwYXRjaGAgY2FsbCBzbyB0aGV5XG4gKiBtYXkgYmUgaW52b2tlZCBkaXJlY3RseS4gVGhpcyBpcyBqdXN0IGEgY29udmVuaWVuY2UgbWV0aG9kLCBhcyB5b3UgY2FuIGNhbGxcbiAqIGBzdG9yZS5kaXNwYXRjaChNeUFjdGlvbkNyZWF0b3JzLmRvU29tZXRoaW5nKCkpYCB5b3Vyc2VsZiBqdXN0IGZpbmUuXG4gKlxuICogRm9yIGNvbnZlbmllbmNlLCB5b3UgY2FuIGFsc28gcGFzcyBhIHNpbmdsZSBmdW5jdGlvbiBhcyB0aGUgZmlyc3QgYXJndW1lbnQsXG4gKiBhbmQgZ2V0IGEgZnVuY3Rpb24gaW4gcmV0dXJuLlxuICpcbiAqIEBwYXJhbSB7RnVuY3Rpb258T2JqZWN0fSBhY3Rpb25DcmVhdG9ycyBBbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGFyZSBhY3Rpb25cbiAqIGNyZWF0b3IgZnVuY3Rpb25zLiBPbmUgaGFuZHkgd2F5IHRvIG9idGFpbiBpdCBpcyB0byB1c2UgRVM2IGBpbXBvcnQgKiBhc2BcbiAqIHN5bnRheC4gWW91IG1heSBhbHNvIHBhc3MgYSBzaW5nbGUgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtGdW5jdGlvbn0gZGlzcGF0Y2ggVGhlIGBkaXNwYXRjaGAgZnVuY3Rpb24gYXZhaWxhYmxlIG9uIHlvdXIgUmVkdXhcbiAqIHN0b3JlLlxuICpcbiAqIEByZXR1cm5zIHtGdW5jdGlvbnxPYmplY3R9IFRoZSBvYmplY3QgbWltaWNraW5nIHRoZSBvcmlnaW5hbCBvYmplY3QsIGJ1dCB3aXRoXG4gKiBldmVyeSBhY3Rpb24gY3JlYXRvciB3cmFwcGVkIGludG8gdGhlIGBkaXNwYXRjaGAgY2FsbC4gSWYgeW91IHBhc3NlZCBhXG4gKiBmdW5jdGlvbiBhcyBgYWN0aW9uQ3JlYXRvcnNgLCB0aGUgcmV0dXJuIHZhbHVlIHdpbGwgYWxzbyBiZSBhIHNpbmdsZVxuICogZnVuY3Rpb24uXG4gKi9cbmZ1bmN0aW9uIGJpbmRBY3Rpb25DcmVhdG9ycyhhY3Rpb25DcmVhdG9ycywgZGlzcGF0Y2gpIHtcbiAgaWYgKHR5cGVvZiBhY3Rpb25DcmVhdG9ycyA9PT0gJ2Z1bmN0aW9uJykge1xuICAgIHJldHVybiBiaW5kQWN0aW9uQ3JlYXRvcihhY3Rpb25DcmVhdG9ycywgZGlzcGF0Y2gpO1xuICB9XG5cbiAgaWYgKHR5cGVvZiBhY3Rpb25DcmVhdG9ycyAhPT0gJ29iamVjdCcgfHwgYWN0aW9uQ3JlYXRvcnMgPT09IG51bGwpIHtcbiAgICB0aHJvdyBuZXcgRXJyb3IoJ2JpbmRBY3Rpb25DcmVhdG9ycyBleHBlY3RlZCBhbiBvYmplY3Qgb3IgYSBmdW5jdGlvbiwgaW5zdGVhZCByZWNlaXZlZCAnICsgKGFjdGlvbkNyZWF0b3JzID09PSBudWxsID8gJ251bGwnIDogdHlwZW9mIGFjdGlvbkNyZWF0b3JzKSArICcuICcgKyAnRGlkIHlvdSB3cml0ZSBcImltcG9ydCBBY3Rpb25DcmVhdG9ycyBmcm9tXCIgaW5zdGVhZCBvZiBcImltcG9ydCAqIGFzIEFjdGlvbkNyZWF0b3JzIGZyb21cIj8nKTtcbiAgfVxuXG4gIHZhciBrZXlzID0gT2JqZWN0LmtleXMoYWN0aW9uQ3JlYXRvcnMpO1xuICB2YXIgYm91bmRBY3Rpb25DcmVhdG9ycyA9IHt9O1xuICBmb3IgKHZhciBpID0gMDsgaSA8IGtleXMubGVuZ3RoOyBpKyspIHtcbiAgICB2YXIga2V5ID0ga2V5c1tpXTtcbiAgICB2YXIgYWN0aW9uQ3JlYXRvciA9IGFjdGlvbkNyZWF0b3JzW2tleV07XG4gICAgaWYgKHR5cGVvZiBhY3Rpb25DcmVhdG9yID09PSAnZnVuY3Rpb24nKSB7XG4gICAgICBib3VuZEFjdGlvbkNyZWF0b3JzW2tleV0gPSBiaW5kQWN0aW9uQ3JlYXRvcihhY3Rpb25DcmVhdG9yLCBkaXNwYXRjaCk7XG4gICAgfVxuICB9XG4gIHJldHVybiBib3VuZEFjdGlvbkNyZWF0b3JzO1xufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gY29tYmluZVJlZHVjZXJzO1xuXG52YXIgX2NyZWF0ZVN0b3JlID0gcmVxdWlyZSgnLi9jcmVhdGVTdG9yZScpO1xuXG52YXIgX2lzUGxhaW5PYmplY3QgPSByZXF1aXJlKCdsb2Rhc2gvaXNQbGFpbk9iamVjdCcpO1xuXG52YXIgX2lzUGxhaW5PYmplY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaXNQbGFpbk9iamVjdCk7XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJy4vdXRpbHMvd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG5mdW5jdGlvbiBnZXRVbmRlZmluZWRTdGF0ZUVycm9yTWVzc2FnZShrZXksIGFjdGlvbikge1xuICB2YXIgYWN0aW9uVHlwZSA9IGFjdGlvbiAmJiBhY3Rpb24udHlwZTtcbiAgdmFyIGFjdGlvbk5hbWUgPSBhY3Rpb25UeXBlICYmICdcIicgKyBhY3Rpb25UeXBlLnRvU3RyaW5nKCkgKyAnXCInIHx8ICdhbiBhY3Rpb24nO1xuXG4gIHJldHVybiAnR2l2ZW4gYWN0aW9uICcgKyBhY3Rpb25OYW1lICsgJywgcmVkdWNlciBcIicgKyBrZXkgKyAnXCIgcmV0dXJuZWQgdW5kZWZpbmVkLiAnICsgJ1RvIGlnbm9yZSBhbiBhY3Rpb24sIHlvdSBtdXN0IGV4cGxpY2l0bHkgcmV0dXJuIHRoZSBwcmV2aW91cyBzdGF0ZS4nO1xufVxuXG5mdW5jdGlvbiBnZXRVbmV4cGVjdGVkU3RhdGVTaGFwZVdhcm5pbmdNZXNzYWdlKGlucHV0U3RhdGUsIHJlZHVjZXJzLCBhY3Rpb24pIHtcbiAgdmFyIHJlZHVjZXJLZXlzID0gT2JqZWN0LmtleXMocmVkdWNlcnMpO1xuICB2YXIgYXJndW1lbnROYW1lID0gYWN0aW9uICYmIGFjdGlvbi50eXBlID09PSBfY3JlYXRlU3RvcmUuQWN0aW9uVHlwZXMuSU5JVCA/ICdpbml0aWFsU3RhdGUgYXJndW1lbnQgcGFzc2VkIHRvIGNyZWF0ZVN0b3JlJyA6ICdwcmV2aW91cyBzdGF0ZSByZWNlaXZlZCBieSB0aGUgcmVkdWNlcic7XG5cbiAgaWYgKHJlZHVjZXJLZXlzLmxlbmd0aCA9PT0gMCkge1xuICAgIHJldHVybiAnU3RvcmUgZG9lcyBub3QgaGF2ZSBhIHZhbGlkIHJlZHVjZXIuIE1ha2Ugc3VyZSB0aGUgYXJndW1lbnQgcGFzc2VkICcgKyAndG8gY29tYmluZVJlZHVjZXJzIGlzIGFuIG9iamVjdCB3aG9zZSB2YWx1ZXMgYXJlIHJlZHVjZXJzLic7XG4gIH1cblxuICBpZiAoISgwLCBfaXNQbGFpbk9iamVjdDJbXCJkZWZhdWx0XCJdKShpbnB1dFN0YXRlKSkge1xuICAgIHJldHVybiAnVGhlICcgKyBhcmd1bWVudE5hbWUgKyAnIGhhcyB1bmV4cGVjdGVkIHR5cGUgb2YgXCInICsge30udG9TdHJpbmcuY2FsbChpbnB1dFN0YXRlKS5tYXRjaCgvXFxzKFthLXp8QS1aXSspLylbMV0gKyAnXCIuIEV4cGVjdGVkIGFyZ3VtZW50IHRvIGJlIGFuIG9iamVjdCB3aXRoIHRoZSBmb2xsb3dpbmcgJyArICgna2V5czogXCInICsgcmVkdWNlcktleXMuam9pbignXCIsIFwiJykgKyAnXCInKTtcbiAgfVxuXG4gIHZhciB1bmV4cGVjdGVkS2V5cyA9IE9iamVjdC5rZXlzKGlucHV0U3RhdGUpLmZpbHRlcihmdW5jdGlvbiAoa2V5KSB7XG4gICAgcmV0dXJuICFyZWR1Y2Vycy5oYXNPd25Qcm9wZXJ0eShrZXkpO1xuICB9KTtcblxuICBpZiAodW5leHBlY3RlZEtleXMubGVuZ3RoID4gMCkge1xuICAgIHJldHVybiAnVW5leHBlY3RlZCAnICsgKHVuZXhwZWN0ZWRLZXlzLmxlbmd0aCA+IDEgPyAna2V5cycgOiAna2V5JykgKyAnICcgKyAoJ1wiJyArIHVuZXhwZWN0ZWRLZXlzLmpvaW4oJ1wiLCBcIicpICsgJ1wiIGZvdW5kIGluICcgKyBhcmd1bWVudE5hbWUgKyAnLiAnKSArICdFeHBlY3RlZCB0byBmaW5kIG9uZSBvZiB0aGUga25vd24gcmVkdWNlciBrZXlzIGluc3RlYWQ6ICcgKyAoJ1wiJyArIHJlZHVjZXJLZXlzLmpvaW4oJ1wiLCBcIicpICsgJ1wiLiBVbmV4cGVjdGVkIGtleXMgd2lsbCBiZSBpZ25vcmVkLicpO1xuICB9XG59XG5cbmZ1bmN0aW9uIGFzc2VydFJlZHVjZXJTYW5pdHkocmVkdWNlcnMpIHtcbiAgT2JqZWN0LmtleXMocmVkdWNlcnMpLmZvckVhY2goZnVuY3Rpb24gKGtleSkge1xuICAgIHZhciByZWR1Y2VyID0gcmVkdWNlcnNba2V5XTtcbiAgICB2YXIgaW5pdGlhbFN0YXRlID0gcmVkdWNlcih1bmRlZmluZWQsIHsgdHlwZTogX2NyZWF0ZVN0b3JlLkFjdGlvblR5cGVzLklOSVQgfSk7XG5cbiAgICBpZiAodHlwZW9mIGluaXRpYWxTdGF0ZSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignUmVkdWNlciBcIicgKyBrZXkgKyAnXCIgcmV0dXJuZWQgdW5kZWZpbmVkIGR1cmluZyBpbml0aWFsaXphdGlvbi4gJyArICdJZiB0aGUgc3RhdGUgcGFzc2VkIHRvIHRoZSByZWR1Y2VyIGlzIHVuZGVmaW5lZCwgeW91IG11c3QgJyArICdleHBsaWNpdGx5IHJldHVybiB0aGUgaW5pdGlhbCBzdGF0ZS4gVGhlIGluaXRpYWwgc3RhdGUgbWF5ICcgKyAnbm90IGJlIHVuZGVmaW5lZC4nKTtcbiAgICB9XG5cbiAgICB2YXIgdHlwZSA9ICdAQHJlZHV4L1BST0JFX1VOS05PV05fQUNUSU9OXycgKyBNYXRoLnJhbmRvbSgpLnRvU3RyaW5nKDM2KS5zdWJzdHJpbmcoNykuc3BsaXQoJycpLmpvaW4oJy4nKTtcbiAgICBpZiAodHlwZW9mIHJlZHVjZXIodW5kZWZpbmVkLCB7IHR5cGU6IHR5cGUgfSkgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1JlZHVjZXIgXCInICsga2V5ICsgJ1wiIHJldHVybmVkIHVuZGVmaW5lZCB3aGVuIHByb2JlZCB3aXRoIGEgcmFuZG9tIHR5cGUuICcgKyAoJ0RvblxcJ3QgdHJ5IHRvIGhhbmRsZSAnICsgX2NyZWF0ZVN0b3JlLkFjdGlvblR5cGVzLklOSVQgKyAnIG9yIG90aGVyIGFjdGlvbnMgaW4gXCJyZWR1eC8qXCIgJykgKyAnbmFtZXNwYWNlLiBUaGV5IGFyZSBjb25zaWRlcmVkIHByaXZhdGUuIEluc3RlYWQsIHlvdSBtdXN0IHJldHVybiB0aGUgJyArICdjdXJyZW50IHN0YXRlIGZvciBhbnkgdW5rbm93biBhY3Rpb25zLCB1bmxlc3MgaXQgaXMgdW5kZWZpbmVkLCAnICsgJ2luIHdoaWNoIGNhc2UgeW91IG11c3QgcmV0dXJuIHRoZSBpbml0aWFsIHN0YXRlLCByZWdhcmRsZXNzIG9mIHRoZSAnICsgJ2FjdGlvbiB0eXBlLiBUaGUgaW5pdGlhbCBzdGF0ZSBtYXkgbm90IGJlIHVuZGVmaW5lZC4nKTtcbiAgICB9XG4gIH0pO1xufVxuXG4vKipcbiAqIFR1cm5zIGFuIG9iamVjdCB3aG9zZSB2YWx1ZXMgYXJlIGRpZmZlcmVudCByZWR1Y2VyIGZ1bmN0aW9ucywgaW50byBhIHNpbmdsZVxuICogcmVkdWNlciBmdW5jdGlvbi4gSXQgd2lsbCBjYWxsIGV2ZXJ5IGNoaWxkIHJlZHVjZXIsIGFuZCBnYXRoZXIgdGhlaXIgcmVzdWx0c1xuICogaW50byBhIHNpbmdsZSBzdGF0ZSBvYmplY3QsIHdob3NlIGtleXMgY29ycmVzcG9uZCB0byB0aGUga2V5cyBvZiB0aGUgcGFzc2VkXG4gKiByZWR1Y2VyIGZ1bmN0aW9ucy5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gcmVkdWNlcnMgQW4gb2JqZWN0IHdob3NlIHZhbHVlcyBjb3JyZXNwb25kIHRvIGRpZmZlcmVudFxuICogcmVkdWNlciBmdW5jdGlvbnMgdGhhdCBuZWVkIHRvIGJlIGNvbWJpbmVkIGludG8gb25lLiBPbmUgaGFuZHkgd2F5IHRvIG9idGFpblxuICogaXQgaXMgdG8gdXNlIEVTNiBgaW1wb3J0ICogYXMgcmVkdWNlcnNgIHN5bnRheC4gVGhlIHJlZHVjZXJzIG1heSBuZXZlciByZXR1cm5cbiAqIHVuZGVmaW5lZCBmb3IgYW55IGFjdGlvbi4gSW5zdGVhZCwgdGhleSBzaG91bGQgcmV0dXJuIHRoZWlyIGluaXRpYWwgc3RhdGVcbiAqIGlmIHRoZSBzdGF0ZSBwYXNzZWQgdG8gdGhlbSB3YXMgdW5kZWZpbmVkLCBhbmQgdGhlIGN1cnJlbnQgc3RhdGUgZm9yIGFueVxuICogdW5yZWNvZ25pemVkIGFjdGlvbi5cbiAqXG4gKiBAcmV0dXJucyB7RnVuY3Rpb259IEEgcmVkdWNlciBmdW5jdGlvbiB0aGF0IGludm9rZXMgZXZlcnkgcmVkdWNlciBpbnNpZGUgdGhlXG4gKiBwYXNzZWQgb2JqZWN0LCBhbmQgYnVpbGRzIGEgc3RhdGUgb2JqZWN0IHdpdGggdGhlIHNhbWUgc2hhcGUuXG4gKi9cbmZ1bmN0aW9uIGNvbWJpbmVSZWR1Y2VycyhyZWR1Y2Vycykge1xuICB2YXIgcmVkdWNlcktleXMgPSBPYmplY3Qua2V5cyhyZWR1Y2Vycyk7XG4gIHZhciBmaW5hbFJlZHVjZXJzID0ge307XG4gIGZvciAodmFyIGkgPSAwOyBpIDwgcmVkdWNlcktleXMubGVuZ3RoOyBpKyspIHtcbiAgICB2YXIga2V5ID0gcmVkdWNlcktleXNbaV07XG4gICAgaWYgKHR5cGVvZiByZWR1Y2Vyc1trZXldID09PSAnZnVuY3Rpb24nKSB7XG4gICAgICBmaW5hbFJlZHVjZXJzW2tleV0gPSByZWR1Y2Vyc1trZXldO1xuICAgIH1cbiAgfVxuICB2YXIgZmluYWxSZWR1Y2VyS2V5cyA9IE9iamVjdC5rZXlzKGZpbmFsUmVkdWNlcnMpO1xuXG4gIHZhciBzYW5pdHlFcnJvcjtcbiAgdHJ5IHtcbiAgICBhc3NlcnRSZWR1Y2VyU2FuaXR5KGZpbmFsUmVkdWNlcnMpO1xuICB9IGNhdGNoIChlKSB7XG4gICAgc2FuaXR5RXJyb3IgPSBlO1xuICB9XG5cbiAgcmV0dXJuIGZ1bmN0aW9uIGNvbWJpbmF0aW9uKCkge1xuICAgIHZhciBzdGF0ZSA9IGFyZ3VtZW50cy5sZW5ndGggPD0gMCB8fCBhcmd1bWVudHNbMF0gPT09IHVuZGVmaW5lZCA/IHt9IDogYXJndW1lbnRzWzBdO1xuICAgIHZhciBhY3Rpb24gPSBhcmd1bWVudHNbMV07XG5cbiAgICBpZiAoc2FuaXR5RXJyb3IpIHtcbiAgICAgIHRocm93IHNhbml0eUVycm9yO1xuICAgIH1cblxuICAgIGlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nKSB7XG4gICAgICB2YXIgd2FybmluZ01lc3NhZ2UgPSBnZXRVbmV4cGVjdGVkU3RhdGVTaGFwZVdhcm5pbmdNZXNzYWdlKHN0YXRlLCBmaW5hbFJlZHVjZXJzLCBhY3Rpb24pO1xuICAgICAgaWYgKHdhcm5pbmdNZXNzYWdlKSB7XG4gICAgICAgICgwLCBfd2FybmluZzJbXCJkZWZhdWx0XCJdKSh3YXJuaW5nTWVzc2FnZSk7XG4gICAgICB9XG4gICAgfVxuXG4gICAgdmFyIGhhc0NoYW5nZWQgPSBmYWxzZTtcbiAgICB2YXIgbmV4dFN0YXRlID0ge307XG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBmaW5hbFJlZHVjZXJLZXlzLmxlbmd0aDsgaSsrKSB7XG4gICAgICB2YXIga2V5ID0gZmluYWxSZWR1Y2VyS2V5c1tpXTtcbiAgICAgIHZhciByZWR1Y2VyID0gZmluYWxSZWR1Y2Vyc1trZXldO1xuICAgICAgdmFyIHByZXZpb3VzU3RhdGVGb3JLZXkgPSBzdGF0ZVtrZXldO1xuICAgICAgdmFyIG5leHRTdGF0ZUZvcktleSA9IHJlZHVjZXIocHJldmlvdXNTdGF0ZUZvcktleSwgYWN0aW9uKTtcbiAgICAgIGlmICh0eXBlb2YgbmV4dFN0YXRlRm9yS2V5ID09PSAndW5kZWZpbmVkJykge1xuICAgICAgICB2YXIgZXJyb3JNZXNzYWdlID0gZ2V0VW5kZWZpbmVkU3RhdGVFcnJvck1lc3NhZ2Uoa2V5LCBhY3Rpb24pO1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3IoZXJyb3JNZXNzYWdlKTtcbiAgICAgIH1cbiAgICAgIG5leHRTdGF0ZVtrZXldID0gbmV4dFN0YXRlRm9yS2V5O1xuICAgICAgaGFzQ2hhbmdlZCA9IGhhc0NoYW5nZWQgfHwgbmV4dFN0YXRlRm9yS2V5ICE9PSBwcmV2aW91c1N0YXRlRm9yS2V5O1xuICAgIH1cbiAgICByZXR1cm4gaGFzQ2hhbmdlZCA/IG5leHRTdGF0ZSA6IHN0YXRlO1xuICB9O1xufSIsIlwidXNlIHN0cmljdFwiO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBjb21wb3NlO1xuLyoqXG4gKiBDb21wb3NlcyBzaW5nbGUtYXJndW1lbnQgZnVuY3Rpb25zIGZyb20gcmlnaHQgdG8gbGVmdC4gVGhlIHJpZ2h0bW9zdFxuICogZnVuY3Rpb24gY2FuIHRha2UgbXVsdGlwbGUgYXJndW1lbnRzIGFzIGl0IHByb3ZpZGVzIHRoZSBzaWduYXR1cmUgZm9yXG4gKiB0aGUgcmVzdWx0aW5nIGNvbXBvc2l0ZSBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0gey4uLkZ1bmN0aW9ufSBmdW5jcyBUaGUgZnVuY3Rpb25zIHRvIGNvbXBvc2UuXG4gKiBAcmV0dXJucyB7RnVuY3Rpb259IEEgZnVuY3Rpb24gb2J0YWluZWQgYnkgY29tcG9zaW5nIHRoZSBhcmd1bWVudCBmdW5jdGlvbnNcbiAqIGZyb20gcmlnaHQgdG8gbGVmdC4gRm9yIGV4YW1wbGUsIGNvbXBvc2UoZiwgZywgaCkgaXMgaWRlbnRpY2FsIHRvIGRvaW5nXG4gKiAoLi4uYXJncykgPT4gZihnKGgoLi4uYXJncykpKS5cbiAqL1xuXG5mdW5jdGlvbiBjb21wb3NlKCkge1xuICBmb3IgKHZhciBfbGVuID0gYXJndW1lbnRzLmxlbmd0aCwgZnVuY3MgPSBBcnJheShfbGVuKSwgX2tleSA9IDA7IF9rZXkgPCBfbGVuOyBfa2V5KyspIHtcbiAgICBmdW5jc1tfa2V5XSA9IGFyZ3VtZW50c1tfa2V5XTtcbiAgfVxuXG4gIGlmIChmdW5jcy5sZW5ndGggPT09IDApIHtcbiAgICByZXR1cm4gZnVuY3Rpb24gKGFyZykge1xuICAgICAgcmV0dXJuIGFyZztcbiAgICB9O1xuICB9IGVsc2Uge1xuICAgIHZhciBfcmV0ID0gZnVuY3Rpb24gKCkge1xuICAgICAgdmFyIGxhc3QgPSBmdW5jc1tmdW5jcy5sZW5ndGggLSAxXTtcbiAgICAgIHZhciByZXN0ID0gZnVuY3Muc2xpY2UoMCwgLTEpO1xuICAgICAgcmV0dXJuIHtcbiAgICAgICAgdjogZnVuY3Rpb24gdigpIHtcbiAgICAgICAgICByZXR1cm4gcmVzdC5yZWR1Y2VSaWdodChmdW5jdGlvbiAoY29tcG9zZWQsIGYpIHtcbiAgICAgICAgICAgIHJldHVybiBmKGNvbXBvc2VkKTtcbiAgICAgICAgICB9LCBsYXN0LmFwcGx5KHVuZGVmaW5lZCwgYXJndW1lbnRzKSk7XG4gICAgICAgIH1cbiAgICAgIH07XG4gICAgfSgpO1xuXG4gICAgaWYgKHR5cGVvZiBfcmV0ID09PSBcIm9iamVjdFwiKSByZXR1cm4gX3JldC52O1xuICB9XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5BY3Rpb25UeXBlcyA9IHVuZGVmaW5lZDtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gY3JlYXRlU3RvcmU7XG5cbnZhciBfaXNQbGFpbk9iamVjdCA9IHJlcXVpcmUoJ2xvZGFzaC9pc1BsYWluT2JqZWN0Jyk7XG5cbnZhciBfaXNQbGFpbk9iamVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9pc1BsYWluT2JqZWN0KTtcblxudmFyIF9zeW1ib2xPYnNlcnZhYmxlID0gcmVxdWlyZSgnc3ltYm9sLW9ic2VydmFibGUnKTtcblxudmFyIF9zeW1ib2xPYnNlcnZhYmxlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3N5bWJvbE9ic2VydmFibGUpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuLyoqXG4gKiBUaGVzZSBhcmUgcHJpdmF0ZSBhY3Rpb24gdHlwZXMgcmVzZXJ2ZWQgYnkgUmVkdXguXG4gKiBGb3IgYW55IHVua25vd24gYWN0aW9ucywgeW91IG11c3QgcmV0dXJuIHRoZSBjdXJyZW50IHN0YXRlLlxuICogSWYgdGhlIGN1cnJlbnQgc3RhdGUgaXMgdW5kZWZpbmVkLCB5b3UgbXVzdCByZXR1cm4gdGhlIGluaXRpYWwgc3RhdGUuXG4gKiBEbyBub3QgcmVmZXJlbmNlIHRoZXNlIGFjdGlvbiB0eXBlcyBkaXJlY3RseSBpbiB5b3VyIGNvZGUuXG4gKi9cbnZhciBBY3Rpb25UeXBlcyA9IGV4cG9ydHMuQWN0aW9uVHlwZXMgPSB7XG4gIElOSVQ6ICdAQHJlZHV4L0lOSVQnXG59O1xuXG4vKipcbiAqIENyZWF0ZXMgYSBSZWR1eCBzdG9yZSB0aGF0IGhvbGRzIHRoZSBzdGF0ZSB0cmVlLlxuICogVGhlIG9ubHkgd2F5IHRvIGNoYW5nZSB0aGUgZGF0YSBpbiB0aGUgc3RvcmUgaXMgdG8gY2FsbCBgZGlzcGF0Y2goKWAgb24gaXQuXG4gKlxuICogVGhlcmUgc2hvdWxkIG9ubHkgYmUgYSBzaW5nbGUgc3RvcmUgaW4geW91ciBhcHAuIFRvIHNwZWNpZnkgaG93IGRpZmZlcmVudFxuICogcGFydHMgb2YgdGhlIHN0YXRlIHRyZWUgcmVzcG9uZCB0byBhY3Rpb25zLCB5b3UgbWF5IGNvbWJpbmUgc2V2ZXJhbCByZWR1Y2Vyc1xuICogaW50byBhIHNpbmdsZSByZWR1Y2VyIGZ1bmN0aW9uIGJ5IHVzaW5nIGBjb21iaW5lUmVkdWNlcnNgLlxuICpcbiAqIEBwYXJhbSB7RnVuY3Rpb259IHJlZHVjZXIgQSBmdW5jdGlvbiB0aGF0IHJldHVybnMgdGhlIG5leHQgc3RhdGUgdHJlZSwgZ2l2ZW5cbiAqIHRoZSBjdXJyZW50IHN0YXRlIHRyZWUgYW5kIHRoZSBhY3Rpb24gdG8gaGFuZGxlLlxuICpcbiAqIEBwYXJhbSB7YW55fSBbaW5pdGlhbFN0YXRlXSBUaGUgaW5pdGlhbCBzdGF0ZS4gWW91IG1heSBvcHRpb25hbGx5IHNwZWNpZnkgaXRcbiAqIHRvIGh5ZHJhdGUgdGhlIHN0YXRlIGZyb20gdGhlIHNlcnZlciBpbiB1bml2ZXJzYWwgYXBwcywgb3IgdG8gcmVzdG9yZSBhXG4gKiBwcmV2aW91c2x5IHNlcmlhbGl6ZWQgdXNlciBzZXNzaW9uLlxuICogSWYgeW91IHVzZSBgY29tYmluZVJlZHVjZXJzYCB0byBwcm9kdWNlIHRoZSByb290IHJlZHVjZXIgZnVuY3Rpb24sIHRoaXMgbXVzdCBiZVxuICogYW4gb2JqZWN0IHdpdGggdGhlIHNhbWUgc2hhcGUgYXMgYGNvbWJpbmVSZWR1Y2Vyc2Aga2V5cy5cbiAqXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBlbmhhbmNlciBUaGUgc3RvcmUgZW5oYW5jZXIuIFlvdSBtYXkgb3B0aW9uYWxseSBzcGVjaWZ5IGl0XG4gKiB0byBlbmhhbmNlIHRoZSBzdG9yZSB3aXRoIHRoaXJkLXBhcnR5IGNhcGFiaWxpdGllcyBzdWNoIGFzIG1pZGRsZXdhcmUsXG4gKiB0aW1lIHRyYXZlbCwgcGVyc2lzdGVuY2UsIGV0Yy4gVGhlIG9ubHkgc3RvcmUgZW5oYW5jZXIgdGhhdCBzaGlwcyB3aXRoIFJlZHV4XG4gKiBpcyBgYXBwbHlNaWRkbGV3YXJlKClgLlxuICpcbiAqIEByZXR1cm5zIHtTdG9yZX0gQSBSZWR1eCBzdG9yZSB0aGF0IGxldHMgeW91IHJlYWQgdGhlIHN0YXRlLCBkaXNwYXRjaCBhY3Rpb25zXG4gKiBhbmQgc3Vic2NyaWJlIHRvIGNoYW5nZXMuXG4gKi9cbmZ1bmN0aW9uIGNyZWF0ZVN0b3JlKHJlZHVjZXIsIGluaXRpYWxTdGF0ZSwgZW5oYW5jZXIpIHtcbiAgdmFyIF9yZWYyO1xuXG4gIGlmICh0eXBlb2YgaW5pdGlhbFN0YXRlID09PSAnZnVuY3Rpb24nICYmIHR5cGVvZiBlbmhhbmNlciA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICBlbmhhbmNlciA9IGluaXRpYWxTdGF0ZTtcbiAgICBpbml0aWFsU3RhdGUgPSB1bmRlZmluZWQ7XG4gIH1cblxuICBpZiAodHlwZW9mIGVuaGFuY2VyICE9PSAndW5kZWZpbmVkJykge1xuICAgIGlmICh0eXBlb2YgZW5oYW5jZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgdGhlIGVuaGFuY2VyIHRvIGJlIGEgZnVuY3Rpb24uJyk7XG4gICAgfVxuXG4gICAgcmV0dXJuIGVuaGFuY2VyKGNyZWF0ZVN0b3JlKShyZWR1Y2VyLCBpbml0aWFsU3RhdGUpO1xuICB9XG5cbiAgaWYgKHR5cGVvZiByZWR1Y2VyICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdFeHBlY3RlZCB0aGUgcmVkdWNlciB0byBiZSBhIGZ1bmN0aW9uLicpO1xuICB9XG5cbiAgdmFyIGN1cnJlbnRSZWR1Y2VyID0gcmVkdWNlcjtcbiAgdmFyIGN1cnJlbnRTdGF0ZSA9IGluaXRpYWxTdGF0ZTtcbiAgdmFyIGN1cnJlbnRMaXN0ZW5lcnMgPSBbXTtcbiAgdmFyIG5leHRMaXN0ZW5lcnMgPSBjdXJyZW50TGlzdGVuZXJzO1xuICB2YXIgaXNEaXNwYXRjaGluZyA9IGZhbHNlO1xuXG4gIGZ1bmN0aW9uIGVuc3VyZUNhbk11dGF0ZU5leHRMaXN0ZW5lcnMoKSB7XG4gICAgaWYgKG5leHRMaXN0ZW5lcnMgPT09IGN1cnJlbnRMaXN0ZW5lcnMpIHtcbiAgICAgIG5leHRMaXN0ZW5lcnMgPSBjdXJyZW50TGlzdGVuZXJzLnNsaWNlKCk7XG4gICAgfVxuICB9XG5cbiAgLyoqXG4gICAqIFJlYWRzIHRoZSBzdGF0ZSB0cmVlIG1hbmFnZWQgYnkgdGhlIHN0b3JlLlxuICAgKlxuICAgKiBAcmV0dXJucyB7YW55fSBUaGUgY3VycmVudCBzdGF0ZSB0cmVlIG9mIHlvdXIgYXBwbGljYXRpb24uXG4gICAqL1xuICBmdW5jdGlvbiBnZXRTdGF0ZSgpIHtcbiAgICByZXR1cm4gY3VycmVudFN0YXRlO1xuICB9XG5cbiAgLyoqXG4gICAqIEFkZHMgYSBjaGFuZ2UgbGlzdGVuZXIuIEl0IHdpbGwgYmUgY2FsbGVkIGFueSB0aW1lIGFuIGFjdGlvbiBpcyBkaXNwYXRjaGVkLFxuICAgKiBhbmQgc29tZSBwYXJ0IG9mIHRoZSBzdGF0ZSB0cmVlIG1heSBwb3RlbnRpYWxseSBoYXZlIGNoYW5nZWQuIFlvdSBtYXkgdGhlblxuICAgKiBjYWxsIGBnZXRTdGF0ZSgpYCB0byByZWFkIHRoZSBjdXJyZW50IHN0YXRlIHRyZWUgaW5zaWRlIHRoZSBjYWxsYmFjay5cbiAgICpcbiAgICogWW91IG1heSBjYWxsIGBkaXNwYXRjaCgpYCBmcm9tIGEgY2hhbmdlIGxpc3RlbmVyLCB3aXRoIHRoZSBmb2xsb3dpbmdcbiAgICogY2F2ZWF0czpcbiAgICpcbiAgICogMS4gVGhlIHN1YnNjcmlwdGlvbnMgYXJlIHNuYXBzaG90dGVkIGp1c3QgYmVmb3JlIGV2ZXJ5IGBkaXNwYXRjaCgpYCBjYWxsLlxuICAgKiBJZiB5b3Ugc3Vic2NyaWJlIG9yIHVuc3Vic2NyaWJlIHdoaWxlIHRoZSBsaXN0ZW5lcnMgYXJlIGJlaW5nIGludm9rZWQsIHRoaXNcbiAgICogd2lsbCBub3QgaGF2ZSBhbnkgZWZmZWN0IG9uIHRoZSBgZGlzcGF0Y2goKWAgdGhhdCBpcyBjdXJyZW50bHkgaW4gcHJvZ3Jlc3MuXG4gICAqIEhvd2V2ZXIsIHRoZSBuZXh0IGBkaXNwYXRjaCgpYCBjYWxsLCB3aGV0aGVyIG5lc3RlZCBvciBub3QsIHdpbGwgdXNlIGEgbW9yZVxuICAgKiByZWNlbnQgc25hcHNob3Qgb2YgdGhlIHN1YnNjcmlwdGlvbiBsaXN0LlxuICAgKlxuICAgKiAyLiBUaGUgbGlzdGVuZXIgc2hvdWxkIG5vdCBleHBlY3QgdG8gc2VlIGFsbCBzdGF0ZSBjaGFuZ2VzLCBhcyB0aGUgc3RhdGVcbiAgICogbWlnaHQgaGF2ZSBiZWVuIHVwZGF0ZWQgbXVsdGlwbGUgdGltZXMgZHVyaW5nIGEgbmVzdGVkIGBkaXNwYXRjaCgpYCBiZWZvcmVcbiAgICogdGhlIGxpc3RlbmVyIGlzIGNhbGxlZC4gSXQgaXMsIGhvd2V2ZXIsIGd1YXJhbnRlZWQgdGhhdCBhbGwgc3Vic2NyaWJlcnNcbiAgICogcmVnaXN0ZXJlZCBiZWZvcmUgdGhlIGBkaXNwYXRjaCgpYCBzdGFydGVkIHdpbGwgYmUgY2FsbGVkIHdpdGggdGhlIGxhdGVzdFxuICAgKiBzdGF0ZSBieSB0aGUgdGltZSBpdCBleGl0cy5cbiAgICpcbiAgICogQHBhcmFtIHtGdW5jdGlvbn0gbGlzdGVuZXIgQSBjYWxsYmFjayB0byBiZSBpbnZva2VkIG9uIGV2ZXJ5IGRpc3BhdGNoLlxuICAgKiBAcmV0dXJucyB7RnVuY3Rpb259IEEgZnVuY3Rpb24gdG8gcmVtb3ZlIHRoaXMgY2hhbmdlIGxpc3RlbmVyLlxuICAgKi9cbiAgZnVuY3Rpb24gc3Vic2NyaWJlKGxpc3RlbmVyKSB7XG4gICAgaWYgKHR5cGVvZiBsaXN0ZW5lciAhPT0gJ2Z1bmN0aW9uJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdFeHBlY3RlZCBsaXN0ZW5lciB0byBiZSBhIGZ1bmN0aW9uLicpO1xuICAgIH1cblxuICAgIHZhciBpc1N1YnNjcmliZWQgPSB0cnVlO1xuXG4gICAgZW5zdXJlQ2FuTXV0YXRlTmV4dExpc3RlbmVycygpO1xuICAgIG5leHRMaXN0ZW5lcnMucHVzaChsaXN0ZW5lcik7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gdW5zdWJzY3JpYmUoKSB7XG4gICAgICBpZiAoIWlzU3Vic2NyaWJlZCkge1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG5cbiAgICAgIGlzU3Vic2NyaWJlZCA9IGZhbHNlO1xuXG4gICAgICBlbnN1cmVDYW5NdXRhdGVOZXh0TGlzdGVuZXJzKCk7XG4gICAgICB2YXIgaW5kZXggPSBuZXh0TGlzdGVuZXJzLmluZGV4T2YobGlzdGVuZXIpO1xuICAgICAgbmV4dExpc3RlbmVycy5zcGxpY2UoaW5kZXgsIDEpO1xuICAgIH07XG4gIH1cblxuICAvKipcbiAgICogRGlzcGF0Y2hlcyBhbiBhY3Rpb24uIEl0IGlzIHRoZSBvbmx5IHdheSB0byB0cmlnZ2VyIGEgc3RhdGUgY2hhbmdlLlxuICAgKlxuICAgKiBUaGUgYHJlZHVjZXJgIGZ1bmN0aW9uLCB1c2VkIHRvIGNyZWF0ZSB0aGUgc3RvcmUsIHdpbGwgYmUgY2FsbGVkIHdpdGggdGhlXG4gICAqIGN1cnJlbnQgc3RhdGUgdHJlZSBhbmQgdGhlIGdpdmVuIGBhY3Rpb25gLiBJdHMgcmV0dXJuIHZhbHVlIHdpbGxcbiAgICogYmUgY29uc2lkZXJlZCB0aGUgKipuZXh0Kiogc3RhdGUgb2YgdGhlIHRyZWUsIGFuZCB0aGUgY2hhbmdlIGxpc3RlbmVyc1xuICAgKiB3aWxsIGJlIG5vdGlmaWVkLlxuICAgKlxuICAgKiBUaGUgYmFzZSBpbXBsZW1lbnRhdGlvbiBvbmx5IHN1cHBvcnRzIHBsYWluIG9iamVjdCBhY3Rpb25zLiBJZiB5b3Ugd2FudCB0b1xuICAgKiBkaXNwYXRjaCBhIFByb21pc2UsIGFuIE9ic2VydmFibGUsIGEgdGh1bmssIG9yIHNvbWV0aGluZyBlbHNlLCB5b3UgbmVlZCB0b1xuICAgKiB3cmFwIHlvdXIgc3RvcmUgY3JlYXRpbmcgZnVuY3Rpb24gaW50byB0aGUgY29ycmVzcG9uZGluZyBtaWRkbGV3YXJlLiBGb3JcbiAgICogZXhhbXBsZSwgc2VlIHRoZSBkb2N1bWVudGF0aW9uIGZvciB0aGUgYHJlZHV4LXRodW5rYCBwYWNrYWdlLiBFdmVuIHRoZVxuICAgKiBtaWRkbGV3YXJlIHdpbGwgZXZlbnR1YWxseSBkaXNwYXRjaCBwbGFpbiBvYmplY3QgYWN0aW9ucyB1c2luZyB0aGlzIG1ldGhvZC5cbiAgICpcbiAgICogQHBhcmFtIHtPYmplY3R9IGFjdGlvbiBBIHBsYWluIG9iamVjdCByZXByZXNlbnRpbmcg4oCcd2hhdCBjaGFuZ2Vk4oCdLiBJdCBpc1xuICAgKiBhIGdvb2QgaWRlYSB0byBrZWVwIGFjdGlvbnMgc2VyaWFsaXphYmxlIHNvIHlvdSBjYW4gcmVjb3JkIGFuZCByZXBsYXkgdXNlclxuICAgKiBzZXNzaW9ucywgb3IgdXNlIHRoZSB0aW1lIHRyYXZlbGxpbmcgYHJlZHV4LWRldnRvb2xzYC4gQW4gYWN0aW9uIG11c3QgaGF2ZVxuICAgKiBhIGB0eXBlYCBwcm9wZXJ0eSB3aGljaCBtYXkgbm90IGJlIGB1bmRlZmluZWRgLiBJdCBpcyBhIGdvb2QgaWRlYSB0byB1c2VcbiAgICogc3RyaW5nIGNvbnN0YW50cyBmb3IgYWN0aW9uIHR5cGVzLlxuICAgKlxuICAgKiBAcmV0dXJucyB7T2JqZWN0fSBGb3IgY29udmVuaWVuY2UsIHRoZSBzYW1lIGFjdGlvbiBvYmplY3QgeW91IGRpc3BhdGNoZWQuXG4gICAqXG4gICAqIE5vdGUgdGhhdCwgaWYgeW91IHVzZSBhIGN1c3RvbSBtaWRkbGV3YXJlLCBpdCBtYXkgd3JhcCBgZGlzcGF0Y2goKWAgdG9cbiAgICogcmV0dXJuIHNvbWV0aGluZyBlbHNlIChmb3IgZXhhbXBsZSwgYSBQcm9taXNlIHlvdSBjYW4gYXdhaXQpLlxuICAgKi9cbiAgZnVuY3Rpb24gZGlzcGF0Y2goYWN0aW9uKSB7XG4gICAgaWYgKCEoMCwgX2lzUGxhaW5PYmplY3QyW1wiZGVmYXVsdFwiXSkoYWN0aW9uKSkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdBY3Rpb25zIG11c3QgYmUgcGxhaW4gb2JqZWN0cy4gJyArICdVc2UgY3VzdG9tIG1pZGRsZXdhcmUgZm9yIGFzeW5jIGFjdGlvbnMuJyk7XG4gICAgfVxuXG4gICAgaWYgKHR5cGVvZiBhY3Rpb24udHlwZSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignQWN0aW9ucyBtYXkgbm90IGhhdmUgYW4gdW5kZWZpbmVkIFwidHlwZVwiIHByb3BlcnR5LiAnICsgJ0hhdmUgeW91IG1pc3NwZWxsZWQgYSBjb25zdGFudD8nKTtcbiAgICB9XG5cbiAgICBpZiAoaXNEaXNwYXRjaGluZykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdSZWR1Y2VycyBtYXkgbm90IGRpc3BhdGNoIGFjdGlvbnMuJyk7XG4gICAgfVxuXG4gICAgdHJ5IHtcbiAgICAgIGlzRGlzcGF0Y2hpbmcgPSB0cnVlO1xuICAgICAgY3VycmVudFN0YXRlID0gY3VycmVudFJlZHVjZXIoY3VycmVudFN0YXRlLCBhY3Rpb24pO1xuICAgIH0gZmluYWxseSB7XG4gICAgICBpc0Rpc3BhdGNoaW5nID0gZmFsc2U7XG4gICAgfVxuXG4gICAgdmFyIGxpc3RlbmVycyA9IGN1cnJlbnRMaXN0ZW5lcnMgPSBuZXh0TGlzdGVuZXJzO1xuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgbGlzdGVuZXJzLmxlbmd0aDsgaSsrKSB7XG4gICAgICBsaXN0ZW5lcnNbaV0oKTtcbiAgICB9XG5cbiAgICByZXR1cm4gYWN0aW9uO1xuICB9XG5cbiAgLyoqXG4gICAqIFJlcGxhY2VzIHRoZSByZWR1Y2VyIGN1cnJlbnRseSB1c2VkIGJ5IHRoZSBzdG9yZSB0byBjYWxjdWxhdGUgdGhlIHN0YXRlLlxuICAgKlxuICAgKiBZb3UgbWlnaHQgbmVlZCB0aGlzIGlmIHlvdXIgYXBwIGltcGxlbWVudHMgY29kZSBzcGxpdHRpbmcgYW5kIHlvdSB3YW50IHRvXG4gICAqIGxvYWQgc29tZSBvZiB0aGUgcmVkdWNlcnMgZHluYW1pY2FsbHkuIFlvdSBtaWdodCBhbHNvIG5lZWQgdGhpcyBpZiB5b3VcbiAgICogaW1wbGVtZW50IGEgaG90IHJlbG9hZGluZyBtZWNoYW5pc20gZm9yIFJlZHV4LlxuICAgKlxuICAgKiBAcGFyYW0ge0Z1bmN0aW9ufSBuZXh0UmVkdWNlciBUaGUgcmVkdWNlciBmb3IgdGhlIHN0b3JlIHRvIHVzZSBpbnN0ZWFkLlxuICAgKiBAcmV0dXJucyB7dm9pZH1cbiAgICovXG4gIGZ1bmN0aW9uIHJlcGxhY2VSZWR1Y2VyKG5leHRSZWR1Y2VyKSB7XG4gICAgaWYgKHR5cGVvZiBuZXh0UmVkdWNlciAhPT0gJ2Z1bmN0aW9uJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdFeHBlY3RlZCB0aGUgbmV4dFJlZHVjZXIgdG8gYmUgYSBmdW5jdGlvbi4nKTtcbiAgICB9XG5cbiAgICBjdXJyZW50UmVkdWNlciA9IG5leHRSZWR1Y2VyO1xuICAgIGRpc3BhdGNoKHsgdHlwZTogQWN0aW9uVHlwZXMuSU5JVCB9KTtcbiAgfVxuXG4gIC8qKlxuICAgKiBJbnRlcm9wZXJhYmlsaXR5IHBvaW50IGZvciBvYnNlcnZhYmxlL3JlYWN0aXZlIGxpYnJhcmllcy5cbiAgICogQHJldHVybnMge29ic2VydmFibGV9IEEgbWluaW1hbCBvYnNlcnZhYmxlIG9mIHN0YXRlIGNoYW5nZXMuXG4gICAqIEZvciBtb3JlIGluZm9ybWF0aW9uLCBzZWUgdGhlIG9ic2VydmFibGUgcHJvcG9zYWw6XG4gICAqIGh0dHBzOi8vZ2l0aHViLmNvbS96ZW5wYXJzaW5nL2VzLW9ic2VydmFibGVcbiAgICovXG4gIGZ1bmN0aW9uIG9ic2VydmFibGUoKSB7XG4gICAgdmFyIF9yZWY7XG5cbiAgICB2YXIgb3V0ZXJTdWJzY3JpYmUgPSBzdWJzY3JpYmU7XG4gICAgcmV0dXJuIF9yZWYgPSB7XG4gICAgICAvKipcbiAgICAgICAqIFRoZSBtaW5pbWFsIG9ic2VydmFibGUgc3Vic2NyaXB0aW9uIG1ldGhvZC5cbiAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvYnNlcnZlciBBbnkgb2JqZWN0IHRoYXQgY2FuIGJlIHVzZWQgYXMgYW4gb2JzZXJ2ZXIuXG4gICAgICAgKiBUaGUgb2JzZXJ2ZXIgb2JqZWN0IHNob3VsZCBoYXZlIGEgYG5leHRgIG1ldGhvZC5cbiAgICAgICAqIEByZXR1cm5zIHtzdWJzY3JpcHRpb259IEFuIG9iamVjdCB3aXRoIGFuIGB1bnN1YnNjcmliZWAgbWV0aG9kIHRoYXQgY2FuXG4gICAgICAgKiBiZSB1c2VkIHRvIHVuc3Vic2NyaWJlIHRoZSBvYnNlcnZhYmxlIGZyb20gdGhlIHN0b3JlLCBhbmQgcHJldmVudCBmdXJ0aGVyXG4gICAgICAgKiBlbWlzc2lvbiBvZiB2YWx1ZXMgZnJvbSB0aGUgb2JzZXJ2YWJsZS5cbiAgICAgICAqL1xuXG4gICAgICBzdWJzY3JpYmU6IGZ1bmN0aW9uIHN1YnNjcmliZShvYnNlcnZlcikge1xuICAgICAgICBpZiAodHlwZW9mIG9ic2VydmVyICE9PSAnb2JqZWN0Jykge1xuICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ0V4cGVjdGVkIHRoZSBvYnNlcnZlciB0byBiZSBhbiBvYmplY3QuJyk7XG4gICAgICAgIH1cblxuICAgICAgICBmdW5jdGlvbiBvYnNlcnZlU3RhdGUoKSB7XG4gICAgICAgICAgaWYgKG9ic2VydmVyLm5leHQpIHtcbiAgICAgICAgICAgIG9ic2VydmVyLm5leHQoZ2V0U3RhdGUoKSk7XG4gICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgb2JzZXJ2ZVN0YXRlKCk7XG4gICAgICAgIHZhciB1bnN1YnNjcmliZSA9IG91dGVyU3Vic2NyaWJlKG9ic2VydmVTdGF0ZSk7XG4gICAgICAgIHJldHVybiB7IHVuc3Vic2NyaWJlOiB1bnN1YnNjcmliZSB9O1xuICAgICAgfVxuICAgIH0sIF9yZWZbX3N5bWJvbE9ic2VydmFibGUyW1wiZGVmYXVsdFwiXV0gPSBmdW5jdGlvbiAoKSB7XG4gICAgICByZXR1cm4gdGhpcztcbiAgICB9LCBfcmVmO1xuICB9XG5cbiAgLy8gV2hlbiBhIHN0b3JlIGlzIGNyZWF0ZWQsIGFuIFwiSU5JVFwiIGFjdGlvbiBpcyBkaXNwYXRjaGVkIHNvIHRoYXQgZXZlcnlcbiAgLy8gcmVkdWNlciByZXR1cm5zIHRoZWlyIGluaXRpYWwgc3RhdGUuIFRoaXMgZWZmZWN0aXZlbHkgcG9wdWxhdGVzXG4gIC8vIHRoZSBpbml0aWFsIHN0YXRlIHRyZWUuXG4gIGRpc3BhdGNoKHsgdHlwZTogQWN0aW9uVHlwZXMuSU5JVCB9KTtcblxuICByZXR1cm4gX3JlZjIgPSB7XG4gICAgZGlzcGF0Y2g6IGRpc3BhdGNoLFxuICAgIHN1YnNjcmliZTogc3Vic2NyaWJlLFxuICAgIGdldFN0YXRlOiBnZXRTdGF0ZSxcbiAgICByZXBsYWNlUmVkdWNlcjogcmVwbGFjZVJlZHVjZXJcbiAgfSwgX3JlZjJbX3N5bWJvbE9ic2VydmFibGUyW1wiZGVmYXVsdFwiXV0gPSBvYnNlcnZhYmxlLCBfcmVmMjtcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzLmNvbXBvc2UgPSBleHBvcnRzLmFwcGx5TWlkZGxld2FyZSA9IGV4cG9ydHMuYmluZEFjdGlvbkNyZWF0b3JzID0gZXhwb3J0cy5jb21iaW5lUmVkdWNlcnMgPSBleHBvcnRzLmNyZWF0ZVN0b3JlID0gdW5kZWZpbmVkO1xuXG52YXIgX2NyZWF0ZVN0b3JlID0gcmVxdWlyZSgnLi9jcmVhdGVTdG9yZScpO1xuXG52YXIgX2NyZWF0ZVN0b3JlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NyZWF0ZVN0b3JlKTtcblxudmFyIF9jb21iaW5lUmVkdWNlcnMgPSByZXF1aXJlKCcuL2NvbWJpbmVSZWR1Y2VycycpO1xuXG52YXIgX2NvbWJpbmVSZWR1Y2VyczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jb21iaW5lUmVkdWNlcnMpO1xuXG52YXIgX2JpbmRBY3Rpb25DcmVhdG9ycyA9IHJlcXVpcmUoJy4vYmluZEFjdGlvbkNyZWF0b3JzJyk7XG5cbnZhciBfYmluZEFjdGlvbkNyZWF0b3JzMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2JpbmRBY3Rpb25DcmVhdG9ycyk7XG5cbnZhciBfYXBwbHlNaWRkbGV3YXJlID0gcmVxdWlyZSgnLi9hcHBseU1pZGRsZXdhcmUnKTtcblxudmFyIF9hcHBseU1pZGRsZXdhcmUyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfYXBwbHlNaWRkbGV3YXJlKTtcblxudmFyIF9jb21wb3NlID0gcmVxdWlyZSgnLi9jb21wb3NlJyk7XG5cbnZhciBfY29tcG9zZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jb21wb3NlKTtcblxudmFyIF93YXJuaW5nID0gcmVxdWlyZSgnLi91dGlscy93YXJuaW5nJyk7XG5cbnZhciBfd2FybmluZzIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF93YXJuaW5nKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbi8qXG4qIFRoaXMgaXMgYSBkdW1teSBmdW5jdGlvbiB0byBjaGVjayBpZiB0aGUgZnVuY3Rpb24gbmFtZSBoYXMgYmVlbiBhbHRlcmVkIGJ5IG1pbmlmaWNhdGlvbi5cbiogSWYgdGhlIGZ1bmN0aW9uIGhhcyBiZWVuIG1pbmlmaWVkIGFuZCBOT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nLCB3YXJuIHRoZSB1c2VyLlxuKi9cbmZ1bmN0aW9uIGlzQ3J1c2hlZCgpIHt9XG5cbmlmIChwcm9jZXNzLmVudi5OT0RFX0VOViAhPT0gJ3Byb2R1Y3Rpb24nICYmIHR5cGVvZiBpc0NydXNoZWQubmFtZSA9PT0gJ3N0cmluZycgJiYgaXNDcnVzaGVkLm5hbWUgIT09ICdpc0NydXNoZWQnKSB7XG4gICgwLCBfd2FybmluZzJbXCJkZWZhdWx0XCJdKSgnWW91IGFyZSBjdXJyZW50bHkgdXNpbmcgbWluaWZpZWQgY29kZSBvdXRzaWRlIG9mIE5PREVfRU5WID09PSBcXCdwcm9kdWN0aW9uXFwnLiAnICsgJ1RoaXMgbWVhbnMgdGhhdCB5b3UgYXJlIHJ1bm5pbmcgYSBzbG93ZXIgZGV2ZWxvcG1lbnQgYnVpbGQgb2YgUmVkdXguICcgKyAnWW91IGNhbiB1c2UgbG9vc2UtZW52aWZ5IChodHRwczovL2dpdGh1Yi5jb20vemVydG9zaC9sb29zZS1lbnZpZnkpIGZvciBicm93c2VyaWZ5ICcgKyAnb3IgRGVmaW5lUGx1Z2luIGZvciB3ZWJwYWNrIChodHRwOi8vc3RhY2tvdmVyZmxvdy5jb20vcXVlc3Rpb25zLzMwMDMwMDMxKSAnICsgJ3RvIGVuc3VyZSB5b3UgaGF2ZSB0aGUgY29ycmVjdCBjb2RlIGZvciB5b3VyIHByb2R1Y3Rpb24gYnVpbGQuJyk7XG59XG5cbmV4cG9ydHMuY3JlYXRlU3RvcmUgPSBfY3JlYXRlU3RvcmUyW1wiZGVmYXVsdFwiXTtcbmV4cG9ydHMuY29tYmluZVJlZHVjZXJzID0gX2NvbWJpbmVSZWR1Y2VyczJbXCJkZWZhdWx0XCJdO1xuZXhwb3J0cy5iaW5kQWN0aW9uQ3JlYXRvcnMgPSBfYmluZEFjdGlvbkNyZWF0b3JzMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmFwcGx5TWlkZGxld2FyZSA9IF9hcHBseU1pZGRsZXdhcmUyW1wiZGVmYXVsdFwiXTtcbmV4cG9ydHMuY29tcG9zZSA9IF9jb21wb3NlMltcImRlZmF1bHRcIl07IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSB3YXJuaW5nO1xuLyoqXG4gKiBQcmludHMgYSB3YXJuaW5nIGluIHRoZSBjb25zb2xlIGlmIGl0IGV4aXN0cy5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ30gbWVzc2FnZSBUaGUgd2FybmluZyBtZXNzYWdlLlxuICogQHJldHVybnMge3ZvaWR9XG4gKi9cbmZ1bmN0aW9uIHdhcm5pbmcobWVzc2FnZSkge1xuICAvKiBlc2xpbnQtZGlzYWJsZSBuby1jb25zb2xlICovXG4gIGlmICh0eXBlb2YgY29uc29sZSAhPT0gJ3VuZGVmaW5lZCcgJiYgdHlwZW9mIGNvbnNvbGUuZXJyb3IgPT09ICdmdW5jdGlvbicpIHtcbiAgICBjb25zb2xlLmVycm9yKG1lc3NhZ2UpO1xuICB9XG4gIC8qIGVzbGludC1lbmFibGUgbm8tY29uc29sZSAqL1xuICB0cnkge1xuICAgIC8vIFRoaXMgZXJyb3Igd2FzIHRocm93biBhcyBhIGNvbnZlbmllbmNlIHNvIHRoYXQgaWYgeW91IGVuYWJsZVxuICAgIC8vIFwiYnJlYWsgb24gYWxsIGV4Y2VwdGlvbnNcIiBpbiB5b3VyIGNvbnNvbGUsXG4gICAgLy8gaXQgd291bGQgcGF1c2UgdGhlIGV4ZWN1dGlvbiBhdCB0aGlzIGxpbmUuXG4gICAgdGhyb3cgbmV3IEVycm9yKG1lc3NhZ2UpO1xuICAgIC8qIGVzbGludC1kaXNhYmxlIG5vLWVtcHR5ICovXG4gIH0gY2F0Y2ggKGUpIHt9XG4gIC8qIGVzbGludC1lbmFibGUgbm8tZW1wdHkgKi9cbn0iLCIvKiBnbG9iYWwgd2luZG93ICovXG4ndXNlIHN0cmljdCc7XG5cbm1vZHVsZS5leHBvcnRzID0gcmVxdWlyZSgnLi9wb255ZmlsbCcpKGdsb2JhbCB8fCB3aW5kb3cgfHwgdGhpcyk7XG4iLCIndXNlIHN0cmljdCc7XG5cbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24gc3ltYm9sT2JzZXJ2YWJsZVBvbnlmaWxsKHJvb3QpIHtcblx0dmFyIHJlc3VsdDtcblx0dmFyIFN5bWJvbCA9IHJvb3QuU3ltYm9sO1xuXG5cdGlmICh0eXBlb2YgU3ltYm9sID09PSAnZnVuY3Rpb24nKSB7XG5cdFx0aWYgKFN5bWJvbC5vYnNlcnZhYmxlKSB7XG5cdFx0XHRyZXN1bHQgPSBTeW1ib2wub2JzZXJ2YWJsZTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0cmVzdWx0ID0gU3ltYm9sKCdvYnNlcnZhYmxlJyk7XG5cdFx0XHRTeW1ib2wub2JzZXJ2YWJsZSA9IHJlc3VsdDtcblx0XHR9XG5cdH0gZWxzZSB7XG5cdFx0cmVzdWx0ID0gJ0BAb2JzZXJ2YWJsZSc7XG5cdH1cblxuXHRyZXR1cm4gcmVzdWx0O1xufTtcbiIsIlxuZXhwb3J0cyA9IG1vZHVsZS5leHBvcnRzID0gdHJpbTtcblxuZnVuY3Rpb24gdHJpbShzdHIpe1xuICByZXR1cm4gc3RyLnJlcGxhY2UoL15cXHMqfFxccyokL2csICcnKTtcbn1cblxuZXhwb3J0cy5sZWZ0ID0gZnVuY3Rpb24oc3RyKXtcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9eXFxzKi8sICcnKTtcbn07XG5cbmV4cG9ydHMucmlnaHQgPSBmdW5jdGlvbihzdHIpe1xuICByZXR1cm4gc3RyLnJlcGxhY2UoL1xccyokLywgJycpO1xufTtcbiIsInZhciB3aW5kb3cgICAgICAgICAgICAgID0gcmVxdWlyZSgnZ2xvYmFsJyk7XG52YXIgTW9ja1hNTEh0dHBSZXF1ZXN0ICA9IHJlcXVpcmUoJy4vbGliL01vY2tYTUxIdHRwUmVxdWVzdCcpO1xudmFyIHJlYWwgICAgICAgICAgICAgICAgPSB3aW5kb3cuWE1MSHR0cFJlcXVlc3Q7XG52YXIgbW9jayAgICAgICAgICAgICAgICA9IE1vY2tYTUxIdHRwUmVxdWVzdDtcblxuLyoqXG4gKiBNb2NrIHV0aWxpdHlcbiAqL1xubW9kdWxlLmV4cG9ydHMgPSB7XG5cblx0WE1MSHR0cFJlcXVlc3Q6IE1vY2tYTUxIdHRwUmVxdWVzdCxcblxuXHQvKipcblx0ICogUmVwbGFjZSB0aGUgbmF0aXZlIFhIUiB3aXRoIHRoZSBtb2NrZWQgWEhSXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0c2V0dXA6IGZ1bmN0aW9uKCkge1xuXHRcdHdpbmRvdy5YTUxIdHRwUmVxdWVzdCA9IG1vY2s7XG5cdFx0TW9ja1hNTEh0dHBSZXF1ZXN0LmhhbmRsZXJzID0gW107XG5cdFx0cmV0dXJuIHRoaXM7XG5cdH0sXG5cblx0LyoqXG5cdCAqIFJlcGxhY2UgdGhlIG1vY2tlZCBYSFIgd2l0aCB0aGUgbmF0aXZlIFhIUiBhbmQgcmVtb3ZlIGFueSBoYW5kbGVyc1xuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHRlYXJkb3duOiBmdW5jdGlvbigpIHtcblx0XHRNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMgPSBbXTtcblx0XHR3aW5kb3cuWE1MSHR0cFJlcXVlc3QgPSByZWFsO1xuXHRcdHJldHVybiB0aGlzO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7c3RyaW5nfSAgICBbbWV0aG9kXVxuXHQgKiBAcGFyYW0gICB7c3RyaW5nfSAgICBbdXJsXVxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdG1vY2s6IGZ1bmN0aW9uKG1ldGhvZCwgdXJsLCBmbikge1xuXHRcdHZhciBoYW5kbGVyO1xuXHRcdGlmIChhcmd1bWVudHMubGVuZ3RoID09PSAzKSB7XG5cdFx0XHRoYW5kbGVyID0gZnVuY3Rpb24ocmVxLCByZXMpIHtcblx0XHRcdFx0aWYgKHJlcS5tZXRob2QoKSA9PT0gbWV0aG9kICYmIHJlcS51cmwoKSA9PT0gdXJsKSB7XG5cdFx0XHRcdFx0cmV0dXJuIGZuKHJlcSwgcmVzKTtcblx0XHRcdFx0fVxuXHRcdFx0XHRyZXR1cm4gZmFsc2U7XG5cdFx0XHR9O1xuXHRcdH0gZWxzZSB7XG5cdFx0XHRoYW5kbGVyID0gbWV0aG9kO1xuXHRcdH1cblxuXHRcdE1vY2tYTUxIdHRwUmVxdWVzdC5hZGRIYW5kbGVyKGhhbmRsZXIpO1xuXG5cdFx0cmV0dXJuIHRoaXM7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBHRVQgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRnZXQ6IGZ1bmN0aW9uKHVybCwgZm4pIHtcblx0XHRyZXR1cm4gdGhpcy5tb2NrKCdHRVQnLCB1cmwsIGZuKTtcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIFBPU1QgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRwb3N0OiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnUE9TVCcsIHVybCwgZm4pO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgUFVUIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge1N0cmluZ30gICAgdXJsXG5cdCAqIEBwYXJhbSAgIHtGdW5jdGlvbn0gIGZuXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0cHV0OiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnUFVUJywgdXJsLCBmbik7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBQQVRDSCByZXF1ZXN0XG5cdCAqIEBwYXJhbSAgIHtTdHJpbmd9ICAgIHVybFxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHBhdGNoOiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnUEFUQ0gnLCB1cmwsIGZuKTtcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIERFTEVURSByZXF1ZXN0XG5cdCAqIEBwYXJhbSAgIHtTdHJpbmd9ICAgIHVybFxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdGRlbGV0ZTogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ0RFTEVURScsIHVybCwgZm4pO1xuXHR9XG5cbn07XG4iLCJcbi8qKlxuICogVGhlIG1vY2tlZCByZXF1ZXN0IGRhdGFcbiAqIEBjb25zdHJ1Y3RvclxuICovXG5mdW5jdGlvbiBNb2NrUmVxdWVzdCh4aHIpIHtcbiAgdGhpcy5fbWV0aG9kICAgID0geGhyLm1ldGhvZDtcbiAgdGhpcy5fdXJsICAgICAgID0geGhyLnVybDtcbiAgdGhpcy5faGVhZGVycyAgID0ge307XG4gIHRoaXMuaGVhZGVycyh4aHIuX3JlcXVlc3RIZWFkZXJzKTtcbiAgdGhpcy5ib2R5KHhoci5kYXRhKTtcbn1cblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIG1ldGhvZFxuICogQHJldHVybnMge3N0cmluZ31cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLm1ldGhvZCA9IGZ1bmN0aW9uKCkge1xuICByZXR1cm4gdGhpcy5fbWV0aG9kO1xufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIFVSTFxuICogQHJldHVybnMge3N0cmluZ31cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLnVybCA9IGZ1bmN0aW9uKCkge1xuICByZXR1cm4gdGhpcy5fdXJsO1xufTtcblxuLyoqXG4gKiBHZXQvc2V0IGEgSFRUUCBoZWFkZXJcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IG5hbWVcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFt2YWx1ZV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8dW5kZWZpbmVkfE1vY2tSZXF1ZXN0fVxuICovXG5Nb2NrUmVxdWVzdC5wcm90b3R5cGUuaGVhZGVyID0gZnVuY3Rpb24obmFtZSwgdmFsdWUpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGggPT09IDIpIHtcbiAgICB0aGlzLl9oZWFkZXJzW25hbWUudG9Mb3dlckNhc2UoKV0gPSB2YWx1ZTtcbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5faGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldIHx8IG51bGw7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCBhbGwgb2YgdGhlIEhUVFAgaGVhZGVyc1xuICogQHBhcmFtICAge09iamVjdH0gW2hlYWRlcnNdXG4gKiBAcmV0dXJucyB7T2JqZWN0fE1vY2tSZXF1ZXN0fVxuICovXG5Nb2NrUmVxdWVzdC5wcm90b3R5cGUuaGVhZGVycyA9IGZ1bmN0aW9uKGhlYWRlcnMpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGgpIHtcbiAgICBmb3IgKHZhciBuYW1lIGluIGhlYWRlcnMpIHtcbiAgICAgIGlmIChoZWFkZXJzLmhhc093blByb3BlcnR5KG5hbWUpKSB7XG4gICAgICAgIHRoaXMuaGVhZGVyKG5hbWUsIGhlYWRlcnNbbmFtZV0pO1xuICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5faGVhZGVycztcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIGJvZHlcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFtib2R5XVxuICogQHJldHVybnMge3N0cmluZ3xNb2NrUmVxdWVzdH1cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLmJvZHkgPSBmdW5jdGlvbihib2R5KSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fYm9keSA9IGJvZHk7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2JvZHk7XG4gIH1cbn07XG5cbm1vZHVsZS5leHBvcnRzID0gTW9ja1JlcXVlc3Q7XG4iLCJcbi8qKlxuICogVGhlIG1vY2tlZCByZXNwb25zZSBkYXRhXG4gKiBAY29uc3RydWN0b3JcbiAqL1xuZnVuY3Rpb24gTW9ja1Jlc3BvbnNlKCkge1xuICB0aGlzLl9zdGF0dXMgICAgICA9IDIwMDtcbiAgdGhpcy5faGVhZGVycyAgICAgPSB7fTtcbiAgdGhpcy5fYm9keSAgICAgICAgPSAnJztcbiAgdGhpcy5fdGltZW91dCAgICAgPSBmYWxzZTtcbn1cblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIHN0YXR1c1xuICogQHBhcmFtICAge251bWJlcn0gW2NvZGVdXG4gKiBAcmV0dXJucyB7bnVtYmVyfE1vY2tSZXNwb25zZX1cbiAqL1xuTW9ja1Jlc3BvbnNlLnByb3RvdHlwZS5zdGF0dXMgPSBmdW5jdGlvbihjb2RlKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fc3RhdHVzID0gY29kZTtcbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5fc3RhdHVzO1xuICB9XG59O1xuXG4vKipcbiAqIEdldC9zZXQgYSBIVFRQIGhlYWRlclxuICogQHBhcmFtICAge3N0cmluZ30gbmFtZVxuICogQHBhcmFtICAge3N0cmluZ30gW3ZhbHVlXVxuICogQHJldHVybnMge3N0cmluZ3x1bmRlZmluZWR8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLmhlYWRlciA9IGZ1bmN0aW9uKG5hbWUsIHZhbHVlKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoID09PSAyKSB7XG4gICAgdGhpcy5faGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldID0gdmFsdWU7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSB8fCBudWxsO1xuICB9XG59O1xuXG4vKipcbiAqIEdldC9zZXQgYWxsIG9mIHRoZSBIVFRQIGhlYWRlcnNcbiAqIEBwYXJhbSAgIHtPYmplY3R9IFtoZWFkZXJzXVxuICogQHJldHVybnMge09iamVjdHxNb2NrUmVzcG9uc2V9XG4gKi9cbk1vY2tSZXNwb25zZS5wcm90b3R5cGUuaGVhZGVycyA9IGZ1bmN0aW9uKGhlYWRlcnMpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGgpIHtcbiAgICBmb3IgKHZhciBuYW1lIGluIGhlYWRlcnMpIHtcbiAgICAgIGlmIChoZWFkZXJzLmhhc093blByb3BlcnR5KG5hbWUpKSB7XG4gICAgICAgIHRoaXMuaGVhZGVyKG5hbWUsIGhlYWRlcnNbbmFtZV0pO1xuICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5faGVhZGVycztcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIGJvZHlcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFtib2R5XVxuICogQHJldHVybnMge3N0cmluZ3xNb2NrUmVzcG9uc2V9XG4gKi9cbk1vY2tSZXNwb25zZS5wcm90b3R5cGUuYm9keSA9IGZ1bmN0aW9uKGJvZHkpIHtcbiAgaWYgKGFyZ3VtZW50cy5sZW5ndGgpIHtcbiAgICB0aGlzLl9ib2R5ID0gYm9keTtcbiAgICByZXR1cm4gdGhpcztcbiAgfSBlbHNlIHtcbiAgICByZXR1cm4gdGhpcy5fYm9keTtcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IHRoZSBIVFRQIHRpbWVvdXRcbiAqIEBwYXJhbSAgIHtib29sZWFufG51bWJlcn0gW3RpbWVvdXRdXG4gKiBAcmV0dXJucyB7Ym9vbGVhbnxudW1iZXJ8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLnRpbWVvdXQgPSBmdW5jdGlvbih0aW1lb3V0KSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fdGltZW91dCA9IHRpbWVvdXQ7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX3RpbWVvdXQ7XG4gIH1cbn07XG5cbm1vZHVsZS5leHBvcnRzID0gTW9ja1Jlc3BvbnNlO1xuIiwidmFyIE1vY2tSZXF1ZXN0ICAgPSByZXF1aXJlKCcuL01vY2tSZXF1ZXN0Jyk7XG52YXIgTW9ja1Jlc3BvbnNlICA9IHJlcXVpcmUoJy4vTW9ja1Jlc3BvbnNlJyk7XG5cbnZhciBub3RJbXBsZW1lbnRlZEVycm9yID0gbmV3IEVycm9yKCdUaGlzIGZlYXR1cmUgaGFzblxcJ3QgYmVlbiBpbXBsbWVudGVkIHlldC4gUGxlYXNlIHN1Ym1pdCBhbiBJc3N1ZSBvciBQdWxsIFJlcXVlc3Qgb24gR2l0aHViLicpO1xuXG4vL2h0dHBzOi8vZGV2ZWxvcGVyLm1vemlsbGEub3JnL2VuLVVTL2RvY3MvV2ViL0FQSS9YTUxIdHRwUmVxdWVzdFxuLy9odHRwczovL3hoci5zcGVjLndoYXR3Zy5vcmcvXG4vL2h0dHA6Ly93d3cudzMub3JnL1RSLzIwMDYvV0QtWE1MSHR0cFJlcXVlc3QtMjAwNjA0MDUvXG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9VTlNFTlQgICAgICAgICAgICAgPSAwO1xuTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX09QRU5FRCAgICAgICAgICAgICA9IDE7XG5Nb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfSEVBREVSU19SRUNFSVZFRCAgID0gMjtcbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9MT0FESU5HICAgICAgICAgICAgPSAzO1xuTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0RPTkUgICAgICAgICAgICAgICA9IDQ7XG5cbi8qKlxuICogVGhlIHJlcXVlc3QgaGFuZGxlcnNcbiAqIEBwcml2YXRlXG4gKiBAdHlwZSB7QXJyYXl9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVycyA9IFtdO1xuXG4vKipcbiAqIEFkZCBhIHJlcXVlc3QgaGFuZGxlclxuICogQHBhcmFtICAge2Z1bmN0aW9uKE1vY2tSZXF1ZXN0LCBNb2NrUmVzcG9uc2UpfSBmblxuICogQHJldHVybnMge01vY2tYTUxIdHRwUmVxdWVzdH1cbiAqL1xuTW9ja1hNTEh0dHBSZXF1ZXN0LmFkZEhhbmRsZXIgPSBmdW5jdGlvbihmbikge1xuICBNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMucHVzaChmbik7XG4gIHJldHVybiB0aGlzO1xufTtcblxuLyoqXG4gKiBSZW1vdmUgYSByZXF1ZXN0IGhhbmRsZXJcbiAqIEBwYXJhbSAgIHtmdW5jdGlvbihNb2NrUmVxdWVzdCwgTW9ja1Jlc3BvbnNlKX0gZm5cbiAqIEByZXR1cm5zIHtNb2NrWE1MSHR0cFJlcXVlc3R9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5yZW1vdmVIYW5kbGVyID0gZnVuY3Rpb24oZm4pIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbi8qKlxuICogSGFuZGxlIGEgcmVxdWVzdFxuICogQHBhcmFtICAge01vY2tSZXF1ZXN0fSByZXF1ZXN0XG4gKiBAcmV0dXJucyB7TW9ja1Jlc3BvbnNlfG51bGx9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGUgPSBmdW5jdGlvbihyZXF1ZXN0KSB7XG5cbiAgZm9yICh2YXIgaT0wOyBpPE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVycy5sZW5ndGg7ICsraSkge1xuXG4gICAgLy9nZXQgdGhlIGdlbmVyYXRvciB0byBjcmVhdGUgYSByZXNwb25zZSB0byB0aGUgcmVxdWVzdFxuICAgIHZhciByZXNwb25zZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVyc1tpXShyZXF1ZXN0LCBuZXcgTW9ja1Jlc3BvbnNlKCkpO1xuXG4gICAgaWYgKHJlc3BvbnNlKSB7XG4gICAgICByZXR1cm4gcmVzcG9uc2U7XG4gICAgfVxuXG4gIH1cblxuICByZXR1cm4gbnVsbDtcbn07XG5cbi8qKlxuICogTW9jayBYTUxIdHRwUmVxdWVzdFxuICogQGNvbnN0cnVjdG9yXG4gKi9cbmZ1bmN0aW9uIE1vY2tYTUxIdHRwUmVxdWVzdCgpIHtcbiAgdGhpcy5yZXNldCgpO1xuICB0aGlzLnRpbWVvdXQgPSAwO1xufVxuXG4vKipcbiAqIFJlc2V0IHRoZSByZXNwb25zZSB2YWx1ZXNcbiAqIEBwcml2YXRlXG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUucmVzZXQgPSBmdW5jdGlvbigpIHtcblxuICB0aGlzLl9yZXF1ZXN0SGVhZGVycyAgPSB7fTtcbiAgdGhpcy5fcmVzcG9uc2VIZWFkZXJzID0ge307XG5cbiAgdGhpcy5zdGF0dXMgICAgICAgPSAwO1xuICB0aGlzLnN0YXR1c1RleHQgICA9ICcnO1xuXG4gIHRoaXMucmVzcG9uc2UgICAgID0gbnVsbDtcbiAgdGhpcy5yZXNwb25zZVR5cGUgPSBudWxsO1xuICB0aGlzLnJlc3BvbnNlVGV4dCA9IG51bGw7XG4gIHRoaXMucmVzcG9uc2VYTUwgID0gbnVsbDtcblxuICB0aGlzLnJlYWR5U3RhdGUgICA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9VTlNFTlQ7XG59O1xuXG4vKipcbiAqIFRyaWdnZXIgYW4gZXZlbnRcbiAqIEBwYXJhbSAgIHtTdHJpbmd9IGV2ZW50XG4gKiBAcmV0dXJucyB7TW9ja1hNTEh0dHBSZXF1ZXN0fVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnRyaWdnZXIgPSBmdW5jdGlvbihldmVudCkge1xuXG4gIGlmICh0aGlzLm9ucmVhZHlzdGF0ZWNoYW5nZSkge1xuICAgIHRoaXMub25yZWFkeXN0YXRlY2hhbmdlKCk7XG4gIH1cblxuICBpZiAodGhpc1snb24nK2V2ZW50XSkge1xuICAgIHRoaXNbJ29uJytldmVudF0oKTtcbiAgfVxuXG4gIC8vaXRlcmF0ZSBvdmVyIHRoZSBsaXN0ZW5lcnNcblxuICByZXR1cm4gdGhpcztcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUub3BlbiA9IGZ1bmN0aW9uKG1ldGhvZCwgdXJsLCBhc3luYywgdXNlciwgcGFzc3dvcmQpIHtcbiAgdGhpcy5yZXNldCgpO1xuICB0aGlzLm1ldGhvZCAgID0gbWV0aG9kO1xuICB0aGlzLnVybCAgICAgID0gdXJsO1xuICB0aGlzLmFzeW5jICAgID0gYXN5bmM7XG4gIHRoaXMudXNlciAgICAgPSB1c2VyO1xuICB0aGlzLnBhc3N3b3JkID0gcGFzc3dvcmQ7XG4gIHRoaXMuZGF0YSAgICAgPSBudWxsO1xuICB0aGlzLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfT1BFTkVEO1xufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5zZXRSZXF1ZXN0SGVhZGVyID0gZnVuY3Rpb24obmFtZSwgdmFsdWUpIHtcbiAgdGhpcy5fcmVxdWVzdEhlYWRlcnNbbmFtZV0gPSB2YWx1ZTtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUub3ZlcnJpZGVNaW1lVHlwZSA9IGZ1bmN0aW9uKG1pbWUpIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUuc2VuZCA9IGZ1bmN0aW9uKGRhdGEpIHtcbiAgdmFyIHNlbGYgPSB0aGlzO1xuICB0aGlzLmRhdGEgPSBkYXRhO1xuXG4gIHNlbGYucmVhZHlTdGF0ZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9MT0FESU5HO1xuXG4gIHNlbGYuX3NlbmRUaW1lb3V0ID0gc2V0VGltZW91dChmdW5jdGlvbigpIHtcblxuICAgIHZhciByZXNwb25zZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGUobmV3IE1vY2tSZXF1ZXN0KHNlbGYpKTtcblxuICAgIGlmIChyZXNwb25zZSAmJiByZXNwb25zZSBpbnN0YW5jZW9mIE1vY2tSZXNwb25zZSkge1xuXG4gICAgICB2YXIgdGltZW91dCA9IHJlc3BvbnNlLnRpbWVvdXQoKTtcblxuICAgICAgaWYgKHRpbWVvdXQpIHtcblxuICAgICAgICAvL3RyaWdnZXIgYSB0aW1lb3V0IGV2ZW50IGJlY2F1c2UgdGhlIHJlcXVlc3QgdGltZWQgb3V0IC0gd2FpdCBmb3IgdGhlIHRpbWVvdXQgdGltZSBiZWNhdXNlIG1hbnkgbGlicyBsaWtlIGpxdWVyeSBhbmQgc3VwZXJhZ2VudCB1c2Ugc2V0VGltZW91dCB0byBkZXRlY3QgdGhlIGVycm9yIHR5cGVcbiAgICAgICAgc2VsZi5fc2VuZFRpbWVvdXQgPSBzZXRUaW1lb3V0KGZ1bmN0aW9uKCkge1xuICAgICAgICAgIHNlbGYucmVhZHlTdGF0ZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FO1xuICAgICAgICAgIHNlbGYudHJpZ2dlcigndGltZW91dCcpO1xuICAgICAgICB9LCB0eXBlb2YodGltZW91dCkgPT09ICdudW1iZXInID8gdGltZW91dCA6IHNlbGYudGltZW91dCsxKTtcblxuICAgICAgfSBlbHNlIHtcblxuICAgICAgICAvL21hcCB0aGUgcmVzcG9uc2UgdG8gdGhlIFhIUiBvYmplY3RcbiAgICAgICAgc2VsZi5zdGF0dXMgICAgICAgICAgICAgPSByZXNwb25zZS5zdGF0dXMoKTtcbiAgICAgICAgc2VsZi5fcmVzcG9uc2VIZWFkZXJzICAgPSByZXNwb25zZS5oZWFkZXJzKCk7XG4gICAgICAgIHNlbGYucmVzcG9uc2VUeXBlICAgICAgID0gJ3RleHQnO1xuICAgICAgICBzZWxmLnJlc3BvbnNlICAgICAgICAgICA9IHJlc3BvbnNlLmJvZHkoKTtcbiAgICAgICAgc2VsZi5yZXNwb25zZVRleHQgICAgICAgPSByZXNwb25zZS5ib2R5KCk7IC8vVE9ETzogZGV0ZWN0IGFuIG9iamVjdCBhbmQgcmV0dXJuIEpTT04sIGRldGVjdCBYTUwgYW5kIHJldHVybiBYTUxcbiAgICAgICAgc2VsZi5yZWFkeVN0YXRlICAgICAgICAgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfRE9ORTtcblxuICAgICAgICAvL3RyaWdnZXIgYSBsb2FkIGV2ZW50IGJlY2F1c2UgdGhlIHJlcXVlc3Qgd2FzIHJlY2VpdmVkXG4gICAgICAgIHNlbGYudHJpZ2dlcignbG9hZCcpO1xuXG4gICAgICB9XG5cbiAgICB9IGVsc2Uge1xuXG4gICAgICAvL3RyaWdnZXIgYW4gZXJyb3IgYmVjYXVzZSB0aGUgcmVxdWVzdCB3YXMgbm90IGhhbmRsZWRcbiAgICAgIHNlbGYucmVhZHlTdGF0ZSA9IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FO1xuICAgICAgc2VsZi50cmlnZ2VyKCdlcnJvcicpO1xuXG4gICAgfVxuXG4gIH0sIDApO1xuXG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmFib3J0ID0gZnVuY3Rpb24oKSB7XG4gIGNsZWFyVGltZW91dCh0aGlzLl9zZW5kVGltZW91dCk7XG5cbiAgaWYgKHRoaXMucmVhZHlTdGF0ZSA+IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9VTlNFTlQgJiYgdGhpcy5yZWFkeVN0YXRlIDwgTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0RPTkUpIHtcbiAgICB0aGlzLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UO1xuICAgIHRoaXMudHJpZ2dlcignYWJvcnQnKTtcbiAgfVxuXG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmdldEFsbFJlc3BvbnNlSGVhZGVycyA9IGZ1bmN0aW9uKCkge1xuXG4gIGlmICh0aGlzLnJlYWR5U3RhdGUgPCBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfSEVBREVSU19SRUNFSVZFRCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG5cbiAgdmFyIGhlYWRlcnMgPSAnJztcbiAgZm9yICh2YXIgbmFtZSBpbiB0aGlzLl9yZXNwb25zZUhlYWRlcnMpIHtcbiAgICBpZiAodGhpcy5fcmVzcG9uc2VIZWFkZXJzLmhhc093blByb3BlcnR5KG5hbWUpKSB7XG4gICAgICBoZWFkZXJzICs9IG5hbWUrJzogJyt0aGlzLl9yZXNwb25zZUhlYWRlcnNbbmFtZV0rJ1xcclxcbic7XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIGhlYWRlcnM7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmdldFJlc3BvbnNlSGVhZGVyID0gZnVuY3Rpb24obmFtZSkge1xuXG4gIGlmICh0aGlzLnJlYWR5U3RhdGUgPCBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfSEVBREVSU19SRUNFSVZFRCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG5cbiAgcmV0dXJuIHRoaXMuX3Jlc3BvbnNlSGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldIHx8IG51bGw7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLmFkZEV2ZW50TGlzdGVuZXIgPSBmdW5jdGlvbihldmVudCwgbGlzdGVuZXIpIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUucmVtb3ZlRXZlbnRMaXN0ZW5lciA9IGZ1bmN0aW9uKGV2ZW50LCBsaXN0ZW5lcikge1xuICB0aHJvdyBub3RJbXBsZW1lbnRlZEVycm9yO1xufTtcblxubW9kdWxlLmV4cG9ydHMgPSBNb2NrWE1MSHR0cFJlcXVlc3Q7XG4iLCJcInVzZSBzdHJpY3RcIjtcbnZhciB3aW5kb3cgPSByZXF1aXJlKFwiZ2xvYmFsL3dpbmRvd1wiKVxudmFyIGlzRnVuY3Rpb24gPSByZXF1aXJlKFwiaXMtZnVuY3Rpb25cIilcbnZhciBwYXJzZUhlYWRlcnMgPSByZXF1aXJlKFwicGFyc2UtaGVhZGVyc1wiKVxudmFyIHh0ZW5kID0gcmVxdWlyZShcInh0ZW5kXCIpXG5cbm1vZHVsZS5leHBvcnRzID0gY3JlYXRlWEhSXG5jcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QgPSB3aW5kb3cuWE1MSHR0cFJlcXVlc3QgfHwgbm9vcFxuY3JlYXRlWEhSLlhEb21haW5SZXF1ZXN0ID0gXCJ3aXRoQ3JlZGVudGlhbHNcIiBpbiAobmV3IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCgpKSA/IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCA6IHdpbmRvdy5YRG9tYWluUmVxdWVzdFxuXG5mb3JFYWNoQXJyYXkoW1wiZ2V0XCIsIFwicHV0XCIsIFwicG9zdFwiLCBcInBhdGNoXCIsIFwiaGVhZFwiLCBcImRlbGV0ZVwiXSwgZnVuY3Rpb24obWV0aG9kKSB7XG4gICAgY3JlYXRlWEhSW21ldGhvZCA9PT0gXCJkZWxldGVcIiA/IFwiZGVsXCIgOiBtZXRob2RdID0gZnVuY3Rpb24odXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgICAgICBvcHRpb25zID0gaW5pdFBhcmFtcyh1cmksIG9wdGlvbnMsIGNhbGxiYWNrKVxuICAgICAgICBvcHRpb25zLm1ldGhvZCA9IG1ldGhvZC50b1VwcGVyQ2FzZSgpXG4gICAgICAgIHJldHVybiBfY3JlYXRlWEhSKG9wdGlvbnMpXG4gICAgfVxufSlcblxuZnVuY3Rpb24gZm9yRWFjaEFycmF5KGFycmF5LCBpdGVyYXRvcikge1xuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgYXJyYXkubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgaXRlcmF0b3IoYXJyYXlbaV0pXG4gICAgfVxufVxuXG5mdW5jdGlvbiBpc0VtcHR5KG9iail7XG4gICAgZm9yKHZhciBpIGluIG9iail7XG4gICAgICAgIGlmKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkgcmV0dXJuIGZhbHNlXG4gICAgfVxuICAgIHJldHVybiB0cnVlXG59XG5cbmZ1bmN0aW9uIGluaXRQYXJhbXModXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgIHZhciBwYXJhbXMgPSB1cmlcblxuICAgIGlmIChpc0Z1bmN0aW9uKG9wdGlvbnMpKSB7XG4gICAgICAgIGNhbGxiYWNrID0gb3B0aW9uc1xuICAgICAgICBpZiAodHlwZW9mIHVyaSA9PT0gXCJzdHJpbmdcIikge1xuICAgICAgICAgICAgcGFyYW1zID0ge3VyaTp1cml9XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICBwYXJhbXMgPSB4dGVuZChvcHRpb25zLCB7dXJpOiB1cml9KVxuICAgIH1cblxuICAgIHBhcmFtcy5jYWxsYmFjayA9IGNhbGxiYWNrXG4gICAgcmV0dXJuIHBhcmFtc1xufVxuXG5mdW5jdGlvbiBjcmVhdGVYSFIodXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgIG9wdGlvbnMgPSBpbml0UGFyYW1zKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spXG4gICAgcmV0dXJuIF9jcmVhdGVYSFIob3B0aW9ucylcbn1cblxuZnVuY3Rpb24gX2NyZWF0ZVhIUihvcHRpb25zKSB7XG4gICAgaWYodHlwZW9mIG9wdGlvbnMuY2FsbGJhY2sgPT09IFwidW5kZWZpbmVkXCIpe1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3IoXCJjYWxsYmFjayBhcmd1bWVudCBtaXNzaW5nXCIpXG4gICAgfVxuXG4gICAgdmFyIGNhbGxlZCA9IGZhbHNlXG4gICAgdmFyIGNhbGxiYWNrID0gZnVuY3Rpb24gY2JPbmNlKGVyciwgcmVzcG9uc2UsIGJvZHkpe1xuICAgICAgICBpZighY2FsbGVkKXtcbiAgICAgICAgICAgIGNhbGxlZCA9IHRydWVcbiAgICAgICAgICAgIG9wdGlvbnMuY2FsbGJhY2soZXJyLCByZXNwb25zZSwgYm9keSlcbiAgICAgICAgfVxuICAgIH1cblxuICAgIGZ1bmN0aW9uIHJlYWR5c3RhdGVjaGFuZ2UoKSB7XG4gICAgICAgIGlmICh4aHIucmVhZHlTdGF0ZSA9PT0gNCkge1xuICAgICAgICAgICAgbG9hZEZ1bmMoKVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gZ2V0Qm9keSgpIHtcbiAgICAgICAgLy8gQ2hyb21lIHdpdGggcmVxdWVzdFR5cGU9YmxvYiB0aHJvd3MgZXJyb3JzIGFycm91bmQgd2hlbiBldmVuIHRlc3RpbmcgYWNjZXNzIHRvIHJlc3BvbnNlVGV4dFxuICAgICAgICB2YXIgYm9keSA9IHVuZGVmaW5lZFxuXG4gICAgICAgIGlmICh4aHIucmVzcG9uc2UpIHtcbiAgICAgICAgICAgIGJvZHkgPSB4aHIucmVzcG9uc2VcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGJvZHkgPSB4aHIucmVzcG9uc2VUZXh0IHx8IGdldFhtbCh4aHIpXG4gICAgICAgIH1cblxuICAgICAgICBpZiAoaXNKc29uKSB7XG4gICAgICAgICAgICB0cnkge1xuICAgICAgICAgICAgICAgIGJvZHkgPSBKU09OLnBhcnNlKGJvZHkpXG4gICAgICAgICAgICB9IGNhdGNoIChlKSB7fVxuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGJvZHlcbiAgICB9XG5cbiAgICB2YXIgZmFpbHVyZVJlc3BvbnNlID0ge1xuICAgICAgICAgICAgICAgIGJvZHk6IHVuZGVmaW5lZCxcbiAgICAgICAgICAgICAgICBoZWFkZXJzOiB7fSxcbiAgICAgICAgICAgICAgICBzdGF0dXNDb2RlOiAwLFxuICAgICAgICAgICAgICAgIG1ldGhvZDogbWV0aG9kLFxuICAgICAgICAgICAgICAgIHVybDogdXJpLFxuICAgICAgICAgICAgICAgIHJhd1JlcXVlc3Q6IHhoclxuICAgICAgICAgICAgfVxuXG4gICAgZnVuY3Rpb24gZXJyb3JGdW5jKGV2dCkge1xuICAgICAgICBjbGVhclRpbWVvdXQodGltZW91dFRpbWVyKVxuICAgICAgICBpZighKGV2dCBpbnN0YW5jZW9mIEVycm9yKSl7XG4gICAgICAgICAgICBldnQgPSBuZXcgRXJyb3IoXCJcIiArIChldnQgfHwgXCJVbmtub3duIFhNTEh0dHBSZXF1ZXN0IEVycm9yXCIpIClcbiAgICAgICAgfVxuICAgICAgICBldnQuc3RhdHVzQ29kZSA9IDBcbiAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGV2dCwgZmFpbHVyZVJlc3BvbnNlKVxuICAgIH1cblxuICAgIC8vIHdpbGwgbG9hZCB0aGUgZGF0YSAmIHByb2Nlc3MgdGhlIHJlc3BvbnNlIGluIGEgc3BlY2lhbCByZXNwb25zZSBvYmplY3RcbiAgICBmdW5jdGlvbiBsb2FkRnVuYygpIHtcbiAgICAgICAgaWYgKGFib3J0ZWQpIHJldHVyblxuICAgICAgICB2YXIgc3RhdHVzXG4gICAgICAgIGNsZWFyVGltZW91dCh0aW1lb3V0VGltZXIpXG4gICAgICAgIGlmKG9wdGlvbnMudXNlWERSICYmIHhoci5zdGF0dXM9PT11bmRlZmluZWQpIHtcbiAgICAgICAgICAgIC8vSUU4IENPUlMgR0VUIHN1Y2Nlc3NmdWwgcmVzcG9uc2UgZG9lc24ndCBoYXZlIGEgc3RhdHVzIGZpZWxkLCBidXQgYm9keSBpcyBmaW5lXG4gICAgICAgICAgICBzdGF0dXMgPSAyMDBcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHN0YXR1cyA9ICh4aHIuc3RhdHVzID09PSAxMjIzID8gMjA0IDogeGhyLnN0YXR1cylcbiAgICAgICAgfVxuICAgICAgICB2YXIgcmVzcG9uc2UgPSBmYWlsdXJlUmVzcG9uc2VcbiAgICAgICAgdmFyIGVyciA9IG51bGxcblxuICAgICAgICBpZiAoc3RhdHVzICE9PSAwKXtcbiAgICAgICAgICAgIHJlc3BvbnNlID0ge1xuICAgICAgICAgICAgICAgIGJvZHk6IGdldEJvZHkoKSxcbiAgICAgICAgICAgICAgICBzdGF0dXNDb2RlOiBzdGF0dXMsXG4gICAgICAgICAgICAgICAgbWV0aG9kOiBtZXRob2QsXG4gICAgICAgICAgICAgICAgaGVhZGVyczoge30sXG4gICAgICAgICAgICAgICAgdXJsOiB1cmksXG4gICAgICAgICAgICAgICAgcmF3UmVxdWVzdDogeGhyXG4gICAgICAgICAgICB9XG4gICAgICAgICAgICBpZih4aHIuZ2V0QWxsUmVzcG9uc2VIZWFkZXJzKXsgLy9yZW1lbWJlciB4aHIgY2FuIGluIGZhY3QgYmUgWERSIGZvciBDT1JTIGluIElFXG4gICAgICAgICAgICAgICAgcmVzcG9uc2UuaGVhZGVycyA9IHBhcnNlSGVhZGVycyh4aHIuZ2V0QWxsUmVzcG9uc2VIZWFkZXJzKCkpXG4gICAgICAgICAgICB9XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBlcnIgPSBuZXcgRXJyb3IoXCJJbnRlcm5hbCBYTUxIdHRwUmVxdWVzdCBFcnJvclwiKVxuICAgICAgICB9XG4gICAgICAgIHJldHVybiBjYWxsYmFjayhlcnIsIHJlc3BvbnNlLCByZXNwb25zZS5ib2R5KVxuICAgIH1cblxuICAgIHZhciB4aHIgPSBvcHRpb25zLnhociB8fCBudWxsXG5cbiAgICBpZiAoIXhocikge1xuICAgICAgICBpZiAob3B0aW9ucy5jb3JzIHx8IG9wdGlvbnMudXNlWERSKSB7XG4gICAgICAgICAgICB4aHIgPSBuZXcgY3JlYXRlWEhSLlhEb21haW5SZXF1ZXN0KClcbiAgICAgICAgfWVsc2V7XG4gICAgICAgICAgICB4aHIgPSBuZXcgY3JlYXRlWEhSLlhNTEh0dHBSZXF1ZXN0KClcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBrZXlcbiAgICB2YXIgYWJvcnRlZFxuICAgIHZhciB1cmkgPSB4aHIudXJsID0gb3B0aW9ucy51cmkgfHwgb3B0aW9ucy51cmxcbiAgICB2YXIgbWV0aG9kID0geGhyLm1ldGhvZCA9IG9wdGlvbnMubWV0aG9kIHx8IFwiR0VUXCJcbiAgICB2YXIgYm9keSA9IG9wdGlvbnMuYm9keSB8fCBvcHRpb25zLmRhdGEgfHwgbnVsbFxuICAgIHZhciBoZWFkZXJzID0geGhyLmhlYWRlcnMgPSBvcHRpb25zLmhlYWRlcnMgfHwge31cbiAgICB2YXIgc3luYyA9ICEhb3B0aW9ucy5zeW5jXG4gICAgdmFyIGlzSnNvbiA9IGZhbHNlXG4gICAgdmFyIHRpbWVvdXRUaW1lclxuXG4gICAgaWYgKFwianNvblwiIGluIG9wdGlvbnMpIHtcbiAgICAgICAgaXNKc29uID0gdHJ1ZVxuICAgICAgICBoZWFkZXJzW1wiYWNjZXB0XCJdIHx8IGhlYWRlcnNbXCJBY2NlcHRcIl0gfHwgKGhlYWRlcnNbXCJBY2NlcHRcIl0gPSBcImFwcGxpY2F0aW9uL2pzb25cIikgLy9Eb24ndCBvdmVycmlkZSBleGlzdGluZyBhY2NlcHQgaGVhZGVyIGRlY2xhcmVkIGJ5IHVzZXJcbiAgICAgICAgaWYgKG1ldGhvZCAhPT0gXCJHRVRcIiAmJiBtZXRob2QgIT09IFwiSEVBRFwiKSB7XG4gICAgICAgICAgICBoZWFkZXJzW1wiY29udGVudC10eXBlXCJdIHx8IGhlYWRlcnNbXCJDb250ZW50LVR5cGVcIl0gfHwgKGhlYWRlcnNbXCJDb250ZW50LVR5cGVcIl0gPSBcImFwcGxpY2F0aW9uL2pzb25cIikgLy9Eb24ndCBvdmVycmlkZSBleGlzdGluZyBhY2NlcHQgaGVhZGVyIGRlY2xhcmVkIGJ5IHVzZXJcbiAgICAgICAgICAgIGJvZHkgPSBKU09OLnN0cmluZ2lmeShvcHRpb25zLmpzb24pXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB4aHIub25yZWFkeXN0YXRlY2hhbmdlID0gcmVhZHlzdGF0ZWNoYW5nZVxuICAgIHhoci5vbmxvYWQgPSBsb2FkRnVuY1xuICAgIHhoci5vbmVycm9yID0gZXJyb3JGdW5jXG4gICAgLy8gSUU5IG11c3QgaGF2ZSBvbnByb2dyZXNzIGJlIHNldCB0byBhIHVuaXF1ZSBmdW5jdGlvbi5cbiAgICB4aHIub25wcm9ncmVzcyA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgLy8gSUUgbXVzdCBkaWVcbiAgICB9XG4gICAgeGhyLm9udGltZW91dCA9IGVycm9yRnVuY1xuICAgIHhoci5vcGVuKG1ldGhvZCwgdXJpLCAhc3luYywgb3B0aW9ucy51c2VybmFtZSwgb3B0aW9ucy5wYXNzd29yZClcbiAgICAvL2hhcyB0byBiZSBhZnRlciBvcGVuXG4gICAgaWYoIXN5bmMpIHtcbiAgICAgICAgeGhyLndpdGhDcmVkZW50aWFscyA9ICEhb3B0aW9ucy53aXRoQ3JlZGVudGlhbHNcbiAgICB9XG4gICAgLy8gQ2Fubm90IHNldCB0aW1lb3V0IHdpdGggc3luYyByZXF1ZXN0XG4gICAgLy8gbm90IHNldHRpbmcgdGltZW91dCBvbiB0aGUgeGhyIG9iamVjdCwgYmVjYXVzZSBvZiBvbGQgd2Via2l0cyBldGMuIG5vdCBoYW5kbGluZyB0aGF0IGNvcnJlY3RseVxuICAgIC8vIGJvdGggbnBtJ3MgcmVxdWVzdCBhbmQganF1ZXJ5IDEueCB1c2UgdGhpcyBraW5kIG9mIHRpbWVvdXQsIHNvIHRoaXMgaXMgYmVpbmcgY29uc2lzdGVudFxuICAgIGlmICghc3luYyAmJiBvcHRpb25zLnRpbWVvdXQgPiAwICkge1xuICAgICAgICB0aW1lb3V0VGltZXIgPSBzZXRUaW1lb3V0KGZ1bmN0aW9uKCl7XG4gICAgICAgICAgICBhYm9ydGVkPXRydWUvL0lFOSBtYXkgc3RpbGwgY2FsbCByZWFkeXN0YXRlY2hhbmdlXG4gICAgICAgICAgICB4aHIuYWJvcnQoXCJ0aW1lb3V0XCIpXG4gICAgICAgICAgICB2YXIgZSA9IG5ldyBFcnJvcihcIlhNTEh0dHBSZXF1ZXN0IHRpbWVvdXRcIilcbiAgICAgICAgICAgIGUuY29kZSA9IFwiRVRJTUVET1VUXCJcbiAgICAgICAgICAgIGVycm9yRnVuYyhlKVxuICAgICAgICB9LCBvcHRpb25zLnRpbWVvdXQgKVxuICAgIH1cblxuICAgIGlmICh4aHIuc2V0UmVxdWVzdEhlYWRlcikge1xuICAgICAgICBmb3Ioa2V5IGluIGhlYWRlcnMpe1xuICAgICAgICAgICAgaWYoaGVhZGVycy5oYXNPd25Qcm9wZXJ0eShrZXkpKXtcbiAgICAgICAgICAgICAgICB4aHIuc2V0UmVxdWVzdEhlYWRlcihrZXksIGhlYWRlcnNba2V5XSlcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH0gZWxzZSBpZiAob3B0aW9ucy5oZWFkZXJzICYmICFpc0VtcHR5KG9wdGlvbnMuaGVhZGVycykpIHtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKFwiSGVhZGVycyBjYW5ub3QgYmUgc2V0IG9uIGFuIFhEb21haW5SZXF1ZXN0IG9iamVjdFwiKVxuICAgIH1cblxuICAgIGlmIChcInJlc3BvbnNlVHlwZVwiIGluIG9wdGlvbnMpIHtcbiAgICAgICAgeGhyLnJlc3BvbnNlVHlwZSA9IG9wdGlvbnMucmVzcG9uc2VUeXBlXG4gICAgfVxuXG4gICAgaWYgKFwiYmVmb3JlU2VuZFwiIGluIG9wdGlvbnMgJiZcbiAgICAgICAgdHlwZW9mIG9wdGlvbnMuYmVmb3JlU2VuZCA9PT0gXCJmdW5jdGlvblwiXG4gICAgKSB7XG4gICAgICAgIG9wdGlvbnMuYmVmb3JlU2VuZCh4aHIpXG4gICAgfVxuXG4gICAgeGhyLnNlbmQoYm9keSlcblxuICAgIHJldHVybiB4aHJcblxuXG59XG5cbmZ1bmN0aW9uIGdldFhtbCh4aHIpIHtcbiAgICBpZiAoeGhyLnJlc3BvbnNlVHlwZSA9PT0gXCJkb2N1bWVudFwiKSB7XG4gICAgICAgIHJldHVybiB4aHIucmVzcG9uc2VYTUxcbiAgICB9XG4gICAgdmFyIGZpcmVmb3hCdWdUYWtlbkVmZmVjdCA9IHhoci5zdGF0dXMgPT09IDIwNCAmJiB4aHIucmVzcG9uc2VYTUwgJiYgeGhyLnJlc3BvbnNlWE1MLmRvY3VtZW50RWxlbWVudC5ub2RlTmFtZSA9PT0gXCJwYXJzZXJlcnJvclwiXG4gICAgaWYgKHhoci5yZXNwb25zZVR5cGUgPT09IFwiXCIgJiYgIWZpcmVmb3hCdWdUYWtlbkVmZmVjdCkge1xuICAgICAgICByZXR1cm4geGhyLnJlc3BvbnNlWE1MXG4gICAgfVxuXG4gICAgcmV0dXJuIG51bGxcbn1cblxuZnVuY3Rpb24gbm9vcCgpIHt9XG4iLCJtb2R1bGUuZXhwb3J0cyA9IGV4dGVuZFxuXG52YXIgaGFzT3duUHJvcGVydHkgPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5O1xuXG5mdW5jdGlvbiBleHRlbmQoKSB7XG4gICAgdmFyIHRhcmdldCA9IHt9XG5cbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykge1xuICAgICAgICB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldXG5cbiAgICAgICAgZm9yICh2YXIga2V5IGluIHNvdXJjZSkge1xuICAgICAgICAgICAgaWYgKGhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7XG4gICAgICAgICAgICAgICAgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XVxuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIHRhcmdldFxufVxuIiwiaW1wb3J0IHN0b3JlIGZyb20gXCIuLi9zdG9yZVwiO1xuaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5pbXBvcnQgbWFwcGluZ1RvSnNvbkxkUm1sIGZyb20gXCIuLi91dGlsL21hcHBpbmdUb0pzb25MZFJtbFwiXG52YXIgdG9Kc29uO1xuaWYgKHByb2Nlc3MuZW52Lk5PREVfRU5WID09PSBcImRldmVsb3BtZW50XCIpIHtcblx0dG9Kc29uID0gZnVuY3Rpb24gdG9Kc29uKGRhdGEpIHtcblx0XHRyZXR1cm4gSlNPTi5zdHJpbmdpZnkoZGF0YSwgdW5kZWZpbmVkLCAyKTtcblx0fVxufSBlbHNlIHtcblx0dG9Kc29uID0gZnVuY3Rpb24gdG9Kc29uKGRhdGEpIHtcblx0XHRyZXR1cm4gSlNPTi5zdHJpbmdpZnkoZGF0YSk7XG5cdH1cbn1cblxudmFyIGFjdGlvbnMgPSB7XG5cdG9uTG9hZE1vcmVDbGljazogZnVuY3Rpb24gKHVybCwgY29sbGVjdGlvbikge1xuXHRcdHN0b3JlLmRpc3BhdGNoKChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0XHRcdHZhciBzdGF0ZSA9IGdldFN0YXRlKCk7XG5cdFx0XHR2YXIgcGF5bG9hZCA9IHtcblx0XHRcdFx0aGVhZGVyczoge1xuXHRcdFx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcblx0XHRcdFx0fVxuXHRcdFx0fTtcblx0XHRcdHhoci5nZXQodXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwLCBib2R5KSB7XG5cdFx0XHRcdGlmIChlcnIpIHtcblx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRVJST1JcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgZXJyb3I6IGVyciB9KVxuXHRcdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRcdHRyeSB7XG5cdFx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfU1VDQ0VFREVEXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGRhdGE6IEpTT04ucGFyc2UoYm9keSl9KTtcblx0XHRcdFx0XHR9IGNhdGNoKGUpIHtcblx0XHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR19FUlJPUlwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBlcnJvcjogZSB9KVxuXHRcdFx0XHRcdH1cblx0XHRcdFx0fVxuXHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRklOSVNIRURcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbn0pXG5cdFx0XHR9KTtcblx0XHR9KVxuXG5cdH0sXG5cdG9uVXBsb2FkRmlsZVNlbGVjdDogZnVuY3Rpb24gKGZpbGVzKSB7XG5cdFx0bGV0IGZpbGUgPSBmaWxlc1swXTtcblx0XHRsZXQgZm9ybURhdGEgPSBuZXcgRm9ybURhdGEoKTtcblx0XHRmb3JtRGF0YS5hcHBlbmQoXCJmaWxlXCIsIGZpbGUpO1xuXHRcdGZvcm1EYXRhLmFwcGVuZChcInZyZVwiLCBmaWxlLm5hbWUpO1xuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNUQVJUX1VQTE9BRFwifSlcblx0XHRzdG9yZS5kaXNwYXRjaChmdW5jdGlvbiAoZGlzcGF0Y2gsIGdldFN0YXRlKSB7XG5cdFx0XHR2YXIgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuXHRcdFx0dmFyIHBheWxvYWQgPSB7XG5cdFx0XHRcdGJvZHk6IGZvcm1EYXRhLFxuXHRcdFx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcdFx0XCJBdXRob3JpemF0aW9uXCI6IHN0YXRlLnVzZXJkYXRhLnVzZXJJZFxuXHRcdFx0XHR9XG5cdFx0XHR9O1xuXHRcdFx0eGhyLnBvc3QocHJvY2Vzcy5lbnYuc2VydmVyICsgXCIvdjIuMS9idWxrLXVwbG9hZFwiLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwKSB7XG5cdFx0XHRcdGxldCBsb2NhdGlvbiA9IHJlc3AuaGVhZGVycy5sb2NhdGlvbjtcblx0XHRcdFx0eGhyLmdldChsb2NhdGlvbiwgZnVuY3Rpb24gKGVyciwgcmVzcCwgYm9keSkge1xuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkZJTklTSF9VUExPQURcIiwgZGF0YTogSlNPTi5wYXJzZShib2R5KX0pXG5cdFx0XHRcdH0pO1xuXHRcdFx0fSk7XG5cdFx0fSk7XG5cdH0sXG5cblx0b25TYXZlTWFwcGluZ3M6IGZ1bmN0aW9uICgpIHtcblx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTQVZFX1NUQVJURURcIn0pXG5cdFx0c3RvcmUuZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuXHRcdFx0dmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdHZhciBwYXlsb2FkID0ge1xuXHRcdFx0XHRqc29uOiBtYXBwaW5nVG9Kc29uTGRSbWwoc3RhdGUubWFwcGluZ3MsIHN0YXRlLmltcG9ydERhdGEudnJlKSxcblx0XHRcdFx0aGVhZGVyczoge1xuXHRcdFx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcblx0XHRcdFx0fVxuXHRcdFx0fTtcblxuXHRcdFx0eGhyLnBvc3Qoc3RhdGUuaW1wb3J0RGF0YS5zYXZlTWFwcGluZ1VybCwgcGF5bG9hZCwgZnVuY3Rpb24gKGVyciwgcmVzcCkge1xuXHRcdFx0XHRpZiAoZXJyKSB7XG5cdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9IQURfRVJST1JcIn0pXG5cdFx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9TVUNDRUVERURcIn0pXG5cdFx0XHRcdH1cblx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9GSU5JU0hFRFwifSlcblx0XHRcdH0pO1xuXHRcdH0pO1xuXHR9LFxuXG5cdG9uUHVibGlzaERhdGE6IGZ1bmN0aW9uICgpe1xuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfU1RBUlRFRFwifSlcblx0XHRzdG9yZS5kaXNwYXRjaChmdW5jdGlvbiAoZGlzcGF0Y2gsIGdldFN0YXRlKSB7XG5cdFx0XHR2YXIgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuXHRcdFx0dmFyIHBheWxvYWQgPSB7XG5cdFx0XHRcdGhlYWRlcnM6IHtcblx0XHRcdFx0XHRcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkXG5cdFx0XHRcdH1cblx0XHRcdH07XG5cblx0XHRcdHhoci5wb3N0KHN0YXRlLmltcG9ydERhdGEuZXhlY3V0ZU1hcHBpbmdVcmwsIHBheWxvYWQsIGZ1bmN0aW9uIChlcnIsIHJlc3ApIHtcblx0XHRcdFx0aWYgKGVycikge1xuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfSEFEX0VSUk9SXCJ9KVxuXHRcdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfU1VDQ0VFREVEXCJ9KVxuXHRcdFx0XHR9XG5cdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfRklOSVNIRURcIn0pXG5cdFx0XHR9KTtcblx0XHR9KTtcblx0fSxcblxuXHRvblNlbGVjdENvbGxlY3Rpb246IChjb2xsZWN0aW9uKSA9PiB7XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0FDVElWRV9DT0xMRUNUSU9OXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb259KTtcblx0XHRzdG9yZS5kaXNwYXRjaChmdW5jdGlvbiAoZGlzcGF0Y2gsIGdldFN0YXRlKSB7XG5cdFx0XHR2YXIgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuXHRcdFx0dmFyIGN1cnJlbnRTaGVldCA9IHN0YXRlLmltcG9ydERhdGEuc2hlZXRzLmZpbmQoeCA9PiB4LmNvbGxlY3Rpb24gPT09IGNvbGxlY3Rpb24pO1xuXHRcdFx0aWYgKGN1cnJlbnRTaGVldC5yb3dzLmxlbmd0aCA9PT0gMCAmJiBjdXJyZW50U2hlZXQubmV4dFVybCAmJiAhY3VycmVudFNoZWV0LmlzTG9hZGluZykge1xuXHRcdFx0XHR2YXIgcGF5bG9hZCA9IHtcblx0XHRcdFx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcdFx0XHRcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkXG5cdFx0XHRcdFx0fVxuXHRcdFx0XHR9O1xuXHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdcIiB9KVxuXHRcdFx0XHR4aHIuZ2V0KGN1cnJlbnRTaGVldC5uZXh0VXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwLCBib2R5KSB7XG5cdFx0XHRcdFx0aWYgKGVycikge1xuXHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlcnIgfSlcblx0XHRcdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRcdFx0dHJ5IHtcblx0XHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX1NVQ0NFRURFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBkYXRhOiBKU09OLnBhcnNlKGJvZHkpfSk7XG5cdFx0XHRcdFx0XHR9IGNhdGNoKGUpIHtcblx0XHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlIH0pXG5cdFx0XHRcdFx0XHR9XG5cdFx0XHRcdFx0fVxuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR19GSU5JU0hFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9ufSlcblx0XHRcdFx0fSk7XG5cdFx0XHR9XG5cdFx0fSk7XG5cdH0sXG5cblx0b25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlOiAoY29sbGVjdGlvbiwgdmFsdWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiTUFQX0NPTExFQ1RJT05fQVJDSEVUWVBFXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHZhbHVlOiB2YWx1ZX0pLFxuXG5cdG9uQ29uZmlybUNvbGxlY3Rpb25BcmNoZXR5cGVNYXBwaW5nczogKCkgPT4ge1xuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkNPTkZJUk1fQ09MTEVDVElPTl9BUkNIRVRZUEVfTUFQUElOR1NcIn0pXG5cdFx0c3RvcmUuZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuXHRcdFx0bGV0IHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdGFjdGlvbnMub25TZWxlY3RDb2xsZWN0aW9uKHN0YXRlLmltcG9ydERhdGEuYWN0aXZlQ29sbGVjdGlvbik7XG5cdFx0fSlcblx0fSxcblxuXHRvblNldEZpZWxkTWFwcGluZzogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQsIGltcG9ydGVkRmllbGQpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0ZJRUxEX01BUFBJTkdcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgaW1wb3J0ZWRGaWVsZDogaW1wb3J0ZWRGaWVsZH0pLFxuXG5cdG9uQ2xlYXJGaWVsZE1hcHBpbmc6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkLCBjbGVhckluZGV4KSA9PlxuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkNMRUFSX0ZJRUxEX01BUFBJTkdcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgY2xlYXJJbmRleDogY2xlYXJJbmRleH0pLFxuXG5cdG9uU2V0RGVmYXVsdFZhbHVlOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCwgdmFsdWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RFRkFVTFRfVkFMVUVcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgdmFsdWU6IHZhbHVlfSksXG5cblx0b25Db25maXJtRmllbGRNYXBwaW5nczogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiQ09ORklSTV9GSUVMRF9NQVBQSU5HU1wiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eUZpZWxkfSksXG5cblx0b25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCkgPT5cblx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJVTkNPTkZJUk1fRklFTERfTUFQUElOR1NcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZH0pLFxuXG5cdG9uU2V0VmFsdWVNYXBwaW5nOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCwgdGltVmFsdWUsIG1hcFZhbHVlKSA9PlxuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNFVF9WQUxVRV9NQVBQSU5HXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQ6IHByb3BlcnR5RmllbGQsIHRpbVZhbHVlOiB0aW1WYWx1ZSwgbWFwVmFsdWU6IG1hcFZhbHVlfSksXG5cblx0b25JZ25vcmVDb2x1bW5Ub2dnbGU6IChjb2xsZWN0aW9uLCB2YXJpYWJsZU5hbWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiVE9HR0xFX0lHTk9SRURfQ09MVU1OXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHZhcmlhYmxlTmFtZTogdmFyaWFibGVOYW1lfSksXG5cblx0b25BZGRDdXN0b21Qcm9wZXJ0eTogKGNvbGxlY3Rpb24sIHByb3BlcnR5TmFtZSwgcHJvcGVydHlUeXBlKSA9PlxuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkFERF9DVVNUT01fUFJPUEVSVFlcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlOYW1lLCBwcm9wZXJ0eVR5cGU6IHByb3BlcnR5VHlwZX0pLFxuXG5cdG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHk6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eU5hbWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiUkVNT1ZFX0NVU1RPTV9QUk9QRVJUWVwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eU5hbWV9KSxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGFjdGlvbnM7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBBcmNoZXR5cGVNYXBwaW5ncyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblxuXHRyZW5kZXIoKSB7XG5cblx0XHRjb25zdCB7IGFyY2hldHlwZSwgaW1wb3J0RGF0YSwgb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlLCBtYXBwaW5ncywgY29sbGVjdGlvbnNBcmVNYXBwZWQsIG9uQ29uZmlybUNvbGxlY3Rpb25BcmNoZXR5cGVNYXBwaW5ncyB9ID0gdGhpcy5wcm9wcztcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJyb3cgY2VudGVyZWQtZm9ybSBjZW50ZXItYmxvY2tcIj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgY29sLW1kLTEyXCIgc3R5bGU9e3t0ZXh0QWxpZ246IFwibGVmdFwifX0+XG5cdFx0XHRcdFx0PG1haW4+XG5cdFx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsIHBhbmVsLWRlZmF1bHQgY29sLW1kLTYgY29sLW1kLW9mZnNldC0zXCI+XG5cdFx0XHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicGFuZWwtYm9keVwiPlxuXHRcdFx0XHRcdFx0XHRcdFdlIGZvdW5kIHtpbXBvcnREYXRhLnNoZWV0cy5sZW5ndGh9IGNvbGxlY3Rpb25zIGluIHRoZSBmaWxlLjxiciAvPlxuXHRcdFx0XHRcdFx0XHRcdENvbm5lY3QgdGhlIHRhYnMgdG8gdGhlIHRpbWJ1Y3RvbyBhcmNoZXR5cGVzXG5cdFx0XHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdFx0XHQ8dWwgY2xhc3NOYW1lPVwibGlzdC1ncm91cFwiPlxuXHRcdFx0XHRcdFx0XHRcdHtpbXBvcnREYXRhLnNoZWV0cy5tYXAoKHNoZWV0LCBpKSA9PiAoXG5cdFx0XHRcdFx0XHRcdFx0XHQ8bGkgY2xhc3NOYW1lPVwibGlzdC1ncm91cC1pdGVtXCIga2V5PXtpfT5cblx0XHRcdFx0XHRcdFx0XHRcdFx0PGxhYmVsPntpICsgMX0ge3NoZWV0LmNvbGxlY3Rpb259PC9sYWJlbD5cblx0XHRcdFx0XHRcdFx0XHRcdFx0PFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0XHRcdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHNoZWV0LmNvbGxlY3Rpb24sIHZhbHVlKX1cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbk1hcENvbGxlY3Rpb25BcmNoZXR5cGUoc2hlZXQuY29sbGVjdGlvbiwgbnVsbCkgfVxuXHRcdFx0XHRcdFx0XHRcdFx0XHRcdG9wdGlvbnM9e09iamVjdC5rZXlzKGFyY2hldHlwZSkuZmlsdGVyKChkb21haW4pID0+IGRvbWFpbiAhPT0gXCJyZWxhdGlvbnNcIikuc29ydCgpfVxuXHRcdFx0XHRcdFx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPXtgQXJjaGV0eXBlIGZvciAke3NoZWV0LmNvbGxlY3Rpb259YH1cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHR2YWx1ZT17bWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0uYXJjaGV0eXBlTmFtZX0gLz5cblx0XHRcdFx0XHRcdFx0XHRcdDwvbGk+XG5cdFx0XHRcdFx0XHRcdFx0KSl9XG5cdFx0XHRcdFx0XHRcdFx0PGxpIGNsYXNzTmFtZT1cImxpc3QtZ3JvdXAtaXRlbVwiPlxuXHRcdFx0XHRcdFx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWxnIGJ0bi1zdWNjZXNzXCIgZGlzYWJsZWQ9eyFjb2xsZWN0aW9uc0FyZU1hcHBlZH0gb25DbGljaz17b25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzfT5cblx0XHRcdFx0XHRcdFx0XHRcdFx0T2tcblx0XHRcdFx0XHRcdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0XHRcdFx0XHRcdDwvbGk+XG5cdFx0XHRcdFx0XHRcdDwvdWw+XG5cdFx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHQ8L21haW4+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5BcmNoZXR5cGVNYXBwaW5ncy5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0Y29sbGVjdGlvbnNBcmVNYXBwZWQ6IFJlYWN0LlByb3BUeXBlcy5ib29sLFxuXHRpbXBvcnREYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0b25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgQXJjaGV0eXBlTWFwcGluZ3M7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQWRkUHJvcGVydHkgZnJvbSBcIi4vcHJvcGVydHktZm9ybS9hZGQtcHJvcGVydHlcIjtcbmltcG9ydCBQcm9wZXJ0eUZvcm0gZnJvbSBcIi4vcHJvcGVydHktZm9ybVwiO1xuXG5jbGFzcyBDb2xsZWN0aW9uRm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgaW1wb3J0RGF0YSwgYXJjaGV0eXBlLCBtYXBwaW5ncyB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IHsgYWN0aXZlQ29sbGVjdGlvbiwgc2hlZXRzIH0gPSBpbXBvcnREYXRhO1xuXG5cblx0XHRjb25zdCBjb2xsZWN0aW9uRGF0YSA9IHNoZWV0cy5maW5kKChzaGVldCkgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aXZlQ29sbGVjdGlvbik7XG5cdFx0Y29uc3QgbWFwcGluZ0RhdGEgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXTtcblxuXHRcdGNvbnN0IHsgYXJjaGV0eXBlTmFtZSB9ID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbYWN0aXZlQ29sbGVjdGlvbl07XG5cdFx0Y29uc3QgYXJjaGV0eXBlRmllbGRzID0gYXJjaGV0eXBlTmFtZSA/IGFyY2hldHlwZVthcmNoZXR5cGVOYW1lXSA6IFtdO1xuXHRcdGNvbnN0IGFyY2hlVHlwZVByb3BGaWVsZHMgPSBhcmNoZXR5cGVGaWVsZHMuZmlsdGVyKChhZikgPT4gYWYudHlwZSAhPT0gXCJyZWxhdGlvblwiKTtcblxuXHRcdGNvbnN0IHByb3BlcnR5Rm9ybXMgPSBhcmNoZVR5cGVQcm9wRmllbGRzXG5cdFx0XHQubWFwKChhZiwgaSkgPT4gPFByb3BlcnR5Rm9ybSB7Li4udGhpcy5wcm9wc30gY29sbGVjdGlvbkRhdGE9e2NvbGxlY3Rpb25EYXRhfSBtYXBwaW5nRGF0YT17bWFwcGluZ0RhdGF9IGN1c3RvbT17ZmFsc2V9IGtleT17aX0gbmFtZT17YWYubmFtZX0gdHlwZT17YWYudHlwZX0gLz4pO1xuXG5cdFx0Y29uc3QgY3VzdG9tUHJvcGVydHlGb3JtcyA9IG1hcHBpbmdzLmNvbGxlY3Rpb25zW2FjdGl2ZUNvbGxlY3Rpb25dLmN1c3RvbVByb3BlcnRpZXNcblx0XHRcdC5tYXAoKGNmLCBpKSA9PiA8UHJvcGVydHlGb3JtIHsuLi50aGlzLnByb3BzfSBjb2xsZWN0aW9uRGF0YT17Y29sbGVjdGlvbkRhdGF9IG1hcHBpbmdEYXRhPXttYXBwaW5nRGF0YX0gY3VzdG9tPXt0cnVlfSBrZXk9e2l9IG5hbWU9e2NmLm5hbWV9IHR5cGU9e2NmLnR5cGV9IC8+KTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsIHBhbmVsLWRlZmF1bHRcIj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJwYW5lbC1oZWFkaW5nXCI+XG5cdFx0XHRcdFx0Q29sbGVjdGlvbiBzZXR0aW5nczoge2FjdGl2ZUNvbGxlY3Rpb259XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDx1bCBjbGFzc05hbWU9XCJsaXN0LWdyb3VwXCI+XG5cdFx0XHRcdFx0e3Byb3BlcnR5Rm9ybXN9XG5cdFx0XHRcdFx0e2N1c3RvbVByb3BlcnR5Rm9ybXN9XG5cdFx0XHRcdFx0PEFkZFByb3BlcnR5IHsuLi50aGlzLnByb3BzfSAvPlxuXHRcdFx0XHQ8L3VsPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5Db2xsZWN0aW9uRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0aW1wb3J0RGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3Rcbn07XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25Gb3JtO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIENvbGxlY3Rpb25JbmRleCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0bWFwcGluZ3NBcmVDb21wbGV0ZShzaGVldCkge1xuXHRcdGNvbnN0IHsgbWFwcGluZ3MgfSA9IHRoaXMucHJvcHM7XG5cblx0XHRjb25zdCBjb25maXJtZWRDb2xDb3VudCA9IG1hcHBpbmdzLmNvbGxlY3Rpb25zW3NoZWV0LmNvbGxlY3Rpb25dLm1hcHBpbmdzXG5cdFx0XHQuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcblx0XHRcdC5tYXAoKG0pID0+IG0udmFyaWFibGUubWFwKCh2KSA9PiB2LnZhcmlhYmxlTmFtZSkpXG5cdFx0XHQucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pXG5cdFx0XHQuZmlsdGVyKCh4LCBpZHgsIHNlbGYpID0+IHNlbGYuaW5kZXhPZih4KSA9PT0gaWR4KVxuXHRcdFx0Lmxlbmd0aDtcblxuXHRcdHJldHVybiBjb25maXJtZWRDb2xDb3VudCArIG1hcHBpbmdzLmNvbGxlY3Rpb25zW3NoZWV0LmNvbGxlY3Rpb25dLmlnbm9yZWRDb2x1bW5zLmxlbmd0aCA9PT0gc2hlZXQudmFyaWFibGVzLmxlbmd0aDtcblx0fVxuXG5cdGFsbE1hcHBpbmdzQXJlSW5jb21wbGV0ZSgpIHtcblx0XHRjb25zdCB7IGltcG9ydERhdGEgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBzaGVldHMgfSA9IGltcG9ydERhdGE7XG5cdFx0cmV0dXJuIHNoZWV0c1xuXHRcdFx0Lm1hcCgoc2hlZXQpID0+IHRoaXMubWFwcGluZ3NBcmVDb21wbGV0ZShzaGVldCkpXG5cdFx0XHQuZmlsdGVyKChyZXN1bHQpID0+IHJlc3VsdCAhPT0gdHJ1ZSlcblx0XHRcdC5sZW5ndGggPT09IDA7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvblNhdmVNYXBwaW5ncywgb25QdWJsaXNoRGF0YSwgaW1wb3J0RGF0YSwgb25TZWxlY3RDb2xsZWN0aW9uIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgc2hlZXRzIH0gPSBpbXBvcnREYXRhO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicGFuZWwgcGFuZWwtZGVmYXVsdFwiPlxuXHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsLWhlYWRpbmdcIj5cblx0XHRcdFx0XHRDb2xsZWN0aW9uc1xuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJsaXN0LWdyb3VwXCI+XG5cdFx0XHRcdFx0eyBzaGVldHMubWFwKChzaGVldCwgaSkgPT4gKFxuXHRcdFx0XHRcdFx0PGFcblx0XHRcdFx0XHRcdFx0Y2xhc3NOYW1lPXtjeChcImxpc3QtZ3JvdXAtaXRlbVwiLCB7YWN0aXZlOiBzaGVldC5jb2xsZWN0aW9uID09PSBpbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24gfSl9XG5cdFx0XHRcdFx0XHRcdGtleT17aX1cblx0XHRcdFx0XHRcdFx0b25DbGljaz17KCkgPT4gb25TZWxlY3RDb2xsZWN0aW9uKHNoZWV0LmNvbGxlY3Rpb24pfVxuXHRcdFx0XHRcdFx0PlxuXHRcdFx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9e2N4KFwiZ2x5cGhpY29uXCIsIFwicHVsbC1yaWdodFwiLCB7XG5cdFx0XHRcdFx0XHRcdFx0XCJnbHlwaGljb24tcXVlc3Rpb24tc2lnblwiOiAhdGhpcy5tYXBwaW5nc0FyZUNvbXBsZXRlKHNoZWV0KSxcblx0XHRcdFx0XHRcdFx0XHRcImdseXBoaWNvbi1vay1zaWduXCI6IHRoaXMubWFwcGluZ3NBcmVDb21wbGV0ZShzaGVldClcblx0XHRcdFx0XHRcdFx0fSl9PlxuXHRcdFx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdFx0XHRcdHtzaGVldC5jb2xsZWN0aW9ufVxuXHRcdFx0XHRcdFx0PC9hPlxuXHRcdFx0XHRcdCkpIH1cblx0XHRcdFx0XHQ8bGkgY2xhc3NOYW1lPVwibGlzdC1ncm91cC1pdGVtXCI+XG5cdFx0XHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tc3VjY2Vzc1wiIG9uQ2xpY2s9e29uU2F2ZU1hcHBpbmdzfT5TYXZlPC9idXR0b24+XG5cdFx0XHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1zdWNjZXNzXCIgb25DbGljaz17b25QdWJsaXNoRGF0YX0gZGlzYWJsZWQ9eyF0aGlzLmFsbE1hcHBpbmdzQXJlSW5jb21wbGV0ZSgpfT5QdWJsaXNoPC9idXR0b24+XG5cdFx0XHRcdFx0PC9saT5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkNvbGxlY3Rpb25JbmRleC5wcm9wVHlwZXMgPSB7XG5cdG9uU2F2ZU1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25QdWJsaXNoRGF0YTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdGltcG9ydERhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRvblNlbGVjdENvbGxlY3Rpb246IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uSW5kZXg7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgRGF0YVJvdyBmcm9tIFwiLi90YWJsZS9kYXRhLXJvd1wiO1xuaW1wb3J0IEhlYWRlckNlbGwgZnJvbSBcIi4vdGFibGUvaGVhZGVyLWNlbGxcIjtcblxuXG5jbGFzcyBDb2xsZWN0aW9uVGFibGUgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGltcG9ydERhdGEsIG1hcHBpbmdzLCBvbklnbm9yZUNvbHVtblRvZ2dsZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IHNoZWV0cywgYWN0aXZlQ29sbGVjdGlvbiB9ID0gaW1wb3J0RGF0YTtcblx0XHRjb25zdCBjb2xsZWN0aW9uRGF0YSA9IHNoZWV0cy5maW5kKChzaGVldCkgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aXZlQ29sbGVjdGlvbik7XG5cblx0XHRjb25zdCB7IHJvd3MsIGNvbGxlY3Rpb24sIHZhcmlhYmxlcyB9ID0gY29sbGVjdGlvbkRhdGE7XG5cblx0XHRjb25zdCBjb25maXJtZWRDb2xzID0gdmFyaWFibGVzXG5cdFx0XHQubWFwKCh2YWx1ZSwgaSkgPT4gKHt2YWx1ZTogdmFsdWUsIGluZGV4OiBpfSkpXG5cdFx0XHQuZmlsdGVyKChjb2xTcGVjKSA9PiBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXS5tYXBwaW5nc1xuXHRcdFx0XHQuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcblx0XHRcdFx0Lm1hcCgobSkgPT4gbS52YXJpYWJsZS5tYXAoKHYpID0+IHYudmFyaWFibGVOYW1lKSlcblx0XHRcdFx0LnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKVxuXHRcdFx0XHQuaW5kZXhPZihjb2xTcGVjLnZhbHVlKSA+IC0xXG5cdFx0XHQpLm1hcCgoY29sU3BlYykgPT4gY29sU3BlYy5pbmRleCk7XG5cblx0XHRjb25zdCB7IGlnbm9yZWRDb2x1bW5zIH0gPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2PlxuXHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsIHBhbmVsLWRlZmF1bHRcIj5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsLWhlYWRpbmdcIj5cblx0XHRcdFx0XHRcdENvbGxlY3Rpb246IHtjb2xsZWN0aW9ufVxuXHRcdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdFx0PHRhYmxlIGNsYXNzTmFtZT1cInRhYmxlIHRhYmxlLWJvcmRlcmVkXCI+XG5cdFx0XHRcdFx0XHQ8dGhlYWQ+XG5cdFx0XHRcdFx0XHRcdDx0cj5cblx0XHRcdFx0XHRcdFx0XHR7dmFyaWFibGVzLm1hcCgoaGVhZGVyLCBpKSA9PiAoXG5cdFx0XHRcdFx0XHRcdFx0XHQ8SGVhZGVyQ2VsbFxuXHRcdFx0XHRcdFx0XHRcdFx0XHRhY3RpdmVDb2xsZWN0aW9uPXthY3RpdmVDb2xsZWN0aW9ufVxuXHRcdFx0XHRcdFx0XHRcdFx0XHRoZWFkZXI9e2hlYWRlcn1cblx0XHRcdFx0XHRcdFx0XHRcdFx0aXNDb25maXJtZWQ9e2NvbmZpcm1lZENvbHMuaW5kZXhPZihpKSA+IC0xfVxuXHRcdFx0XHRcdFx0XHRcdFx0XHRpc0lnbm9yZWQ9e2lnbm9yZWRDb2x1bW5zLmluZGV4T2YoaGVhZGVyKSA+IC0xfVxuXHRcdFx0XHRcdFx0XHRcdFx0XHRrZXk9e2l9XG5cdFx0XHRcdFx0XHRcdFx0XHRcdG9uSWdub3JlQ29sdW1uVG9nZ2xlPXtvbklnbm9yZUNvbHVtblRvZ2dsZX1cblx0XHRcdFx0XHRcdFx0XHRcdC8+XG5cdFx0XHRcdFx0XHRcdFx0KSl9XG5cdFx0XHRcdFx0XHRcdDwvdHI+XG5cdFx0XHRcdFx0XHQ8L3RoZWFkPlxuXHRcdFx0XHRcdFx0PHRib2R5PlxuXHRcdFx0XHRcdFx0eyByb3dzLm1hcCgocm93LCBpKSA9PiAoXG5cdFx0XHRcdFx0XHRcdDxEYXRhUm93XG5cdFx0XHRcdFx0XHRcdFx0Y29uZmlybWVkQ29scz17Y29uZmlybWVkQ29sc31cblx0XHRcdFx0XHRcdFx0XHRpZ25vcmVkQ29sdW1ucz17aWdub3JlZENvbHVtbnN9XG5cdFx0XHRcdFx0XHRcdFx0a2V5PXtpfVxuXHRcdFx0XHRcdFx0XHRcdHJvdz17cm93fVxuXHRcdFx0XHRcdFx0XHRcdHZhcmlhYmxlcz17dmFyaWFibGVzfVxuXHRcdFx0XHRcdFx0XHQvPlxuXHRcdFx0XHRcdFx0KSl9XG5cdFx0XHRcdFx0XHQ8L3Rib2R5PlxuXHRcdFx0XHRcdDwvdGFibGU+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8YnV0dG9uIG9uQ2xpY2s9eygpID0+IHRoaXMucHJvcHMub25Mb2FkTW9yZUNsaWNrICYmIHRoaXMucHJvcHMub25Mb2FkTW9yZUNsaWNrKGNvbGxlY3Rpb25EYXRhLm5leHRVcmwsIGNvbGxlY3Rpb24pfSBkaXNhYmxlZD17IWNvbGxlY3Rpb25EYXRhLm5leHRVcmx9IGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIHN0eWxlPXt7Y29sb3I6IFwiIzMzM1wiLCBiYWNrZ3JvdW5kQ29sb3I6IFwiI2ZmZlwiLCBcImJvcmRlckNvbG9yXCI6IFwiI2NjY1wiLCBmbG9hdDogXCJyaWdodFwifX0+bW9yZS4uLjwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5Db2xsZWN0aW9uVGFibGUucHJvcFR5cGVzID0ge1xuXHRpbXBvcnREYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0b25JZ25vcmVDb2x1bW5Ub2dnbGU6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFibGU7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQ29sbGVjdGlvbkluZGV4IGZyb20gXCIuL2NvbGxlY3Rpb24taW5kZXhcIjtcbmltcG9ydCBDb2xsZWN0aW9uVGFibGUgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJsZVwiO1xuaW1wb3J0IENvbGxlY3Rpb25Gb3JtIGZyb20gXCIuL2NvbGxlY3Rpb24tZm9ybVwiO1xuXG5jbGFzcyBEYXRhc2hlZXRNYXBwaW5ncyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJyb3dcIiBzdHlsZT17e3RleHRBbGlnbjogXCJsZWZ0XCJ9fT5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgY29sLW1kLTEyXCI+XG5cdFx0XHRcdFx0PG5hdiBjbGFzc05hbWU9XCJjb2wtc20tMlwiPlxuXHRcdFx0XHRcdFx0PENvbGxlY3Rpb25JbmRleCB7Li4udGhpcy5wcm9wc30gLz5cblx0XHRcdFx0XHQ8L25hdj5cblx0XHRcdFx0XHQ8bWFpbiBjbGFzc05hbWU9XCJjb2wtc20tMTBcIj5cblx0XHRcdFx0XHRcdDxDb2xsZWN0aW9uRm9ybSB7Li4udGhpcy5wcm9wc30gLz5cblx0XHRcdFx0XHRcdDxDb2xsZWN0aW9uVGFibGUgey4uLnRoaXMucHJvcHN9IC8+XG5cdFx0XHRcdFx0PC9tYWluPlxuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgRGF0YXNoZWV0TWFwcGluZ3M7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0ge1xuXHRcdFx0aXNPcGVuOiBmYWxzZVxuXHRcdH07XG5cdFx0dGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcblx0fVxuXG5cdGNvbXBvbmVudERpZE1vdW50KCkge1xuXHRcdGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuXHR9XG5cblx0Y29tcG9uZW50V2lsbFVubW91bnQoKSB7XG5cdFx0ZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG5cdH1cblxuXHR0b2dnbGVTZWxlY3QoKSB7XG5cdFx0aWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG5cdFx0fVxuXHR9XG5cblx0aGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuXHRcdGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuXHRcdGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe1xuXHRcdFx0XHRpc09wZW46IGZhbHNlXG5cdFx0XHR9KTtcblx0XHR9XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvcHRpb25zLCBvbkNoYW5nZSwgb25DbGVhciwgcGxhY2Vob2xkZXIsIHZhbHVlIH0gPSB0aGlzLnByb3BzO1xuXG5cdFx0cmV0dXJuIChcblxuXHRcdFx0PHNwYW4gY2xhc3NOYW1lPXtjeChcImRyb3Bkb3duXCIsIHtvcGVuOiB0aGlzLnN0YXRlLmlzT3Blbn0pfT5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHQgYnRuLXN4IGRyb3Bkb3duLXRvZ2dsZVwiXG5cdFx0XHRcdFx0b25DbGljaz17dGhpcy50b2dnbGVTZWxlY3QuYmluZCh0aGlzKX1cblx0XHRcdFx0XHRzdHlsZT17dmFsdWUgPyB7Y29sb3I6IFwiIzY2NlwifSA6IHtjb2xvcjogXCIjYWFhXCJ9IH0+XG5cdFx0XHRcdFx0e3ZhbHVlIHx8IHBsYWNlaG9sZGVyfSA8c3BhbiBjbGFzc05hbWU9XCJjYXJldFwiPjwvc3Bhbj5cblx0XHRcdFx0PC9idXR0b24+XG5cblx0XHRcdFx0PHVsIGNsYXNzTmFtZT1cImRyb3Bkb3duLW1lbnVcIj5cblx0XHRcdFx0XHR7IHZhbHVlID8gKFxuXHRcdFx0XHRcdFx0PGxpPlxuXHRcdFx0XHRcdFx0XHQ8YSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2xlYXIoKTsgdGhpcy50b2dnbGVTZWxlY3QoKTt9fT5cblx0XHRcdFx0XHRcdFx0XHQtIGNsZWFyIC1cblx0XHRcdFx0XHRcdFx0PC9hPlxuXHRcdFx0XHRcdFx0PC9saT5cblx0XHRcdFx0XHQpIDogbnVsbH1cblx0XHRcdFx0XHR7b3B0aW9ucy5tYXAoKG9wdGlvbiwgaSkgPT4gKFxuXHRcdFx0XHRcdFx0PGxpIGtleT17aX0+XG5cdFx0XHRcdFx0XHRcdDxhIG9uQ2xpY2s9eygpID0+IHsgb25DaGFuZ2Uob3B0aW9uKTsgdGhpcy50b2dnbGVTZWxlY3QoKTsgfX0+e29wdGlvbn08L2E+XG5cdFx0XHRcdFx0XHQ8L2xpPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHQ8L3VsPlxuXHRcdFx0PC9zcGFuPlxuXHRcdCk7XG5cdH1cblxufVxuXG5TZWxlY3RGaWVsZC5wcm9wVHlwZXMgPSB7XG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25DbGVhcjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcblx0cGxhY2Vob2xkZXI6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdHZhbHVlOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuaW1wb3J0IFVwbG9hZFNwbGFzaFNjcmVlbiBmcm9tIFwiLi91cGxvYWQtc3BsYXNoLXNjcmVlblwiO1xuaW1wb3J0IEFyY2hldHlwZU1hcHBpbmdzIGZyb20gXCIuL2FyY2hldHlwZS1tYXBwaW5nc1wiO1xuaW1wb3J0IERhdGFzaGVldE1hcHBpbmdzIGZyb20gXCIuL2RhdGFzaGVldC1tYXBwaW5nc1wiO1xuXG5jbGFzcyBBcHAgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgaW1wb3J0RGF0YSwgbWFwcGluZ3MgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgY29sbGVjdGlvbnNBcmVNYXBwZWQgPSBPYmplY3Qua2V5cyhtYXBwaW5ncy5jb2xsZWN0aW9ucykubGVuZ3RoID4gMCAmJlxuXHRcdFx0T2JqZWN0LmtleXMobWFwcGluZ3MuY29sbGVjdGlvbnMpLm1hcCgoa2V5KSA9PiBtYXBwaW5ncy5jb2xsZWN0aW9uc1trZXldLmFyY2hldHlwZU5hbWUpLmluZGV4T2YobnVsbCkgPCAwO1xuXG5cdFx0Y29uc3QgZGF0YXNoZWV0TWFwcGluZ3MgPSBpbXBvcnREYXRhLnNoZWV0cyAmJiBjb2xsZWN0aW9uc0FyZU1hcHBlZCAmJiBtYXBwaW5ncy5jb25maXJtZWQgP1xuXHRcdFx0PERhdGFzaGVldE1hcHBpbmdzIHsuLi50aGlzLnByb3BzfSAvPiA6IG51bGw7XG5cblx0XHRjb25zdCBhcmNoZXR5cGVNYXBwaW5ncyA9ICFkYXRhc2hlZXRNYXBwaW5ncyAmJiBpbXBvcnREYXRhLnNoZWV0cyA/XG5cdFx0XHQ8QXJjaGV0eXBlTWFwcGluZ3Mgey4uLnRoaXMucHJvcHN9IGNvbGxlY3Rpb25zQXJlTWFwcGVkPXtjb2xsZWN0aW9uc0FyZU1hcHBlZH0gLz4gOiBudWxsO1xuXG5cdFx0Y29uc3QgdXBsb2FkU3BsYXNoU2NyZWVuID0gIWRhdGFzaGVldE1hcHBpbmdzICYmICFhcmNoZXR5cGVNYXBwaW5ncyA/XG5cdFx0XHQ8VXBsb2FkU3BsYXNoU2NyZWVuIHsuLi50aGlzLnByb3BzfSAvPiA6IG51bGw7XG5cblx0XHRyZXR1cm4gZGF0YXNoZWV0TWFwcGluZ3MgfHwgYXJjaGV0eXBlTWFwcGluZ3MgfHwgdXBsb2FkU3BsYXNoU2NyZWVuO1xuXHR9XG59XG5cbkFwcC5wcm9wVHlwZXMgPSB7XG5cdGltcG9ydERhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0XG59O1xuXG5leHBvcnQgZGVmYXVsdCBBcHA7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEFkZFByb3BlcnR5IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRjb25zdHJ1Y3Rvcihwcm9wcykge1xuXHRcdHN1cGVyKHByb3BzKTtcblxuXHRcdHRoaXMuc3RhdGUgPSB7XG5cdFx0XHRuZXdOYW1lOiBudWxsLFxuXHRcdFx0bmV3VHlwZTogbnVsbFxuXHRcdH07XG5cdH1cblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGltcG9ydERhdGEsIGFyY2hldHlwZTogYWxsQXJjaGV0eXBlcywgbWFwcGluZ3MsIG9uQWRkQ3VzdG9tUHJvcGVydHkgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBuZXdUeXBlLCBuZXdOYW1lIH0gPSB0aGlzLnN0YXRlO1xuXG5cdFx0Y29uc3QgeyBhY3RpdmVDb2xsZWN0aW9uIH0gPSBpbXBvcnREYXRhO1xuXG5cdFx0Y29uc3QgeyBhcmNoZXR5cGVOYW1lIH0gPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXTtcblx0XHRjb25zdCBhcmNoZXR5cGUgPSBhbGxBcmNoZXR5cGVzW2FyY2hldHlwZU5hbWVdO1xuXG5cdFx0Y29uc3QgYXZhaWxhYmxlQXJjaGV0eXBlcyA9IE9iamVjdC5rZXlzKG1hcHBpbmdzLmNvbGxlY3Rpb25zKS5tYXAoKGtleSkgPT4gbWFwcGluZ3MuY29sbGVjdGlvbnNba2V5XS5hcmNoZXR5cGVOYW1lKTtcblx0XHRjb25zdCByZWxhdGlvblR5cGVPcHRpb25zID0gYXJjaGV0eXBlXG5cdFx0XHQuZmlsdGVyKChwcm9wKSA9PiBwcm9wLnR5cGUgPT09IFwicmVsYXRpb25cIilcblx0XHRcdC5maWx0ZXIoKHByb3ApID0+IGF2YWlsYWJsZUFyY2hldHlwZXMuaW5kZXhPZihwcm9wLnJlbGF0aW9uLnRhcmdldENvbGxlY3Rpb24pID4gLTEpXG5cdFx0XHQubWFwKChwcm9wKSA9PiBwcm9wLm5hbWUpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxsaSBjbGFzc05hbWU9XCJsaXN0LWdyb3VwLWl0ZW1cIj5cblx0XHRcdFx0PGxhYmVsPjxzdHJvbmc+QWRkIHByb3BlcnR5PC9zdHJvbmc+PC9sYWJlbD5cblx0XHRcdFx0PFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3VHlwZTogdmFsdWUsIG5ld05hbWU6IHZhbHVlID09PSBcInJlbGF0aW9uXCIgPyBudWxsIDogbmV3TmFtZX0pfVxuXHRcdFx0XHRcdG9uQ2xlYXI9eygpID0+IHRoaXMuc2V0U3RhdGUoe25ld1R5cGU6IG51bGx9KX1cblx0XHRcdFx0XHRvcHRpb25zPXtbXCJ0ZXh0XCIsIFwicmVsYXRpb25cIl19XG5cdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJDaG9vc2UgYSB0eXBlLi4uXCJcblx0XHRcdFx0XHR2YWx1ZT17bmV3VHlwZX0gLz5cblx0XHRcdFx0Jm5ic3A7XG5cdFx0XHRcdHsgbmV3VHlwZSA9PT0gXCJyZWxhdGlvblwiID9cblx0XHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IHZhbHVlfSl9XG5cdFx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiB0aGlzLnNldFN0YXRlKHtuZXdOYW1lOiBudWxsfSl9XG5cdFx0XHRcdFx0XHRvcHRpb25zPXtyZWxhdGlvblR5cGVPcHRpb25zfVxuXHRcdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJDaG9vc2UgYSB0eXBlLi4uXCJcblx0XHRcdFx0XHRcdHZhbHVlPXtuZXdOYW1lfSAvPlxuXHRcdFx0XHRcdDpcblx0XHRcdFx0XHQoPGlucHV0IG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IGV2LnRhcmdldC52YWx1ZSB9KX0gcGxhY2Vob2xkZXI9XCJQcm9wZXJ0eSBuYW1lXCIgdmFsdWU9e25ld05hbWV9IC8+KVxuXHRcdFx0XHR9XG5cdFx0XHRcdCZuYnNwO1xuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tc3VjY2Vzc1wiIGRpc2FibGVkPXshKG5ld05hbWUgJiYgbmV3VHlwZSl9XG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4ge1xuXHRcdFx0XHRcdFx0b25BZGRDdXN0b21Qcm9wZXJ0eShhY3RpdmVDb2xsZWN0aW9uLCBuZXdOYW1lLCBuZXdUeXBlKTtcblx0XHRcdFx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IG51bGwsIG5ld1R5cGU6IG51bGx9KTtcblx0XHRcdFx0XHR9fT5cblx0XHRcdFx0XHRBZGRcblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2xpPlxuXHRcdCk7XG5cdH1cbn1cblxuQWRkUHJvcGVydHkucHJvcFR5cGVzID0ge1xuXHRpbXBvcnREYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0b25BZGRDdXN0b21Qcm9wZXJ0eTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEFkZFByb3BlcnR5O1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5pbXBvcnQgTGlua3MgZnJvbSBcIi4vbGlua3NcIjtcbmltcG9ydCBUZXh0IGZyb20gXCIuL3RleHRcIjtcbmltcG9ydCBTZWxlY3QgZnJvbSBcIi4vc2VsZWN0XCI7XG5pbXBvcnQgTmFtZXMgZnJvbSBcIi4vbmFtZXNcIjtcbmltcG9ydCBSZWxhdGlvbiBmcm9tIFwiLi9yZWxhdGlvblwiO1xuXG5jb25zdCB0eXBlTWFwID0ge1xuXHR0ZXh0OiAocHJvcHMpID0+IDxUZXh0IHsuLi5wcm9wc30gLz4sXG5cdGRhdGFibGU6IChwcm9wcykgPT4gPFRleHQgey4uLnByb3BzfSAvPixcblx0bmFtZXM6IChwcm9wcykgPT4gPE5hbWVzIHsuLi5wcm9wc30gLz4sXG5cdGxpbmtzOiAocHJvcHMpID0+IDxMaW5rcyB7Li4ucHJvcHN9IC8+LFxuXHRzZWxlY3Q6IChwcm9wcykgPT4gPFNlbGVjdCB7Li4ucHJvcHN9IC8+LFxuXHRtdWx0aXNlbGVjdDogKHByb3BzKSA9PiA8U2VsZWN0IHsuLi5wcm9wc30gLz4sXG5cdHJlbGF0aW9uOiAocHJvcHMpID0+IDxSZWxhdGlvbiB7Li4ucHJvcHN9IC8+XG59O1xuXG5jbGFzcyBQcm9wZXJ0eUZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdGNhbkNvbmZpcm0odmFyaWFibGUpIHtcblx0XHRjb25zdCB7IHR5cGUgfSA9IHRoaXMucHJvcHM7XG5cdFx0aWYgKCF2YXJpYWJsZSB8fCB2YXJpYWJsZS5sZW5ndGggPT09IDApIHsgcmV0dXJuIGZhbHNlOyB9XG5cdFx0aWYgKHR5cGUgPT09IFwicmVsYXRpb25cIikge1xuXHRcdFx0cmV0dXJuIHZhcmlhYmxlWzBdLnZhcmlhYmxlTmFtZSAmJiB2YXJpYWJsZVswXS50YXJnZXRDb2xsZWN0aW9uICYmIHZhcmlhYmxlWzBdLnRhcmdldFZhcmlhYmxlTmFtZTtcblx0XHR9XG5cdFx0cmV0dXJuIHZhcmlhYmxlLmZpbHRlcigobSkgPT4gbS52YXJpYWJsZU5hbWUpLmxlbmd0aCA9PT0gdmFyaWFibGUubGVuZ3RoO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgY3VzdG9tLCBuYW1lLCBjb2xsZWN0aW9uRGF0YSwgdHlwZSwgbWFwcGluZ3MsIG9uQ29uZmlybUZpZWxkTWFwcGluZ3MsIG9uVW5jb25maXJtRmllbGRNYXBwaW5ncywgb25SZW1vdmVDdXN0b21Qcm9wZXJ0eSB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblxuXHRcdGNvbnN0IHByb3BlcnR5TWFwcGluZyA9IG1hcHBpbmcuZmluZCgobSkgPT4gbS5wcm9wZXJ0eSA9PT0gbmFtZSkgfHwge307XG5cdFx0Y29uc3QgY29uZmlybWVkID0gcHJvcGVydHlNYXBwaW5nLmNvbmZpcm1lZCB8fCBmYWxzZTtcblxuXHRcdGNvbnN0IGNvbmZpcm1CdXR0b24gPSB0aGlzLmNhbkNvbmZpcm0ocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IG51bGwpICYmICFjb25maXJtZWQgP1xuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tc3VjY2VzcyBidG4tc21cIiBvbkNsaWNrPXsoKSA9PiBvbkNvbmZpcm1GaWVsZE1hcHBpbmdzKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUpfT5Db25maXJtPC9idXR0b24+IDogY29uZmlybWVkID9cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRhbmdlciBidG4tc21cIiBvbkNsaWNrPXsoKSA9PiBvblVuY29uZmlybUZpZWxkTWFwcGluZ3MoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSl9PlVuY29uZmlybTwvYnV0dG9uPiA6IG51bGw7XG5cblxuXHRcdGNvbnN0IGZvcm1Db21wb25lbnQgPSB0eXBlTWFwW3R5cGVdKHRoaXMucHJvcHMpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxsaSBjbGFzc05hbWU9XCJsaXN0LWdyb3VwLWl0ZW1cIj5cblx0XHRcdFx0e2N1c3RvbSA/IChcblx0XHRcdFx0XHQ8YSBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0IGJ0bi1kYW5nZXIgYnRuLXhzXCIgb25DbGljaz17KCkgPT4gb25SZW1vdmVDdXN0b21Qcm9wZXJ0eShjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lKX0+XG5cdFx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdFx0PC9hPikgOiBudWxsfVxuXG5cdFx0XHRcdDxsYWJlbD48c3Ryb25nPntuYW1lfTwvc3Ryb25nPiAoe3R5cGV9KTwvbGFiZWw+XG5cdFx0XHRcdHtmb3JtQ29tcG9uZW50fVxuXHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0e2NvbmZpcm1CdXR0b259XG5cdFx0XHQ8L2xpPlxuXHRcdCk7XG5cdH1cbn1cblxuUHJvcGVydHlGb3JtLnByb3BUeXBlcyA9IHtcblx0Y29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdGN1c3RvbTogUmVhY3QuUHJvcFR5cGVzLmJvb2wsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNvbmZpcm1GaWVsZE1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25SZW1vdmVDdXN0b21Qcm9wZXJ0eTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uVW5jb25maXJtRmllbGRNYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdHR5cGU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmdcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFByb3BlcnR5Rm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBvblNldERlZmF1bHRWYWx1ZSwgbWFwcGluZ3MsIG5hbWV9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblx0XHRjb25zdCBwcm9wZXJ0eU1hcHBpbmcgPSBtYXBwaW5nLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IG5hbWUpIHx8IHt9O1xuXHRcdGNvbnN0IHNlbGVjdGVkVmFyaWFibGVVcmwgPSAocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5maW5kKCh2KSA9PiB2LmZpZWxkID09PSBcInVybFwiKSB8fCB7fTtcblx0XHRjb25zdCBkZWZhdWx0VmFsdWVVcmwgPSAocHJvcGVydHlNYXBwaW5nLmRlZmF1bHRWYWx1ZSB8fCBbXSkuZmluZCgodikgPT4gdi5maWVsZCA9PT0gXCJ1cmxcIikgfHwge307XG5cblx0XHRjb25zdCBzZWxlY3RlZFZhcmlhYmxlTGFiZWwgPSAocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5maW5kKCh2KSA9PiB2LmZpZWxkID09PSBcImxhYmVsXCIpIHx8IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRWYWx1ZUxhYmVsID0gKHByb3BlcnR5TWFwcGluZy5kZWZhdWx0VmFsdWUgfHwgW10pLmZpbmQoKHYpID0+IHYuZmllbGQgPT09IFwibGFiZWxcIikgfHwge307XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PHNwYW4+XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7Li4uc2VsZWN0ZWRWYXJpYWJsZVVybH0sIHsuLi5zZWxlY3RlZFZhcmlhYmxlTGFiZWwsIGZpZWxkOiBcImxhYmVsXCIsIHZhcmlhYmxlTmFtZTogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25DbGVhckZpZWxkTWFwcGluZyhjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCAocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5tYXAoKHYpID0+IHYuZmllbGQpLmluZGV4T2YoXCJsYWJlbFwiKSl9XG5cdFx0XHRcdFx0b3B0aW9ucz17Y29sbGVjdGlvbkRhdGEudmFyaWFibGVzfSBwbGFjZWhvbGRlcj1cIlNlbGVjdCBsYWJlbCBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtzZWxlY3RlZFZhcmlhYmxlTGFiZWwudmFyaWFibGVOYW1lIHx8IG51bGx9IC8+XG5cdFx0XHRcdCZuYnNwO1xuXG5cdFx0XHRcdHtzZWxlY3RlZFZhcmlhYmxlVXJsLnZhcmlhYmxlTmFtZSAmJiBzZWxlY3RlZFZhcmlhYmxlTGFiZWwudmFyaWFibGVOYW1lID8gKFxuXHRcdFx0XHRcdDxpbnB1dCBvbkNoYW5nZT17KGV2KSA9PiBvblNldERlZmF1bHRWYWx1ZShjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCBbey4uLmRlZmF1bHRWYWx1ZVVybH0sIHsuLi5kZWZhdWx0VmFsdWVMYWJlbCwgZmllbGQ6IFwibGFiZWxcIiwgdmFsdWU6IGV2LnRhcmdldC52YWx1ZX1dKX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiRGVmYXVsdCB2YWx1ZS4uLlwiIHR5cGU9XCJ0ZXh0XCIgdmFsdWU9e2RlZmF1bHRWYWx1ZUxhYmVsLnZhbHVlIHx8IG51bGx9IC8+KSA6IG51bGx9XG5cblx0XHRcdFx0Jm5ic3A7XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7Li4uc2VsZWN0ZWRWYXJpYWJsZVVybCwgZmllbGQ6IFwidXJsXCIsIHZhcmlhYmxlTmFtZTogdmFsdWV9LCB7Li4uc2VsZWN0ZWRWYXJpYWJsZUxhYmVsfV0pfVxuXHRcdFx0XHRcdG9uQ2xlYXI9eygpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgKHByb3BlcnR5TWFwcGluZy52YXJpYWJsZSB8fCBbXSkubWFwKCh2KSA9PiB2LmZpZWxkKS5pbmRleE9mKFwidXJsXCIpKX1cblx0XHRcdFx0XHRvcHRpb25zPXtjb2xsZWN0aW9uRGF0YS52YXJpYWJsZXN9IHBsYWNlaG9sZGVyPVwiU2VsZWN0IFVSTCBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtzZWxlY3RlZFZhcmlhYmxlVXJsLnZhcmlhYmxlTmFtZSB8fCBudWxsfSAvPlxuXHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0e3NlbGVjdGVkVmFyaWFibGVVcmwudmFyaWFibGVOYW1lICYmIHNlbGVjdGVkVmFyaWFibGVMYWJlbC52YXJpYWJsZU5hbWUgPyAoXG5cdFx0XHRcdFx0PGlucHV0IG9uQ2hhbmdlPXsoZXYpID0+IG9uU2V0RGVmYXVsdFZhbHVlKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7Li4uZGVmYXVsdFZhbHVlVXJsLCBmaWVsZDogXCJ1cmxcIiwgdmFsdWU6IGV2LnRhcmdldC52YWx1ZX0sIHsuLi5kZWZhdWx0VmFsdWVMYWJlbH1dKX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiRGVmYXVsdCB2YWx1ZS4uLlwiIHR5cGU9XCJ0ZXh0XCIgdmFsdWU9e2RlZmF1bHRWYWx1ZVVybC52YWx1ZSB8fCBudWxsfSAvPikgOiBudWxsfVxuXHRcdFx0PC9zcGFuPlxuXHRcdCk7XG5cdH1cbn1cblxuRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGNvbGxlY3Rpb25EYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DbGVhckZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uU2V0RGVmYXVsdFZhbHVlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZXRGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGb3JtOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5cbmNsYXNzIEZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdG9uQ29tcG9uZW50Q2hhbmdlKHByb3BlcnR5TWFwcGluZywgbWFwcGluZ0luZGV4LCB2YXJpYWJsZU5hbWUpIHtcblx0XHRjb25zdCB7IGNvbGxlY3Rpb25EYXRhLCBvblNldEZpZWxkTWFwcGluZywgbmFtZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB2YXJpYWJsZVNwZWMgPSBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGVcblx0XHRcdC5tYXAoKHYsIGkpID0+IGkgPT09IG1hcHBpbmdJbmRleCA/IHsuLi52LCB2YXJpYWJsZU5hbWU6IHZhcmlhYmxlTmFtZX0gOiB2KTtcblxuXHRcdGlmICh2YXJpYWJsZVNwZWMubGVuZ3RoID4gMCkge1xuXHRcdFx0b25TZXRGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgdmFyaWFibGVTcGVjKTtcblx0XHR9XG5cdH1cblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBtYXBwaW5ncywgbmFtZSwgYXJjaGV0eXBlfSA9IHRoaXMucHJvcHM7XG5cblx0XHRjb25zdCBtYXBwaW5nID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbl0ubWFwcGluZ3M7XG5cdFx0Y29uc3QgcHJvcGVydHlNYXBwaW5nID0gbWFwcGluZy5maW5kKChtKSA9PiBtLnByb3BlcnR5ID09PSBuYW1lKSB8fCB7fTtcblx0XHRjb25zdCBjb21wb25lbnRzID0gYXJjaGV0eXBlW21hcHBpbmdzLmNvbGxlY3Rpb25zW2NvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb25dLmFyY2hldHlwZU5hbWVdLmZpbmQoKGEpID0+IGEubmFtZSA9PT0gbmFtZSkub3B0aW9ucztcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8c3Bhbj5cblx0XHRcdFx0e3Byb3BlcnR5TWFwcGluZy52YXJpYWJsZSAmJiBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGUubGVuZ3RoID8gKFxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7bWFyZ2luQm90dG9tOiBcIjEycHhcIn19PlxuXHRcdFx0XHRcdHsocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5tYXAoKHYsIGkpID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17aX0gc3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXJnaW46IFwiOHB4IDhweCAwIDBcIn19PlxuXHRcdFx0XHRcdFx0XHQ8ZGl2IHN0eWxlPXt7bWFyZ2luQm90dG9tOiBcIjJweFwifX0+XG5cdFx0XHRcdFx0XHRcdFx0PGEgY2xhc3NOYW1lPVwicHVsbC1yaWdodCBidG4tZGFuZ2VyIGJ0bi14c1wiIG9uQ2xpY2s9eygpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgaSl9PlxuXHRcdFx0XHRcdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHRcdFx0XHRcdDwvYT5cblx0XHRcdFx0XHRcdFx0XHR7di5jb21wb25lbnR9Jm5ic3A7XG5cdFx0XHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiB0aGlzLm9uQ29tcG9uZW50Q2hhbmdlKHByb3BlcnR5TWFwcGluZywgaSwgdmFsdWUpfVxuXHRcdFx0XHRcdFx0XHRcdG9uQ2xlYXI9eygpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgaSl9XG5cdFx0XHRcdFx0XHRcdFx0b3B0aW9ucz17Y29sbGVjdGlvbkRhdGEudmFyaWFibGVzfVxuXHRcdFx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPXtgU2VsZWN0IGNvbHVtbiBmb3IgJHt2LmNvbXBvbmVudH1gfVxuXHRcdFx0XHRcdFx0XHRcdHZhbHVlPXt2LnZhcmlhYmxlTmFtZX0gLz5cblx0XHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9kaXY+KSA6IG51bGx9XG5cblx0XHRcdFx0PFNlbGVjdEZpZWxkIG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFsuLi4ocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKSwge2NvbXBvbmVudDogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b3B0aW9ucz17Y29tcG9uZW50c30gcGxhY2Vob2xkZXI9XCJBZGQgbmFtZSBjb21wb25lbnQuLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtudWxsfSAvPlxuXHRcdFx0PC9zcGFuPlxuXHRcdCk7XG5cdH1cbn1cblxuRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0Y29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNsZWFyRmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZXRGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGb3JtOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5cbmNsYXNzIEZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IG93blNoZWV0ID0gdGhpcy5wcm9wcy5jb2xsZWN0aW9uRGF0YTtcblx0XHRjb25zdCBhbGxTaGVldHMgPSB0aGlzLnByb3BzLmltcG9ydERhdGEuc2hlZXRzO1xuXHRcdGNvbnN0IGFsbFNoZWV0TWFwcGluZ3MgPSB0aGlzLnByb3BzLm1hcHBpbmdzLmNvbGxlY3Rpb25zO1xuXHRcdGNvbnN0IG1hcHBpbmcgPSB0aGlzLnByb3BzLm1hcHBpbmdEYXRhLm1hcHBpbmdzLmZpbmQocHJvcCA9PiBwcm9wLnByb3BlcnR5ID09PSB0aGlzLnByb3BzLm5hbWUpO1xuXHRcdC8vYXQgb25lIHBvaW50IHRoZSBtYXBwaW5nIGRvZXMgbm90IHlldCBleGlzdHMsIGJ1dCBhIGN1c3RvbSBwcm9wZXJ0eSByZWZlcmVuY2UgY29udGFpbmluZyB0aGUgbmFtZSBkb2VzIGV4aXN0XG5cdFx0Y29uc3QgY3VzdG9tUHJvcGVydHkgPSB0aGlzLnByb3BzLm1hcHBpbmdEYXRhLmN1c3RvbVByb3BlcnRpZXMuZmluZChwcm9wID0+IHByb3AubmFtZSA9PT0gdGhpcy5wcm9wcy5uYW1lKTtcblx0XHRjb25zdCBvd25BcmNoZXR5cGUgPSB0aGlzLnByb3BzLmFyY2hldHlwZVt0aGlzLnByb3BzLm1hcHBpbmdEYXRhLmFyY2hldHlwZU5hbWVdO1xuXHRcdGNvbnN0IG9uU2V0RmllbGRNYXBwaW5nID0gdGhpcy5wcm9wcy5vblNldEZpZWxkTWFwcGluZy5iaW5kKG51bGwsIG93blNoZWV0LmNvbGxlY3Rpb24sIHRoaXMucHJvcHMubmFtZSk7XG5cdFx0Y29uc3Qgb25DbGVhckZpZWxkTWFwcGluZyA9IHRoaXMucHJvcHMub25DbGVhckZpZWxkTWFwcGluZy5iaW5kKG51bGwsIG93blNoZWV0LmNvbGxlY3Rpb24sIHRoaXMucHJvcHMubmFtZSk7XG5cblx0XHRjb25zdCByZWxhdGlvbkluZm8gPSBtYXBwaW5nICYmIG1hcHBpbmcudmFyaWFibGUgJiYgbWFwcGluZy52YXJpYWJsZS5sZW5ndGggPiAwXG5cdFx0XHQ/IG1hcHBpbmcudmFyaWFibGVbMF1cblx0XHRcdDoge307XG5cblx0XHRjb25zdCBwcm9wZXJ0eU1ldGFkYXRhID0gb3duQXJjaGV0eXBlLmZpbmQobWV0YWRhdGEgPT4gbWV0YWRhdGEubmFtZSA9PT0gKG1hcHBpbmcgPyBtYXBwaW5nLnByb3BlcnR5IDogY3VzdG9tUHJvcGVydHkubmFtZSkpO1xuXG5cdFx0Y29uc3QgYXZhaWxhYmxlU2hlZXRzID0gcHJvcGVydHlNZXRhZGF0YVxuXHRcdFx0PyBPYmplY3Qua2V5cyhhbGxTaGVldE1hcHBpbmdzKVxuXHRcdFx0XHQuZmlsdGVyKGtleSA9PiBhbGxTaGVldE1hcHBpbmdzW2tleV0uYXJjaGV0eXBlTmFtZSA9PT0gcHJvcGVydHlNZXRhZGF0YS5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uKVxuXHRcdFx0OiBbXTtcblxuXHRcdGNvbnN0IGxpbmtlZFNoZWV0ID0gcmVsYXRpb25JbmZvLnRhcmdldENvbGxlY3Rpb25cblx0XHRcdD8gYWxsU2hlZXRzXG5cdFx0XHRcdC5maW5kKHNoZWV0ID0+IHNoZWV0LmNvbGxlY3Rpb24gPT09IHJlbGF0aW9uSW5mby50YXJnZXRDb2xsZWN0aW9uKVxuXHRcdFx0OiBudWxsO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxzcGFuPlxuXHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhbey4uLnJlbGF0aW9uSW5mbywgdmFyaWFibGVOYW1lOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbkNsZWFyRmllbGRNYXBwaW5nKDApfVxuXHRcdFx0XHRcdG9wdGlvbnM9e293blNoZWV0LnZhcmlhYmxlc30gcGxhY2Vob2xkZXI9XCJTZWxlY3Qgc291cmNlIGNvbHVtbi4uLlwiXG5cdFx0XHRcdFx0dmFsdWU9e3JlbGF0aW9uSW5mby52YXJpYWJsZU5hbWUgfHwgbnVsbH0gLz5cblx0XHRcdFx0Jm5ic3A7XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKFt7Li4ucmVsYXRpb25JbmZvLCB0YXJnZXRDb2xsZWN0aW9uOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbkNsZWFyRmllbGRNYXBwaW5nKDApfVxuXHRcdFx0XHRcdG9wdGlvbnM9e2F2YWlsYWJsZVNoZWV0c30gcGxhY2Vob2xkZXI9XCJTZWxlY3QgYSB0YXJnZXQgY29sbGVjdGlvbi4uLlwiXG5cdFx0XHRcdFx0dmFsdWU9e3JlbGF0aW9uSW5mby50YXJnZXRDb2xsZWN0aW9uIHx8IG51bGx9IC8+XG5cdFx0XHRcdCZuYnNwO1xuXHRcdFx0XHR7bGlua2VkU2hlZXRcblx0XHRcdFx0XHQ/IDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhbey4uLnJlbGF0aW9uSW5mbywgdGFyZ2V0VmFyaWFibGVOYW1lOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25DbGVhckZpZWxkTWFwcGluZygwKX1cblx0XHRcdFx0XHRcdFx0b3B0aW9ucz17bGlua2VkU2hlZXQudmFyaWFibGVzfSBwbGFjZWhvbGRlcj1cIlNlbGVjdCBhIHRhcmdldCBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdFx0XHR2YWx1ZT17cmVsYXRpb25JbmZvLnRhcmdldFZhcmlhYmxlTmFtZSB8fCBudWxsfSAvPlxuXHRcdFx0XHRcdDogbnVsbFxuXHRcdFx0XHR9XG5cblx0XHRcdDwvc3Bhbj5cblx0XHQpO1xuXHR9XG59XG5cbkZvcm0ucHJvcFR5cGVzID0ge1xuXHRjb2xsZWN0aW9uRGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0aW1wb3J0RGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2xlYXJGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNldEZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBvblNldERlZmF1bHRWYWx1ZSwgb25TZXRWYWx1ZU1hcHBpbmcsIG1hcHBpbmdzLCBuYW1lLCBhcmNoZXR5cGV9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblx0XHRjb25zdCBwcm9wZXJ0eU1hcHBpbmcgPSBtYXBwaW5nLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IG5hbWUpIHx8IHt9O1xuXHRcdGNvbnN0IHNlbGVjdGVkVmFyaWFibGUgPSBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGUgJiYgcHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlLmxlbmd0aCA/IHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXSA6IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRWYWx1ZSA9IHByb3BlcnR5TWFwcGluZy5kZWZhdWx0VmFsdWUgJiYgcHJvcGVydHlNYXBwaW5nLmRlZmF1bHRWYWx1ZS5sZW5ndGggPyBwcm9wZXJ0eU1hcHBpbmcuZGVmYXVsdFZhbHVlWzBdIDoge307XG5cdFx0Y29uc3QgdmFsdWVNYXBwaW5ncyA9IHByb3BlcnR5TWFwcGluZy52YWx1ZU1hcHBpbmdzIHx8IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRPcHRpb25zID0gKGFyY2hldHlwZVttYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5hcmNoZXR5cGVOYW1lXSB8fCBbXSkuZmluZCgoYSkgPT4gYS5uYW1lID09PSBuYW1lKS5vcHRpb25zIHx8IFtdO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxzcGFuPlxuXHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCBbe3ZhcmlhYmxlTmFtZTogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25DbGVhckZpZWxkTWFwcGluZyhjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCAwKX1cblx0XHRcdFx0XHRvcHRpb25zPXtjb2xsZWN0aW9uRGF0YS52YXJpYWJsZXN9IHBsYWNlaG9sZGVyPVwiU2VsZWN0IGEgY29sdW1uLi4uXCJcblx0XHRcdFx0XHR2YWx1ZT17c2VsZWN0ZWRWYXJpYWJsZS52YXJpYWJsZU5hbWV9IC8+XG5cdFx0XHRcdCZuYnNwO1xuXHRcdFx0XHR7c2VsZWN0ZWRWYXJpYWJsZS52YXJpYWJsZU5hbWUgPyAoPFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25TZXREZWZhdWx0VmFsdWUoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgW3t2YWx1ZTogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25TZXREZWZhdWx0VmFsdWUoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgW3t2YWx1ZTogbnVsbH1dKX1cblx0XHRcdFx0XHRvcHRpb25zPXtkZWZhdWx0T3B0aW9uc30gcGxhY2Vob2xkZXI9XCJTZWxlY3QgYSBkZWZhdWx0IHZhbHVlLi4uXCJcblx0XHRcdFx0XHR2YWx1ZT17ZGVmYXVsdFZhbHVlLnZhbHVlfSAvPikgOiBudWxsIH1cblxuXHRcdFx0XHR7c2VsZWN0ZWRWYXJpYWJsZS52YXJpYWJsZU5hbWUgPyAoXG5cdFx0XHRcdFx0PHVsIGNsYXNzTmFtZT1cImxpc3QtZ3JvdXBcIiBzdHlsZT17e21hcmdpblRvcDogXCIxMnB4XCIsIG1heEhlaWdodDogXCIyNzVweFwiLCBvdmVyZmxvd1k6IFwiYXV0b1wifX0+XG5cdFx0XHRcdFx0XHQ8bGkgY2xhc3NOYW1lPVwibGlzdC1ncm91cC1pdGVtXCI+PHN0cm9uZz5NYXAgaW1wb3J0IHZhbHVlcyB0byBzZWxlY3Qgb3B0aW9uczwvc3Ryb25nPjxwPiogTGVhdmUgYmxhbmsgdG8gbWF0Y2ggZXhhY3QgdmFsdWU8L3A+IDwvbGk+XG5cdFx0XHRcdFx0XHR7ZGVmYXVsdE9wdGlvbnMubWFwKChzZWxlY3RPcHRpb24sIGkpID0+IChcblx0XHRcdFx0XHRcdFx0PGxpIGNsYXNzTmFtZT1cImxpc3QtZ3JvdXAtaXRlbVwiIGtleT17aX0+XG5cdFx0XHRcdFx0XHRcdFx0PGxhYmVsPntzZWxlY3RPcHRpb259PC9sYWJlbD5cblx0XHRcdFx0XHRcdFx0XHQ8aW5wdXQgb25DaGFuZ2U9eyhldikgPT4gb25TZXRWYWx1ZU1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgc2VsZWN0T3B0aW9uLCBldi50YXJnZXQudmFsdWUpfVxuXHRcdFx0XHRcdFx0XHRcdFx0dHlwZT1cInRleHRcIiB2YWx1ZT17dmFsdWVNYXBwaW5nc1tzZWxlY3RPcHRpb25dIHx8IFwiXCJ9IC8+XG5cdFx0XHRcdFx0XHRcdDwvbGk+XG5cdFx0XHRcdFx0XHQpKX1cblx0XHRcdFx0XHQ8L3VsPikgOiBudWxsIH1cblx0XHRcdDwvc3Bhbj5cblxuXHRcdCk7XG5cdH1cbn1cblxuRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0Y29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNsZWFyRmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZXREZWZhdWx0VmFsdWU6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNldEZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uU2V0VmFsdWVNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBvblNldERlZmF1bHRWYWx1ZSwgbWFwcGluZ3MsIG5hbWV9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblx0XHRjb25zdCBwcm9wZXJ0eU1hcHBpbmcgPSBtYXBwaW5nLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IG5hbWUpIHx8IHt9O1xuXHRcdGNvbnN0IHNlbGVjdGVkVmFyaWFibGUgPSBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGUgJiYgcHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlLmxlbmd0aCA/IHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXSA6IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRWYWx1ZSA9IHByb3BlcnR5TWFwcGluZy5kZWZhdWx0VmFsdWUgJiYgcHJvcGVydHlNYXBwaW5nLmRlZmF1bHRWYWx1ZS5sZW5ndGggPyBwcm9wZXJ0eU1hcHBpbmcuZGVmYXVsdFZhbHVlWzBdIDoge307XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PHNwYW4+XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7dmFyaWFibGVOYW1lOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbkNsZWFyRmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIDApfVxuXHRcdFx0XHRcdG9wdGlvbnM9e2NvbGxlY3Rpb25EYXRhLnZhcmlhYmxlc30gcGxhY2Vob2xkZXI9XCJTZWxlY3QgYSBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtzZWxlY3RlZFZhcmlhYmxlLnZhcmlhYmxlTmFtZSB8fCBudWxsfSAvPlxuXHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0PGlucHV0IG9uQ2hhbmdlPXsoZXYpID0+IG9uU2V0RGVmYXVsdFZhbHVlKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7dmFsdWU6IGV2LnRhcmdldC52YWx1ZX1dKX1cblx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkRlZmF1bHQgdmFsdWUuLi5cIiB0eXBlPVwidGV4dFwiIHZhbHVlPXtkZWZhdWx0VmFsdWUudmFsdWUgfHwgbnVsbH0gLz5cblx0XHRcdDwvc3Bhbj5cblx0XHQpO1xuXHR9XG59XG5cbkZvcm0ucHJvcFR5cGVzID0ge1xuXHRjb2xsZWN0aW9uRGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2xlYXJGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNldERlZmF1bHRWYWx1ZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uU2V0RmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgRGF0YVJvdyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgcm93LCBjb25maXJtZWRDb2xzLCBpZ25vcmVkQ29sdW1ucywgdmFyaWFibGVzIH0gPSB0aGlzLnByb3BzO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDx0cj5cblx0XHRcdFx0e3Jvdy5tYXAoKGNlbGwsIGkpID0+IChcblx0XHRcdFx0XHQ8dGQgY2xhc3NOYW1lPXtjeCh7XG5cdFx0XHRcdFx0XHRpZ25vcmVkOiBjb25maXJtZWRDb2xzLmluZGV4T2YoaSkgPCAwICYmIGlnbm9yZWRDb2x1bW5zLmluZGV4T2YodmFyaWFibGVzW2ldKSA+IC0xXG5cdFx0XHRcdFx0fSl9IGtleT17aX0+XG5cdFx0XHRcdFx0XHR7Y2VsbH1cblx0XHRcdFx0XHQ8L3RkPlxuXHRcdFx0XHQpKX1cblx0XHRcdDwvdHI+XG5cdFx0KTtcblx0fVxufVxuXG5EYXRhUm93LnByb3BUeXBlcyA9IHtcblx0Y29uZmlybWVkQ29sczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LFxuXHRpZ25vcmVkQ29sdW1uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LFxuXHRyb3c6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcblx0dmFyaWFibGVzOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXlcbn07XG5cbmV4cG9ydCBkZWZhdWx0IERhdGFSb3c7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIEhlYWRlckNlbGwgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGhlYWRlciwgaXNDb25maXJtZWQsIGlzSWdub3JlZCwgYWN0aXZlQ29sbGVjdGlvbiwgb25JZ25vcmVDb2x1bW5Ub2dnbGUgfSA9IHRoaXMucHJvcHM7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PHRoIGNsYXNzTmFtZT17Y3goe1xuXHRcdFx0XHRzdWNjZXNzOiBpc0NvbmZpcm1lZCxcblx0XHRcdFx0aW5mbzogIWlzQ29uZmlybWVkICYmICFpc0lnbm9yZWQsXG5cdFx0XHRcdGlnbm9yZWQ6ICFpc0NvbmZpcm1lZCAmJiBpc0lnbm9yZWRcblx0XHRcdH0pfT5cblxuXHRcdFx0XHR7aGVhZGVyfVxuXHRcdFx0XHQ8YSBjbGFzc05hbWU9e2N4KFwicHVsbC1yaWdodFwiLCBcImdseXBoaWNvblwiLCB7XG5cdFx0XHRcdFx0XCJnbHlwaGljb24tb2stc2lnblwiOiBpc0NvbmZpcm1lZCxcblx0XHRcdFx0XHRcImdseXBoaWNvbi1xdWVzdGlvbi1zaWduXCI6ICFpc0NvbmZpcm1lZCAmJiAhaXNJZ25vcmVkLFxuXHRcdFx0XHRcdFwiZ2x5cGhpY29uLXJlbW92ZVwiOiAhaXNDb25maXJtZWQgJiYgaXNJZ25vcmVkXG5cdFx0XHRcdH0pfSBvbkNsaWNrPXsoKSA9PiAhaXNDb25maXJtZWQgPyBvbklnbm9yZUNvbHVtblRvZ2dsZShhY3RpdmVDb2xsZWN0aW9uLCBoZWFkZXIpIDogbnVsbCB9ID5cblx0XHRcdFx0PC9hPlxuXHRcdFx0PC90aD5cblx0XHQpO1xuXHR9XG59XG5cbkhlYWRlckNlbGwucHJvcFR5cGVzID0ge1xuXHRhY3RpdmVDb2xsZWN0aW9uOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRoZWFkZXI6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdGlzQ29uZmlybWVkOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbCxcblx0aXNJZ25vcmVkOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbCxcblx0b25JZ25vcmVDb2x1bW5Ub2dnbGU6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJDZWxsOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjbGFzc25hbWVzIGZyb20gJ2NsYXNzbmFtZXMnO1xuY2xhc3MgVXBsb2FkU3BsYXNoU2NyZWVuIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvblVwbG9hZEZpbGVTZWxlY3QsIHVzZXJkYXRhOiB7dXNlcklkfSwgb25Mb2dpbiwgaW1wb3J0RGF0YToge2lzVXBsb2FkaW5nfX0gPSB0aGlzLnByb3BzO1xuXG5cdFx0bGV0IHVwbG9hZEJ1dHRvbjtcblx0XHRpZiAodXNlcklkKSB7XG5cdFx0XHR1cGxvYWRCdXR0b24gPSAoXG5cdFx0XHRcdDxkaXY+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJsb2dpbi1zdWItY29tcG9uZW50IGxlYWRcIj5cblx0XHRcdFx0XHRcdDxsYWJlbCBjbGFzc05hbWU9e2NsYXNzbmFtZXMoXCJidG5cIiwgXCJidG4tbGdcIiwgXCJidG4tZGVmYXVsdFwiLCBcInVuZGVyTWFyZ2luXCIsIHtkaXNhYmxlZDogaXNVcGxvYWRpbmd9KX0+XG5cdFx0XHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tY2xvdWQtdXBsb2FkXCI+PC9zcGFuPlxuXHRcdFx0XHRcdFx0XHR7aXNVcGxvYWRpbmcgPyBcIlVwbG9hZGluZy4uLlwiIDogXCJCcm93c2VcIn1cblx0XHRcdFx0XHRcdFx0PGlucHV0XG5cdFx0XHRcdFx0XHRcdFx0ZGlzYWJsZWQ9e2lzVXBsb2FkaW5nfVxuXHRcdFx0XHRcdFx0XHRcdHR5cGU9XCJmaWxlXCJcblx0XHRcdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwibm9uZVwifX1cblx0XHRcdFx0XHRcdFx0XHRvbkNoYW5nZT17ZSA9PiBvblVwbG9hZEZpbGVTZWxlY3QoZS50YXJnZXQuZmlsZXMpfS8+XG5cdFx0XHRcdFx0XHQ8L2xhYmVsPlxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdDxwIGNsYXNzTmFtZT1cImxlYWRcIj5cblx0XHRcdFx0XHRcdERvbid0IGhhdmUgYSBkYXRhc2V0IGhhbmR5PyBIZXJl4oCZcyBhbiA8YSBocmVmPVwiL3N0YXRpYy9leGFtcGxlLnhsc3hcIj48ZW0+ZXhhbXBsZSBleGNlbCBzaGVldDwvZW0+PC9hPlxuXHRcdFx0XHRcdDwvcD5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQpO1xuXHRcdH0gZWxzZSB7XG5cdFx0XHR1cGxvYWRCdXR0b24gPSAoXG5cdFx0XHRcdDxkaXY+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJsZWFkXCI+XG5cdFx0XHRcdFx0XHQ8Zm9ybSBjbGFzc05hbWU9XCJsb2dpbi1zdWItY29tcG9uZW50XCIgYWN0aW9uPVwiaHR0cHM6Ly9zZWN1cmUuaHV5Z2Vucy5rbmF3Lm5sL3NhbWwyL2xvZ2luXCIgbWV0aG9kPVwiUE9TVFwiPlxuXHRcdFx0XHRcdFx0IFx0PGlucHV0IG5hbWU9XCJoc3VybFwiICB0eXBlPVwiaGlkZGVuXCIgdmFsdWU9e3dpbmRvdy5sb2NhdGlvbi5ocmVmfSAvPlxuXHRcdFx0XHRcdFx0IFx0PGJ1dHRvbiB0eXBlPVwic3VibWl0XCIgY2xhc3NOYW1lPVwiYnRuIGJ0bi1sZyBidG4tZGVmYXVsdCB1bmRlck1hcmdpblwiPlxuXHRcdFx0XHRcdFx0IFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWxvZy1pblwiPjwvc3Bhbj4gTG9nIGluXG5cdFx0XHRcdFx0XHQgXHQ8L2J1dHRvbj5cblx0XHRcdFx0XHRcdDwvZm9ybT5cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHQ8cCBjbGFzc05hbWU9XCJsZWFkXCI+XG5cdFx0XHRcdFx0XHRNb3N0IHVuaXZlcnNpdHkgYWNjb3VudHMgd2lsbCB3b3JrLiBZb3UgY2FuIGFsc28gbG9nIGluIHVzaW5nIGdvb2dsZSwgdHdpdHRlciBvciBmYWNlYm9vay5cblx0XHRcdFx0XHQ8L3A+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0KTtcblx0XHR9XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJzaXRlLXdyYXBwZXItaW5uZXIgIGZ1bGxzaXplX2JhY2tncm91bmRcIj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb3Zlci1jb250YWluZXIgd2hpdGVcIj5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImlubmVyIGNvdmVyXCI+XG5cdFx0XHRcdFx0XHQ8aDEgY2xhc3NOYW1lPVwiY292ZXItaGVhZGluZyB1bmRlck1hcmdpblwiPlxuXHRcdFx0XHRcdFx0XHQ8aW1nIGFsdD1cInRpbWJ1Y3Rvb1wiIGNsYXNzTmFtZT1cImxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nb190aW1idWN0b28uc3ZnXCIvPjxiciAvPlxuXHRcdFx0XHRcdFx0XHRUSU1CVUNUT09cblx0XHRcdFx0XHRcdDwvaDE+XG5cdFx0XHRcdFx0XHQ8cCBjbGFzc05hbWU9XCJsZWFkIHVuZGVyTWFyZ2luXCI+XG5cdFx0XHRcdFx0XHRcdEdldCB5b3VyIGRhdGEgc3RvcmVkIGFuZCBjb25uZWN0ZWQgdG8gdGhlIHdvcmxkLjxiciAvPlxuXHRcdFx0XHRcdFx0XHRTdGFydCB1cGxvYWRpbmcgeW91ciBkYXRhLlxuXHRcdFx0XHRcdFx0PC9wPlxuXHRcdFx0XHRcdFx0e3VwbG9hZEJ1dHRvbn1cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cblVwbG9hZFNwbGFzaFNjcmVlbi5wcm9wVHlwZXMgPSB7XG5cdG9uVXBsb2FkOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0dXNlcmRhdGE6IFJlYWN0LlByb3BUeXBlcy5zaGFwZSh7XG5cdFx0dXNlcklkOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG4gIH0pLFxuXHRpbXBvcnREYXRhOiBSZWFjdC5Qcm9wVHlwZXMuc2hhcGUoe1xuXHRcdGlzVXBsb2FkaW5nOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbGVhblxuXHR9KVxufTtcblxuZXhwb3J0IGRlZmF1bHQgVXBsb2FkU3BsYXNoU2NyZWVuO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vYWN0aW9uc1wiO1xuaW1wb3J0IEFwcCBmcm9tIFwiLi9jb21wb25lbnRzXCI7XG5pbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcbmltcG9ydCB4aHJtb2NrIGZyb20gXCJ4aHItbW9ja1wiO1xuaW1wb3J0IHNldHVwTW9ja3MgZnJvbSBcIi4vc2VydmVybW9ja3NcIjtcblxuaWYgKHByb2Nlc3MuZW52LlVTRV9NT0NLID09PSBcInRydWVcIikge1xuXHR2YXIgb3JpZyA9IHdpbmRvdy5YTUxIdHRwUmVxdWVzdDtcblx0eGhybW9jay5zZXR1cCgpOyAvL21vY2sgd2luZG93LlhNTEh0dHBSZXF1ZXN0IHVzYWdlc1xuXHR2YXIgbW9jayA9IHdpbmRvdy5YTUxIdHRwUmVxdWVzdDtcblx0d2luZG93LlhNTEh0dHBSZXF1ZXN0ID0gb3JpZztcblx0eGhyLlhNTEh0dHBSZXF1ZXN0ID0gbW9jaztcblx0eGhyLlhEb21haW5SZXF1ZXN0ID0gbW9jaztcblx0c2V0dXBNb2Nrcyh4aHJtb2NrLCBvcmlnKTtcbn1cblxuc3RvcmUuc3Vic2NyaWJlKCgpID0+IHtcblx0dmFyIHN0YXRlID0gc3RvcmUuZ2V0U3RhdGUoKTtcblx0UmVhY3RET00ucmVuZGVyKFxuXHRcdDxBcHBcblx0XHRcdHsuLi5zdGF0ZX1cblx0XHRcdHsuLi5hY3Rpb25zfSAvPixcblx0XHRkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKVxuXHQpXG59KTtcblxuZnVuY3Rpb24gY2hlY2tUb2tlbkluVXJsKHN0YXRlKSB7XG5cdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdGxldCBwYXJhbXMgPSBwYXRoLnNwbGl0KCcmJyk7XG5cblx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoJz0nKTtcblx0XHRpZihrZXkgPT09ICdoc2lkJyAmJiAhc3RhdGUudXNlcmRhdGEudXNlcklkKSB7XG5cdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJMT0dJTlwiLCBkYXRhOiB2YWx1ZX0pO1xuXHRcdFx0YnJlYWs7XG5cdFx0fVxuXHR9XG59XG5cbmRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJET01Db250ZW50TG9hZGVkXCIsICgpID0+IHtcblx0bGV0IHN0YXRlID0gc3RvcmUuZ2V0U3RhdGUoKTtcblx0UmVhY3RET00ucmVuZGVyKFxuXHRcdDxBcHBcblx0XHRcdHsuLi5zdGF0ZX1cblx0XHRcdHsuLi5hY3Rpb25zfSAvPixcblx0XHRkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKVxuXHQpXG5cdGNoZWNrVG9rZW5JblVybChzdGF0ZSk7XG5cblx0aWYgKCFzdGF0ZS5hcmNoZXR5cGUgfHwgT2JqZWN0LmtleXMoc3RhdGUuYXJjaGV0eXBlKS5sZW5ndGggPT09IDApIHtcblx0XHR4aHIocHJvY2Vzcy5lbnYuc2VydmVyICsgXCIvdjIuMS9tZXRhZGF0YS9BZG1pblwiLCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTRVRfQVJDSEVUWVBFX01FVEFEQVRBXCIsIGRhdGE6IEpTT04ucGFyc2UocmVzcC5ib2R5KX0pO1xuXHRcdH0pO1xuXHR9XG59KTtcbiIsImltcG9ydCB7IGdldEl0ZW0gfSBmcm9tIFwiLi4vdXRpbC9wZXJzaXN0XCI7XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IGdldEl0ZW0oXCJhcmNoZXR5cGVcIikgfHwge307XG5cblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfQVJDSEVUWVBFX01FVEFEQVRBXCI6XG5cdFx0XHRyZXR1cm4gYWN0aW9uLmRhdGE7XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwiaW1wb3J0IHsgZ2V0SXRlbSB9IGZyb20gXCIuLi91dGlsL3BlcnNpc3RcIjtcbmltcG9ydCBtZXJnZSBmcm9tIFwibWVyZ2Utb3B0aW9uc1wiO1xuXG5jb25zdCBpbml0aWFsU3RhdGUgPSBnZXRJdGVtKFwiaW1wb3J0RGF0YVwiKSB8fCB7XG5cdGlzVXBsb2FkaW5nOiBmYWxzZSxcblx0c2hlZXRzOiBudWxsLFxuXHRhY3RpdmVDb2xsZWN0aW9uOiBudWxsXG59O1xuXG5mdW5jdGlvbiBmaW5kSW5kZXgoYXJyLCBmKSB7XG5cdGxldCBsZW5ndGggPSBhcnIubGVuZ3RoO1xuXHRmb3IgKHZhciBpID0gMDsgaSA8IGxlbmd0aDsgaSsrKSB7XG4gICAgaWYgKGYoYXJyW2ldLCBpLCBhcnIpKSB7XG4gICAgICByZXR1cm4gaTtcbiAgICB9XG4gIH1cblx0cmV0dXJuIC0xO1xufVxuXG5mdW5jdGlvbiBzaGVldFJvd0Zyb21EaWN0VG9BcnJheShyb3dkaWN0LCBhcnJheU9mVmFyaWFibGVOYW1lcykge1xuXHRyZXR1cm4gYXJyYXlPZlZhcmlhYmxlTmFtZXMubWFwKG5hbWUgPT4gcm93ZGljdFtuYW1lXSk7XG59XG5cbmZ1bmN0aW9uIGFkZFJvd3MoY3VyUm93cywgbmV3Um93cywgYXJyYXlPZlZhcmlhYmxlTmFtZXMpIHtcblx0cmV0dXJuIGN1clJvd3MuY29uY2F0KFxuXHRcdG5ld1Jvd3MubWFwKGl0ZW0gPT4gc2hlZXRSb3dGcm9tRGljdFRvQXJyYXkoaXRlbSwgYXJyYXlPZlZhcmlhYmxlTmFtZXMpKVxuXHQpO1xufVxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNUQVJUX1VQTE9BRFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgaXNVcGxvYWRpbmc6IHRydWV9O1xuXHRcdGNhc2UgXCJGSU5JU0hfVVBMT0FEXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLFxuXHRcdFx0XHRzaGVldHM6IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zLm1hcChzaGVldCA9PiAoe1xuXHRcdFx0XHRcdGNvbGxlY3Rpb246IHNoZWV0Lm5hbWUsXG5cdFx0XHRcdFx0dmFyaWFibGVzOiBzaGVldC52YXJpYWJsZXMsXG5cdFx0XHRcdFx0cm93czogW10sXG5cdFx0XHRcdFx0bmV4dFVybDogc2hlZXQuZGF0YVxuXHRcdFx0XHR9KSksXG5cdFx0XHRcdGFjdGl2ZUNvbGxlY3Rpb246IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zWzBdLm5hbWUsXG5cdFx0XHRcdHZyZTogYWN0aW9uLmRhdGEudnJlLFxuXHRcdFx0XHRzYXZlTWFwcGluZ1VybDogYWN0aW9uLmRhdGEuc2F2ZU1hcHBpbmcsXG5cdFx0XHRcdGV4ZWN1dGVNYXBwaW5nVXJsOiBhY3Rpb24uZGF0YS5leGVjdXRlTWFwcGluZ1xuXHRcdFx0fTtcblx0XHRjYXNlIFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX1NVQ0NFRURFRFwiOlxuXHRcdFx0bGV0IHNoZWV0SWR4ID0gZmluZEluZGV4KHN0YXRlLnNoZWV0cywgc2hlZXQgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aW9uLmNvbGxlY3Rpb24pXG5cdFx0XHR2YXIgcmVzdWx0ID0ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0c2hlZXRzOiBbXG5cdFx0XHRcdFx0Li4uc3RhdGUuc2hlZXRzLnNsaWNlKDAsIHNoZWV0SWR4KSxcblx0XHRcdFx0XHR7XG5cdFx0XHRcdFx0XHQuLi5zdGF0ZS5zaGVldHNbc2hlZXRJZHhdLFxuXHRcdFx0XHRcdFx0cm93czogYWRkUm93cyhzdGF0ZS5zaGVldHNbc2hlZXRJZHhdLnJvd3MsIGFjdGlvbi5kYXRhLml0ZW1zLCBzdGF0ZS5zaGVldHNbc2hlZXRJZHhdLnZhcmlhYmxlcyksXG5cdFx0XHRcdFx0XHRuZXh0VXJsOiBhY3Rpb24uZGF0YS5uZXh0XG5cdFx0XHRcdFx0fSxcblx0XHRcdFx0XHQuLi5zdGF0ZS5zaGVldHMuc2xpY2Uoc2hlZXRJZHggKyAxKVxuXHRcdFx0XHRdXG5cdFx0XHR9O1xuXG5cdFx0XHRyZXR1cm4gcmVzdWx0O1xuXHRcdGNhc2UgXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRklOSVNIRURcIjpcblx0XHRcdHZhciByZXN1bHQgPSB7Li4uc3RhdGV9O1xuXHRcdFx0cmVzdWx0LnNoZWV0cyA9IHJlc3VsdC5zaGVldHMuc2xpY2UoKTtcblx0XHRcdHJlc3VsdC5zaGVldHNcblx0XHRcdFx0LmZvckVhY2goKHNoZWV0LCBpKSA9PiB7XG5cdFx0XHRcdFx0aWYgKHNoZWV0LmNvbGxlY3Rpb24gPT09IGFjdGlvbi5jb2xsZWN0aW9uKSB7XG5cdFx0XHRcdFx0XHRyZXN1bHQuc2hlZXRzW2ldID0ge1xuXHRcdFx0XHRcdFx0XHQuLi5zaGVldCxcblx0XHRcdFx0XHRcdFx0aXNMb2FkaW5nOiBmYWxzZVxuXHRcdFx0XHRcdFx0fVxuXHRcdFx0XHRcdH1cblx0XHRcdFx0fSk7XG5cblx0XHRcdHJldHVybiByZXN1bHQ7XG5cdFx0Y2FzZSBcIlNFVF9BQ1RJVkVfQ09MTEVDVElPTlwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgYWN0aXZlQ29sbGVjdGlvbjogYWN0aW9uLmNvbGxlY3Rpb259O1xuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufVxuIiwiaW1wb3J0IGltcG9ydERhdGEgZnJvbSBcIi4vaW1wb3J0LWRhdGFcIjtcbmltcG9ydCBhcmNoZXR5cGUgZnJvbSBcIi4vYXJjaGV0eXBlXCI7XG5pbXBvcnQgbWFwcGluZ3MgZnJvbSBcIi4vbWFwcGluZ3NcIjtcbmltcG9ydCB1c2VyZGF0YSBmcm9tIFwiLi91c2VyZGF0YVwiO1xuXG5leHBvcnQgZGVmYXVsdCB7XG5cdGltcG9ydERhdGE6IGltcG9ydERhdGEsXG5cdGFyY2hldHlwZTogYXJjaGV0eXBlLFxuXHRtYXBwaW5nczogbWFwcGluZ3MsXG5cdHVzZXJkYXRhOiB1c2VyZGF0YVxufTtcbiIsImltcG9ydCBzZXRJbiBmcm9tIFwiLi4vdXRpbC9zZXQtaW5cIjtcbmltcG9ydCBnZXRJbiBmcm9tIFwiLi4vdXRpbC9nZXQtaW5cIjtcbmltcG9ydCB7IGdldEl0ZW0gfSBmcm9tIFwiLi4vdXRpbC9wZXJzaXN0XCI7XG5cbmNvbnN0IG5ld1ZhcmlhYmxlRGVzYyA9IChwcm9wZXJ0eSwgdmFyaWFibGVTcGVjKSA9PiAoe1xuXHRwcm9wZXJ0eTogcHJvcGVydHksXG5cdHZhcmlhYmxlOiB2YXJpYWJsZVNwZWMsXG5cdGRlZmF1bHRWYWx1ZTogW10sXG5cdGNvbmZpcm1lZDogZmFsc2UsXG5cdHZhbHVlTWFwcGluZ3M6IHt9XG59KTtcblxuZnVuY3Rpb24gc2NhZmZvbGRDb2xsZWN0aW9uTWFwcGluZ3MoaW5pdCwgc2hlZXQpe1xuXHRyZXR1cm4gT2JqZWN0LmFzc2lnbihpbml0LCB7XG5cdFx0W3NoZWV0Lm5hbWVdOiB7XG5cdFx0XHRhcmNoZXR5cGVOYW1lOiBudWxsLFxuXHRcdFx0bWFwcGluZ3M6IFtdLFxuXHRcdFx0aWdub3JlZENvbHVtbnM6IFtdLFxuXHRcdFx0Y3VzdG9tUHJvcGVydGllczogW11cblx0XHR9XG5cdH0pO1xufVxuXG5jb25zdCBpbml0aWFsU3RhdGUgPSBnZXRJdGVtKFwibWFwcGluZ3NcIikgfHwge1xuXHRjb2xsZWN0aW9uczoge30sXG5cdGNvbmZpcm1lZDogZmFsc2Vcbn07XG5cbmNvbnN0IGdldE1hcHBpbmdJbmRleCA9IChzdGF0ZSwgYWN0aW9uKSA9PlxuXHRzdGF0ZS5jb2xsZWN0aW9uc1thY3Rpb24uY29sbGVjdGlvbl0ubWFwcGluZ3Ncblx0XHQubWFwKChtLCBpKSA9PiAoe2luZGV4OiBpLCBtOiBtfSkpXG5cdFx0LmZpbHRlcigobVNwZWMpID0+IG1TcGVjLm0ucHJvcGVydHkgPT09IGFjdGlvbi5wcm9wZXJ0eUZpZWxkKVxuXHRcdC5yZWR1Y2UoKHByZXYsIGN1cikgPT4gY3VyLmluZGV4LCAtMSk7XG5cbmNvbnN0IG1hcENvbGxlY3Rpb25BcmNoZXR5cGUgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRsZXQgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiYXJjaGV0eXBlTmFtZVwiXSwgYWN0aW9uLnZhbHVlLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBbXSwgbmV3Q29sbGVjdGlvbnMpO1xuXG5cdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCB1cHNlcnRGaWVsZE1hcHBpbmcgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblx0Y29uc3QgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHggPCAwID8gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucykubGVuZ3RoIDogZm91bmRJZHhdLFxuXHRcdG5ld1ZhcmlhYmxlRGVzYyhhY3Rpb24ucHJvcGVydHlGaWVsZCwgYWN0aW9uLmltcG9ydGVkRmllbGQpLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cblxuXHRyZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuY29uc3QgY2xlYXJGaWVsZE1hcHBpbmcgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblx0aWYgKGZvdW5kSWR4IDwgMCkgeyByZXR1cm4gc3RhdGU7IH1cblxuXHRjb25zdCBjdXJyZW50ID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCIsIGZvdW5kSWR4LCBcInZhcmlhYmxlXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucylcblx0XHQuZmlsdGVyKChtLCBpKSA9PiBpICE9PSBhY3Rpb24uY2xlYXJJbmRleCk7XG5cblx0bGV0IG5ld0NvbGxlY3Rpb25zO1xuXHRpZiAoY3VycmVudC5sZW5ndGggPiAwKSB7XG5cdFx0bmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHgsIFwidmFyaWFibGVcIl0sIGN1cnJlbnQsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblx0fSBlbHNlIHtcblx0XHRjb25zdCBuZXdNYXBwaW5ncyA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpXG5cdFx0XHQuZmlsdGVyKChtLCBpKSA9PiBpICE9PSBmb3VuZElkeCk7XG5cdFx0bmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIG5ld01hcHBpbmdzLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdH1cblxuXG5cdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCBzZXREZWZhdWx0VmFsdWUgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblx0aWYgKGZvdW5kSWR4ID4gLTEpIHtcblx0XHRjb25zdCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiLCBmb3VuZElkeCwgXCJkZWZhdWx0VmFsdWVcIl0sIGFjdGlvbi52YWx1ZSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXHRcdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59O1xuXG5jb25zdCBzZXRGaWVsZENvbmZpcm1hdGlvbiA9IChzdGF0ZSwgYWN0aW9uLCB2YWx1ZSkgPT4ge1xuXHRjb25zdCBjdXJyZW50ID0gKGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpIHx8IFtdKVxuXHRcdC5tYXAoKHZtKSA9PiAoey4uLnZtLCBjb25maXJtZWQ6IGFjdGlvbi5wcm9wZXJ0eUZpZWxkID09PSB2bS5wcm9wZXJ0eSA/IHZhbHVlIDogdm0uY29uZmlybWVkfSkpO1xuXHRsZXQgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIGN1cnJlbnQsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuXHRpZiAodmFsdWUgPT09IHRydWUpIHtcblx0XHRjb25zdCBjb25maXJtZWRWYXJpYWJsZU5hbWVzID0gY3VycmVudC5tYXAoKG0pID0+IG0udmFyaWFibGUubWFwKCh2KSA9PiB2LnZhcmlhYmxlTmFtZSkpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYikpO1xuXHRcdGNvbnN0IG5ld0lnbm9yZWRDb2x1bXMgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKVxuXHRcdFx0LmZpbHRlcigoaWMpID0+IGNvbmZpcm1lZFZhcmlhYmxlTmFtZXMuaW5kZXhPZihpYykgPCAwKTtcblx0XHRuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJpZ25vcmVkQ29sdW1uc1wiXSwgbmV3SWdub3JlZENvbHVtcywgbmV3Q29sbGVjdGlvbnMpO1xuXHR9XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmNvbnN0IHNldFZhbHVlTWFwcGluZyA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGZvdW5kSWR4ID0gZ2V0TWFwcGluZ0luZGV4KHN0YXRlLCBhY3Rpb24pO1xuXG5cdGlmIChmb3VuZElkeCA+IC0xKSB7XG5cdFx0Y29uc3QgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHgsIFwidmFsdWVNYXBwaW5nc1wiLCBhY3Rpb24udGltVmFsdWVdLFxuXHRcdFx0YWN0aW9uLm1hcFZhbHVlLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdFx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcblx0fVxuXHRyZXR1cm4gc3RhdGU7XG59O1xuXG5jb25zdCB0b2dnbGVJZ25vcmVkQ29sdW1uID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcblx0bGV0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuXHRpZiAoY3VycmVudC5pbmRleE9mKGFjdGlvbi52YXJpYWJsZU5hbWUpIDwgMCkge1xuXHRcdGN1cnJlbnQucHVzaChhY3Rpb24udmFyaWFibGVOYW1lKTtcblx0fSBlbHNlIHtcblx0XHRjdXJyZW50ID0gY3VycmVudC5maWx0ZXIoKGMpID0+IGMgIT09IGFjdGlvbi52YXJpYWJsZU5hbWUpO1xuXHR9XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJpZ25vcmVkQ29sdW1uc1wiXSwgY3VycmVudCwgc3RhdGUuY29sbGVjdGlvbnMpIH07XG59O1xuXG5jb25zdCBhZGRDdXN0b21Qcm9wZXJ0eSA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiY3VzdG9tUHJvcGVydGllc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXHRjb25zdCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJjdXN0b21Qcm9wZXJ0aWVzXCIsIGN1cnJlbnQubGVuZ3RoXSwge25hbWU6IGFjdGlvbi5wcm9wZXJ0eUZpZWxkLCB0eXBlOiBhY3Rpb24ucHJvcGVydHlUeXBlfSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXG5cdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCByZW1vdmVDdXN0b21Qcm9wZXJ0eSA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGZvdW5kSWR4ID0gZ2V0TWFwcGluZ0luZGV4KHN0YXRlLCBhY3Rpb24pO1xuXG5cdGNvbnN0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiY3VzdG9tUHJvcGVydGllc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpXG5cdFx0LmZpbHRlcigoY3ApID0+IGNwLm5hbWUgIT09IGFjdGlvbi5wcm9wZXJ0eUZpZWxkKTtcblxuXHRsZXQgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiY3VzdG9tUHJvcGVydGllc1wiXSwgY3VycmVudCwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXG5cdGlmIChmb3VuZElkeCA+IC0xKSB7XG5cdFx0Y29uc3QgbmV3TWFwcGluZ3MgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKVxuXHRcdFx0LmZpbHRlcigobSwgaSkgPT4gaSAhPT0gZm91bmRJZHgpO1xuXHRcdG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBuZXdNYXBwaW5ncywgbmV3Q29sbGVjdGlvbnMpO1xuXHR9XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiRklOSVNIX1VQTE9BRFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zLnJlZHVjZShzY2FmZm9sZENvbGxlY3Rpb25NYXBwaW5ncywge30pfTtcblxuXHRcdGNhc2UgXCJNQVBfQ09MTEVDVElPTl9BUkNIRVRZUEVcIjpcblx0XHRcdHJldHVybiBtYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIkNPTkZJUk1fQ09MTEVDVElPTl9BUkNIRVRZUEVfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIGNvbmZpcm1lZDogdHJ1ZX07XG5cblx0XHRjYXNlIFwiU0VUX0ZJRUxEX01BUFBJTkdcIjpcblx0XHRcdHJldHVybiB1cHNlcnRGaWVsZE1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiQ0xFQVJfRklFTERfTUFQUElOR1wiOlxuXHRcdFx0cmV0dXJuIGNsZWFyRmllbGRNYXBwaW5nKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIlNFVF9ERUZBVUxUX1ZBTFVFXCI6XG5cdFx0XHRyZXR1cm4gc2V0RGVmYXVsdFZhbHVlKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIkNPTkZJUk1fRklFTERfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiBzZXRGaWVsZENvbmZpcm1hdGlvbihzdGF0ZSwgYWN0aW9uLCB0cnVlKTtcblxuXHRcdGNhc2UgXCJVTkNPTkZJUk1fRklFTERfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiBzZXRGaWVsZENvbmZpcm1hdGlvbihzdGF0ZSwgYWN0aW9uLCBmYWxzZSk7XG5cblx0XHRjYXNlIFwiU0VUX1ZBTFVFX01BUFBJTkdcIjpcblx0XHRcdHJldHVybiBzZXRWYWx1ZU1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiVE9HR0xFX0lHTk9SRURfQ09MVU1OXCI6XG5cdFx0XHRyZXR1cm4gdG9nZ2xlSWdub3JlZENvbHVtbihzdGF0ZSwgYWN0aW9uKTtcblxuXHRcdGNhc2UgXCJBRERfQ1VTVE9NX1BST1BFUlRZXCI6XG5cdFx0XHRyZXR1cm4gYWRkQ3VzdG9tUHJvcGVydHkoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiUkVNT1ZFX0NVU1RPTV9QUk9QRVJUWVwiOlxuXHRcdFx0cmV0dXJuIHJlbW92ZUN1c3RvbVByb3BlcnR5KHN0YXRlLCBhY3Rpb24pO1xuXHR9XG5cdHJldHVybiBzdGF0ZTtcbn1cbiIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgdXNlcklkOiB1bmRlZmluZWRcbn07XG5cblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJMT0dJTlwiOlxuXHRcdFx0cmV0dXJuIHtcbiAgICAgICAgdXNlcklkOiBhY3Rpb24uZGF0YVxuICAgICAgfVxuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufVxuIiwiZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24gc2V0dXBNb2Nrcyh4aHJtb2NrLCBvcmlnKSB7XG4gIHhocm1vY2tcbiAgICAuZ2V0KFwiaHR0cDovL3Rlc3QucmVwb3NpdG9yeS5odXlnZW5zLmtuYXcubmwvdjIuMS9tZXRhZGF0YS9BZG1pblwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoYHtcbiAgICAgICAgICBcInBlcnNvbnNcIjogW1xuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJuYW1lXCIsXG4gICAgICAgICAgICAgIFwidHlwZVwiOiBcInRleHRcIlxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwiaGFzV3JpdHRlblwiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJyZWxhdGlvblwiLFxuICAgICAgICAgICAgICBcInF1aWNrc2VhcmNoXCI6IFwiL3YyLjEvZG9tYWluL2RvY3VtZW50cy9hdXRvY29tcGxldGVcIixcbiAgICAgICAgICAgICAgXCJyZWxhdGlvblwiOiB7XG4gICAgICAgICAgICAgICAgXCJkaXJlY3Rpb25cIjogXCJPVVRcIixcbiAgICAgICAgICAgICAgICBcIm91dE5hbWVcIjogXCJoYXNXcml0dGVuXCIsXG4gICAgICAgICAgICAgICAgXCJpbk5hbWVcIjogXCJ3YXNXcml0dGVuQnlcIixcbiAgICAgICAgICAgICAgICBcInRhcmdldENvbGxlY3Rpb25cIjogXCJkb2N1bWVudHNcIixcbiAgICAgICAgICAgICAgICBcInJlbGF0aW9uQ29sbGVjdGlvblwiOiBcInJlbGF0aW9uc1wiLFxuICAgICAgICAgICAgICAgIFwicmVsYXRpb25UeXBlSWRcIjogXCJiYmExMGQzNy04NmNjLTRmMWYtYmEyZC0wMTZhZjJiMjFhYTRcIlxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJpc1JlbGF0ZWRUb1wiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJyZWxhdGlvblwiLFxuICAgICAgICAgICAgICBcInF1aWNrc2VhcmNoXCI6IFwiL3YyLjEvZG9tYWluL3BlcnNvbnMvYXV0b2NvbXBsZXRlXCIsXG4gICAgICAgICAgICAgIFwicmVsYXRpb25cIjoge1xuICAgICAgICAgICAgICAgIFwiZGlyZWN0aW9uXCI6IFwiT1VUXCIsXG4gICAgICAgICAgICAgICAgXCJvdXROYW1lXCI6IFwiaXNSZWxhdGVkVG9cIixcbiAgICAgICAgICAgICAgICBcImluTmFtZVwiOiBcImlzUmVsYXRlZFRvXCIsXG4gICAgICAgICAgICAgICAgXCJ0YXJnZXRDb2xsZWN0aW9uXCI6IFwicGVyc29uc1wiLFxuICAgICAgICAgICAgICAgIFwicmVsYXRpb25Db2xsZWN0aW9uXCI6IFwicmVsYXRpb25zXCIsXG4gICAgICAgICAgICAgICAgXCJyZWxhdGlvblR5cGVJZFwiOiBcImNiYTEwZDM3LTg2Y2MtNGYxZi1iYTJkLTAxNmFmMmIyMWFhNVwiXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgICBdLFxuICAgICAgICAgIFwiZG9jdW1lbnRzXCI6IFtcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwibmFtZVwiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJ0ZXh0XCJcbiAgICAgICAgICAgIH1cbiAgICAgICAgICBdXG4gICAgICAgIH1gKVxuICAgIH0pXG4gICAgLnBvc3QoXCJodHRwOi8vdGVzdC5yZXBvc2l0b3J5Lmh1eWdlbnMua25hdy5ubC92Mi4xL2J1bGstdXBsb2FkXCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiYnVsay11cGxvYWRcIilcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuaGVhZGVyKFwiTG9jYXRpb25cIiwgXCI8PFRoZSBnZXQgcmF3IGRhdGEgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCIpO1xuICAgIH0pXG4gICAgLnBvc3QoXCI8PFRoZSBzYXZlIG1hcHBpbmcgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwic2F2ZSBtYXBwaW5nXCIsIHJlcS5ib2R5KCkpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDQpO1xuICAgIH0pXG4gICAgLnBvc3QoXCI8PFRoZSBleGVjdXRlIG1hcHBpbmcgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZXhlY3V0ZSBtYXBwaW5nXCIsIHJlcS5ib2R5KCkpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDQpO1xuICAgIH0pXG4gICAgLmdldChcIjw8VGhlIGdldCByYXcgZGF0YSB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgcmF3IGRhdGFcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgIHZyZTogXCJ0aGV2cmVuYW1lXCIsXG4gICAgICAgICAgc2F2ZU1hcHBpbmc6IFwiPDxUaGUgc2F2ZSBtYXBwaW5nIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiLFxuICAgICAgICAgIGV4ZWN1dGVNYXBwaW5nOiBcIjw8VGhlIGV4ZWN1dGUgbWFwcGluZyB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIixcbiAgICAgICAgICBjb2xsZWN0aW9uczogW1xuICAgICAgICBcdFx0e1xuICAgICAgICBcdFx0XHRuYW1lOiBcIm1vY2twZXJzb25zXCIsXG4gICAgICAgIFx0XHRcdHZhcmlhYmxlczogW1wiSURcIiwgXCJWb29ybmFhbVwiLCBcInR1c3NlbnZvZWdzZWxcIiwgXCJBY2h0ZXJuYWFtXCIsIFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsIFwiR2Vub2VtZCBpblwiLCBcIklzIGdldHJvdXdkIG1ldFwiXSxcbiAgICAgICAgICAgICAgZGF0YTogXCI8PHVybCBmb3IgcGVyc29uIGRhdGE+PlwiXG4gICAgICAgIFx0XHR9LFxuICAgICAgICBcdFx0e1xuICAgICAgICBcdFx0XHRuYW1lOiBcIm1vY2tkb2N1bWVudHNcIixcbiAgICAgICAgXHRcdFx0dmFyaWFibGVzOiBbXCJ0aXRlbFwiLCBcImRhdHVtXCIsIFwicmVmZXJlbnRpZVwiLCBcInVybFwiXSxcbiAgICAgICAgICAgICAgZGF0YTogXCI8PHVybCBmb3IgZG9jdW1lbnQgZGF0YT4+XCJcbiAgICAgICAgXHRcdH1cbiAgICAgICAgXHRdXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5nZXQoXCI8PHVybCBmb3IgcGVyc29uIGRhdGE+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImdldCBwZXJzb24gaXRlbXMgZGF0YVwiKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShKU09OLnN0cmluZ2lmeSh7XG4gICAgICAgICAgXCJfbmV4dFwiOiBcIjw8bW9yZSBkYXRhPj5cIixcbiAgICAgICAgXHRcIml0ZW1zXCI6IFt7XG4gICAgICAgIFx0XHRcInRpbV9pZFwiOiBcIjFcIixcbiAgICAgICAgICAgIFwiSURcIjogXCIxXCIsXG4gICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwiVm9vcm5hYW1cIixcbiAgICAgICAgICAgIFwidHVzc2Vudm9lZ3NlbFwiOiBcInR1c3NlbnZvZWdzZWxcIixcbiAgICAgICAgICAgIFwiQWNodGVybmFhbVwiOiBcIkFjaHRlcm5hYW1cIixcbiAgICAgICAgICAgIFwiR2VzY2hyZXZlbkRvY3VtZW50XCI6IFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsXG4gICAgICAgICAgICBcIkdlbm9lbWQgaW5cIjogXCJHZW5vZW1kIGluXCIsXG4gICAgICAgICAgICBcIklzIGdldHJvdXdkIG1ldFwiOiBcIklzIGdldHJvdXdkIG1ldFwiLFxuICAgICAgICBcdH0sIHtcbiAgICAgICAgICAgIFwidGltX2lkXCI6IFwiMlwiLFxuICAgICAgICAgICAgXCJJRFwiOiBcIjJcIixcbiAgICAgICAgICAgIFwiVm9vcm5hYW1cIjogXCJWb29ybmFhbVwiLFxuICAgICAgICAgICAgXCJ0dXNzZW52b2Vnc2VsXCI6IFwidHVzc2Vudm9lZ3NlbFwiLFxuICAgICAgICAgICAgXCJBY2h0ZXJuYWFtXCI6IFwiQWNodGVybmFhbVwiLFxuICAgICAgICAgICAgXCJHZXNjaHJldmVuRG9jdW1lbnRcIjogXCJHZXNjaHJldmVuRG9jdW1lbnRcIixcbiAgICAgICAgICAgIFwiR2Vub2VtZCBpblwiOiBcIkdlbm9lbWQgaW5cIixcbiAgICAgICAgICAgIFwiSXMgZ2V0cm91d2QgbWV0XCI6IFwiSXMgZ2V0cm91d2QgbWV0XCIsXG4gICAgICAgIFx0fV1cbiAgICAgICAgfSkpO1xuICAgIH0pXG4gICAgLmdldChcIjw8bW9yZSBkYXRhPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgcGVyc29uIGl0ZW1zIGRhdGFcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICBcdFwiaXRlbXNcIjogW3tcbiAgICAgICAgICAgIFwiSURcIjogXCIzXCIsXG4gICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwiVm9vcm5hYW1cIixcbiAgICAgICAgICAgIFwidHVzc2Vudm9lZ3NlbFwiOiBcInR1c3NlbnZvZWdzZWxcIixcbiAgICAgICAgICAgIFwiQWNodGVybmFhbVwiOiBcIkFjaHRlcm5hYW1cIixcbiAgICAgICAgICAgIFwiR2VzY2hyZXZlbkRvY3VtZW50XCI6IFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsXG4gICAgICAgICAgICBcIkdlbm9lbWQgaW5cIjogXCJHZW5vZW1kIGluXCIsXG4gICAgICAgICAgICBcIklzIGdldHJvdXdkIG1ldFwiOiBcIklzIGdldHJvdXdkIG1ldFwiLFxuICAgICAgICBcdH0sIHtcbiAgICAgICAgICAgIFwiSURcIjogXCI0XCIsXG4gICAgICAgICAgICBcIlZvb3JuYWFtXCI6IFwiVm9vcm5hYW1cIixcbiAgICAgICAgICAgIFwidHVzc2Vudm9lZ3NlbFwiOiBcInR1c3NlbnZvZWdzZWxcIixcbiAgICAgICAgICAgIFwiQWNodGVybmFhbVwiOiBcIkFjaHRlcm5hYW1cIixcbiAgICAgICAgICAgIFwiR2VzY2hyZXZlbkRvY3VtZW50XCI6IFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsXG4gICAgICAgICAgICBcIkdlbm9lbWQgaW5cIjogXCJHZW5vZW1kIGluXCIsXG4gICAgICAgICAgICBcIklzIGdldHJvdXdkIG1ldFwiOiBcIklzIGdldHJvdXdkIG1ldFwiLFxuICAgICAgICBcdH1dXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5nZXQoXCI8PHVybCBmb3IgZG9jdW1lbnQgZGF0YT4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZ2V0IHBlcnNvbiBpdGVtcyBkYXRhXCIpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDApXG4gICAgICAgIC5ib2R5KEpTT04uc3RyaW5naWZ5KHtcbiAgICAgICAgXHRcIm5hbWVcIjogXCJzb21lQ29sbGVjdGlvblwiLFxuICAgICAgICBcdFwidmFyaWFibGVzXCI6IFtcInRpbV9pZFwiLCBcInZhcjFcIiwgXCJ2YXIyXCJdLFxuICAgICAgICBcdFwiaXRlbXNcIjogW3tcbiAgICAgICAgXHRcdFwidGltX2lkXCI6IFwiMVwiLFxuICAgICAgICAgICAgXCJ0aXRlbFwiOiBcInRpdGVsXCIsXG4gICAgICAgICAgICBcImRhdHVtXCI6IFwiZGF0dW1cIixcbiAgICAgICAgICAgIFwicmVmZXJlbnRpZVwiOiBcInJlZmVyZW50aWVcIixcbiAgICAgICAgICAgIFwidXJsXCI6IFwidXJsXCIsXG4gICAgICAgIFx0fSwge1xuICAgICAgICAgICAgXCJ0aW1faWRcIjogXCIyXCIsXG4gICAgICAgICAgICBcInRpdGVsXCI6IFwidGl0ZWxcIixcbiAgICAgICAgICAgIFwiZGF0dW1cIjogXCJkYXR1bVwiLFxuICAgICAgICAgICAgXCJyZWZlcmVudGllXCI6IFwicmVmZXJlbnRpZVwiLFxuICAgICAgICAgICAgXCJ1cmxcIjogXCJ1cmxcIixcbiAgICAgICAgXHR9XVxuICAgICAgICB9KSk7XG4gICAgfSlcbiAgICAubW9jayhmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmVycm9yKFwidW5tb2NrZWQgcmVxdWVzdFwiLCByZXEudXJsKCksIHJlcSwgcmVzcCk7XG4gICAgfSlcbn1cbiIsImltcG9ydCB7Y3JlYXRlU3RvcmUsIGFwcGx5TWlkZGxld2FyZSwgY29tYmluZVJlZHVjZXJzLCBjb21wb3NlfSBmcm9tIFwicmVkdXhcIjtcbmltcG9ydCB0aHVua01pZGRsZXdhcmUgZnJvbSBcInJlZHV4LXRodW5rXCI7XG5cbmltcG9ydCB7IHBlcnNpc3QgfSBmcm9tIFwiLi91dGlsL3BlcnNpc3RcIjtcbmltcG9ydCByZWR1Y2VycyBmcm9tIFwiLi9yZWR1Y2Vyc1wiO1xuXG5sZXQgc3RvcmUgPSBjcmVhdGVTdG9yZShcbiAgY29tYmluZVJlZHVjZXJzKHJlZHVjZXJzKSxcbiAgY29tcG9zZShcbiAgICBhcHBseU1pZGRsZXdhcmUoXG4gICAgICB0aHVua01pZGRsZXdhcmVcbiAgICApLFxuICAgIHdpbmRvdy5kZXZUb29sc0V4dGVuc2lvbiA/IHdpbmRvdy5kZXZUb29sc0V4dGVuc2lvbigpIDogZiA9PiBmXG4gIClcbik7XG5cbi8vIHdpbmRvdy5vbmJlZm9yZXVubG9hZCA9ICgpID0+IHBlcnNpc3Qoc3RvcmUuZ2V0U3RhdGUoKSk7XG5cbmV4cG9ydCBkZWZhdWx0IHN0b3JlO1xuIiwiZnVuY3Rpb24gZGVlcENsb25lOShvYmopIHtcbiAgICB2YXIgaSwgbGVuLCByZXQ7XG5cbiAgICBpZiAodHlwZW9mIG9iaiAhPT0gXCJvYmplY3RcIiB8fCBvYmogPT09IG51bGwpIHtcbiAgICAgICAgcmV0dXJuIG9iajtcbiAgICB9XG5cbiAgICBpZiAoQXJyYXkuaXNBcnJheShvYmopKSB7XG4gICAgICAgIHJldCA9IFtdO1xuICAgICAgICBsZW4gPSBvYmoubGVuZ3RoO1xuICAgICAgICBmb3IgKGkgPSAwOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgICAgIHJldC5wdXNoKCAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldICk7XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICByZXQgPSB7fTtcbiAgICAgICAgZm9yIChpIGluIG9iaikge1xuICAgICAgICAgICAgaWYgKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkge1xuICAgICAgICAgICAgICAgIHJldFtpXSA9ICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV07XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHJldDtcbn1cblxuZXhwb3J0IGRlZmF1bHQgZGVlcENsb25lOTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4vY2xvbmUtZGVlcFwiO1xuXG5jb25zdCBfZ2V0SW4gPSAocGF0aCwgZGF0YSkgPT5cblx0ZGF0YSA/XG5cdFx0cGF0aC5sZW5ndGggPT09IDAgPyBkYXRhIDogX2dldEluKHBhdGgsIGRhdGFbcGF0aC5zaGlmdCgpXSkgOlxuXHRcdG51bGw7XG5cblxuXG5jb25zdCBnZXRJbiA9IChwYXRoLCBkYXRhKSA9PlxuXHRfZ2V0SW4oY2xvbmUocGF0aCksIGRhdGEpO1xuXG5cbmV4cG9ydCBkZWZhdWx0IGdldEluOyIsImV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uIG1hcHBpbmdUb0pzb25MZFJtbChtYXBwaW5nLCB2cmUpIHtcbiAgcmV0dXJuIHtcbiAgXHRcIkBjb250ZXh0XCI6IHtcbiAgXHRcdFwiQHZvY2FiXCI6IFwiaHR0cDovL3d3dy53My5vcmcvbnMvcjJybWwjXCIsXG4gIFx0XHRcInJtbFwiOiBcImh0dHA6Ly9zZW13ZWIubW1sYWIuYmUvbnMvcm1sI1wiLFxuICBcdFx0XCJ0aW1cIjogXCJodHRwOi8vdGltYnVjdG9vLmNvbS9tYXBwaW5nXCIsXG4gIFx0XHRcInByZWRpY2F0ZVwiOiB7XG4gIFx0XHRcdFwiQHR5cGVcIjogXCJAaWRcIlxuICBcdFx0fVxuICBcdH0sXG4gIFx0XCJAZ3JhcGhcIjogT2JqZWN0LmtleXMobWFwcGluZy5jb2xsZWN0aW9ucykubWFwKGtleSA9PiBtYXBTaGVldChrZXksIG1hcHBpbmcuY29sbGVjdGlvbnNba2V5XSwgdnJlKSlcbiAgfTtcbn1cbnZhciBzID0ge1xuICBcImFyY2hldHlwZU5hbWVcIjogXCJwZXJzb25zXCIsXG4gIFwibWFwcGluZ3NcIjogW1xuICAgIHtcbiAgICAgIFwicHJvcGVydHlcIjogXCJuYW1lXCIsXG4gICAgICBcInZhcmlhYmxlXCI6IFtcbiAgICAgICAge1xuICAgICAgICAgIFwidmFyaWFibGVOYW1lXCI6IFwiVm9vcm5hYW1cIlxuICAgICAgICB9XG4gICAgICBdLFxuICAgICAgXCJkZWZhdWx0VmFsdWVcIjogW10sXG4gICAgICBcImNvbmZpcm1lZFwiOiB0cnVlLFxuICAgICAgXCJ2YWx1ZU1hcHBpbmdzXCI6IHt9XG4gICAgfSxcbiAgICB7XG4gICAgICBcInByb3BlcnR5XCI6IFwiRm9vXCIsXG4gICAgICBcInZhcmlhYmxlXCI6IFtcbiAgICAgICAge1xuICAgICAgICAgIFwidmFyaWFibGVOYW1lXCI6IFwiQWNodGVybmFhbVwiXG4gICAgICAgIH1cbiAgICAgIF0sXG4gICAgICBcImRlZmF1bHRWYWx1ZVwiOiBbXSxcbiAgICAgIFwiY29uZmlybWVkXCI6IHRydWUsXG4gICAgICBcInZhbHVlTWFwcGluZ3NcIjoge31cbiAgICB9LFxuICAgIHtcbiAgICAgIFwicHJvcGVydHlcIjogXCJpc1JlbGF0ZWRUb1wiLFxuICAgICAgXCJ2YXJpYWJsZVwiOiBbXG4gICAgICAgIHtcbiAgICAgICAgICBcInZhcmlhYmxlTmFtZVwiOiBcIlZvb3JuYWFtXCIsXG4gICAgICAgICAgXCJ0YXJnZXRDb2xsZWN0aW9uXCI6IFwibW9ja3BlcnNvbnNcIixcbiAgICAgICAgICBcInRhcmdldFZhcmlhYmxlTmFtZVwiOiBcIlZvb3JuYWFtXCJcbiAgICAgICAgfVxuICAgICAgXSxcbiAgICAgIFwiZGVmYXVsdFZhbHVlXCI6IFtdLFxuICAgICAgXCJjb25maXJtZWRcIjogdHJ1ZSxcbiAgICAgIFwidmFsdWVNYXBwaW5nc1wiOiB7fVxuICAgIH1cbiAgXVxufVxuXG5mdW5jdGlvbiBtYWtlTWFwTmFtZShsb2NhbE5hbWUpIHtcbiAgcmV0dXJuIGB0aW06cy8ke2xvY2FsTmFtZX1tYXBgO1xufVxuXG5mdW5jdGlvbiBtYXBTaGVldChrZXksIHNoZWV0LCB2cmUpIHtcbiAgLy8gY29uc29sZS5sb2coSlNPTi5zdHJpbmdpZnkoc2hlZXQsIHVuZGVmaW5lZCwgMikpO1xuICAvL0ZJWE1FOiBtb3ZlIGxvZ2ljYWxTb3VyY2UgYW5kIHN1YmplY3RNYXAgdW5kZXIgdGhlIGNvbnRyb2wgb2YgdGhlIHNlcnZlclxuICByZXR1cm4ge1xuICAgIFwiQGlkXCI6IG1ha2VNYXBOYW1lKGtleSksXG4gICAgXCJybWw6bG9naWNhbFNvdXJjZVwiOiB7XG5cdFx0XHRcInJtbDpzb3VyY2VcIjoge1xuXHRcdFx0XHRcInRpbTpyYXdDb2xsZWN0aW9uXCI6IGtleSxcblx0XHRcdFx0XCJ0aW06dnJlTmFtZVwiOiB2cmVcblx0XHRcdH1cblx0XHR9LFxuICAgIFwic3ViamVjdE1hcFwiOiB7XG5cdFx0XHRcImNsYXNzXCI6IGBodHRwOi8vdGltYnVjdG9vLmNvbS8ke3ZyZX0vJHtrZXl9YCxcblx0XHRcdFwidGVtcGxhdGVcIjogYGh0dHA6Ly90aW1idWN0b28uY29tLyR7dnJlfS8ke2tleX0ve3RpbV9pZH1gXG5cdFx0fSxcbiAgICBcInByZWRpY2F0ZU9iamVjdE1hcFwiOiBzaGVldC5tYXBwaW5ncy5tYXAobWFrZVByZWRpY2F0ZU9iamVjdE1hcClcbiAgfTtcbn1cblxuZnVuY3Rpb24gbWFrZVByZWRpY2F0ZU9iamVjdE1hcChtYXBwaW5nKSB7XG4gIGxldCBwcm9wZXJ0eSA9IG1hcHBpbmcucHJvcGVydHk7XG4gIGxldCB2YXJpYWJsZSA9IG1hcHBpbmcudmFyaWFibGVbMF07XG4gIGlmICh2YXJpYWJsZS50YXJnZXRDb2xsZWN0aW9uKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIFwib2JqZWN0TWFwXCI6IHtcbiAgICAgICAgXCJyZWZlcmVuY2VcIjoge1xuICAgICAgICAgIFwiam9pbkNvbmRpdGlvblwiOiB7XG4gICAgICAgICAgICBcImNoaWxkXCI6IHZhcmlhYmxlLnZhcmlhYmxlTmFtZSxcbiAgICAgICAgICAgIFwicGFyZW50XCI6IHZhcmlhYmxlLnRhcmdldFZhcmlhYmxlTmFtZVxuICAgICAgICAgIH0sXG4gICAgICAgICAgXCJwYXJlbnRUcmlwbGVzTWFwXCI6IG1ha2VNYXBOYW1lKHZhcmlhYmxlLnRhcmdldENvbGxlY3Rpb24pXG4gICAgICAgIH1cbiAgICAgIH0sXG4gICAgICBcInByZWRpY2F0ZVwiOiBgaHR0cDovL3RpbWJ1Y3Rvby5jb20vJHtwcm9wZXJ0eX1gXG4gICAgfVxuICB9IGVsc2Uge1xuICAgIHJldHVybiB7XG4gICAgICBcIm9iamVjdE1hcFwiOiB7XG4gICAgICAgIFwiY29sdW1uXCI6IHZhcmlhYmxlLnZhcmlhYmxlTmFtZVxuICAgICAgfSxcbiAgICAgIFwicHJlZGljYXRlXCI6IGBodHRwOi8vdGltYnVjdG9vLmNvbS8ke3Byb3BlcnR5fWBcbiAgICB9XG4gIH1cbn1cbiIsImxldCBwZXJzaXN0RGlzYWJsZWQgPSBmYWxzZTtcblxuY29uc3QgcGVyc2lzdCA9IChzdGF0ZSkgPT4ge1xuXHRpZiAoIHBlcnNpc3REaXNhYmxlZCApIHsgcmV0dXJuOyB9XG5cdGZvciAobGV0IGtleSBpbiBzdGF0ZSkge1xuXHRcdGxvY2FsU3RvcmFnZS5zZXRJdGVtKGtleSwgSlNPTi5zdHJpbmdpZnkoc3RhdGVba2V5XSkpO1xuXHR9XG59O1xuXG5jb25zdCBnZXRJdGVtID0gKGtleSkgPT4ge1xuXHRpZiAobG9jYWxTdG9yYWdlLmdldEl0ZW0oa2V5KSkge1xuXHRcdHJldHVybiBKU09OLnBhcnNlKGxvY2FsU3RvcmFnZS5nZXRJdGVtKGtleSkpO1xuXHR9XG5cdHJldHVybiBudWxsO1xufTtcblxuY29uc3QgZGlzYWJsZVBlcnNpc3QgPSAoKSA9PiB7XG5cdGxvY2FsU3RvcmFnZS5jbGVhcigpO1xuXHRwZXJzaXN0RGlzYWJsZWQgPSB0cnVlO1xufTtcbndpbmRvdy5kaXNhYmxlUGVyc2lzdCA9IGRpc2FibGVQZXJzaXN0O1xuXG5leHBvcnQgeyBwZXJzaXN0LCBnZXRJdGVtLCBkaXNhYmxlUGVyc2lzdCB9O1xuIiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuLy8gRG8gZWl0aGVyIG9mIHRoZXNlOlxuLy8gIGEpIFNldCBhIHZhbHVlIGJ5IHJlZmVyZW5jZSBpZiBkZXJlZiBpcyBub3QgbnVsbFxuLy8gIGIpIFNldCBhIHZhbHVlIGRpcmVjdGx5IGluIHRvIGRhdGEgb2JqZWN0IGlmIGRlcmVmIGlzIG51bGxcbmNvbnN0IHNldEVpdGhlciA9IChkYXRhLCBkZXJlZiwga2V5LCB2YWwpID0+IHtcblx0KGRlcmVmIHx8IGRhdGEpW2tleV0gPSB2YWw7XG5cdHJldHVybiBkYXRhO1xufTtcblxuLy8gU2V0IGEgbmVzdGVkIHZhbHVlIGluIGRhdGEgKG5vdCB1bmxpa2UgaW1tdXRhYmxlanMsIGJ1dCBhIGNsb25lIG9mIGRhdGEgaXMgZXhwZWN0ZWQgZm9yIHByb3BlciBpbW11dGFiaWxpdHkpXG5jb25zdCBfc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID0gbnVsbCkgPT5cblx0cGF0aC5sZW5ndGggPiAxID9cblx0XHRfc2V0SW4ocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID8gZGVyZWZbcGF0aC5zaGlmdCgpXSA6IGRhdGFbcGF0aC5zaGlmdCgpXSkgOlxuXHRcdHNldEVpdGhlcihkYXRhLCBkZXJlZiwgcGF0aFswXSwgdmFsdWUpO1xuXG5jb25zdCBzZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSkgPT5cblx0X3NldEluKGNsb25lKHBhdGgpLCB2YWx1ZSwgY2xvbmUoZGF0YSkpO1xuXG5leHBvcnQgZGVmYXVsdCBzZXRJbjsiXX0=
