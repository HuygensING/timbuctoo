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
      suggestions: []
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
      getAutocompleteValues(path, ev.target.value, function (results) {
        _this2.setState({ suggestions: results });
      });
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
        _react2.default.createElement("input", { className: "form-control", onChange: this.onQueryChange.bind(this),
          value: this.state.query, placeholder: "Search..." }),
        _react2.default.createElement(
          "div",
          { style: { overflowY: "auto", maxHeight: "300px" } },
          this.state.suggestions.map(function (suggestion) {
            return _react2.default.createElement(
              "a",
              { key: suggestion.key, className: "item-element",
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
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDRWUsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFJLFVBQVU7QUFDYixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEYSxFQUFkOztBQUlBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxPQUFLLEtBQUssS0FBTCxDQUFXLElBQVgsRUFBaUIsR0FBakIsQ0FBcUIsVUFBQyxDQUFELEVBQU87QUFBRSxVQUFPLEVB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RUFBb0IsS0FBcEIsRUFBMkIseUJBQU0sSUFBTixDQUEzQixDQURhO0FBQUEsQ0FBZDs7a0JBR2UsSyIsImZpbGUiOiJnZW5lcmF0ZWQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlc0NvbnRlbnQiOlsiKGZ1bmN0aW9uIGUodCxuLHIpe2Z1bmN0aW9uIHMobyx1KXtpZighbltvXSl7aWYoIXRbb10pe3ZhciBhPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7aWYoIXUmJmEpcmV0dXJuIGEobywhMCk7aWYoaSlyZXR1cm4gaShvLCEwKTt2YXIgZj1uZXcgRXJyb3IoXCJDYW5ub3QgZmluZCBtb2R1bGUgJ1wiK28rXCInXCIpO3Rocm93IGYuY29kZT1cIk1PRFVMRV9OT1RfRk9VTkRcIixmfXZhciBsPW5bb109e2V4cG9ydHM6e319O3Rbb11bMF0uY2FsbChsLmV4cG9ydHMsZnVuY3Rpb24oZSl7dmFyIG49dFtvXVsxXVtlXTtyZXR1cm4gcyhuP246ZSl9LGwsbC5leHBvcnRzLGUsdCxuLHIpfXJldHVybiBuW29dLmV4cG9ydHN9dmFyIGk9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtmb3IodmFyIG89MDtvPHIubGVuZ3RoO28rKylzKHJbb10pO3JldHVybiBzfSkiLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwYXRoLCBxdWVyeSwgZG9uZSkge1xuXHRsZXQgb3B0aW9ucyA9IHtcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS8ke3BhdGgucmVwbGFjZSgvXlxcL3ZbXi9dK1xcLy8sIFwiXCIpfT9xdWVyeT0ke3F1ZXJ5fSpgXG5cdH07XG5cblx0bGV0IHhockRvbmUgPSBmdW5jdGlvbihlcnIsIHJlc3BvbnNlLCBib2R5KSB7XG5cdFx0ZG9uZShKU09OLnBhcnNlKGJvZHkpLm1hcCgoZCkgPT4geyByZXR1cm4ge2tleTogZC5rZXkucmVwbGFjZSgvXi4rXFwvLywgXCJcIiksIHZhbHVlOiBkLnZhbHVlfTsgfSkpO1xuXHR9O1xuXG5cdHNlcnZlci5mYXN0WGhyKG9wdGlvbnMsIHhockRvbmUpO1xufSIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5cbmNvbnN0IHNhdmVOZXdFbnRpdHkgPSAoZG9tYWluLCBzYXZlRGF0YSwgdG9rZW4sIHZyZUlkLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIlBPU1RcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHRib2R5OiBKU09OLnN0cmluZ2lmeShzYXZlRGF0YSksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufWBcblx0fSwgbmV4dCwgZmFpbCwgYENyZWF0ZSBuZXcgJHtkb21haW59YCk7XG5cbmNvbnN0IHVwZGF0ZUVudGl0eSA9IChkb21haW4sIHNhdmVEYXRhLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiUFVUXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0Ym9keTogSlNPTi5zdHJpbmdpZnkoc2F2ZURhdGEpLFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0vJHtzYXZlRGF0YS5faWR9YFxuXHR9LCBuZXh0LCBmYWlsLCBgVXBkYXRlICR7ZG9tYWlufWApO1xuXG5jb25zdCBkZWxldGVFbnRpdHkgPSAoZG9tYWluLCBlbnRpdHlJZCwgdG9rZW4sIHZyZUlkLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkRFTEVURVwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0vJHtlbnRpdHlJZH1gXG5cdH0sIG5leHQsIGZhaWwsIGBEZWxldGUgJHtkb21haW59YCk7XG5cbmNvbnN0IGZldGNoRW50aXR5ID0gKGxvY2F0aW9uLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIn0sXG5cdFx0dXJsOiBsb2NhdGlvblxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0Y29uc3QgZGF0YSA9IEpTT04ucGFyc2UocmVzcC5ib2R5KTtcblx0XHRuZXh0KGRhdGEpO1xuXHR9LCBmYWlsLCBcIkZldGNoIGVudGl0eVwiKTtcblxuY29uc3QgZmV0Y2hFbnRpdHlMaXN0ID0gKGRvbWFpbiwgc3RhcnQsIHJvd3MsIG5leHQpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1wiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwifSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59P3Jvd3M9JHtyb3dzfSZzdGFydD0ke3N0YXJ0fWBcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGNvbnN0IGRhdGEgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0bmV4dChkYXRhKTtcblx0fSk7XG5cbmNvbnN0IGNydWQgPSB7XG5cdHNhdmVOZXdFbnRpdHk6IHNhdmVOZXdFbnRpdHksXG5cdHVwZGF0ZUVudGl0eTogdXBkYXRlRW50aXR5LFxuXHRkZWxldGVFbnRpdHk6IGRlbGV0ZUVudGl0eSxcblx0ZmV0Y2hFbnRpdHk6IGZldGNoRW50aXR5LFxuXHRmZXRjaEVudGl0eUxpc3Q6IGZldGNoRW50aXR5TGlzdFxufTtcblxuZXhwb3J0IHtzYXZlTmV3RW50aXR5LCB1cGRhdGVFbnRpdHksIGRlbGV0ZUVudGl0eSwgZmV0Y2hFbnRpdHksIGZldGNoRW50aXR5TGlzdCwgY3J1ZH07IiwiaW1wb3J0IGNsb25lIGZyb20gXCIuLi91dGlsL2Nsb25lLWRlZXBcIjtcbmltcG9ydCB7IGNydWQgfSBmcm9tIFwiLi9jcnVkXCI7XG5pbXBvcnQgc2F2ZVJlbGF0aW9ucyBmcm9tIFwiLi9zYXZlLXJlbGF0aW9uc1wiO1xuaW1wb3J0IGF1dG9jb21wbGV0ZSBmcm9tIFwiLi9hdXRvY29tcGxldGVcIjtcblxuLy8gU2tlbGV0b24gYmFzZSBkYXRhIHBlciBmaWVsZCBkZWZpbml0aW9uXG5jb25zdCBpbml0aWFsRGF0YSA9IHtcblx0bmFtZXM6IFtdLFxuXHRtdWx0aXNlbGVjdDogW10sXG5cdGxpbmtzOiBbXSxcblx0a2V5d29yZDogW10sXG5cdFwibGlzdC1vZi1zdHJpbmdzXCI6IFtdLFxuXHRhbHRuYW1lczogW10sXG5cdHRleHQ6IFwiXCIsXG5cdHN0cmluZzogXCJcIixcblx0c2VsZWN0OiBcIlwiLFxuXHRkYXRhYmxlOiBcIlwiXG59O1xuXG4vLyBSZXR1cm4gdGhlIGluaXRpYWwgZGF0YSBmb3IgdGhlIHR5cGUgaW4gdGhlIGZpZWxkIGRlZmluaXRpb25cbmNvbnN0IGluaXRpYWxEYXRhRm9yVHlwZSA9IChmaWVsZERlZikgPT5cblx0ZmllbGREZWYuZGVmYXVsdFZhbHVlIHx8IChmaWVsZERlZi50eXBlID09PSBcInJlbGF0aW9uXCIgfHwgZmllbGREZWYudHlwZSA9PT0gXCJrZXl3b3JkXCIgPyB7fSA6IGluaXRpYWxEYXRhW2ZpZWxkRGVmLnR5cGVdKTtcblxuLy8gUmV0dXJuIHRoZSBpbml0aWFsIG5hbWUta2V5IGZvciBhIGNlcnRhaW4gZmllbGQgdHlwZVxuY29uc3QgbmFtZUZvclR5cGUgPSAoZmllbGREZWYpID0+XG5cdGZpZWxkRGVmLnR5cGUgPT09IFwicmVsYXRpb25cIiB8fCBmaWVsZERlZi50eXBlID09PSBcImtleXdvcmRcIiA/IFwiQHJlbGF0aW9uc1wiIDogZmllbGREZWYubmFtZTtcblxuXG4vLyBDcmVhdGUgYSBuZXcgZW1wdHkgZW50aXR5IGJhc2VkIG9uIHRoZSBmaWVsZERlZmluaXRpb25zXG5jb25zdCBtYWtlU2tlbGV0b24gPSBmdW5jdGlvbiAodnJlLCBkb21haW4pIHtcblx0aWYgKHZyZSAmJiB2cmUuY29sbGVjdGlvbnMgJiYgdnJlLmNvbGxlY3Rpb25zW2RvbWFpbl0gJiYgdnJlLmNvbGxlY3Rpb25zW2RvbWFpbl0ucHJvcGVydGllcykge1xuXHRcdHJldHVybiB2cmUuY29sbGVjdGlvbnNbZG9tYWluXS5wcm9wZXJ0aWVzXG5cdFx0XHQubWFwKChmaWVsZERlZikgPT4gW25hbWVGb3JUeXBlKGZpZWxkRGVmKSwgaW5pdGlhbERhdGFGb3JUeXBlKGZpZWxkRGVmKV0pXG5cdFx0XHQuY29uY2F0KFtbXCJAdHlwZVwiLCBkb21haW4ucmVwbGFjZSgvcyQvLCBcIlwiKV1dKVxuXHRcdFx0LnJlZHVjZSgob2JqLCBjdXIpID0+IHtcblx0XHRcdFx0b2JqW2N1clswXV0gPSBjdXJbMV07XG5cdFx0XHRcdHJldHVybiBvYmo7XG5cdFx0XHR9LCB7fSk7XG5cdH1cbn07XG5cbmNvbnN0IGZldGNoRW50aXR5TGlzdCA9IChkb21haW4pID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IDB9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZG9tYWluLCAwLCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGF9KSk7XG59O1xuXG5jb25zdCBwYWdpbmF0ZUxlZnQgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IG5ld1N0YXJ0ID0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5zdGFydCAtIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cztcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3QgcGFnaW5hdGVSaWdodCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgbmV3U3RhcnQgPSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnN0YXJ0ICsgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogbmV3U3RhcnR9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3Qgc2VuZFF1aWNrU2VhcmNoID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSB9ID0gZ2V0U3RhdGUoKTtcblx0aWYgKHF1aWNrU2VhcmNoLnF1ZXJ5Lmxlbmd0aCkge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiAwfSk7XG5cdFx0Y29uc3QgY2FsbGJhY2sgPSAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhLm1hcCgoZCkgPT4gKFxuXHRcdFx0e1xuXHRcdFx0XHRfaWQ6IGQua2V5LnJlcGxhY2UoLy4qXFwvLywgXCJcIiksXG5cdFx0XHRcdFwiQGRpc3BsYXlOYW1lXCI6IGQudmFsdWVcblx0XHRcdH1cblx0XHQpKX0pO1xuXHRcdGF1dG9jb21wbGV0ZShgZG9tYWluLyR7ZW50aXR5LmRvbWFpbn0vYXV0b2NvbXBsZXRlYCwgcXVpY2tTZWFyY2gucXVlcnksIGNhbGxiYWNrKTtcblx0fSBlbHNlIHtcblx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZW50aXR5LmRvbWFpbikpO1xuXHR9XG59O1xuXG5jb25zdCBzZWxlY3REb21haW4gPSAoZG9tYWluKSA9PiAoZGlzcGF0Y2gpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RPTUFJTlwiLCBkb21haW59KTtcblx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGRvbWFpbikpO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIiwgdmFsdWU6IFwiXCJ9KTtcbn07XG5cbi8vIDEpIEZldGNoIGVudGl0eVxuLy8gMikgRGlzcGF0Y2ggUkVDRUlWRV9FTlRJVFkgZm9yIHJlbmRlclxuY29uc3Qgc2VsZWN0RW50aXR5ID0gKGRvbWFpbiwgZW50aXR5SWQsIGVycm9yTWVzc2FnZSA9IG51bGwsIHN1Y2Nlc3NNZXNzYWdlID0gbnVsbCwgbmV4dCA9ICgpID0+IHsgfSkgPT5cblx0KGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRcdGNvbnN0IHsgZW50aXR5OiB7IGRvbWFpbjogY3VycmVudERvbWFpbiB9IH0gPSBnZXRTdGF0ZSgpO1xuXHRcdGlmIChjdXJyZW50RG9tYWluICE9PSBkb21haW4pIHtcblx0XHRcdGRpc3BhdGNoKHNlbGVjdERvbWFpbihkb21haW4pKTtcblx0XHR9XG5cdFx0Y3J1ZC5mZXRjaEVudGl0eShgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke2VudGl0eUlkfWAsIChkYXRhKSA9PiB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLCBkb21haW46IGRvbWFpbiwgZGF0YTogZGF0YSwgZXJyb3JNZXNzYWdlOiBlcnJvck1lc3NhZ2V9KTtcblx0XHRcdGlmIChzdWNjZXNzTWVzc2FnZSAhPT0gbnVsbCkge1xuXHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTVUNDRVNTX01FU1NBR0VcIiwgbWVzc2FnZTogc3VjY2Vzc01lc3NhZ2V9KTtcblx0XHRcdH1cblx0XHR9LCAoKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9GQUlMVVJFXCIsIGVycm9yTWVzc2FnZTogYEZhaWxlZCB0byBmZXRjaCAke2RvbWFpbn0gd2l0aCBJRCAke2VudGl0eUlkfWB9KSk7XG5cdFx0bmV4dCgpO1xuXHR9O1xuXG5cbi8vIDEpIERpc3BhdGNoIFJFQ0VJVkVfRU5USVRZIHdpdGggZW1wdHkgZW50aXR5IHNrZWxldG9uIGZvciByZW5kZXJcbmNvbnN0IG1ha2VOZXdFbnRpdHkgPSAoZG9tYWluLCBlcnJvck1lc3NhZ2UgPSBudWxsKSA9PlxuXHQoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiBkaXNwYXRjaCh7XG5cdFx0dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLFxuXHRcdGRvbWFpbjogZG9tYWluLFxuXHRcdGRhdGE6IG1ha2VTa2VsZXRvbihnZXRTdGF0ZSgpLnZyZSwgZG9tYWluKSB8fCB7fSxcblx0XHRlcnJvck1lc3NhZ2U6IGVycm9yTWVzc2FnZVxuXHR9KTtcblxuY29uc3QgZGVsZXRlRW50aXR5ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjcnVkLmRlbGV0ZUVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLFxuXHRcdCgpID0+IHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNVQ0NFU1NfTUVTU0FHRVwiLCBtZXNzYWdlOiBgU3VjZXNzZnVsbHkgZGVsZXRlZCAke2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbn0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWB9KTtcblx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSk7XG5cdFx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSk7XG5cdFx0fSxcblx0XHQoKSA9PiBkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgYEZhaWxlZCB0byBkZWxldGUgJHtnZXRTdGF0ZSgpLmVudGl0eS5kb21haW59IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gKSkpO1xufTtcblxuLy8gMSkgU2F2ZSBhbiBlbnRpdHlcbi8vIDIpIFNhdmUgdGhlIHJlbGF0aW9ucyBmb3IgdGhpcyBlbnRpdHlcbi8vIDMpIFJlZmV0Y2ggZW50aXR5IGZvciByZW5kZXJcbmNvbnN0IHNhdmVFbnRpdHkgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IGNvbGxlY3Rpb25MYWJlbCA9IGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0uY29sbGVjdGlvbkxhYmVsLnJlcGxhY2UoL3MkLywgXCJcIik7XG5cblx0Ly8gTWFrZSBhIGRlZXAgY29weSBvZiB0aGUgZGF0YSB0byBiZSBzYXZlZCBpbiBvcmRlciB0byBsZWF2ZSBhcHBsaWNhdGlvbiBzdGF0ZSB1bmFsdGVyZWRcblx0bGV0IHNhdmVEYXRhID0gY2xvbmUoZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YSk7XG5cdC8vIE1ha2UgYSBkZWVwIGNvcHkgb2YgdGhlIHJlbGF0aW9uIGRhdGEgaW4gb3JkZXIgdG8gbGVhdmUgYXBwbGljYXRpb24gc3RhdGUgdW5hbHRlcmVkXG5cdGxldCByZWxhdGlvbkRhdGEgPSBjbG9uZShzYXZlRGF0YVtcIkByZWxhdGlvbnNcIl0pIHx8IHt9O1xuXHQvLyBEZWxldGUgdGhlIHJlbGF0aW9uIGRhdGEgZnJvbSB0aGUgc2F2ZURhdGEgYXMgaXQgaXMgbm90IGV4cGVjdGVkIGJ5IHRoZSBzZXJ2ZXJcblx0ZGVsZXRlIHNhdmVEYXRhW1wiQHJlbGF0aW9uc1wiXTtcblxuXHRpZiAoZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQpIHtcblx0XHQvLyAxKSBVcGRhdGUgdGhlIGVudGl0eSB3aXRoIHNhdmVEYXRhXG5cdFx0Y3J1ZC51cGRhdGVFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBzYXZlRGF0YSwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKGVyciwgcmVzcCkgPT5cblx0XHRcdC8vIDIpIFNhdmUgcmVsYXRpb25zIHVzaW5nIHNlcnZlciByZXNwb25zZSBmb3IgY3VycmVudCByZWxhdGlvbnMgdG8gZGlmZiBhZ2FpbnN0IHJlbGF0aW9uRGF0YVxuXHRcdFx0ZGlzcGF0Y2goKHJlZGlzcGF0Y2gpID0+IHNhdmVSZWxhdGlvbnMoSlNPTi5wYXJzZShyZXNwLmJvZHkpLCByZWxhdGlvbkRhdGEsIGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0ucHJvcGVydGllcywgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKCkgPT5cblx0XHRcdFx0Ly8gMykgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuXHRcdFx0XHRyZWRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBudWxsLCBgU3VjY2VzZnVsbHkgc2F2ZWQgJHtjb2xsZWN0aW9uTGFiZWx9IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gLCAoKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSkpKSkpLCAoKSA9PlxuXHRcdFx0XHRcdC8vIDJhKSBIYW5kbGUgZXJyb3IgYnkgcmVmZXRjaGluZyBhbmQgcGFzc2luZyBhbG9uZyBhbiBlcnJvciBtZXNzYWdlXG5cdFx0XHRcdFx0ZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGBGYWlsZWQgdG8gc2F2ZSAke2NvbGxlY3Rpb25MYWJlbH0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWApKSk7XG5cblx0fSBlbHNlIHtcblx0XHQvLyAxKSBDcmVhdGUgbmV3IGVudGl0eSB3aXRoIHNhdmVEYXRhXG5cdFx0Y3J1ZC5zYXZlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgc2F2ZURhdGEsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsIChlcnIsIHJlc3ApID0+XG5cdFx0XHQvLyAyKSBGZXRjaCBlbnRpdHkgdmlhIGxvY2F0aW9uIGhlYWRlclxuXHRcdFx0ZGlzcGF0Y2goKHJlZGlzcGF0Y2gpID0+IGNydWQuZmV0Y2hFbnRpdHkocmVzcC5oZWFkZXJzLmxvY2F0aW9uLCAoZGF0YSkgPT5cblx0XHRcdFx0Ly8gMykgU2F2ZSByZWxhdGlvbnMgdXNpbmcgc2VydmVyIHJlc3BvbnNlIGZvciBjdXJyZW50IHJlbGF0aW9ucyB0byBkaWZmIGFnYWluc3QgcmVsYXRpb25EYXRhXG5cdFx0XHRcdHNhdmVSZWxhdGlvbnMoZGF0YSwgcmVsYXRpb25EYXRhLCBnZXRTdGF0ZSgpLnZyZS5jb2xsZWN0aW9uc1tnZXRTdGF0ZSgpLmVudGl0eS5kb21haW5dLnByb3BlcnRpZXMsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsICgpID0+XG5cdFx0XHRcdFx0Ly8gNCkgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuXHRcdFx0XHRcdHJlZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZGF0YS5faWQsIG51bGwsIGBTdWNjZXNmdWxseSBzYXZlZCAke2NvbGxlY3Rpb25MYWJlbH1gLCAoKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSkpKSkpKSwgKCkgPT5cblx0XHRcdFx0XHRcdC8vIDJhKSBIYW5kbGUgZXJyb3IgYnkgcmVmZXRjaGluZyBhbmQgcGFzc2luZyBhbG9uZyBhbiBlcnJvciBtZXNzYWdlXG5cdFx0XHRcdFx0XHRkaXNwYXRjaChtYWtlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgYEZhaWxlZCB0byBzYXZlIG5ldyAke2NvbGxlY3Rpb25MYWJlbH1gKSkpO1xuXHR9XG59O1xuXG5cbmV4cG9ydCB7IHNhdmVFbnRpdHksIHNlbGVjdEVudGl0eSwgbWFrZU5ld0VudGl0eSwgZGVsZXRlRW50aXR5LCBmZXRjaEVudGl0eUxpc3QsIHBhZ2luYXRlUmlnaHQsIHBhZ2luYXRlTGVmdCwgc2VuZFF1aWNrU2VhcmNoLCBzZWxlY3REb21haW4gfTsiLCJpbXBvcnQgeyBzYXZlRW50aXR5LCBzZWxlY3RFbnRpdHksIG1ha2VOZXdFbnRpdHksIGRlbGV0ZUVudGl0eSwgc2VsZWN0RG9tYWluLCBwYWdpbmF0ZUxlZnQsIHBhZ2luYXRlUmlnaHQsIHNlbmRRdWlja1NlYXJjaCB9IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IHsgc2V0VnJlIH0gZnJvbSBcIi4vdnJlXCI7XG5cbmV4cG9ydCBkZWZhdWx0IChuYXZpZ2F0ZVRvLCBkaXNwYXRjaCkgPT4gKHtcblx0b25OZXc6IChkb21haW4pID0+IGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZG9tYWluKSksXG5cdG9uU2VsZWN0OiAocmVjb3JkKSA9PiBkaXNwYXRjaChzZWxlY3RFbnRpdHkocmVjb3JkLmRvbWFpbiwgcmVjb3JkLmlkKSksXG5cdG9uU2F2ZTogKCkgPT4gZGlzcGF0Y2goc2F2ZUVudGl0eSgpKSxcblx0b25EZWxldGU6ICgpID0+IGRpc3BhdGNoKGRlbGV0ZUVudGl0eSgpKSxcblx0b25DaGFuZ2U6IChmaWVsZFBhdGgsIHZhbHVlKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCIsIGZpZWxkUGF0aDogZmllbGRQYXRoLCB2YWx1ZTogdmFsdWV9KSxcblx0b25Mb2dpbkNoYW5nZTogKHJlc3BvbnNlKSA9PiBkaXNwYXRjaChzZXRVc2VyKHJlc3BvbnNlKSksXG5cdG9uU2VsZWN0VnJlOiAodnJlSWQpID0+IGRpc3BhdGNoKHNldFZyZSh2cmVJZCkpLFxuXHRvbkRpc21pc3NNZXNzYWdlOiAobWVzc2FnZUluZGV4KSA9PiBkaXNwYXRjaCh7dHlwZTogXCJESVNNSVNTX01FU1NBR0VcIiwgbWVzc2FnZUluZGV4OiBtZXNzYWdlSW5kZXh9KSxcblx0b25TZWxlY3REb21haW46IChkb21haW4pID0+IHtcblx0XHRkaXNwYXRjaChzZWxlY3REb21haW4oZG9tYWluKSk7XG5cdH0sXG5cdG9uUGFnaW5hdGVMZWZ0OiAoKSA9PiBkaXNwYXRjaChwYWdpbmF0ZUxlZnQoKSksXG5cdG9uUGFnaW5hdGVSaWdodDogKCkgPT4gZGlzcGF0Y2gocGFnaW5hdGVSaWdodCgpKSxcblx0b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlOiAodmFsdWUpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9RVUlDS1NFQVJDSF9RVUVSWVwiLCB2YWx1ZTogdmFsdWV9KSxcblx0b25RdWlja1NlYXJjaDogKCkgPT4gZGlzcGF0Y2goc2VuZFF1aWNrU2VhcmNoKCkpXG59KTsiLCJpbXBvcnQgeyBzYXZlTmV3RW50aXR5LCB1cGRhdGVFbnRpdHkgfSBmcm9tIFwiLi9jcnVkXCI7XG5cbmNvbnN0IHNhdmVSZWxhdGlvbnNWMjEgPSAoZGF0YSwgcmVsYXRpb25EYXRhLCBmaWVsZERlZnMsIHRva2VuLCB2cmVJZCwgbmV4dCkgPT4ge1xuXHQvLyBSZXR1cm5zIHRoZSBkb21haW4gYmFzZWQgb24gdGhlIGZpZWxkRGVmaW5pdGlvbnMgYW5kIHRoZSByZWxhdGlvbiBrZXkgKGkuZS4gXCJoYXNCaXJ0aFBsYWNlXCIpXG5cdGNvbnN0IG1ha2VSZWxhdGlvbkFyZ3MgPSAocmVsYXRpb24sIGtleSwgYWNjZXB0ZWQgPSB0cnVlLCBpZCA9IG51bGwsIHJldiA9IG51bGwpID0+IHtcblx0XHRjb25zdCBmaWVsZERlZiA9IGZpZWxkRGVmcy5maW5kKChkZWYpID0+IGRlZi5uYW1lID09PSBrZXkpO1xuXG5cblx0XHRjb25zdCBzb3VyY2VUeXBlID0gZGF0YVtcIkB0eXBlXCJdLnJlcGxhY2UoL3MkLywgXCJcIikucmVwbGFjZSgvXnd3LywgXCJcIik7XG5cdFx0Y29uc3QgdGFyZ2V0VHlwZSA9IGZpZWxkRGVmLnJlbGF0aW9uLnRhcmdldENvbGxlY3Rpb24ucmVwbGFjZSgvcyQvLCBcIlwiKS5yZXBsYWNlKC9ed3cvLCBcIlwiKTtcblxuXHRcdGNvbnN0IHJlbGF0aW9uU2F2ZURhdGEgPSB7XG5cdFx0XHRcIkB0eXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uQ29sbGVjdGlvbi5yZXBsYWNlKC9zJC8sIFwiXCIpLCAvLyBjaGVja1xuXHRcdFx0XCJec291cmNlSWRcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyByZWxhdGlvbi5pZCA6IGRhdGEuX2lkLCAvLyBjaGVja1xuXHRcdFx0XCJec291cmNlVHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHRhcmdldFR5cGUgOiBzb3VyY2VUeXBlLCAvLyBjaGVja1xuXHRcdFx0XCJedGFyZ2V0SWRcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyBkYXRhLl9pZCA6IHJlbGF0aW9uLmlkLCAvLyBjaGVja1xuXHRcdFx0XCJedGFyZ2V0VHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHNvdXJjZVR5cGUgOiB0YXJnZXRUeXBlLFxuXHRcdFx0XCJedHlwZUlkXCI6IGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uVHlwZUlkLCAvLyBjaGVja1xuXHRcdFx0YWNjZXB0ZWQ6IGFjY2VwdGVkXG5cdFx0fTtcblxuXHRcdGlmKGlkKSB7IHJlbGF0aW9uU2F2ZURhdGEuX2lkID0gaWQ7IH1cblx0XHRpZihyZXYpIHsgcmVsYXRpb25TYXZlRGF0YVtcIl5yZXZcIl0gPSByZXY7IH1cblx0XHRyZXR1cm4gW1xuXHRcdFx0ZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25Db2xsZWN0aW9uLCAvLyBkb21haW5cblx0XHRcdHJlbGF0aW9uU2F2ZURhdGFcblx0XHRdO1xuXHR9O1xuXG5cdC8vIENvbnN0cnVjdHMgYW4gYXJyYXkgb2YgYXJndW1lbnRzIGZvciBzYXZpbmcgbmV3IHJlbGF0aW9uczpcblx0Ly8gW1xuXHQvLyAgIFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dLFxuXHQvLyAgIFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dLFxuXHQvLyBdXG5cdGNvbnN0IG5ld1JlbGF0aW9ucyA9IE9iamVjdC5rZXlzKHJlbGF0aW9uRGF0YSkubWFwKChrZXkpID0+XG5cdFx0XHRyZWxhdGlvbkRhdGFba2V5XVxuXHRcdFx0Ly8gRmlsdGVycyBvdXQgYWxsIHJlbGF0aW9ucyB3aGljaCBhcmUgbm90IGFscmVhZHkgaW4gZGF0YVtcIkByZWxhdGlvbnNcIl1cblx0XHRcdFx0LmZpbHRlcigocmVsYXRpb24pID0+IChkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldIHx8IFtdKS5tYXAoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmlkKS5pbmRleE9mKHJlbGF0aW9uLmlkKSA8IDApXG5cdFx0XHRcdC8vIE1ha2UgYXJndW1lbnQgYXJyYXkgZm9yIG5ldyByZWxhdGlvbnM6IFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dXG5cdFx0XHRcdC5tYXAoKHJlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKHJlbGF0aW9uLCBrZXkpKVxuXHRcdC8vIEZsYXR0ZW4gbmVzdGVkIGFycmF5c1xuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXG5cdC8vIFJlYWN0aXZhdGUgcHJldmlvdXNseSBhZGRlZCByZWxhdGlvbnMgdXNpbmcgUFVUIHdoaWNoIHdlcmUgJ2RlbGV0ZWQnIGFmdGVyIHVzaW5nIFBVVFxuXHRjb25zdCByZUFkZFJlbGF0aW9ucyA9IE9iamVjdC5rZXlzKHJlbGF0aW9uRGF0YSkubWFwKChrZXkpID0+XG5cdFx0KGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV0gfHwgW10pXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5hY2NlcHRlZCA9PT0gZmFsc2UpXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IChyZWxhdGlvbkRhdGFba2V5XSB8fCBbXSkuZmlsdGVyKChyZWxhdGlvbikgPT4gcmVsYXRpb24uYWNjZXB0ZWQpLm1hcCgocmVsYXRpb24pID0+IHJlbGF0aW9uLmlkKS5pbmRleE9mKG9yaWdSZWxhdGlvbi5pZCkgPiAtMSlcblx0XHRcdC5tYXAoKG9yaWdSZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhvcmlnUmVsYXRpb24sIGtleSwgdHJ1ZSwgb3JpZ1JlbGF0aW9uLnJlbGF0aW9uSWQsIG9yaWdSZWxhdGlvbi5yZXYpKVxuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXHQvLyBEZWFjdGl2YXRlIHByZXZpb3VzbHkgYWRkZWQgcmVsYXRpb25zIHVzaW5nIFBVVFxuXHRjb25zdCBkZWxldGVSZWxhdGlvbnMgPSBPYmplY3Qua2V5cyhkYXRhW1wiQHJlbGF0aW9uc1wiXSkubWFwKChrZXkpID0+XG5cdFx0ZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uYWNjZXB0ZWQpXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IChyZWxhdGlvbkRhdGFba2V5XSB8fCBbXSkubWFwKChyZWxhdGlvbikgPT4gcmVsYXRpb24uaWQpLmluZGV4T2Yob3JpZ1JlbGF0aW9uLmlkKSA8IDApXG5cdFx0XHQubWFwKChvcmlnUmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3Mob3JpZ1JlbGF0aW9uLCBrZXksIGZhbHNlLCBvcmlnUmVsYXRpb24ucmVsYXRpb25JZCwgb3JpZ1JlbGF0aW9uLnJldikpXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cdC8vIENvbWJpbmVzIHNhdmVOZXdFbnRpdHkgYW5kIGRlbGV0ZUVudGl0eSBpbnN0cnVjdGlvbnMgaW50byBwcm9taXNlc1xuXHRjb25zdCBwcm9taXNlcyA9IG5ld1JlbGF0aW9uc1xuXHQvLyBNYXAgbmV3UmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHNhdmVOZXdFbnRpdHlcblx0XHQubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiBzYXZlTmV3RW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSApKVxuXHRcdC8vIE1hcCByZWFkZFJlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiB1cGRhdGVFbnRpdHlcblx0XHQuY29uY2F0KHJlQWRkUmVsYXRpb25zLm1hcCgoYXJncykgPT4gbmV3IFByb21pc2UoKHJlc29sdmUsIHJlamVjdCkgPT4gdXBkYXRlRW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSkpKVxuXHRcdC8vIE1hcCBkZWxldGVSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2YgdXBkYXRlRW50aXR5XG5cdFx0LmNvbmNhdChkZWxldGVSZWxhdGlvbnMubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiB1cGRhdGVFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpKSkpO1xuXG5cdC8vIEludm9rZSBhbGwgQ1JVRCBvcGVyYXRpb25zIGZvciB0aGUgcmVsYXRpb25zXG5cdFByb21pc2UuYWxsKHByb21pc2VzKS50aGVuKG5leHQsIG5leHQpO1xufTtcblxuZXhwb3J0IGRlZmF1bHQgc2F2ZVJlbGF0aW9uc1YyMTsiLCJpbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi4vc3RvcmVcIjtcblxuZXhwb3J0IGRlZmF1bHQge1xuXHRwZXJmb3JtWGhyOiBmdW5jdGlvbiAob3B0aW9ucywgYWNjZXB0LCByZWplY3QgPSAoKSA9PiB7IGNvbnNvbGUud2FybihcIlVuZGVmaW5lZCByZWplY3QgY2FsbGJhY2shIFwiKTsgfSwgb3BlcmF0aW9uID0gXCJTZXJ2ZXIgcmVxdWVzdFwiKSB7XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiUkVRVUVTVF9NRVNTQUdFXCIsIG1lc3NhZ2U6IGAke29wZXJhdGlvbn06ICR7b3B0aW9ucy5tZXRob2QgfHwgXCJHRVRcIn0gJHtvcHRpb25zLnVybH1gfSk7XG5cdFx0eGhyKG9wdGlvbnMsIChlcnIsIHJlc3AsIGJvZHkpID0+IHtcblx0XHRcdGlmKHJlc3Auc3RhdHVzQ29kZSA+PSA0MDApIHtcblx0XHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiRVJST1JfTUVTU0FHRVwiLCBtZXNzYWdlOiBgJHtvcGVyYXRpb259IGZhaWxlZCB3aXRoIGNhdXNlOiAke3Jlc3AuYm9keX1gfSk7XG5cdFx0XHRcdHJlamVjdChlcnIsIHJlc3AsIGJvZHkpO1xuXHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0YWNjZXB0KGVyciwgcmVzcCwgYm9keSk7XG5cdFx0XHR9XG5cdFx0fSk7XG5cdH0sXG5cblx0ZmFzdFhocjogZnVuY3Rpb24ob3B0aW9ucywgYWNjZXB0KSB7XG5cdFx0eGhyKG9wdGlvbnMsIGFjY2VwdCk7XG5cdH0sXG5cblx0bWFrZUhlYWRlcnM6IGZ1bmN0aW9uKHRva2VuLCB2cmVJZCkge1xuXHRcdHJldHVybiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIixcblx0XHRcdFwiQ29udGVudC10eXBlXCI6IFwiYXBwbGljYXRpb24vanNvblwiLFxuXHRcdFx0XCJBdXRob3JpemF0aW9uXCI6IHRva2VuLFxuXHRcdFx0XCJWUkVfSURcIjogdnJlSWRcblx0XHR9O1xuXHR9XG59O1xuIiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2luZGV4XCI7XG5pbXBvcnQge21ha2VOZXdFbnRpdHl9IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IHtmZXRjaEVudGl0eUxpc3R9IGZyb20gXCIuL2VudGl0eVwiO1xuXG5jb25zdCBsaXN0VnJlcyA9ICgpID0+IChkaXNwYXRjaCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIlxuXHRcdH0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvc3lzdGVtL3ZyZXNgXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRkaXNwYXRjaCh7dHlwZTogXCJMSVNUX1ZSRVNcIiwgbGlzdDogSlNPTi5wYXJzZShyZXNwLmJvZHkpfSk7XG5cdH0sIG51bGwsIFwiTGlzdCBWUkVzXCIpO1xuXG5jb25zdCBzZXRWcmUgPSAodnJlSWQsIG5leHQgPSAoKSA9PiB7IH0pID0+IChkaXNwYXRjaCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIlxuXHRcdH0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvbWV0YWRhdGEvJHt2cmVJZH0/d2l0aENvbGxlY3Rpb25JbmZvPXRydWVgXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRpZiAocmVzcC5zdGF0dXNDb2RlID09PSAyMDApIHtcblx0XHRcdHZhciBib2R5ID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1ZSRVwiLCB2cmVJZDogdnJlSWQsIGNvbGxlY3Rpb25zOiBib2R5fSk7XG5cblx0XHRcdGxldCBkZWZhdWx0RG9tYWluID0gT2JqZWN0LmtleXMoYm9keSlcblx0XHRcdFx0Lm1hcChjb2xsZWN0aW9uTmFtZSA9PiBib2R5W2NvbGxlY3Rpb25OYW1lXSlcblx0XHRcdFx0LmZpbHRlcihjb2xsZWN0aW9uID0+ICFjb2xsZWN0aW9uLnVua25vd24gJiYgIWNvbGxlY3Rpb24ucmVsYXRpb25Db2xsZWN0aW9uKVswXVxuXHRcdFx0XHQuY29sbGVjdGlvbk5hbWU7XG5cblx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZGVmYXVsdERvbWFpbikpXG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRE9NQUlOXCIsIGRlZmF1bHREb21haW59KTtcblx0XHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChkZWZhdWx0RG9tYWluKSk7XG5cdFx0XHRuZXh0KCk7XG5cdFx0fVxuXHR9LCAoKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfVlJFXCIsIHZyZUlkOiB2cmVJZCwgY29sbGVjdGlvbnM6IHt9fSksIGBGZXRjaCBWUkUgZGVzY3JpcHRpb24gZm9yICR7dnJlSWR9YCk7XG5cblxuZXhwb3J0IHtsaXN0VnJlcywgc2V0VnJlfTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjbGFzc25hbWVzIGZyb20gXCJjbGFzc25hbWVzXCI7XG5pbXBvcnQge3VybHN9IGZyb20gXCIuLi8uLi91cmxzXCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuXG5jbGFzcyBDb2xsZWN0aW9uVGFicyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0b25Eb21haW5TZWxlY3QoZG9tYWluKSB7XG5cdFx0dGhpcy5wcm9wcy5vbk5ldyhkb21haW4pO1xuXHRcdHRoaXMucHJvcHMub25TZWxlY3REb21haW4oZG9tYWluKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGNvbGxlY3Rpb25zLCBhY3RpdmVEb21haW4gfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgZG9tYWlucyA9IE9iamVjdC5rZXlzKGNvbGxlY3Rpb25zIHx8IHt9KTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXYtdGFic1wiPlxuICAgICAgICAgIHtkb21haW5zXG4gICAgICAgICAgICAuZmlsdGVyKGQgPT4gIShjb2xsZWN0aW9uc1tkXS51bmtub3duIHx8IGNvbGxlY3Rpb25zW2RdLnJlbGF0aW9uQ29sbGVjdGlvbikpXG4gICAgICAgICAgICAubWFwKChkb21haW4pID0+IChcbiAgICAgICAgICAgICAgPGxpIGNsYXNzTmFtZT17Y2xhc3NuYW1lcyh7YWN0aXZlOiBkb21haW4gPT09IGFjdGl2ZURvbWFpbn0pfSBrZXk9e2RvbWFpbn0+XG4gICAgICAgICAgICAgICAgPExpbmsgdG89e3VybHMubmV3RW50aXR5KGRvbWFpbil9PlxuICAgICAgICAgICAgICAgICAge2NvbGxlY3Rpb25zW2RvbWFpbl0uY29sbGVjdGlvbkxhYmVsfVxuICAgICAgICAgICAgICAgIDwvTGluaz5cbiAgICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5Db2xsZWN0aW9uVGFicy5wcm9wVHlwZXMgPSB7XG5cdG9uTmV3OiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZWxlY3REb21haW46IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRjb2xsZWN0aW9uczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0YWN0aXZlRG9tYWluOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFicztcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBQYWdlIGZyb20gXCIuLi9wYWdlLmpzeFwiO1xuXG5pbXBvcnQgUGFnaW5hdGUgZnJvbSBcIi4vZW50aXR5LWluZGV4L3BhZ2luYXRlXCI7XG5pbXBvcnQgUXVpY2tTZWFyY2ggZnJvbSBcIi4vZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoXCI7XG5pbXBvcnQgRW50aXR5TGlzdCBmcm9tIFwiLi9lbnRpdHktaW5kZXgvbGlzdFwiO1xuXG5pbXBvcnQgU2F2ZUZvb3RlciBmcm9tIFwiLi9lbnRpdHktZm9ybS9zYXZlLWZvb3RlclwiO1xuaW1wb3J0IEVudGl0eUZvcm0gZnJvbSBcIi4vZW50aXR5LWZvcm0vZm9ybVwiO1xuXG5pbXBvcnQgQ29sbGVjdGlvblRhYnMgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJzXCI7XG5pbXBvcnQgTWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXMvbGlzdFwiO1xuXG5jbGFzcyBFZGl0R3VpIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGNvbnN0IHsgb25TZWxlY3QsIG9uTmV3LCBvblNlbGVjdERvbWFpbiB9ID0gdGhpcy5wcm9wcztcblxuXHRcdC8vIFRyaWdnZXJzIGZldGNoIGRhdGEgZnJvbSBzZXJ2ZXIgYmFzZWQgb24gaWQgZnJvbSByb3V0ZS5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQgIT09IG5leHRQcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdG9uU2VsZWN0KHtkb21haW46IG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IG5leHRQcm9wcy5wYXJhbXMuaWR9KTtcblx0XHR9IGVsc2UgaWYgKHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24gIT09IG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbikge1xuXHRcdFx0b25OZXcobmV4dFByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKTtcblx0XHRcdG9uU2VsZWN0RG9tYWluKG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbik7XG5cdFx0fVxuXHR9XG5cblx0Y29tcG9uZW50RGlkTW91bnQoKSB7XG5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdHRoaXMucHJvcHMub25TZWxlY3Qoe2RvbWFpbjogdGhpcy5wcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IHRoaXMucHJvcHMucGFyYW1zLmlkfSk7XG5cdFx0fSBlbHNlIGlmICh0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKSB7XG5cdFx0XHR0aGlzLnByb3BzLm9uTmV3KHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pO1xuXHRcdFx0dGhpcy5wcm9wcy5vblNlbGVjdERvbWFpbih0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKTtcblx0XHR9XG5cblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG9uU2VsZWN0LCBvbk5ldywgb25TYXZlLCBvbkRlbGV0ZSwgb25TZWxlY3REb21haW4sIG9uRGlzbWlzc01lc3NhZ2UsIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlLCBvblF1aWNrU2VhcmNoLCBvblBhZ2luYXRlTGVmdCwgb25QYWdpbmF0ZVJpZ2h0IH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgcXVpY2tTZWFyY2gsIGVudGl0eSwgdnJlLCBtZXNzYWdlcyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBjdXJyZW50TW9kZSA9IGVudGl0eS5kb21haW4gJiYgZW50aXR5LmRhdGEuX2lkID8gXCJlZGl0XCIgOiBcIm5ld1wiO1xuXG5cdFx0aWYgKGVudGl0eS5kb21haW4gPT09IG51bGwgfHwgIXZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXSkgeyByZXR1cm4gbnVsbDsgfVxuXHRcdHJldHVybiAoXG5cdFx0XHQ8UGFnZT5cblx0XHRcdFx0PENvbGxlY3Rpb25UYWJzIGNvbGxlY3Rpb25zPXt2cmUuY29sbGVjdGlvbnN9IG9uTmV3PXtvbk5ld30gb25TZWxlY3REb21haW49e29uU2VsZWN0RG9tYWlufVxuXHRcdFx0XHRcdGFjdGl2ZURvbWFpbj17ZW50aXR5LmRvbWFpbn0gLz5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cblx0XHRcdFx0XHQ8TWVzc2FnZXNcblx0XHRcdFx0XHRcdHR5cGVzPXtbXCJTVUNDRVNTX01FU1NBR0VcIiwgXCJFUlJPUl9NRVNTQUdFXCJdfVxuXHRcdFx0XHRcdFx0bWVzc2FnZXM9e21lc3NhZ2VzfVxuXHRcdFx0XHRcdFx0b25EaXNtaXNzTWVzc2FnZT17b25EaXNtaXNzTWVzc2FnZX0gLz5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPlxuXHRcdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtNFwiPlxuXHRcdFx0XHRcdFx0XHQ8UXVpY2tTZWFyY2hcblx0XHRcdFx0XHRcdFx0XHRvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2U9e29uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZX1cblx0XHRcdFx0XHRcdFx0XHRvblF1aWNrU2VhcmNoPXtvblF1aWNrU2VhcmNofVxuXHRcdFx0XHRcdFx0XHRcdHF1ZXJ5PXtxdWlja1NlYXJjaC5xdWVyeX0gLz5cblx0XHRcdFx0XHRcdFx0PEVudGl0eUxpc3Rcblx0XHRcdFx0XHRcdFx0XHRzdGFydD17cXVpY2tTZWFyY2guc3RhcnR9XG5cdFx0XHRcdFx0XHRcdFx0bGlzdD17cXVpY2tTZWFyY2gubGlzdH1cblx0XHRcdFx0XHRcdFx0XHRvblNlbGVjdD17b25TZWxlY3R9XG5cdFx0XHRcdFx0XHRcdFx0ZG9tYWluPXtlbnRpdHkuZG9tYWlufSAvPlxuXHRcdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0XHR7IGVudGl0eS5kb21haW4gPyAoXG5cdFx0XHRcdFx0XHRcdDxFbnRpdHlGb3JtIGN1cnJlbnRNb2RlPXtjdXJyZW50TW9kZX0gZ2V0QXV0b2NvbXBsZXRlVmFsdWVzPXtnZXRBdXRvY29tcGxldGVWYWx1ZXN9XG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5PXtlbnRpdHl9IG9uTmV3PXtvbk5ld30gb25EZWxldGU9e29uRGVsZXRlfSBvbkNoYW5nZT17b25DaGFuZ2V9XG5cdFx0XHRcdFx0XHRcdFx0cHJvcGVydGllcz17dnJlLmNvbGxlY3Rpb25zW2VudGl0eS5kb21haW5dLnByb3BlcnRpZXN9IFxuXHRcdFx0XHRcdFx0XHRcdGVudGl0eUxhYmVsPXt2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0uY29sbGVjdGlvbkxhYmVsLnJlcGxhY2UoL3MkLywgXCJcIikgfSAvPlxuXHRcdFx0XHRcdFx0KSA6IG51bGwgfVxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8L2Rpdj5cblxuXHRcdFx0XHQ8ZGl2IHR5cGU9XCJmb290ZXItYm9keVwiPlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLTRcIj5cblx0XHRcdFx0XHRcdDxQYWdpbmF0ZVxuXHRcdFx0XHRcdFx0XHRzdGFydD17cXVpY2tTZWFyY2guc3RhcnR9XG5cdFx0XHRcdFx0XHRcdGxpc3RMZW5ndGg9e3F1aWNrU2VhcmNoLmxpc3QubGVuZ3RofVxuXHRcdFx0XHRcdFx0XHRyb3dzPXs1MH1cblx0XHRcdFx0XHRcdFx0b25QYWdpbmF0ZUxlZnQ9e29uUGFnaW5hdGVMZWZ0fVxuXHRcdFx0XHRcdFx0XHRvblBhZ2luYXRlUmlnaHQ9e29uUGFnaW5hdGVSaWdodH0gLz5cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC04XCI+XG5cdFx0XHRcdFx0XHQ8U2F2ZUZvb3RlciBvblNhdmU9e29uU2F2ZX0gb25DYW5jZWw9eygpID0+IGN1cnJlbnRNb2RlID09PSBcImVkaXRcIiA/XG5cdFx0XHRcdFx0XHRcdG9uU2VsZWN0KHtkb21haW46IGVudGl0eS5kb21haW4sIGlkOiBlbnRpdHkuZGF0YS5faWR9KSA6IG9uTmV3KGVudGl0eS5kb21haW4pfSAvPlxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PGRpdiB0eXBlPVwiZm9vdGVyLWJvZHlcIj5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L1BhZ2U+XG5cdFx0KVxuXHR9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVkaXRHdWk7XG4iLCJleHBvcnQgZGVmYXVsdCAoY2FtZWxDYXNlKSA9PiBjYW1lbENhc2VcbiAgLnJlcGxhY2UoLyhbQS1aMC05XSkvZywgKG1hdGNoKSA9PiBgICR7bWF0Y2gudG9Mb3dlckNhc2UoKX1gKVxuICAucmVwbGFjZSgvXi4vLCAobWF0Y2gpID0+IG1hdGNoLnRvVXBwZXJDYXNlKCkpO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0geyBuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwiIH07XG5cdH1cblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGlmIChuZXh0UHJvcHMuZW50aXR5LmRhdGEuX2lkICE9PSB0aGlzLnByb3BzLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGlmICh0aGlzLnN0YXRlLm5ld0xhYmVsLmxlbmd0aCA+IDAgJiYgdGhpcy5zdGF0ZS5uZXdVcmwubGVuZ3RoID4gMCkge1xuXHRcdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh7XG5cdFx0XHRcdGxhYmVsOiB0aGlzLnN0YXRlLm5ld0xhYmVsLFxuXHRcdFx0XHR1cmw6IHRoaXMuc3RhdGUubmV3VXJsXG5cdFx0XHR9KSk7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwifSk7XG5cdFx0fVxuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXVxuXHRcdFx0LmZpbHRlcigodmFsKSA9PiB2YWwudXJsICE9PSB2YWx1ZS51cmwpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWUudXJsfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz5cblx0XHRcdFx0XHQ8YSBocmVmPXt2YWx1ZS51cmx9IHRhcmdldD1cIl9ibGFua1wiPlxuXHRcdFx0XHRcdFx0e3ZhbHVlLmxhYmVsfVxuXHRcdFx0XHRcdDwvYT5cblx0XHRcdFx0PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCJ9fT5cblx0XHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2wgcHVsbC1sZWZ0XCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3TGFiZWx9XG5cdFx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkxhYmVsIGZvciB1cmwuLi5cIlxuXHRcdFx0XHRcdFx0c3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXhXaWR0aDogXCI1MCVcIn19IC8+XG5cdFx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sIHB1bGwtbGVmdFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld1VybH1cblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld1VybDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gdGhpcy5vbkFkZCgpIDogZmFsc2V9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIlVybC4uLlwiXG5cdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIG1heFdpZHRoOiBcImNhbGMoNTAlIC0gODBweClcIn19IC8+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuIHB1bGwtbGVmdFwiPlxuXHRcdFx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9PkFkZCBsaW5rPC9idXR0b24+XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHQ8L2Rpdj5cblxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBjbGVhcjogXCJsZWZ0XCJ9fSAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdGNvbnN0cnVjdG9yKHByb3BzKSB7XG5cdFx0c3VwZXIocHJvcHMpO1xuXG5cdFx0dGhpcy5zdGF0ZSA9IHsgbmV3VmFsdWU6IFwiXCIgfTtcblx0fVxuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0aWYgKG5leHRQcm9wcy5lbnRpdHkuZGF0YS5faWQgIT09IHRoaXMucHJvcHMuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh2YWx1ZSkpO1xuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKHZhbCkgPT4gdmFsICE9PSB2YWx1ZSkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZX0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e3ZhbHVlfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdWYWx1ZX1cblx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0b25LZXlQcmVzcz17KGV2KSA9PiBldi5rZXkgPT09IFwiRW50ZXJcIiA/IHRoaXMub25BZGQoZXYudGFyZ2V0LnZhbHVlKSA6IGZhbHNlfVxuXHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiQWRkIGEgdmFsdWUuLi5cIiAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRvbkFkZCh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHZhbHVlKSk7XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigodmFsKSA9PiB2YWwgIT09IHZhbHVlKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57dmFsdWV9PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxTZWxlY3RGaWVsZCBvbkNoYW5nZT17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfSBub0NsZWFyPXt0cnVlfSBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG5cdFx0XHRcdFx0PHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG5cdFx0XHRcdFx0XHRTZWxlY3Qge2xhYmVsLnRvTG93ZXJDYXNlKCl9XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHRcdHtvcHRpb25zLmZpbHRlcigob3B0KSA9PiB2YWx1ZXMuaW5kZXhPZihvcHQpIDwgMCkubWFwKChvcHRpb24pID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9TZWxlY3RGaWVsZD5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5XG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBvbkFkZCgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQoe1xuICAgICAgY29tcG9uZW50czogW3t0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn1dXG4gICAgfSkpO1xuICB9XG5cbiAgb25BZGRDb21wb25lbnQoaXRlbUluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAuY29uY2F0KHt0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn0pXG4gICAgKTtcbiAgfVxuXG4gIG9uUmVtb3ZlQ29tcG9uZW50KGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5maWx0ZXIoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggIT09IGNvbXBvbmVudEluZGV4KVxuICAgICk7XG4gIH1cblxuICBvbkNoYW5nZUNvbXBvbmVudFZhbHVlKGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgsIHZhbHVlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHZhbHVlOiB2YWx1ZX0gOiBjb21wb25lbnRcbiAgICApKTtcbiAgfVxuXG4gIG9uQ2hhbmdlQ29tcG9uZW50VHlwZShpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4LCB0eXBlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHR5cGU6IHR5cGV9IDogY29tcG9uZW50XG4gICAgKSk7XG4gIH1cblxuICBvblJlbW92ZShpdGVtSW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKChuYW1lLCBpZHgpID0+IGlkeCAhPT0gaXRlbUluZGV4KSk7XG4gIH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXG4gICAgY29uc3QgbmFtZUVsZW1lbnRzID0gdmFsdWVzLm1hcCgobmFtZSwgaSkgPT4gKFxuICAgICAgPGRpdiBrZXk9e2Ake25hbWV9LSR7aX1gfSBjbGFzc05hbWU9XCJuYW1lcy1mb3JtIGl0ZW0tZWxlbWVudFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInNtYWxsLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG4gICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKGkpfVxuICAgICAgICAgICAgdHlwZT1cImJ1dHRvblwiPlxuICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgIDxzdHJvbmc+XG4gICAgICAgICAgICB7bmFtZS5jb21wb25lbnRzLm1hcCgoY29tcG9uZW50KSA9PiBjb21wb25lbnQudmFsdWUpLmpvaW4oXCIgXCIpfVxuICAgICAgICAgIDwvc3Ryb25nPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPHVsIGtleT1cImNvbXBvbmVudC1saXN0XCI+XG4gICAgICAgICAge25hbWUuY29tcG9uZW50cy5tYXAoKGNvbXBvbmVudCwgaikgPT4gKFxuICAgICAgICAgICAgPGxpIGtleT17YCR7aX0tJHtqfS1jb21wb25lbnRgfT5cbiAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cFwiIGtleT1cImNvbXBvbmVudC12YWx1ZXNcIj5cbiAgICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICAgICAgICAgICAgPFNlbGVjdEZpZWxkIHZhbHVlPXtjb21wb25lbnQudHlwZX0gbm9DbGVhcj17dHJ1ZX1cbiAgICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWwpID0+IHRoaXMub25DaGFuZ2VDb21wb25lbnRUeXBlKGksIGosIHZhbCl9XG4gICAgICAgICAgICAgICAgICAgIGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cbiAgICAgICAgICAgICAgICAgICAge29wdGlvbnMubWFwKChvcHRpb24pID0+IChcbiAgICAgICAgICAgICAgICAgICAgICA8c3BhbiB2YWx1ZT17b3B0aW9ufSBrZXk9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG4gICAgICAgICAgICAgICAgICAgICkpfVxuICAgICAgICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgICAgICA8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiBrZXk9e2BpbnB1dC0ke2l9LSR7an1gfVxuICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gdGhpcy5vbkNoYW5nZUNvbXBvbmVudFZhbHVlKGksIGosIGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcj17Y29tcG9uZW50LnR5cGV9IHZhbHVlPXtjb21wb25lbnQudmFsdWV9IC8+XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgICAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmVDb21wb25lbnQoaSwgail9ID5cbiAgICAgICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgICAgICAgPC9zcGFuPlxuICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICAgICAgPGJ1dHRvbiBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uQWRkQ29tcG9uZW50KGkpfVxuICAgICAgICAgICAgIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdCBidG4teHMgcHVsbC1yaWdodFwiIHR5cGU9XCJidXR0b25cIj5cbiAgICAgICAgICAgIEFkZCBjb21wb25lbnRcbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICA8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBoZWlnaHQ6IFwiNnB4XCIsIGNsZWFyOiBcInJpZ2h0XCJ9fSAvPlxuICAgICAgPC9kaXY+XG4gICAgKSlcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuICAgICAgICB7bmFtZUVsZW1lbnRzfVxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e3RoaXMub25BZGQuYmluZCh0aGlzKX0+XG4gICAgICAgICAgQWRkIG5hbWVcbiAgICAgICAgPC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXksXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgUmVsYXRpb25GaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIHF1ZXJ5OiBcIlwiLFxuICAgICAgc3VnZ2VzdGlvbnM6IFtdXG4gICAgfVxuICB9XG5cbiAgb25SZW1vdmUodmFsdWUpIHtcbiAgICBjb25zdCBjdXJyZW50VmFsdWVzID0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcblxuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoXG4gICAgICBbXCJAcmVsYXRpb25zXCIsIHRoaXMucHJvcHMubmFtZV0sXG4gICAgICBjdXJyZW50VmFsdWVzLmZpbHRlcigoY3VyVmFsKSA9PiBjdXJWYWwuaWQgIT09IHZhbHVlLmlkKVxuICAgICk7XG5cbiAgfVxuXG4gIG9uQWRkKHN1Z2dlc3Rpb24pIHtcbiAgICBjb25zdCBjdXJyZW50VmFsdWVzID0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcbiAgICBpZiAoY3VycmVudFZhbHVlcy5tYXAoKHZhbCkgPT4gdmFsLmlkKS5pbmRleE9mKHN1Z2dlc3Rpb24ua2V5KSA+IC0xKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoXG4gICAgICBbXCJAcmVsYXRpb25zXCIsIHRoaXMucHJvcHMubmFtZV0sXG4gICAgICBjdXJyZW50VmFsdWVzLmNvbmNhdCh7XG4gICAgICAgIGlkOiBzdWdnZXN0aW9uLmtleSxcbiAgICAgICAgZGlzcGxheU5hbWU6IHN1Z2dlc3Rpb24udmFsdWUsXG4gICAgICAgIGFjY2VwdGVkOiB0cnVlXG4gICAgICB9KVxuICAgICk7XG4gIH1cblxuICBvblF1ZXJ5Q2hhbmdlKGV2KSB7XG4gICAgY29uc3QgeyBnZXRBdXRvY29tcGxldGVWYWx1ZXMsIHBhdGggfSA9IHRoaXMucHJvcHM7XG4gICAgdGhpcy5zZXRTdGF0ZSh7cXVlcnk6IGV2LnRhcmdldC52YWx1ZX0pO1xuICAgIGdldEF1dG9jb21wbGV0ZVZhbHVlcyhwYXRoLCBldi50YXJnZXQudmFsdWUsIChyZXN1bHRzKSA9PiB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogcmVzdWx0c30pXG4gICAgfSk7XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHZhbHVlcyA9IGVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuICAgIGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5maWx0ZXIoKHZhbCkgPT4gdmFsLmFjY2VwdGVkKS5tYXAoKHZhbHVlLCBpKSA9PiAoXG4gICAgICA8ZGl2IGtleT17YCR7aX0tJHt2YWx1ZS5pZH1gfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cbiAgICAgICAgPHN0cm9uZz57dmFsdWUuZGlzcGxheU5hbWV9PC9zdHJvbmc+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG4gICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICA8L2Rpdj5cbiAgICApKTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICA8aDQ+e2NhbWVsMmxhYmVsKG5hbWUpfTwvaDQ+XG4gICAgICAgIHtpdGVtRWxlbWVudHN9XG4gICAgICAgIDxpbnB1dCBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiBvbkNoYW5nZT17dGhpcy5vblF1ZXJ5Q2hhbmdlLmJpbmQodGhpcyl9XG4gICAgICAgICAgdmFsdWU9e3RoaXMuc3RhdGUucXVlcnl9IHBsYWNlaG9sZGVyPVwiU2VhcmNoLi4uXCIgLz5cbiAgICAgICAgPGRpdiBzdHlsZT17e292ZXJmbG93WTogXCJhdXRvXCIsIG1heEhlaWdodDogXCIzMDBweFwifX0+XG4gICAgICAgICAge3RoaXMuc3RhdGUuc3VnZ2VzdGlvbnMubWFwKChzdWdnZXN0aW9uKSA9PiAoXG4gICAgICAgICAgICA8YSBrZXk9e3N1Z2dlc3Rpb24ua2V5fSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIlxuICAgICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uQWRkKHN1Z2dlc3Rpb24pfT5cbiAgICAgICAgICAgICAge3N1Z2dlc3Rpb24udmFsdWV9XG4gICAgICAgICAgICA8L2E+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBSZWxhdGlvbkZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uLy4uLy4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnQgPSBlbnRpdHkuZGF0YVtuYW1lXSAmJiBlbnRpdHkuZGF0YVtuYW1lXS5sZW5ndGggPiAwID8gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57ZW50aXR5LmRhdGFbbmFtZV19PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gb25DaGFuZ2UoW25hbWVdLCBcIlwiKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkgOiBudWxsO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50fVxuXHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvbkNoYW5nZShbbmFtZV0sIHZhbHVlKX1cblx0XHRcdFx0XHRub0NsZWFyPXt0cnVlfSBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG5cdFx0XHRcdFx0PHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG5cdFx0XHRcdFx0XHRTZWxlY3Qge2xhYmVsLnRvTG93ZXJDYXNlKCl9XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHRcdHtvcHRpb25zLm1hcCgob3B0aW9uKSA9PiAoXG5cdFx0XHRcdFx0XHQ8c3BhbiBrZXk9e29wdGlvbn0gdmFsdWU9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG5cdFx0XHRcdFx0KSl9XG5cdFx0XHRcdDwvU2VsZWN0RmllbGQ+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheVxufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgU3RyaW5nRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHQ8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcblx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiBvbkNoYW5nZShbbmFtZV0sIGV2LnRhcmdldC52YWx1ZSl9XG5cdFx0XHRcdFx0dmFsdWU9e2VudGl0eS5kYXRhW25hbWVdIHx8IFwiXCJ9XG5cdFx0XHRcdFx0cGxhY2Vob2xkZXI9e2BFbnRlciAke2xhYmVsLnRvTG93ZXJDYXNlKCl9YH1cblx0XHRcdFx0Lz5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuU3RyaW5nRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgU3RyaW5nRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCJcblxuaW1wb3J0IFN0cmluZ0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9zdHJpbmctZmllbGRcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvc2VsZWN0XCI7XG5pbXBvcnQgTXVsdGlTZWxlY3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvbXVsdGktc2VsZWN0XCI7XG5pbXBvcnQgUmVsYXRpb25GaWVsZCBmcm9tIFwiLi9maWVsZHMvcmVsYXRpb25cIjtcbmltcG9ydCBTdHJpbmdMaXN0RmllbGQgZnJvbSBcIi4vZmllbGRzL2xpc3Qtb2Ytc3RyaW5nc1wiO1xuaW1wb3J0IExpbmtGaWVsZCBmcm9tIFwiLi9maWVsZHMvbGlua3NcIjtcbmltcG9ydCBOYW1lc0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9uYW1lc1wiO1xuXG5jb25zdCBmaWVsZE1hcCA9IHtcblx0XCJzdHJpbmdcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJ0ZXh0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwiZGF0YWJsZVwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcIm11bHRpc2VsZWN0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8TXVsdGlTZWxlY3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KSxcblx0XCJzZWxlY3RcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTZWxlY3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KSxcblx0XCJyZWxhdGlvblwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFJlbGF0aW9uRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBwYXRoPXtmaWVsZERlZi5xdWlja3NlYXJjaH0gLz4pLFxuICBcImxpc3Qtb2Ytc3RyaW5nc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0xpc3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcbiAgXCJsaW5rc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPExpbmtGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJuYW1lc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPE5hbWVzRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPilcbn07XG5cbmNsYXNzIEVudGl0eUZvcm0gZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uTmV3LCBvbkRlbGV0ZSwgb25DaGFuZ2UsIGdldEF1dG9jb21wbGV0ZVZhbHVlcyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGVudGl0eSwgY3VycmVudE1vZGUsIHByb3BlcnRpZXMsIGVudGl0eUxhYmVsIH0gPSB0aGlzLnByb3BzO1xuXG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtOFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1wcmltYXJ5IHB1bGwtcmlnaHRcIiBvbkNsaWNrPXsoKSA9PiBvbk5ldyhlbnRpdHkuZG9tYWluKX0+XG4gICAgICAgICAgICBOZXcge2VudGl0eUxhYmVsfVxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAge3Byb3BlcnRpZXNcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gIWZpZWxkTWFwLmhhc093blByb3BlcnR5KGZpZWxkRGVmLnR5cGUpKVxuICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PiAoPGRpdiBrZXk9e2l9IHN0eWxlPXt7XCJjb2xvclwiOiBcInJlZFwifX0+PHN0cm9uZz5GaWVsZCB0eXBlIG5vdCBzdXBwb3J0ZWQ6IHtmaWVsZERlZi50eXBlfTwvc3Ryb25nPjwvZGl2PikpfVxuICAgICAgICB7cHJvcGVydGllc1xuICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT5cbiAgICAgICAgICBmaWVsZE1hcFtmaWVsZERlZi50eXBlXShmaWVsZERlZiwge1xuXHRcdFx0XHRcdFx0a2V5OiBgJHtpfS0ke2ZpZWxkRGVmLm5hbWV9YCxcblx0XHRcdFx0XHRcdGVudGl0eTogZW50aXR5LFxuXHRcdFx0XHRcdFx0b25DaGFuZ2U6IG9uQ2hhbmdlLFxuXHRcdFx0XHRcdFx0Z2V0QXV0b2NvbXBsZXRlVmFsdWVzOiBnZXRBdXRvY29tcGxldGVWYWx1ZXNcblx0XHRcdFx0XHR9KVxuICAgICAgICApfVxuICAgICAgICB7Y3VycmVudE1vZGUgPT09IFwiZWRpdFwiXG4gICAgICAgICAgPyAoPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICAgICAgPGg0PkRlbGV0ZTwvaDQ+XG4gICAgICAgICAgICAgIDxidXRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRhbmdlclwiIG9uQ2xpY2s9e29uRGVsZXRlfT5cbiAgICAgICAgICAgICAgICBEZWxldGUge2VudGl0eUxhYmVsfVxuICAgICAgICAgICAgICA8L2J1dG9uPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgKSA6IG51bGx9XG4gICAgICA8L2Rpdj5cbiAgICApXG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgRW50aXR5Rm9ybTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBvblNhdmUsIG9uQ2FuY2VsIH0gPSBwcm9wcztcblxuICByZXR1cm4gKFxuICAgIDxkaXY+XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tcHJpbWFyeVwiIG9uQ2xpY2s9e29uU2F2ZX0+U2F2ZTwvYnV0dG9uPlxuICAgICAge1wiIFwifW9ye1wiIFwifVxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWxpbmtcIiBvbkNsaWNrPXtvbkNhbmNlbH0+Q2FuY2VsPC9idXR0b24+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHsgdXJscyB9IGZyb20gXCIuLi8uLi8uLi91cmxzXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgc3RhcnQsIGxpc3QsIGRvbWFpbiB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cInJlc3VsdC1saXN0IHJlc3VsdC1saXN0LWVkaXRcIj5cbiAgICAgIDxvbCBzdGFydD17c3RhcnQgKyAxfSBzdHlsZT17e2NvdW50ZXJSZXNldDogYHN0ZXAtY291bnRlciAke3N0YXJ0fWB9fT5cbiAgICAgICAge2xpc3QubWFwKChlbnRyeSwgaSkgPT4gKFxuICAgICAgICAgIDxsaSBrZXk9e2Ake2l9LSR7ZW50cnkuX2lkfWB9PlxuICAgICAgICAgICAgPExpbmsgdG89e3VybHMuZW50aXR5KGRvbWFpbiwgZW50cnkuX2lkKX0gc3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCB3aWR0aDogXCJjYWxjKDEwMCUgLSAzMHB4KVwiLCBoZWlnaHQ6IFwiMTAwJVwiLCBwYWRkaW5nOiBcIjAuNWVtIDBcIn19PlxuICAgICAgICAgICAgICB7ZW50cnlbXCJAZGlzcGxheU5hbWVcIl19XG4gICAgICAgICAgICA8L0xpbms+XG4gICAgICAgICAgPC9saT5cbiAgICAgICAgKSl9XG4gICAgICA8L29sPlxuICAgIDwvZGl2PlxuICApXG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25QYWdpbmF0ZUxlZnQsIG9uUGFnaW5hdGVSaWdodCB9ID0gcHJvcHM7XG4gIGNvbnN0IHsgc3RhcnQsIHJvd3MsIGxpc3RMZW5ndGggfSA9IHByb3BzO1xuXG5cblxuICByZXR1cm4gKFxuICAgIDxkaXY+XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIGRpc2FibGVkPXtzdGFydCA9PT0gMH0gb25DbGljaz17b25QYWdpbmF0ZUxlZnR9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWNoZXZyb24tbGVmdFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn17c3RhcnQgKyAxfSAtIHtzdGFydCArIHJvd3N9e1wiIFwifVxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17bGlzdExlbmd0aCA8IHJvd3N9IG9uQ2xpY2s9e29uUGFnaW5hdGVSaWdodH0+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tY2hldnJvbi1yaWdodFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UsIG9uUXVpY2tTZWFyY2gsIHF1ZXJ5IH0gPSBwcm9wcztcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAgc21hbGwtbWFyZ2luIFwiPlxuICAgICAgPGlucHV0IHR5cGU9XCJ0ZXh0XCIgcGxhY2Vob2xkZXI9XCJTZWFyY2ggZm9yLi4uXCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlKGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgIG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyBvblF1aWNrU2VhcmNoKCkgOiBmYWxzZX1cbiAgICAgICAgdmFsdWU9e3F1ZXJ5fVxuICAgICAgICAvPlxuICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17b25RdWlja1NlYXJjaH0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1zZWFyY2hcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rXCIgb25DbGljaz17KCkgPT4geyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UoXCJcIik7IG9uUXVpY2tTZWFyY2goKTsgfX0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgIDwvc3Bhbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuaW1wb3J0IE1lc3NhZ2UgZnJvbSBcIi4uLy4uL21lc3NhZ2VcIjtcblxuY29uc3QgTEFCRUxTID0ge1xuXHRcIlNVQ0NFU1NfTUVTU0FHRVwiOiBcIlwiLFxuXHRcIkVSUk9SX01FU1NBR0VcIjogKFxuXHRcdDxzcGFuPlxuXHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgLz4gV2FybmluZyFcblx0XHQ8L3NwYW4+XG5cdClcbn07XG5cbmNvbnN0IEFMRVJUX0xFVkVMUyA9IHtcblx0XCJTVUNDRVNTX01FU1NBR0VcIjogXCJpbmZvXCIsXG5cdFwiRVJST1JfTUVTU0FHRVwiOiBcImRhbmdlclwiXG59O1xuXG5jbGFzcyBNZXNzYWdlcyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG1lc3NhZ2VzLCB0eXBlcywgb25EaXNtaXNzTWVzc2FnZSB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IGZpbHRlcmVkTWVzc2FnZXMgPSBtZXNzYWdlcy5sb2dcblx0XHRcdC5tYXAoKG1zZywgaWR4KSA9PiAoe21lc3NhZ2U6IG1zZy5tZXNzYWdlLCBpbmRleDogaWR4LCB0eXBlOiBtc2cudHlwZSwgZGlzbWlzc2VkOiBtc2cuZGlzbWlzc2VkIH0pKVxuXHRcdFx0LmZpbHRlcigobXNnKSA9PiB0eXBlcy5pbmRleE9mKG1zZy50eXBlKSA+IC0xICYmICFtc2cuZGlzbWlzc2VkKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2PlxuXHRcdFx0XHR7ZmlsdGVyZWRNZXNzYWdlcy5tYXAoKG1zZykgPT4gKFxuXHRcdFx0XHRcdDxNZXNzYWdlIGtleT17bXNnLmluZGV4fVxuXHRcdFx0XHRcdFx0ZGlzbWlzc2libGU9e3RydWV9XG5cdFx0XHRcdFx0XHRhbGVydExldmVsPXtBTEVSVF9MRVZFTFNbbXNnLnR5cGVdfVxuXHRcdFx0XHRcdFx0b25DbG9zZU1lc3NhZ2U9eygpID0+IG9uRGlzbWlzc01lc3NhZ2UobXNnLmluZGV4KX0+XG5cdFx0XHRcdFx0XHQ8c3Ryb25nPntMQUJFTFNbbXNnLnR5cGVdfTwvc3Ryb25nPiA8c3Bhbj57bXNnLm1lc3NhZ2V9PC9zcGFuPlxuXHRcdFx0XHRcdDwvTWVzc2FnZT5cblx0XHRcdFx0KSl9XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbk1lc3NhZ2VzLnByb3BUeXBlcyA9IHtcblx0bWVzc2FnZXM6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG9uRGlzbWlzc01lc3NhZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWQsXG5cdHR5cGVzOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXkuaXNSZXF1aXJlZFxufTtcblxuZXhwb3J0IGRlZmF1bHQgTWVzc2FnZXM7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgaXNPcGVuOiBmYWxzZVxuICAgIH07XG4gICAgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50KCkge1xuICAgIGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG4gIH1cblxuICB0b2dnbGVTZWxlY3QoKSB7XG4gICAgaWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG4gICAgfVxuICB9XG5cbiAgaGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuICAgIGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuICAgIGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe1xuICAgICAgICBpc09wZW46IGZhbHNlXG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbkNoYW5nZSwgb25DbGVhciwgdmFsdWUsIGJ0bkNsYXNzLCBub0NsZWFyIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qgc2VsZWN0ZWRPcHRpb24gPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgPT09IHZhbHVlKTtcbiAgICBjb25zdCBwbGFjZWhvbGRlciA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy50eXBlID09PSBcInBsYWNlaG9sZGVyXCIpO1xuICAgIGNvbnN0IG90aGVyT3B0aW9ucyA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSAmJiBvcHQucHJvcHMudmFsdWUgIT09IHZhbHVlKTtcblxuICAgIHJldHVybiAoXG5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImRyb3Bkb3duXCIsIHtvcGVuOiB0aGlzLnN0YXRlLmlzT3Blbn0pfT5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9e2N4KFwiYnRuXCIsIFwiZHJvcGRvd24tdG9nZ2xlXCIsIGJ0bkNsYXNzIHx8IFwiYnRuLWJsYW5rXCIpfSBvbkNsaWNrPXt0aGlzLnRvZ2dsZVNlbGVjdC5iaW5kKHRoaXMpfT5cbiAgICAgICAgICB7c2VsZWN0ZWRPcHRpb24ubGVuZ3RoID8gc2VsZWN0ZWRPcHRpb24gOiBwbGFjZWhvbGRlcn0gPHNwYW4gY2xhc3NOYW1lPVwiY2FyZXRcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cblxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwiZHJvcGRvd24tbWVudVwiPlxuICAgICAgICAgIHsgdmFsdWUgJiYgIW5vQ2xlYXIgPyAoXG4gICAgICAgICAgICA8bGk+XG4gICAgICAgICAgICAgIDxhIG9uQ2xpY2s9eygpID0+IHsgb25DbGVhcigpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpO319PlxuICAgICAgICAgICAgICAgIC0gY2xlYXIgLVxuICAgICAgICAgICAgICA8L2E+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgICAgIHtvdGhlck9wdGlvbnMubWFwKChvcHRpb24sIGkpID0+IChcbiAgICAgICAgICAgIDxsaSBrZXk9e2l9PlxuICAgICAgICAgICAgICA8YSBzdHlsZT17e2N1cnNvcjogXCJwb2ludGVyXCJ9fSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2hhbmdlKG9wdGlvbi5wcm9wcy52YWx1ZSk7IHRoaXMudG9nZ2xlU2VsZWN0KCk7IH19PntvcHRpb259PC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC91bD5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuU2VsZWN0RmllbGQucHJvcFR5cGVzID0ge1xuICBvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIG9uQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICB2YWx1ZTogUmVhY3QuUHJvcFR5cGVzLmFueSxcbiAgYnRuQ2xhc3M6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG5vQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5ib29sXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZnVuY3Rpb24gRm9vdGVyKHByb3BzKSB7XG4gIGNvbnN0IGhpTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImhpLWxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1odXlnZW5zLWluZy5zdmdcIiAvPlxuICAgIDwvZGl2PlxuICApO1xuXG4gIGNvbnN0IGNsYXJpYWhMb2dvID0gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgY29sLW1kLTFcIj5cbiAgICAgIDxpbWcgY2xhc3NOYW1lPVwibG9nb1wiIHNyYz1cImltYWdlcy9sb2dvLWNsYXJpYWguc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBmb290ZXJCb2R5ID0gUmVhY3QuQ2hpbGRyZW4uY291bnQocHJvcHMuY2hpbGRyZW4pID4gMCA/XG4gICAgUmVhY3QuQ2hpbGRyZW4ubWFwKHByb3BzLmNoaWxkcmVuLCAoY2hpbGQsIGkpID0+IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwid2hpdGUtYmFyXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAge2kgPT09IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSAtIDFcbiAgICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2hpTG9nb308ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMCBjb2wtbWQtMTAgdGV4dC1jZW50ZXJcIj57Y2hpbGR9PC9kaXY+e2NsYXJpYWhMb2dvfTwvZGl2PilcbiAgICAgICAgICAgIDogKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2NoaWxkfTwvZGl2PilcbiAgICAgICAgICB9XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKSkgOiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG4gICAgICAgICAgICB7aGlMb2dvfVxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIHtjbGFyaWFoTG9nb31cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuXG5cbiAgcmV0dXJuIChcbiAgICA8Zm9vdGVyIGNsYXNzTmFtZT1cImZvb3RlclwiPlxuICAgICAge2Zvb3RlckJvZHl9XG4gICAgPC9mb290ZXI+XG4gIClcbn1cblxuZXhwb3J0IGRlZmF1bHQgRm9vdGVyOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IGRpc21pc3NpYmxlLCBhbGVydExldmVsLCBvbkNsb3NlTWVzc2FnZX0gPSBwcm9wcztcbiAgY29uc3QgZGlzbWlzc0J1dHRvbiA9IGRpc21pc3NpYmxlXG4gICAgPyA8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzc05hbWU9XCJjbG9zZVwiIG9uQ2xpY2s9e29uQ2xvc2VNZXNzYWdlfT48c3Bhbj4mdGltZXM7PC9zcGFuPjwvYnV0dG9uPlxuICAgIDogbnVsbDtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImFsZXJ0XCIsIGBhbGVydC0ke2FsZXJ0TGV2ZWx9YCwge1wiYWxlcnQtZGlzbWlzc2libGVcIjogZGlzbWlzc2libGV9KX0gcm9sZT1cImFsZXJ0XCI+XG4gICAgICB7ZGlzbWlzc0J1dHRvbn1cbiAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICA8L2Rpdj5cbiAgKVxufTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IEZvb3RlciBmcm9tIFwiLi9mb290ZXJcIjtcblxuY29uc3QgRk9PVEVSX0hFSUdIVCA9IDgxO1xuXG5mdW5jdGlvbiBQYWdlKHByb3BzKSB7XG4gIGNvbnN0IGZvb3RlcnMgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKGNoaWxkKSA9PiBjaGlsZC5wcm9wcy50eXBlID09PSBcImZvb3Rlci1ib2R5XCIpO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJwYWdlXCI+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpbiBoaS1HcmVlbiBjb250YWluZXItZmx1aWRcIj5cbiAgICAgICAgPG5hdiBjbGFzc05hbWU9XCJuYXZiYXIgXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwibmF2YmFyLWhlYWRlclwiPiA8YSBjbGFzc05hbWU9XCJuYXZiYXItYnJhbmRcIiBocmVmPVwiI1wiPjxpbWcgc3JjPVwiaW1hZ2VzL2xvZ28tdGltYnVjdG9vLnN2Z1wiIGNsYXNzTmFtZT1cImxvZ29cIiBhbHQ9XCJ0aW1idWN0b29cIi8+PC9hPiA8L2Rpdj5cbiAgICAgICAgICAgIDxkaXYgaWQ9XCJuYXZiYXJcIiBjbGFzc05hbWU9XCJuYXZiYXItY29sbGFwc2UgY29sbGFwc2VcIj5cbiAgICAgICAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXZiYXItbmF2IG5hdmJhci1yaWdodFwiPlxuICAgICAgICAgICAgICAgIHtwcm9wcy51c2VybmFtZSA/IDxsaT48YSBocmVmPXtwcm9wcy51c2VybG9jYXRpb24gfHwgJyMnfT48c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXVzZXJcIi8+IHtwcm9wcy51c2VybmFtZX08L2E+PC9saT4gOiBudWxsfVxuICAgICAgICAgICAgICA8L3VsPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvbmF2PlxuICAgICAgPC9kaXY+XG4gICAgICA8ZGl2ICBzdHlsZT17e21hcmdpbkJvdHRvbTogYCR7Rk9PVEVSX0hFSUdIVCAqIGZvb3RlcnMubGVuZ3RofXB4YH19PlxuICAgICAgICB7UmVhY3QuQ2hpbGRyZW4udG9BcnJheShwcm9wcy5jaGlsZHJlbikuZmlsdGVyKChjaGlsZCkgPT4gY2hpbGQucHJvcHMudHlwZSAhPT0gXCJmb290ZXItYm9keVwiKX1cbiAgICAgIDwvZGl2PlxuICAgICAgPEZvb3Rlcj5cbiAgICAgICAge2Zvb3RlcnN9XG4gICAgICA8L0Zvb3Rlcj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgUGFnZTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBSZWFjdERPTSBmcm9tIFwicmVhY3QtZG9tXCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2FjdGlvbnNcIjtcbmltcG9ydCB7c2V0VnJlfSBmcm9tIFwiLi9hY3Rpb25zL3ZyZVwiO1xuaW1wb3J0IEFwcCBmcm9tIFwiLi9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpXCI7XG5pbXBvcnQgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIGZyb20gXCIuL2FjdGlvbnMvYXV0b2NvbXBsZXRlXCI7XG5cbmltcG9ydCByb3V0ZXIgZnJvbSBcIi4vcm91dGVyXCI7XG5cbmNvbnN0IHNldFVzZXIgPSAocmVzcG9uc2UpID0+IHtcblx0cmV0dXJuIHtcblx0XHR0eXBlOiBcIlNFVF9VU0VSXCIsXG5cdFx0dXNlcjogcmVzcG9uc2Vcblx0fTtcbn07XG5cbmRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJET01Db250ZW50TG9hZGVkXCIsICgpID0+IHtcblxuXHRmdW5jdGlvbiBpbml0Um91dGVyKCkge1xuXHRcdFJlYWN0RE9NLnJlbmRlcihyb3V0ZXIsIGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKFwiYXBwXCIpKTtcblx0fVxuXG5cdGZ1bmN0aW9uIGdldFZyZUlkKCkge1xuXHRcdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdFx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdFx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdFx0bGV0IFtrZXksIHZhbHVlXSA9IHBhcmFtc1tpXS5zcGxpdChcIj1cIik7XG5cdFx0XHRpZihrZXkgPT09IFwidnJlSWRcIikge1xuXHRcdFx0XHRyZXR1cm4gdmFsdWU7XG5cdFx0XHR9XG5cdFx0fVxuXHRcdHJldHVybiBcIldvbWVuV3JpdGVyc1wiO1xuXHR9XG5cblx0ZnVuY3Rpb24gZ2V0TG9naW4oKSB7XG5cdFx0bGV0IHBhdGggPSB3aW5kb3cubG9jYXRpb24uc2VhcmNoLnN1YnN0cigxKTtcblx0XHRsZXQgcGFyYW1zID0gcGF0aC5zcGxpdChcIiZcIik7XG5cblx0XHRmb3IobGV0IGkgaW4gcGFyYW1zKSB7XG5cdFx0XHRsZXQgW2tleSwgdmFsdWVdID0gcGFyYW1zW2ldLnNwbGl0KFwiPVwiKTtcblx0XHRcdGlmKGtleSA9PT0gXCJoc2lkXCIpIHtcblx0XHRcdFx0cmV0dXJuIHt1c2VyOiB2YWx1ZSwgdG9rZW46IHZhbHVlfTtcblx0XHRcdH1cblx0XHR9XG5cdFx0cmV0dXJuIHVuZGVmaW5lZDtcblx0fVxuXHRzdG9yZS5kaXNwYXRjaChzZXRWcmUoZ2V0VnJlSWQoKSwgaW5pdFJvdXRlcikpO1xuXHRzdG9yZS5kaXNwYXRjaChzZXRVc2VyKGdldExvZ2luKCkpKTtcbn0pOyIsImltcG9ydCBzZXRJbiBmcm9tIFwiLi4vdXRpbC9zZXQtaW5cIjtcblxubGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0ZGF0YToge1xuXHRcdFwiQHJlbGF0aW9uc1wiOiBbXVxuXHR9LFxuXHRkb21haW46IG51bGwsXG5cdGVycm9yTWVzc2FnZTogbnVsbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiBhY3Rpb24uZGF0YSxcblx0XHRcdFx0ZG9tYWluOiBhY3Rpb24uZG9tYWluLFxuXHRcdFx0XHRlcnJvck1lc3NhZ2U6IGFjdGlvbi5lcnJvck1lc3NhZ2UgfHwgbnVsbFxuXHRcdFx0fX07XG5cblx0XHRjYXNlIFwiU0VUX0VOVElUWV9GSUVMRF9WQUxVRVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiBzZXRJbihhY3Rpb24uZmllbGRQYXRoLCBhY3Rpb24udmFsdWUsIHN0YXRlLmRhdGEpXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWV9GQUlMVVJFXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IHtcblx0XHRcdFx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0XHRcdFx0fSxcblx0XHRcdFx0ZXJyb3JNZXNzYWdlOiBhY3Rpb24uZXJyb3JNZXNzYWdlXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJTRVRfVlJFXCI6IHtcblx0XHRcdHJldHVybiBpbml0aWFsU3RhdGU7XG5cdFx0fVxuXG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwiaW1wb3J0IHtjb21iaW5lUmVkdWNlcnN9IGZyb20gXCJyZWR1eFwiO1xuXG5pbXBvcnQgZW50aXR5IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IG1lc3NhZ2VzIGZyb20gXCIuL21lc3NhZ2VzXCI7XG5pbXBvcnQgdXNlciBmcm9tIFwiLi91c2VyXCI7XG5pbXBvcnQgdnJlIGZyb20gXCIuL3ZyZVwiO1xuaW1wb3J0IHF1aWNrU2VhcmNoIGZyb20gXCIuL3F1aWNrLXNlYXJjaFwiO1xuXG5leHBvcnQgZGVmYXVsdCBjb21iaW5lUmVkdWNlcnMoe1xuXHR2cmU6IHZyZSxcblx0ZW50aXR5OiBlbnRpdHksXG5cdHVzZXI6IHVzZXIsXG5cdG1lc3NhZ2VzOiBtZXNzYWdlcyxcblx0cXVpY2tTZWFyY2g6IHF1aWNrU2VhcmNoXG59KTsiLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcblx0bG9nOiBbXVxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJSRVFVRVNUX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiU1VDQ0VTU19NRVNTQUdFXCI6XG5cdFx0XHRzdGF0ZS5sb2cucHVzaCh7bWVzc2FnZTogYWN0aW9uLm1lc3NhZ2UsIHR5cGU6IGFjdGlvbi50eXBlLCB0aW1lOiBuZXcgRGF0ZSgpfSk7XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0Y2FzZSBcIkVSUk9SX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiRElTTUlTU19NRVNTQUdFXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0bG9nOiBzZXRJbihbYWN0aW9uLm1lc3NhZ2VJbmRleCwgXCJkaXNtaXNzZWRcIl0sIHRydWUsIHN0YXRlLmxvZylcblx0XHRcdH07XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0c3RhcnQ6IDAsXG5cdGxpc3Q6IFtdLFxuXHRyb3dzOiA1MCxcblx0cXVlcnk6IFwiXCJcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIHN0YXJ0OiBhY3Rpb24uc3RhcnR9O1xuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWV9MSVNUXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5kYXRhXG5cdFx0XHR9fTtcblx0XHRjYXNlIFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCI6IHtcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0cXVlcnk6IGFjdGlvbi52YWx1ZVxuXHRcdFx0fX07XG5cdFx0fVxuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0gbnVsbDtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfVVNFUlwiOlxuXHRcdFx0aWYgKGFjdGlvbi51c2VyKSB7XG5cdFx0XHRcdHJldHVybiBhY3Rpb24udXNlcjtcblx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRcdH1cblx0XHRcdGJyZWFrO1xuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0ge1xuXHR2cmVJZDogbnVsbCxcblx0bGlzdDogW10sXG5cdGNvbGxlY3Rpb25zOiB7fSxcblx0ZG9tYWluOiBudWxsXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9WUkVcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHR2cmVJZDogYWN0aW9uLnZyZUlkLFxuXHRcdFx0XHRjb2xsZWN0aW9uczogYWN0aW9uLmNvbGxlY3Rpb25zIHx8IG51bGwsXG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5saXN0IHx8IHN0YXRlLmxpc3Rcblx0XHRcdH07XG5cblx0XHRjYXNlIFwiTElTVF9WUkVTXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0bGlzdDogYWN0aW9uLmxpc3QsXG5cdFx0XHRcdGNvbGxlY3Rpb25zOiBudWxsXG5cdFx0XHR9O1xuXHRcdGNhc2UgXCJTRVRfRE9NQUlOXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0ZG9tYWluOiBhY3Rpb24uZG9tYWluXG5cdFx0XHR9O1xuXG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCB7Um91dGVyLCBSZWRpcmVjdCwgUm91dGUsIGhhc2hIaXN0b3J5fSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQge1Byb3ZpZGVyLCBjb25uZWN0fSBmcm9tIFwicmVhY3QtcmVkdXhcIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IGdldEF1dG9jb21wbGV0ZVZhbHVlcyBmcm9tIFwiLi9hY3Rpb25zL2F1dG9jb21wbGV0ZVwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vYWN0aW9uc1wiO1xuXG5pbXBvcnQgRWRpdEd1aSBmcm9tIFwiLi9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpXCI7XG5pbXBvcnQge3VybHN9IGZyb20gXCIuL3VybHNcIjtcblxuXG5cblxuZXhwb3J0IGZ1bmN0aW9uIG5hdmlnYXRlVG8oa2V5LCBhcmdzKSB7XG5cdGhhc2hIaXN0b3J5LnB1c2godXJsc1trZXldLmFwcGx5KG51bGwsIGFyZ3MpKTtcbn1cblxuY29uc3QgZGVmYXVsdENvbm5lY3QgPSBjb25uZWN0KFxuXHRzdGF0ZSA9PiAoey4uLnN0YXRlLCBnZXRBdXRvY29tcGxldGVWYWx1ZXM6IGdldEF1dG9jb21wbGV0ZVZhbHVlc30pLFxuXHRkaXNwYXRjaCA9PiBhY3Rpb25zKG5hdmlnYXRlVG8sIGRpc3BhdGNoKVxuKTtcblxuXG5leHBvcnQgZGVmYXVsdCAoXG5cdDxQcm92aWRlciBzdG9yZT17c3RvcmV9PlxuXHRcdDxSb3V0ZXIgaGlzdG9yeT17aGFzaEhpc3Rvcnl9PlxuXHRcdFx0PFJvdXRlIHBhdGg9e3VybHMucm9vdCgpfSBjb21wb25lbnRzPXtkZWZhdWx0Q29ubmVjdChFZGl0R3VpKX0gLz5cblx0XHRcdDxSb3V0ZSBwYXRoPXt1cmxzLm5ld0VudGl0eSgpfSBjb21wb25lbnRzPXtkZWZhdWx0Q29ubmVjdChFZGl0R3VpKX0gLz5cblx0XHRcdDxSb3V0ZSBwYXRoPXt1cmxzLmVudGl0eSgpfSBjb21wb25lbnRzPXtkZWZhdWx0Q29ubmVjdChFZGl0R3VpKX0gLz5cblx0XHQ8L1JvdXRlcj5cblx0PC9Qcm92aWRlcj5cbik7XG5cbiIsImltcG9ydCB7Y3JlYXRlU3RvcmUsIGFwcGx5TWlkZGxld2FyZX0gZnJvbSBcInJlZHV4XCI7XG5pbXBvcnQgdGh1bmtNaWRkbGV3YXJlIGZyb20gXCJyZWR1eC10aHVua1wiO1xuXG5pbXBvcnQgcmVkdWNlcnMgZnJvbSBcIi4uL3JlZHVjZXJzXCI7XG5cbmNvbnN0IGxvZ2dlciA9ICgpID0+IG5leHQgPT4gYWN0aW9uID0+IHtcbiAgaWYgKGFjdGlvbi5oYXNPd25Qcm9wZXJ0eShcInR5cGVcIikpIHtcbiAgICBjb25zb2xlLmxvZyhcIltSRURVWF1cIiwgYWN0aW9uLnR5cGUsIGFjdGlvbik7XG4gIH1cblxuICByZXR1cm4gbmV4dChhY3Rpb24pO1xufTtcblxubGV0IGNyZWF0ZVN0b3JlV2l0aE1pZGRsZXdhcmUgPSBhcHBseU1pZGRsZXdhcmUoLypsb2dnZXIsKi8gdGh1bmtNaWRkbGV3YXJlKShjcmVhdGVTdG9yZSk7XG5leHBvcnQgZGVmYXVsdCBjcmVhdGVTdG9yZVdpdGhNaWRkbGV3YXJlKHJlZHVjZXJzKTtcbiIsImNvbnN0IHVybHMgPSB7XG5cdHJvb3QoKSB7XG5cdFx0cmV0dXJuIFwiL1wiO1xuXHR9LFxuXHRuZXdFbnRpdHkoY29sbGVjdGlvbikge1xuXHRcdHJldHVybiBjb2xsZWN0aW9uXG5cdFx0XHQ/IGAvJHtjb2xsZWN0aW9ufWBcblx0XHRcdDogXCIvOmNvbGxlY3Rpb25cIjtcblx0fSxcblx0ZW50aXR5KGNvbGxlY3Rpb24sIGlkKSB7XG5cdFx0cmV0dXJuIGNvbGxlY3Rpb24gJiYgaWRcblx0XHRcdD8gYC8ke2NvbGxlY3Rpb259LyR7aWR9YFxuXHRcdFx0OiBcIi86Y29sbGVjdGlvbi86aWRcIjtcblx0fVxufTtcblxuZXhwb3J0IHsgdXJscyB9IiwiZnVuY3Rpb24gZGVlcENsb25lOShvYmopIHtcbiAgICB2YXIgaSwgbGVuLCByZXQ7XG5cbiAgICBpZiAodHlwZW9mIG9iaiAhPT0gXCJvYmplY3RcIiB8fCBvYmogPT09IG51bGwpIHtcbiAgICAgICAgcmV0dXJuIG9iajtcbiAgICB9XG5cbiAgICBpZiAoQXJyYXkuaXNBcnJheShvYmopKSB7XG4gICAgICAgIHJldCA9IFtdO1xuICAgICAgICBsZW4gPSBvYmoubGVuZ3RoO1xuICAgICAgICBmb3IgKGkgPSAwOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgICAgIHJldC5wdXNoKCAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldICk7XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICByZXQgPSB7fTtcbiAgICAgICAgZm9yIChpIGluIG9iaikge1xuICAgICAgICAgICAgaWYgKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkge1xuICAgICAgICAgICAgICAgIHJldFtpXSA9ICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV07XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHJldDtcbn1cblxuZXhwb3J0IGRlZmF1bHQgZGVlcENsb25lOTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4vY2xvbmUtZGVlcFwiO1xuXG4vLyBEbyBlaXRoZXIgb2YgdGhlc2U6XG4vLyAgYSkgU2V0IGEgdmFsdWUgYnkgcmVmZXJlbmNlIGlmIGRlcmVmIGlzIG5vdCBudWxsXG4vLyAgYikgU2V0IGEgdmFsdWUgZGlyZWN0bHkgaW4gdG8gZGF0YSBvYmplY3QgaWYgZGVyZWYgaXMgbnVsbFxuY29uc3Qgc2V0RWl0aGVyID0gKGRhdGEsIGRlcmVmLCBrZXksIHZhbCkgPT4ge1xuXHQoZGVyZWYgfHwgZGF0YSlba2V5XSA9IHZhbDtcblx0cmV0dXJuIGRhdGE7XG59O1xuXG4vLyBTZXQgYSBuZXN0ZWQgdmFsdWUgaW4gZGF0YSAobm90IHVubGlrZSBpbW11dGFibGVqcywgYnV0IGEgY2xvbmUgb2YgZGF0YSBpcyBleHBlY3RlZCBmb3IgcHJvcGVyIGltbXV0YWJpbGl0eSlcbmNvbnN0IF9zZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPSBudWxsKSA9PlxuXHRwYXRoLmxlbmd0aCA+IDEgP1xuXHRcdF9zZXRJbihwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPyBkZXJlZltwYXRoLnNoaWZ0KCldIDogZGF0YVtwYXRoLnNoaWZ0KCldKSA6XG5cdFx0c2V0RWl0aGVyKGRhdGEsIGRlcmVmLCBwYXRoWzBdLCB2YWx1ZSk7XG5cbmNvbnN0IHNldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhKSA9PlxuXHRfc2V0SW4oY2xvbmUocGF0aCksIHZhbHVlLCBjbG9uZShkYXRhKSk7XG5cbmV4cG9ydCBkZWZhdWx0IHNldEluOyJdfQ==
