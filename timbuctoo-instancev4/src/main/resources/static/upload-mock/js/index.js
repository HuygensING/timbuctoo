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
/* Built-in method references for those with the same name as other `lodash` methods. */
var nativeGetPrototype = Object.getPrototypeOf;

/**
 * Gets the `[[Prototype]]` of `value`.
 *
 * @private
 * @param {*} value The value to query.
 * @returns {null|Object} Returns the `[[Prototype]]`.
 */
function getPrototype(value) {
  return nativeGetPrototype(Object(value));
}

module.exports = getPrototype;

},{}],6:[function(require,module,exports){
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

},{}],7:[function(require,module,exports){
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

},{}],8:[function(require,module,exports){
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

},{"./_getPrototype":5,"./_isHostObject":6,"./isObjectLike":7}],9:[function(require,module,exports){
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
},{"for-each":2,"trim":21}],10:[function(require,module,exports){
// shim for using process in browser

var process = module.exports = {};
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
    var timeout = setTimeout(cleanUpNextTick);
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
    clearTimeout(timeout);
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
        setTimeout(drainQueue, 0);
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

},{}],11:[function(require,module,exports){
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
},{}],12:[function(require,module,exports){
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
},{"./compose":15}],13:[function(require,module,exports){
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
},{}],14:[function(require,module,exports){
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
},{"./createStore":16,"./utils/warning":18,"_process":10,"lodash/isPlainObject":8}],15:[function(require,module,exports){
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
},{}],16:[function(require,module,exports){
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
},{"lodash/isPlainObject":8,"symbol-observable":19}],17:[function(require,module,exports){
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
},{"./applyMiddleware":12,"./bindActionCreators":13,"./combineReducers":14,"./compose":15,"./createStore":16,"./utils/warning":18,"_process":10}],18:[function(require,module,exports){
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
},{}],19:[function(require,module,exports){
(function (global){
/* global window */
'use strict';

module.exports = require('./ponyfill')(global || window || this);

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"./ponyfill":20}],20:[function(require,module,exports){
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

},{}],21:[function(require,module,exports){

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

},{}],22:[function(require,module,exports){
"use strict";
var window = require("global/window")
var once = require("once")
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
    var callback = options.callback
    if(typeof callback === "undefined"){
        throw new Error("callback argument missing")
    }
    callback = once(callback)

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
        } else if (xhr.responseType === "text" || !xhr.responseType) {
            body = xhr.responseText || xhr.responseXML
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
        callback(evt, failureResponse)
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
        callback(err, response, response.body)

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

function noop() {}

},{"global/window":3,"is-function":4,"once":23,"parse-headers":9,"xtend":24}],23:[function(require,module,exports){
module.exports = once

once.proto = once(function () {
  Object.defineProperty(Function.prototype, 'once', {
    value: function () {
      return once(this)
    },
    configurable: true
  })
})

function once (fn) {
  var called = false
  return function () {
    if (called) return
    called = true
    return fn.apply(this, arguments)
  }
}

},{}],24:[function(require,module,exports){
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

},{}],25:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _reducersStore = require("../reducers/store");

var _reducersStore2 = _interopRequireDefault(_reducersStore);

exports["default"] = {
	onUpload: function onUpload() {
		return _reducersStore2["default"].dispatch({ type: "UPLOAD" });
	},

	onSelectCollection: function onSelectCollection(collection) {
		return _reducersStore2["default"].dispatch({ type: "SET_ACTIVE_COLLECTION", collection: collection });
	},

	onMapCollectionArchetype: function onMapCollectionArchetype(collection, value) {
		return _reducersStore2["default"].dispatch({ type: "MAP_COLLECTION_ARCHETYPE", collection: collection, value: value });
	},

	onConfirmCollectionArchetypeMappings: function onConfirmCollectionArchetypeMappings() {
		return _reducersStore2["default"].dispatch({ type: "CONFIRM_COLLECTION_ARCHETYPE_MAPPINGS" });
	},

	onSetFieldMapping: function onSetFieldMapping(collection, propertyField, importedField) {
		return _reducersStore2["default"].dispatch({ type: "SET_FIELD_MAPPING", collection: collection, propertyField: propertyField, importedField: importedField });
	},

	onClearFieldMapping: function onClearFieldMapping(collection, propertyField, clearIndex) {
		return _reducersStore2["default"].dispatch({ type: "CLEAR_FIELD_MAPPING", collection: collection, propertyField: propertyField, clearIndex: clearIndex });
	},

	onSetDefaultValue: function onSetDefaultValue(collection, propertyField, value) {
		return _reducersStore2["default"].dispatch({ type: "SET_DEFAULT_VALUE", collection: collection, propertyField: propertyField, value: value });
	},

	onConfirmFieldMappings: function onConfirmFieldMappings(collection, propertyField) {
		return _reducersStore2["default"].dispatch({ type: "CONFIRM_FIELD_MAPPINGS", collection: collection, propertyField: propertyField });
	},

	onUnconfirmFieldMappings: function onUnconfirmFieldMappings(collection, propertyField) {
		return _reducersStore2["default"].dispatch({ type: "UNCONFIRM_FIELD_MAPPINGS", collection: collection, propertyField: propertyField });
	},

	onSetValueMapping: function onSetValueMapping(collection, propertyField, timValue, mapValue) {
		return _reducersStore2["default"].dispatch({ type: "SET_VALUE_MAPPING", collection: collection, propertyField: propertyField, timValue: timValue, mapValue: mapValue });
	},

	onIgnoreColumnToggle: function onIgnoreColumnToggle(collection, variableName) {
		return _reducersStore2["default"].dispatch({ type: "TOGGLE_IGNORED_COLUMN", collection: collection, variableName: variableName });
	},

	onAddCustomProperty: function onAddCustomProperty(collection, propertyName, propertyType) {
		return _reducersStore2["default"].dispatch({ type: "ADD_CUSTOM_PROPERTY", collection: collection, propertyField: propertyName, propertyType: propertyType });
	},

	onRemoveCustomProperty: function onRemoveCustomProperty(collection, propertyName) {
		return _reducersStore2["default"].dispatch({ type: "REMOVE_CUSTOM_PROPERTY", collection: collection, propertyField: propertyName });
	}
};
module.exports = exports["default"];

},{"../reducers/store":49}],26:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsSelectField = require("./fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var ArchetypeMappings = (function (_React$Component) {
	_inherits(ArchetypeMappings, _React$Component);

	function ArchetypeMappings() {
		_classCallCheck(this, ArchetypeMappings);

		_get(Object.getPrototypeOf(ArchetypeMappings.prototype), "constructor", this).apply(this, arguments);
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

			return _react2["default"].createElement(
				"div",
				{ className: "panel panel-default col-md-6 col-md-offset-3" },
				_react2["default"].createElement(
					"div",
					{ className: "panel-body" },
					"You have uploaded test.xlsx. We found 2 tabs.",
					_react2["default"].createElement("br", null),
					"Connect the tabs to the timbuctoo archetypes"
				),
				_react2["default"].createElement(
					"ul",
					{ className: "list-group" },
					importData.sheets.map(function (sheet, i) {
						return _react2["default"].createElement(
							"li",
							{ className: "list-group-item", key: i },
							_react2["default"].createElement(
								"label",
								null,
								i + 1,
								" ",
								sheet.collection
							),
							_react2["default"].createElement(_fieldsSelectField2["default"], {
								onChange: function (value) {
									return onMapCollectionArchetype(sheet.collection, value);
								},
								onClear: function () {
									return onMapCollectionArchetype(sheet.collection, null);
								},
								options: Object.keys(archetype).filter(function (domain) {
									return domain !== "relations";
								}),
								placeholder: "Archetype for " + sheet.collection,
								value: mappings.collections[sheet.collection].archetypeName })
						);
					}),
					_react2["default"].createElement(
						"li",
						{ className: "list-group-item" },
						_react2["default"].createElement(
							"button",
							{ className: "btn btn-lg btn-success", disabled: !collectionsAreMapped, onClick: onConfirmCollectionArchetypeMappings },
							"Ok"
						)
					)
				)
			);
		}
	}]);

	return ArchetypeMappings;
})(_react2["default"].Component);

ArchetypeMappings.propTypes = {
	archetype: _react2["default"].PropTypes.object,
	collectionsAreMapped: _react2["default"].PropTypes.bool,
	importData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	onConfirmCollectionArchetypeMappings: _react2["default"].PropTypes.func,
	onMapCollectionArchetype: _react2["default"].PropTypes.func
};

exports["default"] = ArchetypeMappings;
module.exports = exports["default"];

},{"./fields/select-field":31,"react":"react"}],27:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _propertyFormAddProperty = require("./property-form/add-property");

var _propertyFormAddProperty2 = _interopRequireDefault(_propertyFormAddProperty);

var _propertyForm = require("./property-form");

var _propertyForm2 = _interopRequireDefault(_propertyForm);

var CollectionForm = (function (_React$Component) {
	_inherits(CollectionForm, _React$Component);

	function CollectionForm() {
		_classCallCheck(this, CollectionForm);

		_get(Object.getPrototypeOf(CollectionForm.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(CollectionForm, [{
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var importData = _props.importData;
			var archetype = _props.archetype;
			var mappings = _props.mappings;
			var activeCollection = importData.activeCollection;
			var sheets = importData.sheets;

			var collectionData = sheets.find(function (sheet) {
				return sheet.collection === activeCollection;
			});

			var archetypeName = mappings.collections[activeCollection].archetypeName;

			var archetypeFields = archetypeName ? archetype[archetypeName] : [];
			var archeTypePropFields = archetypeFields.filter(function (af) {
				return af.type !== "relation";
			});

			var propertyForms = archeTypePropFields.map(function (af, i) {
				return _react2["default"].createElement(_propertyForm2["default"], _extends({}, _this.props, { collectionData: collectionData, custom: false, key: i, name: af.name, type: af.type }));
			});

			var customPropertyForms = mappings.collections[activeCollection].customProperties.map(function (cf, i) {
				return _react2["default"].createElement(_propertyForm2["default"], _extends({}, _this.props, { collectionData: collectionData, custom: true, key: i, name: cf.name, type: cf.type }));
			});

			return _react2["default"].createElement(
				"div",
				{ className: "panel panel-default" },
				_react2["default"].createElement(
					"div",
					{ className: "panel-heading" },
					"Collection settings: ",
					activeCollection
				),
				_react2["default"].createElement(
					"ul",
					{ className: "list-group" },
					propertyForms,
					customPropertyForms,
					_react2["default"].createElement(_propertyFormAddProperty2["default"], this.props)
				)
			);
		}
	}]);

	return CollectionForm;
})(_react2["default"].Component);

CollectionForm.propTypes = {
	archetype: _react2["default"].PropTypes.object,
	importData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object
};

exports["default"] = CollectionForm;
module.exports = exports["default"];

},{"./property-form":34,"./property-form/add-property":33,"react":"react"}],28:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var CollectionIndex = (function (_React$Component) {
	_inherits(CollectionIndex, _React$Component);

	function CollectionIndex() {
		_classCallCheck(this, CollectionIndex);

		_get(Object.getPrototypeOf(CollectionIndex.prototype), "constructor", this).apply(this, arguments);
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
			}, []).length;

			return confirmedColCount + mappings.collections[sheet.collection].ignoredColumns.length === sheet.variables.length;
		}
	}, {
		key: "allMappingsAreIncomplete",
		value: function allMappingsAreIncomplete() {
			var _this = this;

			var importData = this.props.importData;
			var sheets = importData.sheets;

			return sheets.map(function (sheet) {
				return _this.mappingsAreComplete(sheet);
			}).filter(function (result) {
				return result !== true;
			}).length === 0;
		}
	}, {
		key: "render",
		value: function render() {
			var _this2 = this;

			var _props = this.props;
			var importData = _props.importData;
			var onSelectCollection = _props.onSelectCollection;
			var sheets = importData.sheets;

			return _react2["default"].createElement(
				"div",
				{ className: "panel panel-default" },
				_react2["default"].createElement(
					"div",
					{ className: "panel-heading" },
					"Collections"
				),
				_react2["default"].createElement(
					"div",
					{ className: "list-group" },
					sheets.map(function (sheet, i) {
						return _react2["default"].createElement(
							"a",
							{
								className: (0, _classnames2["default"])("list-group-item", { active: sheet.collection === importData.activeCollection }),
								key: i,
								onClick: function () {
									return onSelectCollection(sheet.collection);
								}
							},
							_react2["default"].createElement("span", { className: (0, _classnames2["default"])("glyphicon", "pull-right", {
									"glyphicon-question-sign": !_this2.mappingsAreComplete(sheet),
									"glyphicon-ok-sign": _this2.mappingsAreComplete(sheet)
								}) }),
							sheet.collection
						);
					}),
					_react2["default"].createElement(
						"li",
						{ className: "list-group-item" },
						_react2["default"].createElement(
							"button",
							{ className: "btn btn-success" },
							"Save"
						),
						" ",
						_react2["default"].createElement(
							"button",
							{ className: "btn btn-success", disabled: !this.allMappingsAreIncomplete() },
							"Publish"
						)
					)
				)
			);
		}
	}]);

	return CollectionIndex;
})(_react2["default"].Component);

CollectionIndex.propTypes = {
	importData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	onSelectCollection: _react2["default"].PropTypes.func
};

exports["default"] = CollectionIndex;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],29:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _tableDataRow = require("./table/data-row");

var _tableDataRow2 = _interopRequireDefault(_tableDataRow);

var _tableHeaderCell = require("./table/header-cell");

var _tableHeaderCell2 = _interopRequireDefault(_tableHeaderCell);

var CollectionTable = (function (_React$Component) {
	_inherits(CollectionTable, _React$Component);

	function CollectionTable() {
		_classCallCheck(this, CollectionTable);

		_get(Object.getPrototypeOf(CollectionTable.prototype), "constructor", this).apply(this, arguments);
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

			return _react2["default"].createElement(
				"div",
				{ className: "panel panel-default" },
				_react2["default"].createElement(
					"div",
					{ className: "panel-heading" },
					"Collection: ",
					collection
				),
				_react2["default"].createElement(
					"table",
					{ className: "table table-bordered" },
					_react2["default"].createElement(
						"thead",
						null,
						_react2["default"].createElement(
							"tr",
							null,
							variables.map(function (header, i) {
								return _react2["default"].createElement(_tableHeaderCell2["default"], {
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
					_react2["default"].createElement(
						"tbody",
						null,
						rows.map(function (row, i) {
							return _react2["default"].createElement(_tableDataRow2["default"], {
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
})(_react2["default"].Component);

CollectionTable.propTypes = {
	importData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	onIgnoreColumnToggle: _react2["default"].PropTypes.func
};

exports["default"] = CollectionTable;
module.exports = exports["default"];

},{"./table/data-row":40,"./table/header-cell":41,"react":"react"}],30:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _collectionIndex = require("./collection-index");

var _collectionIndex2 = _interopRequireDefault(_collectionIndex);

var _collectionTable = require("./collection-table");

var _collectionTable2 = _interopRequireDefault(_collectionTable);

var _collectionForm = require("./collection-form");

var _collectionForm2 = _interopRequireDefault(_collectionForm);

var DatasheetMappings = (function (_React$Component) {
	_inherits(DatasheetMappings, _React$Component);

	function DatasheetMappings() {
		_classCallCheck(this, DatasheetMappings);

		_get(Object.getPrototypeOf(DatasheetMappings.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(DatasheetMappings, [{
		key: "render",
		value: function render() {

			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"nav",
					{ className: "col-sm-2" },
					_react2["default"].createElement(_collectionIndex2["default"], this.props)
				),
				_react2["default"].createElement(
					"main",
					{ className: "col-sm-10" },
					_react2["default"].createElement(_collectionForm2["default"], this.props),
					_react2["default"].createElement(_collectionTable2["default"], this.props)
				)
			);
		}
	}]);

	return DatasheetMappings;
})(_react2["default"].Component);

exports["default"] = DatasheetMappings;
module.exports = exports["default"];

},{"./collection-form":27,"./collection-index":28,"./collection-table":29,"react":"react"}],31:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var SelectField = (function (_React$Component) {
	_inherits(SelectField, _React$Component);

	function SelectField(props) {
		_classCallCheck(this, SelectField);

		_get(Object.getPrototypeOf(SelectField.prototype), "constructor", this).call(this, props);

		this.state = {
			isOpen: false
		};
		this.documentClickListener = this.handleDocumentClick.bind(this);
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

			if (isOpen && !_reactDom2["default"].findDOMNode(this).contains(ev.target)) {
				this.setState({
					isOpen: false
				});
			}
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var options = _props.options;
			var onChange = _props.onChange;
			var onClear = _props.onClear;
			var placeholder = _props.placeholder;
			var value = _props.value;

			return _react2["default"].createElement(
				"span",
				{ className: (0, _classnames2["default"])("dropdown", { open: this.state.isOpen }) },
				_react2["default"].createElement(
					"button",
					{ className: "btn btn-default btn-sx dropdown-toggle",
						onClick: this.toggleSelect.bind(this),
						style: value ? { color: "#666" } : { color: "#aaa" } },
					value || placeholder,
					" ",
					_react2["default"].createElement("span", { className: "caret" })
				),
				_react2["default"].createElement(
					"ul",
					{ className: "dropdown-menu" },
					options.map(function (option, i) {
						return _react2["default"].createElement(
							"li",
							{ key: i },
							_react2["default"].createElement(
								"a",
								{ onClick: function () {
										onChange(option);_this.toggleSelect();
									} },
								option
							)
						);
					}),
					value ? _react2["default"].createElement(
						"li",
						null,
						_react2["default"].createElement(
							"a",
							{ onClick: function () {
									onClear();_this.toggleSelect();
								} },
							"- clear -"
						)
					) : null
				)
			);
		}
	}]);

	return SelectField;
})(_react2["default"].Component);

SelectField.propTypes = {
	onChange: _react2["default"].PropTypes.func,
	onClear: _react2["default"].PropTypes.func,
	options: _react2["default"].PropTypes.array,
	placeholder: _react2["default"].PropTypes.string,
	value: _react2["default"].PropTypes.string
};

exports["default"] = SelectField;
module.exports = exports["default"];

},{"classnames":1,"react":"react","react-dom":"react-dom"}],32:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _uploadSplashScreen = require("./upload-splash-screen");

var _uploadSplashScreen2 = _interopRequireDefault(_uploadSplashScreen);

var _archetypeMappings = require("./archetype-mappings");

var _archetypeMappings2 = _interopRequireDefault(_archetypeMappings);

var _datasheetMappings = require("./datasheet-mappings");

var _datasheetMappings2 = _interopRequireDefault(_datasheetMappings);

var _utilPersist = require("../util/persist");

var App = (function (_React$Component) {
	_inherits(App, _React$Component);

	function App() {
		_classCallCheck(this, App);

		_get(Object.getPrototypeOf(App.prototype), "constructor", this).apply(this, arguments);
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

			var datasheetMappings = importData.sheets && collectionsAreMapped && mappings.confirmed ? _react2["default"].createElement(_datasheetMappings2["default"], this.props) : null;

			var archetypeMappings = !datasheetMappings && importData.sheets ? _react2["default"].createElement(_archetypeMappings2["default"], _extends({}, this.props, { collectionsAreMapped: collectionsAreMapped })) : null;

			var uploadSplashScreen = !datasheetMappings && !archetypeMappings ? _react2["default"].createElement(_uploadSplashScreen2["default"], this.props) : null;

			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"a",
					{ onClick: function () {
							(0, _utilPersist.disablePersist)();location.reload();
						}, style: { position: "absolute", top: 0, right: 0, zIndex: 10 } },
					"clear state"
				),
				_react2["default"].createElement(
					"div",
					{ className: "row centered-form center-block" },
					_react2["default"].createElement(
						"div",
						{ className: "container col-md-12" },
						_react2["default"].createElement(
							"main",
							null,
							datasheetMappings,
							archetypeMappings,
							uploadSplashScreen
						)
					)
				)
			);
		}
	}]);

	return App;
})(_react2["default"].Component);

App.propTypes = {
	importData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object
};

exports["default"] = App;
module.exports = exports["default"];

},{"../util/persist":53,"./archetype-mappings":26,"./datasheet-mappings":30,"./upload-splash-screen":42,"react":"react"}],33:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsSelectField = require("../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var AddProperty = (function (_React$Component) {
	_inherits(AddProperty, _React$Component);

	function AddProperty(props) {
		_classCallCheck(this, AddProperty);

		_get(Object.getPrototypeOf(AddProperty.prototype), "constructor", this).call(this, props);

		this.state = {
			newName: null,
			newType: null
		};
	}

	_createClass(AddProperty, [{
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var importData = _props.importData;
			var relationTypes = _props.relationTypes;
			var mappings = _props.mappings;
			var onAddCustomProperty = _props.onAddCustomProperty;
			var _state = this.state;
			var newType = _state.newType;
			var newName = _state.newName;
			var activeCollection = importData.activeCollection;
			var archetypeName = mappings.collections[activeCollection].archetypeName;

			var availableArchetypes = Object.keys(mappings.collections).map(function (key) {
				return mappings.collections[key].archetypeName;
			});

			var relationTypeOptions = relationTypes.data.filter(function (relType) {
				return relType.sourceTypeName + "s" === archetypeName || relType.targetTypeName + "s" === archetypeName;
			}).filter(function (relType) {
				return availableArchetypes.indexOf(relType.sourceTypeName + "s") > -1 && availableArchetypes.indexOf(relType.targetTypeName + "s") > -1;
			}).map(function (relType) {
				return relType.sourceTypeName + "s" === archetypeName ? relType.regularName : relType.inverseName;
			});

			return _react2["default"].createElement(
				"li",
				{ className: "list-group-item" },
				_react2["default"].createElement(
					"label",
					null,
					_react2["default"].createElement(
						"strong",
						null,
						"Add property"
					)
				),
				_react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return _this.setState({ newType: value, newName: value === "relation" ? null : newName });
					},
					onClear: function () {
						return _this.setState({ newType: null });
					},
					options: ["text", "datable", "relation"],
					placeholder: "Choose a type...",
					value: newType }),
				" ",
				newType === "relation" ? _react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return _this.setState({ newName: value });
					},
					onClear: function () {
						return _this.setState({ newName: null });
					},
					options: relationTypeOptions,
					placeholder: "Choose a type...",
					value: newName }) : _react2["default"].createElement("input", { onChange: function (ev) {
						return _this.setState({ newName: ev.target.value });
					}, placeholder: "Property name", value: newName }),
				" ",
				_react2["default"].createElement(
					"button",
					{ className: "btn btn-success", disabled: !(newName && newType),
						onClick: function () {
							onAddCustomProperty(activeCollection, newName, newType);
							_this.setState({ newName: null, newType: null });
						} },
					"Add"
				)
			);
		}
	}]);

	return AddProperty;
})(_react2["default"].Component);

AddProperty.propTypes = {
	importData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	onAddCustomProperty: _react2["default"].PropTypes.func,
	relationTypes: _react2["default"].PropTypes.object
};

exports["default"] = AddProperty;
module.exports = exports["default"];

},{"../fields/select-field":31,"react":"react"}],34:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

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

var typeMap = {
	text: function text(props) {
		return _react2["default"].createElement(_text2["default"], props);
	},
	datable: function datable(props) {
		return _react2["default"].createElement(_text2["default"], props);
	},
	names: function names(props) {
		return _react2["default"].createElement(_names2["default"], props);
	},
	links: function links(props) {
		return _react2["default"].createElement(_links2["default"], props);
	},
	select: function select(props) {
		return _react2["default"].createElement(_select2["default"], props);
	},
	multiselect: function multiselect(props) {
		return _react2["default"].createElement(_select2["default"], props);
	},
	relation: function relation(props) {
		return _react2["default"].createElement(_relation2["default"], props);
	}
};

var PropertyForm = (function (_React$Component) {
	_inherits(PropertyForm, _React$Component);

	function PropertyForm() {
		_classCallCheck(this, PropertyForm);

		_get(Object.getPrototypeOf(PropertyForm.prototype), "constructor", this).apply(this, arguments);
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

			var confirmButton = this.canConfirm(propertyMapping.variable || null) && !confirmed ? _react2["default"].createElement(
				"button",
				{ className: "btn btn-success btn-sm", onClick: function () {
						return onConfirmFieldMappings(collectionData.collection, name);
					} },
				"Confirm"
			) : confirmed ? _react2["default"].createElement(
				"button",
				{ className: "btn btn-danger btn-sm", onClick: function () {
						return onUnconfirmFieldMappings(collectionData.collection, name);
					} },
				"Unconfirm"
			) : null;

			var formComponent = typeMap[type](this.props);

			return _react2["default"].createElement(
				"li",
				{ className: "list-group-item" },
				custom ? _react2["default"].createElement(
					"a",
					{ className: "pull-right btn-danger btn-xs", onClick: function () {
							return onRemoveCustomProperty(collectionData.collection, name);
						} },
					_react2["default"].createElement("span", { className: "glyphicon glyphicon-remove" })
				) : null,
				_react2["default"].createElement(
					"label",
					null,
					_react2["default"].createElement(
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
})(_react2["default"].Component);

PropertyForm.propTypes = {
	collectionData: _react2["default"].PropTypes.object,
	custom: _react2["default"].PropTypes.bool,
	mappings: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onConfirmFieldMappings: _react2["default"].PropTypes.func,
	onRemoveCustomProperty: _react2["default"].PropTypes.func,
	onUnconfirmFieldMappings: _react2["default"].PropTypes.func,
	type: _react2["default"].PropTypes.string
};

exports["default"] = PropertyForm;
module.exports = exports["default"];

},{"./links":35,"./names":36,"./relation":37,"./select":38,"./text":39,"react":"react"}],35:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsSelectField = require("../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var Form = (function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		_get(Object.getPrototypeOf(Form.prototype), "constructor", this).apply(this, arguments);
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

			return _react2["default"].createElement(
				"span",
				null,
				_react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [_extends({}, selectedVariableUrl), _extends({}, selectedVariableLabel, { field: "label", variableName: value })]);
					},
					onClear: function () {
						return onClearFieldMapping(collectionData.collection, name, (propertyMapping.variable || []).map(function (v) {
							return v.field;
						}).indexOf("label"));
					},
					options: collectionData.variables, placeholder: "Select label column...",
					value: selectedVariableLabel.variableName || null }),
				" ",
				selectedVariableUrl.variableName && selectedVariableLabel.variableName ? _react2["default"].createElement("input", { onChange: function (ev) {
						return onSetDefaultValue(collectionData.collection, name, [_extends({}, defaultValueUrl), _extends({}, defaultValueLabel, { field: "label", value: ev.target.value })]);
					},
					placeholder: "Default value...", type: "text", value: defaultValueLabel.value || null }) : null,
				" ",
				_react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [_extends({}, selectedVariableUrl, { field: "url", variableName: value }), _extends({}, selectedVariableLabel)]);
					},
					onClear: function () {
						return onClearFieldMapping(collectionData.collection, name, (propertyMapping.variable || []).map(function (v) {
							return v.field;
						}).indexOf("url"));
					},
					options: collectionData.variables, placeholder: "Select URL column...",
					value: selectedVariableUrl.variableName || null }),
				" ",
				selectedVariableUrl.variableName && selectedVariableLabel.variableName ? _react2["default"].createElement("input", { onChange: function (ev) {
						return onSetDefaultValue(collectionData.collection, name, [_extends({}, defaultValueUrl, { field: "url", value: ev.target.value }), _extends({}, defaultValueLabel)]);
					},
					placeholder: "Default value...", type: "text", value: defaultValueUrl.value || null }) : null
			);
		}
	}]);

	return Form;
})(_react2["default"].Component);

Form.propTypes = {
	collectionData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onClearFieldMapping: _react2["default"].PropTypes.func,
	onSetDefaultValue: _react2["default"].PropTypes.func,
	onSetFieldMapping: _react2["default"].PropTypes.func
};

exports["default"] = Form;
module.exports = exports["default"];

},{"../fields/select-field":31,"react":"react"}],36:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i]; return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsSelectField = require("../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var Form = (function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		_get(Object.getPrototypeOf(Form.prototype), "constructor", this).apply(this, arguments);
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
			var _this = this;

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

			return _react2["default"].createElement(
				"span",
				null,
				propertyMapping.variable && propertyMapping.variable.length ? _react2["default"].createElement(
					"div",
					{ style: { marginBottom: "12px" } },
					(propertyMapping.variable || []).map(function (v, i) {
						return _react2["default"].createElement(
							"span",
							{ key: i, style: { display: "inline-block", margin: "8px 8px 0 0" } },
							_react2["default"].createElement(
								"div",
								{ style: { marginBottom: "2px" } },
								_react2["default"].createElement(
									"a",
									{ className: "pull-right btn-danger btn-xs", onClick: function () {
											return onClearFieldMapping(collectionData.collection, name, i);
										} },
									_react2["default"].createElement("span", { className: "glyphicon glyphicon-remove" })
								),
								v.component,
								" "
							),
							_react2["default"].createElement(_fieldsSelectField2["default"], {
								onChange: function (value) {
									return _this.onComponentChange(propertyMapping, i, value);
								},
								onClear: function () {
									return onClearFieldMapping(collectionData.collection, name, i);
								},
								options: collectionData.variables,
								placeholder: "Select column for " + v.component,
								value: v.variableName })
						);
					})
				) : null,
				_react2["default"].createElement(_fieldsSelectField2["default"], { onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [].concat(_toConsumableArray(propertyMapping.variable || []), [{ component: value }]));
					},
					options: components, placeholder: "Add name component...",
					value: null })
			);
		}
	}]);

	return Form;
})(_react2["default"].Component);

Form.propTypes = {
	archetype: _react2["default"].PropTypes.object,
	collectionData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onClearFieldMapping: _react2["default"].PropTypes.func,
	onSetFieldMapping: _react2["default"].PropTypes.func
};

exports["default"] = Form;
module.exports = exports["default"];

},{"../fields/select-field":31,"react":"react"}],37:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsSelectField = require("../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var Form = (function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		_get(Object.getPrototypeOf(Form.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Form, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var collectionData = _props.collectionData;
			var onSetFieldMapping = _props.onSetFieldMapping;
			var onClearFieldMapping = _props.onClearFieldMapping;
			var mappings = _props.mappings;
			var name = _props.name;
			var importData = _props.importData;
			var relationTypes = _props.relationTypes;

			var mapping = mappings.collections[collectionData.collection].mappings;
			var propertyMapping = mapping.find(function (m) {
				return m.property === name;
			}) || {};
			var selectedVariable = propertyMapping.variable && propertyMapping.variable.length ? propertyMapping.variable[0] : {};
			var relationType = relationTypes.data.find(function (relType) {
				return relType.regularName === name || relType.inverseName === name;
			});
			var isInverse = relationType.inverseName === name;
			var availableCollections = Object.keys(mappings.collections).map(function (key) {
				return {
					archetype: mappings.collections[key].archetypeName,
					collection: key
				};
			}).filter(function (ac) {
				return ac.archetype === (isInverse ? relationType.sourceTypeName + "s" : relationType.targetTypeName + "s");
			}).map(function (ac) {
				return ac.collection;
			});

			var availableTargetColumns = (importData.sheets.find(function (sheet) {
				return sheet.collection === selectedVariable.targetCollection;
			}) || {}).variables;

			return _react2["default"].createElement(
				"span",
				null,
				_react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [_extends({}, selectedVariable, { variableName: value })]);
					},
					onClear: function () {
						return onClearFieldMapping(collectionData.collection, name, 0);
					},
					options: collectionData.variables, placeholder: "Select source column...",
					value: selectedVariable.variableName || null }),
				" ",
				selectedVariable.variableName ? _react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [_extends({}, selectedVariable, { targetCollection: value })]);
					},
					onClear: function () {
						return onClearFieldMapping(collectionData.collection, name, 0);
					},
					options: availableCollections, placeholder: "Select a target collection...",
					value: selectedVariable.targetCollection || null }) : null,
				" ",
				selectedVariable.targetCollection ? _react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [_extends({}, selectedVariable, { targetVariableName: value })]);
					},
					onClear: function () {
						return onClearFieldMapping(collectionData.collection, name, 0);
					},
					options: availableTargetColumns, placeholder: "Select a target column...",
					value: selectedVariable.targetVariableName || null }) : null
			);
		}
	}]);

	return Form;
})(_react2["default"].Component);

Form.propTypes = {
	collectionData: _react2["default"].PropTypes.object,
	importData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onClearFieldMapping: _react2["default"].PropTypes.func,
	onSetFieldMapping: _react2["default"].PropTypes.func,
	relationTypes: _react2["default"].PropTypes.object
};

exports["default"] = Form;
module.exports = exports["default"];

},{"../fields/select-field":31,"react":"react"}],38:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsSelectField = require("../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var Form = (function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		_get(Object.getPrototypeOf(Form.prototype), "constructor", this).apply(this, arguments);
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

			return _react2["default"].createElement(
				"span",
				null,
				_react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [{ variableName: value }]);
					},
					onClear: function () {
						return onClearFieldMapping(collectionData.collection, name, 0);
					},
					options: collectionData.variables, placeholder: "Select a column...",
					value: selectedVariable.variableName }),
				" ",
				selectedVariable.variableName ? _react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetDefaultValue(collectionData.collection, name, [{ value: value }]);
					},
					onClear: function () {
						return onSetDefaultValue(collectionData.collection, name, [{ value: null }]);
					},
					options: defaultOptions, placeholder: "Select a default value...",
					value: defaultValue.value }) : null,
				selectedVariable.variableName ? _react2["default"].createElement(
					"ul",
					{ className: "list-group", style: { marginTop: "12px", maxHeight: "275px", overflowY: "auto" } },
					_react2["default"].createElement(
						"li",
						{ className: "list-group-item" },
						_react2["default"].createElement(
							"strong",
							null,
							"Map import values to select options"
						),
						_react2["default"].createElement(
							"p",
							null,
							"* Leave blank to match exact value"
						),
						" "
					),
					defaultOptions.map(function (selectOption, i) {
						return _react2["default"].createElement(
							"li",
							{ className: "list-group-item", key: i },
							_react2["default"].createElement(
								"label",
								null,
								selectOption
							),
							_react2["default"].createElement("input", { onChange: function (ev) {
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
})(_react2["default"].Component);

Form.propTypes = {
	archetype: _react2["default"].PropTypes.object,
	collectionData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onClearFieldMapping: _react2["default"].PropTypes.func,
	onSetDefaultValue: _react2["default"].PropTypes.func,
	onSetFieldMapping: _react2["default"].PropTypes.func,
	onSetValueMapping: _react2["default"].PropTypes.func
};

exports["default"] = Form;
module.exports = exports["default"];

},{"../fields/select-field":31,"react":"react"}],39:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsSelectField = require("../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var Form = (function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		_get(Object.getPrototypeOf(Form.prototype), "constructor", this).apply(this, arguments);
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

			return _react2["default"].createElement(
				"span",
				null,
				_react2["default"].createElement(_fieldsSelectField2["default"], {
					onChange: function (value) {
						return onSetFieldMapping(collectionData.collection, name, [{ variableName: value }]);
					},
					onClear: function () {
						return onClearFieldMapping(collectionData.collection, name, 0);
					},
					options: collectionData.variables, placeholder: "Select a column...",
					value: selectedVariable.variableName || null }),
				" ",
				_react2["default"].createElement("input", { onChange: function (ev) {
						return onSetDefaultValue(collectionData.collection, name, [{ value: ev.target.value }]);
					},
					placeholder: "Default value...", type: "text", value: defaultValue.value || null })
			);
		}
	}]);

	return Form;
})(_react2["default"].Component);

Form.propTypes = {
	collectionData: _react2["default"].PropTypes.object,
	mappings: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onClearFieldMapping: _react2["default"].PropTypes.func,
	onSetDefaultValue: _react2["default"].PropTypes.func,
	onSetFieldMapping: _react2["default"].PropTypes.func
};

exports["default"] = Form;
module.exports = exports["default"];

},{"../fields/select-field":31,"react":"react"}],40:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var DataRow = (function (_React$Component) {
	_inherits(DataRow, _React$Component);

	function DataRow() {
		_classCallCheck(this, DataRow);

		_get(Object.getPrototypeOf(DataRow.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(DataRow, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var row = _props.row;
			var confirmedCols = _props.confirmedCols;
			var ignoredColumns = _props.ignoredColumns;
			var variables = _props.variables;

			return _react2["default"].createElement(
				"tr",
				null,
				row.map(function (cell, i) {
					return _react2["default"].createElement(
						"td",
						{ className: (0, _classnames2["default"])({
								ignored: confirmedCols.indexOf(i) < 0 && ignoredColumns.indexOf(variables[i]) > -1
							}), key: i },
						cell
					);
				})
			);
		}
	}]);

	return DataRow;
})(_react2["default"].Component);

DataRow.propTypes = {
	confirmedCols: _react2["default"].PropTypes.array,
	ignoredColumns: _react2["default"].PropTypes.array,
	row: _react2["default"].PropTypes.array,
	variables: _react2["default"].PropTypes.array
};

exports["default"] = DataRow;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],41:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var HeaderCell = (function (_React$Component) {
	_inherits(HeaderCell, _React$Component);

	function HeaderCell() {
		_classCallCheck(this, HeaderCell);

		_get(Object.getPrototypeOf(HeaderCell.prototype), "constructor", this).apply(this, arguments);
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

			return _react2["default"].createElement(
				"th",
				{ className: (0, _classnames2["default"])({
						success: isConfirmed,
						info: !isConfirmed && !isIgnored,
						ignored: !isConfirmed && isIgnored
					}) },
				header,
				_react2["default"].createElement("a", { className: (0, _classnames2["default"])("pull-right", "glyphicon", {
						"glyphicon-ok-sign": isConfirmed,
						"glyphicon-question-sign": !isConfirmed && !isIgnored,
						"glyphicon-remove": !isConfirmed && isIgnored
					}), onClick: function () {
						return !isConfirmed ? onIgnoreColumnToggle(activeCollection, header) : null;
					} })
			);
		}
	}]);

	return HeaderCell;
})(_react2["default"].Component);

HeaderCell.propTypes = {
	activeCollection: _react2["default"].PropTypes.string,
	header: _react2["default"].PropTypes.string,
	isConfirmed: _react2["default"].PropTypes.bool,
	isIgnored: _react2["default"].PropTypes.bool,
	onIgnoreColumnToggle: _react2["default"].PropTypes.func
};

exports["default"] = HeaderCell;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],42:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var UploadSplashScreen = (function (_React$Component) {
	_inherits(UploadSplashScreen, _React$Component);

	function UploadSplashScreen() {
		_classCallCheck(this, UploadSplashScreen);

		_get(Object.getPrototypeOf(UploadSplashScreen.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(UploadSplashScreen, [{
		key: "render",
		value: function render() {
			var onUpload = this.props.onUpload;

			return _react2["default"].createElement(
				"div",
				{ id: "splash" },
				_react2["default"].createElement(
					"h1",
					null,
					"TIMBUCTOO"
				),
				_react2["default"].createElement(
					"p",
					null,
					"Get your data stored and connected to the world.",
					_react2["default"].createElement("br", null),
					"Start uploading your data."
				),
				_react2["default"].createElement(
					"button",
					{ className: "btn btn-lg btn-default", onClick: onUpload },
					_react2["default"].createElement("span", { className: "glyphicon glyphicon-cloud-upload pull-left" }),
					"  Upload"
				),
				_react2["default"].createElement(
					"p",
					null,
					"Need to get started? Here's an example ",
					_react2["default"].createElement(
						"a",
						null,
						"speadsheet.xlsx"
					)
				)
			);
		}
	}]);

	return UploadSplashScreen;
})(_react2["default"].Component);

UploadSplashScreen.propTypes = {
	onUpload: _react2["default"].PropTypes.func
};

exports["default"] = UploadSplashScreen;
module.exports = exports["default"];

},{"react":"react"}],43:[function(require,module,exports){
"use strict";

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _reducersStore = require("./reducers/store");

var _reducersStore2 = _interopRequireDefault(_reducersStore);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var _components = require("./components");

var _components2 = _interopRequireDefault(_components);

var _relationtypes = require("./relationtypes");

var _relationtypes2 = _interopRequireDefault(_relationtypes);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

_reducersStore2["default"].subscribe(function () {
	return _reactDom2["default"].render(_react2["default"].createElement(_components2["default"], _extends({}, _reducersStore2["default"].getState(), _actions2["default"])), document.getElementById("app"));
});

document.addEventListener("DOMContentLoaded", function () {
	_reactDom2["default"].render(_react2["default"].createElement(
		"p",
		null,
		"fetching relation types"
	), document.getElementById("app"));

	_reducersStore2["default"].dispatch({ type: "SET_RELATION_TYPES", data: _relationtypes2["default"] });
	(0, _xhr2["default"])("http://acc.repository.huygens.knaw.nl/v2.1/metadata/Admin", function (err, resp) {
		_reducersStore2["default"].dispatch({ type: "SET_ARCHETYPE_METADATA", data: JSON.parse(resp.body) });
	});
});

},{"./actions":25,"./components":32,"./reducers/store":49,"./relationtypes":50,"react":"react","react-dom":"react-dom","xhr":22}],44:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _utilPersist = require("../util/persist");

var initialState = (0, _utilPersist.getItem)("archetype") || {};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_ARCHETYPE_METADATA":
			return action.data;
	}

	return state;
};

module.exports = exports["default"];

},{"../util/persist":53}],45:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _utilPersist = require("../util/persist");

var mockPersonsHeader = ["ID", "Voornaam", "tussenvoegsel", "Achternaam", "GeschrevenDocument", "Genoemd in", "Is getrouwd met"];
var mockDocumentsHeader = ["titel", "datum", "referentie", "url"];

var scaffoldSheets = function scaffoldSheets(state) {
	var sheets = [{
		collection: "mockpersons",
		rows: [["1", "Jan", "", "Jansen", "Tekst 1", "Tekst 2", null], ["2", "Klaas", "", "Klaassen", "Tekst 2", null, null], ["3", "Ina", "van der", "Poel - Jansen", null, null, "1"]],
		variables: mockPersonsHeader
	}, {
		collection: "mockdocuments",
		rows: [["Tekst 1", "1850", "voorbeeld", "http://example.com"], ["Tekst 2", "1860", null, null]],
		variables: mockDocumentsHeader
	}];

	return _extends({}, state, {
		sheets: sheets,
		activeCollection: "mockpersons"
	});
};

var initialState = (0, _utilPersist.getItem)("importData") || {
	sheets: null,
	activeCollection: null
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "UPLOAD":
			return scaffoldSheets(state);
		case "SET_ACTIVE_COLLECTION":
			return _extends({}, state, { activeCollection: action.collection });
	}

	return state;
};

module.exports = exports["default"];

},{"../util/persist":53}],46:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _importData = require("./import-data");

var _importData2 = _interopRequireDefault(_importData);

var _relationTypes = require("./relation-types");

var _relationTypes2 = _interopRequireDefault(_relationTypes);

var _archetype = require("./archetype");

var _archetype2 = _interopRequireDefault(_archetype);

var _mappings = require("./mappings");

var _mappings2 = _interopRequireDefault(_mappings);

exports["default"] = {
	importData: _importData2["default"],
	relationTypes: _relationTypes2["default"],
	archetype: _archetype2["default"],
	mappings: _mappings2["default"]
};
module.exports = exports["default"];

},{"./archetype":44,"./import-data":45,"./mappings":47,"./relation-types":48}],47:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _utilSetIn = require("../util/set-in");

var _utilSetIn2 = _interopRequireDefault(_utilSetIn);

var _utilGetIn = require("../util/get-in");

var _utilGetIn2 = _interopRequireDefault(_utilGetIn);

var _utilPersist = require("../util/persist");

var newVariableDesc = function newVariableDesc(property, variableSpec) {
	return {
		property: property,
		variable: variableSpec,
		defaultValue: [],
		confirmed: false,
		valueMappings: {}
	};
};

var scaffoldCollectionMappings = function scaffoldCollectionMappings() {
	return {
		mockpersons: {
			archetypeName: null,
			mappings: [],
			ignoredColumns: [],
			customProperties: []
		},
		mockdocuments: {
			archetypeName: null,
			mappings: [],
			ignoredColumns: [],
			customProperties: []
		}
	};
};

var initialState = (0, _utilPersist.getItem)("mappings") || {
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
	var newCollections = (0, _utilSetIn2["default"])([action.collection, "archetypeName"], action.value, state.collections);
	newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings"], [], newCollections);

	return _extends({}, state, { collections: newCollections });
};

var upsertFieldMapping = function upsertFieldMapping(state, action) {
	var foundIdx = getMappingIndex(state, action);
	var newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings", foundIdx < 0 ? (0, _utilGetIn2["default"])([action.collection, "mappings"], state.collections).length : foundIdx], newVariableDesc(action.propertyField, action.importedField), state.collections);

	return _extends({}, state, { collections: newCollections });
};

var clearFieldMapping = function clearFieldMapping(state, action) {
	var foundIdx = getMappingIndex(state, action);
	if (foundIdx < 0) {
		return state;
	}

	var current = (0, _utilGetIn2["default"])([action.collection, "mappings", foundIdx, "variable"], state.collections).filter(function (m, i) {
		return i !== action.clearIndex;
	});

	var newCollections = undefined;
	if (current.length > 0) {
		newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings", foundIdx, "variable"], current, state.collections);
	} else {
		var newMappings = (0, _utilGetIn2["default"])([action.collection, "mappings"], state.collections).filter(function (m, i) {
			return i !== foundIdx;
		});
		newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings"], newMappings, state.collections);
	}

	return _extends({}, state, { collections: newCollections });
};

var setDefaultValue = function setDefaultValue(state, action) {
	var foundIdx = getMappingIndex(state, action);
	if (foundIdx > -1) {
		var newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings", foundIdx, "defaultValue"], action.value, state.collections);
		return _extends({}, state, { collections: newCollections });
	}

	return state;
};

var setFieldConfirmation = function setFieldConfirmation(state, action, value) {
	var current = ((0, _utilGetIn2["default"])([action.collection, "mappings"], state.collections) || []).map(function (vm) {
		return _extends({}, vm, { confirmed: action.propertyField === vm.property ? value : vm.confirmed });
	});
	var newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings"], current, state.collections);

	if (value === true) {
		(function () {
			var confirmedVariableNames = current.map(function (m) {
				return m.variable.map(function (v) {
					return v.variableName;
				});
			}).reduce(function (a, b) {
				return a.concat(b);
			});
			var newIgnoredColums = (0, _utilGetIn2["default"])([action.collection, "ignoredColumns"], state.collections).filter(function (ic) {
				return confirmedVariableNames.indexOf(ic) < 0;
			});
			newCollections = (0, _utilSetIn2["default"])([action.collection, "ignoredColumns"], newIgnoredColums, newCollections);
		})();
	}

	return _extends({}, state, { collections: newCollections });
};

var setValueMapping = function setValueMapping(state, action) {
	var foundIdx = getMappingIndex(state, action);

	if (foundIdx > -1) {
		var newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings", foundIdx, "valueMappings", action.timValue], action.mapValue, state.collections);
		return _extends({}, state, { collections: newCollections });
	}
	return state;
};

var toggleIgnoredColumn = function toggleIgnoredColumn(state, action) {
	var current = (0, _utilGetIn2["default"])([action.collection, "ignoredColumns"], state.collections);

	if (current.indexOf(action.variableName) < 0) {
		current.push(action.variableName);
	} else {
		current = current.filter(function (c) {
			return c !== action.variableName;
		});
	}

	return _extends({}, state, { collections: (0, _utilSetIn2["default"])([action.collection, "ignoredColumns"], current, state.collections) });
};

var addCustomProperty = function addCustomProperty(state, action) {
	var current = (0, _utilGetIn2["default"])([action.collection, "customProperties"], state.collections);
	var newCollections = (0, _utilSetIn2["default"])([action.collection, "customProperties", current.length], { name: action.propertyField, type: action.propertyType }, state.collections);

	return _extends({}, state, { collections: newCollections });
};

var removeCustomProperty = function removeCustomProperty(state, action) {
	var foundIdx = getMappingIndex(state, action);

	var current = (0, _utilGetIn2["default"])([action.collection, "customProperties"], state.collections).filter(function (cp) {
		return cp.name !== action.propertyField;
	});

	var newCollections = (0, _utilSetIn2["default"])([action.collection, "customProperties"], current, state.collections);

	if (foundIdx > -1) {
		var newMappings = (0, _utilGetIn2["default"])([action.collection, "mappings"], state.collections).filter(function (m, i) {
			return i !== foundIdx;
		});
		newCollections = (0, _utilSetIn2["default"])([action.collection, "mappings"], newMappings, newCollections);
	}

	return _extends({}, state, { collections: newCollections });
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "UPLOAD":
			return _extends({}, state, { collections: scaffoldCollectionMappings() });

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

module.exports = exports["default"];

},{"../util/get-in":52,"../util/persist":53,"../util/set-in":54}],48:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	data: []
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_RELATION_TYPES":
			return _extends({}, state, { data: action.data });
	}

	return state;
};

module.exports = exports["default"];

},{}],49:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _redux = require("redux");

var _reduxThunk = require("redux-thunk");

var _reduxThunk2 = _interopRequireDefault(_reduxThunk);

var _utilPersist = require("../util/persist");

var _index = require("./index");

var _index2 = _interopRequireDefault(_index);

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

var data = (0, _redux.combineReducers)(_index2["default"]);

var store = (0, _redux.createStore)(data, (0, _redux.applyMiddleware)(logger, _reduxThunk2["default"]));

window.onbeforeunload = function () {
	return (0, _utilPersist.persist)(store.getState());
};

exports["default"] = store;
module.exports = exports["default"];

},{"../util/persist":53,"./index":46,"redux":17,"redux-thunk":11}],50:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports["default"] = [{ "derived": false, "inverseName": "is_created_by", "reflexive": false, "regularName": "is_creator_of", "sourceTypeName": "archiver", "symmetric": false, "targetTypeName": "archive", "@type": "relationtype", "^created": { "timeStamp": 1411642606354, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606354, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "a69b5063-9b8b-46c0-b435-a6b05b3895d7" }, { "derived": false, "inverseName": "is_archive_keyword_of", "reflexive": false, "regularName": "has_archive_keyword", "sourceTypeName": "archive", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606480, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606480, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "4a38238d-c692-442a-8c31-334709ddf88b" }, { "derived": false, "inverseName": "is_archiver_keyword_of", "reflexive": false, "regularName": "has_archiver_keyword", "sourceTypeName": "archiver", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606500, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606500, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "7e495cd4-5ead-4ebd-82a8-6a9be856040c" }, { "derived": false, "inverseName": "is_legislation_keyword_of", "reflexive": false, "regularName": "has_legislation_keyword", "sourceTypeName": "legislation", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606511, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606511, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "422a9a94-4780-4010-945e-f3f731a54b70" }, { "derived": false, "inverseName": "is_archive_person_of", "reflexive": false, "regularName": "has_archive_person", "sourceTypeName": "archive", "symmetric": false, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606521, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606521, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "c2ae6e74-0d40-4ae7-b042-f77a692716b2" }, { "derived": false, "inverseName": "is_archiver_person_of", "reflexive": false, "regularName": "has_archiver_person", "sourceTypeName": "archiver", "symmetric": false, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606526, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606526, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "49a20dc7-48a4-439a-bfae-5fe8a428c85d" }, { "derived": false, "inverseName": "is_legislation_person_of", "reflexive": false, "regularName": "has_legislation_person", "sourceTypeName": "legislation", "symmetric": false, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606531, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606531, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "b2dbf54a-a374-429b-8313-851885187ce4" }, { "derived": false, "inverseName": "is_archive_place_of", "reflexive": false, "regularName": "has_archive_place", "sourceTypeName": "archive", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606535, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606535, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "acb9ae4a-3ec3-48d7-acbf-5c649c4f039a" }, { "derived": false, "inverseName": "is_archiver_place_of", "reflexive": false, "regularName": "has_archiver_place", "sourceTypeName": "archiver", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606541, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606541, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "cc1b1e90-c7ad-4aec-9512-56629e5a1287" }, { "derived": false, "inverseName": "is_legislation_place_of", "reflexive": false, "regularName": "has_legislation_place", "sourceTypeName": "legislation", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606546, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606546, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "46c17fd8-45a8-46e1-a816-3a42503da864" }, { "derived": false, "inverseName": "has_child_archive", "reflexive": false, "regularName": "has_parent_archive", "sourceTypeName": "archive", "symmetric": false, "targetTypeName": "archive", "@type": "relationtype", "^created": { "timeStamp": 1411642606551, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606551, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "8a76add3-e9b3-4ab4-84ba-0981159399c8" }, { "derived": false, "inverseName": "has_sibling_archive", "reflexive": false, "regularName": "has_sibling_archive", "sourceTypeName": "archive", "symmetric": true, "targetTypeName": "archive", "@type": "relationtype", "^created": { "timeStamp": 1411642606556, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606556, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "d926dec9-1f9f-44d8-9955-bfa7c69aac8b" }, { "derived": false, "inverseName": "has_sibling_archiver", "reflexive": false, "regularName": "has_sibling_archiver", "sourceTypeName": "archiver", "symmetric": true, "targetTypeName": "archiver", "@type": "relationtype", "^created": { "timeStamp": 1411642606568, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606568, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "7e6625b3-da9e-4945-b3cc-805976258e55" }, { "derived": false, "inverseName": "isBirthPlaceOf", "reflexive": false, "regularName": "hasBirthPlace", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "location", "@type": "relationtype", "^created": { "timeStamp": 1411642606572, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606572, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "12464957-59a5-4837-ba56-6ffd0fdafffc" }, { "derived": false, "inverseName": "isDeathPlaceOf", "reflexive": false, "regularName": "hasDeathPlace", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "location", "@type": "relationtype", "^created": { "timeStamp": 1411642606576, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606576, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "8f40f698-6bfc-46ab-ad9c-d28f90e8784e" }, { "derived": false, "inverseName": "isResidenceLocationOf", "reflexive": false, "regularName": "hasResidenceLocation", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "location", "@type": "relationtype", "^created": { "timeStamp": 1411642606580, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606580, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "83e3a1d2-c339-40a6-bd20-9cbd64c74932" }, { "derived": false, "inverseName": "isGenreOf", "reflexive": false, "regularName": "hasGenre", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606585, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606585, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "e932e2bb-4464-493b-9c88-2b28654492ef" }, { "derived": true, "inverseName": "isPersonLanguageOf", "reflexive": false, "regularName": "hasPersonLanguage", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "language", "@type": "relationtype", "^created": { "timeStamp": 1411642606589, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606589, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "bba10d37-86cc-4f1f-ba2d-016af2b21aa4" }, { "derived": false, "inverseName": "isWorkLanguageOf", "reflexive": false, "regularName": "hasWorkLanguage", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "language", "@type": "relationtype", "^created": { "timeStamp": 1411642606593, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606593, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "acd65f6a-73b1-48ec-a5a3-0057091939e7" }, { "derived": false, "inverseName": "isLocationOf", "reflexive": false, "regularName": "hasLocation", "sourceTypeName": "collective", "symmetric": false, "targetTypeName": "location", "@type": "relationtype", "^created": { "timeStamp": 1411642606598, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606598, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "55932384-528e-4a80-aa54-46559362a78c" }, { "derived": false, "inverseName": "isPublishLocationOf", "reflexive": false, "regularName": "hasPublishLocation", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "location", "@type": "relationtype", "^created": { "timeStamp": 1411642606602, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606602, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "4949b109-6103-4619-820a-67c9fa128d01" }, { "derived": false, "inverseName": "isCollaboratorOf", "reflexive": false, "regularName": "isCollaboratorOf", "sourceTypeName": "person", "symmetric": true, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606606, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606606, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "e487ce11-67b5-4b05-b103-69e236bfa875" }, { "derived": false, "inverseName": "isCreatorOf", "reflexive": false, "regularName": "isCreatedBy", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606610, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606610, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "4d139c1d-d6c7-48dc-82ec-174c67908d5b" }, { "derived": false, "inverseName": "hasMember", "reflexive": false, "regularName": "isMemberOf", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "collective", "@type": "relationtype", "^created": { "timeStamp": 1411642606614, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606614, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "dcf1217e-6333-4f7b-bd7c-a8ba635c3e6b" }, { "derived": false, "inverseName": "hasPseudonym", "reflexive": false, "regularName": "isPseudonymOf", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606618, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606618, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "d1f6b0fd-56ef-48c8-ad57-8149deb457c9" }, { "derived": false, "inverseName": "isPublisherOf", "reflexive": false, "regularName": "isPublishedBy", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "collective", "@type": "relationtype", "^created": { "timeStamp": 1411642606622, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606622, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "279a8933-0dbc-4f9e-82df-bf70ec08994a" }, { "derived": false, "inverseName": "isRelatedTo", "reflexive": false, "regularName": "isRelatedTo", "sourceTypeName": "person", "symmetric": true, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606626, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606626, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "25a49497-f4fd-4179-824c-78ad7274e812" }, { "derived": false, "inverseName": "isChildOf", "reflexive": false, "regularName": "isParentOf", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606631, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606631, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "8212e35d-6b14-47fd-9a82-47b764143db2" }, { "derived": false, "inverseName": "isSpouseOf", "reflexive": false, "regularName": "isSpouseOf", "sourceTypeName": "person", "symmetric": true, "targetTypeName": "person", "@type": "relationtype", "^created": { "timeStamp": 1411642606635, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606635, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "a68b9025-27ae-4e85-ab07-528b3e636105" }, { "derived": false, "inverseName": "isStorageOf", "reflexive": false, "regularName": "isStoredAt", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "collective", "@type": "relationtype", "^created": { "timeStamp": 1411642606648, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606648, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "63ed1353-d904-45b4-80a8-2d5beade6191" }, { "derived": false, "inverseName": "isEditionOf", "reflexive": false, "regularName": "hasEdition", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606652, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606652, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "eb909e6a-b87b-41a8-9ac2-f7207052c09b" }, { "derived": false, "inverseName": "isSequelOf", "reflexive": false, "regularName": "hasSequel", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606656, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606656, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "90418e86-2b40-45e1-98cd-b2d4f067abf5" }, { "derived": false, "inverseName": "isTranslationOf", "reflexive": false, "regularName": "hasTranslation", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606660, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606660, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "2a6b7df7-a4f9-4443-8480-5a080692c917" }, { "derived": false, "inverseName": "isAdaptationOf", "reflexive": false, "regularName": "hasAdaptation", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606664, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606664, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "08965f9d-686c-4d2c-bc78-a819e142f4dd" }, { "derived": false, "inverseName": "isPlagiarismOf", "reflexive": false, "regularName": "hasPlagiarismBy", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606669, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606669, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "4c1c3f71-ecac-4554-8e79-17186dc31af3" }, { "derived": false, "inverseName": "hasAnnotationsOn", "reflexive": false, "regularName": "isAnnotatedIn", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606673, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606673, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "2462412e-a693-4905-a88d-4c607401799e" }, { "derived": false, "inverseName": "isBibliographyOf", "reflexive": false, "regularName": "hasBibliography", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606677, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606677, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "98caa90d-3b9e-454c-808f-79da5b489018" }, { "derived": false, "inverseName": "isBiographyOf", "reflexive": false, "regularName": "hasBiography", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606680, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606680, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "419c927b-08dd-4bfe-b46e-0bdb0310741e" }, { "derived": false, "inverseName": "isCensoringOf", "reflexive": false, "regularName": "isCensoredBy", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606684, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606684, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "2b0cd389-2296-45dc-aba7-e7031cb15739" }, { "derived": false, "inverseName": "commentsOnPerson", "reflexive": false, "regularName": "isPersonCommentedOnIn", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606688, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606688, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "f3002af0-d9b9-4bba-93aa-d3eb04a316cd" }, { "derived": false, "inverseName": "commentsOnWork", "reflexive": false, "regularName": "isWorkCommentedOnIn", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606693, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606693, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "46bf14ba-d403-4283-95b2-aa13472d3982" }, { "derived": false, "inverseName": "isAnthologyContaining", "reflexive": false, "regularName": "containedInAnthology", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606697, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606697, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "88a6b99e-54f0-4acc-a717-978e68cc18b1" }, { "derived": false, "inverseName": "isCopyOf", "reflexive": false, "regularName": "isCopiedBy", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606700, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606700, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "e6ff8886-4f32-4545-a0e6-81979af44262" }, { "derived": false, "inverseName": "isDedicatedTo", "reflexive": false, "regularName": "isDedicatedPersonOf", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606704, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606704, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "a8290105-6dc1-4af4-88a8-5c73eb24c709" }, { "derived": false, "inverseName": "isAwardForPerson", "reflexive": false, "regularName": "isPersonAwarded", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606708, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606708, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "20aa9556-00ca-463b-9fe6-45bf20747391" }, { "derived": false, "inverseName": "isAwardForWork", "reflexive": false, "regularName": "isWorkAwarded", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606712, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606712, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "3d279b2e-b26f-46e4-858e-ffb13d9617b4" }, { "derived": false, "inverseName": "isPrefaceOf", "reflexive": false, "regularName": "hasPreface", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606716, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606716, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "6ae39b1e-6312-45bd-93d8-17ee91189f26" }, { "derived": false, "inverseName": "isIntertextualTo", "reflexive": false, "regularName": "isIntertextualOf", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606721, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606721, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "e39f067e-ac14-48c1-b2fe-3f7b248dce97" }, { "derived": false, "inverseName": "listsPerson", "reflexive": false, "regularName": "isPersonListedOn", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606725, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606725, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "5fa85017-ced3-4ed4-afb2-93d753fd9507" }, { "derived": false, "inverseName": "listsWork", "reflexive": false, "regularName": "isWorkListedOn", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606729, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606729, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "fe1b9b0b-fca3-48b9-87bb-1aa4b2705c0d" }, { "derived": false, "inverseName": "mentionsPerson", "reflexive": false, "regularName": "isPersonMentionedIn", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606733, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606733, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "fedbd566-b74b-4fe7-a6c2-5311b846c1b3" }, { "derived": false, "inverseName": "mentionsWork", "reflexive": false, "regularName": "isWorkMentionedIn", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606737, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606737, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "3ca7ccdd-60ea-4255-ab75-e9a9508db107" }, { "derived": false, "inverseName": "isObituaryOf", "reflexive": false, "regularName": "hasObituary", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606741, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606741, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "de64e0f0-3797-4e1f-840c-0cab1136f997" }, { "derived": false, "inverseName": "isParodyOf", "reflexive": false, "regularName": "isParodiedBy", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606745, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606745, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "134342cc-3395-4cc8-ac3d-4fd43f1e303b" }, { "derived": false, "inverseName": "quotesPerson", "reflexive": false, "regularName": "isPersonQuotedIn", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606748, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606748, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "c3adaf6f-4556-4782-88f7-704975d57e4a" }, { "derived": false, "inverseName": "quotesWork", "reflexive": false, "regularName": "isWorkQuotedIn", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606752, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606752, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "86c9b977-7df3-424c-830c-8f4aa2051a7b" }, { "derived": false, "inverseName": "referencesPerson", "reflexive": false, "regularName": "isPersonReferencedIn", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606756, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606756, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "a22a78d4-eb9d-4d99-bc7a-f2be6ce4d58e" }, { "derived": false, "inverseName": "referencesWork", "reflexive": false, "regularName": "isWorkReferencedIn", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606760, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606760, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "a1bf44ce-5086-4508-8441-b414d5fb4b69" }, { "derived": false, "inverseName": "isSourceCategoryOf", "reflexive": false, "regularName": "hasSourceCategory", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606764, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606764, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "1b2ec81e-697d-4e6c-92a9-3af99f85c8a4" }, { "derived": false, "inverseName": "isDocumentSourceOf", "reflexive": false, "regularName": "hasDocumentSource", "sourceTypeName": "document", "symmetric": false, "targetTypeName": "document", "@type": "relationtype", "^created": { "timeStamp": 1411642606768, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606768, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "6b8c2f13-882f-4789-834e-bebca2796a2e" }, { "derived": false, "inverseName": "isEducationOf", "reflexive": false, "regularName": "hasEducation", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606772, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606772, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "e703c3de-67ed-4980-8db3-763dcf07fdf5" }, { "derived": false, "inverseName": "isFinancialSituationOf", "reflexive": false, "regularName": "hasFinancialSituation", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606776, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606776, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "28a5b1a7-31e1-41b7-abd0-5ec17c66d12c" }, { "derived": false, "inverseName": "isMaritalStatusOf", "reflexive": false, "regularName": "hasMaritalStatus", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606780, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606780, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "f5a07168-15da-420d-aaee-7402c6feb64e" }, { "derived": false, "inverseName": "isProfessionOf", "reflexive": false, "regularName": "hasProfession", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606784, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606784, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "254061f4-42ca-4750-834b-7a7a52dd962d" }, { "derived": false, "inverseName": "isReligionOf", "reflexive": false, "regularName": "hasReligion", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606788, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606788, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "d79a9e94-c36b-4308-9440-535b4b1b6815" }, { "derived": false, "inverseName": "isSocialClassOf", "reflexive": false, "regularName": "hasSocialClass", "sourceTypeName": "person", "symmetric": false, "targetTypeName": "keyword", "@type": "relationtype", "^created": { "timeStamp": 1411642606791, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^modified": { "timeStamp": 1411642606791, "userId": "timbuctoo", "vreId": "timbuctoo" }, "^rev": 1, "_id": "c17de8ea-48b4-439f-aabc-5efeb44232f2" }];
module.exports = exports["default"];

},{}],51:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
    value: true
});
function deepClone9(obj) {
    var i, len, ret;

    if (typeof obj !== "object" || obj === null) {
        return obj;
    }

    if (Array.isArray(obj)) {
        ret = [];
        len = obj.length;
        for (i = 0; i < len; i++) {
            ret.push(typeof obj[i] === "object" && obj[i] !== null ? deepClone9(obj[i]) : obj[i]);
        }
    } else {
        ret = {};
        for (i in obj) {
            if (obj.hasOwnProperty(i)) {
                ret[i] = typeof obj[i] === "object" && obj[i] !== null ? deepClone9(obj[i]) : obj[i];
            }
        }
    }
    return ret;
}

exports["default"] = deepClone9;
module.exports = exports["default"];

},{}],52:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _cloneDeep = require("./clone-deep");

var _cloneDeep2 = _interopRequireDefault(_cloneDeep);

var _getIn = function _getIn(_x, _x2) {
	var _again = true;

	_function: while (_again) {
		var path = _x,
		    data = _x2;
		_again = false;

		if (data) {
			if (path.length === 0) {
				return data;
			} else {
				_x = path;
				_x2 = data[path.shift()];
				_again = true;
				continue _function;
			}
		} else {
			return null;
		}
	}
};

var getIn = function getIn(path, data) {
	return _getIn((0, _cloneDeep2["default"])(path), data);
};

exports["default"] = getIn;
module.exports = exports["default"];

},{"./clone-deep":51}],53:[function(require,module,exports){
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

exports.persist = persist;
exports.getItem = getItem;
exports.disablePersist = disablePersist;

},{}],54:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _cloneDeep = require("./clone-deep");

var _cloneDeep2 = _interopRequireDefault(_cloneDeep);

// Do either of these:
//  a) Set a value by reference if deref is not null
//  b) Set a value directly in to data object if deref is null
var setEither = function setEither(data, deref, key, val) {
	(deref || data)[key] = val;
	return data;
};

// Set a nested value in data (not unlike immutablejs, but a clone of data is expected for proper immutability)
var _setIn = function _setIn(_x2, _x3, _x4) {
	var _arguments = arguments;
	var _again = true;

	_function: while (_again) {
		var path = _x2,
		    value = _x3,
		    data = _x4;
		_again = false;
		var deref = _arguments.length <= 3 || _arguments[3] === undefined ? null : _arguments[3];

		if (path.length > 1) {
			_arguments = [_x2 = path, _x3 = value, _x4 = data, deref ? deref[path.shift()] : data[path.shift()]];
			_again = true;
			deref = undefined;
			continue _function;
		} else {
			return setEither(data, deref, path[0], value);
		}
	}
};

var setIn = function setIn(path, value, data) {
	return _setIn((0, _cloneDeep2["default"])(path), value, (0, _cloneDeep2["default"])(data));
};

exports["default"] = setIn;
module.exports = exports["default"];

},{"./clone-deep":51}]},{},[43])(43)
});