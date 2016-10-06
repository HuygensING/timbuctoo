(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.ExcelImportMock = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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
},{"for-each":1,"trim":5}],5:[function(require,module,exports){

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

},{}],6:[function(require,module,exports){
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

},{"global/window":2,"is-function":3,"parse-headers":4,"xtend":7}],7:[function(require,module,exports){
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

},{}],8:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var App = function (_React$Component) {
	_inherits(App, _React$Component);

	function App(props) {
		_classCallCheck(this, App);

		var _this = _possibleConstructorReturn(this, (App.__proto__ || Object.getPrototypeOf(App)).call(this, props));

		_this.state = {
			linkLines: [],
			width: window.innerWidth
		};

		return _this;
	}

	_createClass(App, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			var _this2 = this;

			window.addEventListener("resize", function () {
				_this2.setState({ width: window.innerWidth }, function () {
					return _this2.generateLines();
				});
			});
			this.generateLines();
		}
	}, {
		key: "generateLines",
		value: function generateLines() {
			var _this3 = this;

			var links = this.props.links;

			var linkLines = links.map(function (link) {
				var sourceRect = _this3.refs[link.source].getBoundingClientRect();
				var targetRect = _this3.refs[link.target].getBoundingClientRect();
				return {
					source: { x: sourceRect.x + sourceRect.width / 2, y: sourceRect.y + sourceRect.height / 2 },
					target: { x: targetRect.x + targetRect.width / 2, y: targetRect.y + targetRect.height / 2 },
					type: link.type
				};
			});
			this.setState({ linkLines: linkLines });
		}
	}, {
		key: "render",
		value: function render() {
			var datasets = this.props.datasets;
			var _state = this.state;
			var linkLines = _state.linkLines;
			var width = _state.width;


			var height = 10000;
			var interval = (width - 80) / datasets.length;
			return _react2.default.createElement(
				"svg",
				{ style: { position: "absolute", top: 0, left: 0 },
					width: width, height: height, viewBox: "0 0 " + width + " " + height },
				linkLines.map(function (linkLine, idx) {
					return _react2.default.createElement(
						"g",
						{ key: idx },
						_react2.default.createElement("line", { x1: linkLine.source.x, y1: linkLine.source.y,
							x2: linkLine.target.x, y2: linkLine.target.y,
							strokeWidth: 0.5, stroke: "black" }),
						linkLine.type !== "isInCollection" && linkLine.type !== "hasCollection" ? _react2.default.createElement(
							"g",
							{ transform: "translate(" + ((linkLine.source.x + linkLine.target.x) / 2 - 50) + ", " + (linkLine.source.y + linkLine.target.y) / 2 + ")" },
							_react2.default.createElement(
								"text",
								null,
								linkLine.type
							)
						) : null
					);
				}),
				datasets.map(function (dataset, idx) {
					var collectionInterval = interval / dataset.collections.length;
					return _react2.default.createElement(
						"g",
						{ key: dataset.label, transform: "translate(" + (0.5 * interval + idx * interval) + ", 10)" },
						_react2.default.createElement("circle", { r: 10, ref: dataset.id }),
						_react2.default.createElement(
							"g",
							{ transform: "translate(12, 6)" },
							_react2.default.createElement(
								"text",
								null,
								"Dataset: ",
								dataset.label
							)
						),
						_react2.default.createElement(
							"g",
							{ transform: "translate(" + -0.5 * interval + ", 100)" },
							dataset.collections.map(function (collection, idx) {
								return _react2.default.createElement(
									"g",
									{ key: collection.label, transform: "translate(" + (0.5 * collectionInterval + collectionInterval * idx) + ", 0)" },
									_react2.default.createElement("circle", { r: 10, ref: collection.id }),
									_react2.default.createElement(
										"g",
										{ transform: "translate(12, 6)" },
										_react2.default.createElement(
											"text",
											null,
											"Collection: ",
											collection.label
										)
									),
									collection.entities.map(function (entity, idx) {
										return _react2.default.createElement(
											"g",
											{ key: entity.id, transform: "translate(0, " + (idx + 1) * 30 + ")" },
											_react2.default.createElement("circle", { r: 5, ref: entity.id }),
											_react2.default.createElement(
												"g",
												{ transform: "translate(5, 2)" },
												_react2.default.createElement(
													"text",
													null,
													entity.label
												)
											)
										);
									})
								);
							})
						)
					);
				})
			);
		}
	}]);

	return App;
}(_react2.default.Component);

exports.default = App;

},{"react":"react","react-dom":"react-dom"}],9:[function(require,module,exports){
"use strict";

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _app = require("./components/app");

var _app2 = _interopRequireDefault(_app);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

document.addEventListener("DOMContentLoaded", function () {
	(0, _xhr2.default)("" + "/v2.1/gremlin/datasets", function (err, resp, body) {
		var data = JSON.parse(body);
		var nodes = data.nodes.map(function (node, id) {
			return _extends({}, node, { id: id });
		});
		var links = data.links.map(function (link, id) {
			return _extends({}, link, { id: id });
		});
		var datasets = nodes.filter(function (node) {
			return node.type === "dataset";
		}).map(function (node) {
			return _extends({}, node, {
				collections: links.filter(function (link) {
					return link.source === node.id && link.type === "hasCollection";
				}).map(function (link) {
					return _extends({}, nodes.find(function (node) {
						return node.id === link.target;
					}), {
						entities: links.filter(function (link1) {
							return link1.target === link.target && link1.type === "isInCollection";
						}).map(function (link1) {
							return nodes.find(function (node1) {
								return node1.id === link1.source;
							});
						})
					});
				})
			});
		});

		_reactDom2.default.render(_react2.default.createElement(_app2.default, { datasets: datasets, links: links }), document.getElementById("app"));
	});
});

},{"./components/app":8,"react":"react","react-dom":"react-dom","xhr":6}]},{},[9])(9)
});
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvZm9yLWVhY2gvaW5kZXguanMiLCJub2RlX21vZHVsZXMvZ2xvYmFsL3dpbmRvdy5qcyIsIm5vZGVfbW9kdWxlcy9pcy1mdW5jdGlvbi9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy9wYXJzZS1oZWFkZXJzL3BhcnNlLWhlYWRlcnMuanMiLCJub2RlX21vZHVsZXMvdHJpbS9pbmRleC5qcyIsIm5vZGVfbW9kdWxlcy94aHIvaW5kZXguanMiLCJub2RlX21vZHVsZXMveHRlbmQvaW1tdXRhYmxlLmpzIiwic3JjL2NvbXBvbmVudHMvYXBwLmpzIiwic3JjL2luZGV4LmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FDQUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FDOUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDVEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDZkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNkQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7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LHVCQUFtQixJQUFuQixJQUF5QixJQUFJLEVBQTdCO0FBQUEsR0FBZixDQUFkO0FBQ0EsTUFBTSxXQUFXLE1BQ2YsTUFEZSxDQUNSLFVBQUMsSUFBRDtBQUFBLFVBQVUsS0FBSyxJQUFMLEtBQWMsU0FBeEI7QUFBQSxHQURRLEVBRWYsR0FGZSxDQUVYLFVBQUMsSUFBRDtBQUFBLHVCQUNELElBREM7QUFFSixpQkFBYSxNQUNYLE1BRFcsQ0FDSixVQUFDLElBQUQ7QUFBQSxZQUFVLEtBQUssTUFBTCxLQUFnQixLQUFLLEVBQXJCLElBQTJCLEtBQUssSUFBTCxLQUFjLGVBQW5EO0FBQUEsS0FESSxFQUVYLEdBRlcsQ0FFUCxVQUFDLElBQUQsRUFBVTtBQUFFLHlCQUNiLE1BQU0sSUFBTixDQUFXLFVBQUMsSUFBRDtBQUFBLGFBQVUsS0FBSyxFQUFMLEtBQVksS0FBSyxNQUEzQjtBQUFBLE1BQVgsQ0FEYTtBQUVoQixnQkFBVSxNQUNSLE1BRFEsQ0FDRCxVQUFDLEtBQUQ7QUFBQSxjQUFXLE1BQU0sTUFBTixLQUFpQixLQUFLLE1BQXRCLElBQWdDLE1BQU0sSUFBTixLQUFjLGdCQUF6RDtBQUFBLE9BREMsRUFFUixHQUZRLENBRUosVUFBQyxLQUFEO0FBQUEsY0FBVyxNQUFNLElBQU4sQ0FBVyxVQUFDLEtBQUQ7QUFBQSxlQUFXLE1BQU0sRUFBTixLQUFhLE1BQU0sTUFBOUI7QUFBQSxRQUFYLENBQVg7QUFBQSxPQUZJO0FBRk07QUFLZCxLQVBTO0FBRlQ7QUFBQSxHQUZXLENBQWpCOztBQWNBLHFCQUFTLE1BQVQsQ0FBZ0IsK0NBQUssVUFBVSxRQUFmLEVBQXlCLE9BQU8sS0FBaEMsR0FBaEIsRUFBMkQsU0FBUyxjQUFULENBQXdCLEtBQXhCLENBQTNEO0FBQ0EsRUFuQkQ7QUFvQkEsQ0FyQkQiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwidmFyIGlzRnVuY3Rpb24gPSByZXF1aXJlKCdpcy1mdW5jdGlvbicpXG5cbm1vZHVsZS5leHBvcnRzID0gZm9yRWFjaFxuXG52YXIgdG9TdHJpbmcgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nXG52YXIgaGFzT3duUHJvcGVydHkgPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5XG5cbmZ1bmN0aW9uIGZvckVhY2gobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBpZiAoIWlzRnVuY3Rpb24oaXRlcmF0b3IpKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ2l0ZXJhdG9yIG11c3QgYmUgYSBmdW5jdGlvbicpXG4gICAgfVxuXG4gICAgaWYgKGFyZ3VtZW50cy5sZW5ndGggPCAzKSB7XG4gICAgICAgIGNvbnRleHQgPSB0aGlzXG4gICAgfVxuICAgIFxuICAgIGlmICh0b1N0cmluZy5jYWxsKGxpc3QpID09PSAnW29iamVjdCBBcnJheV0nKVxuICAgICAgICBmb3JFYWNoQXJyYXkobGlzdCwgaXRlcmF0b3IsIGNvbnRleHQpXG4gICAgZWxzZSBpZiAodHlwZW9mIGxpc3QgPT09ICdzdHJpbmcnKVxuICAgICAgICBmb3JFYWNoU3RyaW5nKGxpc3QsIGl0ZXJhdG9yLCBjb250ZXh0KVxuICAgIGVsc2VcbiAgICAgICAgZm9yRWFjaE9iamVjdChsaXN0LCBpdGVyYXRvciwgY29udGV4dClcbn1cblxuZnVuY3Rpb24gZm9yRWFjaEFycmF5KGFycmF5LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBhcnJheS5sZW5ndGg7IGkgPCBsZW47IGkrKykge1xuICAgICAgICBpZiAoaGFzT3duUHJvcGVydHkuY2FsbChhcnJheSwgaSkpIHtcbiAgICAgICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgYXJyYXlbaV0sIGksIGFycmF5KVxuICAgICAgICB9XG4gICAgfVxufVxuXG5mdW5jdGlvbiBmb3JFYWNoU3RyaW5nKHN0cmluZywgaXRlcmF0b3IsIGNvbnRleHQpIHtcbiAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gc3RyaW5nLmxlbmd0aDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgIC8vIG5vIHN1Y2ggdGhpbmcgYXMgYSBzcGFyc2Ugc3RyaW5nLlxuICAgICAgICBpdGVyYXRvci5jYWxsKGNvbnRleHQsIHN0cmluZy5jaGFyQXQoaSksIGksIHN0cmluZylcbiAgICB9XG59XG5cbmZ1bmN0aW9uIGZvckVhY2hPYmplY3Qob2JqZWN0LCBpdGVyYXRvciwgY29udGV4dCkge1xuICAgIGZvciAodmFyIGsgaW4gb2JqZWN0KSB7XG4gICAgICAgIGlmIChoYXNPd25Qcm9wZXJ0eS5jYWxsKG9iamVjdCwgaykpIHtcbiAgICAgICAgICAgIGl0ZXJhdG9yLmNhbGwoY29udGV4dCwgb2JqZWN0W2tdLCBrLCBvYmplY3QpXG4gICAgICAgIH1cbiAgICB9XG59XG4iLCJpZiAodHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgIG1vZHVsZS5leHBvcnRzID0gd2luZG93O1xufSBlbHNlIGlmICh0eXBlb2YgZ2xvYmFsICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSBnbG9iYWw7XG59IGVsc2UgaWYgKHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiKXtcbiAgICBtb2R1bGUuZXhwb3J0cyA9IHNlbGY7XG59IGVsc2Uge1xuICAgIG1vZHVsZS5leHBvcnRzID0ge307XG59XG4iLCJtb2R1bGUuZXhwb3J0cyA9IGlzRnVuY3Rpb25cblxudmFyIHRvU3RyaW5nID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZ1xuXG5mdW5jdGlvbiBpc0Z1bmN0aW9uIChmbikge1xuICB2YXIgc3RyaW5nID0gdG9TdHJpbmcuY2FsbChmbilcbiAgcmV0dXJuIHN0cmluZyA9PT0gJ1tvYmplY3QgRnVuY3Rpb25dJyB8fFxuICAgICh0eXBlb2YgZm4gPT09ICdmdW5jdGlvbicgJiYgc3RyaW5nICE9PSAnW29iamVjdCBSZWdFeHBdJykgfHxcbiAgICAodHlwZW9mIHdpbmRvdyAhPT0gJ3VuZGVmaW5lZCcgJiZcbiAgICAgLy8gSUU4IGFuZCBiZWxvd1xuICAgICAoZm4gPT09IHdpbmRvdy5zZXRUaW1lb3V0IHx8XG4gICAgICBmbiA9PT0gd2luZG93LmFsZXJ0IHx8XG4gICAgICBmbiA9PT0gd2luZG93LmNvbmZpcm0gfHxcbiAgICAgIGZuID09PSB3aW5kb3cucHJvbXB0KSlcbn07XG4iLCJ2YXIgdHJpbSA9IHJlcXVpcmUoJ3RyaW0nKVxuICAsIGZvckVhY2ggPSByZXF1aXJlKCdmb3ItZWFjaCcpXG4gICwgaXNBcnJheSA9IGZ1bmN0aW9uKGFyZykge1xuICAgICAgcmV0dXJuIE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbChhcmcpID09PSAnW29iamVjdCBBcnJheV0nO1xuICAgIH1cblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbiAoaGVhZGVycykge1xuICBpZiAoIWhlYWRlcnMpXG4gICAgcmV0dXJuIHt9XG5cbiAgdmFyIHJlc3VsdCA9IHt9XG5cbiAgZm9yRWFjaChcbiAgICAgIHRyaW0oaGVhZGVycykuc3BsaXQoJ1xcbicpXG4gICAgLCBmdW5jdGlvbiAocm93KSB7XG4gICAgICAgIHZhciBpbmRleCA9IHJvdy5pbmRleE9mKCc6JylcbiAgICAgICAgICAsIGtleSA9IHRyaW0ocm93LnNsaWNlKDAsIGluZGV4KSkudG9Mb3dlckNhc2UoKVxuICAgICAgICAgICwgdmFsdWUgPSB0cmltKHJvdy5zbGljZShpbmRleCArIDEpKVxuXG4gICAgICAgIGlmICh0eXBlb2YocmVzdWx0W2tleV0pID09PSAndW5kZWZpbmVkJykge1xuICAgICAgICAgIHJlc3VsdFtrZXldID0gdmFsdWVcbiAgICAgICAgfSBlbHNlIGlmIChpc0FycmF5KHJlc3VsdFtrZXldKSkge1xuICAgICAgICAgIHJlc3VsdFtrZXldLnB1c2godmFsdWUpXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgcmVzdWx0W2tleV0gPSBbIHJlc3VsdFtrZXldLCB2YWx1ZSBdXG4gICAgICAgIH1cbiAgICAgIH1cbiAgKVxuXG4gIHJldHVybiByZXN1bHRcbn0iLCJcbmV4cG9ydHMgPSBtb2R1bGUuZXhwb3J0cyA9IHRyaW07XG5cbmZ1bmN0aW9uIHRyaW0oc3RyKXtcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9eXFxzKnxcXHMqJC9nLCAnJyk7XG59XG5cbmV4cG9ydHMubGVmdCA9IGZ1bmN0aW9uKHN0cil7XG4gIHJldHVybiBzdHIucmVwbGFjZSgvXlxccyovLCAnJyk7XG59O1xuXG5leHBvcnRzLnJpZ2h0ID0gZnVuY3Rpb24oc3RyKXtcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9cXHMqJC8sICcnKTtcbn07XG4iLCJcInVzZSBzdHJpY3RcIjtcbnZhciB3aW5kb3cgPSByZXF1aXJlKFwiZ2xvYmFsL3dpbmRvd1wiKVxudmFyIGlzRnVuY3Rpb24gPSByZXF1aXJlKFwiaXMtZnVuY3Rpb25cIilcbnZhciBwYXJzZUhlYWRlcnMgPSByZXF1aXJlKFwicGFyc2UtaGVhZGVyc1wiKVxudmFyIHh0ZW5kID0gcmVxdWlyZShcInh0ZW5kXCIpXG5cbm1vZHVsZS5leHBvcnRzID0gY3JlYXRlWEhSXG5jcmVhdGVYSFIuWE1MSHR0cFJlcXVlc3QgPSB3aW5kb3cuWE1MSHR0cFJlcXVlc3QgfHwgbm9vcFxuY3JlYXRlWEhSLlhEb21haW5SZXF1ZXN0ID0gXCJ3aXRoQ3JlZGVudGlhbHNcIiBpbiAobmV3IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCgpKSA/IGNyZWF0ZVhIUi5YTUxIdHRwUmVxdWVzdCA6IHdpbmRvdy5YRG9tYWluUmVxdWVzdFxuXG5mb3JFYWNoQXJyYXkoW1wiZ2V0XCIsIFwicHV0XCIsIFwicG9zdFwiLCBcInBhdGNoXCIsIFwiaGVhZFwiLCBcImRlbGV0ZVwiXSwgZnVuY3Rpb24obWV0aG9kKSB7XG4gICAgY3JlYXRlWEhSW21ldGhvZCA9PT0gXCJkZWxldGVcIiA/IFwiZGVsXCIgOiBtZXRob2RdID0gZnVuY3Rpb24odXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgICAgICBvcHRpb25zID0gaW5pdFBhcmFtcyh1cmksIG9wdGlvbnMsIGNhbGxiYWNrKVxuICAgICAgICBvcHRpb25zLm1ldGhvZCA9IG1ldGhvZC50b1VwcGVyQ2FzZSgpXG4gICAgICAgIHJldHVybiBfY3JlYXRlWEhSKG9wdGlvbnMpXG4gICAgfVxufSlcblxuZnVuY3Rpb24gZm9yRWFjaEFycmF5KGFycmF5LCBpdGVyYXRvcikge1xuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgYXJyYXkubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgaXRlcmF0b3IoYXJyYXlbaV0pXG4gICAgfVxufVxuXG5mdW5jdGlvbiBpc0VtcHR5KG9iail7XG4gICAgZm9yKHZhciBpIGluIG9iail7XG4gICAgICAgIGlmKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkgcmV0dXJuIGZhbHNlXG4gICAgfVxuICAgIHJldHVybiB0cnVlXG59XG5cbmZ1bmN0aW9uIGluaXRQYXJhbXModXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgIHZhciBwYXJhbXMgPSB1cmlcblxuICAgIGlmIChpc0Z1bmN0aW9uKG9wdGlvbnMpKSB7XG4gICAgICAgIGNhbGxiYWNrID0gb3B0aW9uc1xuICAgICAgICBpZiAodHlwZW9mIHVyaSA9PT0gXCJzdHJpbmdcIikge1xuICAgICAgICAgICAgcGFyYW1zID0ge3VyaTp1cml9XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICBwYXJhbXMgPSB4dGVuZChvcHRpb25zLCB7dXJpOiB1cml9KVxuICAgIH1cblxuICAgIHBhcmFtcy5jYWxsYmFjayA9IGNhbGxiYWNrXG4gICAgcmV0dXJuIHBhcmFtc1xufVxuXG5mdW5jdGlvbiBjcmVhdGVYSFIodXJpLCBvcHRpb25zLCBjYWxsYmFjaykge1xuICAgIG9wdGlvbnMgPSBpbml0UGFyYW1zKHVyaSwgb3B0aW9ucywgY2FsbGJhY2spXG4gICAgcmV0dXJuIF9jcmVhdGVYSFIob3B0aW9ucylcbn1cblxuZnVuY3Rpb24gX2NyZWF0ZVhIUihvcHRpb25zKSB7XG4gICAgaWYodHlwZW9mIG9wdGlvbnMuY2FsbGJhY2sgPT09IFwidW5kZWZpbmVkXCIpe1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3IoXCJjYWxsYmFjayBhcmd1bWVudCBtaXNzaW5nXCIpXG4gICAgfVxuXG4gICAgdmFyIGNhbGxlZCA9IGZhbHNlXG4gICAgdmFyIGNhbGxiYWNrID0gZnVuY3Rpb24gY2JPbmNlKGVyciwgcmVzcG9uc2UsIGJvZHkpe1xuICAgICAgICBpZighY2FsbGVkKXtcbiAgICAgICAgICAgIGNhbGxlZCA9IHRydWVcbiAgICAgICAgICAgIG9wdGlvbnMuY2FsbGJhY2soZXJyLCByZXNwb25zZSwgYm9keSlcbiAgICAgICAgfVxuICAgIH1cblxuICAgIGZ1bmN0aW9uIHJlYWR5c3RhdGVjaGFuZ2UoKSB7XG4gICAgICAgIGlmICh4aHIucmVhZHlTdGF0ZSA9PT0gNCkge1xuICAgICAgICAgICAgbG9hZEZ1bmMoKVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gZ2V0Qm9keSgpIHtcbiAgICAgICAgLy8gQ2hyb21lIHdpdGggcmVxdWVzdFR5cGU9YmxvYiB0aHJvd3MgZXJyb3JzIGFycm91bmQgd2hlbiBldmVuIHRlc3RpbmcgYWNjZXNzIHRvIHJlc3BvbnNlVGV4dFxuICAgICAgICB2YXIgYm9keSA9IHVuZGVmaW5lZFxuXG4gICAgICAgIGlmICh4aHIucmVzcG9uc2UpIHtcbiAgICAgICAgICAgIGJvZHkgPSB4aHIucmVzcG9uc2VcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIGJvZHkgPSB4aHIucmVzcG9uc2VUZXh0IHx8IGdldFhtbCh4aHIpXG4gICAgICAgIH1cblxuICAgICAgICBpZiAoaXNKc29uKSB7XG4gICAgICAgICAgICB0cnkge1xuICAgICAgICAgICAgICAgIGJvZHkgPSBKU09OLnBhcnNlKGJvZHkpXG4gICAgICAgICAgICB9IGNhdGNoIChlKSB7fVxuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGJvZHlcbiAgICB9XG5cbiAgICB2YXIgZmFpbHVyZVJlc3BvbnNlID0ge1xuICAgICAgICAgICAgICAgIGJvZHk6IHVuZGVmaW5lZCxcbiAgICAgICAgICAgICAgICBoZWFkZXJzOiB7fSxcbiAgICAgICAgICAgICAgICBzdGF0dXNDb2RlOiAwLFxuICAgICAgICAgICAgICAgIG1ldGhvZDogbWV0aG9kLFxuICAgICAgICAgICAgICAgIHVybDogdXJpLFxuICAgICAgICAgICAgICAgIHJhd1JlcXVlc3Q6IHhoclxuICAgICAgICAgICAgfVxuXG4gICAgZnVuY3Rpb24gZXJyb3JGdW5jKGV2dCkge1xuICAgICAgICBjbGVhclRpbWVvdXQodGltZW91dFRpbWVyKVxuICAgICAgICBpZighKGV2dCBpbnN0YW5jZW9mIEVycm9yKSl7XG4gICAgICAgICAgICBldnQgPSBuZXcgRXJyb3IoXCJcIiArIChldnQgfHwgXCJVbmtub3duIFhNTEh0dHBSZXF1ZXN0IEVycm9yXCIpIClcbiAgICAgICAgfVxuICAgICAgICBldnQuc3RhdHVzQ29kZSA9IDBcbiAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGV2dCwgZmFpbHVyZVJlc3BvbnNlKVxuICAgIH1cblxuICAgIC8vIHdpbGwgbG9hZCB0aGUgZGF0YSAmIHByb2Nlc3MgdGhlIHJlc3BvbnNlIGluIGEgc3BlY2lhbCByZXNwb25zZSBvYmplY3RcbiAgICBmdW5jdGlvbiBsb2FkRnVuYygpIHtcbiAgICAgICAgaWYgKGFib3J0ZWQpIHJldHVyblxuICAgICAgICB2YXIgc3RhdHVzXG4gICAgICAgIGNsZWFyVGltZW91dCh0aW1lb3V0VGltZXIpXG4gICAgICAgIGlmKG9wdGlvbnMudXNlWERSICYmIHhoci5zdGF0dXM9PT11bmRlZmluZWQpIHtcbiAgICAgICAgICAgIC8vSUU4IENPUlMgR0VUIHN1Y2Nlc3NmdWwgcmVzcG9uc2UgZG9lc24ndCBoYXZlIGEgc3RhdHVzIGZpZWxkLCBidXQgYm9keSBpcyBmaW5lXG4gICAgICAgICAgICBzdGF0dXMgPSAyMDBcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHN0YXR1cyA9ICh4aHIuc3RhdHVzID09PSAxMjIzID8gMjA0IDogeGhyLnN0YXR1cylcbiAgICAgICAgfVxuICAgICAgICB2YXIgcmVzcG9uc2UgPSBmYWlsdXJlUmVzcG9uc2VcbiAgICAgICAgdmFyIGVyciA9IG51bGxcblxuICAgICAgICBpZiAoc3RhdHVzICE9PSAwKXtcbiAgICAgICAgICAgIHJlc3BvbnNlID0ge1xuICAgICAgICAgICAgICAgIGJvZHk6IGdldEJvZHkoKSxcbiAgICAgICAgICAgICAgICBzdGF0dXNDb2RlOiBzdGF0dXMsXG4gICAgICAgICAgICAgICAgbWV0aG9kOiBtZXRob2QsXG4gICAgICAgICAgICAgICAgaGVhZGVyczoge30sXG4gICAgICAgICAgICAgICAgdXJsOiB1cmksXG4gICAgICAgICAgICAgICAgcmF3UmVxdWVzdDogeGhyXG4gICAgICAgICAgICB9XG4gICAgICAgICAgICBpZih4aHIuZ2V0QWxsUmVzcG9uc2VIZWFkZXJzKXsgLy9yZW1lbWJlciB4aHIgY2FuIGluIGZhY3QgYmUgWERSIGZvciBDT1JTIGluIElFXG4gICAgICAgICAgICAgICAgcmVzcG9uc2UuaGVhZGVycyA9IHBhcnNlSGVhZGVycyh4aHIuZ2V0QWxsUmVzcG9uc2VIZWFkZXJzKCkpXG4gICAgICAgICAgICB9XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBlcnIgPSBuZXcgRXJyb3IoXCJJbnRlcm5hbCBYTUxIdHRwUmVxdWVzdCBFcnJvclwiKVxuICAgICAgICB9XG4gICAgICAgIHJldHVybiBjYWxsYmFjayhlcnIsIHJlc3BvbnNlLCByZXNwb25zZS5ib2R5KVxuICAgIH1cblxuICAgIHZhciB4aHIgPSBvcHRpb25zLnhociB8fCBudWxsXG5cbiAgICBpZiAoIXhocikge1xuICAgICAgICBpZiAob3B0aW9ucy5jb3JzIHx8IG9wdGlvbnMudXNlWERSKSB7XG4gICAgICAgICAgICB4aHIgPSBuZXcgY3JlYXRlWEhSLlhEb21haW5SZXF1ZXN0KClcbiAgICAgICAgfWVsc2V7XG4gICAgICAgICAgICB4aHIgPSBuZXcgY3JlYXRlWEhSLlhNTEh0dHBSZXF1ZXN0KClcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBrZXlcbiAgICB2YXIgYWJvcnRlZFxuICAgIHZhciB1cmkgPSB4aHIudXJsID0gb3B0aW9ucy51cmkgfHwgb3B0aW9ucy51cmxcbiAgICB2YXIgbWV0aG9kID0geGhyLm1ldGhvZCA9IG9wdGlvbnMubWV0aG9kIHx8IFwiR0VUXCJcbiAgICB2YXIgYm9keSA9IG9wdGlvbnMuYm9keSB8fCBvcHRpb25zLmRhdGEgfHwgbnVsbFxuICAgIHZhciBoZWFkZXJzID0geGhyLmhlYWRlcnMgPSBvcHRpb25zLmhlYWRlcnMgfHwge31cbiAgICB2YXIgc3luYyA9ICEhb3B0aW9ucy5zeW5jXG4gICAgdmFyIGlzSnNvbiA9IGZhbHNlXG4gICAgdmFyIHRpbWVvdXRUaW1lclxuXG4gICAgaWYgKFwianNvblwiIGluIG9wdGlvbnMpIHtcbiAgICAgICAgaXNKc29uID0gdHJ1ZVxuICAgICAgICBoZWFkZXJzW1wiYWNjZXB0XCJdIHx8IGhlYWRlcnNbXCJBY2NlcHRcIl0gfHwgKGhlYWRlcnNbXCJBY2NlcHRcIl0gPSBcImFwcGxpY2F0aW9uL2pzb25cIikgLy9Eb24ndCBvdmVycmlkZSBleGlzdGluZyBhY2NlcHQgaGVhZGVyIGRlY2xhcmVkIGJ5IHVzZXJcbiAgICAgICAgaWYgKG1ldGhvZCAhPT0gXCJHRVRcIiAmJiBtZXRob2QgIT09IFwiSEVBRFwiKSB7XG4gICAgICAgICAgICBoZWFkZXJzW1wiY29udGVudC10eXBlXCJdIHx8IGhlYWRlcnNbXCJDb250ZW50LVR5cGVcIl0gfHwgKGhlYWRlcnNbXCJDb250ZW50LVR5cGVcIl0gPSBcImFwcGxpY2F0aW9uL2pzb25cIikgLy9Eb24ndCBvdmVycmlkZSBleGlzdGluZyBhY2NlcHQgaGVhZGVyIGRlY2xhcmVkIGJ5IHVzZXJcbiAgICAgICAgICAgIGJvZHkgPSBKU09OLnN0cmluZ2lmeShvcHRpb25zLmpzb24pXG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB4aHIub25yZWFkeXN0YXRlY2hhbmdlID0gcmVhZHlzdGF0ZWNoYW5nZVxuICAgIHhoci5vbmxvYWQgPSBsb2FkRnVuY1xuICAgIHhoci5vbmVycm9yID0gZXJyb3JGdW5jXG4gICAgLy8gSUU5IG11c3QgaGF2ZSBvbnByb2dyZXNzIGJlIHNldCB0byBhIHVuaXF1ZSBmdW5jdGlvbi5cbiAgICB4aHIub25wcm9ncmVzcyA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgLy8gSUUgbXVzdCBkaWVcbiAgICB9XG4gICAgeGhyLm9udGltZW91dCA9IGVycm9yRnVuY1xuICAgIHhoci5vcGVuKG1ldGhvZCwgdXJpLCAhc3luYywgb3B0aW9ucy51c2VybmFtZSwgb3B0aW9ucy5wYXNzd29yZClcbiAgICAvL2hhcyB0byBiZSBhZnRlciBvcGVuXG4gICAgaWYoIXN5bmMpIHtcbiAgICAgICAgeGhyLndpdGhDcmVkZW50aWFscyA9ICEhb3B0aW9ucy53aXRoQ3JlZGVudGlhbHNcbiAgICB9XG4gICAgLy8gQ2Fubm90IHNldCB0aW1lb3V0IHdpdGggc3luYyByZXF1ZXN0XG4gICAgLy8gbm90IHNldHRpbmcgdGltZW91dCBvbiB0aGUgeGhyIG9iamVjdCwgYmVjYXVzZSBvZiBvbGQgd2Via2l0cyBldGMuIG5vdCBoYW5kbGluZyB0aGF0IGNvcnJlY3RseVxuICAgIC8vIGJvdGggbnBtJ3MgcmVxdWVzdCBhbmQganF1ZXJ5IDEueCB1c2UgdGhpcyBraW5kIG9mIHRpbWVvdXQsIHNvIHRoaXMgaXMgYmVpbmcgY29uc2lzdGVudFxuICAgIGlmICghc3luYyAmJiBvcHRpb25zLnRpbWVvdXQgPiAwICkge1xuICAgICAgICB0aW1lb3V0VGltZXIgPSBzZXRUaW1lb3V0KGZ1bmN0aW9uKCl7XG4gICAgICAgICAgICBhYm9ydGVkPXRydWUvL0lFOSBtYXkgc3RpbGwgY2FsbCByZWFkeXN0YXRlY2hhbmdlXG4gICAgICAgICAgICB4aHIuYWJvcnQoXCJ0aW1lb3V0XCIpXG4gICAgICAgICAgICB2YXIgZSA9IG5ldyBFcnJvcihcIlhNTEh0dHBSZXF1ZXN0IHRpbWVvdXRcIilcbiAgICAgICAgICAgIGUuY29kZSA9IFwiRVRJTUVET1VUXCJcbiAgICAgICAgICAgIGVycm9yRnVuYyhlKVxuICAgICAgICB9LCBvcHRpb25zLnRpbWVvdXQgKVxuICAgIH1cblxuICAgIGlmICh4aHIuc2V0UmVxdWVzdEhlYWRlcikge1xuICAgICAgICBmb3Ioa2V5IGluIGhlYWRlcnMpe1xuICAgICAgICAgICAgaWYoaGVhZGVycy5oYXNPd25Qcm9wZXJ0eShrZXkpKXtcbiAgICAgICAgICAgICAgICB4aHIuc2V0UmVxdWVzdEhlYWRlcihrZXksIGhlYWRlcnNba2V5XSlcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH0gZWxzZSBpZiAob3B0aW9ucy5oZWFkZXJzICYmICFpc0VtcHR5KG9wdGlvbnMuaGVhZGVycykpIHtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKFwiSGVhZGVycyBjYW5ub3QgYmUgc2V0IG9uIGFuIFhEb21haW5SZXF1ZXN0IG9iamVjdFwiKVxuICAgIH1cblxuICAgIGlmIChcInJlc3BvbnNlVHlwZVwiIGluIG9wdGlvbnMpIHtcbiAgICAgICAgeGhyLnJlc3BvbnNlVHlwZSA9IG9wdGlvbnMucmVzcG9uc2VUeXBlXG4gICAgfVxuXG4gICAgaWYgKFwiYmVmb3JlU2VuZFwiIGluIG9wdGlvbnMgJiZcbiAgICAgICAgdHlwZW9mIG9wdGlvbnMuYmVmb3JlU2VuZCA9PT0gXCJmdW5jdGlvblwiXG4gICAgKSB7XG4gICAgICAgIG9wdGlvbnMuYmVmb3JlU2VuZCh4aHIpXG4gICAgfVxuXG4gICAgeGhyLnNlbmQoYm9keSlcblxuICAgIHJldHVybiB4aHJcblxuXG59XG5cbmZ1bmN0aW9uIGdldFhtbCh4aHIpIHtcbiAgICBpZiAoeGhyLnJlc3BvbnNlVHlwZSA9PT0gXCJkb2N1bWVudFwiKSB7XG4gICAgICAgIHJldHVybiB4aHIucmVzcG9uc2VYTUxcbiAgICB9XG4gICAgdmFyIGZpcmVmb3hCdWdUYWtlbkVmZmVjdCA9IHhoci5zdGF0dXMgPT09IDIwNCAmJiB4aHIucmVzcG9uc2VYTUwgJiYgeGhyLnJlc3BvbnNlWE1MLmRvY3VtZW50RWxlbWVudC5ub2RlTmFtZSA9PT0gXCJwYXJzZXJlcnJvclwiXG4gICAgaWYgKHhoci5yZXNwb25zZVR5cGUgPT09IFwiXCIgJiYgIWZpcmVmb3hCdWdUYWtlbkVmZmVjdCkge1xuICAgICAgICByZXR1cm4geGhyLnJlc3BvbnNlWE1MXG4gICAgfVxuXG4gICAgcmV0dXJuIG51bGxcbn1cblxuZnVuY3Rpb24gbm9vcCgpIHt9XG4iLCJtb2R1bGUuZXhwb3J0cyA9IGV4dGVuZFxuXG52YXIgaGFzT3duUHJvcGVydHkgPSBPYmplY3QucHJvdG90eXBlLmhhc093blByb3BlcnR5O1xuXG5mdW5jdGlvbiBleHRlbmQoKSB7XG4gICAgdmFyIHRhcmdldCA9IHt9XG5cbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGFyZ3VtZW50cy5sZW5ndGg7IGkrKykge1xuICAgICAgICB2YXIgc291cmNlID0gYXJndW1lbnRzW2ldXG5cbiAgICAgICAgZm9yICh2YXIga2V5IGluIHNvdXJjZSkge1xuICAgICAgICAgICAgaWYgKGhhc093blByb3BlcnR5LmNhbGwoc291cmNlLCBrZXkpKSB7XG4gICAgICAgICAgICAgICAgdGFyZ2V0W2tleV0gPSBzb3VyY2Vba2V5XVxuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuXG4gICAgcmV0dXJuIHRhcmdldFxufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcblxuXG5jbGFzcyBBcHAgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRjb25zdHJ1Y3Rvcihwcm9wcykge1xuXHRcdHN1cGVyKHByb3BzKTtcblx0XHR0aGlzLnN0YXRlID0ge1xuXHRcdFx0bGlua0xpbmVzOiBbXSxcblx0XHRcdHdpZHRoOiB3aW5kb3cuaW5uZXJXaWR0aFxuXHRcdH07XG5cblx0fVxuXG5cdGNvbXBvbmVudERpZE1vdW50KCkge1xuXHRcdHdpbmRvdy5hZGRFdmVudExpc3RlbmVyKFwicmVzaXplXCIsICgpID0+IHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe3dpZHRoOiB3aW5kb3cuaW5uZXJXaWR0aH0sICgpID0+IHRoaXMuZ2VuZXJhdGVMaW5lcygpKVxuXHRcdH0pO1xuXHRcdHRoaXMuZ2VuZXJhdGVMaW5lcygpO1xuXHR9XG5cblx0Z2VuZXJhdGVMaW5lcygpIHtcblx0XHRjb25zdCB7IGxpbmtzIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxpbmtMaW5lcyA9IGxpbmtzLm1hcCgobGluaykgPT4ge1xuXHRcdFx0Y29uc3Qgc291cmNlUmVjdCA9IHRoaXMucmVmc1tsaW5rLnNvdXJjZV0uZ2V0Qm91bmRpbmdDbGllbnRSZWN0KCk7XG5cdFx0XHRjb25zdCB0YXJnZXRSZWN0ID0gdGhpcy5yZWZzW2xpbmsudGFyZ2V0XS5nZXRCb3VuZGluZ0NsaWVudFJlY3QoKTtcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdHNvdXJjZTogeyB4OiBzb3VyY2VSZWN0LnggKyBzb3VyY2VSZWN0LndpZHRoIC8gMiwgeTogc291cmNlUmVjdC55ICsgc291cmNlUmVjdC5oZWlnaHQgLyAyfSxcblx0XHRcdFx0dGFyZ2V0OiB7IHg6IHRhcmdldFJlY3QueCArIHRhcmdldFJlY3Qud2lkdGggLyAyLCB5OiB0YXJnZXRSZWN0LnkgKyB0YXJnZXRSZWN0LmhlaWdodCAvIDJ9LFxuXHRcdFx0XHR0eXBlOiBsaW5rLnR5cGVcblx0XHRcdH1cblx0XHR9KTtcblx0XHR0aGlzLnNldFN0YXRlKHtsaW5rTGluZXM6IGxpbmtMaW5lc30pO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgZGF0YXNldHMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBsaW5rTGluZXMsIHdpZHRoIH0gPSB0aGlzLnN0YXRlO1xuXHRcdFxuXHRcdGNvbnN0IGhlaWdodCA9IDEwMDAwO1xuXHRcdGNvbnN0IGludGVydmFsID0gKHdpZHRoIC0gODApICAvIGRhdGFzZXRzLmxlbmd0aDtcblx0XHRyZXR1cm4gKFxuXHRcdFx0PHN2ZyBzdHlsZT17e3Bvc2l0aW9uOiBcImFic29sdXRlXCIsIHRvcDogMCwgbGVmdDogMH19XG5cdFx0XHRcdHdpZHRoPXt3aWR0aH0gaGVpZ2h0PXtoZWlnaHR9IHZpZXdCb3g9e2AwIDAgJHt3aWR0aH0gJHtoZWlnaHR9YH0+XG5cdFx0XHRcdHtsaW5rTGluZXMubWFwKChsaW5rTGluZSwgaWR4KSA9PiAoXG5cdFx0XHRcdFx0PGcga2V5PXtpZHh9PlxuXHRcdFx0XHRcdFx0PGxpbmUgeDE9e2xpbmtMaW5lLnNvdXJjZS54fSB5MT17bGlua0xpbmUuc291cmNlLnl9XG5cdFx0XHRcdFx0XHRcdHgyPXtsaW5rTGluZS50YXJnZXQueH0geTI9e2xpbmtMaW5lLnRhcmdldC55fSBcblx0XHRcdFx0XHRcdFx0c3Ryb2tlV2lkdGg9ezAuNX0gc3Ryb2tlPVwiYmxhY2tcIiAvPlxuXHRcdFx0XHRcdFx0e2xpbmtMaW5lLnR5cGUgIT09IFwiaXNJbkNvbGxlY3Rpb25cIiAmJiBsaW5rTGluZS50eXBlICE9PSBcImhhc0NvbGxlY3Rpb25cIiA/IFxuXHRcdFx0XHRcdFx0XHQ8ZyB0cmFuc2Zvcm09e2B0cmFuc2xhdGUoJHsobGlua0xpbmUuc291cmNlLnggKyBsaW5rTGluZS50YXJnZXQueCkgLyAyIC0gNTB9LCAkeyhsaW5rTGluZS5zb3VyY2UueSArIGxpbmtMaW5lLnRhcmdldC55KSAvIDJ9KWB9PlxuXHRcdFx0XHRcdFx0XHRcdDx0ZXh0PntsaW5rTGluZS50eXBlfTwvdGV4dD5cblx0XHRcdFx0XHRcdFx0PC9nPlxuXHRcdFx0XHRcdFx0OiBudWxsfVxuXHRcdFx0XHRcdDwvZz5cblx0XHRcdFx0KSl9XG5cblx0XHRcdFx0e2RhdGFzZXRzLm1hcCgoZGF0YXNldCwgaWR4KSA9PiB7XG5cdFx0XHRcdFx0Y29uc3QgY29sbGVjdGlvbkludGVydmFsID0gaW50ZXJ2YWwgLyBkYXRhc2V0LmNvbGxlY3Rpb25zLmxlbmd0aDtcblx0XHRcdFx0XHRyZXR1cm4gKFxuXHRcdFx0XHRcdFx0PGcga2V5PXtkYXRhc2V0LmxhYmVsfSB0cmFuc2Zvcm09e2B0cmFuc2xhdGUoJHsoMC41KmludGVydmFsKSArICAoaWR4KSAqIGludGVydmFsIH0sIDEwKWB9PlxuXHRcdFx0XHRcdFx0XHQ8Y2lyY2xlIHI9ezEwfSByZWY9e2RhdGFzZXQuaWR9IC8+XG5cdFx0XHRcdFx0XHRcdDxnIHRyYW5zZm9ybT1cInRyYW5zbGF0ZSgxMiwgNilcIj5cblx0XHRcdFx0XHRcdFx0XHQ8dGV4dD5EYXRhc2V0OiB7ZGF0YXNldC5sYWJlbH08L3RleHQ+XG5cdFx0XHRcdFx0XHRcdDwvZz5cblx0XHRcdFx0XHRcdFx0PGcgdHJhbnNmb3JtPXtgdHJhbnNsYXRlKCR7LTAuNSppbnRlcnZhbH0sIDEwMClgfT5cblx0XHRcdFx0XHRcdFx0XHR7ZGF0YXNldC5jb2xsZWN0aW9ucy5tYXAoKGNvbGxlY3Rpb24sIGlkeCkgPT4gKFxuXHRcdFx0XHRcdFx0XHRcdFx0PGcga2V5PXtjb2xsZWN0aW9uLmxhYmVsfSB0cmFuc2Zvcm09e2B0cmFuc2xhdGUoJHsoMC41KmNvbGxlY3Rpb25JbnRlcnZhbCkgKyBjb2xsZWN0aW9uSW50ZXJ2YWwgKiBpZHh9LCAwKWB9PlxuXHRcdFx0XHRcdFx0XHRcdFx0XHQ8Y2lyY2xlIHI9ezEwfSByZWY9e2NvbGxlY3Rpb24uaWR9IC8+XG5cdFx0XHRcdFx0XHRcdFx0XHRcdDxnIHRyYW5zZm9ybT1cInRyYW5zbGF0ZSgxMiwgNilcIj5cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHQ8dGV4dD5Db2xsZWN0aW9uOiB7Y29sbGVjdGlvbi5sYWJlbH08L3RleHQ+XG5cdFx0XHRcdFx0XHRcdFx0XHRcdDwvZz5cblx0XHRcdFx0XHRcdFx0XHRcdFx0e2NvbGxlY3Rpb24uZW50aXRpZXMubWFwKChlbnRpdHksIGlkeCkgPT4gKFxuXHRcdFx0XHRcdFx0XHRcdFx0XHRcdDxnIGtleT17ZW50aXR5LmlkfSB0cmFuc2Zvcm09e2B0cmFuc2xhdGUoMCwgJHsoaWR4KzEpICogMzB9KWB9PlxuXHRcdFx0XHRcdFx0XHRcdFx0XHRcdFx0PGNpcmNsZSByPXs1fSByZWY9e2VudGl0eS5pZH0gLz5cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHRcdDxnIHRyYW5zZm9ybT1cInRyYW5zbGF0ZSg1LCAyKVwiPlxuXHRcdFx0XHRcdFx0XHRcdFx0XHRcdFx0XHQ8dGV4dD57ZW50aXR5LmxhYmVsfTwvdGV4dD5cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHRcdDwvZz5cblx0XHRcdFx0XHRcdFx0XHRcdFx0XHQ8L2c+XG5cdFx0XHRcdFx0XHRcdFx0XHRcdCkpfVxuXHRcdFx0XHRcdFx0XHRcdFx0PC9nPlxuXHRcdFx0XHRcdFx0XHRcdCkpfVxuXG5cdFx0XHRcdFx0XHRcdDwvZz5cblx0XHRcdFx0XHRcdDwvZz5cblx0XHRcdFx0XHQpO1xuXHRcdFx0XHR9KX1cblx0XHRcdDwvc3ZnPlxuXHRcdCk7XG5cdH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgQXBwOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBSZWFjdERPTSBmcm9tIFwicmVhY3QtZG9tXCI7XG5pbXBvcnQgQXBwIGZyb20gXCIuL2NvbXBvbmVudHMvYXBwXCI7XG5pbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcblxuZG9jdW1lbnQuYWRkRXZlbnRMaXN0ZW5lcihcIkRPTUNvbnRlbnRMb2FkZWRcIiwgKCkgPT4ge1xuXHR4aHIoYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2dyZW1saW4vZGF0YXNldHNgLCAoZXJyLCByZXNwLCBib2R5KSA9PiB7XG5cdFx0Y29uc3QgZGF0YSA9IEpTT04ucGFyc2UoYm9keSk7XG5cdFx0Y29uc3Qgbm9kZXMgPSBkYXRhLm5vZGVzLm1hcCgobm9kZSwgaWQpID0+ICh7Li4ubm9kZSwgaWQ6IGlkfSkpO1xuXHRcdGNvbnN0IGxpbmtzID0gZGF0YS5saW5rcy5tYXAoKGxpbmssIGlkKSA9PiAoey4uLmxpbmssIGlkOiBpZH0pKTtcblx0XHRjb25zdCBkYXRhc2V0cyA9IG5vZGVzXG5cdFx0XHQuZmlsdGVyKChub2RlKSA9PiBub2RlLnR5cGUgPT09IFwiZGF0YXNldFwiKVxuXHRcdFx0Lm1hcCgobm9kZSkgPT4gKHtcblx0XHRcdFx0Li4ubm9kZSxcblx0XHRcdFx0Y29sbGVjdGlvbnM6IGxpbmtzXG5cdFx0XHRcdFx0LmZpbHRlcigobGluaykgPT4gbGluay5zb3VyY2UgPT09IG5vZGUuaWQgJiYgbGluay50eXBlID09PSBcImhhc0NvbGxlY3Rpb25cIilcblx0XHRcdFx0XHQubWFwKChsaW5rKSA9PiB7IHJldHVybiAoe1xuXHRcdFx0XHRcdFx0Li4ubm9kZXMuZmluZCgobm9kZSkgPT4gbm9kZS5pZCA9PT0gbGluay50YXJnZXQpLFxuXHRcdFx0XHRcdFx0ZW50aXRpZXM6IGxpbmtzXG5cdFx0XHRcdFx0XHRcdC5maWx0ZXIoKGxpbmsxKSA9PiBsaW5rMS50YXJnZXQgPT09IGxpbmsudGFyZ2V0ICYmIGxpbmsxLnR5cGUgPT09XCJpc0luQ29sbGVjdGlvblwiKVxuXHRcdFx0XHRcdFx0XHQubWFwKChsaW5rMSkgPT4gbm9kZXMuZmluZCgobm9kZTEpID0+IG5vZGUxLmlkID09PSBsaW5rMS5zb3VyY2UpKVxuXHRcdFx0XHRcdH0pfSlcblx0XHRcdH0pKTtcblxuXHRcdFJlYWN0RE9NLnJlbmRlcig8QXBwIGRhdGFzZXRzPXtkYXRhc2V0c30gbGlua3M9e2xpbmtzfSAvPiwgZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoXCJhcHBcIikpO1xuXHR9KTtcbn0pOyJdfQ==
