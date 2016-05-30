(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.TimbuctooEdit = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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

},{"is-function":14}],2:[function(require,module,exports){
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
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsAutocompleteList = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsAutocomplete = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsInput = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
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

var Input = (function (_React$Component) {
	_inherits(Input, _React$Component);

	function Input(props) {
		_classCallCheck(this, Input);

		_get(Object.getPrototypeOf(Input.prototype), "constructor", this).call(this, props);

		this.state = {
			valid: true,
			invalidMessage: null
		};
	}

	_createClass(Input, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (this.props.value === nextProps.value) {
				return;
			}

			if (nextProps.value === "") {
				if (!this.state.valid) {
					this.setState({
						valid: true,
						invalidMessage: null
					});
				}

				return;
			} else if (this.props.validate) {
				var validator = this.props.validate(nextProps.value);

				this.setState({
					valid: validator.isValid,
					invalidMessage: validator.message
				});

				if (!validator.isValid && this.props.onInvalid) {
					this.props.onInvalid(validator.message, nextProps.value);
				}
			}
		}
	}, {
		key: "shouldComponentUpdate",
		value: function shouldComponentUpdate(nextProps, nextState) {
			return this.props.value !== nextProps.value;
		}
	}, {
		key: "handleChange",
		value: function handleChange(ev) {
			this.props.onChange(ev.currentTarget.value, ev);
		}
	}, {
		key: "render",
		value: function render() {
			var invalidMessage = this.state.invalidMessage ? _react2["default"].createElement(
				"div",
				{ className: "hire-forms-invalid-message" },
				this.state.invalidMessage
			) : null;

			return _react2["default"].createElement(
				"div",
				{
					className: (0, _classnames2["default"])("hire-input", { invalid: !this.state.valid }) },
				_react2["default"].createElement("input", {
					onBlur: this.props.onBlur,
					onChange: this.handleChange.bind(this),
					onFocus: this.props.onFocus,
					onKeyDown: this.props.onKeyDown,
					onKeyUp: this.props.onKeyUp,
					placeholder: this.props.placeholder,
					style: this.props.style,
					value: this.props.value }),
				invalidMessage
			);
		}
	}]);

	return Input;
})(_react2["default"].Component);

Input.propTypes = {
	onBlur: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	onFocus: _react2["default"].PropTypes.func,
	onInvalid: _react2["default"].PropTypes.func,
	onKeyDown: _react2["default"].PropTypes.func,
	onKeyUp: _react2["default"].PropTypes.func,
	placeholder: _react2["default"].PropTypes.string,
	style: _react2["default"].PropTypes.object,
	valid: _react2["default"].PropTypes.bool,
	validate: _react2["default"].PropTypes.func,
	value: _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.number])
};

Input.defaultProps = {
	value: ""
};

exports["default"] = Input;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}]},{},[1])(1)
});
},{}],2:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],2:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],3:[function(_dereq_,module,exports){
// TODO move listitem to seperate component (so we don't have to store data-key and data-value as attributes)
// Move util functions to seperate module

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

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var hasKeyValue = function hasKeyValue(list, item) {
	return list.filter(function (li) {
		return li.key === item.key;
	}).length > 0;
};

/**
 * Options are rendered beneath the autocomplete and select components.
 *
 * @class
 * @extends React.Component
 */

var Options = (function (_React$Component) {
	_inherits(Options, _React$Component);

	function Options() {
		_classCallCheck(this, Options);

		_get(Object.getPrototypeOf(Options.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Options, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			var node = _reactDom2["default"].findDOMNode(this);

			if (node) {
				node.style.zIndex = 1000;
			}
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			var node = _reactDom2["default"].findDOMNode(this);
			node.style.zIndex = 0;
		}

		/**
   * Sort values on relevance. A result is more relevant when the search
   * query is more at the beginning of the string. In other words:
   * String.indexOf(props.query): lower is better.
   * @param {Array<Object>} value An array of key/value maps
   * @param {String} query A search query
   * @returns {Array<Object>} Sorted values on relevance
   */
	}, {
		key: "sortRelevance",
		value: function sortRelevance(values, query) {
			return values.sort(function (a, b) {
				a = a.value.toLowerCase();
				b = b.value.toLowerCase();

				var indexA = a.indexOf(query);
				var indexB = b.indexOf(query);

				if (indexA > indexB) {
					return 1;
				}

				if (indexA < indexB) {
					return -1;
				}

				if (indexA === indexB) {
					if (a > b) {
						return 1;
					}

					if (a < b) {
						return -1;
					}
				}

				return 0;
			});
		}

		/*
   * highlight the currently highlighted option.
   *
   * @param {Object} target An HTMLElement or event object
   * @param {String} className Name of the highlight class
   */
	}, {
		key: "highlight",
		value: function highlight(target, className) {
			// Check if target is an event object.
			if (target.hasOwnProperty("currentTarget")) {
				target = target.currentTarget;
			}

			target.classList.add(className);
		}

		/**
   * Unhighlight the currently highlighted option.
   *
   * @param {String} className Name of the highlight class
   * @return {Object} The unhighlighted HTMLElement
   */
	}, {
		key: "unhighlight",
		value: function unhighlight(className) {
			var el = undefined;
			var node = _reactDom2["default"].findDOMNode(this);

			if (node) {
				el = node.querySelector("li." + className);

				if (el) {
					el.classList.remove(className);
				}
			}

			return el;
		}
	}, {
		key: "handleClick",
		value: function handleClick(ev) {
			this.props.onChange(this.getOptionData(ev.currentTarget));
		}
	}, {
		key: "highlightPrev",
		value: function highlightPrev() {
			var prev = undefined;
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				prev = current.previousElementSibling;
			}

			// If current and prev aren't found, start at the top.
			// Current is not found if there is no list item highlighted.
			// Prev is not found if the first list item is highlighted.
			if (!prev) {
				prev = _reactDom2["default"].findDOMNode(this).lastChild;
			}

			this.highlight(prev, this.props.highlightClass);
		}
	}, {
		key: "highlightNext",
		value: function highlightNext() {
			var next = undefined;
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				next = current.nextElementSibling;
			}

			// If current and next aren't found, start at the top.
			// Current is not found if there is no list item highlighted.
			// Next is not found if the last list item is highlighted.
			if (!next) {
				next = _reactDom2["default"].findDOMNode(this).firstChild;
			}

			this.highlight(next, this.props.highlightClass);
		}
	}, {
		key: "select",
		value: function select() {
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				this.props.onChange(this.getOptionData(current));
			}
		}

		/**
   * Get the key (id) and value (display name) of an option DOM element.
   *
   * @param {Object} el - Option DOM element
   * @returns {Object}
   */
	}, {
		key: "getOptionData",
		value: function getOptionData(el) {
			return {
				key: el.getAttribute("data-key"),
				value: el.getAttribute("data-value")
			};
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			if (this.props.values.length === 0) {
				return null;
			}

			var values = this.props.sort || this.props.sortRelevance && this.props.query !== "" ? this.sortRelevance(this.props.values, this.props.querySelector) : this.props.values;

			var listitems = values.map(function (data, index) {
				var displayValue = data.value;

				if (_this.props.query.length) {
					var re = new RegExp(_this.props.query, "ig");
					displayValue = data.value.replace(re, "<span class=\"highlight\">$&</span>");
				}

				return _react2["default"].createElement("li", {
					className: (0, _classnames2["default"])({
						"hire-forms-option": true,
						selected: hasKeyValue((0, _hireFormsUtils.castArray)(_this.props.value), data)
					}),
					dangerouslySetInnerHTML: { __html: displayValue },
					"data-key": data.key,
					"data-value": data.value,
					key: index,
					onClick: _this.handleClick.bind(_this) });
			});

			return _react2["default"].createElement(
				"ul",
				{
					className: "hire-options" },
				listitems
			);
		}
	}]);

	return Options;
})(_react2["default"].Component);

Options.defaultProps = {
	highlightClass: "highlight",
	query: "",
	sort: false,
	sortRelevance: true,
	value: { key: "", value: "" },
	values: []
};

Options.propTypes = {
	highlightClass: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func.isRequired,
	query: _react2["default"].PropTypes.string,
	sort: _react2["default"].PropTypes.bool,
	sortRelevance: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.keyValueMapOrArrayOfKeyValueMaps,
	values: _hireFormsPropTypes.arrayOfKeyValueMaps
};

exports["default"] = Options;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-prop-types":1,"hire-forms-utils":2,"react":"react","react-dom":"react-dom"}]},{},[3])(3)
});
},{"classnames":"classnames","hire-forms-prop-types":3,"hire-forms-utils":4,"react":"react","react-dom":"react-dom"}],3:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],4:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],5:[function(_dereq_,module,exports){
// Rebuild build
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

var _hireFormsInput = _dereq_("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var _hireFormsOptions = _dereq_("hire-forms-options");

var _hireFormsOptions2 = _interopRequireDefault(_hireFormsOptions);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var Autocomplete = (function (_React$Component) {
	_inherits(Autocomplete, _React$Component);

	_createClass(Autocomplete, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			this.setState({
				query: nextProps.value.value,
				options: []
			});
		}
	}]);

	function Autocomplete(props) {
		_classCallCheck(this, Autocomplete);

		_get(Object.getPrototypeOf(Autocomplete.prototype), "constructor", this).call(this, props);

		this.cache = {};

		this.state = {
			options: [],
			query: props.value.value
		};
	}

	_createClass(Autocomplete, [{
		key: "handleInputChange",
		value: function handleInputChange(inputValue) {
			// Return empty options if inputValue length is beneath a treshold.
			if (inputValue.length < this.props.minLength) {
				return this.setState({
					query: inputValue,
					options: []
				});
			}

			// Return cache if inputValue is found in the cache.
			if (this.cache.hasOwnProperty(inputValue)) {
				return this.setState({
					query: inputValue,
					options: this.cache[inputValue]
				});
			}

			if (this.props.async == null) {
				this.filter(inputValue);
			} else {
				this.filterAsync(inputValue);
			}
		}
	}, {
		key: "filterAsync",
		value: function filterAsync(inputValue) {
			this.setState({ "query": inputValue });

			var done = function done(response) {
				// Add the options to the cache.
				this.cache[inputValue] = response;

				// Get the cache from the current (!!!) inputValue. The results trail behind
				// the user typing, so we have to pass the options of the current inputValue,
				// not the options of the inputValue of the fetch.
				var state = this.cache.hasOwnProperty(this.state.query) ? { options: this.cache[this.state.query] } : { options: [] };

				this.setState(state);
			};

			this.props.async(inputValue, done.bind(this));
		}
	}, {
		key: "filter",
		value: function filter(inputValue) {
			this.cache[inputValue] = inputValue === "" ? [] : this.props.options.filter(function (value) {
				if ((0, _hireFormsUtils.isKeyValueMap)(value)) {
					value = value.value;
				}

				return value.toLowerCase().indexOf(inputValue.toLowerCase()) > -1;
			});

			this.setState({
				query: inputValue,
				options: this.cache[inputValue]
			});
		}
	}, {
		key: "handleInputKeyDown",
		value: function handleInputKeyDown(ev) {
			// Escape
			if (ev.keyCode === 27) {
				this.setState({
					options: [],
					query: ""
				});
			}

			if (this.refs.options == null) {
				return;
			}

			// Up
			if (ev.keyCode === 38) {
				this.refs.options.highlightPrev();
			}

			// Down
			if (ev.keyCode === 40) {
				this.refs.options.highlightNext();
			}

			// Enter
			if (ev.keyCode === 13) {
				this.refs.options.select();
			}
		}

		/*
   * @param {Object} value Key/value map, ie: {key: "A", value: "A"}
   */
	}, {
		key: "handleOptionsChange",
		value: function handleOptionsChange(value) {
			this.props.onChange(value);
		}
	}, {
		key: "render",
		value: function render() {
			var options = this.state.options.length !== 0 ? _react2["default"].createElement(_hireFormsOptions2["default"], {
				onChange: this.handleOptionsChange.bind(this),
				query: this.state.query,
				ref: "options",
				value: this.props.value,
				values: (0, _hireFormsUtils.castKeyValueArray)(this.state.options) }) : null;

			return _react2["default"].createElement(
				"div",
				{
					className: "hire-forms-autocomplete",
					style: { position: "relative" } },
				_react2["default"].createElement(_hireFormsInput2["default"], {
					onChange: this.handleInputChange.bind(this),
					onKeyDown: this.handleInputKeyDown.bind(this),
					placeholder: this.props.placeholder,
					ref: "input",
					value: this.state.query }),
				this.props.children,
				options
			);
		}
	}]);

	return Autocomplete;
})(_react2["default"].Component);

Autocomplete.propTypes = {
	async: _react2["default"].PropTypes.func,
	children: _react2["default"].PropTypes.element,
	minLength: _react2["default"].PropTypes.number,
	onChange: _react2["default"].PropTypes.func,
	options: _hireFormsPropTypes.arrayOfKeyValueMaps,
	placeholder: _react2["default"].PropTypes.string,
	value: _hireFormsPropTypes.keyValueMap
};

Autocomplete.defaultProps = {
	minLength: 1,
	value: {
		key: "",
		value: ""
	}
};

exports["default"] = Autocomplete;
module.exports = exports["default"];

},{"hire-forms-input":1,"hire-forms-options":2,"hire-forms-prop-types":3,"hire-forms-utils":4,"react":"react"}]},{},[5])(5)
});
},{}],2:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsMutableList = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsInput = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
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

var Input = (function (_React$Component) {
	_inherits(Input, _React$Component);

	function Input(props) {
		_classCallCheck(this, Input);

		_get(Object.getPrototypeOf(Input.prototype), "constructor", this).call(this, props);

		this.state = {
			valid: true,
			invalidMessage: null
		};
	}

	_createClass(Input, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (this.props.value === nextProps.value) {
				return;
			}

			if (nextProps.value === "") {
				if (!this.state.valid) {
					this.setState({
						valid: true,
						invalidMessage: null
					});
				}

				return;
			} else if (this.props.validate) {
				var validator = this.props.validate(nextProps.value);

				this.setState({
					valid: validator.isValid,
					invalidMessage: validator.message
				});

				if (!validator.isValid && this.props.onInvalid) {
					this.props.onInvalid(validator.message, nextProps.value);
				}
			}
		}
	}, {
		key: "shouldComponentUpdate",
		value: function shouldComponentUpdate(nextProps, nextState) {
			return this.props.value !== nextProps.value;
		}
	}, {
		key: "handleChange",
		value: function handleChange(ev) {
			this.props.onChange(ev.currentTarget.value, ev);
		}
	}, {
		key: "render",
		value: function render() {
			var invalidMessage = this.state.invalidMessage ? _react2["default"].createElement(
				"div",
				{ className: "hire-forms-invalid-message" },
				this.state.invalidMessage
			) : null;

			return _react2["default"].createElement(
				"div",
				{
					className: (0, _classnames2["default"])("hire-input", { invalid: !this.state.valid }) },
				_react2["default"].createElement("input", {
					onBlur: this.props.onBlur,
					onChange: this.handleChange.bind(this),
					onFocus: this.props.onFocus,
					onKeyDown: this.props.onKeyDown,
					onKeyUp: this.props.onKeyUp,
					placeholder: this.props.placeholder,
					style: this.props.style,
					value: this.props.value }),
				invalidMessage
			);
		}
	}]);

	return Input;
})(_react2["default"].Component);

Input.propTypes = {
	onBlur: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	onFocus: _react2["default"].PropTypes.func,
	onInvalid: _react2["default"].PropTypes.func,
	onKeyDown: _react2["default"].PropTypes.func,
	onKeyUp: _react2["default"].PropTypes.func,
	placeholder: _react2["default"].PropTypes.string,
	style: _react2["default"].PropTypes.object,
	valid: _react2["default"].PropTypes.bool,
	validate: _react2["default"].PropTypes.func,
	value: _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.number])
};

Input.defaultProps = {
	value: ""
};

exports["default"] = Input;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}]},{},[1])(1)
});
},{}],2:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],3:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],4:[function(_dereq_,module,exports){
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

var _listItem = _dereq_("./list-item");

var _listItem2 = _interopRequireDefault(_listItem);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var List = (function (_React$Component) {
	_inherits(List, _React$Component);

	function List(props) {
		_classCallCheck(this, List);

		_get(Object.getPrototypeOf(List.prototype), "constructor", this).call(this, props);

		this.state = { editItemIndex: null };
	}

	_createClass(List, [{
		key: "handleListItemClick",
		value: function handleListItemClick(index, ev) {
			this.setState({ editItemIndex: index });

			if (this.props.onClick) {
				this.props.onClick(index, ev);
			}
		}
	}, {
		key: "handleListItemCancel",
		value: function handleListItemCancel() {
			this.setState({ editItemIndex: null });
		}
	}, {
		key: "handleListItemChange",
		value: function handleListItemChange(index, newValue) {
			this.setState({ editItemIndex: null });

			this.props.values[index] = newValue;
			this.props.onChange(this.props.values);
		}
	}, {
		key: "handleListItemRemove",
		value: function handleListItemRemove(index) {
			this.setState({ editItemIndex: null });

			this.props.values.splice(index, 1);
			this.props.onChange(this.props.values);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var list = this.props.values.map(function (item, index) {
				return _react2["default"].createElement(_listItem2["default"], {
					active: _this.state.editItemIndex === index,
					editable: _this.props.editable,
					key: index,
					onCancel: _this.handleListItemCancel.bind(_this, index),
					onChange: _this.handleListItemChange.bind(_this, index),
					onClick: _this.handleListItemClick.bind(_this, index),
					onRemove: _this.handleListItemRemove.bind(_this, index),
					removable: _this.props.removable,
					value: (0, _hireFormsUtils.castKeyValue)(item) });
			});

			list = list.length ? this.props.ordered ? _react2["default"].createElement(
				"ol",
				null,
				list
			) : _react2["default"].createElement(
				"ul",
				null,
				list
			) : _react2["default"].createElement(
				"span",
				{ className: "hire-empty-list" },
				"The list is empty"
			);

			return _react2["default"].createElement(
				"div",
				{ className: "hire-list" },
				list
			);
		}
	}]);

	return List;
})(_react2["default"].Component);

List.defaultProps = {
	editable: false,
	ordered: false,
	removable: true,
	values: []
};

List.propTypes = {
	editable: _react2["default"].PropTypes.bool,
	onChange: _react2["default"].PropTypes.func,
	onClick: _react2["default"].PropTypes.func,
	options: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps,
	ordered: _react2["default"].PropTypes.bool,
	removable: _react2["default"].PropTypes.bool,
	values: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps
};

exports["default"] = List;
module.exports = exports["default"];

},{"./list-item":5,"hire-forms-prop-types":2,"hire-forms-utils":3,"react":"react"}],5:[function(_dereq_,module,exports){
// TODO merge with static-list/list-item?
// TODO move css to default css file

"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = _dereq_("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _hireFormsInput = _dereq_("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var ext = function ext() {
	for (var _len = arguments.length, styles = Array(_len), _key = 0; _key < _len; _key++) {
		styles[_key] = arguments[_key];
	}

	return _extends.apply(undefined, [{}].concat(styles));
};

var liStyle = {
	cursor: "pointer"
};

var inputStyle = {
	width: "90%"
};

var buttonStyle = {
	width: "10%"
};

var spanStyle = {
	width: "90%"
};

var inlineBlockStyle = {
	display: "inline-block",
	boxSizing: "border-box",
	verticalAlign: "top"
};

var ListItem = (function (_React$Component) {
	_inherits(ListItem, _React$Component);

	_createClass(ListItem, [{
		key: "componentWillUpdate",
		value: function componentWillUpdate(nextProps, nextState) {
			if (!nextProps.active) {
				nextState.value = nextProps.value.value;
			}
		}
	}, {
		key: "componentDidUpdate",
		value: function componentDidUpdate() {
			if (this.props.active && this.props.editable) {
				var node = _reactDom2["default"].findDOMNode(this.refs.input);
				node.focus();
				node.value = node.value;
			}
		}
	}]);

	function ListItem(props) {
		_classCallCheck(this, ListItem);

		_get(Object.getPrototypeOf(ListItem.prototype), "constructor", this).call(this, props);

		this.state = { value: props.value.value };
	}

	_createClass(ListItem, [{
		key: "onInputChange",
		value: function onInputChange(value) {
			this.setState({ value: value });
		}
	}, {
		key: "onInputKeyDown",
		value: function onInputKeyDown(ev) {
			// if keyCode is "enter" or "tab"
			if (ev.keyCode === 13 || ev.keyCode === 9) {
				if (this.state.value === this.props.value.value) {
					this.props.onCancel();
				} else {
					this.props.onChange(this.state.value);
				}
			}

			// if keyCode is "escape"
			if (ev.keyCode === 27) {
				this.props.onCancel();
			}
		}
	}, {
		key: "render",
		value: function render() {
			var remove = undefined;

			var el = this.props.active && this.props.editable ? _react2["default"].createElement(_hireFormsInput2["default"], {
				onChange: this.onInputChange.bind(this),
				onKeyDown: this.onInputKeyDown.bind(this),
				ref: "input",
				style: ext(inlineBlockStyle, inputStyle),
				value: this.state.value }) : _react2["default"].createElement(
				"span",
				{
					className: "value",
					onClick: this.props.onClick.bind(this),
					style: ext(inlineBlockStyle, spanStyle) },
				this.props.value.value
			);

			if (this.props.active && this.props.removable) {
				remove = _react2["default"].createElement(
					"button",
					{
						className: "remove",
						onClick: this.props.onRemove,
						style: ext(inlineBlockStyle, buttonStyle) },
					"x"
				);
			}

			return _react2["default"].createElement(
				"li",
				{
					className: (0, _classnames2["default"])("hire-list-item", { active: this.props.active }),
					style: liStyle },
				el,
				remove
			);
		}
	}]);

	return ListItem;
})(_react2["default"].Component);

ListItem.defaultProps = {
	active: false,
	editable: false,
	removable: true
};

ListItem.propTypes = {
	active: _react2["default"].PropTypes.bool,
	editable: _react2["default"].PropTypes.bool,
	onCancel: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func,
	onClick: _react2["default"].PropTypes.func,
	onRemove: _react2["default"].PropTypes.func,
	removable: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.keyValueMap
};

exports["default"] = ListItem;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-input":1,"hire-forms-prop-types":2,"react":"react","react-dom":"react-dom"}]},{},[4])(4)
});
},{}],3:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],4:[function(_dereq_,module,exports){
// TODO Fix filter options (remove values from options). Doesn't work, because AutoComplete has a cache for the async.
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

var _hireFormsList = _dereq_("hire-forms-list");

var _hireFormsList2 = _interopRequireDefault(_hireFormsList);

var _hireFormsAutocomplete = _dereq_("hire-forms-autocomplete");

var _hireFormsAutocomplete2 = _interopRequireDefault(_hireFormsAutocomplete);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var AutocompleteList = (function (_React$Component) {
	_inherits(AutocompleteList, _React$Component);

	function AutocompleteList() {
		_classCallCheck(this, AutocompleteList);

		_get(Object.getPrototypeOf(AutocompleteList.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(AutocompleteList, [{
		key: "handleEditableListChange",
		value: function handleEditableListChange(values) {
			this.props.onChange(values);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			return _react2["default"].createElement(
				"div",
				{ className: "hire-forms-autocomplete-list" },
				_react2["default"].createElement(_hireFormsList2["default"], {
					mutable: true,
					onChange: this.handleEditableListChange.bind(this),
					ordered: this.props.ordered,
					values: this.props.values }),
				_react2["default"].createElement(_hireFormsAutocomplete2["default"], {
					async: this.props.async,
					onChange: function (value) {
						return _this.props.onChange(_this.props.values.concat(value));
					},
					options: this.props.options,
					placeholder: this.props.placeholder,
					ref: "autocomplete" })
			);
		}
	}]);

	return AutocompleteList;
})(_react2["default"].Component);

AutocompleteList.defaultProps = {
	options: [],
	ordered: false,
	values: []
};
AutocompleteList.propTypes = {
	async: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	options: _hireFormsPropTypes.arrayOfKeyValueMaps,
	ordered: _react2["default"].PropTypes.bool,
	placeholder: _react2["default"].PropTypes.string,
	values: _hireFormsPropTypes.arrayOfKeyValueMaps
};

exports["default"] = AutocompleteList;
module.exports = exports["default"];

},{"hire-forms-autocomplete":1,"hire-forms-list":2,"hire-forms-prop-types":3,"react":"react"}]},{},[4])(4)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],4:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsForm = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],2:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],3:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x2, _x3, _x4) { var _again = true; _function: while (_again) { var object = _x2, property = _x3, receiver = _x4; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x2 = parent; _x3 = property; _x4 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

exports["default"] = function (ComposedComponent) {
	var classNames = arguments.length <= 1 || arguments[1] === undefined ? [] : arguments[1];

	var Form = (function (_React$Component) {
		_inherits(Form, _React$Component);

		function Form() {
			_classCallCheck(this, Form);

			_get(Object.getPrototypeOf(Form.prototype), "constructor", this).apply(this, arguments);
		}

		_createClass(Form, [{
			key: "handleChange",
			value: function handleChange(key, value) {
				var attr = (0, _hireFormsUtils.castArray)(this.props.attr);

				this.props.onChange(attr.concat(key), value);
			}
		}, {
			key: "handleDelete",
			value: function handleDelete(key) {
				var attr = (0, _hireFormsUtils.castArray)(this.props.attr);

				this.props.onDelete(attr.concat(key));
			}
		}, {
			key: "handleInvalid",
			value: function handleInvalid(key) {
				var attr = (0, _hireFormsUtils.castArray)(this.props.attr);

				this.props.onInvalid(attr.concat(key));
			}
		}, {
			key: "render",
			value: function render() {
				return _react2["default"].createElement(
					"div",
					{ className: (0, _classnames2["default"])("hire-forms-form", classNames) },
					_react2["default"].createElement(ComposedComponent, _extends({}, this.props, {
						handleChange: this.handleChange.bind(this),
						handleDelete: this.handleDelete.bind(this),
						handleInvalid: this.handleInvalid.bind(this) }))
				);
			}
		}]);

		return Form;
	})(_react2["default"].Component);

	Form.propTypes = {
		// The array can consist of strings and numbers.
		attr: _hireFormsPropTypes.stringOrArray,
		onChange: _react2["default"].PropTypes.func.isRequired,
		onDelete: _react2["default"].PropTypes.func,
		onInvalid: _react2["default"].PropTypes.func,
		value: _react2["default"].PropTypes.object
	};

	Form.defaultProps = {
		attr: []
	};

	return Form;
};

module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-prop-types":1,"hire-forms-utils":2,"react":"react"}]},{},[3])(3)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],5:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsInput = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
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

var Input = (function (_React$Component) {
	_inherits(Input, _React$Component);

	function Input(props) {
		_classCallCheck(this, Input);

		_get(Object.getPrototypeOf(Input.prototype), "constructor", this).call(this, props);

		this.state = {
			valid: true,
			invalidMessage: null
		};
	}

	_createClass(Input, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (this.props.value === nextProps.value) {
				return;
			}

			if (nextProps.value === "") {
				if (!this.state.valid) {
					this.setState({
						valid: true,
						invalidMessage: null
					});
				}

				return;
			} else if (this.props.validate) {
				var validator = this.props.validate(nextProps.value);

				this.setState({
					valid: validator.isValid,
					invalidMessage: validator.message
				});

				if (!validator.isValid && this.props.onInvalid) {
					this.props.onInvalid(validator.message, nextProps.value);
				}
			}
		}
	}, {
		key: "shouldComponentUpdate",
		value: function shouldComponentUpdate(nextProps, nextState) {
			return this.props.value !== nextProps.value;
		}
	}, {
		key: "handleChange",
		value: function handleChange(ev) {
			this.props.onChange(ev.currentTarget.value, ev);
		}
	}, {
		key: "render",
		value: function render() {
			var invalidMessage = this.state.invalidMessage ? _react2["default"].createElement(
				"div",
				{ className: "hire-forms-invalid-message" },
				this.state.invalidMessage
			) : null;

			return _react2["default"].createElement(
				"div",
				{
					className: (0, _classnames2["default"])("hire-input", { invalid: !this.state.valid }) },
				_react2["default"].createElement("input", {
					onBlur: this.props.onBlur,
					onChange: this.handleChange.bind(this),
					onFocus: this.props.onFocus,
					onKeyDown: this.props.onKeyDown,
					onKeyUp: this.props.onKeyUp,
					placeholder: this.props.placeholder,
					style: this.props.style,
					value: this.props.value }),
				invalidMessage
			);
		}
	}]);

	return Input;
})(_react2["default"].Component);

Input.propTypes = {
	onBlur: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	onFocus: _react2["default"].PropTypes.func,
	onInvalid: _react2["default"].PropTypes.func,
	onKeyDown: _react2["default"].PropTypes.func,
	onKeyUp: _react2["default"].PropTypes.func,
	placeholder: _react2["default"].PropTypes.string,
	style: _react2["default"].PropTypes.object,
	valid: _react2["default"].PropTypes.bool,
	validate: _react2["default"].PropTypes.func,
	value: _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.number])
};

Input.defaultProps = {
	value: ""
};

exports["default"] = Input;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}]},{},[1])(1)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],6:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsMutableList = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsInput = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
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

var Input = (function (_React$Component) {
	_inherits(Input, _React$Component);

	function Input(props) {
		_classCallCheck(this, Input);

		_get(Object.getPrototypeOf(Input.prototype), "constructor", this).call(this, props);

		this.state = {
			valid: true,
			invalidMessage: null
		};
	}

	_createClass(Input, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (this.props.value === nextProps.value) {
				return;
			}

			if (nextProps.value === "") {
				if (!this.state.valid) {
					this.setState({
						valid: true,
						invalidMessage: null
					});
				}

				return;
			} else if (this.props.validate) {
				var validator = this.props.validate(nextProps.value);

				this.setState({
					valid: validator.isValid,
					invalidMessage: validator.message
				});

				if (!validator.isValid && this.props.onInvalid) {
					this.props.onInvalid(validator.message, nextProps.value);
				}
			}
		}
	}, {
		key: "shouldComponentUpdate",
		value: function shouldComponentUpdate(nextProps, nextState) {
			return this.props.value !== nextProps.value;
		}
	}, {
		key: "handleChange",
		value: function handleChange(ev) {
			this.props.onChange(ev.currentTarget.value, ev);
		}
	}, {
		key: "render",
		value: function render() {
			var invalidMessage = this.state.invalidMessage ? _react2["default"].createElement(
				"div",
				{ className: "hire-forms-invalid-message" },
				this.state.invalidMessage
			) : null;

			return _react2["default"].createElement(
				"div",
				{
					className: (0, _classnames2["default"])("hire-input", { invalid: !this.state.valid }) },
				_react2["default"].createElement("input", {
					onBlur: this.props.onBlur,
					onChange: this.handleChange.bind(this),
					onFocus: this.props.onFocus,
					onKeyDown: this.props.onKeyDown,
					onKeyUp: this.props.onKeyUp,
					placeholder: this.props.placeholder,
					style: this.props.style,
					value: this.props.value }),
				invalidMessage
			);
		}
	}]);

	return Input;
})(_react2["default"].Component);

Input.propTypes = {
	onBlur: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	onFocus: _react2["default"].PropTypes.func,
	onInvalid: _react2["default"].PropTypes.func,
	onKeyDown: _react2["default"].PropTypes.func,
	onKeyUp: _react2["default"].PropTypes.func,
	placeholder: _react2["default"].PropTypes.string,
	style: _react2["default"].PropTypes.object,
	valid: _react2["default"].PropTypes.bool,
	validate: _react2["default"].PropTypes.func,
	value: _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.number])
};

Input.defaultProps = {
	value: ""
};

exports["default"] = Input;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}]},{},[1])(1)
});
},{}],2:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],3:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],4:[function(_dereq_,module,exports){
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

var _listItem = _dereq_("./list-item");

var _listItem2 = _interopRequireDefault(_listItem);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var List = (function (_React$Component) {
	_inherits(List, _React$Component);

	function List(props) {
		_classCallCheck(this, List);

		_get(Object.getPrototypeOf(List.prototype), "constructor", this).call(this, props);

		this.state = { editItemIndex: null };
	}

	_createClass(List, [{
		key: "handleListItemClick",
		value: function handleListItemClick(index, ev) {
			this.setState({ editItemIndex: index });

			if (this.props.onClick) {
				this.props.onClick(index, ev);
			}
		}
	}, {
		key: "handleListItemCancel",
		value: function handleListItemCancel() {
			this.setState({ editItemIndex: null });
		}
	}, {
		key: "handleListItemChange",
		value: function handleListItemChange(index, newValue) {
			this.setState({ editItemIndex: null });

			this.props.values[index] = newValue;
			this.props.onChange(this.props.values);
		}
	}, {
		key: "handleListItemRemove",
		value: function handleListItemRemove(index) {
			this.setState({ editItemIndex: null });

			this.props.values.splice(index, 1);
			this.props.onChange(this.props.values);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var list = this.props.values.map(function (item, index) {
				return _react2["default"].createElement(_listItem2["default"], {
					active: _this.state.editItemIndex === index,
					editable: _this.props.editable,
					key: index,
					onCancel: _this.handleListItemCancel.bind(_this, index),
					onChange: _this.handleListItemChange.bind(_this, index),
					onClick: _this.handleListItemClick.bind(_this, index),
					onRemove: _this.handleListItemRemove.bind(_this, index),
					removable: _this.props.removable,
					value: (0, _hireFormsUtils.castKeyValue)(item) });
			});

			list = list.length ? this.props.ordered ? _react2["default"].createElement(
				"ol",
				null,
				list
			) : _react2["default"].createElement(
				"ul",
				null,
				list
			) : _react2["default"].createElement(
				"span",
				{ className: "hire-empty-list" },
				"The list is empty"
			);

			return _react2["default"].createElement(
				"div",
				{ className: "hire-list" },
				list
			);
		}
	}]);

	return List;
})(_react2["default"].Component);

List.defaultProps = {
	editable: false,
	ordered: false,
	removable: true,
	values: []
};

List.propTypes = {
	editable: _react2["default"].PropTypes.bool,
	onChange: _react2["default"].PropTypes.func,
	onClick: _react2["default"].PropTypes.func,
	options: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps,
	ordered: _react2["default"].PropTypes.bool,
	removable: _react2["default"].PropTypes.bool,
	values: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps
};

exports["default"] = List;
module.exports = exports["default"];

},{"./list-item":5,"hire-forms-prop-types":2,"hire-forms-utils":3,"react":"react"}],5:[function(_dereq_,module,exports){
// TODO merge with static-list/list-item?
// TODO move css to default css file

"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = _dereq_("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _hireFormsInput = _dereq_("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var ext = function ext() {
	for (var _len = arguments.length, styles = Array(_len), _key = 0; _key < _len; _key++) {
		styles[_key] = arguments[_key];
	}

	return _extends.apply(undefined, [{}].concat(styles));
};

var liStyle = {
	cursor: "pointer"
};

var inputStyle = {
	width: "90%"
};

var buttonStyle = {
	width: "10%"
};

var spanStyle = {
	width: "90%"
};

var inlineBlockStyle = {
	display: "inline-block",
	boxSizing: "border-box",
	verticalAlign: "top"
};

var ListItem = (function (_React$Component) {
	_inherits(ListItem, _React$Component);

	_createClass(ListItem, [{
		key: "componentWillUpdate",
		value: function componentWillUpdate(nextProps, nextState) {
			if (!nextProps.active) {
				nextState.value = nextProps.value.value;
			}
		}
	}, {
		key: "componentDidUpdate",
		value: function componentDidUpdate() {
			if (this.props.active && this.props.editable) {
				var node = _reactDom2["default"].findDOMNode(this.refs.input);
				node.focus();
				node.value = node.value;
			}
		}
	}]);

	function ListItem(props) {
		_classCallCheck(this, ListItem);

		_get(Object.getPrototypeOf(ListItem.prototype), "constructor", this).call(this, props);

		this.state = { value: props.value.value };
	}

	_createClass(ListItem, [{
		key: "onInputChange",
		value: function onInputChange(value) {
			this.setState({ value: value });
		}
	}, {
		key: "onInputKeyDown",
		value: function onInputKeyDown(ev) {
			// if keyCode is "enter" or "tab"
			if (ev.keyCode === 13 || ev.keyCode === 9) {
				if (this.state.value === this.props.value.value) {
					this.props.onCancel();
				} else {
					this.props.onChange(this.state.value);
				}
			}

			// if keyCode is "escape"
			if (ev.keyCode === 27) {
				this.props.onCancel();
			}
		}
	}, {
		key: "render",
		value: function render() {
			var remove = undefined;

			var el = this.props.active && this.props.editable ? _react2["default"].createElement(_hireFormsInput2["default"], {
				onChange: this.onInputChange.bind(this),
				onKeyDown: this.onInputKeyDown.bind(this),
				ref: "input",
				style: ext(inlineBlockStyle, inputStyle),
				value: this.state.value }) : _react2["default"].createElement(
				"span",
				{
					className: "value",
					onClick: this.props.onClick.bind(this),
					style: ext(inlineBlockStyle, spanStyle) },
				this.props.value.value
			);

			if (this.props.active && this.props.removable) {
				remove = _react2["default"].createElement(
					"button",
					{
						className: "remove",
						onClick: this.props.onRemove,
						style: ext(inlineBlockStyle, buttonStyle) },
					"x"
				);
			}

			return _react2["default"].createElement(
				"li",
				{
					className: (0, _classnames2["default"])("hire-list-item", { active: this.props.active }),
					style: liStyle },
				el,
				remove
			);
		}
	}]);

	return ListItem;
})(_react2["default"].Component);

ListItem.defaultProps = {
	active: false,
	editable: false,
	removable: true
};

ListItem.propTypes = {
	active: _react2["default"].PropTypes.bool,
	editable: _react2["default"].PropTypes.bool,
	onCancel: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func,
	onClick: _react2["default"].PropTypes.func,
	onRemove: _react2["default"].PropTypes.func,
	removable: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.keyValueMap
};

exports["default"] = ListItem;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-input":1,"hire-forms-prop-types":2,"react":"react","react-dom":"react-dom"}]},{},[4])(4)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],7:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsMultiForm = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],2:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],3:[function(_dereq_,module,exports){
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

var _hireFormsUtils = _dereq_("hire-forms-utils");

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _removeButton = _dereq_("./remove-button");

var _removeButton2 = _interopRequireDefault(_removeButton);

// <DateAndLocalityForm
// 	attr={"origin"}
// 	onChange={this.props.handleChange}
// 	onInvalid={this.props.handleInvalid}
// 	formData={model.origin} />

//TODO fix propType for this.props.component

var MultiForm = (function (_React$Component) {
	_inherits(MultiForm, _React$Component);

	function MultiForm() {
		_classCallCheck(this, MultiForm);

		_get(Object.getPrototypeOf(MultiForm.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(MultiForm, [{
		key: "handleRemoveForm",
		value: function handleRemoveForm(index) {
			var attr = (0, _hireFormsUtils.castArray)(this.props.attr);
			var key = attr.concat(index);

			this.props.onDelete(key);
		}
	}, {
		key: "handleInvalid",
		value: function handleInvalid(key) {
			if (this.props.onInvalid != null) {
				this.props.onInvalid(key);
			}
		}
	}, {
		key: "handleAdd",
		value: function handleAdd() {
			var values = (0, _hireFormsUtils.castArray)(this.props.model).concat(this.props.values);
			// let key = castArray(this.props.attr).concat(this.props.values.length);
			this.props.onChange(this.props.attr, values);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var attr = (0, _hireFormsUtils.castArray)(this.props.attr);

			var formList = this.props.values.length ? _react2["default"].createElement(
				"ul",
				{ className: "form-list" },
				this.props.values.map(function (listItem, index) {
					return _react2["default"].createElement(
						"li",
						{ className: (0, _classnames2["default"])("form", { "new": listItem === _this.props.model }),
							key: index },
						_react2["default"].createElement(_this.props.component, _extends({}, _this.props, {
							attr: attr.concat(index),
							formData: listItem,
							onInvalid: _this.handleInvalid.bind(_this) })),
						_react2["default"].createElement(_removeButton2["default"], _extends({}, _this.props, {
							attr: attr.concat(index) }))
					);
				})
			) : null;

			return _react2["default"].createElement(
				"div",
				{ className: "hire-multi-form" },
				_react2["default"].createElement(
					"button",
					{ onClick: this.handleAdd.bind(this) },
					"Add new"
				),
				formList
			);
		}
	}]);

	return MultiForm;
})(_react2["default"].Component);

MultiForm.defaultProps = {
	addButtonValue: "Add",
	removeButtonValue: "✕",
	values: []
};

MultiForm.propTypes = {
	addButtonValue: _react2["default"].PropTypes.string,
	attr: _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]),
	component: _react2["default"].PropTypes.func.isRequired,
	model: _react2["default"].PropTypes.object.isRequired,
	onChange: _react2["default"].PropTypes.func.isRequired,
	onDelete: _react2["default"].PropTypes.func.isRequired,
	onInvalid: _react2["default"].PropTypes.func,
	removeButtonValue: _react2["default"].PropTypes.string,
	values: _react2["default"].PropTypes.array
};

exports["default"] = MultiForm;
module.exports = exports["default"];
/*<AddForm
{...this.props}
attr={attr.concat(this.props.values.length)}/>*/

},{"./remove-button":4,"classnames":"classnames","hire-forms-prop-types":1,"hire-forms-utils":2,"react":"react"}],4:[function(_dereq_,module,exports){
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

var RemoveButton = (function (_React$Component) {
	_inherits(RemoveButton, _React$Component);

	function RemoveButton() {
		_classCallCheck(this, RemoveButton);

		_get(Object.getPrototypeOf(RemoveButton.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(RemoveButton, [{
		key: "render",
		value: function render() {
			var _this = this;

			return _react2["default"].createElement(
				"button",
				{
					className: "hire-remove-form",
					onClick: function () {
						return _this.props.onDelete(_this.props.attr);
					},
					title: "Remove" },
				this.props.removeButtonValue
			);
		}
	}]);

	return RemoveButton;
})(_react2["default"].Component);

exports["default"] = RemoveButton;
module.exports = exports["default"];

},{"react":"react"}]},{},[3])(3)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],8:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"react":"react"}],9:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsSelectList = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsMutableList = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsInput = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
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

var Input = (function (_React$Component) {
	_inherits(Input, _React$Component);

	function Input(props) {
		_classCallCheck(this, Input);

		_get(Object.getPrototypeOf(Input.prototype), "constructor", this).call(this, props);

		this.state = {
			valid: true,
			invalidMessage: null
		};
	}

	_createClass(Input, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (this.props.value === nextProps.value) {
				return;
			}

			if (nextProps.value === "") {
				if (!this.state.valid) {
					this.setState({
						valid: true,
						invalidMessage: null
					});
				}

				return;
			} else if (this.props.validate) {
				var validator = this.props.validate(nextProps.value);

				this.setState({
					valid: validator.isValid,
					invalidMessage: validator.message
				});

				if (!validator.isValid && this.props.onInvalid) {
					this.props.onInvalid(validator.message, nextProps.value);
				}
			}
		}
	}, {
		key: "shouldComponentUpdate",
		value: function shouldComponentUpdate(nextProps, nextState) {
			return this.props.value !== nextProps.value;
		}
	}, {
		key: "handleChange",
		value: function handleChange(ev) {
			this.props.onChange(ev.currentTarget.value, ev);
		}
	}, {
		key: "render",
		value: function render() {
			var invalidMessage = this.state.invalidMessage ? _react2["default"].createElement(
				"div",
				{ className: "hire-forms-invalid-message" },
				this.state.invalidMessage
			) : null;

			return _react2["default"].createElement(
				"div",
				{
					className: (0, _classnames2["default"])("hire-input", { invalid: !this.state.valid }) },
				_react2["default"].createElement("input", {
					onBlur: this.props.onBlur,
					onChange: this.handleChange.bind(this),
					onFocus: this.props.onFocus,
					onKeyDown: this.props.onKeyDown,
					onKeyUp: this.props.onKeyUp,
					placeholder: this.props.placeholder,
					style: this.props.style,
					value: this.props.value }),
				invalidMessage
			);
		}
	}]);

	return Input;
})(_react2["default"].Component);

Input.propTypes = {
	onBlur: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	onFocus: _react2["default"].PropTypes.func,
	onInvalid: _react2["default"].PropTypes.func,
	onKeyDown: _react2["default"].PropTypes.func,
	onKeyUp: _react2["default"].PropTypes.func,
	placeholder: _react2["default"].PropTypes.string,
	style: _react2["default"].PropTypes.object,
	valid: _react2["default"].PropTypes.bool,
	validate: _react2["default"].PropTypes.func,
	value: _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.number])
};

Input.defaultProps = {
	value: ""
};

exports["default"] = Input;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react"}]},{},[1])(1)
});
},{}],2:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],3:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],4:[function(_dereq_,module,exports){
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

var _listItem = _dereq_("./list-item");

var _listItem2 = _interopRequireDefault(_listItem);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var List = (function (_React$Component) {
	_inherits(List, _React$Component);

	function List(props) {
		_classCallCheck(this, List);

		_get(Object.getPrototypeOf(List.prototype), "constructor", this).call(this, props);

		this.state = { editItemIndex: null };
	}

	_createClass(List, [{
		key: "handleListItemClick",
		value: function handleListItemClick(index, ev) {
			this.setState({ editItemIndex: index });

			if (this.props.onClick) {
				this.props.onClick(index, ev);
			}
		}
	}, {
		key: "handleListItemCancel",
		value: function handleListItemCancel() {
			this.setState({ editItemIndex: null });
		}
	}, {
		key: "handleListItemChange",
		value: function handleListItemChange(index, newValue) {
			this.setState({ editItemIndex: null });

			this.props.values[index] = newValue;
			this.props.onChange(this.props.values);
		}
	}, {
		key: "handleListItemRemove",
		value: function handleListItemRemove(index) {
			this.setState({ editItemIndex: null });

			this.props.values.splice(index, 1);
			this.props.onChange(this.props.values);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var list = this.props.values.map(function (item, index) {
				return _react2["default"].createElement(_listItem2["default"], {
					active: _this.state.editItemIndex === index,
					editable: _this.props.editable,
					key: index,
					onCancel: _this.handleListItemCancel.bind(_this, index),
					onChange: _this.handleListItemChange.bind(_this, index),
					onClick: _this.handleListItemClick.bind(_this, index),
					onRemove: _this.handleListItemRemove.bind(_this, index),
					removable: _this.props.removable,
					value: (0, _hireFormsUtils.castKeyValue)(item) });
			});

			list = list.length ? this.props.ordered ? _react2["default"].createElement(
				"ol",
				null,
				list
			) : _react2["default"].createElement(
				"ul",
				null,
				list
			) : _react2["default"].createElement(
				"span",
				{ className: "hire-empty-list" },
				"The list is empty"
			);

			return _react2["default"].createElement(
				"div",
				{ className: "hire-list" },
				list
			);
		}
	}]);

	return List;
})(_react2["default"].Component);

List.defaultProps = {
	editable: false,
	ordered: false,
	removable: true,
	values: []
};

List.propTypes = {
	editable: _react2["default"].PropTypes.bool,
	onChange: _react2["default"].PropTypes.func,
	onClick: _react2["default"].PropTypes.func,
	options: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps,
	ordered: _react2["default"].PropTypes.bool,
	removable: _react2["default"].PropTypes.bool,
	values: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps
};

exports["default"] = List;
module.exports = exports["default"];

},{"./list-item":5,"hire-forms-prop-types":2,"hire-forms-utils":3,"react":"react"}],5:[function(_dereq_,module,exports){
// TODO merge with static-list/list-item?
// TODO move css to default css file

"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = _dereq_("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _classnames = _dereq_("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _hireFormsInput = _dereq_("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var ext = function ext() {
	for (var _len = arguments.length, styles = Array(_len), _key = 0; _key < _len; _key++) {
		styles[_key] = arguments[_key];
	}

	return _extends.apply(undefined, [{}].concat(styles));
};

var liStyle = {
	cursor: "pointer"
};

var inputStyle = {
	width: "90%"
};

var buttonStyle = {
	width: "10%"
};

var spanStyle = {
	width: "90%"
};

var inlineBlockStyle = {
	display: "inline-block",
	boxSizing: "border-box",
	verticalAlign: "top"
};

var ListItem = (function (_React$Component) {
	_inherits(ListItem, _React$Component);

	_createClass(ListItem, [{
		key: "componentWillUpdate",
		value: function componentWillUpdate(nextProps, nextState) {
			if (!nextProps.active) {
				nextState.value = nextProps.value.value;
			}
		}
	}, {
		key: "componentDidUpdate",
		value: function componentDidUpdate() {
			if (this.props.active && this.props.editable) {
				var node = _reactDom2["default"].findDOMNode(this.refs.input);
				node.focus();
				node.value = node.value;
			}
		}
	}]);

	function ListItem(props) {
		_classCallCheck(this, ListItem);

		_get(Object.getPrototypeOf(ListItem.prototype), "constructor", this).call(this, props);

		this.state = { value: props.value.value };
	}

	_createClass(ListItem, [{
		key: "onInputChange",
		value: function onInputChange(value) {
			this.setState({ value: value });
		}
	}, {
		key: "onInputKeyDown",
		value: function onInputKeyDown(ev) {
			// if keyCode is "enter" or "tab"
			if (ev.keyCode === 13 || ev.keyCode === 9) {
				if (this.state.value === this.props.value.value) {
					this.props.onCancel();
				} else {
					this.props.onChange(this.state.value);
				}
			}

			// if keyCode is "escape"
			if (ev.keyCode === 27) {
				this.props.onCancel();
			}
		}
	}, {
		key: "render",
		value: function render() {
			var remove = undefined;

			var el = this.props.active && this.props.editable ? _react2["default"].createElement(_hireFormsInput2["default"], {
				onChange: this.onInputChange.bind(this),
				onKeyDown: this.onInputKeyDown.bind(this),
				ref: "input",
				style: ext(inlineBlockStyle, inputStyle),
				value: this.state.value }) : _react2["default"].createElement(
				"span",
				{
					className: "value",
					onClick: this.props.onClick.bind(this),
					style: ext(inlineBlockStyle, spanStyle) },
				this.props.value.value
			);

			if (this.props.active && this.props.removable) {
				remove = _react2["default"].createElement(
					"button",
					{
						className: "remove",
						onClick: this.props.onRemove,
						style: ext(inlineBlockStyle, buttonStyle) },
					"x"
				);
			}

			return _react2["default"].createElement(
				"li",
				{
					className: (0, _classnames2["default"])("hire-list-item", { active: this.props.active }),
					style: liStyle },
				el,
				remove
			);
		}
	}]);

	return ListItem;
})(_react2["default"].Component);

ListItem.defaultProps = {
	active: false,
	editable: false,
	removable: true
};

ListItem.propTypes = {
	active: _react2["default"].PropTypes.bool,
	editable: _react2["default"].PropTypes.bool,
	onCancel: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func,
	onClick: _react2["default"].PropTypes.func,
	onRemove: _react2["default"].PropTypes.func,
	removable: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.keyValueMap
};

exports["default"] = ListItem;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-input":1,"hire-forms-prop-types":2,"react":"react","react-dom":"react-dom"}]},{},[4])(4)
});
},{}],2:[function(require,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],3:[function(require,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsSelect = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],2:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],3:[function(_dereq_,module,exports){
// TODO move listitem to seperate component (so we don't have to store data-key and data-value as attributes)
// Move util functions to seperate module

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

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var hasKeyValue = function hasKeyValue(list, item) {
	return list.filter(function (li) {
		return li.key === item.key;
	}).length > 0;
};

/**
 * Options are rendered beneath the autocomplete and select components.
 *
 * @class
 * @extends React.Component
 */

var Options = (function (_React$Component) {
	_inherits(Options, _React$Component);

	function Options() {
		_classCallCheck(this, Options);

		_get(Object.getPrototypeOf(Options.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Options, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			var node = _reactDom2["default"].findDOMNode(this);

			if (node) {
				node.style.zIndex = 1000;
			}
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			var node = _reactDom2["default"].findDOMNode(this);
			node.style.zIndex = 0;
		}

		/**
   * Sort values on relevance. A result is more relevant when the search
   * query is more at the beginning of the string. In other words:
   * String.indexOf(props.query): lower is better.
   * @param {Array<Object>} value An array of key/value maps
   * @param {String} query A search query
   * @returns {Array<Object>} Sorted values on relevance
   */
	}, {
		key: "sortRelevance",
		value: function sortRelevance(values, query) {
			return values.sort(function (a, b) {
				a = a.value.toLowerCase();
				b = b.value.toLowerCase();

				var indexA = a.indexOf(query);
				var indexB = b.indexOf(query);

				if (indexA > indexB) {
					return 1;
				}

				if (indexA < indexB) {
					return -1;
				}

				if (indexA === indexB) {
					if (a > b) {
						return 1;
					}

					if (a < b) {
						return -1;
					}
				}

				return 0;
			});
		}

		/*
   * highlight the currently highlighted option.
   *
   * @param {Object} target An HTMLElement or event object
   * @param {String} className Name of the highlight class
   */
	}, {
		key: "highlight",
		value: function highlight(target, className) {
			// Check if target is an event object.
			if (target.hasOwnProperty("currentTarget")) {
				target = target.currentTarget;
			}

			target.classList.add(className);
		}

		/**
   * Unhighlight the currently highlighted option.
   *
   * @param {String} className Name of the highlight class
   * @return {Object} The unhighlighted HTMLElement
   */
	}, {
		key: "unhighlight",
		value: function unhighlight(className) {
			var el = undefined;
			var node = _reactDom2["default"].findDOMNode(this);

			if (node) {
				el = node.querySelector("li." + className);

				if (el) {
					el.classList.remove(className);
				}
			}

			return el;
		}
	}, {
		key: "handleClick",
		value: function handleClick(ev) {
			this.props.onChange(this.getOptionData(ev.currentTarget));
		}
	}, {
		key: "highlightPrev",
		value: function highlightPrev() {
			var prev = undefined;
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				prev = current.previousElementSibling;
			}

			// If current and prev aren't found, start at the top.
			// Current is not found if there is no list item highlighted.
			// Prev is not found if the first list item is highlighted.
			if (!prev) {
				prev = _reactDom2["default"].findDOMNode(this).lastChild;
			}

			this.highlight(prev, this.props.highlightClass);
		}
	}, {
		key: "highlightNext",
		value: function highlightNext() {
			var next = undefined;
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				next = current.nextElementSibling;
			}

			// If current and next aren't found, start at the top.
			// Current is not found if there is no list item highlighted.
			// Next is not found if the last list item is highlighted.
			if (!next) {
				next = _reactDom2["default"].findDOMNode(this).firstChild;
			}

			this.highlight(next, this.props.highlightClass);
		}
	}, {
		key: "select",
		value: function select() {
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				this.props.onChange(this.getOptionData(current));
			}
		}

		/**
   * Get the key (id) and value (display name) of an option DOM element.
   *
   * @param {Object} el - Option DOM element
   * @returns {Object}
   */
	}, {
		key: "getOptionData",
		value: function getOptionData(el) {
			return {
				key: el.getAttribute("data-key"),
				value: el.getAttribute("data-value")
			};
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			if (this.props.values.length === 0) {
				return null;
			}

			var values = this.props.sort || this.props.sortRelevance && this.props.query !== "" ? this.sortRelevance(this.props.values, this.props.querySelector) : this.props.values;

			var listitems = values.map(function (data, index) {
				var displayValue = data.value;

				if (_this.props.query.length) {
					var re = new RegExp(_this.props.query, "ig");
					displayValue = data.value.replace(re, "<span class=\"highlight\">$&</span>");
				}

				return _react2["default"].createElement("li", {
					className: (0, _classnames2["default"])({
						"hire-forms-option": true,
						selected: hasKeyValue((0, _hireFormsUtils.castArray)(_this.props.value), data)
					}),
					dangerouslySetInnerHTML: { __html: displayValue },
					"data-key": data.key,
					"data-value": data.value,
					key: index,
					onClick: _this.handleClick.bind(_this) });
			});

			return _react2["default"].createElement(
				"ul",
				{
					className: "hire-options" },
				listitems
			);
		}
	}]);

	return Options;
})(_react2["default"].Component);

Options.defaultProps = {
	highlightClass: "highlight",
	query: "",
	sort: false,
	sortRelevance: true,
	value: { key: "", value: "" },
	values: []
};

Options.propTypes = {
	highlightClass: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func.isRequired,
	query: _react2["default"].PropTypes.string,
	sort: _react2["default"].PropTypes.bool,
	sortRelevance: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.keyValueMapOrArrayOfKeyValueMaps,
	values: _hireFormsPropTypes.arrayOfKeyValueMaps
};

exports["default"] = Options;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-prop-types":1,"hire-forms-utils":2,"react":"react","react-dom":"react-dom"}]},{},[3])(3)
});
},{"classnames":"classnames","hire-forms-prop-types":2,"hire-forms-utils":3,"react":"react","react-dom":"react-dom"}],2:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],3:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],4:[function(_dereq_,module,exports){
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

var _hireFormsOptions = _dereq_("hire-forms-options");

var _hireFormsOptions2 = _interopRequireDefault(_hireFormsOptions);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var Select = (function (_React$Component) {
	_inherits(Select, _React$Component);

	function Select(props) {
		_classCallCheck(this, Select);

		_get(Object.getPrototypeOf(Select.prototype), "constructor", this).call(this, props);

		this.handleDocumentClick = this.handleDocumentClick.bind(this);

		this.state = {
			options: props.options,
			visible: false
		};
	}

	_createClass(Select, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			var _this = this;

			document.addEventListener("click", this.handleDocumentClick, false);

			if (this.props.async != null) {
				this.props.async(function (response) {
					_this.setState({
						options: response
					});
				});
			}
		}
	}, {
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (nextProps.options && nextProps.options.length) {
				this.setState({
					options: nextProps.options
				});
			}
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			document.removeEventListener("click", this.handleDocumentClick, false);
		}
	}, {
		key: "handleDocumentClick",
		value: function handleDocumentClick(ev) {
			if (this.state.visible && !_reactDom2["default"].findDOMNode(this).contains(ev.target)) {
				this.setState({
					visible: false
				});
			}
		}
	}, {
		key: "handleInputClick",
		value: function handleInputClick(ev) {
			// Visible state shouldn't change when there are no options.
			if (this.state.options.length > 0) {
				this.setState({ visible: !this.state.visible });
			}
		}

		/**
   * @method
   * @param {object} value Map of key and value: {key: "somekey", value: "somevalue"}
   */
	}, {
		key: "handleOptionsChange",
		value: function handleOptionsChange(value) {
			this.setState({ visible: false });

			// If the options prop is an array of strings, return a string.
			if ((0, _hireFormsUtils.isListOfStrings)(this.state.options)) {
				value = value.value;
			}

			this.props.onChange(value);
		}
	}, {
		key: "render",
		value: function render() {
			var optionValues = undefined,
			    options = undefined;

			if (this.state.visible) {
				options = _react2["default"].createElement(_hireFormsOptions2["default"], {
					onChange: this.handleOptionsChange.bind(this),
					sort: this.props.sort,
					sortRelevance: this.props.sortRelevance,
					value: (0, _hireFormsUtils.castKeyValue)(this.props.value),
					values: (0, _hireFormsUtils.castKeyValueArray)(this.state.options) });
			}

			// If value prop is a key/value map, extract the value.
			var value = (0, _hireFormsUtils.isKeyValueMap)(this.props.value) ? this.props.value.value : this.props.value;

			// Create new var so we can check value in cx()
			var inputValue = value === "" ? this.props.placeholder : value;

			return _react2["default"].createElement(
				"div",
				{ className: "hire-select" },
				_react2["default"].createElement(
					"div",
					{
						className: "input-container",
						onClick: this.handleInputClick.bind(this) },
					_react2["default"].createElement(
						"div",
						{ className: (0, _classnames2["default"])({
								"input": true,
								"placeholder": value === "" }) },
						inputValue
					),
					_react2["default"].createElement(
						"button",
						null,
						"▾"
					)
				),
				options
			);
		}
	}]);

	return Select;
})(_react2["default"].Component);

Select.defaultProps = {
	options: [],
	value: ""
};

Select.propTypes = {
	async: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	options: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps,
	placeholder: _react2["default"].PropTypes.string,
	sort: _react2["default"].PropTypes.bool,
	sortRelevance: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.stringOrKeyValueMap
};

exports["default"] = Select;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-options":1,"hire-forms-prop-types":2,"hire-forms-utils":3,"react":"react","react-dom":"react-dom"}]},{},[4])(4)
});
},{}],4:[function(require,module,exports){
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

var _hireFormsList = require("hire-forms-list");

var _hireFormsList2 = _interopRequireDefault(_hireFormsList);

var _hireFormsSelect = require("hire-forms-select");

var _hireFormsSelect2 = _interopRequireDefault(_hireFormsSelect);

var _hireFormsPropTypes = require("hire-forms-prop-types");

var SelectList = (function (_React$Component) {
	_inherits(SelectList, _React$Component);

	function SelectList() {
		_classCallCheck(this, SelectList);

		_get(Object.getPrototypeOf(SelectList.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(SelectList, [{
		key: "handleListChange",
		value: function handleListChange(values) {
			this.props.onChange(values);
		}
	}, {
		key: "handleSelectChange",
		value: function handleSelectChange(value) {
			// Use concat instead of push so we don't alter the this.props.
			var values = this.props.values.concat([value]);

			this.props.onChange(values);
		}
	}, {
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				{ className: "hire-select-list" },
				_react2["default"].createElement(_hireFormsList2["default"], {
					onChange: this.handleListChange.bind(this),
					values: this.props.values }),
				_react2["default"].createElement(_hireFormsSelect2["default"], {
					async: this.props.async,
					onChange: this.handleSelectChange.bind(this),
					options: this.props.options,
					placeholder: this.props.placeholder })
			);
		}
	}]);

	return SelectList;
})(_react2["default"].Component);

SelectList.defaultProps = {
	values: [],
	options: [],
	ordered: false
};

SelectList.propTypes = {
	async: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	options: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps,
	ordered: _react2["default"].PropTypes.bool,
	placeholder: _react2["default"].PropTypes.string,
	values: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps
};

exports["default"] = SelectList;
module.exports = exports["default"];

},{"hire-forms-list":1,"hire-forms-prop-types":2,"hire-forms-select":3,"react":"react"}]},{},[4])(4)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"hire-forms-list":6,"hire-forms-prop-types":8,"hire-forms-select":10,"react":"react"}],10:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsSelect = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],2:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],3:[function(_dereq_,module,exports){
// TODO move listitem to seperate component (so we don't have to store data-key and data-value as attributes)
// Move util functions to seperate module

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

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var hasKeyValue = function hasKeyValue(list, item) {
	return list.filter(function (li) {
		return li.key === item.key;
	}).length > 0;
};

/**
 * Options are rendered beneath the autocomplete and select components.
 *
 * @class
 * @extends React.Component
 */

var Options = (function (_React$Component) {
	_inherits(Options, _React$Component);

	function Options() {
		_classCallCheck(this, Options);

		_get(Object.getPrototypeOf(Options.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Options, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			var node = _reactDom2["default"].findDOMNode(this);

			if (node) {
				node.style.zIndex = 1000;
			}
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			var node = _reactDom2["default"].findDOMNode(this);
			node.style.zIndex = 0;
		}

		/**
   * Sort values on relevance. A result is more relevant when the search
   * query is more at the beginning of the string. In other words:
   * String.indexOf(props.query): lower is better.
   * @param {Array<Object>} value An array of key/value maps
   * @param {String} query A search query
   * @returns {Array<Object>} Sorted values on relevance
   */
	}, {
		key: "sortRelevance",
		value: function sortRelevance(values, query) {
			return values.sort(function (a, b) {
				a = a.value.toLowerCase();
				b = b.value.toLowerCase();

				var indexA = a.indexOf(query);
				var indexB = b.indexOf(query);

				if (indexA > indexB) {
					return 1;
				}

				if (indexA < indexB) {
					return -1;
				}

				if (indexA === indexB) {
					if (a > b) {
						return 1;
					}

					if (a < b) {
						return -1;
					}
				}

				return 0;
			});
		}

		/*
   * highlight the currently highlighted option.
   *
   * @param {Object} target An HTMLElement or event object
   * @param {String} className Name of the highlight class
   */
	}, {
		key: "highlight",
		value: function highlight(target, className) {
			// Check if target is an event object.
			if (target.hasOwnProperty("currentTarget")) {
				target = target.currentTarget;
			}

			target.classList.add(className);
		}

		/**
   * Unhighlight the currently highlighted option.
   *
   * @param {String} className Name of the highlight class
   * @return {Object} The unhighlighted HTMLElement
   */
	}, {
		key: "unhighlight",
		value: function unhighlight(className) {
			var el = undefined;
			var node = _reactDom2["default"].findDOMNode(this);

			if (node) {
				el = node.querySelector("li." + className);

				if (el) {
					el.classList.remove(className);
				}
			}

			return el;
		}
	}, {
		key: "handleClick",
		value: function handleClick(ev) {
			this.props.onChange(this.getOptionData(ev.currentTarget));
		}
	}, {
		key: "highlightPrev",
		value: function highlightPrev() {
			var prev = undefined;
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				prev = current.previousElementSibling;
			}

			// If current and prev aren't found, start at the top.
			// Current is not found if there is no list item highlighted.
			// Prev is not found if the first list item is highlighted.
			if (!prev) {
				prev = _reactDom2["default"].findDOMNode(this).lastChild;
			}

			this.highlight(prev, this.props.highlightClass);
		}
	}, {
		key: "highlightNext",
		value: function highlightNext() {
			var next = undefined;
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				next = current.nextElementSibling;
			}

			// If current and next aren't found, start at the top.
			// Current is not found if there is no list item highlighted.
			// Next is not found if the last list item is highlighted.
			if (!next) {
				next = _reactDom2["default"].findDOMNode(this).firstChild;
			}

			this.highlight(next, this.props.highlightClass);
		}
	}, {
		key: "select",
		value: function select() {
			var current = this.unhighlight(this.props.highlightClass);

			if (current) {
				this.props.onChange(this.getOptionData(current));
			}
		}

		/**
   * Get the key (id) and value (display name) of an option DOM element.
   *
   * @param {Object} el - Option DOM element
   * @returns {Object}
   */
	}, {
		key: "getOptionData",
		value: function getOptionData(el) {
			return {
				key: el.getAttribute("data-key"),
				value: el.getAttribute("data-value")
			};
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			if (this.props.values.length === 0) {
				return null;
			}

			var values = this.props.sort || this.props.sortRelevance && this.props.query !== "" ? this.sortRelevance(this.props.values, this.props.querySelector) : this.props.values;

			var listitems = values.map(function (data, index) {
				var displayValue = data.value;

				if (_this.props.query.length) {
					var re = new RegExp(_this.props.query, "ig");
					displayValue = data.value.replace(re, "<span class=\"highlight\">$&</span>");
				}

				return _react2["default"].createElement("li", {
					className: (0, _classnames2["default"])({
						"hire-forms-option": true,
						selected: hasKeyValue((0, _hireFormsUtils.castArray)(_this.props.value), data)
					}),
					dangerouslySetInnerHTML: { __html: displayValue },
					"data-key": data.key,
					"data-value": data.value,
					key: index,
					onClick: _this.handleClick.bind(_this) });
			});

			return _react2["default"].createElement(
				"ul",
				{
					className: "hire-options" },
				listitems
			);
		}
	}]);

	return Options;
})(_react2["default"].Component);

Options.defaultProps = {
	highlightClass: "highlight",
	query: "",
	sort: false,
	sortRelevance: true,
	value: { key: "", value: "" },
	values: []
};

Options.propTypes = {
	highlightClass: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func.isRequired,
	query: _react2["default"].PropTypes.string,
	sort: _react2["default"].PropTypes.bool,
	sortRelevance: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.keyValueMapOrArrayOfKeyValueMaps,
	values: _hireFormsPropTypes.arrayOfKeyValueMaps
};

exports["default"] = Options;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-prop-types":1,"hire-forms-utils":2,"react":"react","react-dom":"react-dom"}]},{},[3])(3)
});
},{"classnames":"classnames","hire-forms-prop-types":2,"hire-forms-utils":3,"react":"react","react-dom":"react-dom"}],2:[function(_dereq_,module,exports){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsOptions = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof _dereq_=="function"&&_dereq_;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof _dereq_=="function"&&_dereq_;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = _dereq_("react");

var _react2 = _interopRequireDefault(_react);

var keyValueMap = _react2["default"].PropTypes.shape({
	key: _react2["default"].PropTypes.string.isRequired,
	value: _react2["default"].PropTypes.string.isRequired
});

exports.keyValueMap = keyValueMap;
// ARRAY OF

var arrayOfKeyValueMaps = _react2["default"].PropTypes.arrayOf(keyValueMap);

exports.arrayOfKeyValueMaps = arrayOfKeyValueMaps;
var arrayOfStrings = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.string);

exports.arrayOfStrings = arrayOfStrings;
var arrayOfElements = _react2["default"].PropTypes.arrayOf(_react2["default"].PropTypes.element);

exports.arrayOfElements = arrayOfElements;
// OR

var stringOrArray = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, _react2["default"].PropTypes.array]);

exports.stringOrArray = stringOrArray;
var stringOrKeyValueMap = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, keyValueMap]);

exports.stringOrKeyValueMap = stringOrKeyValueMap;
var stringOrArrayOfStrings = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.string, arrayOfStrings]);

exports.stringOrArrayOfStrings = stringOrArrayOfStrings;
var elementOrArrayOfElement = _react2["default"].PropTypes.oneOfType([_react2["default"].PropTypes.element, arrayOfElements]);

exports.elementOrArrayOfElement = elementOrArrayOfElement;
var arrayOfStringsOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([arrayOfStrings, arrayOfKeyValueMaps]);

exports.arrayOfStringsOrArrayOfKeyValueMaps = arrayOfStringsOrArrayOfKeyValueMaps;
var keyValueMapOrArrayOfKeyValueMaps = _react2["default"].PropTypes.oneOfType([keyValueMap, arrayOfKeyValueMaps]);
exports.keyValueMapOrArrayOfKeyValueMaps = keyValueMapOrArrayOfKeyValueMaps;

},{"react":"react"}]},{},[1])(1)
});
},{"react":"react"}],3:[function(_dereq_,module,exports){

/*
 * @param {Array} list
 * @returns {Boolean}
 */
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isListOfStrings = isListOfStrings;
exports.isKeyValueMap = isKeyValueMap;
exports.castArray = castArray;
exports.castKeyValue = castKeyValue;
exports.castKeyValueArray = castKeyValueArray;

function isListOfStrings(list) {
  if (!Array.isArray(list) || !list.length) {
    return false;
  }

  return list.every(function (item) {
    return typeof item === "string";
  });
}

/*
 * @param {Object} map
 * @returns {Boolean}
 */

function isKeyValueMap(map) {
  if (map == null) {
    return false;
  }

  return map.hasOwnProperty("key") && map.hasOwnProperty("value");
}

/*
 * Always return an array.
 *
 * @param {String|Array} arr
 * @returns {Array}
 */

function castArray(arr) {
  return Array.isArray(arr) ? arr : [arr];
}

;

/*
 * Always return a key/value map.
 *
 * @param {Number|String|Boolean|Object} item
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValue(item) {
  return isKeyValueMap(item) ? item : {
    key: item,
    value: item
  };
}

/*
 * Always return an array of key/value maps.
 *
 * @param {Number|String|Boolean|Array|Object} list
 * @returns {Array} Array of key value maps, ie: [{key: "A", value: "A"}, {key: "B", value: "B"}, ...]
 */

function castKeyValueArray(list) {
  list = castArray(list);

  return list.map(castKeyValue);
}

},{}],4:[function(_dereq_,module,exports){
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

var _hireFormsOptions = _dereq_("hire-forms-options");

var _hireFormsOptions2 = _interopRequireDefault(_hireFormsOptions);

var _hireFormsPropTypes = _dereq_("hire-forms-prop-types");

var _hireFormsUtils = _dereq_("hire-forms-utils");

var Select = (function (_React$Component) {
	_inherits(Select, _React$Component);

	function Select(props) {
		_classCallCheck(this, Select);

		_get(Object.getPrototypeOf(Select.prototype), "constructor", this).call(this, props);

		this.handleDocumentClick = this.handleDocumentClick.bind(this);

		this.state = {
			options: props.options,
			visible: false
		};
	}

	_createClass(Select, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			var _this = this;

			document.addEventListener("click", this.handleDocumentClick, false);

			if (this.props.async != null) {
				this.props.async(function (response) {
					_this.setState({
						options: response
					});
				});
			}
		}
	}, {
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (nextProps.options && nextProps.options.length) {
				this.setState({
					options: nextProps.options
				});
			}
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			document.removeEventListener("click", this.handleDocumentClick, false);
		}
	}, {
		key: "handleDocumentClick",
		value: function handleDocumentClick(ev) {
			if (this.state.visible && !_reactDom2["default"].findDOMNode(this).contains(ev.target)) {
				this.setState({
					visible: false
				});
			}
		}
	}, {
		key: "handleInputClick",
		value: function handleInputClick(ev) {
			// Visible state shouldn't change when there are no options.
			if (this.state.options.length > 0) {
				this.setState({ visible: !this.state.visible });
			}
		}

		/**
   * @method
   * @param {object} value Map of key and value: {key: "somekey", value: "somevalue"}
   */
	}, {
		key: "handleOptionsChange",
		value: function handleOptionsChange(value) {
			this.setState({ visible: false });

			// If the options prop is an array of strings, return a string.
			if ((0, _hireFormsUtils.isListOfStrings)(this.state.options)) {
				value = value.value;
			}

			this.props.onChange(value);
		}
	}, {
		key: "render",
		value: function render() {
			var optionValues = undefined,
			    options = undefined;

			if (this.state.visible) {
				options = _react2["default"].createElement(_hireFormsOptions2["default"], {
					onChange: this.handleOptionsChange.bind(this),
					sort: this.props.sort,
					sortRelevance: this.props.sortRelevance,
					value: (0, _hireFormsUtils.castKeyValue)(this.props.value),
					values: (0, _hireFormsUtils.castKeyValueArray)(this.state.options) });
			}

			// If value prop is a key/value map, extract the value.
			var value = (0, _hireFormsUtils.isKeyValueMap)(this.props.value) ? this.props.value.value : this.props.value;

			// Create new var so we can check value in cx()
			var inputValue = value === "" ? this.props.placeholder : value;

			return _react2["default"].createElement(
				"div",
				{ className: "hire-select" },
				_react2["default"].createElement(
					"div",
					{
						className: "input-container",
						onClick: this.handleInputClick.bind(this) },
					_react2["default"].createElement(
						"div",
						{ className: (0, _classnames2["default"])({
								"input": true,
								"placeholder": value === "" }) },
						inputValue
					),
					_react2["default"].createElement(
						"button",
						null,
						"▾"
					)
				),
				options
			);
		}
	}]);

	return Select;
})(_react2["default"].Component);

Select.defaultProps = {
	options: [],
	value: ""
};

Select.propTypes = {
	async: _react2["default"].PropTypes.func,
	onChange: _react2["default"].PropTypes.func.isRequired,
	options: _hireFormsPropTypes.arrayOfStringsOrArrayOfKeyValueMaps,
	placeholder: _react2["default"].PropTypes.string,
	sort: _react2["default"].PropTypes.bool,
	sortRelevance: _react2["default"].PropTypes.bool,
	value: _hireFormsPropTypes.stringOrKeyValueMap
};

exports["default"] = Select;
module.exports = exports["default"];

},{"classnames":"classnames","hire-forms-options":1,"hire-forms-prop-types":2,"hire-forms-utils":3,"react":"react","react-dom":"react-dom"}]},{},[4])(4)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],11:[function(require,module,exports){
(function (global){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsTextarea = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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

var Textarea = (function (_React$Component) {
	_inherits(Textarea, _React$Component);

	_createClass(Textarea, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			this.adjustHeight(_reactDom2["default"].findDOMNode(this));
		}
	}, {
		key: "shouldComponentUpdate",
		value: function shouldComponentUpdate(nextProps, nextState) {
			var propsValueChange = this.props.value !== nextProps.value;
			var stateFocusChange = this.state.focus !== nextState.focus;

			return propsValueChange || stateFocusChange;
		}
	}, {
		key: "componentDidUpdate",
		value: function componentDidUpdate(prevProps) {
			if (this.props.value !== prevProps.value) {
				this.adjustHeight();
			}
		}
	}]);

	function Textarea(props) {
		_classCallCheck(this, Textarea);

		_get(Object.getPrototypeOf(Textarea.prototype), "constructor", this).call(this, props);

		this.state = { focus: false };
	}

	_createClass(Textarea, [{
		key: "adjustHeight",
		value: function adjustHeight() {
			var textarea = _reactDom2["default"].findDOMNode(this);

			textarea.style.height = "auto";
			textarea.style.height = textarea.scrollHeight + 6 > 32 ? textarea.scrollHeight + 6 + "px" : "32px";
		}
	}, {
		key: "toggleFocus",
		value: function toggleFocus() {
			this.setState({ focus: !this.state.focus });
		}
	}, {
		key: "handleKeyDown",
		value: function handleKeyDown(ev) {
			if (this.props.onKeyDown) {
				this.props.onKeyDown(ev);
			}
		}
	}, {
		key: "handleKeyUp",
		value: function handleKeyUp(ev) {
			if (this.props.onKeyUp) {
				this.props.onKeyUp(ev);
			}
		}
	}, {
		key: "handleChange",
		value: function handleChange(ev) {
			this.props.onChange(ev.currentTarget.value, ev);
		}
	}, {
		key: "render",
		value: function render() {
			return _react2["default"].createElement("textarea", {
				className: (0, _classnames2["default"])("hire-textarea", { focus: this.state.focus }),
				onBlur: this.toggleFocus.bind(this),
				onChange: this.handleChange.bind(this),
				onFocus: this.toggleFocus.bind(this),
				onKeyDown: this.handleKeyDown.bind(this),
				onKeyUp: this.handleKeyUp.bind(this),
				placeholder: this.props.placeholder,
				style: this.props.style,
				value: this.props.value });
		}
	}]);

	return Textarea;
})(_react2["default"].Component);

Textarea.defaultProps = {
	autoResize: true,
	value: ""
};

Textarea.propTypes = {
	autoResize: _react2["default"].PropTypes.bool,
	onChange: _react2["default"].PropTypes.func,
	onKeyDown: _react2["default"].PropTypes.func,
	onKeyUp: _react2["default"].PropTypes.func,
	placeholder: _react2["default"].PropTypes.string,
	style: _react2["default"].PropTypes.object,
	value: _react2["default"].PropTypes.string
};

exports["default"] = Textarea;
module.exports = exports["default"];

},{"classnames":"classnames","react":"react","react-dom":"react-dom"}]},{},[1])(1)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"classnames":"classnames","react":"react","react-dom":"react-dom"}],12:[function(require,module,exports){
(function (global,Buffer){
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.HireFormsLogin = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
// Copyright Joyent, Inc. and other Node contributors.
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.

function EventEmitter() {
  this._events = this._events || {};
  this._maxListeners = this._maxListeners || undefined;
}
module.exports = EventEmitter;

// Backwards-compat with node 0.10.x
EventEmitter.EventEmitter = EventEmitter;

EventEmitter.prototype._events = undefined;
EventEmitter.prototype._maxListeners = undefined;

// By default EventEmitters will print a warning if more than 10 listeners are
// added to it. This is a useful default which helps finding memory leaks.
EventEmitter.defaultMaxListeners = 10;

// Obviously not all Emitters should be limited to 10. This function allows
// that to be increased. Set to zero for unlimited.
EventEmitter.prototype.setMaxListeners = function(n) {
  if (!isNumber(n) || n < 0 || isNaN(n))
    throw TypeError('n must be a positive number');
  this._maxListeners = n;
  return this;
};

EventEmitter.prototype.emit = function(type) {
  var er, handler, len, args, i, listeners;

  if (!this._events)
    this._events = {};

  // If there is no 'error' event listener then throw.
  if (type === 'error') {
    if (!this._events.error ||
        (isObject(this._events.error) && !this._events.error.length)) {
      er = arguments[1];
      if (er instanceof Error) {
        throw er; // Unhandled 'error' event
      }
      throw TypeError('Uncaught, unspecified "error" event.');
    }
  }

  handler = this._events[type];

  if (isUndefined(handler))
    return false;

  if (isFunction(handler)) {
    switch (arguments.length) {
      // fast cases
      case 1:
        handler.call(this);
        break;
      case 2:
        handler.call(this, arguments[1]);
        break;
      case 3:
        handler.call(this, arguments[1], arguments[2]);
        break;
      // slower
      default:
        args = Array.prototype.slice.call(arguments, 1);
        handler.apply(this, args);
    }
  } else if (isObject(handler)) {
    args = Array.prototype.slice.call(arguments, 1);
    listeners = handler.slice();
    len = listeners.length;
    for (i = 0; i < len; i++)
      listeners[i].apply(this, args);
  }

  return true;
};

EventEmitter.prototype.addListener = function(type, listener) {
  var m;

  if (!isFunction(listener))
    throw TypeError('listener must be a function');

  if (!this._events)
    this._events = {};

  // To avoid recursion in the case that type === "newListener"! Before
  // adding it to the listeners, first emit "newListener".
  if (this._events.newListener)
    this.emit('newListener', type,
              isFunction(listener.listener) ?
              listener.listener : listener);

  if (!this._events[type])
    // Optimize the case of one listener. Don't need the extra array object.
    this._events[type] = listener;
  else if (isObject(this._events[type]))
    // If we've already got an array, just append.
    this._events[type].push(listener);
  else
    // Adding the second element, need to change to array.
    this._events[type] = [this._events[type], listener];

  // Check for listener leak
  if (isObject(this._events[type]) && !this._events[type].warned) {
    if (!isUndefined(this._maxListeners)) {
      m = this._maxListeners;
    } else {
      m = EventEmitter.defaultMaxListeners;
    }

    if (m && m > 0 && this._events[type].length > m) {
      this._events[type].warned = true;
      console.error('(node) warning: possible EventEmitter memory ' +
                    'leak detected. %d listeners added. ' +
                    'Use emitter.setMaxListeners() to increase limit.',
                    this._events[type].length);
      if (typeof console.trace === 'function') {
        // not supported in IE 10
        console.trace();
      }
    }
  }

  return this;
};

EventEmitter.prototype.on = EventEmitter.prototype.addListener;

EventEmitter.prototype.once = function(type, listener) {
  if (!isFunction(listener))
    throw TypeError('listener must be a function');

  var fired = false;

  function g() {
    this.removeListener(type, g);

    if (!fired) {
      fired = true;
      listener.apply(this, arguments);
    }
  }

  g.listener = listener;
  this.on(type, g);

  return this;
};

// emits a 'removeListener' event iff the listener was removed
EventEmitter.prototype.removeListener = function(type, listener) {
  var list, position, length, i;

  if (!isFunction(listener))
    throw TypeError('listener must be a function');

  if (!this._events || !this._events[type])
    return this;

  list = this._events[type];
  length = list.length;
  position = -1;

  if (list === listener ||
      (isFunction(list.listener) && list.listener === listener)) {
    delete this._events[type];
    if (this._events.removeListener)
      this.emit('removeListener', type, listener);

  } else if (isObject(list)) {
    for (i = length; i-- > 0;) {
      if (list[i] === listener ||
          (list[i].listener && list[i].listener === listener)) {
        position = i;
        break;
      }
    }

    if (position < 0)
      return this;

    if (list.length === 1) {
      list.length = 0;
      delete this._events[type];
    } else {
      list.splice(position, 1);
    }

    if (this._events.removeListener)
      this.emit('removeListener', type, listener);
  }

  return this;
};

EventEmitter.prototype.removeAllListeners = function(type) {
  var key, listeners;

  if (!this._events)
    return this;

  // not listening for removeListener, no need to emit
  if (!this._events.removeListener) {
    if (arguments.length === 0)
      this._events = {};
    else if (this._events[type])
      delete this._events[type];
    return this;
  }

  // emit removeListener for all listeners on all events
  if (arguments.length === 0) {
    for (key in this._events) {
      if (key === 'removeListener') continue;
      this.removeAllListeners(key);
    }
    this.removeAllListeners('removeListener');
    this._events = {};
    return this;
  }

  listeners = this._events[type];

  if (isFunction(listeners)) {
    this.removeListener(type, listeners);
  } else if (listeners) {
    // LIFO order
    while (listeners.length)
      this.removeListener(type, listeners[listeners.length - 1]);
  }
  delete this._events[type];

  return this;
};

EventEmitter.prototype.listeners = function(type) {
  var ret;
  if (!this._events || !this._events[type])
    ret = [];
  else if (isFunction(this._events[type]))
    ret = [this._events[type]];
  else
    ret = this._events[type].slice();
  return ret;
};

EventEmitter.prototype.listenerCount = function(type) {
  if (this._events) {
    var evlistener = this._events[type];

    if (isFunction(evlistener))
      return 1;
    else if (evlistener)
      return evlistener.length;
  }
  return 0;
};

EventEmitter.listenerCount = function(emitter, type) {
  return emitter.listenerCount(type);
};

function isFunction(arg) {
  return typeof arg === 'function';
}

function isNumber(arg) {
  return typeof arg === 'number';
}

function isObject(arg) {
  return typeof arg === 'object' && arg !== null;
}

function isUndefined(arg) {
  return arg === void 0;
}

},{}],2:[function(_dereq_,module,exports){
/**
 * Copyright (c) 2014-2015, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

module.exports.Dispatcher = _dereq_('./lib/Dispatcher')

},{"./lib/Dispatcher":3}],3:[function(_dereq_,module,exports){
/*
 * Copyright (c) 2014, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * @providesModule Dispatcher
 * @typechecks
 */

"use strict";

var invariant = _dereq_('./invariant');

var _lastID = 1;
var _prefix = 'ID_';

/**
 * Dispatcher is used to broadcast payloads to registered callbacks. This is
 * different from generic pub-sub systems in two ways:
 *
 *   1) Callbacks are not subscribed to particular events. Every payload is
 *      dispatched to every registered callback.
 *   2) Callbacks can be deferred in whole or part until other callbacks have
 *      been executed.
 *
 * For example, consider this hypothetical flight destination form, which
 * selects a default city when a country is selected:
 *
 *   var flightDispatcher = new Dispatcher();
 *
 *   // Keeps track of which country is selected
 *   var CountryStore = {country: null};
 *
 *   // Keeps track of which city is selected
 *   var CityStore = {city: null};
 *
 *   // Keeps track of the base flight price of the selected city
 *   var FlightPriceStore = {price: null}
 *
 * When a user changes the selected city, we dispatch the payload:
 *
 *   flightDispatcher.dispatch({
 *     actionType: 'city-update',
 *     selectedCity: 'paris'
 *   });
 *
 * This payload is digested by `CityStore`:
 *
 *   flightDispatcher.register(function(payload) {
 *     if (payload.actionType === 'city-update') {
 *       CityStore.city = payload.selectedCity;
 *     }
 *   });
 *
 * When the user selects a country, we dispatch the payload:
 *
 *   flightDispatcher.dispatch({
 *     actionType: 'country-update',
 *     selectedCountry: 'australia'
 *   });
 *
 * This payload is digested by both stores:
 *
 *    CountryStore.dispatchToken = flightDispatcher.register(function(payload) {
 *     if (payload.actionType === 'country-update') {
 *       CountryStore.country = payload.selectedCountry;
 *     }
 *   });
 *
 * When the callback to update `CountryStore` is registered, we save a reference
 * to the returned token. Using this token with `waitFor()`, we can guarantee
 * that `CountryStore` is updated before the callback that updates `CityStore`
 * needs to query its data.
 *
 *   CityStore.dispatchToken = flightDispatcher.register(function(payload) {
 *     if (payload.actionType === 'country-update') {
 *       // `CountryStore.country` may not be updated.
 *       flightDispatcher.waitFor([CountryStore.dispatchToken]);
 *       // `CountryStore.country` is now guaranteed to be updated.
 *
 *       // Select the default city for the new country
 *       CityStore.city = getDefaultCityForCountry(CountryStore.country);
 *     }
 *   });
 *
 * The usage of `waitFor()` can be chained, for example:
 *
 *   FlightPriceStore.dispatchToken =
 *     flightDispatcher.register(function(payload) {
 *       switch (payload.actionType) {
 *         case 'country-update':
 *           flightDispatcher.waitFor([CityStore.dispatchToken]);
 *           FlightPriceStore.price =
 *             getFlightPriceStore(CountryStore.country, CityStore.city);
 *           break;
 *
 *         case 'city-update':
 *           FlightPriceStore.price =
 *             FlightPriceStore(CountryStore.country, CityStore.city);
 *           break;
 *     }
 *   });
 *
 * The `country-update` payload will be guaranteed to invoke the stores'
 * registered callbacks in order: `CountryStore`, `CityStore`, then
 * `FlightPriceStore`.
 */

  function Dispatcher() {
    this.$Dispatcher_callbacks = {};
    this.$Dispatcher_isPending = {};
    this.$Dispatcher_isHandled = {};
    this.$Dispatcher_isDispatching = false;
    this.$Dispatcher_pendingPayload = null;
  }

  /**
   * Registers a callback to be invoked with every dispatched payload. Returns
   * a token that can be used with `waitFor()`.
   *
   * @param {function} callback
   * @return {string}
   */
  Dispatcher.prototype.register=function(callback) {
    var id = _prefix + _lastID++;
    this.$Dispatcher_callbacks[id] = callback;
    return id;
  };

  /**
   * Removes a callback based on its token.
   *
   * @param {string} id
   */
  Dispatcher.prototype.unregister=function(id) {
    invariant(
      this.$Dispatcher_callbacks[id],
      'Dispatcher.unregister(...): `%s` does not map to a registered callback.',
      id
    );
    delete this.$Dispatcher_callbacks[id];
  };

  /**
   * Waits for the callbacks specified to be invoked before continuing execution
   * of the current callback. This method should only be used by a callback in
   * response to a dispatched payload.
   *
   * @param {array<string>} ids
   */
  Dispatcher.prototype.waitFor=function(ids) {
    invariant(
      this.$Dispatcher_isDispatching,
      'Dispatcher.waitFor(...): Must be invoked while dispatching.'
    );
    for (var ii = 0; ii < ids.length; ii++) {
      var id = ids[ii];
      if (this.$Dispatcher_isPending[id]) {
        invariant(
          this.$Dispatcher_isHandled[id],
          'Dispatcher.waitFor(...): Circular dependency detected while ' +
          'waiting for `%s`.',
          id
        );
        continue;
      }
      invariant(
        this.$Dispatcher_callbacks[id],
        'Dispatcher.waitFor(...): `%s` does not map to a registered callback.',
        id
      );
      this.$Dispatcher_invokeCallback(id);
    }
  };

  /**
   * Dispatches a payload to all registered callbacks.
   *
   * @param {object} payload
   */
  Dispatcher.prototype.dispatch=function(payload) {
    invariant(
      !this.$Dispatcher_isDispatching,
      'Dispatch.dispatch(...): Cannot dispatch in the middle of a dispatch.'
    );
    this.$Dispatcher_startDispatching(payload);
    try {
      for (var id in this.$Dispatcher_callbacks) {
        if (this.$Dispatcher_isPending[id]) {
          continue;
        }
        this.$Dispatcher_invokeCallback(id);
      }
    } finally {
      this.$Dispatcher_stopDispatching();
    }
  };

  /**
   * Is this Dispatcher currently dispatching.
   *
   * @return {boolean}
   */
  Dispatcher.prototype.isDispatching=function() {
    return this.$Dispatcher_isDispatching;
  };

  /**
   * Call the callback stored with the given id. Also do some internal
   * bookkeeping.
   *
   * @param {string} id
   * @internal
   */
  Dispatcher.prototype.$Dispatcher_invokeCallback=function(id) {
    this.$Dispatcher_isPending[id] = true;
    this.$Dispatcher_callbacks[id](this.$Dispatcher_pendingPayload);
    this.$Dispatcher_isHandled[id] = true;
  };

  /**
   * Set up bookkeeping needed when dispatching.
   *
   * @param {object} payload
   * @internal
   */
  Dispatcher.prototype.$Dispatcher_startDispatching=function(payload) {
    for (var id in this.$Dispatcher_callbacks) {
      this.$Dispatcher_isPending[id] = false;
      this.$Dispatcher_isHandled[id] = false;
    }
    this.$Dispatcher_pendingPayload = payload;
    this.$Dispatcher_isDispatching = true;
  };

  /**
   * Clear bookkeeping used for dispatching.
   *
   * @internal
   */
  Dispatcher.prototype.$Dispatcher_stopDispatching=function() {
    this.$Dispatcher_pendingPayload = null;
    this.$Dispatcher_isDispatching = false;
  };


module.exports = Dispatcher;

},{"./invariant":4}],4:[function(_dereq_,module,exports){
/**
 * Copyright (c) 2014, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * @providesModule invariant
 */

"use strict";

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
  if (false) {
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
        'Invariant Violation: ' +
        format.replace(/%s/g, function() { return args[argIndex++]; })
      );
    }

    error.framesToPop = 1; // we don't care about invariant's own frame
    throw error;
  }
};

module.exports = invariant;

},{}],5:[function(_dereq_,module,exports){
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

},{"is-function":8}],6:[function(_dereq_,module,exports){
if (typeof window !== "undefined") {
    module.exports = window;
} else if (typeof global !== "undefined") {
    module.exports = global;
} else if (typeof self !== "undefined"){
    module.exports = self;
} else {
    module.exports = {};
}

},{}],7:[function(_dereq_,module,exports){
var inserted = {};

module.exports = function (css, options) {
    if (inserted[css]) return;
    inserted[css] = true;
    
    var elem = document.createElement('style');
    elem.setAttribute('type', 'text/css');

    if ('textContent' in elem) {
      elem.textContent = css;
    } else {
      elem.styleSheet.cssText = css;
    }
    
    var head = document.getElementsByTagName('head')[0];
    if (options && options.prepend) {
        head.insertBefore(elem, head.childNodes[0]);
    } else {
        head.appendChild(elem);
    }
};

},{}],8:[function(_dereq_,module,exports){
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

},{}],9:[function(_dereq_,module,exports){
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
},{"for-each":5,"trim":10}],10:[function(_dereq_,module,exports){

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

},{}],11:[function(_dereq_,module,exports){
"use strict";
var window = _dereq_("global/window")
var once = _dereq_("once")
var parseHeaders = _dereq_("parse-headers")



module.exports = createXHR
createXHR.XMLHttpRequest = window.XMLHttpRequest || noop
createXHR.XDomainRequest = "withCredentials" in (new createXHR.XMLHttpRequest()) ? createXHR.XMLHttpRequest : window.XDomainRequest


function isEmpty(obj){
    for(var i in obj){
        if(obj.hasOwnProperty(i)) return false
    }
    return true
}

function createXHR(options, callback) {
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

    if (typeof options === "string") {
        options = { uri: options }
    }

    options = options || {}
    if(typeof callback === "undefined"){
        throw new Error("callback argument missing")
    }
    callback = once(callback)

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

},{"global/window":6,"once":12,"parse-headers":9}],12:[function(_dereq_,module,exports){
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

},{}],13:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _dispatcher = _dereq_("./dispatcher");

var _dispatcher2 = _interopRequireDefault(_dispatcher);

exports["default"] = {
	logout: function logout() {
		_dispatcher2["default"].handleViewAction({ actionType: "LOGOUT" });
	},

	receiveBasicLogin: function receiveBasicLogin(err, resp, body) {
		if (resp.statusCode >= 400) {
			_dispatcher2["default"].handleServerAction({
				actionType: "BASIC_LOGIN_FAILURE",
				data: resp
			});
		} else if (resp.statusCode >= 200 && resp.statusCode < 300) {
			_dispatcher2["default"].handleServerAction({
				actionType: "BASIC_LOGIN_SUCCESS",
				data: resp
			});
		}
	},

	receiveUserData: function receiveUserData(err, resp, body) {
		if (resp.statusCode >= 400) {
			_dispatcher2["default"].handleServerAction({
				actionType: "USER_DATA_FAILURE",
				data: resp
			});
		} else if (resp.statusCode >= 200 && resp.statusCode < 300) {
			_dispatcher2["default"].handleServerAction({
				actionType: "USER_DATA_SUCCESS",
				data: resp
			});
		}
	}
};
module.exports = exports["default"];

},{"./dispatcher":16}],14:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _xhr = _dereq_("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _dispatcher = _dereq_("./dispatcher");

var _dispatcher2 = _interopRequireDefault(_dispatcher);

var _actions = _dereq_("./actions");

var _actions2 = _interopRequireDefault(_actions);

exports["default"] = {
	performXhr: function performXhr(opts, callback) {
		(0, _xhr2["default"])(opts, callback);
	},

	basicLogin: function basicLogin(url, username, password) {
		this.performXhr({
			method: 'POST',
			uri: url,
			headers: {
				Accept: "application/json",
				Authorization: 'Basic ' + btoa(username + ':' + password)
			}
		}, _actions2["default"].receiveBasicLogin);
	},

	fetchUserData: function fetchUserData(url, token, optHeaders) {
		var headers = optHeaders || {};
		_extends(headers, {
			Accept: "application/json",
			Authorization: token
		});

		this.performXhr({
			method: 'GET',
			uri: url,
			headers: headers
		}, _actions2["default"].receiveUserData);
	}
};
module.exports = exports["default"];

},{"./actions":13,"./dispatcher":16,"xhr":11}],15:[function(_dereq_,module,exports){
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

var _loginStore = _dereq_("./login-store");

var _loginStore2 = _interopRequireDefault(_loginStore);

var _api = _dereq_("./api");

var _api2 = _interopRequireDefault(_api);

var Basic = (function (_React$Component) {
	_inherits(Basic, _React$Component);

	function Basic(props) {
		_classCallCheck(this, Basic);

		_get(Object.getPrototypeOf(Basic.prototype), "constructor", this).call(this, props);
		this.state = {
			username: "",
			password: ""
		};
	}

	_createClass(Basic, [{
		key: "onUserChange",
		value: function onUserChange(ev) {
			this.setState({ username: ev.target.value });
		}
	}, {
		key: "onPasswordChange",
		value: function onPasswordChange(ev) {
			this.setState({ password: ev.target.value });
		}
	}, {
		key: "onBasicLoginClick",
		value: function onBasicLoginClick(ev) {
			_api2["default"].basicLogin(this.props.url, this.state.username, this.state.password);
		}
	}, {
		key: "onKeyDown",
		value: function onKeyDown(ev) {
			if (ev.keyCode === 13) {
				this.onBasicLoginClick();
			}
		}
	}, {
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				{ className: "login-sub-component" },
				_react2["default"].createElement("input", {
					onChange: this.onUserChange.bind(this),
					onKeyDown: this.onKeyDown.bind(this),
					placeholder: this.props.userPlaceholder,
					type: "text",
					value: this.state.username }),
				_react2["default"].createElement("input", { onChange: this.onPasswordChange.bind(this),
					onKeyDown: this.onKeyDown.bind(this),
					placeholder: this.props.passwordPlaceholder,
					type: "password",
					value: this.state.password }),
				_react2["default"].createElement(
					"button",
					{ onClick: this.onBasicLoginClick.bind(this) },
					this.props.label
				)
			);
		}
	}]);

	return Basic;
})(_react2["default"].Component);

Basic.propTypes = {
	label: _react2["default"].PropTypes.string,
	passwordPlaceholder: _react2["default"].PropTypes.string,
	url: _react2["default"].PropTypes.string.isRequired,
	userPlaceholder: _react2["default"].PropTypes.string
};

Basic.defaultProps = {
	label: "Basic Login",
	userPlaceholder: "Username or email address",
	passwordPlaceholder: "Password"
};

exports["default"] = Basic;
module.exports = exports["default"];

},{"./api":14,"./login-store":19,"react":"react"}],16:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _flux = _dereq_("flux");

var LoginDispatcher = (function (_Dispatcher) {
	_inherits(LoginDispatcher, _Dispatcher);

	function LoginDispatcher() {
		_classCallCheck(this, LoginDispatcher);

		_get(Object.getPrototypeOf(LoginDispatcher.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(LoginDispatcher, [{
		key: "handleServerAction",
		value: function handleServerAction(action) {
			return this.dispatch({
				source: "SERVER_ACTION",
				action: action
			});
		}
	}, {
		key: "handleViewAction",
		value: function handleViewAction(action) {
			return this.dispatch({
				source: "VIEW_ACTION",
				action: action
			});
		}
	}]);

	return LoginDispatcher;
})(_flux.Dispatcher);

exports["default"] = new LoginDispatcher();
module.exports = exports["default"];

},{"flux":2}],17:[function(_dereq_,module,exports){
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

var _loginStore = _dereq_("./login-store");

var _loginStore2 = _interopRequireDefault(_loginStore);

var Federated = (function (_React$Component) {
	_inherits(Federated, _React$Component);

	function Federated(props) {
		_classCallCheck(this, Federated);

		_get(Object.getPrototypeOf(Federated.prototype), "constructor", this).call(this, props);
	}

	_createClass(Federated, [{
		key: "render",
		value: function render() {
			var hsURL = window.location.href;

			return _react2["default"].createElement(
				"form",
				{ className: "login-sub-component",
					action: this.props.url,
					method: "POST" },
				_react2["default"].createElement("input", { name: "hsurl", type: "hidden", value: hsURL }),
				_react2["default"].createElement(
					"button",
					{ type: "submit" },
					this.props.label
				)
			);
		}
	}]);

	return Federated;
})(_react2["default"].Component);

Federated.propTypes = {
	label: _react2["default"].PropTypes.string,
	url: _react2["default"].PropTypes.string.isRequired
};

Federated.defaultProps = {
	label: "Federated Login"
};

exports["default"] = Federated;
module.exports = exports["default"];

},{"./login-store":19,"react":"react"}],18:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _login = _dereq_("./login");

var _login2 = _interopRequireDefault(_login);

var _federated = _dereq_("./federated");

var _federated2 = _interopRequireDefault(_federated);

var _basic = _dereq_("./basic");

var _basic2 = _interopRequireDefault(_basic);

var _insertCss = _dereq_("insert-css");

var _insertCss2 = _interopRequireDefault(_insertCss);


var css = Buffer("LmhpcmUtbG9naW57dGV4dC1hbGlnbjpsZWZ0fS5oaXJlLWxvZ2luIC5sb2dpbi1mb3Jte3Bvc2l0aW9uOmFic29sdXRlO3otaW5kZXg6MTAwMDA7YmFja2dyb3VuZC1jb2xvcjojZmZmfS5oaXJlLWxvZ2luIGlucHV0e2Rpc3BsYXk6YmxvY2t9LmhpcmUtbG9naW4gLmxvZ2luLWZvcm0gYnV0dG9ue3dpZHRoOjc1JX0uaGlyZS1sb2dpbiAubG9naW4tc3ViLWNvbXBvbmVudHtwYWRkaW5nOjEycHh9LmhpcmUtbG9naW4gLmhpcmUtbG9naW4tZXJyb3J7Y29sb3I6I2YwMDtmb250LXdlaWdodDpib2xkfQ==","base64");

(0, _insertCss2["default"])(css, { prepend: true });

exports.Login = _login2["default"];
exports.Federated = _federated2["default"];
exports.Basic = _basic2["default"];

},{"./basic":15,"./federated":17,"./login":20,"insert-css":7}],19:[function(_dereq_,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; })();

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _events = _dereq_("events");

var _dispatcher = _dereq_("./dispatcher");

var _dispatcher2 = _interopRequireDefault(_dispatcher);

var CHANGE_EVENT = "change";

var LoginStore = (function (_EventEmitter) {
	_inherits(LoginStore, _EventEmitter);

	function LoginStore() {
		_classCallCheck(this, LoginStore);

		_get(Object.getPrototypeOf(LoginStore.prototype), "constructor", this).call(this);

		this.errorMessage = null;
		this.userData = null;
		this.tokenPropertyName = null;
		this.tokenPrefix = null;
	}

	_createClass(LoginStore, [{
		key: "setTokenPrefix",
		value: function setTokenPrefix(prefix) {
			this.tokenPrefix = prefix;
		}
	}, {
		key: "setTokenPropertyName",
		value: function setTokenPropertyName(id) {
			this.tokenPropertyName = id + "-auth-token";
			this.checkTokenInUrl();
		}
	}, {
		key: "checkTokenInUrl",
		value: function checkTokenInUrl() {
			var path = window.location.search.substr(1);
			var params = path.split('&');

			for (var i in params) {
				var _params$i$split = params[i].split('=');

				var _params$i$split2 = _slicedToArray(_params$i$split, 2);

				var key = _params$i$split2[0];
				var value = _params$i$split2[1];

				if (key === 'hsid') {
					var newLocation = window.location.href.replace(params[i], "").replace(/[\?\&]$/, "");
					this.setToken(value);
					this.setSupportLogout(false);
					history.replaceState(history.state, 'tokened', newLocation);
					break;
				}
			}
		}
	}, {
		key: "getState",
		value: function getState() {
			return {
				token: this.getToken(),
				errorMessage: this.errorMessage,
				authenticated: this.getToken() !== null && this.userData !== null,
				userData: this.userData,
				supportLogout: this.supportsLogout()
			};
		}

		// GB: Is a warning enough? What happens when tokenPropertyName is missing?
		// GB: "call initializeVre" is Timbuctoo specific whereas hire-login should be generic.
	}, {
		key: "onMissingTokenPropertyName",
		value: function onMissingTokenPropertyName() {
			console.warn("WARNING: missing tokenPropertyName, call initializeVre before attempting authentication");
		}
	}, {
		key: "setToken",
		value: function setToken(token) {
			if (this.tokenPropertyName === null) {
				return this.onMissingTokenPropertyName();
			}

			if (this.tokenPrefix != null) {
				token = "" + this.tokenPrefix + token;
			}

			localStorage.setItem(this.tokenPropertyName, token);
		}
	}, {
		key: "setSupportLogout",
		value: function setSupportLogout(supportsLogout) {
			if (supportsLogout) {
				localStorage.setItem("hi-support-auth-logout", "yes");
			} else {
				localStorage.removeItem("hi-support-auth-logout");
			}
		}
	}, {
		key: "supportsLogout",
		value: function supportsLogout() {
			return localStorage.getItem("hi-support-auth-logout") === "yes";
		}
	}, {
		key: "getToken",
		value: function getToken() {
			if (this.tokenPropertyName === null) {
				return this.onMissingTokenPropertyName();
			}
			return localStorage.getItem(this.tokenPropertyName);
		}
	}, {
		key: "removeToken",
		value: function removeToken() {
			if (this.tokenPropertyName === null) {
				return this.onMissingTokenPropertyName();
			}
			localStorage.removeItem(this.tokenPropertyName);
		}
	}, {
		key: "receiveBasicAuth",
		value: function receiveBasicAuth(data) {
			this.setSupportLogout(true);
			this.setToken(data.headers.x_auth_token);
			this.errorMessage = null;
		}
	}, {
		key: "receiveBasicAuthFailure",
		value: function receiveBasicAuthFailure(data) {
			var body = JSON.parse(data.body);
			this.errorMessage = body.message;
			this.removeToken();
		}
	}, {
		key: "receiveUserData",
		value: function receiveUserData(data) {
			this.userData = JSON.parse(data.body);
		}
	}, {
		key: "receiveUserDataFailure",
		value: function receiveUserDataFailure(data) {
			this.removeToken();
			this.errorMessage = "Unauthorized";
		}
	}, {
		key: "receiveLogout",
		value: function receiveLogout() {
			this.removeToken();
			this.setSupportLogout(false);
			this.errorMessage = null;
			this.userData = null;
		}
	}, {
		key: "stopListening",
		value: function stopListening(callback) {
			this.removeListener(CHANGE_EVENT, callback);
		}
	}, {
		key: "listen",
		value: function listen(callback) {
			this.addListener(CHANGE_EVENT, callback);
		}
	}]);

	return LoginStore;
})(_events.EventEmitter);

var loginStore = new LoginStore();

var dispatcherCallback = function dispatcherCallback(payload) {
	switch (payload.action.actionType) {
		case "BASIC_LOGIN_SUCCESS":
			loginStore.receiveBasicAuth(payload.action.data);
			break;
		case "BASIC_LOGIN_FAILURE":
			loginStore.receiveBasicAuthFailure(payload.action.data);
			break;
		case "USER_DATA_SUCCESS":
			loginStore.receiveUserData(payload.action.data);
			break;
		case "USER_DATA_FAILURE":
			loginStore.receiveUserDataFailure(payload.action.data);
			break;
		case "LOGOUT":
			loginStore.receiveLogout(payload.action.data);
			break;

		default:
			return;
	}

	loginStore.emit(CHANGE_EVENT);
};

loginStore.dispatcherIndex = _dispatcher2["default"].register(dispatcherCallback);

exports["default"] = loginStore;
module.exports = exports["default"];

},{"./dispatcher":16,"events":1}],20:[function(_dereq_,module,exports){
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

var _loginStore = _dereq_("./login-store");

var _loginStore2 = _interopRequireDefault(_loginStore);

var _federated = _dereq_("./federated");

var _federated2 = _interopRequireDefault(_federated);

var _api = _dereq_("./api");

var _api2 = _interopRequireDefault(_api);

var _actions = _dereq_("./actions");

var _actions2 = _interopRequireDefault(_actions);

var LoginComponent = (function (_React$Component) {
	_inherits(LoginComponent, _React$Component);

	function LoginComponent(props) {
		_classCallCheck(this, LoginComponent);

		_get(Object.getPrototypeOf(LoginComponent.prototype), "constructor", this).call(this, props);

		_loginStore2["default"].setTokenPrefix(this.props.tokenPrefix);
		_loginStore2["default"].setTokenPropertyName(this.props.appId);

		this.state = _loginStore2["default"].getState();
		this.state.opened = false;

		if (!this.state.initialized) {
			this.state.initialized = _loginStore2["default"].getToken() === null;
		}
	}

	_createClass(LoginComponent, [{
		key: "componentDidMount",
		value: function componentDidMount() {
			_loginStore2["default"].listen(this.onStoreChange.bind(this));

			if (this.state.token != null) {
				_api2["default"].fetchUserData(this.props.userUrl, this.state.token, this.props.headers);
			}

			document.addEventListener("click", this.handleDocumentClick.bind(this), false);
		}
	}, {
		key: "componentWillUnmount",
		value: function componentWillUnmount() {
			_loginStore2["default"].stopListening(this.onStoreChange.bind(this));

			document.removeEventListener("click", this.handleDocumentClick.bind(this), false);
		}
	}, {
		key: "onStoreChange",
		value: function onStoreChange() {
			this.setState(_loginStore2["default"].getState());

			if (this.state.token != null && !this.state.authenticated) {
				_api2["default"].fetchUserData(this.props.userUrl, this.state.token, this.props.headers);
			} else {
				this.props.onChange(_loginStore2["default"].getState());
			}
			this.setState({ initialized: true });
		}
	}, {
		key: "toggleLogin",
		value: function toggleLogin(ev) {
			this.setState({ opened: !this.state.opened });
		}
	}, {
		key: "onLogoutClick",
		value: function onLogoutClick(ev) {
			_actions2["default"].logout();
		}
	}, {
		key: "handleDocumentClick",
		value: function handleDocumentClick(ev) {
			if (this.state.opened && !_reactDom2["default"].findDOMNode(this).contains(ev.target)) {
				this.setState({
					opened: false
				});
			}
		}
	}, {
		key: "render",
		value: function render() {
			if (!this.state.initialized) {
				return _react2["default"].createElement("div", null);
			}

			if (this.state.authenticated) {
				var logoutButton = this.state.supportLogout ? _react2["default"].createElement(
					"button",
					{ onClick: this.onLogoutClick.bind(this) },
					this.props.logoutLabel
				) : null;

				return _react2["default"].createElement(
					"div",
					{ className: "hire-login" },
					_react2["default"].createElement(
						"span",
						{ className: "login-status" },
						this.props.loggedInLabel ? this.props.loggedInLabel + " " : "",
						this.state.userData.displayName
					),
					logoutButton
				);
			}

			return _react2["default"].createElement(
				"div",
				{ className: "hire-login" },
				_react2["default"].createElement(
					"button",
					{ className: this.state.opened ? 'toggle-opened' : 'toggle-closed',
						onClick: this.toggleLogin.bind(this) },
					this.props.buttonLabel
				),
				_react2["default"].createElement(
					"div",
					{ className: "login-form", id: "hire-login-form", style: this.state.opened ? { display: "block" } : { display: "none" } },
					_react2["default"].Children.map(this.props.children, function (child) {
						return _react2["default"].createElement(
							"div",
							null,
							child
						);
					}),
					_react2["default"].createElement(
						"div",
						{ className: "hire-login-error" },
						this.state.errorMessage
					)
				)
			);
		}
	}]);

	return LoginComponent;
})(_react2["default"].Component);

LoginComponent.propTypes = {
	appId: _react2["default"].PropTypes.string,
	buttonLabel: _react2["default"].PropTypes.string,
	children: _react2["default"].PropTypes.node,
	headers: _react2["default"].PropTypes.object,
	loggedInLabel: _react2["default"].PropTypes.string,
	logoutLabel: _react2["default"].PropTypes.string,
	tokenPrefix: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func.isRequired,
	userUrl: _react2["default"].PropTypes.string.isRequired
};

LoginComponent.defaultProps = {
	buttonLabel: "Login",
	loggedInLabel: "Logged in as",
	logoutLabel: "Logout",
	appId: "default-login",
	headers: {}
};

exports["default"] = LoginComponent;
module.exports = exports["default"];
/* GB: Why are children wrapped in a div? Could this.props.children just be passed as is? */

},{"./actions":13,"./api":14,"./federated":17,"./login-store":19,"react":"react","react-dom":"react-dom"}]},{},[18])(18)
});
}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer)
},{"buffer":30}],13:[function(require,module,exports){
exports.read = function (buffer, offset, isLE, mLen, nBytes) {
  var e, m
  var eLen = nBytes * 8 - mLen - 1
  var eMax = (1 << eLen) - 1
  var eBias = eMax >> 1
  var nBits = -7
  var i = isLE ? (nBytes - 1) : 0
  var d = isLE ? -1 : 1
  var s = buffer[offset + i]

  i += d

  e = s & ((1 << (-nBits)) - 1)
  s >>= (-nBits)
  nBits += eLen
  for (; nBits > 0; e = e * 256 + buffer[offset + i], i += d, nBits -= 8) {}

  m = e & ((1 << (-nBits)) - 1)
  e >>= (-nBits)
  nBits += mLen
  for (; nBits > 0; m = m * 256 + buffer[offset + i], i += d, nBits -= 8) {}

  if (e === 0) {
    e = 1 - eBias
  } else if (e === eMax) {
    return m ? NaN : ((s ? -1 : 1) * Infinity)
  } else {
    m = m + Math.pow(2, mLen)
    e = e - eBias
  }
  return (s ? -1 : 1) * m * Math.pow(2, e - mLen)
}

exports.write = function (buffer, value, offset, isLE, mLen, nBytes) {
  var e, m, c
  var eLen = nBytes * 8 - mLen - 1
  var eMax = (1 << eLen) - 1
  var eBias = eMax >> 1
  var rt = (mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0)
  var i = isLE ? 0 : (nBytes - 1)
  var d = isLE ? 1 : -1
  var s = value < 0 || (value === 0 && 1 / value < 0) ? 1 : 0

  value = Math.abs(value)

  if (isNaN(value) || value === Infinity) {
    m = isNaN(value) ? 1 : 0
    e = eMax
  } else {
    e = Math.floor(Math.log(value) / Math.LN2)
    if (value * (c = Math.pow(2, -e)) < 1) {
      e--
      c *= 2
    }
    if (e + eBias >= 1) {
      value += rt / c
    } else {
      value += rt * Math.pow(2, 1 - eBias)
    }
    if (value * c >= 2) {
      e++
      c /= 2
    }

    if (e + eBias >= eMax) {
      m = 0
      e = eMax
    } else if (e + eBias >= 1) {
      m = (value * c - 1) * Math.pow(2, mLen)
      e = e + eBias
    } else {
      m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen)
      e = 0
    }
  }

  for (; mLen >= 8; buffer[offset + i] = m & 0xff, i += d, m /= 256, mLen -= 8) {}

  e = (e << mLen) | m
  eLen += mLen
  for (; eLen > 0; buffer[offset + i] = e & 0xff, i += d, e /= 256, eLen -= 8) {}

  buffer[offset + i - d] |= s * 128
}

},{}],14:[function(require,module,exports){
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

},{}],15:[function(require,module,exports){
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

},{}],16:[function(require,module,exports){
/**
 * Checks if `value` is object-like. A value is object-like if it's not `null`
 * and has a `typeof` result of "object".
 *
 * @static
 * @memberOf _
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

},{}],17:[function(require,module,exports){
var isHostObject = require('./_isHostObject'),
    isObjectLike = require('./isObjectLike');

/** `Object#toString` result references. */
var objectTag = '[object Object]';

/** Used for built-in method references. */
var objectProto = Object.prototype;

/** Used to resolve the decompiled source of functions. */
var funcToString = Function.prototype.toString;

/** Used to infer the `Object` constructor. */
var objectCtorString = funcToString.call(Object);

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objectToString = objectProto.toString;

/** Built-in value references. */
var getPrototypeOf = Object.getPrototypeOf;

/**
 * Checks if `value` is a plain object, that is, an object created by the
 * `Object` constructor or one with a `[[Prototype]]` of `null`.
 *
 * @static
 * @memberOf _
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
  var proto = getPrototypeOf(value);
  if (proto === null) {
    return true;
  }
  var Ctor = proto.constructor;
  return (typeof Ctor == 'function' &&
    Ctor instanceof Ctor && funcToString.call(Ctor) == objectCtorString);
}

module.exports = isPlainObject;

},{"./_isHostObject":15,"./isObjectLike":16}],18:[function(require,module,exports){
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

},{}],19:[function(require,module,exports){
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
},{"for-each":1,"trim":28}],20:[function(require,module,exports){
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
},{}],21:[function(require,module,exports){
'use strict';

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.__esModule = true;
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
},{"./compose":24}],22:[function(require,module,exports){
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
},{}],23:[function(require,module,exports){
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

  return 'Reducer "' + key + '" returned undefined handling ' + actionName + '. ' + 'To ignore an action, you must explicitly return the previous state.';
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
},{"./createStore":25,"./utils/warning":27,"_process":32,"lodash/isPlainObject":17}],24:[function(require,module,exports){
"use strict";

exports.__esModule = true;
exports["default"] = compose;
/**
 * Composes single-argument functions from right to left.
 *
 * @param {...Function} funcs The functions to compose.
 * @returns {Function} A function obtained by composing functions from right to
 * left. For example, compose(f, g, h) is identical to arg => f(g(h(arg))).
 */
function compose() {
  for (var _len = arguments.length, funcs = Array(_len), _key = 0; _key < _len; _key++) {
    funcs[_key] = arguments[_key];
  }

  return function () {
    if (funcs.length === 0) {
      return arguments.length <= 0 ? undefined : arguments[0];
    }

    var last = funcs[funcs.length - 1];
    var rest = funcs.slice(0, -1);

    return rest.reduceRight(function (composed, f) {
      return f(composed);
    }, last.apply(undefined, arguments));
  };
}
},{}],25:[function(require,module,exports){
'use strict';

exports.__esModule = true;
exports.ActionTypes = undefined;
exports["default"] = createStore;

var _isPlainObject = require('lodash/isPlainObject');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

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
   * 2. The listener should not expect to see all states changes, as the state
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

  // When a store is created, an "INIT" action is dispatched so that every
  // reducer returns their initial state. This effectively populates
  // the initial state tree.
  dispatch({ type: ActionTypes.INIT });

  return {
    dispatch: dispatch,
    subscribe: subscribe,
    getState: getState,
    replaceReducer: replaceReducer
  };
}
},{"lodash/isPlainObject":17}],26:[function(require,module,exports){
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
},{"./applyMiddleware":21,"./bindActionCreators":22,"./combineReducers":23,"./compose":24,"./createStore":25,"./utils/warning":27,"_process":32}],27:[function(require,module,exports){
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
},{}],28:[function(require,module,exports){

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

},{}],29:[function(require,module,exports){
'use strict'

exports.toByteArray = toByteArray
exports.fromByteArray = fromByteArray

var lookup = []
var revLookup = []
var Arr = typeof Uint8Array !== 'undefined' ? Uint8Array : Array

function init () {
  var i
  var code = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
  var len = code.length

  for (i = 0; i < len; i++) {
    lookup[i] = code[i]
  }

  for (i = 0; i < len; ++i) {
    revLookup[code.charCodeAt(i)] = i
  }
  revLookup['-'.charCodeAt(0)] = 62
  revLookup['_'.charCodeAt(0)] = 63
}

init()

function toByteArray (b64) {
  var i, j, l, tmp, placeHolders, arr
  var len = b64.length

  if (len % 4 > 0) {
    throw new Error('Invalid string. Length must be a multiple of 4')
  }

  // the number of equal signs (place holders)
  // if there are two placeholders, than the two characters before it
  // represent one byte
  // if there is only one, then the three characters before it represent 2 bytes
  // this is just a cheap hack to not do indexOf twice
  placeHolders = b64[len - 2] === '=' ? 2 : b64[len - 1] === '=' ? 1 : 0

  // base64 is 4/3 + up to two characters of the original data
  arr = new Arr(len * 3 / 4 - placeHolders)

  // if there are placeholders, only get up to the last complete 4 chars
  l = placeHolders > 0 ? len - 4 : len

  var L = 0

  for (i = 0, j = 0; i < l; i += 4, j += 3) {
    tmp = (revLookup[b64.charCodeAt(i)] << 18) | (revLookup[b64.charCodeAt(i + 1)] << 12) | (revLookup[b64.charCodeAt(i + 2)] << 6) | revLookup[b64.charCodeAt(i + 3)]
    arr[L++] = (tmp & 0xFF0000) >> 16
    arr[L++] = (tmp & 0xFF00) >> 8
    arr[L++] = tmp & 0xFF
  }

  if (placeHolders === 2) {
    tmp = (revLookup[b64.charCodeAt(i)] << 2) | (revLookup[b64.charCodeAt(i + 1)] >> 4)
    arr[L++] = tmp & 0xFF
  } else if (placeHolders === 1) {
    tmp = (revLookup[b64.charCodeAt(i)] << 10) | (revLookup[b64.charCodeAt(i + 1)] << 4) | (revLookup[b64.charCodeAt(i + 2)] >> 2)
    arr[L++] = (tmp >> 8) & 0xFF
    arr[L++] = tmp & 0xFF
  }

  return arr
}

function tripletToBase64 (num) {
  return lookup[num >> 18 & 0x3F] + lookup[num >> 12 & 0x3F] + lookup[num >> 6 & 0x3F] + lookup[num & 0x3F]
}

function encodeChunk (uint8, start, end) {
  var tmp
  var output = []
  for (var i = start; i < end; i += 3) {
    tmp = (uint8[i] << 16) + (uint8[i + 1] << 8) + (uint8[i + 2])
    output.push(tripletToBase64(tmp))
  }
  return output.join('')
}

function fromByteArray (uint8) {
  var tmp
  var len = uint8.length
  var extraBytes = len % 3 // if we have 1 byte left, pad 2 bytes
  var output = ''
  var parts = []
  var maxChunkLength = 16383 // must be multiple of 3

  // go through the array every three bytes, we'll deal with trailing stuff later
  for (var i = 0, len2 = len - extraBytes; i < len2; i += maxChunkLength) {
    parts.push(encodeChunk(uint8, i, (i + maxChunkLength) > len2 ? len2 : (i + maxChunkLength)))
  }

  // pad the end with zeros, but make sure to not forget the extra bytes
  if (extraBytes === 1) {
    tmp = uint8[len - 1]
    output += lookup[tmp >> 2]
    output += lookup[(tmp << 4) & 0x3F]
    output += '=='
  } else if (extraBytes === 2) {
    tmp = (uint8[len - 2] << 8) + (uint8[len - 1])
    output += lookup[tmp >> 10]
    output += lookup[(tmp >> 4) & 0x3F]
    output += lookup[(tmp << 2) & 0x3F]
    output += '='
  }

  parts.push(output)

  return parts.join('')
}

},{}],30:[function(require,module,exports){
(function (global){
/*!
 * The buffer module from node.js, for the browser.
 *
 * @author   Feross Aboukhadijeh <feross@feross.org> <http://feross.org>
 * @license  MIT
 */
/* eslint-disable no-proto */

'use strict'

var base64 = require('base64-js')
var ieee754 = require('ieee754')
var isArray = require('isarray')

exports.Buffer = Buffer
exports.SlowBuffer = SlowBuffer
exports.INSPECT_MAX_BYTES = 50
Buffer.poolSize = 8192 // not used by this implementation

var rootParent = {}

/**
 * If `Buffer.TYPED_ARRAY_SUPPORT`:
 *   === true    Use Uint8Array implementation (fastest)
 *   === false   Use Object implementation (most compatible, even IE6)
 *
 * Browsers that support typed arrays are IE 10+, Firefox 4+, Chrome 7+, Safari 5.1+,
 * Opera 11.6+, iOS 4.2+.
 *
 * Due to various browser bugs, sometimes the Object implementation will be used even
 * when the browser supports typed arrays.
 *
 * Note:
 *
 *   - Firefox 4-29 lacks support for adding new properties to `Uint8Array` instances,
 *     See: https://bugzilla.mozilla.org/show_bug.cgi?id=695438.
 *
 *   - Chrome 9-10 is missing the `TypedArray.prototype.subarray` function.
 *
 *   - IE10 has a broken `TypedArray.prototype.subarray` function which returns arrays of
 *     incorrect length in some situations.

 * We detect these buggy browsers and set `Buffer.TYPED_ARRAY_SUPPORT` to `false` so they
 * get the Object implementation, which is slower but behaves correctly.
 */
Buffer.TYPED_ARRAY_SUPPORT = global.TYPED_ARRAY_SUPPORT !== undefined
  ? global.TYPED_ARRAY_SUPPORT
  : typedArraySupport()

function typedArraySupport () {
  try {
    var arr = new Uint8Array(1)
    arr.foo = function () { return 42 }
    return arr.foo() === 42 && // typed array instances can be augmented
        typeof arr.subarray === 'function' && // chrome 9-10 lack `subarray`
        arr.subarray(1, 1).byteLength === 0 // ie10 has broken `subarray`
  } catch (e) {
    return false
  }
}

function kMaxLength () {
  return Buffer.TYPED_ARRAY_SUPPORT
    ? 0x7fffffff
    : 0x3fffffff
}

/**
 * The Buffer constructor returns instances of `Uint8Array` that have their
 * prototype changed to `Buffer.prototype`. Furthermore, `Buffer` is a subclass of
 * `Uint8Array`, so the returned instances will have all the node `Buffer` methods
 * and the `Uint8Array` methods. Square bracket notation works as expected -- it
 * returns a single octet.
 *
 * The `Uint8Array` prototype remains unmodified.
 */
function Buffer (arg) {
  if (!(this instanceof Buffer)) {
    // Avoid going through an ArgumentsAdaptorTrampoline in the common case.
    if (arguments.length > 1) return new Buffer(arg, arguments[1])
    return new Buffer(arg)
  }

  if (!Buffer.TYPED_ARRAY_SUPPORT) {
    this.length = 0
    this.parent = undefined
  }

  // Common case.
  if (typeof arg === 'number') {
    return fromNumber(this, arg)
  }

  // Slightly less common case.
  if (typeof arg === 'string') {
    return fromString(this, arg, arguments.length > 1 ? arguments[1] : 'utf8')
  }

  // Unusual.
  return fromObject(this, arg)
}

// TODO: Legacy, not needed anymore. Remove in next major version.
Buffer._augment = function (arr) {
  arr.__proto__ = Buffer.prototype
  return arr
}

function fromNumber (that, length) {
  that = allocate(that, length < 0 ? 0 : checked(length) | 0)
  if (!Buffer.TYPED_ARRAY_SUPPORT) {
    for (var i = 0; i < length; i++) {
      that[i] = 0
    }
  }
  return that
}

function fromString (that, string, encoding) {
  if (typeof encoding !== 'string' || encoding === '') encoding = 'utf8'

  // Assumption: byteLength() return value is always < kMaxLength.
  var length = byteLength(string, encoding) | 0
  that = allocate(that, length)

  that.write(string, encoding)
  return that
}

function fromObject (that, object) {
  if (Buffer.isBuffer(object)) return fromBuffer(that, object)

  if (isArray(object)) return fromArray(that, object)

  if (object == null) {
    throw new TypeError('must start with number, buffer, array or string')
  }

  if (typeof ArrayBuffer !== 'undefined') {
    if (object.buffer instanceof ArrayBuffer) {
      return fromTypedArray(that, object)
    }
    if (object instanceof ArrayBuffer) {
      return fromArrayBuffer(that, object)
    }
  }

  if (object.length) return fromArrayLike(that, object)

  return fromJsonObject(that, object)
}

function fromBuffer (that, buffer) {
  var length = checked(buffer.length) | 0
  that = allocate(that, length)
  buffer.copy(that, 0, 0, length)
  return that
}

function fromArray (that, array) {
  var length = checked(array.length) | 0
  that = allocate(that, length)
  for (var i = 0; i < length; i += 1) {
    that[i] = array[i] & 255
  }
  return that
}

// Duplicate of fromArray() to keep fromArray() monomorphic.
function fromTypedArray (that, array) {
  var length = checked(array.length) | 0
  that = allocate(that, length)
  // Truncating the elements is probably not what people expect from typed
  // arrays with BYTES_PER_ELEMENT > 1 but it's compatible with the behavior
  // of the old Buffer constructor.
  for (var i = 0; i < length; i += 1) {
    that[i] = array[i] & 255
  }
  return that
}

function fromArrayBuffer (that, array) {
  array.byteLength // this throws if `array` is not a valid ArrayBuffer

  if (Buffer.TYPED_ARRAY_SUPPORT) {
    // Return an augmented `Uint8Array` instance, for best performance
    that = new Uint8Array(array)
    that.__proto__ = Buffer.prototype
  } else {
    // Fallback: Return an object instance of the Buffer class
    that = fromTypedArray(that, new Uint8Array(array))
  }
  return that
}

function fromArrayLike (that, array) {
  var length = checked(array.length) | 0
  that = allocate(that, length)
  for (var i = 0; i < length; i += 1) {
    that[i] = array[i] & 255
  }
  return that
}

// Deserialize { type: 'Buffer', data: [1,2,3,...] } into a Buffer object.
// Returns a zero-length buffer for inputs that don't conform to the spec.
function fromJsonObject (that, object) {
  var array
  var length = 0

  if (object.type === 'Buffer' && isArray(object.data)) {
    array = object.data
    length = checked(array.length) | 0
  }
  that = allocate(that, length)

  for (var i = 0; i < length; i += 1) {
    that[i] = array[i] & 255
  }
  return that
}

if (Buffer.TYPED_ARRAY_SUPPORT) {
  Buffer.prototype.__proto__ = Uint8Array.prototype
  Buffer.__proto__ = Uint8Array
  if (typeof Symbol !== 'undefined' && Symbol.species &&
      Buffer[Symbol.species] === Buffer) {
    // Fix subarray() in ES2016. See: https://github.com/feross/buffer/pull/97
    Object.defineProperty(Buffer, Symbol.species, {
      value: null,
      configurable: true
    })
  }
} else {
  // pre-set for values that may exist in the future
  Buffer.prototype.length = undefined
  Buffer.prototype.parent = undefined
}

function allocate (that, length) {
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    // Return an augmented `Uint8Array` instance, for best performance
    that = new Uint8Array(length)
    that.__proto__ = Buffer.prototype
  } else {
    // Fallback: Return an object instance of the Buffer class
    that.length = length
  }

  var fromPool = length !== 0 && length <= Buffer.poolSize >>> 1
  if (fromPool) that.parent = rootParent

  return that
}

function checked (length) {
  // Note: cannot use `length < kMaxLength` here because that fails when
  // length is NaN (which is otherwise coerced to zero.)
  if (length >= kMaxLength()) {
    throw new RangeError('Attempt to allocate Buffer larger than maximum ' +
                         'size: 0x' + kMaxLength().toString(16) + ' bytes')
  }
  return length | 0
}

function SlowBuffer (subject, encoding) {
  if (!(this instanceof SlowBuffer)) return new SlowBuffer(subject, encoding)

  var buf = new Buffer(subject, encoding)
  delete buf.parent
  return buf
}

Buffer.isBuffer = function isBuffer (b) {
  return !!(b != null && b._isBuffer)
}

Buffer.compare = function compare (a, b) {
  if (!Buffer.isBuffer(a) || !Buffer.isBuffer(b)) {
    throw new TypeError('Arguments must be Buffers')
  }

  if (a === b) return 0

  var x = a.length
  var y = b.length

  var i = 0
  var len = Math.min(x, y)
  while (i < len) {
    if (a[i] !== b[i]) break

    ++i
  }

  if (i !== len) {
    x = a[i]
    y = b[i]
  }

  if (x < y) return -1
  if (y < x) return 1
  return 0
}

Buffer.isEncoding = function isEncoding (encoding) {
  switch (String(encoding).toLowerCase()) {
    case 'hex':
    case 'utf8':
    case 'utf-8':
    case 'ascii':
    case 'binary':
    case 'base64':
    case 'raw':
    case 'ucs2':
    case 'ucs-2':
    case 'utf16le':
    case 'utf-16le':
      return true
    default:
      return false
  }
}

Buffer.concat = function concat (list, length) {
  if (!isArray(list)) throw new TypeError('list argument must be an Array of Buffers.')

  if (list.length === 0) {
    return new Buffer(0)
  }

  var i
  if (length === undefined) {
    length = 0
    for (i = 0; i < list.length; i++) {
      length += list[i].length
    }
  }

  var buf = new Buffer(length)
  var pos = 0
  for (i = 0; i < list.length; i++) {
    var item = list[i]
    item.copy(buf, pos)
    pos += item.length
  }
  return buf
}

function byteLength (string, encoding) {
  if (typeof string !== 'string') string = '' + string

  var len = string.length
  if (len === 0) return 0

  // Use a for loop to avoid recursion
  var loweredCase = false
  for (;;) {
    switch (encoding) {
      case 'ascii':
      case 'binary':
      // Deprecated
      case 'raw':
      case 'raws':
        return len
      case 'utf8':
      case 'utf-8':
        return utf8ToBytes(string).length
      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return len * 2
      case 'hex':
        return len >>> 1
      case 'base64':
        return base64ToBytes(string).length
      default:
        if (loweredCase) return utf8ToBytes(string).length // assume utf8
        encoding = ('' + encoding).toLowerCase()
        loweredCase = true
    }
  }
}
Buffer.byteLength = byteLength

function slowToString (encoding, start, end) {
  var loweredCase = false

  start = start | 0
  end = end === undefined || end === Infinity ? this.length : end | 0

  if (!encoding) encoding = 'utf8'
  if (start < 0) start = 0
  if (end > this.length) end = this.length
  if (end <= start) return ''

  while (true) {
    switch (encoding) {
      case 'hex':
        return hexSlice(this, start, end)

      case 'utf8':
      case 'utf-8':
        return utf8Slice(this, start, end)

      case 'ascii':
        return asciiSlice(this, start, end)

      case 'binary':
        return binarySlice(this, start, end)

      case 'base64':
        return base64Slice(this, start, end)

      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return utf16leSlice(this, start, end)

      default:
        if (loweredCase) throw new TypeError('Unknown encoding: ' + encoding)
        encoding = (encoding + '').toLowerCase()
        loweredCase = true
    }
  }
}

// The property is used by `Buffer.isBuffer` and `is-buffer` (in Safari 5-7) to detect
// Buffer instances.
Buffer.prototype._isBuffer = true

Buffer.prototype.toString = function toString () {
  var length = this.length | 0
  if (length === 0) return ''
  if (arguments.length === 0) return utf8Slice(this, 0, length)
  return slowToString.apply(this, arguments)
}

Buffer.prototype.equals = function equals (b) {
  if (!Buffer.isBuffer(b)) throw new TypeError('Argument must be a Buffer')
  if (this === b) return true
  return Buffer.compare(this, b) === 0
}

Buffer.prototype.inspect = function inspect () {
  var str = ''
  var max = exports.INSPECT_MAX_BYTES
  if (this.length > 0) {
    str = this.toString('hex', 0, max).match(/.{2}/g).join(' ')
    if (this.length > max) str += ' ... '
  }
  return '<Buffer ' + str + '>'
}

Buffer.prototype.compare = function compare (b) {
  if (!Buffer.isBuffer(b)) throw new TypeError('Argument must be a Buffer')
  if (this === b) return 0
  return Buffer.compare(this, b)
}

Buffer.prototype.indexOf = function indexOf (val, byteOffset) {
  if (byteOffset > 0x7fffffff) byteOffset = 0x7fffffff
  else if (byteOffset < -0x80000000) byteOffset = -0x80000000
  byteOffset >>= 0

  if (this.length === 0) return -1
  if (byteOffset >= this.length) return -1

  // Negative offsets start from the end of the buffer
  if (byteOffset < 0) byteOffset = Math.max(this.length + byteOffset, 0)

  if (typeof val === 'string') {
    if (val.length === 0) return -1 // special case: looking for empty string always fails
    return String.prototype.indexOf.call(this, val, byteOffset)
  }
  if (Buffer.isBuffer(val)) {
    return arrayIndexOf(this, val, byteOffset)
  }
  if (typeof val === 'number') {
    if (Buffer.TYPED_ARRAY_SUPPORT && Uint8Array.prototype.indexOf === 'function') {
      return Uint8Array.prototype.indexOf.call(this, val, byteOffset)
    }
    return arrayIndexOf(this, [ val ], byteOffset)
  }

  function arrayIndexOf (arr, val, byteOffset) {
    var foundIndex = -1
    for (var i = 0; byteOffset + i < arr.length; i++) {
      if (arr[byteOffset + i] === val[foundIndex === -1 ? 0 : i - foundIndex]) {
        if (foundIndex === -1) foundIndex = i
        if (i - foundIndex + 1 === val.length) return byteOffset + foundIndex
      } else {
        foundIndex = -1
      }
    }
    return -1
  }

  throw new TypeError('val must be string, number or Buffer')
}

function hexWrite (buf, string, offset, length) {
  offset = Number(offset) || 0
  var remaining = buf.length - offset
  if (!length) {
    length = remaining
  } else {
    length = Number(length)
    if (length > remaining) {
      length = remaining
    }
  }

  // must be an even number of digits
  var strLen = string.length
  if (strLen % 2 !== 0) throw new Error('Invalid hex string')

  if (length > strLen / 2) {
    length = strLen / 2
  }
  for (var i = 0; i < length; i++) {
    var parsed = parseInt(string.substr(i * 2, 2), 16)
    if (isNaN(parsed)) throw new Error('Invalid hex string')
    buf[offset + i] = parsed
  }
  return i
}

function utf8Write (buf, string, offset, length) {
  return blitBuffer(utf8ToBytes(string, buf.length - offset), buf, offset, length)
}

function asciiWrite (buf, string, offset, length) {
  return blitBuffer(asciiToBytes(string), buf, offset, length)
}

function binaryWrite (buf, string, offset, length) {
  return asciiWrite(buf, string, offset, length)
}

function base64Write (buf, string, offset, length) {
  return blitBuffer(base64ToBytes(string), buf, offset, length)
}

function ucs2Write (buf, string, offset, length) {
  return blitBuffer(utf16leToBytes(string, buf.length - offset), buf, offset, length)
}

Buffer.prototype.write = function write (string, offset, length, encoding) {
  // Buffer#write(string)
  if (offset === undefined) {
    encoding = 'utf8'
    length = this.length
    offset = 0
  // Buffer#write(string, encoding)
  } else if (length === undefined && typeof offset === 'string') {
    encoding = offset
    length = this.length
    offset = 0
  // Buffer#write(string, offset[, length][, encoding])
  } else if (isFinite(offset)) {
    offset = offset | 0
    if (isFinite(length)) {
      length = length | 0
      if (encoding === undefined) encoding = 'utf8'
    } else {
      encoding = length
      length = undefined
    }
  // legacy write(string, encoding, offset, length) - remove in v0.13
  } else {
    var swap = encoding
    encoding = offset
    offset = length | 0
    length = swap
  }

  var remaining = this.length - offset
  if (length === undefined || length > remaining) length = remaining

  if ((string.length > 0 && (length < 0 || offset < 0)) || offset > this.length) {
    throw new RangeError('attempt to write outside buffer bounds')
  }

  if (!encoding) encoding = 'utf8'

  var loweredCase = false
  for (;;) {
    switch (encoding) {
      case 'hex':
        return hexWrite(this, string, offset, length)

      case 'utf8':
      case 'utf-8':
        return utf8Write(this, string, offset, length)

      case 'ascii':
        return asciiWrite(this, string, offset, length)

      case 'binary':
        return binaryWrite(this, string, offset, length)

      case 'base64':
        // Warning: maxLength not taken into account in base64Write
        return base64Write(this, string, offset, length)

      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return ucs2Write(this, string, offset, length)

      default:
        if (loweredCase) throw new TypeError('Unknown encoding: ' + encoding)
        encoding = ('' + encoding).toLowerCase()
        loweredCase = true
    }
  }
}

Buffer.prototype.toJSON = function toJSON () {
  return {
    type: 'Buffer',
    data: Array.prototype.slice.call(this._arr || this, 0)
  }
}

function base64Slice (buf, start, end) {
  if (start === 0 && end === buf.length) {
    return base64.fromByteArray(buf)
  } else {
    return base64.fromByteArray(buf.slice(start, end))
  }
}

function utf8Slice (buf, start, end) {
  end = Math.min(buf.length, end)
  var res = []

  var i = start
  while (i < end) {
    var firstByte = buf[i]
    var codePoint = null
    var bytesPerSequence = (firstByte > 0xEF) ? 4
      : (firstByte > 0xDF) ? 3
      : (firstByte > 0xBF) ? 2
      : 1

    if (i + bytesPerSequence <= end) {
      var secondByte, thirdByte, fourthByte, tempCodePoint

      switch (bytesPerSequence) {
        case 1:
          if (firstByte < 0x80) {
            codePoint = firstByte
          }
          break
        case 2:
          secondByte = buf[i + 1]
          if ((secondByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0x1F) << 0x6 | (secondByte & 0x3F)
            if (tempCodePoint > 0x7F) {
              codePoint = tempCodePoint
            }
          }
          break
        case 3:
          secondByte = buf[i + 1]
          thirdByte = buf[i + 2]
          if ((secondByte & 0xC0) === 0x80 && (thirdByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0xF) << 0xC | (secondByte & 0x3F) << 0x6 | (thirdByte & 0x3F)
            if (tempCodePoint > 0x7FF && (tempCodePoint < 0xD800 || tempCodePoint > 0xDFFF)) {
              codePoint = tempCodePoint
            }
          }
          break
        case 4:
          secondByte = buf[i + 1]
          thirdByte = buf[i + 2]
          fourthByte = buf[i + 3]
          if ((secondByte & 0xC0) === 0x80 && (thirdByte & 0xC0) === 0x80 && (fourthByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0xF) << 0x12 | (secondByte & 0x3F) << 0xC | (thirdByte & 0x3F) << 0x6 | (fourthByte & 0x3F)
            if (tempCodePoint > 0xFFFF && tempCodePoint < 0x110000) {
              codePoint = tempCodePoint
            }
          }
      }
    }

    if (codePoint === null) {
      // we did not generate a valid codePoint so insert a
      // replacement char (U+FFFD) and advance only 1 byte
      codePoint = 0xFFFD
      bytesPerSequence = 1
    } else if (codePoint > 0xFFFF) {
      // encode to utf16 (surrogate pair dance)
      codePoint -= 0x10000
      res.push(codePoint >>> 10 & 0x3FF | 0xD800)
      codePoint = 0xDC00 | codePoint & 0x3FF
    }

    res.push(codePoint)
    i += bytesPerSequence
  }

  return decodeCodePointsArray(res)
}

// Based on http://stackoverflow.com/a/22747272/680742, the browser with
// the lowest limit is Chrome, with 0x10000 args.
// We go 1 magnitude less, for safety
var MAX_ARGUMENTS_LENGTH = 0x1000

function decodeCodePointsArray (codePoints) {
  var len = codePoints.length
  if (len <= MAX_ARGUMENTS_LENGTH) {
    return String.fromCharCode.apply(String, codePoints) // avoid extra slice()
  }

  // Decode in chunks to avoid "call stack size exceeded".
  var res = ''
  var i = 0
  while (i < len) {
    res += String.fromCharCode.apply(
      String,
      codePoints.slice(i, i += MAX_ARGUMENTS_LENGTH)
    )
  }
  return res
}

function asciiSlice (buf, start, end) {
  var ret = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; i++) {
    ret += String.fromCharCode(buf[i] & 0x7F)
  }
  return ret
}

function binarySlice (buf, start, end) {
  var ret = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; i++) {
    ret += String.fromCharCode(buf[i])
  }
  return ret
}

function hexSlice (buf, start, end) {
  var len = buf.length

  if (!start || start < 0) start = 0
  if (!end || end < 0 || end > len) end = len

  var out = ''
  for (var i = start; i < end; i++) {
    out += toHex(buf[i])
  }
  return out
}

function utf16leSlice (buf, start, end) {
  var bytes = buf.slice(start, end)
  var res = ''
  for (var i = 0; i < bytes.length; i += 2) {
    res += String.fromCharCode(bytes[i] + bytes[i + 1] * 256)
  }
  return res
}

Buffer.prototype.slice = function slice (start, end) {
  var len = this.length
  start = ~~start
  end = end === undefined ? len : ~~end

  if (start < 0) {
    start += len
    if (start < 0) start = 0
  } else if (start > len) {
    start = len
  }

  if (end < 0) {
    end += len
    if (end < 0) end = 0
  } else if (end > len) {
    end = len
  }

  if (end < start) end = start

  var newBuf
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    newBuf = this.subarray(start, end)
    newBuf.__proto__ = Buffer.prototype
  } else {
    var sliceLen = end - start
    newBuf = new Buffer(sliceLen, undefined)
    for (var i = 0; i < sliceLen; i++) {
      newBuf[i] = this[i + start]
    }
  }

  if (newBuf.length) newBuf.parent = this.parent || this

  return newBuf
}

/*
 * Need to make sure that buffer isn't trying to write out of bounds.
 */
function checkOffset (offset, ext, length) {
  if ((offset % 1) !== 0 || offset < 0) throw new RangeError('offset is not uint')
  if (offset + ext > length) throw new RangeError('Trying to access beyond buffer length')
}

Buffer.prototype.readUIntLE = function readUIntLE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var val = this[offset]
  var mul = 1
  var i = 0
  while (++i < byteLength && (mul *= 0x100)) {
    val += this[offset + i] * mul
  }

  return val
}

Buffer.prototype.readUIntBE = function readUIntBE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) {
    checkOffset(offset, byteLength, this.length)
  }

  var val = this[offset + --byteLength]
  var mul = 1
  while (byteLength > 0 && (mul *= 0x100)) {
    val += this[offset + --byteLength] * mul
  }

  return val
}

Buffer.prototype.readUInt8 = function readUInt8 (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 1, this.length)
  return this[offset]
}

Buffer.prototype.readUInt16LE = function readUInt16LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  return this[offset] | (this[offset + 1] << 8)
}

Buffer.prototype.readUInt16BE = function readUInt16BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  return (this[offset] << 8) | this[offset + 1]
}

Buffer.prototype.readUInt32LE = function readUInt32LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return ((this[offset]) |
      (this[offset + 1] << 8) |
      (this[offset + 2] << 16)) +
      (this[offset + 3] * 0x1000000)
}

Buffer.prototype.readUInt32BE = function readUInt32BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset] * 0x1000000) +
    ((this[offset + 1] << 16) |
    (this[offset + 2] << 8) |
    this[offset + 3])
}

Buffer.prototype.readIntLE = function readIntLE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var val = this[offset]
  var mul = 1
  var i = 0
  while (++i < byteLength && (mul *= 0x100)) {
    val += this[offset + i] * mul
  }
  mul *= 0x80

  if (val >= mul) val -= Math.pow(2, 8 * byteLength)

  return val
}

Buffer.prototype.readIntBE = function readIntBE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var i = byteLength
  var mul = 1
  var val = this[offset + --i]
  while (i > 0 && (mul *= 0x100)) {
    val += this[offset + --i] * mul
  }
  mul *= 0x80

  if (val >= mul) val -= Math.pow(2, 8 * byteLength)

  return val
}

Buffer.prototype.readInt8 = function readInt8 (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 1, this.length)
  if (!(this[offset] & 0x80)) return (this[offset])
  return ((0xff - this[offset] + 1) * -1)
}

Buffer.prototype.readInt16LE = function readInt16LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  var val = this[offset] | (this[offset + 1] << 8)
  return (val & 0x8000) ? val | 0xFFFF0000 : val
}

Buffer.prototype.readInt16BE = function readInt16BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  var val = this[offset + 1] | (this[offset] << 8)
  return (val & 0x8000) ? val | 0xFFFF0000 : val
}

Buffer.prototype.readInt32LE = function readInt32LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset]) |
    (this[offset + 1] << 8) |
    (this[offset + 2] << 16) |
    (this[offset + 3] << 24)
}

Buffer.prototype.readInt32BE = function readInt32BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset] << 24) |
    (this[offset + 1] << 16) |
    (this[offset + 2] << 8) |
    (this[offset + 3])
}

Buffer.prototype.readFloatLE = function readFloatLE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)
  return ieee754.read(this, offset, true, 23, 4)
}

Buffer.prototype.readFloatBE = function readFloatBE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)
  return ieee754.read(this, offset, false, 23, 4)
}

Buffer.prototype.readDoubleLE = function readDoubleLE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 8, this.length)
  return ieee754.read(this, offset, true, 52, 8)
}

Buffer.prototype.readDoubleBE = function readDoubleBE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 8, this.length)
  return ieee754.read(this, offset, false, 52, 8)
}

function checkInt (buf, value, offset, ext, max, min) {
  if (!Buffer.isBuffer(buf)) throw new TypeError('buffer must be a Buffer instance')
  if (value > max || value < min) throw new RangeError('value is out of bounds')
  if (offset + ext > buf.length) throw new RangeError('index out of range')
}

Buffer.prototype.writeUIntLE = function writeUIntLE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkInt(this, value, offset, byteLength, Math.pow(2, 8 * byteLength), 0)

  var mul = 1
  var i = 0
  this[offset] = value & 0xFF
  while (++i < byteLength && (mul *= 0x100)) {
    this[offset + i] = (value / mul) & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeUIntBE = function writeUIntBE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkInt(this, value, offset, byteLength, Math.pow(2, 8 * byteLength), 0)

  var i = byteLength - 1
  var mul = 1
  this[offset + i] = value & 0xFF
  while (--i >= 0 && (mul *= 0x100)) {
    this[offset + i] = (value / mul) & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeUInt8 = function writeUInt8 (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 1, 0xff, 0)
  if (!Buffer.TYPED_ARRAY_SUPPORT) value = Math.floor(value)
  this[offset] = (value & 0xff)
  return offset + 1
}

function objectWriteUInt16 (buf, value, offset, littleEndian) {
  if (value < 0) value = 0xffff + value + 1
  for (var i = 0, j = Math.min(buf.length - offset, 2); i < j; i++) {
    buf[offset + i] = (value & (0xff << (8 * (littleEndian ? i : 1 - i)))) >>>
      (littleEndian ? i : 1 - i) * 8
  }
}

Buffer.prototype.writeUInt16LE = function writeUInt16LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0xffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value & 0xff)
    this[offset + 1] = (value >>> 8)
  } else {
    objectWriteUInt16(this, value, offset, true)
  }
  return offset + 2
}

Buffer.prototype.writeUInt16BE = function writeUInt16BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0xffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 8)
    this[offset + 1] = (value & 0xff)
  } else {
    objectWriteUInt16(this, value, offset, false)
  }
  return offset + 2
}

function objectWriteUInt32 (buf, value, offset, littleEndian) {
  if (value < 0) value = 0xffffffff + value + 1
  for (var i = 0, j = Math.min(buf.length - offset, 4); i < j; i++) {
    buf[offset + i] = (value >>> (littleEndian ? i : 3 - i) * 8) & 0xff
  }
}

Buffer.prototype.writeUInt32LE = function writeUInt32LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0xffffffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset + 3] = (value >>> 24)
    this[offset + 2] = (value >>> 16)
    this[offset + 1] = (value >>> 8)
    this[offset] = (value & 0xff)
  } else {
    objectWriteUInt32(this, value, offset, true)
  }
  return offset + 4
}

Buffer.prototype.writeUInt32BE = function writeUInt32BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0xffffffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 24)
    this[offset + 1] = (value >>> 16)
    this[offset + 2] = (value >>> 8)
    this[offset + 3] = (value & 0xff)
  } else {
    objectWriteUInt32(this, value, offset, false)
  }
  return offset + 4
}

Buffer.prototype.writeIntLE = function writeIntLE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) {
    var limit = Math.pow(2, 8 * byteLength - 1)

    checkInt(this, value, offset, byteLength, limit - 1, -limit)
  }

  var i = 0
  var mul = 1
  var sub = value < 0 ? 1 : 0
  this[offset] = value & 0xFF
  while (++i < byteLength && (mul *= 0x100)) {
    this[offset + i] = ((value / mul) >> 0) - sub & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeIntBE = function writeIntBE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) {
    var limit = Math.pow(2, 8 * byteLength - 1)

    checkInt(this, value, offset, byteLength, limit - 1, -limit)
  }

  var i = byteLength - 1
  var mul = 1
  var sub = value < 0 ? 1 : 0
  this[offset + i] = value & 0xFF
  while (--i >= 0 && (mul *= 0x100)) {
    this[offset + i] = ((value / mul) >> 0) - sub & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeInt8 = function writeInt8 (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 1, 0x7f, -0x80)
  if (!Buffer.TYPED_ARRAY_SUPPORT) value = Math.floor(value)
  if (value < 0) value = 0xff + value + 1
  this[offset] = (value & 0xff)
  return offset + 1
}

Buffer.prototype.writeInt16LE = function writeInt16LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0x7fff, -0x8000)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value & 0xff)
    this[offset + 1] = (value >>> 8)
  } else {
    objectWriteUInt16(this, value, offset, true)
  }
  return offset + 2
}

Buffer.prototype.writeInt16BE = function writeInt16BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0x7fff, -0x8000)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 8)
    this[offset + 1] = (value & 0xff)
  } else {
    objectWriteUInt16(this, value, offset, false)
  }
  return offset + 2
}

Buffer.prototype.writeInt32LE = function writeInt32LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0x7fffffff, -0x80000000)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value & 0xff)
    this[offset + 1] = (value >>> 8)
    this[offset + 2] = (value >>> 16)
    this[offset + 3] = (value >>> 24)
  } else {
    objectWriteUInt32(this, value, offset, true)
  }
  return offset + 4
}

Buffer.prototype.writeInt32BE = function writeInt32BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0x7fffffff, -0x80000000)
  if (value < 0) value = 0xffffffff + value + 1
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 24)
    this[offset + 1] = (value >>> 16)
    this[offset + 2] = (value >>> 8)
    this[offset + 3] = (value & 0xff)
  } else {
    objectWriteUInt32(this, value, offset, false)
  }
  return offset + 4
}

function checkIEEE754 (buf, value, offset, ext, max, min) {
  if (offset + ext > buf.length) throw new RangeError('index out of range')
  if (offset < 0) throw new RangeError('index out of range')
}

function writeFloat (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    checkIEEE754(buf, value, offset, 4, 3.4028234663852886e+38, -3.4028234663852886e+38)
  }
  ieee754.write(buf, value, offset, littleEndian, 23, 4)
  return offset + 4
}

Buffer.prototype.writeFloatLE = function writeFloatLE (value, offset, noAssert) {
  return writeFloat(this, value, offset, true, noAssert)
}

Buffer.prototype.writeFloatBE = function writeFloatBE (value, offset, noAssert) {
  return writeFloat(this, value, offset, false, noAssert)
}

function writeDouble (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    checkIEEE754(buf, value, offset, 8, 1.7976931348623157E+308, -1.7976931348623157E+308)
  }
  ieee754.write(buf, value, offset, littleEndian, 52, 8)
  return offset + 8
}

Buffer.prototype.writeDoubleLE = function writeDoubleLE (value, offset, noAssert) {
  return writeDouble(this, value, offset, true, noAssert)
}

Buffer.prototype.writeDoubleBE = function writeDoubleBE (value, offset, noAssert) {
  return writeDouble(this, value, offset, false, noAssert)
}

// copy(targetBuffer, targetStart=0, sourceStart=0, sourceEnd=buffer.length)
Buffer.prototype.copy = function copy (target, targetStart, start, end) {
  if (!start) start = 0
  if (!end && end !== 0) end = this.length
  if (targetStart >= target.length) targetStart = target.length
  if (!targetStart) targetStart = 0
  if (end > 0 && end < start) end = start

  // Copy 0 bytes; we're done
  if (end === start) return 0
  if (target.length === 0 || this.length === 0) return 0

  // Fatal error conditions
  if (targetStart < 0) {
    throw new RangeError('targetStart out of bounds')
  }
  if (start < 0 || start >= this.length) throw new RangeError('sourceStart out of bounds')
  if (end < 0) throw new RangeError('sourceEnd out of bounds')

  // Are we oob?
  if (end > this.length) end = this.length
  if (target.length - targetStart < end - start) {
    end = target.length - targetStart + start
  }

  var len = end - start
  var i

  if (this === target && start < targetStart && targetStart < end) {
    // descending copy from end
    for (i = len - 1; i >= 0; i--) {
      target[i + targetStart] = this[i + start]
    }
  } else if (len < 1000 || !Buffer.TYPED_ARRAY_SUPPORT) {
    // ascending copy from start
    for (i = 0; i < len; i++) {
      target[i + targetStart] = this[i + start]
    }
  } else {
    Uint8Array.prototype.set.call(
      target,
      this.subarray(start, start + len),
      targetStart
    )
  }

  return len
}

// fill(value, start=0, end=buffer.length)
Buffer.prototype.fill = function fill (value, start, end) {
  if (!value) value = 0
  if (!start) start = 0
  if (!end) end = this.length

  if (end < start) throw new RangeError('end < start')

  // Fill 0 bytes; we're done
  if (end === start) return
  if (this.length === 0) return

  if (start < 0 || start >= this.length) throw new RangeError('start out of bounds')
  if (end < 0 || end > this.length) throw new RangeError('end out of bounds')

  var i
  if (typeof value === 'number') {
    for (i = start; i < end; i++) {
      this[i] = value
    }
  } else {
    var bytes = utf8ToBytes(value.toString())
    var len = bytes.length
    for (i = start; i < end; i++) {
      this[i] = bytes[i % len]
    }
  }

  return this
}

// HELPER FUNCTIONS
// ================

var INVALID_BASE64_RE = /[^+\/0-9A-Za-z-_]/g

function base64clean (str) {
  // Node strips out invalid characters like \n and \t from the string, base64-js does not
  str = stringtrim(str).replace(INVALID_BASE64_RE, '')
  // Node converts strings with length < 2 to ''
  if (str.length < 2) return ''
  // Node allows for non-padded base64 strings (missing trailing ===), base64-js does not
  while (str.length % 4 !== 0) {
    str = str + '='
  }
  return str
}

function stringtrim (str) {
  if (str.trim) return str.trim()
  return str.replace(/^\s+|\s+$/g, '')
}

function toHex (n) {
  if (n < 16) return '0' + n.toString(16)
  return n.toString(16)
}

function utf8ToBytes (string, units) {
  units = units || Infinity
  var codePoint
  var length = string.length
  var leadSurrogate = null
  var bytes = []

  for (var i = 0; i < length; i++) {
    codePoint = string.charCodeAt(i)

    // is surrogate component
    if (codePoint > 0xD7FF && codePoint < 0xE000) {
      // last char was a lead
      if (!leadSurrogate) {
        // no lead yet
        if (codePoint > 0xDBFF) {
          // unexpected trail
          if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
          continue
        } else if (i + 1 === length) {
          // unpaired lead
          if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
          continue
        }

        // valid lead
        leadSurrogate = codePoint

        continue
      }

      // 2 leads in a row
      if (codePoint < 0xDC00) {
        if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
        leadSurrogate = codePoint
        continue
      }

      // valid surrogate pair
      codePoint = (leadSurrogate - 0xD800 << 10 | codePoint - 0xDC00) + 0x10000
    } else if (leadSurrogate) {
      // valid bmp char, but last char was a lead
      if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
    }

    leadSurrogate = null

    // encode utf8
    if (codePoint < 0x80) {
      if ((units -= 1) < 0) break
      bytes.push(codePoint)
    } else if (codePoint < 0x800) {
      if ((units -= 2) < 0) break
      bytes.push(
        codePoint >> 0x6 | 0xC0,
        codePoint & 0x3F | 0x80
      )
    } else if (codePoint < 0x10000) {
      if ((units -= 3) < 0) break
      bytes.push(
        codePoint >> 0xC | 0xE0,
        codePoint >> 0x6 & 0x3F | 0x80,
        codePoint & 0x3F | 0x80
      )
    } else if (codePoint < 0x110000) {
      if ((units -= 4) < 0) break
      bytes.push(
        codePoint >> 0x12 | 0xF0,
        codePoint >> 0xC & 0x3F | 0x80,
        codePoint >> 0x6 & 0x3F | 0x80,
        codePoint & 0x3F | 0x80
      )
    } else {
      throw new Error('Invalid code point')
    }
  }

  return bytes
}

function asciiToBytes (str) {
  var byteArray = []
  for (var i = 0; i < str.length; i++) {
    // Node's code seems to be doing this and not & 0x7F..
    byteArray.push(str.charCodeAt(i) & 0xFF)
  }
  return byteArray
}

function utf16leToBytes (str, units) {
  var c, hi, lo
  var byteArray = []
  for (var i = 0; i < str.length; i++) {
    if ((units -= 2) < 0) break

    c = str.charCodeAt(i)
    hi = c >> 8
    lo = c % 256
    byteArray.push(lo)
    byteArray.push(hi)
  }

  return byteArray
}

function base64ToBytes (str) {
  return base64.toByteArray(base64clean(str))
}

function blitBuffer (src, dst, offset, length) {
  for (var i = 0; i < length; i++) {
    if ((i + offset >= dst.length) || (i >= src.length)) break
    dst[i + offset] = src[i]
  }
  return i
}

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"base64-js":29,"ieee754":13,"isarray":31}],31:[function(require,module,exports){
var toString = {}.toString;

module.exports = Array.isArray || function (arr) {
  return toString.call(arr) == '[object Array]';
};

},{}],32:[function(require,module,exports){
// shim for using process in browser

var process = module.exports = {};
var queue = [];
var draining = false;
var currentQueue;
var queueIndex = -1;

function cleanUpNextTick() {
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

},{}],33:[function(require,module,exports){
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

},{"global/window":2,"is-function":14,"once":18,"parse-headers":19,"xtend":34}],34:[function(require,module,exports){
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

},{}],35:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

var _config = require("../config");

var _config2 = _interopRequireDefault(_config);

exports["default"] = function (path, query, vreId, done) {
	var options = {
		headers: { "Accept": "application/json", "VRE_ID": vreId },
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

},{"../config":65,"./server":42}],36:[function(require,module,exports){
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

},{"../config":65,"./server":42}],37:[function(require,module,exports){
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

// Skeleton base data per field definition
var initialData = {
	names: [],
	multiselect: [],
	links: [],
	keyword: [],
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
var makeSkeleton = function makeSkeleton(fieldDefs, domain) {
	return fieldDefs.map(function (fieldDef) {
		return [nameForType(fieldDef), initialDataForType(fieldDef)];
	}).concat([["@type", domain.replace(/s$/, "")]]).reduce(function (obj, cur) {
		obj[cur[0]] = cur[1];
		return obj;
	}, {});
};

// 1) Fetch entity
// 2) Dispatch RECEIVE_ENTITY for render
var selectEntity = function selectEntity(domain, entityId) {
	var errorMessage = arguments.length <= 2 || arguments[2] === undefined ? null : arguments[2];
	var successMessage = arguments.length <= 3 || arguments[3] === undefined ? null : arguments[3];
	return function (dispatch) {
		return _crud.crud.fetchEntity(_config2["default"].apiUrl[_config2["default"].apiVersion] + "/domain/" + domain + "/" + entityId, function (data) {
			dispatch({ type: "RECEIVE_ENTITY", domain: domain, data: data, errorMessage: errorMessage });
			if (successMessage !== null) {
				dispatch({ type: "SUCCESS_MESSAGE", message: successMessage });
			}
		}, function () {
			return dispatch({ type: "RECEIVE_ENTITY_FAILURE", errorMessage: "Failed to fetch " + domain + " with ID " + entityId });
		});
	};
};

// 1) Dispatch RECEIVE_ENTITY with empty entity skeleton for render
var makeNewEntity = function makeNewEntity(domain) {
	var errorMessage = arguments.length <= 1 || arguments[1] === undefined ? null : arguments[1];
	return function (dispatch, getState) {
		return dispatch({
			type: "RECEIVE_ENTITY",
			domain: domain,
			data: makeSkeleton(getState().vre.collections[domain], domain),
			errorMessage: errorMessage
		});
	};
};

var deleteEntity = function deleteEntity() {
	return function (dispatch, getState) {
		_crud.crud.deleteEntity(getState().entity.domain, getState().entity.data._id, getState().user.token, getState().vre.vreId, function () {
			dispatch({ type: "SUCCESS_MESSAGE", message: "Sucessfully deleted " + getState().entity.domain + " with ID " + getState().entity.data._id });
			dispatch(makeNewEntity(getState().entity.domain));
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
						return _relationSavers2["default"][_config2["default"].apiVersion](JSON.parse(resp.body), relationData, getState().vre.collections[getState().entity.domain], getState().user.token, getState().vre.vreId, function () {
							return(
								// 3) Refetch entity for render
								redispatch(selectEntity(getState().entity.domain, getState().entity.data._id, null, "Succesfully saved " + getState().entity.domain + " with ID " + getState().entity.data._id))
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
								_relationSavers2["default"][_config2["default"].apiVersion](data, relationData, getState().vre.collections[getState().entity.domain], getState().user.token, getState().vre.vreId, function () {
									return(
										// 4) Refetch entity for render
										redispatch(selectEntity(getState().entity.domain, data._id, null, "Succesfully saved " + getState().entity.domain))
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

var fetchEntityList = function fetchEntityList(domain) {
	return function (dispatch, getState) {
		dispatch({ type: "SET_PAGINATION_START", start: 0 });
		_crud.crud.fetchEntityList(domain, 0, getState().pagination.rows, function (data) {
			return dispatch({ type: "RECEIVE_ENTITY_LIST", data: data });
		});
	};
};

var paginateLeft = function paginateLeft() {
	return function (dispatch, getState) {
		var newStart = getState().pagination.start - getState().pagination.rows;
		dispatch({ type: "SET_PAGINATION_START", start: newStart < 0 ? 0 : newStart });
		_crud.crud.fetchEntityList(getState().entity.domain, newStart < 0 ? 0 : newStart, getState().pagination.rows, function (data) {
			return dispatch({ type: "RECEIVE_ENTITY_LIST", data: data });
		});
	};
};

var paginateRight = function paginateRight() {
	return function (dispatch, getState) {
		var newStart = getState().pagination.start + getState().pagination.rows;
		dispatch({ type: "SET_PAGINATION_START", start: newStart });
		_crud.crud.fetchEntityList(getState().entity.domain, newStart, getState().pagination.rows, function (data) {
			return dispatch({ type: "RECEIVE_ENTITY_LIST", data: data });
		});
	};
};

exports.saveEntity = saveEntity;
exports.selectEntity = selectEntity;
exports.makeNewEntity = makeNewEntity;
exports.deleteEntity = deleteEntity;
exports.fetchEntityList = fetchEntityList;
exports.paginateRight = paginateRight;
exports.paginateLeft = paginateLeft;

},{"../config":65,"../util/clone-deep":76,"./crud":36,"./relation-savers":40}],38:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _store = require("../store");

var _store2 = _interopRequireDefault(_store);

var _entity = require("./entity");

var _queries = require("./queries");

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
		return _store2["default"].dispatch((0, _entity.fetchEntityList)(domain));
	},
	onPaginateLeft: function onPaginateLeft() {
		return _store2["default"].dispatch((0, _entity.paginateLeft)());
	},
	onPaginateRight: function onPaginateRight() {
		return _store2["default"].dispatch((0, _entity.paginateRight)());
	},

	onSelectQuery: function onSelectQuery(domain, queryIndex) {
		var position = arguments.length <= 2 || arguments[2] === undefined ? null : arguments[2];
		return _store2["default"].dispatch((0, _queries.selectQuery)(domain, queryIndex, position));
	},
	onDeleteQuery: function onDeleteQuery(queryIndex) {
		return _store2["default"].dispatch((0, _queries.deleteQuery)(queryIndex));
	},
	onSetQueryPath: function onSetQueryPath(path) {
		return _store2["default"].dispatch((0, _queries.setQueryPath)(path));
	},
	onQueryChange: function onQueryChange(fieldPath, value) {
		return _store2["default"].dispatch((0, _queries.changeQuery)(fieldPath, value));
	},
	onAddQueryFilter: function onAddQueryFilter(fieldPath, value) {
		return _store2["default"].dispatch((0, _queries.addQueryFilter)(fieldPath, value));
	},
	onDeleteQueryFilter: function onDeleteQueryFilter(queryIndex) {
		return _store2["default"].dispatch((0, _queries.deleteQueryFilter)(queryIndex));
	},
	onMoveQueryPosition: function onMoveQueryPosition(queryIndex, movement) {
		return _store2["default"].dispatch((0, _queries.moveQueryPosition)(queryIndex, movement));
	},
	onSubmitQuery: function onSubmitQuery() {
		return _store2["default"].dispatch((0, _queries.submitQuery)());
	},
	onNameQuery: function onNameQuery(value) {
		return _store2["default"].dispatch((0, _queries.nameQuery)(value));
	},
	onSaveQuery: function onSaveQuery() {
		return _store2["default"].dispatch((0, _queries.saveQuery)());
	},
	onLoadQuery: function onLoadQuery(name) {
		return _store2["default"].dispatch((0, _queries.loadQuery)(name));
	}
};
module.exports = exports["default"];

},{"../store":75,"./entity":37,"./queries":39,"./vre":44}],39:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _parsersGremlin = require("../parsers/gremlin");

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

var _config = require("../config");

var _config2 = _interopRequireDefault(_config);

var moveQueryPosition = function moveQueryPosition(queryIndex, movement) {
	return function (dispatch, getState) {
		dispatch({ type: "SET_QUERY_POSITION", queryIndex: queryIndex, position: {
				x: getState().queries.queries[queryIndex].position.x - movement.x,
				y: getState().queries.queries[queryIndex].position.y - movement.y
			} });
	};
};

var selectQuery = function selectQuery(domain, queryIndex) {
	var position = arguments.length <= 2 || arguments[2] === undefined ? null : arguments[2];
	return function (dispatch) {
		return dispatch({ type: "SELECT_QUERY", queryIndex: queryIndex, domain: domain, position: position });
	};
};

var setQueryPath = function setQueryPath(path) {
	return function (dispatch) {
		return dispatch({ type: "SET_QUERY_PATH", path: path });
	};
};

var deleteQuery = function deleteQuery(queryIndex) {
	return function (dispatch) {
		return dispatch({ type: "DELETE_QUERY", queryIndex: queryIndex });
	};
};

var changeQuery = function changeQuery(fieldPath, value) {
	return function (dispatch) {
		return dispatch({ type: "SET_QUERY_FIELD_VALUE", fieldPath: fieldPath, value: value });
	};
};

var nameQuery = function nameQuery(value) {
	return function (dispatch) {
		return dispatch({ type: "SET_QUERY_NAME", value: value });
	};
};

var addQueryFilter = function addQueryFilter(fieldPath, value) {
	return function (dispatch) {
		if (value.type === "relation") {
			var newEntity = { type: "entity", domain: value.targetType, and: [] };
			value.or = [newEntity];
			value.targetDomain = value.targetType;
			delete value.targetType;
		}
		dispatch({ type: "ADD_QUERY_FILTER", fieldPath: fieldPath, value: value });
	};
};

var deleteQueryFilter = function deleteQueryFilter(queryIndex) {
	return function (dispatch) {
		return dispatch({ type: "DELETE_QUERY_FILTER", queryIndex: queryIndex });
	};
};

var submitQuery = function submitQuery() {
	return function (dispatch, getState) {
		var _getState = getState();

		var queries = _getState.queries;

		dispatch({ type: "SET_QUERY_RESULTS_PENDING" });

		var q = _parsersGremlin.parsers.parseGremlin(queries.queries[queries.currentQuery]);
		_server2["default"].fastXhr({
			method: "POST",
			headers: { "Accept": "application/json", "Content-type": "application/json" },
			url: _config2["default"].apiUrl.v4 + "/gremlin",
			body: JSON.stringify({ or: queries.queries[queries.currentQuery]["or"] })
		}, function (err, resp) {
			return dispatch({ type: "SET_QUERY_RESULTS", results: JSON.parse(resp.body) });
		});
		/*	server.fastXhr({
  		method: "POST",
  		url: `${config.apiUrl.v4}/gremlin`,
  		body: q[1]
  	}, (err, resp) => dispatch({type: "SET_QUERY_RESULT_COUNT", count: resp.body}));*/
	};
};

var saveQuery = function saveQuery() {
	return function (dispatch, getState) {
		var _getState2 = getState();

		var queries = _getState2.queries;

		var query = queries.queries[queries.currentQuery];
		_server2["default"].fastXhr({ method: "POST", headers: { "Content-type": "application/json" }, url: _config2["default"].apiUrl.v4 + "/saved-queries", body: JSON.stringify(query) }, function (err, resp) {
			return dispatch({ type: "SET_SAVED_QUERIES", savedQueries: JSON.parse(resp.body) });
		});
	};
};

var loadSavedQueries = function loadSavedQueries() {
	return function (dispatch) {
		return dispatch({ type: "SET_SAVED_QUERIES", savedQueries: [] });
	};
};
//	server.fastXhr({method: "GET", url: `${config.apiUrl.v4}/saved-queries`},
//		(err, resp) => dispatch({type: "SET_SAVED_QUERIES", savedQueries: JSON.parse(resp.body)}));

var loadQuery = function loadQuery(name) {
	return function (dispatch) {
		return dispatch({ type: "LOAD_SAVED_QUERY", name: name });
	};
};

exports.deleteQuery = deleteQuery;
exports.selectQuery = selectQuery;
exports.changeQuery = changeQuery;
exports.setQueryPath = setQueryPath;
exports.addQueryFilter = addQueryFilter;
exports.deleteQueryFilter = deleteQueryFilter;
exports.moveQueryPosition = moveQueryPosition;
exports.submitQuery = submitQuery;
exports.saveQuery = saveQuery;
exports.nameQuery = nameQuery;
exports.loadSavedQueries = loadSavedQueries;
exports.loadQuery = loadQuery;

},{"../config":65,"../parsers/gremlin":67,"./server":42}],40:[function(require,module,exports){
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

},{"./save-relations":41,"./v2.1/save-relations":43}],41:[function(require,module,exports){
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

},{"./crud":36}],42:[function(require,module,exports){
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

},{"../store":75,"xhr":33}],43:[function(require,module,exports){
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

},{"../crud":36}],44:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

var _config = require("../config");

var _config2 = _interopRequireDefault(_config);

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
			url: _config2["default"].apiUrl.v4 + "/metadata/" + vreId
		}, function (err, resp) {
			if (resp.statusCode === 200) {
				dispatch({ type: "SET_VRE", vreId: vreId, collections: JSON.parse(resp.body) });
			}
		}, function () {
			return dispatch({ type: "SET_VRE", vreId: vreId, collections: [] });
		}, "Fetch VRE description for " + vreId);
	};
};

exports.listVres = listVres;
exports.setVre = setVre;

},{"../config":65,"./server":42}],45:[function(require,module,exports){
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

var EntityList = (function (_React$Component) {
	_inherits(EntityList, _React$Component);

	function EntityList() {
		_classCallCheck(this, EntityList);

		_get(Object.getPrototypeOf(EntityList.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(EntityList, [{
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var entity = _props.entity;
			var pagination = _props.pagination;
			var onPaginateLeft = _props.onPaginateLeft;
			var onPaginateRight = _props.onPaginateRight;

			var leftButton = pagination.start > 0 ? _react2["default"].createElement(
				"button",
				{ onClick: onPaginateLeft },
				_react2["default"].createElement("span", { className: "glyphicon glyphicon-chevron-left" })
			) : _react2["default"].createElement(
				"button",
				{ disabled: true },
				_react2["default"].createElement("span", { className: "glyphicon glyphicon-chevron-left" })
			);

			var rightButton = entity.list.length < pagination.rows ? _react2["default"].createElement(
				"button",
				{ disabled: true },
				_react2["default"].createElement("span", { className: "glyphicon glyphicon-chevron-right" })
			) : _react2["default"].createElement(
				"button",
				{ onClick: onPaginateRight },
				_react2["default"].createElement("span", { className: "glyphicon glyphicon-chevron-right" })
			);

			return _react2["default"].createElement(
				"div",
				{ className: "panel panel-default" },
				_react2["default"].createElement(
					"div",
					{ className: "panel-heading" },
					_react2["default"].createElement(
						"h3",
						{ className: "panel-title" },
						"List of: ",
						entity.domain
					)
				),
				_react2["default"].createElement(
					"div",
					{ className: "panel-body" },
					leftButton,
					_react2["default"].createElement(
						"span",
						{ style: { margin: "20px" } },
						pagination.start + 1,
						" - ",
						pagination.start + pagination.rows
					),
					rightButton
				),
				_react2["default"].createElement(
					"ul",
					{ className: "list-group" },
					entity.list.map(function (entry, i) {
						return _react2["default"].createElement(
							"li",
							{ className: "list-group-item", key: i },
							_react2["default"].createElement(
								"span",
								{ style: { marginRight: "20px" } },
								i + pagination.start + 1,
								"."
							),
							_react2["default"].createElement(
								"a",
								{ onClick: function () {
										return _this.props.onSelect({ domain: entity.domain, id: entry._id });
									} },
								entry.displayName
							)
						);
					})
				)
			);
		}
	}]);

	return EntityList;
})(_react2["default"].Component);

EntityList.propTypes = {
	entity: _react2["default"].PropTypes.object,
	onPaginateLeft: _react2["default"].PropTypes.func,
	onPaginateRight: _react2["default"].PropTypes.func,
	onSelect: _react2["default"].PropTypes.func,
	pagination: _react2["default"].PropTypes.object
};

exports["default"] = EntityList;
module.exports = exports["default"];

},{"react":"react"}],46:[function(require,module,exports){
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

var _hireFormsInput = require("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var _validations = require("../validations");

var DatableField = (function (_React$Component) {
	_inherits(DatableField, _React$Component);

	function DatableField() {
		_classCallCheck(this, DatableField);

		_get(Object.getPrototypeOf(DatableField.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(DatableField, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsInput2["default"], {
					onChange: this.props.onChange.bind(this, [this.props.name]),
					validate: _validations.validateDate,
					value: this.props.entity.data[this.props.name]
				})
			);
		}
	}]);

	return DatableField;
})(_react2["default"].Component);

DatableField.propTypes = {
	entity: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func
};

exports["default"] = DatableField;
module.exports = exports["default"];

},{"../validations":59,"hire-forms-input":5,"react":"react"}],47:[function(require,module,exports){
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

var _hireFormsSelectList = require("hire-forms-select-list");

var _hireFormsSelectList2 = _interopRequireDefault(_hireFormsSelectList);

var KeywordField = (function (_React$Component) {
	_inherits(KeywordField, _React$Component);

	function KeywordField() {
		_classCallCheck(this, KeywordField);

		_get(Object.getPrototypeOf(KeywordField.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(KeywordField, [{
		key: "onChange",
		value: function onChange(values) {
			var currentValues = this.props.entity.data["@relations"][this.props.name] || [];
			this.props.onChange(["@relations", this.props.name], values.map(function (val) {
				return _extends({
					"id": val.key,
					"displayName": val.value
				}, currentValues.find(function (curVal) {
					return curVal.id === val.key;
				}) || {}, {
					accepted: true
				});
			}));
		}
	}, {
		key: "render",
		value: function render() {
			var values = this.props.entity.data["@relations"][this.props.name] || [];
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsSelectList2["default"], {
					onChange: this.onChange.bind(this),
					options: this.props.fieldDefinition.options,
					values: values.filter(function (val) {
						return val.accepted;
					}).map(function (val) {
						return { value: val.displayName, key: val.id };
					})
				})
			);
		}
	}]);

	return KeywordField;
})(_react2["default"].Component);

KeywordField.propTypes = {
	entity: _react2["default"].PropTypes.object,
	fieldDefinition: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func
};

exports["default"] = KeywordField;
module.exports = exports["default"];

},{"hire-forms-select-list":9,"react":"react"}],48:[function(require,module,exports){
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

var _hireFormsMultiForm = require("hire-forms-multi-form");

var _hireFormsMultiForm2 = _interopRequireDefault(_hireFormsMultiForm);

var _multiformLinkJs = require("./multiform/link.js");

var _multiformLinkJs2 = _interopRequireDefault(_multiformLinkJs);

var Links = (function (_React$Component) {
	_inherits(Links, _React$Component);

	function Links() {
		_classCallCheck(this, Links);

		_get(Object.getPrototypeOf(Links.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Links, [{
		key: "onChange",
		value: function onChange(inPath, data) {
			var path = typeof inPath === "string" ? [inPath] : inPath;
			this.props.onChange(path, data);
		}
	}, {
		key: "onDelete",
		value: function onDelete(path) {
			var values = this.props.entity.data[this.props.name];
			values.splice(path[1], 1);
			this.props.onChange([path[0]], values);
		}
	}, {
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsMultiForm2["default"], {
					attr: this.props.name,
					component: _multiformLinkJs2["default"],
					model: { label: "", url: "" },
					onChange: this.onChange.bind(this),
					onDelete: this.onDelete.bind(this),
					values: this.props.entity.data[this.props.name] })
			);
		}
	}]);

	return Links;
})(_react2["default"].Component);

Links.propTypes = {
	entity: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func
};

exports["default"] = Links;
module.exports = exports["default"];

},{"./multiform/link.js":50,"hire-forms-multi-form":7,"react":"react"}],49:[function(require,module,exports){
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

var _hireFormsSelectList = require("hire-forms-select-list");

var _hireFormsSelectList2 = _interopRequireDefault(_hireFormsSelectList);

var MultiSelect = (function (_React$Component) {
	_inherits(MultiSelect, _React$Component);

	function MultiSelect() {
		_classCallCheck(this, MultiSelect);

		_get(Object.getPrototypeOf(MultiSelect.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(MultiSelect, [{
		key: "onChange",
		value: function onChange(values) {
			this.props.onChange([this.props.name], values.filter(function (val, idx, me) {
				return me.indexOf(val) === idx;
			}));
		}
	}, {
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsSelectList2["default"], {
					onChange: this.onChange.bind(this),
					options: this.props.options,
					values: this.props.entity.data[this.props.name]
				})
			);
		}
	}]);

	return MultiSelect;
})(_react2["default"].Component);

MultiSelect.propTypes = {
	entity: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func,
	options: _react2["default"].PropTypes.array
};

exports["default"] = MultiSelect;
module.exports = exports["default"];

},{"hire-forms-select-list":9,"react":"react"}],50:[function(require,module,exports){
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

var _hireFormsForm = require("hire-forms-form");

var _hireFormsForm2 = _interopRequireDefault(_hireFormsForm);

var _hireFormsInput = require("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var _validations = require("../../validations");

var LinkForm = (function (_React$Component) {
	_inherits(LinkForm, _React$Component);

	function LinkForm() {
		_classCallCheck(this, LinkForm);

		_get(Object.getPrototypeOf(LinkForm.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(LinkForm, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"ul",
				null,
				_react2["default"].createElement(
					"li",
					null,
					_react2["default"].createElement(_hireFormsInput2["default"], {
						onChange: this.props.handleChange.bind(this, "label"),
						placeholder: "Label",
						value: this.props.formData.label })
				),
				_react2["default"].createElement(
					"li",
					null,
					_react2["default"].createElement(_hireFormsInput2["default"], {
						onChange: this.props.handleChange.bind(this, "url"),
						onInvalid: this.props.onInvalid,
						placeholder: "http://",
						validate: _validations.validateURL,
						value: this.props.formData.url })
				)
			);
		}
	}]);

	return LinkForm;
})(_react2["default"].Component);

LinkForm.propTypes = {
	formData: _react2["default"].PropTypes.object,
	handleChange: _react2["default"].PropTypes.func,
	onInvalid: _react2["default"].PropTypes.func,
	value: _react2["default"].PropTypes.object
};

exports["default"] = (0, _hireFormsForm2["default"])(LinkForm, "hire-forms-link-form");
module.exports = exports["default"];

},{"../../validations":59,"hire-forms-form":4,"hire-forms-input":5,"react":"react"}],51:[function(require,module,exports){
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

var _hireFormsForm = require("hire-forms-form");

var _hireFormsForm2 = _interopRequireDefault(_hireFormsForm);

var _hireFormsInput = require("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var _hireFormsSelect = require("hire-forms-select");

var _hireFormsSelect2 = _interopRequireDefault(_hireFormsSelect);

var NameForm = (function (_React$Component) {
	_inherits(NameForm, _React$Component);

	function NameForm() {
		_classCallCheck(this, NameForm);

		_get(Object.getPrototypeOf(NameForm.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(NameForm, [{
		key: "render",
		value: function render() {
			var _this = this;

			var components = this.props.formData.components;
			var inputs = components.map(function (component, i) {
				return _react2["default"].createElement(
					"li",
					{ key: i },
					_react2["default"].createElement(
						"div",
						null,
						_react2["default"].createElement(
							"button",
							{ className: "hire-remove-form", onClick: _this.props.handleChange.bind(_this, ["components", i, "REMOVE"]) },
							"✕"
						),
						component.type
					),
					_react2["default"].createElement(_hireFormsInput2["default"], {
						onChange: _this.props.handleChange.bind(_this, ["components", i, component.type]),
						placeholder: component.type,
						value: component.value })
				);
			});

			if (components.length) {
				inputs.push(_react2["default"].createElement(
					"li",
					{ key: components.length },
					_react2["default"].createElement(
						"div",
						null,
						"ADD COMPONENT",
						_react2["default"].createElement(_hireFormsSelect2["default"], { onChange: this.props.handleChange.bind(this, ["components", components.length]), options: this.props.options })
					)
				));
			}

			return _react2["default"].createElement(
				"ul",
				null,
				inputs
			);
		}
	}]);

	return NameForm;
})(_react2["default"].Component);

NameForm.propTypes = {
	formData: _react2["default"].PropTypes.object,
	handleChange: _react2["default"].PropTypes.func,
	options: _react2["default"].PropTypes.array,
	value: _react2["default"].PropTypes.object
};

exports["default"] = (0, _hireFormsForm2["default"])(NameForm, "names-form");
module.exports = exports["default"];

},{"hire-forms-form":4,"hire-forms-input":5,"hire-forms-select":10,"react":"react"}],52:[function(require,module,exports){
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

var _hireFormsMultiForm = require("hire-forms-multi-form");

var _hireFormsMultiForm2 = _interopRequireDefault(_hireFormsMultiForm);

var _multiformNameJs = require("./multiform/name.js");

var _multiformNameJs2 = _interopRequireDefault(_multiformNameJs);

var Names = (function (_React$Component) {
	_inherits(Names, _React$Component);

	function Names() {
		_classCallCheck(this, Names);

		_get(Object.getPrototypeOf(Names.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Names, [{
		key: "addName",
		value: function addName() {
			this.props.onChange([this.props.name], this.props.entity.data[this.props.name].concat([{ components: [{ type: "FORENAME", value: "" }] }]));
		}
	}, {
		key: "addNameComponent",
		value: function addNameComponent(key, value) {
			this.props.onChange(key, { type: value, value: "" });
		}
	}, {
		key: "removeNameComponent",
		value: function removeNameComponent(key) {
			this.props.onChange(key.slice(0, 3), this.props.entity.data[this.props.name][key[1]].components.filter(function (v, i) {
				return i !== key[3];
			}));
		}
	}, {
		key: "setNameComponentValue",
		value: function setNameComponentValue(key, value) {
			this.props.onChange(key.slice(0, 4), { type: key[4], value: value });
		}
	}, {
		key: "onChange",
		value: function onChange(key, value) {
			if (key === this.props.name) {
				return this.addName();
			}
			if (key.length === 4) {
				return this.addNameComponent(key, value);
			}
			if (key[4] === "REMOVE") {
				return this.removeNameComponent(key);
			}
			return this.setNameComponentValue(key, value);
		}
	}, {
		key: "onDelete",
		value: function onDelete(path) {
			var values = this.props.entity.data[this.props.name];
			values.splice(path[1], 1);
			this.props.onChange([path[0]], values);
		}
	}, {
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsMultiForm2["default"], {
					attr: this.props.name,
					component: _multiformNameJs2["default"],
					model: {
						FORENAME: "",
						SURNAME: "",
						components: []
					},
					onChange: this.onChange.bind(this),
					onDelete: this.onDelete.bind(this),
					options: this.props.options,
					values: this.props.entity.data[this.props.name] })
			);
		}
	}]);

	return Names;
})(_react2["default"].Component);

Names.propTypes = {
	entity: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func,
	options: _react2["default"].PropTypes.array
};

exports["default"] = Names;
module.exports = exports["default"];

},{"./multiform/name.js":51,"hire-forms-multi-form":7,"react":"react"}],53:[function(require,module,exports){
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

var _hireFormsAutocompleteList = require("hire-forms-autocomplete-list");

var _hireFormsAutocompleteList2 = _interopRequireDefault(_hireFormsAutocompleteList);

var _actionsAutocomplete = require("../../../../actions/autocomplete");

var _actionsAutocomplete2 = _interopRequireDefault(_actionsAutocomplete);

var RelationField = (function (_React$Component) {
	_inherits(RelationField, _React$Component);

	function RelationField() {
		_classCallCheck(this, RelationField);

		_get(Object.getPrototypeOf(RelationField.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(RelationField, [{
		key: "onChange",
		value: function onChange(values) {
			var currentValues = this.props.entity.data["@relations"][this.props.name] || [];
			this.props.onChange(["@relations", this.props.name], values.map(function (val) {
				return _extends({
					"id": val.key,
					"displayName": val.value
				}, currentValues.find(function (curVal) {
					return curVal.id === val.key;
				}) || {}, {
					accepted: true
				});
			}));
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var values = this.props.entity.data["@relations"][this.props.name] || [];

			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsAutocompleteList2["default"], {
					async: function (query, done) {
						return (0, _actionsAutocomplete2["default"])(_this.props.path, query, _this.props.vre.vreId, done);
					},
					onChange: this.onChange.bind(this),
					values: values.filter(function (val) {
						return val.accepted;
					}).map(function (val) {
						return { value: val.displayName, key: val.id };
					}) })
			);
		}
	}]);

	return RelationField;
})(_react2["default"].Component);

RelationField.propTypes = {
	entity: _react2["default"].PropTypes.object,
	fieldDefinition: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func,
	path: _react2["default"].PropTypes.string,
	vre: _react2["default"].PropTypes.object
};

exports["default"] = RelationField;
module.exports = exports["default"];

},{"../../../../actions/autocomplete":35,"hire-forms-autocomplete-list":3,"react":"react"}],54:[function(require,module,exports){
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

var _hireFormsSelect = require("hire-forms-select");

var _hireFormsSelect2 = _interopRequireDefault(_hireFormsSelect);

var Field = (function (_React$Component) {
	_inherits(Field, _React$Component);

	function Field() {
		_classCallCheck(this, Field);

		_get(Object.getPrototypeOf(Field.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Field, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsSelect2["default"], {
					onChange: this.props.onChange.bind(this, [this.props.name]),
					options: this.props.options,
					value: this.props.entity.data[this.props.name]
				})
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

},{"hire-forms-select":10,"react":"react"}],55:[function(require,module,exports){
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

var _hireFormsInput = require("hire-forms-input");

var _hireFormsInput2 = _interopRequireDefault(_hireFormsInput);

var StringField = (function (_React$Component) {
	_inherits(StringField, _React$Component);

	function StringField() {
		_classCallCheck(this, StringField);

		_get(Object.getPrototypeOf(StringField.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(StringField, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsInput2["default"], {
					onChange: this.props.onChange.bind(this, [this.props.name]),
					value: this.props.entity.data[this.props.name] || ""
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

},{"hire-forms-input":5,"react":"react"}],56:[function(require,module,exports){
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

var _hireFormsTextarea = require("hire-forms-textarea");

var _hireFormsTextarea2 = _interopRequireDefault(_hireFormsTextarea);

var TextField = (function (_React$Component) {
	_inherits(TextField, _React$Component);

	function TextField() {
		_classCallCheck(this, TextField);

		_get(Object.getPrototypeOf(TextField.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(TextField, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"label",
					null,
					this.props.name
				),
				_react2["default"].createElement(_hireFormsTextarea2["default"], {
					onChange: this.props.onChange.bind(this, [this.props.name]),
					value: this.props.entity.data[this.props.name] || ""
				})
			);
		}
	}]);

	return TextField;
})(_react2["default"].Component);

TextField.propTypes = {
	entity: _react2["default"].PropTypes.object,
	name: _react2["default"].PropTypes.string,
	onChange: _react2["default"].PropTypes.func
};

exports["default"] = TextField;
module.exports = exports["default"];

},{"hire-forms-textarea":11,"react":"react"}],57:[function(require,module,exports){
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

var _mapField = require("./map-field");

var _mapField2 = _interopRequireDefault(_mapField);

var MODE_LABELS = {
	edit: "Editing",
	"new": "Adding new"
};

var Form = (function (_React$Component) {
	_inherits(Form, _React$Component);

	function Form() {
		_classCallCheck(this, Form);

		_get(Object.getPrototypeOf(Form.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(Form, [{
		key: "render",
		value: function render() {
			var _this = this;

			var _props = this.props;
			var entity = _props.entity;
			var user = _props.user;
			var vre = _props.vre;
			var onSave = _props.onSave;
			var onDelete = _props.onDelete;
			var onNew = _props.onNew;

			if (!entity.data) {
				return null;
			}

			var disabled = !(user && user.token);

			var currentMode = entity.domain && entity.data._id ? "edit" : "new";

			var saveButton = _react2["default"].createElement(
				"button",
				{ className: "pull-right", disabled: disabled, onClick: onSave },
				"Save"
			);

			var deleteButton = entity.data._id ? _react2["default"].createElement(
				"button",
				{ className: "pull-right", disabled: disabled, onClick: onDelete },
				"Delete"
			) : null;

			var addNewButton = vre.vreId && entity.domain && entity.data._id ? _react2["default"].createElement(
				"button",
				{ className: "pull-right", disabled: disabled, onClick: function () {
						return onNew(entity.domain);
					} },
				"Add new"
			) : null;

			var formFields = vre.collections[entity.domain].map(function (fieldDef, i) {
				return _react2["default"].createElement(
					"li",
					{ className: "list-group-item", key: i },
					(0, _mapField2["default"])(fieldDef, _this.props)
				);
			});

			return _react2["default"].createElement(
				"div",
				{ className: "panel panel-default edit-form" },
				_react2["default"].createElement(
					"div",
					{ className: "panel-heading" },
					addNewButton,
					saveButton,
					deleteButton,
					_react2["default"].createElement(
						"h3",
						{ className: "panel-title" },
						MODE_LABELS[currentMode],
						": ",
						entity.domain.replace(/s$/, "")
					)
				),
				_react2["default"].createElement(
					"ul",
					{ className: "list-group" },
					formFields
				)
			);
		}
	}]);

	return Form;
})(_react2["default"].Component);

Form.propTypes = {
	entity: _react2["default"].PropTypes.object,
	onDelete: _react2["default"].PropTypes.func,
	onNew: _react2["default"].PropTypes.func,
	onSave: _react2["default"].PropTypes.func,
	user: _react2["default"].PropTypes.object,
	vre: _react2["default"].PropTypes.object
};

exports["default"] = Form;
module.exports = exports["default"];

},{"./map-field":58,"react":"react"}],58:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _fieldsString = require("./fields/string");

var _fieldsString2 = _interopRequireDefault(_fieldsString);

var _fieldsText = require("./fields/text");

var _fieldsText2 = _interopRequireDefault(_fieldsText);

var _fieldsLinks = require("./fields/links");

var _fieldsLinks2 = _interopRequireDefault(_fieldsLinks);

var _fieldsDatable = require("./fields/datable");

var _fieldsDatable2 = _interopRequireDefault(_fieldsDatable);

var _fieldsNames = require("./fields/names");

var _fieldsNames2 = _interopRequireDefault(_fieldsNames);

var _fieldsMultiSelect = require("./fields/multi-select");

var _fieldsMultiSelect2 = _interopRequireDefault(_fieldsMultiSelect);

var _fieldsSelect = require("./fields/select");

var _fieldsSelect2 = _interopRequireDefault(_fieldsSelect);

var _fieldsRelation = require("./fields/relation");

var _fieldsRelation2 = _interopRequireDefault(_fieldsRelation);

var _fieldsKeyword = require("./fields/keyword");

var _fieldsKeyword2 = _interopRequireDefault(_fieldsKeyword);

var MAP = {
	"string": function string(fieldDef, props) {
		return _react2["default"].createElement(_fieldsString2["default"], _extends({}, props, { name: fieldDef.name }));
	},
	"text": function text(fieldDef, props) {
		return _react2["default"].createElement(_fieldsString2["default"], _extends({}, props, { name: fieldDef.name }));
	},
	"links": function links(fieldDef, props) {
		return _react2["default"].createElement(_fieldsLinks2["default"], _extends({}, props, { name: fieldDef.name }));
	},
	"datable": function datable(fieldDef, props) {
		return _react2["default"].createElement(_fieldsDatable2["default"], _extends({}, props, { name: fieldDef.name }));
	},
	"names": function names(fieldDef, props) {
		return _react2["default"].createElement(_fieldsNames2["default"], _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
	},
	"multiselect": function multiselect(fieldDef, props) {
		return _react2["default"].createElement(_fieldsMultiSelect2["default"], _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
	},
	"select": function select(fieldDef, props) {
		return _react2["default"].createElement(_fieldsSelect2["default"], _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
	},
	"relation": function relation(fieldDef, props) {
		return _react2["default"].createElement(_fieldsRelation2["default"], _extends({}, props, { name: fieldDef.name, path: fieldDef.quicksearch }));
	},
	"keyword": function keyword(fieldDef, props) {
		return _react2["default"].createElement(_fieldsKeyword2["default"], _extends({}, props, { fieldDefinition: fieldDef, name: fieldDef.name }));
	}
};

exports["default"] = function (fieldDef, props) {
	return (MAP[fieldDef.type] || MAP.string)(fieldDef, props);
};

module.exports = exports["default"];

},{"./fields/datable":46,"./fields/keyword":47,"./fields/links":48,"./fields/multi-select":49,"./fields/names":52,"./fields/relation":53,"./fields/select":54,"./fields/string":55,"./fields/text":56,"react":"react"}],59:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports.validateDate = validateDate;
exports.validateURL = validateURL;

function validateDate(value) {
	// Handle validation.
	var re = /^(\d{2}-)?(\d{2}-)?\d{1,4}(~|\?)?$/;
	var isValid = re.test(value);

	// Return a validator object.
	return {
		isValid: isValid,
		message: isValid ? "" : "A date should be formatted as: DD-MM-YYYY. \nOptionally a '~' (approximate) or '?' (uncertain) can be added."
	};
}

function validateURL(value) {
	var re = /\s+|\.\.|"/;
	var oneDot = value.indexOf(".") > 0;
	var noEndDot = value.charAt(value.length - 1) !== ".";
	var isValid = !re.test(value) && oneDot && noEndDot;

	return {
		isValid: value != null && value !== "" && isValid,
		message: isValid ? "" : "Please enter a valid URL."
	};
}

},{}],60:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

var _get = function get(_x, _x2, _x3) { var _again = true; _function: while (_again) { var object = _x, property = _x2, receiver = _x3; _again = false; if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { _x = parent; _x2 = property; _x3 = receiver; _again = true; desc = parent = undefined; continue _function; } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } } };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _login = require("./login");

var _login2 = _interopRequireDefault(_login);

var dropDownIsActive = function dropDownIsActive(currentVre, vre) {
	return currentVre === vre.vreId || null;
};

var Header = (function (_React$Component) {
	_inherits(Header, _React$Component);

	function Header(props) {
		_classCallCheck(this, Header);

		_get(Object.getPrototypeOf(Header.prototype), "constructor", this).call(this, props);

		this.state = {
			openMenuVreId: "none"
		};

		this.documentClickListener = this.handleDocumentClick.bind(this);
	}

	_createClass(Header, [{
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
		key: "handleDocumentClick",
		value: function handleDocumentClick(ev) {
			var openMenuVreId = this.state.openMenuVreId;

			if (this.state.openMenuVreId !== "none" && !document.querySelector(".dropdown." + openMenuVreId).contains(ev.target)) {
				this.setState({
					openMenuVreId: "none"
				});
			}
		}
	}, {
		key: "onVreMenuClick",
		value: function onVreMenuClick(currentVre) {
			this.setState({ openMenuVreId: currentVre });
			this.props.onSelectVre(currentVre);
		}
	}, {
		key: "onDomainSelect",
		value: function onDomainSelect(domain) {
			this.setState({ openMenuVreId: "none" });
			this.props.onNew(domain);
			this.props.onSelectDomain(domain);
		}
	}, {
		key: "render",
		value: function render() {
			var _this = this;

			var vre = this.props.vre;

			var domains = Object.keys(vre.collections || {});
			var openMenuVreId = this.state.openMenuVreId;

			return _react2["default"].createElement(
				"nav",
				{ className: "navbar navbar-default" },
				_react2["default"].createElement(
					"div",
					{ className: "container-fluid" },
					_react2["default"].createElement(_login2["default"], this.props),
					_react2["default"].createElement(
						"ul",
						{ className: "nav navbar-nav navbar-left" },
						vre.list.map(function (currentVre) {
							return _react2["default"].createElement(
								"li",
								{ className: (0, _classnames2["default"])("dropdown", currentVre, {
										active: dropDownIsActive(currentVre, vre),
										open: currentVre === openMenuVreId
									}), key: currentVre },
								_react2["default"].createElement(
									"a",
									{ className: "dropdown-toggle", onClick: _this.onVreMenuClick.bind(_this, currentVre) },
									currentVre,
									_react2["default"].createElement("span", { className: "caret" })
								),
								_react2["default"].createElement(
									"ul",
									{ className: "dropdown-menu" },
									domains.map(function (domain, i) {
										return _react2["default"].createElement(
											"li",
											{ key: i },
											_react2["default"].createElement(
												"a",
												{ onClick: function () {
														return _this.onDomainSelect(domain);
													} },
												domain
											)
										);
									})
								)
							);
						})
					)
				)
			);
		}
	}]);

	return Header;
})(_react2["default"].Component);

Header.propTypes = {
	entity: _react2["default"].PropTypes.object,
	onLoginChange: _react2["default"].PropTypes.func,
	onNew: _react2["default"].PropTypes.func,
	onSelectDomain: _react2["default"].PropTypes.func,
	onSelectVre: _react2["default"].PropTypes.func,
	vre: _react2["default"].PropTypes.object
};

exports["default"] = Header;
module.exports = exports["default"];

},{"./login":62,"classnames":"classnames","react":"react"}],61:[function(require,module,exports){
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

var _form = require("./form");

var _form2 = _interopRequireDefault(_form);

var _header = require("./header");

var _header2 = _interopRequireDefault(_header);

var _messages = require("./messages");

var _messages2 = _interopRequireDefault(_messages);

var _entityList = require("./entity-list");

var _entityList2 = _interopRequireDefault(_entityList);

var App = (function (_React$Component) {
	_inherits(App, _React$Component);

	function App() {
		_classCallCheck(this, App);

		_get(Object.getPrototypeOf(App.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(App, [{
		key: "render",
		value: function render() {
			console.log(this.props.vre, this.props.entity);

			var businessPart = this.props.vre.vreId && this.props.entity.domain ? _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(_form2["default"], this.props),
				_react2["default"].createElement(_entityList2["default"], this.props)
			) : null;

			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"header",
					null,
					_react2["default"].createElement(_header2["default"], this.props)
				),
				_react2["default"].createElement(
					"main",
					null,
					_react2["default"].createElement(_messages2["default"], _extends({}, this.props, { types: ["ERROR_MESSAGE", "SUCCESS_MESSAGE"] })),
					businessPart
				)
			);
		}
	}]);

	return App;
})(_react2["default"].Component);

App.propTypes = {
	entity: _react2["default"].PropTypes.object,
	messages: _react2["default"].PropTypes.object,
	onLoginChange: _react2["default"].PropTypes.func,
	onNew: _react2["default"].PropTypes.func,
	onSelect: _react2["default"].PropTypes.func,
	onSelectVre: _react2["default"].PropTypes.func,
	vre: _react2["default"].PropTypes.object
};

exports["default"] = App;
module.exports = exports["default"];

},{"./entity-list":45,"./form":57,"./header":60,"./messages":64,"react":"react"}],62:[function(require,module,exports){
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

var _hireLogin = require("hire-login");

var _loginDropdown = require("./login-dropdown");

var _loginDropdown2 = _interopRequireDefault(_loginDropdown);

var _config = require("../../../config");

var _config2 = _interopRequireDefault(_config);

var LoginMenu = (function (_React$Component) {
	_inherits(LoginMenu, _React$Component);

	function LoginMenu(props) {
		_classCallCheck(this, LoginMenu);

		_get(Object.getPrototypeOf(LoginMenu.prototype), "constructor", this).call(this, props);

		this.state = {
			loginDropdownOpen: false
		};

		this.documentClickListener = this.handleDocumentClick.bind(this);
	}

	_createClass(LoginMenu, [{
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
		key: "onToggleLogin",
		value: function onToggleLogin() {
			if (this.state.loginDropdownOpen) {
				this.setState({ loginDropdownOpen: false });
			} else {
				this.setState({ loginDropdownOpen: true });
			}
		}
	}, {
		key: "handleDocumentClick",
		value: function handleDocumentClick(ev) {
			if (this.state.loginDropdownOpen && !document.querySelector(".login-dropdown-list-item").contains(ev.target)) {
				this.setState({
					loginDropdownOpen: false
				});
			}
		}
	}, {
		key: "render",
		value: function render() {
			var user = this.props.user;

			return _react2["default"].createElement(
				"div",
				null,
				_react2["default"].createElement(
					"div",
					{ className: "navbar-form navbar-right", style: { display: user && user.token ? "inline-block" : "none" } },
					_react2["default"].createElement(
						"div",
						{ className: "form-group" },
						_react2["default"].createElement(_hireLogin.Login, {
							appId: this.props.vre.vreId,
							headers: { VRE_ID: "WomenWriters" },
							onChange: this.props.onLoginChange,
							userUrl: _config2["default"].apiUrl["v2.1"] + "/system/users/me" })
					)
				),
				_react2["default"].createElement(
					"ul",
					{ className: "nav navbar-nav navbar-right", style: { display: user && user.token ? "none" : "inline-block" } },
					_react2["default"].createElement(
						"li",
						{ className: (0, _classnames2["default"])("login-dropdown-list-item", "dropdown", { open: this.state.loginDropdownOpen }) },
						_react2["default"].createElement(
							"a",
							{ className: "dropdown-toggle", onClick: this.onToggleLogin.bind(this), role: "button" },
							"Login ",
							_react2["default"].createElement("span", { className: "caret" })
						),
						_react2["default"].createElement(_loginDropdown2["default"], this.props)
					)
				)
			);
		}
	}]);

	return LoginMenu;
})(_react2["default"].Component);

LoginMenu.propTypes = {
	onLoginChange: _react2["default"].PropTypes.func,
	user: _react2["default"].PropTypes.object,
	vre: _react2["default"].PropTypes.object
};

exports["default"] = LoginMenu;
module.exports = exports["default"];

},{"../../../config":65,"./login-dropdown":63,"classnames":"classnames","hire-login":12,"react":"react"}],63:[function(require,module,exports){
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

var _hireLogin = require("hire-login");

var _config = require("../../../config");

var _config2 = _interopRequireDefault(_config);

var LoginDropdown = (function (_React$Component) {
	_inherits(LoginDropdown, _React$Component);

	function LoginDropdown() {
		_classCallCheck(this, LoginDropdown);

		_get(Object.getPrototypeOf(LoginDropdown.prototype), "constructor", this).apply(this, arguments);
	}

	_createClass(LoginDropdown, [{
		key: "render",
		value: function render() {
			return _react2["default"].createElement(
				"ul",
				{ className: "dropdown-menu" },
				_react2["default"].createElement(
					"li",
					null,
					_react2["default"].createElement(_hireLogin.Basic, { url: _config2["default"].apiUrl["v2.1"] + "/authenticate" })
				)
			);
		}
	}]);

	return LoginDropdown;
})(_react2["default"].Component);

exports["default"] = LoginDropdown;
module.exports = exports["default"];

},{"../../../config":65,"hire-login":12,"react":"react"}],64:[function(require,module,exports){
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

var LABELS = {
	"SUCCESS_MESSAGE": "Info",
	"ERROR_MESSAGE": "Warning!"
};

var CLASS_NAMES = {
	"SUCCESS_MESSAGE": "alert-success",
	"ERROR_MESSAGE": "alert-danger"
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
				filteredMessages.map(function (msg, i) {
					return _react2["default"].createElement(
						"div",
						{ className: (0, _classnames2["default"])("alert", "alert-dismissible", CLASS_NAMES[msg.type]), key: i },
						_react2["default"].createElement(
							"button",
							{ className: "close", onClick: function () {
									return onDismissMessage(msg.index);
								} },
							_react2["default"].createElement(
								"span",
								{ "aria-hidden": "true" },
								"×"
							)
						),
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

},{"classnames":"classnames","react":"react"}],65:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports["default"] = {
	apiUrl: {
		"v2.1": "/v2.1",
		"v4": "/v2.1"
	},
	apiVersion: "v4"
};
module.exports = exports["default"];

},{}],66:[function(require,module,exports){
"use strict";

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

var _componentsEdit = require("./components/edit");

var _componentsEdit2 = _interopRequireDefault(_componentsEdit);

document.addEventListener("DOMContentLoaded", function () {

	_store2["default"].subscribe(function () {
		return _reactDom2["default"].render(_react2["default"].createElement(_componentsEdit2["default"], _extends({}, _store2["default"].getState(), _actions2["default"])), document.getElementById("app"));
	});

	_store2["default"].dispatch((0, _actionsVre.listVres)());
});

},{"./actions":38,"./actions/vre":44,"./components/edit":61,"./store":75,"react":"react","react-dom":"react-dom"}],67:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _utilCloneDeep = require("../util/clone-deep");

var _utilCloneDeep2 = _interopRequireDefault(_utilCloneDeep);

var _utilGetIn = require("../util/get-in");

var _utilGetIn2 = _interopRequireDefault(_utilGetIn);

var parseEntities = undefined;
var matchers = undefined;
var v2UnquotedPropVals = ["wwperson_children", "wwcollective_type"];
var quoteProp = function quoteProp(domain, prop, val) {
	return v2UnquotedPropVals.indexOf(domain + "_" + prop.name) > -1 ? "\"" + val + "\"" : "\"\\\"" + val + "\\\"\"";
};

var identity = function identity(domain) {
	return "g.V().has(\"isLatest\", true).filter{it.get().property(\"types\").value().contains(\"\\\"" + domain + "\\\"\")}";
};

var parsePropVal = function parsePropVal(prop, valueFilter, domain) {
	if (valueFilter.type === "value") {
		return prop.name === "tim_id" ? "has(\"tim_id\", \"" + valueFilter.value + "\")" : "has(\"" + domain + "_" + prop.name + "\").filter{it.get().property(\"" + domain + "_" + prop.name + "\").value().contains(" + quoteProp(domain, prop, valueFilter.value) + ")}";
	} else {
		return "has(\"" + domain + "_" + prop.name + "\")" + (".where(__.values(\"" + domain + "_" + prop.name + "\")") + ".map{ try { return ((String) it).replace(\"\\\"\", \"\").toInteger() } catch (Exception e) { return null; } }" + ".filter{it != null}" + (".is(" + valueFilter.type + "(" + valueFilter.values.join(", ") + ")))");
	}
};

var parseProp = function parseProp(prop, domain) {
	if (prop.or.length === 1) {
		return parsePropVal(prop, prop.or[0], domain);
	}
	return "or(" + prop.or.map(function (pv) {
		return parsePropVal(prop, pv, domain);
	}).join(", ") + ")";
};

var parseProps = function parseProps(props, domain) {
	if (props.length === 0) {
		return "";
	}
	if (props.length === 1) {
		return "." + parseProp(props[0].value, domain);
	}
	return ".and(" + props.map(function (p) {
		return parseProp(p.value, domain);
	}).join(", ") + ")";
};

var parseRelation = function parseRelation(rel, relName, path) {
	var addAlias = arguments.length <= 3 || arguments[3] === undefined ? true : arguments[3];
	return rel.direction + "E(\"" + relName + "\")" + (addAlias ? ".as(\"" + path.join("|") + "\")" : "") + ".otherV()" + parseEntities(rel.or, path.concat(["or"]));
};

var parseRelations = function parseRelations(rels, ent, path) {
	if (rels.length === 0) {
		return "";
	}
	var relQs = rels.map(function (r) {
		return "__." + parseRelation(r.value, r.value.name, path.concat(["and", r.index]), false);
	});

	var aliasedRelQs = rels.map(function (r) {
		return "__." + parseRelation(r.value, r.value.name, path.concat(["and", r.index])) + ".as(\"" + path.concat(r.index).join("|") + "\")";
	});

	return ".and(" + relQs.join(", ") + ").union(" + aliasedRelQs.join(", ") + ")";
};

var parseEntity = function parseEntity(ent) {
	var path = arguments.length <= 1 || arguments[1] === undefined ? ["or", 0] : arguments[1];

	var propFilters = ent.and.map(function (d, i) {
		return { index: i, value: d };
	}).filter(function (f) {
		return f.value.type === "property";
	});

	var relFilters = ent.and.map(function (d, i) {
		return { index: i, value: d };
	}).filter(function (f) {
		return f.value.type === "relation";
	});

	var propQ = parseProps(propFilters, ent.domain);
	var relQ = parseRelations(relFilters, ent, path);

	if (matchers.indexOf(path.join("|")) < 0) {
		matchers.push(path.join("|"));
	}

	return propQ + relQ;
};

parseEntities = function (queries) {
	var path = arguments.length <= 1 || arguments[1] === undefined ? ["or"] : arguments[1];

	var entityQs = queries.map(function (q, i) {
		return parseEntity(q, path.concat(i));
	}).map(function (q) {
		return "__" + (q === "" ? "()" : q);
	});

	var aliasedEntityQs = queries.map(function (q, i) {
		return parseEntity(q, path.concat(i));
	}).map(function (q, i) {
		return "__().as(\"" + path.concat(i).join("|") + "\")" + q;
	});

	return ".as(\"" + path.join("|") + "\").or(" + entityQs.join(", ") + ").union(" + aliasedEntityQs.join(", ") + ")";
};

var parseQuery = function parseQuery(query) {
	var path = query.pathToQuerySelection ? (0, _utilCloneDeep2["default"])(query.pathToQuerySelection) : [];

	if ((0, _utilGetIn2["default"])(path, query) && (0, _utilGetIn2["default"])(path, query).type === "property") {
		path.pop();path.pop();
	} else if ((0, _utilGetIn2["default"])(path, query) && (0, _utilGetIn2["default"])(path, query).type === "value") {
		path.pop();path.pop();path.pop();path.pop();
	}

	var selectVal = path.length ? path.join("|") : "result";
	matchers = ["or"];

	var baseQuery = "" + identity(query.or[0].domain) + parseEntities(query.or);

	return [baseQuery + ".select(\"" + selectVal + "\", \"or\")", baseQuery + ".select(\"" + selectVal + "\").dedup().count()"];
};

var parsers = {
	parseGremlin: parseQuery
};

exports["default"] = parseQuery;
exports.parsers = parsers;

},{"../util/clone-deep":76,"../util/get-in":77}],68:[function(require,module,exports){
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
	list: [],
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

		case "RECEIVE_ENTITY_LIST":
			return _extends({}, state, {
				list: action.data
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

},{"../util/set-in":78}],69:[function(require,module,exports){
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

var _queries = require("./queries");

var _queries2 = _interopRequireDefault(_queries);

var _pagination = require("./pagination");

var _pagination2 = _interopRequireDefault(_pagination);

exports["default"] = {
	vre: _vre2["default"],
	entity: _entity2["default"],
	user: _user2["default"],
	messages: _messages2["default"],
	queries: _queries2["default"],
	pagination: _pagination2["default"]
};
module.exports = exports["default"];

},{"./entity":68,"./messages":70,"./pagination":71,"./queries":72,"./user":73,"./vre":74}],70:[function(require,module,exports){
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

},{"../util/set-in":78}],71:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	start: 0,
	rows: 50
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_PAGINATION_START":
			return _extends({}, state, { start: action.start });
		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],72:[function(require,module,exports){
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

var initialState = {
	queries: [],
	savedQueries: [],
	currentQuery: -1,
	results: "",
	resultCount: "",
	resultsPending: false,
	resultCountPending: false
};

var makeQuery = function makeQuery(domain, position) {
	return {
		name: "",
		domain: domain,
		deleted: false,
		pathToQuerySelection: ["or", 0],
		position: position,
		or: [{
			type: "entity",
			domain: domain,
			and: []
		}]
	};
};

var selectQuery = function selectQuery(state, action) {
	var current = state.queries[action.queryIndex] ? state.queries : (0, _utilSetIn2["default"])([action.queryIndex], makeQuery(action.domain, action.position), state.queries);

	return _extends({}, state, {
		queries: current,
		currentQuery: action.queryIndex
	});
};

var loadSavedQuery = function loadSavedQuery(state, action) {
	var newIndex = state.queries.length;
	var current = (0, _utilSetIn2["default"])([newIndex], state.savedQueries.filter(function (q) {
		return q.name === action.name;
	})[0], state.queries);

	return _extends({}, state, {
		queries: current,
		currentQuery: newIndex
	});
};

var setQueryPath = function setQueryPath(state, action) {
	var current = (0, _utilSetIn2["default"])([state.currentQuery, "pathToQuerySelection"], action.path, state.queries);
	return _extends({}, state, {
		queries: current
	});
};

var setQueryFieldValue = function setQueryFieldValue(state, action) {
	var pathToQuerySelection = state.queries[state.currentQuery].pathToQuerySelection;
	var current = (0, _utilSetIn2["default"])([state.currentQuery].concat(pathToQuerySelection).concat(action.fieldPath), action.value, state.queries);
	return _extends({}, state, {
		queries: current
	});
};

var setQueryName = function setQueryName(state, action) {
	var current = (0, _utilSetIn2["default"])([state.currentQuery].concat("name"), action.value, state.queries);
	return _extends({}, state, {
		queries: current
	});
};

var setQueryPosition = function setQueryPosition(state, action) {
	var current = (0, _utilSetIn2["default"])([action.queryIndex].concat("position"), action.position, state.queries);
	return _extends({}, state, {
		queries: current
	});
};

var getPath = function getPath(state, action) {
	var pathToQuerySelection = state.queries[state.currentQuery].pathToQuerySelection;
	if (typeof action.fieldPath === "number") {
		var fullPath = [state.currentQuery].concat(pathToQuerySelection);
		return fullPath.slice(0, fullPath.length + action.fieldPath);
	} else {
		return [state.currentQuery].concat(pathToQuerySelection).concat(action.fieldPath);
	}
};

var addQueryFilter = function addQueryFilter(state, action) {
	var pathToFilters = getPath(state, action);

	var filters = (0, _utilGetIn2["default"])(pathToFilters, state.queries);
	var current = (0, _utilSetIn2["default"])(pathToFilters.concat(filters.length), action.value, state.queries);

	return _extends({}, state, {
		queries: current
	});
};

var deleteQuery = function deleteQuery(state, action) {
	return _extends({}, state, {
		queries: (0, _utilSetIn2["default"])([action.queryIndex], _extends({}, state.queries[action.queryIndex], { deleted: true }), state.queries),
		currentQuery: -1
	});
};

var deleteQueryFilter = function deleteQueryFilter(state, action) {
	var pathToQuerySelection = state.queries[action.queryIndex].pathToQuerySelection;
	var currentNode = (0, _utilGetIn2["default"])([state.currentQuery].concat(pathToQuerySelection), state.queries);
	var sliceEnd = pathToQuerySelection.length - 1;

	if (typeof currentNode === "object" && currentNode.type === "entity" && (0, _utilGetIn2["default"])([state.currentQuery].concat(pathToQuerySelection.slice(0, sliceEnd)), state.queries).length === 1) {
		sliceEnd = pathToQuerySelection.length - 3;
		deleteQueryFilterIndex = pathToQuerySelection[sliceEnd];
	}

	var deleteQueryFilterIndex = pathToQuerySelection[sliceEnd];
	var queryFilters = (0, _utilGetIn2["default"])([state.currentQuery].concat(pathToQuerySelection.slice(0, sliceEnd)), state.queries);

	queryFilters.splice(deleteQueryFilterIndex, 1);

	var current = (0, _utilSetIn2["default"])([state.currentQuery].concat(pathToQuerySelection.slice(0, sliceEnd)), queryFilters, state.queries);
	current[state.currentQuery].pathToQuerySelection = ["or", 0];
	return _extends({}, state, {
		queries: current
	});
};

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SELECT_QUERY":
			return selectQuery(state, action);
		case "SET_QUERY_PATH":
			return setQueryPath(state, action);
		case "SET_QUERY_FIELD_VALUE":
			return setQueryFieldValue(state, action);
		case "SET_QUERY_NAME":
			return setQueryName(state, action);
		case "SET_QUERY_POSITION":
			return setQueryPosition(state, action);
		case "ADD_QUERY_FILTER":
			return addQueryFilter(state, action);
		case "DELETE_QUERY":
			return deleteQuery(state, action);
		case "DELETE_QUERY_FILTER":
			return deleteQueryFilter(state, action);
		case "LOAD_SAVED_QUERY":
			return loadSavedQuery(state, action);

		case "SET_QUERY_RESULTS_PENDING":
			return _extends({}, state, { results: null, resultCount: null, resultsPending: true, resultCountPending: true });

		case "SET_QUERY_RESULTS":
			return _extends({}, state, { results: { or: action.results.or, root: action.results.results }, resultCount: action.results.resultCount, resultCountPending: false });

		case "SET_QUERY_RESULT_COUNT":
			return _extends({}, state, { resultCount: action.count, resultCountPending: false });

		case "SET_SAVED_QUERIES":
			return _extends({}, state, { savedQueries: action.savedQueries });

	}

	return state;
};

module.exports = exports["default"];

},{"../util/get-in":77,"../util/set-in":78}],73:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
var initialState = null;

exports["default"] = function (state, action) {
	if (state === undefined) state = initialState;

	switch (action.type) {
		case "SET_USER":
			return action.user;

		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],74:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var initialState = {
	vreId: null,
	list: [],
	collections: null
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

		default:
			return state;
	}
};

module.exports = exports["default"];

},{}],75:[function(require,module,exports){
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

//const logger = () => next => action => {
//	if (action.hasOwnProperty("type")) {
//		console.log("[REDUX]", action.type, action);
//	}
//	return next(action);
//};

var data = (0, _redux.combineReducers)(_reducers2["default"]);

exports["default"] = (0, _redux.createStore)(data, (0, _redux.applyMiddleware)( /*logger, */_reduxThunk2["default"]));
module.exports = exports["default"];

},{"../reducers":69,"redux":26,"redux-thunk":20}],76:[function(require,module,exports){
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

},{}],77:[function(require,module,exports){
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

},{"./clone-deep":76}],78:[function(require,module,exports){
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

},{"./clone-deep":76}]},{},[66])(66)
});