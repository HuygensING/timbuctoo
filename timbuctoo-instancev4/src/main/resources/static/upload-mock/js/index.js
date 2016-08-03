(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.ExcelImportMock = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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

},{}],2:[function(require,module,exports){
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

},{"is-function":4}],3:[function(require,module,exports){
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

},{}],4:[function(require,module,exports){
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

},{}],5:[function(require,module,exports){
'use strict';
var toString = Object.prototype.toString;

module.exports = function (x) {
	var prototype;
	return toString.call(x) === '[object Object]' && (prototype = Object.getPrototypeOf(x), prototype === null || prototype === Object.getPrototypeOf({}));
};

},{}],6:[function(require,module,exports){
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

},{"./_overArg":8}],7:[function(require,module,exports){
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

},{}],8:[function(require,module,exports){
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

},{}],9:[function(require,module,exports){
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

},{}],10:[function(require,module,exports){
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

},{"./_getPrototype":6,"./_isHostObject":7,"./isObjectLike":9}],11:[function(require,module,exports){
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

},{"is-plain-obj":5}],12:[function(require,module,exports){
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
},{"for-each":2,"trim":23}],13:[function(require,module,exports){
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
},{}],14:[function(require,module,exports){
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
},{"./compose":17}],15:[function(require,module,exports){
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
},{}],16:[function(require,module,exports){
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
},{"./createStore":18,"./utils/warning":20,"lodash/isPlainObject":10}],17:[function(require,module,exports){
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
},{}],18:[function(require,module,exports){
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
},{"lodash/isPlainObject":10,"symbol-observable":21}],19:[function(require,module,exports){
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
},{"./applyMiddleware":14,"./bindActionCreators":15,"./combineReducers":16,"./compose":17,"./createStore":18,"./utils/warning":20}],20:[function(require,module,exports){
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
},{}],21:[function(require,module,exports){
(function (global){
/* global window */
'use strict';

module.exports = require('./ponyfill')(global || window || this);

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"./ponyfill":22}],22:[function(require,module,exports){
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

},{}],23:[function(require,module,exports){

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

},{}],24:[function(require,module,exports){
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

},{"./lib/MockXMLHttpRequest":27,"global":3}],25:[function(require,module,exports){

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

},{}],26:[function(require,module,exports){

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

},{}],27:[function(require,module,exports){
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

},{"./MockRequest":25,"./MockResponse":26}],28:[function(require,module,exports){
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

},{"global/window":3,"is-function":4,"parse-headers":12,"xtend":29}],29:[function(require,module,exports){
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

},{}],30:[function(require,module,exports){
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
if ("production" === "development") {
	toJson = function toJson(data) {
		return JSON.stringify(data, undefined, 2);
	};
} else {
	toJson = function toJson(data) {
		return JSON.stringify(data);
	};
}

var actions = {
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
				body: toJson((0, _mappingToJsonLdRml2.default)(state.mappings, state.importData.vre)),
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

},{"../store":55,"../util/mappingToJsonLdRml":58,"xhr":28}],31:[function(require,module,exports){
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

},{"./fields/select-field":36,"react":"react"}],32:[function(require,module,exports){
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

},{"./property-form":39,"./property-form/add-property":38,"react":"react"}],33:[function(require,module,exports){
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

},{"classnames":1,"react":"react"}],34:[function(require,module,exports){
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

},{"./table/data-row":45,"./table/header-cell":46,"react":"react"}],35:[function(require,module,exports){
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

},{"./collection-form":32,"./collection-index":33,"./collection-table":34,"react":"react"}],36:[function(require,module,exports){
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

},{"classnames":1,"react":"react","react-dom":"react-dom"}],37:[function(require,module,exports){
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

},{"./archetype-mappings":31,"./datasheet-mappings":35,"./upload-splash-screen":47,"react":"react"}],38:[function(require,module,exports){
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

},{"../fields/select-field":36,"react":"react"}],39:[function(require,module,exports){
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

},{"./links":40,"./names":41,"./relation":42,"./select":43,"./text":44,"react":"react"}],40:[function(require,module,exports){
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

},{"../fields/select-field":36,"react":"react"}],41:[function(require,module,exports){
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

},{"../fields/select-field":36,"react":"react"}],42:[function(require,module,exports){
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

},{"../fields/select-field":36,"react":"react"}],43:[function(require,module,exports){
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

},{"../fields/select-field":36,"react":"react"}],44:[function(require,module,exports){
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

},{"../fields/select-field":36,"react":"react"}],45:[function(require,module,exports){
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

},{"classnames":1,"react":"react"}],46:[function(require,module,exports){
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

},{"classnames":1,"react":"react"}],47:[function(require,module,exports){
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

},{"classnames":1,"react":"react"}],48:[function(require,module,exports){
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

},{"./actions":30,"./components":37,"./servermocks":54,"./store":55,"react":"react","react-dom":"react-dom","xhr":28,"xhr-mock":24}],49:[function(require,module,exports){
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

},{"../util/persist":59}],50:[function(require,module,exports){
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

},{"../util/persist":59,"merge-options":11}],51:[function(require,module,exports){
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

},{"./archetype":49,"./import-data":50,"./mappings":52,"./userdata":53}],52:[function(require,module,exports){
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

},{"../util/get-in":57,"../util/persist":59,"../util/set-in":60}],53:[function(require,module,exports){
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

},{}],54:[function(require,module,exports){
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
      "name": "someCollection",
      "variables": ["tim_id", "var1", "var2"],
      "items": [{
        "tim_id": "1",
        "ID": "ID",
        "Voornaam": "Voornaam",
        "tussenvoegsel": "tussenvoegsel",
        "Achternaam": "Achternaam",
        "GeschrevenDocument": "GeschrevenDocument",
        "Genoemd in": "Genoemd in",
        "Is getrouwd met": "Is getrouwd met"
      }, {
        "tim_id": "2",
        "ID": "ID",
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

},{}],55:[function(require,module,exports){
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

},{"./reducers":51,"./util/persist":59,"redux":19,"redux-thunk":13}],56:[function(require,module,exports){
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

},{}],57:[function(require,module,exports){
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

},{"./clone-deep":56}],58:[function(require,module,exports){
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

},{}],59:[function(require,module,exports){
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

},{}],60:[function(require,module,exports){
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

},{"./clone-deep":56}]},{},[48])(48)
});
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvY2xhc3NuYW1lcy9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9mb3ItZWFjaC9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9nbG9iYWwvd2luZG93LmpzIiwibm9kZV9tb2R1bGVzL2lzLWZ1bmN0aW9uL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL2lzLXBsYWluLW9iai9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX2dldFByb3RvdHlwZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX2lzSG9zdE9iamVjdC5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvX292ZXJBcmcuanMiLCJub2RlX21vZHVsZXMvbG9kYXNoL2lzT2JqZWN0TGlrZS5qcyIsIm5vZGVfbW9kdWxlcy9sb2Rhc2gvaXNQbGFpbk9iamVjdC5qcyIsIm5vZGVfbW9kdWxlcy9tZXJnZS1vcHRpb25zL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3BhcnNlLWhlYWRlcnMvcGFyc2UtaGVhZGVycy5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC10aHVuay9saWIvaW5kZXguanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2FwcGx5TWlkZGxld2FyZS5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvYmluZEFjdGlvbkNyZWF0b3JzLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9jb21iaW5lUmVkdWNlcnMuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NvbXBvc2UuanMiLCJub2RlX21vZHVsZXMvcmVkdXgvbGliL2NyZWF0ZVN0b3JlLmpzIiwibm9kZV9tb2R1bGVzL3JlZHV4L2xpYi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9yZWR1eC9saWIvdXRpbHMvd2FybmluZy5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9zeW1ib2wtb2JzZXJ2YWJsZS9wb255ZmlsbC5qcyIsIm5vZGVfbW9kdWxlcy90cmltL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3hoci1tb2NrL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3hoci1tb2NrL2xpYi9Nb2NrUmVxdWVzdC5qcyIsIm5vZGVfbW9kdWxlcy94aHItbW9jay9saWIvTW9ja1Jlc3BvbnNlLmpzIiwibm9kZV9tb2R1bGVzL3hoci1tb2NrL2xpYi9Nb2NrWE1MSHR0cFJlcXVlc3QuanMiLCJub2RlX21vZHVsZXMveGhyL2luZGV4LmpzIiwibm9kZV9tb2R1bGVzL3h0ZW5kL2ltbXV0YWJsZS5qcyIsInNyYy9hY3Rpb25zL2luZGV4LmpzIiwic3JjL2NvbXBvbmVudHMvYXJjaGV0eXBlLW1hcHBpbmdzLmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1mb3JtLmpzIiwic3JjL2NvbXBvbmVudHMvY29sbGVjdGlvbi1pbmRleC5qcyIsInNyYy9jb21wb25lbnRzL2NvbGxlY3Rpb24tdGFibGUuanMiLCJzcmMvY29tcG9uZW50cy9kYXRhc2hlZXQtbWFwcGluZ3MuanMiLCJzcmMvY29tcG9uZW50cy9maWVsZHMvc2VsZWN0LWZpZWxkLmpzIiwic3JjL2NvbXBvbmVudHMvaW5kZXguanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL2FkZC1wcm9wZXJ0eS5qcyIsInNyYy9jb21wb25lbnRzL3Byb3BlcnR5LWZvcm0vaW5kZXguanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvcHJvcGVydHktZm9ybS9uYW1lcy5qcyIsInNyYy9jb21wb25lbnRzL3Byb3BlcnR5LWZvcm0vcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9wcm9wZXJ0eS1mb3JtL3NlbGVjdC5qcyIsInNyYy9jb21wb25lbnRzL3Byb3BlcnR5LWZvcm0vdGV4dC5qcyIsInNyYy9jb21wb25lbnRzL3RhYmxlL2RhdGEtcm93LmpzIiwic3JjL2NvbXBvbmVudHMvdGFibGUvaGVhZGVyLWNlbGwuanMiLCJzcmMvY29tcG9uZW50cy91cGxvYWQtc3BsYXNoLXNjcmVlbi5qcyIsInNyYy9pbmRleC5qcyIsInNyYy9yZWR1Y2Vycy9hcmNoZXR5cGUuanMiLCJzcmMvcmVkdWNlcnMvaW1wb3J0LWRhdGEuanMiLCJzcmMvcmVkdWNlcnMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvbWFwcGluZ3MuanMiLCJzcmMvcmVkdWNlcnMvdXNlcmRhdGEuanMiLCJzcmMvc2VydmVybW9ja3MuanMiLCJzcmMvc3RvcmUuanMiLCJzcmMvdXRpbC9jbG9uZS1kZWVwLmpzIiwic3JjL3V0aWwvZ2V0LWluLmpzIiwic3JjL3V0aWwvbWFwcGluZ1RvSnNvbkxkUm1sLmpzIiwic3JjL3V0aWwvcGVyc2lzdC5qcyIsInNyYy91dGlsL3NldC1pbi5qcyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTtBQ0FBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2hEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUM5Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7Ozs7QUNUQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ1BBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDZkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3RFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN6SkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDYkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDekRBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNsREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUhBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3ZDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNyUUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDN0NBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUN4QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ0pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbkJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNkQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1R0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdFQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3hGQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQy9OQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzNPQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7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QUFkO0FBQUEsUUFDYixPQUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIi8qIVxuICBDb3B5cmlnaHQgKGMpIDIwMTYgSmVkIFdhdHNvbi5cbiAgTGljZW5zZWQgdW5kZXIgdGhlIE1JVCBMaWNlbnNlIChNSVQpLCBzZWVcbiAgaHR0cDovL2plZHdhdHNvbi5naXRodWIuaW8vY2xhc3NuYW1lc1xuKi9cbi8qIGdsb2JhbCBkZWZpbmUgKi9cblxuKGZ1bmN0aW9uICgpIHtcblx0J3VzZSBzdHJpY3QnO1xuXG5cdHZhciBoYXNPd24gPSB7fS5oYXNPd25Qcm9wZXJ0eTtcblxuXHRmdW5jdGlvbiBjbGFzc05hbWVzICgpIHtcblx0XHR2YXIgY2xhc3NlcyA9IFtdO1xuXG5cdFx0Zm9yICh2YXIgaSA9IDA7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcblx0XHRcdHZhciBhcmcgPSBhcmd1bWVudHNbaV07XG5cdFx0XHRpZiAoIWFyZykgY29udGludWU7XG5cblx0XHRcdHZhciBhcmdUeXBlID0gdHlwZW9mIGFyZztcblxuXHRcdFx0aWYgKGFyZ1R5cGUgPT09ICdzdHJpbmcnIHx8IGFyZ1R5cGUgPT09ICdudW1iZXInKSB7XG5cdFx0XHRcdGNsYXNzZXMucHVzaChhcmcpO1xuXHRcdFx0fSBlbHNlIGlmIChBcnJheS5pc0FycmF5KGFyZykpIHtcblx0XHRcdFx0Y2xhc3Nlcy5wdXNoKGNsYXNzTmFtZXMuYXBwbHkobnVsbCwgYXJnKSk7XG5cdFx0XHR9IGVsc2UgaWYgKGFyZ1R5cGUgPT09ICdvYmplY3QnKSB7XG5cdFx0XHRcdGZvciAodmFyIGtleSBpbiBhcmcpIHtcblx0XHRcdFx0XHRpZiAoaGFzT3duLmNhbGwoYXJnLCBrZXkpICYmIGFyZ1trZXldKSB7XG5cdFx0XHRcdFx0XHRjbGFzc2VzLnB1c2goa2V5KTtcblx0XHRcdFx0XHR9XG5cdFx0XHRcdH1cblx0XHRcdH1cblx0XHR9XG5cblx0XHRyZXR1cm4gY2xhc3Nlcy5qb2luKCcgJyk7XG5cdH1cblxuXHRpZiAodHlwZW9mIG1vZHVsZSAhPT0gJ3VuZGVmaW5lZCcgJiYgbW9kdWxlLmV4cG9ydHMpIHtcblx0XHRtb2R1bGUuZXhwb3J0cyA9IGNsYXNzTmFtZXM7XG5cdH0gZWxzZSBpZiAodHlwZW9mIGRlZmluZSA9PT0gJ2Z1bmN0aW9uJyAmJiB0eXBlb2YgZGVmaW5lLmFtZCA9PT0gJ29iamVjdCcgJiYgZGVmaW5lLmFtZCkge1xuXHRcdC8vIHJlZ2lzdGVyIGFzICdjbGFzc25hbWVzJywgY29uc2lzdGVudCB3aXRoIG5wbSBwYWNrYWdlIG5hbWVcblx0XHRkZWZpbmUoJ2NsYXNzbmFtZXMnLCBbXSwgZnVuY3Rpb24gKCkge1xuXHRcdFx0cmV0dXJuIGNsYXNzTmFtZXM7XG5cdFx0fSk7XG5cdH0gZWxzZSB7XG5cdFx0d2luZG93LmNsYXNzTmFtZXMgPSBjbGFzc05hbWVzO1xuXHR9XG59KCkpO1xuIiwidmFyIGlzRnVuY3Rpb24gPSByZXF1aXJlKCdpcy1mdW5jdGlvbicpXG5cbm1vZHVsZS5leHBvcnRzID0gZm9yRWFjaFxuXG52YXIgdG9TdHJpbmcgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nXG52YXIgaGFzT3duUHJvcGVydHkgPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5XG5cbmZ1bmN0aW9uIGZvckVhY2gobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBpZiAoIWlzRnVuY3Rpb24oaXRlcmF0b3IpKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ2l0ZXJhdG9yIG11c3QgYmUgYSBmdW5jdGlvbicpXG4gICAgfVxuXG4gICAgaWYgKGFyZ3VtZW50cy5sZW5ndGggPCAzKSB7XG4gICAgICAgIGNvbnRleHQgPSB0aGlzXG4gICAgfVxuICAgIFxuICAgIGlmICh0b1N0cmluZy5jYWxsKGxpc3QpID09PSAnW29iamVjdCBBcnJheV0nKVxuICAgICAgICBmb3JFYWNoQXJyYXkobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpXG4gICAgZWxzZSBpZiAodHlwZW9mIGxpc3QgPT09ICdzdHJpbmcnKVxuICAgICAgICBmb3JFYWNoU3RyaW5nKGxpc3QsIGl0ZXJhdG9yLCBjb250ZXh0KVxuICAgIGVsc2VcbiAgICAgICAgZm9yRWFjaE9iamVjdChsaXN0LCBpdGVyYXRvciwgY29udGV4dClcbn1cblxuZnVuY3Rpb24gZm9yRWFjaEFycmF5KGFycmF5LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBhcnJheS5sZW5ndGg7IGkgPCBsZW47IGkrKykge1xuICAgICAgICBpZiAoaGFzT3duUHJvcGVydHkuY2FsbChhcnJheSwgaSkpIHtcbiAgICAgICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgYXJyYXlbaV0sIGksIGFycmF5KVxuICAgICAgICB9XG4gICAgfVxufVxuXG5mdW5jdGlvbiBmb3JFYWNoU3RyaW5nKHN0cmluZywgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gc3RyaW5nLmxlbmd0aDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgIC8vIG5vIHN1Y2ggdGhpbmcgYXMgYSBzcGFyc2Ugc3RyaW5nLlxuICAgICAgICBpdGVyYXRvci5jYWxsKGNvbnRleHQsIHN0cmluZy5jaGFyQXQoaSksIGksIHN0cmluZylcbiAgICB9XG59XG5cbmZ1bmN0aW9uIGZvckVhY2hPYmplY3Qob2JqZWN0LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGsgaW4gb2JqZWN0KSB7XG4gICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKG9iamVjdCwgaykpIHtcbiAgICAgICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgb2JqZWN0W2tdLCBrLCBvYmplY3QpXG4gICAgICAgIH1cbiAgICB9XG59XG4iLCJpZiAodHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgIG1vZHVsZS5leHBvcnRzID0gd2luZG93O1xufSBlbHNlIGlmICh0eXBlb2YgZ2xvYmFsICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSBnbG9iYWw7XG59IGVsc2UgaWYgKHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiKXtcbiAgICBtb2R1bGUuZXhwb3J0cyA9IHNlbGY7XG59IGVsc2Uge1xuICAgIG1vZHVsZS5leHBvcnRzID0ge307XG59XG4iLCJtb2R1bGUuZXhwb3J0cyA9IGlzRnVuY3Rpb25cblxudmFyIHRvU3RyaW5nID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZ1xuXG5mdW5jdGlvbiBpc0Z1bmN0aW9uIChmbikge1xuICB2YXIgc3RyaW5nID0gdG9TdHJpbmcuY2FsbChmbilcbiAgcmV0dXJuIHN0cmluZyA9PT0gJ1tvYmplY3QgRnVuY3Rpb25dJyB8fFxuICAgICh0eXBlb2YgZm4gPT09ICdmdW5jdGlvbicgJiYgc3RyaW5nICE9PSAnW29iamVjdCBSZWdFeHBdJykgfHxcbiAgICAodHlwZW9mIHdpbmRvdyAhPT0gJ3VuZGVmaW5lZCcgJiZcbiAgICAgLy8gSUU4IGFuZCBiZWxvd1xuICAgICAoZm4gPT09IHdpbmRvdy5zZXRUaW1lb3V0IHx8XG4gICAgICBmbiA9PT0gd2luZG93LmFsZXJ0IHx8XG4gICAgICBmbiA9PT0gd2luZG93LmNvbmZpcm0gfHxcbiAgICAgIGZuID09PSB3aW5kb3cucHJvbXB0KSlcbn07XG4iLCIndXNlIHN0cmljdCc7XG52YXIgdG9TdHJpbmcgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uICh4KSB7XG5cdHZhciBwcm90b3R5cGU7XG5cdHJldHVybiB0b1N0cmluZy5jYWxsKHgpID09PSAnW29iamVjdCBPYmplY3RdJyAmJiAocHJvdG90eXBlID0gT2JqZWN0LmdldFByb3RvdHlwZU9mKHgpLCBwcm90b3R5cGUgPT09IG51bGwgfHwgcHJvdG90eXBlID09PSBPYmplY3QuZ2V0UHJvdG90eXBlT2Yoe30pKTtcbn07XG4iLCJ2YXIgb3ZlckFyZyA9IHJlcXVpcmUoJy4vX292ZXJBcmcnKTtcblxuLyogQnVpbHQtaW4gbWV0aG9kIHJlZmVyZW5jZXMgZm9yIHRob3NlIHdpdGggdGhlIHNhbWUgbmFtZSBhcyBvdGhlciBgbG9kYXNoYCBtZXRob2RzLiAqL1xudmFyIG5hdGl2ZUdldFByb3RvdHlwZSA9IE9iamVjdC5nZXRQcm90b3R5cGVPZjtcblxuLyoqXG4gKiBHZXRzIHRoZSBgW1tQcm90b3R5cGVdXWAgb2YgYHZhbHVlYC5cbiAqXG4gKiBAcHJpdmF0ZVxuICogQHBhcmFtIHsqfSB2YWx1ZSBUaGUgdmFsdWUgdG8gcXVlcnkuXG4gKiBAcmV0dXJucyB7bnVsbHxPYmplY3R9IFJldHVybnMgdGhlIGBbW1Byb3RvdHlwZV1dYC5cbiAqL1xudmFyIGdldFByb3RvdHlwZSA9IG92ZXJBcmcobmF0aXZlR2V0UHJvdG90eXBlLCBPYmplY3QpO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGdldFByb3RvdHlwZTtcbiIsIi8qKlxuICogQ2hlY2tzIGlmIGB2YWx1ZWAgaXMgYSBob3N0IG9iamVjdCBpbiBJRSA8IDkuXG4gKlxuICogQHByaXZhdGVcbiAqIEBwYXJhbSB7Kn0gdmFsdWUgVGhlIHZhbHVlIHRvIGNoZWNrLlxuICogQHJldHVybnMge2Jvb2xlYW59IFJldHVybnMgYHRydWVgIGlmIGB2YWx1ZWAgaXMgYSBob3N0IG9iamVjdCwgZWxzZSBgZmFsc2VgLlxuICovXG5mdW5jdGlvbiBpc0hvc3RPYmplY3QodmFsdWUpIHtcbiAgLy8gTWFueSBob3N0IG9iamVjdHMgYXJlIGBPYmplY3RgIG9iamVjdHMgdGhhdCBjYW4gY29lcmNlIHRvIHN0cmluZ3NcbiAgLy8gZGVzcGl0ZSBoYXZpbmcgaW1wcm9wZXJseSBkZWZpbmVkIGB0b1N0cmluZ2AgbWV0aG9kcy5cbiAgdmFyIHJlc3VsdCA9IGZhbHNlO1xuICBpZiAodmFsdWUgIT0gbnVsbCAmJiB0eXBlb2YgdmFsdWUudG9TdHJpbmcgIT0gJ2Z1bmN0aW9uJykge1xuICAgIHRyeSB7XG4gICAgICByZXN1bHQgPSAhISh2YWx1ZSArICcnKTtcbiAgICB9IGNhdGNoIChlKSB7fVxuICB9XG4gIHJldHVybiByZXN1bHQ7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gaXNIb3N0T2JqZWN0O1xuIiwiLyoqXG4gKiBDcmVhdGVzIGEgZnVuY3Rpb24gdGhhdCBpbnZva2VzIGBmdW5jYCB3aXRoIGl0cyBmaXJzdCBhcmd1bWVudCB0cmFuc2Zvcm1lZC5cbiAqXG4gKiBAcHJpdmF0ZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gZnVuYyBUaGUgZnVuY3Rpb24gdG8gd3JhcC5cbiAqIEBwYXJhbSB7RnVuY3Rpb259IHRyYW5zZm9ybSBUaGUgYXJndW1lbnQgdHJhbnNmb3JtLlxuICogQHJldHVybnMge0Z1bmN0aW9ufSBSZXR1cm5zIHRoZSBuZXcgZnVuY3Rpb24uXG4gKi9cbmZ1bmN0aW9uIG92ZXJBcmcoZnVuYywgdHJhbnNmb3JtKSB7XG4gIHJldHVybiBmdW5jdGlvbihhcmcpIHtcbiAgICByZXR1cm4gZnVuYyh0cmFuc2Zvcm0oYXJnKSk7XG4gIH07XG59XG5cbm1vZHVsZS5leHBvcnRzID0gb3ZlckFyZztcbiIsIi8qKlxuICogQ2hlY2tzIGlmIGB2YWx1ZWAgaXMgb2JqZWN0LWxpa2UuIEEgdmFsdWUgaXMgb2JqZWN0LWxpa2UgaWYgaXQncyBub3QgYG51bGxgXG4gKiBhbmQgaGFzIGEgYHR5cGVvZmAgcmVzdWx0IG9mIFwib2JqZWN0XCIuXG4gKlxuICogQHN0YXRpY1xuICogQG1lbWJlck9mIF9cbiAqIEBzaW5jZSA0LjAuMFxuICogQGNhdGVnb3J5IExhbmdcbiAqIEBwYXJhbSB7Kn0gdmFsdWUgVGhlIHZhbHVlIHRvIGNoZWNrLlxuICogQHJldHVybnMge2Jvb2xlYW59IFJldHVybnMgYHRydWVgIGlmIGB2YWx1ZWAgaXMgb2JqZWN0LWxpa2UsIGVsc2UgYGZhbHNlYC5cbiAqIEBleGFtcGxlXG4gKlxuICogXy5pc09iamVjdExpa2Uoe30pO1xuICogLy8gPT4gdHJ1ZVxuICpcbiAqIF8uaXNPYmplY3RMaWtlKFsxLCAyLCAzXSk7XG4gKiAvLyA9PiB0cnVlXG4gKlxuICogXy5pc09iamVjdExpa2UoXy5ub29wKTtcbiAqIC8vID0+IGZhbHNlXG4gKlxuICogXy5pc09iamVjdExpa2UobnVsbCk7XG4gKiAvLyA9PiBmYWxzZVxuICovXG5mdW5jdGlvbiBpc09iamVjdExpa2UodmFsdWUpIHtcbiAgcmV0dXJuICEhdmFsdWUgJiYgdHlwZW9mIHZhbHVlID09ICdvYmplY3QnO1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGlzT2JqZWN0TGlrZTtcbiIsInZhciBnZXRQcm90b3R5cGUgPSByZXF1aXJlKCcuL19nZXRQcm90b3R5cGUnKSxcbiAgICBpc0hvc3RPYmplY3QgPSByZXF1aXJlKCcuL19pc0hvc3RPYmplY3QnKSxcbiAgICBpc09iamVjdExpa2UgPSByZXF1aXJlKCcuL2lzT2JqZWN0TGlrZScpO1xuXG4vKiogYE9iamVjdCN0b1N0cmluZ2AgcmVzdWx0IHJlZmVyZW5jZXMuICovXG52YXIgb2JqZWN0VGFnID0gJ1tvYmplY3QgT2JqZWN0XSc7XG5cbi8qKiBVc2VkIGZvciBidWlsdC1pbiBtZXRob2QgcmVmZXJlbmNlcy4gKi9cbnZhciBvYmplY3RQcm90byA9IE9iamVjdC5wcm90b3R5cGU7XG5cbi8qKiBVc2VkIHRvIHJlc29sdmUgdGhlIGRlY29tcGlsZWQgc291cmNlIG9mIGZ1bmN0aW9ucy4gKi9cbnZhciBmdW5jVG9TdHJpbmcgPSBGdW5jdGlvbi5wcm90b3R5cGUudG9TdHJpbmc7XG5cbi8qKiBVc2VkIHRvIGNoZWNrIG9iamVjdHMgZm9yIG93biBwcm9wZXJ0aWVzLiAqL1xudmFyIGhhc093blByb3BlcnR5ID0gb2JqZWN0UHJvdG8uaGFzT3duUHJvcGVydHk7XG5cbi8qKiBVc2VkIHRvIGluZmVyIHRoZSBgT2JqZWN0YCBjb25zdHJ1Y3Rvci4gKi9cbnZhciBvYmplY3RDdG9yU3RyaW5nID0gZnVuY1RvU3RyaW5nLmNhbGwoT2JqZWN0KTtcblxuLyoqXG4gKiBVc2VkIHRvIHJlc29sdmUgdGhlXG4gKiBbYHRvU3RyaW5nVGFnYF0oaHR0cDovL2VjbWEtaW50ZXJuYXRpb25hbC5vcmcvZWNtYS0yNjIvNi4wLyNzZWMtb2JqZWN0LnByb3RvdHlwZS50b3N0cmluZylcbiAqIG9mIHZhbHVlcy5cbiAqL1xudmFyIG9iamVjdFRvU3RyaW5nID0gb2JqZWN0UHJvdG8udG9TdHJpbmc7XG5cbi8qKlxuICogQ2hlY2tzIGlmIGB2YWx1ZWAgaXMgYSBwbGFpbiBvYmplY3QsIHRoYXQgaXMsIGFuIG9iamVjdCBjcmVhdGVkIGJ5IHRoZVxuICogYE9iamVjdGAgY29uc3RydWN0b3Igb3Igb25lIHdpdGggYSBgW1tQcm90b3R5cGVdXWAgb2YgYG51bGxgLlxuICpcbiAqIEBzdGF0aWNcbiAqIEBtZW1iZXJPZiBfXG4gKiBAc2luY2UgMC44LjBcbiAqIEBjYXRlZ29yeSBMYW5nXG4gKiBAcGFyYW0geyp9IHZhbHVlIFRoZSB2YWx1ZSB0byBjaGVjay5cbiAqIEByZXR1cm5zIHtib29sZWFufSBSZXR1cm5zIGB0cnVlYCBpZiBgdmFsdWVgIGlzIGEgcGxhaW4gb2JqZWN0LFxuICogIGVsc2UgYGZhbHNlYC5cbiAqIEBleGFtcGxlXG4gKlxuICogZnVuY3Rpb24gRm9vKCkge1xuICogICB0aGlzLmEgPSAxO1xuICogfVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdChuZXcgRm9vKTtcbiAqIC8vID0+IGZhbHNlXG4gKlxuICogXy5pc1BsYWluT2JqZWN0KFsxLCAyLCAzXSk7XG4gKiAvLyA9PiBmYWxzZVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdCh7ICd4JzogMCwgJ3knOiAwIH0pO1xuICogLy8gPT4gdHJ1ZVxuICpcbiAqIF8uaXNQbGFpbk9iamVjdChPYmplY3QuY3JlYXRlKG51bGwpKTtcbiAqIC8vID0+IHRydWVcbiAqL1xuZnVuY3Rpb24gaXNQbGFpbk9iamVjdCh2YWx1ZSkge1xuICBpZiAoIWlzT2JqZWN0TGlrZSh2YWx1ZSkgfHxcbiAgICAgIG9iamVjdFRvU3RyaW5nLmNhbGwodmFsdWUpICE9IG9iamVjdFRhZyB8fCBpc0hvc3RPYmplY3QodmFsdWUpKSB7XG4gICAgcmV0dXJuIGZhbHNlO1xuICB9XG4gIHZhciBwcm90byA9IGdldFByb3RvdHlwZSh2YWx1ZSk7XG4gIGlmIChwcm90byA9PT0gbnVsbCkge1xuICAgIHJldHVybiB0cnVlO1xuICB9XG4gIHZhciBDdG9yID0gaGFzT3duUHJvcGVydHkuY2FsbChwcm90bywgJ2NvbnN0cnVjdG9yJykgJiYgcHJvdG8uY29uc3RydWN0b3I7XG4gIHJldHVybiAodHlwZW9mIEN0b3IgPT0gJ2Z1bmN0aW9uJyAmJlxuICAgIEN0b3IgaW5zdGFuY2VvZiBDdG9yICYmIGZ1bmNUb1N0cmluZy5jYWxsKEN0b3IpID09IG9iamVjdEN0b3JTdHJpbmcpO1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGlzUGxhaW5PYmplY3Q7XG4iLCIndXNlIHN0cmljdCc7XG52YXIgaXNPcHRpb25PYmplY3QgPSByZXF1aXJlKCdpcy1wbGFpbi1vYmonKTtcbnZhciBoYXNPd25Qcm9wZXJ0eSA9IE9iamVjdC5wcm90b3R5cGUuaGFzT3duUHJvcGVydHk7XG52YXIgcHJvcElzRW51bWVyYWJsZSA9IE9iamVjdC5wcm9wZXJ0eUlzRW51bWVyYWJsZTtcbnZhciBnbG9iYWxUaGlzID0gdGhpcztcbnZhciBkZWZhdWx0TWVyZ2VPcHRzID0ge1xuXHRjb25jYXRBcnJheXM6IGZhbHNlXG59O1xuXG5mdW5jdGlvbiBnZXRFbnVtZXJhYmxlT3duUHJvcGVydHlLZXlzKHZhbHVlKSB7XG5cdHZhciBrZXlzID0gW107XG5cblx0Zm9yICh2YXIga2V5IGluIHZhbHVlKSB7XG5cdFx0aWYgKGhhc093blByb3BlcnR5LmNhbGwodmFsdWUsIGtleSkpIHtcblx0XHRcdGtleXMucHVzaChrZXkpO1xuXHRcdH1cblx0fVxuXG5cdGlmIChPYmplY3QuZ2V0T3duUHJvcGVydHlTeW1ib2xzKSB7XG5cdFx0dmFyIHN5bWJvbHMgPSBPYmplY3QuZ2V0T3duUHJvcGVydHlTeW1ib2xzKHZhbHVlKTtcblxuXHRcdGZvciAodmFyIGkgPSAwOyBpIDwgc3ltYm9scy5sZW5ndGg7IGkrKykge1xuXHRcdFx0aWYgKHByb3BJc0VudW1lcmFibGUuY2FsbCh2YWx1ZSwgc3ltYm9sc1tpXSkpIHtcblx0XHRcdFx0a2V5cy5wdXNoKHN5bWJvbHNbaV0pO1xuXHRcdFx0fVxuXHRcdH1cblx0fVxuXG5cdHJldHVybiBrZXlzO1xufVxuXG5mdW5jdGlvbiBjbG9uZSh2YWx1ZSkge1xuXHRpZiAoQXJyYXkuaXNBcnJheSh2YWx1ZSkpIHtcblx0XHRyZXR1cm4gY2xvbmVBcnJheSh2YWx1ZSk7XG5cdH1cblxuXHRpZiAoaXNPcHRpb25PYmplY3QodmFsdWUpKSB7XG5cdFx0cmV0dXJuIGNsb25lT3B0aW9uT2JqZWN0KHZhbHVlKTtcblx0fVxuXG5cdHJldHVybiB2YWx1ZTtcbn1cblxuZnVuY3Rpb24gY2xvbmVBcnJheShhcnJheSkge1xuXHR2YXIgcmVzdWx0ID0gYXJyYXkuc2xpY2UoMCwgMCk7XG5cblx0Z2V0RW51bWVyYWJsZU93blByb3BlcnR5S2V5cyhhcnJheSkuZm9yRWFjaChmdW5jdGlvbiAoa2V5KSB7XG5cdFx0cmVzdWx0W2tleV0gPSBjbG9uZShhcnJheVtrZXldKTtcblx0fSk7XG5cblx0cmV0dXJuIHJlc3VsdDtcbn1cblxuZnVuY3Rpb24gY2xvbmVPcHRpb25PYmplY3Qob2JqKSB7XG5cdHZhciByZXN1bHQgPSBPYmplY3QuZ2V0UHJvdG90eXBlT2Yob2JqKSA9PT0gbnVsbCA/IE9iamVjdC5jcmVhdGUobnVsbCkgOiB7fTtcblxuXHRnZXRFbnVtZXJhYmxlT3duUHJvcGVydHlLZXlzKG9iaikuZm9yRWFjaChmdW5jdGlvbiAoa2V5KSB7XG5cdFx0cmVzdWx0W2tleV0gPSBjbG9uZShvYmpba2V5XSk7XG5cdH0pO1xuXG5cdHJldHVybiByZXN1bHQ7XG59XG5cbi8qKlxuICogQHBhcmFtIG1lcmdlZCB7YWxyZWFkeSBjbG9uZWR9XG4gKiBAcmV0dXJuIHtjbG9uZWQgT2JqZWN0fVxuICovXG5mdW5jdGlvbiBtZXJnZUtleXMobWVyZ2VkLCBzb3VyY2UsIGtleXMsIG1lcmdlT3B0cykge1xuXHRrZXlzLmZvckVhY2goZnVuY3Rpb24gKGtleSkge1xuXHRcdGlmIChrZXkgaW4gbWVyZ2VkKSB7XG5cdFx0XHRtZXJnZWRba2V5XSA9IG1lcmdlKG1lcmdlZFtrZXldLCBzb3VyY2Vba2V5XSwgbWVyZ2VPcHRzKTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0bWVyZ2VkW2tleV0gPSBjbG9uZShzb3VyY2Vba2V5XSk7XG5cdFx0fVxuXHR9KTtcblxuXHRyZXR1cm4gbWVyZ2VkO1xufVxuXG4vKipcbiAqIEBwYXJhbSBtZXJnZWQge2FscmVhZHkgY2xvbmVkfVxuICogQHJldHVybiB7Y2xvbmVkIE9iamVjdH1cbiAqXG4gKiBzZWUgW0FycmF5LnByb3RvdHlwZS5jb25jYXQgKCAuLi5hcmd1bWVudHMgKV0oaHR0cDovL3d3dy5lY21hLWludGVybmF0aW9uYWwub3JnL2VjbWEtMjYyLzYuMC8jc2VjLWFycmF5LnByb3RvdHlwZS5jb25jYXQpXG4gKi9cbmZ1bmN0aW9uIGNvbmNhdEFycmF5cyhtZXJnZWQsIHNvdXJjZSwgbWVyZ2VPcHRzKSB7XG5cdHZhciByZXN1bHQgPSBtZXJnZWQuc2xpY2UoMCwgMCk7XG5cdHZhciByZXN1bHRJbmRleCA9IDA7XG5cblx0W21lcmdlZCwgc291cmNlXS5mb3JFYWNoKGZ1bmN0aW9uIChhcnJheSkge1xuXHRcdHZhciBpbmRpY2VzID0gW107XG5cblx0XHQvLyByZXN1bHQuY29uY2F0KGFycmF5KSB3aXRoIGNsb25pbmdcblx0XHRmb3IgKHZhciBrID0gMDsgayA8IGFycmF5Lmxlbmd0aDsgaysrKSB7XG5cdFx0XHRpZiAoIWhhc093blByb3BlcnR5LmNhbGwoYXJyYXksIGspKSB7XG5cdFx0XHRcdGNvbnRpbnVlO1xuXHRcdFx0fVxuXG5cdFx0XHRpbmRpY2VzLnB1c2goU3RyaW5nKGspKTtcblxuXHRcdFx0aWYgKGFycmF5ID09PSBtZXJnZWQpIHtcblx0XHRcdFx0Ly8gYWxyZWFkeSBjbG9uZWRcblx0XHRcdFx0cmVzdWx0W3Jlc3VsdEluZGV4KytdID0gYXJyYXlba107XG5cdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRyZXN1bHRbcmVzdWx0SW5kZXgrK10gPSBjbG9uZShhcnJheVtrXSk7XG5cdFx0XHR9XG5cdFx0fVxuXG5cdFx0Ly8gbWVyZ2Ugbm9uLWluZGV4IGtleXNcblx0XHRyZXN1bHQgPSBtZXJnZUtleXMocmVzdWx0LCBhcnJheSwgZ2V0RW51bWVyYWJsZU93blByb3BlcnR5S2V5cyhhcnJheSkuZmlsdGVyKGZ1bmN0aW9uIChrZXkpIHtcblx0XHRcdHJldHVybiBpbmRpY2VzLmluZGV4T2Yoa2V5KSA9PT0gLTE7XG5cdFx0fSksIG1lcmdlT3B0cyk7XG5cdH0pO1xuXG5cdHJldHVybiByZXN1bHQ7XG59XG5cbi8qKlxuICogQHBhcmFtIG1lcmdlZCB7YWxyZWFkeSBjbG9uZWR9XG4gKiBAcmV0dXJuIHtjbG9uZWQgT2JqZWN0fVxuICovXG5mdW5jdGlvbiBtZXJnZShtZXJnZWQsIHNvdXJjZSwgbWVyZ2VPcHRzKSB7XG5cdGlmIChtZXJnZU9wdHMuY29uY2F0QXJyYXlzICYmIEFycmF5LmlzQXJyYXkobWVyZ2VkKSAmJiBBcnJheS5pc0FycmF5KHNvdXJjZSkpIHtcblx0XHRyZXR1cm4gY29uY2F0QXJyYXlzKG1lcmdlZCwgc291cmNlLCBtZXJnZU9wdHMpO1xuXHR9XG5cblx0aWYgKCFpc09wdGlvbk9iamVjdChzb3VyY2UpIHx8ICFpc09wdGlvbk9iamVjdChtZXJnZWQpKSB7XG5cdFx0cmV0dXJuIGNsb25lKHNvdXJjZSk7XG5cdH1cblxuXHRyZXR1cm4gbWVyZ2VLZXlzKG1lcmdlZCwgc291cmNlLCBnZXRFbnVtZXJhYmxlT3duUHJvcGVydHlLZXlzKHNvdXJjZSksIG1lcmdlT3B0cyk7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24gKCkge1xuXHR2YXIgbWVyZ2VPcHRzID0gbWVyZ2UoY2xvbmUoZGVmYXVsdE1lcmdlT3B0cyksICh0aGlzICE9PSBnbG9iYWxUaGlzICYmIHRoaXMpIHx8IHt9LCBkZWZhdWx0TWVyZ2VPcHRzKTtcblx0dmFyIG1lcmdlZCA9IHt9O1xuXG5cdGZvciAodmFyIGkgPSAwOyBpIDwgYXJndW1lbnRzLmxlbmd0aDsgaSsrKSB7XG5cdFx0dmFyIG9wdGlvbiA9IGFyZ3VtZW50c1tpXTtcblxuXHRcdGlmIChvcHRpb24gPT09IHVuZGVmaW5lZCkge1xuXHRcdFx0Y29udGludWU7XG5cdFx0fVxuXG5cdFx0aWYgKCFpc09wdGlvbk9iamVjdChvcHRpb24pKSB7XG5cdFx0XHR0aHJvdyBuZXcgVHlwZUVycm9yKCdgJyArIG9wdGlvbiArICdgIGlzIG5vdCBhbiBPcHRpb24gT2JqZWN0Jyk7XG5cdFx0fVxuXG5cdFx0bWVyZ2VkID0gbWVyZ2UobWVyZ2VkLCBvcHRpb24sIG1lcmdlT3B0cyk7XG5cdH1cblxuXHRyZXR1cm4gbWVyZ2VkO1xufTtcbiIsInZhciB0cmltID0gcmVxdWlyZSgndHJpbScpXG4gICwgZm9yRWFjaCA9IHJlcXVpcmUoJ2Zvci1lYWNoJylcbiAgLCBpc0FycmF5ID0gZnVuY3Rpb24oYXJnKSB7XG4gICAgICByZXR1cm4gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKGFyZykgPT09ICdbb2JqZWN0IEFycmF5XSc7XG4gICAgfVxuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uIChoZWFkZXJzKSB7XG4gIGlmICghaGVhZGVycylcbiAgICByZXR1cm4ge31cblxuICB2YXIgcmVzdWx0ID0ge31cblxuICBmb3JFYWNoKFxuICAgICAgdHJpbShoZWFkZXJzKS5zcGxpdCgnXFxuJylcbiAgICAsIGZ1bmN0aW9uIChyb3cpIHtcbiAgICAgICAgdmFyIGluZGV4ID0gcm93LmluZGV4T2YoJzonKVxuICAgICAgICAgICwga2V5ID0gdHJpbShyb3cuc2xpY2UoMCwgaW5kZXgpKS50b0xvd2VyQ2FzZSgpXG4gICAgICAgICAgLCB2YWx1ZSA9IHRyaW0ocm93LnNsaWNlKGluZGV4ICsgMSkpXG5cbiAgICAgICAgaWYgKHR5cGVvZihyZXN1bHRba2V5XSkgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICAgICAgcmVzdWx0W2tleV0gPSB2YWx1ZVxuICAgICAgICB9IGVsc2UgaWYgKGlzQXJyYXkocmVzdWx0W2tleV0pKSB7XG4gICAgICAgICAgcmVzdWx0W2tleV0ucHVzaCh2YWx1ZSlcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICByZXN1bHRba2V5XSA9IFsgcmVzdWx0W2tleV0sIHZhbHVlIF1cbiAgICAgICAgfVxuICAgICAgfVxuICApXG5cbiAgcmV0dXJuIHJlc3VsdFxufSIsIid1c2Ugc3RyaWN0JztcblxuZnVuY3Rpb24gdGh1bmtNaWRkbGV3YXJlKF9yZWYpIHtcbiAgdmFyIGRpc3BhdGNoID0gX3JlZi5kaXNwYXRjaDtcbiAgdmFyIGdldFN0YXRlID0gX3JlZi5nZXRTdGF0ZTtcblxuICByZXR1cm4gZnVuY3Rpb24gKG5leHQpIHtcbiAgICByZXR1cm4gZnVuY3Rpb24gKGFjdGlvbikge1xuICAgICAgcmV0dXJuIHR5cGVvZiBhY3Rpb24gPT09ICdmdW5jdGlvbicgPyBhY3Rpb24oZGlzcGF0Y2gsIGdldFN0YXRlKSA6IG5leHQoYWN0aW9uKTtcbiAgICB9O1xuICB9O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IHRodW5rTWlkZGxld2FyZTsiLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5cbnZhciBfZXh0ZW5kcyA9IE9iamVjdC5hc3NpZ24gfHwgZnVuY3Rpb24gKHRhcmdldCkgeyBmb3IgKHZhciBpID0gMTsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykgeyB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldOyBmb3IgKHZhciBrZXkgaW4gc291cmNlKSB7IGlmIChPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7IHRhcmdldFtrZXldID0gc291cmNlW2tleV07IH0gfSB9IHJldHVybiB0YXJnZXQ7IH07XG5cbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gYXBwbHlNaWRkbGV3YXJlO1xuXG52YXIgX2NvbXBvc2UgPSByZXF1aXJlKCcuL2NvbXBvc2UnKTtcblxudmFyIF9jb21wb3NlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NvbXBvc2UpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuLyoqXG4gKiBDcmVhdGVzIGEgc3RvcmUgZW5oYW5jZXIgdGhhdCBhcHBsaWVzIG1pZGRsZXdhcmUgdG8gdGhlIGRpc3BhdGNoIG1ldGhvZFxuICogb2YgdGhlIFJlZHV4IHN0b3JlLiBUaGlzIGlzIGhhbmR5IGZvciBhIHZhcmlldHkgb2YgdGFza3MsIHN1Y2ggYXMgZXhwcmVzc2luZ1xuICogYXN5bmNocm9ub3VzIGFjdGlvbnMgaW4gYSBjb25jaXNlIG1hbm5lciwgb3IgbG9nZ2luZyBldmVyeSBhY3Rpb24gcGF5bG9hZC5cbiAqXG4gKiBTZWUgYHJlZHV4LXRodW5rYCBwYWNrYWdlIGFzIGFuIGV4YW1wbGUgb2YgdGhlIFJlZHV4IG1pZGRsZXdhcmUuXG4gKlxuICogQmVjYXVzZSBtaWRkbGV3YXJlIGlzIHBvdGVudGlhbGx5IGFzeW5jaHJvbm91cywgdGhpcyBzaG91bGQgYmUgdGhlIGZpcnN0XG4gKiBzdG9yZSBlbmhhbmNlciBpbiB0aGUgY29tcG9zaXRpb24gY2hhaW4uXG4gKlxuICogTm90ZSB0aGF0IGVhY2ggbWlkZGxld2FyZSB3aWxsIGJlIGdpdmVuIHRoZSBgZGlzcGF0Y2hgIGFuZCBgZ2V0U3RhdGVgIGZ1bmN0aW9uc1xuICogYXMgbmFtZWQgYXJndW1lbnRzLlxuICpcbiAqIEBwYXJhbSB7Li4uRnVuY3Rpb259IG1pZGRsZXdhcmVzIFRoZSBtaWRkbGV3YXJlIGNoYWluIHRvIGJlIGFwcGxpZWQuXG4gKiBAcmV0dXJucyB7RnVuY3Rpb259IEEgc3RvcmUgZW5oYW5jZXIgYXBwbHlpbmcgdGhlIG1pZGRsZXdhcmUuXG4gKi9cbmZ1bmN0aW9uIGFwcGx5TWlkZGxld2FyZSgpIHtcbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIG1pZGRsZXdhcmVzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgbWlkZGxld2FyZXNbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICByZXR1cm4gZnVuY3Rpb24gKGNyZWF0ZVN0b3JlKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChyZWR1Y2VyLCBpbml0aWFsU3RhdGUsIGVuaGFuY2VyKSB7XG4gICAgICB2YXIgc3RvcmUgPSBjcmVhdGVTdG9yZShyZWR1Y2VyLCBpbml0aWFsU3RhdGUsIGVuaGFuY2VyKTtcbiAgICAgIHZhciBfZGlzcGF0Y2ggPSBzdG9yZS5kaXNwYXRjaDtcbiAgICAgIHZhciBjaGFpbiA9IFtdO1xuXG4gICAgICB2YXIgbWlkZGxld2FyZUFQSSA9IHtcbiAgICAgICAgZ2V0U3RhdGU6IHN0b3JlLmdldFN0YXRlLFxuICAgICAgICBkaXNwYXRjaDogZnVuY3Rpb24gZGlzcGF0Y2goYWN0aW9uKSB7XG4gICAgICAgICAgcmV0dXJuIF9kaXNwYXRjaChhY3Rpb24pO1xuICAgICAgICB9XG4gICAgICB9O1xuICAgICAgY2hhaW4gPSBtaWRkbGV3YXJlcy5tYXAoZnVuY3Rpb24gKG1pZGRsZXdhcmUpIHtcbiAgICAgICAgcmV0dXJuIG1pZGRsZXdhcmUobWlkZGxld2FyZUFQSSk7XG4gICAgICB9KTtcbiAgICAgIF9kaXNwYXRjaCA9IF9jb21wb3NlMltcImRlZmF1bHRcIl0uYXBwbHkodW5kZWZpbmVkLCBjaGFpbikoc3RvcmUuZGlzcGF0Y2gpO1xuXG4gICAgICByZXR1cm4gX2V4dGVuZHMoe30sIHN0b3JlLCB7XG4gICAgICAgIGRpc3BhdGNoOiBfZGlzcGF0Y2hcbiAgICAgIH0pO1xuICAgIH07XG4gIH07XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0c1tcImRlZmF1bHRcIl0gPSBiaW5kQWN0aW9uQ3JlYXRvcnM7XG5mdW5jdGlvbiBiaW5kQWN0aW9uQ3JlYXRvcihhY3Rpb25DcmVhdG9yLCBkaXNwYXRjaCkge1xuICByZXR1cm4gZnVuY3Rpb24gKCkge1xuICAgIHJldHVybiBkaXNwYXRjaChhY3Rpb25DcmVhdG9yLmFwcGx5KHVuZGVmaW5lZCwgYXJndW1lbnRzKSk7XG4gIH07XG59XG5cbi8qKlxuICogVHVybnMgYW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgYWN0aW9uIGNyZWF0b3JzLCBpbnRvIGFuIG9iamVjdCB3aXRoIHRoZVxuICogc2FtZSBrZXlzLCBidXQgd2l0aCBldmVyeSBmdW5jdGlvbiB3cmFwcGVkIGludG8gYSBgZGlzcGF0Y2hgIGNhbGwgc28gdGhleVxuICogbWF5IGJlIGludm9rZWQgZGlyZWN0bHkuIFRoaXMgaXMganVzdCBhIGNvbnZlbmllbmNlIG1ldGhvZCwgYXMgeW91IGNhbiBjYWxsXG4gKiBgc3RvcmUuZGlzcGF0Y2goTXlBY3Rpb25DcmVhdG9ycy5kb1NvbWV0aGluZygpKWAgeW91cnNlbGYganVzdCBmaW5lLlxuICpcbiAqIEZvciBjb252ZW5pZW5jZSwgeW91IGNhbiBhbHNvIHBhc3MgYSBzaW5nbGUgZnVuY3Rpb24gYXMgdGhlIGZpcnN0IGFyZ3VtZW50LFxuICogYW5kIGdldCBhIGZ1bmN0aW9uIGluIHJldHVybi5cbiAqXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufE9iamVjdH0gYWN0aW9uQ3JlYXRvcnMgQW4gb2JqZWN0IHdob3NlIHZhbHVlcyBhcmUgYWN0aW9uXG4gKiBjcmVhdG9yIGZ1bmN0aW9ucy4gT25lIGhhbmR5IHdheSB0byBvYnRhaW4gaXQgaXMgdG8gdXNlIEVTNiBgaW1wb3J0ICogYXNgXG4gKiBzeW50YXguIFlvdSBtYXkgYWxzbyBwYXNzIGEgc2luZ2xlIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGRpc3BhdGNoIFRoZSBgZGlzcGF0Y2hgIGZ1bmN0aW9uIGF2YWlsYWJsZSBvbiB5b3VyIFJlZHV4XG4gKiBzdG9yZS5cbiAqXG4gKiBAcmV0dXJucyB7RnVuY3Rpb258T2JqZWN0fSBUaGUgb2JqZWN0IG1pbWlja2luZyB0aGUgb3JpZ2luYWwgb2JqZWN0LCBidXQgd2l0aFxuICogZXZlcnkgYWN0aW9uIGNyZWF0b3Igd3JhcHBlZCBpbnRvIHRoZSBgZGlzcGF0Y2hgIGNhbGwuIElmIHlvdSBwYXNzZWQgYVxuICogZnVuY3Rpb24gYXMgYGFjdGlvbkNyZWF0b3JzYCwgdGhlIHJldHVybiB2YWx1ZSB3aWxsIGFsc28gYmUgYSBzaW5nbGVcbiAqIGZ1bmN0aW9uLlxuICovXG5mdW5jdGlvbiBiaW5kQWN0aW9uQ3JlYXRvcnMoYWN0aW9uQ3JlYXRvcnMsIGRpc3BhdGNoKSB7XG4gIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvcnMgPT09ICdmdW5jdGlvbicpIHtcbiAgICByZXR1cm4gYmluZEFjdGlvbkNyZWF0b3IoYWN0aW9uQ3JlYXRvcnMsIGRpc3BhdGNoKTtcbiAgfVxuXG4gIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvcnMgIT09ICdvYmplY3QnIHx8IGFjdGlvbkNyZWF0b3JzID09PSBudWxsKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdiaW5kQWN0aW9uQ3JlYXRvcnMgZXhwZWN0ZWQgYW4gb2JqZWN0IG9yIGEgZnVuY3Rpb24sIGluc3RlYWQgcmVjZWl2ZWQgJyArIChhY3Rpb25DcmVhdG9ycyA9PT0gbnVsbCA/ICdudWxsJyA6IHR5cGVvZiBhY3Rpb25DcmVhdG9ycykgKyAnLiAnICsgJ0RpZCB5b3Ugd3JpdGUgXCJpbXBvcnQgQWN0aW9uQ3JlYXRvcnMgZnJvbVwiIGluc3RlYWQgb2YgXCJpbXBvcnQgKiBhcyBBY3Rpb25DcmVhdG9ycyBmcm9tXCI/Jyk7XG4gIH1cblxuICB2YXIga2V5cyA9IE9iamVjdC5rZXlzKGFjdGlvbkNyZWF0b3JzKTtcbiAgdmFyIGJvdW5kQWN0aW9uQ3JlYXRvcnMgPSB7fTtcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBrZXlzLmxlbmd0aDsgaSsrKSB7XG4gICAgdmFyIGtleSA9IGtleXNbaV07XG4gICAgdmFyIGFjdGlvbkNyZWF0b3IgPSBhY3Rpb25DcmVhdG9yc1trZXldO1xuICAgIGlmICh0eXBlb2YgYWN0aW9uQ3JlYXRvciA9PT0gJ2Z1bmN0aW9uJykge1xuICAgICAgYm91bmRBY3Rpb25DcmVhdG9yc1trZXldID0gYmluZEFjdGlvbkNyZWF0b3IoYWN0aW9uQ3JlYXRvciwgZGlzcGF0Y2gpO1xuICAgIH1cbiAgfVxuICByZXR1cm4gYm91bmRBY3Rpb25DcmVhdG9ycztcbn0iLCIndXNlIHN0cmljdCc7XG5cbmV4cG9ydHMuX19lc01vZHVsZSA9IHRydWU7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGNvbWJpbmVSZWR1Y2VycztcblxudmFyIF9jcmVhdGVTdG9yZSA9IHJlcXVpcmUoJy4vY3JlYXRlU3RvcmUnKTtcblxudmFyIF9pc1BsYWluT2JqZWN0ID0gcmVxdWlyZSgnbG9kYXNoL2lzUGxhaW5PYmplY3QnKTtcblxudmFyIF9pc1BsYWluT2JqZWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2lzUGxhaW5PYmplY3QpO1xuXG52YXIgX3dhcm5pbmcgPSByZXF1aXJlKCcuL3V0aWxzL3dhcm5pbmcnKTtcblxudmFyIF93YXJuaW5nMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3dhcm5pbmcpO1xuXG5mdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikgeyByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDogeyBcImRlZmF1bHRcIjogb2JqIH07IH1cblxuZnVuY3Rpb24gZ2V0VW5kZWZpbmVkU3RhdGVFcnJvck1lc3NhZ2Uoa2V5LCBhY3Rpb24pIHtcbiAgdmFyIGFjdGlvblR5cGUgPSBhY3Rpb24gJiYgYWN0aW9uLnR5cGU7XG4gIHZhciBhY3Rpb25OYW1lID0gYWN0aW9uVHlwZSAmJiAnXCInICsgYWN0aW9uVHlwZS50b1N0cmluZygpICsgJ1wiJyB8fCAnYW4gYWN0aW9uJztcblxuICByZXR1cm4gJ0dpdmVuIGFjdGlvbiAnICsgYWN0aW9uTmFtZSArICcsIHJlZHVjZXIgXCInICsga2V5ICsgJ1wiIHJldHVybmVkIHVuZGVmaW5lZC4gJyArICdUbyBpZ25vcmUgYW4gYWN0aW9uLCB5b3UgbXVzdCBleHBsaWNpdGx5IHJldHVybiB0aGUgcHJldmlvdXMgc3RhdGUuJztcbn1cblxuZnVuY3Rpb24gZ2V0VW5leHBlY3RlZFN0YXRlU2hhcGVXYXJuaW5nTWVzc2FnZShpbnB1dFN0YXRlLCByZWR1Y2VycywgYWN0aW9uKSB7XG4gIHZhciByZWR1Y2VyS2V5cyA9IE9iamVjdC5rZXlzKHJlZHVjZXJzKTtcbiAgdmFyIGFyZ3VtZW50TmFtZSA9IGFjdGlvbiAmJiBhY3Rpb24udHlwZSA9PT0gX2NyZWF0ZVN0b3JlLkFjdGlvblR5cGVzLklOSVQgPyAnaW5pdGlhbFN0YXRlIGFyZ3VtZW50IHBhc3NlZCB0byBjcmVhdGVTdG9yZScgOiAncHJldmlvdXMgc3RhdGUgcmVjZWl2ZWQgYnkgdGhlIHJlZHVjZXInO1xuXG4gIGlmIChyZWR1Y2VyS2V5cy5sZW5ndGggPT09IDApIHtcbiAgICByZXR1cm4gJ1N0b3JlIGRvZXMgbm90IGhhdmUgYSB2YWxpZCByZWR1Y2VyLiBNYWtlIHN1cmUgdGhlIGFyZ3VtZW50IHBhc3NlZCAnICsgJ3RvIGNvbWJpbmVSZWR1Y2VycyBpcyBhbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGFyZSByZWR1Y2Vycy4nO1xuICB9XG5cbiAgaWYgKCEoMCwgX2lzUGxhaW5PYmplY3QyW1wiZGVmYXVsdFwiXSkoaW5wdXRTdGF0ZSkpIHtcbiAgICByZXR1cm4gJ1RoZSAnICsgYXJndW1lbnROYW1lICsgJyBoYXMgdW5leHBlY3RlZCB0eXBlIG9mIFwiJyArIHt9LnRvU3RyaW5nLmNhbGwoaW5wdXRTdGF0ZSkubWF0Y2goL1xccyhbYS16fEEtWl0rKS8pWzFdICsgJ1wiLiBFeHBlY3RlZCBhcmd1bWVudCB0byBiZSBhbiBvYmplY3Qgd2l0aCB0aGUgZm9sbG93aW5nICcgKyAoJ2tleXM6IFwiJyArIHJlZHVjZXJLZXlzLmpvaW4oJ1wiLCBcIicpICsgJ1wiJyk7XG4gIH1cblxuICB2YXIgdW5leHBlY3RlZEtleXMgPSBPYmplY3Qua2V5cyhpbnB1dFN0YXRlKS5maWx0ZXIoZnVuY3Rpb24gKGtleSkge1xuICAgIHJldHVybiAhcmVkdWNlcnMuaGFzT3duUHJvcGVydHkoa2V5KTtcbiAgfSk7XG5cbiAgaWYgKHVuZXhwZWN0ZWRLZXlzLmxlbmd0aCA+IDApIHtcbiAgICByZXR1cm4gJ1VuZXhwZWN0ZWQgJyArICh1bmV4cGVjdGVkS2V5cy5sZW5ndGggPiAxID8gJ2tleXMnIDogJ2tleScpICsgJyAnICsgKCdcIicgKyB1bmV4cGVjdGVkS2V5cy5qb2luKCdcIiwgXCInKSArICdcIiBmb3VuZCBpbiAnICsgYXJndW1lbnROYW1lICsgJy4gJykgKyAnRXhwZWN0ZWQgdG8gZmluZCBvbmUgb2YgdGhlIGtub3duIHJlZHVjZXIga2V5cyBpbnN0ZWFkOiAnICsgKCdcIicgKyByZWR1Y2VyS2V5cy5qb2luKCdcIiwgXCInKSArICdcIi4gVW5leHBlY3RlZCBrZXlzIHdpbGwgYmUgaWdub3JlZC4nKTtcbiAgfVxufVxuXG5mdW5jdGlvbiBhc3NlcnRSZWR1Y2VyU2FuaXR5KHJlZHVjZXJzKSB7XG4gIE9iamVjdC5rZXlzKHJlZHVjZXJzKS5mb3JFYWNoKGZ1bmN0aW9uIChrZXkpIHtcbiAgICB2YXIgcmVkdWNlciA9IHJlZHVjZXJzW2tleV07XG4gICAgdmFyIGluaXRpYWxTdGF0ZSA9IHJlZHVjZXIodW5kZWZpbmVkLCB7IHR5cGU6IF9jcmVhdGVTdG9yZS5BY3Rpb25UeXBlcy5JTklUIH0pO1xuXG4gICAgaWYgKHR5cGVvZiBpbml0aWFsU3RhdGUgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1JlZHVjZXIgXCInICsga2V5ICsgJ1wiIHJldHVybmVkIHVuZGVmaW5lZCBkdXJpbmcgaW5pdGlhbGl6YXRpb24uICcgKyAnSWYgdGhlIHN0YXRlIHBhc3NlZCB0byB0aGUgcmVkdWNlciBpcyB1bmRlZmluZWQsIHlvdSBtdXN0ICcgKyAnZXhwbGljaXRseSByZXR1cm4gdGhlIGluaXRpYWwgc3RhdGUuIFRoZSBpbml0aWFsIHN0YXRlIG1heSAnICsgJ25vdCBiZSB1bmRlZmluZWQuJyk7XG4gICAgfVxuXG4gICAgdmFyIHR5cGUgPSAnQEByZWR1eC9QUk9CRV9VTktOT1dOX0FDVElPTl8nICsgTWF0aC5yYW5kb20oKS50b1N0cmluZygzNikuc3Vic3RyaW5nKDcpLnNwbGl0KCcnKS5qb2luKCcuJyk7XG4gICAgaWYgKHR5cGVvZiByZWR1Y2VyKHVuZGVmaW5lZCwgeyB0eXBlOiB0eXBlIH0pID09PSAndW5kZWZpbmVkJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKCdSZWR1Y2VyIFwiJyArIGtleSArICdcIiByZXR1cm5lZCB1bmRlZmluZWQgd2hlbiBwcm9iZWQgd2l0aCBhIHJhbmRvbSB0eXBlLiAnICsgKCdEb25cXCd0IHRyeSB0byBoYW5kbGUgJyArIF9jcmVhdGVTdG9yZS5BY3Rpb25UeXBlcy5JTklUICsgJyBvciBvdGhlciBhY3Rpb25zIGluIFwicmVkdXgvKlwiICcpICsgJ25hbWVzcGFjZS4gVGhleSBhcmUgY29uc2lkZXJlZCBwcml2YXRlLiBJbnN0ZWFkLCB5b3UgbXVzdCByZXR1cm4gdGhlICcgKyAnY3VycmVudCBzdGF0ZSBmb3IgYW55IHVua25vd24gYWN0aW9ucywgdW5sZXNzIGl0IGlzIHVuZGVmaW5lZCwgJyArICdpbiB3aGljaCBjYXNlIHlvdSBtdXN0IHJldHVybiB0aGUgaW5pdGlhbCBzdGF0ZSwgcmVnYXJkbGVzcyBvZiB0aGUgJyArICdhY3Rpb24gdHlwZS4gVGhlIGluaXRpYWwgc3RhdGUgbWF5IG5vdCBiZSB1bmRlZmluZWQuJyk7XG4gICAgfVxuICB9KTtcbn1cblxuLyoqXG4gKiBUdXJucyBhbiBvYmplY3Qgd2hvc2UgdmFsdWVzIGFyZSBkaWZmZXJlbnQgcmVkdWNlciBmdW5jdGlvbnMsIGludG8gYSBzaW5nbGVcbiAqIHJlZHVjZXIgZnVuY3Rpb24uIEl0IHdpbGwgY2FsbCBldmVyeSBjaGlsZCByZWR1Y2VyLCBhbmQgZ2F0aGVyIHRoZWlyIHJlc3VsdHNcbiAqIGludG8gYSBzaW5nbGUgc3RhdGUgb2JqZWN0LCB3aG9zZSBrZXlzIGNvcnJlc3BvbmQgdG8gdGhlIGtleXMgb2YgdGhlIHBhc3NlZFxuICogcmVkdWNlciBmdW5jdGlvbnMuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHJlZHVjZXJzIEFuIG9iamVjdCB3aG9zZSB2YWx1ZXMgY29ycmVzcG9uZCB0byBkaWZmZXJlbnRcbiAqIHJlZHVjZXIgZnVuY3Rpb25zIHRoYXQgbmVlZCB0byBiZSBjb21iaW5lZCBpbnRvIG9uZS4gT25lIGhhbmR5IHdheSB0byBvYnRhaW5cbiAqIGl0IGlzIHRvIHVzZSBFUzYgYGltcG9ydCAqIGFzIHJlZHVjZXJzYCBzeW50YXguIFRoZSByZWR1Y2VycyBtYXkgbmV2ZXIgcmV0dXJuXG4gKiB1bmRlZmluZWQgZm9yIGFueSBhY3Rpb24uIEluc3RlYWQsIHRoZXkgc2hvdWxkIHJldHVybiB0aGVpciBpbml0aWFsIHN0YXRlXG4gKiBpZiB0aGUgc3RhdGUgcGFzc2VkIHRvIHRoZW0gd2FzIHVuZGVmaW5lZCwgYW5kIHRoZSBjdXJyZW50IHN0YXRlIGZvciBhbnlcbiAqIHVucmVjb2duaXplZCBhY3Rpb24uXG4gKlxuICogQHJldHVybnMge0Z1bmN0aW9ufSBBIHJlZHVjZXIgZnVuY3Rpb24gdGhhdCBpbnZva2VzIGV2ZXJ5IHJlZHVjZXIgaW5zaWRlIHRoZVxuICogcGFzc2VkIG9iamVjdCwgYW5kIGJ1aWxkcyBhIHN0YXRlIG9iamVjdCB3aXRoIHRoZSBzYW1lIHNoYXBlLlxuICovXG5mdW5jdGlvbiBjb21iaW5lUmVkdWNlcnMocmVkdWNlcnMpIHtcbiAgdmFyIHJlZHVjZXJLZXlzID0gT2JqZWN0LmtleXMocmVkdWNlcnMpO1xuICB2YXIgZmluYWxSZWR1Y2VycyA9IHt9O1xuICBmb3IgKHZhciBpID0gMDsgaSA8IHJlZHVjZXJLZXlzLmxlbmd0aDsgaSsrKSB7XG4gICAgdmFyIGtleSA9IHJlZHVjZXJLZXlzW2ldO1xuICAgIGlmICh0eXBlb2YgcmVkdWNlcnNba2V5XSA9PT0gJ2Z1bmN0aW9uJykge1xuICAgICAgZmluYWxSZWR1Y2Vyc1trZXldID0gcmVkdWNlcnNba2V5XTtcbiAgICB9XG4gIH1cbiAgdmFyIGZpbmFsUmVkdWNlcktleXMgPSBPYmplY3Qua2V5cyhmaW5hbFJlZHVjZXJzKTtcblxuICB2YXIgc2FuaXR5RXJyb3I7XG4gIHRyeSB7XG4gICAgYXNzZXJ0UmVkdWNlclNhbml0eShmaW5hbFJlZHVjZXJzKTtcbiAgfSBjYXRjaCAoZSkge1xuICAgIHNhbml0eUVycm9yID0gZTtcbiAgfVxuXG4gIHJldHVybiBmdW5jdGlvbiBjb21iaW5hdGlvbigpIHtcbiAgICB2YXIgc3RhdGUgPSBhcmd1bWVudHMubGVuZ3RoIDw9IDAgfHwgYXJndW1lbnRzWzBdID09PSB1bmRlZmluZWQgPyB7fSA6IGFyZ3VtZW50c1swXTtcbiAgICB2YXIgYWN0aW9uID0gYXJndW1lbnRzWzFdO1xuXG4gICAgaWYgKHNhbml0eUVycm9yKSB7XG4gICAgICB0aHJvdyBzYW5pdHlFcnJvcjtcbiAgICB9XG5cbiAgICBpZiAoXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJykge1xuICAgICAgdmFyIHdhcm5pbmdNZXNzYWdlID0gZ2V0VW5leHBlY3RlZFN0YXRlU2hhcGVXYXJuaW5nTWVzc2FnZShzdGF0ZSwgZmluYWxSZWR1Y2VycywgYWN0aW9uKTtcbiAgICAgIGlmICh3YXJuaW5nTWVzc2FnZSkge1xuICAgICAgICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkod2FybmluZ01lc3NhZ2UpO1xuICAgICAgfVxuICAgIH1cblxuICAgIHZhciBoYXNDaGFuZ2VkID0gZmFsc2U7XG4gICAgdmFyIG5leHRTdGF0ZSA9IHt9O1xuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgZmluYWxSZWR1Y2VyS2V5cy5sZW5ndGg7IGkrKykge1xuICAgICAgdmFyIGtleSA9IGZpbmFsUmVkdWNlcktleXNbaV07XG4gICAgICB2YXIgcmVkdWNlciA9IGZpbmFsUmVkdWNlcnNba2V5XTtcbiAgICAgIHZhciBwcmV2aW91c1N0YXRlRm9yS2V5ID0gc3RhdGVba2V5XTtcbiAgICAgIHZhciBuZXh0U3RhdGVGb3JLZXkgPSByZWR1Y2VyKHByZXZpb3VzU3RhdGVGb3JLZXksIGFjdGlvbik7XG4gICAgICBpZiAodHlwZW9mIG5leHRTdGF0ZUZvcktleSA9PT0gJ3VuZGVmaW5lZCcpIHtcbiAgICAgICAgdmFyIGVycm9yTWVzc2FnZSA9IGdldFVuZGVmaW5lZFN0YXRlRXJyb3JNZXNzYWdlKGtleSwgYWN0aW9uKTtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKGVycm9yTWVzc2FnZSk7XG4gICAgICB9XG4gICAgICBuZXh0U3RhdGVba2V5XSA9IG5leHRTdGF0ZUZvcktleTtcbiAgICAgIGhhc0NoYW5nZWQgPSBoYXNDaGFuZ2VkIHx8IG5leHRTdGF0ZUZvcktleSAhPT0gcHJldmlvdXNTdGF0ZUZvcktleTtcbiAgICB9XG4gICAgcmV0dXJuIGhhc0NoYW5nZWQgPyBuZXh0U3RhdGUgOiBzdGF0ZTtcbiAgfTtcbn0iLCJcInVzZSBzdHJpY3RcIjtcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gY29tcG9zZTtcbi8qKlxuICogQ29tcG9zZXMgc2luZ2xlLWFyZ3VtZW50IGZ1bmN0aW9ucyBmcm9tIHJpZ2h0IHRvIGxlZnQuIFRoZSByaWdodG1vc3RcbiAqIGZ1bmN0aW9uIGNhbiB0YWtlIG11bHRpcGxlIGFyZ3VtZW50cyBhcyBpdCBwcm92aWRlcyB0aGUgc2lnbmF0dXJlIGZvclxuICogdGhlIHJlc3VsdGluZyBjb21wb3NpdGUgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHsuLi5GdW5jdGlvbn0gZnVuY3MgVGhlIGZ1bmN0aW9ucyB0byBjb21wb3NlLlxuICogQHJldHVybnMge0Z1bmN0aW9ufSBBIGZ1bmN0aW9uIG9idGFpbmVkIGJ5IGNvbXBvc2luZyB0aGUgYXJndW1lbnQgZnVuY3Rpb25zXG4gKiBmcm9tIHJpZ2h0IHRvIGxlZnQuIEZvciBleGFtcGxlLCBjb21wb3NlKGYsIGcsIGgpIGlzIGlkZW50aWNhbCB0byBkb2luZ1xuICogKC4uLmFyZ3MpID0+IGYoZyhoKC4uLmFyZ3MpKSkuXG4gKi9cblxuZnVuY3Rpb24gY29tcG9zZSgpIHtcbiAgZm9yICh2YXIgX2xlbiA9IGFyZ3VtZW50cy5sZW5ndGgsIGZ1bmNzID0gQXJyYXkoX2xlbiksIF9rZXkgPSAwOyBfa2V5IDwgX2xlbjsgX2tleSsrKSB7XG4gICAgZnVuY3NbX2tleV0gPSBhcmd1bWVudHNbX2tleV07XG4gIH1cblxuICBpZiAoZnVuY3MubGVuZ3RoID09PSAwKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uIChhcmcpIHtcbiAgICAgIHJldHVybiBhcmc7XG4gICAgfTtcbiAgfSBlbHNlIHtcbiAgICB2YXIgX3JldCA9IGZ1bmN0aW9uICgpIHtcbiAgICAgIHZhciBsYXN0ID0gZnVuY3NbZnVuY3MubGVuZ3RoIC0gMV07XG4gICAgICB2YXIgcmVzdCA9IGZ1bmNzLnNsaWNlKDAsIC0xKTtcbiAgICAgIHJldHVybiB7XG4gICAgICAgIHY6IGZ1bmN0aW9uIHYoKSB7XG4gICAgICAgICAgcmV0dXJuIHJlc3QucmVkdWNlUmlnaHQoZnVuY3Rpb24gKGNvbXBvc2VkLCBmKSB7XG4gICAgICAgICAgICByZXR1cm4gZihjb21wb3NlZCk7XG4gICAgICAgICAgfSwgbGFzdC5hcHBseSh1bmRlZmluZWQsIGFyZ3VtZW50cykpO1xuICAgICAgICB9XG4gICAgICB9O1xuICAgIH0oKTtcblxuICAgIGlmICh0eXBlb2YgX3JldCA9PT0gXCJvYmplY3RcIikgcmV0dXJuIF9yZXQudjtcbiAgfVxufSIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHMuQWN0aW9uVHlwZXMgPSB1bmRlZmluZWQ7XG5leHBvcnRzW1wiZGVmYXVsdFwiXSA9IGNyZWF0ZVN0b3JlO1xuXG52YXIgX2lzUGxhaW5PYmplY3QgPSByZXF1aXJlKCdsb2Rhc2gvaXNQbGFpbk9iamVjdCcpO1xuXG52YXIgX2lzUGxhaW5PYmplY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfaXNQbGFpbk9iamVjdCk7XG5cbnZhciBfc3ltYm9sT2JzZXJ2YWJsZSA9IHJlcXVpcmUoJ3N5bWJvbC1vYnNlcnZhYmxlJyk7XG5cbnZhciBfc3ltYm9sT2JzZXJ2YWJsZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zeW1ib2xPYnNlcnZhYmxlKTtcblxuZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHsgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHsgXCJkZWZhdWx0XCI6IG9iaiB9OyB9XG5cbi8qKlxuICogVGhlc2UgYXJlIHByaXZhdGUgYWN0aW9uIHR5cGVzIHJlc2VydmVkIGJ5IFJlZHV4LlxuICogRm9yIGFueSB1bmtub3duIGFjdGlvbnMsIHlvdSBtdXN0IHJldHVybiB0aGUgY3VycmVudCBzdGF0ZS5cbiAqIElmIHRoZSBjdXJyZW50IHN0YXRlIGlzIHVuZGVmaW5lZCwgeW91IG11c3QgcmV0dXJuIHRoZSBpbml0aWFsIHN0YXRlLlxuICogRG8gbm90IHJlZmVyZW5jZSB0aGVzZSBhY3Rpb24gdHlwZXMgZGlyZWN0bHkgaW4geW91ciBjb2RlLlxuICovXG52YXIgQWN0aW9uVHlwZXMgPSBleHBvcnRzLkFjdGlvblR5cGVzID0ge1xuICBJTklUOiAnQEByZWR1eC9JTklUJ1xufTtcblxuLyoqXG4gKiBDcmVhdGVzIGEgUmVkdXggc3RvcmUgdGhhdCBob2xkcyB0aGUgc3RhdGUgdHJlZS5cbiAqIFRoZSBvbmx5IHdheSB0byBjaGFuZ2UgdGhlIGRhdGEgaW4gdGhlIHN0b3JlIGlzIHRvIGNhbGwgYGRpc3BhdGNoKClgIG9uIGl0LlxuICpcbiAqIFRoZXJlIHNob3VsZCBvbmx5IGJlIGEgc2luZ2xlIHN0b3JlIGluIHlvdXIgYXBwLiBUbyBzcGVjaWZ5IGhvdyBkaWZmZXJlbnRcbiAqIHBhcnRzIG9mIHRoZSBzdGF0ZSB0cmVlIHJlc3BvbmQgdG8gYWN0aW9ucywgeW91IG1heSBjb21iaW5lIHNldmVyYWwgcmVkdWNlcnNcbiAqIGludG8gYSBzaW5nbGUgcmVkdWNlciBmdW5jdGlvbiBieSB1c2luZyBgY29tYmluZVJlZHVjZXJzYC5cbiAqXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSByZWR1Y2VyIEEgZnVuY3Rpb24gdGhhdCByZXR1cm5zIHRoZSBuZXh0IHN0YXRlIHRyZWUsIGdpdmVuXG4gKiB0aGUgY3VycmVudCBzdGF0ZSB0cmVlIGFuZCB0aGUgYWN0aW9uIHRvIGhhbmRsZS5cbiAqXG4gKiBAcGFyYW0ge2FueX0gW2luaXRpYWxTdGF0ZV0gVGhlIGluaXRpYWwgc3RhdGUuIFlvdSBtYXkgb3B0aW9uYWxseSBzcGVjaWZ5IGl0XG4gKiB0byBoeWRyYXRlIHRoZSBzdGF0ZSBmcm9tIHRoZSBzZXJ2ZXIgaW4gdW5pdmVyc2FsIGFwcHMsIG9yIHRvIHJlc3RvcmUgYVxuICogcHJldmlvdXNseSBzZXJpYWxpemVkIHVzZXIgc2Vzc2lvbi5cbiAqIElmIHlvdSB1c2UgYGNvbWJpbmVSZWR1Y2Vyc2AgdG8gcHJvZHVjZSB0aGUgcm9vdCByZWR1Y2VyIGZ1bmN0aW9uLCB0aGlzIG11c3QgYmVcbiAqIGFuIG9iamVjdCB3aXRoIHRoZSBzYW1lIHNoYXBlIGFzIGBjb21iaW5lUmVkdWNlcnNgIGtleXMuXG4gKlxuICogQHBhcmFtIHtGdW5jdGlvbn0gZW5oYW5jZXIgVGhlIHN0b3JlIGVuaGFuY2VyLiBZb3UgbWF5IG9wdGlvbmFsbHkgc3BlY2lmeSBpdFxuICogdG8gZW5oYW5jZSB0aGUgc3RvcmUgd2l0aCB0aGlyZC1wYXJ0eSBjYXBhYmlsaXRpZXMgc3VjaCBhcyBtaWRkbGV3YXJlLFxuICogdGltZSB0cmF2ZWwsIHBlcnNpc3RlbmNlLCBldGMuIFRoZSBvbmx5IHN0b3JlIGVuaGFuY2VyIHRoYXQgc2hpcHMgd2l0aCBSZWR1eFxuICogaXMgYGFwcGx5TWlkZGxld2FyZSgpYC5cbiAqXG4gKiBAcmV0dXJucyB7U3RvcmV9IEEgUmVkdXggc3RvcmUgdGhhdCBsZXRzIHlvdSByZWFkIHRoZSBzdGF0ZSwgZGlzcGF0Y2ggYWN0aW9uc1xuICogYW5kIHN1YnNjcmliZSB0byBjaGFuZ2VzLlxuICovXG5mdW5jdGlvbiBjcmVhdGVTdG9yZShyZWR1Y2VyLCBpbml0aWFsU3RhdGUsIGVuaGFuY2VyKSB7XG4gIHZhciBfcmVmMjtcblxuICBpZiAodHlwZW9mIGluaXRpYWxTdGF0ZSA9PT0gJ2Z1bmN0aW9uJyAmJiB0eXBlb2YgZW5oYW5jZXIgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgZW5oYW5jZXIgPSBpbml0aWFsU3RhdGU7XG4gICAgaW5pdGlhbFN0YXRlID0gdW5kZWZpbmVkO1xuICB9XG5cbiAgaWYgKHR5cGVvZiBlbmhhbmNlciAhPT0gJ3VuZGVmaW5lZCcpIHtcbiAgICBpZiAodHlwZW9mIGVuaGFuY2VyICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0V4cGVjdGVkIHRoZSBlbmhhbmNlciB0byBiZSBhIGZ1bmN0aW9uLicpO1xuICAgIH1cblxuICAgIHJldHVybiBlbmhhbmNlcihjcmVhdGVTdG9yZSkocmVkdWNlciwgaW5pdGlhbFN0YXRlKTtcbiAgfVxuXG4gIGlmICh0eXBlb2YgcmVkdWNlciAhPT0gJ2Z1bmN0aW9uJykge1xuICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgdGhlIHJlZHVjZXIgdG8gYmUgYSBmdW5jdGlvbi4nKTtcbiAgfVxuXG4gIHZhciBjdXJyZW50UmVkdWNlciA9IHJlZHVjZXI7XG4gIHZhciBjdXJyZW50U3RhdGUgPSBpbml0aWFsU3RhdGU7XG4gIHZhciBjdXJyZW50TGlzdGVuZXJzID0gW107XG4gIHZhciBuZXh0TGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycztcbiAgdmFyIGlzRGlzcGF0Y2hpbmcgPSBmYWxzZTtcblxuICBmdW5jdGlvbiBlbnN1cmVDYW5NdXRhdGVOZXh0TGlzdGVuZXJzKCkge1xuICAgIGlmIChuZXh0TGlzdGVuZXJzID09PSBjdXJyZW50TGlzdGVuZXJzKSB7XG4gICAgICBuZXh0TGlzdGVuZXJzID0gY3VycmVudExpc3RlbmVycy5zbGljZSgpO1xuICAgIH1cbiAgfVxuXG4gIC8qKlxuICAgKiBSZWFkcyB0aGUgc3RhdGUgdHJlZSBtYW5hZ2VkIGJ5IHRoZSBzdG9yZS5cbiAgICpcbiAgICogQHJldHVybnMge2FueX0gVGhlIGN1cnJlbnQgc3RhdGUgdHJlZSBvZiB5b3VyIGFwcGxpY2F0aW9uLlxuICAgKi9cbiAgZnVuY3Rpb24gZ2V0U3RhdGUoKSB7XG4gICAgcmV0dXJuIGN1cnJlbnRTdGF0ZTtcbiAgfVxuXG4gIC8qKlxuICAgKiBBZGRzIGEgY2hhbmdlIGxpc3RlbmVyLiBJdCB3aWxsIGJlIGNhbGxlZCBhbnkgdGltZSBhbiBhY3Rpb24gaXMgZGlzcGF0Y2hlZCxcbiAgICogYW5kIHNvbWUgcGFydCBvZiB0aGUgc3RhdGUgdHJlZSBtYXkgcG90ZW50aWFsbHkgaGF2ZSBjaGFuZ2VkLiBZb3UgbWF5IHRoZW5cbiAgICogY2FsbCBgZ2V0U3RhdGUoKWAgdG8gcmVhZCB0aGUgY3VycmVudCBzdGF0ZSB0cmVlIGluc2lkZSB0aGUgY2FsbGJhY2suXG4gICAqXG4gICAqIFlvdSBtYXkgY2FsbCBgZGlzcGF0Y2goKWAgZnJvbSBhIGNoYW5nZSBsaXN0ZW5lciwgd2l0aCB0aGUgZm9sbG93aW5nXG4gICAqIGNhdmVhdHM6XG4gICAqXG4gICAqIDEuIFRoZSBzdWJzY3JpcHRpb25zIGFyZSBzbmFwc2hvdHRlZCBqdXN0IGJlZm9yZSBldmVyeSBgZGlzcGF0Y2goKWAgY2FsbC5cbiAgICogSWYgeW91IHN1YnNjcmliZSBvciB1bnN1YnNjcmliZSB3aGlsZSB0aGUgbGlzdGVuZXJzIGFyZSBiZWluZyBpbnZva2VkLCB0aGlzXG4gICAqIHdpbGwgbm90IGhhdmUgYW55IGVmZmVjdCBvbiB0aGUgYGRpc3BhdGNoKClgIHRoYXQgaXMgY3VycmVudGx5IGluIHByb2dyZXNzLlxuICAgKiBIb3dldmVyLCB0aGUgbmV4dCBgZGlzcGF0Y2goKWAgY2FsbCwgd2hldGhlciBuZXN0ZWQgb3Igbm90LCB3aWxsIHVzZSBhIG1vcmVcbiAgICogcmVjZW50IHNuYXBzaG90IG9mIHRoZSBzdWJzY3JpcHRpb24gbGlzdC5cbiAgICpcbiAgICogMi4gVGhlIGxpc3RlbmVyIHNob3VsZCBub3QgZXhwZWN0IHRvIHNlZSBhbGwgc3RhdGUgY2hhbmdlcywgYXMgdGhlIHN0YXRlXG4gICAqIG1pZ2h0IGhhdmUgYmVlbiB1cGRhdGVkIG11bHRpcGxlIHRpbWVzIGR1cmluZyBhIG5lc3RlZCBgZGlzcGF0Y2goKWAgYmVmb3JlXG4gICAqIHRoZSBsaXN0ZW5lciBpcyBjYWxsZWQuIEl0IGlzLCBob3dldmVyLCBndWFyYW50ZWVkIHRoYXQgYWxsIHN1YnNjcmliZXJzXG4gICAqIHJlZ2lzdGVyZWQgYmVmb3JlIHRoZSBgZGlzcGF0Y2goKWAgc3RhcnRlZCB3aWxsIGJlIGNhbGxlZCB3aXRoIHRoZSBsYXRlc3RcbiAgICogc3RhdGUgYnkgdGhlIHRpbWUgaXQgZXhpdHMuXG4gICAqXG4gICAqIEBwYXJhbSB7RnVuY3Rpb259IGxpc3RlbmVyIEEgY2FsbGJhY2sgdG8gYmUgaW52b2tlZCBvbiBldmVyeSBkaXNwYXRjaC5cbiAgICogQHJldHVybnMge0Z1bmN0aW9ufSBBIGZ1bmN0aW9uIHRvIHJlbW92ZSB0aGlzIGNoYW5nZSBsaXN0ZW5lci5cbiAgICovXG4gIGZ1bmN0aW9uIHN1YnNjcmliZShsaXN0ZW5lcikge1xuICAgIGlmICh0eXBlb2YgbGlzdGVuZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgbGlzdGVuZXIgdG8gYmUgYSBmdW5jdGlvbi4nKTtcbiAgICB9XG5cbiAgICB2YXIgaXNTdWJzY3JpYmVkID0gdHJ1ZTtcblxuICAgIGVuc3VyZUNhbk11dGF0ZU5leHRMaXN0ZW5lcnMoKTtcbiAgICBuZXh0TGlzdGVuZXJzLnB1c2gobGlzdGVuZXIpO1xuXG4gICAgcmV0dXJuIGZ1bmN0aW9uIHVuc3Vic2NyaWJlKCkge1xuICAgICAgaWYgKCFpc1N1YnNjcmliZWQpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuXG4gICAgICBpc1N1YnNjcmliZWQgPSBmYWxzZTtcblxuICAgICAgZW5zdXJlQ2FuTXV0YXRlTmV4dExpc3RlbmVycygpO1xuICAgICAgdmFyIGluZGV4ID0gbmV4dExpc3RlbmVycy5pbmRleE9mKGxpc3RlbmVyKTtcbiAgICAgIG5leHRMaXN0ZW5lcnMuc3BsaWNlKGluZGV4LCAxKTtcbiAgICB9O1xuICB9XG5cbiAgLyoqXG4gICAqIERpc3BhdGNoZXMgYW4gYWN0aW9uLiBJdCBpcyB0aGUgb25seSB3YXkgdG8gdHJpZ2dlciBhIHN0YXRlIGNoYW5nZS5cbiAgICpcbiAgICogVGhlIGByZWR1Y2VyYCBmdW5jdGlvbiwgdXNlZCB0byBjcmVhdGUgdGhlIHN0b3JlLCB3aWxsIGJlIGNhbGxlZCB3aXRoIHRoZVxuICAgKiBjdXJyZW50IHN0YXRlIHRyZWUgYW5kIHRoZSBnaXZlbiBgYWN0aW9uYC4gSXRzIHJldHVybiB2YWx1ZSB3aWxsXG4gICAqIGJlIGNvbnNpZGVyZWQgdGhlICoqbmV4dCoqIHN0YXRlIG9mIHRoZSB0cmVlLCBhbmQgdGhlIGNoYW5nZSBsaXN0ZW5lcnNcbiAgICogd2lsbCBiZSBub3RpZmllZC5cbiAgICpcbiAgICogVGhlIGJhc2UgaW1wbGVtZW50YXRpb24gb25seSBzdXBwb3J0cyBwbGFpbiBvYmplY3QgYWN0aW9ucy4gSWYgeW91IHdhbnQgdG9cbiAgICogZGlzcGF0Y2ggYSBQcm9taXNlLCBhbiBPYnNlcnZhYmxlLCBhIHRodW5rLCBvciBzb21ldGhpbmcgZWxzZSwgeW91IG5lZWQgdG9cbiAgICogd3JhcCB5b3VyIHN0b3JlIGNyZWF0aW5nIGZ1bmN0aW9uIGludG8gdGhlIGNvcnJlc3BvbmRpbmcgbWlkZGxld2FyZS4gRm9yXG4gICAqIGV4YW1wbGUsIHNlZSB0aGUgZG9jdW1lbnRhdGlvbiBmb3IgdGhlIGByZWR1eC10aHVua2AgcGFja2FnZS4gRXZlbiB0aGVcbiAgICogbWlkZGxld2FyZSB3aWxsIGV2ZW50dWFsbHkgZGlzcGF0Y2ggcGxhaW4gb2JqZWN0IGFjdGlvbnMgdXNpbmcgdGhpcyBtZXRob2QuXG4gICAqXG4gICAqIEBwYXJhbSB7T2JqZWN0fSBhY3Rpb24gQSBwbGFpbiBvYmplY3QgcmVwcmVzZW50aW5nIOKAnHdoYXQgY2hhbmdlZOKAnS4gSXQgaXNcbiAgICogYSBnb29kIGlkZWEgdG8ga2VlcCBhY3Rpb25zIHNlcmlhbGl6YWJsZSBzbyB5b3UgY2FuIHJlY29yZCBhbmQgcmVwbGF5IHVzZXJcbiAgICogc2Vzc2lvbnMsIG9yIHVzZSB0aGUgdGltZSB0cmF2ZWxsaW5nIGByZWR1eC1kZXZ0b29sc2AuIEFuIGFjdGlvbiBtdXN0IGhhdmVcbiAgICogYSBgdHlwZWAgcHJvcGVydHkgd2hpY2ggbWF5IG5vdCBiZSBgdW5kZWZpbmVkYC4gSXQgaXMgYSBnb29kIGlkZWEgdG8gdXNlXG4gICAqIHN0cmluZyBjb25zdGFudHMgZm9yIGFjdGlvbiB0eXBlcy5cbiAgICpcbiAgICogQHJldHVybnMge09iamVjdH0gRm9yIGNvbnZlbmllbmNlLCB0aGUgc2FtZSBhY3Rpb24gb2JqZWN0IHlvdSBkaXNwYXRjaGVkLlxuICAgKlxuICAgKiBOb3RlIHRoYXQsIGlmIHlvdSB1c2UgYSBjdXN0b20gbWlkZGxld2FyZSwgaXQgbWF5IHdyYXAgYGRpc3BhdGNoKClgIHRvXG4gICAqIHJldHVybiBzb21ldGhpbmcgZWxzZSAoZm9yIGV4YW1wbGUsIGEgUHJvbWlzZSB5b3UgY2FuIGF3YWl0KS5cbiAgICovXG4gIGZ1bmN0aW9uIGRpc3BhdGNoKGFjdGlvbikge1xuICAgIGlmICghKDAsIF9pc1BsYWluT2JqZWN0MltcImRlZmF1bHRcIl0pKGFjdGlvbikpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignQWN0aW9ucyBtdXN0IGJlIHBsYWluIG9iamVjdHMuICcgKyAnVXNlIGN1c3RvbSBtaWRkbGV3YXJlIGZvciBhc3luYyBhY3Rpb25zLicpO1xuICAgIH1cblxuICAgIGlmICh0eXBlb2YgYWN0aW9uLnR5cGUgPT09ICd1bmRlZmluZWQnKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ0FjdGlvbnMgbWF5IG5vdCBoYXZlIGFuIHVuZGVmaW5lZCBcInR5cGVcIiBwcm9wZXJ0eS4gJyArICdIYXZlIHlvdSBtaXNzcGVsbGVkIGEgY29uc3RhbnQ/Jyk7XG4gICAgfVxuXG4gICAgaWYgKGlzRGlzcGF0Y2hpbmcpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignUmVkdWNlcnMgbWF5IG5vdCBkaXNwYXRjaCBhY3Rpb25zLicpO1xuICAgIH1cblxuICAgIHRyeSB7XG4gICAgICBpc0Rpc3BhdGNoaW5nID0gdHJ1ZTtcbiAgICAgIGN1cnJlbnRTdGF0ZSA9IGN1cnJlbnRSZWR1Y2VyKGN1cnJlbnRTdGF0ZSwgYWN0aW9uKTtcbiAgICB9IGZpbmFsbHkge1xuICAgICAgaXNEaXNwYXRjaGluZyA9IGZhbHNlO1xuICAgIH1cblxuICAgIHZhciBsaXN0ZW5lcnMgPSBjdXJyZW50TGlzdGVuZXJzID0gbmV4dExpc3RlbmVycztcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGxpc3RlbmVycy5sZW5ndGg7IGkrKykge1xuICAgICAgbGlzdGVuZXJzW2ldKCk7XG4gICAgfVxuXG4gICAgcmV0dXJuIGFjdGlvbjtcbiAgfVxuXG4gIC8qKlxuICAgKiBSZXBsYWNlcyB0aGUgcmVkdWNlciBjdXJyZW50bHkgdXNlZCBieSB0aGUgc3RvcmUgdG8gY2FsY3VsYXRlIHRoZSBzdGF0ZS5cbiAgICpcbiAgICogWW91IG1pZ2h0IG5lZWQgdGhpcyBpZiB5b3VyIGFwcCBpbXBsZW1lbnRzIGNvZGUgc3BsaXR0aW5nIGFuZCB5b3Ugd2FudCB0b1xuICAgKiBsb2FkIHNvbWUgb2YgdGhlIHJlZHVjZXJzIGR5bmFtaWNhbGx5LiBZb3UgbWlnaHQgYWxzbyBuZWVkIHRoaXMgaWYgeW91XG4gICAqIGltcGxlbWVudCBhIGhvdCByZWxvYWRpbmcgbWVjaGFuaXNtIGZvciBSZWR1eC5cbiAgICpcbiAgICogQHBhcmFtIHtGdW5jdGlvbn0gbmV4dFJlZHVjZXIgVGhlIHJlZHVjZXIgZm9yIHRoZSBzdG9yZSB0byB1c2UgaW5zdGVhZC5cbiAgICogQHJldHVybnMge3ZvaWR9XG4gICAqL1xuICBmdW5jdGlvbiByZXBsYWNlUmVkdWNlcihuZXh0UmVkdWNlcikge1xuICAgIGlmICh0eXBlb2YgbmV4dFJlZHVjZXIgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignRXhwZWN0ZWQgdGhlIG5leHRSZWR1Y2VyIHRvIGJlIGEgZnVuY3Rpb24uJyk7XG4gICAgfVxuXG4gICAgY3VycmVudFJlZHVjZXIgPSBuZXh0UmVkdWNlcjtcbiAgICBkaXNwYXRjaCh7IHR5cGU6IEFjdGlvblR5cGVzLklOSVQgfSk7XG4gIH1cblxuICAvKipcbiAgICogSW50ZXJvcGVyYWJpbGl0eSBwb2ludCBmb3Igb2JzZXJ2YWJsZS9yZWFjdGl2ZSBsaWJyYXJpZXMuXG4gICAqIEByZXR1cm5zIHtvYnNlcnZhYmxlfSBBIG1pbmltYWwgb2JzZXJ2YWJsZSBvZiBzdGF0ZSBjaGFuZ2VzLlxuICAgKiBGb3IgbW9yZSBpbmZvcm1hdGlvbiwgc2VlIHRoZSBvYnNlcnZhYmxlIHByb3Bvc2FsOlxuICAgKiBodHRwczovL2dpdGh1Yi5jb20vemVucGFyc2luZy9lcy1vYnNlcnZhYmxlXG4gICAqL1xuICBmdW5jdGlvbiBvYnNlcnZhYmxlKCkge1xuICAgIHZhciBfcmVmO1xuXG4gICAgdmFyIG91dGVyU3Vic2NyaWJlID0gc3Vic2NyaWJlO1xuICAgIHJldHVybiBfcmVmID0ge1xuICAgICAgLyoqXG4gICAgICAgKiBUaGUgbWluaW1hbCBvYnNlcnZhYmxlIHN1YnNjcmlwdGlvbiBtZXRob2QuXG4gICAgICAgKiBAcGFyYW0ge09iamVjdH0gb2JzZXJ2ZXIgQW55IG9iamVjdCB0aGF0IGNhbiBiZSB1c2VkIGFzIGFuIG9ic2VydmVyLlxuICAgICAgICogVGhlIG9ic2VydmVyIG9iamVjdCBzaG91bGQgaGF2ZSBhIGBuZXh0YCBtZXRob2QuXG4gICAgICAgKiBAcmV0dXJucyB7c3Vic2NyaXB0aW9ufSBBbiBvYmplY3Qgd2l0aCBhbiBgdW5zdWJzY3JpYmVgIG1ldGhvZCB0aGF0IGNhblxuICAgICAgICogYmUgdXNlZCB0byB1bnN1YnNjcmliZSB0aGUgb2JzZXJ2YWJsZSBmcm9tIHRoZSBzdG9yZSwgYW5kIHByZXZlbnQgZnVydGhlclxuICAgICAgICogZW1pc3Npb24gb2YgdmFsdWVzIGZyb20gdGhlIG9ic2VydmFibGUuXG4gICAgICAgKi9cblxuICAgICAgc3Vic2NyaWJlOiBmdW5jdGlvbiBzdWJzY3JpYmUob2JzZXJ2ZXIpIHtcbiAgICAgICAgaWYgKHR5cGVvZiBvYnNlcnZlciAhPT0gJ29iamVjdCcpIHtcbiAgICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdFeHBlY3RlZCB0aGUgb2JzZXJ2ZXIgdG8gYmUgYW4gb2JqZWN0LicpO1xuICAgICAgICB9XG5cbiAgICAgICAgZnVuY3Rpb24gb2JzZXJ2ZVN0YXRlKCkge1xuICAgICAgICAgIGlmIChvYnNlcnZlci5uZXh0KSB7XG4gICAgICAgICAgICBvYnNlcnZlci5uZXh0KGdldFN0YXRlKCkpO1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIG9ic2VydmVTdGF0ZSgpO1xuICAgICAgICB2YXIgdW5zdWJzY3JpYmUgPSBvdXRlclN1YnNjcmliZShvYnNlcnZlU3RhdGUpO1xuICAgICAgICByZXR1cm4geyB1bnN1YnNjcmliZTogdW5zdWJzY3JpYmUgfTtcbiAgICAgIH1cbiAgICB9LCBfcmVmW19zeW1ib2xPYnNlcnZhYmxlMltcImRlZmF1bHRcIl1dID0gZnVuY3Rpb24gKCkge1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfSwgX3JlZjtcbiAgfVxuXG4gIC8vIFdoZW4gYSBzdG9yZSBpcyBjcmVhdGVkLCBhbiBcIklOSVRcIiBhY3Rpb24gaXMgZGlzcGF0Y2hlZCBzbyB0aGF0IGV2ZXJ5XG4gIC8vIHJlZHVjZXIgcmV0dXJucyB0aGVpciBpbml0aWFsIHN0YXRlLiBUaGlzIGVmZmVjdGl2ZWx5IHBvcHVsYXRlc1xuICAvLyB0aGUgaW5pdGlhbCBzdGF0ZSB0cmVlLlxuICBkaXNwYXRjaCh7IHR5cGU6IEFjdGlvblR5cGVzLklOSVQgfSk7XG5cbiAgcmV0dXJuIF9yZWYyID0ge1xuICAgIGRpc3BhdGNoOiBkaXNwYXRjaCxcbiAgICBzdWJzY3JpYmU6IHN1YnNjcmliZSxcbiAgICBnZXRTdGF0ZTogZ2V0U3RhdGUsXG4gICAgcmVwbGFjZVJlZHVjZXI6IHJlcGxhY2VSZWR1Y2VyXG4gIH0sIF9yZWYyW19zeW1ib2xPYnNlcnZhYmxlMltcImRlZmF1bHRcIl1dID0gb2JzZXJ2YWJsZSwgX3JlZjI7XG59IiwiJ3VzZSBzdHJpY3QnO1xuXG5leHBvcnRzLl9fZXNNb2R1bGUgPSB0cnVlO1xuZXhwb3J0cy5jb21wb3NlID0gZXhwb3J0cy5hcHBseU1pZGRsZXdhcmUgPSBleHBvcnRzLmJpbmRBY3Rpb25DcmVhdG9ycyA9IGV4cG9ydHMuY29tYmluZVJlZHVjZXJzID0gZXhwb3J0cy5jcmVhdGVTdG9yZSA9IHVuZGVmaW5lZDtcblxudmFyIF9jcmVhdGVTdG9yZSA9IHJlcXVpcmUoJy4vY3JlYXRlU3RvcmUnKTtcblxudmFyIF9jcmVhdGVTdG9yZTIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jcmVhdGVTdG9yZSk7XG5cbnZhciBfY29tYmluZVJlZHVjZXJzID0gcmVxdWlyZSgnLi9jb21iaW5lUmVkdWNlcnMnKTtcblxudmFyIF9jb21iaW5lUmVkdWNlcnMyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29tYmluZVJlZHVjZXJzKTtcblxudmFyIF9iaW5kQWN0aW9uQ3JlYXRvcnMgPSByZXF1aXJlKCcuL2JpbmRBY3Rpb25DcmVhdG9ycycpO1xuXG52YXIgX2JpbmRBY3Rpb25DcmVhdG9yczIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9iaW5kQWN0aW9uQ3JlYXRvcnMpO1xuXG52YXIgX2FwcGx5TWlkZGxld2FyZSA9IHJlcXVpcmUoJy4vYXBwbHlNaWRkbGV3YXJlJyk7XG5cbnZhciBfYXBwbHlNaWRkbGV3YXJlMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2FwcGx5TWlkZGxld2FyZSk7XG5cbnZhciBfY29tcG9zZSA9IHJlcXVpcmUoJy4vY29tcG9zZScpO1xuXG52YXIgX2NvbXBvc2UyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY29tcG9zZSk7XG5cbnZhciBfd2FybmluZyA9IHJlcXVpcmUoJy4vdXRpbHMvd2FybmluZycpO1xuXG52YXIgX3dhcm5pbmcyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfd2FybmluZyk7XG5cbmZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7IHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7IFwiZGVmYXVsdFwiOiBvYmogfTsgfVxuXG4vKlxuKiBUaGlzIGlzIGEgZHVtbXkgZnVuY3Rpb24gdG8gY2hlY2sgaWYgdGhlIGZ1bmN0aW9uIG5hbWUgaGFzIGJlZW4gYWx0ZXJlZCBieSBtaW5pZmljYXRpb24uXG4qIElmIHRoZSBmdW5jdGlvbiBoYXMgYmVlbiBtaW5pZmllZCBhbmQgTk9ERV9FTlYgIT09ICdwcm9kdWN0aW9uJywgd2FybiB0aGUgdXNlci5cbiovXG5mdW5jdGlvbiBpc0NydXNoZWQoKSB7fVxuXG5pZiAoXCJwcm9kdWN0aW9uXCIgIT09ICdwcm9kdWN0aW9uJyAmJiB0eXBlb2YgaXNDcnVzaGVkLm5hbWUgPT09ICdzdHJpbmcnICYmIGlzQ3J1c2hlZC5uYW1lICE9PSAnaXNDcnVzaGVkJykge1xuICAoMCwgX3dhcm5pbmcyW1wiZGVmYXVsdFwiXSkoJ1lvdSBhcmUgY3VycmVudGx5IHVzaW5nIG1pbmlmaWVkIGNvZGUgb3V0c2lkZSBvZiBOT0RFX0VOViA9PT0gXFwncHJvZHVjdGlvblxcJy4gJyArICdUaGlzIG1lYW5zIHRoYXQgeW91IGFyZSBydW5uaW5nIGEgc2xvd2VyIGRldmVsb3BtZW50IGJ1aWxkIG9mIFJlZHV4LiAnICsgJ1lvdSBjYW4gdXNlIGxvb3NlLWVudmlmeSAoaHR0cHM6Ly9naXRodWIuY29tL3plcnRvc2gvbG9vc2UtZW52aWZ5KSBmb3IgYnJvd3NlcmlmeSAnICsgJ29yIERlZmluZVBsdWdpbiBmb3Igd2VicGFjayAoaHR0cDovL3N0YWNrb3ZlcmZsb3cuY29tL3F1ZXN0aW9ucy8zMDAzMDAzMSkgJyArICd0byBlbnN1cmUgeW91IGhhdmUgdGhlIGNvcnJlY3QgY29kZSBmb3IgeW91ciBwcm9kdWN0aW9uIGJ1aWxkLicpO1xufVxuXG5leHBvcnRzLmNyZWF0ZVN0b3JlID0gX2NyZWF0ZVN0b3JlMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmNvbWJpbmVSZWR1Y2VycyA9IF9jb21iaW5lUmVkdWNlcnMyW1wiZGVmYXVsdFwiXTtcbmV4cG9ydHMuYmluZEFjdGlvbkNyZWF0b3JzID0gX2JpbmRBY3Rpb25DcmVhdG9yczJbXCJkZWZhdWx0XCJdO1xuZXhwb3J0cy5hcHBseU1pZGRsZXdhcmUgPSBfYXBwbHlNaWRkbGV3YXJlMltcImRlZmF1bHRcIl07XG5leHBvcnRzLmNvbXBvc2UgPSBfY29tcG9zZTJbXCJkZWZhdWx0XCJdOyIsIid1c2Ugc3RyaWN0JztcblxuZXhwb3J0cy5fX2VzTW9kdWxlID0gdHJ1ZTtcbmV4cG9ydHNbXCJkZWZhdWx0XCJdID0gd2FybmluZztcbi8qKlxuICogUHJpbnRzIGEgd2FybmluZyBpbiB0aGUgY29uc29sZSBpZiBpdCBleGlzdHMuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd9IG1lc3NhZ2UgVGhlIHdhcm5pbmcgbWVzc2FnZS5cbiAqIEByZXR1cm5zIHt2b2lkfVxuICovXG5mdW5jdGlvbiB3YXJuaW5nKG1lc3NhZ2UpIHtcbiAgLyogZXNsaW50LWRpc2FibGUgbm8tY29uc29sZSAqL1xuICBpZiAodHlwZW9mIGNvbnNvbGUgIT09ICd1bmRlZmluZWQnICYmIHR5cGVvZiBjb25zb2xlLmVycm9yID09PSAnZnVuY3Rpb24nKSB7XG4gICAgY29uc29sZS5lcnJvcihtZXNzYWdlKTtcbiAgfVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWNvbnNvbGUgKi9cbiAgdHJ5IHtcbiAgICAvLyBUaGlzIGVycm9yIHdhcyB0aHJvd24gYXMgYSBjb252ZW5pZW5jZSBzbyB0aGF0IGlmIHlvdSBlbmFibGVcbiAgICAvLyBcImJyZWFrIG9uIGFsbCBleGNlcHRpb25zXCIgaW4geW91ciBjb25zb2xlLFxuICAgIC8vIGl0IHdvdWxkIHBhdXNlIHRoZSBleGVjdXRpb24gYXQgdGhpcyBsaW5lLlxuICAgIHRocm93IG5ldyBFcnJvcihtZXNzYWdlKTtcbiAgICAvKiBlc2xpbnQtZGlzYWJsZSBuby1lbXB0eSAqL1xuICB9IGNhdGNoIChlKSB7fVxuICAvKiBlc2xpbnQtZW5hYmxlIG5vLWVtcHR5ICovXG59IiwiLyogZ2xvYmFsIHdpbmRvdyAqL1xuJ3VzZSBzdHJpY3QnO1xuXG5tb2R1bGUuZXhwb3J0cyA9IHJlcXVpcmUoJy4vcG9ueWZpbGwnKShnbG9iYWwgfHwgd2luZG93IHx8IHRoaXMpO1xuIiwiJ3VzZSBzdHJpY3QnO1xuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uIHN5bWJvbE9ic2VydmFibGVQb255ZmlsbChyb290KSB7XG5cdHZhciByZXN1bHQ7XG5cdHZhciBTeW1ib2wgPSByb290LlN5bWJvbDtcblxuXHRpZiAodHlwZW9mIFN5bWJvbCA9PT0gJ2Z1bmN0aW9uJykge1xuXHRcdGlmIChTeW1ib2wub2JzZXJ2YWJsZSkge1xuXHRcdFx0cmVzdWx0ID0gU3ltYm9sLm9ic2VydmFibGU7XG5cdFx0fSBlbHNlIHtcblx0XHRcdHJlc3VsdCA9IFN5bWJvbCgnb2JzZXJ2YWJsZScpO1xuXHRcdFx0U3ltYm9sLm9ic2VydmFibGUgPSByZXN1bHQ7XG5cdFx0fVxuXHR9IGVsc2Uge1xuXHRcdHJlc3VsdCA9ICdAQG9ic2VydmFibGUnO1xuXHR9XG5cblx0cmV0dXJuIHJlc3VsdDtcbn07XG4iLCJcbmV4cG9ydHMgPSBtb2R1bGUuZXhwb3J0cyA9IHRyaW07XG5cbmZ1bmN0aW9uIHRyaW0oc3RyKXtcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9eXFxzKnxcXHMqJC9nLCAnJyk7XG59XG5cbmV4cG9ydHMubGVmdCA9IGZ1bmN0aW9uKHN0cil7XG4gIHJldHVybiBzdHIucmVwbGFjZSgvXlxccyovLCAnJyk7XG59O1xuXG5leHBvcnRzLnJpZ2h0ID0gZnVuY3Rpb24oc3RyKXtcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9cXHMqJC8sICcnKTtcbn07XG4iLCJ2YXIgd2luZG93ICAgICAgICAgICAgICA9IHJlcXVpcmUoJ2dsb2JhbCcpO1xudmFyIE1vY2tYTUxIdHRwUmVxdWVzdCAgPSByZXF1aXJlKCcuL2xpYi9Nb2NrWE1MSHR0cFJlcXVlc3QnKTtcbnZhciByZWFsICAgICAgICAgICAgICAgID0gd2luZG93LlhNTEh0dHBSZXF1ZXN0O1xudmFyIG1vY2sgICAgICAgICAgICAgICAgPSBNb2NrWE1MSHR0cFJlcXVlc3Q7XG5cbi8qKlxuICogTW9jayB1dGlsaXR5XG4gKi9cbm1vZHVsZS5leHBvcnRzID0ge1xuXG5cdFhNTEh0dHBSZXF1ZXN0OiBNb2NrWE1MSHR0cFJlcXVlc3QsXG5cblx0LyoqXG5cdCAqIFJlcGxhY2UgdGhlIG5hdGl2ZSBYSFIgd2l0aCB0aGUgbW9ja2VkIFhIUlxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHNldHVwOiBmdW5jdGlvbigpIHtcblx0XHR3aW5kb3cuWE1MSHR0cFJlcXVlc3QgPSBtb2NrO1xuXHRcdE1vY2tYTUxIdHRwUmVxdWVzdC5oYW5kbGVycyA9IFtdO1xuXHRcdHJldHVybiB0aGlzO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBSZXBsYWNlIHRoZSBtb2NrZWQgWEhSIHdpdGggdGhlIG5hdGl2ZSBYSFIgYW5kIHJlbW92ZSBhbnkgaGFuZGxlcnNcblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHR0ZWFyZG93bjogZnVuY3Rpb24oKSB7XG5cdFx0TW9ja1hNTEh0dHBSZXF1ZXN0LmhhbmRsZXJzID0gW107XG5cdFx0d2luZG93LlhNTEh0dHBSZXF1ZXN0ID0gcmVhbDtcblx0XHRyZXR1cm4gdGhpcztcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge3N0cmluZ30gICAgW21ldGhvZF1cblx0ICogQHBhcmFtICAge3N0cmluZ30gICAgW3VybF1cblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRtb2NrOiBmdW5jdGlvbihtZXRob2QsIHVybCwgZm4pIHtcblx0XHR2YXIgaGFuZGxlcjtcblx0XHRpZiAoYXJndW1lbnRzLmxlbmd0aCA9PT0gMykge1xuXHRcdFx0aGFuZGxlciA9IGZ1bmN0aW9uKHJlcSwgcmVzKSB7XG5cdFx0XHRcdGlmIChyZXEubWV0aG9kKCkgPT09IG1ldGhvZCAmJiByZXEudXJsKCkgPT09IHVybCkge1xuXHRcdFx0XHRcdHJldHVybiBmbihyZXEsIHJlcyk7XG5cdFx0XHRcdH1cblx0XHRcdFx0cmV0dXJuIGZhbHNlO1xuXHRcdFx0fTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0aGFuZGxlciA9IG1ldGhvZDtcblx0XHR9XG5cblx0XHRNb2NrWE1MSHR0cFJlcXVlc3QuYWRkSGFuZGxlcihoYW5kbGVyKTtcblxuXHRcdHJldHVybiB0aGlzO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgR0VUIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge1N0cmluZ30gICAgdXJsXG5cdCAqIEBwYXJhbSAgIHtGdW5jdGlvbn0gIGZuXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0Z2V0OiBmdW5jdGlvbih1cmwsIGZuKSB7XG5cdFx0cmV0dXJuIHRoaXMubW9jaygnR0VUJywgdXJsLCBmbik7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBQT1NUIHJlcXVlc3Rcblx0ICogQHBhcmFtICAge1N0cmluZ30gICAgdXJsXG5cdCAqIEBwYXJhbSAgIHtGdW5jdGlvbn0gIGZuXG5cdCAqIEByZXR1cm5zIHtleHBvcnRzfVxuXHQgKi9cblx0cG9zdDogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ1BPU1QnLCB1cmwsIGZuKTtcblx0fSxcblxuXHQvKipcblx0ICogTW9jayBhIFBVVCByZXF1ZXN0XG5cdCAqIEBwYXJhbSAgIHtTdHJpbmd9ICAgIHVybFxuXHQgKiBAcGFyYW0gICB7RnVuY3Rpb259ICBmblxuXHQgKiBAcmV0dXJucyB7ZXhwb3J0c31cblx0ICovXG5cdHB1dDogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ1BVVCcsIHVybCwgZm4pO1xuXHR9LFxuXG5cdC8qKlxuXHQgKiBNb2NrIGEgUEFUQ0ggcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRwYXRjaDogZnVuY3Rpb24odXJsLCBmbikge1xuXHRcdHJldHVybiB0aGlzLm1vY2soJ1BBVENIJywgdXJsLCBmbik7XG5cdH0sXG5cblx0LyoqXG5cdCAqIE1vY2sgYSBERUxFVEUgcmVxdWVzdFxuXHQgKiBAcGFyYW0gICB7U3RyaW5nfSAgICB1cmxcblx0ICogQHBhcmFtICAge0Z1bmN0aW9ufSAgZm5cblx0ICogQHJldHVybnMge2V4cG9ydHN9XG5cdCAqL1xuXHRkZWxldGU6IGZ1bmN0aW9uKHVybCwgZm4pIHtcblx0XHRyZXR1cm4gdGhpcy5tb2NrKCdERUxFVEUnLCB1cmwsIGZuKTtcblx0fVxuXG59O1xuIiwiXG4vKipcbiAqIFRoZSBtb2NrZWQgcmVxdWVzdCBkYXRhXG4gKiBAY29uc3RydWN0b3JcbiAqL1xuZnVuY3Rpb24gTW9ja1JlcXVlc3QoeGhyKSB7XG4gIHRoaXMuX21ldGhvZCAgICA9IHhoci5tZXRob2Q7XG4gIHRoaXMuX3VybCAgICAgICA9IHhoci51cmw7XG4gIHRoaXMuX2hlYWRlcnMgICA9IHt9O1xuICB0aGlzLmhlYWRlcnMoeGhyLl9yZXF1ZXN0SGVhZGVycyk7XG4gIHRoaXMuYm9keSh4aHIuZGF0YSk7XG59XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBtZXRob2RcbiAqIEByZXR1cm5zIHtzdHJpbmd9XG4gKi9cbk1vY2tSZXF1ZXN0LnByb3RvdHlwZS5tZXRob2QgPSBmdW5jdGlvbigpIHtcbiAgcmV0dXJuIHRoaXMuX21ldGhvZDtcbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBVUkxcbiAqIEByZXR1cm5zIHtzdHJpbmd9XG4gKi9cbk1vY2tSZXF1ZXN0LnByb3RvdHlwZS51cmwgPSBmdW5jdGlvbigpIHtcbiAgcmV0dXJuIHRoaXMuX3VybDtcbn07XG5cbi8qKlxuICogR2V0L3NldCBhIEhUVFAgaGVhZGVyXG4gKiBAcGFyYW0gICB7c3RyaW5nfSBuYW1lXG4gKiBAcGFyYW0gICB7c3RyaW5nfSBbdmFsdWVdXG4gKiBAcmV0dXJucyB7c3RyaW5nfHVuZGVmaW5lZHxNb2NrUmVxdWVzdH1cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLmhlYWRlciA9IGZ1bmN0aW9uKG5hbWUsIHZhbHVlKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoID09PSAyKSB7XG4gICAgdGhpcy5faGVhZGVyc1tuYW1lLnRvTG93ZXJDYXNlKCldID0gdmFsdWU7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSB8fCBudWxsO1xuICB9XG59O1xuXG4vKipcbiAqIEdldC9zZXQgYWxsIG9mIHRoZSBIVFRQIGhlYWRlcnNcbiAqIEBwYXJhbSAgIHtPYmplY3R9IFtoZWFkZXJzXVxuICogQHJldHVybnMge09iamVjdHxNb2NrUmVxdWVzdH1cbiAqL1xuTW9ja1JlcXVlc3QucHJvdG90eXBlLmhlYWRlcnMgPSBmdW5jdGlvbihoZWFkZXJzKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgZm9yICh2YXIgbmFtZSBpbiBoZWFkZXJzKSB7XG4gICAgICBpZiAoaGVhZGVycy5oYXNPd25Qcm9wZXJ0eShuYW1lKSkge1xuICAgICAgICB0aGlzLmhlYWRlcihuYW1lLCBoZWFkZXJzW25hbWVdKTtcbiAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnM7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBib2R5XG4gKiBAcGFyYW0gICB7c3RyaW5nfSBbYm9keV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8TW9ja1JlcXVlc3R9XG4gKi9cbk1vY2tSZXF1ZXN0LnByb3RvdHlwZS5ib2R5ID0gZnVuY3Rpb24oYm9keSkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCkge1xuICAgIHRoaXMuX2JvZHkgPSBib2R5O1xuICAgIHJldHVybiB0aGlzO1xuICB9IGVsc2Uge1xuICAgIHJldHVybiB0aGlzLl9ib2R5O1xuICB9XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IE1vY2tSZXF1ZXN0O1xuIiwiXG4vKipcbiAqIFRoZSBtb2NrZWQgcmVzcG9uc2UgZGF0YVxuICogQGNvbnN0cnVjdG9yXG4gKi9cbmZ1bmN0aW9uIE1vY2tSZXNwb25zZSgpIHtcbiAgdGhpcy5fc3RhdHVzICAgICAgPSAyMDA7XG4gIHRoaXMuX2hlYWRlcnMgICAgID0ge307XG4gIHRoaXMuX2JvZHkgICAgICAgID0gJyc7XG4gIHRoaXMuX3RpbWVvdXQgICAgID0gZmFsc2U7XG59XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBzdGF0dXNcbiAqIEBwYXJhbSAgIHtudW1iZXJ9IFtjb2RlXVxuICogQHJldHVybnMge251bWJlcnxNb2NrUmVzcG9uc2V9XG4gKi9cbk1vY2tSZXNwb25zZS5wcm90b3R5cGUuc3RhdHVzID0gZnVuY3Rpb24oY29kZSkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCkge1xuICAgIHRoaXMuX3N0YXR1cyA9IGNvZGU7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX3N0YXR1cztcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IGEgSFRUUCBoZWFkZXJcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IG5hbWVcbiAqIEBwYXJhbSAgIHtzdHJpbmd9IFt2YWx1ZV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8dW5kZWZpbmVkfE1vY2tSZXNwb25zZX1cbiAqL1xuTW9ja1Jlc3BvbnNlLnByb3RvdHlwZS5oZWFkZXIgPSBmdW5jdGlvbihuYW1lLCB2YWx1ZSkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCA9PT0gMikge1xuICAgIHRoaXMuX2hlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSA9IHZhbHVlO1xuICAgIHJldHVybiB0aGlzO1xuICB9IGVsc2Uge1xuICAgIHJldHVybiB0aGlzLl9oZWFkZXJzW25hbWUudG9Mb3dlckNhc2UoKV0gfHwgbnVsbDtcbiAgfVxufTtcblxuLyoqXG4gKiBHZXQvc2V0IGFsbCBvZiB0aGUgSFRUUCBoZWFkZXJzXG4gKiBAcGFyYW0gICB7T2JqZWN0fSBbaGVhZGVyc11cbiAqIEByZXR1cm5zIHtPYmplY3R8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLmhlYWRlcnMgPSBmdW5jdGlvbihoZWFkZXJzKSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgZm9yICh2YXIgbmFtZSBpbiBoZWFkZXJzKSB7XG4gICAgICBpZiAoaGVhZGVycy5oYXNPd25Qcm9wZXJ0eShuYW1lKSkge1xuICAgICAgICB0aGlzLmhlYWRlcihuYW1lLCBoZWFkZXJzW25hbWVdKTtcbiAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2hlYWRlcnM7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCBib2R5XG4gKiBAcGFyYW0gICB7c3RyaW5nfSBbYm9keV1cbiAqIEByZXR1cm5zIHtzdHJpbmd8TW9ja1Jlc3BvbnNlfVxuICovXG5Nb2NrUmVzcG9uc2UucHJvdG90eXBlLmJvZHkgPSBmdW5jdGlvbihib2R5KSB7XG4gIGlmIChhcmd1bWVudHMubGVuZ3RoKSB7XG4gICAgdGhpcy5fYm9keSA9IGJvZHk7XG4gICAgcmV0dXJuIHRoaXM7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIHRoaXMuX2JvZHk7XG4gIH1cbn07XG5cbi8qKlxuICogR2V0L3NldCB0aGUgSFRUUCB0aW1lb3V0XG4gKiBAcGFyYW0gICB7Ym9vbGVhbnxudW1iZXJ9IFt0aW1lb3V0XVxuICogQHJldHVybnMge2Jvb2xlYW58bnVtYmVyfE1vY2tSZXNwb25zZX1cbiAqL1xuTW9ja1Jlc3BvbnNlLnByb3RvdHlwZS50aW1lb3V0ID0gZnVuY3Rpb24odGltZW91dCkge1xuICBpZiAoYXJndW1lbnRzLmxlbmd0aCkge1xuICAgIHRoaXMuX3RpbWVvdXQgPSB0aW1lb3V0O1xuICAgIHJldHVybiB0aGlzO1xuICB9IGVsc2Uge1xuICAgIHJldHVybiB0aGlzLl90aW1lb3V0O1xuICB9XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IE1vY2tSZXNwb25zZTtcbiIsInZhciBNb2NrUmVxdWVzdCAgID0gcmVxdWlyZSgnLi9Nb2NrUmVxdWVzdCcpO1xudmFyIE1vY2tSZXNwb25zZSAgPSByZXF1aXJlKCcuL01vY2tSZXNwb25zZScpO1xuXG52YXIgbm90SW1wbGVtZW50ZWRFcnJvciA9IG5ldyBFcnJvcignVGhpcyBmZWF0dXJlIGhhc25cXCd0IGJlZW4gaW1wbG1lbnRlZCB5ZXQuIFBsZWFzZSBzdWJtaXQgYW4gSXNzdWUgb3IgUHVsbCBSZXF1ZXN0IG9uIEdpdGh1Yi4nKTtcblxuLy9odHRwczovL2RldmVsb3Blci5tb3ppbGxhLm9yZy9lbi1VUy9kb2NzL1dlYi9BUEkvWE1MSHR0cFJlcXVlc3Rcbi8vaHR0cHM6Ly94aHIuc3BlYy53aGF0d2cub3JnL1xuLy9odHRwOi8vd3d3LnczLm9yZy9UUi8yMDA2L1dELVhNTEh0dHBSZXF1ZXN0LTIwMDYwNDA1L1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UICAgICAgICAgICAgID0gMDtcbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9PUEVORUQgICAgICAgICAgICAgPSAxO1xuTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0hFQURFUlNfUkVDRUlWRUQgICA9IDI7XG5Nb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfTE9BRElORyAgICAgICAgICAgID0gMztcbk1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FICAgICAgICAgICAgICAgPSA0O1xuXG4vKipcbiAqIFRoZSByZXF1ZXN0IGhhbmRsZXJzXG4gKiBAcHJpdmF0ZVxuICogQHR5cGUge0FycmF5fVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMgPSBbXTtcblxuLyoqXG4gKiBBZGQgYSByZXF1ZXN0IGhhbmRsZXJcbiAqIEBwYXJhbSAgIHtmdW5jdGlvbihNb2NrUmVxdWVzdCwgTW9ja1Jlc3BvbnNlKX0gZm5cbiAqIEByZXR1cm5zIHtNb2NrWE1MSHR0cFJlcXVlc3R9XG4gKi9cbk1vY2tYTUxIdHRwUmVxdWVzdC5hZGRIYW5kbGVyID0gZnVuY3Rpb24oZm4pIHtcbiAgTW9ja1hNTEh0dHBSZXF1ZXN0LmhhbmRsZXJzLnB1c2goZm4pO1xuICByZXR1cm4gdGhpcztcbn07XG5cbi8qKlxuICogUmVtb3ZlIGEgcmVxdWVzdCBoYW5kbGVyXG4gKiBAcGFyYW0gICB7ZnVuY3Rpb24oTW9ja1JlcXVlc3QsIE1vY2tSZXNwb25zZSl9IGZuXG4gKiBAcmV0dXJucyB7TW9ja1hNTEh0dHBSZXF1ZXN0fVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QucmVtb3ZlSGFuZGxlciA9IGZ1bmN0aW9uKGZuKSB7XG4gIHRocm93IG5vdEltcGxlbWVudGVkRXJyb3I7XG59O1xuXG4vKipcbiAqIEhhbmRsZSBhIHJlcXVlc3RcbiAqIEBwYXJhbSAgIHtNb2NrUmVxdWVzdH0gcmVxdWVzdFxuICogQHJldHVybnMge01vY2tSZXNwb25zZXxudWxsfVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlID0gZnVuY3Rpb24ocmVxdWVzdCkge1xuXG4gIGZvciAodmFyIGk9MDsgaTxNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnMubGVuZ3RoOyArK2kpIHtcblxuICAgIC8vZ2V0IHRoZSBnZW5lcmF0b3IgdG8gY3JlYXRlIGEgcmVzcG9uc2UgdG8gdGhlIHJlcXVlc3RcbiAgICB2YXIgcmVzcG9uc2UgPSBNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlcnNbaV0ocmVxdWVzdCwgbmV3IE1vY2tSZXNwb25zZSgpKTtcblxuICAgIGlmIChyZXNwb25zZSkge1xuICAgICAgcmV0dXJuIHJlc3BvbnNlO1xuICAgIH1cblxuICB9XG5cbiAgcmV0dXJuIG51bGw7XG59O1xuXG4vKipcbiAqIE1vY2sgWE1MSHR0cFJlcXVlc3RcbiAqIEBjb25zdHJ1Y3RvclxuICovXG5mdW5jdGlvbiBNb2NrWE1MSHR0cFJlcXVlc3QoKSB7XG4gIHRoaXMucmVzZXQoKTtcbiAgdGhpcy50aW1lb3V0ID0gMDtcbn1cblxuLyoqXG4gKiBSZXNldCB0aGUgcmVzcG9uc2UgdmFsdWVzXG4gKiBAcHJpdmF0ZVxuICovXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnJlc2V0ID0gZnVuY3Rpb24oKSB7XG5cbiAgdGhpcy5fcmVxdWVzdEhlYWRlcnMgID0ge307XG4gIHRoaXMuX3Jlc3BvbnNlSGVhZGVycyA9IHt9O1xuXG4gIHRoaXMuc3RhdHVzICAgICAgID0gMDtcbiAgdGhpcy5zdGF0dXNUZXh0ICAgPSAnJztcblxuICB0aGlzLnJlc3BvbnNlICAgICA9IG51bGw7XG4gIHRoaXMucmVzcG9uc2VUeXBlID0gbnVsbDtcbiAgdGhpcy5yZXNwb25zZVRleHQgPSBudWxsO1xuICB0aGlzLnJlc3BvbnNlWE1MICA9IG51bGw7XG5cbiAgdGhpcy5yZWFkeVN0YXRlICAgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UO1xufTtcblxuLyoqXG4gKiBUcmlnZ2VyIGFuIGV2ZW50XG4gKiBAcGFyYW0gICB7U3RyaW5nfSBldmVudFxuICogQHJldHVybnMge01vY2tYTUxIdHRwUmVxdWVzdH1cbiAqL1xuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS50cmlnZ2VyID0gZnVuY3Rpb24oZXZlbnQpIHtcblxuICBpZiAodGhpcy5vbnJlYWR5c3RhdGVjaGFuZ2UpIHtcbiAgICB0aGlzLm9ucmVhZHlzdGF0ZWNoYW5nZSgpO1xuICB9XG5cbiAgaWYgKHRoaXNbJ29uJytldmVudF0pIHtcbiAgICB0aGlzWydvbicrZXZlbnRdKCk7XG4gIH1cblxuICAvL2l0ZXJhdGUgb3ZlciB0aGUgbGlzdGVuZXJzXG5cbiAgcmV0dXJuIHRoaXM7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLm9wZW4gPSBmdW5jdGlvbihtZXRob2QsIHVybCwgYXN5bmMsIHVzZXIsIHBhc3N3b3JkKSB7XG4gIHRoaXMucmVzZXQoKTtcbiAgdGhpcy5tZXRob2QgICA9IG1ldGhvZDtcbiAgdGhpcy51cmwgICAgICA9IHVybDtcbiAgdGhpcy5hc3luYyAgICA9IGFzeW5jO1xuICB0aGlzLnVzZXIgICAgID0gdXNlcjtcbiAgdGhpcy5wYXNzd29yZCA9IHBhc3N3b3JkO1xuICB0aGlzLmRhdGEgICAgID0gbnVsbDtcbiAgdGhpcy5yZWFkeVN0YXRlID0gTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX09QRU5FRDtcbn07XG5cbk1vY2tYTUxIdHRwUmVxdWVzdC5wcm90b3R5cGUuc2V0UmVxdWVzdEhlYWRlciA9IGZ1bmN0aW9uKG5hbWUsIHZhbHVlKSB7XG4gIHRoaXMuX3JlcXVlc3RIZWFkZXJzW25hbWVdID0gdmFsdWU7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLm92ZXJyaWRlTWltZVR5cGUgPSBmdW5jdGlvbihtaW1lKSB7XG4gIHRocm93IG5vdEltcGxlbWVudGVkRXJyb3I7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnNlbmQgPSBmdW5jdGlvbihkYXRhKSB7XG4gIHZhciBzZWxmID0gdGhpcztcbiAgdGhpcy5kYXRhID0gZGF0YTtcblxuICBzZWxmLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfTE9BRElORztcblxuICBzZWxmLl9zZW5kVGltZW91dCA9IHNldFRpbWVvdXQoZnVuY3Rpb24oKSB7XG5cbiAgICB2YXIgcmVzcG9uc2UgPSBNb2NrWE1MSHR0cFJlcXVlc3QuaGFuZGxlKG5ldyBNb2NrUmVxdWVzdChzZWxmKSk7XG5cbiAgICBpZiAocmVzcG9uc2UgJiYgcmVzcG9uc2UgaW5zdGFuY2VvZiBNb2NrUmVzcG9uc2UpIHtcblxuICAgICAgdmFyIHRpbWVvdXQgPSByZXNwb25zZS50aW1lb3V0KCk7XG5cbiAgICAgIGlmICh0aW1lb3V0KSB7XG5cbiAgICAgICAgLy90cmlnZ2VyIGEgdGltZW91dCBldmVudCBiZWNhdXNlIHRoZSByZXF1ZXN0IHRpbWVkIG91dCAtIHdhaXQgZm9yIHRoZSB0aW1lb3V0IHRpbWUgYmVjYXVzZSBtYW55IGxpYnMgbGlrZSBqcXVlcnkgYW5kIHN1cGVyYWdlbnQgdXNlIHNldFRpbWVvdXQgdG8gZGV0ZWN0IHRoZSBlcnJvciB0eXBlXG4gICAgICAgIHNlbGYuX3NlbmRUaW1lb3V0ID0gc2V0VGltZW91dChmdW5jdGlvbigpIHtcbiAgICAgICAgICBzZWxmLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfRE9ORTtcbiAgICAgICAgICBzZWxmLnRyaWdnZXIoJ3RpbWVvdXQnKTtcbiAgICAgICAgfSwgdHlwZW9mKHRpbWVvdXQpID09PSAnbnVtYmVyJyA/IHRpbWVvdXQgOiBzZWxmLnRpbWVvdXQrMSk7XG5cbiAgICAgIH0gZWxzZSB7XG5cbiAgICAgICAgLy9tYXAgdGhlIHJlc3BvbnNlIHRvIHRoZSBYSFIgb2JqZWN0XG4gICAgICAgIHNlbGYuc3RhdHVzICAgICAgICAgICAgID0gcmVzcG9uc2Uuc3RhdHVzKCk7XG4gICAgICAgIHNlbGYuX3Jlc3BvbnNlSGVhZGVycyAgID0gcmVzcG9uc2UuaGVhZGVycygpO1xuICAgICAgICBzZWxmLnJlc3BvbnNlVHlwZSAgICAgICA9ICd0ZXh0JztcbiAgICAgICAgc2VsZi5yZXNwb25zZSAgICAgICAgICAgPSByZXNwb25zZS5ib2R5KCk7XG4gICAgICAgIHNlbGYucmVzcG9uc2VUZXh0ICAgICAgID0gcmVzcG9uc2UuYm9keSgpOyAvL1RPRE86IGRldGVjdCBhbiBvYmplY3QgYW5kIHJldHVybiBKU09OLCBkZXRlY3QgWE1MIGFuZCByZXR1cm4gWE1MXG4gICAgICAgIHNlbGYucmVhZHlTdGF0ZSAgICAgICAgID0gTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0RPTkU7XG5cbiAgICAgICAgLy90cmlnZ2VyIGEgbG9hZCBldmVudCBiZWNhdXNlIHRoZSByZXF1ZXN0IHdhcyByZWNlaXZlZFxuICAgICAgICBzZWxmLnRyaWdnZXIoJ2xvYWQnKTtcblxuICAgICAgfVxuXG4gICAgfSBlbHNlIHtcblxuICAgICAgLy90cmlnZ2VyIGFuIGVycm9yIGJlY2F1c2UgdGhlIHJlcXVlc3Qgd2FzIG5vdCBoYW5kbGVkXG4gICAgICBzZWxmLnJlYWR5U3RhdGUgPSBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfRE9ORTtcbiAgICAgIHNlbGYudHJpZ2dlcignZXJyb3InKTtcblxuICAgIH1cblxuICB9LCAwKTtcblxufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5hYm9ydCA9IGZ1bmN0aW9uKCkge1xuICBjbGVhclRpbWVvdXQodGhpcy5fc2VuZFRpbWVvdXQpO1xuXG4gIGlmICh0aGlzLnJlYWR5U3RhdGUgPiBNb2NrWE1MSHR0cFJlcXVlc3QuU1RBVEVfVU5TRU5UICYmIHRoaXMucmVhZHlTdGF0ZSA8IE1vY2tYTUxIdHRwUmVxdWVzdC5TVEFURV9ET05FKSB7XG4gICAgdGhpcy5yZWFkeVN0YXRlID0gTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX1VOU0VOVDtcbiAgICB0aGlzLnRyaWdnZXIoJ2Fib3J0Jyk7XG4gIH1cblxufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5nZXRBbGxSZXNwb25zZUhlYWRlcnMgPSBmdW5jdGlvbigpIHtcblxuICBpZiAodGhpcy5yZWFkeVN0YXRlIDwgTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0hFQURFUlNfUkVDRUlWRUQpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuXG4gIHZhciBoZWFkZXJzID0gJyc7XG4gIGZvciAodmFyIG5hbWUgaW4gdGhpcy5fcmVzcG9uc2VIZWFkZXJzKSB7XG4gICAgaWYgKHRoaXMuX3Jlc3BvbnNlSGVhZGVycy5oYXNPd25Qcm9wZXJ0eShuYW1lKSkge1xuICAgICAgaGVhZGVycyArPSBuYW1lKyc6ICcrdGhpcy5fcmVzcG9uc2VIZWFkZXJzW25hbWVdKydcXHJcXG4nO1xuICAgIH1cbiAgfVxuXG4gIHJldHVybiBoZWFkZXJzO1xufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5nZXRSZXNwb25zZUhlYWRlciA9IGZ1bmN0aW9uKG5hbWUpIHtcblxuICBpZiAodGhpcy5yZWFkeVN0YXRlIDwgTW9ja1hNTEh0dHBSZXF1ZXN0LlNUQVRFX0hFQURFUlNfUkVDRUlWRUQpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuXG4gIHJldHVybiB0aGlzLl9yZXNwb25zZUhlYWRlcnNbbmFtZS50b0xvd2VyQ2FzZSgpXSB8fCBudWxsO1xufTtcblxuTW9ja1hNTEh0dHBSZXF1ZXN0LnByb3RvdHlwZS5hZGRFdmVudExpc3RlbmVyID0gZnVuY3Rpb24oZXZlbnQsIGxpc3RlbmVyKSB7XG4gIHRocm93IG5vdEltcGxlbWVudGVkRXJyb3I7XG59O1xuXG5Nb2NrWE1MSHR0cFJlcXVlc3QucHJvdG90eXBlLnJlbW92ZUV2ZW50TGlzdGVuZXIgPSBmdW5jdGlvbihldmVudCwgbGlzdGVuZXIpIHtcbiAgdGhyb3cgbm90SW1wbGVtZW50ZWRFcnJvcjtcbn07XG5cbm1vZHVsZS5leHBvcnRzID0gTW9ja1hNTEh0dHBSZXF1ZXN0O1xuIiwiXCJ1c2Ugc3RyaWN0XCI7XG52YXIgd2luZG93ID0gcmVxdWlyZShcImdsb2JhbC93aW5kb3dcIilcbnZhciBpc0Z1bmN0aW9uID0gcmVxdWlyZShcImlzLWZ1bmN0aW9uXCIpXG52YXIgcGFyc2VIZWFkZXJzID0gcmVxdWlyZShcInBhcnNlLWhlYWRlcnNcIilcbnZhciB4dGVuZCA9IHJlcXVpcmUoXCJ4dGVuZFwiKVxuXG5tb2R1bGUuZXhwb3J0cyA9IGNyZWF0ZVhIUlxuY3JlYXRlWEhSLlhNTEh0dHBSZXF1ZXN0ID0gd2luZG93LlhNTEh0dHBSZXF1ZXN0IHx8IG5vb3BcbmNyZWF0ZVhIUi5YRG9tYWluUmVxdWVzdCA9IFwid2l0aENyZWRlbnRpYWxzXCIgaW4gKG5ldyBjcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QoKSkgPyBjcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QgOiB3aW5kb3cuWERvbWFpblJlcXVlc3RcblxuZm9yRWFjaEFycmF5KFtcImdldFwiLCBcInB1dFwiLCBcInBvc3RcIiwgXCJwYXRjaFwiLCBcImhlYWRcIiwgXCJkZWxldGVcIl0sIGZ1bmN0aW9uKG1ldGhvZCkge1xuICAgIGNyZWF0ZVhIUlttZXRob2QgPT09IFwiZGVsZXRlXCIgPyBcImRlbFwiIDogbWV0aG9kXSA9IGZ1bmN0aW9uKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICAgICAgb3B0aW9ucyA9IGluaXRQYXJhbXModXJpLCBvcHRpb25zLCBjYWxsYmFjaylcbiAgICAgICAgb3B0aW9ucy5tZXRob2QgPSBtZXRob2QudG9VcHBlckNhc2UoKVxuICAgICAgICByZXR1cm4gX2NyZWF0ZVhIUihvcHRpb25zKVxuICAgIH1cbn0pXG5cbmZ1bmN0aW9uIGZvckVhY2hBcnJheShhcnJheSwgaXRlcmF0b3IpIHtcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGFycmF5Lmxlbmd0aDsgaSsrKSB7XG4gICAgICAgIGl0ZXJhdG9yKGFycmF5W2ldKVxuICAgIH1cbn1cblxuZnVuY3Rpb24gaXNFbXB0eShvYmope1xuICAgIGZvcih2YXIgaSBpbiBvYmope1xuICAgICAgICBpZihvYmouaGFzT3duUHJvcGVydHkoaSkpIHJldHVybiBmYWxzZVxuICAgIH1cbiAgICByZXR1cm4gdHJ1ZVxufVxuXG5mdW5jdGlvbiBpbml0UGFyYW1zKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICB2YXIgcGFyYW1zID0gdXJpXG5cbiAgICBpZiAoaXNGdW5jdGlvbihvcHRpb25zKSkge1xuICAgICAgICBjYWxsYmFjayA9IG9wdGlvbnNcbiAgICAgICAgaWYgKHR5cGVvZiB1cmkgPT09IFwic3RyaW5nXCIpIHtcbiAgICAgICAgICAgIHBhcmFtcyA9IHt1cmk6dXJpfVxuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcGFyYW1zID0geHRlbmQob3B0aW9ucywge3VyaTogdXJpfSlcbiAgICB9XG5cbiAgICBwYXJhbXMuY2FsbGJhY2sgPSBjYWxsYmFja1xuICAgIHJldHVybiBwYXJhbXNcbn1cblxuZnVuY3Rpb24gY3JlYXRlWEhSKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spIHtcbiAgICBvcHRpb25zID0gaW5pdFBhcmFtcyh1cmksIG9wdGlvbnMsIGNhbGxiYWNrKVxuICAgIHJldHVybiBfY3JlYXRlWEhSKG9wdGlvbnMpXG59XG5cbmZ1bmN0aW9uIF9jcmVhdGVYSFIob3B0aW9ucykge1xuICAgIGlmKHR5cGVvZiBvcHRpb25zLmNhbGxiYWNrID09PSBcInVuZGVmaW5lZFwiKXtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKFwiY2FsbGJhY2sgYXJndW1lbnQgbWlzc2luZ1wiKVxuICAgIH1cblxuICAgIHZhciBjYWxsZWQgPSBmYWxzZVxuICAgIHZhciBjYWxsYmFjayA9IGZ1bmN0aW9uIGNiT25jZShlcnIsIHJlc3BvbnNlLCBib2R5KXtcbiAgICAgICAgaWYoIWNhbGxlZCl7XG4gICAgICAgICAgICBjYWxsZWQgPSB0cnVlXG4gICAgICAgICAgICBvcHRpb25zLmNhbGxiYWNrKGVyciwgcmVzcG9uc2UsIGJvZHkpXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICBmdW5jdGlvbiByZWFkeXN0YXRlY2hhbmdlKCkge1xuICAgICAgICBpZiAoeGhyLnJlYWR5U3RhdGUgPT09IDQpIHtcbiAgICAgICAgICAgIGxvYWRGdW5jKClcbiAgICAgICAgfVxuICAgIH1cblxuICAgIGZ1bmN0aW9uIGdldEJvZHkoKSB7XG4gICAgICAgIC8vIENocm9tZSB3aXRoIHJlcXVlc3RUeXBlPWJsb2IgdGhyb3dzIGVycm9ycyBhcnJvdW5kIHdoZW4gZXZlbiB0ZXN0aW5nIGFjY2VzcyB0byByZXNwb25zZVRleHRcbiAgICAgICAgdmFyIGJvZHkgPSB1bmRlZmluZWRcblxuICAgICAgICBpZiAoeGhyLnJlc3BvbnNlKSB7XG4gICAgICAgICAgICBib2R5ID0geGhyLnJlc3BvbnNlXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBib2R5ID0geGhyLnJlc3BvbnNlVGV4dCB8fCBnZXRYbWwoeGhyKVxuICAgICAgICB9XG5cbiAgICAgICAgaWYgKGlzSnNvbikge1xuICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICBib2R5ID0gSlNPTi5wYXJzZShib2R5KVxuICAgICAgICAgICAgfSBjYXRjaCAoZSkge31cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBib2R5XG4gICAgfVxuXG4gICAgdmFyIGZhaWx1cmVSZXNwb25zZSA9IHtcbiAgICAgICAgICAgICAgICBib2R5OiB1bmRlZmluZWQsXG4gICAgICAgICAgICAgICAgaGVhZGVyczoge30sXG4gICAgICAgICAgICAgICAgc3RhdHVzQ29kZTogMCxcbiAgICAgICAgICAgICAgICBtZXRob2Q6IG1ldGhvZCxcbiAgICAgICAgICAgICAgICB1cmw6IHVyaSxcbiAgICAgICAgICAgICAgICByYXdSZXF1ZXN0OiB4aHJcbiAgICAgICAgICAgIH1cblxuICAgIGZ1bmN0aW9uIGVycm9yRnVuYyhldnQpIHtcbiAgICAgICAgY2xlYXJUaW1lb3V0KHRpbWVvdXRUaW1lcilcbiAgICAgICAgaWYoIShldnQgaW5zdGFuY2VvZiBFcnJvcikpe1xuICAgICAgICAgICAgZXZ0ID0gbmV3IEVycm9yKFwiXCIgKyAoZXZ0IHx8IFwiVW5rbm93biBYTUxIdHRwUmVxdWVzdCBFcnJvclwiKSApXG4gICAgICAgIH1cbiAgICAgICAgZXZ0LnN0YXR1c0NvZGUgPSAwXG4gICAgICAgIHJldHVybiBjYWxsYmFjayhldnQsIGZhaWx1cmVSZXNwb25zZSlcbiAgICB9XG5cbiAgICAvLyB3aWxsIGxvYWQgdGhlIGRhdGEgJiBwcm9jZXNzIHRoZSByZXNwb25zZSBpbiBhIHNwZWNpYWwgcmVzcG9uc2Ugb2JqZWN0XG4gICAgZnVuY3Rpb24gbG9hZEZ1bmMoKSB7XG4gICAgICAgIGlmIChhYm9ydGVkKSByZXR1cm5cbiAgICAgICAgdmFyIHN0YXR1c1xuICAgICAgICBjbGVhclRpbWVvdXQodGltZW91dFRpbWVyKVxuICAgICAgICBpZihvcHRpb25zLnVzZVhEUiAmJiB4aHIuc3RhdHVzPT09dW5kZWZpbmVkKSB7XG4gICAgICAgICAgICAvL0lFOCBDT1JTIEdFVCBzdWNjZXNzZnVsIHJlc3BvbnNlIGRvZXNuJ3QgaGF2ZSBhIHN0YXR1cyBmaWVsZCwgYnV0IGJvZHkgaXMgZmluZVxuICAgICAgICAgICAgc3RhdHVzID0gMjAwXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBzdGF0dXMgPSAoeGhyLnN0YXR1cyA9PT0gMTIyMyA/IDIwNCA6IHhoci5zdGF0dXMpXG4gICAgICAgIH1cbiAgICAgICAgdmFyIHJlc3BvbnNlID0gZmFpbHVyZVJlc3BvbnNlXG4gICAgICAgIHZhciBlcnIgPSBudWxsXG5cbiAgICAgICAgaWYgKHN0YXR1cyAhPT0gMCl7XG4gICAgICAgICAgICByZXNwb25zZSA9IHtcbiAgICAgICAgICAgICAgICBib2R5OiBnZXRCb2R5KCksXG4gICAgICAgICAgICAgICAgc3RhdHVzQ29kZTogc3RhdHVzLFxuICAgICAgICAgICAgICAgIG1ldGhvZDogbWV0aG9kLFxuICAgICAgICAgICAgICAgIGhlYWRlcnM6IHt9LFxuICAgICAgICAgICAgICAgIHVybDogdXJpLFxuICAgICAgICAgICAgICAgIHJhd1JlcXVlc3Q6IHhoclxuICAgICAgICAgICAgfVxuICAgICAgICAgICAgaWYoeGhyLmdldEFsbFJlc3BvbnNlSGVhZGVycyl7IC8vcmVtZW1iZXIgeGhyIGNhbiBpbiBmYWN0IGJlIFhEUiBmb3IgQ09SUyBpbiBJRVxuICAgICAgICAgICAgICAgIHJlc3BvbnNlLmhlYWRlcnMgPSBwYXJzZUhlYWRlcnMoeGhyLmdldEFsbFJlc3BvbnNlSGVhZGVycygpKVxuICAgICAgICAgICAgfVxuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgZXJyID0gbmV3IEVycm9yKFwiSW50ZXJuYWwgWE1MSHR0cFJlcXVlc3QgRXJyb3JcIilcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gY2FsbGJhY2soZXJyLCByZXNwb25zZSwgcmVzcG9uc2UuYm9keSlcbiAgICB9XG5cbiAgICB2YXIgeGhyID0gb3B0aW9ucy54aHIgfHwgbnVsbFxuXG4gICAgaWYgKCF4aHIpIHtcbiAgICAgICAgaWYgKG9wdGlvbnMuY29ycyB8fCBvcHRpb25zLnVzZVhEUikge1xuICAgICAgICAgICAgeGhyID0gbmV3IGNyZWF0ZVhIUi5YRG9tYWluUmVxdWVzdCgpXG4gICAgICAgIH1lbHNle1xuICAgICAgICAgICAgeGhyID0gbmV3IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCgpXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIga2V5XG4gICAgdmFyIGFib3J0ZWRcbiAgICB2YXIgdXJpID0geGhyLnVybCA9IG9wdGlvbnMudXJpIHx8IG9wdGlvbnMudXJsXG4gICAgdmFyIG1ldGhvZCA9IHhoci5tZXRob2QgPSBvcHRpb25zLm1ldGhvZCB8fCBcIkdFVFwiXG4gICAgdmFyIGJvZHkgPSBvcHRpb25zLmJvZHkgfHwgb3B0aW9ucy5kYXRhIHx8IG51bGxcbiAgICB2YXIgaGVhZGVycyA9IHhoci5oZWFkZXJzID0gb3B0aW9ucy5oZWFkZXJzIHx8IHt9XG4gICAgdmFyIHN5bmMgPSAhIW9wdGlvbnMuc3luY1xuICAgIHZhciBpc0pzb24gPSBmYWxzZVxuICAgIHZhciB0aW1lb3V0VGltZXJcblxuICAgIGlmIChcImpzb25cIiBpbiBvcHRpb25zKSB7XG4gICAgICAgIGlzSnNvbiA9IHRydWVcbiAgICAgICAgaGVhZGVyc1tcImFjY2VwdFwiXSB8fCBoZWFkZXJzW1wiQWNjZXB0XCJdIHx8IChoZWFkZXJzW1wiQWNjZXB0XCJdID0gXCJhcHBsaWNhdGlvbi9qc29uXCIpIC8vRG9uJ3Qgb3ZlcnJpZGUgZXhpc3RpbmcgYWNjZXB0IGhlYWRlciBkZWNsYXJlZCBieSB1c2VyXG4gICAgICAgIGlmIChtZXRob2QgIT09IFwiR0VUXCIgJiYgbWV0aG9kICE9PSBcIkhFQURcIikge1xuICAgICAgICAgICAgaGVhZGVyc1tcImNvbnRlbnQtdHlwZVwiXSB8fCBoZWFkZXJzW1wiQ29udGVudC1UeXBlXCJdIHx8IChoZWFkZXJzW1wiQ29udGVudC1UeXBlXCJdID0gXCJhcHBsaWNhdGlvbi9qc29uXCIpIC8vRG9uJ3Qgb3ZlcnJpZGUgZXhpc3RpbmcgYWNjZXB0IGhlYWRlciBkZWNsYXJlZCBieSB1c2VyXG4gICAgICAgICAgICBib2R5ID0gSlNPTi5zdHJpbmdpZnkob3B0aW9ucy5qc29uKVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgeGhyLm9ucmVhZHlzdGF0ZWNoYW5nZSA9IHJlYWR5c3RhdGVjaGFuZ2VcbiAgICB4aHIub25sb2FkID0gbG9hZEZ1bmNcbiAgICB4aHIub25lcnJvciA9IGVycm9yRnVuY1xuICAgIC8vIElFOSBtdXN0IGhhdmUgb25wcm9ncmVzcyBiZSBzZXQgdG8gYSB1bmlxdWUgZnVuY3Rpb24uXG4gICAgeGhyLm9ucHJvZ3Jlc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIC8vIElFIG11c3QgZGllXG4gICAgfVxuICAgIHhoci5vbnRpbWVvdXQgPSBlcnJvckZ1bmNcbiAgICB4aHIub3BlbihtZXRob2QsIHVyaSwgIXN5bmMsIG9wdGlvbnMudXNlcm5hbWUsIG9wdGlvbnMucGFzc3dvcmQpXG4gICAgLy9oYXMgdG8gYmUgYWZ0ZXIgb3BlblxuICAgIGlmKCFzeW5jKSB7XG4gICAgICAgIHhoci53aXRoQ3JlZGVudGlhbHMgPSAhIW9wdGlvbnMud2l0aENyZWRlbnRpYWxzXG4gICAgfVxuICAgIC8vIENhbm5vdCBzZXQgdGltZW91dCB3aXRoIHN5bmMgcmVxdWVzdFxuICAgIC8vIG5vdCBzZXR0aW5nIHRpbWVvdXQgb24gdGhlIHhociBvYmplY3QsIGJlY2F1c2Ugb2Ygb2xkIHdlYmtpdHMgZXRjLiBub3QgaGFuZGxpbmcgdGhhdCBjb3JyZWN0bHlcbiAgICAvLyBib3RoIG5wbSdzIHJlcXVlc3QgYW5kIGpxdWVyeSAxLnggdXNlIHRoaXMga2luZCBvZiB0aW1lb3V0LCBzbyB0aGlzIGlzIGJlaW5nIGNvbnNpc3RlbnRcbiAgICBpZiAoIXN5bmMgJiYgb3B0aW9ucy50aW1lb3V0ID4gMCApIHtcbiAgICAgICAgdGltZW91dFRpbWVyID0gc2V0VGltZW91dChmdW5jdGlvbigpe1xuICAgICAgICAgICAgYWJvcnRlZD10cnVlLy9JRTkgbWF5IHN0aWxsIGNhbGwgcmVhZHlzdGF0ZWNoYW5nZVxuICAgICAgICAgICAgeGhyLmFib3J0KFwidGltZW91dFwiKVxuICAgICAgICAgICAgdmFyIGUgPSBuZXcgRXJyb3IoXCJYTUxIdHRwUmVxdWVzdCB0aW1lb3V0XCIpXG4gICAgICAgICAgICBlLmNvZGUgPSBcIkVUSU1FRE9VVFwiXG4gICAgICAgICAgICBlcnJvckZ1bmMoZSlcbiAgICAgICAgfSwgb3B0aW9ucy50aW1lb3V0IClcbiAgICB9XG5cbiAgICBpZiAoeGhyLnNldFJlcXVlc3RIZWFkZXIpIHtcbiAgICAgICAgZm9yKGtleSBpbiBoZWFkZXJzKXtcbiAgICAgICAgICAgIGlmKGhlYWRlcnMuaGFzT3duUHJvcGVydHkoa2V5KSl7XG4gICAgICAgICAgICAgICAgeGhyLnNldFJlcXVlc3RIZWFkZXIoa2V5LCBoZWFkZXJzW2tleV0pXG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9IGVsc2UgaWYgKG9wdGlvbnMuaGVhZGVycyAmJiAhaXNFbXB0eShvcHRpb25zLmhlYWRlcnMpKSB7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcihcIkhlYWRlcnMgY2Fubm90IGJlIHNldCBvbiBhbiBYRG9tYWluUmVxdWVzdCBvYmplY3RcIilcbiAgICB9XG5cbiAgICBpZiAoXCJyZXNwb25zZVR5cGVcIiBpbiBvcHRpb25zKSB7XG4gICAgICAgIHhoci5yZXNwb25zZVR5cGUgPSBvcHRpb25zLnJlc3BvbnNlVHlwZVxuICAgIH1cblxuICAgIGlmIChcImJlZm9yZVNlbmRcIiBpbiBvcHRpb25zICYmXG4gICAgICAgIHR5cGVvZiBvcHRpb25zLmJlZm9yZVNlbmQgPT09IFwiZnVuY3Rpb25cIlxuICAgICkge1xuICAgICAgICBvcHRpb25zLmJlZm9yZVNlbmQoeGhyKVxuICAgIH1cblxuICAgIHhoci5zZW5kKGJvZHkpXG5cbiAgICByZXR1cm4geGhyXG5cblxufVxuXG5mdW5jdGlvbiBnZXRYbWwoeGhyKSB7XG4gICAgaWYgKHhoci5yZXNwb25zZVR5cGUgPT09IFwiZG9jdW1lbnRcIikge1xuICAgICAgICByZXR1cm4geGhyLnJlc3BvbnNlWE1MXG4gICAgfVxuICAgIHZhciBmaXJlZm94QnVnVGFrZW5FZmZlY3QgPSB4aHIuc3RhdHVzID09PSAyMDQgJiYgeGhyLnJlc3BvbnNlWE1MICYmIHhoci5yZXNwb25zZVhNTC5kb2N1bWVudEVsZW1lbnQubm9kZU5hbWUgPT09IFwicGFyc2VyZXJyb3JcIlxuICAgIGlmICh4aHIucmVzcG9uc2VUeXBlID09PSBcIlwiICYmICFmaXJlZm94QnVnVGFrZW5FZmZlY3QpIHtcbiAgICAgICAgcmV0dXJuIHhoci5yZXNwb25zZVhNTFxuICAgIH1cblxuICAgIHJldHVybiBudWxsXG59XG5cbmZ1bmN0aW9uIG5vb3AoKSB7fVxuIiwibW9kdWxlLmV4cG9ydHMgPSBleHRlbmRcblxudmFyIGhhc093blByb3BlcnR5ID0gT2JqZWN0LnByb3RvdHlwZS5oYXNPd25Qcm9wZXJ0eTtcblxuZnVuY3Rpb24gZXh0ZW5kKCkge1xuICAgIHZhciB0YXJnZXQgPSB7fVxuXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBhcmd1bWVudHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgdmFyIHNvdXJjZSA9IGFyZ3VtZW50c1tpXVxuXG4gICAgICAgIGZvciAodmFyIGtleSBpbiBzb3VyY2UpIHtcbiAgICAgICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKHNvdXJjZSwga2V5KSkge1xuICAgICAgICAgICAgICAgIHRhcmdldFtrZXldID0gc291cmNlW2tleV1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH1cblxuICAgIHJldHVybiB0YXJnZXRcbn1cbiIsImltcG9ydCBzdG9yZSBmcm9tIFwiLi4vc3RvcmVcIjtcbmltcG9ydCB4aHIgZnJvbSBcInhoclwiO1xuaW1wb3J0IG1hcHBpbmdUb0pzb25MZFJtbCBmcm9tIFwiLi4vdXRpbC9tYXBwaW5nVG9Kc29uTGRSbWxcIlxudmFyIHRvSnNvbjtcbmlmIChwcm9jZXNzLmVudi5OT0RFX0VOViA9PT0gXCJkZXZlbG9wbWVudFwiKSB7XG5cdHRvSnNvbiA9IGZ1bmN0aW9uIHRvSnNvbihkYXRhKSB7XG5cdFx0cmV0dXJuIEpTT04uc3RyaW5naWZ5KGRhdGEsIHVuZGVmaW5lZCwgMik7XG5cdH1cbn0gZWxzZSB7XG5cdHRvSnNvbiA9IGZ1bmN0aW9uIHRvSnNvbihkYXRhKSB7XG5cdFx0cmV0dXJuIEpTT04uc3RyaW5naWZ5KGRhdGEpO1xuXHR9XG59XG5cbnZhciBhY3Rpb25zID0ge1xuXHRvblVwbG9hZEZpbGVTZWxlY3Q6IGZ1bmN0aW9uIChmaWxlcykge1xuXHRcdGxldCBmaWxlID0gZmlsZXNbMF07XG5cdFx0bGV0IGZvcm1EYXRhID0gbmV3IEZvcm1EYXRhKCk7XG5cdFx0Zm9ybURhdGEuYXBwZW5kKFwiZmlsZVwiLCBmaWxlKTtcblx0XHRmb3JtRGF0YS5hcHBlbmQoXCJ2cmVcIiwgZmlsZS5uYW1lKTtcblx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTVEFSVF9VUExPQURcIn0pXG5cdFx0c3RvcmUuZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuXHRcdFx0dmFyIHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdHZhciBwYXlsb2FkID0ge1xuXHRcdFx0XHRib2R5OiBmb3JtRGF0YSxcblx0XHRcdFx0aGVhZGVyczoge1xuXHRcdFx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcblx0XHRcdFx0fVxuXHRcdFx0fTtcblx0XHRcdHhoci5wb3N0KHByb2Nlc3MuZW52LnNlcnZlciArIFwiL3YyLjEvYnVsay11cGxvYWRcIiwgcGF5bG9hZCwgZnVuY3Rpb24gKGVyciwgcmVzcCkge1xuXHRcdFx0XHRsZXQgbG9jYXRpb24gPSByZXNwLmhlYWRlcnMubG9jYXRpb247XG5cdFx0XHRcdHhoci5nZXQobG9jYXRpb24sIGZ1bmN0aW9uIChlcnIsIHJlc3AsIGJvZHkpIHtcblx0XHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJGSU5JU0hfVVBMT0FEXCIsIGRhdGE6IEpTT04ucGFyc2UoYm9keSl9KVxuXHRcdFx0XHR9KTtcblx0XHRcdH0pO1xuXHRcdH0pO1xuXHR9LFxuXG5cdG9uU2F2ZU1hcHBpbmdzOiBmdW5jdGlvbiAoKSB7XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9TVEFSVEVEXCJ9KVxuXHRcdHN0b3JlLmRpc3BhdGNoKGZ1bmN0aW9uIChkaXNwYXRjaCwgZ2V0U3RhdGUpIHtcblx0XHRcdHZhciBzdGF0ZSA9IGdldFN0YXRlKCk7XG5cdFx0XHR2YXIgcGF5bG9hZCA9IHtcblx0XHRcdFx0Ym9keTogdG9Kc29uKG1hcHBpbmdUb0pzb25MZFJtbChzdGF0ZS5tYXBwaW5ncywgc3RhdGUuaW1wb3J0RGF0YS52cmUpKSxcblx0XHRcdFx0aGVhZGVyczoge1xuXHRcdFx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiBzdGF0ZS51c2VyZGF0YS51c2VySWRcblx0XHRcdFx0fVxuXHRcdFx0fTtcblxuXHRcdFx0eGhyLnBvc3Qoc3RhdGUuaW1wb3J0RGF0YS5zYXZlTWFwcGluZ1VybCwgcGF5bG9hZCwgZnVuY3Rpb24gKGVyciwgcmVzcCkge1xuXHRcdFx0XHRpZiAoZXJyKSB7XG5cdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9IQURfRVJST1JcIn0pXG5cdFx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9TVUNDRUVERURcIn0pXG5cdFx0XHRcdH1cblx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0FWRV9GSU5JU0hFRFwifSlcblx0XHRcdH0pO1xuXHRcdH0pO1xuXHR9LFxuXG5cdG9uUHVibGlzaERhdGE6IGZ1bmN0aW9uICgpe1xuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfU1RBUlRFRFwifSlcblx0XHRzdG9yZS5kaXNwYXRjaChmdW5jdGlvbiAoZGlzcGF0Y2gsIGdldFN0YXRlKSB7XG5cdFx0XHR2YXIgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuXHRcdFx0dmFyIHBheWxvYWQgPSB7XG5cdFx0XHRcdGhlYWRlcnM6IHtcblx0XHRcdFx0XHRcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkXG5cdFx0XHRcdH1cblx0XHRcdH07XG5cblx0XHRcdHhoci5wb3N0KHN0YXRlLmltcG9ydERhdGEuZXhlY3V0ZU1hcHBpbmdVcmwsIHBheWxvYWQsIGZ1bmN0aW9uIChlcnIsIHJlc3ApIHtcblx0XHRcdFx0aWYgKGVycikge1xuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfSEFEX0VSUk9SXCJ9KVxuXHRcdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfU1VDQ0VFREVEXCJ9KVxuXHRcdFx0XHR9XG5cdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlBVQkxJU0hfRklOSVNIRURcIn0pXG5cdFx0XHR9KTtcblx0XHR9KTtcblx0fSxcblxuXHRvblNlbGVjdENvbGxlY3Rpb246IChjb2xsZWN0aW9uKSA9PiB7XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0FDVElWRV9DT0xMRUNUSU9OXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb259KTtcblx0XHRzdG9yZS5kaXNwYXRjaChmdW5jdGlvbiAoZGlzcGF0Y2gsIGdldFN0YXRlKSB7XG5cdFx0XHR2YXIgc3RhdGUgPSBnZXRTdGF0ZSgpO1xuXHRcdFx0dmFyIGN1cnJlbnRTaGVldCA9IHN0YXRlLmltcG9ydERhdGEuc2hlZXRzLmZpbmQoeCA9PiB4LmNvbGxlY3Rpb24gPT09IGNvbGxlY3Rpb24pO1xuXHRcdFx0aWYgKGN1cnJlbnRTaGVldC5yb3dzLmxlbmd0aCA9PT0gMCAmJiBjdXJyZW50U2hlZXQubmV4dFVybCAmJiAhY3VycmVudFNoZWV0LmlzTG9hZGluZykge1xuXHRcdFx0XHR2YXIgcGF5bG9hZCA9IHtcblx0XHRcdFx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcdFx0XHRcIkF1dGhvcml6YXRpb25cIjogc3RhdGUudXNlcmRhdGEudXNlcklkXG5cdFx0XHRcdFx0fVxuXHRcdFx0XHR9O1xuXHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdcIiB9KVxuXHRcdFx0XHR4aHIuZ2V0KGN1cnJlbnRTaGVldC5uZXh0VXJsLCBwYXlsb2FkLCBmdW5jdGlvbiAoZXJyLCByZXNwLCBib2R5KSB7XG5cdFx0XHRcdFx0aWYgKGVycikge1xuXHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlcnIgfSlcblx0XHRcdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRcdFx0dHJ5IHtcblx0XHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX1NVQ0NFRURFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBkYXRhOiBKU09OLnBhcnNlKGJvZHkpfSk7XG5cdFx0XHRcdFx0XHR9IGNhdGNoKGUpIHtcblx0XHRcdFx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX0VSUk9SXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIGVycm9yOiBlIH0pXG5cdFx0XHRcdFx0XHR9XG5cdFx0XHRcdFx0fVxuXHRcdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIkNPTExFQ1RJT05fSVRFTVNfTE9BRElOR19GSU5JU0hFRFwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9ufSlcblx0XHRcdFx0fSk7XG5cdFx0XHR9XG5cdFx0fSk7XG5cdH0sXG5cblx0b25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlOiAoY29sbGVjdGlvbiwgdmFsdWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiTUFQX0NPTExFQ1RJT05fQVJDSEVUWVBFXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHZhbHVlOiB2YWx1ZX0pLFxuXG5cdG9uQ29uZmlybUNvbGxlY3Rpb25BcmNoZXR5cGVNYXBwaW5nczogKCkgPT4ge1xuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkNPTkZJUk1fQ09MTEVDVElPTl9BUkNIRVRZUEVfTUFQUElOR1NcIn0pXG5cdFx0c3RvcmUuZGlzcGF0Y2goZnVuY3Rpb24gKGRpc3BhdGNoLCBnZXRTdGF0ZSkge1xuXHRcdFx0bGV0IHN0YXRlID0gZ2V0U3RhdGUoKTtcblx0XHRcdGFjdGlvbnMub25TZWxlY3RDb2xsZWN0aW9uKHN0YXRlLmltcG9ydERhdGEuYWN0aXZlQ29sbGVjdGlvbik7XG5cdFx0fSlcblx0fSxcblxuXHRvblNldEZpZWxkTWFwcGluZzogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQsIGltcG9ydGVkRmllbGQpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0ZJRUxEX01BUFBJTkdcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgaW1wb3J0ZWRGaWVsZDogaW1wb3J0ZWRGaWVsZH0pLFxuXG5cdG9uQ2xlYXJGaWVsZE1hcHBpbmc6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkLCBjbGVhckluZGV4KSA9PlxuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkNMRUFSX0ZJRUxEX01BUFBJTkdcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgY2xlYXJJbmRleDogY2xlYXJJbmRleH0pLFxuXG5cdG9uU2V0RGVmYXVsdFZhbHVlOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCwgdmFsdWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RFRkFVTFRfVkFMVUVcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZCwgdmFsdWU6IHZhbHVlfSksXG5cblx0b25Db25maXJtRmllbGRNYXBwaW5nczogKGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiQ09ORklSTV9GSUVMRF9NQVBQSU5HU1wiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eUZpZWxkfSksXG5cblx0b25VbmNvbmZpcm1GaWVsZE1hcHBpbmdzOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCkgPT5cblx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJVTkNPTkZJUk1fRklFTERfTUFQUElOR1NcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlGaWVsZH0pLFxuXG5cdG9uU2V0VmFsdWVNYXBwaW5nOiAoY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZCwgdGltVmFsdWUsIG1hcFZhbHVlKSA9PlxuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNFVF9WQUxVRV9NQVBQSU5HXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHByb3BlcnR5RmllbGQ6IHByb3BlcnR5RmllbGQsIHRpbVZhbHVlOiB0aW1WYWx1ZSwgbWFwVmFsdWU6IG1hcFZhbHVlfSksXG5cblx0b25JZ25vcmVDb2x1bW5Ub2dnbGU6IChjb2xsZWN0aW9uLCB2YXJpYWJsZU5hbWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiVE9HR0xFX0lHTk9SRURfQ09MVU1OXCIsIGNvbGxlY3Rpb246IGNvbGxlY3Rpb24sIHZhcmlhYmxlTmFtZTogdmFyaWFibGVOYW1lfSksXG5cblx0b25BZGRDdXN0b21Qcm9wZXJ0eTogKGNvbGxlY3Rpb24sIHByb3BlcnR5TmFtZSwgcHJvcGVydHlUeXBlKSA9PlxuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkFERF9DVVNUT01fUFJPUEVSVFlcIiwgY29sbGVjdGlvbjogY29sbGVjdGlvbiwgcHJvcGVydHlGaWVsZDogcHJvcGVydHlOYW1lLCBwcm9wZXJ0eVR5cGU6IHByb3BlcnR5VHlwZX0pLFxuXG5cdG9uUmVtb3ZlQ3VzdG9tUHJvcGVydHk6IChjb2xsZWN0aW9uLCBwcm9wZXJ0eU5hbWUpID0+XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiUkVNT1ZFX0NVU1RPTV9QUk9QRVJUWVwiLCBjb2xsZWN0aW9uOiBjb2xsZWN0aW9uLCBwcm9wZXJ0eUZpZWxkOiBwcm9wZXJ0eU5hbWV9KSxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGFjdGlvbnM7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBBcmNoZXR5cGVNYXBwaW5ncyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblxuXHRyZW5kZXIoKSB7XG5cblx0XHRjb25zdCB7IGFyY2hldHlwZSwgaW1wb3J0RGF0YSwgb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlLCBtYXBwaW5ncywgY29sbGVjdGlvbnNBcmVNYXBwZWQsIG9uQ29uZmlybUNvbGxlY3Rpb25BcmNoZXR5cGVNYXBwaW5ncyB9ID0gdGhpcy5wcm9wcztcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJyb3cgY2VudGVyZWQtZm9ybSBjZW50ZXItYmxvY2tcIj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgY29sLW1kLTEyXCIgc3R5bGU9e3t0ZXh0QWxpZ246IFwibGVmdFwifX0+XG5cdFx0XHRcdFx0PG1haW4+XG5cdFx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsIHBhbmVsLWRlZmF1bHQgY29sLW1kLTYgY29sLW1kLW9mZnNldC0zXCI+XG5cdFx0XHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicGFuZWwtYm9keVwiPlxuXHRcdFx0XHRcdFx0XHRcdFdlIGZvdW5kIHtpbXBvcnREYXRhLnNoZWV0cy5sZW5ndGh9IGNvbGxlY3Rpb25zIGluIHRoZSBmaWxlLjxiciAvPlxuXHRcdFx0XHRcdFx0XHRcdENvbm5lY3QgdGhlIHRhYnMgdG8gdGhlIHRpbWJ1Y3RvbyBhcmNoZXR5cGVzXG5cdFx0XHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdFx0XHQ8dWwgY2xhc3NOYW1lPVwibGlzdC1ncm91cFwiPlxuXHRcdFx0XHRcdFx0XHRcdHtpbXBvcnREYXRhLnNoZWV0cy5tYXAoKHNoZWV0LCBpKSA9PiAoXG5cdFx0XHRcdFx0XHRcdFx0XHQ8bGkgY2xhc3NOYW1lPVwibGlzdC1ncm91cC1pdGVtXCIga2V5PXtpfT5cblx0XHRcdFx0XHRcdFx0XHRcdFx0PGxhYmVsPntpICsgMX0ge3NoZWV0LmNvbGxlY3Rpb259PC9sYWJlbD5cblx0XHRcdFx0XHRcdFx0XHRcdFx0PFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0XHRcdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHNoZWV0LmNvbGxlY3Rpb24sIHZhbHVlKX1cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbk1hcENvbGxlY3Rpb25BcmNoZXR5cGUoc2hlZXQuY29sbGVjdGlvbiwgbnVsbCkgfVxuXHRcdFx0XHRcdFx0XHRcdFx0XHRcdG9wdGlvbnM9e09iamVjdC5rZXlzKGFyY2hldHlwZSkuZmlsdGVyKChkb21haW4pID0+IGRvbWFpbiAhPT0gXCJyZWxhdGlvbnNcIikuc29ydCgpfVxuXHRcdFx0XHRcdFx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPXtgQXJjaGV0eXBlIGZvciAke3NoZWV0LmNvbGxlY3Rpb259YH1cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHR2YWx1ZT17bWFwcGluZ3MuY29sbGVjdGlvbnNbc2hlZXQuY29sbGVjdGlvbl0uYXJjaGV0eXBlTmFtZX0gLz5cblx0XHRcdFx0XHRcdFx0XHRcdDwvbGk+XG5cdFx0XHRcdFx0XHRcdFx0KSl9XG5cdFx0XHRcdFx0XHRcdFx0PGxpIGNsYXNzTmFtZT1cImxpc3QtZ3JvdXAtaXRlbVwiPlxuXHRcdFx0XHRcdFx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWxnIGJ0bi1zdWNjZXNzXCIgZGlzYWJsZWQ9eyFjb2xsZWN0aW9uc0FyZU1hcHBlZH0gb25DbGljaz17b25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzfT5cblx0XHRcdFx0XHRcdFx0XHRcdFx0T2tcblx0XHRcdFx0XHRcdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0XHRcdFx0XHRcdDwvbGk+XG5cdFx0XHRcdFx0XHRcdDwvdWw+XG5cdFx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHQ8L21haW4+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5BcmNoZXR5cGVNYXBwaW5ncy5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0Y29sbGVjdGlvbnNBcmVNYXBwZWQ6IFJlYWN0LlByb3BUeXBlcy5ib29sLFxuXHRpbXBvcnREYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0b25Db25maXJtQ29sbGVjdGlvbkFyY2hldHlwZU1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25NYXBDb2xsZWN0aW9uQXJjaGV0eXBlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgQXJjaGV0eXBlTWFwcGluZ3M7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQWRkUHJvcGVydHkgZnJvbSBcIi4vcHJvcGVydHktZm9ybS9hZGQtcHJvcGVydHlcIjtcbmltcG9ydCBQcm9wZXJ0eUZvcm0gZnJvbSBcIi4vcHJvcGVydHktZm9ybVwiO1xuXG5jbGFzcyBDb2xsZWN0aW9uRm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgaW1wb3J0RGF0YSwgYXJjaGV0eXBlLCBtYXBwaW5ncyB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IHsgYWN0aXZlQ29sbGVjdGlvbiwgc2hlZXRzIH0gPSBpbXBvcnREYXRhO1xuXG5cblx0XHRjb25zdCBjb2xsZWN0aW9uRGF0YSA9IHNoZWV0cy5maW5kKChzaGVldCkgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aXZlQ29sbGVjdGlvbik7XG5cdFx0Y29uc3QgbWFwcGluZ0RhdGEgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXTtcblxuXHRcdGNvbnN0IHsgYXJjaGV0eXBlTmFtZSB9ID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbYWN0aXZlQ29sbGVjdGlvbl07XG5cdFx0Y29uc3QgYXJjaGV0eXBlRmllbGRzID0gYXJjaGV0eXBlTmFtZSA/IGFyY2hldHlwZVthcmNoZXR5cGVOYW1lXSA6IFtdO1xuXHRcdGNvbnN0IGFyY2hlVHlwZVByb3BGaWVsZHMgPSBhcmNoZXR5cGVGaWVsZHMuZmlsdGVyKChhZikgPT4gYWYudHlwZSAhPT0gXCJyZWxhdGlvblwiKTtcblxuXHRcdGNvbnN0IHByb3BlcnR5Rm9ybXMgPSBhcmNoZVR5cGVQcm9wRmllbGRzXG5cdFx0XHQubWFwKChhZiwgaSkgPT4gPFByb3BlcnR5Rm9ybSB7Li4udGhpcy5wcm9wc30gY29sbGVjdGlvbkRhdGE9e2NvbGxlY3Rpb25EYXRhfSBtYXBwaW5nRGF0YT17bWFwcGluZ0RhdGF9IGN1c3RvbT17ZmFsc2V9IGtleT17aX0gbmFtZT17YWYubmFtZX0gdHlwZT17YWYudHlwZX0gLz4pO1xuXG5cdFx0Y29uc3QgY3VzdG9tUHJvcGVydHlGb3JtcyA9IG1hcHBpbmdzLmNvbGxlY3Rpb25zW2FjdGl2ZUNvbGxlY3Rpb25dLmN1c3RvbVByb3BlcnRpZXNcblx0XHRcdC5tYXAoKGNmLCBpKSA9PiA8UHJvcGVydHlGb3JtIHsuLi50aGlzLnByb3BzfSBjb2xsZWN0aW9uRGF0YT17Y29sbGVjdGlvbkRhdGF9IG1hcHBpbmdEYXRhPXttYXBwaW5nRGF0YX0gY3VzdG9tPXt0cnVlfSBrZXk9e2l9IG5hbWU9e2NmLm5hbWV9IHR5cGU9e2NmLnR5cGV9IC8+KTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsIHBhbmVsLWRlZmF1bHRcIj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJwYW5lbC1oZWFkaW5nXCI+XG5cdFx0XHRcdFx0Q29sbGVjdGlvbiBzZXR0aW5nczoge2FjdGl2ZUNvbGxlY3Rpb259XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDx1bCBjbGFzc05hbWU9XCJsaXN0LWdyb3VwXCI+XG5cdFx0XHRcdFx0e3Byb3BlcnR5Rm9ybXN9XG5cdFx0XHRcdFx0e2N1c3RvbVByb3BlcnR5Rm9ybXN9XG5cdFx0XHRcdFx0PEFkZFByb3BlcnR5IHsuLi50aGlzLnByb3BzfSAvPlxuXHRcdFx0XHQ8L3VsPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5Db2xsZWN0aW9uRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0aW1wb3J0RGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3Rcbn07XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25Gb3JtO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIENvbGxlY3Rpb25JbmRleCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0bWFwcGluZ3NBcmVDb21wbGV0ZShzaGVldCkge1xuXHRcdGNvbnN0IHsgbWFwcGluZ3MgfSA9IHRoaXMucHJvcHM7XG5cblx0XHRjb25zdCBjb25maXJtZWRDb2xDb3VudCA9IG1hcHBpbmdzLmNvbGxlY3Rpb25zW3NoZWV0LmNvbGxlY3Rpb25dLm1hcHBpbmdzXG5cdFx0XHQuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcblx0XHRcdC5tYXAoKG0pID0+IG0udmFyaWFibGUubWFwKCh2KSA9PiB2LnZhcmlhYmxlTmFtZSkpXG5cdFx0XHQucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pXG5cdFx0XHQuZmlsdGVyKCh4LCBpZHgsIHNlbGYpID0+IHNlbGYuaW5kZXhPZih4KSA9PT0gaWR4KVxuXHRcdFx0Lmxlbmd0aDtcblxuXHRcdHJldHVybiBjb25maXJtZWRDb2xDb3VudCArIG1hcHBpbmdzLmNvbGxlY3Rpb25zW3NoZWV0LmNvbGxlY3Rpb25dLmlnbm9yZWRDb2x1bW5zLmxlbmd0aCA9PT0gc2hlZXQudmFyaWFibGVzLmxlbmd0aDtcblx0fVxuXG5cdGFsbE1hcHBpbmdzQXJlSW5jb21wbGV0ZSgpIHtcblx0XHRjb25zdCB7IGltcG9ydERhdGEgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBzaGVldHMgfSA9IGltcG9ydERhdGE7XG5cdFx0cmV0dXJuIHNoZWV0c1xuXHRcdFx0Lm1hcCgoc2hlZXQpID0+IHRoaXMubWFwcGluZ3NBcmVDb21wbGV0ZShzaGVldCkpXG5cdFx0XHQuZmlsdGVyKChyZXN1bHQpID0+IHJlc3VsdCAhPT0gdHJ1ZSlcblx0XHRcdC5sZW5ndGggPT09IDA7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvblNhdmVNYXBwaW5ncywgb25QdWJsaXNoRGF0YSwgaW1wb3J0RGF0YSwgb25TZWxlY3RDb2xsZWN0aW9uIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgc2hlZXRzIH0gPSBpbXBvcnREYXRhO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicGFuZWwgcGFuZWwtZGVmYXVsdFwiPlxuXHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsLWhlYWRpbmdcIj5cblx0XHRcdFx0XHRDb2xsZWN0aW9uc1xuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJsaXN0LWdyb3VwXCI+XG5cdFx0XHRcdFx0eyBzaGVldHMubWFwKChzaGVldCwgaSkgPT4gKFxuXHRcdFx0XHRcdFx0PGFcblx0XHRcdFx0XHRcdFx0Y2xhc3NOYW1lPXtjeChcImxpc3QtZ3JvdXAtaXRlbVwiLCB7YWN0aXZlOiBzaGVldC5jb2xsZWN0aW9uID09PSBpbXBvcnREYXRhLmFjdGl2ZUNvbGxlY3Rpb24gfSl9XG5cdFx0XHRcdFx0XHRcdGtleT17aX1cblx0XHRcdFx0XHRcdFx0b25DbGljaz17KCkgPT4gb25TZWxlY3RDb2xsZWN0aW9uKHNoZWV0LmNvbGxlY3Rpb24pfVxuXHRcdFx0XHRcdFx0PlxuXHRcdFx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9e2N4KFwiZ2x5cGhpY29uXCIsIFwicHVsbC1yaWdodFwiLCB7XG5cdFx0XHRcdFx0XHRcdFx0XCJnbHlwaGljb24tcXVlc3Rpb24tc2lnblwiOiAhdGhpcy5tYXBwaW5nc0FyZUNvbXBsZXRlKHNoZWV0KSxcblx0XHRcdFx0XHRcdFx0XHRcImdseXBoaWNvbi1vay1zaWduXCI6IHRoaXMubWFwcGluZ3NBcmVDb21wbGV0ZShzaGVldClcblx0XHRcdFx0XHRcdFx0fSl9PlxuXHRcdFx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdFx0XHRcdHtzaGVldC5jb2xsZWN0aW9ufVxuXHRcdFx0XHRcdFx0PC9hPlxuXHRcdFx0XHRcdCkpIH1cblx0XHRcdFx0XHQ8bGkgY2xhc3NOYW1lPVwibGlzdC1ncm91cC1pdGVtXCI+XG5cdFx0XHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tc3VjY2Vzc1wiIG9uQ2xpY2s9e29uU2F2ZU1hcHBpbmdzfT5TYXZlPC9idXR0b24+XG5cdFx0XHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1zdWNjZXNzXCIgb25DbGljaz17b25QdWJsaXNoRGF0YX0gZGlzYWJsZWQ9eyF0aGlzLmFsbE1hcHBpbmdzQXJlSW5jb21wbGV0ZSgpfT5QdWJsaXNoPC9idXR0b24+XG5cdFx0XHRcdFx0PC9saT5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkNvbGxlY3Rpb25JbmRleC5wcm9wVHlwZXMgPSB7XG5cdG9uU2F2ZU1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25QdWJsaXNoRGF0YTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdGltcG9ydERhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRvblNlbGVjdENvbGxlY3Rpb246IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uSW5kZXg7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgRGF0YVJvdyBmcm9tIFwiLi90YWJsZS9kYXRhLXJvd1wiO1xuaW1wb3J0IEhlYWRlckNlbGwgZnJvbSBcIi4vdGFibGUvaGVhZGVyLWNlbGxcIjtcblxuXG5jbGFzcyBDb2xsZWN0aW9uVGFibGUgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGltcG9ydERhdGEsIG1hcHBpbmdzLCBvbklnbm9yZUNvbHVtblRvZ2dsZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IHNoZWV0cywgYWN0aXZlQ29sbGVjdGlvbiB9ID0gaW1wb3J0RGF0YTtcblx0XHRjb25zdCBjb2xsZWN0aW9uRGF0YSA9IHNoZWV0cy5maW5kKChzaGVldCkgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aXZlQ29sbGVjdGlvbik7XG5cblx0XHRjb25zdCB7IHJvd3MsIGNvbGxlY3Rpb24sIHZhcmlhYmxlcyB9ID0gY29sbGVjdGlvbkRhdGE7XG5cblx0XHRjb25zdCBjb25maXJtZWRDb2xzID0gdmFyaWFibGVzXG5cdFx0XHQubWFwKCh2YWx1ZSwgaSkgPT4gKHt2YWx1ZTogdmFsdWUsIGluZGV4OiBpfSkpXG5cdFx0XHQuZmlsdGVyKChjb2xTcGVjKSA9PiBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXS5tYXBwaW5nc1xuXHRcdFx0XHQuZmlsdGVyKChtKSA9PiBtLmNvbmZpcm1lZClcblx0XHRcdFx0Lm1hcCgobSkgPT4gbS52YXJpYWJsZS5tYXAoKHYpID0+IHYudmFyaWFibGVOYW1lKSlcblx0XHRcdFx0LnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKVxuXHRcdFx0XHQuaW5kZXhPZihjb2xTcGVjLnZhbHVlKSA+IC0xXG5cdFx0XHQpLm1hcCgoY29sU3BlYykgPT4gY29sU3BlYy5pbmRleCk7XG5cblx0XHRjb25zdCB7IGlnbm9yZWRDb2x1bW5zIH0gPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInBhbmVsIHBhbmVsLWRlZmF1bHRcIj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJwYW5lbC1oZWFkaW5nXCI+XG5cdFx0XHRcdFx0Q29sbGVjdGlvbjoge2NvbGxlY3Rpb259XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDx0YWJsZSBjbGFzc05hbWU9XCJ0YWJsZSB0YWJsZS1ib3JkZXJlZFwiPlxuXHRcdFx0XHRcdDx0aGVhZD5cblx0XHRcdFx0XHRcdDx0cj5cblx0XHRcdFx0XHRcdFx0e3ZhcmlhYmxlcy5tYXAoKGhlYWRlciwgaSkgPT4gKFxuXHRcdFx0XHRcdFx0XHRcdDxIZWFkZXJDZWxsXG5cdFx0XHRcdFx0XHRcdFx0XHRhY3RpdmVDb2xsZWN0aW9uPXthY3RpdmVDb2xsZWN0aW9ufVxuXHRcdFx0XHRcdFx0XHRcdFx0aGVhZGVyPXtoZWFkZXJ9XG5cdFx0XHRcdFx0XHRcdFx0XHRpc0NvbmZpcm1lZD17Y29uZmlybWVkQ29scy5pbmRleE9mKGkpID4gLTF9XG5cdFx0XHRcdFx0XHRcdFx0XHRpc0lnbm9yZWQ9e2lnbm9yZWRDb2x1bW5zLmluZGV4T2YoaGVhZGVyKSA+IC0xfVxuXHRcdFx0XHRcdFx0XHRcdFx0a2V5PXtpfVxuXHRcdFx0XHRcdFx0XHRcdFx0b25JZ25vcmVDb2x1bW5Ub2dnbGU9e29uSWdub3JlQ29sdW1uVG9nZ2xlfVxuXHRcdFx0XHRcdFx0XHRcdC8+XG5cdFx0XHRcdFx0XHRcdCkpfVxuXHRcdFx0XHRcdFx0PC90cj5cblx0XHRcdFx0XHQ8L3RoZWFkPlxuXHRcdFx0XHRcdDx0Ym9keT5cblx0XHRcdFx0XHR7IHJvd3MubWFwKChyb3csIGkpID0+IChcblx0XHRcdFx0XHRcdDxEYXRhUm93XG5cdFx0XHRcdFx0XHRcdGNvbmZpcm1lZENvbHM9e2NvbmZpcm1lZENvbHN9XG5cdFx0XHRcdFx0XHRcdGlnbm9yZWRDb2x1bW5zPXtpZ25vcmVkQ29sdW1uc31cblx0XHRcdFx0XHRcdFx0a2V5PXtpfVxuXHRcdFx0XHRcdFx0XHRyb3c9e3Jvd31cblx0XHRcdFx0XHRcdFx0dmFyaWFibGVzPXt2YXJpYWJsZXN9XG5cdFx0XHRcdFx0XHQvPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHRcdDwvdGJvZHk+XG5cdFx0XHRcdDwvdGFibGU+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkNvbGxlY3Rpb25UYWJsZS5wcm9wVHlwZXMgPSB7XG5cdGltcG9ydERhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRvbklnbm9yZUNvbHVtblRvZ2dsZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25UYWJsZTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgQ29sbGVjdGlvbkluZGV4IGZyb20gXCIuL2NvbGxlY3Rpb24taW5kZXhcIjtcbmltcG9ydCBDb2xsZWN0aW9uVGFibGUgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJsZVwiO1xuaW1wb3J0IENvbGxlY3Rpb25Gb3JtIGZyb20gXCIuL2NvbGxlY3Rpb24tZm9ybVwiO1xuXG5jbGFzcyBEYXRhc2hlZXRNYXBwaW5ncyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJyb3dcIiBzdHlsZT17e3RleHRBbGlnbjogXCJsZWZ0XCJ9fT5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgY29sLW1kLTEyXCI+XG5cdFx0XHRcdFx0PG5hdiBjbGFzc05hbWU9XCJjb2wtc20tMlwiPlxuXHRcdFx0XHRcdFx0PENvbGxlY3Rpb25JbmRleCB7Li4udGhpcy5wcm9wc30gLz5cblx0XHRcdFx0XHQ8L25hdj5cblx0XHRcdFx0XHQ8bWFpbiBjbGFzc05hbWU9XCJjb2wtc20tMTBcIj5cblx0XHRcdFx0XHRcdDxDb2xsZWN0aW9uRm9ybSB7Li4udGhpcy5wcm9wc30gLz5cblx0XHRcdFx0XHRcdDxDb2xsZWN0aW9uVGFibGUgey4uLnRoaXMucHJvcHN9IC8+XG5cdFx0XHRcdFx0PC9tYWluPlxuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgRGF0YXNoZWV0TWFwcGluZ3M7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0ge1xuXHRcdFx0aXNPcGVuOiBmYWxzZVxuXHRcdH07XG5cdFx0dGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcblx0fVxuXG5cdGNvbXBvbmVudERpZE1vdW50KCkge1xuXHRcdGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuXHR9XG5cblx0Y29tcG9uZW50V2lsbFVubW91bnQoKSB7XG5cdFx0ZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG5cdH1cblxuXHR0b2dnbGVTZWxlY3QoKSB7XG5cdFx0aWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG5cdFx0fVxuXHR9XG5cblx0aGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuXHRcdGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuXHRcdGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe1xuXHRcdFx0XHRpc09wZW46IGZhbHNlXG5cdFx0XHR9KTtcblx0XHR9XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvcHRpb25zLCBvbkNoYW5nZSwgb25DbGVhciwgcGxhY2Vob2xkZXIsIHZhbHVlIH0gPSB0aGlzLnByb3BzO1xuXG5cdFx0cmV0dXJuIChcblxuXHRcdFx0PHNwYW4gY2xhc3NOYW1lPXtjeChcImRyb3Bkb3duXCIsIHtvcGVuOiB0aGlzLnN0YXRlLmlzT3Blbn0pfT5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHQgYnRuLXN4IGRyb3Bkb3duLXRvZ2dsZVwiXG5cdFx0XHRcdFx0b25DbGljaz17dGhpcy50b2dnbGVTZWxlY3QuYmluZCh0aGlzKX1cblx0XHRcdFx0XHRzdHlsZT17dmFsdWUgPyB7Y29sb3I6IFwiIzY2NlwifSA6IHtjb2xvcjogXCIjYWFhXCJ9IH0+XG5cdFx0XHRcdFx0e3ZhbHVlIHx8IHBsYWNlaG9sZGVyfSA8c3BhbiBjbGFzc05hbWU9XCJjYXJldFwiPjwvc3Bhbj5cblx0XHRcdFx0PC9idXR0b24+XG5cblx0XHRcdFx0PHVsIGNsYXNzTmFtZT1cImRyb3Bkb3duLW1lbnVcIj5cblx0XHRcdFx0XHR7IHZhbHVlID8gKFxuXHRcdFx0XHRcdFx0PGxpPlxuXHRcdFx0XHRcdFx0XHQ8YSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2xlYXIoKTsgdGhpcy50b2dnbGVTZWxlY3QoKTt9fT5cblx0XHRcdFx0XHRcdFx0XHQtIGNsZWFyIC1cblx0XHRcdFx0XHRcdFx0PC9hPlxuXHRcdFx0XHRcdFx0PC9saT5cblx0XHRcdFx0XHQpIDogbnVsbH1cblx0XHRcdFx0XHR7b3B0aW9ucy5tYXAoKG9wdGlvbiwgaSkgPT4gKFxuXHRcdFx0XHRcdFx0PGxpIGtleT17aX0+XG5cdFx0XHRcdFx0XHRcdDxhIG9uQ2xpY2s9eygpID0+IHsgb25DaGFuZ2Uob3B0aW9uKTsgdGhpcy50b2dnbGVTZWxlY3QoKTsgfX0+e29wdGlvbn08L2E+XG5cdFx0XHRcdFx0XHQ8L2xpPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHQ8L3VsPlxuXHRcdFx0PC9zcGFuPlxuXHRcdCk7XG5cdH1cblxufVxuXG5TZWxlY3RGaWVsZC5wcm9wVHlwZXMgPSB7XG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25DbGVhcjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcblx0cGxhY2Vob2xkZXI6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdHZhbHVlOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuaW1wb3J0IFVwbG9hZFNwbGFzaFNjcmVlbiBmcm9tIFwiLi91cGxvYWQtc3BsYXNoLXNjcmVlblwiO1xuaW1wb3J0IEFyY2hldHlwZU1hcHBpbmdzIGZyb20gXCIuL2FyY2hldHlwZS1tYXBwaW5nc1wiO1xuaW1wb3J0IERhdGFzaGVldE1hcHBpbmdzIGZyb20gXCIuL2RhdGFzaGVldC1tYXBwaW5nc1wiO1xuXG5jbGFzcyBBcHAgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgaW1wb3J0RGF0YSwgbWFwcGluZ3MgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgY29sbGVjdGlvbnNBcmVNYXBwZWQgPSBPYmplY3Qua2V5cyhtYXBwaW5ncy5jb2xsZWN0aW9ucykubGVuZ3RoID4gMCAmJlxuXHRcdFx0T2JqZWN0LmtleXMobWFwcGluZ3MuY29sbGVjdGlvbnMpLm1hcCgoa2V5KSA9PiBtYXBwaW5ncy5jb2xsZWN0aW9uc1trZXldLmFyY2hldHlwZU5hbWUpLmluZGV4T2YobnVsbCkgPCAwO1xuXG5cdFx0Y29uc3QgZGF0YXNoZWV0TWFwcGluZ3MgPSBpbXBvcnREYXRhLnNoZWV0cyAmJiBjb2xsZWN0aW9uc0FyZU1hcHBlZCAmJiBtYXBwaW5ncy5jb25maXJtZWQgP1xuXHRcdFx0PERhdGFzaGVldE1hcHBpbmdzIHsuLi50aGlzLnByb3BzfSAvPiA6IG51bGw7XG5cblx0XHRjb25zdCBhcmNoZXR5cGVNYXBwaW5ncyA9ICFkYXRhc2hlZXRNYXBwaW5ncyAmJiBpbXBvcnREYXRhLnNoZWV0cyA/XG5cdFx0XHQ8QXJjaGV0eXBlTWFwcGluZ3Mgey4uLnRoaXMucHJvcHN9IGNvbGxlY3Rpb25zQXJlTWFwcGVkPXtjb2xsZWN0aW9uc0FyZU1hcHBlZH0gLz4gOiBudWxsO1xuXG5cdFx0Y29uc3QgdXBsb2FkU3BsYXNoU2NyZWVuID0gIWRhdGFzaGVldE1hcHBpbmdzICYmICFhcmNoZXR5cGVNYXBwaW5ncyA/XG5cdFx0XHQ8VXBsb2FkU3BsYXNoU2NyZWVuIHsuLi50aGlzLnByb3BzfSAvPiA6IG51bGw7XG5cblx0XHRyZXR1cm4gZGF0YXNoZWV0TWFwcGluZ3MgfHwgYXJjaGV0eXBlTWFwcGluZ3MgfHwgdXBsb2FkU3BsYXNoU2NyZWVuO1xuXHR9XG59XG5cbkFwcC5wcm9wVHlwZXMgPSB7XG5cdGltcG9ydERhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0XG59O1xuXG5leHBvcnQgZGVmYXVsdCBBcHA7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEFkZFByb3BlcnR5IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRjb25zdHJ1Y3Rvcihwcm9wcykge1xuXHRcdHN1cGVyKHByb3BzKTtcblxuXHRcdHRoaXMuc3RhdGUgPSB7XG5cdFx0XHRuZXdOYW1lOiBudWxsLFxuXHRcdFx0bmV3VHlwZTogbnVsbFxuXHRcdH07XG5cdH1cblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGltcG9ydERhdGEsIGFyY2hldHlwZTogYWxsQXJjaGV0eXBlcywgbWFwcGluZ3MsIG9uQWRkQ3VzdG9tUHJvcGVydHkgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBuZXdUeXBlLCBuZXdOYW1lIH0gPSB0aGlzLnN0YXRlO1xuXG5cdFx0Y29uc3QgeyBhY3RpdmVDb2xsZWN0aW9uIH0gPSBpbXBvcnREYXRhO1xuXG5cdFx0Y29uc3QgeyBhcmNoZXR5cGVOYW1lIH0gPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1thY3RpdmVDb2xsZWN0aW9uXTtcblx0XHRjb25zdCBhcmNoZXR5cGUgPSBhbGxBcmNoZXR5cGVzW2FyY2hldHlwZU5hbWVdO1xuXG5cdFx0Y29uc3QgYXZhaWxhYmxlQXJjaGV0eXBlcyA9IE9iamVjdC5rZXlzKG1hcHBpbmdzLmNvbGxlY3Rpb25zKS5tYXAoKGtleSkgPT4gbWFwcGluZ3MuY29sbGVjdGlvbnNba2V5XS5hcmNoZXR5cGVOYW1lKTtcblx0XHRjb25zdCByZWxhdGlvblR5cGVPcHRpb25zID0gYXJjaGV0eXBlXG5cdFx0XHQuZmlsdGVyKChwcm9wKSA9PiBwcm9wLnR5cGUgPT09IFwicmVsYXRpb25cIilcblx0XHRcdC5maWx0ZXIoKHByb3ApID0+IGF2YWlsYWJsZUFyY2hldHlwZXMuaW5kZXhPZihwcm9wLnJlbGF0aW9uLnRhcmdldENvbGxlY3Rpb24pID4gLTEpXG5cdFx0XHQubWFwKChwcm9wKSA9PiBwcm9wLm5hbWUpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxsaSBjbGFzc05hbWU9XCJsaXN0LWdyb3VwLWl0ZW1cIj5cblx0XHRcdFx0PGxhYmVsPjxzdHJvbmc+QWRkIHByb3BlcnR5PC9zdHJvbmc+PC9sYWJlbD5cblx0XHRcdFx0PFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3VHlwZTogdmFsdWUsIG5ld05hbWU6IHZhbHVlID09PSBcInJlbGF0aW9uXCIgPyBudWxsIDogbmV3TmFtZX0pfVxuXHRcdFx0XHRcdG9uQ2xlYXI9eygpID0+IHRoaXMuc2V0U3RhdGUoe25ld1R5cGU6IG51bGx9KX1cblx0XHRcdFx0XHRvcHRpb25zPXtbXCJ0ZXh0XCIsIFwicmVsYXRpb25cIl19XG5cdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJDaG9vc2UgYSB0eXBlLi4uXCJcblx0XHRcdFx0XHR2YWx1ZT17bmV3VHlwZX0gLz5cblx0XHRcdFx0Jm5ic3A7XG5cdFx0XHRcdHsgbmV3VHlwZSA9PT0gXCJyZWxhdGlvblwiID9cblx0XHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IHZhbHVlfSl9XG5cdFx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiB0aGlzLnNldFN0YXRlKHtuZXdOYW1lOiBudWxsfSl9XG5cdFx0XHRcdFx0XHRvcHRpb25zPXtyZWxhdGlvblR5cGVPcHRpb25zfVxuXHRcdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJDaG9vc2UgYSB0eXBlLi4uXCJcblx0XHRcdFx0XHRcdHZhbHVlPXtuZXdOYW1lfSAvPlxuXHRcdFx0XHRcdDpcblx0XHRcdFx0XHQoPGlucHV0IG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IGV2LnRhcmdldC52YWx1ZSB9KX0gcGxhY2Vob2xkZXI9XCJQcm9wZXJ0eSBuYW1lXCIgdmFsdWU9e25ld05hbWV9IC8+KVxuXHRcdFx0XHR9XG5cdFx0XHRcdCZuYnNwO1xuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tc3VjY2Vzc1wiIGRpc2FibGVkPXshKG5ld05hbWUgJiYgbmV3VHlwZSl9XG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4ge1xuXHRcdFx0XHRcdFx0b25BZGRDdXN0b21Qcm9wZXJ0eShhY3RpdmVDb2xsZWN0aW9uLCBuZXdOYW1lLCBuZXdUeXBlKTtcblx0XHRcdFx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld05hbWU6IG51bGwsIG5ld1R5cGU6IG51bGx9KTtcblx0XHRcdFx0XHR9fT5cblx0XHRcdFx0XHRBZGRcblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2xpPlxuXHRcdCk7XG5cdH1cbn1cblxuQWRkUHJvcGVydHkucHJvcFR5cGVzID0ge1xuXHRpbXBvcnREYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0b25BZGRDdXN0b21Qcm9wZXJ0eTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEFkZFByb3BlcnR5O1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5pbXBvcnQgTGlua3MgZnJvbSBcIi4vbGlua3NcIjtcbmltcG9ydCBUZXh0IGZyb20gXCIuL3RleHRcIjtcbmltcG9ydCBTZWxlY3QgZnJvbSBcIi4vc2VsZWN0XCI7XG5pbXBvcnQgTmFtZXMgZnJvbSBcIi4vbmFtZXNcIjtcbmltcG9ydCBSZWxhdGlvbiBmcm9tIFwiLi9yZWxhdGlvblwiO1xuXG5jb25zdCB0eXBlTWFwID0ge1xuXHR0ZXh0OiAocHJvcHMpID0+IDxUZXh0IHsuLi5wcm9wc30gLz4sXG5cdGRhdGFibGU6IChwcm9wcykgPT4gPFRleHQgey4uLnByb3BzfSAvPixcblx0bmFtZXM6IChwcm9wcykgPT4gPE5hbWVzIHsuLi5wcm9wc30gLz4sXG5cdGxpbmtzOiAocHJvcHMpID0+IDxMaW5rcyB7Li4ucHJvcHN9IC8+LFxuXHRzZWxlY3Q6IChwcm9wcykgPT4gPFNlbGVjdCB7Li4ucHJvcHN9IC8+LFxuXHRtdWx0aXNlbGVjdDogKHByb3BzKSA9PiA8U2VsZWN0IHsuLi5wcm9wc30gLz4sXG5cdHJlbGF0aW9uOiAocHJvcHMpID0+IDxSZWxhdGlvbiB7Li4ucHJvcHN9IC8+XG59O1xuXG5jbGFzcyBQcm9wZXJ0eUZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdGNhbkNvbmZpcm0odmFyaWFibGUpIHtcblx0XHRjb25zdCB7IHR5cGUgfSA9IHRoaXMucHJvcHM7XG5cdFx0aWYgKCF2YXJpYWJsZSB8fCB2YXJpYWJsZS5sZW5ndGggPT09IDApIHsgcmV0dXJuIGZhbHNlOyB9XG5cdFx0aWYgKHR5cGUgPT09IFwicmVsYXRpb25cIikge1xuXHRcdFx0cmV0dXJuIHZhcmlhYmxlWzBdLnZhcmlhYmxlTmFtZSAmJiB2YXJpYWJsZVswXS50YXJnZXRDb2xsZWN0aW9uICYmIHZhcmlhYmxlWzBdLnRhcmdldFZhcmlhYmxlTmFtZTtcblx0XHR9XG5cdFx0cmV0dXJuIHZhcmlhYmxlLmZpbHRlcigobSkgPT4gbS52YXJpYWJsZU5hbWUpLmxlbmd0aCA9PT0gdmFyaWFibGUubGVuZ3RoO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgY3VzdG9tLCBuYW1lLCBjb2xsZWN0aW9uRGF0YSwgdHlwZSwgbWFwcGluZ3MsIG9uQ29uZmlybUZpZWxkTWFwcGluZ3MsIG9uVW5jb25maXJtRmllbGRNYXBwaW5ncywgb25SZW1vdmVDdXN0b21Qcm9wZXJ0eSB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblxuXHRcdGNvbnN0IHByb3BlcnR5TWFwcGluZyA9IG1hcHBpbmcuZmluZCgobSkgPT4gbS5wcm9wZXJ0eSA9PT0gbmFtZSkgfHwge307XG5cdFx0Y29uc3QgY29uZmlybWVkID0gcHJvcGVydHlNYXBwaW5nLmNvbmZpcm1lZCB8fCBmYWxzZTtcblxuXHRcdGNvbnN0IGNvbmZpcm1CdXR0b24gPSB0aGlzLmNhbkNvbmZpcm0ocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IG51bGwpICYmICFjb25maXJtZWQgP1xuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tc3VjY2VzcyBidG4tc21cIiBvbkNsaWNrPXsoKSA9PiBvbkNvbmZpcm1GaWVsZE1hcHBpbmdzKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUpfT5Db25maXJtPC9idXR0b24+IDogY29uZmlybWVkID9cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRhbmdlciBidG4tc21cIiBvbkNsaWNrPXsoKSA9PiBvblVuY29uZmlybUZpZWxkTWFwcGluZ3MoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSl9PlVuY29uZmlybTwvYnV0dG9uPiA6IG51bGw7XG5cblxuXHRcdGNvbnN0IGZvcm1Db21wb25lbnQgPSB0eXBlTWFwW3R5cGVdKHRoaXMucHJvcHMpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxsaSBjbGFzc05hbWU9XCJsaXN0LWdyb3VwLWl0ZW1cIj5cblx0XHRcdFx0e2N1c3RvbSA/IChcblx0XHRcdFx0XHQ8YSBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0IGJ0bi1kYW5nZXIgYnRuLXhzXCIgb25DbGljaz17KCkgPT4gb25SZW1vdmVDdXN0b21Qcm9wZXJ0eShjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lKX0+XG5cdFx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdFx0PC9hPikgOiBudWxsfVxuXG5cdFx0XHRcdDxsYWJlbD48c3Ryb25nPntuYW1lfTwvc3Ryb25nPiAoe3R5cGV9KTwvbGFiZWw+XG5cdFx0XHRcdHtmb3JtQ29tcG9uZW50fVxuXHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0e2NvbmZpcm1CdXR0b259XG5cdFx0XHQ8L2xpPlxuXHRcdCk7XG5cdH1cbn1cblxuUHJvcGVydHlGb3JtLnByb3BUeXBlcyA9IHtcblx0Y29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdGN1c3RvbTogUmVhY3QuUHJvcFR5cGVzLmJvb2wsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNvbmZpcm1GaWVsZE1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25SZW1vdmVDdXN0b21Qcm9wZXJ0eTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uVW5jb25maXJtRmllbGRNYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdHR5cGU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmdcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFByb3BlcnR5Rm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBvblNldERlZmF1bHRWYWx1ZSwgbWFwcGluZ3MsIG5hbWV9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblx0XHRjb25zdCBwcm9wZXJ0eU1hcHBpbmcgPSBtYXBwaW5nLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IG5hbWUpIHx8IHt9O1xuXHRcdGNvbnN0IHNlbGVjdGVkVmFyaWFibGVVcmwgPSAocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5maW5kKCh2KSA9PiB2LmZpZWxkID09PSBcInVybFwiKSB8fCB7fTtcblx0XHRjb25zdCBkZWZhdWx0VmFsdWVVcmwgPSAocHJvcGVydHlNYXBwaW5nLmRlZmF1bHRWYWx1ZSB8fCBbXSkuZmluZCgodikgPT4gdi5maWVsZCA9PT0gXCJ1cmxcIikgfHwge307XG5cblx0XHRjb25zdCBzZWxlY3RlZFZhcmlhYmxlTGFiZWwgPSAocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5maW5kKCh2KSA9PiB2LmZpZWxkID09PSBcImxhYmVsXCIpIHx8IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRWYWx1ZUxhYmVsID0gKHByb3BlcnR5TWFwcGluZy5kZWZhdWx0VmFsdWUgfHwgW10pLmZpbmQoKHYpID0+IHYuZmllbGQgPT09IFwibGFiZWxcIikgfHwge307XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PHNwYW4+XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7Li4uc2VsZWN0ZWRWYXJpYWJsZVVybH0sIHsuLi5zZWxlY3RlZFZhcmlhYmxlTGFiZWwsIGZpZWxkOiBcImxhYmVsXCIsIHZhcmlhYmxlTmFtZTogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25DbGVhckZpZWxkTWFwcGluZyhjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCAocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5tYXAoKHYpID0+IHYuZmllbGQpLmluZGV4T2YoXCJsYWJlbFwiKSl9XG5cdFx0XHRcdFx0b3B0aW9ucz17Y29sbGVjdGlvbkRhdGEudmFyaWFibGVzfSBwbGFjZWhvbGRlcj1cIlNlbGVjdCBsYWJlbCBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtzZWxlY3RlZFZhcmlhYmxlTGFiZWwudmFyaWFibGVOYW1lIHx8IG51bGx9IC8+XG5cdFx0XHRcdCZuYnNwO1xuXG5cdFx0XHRcdHtzZWxlY3RlZFZhcmlhYmxlVXJsLnZhcmlhYmxlTmFtZSAmJiBzZWxlY3RlZFZhcmlhYmxlTGFiZWwudmFyaWFibGVOYW1lID8gKFxuXHRcdFx0XHRcdDxpbnB1dCBvbkNoYW5nZT17KGV2KSA9PiBvblNldERlZmF1bHRWYWx1ZShjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCBbey4uLmRlZmF1bHRWYWx1ZVVybH0sIHsuLi5kZWZhdWx0VmFsdWVMYWJlbCwgZmllbGQ6IFwibGFiZWxcIiwgdmFsdWU6IGV2LnRhcmdldC52YWx1ZX1dKX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiRGVmYXVsdCB2YWx1ZS4uLlwiIHR5cGU9XCJ0ZXh0XCIgdmFsdWU9e2RlZmF1bHRWYWx1ZUxhYmVsLnZhbHVlIHx8IG51bGx9IC8+KSA6IG51bGx9XG5cblx0XHRcdFx0Jm5ic3A7XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7Li4uc2VsZWN0ZWRWYXJpYWJsZVVybCwgZmllbGQ6IFwidXJsXCIsIHZhcmlhYmxlTmFtZTogdmFsdWV9LCB7Li4uc2VsZWN0ZWRWYXJpYWJsZUxhYmVsfV0pfVxuXHRcdFx0XHRcdG9uQ2xlYXI9eygpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgKHByb3BlcnR5TWFwcGluZy52YXJpYWJsZSB8fCBbXSkubWFwKCh2KSA9PiB2LmZpZWxkKS5pbmRleE9mKFwidXJsXCIpKX1cblx0XHRcdFx0XHRvcHRpb25zPXtjb2xsZWN0aW9uRGF0YS52YXJpYWJsZXN9IHBsYWNlaG9sZGVyPVwiU2VsZWN0IFVSTCBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtzZWxlY3RlZFZhcmlhYmxlVXJsLnZhcmlhYmxlTmFtZSB8fCBudWxsfSAvPlxuXHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0e3NlbGVjdGVkVmFyaWFibGVVcmwudmFyaWFibGVOYW1lICYmIHNlbGVjdGVkVmFyaWFibGVMYWJlbC52YXJpYWJsZU5hbWUgPyAoXG5cdFx0XHRcdFx0PGlucHV0IG9uQ2hhbmdlPXsoZXYpID0+IG9uU2V0RGVmYXVsdFZhbHVlKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7Li4uZGVmYXVsdFZhbHVlVXJsLCBmaWVsZDogXCJ1cmxcIiwgdmFsdWU6IGV2LnRhcmdldC52YWx1ZX0sIHsuLi5kZWZhdWx0VmFsdWVMYWJlbH1dKX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiRGVmYXVsdCB2YWx1ZS4uLlwiIHR5cGU9XCJ0ZXh0XCIgdmFsdWU9e2RlZmF1bHRWYWx1ZVVybC52YWx1ZSB8fCBudWxsfSAvPikgOiBudWxsfVxuXHRcdFx0PC9zcGFuPlxuXHRcdCk7XG5cdH1cbn1cblxuRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGNvbGxlY3Rpb25EYXRhOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRtYXBwaW5nczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DbGVhckZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uU2V0RGVmYXVsdFZhbHVlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZXRGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGb3JtOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5cbmNsYXNzIEZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdG9uQ29tcG9uZW50Q2hhbmdlKHByb3BlcnR5TWFwcGluZywgbWFwcGluZ0luZGV4LCB2YXJpYWJsZU5hbWUpIHtcblx0XHRjb25zdCB7IGNvbGxlY3Rpb25EYXRhLCBvblNldEZpZWxkTWFwcGluZywgbmFtZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB2YXJpYWJsZVNwZWMgPSBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGVcblx0XHRcdC5tYXAoKHYsIGkpID0+IGkgPT09IG1hcHBpbmdJbmRleCA/IHsuLi52LCB2YXJpYWJsZU5hbWU6IHZhcmlhYmxlTmFtZX0gOiB2KTtcblxuXHRcdGlmICh2YXJpYWJsZVNwZWMubGVuZ3RoID4gMCkge1xuXHRcdFx0b25TZXRGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgdmFyaWFibGVTcGVjKTtcblx0XHR9XG5cdH1cblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBtYXBwaW5ncywgbmFtZSwgYXJjaGV0eXBlfSA9IHRoaXMucHJvcHM7XG5cblx0XHRjb25zdCBtYXBwaW5nID0gbWFwcGluZ3MuY29sbGVjdGlvbnNbY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbl0ubWFwcGluZ3M7XG5cdFx0Y29uc3QgcHJvcGVydHlNYXBwaW5nID0gbWFwcGluZy5maW5kKChtKSA9PiBtLnByb3BlcnR5ID09PSBuYW1lKSB8fCB7fTtcblx0XHRjb25zdCBjb21wb25lbnRzID0gYXJjaGV0eXBlW21hcHBpbmdzLmNvbGxlY3Rpb25zW2NvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb25dLmFyY2hldHlwZU5hbWVdLmZpbmQoKGEpID0+IGEubmFtZSA9PT0gbmFtZSkub3B0aW9ucztcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8c3Bhbj5cblx0XHRcdFx0e3Byb3BlcnR5TWFwcGluZy52YXJpYWJsZSAmJiBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGUubGVuZ3RoID8gKFxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7bWFyZ2luQm90dG9tOiBcIjEycHhcIn19PlxuXHRcdFx0XHRcdHsocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKS5tYXAoKHYsIGkpID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17aX0gc3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXJnaW46IFwiOHB4IDhweCAwIDBcIn19PlxuXHRcdFx0XHRcdFx0XHQ8ZGl2IHN0eWxlPXt7bWFyZ2luQm90dG9tOiBcIjJweFwifX0+XG5cdFx0XHRcdFx0XHRcdFx0PGEgY2xhc3NOYW1lPVwicHVsbC1yaWdodCBidG4tZGFuZ2VyIGJ0bi14c1wiIG9uQ2xpY2s9eygpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgaSl9PlxuXHRcdFx0XHRcdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHRcdFx0XHRcdDwvYT5cblx0XHRcdFx0XHRcdFx0XHR7di5jb21wb25lbnR9Jm5ic3A7XG5cdFx0XHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiB0aGlzLm9uQ29tcG9uZW50Q2hhbmdlKHByb3BlcnR5TWFwcGluZywgaSwgdmFsdWUpfVxuXHRcdFx0XHRcdFx0XHRcdG9uQ2xlYXI9eygpID0+IG9uQ2xlYXJGaWVsZE1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgaSl9XG5cdFx0XHRcdFx0XHRcdFx0b3B0aW9ucz17Y29sbGVjdGlvbkRhdGEudmFyaWFibGVzfVxuXHRcdFx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPXtgU2VsZWN0IGNvbHVtbiBmb3IgJHt2LmNvbXBvbmVudH1gfVxuXHRcdFx0XHRcdFx0XHRcdHZhbHVlPXt2LnZhcmlhYmxlTmFtZX0gLz5cblx0XHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9kaXY+KSA6IG51bGx9XG5cblx0XHRcdFx0PFNlbGVjdEZpZWxkIG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFsuLi4ocHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlIHx8IFtdKSwge2NvbXBvbmVudDogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b3B0aW9ucz17Y29tcG9uZW50c30gcGxhY2Vob2xkZXI9XCJBZGQgbmFtZSBjb21wb25lbnQuLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtudWxsfSAvPlxuXHRcdFx0PC9zcGFuPlxuXHRcdCk7XG5cdH1cbn1cblxuRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0Y29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNsZWFyRmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZXRGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGb3JtOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5cbmNsYXNzIEZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IG93blNoZWV0ID0gdGhpcy5wcm9wcy5jb2xsZWN0aW9uRGF0YTtcblx0XHRjb25zdCBhbGxTaGVldHMgPSB0aGlzLnByb3BzLmltcG9ydERhdGEuc2hlZXRzO1xuXHRcdGNvbnN0IGFsbFNoZWV0TWFwcGluZ3MgPSB0aGlzLnByb3BzLm1hcHBpbmdzLmNvbGxlY3Rpb25zO1xuXHRcdGNvbnN0IG1hcHBpbmcgPSB0aGlzLnByb3BzLm1hcHBpbmdEYXRhLm1hcHBpbmdzLmZpbmQocHJvcCA9PiBwcm9wLnByb3BlcnR5ID09PSB0aGlzLnByb3BzLm5hbWUpO1xuXHRcdC8vYXQgb25lIHBvaW50IHRoZSBtYXBwaW5nIGRvZXMgbm90IHlldCBleGlzdHMsIGJ1dCBhIGN1c3RvbSBwcm9wZXJ0eSByZWZlcmVuY2UgY29udGFpbmluZyB0aGUgbmFtZSBkb2VzIGV4aXN0XG5cdFx0Y29uc3QgY3VzdG9tUHJvcGVydHkgPSB0aGlzLnByb3BzLm1hcHBpbmdEYXRhLmN1c3RvbVByb3BlcnRpZXMuZmluZChwcm9wID0+IHByb3AubmFtZSA9PT0gdGhpcy5wcm9wcy5uYW1lKTtcblx0XHRjb25zdCBvd25BcmNoZXR5cGUgPSB0aGlzLnByb3BzLmFyY2hldHlwZVt0aGlzLnByb3BzLm1hcHBpbmdEYXRhLmFyY2hldHlwZU5hbWVdO1xuXHRcdGNvbnN0IG9uU2V0RmllbGRNYXBwaW5nID0gdGhpcy5wcm9wcy5vblNldEZpZWxkTWFwcGluZy5iaW5kKG51bGwsIG93blNoZWV0LmNvbGxlY3Rpb24sIHRoaXMucHJvcHMubmFtZSk7XG5cdFx0Y29uc3Qgb25DbGVhckZpZWxkTWFwcGluZyA9IHRoaXMucHJvcHMub25DbGVhckZpZWxkTWFwcGluZy5iaW5kKG51bGwsIG93blNoZWV0LmNvbGxlY3Rpb24sIHRoaXMucHJvcHMubmFtZSk7XG5cblx0XHRjb25zdCByZWxhdGlvbkluZm8gPSBtYXBwaW5nICYmIG1hcHBpbmcudmFyaWFibGUgJiYgbWFwcGluZy52YXJpYWJsZS5sZW5ndGggPiAwXG5cdFx0XHQ/IG1hcHBpbmcudmFyaWFibGVbMF1cblx0XHRcdDoge307XG5cblx0XHRjb25zdCBwcm9wZXJ0eU1ldGFkYXRhID0gb3duQXJjaGV0eXBlLmZpbmQobWV0YWRhdGEgPT4gbWV0YWRhdGEubmFtZSA9PT0gKG1hcHBpbmcgPyBtYXBwaW5nLnByb3BlcnR5IDogY3VzdG9tUHJvcGVydHkubmFtZSkpO1xuXG5cdFx0Y29uc3QgYXZhaWxhYmxlU2hlZXRzID0gcHJvcGVydHlNZXRhZGF0YVxuXHRcdFx0PyBPYmplY3Qua2V5cyhhbGxTaGVldE1hcHBpbmdzKVxuXHRcdFx0XHQuZmlsdGVyKGtleSA9PiBhbGxTaGVldE1hcHBpbmdzW2tleV0uYXJjaGV0eXBlTmFtZSA9PT0gcHJvcGVydHlNZXRhZGF0YS5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uKVxuXHRcdFx0OiBbXTtcblxuXHRcdGNvbnN0IGxpbmtlZFNoZWV0ID0gcmVsYXRpb25JbmZvLnRhcmdldENvbGxlY3Rpb25cblx0XHRcdD8gYWxsU2hlZXRzXG5cdFx0XHRcdC5maW5kKHNoZWV0ID0+IHNoZWV0LmNvbGxlY3Rpb24gPT09IHJlbGF0aW9uSW5mby50YXJnZXRDb2xsZWN0aW9uKVxuXHRcdFx0OiBudWxsO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxzcGFuPlxuXHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhbey4uLnJlbGF0aW9uSW5mbywgdmFyaWFibGVOYW1lOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbkNsZWFyRmllbGRNYXBwaW5nKDApfVxuXHRcdFx0XHRcdG9wdGlvbnM9e293blNoZWV0LnZhcmlhYmxlc30gcGxhY2Vob2xkZXI9XCJTZWxlY3Qgc291cmNlIGNvbHVtbi4uLlwiXG5cdFx0XHRcdFx0dmFsdWU9e3JlbGF0aW9uSW5mby52YXJpYWJsZU5hbWUgfHwgbnVsbH0gLz5cblx0XHRcdFx0Jm5ic3A7XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKFt7Li4ucmVsYXRpb25JbmZvLCB0YXJnZXRDb2xsZWN0aW9uOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbkNsZWFyRmllbGRNYXBwaW5nKDApfVxuXHRcdFx0XHRcdG9wdGlvbnM9e2F2YWlsYWJsZVNoZWV0c30gcGxhY2Vob2xkZXI9XCJTZWxlY3QgYSB0YXJnZXQgY29sbGVjdGlvbi4uLlwiXG5cdFx0XHRcdFx0dmFsdWU9e3JlbGF0aW9uSW5mby50YXJnZXRDb2xsZWN0aW9uIHx8IG51bGx9IC8+XG5cdFx0XHRcdCZuYnNwO1xuXHRcdFx0XHR7bGlua2VkU2hlZXRcblx0XHRcdFx0XHQ/IDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhbey4uLnJlbGF0aW9uSW5mbywgdGFyZ2V0VmFyaWFibGVOYW1lOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25DbGVhckZpZWxkTWFwcGluZygwKX1cblx0XHRcdFx0XHRcdFx0b3B0aW9ucz17bGlua2VkU2hlZXQudmFyaWFibGVzfSBwbGFjZWhvbGRlcj1cIlNlbGVjdCBhIHRhcmdldCBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdFx0XHR2YWx1ZT17cmVsYXRpb25JbmZvLnRhcmdldFZhcmlhYmxlTmFtZSB8fCBudWxsfSAvPlxuXHRcdFx0XHRcdDogbnVsbFxuXHRcdFx0XHR9XG5cblx0XHRcdDwvc3Bhbj5cblx0XHQpO1xuXHR9XG59XG5cbkZvcm0ucHJvcFR5cGVzID0ge1xuXHRjb2xsZWN0aW9uRGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0aW1wb3J0RGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2xlYXJGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNldEZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBvblNldERlZmF1bHRWYWx1ZSwgb25TZXRWYWx1ZU1hcHBpbmcsIG1hcHBpbmdzLCBuYW1lLCBhcmNoZXR5cGV9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblx0XHRjb25zdCBwcm9wZXJ0eU1hcHBpbmcgPSBtYXBwaW5nLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IG5hbWUpIHx8IHt9O1xuXHRcdGNvbnN0IHNlbGVjdGVkVmFyaWFibGUgPSBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGUgJiYgcHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlLmxlbmd0aCA/IHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXSA6IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRWYWx1ZSA9IHByb3BlcnR5TWFwcGluZy5kZWZhdWx0VmFsdWUgJiYgcHJvcGVydHlNYXBwaW5nLmRlZmF1bHRWYWx1ZS5sZW5ndGggPyBwcm9wZXJ0eU1hcHBpbmcuZGVmYXVsdFZhbHVlWzBdIDoge307XG5cdFx0Y29uc3QgdmFsdWVNYXBwaW5ncyA9IHByb3BlcnR5TWFwcGluZy52YWx1ZU1hcHBpbmdzIHx8IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRPcHRpb25zID0gKGFyY2hldHlwZVttYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5hcmNoZXR5cGVOYW1lXSB8fCBbXSkuZmluZCgoYSkgPT4gYS5uYW1lID09PSBuYW1lKS5vcHRpb25zIHx8IFtdO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxzcGFuPlxuXHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvblNldEZpZWxkTWFwcGluZyhjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCBbe3ZhcmlhYmxlTmFtZTogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25DbGVhckZpZWxkTWFwcGluZyhjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uLCBuYW1lLCAwKX1cblx0XHRcdFx0XHRvcHRpb25zPXtjb2xsZWN0aW9uRGF0YS52YXJpYWJsZXN9IHBsYWNlaG9sZGVyPVwiU2VsZWN0IGEgY29sdW1uLi4uXCJcblx0XHRcdFx0XHR2YWx1ZT17c2VsZWN0ZWRWYXJpYWJsZS52YXJpYWJsZU5hbWV9IC8+XG5cdFx0XHRcdCZuYnNwO1xuXHRcdFx0XHR7c2VsZWN0ZWRWYXJpYWJsZS52YXJpYWJsZU5hbWUgPyAoPFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25TZXREZWZhdWx0VmFsdWUoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgW3t2YWx1ZTogdmFsdWV9XSl9XG5cdFx0XHRcdFx0b25DbGVhcj17KCkgPT4gb25TZXREZWZhdWx0VmFsdWUoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgW3t2YWx1ZTogbnVsbH1dKX1cblx0XHRcdFx0XHRvcHRpb25zPXtkZWZhdWx0T3B0aW9uc30gcGxhY2Vob2xkZXI9XCJTZWxlY3QgYSBkZWZhdWx0IHZhbHVlLi4uXCJcblx0XHRcdFx0XHR2YWx1ZT17ZGVmYXVsdFZhbHVlLnZhbHVlfSAvPikgOiBudWxsIH1cblxuXHRcdFx0XHR7c2VsZWN0ZWRWYXJpYWJsZS52YXJpYWJsZU5hbWUgPyAoXG5cdFx0XHRcdFx0PHVsIGNsYXNzTmFtZT1cImxpc3QtZ3JvdXBcIiBzdHlsZT17e21hcmdpblRvcDogXCIxMnB4XCIsIG1heEhlaWdodDogXCIyNzVweFwiLCBvdmVyZmxvd1k6IFwiYXV0b1wifX0+XG5cdFx0XHRcdFx0XHQ8bGkgY2xhc3NOYW1lPVwibGlzdC1ncm91cC1pdGVtXCI+PHN0cm9uZz5NYXAgaW1wb3J0IHZhbHVlcyB0byBzZWxlY3Qgb3B0aW9uczwvc3Ryb25nPjxwPiogTGVhdmUgYmxhbmsgdG8gbWF0Y2ggZXhhY3QgdmFsdWU8L3A+IDwvbGk+XG5cdFx0XHRcdFx0XHR7ZGVmYXVsdE9wdGlvbnMubWFwKChzZWxlY3RPcHRpb24sIGkpID0+IChcblx0XHRcdFx0XHRcdFx0PGxpIGNsYXNzTmFtZT1cImxpc3QtZ3JvdXAtaXRlbVwiIGtleT17aX0+XG5cdFx0XHRcdFx0XHRcdFx0PGxhYmVsPntzZWxlY3RPcHRpb259PC9sYWJlbD5cblx0XHRcdFx0XHRcdFx0XHQ8aW5wdXQgb25DaGFuZ2U9eyhldikgPT4gb25TZXRWYWx1ZU1hcHBpbmcoY29sbGVjdGlvbkRhdGEuY29sbGVjdGlvbiwgbmFtZSwgc2VsZWN0T3B0aW9uLCBldi50YXJnZXQudmFsdWUpfVxuXHRcdFx0XHRcdFx0XHRcdFx0dHlwZT1cInRleHRcIiB2YWx1ZT17dmFsdWVNYXBwaW5nc1tzZWxlY3RPcHRpb25dIHx8IFwiXCJ9IC8+XG5cdFx0XHRcdFx0XHRcdDwvbGk+XG5cdFx0XHRcdFx0XHQpKX1cblx0XHRcdFx0XHQ8L3VsPikgOiBudWxsIH1cblx0XHRcdDwvc3Bhbj5cblxuXHRcdCk7XG5cdH1cbn1cblxuRm9ybS5wcm9wVHlwZXMgPSB7XG5cdGFyY2hldHlwZTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0Y29sbGVjdGlvbkRhdGE6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG1hcHBpbmdzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNsZWFyRmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZXREZWZhdWx0VmFsdWU6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNldEZpZWxkTWFwcGluZzogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uU2V0VmFsdWVNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuXG5jbGFzcyBGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7Y29sbGVjdGlvbkRhdGEsIG9uU2V0RmllbGRNYXBwaW5nLCBvbkNsZWFyRmllbGRNYXBwaW5nLCBvblNldERlZmF1bHRWYWx1ZSwgbWFwcGluZ3MsIG5hbWV9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IG1hcHBpbmcgPSBtYXBwaW5ncy5jb2xsZWN0aW9uc1tjb2xsZWN0aW9uRGF0YS5jb2xsZWN0aW9uXS5tYXBwaW5ncztcblx0XHRjb25zdCBwcm9wZXJ0eU1hcHBpbmcgPSBtYXBwaW5nLmZpbmQoKG0pID0+IG0ucHJvcGVydHkgPT09IG5hbWUpIHx8IHt9O1xuXHRcdGNvbnN0IHNlbGVjdGVkVmFyaWFibGUgPSBwcm9wZXJ0eU1hcHBpbmcudmFyaWFibGUgJiYgcHJvcGVydHlNYXBwaW5nLnZhcmlhYmxlLmxlbmd0aCA/IHByb3BlcnR5TWFwcGluZy52YXJpYWJsZVswXSA6IHt9O1xuXHRcdGNvbnN0IGRlZmF1bHRWYWx1ZSA9IHByb3BlcnR5TWFwcGluZy5kZWZhdWx0VmFsdWUgJiYgcHJvcGVydHlNYXBwaW5nLmRlZmF1bHRWYWx1ZS5sZW5ndGggPyBwcm9wZXJ0eU1hcHBpbmcuZGVmYXVsdFZhbHVlWzBdIDoge307XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PHNwYW4+XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uU2V0RmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7dmFyaWFibGVOYW1lOiB2YWx1ZX1dKX1cblx0XHRcdFx0XHRvbkNsZWFyPXsoKSA9PiBvbkNsZWFyRmllbGRNYXBwaW5nKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIDApfVxuXHRcdFx0XHRcdG9wdGlvbnM9e2NvbGxlY3Rpb25EYXRhLnZhcmlhYmxlc30gcGxhY2Vob2xkZXI9XCJTZWxlY3QgYSBjb2x1bW4uLi5cIlxuXHRcdFx0XHRcdHZhbHVlPXtzZWxlY3RlZFZhcmlhYmxlLnZhcmlhYmxlTmFtZSB8fCBudWxsfSAvPlxuXHRcdFx0XHQmbmJzcDtcblx0XHRcdFx0PGlucHV0IG9uQ2hhbmdlPXsoZXYpID0+IG9uU2V0RGVmYXVsdFZhbHVlKGNvbGxlY3Rpb25EYXRhLmNvbGxlY3Rpb24sIG5hbWUsIFt7dmFsdWU6IGV2LnRhcmdldC52YWx1ZX1dKX1cblx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkRlZmF1bHQgdmFsdWUuLi5cIiB0eXBlPVwidGV4dFwiIHZhbHVlPXtkZWZhdWx0VmFsdWUudmFsdWUgfHwgbnVsbH0gLz5cblx0XHRcdDwvc3Bhbj5cblx0XHQpO1xuXHR9XG59XG5cbkZvcm0ucHJvcFR5cGVzID0ge1xuXHRjb2xsZWN0aW9uRGF0YTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bWFwcGluZ3M6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2xlYXJGaWVsZE1hcHBpbmc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNldERlZmF1bHRWYWx1ZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uU2V0RmllbGRNYXBwaW5nOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRm9ybTsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgRGF0YVJvdyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgcm93LCBjb25maXJtZWRDb2xzLCBpZ25vcmVkQ29sdW1ucywgdmFyaWFibGVzIH0gPSB0aGlzLnByb3BzO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDx0cj5cblx0XHRcdFx0e3Jvdy5tYXAoKGNlbGwsIGkpID0+IChcblx0XHRcdFx0XHQ8dGQgY2xhc3NOYW1lPXtjeCh7XG5cdFx0XHRcdFx0XHRpZ25vcmVkOiBjb25maXJtZWRDb2xzLmluZGV4T2YoaSkgPCAwICYmIGlnbm9yZWRDb2x1bW5zLmluZGV4T2YodmFyaWFibGVzW2ldKSA+IC0xXG5cdFx0XHRcdFx0fSl9IGtleT17aX0+XG5cdFx0XHRcdFx0XHR7Y2VsbH1cblx0XHRcdFx0XHQ8L3RkPlxuXHRcdFx0XHQpKX1cblx0XHRcdDwvdHI+XG5cdFx0KTtcblx0fVxufVxuXG5EYXRhUm93LnByb3BUeXBlcyA9IHtcblx0Y29uZmlybWVkQ29sczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LFxuXHRpZ25vcmVkQ29sdW1uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LFxuXHRyb3c6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcblx0dmFyaWFibGVzOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXlcbn07XG5cbmV4cG9ydCBkZWZhdWx0IERhdGFSb3c7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIEhlYWRlckNlbGwgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGhlYWRlciwgaXNDb25maXJtZWQsIGlzSWdub3JlZCwgYWN0aXZlQ29sbGVjdGlvbiwgb25JZ25vcmVDb2x1bW5Ub2dnbGUgfSA9IHRoaXMucHJvcHM7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PHRoIGNsYXNzTmFtZT17Y3goe1xuXHRcdFx0XHRzdWNjZXNzOiBpc0NvbmZpcm1lZCxcblx0XHRcdFx0aW5mbzogIWlzQ29uZmlybWVkICYmICFpc0lnbm9yZWQsXG5cdFx0XHRcdGlnbm9yZWQ6ICFpc0NvbmZpcm1lZCAmJiBpc0lnbm9yZWRcblx0XHRcdH0pfT5cblxuXHRcdFx0XHR7aGVhZGVyfVxuXHRcdFx0XHQ8YSBjbGFzc05hbWU9e2N4KFwicHVsbC1yaWdodFwiLCBcImdseXBoaWNvblwiLCB7XG5cdFx0XHRcdFx0XCJnbHlwaGljb24tb2stc2lnblwiOiBpc0NvbmZpcm1lZCxcblx0XHRcdFx0XHRcImdseXBoaWNvbi1xdWVzdGlvbi1zaWduXCI6ICFpc0NvbmZpcm1lZCAmJiAhaXNJZ25vcmVkLFxuXHRcdFx0XHRcdFwiZ2x5cGhpY29uLXJlbW92ZVwiOiAhaXNDb25maXJtZWQgJiYgaXNJZ25vcmVkXG5cdFx0XHRcdH0pfSBvbkNsaWNrPXsoKSA9PiAhaXNDb25maXJtZWQgPyBvbklnbm9yZUNvbHVtblRvZ2dsZShhY3RpdmVDb2xsZWN0aW9uLCBoZWFkZXIpIDogbnVsbCB9ID5cblx0XHRcdFx0PC9hPlxuXHRcdFx0PC90aD5cblx0XHQpO1xuXHR9XG59XG5cbkhlYWRlckNlbGwucHJvcFR5cGVzID0ge1xuXHRhY3RpdmVDb2xsZWN0aW9uOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRoZWFkZXI6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdGlzQ29uZmlybWVkOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbCxcblx0aXNJZ25vcmVkOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbCxcblx0b25JZ25vcmVDb2x1bW5Ub2dnbGU6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJDZWxsOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjbGFzc25hbWVzIGZyb20gJ2NsYXNzbmFtZXMnO1xuY2xhc3MgVXBsb2FkU3BsYXNoU2NyZWVuIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvblVwbG9hZEZpbGVTZWxlY3QsIHVzZXJkYXRhOiB7dXNlcklkfSwgb25Mb2dpbiwgaW1wb3J0RGF0YToge2lzVXBsb2FkaW5nfX0gPSB0aGlzLnByb3BzO1xuXG5cdFx0bGV0IHVwbG9hZEJ1dHRvbjtcblx0XHRpZiAodXNlcklkKSB7XG5cdFx0XHR1cGxvYWRCdXR0b24gPSAoXG5cdFx0XHRcdDxkaXY+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJsb2dpbi1zdWItY29tcG9uZW50IGxlYWRcIj5cblx0XHRcdFx0XHRcdDxsYWJlbCBjbGFzc05hbWU9e2NsYXNzbmFtZXMoXCJidG5cIiwgXCJidG4tbGdcIiwgXCJidG4tZGVmYXVsdFwiLCBcInVuZGVyTWFyZ2luXCIsIHtkaXNhYmxlZDogaXNVcGxvYWRpbmd9KX0+XG5cdFx0XHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tY2xvdWQtdXBsb2FkXCI+PC9zcGFuPlxuXHRcdFx0XHRcdFx0XHR7aXNVcGxvYWRpbmcgPyBcIlVwbG9hZGluZy4uLlwiIDogXCJCcm93c2VcIn1cblx0XHRcdFx0XHRcdFx0PGlucHV0XG5cdFx0XHRcdFx0XHRcdFx0ZGlzYWJsZWQ9e2lzVXBsb2FkaW5nfVxuXHRcdFx0XHRcdFx0XHRcdHR5cGU9XCJmaWxlXCJcblx0XHRcdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwibm9uZVwifX1cblx0XHRcdFx0XHRcdFx0XHRvbkNoYW5nZT17ZSA9PiBvblVwbG9hZEZpbGVTZWxlY3QoZS50YXJnZXQuZmlsZXMpfS8+XG5cdFx0XHRcdFx0XHQ8L2xhYmVsPlxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdDxwIGNsYXNzTmFtZT1cImxlYWRcIj5cblx0XHRcdFx0XHRcdERvbid0IGhhdmUgYSBkYXRhc2V0IGhhbmR5PyBIZXJl4oCZcyBhbiA8YSBocmVmPVwiL3N0YXRpYy9leGFtcGxlLnhsc3hcIj48ZW0+ZXhhbXBsZSBleGNlbCBzaGVldDwvZW0+PC9hPlxuXHRcdFx0XHRcdDwvcD5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQpO1xuXHRcdH0gZWxzZSB7XG5cdFx0XHR1cGxvYWRCdXR0b24gPSAoXG5cdFx0XHRcdDxkaXY+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJsZWFkXCI+XG5cdFx0XHRcdFx0XHQ8Zm9ybSBjbGFzc05hbWU9XCJsb2dpbi1zdWItY29tcG9uZW50XCIgYWN0aW9uPVwiaHR0cHM6Ly9zZWN1cmUuaHV5Z2Vucy5rbmF3Lm5sL3NhbWwyL2xvZ2luXCIgbWV0aG9kPVwiUE9TVFwiPlxuXHRcdFx0XHRcdFx0IFx0PGlucHV0IG5hbWU9XCJoc3VybFwiICB0eXBlPVwiaGlkZGVuXCIgdmFsdWU9e3dpbmRvdy5sb2NhdGlvbi5ocmVmfSAvPlxuXHRcdFx0XHRcdFx0IFx0PGJ1dHRvbiB0eXBlPVwic3VibWl0XCIgY2xhc3NOYW1lPVwiYnRuIGJ0bi1sZyBidG4tZGVmYXVsdCB1bmRlck1hcmdpblwiPlxuXHRcdFx0XHRcdFx0IFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWxvZy1pblwiPjwvc3Bhbj4gTG9nIGluXG5cdFx0XHRcdFx0XHQgXHQ8L2J1dHRvbj5cblx0XHRcdFx0XHRcdDwvZm9ybT5cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHQ8cCBjbGFzc05hbWU9XCJsZWFkXCI+XG5cdFx0XHRcdFx0XHRNb3N0IHVuaXZlcnNpdHkgYWNjb3VudHMgd2lsbCB3b3JrLiBZb3UgY2FuIGFsc28gbG9nIGluIHVzaW5nIGdvb2dsZSwgdHdpdHRlciBvciBmYWNlYm9vay5cblx0XHRcdFx0XHQ8L3A+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0KTtcblx0XHR9XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJzaXRlLXdyYXBwZXItaW5uZXIgIGZ1bGxzaXplX2JhY2tncm91bmRcIj5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb3Zlci1jb250YWluZXIgd2hpdGVcIj5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImlubmVyIGNvdmVyXCI+XG5cdFx0XHRcdFx0XHQ8aDEgY2xhc3NOYW1lPVwiY292ZXItaGVhZGluZyB1bmRlck1hcmdpblwiPlxuXHRcdFx0XHRcdFx0XHQ8aW1nIGFsdD1cInRpbWJ1Y3Rvb1wiIGNsYXNzTmFtZT1cImxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nb190aW1idWN0b28uc3ZnXCIvPjxiciAvPlxuXHRcdFx0XHRcdFx0XHRUSU1CVUNUT09cblx0XHRcdFx0XHRcdDwvaDE+XG5cdFx0XHRcdFx0XHQ8cCBjbGFzc05hbWU9XCJsZWFkIHVuZGVyTWFyZ2luXCI+XG5cdFx0XHRcdFx0XHRcdEdldCB5b3VyIGRhdGEgc3RvcmVkIGFuZCBjb25uZWN0ZWQgdG8gdGhlIHdvcmxkLjxiciAvPlxuXHRcdFx0XHRcdFx0XHRTdGFydCB1cGxvYWRpbmcgeW91ciBkYXRhLlxuXHRcdFx0XHRcdFx0PC9wPlxuXHRcdFx0XHRcdFx0e3VwbG9hZEJ1dHRvbn1cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cblVwbG9hZFNwbGFzaFNjcmVlbi5wcm9wVHlwZXMgPSB7XG5cdG9uVXBsb2FkOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0dXNlcmRhdGE6IFJlYWN0LlByb3BUeXBlcy5zaGFwZSh7XG5cdFx0dXNlcklkOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG4gIH0pLFxuXHRpbXBvcnREYXRhOiBSZWFjdC5Qcm9wVHlwZXMuc2hhcGUoe1xuXHRcdGlzVXBsb2FkaW5nOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbGVhblxuXHR9KVxufTtcblxuZXhwb3J0IGRlZmF1bHQgVXBsb2FkU3BsYXNoU2NyZWVuO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vYWN0aW9uc1wiO1xuaW1wb3J0IEFwcCBmcm9tIFwiLi9jb21wb25lbnRzXCI7XG5pbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcbmltcG9ydCB4aHJtb2NrIGZyb20gXCJ4aHItbW9ja1wiO1xuaW1wb3J0IHNldHVwTW9ja3MgZnJvbSBcIi4vc2VydmVybW9ja3NcIjtcblxuaWYgKHByb2Nlc3MuZW52LlVTRV9NT0NLID09PSBcInRydWVcIikge1xuXHR2YXIgb3JpZyA9IHdpbmRvdy5YTUxIdHRwUmVxdWVzdDtcblx0eGhybW9jay5zZXR1cCgpOyAvL21vY2sgd2luZG93LlhNTEh0dHBSZXF1ZXN0IHVzYWdlc1xuXHR2YXIgbW9jayA9IHdpbmRvdy5YTUxIdHRwUmVxdWVzdDtcblx0d2luZG93LlhNTEh0dHBSZXF1ZXN0ID0gb3JpZztcblx0eGhyLlhNTEh0dHBSZXF1ZXN0ID0gbW9jaztcblx0eGhyLlhEb21haW5SZXF1ZXN0ID0gbW9jaztcblx0c2V0dXBNb2Nrcyh4aHJtb2NrLCBvcmlnKTtcbn1cblxuc3RvcmUuc3Vic2NyaWJlKCgpID0+IHtcblx0dmFyIHN0YXRlID0gc3RvcmUuZ2V0U3RhdGUoKTtcblx0UmVhY3RET00ucmVuZGVyKFxuXHRcdDxBcHBcblx0XHRcdHsuLi5zdGF0ZX1cblx0XHRcdHsuLi5hY3Rpb25zfSAvPixcblx0XHRkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKVxuXHQpXG59KTtcblxuZnVuY3Rpb24gY2hlY2tUb2tlbkluVXJsKHN0YXRlKSB7XG5cdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdGxldCBwYXJhbXMgPSBwYXRoLnNwbGl0KCcmJyk7XG5cblx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoJz0nKTtcblx0XHRpZihrZXkgPT09ICdoc2lkJyAmJiAhc3RhdGUudXNlcmRhdGEudXNlcklkKSB7XG5cdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJMT0dJTlwiLCBkYXRhOiB2YWx1ZX0pO1xuXHRcdFx0YnJlYWs7XG5cdFx0fVxuXHR9XG59XG5cbmRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJET01Db250ZW50TG9hZGVkXCIsICgpID0+IHtcblx0bGV0IHN0YXRlID0gc3RvcmUuZ2V0U3RhdGUoKTtcblx0UmVhY3RET00ucmVuZGVyKFxuXHRcdDxBcHBcblx0XHRcdHsuLi5zdGF0ZX1cblx0XHRcdHsuLi5hY3Rpb25zfSAvPixcblx0XHRkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKVxuXHQpXG5cdGNoZWNrVG9rZW5JblVybChzdGF0ZSk7XG5cblx0aWYgKCFzdGF0ZS5hcmNoZXR5cGUgfHwgT2JqZWN0LmtleXMoc3RhdGUuYXJjaGV0eXBlKS5sZW5ndGggPT09IDApIHtcblx0XHR4aHIocHJvY2Vzcy5lbnYuc2VydmVyICsgXCIvdjIuMS9tZXRhZGF0YS9BZG1pblwiLCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTRVRfQVJDSEVUWVBFX01FVEFEQVRBXCIsIGRhdGE6IEpTT04ucGFyc2UocmVzcC5ib2R5KX0pO1xuXHRcdH0pO1xuXHR9XG59KTtcbiIsImltcG9ydCB7IGdldEl0ZW0gfSBmcm9tIFwiLi4vdXRpbC9wZXJzaXN0XCI7XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IGdldEl0ZW0oXCJhcmNoZXR5cGVcIikgfHwge307XG5cblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfQVJDSEVUWVBFX01FVEFEQVRBXCI6XG5cdFx0XHRyZXR1cm4gYWN0aW9uLmRhdGE7XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwiaW1wb3J0IHsgZ2V0SXRlbSB9IGZyb20gXCIuLi91dGlsL3BlcnNpc3RcIjtcbmltcG9ydCBtZXJnZSBmcm9tIFwibWVyZ2Utb3B0aW9uc1wiO1xuXG5jb25zdCBpbml0aWFsU3RhdGUgPSBnZXRJdGVtKFwiaW1wb3J0RGF0YVwiKSB8fCB7XG5cdGlzVXBsb2FkaW5nOiBmYWxzZSxcblx0c2hlZXRzOiBudWxsLFxuXHRhY3RpdmVDb2xsZWN0aW9uOiBudWxsXG59O1xuXG5mdW5jdGlvbiBmaW5kSW5kZXgoYXJyLCBmKSB7XG5cdGxldCBsZW5ndGggPSBhcnIubGVuZ3RoO1xuXHRmb3IgKHZhciBpID0gMDsgaSA8IGxlbmd0aDsgaSsrKSB7XG4gICAgaWYgKGYoYXJyW2ldLCBpLCBhcnIpKSB7XG4gICAgICByZXR1cm4gaTtcbiAgICB9XG4gIH1cblx0cmV0dXJuIC0xO1xufVxuXG5mdW5jdGlvbiBzaGVldFJvd0Zyb21EaWN0VG9BcnJheShyb3dkaWN0LCBhcnJheU9mVmFyaWFibGVOYW1lcykge1xuXHRyZXR1cm4gYXJyYXlPZlZhcmlhYmxlTmFtZXMubWFwKG5hbWUgPT4gcm93ZGljdFtuYW1lXSk7XG59XG5cbmZ1bmN0aW9uIGFkZFJvd3MoY3VyUm93cywgbmV3Um93cywgYXJyYXlPZlZhcmlhYmxlTmFtZXMpIHtcblx0cmV0dXJuIGN1clJvd3MuY29uY2F0KFxuXHRcdG5ld1Jvd3MubWFwKGl0ZW0gPT4gc2hlZXRSb3dGcm9tRGljdFRvQXJyYXkoaXRlbSwgYXJyYXlPZlZhcmlhYmxlTmFtZXMpKVxuXHQpO1xufVxuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNUQVJUX1VQTE9BRFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgaXNVcGxvYWRpbmc6IHRydWV9O1xuXHRcdGNhc2UgXCJGSU5JU0hfVVBMT0FEXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLFxuXHRcdFx0XHRzaGVldHM6IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zLm1hcChzaGVldCA9PiAoe1xuXHRcdFx0XHRcdGNvbGxlY3Rpb246IHNoZWV0Lm5hbWUsXG5cdFx0XHRcdFx0dmFyaWFibGVzOiBzaGVldC52YXJpYWJsZXMsXG5cdFx0XHRcdFx0cm93czogW10sXG5cdFx0XHRcdFx0bmV4dFVybDogc2hlZXQuZGF0YVxuXHRcdFx0XHR9KSksXG5cdFx0XHRcdGFjdGl2ZUNvbGxlY3Rpb246IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zWzBdLm5hbWUsXG5cdFx0XHRcdHZyZTogYWN0aW9uLmRhdGEudnJlLFxuXHRcdFx0XHRzYXZlTWFwcGluZ1VybDogYWN0aW9uLmRhdGEuc2F2ZU1hcHBpbmcsXG5cdFx0XHRcdGV4ZWN1dGVNYXBwaW5nVXJsOiBhY3Rpb24uZGF0YS5leGVjdXRlTWFwcGluZ1xuXHRcdFx0fTtcblx0XHRjYXNlIFwiQ09MTEVDVElPTl9JVEVNU19MT0FESU5HX1NVQ0NFRURFRFwiOlxuXHRcdFx0bGV0IHNoZWV0SWR4ID0gZmluZEluZGV4KHN0YXRlLnNoZWV0cywgc2hlZXQgPT4gc2hlZXQuY29sbGVjdGlvbiA9PT0gYWN0aW9uLmNvbGxlY3Rpb24pXG5cdFx0XHR2YXIgcmVzdWx0ID0ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0c2hlZXRzOiBbXG5cdFx0XHRcdFx0Li4uc3RhdGUuc2hlZXRzLnNsaWNlKDAsIHNoZWV0SWR4KSxcblx0XHRcdFx0XHR7XG5cdFx0XHRcdFx0XHQuLi5zdGF0ZS5zaGVldHNbc2hlZXRJZHhdLFxuXHRcdFx0XHRcdFx0cm93czogYWRkUm93cyhzdGF0ZS5zaGVldHNbc2hlZXRJZHhdLnJvd3MsIGFjdGlvbi5kYXRhLml0ZW1zLCBzdGF0ZS5zaGVldHNbc2hlZXRJZHhdLnZhcmlhYmxlcyksXG5cdFx0XHRcdFx0XHRuZXh0VXJsOiBhY3Rpb24uZGF0YS5uZXh0XG5cdFx0XHRcdFx0fSxcblx0XHRcdFx0XHQuLi5zdGF0ZS5zaGVldHMuc2xpY2Uoc2hlZXRJZHggKyAxKVxuXHRcdFx0XHRdXG5cdFx0XHR9O1xuXG5cdFx0XHRyZXR1cm4gcmVzdWx0O1xuXHRcdGNhc2UgXCJDT0xMRUNUSU9OX0lURU1TX0xPQURJTkdfRklOSVNIRURcIjpcblx0XHRcdHZhciByZXN1bHQgPSB7Li4uc3RhdGV9O1xuXHRcdFx0cmVzdWx0LnNoZWV0cyA9IHJlc3VsdC5zaGVldHMuc2xpY2UoKTtcblx0XHRcdHJlc3VsdC5zaGVldHNcblx0XHRcdFx0LmZvckVhY2goKHNoZWV0LCBpKSA9PiB7XG5cdFx0XHRcdFx0aWYgKHNoZWV0LmNvbGxlY3Rpb24gPT09IGFjdGlvbi5jb2xsZWN0aW9uKSB7XG5cdFx0XHRcdFx0XHRyZXN1bHQuc2hlZXRzW2ldID0ge1xuXHRcdFx0XHRcdFx0XHQuLi5zaGVldCxcblx0XHRcdFx0XHRcdFx0aXNMb2FkaW5nOiBmYWxzZVxuXHRcdFx0XHRcdFx0fVxuXHRcdFx0XHRcdH1cblx0XHRcdFx0fSk7XG5cblx0XHRcdHJldHVybiByZXN1bHQ7XG5cdFx0Y2FzZSBcIlNFVF9BQ1RJVkVfQ09MTEVDVElPTlwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgYWN0aXZlQ29sbGVjdGlvbjogYWN0aW9uLmNvbGxlY3Rpb259O1xuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufVxuIiwiaW1wb3J0IGltcG9ydERhdGEgZnJvbSBcIi4vaW1wb3J0LWRhdGFcIjtcbmltcG9ydCBhcmNoZXR5cGUgZnJvbSBcIi4vYXJjaGV0eXBlXCI7XG5pbXBvcnQgbWFwcGluZ3MgZnJvbSBcIi4vbWFwcGluZ3NcIjtcbmltcG9ydCB1c2VyZGF0YSBmcm9tIFwiLi91c2VyZGF0YVwiO1xuXG5leHBvcnQgZGVmYXVsdCB7XG5cdGltcG9ydERhdGE6IGltcG9ydERhdGEsXG5cdGFyY2hldHlwZTogYXJjaGV0eXBlLFxuXHRtYXBwaW5nczogbWFwcGluZ3MsXG5cdHVzZXJkYXRhOiB1c2VyZGF0YVxufTtcbiIsImltcG9ydCBzZXRJbiBmcm9tIFwiLi4vdXRpbC9zZXQtaW5cIjtcbmltcG9ydCBnZXRJbiBmcm9tIFwiLi4vdXRpbC9nZXQtaW5cIjtcbmltcG9ydCB7IGdldEl0ZW0gfSBmcm9tIFwiLi4vdXRpbC9wZXJzaXN0XCI7XG5cbmNvbnN0IG5ld1ZhcmlhYmxlRGVzYyA9IChwcm9wZXJ0eSwgdmFyaWFibGVTcGVjKSA9PiAoe1xuXHRwcm9wZXJ0eTogcHJvcGVydHksXG5cdHZhcmlhYmxlOiB2YXJpYWJsZVNwZWMsXG5cdGRlZmF1bHRWYWx1ZTogW10sXG5cdGNvbmZpcm1lZDogZmFsc2UsXG5cdHZhbHVlTWFwcGluZ3M6IHt9XG59KTtcblxuZnVuY3Rpb24gc2NhZmZvbGRDb2xsZWN0aW9uTWFwcGluZ3MoaW5pdCwgc2hlZXQpe1xuXHRyZXR1cm4gT2JqZWN0LmFzc2lnbihpbml0LCB7XG5cdFx0W3NoZWV0Lm5hbWVdOiB7XG5cdFx0XHRhcmNoZXR5cGVOYW1lOiBudWxsLFxuXHRcdFx0bWFwcGluZ3M6IFtdLFxuXHRcdFx0aWdub3JlZENvbHVtbnM6IFtdLFxuXHRcdFx0Y3VzdG9tUHJvcGVydGllczogW11cblx0XHR9XG5cdH0pO1xufVxuXG5jb25zdCBpbml0aWFsU3RhdGUgPSBnZXRJdGVtKFwibWFwcGluZ3NcIikgfHwge1xuXHRjb2xsZWN0aW9uczoge30sXG5cdGNvbmZpcm1lZDogZmFsc2Vcbn07XG5cbmNvbnN0IGdldE1hcHBpbmdJbmRleCA9IChzdGF0ZSwgYWN0aW9uKSA9PlxuXHRzdGF0ZS5jb2xsZWN0aW9uc1thY3Rpb24uY29sbGVjdGlvbl0ubWFwcGluZ3Ncblx0XHQubWFwKChtLCBpKSA9PiAoe2luZGV4OiBpLCBtOiBtfSkpXG5cdFx0LmZpbHRlcigobVNwZWMpID0+IG1TcGVjLm0ucHJvcGVydHkgPT09IGFjdGlvbi5wcm9wZXJ0eUZpZWxkKVxuXHRcdC5yZWR1Y2UoKHByZXYsIGN1cikgPT4gY3VyLmluZGV4LCAtMSk7XG5cbmNvbnN0IG1hcENvbGxlY3Rpb25BcmNoZXR5cGUgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRsZXQgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiYXJjaGV0eXBlTmFtZVwiXSwgYWN0aW9uLnZhbHVlLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBbXSwgbmV3Q29sbGVjdGlvbnMpO1xuXG5cdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCB1cHNlcnRGaWVsZE1hcHBpbmcgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblx0Y29uc3QgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHggPCAwID8gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucykubGVuZ3RoIDogZm91bmRJZHhdLFxuXHRcdG5ld1ZhcmlhYmxlRGVzYyhhY3Rpb24ucHJvcGVydHlGaWVsZCwgYWN0aW9uLmltcG9ydGVkRmllbGQpLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cblxuXHRyZXR1cm4gey4uLnN0YXRlLCBjb2xsZWN0aW9uczogbmV3Q29sbGVjdGlvbnN9O1xufTtcblxuY29uc3QgY2xlYXJGaWVsZE1hcHBpbmcgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblx0aWYgKGZvdW5kSWR4IDwgMCkgeyByZXR1cm4gc3RhdGU7IH1cblxuXHRjb25zdCBjdXJyZW50ID0gZ2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCIsIGZvdW5kSWR4LCBcInZhcmlhYmxlXCJdLCBzdGF0ZS5jb2xsZWN0aW9ucylcblx0XHQuZmlsdGVyKChtLCBpKSA9PiBpICE9PSBhY3Rpb24uY2xlYXJJbmRleCk7XG5cblx0bGV0IG5ld0NvbGxlY3Rpb25zO1xuXHRpZiAoY3VycmVudC5sZW5ndGggPiAwKSB7XG5cdFx0bmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHgsIFwidmFyaWFibGVcIl0sIGN1cnJlbnQsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblx0fSBlbHNlIHtcblx0XHRjb25zdCBuZXdNYXBwaW5ncyA9IGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpXG5cdFx0XHQuZmlsdGVyKChtLCBpKSA9PiBpICE9PSBmb3VuZElkeCk7XG5cdFx0bmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIG5ld01hcHBpbmdzLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdH1cblxuXG5cdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCBzZXREZWZhdWx0VmFsdWUgPSAoc3RhdGUsIGFjdGlvbikgPT4ge1xuXHRjb25zdCBmb3VuZElkeCA9IGdldE1hcHBpbmdJbmRleChzdGF0ZSwgYWN0aW9uKTtcblx0aWYgKGZvdW5kSWR4ID4gLTEpIHtcblx0XHRjb25zdCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiLCBmb3VuZElkeCwgXCJkZWZhdWx0VmFsdWVcIl0sIGFjdGlvbi52YWx1ZSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXHRcdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59O1xuXG5jb25zdCBzZXRGaWVsZENvbmZpcm1hdGlvbiA9IChzdGF0ZSwgYWN0aW9uLCB2YWx1ZSkgPT4ge1xuXHRjb25zdCBjdXJyZW50ID0gKGdldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJtYXBwaW5nc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpIHx8IFtdKVxuXHRcdC5tYXAoKHZtKSA9PiAoey4uLnZtLCBjb25maXJtZWQ6IGFjdGlvbi5wcm9wZXJ0eUZpZWxkID09PSB2bS5wcm9wZXJ0eSA/IHZhbHVlIDogdm0uY29uZmlybWVkfSkpO1xuXHRsZXQgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIGN1cnJlbnQsIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuXHRpZiAodmFsdWUgPT09IHRydWUpIHtcblx0XHRjb25zdCBjb25maXJtZWRWYXJpYWJsZU5hbWVzID0gY3VycmVudC5tYXAoKG0pID0+IG0udmFyaWFibGUubWFwKCh2KSA9PiB2LnZhcmlhYmxlTmFtZSkpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYikpO1xuXHRcdGNvbnN0IG5ld0lnbm9yZWRDb2x1bXMgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKVxuXHRcdFx0LmZpbHRlcigoaWMpID0+IGNvbmZpcm1lZFZhcmlhYmxlTmFtZXMuaW5kZXhPZihpYykgPCAwKTtcblx0XHRuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJpZ25vcmVkQ29sdW1uc1wiXSwgbmV3SWdub3JlZENvbHVtcywgbmV3Q29sbGVjdGlvbnMpO1xuXHR9XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmNvbnN0IHNldFZhbHVlTWFwcGluZyA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGZvdW5kSWR4ID0gZ2V0TWFwcGluZ0luZGV4KHN0YXRlLCBhY3Rpb24pO1xuXG5cdGlmIChmb3VuZElkeCA+IC0xKSB7XG5cdFx0Y29uc3QgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIiwgZm91bmRJZHgsIFwidmFsdWVNYXBwaW5nc1wiLCBhY3Rpb24udGltVmFsdWVdLFxuXHRcdFx0YWN0aW9uLm1hcFZhbHVlLCBzdGF0ZS5jb2xsZWN0aW9ucyk7XG5cdFx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcblx0fVxuXHRyZXR1cm4gc3RhdGU7XG59O1xuXG5jb25zdCB0b2dnbGVJZ25vcmVkQ29sdW1uID0gKHN0YXRlLCBhY3Rpb24pID0+IHtcblx0bGV0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiaWdub3JlZENvbHVtbnNcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKTtcblxuXHRpZiAoY3VycmVudC5pbmRleE9mKGFjdGlvbi52YXJpYWJsZU5hbWUpIDwgMCkge1xuXHRcdGN1cnJlbnQucHVzaChhY3Rpb24udmFyaWFibGVOYW1lKTtcblx0fSBlbHNlIHtcblx0XHRjdXJyZW50ID0gY3VycmVudC5maWx0ZXIoKGMpID0+IGMgIT09IGFjdGlvbi52YXJpYWJsZU5hbWUpO1xuXHR9XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJpZ25vcmVkQ29sdW1uc1wiXSwgY3VycmVudCwgc3RhdGUuY29sbGVjdGlvbnMpIH07XG59O1xuXG5jb25zdCBhZGRDdXN0b21Qcm9wZXJ0eSA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiY3VzdG9tUHJvcGVydGllc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXHRjb25zdCBuZXdDb2xsZWN0aW9ucyA9IHNldEluKFthY3Rpb24uY29sbGVjdGlvbiwgXCJjdXN0b21Qcm9wZXJ0aWVzXCIsIGN1cnJlbnQubGVuZ3RoXSwge25hbWU6IGFjdGlvbi5wcm9wZXJ0eUZpZWxkLCB0eXBlOiBhY3Rpb24ucHJvcGVydHlUeXBlfSwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXG5cdHJldHVybiB7Li4uc3RhdGUsIGNvbGxlY3Rpb25zOiBuZXdDb2xsZWN0aW9uc307XG59O1xuXG5jb25zdCByZW1vdmVDdXN0b21Qcm9wZXJ0eSA9IChzdGF0ZSwgYWN0aW9uKSA9PiB7XG5cdGNvbnN0IGZvdW5kSWR4ID0gZ2V0TWFwcGluZ0luZGV4KHN0YXRlLCBhY3Rpb24pO1xuXG5cdGNvbnN0IGN1cnJlbnQgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiY3VzdG9tUHJvcGVydGllc1wiXSwgc3RhdGUuY29sbGVjdGlvbnMpXG5cdFx0LmZpbHRlcigoY3ApID0+IGNwLm5hbWUgIT09IGFjdGlvbi5wcm9wZXJ0eUZpZWxkKTtcblxuXHRsZXQgbmV3Q29sbGVjdGlvbnMgPSBzZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwiY3VzdG9tUHJvcGVydGllc1wiXSwgY3VycmVudCwgc3RhdGUuY29sbGVjdGlvbnMpO1xuXG5cdGlmIChmb3VuZElkeCA+IC0xKSB7XG5cdFx0Y29uc3QgbmV3TWFwcGluZ3MgPSBnZXRJbihbYWN0aW9uLmNvbGxlY3Rpb24sIFwibWFwcGluZ3NcIl0sIHN0YXRlLmNvbGxlY3Rpb25zKVxuXHRcdFx0LmZpbHRlcigobSwgaSkgPT4gaSAhPT0gZm91bmRJZHgpO1xuXHRcdG5ld0NvbGxlY3Rpb25zID0gc2V0SW4oW2FjdGlvbi5jb2xsZWN0aW9uLCBcIm1hcHBpbmdzXCJdLCBuZXdNYXBwaW5ncywgbmV3Q29sbGVjdGlvbnMpO1xuXHR9XG5cblx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IG5ld0NvbGxlY3Rpb25zfTtcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiRklOSVNIX1VQTE9BRFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgY29sbGVjdGlvbnM6IGFjdGlvbi5kYXRhLmNvbGxlY3Rpb25zLnJlZHVjZShzY2FmZm9sZENvbGxlY3Rpb25NYXBwaW5ncywge30pfTtcblxuXHRcdGNhc2UgXCJNQVBfQ09MTEVDVElPTl9BUkNIRVRZUEVcIjpcblx0XHRcdHJldHVybiBtYXBDb2xsZWN0aW9uQXJjaGV0eXBlKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIkNPTkZJUk1fQ09MTEVDVElPTl9BUkNIRVRZUEVfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIGNvbmZpcm1lZDogdHJ1ZX07XG5cblx0XHRjYXNlIFwiU0VUX0ZJRUxEX01BUFBJTkdcIjpcblx0XHRcdHJldHVybiB1cHNlcnRGaWVsZE1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiQ0xFQVJfRklFTERfTUFQUElOR1wiOlxuXHRcdFx0cmV0dXJuIGNsZWFyRmllbGRNYXBwaW5nKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIlNFVF9ERUZBVUxUX1ZBTFVFXCI6XG5cdFx0XHRyZXR1cm4gc2V0RGVmYXVsdFZhbHVlKHN0YXRlLCBhY3Rpb24pO1xuXG5cdFx0Y2FzZSBcIkNPTkZJUk1fRklFTERfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiBzZXRGaWVsZENvbmZpcm1hdGlvbihzdGF0ZSwgYWN0aW9uLCB0cnVlKTtcblxuXHRcdGNhc2UgXCJVTkNPTkZJUk1fRklFTERfTUFQUElOR1NcIjpcblx0XHRcdHJldHVybiBzZXRGaWVsZENvbmZpcm1hdGlvbihzdGF0ZSwgYWN0aW9uLCBmYWxzZSk7XG5cblx0XHRjYXNlIFwiU0VUX1ZBTFVFX01BUFBJTkdcIjpcblx0XHRcdHJldHVybiBzZXRWYWx1ZU1hcHBpbmcoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiVE9HR0xFX0lHTk9SRURfQ09MVU1OXCI6XG5cdFx0XHRyZXR1cm4gdG9nZ2xlSWdub3JlZENvbHVtbihzdGF0ZSwgYWN0aW9uKTtcblxuXHRcdGNhc2UgXCJBRERfQ1VTVE9NX1BST1BFUlRZXCI6XG5cdFx0XHRyZXR1cm4gYWRkQ3VzdG9tUHJvcGVydHkoc3RhdGUsIGFjdGlvbik7XG5cblx0XHRjYXNlIFwiUkVNT1ZFX0NVU1RPTV9QUk9QRVJUWVwiOlxuXHRcdFx0cmV0dXJuIHJlbW92ZUN1c3RvbVByb3BlcnR5KHN0YXRlLCBhY3Rpb24pO1xuXHR9XG5cdHJldHVybiBzdGF0ZTtcbn1cbiIsImNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcbiAgdXNlcklkOiB1bmRlZmluZWRcbn07XG5cblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJMT0dJTlwiOlxuXHRcdFx0cmV0dXJuIHtcbiAgICAgICAgdXNlcklkOiBhY3Rpb24uZGF0YVxuICAgICAgfVxuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufVxuIiwiZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24gc2V0dXBNb2Nrcyh4aHJtb2NrLCBvcmlnKSB7XG4gIHhocm1vY2tcbiAgICAuZ2V0KFwiaHR0cDovL3Rlc3QucmVwb3NpdG9yeS5odXlnZW5zLmtuYXcubmwvdjIuMS9tZXRhZGF0YS9BZG1pblwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoYHtcbiAgICAgICAgICBcInBlcnNvbnNcIjogW1xuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJuYW1lXCIsXG4gICAgICAgICAgICAgIFwidHlwZVwiOiBcInRleHRcIlxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwiaGFzV3JpdHRlblwiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJyZWxhdGlvblwiLFxuICAgICAgICAgICAgICBcInF1aWNrc2VhcmNoXCI6IFwiL3YyLjEvZG9tYWluL2RvY3VtZW50cy9hdXRvY29tcGxldGVcIixcbiAgICAgICAgICAgICAgXCJyZWxhdGlvblwiOiB7XG4gICAgICAgICAgICAgICAgXCJkaXJlY3Rpb25cIjogXCJPVVRcIixcbiAgICAgICAgICAgICAgICBcIm91dE5hbWVcIjogXCJoYXNXcml0dGVuXCIsXG4gICAgICAgICAgICAgICAgXCJpbk5hbWVcIjogXCJ3YXNXcml0dGVuQnlcIixcbiAgICAgICAgICAgICAgICBcInRhcmdldENvbGxlY3Rpb25cIjogXCJkb2N1bWVudHNcIixcbiAgICAgICAgICAgICAgICBcInJlbGF0aW9uQ29sbGVjdGlvblwiOiBcInJlbGF0aW9uc1wiLFxuICAgICAgICAgICAgICAgIFwicmVsYXRpb25UeXBlSWRcIjogXCJiYmExMGQzNy04NmNjLTRmMWYtYmEyZC0wMTZhZjJiMjFhYTRcIlxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAge1xuICAgICAgICAgICAgICBcIm5hbWVcIjogXCJpc1JlbGF0ZWRUb1wiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJyZWxhdGlvblwiLFxuICAgICAgICAgICAgICBcInF1aWNrc2VhcmNoXCI6IFwiL3YyLjEvZG9tYWluL3BlcnNvbnMvYXV0b2NvbXBsZXRlXCIsXG4gICAgICAgICAgICAgIFwicmVsYXRpb25cIjoge1xuICAgICAgICAgICAgICAgIFwiZGlyZWN0aW9uXCI6IFwiT1VUXCIsXG4gICAgICAgICAgICAgICAgXCJvdXROYW1lXCI6IFwiaXNSZWxhdGVkVG9cIixcbiAgICAgICAgICAgICAgICBcImluTmFtZVwiOiBcImlzUmVsYXRlZFRvXCIsXG4gICAgICAgICAgICAgICAgXCJ0YXJnZXRDb2xsZWN0aW9uXCI6IFwicGVyc29uc1wiLFxuICAgICAgICAgICAgICAgIFwicmVsYXRpb25Db2xsZWN0aW9uXCI6IFwicmVsYXRpb25zXCIsXG4gICAgICAgICAgICAgICAgXCJyZWxhdGlvblR5cGVJZFwiOiBcImNiYTEwZDM3LTg2Y2MtNGYxZi1iYTJkLTAxNmFmMmIyMWFhNVwiXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgICBdLFxuICAgICAgICAgIFwiZG9jdW1lbnRzXCI6IFtcbiAgICAgICAgICAgIHtcbiAgICAgICAgICAgICAgXCJuYW1lXCI6IFwibmFtZVwiLFxuICAgICAgICAgICAgICBcInR5cGVcIjogXCJ0ZXh0XCJcbiAgICAgICAgICAgIH1cbiAgICAgICAgICBdXG4gICAgICAgIH1gKVxuICAgIH0pXG4gICAgLnBvc3QoXCJodHRwOi8vdGVzdC5yZXBvc2l0b3J5Lmh1eWdlbnMua25hdy5ubC92Mi4xL2J1bGstdXBsb2FkXCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiYnVsay11cGxvYWRcIilcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuaGVhZGVyKFwiTG9jYXRpb25cIiwgXCI8PFRoZSBnZXQgcmF3IGRhdGEgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCIpO1xuICAgIH0pXG4gICAgLnBvc3QoXCI8PFRoZSBzYXZlIG1hcHBpbmcgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwic2F2ZSBtYXBwaW5nXCIsIHJlcS5ib2R5KCkpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDQpO1xuICAgIH0pXG4gICAgLnBvc3QoXCI8PFRoZSBleGVjdXRlIG1hcHBpbmcgdXJsIHRoYXQgdGhlIHNlcnZlciBwcm92aWRlcz4+XCIsIGZ1bmN0aW9uIChyZXEsIHJlc3ApIHtcbiAgICAgIGNvbnNvbGUubG9nKFwiZXhlY3V0ZSBtYXBwaW5nXCIsIHJlcS5ib2R5KCkpO1xuICAgICAgcmV0dXJuIHJlc3BcbiAgICAgICAgLnN0YXR1cygyMDQpO1xuICAgIH0pXG4gICAgLmdldChcIjw8VGhlIGdldCByYXcgZGF0YSB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIiwgZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5sb2coXCJnZXQgcmF3IGRhdGFcIik7XG4gICAgICByZXR1cm4gcmVzcFxuICAgICAgICAuc3RhdHVzKDIwMClcbiAgICAgICAgLmJvZHkoSlNPTi5zdHJpbmdpZnkoe1xuICAgICAgICAgIHZyZTogXCJ0aGV2cmVuYW1lXCIsXG4gICAgICAgICAgc2F2ZU1hcHBpbmc6IFwiPDxUaGUgc2F2ZSBtYXBwaW5nIHVybCB0aGF0IHRoZSBzZXJ2ZXIgcHJvdmlkZXM+PlwiLFxuICAgICAgICAgIGV4ZWN1dGVNYXBwaW5nOiBcIjw8VGhlIGV4ZWN1dGUgbWFwcGluZyB1cmwgdGhhdCB0aGUgc2VydmVyIHByb3ZpZGVzPj5cIixcbiAgICAgICAgICBjb2xsZWN0aW9uczogW1xuICAgICAgICBcdFx0e1xuICAgICAgICBcdFx0XHRuYW1lOiBcIm1vY2twZXJzb25zXCIsXG4gICAgICAgIFx0XHRcdHZhcmlhYmxlczogW1wiSURcIiwgXCJWb29ybmFhbVwiLCBcInR1c3NlbnZvZWdzZWxcIiwgXCJBY2h0ZXJuYWFtXCIsIFwiR2VzY2hyZXZlbkRvY3VtZW50XCIsIFwiR2Vub2VtZCBpblwiLCBcIklzIGdldHJvdXdkIG1ldFwiXSxcbiAgICAgICAgICAgICAgZGF0YTogXCI8PHVybCBmb3IgcGVyc29uIGRhdGE+PlwiXG4gICAgICAgIFx0XHR9LFxuICAgICAgICBcdFx0e1xuICAgICAgICBcdFx0XHRuYW1lOiBcIm1vY2tkb2N1bWVudHNcIixcbiAgICAgICAgXHRcdFx0dmFyaWFibGVzOiBbXCJ0aXRlbFwiLCBcImRhdHVtXCIsIFwicmVmZXJlbnRpZVwiLCBcInVybFwiXSxcbiAgICAgICAgICAgICAgZGF0YTogXCI8PHVybCBmb3IgZG9jdW1lbnQgZGF0YT4+XCJcbiAgICAgICAgXHRcdH1cbiAgICAgICAgXHRdXG4gICAgICAgIH0pKTtcbiAgICB9KVxuICAgIC5nZXQoXCI8PHVybCBmb3IgcGVyc29uIGRhdGE+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImdldCBwZXJzb24gaXRlbXMgZGF0YVwiKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShKU09OLnN0cmluZ2lmeSh7XG4gICAgICAgIFx0XCJuYW1lXCI6IFwic29tZUNvbGxlY3Rpb25cIixcbiAgICAgICAgXHRcInZhcmlhYmxlc1wiOiBbXCJ0aW1faWRcIiwgXCJ2YXIxXCIsIFwidmFyMlwiXSxcbiAgICAgICAgXHRcIml0ZW1zXCI6IFt7XG4gICAgICAgIFx0XHRcInRpbV9pZFwiOiBcIjFcIixcbiAgICAgICAgICAgIFwiSURcIjogXCJJRFwiLFxuICAgICAgICAgICAgXCJWb29ybmFhbVwiOiBcIlZvb3JuYWFtXCIsXG4gICAgICAgICAgICBcInR1c3NlbnZvZWdzZWxcIjogXCJ0dXNzZW52b2Vnc2VsXCIsXG4gICAgICAgICAgICBcIkFjaHRlcm5hYW1cIjogXCJBY2h0ZXJuYWFtXCIsXG4gICAgICAgICAgICBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiOiBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiLFxuICAgICAgICAgICAgXCJHZW5vZW1kIGluXCI6IFwiR2Vub2VtZCBpblwiLFxuICAgICAgICAgICAgXCJJcyBnZXRyb3V3ZCBtZXRcIjogXCJJcyBnZXRyb3V3ZCBtZXRcIixcbiAgICAgICAgXHR9LCB7XG4gICAgICAgICAgICBcInRpbV9pZFwiOiBcIjJcIixcbiAgICAgICAgICAgIFwiSURcIjogXCJJRFwiLFxuICAgICAgICAgICAgXCJWb29ybmFhbVwiOiBcIlZvb3JuYWFtXCIsXG4gICAgICAgICAgICBcInR1c3NlbnZvZWdzZWxcIjogXCJ0dXNzZW52b2Vnc2VsXCIsXG4gICAgICAgICAgICBcIkFjaHRlcm5hYW1cIjogXCJBY2h0ZXJuYWFtXCIsXG4gICAgICAgICAgICBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiOiBcIkdlc2NocmV2ZW5Eb2N1bWVudFwiLFxuICAgICAgICAgICAgXCJHZW5vZW1kIGluXCI6IFwiR2Vub2VtZCBpblwiLFxuICAgICAgICAgICAgXCJJcyBnZXRyb3V3ZCBtZXRcIjogXCJJcyBnZXRyb3V3ZCBtZXRcIixcbiAgICAgICAgXHR9XVxuICAgICAgICB9KSk7XG4gICAgfSlcbiAgICAuZ2V0KFwiPDx1cmwgZm9yIGRvY3VtZW50IGRhdGE+PlwiLCBmdW5jdGlvbiAocmVxLCByZXNwKSB7XG4gICAgICBjb25zb2xlLmxvZyhcImdldCBwZXJzb24gaXRlbXMgZGF0YVwiKTtcbiAgICAgIHJldHVybiByZXNwXG4gICAgICAgIC5zdGF0dXMoMjAwKVxuICAgICAgICAuYm9keShKU09OLnN0cmluZ2lmeSh7XG4gICAgICAgIFx0XCJuYW1lXCI6IFwic29tZUNvbGxlY3Rpb25cIixcbiAgICAgICAgXHRcInZhcmlhYmxlc1wiOiBbXCJ0aW1faWRcIiwgXCJ2YXIxXCIsIFwidmFyMlwiXSxcbiAgICAgICAgXHRcIml0ZW1zXCI6IFt7XG4gICAgICAgIFx0XHRcInRpbV9pZFwiOiBcIjFcIixcbiAgICAgICAgICAgIFwidGl0ZWxcIjogXCJ0aXRlbFwiLFxuICAgICAgICAgICAgXCJkYXR1bVwiOiBcImRhdHVtXCIsXG4gICAgICAgICAgICBcInJlZmVyZW50aWVcIjogXCJyZWZlcmVudGllXCIsXG4gICAgICAgICAgICBcInVybFwiOiBcInVybFwiLFxuICAgICAgICBcdH0sIHtcbiAgICAgICAgICAgIFwidGltX2lkXCI6IFwiMlwiLFxuICAgICAgICAgICAgXCJ0aXRlbFwiOiBcInRpdGVsXCIsXG4gICAgICAgICAgICBcImRhdHVtXCI6IFwiZGF0dW1cIixcbiAgICAgICAgICAgIFwicmVmZXJlbnRpZVwiOiBcInJlZmVyZW50aWVcIixcbiAgICAgICAgICAgIFwidXJsXCI6IFwidXJsXCIsXG4gICAgICAgIFx0fV1cbiAgICAgICAgfSkpO1xuICAgIH0pXG4gICAgLm1vY2soZnVuY3Rpb24gKHJlcSwgcmVzcCkge1xuICAgICAgY29uc29sZS5lcnJvcihcInVubW9ja2VkIHJlcXVlc3RcIiwgcmVxLnVybCgpLCByZXEsIHJlc3ApO1xuICAgIH0pXG59XG4iLCJpbXBvcnQge2NyZWF0ZVN0b3JlLCBhcHBseU1pZGRsZXdhcmUsIGNvbWJpbmVSZWR1Y2VycywgY29tcG9zZX0gZnJvbSBcInJlZHV4XCI7XG5pbXBvcnQgdGh1bmtNaWRkbGV3YXJlIGZyb20gXCJyZWR1eC10aHVua1wiO1xuXG5pbXBvcnQgeyBwZXJzaXN0IH0gZnJvbSBcIi4vdXRpbC9wZXJzaXN0XCI7XG5pbXBvcnQgcmVkdWNlcnMgZnJvbSBcIi4vcmVkdWNlcnNcIjtcblxubGV0IHN0b3JlID0gY3JlYXRlU3RvcmUoXG4gIGNvbWJpbmVSZWR1Y2VycyhyZWR1Y2VycyksXG4gIGNvbXBvc2UoXG4gICAgYXBwbHlNaWRkbGV3YXJlKFxuICAgICAgdGh1bmtNaWRkbGV3YXJlXG4gICAgKSxcbiAgICB3aW5kb3cuZGV2VG9vbHNFeHRlbnNpb24gPyB3aW5kb3cuZGV2VG9vbHNFeHRlbnNpb24oKSA6IGYgPT4gZlxuICApXG4pO1xuXG4vLyB3aW5kb3cub25iZWZvcmV1bmxvYWQgPSAoKSA9PiBwZXJzaXN0KHN0b3JlLmdldFN0YXRlKCkpO1xuXG5leHBvcnQgZGVmYXVsdCBzdG9yZTtcbiIsImZ1bmN0aW9uIGRlZXBDbG9uZTkob2JqKSB7XG4gICAgdmFyIGksIGxlbiwgcmV0O1xuXG4gICAgaWYgKHR5cGVvZiBvYmogIT09IFwib2JqZWN0XCIgfHwgb2JqID09PSBudWxsKSB7XG4gICAgICAgIHJldHVybiBvYmo7XG4gICAgfVxuXG4gICAgaWYgKEFycmF5LmlzQXJyYXkob2JqKSkge1xuICAgICAgICByZXQgPSBbXTtcbiAgICAgICAgbGVuID0gb2JqLmxlbmd0aDtcbiAgICAgICAgZm9yIChpID0gMDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgICAgICByZXQucHVzaCggKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXSApO1xuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcmV0ID0ge307XG4gICAgICAgIGZvciAoaSBpbiBvYmopIHtcbiAgICAgICAgICAgIGlmIChvYmouaGFzT3duUHJvcGVydHkoaSkpIHtcbiAgICAgICAgICAgICAgICByZXRbaV0gPSAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuICAgIHJldHVybiByZXQ7XG59XG5cbmV4cG9ydCBkZWZhdWx0IGRlZXBDbG9uZTk7IiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuY29uc3QgX2dldEluID0gKHBhdGgsIGRhdGEpID0+XG5cdGRhdGEgP1xuXHRcdHBhdGgubGVuZ3RoID09PSAwID8gZGF0YSA6IF9nZXRJbihwYXRoLCBkYXRhW3BhdGguc2hpZnQoKV0pIDpcblx0XHRudWxsO1xuXG5cblxuY29uc3QgZ2V0SW4gPSAocGF0aCwgZGF0YSkgPT5cblx0X2dldEluKGNsb25lKHBhdGgpLCBkYXRhKTtcblxuXG5leHBvcnQgZGVmYXVsdCBnZXRJbjsiLCJleHBvcnQgZGVmYXVsdCBmdW5jdGlvbiBtYXBwaW5nVG9Kc29uTGRSbWwobWFwcGluZywgdnJlKSB7XG4gIHJldHVybiB7XG4gIFx0XCJAY29udGV4dFwiOiB7XG4gIFx0XHRcIkB2b2NhYlwiOiBcImh0dHA6Ly93d3cudzMub3JnL25zL3Iycm1sI1wiLFxuICBcdFx0XCJybWxcIjogXCJodHRwOi8vc2Vtd2ViLm1tbGFiLmJlL25zL3JtbCNcIixcbiAgXHRcdFwidGltXCI6IFwiaHR0cDovL3RpbWJ1Y3Rvby5jb20vbWFwcGluZ1wiLFxuICBcdFx0XCJwcmVkaWNhdGVcIjoge1xuICBcdFx0XHRcIkB0eXBlXCI6IFwiQGlkXCJcbiAgXHRcdH1cbiAgXHR9LFxuICBcdFwiQGdyYXBoXCI6IE9iamVjdC5rZXlzKG1hcHBpbmcuY29sbGVjdGlvbnMpLm1hcChrZXkgPT4gbWFwU2hlZXQoa2V5LCBtYXBwaW5nLmNvbGxlY3Rpb25zW2tleV0sIHZyZSkpXG4gIH07XG59XG52YXIgcyA9IHtcbiAgXCJhcmNoZXR5cGVOYW1lXCI6IFwicGVyc29uc1wiLFxuICBcIm1hcHBpbmdzXCI6IFtcbiAgICB7XG4gICAgICBcInByb3BlcnR5XCI6IFwibmFtZVwiLFxuICAgICAgXCJ2YXJpYWJsZVwiOiBbXG4gICAgICAgIHtcbiAgICAgICAgICBcInZhcmlhYmxlTmFtZVwiOiBcIlZvb3JuYWFtXCJcbiAgICAgICAgfVxuICAgICAgXSxcbiAgICAgIFwiZGVmYXVsdFZhbHVlXCI6IFtdLFxuICAgICAgXCJjb25maXJtZWRcIjogdHJ1ZSxcbiAgICAgIFwidmFsdWVNYXBwaW5nc1wiOiB7fVxuICAgIH0sXG4gICAge1xuICAgICAgXCJwcm9wZXJ0eVwiOiBcIkZvb1wiLFxuICAgICAgXCJ2YXJpYWJsZVwiOiBbXG4gICAgICAgIHtcbiAgICAgICAgICBcInZhcmlhYmxlTmFtZVwiOiBcIkFjaHRlcm5hYW1cIlxuICAgICAgICB9XG4gICAgICBdLFxuICAgICAgXCJkZWZhdWx0VmFsdWVcIjogW10sXG4gICAgICBcImNvbmZpcm1lZFwiOiB0cnVlLFxuICAgICAgXCJ2YWx1ZU1hcHBpbmdzXCI6IHt9XG4gICAgfSxcbiAgICB7XG4gICAgICBcInByb3BlcnR5XCI6IFwiaXNSZWxhdGVkVG9cIixcbiAgICAgIFwidmFyaWFibGVcIjogW1xuICAgICAgICB7XG4gICAgICAgICAgXCJ2YXJpYWJsZU5hbWVcIjogXCJWb29ybmFhbVwiLFxuICAgICAgICAgIFwidGFyZ2V0Q29sbGVjdGlvblwiOiBcIm1vY2twZXJzb25zXCIsXG4gICAgICAgICAgXCJ0YXJnZXRWYXJpYWJsZU5hbWVcIjogXCJWb29ybmFhbVwiXG4gICAgICAgIH1cbiAgICAgIF0sXG4gICAgICBcImRlZmF1bHRWYWx1ZVwiOiBbXSxcbiAgICAgIFwiY29uZmlybWVkXCI6IHRydWUsXG4gICAgICBcInZhbHVlTWFwcGluZ3NcIjoge31cbiAgICB9XG4gIF1cbn1cblxuZnVuY3Rpb24gbWFrZU1hcE5hbWUobG9jYWxOYW1lKSB7XG4gIHJldHVybiBgdGltOnMvJHtsb2NhbE5hbWV9bWFwYDtcbn1cblxuZnVuY3Rpb24gbWFwU2hlZXQoa2V5LCBzaGVldCwgdnJlKSB7XG4gIC8vIGNvbnNvbGUubG9nKEpTT04uc3RyaW5naWZ5KHNoZWV0LCB1bmRlZmluZWQsIDIpKTtcbiAgLy9GSVhNRTogbW92ZSBsb2dpY2FsU291cmNlIGFuZCBzdWJqZWN0TWFwIHVuZGVyIHRoZSBjb250cm9sIG9mIHRoZSBzZXJ2ZXJcbiAgcmV0dXJuIHtcbiAgICBcIkBpZFwiOiBtYWtlTWFwTmFtZShrZXkpLFxuICAgIFwicm1sOmxvZ2ljYWxTb3VyY2VcIjoge1xuXHRcdFx0XCJybWw6c291cmNlXCI6IHtcblx0XHRcdFx0XCJ0aW06cmF3Q29sbGVjdGlvblwiOiBrZXksXG5cdFx0XHRcdFwidGltOnZyZU5hbWVcIjogdnJlXG5cdFx0XHR9XG5cdFx0fSxcbiAgICBcInN1YmplY3RNYXBcIjoge1xuXHRcdFx0XCJjbGFzc1wiOiBgaHR0cDovL3RpbWJ1Y3Rvby5jb20vJHt2cmV9LyR7a2V5fWAsXG5cdFx0XHRcInRlbXBsYXRlXCI6IGBodHRwOi8vdGltYnVjdG9vLmNvbS8ke3ZyZX0vJHtrZXl9L3t0aW1faWR9YFxuXHRcdH0sXG4gICAgXCJwcmVkaWNhdGVPYmplY3RNYXBcIjogc2hlZXQubWFwcGluZ3MubWFwKG1ha2VQcmVkaWNhdGVPYmplY3RNYXApXG4gIH07XG59XG5cbmZ1bmN0aW9uIG1ha2VQcmVkaWNhdGVPYmplY3RNYXAobWFwcGluZykge1xuICBsZXQgcHJvcGVydHkgPSBtYXBwaW5nLnByb3BlcnR5O1xuICBsZXQgdmFyaWFibGUgPSBtYXBwaW5nLnZhcmlhYmxlWzBdO1xuICBpZiAodmFyaWFibGUudGFyZ2V0Q29sbGVjdGlvbikge1xuICAgIHJldHVybiB7XG4gICAgICBcIm9iamVjdE1hcFwiOiB7XG4gICAgICAgIFwicmVmZXJlbmNlXCI6IHtcbiAgICAgICAgICBcImpvaW5Db25kaXRpb25cIjoge1xuICAgICAgICAgICAgXCJjaGlsZFwiOiB2YXJpYWJsZS52YXJpYWJsZU5hbWUsXG4gICAgICAgICAgICBcInBhcmVudFwiOiB2YXJpYWJsZS50YXJnZXRWYXJpYWJsZU5hbWVcbiAgICAgICAgICB9LFxuICAgICAgICAgIFwicGFyZW50VHJpcGxlc01hcFwiOiBtYWtlTWFwTmFtZSh2YXJpYWJsZS50YXJnZXRDb2xsZWN0aW9uKVxuICAgICAgICB9XG4gICAgICB9LFxuICAgICAgXCJwcmVkaWNhdGVcIjogYGh0dHA6Ly90aW1idWN0b28uY29tLyR7cHJvcGVydHl9YFxuICAgIH1cbiAgfSBlbHNlIHtcbiAgICByZXR1cm4ge1xuICAgICAgXCJvYmplY3RNYXBcIjoge1xuICAgICAgICBcImNvbHVtblwiOiB2YXJpYWJsZS52YXJpYWJsZU5hbWVcbiAgICAgIH0sXG4gICAgICBcInByZWRpY2F0ZVwiOiBgaHR0cDovL3RpbWJ1Y3Rvby5jb20vJHtwcm9wZXJ0eX1gXG4gICAgfVxuICB9XG59XG4iLCJsZXQgcGVyc2lzdERpc2FibGVkID0gZmFsc2U7XG5cbmNvbnN0IHBlcnNpc3QgPSAoc3RhdGUpID0+IHtcblx0aWYgKCBwZXJzaXN0RGlzYWJsZWQgKSB7IHJldHVybjsgfVxuXHRmb3IgKGxldCBrZXkgaW4gc3RhdGUpIHtcblx0XHRsb2NhbFN0b3JhZ2Uuc2V0SXRlbShrZXksIEpTT04uc3RyaW5naWZ5KHN0YXRlW2tleV0pKTtcblx0fVxufTtcblxuY29uc3QgZ2V0SXRlbSA9IChrZXkpID0+IHtcblx0aWYgKGxvY2FsU3RvcmFnZS5nZXRJdGVtKGtleSkpIHtcblx0XHRyZXR1cm4gSlNPTi5wYXJzZShsb2NhbFN0b3JhZ2UuZ2V0SXRlbShrZXkpKTtcblx0fVxuXHRyZXR1cm4gbnVsbDtcbn07XG5cbmNvbnN0IGRpc2FibGVQZXJzaXN0ID0gKCkgPT4ge1xuXHRsb2NhbFN0b3JhZ2UuY2xlYXIoKTtcblx0cGVyc2lzdERpc2FibGVkID0gdHJ1ZTtcbn07XG53aW5kb3cuZGlzYWJsZVBlcnNpc3QgPSBkaXNhYmxlUGVyc2lzdDtcblxuZXhwb3J0IHsgcGVyc2lzdCwgZ2V0SXRlbSwgZGlzYWJsZVBlcnNpc3QgfTtcbiIsImltcG9ydCBjbG9uZSBmcm9tIFwiLi9jbG9uZS1kZWVwXCI7XG5cbi8vIERvIGVpdGhlciBvZiB0aGVzZTpcbi8vICBhKSBTZXQgYSB2YWx1ZSBieSByZWZlcmVuY2UgaWYgZGVyZWYgaXMgbm90IG51bGxcbi8vICBiKSBTZXQgYSB2YWx1ZSBkaXJlY3RseSBpbiB0byBkYXRhIG9iamVjdCBpZiBkZXJlZiBpcyBudWxsXG5jb25zdCBzZXRFaXRoZXIgPSAoZGF0YSwgZGVyZWYsIGtleSwgdmFsKSA9PiB7XG5cdChkZXJlZiB8fCBkYXRhKVtrZXldID0gdmFsO1xuXHRyZXR1cm4gZGF0YTtcbn07XG5cbi8vIFNldCBhIG5lc3RlZCB2YWx1ZSBpbiBkYXRhIChub3QgdW5saWtlIGltbXV0YWJsZWpzLCBidXQgYSBjbG9uZSBvZiBkYXRhIGlzIGV4cGVjdGVkIGZvciBwcm9wZXIgaW1tdXRhYmlsaXR5KVxuY29uc3QgX3NldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA9IG51bGwpID0+XG5cdHBhdGgubGVuZ3RoID4gMSA/XG5cdFx0X3NldEluKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA/IGRlcmVmW3BhdGguc2hpZnQoKV0gOiBkYXRhW3BhdGguc2hpZnQoKV0pIDpcblx0XHRzZXRFaXRoZXIoZGF0YSwgZGVyZWYsIHBhdGhbMF0sIHZhbHVlKTtcblxuY29uc3Qgc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEpID0+XG5cdF9zZXRJbihjbG9uZShwYXRoKSwgdmFsdWUsIGNsb25lKGRhdGEpKTtcblxuZXhwb3J0IGRlZmF1bHQgc2V0SW47Il19
