(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.TimbuctooSearch = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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

},{"is-function":3}],2:[function(require,module,exports){
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
},{}],3:[function(require,module,exports){
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

},{}],4:[function(require,module,exports){
var overArg = require('./_overArg');

/** Built-in value references. */
var getPrototype = overArg(Object.getPrototypeOf, Object);

module.exports = getPrototype;

},{"./_overArg":6}],5:[function(require,module,exports){
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

},{}],6:[function(require,module,exports){
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

},{"./_getPrototype":4,"./_isHostObject":5,"./isObjectLike":7}],9:[function(require,module,exports){
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
},{"for-each":1,"trim":22}],10:[function(require,module,exports){
'use strict';

exports.__esModule = true;
function createThunkMiddleware(extraArgument) {
  return function (_ref) {
    var dispatch = _ref.dispatch;
    var getState = _ref.getState;
    return function (next) {
      return function (action) {
        if (typeof action === 'function') {
          return action(dispatch, getState, extraArgument);
        }

        return next(action);
      };
    };
  };
}

var thunk = createThunkMiddleware();
thunk.withExtraArgument = createThunkMiddleware;

exports['default'] = thunk;
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
},{"./createStore":15,"./utils/warning":17,"_process":23,"lodash/isPlainObject":8}],14:[function(require,module,exports){
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
},{"lodash/isPlainObject":8,"symbol-observable":19}],16:[function(require,module,exports){
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
},{"./applyMiddleware":11,"./bindActionCreators":12,"./combineReducers":13,"./compose":14,"./createStore":15,"./utils/warning":17,"_process":23}],17:[function(require,module,exports){
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
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.SolrFacetedSearch = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
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

},{}],2:[function(_dereq_,module,exports){
var isFunction = _dereq_('is-function')

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

},{"is-function":4}],3:[function(_dereq_,module,exports){
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
},{}],4:[function(_dereq_,module,exports){
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

},{}],5:[function(_dereq_,module,exports){
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

},{}],6:[function(_dereq_,module,exports){
var trim = _dereq_('trim')
  , forEach = _dereq_('for-each')
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
},{"for-each":2,"trim":7}],7:[function(_dereq_,module,exports){

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

},{}],8:[function(_dereq_,module,exports){
"use strict";
var window = _dereq_("global/window")
var once = _dereq_("once")
var isFunction = _dereq_("is-function")
var parseHeaders = _dereq_("parse-headers")
var xtend = _dereq_("xtend")

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

},{"global/window":3,"is-function":4,"once":5,"parse-headers":6,"xtend":9}],9:[function(_dereq_,module,exports){
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

},{}],10:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _xhr = _dereq_("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _solrQuery = _dereq_("./solr-query");

var _solrQuery2 = _interopRequireDefault(_solrQuery);

var MAX_INT = 2147483647;

var server = {};

server.performXhr = function (options, accept) {
	var reject = arguments.length <= 2 || arguments[2] === undefined ? function () {
		console.warn("Undefined reject callback! ");(console.trace || function () {})();
	} : arguments[2];

	(0, _xhr2["default"])(options, accept, reject);
};

server.submitQuery = function (query, callback) {
	callback({ type: "SET_RESULTS_PENDING" });

	server.performXhr({
		url: query.url,
		data: (0, _solrQuery2["default"])(query),
		method: "POST",
		headers: {
			"Content-type": "application/x-www-form-urlencoded"
		}
	}, function (err, resp) {
		if (resp.statusCode >= 200 && resp.statusCode < 300) {
			callback({ type: "SET_RESULTS", data: JSON.parse(resp.body) });
		} else {
			console.log("Server error: ", resp.statusCode);
		}
	});
};

server.fetchCsv = function (query, callback) {
	server.performXhr({
		url: query.url,
		data: (0, _solrQuery2["default"])(_extends({}, query, { rows: MAX_INT }), {
			wt: "csv",
			"csv.mv.separator": "|",
			"csv.separator": ";"
		}),
		method: "POST",
		headers: {
			"Content-type": "application/x-www-form-urlencoded"
		}
	}, function (err, resp) {
		if (resp.statusCode >= 200 && resp.statusCode < 300) {
			callback(resp.body);
		} else {
			console.log("Server error: ", resp.statusCode);
		}
	});
};

exports["default"] = server;
module.exports = exports["default"];

},{"./solr-query":12,"xhr":8}],11:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var _reducersQuery = _dereq_("../reducers/query");

var _reducersQuery2 = _interopRequireDefault(_reducersQuery);

var _reducersResults = _dereq_("../reducers/results");

var _reducersResults2 = _interopRequireDefault(_reducersResults);

var _server = _dereq_("./server");

var SolrClient = (function () {
	function SolrClient(settings) {
		_classCallCheck(this, SolrClient);

		var onChange = settings.onChange;

		this.onChange = onChange;
		delete settings.onChange;

		this.state = {
			query: settings,
			results: {
				facets: [],
				docs: [],
				numFound: 0
			}
		};
		this.settings = _extends({}, settings);

		if (!this.state.query.pageStrategy) {
			this.state.query.pageStrategy = "paginate";
		}
		if (!this.state.query.rows) {
			this.state.query.rows = 20;
		}

		if (this.state.query.pageStrategy === "cursor" && !this.state.query.idField) {
			throw new Error("Pagination strategy 'cursor' requires a unique 'idField' to be passed.");
		}
	}

	_createClass(SolrClient, [{
		key: "setInitialQuery",
		value: function setInitialQuery(queryToMerge) {

			var searchFieldsToMerge = queryToMerge.searchFields || [];
			var sortFieldsToMerge = queryToMerge.sortFields || [];

			this.state.query.searchFields = this.state.query.searchFields.map(function (sf) {
				return searchFieldsToMerge.map(function (sfm) {
					return sfm.field;
				}).indexOf(sf.field) > -1 ? _extends({}, sf, { value: searchFieldsToMerge.find(function (sfm) {
						return sfm.field === sf.field;
					}).value }) : sf;
			});

			this.state.query.sortFields = this.state.query.sortFields.map(function (sf) {
				return sortFieldsToMerge.map(function (sfm) {
					return sfm.field;
				}).indexOf(sf.field) > -1 ? _extends({}, sf, { value: sortFieldsToMerge.find(function (sfm) {
						return sfm.field === sf.field;
					}).value }) : sf;
			});
		}
	}, {
		key: "initialize",
		value: function initialize() {
			var query = this.state.query;
			var pageStrategy = query.pageStrategy;

			var payload = _extends({ type: "SET_QUERY_FIELDS"
			}, query, { start: pageStrategy === "paginate" ? 0 : null
			});

			this.sendQuery((0, _reducersQuery2["default"])(this.state.query, payload));

			return this;
		}
	}, {
		key: "resetSearchFields",
		value: function resetSearchFields() {
			var query = this.state.query;
			var pageStrategy = query.pageStrategy;

			var payload = _extends({ type: "SET_QUERY_FIELDS"
			}, this.settings, { start: pageStrategy === "paginate" ? 0 : null
			});
			this.sendQuery((0, _reducersQuery2["default"])(this.state.query, payload));
		}
	}, {
		key: "sendQuery",
		value: function sendQuery() {
			var _this = this;

			var query = arguments.length <= 0 || arguments[0] === undefined ? this.state.query : arguments[0];

			delete query.cursorMark;
			this.state.query = query;
			(0, _server.submitQuery)(query, function (action) {
				_this.state.results = (0, _reducersResults2["default"])(_this.state.results, action);
				_this.state.query = (0, _reducersQuery2["default"])(_this.state.query, action);
				_this.onChange(_this.state, _this.getHandlers());
			});
		}
	}, {
		key: "sendNextCursorQuery",
		value: function sendNextCursorQuery() {
			var _this2 = this;

			(0, _server.submitQuery)(this.state.query, function (action) {
				_this2.state.results = (0, _reducersResults2["default"])(_this2.state.results, _extends({}, action, {
					type: action.type === "SET_RESULTS" ? "SET_NEXT_RESULTS" : action.type
				}));
				_this2.state.query = (0, _reducersQuery2["default"])(_this2.state.query, action);
				_this2.onChange(_this2.state, _this2.getHandlers());
			});
		}
	}, {
		key: "fetchCsv",
		value: function fetchCsv() {
			(0, _server.fetchCsv)(this.state.query, function (data) {
				var element = document.createElement("a");
				element.setAttribute("href", "data:application/csv;charset=utf-8," + encodeURIComponent(data));
				element.setAttribute("download", "export.csv");

				element.style.display = "none";
				document.body.appendChild(element);

				element.click();

				document.body.removeChild(element);
			});
		}
	}, {
		key: "setCurrentPage",
		value: function setCurrentPage(page) {
			var query = this.state.query;
			var rows = query.rows;

			var payload = { type: "SET_START", newStart: page * rows };

			this.sendQuery((0, _reducersQuery2["default"])(this.state.query, payload));
		}
	}, {
		key: "setSearchFieldValue",
		value: function setSearchFieldValue(field, value) {
			var query = this.state.query;
			var searchFields = query.searchFields;

			var newFields = searchFields.map(function (searchField) {
				return searchField.field === field ? _extends({}, searchField, { value: value }) : searchField;
			});

			var payload = { type: "SET_SEARCH_FIELDS", newFields: newFields };

			this.sendQuery((0, _reducersQuery2["default"])(this.state.query, payload));
		}
	}, {
		key: "setFacetSort",
		value: function setFacetSort(field, value) {
			var query = this.state.query;
			var searchFields = query.searchFields;

			var newFields = searchFields.map(function (searchField) {
				return searchField.field === field ? _extends({}, searchField, { facetSort: value }) : searchField;
			});

			var payload = { type: "SET_SEARCH_FIELDS", newFields: newFields };

			this.sendQuery((0, _reducersQuery2["default"])(this.state.query, payload));
		}
	}, {
		key: "setSortFieldValue",
		value: function setSortFieldValue(field, value) {
			var query = this.state.query;
			var sortFields = query.sortFields;

			var newSortFields = sortFields.map(function (sortField) {
				return sortField.field === field ? _extends({}, sortField, { value: value }) : _extends({}, sortField, { value: null });
			});

			var payload = { type: "SET_SORT_FIELDS", newSortFields: newSortFields };
			this.sendQuery((0, _reducersQuery2["default"])(this.state.query, payload));
		}
	}, {
		key: "setFilters",
		value: function setFilters(filters) {
			var payload = { type: "SET_FILTERS", newFilters: filters };
			this.sendQuery((0, _reducersQuery2["default"])(this.state.query, payload));
		}
	}, {
		key: "setCollapse",
		value: function setCollapse(field, value) {
			var query = this.state.query;
			var searchFields = query.searchFields;

			var newFields = searchFields.map(function (searchField) {
				return searchField.field === field ? _extends({}, searchField, { collapse: value }) : searchField;
			});
			var payload = { type: "SET_SEARCH_FIELDS", newFields: newFields };
			this.state.query = (0, _reducersQuery2["default"])(this.state.query, payload);
			this.onChange(this.state, this.getHandlers());
		}
	}, {
		key: "getHandlers",
		value: function getHandlers() {
			return {
				onSortFieldChange: this.setSortFieldValue.bind(this),
				onSearchFieldChange: this.setSearchFieldValue.bind(this),
				onFacetSortChange: this.setFacetSort.bind(this),
				onPageChange: this.setCurrentPage.bind(this),
				onNextCursorQuery: this.sendNextCursorQuery.bind(this),
				onSetCollapse: this.setCollapse.bind(this),
				onNewSearch: this.resetSearchFields.bind(this),
				onCsvExport: this.fetchCsv.bind(this)
			};
		}
	}]);

	return SolrClient;
})();

exports.SolrClient = SolrClient;

},{"../reducers/query":35,"../reducers/results":36,"./server":10}],12:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var rangeFacetToQueryFilter = function rangeFacetToQueryFilter(field) {
	var filters = field.value || [];
	if (filters.length < 2) {
		return null;
	}

	return encodeURIComponent(field.field + ":[" + filters[0] + " TO " + filters[1] + "]");
};

var listFacetFieldToQueryFilter = function listFacetFieldToQueryFilter(field) {
	var filters = field.value || [];
	if (filters.length === 0) {
		return null;
	}

	var filterQ = filters.map(function (f) {
		return "\"" + f + "\"";
	}).join(" OR ");
	return encodeURIComponent(field.field + ":(" + filterQ + ")");
};

var textFieldToQueryFilter = function textFieldToQueryFilter(field) {
	if (!field.value || field.value.length === 0) {
		return null;
	}

	return encodeURIComponent(field.field === "*" ? field.value : field.field + ":" + field.value);
};

var fieldToQueryFilter = function fieldToQueryFilter(field) {
	if (field.type === "text") {
		return textFieldToQueryFilter(field);
	} else if (field.type === "list-facet") {
		return listFacetFieldToQueryFilter(field);
	} else if (field.type.indexOf("range") > -1) {
		return rangeFacetToQueryFilter(field);
	}
	return null;
};

var buildQuery = function buildQuery(fields) {
	return fields.map(fieldToQueryFilter).filter(function (queryFilter) {
		return queryFilter !== null;
	}).map(function (queryFilter) {
		return "fq=" + queryFilter;
	}).join("&");
};

var facetFields = function facetFields(fields) {
	return fields.filter(function (field) {
		return field.type === "list-facet" || field.type === "range-facet";
	}).map(function (field) {
		return "facet.field=" + encodeURIComponent(field.field);
	}).join("&");
};

var facetSorts = function facetSorts(fields) {
	return fields.filter(function (field) {
		return field.facetSort;
	}).map(function (field) {
		return "f." + encodeURIComponent(field.field) + ".facet.sort=" + field.facetSort;
	}).join("&");
};

var buildSort = function buildSort(sortFields) {
	return sortFields.filter(function (sortField) {
		return sortField.value;
	}).map(function (sortField) {
		return encodeURIComponent(sortField.field + " " + sortField.value);
	}).join(",");
};

var buildFormat = function buildFormat(format) {
	return Object.keys(format).map(function (key) {
		return key + "=" + encodeURIComponent(format[key]);
	}).join("&");
};

var solrQuery = function solrQuery(query) {
	var format = arguments.length <= 1 || arguments[1] === undefined ? { wt: "json" } : arguments[1];
	var searchFields = query.searchFields;
	var sortFields = query.sortFields;
	var rows = query.rows;
	var start = query.start;
	var facetLimit = query.facetLimit;
	var facetSort = query.facetSort;
	var pageStrategy = query.pageStrategy;
	var cursorMark = query.cursorMark;
	var idField = query.idField;

	var filters = (query.filters || []).map(function (filter) {
		return _extends({}, filter, { type: filter.type || "text" });
	});
	var queryParams = buildQuery(searchFields.concat(filters));

	var facetFieldParam = facetFields(searchFields);
	var facetSortParams = facetSorts(searchFields);
	var facetLimitParam = "facet.limit=" + (facetLimit || -1);
	var facetSortParam = "facet.sort=" + (facetSort || "index");

	var cursorMarkParam = pageStrategy === "cursor" ? "cursorMark=" + encodeURIComponent(cursorMark || "*") : "";
	var idSort = pageStrategy === "cursor" ? [{ field: idField, value: "asc" }] : [];

	var sortParam = buildSort(sortFields.concat(idSort));

	return "q=*:*&" + (queryParams.length > 0 ? queryParams : "") + ("" + (sortParam.length > 0 ? "&sort=" + sortParam : "")) + ("" + (facetFieldParam.length > 0 ? "&" + facetFieldParam : "")) + ("" + (facetSortParams.length > 0 ? "&" + facetSortParams : "")) + ("&rows=" + rows) + ("&" + facetLimitParam) + ("&" + facetSortParam) + ("&" + cursorMarkParam) + (start === null ? "" : "&start=" + start) + "&facet=on" + ("&" + buildFormat(format));
};

exports["default"] = solrQuery;
exports.rangeFacetToQueryFilter = rangeFacetToQueryFilter;
exports.listFacetFieldToQueryFilter = listFacetFieldToQueryFilter;
exports.textFieldToQueryFilter = textFieldToQueryFilter;
exports.fieldToQueryFilter = fieldToQueryFilter;
exports.buildQuery = buildQuery;
exports.facetFields = facetFields;
exports.facetSorts = facetSorts;
exports.buildSort = buildSort;
exports.solrQuery = solrQuery;

},{}],13:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _resultsResult = _dereq_("./results/result");

var _resultsResult2 = _interopRequireDefault(_resultsResult);

var _textSearch = _dereq_("./text-search");

var _textSearch2 = _interopRequireDefault(_textSearch);

var _listFacet = _dereq_("./list-facet");

var _listFacet2 = _interopRequireDefault(_listFacet);

var _resultsHeader = _dereq_("./results/header");

var _resultsHeader2 = _interopRequireDefault(_resultsHeader);

var _resultsList = _dereq_("./results/list");

var _resultsList2 = _interopRequireDefault(_resultsList);

var _resultsPending = _dereq_("./results/pending");

var _resultsPending2 = _interopRequireDefault(_resultsPending);

var _resultsContainer = _dereq_("./results/container");

var _resultsContainer2 = _interopRequireDefault(_resultsContainer);

var _resultsPagination = _dereq_("./results/pagination");

var _resultsPagination2 = _interopRequireDefault(_resultsPagination);

var _resultsPreloadIndicator = _dereq_("./results/preload-indicator");

var _resultsPreloadIndicator2 = _interopRequireDefault(_resultsPreloadIndicator);

var _resultsCsvExport = _dereq_("./results/csv-export");

var _resultsCsvExport2 = _interopRequireDefault(_resultsCsvExport);

var _searchFieldContainer = _dereq_("./search-field-container");

var _searchFieldContainer2 = _interopRequireDefault(_searchFieldContainer);

var _rangeFacet = _dereq_("./range-facet");

var _rangeFacet2 = _interopRequireDefault(_rangeFacet);

var _resultsCountLabel = _dereq_("./results/count-label");

var _resultsCountLabel2 = _interopRequireDefault(_resultsCountLabel);

var _sortMenu = _dereq_("./sort-menu");

var _sortMenu2 = _interopRequireDefault(_sortMenu);

var _currentQuery = _dereq_("./current-query");

var _currentQuery2 = _interopRequireDefault(_currentQuery);

exports["default"] = {
	searchFields: {
		text: _textSearch2["default"],
		"list-facet": _listFacet2["default"],
		"range-facet": _rangeFacet2["default"],
		container: _searchFieldContainer2["default"],
		currentQuery: _currentQuery2["default"]
	},
	results: {
		result: _resultsResult2["default"],
		resultCount: _resultsCountLabel2["default"],
		header: _resultsHeader2["default"],
		list: _resultsList2["default"],
		container: _resultsContainer2["default"],
		pending: _resultsPending2["default"],
		preloadIndicator: _resultsPreloadIndicator2["default"],
		csvExport: _resultsCsvExport2["default"],
		paginate: _resultsPagination2["default"]
	},
	sortFields: {
		menu: _sortMenu2["default"]
	}
};
module.exports = exports["default"];

},{"./current-query":14,"./list-facet":18,"./range-facet":19,"./results/container":21,"./results/count-label":22,"./results/csv-export":23,"./results/header":24,"./results/list":25,"./results/pagination":26,"./results/pending":27,"./results/preload-indicator":28,"./results/result":29,"./search-field-container":30,"./sort-menu":32,"./text-search":33}],14:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var CurrentQuery = (function (_React$Component) {
	_inherits(CurrentQuery, _React$Component);

	function CurrentQuery() {
		_classCallCheck(this, CurrentQuery);

		_get(Object.getPrototypeOf(CurrentQuery.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(CurrentQuery, [{
		key: "removeListFacetValue",
		value: function removeListFacetValue(field, values, value) {
			var foundIdx = values.indexOf(value);
			if (foundIdx > -1) {
				this.props.onChange(field, values.filter(function (v, i) {
					return i !== foundIdx;
				}));
			}
		}
	}, {
		key: "removeRangeFacetValue",
		value: function removeRangeFacetValue(field) {
			this.props.onChange(field, []);
		}
	}, {
		key: "removeTextValue",
		value: function removeTextValue(field) {
			this.props.onChange(field, "");
		}
	}, {
		key: "renderFieldValues",
		value: function renderFieldValues(searchField) {
			var _this = this;

			var bootstrapCss = this.props.bootstrapCss;

			switch (searchField.type) {
				case "list-facet":
					return searchField.value.map(function (val, i) {
						return _react2["default"].createElement(
							"span",
							{ className: (0, _classnames2["default"])({ "label": bootstrapCss, "label-default": bootstrapCss }), key: i,
								onClick: function () {
									return _this.removeListFacetValue(searchField.field, searchField.value, val);
								} },
							val,
							_react2["default"].createElement(
								"a",
								null,
								bootstrapCss ? _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove-sign" }) : "❌"
							)
						);
					});

				case "range-facet":
					return _react2["default"].createElement(
						"span",
						{ className: (0, _classnames2["default"])({ "label": bootstrapCss, "label-default": bootstrapCss }),
							onClick: function () {
								return _this.removeRangeFacetValue(searchField.field);
							} },
						searchField.value[0],
						" - ",
						searchField.value[1],
						_react2["default"].createElement(
							"a",
							null,
							bootstrapCss ? _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove-sign" }) : "❌"
						)
					);

				case "text":
					return _react2["default"].createElement(
						"span",
						{ className: (0, _classnames2["default"])({ "label": bootstrapCss, "label-default": bootstrapCss }),
							onClick: function () {
								return _this.removeTextValue(searchField.field);
							} },
						searchField.value,
						_react2["default"].createElement(
							"a",
							null,
							bootstrapCss ? _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove-sign" }) : "❌"
						)
					);
			}
			return null;
		}
	}, {
		key: "render",
		value: function render() {
			var _this2 = this;

			var _props = this.props;
			var bootstrapCss = _props.bootstrapCss;
			var query = _props.query;

			var splitFields = query.searchFields.filter(function (searchField) {
				return searchField.value && searchField.value.length > 0;
			}).map(function (searchField, i) {
				return i % 2 === 0 ? { type: "odds", searchField: searchField } : { type: "evens", searchField: searchField };
			});

			var odds = splitFields.filter(function (sf) {
				return sf.type === "evens";
			}).map(function (sf) {
				return sf.searchField;
			});
			var evens = splitFields.filter(function (sf) {
				return sf.type === "odds";
			}).map(function (sf) {
				return sf.searchField;
			});

			if (odds.length === 0 && evens.length === 0) {
				return null;
			}

			return _react2["default"].createElement(
				"div",
				{ className: (0, _classnames2["default"])("current-query", { "panel-body": bootstrapCss }) },
				_react2["default"].createElement(
					"div",
					{ className: (0, _classnames2["default"])({ "row": bootstrapCss }) },
					_react2["default"].createElement(
						"ul",
						{ className: (0, _classnames2["default"])({ "col-md-6": bootstrapCss }) },
						evens.map(function (searchField, i) {
							return _react2["default"].createElement(
								"li",
								{ className: (0, _classnames2["default"])({ "list-group-item": bootstrapCss }), key: i },
								_react2["default"].createElement(
									"label",
									null,
									searchField.label
								),
								_this2.renderFieldValues(searchField)
							);
						})
					),
					_react2["default"].createElement(
						"ul",
						{ className: (0, _classnames2["default"])({ "col-md-6": bootstrapCss }) },
						odds.map(function (searchField, i) {
							return _react2["default"].createElement(
								"li",
								{ className: (0, _classnames2["default"])({ "list-group-item": bootstrapCss }), key: i },
								_react2["default"].createElement(
									"label",
									null,
									searchField.label
								),
								_this2.renderFieldValues(searchField)
							);
						})
					)
				)
			);
		}
	}]);

	return CurrentQuery;
})(_react2["default"].Component);

CurrentQuery.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	onChange: _react2["default"].PropTypes.func,
	query: _react2["default"].PropTypes.object
};

exports["default"] = CurrentQuery;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],15:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var CheckedIcon = (function (_React$Component) {
	_inherits(CheckedIcon, _React$Component);

	function CheckedIcon() {
		_classCallCheck(this, CheckedIcon);

		_get(Object.getPrototypeOf(CheckedIcon.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(CheckedIcon, [{
		key: "render",
		value: function render() {
			var title = this.props.title != null ? _react2["default"].createElement(
				"title",
				null,
				this.props.title
			) : null;

			return _react2["default"].createElement(
				"svg",
				{ className: "checkbox-icon checked", viewBox: "0 0 489 402", width: "10" },
				title,
				_react2["default"].createElement("path", { d: "M 377.87,24.128 C 361.786,8.044 342.417,0.002 319.769,0.002 H 82.227 C 59.579,0.002 40.211,8.044 24.125,24.128 8.044,40.214 0.002,59.578 0.002,82.23 v 237.543 c 0,22.647 8.042,42.014 24.123,58.101 16.086,16.085 35.454,24.127 58.102,24.127 h 237.542 c 22.648,0 42.011,-8.042 58.102,-24.127 16.085,-16.087 24.126,-35.453 24.126,-58.101 V 82.23 C 401.993,59.582 393.951,40.214 377.87,24.128 z m -12.422,295.645 c 0,12.559 -4.47,23.314 -13.415,32.264 -8.945,8.945 -19.698,13.411 -32.265,13.411 H 82.227 c -12.563,0 -23.317,-4.466 -32.264,-13.411 -8.945,-8.949 -13.418,-19.705 -13.418,-32.264 V 82.23 c 0,-12.562 4.473,-23.316 13.418,-32.264 C 58.91,41.02 69.664,36.548 82.227,36.548 h 237.542 c 12.566,0 23.319,4.473 32.265,13.418 8.945,8.947 13.415,19.701 13.415,32.264 v 237.543 l -0.001,0 z" }),
				_react2["default"].createElement("path", { d: "M 480.59183,75.709029 442.06274,38.831006 c -5.28301,-5.060423 -11.70817,-7.591583 -19.26056,-7.591583 -7.55937,0 -13.98453,2.53116 -19.26753,7.591583 L 217.6825,216.98773 134.38968,136.99258 c -5.28896,-5.06231 -11.71015,-7.59062 -19.26256,-7.59062 -7.55736,0 -13.97854,2.52831 -19.267516,7.59062 l -38.529082,36.87898 c -5.28897,5.06136 -7.932461,11.20929 -7.932461,18.44186 0,7.22686 2.643491,13.38049 7.932461,18.4409 l 102.555358,98.15873 38.53207,36.87803 c 5.28598,5.06421 11.70916,7.59253 19.26455,7.59253 7.5524,0 13.97558,-2.53496 19.26454,-7.59253 l 38.53107,-36.87803 205.11372,-196.32314 c 5.284,-5.06232 7.93246,-11.20929 7.93246,-18.441873 0.005,-7.228765 -2.64846,-13.376685 -7.93246,-18.439008 z" })
			);
		}
	}]);

	return CheckedIcon;
})(_react2["default"].Component);

CheckedIcon.defaultProps = {};

CheckedIcon.propTypes = {
	title: _react2["default"].PropTypes.string
};

exports["default"] = CheckedIcon;
module.exports = exports["default"];

},{"react":"react"}],16:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var Search = (function (_React$Component) {
	_inherits(Search, _React$Component);

	function Search() {
		_classCallCheck(this, Search);

		_get(Object.getPrototypeOf(Search.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Search, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"svg",
				{ className: "search-icon", viewBox: "0 0 250.313 250.313", width: "10" },
				_react2["default"].createElement("path", { d: "M244.186,214.604l-54.379-54.378c-0.289-0.289-0.628-0.491-0.93-0.76 c10.7-16.231,16.945-35.66,16.945-56.554C205.822,46.075,159.747,0,102.911,0S0,46.075,0,102.911 c0,56.835,46.074,102.911,102.91,102.911c20.895,0,40.323-6.245,56.554-16.945c0.269,0.301,0.47,0.64,0.759,0.929l54.38,54.38 c8.169,8.168,21.413,8.168,29.583,0C252.354,236.017,252.354,222.773,244.186,214.604z M102.911,170.146 c-37.134,0-67.236-30.102-67.236-67.235c0-37.134,30.103-67.236,67.236-67.236c37.132,0,67.235,30.103,67.235,67.236 C170.146,140.044,140.043,170.146,102.911,170.146z" })
			);
		}
	}]);

	return Search;
})(_react2["default"].Component);

exports["default"] = Search;
module.exports = exports["default"];

},{"react":"react"}],17:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var UncheckedIcon = (function (_React$Component) {
	_inherits(UncheckedIcon, _React$Component);

	function UncheckedIcon() {
		_classCallCheck(this, UncheckedIcon);

		_get(Object.getPrototypeOf(UncheckedIcon.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(UncheckedIcon, [{
		key: "render",
		value: function render() {
			var title = this.props.title != null ? _react2["default"].createElement(
				"title",
				null,
				this.props.title
			) : null;

			return _react2["default"].createElement(
				"svg",
				{ className: "checkbox-icon unchecked", viewBox: "0 0 401.998 401.998", width: "10" },
				_react2["default"].createElement("path", { d: "M377.87,24.126C361.786,8.042,342.417,0,319.769,0H82.227C59.579,0,40.211,8.042,24.125,24.126 C8.044,40.212,0.002,59.576,0.002,82.228v237.543c0,22.647,8.042,42.014,24.123,58.101c16.086,16.085,35.454,24.127,58.102,24.127 h237.542c22.648,0,42.011-8.042,58.102-24.127c16.085-16.087,24.126-35.453,24.126-58.101V82.228 C401.993,59.58,393.951,40.212,377.87,24.126z M365.448,319.771c0,12.559-4.47,23.314-13.415,32.264 c-8.945,8.945-19.698,13.411-32.265,13.411H82.227c-12.563,0-23.317-4.466-32.264-13.411c-8.945-8.949-13.418-19.705-13.418-32.264 V82.228c0-12.562,4.473-23.316,13.418-32.264c8.947-8.946,19.701-13.418,32.264-13.418h237.542 c12.566,0,23.319,4.473,32.265,13.418c8.945,8.947,13.415,19.701,13.415,32.264V319.771L365.448,319.771z" })
			);
		}
	}]);

	return UncheckedIcon;
})(_react2["default"].Component);

UncheckedIcon.defaultProps = {};

UncheckedIcon.propTypes = {
	title: _react2["default"].PropTypes.string
};

exports["default"] = UncheckedIcon;
module.exports = exports["default"];

},{"react":"react"}],18:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _iconsChecked = _dereq_("../icons/checked");

var _iconsChecked2 = _interopRequireDefault(_iconsChecked);

var _iconsUnchecked = _dereq_("../icons/unchecked");

var _iconsUnchecked2 = _interopRequireDefault(_iconsUnchecked);

var ListFacet = (function (_React$Component) {
	_inherits(ListFacet, _React$Component);

	function ListFacet(props) {
		_classCallCheck(this, ListFacet);

		_get(Object.getPrototypeOf(ListFacet.prototype), "constructor", this).call(this, props);

		this.state = {
			filter: "",
			truncateFacetListsAt: props.truncateFacetListsAt
		};
	}

	_createClass(ListFacet, [{
		key: "handleClick",
		value: function handleClick(value) {
			var foundIdx = this.props.value.indexOf(value);
			if (foundIdx < 0) {
				this.props.onChange(this.props.field, this.props.value.concat(value));
			} else {
				this.props.onChange(this.props.field, this.props.value.filter(function (v, i) {
					return i !== foundIdx;
				}));
			}
		}
	}, {
		key: "toggleExpand",
		value: function toggleExpand() {
			this.props.onSetCollapse(this.props.field, !(this.props.collapse || false));
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var query = _props.query;
			var label = _props.label;
			var facets = _props.facets;
			var field = _props.field;
			var value = _props.value;
			var bootstrapCss = _props.bootstrapCss;
			var facetSort = _props.facetSort;
			var collapse = _props.collapse;
			var truncateFacetListsAt = this.state.truncateFacetListsAt;

			var facetCounts = facets.filter(function (facet, i) {
				return i % 2 === 1;
			});
			var facetValues = facets.filter(function (facet, i) {
				return i % 2 === 0;
			});

			var facetSortValue = facetSort ? facetSort : query.facetSort ? query.facetSort : query.facetLimit && query.facetLimit > -1 ? "count" : "index";

			var expanded = !(collapse || false);

			var showMoreLink = truncateFacetListsAt > -1 && truncateFacetListsAt < facetValues.length ? _react2["default"].createElement(
				"li",
				{ className: (0, _classnames2["default"])({ "list-group-item": bootstrapCss }), onClick: function () {
						return _this.setState({ truncateFacetListsAt: -1 });
					} },
				"Show all (",
				facetValues.length,
				")"
			) : null;

			return _react2["default"].createElement(
				"li",
				{ className: (0, _classnames2["default"])("list-facet", { "list-group-item": bootstrapCss }), id: "solr-list-facet-" + field },
				_react2["default"].createElement(
					"header",
					{ onClick: this.toggleExpand.bind(this) },
					_react2["default"].createElement(
						"h5",
						null,
						bootstrapCss ? _react2["default"].createElement(
							"span",
							null,
							_react2["default"].createElement("span", { className: (0, _classnames2["default"])("glyphicon", {
									"glyphicon-collapse-down": expanded,
									"glyphicon-collapse-up": !expanded
								}) }),
							" "
						) : null,
						label
					)
				),
				expanded ? _react2["default"].createElement(
					"div",
					null,
					_react2["default"].createElement(
						"ul",
						{ className: (0, _classnames2["default"])({ "list-group": bootstrapCss }) },
						facetValues.filter(function (facetValue, i) {
							return truncateFacetListsAt < 0 || i < truncateFacetListsAt;
						}).map(function (facetValue, i) {
							return _this.state.filter.length === 0 || facetValue.toLowerCase().indexOf(_this.state.filter.toLowerCase()) > -1 ? _react2["default"].createElement(
								"li",
								{ className: (0, _classnames2["default"])("facet-item-type-" + field, { "list-group-item": bootstrapCss }), key: facetValue + "_" + facetCounts[i], onClick: function () {
										return _this.handleClick(facetValue);
									} },
								value.indexOf(facetValue) > -1 ? _react2["default"].createElement(_iconsChecked2["default"], null) : _react2["default"].createElement(_iconsUnchecked2["default"], null),
								" ",
								facetValue,
								_react2["default"].createElement(
									"span",
									{ className: "facet-item-amount" },
									facetCounts[i]
								)
							) : null;
						}),
						showMoreLink
					),
					facetValues.length > 4 ? _react2["default"].createElement(
						"div",
						null,
						_react2["default"].createElement("input", { onChange: function (ev) {
								return _this.setState({ filter: ev.target.value });
							}, placeholder: "Filter... ", type: "text", value: this.state.filter }),
						" ",
						_react2["default"].createElement(
							"span",
							{ className: (0, _classnames2["default"])({ "btn-group": bootstrapCss }) },
							_react2["default"].createElement(
								"button",
								{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-xs": bootstrapCss, active: facetSortValue === "index" }),
									onClick: function () {
										return _this.props.onFacetSortChange(field, "index");
									} },
								"a-z"
							),
							_react2["default"].createElement(
								"button",
								{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-xs": bootstrapCss, active: facetSortValue === "count" }),
									onClick: function () {
										return _this.props.onFacetSortChange(field, "count");
									} },
								"0-9"
							)
						),
						_react2["default"].createElement(
							"span",
							{ className: (0, _classnames2["default"])({ "btn-group": bootstrapCss, "pull-right": bootstrapCss }) },
							_react2["default"].createElement(
								"button",
								{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-xs": bootstrapCss }),
									onClick: function () {
										return _this.props.onChange(field, []);
									} },
								"clear"
							)
						)
					) : null
				) : null
			);
		}
	}]);

	return ListFacet;
})(_react2["default"].Component);

ListFacet.defaultProps = {
	value: []
};

ListFacet.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	children: _react2["default"].PropTypes.array,
	collapse: _react2["default"].PropTypes.bool,
	facetSort: _react2["default"].PropTypes.string,
	facets: _react2["default"].PropTypes.array.isRequired,
	field: _react2["default"].PropTypes.string.isRequired,
	label: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func,
	onFacetSortChange: _react2["default"].PropTypes.func,
	onSetCollapse: _react2["default"].PropTypes.func,
	query: _react2["default"].PropTypes.object,
	truncateFacetListsAt: _react2["default"].PropTypes.number,
	value: _react2["default"].PropTypes.array
};

exports["default"] = ListFacet;
module.exports = exports["default"];

},{"../icons/checked":15,"../icons/unchecked":17,"classnames":1,"react":"react"}],19:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _rangeSlider = _dereq_("./range-slider");

var _rangeSlider2 = _interopRequireDefault(_rangeSlider);

var RangeFacet = (function (_React$Component) {
	_inherits(RangeFacet, _React$Component);

	function RangeFacet(props) {
		_classCallCheck(this, RangeFacet);

		_get(Object.getPrototypeOf(RangeFacet.prototype), "constructor", this).call(this, props);

		this.state = {
			value: props.value
		};
	}

	_createClass(RangeFacet, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			this.setState({ value: nextProps.value });
		}
	}, {
		key: "facetsToRange",
		value: function facetsToRange() {
			var facets = this.props.facets;

			return facets.filter(function (facet, i) {
				return i % 2 === 0;
			}).map(function (v) {
				return parseInt(v);
			}).sort(function (a, b) {
				return a > b ? 1 : -1;
			}).filter(function (a, i, me) {
				return i === 0 || i === me.length - 1;
			});
		}
	}, {
		key: "onRangeChange",
		value: function onRangeChange(range) {
			var bounds = this.facetsToRange();
			var lowerBound = bounds[0];
			var upperBound = bounds[1];
			var realRange = upperBound - lowerBound;

			var newState = {
				value: [Math.floor(range.lowerLimit * realRange) + lowerBound, Math.ceil(range.upperLimit * realRange) + lowerBound]
			};

			if (range.refresh) {
				this.props.onChange(this.props.field, newState.value);
			} else {
				this.setState(newState);
			}
		}
	}, {
		key: "getPercentage",
		value: function getPercentage(range, value) {
			var lowerBound = range[0];
			var upperBound = range[1];
			var realRange = upperBound - lowerBound;

			var atRange = value - lowerBound;
			return atRange / realRange;
		}
	}, {
		key: "toggleExpand",
		value: function toggleExpand(ev) {
			if (ev.target.className.indexOf("clear-button") < 0) {
				this.props.onSetCollapse(this.props.field, !(this.props.collapse || false));
			}
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var label = _props.label;
			var field = _props.field;
			var bootstrapCss = _props.bootstrapCss;
			var collapse = _props.collapse;
			var value = this.state.value;

			var range = this.facetsToRange();

			var filterRange = value.length > 0 ? value : range;

			return _react2["default"].createElement(
				"li",
				{ className: (0, _classnames2["default"])("range-facet", { "list-group-item": bootstrapCss }), id: "solr-range-facet-" + field },
				_react2["default"].createElement(
					"header",
					{ onClick: this.toggleExpand.bind(this) },
					_react2["default"].createElement(
						"button",
						{ style: { display: this.state.expanded ? "block" : "none" },
							className: (0, _classnames2["default"])("clear-button", {
								"btn": bootstrapCss,
								"btn-default": bootstrapCss,
								"btn-xs": bootstrapCss,
								"pull-right": bootstrapCss }),
							onClick: function () {
								return _this.props.onChange(field, []);
							} },
						"clear"
					),
					_react2["default"].createElement(
						"h5",
						null,
						bootstrapCss ? _react2["default"].createElement(
							"span",
							null,
							_react2["default"].createElement("span", { className: (0, _classnames2["default"])("glyphicon", {
									"glyphicon-collapse-down": !collapse,
									"glyphicon-collapse-up": collapse
								}) }),
							" "
						) : null,
						label
					)
				),
				_react2["default"].createElement(
					"div",
					{ style: { display: collapse ? "none" : "block" } },
					_react2["default"].createElement(_rangeSlider2["default"], { lowerLimit: this.getPercentage(range, filterRange[0]), onChange: this.onRangeChange.bind(this), upperLimit: this.getPercentage(range, filterRange[1]) }),
					_react2["default"].createElement(
						"label",
						null,
						filterRange[0]
					),
					_react2["default"].createElement(
						"label",
						{ className: (0, _classnames2["default"])({ "pull-right": bootstrapCss }) },
						filterRange[1]
					)
				)
			);
		}
	}]);

	return RangeFacet;
})(_react2["default"].Component);

RangeFacet.defaultProps = {
	value: []
};

RangeFacet.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	collapse: _react2["default"].PropTypes.bool,
	facets: _react2["default"].PropTypes.array.isRequired,
	field: _react2["default"].PropTypes.string.isRequired,
	label: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func,
	onSetCollapse: _react2["default"].PropTypes.func,
	value: _react2["default"].PropTypes.array
};

exports["default"] = RangeFacet;
module.exports = exports["default"];

},{"./range-slider":20,"classnames":1,"react":"react"}],20:[function(_dereq_,module,exports){
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

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = _dereq_("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var MOUSE_DOWN = 0;
var MOUSE_UP = 1;

var styles = {
	slider: {
		"MozUserSelect": "none",
		"WebkitUserSelect": "none",
		"MsUserSelect": "none",
		"UserSelect": "none",
		"WebkitUserDrag": "none",
		"userDrag": "none",
		"cursor": "pointer",
		width: "100%",
		stroke: "#f1ebe6",
		fill: "#f1ebe6"
	}
};

var RangeSlider = (function (_React$Component) {
	_inherits(RangeSlider, _React$Component);

	function RangeSlider(props) {
		_classCallCheck(this, RangeSlider);

		_get(Object.getPrototypeOf(RangeSlider.prototype), "constructor", this).call(this, props);
		this.mouseState = MOUSE_UP;
		this.mouseUpListener = this.onMouseUp.bind(this);
		this.mouseMoveListener = this.onMouseMove.bind(this);
		this.touchMoveListener = this.onTouchMove.bind(this);

		this.state = _extends({}, this.propsToState(this.props), { hoverState: null });
	}

	_createClass(RangeSlider, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			window.addEventListener("mouseup", this.mouseUpListener);
			window.addEventListener("mousemove", this.mouseMoveListener);
			window.addEventListener("touchend", this.mouseUpListener);
			window.addEventListener("touchmove", this.touchMoveListener);
		}
	}, {
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			this.setState(this.propsToState(nextProps));
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			window.removeEventListener("mouseup", this.mouseUpListener);
			window.removeEventListener("mousemove", this.mouseMoveListener);
			window.removeEventListener("touchend", this.mouseUpListener);
			window.removeEventListener("touchmove", this.touchMoveListener);
		}
	}, {
		key: "propsToState",
		value: function propsToState(props) {
			var lowerLimit = props.lowerLimit || 0;
			var upperLimit = props.upperLimit || 1;
			return {
				lowerLimit: lowerLimit,
				upperLimit: upperLimit
			};
		}
	}, {
		key: "getPositionForLimit",
		value: function getPositionForLimit(pageX) {
			var rect = _reactDom2["default"].findDOMNode(this).getBoundingClientRect();
			if (rect.width > 0) {
				var percentage = (pageX - rect.left) / rect.width;
				if (percentage > 1) {
					percentage = 1;
				} else if (percentage < 0) {
					percentage = 0;
				}
				var center = (this.state.upperLimit + this.state.lowerLimit) / 2;

				if (this.state.hoverState === "bar") {
					var lowerLimit = percentage + this.state.lowerLimit - center;
					var upperLimit = percentage - (center - this.state.upperLimit);
					if (upperLimit >= 1) {
						upperLimit = 1;
					}
					if (lowerLimit <= 0) {
						lowerLimit = 0;
					}
					return { lowerLimit: lowerLimit, upperLimit: upperLimit };
				} else if (this.state.hoverState === "lowerLimit") {
					if (percentage >= this.state.upperLimit) {
						percentage = this.state.upperLimit;
					}
					return { lowerLimit: percentage };
				} else if (this.state.hoverState === "upperLimit") {
					if (percentage <= this.state.lowerLimit) {
						percentage = this.state.lowerLimit;
					}
					return { upperLimit: percentage };
				}
			}
			return null;
		}
	}, {
		key: "setRange",
		value: function setRange(pageX) {
			var posForLim = this.getPositionForLimit(pageX);
			if (posForLim !== null) {
				this.setState(posForLim);
				this.props.onChange(_extends({}, this.state, { refresh: false }));
			}
		}
	}, {
		key: "onMouseDown",
		value: function onMouseDown(target, ev) {
			this.mouseState = MOUSE_DOWN;
			this.setState({ hoverState: target });
			return ev.preventDefault();
		}
	}, {
		key: "onMouseMove",
		value: function onMouseMove(ev) {
			if (this.mouseState === MOUSE_DOWN) {
				this.setRange(ev.pageX);
				return ev.preventDefault();
			}
		}
	}, {
		key: "onTouchMove",
		value: function onTouchMove(ev) {
			if (this.mouseState === MOUSE_DOWN) {
				this.setRange(ev.touches[0].pageX);
				return ev.preventDefault();
			}
		}
	}, {
		key: "onMouseUp",
		value: function onMouseUp() {
			if (this.mouseState === MOUSE_DOWN) {
				this.props.onChange(_extends({}, this.state, { refresh: true }));
			}
			this.setState({ hoverState: null });
			this.mouseState = MOUSE_UP;
		}
	}, {
		key: "getRangePath",
		value: function getRangePath() {
			return "M" + (8 + Math.floor(this.state.lowerLimit * 400)) + " 13 L " + (Math.ceil(this.state.upperLimit * 400) - 8) + " 13 Z";
		}
	}, {
		key: "getRangeCircle",
		value: function getRangeCircle(key) {
			var percentage = this.state[key];
			return _react2["default"].createElement("circle", {
				className: this.state.hoverState === key ? "hovering" : "",
				cx: percentage * 400, cy: "13",
				onMouseDown: this.onMouseDown.bind(this, key),
				onTouchStart: this.onMouseDown.bind(this, key),
				r: "13" });
		}
	}, {
		key: "render",
		value: function render() {
			var keys = this.state.hoverState === "lowerLimit" ? ["upperLimit", "lowerLimit"] : ["lowerLimit", "upperLimit"];
			return _react2["default"].createElement(
				"svg",
				{ className: "facet-range-slider", viewBox: "0 0 400 26" },
				_react2["default"].createElement("path", { d: "M0 0 L 0 26 Z", fill: "transparent" }),
				_react2["default"].createElement("path", { d: "M400 0 L 400 26 Z", fill: "transparent" }),
				_react2["default"].createElement("path", { d: "M0 13 L 400 13 Z", fill: "transparent" }),
				_react2["default"].createElement(
					"g",
					{ className: "range-line" },
					_react2["default"].createElement("path", {
						className: this.state.hoverState === "bar" ? "hovering" : "",
						d: this.getRangePath(),
						onMouseDown: this.onMouseDown.bind(this, "bar"),
						onTouchStart: this.onMouseDown.bind(this, "bar")
					}),
					this.getRangeCircle(keys[0]),
					this.getRangeCircle(keys[1])
				)
			);
		}
	}]);

	return RangeSlider;
})(_react2["default"].Component);

RangeSlider.propTypes = {
	lowerLimit: _react2["default"].PropTypes.number,
	onChange: _react2["default"].PropTypes.func.isRequired,
	upperLimit: _react2["default"].PropTypes.number
};

exports["default"] = RangeSlider;
module.exports = exports["default"];

},{"react":"react","react-dom":"react-dom"}],21:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var ResultContainer = (function (_React$Component) {
	_inherits(ResultContainer, _React$Component);

	function ResultContainer() {
		_classCallCheck(this, ResultContainer);

		_get(Object.getPrototypeOf(ResultContainer.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(ResultContainer, [{
		key: "render",
		value: function render() {
			var bootstrapCss = this.props.bootstrapCss;

			return _react2["default"].createElement(
				"div",
				{ className: (0, _classnames2["default"])("solr-search-results", { "col-md-9": bootstrapCss }) },
				_react2["default"].createElement(
					"div",
					{ className: (0, _classnames2["default"])({ "panel": bootstrapCss, "panel-default": bootstrapCss }) },
					this.props.children
				)
			);
		}
	}]);

	return ResultContainer;
})(_react2["default"].Component);

ResultContainer.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	children: _react2["default"].PropTypes.array
};

exports["default"] = ResultContainer;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],22:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var resultCountLabels = {
	pl: "Found % results",
	sg: "Found % result",
	none: "No results"
};

var Result = (function (_React$Component) {
	_inherits(Result, _React$Component);

	function Result() {
		_classCallCheck(this, Result);

		_get(Object.getPrototypeOf(Result.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Result, [{
		key: "render",
		value: function render() {
			var numFound = this.props.numFound;

			var resultLabel = numFound > 1 ? resultCountLabels.pl : numFound === 1 ? resultCountLabels.sg : resultCountLabels.none;

			return _react2["default"].createElement(
				"label",
				null,
				resultLabel.replace("%", numFound)
			);
		}
	}]);

	return Result;
})(_react2["default"].Component);

Result.propTypes = {
	numFound: _react2["default"].PropTypes.number.isRequired
};

exports["default"] = Result;
module.exports = exports["default"];

},{"react":"react"}],23:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

exports["default"] = function (props) {
	var bootstrapCss = props.bootstrapCss;
	var onClick = props.onClick;

	return _react2["default"].createElement(
		"button",
		{ onClick: onClick, className: (0, _classnames2["default"])({ btn: bootstrapCss, "btn-default": bootstrapCss, "pull-right": bootstrapCss, "btn-xs": bootstrapCss }) },
		"Export excel"
	);
};

module.exports = exports["default"];

},{"classnames":1,"react":"react"}],24:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var ResultHeader = (function (_React$Component) {
	_inherits(ResultHeader, _React$Component);

	function ResultHeader() {
		_classCallCheck(this, ResultHeader);

		_get(Object.getPrototypeOf(ResultHeader.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(ResultHeader, [{
		key: "render",
		value: function render() {
			var bootstrapCss = this.props.bootstrapCss;

			return _react2["default"].createElement(
				"div",
				{ className: (0, _classnames2["default"])({ "panel-heading": bootstrapCss }) },
				this.props.children
			);
		}
	}]);

	return ResultHeader;
})(_react2["default"].Component);

ResultHeader.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	children: _react2["default"].PropTypes.array
};

exports["default"] = ResultHeader;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],25:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var ResultList = (function (_React$Component) {
	_inherits(ResultList, _React$Component);

	function ResultList() {
		_classCallCheck(this, ResultList);

		_get(Object.getPrototypeOf(ResultList.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(ResultList, [{
		key: "render",
		value: function render() {
			var bootstrapCss = this.props.bootstrapCss;

			return _react2["default"].createElement(
				"ul",
				{ className: (0, _classnames2["default"])({ "list-group": bootstrapCss }) },
				this.props.children
			);
		}
	}]);

	return ResultList;
})(_react2["default"].Component);

ResultList.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	children: _react2["default"].PropTypes.array
};

exports["default"] = ResultList;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],26:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var Pagination = (function (_React$Component) {
	_inherits(Pagination, _React$Component);

	function Pagination() {
		_classCallCheck(this, Pagination);

		_get(Object.getPrototypeOf(Pagination.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Pagination, [{
		key: "onPageChange",
		value: function onPageChange(page, pageAmt) {
			if (page >= pageAmt || page < 0) {
				return;
			}
			this.props.onChange(page);
		}
	}, {
		key: "renderPage",
		value: function renderPage(page, currentPage, key) {
			return _react2["default"].createElement(
				"li",
				{ className: (0, _classnames2["default"])({ "active": page === currentPage }), key: key },
				_react2["default"].createElement(
					"a",
					{ onClick: this.onPageChange.bind(this, page) },
					page + 1
				)
			);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var bootstrapCss = _props.bootstrapCss;
			var query = _props.query;
			var results = _props.results;
			var start = query.start;
			var rows = query.rows;
			var numFound = results.numFound;

			var pageAmt = Math.ceil(numFound / rows);
			var currentPage = start / rows;

			var rangeStart = currentPage - 2 < 0 ? 0 : currentPage - 2;
			var rangeEnd = rangeStart + 5 > pageAmt ? pageAmt : rangeStart + 5;

			if (rangeEnd - rangeStart < 5 && rangeStart > 0) {
				rangeStart = rangeEnd - 5;
				if (rangeStart < 0) {
					rangeStart = 0;
				}
			}

			var pages = [];
			for (var page = rangeStart; page < rangeEnd; page++) {
				if (pages.indexOf(page) < 0) {
					pages.push(page);
				}
			}

			return _react2["default"].createElement(
				"div",
				{ className: (0, _classnames2["default"])({ "panel-body": bootstrapCss, "text-center": bootstrapCss }) },
				_react2["default"].createElement(
					"ul",
					{ className: (0, _classnames2["default"])("pagination", { "pagination-sm": bootstrapCss }) },
					_react2["default"].createElement(
						"li",
						{ className: (0, _classnames2["default"])({ "disabled": currentPage === 0 }), key: "start" },
						_react2["default"].createElement(
							"a",
							{ onClick: this.onPageChange.bind(this, 0) },
							"<<"
						)
					),
					_react2["default"].createElement(
						"li",
						{ className: (0, _classnames2["default"])({ "disabled": currentPage - 1 < 0 }), key: "prev" },
						_react2["default"].createElement(
							"a",
							{ onClick: this.onPageChange.bind(this, currentPage - 1) },
							"<"
						)
					),
					pages.map(function (page, idx) {
						return _this.renderPage(page, currentPage, idx);
					}),
					_react2["default"].createElement(
						"li",
						{ className: (0, _classnames2["default"])({ "disabled": currentPage + 1 >= pageAmt }), key: "next" },
						_react2["default"].createElement(
							"a",
							{ onClick: this.onPageChange.bind(this, currentPage + 1, pageAmt) },
							">"
						)
					),
					_react2["default"].createElement(
						"li",
						{ className: (0, _classnames2["default"])({ "disabled": currentPage === pageAmt - 1 }), key: "end" },
						_react2["default"].createElement(
							"a",
							{ onClick: this.onPageChange.bind(this, pageAmt - 1) },
							">>"
						)
					)
				)
			);
		}
	}]);

	return Pagination;
})(_react2["default"].Component);

Pagination.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	onChange: _react2["default"].PropTypes.func,
	query: _react2["default"].PropTypes.object,
	results: _react2["default"].PropTypes.object
};

exports["default"] = Pagination;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],27:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var Pending = (function (_React$Component) {
	_inherits(Pending, _React$Component);

	function Pending() {
		_classCallCheck(this, Pending);

		_get(Object.getPrototypeOf(Pending.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Pending, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"span",
				null,
				"Waiting for results"
			);
		}
	}]);

	return Pending;
})(_react2["default"].Component);

Pending.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool
};

exports["default"] = Pending;
module.exports = exports["default"];

},{"react":"react"}],28:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = _dereq_("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var PreloadIndicator = (function (_React$Component) {
	_inherits(PreloadIndicator, _React$Component);

	function PreloadIndicator(props) {
		_classCallCheck(this, PreloadIndicator);

		_get(Object.getPrototypeOf(PreloadIndicator.prototype), "constructor", this).call(this, props);

		this.scrollListener = this.onWindowScroll.bind(this);
	}

	_createClass(PreloadIndicator, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			window.addEventListener("scroll", this.scrollListener);
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			window.removeEventListener("scroll", this.scrollListener);
		}
	}, {
		key: "onWindowScroll",
		value: function onWindowScroll() {
			var pageStrategy = this.props.query.pageStrategy;
			var pending = this.props.results.pending;

			if (pageStrategy !== "cursor" || pending) {
				return;
			}

			var domNode = _reactDom2["default"].findDOMNode(this);
			if (!domNode) {
				return;
			}

			var _domNode$getBoundingClientRect = domNode.getBoundingClientRect();

			var top = _domNode$getBoundingClientRect.top;

			if (top < window.innerHeight) {
				this.props.onNextCursorQuery();
			}
		}
	}, {
		key: "render",
		value: function render() {
			var bootstrapCss = this.props.bootstrapCss;

			return _react2["default"].createElement(
				"li",
				{ className: (0, _classnames2["default"])("fetch-by-cursor", { "list-group-item": bootstrapCss }) },
				"Loading more..."
			);
		}
	}]);

	return PreloadIndicator;
})(_react2["default"].Component);

PreloadIndicator.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	onNextCursorQuery: _react2["default"].PropTypes.func,
	query: _react2["default"].PropTypes.object,
	results: _react2["default"].PropTypes.object
};

exports["default"] = PreloadIndicator;
module.exports = exports["default"];

},{"classnames":1,"react":"react","react-dom":"react-dom"}],29:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var Result = (function (_React$Component) {
	_inherits(Result, _React$Component);

	function Result() {
		_classCallCheck(this, Result);

		_get(Object.getPrototypeOf(Result.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Result, [{
		key: "renderValue",
		value: function renderValue(field, doc) {
			var value = [].concat(doc[field] || null).filter(function (v) {
				return v !== null;
			});

			return value.join(", ");
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var bootstrapCss = _props.bootstrapCss;
			var doc = _props.doc;
			var fields = _props.fields;

			return _react2["default"].createElement(
				"li",
				{ className: (0, _classnames2["default"])({ "list-group-item": bootstrapCss }), onClick: function () {
						return _this.props.onSelect(doc);
					} },
				_react2["default"].createElement(
					"ul",
					null,
					fields.filter(function (field) {
						return field.field !== "*";
					}).map(function (field, i) {
						return _react2["default"].createElement(
							"li",
							{ key: i },
							_react2["default"].createElement(
								"label",
								null,
								field.label || field.field
							),
							_this.renderValue(field.field, doc)
						);
					})
				)
			);
		}
	}]);

	return Result;
})(_react2["default"].Component);

Result.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	doc: _react2["default"].PropTypes.object,
	fields: _react2["default"].PropTypes.array,
	onSelect: _react2["default"].PropTypes.func.isRequired
};

exports["default"] = Result;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],30:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var SearchFieldContainer = (function (_React$Component) {
	_inherits(SearchFieldContainer, _React$Component);

	function SearchFieldContainer() {
		_classCallCheck(this, SearchFieldContainer);

		_get(Object.getPrototypeOf(SearchFieldContainer.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(SearchFieldContainer, [{
		key: "render",
		value: function render() {
			var _props = this.props;
			var bootstrapCss = _props.bootstrapCss;
			var onNewSearch = _props.onNewSearch;

			return _react2["default"].createElement(
				"div",
				{ className: (0, _classnames2["default"])({ "col-md-3": bootstrapCss }) },
				_react2["default"].createElement(
					"div",
					{ className: (0, _classnames2["default"])({ "panel": bootstrapCss, "panel-default": bootstrapCss }) },
					_react2["default"].createElement(
						"header",
						{ className: (0, _classnames2["default"])({ "panel-heading": bootstrapCss }) },
						_react2["default"].createElement(
							"button",
							{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-xs": bootstrapCss, "pull-right": bootstrapCss }),
								onClick: onNewSearch },
							"New search"
						),
						_react2["default"].createElement(
							"label",
							null,
							"Search"
						)
					),
					_react2["default"].createElement(
						"ul",
						{ className: (0, _classnames2["default"])("solr-search-fields", { "list-group": bootstrapCss }) },
						this.props.children
					)
				)
			);
		}
	}]);

	return SearchFieldContainer;
})(_react2["default"].Component);

SearchFieldContainer.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	children: _react2["default"].PropTypes.array,
	onNewSearch: _react2["default"].PropTypes.func
};

exports["default"] = SearchFieldContainer;
module.exports = exports["default"];

},{"classnames":1,"react":"react"}],31:[function(_dereq_,module,exports){
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

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _componentPack = _dereq_("./component-pack");

var _componentPack2 = _interopRequireDefault(_componentPack);

var SolrFacetedSearch = (function (_React$Component) {
	_inherits(SolrFacetedSearch, _React$Component);

	function SolrFacetedSearch() {
		_classCallCheck(this, SolrFacetedSearch);

		_get(Object.getPrototypeOf(SolrFacetedSearch.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(SolrFacetedSearch, [{
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var customComponents = _props.customComponents;
			var bootstrapCss = _props.bootstrapCss;
			var query = _props.query;
			var results = _props.results;
			var truncateFacetListsAt = _props.truncateFacetListsAt;
			var _props2 = this.props;
			var onSearchFieldChange = _props2.onSearchFieldChange;
			var onSortFieldChange = _props2.onSortFieldChange;
			var onPageChange = _props2.onPageChange;
			var onCsvExport = _props2.onCsvExport;
			var searchFields = query.searchFields;
			var sortFields = query.sortFields;
			var start = query.start;
			var rows = query.rows;

			var SearchFieldContainerComponent = customComponents.searchFields.container;
			var ResultContainerComponent = customComponents.results.container;

			var ResultComponent = customComponents.results.result;
			var ResultCount = customComponents.results.resultCount;
			var ResultHeaderComponent = customComponents.results.header;
			var ResultListComponent = customComponents.results.list;
			var ResultPendingComponent = customComponents.results.pending;
			var PaginateComponent = customComponents.results.paginate;
			var PreloadComponent = customComponents.results.preloadIndicator;
			var CsvExportComponent = customComponents.results.csvExport;
			var CurrentQueryComponent = customComponents.searchFields.currentQuery;
			var SortComponent = customComponents.sortFields.menu;
			var resultPending = results.pending ? _react2["default"].createElement(ResultPendingComponent, { bootstrapCss: bootstrapCss }) : null;

			var pagination = query.pageStrategy === "paginate" ? _react2["default"].createElement(PaginateComponent, _extends({}, this.props, { bootstrapCss: bootstrapCss, onChange: onPageChange })) : null;

			var preloadListItem = query.pageStrategy === "cursor" && results.docs.length < results.numFound ? _react2["default"].createElement(PreloadComponent, this.props) : null;

			return _react2["default"].createElement(
				"div",
				{ className: (0, _classnames2["default"])("solr-faceted-search", { "container": bootstrapCss, "col-md-12": bootstrapCss }) },
				_react2["default"].createElement(
					SearchFieldContainerComponent,
					{ bootstrapCss: bootstrapCss, onNewSearch: this.props.onNewSearch },
					searchFields.map(function (searchField, i) {
						var type = searchField.type;
						var field = searchField.field;

						var SearchComponent = customComponents.searchFields[type];
						var facets = type === "list-facet" || type === "range-facet" ? results.facets[field] || [] : null;
						return _react2["default"].createElement(SearchComponent, _extends({
							key: i }, _this.props, searchField, {
							bootstrapCss: bootstrapCss,
							facets: facets,
							truncateFacetListsAt: truncateFacetListsAt,
							onChange: onSearchFieldChange }));
					})
				),
				_react2["default"].createElement(
					ResultContainerComponent,
					{ bootstrapCss: bootstrapCss },
					_react2["default"].createElement(
						ResultHeaderComponent,
						{ bootstrapCss: bootstrapCss },
						_react2["default"].createElement(ResultCount, { bootstrapCss: bootstrapCss, numFound: results.numFound }),
						resultPending,
						_react2["default"].createElement(SortComponent, { bootstrapCss: bootstrapCss, onChange: onSortFieldChange, sortFields: sortFields }),
						this.props.showCsvExport ? _react2["default"].createElement(CsvExportComponent, { bootstrapCss: bootstrapCss, onClick: onCsvExport }) : null
					),
					_react2["default"].createElement(CurrentQueryComponent, _extends({}, this.props, { onChange: onSearchFieldChange })),
					pagination,
					_react2["default"].createElement(
						ResultListComponent,
						{ bootstrapCss: bootstrapCss },
						results.docs.map(function (doc, i) {
							return _react2["default"].createElement(ResultComponent, { bootstrapCss: bootstrapCss,
								doc: doc,
								fields: searchFields,
								key: doc.id || i,
								onSelect: _this.props.onSelectDoc,
								resultIndex: i,
								rows: rows,
								start: start
							});
						}),
						preloadListItem
					),
					pagination
				)
			);
		}
	}]);

	return SolrFacetedSearch;
})(_react2["default"].Component);

SolrFacetedSearch.defaultProps = {
	bootstrapCss: true,
	customComponents: _componentPack2["default"],
	pageStrategy: "paginate",
	rows: 20,
	searchFields: [{ type: "text", field: "*" }],
	sortFields: [],
	truncateFacetListsAt: -1,
	showCsvExport: false
};

SolrFacetedSearch.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	customComponents: _react2["default"].PropTypes.object,
	onCsvExport: _react2["default"].PropTypes.func,
	onNewSearch: _react2["default"].PropTypes.func,
	onPageChange: _react2["default"].PropTypes.func,
	onSearchFieldChange: _react2["default"].PropTypes.func.isRequired,
	onSelectDoc: _react2["default"].PropTypes.func,
	onSortFieldChange: _react2["default"].PropTypes.func.isRequired,
	query: _react2["default"].PropTypes.object,
	results: _react2["default"].PropTypes.object,
	showCsvExport: _react2["default"].PropTypes.bool,
	truncateFacetListsAt: _react2["default"].PropTypes.number
};

exports["default"] = SolrFacetedSearch;
module.exports = exports["default"];

},{"./component-pack":13,"classnames":1,"react":"react"}],32:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = _dereq_("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var SortMenu = (function (_React$Component) {
	_inherits(SortMenu, _React$Component);

	function SortMenu(props) {
		_classCallCheck(this, SortMenu);

		_get(Object.getPrototypeOf(SortMenu.prototype), "constructor", this).call(this, props);

		this.state = {
			isOpen: false
		};
		this.documentClickListener = this.handleDocumentClick.bind(this);
	}

	_createClass(SortMenu, [{
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
		key: "onSelect",
		value: function onSelect(sortField) {
			var foundIdx = this.props.sortFields.indexOf(sortField);
			if (foundIdx < 0) {
				this.props.onChange(sortField, "asc");
			} else {
				this.props.onChange(sortField, null);
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
			var bootstrapCss = _props.bootstrapCss;
			var sortFields = _props.sortFields;

			if (sortFields.length === 0) {
				return null;
			}

			var value = sortFields.find(function (sf) {
				return sf.value;
			});

			return _react2["default"].createElement(
				"span",
				{ className: (0, _classnames2["default"])({ "pull-right": bootstrapCss }) },
				_react2["default"].createElement(
					"span",
					{ className: (0, _classnames2["default"])({ "dropdown": bootstrapCss, "open": this.state.isOpen }) },
					_react2["default"].createElement(
						"button",
						{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-xs": bootstrapCss, "dropdown-toggle": bootstrapCss }),
							onClick: this.toggleSelect.bind(this) },
						value ? value.label : "- select sort -",
						" ",
						_react2["default"].createElement("span", { className: "caret" })
					),
					_react2["default"].createElement(
						"ul",
						{ className: "dropdown-menu" },
						sortFields.map(function (sortField, i) {
							return _react2["default"].createElement(
								"li",
								{ key: i },
								_react2["default"].createElement(
									"a",
									{ onClick: function () {
											_this.onSelect(sortField.field);_this.toggleSelect();
										} },
									sortField.label
								)
							);
						}),
						value ? _react2["default"].createElement(
							"li",
							null,
							_react2["default"].createElement(
								"a",
								{ onClick: function () {
										_this.props.onChange(value.field, null);_this.toggleSelect();
									} },
								"- clear -"
							)
						) : null
					)
				),
				value ? _react2["default"].createElement(
					"span",
					{ className: (0, _classnames2["default"])({ "btn-group": bootstrapCss }) },
					_react2["default"].createElement(
						"button",
						{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-xs": bootstrapCss, active: value.value === "asc" }),
							onClick: function () {
								return _this.props.onChange(value.field, "asc");
							} },
						"asc"
					),
					_react2["default"].createElement(
						"button",
						{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-xs": bootstrapCss, active: value.value === "desc" }),
							onClick: function () {
								return _this.props.onChange(value.field, "desc");
							} },
						"desc"
					)
				) : null
			);
		}
	}]);

	return SortMenu;
})(_react2["default"].Component);

SortMenu.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	onChange: _react2["default"].PropTypes.func,
	sortFields: _react2["default"].PropTypes.array
};

exports["default"] = SortMenu;
module.exports = exports["default"];

},{"classnames":1,"react":"react","react-dom":"react-dom"}],33:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _iconsSearch = _dereq_("../icons/search");

var _iconsSearch2 = _interopRequireDefault(_iconsSearch);

var TextSearch = (function (_React$Component) {
	_inherits(TextSearch, _React$Component);

	function TextSearch(props) {
		_classCallCheck(this, TextSearch);

		_get(Object.getPrototypeOf(TextSearch.prototype), "constructor", this).call(this, props);

		this.state = {
			value: ""
		};
	}

	_createClass(TextSearch, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			this.setState({
				value: nextProps.value
			});
		}
	}, {
		key: "handleInputChange",
		value: function handleInputChange(ev) {
			this.setState({
				value: ev.target.value
			});
		}
	}, {
		key: "handleInputKeyDown",
		value: function handleInputKeyDown(ev) {
			if (ev.keyCode === 13) {
				this.handleSubmit();
			}
		}
	}, {
		key: "handleSubmit",
		value: function handleSubmit() {
			this.props.onChange(this.props.field, this.state.value);
		}
	}, {
		key: "toggleExpand",
		value: function toggleExpand() {
			this.props.onSetCollapse(this.props.field, !(this.props.collapse || false));
		}
	}, {
		key: "render",
		value: function render() {
			var _props = this.props;
			var label = _props.label;
			var bootstrapCss = _props.bootstrapCss;
			var collapse = _props.collapse;

			return _react2["default"].createElement(
				"li",
				{ className: (0, _classnames2["default"])({ "list-group-item": bootstrapCss }) },
				_react2["default"].createElement(
					"header",
					{ onClick: this.toggleExpand.bind(this) },
					_react2["default"].createElement(
						"h5",
						null,
						bootstrapCss ? _react2["default"].createElement(
							"span",
							null,
							_react2["default"].createElement("span", { className: (0, _classnames2["default"])("glyphicon", {
									"glyphicon-collapse-down": !collapse,
									"glyphicon-collapse-up": collapse
								}) }),
							" "
						) : null,
						label
					)
				),
				_react2["default"].createElement(
					"div",
					{ style: { display: collapse ? "none" : "block" } },
					_react2["default"].createElement("input", {
						onChange: this.handleInputChange.bind(this),
						onKeyDown: this.handleInputKeyDown.bind(this),
						value: this.state.value || "" }),
					" ",
					_react2["default"].createElement(
						"button",
						{ className: (0, _classnames2["default"])({ "btn": bootstrapCss, "btn-default": bootstrapCss, "btn-sm": bootstrapCss }), onClick: this.handleSubmit.bind(this) },
						_react2["default"].createElement(_iconsSearch2["default"], null)
					)
				)
			);
		}
	}]);

	return TextSearch;
})(_react2["default"].Component);

TextSearch.defaultProps = {
	field: null
};

TextSearch.propTypes = {
	bootstrapCss: _react2["default"].PropTypes.bool,
	collapse: _react2["default"].PropTypes.bool,
	field: _react2["default"].PropTypes.string.isRequired,
	label: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func,
	onSetCollapse: _react2["default"].PropTypes.func
};

exports["default"] = TextSearch;
module.exports = exports["default"];

},{"../icons/search":16,"classnames":1,"react":"react"}],34:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _componentsSolrFacetedSearch = _dereq_("./components/solr-faceted-search");

var _componentsSolrFacetedSearch2 = _interopRequireDefault(_componentsSolrFacetedSearch);

var _componentsComponentPack = _dereq_("./components/component-pack");

var _componentsComponentPack2 = _interopRequireDefault(_componentsComponentPack);

var _apiSolrClient = _dereq_("./api/solr-client");

exports["default"] = _componentsSolrFacetedSearch2["default"];
exports.SolrFacetedSearch = _componentsSolrFacetedSearch2["default"];
exports.defaultComponentPack = _componentsComponentPack2["default"];
exports.SolrClient = _apiSolrClient.SolrClient;

},{"./api/solr-client":11,"./components/component-pack":13,"./components/solr-faceted-search":31}],35:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	searchFields: [],
	sortFields: [],
	rows: 0,
	url: null,
	pageStrategy: null,
	start: null
};

var setQueryFields = function setQueryFields(state, action) {
	return _extends({}, state, {
		searchFields: action.searchFields,
		sortFields: action.sortFields,
		url: action.url,
		rows: action.rows,
		pageStrategy: action.pageStrategy,
		start: action.start
	});
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_QUERY_FIELDS":
			return setQueryFields(state, action);
		case "SET_SEARCH_FIELDS":
			return _extends({}, state, { searchFields: action.newFields, start: state.pageStrategy === "paginate" ? 0 : null });
		case "SET_SORT_FIELDS":
			return _extends({}, state, { sortFields: action.newSortFields, start: state.pageStrategy === "paginate" ? 0 : null });
		case "SET_FILTERS":
			return _extends({}, state, { filters: action.newFilters, start: state.pageStrategy === "paginate" ? 0 : null });
		case "SET_START":
			return _extends({}, state, { start: action.newStart });
		case "SET_RESULTS":
			return action.data.nextCursorMark ? _extends({}, state, { cursorMark: action.data.nextCursorMark }) : state;
	}

	return state;
};

module.exports = exports["default"];

},{}],36:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	facets: {},
	docs: [],
	numFound: 0,
	pending: false
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_RESULTS":
			return _extends({}, state, {
				docs: action.data.response.docs,
				numFound: action.data.response.numFound,
				facets: action.data.facet_counts.facet_fields,
				pending: false
			});

		case "SET_NEXT_RESULTS":
			return _extends({}, state, {
				docs: state.docs.concat(action.data.response.docs),
				pending: false
			});

		case "SET_RESULTS_PENDING":
			return _extends({}, state, { pending: true
			});
	}

	return state;
};

module.exports = exports["default"];

},{}]},{},[34])(34)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],19:[function(require,module,exports){
module.exports = require('./lib/index');

},{"./lib/index":20}],20:[function(require,module,exports){
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
},{"./ponyfill":21}],21:[function(require,module,exports){
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
},{}],22:[function(require,module,exports){

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

},{}],23:[function(require,module,exports){
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

},{}],24:[function(require,module,exports){
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

},{"global/window":2,"is-function":3,"parse-headers":9,"xtend":25}],25:[function(require,module,exports){
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

},{}],26:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports["default"] = actionsMaker;

var _actionsSolr = require("./actions/solr");

function actionsMaker(navigateTo, dispatch) {
	var actions = {
		onCreateIndexes: function onCreateIndexes() {
			dispatch((0, _actionsSolr.createIndexes)());
		}
	};
	return actions;
}

;
module.exports = exports["default"];

},{"./actions/solr":28}],27:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var setArchetypes = function setArchetypes(afterInit) {
	return function (dispatch) {
		return (0, _xhr2["default"])({
			method: "GET",
			headers: {
				"Accept": "application/json"
			},
			url: globals.env.SERVER + "/v2.1/metadata/Admin?withCollectionInfo=true"
		}, function (err, resp) {
			if (resp.statusCode === 200) {
				var collectionMetadata = JSON.parse(resp.body);
				dispatch({ type: "SET_ARCHETYPES", collections: collectionMetadata });
				afterInit();
			}
		});
	};
};

var setVre = function setVre(vreId, afterInit) {
	return function (dispatch) {
		return (0, _xhr2["default"])({
			method: "GET",
			headers: {
				"Accept": "application/json"
			},
			url: globals.env.SERVER + "/v2.1/metadata/" + vreId + "?withCollectionInfo=true"
		}, function (err, resp) {
			if (resp.statusCode === 200) {
				var collectionMetadata = JSON.parse(resp.body);
				dispatch({ type: "SET_VRE", vreId: vreId, collections: collectionMetadata });
				dispatch(setArchetypes(afterInit));
			}
		});
	};
};

exports.setVre = setVre;

},{"xhr":24}],28:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _solrFacetedSearchReact = require("solr-faceted-search-react");

var searchClients = [];

var getSearchClients = function getSearchClients() {
	return searchClients;
};

var checkIndex = function checkIndex(afterCheck) {
	return function (dispatch, getState) {
		var _getState = getState();

		var collections = _getState.metadata.collections;

		var collection = Object.keys(collections).map(function (collectionName) {
			return collections[collectionName];
		}).filter(function (collection) {
			return !collection.unknown && !collection.relationCollection;
		}).map(function (collection) {
			return collection.collectionName;
		})[0];

		(0, _xhr2["default"])("/solr/" + collection + "/select", {
			headers: { "Accept": "application/json" }
		}, function (err, resp) {
			if (resp.statusCode !== 200) {
				dispatch({ type: "SET_INDEX_PRESENT", present: false });
			} else {
				dispatch(configureSearchClients());
			}
			afterCheck();
		});
	};
};

var getPropSuffix = function getPropSuffix(archetypeType) {
	return archetypeType === "datable" ? "i" : archetypeType === "text" ? "s" : archetypeType === "relation" ? "ss" : "";
};

var getFacetType = function getFacetType(archetypeType) {
	return archetypeType === "datable" ? "range-facet" : archetypeType === "text" ? "list-facet" : archetypeType === "relation" ? "list-facet" : "";
};

var configureSearchClients = function configureSearchClients() {
	return function (dispatch, getState) {
		var _getState2 = getState();

		var _getState2$metadata = _getState2.metadata;
		var collections = _getState2$metadata.collections;
		var archetypeCollections = _getState2$metadata.archetypeCollections;

		var archetypes = Object.keys(archetypeCollections).map(function (archetypeCollectionName) {
			return archetypeCollections[archetypeCollectionName];
		});

		searchClients = Object.keys(collections).map(function (collectionName) {
			return collections[collectionName];
		}).filter(function (collection) {
			return !collection.unknown && !collection.relationCollection;
		}).map(function (collection) {
			return {
				client: new _solrFacetedSearchReact.SolrClient({
					url: "/solr/" + collection.collectionName + "/select",
					searchFields: [{ label: "Search", field: "displayName_t", type: "text" }].concat(archetypes.find(function (archetype) {
						return archetype.archetypeName === collection.archetypeName;
					}).properties.map(function (prop) {
						return {
							label: prop.name,
							field: prop.name + "_" + getPropSuffix(prop.type),
							type: getFacetType(prop.type),
							collapse: true
						};
					})),
					sortFields: archetypes.find(function (archetype) {
						return archetype.archetypeName === collection.archetypeName;
					}).properties.map(function (prop) {
						return {
							label: prop.name,
							field: prop.name + "_" + getPropSuffix(prop.type)
						};
					}),
					facetSort: "count",
					rows: 25,
					onChange: function onChange(state) {
						return dispatch({
							type: "SET_SEARCH_STATE",
							collectionName: collection.collectionName,
							searchState: state
						});
					}
				}),
				name: collection.collectionName,
				label: collection.collectionLabel
			};
		});

		searchClients.forEach(function (searchClient) {
			return searchClient.client.initialize();
		});

		dispatch({ type: "SET_INDEX_PRESENT", present: true });
	};
};

var createIndexes = function createIndexes() {
	return function (dispatch, getState) {
		var _getState3 = getState();

		var vreId = _getState3.metadata.vreId;

		dispatch({ type: "INDEXES_PENDING" });
		(0, _xhr2["default"])("/" + vreId, {
			method: "POST"
		}, function (err, resp) {
			if (resp.statusCode === 200) {
				dispatch(configureSearchClients());
			} else {
				dispatch({ type: "INDEXES_FAILED" });
			}
		});
	};
};

exports["default"] = { checkIndex: checkIndex, createIndexes: createIndexes, getSearchClients: getSearchClients };
module.exports = exports["default"];

},{"solr-faceted-search-react":18,"xhr":24}],29:[function(require,module,exports){
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

var _actionsSolr = require("../actions/solr");

var _facetedSearchFacetedSearch = require("./faceted-search/faceted-search");

var _facetedSearchFacetedSearch2 = _interopRequireDefault(_facetedSearchFacetedSearch);

var _pageJsx = require("./page.jsx");

var _pageJsx2 = _interopRequireDefault(_pageJsx);

var App = (function (_React$Component) {
	_inherits(App, _React$Component);

	function App(props) {
		_classCallCheck(this, App);

		_get(Object.getPrototypeOf(App.prototype), "constructor", this).call(this, props);
		this.state = {
			activeClient: null
		};
	}

	_createClass(App, [{
		key: "setActiveClient",
		value: function setActiveClient(name) {
			this.setState({ activeClient: name });
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var solr = _props.solr;
			var onCreateIndexes = _props.onCreateIndexes;
			var activeClient = this.state.activeClient;

			var searchClients = (0, _actionsSolr.getSearchClients)();

			var visibleClient = !activeClient && searchClients.length > 0 ? searchClients[0] : searchClients.find(function (client) {
				return client.name === activeClient;
			});

			var visibleClientName = visibleClient ? visibleClient.name : null;

			var facetedSearchProps = visibleClient ? _extends({
				collections: searchClients.map(function (searchClient) {
					return {
						label: searchClient.label,
						name: searchClient.name,
						selected: searchClient.name === visibleClientName,
						query: solr.searchStates[searchClient.name] ? solr.searchStates[searchClient.name].query : {},
						results: solr.searchStates[searchClient.name] ? solr.searchStates[searchClient.name].results : {
							docs: [],
							numFound: 0,
							facets: {}
						}
					};
				}),
				onCollectionSelect: function onCollectionSelect(collectionName) {
					return _this.setActiveClient(collectionName);
				}
			}, visibleClient.client.getHandlers(), {
				truncateFacetListsAt: 5
			}) : null;

			return solr.indexPresent ? _react2["default"].createElement(_facetedSearchFacetedSearch2["default"], facetedSearchProps) : _react2["default"].createElement(
				_pageJsx2["default"],
				null,
				_react2["default"].createElement(
					"div",
					{ className: "container" },
					_react2["default"].createElement(
						"div",
						{ className: "col-md-6" },
						"Your dataset does not appear to have indexes yet."
					),
					_react2["default"].createElement(
						"div",
						{ className: "col-md-6" },
						_react2["default"].createElement(
							"button",
							{ className: "btn btn-success",
								onClick: onCreateIndexes,
								disabled: solr.indexesPending },
							solr.indexesPending ? "Creating search index, please wait" : "Create search index"
						)
					)
				)
			);
		}
	}]);

	return App;
})(_react2["default"].Component);

exports["default"] = App;
module.exports = exports["default"];

},{"../actions/solr":28,"./faceted-search/faceted-search":31,"./page.jsx":41,"react":"react"}],30:[function(require,module,exports){
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

var CurrentQuery = (function (_React$Component) {
  _inherits(CurrentQuery, _React$Component);

  function CurrentQuery() {
    _classCallCheck(this, CurrentQuery);

    _get(Object.getPrototypeOf(CurrentQuery.prototype), "constructor", this).apply(this, arguments);
  }

  _createClass(CurrentQuery, [{
    key: "removeListFacetValue",
    value: function removeListFacetValue(field, values, value) {
      var foundIdx = values.indexOf(value);
      if (foundIdx > -1) {
        this.props.onChange(field, values.filter(function (v, i) {
          return i !== foundIdx;
        }));
      }
    }
  }, {
    key: "removeRangeFacetValue",
    value: function removeRangeFacetValue(field) {
      this.props.onChange(field, []);
    }
  }, {
    key: "removeTextValue",
    value: function removeTextValue(field) {
      this.props.onChange(field, "");
    }
  }, {
    key: "renderFieldValues",
    value: function renderFieldValues(searchField) {
      var _this = this;

      switch (searchField.type) {
        case "list-facet":
          return searchField.value.map(function (val, i) {
            return _react2["default"].createElement(
              "span",
              { key: searchField.field + "-" + i },
              _react2["default"].createElement(
                "span",
                { className: "btn btn-primary btn-sm downcase-then-capitalize", onClick: function () {
                    return _this.removeListFacetValue(searchField.field, searchField.value, val);
                  },
                  title: searchField.label + ": " + val },
                val,
                " ",
                _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove-sign hi-half-transp" })
              ),
              " "
            );
          });

        case "range-facet":
          return _react2["default"].createElement(
            "span",
            { key: "" + searchField.field },
            _react2["default"].createElement(
              "span",
              { className: "btn btn-primary btn-sm downcase-then-capitalize", onClick: function () {
                  return _this.removeRangeFacetValue(searchField.field);
                },
                title: searchField.label + ": " + searchField.value[0] + " - " + searchField.value[1] },
              searchField.value[0],
              " - ",
              searchField.value[1],
              " ",
              _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove-sign hi-half-transp" })
            ),
            " "
          );

        case "text":
          return _react2["default"].createElement(
            "span",
            { key: "" + searchField.field },
            _react2["default"].createElement(
              "span",
              { className: "btn btn-primary btn-sm downcase-then-capitalize", onClick: function () {
                  return _this.removeTextValue(searchField.field);
                },
                title: searchField.label + ": " + searchField.value },
              searchField.value,
              " ",
              _react2["default"].createElement("span", { className: "glyphicon glyphicon-remove-sign hi-half-transp" })
            ),
            " "
          );
      }
      return null;
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var searchFields = this.props.searchFields;

      return _react2["default"].createElement(
        "div",
        null,
        searchFields.filter(function (searchField) {
          return searchField.value && searchField.value.length > 0;
        }).map(function (searchField) {
          return _this2.renderFieldValues(searchField);
        })
      );
    }
  }]);

  return CurrentQuery;
})(_react2["default"].Component);

CurrentQuery.propTypes = {
  onChange: _react2["default"].PropTypes.func,
  searchFields: _react2["default"].PropTypes.array
};

exports["default"] = CurrentQuery;
module.exports = exports["default"];

},{"react":"react"}],31:[function(require,module,exports){
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

var _searchFields = require("./search-fields");

var _searchFields2 = _interopRequireDefault(_searchFields);

var _pageJsx = require("../page.jsx");

var _pageJsx2 = _interopRequireDefault(_pageJsx);

var _currentQuery = require("./current-query");

var _currentQuery2 = _interopRequireDefault(_currentQuery);

var _resultsPagination = require("./results/pagination");

var _resultsPagination2 = _interopRequireDefault(_resultsPagination);

var _sortMenu = require("./sort-menu");

var _sortMenu2 = _interopRequireDefault(_sortMenu);

var FacetedSearch = (function (_React$Component) {
  _inherits(FacetedSearch, _React$Component);

  function FacetedSearch() {
    _classCallCheck(this, FacetedSearch);

    _get(Object.getPrototypeOf(FacetedSearch.prototype), "constructor", this).apply(this, arguments);
  }

  _createClass(FacetedSearch, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var collections = _props.collections;
      var truncateFacetListsAt = _props.truncateFacetListsAt;
      var _props2 = this.props;
      var onCollectionSelect = _props2.onCollectionSelect;
      var onSearchFieldChange = _props2.onSearchFieldChange;
      var onNewSearch = _props2.onNewSearch;
      var onPageChange = _props2.onPageChange;
      var onSortFieldChange = _props2.onSortFieldChange;
      var onSetCollapse = _props2.onSetCollapse;
      var onFacetSortChange = _props2.onFacetSortChange;

      var activeCollection = collections.find(function (collection) {
        return collection.selected;
      });

      return _react2["default"].createElement(
        _pageJsx2["default"],
        null,
        _react2["default"].createElement(
          "div",
          { className: "container big-margin" },
          _react2["default"].createElement(
            "div",
            { className: "row" },
            _react2["default"].createElement(
              "div",
              { className: "col-sm-4 col-md-3" },
              _react2["default"].createElement(
                "div",
                { className: "basic-margin" },
                _react2["default"].createElement(
                  _fieldsSelectField2["default"],
                  { btnClass: "btn-default", onChange: onCollectionSelect, noClear: true, value: activeCollection.name },
                  collections.map(function (collection) {
                    return _react2["default"].createElement(
                      "span",
                      { key: collection.name, value: collection.name },
                      collection.label
                    );
                  })
                )
              ),
              _react2["default"].createElement(_searchFields2["default"], { fields: activeCollection.query.searchFields, query: activeCollection.query,
                truncateFacetListsAt: truncateFacetListsAt,
                onSetCollapse: onSetCollapse,
                onFacetSortChange: onFacetSortChange,
                results: activeCollection.results, onSearchFieldChange: onSearchFieldChange })
            ),
            _react2["default"].createElement("div", { className: ".hidden-sm col-md-1" }),
            _react2["default"].createElement(
              "div",
              { className: "col-sm-8 col-md-8" },
              _react2["default"].createElement(_sortMenu2["default"], { onChange: onSortFieldChange, sortFields: activeCollection.query.sortFields }),
              _react2["default"].createElement(
                "div",
                { className: "basic-margin" },
                _react2["default"].createElement(
                  "strong",
                  null,
                  "Found ",
                  activeCollection.results.numFound,
                  " ",
                  activeCollection.results.numFound === 1 ? activeCollection.label.replace(/s$/, "") : activeCollection.label
                )
              ),
              _react2["default"].createElement(
                "div",
                { className: "result-list big-margin" },
                _react2["default"].createElement(
                  "ol",
                  { start: activeCollection.query.start + 1, style: { counterReset: "step-counter " + activeCollection.query.start } },
                  activeCollection.results.docs.map(function (doc, i) {
                    return _react2["default"].createElement(
                      "li",
                      { key: i + activeCollection.query.start },
                      _react2["default"].createElement(
                        "a",
                        { target: "_blank", href: globals.env.SERVER + "/v2.1/domain/" + activeCollection.name + "/" + doc.id },
                        doc.displayName_s,
                        doc.birthDate_i ? _react2["default"].createElement(
                          "span",
                          { className: "hi-light-grey pull-right" },
                          doc.birthDate_i,
                          " - ",
                          doc.deathDate_i
                        ) : null,
                        doc.country_s ? _react2["default"].createElement(
                          "span",
                          { className: "hi-light-grey pull-right" },
                          doc.country_s
                        ) : null
                      )
                    );
                  })
                )
              )
            )
          )
        ),
        _react2["default"].createElement(
          "span",
          { type: "footer-body" },
          _react2["default"].createElement(
            "span",
            { className: "col-sm-2 col-md-2" },
            _react2["default"].createElement(
              "button",
              { className: "btn btn-default", onClick: onNewSearch },
              "New Search"
            )
          ),
          _react2["default"].createElement(
            "div",
            { className: "col-sm-10 col-md-10 text-right" },
            _react2["default"].createElement(_currentQuery2["default"], { onChange: onSearchFieldChange, searchFields: activeCollection.query.searchFields })
          )
        ),
        _react2["default"].createElement(
          "span",
          { type: "footer-body" },
          _react2["default"].createElement(_resultsPagination2["default"], { onChange: onPageChange,
            numFound: activeCollection.results.numFound,
            start: activeCollection.query.start || 0,
            rows: activeCollection.query.rows })
        )
      );
    }
  }]);

  return FacetedSearch;
})(_react2["default"].Component);

exports["default"] = FacetedSearch;
module.exports = exports["default"];

},{"../fields/select-field":39,"../page.jsx":41,"./current-query":30,"./results/pagination":32,"./search-fields":33,"./sort-menu":38,"react":"react"}],32:[function(require,module,exports){
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

var Pagination = (function (_React$Component) {
  _inherits(Pagination, _React$Component);

  function Pagination() {
    _classCallCheck(this, Pagination);

    _get(Object.getPrototypeOf(Pagination.prototype), "constructor", this).apply(this, arguments);
  }

  _createClass(Pagination, [{
    key: "onPageChange",
    value: function onPageChange(page, pageAmt) {
      if (page >= pageAmt || page < 0) {
        return;
      }
      this.props.onChange(page);
    }
  }, {
    key: "renderPage",
    value: function renderPage(page, currentPage, key) {
      return _react2["default"].createElement(
        "li",
        { className: (0, _classnames2["default"])({ "active": page === currentPage }), key: key },
        _react2["default"].createElement(
          "a",
          { onClick: this.onPageChange.bind(this, page) },
          page + 1
        )
      );
    }
  }, {
    key: "render",
    value: function render() {
      var _this = this;

      var _props = this.props;
      var numFound = _props.numFound;
      var start = _props.start;
      var rows = _props.rows;

      var pageAmt = Math.ceil(numFound / rows);
      var currentPage = start / rows;

      var rangeStart = currentPage - 2 < 0 ? 0 : currentPage - 2;
      var rangeEnd = rangeStart + 5 > pageAmt ? pageAmt : rangeStart + 5;

      if (rangeEnd - rangeStart < 5 && rangeStart > 0) {
        rangeStart = rangeEnd - 5;
        if (rangeStart < 0) {
          rangeStart = 0;
        }
      }

      var pages = [];
      for (var page = rangeStart; page < rangeEnd; page++) {
        if (pages.indexOf(page) < 0) {
          pages.push(page);
        }
      }

      return _react2["default"].createElement(
        "nav",
        null,
        _react2["default"].createElement(
          "ul",
          { className: "pagination pagination-sm" },
          _react2["default"].createElement(
            "li",
            { className: (0, _classnames2["default"])({ "disabled": currentPage === 0 }), key: "start" },
            _react2["default"].createElement(
              "a",
              { onClick: this.onPageChange.bind(this, 0) },
              "«"
            )
          ),
          pages.map(function (page, idx) {
            return _this.renderPage(page, currentPage, idx);
          }),
          _react2["default"].createElement(
            "li",
            { className: (0, _classnames2["default"])({ "disabled": currentPage === pageAmt - 1 }), key: "end" },
            _react2["default"].createElement(
              "a",
              { onClick: this.onPageChange.bind(this, pageAmt - 1) },
              "»"
            )
          )
        )
      )
      /*      <div className={cx({"panel-body": bootstrapCss, "text-center": bootstrapCss})}>
              <ul className={cx("pagination", {"pagination-sm": bootstrapCss})}>
                <li className={cx({"disabled": currentPage === 0})} key="start">
                  <a onClick={this.onPageChange.bind(this, 0)}>&lt;&lt;</a>
                </li>
                <li className={cx({"disabled": currentPage - 1 < 0})} key="prev">
                  <a onClick={this.onPageChange.bind(this, currentPage - 1)}>&lt;</a>
                </li>
                {pages.map((page, idx) => this.renderPage(page, currentPage, idx))}
                <li className={cx({"disabled": currentPage + 1 >= pageAmt})} key="next">
                  <a onClick={this.onPageChange.bind(this, currentPage + 1, pageAmt)}>&gt;</a>
                </li>
                <li className={cx({"disabled": currentPage === pageAmt - 1})} key="end">
                  <a onClick={this.onPageChange.bind(this, pageAmt - 1)}>&gt;&gt;</a>
                </li>
              </ul>
            </div>*/
      ;
    }
  }]);

  return Pagination;
})(_react2["default"].Component);

Pagination.propTypes = {
  onChange: _react2["default"].PropTypes.func.isRequired,
  numFound: _react2["default"].PropTypes.number.isRequired,
  start: _react2["default"].PropTypes.number.isRequired,
  rows: _react2["default"].PropTypes.number.isRequired
};

exports["default"] = Pagination;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}],33:[function(require,module,exports){
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

var _searchFieldsTextSearch = require("./search-fields/text-search");

var _searchFieldsTextSearch2 = _interopRequireDefault(_searchFieldsTextSearch);

var _searchFieldsListFacet = require("./search-fields/list-facet");

var _searchFieldsListFacet2 = _interopRequireDefault(_searchFieldsListFacet);

var _searchFieldsRangeFacet = require("./search-fields/range-facet");

var _searchFieldsRangeFacet2 = _interopRequireDefault(_searchFieldsRangeFacet);

var components = {
  text: _searchFieldsTextSearch2["default"],
  "list-facet": _searchFieldsListFacet2["default"],
  "range-facet": _searchFieldsRangeFacet2["default"]
};

var SearchFields = (function (_React$Component) {
  _inherits(SearchFields, _React$Component);

  function SearchFields() {
    _classCallCheck(this, SearchFields);

    _get(Object.getPrototypeOf(SearchFields.prototype), "constructor", this).apply(this, arguments);
  }

  _createClass(SearchFields, [{
    key: "render",
    value: function render() {
      var _props = this.props;
      var onSetCollapse = _props.onSetCollapse;
      var onFacetSortChange = _props.onFacetSortChange;
      var onSearchFieldChange = _props.onSearchFieldChange;
      var _props2 = this.props;
      var fields = _props2.fields;
      var results = _props2.results;
      var query = _props2.query;
      var truncateFacetListsAt = _props2.truncateFacetListsAt;

      return _react2["default"].createElement(
        "div",
        { className: "facet-group" },
        fields.map(function (searchField, i) {
          var type = searchField.type;
          var field = searchField.field;

          var SearchComponent = components[type];
          var facets = type === "list-facet" || type === "range-facet" ? results.facets[field] || [] : null;
          return _react2["default"].createElement(SearchComponent, { key: i + "_" + field, facets: facets, onChange: onSearchFieldChange,
            collapse: searchField.collapse,
            onFacetSortChange: onFacetSortChange, onSetCollapse: onSetCollapse,
            query: query, truncateFacetListsAt: truncateFacetListsAt,
            field: searchField.field, label: searchField.label, value: searchField.value });
        })
      );
    }
  }]);

  return SearchFields;
})(_react2["default"].Component);

exports["default"] = SearchFields;
module.exports = exports["default"];

},{"./search-fields/list-facet":34,"./search-fields/range-facet":35,"./search-fields/text-search":37,"react":"react"}],34:[function(require,module,exports){
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

var ListFacet = (function (_React$Component) {
  _inherits(ListFacet, _React$Component);

  function ListFacet(props) {
    _classCallCheck(this, ListFacet);

    _get(Object.getPrototypeOf(ListFacet.prototype), "constructor", this).call(this, props);

    this.state = {
      filter: "",
      truncateFacetListsAt: props.truncateFacetListsAt
    };
  }

  _createClass(ListFacet, [{
    key: "handleClick",
    value: function handleClick(value) {
      var foundIdx = this.props.value.indexOf(value);
      if (foundIdx < 0) {
        this.props.onChange(this.props.field, this.props.value.concat(value));
      } else {
        this.props.onChange(this.props.field, this.props.value.filter(function (v, i) {
          return i !== foundIdx;
        }));
      }
    }
  }, {
    key: "toggleExpand",
    value: function toggleExpand() {
      this.props.onSetCollapse(this.props.field, !(this.props.collapse || false));
    }
  }, {
    key: "render",
    value: function render() {
      var _this = this;

      var _props = this.props;
      var query = _props.query;
      var label = _props.label;
      var facets = _props.facets;
      var field = _props.field;
      var value = _props.value;
      var facetSort = _props.facetSort;
      var collapse = _props.collapse;
      var truncateFacetListsAt = this.state.truncateFacetListsAt;

      var facetCounts = facets.filter(function (facet, i) {
        return i % 2 === 1;
      });
      var facetValues = facets.filter(function (facet, i) {
        return i % 2 === 0;
      });

      var facetSortValue = facetSort ? facetSort : query.facetSort ? query.facetSort : query.facetLimit && query.facetLimit > -1 ? "count" : "index";

      var expanded = !(collapse || false);

      var showMoreLink = truncateFacetListsAt > -1 && truncateFacetListsAt < facetValues.length ? _react2["default"].createElement(
        "a",
        { onClick: function () {
            return _this.setState({ truncateFacetListsAt: -1 });
          } },
        "Show all ",
        facetValues.length,
        " items"
      ) : null;

      return _react2["default"].createElement(
        "div",
        { className: "facet basic-facet" },
        _react2["default"].createElement("span", { onClick: this.toggleExpand.bind(this), style: { cursor: "pointer" },
          className: (0, _classnames2["default"])("glyphicon", "pull-right", "facet-extra", "hi-light-grey", { "glyphicon-collapse-up": !collapse, "glyphicon-collapse-down": collapse }) }),
        _react2["default"].createElement(
          "h2",
          { onClick: this.toggleExpand.bind(this), style: { cursor: "pointer" } },
          label
        ),
        expanded ? _react2["default"].createElement(
          "div",
          null,
          _react2["default"].createElement(
            "div",
            { className: "facet-items-box" },
            facetValues.filter(function (facetValue, i) {
              return truncateFacetListsAt < 0 || i < truncateFacetListsAt;
            }).map(function (facetValue, i) {
              return _this.state.filter.length === 0 || facetValue.toLowerCase().indexOf(_this.state.filter.toLowerCase()) > -1 ? _react2["default"].createElement(
                "div",
                { className: "facet-item downcase-then-capitalize", key: facetValue + "_" + facetCounts[i], onClick: function () {
                    return _this.handleClick(facetValue);
                  } },
                facetValue,
                _react2["default"].createElement(
                  "span",
                  { className: "facet-item-amount" },
                  facetCounts[i]
                ),
                _react2["default"].createElement(
                  "svg",
                  { className: (0, _classnames2["default"])("facet-check-box", { checked: value.indexOf(facetValue) > -1 }), viewBox: "0 0 15 15" },
                  _react2["default"].createElement("circle", { cx: "7.5", cy: "7.5", r: "7" })
                )
              ) : null;
            }),
            showMoreLink
          ),
          facetValues.length > 4 ? _react2["default"].createElement(
            "div",
            { className: "facet-extra-space" },
            _react2["default"].createElement(
              "div",
              { className: "facet-extra" },
              _react2["default"].createElement("span", { style: { cursor: "pointer" }, onClick: function () {
                  return _this.props.onChange(field, []);
                },
                className: "glyphicon glyphicon-remove-sign pull-right hi-light-grey" }),
              _react2["default"].createElement("input", { className: "input-xs", onChange: function (ev) {
                  return _this.setState({ filter: ev.target.value });
                }, placeholder: "Filter in " + label, type: "text", value: this.state.filter }),
              _react2["default"].createElement(
                "span",
                { className: "btn-group" },
                _react2["default"].createElement(
                  "button",
                  { className: (0, _classnames2["default"])("btn", "btn-default", "btn-xs", { "active": facetSortValue === "index" }),
                    onClick: function () {
                      return _this.props.onFacetSortChange(field, "index");
                    } },
                  "a-z"
                ),
                _react2["default"].createElement(
                  "button",
                  { className: (0, _classnames2["default"])("btn", "btn-default", "btn-xs", { "active": facetSortValue === "count" }),
                    onClick: function () {
                      return _this.props.onFacetSortChange(field, "count");
                    } },
                  "0-9"
                )
              )
            )
          ) : null
        ) : null
      );
    }
  }]);

  return ListFacet;
})(_react2["default"].Component);

ListFacet.defaultProps = {
  value: [],
  truncateFacetListsAt: -1
};

ListFacet.propTypes = {
  bootstrapCss: _react2["default"].PropTypes.bool,
  children: _react2["default"].PropTypes.array,
  collapse: _react2["default"].PropTypes.bool,
  facetSort: _react2["default"].PropTypes.string,
  facets: _react2["default"].PropTypes.array.isRequired,
  field: _react2["default"].PropTypes.string.isRequired,
  label: _react2["default"].PropTypes.string,
  onChange: _react2["default"].PropTypes.func,
  onFacetSortChange: _react2["default"].PropTypes.func,
  onSetCollapse: _react2["default"].PropTypes.func,
  query: _react2["default"].PropTypes.object.isRequired,
  truncateFacetListsAt: _react2["default"].PropTypes.number,
  value: _react2["default"].PropTypes.array
};

exports["default"] = ListFacet;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}],35:[function(require,module,exports){
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

var _rangeSlider = require("./range-slider");

var _rangeSlider2 = _interopRequireDefault(_rangeSlider);

var RangeFacet = (function (_React$Component) {
  _inherits(RangeFacet, _React$Component);

  function RangeFacet(props) {
    _classCallCheck(this, RangeFacet);

    _get(Object.getPrototypeOf(RangeFacet.prototype), "constructor", this).call(this, props);

    this.state = {
      value: props.value
    };
  }

  _createClass(RangeFacet, [{
    key: "componentWillReceiveProps",
    value: function componentWillReceiveProps(nextProps) {
      this.setState({ value: nextProps.value });
    }
  }, {
    key: "facetsToRange",
    value: function facetsToRange() {
      var facets = this.props.facets;

      return facets.filter(function (facet, i) {
        return i % 2 === 0;
      }).map(function (v) {
        return parseInt(v);
      }).sort(function (a, b) {
        return a > b ? 1 : -1;
      }).filter(function (a, i, me) {
        return i === 0 || i === me.length - 1;
      });
    }
  }, {
    key: "onRangeChange",
    value: function onRangeChange(range) {
      var bounds = this.facetsToRange();
      var lowerBound = bounds[0];
      var upperBound = bounds[1];
      var realRange = upperBound - lowerBound;

      var newState = {
        value: [Math.floor(range.lowerLimit * realRange) + lowerBound, Math.ceil(range.upperLimit * realRange) + lowerBound]
      };

      if (range.refresh) {
        this.props.onChange(this.props.field, newState.value);
      } else {
        this.setState(newState);
      }
    }
  }, {
    key: "getPercentage",
    value: function getPercentage(range, value) {
      var lowerBound = range[0];
      var upperBound = range[1];
      var realRange = upperBound - lowerBound;

      var atRange = value - lowerBound;
      return atRange / realRange;
    }
  }, {
    key: "toggleExpand",
    value: function toggleExpand(ev) {
      if (ev.target.className.indexOf("clear-button") < 0) {
        this.props.onSetCollapse(this.props.field, !(this.props.collapse || false));
      }
    }
  }, {
    key: "render",
    value: function render() {
      var _props = this.props;
      var label = _props.label;
      var collapse = _props.collapse;
      var value = this.state.value;

      var range = this.facetsToRange();

      var filterRange = value.length > 0 ? value : range;

      return _react2["default"].createElement(
        "div",
        { className: "facet basic-facet" },
        _react2["default"].createElement("span", { onClick: this.toggleExpand.bind(this), style: { cursor: "pointer" },
          className: (0, _classnames2["default"])("glyphicon", "pull-right", "facet-extra", "hi-light-grey", { "glyphicon-collapse-up": !collapse, "glyphicon-collapse-down": collapse }) }),
        _react2["default"].createElement(
          "h2",
          { onClick: this.toggleExpand.bind(this), style: { cursor: "pointer" } },
          label
        ),
        _react2["default"].createElement(
          "div",
          { style: { display: collapse ? "none" : "block" } },
          _react2["default"].createElement(_rangeSlider2["default"], { lowerLimit: this.getPercentage(range, filterRange[0]), onChange: this.onRangeChange.bind(this), upperLimit: this.getPercentage(range, filterRange[1]) }),
          _react2["default"].createElement(
            "span",
            null,
            filterRange[0]
          ),
          _react2["default"].createElement(
            "span",
            { className: "pull-right" },
            filterRange[1]
          )
        )
      );
    }
  }]);

  return RangeFacet;
})(_react2["default"].Component);

RangeFacet.defaultProps = {
  value: []
};

RangeFacet.propTypes = {
  collapse: _react2["default"].PropTypes.bool,
  facets: _react2["default"].PropTypes.array.isRequired,
  field: _react2["default"].PropTypes.string.isRequired,
  label: _react2["default"].PropTypes.string,
  onChange: _react2["default"].PropTypes.func,
  onSetCollapse: _react2["default"].PropTypes.func,
  value: _react2["default"].PropTypes.array
};

exports["default"] = RangeFacet;
module.exports = exports["default"];

},{"./range-slider":36,"classnames":"classnames","react":"react"}],36:[function(require,module,exports){
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

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var MOUSE_DOWN = 0;
var MOUSE_UP = 1;

var styles = {
  slider: {
    "MozUserSelect": "none",
    "WebkitUserSelect": "none",
    "MsUserSelect": "none",
    "UserSelect": "none",
    "WebkitUserDrag": "none",
    "userDrag": "none",
    "cursor": "pointer",
    width: "100%",
    stroke: "#f1ebe6",
    fill: "#f1ebe6"
  }
};

var RangeSlider = (function (_React$Component) {
  _inherits(RangeSlider, _React$Component);

  function RangeSlider(props) {
    _classCallCheck(this, RangeSlider);

    _get(Object.getPrototypeOf(RangeSlider.prototype), "constructor", this).call(this, props);
    this.mouseState = MOUSE_UP;
    this.mouseUpListener = this.onMouseUp.bind(this);
    this.mouseMoveListener = this.onMouseMove.bind(this);
    this.touchMoveListener = this.onTouchMove.bind(this);

    this.state = _extends({}, this.propsToState(this.props), { hoverState: null });
  }

  _createClass(RangeSlider, [{
    key: "componentDidMount",
    value: function componentDidMount() {
      window.addEventListener("mouseup", this.mouseUpListener);
      window.addEventListener("mousemove", this.mouseMoveListener);
      window.addEventListener("touchend", this.mouseUpListener);
      window.addEventListener("touchmove", this.touchMoveListener);
    }
  }, {
    key: "componentWillReceiveProps",
    value: function componentWillReceiveProps(nextProps) {
      this.setState(this.propsToState(nextProps));
    }
  }, {
    key: "componentWillUnmount",
    value: function componentWillUnmount() {
      window.removeEventListener("mouseup", this.mouseUpListener);
      window.removeEventListener("mousemove", this.mouseMoveListener);
      window.removeEventListener("touchend", this.mouseUpListener);
      window.removeEventListener("touchmove", this.touchMoveListener);
    }
  }, {
    key: "propsToState",
    value: function propsToState(props) {
      var lowerLimit = props.lowerLimit || 0;
      var upperLimit = props.upperLimit || 1;
      return {
        lowerLimit: lowerLimit,
        upperLimit: upperLimit
      };
    }
  }, {
    key: "getPositionForLimit",
    value: function getPositionForLimit(pageX) {
      var rect = _reactDom2["default"].findDOMNode(this).getBoundingClientRect();
      if (rect.width > 0) {
        var percentage = (pageX - rect.left) / rect.width;
        if (percentage > 1) {
          percentage = 1;
        } else if (percentage < 0) {
          percentage = 0;
        }

        if (this.state.hoverState === "lowerLimit") {
          if (percentage >= this.state.upperLimit) {
            percentage = this.state.upperLimit;
          }
          return { lowerLimit: percentage };
        } else if (this.state.hoverState === "upperLimit") {
          if (percentage <= this.state.lowerLimit) {
            percentage = this.state.lowerLimit;
          }
          return { upperLimit: percentage };
        }
      }
      return null;
    }
  }, {
    key: "setRange",
    value: function setRange(pageX) {
      var posForLim = this.getPositionForLimit(pageX);
      if (posForLim !== null) {
        this.setState(posForLim);
        this.props.onChange(_extends({}, this.state, { refresh: false }));
      }
    }
  }, {
    key: "onMouseDown",
    value: function onMouseDown(target, ev) {
      this.mouseState = MOUSE_DOWN;
      this.setState({ hoverState: target });
      return ev.preventDefault();
    }
  }, {
    key: "onMouseMove",
    value: function onMouseMove(ev) {
      if (this.mouseState === MOUSE_DOWN) {
        this.setRange(ev.pageX);
        return ev.preventDefault();
      }
    }
  }, {
    key: "onTouchMove",
    value: function onTouchMove(ev) {
      if (this.mouseState === MOUSE_DOWN) {
        this.setRange(ev.touches[0].pageX);
        return ev.preventDefault();
      }
    }
  }, {
    key: "onMouseUp",
    value: function onMouseUp() {
      if (this.mouseState === MOUSE_DOWN) {
        this.props.onChange(_extends({}, this.state, { refresh: true }));
      }
      this.setState({ hoverState: null });
      this.mouseState = MOUSE_UP;
    }
  }, {
    key: "getRangePath",
    value: function getRangePath() {
      return "M" + (8 + Math.floor(this.state.lowerLimit * 400)) + " 13 L " + (Math.ceil(this.state.upperLimit * 400) - 8) + " 13 Z";
    }
  }, {
    key: "getRangeCircle",
    value: function getRangeCircle(key) {
      var percentage = this.state[key];
      return _react2["default"].createElement("circle", {
        className: this.state.hoverState === key ? "hovering" : "",
        cx: percentage * 400, cy: "13",
        onMouseDown: this.onMouseDown.bind(this, key),
        onTouchStart: this.onMouseDown.bind(this, key),
        r: "13" });
    }
  }, {
    key: "render",
    value: function render() {
      var keys = this.state.hoverState === "lowerLimit" ? ["upperLimit", "lowerLimit"] : ["lowerLimit", "upperLimit"];
      return _react2["default"].createElement(
        "svg",
        { className: "facet-range-slider", viewBox: "0 0 400 26" },
        _react2["default"].createElement("path", { d: "M0 0 L 0 26 Z", fill: "transparent" }),
        _react2["default"].createElement("path", { d: "M400 0 L 400 26 Z", fill: "transparent" }),
        _react2["default"].createElement("path", { d: "M0 13 L 400 13 Z", fill: "transparent" }),
        _react2["default"].createElement(
          "g",
          { className: "range-line" },
          _react2["default"].createElement("path", {
            className: this.state.hoverState === "bar" ? "hovering" : "",
            d: this.getRangePath()
          }),
          this.getRangeCircle(keys[0]),
          this.getRangeCircle(keys[1])
        )
      );
    }
  }]);

  return RangeSlider;
})(_react2["default"].Component);

RangeSlider.propTypes = {
  lowerLimit: _react2["default"].PropTypes.number,
  onChange: _react2["default"].PropTypes.func.isRequired,
  upperLimit: _react2["default"].PropTypes.number
};

exports["default"] = RangeSlider;
module.exports = exports["default"];

},{"react":"react","react-dom":"react-dom"}],37:[function(require,module,exports){
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

var TextSearch = (function (_React$Component) {
  _inherits(TextSearch, _React$Component);

  function TextSearch(props) {
    _classCallCheck(this, TextSearch);

    _get(Object.getPrototypeOf(TextSearch.prototype), "constructor", this).call(this, props);

    this.state = {
      value: props.value || ""
    };
  }

  _createClass(TextSearch, [{
    key: "componentWillReceiveProps",
    value: function componentWillReceiveProps(nextProps) {
      this.setState({
        value: nextProps.value
      });
    }
  }, {
    key: "handleInputChange",
    value: function handleInputChange(ev) {
      this.setState({
        value: ev.target.value
      });
    }
  }, {
    key: "handleInputKeyDown",
    value: function handleInputKeyDown(ev) {
      if (ev.keyCode === 13) {
        this.handleSubmit();
      }
    }
  }, {
    key: "handleSubmit",
    value: function handleSubmit() {
      this.props.onChange(this.props.field, this.state.value);
    }
  }, {
    key: "render",
    value: function render() {
      var label = this.props.label;

      return _react2["default"].createElement(
        "div",
        { className: "facet" },
        _react2["default"].createElement(
          "div",
          { className: "input-group" },
          _react2["default"].createElement("input", { className: "form-control",
            placeholder: label || "",
            onChange: this.handleInputChange.bind(this),
            onKeyDown: this.handleInputKeyDown.bind(this),
            value: this.state.value || "" }),
          _react2["default"].createElement(
            "span",
            { className: "input-group-btn" },
            _react2["default"].createElement(
              "button",
              { className: "btn btn-default", type: "button", onClick: this.handleSubmit.bind(this) },
              _react2["default"].createElement("span", { className: "glyphicon glyphicon-search" })
            )
          )
        )
      );
    }
  }]);

  return TextSearch;
})(_react2["default"].Component);

TextSearch.defaultProps = {
  field: null
};

TextSearch.propTypes = {
  field: _react2["default"].PropTypes.string.isRequired,
  label: _react2["default"].PropTypes.string,
  onChange: _react2["default"].PropTypes.func
};

exports["default"] = TextSearch;
module.exports = exports["default"];

},{"react":"react"}],38:[function(require,module,exports){
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

var _fieldsSelectField = require("../fields/select-field");

var _fieldsSelectField2 = _interopRequireDefault(_fieldsSelectField);

var SortMenu = (function (_React$Component) {
  _inherits(SortMenu, _React$Component);

  function SortMenu() {
    _classCallCheck(this, SortMenu);

    _get(Object.getPrototypeOf(SortMenu.prototype), "constructor", this).apply(this, arguments);
  }

  _createClass(SortMenu, [{
    key: "render",
    value: function render() {
      var _this = this;

      var _props = this.props;
      var sortFields = _props.sortFields;
      var onChange = _props.onChange;

      if (sortFields.length === 0) {
        return null;
      }

      var value = sortFields.find(function (sf) {
        return sf.value;
      });

      return _react2["default"].createElement(
        "div",
        { className: "pull-right" },
        value ? _react2["default"].createElement(
          "span",
          { className: "pull-right btn-group" },
          _react2["default"].createElement(
            "button",
            { className: (0, _classnames2["default"])("btn", "btn-default", { active: value.value === "asc" }),
              onClick: function () {
                return _this.props.onChange(value.field, "asc");
              } },
            "asc"
          ),
          _react2["default"].createElement(
            "button",
            { className: (0, _classnames2["default"])("btn", "btn-default", { active: value.value === "desc" }),
              onClick: function () {
                return _this.props.onChange(value.field, "desc");
              } },
            "desc"
          )
        ) : null,
        _react2["default"].createElement(
          "span",
          { className: "pull-right" },
          _react2["default"].createElement(
            _fieldsSelectField2["default"],
            { btnClass: "btn-blank", onChange: function (sortField) {
                return onChange(sortField, "asc");
              },
              onClear: function () {
                return onChange(value.field, null);
              }, value: value ? value.field : null },
            _react2["default"].createElement(
              "span",
              { type: "placeholder" },
              "Order"
            ),
            sortFields.map(function (sortField) {
              return _react2["default"].createElement(
                "span",
                { key: sortField.field, value: sortField.field },
                sortField.label
              );
            })
          )
        )
      );
    }
  }]);

  return SortMenu;
})(_react2["default"].Component);

SortMenu.defaultProps = {
  sortFields: []
};

SortMenu.propTypes = {
  onChange: _react2["default"].PropTypes.func,
  sortFields: _react2["default"].PropTypes.array
};

exports["default"] = SortMenu;
module.exports = exports["default"];

},{"../fields/select-field":39,"classnames":"classnames","react":"react"}],39:[function(require,module,exports){
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

},{"classnames":"classnames","react":"react","react-dom":"react-dom"}],40:[function(require,module,exports){
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
    _react2["default"].createElement("img", { className: "hi-logo", src: "/logo-huygens-ing.svg" })
  );

  var clariahLogo = _react2["default"].createElement(
    "div",
    { className: "col-sm-1 col-md-1" },
    _react2["default"].createElement("img", { className: "logo", src: "/logo-clariah.svg" })
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

},{"react":"react"}],41:[function(require,module,exports){
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
              _react2["default"].createElement("img", { src: "/logo-timbuctoo.svg", className: "logo", alt: "timbuctoo" })
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

},{"./footer":40,"react":"react"}],42:[function(require,module,exports){
"use strict";

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; })();

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _storeStore = require("./store/store");

var _storeStore2 = _interopRequireDefault(_storeStore);

var _actionsMetadata = require("./actions/metadata");

var _actionsSolr = require("./actions/solr");

var _router = require("./router");

var _router2 = _interopRequireDefault(_router);

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
}

document.addEventListener("DOMContentLoaded", function () {
	var afterInit = function afterInit() {
		return _reactDom2["default"].render(_router2["default"], document.getElementById("app"));
	};
	var checkForIndex = function checkForIndex() {
		return _storeStore2["default"].dispatch((0, _actionsSolr.checkIndex)(afterInit));
	};
	_storeStore2["default"].dispatch((0, _actionsMetadata.setVre)(getVreId(), checkForIndex));
});

},{"./actions/metadata":27,"./actions/solr":28,"./router":43,"./store/store":46,"react-dom":"react-dom"}],43:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports.navigateTo = navigateTo;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _storeStore = require("./store/store");

var _storeStore2 = _interopRequireDefault(_storeStore);

var _reactRouter = require("react-router");

var _reactRedux = require("react-redux");

var _componentsApp = require("./components/app");

var _componentsApp2 = _interopRequireDefault(_componentsApp);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var urls = {
	root: function root() {
		return "/";
	}
};

exports.urls = urls;

function navigateTo(key, args) {
	_reactRouter.browserHistory.push(urls[key].apply(null, args));
}

var makeContainerComponent = (0, _reactRedux.connect)(function (state) {
	return state;
}, function (dispatch) {
	return (0, _actions2["default"])(navigateTo, dispatch);
});

var router = _react2["default"].createElement(
	_reactRedux.Provider,
	{ store: _storeStore2["default"] },
	_react2["default"].createElement(
		_reactRouter.Router,
		{ history: _reactRouter.browserHistory },
		_react2["default"].createElement(_reactRouter.Route, { path: urls.root(true), component: makeContainerComponent(_componentsApp2["default"]) })
	)
);

exports["default"] = router;

},{"./actions":26,"./components/app":29,"./store/store":46,"react":"react","react-redux":"react-redux","react-router":"react-router"}],44:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	vreId: null,
	list: [],
	collections: {},
	archetypeCollections: {},
	domain: null
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_VRE":
			return _extends({}, state, {
				vreId: action.vreId,
				collections: action.collections || null
			});

		case "SET_ARCHETYPES":
			return _extends({}, state, {
				archetypeCollections: action.collections || null
			});

		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],45:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	indexPresent: false,
	indexesPending: false,
	searchStates: {}
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_INDEX_PRESENT":
			return _extends({}, state, {
				indexPresent: action.present,
				indexesPending: false
			});
		case "INDEXES_PENDING":
			return _extends({}, state, {
				indexesPending: true
			});

		case "SET_SEARCH_STATE":
			var newState = _extends({}, state);
			newState.searchStates[action.collectionName] = action.searchState;
			return newState;
		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],46:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _redux = require("redux");

var _reduxThunk = require("redux-thunk");

var _reduxThunk2 = _interopRequireDefault(_reduxThunk);

var _metadataReducer = require("./metadata-reducer");

var _metadataReducer2 = _interopRequireDefault(_metadataReducer);

var _solrReducer = require("./solr-reducer");

var _solrReducer2 = _interopRequireDefault(_solrReducer);

var reducers = {
	metadata: _metadataReducer2["default"],
	solr: _solrReducer2["default"]
};

var data = (0, _redux.combineReducers)(reducers);

var store = (0, _redux.createStore)(data, {}, (0, _redux.applyMiddleware)(_reduxThunk2["default"]), window.devToolsExtension ? window.devToolsExtension() : function (f) {
	return f;
});

exports["default"] = store;
module.exports = exports["default"];

},{"./metadata-reducer":44,"./solr-reducer":45,"redux":16,"redux-thunk":10}]},{},[42])(42)
});