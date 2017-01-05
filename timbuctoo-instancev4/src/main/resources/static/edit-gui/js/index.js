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
		dispatch({ type: "BEFORE_FETCH_ENTITY" });
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

var _reactRouter = require("react-router");

var _urls = require("../../../../urls");

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
            _reactRouter.Link,
            { to: value.path.replace(/^domain\//, "") },
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

},{"../../../../urls":37,"./camel2label":10,"react":"react","react-router":"react-router"}],16:[function(require,module,exports){
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

		case "BEFORE_FETCH_ENTITY":
			return _extends({}, state, {
				data: {
					"@relations": []
				}
			});
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
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDRWUsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFJLFVBQVU7QUFDYixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEYSxFQUFkOztBQUlBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxPQUFLLEtBQUssS0FBTCxDQUFXLElBQVgsRUFBaUIsR0FBakIsQ0FBcUIsVUFBQyxDQUFELEVBQU87QUFBRSxVQUFPLEVB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QUosRUFBMkI7QUFDdkIsb0JBQUksQ0FBSixJQUFVLFFBQU8sSUFBSSxDQUFKLENBQVAsTUFBa0IsUUFBbEIsSUFBOEIsSUFBSSxDQUFKLE1BQVcsSUFBMUMsR0FBa0QsV0FBVyxJQUFJLENBQUosQ0FBWCxDQUFsRCxHQUF1RSxJQUFJLENBQUosQ0FBaEY7QUFDSDtBQUNKO0FBQ0o7QUFDRCxXQUFPLEdBQVA7QUFDSDs7a0JBRWMsVTs7Ozs7Ozs7O0FDeEJmOzs7Ozs7QUFFQTtBQUNBO0FBQ0E7QUFDQSxJQUFNLFlBQVksU0FBWixTQUFZLENBQUMsSUFBRCxFQUFPLEtBQVAsRUFBYyxHQUFkLEVBQW1CLEdBQW5CLEVBQTJCO0FBQzVDLEVBQUMsU0FBUyxJQUFWLEVBQWdCLEdBQWhCLElBQXVCLEdBQXZCO0FBQ0EsUUFBTyxJQUFQO0FBQ0EsQ0FIRDs7QUFLQTtBQUNBLElBQU0sU0FBUyxTQUFULE1BQVMsQ0FBQyxJQUFELEVBQU8sS0FBUCxFQUFjLElBQWQ7QUFBQSxLQUFvQixLQUFwQix1RUFBNEIsSUFBNUI7QUFBQSxRQUNkLEtBQUssTUFBTCxHQUFjLENBQWQsR0FDQyxPQUFPLElBQVAsRUFBYSxLQUFiLEVBQW9CLElBQXBCLEVBQTBCLFFBQVEsTUFBTSxLQUFLLEtBQUwsRUFBTixDQUFSLEdBQThCLEtBQUssS0FBSyxLQUFMLEVBQUwsQ0FBeEQsQ0FERCxHQUVDLFVBQVUsSUFBVixFQUFnQixLQUFoQixFQUF1QixLQUFLLENBQUwsQ0FBdkIsRUFBZ0MsS0FBaEMsQ0FIYTtBQUFBLENBQWY7O0FBS0EsSUFBTSxRQUFRLFNBQVIsS0FBUSxDQUFDLElBQUQsRUFBTyxLQUFQLEVBQWMsSUFBZDtBQUFBLFFBQ2IsT0FBTyx5QkFBTSxJQUFOLENBQVAsRUFBb0IsS0FBcEIsRUFBMkIseUJBQU0sSUFBTixDQUEzQixDQURhO0FBQUEsQ0FBZDs7a0JBR2UsSyIsImZpbGUiOiJnZW5lcmF0ZWQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlc0NvbnRlbnQiOlsiKGZ1bmN0aW9uIGUodCxuLHIpe2Z1bmN0aW9uIHMobyx1KXtpZighbltvXSl7aWYoIXRbb10pe3ZhciBhPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7aWYoIXUmJmEpcmV0dXJuIGEobywhMCk7aWYoaSlyZXR1cm4gaShvLCEwKTt2YXIgZj1uZXcgRXJyb3IoXCJDYW5ub3QgZmluZCBtb2R1bGUgJ1wiK28rXCInXCIpO3Rocm93IGYuY29kZT1cIk1PRFVMRV9OT1RfRk9VTkRcIixmfXZhciBsPW5bb109e2V4cG9ydHM6e319O3Rbb11bMF0uY2FsbChsLmV4cG9ydHMsZnVuY3Rpb24oZSl7dmFyIG49dFtvXVsxXVtlXTtyZXR1cm4gcyhuP246ZSl9LGwsbC5leHBvcnRzLGUsdCxuLHIpfXJldHVybiBuW29dLmV4cG9ydHN9dmFyIGk9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtmb3IodmFyIG89MDtvPHIubGVuZ3RoO28rKylzKHJbb10pO3JldHVybiBzfSkiLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwYXRoLCBxdWVyeSwgZG9uZSkge1xuXHRsZXQgb3B0aW9ucyA9IHtcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS8ke3BhdGgucmVwbGFjZSgvXlxcL3ZbXi9dK1xcLy8sIFwiXCIpfT9xdWVyeT0ke3F1ZXJ5fSpgXG5cdH07XG5cblx0bGV0IHhockRvbmUgPSBmdW5jdGlvbihlcnIsIHJlc3BvbnNlLCBib2R5KSB7XG5cdFx0ZG9uZShKU09OLnBhcnNlKGJvZHkpLm1hcCgoZCkgPT4geyByZXR1cm4ge2tleTogZC5rZXkucmVwbGFjZSgvXi4rXFwvLywgXCJcIiksIHZhbHVlOiBkLnZhbHVlfTsgfSkpO1xuXHR9O1xuXG5cdHNlcnZlci5mYXN0WGhyKG9wdGlvbnMsIHhockRvbmUpO1xufSIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5cbmNvbnN0IHNhdmVOZXdFbnRpdHkgPSAoZG9tYWluLCBzYXZlRGF0YSwgdG9rZW4sIHZyZUlkLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIlBPU1RcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHRib2R5OiBKU09OLnN0cmluZ2lmeShzYXZlRGF0YSksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufWBcblx0fSwgbmV4dCwgZmFpbCwgYENyZWF0ZSBuZXcgJHtkb21haW59YCk7XG5cbmNvbnN0IHVwZGF0ZUVudGl0eSA9IChkb21haW4sIHNhdmVEYXRhLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiUFVUXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0Ym9keTogSlNPTi5zdHJpbmdpZnkoc2F2ZURhdGEpLFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0vJHtzYXZlRGF0YS5faWR9YFxuXHR9LCBuZXh0LCBmYWlsLCBgVXBkYXRlICR7ZG9tYWlufWApO1xuXG5jb25zdCBkZWxldGVFbnRpdHkgPSAoZG9tYWluLCBlbnRpdHlJZCwgdG9rZW4sIHZyZUlkLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkRFTEVURVwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0vJHtlbnRpdHlJZH1gXG5cdH0sIG5leHQsIGZhaWwsIGBEZWxldGUgJHtkb21haW59YCk7XG5cbmNvbnN0IGZldGNoRW50aXR5ID0gKGxvY2F0aW9uLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIn0sXG5cdFx0dXJsOiBsb2NhdGlvblxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0Y29uc3QgZGF0YSA9IEpTT04ucGFyc2UocmVzcC5ib2R5KTtcblx0XHRuZXh0KGRhdGEpO1xuXHR9LCBmYWlsLCBcIkZldGNoIGVudGl0eVwiKTtcblxuY29uc3QgZmV0Y2hFbnRpdHlMaXN0ID0gKGRvbWFpbiwgc3RhcnQsIHJvd3MsIG5leHQpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1wiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwifSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59P3Jvd3M9JHtyb3dzfSZzdGFydD0ke3N0YXJ0fWBcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGNvbnN0IGRhdGEgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0bmV4dChkYXRhKTtcblx0fSk7XG5cbmNvbnN0IGNydWQgPSB7XG5cdHNhdmVOZXdFbnRpdHk6IHNhdmVOZXdFbnRpdHksXG5cdHVwZGF0ZUVudGl0eTogdXBkYXRlRW50aXR5LFxuXHRkZWxldGVFbnRpdHk6IGRlbGV0ZUVudGl0eSxcblx0ZmV0Y2hFbnRpdHk6IGZldGNoRW50aXR5LFxuXHRmZXRjaEVudGl0eUxpc3Q6IGZldGNoRW50aXR5TGlzdFxufTtcblxuZXhwb3J0IHtzYXZlTmV3RW50aXR5LCB1cGRhdGVFbnRpdHksIGRlbGV0ZUVudGl0eSwgZmV0Y2hFbnRpdHksIGZldGNoRW50aXR5TGlzdCwgY3J1ZH07IiwiaW1wb3J0IGNsb25lIGZyb20gXCIuLi91dGlsL2Nsb25lLWRlZXBcIjtcbmltcG9ydCB7IGNydWQgfSBmcm9tIFwiLi9jcnVkXCI7XG5pbXBvcnQgc2F2ZVJlbGF0aW9ucyBmcm9tIFwiLi9zYXZlLXJlbGF0aW9uc1wiO1xuaW1wb3J0IGF1dG9jb21wbGV0ZSBmcm9tIFwiLi9hdXRvY29tcGxldGVcIjtcblxuLy8gU2tlbGV0b24gYmFzZSBkYXRhIHBlciBmaWVsZCBkZWZpbml0aW9uXG5jb25zdCBpbml0aWFsRGF0YSA9IHtcblx0bmFtZXM6IFtdLFxuXHRtdWx0aXNlbGVjdDogW10sXG5cdGxpbmtzOiBbXSxcblx0a2V5d29yZDogW10sXG5cdFwibGlzdC1vZi1zdHJpbmdzXCI6IFtdLFxuXHRhbHRuYW1lczogW10sXG5cdHRleHQ6IFwiXCIsXG5cdHN0cmluZzogXCJcIixcblx0c2VsZWN0OiBcIlwiLFxuXHRkYXRhYmxlOiBcIlwiXG59O1xuXG4vLyBSZXR1cm4gdGhlIGluaXRpYWwgZGF0YSBmb3IgdGhlIHR5cGUgaW4gdGhlIGZpZWxkIGRlZmluaXRpb25cbmNvbnN0IGluaXRpYWxEYXRhRm9yVHlwZSA9IChmaWVsZERlZikgPT5cblx0ZmllbGREZWYuZGVmYXVsdFZhbHVlIHx8IChmaWVsZERlZi50eXBlID09PSBcInJlbGF0aW9uXCIgfHwgZmllbGREZWYudHlwZSA9PT0gXCJrZXl3b3JkXCIgPyB7fSA6IGluaXRpYWxEYXRhW2ZpZWxkRGVmLnR5cGVdKTtcblxuLy8gUmV0dXJuIHRoZSBpbml0aWFsIG5hbWUta2V5IGZvciBhIGNlcnRhaW4gZmllbGQgdHlwZVxuY29uc3QgbmFtZUZvclR5cGUgPSAoZmllbGREZWYpID0+XG5cdGZpZWxkRGVmLnR5cGUgPT09IFwicmVsYXRpb25cIiB8fCBmaWVsZERlZi50eXBlID09PSBcImtleXdvcmRcIiA/IFwiQHJlbGF0aW9uc1wiIDogZmllbGREZWYubmFtZTtcblxuXG4vLyBDcmVhdGUgYSBuZXcgZW1wdHkgZW50aXR5IGJhc2VkIG9uIHRoZSBmaWVsZERlZmluaXRpb25zXG5jb25zdCBtYWtlU2tlbGV0b24gPSBmdW5jdGlvbiAodnJlLCBkb21haW4pIHtcblx0aWYgKHZyZSAmJiB2cmUuY29sbGVjdGlvbnMgJiYgdnJlLmNvbGxlY3Rpb25zW2RvbWFpbl0gJiYgdnJlLmNvbGxlY3Rpb25zW2RvbWFpbl0ucHJvcGVydGllcykge1xuXHRcdHJldHVybiB2cmUuY29sbGVjdGlvbnNbZG9tYWluXS5wcm9wZXJ0aWVzXG5cdFx0XHQubWFwKChmaWVsZERlZikgPT4gW25hbWVGb3JUeXBlKGZpZWxkRGVmKSwgaW5pdGlhbERhdGFGb3JUeXBlKGZpZWxkRGVmKV0pXG5cdFx0XHQuY29uY2F0KFtbXCJAdHlwZVwiLCBkb21haW4ucmVwbGFjZSgvcyQvLCBcIlwiKV1dKVxuXHRcdFx0LnJlZHVjZSgob2JqLCBjdXIpID0+IHtcblx0XHRcdFx0b2JqW2N1clswXV0gPSBjdXJbMV07XG5cdFx0XHRcdHJldHVybiBvYmo7XG5cdFx0XHR9LCB7fSk7XG5cdH1cbn07XG5cbmNvbnN0IGZldGNoRW50aXR5TGlzdCA9IChkb21haW4pID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IDB9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZG9tYWluLCAwLCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGF9KSk7XG59O1xuXG5jb25zdCBwYWdpbmF0ZUxlZnQgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IG5ld1N0YXJ0ID0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5zdGFydCAtIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cztcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3QgcGFnaW5hdGVSaWdodCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgbmV3U3RhcnQgPSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnN0YXJ0ICsgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogbmV3U3RhcnR9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3Qgc2VuZFF1aWNrU2VhcmNoID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSB9ID0gZ2V0U3RhdGUoKTtcblx0aWYgKHF1aWNrU2VhcmNoLnF1ZXJ5Lmxlbmd0aCkge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiAwfSk7XG5cdFx0Y29uc3QgY2FsbGJhY2sgPSAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhLm1hcCgoZCkgPT4gKFxuXHRcdFx0e1xuXHRcdFx0XHRfaWQ6IGQua2V5LnJlcGxhY2UoLy4qXFwvLywgXCJcIiksXG5cdFx0XHRcdFwiQGRpc3BsYXlOYW1lXCI6IGQudmFsdWVcblx0XHRcdH1cblx0XHQpKX0pO1xuXHRcdGF1dG9jb21wbGV0ZShgZG9tYWluLyR7ZW50aXR5LmRvbWFpbn0vYXV0b2NvbXBsZXRlYCwgcXVpY2tTZWFyY2gucXVlcnksIGNhbGxiYWNrKTtcblx0fSBlbHNlIHtcblx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZW50aXR5LmRvbWFpbikpO1xuXHR9XG59O1xuXG5jb25zdCBzZWxlY3REb21haW4gPSAoZG9tYWluKSA9PiAoZGlzcGF0Y2gpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RPTUFJTlwiLCBkb21haW59KTtcblx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGRvbWFpbikpO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIiwgdmFsdWU6IFwiXCJ9KTtcbn07XG5cbi8vIDEpIEZldGNoIGVudGl0eVxuLy8gMikgRGlzcGF0Y2ggUkVDRUlWRV9FTlRJVFkgZm9yIHJlbmRlclxuY29uc3Qgc2VsZWN0RW50aXR5ID0gKGRvbWFpbiwgZW50aXR5SWQsIGVycm9yTWVzc2FnZSA9IG51bGwsIHN1Y2Nlc3NNZXNzYWdlID0gbnVsbCwgbmV4dCA9ICgpID0+IHsgfSkgPT5cblx0KGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRcdGNvbnN0IHsgZW50aXR5OiB7IGRvbWFpbjogY3VycmVudERvbWFpbiB9IH0gPSBnZXRTdGF0ZSgpO1xuXHRcdGlmIChjdXJyZW50RG9tYWluICE9PSBkb21haW4pIHtcblx0XHRcdGRpc3BhdGNoKHNlbGVjdERvbWFpbihkb21haW4pKTtcblx0XHR9XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQkVGT1JFX0ZFVENIX0VOVElUWVwifSk7XG5cdFx0Y3J1ZC5mZXRjaEVudGl0eShgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke2VudGl0eUlkfWAsIChkYXRhKSA9PiB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLCBkb21haW46IGRvbWFpbiwgZGF0YTogZGF0YSwgZXJyb3JNZXNzYWdlOiBlcnJvck1lc3NhZ2V9KTtcblx0XHRcdGlmIChzdWNjZXNzTWVzc2FnZSAhPT0gbnVsbCkge1xuXHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTVUNDRVNTX01FU1NBR0VcIiwgbWVzc2FnZTogc3VjY2Vzc01lc3NhZ2V9KTtcblx0XHRcdH1cblx0XHR9LCAoKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9GQUlMVVJFXCIsIGVycm9yTWVzc2FnZTogYEZhaWxlZCB0byBmZXRjaCAke2RvbWFpbn0gd2l0aCBJRCAke2VudGl0eUlkfWB9KSk7XG5cdFx0bmV4dCgpO1xuXHR9O1xuXG5cbi8vIDEpIERpc3BhdGNoIFJFQ0VJVkVfRU5USVRZIHdpdGggZW1wdHkgZW50aXR5IHNrZWxldG9uIGZvciByZW5kZXJcbmNvbnN0IG1ha2VOZXdFbnRpdHkgPSAoZG9tYWluLCBlcnJvck1lc3NhZ2UgPSBudWxsKSA9PlxuXHQoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiBkaXNwYXRjaCh7XG5cdFx0dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLFxuXHRcdGRvbWFpbjogZG9tYWluLFxuXHRcdGRhdGE6IG1ha2VTa2VsZXRvbihnZXRTdGF0ZSgpLnZyZSwgZG9tYWluKSB8fCB7fSxcblx0XHRlcnJvck1lc3NhZ2U6IGVycm9yTWVzc2FnZVxuXHR9KTtcblxuY29uc3QgZGVsZXRlRW50aXR5ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjcnVkLmRlbGV0ZUVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLFxuXHRcdCgpID0+IHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNVQ0NFU1NfTUVTU0FHRVwiLCBtZXNzYWdlOiBgU3VjZXNzZnVsbHkgZGVsZXRlZCAke2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbn0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWB9KTtcblx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSk7XG5cdFx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSk7XG5cdFx0fSxcblx0XHQoKSA9PiBkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgYEZhaWxlZCB0byBkZWxldGUgJHtnZXRTdGF0ZSgpLmVudGl0eS5kb21haW59IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gKSkpO1xufTtcblxuLy8gMSkgU2F2ZSBhbiBlbnRpdHlcbi8vIDIpIFNhdmUgdGhlIHJlbGF0aW9ucyBmb3IgdGhpcyBlbnRpdHlcbi8vIDMpIFJlZmV0Y2ggZW50aXR5IGZvciByZW5kZXJcbmNvbnN0IHNhdmVFbnRpdHkgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IGNvbGxlY3Rpb25MYWJlbCA9IGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0uY29sbGVjdGlvbkxhYmVsLnJlcGxhY2UoL3MkLywgXCJcIik7XG5cblx0Ly8gTWFrZSBhIGRlZXAgY29weSBvZiB0aGUgZGF0YSB0byBiZSBzYXZlZCBpbiBvcmRlciB0byBsZWF2ZSBhcHBsaWNhdGlvbiBzdGF0ZSB1bmFsdGVyZWRcblx0bGV0IHNhdmVEYXRhID0gY2xvbmUoZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YSk7XG5cdC8vIE1ha2UgYSBkZWVwIGNvcHkgb2YgdGhlIHJlbGF0aW9uIGRhdGEgaW4gb3JkZXIgdG8gbGVhdmUgYXBwbGljYXRpb24gc3RhdGUgdW5hbHRlcmVkXG5cdGxldCByZWxhdGlvbkRhdGEgPSBjbG9uZShzYXZlRGF0YVtcIkByZWxhdGlvbnNcIl0pIHx8IHt9O1xuXHQvLyBEZWxldGUgdGhlIHJlbGF0aW9uIGRhdGEgZnJvbSB0aGUgc2F2ZURhdGEgYXMgaXQgaXMgbm90IGV4cGVjdGVkIGJ5IHRoZSBzZXJ2ZXJcblx0ZGVsZXRlIHNhdmVEYXRhW1wiQHJlbGF0aW9uc1wiXTtcblxuXHRpZiAoZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQpIHtcblx0XHQvLyAxKSBVcGRhdGUgdGhlIGVudGl0eSB3aXRoIHNhdmVEYXRhXG5cdFx0Y3J1ZC51cGRhdGVFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBzYXZlRGF0YSwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKGVyciwgcmVzcCkgPT5cblx0XHRcdC8vIDIpIFNhdmUgcmVsYXRpb25zIHVzaW5nIHNlcnZlciByZXNwb25zZSBmb3IgY3VycmVudCByZWxhdGlvbnMgdG8gZGlmZiBhZ2FpbnN0IHJlbGF0aW9uRGF0YVxuXHRcdFx0ZGlzcGF0Y2goKHJlZGlzcGF0Y2gpID0+IHNhdmVSZWxhdGlvbnMoSlNPTi5wYXJzZShyZXNwLmJvZHkpLCByZWxhdGlvbkRhdGEsIGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0ucHJvcGVydGllcywgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKCkgPT5cblx0XHRcdFx0Ly8gMykgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuXHRcdFx0XHRyZWRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBudWxsLCBgU3VjY2VzZnVsbHkgc2F2ZWQgJHtjb2xsZWN0aW9uTGFiZWx9IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gLCAoKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSkpKSkpLCAoKSA9PlxuXHRcdFx0XHRcdC8vIDJhKSBIYW5kbGUgZXJyb3IgYnkgcmVmZXRjaGluZyBhbmQgcGFzc2luZyBhbG9uZyBhbiBlcnJvciBtZXNzYWdlXG5cdFx0XHRcdFx0ZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGBGYWlsZWQgdG8gc2F2ZSAke2NvbGxlY3Rpb25MYWJlbH0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWApKSk7XG5cblx0fSBlbHNlIHtcblx0XHQvLyAxKSBDcmVhdGUgbmV3IGVudGl0eSB3aXRoIHNhdmVEYXRhXG5cdFx0Y3J1ZC5zYXZlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgc2F2ZURhdGEsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsIChlcnIsIHJlc3ApID0+XG5cdFx0XHQvLyAyKSBGZXRjaCBlbnRpdHkgdmlhIGxvY2F0aW9uIGhlYWRlclxuXHRcdFx0ZGlzcGF0Y2goKHJlZGlzcGF0Y2gpID0+IGNydWQuZmV0Y2hFbnRpdHkocmVzcC5oZWFkZXJzLmxvY2F0aW9uLCAoZGF0YSkgPT5cblx0XHRcdFx0Ly8gMykgU2F2ZSByZWxhdGlvbnMgdXNpbmcgc2VydmVyIHJlc3BvbnNlIGZvciBjdXJyZW50IHJlbGF0aW9ucyB0byBkaWZmIGFnYWluc3QgcmVsYXRpb25EYXRhXG5cdFx0XHRcdHNhdmVSZWxhdGlvbnMoZGF0YSwgcmVsYXRpb25EYXRhLCBnZXRTdGF0ZSgpLnZyZS5jb2xsZWN0aW9uc1tnZXRTdGF0ZSgpLmVudGl0eS5kb21haW5dLnByb3BlcnRpZXMsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsICgpID0+XG5cdFx0XHRcdFx0Ly8gNCkgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuXHRcdFx0XHRcdHJlZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZGF0YS5faWQsIG51bGwsIGBTdWNjZXNmdWxseSBzYXZlZCAke2NvbGxlY3Rpb25MYWJlbH1gLCAoKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSkpKSkpKSwgKCkgPT5cblx0XHRcdFx0XHRcdC8vIDJhKSBIYW5kbGUgZXJyb3IgYnkgcmVmZXRjaGluZyBhbmQgcGFzc2luZyBhbG9uZyBhbiBlcnJvciBtZXNzYWdlXG5cdFx0XHRcdFx0XHRkaXNwYXRjaChtYWtlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgYEZhaWxlZCB0byBzYXZlIG5ldyAke2NvbGxlY3Rpb25MYWJlbH1gKSkpO1xuXHR9XG59O1xuXG5cbmV4cG9ydCB7IHNhdmVFbnRpdHksIHNlbGVjdEVudGl0eSwgbWFrZU5ld0VudGl0eSwgZGVsZXRlRW50aXR5LCBmZXRjaEVudGl0eUxpc3QsIHBhZ2luYXRlUmlnaHQsIHBhZ2luYXRlTGVmdCwgc2VuZFF1aWNrU2VhcmNoLCBzZWxlY3REb21haW4gfTsiLCJpbXBvcnQgeyBzYXZlRW50aXR5LCBzZWxlY3RFbnRpdHksIG1ha2VOZXdFbnRpdHksIGRlbGV0ZUVudGl0eSwgc2VsZWN0RG9tYWluLCBwYWdpbmF0ZUxlZnQsIHBhZ2luYXRlUmlnaHQsIHNlbmRRdWlja1NlYXJjaCB9IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IHsgc2V0VnJlIH0gZnJvbSBcIi4vdnJlXCI7XG5cbmV4cG9ydCBkZWZhdWx0IChuYXZpZ2F0ZVRvLCBkaXNwYXRjaCkgPT4gKHtcblx0b25OZXc6IChkb21haW4pID0+IGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZG9tYWluKSksXG5cdG9uU2VsZWN0OiAocmVjb3JkKSA9PiBkaXNwYXRjaChzZWxlY3RFbnRpdHkocmVjb3JkLmRvbWFpbiwgcmVjb3JkLmlkKSksXG5cdG9uU2F2ZTogKCkgPT4gZGlzcGF0Y2goc2F2ZUVudGl0eSgpKSxcblx0b25EZWxldGU6ICgpID0+IGRpc3BhdGNoKGRlbGV0ZUVudGl0eSgpKSxcblx0b25DaGFuZ2U6IChmaWVsZFBhdGgsIHZhbHVlKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCIsIGZpZWxkUGF0aDogZmllbGRQYXRoLCB2YWx1ZTogdmFsdWV9KSxcblx0b25Mb2dpbkNoYW5nZTogKHJlc3BvbnNlKSA9PiBkaXNwYXRjaChzZXRVc2VyKHJlc3BvbnNlKSksXG5cdG9uU2VsZWN0VnJlOiAodnJlSWQpID0+IGRpc3BhdGNoKHNldFZyZSh2cmVJZCkpLFxuXHRvbkRpc21pc3NNZXNzYWdlOiAobWVzc2FnZUluZGV4KSA9PiBkaXNwYXRjaCh7dHlwZTogXCJESVNNSVNTX01FU1NBR0VcIiwgbWVzc2FnZUluZGV4OiBtZXNzYWdlSW5kZXh9KSxcblx0b25TZWxlY3REb21haW46IChkb21haW4pID0+IHtcblx0XHRkaXNwYXRjaChzZWxlY3REb21haW4oZG9tYWluKSk7XG5cdH0sXG5cdG9uUGFnaW5hdGVMZWZ0OiAoKSA9PiBkaXNwYXRjaChwYWdpbmF0ZUxlZnQoKSksXG5cdG9uUGFnaW5hdGVSaWdodDogKCkgPT4gZGlzcGF0Y2gocGFnaW5hdGVSaWdodCgpKSxcblx0b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlOiAodmFsdWUpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9RVUlDS1NFQVJDSF9RVUVSWVwiLCB2YWx1ZTogdmFsdWV9KSxcblx0b25RdWlja1NlYXJjaDogKCkgPT4gZGlzcGF0Y2goc2VuZFF1aWNrU2VhcmNoKCkpXG59KTsiLCJpbXBvcnQgeyBzYXZlTmV3RW50aXR5LCB1cGRhdGVFbnRpdHkgfSBmcm9tIFwiLi9jcnVkXCI7XG5cbmNvbnN0IHNhdmVSZWxhdGlvbnNWMjEgPSAoZGF0YSwgcmVsYXRpb25EYXRhLCBmaWVsZERlZnMsIHRva2VuLCB2cmVJZCwgbmV4dCkgPT4ge1xuXHQvLyBSZXR1cm5zIHRoZSBkb21haW4gYmFzZWQgb24gdGhlIGZpZWxkRGVmaW5pdGlvbnMgYW5kIHRoZSByZWxhdGlvbiBrZXkgKGkuZS4gXCJoYXNCaXJ0aFBsYWNlXCIpXG5cdGNvbnN0IG1ha2VSZWxhdGlvbkFyZ3MgPSAocmVsYXRpb24sIGtleSwgYWNjZXB0ZWQgPSB0cnVlLCBpZCA9IG51bGwsIHJldiA9IG51bGwpID0+IHtcblx0XHRjb25zdCBmaWVsZERlZiA9IGZpZWxkRGVmcy5maW5kKChkZWYpID0+IGRlZi5uYW1lID09PSBrZXkpO1xuXG5cblx0XHRjb25zdCBzb3VyY2VUeXBlID0gZGF0YVtcIkB0eXBlXCJdLnJlcGxhY2UoL3MkLywgXCJcIikucmVwbGFjZSgvXnd3LywgXCJcIik7XG5cdFx0Y29uc3QgdGFyZ2V0VHlwZSA9IGZpZWxkRGVmLnJlbGF0aW9uLnRhcmdldENvbGxlY3Rpb24ucmVwbGFjZSgvcyQvLCBcIlwiKS5yZXBsYWNlKC9ed3cvLCBcIlwiKTtcblxuXHRcdGNvbnN0IHJlbGF0aW9uU2F2ZURhdGEgPSB7XG5cdFx0XHRcIkB0eXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uQ29sbGVjdGlvbi5yZXBsYWNlKC9zJC8sIFwiXCIpLCAvLyBjaGVja1xuXHRcdFx0XCJec291cmNlSWRcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyByZWxhdGlvbi5pZCA6IGRhdGEuX2lkLCAvLyBjaGVja1xuXHRcdFx0XCJec291cmNlVHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHRhcmdldFR5cGUgOiBzb3VyY2VUeXBlLCAvLyBjaGVja1xuXHRcdFx0XCJedGFyZ2V0SWRcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyBkYXRhLl9pZCA6IHJlbGF0aW9uLmlkLCAvLyBjaGVja1xuXHRcdFx0XCJedGFyZ2V0VHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHNvdXJjZVR5cGUgOiB0YXJnZXRUeXBlLFxuXHRcdFx0XCJedHlwZUlkXCI6IGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uVHlwZUlkLCAvLyBjaGVja1xuXHRcdFx0YWNjZXB0ZWQ6IGFjY2VwdGVkXG5cdFx0fTtcblxuXHRcdGlmKGlkKSB7IHJlbGF0aW9uU2F2ZURhdGEuX2lkID0gaWQ7IH1cblx0XHRpZihyZXYpIHsgcmVsYXRpb25TYXZlRGF0YVtcIl5yZXZcIl0gPSByZXY7IH1cblx0XHRyZXR1cm4gW1xuXHRcdFx0ZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25Db2xsZWN0aW9uLCAvLyBkb21haW5cblx0XHRcdHJlbGF0aW9uU2F2ZURhdGFcblx0XHRdO1xuXHR9O1xuXG5cdC8vIENvbnN0cnVjdHMgYW4gYXJyYXkgb2YgYXJndW1lbnRzIGZvciBzYXZpbmcgbmV3IHJlbGF0aW9uczpcblx0Ly8gW1xuXHQvLyAgIFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dLFxuXHQvLyAgIFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dLFxuXHQvLyBdXG5cdGNvbnN0IG5ld1JlbGF0aW9ucyA9IE9iamVjdC5rZXlzKHJlbGF0aW9uRGF0YSkubWFwKChrZXkpID0+XG5cdFx0XHRyZWxhdGlvbkRhdGFba2V5XVxuXHRcdFx0Ly8gRmlsdGVycyBvdXQgYWxsIHJlbGF0aW9ucyB3aGljaCBhcmUgbm90IGFscmVhZHkgaW4gZGF0YVtcIkByZWxhdGlvbnNcIl1cblx0XHRcdFx0LmZpbHRlcigocmVsYXRpb24pID0+IChkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldIHx8IFtdKS5tYXAoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmlkKS5pbmRleE9mKHJlbGF0aW9uLmlkKSA8IDApXG5cdFx0XHRcdC8vIE1ha2UgYXJndW1lbnQgYXJyYXkgZm9yIG5ldyByZWxhdGlvbnM6IFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dXG5cdFx0XHRcdC5tYXAoKHJlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKHJlbGF0aW9uLCBrZXkpKVxuXHRcdC8vIEZsYXR0ZW4gbmVzdGVkIGFycmF5c1xuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXG5cdC8vIFJlYWN0aXZhdGUgcHJldmlvdXNseSBhZGRlZCByZWxhdGlvbnMgdXNpbmcgUFVUIHdoaWNoIHdlcmUgJ2RlbGV0ZWQnIGFmdGVyIHVzaW5nIFBVVFxuXHRjb25zdCByZUFkZFJlbGF0aW9ucyA9IE9iamVjdC5rZXlzKHJlbGF0aW9uRGF0YSkubWFwKChrZXkpID0+XG5cdFx0KGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV0gfHwgW10pXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5hY2NlcHRlZCA9PT0gZmFsc2UpXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IChyZWxhdGlvbkRhdGFba2V5XSB8fCBbXSkuZmlsdGVyKChyZWxhdGlvbikgPT4gcmVsYXRpb24uYWNjZXB0ZWQpLm1hcCgocmVsYXRpb24pID0+IHJlbGF0aW9uLmlkKS5pbmRleE9mKG9yaWdSZWxhdGlvbi5pZCkgPiAtMSlcblx0XHRcdC5tYXAoKG9yaWdSZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhvcmlnUmVsYXRpb24sIGtleSwgdHJ1ZSwgb3JpZ1JlbGF0aW9uLnJlbGF0aW9uSWQsIG9yaWdSZWxhdGlvbi5yZXYpKVxuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXHQvLyBEZWFjdGl2YXRlIHByZXZpb3VzbHkgYWRkZWQgcmVsYXRpb25zIHVzaW5nIFBVVFxuXHRjb25zdCBkZWxldGVSZWxhdGlvbnMgPSBPYmplY3Qua2V5cyhkYXRhW1wiQHJlbGF0aW9uc1wiXSkubWFwKChrZXkpID0+XG5cdFx0ZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uYWNjZXB0ZWQpXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IChyZWxhdGlvbkRhdGFba2V5XSB8fCBbXSkubWFwKChyZWxhdGlvbikgPT4gcmVsYXRpb24uaWQpLmluZGV4T2Yob3JpZ1JlbGF0aW9uLmlkKSA8IDApXG5cdFx0XHQubWFwKChvcmlnUmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3Mob3JpZ1JlbGF0aW9uLCBrZXksIGZhbHNlLCBvcmlnUmVsYXRpb24ucmVsYXRpb25JZCwgb3JpZ1JlbGF0aW9uLnJldikpXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cdC8vIENvbWJpbmVzIHNhdmVOZXdFbnRpdHkgYW5kIGRlbGV0ZUVudGl0eSBpbnN0cnVjdGlvbnMgaW50byBwcm9taXNlc1xuXHRjb25zdCBwcm9taXNlcyA9IG5ld1JlbGF0aW9uc1xuXHQvLyBNYXAgbmV3UmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHNhdmVOZXdFbnRpdHlcblx0XHQubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiBzYXZlTmV3RW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSApKVxuXHRcdC8vIE1hcCByZWFkZFJlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiB1cGRhdGVFbnRpdHlcblx0XHQuY29uY2F0KHJlQWRkUmVsYXRpb25zLm1hcCgoYXJncykgPT4gbmV3IFByb21pc2UoKHJlc29sdmUsIHJlamVjdCkgPT4gdXBkYXRlRW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSkpKVxuXHRcdC8vIE1hcCBkZWxldGVSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2YgdXBkYXRlRW50aXR5XG5cdFx0LmNvbmNhdChkZWxldGVSZWxhdGlvbnMubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiB1cGRhdGVFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpKSkpO1xuXG5cdC8vIEludm9rZSBhbGwgQ1JVRCBvcGVyYXRpb25zIGZvciB0aGUgcmVsYXRpb25zXG5cdFByb21pc2UuYWxsKHByb21pc2VzKS50aGVuKG5leHQsIG5leHQpO1xufTtcblxuZXhwb3J0IGRlZmF1bHQgc2F2ZVJlbGF0aW9uc1YyMTsiLCJpbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi4vc3RvcmVcIjtcblxuZXhwb3J0IGRlZmF1bHQge1xuXHRwZXJmb3JtWGhyOiBmdW5jdGlvbiAob3B0aW9ucywgYWNjZXB0LCByZWplY3QgPSAoKSA9PiB7IGNvbnNvbGUud2FybihcIlVuZGVmaW5lZCByZWplY3QgY2FsbGJhY2shIFwiKTsgfSwgb3BlcmF0aW9uID0gXCJTZXJ2ZXIgcmVxdWVzdFwiKSB7XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiUkVRVUVTVF9NRVNTQUdFXCIsIG1lc3NhZ2U6IGAke29wZXJhdGlvbn06ICR7b3B0aW9ucy5tZXRob2QgfHwgXCJHRVRcIn0gJHtvcHRpb25zLnVybH1gfSk7XG5cdFx0eGhyKG9wdGlvbnMsIChlcnIsIHJlc3AsIGJvZHkpID0+IHtcblx0XHRcdGlmKHJlc3Auc3RhdHVzQ29kZSA+PSA0MDApIHtcblx0XHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiRVJST1JfTUVTU0FHRVwiLCBtZXNzYWdlOiBgJHtvcGVyYXRpb259IGZhaWxlZCB3aXRoIGNhdXNlOiAke3Jlc3AuYm9keX1gfSk7XG5cdFx0XHRcdHJlamVjdChlcnIsIHJlc3AsIGJvZHkpO1xuXHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0YWNjZXB0KGVyciwgcmVzcCwgYm9keSk7XG5cdFx0XHR9XG5cdFx0fSk7XG5cdH0sXG5cblx0ZmFzdFhocjogZnVuY3Rpb24ob3B0aW9ucywgYWNjZXB0KSB7XG5cdFx0eGhyKG9wdGlvbnMsIGFjY2VwdCk7XG5cdH0sXG5cblx0bWFrZUhlYWRlcnM6IGZ1bmN0aW9uKHRva2VuLCB2cmVJZCkge1xuXHRcdHJldHVybiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIixcblx0XHRcdFwiQ29udGVudC10eXBlXCI6IFwiYXBwbGljYXRpb24vanNvblwiLFxuXHRcdFx0XCJBdXRob3JpemF0aW9uXCI6IHRva2VuLFxuXHRcdFx0XCJWUkVfSURcIjogdnJlSWRcblx0XHR9O1xuXHR9XG59O1xuIiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2luZGV4XCI7XG5pbXBvcnQge21ha2VOZXdFbnRpdHl9IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IHtmZXRjaEVudGl0eUxpc3R9IGZyb20gXCIuL2VudGl0eVwiO1xuXG5jb25zdCBsaXN0VnJlcyA9ICgpID0+IChkaXNwYXRjaCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIlxuXHRcdH0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvc3lzdGVtL3ZyZXNgXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRkaXNwYXRjaCh7dHlwZTogXCJMSVNUX1ZSRVNcIiwgbGlzdDogSlNPTi5wYXJzZShyZXNwLmJvZHkpfSk7XG5cdH0sIG51bGwsIFwiTGlzdCBWUkVzXCIpO1xuXG5jb25zdCBzZXRWcmUgPSAodnJlSWQsIG5leHQgPSAoKSA9PiB7IH0pID0+IChkaXNwYXRjaCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIlxuXHRcdH0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvbWV0YWRhdGEvJHt2cmVJZH0/d2l0aENvbGxlY3Rpb25JbmZvPXRydWVgXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRpZiAocmVzcC5zdGF0dXNDb2RlID09PSAyMDApIHtcblx0XHRcdHZhciBib2R5ID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1ZSRVwiLCB2cmVJZDogdnJlSWQsIGNvbGxlY3Rpb25zOiBib2R5fSk7XG5cblx0XHRcdGxldCBkZWZhdWx0RG9tYWluID0gT2JqZWN0LmtleXMoYm9keSlcblx0XHRcdFx0Lm1hcChjb2xsZWN0aW9uTmFtZSA9PiBib2R5W2NvbGxlY3Rpb25OYW1lXSlcblx0XHRcdFx0LmZpbHRlcihjb2xsZWN0aW9uID0+ICFjb2xsZWN0aW9uLnVua25vd24gJiYgIWNvbGxlY3Rpb24ucmVsYXRpb25Db2xsZWN0aW9uKVswXVxuXHRcdFx0XHQuY29sbGVjdGlvbk5hbWU7XG5cblx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZGVmYXVsdERvbWFpbikpXG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRE9NQUlOXCIsIGRlZmF1bHREb21haW59KTtcblx0XHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChkZWZhdWx0RG9tYWluKSk7XG5cdFx0XHRuZXh0KCk7XG5cdFx0fVxuXHR9LCAoKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfVlJFXCIsIHZyZUlkOiB2cmVJZCwgY29sbGVjdGlvbnM6IHt9fSksIGBGZXRjaCBWUkUgZGVzY3JpcHRpb24gZm9yICR7dnJlSWR9YCk7XG5cblxuZXhwb3J0IHtsaXN0VnJlcywgc2V0VnJlfTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjbGFzc25hbWVzIGZyb20gXCJjbGFzc25hbWVzXCI7XG5pbXBvcnQge3VybHN9IGZyb20gXCIuLi8uLi91cmxzXCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuXG5jbGFzcyBDb2xsZWN0aW9uVGFicyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0b25Eb21haW5TZWxlY3QoZG9tYWluKSB7XG5cdFx0dGhpcy5wcm9wcy5vbk5ldyhkb21haW4pO1xuXHRcdHRoaXMucHJvcHMub25TZWxlY3REb21haW4oZG9tYWluKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGNvbGxlY3Rpb25zLCBhY3RpdmVEb21haW4gfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgZG9tYWlucyA9IE9iamVjdC5rZXlzKGNvbGxlY3Rpb25zIHx8IHt9KTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXYtdGFic1wiPlxuICAgICAgICAgIHtkb21haW5zXG4gICAgICAgICAgICAuZmlsdGVyKGQgPT4gIShjb2xsZWN0aW9uc1tkXS51bmtub3duIHx8IGNvbGxlY3Rpb25zW2RdLnJlbGF0aW9uQ29sbGVjdGlvbikpXG4gICAgICAgICAgICAubWFwKChkb21haW4pID0+IChcbiAgICAgICAgICAgICAgPGxpIGNsYXNzTmFtZT17Y2xhc3NuYW1lcyh7YWN0aXZlOiBkb21haW4gPT09IGFjdGl2ZURvbWFpbn0pfSBrZXk9e2RvbWFpbn0+XG4gICAgICAgICAgICAgICAgPExpbmsgdG89e3VybHMubmV3RW50aXR5KGRvbWFpbil9PlxuICAgICAgICAgICAgICAgICAge2NvbGxlY3Rpb25zW2RvbWFpbl0uY29sbGVjdGlvbkxhYmVsfVxuICAgICAgICAgICAgICAgIDwvTGluaz5cbiAgICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5Db2xsZWN0aW9uVGFicy5wcm9wVHlwZXMgPSB7XG5cdG9uTmV3OiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZWxlY3REb21haW46IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRjb2xsZWN0aW9uczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0YWN0aXZlRG9tYWluOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFicztcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBQYWdlIGZyb20gXCIuLi9wYWdlLmpzeFwiO1xuXG5pbXBvcnQgUGFnaW5hdGUgZnJvbSBcIi4vZW50aXR5LWluZGV4L3BhZ2luYXRlXCI7XG5pbXBvcnQgUXVpY2tTZWFyY2ggZnJvbSBcIi4vZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoXCI7XG5pbXBvcnQgRW50aXR5TGlzdCBmcm9tIFwiLi9lbnRpdHktaW5kZXgvbGlzdFwiO1xuXG5pbXBvcnQgU2F2ZUZvb3RlciBmcm9tIFwiLi9lbnRpdHktZm9ybS9zYXZlLWZvb3RlclwiO1xuaW1wb3J0IEVudGl0eUZvcm0gZnJvbSBcIi4vZW50aXR5LWZvcm0vZm9ybVwiO1xuXG5pbXBvcnQgQ29sbGVjdGlvblRhYnMgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJzXCI7XG5pbXBvcnQgTWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXMvbGlzdFwiO1xuXG5jbGFzcyBFZGl0R3VpIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGNvbnN0IHsgb25TZWxlY3QsIG9uTmV3LCBvblNlbGVjdERvbWFpbiB9ID0gdGhpcy5wcm9wcztcblxuXHRcdC8vIFRyaWdnZXJzIGZldGNoIGRhdGEgZnJvbSBzZXJ2ZXIgYmFzZWQgb24gaWQgZnJvbSByb3V0ZS5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQgIT09IG5leHRQcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdG9uU2VsZWN0KHtkb21haW46IG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IG5leHRQcm9wcy5wYXJhbXMuaWR9KTtcblx0XHR9IGVsc2UgaWYgKHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24gIT09IG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbikge1xuXHRcdFx0b25OZXcobmV4dFByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKTtcblx0XHRcdG9uU2VsZWN0RG9tYWluKG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbik7XG5cdFx0fVxuXHR9XG5cblx0Y29tcG9uZW50RGlkTW91bnQoKSB7XG5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdHRoaXMucHJvcHMub25TZWxlY3Qoe2RvbWFpbjogdGhpcy5wcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IHRoaXMucHJvcHMucGFyYW1zLmlkfSk7XG5cdFx0fSBlbHNlIGlmICh0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKSB7XG5cdFx0XHR0aGlzLnByb3BzLm9uTmV3KHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pO1xuXHRcdFx0dGhpcy5wcm9wcy5vblNlbGVjdERvbWFpbih0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKTtcblx0XHR9XG5cblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG9uU2VsZWN0LCBvbk5ldywgb25TYXZlLCBvbkRlbGV0ZSwgb25TZWxlY3REb21haW4sIG9uRGlzbWlzc01lc3NhZ2UsIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlLCBvblF1aWNrU2VhcmNoLCBvblBhZ2luYXRlTGVmdCwgb25QYWdpbmF0ZVJpZ2h0IH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgcXVpY2tTZWFyY2gsIGVudGl0eSwgdnJlLCBtZXNzYWdlcyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBjdXJyZW50TW9kZSA9IGVudGl0eS5kb21haW4gJiYgZW50aXR5LmRhdGEuX2lkID8gXCJlZGl0XCIgOiBcIm5ld1wiO1xuXG5cdFx0aWYgKGVudGl0eS5kb21haW4gPT09IG51bGwgfHwgIXZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXSkgeyByZXR1cm4gbnVsbDsgfVxuXHRcdHJldHVybiAoXG5cdFx0XHQ8UGFnZT5cblx0XHRcdFx0PENvbGxlY3Rpb25UYWJzIGNvbGxlY3Rpb25zPXt2cmUuY29sbGVjdGlvbnN9IG9uTmV3PXtvbk5ld30gb25TZWxlY3REb21haW49e29uU2VsZWN0RG9tYWlufVxuXHRcdFx0XHRcdGFjdGl2ZURvbWFpbj17ZW50aXR5LmRvbWFpbn0gLz5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cblx0XHRcdFx0XHQ8TWVzc2FnZXNcblx0XHRcdFx0XHRcdHR5cGVzPXtbXCJTVUNDRVNTX01FU1NBR0VcIiwgXCJFUlJPUl9NRVNTQUdFXCJdfVxuXHRcdFx0XHRcdFx0bWVzc2FnZXM9e21lc3NhZ2VzfVxuXHRcdFx0XHRcdFx0b25EaXNtaXNzTWVzc2FnZT17b25EaXNtaXNzTWVzc2FnZX0gLz5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPlxuXHRcdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtNFwiPlxuXHRcdFx0XHRcdFx0XHQ8UXVpY2tTZWFyY2hcblx0XHRcdFx0XHRcdFx0XHRvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2U9e29uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZX1cblx0XHRcdFx0XHRcdFx0XHRvblF1aWNrU2VhcmNoPXtvblF1aWNrU2VhcmNofVxuXHRcdFx0XHRcdFx0XHRcdHF1ZXJ5PXtxdWlja1NlYXJjaC5xdWVyeX0gLz5cblx0XHRcdFx0XHRcdFx0PEVudGl0eUxpc3Rcblx0XHRcdFx0XHRcdFx0XHRzdGFydD17cXVpY2tTZWFyY2guc3RhcnR9XG5cdFx0XHRcdFx0XHRcdFx0bGlzdD17cXVpY2tTZWFyY2gubGlzdH1cblx0XHRcdFx0XHRcdFx0XHRvblNlbGVjdD17b25TZWxlY3R9XG5cdFx0XHRcdFx0XHRcdFx0ZG9tYWluPXtlbnRpdHkuZG9tYWlufSAvPlxuXHRcdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0XHR7IGVudGl0eS5kb21haW4gPyAoXG5cdFx0XHRcdFx0XHRcdDxFbnRpdHlGb3JtIGN1cnJlbnRNb2RlPXtjdXJyZW50TW9kZX0gZ2V0QXV0b2NvbXBsZXRlVmFsdWVzPXtnZXRBdXRvY29tcGxldGVWYWx1ZXN9XG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5PXtlbnRpdHl9IG9uTmV3PXtvbk5ld30gb25EZWxldGU9e29uRGVsZXRlfSBvbkNoYW5nZT17b25DaGFuZ2V9XG5cdFx0XHRcdFx0XHRcdFx0cHJvcGVydGllcz17dnJlLmNvbGxlY3Rpb25zW2VudGl0eS5kb21haW5dLnByb3BlcnRpZXN9IFxuXHRcdFx0XHRcdFx0XHRcdGVudGl0eUxhYmVsPXt2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0uY29sbGVjdGlvbkxhYmVsLnJlcGxhY2UoL3MkLywgXCJcIikgfSAvPlxuXHRcdFx0XHRcdFx0KSA6IG51bGwgfVxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8L2Rpdj5cblxuXHRcdFx0XHQ8ZGl2IHR5cGU9XCJmb290ZXItYm9keVwiPlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLTRcIj5cblx0XHRcdFx0XHRcdDxQYWdpbmF0ZVxuXHRcdFx0XHRcdFx0XHRzdGFydD17cXVpY2tTZWFyY2guc3RhcnR9XG5cdFx0XHRcdFx0XHRcdGxpc3RMZW5ndGg9e3F1aWNrU2VhcmNoLmxpc3QubGVuZ3RofVxuXHRcdFx0XHRcdFx0XHRyb3dzPXs1MH1cblx0XHRcdFx0XHRcdFx0b25QYWdpbmF0ZUxlZnQ9e29uUGFnaW5hdGVMZWZ0fVxuXHRcdFx0XHRcdFx0XHRvblBhZ2luYXRlUmlnaHQ9e29uUGFnaW5hdGVSaWdodH0gLz5cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC04XCI+XG5cdFx0XHRcdFx0XHQ8U2F2ZUZvb3RlciBvblNhdmU9e29uU2F2ZX0gb25DYW5jZWw9eygpID0+IGN1cnJlbnRNb2RlID09PSBcImVkaXRcIiA/XG5cdFx0XHRcdFx0XHRcdG9uU2VsZWN0KHtkb21haW46IGVudGl0eS5kb21haW4sIGlkOiBlbnRpdHkuZGF0YS5faWR9KSA6IG9uTmV3KGVudGl0eS5kb21haW4pfSAvPlxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PGRpdiB0eXBlPVwiZm9vdGVyLWJvZHlcIj5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L1BhZ2U+XG5cdFx0KVxuXHR9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVkaXRHdWk7XG4iLCJleHBvcnQgZGVmYXVsdCAoY2FtZWxDYXNlKSA9PiBjYW1lbENhc2VcbiAgLnJlcGxhY2UoLyhbQS1aMC05XSkvZywgKG1hdGNoKSA9PiBgICR7bWF0Y2gudG9Mb3dlckNhc2UoKX1gKVxuICAucmVwbGFjZSgvXi4vLCAobWF0Y2gpID0+IG1hdGNoLnRvVXBwZXJDYXNlKCkpO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0geyBuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwiIH07XG5cdH1cblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGlmIChuZXh0UHJvcHMuZW50aXR5LmRhdGEuX2lkICE9PSB0aGlzLnByb3BzLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGlmICh0aGlzLnN0YXRlLm5ld0xhYmVsLmxlbmd0aCA+IDAgJiYgdGhpcy5zdGF0ZS5uZXdVcmwubGVuZ3RoID4gMCkge1xuXHRcdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh7XG5cdFx0XHRcdGxhYmVsOiB0aGlzLnN0YXRlLm5ld0xhYmVsLFxuXHRcdFx0XHR1cmw6IHRoaXMuc3RhdGUubmV3VXJsXG5cdFx0XHR9KSk7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwifSk7XG5cdFx0fVxuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXVxuXHRcdFx0LmZpbHRlcigodmFsKSA9PiB2YWwudXJsICE9PSB2YWx1ZS51cmwpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWUudXJsfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz5cblx0XHRcdFx0XHQ8YSBocmVmPXt2YWx1ZS51cmx9IHRhcmdldD1cIl9ibGFua1wiPlxuXHRcdFx0XHRcdFx0e3ZhbHVlLmxhYmVsfVxuXHRcdFx0XHRcdDwvYT5cblx0XHRcdFx0PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCJ9fT5cblx0XHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2wgcHVsbC1sZWZ0XCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3TGFiZWx9XG5cdFx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkxhYmVsIGZvciB1cmwuLi5cIlxuXHRcdFx0XHRcdFx0c3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXhXaWR0aDogXCI1MCVcIn19IC8+XG5cdFx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sIHB1bGwtbGVmdFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld1VybH1cblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld1VybDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gdGhpcy5vbkFkZCgpIDogZmFsc2V9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIlVybC4uLlwiXG5cdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIG1heFdpZHRoOiBcImNhbGMoNTAlIC0gODBweClcIn19IC8+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuIHB1bGwtbGVmdFwiPlxuXHRcdFx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9PkFkZCBsaW5rPC9idXR0b24+XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHQ8L2Rpdj5cblxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBjbGVhcjogXCJsZWZ0XCJ9fSAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdGNvbnN0cnVjdG9yKHByb3BzKSB7XG5cdFx0c3VwZXIocHJvcHMpO1xuXG5cdFx0dGhpcy5zdGF0ZSA9IHsgbmV3VmFsdWU6IFwiXCIgfTtcblx0fVxuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0aWYgKG5leHRQcm9wcy5lbnRpdHkuZGF0YS5faWQgIT09IHRoaXMucHJvcHMuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh2YWx1ZSkpO1xuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKHZhbCkgPT4gdmFsICE9PSB2YWx1ZSkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZX0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e3ZhbHVlfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdWYWx1ZX1cblx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0b25LZXlQcmVzcz17KGV2KSA9PiBldi5rZXkgPT09IFwiRW50ZXJcIiA/IHRoaXMub25BZGQoZXYudGFyZ2V0LnZhbHVlKSA6IGZhbHNlfVxuXHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiQWRkIGEgdmFsdWUuLi5cIiAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRvbkFkZCh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHZhbHVlKSk7XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigodmFsKSA9PiB2YWwgIT09IHZhbHVlKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57dmFsdWV9PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxTZWxlY3RGaWVsZCBvbkNoYW5nZT17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfSBub0NsZWFyPXt0cnVlfSBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG5cdFx0XHRcdFx0PHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG5cdFx0XHRcdFx0XHRTZWxlY3Qge2xhYmVsLnRvTG93ZXJDYXNlKCl9XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHRcdHtvcHRpb25zLmZpbHRlcigob3B0KSA9PiB2YWx1ZXMuaW5kZXhPZihvcHQpIDwgMCkubWFwKChvcHRpb24pID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9TZWxlY3RGaWVsZD5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5XG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBvbkFkZCgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQoe1xuICAgICAgY29tcG9uZW50czogW3t0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn1dXG4gICAgfSkpO1xuICB9XG5cbiAgb25BZGRDb21wb25lbnQoaXRlbUluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAuY29uY2F0KHt0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn0pXG4gICAgKTtcbiAgfVxuXG4gIG9uUmVtb3ZlQ29tcG9uZW50KGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5maWx0ZXIoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggIT09IGNvbXBvbmVudEluZGV4KVxuICAgICk7XG4gIH1cblxuICBvbkNoYW5nZUNvbXBvbmVudFZhbHVlKGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgsIHZhbHVlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHZhbHVlOiB2YWx1ZX0gOiBjb21wb25lbnRcbiAgICApKTtcbiAgfVxuXG4gIG9uQ2hhbmdlQ29tcG9uZW50VHlwZShpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4LCB0eXBlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHR5cGU6IHR5cGV9IDogY29tcG9uZW50XG4gICAgKSk7XG4gIH1cblxuICBvblJlbW92ZShpdGVtSW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKChuYW1lLCBpZHgpID0+IGlkeCAhPT0gaXRlbUluZGV4KSk7XG4gIH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXG4gICAgY29uc3QgbmFtZUVsZW1lbnRzID0gdmFsdWVzLm1hcCgobmFtZSwgaSkgPT4gKFxuICAgICAgPGRpdiBrZXk9e2Ake25hbWV9LSR7aX1gfSBjbGFzc05hbWU9XCJuYW1lcy1mb3JtIGl0ZW0tZWxlbWVudFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInNtYWxsLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG4gICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKGkpfVxuICAgICAgICAgICAgdHlwZT1cImJ1dHRvblwiPlxuICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgIDxzdHJvbmc+XG4gICAgICAgICAgICB7bmFtZS5jb21wb25lbnRzLm1hcCgoY29tcG9uZW50KSA9PiBjb21wb25lbnQudmFsdWUpLmpvaW4oXCIgXCIpfVxuICAgICAgICAgIDwvc3Ryb25nPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPHVsIGtleT1cImNvbXBvbmVudC1saXN0XCI+XG4gICAgICAgICAge25hbWUuY29tcG9uZW50cy5tYXAoKGNvbXBvbmVudCwgaikgPT4gKFxuICAgICAgICAgICAgPGxpIGtleT17YCR7aX0tJHtqfS1jb21wb25lbnRgfT5cbiAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cFwiIGtleT1cImNvbXBvbmVudC12YWx1ZXNcIj5cbiAgICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICAgICAgICAgICAgPFNlbGVjdEZpZWxkIHZhbHVlPXtjb21wb25lbnQudHlwZX0gbm9DbGVhcj17dHJ1ZX1cbiAgICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWwpID0+IHRoaXMub25DaGFuZ2VDb21wb25lbnRUeXBlKGksIGosIHZhbCl9XG4gICAgICAgICAgICAgICAgICAgIGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cbiAgICAgICAgICAgICAgICAgICAge29wdGlvbnMubWFwKChvcHRpb24pID0+IChcbiAgICAgICAgICAgICAgICAgICAgICA8c3BhbiB2YWx1ZT17b3B0aW9ufSBrZXk9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG4gICAgICAgICAgICAgICAgICAgICkpfVxuICAgICAgICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgICAgICA8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiBrZXk9e2BpbnB1dC0ke2l9LSR7an1gfVxuICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gdGhpcy5vbkNoYW5nZUNvbXBvbmVudFZhbHVlKGksIGosIGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcj17Y29tcG9uZW50LnR5cGV9IHZhbHVlPXtjb21wb25lbnQudmFsdWV9IC8+XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgICAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmVDb21wb25lbnQoaSwgail9ID5cbiAgICAgICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgICAgICAgPC9zcGFuPlxuICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICAgICAgPGJ1dHRvbiBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uQWRkQ29tcG9uZW50KGkpfVxuICAgICAgICAgICAgIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdCBidG4teHMgcHVsbC1yaWdodFwiIHR5cGU9XCJidXR0b25cIj5cbiAgICAgICAgICAgIEFkZCBjb21wb25lbnRcbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICA8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBoZWlnaHQ6IFwiNnB4XCIsIGNsZWFyOiBcInJpZ2h0XCJ9fSAvPlxuICAgICAgPC9kaXY+XG4gICAgKSlcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuICAgICAgICB7bmFtZUVsZW1lbnRzfVxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e3RoaXMub25BZGQuYmluZCh0aGlzKX0+XG4gICAgICAgICAgQWRkIG5hbWVcbiAgICAgICAgPC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXksXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQgeyB1cmxzIH0gZnJvbSBcIi4uLy4uLy4uLy4uL3VybHNcIjtcblxuY2xhc3MgUmVsYXRpb25GaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIHF1ZXJ5OiBcIlwiLFxuICAgICAgc3VnZ2VzdGlvbnM6IFtdLFxuICAgICAgYmx1cklzQmxvY2tlZDogZmFsc2VcbiAgICB9XG4gIH1cblxuICBvblJlbW92ZSh2YWx1ZSkge1xuICAgIGNvbnN0IGN1cnJlbnRWYWx1ZXMgPSB0aGlzLnByb3BzLmVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuXG4gICAgdGhpcy5wcm9wcy5vbkNoYW5nZShcbiAgICAgIFtcIkByZWxhdGlvbnNcIiwgdGhpcy5wcm9wcy5uYW1lXSxcbiAgICAgIGN1cnJlbnRWYWx1ZXMuZmlsdGVyKChjdXJWYWwpID0+IGN1clZhbC5pZCAhPT0gdmFsdWUuaWQpXG4gICAgKTtcblxuICB9XG5cbiAgb25BZGQoc3VnZ2VzdGlvbikge1xuICAgIGNvbnN0IGN1cnJlbnRWYWx1ZXMgPSB0aGlzLnByb3BzLmVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuICAgIGlmIChjdXJyZW50VmFsdWVzLm1hcCgodmFsKSA9PiB2YWwuaWQpLmluZGV4T2Yoc3VnZ2VzdGlvbi5rZXkpID4gLTEpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG4gICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IFtdLCBxdWVyeTogXCJcIiwgYmx1cklzQmxvY2tlZDogZmFsc2V9KTtcblxuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoXG4gICAgICBbXCJAcmVsYXRpb25zXCIsIHRoaXMucHJvcHMubmFtZV0sXG4gICAgICBjdXJyZW50VmFsdWVzLmNvbmNhdCh7XG4gICAgICAgIGlkOiBzdWdnZXN0aW9uLmtleSxcbiAgICAgICAgZGlzcGxheU5hbWU6IHN1Z2dlc3Rpb24udmFsdWUsXG4gICAgICAgIGFjY2VwdGVkOiB0cnVlXG4gICAgICB9KVxuICAgICk7XG4gIH1cblxuICBvblF1ZXJ5Q2hhbmdlKGV2KSB7XG4gICAgY29uc3QgeyBnZXRBdXRvY29tcGxldGVWYWx1ZXMsIHBhdGggfSA9IHRoaXMucHJvcHM7XG4gICAgdGhpcy5zZXRTdGF0ZSh7cXVlcnk6IGV2LnRhcmdldC52YWx1ZX0pO1xuICAgIGlmIChldi50YXJnZXQudmFsdWUgPT09IFwiXCIpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXX0pO1xuICAgIH0gZWxzZSB7XG4gICAgICBnZXRBdXRvY29tcGxldGVWYWx1ZXMocGF0aCwgZXYudGFyZ2V0LnZhbHVlLCAocmVzdWx0cykgPT4ge1xuICAgICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogcmVzdWx0c30pO1xuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgb25RdWVyeUNsZWFyKGV2KSB7XG4gICAgaWYgKCF0aGlzLnN0YXRlLmJsdXJJc0Jsb2NrZWQpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXSwgcXVlcnk6IFwiXCJ9KTtcbiAgICB9XG4gIH1cblxuICBvbkJsdXJCbG9jayh0b2dnbGUpIHtcbiAgICB0aGlzLnNldFN0YXRlKHtibHVySXNCbG9ja2VkOiB0b2dnbGV9KTtcbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgdmFsdWVzID0gZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdW3RoaXMucHJvcHMubmFtZV0gfHwgW107XG4gICAgY29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLmZpbHRlcigodmFsKSA9PiB2YWwuYWNjZXB0ZWQpLm1hcCgodmFsdWUsIGkpID0+IChcbiAgICAgIDxkaXYga2V5PXtgJHtpfS0ke3ZhbHVlLmlkfWB9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuICAgICAgICA8TGluayB0bz17dmFsdWUucGF0aC5yZXBsYWNlKC9eZG9tYWluXFwvLywgXCJcIil9ID57dmFsdWUuZGlzcGxheU5hbWV9PC9MaW5rPlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuICAgICAgPC9kaXY+XG4gICAgKSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgPGg0PntjYW1lbDJsYWJlbChuYW1lKX08L2g0PlxuICAgICAgICB7aXRlbUVsZW1lbnRzfVxuICAgICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgICAgICAgIG9uQmx1cj17dGhpcy5vblF1ZXJ5Q2xlYXIuYmluZCh0aGlzKX1cbiAgICAgICAgICAgICAgIG9uQ2hhbmdlPXt0aGlzLm9uUXVlcnlDaGFuZ2UuYmluZCh0aGlzKX1cbiAgICAgICAgICAgICAgIHZhbHVlPXt0aGlzLnN0YXRlLnF1ZXJ5fSBwbGFjZWhvbGRlcj1cIlNlYXJjaC4uLlwiIC8+XG5cbiAgICAgICAgPGRpdiBvbk1vdXNlT3Zlcj17KCkgPT4gdGhpcy5vbkJsdXJCbG9jayh0cnVlKX1cbiAgICAgICAgICAgICBvbk1vdXNlT3V0PXsoKSA9PiB0aGlzLm9uQmx1ckJsb2NrKGZhbHNlKX1cbiAgICAgICAgICAgICBzdHlsZT17e292ZXJmbG93WTogXCJhdXRvXCIsIG1heEhlaWdodDogXCIzMDBweFwifX0+XG4gICAgICAgICAge3RoaXMuc3RhdGUuc3VnZ2VzdGlvbnMubWFwKChzdWdnZXN0aW9uLCBpKSA9PiAoXG4gICAgICAgICAgICA8YSBrZXk9e2Ake2l9LSR7c3VnZ2VzdGlvbi5rZXl9YH0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCJcbiAgICAgICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vbkFkZChzdWdnZXN0aW9uKX0+XG4gICAgICAgICAgICAgIHtzdWdnZXN0aW9uLnZhbHVlfVxuICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgUmVsYXRpb25GaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50ID0gZW50aXR5LmRhdGFbbmFtZV0gJiYgZW50aXR5LmRhdGFbbmFtZV0ubGVuZ3RoID4gMCA/IChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e2VudGl0eS5kYXRhW25hbWVdfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IG9uQ2hhbmdlKFtuYW1lXSwgXCJcIil9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpIDogbnVsbDtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudH1cblx0XHRcdFx0PFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25DaGFuZ2UoW25hbWVdLCB2YWx1ZSl9XG5cdFx0XHRcdFx0bm9DbGVhcj17dHJ1ZX0gYnRuQ2xhc3M9XCJidG4tZGVmYXVsdFwiPlxuXHRcdFx0XHRcdDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPlxuXHRcdFx0XHRcdFx0U2VsZWN0IHtsYWJlbC50b0xvd2VyQ2FzZSgpfVxuXHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0XHR7b3B0aW9ucy5tYXAoKG9wdGlvbikgPT4gKFxuXHRcdFx0XHRcdFx0PHNwYW4ga2V5PXtvcHRpb259IHZhbHVlPXtvcHRpb259PntvcHRpb259PC9zcGFuPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHQ8L1NlbGVjdEZpZWxkPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXlcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIFN0cmluZ0ZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0PGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gb25DaGFuZ2UoW25hbWVdLCBldi50YXJnZXQudmFsdWUpfVxuXHRcdFx0XHRcdHZhbHVlPXtlbnRpdHkuZGF0YVtuYW1lXSB8fCBcIlwifVxuXHRcdFx0XHRcdHBsYWNlaG9sZGVyPXtgRW50ZXIgJHtsYWJlbC50b0xvd2VyQ2FzZSgpfWB9XG5cdFx0XHRcdC8+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cblN0cmluZ0ZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFN0cmluZ0ZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiXG5cbmltcG9ydCBTdHJpbmdGaWVsZCBmcm9tIFwiLi9maWVsZHMvc3RyaW5nLWZpZWxkXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL3NlbGVjdFwiO1xuaW1wb3J0IE11bHRpU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL211bHRpLXNlbGVjdFwiO1xuaW1wb3J0IFJlbGF0aW9uRmllbGQgZnJvbSBcIi4vZmllbGRzL3JlbGF0aW9uXCI7XG5pbXBvcnQgU3RyaW5nTGlzdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9saXN0LW9mLXN0cmluZ3NcIjtcbmltcG9ydCBMaW5rRmllbGQgZnJvbSBcIi4vZmllbGRzL2xpbmtzXCI7XG5pbXBvcnQgTmFtZXNGaWVsZCBmcm9tIFwiLi9maWVsZHMvbmFtZXNcIjtcblxuY29uc3QgZmllbGRNYXAgPSB7XG5cdFwic3RyaW5nXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwidGV4dFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcImRhdGFibGVcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJtdWx0aXNlbGVjdFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPE11bHRpU2VsZWN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPiksXG5cdFwic2VsZWN0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U2VsZWN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPiksXG5cdFwicmVsYXRpb25cIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxSZWxhdGlvbkZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gcGF0aD17ZmllbGREZWYucXVpY2tzZWFyY2h9IC8+KSxcbiAgXCJsaXN0LW9mLXN0cmluZ3NcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdMaXN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG4gIFwibGlua3NcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxMaW5rRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwibmFtZXNcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxOYW1lc0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gb3B0aW9ucz17ZmllbGREZWYub3B0aW9uc30gLz4pXG59O1xuXG5jbGFzcyBFbnRpdHlGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbk5ldywgb25EZWxldGUsIG9uQ2hhbmdlLCBnZXRBdXRvY29tcGxldGVWYWx1ZXMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBlbnRpdHksIGN1cnJlbnRNb2RlLCBwcm9wZXJ0aWVzLCBlbnRpdHlMYWJlbCB9ID0gdGhpcy5wcm9wcztcblxuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLThcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tcHJpbWFyeSBwdWxsLXJpZ2h0XCIgb25DbGljaz17KCkgPT4gb25OZXcoZW50aXR5LmRvbWFpbil9PlxuICAgICAgICAgICAgTmV3IHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+ICFmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT4gKDxkaXYga2V5PXtpfSBzdHlsZT17e1wiY29sb3JcIjogXCJyZWRcIn19PjxzdHJvbmc+RmllbGQgdHlwZSBub3Qgc3VwcG9ydGVkOiB7ZmllbGREZWYudHlwZX08L3N0cm9uZz48L2Rpdj4pKX1cbiAgICAgICAge3Byb3BlcnRpZXNcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gZmllbGRNYXAuaGFzT3duUHJvcGVydHkoZmllbGREZWYudHlwZSkpXG4gICAgICAgICAgLm1hcCgoZmllbGREZWYsIGkpID0+XG4gICAgICAgICAgZmllbGRNYXBbZmllbGREZWYudHlwZV0oZmllbGREZWYsIHtcblx0XHRcdFx0XHRcdGtleTogYCR7aX0tJHtmaWVsZERlZi5uYW1lfWAsXG5cdFx0XHRcdFx0XHRlbnRpdHk6IGVudGl0eSxcblx0XHRcdFx0XHRcdG9uQ2hhbmdlOiBvbkNoYW5nZSxcblx0XHRcdFx0XHRcdGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzXG5cdFx0XHRcdFx0fSlcbiAgICAgICAgKX1cbiAgICAgICAge2N1cnJlbnRNb2RlID09PSBcImVkaXRcIlxuICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAgICAgIDxoND5EZWxldGU8L2g0PlxuICAgICAgICAgICAgICA8YnV0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kYW5nZXJcIiBvbkNsaWNrPXtvbkRlbGV0ZX0+XG4gICAgICAgICAgICAgICAgRGVsZXRlIHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICAgICAgPC9idXRvbj5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVudGl0eUZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25TYXZlLCBvbkNhbmNlbCB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2PlxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnlcIiBvbkNsaWNrPXtvblNhdmV9PlNhdmU8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn1vcntcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1saW5rXCIgb25DbGljaz17b25DYW5jZWx9PkNhbmNlbDwvYnV0dG9uPlxuICAgIDwvZGl2PlxuICApO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vdXJsc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IHN0YXJ0LCBsaXN0LCBkb21haW4gfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJyZXN1bHQtbGlzdCByZXN1bHQtbGlzdC1lZGl0XCI+XG4gICAgICA8b2wgc3RhcnQ9e3N0YXJ0ICsgMX0gc3R5bGU9e3tjb3VudGVyUmVzZXQ6IGBzdGVwLWNvdW50ZXIgJHtzdGFydH1gfX0+XG4gICAgICAgIHtsaXN0Lm1hcCgoZW50cnksIGkpID0+IChcbiAgICAgICAgICA8bGkga2V5PXtgJHtpfS0ke2VudHJ5Ll9pZH1gfT5cbiAgICAgICAgICAgIDxMaW5rIHRvPXt1cmxzLmVudGl0eShkb21haW4sIGVudHJ5Ll9pZCl9IHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgd2lkdGg6IFwiY2FsYygxMDAlIC0gMzBweClcIiwgaGVpZ2h0OiBcIjEwMCVcIiwgcGFkZGluZzogXCIwLjVlbSAwXCJ9fT5cbiAgICAgICAgICAgICAge2VudHJ5W1wiQGRpc3BsYXlOYW1lXCJdfVxuICAgICAgICAgICAgPC9MaW5rPlxuICAgICAgICAgIDwvbGk+XG4gICAgICAgICkpfVxuICAgICAgPC9vbD5cbiAgICA8L2Rpdj5cbiAgKVxufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IG9uUGFnaW5hdGVMZWZ0LCBvblBhZ2luYXRlUmlnaHQgfSA9IHByb3BzO1xuICBjb25zdCB7IHN0YXJ0LCByb3dzLCBsaXN0TGVuZ3RoIH0gPSBwcm9wcztcblxuXG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2PlxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17c3RhcnQgPT09IDB9IG9uQ2xpY2s9e29uUGFnaW5hdGVMZWZ0fT5cbiAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1jaGV2cm9uLWxlZnRcIiAvPlxuICAgICAgPC9idXR0b24+XG4gICAgICB7XCIgXCJ9e3N0YXJ0ICsgMX0gLSB7c3RhcnQgKyByb3dzfXtcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgZGlzYWJsZWQ9e2xpc3RMZW5ndGggPCByb3dzfSBvbkNsaWNrPXtvblBhZ2luYXRlUmlnaHR9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWNoZXZyb24tcmlnaHRcIiAvPlxuICAgICAgPC9idXR0b24+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlLCBvblF1aWNrU2VhcmNoLCBxdWVyeSB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwIHNtYWxsLW1hcmdpbiBcIj5cbiAgICAgIDxpbnB1dCB0eXBlPVwidGV4dFwiIHBsYWNlaG9sZGVyPVwiU2VhcmNoIGZvci4uLlwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG4gICAgICAgIG9uQ2hhbmdlPXsoZXYpID0+IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZShldi50YXJnZXQudmFsdWUpfVxuICAgICAgICBvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gb25RdWlja1NlYXJjaCgpIDogZmFsc2V9XG4gICAgICAgIHZhbHVlPXtxdWVyeX1cbiAgICAgICAgLz5cbiAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e29uUXVpY2tTZWFyY2h9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tc2VhcmNoXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFua1wiIG9uQ2xpY2s9eygpID0+IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlKFwiXCIpOyBvblF1aWNrU2VhcmNoKCk7IH19PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICA8L3NwYW4+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcbmltcG9ydCBNZXNzYWdlIGZyb20gXCIuLi8uLi9tZXNzYWdlXCI7XG5cbmNvbnN0IExBQkVMUyA9IHtcblx0XCJTVUNDRVNTX01FU1NBR0VcIjogXCJcIixcblx0XCJFUlJPUl9NRVNTQUdFXCI6IChcblx0XHQ8c3Bhbj5cblx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tZXhjbGFtYXRpb24tc2lnblwiIC8+IFdhcm5pbmchXG5cdFx0PC9zcGFuPlxuXHQpXG59O1xuXG5jb25zdCBBTEVSVF9MRVZFTFMgPSB7XG5cdFwiU1VDQ0VTU19NRVNTQUdFXCI6IFwiaW5mb1wiLFxuXHRcIkVSUk9SX01FU1NBR0VcIjogXCJkYW5nZXJcIlxufTtcblxuY2xhc3MgTWVzc2FnZXMgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBtZXNzYWdlcywgdHlwZXMsIG9uRGlzbWlzc01lc3NhZ2UgfSA9IHRoaXMucHJvcHM7XG5cblx0XHRjb25zdCBmaWx0ZXJlZE1lc3NhZ2VzID0gbWVzc2FnZXMubG9nXG5cdFx0XHQubWFwKChtc2csIGlkeCkgPT4gKHttZXNzYWdlOiBtc2cubWVzc2FnZSwgaW5kZXg6IGlkeCwgdHlwZTogbXNnLnR5cGUsIGRpc21pc3NlZDogbXNnLmRpc21pc3NlZCB9KSlcblx0XHRcdC5maWx0ZXIoKG1zZykgPT4gdHlwZXMuaW5kZXhPZihtc2cudHlwZSkgPiAtMSAmJiAhbXNnLmRpc21pc3NlZCk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdj5cblx0XHRcdFx0e2ZpbHRlcmVkTWVzc2FnZXMubWFwKChtc2cpID0+IChcblx0XHRcdFx0XHQ8TWVzc2FnZSBrZXk9e21zZy5pbmRleH1cblx0XHRcdFx0XHRcdGRpc21pc3NpYmxlPXt0cnVlfVxuXHRcdFx0XHRcdFx0YWxlcnRMZXZlbD17QUxFUlRfTEVWRUxTW21zZy50eXBlXX1cblx0XHRcdFx0XHRcdG9uQ2xvc2VNZXNzYWdlPXsoKSA9PiBvbkRpc21pc3NNZXNzYWdlKG1zZy5pbmRleCl9PlxuXHRcdFx0XHRcdFx0PHN0cm9uZz57TEFCRUxTW21zZy50eXBlXX08L3N0cm9uZz4gPHNwYW4+e21zZy5tZXNzYWdlfTwvc3Bhbj5cblx0XHRcdFx0XHQ8L01lc3NhZ2U+XG5cdFx0XHRcdCkpfVxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5NZXNzYWdlcy5wcm9wVHlwZXMgPSB7XG5cdG1lc3NhZ2VzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRvbkRpc21pc3NNZXNzYWdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYy5pc1JlcXVpcmVkLFxuXHR0eXBlczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LmlzUmVxdWlyZWRcbn07XG5cbmV4cG9ydCBkZWZhdWx0IE1lc3NhZ2VzO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5jbGFzcyBTZWxlY3RGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIGlzT3BlbjogZmFsc2VcbiAgICB9O1xuICAgIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyID0gdGhpcy5oYW5kbGVEb2N1bWVudENsaWNrLmJpbmQodGhpcyk7XG4gIH1cblxuICBjb21wb25lbnREaWRNb3VudCgpIHtcbiAgICBkb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiY2xpY2tcIiwgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIsIGZhbHNlKTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgIGRvY3VtZW50LnJlbW92ZUV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgdG9nZ2xlU2VsZWN0KCkge1xuICAgIGlmKHRoaXMuc3RhdGUuaXNPcGVuKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtpc09wZW46IGZhbHNlfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogdHJ1ZX0pO1xuICAgIH1cbiAgfVxuXG4gIGhhbmRsZURvY3VtZW50Q2xpY2soZXYpIHtcbiAgICBjb25zdCB7IGlzT3BlbiB9ID0gdGhpcy5zdGF0ZTtcbiAgICBpZiAoaXNPcGVuICYmICFSZWFjdERPTS5maW5kRE9NTm9kZSh0aGlzKS5jb250YWlucyhldi50YXJnZXQpKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtcbiAgICAgICAgaXNPcGVuOiBmYWxzZVxuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25DaGFuZ2UsIG9uQ2xlYXIsIHZhbHVlLCBidG5DbGFzcywgbm9DbGVhciB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHNlbGVjdGVkT3B0aW9uID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnZhbHVlID09PSB2YWx1ZSk7XG4gICAgY29uc3QgcGxhY2Vob2xkZXIgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudHlwZSA9PT0gXCJwbGFjZWhvbGRlclwiKTtcbiAgICBjb25zdCBvdGhlck9wdGlvbnMgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgJiYgb3B0LnByb3BzLnZhbHVlICE9PSB2YWx1ZSk7XG5cbiAgICByZXR1cm4gKFxuXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17Y3goXCJkcm9wZG93blwiLCB7b3BlbjogdGhpcy5zdGF0ZS5pc09wZW59KX0+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPXtjeChcImJ0blwiLCBcImRyb3Bkb3duLXRvZ2dsZVwiLCBidG5DbGFzcyB8fCBcImJ0bi1ibGFua1wiKX0gb25DbGljaz17dGhpcy50b2dnbGVTZWxlY3QuYmluZCh0aGlzKX0+XG4gICAgICAgICAge3NlbGVjdGVkT3B0aW9uLmxlbmd0aCA/IHNlbGVjdGVkT3B0aW9uIDogcGxhY2Vob2xkZXJ9IDxzcGFuIGNsYXNzTmFtZT1cImNhcmV0XCIgLz5cbiAgICAgICAgPC9idXR0b24+XG5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cImRyb3Bkb3duLW1lbnVcIj5cbiAgICAgICAgICB7IHZhbHVlICYmICFub0NsZWFyID8gKFxuICAgICAgICAgICAgPGxpPlxuICAgICAgICAgICAgICA8YSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2xlYXIoKTsgdGhpcy50b2dnbGVTZWxlY3QoKTt9fT5cbiAgICAgICAgICAgICAgICAtIGNsZWFyIC1cbiAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApIDogbnVsbH1cbiAgICAgICAgICB7b3RoZXJPcHRpb25zLm1hcCgob3B0aW9uLCBpKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtpfT5cbiAgICAgICAgICAgICAgPGEgc3R5bGU9e3tjdXJzb3I6IFwicG9pbnRlclwifX0gb25DbGljaz17KCkgPT4geyBvbkNoYW5nZShvcHRpb24ucHJvcHMudmFsdWUpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpOyB9fT57b3B0aW9ufTwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cblNlbGVjdEZpZWxkLnByb3BUeXBlcyA9IHtcbiAgb25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICBvbkNsZWFyOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgdmFsdWU6IFJlYWN0LlByb3BUeXBlcy5hbnksXG4gIGJ0bkNsYXNzOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBub0NsZWFyOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgU2VsZWN0RmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmZ1bmN0aW9uIEZvb3Rlcihwcm9wcykge1xuICBjb25zdCBoaUxvZ28gPSAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMSBjb2wtbWQtMVwiPlxuICAgICAgPGltZyBjbGFzc05hbWU9XCJoaS1sb2dvXCIgc3JjPVwiaW1hZ2VzL2xvZ28taHV5Z2Vucy1pbmcuc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBjbGFyaWFoTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1jbGFyaWFoLnN2Z1wiIC8+XG4gICAgPC9kaXY+XG4gICk7XG5cbiAgY29uc3QgZm9vdGVyQm9keSA9IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSA+IDAgP1xuICAgIFJlYWN0LkNoaWxkcmVuLm1hcChwcm9wcy5jaGlsZHJlbiwgKGNoaWxkLCBpKSA9PiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIHtpID09PSBSZWFjdC5DaGlsZHJlbi5jb3VudChwcm9wcy5jaGlsZHJlbikgLSAxXG4gICAgICAgICAgICA/ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPntoaUxvZ299PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+e2NoaWxkfTwvZGl2PntjbGFyaWFoTG9nb308L2Rpdj4pXG4gICAgICAgICAgICA6ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPntjaGlsZH08L2Rpdj4pXG4gICAgICAgICAgfVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICkpIDogKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJ3aGl0ZS1iYXJcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPlxuICAgICAgICAgICAge2hpTG9nb31cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEwIGNvbC1tZC0xMCB0ZXh0LWNlbnRlclwiPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICB7Y2xhcmlhaExvZ299XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcblxuXG4gIHJldHVybiAoXG4gICAgPGZvb3RlciBjbGFzc05hbWU9XCJmb290ZXJcIj5cbiAgICAgIHtmb290ZXJCb2R5fVxuICAgIDwvZm9vdGVyPlxuICApXG59XG5cbmV4cG9ydCBkZWZhdWx0IEZvb3RlcjsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBkaXNtaXNzaWJsZSwgYWxlcnRMZXZlbCwgb25DbG9zZU1lc3NhZ2V9ID0gcHJvcHM7XG4gIGNvbnN0IGRpc21pc3NCdXR0b24gPSBkaXNtaXNzaWJsZVxuICAgID8gPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3NOYW1lPVwiY2xvc2VcIiBvbkNsaWNrPXtvbkNsb3NlTWVzc2FnZX0+PHNwYW4+JnRpbWVzOzwvc3Bhbj48L2J1dHRvbj5cbiAgICA6IG51bGw7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT17Y3goXCJhbGVydFwiLCBgYWxlcnQtJHthbGVydExldmVsfWAsIHtcImFsZXJ0LWRpc21pc3NpYmxlXCI6IGRpc21pc3NpYmxlfSl9IHJvbGU9XCJhbGVydFwiPlxuICAgICAge2Rpc21pc3NCdXR0b259XG4gICAgICB7cHJvcHMuY2hpbGRyZW59XG4gICAgPC9kaXY+XG4gIClcbn07IiwiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBGb290ZXIgZnJvbSBcIi4vZm9vdGVyXCI7XG5cbmNvbnN0IEZPT1RFUl9IRUlHSFQgPSA4MTtcblxuZnVuY3Rpb24gUGFnZShwcm9wcykge1xuICBjb25zdCBmb290ZXJzID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheShwcm9wcy5jaGlsZHJlbikuZmlsdGVyKChjaGlsZCkgPT4gY2hpbGQucHJvcHMudHlwZSA9PT0gXCJmb290ZXItYm9keVwiKTtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwicGFnZVwiPlxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW4gaGktR3JlZW4gY29udGFpbmVyLWZsdWlkXCI+XG4gICAgICAgIDxuYXYgY2xhc3NOYW1lPVwibmF2YmFyIFwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cIm5hdmJhci1oZWFkZXJcIj4gPGEgY2xhc3NOYW1lPVwibmF2YmFyLWJyYW5kXCIgaHJlZj1cIiNcIj48aW1nIHNyYz1cImltYWdlcy9sb2dvLXRpbWJ1Y3Rvby5zdmdcIiBjbGFzc05hbWU9XCJsb2dvXCIgYWx0PVwidGltYnVjdG9vXCIvPjwvYT4gPC9kaXY+XG4gICAgICAgICAgICA8ZGl2IGlkPVwibmF2YmFyXCIgY2xhc3NOYW1lPVwibmF2YmFyLWNvbGxhcHNlIGNvbGxhcHNlXCI+XG4gICAgICAgICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2YmFyLW5hdiBuYXZiYXItcmlnaHRcIj5cbiAgICAgICAgICAgICAgICB7cHJvcHMudXNlcm5hbWUgPyA8bGk+PGEgaHJlZj17cHJvcHMudXNlcmxvY2F0aW9uIHx8ICcjJ30+PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi11c2VyXCIvPiB7cHJvcHMudXNlcm5hbWV9PC9hPjwvbGk+IDogbnVsbH1cbiAgICAgICAgICAgICAgPC91bD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L25hdj5cbiAgICAgIDwvZGl2PlxuICAgICAgPGRpdiAgc3R5bGU9e3ttYXJnaW5Cb3R0b206IGAke0ZPT1RFUl9IRUlHSFQgKiBmb290ZXJzLmxlbmd0aH1weGB9fT5cbiAgICAgICAge1JlYWN0LkNoaWxkcmVuLnRvQXJyYXkocHJvcHMuY2hpbGRyZW4pLmZpbHRlcigoY2hpbGQpID0+IGNoaWxkLnByb3BzLnR5cGUgIT09IFwiZm9vdGVyLWJvZHlcIil9XG4gICAgICA8L2Rpdj5cbiAgICAgIDxGb290ZXI+XG4gICAgICAgIHtmb290ZXJzfVxuICAgICAgPC9Gb290ZXI+XG4gICAgPC9kaXY+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IFBhZ2U7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuL3N0b3JlXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9hY3Rpb25zXCI7XG5pbXBvcnQge3NldFZyZX0gZnJvbSBcIi4vYWN0aW9ucy92cmVcIjtcbmltcG9ydCBBcHAgZnJvbSBcIi4vY29tcG9uZW50cy9lZGl0LWd1aS9lZGl0LWd1aVwiO1xuaW1wb3J0IGdldEF1dG9jb21wbGV0ZVZhbHVlcyBmcm9tIFwiLi9hY3Rpb25zL2F1dG9jb21wbGV0ZVwiO1xuXG5pbXBvcnQgcm91dGVyIGZyb20gXCIuL3JvdXRlclwiO1xuXG5jb25zdCBzZXRVc2VyID0gKHJlc3BvbnNlKSA9PiB7XG5cdHJldHVybiB7XG5cdFx0dHlwZTogXCJTRVRfVVNFUlwiLFxuXHRcdHVzZXI6IHJlc3BvbnNlXG5cdH07XG59O1xuXG5kb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiRE9NQ29udGVudExvYWRlZFwiLCAoKSA9PiB7XG5cblx0ZnVuY3Rpb24gaW5pdFJvdXRlcigpIHtcblx0XHRSZWFjdERPTS5yZW5kZXIocm91dGVyLCBkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKSk7XG5cdH1cblxuXHRmdW5jdGlvbiBnZXRWcmVJZCgpIHtcblx0XHRsZXQgcGF0aCA9IHdpbmRvdy5sb2NhdGlvbi5zZWFyY2guc3Vic3RyKDEpO1xuXHRcdGxldCBwYXJhbXMgPSBwYXRoLnNwbGl0KFwiJlwiKTtcblxuXHRcdGZvcihsZXQgaSBpbiBwYXJhbXMpIHtcblx0XHRcdGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoXCI9XCIpO1xuXHRcdFx0aWYoa2V5ID09PSBcInZyZUlkXCIpIHtcblx0XHRcdFx0cmV0dXJuIHZhbHVlO1xuXHRcdFx0fVxuXHRcdH1cblx0XHRyZXR1cm4gXCJXb21lbldyaXRlcnNcIjtcblx0fVxuXG5cdGZ1bmN0aW9uIGdldExvZ2luKCkge1xuXHRcdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdFx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdFx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdFx0bGV0IFtrZXksIHZhbHVlXSA9IHBhcmFtc1tpXS5zcGxpdChcIj1cIik7XG5cdFx0XHRpZihrZXkgPT09IFwiaHNpZFwiKSB7XG5cdFx0XHRcdHJldHVybiB7dXNlcjogdmFsdWUsIHRva2VuOiB2YWx1ZX07XG5cdFx0XHR9XG5cdFx0fVxuXHRcdHJldHVybiB1bmRlZmluZWQ7XG5cdH1cblx0c3RvcmUuZGlzcGF0Y2goc2V0VnJlKGdldFZyZUlkKCksIGluaXRSb3V0ZXIpKTtcblx0c3RvcmUuZGlzcGF0Y2goc2V0VXNlcihnZXRMb2dpbigpKSk7XG59KTsiLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5cbmxldCBpbml0aWFsU3RhdGUgPSB7XG5cdGRhdGE6IHtcblx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0fSxcblx0ZG9tYWluOiBudWxsLFxuXHRlcnJvck1lc3NhZ2U6IG51bGxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblxuXHRcdGNhc2UgXCJCRUZPUkVfRkVUQ0hfRU5USVRZXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IHtcblx0XHRcdFx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0XHRcdFx0fVxuXHRcdFx0fX07XG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IGFjdGlvbi5kYXRhLFxuXHRcdFx0XHRkb21haW46IGFjdGlvbi5kb21haW4sXG5cdFx0XHRcdGVycm9yTWVzc2FnZTogYWN0aW9uLmVycm9yTWVzc2FnZSB8fCBudWxsXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IHNldEluKGFjdGlvbi5maWVsZFBhdGgsIGFjdGlvbi52YWx1ZSwgc3RhdGUuZGF0YSlcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZX0ZBSUxVUkVcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YToge1xuXHRcdFx0XHRcdFwiQHJlbGF0aW9uc1wiOiBbXVxuXHRcdFx0XHR9LFxuXHRcdFx0XHRlcnJvck1lc3NhZ2U6IGFjdGlvbi5lcnJvck1lc3NhZ2Vcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlNFVF9WUkVcIjoge1xuXHRcdFx0cmV0dXJuIGluaXRpYWxTdGF0ZTtcblx0XHR9XG5cblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn0iLCJpbXBvcnQge2NvbWJpbmVSZWR1Y2Vyc30gZnJvbSBcInJlZHV4XCI7XG5cbmltcG9ydCBlbnRpdHkgZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQgbWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXNcIjtcbmltcG9ydCB1c2VyIGZyb20gXCIuL3VzZXJcIjtcbmltcG9ydCB2cmUgZnJvbSBcIi4vdnJlXCI7XG5pbXBvcnQgcXVpY2tTZWFyY2ggZnJvbSBcIi4vcXVpY2stc2VhcmNoXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGNvbWJpbmVSZWR1Y2Vycyh7XG5cdHZyZTogdnJlLFxuXHRlbnRpdHk6IGVudGl0eSxcblx0dXNlcjogdXNlcixcblx0bWVzc2FnZXM6IG1lc3NhZ2VzLFxuXHRxdWlja1NlYXJjaDogcXVpY2tTZWFyY2hcbn0pOyIsImltcG9ydCBzZXRJbiBmcm9tIFwiLi4vdXRpbC9zZXQtaW5cIjtcblxuY29uc3QgaW5pdGlhbFN0YXRlID0ge1xuXHRsb2c6IFtdXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlJFUVVFU1RfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJTVUNDRVNTX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiRVJST1JfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJESVNNSVNTX01FU1NBR0VcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRsb2c6IHNldEluKFthY3Rpb24ubWVzc2FnZUluZGV4LCBcImRpc21pc3NlZFwiXSwgdHJ1ZSwgc3RhdGUubG9nKVxuXHRcdFx0fTtcblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0ge1xuXHRzdGFydDogMCxcblx0bGlzdDogW10sXG5cdHJvd3M6IDUwLFxuXHRxdWVyeTogXCJcIlxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgc3RhcnQ6IGFjdGlvbi5zdGFydH07XG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0bGlzdDogYWN0aW9uLmRhdGFcblx0XHRcdH19O1xuXHRcdGNhc2UgXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIjoge1xuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRxdWVyeTogYWN0aW9uLnZhbHVlXG5cdFx0XHR9fTtcblx0XHR9XG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImxldCBpbml0aWFsU3RhdGUgPSBudWxsO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9VU0VSXCI6XG5cdFx0XHRpZiAoYWN0aW9uLnVzZXIpIHtcblx0XHRcdFx0cmV0dXJuIGFjdGlvbi51c2VyO1xuXHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdFx0fVxuXHRcdFx0YnJlYWs7XG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImxldCBpbml0aWFsU3RhdGUgPSB7XG5cdHZyZUlkOiBudWxsLFxuXHRsaXN0OiBbXSxcblx0Y29sbGVjdGlvbnM6IHt9LFxuXHRkb21haW46IG51bGxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1ZSRVwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdHZyZUlkOiBhY3Rpb24udnJlSWQsXG5cdFx0XHRcdGNvbGxlY3Rpb25zOiBhY3Rpb24uY29sbGVjdGlvbnMgfHwgbnVsbCxcblx0XHRcdFx0bGlzdDogYWN0aW9uLmxpc3QgfHwgc3RhdGUubGlzdFxuXHRcdFx0fTtcblxuXHRcdGNhc2UgXCJMSVNUX1ZSRVNcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRsaXN0OiBhY3Rpb24ubGlzdCxcblx0XHRcdFx0Y29sbGVjdGlvbnM6IG51bGxcblx0XHRcdH07XG5cdFx0Y2FzZSBcIlNFVF9ET01BSU5cIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRkb21haW46IGFjdGlvbi5kb21haW5cblx0XHRcdH07XG5cblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHtSb3V0ZXIsIFJlZGlyZWN0LCBSb3V0ZSwgaGFzaEhpc3Rvcnl9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7UHJvdmlkZXIsIGNvbm5lY3R9IGZyb20gXCJyZWFjdC1yZWR1eFwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuL3N0b3JlXCI7XG5pbXBvcnQgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIGZyb20gXCIuL2FjdGlvbnMvYXV0b2NvbXBsZXRlXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9hY3Rpb25zXCI7XG5cbmltcG9ydCBFZGl0R3VpIGZyb20gXCIuL2NvbXBvbmVudHMvZWRpdC1ndWkvZWRpdC1ndWlcIjtcbmltcG9ydCB7dXJsc30gZnJvbSBcIi4vdXJsc1wiO1xuXG5cblxuXG5leHBvcnQgZnVuY3Rpb24gbmF2aWdhdGVUbyhrZXksIGFyZ3MpIHtcblx0aGFzaEhpc3RvcnkucHVzaCh1cmxzW2tleV0uYXBwbHkobnVsbCwgYXJncykpO1xufVxuXG5jb25zdCBkZWZhdWx0Q29ubmVjdCA9IGNvbm5lY3QoXG5cdHN0YXRlID0+ICh7Li4uc3RhdGUsIGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzfSksXG5cdGRpc3BhdGNoID0+IGFjdGlvbnMobmF2aWdhdGVUbywgZGlzcGF0Y2gpXG4pO1xuXG5cbmV4cG9ydCBkZWZhdWx0IChcblx0PFByb3ZpZGVyIHN0b3JlPXtzdG9yZX0+XG5cdFx0PFJvdXRlciBoaXN0b3J5PXtoYXNoSGlzdG9yeX0+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5yb290KCl9IGNvbXBvbmVudHM9e2RlZmF1bHRDb25uZWN0KEVkaXRHdWkpfSAvPlxuXHRcdFx0PFJvdXRlIHBhdGg9e3VybHMubmV3RW50aXR5KCl9IGNvbXBvbmVudHM9e2RlZmF1bHRDb25uZWN0KEVkaXRHdWkpfSAvPlxuXHRcdFx0PFJvdXRlIHBhdGg9e3VybHMuZW50aXR5KCl9IGNvbXBvbmVudHM9e2RlZmF1bHRDb25uZWN0KEVkaXRHdWkpfSAvPlxuXHRcdDwvUm91dGVyPlxuXHQ8L1Byb3ZpZGVyPlxuKTtcblxuIiwiaW1wb3J0IHtjcmVhdGVTdG9yZSwgYXBwbHlNaWRkbGV3YXJlfSBmcm9tIFwicmVkdXhcIjtcbmltcG9ydCB0aHVua01pZGRsZXdhcmUgZnJvbSBcInJlZHV4LXRodW5rXCI7XG5cbmltcG9ydCByZWR1Y2VycyBmcm9tIFwiLi4vcmVkdWNlcnNcIjtcblxuY29uc3QgbG9nZ2VyID0gKCkgPT4gbmV4dCA9PiBhY3Rpb24gPT4ge1xuICBpZiAoYWN0aW9uLmhhc093blByb3BlcnR5KFwidHlwZVwiKSkge1xuICAgIGNvbnNvbGUubG9nKFwiW1JFRFVYXVwiLCBhY3Rpb24udHlwZSwgYWN0aW9uKTtcbiAgfVxuXG4gIHJldHVybiBuZXh0KGFjdGlvbik7XG59O1xuXG5sZXQgY3JlYXRlU3RvcmVXaXRoTWlkZGxld2FyZSA9IGFwcGx5TWlkZGxld2FyZSgvKmxvZ2dlciwqLyB0aHVua01pZGRsZXdhcmUpKGNyZWF0ZVN0b3JlKTtcbmV4cG9ydCBkZWZhdWx0IGNyZWF0ZVN0b3JlV2l0aE1pZGRsZXdhcmUocmVkdWNlcnMpO1xuIiwiY29uc3QgdXJscyA9IHtcblx0cm9vdCgpIHtcblx0XHRyZXR1cm4gXCIvXCI7XG5cdH0sXG5cdG5ld0VudGl0eShjb2xsZWN0aW9uKSB7XG5cdFx0cmV0dXJuIGNvbGxlY3Rpb25cblx0XHRcdD8gYC8ke2NvbGxlY3Rpb259YFxuXHRcdFx0OiBcIi86Y29sbGVjdGlvblwiO1xuXHR9LFxuXHRlbnRpdHkoY29sbGVjdGlvbiwgaWQpIHtcblx0XHRyZXR1cm4gY29sbGVjdGlvbiAmJiBpZFxuXHRcdFx0PyBgLyR7Y29sbGVjdGlvbn0vJHtpZH1gXG5cdFx0XHQ6IFwiLzpjb2xsZWN0aW9uLzppZFwiO1xuXHR9XG59O1xuXG5leHBvcnQgeyB1cmxzIH0iLCJmdW5jdGlvbiBkZWVwQ2xvbmU5KG9iaikge1xuICAgIHZhciBpLCBsZW4sIHJldDtcblxuICAgIGlmICh0eXBlb2Ygb2JqICE9PSBcIm9iamVjdFwiIHx8IG9iaiA9PT0gbnVsbCkge1xuICAgICAgICByZXR1cm4gb2JqO1xuICAgIH1cblxuICAgIGlmIChBcnJheS5pc0FycmF5KG9iaikpIHtcbiAgICAgICAgcmV0ID0gW107XG4gICAgICAgIGxlbiA9IG9iai5sZW5ndGg7XG4gICAgICAgIGZvciAoaSA9IDA7IGkgPCBsZW47IGkrKykge1xuICAgICAgICAgICAgcmV0LnB1c2goICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV0gKTtcbiAgICAgICAgfVxuICAgIH0gZWxzZSB7XG4gICAgICAgIHJldCA9IHt9O1xuICAgICAgICBmb3IgKGkgaW4gb2JqKSB7XG4gICAgICAgICAgICBpZiAob2JqLmhhc093blByb3BlcnR5KGkpKSB7XG4gICAgICAgICAgICAgICAgcmV0W2ldID0gKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gcmV0O1xufVxuXG5leHBvcnQgZGVmYXVsdCBkZWVwQ2xvbmU5OyIsImltcG9ydCBjbG9uZSBmcm9tIFwiLi9jbG9uZS1kZWVwXCI7XG5cbi8vIERvIGVpdGhlciBvZiB0aGVzZTpcbi8vICBhKSBTZXQgYSB2YWx1ZSBieSByZWZlcmVuY2UgaWYgZGVyZWYgaXMgbm90IG51bGxcbi8vICBiKSBTZXQgYSB2YWx1ZSBkaXJlY3RseSBpbiB0byBkYXRhIG9iamVjdCBpZiBkZXJlZiBpcyBudWxsXG5jb25zdCBzZXRFaXRoZXIgPSAoZGF0YSwgZGVyZWYsIGtleSwgdmFsKSA9PiB7XG5cdChkZXJlZiB8fCBkYXRhKVtrZXldID0gdmFsO1xuXHRyZXR1cm4gZGF0YTtcbn07XG5cbi8vIFNldCBhIG5lc3RlZCB2YWx1ZSBpbiBkYXRhIChub3QgdW5saWtlIGltbXV0YWJsZWpzLCBidXQgYSBjbG9uZSBvZiBkYXRhIGlzIGV4cGVjdGVkIGZvciBwcm9wZXIgaW1tdXRhYmlsaXR5KVxuY29uc3QgX3NldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA9IG51bGwpID0+XG5cdHBhdGgubGVuZ3RoID4gMSA/XG5cdFx0X3NldEluKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA/IGRlcmVmW3BhdGguc2hpZnQoKV0gOiBkYXRhW3BhdGguc2hpZnQoKV0pIDpcblx0XHRzZXRFaXRoZXIoZGF0YSwgZGVyZWYsIHBhdGhbMF0sIHZhbHVlKTtcblxuY29uc3Qgc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEpID0+XG5cdF9zZXRJbihjbG9uZShwYXRoKSwgdmFsdWUsIGNsb25lKGRhdGEpKTtcblxuZXhwb3J0IGRlZmF1bHQgc2V0SW47Il19
