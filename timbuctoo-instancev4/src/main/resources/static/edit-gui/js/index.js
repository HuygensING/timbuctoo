(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.TimbuctooEdit = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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
var overArg = require('./_overArg');

/** Built-in value references. */
var getPrototype = overArg(Object.getPrototypeOf, Object);

module.exports = getPrototype;

},{"./_overArg":6}],6:[function(require,module,exports){
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
  return value != null && typeof value == 'object';
}

module.exports = isObjectLike;

},{}],8:[function(require,module,exports){
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

},{"./_getPrototype":5,"./isObjectLike":7}],9:[function(require,module,exports){
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
},{}],11:[function(require,module,exports){
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
},{"./compose":14}],12:[function(require,module,exports){
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
},{}],13:[function(require,module,exports){
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
},{"./createStore":15,"./utils/warning":17,"_process":1,"lodash/isPlainObject":8}],14:[function(require,module,exports){
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
},{}],15:[function(require,module,exports){
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
},{"lodash/isPlainObject":8,"symbol-observable":18}],16:[function(require,module,exports){
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
},{"./applyMiddleware":11,"./bindActionCreators":12,"./combineReducers":13,"./compose":14,"./createStore":15,"./utils/warning":17,"_process":1}],17:[function(require,module,exports){
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
},{}],18:[function(require,module,exports){
module.exports = require('./lib/index');

},{"./lib/index":19}],19:[function(require,module,exports){
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
},{"./ponyfill":20}],20:[function(require,module,exports){
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

},{"global/window":3,"is-function":4,"parse-headers":9,"xtend":23}],23:[function(require,module,exports){
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

},{}],24:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

var _config = require("../config");

var _config2 = _interopRequireDefault(_config);

exports["default"] = function (path, query, done) {
	var options = {
		url: _config2["default"].apiUrl[_config2["default"].apiVersion] + "/" + path.replace(/^\/v[^/]+\//, "") + "?query=" + query + "*"
	};

	var xhrDone = function xhrDone(err, response, body) {
		done(JSON.parse(body).map(function (d) {
			return { key: d.key.replace(/^.+\//, ""), value: d.value };
		}));
	};

	_server2["default"].fastXhr(options, xhrDone);
};

module.exports = exports["default"];

},{"../config":50,"./server":30}],25:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

var _config = require("../config");

var _config2 = _interopRequireDefault(_config);

var saveNewEntity = function saveNewEntity(domain, saveData, token, vreId, next, fail) {
	return _server2["default"].performXhr({
		method: "POST",
		headers: _server2["default"].makeHeaders(token, vreId),
		body: JSON.stringify(saveData),
		url: _config2["default"].apiUrl[_config2["default"].apiVersion] + "/domain/" + domain
	}, next, fail, "Create new " + domain);
};

var updateEntity = function updateEntity(domain, saveData, token, vreId, next, fail) {
	return _server2["default"].performXhr({
		method: "PUT",
		headers: _server2["default"].makeHeaders(token, vreId),
		body: JSON.stringify(saveData),
		url: _config2["default"].apiUrl[_config2["default"].apiVersion] + "/domain/" + domain + "/" + saveData._id
	}, next, fail, "Update " + domain);
};

var deleteEntity = function deleteEntity(domain, entityId, token, vreId, next, fail) {
	return _server2["default"].performXhr({
		method: "DELETE",
		headers: _server2["default"].makeHeaders(token, vreId),
		url: _config2["default"].apiUrl[_config2["default"].apiVersion] + "/domain/" + domain + "/" + entityId
	}, next, fail, "Delete " + domain);
};

var fetchEntity = function fetchEntity(location, next, fail) {
	return _server2["default"].performXhr({
		method: "GET",
		headers: { "Accept": "application/json" },
		url: location
	}, function (err, resp) {
		var data = JSON.parse(resp.body);
		next(data);
	}, fail, "Fetch entity");
};

var fetchEntityList = function fetchEntityList(domain, start, rows, next) {
	return _server2["default"].performXhr({
		method: "GET",
		headers: { "Accept": "application/json" },
		url: _config2["default"].apiUrl[_config2["default"].apiVersion] + "/domain/" + domain + "?rows=" + rows + "&start=" + start
	}, function (err, resp) {
		var data = JSON.parse(resp.body);
		next(data);
	});
};

var crud = {
	saveNewEntity: saveNewEntity,
	updateEntity: updateEntity,
	deleteEntity: deleteEntity,
	fetchEntity: fetchEntity,
	fetchEntityList: fetchEntityList
};

exports.saveNewEntity = saveNewEntity;
exports.updateEntity = updateEntity;
exports.deleteEntity = deleteEntity;
exports.fetchEntity = fetchEntity;
exports.fetchEntityList = fetchEntityList;
exports.crud = crud;

},{"../config":50,"./server":30}],26:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _utilCloneDeep = require("../util/clone-deep");

var _utilCloneDeep2 = _interopRequireDefault(_utilCloneDeep);

var _crud = require("./crud");

var _relationSavers = require("./relation-savers");

var _relationSavers2 = _interopRequireDefault(_relationSavers);

var _config = require("../config");

var _config2 = _interopRequireDefault(_config);

var _autocomplete = require("./autocomplete");

var _autocomplete2 = _interopRequireDefault(_autocomplete);

// Skeleton base data per field definition
var initialData = {
	names: [],
	multiselect: [],
	links: [],
	keyword: [],
	"list-of-strings": [],
	altnames: [],
	text: "",
	string: "",
	select: "",
	datable: ""
};

// Return the initial data for the type in the field definition
var initialDataForType = function initialDataForType(fieldDef) {
	return fieldDef.defaultValue || (fieldDef.type === "relation" || fieldDef.type === "keyword" ? {} : initialData[fieldDef.type]);
};

// Return the initial name-key for a certain field type
var nameForType = function nameForType(fieldDef) {
	return fieldDef.type === "relation" || fieldDef.type === "keyword" ? "@relations" : fieldDef.name;
};

// Create a new empty entity based on the fieldDefinitions
var makeSkeleton = function makeSkeleton(vre, domain) {
	if (vre && vre.collections && vre.collections[domain] && vre.collections[domain].properties) {
		return vre.collections[domain].properties.map(function (fieldDef) {
			return [nameForType(fieldDef), initialDataForType(fieldDef)];
		}).concat([["@type", domain.replace(/s$/, "")]]).reduce(function (obj, cur) {
			obj[cur[0]] = cur[1];
			return obj;
		}, {});
	}
};

var fetchEntityList = function fetchEntityList(domain) {
	return function (dispatch, getState) {
		dispatch({ type: "SET_PAGINATION_START", start: 0 });
		_crud.crud.fetchEntityList(domain, 0, getState().quickSearch.rows, function (data) {
			return dispatch({ type: "RECEIVE_ENTITY_LIST", data: data });
		});
	};
};

var paginateLeft = function paginateLeft() {
	return function (dispatch, getState) {
		var newStart = getState().quickSearch.start - getState().quickSearch.rows;
		dispatch({ type: "SET_PAGINATION_START", start: newStart < 0 ? 0 : newStart });
		_crud.crud.fetchEntityList(getState().entity.domain, newStart < 0 ? 0 : newStart, getState().quickSearch.rows, function (data) {
			return dispatch({ type: "RECEIVE_ENTITY_LIST", data: data });
		});
	};
};

var paginateRight = function paginateRight() {
	return function (dispatch, getState) {
		var newStart = getState().quickSearch.start + getState().quickSearch.rows;
		dispatch({ type: "SET_PAGINATION_START", start: newStart });
		_crud.crud.fetchEntityList(getState().entity.domain, newStart, getState().quickSearch.rows, function (data) {
			return dispatch({ type: "RECEIVE_ENTITY_LIST", data: data });
		});
	};
};

var sendQuickSearch = function sendQuickSearch() {
	return function (dispatch, getState) {
		var _getState = getState();

		var quickSearch = _getState.quickSearch;
		var entity = _getState.entity;
		var vre = _getState.vre;

		if (quickSearch.query.length) {
			dispatch({ type: "SET_PAGINATION_START", start: 0 });
			var callback = function callback(data) {
				return dispatch({ type: "RECEIVE_ENTITY_LIST", data: data.map(function (d) {
						return {
							_id: d.key.replace(/.*\//, ""),
							"@displayName": d.value
						};
					}) });
			};
			(0, _autocomplete2["default"])("domain/" + entity.domain + "/autocomplete", quickSearch.query, callback);
		} else {
			dispatch(fetchEntityList(entity.domain));
		}
	};
};

// 1) Fetch entity
// 2) Dispatch RECEIVE_ENTITY for render
var selectEntity = function selectEntity(domain, entityId) {
	var errorMessage = arguments.length <= 2 || arguments[2] === undefined ? null : arguments[2];
	var successMessage = arguments.length <= 3 || arguments[3] === undefined ? null : arguments[3];
	var next = arguments.length <= 4 || arguments[4] === undefined ? function () {} : arguments[4];
	return function (dispatch) {
		_crud.crud.fetchEntity(_config2["default"].apiUrl[_config2["default"].apiVersion] + "/domain/" + domain + "/" + entityId, function (data) {
			dispatch({ type: "RECEIVE_ENTITY", domain: domain, data: data, errorMessage: errorMessage });
			if (successMessage !== null) {
				dispatch({ type: "SUCCESS_MESSAGE", message: successMessage });
			}
		}, function () {
			return dispatch({ type: "RECEIVE_ENTITY_FAILURE", errorMessage: "Failed to fetch " + domain + " with ID " + entityId });
		});
		next();
	};
};

// 1) Dispatch RECEIVE_ENTITY with empty entity skeleton for render
var makeNewEntity = function makeNewEntity(domain) {
	var errorMessage = arguments.length <= 1 || arguments[1] === undefined ? null : arguments[1];
	return function (dispatch, getState) {
		return dispatch({
			type: "RECEIVE_ENTITY",
			domain: domain,
			data: makeSkeleton(getState().vre, domain) || {},
			errorMessage: errorMessage
		});
	};
};

var deleteEntity = function deleteEntity() {
	return function (dispatch, getState) {
		_crud.crud.deleteEntity(getState().entity.domain, getState().entity.data._id, getState().user.token, getState().vre.vreId, function () {
			dispatch({ type: "SUCCESS_MESSAGE", message: "Sucessfully deleted " + getState().entity.domain + " with ID " + getState().entity.data._id });
			dispatch(makeNewEntity(getState().entity.domain));
			dispatch(fetchEntityList(getState().entity.domain));
		}, function () {
			return dispatch(selectEntity(getState().entity.domain, getState().entity.data._id, "Failed to delete " + getState().entity.domain + " with ID " + getState().entity.data._id));
		});
	};
};

// 1) Save an entity
// 2) Save the relations for this entity
// 3) Refetch entity for render
var saveEntity = function saveEntity() {
	return function (dispatch, getState) {
		// Make a deep copy of the data to be saved in order to leave application state unaltered
		var saveData = (0, _utilCloneDeep2["default"])(getState().entity.data);
		// Make a deep copy of the relation data in order to leave application state unaltered
		var relationData = (0, _utilCloneDeep2["default"])(saveData["@relations"]) || {};
		// Delete the relation data from the saveData as it is not expected by the server
		delete saveData["@relations"];

		if (getState().entity.data._id) {
			// 1) Update the entity with saveData
			_crud.crud.updateEntity(getState().entity.domain, saveData, getState().user.token, getState().vre.vreId, function (err, resp) {
				return(
					// 2) Save relations using server response for current relations to diff against relationData
					dispatch(function (redispatch) {
						return _relationSavers2["default"][_config2["default"].apiVersion](JSON.parse(resp.body), relationData, getState().vre.collections[getState().entity.domain].properties, getState().user.token, getState().vre.vreId, function () {
							return(
								// 3) Refetch entity for render
								redispatch(selectEntity(getState().entity.domain, getState().entity.data._id, null, "Succesfully saved " + getState().entity.domain + " with ID " + getState().entity.data._id, function () {
									return dispatch(fetchEntityList(getState().entity.domain));
								}))
							);
						});
					})
				);
			}, function () {
				return(
					// 2a) Handle error by refetching and passing along an error message
					dispatch(selectEntity(getState().entity.domain, getState().entity.data._id, "Failed to save " + getState().entity.domain + " with ID " + getState().entity.data._id))
				);
			});
		} else {
			// 1) Create new entity with saveData
			_crud.crud.saveNewEntity(getState().entity.domain, saveData, getState().user.token, getState().vre.vreId, function (err, resp) {
				return(
					// 2) Fetch entity via location header
					dispatch(function (redispatch) {
						return _crud.crud.fetchEntity(resp.headers.location, function (data) {
							return(
								// 3) Save relations using server response for current relations to diff against relationData
								_relationSavers2["default"][_config2["default"].apiVersion](data, relationData, getState().vre.collections[getState().entity.domain].properties, getState().user.token, getState().vre.vreId, function () {
									return(
										// 4) Refetch entity for render
										redispatch(selectEntity(getState().entity.domain, data._id, null, "Succesfully saved " + getState().entity.domain, function () {
											return dispatch(fetchEntityList(getState().entity.domain));
										}))
									);
								})
							);
						});
					})
				);
			}, function () {
				return(
					// 2a) Handle error by refetching and passing along an error message
					dispatch(makeNewEntity(getState().entity.domain, "Failed to save new " + getState().entity.domain))
				);
			});
		}
	};
};

exports.saveEntity = saveEntity;
exports.selectEntity = selectEntity;
exports.makeNewEntity = makeNewEntity;
exports.deleteEntity = deleteEntity;
exports.fetchEntityList = fetchEntityList;
exports.paginateRight = paginateRight;
exports.paginateLeft = paginateLeft;
exports.sendQuickSearch = sendQuickSearch;

},{"../config":50,"../util/clone-deep":59,"./autocomplete":24,"./crud":25,"./relation-savers":28}],27:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _store = require("../store");

var _store2 = _interopRequireDefault(_store);

var _entity = require("./entity");

var _vre = require("./vre");

var setUser = function setUser(response) {
	return {
		type: "SET_USER",
		user: response
	};
};

exports["default"] = {
	onNew: function onNew(domain) {
		return _store2["default"].dispatch((0, _entity.makeNewEntity)(domain));
	},
	onSelect: function onSelect(record) {
		return _store2["default"].dispatch((0, _entity.selectEntity)(record.domain, record.id));
	},
	onSave: function onSave() {
		return _store2["default"].dispatch((0, _entity.saveEntity)());
	},
	onDelete: function onDelete() {
		return _store2["default"].dispatch((0, _entity.deleteEntity)());
	},
	onChange: function onChange(fieldPath, value) {
		return _store2["default"].dispatch({ type: "SET_ENTITY_FIELD_VALUE", fieldPath: fieldPath, value: value });
	},
	onLoginChange: function onLoginChange(response) {
		return _store2["default"].dispatch(setUser(response));
	},
	onSelectVre: function onSelectVre(vreId) {
		return _store2["default"].dispatch((0, _vre.setVre)(vreId));
	},
	onDismissMessage: function onDismissMessage(messageIndex) {
		return _store2["default"].dispatch({ type: "DISMISS_MESSAGE", messageIndex: messageIndex });
	},
	onSelectDomain: function onSelectDomain(domain) {
		_store2["default"].dispatch({ type: "SET_DOMAIN", domain: domain });_store2["default"].dispatch((0, _entity.fetchEntityList)(domain));_store2["default"].dispatch({ type: "SET_QUICKSEARCH_QUERY", value: "" });
	},
	onPaginateLeft: function onPaginateLeft() {
		return _store2["default"].dispatch((0, _entity.paginateLeft)());
	},
	onPaginateRight: function onPaginateRight() {
		return _store2["default"].dispatch((0, _entity.paginateRight)());
	},
	onQuickSearchQueryChange: function onQuickSearchQueryChange(value) {
		return _store2["default"].dispatch({ type: "SET_QUICKSEARCH_QUERY", value: value });
	},
	onQuickSearch: function onQuickSearch() {
		return _store2["default"].dispatch((0, _entity.sendQuickSearch)());
	}
};
module.exports = exports["default"];

},{"../store":58,"./entity":26,"./vre":32}],28:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _saveRelations = require("./save-relations");

var _saveRelations2 = _interopRequireDefault(_saveRelations);

var _v21SaveRelations = require("./v2.1/save-relations");

var _v21SaveRelations2 = _interopRequireDefault(_v21SaveRelations);

exports["default"] = {
	v4: _v21SaveRelations2["default"],
	"v2.1": _v21SaveRelations2["default"]
};
module.exports = exports["default"];

},{"./save-relations":29,"./v2.1/save-relations":31}],29:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i]; return arr2; } else { return Array.from(arr); } }

var _crud = require("./crud");

// Save relations described in relationData
// a) create all relations which are relationData but not in data["@relations"]
// b) delete all relations which are in data["@relations"] but not in relationData
// c) ignore all relations which are in both
var saveRelations = function saveRelations(data, relationData, fieldDefs, token, vreId, next) {

	// Returns the domain based on the fieldDefinitions and the relation key (i.e. "hasBirthPlace")
	var makeNewRelationArgs = function makeNewRelationArgs(relation, key) {
		var fieldDef = fieldDefs.find(function (def) {
			return def.name === key;
		});
		return [fieldDef.relation.relationCollection, // domain
		{
			"@type": fieldDef.relation.relationCollection.replace(/s$/, ""),
			"^sourceId": fieldDef.relation.direction === "IN" ? relation.id : data._id,
			"^sourceType": data["@type"],
			"^targetId": fieldDef.relation.direction === "IN" ? data._id : relation.id,
			"^targetType": fieldDef.relation.targetCollection.replace(/s$/, ""),
			"^typeId": fieldDef.relation.relationTypeId,
			accepted: true
		}];
	};

	// Constructs an array of arguments for saving new relations:
	// [
	//   ["wwrelations", { ... }],
	//   ["wwrelations", { ... }],
	// ]
	var newRelations = Object.keys(relationData).map(function (key) {
		return relationData[key]
		// Filters out all relations which are not already in data["@relations"]
		.filter(function (relation) {
			return (data["@relations"][key] || []).map(function (origRelation) {
				return origRelation.id;
			}).indexOf(relation.id) < 0;
		})
		// Make argument array for new relations: ["wwrelations", { ... }]
		.map(function (relation) {
			return makeNewRelationArgs(relation, key);
		});
	}
	// Flatten nested arrays
	).reduce(function (a, b) {
		return a.concat(b);
	}, []);

	// Constructs an array of arguments for deleting existing relations:
	// [
	//   ["wwrelations", ":relationId"],
	//   ["wwrelations", ":relationId"],
	// ]
	var deleteRelations = Object.keys(data["@relations"]).map(function (key) {
		return data["@relations"][key]
		// Filters out all relations which still in data["@relations"] but not in relationData
		.filter(function (origRelation) {
			return (relationData[key] || []).map(function (relation) {
				return relation.id;
			}).indexOf(origRelation.id) < 0;
		})
		// Make argument array for deleted relations
		.map(function (relation) {
			return [fieldDefs.find(function (def) {
				return def.name === key;
			}).relation.relationCollection, relation.relationId];
		});
	}
	// Flatten nested arrays
	).reduce(function (a, b) {
		return a.concat(b);
	}, []);

	// Combines saveNewEntity and deleteEntity instructions into promises
	var promises = newRelations
	// Map newRelations to promised invocations of saveNewEntity
	.map(function (args) {
		return new Promise(function (resolve, reject) {
			return _crud.saveNewEntity.apply(undefined, _toConsumableArray(args).concat([token, vreId, resolve, reject]));
		});
	})
	// Map deleteRelations to promised invocations of deleteEntity
	.concat(deleteRelations.map(function (args) {
		return new Promise(function (resolve, reject) {
			return _crud.deleteEntity.apply(undefined, _toConsumableArray(args).concat([token, vreId, resolve, reject]));
		});
	}));

	// Invoke all CRUD operations for the relations
	Promise.all(promises).then(next, next);
};

exports["default"] = saveRelations;
module.exports = exports["default"];

},{"./crud":25}],30:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _store = require("../store");

var _store2 = _interopRequireDefault(_store);

exports["default"] = {
	performXhr: function performXhr(options, accept) {
		var reject = arguments.length <= 2 || arguments[2] === undefined ? function () {
			console.warn("Undefined reject callback! ");(console.trace || function () {})();
		} : arguments[2];
		var operation = arguments.length <= 3 || arguments[3] === undefined ? "Server request" : arguments[3];

		_store2["default"].dispatch({ type: "REQUEST_MESSAGE", message: operation + ": " + (options.method || "GET") + " " + options.url });
		(0, _xhr2["default"])(options, function (err, resp, body) {
			if (resp.statusCode >= 400) {
				_store2["default"].dispatch({ type: "ERROR_MESSAGE", message: operation + " failed with cause: " + resp.body });
				reject(err, resp, body);
			} else {
				accept(err, resp, body);
			}
		});
	},

	fastXhr: function fastXhr(options, accept) {
		(0, _xhr2["default"])(options, accept);
	},

	makeHeaders: function makeHeaders(token, vreId) {
		return {
			"Accept": "application/json",
			"Content-type": "application/json",
			"Authorization": token,
			"VRE_ID": vreId
		};
	}
};
module.exports = exports["default"];

},{"../store":58,"xhr":22}],31:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i]; return arr2; } else { return Array.from(arr); } }

var _crud = require("../crud");

var saveRelationsV21 = function saveRelationsV21(data, relationData, fieldDefs, token, vreId, next) {
	// Returns the domain based on the fieldDefinitions and the relation key (i.e. "hasBirthPlace")
	var makeRelationArgs = function makeRelationArgs(relation, key) {
		var accepted = arguments.length <= 2 || arguments[2] === undefined ? true : arguments[2];
		var id = arguments.length <= 3 || arguments[3] === undefined ? null : arguments[3];
		var rev = arguments.length <= 4 || arguments[4] === undefined ? null : arguments[4];

		var fieldDef = fieldDefs.find(function (def) {
			return def.name === key;
		});

		var sourceType = data["@type"].replace(/s$/, "").replace(/^ww/, "");
		var targetType = fieldDef.relation.targetCollection.replace(/s$/, "").replace(/^ww/, "");

		var relationSaveData = {
			"@type": fieldDef.relation.relationCollection.replace(/s$/, ""), // check
			"^sourceId": fieldDef.relation.direction === "IN" ? relation.id : data._id, // check
			"^sourceType": fieldDef.relation.direction === "IN" ? targetType : sourceType, // check
			"^targetId": fieldDef.relation.direction === "IN" ? data._id : relation.id, // check
			"^targetType": fieldDef.relation.direction === "IN" ? sourceType : targetType,
			"^typeId": fieldDef.relation.relationTypeId, // check
			accepted: accepted
		};

		if (id) {
			relationSaveData._id = id;
		}
		if (rev) {
			relationSaveData["^rev"] = rev;
		}
		return [fieldDef.relation.relationCollection, // domain
		relationSaveData];
	};

	// Constructs an array of arguments for saving new relations:
	// [
	//   ["wwrelations", { ... }],
	//   ["wwrelations", { ... }],
	// ]
	var newRelations = Object.keys(relationData).map(function (key) {
		return relationData[key]
		// Filters out all relations which are not already in data["@relations"]
		.filter(function (relation) {
			return (data["@relations"][key] || []).map(function (origRelation) {
				return origRelation.id;
			}).indexOf(relation.id) < 0;
		})
		// Make argument array for new relations: ["wwrelations", { ... }]
		.map(function (relation) {
			return makeRelationArgs(relation, key);
		});
	}
	// Flatten nested arrays
	).reduce(function (a, b) {
		return a.concat(b);
	}, []);

	// Reactivate previously added relations using PUT which were 'deleted' after using PUT
	var reAddRelations = Object.keys(relationData).map(function (key) {
		return (data["@relations"][key] || []).filter(function (origRelation) {
			return origRelation.accepted === false;
		}).filter(function (origRelation) {
			return (relationData[key] || []).filter(function (relation) {
				return relation.accepted;
			}).map(function (relation) {
				return relation.id;
			}).indexOf(origRelation.id) > -1;
		}).map(function (origRelation) {
			return makeRelationArgs(origRelation, key, true, origRelation.relationId, origRelation.rev);
		});
	}).reduce(function (a, b) {
		return a.concat(b);
	}, []);

	// Deactivate previously added relations using PUT
	var deleteRelations = Object.keys(data["@relations"]).map(function (key) {
		return data["@relations"][key].filter(function (origRelation) {
			return origRelation.accepted;
		}).filter(function (origRelation) {
			return (relationData[key] || []).map(function (relation) {
				return relation.id;
			}).indexOf(origRelation.id) < 0;
		}).map(function (origRelation) {
			return makeRelationArgs(origRelation, key, false, origRelation.relationId, origRelation.rev);
		});
	}).reduce(function (a, b) {
		return a.concat(b);
	}, []);

	// Combines saveNewEntity and deleteEntity instructions into promises
	var promises = newRelations
	// Map newRelations to promised invocations of saveNewEntity
	.map(function (args) {
		return new Promise(function (resolve, reject) {
			return _crud.saveNewEntity.apply(undefined, _toConsumableArray(args).concat([token, vreId, resolve, reject]));
		});
	})
	// Map readdRelations to promised invocations of updateEntity
	.concat(reAddRelations.map(function (args) {
		return new Promise(function (resolve, reject) {
			return _crud.updateEntity.apply(undefined, _toConsumableArray(args).concat([token, vreId, resolve, reject]));
		});
	}))
	// Map deleteRelations to promised invocations of updateEntity
	.concat(deleteRelations.map(function (args) {
		return new Promise(function (resolve, reject) {
			return _crud.updateEntity.apply(undefined, _toConsumableArray(args).concat([token, vreId, resolve, reject]));
		});
	}));

	// Invoke all CRUD operations for the relations
	Promise.all(promises).then(next, next);
};

exports["default"] = saveRelationsV21;
module.exports = exports["default"];

},{"../crud":25}],32:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

var _config = require("../config");

var _config2 = _interopRequireDefault(_config);

var _index = require("./index");

var _index2 = _interopRequireDefault(_index);

var listVres = function listVres() {
	return function (dispatch) {
		return _server2["default"].performXhr({
			method: "GET",
			headers: {
				"Accept": "application/json"
			},
			url: _config2["default"].apiUrl.v4 + "/system/vres"
		}, function (err, resp) {
			dispatch({ type: "LIST_VRES", list: JSON.parse(resp.body) });
		}, null, "List VREs");
	};
};

var setVre = function setVre(vreId) {
	return function (dispatch) {
		return _server2["default"].performXhr({
			method: "GET",
			headers: {
				"Accept": "application/json"
			},
			url: _config2["default"].apiUrl.v4 + "/metadata/" + vreId + "?withCollectionInfo=true"
		}, function (err, resp) {
			if (resp.statusCode === 200) {
				var body = JSON.parse(resp.body);
				dispatch({ type: "SET_VRE", vreId: vreId, collections: body });

				var defaultDomain = Object.keys(body)[0];
				_index2["default"].onNew(defaultDomain);
				_index2["default"].onSelectDomain(defaultDomain);
			}
		}, function () {
			return dispatch({ type: "SET_VRE", vreId: vreId, collections: {} });
		}, "Fetch VRE description for " + vreId);
	};
};

exports.listVres = listVres;
exports.setVre = setVre;

},{"../config":50,"./index":27,"./server":30}],33:[function(require,module,exports){
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

var CollectionTabs = (function (_React$Component) {
	_inherits(CollectionTabs, _React$Component);

	function CollectionTabs() {
		_classCallCheck(this, CollectionTabs);

		_get(Object.getPrototypeOf(CollectionTabs.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(CollectionTabs, [{
		key: "onDomainSelect",
		value: function onDomainSelect(domain) {
			this.props.onNew(domain);
			this.props.onSelectDomain(domain);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var collections = _props.collections;
			var activeDomain = _props.activeDomain;

			var domains = Object.keys(collections || {});

			return _react2["default"].createElement(
				"div",
				{ className: "container basic-margin" },
				_react2["default"].createElement(
					"ul",
					{ className: "nav nav-tabs" },
					domains.filter(function (d) {
						return !(collections[d].unknown || collections[d].relationCollection);
					}).map(function (domain) {
						return _react2["default"].createElement(
							"li",
							{ className: (0, _classnames2["default"])({ active: domain === activeDomain }), key: domain },
							_react2["default"].createElement(
								"a",
								{ onClick: domain === activeDomain ? function () {} : function () {
										return _this.onDomainSelect(domain);
									} },
								collections[domain].collectionLabel
							)
						);
					})
				)
			);
		}
	}]);

	return CollectionTabs;
})(_react2["default"].Component);

CollectionTabs.propTypes = {
	onNew: _react2["default"].PropTypes.func,
	onSelectDomain: _react2["default"].PropTypes.func,
	collections: _react2["default"].PropTypes.object,
	activeDomain: _react2["default"].PropTypes.string
};

exports["default"] = CollectionTabs;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}],34:[function(require,module,exports){
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

var _pageJsx = require("../page.jsx");

var _pageJsx2 = _interopRequireDefault(_pageJsx);

var _entityIndexPaginate = require("./entity-index/paginate");

var _entityIndexPaginate2 = _interopRequireDefault(_entityIndexPaginate);

var _entityIndexQuicksearch = require("./entity-index/quicksearch");

var _entityIndexQuicksearch2 = _interopRequireDefault(_entityIndexQuicksearch);

var _entityIndexList = require("./entity-index/list");

var _entityIndexList2 = _interopRequireDefault(_entityIndexList);

var _entityFormSaveFooter = require("./entity-form/save-footer");

var _entityFormSaveFooter2 = _interopRequireDefault(_entityFormSaveFooter);

var _entityFormForm = require("./entity-form/form");

var _entityFormForm2 = _interopRequireDefault(_entityFormForm);

var _collectionTabs = require("./collection-tabs");

var _collectionTabs2 = _interopRequireDefault(_collectionTabs);

var _messagesList = require("./messages/list");

var _messagesList2 = _interopRequireDefault(_messagesList);

var EditGui = (function (_React$Component) {
	_inherits(EditGui, _React$Component);

	function EditGui() {
		_classCallCheck(this, EditGui);

		_get(Object.getPrototypeOf(EditGui.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(EditGui, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var onSelect = _props.onSelect;
			var onNew = _props.onNew;
			var onSave = _props.onSave;
			var onDelete = _props.onDelete;
			var onSelectDomain = _props.onSelectDomain;
			var onDismissMessage = _props.onDismissMessage;
			var onChange = _props.onChange;
			var _props2 = this.props;
			var onQuickSearchQueryChange = _props2.onQuickSearchQueryChange;
			var onQuickSearch = _props2.onQuickSearch;
			var onPaginateLeft = _props2.onPaginateLeft;
			var onPaginateRight = _props2.onPaginateRight;
			var getAutocompleteValues = this.props.getAutocompleteValues;
			var _props3 = this.props;
			var quickSearch = _props3.quickSearch;
			var entity = _props3.entity;
			var vre = _props3.vre;
			var messages = _props3.messages;

			var currentMode = entity.domain && entity.data._id ? "edit" : "new";

			return _react2["default"].createElement(
				_pageJsx2["default"],
				null,
				_react2["default"].createElement(_collectionTabs2["default"], { collections: vre.collections, onNew: onNew, onSelectDomain: onSelectDomain,
					activeDomain: entity.domain }),
				_react2["default"].createElement(
					"div",
					{ className: "container" },
					_react2["default"].createElement(_messagesList2["default"], {
						types: ["SUCCESS_MESSAGE", "ERROR_MESSAGE"],
						messages: messages,
						onDismissMessage: onDismissMessage }),
					_react2["default"].createElement(
						"div",
						{ className: "row" },
						_react2["default"].createElement(
							"div",
							{ className: "col-sm-6 col-md-4" },
							_react2["default"].createElement(_entityIndexQuicksearch2["default"], {
								onQuickSearchQueryChange: onQuickSearchQueryChange,
								onQuickSearch: onQuickSearch,
								query: quickSearch.query }),
							_react2["default"].createElement(_entityIndexList2["default"], {
								start: quickSearch.start,
								list: quickSearch.list,
								onSelect: onSelect,
								domain: entity.domain })
						),
						entity.domain ? _react2["default"].createElement(_entityFormForm2["default"], { currentMode: currentMode, getAutocompleteValues: getAutocompleteValues,
							entity: entity, onNew: onNew, onDelete: onDelete, onChange: onChange,
							properties: vre.collections[entity.domain].properties,
							entityLabel: vre.collections[entity.domain].collectionLabel.replace(/s$/, "") }) : null
					)
				),
				_react2["default"].createElement(
					"div",
					{ type: "footer-body" },
					_react2["default"].createElement(
						"div",
						{ className: "col-sm-6 col-md-4" },
						_react2["default"].createElement(_entityIndexPaginate2["default"], {
							start: quickSearch.start,
							listLength: quickSearch.list.length,
							rows: 50,
							onPaginateLeft: onPaginateLeft,
							onPaginateRight: onPaginateRight })
					),
					_react2["default"].createElement(
						"div",
						{ className: "col-sm-6 col-md-8" },
						_react2["default"].createElement(_entityFormSaveFooter2["default"], { onSave: onSave, onCancel: function () {
								return currentMode === "edit" ? onSelect({ domain: entity.domain, id: entity.data._id }) : onNew(entity.domain);
							} })
					)
				),
				_react2["default"].createElement("div", { type: "footer-body" })
			);
		}
	}]);

	return EditGui;
})(_react2["default"].Component);

exports["default"] = EditGui;
module.exports = exports["default"];

},{"../page.jsx":49,"./collection-tabs":33,"./entity-form/form":40,"./entity-form/save-footer":41,"./entity-index/list":42,"./entity-index/paginate":43,"./entity-index/quicksearch":44,"./messages/list":45,"react":"react"}],35:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports["default"] = function (camelCase) {
  return camelCase.replace(/([A-Z0-9])/g, function (match) {
    return " " + match.toLowerCase();
  }).replace(/^./, function (match) {
    return match.toUpperCase();
  });
};

module.exports = exports["default"];

},{}],36:[function(require,module,exports){
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

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var _fieldsSelectField = require("../../../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var Field = (function (_React$Component) {
	_inherits(Field, _React$Component);

	function Field() {
		_classCallCheck(this, Field);

		_get(Object.getPrototypeOf(Field.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Field, [{
		key: "onChange",
		value: function onChange(values) {
			this.props.onChange([this.props.name], values.filter(function (val, idx, me) {
				return me.indexOf(val) === idx;
			}));
		}
	}, {
		key: "onAdd",
		value: function onAdd(value) {
			var _props = this.props;
			var name = _props.name;
			var entity = _props.entity;
			var onChange = _props.onChange;

			onChange([name], (entity.data[name] || []).concat(value));
		}
	}, {
		key: "onRemove",
		value: function onRemove(value) {
			var _props2 = this.props;
			var name = _props2.name;
			var entity = _props2.entity;
			var onChange = _props2.onChange;

			onChange([name], entity.data[name].filter(function (val) {
				return val !== value;
			}));
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props3 = this.props;
			var name = _props3.name;
			var entity = _props3.entity;
			var onChange = _props3.onChange;
			var options = _props3.options;

			var label = (0, _camel2label2["default"])(name);
			var values = entity.data[name] || [];
			var itemElements = values.map(function (value) {
				return _react2["default"].createElement(
					"div",
					{ key: value, className: "item-element" },
					_react2["default"].createElement(
						"strong",
						null,
						value
					),
					_react2["default"].createElement(
						"button",
						{ className: "btn btn-blank btn-xs pull-right",
							onClick: function () {
								return _this.onRemove(value);
							} },
						_react2["default"].createElement("span", { className: "glyphicon glyphicon-remove" })
					)
				);
			});

			return _react2["default"].createElement(
				"div",
				{ className: "basic-margin" },
				_react2["default"].createElement(
					"h4",
					null,
					label
				),
				itemElements,
				_react2["default"].createElement(
					_fieldsSelectField2["default"],
					{
						onChange: this.onAdd.bind(this),
						noClear: true, btnClass: "btn-default" },
					_react2["default"].createElement(
						"span",
						{ type: "placeholder" },
						"Select ",
						label.toLowerCase()
					),
					options.filter(function (opt) {
						return values.indexOf(opt) < 0;
					}).map(function (option) {
						return _react2["default"].createElement(
							"span",
							{ key: option, value: option },
							option
						);
					})
				)
			);
		}
	}]);

	return Field;
})(_react2["default"].Component);

Field.propTypes = {
	entity: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func,
	options: _react2["default"].PropTypes.array
};

exports["default"] = Field;
module.exports = exports["default"];

},{"../../../fields/select-field":46,"./camel2label":35,"react":"react"}],37:[function(require,module,exports){
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

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var RelationField = (function (_React$Component) {
  _inherits(RelationField, _React$Component);

  function RelationField(props) {
    _classCallCheck(this, RelationField);

    _get(Object.getPrototypeOf(RelationField.prototype), "constructor", this).call(this, props);

    this.state = {
      query: "",
      suggestions: []
    };
  }

  _createClass(RelationField, [{
    key: "onRemove",
    value: function onRemove(value) {
      var currentValues = this.props.entity.data["@relations"][this.props.name] || [];

      this.props.onChange(["@relations", this.props.name], currentValues.filter(function (curVal) {
        return curVal.id !== value.id;
      }));
    }
  }, {
    key: "onAdd",
    value: function onAdd(suggestion) {
      var currentValues = this.props.entity.data["@relations"][this.props.name] || [];
      if (currentValues.map(function (val) {
        return val.id;
      }).indexOf(suggestion.key) > -1) {
        return;
      }
      this.props.onChange(["@relations", this.props.name], currentValues.concat({
        id: suggestion.key,
        displayName: suggestion.value,
        accepted: true
      }));
    }
  }, {
    key: "onQueryChange",
    value: function onQueryChange(ev) {
      var _this = this;

      var _props = this.props;
      var getAutocompleteValues = _props.getAutocompleteValues;
      var path = _props.path;

      this.setState({ query: ev.target.value });
      getAutocompleteValues(path, ev.target.value, function (results) {
        _this.setState({ suggestions: results });
      });
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var _props2 = this.props;
      var name = _props2.name;
      var entity = _props2.entity;
      var onChange = _props2.onChange;

      var values = entity.data["@relations"][this.props.name] || [];
      var itemElements = values.filter(function (val) {
        return val.accepted;
      }).map(function (value, i) {
        return _react2["default"].createElement(
          "div",
          { key: i + "-" + value.id, className: "item-element" },
          _react2["default"].createElement(
            "strong",
            null,
            value.displayName
          ),
          _react2["default"].createElement(
            "button",
            { className: "btn btn-blank btn-xs pull-right",
              onClick: function () {
                return _this2.onRemove(value);
              } },
            _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove" })
          )
        );
      });

      return _react2["default"].createElement(
        "div",
        { className: "basic-margin" },
        _react2["default"].createElement(
          "h4",
          null,
          (0, _camel2label2["default"])(name)
        ),
        itemElements,
        _react2["default"].createElement("input", { className: "form-control", onChange: this.onQueryChange.bind(this),
          value: this.state.query, placeholder: "Search..." }),
        _react2["default"].createElement(
          "div",
          { style: { overflowY: "auto", maxHeight: "300px" } },
          this.state.suggestions.map(function (suggestion) {
            return _react2["default"].createElement(
              "a",
              { key: suggestion.key, className: "item-element",
                onClick: function () {
                  return _this2.onAdd(suggestion);
                } },
              suggestion.value
            );
          })
        )
      );
    }
  }]);

  return RelationField;
})(_react2["default"].Component);

exports["default"] = RelationField;
module.exports = exports["default"];

},{"./camel2label":35,"react":"react"}],38:[function(require,module,exports){
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

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var _fieldsSelectField = require("../../../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var Field = (function (_React$Component) {
  _inherits(Field, _React$Component);

  function Field() {
    _classCallCheck(this, Field);

    _get(Object.getPrototypeOf(Field.prototype), "constructor", this).apply(this, arguments);
  }

  _createClass(Field, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var name = _props.name;
      var entity = _props.entity;
      var onChange = _props.onChange;
      var options = _props.options;

      var label = (0, _camel2label2["default"])(name);
      var itemElement = entity.data[name] && entity.data[name].length > 0 ? _react2["default"].createElement(
        "div",
        { className: "item-element" },
        _react2["default"].createElement(
          "strong",
          null,
          entity.data[name]
        ),
        _react2["default"].createElement(
          "button",
          { className: "btn btn-blank btn-xs pull-right",
            onClick: function () {
              return onChange([name], "");
            } },
          _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove" })
        )
      ) : null;

      return _react2["default"].createElement(
        "div",
        { className: "basic-margin" },
        _react2["default"].createElement(
          "h4",
          null,
          label
        ),
        itemElement,
        _react2["default"].createElement(
          _fieldsSelectField2["default"],
          {
            onChange: function (value) {
              return onChange([name], value);
            },
            noClear: true, btnClass: "btn-default" },
          _react2["default"].createElement(
            "span",
            { type: "placeholder" },
            "Select ",
            label.toLowerCase()
          ),
          options.map(function (option) {
            return _react2["default"].createElement(
              "span",
              { key: option, value: option },
              option
            );
          })
        )
      );
    }
  }]);

  return Field;
})(_react2["default"].Component);

Field.propTypes = {
  entity: _react2["default"].PropTypes.object,
  name: _react2["default"].PropTypes.string,
  onChange: _react2["default"].PropTypes.func,
  options: _react2["default"].PropTypes.array
};

exports["default"] = Field;
module.exports = exports["default"];

},{"../../../fields/select-field":46,"./camel2label":35,"react":"react"}],39:[function(require,module,exports){
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

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var StringField = (function (_React$Component) {
	_inherits(StringField, _React$Component);

	function StringField() {
		_classCallCheck(this, StringField);

		_get(Object.getPrototypeOf(StringField.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(StringField, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var name = _props.name;
			var entity = _props.entity;
			var onChange = _props.onChange;

			var label = (0, _camel2label2["default"])(name);

			return _react2["default"].createElement(
				"div",
				{ className: "basic-margin" },
				_react2["default"].createElement(
					"h4",
					null,
					label
				),
				_react2["default"].createElement("input", { className: "form-control",
					onChange: function (ev) {
						return onChange([name], ev.target.value);
					},
					value: entity.data[name] || "",
					placeholder: "Enter " + label.toLowerCase()
				})
			);
		}
	}]);

	return StringField;
})(_react2["default"].Component);

StringField.propTypes = {
	entity: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func
};

exports["default"] = StringField;
module.exports = exports["default"];

},{"./camel2label":35,"react":"react"}],40:[function(require,module,exports){
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

var _fieldsStringField = require("./fields/string-field");

var _fieldsStringField2 = _interopRequireDefault(_fieldsStringField);

var _fieldsSelect = require("./fields/select");

var _fieldsSelect2 = _interopRequireDefault(_fieldsSelect);

var _fieldsMultiSelect = require("./fields/multi-select");

var _fieldsMultiSelect2 = _interopRequireDefault(_fieldsMultiSelect);

var _fieldsRelation = require("./fields/relation");

var _fieldsRelation2 = _interopRequireDefault(_fieldsRelation);

var fieldMap = {
  "string": function string(fieldDef, props) {
    return _react2["default"].createElement(_fieldsStringField2["default"], _extends({}, props, { name: fieldDef.name }));
  },
  "text": function text(fieldDef, props) {
    return _react2["default"].createElement(_fieldsStringField2["default"], _extends({}, props, { name: fieldDef.name }));
  },
  "datable": function datable(fieldDef, props) {
    return _react2["default"].createElement(_fieldsStringField2["default"], _extends({}, props, { name: fieldDef.name }));
  },
  "multiselect": function multiselect(fieldDef, props) {
    return _react2["default"].createElement(_fieldsMultiSelect2["default"], _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
  },
  "select": function select(fieldDef, props) {
    return _react2["default"].createElement(_fieldsSelect2["default"], _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
  },
  "relation": function relation(fieldDef, props) {
    return _react2["default"].createElement(_fieldsRelation2["default"], _extends({}, props, { name: fieldDef.name, path: fieldDef.quicksearch }));
  }
};

var EntityForm = (function (_React$Component) {
  _inherits(EntityForm, _React$Component);

  function EntityForm() {
    _classCallCheck(this, EntityForm);

    _get(Object.getPrototypeOf(EntityForm.prototype), "constructor", this).apply(this, arguments);
  }

  _createClass(EntityForm, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var onNew = _props.onNew;
      var onDelete = _props.onDelete;
      var onChange = _props.onChange;
      var getAutocompleteValues = _props.getAutocompleteValues;
      var _props2 = this.props;
      var entity = _props2.entity;
      var currentMode = _props2.currentMode;
      var properties = _props2.properties;
      var entityLabel = _props2.entityLabel;

      return _react2["default"].createElement(
        "div",
        { className: "col-sm-6 col-md-8" },
        _react2["default"].createElement(
          "div",
          { className: "basic-margin" },
          _react2["default"].createElement(
            "button",
            { className: "btn btn-primary pull-right", onClick: function () {
                return onNew(entity.domain);
              } },
            "New ",
            entityLabel
          )
        ),
        properties.filter(function (fieldDef) {
          return !fieldMap.hasOwnProperty(fieldDef.type);
        }).map(function (fieldDef, i) {
          return _react2["default"].createElement(
            "div",
            { key: i, style: { "color": "red" } },
            _react2["default"].createElement(
              "strong",
              null,
              "Field type not supported ",
              fieldDef.type
            )
          );
        }),
        properties.filter(function (fieldDef) {
          return fieldMap.hasOwnProperty(fieldDef.type);
        }).map(function (fieldDef, i) {
          return fieldMap[fieldDef.type](fieldDef, {
            key: i + "-" + fieldDef.name,
            entity: entity,
            onChange: onChange,
            getAutocompleteValues: getAutocompleteValues
          });
        }),
        currentMode === "edit" ? _react2["default"].createElement(
          "div",
          { className: "basic-margin" },
          _react2["default"].createElement(
            "h4",
            null,
            "Delete"
          ),
          _react2["default"].createElement(
            "buton",
            { className: "btn btn-danger", onClick: onDelete },
            "Delete ",
            entityLabel
          )
        ) : null
      );
    }
  }]);

  return EntityForm;
})(_react2["default"].Component);

exports["default"] = EntityForm;
module.exports = exports["default"];

},{"./fields/multi-select":36,"./fields/relation":37,"./fields/select":38,"./fields/string-field":39,"react":"react"}],41:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

exports["default"] = function (props) {
  var onSave = props.onSave;
  var onCancel = props.onCancel;

  return _react2["default"].createElement(
    "div",
    null,
    _react2["default"].createElement(
      "button",
      { className: "btn btn-primary", onClick: onSave },
      "Save"
    ),
    " ",
    "or",
    " ",
    _react2["default"].createElement(
      "button",
      { className: "btn btn-link", onClick: onCancel },
      "Cancel"
    )
  );
};

module.exports = exports["default"];

},{"react":"react"}],42:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

exports["default"] = function (props) {
  var start = props.start;
  var list = props.list;
  var onSelect = props.onSelect;
  var domain = props.domain;

  return _react2["default"].createElement(
    "div",
    { className: "result-list result-list-edit" },
    _react2["default"].createElement(
      "ol",
      { start: start + 1, style: { counterReset: "step-counter " + start } },
      list.map(function (entry, i) {
        return _react2["default"].createElement(
          "li",
          { key: entry._id },
          _react2["default"].createElement(
            "a",
            { onClick: function () {
                return onSelect({ domain: domain, id: entry._id });
              } },
            entry["@displayName"]
          )
        );
      })
    )
  );
};

module.exports = exports["default"];

},{"react":"react"}],43:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

exports["default"] = function (props) {
  var onPaginateLeft = props.onPaginateLeft;
  var onPaginateRight = props.onPaginateRight;
  var start = props.start;
  var rows = props.rows;
  var listLength = props.listLength;

  return _react2["default"].createElement(
    "div",
    null,
    _react2["default"].createElement(
      "button",
      { className: "btn btn-default", disabled: start === 0, onClick: onPaginateLeft },
      _react2["default"].createElement("span", { className: "glyphicon glyphicon-chevron-left" })
    ),
    " ",
    start + 1,
    " - ",
    start + rows,
    " ",
    _react2["default"].createElement(
      "button",
      { className: "btn btn-default", disabled: listLength < rows, onClick: onPaginateRight },
      _react2["default"].createElement("span", { className: "glyphicon glyphicon-chevron-right" })
    )
  );
};

module.exports = exports["default"];

},{"react":"react"}],44:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

exports["default"] = function (props) {
  var onQuickSearchQueryChange = props.onQuickSearchQueryChange;
  var onQuickSearch = props.onQuickSearch;
  var query = props.query;

  return _react2["default"].createElement(
    "div",
    { className: "input-group small-margin " },
    _react2["default"].createElement("input", { type: "text", placeholder: "Search for...", className: "form-control",
      onChange: function (ev) {
        return onQuickSearchQueryChange(ev.target.value);
      },
      onKeyPress: function (ev) {
        return ev.key === "Enter" ? onQuickSearch() : false;
      },
      value: query
    }),
    _react2["default"].createElement(
      "span",
      { className: "input-group-btn" },
      _react2["default"].createElement(
        "button",
        { className: "btn btn-default", onClick: onQuickSearch },
        _react2["default"].createElement("span", { className: "glyphicon glyphicon-search" })
      ),
      _react2["default"].createElement(
        "button",
        { className: "btn btn-blank", onClick: function () {
            onQuickSearchQueryChange("");onQuickSearch();
          } },
        _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove" })
      )
    )
  );
};

module.exports = exports["default"];

},{"react":"react"}],45:[function(require,module,exports){
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

var _message = require("../../message");

var _message2 = _interopRequireDefault(_message);

var LABELS = {
	"SUCCESS_MESSAGE": "",
	"ERROR_MESSAGE": _react2["default"].createElement(
		"span",
		null,
		_react2["default"].createElement("span", { className: "glyphicon glyphicon-exclamation-sign" }),
		" Warning!"
	)
};

var ALERT_LEVELS = {
	"SUCCESS_MESSAGE": "info",
	"ERROR_MESSAGE": "danger"
};

var Messages = (function (_React$Component) {
	_inherits(Messages, _React$Component);

	function Messages() {
		_classCallCheck(this, Messages);

		_get(Object.getPrototypeOf(Messages.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Messages, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var messages = _props.messages;
			var types = _props.types;
			var onDismissMessage = _props.onDismissMessage;

			var filteredMessages = messages.log.map(function (msg, idx) {
				return { message: msg.message, index: idx, type: msg.type, dismissed: msg.dismissed };
			}).filter(function (msg) {
				return types.indexOf(msg.type) > -1 && !msg.dismissed;
			});

			return _react2["default"].createElement(
				"div",
				null,
				filteredMessages.map(function (msg) {
					return _react2["default"].createElement(
						_message2["default"],
						{ key: msg.index,
							dismissible: true,
							alertLevel: ALERT_LEVELS[msg.type],
							onCloseMessage: function () {
								return onDismissMessage(msg.index);
							} },
						_react2["default"].createElement(
							"strong",
							null,
							LABELS[msg.type]
						),
						" ",
						_react2["default"].createElement(
							"span",
							null,
							msg.message
						)
					);
				})
			);
		}
	}]);

	return Messages;
})(_react2["default"].Component);

Messages.propTypes = {
	messages: _react2["default"].PropTypes.object,
	onDismissMessage: _react2["default"].PropTypes.func.isRequired,
	types: _react2["default"].PropTypes.array.isRequired
};

exports["default"] = Messages;
module.exports = exports["default"];

},{"../../message":48,"classnames":"classnames","react":"react"}],46:[function(require,module,exports){
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
      var onChange = _props.onChange;
      var onClear = _props.onClear;
      var value = _props.value;
      var btnClass = _props.btnClass;
      var noClear = _props.noClear;

      var selectedOption = _react2["default"].Children.toArray(this.props.children).filter(function (opt) {
        return opt.props.value === value;
      });
      var placeholder = _react2["default"].Children.toArray(this.props.children).filter(function (opt) {
        return opt.props.type === "placeholder";
      });
      var otherOptions = _react2["default"].Children.toArray(this.props.children).filter(function (opt) {
        return opt.props.value && opt.props.value !== value;
      });

      return _react2["default"].createElement(
        "div",
        { className: (0, _classnames2["default"])("dropdown", { open: this.state.isOpen }) },
        _react2["default"].createElement(
          "button",
          { className: (0, _classnames2["default"])("btn", "dropdown-toggle", btnClass || "btn-blank"), onClick: this.toggleSelect.bind(this) },
          selectedOption.length ? selectedOption : placeholder,
          " ",
          _react2["default"].createElement("span", { className: "caret" })
        ),
        _react2["default"].createElement(
          "ul",
          { className: "dropdown-menu" },
          value && !noClear ? _react2["default"].createElement(
            "li",
            null,
            _react2["default"].createElement(
              "a",
              { onClick: function () {
                  onClear();_this.toggleSelect();
                } },
              "- clear -"
            )
          ) : null,
          otherOptions.map(function (option, i) {
            return _react2["default"].createElement(
              "li",
              { key: i },
              _react2["default"].createElement(
                "a",
                { style: { cursor: "pointer" }, onClick: function () {
                    onChange(option.props.value);_this.toggleSelect();
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
})(_react2["default"].Component);

SelectField.propTypes = {
  onChange: _react2["default"].PropTypes.func,
  onClear: _react2["default"].PropTypes.func,
  value: _react2["default"].PropTypes.any,
  btnClass: _react2["default"].PropTypes.string,
  noClear: _react2["default"].PropTypes.bool
};

exports["default"] = SelectField;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react","react-dom":"react-dom"}],47:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function Footer(props) {
  var hiLogo = _react2["default"].createElement(
    "div",
    { className: "col-sm-1 col-md-1" },
    _react2["default"].createElement("img", { className: "hi-logo", src: "images/logo-huygens-ing.svg" })
  );

  var clariahLogo = _react2["default"].createElement(
    "div",
    { className: "col-sm-1 col-md-1" },
    _react2["default"].createElement("img", { className: "logo", src: "images/logo-clariah.svg" })
  );

  var footerBody = _react2["default"].Children.count(props.children) > 0 ? _react2["default"].Children.map(props.children, function (child, i) {
    return _react2["default"].createElement(
      "div",
      { className: "white-bar" },
      _react2["default"].createElement(
        "div",
        { className: "container" },
        i === _react2["default"].Children.count(props.children) - 1 ? _react2["default"].createElement(
          "div",
          { className: "row" },
          hiLogo,
          _react2["default"].createElement(
            "div",
            { className: "col-sm-10 col-md-10 text-center" },
            child
          ),
          clariahLogo
        ) : _react2["default"].createElement(
          "div",
          { className: "row" },
          child
        )
      )
    );
  }) : _react2["default"].createElement(
    "div",
    { className: "white-bar" },
    _react2["default"].createElement(
      "div",
      { className: "container" },
      _react2["default"].createElement(
        "div",
        { className: "row" },
        hiLogo,
        _react2["default"].createElement("div", { className: "col-sm-10 col-md-10 text-center" }),
        clariahLogo
      )
    )
  );

  return _react2["default"].createElement(
    "footer",
    { className: "footer" },
    footerBody
  );
}

exports["default"] = Footer;
module.exports = exports["default"];

},{"react":"react"}],48:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

exports["default"] = function (props) {
  var dismissible = props.dismissible;
  var alertLevel = props.alertLevel;
  var onCloseMessage = props.onCloseMessage;

  var dismissButton = dismissible ? _react2["default"].createElement(
    "button",
    { type: "button", className: "close", onClick: onCloseMessage },
    _react2["default"].createElement(
      "span",
      null,
      "×"
    )
  ) : null;

  return _react2["default"].createElement(
    "div",
    { className: (0, _classnames2["default"])("alert", "alert-" + alertLevel, { "alert-dismissible": dismissible }), role: "alert" },
    dismissButton,
    props.children
  );
};

;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}],49:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _footer = require("./footer");

var _footer2 = _interopRequireDefault(_footer);

var FOOTER_HEIGHT = 81;

function Page(props) {
  var footers = _react2["default"].Children.toArray(props.children).filter(function (child) {
    return child.props.type === "footer-body";
  });

  return _react2["default"].createElement(
    "div",
    { className: "page" },
    _react2["default"].createElement(
      "div",
      { className: "basic-margin hi-Green container-fluid" },
      _react2["default"].createElement(
        "nav",
        { className: "navbar " },
        _react2["default"].createElement(
          "div",
          { className: "container" },
          _react2["default"].createElement(
            "div",
            { className: "navbar-header" },
            " ",
            _react2["default"].createElement(
              "a",
              { className: "navbar-brand", href: "#" },
              _react2["default"].createElement("img", { src: "images/logo-timbuctoo.svg", className: "logo", alt: "timbuctoo" })
            ),
            " "
          ),
          _react2["default"].createElement(
            "div",
            { id: "navbar", className: "navbar-collapse collapse" },
            _react2["default"].createElement(
              "ul",
              { className: "nav navbar-nav navbar-right" },
              props.username ? _react2["default"].createElement(
                "li",
                null,
                _react2["default"].createElement(
                  "a",
                  { href: props.userlocation || '#' },
                  _react2["default"].createElement("span", { className: "glyphicon glyphicon-user" }),
                  " ",
                  props.username
                )
              ) : null
            )
          )
        )
      )
    ),
    _react2["default"].createElement(
      "div",
      { style: { marginBottom: FOOTER_HEIGHT * footers.length + "px" } },
      _react2["default"].Children.toArray(props.children).filter(function (child) {
        return child.props.type !== "footer-body";
      })
    ),
    _react2["default"].createElement(
      _footer2["default"],
      null,
      footers
    )
  );
}

exports["default"] = Page;
module.exports = exports["default"];

},{"./footer":47,"react":"react"}],50:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
console.log("");
exports["default"] = {
	apiUrl: {
		"v2.1": "" + "/v2.1",
		"v4": "" + "/v2.1"
	},
	apiVersion: "v4"
};
module.exports = exports["default"];

},{}],51:[function(require,module,exports){
"use strict";

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; })();

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var _actionsVre = require("./actions/vre");

var _componentsEditGuiEditGui = require("./components/edit-gui/edit-gui");

var _componentsEditGuiEditGui2 = _interopRequireDefault(_componentsEditGuiEditGui);

var _actionsAutocomplete = require("./actions/autocomplete");

var _actionsAutocomplete2 = _interopRequireDefault(_actionsAutocomplete);

document.addEventListener("DOMContentLoaded", function () {

	_store2["default"].subscribe(function () {
		return _reactDom2["default"].render(_react2["default"].createElement(_componentsEditGuiEditGui2["default"], _extends({}, _store2["default"].getState(), _actions2["default"], { getAutocompleteValues: _actionsAutocomplete2["default"] })), document.getElementById("app"));
	});

	function getVreId() {
		var path = window.location.search.substr(1);
		var params = path.split("&");

		for (var i in params) {
			var _params$i$split = params[i].split("=");

			var _params$i$split2 = _slicedToArray(_params$i$split, 2);

			var key = _params$i$split2[0];
			var value = _params$i$split2[1];

			if (key === "vreId") {
				return value;
			}
		}
		return "WomenWriters";
	}

	function getLogin() {
		var path = window.location.search.substr(1);
		var params = path.split("&");

		for (var i in params) {
			var _params$i$split3 = params[i].split("=");

			var _params$i$split32 = _slicedToArray(_params$i$split3, 2);

			var key = _params$i$split32[0];
			var value = _params$i$split32[1];

			if (key === "hsid") {
				return { user: value, token: value };
			}
		}
		return undefined;
	}
	_store2["default"].dispatch((0, _actionsVre.setVre)(getVreId()));
	_actions2["default"].onLoginChange(getLogin());
});

},{"./actions":27,"./actions/autocomplete":24,"./actions/vre":32,"./components/edit-gui/edit-gui":34,"./store":58,"react":"react","react-dom":"react-dom"}],52:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _utilSetIn = require("../util/set-in");

var _utilSetIn2 = _interopRequireDefault(_utilSetIn);

var initialState = {
	data: null,
	domain: null,
	errorMessage: null
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "RECEIVE_ENTITY":
			return _extends({}, state, {
				data: action.data,
				domain: action.domain,
				errorMessage: action.errorMessage || null
			});

		case "SET_ENTITY_FIELD_VALUE":
			return _extends({}, state, {
				data: (0, _utilSetIn2["default"])(action.fieldPath, action.value, state.data)
			});

		case "RECEIVE_ENTITY_FAILURE":
			return _extends({}, state, {
				data: null,
				errorMessage: action.errorMessage
			});

		case "SET_VRE":
			{
				return initialState;
			}

	}

	return state;
};

module.exports = exports["default"];

},{"../util/set-in":60}],53:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _entity = require("./entity");

var _entity2 = _interopRequireDefault(_entity);

var _messages = require("./messages");

var _messages2 = _interopRequireDefault(_messages);

var _user = require("./user");

var _user2 = _interopRequireDefault(_user);

var _vre = require("./vre");

var _vre2 = _interopRequireDefault(_vre);

var _quickSearch = require("./quick-search");

var _quickSearch2 = _interopRequireDefault(_quickSearch);

exports["default"] = {
	vre: _vre2["default"],
	entity: _entity2["default"],
	user: _user2["default"],
	messages: _messages2["default"],
	quickSearch: _quickSearch2["default"]
};
module.exports = exports["default"];

},{"./entity":52,"./messages":54,"./quick-search":55,"./user":56,"./vre":57}],54:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _utilSetIn = require("../util/set-in");

var _utilSetIn2 = _interopRequireDefault(_utilSetIn);

var initialState = {
	log: []
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "REQUEST_MESSAGE":
			state.log.push({ message: action.message, type: action.type, time: new Date() });
			return state;
		case "SUCCESS_MESSAGE":
			state.log.push({ message: action.message, type: action.type, time: new Date() });
			return state;
		case "ERROR_MESSAGE":
			state.log.push({ message: action.message, type: action.type, time: new Date() });
			return state;
		case "DISMISS_MESSAGE":
			return _extends({}, state, {
				log: (0, _utilSetIn2["default"])([action.messageIndex, "dismissed"], true, state.log)
			});
	}

	return state;
};

module.exports = exports["default"];

},{"../util/set-in":60}],55:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	start: 0,
	list: [],
	rows: 50,
	query: ""
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_PAGINATION_START":
			return _extends({}, state, { start: action.start });
		case "RECEIVE_ENTITY_LIST":
			return _extends({}, state, {
				list: action.data
			});
		case "SET_QUICKSEARCH_QUERY":
			{
				return _extends({}, state, {
					query: action.value
				});
			}
		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],56:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
var initialState = null;

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_USER":
			if (action.user) {
				return action.user;
			} else {
				return state;
			}
			break;
		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],57:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	vreId: null,
	list: [],
	collections: {},
	domain: null
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_VRE":
			return _extends({}, state, {
				vreId: action.vreId,
				collections: action.collections || null,
				list: action.list || state.list
			});

		case "LIST_VRES":
			return _extends({}, state, {
				list: action.list,
				collections: null
			});
		case "SET_DOMAIN":
			return _extends({}, state, {
				domain: action.domain
			});

		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],58:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _redux = require("redux");

var _reducers = require("../reducers");

var _reducers2 = _interopRequireDefault(_reducers);

var _reduxThunk = require("redux-thunk");

var _reduxThunk2 = _interopRequireDefault(_reduxThunk);

exports["default"] = (0, _redux.createStore)((0, _redux.combineReducers)(_reducers2["default"]), (0, _redux.compose)((0, _redux.applyMiddleware)(_reduxThunk2["default"]), window.devToolsExtension ? window.devToolsExtension() : function (f) {
  return f;
}));
module.exports = exports["default"];

},{"../reducers":53,"redux":16,"redux-thunk":10}],59:[function(require,module,exports){
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

},{}],60:[function(require,module,exports){
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

},{"./clone-deep":59}]},{},[51])(51)
});