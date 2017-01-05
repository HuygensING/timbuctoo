(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.TimbuctooEdit = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

exports.default = function (path, query, done) {
	var options = {
		url: "" + "/v2.1/" + path.replace(/^\/v[^/]+\//, "") + "?query=" + query + "*"
	};

	var xhrDone = function xhrDone(err, response, body) {
		done(JSON.parse(body).map(function (d) {
			return { key: d.key.replace(/^.+\//, ""), value: d.value };
		}));
	};

	_server2.default.fastXhr(options, xhrDone);
};

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

},{"./server":6}],2:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports.crud = exports.fetchEntityList = exports.fetchEntity = exports.deleteEntity = exports.updateEntity = exports.saveNewEntity = undefined;

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var saveNewEntity = function saveNewEntity(domain, saveData, token, vreId, next, fail) {
	return _server2.default.performXhr({
		method: "POST",
		headers: _server2.default.makeHeaders(token, vreId),
		body: JSON.stringify(saveData),
		url: "" + "/v2.1/domain/" + domain
	}, next, fail, "Create new " + domain);
};

var updateEntity = function updateEntity(domain, saveData, token, vreId, next, fail) {
	return _server2.default.performXhr({
		method: "PUT",
		headers: _server2.default.makeHeaders(token, vreId),
		body: JSON.stringify(saveData),
		url: "" + "/v2.1/domain/" + domain + "/" + saveData._id
	}, next, fail, "Update " + domain);
};

var deleteEntity = function deleteEntity(domain, entityId, token, vreId, next, fail) {
	return _server2.default.performXhr({
		method: "DELETE",
		headers: _server2.default.makeHeaders(token, vreId),
		url: "" + "/v2.1/domain/" + domain + "/" + entityId
	}, next, fail, "Delete " + domain);
};

var fetchEntity = function fetchEntity(location, next, fail) {
	return _server2.default.performXhr({
		method: "GET",
		headers: { "Accept": "application/json" },
		url: location
	}, function (err, resp) {
		var data = JSON.parse(resp.body);
		next(data);
	}, fail, "Fetch entity");
};

var fetchEntityList = function fetchEntityList(domain, start, rows, next) {
	return _server2.default.performXhr({
		method: "GET",
		headers: { "Accept": "application/json" },
		url: "" + "/v2.1/domain/" + domain + "?rows=" + rows + "&start=" + start
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

},{"./server":6}],3:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports.selectDomain = exports.sendQuickSearch = exports.paginateLeft = exports.paginateRight = exports.fetchEntityList = exports.deleteEntity = exports.makeNewEntity = exports.selectEntity = exports.saveEntity = undefined;

var _cloneDeep = require("../util/clone-deep");

var _cloneDeep2 = _interopRequireDefault(_cloneDeep);

var _crud = require("./crud");

var _saveRelations = require("./save-relations");

var _saveRelations2 = _interopRequireDefault(_saveRelations);

var _autocomplete = require("./autocomplete");

var _autocomplete2 = _interopRequireDefault(_autocomplete);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
		var _getState = getState(),
		    quickSearch = _getState.quickSearch,
		    entity = _getState.entity,
		    vre = _getState.vre;

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
			(0, _autocomplete2.default)("domain/" + entity.domain + "/autocomplete", quickSearch.query, callback);
		} else {
			dispatch(fetchEntityList(entity.domain));
		}
	};
};

var selectDomain = function selectDomain(domain) {
	return function (dispatch) {
		dispatch({ type: "SET_DOMAIN", domain: domain });
		dispatch(fetchEntityList(domain));
		dispatch({ type: "SET_QUICKSEARCH_QUERY", value: "" });
	};
};

// 1) Fetch entity
// 2) Dispatch RECEIVE_ENTITY for render
var selectEntity = function selectEntity(domain, entityId) {
	var errorMessage = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : null;
	var successMessage = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : null;
	var next = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : function () {};
	return function (dispatch, getState) {
		var _getState2 = getState(),
		    currentDomain = _getState2.entity.domain;

		if (currentDomain !== domain) {
			dispatch(selectDomain(domain));
		}
		_crud.crud.fetchEntity("" + "/v2.1/domain/" + domain + "/" + entityId, function (data) {
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
	var errorMessage = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : null;
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
		var collectionLabel = getState().vre.collections[getState().entity.domain].collectionLabel.replace(/s$/, "");

		// Make a deep copy of the data to be saved in order to leave application state unaltered
		var saveData = (0, _cloneDeep2.default)(getState().entity.data);
		// Make a deep copy of the relation data in order to leave application state unaltered
		var relationData = (0, _cloneDeep2.default)(saveData["@relations"]) || {};
		// Delete the relation data from the saveData as it is not expected by the server
		delete saveData["@relations"];

		if (getState().entity.data._id) {
			// 1) Update the entity with saveData
			_crud.crud.updateEntity(getState().entity.domain, saveData, getState().user.token, getState().vre.vreId, function (err, resp) {
				return (
					// 2) Save relations using server response for current relations to diff against relationData
					dispatch(function (redispatch) {
						return (0, _saveRelations2.default)(JSON.parse(resp.body), relationData, getState().vre.collections[getState().entity.domain].properties, getState().user.token, getState().vre.vreId, function () {
							return (
								// 3) Refetch entity for render
								redispatch(selectEntity(getState().entity.domain, getState().entity.data._id, null, "Succesfully saved " + collectionLabel + " with ID " + getState().entity.data._id, function () {
									return dispatch(fetchEntityList(getState().entity.domain));
								}))
							);
						});
					})
				);
			}, function () {
				return (
					// 2a) Handle error by refetching and passing along an error message
					dispatch(selectEntity(getState().entity.domain, getState().entity.data._id, "Failed to save " + collectionLabel + " with ID " + getState().entity.data._id))
				);
			});
		} else {
			// 1) Create new entity with saveData
			_crud.crud.saveNewEntity(getState().entity.domain, saveData, getState().user.token, getState().vre.vreId, function (err, resp) {
				return (
					// 2) Fetch entity via location header
					dispatch(function (redispatch) {
						return _crud.crud.fetchEntity(resp.headers.location, function (data) {
							return (
								// 3) Save relations using server response for current relations to diff against relationData
								(0, _saveRelations2.default)(data, relationData, getState().vre.collections[getState().entity.domain].properties, getState().user.token, getState().vre.vreId, function () {
									return (
										// 4) Refetch entity for render
										redispatch(selectEntity(getState().entity.domain, data._id, null, "Succesfully saved " + collectionLabel, function () {
											return dispatch(fetchEntityList(getState().entity.domain));
										}))
									);
								})
							);
						});
					})
				);
			}, function () {
				return (
					// 2a) Handle error by refetching and passing along an error message
					dispatch(makeNewEntity(getState().entity.domain, "Failed to save new " + collectionLabel))
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
exports.selectDomain = selectDomain;

},{"../util/clone-deep":38,"./autocomplete":1,"./crud":2,"./save-relations":5}],4:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _entity = require("./entity");

var _vre = require("./vre");

exports.default = function (navigateTo, dispatch) {
	return {
		onNew: function onNew(domain) {
			return dispatch((0, _entity.makeNewEntity)(domain));
		},
		onSelect: function onSelect(record) {
			return dispatch((0, _entity.selectEntity)(record.domain, record.id));
		},
		onSave: function onSave() {
			return dispatch((0, _entity.saveEntity)());
		},
		onDelete: function onDelete() {
			return dispatch((0, _entity.deleteEntity)());
		},
		onChange: function onChange(fieldPath, value) {
			return dispatch({ type: "SET_ENTITY_FIELD_VALUE", fieldPath: fieldPath, value: value });
		},
		onLoginChange: function onLoginChange(response) {
			return dispatch(setUser(response));
		},
		onSelectVre: function onSelectVre(vreId) {
			return dispatch((0, _vre.setVre)(vreId));
		},
		onDismissMessage: function onDismissMessage(messageIndex) {
			return dispatch({ type: "DISMISS_MESSAGE", messageIndex: messageIndex });
		},
		onSelectDomain: function onSelectDomain(domain) {
			dispatch((0, _entity.selectDomain)(domain));
		},
		onPaginateLeft: function onPaginateLeft() {
			return dispatch((0, _entity.paginateLeft)());
		},
		onPaginateRight: function onPaginateRight() {
			return dispatch((0, _entity.paginateRight)());
		},
		onQuickSearchQueryChange: function onQuickSearchQueryChange(value) {
			return dispatch({ type: "SET_QUICKSEARCH_QUERY", value: value });
		},
		onQuickSearch: function onQuickSearch() {
			return dispatch((0, _entity.sendQuickSearch)());
		}
	};
};

},{"./entity":3,"./vre":7}],5:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _crud = require("./crud");

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var saveRelationsV21 = function saveRelationsV21(data, relationData, fieldDefs, token, vreId, next) {
	// Returns the domain based on the fieldDefinitions and the relation key (i.e. "hasBirthPlace")
	var makeRelationArgs = function makeRelationArgs(relation, key) {
		var accepted = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
		var id = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : null;
		var rev = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : null;

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

exports.default = saveRelationsV21;

},{"./crud":2}],6:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

var _store = require("../store");

var _store2 = _interopRequireDefault(_store);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = {
	performXhr: function performXhr(options, accept) {
		var reject = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : function () {
			console.warn("Undefined reject callback! ");
		};
		var operation = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : "Server request";

		_store2.default.dispatch({ type: "REQUEST_MESSAGE", message: operation + ": " + (options.method || "GET") + " " + options.url });
		(0, _xhr2.default)(options, function (err, resp, body) {
			if (resp.statusCode >= 400) {
				_store2.default.dispatch({ type: "ERROR_MESSAGE", message: operation + " failed with cause: " + resp.body });
				reject(err, resp, body);
			} else {
				accept(err, resp, body);
			}
		});
	},

	fastXhr: function fastXhr(options, accept) {
		(0, _xhr2.default)(options, accept);
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

},{"../store":36,"xhr":"xhr"}],7:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
exports.setVre = exports.listVres = undefined;

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

var _index = require("./index");

var _index2 = _interopRequireDefault(_index);

var _entity = require("./entity");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var listVres = function listVres() {
	return function (dispatch) {
		return _server2.default.performXhr({
			method: "GET",
			headers: {
				"Accept": "application/json"
			},
			url: "" + "/v2.1/system/vres"
		}, function (err, resp) {
			dispatch({ type: "LIST_VRES", list: JSON.parse(resp.body) });
		}, null, "List VREs");
	};
};

var setVre = function setVre(vreId) {
	var next = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : function () {};
	return function (dispatch) {
		return _server2.default.performXhr({
			method: "GET",
			headers: {
				"Accept": "application/json"
			},
			url: "" + "/v2.1/metadata/" + vreId + "?withCollectionInfo=true"
		}, function (err, resp) {
			if (resp.statusCode === 200) {
				var body = JSON.parse(resp.body);
				dispatch({ type: "SET_VRE", vreId: vreId, collections: body });

				var defaultDomain = Object.keys(body).map(function (collectionName) {
					return body[collectionName];
				}).filter(function (collection) {
					return !collection.unknown && !collection.relationCollection;
				})[0].collectionName;

				dispatch((0, _entity.makeNewEntity)(defaultDomain));
				dispatch({ type: "SET_DOMAIN", defaultDomain: defaultDomain });
				dispatch((0, _entity.fetchEntityList)(defaultDomain));
				next();
			}
		}, function () {
			return dispatch({ type: "SET_VRE", vreId: vreId, collections: {} });
		}, "Fetch VRE description for " + vreId);
	};
};

exports.listVres = listVres;
exports.setVre = setVre;

},{"./entity":3,"./index":4,"./server":6}],8:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _urls = require("../../urls");

var _reactRouter = require("react-router");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var CollectionTabs = function (_React$Component) {
	_inherits(CollectionTabs, _React$Component);

	function CollectionTabs() {
		_classCallCheck(this, CollectionTabs);

		return _possibleConstructorReturn(this, (CollectionTabs.__proto__ || Object.getPrototypeOf(CollectionTabs)).apply(this, arguments));
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
			var _props = this.props,
			    collections = _props.collections,
			    activeDomain = _props.activeDomain;

			var domains = Object.keys(collections || {});

			return _react2.default.createElement(
				"div",
				{ className: "container basic-margin" },
				_react2.default.createElement(
					"ul",
					{ className: "nav nav-tabs" },
					domains.filter(function (d) {
						return !(collections[d].unknown || collections[d].relationCollection);
					}).map(function (domain) {
						return _react2.default.createElement(
							"li",
							{ className: (0, _classnames2.default)({ active: domain === activeDomain }), key: domain },
							_react2.default.createElement(
								_reactRouter.Link,
								{ to: _urls.urls.newEntity(domain) },
								collections[domain].collectionLabel
							)
						);
					})
				)
			);
		}
	}]);

	return CollectionTabs;
}(_react2.default.Component);

CollectionTabs.propTypes = {
	onNew: _react2.default.PropTypes.func,
	onSelectDomain: _react2.default.PropTypes.func,
	collections: _react2.default.PropTypes.object,
	activeDomain: _react2.default.PropTypes.string
};

exports.default = CollectionTabs;

},{"../../urls":37,"classnames":"classnames","react":"react","react-router":"react-router"}],9:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _page = require("../page.jsx");

var _page2 = _interopRequireDefault(_page);

var _paginate = require("./entity-index/paginate");

var _paginate2 = _interopRequireDefault(_paginate);

var _quicksearch = require("./entity-index/quicksearch");

var _quicksearch2 = _interopRequireDefault(_quicksearch);

var _list = require("./entity-index/list");

var _list2 = _interopRequireDefault(_list);

var _saveFooter = require("./entity-form/save-footer");

var _saveFooter2 = _interopRequireDefault(_saveFooter);

var _form = require("./entity-form/form");

var _form2 = _interopRequireDefault(_form);

var _collectionTabs = require("./collection-tabs");

var _collectionTabs2 = _interopRequireDefault(_collectionTabs);

var _list3 = require("./messages/list");

var _list4 = _interopRequireDefault(_list3);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var EditGui = function (_React$Component) {
	_inherits(EditGui, _React$Component);

	function EditGui() {
		_classCallCheck(this, EditGui);

		return _possibleConstructorReturn(this, (EditGui.__proto__ || Object.getPrototypeOf(EditGui)).apply(this, arguments));
	}

	_createClass(EditGui, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			var _props = this.props,
			    onSelect = _props.onSelect,
			    onNew = _props.onNew,
			    onSelectDomain = _props.onSelectDomain;

			// Triggers fetch data from server based on id from route.

			if (this.props.params.id !== nextProps.params.id) {
				onSelect({ domain: nextProps.params.collection, id: nextProps.params.id });
			} else if (this.props.params.collection !== nextProps.params.collection) {
				onNew(nextProps.params.collection);
				onSelectDomain(nextProps.params.collection);
			}
		}
	}, {
		key: "componentDidMount",
		value: function componentDidMount() {

			if (this.props.params.id) {
				this.props.onSelect({ domain: this.props.params.collection, id: this.props.params.id });
			} else if (this.props.params.collection) {
				this.props.onNew(this.props.params.collection);
				this.props.onSelectDomain(this.props.params.collection);
			}
		}
	}, {
		key: "render",
		value: function render() {
			var _props2 = this.props,
			    onSelect = _props2.onSelect,
			    onNew = _props2.onNew,
			    onSave = _props2.onSave,
			    onDelete = _props2.onDelete,
			    onSelectDomain = _props2.onSelectDomain,
			    onDismissMessage = _props2.onDismissMessage,
			    onChange = _props2.onChange;
			var _props3 = this.props,
			    onQuickSearchQueryChange = _props3.onQuickSearchQueryChange,
			    onQuickSearch = _props3.onQuickSearch,
			    onPaginateLeft = _props3.onPaginateLeft,
			    onPaginateRight = _props3.onPaginateRight;
			var getAutocompleteValues = this.props.getAutocompleteValues;
			var _props4 = this.props,
			    quickSearch = _props4.quickSearch,
			    entity = _props4.entity,
			    vre = _props4.vre,
			    messages = _props4.messages;

			var currentMode = entity.domain && entity.data._id ? "edit" : "new";

			if (entity.domain === null || !vre.collections[entity.domain]) {
				return null;
			}
			return _react2.default.createElement(
				_page2.default,
				null,
				_react2.default.createElement(_collectionTabs2.default, { collections: vre.collections, onNew: onNew, onSelectDomain: onSelectDomain,
					activeDomain: entity.domain }),
				_react2.default.createElement(
					"div",
					{ className: "container" },
					_react2.default.createElement(_list4.default, {
						types: ["SUCCESS_MESSAGE", "ERROR_MESSAGE"],
						messages: messages,
						onDismissMessage: onDismissMessage }),
					_react2.default.createElement(
						"div",
						{ className: "row" },
						_react2.default.createElement(
							"div",
							{ className: "col-sm-6 col-md-4" },
							_react2.default.createElement(_quicksearch2.default, {
								onQuickSearchQueryChange: onQuickSearchQueryChange,
								onQuickSearch: onQuickSearch,
								query: quickSearch.query }),
							_react2.default.createElement(_list2.default, {
								start: quickSearch.start,
								list: quickSearch.list,
								onSelect: onSelect,
								domain: entity.domain })
						),
						entity.domain ? _react2.default.createElement(_form2.default, { currentMode: currentMode, getAutocompleteValues: getAutocompleteValues,
							entity: entity, onNew: onNew, onDelete: onDelete, onChange: onChange,
							properties: vre.collections[entity.domain].properties,
							entityLabel: vre.collections[entity.domain].collectionLabel.replace(/s$/, "") }) : null
					)
				),
				_react2.default.createElement(
					"div",
					{ type: "footer-body" },
					_react2.default.createElement(
						"div",
						{ className: "col-sm-6 col-md-4" },
						_react2.default.createElement(_paginate2.default, {
							start: quickSearch.start,
							listLength: quickSearch.list.length,
							rows: 50,
							onPaginateLeft: onPaginateLeft,
							onPaginateRight: onPaginateRight })
					),
					_react2.default.createElement(
						"div",
						{ className: "col-sm-6 col-md-8" },
						_react2.default.createElement(_saveFooter2.default, { onSave: onSave, onCancel: function onCancel() {
								return currentMode === "edit" ? onSelect({ domain: entity.domain, id: entity.data._id }) : onNew(entity.domain);
							} })
					)
				),
				_react2.default.createElement("div", { type: "footer-body" })
			);
		}
	}]);

	return EditGui;
}(_react2.default.Component);

exports.default = EditGui;

},{"../page.jsx":27,"./collection-tabs":8,"./entity-form/form":18,"./entity-form/save-footer":19,"./entity-index/list":20,"./entity-index/paginate":21,"./entity-index/quicksearch":22,"./messages/list":23,"react":"react"}],10:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (camelCase) {
  return camelCase.replace(/([A-Z0-9])/g, function (match) {
    return " " + match.toLowerCase();
  }).replace(/^./, function (match) {
    return match.toUpperCase();
  });
};

},{}],11:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Field = function (_React$Component) {
	_inherits(Field, _React$Component);

	function Field(props) {
		_classCallCheck(this, Field);

		var _this = _possibleConstructorReturn(this, (Field.__proto__ || Object.getPrototypeOf(Field)).call(this, props));

		_this.state = { newLabel: "", newUrl: "" };
		return _this;
	}

	_createClass(Field, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (nextProps.entity.data._id !== this.props.entity.data._id) {
				this.setState({ newLabel: "", newUrl: "" });
			}
		}
	}, {
		key: "onAdd",
		value: function onAdd() {
			var _props = this.props,
			    name = _props.name,
			    entity = _props.entity,
			    onChange = _props.onChange;

			if (this.state.newLabel.length > 0 && this.state.newUrl.length > 0) {
				onChange([name], (entity.data[name] || []).concat({
					label: this.state.newLabel,
					url: this.state.newUrl
				}));
				this.setState({ newLabel: "", newUrl: "" });
			}
		}
	}, {
		key: "onRemove",
		value: function onRemove(value) {
			var _props2 = this.props,
			    name = _props2.name,
			    entity = _props2.entity,
			    onChange = _props2.onChange;

			onChange([name], entity.data[name].filter(function (val) {
				return val.url !== value.url;
			}));
		}
	}, {
		key: "render",
		value: function render() {
			var _this2 = this;

			var _props3 = this.props,
			    name = _props3.name,
			    entity = _props3.entity,
			    onChange = _props3.onChange;

			var label = (0, _camel2label2.default)(name);
			var values = entity.data[name] || [];
			var itemElements = values.map(function (value) {
				return _react2.default.createElement(
					"div",
					{ key: value.url, className: "item-element" },
					_react2.default.createElement(
						"strong",
						null,
						_react2.default.createElement(
							"a",
							{ href: value.url, target: "_blank" },
							value.label
						)
					),
					_react2.default.createElement(
						"button",
						{ className: "btn btn-blank btn-xs pull-right",
							onClick: function onClick() {
								return _this2.onRemove(value);
							} },
						_react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
					)
				);
			});

			return _react2.default.createElement(
				"div",
				{ className: "basic-margin" },
				_react2.default.createElement(
					"h4",
					null,
					label
				),
				itemElements,
				_react2.default.createElement(
					"div",
					{ style: { width: "100%" } },
					_react2.default.createElement("input", { type: "text", className: "form-control pull-left", value: this.state.newLabel,
						onChange: function onChange(ev) {
							return _this2.setState({ newLabel: ev.target.value });
						},
						placeholder: "Label for url...",
						style: { display: "inline-block", maxWidth: "50%" } }),
					_react2.default.createElement("input", { type: "text", className: "form-control pull-left", value: this.state.newUrl,
						onChange: function onChange(ev) {
							return _this2.setState({ newUrl: ev.target.value });
						},
						onKeyPress: function onKeyPress(ev) {
							return ev.key === "Enter" ? _this2.onAdd() : false;
						},
						placeholder: "Url...",
						style: { display: "inline-block", maxWidth: "calc(50% - 80px)" } }),
					_react2.default.createElement(
						"span",
						{ className: "input-group-btn pull-left" },
						_react2.default.createElement(
							"button",
							{ className: "btn btn-default", onClick: this.onAdd.bind(this) },
							"Add link"
						)
					)
				),
				_react2.default.createElement("div", { style: { width: "100%", clear: "left" } })
			);
		}
	}]);

	return Field;
}(_react2.default.Component);

Field.propTypes = {
	entity: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onChange: _react2.default.PropTypes.func
};

exports.default = Field;

},{"./camel2label":10,"react":"react"}],12:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Field = function (_React$Component) {
	_inherits(Field, _React$Component);

	function Field(props) {
		_classCallCheck(this, Field);

		var _this = _possibleConstructorReturn(this, (Field.__proto__ || Object.getPrototypeOf(Field)).call(this, props));

		_this.state = { newValue: "" };
		return _this;
	}

	_createClass(Field, [{
		key: "componentWillReceiveProps",
		value: function componentWillReceiveProps(nextProps) {
			if (nextProps.entity.data._id !== this.props.entity.data._id) {
				this.setState({ newValue: "" });
			}
		}
	}, {
		key: "onAdd",
		value: function onAdd(value) {
			var _props = this.props,
			    name = _props.name,
			    entity = _props.entity,
			    onChange = _props.onChange;

			onChange([name], (entity.data[name] || []).concat(value));
		}
	}, {
		key: "onRemove",
		value: function onRemove(value) {
			var _props2 = this.props,
			    name = _props2.name,
			    entity = _props2.entity,
			    onChange = _props2.onChange;

			onChange([name], entity.data[name].filter(function (val) {
				return val !== value;
			}));
		}
	}, {
		key: "render",
		value: function render() {
			var _this2 = this;

			var _props3 = this.props,
			    name = _props3.name,
			    entity = _props3.entity,
			    onChange = _props3.onChange;

			var label = (0, _camel2label2.default)(name);
			var values = entity.data[name] || [];
			var itemElements = values.map(function (value) {
				return _react2.default.createElement(
					"div",
					{ key: value, className: "item-element" },
					_react2.default.createElement(
						"strong",
						null,
						value
					),
					_react2.default.createElement(
						"button",
						{ className: "btn btn-blank btn-xs pull-right",
							onClick: function onClick() {
								return _this2.onRemove(value);
							} },
						_react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
					)
				);
			});

			return _react2.default.createElement(
				"div",
				{ className: "basic-margin" },
				_react2.default.createElement(
					"h4",
					null,
					label
				),
				itemElements,
				_react2.default.createElement("input", { type: "text", className: "form-control", value: this.state.newValue,
					onChange: function onChange(ev) {
						return _this2.setState({ newValue: ev.target.value });
					},
					onKeyPress: function onKeyPress(ev) {
						return ev.key === "Enter" ? _this2.onAdd(ev.target.value) : false;
					},
					placeholder: "Add a value..." })
			);
		}
	}]);

	return Field;
}(_react2.default.Component);

Field.propTypes = {
	entity: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onChange: _react2.default.PropTypes.func
};

exports.default = Field;

},{"./camel2label":10,"react":"react"}],13:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var _selectField = require("../../../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Field = function (_React$Component) {
	_inherits(Field, _React$Component);

	function Field() {
		_classCallCheck(this, Field);

		return _possibleConstructorReturn(this, (Field.__proto__ || Object.getPrototypeOf(Field)).apply(this, arguments));
	}

	_createClass(Field, [{
		key: "onAdd",
		value: function onAdd(value) {
			var _props = this.props,
			    name = _props.name,
			    entity = _props.entity,
			    onChange = _props.onChange;

			onChange([name], (entity.data[name] || []).concat(value));
		}
	}, {
		key: "onRemove",
		value: function onRemove(value) {
			var _props2 = this.props,
			    name = _props2.name,
			    entity = _props2.entity,
			    onChange = _props2.onChange;

			onChange([name], entity.data[name].filter(function (val) {
				return val !== value;
			}));
		}
	}, {
		key: "render",
		value: function render() {
			var _this2 = this;

			var _props3 = this.props,
			    name = _props3.name,
			    entity = _props3.entity,
			    onChange = _props3.onChange,
			    options = _props3.options;

			var label = (0, _camel2label2.default)(name);
			var values = entity.data[name] || [];
			var itemElements = values.map(function (value) {
				return _react2.default.createElement(
					"div",
					{ key: value, className: "item-element" },
					_react2.default.createElement(
						"strong",
						null,
						value
					),
					_react2.default.createElement(
						"button",
						{ className: "btn btn-blank btn-xs pull-right",
							onClick: function onClick() {
								return _this2.onRemove(value);
							} },
						_react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
					)
				);
			});

			return _react2.default.createElement(
				"div",
				{ className: "basic-margin" },
				_react2.default.createElement(
					"h4",
					null,
					label
				),
				itemElements,
				_react2.default.createElement(
					_selectField2.default,
					{ onChange: this.onAdd.bind(this), noClear: true, btnClass: "btn-default" },
					_react2.default.createElement(
						"span",
						{ type: "placeholder" },
						"Select ",
						label.toLowerCase()
					),
					options.filter(function (opt) {
						return values.indexOf(opt) < 0;
					}).map(function (option) {
						return _react2.default.createElement(
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
}(_react2.default.Component);

Field.propTypes = {
	entity: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onChange: _react2.default.PropTypes.func,
	options: _react2.default.PropTypes.array
};

exports.default = Field;

},{"../../../fields/select-field":24,"./camel2label":10,"react":"react"}],14:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var _selectField = require("../../../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Field = function (_React$Component) {
  _inherits(Field, _React$Component);

  function Field() {
    _classCallCheck(this, Field);

    return _possibleConstructorReturn(this, (Field.__proto__ || Object.getPrototypeOf(Field)).apply(this, arguments));
  }

  _createClass(Field, [{
    key: "onAdd",
    value: function onAdd() {
      var _props = this.props,
          entity = _props.entity,
          name = _props.name,
          onChange = _props.onChange,
          options = _props.options;

      onChange([name], (entity.data[name] || []).concat({
        components: [{ type: options[0], value: "" }]
      }));
    }
  }, {
    key: "onAddComponent",
    value: function onAddComponent(itemIndex) {
      var _props2 = this.props,
          entity = _props2.entity,
          name = _props2.name,
          onChange = _props2.onChange,
          options = _props2.options;

      var currentComponents = entity.data[name][itemIndex].components;
      onChange([name, itemIndex, "components"], currentComponents.concat({ type: options[0], value: "" }));
    }
  }, {
    key: "onRemoveComponent",
    value: function onRemoveComponent(itemIndex, componentIndex) {
      var _props3 = this.props,
          entity = _props3.entity,
          name = _props3.name,
          onChange = _props3.onChange;

      var currentComponents = entity.data[name][itemIndex].components;
      onChange([name, itemIndex, "components"], currentComponents.filter(function (component, idx) {
        return idx !== componentIndex;
      }));
    }
  }, {
    key: "onChangeComponentValue",
    value: function onChangeComponentValue(itemIndex, componentIndex, value) {
      var _props4 = this.props,
          entity = _props4.entity,
          name = _props4.name,
          onChange = _props4.onChange;

      var currentComponents = entity.data[name][itemIndex].components;
      onChange([name, itemIndex, "components"], currentComponents.map(function (component, idx) {
        return idx === componentIndex ? _extends({}, component, { value: value }) : component;
      }));
    }
  }, {
    key: "onChangeComponentType",
    value: function onChangeComponentType(itemIndex, componentIndex, type) {
      var _props5 = this.props,
          entity = _props5.entity,
          name = _props5.name,
          onChange = _props5.onChange;

      var currentComponents = entity.data[name][itemIndex].components;
      onChange([name, itemIndex, "components"], currentComponents.map(function (component, idx) {
        return idx === componentIndex ? _extends({}, component, { type: type }) : component;
      }));
    }
  }, {
    key: "onRemove",
    value: function onRemove(itemIndex) {
      var _props6 = this.props,
          entity = _props6.entity,
          name = _props6.name,
          onChange = _props6.onChange;

      onChange([name], entity.data[name].filter(function (name, idx) {
        return idx !== itemIndex;
      }));
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var _props7 = this.props,
          name = _props7.name,
          entity = _props7.entity,
          options = _props7.options;

      var label = (0, _camel2label2.default)(name);
      var values = entity.data[name] || [];

      var nameElements = values.map(function (name, i) {
        return _react2.default.createElement(
          "div",
          { key: name + "-" + i, className: "names-form item-element" },
          _react2.default.createElement(
            "div",
            { className: "small-margin" },
            _react2.default.createElement(
              "button",
              { className: "btn btn-blank btn-xs pull-right",
                onClick: function onClick() {
                  return _this2.onRemove(i);
                },
                type: "button" },
              _react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
            ),
            _react2.default.createElement(
              "strong",
              null,
              name.components.map(function (component) {
                return component.value;
              }).join(" ")
            )
          ),
          _react2.default.createElement(
            "ul",
            { key: "component-list" },
            name.components.map(function (component, j) {
              return _react2.default.createElement(
                "li",
                { key: i + "-" + j + "-component" },
                _react2.default.createElement(
                  "div",
                  { className: "input-group", key: "component-values" },
                  _react2.default.createElement(
                    "div",
                    { className: "input-group-btn" },
                    _react2.default.createElement(
                      _selectField2.default,
                      { value: component.type, noClear: true,
                        onChange: function onChange(val) {
                          return _this2.onChangeComponentType(i, j, val);
                        },
                        btnClass: "btn-default" },
                      options.map(function (option) {
                        return _react2.default.createElement(
                          "span",
                          { value: option, key: option },
                          option
                        );
                      })
                    )
                  ),
                  _react2.default.createElement("input", { type: "text", className: "form-control", key: "input-" + i + "-" + j,
                    onChange: function onChange(ev) {
                      return _this2.onChangeComponentValue(i, j, ev.target.value);
                    },
                    placeholder: component.type, value: component.value }),
                  _react2.default.createElement(
                    "span",
                    { className: "input-group-btn" },
                    _react2.default.createElement(
                      "button",
                      { className: "btn btn-default", onClick: function onClick() {
                          return _this2.onRemoveComponent(i, j);
                        } },
                      _react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
                    )
                  )
                )
              );
            })
          ),
          _react2.default.createElement(
            "button",
            { onClick: function onClick() {
                return _this2.onAddComponent(i);
              },
              className: "btn btn-default btn-xs pull-right", type: "button" },
            "Add component"
          ),
          _react2.default.createElement("div", { style: { width: "100%", height: "6px", clear: "right" } })
        );
      });
      return _react2.default.createElement(
        "div",
        { className: "basic-margin" },
        _react2.default.createElement(
          "h4",
          null,
          label
        ),
        nameElements,
        _react2.default.createElement(
          "button",
          { className: "btn btn-default", onClick: this.onAdd.bind(this) },
          "Add name"
        )
      );
    }
  }]);

  return Field;
}(_react2.default.Component);

Field.propTypes = {
  entity: _react2.default.PropTypes.object,
  name: _react2.default.PropTypes.string,
  options: _react2.default.PropTypes.array,
  onChange: _react2.default.PropTypes.func
};

exports.default = Field;

},{"../../../fields/select-field":24,"./camel2label":10,"react":"react"}],15:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var RelationField = function (_React$Component) {
  _inherits(RelationField, _React$Component);

  function RelationField(props) {
    _classCallCheck(this, RelationField);

    var _this = _possibleConstructorReturn(this, (RelationField.__proto__ || Object.getPrototypeOf(RelationField)).call(this, props));

    _this.state = {
      query: "",
      suggestions: [],
      blurIsBlocked: false
    };
    return _this;
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
      this.setState({ suggestions: [], query: "", blurIsBlocked: false });

      this.props.onChange(["@relations", this.props.name], currentValues.concat({
        id: suggestion.key,
        displayName: suggestion.value,
        accepted: true
      }));
    }
  }, {
    key: "onQueryChange",
    value: function onQueryChange(ev) {
      var _this2 = this;

      var _props = this.props,
          getAutocompleteValues = _props.getAutocompleteValues,
          path = _props.path;

      this.setState({ query: ev.target.value });
      if (ev.target.value === "") {
        this.setState({ suggestions: [] });
      } else {
        getAutocompleteValues(path, ev.target.value, function (results) {
          _this2.setState({ suggestions: results });
        });
      }
    }
  }, {
    key: "onQueryClear",
    value: function onQueryClear(ev) {
      if (!this.state.blurIsBlocked) {
        this.setState({ suggestions: [], query: "" });
      }
    }
  }, {
    key: "onBlurBlock",
    value: function onBlurBlock(toggle) {
      this.setState({ blurIsBlocked: toggle });
    }
  }, {
    key: "render",
    value: function render() {
      var _this3 = this;

      var _props2 = this.props,
          name = _props2.name,
          entity = _props2.entity,
          onChange = _props2.onChange;

      var values = entity.data["@relations"][this.props.name] || [];
      var itemElements = values.filter(function (val) {
        return val.accepted;
      }).map(function (value, i) {
        return _react2.default.createElement(
          "div",
          { key: i + "-" + value.id, className: "item-element" },
          _react2.default.createElement(
            "strong",
            null,
            value.displayName
          ),
          _react2.default.createElement(
            "button",
            { className: "btn btn-blank btn-xs pull-right",
              onClick: function onClick() {
                return _this3.onRemove(value);
              } },
            _react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
          )
        );
      });

      return _react2.default.createElement(
        "div",
        { className: "basic-margin" },
        _react2.default.createElement(
          "h4",
          null,
          (0, _camel2label2.default)(name)
        ),
        itemElements,
        _react2.default.createElement("input", { className: "form-control",
          onBlur: this.onQueryClear.bind(this),
          onChange: this.onQueryChange.bind(this),
          value: this.state.query, placeholder: "Search..." }),
        _react2.default.createElement(
          "div",
          { onMouseOver: function onMouseOver() {
              return _this3.onBlurBlock(true);
            },
            onMouseOut: function onMouseOut() {
              return _this3.onBlurBlock(false);
            },
            style: { overflowY: "auto", maxHeight: "300px" } },
          this.state.suggestions.map(function (suggestion, i) {
            return _react2.default.createElement(
              "a",
              { key: i + "-" + suggestion.key, className: "item-element",
                onClick: function onClick() {
                  return _this3.onAdd(suggestion);
                } },
              suggestion.value
            );
          })
        )
      );
    }
  }]);

  return RelationField;
}(_react2.default.Component);

exports.default = RelationField;

},{"./camel2label":10,"react":"react"}],16:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

var _selectField = require("../../../fields/select-field");

var _selectField2 = _interopRequireDefault(_selectField);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Field = function (_React$Component) {
	_inherits(Field, _React$Component);

	function Field() {
		_classCallCheck(this, Field);

		return _possibleConstructorReturn(this, (Field.__proto__ || Object.getPrototypeOf(Field)).apply(this, arguments));
	}

	_createClass(Field, [{
		key: "render",
		value: function render() {
			var _props = this.props,
			    name = _props.name,
			    entity = _props.entity,
			    _onChange = _props.onChange,
			    options = _props.options;

			var label = (0, _camel2label2.default)(name);
			var itemElement = entity.data[name] && entity.data[name].length > 0 ? _react2.default.createElement(
				"div",
				{ className: "item-element" },
				_react2.default.createElement(
					"strong",
					null,
					entity.data[name]
				),
				_react2.default.createElement(
					"button",
					{ className: "btn btn-blank btn-xs pull-right",
						onClick: function onClick() {
							return _onChange([name], "");
						} },
					_react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
				)
			) : null;

			return _react2.default.createElement(
				"div",
				{ className: "basic-margin" },
				_react2.default.createElement(
					"h4",
					null,
					label
				),
				itemElement,
				_react2.default.createElement(
					_selectField2.default,
					{
						onChange: function onChange(value) {
							return _onChange([name], value);
						},
						noClear: true, btnClass: "btn-default" },
					_react2.default.createElement(
						"span",
						{ type: "placeholder" },
						"Select ",
						label.toLowerCase()
					),
					options.map(function (option) {
						return _react2.default.createElement(
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
}(_react2.default.Component);

Field.propTypes = {
	entity: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onChange: _react2.default.PropTypes.func,
	options: _react2.default.PropTypes.array
};

exports.default = Field;

},{"../../../fields/select-field":24,"./camel2label":10,"react":"react"}],17:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _camel2label = require("./camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var StringField = function (_React$Component) {
	_inherits(StringField, _React$Component);

	function StringField() {
		_classCallCheck(this, StringField);

		return _possibleConstructorReturn(this, (StringField.__proto__ || Object.getPrototypeOf(StringField)).apply(this, arguments));
	}

	_createClass(StringField, [{
		key: "render",
		value: function render() {
			var _props = this.props,
			    name = _props.name,
			    entity = _props.entity,
			    _onChange = _props.onChange;

			var label = (0, _camel2label2.default)(name);

			return _react2.default.createElement(
				"div",
				{ className: "basic-margin" },
				_react2.default.createElement(
					"h4",
					null,
					label
				),
				_react2.default.createElement("input", { className: "form-control",
					onChange: function onChange(ev) {
						return _onChange([name], ev.target.value);
					},
					value: entity.data[name] || "",
					placeholder: "Enter " + label.toLowerCase()
				})
			);
		}
	}]);

	return StringField;
}(_react2.default.Component);

StringField.propTypes = {
	entity: _react2.default.PropTypes.object,
	name: _react2.default.PropTypes.string,
	onChange: _react2.default.PropTypes.func
};

exports.default = StringField;

},{"./camel2label":10,"react":"react"}],18:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _stringField = require("./fields/string-field");

var _stringField2 = _interopRequireDefault(_stringField);

var _select = require("./fields/select");

var _select2 = _interopRequireDefault(_select);

var _multiSelect = require("./fields/multi-select");

var _multiSelect2 = _interopRequireDefault(_multiSelect);

var _relation = require("./fields/relation");

var _relation2 = _interopRequireDefault(_relation);

var _listOfStrings = require("./fields/list-of-strings");

var _listOfStrings2 = _interopRequireDefault(_listOfStrings);

var _links = require("./fields/links");

var _links2 = _interopRequireDefault(_links);

var _names = require("./fields/names");

var _names2 = _interopRequireDefault(_names);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var fieldMap = {
  "string": function string(fieldDef, props) {
    return _react2.default.createElement(_stringField2.default, _extends({}, props, { name: fieldDef.name }));
  },
  "text": function text(fieldDef, props) {
    return _react2.default.createElement(_stringField2.default, _extends({}, props, { name: fieldDef.name }));
  },
  "datable": function datable(fieldDef, props) {
    return _react2.default.createElement(_stringField2.default, _extends({}, props, { name: fieldDef.name }));
  },
  "multiselect": function multiselect(fieldDef, props) {
    return _react2.default.createElement(_multiSelect2.default, _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
  },
  "select": function select(fieldDef, props) {
    return _react2.default.createElement(_select2.default, _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
  },
  "relation": function relation(fieldDef, props) {
    return _react2.default.createElement(_relation2.default, _extends({}, props, { name: fieldDef.name, path: fieldDef.quicksearch }));
  },
  "list-of-strings": function listOfStrings(fieldDef, props) {
    return _react2.default.createElement(_listOfStrings2.default, _extends({}, props, { name: fieldDef.name }));
  },
  "links": function links(fieldDef, props) {
    return _react2.default.createElement(_links2.default, _extends({}, props, { name: fieldDef.name }));
  },
  "names": function names(fieldDef, props) {
    return _react2.default.createElement(_names2.default, _extends({}, props, { name: fieldDef.name, options: fieldDef.options }));
  }
};

var EntityForm = function (_React$Component) {
  _inherits(EntityForm, _React$Component);

  function EntityForm() {
    _classCallCheck(this, EntityForm);

    return _possibleConstructorReturn(this, (EntityForm.__proto__ || Object.getPrototypeOf(EntityForm)).apply(this, arguments));
  }

  _createClass(EntityForm, [{
    key: "render",
    value: function render() {
      var _props = this.props,
          onNew = _props.onNew,
          onDelete = _props.onDelete,
          onChange = _props.onChange,
          getAutocompleteValues = _props.getAutocompleteValues;
      var _props2 = this.props,
          entity = _props2.entity,
          currentMode = _props2.currentMode,
          properties = _props2.properties,
          entityLabel = _props2.entityLabel;


      return _react2.default.createElement(
        "div",
        { className: "col-sm-6 col-md-8" },
        _react2.default.createElement(
          "div",
          { className: "basic-margin" },
          _react2.default.createElement(
            "button",
            { className: "btn btn-primary pull-right", onClick: function onClick() {
                return onNew(entity.domain);
              } },
            "New ",
            entityLabel
          )
        ),
        properties.filter(function (fieldDef) {
          return !fieldMap.hasOwnProperty(fieldDef.type);
        }).map(function (fieldDef, i) {
          return _react2.default.createElement(
            "div",
            { key: i, style: { "color": "red" } },
            _react2.default.createElement(
              "strong",
              null,
              "Field type not supported: ",
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
        currentMode === "edit" ? _react2.default.createElement(
          "div",
          { className: "basic-margin" },
          _react2.default.createElement(
            "h4",
            null,
            "Delete"
          ),
          _react2.default.createElement(
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
}(_react2.default.Component);

exports.default = EntityForm;

},{"./fields/links":11,"./fields/list-of-strings":12,"./fields/multi-select":13,"./fields/names":14,"./fields/relation":15,"./fields/select":16,"./fields/string-field":17,"react":"react"}],19:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var onSave = props.onSave,
      onCancel = props.onCancel;


  return _react2.default.createElement(
    "div",
    null,
    _react2.default.createElement(
      "button",
      { className: "btn btn-primary", onClick: onSave },
      "Save"
    ),
    " ",
    "or",
    " ",
    _react2.default.createElement(
      "button",
      { className: "btn btn-link", onClick: onCancel },
      "Cancel"
    )
  );
};

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

},{"react":"react"}],20:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var start = props.start,
      list = props.list,
      domain = props.domain;


  return _react2.default.createElement(
    "div",
    { className: "result-list result-list-edit" },
    _react2.default.createElement(
      "ol",
      { start: start + 1, style: { counterReset: "step-counter " + start } },
      list.map(function (entry, i) {
        return _react2.default.createElement(
          "li",
          { key: i + "-" + entry._id },
          _react2.default.createElement(
            _reactRouter.Link,
            { to: _urls.urls.entity(domain, entry._id), style: { display: "inline-block", width: "calc(100% - 30px)", height: "100%", padding: "0.5em 0" } },
            entry["@displayName"]
          )
        );
      })
    )
  );
};

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactRouter = require("react-router");

var _urls = require("../../../urls");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

},{"../../../urls":37,"react":"react","react-router":"react-router"}],21:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var onPaginateLeft = props.onPaginateLeft,
      onPaginateRight = props.onPaginateRight;
  var start = props.start,
      rows = props.rows,
      listLength = props.listLength;


  return _react2.default.createElement(
    "div",
    null,
    _react2.default.createElement(
      "button",
      { className: "btn btn-default", disabled: start === 0, onClick: onPaginateLeft },
      _react2.default.createElement("span", { className: "glyphicon glyphicon-chevron-left" })
    ),
    " ",
    start + 1,
    " - ",
    start + rows,
    " ",
    _react2.default.createElement(
      "button",
      { className: "btn btn-default", disabled: listLength < rows, onClick: onPaginateRight },
      _react2.default.createElement("span", { className: "glyphicon glyphicon-chevron-right" })
    )
  );
};

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

},{"react":"react"}],22:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var onQuickSearchQueryChange = props.onQuickSearchQueryChange,
      onQuickSearch = props.onQuickSearch,
      query = props.query;


  return _react2.default.createElement(
    "div",
    { className: "input-group small-margin " },
    _react2.default.createElement("input", { type: "text", placeholder: "Search for...", className: "form-control",
      onChange: function onChange(ev) {
        return onQuickSearchQueryChange(ev.target.value);
      },
      onKeyPress: function onKeyPress(ev) {
        return ev.key === "Enter" ? onQuickSearch() : false;
      },
      value: query
    }),
    _react2.default.createElement(
      "span",
      { className: "input-group-btn" },
      _react2.default.createElement(
        "button",
        { className: "btn btn-default", onClick: onQuickSearch },
        _react2.default.createElement("span", { className: "glyphicon glyphicon-search" })
      ),
      _react2.default.createElement(
        "button",
        { className: "btn btn-blank", onClick: function onClick() {
            onQuickSearchQueryChange("");onQuickSearch();
          } },
        _react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
      )
    )
  );
};

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

},{"react":"react"}],23:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _classnames = require("classnames");

var _classnames2 = _interopRequireDefault(_classnames);

var _message = require("../../message");

var _message2 = _interopRequireDefault(_message);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var LABELS = {
	"SUCCESS_MESSAGE": "",
	"ERROR_MESSAGE": _react2.default.createElement(
		"span",
		null,
		_react2.default.createElement("span", { className: "glyphicon glyphicon-exclamation-sign" }),
		" Warning!"
	)
};

var ALERT_LEVELS = {
	"SUCCESS_MESSAGE": "info",
	"ERROR_MESSAGE": "danger"
};

var Messages = function (_React$Component) {
	_inherits(Messages, _React$Component);

	function Messages() {
		_classCallCheck(this, Messages);

		return _possibleConstructorReturn(this, (Messages.__proto__ || Object.getPrototypeOf(Messages)).apply(this, arguments));
	}

	_createClass(Messages, [{
		key: "render",
		value: function render() {
			var _props = this.props,
			    messages = _props.messages,
			    types = _props.types,
			    onDismissMessage = _props.onDismissMessage;


			var filteredMessages = messages.log.map(function (msg, idx) {
				return { message: msg.message, index: idx, type: msg.type, dismissed: msg.dismissed };
			}).filter(function (msg) {
				return types.indexOf(msg.type) > -1 && !msg.dismissed;
			});

			return _react2.default.createElement(
				"div",
				null,
				filteredMessages.map(function (msg) {
					return _react2.default.createElement(
						_message2.default,
						{ key: msg.index,
							dismissible: true,
							alertLevel: ALERT_LEVELS[msg.type],
							onCloseMessage: function onCloseMessage() {
								return onDismissMessage(msg.index);
							} },
						_react2.default.createElement(
							"strong",
							null,
							LABELS[msg.type]
						),
						" ",
						_react2.default.createElement(
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
}(_react2.default.Component);

Messages.propTypes = {
	messages: _react2.default.PropTypes.object,
	onDismissMessage: _react2.default.PropTypes.func.isRequired,
	types: _react2.default.PropTypes.array.isRequired
};

exports.default = Messages;

},{"../../message":26,"classnames":"classnames","react":"react"}],24:[function(require,module,exports){
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

      var _props = this.props,
          onChange = _props.onChange,
          onClear = _props.onClear,
          value = _props.value,
          btnClass = _props.btnClass,
          noClear = _props.noClear;


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
          { className: (0, _classnames2.default)("btn", "dropdown-toggle", btnClass || "btn-blank"), onClick: this.toggleSelect.bind(this) },
          selectedOption.length ? selectedOption : placeholder,
          " ",
          _react2.default.createElement("span", { className: "caret" })
        ),
        _react2.default.createElement(
          "ul",
          { className: "dropdown-menu" },
          value && !noClear ? _react2.default.createElement(
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
  value: _react2.default.PropTypes.any,
  btnClass: _react2.default.PropTypes.string,
  noClear: _react2.default.PropTypes.bool
};

exports.default = SelectField;

},{"classnames":"classnames","react":"react","react-dom":"react-dom"}],25:[function(require,module,exports){
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
          child
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

},{"react":"react"}],26:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var dismissible = props.dismissible,
      alertLevel = props.alertLevel,
      onCloseMessage = props.onCloseMessage;

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

},{"classnames":"classnames","react":"react"}],27:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _footer = require("./footer");

var _footer2 = _interopRequireDefault(_footer);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var FOOTER_HEIGHT = 81;

function Page(props) {
  var footers = _react2.default.Children.toArray(props.children).filter(function (child) {
    return child.props.type === "footer-body";
  });

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
      { style: { marginBottom: FOOTER_HEIGHT * footers.length + "px" } },
      _react2.default.Children.toArray(props.children).filter(function (child) {
        return child.props.type !== "footer-body";
      })
    ),
    _react2.default.createElement(
      _footer2.default,
      null,
      footers
    )
  );
}

exports.default = Page;

},{"./footer":25,"react":"react"}],28:[function(require,module,exports){
"use strict";

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactDom = require("react-dom");

var _reactDom2 = _interopRequireDefault(_reactDom);

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var _vre = require("./actions/vre");

var _editGui = require("./components/edit-gui/edit-gui");

var _editGui2 = _interopRequireDefault(_editGui);

var _autocomplete = require("./actions/autocomplete");

var _autocomplete2 = _interopRequireDefault(_autocomplete);

var _router = require("./router");

var _router2 = _interopRequireDefault(_router);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var setUser = function setUser(response) {
	return {
		type: "SET_USER",
		user: response
	};
};

document.addEventListener("DOMContentLoaded", function () {

	function initRouter() {
		_reactDom2.default.render(_router2.default, document.getElementById("app"));
	}

	function getVreId() {
		var path = window.location.search.substr(1);
		var params = path.split("&");

		for (var i in params) {
			var _params$i$split = params[i].split("="),
			    _params$i$split2 = _slicedToArray(_params$i$split, 2),
			    key = _params$i$split2[0],
			    value = _params$i$split2[1];

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
			var _params$i$split3 = params[i].split("="),
			    _params$i$split4 = _slicedToArray(_params$i$split3, 2),
			    key = _params$i$split4[0],
			    value = _params$i$split4[1];

			if (key === "hsid") {
				return { user: value, token: value };
			}
		}
		return undefined;
	}
	_store2.default.dispatch((0, _vre.setVre)(getVreId(), initRouter));
	_store2.default.dispatch(setUser(getLogin()));
});

},{"./actions":4,"./actions/autocomplete":1,"./actions/vre":7,"./components/edit-gui/edit-gui":9,"./router":35,"./store":36,"react":"react","react-dom":"react-dom"}],29:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
	var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
	var action = arguments[1];

	switch (action.type) {
		case "RECEIVE_ENTITY":
			return _extends({}, state, {
				data: action.data,
				domain: action.domain,
				errorMessage: action.errorMessage || null
			});

		case "SET_ENTITY_FIELD_VALUE":
			return _extends({}, state, {
				data: (0, _setIn2.default)(action.fieldPath, action.value, state.data)
			});

		case "RECEIVE_ENTITY_FAILURE":
			return _extends({}, state, {
				data: {
					"@relations": []
				},
				errorMessage: action.errorMessage
			});

		case "SET_VRE":
			{
				return initialState;
			}

	}

	return state;
};

var _setIn = require("../util/set-in");

var _setIn2 = _interopRequireDefault(_setIn);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var initialState = {
	data: {
		"@relations": []
	},
	domain: null,
	errorMessage: null
};

},{"../util/set-in":39}],30:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _redux = require("redux");

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

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (0, _redux.combineReducers)({
	vre: _vre2.default,
	entity: _entity2.default,
	user: _user2.default,
	messages: _messages2.default,
	quickSearch: _quickSearch2.default
});

},{"./entity":29,"./messages":31,"./quick-search":32,"./user":33,"./vre":34,"redux":"redux"}],31:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
	var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
	var action = arguments[1];

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
				log: (0, _setIn2.default)([action.messageIndex, "dismissed"], true, state.log)
			});
	}

	return state;
};

var _setIn = require("../util/set-in");

var _setIn2 = _interopRequireDefault(_setIn);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var initialState = {
	log: []
};

},{"../util/set-in":39}],32:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
	var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
	var action = arguments[1];

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

var initialState = {
	start: 0,
	list: [],
	rows: 50,
	query: ""
};

},{}],33:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

exports.default = function () {
	var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
	var action = arguments[1];

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

var initialState = null;

},{}],34:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = function () {
	var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : initialState;
	var action = arguments[1];

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

var initialState = {
	vreId: null,
	list: [],
	collections: {},
	domain: null
};

},{}],35:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.navigateTo = navigateTo;

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

var _reactRouter = require("react-router");

var _reactRedux = require("react-redux");

var _store = require("./store");

var _store2 = _interopRequireDefault(_store);

var _autocomplete = require("./actions/autocomplete");

var _autocomplete2 = _interopRequireDefault(_autocomplete);

var _actions = require("./actions");

var _actions2 = _interopRequireDefault(_actions);

var _editGui = require("./components/edit-gui/edit-gui");

var _editGui2 = _interopRequireDefault(_editGui);

var _urls = require("./urls");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function navigateTo(key, args) {
	_reactRouter.hashHistory.push(_urls.urls[key].apply(null, args));
}

var defaultConnect = (0, _reactRedux.connect)(function (state) {
	return _extends({}, state, { getAutocompleteValues: _autocomplete2.default });
}, function (dispatch) {
	return (0, _actions2.default)(navigateTo, dispatch);
});

exports.default = _react2.default.createElement(
	_reactRedux.Provider,
	{ store: _store2.default },
	_react2.default.createElement(
		_reactRouter.Router,
		{ history: _reactRouter.hashHistory },
		_react2.default.createElement(_reactRouter.Route, { path: _urls.urls.root(), components: defaultConnect(_editGui2.default) }),
		_react2.default.createElement(_reactRouter.Route, { path: _urls.urls.newEntity(), components: defaultConnect(_editGui2.default) }),
		_react2.default.createElement(_reactRouter.Route, { path: _urls.urls.entity(), components: defaultConnect(_editGui2.default) })
	)
);

},{"./actions":4,"./actions/autocomplete":1,"./components/edit-gui/edit-gui":9,"./store":36,"./urls":37,"react":"react","react-redux":"react-redux","react-router":"react-router"}],36:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _redux = require("redux");

var _reduxThunk = require("redux-thunk");

var _reduxThunk2 = _interopRequireDefault(_reduxThunk);

var _reducers = require("../reducers");

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

},{"../reducers":30,"redux":"redux","redux-thunk":"redux-thunk"}],37:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});
var urls = {
	root: function root() {
		return "/";
	},
	newEntity: function newEntity(collection) {
		return collection ? "/" + collection : "/:collection";
	},
	entity: function entity(collection, id) {
		return collection && id ? "/" + collection + "/" + id : "/:collection/:id";
	}
};

exports.urls = urls;

},{}],38:[function(require,module,exports){
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

},{}],39:[function(require,module,exports){
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
	var deref = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : null;
	return path.length > 1 ? _setIn(path, value, data, deref ? deref[path.shift()] : data[path.shift()]) : setEither(data, deref, path[0], value);
};

var setIn = function setIn(path, value, data) {
	return _setIn((0, _cloneDeep2.default)(path), value, (0, _cloneDeep2.default)(data));
};

exports.default = setIn;

},{"./clone-deep":38}]},{},[28])(28)
});
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDRWUsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFJLFVBQVU7QUFDYixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEYSxFQUFkOztBQUlBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxPQUFLLEtBQUssS0FBTCxDQUFXLElBQVgsRUFBaUIsR0FBakIsQ0FBcUIsVUFBQyxDQUFELEVBQU87QUFBRSxVQUFPLEVB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QkFFYyxVOzs7Ozs7Ozs7QUN4QmY7Ozs7OztBQUVBO0FBQ0E7QUFDQTtBQUNBLElBQU0sWUFBWSxTQUFaLFNBQVksQ0FBQyxJQUFELEVBQU8sS0FBUCxFQUFjLEdBQWQsRUFBbUIsR0FBbkIsRUFBMkI7QUFDNUMsRUFBQyxTQUFTLElBQVYsRUFBZ0IsR0FBaEIsSUFBdUIsR0FBdkI7QUFDQSxRQUFPLElBQVA7QUFDQSxDQUhEOztBQUtBO0FBQ0EsSUFBTSxTQUFTLFNBQVQsTUFBUyxDQUFDLElBQUQsRUFBTyxLQUFQLEVBQWMsSUFBZDtBQUFBLEtBQW9CLEtBQXBCLHVFQUE0QixJQUE1QjtBQUFBLFFBQ2QsS0FBSyxNQUFMLEdBQWMsQ0FBZCxHQUNDLE9BQU8sSUFBUCxFQUFhLEtBQWIsRUFBb0IsSUFBcEIsRUFBMEIsUUFBUSxNQUFNLEtBQUssS0FBTCxFQUFOLENBQVIsR0FBOEIsS0FBSyxLQUFLLEtBQUwsRUFBTCxDQUF4RCxDQURELEdBRUMsVUFBVSxJQUFWLEVBQWdCLEtBQWhCLEVBQXVCLEtBQUssQ0FBTCxDQUF2QixFQUFnQyxLQUFoQyxDQUhhO0FBQUEsQ0FBZjs7QUFLQSxJQUFNLFFBQVEsU0FBUixLQUFRLENBQUMsSUFBRCxFQUFPLEtBQVAsRUFBYyxJQUFkO0FBQUEsUUFDYixPQUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHBhdGgsIHF1ZXJ5LCBkb25lKSB7XG5cdGxldCBvcHRpb25zID0ge1xuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xLyR7cGF0aC5yZXBsYWNlKC9eXFwvdlteL10rXFwvLywgXCJcIil9P3F1ZXJ5PSR7cXVlcnl9KmBcblx0fTtcblxuXHRsZXQgeGhyRG9uZSA9IGZ1bmN0aW9uKGVyciwgcmVzcG9uc2UsIGJvZHkpIHtcblx0XHRkb25lKEpTT04ucGFyc2UoYm9keSkubWFwKChkKSA9PiB7IHJldHVybiB7a2V5OiBkLmtleS5yZXBsYWNlKC9eLitcXC8vLCBcIlwiKSwgdmFsdWU6IGQudmFsdWV9OyB9KSk7XG5cdH07XG5cblx0c2VydmVyLmZhc3RYaHIob3B0aW9ucywgeGhyRG9uZSk7XG59IiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcblxuY29uc3Qgc2F2ZU5ld0VudGl0eSA9IChkb21haW4sIHNhdmVEYXRhLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiUE9TVFwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdGJvZHk6IEpTT04uc3RyaW5naWZ5KHNhdmVEYXRhKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59YFxuXHR9LCBuZXh0LCBmYWlsLCBgQ3JlYXRlIG5ldyAke2RvbWFpbn1gKTtcblxuY29uc3QgdXBkYXRlRW50aXR5ID0gKGRvbWFpbiwgc2F2ZURhdGEsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJQVVRcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHRib2R5OiBKU09OLnN0cmluZ2lmeShzYXZlRGF0YSksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke3NhdmVEYXRhLl9pZH1gXG5cdH0sIG5leHQsIGZhaWwsIGBVcGRhdGUgJHtkb21haW59YCk7XG5cbmNvbnN0IGRlbGV0ZUVudGl0eSA9IChkb21haW4sIGVudGl0eUlkLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiREVMRVRFXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke2VudGl0eUlkfWBcblx0fSwgbmV4dCwgZmFpbCwgYERlbGV0ZSAke2RvbWFpbn1gKTtcblxuY29uc3QgZmV0Y2hFbnRpdHkgPSAobG9jYXRpb24sIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1wiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwifSxcblx0XHR1cmw6IGxvY2F0aW9uXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdG5leHQoZGF0YSk7XG5cdH0sIGZhaWwsIFwiRmV0Y2ggZW50aXR5XCIpO1xuXG5jb25zdCBmZXRjaEVudGl0eUxpc3QgPSAoZG9tYWluLCBzdGFydCwgcm93cywgbmV4dCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJ9LFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0/cm93cz0ke3Jvd3N9JnN0YXJ0PSR7c3RhcnR9YFxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0Y29uc3QgZGF0YSA9IEpTT04ucGFyc2UocmVzcC5ib2R5KTtcblx0XHRuZXh0KGRhdGEpO1xuXHR9KTtcblxuY29uc3QgY3J1ZCA9IHtcblx0c2F2ZU5ld0VudGl0eTogc2F2ZU5ld0VudGl0eSxcblx0dXBkYXRlRW50aXR5OiB1cGRhdGVFbnRpdHksXG5cdGRlbGV0ZUVudGl0eTogZGVsZXRlRW50aXR5LFxuXHRmZXRjaEVudGl0eTogZmV0Y2hFbnRpdHksXG5cdGZldGNoRW50aXR5TGlzdDogZmV0Y2hFbnRpdHlMaXN0XG59O1xuXG5leHBvcnQge3NhdmVOZXdFbnRpdHksIHVwZGF0ZUVudGl0eSwgZGVsZXRlRW50aXR5LCBmZXRjaEVudGl0eSwgZmV0Y2hFbnRpdHlMaXN0LCBjcnVkfTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4uL3V0aWwvY2xvbmUtZGVlcFwiO1xuaW1wb3J0IHsgY3J1ZCB9IGZyb20gXCIuL2NydWRcIjtcbmltcG9ydCBzYXZlUmVsYXRpb25zIGZyb20gXCIuL3NhdmUtcmVsYXRpb25zXCI7XG5pbXBvcnQgYXV0b2NvbXBsZXRlIGZyb20gXCIuL2F1dG9jb21wbGV0ZVwiO1xuXG4vLyBTa2VsZXRvbiBiYXNlIGRhdGEgcGVyIGZpZWxkIGRlZmluaXRpb25cbmNvbnN0IGluaXRpYWxEYXRhID0ge1xuXHRuYW1lczogW10sXG5cdG11bHRpc2VsZWN0OiBbXSxcblx0bGlua3M6IFtdLFxuXHRrZXl3b3JkOiBbXSxcblx0XCJsaXN0LW9mLXN0cmluZ3NcIjogW10sXG5cdGFsdG5hbWVzOiBbXSxcblx0dGV4dDogXCJcIixcblx0c3RyaW5nOiBcIlwiLFxuXHRzZWxlY3Q6IFwiXCIsXG5cdGRhdGFibGU6IFwiXCJcbn07XG5cbi8vIFJldHVybiB0aGUgaW5pdGlhbCBkYXRhIGZvciB0aGUgdHlwZSBpbiB0aGUgZmllbGQgZGVmaW5pdGlvblxuY29uc3QgaW5pdGlhbERhdGFGb3JUeXBlID0gKGZpZWxkRGVmKSA9PlxuXHRmaWVsZERlZi5kZWZhdWx0VmFsdWUgfHwgKGZpZWxkRGVmLnR5cGUgPT09IFwicmVsYXRpb25cIiB8fCBmaWVsZERlZi50eXBlID09PSBcImtleXdvcmRcIiA/IHt9IDogaW5pdGlhbERhdGFbZmllbGREZWYudHlwZV0pO1xuXG4vLyBSZXR1cm4gdGhlIGluaXRpYWwgbmFtZS1rZXkgZm9yIGEgY2VydGFpbiBmaWVsZCB0eXBlXG5jb25zdCBuYW1lRm9yVHlwZSA9IChmaWVsZERlZikgPT5cblx0ZmllbGREZWYudHlwZSA9PT0gXCJyZWxhdGlvblwiIHx8IGZpZWxkRGVmLnR5cGUgPT09IFwia2V5d29yZFwiID8gXCJAcmVsYXRpb25zXCIgOiBmaWVsZERlZi5uYW1lO1xuXG5cbi8vIENyZWF0ZSBhIG5ldyBlbXB0eSBlbnRpdHkgYmFzZWQgb24gdGhlIGZpZWxkRGVmaW5pdGlvbnNcbmNvbnN0IG1ha2VTa2VsZXRvbiA9IGZ1bmN0aW9uICh2cmUsIGRvbWFpbikge1xuXHRpZiAodnJlICYmIHZyZS5jb2xsZWN0aW9ucyAmJiB2cmUuY29sbGVjdGlvbnNbZG9tYWluXSAmJiB2cmUuY29sbGVjdGlvbnNbZG9tYWluXS5wcm9wZXJ0aWVzKSB7XG5cdFx0cmV0dXJuIHZyZS5jb2xsZWN0aW9uc1tkb21haW5dLnByb3BlcnRpZXNcblx0XHRcdC5tYXAoKGZpZWxkRGVmKSA9PiBbbmFtZUZvclR5cGUoZmllbGREZWYpLCBpbml0aWFsRGF0YUZvclR5cGUoZmllbGREZWYpXSlcblx0XHRcdC5jb25jYXQoW1tcIkB0eXBlXCIsIGRvbWFpbi5yZXBsYWNlKC9zJC8sIFwiXCIpXV0pXG5cdFx0XHQucmVkdWNlKChvYmosIGN1cikgPT4ge1xuXHRcdFx0XHRvYmpbY3VyWzBdXSA9IGN1clsxXTtcblx0XHRcdFx0cmV0dXJuIG9iajtcblx0XHRcdH0sIHt9KTtcblx0fVxufTtcblxuY29uc3QgZmV0Y2hFbnRpdHlMaXN0ID0gKGRvbWFpbikgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogMH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChkb21haW4sIDAsIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cywgKGRhdGEpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIiwgZGF0YTogZGF0YX0pKTtcbn07XG5cbmNvbnN0IHBhZ2luYXRlTGVmdCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgbmV3U3RhcnQgPSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnN0YXJ0IC0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogbmV3U3RhcnQgPCAwID8gMCA6IG5ld1N0YXJ0fSk7XG5cdGNydWQuZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgbmV3U3RhcnQgPCAwID8gMCA6IG5ld1N0YXJ0LCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGF9KSk7XG59O1xuXG5jb25zdCBwYWdpbmF0ZVJpZ2h0ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCBuZXdTdGFydCA9IGdldFN0YXRlKCkucXVpY2tTZWFyY2guc3RhcnQgKyBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3M7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiBuZXdTdGFydH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIG5ld1N0YXJ0LCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGF9KSk7XG59O1xuXG5jb25zdCBzZW5kUXVpY2tTZWFyY2ggPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IHsgcXVpY2tTZWFyY2gsIGVudGl0eSwgdnJlIH0gPSBnZXRTdGF0ZSgpO1xuXHRpZiAocXVpY2tTZWFyY2gucXVlcnkubGVuZ3RoKSB7XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IDB9KTtcblx0XHRjb25zdCBjYWxsYmFjayA9IChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGEubWFwKChkKSA9PiAoXG5cdFx0XHR7XG5cdFx0XHRcdF9pZDogZC5rZXkucmVwbGFjZSgvLipcXC8vLCBcIlwiKSxcblx0XHRcdFx0XCJAZGlzcGxheU5hbWVcIjogZC52YWx1ZVxuXHRcdFx0fVxuXHRcdCkpfSk7XG5cdFx0YXV0b2NvbXBsZXRlKGBkb21haW4vJHtlbnRpdHkuZG9tYWlufS9hdXRvY29tcGxldGVgLCBxdWlja1NlYXJjaC5xdWVyeSwgY2FsbGJhY2spO1xuXHR9IGVsc2Uge1xuXHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChlbnRpdHkuZG9tYWluKSk7XG5cdH1cbn07XG5cbmNvbnN0IHNlbGVjdERvbWFpbiA9IChkb21haW4pID0+IChkaXNwYXRjaCkgPT4ge1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRE9NQUlOXCIsIGRvbWFpbn0pO1xuXHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZG9tYWluKSk7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9RVUlDS1NFQVJDSF9RVUVSWVwiLCB2YWx1ZTogXCJcIn0pO1xufTtcblxuLy8gMSkgRmV0Y2ggZW50aXR5XG4vLyAyKSBEaXNwYXRjaCBSRUNFSVZFX0VOVElUWSBmb3IgcmVuZGVyXG5jb25zdCBzZWxlY3RFbnRpdHkgPSAoZG9tYWluLCBlbnRpdHlJZCwgZXJyb3JNZXNzYWdlID0gbnVsbCwgc3VjY2Vzc01lc3NhZ2UgPSBudWxsLCBuZXh0ID0gKCkgPT4geyB9KSA9PlxuXHQoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdFx0Y29uc3QgeyBlbnRpdHk6IHsgZG9tYWluOiBjdXJyZW50RG9tYWluIH0gfSA9IGdldFN0YXRlKCk7XG5cdFx0aWYgKGN1cnJlbnREb21haW4gIT09IGRvbWFpbikge1xuXHRcdFx0ZGlzcGF0Y2goc2VsZWN0RG9tYWluKGRvbWFpbikpO1xuXHRcdH1cblx0XHRjcnVkLmZldGNoRW50aXR5KGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7ZW50aXR5SWR9YCwgKGRhdGEpID0+IHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZXCIsIGRvbWFpbjogZG9tYWluLCBkYXRhOiBkYXRhLCBlcnJvck1lc3NhZ2U6IGVycm9yTWVzc2FnZX0pO1xuXHRcdFx0aWYgKHN1Y2Nlc3NNZXNzYWdlICE9PSBudWxsKSB7XG5cdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNVQ0NFU1NfTUVTU0FHRVwiLCBtZXNzYWdlOiBzdWNjZXNzTWVzc2FnZX0pO1xuXHRcdFx0fVxuXHRcdH0sICgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0ZBSUxVUkVcIiwgZXJyb3JNZXNzYWdlOiBgRmFpbGVkIHRvIGZldGNoICR7ZG9tYWlufSB3aXRoIElEICR7ZW50aXR5SWR9YH0pKTtcblx0XHRuZXh0KCk7XG5cdH07XG5cblxuLy8gMSkgRGlzcGF0Y2ggUkVDRUlWRV9FTlRJVFkgd2l0aCBlbXB0eSBlbnRpdHkgc2tlbGV0b24gZm9yIHJlbmRlclxuY29uc3QgbWFrZU5ld0VudGl0eSA9IChkb21haW4sIGVycm9yTWVzc2FnZSA9IG51bGwpID0+XG5cdChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IGRpc3BhdGNoKHtcblx0XHR0eXBlOiBcIlJFQ0VJVkVfRU5USVRZXCIsXG5cdFx0ZG9tYWluOiBkb21haW4sXG5cdFx0ZGF0YTogbWFrZVNrZWxldG9uKGdldFN0YXRlKCkudnJlLCBkb21haW4pIHx8IHt9LFxuXHRcdGVycm9yTWVzc2FnZTogZXJyb3JNZXNzYWdlXG5cdH0pO1xuXG5jb25zdCBkZWxldGVFbnRpdHkgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNydWQuZGVsZXRlRW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsXG5cdFx0KCkgPT4ge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU1VDQ0VTU19NRVNTQUdFXCIsIG1lc3NhZ2U6IGBTdWNlc3NmdWxseSBkZWxldGVkICR7Z2V0U3RhdGUoKS5lbnRpdHkuZG9tYWlufSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YH0pO1xuXHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKTtcblx0XHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKTtcblx0XHR9LFxuXHRcdCgpID0+IGRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBgRmFpbGVkIHRvIGRlbGV0ZSAke2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbn0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWApKSk7XG59O1xuXG4vLyAxKSBTYXZlIGFuIGVudGl0eVxuLy8gMikgU2F2ZSB0aGUgcmVsYXRpb25zIGZvciB0aGlzIGVudGl0eVxuLy8gMykgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuY29uc3Qgc2F2ZUVudGl0eSA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgY29sbGVjdGlvbkxhYmVsID0gZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5jb2xsZWN0aW9uTGFiZWwucmVwbGFjZSgvcyQvLCBcIlwiKTtcblxuXHQvLyBNYWtlIGEgZGVlcCBjb3B5IG9mIHRoZSBkYXRhIHRvIGJlIHNhdmVkIGluIG9yZGVyIHRvIGxlYXZlIGFwcGxpY2F0aW9uIHN0YXRlIHVuYWx0ZXJlZFxuXHRsZXQgc2F2ZURhdGEgPSBjbG9uZShnZXRTdGF0ZSgpLmVudGl0eS5kYXRhKTtcblx0Ly8gTWFrZSBhIGRlZXAgY29weSBvZiB0aGUgcmVsYXRpb24gZGF0YSBpbiBvcmRlciB0byBsZWF2ZSBhcHBsaWNhdGlvbiBzdGF0ZSB1bmFsdGVyZWRcblx0bGV0IHJlbGF0aW9uRGF0YSA9IGNsb25lKHNhdmVEYXRhW1wiQHJlbGF0aW9uc1wiXSkgfHwge307XG5cdC8vIERlbGV0ZSB0aGUgcmVsYXRpb24gZGF0YSBmcm9tIHRoZSBzYXZlRGF0YSBhcyBpdCBpcyBub3QgZXhwZWN0ZWQgYnkgdGhlIHNlcnZlclxuXHRkZWxldGUgc2F2ZURhdGFbXCJAcmVsYXRpb25zXCJdO1xuXG5cdGlmIChnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdC8vIDEpIFVwZGF0ZSB0aGUgZW50aXR5IHdpdGggc2F2ZURhdGFcblx0XHRjcnVkLnVwZGF0ZUVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIHNhdmVEYXRhLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoZXJyLCByZXNwKSA9PlxuXHRcdFx0Ly8gMikgU2F2ZSByZWxhdGlvbnMgdXNpbmcgc2VydmVyIHJlc3BvbnNlIGZvciBjdXJyZW50IHJlbGF0aW9ucyB0byBkaWZmIGFnYWluc3QgcmVsYXRpb25EYXRhXG5cdFx0XHRkaXNwYXRjaCgocmVkaXNwYXRjaCkgPT4gc2F2ZVJlbGF0aW9ucyhKU09OLnBhcnNlKHJlc3AuYm9keSksIHJlbGF0aW9uRGF0YSwgZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5wcm9wZXJ0aWVzLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoKSA9PlxuXHRcdFx0XHQvLyAzKSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5cdFx0XHRcdHJlZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIG51bGwsIGBTdWNjZXNmdWxseSBzYXZlZCAke2NvbGxlY3Rpb25MYWJlbH0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWAsICgpID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKSkpKSksICgpID0+XG5cdFx0XHRcdFx0Ly8gMmEpIEhhbmRsZSBlcnJvciBieSByZWZldGNoaW5nIGFuZCBwYXNzaW5nIGFsb25nIGFuIGVycm9yIG1lc3NhZ2Vcblx0XHRcdFx0XHRkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgYEZhaWxlZCB0byBzYXZlICR7Y29sbGVjdGlvbkxhYmVsfSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YCkpKTtcblxuXHR9IGVsc2Uge1xuXHRcdC8vIDEpIENyZWF0ZSBuZXcgZW50aXR5IHdpdGggc2F2ZURhdGFcblx0XHRjcnVkLnNhdmVOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBzYXZlRGF0YSwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKGVyciwgcmVzcCkgPT5cblx0XHRcdC8vIDIpIEZldGNoIGVudGl0eSB2aWEgbG9jYXRpb24gaGVhZGVyXG5cdFx0XHRkaXNwYXRjaCgocmVkaXNwYXRjaCkgPT4gY3J1ZC5mZXRjaEVudGl0eShyZXNwLmhlYWRlcnMubG9jYXRpb24sIChkYXRhKSA9PlxuXHRcdFx0XHQvLyAzKSBTYXZlIHJlbGF0aW9ucyB1c2luZyBzZXJ2ZXIgcmVzcG9uc2UgZm9yIGN1cnJlbnQgcmVsYXRpb25zIHRvIGRpZmYgYWdhaW5zdCByZWxhdGlvbkRhdGFcblx0XHRcdFx0c2F2ZVJlbGF0aW9ucyhkYXRhLCByZWxhdGlvbkRhdGEsIGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0ucHJvcGVydGllcywgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKCkgPT5cblx0XHRcdFx0XHQvLyA0KSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5cdFx0XHRcdFx0cmVkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBkYXRhLl9pZCwgbnVsbCwgYFN1Y2Nlc2Z1bGx5IHNhdmVkICR7Y29sbGVjdGlvbkxhYmVsfWAsICgpID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKSkpKSkpLCAoKSA9PlxuXHRcdFx0XHRcdFx0Ly8gMmEpIEhhbmRsZSBlcnJvciBieSByZWZldGNoaW5nIGFuZCBwYXNzaW5nIGFsb25nIGFuIGVycm9yIG1lc3NhZ2Vcblx0XHRcdFx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBgRmFpbGVkIHRvIHNhdmUgbmV3ICR7Y29sbGVjdGlvbkxhYmVsfWApKSk7XG5cdH1cbn07XG5cblxuZXhwb3J0IHsgc2F2ZUVudGl0eSwgc2VsZWN0RW50aXR5LCBtYWtlTmV3RW50aXR5LCBkZWxldGVFbnRpdHksIGZldGNoRW50aXR5TGlzdCwgcGFnaW5hdGVSaWdodCwgcGFnaW5hdGVMZWZ0LCBzZW5kUXVpY2tTZWFyY2gsIHNlbGVjdERvbWFpbiB9OyIsImltcG9ydCB7IHNhdmVFbnRpdHksIHNlbGVjdEVudGl0eSwgbWFrZU5ld0VudGl0eSwgZGVsZXRlRW50aXR5LCBzZWxlY3REb21haW4sIHBhZ2luYXRlTGVmdCwgcGFnaW5hdGVSaWdodCwgc2VuZFF1aWNrU2VhcmNoIH0gZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQgeyBzZXRWcmUgfSBmcm9tIFwiLi92cmVcIjtcblxuZXhwb3J0IGRlZmF1bHQgKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSA9PiAoe1xuXHRvbk5ldzogKGRvbWFpbikgPT4gZGlzcGF0Y2gobWFrZU5ld0VudGl0eShkb21haW4pKSxcblx0b25TZWxlY3Q6IChyZWNvcmQpID0+IGRpc3BhdGNoKHNlbGVjdEVudGl0eShyZWNvcmQuZG9tYWluLCByZWNvcmQuaWQpKSxcblx0b25TYXZlOiAoKSA9PiBkaXNwYXRjaChzYXZlRW50aXR5KCkpLFxuXHRvbkRlbGV0ZTogKCkgPT4gZGlzcGF0Y2goZGVsZXRlRW50aXR5KCkpLFxuXHRvbkNoYW5nZTogKGZpZWxkUGF0aCwgdmFsdWUpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBmaWVsZFBhdGgsIHZhbHVlOiB2YWx1ZX0pLFxuXHRvbkxvZ2luQ2hhbmdlOiAocmVzcG9uc2UpID0+IGRpc3BhdGNoKHNldFVzZXIocmVzcG9uc2UpKSxcblx0b25TZWxlY3RWcmU6ICh2cmVJZCkgPT4gZGlzcGF0Y2goc2V0VnJlKHZyZUlkKSksXG5cdG9uRGlzbWlzc01lc3NhZ2U6IChtZXNzYWdlSW5kZXgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIkRJU01JU1NfTUVTU0FHRVwiLCBtZXNzYWdlSW5kZXg6IG1lc3NhZ2VJbmRleH0pLFxuXHRvblNlbGVjdERvbWFpbjogKGRvbWFpbikgPT4ge1xuXHRcdGRpc3BhdGNoKHNlbGVjdERvbWFpbihkb21haW4pKTtcblx0fSxcblx0b25QYWdpbmF0ZUxlZnQ6ICgpID0+IGRpc3BhdGNoKHBhZ2luYXRlTGVmdCgpKSxcblx0b25QYWdpbmF0ZVJpZ2h0OiAoKSA9PiBkaXNwYXRjaChwYWdpbmF0ZVJpZ2h0KCkpLFxuXHRvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2U6ICh2YWx1ZSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCIsIHZhbHVlOiB2YWx1ZX0pLFxuXHRvblF1aWNrU2VhcmNoOiAoKSA9PiBkaXNwYXRjaChzZW5kUXVpY2tTZWFyY2goKSlcbn0pOyIsImltcG9ydCB7IHNhdmVOZXdFbnRpdHksIHVwZGF0ZUVudGl0eSB9IGZyb20gXCIuL2NydWRcIjtcblxuY29uc3Qgc2F2ZVJlbGF0aW9uc1YyMSA9IChkYXRhLCByZWxhdGlvbkRhdGEsIGZpZWxkRGVmcywgdG9rZW4sIHZyZUlkLCBuZXh0KSA9PiB7XG5cdC8vIFJldHVybnMgdGhlIGRvbWFpbiBiYXNlZCBvbiB0aGUgZmllbGREZWZpbml0aW9ucyBhbmQgdGhlIHJlbGF0aW9uIGtleSAoaS5lLiBcImhhc0JpcnRoUGxhY2VcIilcblx0Y29uc3QgbWFrZVJlbGF0aW9uQXJncyA9IChyZWxhdGlvbiwga2V5LCBhY2NlcHRlZCA9IHRydWUsIGlkID0gbnVsbCwgcmV2ID0gbnVsbCkgPT4ge1xuXHRcdGNvbnN0IGZpZWxkRGVmID0gZmllbGREZWZzLmZpbmQoKGRlZikgPT4gZGVmLm5hbWUgPT09IGtleSk7XG5cblxuXHRcdGNvbnN0IHNvdXJjZVR5cGUgPSBkYXRhW1wiQHR5cGVcIl0ucmVwbGFjZSgvcyQvLCBcIlwiKS5yZXBsYWNlKC9ed3cvLCBcIlwiKTtcblx0XHRjb25zdCB0YXJnZXRUeXBlID0gZmllbGREZWYucmVsYXRpb24udGFyZ2V0Q29sbGVjdGlvbi5yZXBsYWNlKC9zJC8sIFwiXCIpLnJlcGxhY2UoL153dy8sIFwiXCIpO1xuXG5cdFx0Y29uc3QgcmVsYXRpb25TYXZlRGF0YSA9IHtcblx0XHRcdFwiQHR5cGVcIjogZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25Db2xsZWN0aW9uLnJlcGxhY2UoL3MkLywgXCJcIiksIC8vIGNoZWNrXG5cdFx0XHRcIl5zb3VyY2VJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHJlbGF0aW9uLmlkIDogZGF0YS5faWQsIC8vIGNoZWNrXG5cdFx0XHRcIl5zb3VyY2VUeXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gdGFyZ2V0VHlwZSA6IHNvdXJjZVR5cGUsIC8vIGNoZWNrXG5cdFx0XHRcIl50YXJnZXRJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IGRhdGEuX2lkIDogcmVsYXRpb24uaWQsIC8vIGNoZWNrXG5cdFx0XHRcIl50YXJnZXRUeXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gc291cmNlVHlwZSA6IHRhcmdldFR5cGUsXG5cdFx0XHRcIl50eXBlSWRcIjogZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25UeXBlSWQsIC8vIGNoZWNrXG5cdFx0XHRhY2NlcHRlZDogYWNjZXB0ZWRcblx0XHR9O1xuXG5cdFx0aWYoaWQpIHsgcmVsYXRpb25TYXZlRGF0YS5faWQgPSBpZDsgfVxuXHRcdGlmKHJldikgeyByZWxhdGlvblNhdmVEYXRhW1wiXnJldlwiXSA9IHJldjsgfVxuXHRcdHJldHVybiBbXG5cdFx0XHRmaWVsZERlZi5yZWxhdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24sIC8vIGRvbWFpblxuXHRcdFx0cmVsYXRpb25TYXZlRGF0YVxuXHRcdF07XG5cdH07XG5cblx0Ly8gQ29uc3RydWN0cyBhbiBhcnJheSBvZiBhcmd1bWVudHMgZm9yIHNhdmluZyBuZXcgcmVsYXRpb25zOlxuXHQvLyBbXG5cdC8vICAgW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV0sXG5cdC8vICAgW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV0sXG5cdC8vIF1cblx0Y29uc3QgbmV3UmVsYXRpb25zID0gT2JqZWN0LmtleXMocmVsYXRpb25EYXRhKS5tYXAoKGtleSkgPT5cblx0XHRcdHJlbGF0aW9uRGF0YVtrZXldXG5cdFx0XHQvLyBGaWx0ZXJzIG91dCBhbGwgcmVsYXRpb25zIHdoaWNoIGFyZSBub3QgYWxyZWFkeSBpbiBkYXRhW1wiQHJlbGF0aW9uc1wiXVxuXHRcdFx0XHQuZmlsdGVyKChyZWxhdGlvbikgPT4gKGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV0gfHwgW10pLm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uaWQpLmluZGV4T2YocmVsYXRpb24uaWQpIDwgMClcblx0XHRcdFx0Ly8gTWFrZSBhcmd1bWVudCBhcnJheSBmb3IgbmV3IHJlbGF0aW9uczogW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV1cblx0XHRcdFx0Lm1hcCgocmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3MocmVsYXRpb24sIGtleSkpXG5cdFx0Ly8gRmxhdHRlbiBuZXN0ZWQgYXJyYXlzXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cblx0Ly8gUmVhY3RpdmF0ZSBwcmV2aW91c2x5IGFkZGVkIHJlbGF0aW9ucyB1c2luZyBQVVQgd2hpY2ggd2VyZSAnZGVsZXRlZCcgYWZ0ZXIgdXNpbmcgUFVUXG5cdGNvbnN0IHJlQWRkUmVsYXRpb25zID0gT2JqZWN0LmtleXMocmVsYXRpb25EYXRhKS5tYXAoKGtleSkgPT5cblx0XHQoZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XSB8fCBbXSlcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmFjY2VwdGVkID09PSBmYWxzZSlcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gKHJlbGF0aW9uRGF0YVtrZXldIHx8IFtdKS5maWx0ZXIoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5hY2NlcHRlZCkubWFwKChyZWxhdGlvbikgPT4gcmVsYXRpb24uaWQpLmluZGV4T2Yob3JpZ1JlbGF0aW9uLmlkKSA+IC0xKVxuXHRcdFx0Lm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKG9yaWdSZWxhdGlvbiwga2V5LCB0cnVlLCBvcmlnUmVsYXRpb24ucmVsYXRpb25JZCwgb3JpZ1JlbGF0aW9uLnJldikpXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cdC8vIERlYWN0aXZhdGUgcHJldmlvdXNseSBhZGRlZCByZWxhdGlvbnMgdXNpbmcgUFVUXG5cdGNvbnN0IGRlbGV0ZVJlbGF0aW9ucyA9IE9iamVjdC5rZXlzKGRhdGFbXCJAcmVsYXRpb25zXCJdKS5tYXAoKGtleSkgPT5cblx0XHRkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5hY2NlcHRlZClcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gKHJlbGF0aW9uRGF0YVtrZXldIHx8IFtdKS5tYXAoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5pZCkuaW5kZXhPZihvcmlnUmVsYXRpb24uaWQpIDwgMClcblx0XHRcdC5tYXAoKG9yaWdSZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhvcmlnUmVsYXRpb24sIGtleSwgZmFsc2UsIG9yaWdSZWxhdGlvbi5yZWxhdGlvbklkLCBvcmlnUmVsYXRpb24ucmV2KSlcblx0KS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSk7XG5cblx0Ly8gQ29tYmluZXMgc2F2ZU5ld0VudGl0eSBhbmQgZGVsZXRlRW50aXR5IGluc3RydWN0aW9ucyBpbnRvIHByb21pc2VzXG5cdGNvbnN0IHByb21pc2VzID0gbmV3UmVsYXRpb25zXG5cdC8vIE1hcCBuZXdSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2Ygc2F2ZU5ld0VudGl0eVxuXHRcdC5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHNhdmVOZXdFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpICkpXG5cdFx0Ly8gTWFwIHJlYWRkUmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHVwZGF0ZUVudGl0eVxuXHRcdC5jb25jYXQocmVBZGRSZWxhdGlvbnMubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiB1cGRhdGVFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpKSkpXG5cdFx0Ly8gTWFwIGRlbGV0ZVJlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiB1cGRhdGVFbnRpdHlcblx0XHQuY29uY2F0KGRlbGV0ZVJlbGF0aW9ucy5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHVwZGF0ZUVudGl0eSguLi5hcmdzLCB0b2tlbiwgdnJlSWQsIHJlc29sdmUsIHJlamVjdCkpKSk7XG5cblx0Ly8gSW52b2tlIGFsbCBDUlVEIG9wZXJhdGlvbnMgZm9yIHRoZSByZWxhdGlvbnNcblx0UHJvbWlzZS5hbGwocHJvbWlzZXMpLnRoZW4obmV4dCwgbmV4dCk7XG59O1xuXG5leHBvcnQgZGVmYXVsdCBzYXZlUmVsYXRpb25zVjIxOyIsImltcG9ydCB4aHIgZnJvbSBcInhoclwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuLi9zdG9yZVwiO1xuXG5leHBvcnQgZGVmYXVsdCB7XG5cdHBlcmZvcm1YaHI6IGZ1bmN0aW9uIChvcHRpb25zLCBhY2NlcHQsIHJlamVjdCA9ICgpID0+IHsgY29uc29sZS53YXJuKFwiVW5kZWZpbmVkIHJlamVjdCBjYWxsYmFjayEgXCIpOyB9LCBvcGVyYXRpb24gPSBcIlNlcnZlciByZXF1ZXN0XCIpIHtcblx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJSRVFVRVNUX01FU1NBR0VcIiwgbWVzc2FnZTogYCR7b3BlcmF0aW9ufTogJHtvcHRpb25zLm1ldGhvZCB8fCBcIkdFVFwifSAke29wdGlvbnMudXJsfWB9KTtcblx0XHR4aHIob3B0aW9ucywgKGVyciwgcmVzcCwgYm9keSkgPT4ge1xuXHRcdFx0aWYocmVzcC5zdGF0dXNDb2RlID49IDQwMCkge1xuXHRcdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJFUlJPUl9NRVNTQUdFXCIsIG1lc3NhZ2U6IGAke29wZXJhdGlvbn0gZmFpbGVkIHdpdGggY2F1c2U6ICR7cmVzcC5ib2R5fWB9KTtcblx0XHRcdFx0cmVqZWN0KGVyciwgcmVzcCwgYm9keSk7XG5cdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRhY2NlcHQoZXJyLCByZXNwLCBib2R5KTtcblx0XHRcdH1cblx0XHR9KTtcblx0fSxcblxuXHRmYXN0WGhyOiBmdW5jdGlvbihvcHRpb25zLCBhY2NlcHQpIHtcblx0XHR4aHIob3B0aW9ucywgYWNjZXB0KTtcblx0fSxcblxuXHRtYWtlSGVhZGVyczogZnVuY3Rpb24odG9rZW4sIHZyZUlkKSB7XG5cdFx0cmV0dXJuIHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiLFxuXHRcdFx0XCJDb250ZW50LXR5cGVcIjogXCJhcHBsaWNhdGlvbi9qc29uXCIsXG5cdFx0XHRcIkF1dGhvcml6YXRpb25cIjogdG9rZW4sXG5cdFx0XHRcIlZSRV9JRFwiOiB2cmVJZFxuXHRcdH07XG5cdH1cbn07XG4iLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vaW5kZXhcIjtcbmltcG9ydCB7bWFrZU5ld0VudGl0eX0gZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQge2ZldGNoRW50aXR5TGlzdH0gZnJvbSBcIi4vZW50aXR5XCI7XG5cbmNvbnN0IGxpc3RWcmVzID0gKCkgPT4gKGRpc3BhdGNoKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiXG5cdFx0fSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9zeXN0ZW0vdnJlc2Bcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIkxJU1RfVlJFU1wiLCBsaXN0OiBKU09OLnBhcnNlKHJlc3AuYm9keSl9KTtcblx0fSwgbnVsbCwgXCJMaXN0IFZSRXNcIik7XG5cbmNvbnN0IHNldFZyZSA9ICh2cmVJZCwgbmV4dCA9ICgpID0+IHsgfSkgPT4gKGRpc3BhdGNoKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiXG5cdFx0fSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9tZXRhZGF0YS8ke3ZyZUlkfT93aXRoQ29sbGVjdGlvbkluZm89dHJ1ZWBcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGlmIChyZXNwLnN0YXR1c0NvZGUgPT09IDIwMCkge1xuXHRcdFx0dmFyIGJvZHkgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfVlJFXCIsIHZyZUlkOiB2cmVJZCwgY29sbGVjdGlvbnM6IGJvZHl9KTtcblxuXHRcdFx0bGV0IGRlZmF1bHREb21haW4gPSBPYmplY3Qua2V5cyhib2R5KVxuXHRcdFx0XHQubWFwKGNvbGxlY3Rpb25OYW1lID0+IGJvZHlbY29sbGVjdGlvbk5hbWVdKVxuXHRcdFx0XHQuZmlsdGVyKGNvbGxlY3Rpb24gPT4gIWNvbGxlY3Rpb24udW5rbm93biAmJiAhY29sbGVjdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24pWzBdXG5cdFx0XHRcdC5jb2xsZWN0aW9uTmFtZTtcblxuXHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShkZWZhdWx0RG9tYWluKSlcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9ET01BSU5cIiwgZGVmYXVsdERvbWFpbn0pO1xuXHRcdFx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGRlZmF1bHREb21haW4pKTtcblx0XHRcdG5leHQoKTtcblx0XHR9XG5cdH0sICgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9WUkVcIiwgdnJlSWQ6IHZyZUlkLCBjb2xsZWN0aW9uczoge319KSwgYEZldGNoIFZSRSBkZXNjcmlwdGlvbiBmb3IgJHt2cmVJZH1gKTtcblxuXG5leHBvcnQge2xpc3RWcmVzLCBzZXRWcmV9O1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNsYXNzbmFtZXMgZnJvbSBcImNsYXNzbmFtZXNcIjtcbmltcG9ydCB7dXJsc30gZnJvbSBcIi4uLy4uL3VybHNcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5cbmNsYXNzIENvbGxlY3Rpb25UYWJzIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRvbkRvbWFpblNlbGVjdChkb21haW4pIHtcblx0XHR0aGlzLnByb3BzLm9uTmV3KGRvbWFpbik7XG5cdFx0dGhpcy5wcm9wcy5vblNlbGVjdERvbWFpbihkb21haW4pO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgY29sbGVjdGlvbnMsIGFjdGl2ZURvbWFpbiB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBkb21haW5zID0gT2JqZWN0LmtleXMoY29sbGVjdGlvbnMgfHwge30pO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwibmF2IG5hdi10YWJzXCI+XG4gICAgICAgICAge2RvbWFpbnNcbiAgICAgICAgICAgIC5maWx0ZXIoZCA9PiAhKGNvbGxlY3Rpb25zW2RdLnVua25vd24gfHwgY29sbGVjdGlvbnNbZF0ucmVsYXRpb25Db2xsZWN0aW9uKSlcbiAgICAgICAgICAgIC5tYXAoKGRvbWFpbikgPT4gKFxuICAgICAgICAgICAgICA8bGkgY2xhc3NOYW1lPXtjbGFzc25hbWVzKHthY3RpdmU6IGRvbWFpbiA9PT0gYWN0aXZlRG9tYWlufSl9IGtleT17ZG9tYWlufT5cbiAgICAgICAgICAgICAgICA8TGluayB0bz17dXJscy5uZXdFbnRpdHkoZG9tYWluKX0+XG4gICAgICAgICAgICAgICAgICB7Y29sbGVjdGlvbnNbZG9tYWluXS5jb2xsZWN0aW9uTGFiZWx9XG4gICAgICAgICAgICAgICAgPC9MaW5rPlxuICAgICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkNvbGxlY3Rpb25UYWJzLnByb3BUeXBlcyA9IHtcblx0b25OZXc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNlbGVjdERvbWFpbjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdGNvbGxlY3Rpb25zOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRhY3RpdmVEb21haW46IFJlYWN0LlByb3BUeXBlcy5zdHJpbmdcbn07XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25UYWJzO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFBhZ2UgZnJvbSBcIi4uL3BhZ2UuanN4XCI7XG5cbmltcG9ydCBQYWdpbmF0ZSBmcm9tIFwiLi9lbnRpdHktaW5kZXgvcGFnaW5hdGVcIjtcbmltcG9ydCBRdWlja1NlYXJjaCBmcm9tIFwiLi9lbnRpdHktaW5kZXgvcXVpY2tzZWFyY2hcIjtcbmltcG9ydCBFbnRpdHlMaXN0IGZyb20gXCIuL2VudGl0eS1pbmRleC9saXN0XCI7XG5cbmltcG9ydCBTYXZlRm9vdGVyIGZyb20gXCIuL2VudGl0eS1mb3JtL3NhdmUtZm9vdGVyXCI7XG5pbXBvcnQgRW50aXR5Rm9ybSBmcm9tIFwiLi9lbnRpdHktZm9ybS9mb3JtXCI7XG5cbmltcG9ydCBDb2xsZWN0aW9uVGFicyBmcm9tIFwiLi9jb2xsZWN0aW9uLXRhYnNcIjtcbmltcG9ydCBNZXNzYWdlcyBmcm9tIFwiLi9tZXNzYWdlcy9saXN0XCI7XG5cbmNsYXNzIEVkaXRHdWkgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0Y29uc3QgeyBvblNlbGVjdCwgb25OZXcsIG9uU2VsZWN0RG9tYWluIH0gPSB0aGlzLnByb3BzO1xuXG5cdFx0Ly8gVHJpZ2dlcnMgZmV0Y2ggZGF0YSBmcm9tIHNlcnZlciBiYXNlZCBvbiBpZCBmcm9tIHJvdXRlLlxuXHRcdGlmICh0aGlzLnByb3BzLnBhcmFtcy5pZCAhPT0gbmV4dFByb3BzLnBhcmFtcy5pZCkge1xuXHRcdFx0b25TZWxlY3Qoe2RvbWFpbjogbmV4dFByb3BzLnBhcmFtcy5jb2xsZWN0aW9uLCBpZDogbmV4dFByb3BzLnBhcmFtcy5pZH0pO1xuXHRcdH0gZWxzZSBpZiAodGhpcy5wcm9wcy5wYXJhbXMuY29sbGVjdGlvbiAhPT0gbmV4dFByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKSB7XG5cdFx0XHRvbk5ldyhuZXh0UHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pO1xuXHRcdFx0b25TZWxlY3REb21haW4obmV4dFByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKTtcblx0XHR9XG5cdH1cblxuXHRjb21wb25lbnREaWRNb3VudCgpIHtcblxuXHRcdGlmICh0aGlzLnByb3BzLnBhcmFtcy5pZCkge1xuXHRcdFx0dGhpcy5wcm9wcy5vblNlbGVjdCh7ZG9tYWluOiB0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uLCBpZDogdGhpcy5wcm9wcy5wYXJhbXMuaWR9KTtcblx0XHR9IGVsc2UgaWYgKHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pIHtcblx0XHRcdHRoaXMucHJvcHMub25OZXcodGhpcy5wcm9wcy5wYXJhbXMuY29sbGVjdGlvbik7XG5cdFx0XHR0aGlzLnByb3BzLm9uU2VsZWN0RG9tYWluKHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pO1xuXHRcdH1cblxuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgb25TZWxlY3QsIG9uTmV3LCBvblNhdmUsIG9uRGVsZXRlLCBvblNlbGVjdERvbWFpbiwgb25EaXNtaXNzTWVzc2FnZSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UsIG9uUXVpY2tTZWFyY2gsIG9uUGFnaW5hdGVMZWZ0LCBvblBhZ2luYXRlUmlnaHQgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBnZXRBdXRvY29tcGxldGVWYWx1ZXMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBxdWlja1NlYXJjaCwgZW50aXR5LCB2cmUsIG1lc3NhZ2VzIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGN1cnJlbnRNb2RlID0gZW50aXR5LmRvbWFpbiAmJiBlbnRpdHkuZGF0YS5faWQgPyBcImVkaXRcIiA6IFwibmV3XCI7XG5cblx0XHRpZiAoZW50aXR5LmRvbWFpbiA9PT0gbnVsbCB8fCAhdnJlLmNvbGxlY3Rpb25zW2VudGl0eS5kb21haW5dKSB7IHJldHVybiBudWxsOyB9XG5cdFx0cmV0dXJuIChcblx0XHRcdDxQYWdlPlxuXHRcdFx0XHQ8Q29sbGVjdGlvblRhYnMgY29sbGVjdGlvbnM9e3ZyZS5jb2xsZWN0aW9uc30gb25OZXc9e29uTmV3fSBvblNlbGVjdERvbWFpbj17b25TZWxlY3REb21haW59XG5cdFx0XHRcdFx0YWN0aXZlRG9tYWluPXtlbnRpdHkuZG9tYWlufSAvPlxuXHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuXHRcdFx0XHRcdDxNZXNzYWdlc1xuXHRcdFx0XHRcdFx0dHlwZXM9e1tcIlNVQ0NFU1NfTUVTU0FHRVwiLCBcIkVSUk9SX01FU1NBR0VcIl19XG5cdFx0XHRcdFx0XHRtZXNzYWdlcz17bWVzc2FnZXN9XG5cdFx0XHRcdFx0XHRvbkRpc21pc3NNZXNzYWdlPXtvbkRpc21pc3NNZXNzYWdlfSAvPlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG5cdFx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC00XCI+XG5cdFx0XHRcdFx0XHRcdDxRdWlja1NlYXJjaFxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZT17b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlfVxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2g9e29uUXVpY2tTZWFyY2h9XG5cdFx0XHRcdFx0XHRcdFx0cXVlcnk9e3F1aWNrU2VhcmNoLnF1ZXJ5fSAvPlxuXHRcdFx0XHRcdFx0XHQ8RW50aXR5TGlzdFxuXHRcdFx0XHRcdFx0XHRcdHN0YXJ0PXtxdWlja1NlYXJjaC5zdGFydH1cblx0XHRcdFx0XHRcdFx0XHRsaXN0PXtxdWlja1NlYXJjaC5saXN0fVxuXHRcdFx0XHRcdFx0XHRcdG9uU2VsZWN0PXtvblNlbGVjdH1cblx0XHRcdFx0XHRcdFx0XHRkb21haW49e2VudGl0eS5kb21haW59IC8+XG5cdFx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHRcdHsgZW50aXR5LmRvbWFpbiA/IChcblx0XHRcdFx0XHRcdFx0PEVudGl0eUZvcm0gY3VycmVudE1vZGU9e2N1cnJlbnRNb2RlfSBnZXRBdXRvY29tcGxldGVWYWx1ZXM9e2dldEF1dG9jb21wbGV0ZVZhbHVlc31cblx0XHRcdFx0XHRcdFx0XHRlbnRpdHk9e2VudGl0eX0gb25OZXc9e29uTmV3fSBvbkRlbGV0ZT17b25EZWxldGV9IG9uQ2hhbmdlPXtvbkNoYW5nZX1cblx0XHRcdFx0XHRcdFx0XHRwcm9wZXJ0aWVzPXt2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0ucHJvcGVydGllc30gXG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5TGFiZWw9e3ZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXS5jb2xsZWN0aW9uTGFiZWwucmVwbGFjZSgvcyQvLCBcIlwiKSB9IC8+XG5cdFx0XHRcdFx0XHQpIDogbnVsbCB9XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDxkaXYgdHlwZT1cImZvb3Rlci1ib2R5XCI+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtNFwiPlxuXHRcdFx0XHRcdFx0PFBhZ2luYXRlXG5cdFx0XHRcdFx0XHRcdHN0YXJ0PXtxdWlja1NlYXJjaC5zdGFydH1cblx0XHRcdFx0XHRcdFx0bGlzdExlbmd0aD17cXVpY2tTZWFyY2gubGlzdC5sZW5ndGh9XG5cdFx0XHRcdFx0XHRcdHJvd3M9ezUwfVxuXHRcdFx0XHRcdFx0XHRvblBhZ2luYXRlTGVmdD17b25QYWdpbmF0ZUxlZnR9XG5cdFx0XHRcdFx0XHRcdG9uUGFnaW5hdGVSaWdodD17b25QYWdpbmF0ZVJpZ2h0fSAvPlxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLThcIj5cblx0XHRcdFx0XHRcdDxTYXZlRm9vdGVyIG9uU2F2ZT17b25TYXZlfSBvbkNhbmNlbD17KCkgPT4gY3VycmVudE1vZGUgPT09IFwiZWRpdFwiID9cblx0XHRcdFx0XHRcdFx0b25TZWxlY3Qoe2RvbWFpbjogZW50aXR5LmRvbWFpbiwgaWQ6IGVudGl0eS5kYXRhLl9pZH0pIDogb25OZXcoZW50aXR5LmRvbWFpbil9IC8+XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8ZGl2IHR5cGU9XCJmb290ZXItYm9keVwiPlxuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdDwvUGFnZT5cblx0XHQpXG5cdH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgRWRpdEd1aTtcbiIsImV4cG9ydCBkZWZhdWx0IChjYW1lbENhc2UpID0+IGNhbWVsQ2FzZVxuICAucmVwbGFjZSgvKFtBLVowLTldKS9nLCAobWF0Y2gpID0+IGAgJHttYXRjaC50b0xvd2VyQ2FzZSgpfWApXG4gIC5yZXBsYWNlKC9eLi8sIChtYXRjaCkgPT4gbWF0Y2gudG9VcHBlckNhc2UoKSk7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRjb25zdHJ1Y3Rvcihwcm9wcykge1xuXHRcdHN1cGVyKHByb3BzKTtcblxuXHRcdHRoaXMuc3RhdGUgPSB7IG5ld0xhYmVsOiBcIlwiLCBuZXdVcmw6IFwiXCIgfTtcblx0fVxuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0aWYgKG5leHRQcm9wcy5lbnRpdHkuZGF0YS5faWQgIT09IHRoaXMucHJvcHMuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwifSlcblx0XHR9XG5cdH1cblxuXHRvbkFkZCgpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0aWYgKHRoaXMuc3RhdGUubmV3TGFiZWwubGVuZ3RoID4gMCAmJiB0aGlzLnN0YXRlLm5ld1VybC5sZW5ndGggPiAwKSB7XG5cdFx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHtcblx0XHRcdFx0bGFiZWw6IHRoaXMuc3RhdGUubmV3TGFiZWwsXG5cdFx0XHRcdHVybDogdGhpcy5zdGF0ZS5uZXdVcmxcblx0XHRcdH0pKTtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld0xhYmVsOiBcIlwiLCBuZXdVcmw6IFwiXCJ9KTtcblx0XHR9XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdXG5cdFx0XHQuZmlsdGVyKCh2YWwpID0+IHZhbC51cmwgIT09IHZhbHVlLnVybCkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZS51cmx9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPlxuXHRcdFx0XHRcdDxhIGhyZWY9e3ZhbHVlLnVybH0gdGFyZ2V0PVwiX2JsYW5rXCI+XG5cdFx0XHRcdFx0XHR7dmFsdWUubGFiZWx9XG5cdFx0XHRcdFx0PC9hPlxuXHRcdFx0XHQ8L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50c31cblx0XHRcdFx0PGRpdiBzdHlsZT17e3dpZHRoOiBcIjEwMCVcIn19PlxuXHRcdFx0XHRcdDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbCBwdWxsLWxlZnRcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdMYWJlbH1cblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld0xhYmVsOiBldi50YXJnZXQudmFsdWV9KX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiTGFiZWwgZm9yIHVybC4uLlwiXG5cdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIG1heFdpZHRoOiBcIjUwJVwifX0gLz5cblx0XHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2wgcHVsbC1sZWZ0XCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3VXJsfVxuXHRcdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3VXJsOiBldi50YXJnZXQudmFsdWV9KX1cblx0XHRcdFx0XHRcdG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyB0aGlzLm9uQWRkKCkgOiBmYWxzZX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiVXJsLi4uXCJcblx0XHRcdFx0XHRcdHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgbWF4V2lkdGg6IFwiY2FsYyg1MCUgLSA4MHB4KVwifX0gLz5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cC1idG4gcHVsbC1sZWZ0XCI+XG5cdFx0XHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e3RoaXMub25BZGQuYmluZCh0aGlzKX0+QWRkIGxpbms8L2J1dHRvbj5cblx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCIsIGNsZWFyOiBcImxlZnRcIn19IC8+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0geyBuZXdWYWx1ZTogXCJcIiB9O1xuXHR9XG5cblx0Y29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcblx0XHRpZiAobmV4dFByb3BzLmVudGl0eS5kYXRhLl9pZCAhPT0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YS5faWQpIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld1ZhbHVlOiBcIlwifSlcblx0XHR9XG5cdH1cblxuXHRvbkFkZCh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHZhbHVlKSk7XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigodmFsKSA9PiB2YWwgIT09IHZhbHVlKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57dmFsdWV9PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld1ZhbHVlfVxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld1ZhbHVlOiBldi50YXJnZXQudmFsdWV9KX1cblx0XHRcdFx0XHRvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gdGhpcy5vbkFkZChldi50YXJnZXQudmFsdWUpIDogZmFsc2V9XG5cdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJBZGQgYSB2YWx1ZS4uLlwiIC8+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uLy4uLy4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdG9uQWRkKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQodmFsdWUpKTtcblx0fVxuXG5cdG9uUmVtb3ZlKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKCh2YWwpID0+IHZhbCAhPT0gdmFsdWUpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWV9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPnt2YWx1ZX08L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50c31cblx0XHRcdFx0PFNlbGVjdEZpZWxkIG9uQ2hhbmdlPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9IG5vQ2xlYXI9e3RydWV9IGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cblx0XHRcdFx0XHQ8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIj5cblx0XHRcdFx0XHRcdFNlbGVjdCB7bGFiZWwudG9Mb3dlckNhc2UoKX1cblx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdFx0e29wdGlvbnMuZmlsdGVyKChvcHQpID0+IHZhbHVlcy5pbmRleE9mKG9wdCkgPCAwKS5tYXAoKG9wdGlvbikgPT4gKFxuXHRcdFx0XHRcdFx0PHNwYW4ga2V5PXtvcHRpb259IHZhbHVlPXtvcHRpb259PntvcHRpb259PC9zcGFuPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHQ8L1NlbGVjdEZpZWxkPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXlcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uLy4uLy4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIG9uQWRkKCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgb25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh7XG4gICAgICBjb21wb25lbnRzOiBbe3R5cGU6IG9wdGlvbnNbMF0sIHZhbHVlOiBcIlwifV1cbiAgICB9KSk7XG4gIH1cblxuICBvbkFkZENvbXBvbmVudChpdGVtSW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5jb25jYXQoe3R5cGU6IG9wdGlvbnNbMF0sIHZhbHVlOiBcIlwifSlcbiAgICApO1xuICB9XG5cbiAgb25SZW1vdmVDb21wb25lbnQoaXRlbUluZGV4LCBjb21wb25lbnRJbmRleCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLmZpbHRlcigoY29tcG9uZW50LCBpZHgpID0+IGlkeCAhPT0gY29tcG9uZW50SW5kZXgpXG4gICAgKTtcbiAgfVxuXG4gIG9uQ2hhbmdlQ29tcG9uZW50VmFsdWUoaXRlbUluZGV4LCBjb21wb25lbnRJbmRleCwgdmFsdWUpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5tYXAoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggPT09IGNvbXBvbmVudEluZGV4XG4gICAgICAgID8gey4uLmNvbXBvbmVudCwgdmFsdWU6IHZhbHVlfSA6IGNvbXBvbmVudFxuICAgICkpO1xuICB9XG5cbiAgb25DaGFuZ2VDb21wb25lbnRUeXBlKGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgsIHR5cGUpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5tYXAoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggPT09IGNvbXBvbmVudEluZGV4XG4gICAgICAgID8gey4uLmNvbXBvbmVudCwgdHlwZTogdHlwZX0gOiBjb21wb25lbnRcbiAgICApKTtcbiAgfVxuXG4gIG9uUmVtb3ZlKGl0ZW1JbmRleCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgb25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKG5hbWUsIGlkeCkgPT4gaWR4ICE9PSBpdGVtSW5kZXgpKTtcbiAgfVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cbiAgICBjb25zdCBuYW1lRWxlbWVudHMgPSB2YWx1ZXMubWFwKChuYW1lLCBpKSA9PiAoXG4gICAgICA8ZGl2IGtleT17YCR7bmFtZX0tJHtpfWB9IGNsYXNzTmFtZT1cIm5hbWVzLWZvcm0gaXRlbS1lbGVtZW50XCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic21hbGwtbWFyZ2luXCI+XG4gICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcbiAgICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUoaSl9XG4gICAgICAgICAgICB0eXBlPVwiYnV0dG9uXCI+XG4gICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgPHN0cm9uZz5cbiAgICAgICAgICAgIHtuYW1lLmNvbXBvbmVudHMubWFwKChjb21wb25lbnQpID0+IGNvbXBvbmVudC52YWx1ZSkuam9pbihcIiBcIil9XG4gICAgICAgICAgPC9zdHJvbmc+XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8dWwga2V5PVwiY29tcG9uZW50LWxpc3RcIj5cbiAgICAgICAgICB7bmFtZS5jb21wb25lbnRzLm1hcCgoY29tcG9uZW50LCBqKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtgJHtpfS0ke2p9LWNvbXBvbmVudGB9PlxuICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwXCIga2V5PVwiY29tcG9uZW50LXZhbHVlc1wiPlxuICAgICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgICAgICAgICAgICA8U2VsZWN0RmllbGQgdmFsdWU9e2NvbXBvbmVudC50eXBlfSBub0NsZWFyPXt0cnVlfVxuICAgICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbCkgPT4gdGhpcy5vbkNoYW5nZUNvbXBvbmVudFR5cGUoaSwgaiwgdmFsKX1cbiAgICAgICAgICAgICAgICAgICAgYnRuQ2xhc3M9XCJidG4tZGVmYXVsdFwiPlxuICAgICAgICAgICAgICAgICAgICB7b3B0aW9ucy5tYXAoKG9wdGlvbikgPT4gKFxuICAgICAgICAgICAgICAgICAgICAgIDxzcGFuIHZhbHVlPXtvcHRpb259IGtleT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cbiAgICAgICAgICAgICAgICAgICAgKSl9XG4gICAgICAgICAgICAgICAgICA8L1NlbGVjdEZpZWxkPlxuICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICAgIDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiIGtleT17YGlucHV0LSR7aX0tJHtqfWB9XG4gICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KGV2KSA9PiB0aGlzLm9uQ2hhbmdlQ29tcG9uZW50VmFsdWUoaSwgaiwgZXYudGFyZ2V0LnZhbHVlKX1cbiAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyPXtjb21wb25lbnQudHlwZX0gdmFsdWU9e2NvbXBvbmVudC52YWx1ZX0gLz5cbiAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cC1idG5cIj5cbiAgICAgICAgICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZUNvbXBvbmVudChpLCBqKX0gPlxuICAgICAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgICAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICAgICAgICA8L3NwYW4+XG4gICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC91bD5cbiAgICAgICAgICA8YnV0dG9uIG9uQ2xpY2s9eygpID0+IHRoaXMub25BZGRDb21wb25lbnQoaSl9XG4gICAgICAgICAgICAgY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0IGJ0bi14cyBwdWxsLXJpZ2h0XCIgdHlwZT1cImJ1dHRvblwiPlxuICAgICAgICAgICAgQWRkIGNvbXBvbmVudFxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgIDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCIsIGhlaWdodDogXCI2cHhcIiwgY2xlYXI6IFwicmlnaHRcIn19IC8+XG4gICAgICA8L2Rpdj5cbiAgICApKVxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG4gICAgICAgIHtuYW1lRWxlbWVudHN9XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfT5cbiAgICAgICAgICBBZGQgbmFtZVxuICAgICAgICA8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBSZWxhdGlvbkZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgcXVlcnk6IFwiXCIsXG4gICAgICBzdWdnZXN0aW9uczogW10sXG4gICAgICBibHVySXNCbG9ja2VkOiBmYWxzZVxuICAgIH1cbiAgfVxuXG4gIG9uUmVtb3ZlKHZhbHVlKSB7XG4gICAgY29uc3QgY3VycmVudFZhbHVlcyA9IHRoaXMucHJvcHMuZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdW3RoaXMucHJvcHMubmFtZV0gfHwgW107XG5cbiAgICB0aGlzLnByb3BzLm9uQ2hhbmdlKFxuICAgICAgW1wiQHJlbGF0aW9uc1wiLCB0aGlzLnByb3BzLm5hbWVdLFxuICAgICAgY3VycmVudFZhbHVlcy5maWx0ZXIoKGN1clZhbCkgPT4gY3VyVmFsLmlkICE9PSB2YWx1ZS5pZClcbiAgICApO1xuXG4gIH1cblxuICBvbkFkZChzdWdnZXN0aW9uKSB7XG4gICAgY29uc3QgY3VycmVudFZhbHVlcyA9IHRoaXMucHJvcHMuZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdW3RoaXMucHJvcHMubmFtZV0gfHwgW107XG4gICAgaWYgKGN1cnJlbnRWYWx1ZXMubWFwKCh2YWwpID0+IHZhbC5pZCkuaW5kZXhPZihzdWdnZXN0aW9uLmtleSkgPiAtMSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cbiAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogW10sIHF1ZXJ5OiBcIlwiLCBibHVySXNCbG9ja2VkOiBmYWxzZX0pO1xuXG4gICAgdGhpcy5wcm9wcy5vbkNoYW5nZShcbiAgICAgIFtcIkByZWxhdGlvbnNcIiwgdGhpcy5wcm9wcy5uYW1lXSxcbiAgICAgIGN1cnJlbnRWYWx1ZXMuY29uY2F0KHtcbiAgICAgICAgaWQ6IHN1Z2dlc3Rpb24ua2V5LFxuICAgICAgICBkaXNwbGF5TmFtZTogc3VnZ2VzdGlvbi52YWx1ZSxcbiAgICAgICAgYWNjZXB0ZWQ6IHRydWVcbiAgICAgIH0pXG4gICAgKTtcbiAgfVxuXG4gIG9uUXVlcnlDaGFuZ2UoZXYpIHtcbiAgICBjb25zdCB7IGdldEF1dG9jb21wbGV0ZVZhbHVlcywgcGF0aCB9ID0gdGhpcy5wcm9wcztcbiAgICB0aGlzLnNldFN0YXRlKHtxdWVyeTogZXYudGFyZ2V0LnZhbHVlfSk7XG4gICAgaWYgKGV2LnRhcmdldC52YWx1ZSA9PT0gXCJcIikge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IFtdfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIGdldEF1dG9jb21wbGV0ZVZhbHVlcyhwYXRoLCBldi50YXJnZXQudmFsdWUsIChyZXN1bHRzKSA9PiB7XG4gICAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiByZXN1bHRzfSk7XG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICBvblF1ZXJ5Q2xlYXIoZXYpIHtcbiAgICBpZiAoIXRoaXMuc3RhdGUuYmx1cklzQmxvY2tlZCkge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IFtdLCBxdWVyeTogXCJcIn0pO1xuICAgIH1cbiAgfVxuXG4gIG9uQmx1ckJsb2NrKHRvZ2dsZSkge1xuICAgIHRoaXMuc2V0U3RhdGUoe2JsdXJJc0Jsb2NrZWQ6IHRvZ2dsZX0pO1xuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB2YWx1ZXMgPSBlbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcbiAgICBjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMuZmlsdGVyKCh2YWwpID0+IHZhbC5hY2NlcHRlZCkubWFwKCh2YWx1ZSwgaSkgPT4gKFxuICAgICAgPGRpdiBrZXk9e2Ake2l9LSR7dmFsdWUuaWR9YH0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG4gICAgICAgIDxzdHJvbmc+e3ZhbHVlLmRpc3BsYXlOYW1lfTwvc3Ryb25nPlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuICAgICAgPC9kaXY+XG4gICAgKSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgPGg0PntjYW1lbDJsYWJlbChuYW1lKX08L2g0PlxuICAgICAgICB7aXRlbUVsZW1lbnRzfVxuICAgICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgICAgICAgIG9uQmx1cj17dGhpcy5vblF1ZXJ5Q2xlYXIuYmluZCh0aGlzKX1cbiAgICAgICAgICAgICAgIG9uQ2hhbmdlPXt0aGlzLm9uUXVlcnlDaGFuZ2UuYmluZCh0aGlzKX1cbiAgICAgICAgICAgICAgIHZhbHVlPXt0aGlzLnN0YXRlLnF1ZXJ5fSBwbGFjZWhvbGRlcj1cIlNlYXJjaC4uLlwiIC8+XG5cbiAgICAgICAgPGRpdiBvbk1vdXNlT3Zlcj17KCkgPT4gdGhpcy5vbkJsdXJCbG9jayh0cnVlKX1cbiAgICAgICAgICAgICBvbk1vdXNlT3V0PXsoKSA9PiB0aGlzLm9uQmx1ckJsb2NrKGZhbHNlKX1cbiAgICAgICAgICAgICBzdHlsZT17e292ZXJmbG93WTogXCJhdXRvXCIsIG1heEhlaWdodDogXCIzMDBweFwifX0+XG4gICAgICAgICAge3RoaXMuc3RhdGUuc3VnZ2VzdGlvbnMubWFwKChzdWdnZXN0aW9uLCBpKSA9PiAoXG4gICAgICAgICAgICA8YSBrZXk9e2Ake2l9LSR7c3VnZ2VzdGlvbi5rZXl9YH0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCJcbiAgICAgICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vbkFkZChzdWdnZXN0aW9uKX0+XG4gICAgICAgICAgICAgIHtzdWdnZXN0aW9uLnZhbHVlfVxuICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgUmVsYXRpb25GaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50ID0gZW50aXR5LmRhdGFbbmFtZV0gJiYgZW50aXR5LmRhdGFbbmFtZV0ubGVuZ3RoID4gMCA/IChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e2VudGl0eS5kYXRhW25hbWVdfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IG9uQ2hhbmdlKFtuYW1lXSwgXCJcIil9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpIDogbnVsbDtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudH1cblx0XHRcdFx0PFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25DaGFuZ2UoW25hbWVdLCB2YWx1ZSl9XG5cdFx0XHRcdFx0bm9DbGVhcj17dHJ1ZX0gYnRuQ2xhc3M9XCJidG4tZGVmYXVsdFwiPlxuXHRcdFx0XHRcdDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPlxuXHRcdFx0XHRcdFx0U2VsZWN0IHtsYWJlbC50b0xvd2VyQ2FzZSgpfVxuXHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0XHR7b3B0aW9ucy5tYXAoKG9wdGlvbikgPT4gKFxuXHRcdFx0XHRcdFx0PHNwYW4ga2V5PXtvcHRpb259IHZhbHVlPXtvcHRpb259PntvcHRpb259PC9zcGFuPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHQ8L1NlbGVjdEZpZWxkPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXlcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIFN0cmluZ0ZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0PGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gb25DaGFuZ2UoW25hbWVdLCBldi50YXJnZXQudmFsdWUpfVxuXHRcdFx0XHRcdHZhbHVlPXtlbnRpdHkuZGF0YVtuYW1lXSB8fCBcIlwifVxuXHRcdFx0XHRcdHBsYWNlaG9sZGVyPXtgRW50ZXIgJHtsYWJlbC50b0xvd2VyQ2FzZSgpfWB9XG5cdFx0XHRcdC8+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cblN0cmluZ0ZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFN0cmluZ0ZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiXG5cbmltcG9ydCBTdHJpbmdGaWVsZCBmcm9tIFwiLi9maWVsZHMvc3RyaW5nLWZpZWxkXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL3NlbGVjdFwiO1xuaW1wb3J0IE11bHRpU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL211bHRpLXNlbGVjdFwiO1xuaW1wb3J0IFJlbGF0aW9uRmllbGQgZnJvbSBcIi4vZmllbGRzL3JlbGF0aW9uXCI7XG5pbXBvcnQgU3RyaW5nTGlzdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9saXN0LW9mLXN0cmluZ3NcIjtcbmltcG9ydCBMaW5rRmllbGQgZnJvbSBcIi4vZmllbGRzL2xpbmtzXCI7XG5pbXBvcnQgTmFtZXNGaWVsZCBmcm9tIFwiLi9maWVsZHMvbmFtZXNcIjtcblxuY29uc3QgZmllbGRNYXAgPSB7XG5cdFwic3RyaW5nXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwidGV4dFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcImRhdGFibGVcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJtdWx0aXNlbGVjdFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPE11bHRpU2VsZWN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPiksXG5cdFwic2VsZWN0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U2VsZWN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPiksXG5cdFwicmVsYXRpb25cIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxSZWxhdGlvbkZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gcGF0aD17ZmllbGREZWYucXVpY2tzZWFyY2h9IC8+KSxcbiAgXCJsaXN0LW9mLXN0cmluZ3NcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdMaXN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG4gIFwibGlua3NcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxMaW5rRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwibmFtZXNcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxOYW1lc0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gb3B0aW9ucz17ZmllbGREZWYub3B0aW9uc30gLz4pXG59O1xuXG5jbGFzcyBFbnRpdHlGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbk5ldywgb25EZWxldGUsIG9uQ2hhbmdlLCBnZXRBdXRvY29tcGxldGVWYWx1ZXMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBlbnRpdHksIGN1cnJlbnRNb2RlLCBwcm9wZXJ0aWVzLCBlbnRpdHlMYWJlbCB9ID0gdGhpcy5wcm9wcztcblxuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLThcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tcHJpbWFyeSBwdWxsLXJpZ2h0XCIgb25DbGljaz17KCkgPT4gb25OZXcoZW50aXR5LmRvbWFpbil9PlxuICAgICAgICAgICAgTmV3IHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+ICFmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT4gKDxkaXYga2V5PXtpfSBzdHlsZT17e1wiY29sb3JcIjogXCJyZWRcIn19PjxzdHJvbmc+RmllbGQgdHlwZSBub3Qgc3VwcG9ydGVkOiB7ZmllbGREZWYudHlwZX08L3N0cm9uZz48L2Rpdj4pKX1cbiAgICAgICAge3Byb3BlcnRpZXNcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gZmllbGRNYXAuaGFzT3duUHJvcGVydHkoZmllbGREZWYudHlwZSkpXG4gICAgICAgICAgLm1hcCgoZmllbGREZWYsIGkpID0+XG4gICAgICAgICAgZmllbGRNYXBbZmllbGREZWYudHlwZV0oZmllbGREZWYsIHtcblx0XHRcdFx0XHRcdGtleTogYCR7aX0tJHtmaWVsZERlZi5uYW1lfWAsXG5cdFx0XHRcdFx0XHRlbnRpdHk6IGVudGl0eSxcblx0XHRcdFx0XHRcdG9uQ2hhbmdlOiBvbkNoYW5nZSxcblx0XHRcdFx0XHRcdGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzXG5cdFx0XHRcdFx0fSlcbiAgICAgICAgKX1cbiAgICAgICAge2N1cnJlbnRNb2RlID09PSBcImVkaXRcIlxuICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAgICAgIDxoND5EZWxldGU8L2g0PlxuICAgICAgICAgICAgICA8YnV0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kYW5nZXJcIiBvbkNsaWNrPXtvbkRlbGV0ZX0+XG4gICAgICAgICAgICAgICAgRGVsZXRlIHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICAgICAgPC9idXRvbj5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVudGl0eUZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25TYXZlLCBvbkNhbmNlbCB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2PlxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnlcIiBvbkNsaWNrPXtvblNhdmV9PlNhdmU8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn1vcntcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1saW5rXCIgb25DbGljaz17b25DYW5jZWx9PkNhbmNlbDwvYnV0dG9uPlxuICAgIDwvZGl2PlxuICApO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vdXJsc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IHN0YXJ0LCBsaXN0LCBkb21haW4gfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJyZXN1bHQtbGlzdCByZXN1bHQtbGlzdC1lZGl0XCI+XG4gICAgICA8b2wgc3RhcnQ9e3N0YXJ0ICsgMX0gc3R5bGU9e3tjb3VudGVyUmVzZXQ6IGBzdGVwLWNvdW50ZXIgJHtzdGFydH1gfX0+XG4gICAgICAgIHtsaXN0Lm1hcCgoZW50cnksIGkpID0+IChcbiAgICAgICAgICA8bGkga2V5PXtgJHtpfS0ke2VudHJ5Ll9pZH1gfT5cbiAgICAgICAgICAgIDxMaW5rIHRvPXt1cmxzLmVudGl0eShkb21haW4sIGVudHJ5Ll9pZCl9IHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgd2lkdGg6IFwiY2FsYygxMDAlIC0gMzBweClcIiwgaGVpZ2h0OiBcIjEwMCVcIiwgcGFkZGluZzogXCIwLjVlbSAwXCJ9fT5cbiAgICAgICAgICAgICAge2VudHJ5W1wiQGRpc3BsYXlOYW1lXCJdfVxuICAgICAgICAgICAgPC9MaW5rPlxuICAgICAgICAgIDwvbGk+XG4gICAgICAgICkpfVxuICAgICAgPC9vbD5cbiAgICA8L2Rpdj5cbiAgKVxufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IG9uUGFnaW5hdGVMZWZ0LCBvblBhZ2luYXRlUmlnaHQgfSA9IHByb3BzO1xuICBjb25zdCB7IHN0YXJ0LCByb3dzLCBsaXN0TGVuZ3RoIH0gPSBwcm9wcztcblxuXG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2PlxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17c3RhcnQgPT09IDB9IG9uQ2xpY2s9e29uUGFnaW5hdGVMZWZ0fT5cbiAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1jaGV2cm9uLWxlZnRcIiAvPlxuICAgICAgPC9idXR0b24+XG4gICAgICB7XCIgXCJ9e3N0YXJ0ICsgMX0gLSB7c3RhcnQgKyByb3dzfXtcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgZGlzYWJsZWQ9e2xpc3RMZW5ndGggPCByb3dzfSBvbkNsaWNrPXtvblBhZ2luYXRlUmlnaHR9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWNoZXZyb24tcmlnaHRcIiAvPlxuICAgICAgPC9idXR0b24+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlLCBvblF1aWNrU2VhcmNoLCBxdWVyeSB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwIHNtYWxsLW1hcmdpbiBcIj5cbiAgICAgIDxpbnB1dCB0eXBlPVwidGV4dFwiIHBsYWNlaG9sZGVyPVwiU2VhcmNoIGZvci4uLlwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG4gICAgICAgIG9uQ2hhbmdlPXsoZXYpID0+IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZShldi50YXJnZXQudmFsdWUpfVxuICAgICAgICBvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gb25RdWlja1NlYXJjaCgpIDogZmFsc2V9XG4gICAgICAgIHZhbHVlPXtxdWVyeX1cbiAgICAgICAgLz5cbiAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e29uUXVpY2tTZWFyY2h9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tc2VhcmNoXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFua1wiIG9uQ2xpY2s9eygpID0+IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlKFwiXCIpOyBvblF1aWNrU2VhcmNoKCk7IH19PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICA8L3NwYW4+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcbmltcG9ydCBNZXNzYWdlIGZyb20gXCIuLi8uLi9tZXNzYWdlXCI7XG5cbmNvbnN0IExBQkVMUyA9IHtcblx0XCJTVUNDRVNTX01FU1NBR0VcIjogXCJcIixcblx0XCJFUlJPUl9NRVNTQUdFXCI6IChcblx0XHQ8c3Bhbj5cblx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tZXhjbGFtYXRpb24tc2lnblwiIC8+IFdhcm5pbmchXG5cdFx0PC9zcGFuPlxuXHQpXG59O1xuXG5jb25zdCBBTEVSVF9MRVZFTFMgPSB7XG5cdFwiU1VDQ0VTU19NRVNTQUdFXCI6IFwiaW5mb1wiLFxuXHRcIkVSUk9SX01FU1NBR0VcIjogXCJkYW5nZXJcIlxufTtcblxuY2xhc3MgTWVzc2FnZXMgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBtZXNzYWdlcywgdHlwZXMsIG9uRGlzbWlzc01lc3NhZ2UgfSA9IHRoaXMucHJvcHM7XG5cblx0XHRjb25zdCBmaWx0ZXJlZE1lc3NhZ2VzID0gbWVzc2FnZXMubG9nXG5cdFx0XHQubWFwKChtc2csIGlkeCkgPT4gKHttZXNzYWdlOiBtc2cubWVzc2FnZSwgaW5kZXg6IGlkeCwgdHlwZTogbXNnLnR5cGUsIGRpc21pc3NlZDogbXNnLmRpc21pc3NlZCB9KSlcblx0XHRcdC5maWx0ZXIoKG1zZykgPT4gdHlwZXMuaW5kZXhPZihtc2cudHlwZSkgPiAtMSAmJiAhbXNnLmRpc21pc3NlZCk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdj5cblx0XHRcdFx0e2ZpbHRlcmVkTWVzc2FnZXMubWFwKChtc2cpID0+IChcblx0XHRcdFx0XHQ8TWVzc2FnZSBrZXk9e21zZy5pbmRleH1cblx0XHRcdFx0XHRcdGRpc21pc3NpYmxlPXt0cnVlfVxuXHRcdFx0XHRcdFx0YWxlcnRMZXZlbD17QUxFUlRfTEVWRUxTW21zZy50eXBlXX1cblx0XHRcdFx0XHRcdG9uQ2xvc2VNZXNzYWdlPXsoKSA9PiBvbkRpc21pc3NNZXNzYWdlKG1zZy5pbmRleCl9PlxuXHRcdFx0XHRcdFx0PHN0cm9uZz57TEFCRUxTW21zZy50eXBlXX08L3N0cm9uZz4gPHNwYW4+e21zZy5tZXNzYWdlfTwvc3Bhbj5cblx0XHRcdFx0XHQ8L01lc3NhZ2U+XG5cdFx0XHRcdCkpfVxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5NZXNzYWdlcy5wcm9wVHlwZXMgPSB7XG5cdG1lc3NhZ2VzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRvbkRpc21pc3NNZXNzYWdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYy5pc1JlcXVpcmVkLFxuXHR0eXBlczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LmlzUmVxdWlyZWRcbn07XG5cbmV4cG9ydCBkZWZhdWx0IE1lc3NhZ2VzO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5jbGFzcyBTZWxlY3RGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIGlzT3BlbjogZmFsc2VcbiAgICB9O1xuICAgIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyID0gdGhpcy5oYW5kbGVEb2N1bWVudENsaWNrLmJpbmQodGhpcyk7XG4gIH1cblxuICBjb21wb25lbnREaWRNb3VudCgpIHtcbiAgICBkb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiY2xpY2tcIiwgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIsIGZhbHNlKTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgIGRvY3VtZW50LnJlbW92ZUV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgdG9nZ2xlU2VsZWN0KCkge1xuICAgIGlmKHRoaXMuc3RhdGUuaXNPcGVuKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtpc09wZW46IGZhbHNlfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogdHJ1ZX0pO1xuICAgIH1cbiAgfVxuXG4gIGhhbmRsZURvY3VtZW50Q2xpY2soZXYpIHtcbiAgICBjb25zdCB7IGlzT3BlbiB9ID0gdGhpcy5zdGF0ZTtcbiAgICBpZiAoaXNPcGVuICYmICFSZWFjdERPTS5maW5kRE9NTm9kZSh0aGlzKS5jb250YWlucyhldi50YXJnZXQpKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtcbiAgICAgICAgaXNPcGVuOiBmYWxzZVxuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25DaGFuZ2UsIG9uQ2xlYXIsIHZhbHVlLCBidG5DbGFzcywgbm9DbGVhciB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHNlbGVjdGVkT3B0aW9uID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnZhbHVlID09PSB2YWx1ZSk7XG4gICAgY29uc3QgcGxhY2Vob2xkZXIgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudHlwZSA9PT0gXCJwbGFjZWhvbGRlclwiKTtcbiAgICBjb25zdCBvdGhlck9wdGlvbnMgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgJiYgb3B0LnByb3BzLnZhbHVlICE9PSB2YWx1ZSk7XG5cbiAgICByZXR1cm4gKFxuXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17Y3goXCJkcm9wZG93blwiLCB7b3BlbjogdGhpcy5zdGF0ZS5pc09wZW59KX0+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPXtjeChcImJ0blwiLCBcImRyb3Bkb3duLXRvZ2dsZVwiLCBidG5DbGFzcyB8fCBcImJ0bi1ibGFua1wiKX0gb25DbGljaz17dGhpcy50b2dnbGVTZWxlY3QuYmluZCh0aGlzKX0+XG4gICAgICAgICAge3NlbGVjdGVkT3B0aW9uLmxlbmd0aCA/IHNlbGVjdGVkT3B0aW9uIDogcGxhY2Vob2xkZXJ9IDxzcGFuIGNsYXNzTmFtZT1cImNhcmV0XCIgLz5cbiAgICAgICAgPC9idXR0b24+XG5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cImRyb3Bkb3duLW1lbnVcIj5cbiAgICAgICAgICB7IHZhbHVlICYmICFub0NsZWFyID8gKFxuICAgICAgICAgICAgPGxpPlxuICAgICAgICAgICAgICA8YSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2xlYXIoKTsgdGhpcy50b2dnbGVTZWxlY3QoKTt9fT5cbiAgICAgICAgICAgICAgICAtIGNsZWFyIC1cbiAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApIDogbnVsbH1cbiAgICAgICAgICB7b3RoZXJPcHRpb25zLm1hcCgob3B0aW9uLCBpKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtpfT5cbiAgICAgICAgICAgICAgPGEgc3R5bGU9e3tjdXJzb3I6IFwicG9pbnRlclwifX0gb25DbGljaz17KCkgPT4geyBvbkNoYW5nZShvcHRpb24ucHJvcHMudmFsdWUpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpOyB9fT57b3B0aW9ufTwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cblNlbGVjdEZpZWxkLnByb3BUeXBlcyA9IHtcbiAgb25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICBvbkNsZWFyOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgdmFsdWU6IFJlYWN0LlByb3BUeXBlcy5hbnksXG4gIGJ0bkNsYXNzOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBub0NsZWFyOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgU2VsZWN0RmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmZ1bmN0aW9uIEZvb3Rlcihwcm9wcykge1xuICBjb25zdCBoaUxvZ28gPSAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMSBjb2wtbWQtMVwiPlxuICAgICAgPGltZyBjbGFzc05hbWU9XCJoaS1sb2dvXCIgc3JjPVwiaW1hZ2VzL2xvZ28taHV5Z2Vucy1pbmcuc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBjbGFyaWFoTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1jbGFyaWFoLnN2Z1wiIC8+XG4gICAgPC9kaXY+XG4gICk7XG5cbiAgY29uc3QgZm9vdGVyQm9keSA9IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSA+IDAgP1xuICAgIFJlYWN0LkNoaWxkcmVuLm1hcChwcm9wcy5jaGlsZHJlbiwgKGNoaWxkLCBpKSA9PiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIHtpID09PSBSZWFjdC5DaGlsZHJlbi5jb3VudChwcm9wcy5jaGlsZHJlbikgLSAxXG4gICAgICAgICAgICA/ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPntoaUxvZ299PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+e2NoaWxkfTwvZGl2PntjbGFyaWFoTG9nb308L2Rpdj4pXG4gICAgICAgICAgICA6ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPntjaGlsZH08L2Rpdj4pXG4gICAgICAgICAgfVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICkpIDogKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJ3aGl0ZS1iYXJcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPlxuICAgICAgICAgICAge2hpTG9nb31cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEwIGNvbC1tZC0xMCB0ZXh0LWNlbnRlclwiPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICB7Y2xhcmlhaExvZ299XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcblxuXG4gIHJldHVybiAoXG4gICAgPGZvb3RlciBjbGFzc05hbWU9XCJmb290ZXJcIj5cbiAgICAgIHtmb290ZXJCb2R5fVxuICAgIDwvZm9vdGVyPlxuICApXG59XG5cbmV4cG9ydCBkZWZhdWx0IEZvb3RlcjsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBkaXNtaXNzaWJsZSwgYWxlcnRMZXZlbCwgb25DbG9zZU1lc3NhZ2V9ID0gcHJvcHM7XG4gIGNvbnN0IGRpc21pc3NCdXR0b24gPSBkaXNtaXNzaWJsZVxuICAgID8gPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3NOYW1lPVwiY2xvc2VcIiBvbkNsaWNrPXtvbkNsb3NlTWVzc2FnZX0+PHNwYW4+JnRpbWVzOzwvc3Bhbj48L2J1dHRvbj5cbiAgICA6IG51bGw7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT17Y3goXCJhbGVydFwiLCBgYWxlcnQtJHthbGVydExldmVsfWAsIHtcImFsZXJ0LWRpc21pc3NpYmxlXCI6IGRpc21pc3NpYmxlfSl9IHJvbGU9XCJhbGVydFwiPlxuICAgICAge2Rpc21pc3NCdXR0b259XG4gICAgICB7cHJvcHMuY2hpbGRyZW59XG4gICAgPC9kaXY+XG4gIClcbn07IiwiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBGb290ZXIgZnJvbSBcIi4vZm9vdGVyXCI7XG5cbmNvbnN0IEZPT1RFUl9IRUlHSFQgPSA4MTtcblxuZnVuY3Rpb24gUGFnZShwcm9wcykge1xuICBjb25zdCBmb290ZXJzID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheShwcm9wcy5jaGlsZHJlbikuZmlsdGVyKChjaGlsZCkgPT4gY2hpbGQucHJvcHMudHlwZSA9PT0gXCJmb290ZXItYm9keVwiKTtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwicGFnZVwiPlxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW4gaGktR3JlZW4gY29udGFpbmVyLWZsdWlkXCI+XG4gICAgICAgIDxuYXYgY2xhc3NOYW1lPVwibmF2YmFyIFwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cIm5hdmJhci1oZWFkZXJcIj4gPGEgY2xhc3NOYW1lPVwibmF2YmFyLWJyYW5kXCIgaHJlZj1cIiNcIj48aW1nIHNyYz1cImltYWdlcy9sb2dvLXRpbWJ1Y3Rvby5zdmdcIiBjbGFzc05hbWU9XCJsb2dvXCIgYWx0PVwidGltYnVjdG9vXCIvPjwvYT4gPC9kaXY+XG4gICAgICAgICAgICA8ZGl2IGlkPVwibmF2YmFyXCIgY2xhc3NOYW1lPVwibmF2YmFyLWNvbGxhcHNlIGNvbGxhcHNlXCI+XG4gICAgICAgICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2YmFyLW5hdiBuYXZiYXItcmlnaHRcIj5cbiAgICAgICAgICAgICAgICB7cHJvcHMudXNlcm5hbWUgPyA8bGk+PGEgaHJlZj17cHJvcHMudXNlcmxvY2F0aW9uIHx8ICcjJ30+PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi11c2VyXCIvPiB7cHJvcHMudXNlcm5hbWV9PC9hPjwvbGk+IDogbnVsbH1cbiAgICAgICAgICAgICAgPC91bD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L25hdj5cbiAgICAgIDwvZGl2PlxuICAgICAgPGRpdiAgc3R5bGU9e3ttYXJnaW5Cb3R0b206IGAke0ZPT1RFUl9IRUlHSFQgKiBmb290ZXJzLmxlbmd0aH1weGB9fT5cbiAgICAgICAge1JlYWN0LkNoaWxkcmVuLnRvQXJyYXkocHJvcHMuY2hpbGRyZW4pLmZpbHRlcigoY2hpbGQpID0+IGNoaWxkLnByb3BzLnR5cGUgIT09IFwiZm9vdGVyLWJvZHlcIil9XG4gICAgICA8L2Rpdj5cbiAgICAgIDxGb290ZXI+XG4gICAgICAgIHtmb290ZXJzfVxuICAgICAgPC9Gb290ZXI+XG4gICAgPC9kaXY+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IFBhZ2U7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuL3N0b3JlXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9hY3Rpb25zXCI7XG5pbXBvcnQge3NldFZyZX0gZnJvbSBcIi4vYWN0aW9ucy92cmVcIjtcbmltcG9ydCBBcHAgZnJvbSBcIi4vY29tcG9uZW50cy9lZGl0LWd1aS9lZGl0LWd1aVwiO1xuaW1wb3J0IGdldEF1dG9jb21wbGV0ZVZhbHVlcyBmcm9tIFwiLi9hY3Rpb25zL2F1dG9jb21wbGV0ZVwiO1xuXG5pbXBvcnQgcm91dGVyIGZyb20gXCIuL3JvdXRlclwiO1xuXG5jb25zdCBzZXRVc2VyID0gKHJlc3BvbnNlKSA9PiB7XG5cdHJldHVybiB7XG5cdFx0dHlwZTogXCJTRVRfVVNFUlwiLFxuXHRcdHVzZXI6IHJlc3BvbnNlXG5cdH07XG59O1xuXG5kb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiRE9NQ29udGVudExvYWRlZFwiLCAoKSA9PiB7XG5cblx0ZnVuY3Rpb24gaW5pdFJvdXRlcigpIHtcblx0XHRSZWFjdERPTS5yZW5kZXIocm91dGVyLCBkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKSk7XG5cdH1cblxuXHRmdW5jdGlvbiBnZXRWcmVJZCgpIHtcblx0XHRsZXQgcGF0aCA9IHdpbmRvdy5sb2NhdGlvbi5zZWFyY2guc3Vic3RyKDEpO1xuXHRcdGxldCBwYXJhbXMgPSBwYXRoLnNwbGl0KFwiJlwiKTtcblxuXHRcdGZvcihsZXQgaSBpbiBwYXJhbXMpIHtcblx0XHRcdGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoXCI9XCIpO1xuXHRcdFx0aWYoa2V5ID09PSBcInZyZUlkXCIpIHtcblx0XHRcdFx0cmV0dXJuIHZhbHVlO1xuXHRcdFx0fVxuXHRcdH1cblx0XHRyZXR1cm4gXCJXb21lbldyaXRlcnNcIjtcblx0fVxuXG5cdGZ1bmN0aW9uIGdldExvZ2luKCkge1xuXHRcdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdFx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdFx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdFx0bGV0IFtrZXksIHZhbHVlXSA9IHBhcmFtc1tpXS5zcGxpdChcIj1cIik7XG5cdFx0XHRpZihrZXkgPT09IFwiaHNpZFwiKSB7XG5cdFx0XHRcdHJldHVybiB7dXNlcjogdmFsdWUsIHRva2VuOiB2YWx1ZX07XG5cdFx0XHR9XG5cdFx0fVxuXHRcdHJldHVybiB1bmRlZmluZWQ7XG5cdH1cblx0c3RvcmUuZGlzcGF0Y2goc2V0VnJlKGdldFZyZUlkKCksIGluaXRSb3V0ZXIpKTtcblx0c3RvcmUuZGlzcGF0Y2goc2V0VXNlcihnZXRMb2dpbigpKSk7XG59KTsiLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5cbmxldCBpbml0aWFsU3RhdGUgPSB7XG5cdGRhdGE6IHtcblx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0fSxcblx0ZG9tYWluOiBudWxsLFxuXHRlcnJvck1lc3NhZ2U6IG51bGxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YTogYWN0aW9uLmRhdGEsXG5cdFx0XHRcdGRvbWFpbjogYWN0aW9uLmRvbWFpbixcblx0XHRcdFx0ZXJyb3JNZXNzYWdlOiBhY3Rpb24uZXJyb3JNZXNzYWdlIHx8IG51bGxcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YTogc2V0SW4oYWN0aW9uLmZpZWxkUGF0aCwgYWN0aW9uLnZhbHVlLCBzdGF0ZS5kYXRhKVxuXHRcdFx0fX07XG5cblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlfRkFJTFVSRVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiB7XG5cdFx0XHRcdFx0XCJAcmVsYXRpb25zXCI6IFtdXG5cdFx0XHRcdH0sXG5cdFx0XHRcdGVycm9yTWVzc2FnZTogYWN0aW9uLmVycm9yTWVzc2FnZVxuXHRcdFx0fX07XG5cblx0XHRjYXNlIFwiU0VUX1ZSRVwiOiB7XG5cdFx0XHRyZXR1cm4gaW5pdGlhbFN0YXRlO1xuXHRcdH1cblxuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufSIsImltcG9ydCB7Y29tYmluZVJlZHVjZXJzfSBmcm9tIFwicmVkdXhcIjtcblxuaW1wb3J0IGVudGl0eSBmcm9tIFwiLi9lbnRpdHlcIjtcbmltcG9ydCBtZXNzYWdlcyBmcm9tIFwiLi9tZXNzYWdlc1wiO1xuaW1wb3J0IHVzZXIgZnJvbSBcIi4vdXNlclwiO1xuaW1wb3J0IHZyZSBmcm9tIFwiLi92cmVcIjtcbmltcG9ydCBxdWlja1NlYXJjaCBmcm9tIFwiLi9xdWljay1zZWFyY2hcIjtcblxuZXhwb3J0IGRlZmF1bHQgY29tYmluZVJlZHVjZXJzKHtcblx0dnJlOiB2cmUsXG5cdGVudGl0eTogZW50aXR5LFxuXHR1c2VyOiB1c2VyLFxuXHRtZXNzYWdlczogbWVzc2FnZXMsXG5cdHF1aWNrU2VhcmNoOiBxdWlja1NlYXJjaFxufSk7IiwiaW1wb3J0IHNldEluIGZyb20gXCIuLi91dGlsL3NldC1pblwiO1xuXG5jb25zdCBpbml0aWFsU3RhdGUgPSB7XG5cdGxvZzogW11cbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiUkVRVUVTVF9NRVNTQUdFXCI6XG5cdFx0XHRzdGF0ZS5sb2cucHVzaCh7bWVzc2FnZTogYWN0aW9uLm1lc3NhZ2UsIHR5cGU6IGFjdGlvbi50eXBlLCB0aW1lOiBuZXcgRGF0ZSgpfSk7XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0Y2FzZSBcIlNVQ0NFU1NfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJFUlJPUl9NRVNTQUdFXCI6XG5cdFx0XHRzdGF0ZS5sb2cucHVzaCh7bWVzc2FnZTogYWN0aW9uLm1lc3NhZ2UsIHR5cGU6IGFjdGlvbi50eXBlLCB0aW1lOiBuZXcgRGF0ZSgpfSk7XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0Y2FzZSBcIkRJU01JU1NfTUVTU0FHRVwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGxvZzogc2V0SW4oW2FjdGlvbi5tZXNzYWdlSW5kZXgsIFwiZGlzbWlzc2VkXCJdLCB0cnVlLCBzdGF0ZS5sb2cpXG5cdFx0XHR9O1xuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufSIsImxldCBpbml0aWFsU3RhdGUgPSB7XG5cdHN0YXJ0OiAwLFxuXHRsaXN0OiBbXSxcblx0cm93czogNTAsXG5cdHF1ZXJ5OiBcIlwiXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCBzdGFydDogYWN0aW9uLnN0YXJ0fTtcblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRsaXN0OiBhY3Rpb24uZGF0YVxuXHRcdFx0fX07XG5cdFx0Y2FzZSBcIlNFVF9RVUlDS1NFQVJDSF9RVUVSWVwiOiB7XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdHF1ZXJ5OiBhY3Rpb24udmFsdWVcblx0XHRcdH19O1xuXHRcdH1cblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IG51bGw7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1VTRVJcIjpcblx0XHRcdGlmIChhY3Rpb24udXNlcikge1xuXHRcdFx0XHRyZXR1cm4gYWN0aW9uLnVzZXI7XG5cdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0XHR9XG5cdFx0XHRicmVhaztcblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0dnJlSWQ6IG51bGwsXG5cdGxpc3Q6IFtdLFxuXHRjb2xsZWN0aW9uczoge30sXG5cdGRvbWFpbjogbnVsbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfVlJFXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0dnJlSWQ6IGFjdGlvbi52cmVJZCxcblx0XHRcdFx0Y29sbGVjdGlvbnM6IGFjdGlvbi5jb2xsZWN0aW9ucyB8fCBudWxsLFxuXHRcdFx0XHRsaXN0OiBhY3Rpb24ubGlzdCB8fCBzdGF0ZS5saXN0XG5cdFx0XHR9O1xuXG5cdFx0Y2FzZSBcIkxJU1RfVlJFU1wiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5saXN0LFxuXHRcdFx0XHRjb2xsZWN0aW9uczogbnVsbFxuXHRcdFx0fTtcblx0XHRjYXNlIFwiU0VUX0RPTUFJTlwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGRvbWFpbjogYWN0aW9uLmRvbWFpblxuXHRcdFx0fTtcblxuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQge1JvdXRlciwgUmVkaXJlY3QsIFJvdXRlLCBoYXNoSGlzdG9yeX0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHtQcm92aWRlciwgY29ubmVjdH0gZnJvbSBcInJlYWN0LXJlZHV4XCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCBnZXRBdXRvY29tcGxldGVWYWx1ZXMgZnJvbSBcIi4vYWN0aW9ucy9hdXRvY29tcGxldGVcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2FjdGlvbnNcIjtcblxuaW1wb3J0IEVkaXRHdWkgZnJvbSBcIi4vY29tcG9uZW50cy9lZGl0LWd1aS9lZGl0LWd1aVwiO1xuaW1wb3J0IHt1cmxzfSBmcm9tIFwiLi91cmxzXCI7XG5cblxuXG5cbmV4cG9ydCBmdW5jdGlvbiBuYXZpZ2F0ZVRvKGtleSwgYXJncykge1xuXHRoYXNoSGlzdG9yeS5wdXNoKHVybHNba2V5XS5hcHBseShudWxsLCBhcmdzKSk7XG59XG5cbmNvbnN0IGRlZmF1bHRDb25uZWN0ID0gY29ubmVjdChcblx0c3RhdGUgPT4gKHsuLi5zdGF0ZSwgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzOiBnZXRBdXRvY29tcGxldGVWYWx1ZXN9KSxcblx0ZGlzcGF0Y2ggPT4gYWN0aW9ucyhuYXZpZ2F0ZVRvLCBkaXNwYXRjaClcbik7XG5cblxuZXhwb3J0IGRlZmF1bHQgKFxuXHQ8UHJvdmlkZXIgc3RvcmU9e3N0b3JlfT5cblx0XHQ8Um91dGVyIGhpc3Rvcnk9e2hhc2hIaXN0b3J5fT5cblx0XHRcdDxSb3V0ZSBwYXRoPXt1cmxzLnJvb3QoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5uZXdFbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5lbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0PC9Sb3V0ZXI+XG5cdDwvUHJvdmlkZXI+XG4pO1xuXG4iLCJpbXBvcnQge2NyZWF0ZVN0b3JlLCBhcHBseU1pZGRsZXdhcmV9IGZyb20gXCJyZWR1eFwiO1xuaW1wb3J0IHRodW5rTWlkZGxld2FyZSBmcm9tIFwicmVkdXgtdGh1bmtcIjtcblxuaW1wb3J0IHJlZHVjZXJzIGZyb20gXCIuLi9yZWR1Y2Vyc1wiO1xuXG5jb25zdCBsb2dnZXIgPSAoKSA9PiBuZXh0ID0+IGFjdGlvbiA9PiB7XG4gIGlmIChhY3Rpb24uaGFzT3duUHJvcGVydHkoXCJ0eXBlXCIpKSB7XG4gICAgY29uc29sZS5sb2coXCJbUkVEVVhdXCIsIGFjdGlvbi50eXBlLCBhY3Rpb24pO1xuICB9XG5cbiAgcmV0dXJuIG5leHQoYWN0aW9uKTtcbn07XG5cbmxldCBjcmVhdGVTdG9yZVdpdGhNaWRkbGV3YXJlID0gYXBwbHlNaWRkbGV3YXJlKC8qbG9nZ2VyLCovIHRodW5rTWlkZGxld2FyZSkoY3JlYXRlU3RvcmUpO1xuZXhwb3J0IGRlZmF1bHQgY3JlYXRlU3RvcmVXaXRoTWlkZGxld2FyZShyZWR1Y2Vycyk7XG4iLCJjb25zdCB1cmxzID0ge1xuXHRyb290KCkge1xuXHRcdHJldHVybiBcIi9cIjtcblx0fSxcblx0bmV3RW50aXR5KGNvbGxlY3Rpb24pIHtcblx0XHRyZXR1cm4gY29sbGVjdGlvblxuXHRcdFx0PyBgLyR7Y29sbGVjdGlvbn1gXG5cdFx0XHQ6IFwiLzpjb2xsZWN0aW9uXCI7XG5cdH0sXG5cdGVudGl0eShjb2xsZWN0aW9uLCBpZCkge1xuXHRcdHJldHVybiBjb2xsZWN0aW9uICYmIGlkXG5cdFx0XHQ/IGAvJHtjb2xsZWN0aW9ufS8ke2lkfWBcblx0XHRcdDogXCIvOmNvbGxlY3Rpb24vOmlkXCI7XG5cdH1cbn07XG5cbmV4cG9ydCB7IHVybHMgfSIsImZ1bmN0aW9uIGRlZXBDbG9uZTkob2JqKSB7XG4gICAgdmFyIGksIGxlbiwgcmV0O1xuXG4gICAgaWYgKHR5cGVvZiBvYmogIT09IFwib2JqZWN0XCIgfHwgb2JqID09PSBudWxsKSB7XG4gICAgICAgIHJldHVybiBvYmo7XG4gICAgfVxuXG4gICAgaWYgKEFycmF5LmlzQXJyYXkob2JqKSkge1xuICAgICAgICByZXQgPSBbXTtcbiAgICAgICAgbGVuID0gb2JqLmxlbmd0aDtcbiAgICAgICAgZm9yIChpID0gMDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgICAgICByZXQucHVzaCggKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXSApO1xuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcmV0ID0ge307XG4gICAgICAgIGZvciAoaSBpbiBvYmopIHtcbiAgICAgICAgICAgIGlmIChvYmouaGFzT3duUHJvcGVydHkoaSkpIHtcbiAgICAgICAgICAgICAgICByZXRbaV0gPSAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuICAgIHJldHVybiByZXQ7XG59XG5cbmV4cG9ydCBkZWZhdWx0IGRlZXBDbG9uZTk7IiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuLy8gRG8gZWl0aGVyIG9mIHRoZXNlOlxuLy8gIGEpIFNldCBhIHZhbHVlIGJ5IHJlZmVyZW5jZSBpZiBkZXJlZiBpcyBub3QgbnVsbFxuLy8gIGIpIFNldCBhIHZhbHVlIGRpcmVjdGx5IGluIHRvIGRhdGEgb2JqZWN0IGlmIGRlcmVmIGlzIG51bGxcbmNvbnN0IHNldEVpdGhlciA9IChkYXRhLCBkZXJlZiwga2V5LCB2YWwpID0+IHtcblx0KGRlcmVmIHx8IGRhdGEpW2tleV0gPSB2YWw7XG5cdHJldHVybiBkYXRhO1xufTtcblxuLy8gU2V0IGEgbmVzdGVkIHZhbHVlIGluIGRhdGEgKG5vdCB1bmxpa2UgaW1tdXRhYmxlanMsIGJ1dCBhIGNsb25lIG9mIGRhdGEgaXMgZXhwZWN0ZWQgZm9yIHByb3BlciBpbW11dGFiaWxpdHkpXG5jb25zdCBfc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID0gbnVsbCkgPT5cblx0cGF0aC5sZW5ndGggPiAxID9cblx0XHRfc2V0SW4ocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID8gZGVyZWZbcGF0aC5zaGlmdCgpXSA6IGRhdGFbcGF0aC5zaGlmdCgpXSkgOlxuXHRcdHNldEVpdGhlcihkYXRhLCBkZXJlZiwgcGF0aFswXSwgdmFsdWUpO1xuXG5jb25zdCBzZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSkgPT5cblx0X3NldEluKGNsb25lKHBhdGgpLCB2YWx1ZSwgY2xvbmUoZGF0YSkpO1xuXG5leHBvcnQgZGVmYXVsdCBzZXRJbjsiXX0=
