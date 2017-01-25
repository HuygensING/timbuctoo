(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.TimbuctooEdit = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
	value: true
});

exports.default = function (path, query, done) {
	var options = {
		url: "" + "/v2.1/" + path.replace(/^\/v[^/]+\//, "") + "?query=" + query + "*"
	};
	var requestTime = new Date().getTime();
	lastRequestTime = requestTime;

	var xhrDone = function xhrDone(err, response, body) {
		if (requestTime === lastRequestTime) {
			done(JSON.parse(body).map(function (d) {
				return { key: d.key.replace(/^.+\//, ""), value: d.value };
			}));
		}
	};

	_server2.default.fastXhr(options, xhrDone);
};

var _server = require("./server");

var _server2 = _interopRequireDefault(_server);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var lastRequestTime = 0;

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
exports.addFieldsToEntity = exports.selectDomain = exports.sendQuickSearch = exports.paginateLeft = exports.paginateRight = exports.fetchEntityList = exports.deleteEntity = exports.makeNewEntity = exports.selectEntity = exports.saveEntity = undefined;

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

var addFieldsToEntity = function addFieldsToEntity(fields) {
	return function (dispatch) {
		fields.forEach(function (field) {
			if (field.type === "relation") {
				dispatch({ type: "SET_ENTITY_FIELD_VALUE", fieldPath: ["@relations", field.name], value: [] });
			} else {
				dispatch({ type: "SET_ENTITY_FIELD_VALUE", fieldPath: [field.name], value: initialDataForType(field) });
			}
		});
	};
};

var fetchEntityList = function fetchEntityList(domain) {
	var next = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : function () {};
	return function (dispatch) {
		dispatch({ type: "SET_PAGINATION_START", start: 0 });

		(0, _autocomplete2.default)("domain/" + domain + "/autocomplete", "", function (data) {
			var transformedData = data.map(function (d) {
				return {
					_id: d.key.replace(/.*\//, ""),
					"@displayName": d.value
				};
			});
			dispatch({ type: "RECEIVE_ENTITY_LIST", data: transformedData });
			next(transformedData);
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
			data: { "@relations": {} },
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
exports.addFieldsToEntity = addFieldsToEntity;

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
		onAddSelectedFields: function onAddSelectedFields(fields) {
			return dispatch((0, _entity.addFieldsToEntity)(fields));
		},

		onRedirectToFirst: function onRedirectToFirst(collection) {
			return dispatch((0, _entity.fetchEntityList)(collection, function (list) {
				if (list.length > 0) {
					navigateTo('entity', [collection, list[0]._id]);
				}
			}));
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
		key: "render",
		value: function render() {
			var _props = this.props,
			    collections = _props.collections,
			    activeDomain = _props.activeDomain,
			    onRedirectToFirst = _props.onRedirectToFirst;

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
								"a",
								{ onClick: function onClick() {
										return onRedirectToFirst(domain);
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

var _message = require("../message");

var _message2 = _interopRequireDefault(_message);

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
			}
		}
	}, {
		key: "componentDidMount",
		value: function componentDidMount() {

			if (this.props.params.id) {
				this.props.onSelect({ domain: this.props.params.collection, id: this.props.params.id });
			} else if (!this.props.params.collection && !this.props.location.pathname.match(/new$/) && this.props.entity.domain) {
				this.props.onRedirectToFirst(this.props.entity.domain);
			} else if (this.props.location.pathname.match(/new$/)) {
				this.props.onNew(this.props.entity.domain);
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
			    onChange = _props2.onChange,
			    onAddSelectedFields = _props2.onAddSelectedFields,
			    onRedirectToFirst = _props2.onRedirectToFirst;
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
			var loginMessage = this.props.user ? null : _react2.default.createElement(
				_message2.default,
				{ dismissible: false, alertLevel: "warning" },
				_react2.default.createElement(
					"form",
					{ action: "https://secure.huygens.knaw.nl/saml2/login", method: "POST", style: { display: "inline-block", float: "right" } },
					_react2.default.createElement("input", { name: "hsurl", value: "" + location.href, type: "hidden" }),
					_react2.default.createElement(
						"button",
						{ className: "btn btn-warning btn-sm", type: "submit" },
						_react2.default.createElement("span", { className: "glyphicon glyphicon-log-in" }),
						" Log in"
					)
				),
				_react2.default.createElement("span", { className: "glyphicon glyphicon-exclamation-sign" }),
				" ",
				"You are not logged in, your session has expired, or you are not allowed to edit this dataset"
			);

			return _react2.default.createElement(
				_page2.default,
				{ username: this.props.user && this.props.user.userData && this.props.user.userData.displayName ? this.props.user.userData.displayName : "" },
				_react2.default.createElement(
					"div",
					{ className: "container", style: { textAlign: "right" } },
					"This edit interface is machine-generated based on the data-model. ",
					_react2.default.createElement(
						"a",
						{ href: "https://github.com/huygensing/timbuctoo/issues/new", target: "_blank" },
						"Suggestions"
					),
					" for improvement are very welcome!"
				),
				_react2.default.createElement(_collectionTabs2.default, { collections: vre.collections, onNew: onNew, onSelectDomain: onSelectDomain, onRedirectToFirst: onRedirectToFirst,
					activeDomain: entity.domain }),
				_react2.default.createElement(
					"div",
					{ className: "container" },
					loginMessage,
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
								domain: entity.domain,
								selectedId: entity.data._id,
								entityPending: entity.pending
							})
						),
						entity.pending ? _react2.default.createElement(
							"div",
							{ className: "basic-margin" },
							"Loading, please wait..."
						) : entity.domain ? _react2.default.createElement(_form2.default, { currentMode: currentMode, getAutocompleteValues: getAutocompleteValues,
							onAddSelectedFields: onAddSelectedFields,
							entity: entity, onNew: onNew, onDelete: onDelete, onChange: onChange, user: this.props.user,
							properties: vre.collections[entity.domain].properties,
							entityLabel: vre.collections[entity.domain].collectionLabel.replace(/s$/, "") }) : null
					)
				),
				_react2.default.createElement(
					"div",
					{ type: "footer-body", className: "row" },
					_react2.default.createElement(
						"div",
						{ className: "col-sm-6 col-md-4", style: { textAlign: "left", padding: '0' } },
						_react2.default.createElement(_paginate2.default, {
							start: quickSearch.start,
							listLength: quickSearch.list.length,
							rows: 50,
							onPaginateLeft: onPaginateLeft,
							onPaginateRight: onPaginateRight })
					),
					_react2.default.createElement(
						"div",
						{ className: "col-sm-6 col-md-8", style: { textAlign: "left", padding: '0' } },
						!entity.pending ? _react2.default.createElement(_saveFooter2.default, { onSave: onSave, onCancel: function onCancel() {
								return currentMode === "edit" ? onSelect({ domain: entity.domain, id: entity.data._id }) : onNew(entity.domain);
							}, user: this.props.user }) : null
					)
				)
			);
		}
	}]);

	return EditGui;
}(_react2.default.Component);

exports.default = EditGui;

},{"../message":26,"../page.jsx":27,"./collection-tabs":8,"./entity-form/form":18,"./entity-form/save-footer":19,"./entity-index/list":20,"./entity-index/paginate":21,"./entity-index/quicksearch":22,"./messages/list":23,"react":"react"}],10:[function(require,module,exports){
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
          onChange = _props2.onChange,
          targetCollection = _props2.targetCollection;

      var values = entity.data["@relations"][this.props.name] || [];
      var itemElements = values.filter(function (val) {
        return val.accepted;
      }).map(function (value, i) {
        return _react2.default.createElement(
          "div",
          { key: i + "-" + value.id, className: "item-element" },
          _react2.default.createElement(
            _reactRouter.Link,
            { to: _urls.urls.entity(targetCollection, value.id) },
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

var _reactRouter = require("react-router");

var _urls = require("../../../urls");

var _camel2label = require("./fields/camel2label");

var _camel2label2 = _interopRequireDefault(_camel2label);

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
    return _react2.default.createElement(_relation2.default, _extends({}, props, { name: fieldDef.name, targetCollection: fieldDef.relation.targetCollection, path: fieldDef.quicksearch }));
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

var applyFilter = function applyFilter(field, filter) {
  return field.toLowerCase().indexOf(filter.toLowerCase()) > -1 || (0, _camel2label2.default)(field).toLowerCase().indexOf(filter.toLowerCase()) > -1;
};

var EntityForm = function (_React$Component) {
  _inherits(EntityForm, _React$Component);

  function EntityForm(props) {
    _classCallCheck(this, EntityForm);

    var _this = _possibleConstructorReturn(this, (EntityForm.__proto__ || Object.getPrototypeOf(EntityForm)).call(this, props));

    _this.state = {
      fieldsToAdd: [],
      addFieldFilter: ""
    };
    return _this;
  }

  _createClass(EntityForm, [{
    key: "onFilterChange",
    value: function onFilterChange(ev) {
      var _this2 = this;

      this.setState({ addFieldFilter: ev.target.value }, function () {
        var filtered = _this2.getAddableFieldsFromProperties().filter(function (prop) {
          return applyFilter(prop.name, _this2.state.addFieldFilter);
        });
        if (filtered.length > 0) {
          if (_this2.state.addFieldFilter === "") {
            _this2.setState({ fieldsToAdd: [] });
          } else {
            _this2.setState({ fieldsToAdd: [filtered[0].name] });
          }
        }
      });
    }
  }, {
    key: "onFilterKeyDown",
    value: function onFilterKeyDown(ev) {
      if (ev.key === "Enter" && this.state.fieldsToAdd.length > 0) {
        this.onAddSelectedFields();
      }
    }
  }, {
    key: "toggleFieldToAdd",
    value: function toggleFieldToAdd(fieldName) {
      if (this.state.fieldsToAdd.indexOf(fieldName) > -1) {
        this.setState({ fieldsToAdd: this.state.fieldsToAdd.filter(function (fAdd) {
            return fAdd !== fieldName;
          }) });
      } else {
        this.setState({ fieldsToAdd: this.state.fieldsToAdd.concat(fieldName) });
      }
    }
  }, {
    key: "onAddSelectedFields",
    value: function onAddSelectedFields() {
      var properties = this.props.properties;


      this.props.onAddSelectedFields(this.state.fieldsToAdd.map(function (fAdd) {
        return {
          name: fAdd,
          type: properties.find(function (prop) {
            return prop.name === fAdd;
          }).type
        };
      }));

      this.setState({ fieldsToAdd: [], addFieldFilter: "" });
    }
  }, {
    key: "getAddableFieldsFromProperties",
    value: function getAddableFieldsFromProperties() {
      var _props = this.props,
          entity = _props.entity,
          properties = _props.properties;


      return properties.filter(function (fieldDef) {
        return fieldMap.hasOwnProperty(fieldDef.type);
      }).filter(function (fieldDef) {
        return !entity.data.hasOwnProperty(fieldDef.name) && !entity.data["@relations"].hasOwnProperty(fieldDef.name);
      });
    }
  }, {
    key: "render",
    value: function render() {
      var _this3 = this;

      var _props2 = this.props,
          onDelete = _props2.onDelete,
          onChange = _props2.onChange,
          getAutocompleteValues = _props2.getAutocompleteValues;
      var _props3 = this.props,
          entity = _props3.entity,
          currentMode = _props3.currentMode,
          properties = _props3.properties,
          entityLabel = _props3.entityLabel;
      var _state = this.state,
          fieldsToAdd = _state.fieldsToAdd,
          addFieldFilter = _state.addFieldFilter;


      return _react2.default.createElement(
        "div",
        { className: "col-sm-6 col-md-8" },
        _react2.default.createElement(
          "div",
          { className: "basic-margin" },
          _react2.default.createElement(
            _reactRouter.Link,
            { to: _urls.urls.newEntity(entity.domain), className: "btn btn-primary pull-right" },
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
        }).filter(function (fieldDef) {
          return entity.data.hasOwnProperty(fieldDef.name) || entity.data["@relations"].hasOwnProperty(fieldDef.name);
        }).map(function (fieldDef, i) {
          return fieldMap[fieldDef.type](fieldDef, {
            key: i + "-" + fieldDef.name,
            entity: entity,
            onChange: onChange,
            getAutocompleteValues: getAutocompleteValues
          });
        }),
        _react2.default.createElement(
          "div",
          { className: "basic-margin add-field-form" },
          _react2.default.createElement(
            "h4",
            null,
            "Add fields"
          ),
          _react2.default.createElement("input", { className: "form-control", value: addFieldFilter, placeholder: "Filter...",
            onChange: this.onFilterChange.bind(this),
            onKeyPress: this.onFilterKeyDown.bind(this)
          }),
          _react2.default.createElement(
            "div",
            { style: { maxHeight: "250px", overflowY: "auto" } },
            this.getAddableFieldsFromProperties().filter(function (fieldDef) {
              return applyFilter(fieldDef.name, addFieldFilter);
            }).map(function (fieldDef, i) {
              return _react2.default.createElement(
                "div",
                { key: i, onClick: function onClick() {
                    return _this3.toggleFieldToAdd(fieldDef.name);
                  },
                  className: fieldsToAdd.indexOf(fieldDef.name) > -1 ? "selected" : "" },
                _react2.default.createElement(
                  "span",
                  { className: "pull-right" },
                  "(",
                  fieldDef.type,
                  ")"
                ),
                (0, _camel2label2.default)(fieldDef.name)
              );
            })
          ),
          _react2.default.createElement(
            "button",
            { className: "btn btn-default", onClick: this.onAddSelectedFields.bind(this) },
            "Add selected fields"
          )
        ),
        currentMode === "edit" ? _react2.default.createElement(
          "div",
          { className: "basic-margin" },
          _react2.default.createElement(
            "h4",
            null,
            "Delete"
          ),
          _react2.default.createElement(
            "button",
            { className: "btn btn-danger", onClick: onDelete, disabled: !this.props.user },
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

},{"../../../urls":37,"./fields/camel2label":10,"./fields/links":11,"./fields/list-of-strings":12,"./fields/multi-select":13,"./fields/names":14,"./fields/relation":15,"./fields/select":16,"./fields/string-field":17,"react":"react","react-router":"react-router"}],19:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (props) {
  var onSave = props.onSave,
      onCancel = props.onCancel,
      user = props.user;


  return _react2.default.createElement(
    "div",
    null,
    _react2.default.createElement(
      "button",
      { disabled: !user, className: "btn btn-primary", onClick: onSave },
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
      domain = props.domain,
      selectedId = props.selectedId,
      entityPending = props.entityPending;


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
          entityPending ? _react2.default.createElement(
            "a",
            { style: {
                display: "inline-block", width: "calc(100% - 30px)", height: "100%", padding: "0.5em 0",
                cursor: "default", opacity: "0.5", textDecoration: "none", fontWeight: "300"
              } },
            entry["@displayName"]
          ) : _react2.default.createElement(
            _reactRouter.Link,
            { to: _urls.urls.entity(domain, entry._id), style: {
                display: "inline-block", width: "calc(100% - 30px)", height: "100%", padding: "0.5em 0",
                fontWeight: selectedId === entry._id ? "500" : "300"
              } },
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
      { className: "small-margin hi-Green container-fluid" },
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

var _vre = require("./actions/vre");

var _router = require("./router");

var _router2 = _interopRequireDefault(_router);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var setUser = function setUser(user) {
	// TODO: validate user session.
	if (user) {
		(0, _xhr2.default)({
			url: "" + "/v2.1/system/users/me/vres",
			headers: {
				'Authorization': user.token
			}
		}, function (err, resp) {
			if (err || resp.statusCode >= 300) {
				_store2.default.dispatch({ type: "SESSION_EXPIRED" });
			} else {
				var data = JSON.parse(resp.body);
				if (!data.mine || Object.keys(data.mine).indexOf(getVreId()) < 0) {
					_store2.default.dispatch({ type: "ERROR_MESSAGE", message: "You are not allowed to edit this vre" });
					_store2.default.dispatch({ type: "SESSION_EXPIRED" });
				}
			}
		});

		(0, _xhr2.default)({
			url: "" + "/v2.1/system/users/me",
			headers: {
				'Authorization': user.token
			}
		}, function (err, resp) {
			try {
				var userData = JSON.parse(resp.body);
				_store2.default.dispatch({ type: "SET_USER_DATA", userData: userData });
			} catch (e) {
				console.warn(e);
			}
		});
	}

	return {
		type: "SET_USER",
		user: user
	};
};

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

document.addEventListener("DOMContentLoaded", function () {

	function initRouter() {
		_reactDom2.default.render(_router2.default, document.getElementById("app"));
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
				localStorage.setItem("token", JSON.stringify({ user: value, token: value }));
				location.href = window.location.href.replace("hsid=" + value, "");
				return;
			}
		}
		return JSON.parse(localStorage.getItem("token") || "null");
	}

	_store2.default.dispatch((0, _vre.setVre)(getVreId(), initRouter));
	_store2.default.dispatch(setUser(getLogin()));
});

},{"./actions/vre":7,"./router":35,"./store":36,"react":"react","react-dom":"react-dom","xhr":"xhr"}],29:[function(require,module,exports){
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
				},
				pending: true
			});
		case "RECEIVE_ENTITY":
			return _extends({}, state, {
				data: action.data,
				domain: action.domain,
				errorMessage: action.errorMessage || null,
				pending: false
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
				errorMessage: action.errorMessage,
				pending: false
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
	errorMessage: null,
	pending: false
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

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

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
		case "SESSION_EXPIRED":
			return null;
		case "SET_USER_DATA":
			return state ? _extends({}, state, { userData: action.userData }) : null;
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
		return collection ? "/" + collection + "/new" : "/:collection/new";
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
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDR2UsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFNLFVBQVU7QUFDZixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEZSxFQUFoQjtBQUdBLEtBQU0sY0FBYyxJQUFJLElBQUosR0FBVyxPQUFYLEVBQXBCO0FBQ0EsbUJBQWtCLFdBQWxCOztBQUVBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxNQUFJLGdCQUFn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QSxDQUhEOztBQUtBO0FBQ0EsSUFBTSxTQUFTLFNBQVQsTUFBUyxDQUFDLElBQUQsRUFBTyxLQUFQLEVBQWMsSUFBZDtBQUFBLEtBQW9CLEtBQXBCLHVFQUE0QixJQUE1QjtBQUFBLFFBQ2QsS0FBSyxNQUFMLEdBQWMsQ0FBZCxHQUNDLE9BQU8sSUFBUCxFQUFhLEtBQWIsRUFBb0IsSUFBcEIsRUFBMEIsUUFBUSxNQUFNLEtBQUssS0FBTCxFQUFOLENBQVIsR0FBOEIsS0FBSyxLQUFLLEtBQUwsRUFBTCxDQUF4RCxDQURELEdBRUMsVUFBVSxJQUFWLEVBQWdCLEtBQWhCLEVBQXVCLEtBQUssQ0FBTCxDQUF2QixFQUFnQyxLQUFoQyxDQUhhO0FBQUEsQ0FBZjs7QUFLQSxJQUFNLFFBQVEsU0FBUixLQUFRLENBQUMsSUFBRCxFQUFPLEtBQVAsRUFBYyxJQUFkO0FBQUEsUUFDYixPQUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5cbmxldCBsYXN0UmVxdWVzdFRpbWUgPSAwO1xuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocGF0aCwgcXVlcnksIGRvbmUpIHtcblx0Y29uc3Qgb3B0aW9ucyA9IHtcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS8ke3BhdGgucmVwbGFjZSgvXlxcL3ZbXi9dK1xcLy8sIFwiXCIpfT9xdWVyeT0ke3F1ZXJ5fSpgXG5cdH07XG5cdGNvbnN0IHJlcXVlc3RUaW1lID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XG5cdGxhc3RSZXF1ZXN0VGltZSA9IHJlcXVlc3RUaW1lO1xuXG5cdGxldCB4aHJEb25lID0gZnVuY3Rpb24oZXJyLCByZXNwb25zZSwgYm9keSkge1xuXHRcdGlmIChyZXF1ZXN0VGltZSA9PT0gbGFzdFJlcXVlc3RUaW1lKSB7XG5cdFx0XHRkb25lKEpTT04ucGFyc2UoYm9keSkubWFwKChkKSA9PiB7XG5cdFx0XHRcdHJldHVybiB7a2V5OiBkLmtleS5yZXBsYWNlKC9eLitcXC8vLCBcIlwiKSwgdmFsdWU6IGQudmFsdWV9O1xuXHRcdFx0fSkpO1xuXHRcdH1cblx0fTtcblxuXHRzZXJ2ZXIuZmFzdFhocihvcHRpb25zLCB4aHJEb25lKTtcbn0iLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuXG5jb25zdCBzYXZlTmV3RW50aXR5ID0gKGRvbWFpbiwgc2F2ZURhdGEsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJQT1NUXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0Ym9keTogSlNPTi5zdHJpbmdpZnkoc2F2ZURhdGEpLFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn1gXG5cdH0sIG5leHQsIGZhaWwsIGBDcmVhdGUgbmV3ICR7ZG9tYWlufWApO1xuXG5jb25zdCB1cGRhdGVFbnRpdHkgPSAoZG9tYWluLCBzYXZlRGF0YSwgdG9rZW4sIHZyZUlkLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIlBVVFwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdGJvZHk6IEpTT04uc3RyaW5naWZ5KHNhdmVEYXRhKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7c2F2ZURhdGEuX2lkfWBcblx0fSwgbmV4dCwgZmFpbCwgYFVwZGF0ZSAke2RvbWFpbn1gKTtcblxuY29uc3QgZGVsZXRlRW50aXR5ID0gKGRvbWFpbiwgZW50aXR5SWQsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJERUxFVEVcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7ZW50aXR5SWR9YFxuXHR9LCBuZXh0LCBmYWlsLCBgRGVsZXRlICR7ZG9tYWlufWApO1xuXG5jb25zdCBmZXRjaEVudGl0eSA9IChsb2NhdGlvbiwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJ9LFxuXHRcdHVybDogbG9jYXRpb25cblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGNvbnN0IGRhdGEgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0bmV4dChkYXRhKTtcblx0fSwgZmFpbCwgXCJGZXRjaCBlbnRpdHlcIik7XG5cbmNvbnN0IGZldGNoRW50aXR5TGlzdCA9IChkb21haW4sIHN0YXJ0LCByb3dzLCBuZXh0KSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIn0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufT9yb3dzPSR7cm93c30mc3RhcnQ9JHtzdGFydH1gXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdG5leHQoZGF0YSk7XG5cdH0pO1xuXG5jb25zdCBjcnVkID0ge1xuXHRzYXZlTmV3RW50aXR5OiBzYXZlTmV3RW50aXR5LFxuXHR1cGRhdGVFbnRpdHk6IHVwZGF0ZUVudGl0eSxcblx0ZGVsZXRlRW50aXR5OiBkZWxldGVFbnRpdHksXG5cdGZldGNoRW50aXR5OiBmZXRjaEVudGl0eSxcblx0ZmV0Y2hFbnRpdHlMaXN0OiBmZXRjaEVudGl0eUxpc3Rcbn07XG5cbmV4cG9ydCB7c2F2ZU5ld0VudGl0eSwgdXBkYXRlRW50aXR5LCBkZWxldGVFbnRpdHksIGZldGNoRW50aXR5LCBmZXRjaEVudGl0eUxpc3QsIGNydWR9OyIsImltcG9ydCBjbG9uZSBmcm9tIFwiLi4vdXRpbC9jbG9uZS1kZWVwXCI7XG5pbXBvcnQgeyBjcnVkIH0gZnJvbSBcIi4vY3J1ZFwiO1xuaW1wb3J0IHNhdmVSZWxhdGlvbnMgZnJvbSBcIi4vc2F2ZS1yZWxhdGlvbnNcIjtcbmltcG9ydCBhdXRvY29tcGxldGUgZnJvbSBcIi4vYXV0b2NvbXBsZXRlXCI7XG5cbi8vIFNrZWxldG9uIGJhc2UgZGF0YSBwZXIgZmllbGQgZGVmaW5pdGlvblxuY29uc3QgaW5pdGlhbERhdGEgPSB7XG5cdG5hbWVzOiBbXSxcblx0bXVsdGlzZWxlY3Q6IFtdLFxuXHRsaW5rczogW10sXG5cdGtleXdvcmQ6IFtdLFxuXHRcImxpc3Qtb2Ytc3RyaW5nc1wiOiBbXSxcblx0YWx0bmFtZXM6IFtdLFxuXHR0ZXh0OiBcIlwiLFxuXHRzdHJpbmc6IFwiXCIsXG5cdHNlbGVjdDogXCJcIixcblx0ZGF0YWJsZTogXCJcIlxufTtcblxuLy8gUmV0dXJuIHRoZSBpbml0aWFsIGRhdGEgZm9yIHRoZSB0eXBlIGluIHRoZSBmaWVsZCBkZWZpbml0aW9uXG5jb25zdCBpbml0aWFsRGF0YUZvclR5cGUgPSAoZmllbGREZWYpID0+XG5cdGZpZWxkRGVmLmRlZmF1bHRWYWx1ZSB8fCAoZmllbGREZWYudHlwZSA9PT0gXCJyZWxhdGlvblwiIHx8IGZpZWxkRGVmLnR5cGUgPT09IFwia2V5d29yZFwiID8ge30gOiBpbml0aWFsRGF0YVtmaWVsZERlZi50eXBlXSk7XG5cbmNvbnN0IGFkZEZpZWxkc1RvRW50aXR5ID0gKGZpZWxkcykgPT4gKGRpc3BhdGNoKSA9PiB7XG5cdGZpZWxkcy5mb3JFYWNoKChmaWVsZCkgPT4ge1xuXHRcdGlmIChmaWVsZC50eXBlID09PSBcInJlbGF0aW9uXCIpIHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBbXCJAcmVsYXRpb25zXCIsIGZpZWxkLm5hbWVdLCB2YWx1ZTogW119KTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0VOVElUWV9GSUVMRF9WQUxVRVwiLCBmaWVsZFBhdGg6IFtmaWVsZC5uYW1lXSwgdmFsdWU6IGluaXRpYWxEYXRhRm9yVHlwZShmaWVsZCl9KTtcblx0XHR9XG5cdH0pXG59O1xuXG5jb25zdCBmZXRjaEVudGl0eUxpc3QgPSAoZG9tYWluLCBuZXh0ID0gKCkgPT4ge30pID0+IChkaXNwYXRjaCkgPT4ge1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogMH0pO1xuXG5cdGF1dG9jb21wbGV0ZShgZG9tYWluLyR7ZG9tYWlufS9hdXRvY29tcGxldGVgLCBcIlwiLCAoZGF0YSkgPT4ge1xuXHRcdGNvbnN0IHRyYW5zZm9ybWVkRGF0YSA9IGRhdGEubWFwKChkKSA9PiAoe1xuXHRcdFx0X2lkOiBkLmtleS5yZXBsYWNlKC8uKlxcLy8sIFwiXCIpLFxuXHRcdFx0XCJAZGlzcGxheU5hbWVcIjogZC52YWx1ZVxuXHRcdH0pKTtcblx0XHRkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IHRyYW5zZm9ybWVkRGF0YX0pO1xuXHRcdG5leHQodHJhbnNmb3JtZWREYXRhKTtcblx0fSk7XG59O1xuXG5jb25zdCBwYWdpbmF0ZUxlZnQgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IG5ld1N0YXJ0ID0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5zdGFydCAtIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cztcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3QgcGFnaW5hdGVSaWdodCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgbmV3U3RhcnQgPSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnN0YXJ0ICsgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogbmV3U3RhcnR9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3Qgc2VuZFF1aWNrU2VhcmNoID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSB9ID0gZ2V0U3RhdGUoKTtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IDB9KTtcblx0Y29uc3QgY2FsbGJhY2sgPSAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhLm1hcCgoZCkgPT4gKFxuXHRcdHtcblx0XHRcdF9pZDogZC5rZXkucmVwbGFjZSgvLipcXC8vLCBcIlwiKSxcblx0XHRcdFwiQGRpc3BsYXlOYW1lXCI6IGQudmFsdWVcblx0XHR9XG5cdCkpfSk7XG5cdGF1dG9jb21wbGV0ZShgZG9tYWluLyR7ZW50aXR5LmRvbWFpbn0vYXV0b2NvbXBsZXRlYCwgcXVpY2tTZWFyY2gucXVlcnksIGNhbGxiYWNrKTtcblxufTtcblxuY29uc3Qgc2VsZWN0RG9tYWluID0gKGRvbWFpbikgPT4gKGRpc3BhdGNoKSA9PiB7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9ET01BSU5cIiwgZG9tYWlufSk7XG5cdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChkb21haW4pKTtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCIsIHZhbHVlOiBcIlwifSk7XG59O1xuXG4vLyAxKSBGZXRjaCBlbnRpdHlcbi8vIDIpIERpc3BhdGNoIFJFQ0VJVkVfRU5USVRZIGZvciByZW5kZXJcbmNvbnN0IHNlbGVjdEVudGl0eSA9IChkb21haW4sIGVudGl0eUlkLCBlcnJvck1lc3NhZ2UgPSBudWxsLCBzdWNjZXNzTWVzc2FnZSA9IG51bGwsIG5leHQgPSAoKSA9PiB7IH0pID0+XG5cdChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0XHRjb25zdCB7IGVudGl0eTogeyBkb21haW46IGN1cnJlbnREb21haW4gfSB9ID0gZ2V0U3RhdGUoKTtcblx0XHRpZiAoY3VycmVudERvbWFpbiAhPT0gZG9tYWluKSB7XG5cdFx0XHRkaXNwYXRjaChzZWxlY3REb21haW4oZG9tYWluKSk7XG5cdFx0fVxuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIkJFRk9SRV9GRVRDSF9FTlRJVFlcIn0pXG5cdFx0Y3J1ZC5mZXRjaEVudGl0eShgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke2VudGl0eUlkfWAsIChkYXRhKSA9PiB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLCBkb21haW46IGRvbWFpbiwgZGF0YTogZGF0YSwgZXJyb3JNZXNzYWdlOiBlcnJvck1lc3NhZ2V9KTtcblx0XHRcdGlmIChzdWNjZXNzTWVzc2FnZSAhPT0gbnVsbCkge1xuXHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTVUNDRVNTX01FU1NBR0VcIiwgbWVzc2FnZTogc3VjY2Vzc01lc3NhZ2V9KTtcblx0XHRcdH1cblx0XHR9LCAoKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9GQUlMVVJFXCIsIGVycm9yTWVzc2FnZTogYEZhaWxlZCB0byBmZXRjaCAke2RvbWFpbn0gd2l0aCBJRCAke2VudGl0eUlkfWB9KSk7XG5cdFx0bmV4dCgpO1xuXHR9O1xuXG5cbi8vIDEpIERpc3BhdGNoIFJFQ0VJVkVfRU5USVRZIHdpdGggZW1wdHkgZW50aXR5IHNrZWxldG9uIGZvciByZW5kZXJcbmNvbnN0IG1ha2VOZXdFbnRpdHkgPSAoZG9tYWluLCBlcnJvck1lc3NhZ2UgPSBudWxsKSA9PlxuXHQoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiBkaXNwYXRjaCh7XG5cdFx0dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLFxuXHRcdGRvbWFpbjogZG9tYWluLFxuXHRcdGRhdGE6IHtcIkByZWxhdGlvbnNcIjoge319LFxuXHRcdGVycm9yTWVzc2FnZTogZXJyb3JNZXNzYWdlXG5cdH0pO1xuXG5jb25zdCBkZWxldGVFbnRpdHkgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNydWQuZGVsZXRlRW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsXG5cdFx0KCkgPT4ge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU1VDQ0VTU19NRVNTQUdFXCIsIG1lc3NhZ2U6IGBTdWNlc3NmdWxseSBkZWxldGVkICR7Z2V0U3RhdGUoKS5lbnRpdHkuZG9tYWlufSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YH0pO1xuXHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKTtcblx0XHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKTtcblx0XHR9LFxuXHRcdCgpID0+IGRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBgRmFpbGVkIHRvIGRlbGV0ZSAke2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbn0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWApKSk7XG59O1xuXG4vLyAxKSBTYXZlIGFuIGVudGl0eVxuLy8gMikgU2F2ZSB0aGUgcmVsYXRpb25zIGZvciB0aGlzIGVudGl0eVxuLy8gMykgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuY29uc3Qgc2F2ZUVudGl0eSA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgY29sbGVjdGlvbkxhYmVsID0gZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5jb2xsZWN0aW9uTGFiZWwucmVwbGFjZSgvcyQvLCBcIlwiKTtcblxuXHQvLyBNYWtlIGEgZGVlcCBjb3B5IG9mIHRoZSBkYXRhIHRvIGJlIHNhdmVkIGluIG9yZGVyIHRvIGxlYXZlIGFwcGxpY2F0aW9uIHN0YXRlIHVuYWx0ZXJlZFxuXHRsZXQgc2F2ZURhdGEgPSBjbG9uZShnZXRTdGF0ZSgpLmVudGl0eS5kYXRhKTtcblx0Ly8gTWFrZSBhIGRlZXAgY29weSBvZiB0aGUgcmVsYXRpb24gZGF0YSBpbiBvcmRlciB0byBsZWF2ZSBhcHBsaWNhdGlvbiBzdGF0ZSB1bmFsdGVyZWRcblx0bGV0IHJlbGF0aW9uRGF0YSA9IGNsb25lKHNhdmVEYXRhW1wiQHJlbGF0aW9uc1wiXSkgfHwge307XG5cdC8vIERlbGV0ZSB0aGUgcmVsYXRpb24gZGF0YSBmcm9tIHRoZSBzYXZlRGF0YSBhcyBpdCBpcyBub3QgZXhwZWN0ZWQgYnkgdGhlIHNlcnZlclxuXHRkZWxldGUgc2F2ZURhdGFbXCJAcmVsYXRpb25zXCJdO1xuXG5cdGlmIChnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdC8vIDEpIFVwZGF0ZSB0aGUgZW50aXR5IHdpdGggc2F2ZURhdGFcblx0XHRjcnVkLnVwZGF0ZUVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIHNhdmVEYXRhLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoZXJyLCByZXNwKSA9PlxuXHRcdFx0Ly8gMikgU2F2ZSByZWxhdGlvbnMgdXNpbmcgc2VydmVyIHJlc3BvbnNlIGZvciBjdXJyZW50IHJlbGF0aW9ucyB0byBkaWZmIGFnYWluc3QgcmVsYXRpb25EYXRhXG5cdFx0XHRkaXNwYXRjaCgocmVkaXNwYXRjaCkgPT4gc2F2ZVJlbGF0aW9ucyhKU09OLnBhcnNlKHJlc3AuYm9keSksIHJlbGF0aW9uRGF0YSwgZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5wcm9wZXJ0aWVzLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoKSA9PlxuXHRcdFx0XHQvLyAzKSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5cdFx0XHRcdHJlZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIG51bGwsIGBTdWNjZXNmdWxseSBzYXZlZCAke2NvbGxlY3Rpb25MYWJlbH0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWAsICgpID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKSkpKSksICgpID0+XG5cdFx0XHRcdFx0Ly8gMmEpIEhhbmRsZSBlcnJvciBieSByZWZldGNoaW5nIGFuZCBwYXNzaW5nIGFsb25nIGFuIGVycm9yIG1lc3NhZ2Vcblx0XHRcdFx0XHRkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgYEZhaWxlZCB0byBzYXZlICR7Y29sbGVjdGlvbkxhYmVsfSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YCkpKTtcblxuXHR9IGVsc2Uge1xuXHRcdC8vIDEpIENyZWF0ZSBuZXcgZW50aXR5IHdpdGggc2F2ZURhdGFcblx0XHRjcnVkLnNhdmVOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBzYXZlRGF0YSwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKGVyciwgcmVzcCkgPT5cblx0XHRcdC8vIDIpIEZldGNoIGVudGl0eSB2aWEgbG9jYXRpb24gaGVhZGVyXG5cdFx0XHRkaXNwYXRjaCgocmVkaXNwYXRjaCkgPT4gY3J1ZC5mZXRjaEVudGl0eShyZXNwLmhlYWRlcnMubG9jYXRpb24sIChkYXRhKSA9PlxuXHRcdFx0XHQvLyAzKSBTYXZlIHJlbGF0aW9ucyB1c2luZyBzZXJ2ZXIgcmVzcG9uc2UgZm9yIGN1cnJlbnQgcmVsYXRpb25zIHRvIGRpZmYgYWdhaW5zdCByZWxhdGlvbkRhdGFcblx0XHRcdFx0c2F2ZVJlbGF0aW9ucyhkYXRhLCByZWxhdGlvbkRhdGEsIGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0ucHJvcGVydGllcywgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKCkgPT5cblx0XHRcdFx0XHQvLyA0KSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5cdFx0XHRcdFx0cmVkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBkYXRhLl9pZCwgbnVsbCwgYFN1Y2Nlc2Z1bGx5IHNhdmVkICR7Y29sbGVjdGlvbkxhYmVsfWAsICgpID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKSkpKSkpLCAoKSA9PlxuXHRcdFx0XHRcdFx0Ly8gMmEpIEhhbmRsZSBlcnJvciBieSByZWZldGNoaW5nIGFuZCBwYXNzaW5nIGFsb25nIGFuIGVycm9yIG1lc3NhZ2Vcblx0XHRcdFx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBgRmFpbGVkIHRvIHNhdmUgbmV3ICR7Y29sbGVjdGlvbkxhYmVsfWApKSk7XG5cdH1cbn07XG5cblxuZXhwb3J0IHsgc2F2ZUVudGl0eSwgc2VsZWN0RW50aXR5LCBtYWtlTmV3RW50aXR5LCBkZWxldGVFbnRpdHksIGZldGNoRW50aXR5TGlzdCwgcGFnaW5hdGVSaWdodCwgcGFnaW5hdGVMZWZ0LCBzZW5kUXVpY2tTZWFyY2gsIHNlbGVjdERvbWFpbiwgYWRkRmllbGRzVG9FbnRpdHkgfTsiLCJpbXBvcnQgeyBzYXZlRW50aXR5LCBzZWxlY3RFbnRpdHksIG1ha2VOZXdFbnRpdHksIGRlbGV0ZUVudGl0eSwgYWRkRmllbGRzVG9FbnRpdHksXG5cdHNlbGVjdERvbWFpbiwgcGFnaW5hdGVMZWZ0LCBwYWdpbmF0ZVJpZ2h0LCBzZW5kUXVpY2tTZWFyY2gsIGZldGNoRW50aXR5TGlzdCB9IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IHsgc2V0VnJlIH0gZnJvbSBcIi4vdnJlXCI7XG5cbmV4cG9ydCBkZWZhdWx0IChuYXZpZ2F0ZVRvLCBkaXNwYXRjaCkgPT4gKHtcblx0b25OZXc6IChkb21haW4pID0+IGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZG9tYWluKSksXG5cdG9uU2VsZWN0OiAocmVjb3JkKSA9PiBkaXNwYXRjaChzZWxlY3RFbnRpdHkocmVjb3JkLmRvbWFpbiwgcmVjb3JkLmlkKSksXG5cdG9uU2F2ZTogKCkgPT4gZGlzcGF0Y2goc2F2ZUVudGl0eSgpKSxcblx0b25EZWxldGU6ICgpID0+IGRpc3BhdGNoKGRlbGV0ZUVudGl0eSgpKSxcblx0b25DaGFuZ2U6IChmaWVsZFBhdGgsIHZhbHVlKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCIsIGZpZWxkUGF0aDogZmllbGRQYXRoLCB2YWx1ZTogdmFsdWV9KSxcblx0b25BZGRTZWxlY3RlZEZpZWxkczogKGZpZWxkcykgPT4gZGlzcGF0Y2goYWRkRmllbGRzVG9FbnRpdHkoZmllbGRzKSksXG5cblx0b25SZWRpcmVjdFRvRmlyc3Q6IChjb2xsZWN0aW9uKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoY29sbGVjdGlvbiwgKGxpc3QpID0+IHtcblx0XHRpZiAobGlzdC5sZW5ndGggPiAwKSB7XG5cdFx0XHRuYXZpZ2F0ZVRvKCdlbnRpdHknLCBbY29sbGVjdGlvbiwgbGlzdFswXS5faWRdKTtcblx0XHR9XG5cdH0pKSxcblxuXHRvbkxvZ2luQ2hhbmdlOiAocmVzcG9uc2UpID0+IGRpc3BhdGNoKHNldFVzZXIocmVzcG9uc2UpKSxcblx0b25TZWxlY3RWcmU6ICh2cmVJZCkgPT4gZGlzcGF0Y2goc2V0VnJlKHZyZUlkKSksXG5cdG9uRGlzbWlzc01lc3NhZ2U6IChtZXNzYWdlSW5kZXgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIkRJU01JU1NfTUVTU0FHRVwiLCBtZXNzYWdlSW5kZXg6IG1lc3NhZ2VJbmRleH0pLFxuXHRvblNlbGVjdERvbWFpbjogKGRvbWFpbikgPT4ge1xuXHRcdGRpc3BhdGNoKHNlbGVjdERvbWFpbihkb21haW4pKTtcblx0fSxcblx0b25QYWdpbmF0ZUxlZnQ6ICgpID0+IGRpc3BhdGNoKHBhZ2luYXRlTGVmdCgpKSxcblx0b25QYWdpbmF0ZVJpZ2h0OiAoKSA9PiBkaXNwYXRjaChwYWdpbmF0ZVJpZ2h0KCkpLFxuXHRvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2U6ICh2YWx1ZSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCIsIHZhbHVlOiB2YWx1ZX0pLFxuXHRvblF1aWNrU2VhcmNoOiAoKSA9PiBkaXNwYXRjaChzZW5kUXVpY2tTZWFyY2goKSlcbn0pOyIsImltcG9ydCB7IHNhdmVOZXdFbnRpdHksIHVwZGF0ZUVudGl0eSB9IGZyb20gXCIuL2NydWRcIjtcblxuY29uc3Qgc2F2ZVJlbGF0aW9uc1YyMSA9IChkYXRhLCByZWxhdGlvbkRhdGEsIGZpZWxkRGVmcywgdG9rZW4sIHZyZUlkLCBuZXh0KSA9PiB7XG5cdC8vIFJldHVybnMgdGhlIGRvbWFpbiBiYXNlZCBvbiB0aGUgZmllbGREZWZpbml0aW9ucyBhbmQgdGhlIHJlbGF0aW9uIGtleSAoaS5lLiBcImhhc0JpcnRoUGxhY2VcIilcblx0Y29uc3QgbWFrZVJlbGF0aW9uQXJncyA9IChyZWxhdGlvbiwga2V5LCBhY2NlcHRlZCA9IHRydWUsIGlkID0gbnVsbCwgcmV2ID0gbnVsbCkgPT4ge1xuXHRcdGNvbnN0IGZpZWxkRGVmID0gZmllbGREZWZzLmZpbmQoKGRlZikgPT4gZGVmLm5hbWUgPT09IGtleSk7XG5cblxuXHRcdGNvbnN0IHNvdXJjZVR5cGUgPSBkYXRhW1wiQHR5cGVcIl0ucmVwbGFjZSgvcyQvLCBcIlwiKS5yZXBsYWNlKC9ed3cvLCBcIlwiKTtcblx0XHRjb25zdCB0YXJnZXRUeXBlID0gZmllbGREZWYucmVsYXRpb24udGFyZ2V0Q29sbGVjdGlvbi5yZXBsYWNlKC9zJC8sIFwiXCIpLnJlcGxhY2UoL153dy8sIFwiXCIpO1xuXG5cdFx0Y29uc3QgcmVsYXRpb25TYXZlRGF0YSA9IHtcblx0XHRcdFwiQHR5cGVcIjogZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25Db2xsZWN0aW9uLnJlcGxhY2UoL3MkLywgXCJcIiksIC8vIGNoZWNrXG5cdFx0XHRcIl5zb3VyY2VJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHJlbGF0aW9uLmlkIDogZGF0YS5faWQsIC8vIGNoZWNrXG5cdFx0XHRcIl5zb3VyY2VUeXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gdGFyZ2V0VHlwZSA6IHNvdXJjZVR5cGUsIC8vIGNoZWNrXG5cdFx0XHRcIl50YXJnZXRJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IGRhdGEuX2lkIDogcmVsYXRpb24uaWQsIC8vIGNoZWNrXG5cdFx0XHRcIl50YXJnZXRUeXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gc291cmNlVHlwZSA6IHRhcmdldFR5cGUsXG5cdFx0XHRcIl50eXBlSWRcIjogZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25UeXBlSWQsIC8vIGNoZWNrXG5cdFx0XHRhY2NlcHRlZDogYWNjZXB0ZWRcblx0XHR9O1xuXG5cdFx0aWYoaWQpIHsgcmVsYXRpb25TYXZlRGF0YS5faWQgPSBpZDsgfVxuXHRcdGlmKHJldikgeyByZWxhdGlvblNhdmVEYXRhW1wiXnJldlwiXSA9IHJldjsgfVxuXHRcdHJldHVybiBbXG5cdFx0XHRmaWVsZERlZi5yZWxhdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24sIC8vIGRvbWFpblxuXHRcdFx0cmVsYXRpb25TYXZlRGF0YVxuXHRcdF07XG5cdH07XG5cblx0Ly8gQ29uc3RydWN0cyBhbiBhcnJheSBvZiBhcmd1bWVudHMgZm9yIHNhdmluZyBuZXcgcmVsYXRpb25zOlxuXHQvLyBbXG5cdC8vICAgW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV0sXG5cdC8vICAgW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV0sXG5cdC8vIF1cblx0Y29uc3QgbmV3UmVsYXRpb25zID0gT2JqZWN0LmtleXMocmVsYXRpb25EYXRhKS5tYXAoKGtleSkgPT5cblx0XHRcdHJlbGF0aW9uRGF0YVtrZXldXG5cdFx0XHQvLyBGaWx0ZXJzIG91dCBhbGwgcmVsYXRpb25zIHdoaWNoIGFyZSBub3QgYWxyZWFkeSBpbiBkYXRhW1wiQHJlbGF0aW9uc1wiXVxuXHRcdFx0XHQuZmlsdGVyKChyZWxhdGlvbikgPT4gKGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV0gfHwgW10pLm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uaWQpLmluZGV4T2YocmVsYXRpb24uaWQpIDwgMClcblx0XHRcdFx0Ly8gTWFrZSBhcmd1bWVudCBhcnJheSBmb3IgbmV3IHJlbGF0aW9uczogW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV1cblx0XHRcdFx0Lm1hcCgocmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3MocmVsYXRpb24sIGtleSkpXG5cdFx0Ly8gRmxhdHRlbiBuZXN0ZWQgYXJyYXlzXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cblx0Ly8gUmVhY3RpdmF0ZSBwcmV2aW91c2x5IGFkZGVkIHJlbGF0aW9ucyB1c2luZyBQVVQgd2hpY2ggd2VyZSAnZGVsZXRlZCcgYWZ0ZXIgdXNpbmcgUFVUXG5cdGNvbnN0IHJlQWRkUmVsYXRpb25zID0gT2JqZWN0LmtleXMocmVsYXRpb25EYXRhKS5tYXAoKGtleSkgPT5cblx0XHQoZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XSB8fCBbXSlcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmFjY2VwdGVkID09PSBmYWxzZSlcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gKHJlbGF0aW9uRGF0YVtrZXldIHx8IFtdKS5maWx0ZXIoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5hY2NlcHRlZCkubWFwKChyZWxhdGlvbikgPT4gcmVsYXRpb24uaWQpLmluZGV4T2Yob3JpZ1JlbGF0aW9uLmlkKSA+IC0xKVxuXHRcdFx0Lm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKG9yaWdSZWxhdGlvbiwga2V5LCB0cnVlLCBvcmlnUmVsYXRpb24ucmVsYXRpb25JZCwgb3JpZ1JlbGF0aW9uLnJldikpXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cdC8vIERlYWN0aXZhdGUgcHJldmlvdXNseSBhZGRlZCByZWxhdGlvbnMgdXNpbmcgUFVUXG5cdGNvbnN0IGRlbGV0ZVJlbGF0aW9ucyA9IE9iamVjdC5rZXlzKGRhdGFbXCJAcmVsYXRpb25zXCJdKS5tYXAoKGtleSkgPT5cblx0XHRkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5hY2NlcHRlZClcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gKHJlbGF0aW9uRGF0YVtrZXldIHx8IFtdKS5tYXAoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5pZCkuaW5kZXhPZihvcmlnUmVsYXRpb24uaWQpIDwgMClcblx0XHRcdC5tYXAoKG9yaWdSZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhvcmlnUmVsYXRpb24sIGtleSwgZmFsc2UsIG9yaWdSZWxhdGlvbi5yZWxhdGlvbklkLCBvcmlnUmVsYXRpb24ucmV2KSlcblx0KS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSk7XG5cblx0Ly8gQ29tYmluZXMgc2F2ZU5ld0VudGl0eSBhbmQgZGVsZXRlRW50aXR5IGluc3RydWN0aW9ucyBpbnRvIHByb21pc2VzXG5cdGNvbnN0IHByb21pc2VzID0gbmV3UmVsYXRpb25zXG5cdC8vIE1hcCBuZXdSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2Ygc2F2ZU5ld0VudGl0eVxuXHRcdC5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHNhdmVOZXdFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpICkpXG5cdFx0Ly8gTWFwIHJlYWRkUmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHVwZGF0ZUVudGl0eVxuXHRcdC5jb25jYXQocmVBZGRSZWxhdGlvbnMubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiB1cGRhdGVFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpKSkpXG5cdFx0Ly8gTWFwIGRlbGV0ZVJlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiB1cGRhdGVFbnRpdHlcblx0XHQuY29uY2F0KGRlbGV0ZVJlbGF0aW9ucy5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHVwZGF0ZUVudGl0eSguLi5hcmdzLCB0b2tlbiwgdnJlSWQsIHJlc29sdmUsIHJlamVjdCkpKSk7XG5cblx0Ly8gSW52b2tlIGFsbCBDUlVEIG9wZXJhdGlvbnMgZm9yIHRoZSByZWxhdGlvbnNcblx0UHJvbWlzZS5hbGwocHJvbWlzZXMpLnRoZW4obmV4dCwgbmV4dCk7XG59O1xuXG5leHBvcnQgZGVmYXVsdCBzYXZlUmVsYXRpb25zVjIxOyIsImltcG9ydCB4aHIgZnJvbSBcInhoclwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuLi9zdG9yZVwiO1xuXG5leHBvcnQgZGVmYXVsdCB7XG5cdHBlcmZvcm1YaHI6IGZ1bmN0aW9uIChvcHRpb25zLCBhY2NlcHQsIHJlamVjdCA9ICgpID0+IHsgY29uc29sZS53YXJuKFwiVW5kZWZpbmVkIHJlamVjdCBjYWxsYmFjayEgXCIpOyB9LCBvcGVyYXRpb24gPSBcIlNlcnZlciByZXF1ZXN0XCIpIHtcblx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJSRVFVRVNUX01FU1NBR0VcIiwgbWVzc2FnZTogYCR7b3BlcmF0aW9ufTogJHtvcHRpb25zLm1ldGhvZCB8fCBcIkdFVFwifSAke29wdGlvbnMudXJsfWB9KTtcblx0XHR4aHIob3B0aW9ucywgKGVyciwgcmVzcCwgYm9keSkgPT4ge1xuXHRcdFx0aWYocmVzcC5zdGF0dXNDb2RlID49IDQwMCkge1xuXHRcdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJFUlJPUl9NRVNTQUdFXCIsIG1lc3NhZ2U6IGAke29wZXJhdGlvbn0gZmFpbGVkIHdpdGggY2F1c2U6ICR7cmVzcC5ib2R5fWB9KTtcblx0XHRcdFx0cmVqZWN0KGVyciwgcmVzcCwgYm9keSk7XG5cdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRhY2NlcHQoZXJyLCByZXNwLCBib2R5KTtcblx0XHRcdH1cblx0XHR9KTtcblx0fSxcblxuXHRmYXN0WGhyOiBmdW5jdGlvbihvcHRpb25zLCBhY2NlcHQpIHtcblx0XHR4aHIob3B0aW9ucywgYWNjZXB0KTtcblx0fSxcblxuXHRtYWtlSGVhZGVyczogZnVuY3Rpb24odG9rZW4sIHZyZUlkKSB7XG5cdFx0cmV0dXJuIHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiLFxuXHRcdFx0XCJDb250ZW50LXR5cGVcIjogXCJhcHBsaWNhdGlvbi9qc29uXCIsXG5cdFx0XHRcIkF1dGhvcml6YXRpb25cIjogdG9rZW4sXG5cdFx0XHRcIlZSRV9JRFwiOiB2cmVJZFxuXHRcdH07XG5cdH1cbn07XG4iLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vaW5kZXhcIjtcbmltcG9ydCB7bWFrZU5ld0VudGl0eX0gZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQge2ZldGNoRW50aXR5TGlzdH0gZnJvbSBcIi4vZW50aXR5XCI7XG5cbmNvbnN0IGxpc3RWcmVzID0gKCkgPT4gKGRpc3BhdGNoKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiXG5cdFx0fSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9zeXN0ZW0vdnJlc2Bcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIkxJU1RfVlJFU1wiLCBsaXN0OiBKU09OLnBhcnNlKHJlc3AuYm9keSl9KTtcblx0fSwgbnVsbCwgXCJMaXN0IFZSRXNcIik7XG5cbmNvbnN0IHNldFZyZSA9ICh2cmVJZCwgbmV4dCA9ICgpID0+IHsgfSkgPT4gKGRpc3BhdGNoKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiXG5cdFx0fSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9tZXRhZGF0YS8ke3ZyZUlkfT93aXRoQ29sbGVjdGlvbkluZm89dHJ1ZWBcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGlmIChyZXNwLnN0YXR1c0NvZGUgPT09IDIwMCkge1xuXHRcdFx0dmFyIGJvZHkgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfVlJFXCIsIHZyZUlkOiB2cmVJZCwgY29sbGVjdGlvbnM6IGJvZHl9KTtcblxuXHRcdFx0bGV0IGRlZmF1bHREb21haW4gPSBPYmplY3Qua2V5cyhib2R5KVxuXHRcdFx0XHQubWFwKGNvbGxlY3Rpb25OYW1lID0+IGJvZHlbY29sbGVjdGlvbk5hbWVdKVxuXHRcdFx0XHQuZmlsdGVyKGNvbGxlY3Rpb24gPT4gIWNvbGxlY3Rpb24udW5rbm93biAmJiAhY29sbGVjdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24pWzBdXG5cdFx0XHRcdC5jb2xsZWN0aW9uTmFtZTtcblxuXHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShkZWZhdWx0RG9tYWluKSlcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9ET01BSU5cIiwgZGVmYXVsdERvbWFpbn0pO1xuXHRcdFx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGRlZmF1bHREb21haW4pKTtcblx0XHRcdG5leHQoKTtcblx0XHR9XG5cdH0sICgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9WUkVcIiwgdnJlSWQ6IHZyZUlkLCBjb2xsZWN0aW9uczoge319KSwgYEZldGNoIFZSRSBkZXNjcmlwdGlvbiBmb3IgJHt2cmVJZH1gKTtcblxuXG5leHBvcnQge2xpc3RWcmVzLCBzZXRWcmV9O1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNsYXNzbmFtZXMgZnJvbSBcImNsYXNzbmFtZXNcIjtcbmltcG9ydCB7dXJsc30gZnJvbSBcIi4uLy4uL3VybHNcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5cbmNsYXNzIENvbGxlY3Rpb25UYWJzIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGNvbGxlY3Rpb25zLCBhY3RpdmVEb21haW4sIG9uUmVkaXJlY3RUb0ZpcnN0IH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGRvbWFpbnMgPSBPYmplY3Qua2V5cyhjb2xsZWN0aW9ucyB8fCB7fSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2LXRhYnNcIj5cbiAgICAgICAgICB7ZG9tYWluc1xuICAgICAgICAgICAgLmZpbHRlcihkID0+ICEoY29sbGVjdGlvbnNbZF0udW5rbm93biB8fCBjb2xsZWN0aW9uc1tkXS5yZWxhdGlvbkNvbGxlY3Rpb24pKVxuICAgICAgICAgICAgLm1hcCgoZG9tYWluKSA9PiAoXG4gICAgICAgICAgICAgIDxsaSBjbGFzc05hbWU9e2NsYXNzbmFtZXMoe2FjdGl2ZTogZG9tYWluID09PSBhY3RpdmVEb21haW59KX0ga2V5PXtkb21haW59PlxuICAgICAgICAgICAgICAgIDxhIG9uQ2xpY2s9eygpID0+IG9uUmVkaXJlY3RUb0ZpcnN0KGRvbWFpbil9PlxuICAgICAgICAgICAgICAgICAge2NvbGxlY3Rpb25zW2RvbWFpbl0uY29sbGVjdGlvbkxhYmVsfVxuICAgICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5Db2xsZWN0aW9uVGFicy5wcm9wVHlwZXMgPSB7XG5cdG9uTmV3OiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZWxlY3REb21haW46IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRjb2xsZWN0aW9uczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0YWN0aXZlRG9tYWluOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFicztcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBQYWdlIGZyb20gXCIuLi9wYWdlLmpzeFwiO1xuXG5pbXBvcnQgUGFnaW5hdGUgZnJvbSBcIi4vZW50aXR5LWluZGV4L3BhZ2luYXRlXCI7XG5pbXBvcnQgUXVpY2tTZWFyY2ggZnJvbSBcIi4vZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoXCI7XG5pbXBvcnQgRW50aXR5TGlzdCBmcm9tIFwiLi9lbnRpdHktaW5kZXgvbGlzdFwiO1xuXG5pbXBvcnQgU2F2ZUZvb3RlciBmcm9tIFwiLi9lbnRpdHktZm9ybS9zYXZlLWZvb3RlclwiO1xuaW1wb3J0IEVudGl0eUZvcm0gZnJvbSBcIi4vZW50aXR5LWZvcm0vZm9ybVwiO1xuXG5pbXBvcnQgQ29sbGVjdGlvblRhYnMgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJzXCI7XG5pbXBvcnQgTWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXMvbGlzdFwiO1xuaW1wb3J0IE1lc3NhZ2UgZnJvbSBcIi4uL21lc3NhZ2VcIjtcblxuY2xhc3MgRWRpdEd1aSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0Y29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcblx0XHRjb25zdCB7IG9uU2VsZWN0LCBvbk5ldywgb25TZWxlY3REb21haW4gfSA9IHRoaXMucHJvcHM7XG5cblx0XHQvLyBUcmlnZ2VycyBmZXRjaCBkYXRhIGZyb20gc2VydmVyIGJhc2VkIG9uIGlkIGZyb20gcm91dGUuXG5cdFx0aWYgKHRoaXMucHJvcHMucGFyYW1zLmlkICE9PSBuZXh0UHJvcHMucGFyYW1zLmlkKSB7XG5cdFx0XHRvblNlbGVjdCh7ZG9tYWluOiBuZXh0UHJvcHMucGFyYW1zLmNvbGxlY3Rpb24sIGlkOiBuZXh0UHJvcHMucGFyYW1zLmlkfSk7XG5cdFx0fVxuXHR9XG5cblx0Y29tcG9uZW50RGlkTW91bnQoKSB7XG5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdHRoaXMucHJvcHMub25TZWxlY3Qoe2RvbWFpbjogdGhpcy5wcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IHRoaXMucHJvcHMucGFyYW1zLmlkfSk7XG5cdFx0fSBlbHNlIGlmICghdGhpcy5wcm9wcy5wYXJhbXMuY29sbGVjdGlvbiAmJiAhdGhpcy5wcm9wcy5sb2NhdGlvbi5wYXRobmFtZS5tYXRjaCgvbmV3JC8pICYmIHRoaXMucHJvcHMuZW50aXR5LmRvbWFpbikge1xuXHRcdFx0dGhpcy5wcm9wcy5vblJlZGlyZWN0VG9GaXJzdCh0aGlzLnByb3BzLmVudGl0eS5kb21haW4pXG5cdFx0fSBlbHNlIGlmICh0aGlzLnByb3BzLmxvY2F0aW9uLnBhdGhuYW1lLm1hdGNoKC9uZXckLykpIHtcblx0XHRcdHRoaXMucHJvcHMub25OZXcodGhpcy5wcm9wcy5lbnRpdHkuZG9tYWluKTtcblx0XHR9XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvblNlbGVjdCwgb25OZXcsIG9uU2F2ZSwgb25EZWxldGUsIG9uU2VsZWN0RG9tYWluLCBvbkRpc21pc3NNZXNzYWdlLCBvbkNoYW5nZSwgb25BZGRTZWxlY3RlZEZpZWxkcywgb25SZWRpcmVjdFRvRmlyc3QgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UsIG9uUXVpY2tTZWFyY2gsIG9uUGFnaW5hdGVMZWZ0LCBvblBhZ2luYXRlUmlnaHQgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBnZXRBdXRvY29tcGxldGVWYWx1ZXMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgeyBxdWlja1NlYXJjaCwgZW50aXR5LCB2cmUsIG1lc3NhZ2VzIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGN1cnJlbnRNb2RlID0gZW50aXR5LmRvbWFpbiAmJiBlbnRpdHkuZGF0YS5faWQgPyBcImVkaXRcIiA6IFwibmV3XCI7XG5cblx0XHRpZiAoZW50aXR5LmRvbWFpbiA9PT0gbnVsbCB8fCAhdnJlLmNvbGxlY3Rpb25zW2VudGl0eS5kb21haW5dKSB7IHJldHVybiBudWxsOyB9XG5cdFx0Y29uc3QgbG9naW5NZXNzYWdlID0gdGhpcy5wcm9wcy51c2VyID8gbnVsbCA6IChcblx0XHRcdDxNZXNzYWdlIGRpc21pc3NpYmxlPXtmYWxzZX0gYWxlcnRMZXZlbD1cIndhcm5pbmdcIj5cblx0XHRcdFx0PGZvcm0gYWN0aW9uPVwiaHR0cHM6Ly9zZWN1cmUuaHV5Z2Vucy5rbmF3Lm5sL3NhbWwyL2xvZ2luXCIgbWV0aG9kPVwiUE9TVFwiIHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgZmxvYXQ6IFwicmlnaHRcIn19PlxuXHRcdFx0XHRcdDxpbnB1dCBuYW1lPVwiaHN1cmxcIiB2YWx1ZT17YCR7bG9jYXRpb24uaHJlZn1gfSB0eXBlPVwiaGlkZGVuXCIgLz5cblx0XHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4td2FybmluZyBidG4tc21cIiB0eXBlPVwic3VibWl0XCI+XG5cdFx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWxvZy1pblwiIC8+IExvZyBpblxuXHRcdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0XHQ8L2Zvcm0+XG5cdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tZXhjbGFtYXRpb24tc2lnblwiIC8+e1wiIFwifVxuXHRcdFx0XHRZb3UgYXJlIG5vdCBsb2dnZWQgaW4sIHlvdXIgc2Vzc2lvbiBoYXMgZXhwaXJlZCwgb3IgeW91IGFyZSBub3QgYWxsb3dlZCB0byBlZGl0IHRoaXMgZGF0YXNldFxuXHRcdFx0PC9NZXNzYWdlPlxuXHRcdCk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PFBhZ2UgdXNlcm5hbWU9e3RoaXMucHJvcHMudXNlciAmJiB0aGlzLnByb3BzLnVzZXIudXNlckRhdGEgJiYgdGhpcy5wcm9wcy51c2VyLnVzZXJEYXRhLmRpc3BsYXlOYW1lID8gdGhpcy5wcm9wcy51c2VyLnVzZXJEYXRhLmRpc3BsYXlOYW1lIDogXCJcIn0+XG5cdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCIgc3R5bGU9e3t0ZXh0QWxpZ246IFwicmlnaHRcIn19PlxuXHRcdFx0XHRcdFRoaXMgZWRpdCBpbnRlcmZhY2UgaXMgbWFjaGluZS1nZW5lcmF0ZWQgYmFzZWQgb24gdGhlIGRhdGEtbW9kZWwuIDxhIGhyZWY9XCJodHRwczovL2dpdGh1Yi5jb20vaHV5Z2Vuc2luZy90aW1idWN0b28vaXNzdWVzL25ld1wiIHRhcmdldD1cIl9ibGFua1wiPlN1Z2dlc3Rpb25zPC9hPiBmb3IgaW1wcm92ZW1lbnQgYXJlIHZlcnkgd2VsY29tZSFcblx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDxDb2xsZWN0aW9uVGFicyBjb2xsZWN0aW9ucz17dnJlLmNvbGxlY3Rpb25zfSBvbk5ldz17b25OZXd9IG9uU2VsZWN0RG9tYWluPXtvblNlbGVjdERvbWFpbn0gb25SZWRpcmVjdFRvRmlyc3Q9e29uUmVkaXJlY3RUb0ZpcnN0fVxuXHRcdFx0XHRcdGFjdGl2ZURvbWFpbj17ZW50aXR5LmRvbWFpbn0gLz5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cblx0XHRcdFx0XHR7bG9naW5NZXNzYWdlfVxuXHRcdFx0XHRcdDxNZXNzYWdlc1xuXHRcdFx0XHRcdFx0dHlwZXM9e1tcIlNVQ0NFU1NfTUVTU0FHRVwiLCBcIkVSUk9SX01FU1NBR0VcIl19XG5cdFx0XHRcdFx0XHRtZXNzYWdlcz17bWVzc2FnZXN9XG5cdFx0XHRcdFx0XHRvbkRpc21pc3NNZXNzYWdlPXtvbkRpc21pc3NNZXNzYWdlfSAvPlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG5cdFx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC00XCI+XG5cdFx0XHRcdFx0XHRcdDxRdWlja1NlYXJjaFxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZT17b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlfVxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2g9e29uUXVpY2tTZWFyY2h9XG5cdFx0XHRcdFx0XHRcdFx0cXVlcnk9e3F1aWNrU2VhcmNoLnF1ZXJ5fSAvPlxuXHRcdFx0XHRcdFx0XHQ8RW50aXR5TGlzdFxuXHRcdFx0XHRcdFx0XHRcdHN0YXJ0PXtxdWlja1NlYXJjaC5zdGFydH1cblx0XHRcdFx0XHRcdFx0XHRsaXN0PXtxdWlja1NlYXJjaC5saXN0fVxuXHRcdFx0XHRcdFx0XHRcdG9uU2VsZWN0PXtvblNlbGVjdH1cblx0XHRcdFx0XHRcdFx0XHRkb21haW49e2VudGl0eS5kb21haW59XG5cdFx0XHRcdFx0XHRcdFx0c2VsZWN0ZWRJZD17ZW50aXR5LmRhdGEuX2lkfVxuXHRcdFx0XHRcdFx0XHRcdGVudGl0eVBlbmRpbmc9e2VudGl0eS5wZW5kaW5nfVxuXHRcdFx0XHRcdFx0XHQvPlxuXHRcdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0XHR7ZW50aXR5LnBlbmRpbmcgPyAoXG5cdFx0XHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+TG9hZGluZywgcGxlYXNlIHdhaXQuLi48L2Rpdj5cblx0XHRcdFx0XHRcdCkgOiBlbnRpdHkuZG9tYWluID8gKFxuXHRcdFx0XHRcdFx0XHQ8RW50aXR5Rm9ybSBjdXJyZW50TW9kZT17Y3VycmVudE1vZGV9IGdldEF1dG9jb21wbGV0ZVZhbHVlcz17Z2V0QXV0b2NvbXBsZXRlVmFsdWVzfVxuXHRcdFx0XHRcdFx0XHRcdG9uQWRkU2VsZWN0ZWRGaWVsZHM9e29uQWRkU2VsZWN0ZWRGaWVsZHN9XG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5PXtlbnRpdHl9IG9uTmV3PXtvbk5ld30gb25EZWxldGU9e29uRGVsZXRlfSBvbkNoYW5nZT17b25DaGFuZ2V9IHVzZXI9e3RoaXMucHJvcHMudXNlcn1cblx0XHRcdFx0XHRcdFx0XHRwcm9wZXJ0aWVzPXt2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0ucHJvcGVydGllc30gXG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5TGFiZWw9e3ZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXS5jb2xsZWN0aW9uTGFiZWwucmVwbGFjZSgvcyQvLCBcIlwiKSB9IC8+XG5cdFx0XHRcdFx0XHQpIDogbnVsbCB9XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDxkaXYgdHlwZT1cImZvb3Rlci1ib2R5XCIgY2xhc3NOYW1lPVwicm93XCI+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtNFwiIHN0eWxlPXt7dGV4dEFsaWduOiBcImxlZnRcIiwgcGFkZGluZzogJzAnfX0+XG5cdFx0XHRcdFx0XHQ8UGFnaW5hdGVcblx0XHRcdFx0XHRcdFx0c3RhcnQ9e3F1aWNrU2VhcmNoLnN0YXJ0fVxuXHRcdFx0XHRcdFx0XHRsaXN0TGVuZ3RoPXtxdWlja1NlYXJjaC5saXN0Lmxlbmd0aH1cblx0XHRcdFx0XHRcdFx0cm93cz17NTB9XG5cdFx0XHRcdFx0XHRcdG9uUGFnaW5hdGVMZWZ0PXtvblBhZ2luYXRlTGVmdH1cblx0XHRcdFx0XHRcdFx0b25QYWdpbmF0ZVJpZ2h0PXtvblBhZ2luYXRlUmlnaHR9IC8+XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtOFwiIHN0eWxlPXt7dGV4dEFsaWduOiBcImxlZnRcIiwgcGFkZGluZzogJzAnfX0+XG5cdFx0XHRcdFx0XHR7IWVudGl0eS5wZW5kaW5nID9cblx0XHRcdFx0XHRcdFx0PFNhdmVGb290ZXIgb25TYXZlPXtvblNhdmV9IG9uQ2FuY2VsPXsoKSA9PiBjdXJyZW50TW9kZSA9PT0gXCJlZGl0XCIgP1xuXHRcdFx0XHRcdFx0XHRcdG9uU2VsZWN0KHtkb21haW46IGVudGl0eS5kb21haW4sIGlkOiBlbnRpdHkuZGF0YS5faWR9KSA6IG9uTmV3KGVudGl0eS5kb21haW4pfSB1c2VyPXt0aGlzLnByb3BzLnVzZXJ9Lz4gOiBudWxsXG5cdFx0XHRcdFx0XHR9XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0PC9QYWdlPlxuXHRcdClcblx0fVxufVxuXG5leHBvcnQgZGVmYXVsdCBFZGl0R3VpO1xuIiwiZXhwb3J0IGRlZmF1bHQgKGNhbWVsQ2FzZSkgPT4gY2FtZWxDYXNlXG4gIC5yZXBsYWNlKC8oW0EtWjAtOV0pL2csIChtYXRjaCkgPT4gYCAke21hdGNoLnRvTG93ZXJDYXNlKCl9YClcbiAgLnJlcGxhY2UoL14uLywgKG1hdGNoKSA9PiBtYXRjaC50b1VwcGVyQ2FzZSgpKTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdGNvbnN0cnVjdG9yKHByb3BzKSB7XG5cdFx0c3VwZXIocHJvcHMpO1xuXG5cdFx0dGhpcy5zdGF0ZSA9IHsgbmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIiB9O1xuXHR9XG5cblx0Y29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcblx0XHRpZiAobmV4dFByb3BzLmVudGl0eS5kYXRhLl9pZCAhPT0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YS5faWQpIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld0xhYmVsOiBcIlwiLCBuZXdVcmw6IFwiXCJ9KVxuXHRcdH1cblx0fVxuXG5cdG9uQWRkKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRpZiAodGhpcy5zdGF0ZS5uZXdMYWJlbC5sZW5ndGggPiAwICYmIHRoaXMuc3RhdGUubmV3VXJsLmxlbmd0aCA+IDApIHtcblx0XHRcdG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQoe1xuXHRcdFx0XHRsYWJlbDogdGhpcy5zdGF0ZS5uZXdMYWJlbCxcblx0XHRcdFx0dXJsOiB0aGlzLnN0YXRlLm5ld1VybFxuXHRcdFx0fSkpO1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIn0pO1xuXHRcdH1cblx0fVxuXG5cdG9uUmVtb3ZlKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV1cblx0XHRcdC5maWx0ZXIoKHZhbCkgPT4gdmFsLnVybCAhPT0gdmFsdWUudXJsKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlLnVybH0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+XG5cdFx0XHRcdFx0PGEgaHJlZj17dmFsdWUudXJsfSB0YXJnZXQ9XCJfYmxhbmtcIj5cblx0XHRcdFx0XHRcdHt2YWx1ZS5sYWJlbH1cblx0XHRcdFx0XHQ8L2E+XG5cdFx0XHRcdDwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwifX0+XG5cdFx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sIHB1bGwtbGVmdFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld0xhYmVsfVxuXHRcdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IGV2LnRhcmdldC52YWx1ZX0pfVxuXHRcdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJMYWJlbCBmb3IgdXJsLi4uXCJcblx0XHRcdFx0XHRcdHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgbWF4V2lkdGg6IFwiNTAlXCJ9fSAvPlxuXHRcdFx0XHRcdDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbCBwdWxsLWxlZnRcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdVcmx9XG5cdFx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdVcmw6IGV2LnRhcmdldC52YWx1ZX0pfVxuXHRcdFx0XHRcdFx0b25LZXlQcmVzcz17KGV2KSA9PiBldi5rZXkgPT09IFwiRW50ZXJcIiA/IHRoaXMub25BZGQoKSA6IGZhbHNlfVxuXHRcdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJVcmwuLi5cIlxuXHRcdFx0XHRcdFx0c3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXhXaWR0aDogXCJjYWxjKDUwJSAtIDgwcHgpXCJ9fSAvPlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0biBwdWxsLWxlZnRcIj5cblx0XHRcdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfT5BZGQgbGluazwvYnV0dG9uPlxuXHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0PC9kaXY+XG5cblx0XHRcdFx0PGRpdiBzdHlsZT17e3dpZHRoOiBcIjEwMCVcIiwgY2xlYXI6IFwibGVmdFwifX0gLz5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRjb25zdHJ1Y3Rvcihwcm9wcykge1xuXHRcdHN1cGVyKHByb3BzKTtcblxuXHRcdHRoaXMuc3RhdGUgPSB7IG5ld1ZhbHVlOiBcIlwiIH07XG5cdH1cblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGlmIChuZXh0UHJvcHMuZW50aXR5LmRhdGEuX2lkICE9PSB0aGlzLnByb3BzLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3VmFsdWU6IFwiXCJ9KVxuXHRcdH1cblx0fVxuXG5cdG9uQWRkKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQodmFsdWUpKTtcblx0fVxuXG5cdG9uUmVtb3ZlKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKCh2YWwpID0+IHZhbCAhPT0gdmFsdWUpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWV9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPnt2YWx1ZX08L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50c31cblx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3VmFsdWV9XG5cdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3VmFsdWU6IGV2LnRhcmdldC52YWx1ZX0pfVxuXHRcdFx0XHRcdG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyB0aGlzLm9uQWRkKGV2LnRhcmdldC52YWx1ZSkgOiBmYWxzZX1cblx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkFkZCBhIHZhbHVlLi4uXCIgLz5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vLi4vLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0b25BZGQodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh2YWx1ZSkpO1xuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKHZhbCkgPT4gdmFsICE9PSB2YWx1ZSkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZX0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e3ZhbHVlfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8U2VsZWN0RmllbGQgb25DaGFuZ2U9e3RoaXMub25BZGQuYmluZCh0aGlzKX0gbm9DbGVhcj17dHJ1ZX0gYnRuQ2xhc3M9XCJidG4tZGVmYXVsdFwiPlxuXHRcdFx0XHRcdDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPlxuXHRcdFx0XHRcdFx0U2VsZWN0IHtsYWJlbC50b0xvd2VyQ2FzZSgpfVxuXHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0XHR7b3B0aW9ucy5maWx0ZXIoKG9wdCkgPT4gdmFsdWVzLmluZGV4T2Yob3B0KSA8IDApLm1hcCgob3B0aW9uKSA9PiAoXG5cdFx0XHRcdFx0XHQ8c3BhbiBrZXk9e29wdGlvbn0gdmFsdWU9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG5cdFx0XHRcdFx0KSl9XG5cdFx0XHRcdDwvU2VsZWN0RmllbGQ+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheVxufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vLi4vLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgb25BZGQoKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHtcbiAgICAgIGNvbXBvbmVudHM6IFt7dHlwZTogb3B0aW9uc1swXSwgdmFsdWU6IFwiXCJ9XVxuICAgIH0pKTtcbiAgfVxuXG4gIG9uQWRkQ29tcG9uZW50KGl0ZW1JbmRleCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLmNvbmNhdCh7dHlwZTogb3B0aW9uc1swXSwgdmFsdWU6IFwiXCJ9KVxuICAgICk7XG4gIH1cblxuICBvblJlbW92ZUNvbXBvbmVudChpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAuZmlsdGVyKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ICE9PSBjb21wb25lbnRJbmRleClcbiAgICApO1xuICB9XG5cbiAgb25DaGFuZ2VDb21wb25lbnRWYWx1ZShpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4LCB2YWx1ZSkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLm1hcCgoY29tcG9uZW50LCBpZHgpID0+IGlkeCA9PT0gY29tcG9uZW50SW5kZXhcbiAgICAgICAgPyB7Li4uY29tcG9uZW50LCB2YWx1ZTogdmFsdWV9IDogY29tcG9uZW50XG4gICAgKSk7XG4gIH1cblxuICBvbkNoYW5nZUNvbXBvbmVudFR5cGUoaXRlbUluZGV4LCBjb21wb25lbnRJbmRleCwgdHlwZSkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLm1hcCgoY29tcG9uZW50LCBpZHgpID0+IGlkeCA9PT0gY29tcG9uZW50SW5kZXhcbiAgICAgICAgPyB7Li4uY29tcG9uZW50LCB0eXBlOiB0eXBlfSA6IGNvbXBvbmVudFxuICAgICkpO1xuICB9XG5cbiAgb25SZW1vdmUoaXRlbUluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigobmFtZSwgaWR4KSA9PiBpZHggIT09IGl0ZW1JbmRleCkpO1xuICB9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblxuICAgIGNvbnN0IG5hbWVFbGVtZW50cyA9IHZhbHVlcy5tYXAoKG5hbWUsIGkpID0+IChcbiAgICAgIDxkaXYga2V5PXtgJHtuYW1lfS0ke2l9YH0gY2xhc3NOYW1lPVwibmFtZXMtZm9ybSBpdGVtLWVsZW1lbnRcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzbWFsbC1tYXJnaW5cIj5cbiAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuICAgICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZShpKX1cbiAgICAgICAgICAgIHR5cGU9XCJidXR0b25cIj5cbiAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICA8c3Ryb25nPlxuICAgICAgICAgICAge25hbWUuY29tcG9uZW50cy5tYXAoKGNvbXBvbmVudCkgPT4gY29tcG9uZW50LnZhbHVlKS5qb2luKFwiIFwiKX1cbiAgICAgICAgICA8L3N0cm9uZz5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDx1bCBrZXk9XCJjb21wb25lbnQtbGlzdFwiPlxuICAgICAgICAgIHtuYW1lLmNvbXBvbmVudHMubWFwKChjb21wb25lbnQsIGopID0+IChcbiAgICAgICAgICAgIDxsaSBrZXk9e2Ake2l9LSR7an0tY29tcG9uZW50YH0+XG4gICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXBcIiBrZXk9XCJjb21wb25lbnQtdmFsdWVzXCI+XG4gICAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cC1idG5cIj5cbiAgICAgICAgICAgICAgICAgIDxTZWxlY3RGaWVsZCB2YWx1ZT17Y29tcG9uZW50LnR5cGV9IG5vQ2xlYXI9e3RydWV9XG4gICAgICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsodmFsKSA9PiB0aGlzLm9uQ2hhbmdlQ29tcG9uZW50VHlwZShpLCBqLCB2YWwpfVxuICAgICAgICAgICAgICAgICAgICBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG4gICAgICAgICAgICAgICAgICAgIHtvcHRpb25zLm1hcCgob3B0aW9uKSA9PiAoXG4gICAgICAgICAgICAgICAgICAgICAgPHNwYW4gdmFsdWU9e29wdGlvbn0ga2V5PXtvcHRpb259PntvcHRpb259PC9zcGFuPlxuICAgICAgICAgICAgICAgICAgICApKX1cbiAgICAgICAgICAgICAgICAgIDwvU2VsZWN0RmllbGQ+XG4gICAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgICAgPGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIga2V5PXtgaW5wdXQtJHtpfS0ke2p9YH1cbiAgICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMub25DaGFuZ2VDb21wb25lbnRWYWx1ZShpLCBqLCBldi50YXJnZXQudmFsdWUpfVxuICAgICAgICAgICAgICAgICAgcGxhY2Vob2xkZXI9e2NvbXBvbmVudC50eXBlfSB2YWx1ZT17Y29tcG9uZW50LnZhbHVlfSAvPlxuICAgICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICAgICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlQ29tcG9uZW50KGksIGopfSA+XG4gICAgICAgICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgICAgICAgIDwvc3Bhbj5cbiAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuICAgICAgICAgIDxidXR0b24gb25DbGljaz17KCkgPT4gdGhpcy5vbkFkZENvbXBvbmVudChpKX1cbiAgICAgICAgICAgICBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHQgYnRuLXhzIHB1bGwtcmlnaHRcIiB0eXBlPVwiYnV0dG9uXCI+XG4gICAgICAgICAgICBBZGQgY29tcG9uZW50XG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgPGRpdiBzdHlsZT17e3dpZHRoOiBcIjEwMCVcIiwgaGVpZ2h0OiBcIjZweFwiLCBjbGVhcjogXCJyaWdodFwifX0gLz5cbiAgICAgIDwvZGl2PlxuICAgICkpXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cbiAgICAgICAge25hbWVFbGVtZW50c31cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9PlxuICAgICAgICAgIEFkZCBuYW1lXG4gICAgICAgIDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcbiAgb3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHsgdXJscyB9IGZyb20gXCIuLi8uLi8uLi8uLi91cmxzXCI7XG5cbmNsYXNzIFJlbGF0aW9uRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3Rvcihwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBxdWVyeTogXCJcIixcbiAgICAgIHN1Z2dlc3Rpb25zOiBbXSxcbiAgICAgIGJsdXJJc0Jsb2NrZWQ6IGZhbHNlXG4gICAgfVxuICB9XG5cbiAgb25SZW1vdmUodmFsdWUpIHtcbiAgICBjb25zdCBjdXJyZW50VmFsdWVzID0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcblxuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoXG4gICAgICBbXCJAcmVsYXRpb25zXCIsIHRoaXMucHJvcHMubmFtZV0sXG4gICAgICBjdXJyZW50VmFsdWVzLmZpbHRlcigoY3VyVmFsKSA9PiBjdXJWYWwuaWQgIT09IHZhbHVlLmlkKVxuICAgICk7XG5cbiAgfVxuXG4gIG9uQWRkKHN1Z2dlc3Rpb24pIHtcbiAgICBjb25zdCBjdXJyZW50VmFsdWVzID0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcbiAgICBpZiAoY3VycmVudFZhbHVlcy5tYXAoKHZhbCkgPT4gdmFsLmlkKS5pbmRleE9mKHN1Z2dlc3Rpb24ua2V5KSA+IC0xKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXSwgcXVlcnk6IFwiXCIsIGJsdXJJc0Jsb2NrZWQ6IGZhbHNlfSk7XG5cbiAgICB0aGlzLnByb3BzLm9uQ2hhbmdlKFxuICAgICAgW1wiQHJlbGF0aW9uc1wiLCB0aGlzLnByb3BzLm5hbWVdLFxuICAgICAgY3VycmVudFZhbHVlcy5jb25jYXQoe1xuICAgICAgICBpZDogc3VnZ2VzdGlvbi5rZXksXG4gICAgICAgIGRpc3BsYXlOYW1lOiBzdWdnZXN0aW9uLnZhbHVlLFxuICAgICAgICBhY2NlcHRlZDogdHJ1ZVxuICAgICAgfSlcbiAgICApO1xuICB9XG5cbiAgb25RdWVyeUNoYW5nZShldikge1xuICAgIGNvbnN0IHsgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzLCBwYXRoIH0gPSB0aGlzLnByb3BzO1xuICAgIHRoaXMuc2V0U3RhdGUoe3F1ZXJ5OiBldi50YXJnZXQudmFsdWV9KTtcbiAgICBpZiAoZXYudGFyZ2V0LnZhbHVlID09PSBcIlwiKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogW119KTtcbiAgICB9IGVsc2Uge1xuICAgICAgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzKHBhdGgsIGV2LnRhcmdldC52YWx1ZSwgKHJlc3VsdHMpID0+IHtcbiAgICAgICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IHJlc3VsdHN9KTtcbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIG9uUXVlcnlDbGVhcihldikge1xuICAgIGlmICghdGhpcy5zdGF0ZS5ibHVySXNCbG9ja2VkKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogW10sIHF1ZXJ5OiBcIlwifSk7XG4gICAgfVxuICB9XG5cbiAgb25CbHVyQmxvY2sodG9nZ2xlKSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7Ymx1cklzQmxvY2tlZDogdG9nZ2xlfSk7XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCB0YXJnZXRDb2xsZWN0aW9uIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHZhbHVlcyA9IGVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuICAgIGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5maWx0ZXIoKHZhbCkgPT4gdmFsLmFjY2VwdGVkKS5tYXAoKHZhbHVlLCBpKSA9PiAoXG4gICAgICA8ZGl2IGtleT17YCR7aX0tJHt2YWx1ZS5pZH1gfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cbiAgICAgICAgPExpbmsgdG89e3VybHMuZW50aXR5KHRhcmdldENvbGxlY3Rpb24sIHZhbHVlLmlkKX0gPnt2YWx1ZS5kaXNwbGF5TmFtZX08L0xpbms+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG4gICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICA8L2Rpdj5cbiAgICApKTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICA8aDQ+e2NhbWVsMmxhYmVsKG5hbWUpfTwvaDQ+XG4gICAgICAgIHtpdGVtRWxlbWVudHN9XG4gICAgICAgIDxpbnB1dCBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIlxuICAgICAgICAgICAgICAgb25CbHVyPXt0aGlzLm9uUXVlcnlDbGVhci5iaW5kKHRoaXMpfVxuICAgICAgICAgICAgICAgb25DaGFuZ2U9e3RoaXMub25RdWVyeUNoYW5nZS5iaW5kKHRoaXMpfVxuICAgICAgICAgICAgICAgdmFsdWU9e3RoaXMuc3RhdGUucXVlcnl9IHBsYWNlaG9sZGVyPVwiU2VhcmNoLi4uXCIgLz5cblxuICAgICAgICA8ZGl2IG9uTW91c2VPdmVyPXsoKSA9PiB0aGlzLm9uQmx1ckJsb2NrKHRydWUpfVxuICAgICAgICAgICAgIG9uTW91c2VPdXQ9eygpID0+IHRoaXMub25CbHVyQmxvY2soZmFsc2UpfVxuICAgICAgICAgICAgIHN0eWxlPXt7b3ZlcmZsb3dZOiBcImF1dG9cIiwgbWF4SGVpZ2h0OiBcIjMwMHB4XCJ9fT5cbiAgICAgICAgICB7dGhpcy5zdGF0ZS5zdWdnZXN0aW9ucy5tYXAoKHN1Z2dlc3Rpb24sIGkpID0+IChcbiAgICAgICAgICAgIDxhIGtleT17YCR7aX0tJHtzdWdnZXN0aW9uLmtleX1gfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIlxuICAgICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uQWRkKHN1Z2dlc3Rpb24pfT5cbiAgICAgICAgICAgICAge3N1Z2dlc3Rpb24udmFsdWV9XG4gICAgICAgICAgICA8L2E+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBSZWxhdGlvbkZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uLy4uLy4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnQgPSBlbnRpdHkuZGF0YVtuYW1lXSAmJiBlbnRpdHkuZGF0YVtuYW1lXS5sZW5ndGggPiAwID8gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57ZW50aXR5LmRhdGFbbmFtZV19PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gb25DaGFuZ2UoW25hbWVdLCBcIlwiKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkgOiBudWxsO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50fVxuXHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvbkNoYW5nZShbbmFtZV0sIHZhbHVlKX1cblx0XHRcdFx0XHRub0NsZWFyPXt0cnVlfSBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG5cdFx0XHRcdFx0PHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG5cdFx0XHRcdFx0XHRTZWxlY3Qge2xhYmVsLnRvTG93ZXJDYXNlKCl9XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHRcdHtvcHRpb25zLm1hcCgob3B0aW9uKSA9PiAoXG5cdFx0XHRcdFx0XHQ8c3BhbiBrZXk9e29wdGlvbn0gdmFsdWU9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG5cdFx0XHRcdFx0KSl9XG5cdFx0XHRcdDwvU2VsZWN0RmllbGQ+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheVxufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgU3RyaW5nRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHQ8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcblx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiBvbkNoYW5nZShbbmFtZV0sIGV2LnRhcmdldC52YWx1ZSl9XG5cdFx0XHRcdFx0dmFsdWU9e2VudGl0eS5kYXRhW25hbWVdIHx8IFwiXCJ9XG5cdFx0XHRcdFx0cGxhY2Vob2xkZXI9e2BFbnRlciAke2xhYmVsLnRvTG93ZXJDYXNlKCl9YH1cblx0XHRcdFx0Lz5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuU3RyaW5nRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgU3RyaW5nRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCJcblxuaW1wb3J0IFN0cmluZ0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9zdHJpbmctZmllbGRcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvc2VsZWN0XCI7XG5pbXBvcnQgTXVsdGlTZWxlY3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvbXVsdGktc2VsZWN0XCI7XG5pbXBvcnQgUmVsYXRpb25GaWVsZCBmcm9tIFwiLi9maWVsZHMvcmVsYXRpb25cIjtcbmltcG9ydCBTdHJpbmdMaXN0RmllbGQgZnJvbSBcIi4vZmllbGRzL2xpc3Qtb2Ytc3RyaW5nc1wiO1xuaW1wb3J0IExpbmtGaWVsZCBmcm9tIFwiLi9maWVsZHMvbGlua3NcIjtcbmltcG9ydCBOYW1lc0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9uYW1lc1wiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vdXJsc1wiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2ZpZWxkcy9jYW1lbDJsYWJlbFwiO1xuXG5jb25zdCBmaWVsZE1hcCA9IHtcblx0XCJzdHJpbmdcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJ0ZXh0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwiZGF0YWJsZVwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcIm11bHRpc2VsZWN0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8TXVsdGlTZWxlY3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KSxcblx0XCJzZWxlY3RcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTZWxlY3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KSxcblx0XCJyZWxhdGlvblwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFJlbGF0aW9uRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSB0YXJnZXRDb2xsZWN0aW9uPXtmaWVsZERlZi5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9ufSBwYXRoPXtmaWVsZERlZi5xdWlja3NlYXJjaH0gLz4pLFxuICBcImxpc3Qtb2Ytc3RyaW5nc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0xpc3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcbiAgXCJsaW5rc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPExpbmtGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJuYW1lc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPE5hbWVzRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPilcbn07XG5cbmNvbnN0IGFwcGx5RmlsdGVyID0gKGZpZWxkLCBmaWx0ZXIpID0+XG4gICAgZmllbGQudG9Mb3dlckNhc2UoKS5pbmRleE9mKGZpbHRlci50b0xvd2VyQ2FzZSgpKSA+IC0xIHx8XG4gICAgY2FtZWwybGFiZWwoZmllbGQpLnRvTG93ZXJDYXNlKCkuaW5kZXhPZihmaWx0ZXIudG9Mb3dlckNhc2UoKSkgPiAtMTtcblxuY2xhc3MgRW50aXR5Rm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgZmllbGRzVG9BZGQ6IFtdLFxuICAgICAgYWRkRmllbGRGaWx0ZXI6IFwiXCJcbiAgICB9XG4gIH1cblxuICBvbkZpbHRlckNoYW5nZShldikge1xuICAgIHRoaXMuc2V0U3RhdGUoe2FkZEZpZWxkRmlsdGVyOiBldi50YXJnZXQudmFsdWV9LCAoKSA9PiB7XG4gICAgICBjb25zdCBmaWx0ZXJlZCA9IHRoaXMuZ2V0QWRkYWJsZUZpZWxkc0Zyb21Qcm9wZXJ0aWVzKCkuZmlsdGVyKHByb3AgPT4gYXBwbHlGaWx0ZXIocHJvcC5uYW1lLCB0aGlzLnN0YXRlLmFkZEZpZWxkRmlsdGVyKSk7XG4gICAgICBpZiAoZmlsdGVyZWQubGVuZ3RoID4gMCkge1xuICAgICAgICBpZiAodGhpcy5zdGF0ZS5hZGRGaWVsZEZpbHRlciA9PT0gXCJcIikge1xuICAgICAgICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiBbXX0pXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IFtmaWx0ZXJlZFswXS5uYW1lXX0pXG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9KTtcbiAgfVxuXG4gIG9uRmlsdGVyS2V5RG93bihldikge1xuICAgIGlmIChldi5rZXkgPT09IFwiRW50ZXJcIiAmJiB0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLmxlbmd0aCA+IDApIHtcbiAgICAgIHRoaXMub25BZGRTZWxlY3RlZEZpZWxkcygpO1xuICAgIH1cbiAgfVxuXG4gIHRvZ2dsZUZpZWxkVG9BZGQoZmllbGROYW1lKSB7XG4gICAgaWYgKHRoaXMuc3RhdGUuZmllbGRzVG9BZGQuaW5kZXhPZihmaWVsZE5hbWUpID4gLTEpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiB0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLmZpbHRlcigoZkFkZCkgPT4gZkFkZCAhPT0gZmllbGROYW1lKX0pO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtmaWVsZHNUb0FkZDogdGhpcy5zdGF0ZS5maWVsZHNUb0FkZC5jb25jYXQoZmllbGROYW1lKX0pO1xuICAgIH1cbiAgfVxuXG4gIG9uQWRkU2VsZWN0ZWRGaWVsZHMoKSB7XG4gICAgY29uc3QgeyBwcm9wZXJ0aWVzIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgdGhpcy5wcm9wcy5vbkFkZFNlbGVjdGVkRmllbGRzKHRoaXMuc3RhdGUuZmllbGRzVG9BZGQubWFwKChmQWRkKSA9PiAoe1xuICAgICAgbmFtZTogZkFkZCxcbiAgICAgIHR5cGU6IHByb3BlcnRpZXMuZmluZCgocHJvcCkgPT4gcHJvcC5uYW1lID09PSBmQWRkKS50eXBlXG4gICAgfSkpKTtcblxuICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiBbXSwgYWRkRmllbGRGaWx0ZXI6IFwiXCJ9KTtcbiAgfVxuXG4gIGdldEFkZGFibGVGaWVsZHNGcm9tUHJvcGVydGllcygpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgcHJvcGVydGllcyB9ID0gdGhpcy5wcm9wcztcblxuICAgIHJldHVybiBwcm9wZXJ0aWVzXG4gICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gZmllbGRNYXAuaGFzT3duUHJvcGVydHkoZmllbGREZWYudHlwZSkpXG4gICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gIWVudGl0eS5kYXRhLmhhc093blByb3BlcnR5KGZpZWxkRGVmLm5hbWUpICYmICFlbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl0uaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkpXG5cbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uRGVsZXRlLCBvbkNoYW5nZSwgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgZW50aXR5LCBjdXJyZW50TW9kZSwgcHJvcGVydGllcywgZW50aXR5TGFiZWwgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBmaWVsZHNUb0FkZCwgYWRkRmllbGRGaWx0ZXIgfSA9IHRoaXMuc3RhdGU7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtOFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgIDxMaW5rIHRvPXt1cmxzLm5ld0VudGl0eShlbnRpdHkuZG9tYWluKX0gY2xhc3NOYW1lPVwiYnRuIGJ0bi1wcmltYXJ5IHB1bGwtcmlnaHRcIj5cbiAgICAgICAgICAgIE5ldyB7ZW50aXR5TGFiZWx9XG4gICAgICAgICAgPC9MaW5rPlxuICAgICAgICA8L2Rpdj5cblxuXG4gICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+ICFmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT4gKDxkaXYga2V5PXtpfSBzdHlsZT17e1wiY29sb3JcIjogXCJyZWRcIn19PjxzdHJvbmc+RmllbGQgdHlwZSBub3Qgc3VwcG9ydGVkOiB7ZmllbGREZWYudHlwZX08L3N0cm9uZz48L2Rpdj4pKX1cblxuICAgICAgICB7cHJvcGVydGllc1xuICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gZW50aXR5LmRhdGEuaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkgfHwgZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdLmhhc093blByb3BlcnR5KGZpZWxkRGVmLm5hbWUpKVxuICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PlxuICAgICAgICAgIGZpZWxkTWFwW2ZpZWxkRGVmLnR5cGVdKGZpZWxkRGVmLCB7XG5cdFx0XHRcdFx0XHRrZXk6IGAke2l9LSR7ZmllbGREZWYubmFtZX1gLFxuXHRcdFx0XHRcdFx0ZW50aXR5OiBlbnRpdHksXG5cdFx0XHRcdFx0XHRvbkNoYW5nZTogb25DaGFuZ2UsXG5cdFx0XHRcdFx0XHRnZXRBdXRvY29tcGxldGVWYWx1ZXM6IGdldEF1dG9jb21wbGV0ZVZhbHVlc1xuXHRcdFx0XHRcdH0pXG4gICAgICAgICl9XG5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW4gYWRkLWZpZWxkLWZvcm1cIj5cbiAgICAgICAgICA8aDQ+QWRkIGZpZWxkczwvaDQ+XG4gICAgICAgICAgPGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiIHZhbHVlPXthZGRGaWVsZEZpbHRlcn0gcGxhY2Vob2xkZXI9XCJGaWx0ZXIuLi5cIlxuICAgICAgICAgICAgICAgICBvbkNoYW5nZT17dGhpcy5vbkZpbHRlckNoYW5nZS5iaW5kKHRoaXMpfVxuICAgICAgICAgICAgICAgICBvbktleVByZXNzPXt0aGlzLm9uRmlsdGVyS2V5RG93bi5iaW5kKHRoaXMpfVxuICAgICAgICAgIC8+XG4gICAgICAgICAgPGRpdiBzdHlsZT17e21heEhlaWdodDogXCIyNTBweFwiLCBvdmVyZmxvd1k6IFwiYXV0b1wifX0+XG4gICAgICAgICAgICB7dGhpcy5nZXRBZGRhYmxlRmllbGRzRnJvbVByb3BlcnRpZXMoKVxuICAgICAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gYXBwbHlGaWx0ZXIoZmllbGREZWYubmFtZSwgYWRkRmllbGRGaWx0ZXIpKVxuICAgICAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT4gKFxuICAgICAgICAgICAgICAgIDxkaXYga2V5PXtpfSBvbkNsaWNrPXsoKSA9PiB0aGlzLnRvZ2dsZUZpZWxkVG9BZGQoZmllbGREZWYubmFtZSl9XG4gICAgICAgICAgICAgICAgICAgICBjbGFzc05hbWU9e2ZpZWxkc1RvQWRkLmluZGV4T2YoZmllbGREZWYubmFtZSkgPiAtMSA/IFwic2VsZWN0ZWRcIiA6IFwiXCJ9PlxuICAgICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwicHVsbC1yaWdodFwiPih7ZmllbGREZWYudHlwZX0pPC9zcGFuPlxuICAgICAgICAgICAgICAgICAge2NhbWVsMmxhYmVsKGZpZWxkRGVmLm5hbWUpfVxuICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICApKVxuICAgICAgICAgICAgfVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17dGhpcy5vbkFkZFNlbGVjdGVkRmllbGRzLmJpbmQodGhpcyl9PkFkZCBzZWxlY3RlZCBmaWVsZHM8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIHtjdXJyZW50TW9kZSA9PT0gXCJlZGl0XCJcbiAgICAgICAgICA/ICg8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgICAgICA8aDQ+RGVsZXRlPC9oND5cbiAgICAgICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRhbmdlclwiIG9uQ2xpY2s9e29uRGVsZXRlfSBkaXNhYmxlZD17IXRoaXMucHJvcHMudXNlcn0+XG4gICAgICAgICAgICAgICAgRGVsZXRlIHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICApIDogbnVsbH1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBFbnRpdHlGb3JtO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IG9uU2F2ZSwgb25DYW5jZWwsIHVzZXIgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdj5cbiAgICAgIDxidXR0b24gZGlzYWJsZWQ9eyF1c2VyfSBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnlcIiBvbkNsaWNrPXtvblNhdmV9PlNhdmU8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn1vcntcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1saW5rXCIgb25DbGljaz17b25DYW5jZWx9PkNhbmNlbDwvYnV0dG9uPlxuICAgIDwvZGl2PlxuICApO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vdXJsc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IHN0YXJ0LCBsaXN0LCBkb21haW4sIHNlbGVjdGVkSWQsIGVudGl0eVBlbmRpbmcgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJyZXN1bHQtbGlzdCByZXN1bHQtbGlzdC1lZGl0XCI+XG4gICAgICA8b2wgc3RhcnQ9e3N0YXJ0ICsgMX0gc3R5bGU9e3tjb3VudGVyUmVzZXQ6IGBzdGVwLWNvdW50ZXIgJHtzdGFydH1gfX0+XG4gICAgICAgIHtsaXN0Lm1hcCgoZW50cnksIGkpID0+IChcbiAgICAgICAgICA8bGkga2V5PXtgJHtpfS0ke2VudHJ5Ll9pZH1gfT5cbiAgICAgICAgICAgIHtlbnRpdHlQZW5kaW5nXG4gICAgICAgICAgICAgID8gKFxuICAgICAgICAgICAgICAgIDxhIHN0eWxlPXt7XG4gICAgICAgICAgICAgICAgICBkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCB3aWR0aDogXCJjYWxjKDEwMCUgLSAzMHB4KVwiLCBoZWlnaHQ6IFwiMTAwJVwiLCBwYWRkaW5nOiBcIjAuNWVtIDBcIixcbiAgICAgICAgICAgICAgICAgIGN1cnNvcjogXCJkZWZhdWx0XCIsIG9wYWNpdHk6IFwiMC41XCIsIHRleHREZWNvcmF0aW9uOiBcIm5vbmVcIiwgZm9udFdlaWdodDogXCIzMDBcIlxuICAgICAgICAgICAgICAgIH19PlxuICAgICAgICAgICAgICAgICAge2VudHJ5W1wiQGRpc3BsYXlOYW1lXCJdfVxuICAgICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgICAgKSA6IChcbiAgICAgICAgICAgICAgICA8TGluayB0bz17dXJscy5lbnRpdHkoZG9tYWluLCBlbnRyeS5faWQpfSBzdHlsZT17e1xuICAgICAgICAgICAgICAgICAgZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgd2lkdGg6IFwiY2FsYygxMDAlIC0gMzBweClcIiwgaGVpZ2h0OiBcIjEwMCVcIiwgcGFkZGluZzogXCIwLjVlbSAwXCIsXG4gICAgICAgICAgICAgICAgICBmb250V2VpZ2h0OiBzZWxlY3RlZElkID09PSBlbnRyeS5faWQgPyBcIjUwMFwiIDogXCIzMDBcIlxuICAgICAgICAgICAgICAgIH19PlxuXG4gICAgICAgICAgICAgICAgICB7ZW50cnlbXCJAZGlzcGxheU5hbWVcIl19XG4gICAgICAgICAgICAgICAgPC9MaW5rPlxuICAgICAgICAgICAgICApXG4gICAgICAgICAgICB9XG4gICAgICAgICAgPC9saT5cbiAgICAgICAgKSl9XG4gICAgICA8L29sPlxuICAgIDwvZGl2PlxuICApXG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25QYWdpbmF0ZUxlZnQsIG9uUGFnaW5hdGVSaWdodCB9ID0gcHJvcHM7XG4gIGNvbnN0IHsgc3RhcnQsIHJvd3MsIGxpc3RMZW5ndGggfSA9IHByb3BzO1xuXG5cblxuICByZXR1cm4gKFxuICAgIDxkaXY+XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIGRpc2FibGVkPXtzdGFydCA9PT0gMH0gb25DbGljaz17b25QYWdpbmF0ZUxlZnR9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWNoZXZyb24tbGVmdFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn17c3RhcnQgKyAxfSAtIHtzdGFydCArIHJvd3N9e1wiIFwifVxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17bGlzdExlbmd0aCA8IHJvd3N9IG9uQ2xpY2s9e29uUGFnaW5hdGVSaWdodH0+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tY2hldnJvbi1yaWdodFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UsIG9uUXVpY2tTZWFyY2gsIHF1ZXJ5IH0gPSBwcm9wcztcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAgc21hbGwtbWFyZ2luIFwiPlxuICAgICAgPGlucHV0IHR5cGU9XCJ0ZXh0XCIgcGxhY2Vob2xkZXI9XCJTZWFyY2ggZm9yLi4uXCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlKGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgIG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyBvblF1aWNrU2VhcmNoKCkgOiBmYWxzZX1cbiAgICAgICAgdmFsdWU9e3F1ZXJ5fVxuICAgICAgICAvPlxuICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17b25RdWlja1NlYXJjaH0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1zZWFyY2hcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rXCIgb25DbGljaz17KCkgPT4geyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UoXCJcIik7IG9uUXVpY2tTZWFyY2goKTsgfX0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgIDwvc3Bhbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuaW1wb3J0IE1lc3NhZ2UgZnJvbSBcIi4uLy4uL21lc3NhZ2VcIjtcblxuY29uc3QgTEFCRUxTID0ge1xuXHRcIlNVQ0NFU1NfTUVTU0FHRVwiOiBcIlwiLFxuXHRcIkVSUk9SX01FU1NBR0VcIjogKFxuXHRcdDxzcGFuPlxuXHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgLz4gV2FybmluZyFcblx0XHQ8L3NwYW4+XG5cdClcbn07XG5cbmNvbnN0IEFMRVJUX0xFVkVMUyA9IHtcblx0XCJTVUNDRVNTX01FU1NBR0VcIjogXCJpbmZvXCIsXG5cdFwiRVJST1JfTUVTU0FHRVwiOiBcImRhbmdlclwiXG59O1xuXG5jbGFzcyBNZXNzYWdlcyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG1lc3NhZ2VzLCB0eXBlcywgb25EaXNtaXNzTWVzc2FnZSB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IGZpbHRlcmVkTWVzc2FnZXMgPSBtZXNzYWdlcy5sb2dcblx0XHRcdC5tYXAoKG1zZywgaWR4KSA9PiAoe21lc3NhZ2U6IG1zZy5tZXNzYWdlLCBpbmRleDogaWR4LCB0eXBlOiBtc2cudHlwZSwgZGlzbWlzc2VkOiBtc2cuZGlzbWlzc2VkIH0pKVxuXHRcdFx0LmZpbHRlcigobXNnKSA9PiB0eXBlcy5pbmRleE9mKG1zZy50eXBlKSA+IC0xICYmICFtc2cuZGlzbWlzc2VkKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2PlxuXHRcdFx0XHR7ZmlsdGVyZWRNZXNzYWdlcy5tYXAoKG1zZykgPT4gKFxuXHRcdFx0XHRcdDxNZXNzYWdlIGtleT17bXNnLmluZGV4fVxuXHRcdFx0XHRcdFx0ZGlzbWlzc2libGU9e3RydWV9XG5cdFx0XHRcdFx0XHRhbGVydExldmVsPXtBTEVSVF9MRVZFTFNbbXNnLnR5cGVdfVxuXHRcdFx0XHRcdFx0b25DbG9zZU1lc3NhZ2U9eygpID0+IG9uRGlzbWlzc01lc3NhZ2UobXNnLmluZGV4KX0+XG5cdFx0XHRcdFx0XHQ8c3Ryb25nPntMQUJFTFNbbXNnLnR5cGVdfTwvc3Ryb25nPiA8c3Bhbj57bXNnLm1lc3NhZ2V9PC9zcGFuPlxuXHRcdFx0XHRcdDwvTWVzc2FnZT5cblx0XHRcdFx0KSl9XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbk1lc3NhZ2VzLnByb3BUeXBlcyA9IHtcblx0bWVzc2FnZXM6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG9uRGlzbWlzc01lc3NhZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWQsXG5cdHR5cGVzOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXkuaXNSZXF1aXJlZFxufTtcblxuZXhwb3J0IGRlZmF1bHQgTWVzc2FnZXM7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgaXNPcGVuOiBmYWxzZVxuICAgIH07XG4gICAgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50KCkge1xuICAgIGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG4gIH1cblxuICB0b2dnbGVTZWxlY3QoKSB7XG4gICAgaWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG4gICAgfVxuICB9XG5cbiAgaGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuICAgIGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuICAgIGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe1xuICAgICAgICBpc09wZW46IGZhbHNlXG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbkNoYW5nZSwgb25DbGVhciwgdmFsdWUsIGJ0bkNsYXNzLCBub0NsZWFyIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qgc2VsZWN0ZWRPcHRpb24gPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgPT09IHZhbHVlKTtcbiAgICBjb25zdCBwbGFjZWhvbGRlciA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy50eXBlID09PSBcInBsYWNlaG9sZGVyXCIpO1xuICAgIGNvbnN0IG90aGVyT3B0aW9ucyA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSAmJiBvcHQucHJvcHMudmFsdWUgIT09IHZhbHVlKTtcblxuICAgIHJldHVybiAoXG5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImRyb3Bkb3duXCIsIHtvcGVuOiB0aGlzLnN0YXRlLmlzT3Blbn0pfT5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9e2N4KFwiYnRuXCIsIFwiZHJvcGRvd24tdG9nZ2xlXCIsIGJ0bkNsYXNzIHx8IFwiYnRuLWJsYW5rXCIpfSBvbkNsaWNrPXt0aGlzLnRvZ2dsZVNlbGVjdC5iaW5kKHRoaXMpfT5cbiAgICAgICAgICB7c2VsZWN0ZWRPcHRpb24ubGVuZ3RoID8gc2VsZWN0ZWRPcHRpb24gOiBwbGFjZWhvbGRlcn0gPHNwYW4gY2xhc3NOYW1lPVwiY2FyZXRcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cblxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwiZHJvcGRvd24tbWVudVwiPlxuICAgICAgICAgIHsgdmFsdWUgJiYgIW5vQ2xlYXIgPyAoXG4gICAgICAgICAgICA8bGk+XG4gICAgICAgICAgICAgIDxhIG9uQ2xpY2s9eygpID0+IHsgb25DbGVhcigpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpO319PlxuICAgICAgICAgICAgICAgIC0gY2xlYXIgLVxuICAgICAgICAgICAgICA8L2E+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgICAgIHtvdGhlck9wdGlvbnMubWFwKChvcHRpb24sIGkpID0+IChcbiAgICAgICAgICAgIDxsaSBrZXk9e2l9PlxuICAgICAgICAgICAgICA8YSBzdHlsZT17e2N1cnNvcjogXCJwb2ludGVyXCJ9fSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2hhbmdlKG9wdGlvbi5wcm9wcy52YWx1ZSk7IHRoaXMudG9nZ2xlU2VsZWN0KCk7IH19PntvcHRpb259PC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC91bD5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuU2VsZWN0RmllbGQucHJvcFR5cGVzID0ge1xuICBvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIG9uQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICB2YWx1ZTogUmVhY3QuUHJvcFR5cGVzLmFueSxcbiAgYnRuQ2xhc3M6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG5vQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5ib29sXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZnVuY3Rpb24gRm9vdGVyKHByb3BzKSB7XG4gIGNvbnN0IGhpTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImhpLWxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1odXlnZW5zLWluZy5zdmdcIiAvPlxuICAgIDwvZGl2PlxuICApO1xuXG4gIGNvbnN0IGNsYXJpYWhMb2dvID0gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgY29sLW1kLTFcIj5cbiAgICAgIDxpbWcgY2xhc3NOYW1lPVwibG9nb1wiIHNyYz1cImltYWdlcy9sb2dvLWNsYXJpYWguc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBmb290ZXJCb2R5ID0gUmVhY3QuQ2hpbGRyZW4uY291bnQocHJvcHMuY2hpbGRyZW4pID4gMCA/XG4gICAgUmVhY3QuQ2hpbGRyZW4ubWFwKHByb3BzLmNoaWxkcmVuLCAoY2hpbGQsIGkpID0+IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwid2hpdGUtYmFyXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAge2kgPT09IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSAtIDFcbiAgICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2hpTG9nb308ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMCBjb2wtbWQtMTAgdGV4dC1jZW50ZXJcIj57Y2hpbGR9PC9kaXY+e2NsYXJpYWhMb2dvfTwvZGl2PilcbiAgICAgICAgICAgIDogKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2NoaWxkfTwvZGl2PilcbiAgICAgICAgICB9XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKSkgOiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG4gICAgICAgICAgICB7aGlMb2dvfVxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIHtjbGFyaWFoTG9nb31cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuXG5cbiAgcmV0dXJuIChcbiAgICA8Zm9vdGVyIGNsYXNzTmFtZT1cImZvb3RlclwiPlxuICAgICAge2Zvb3RlckJvZHl9XG4gICAgPC9mb290ZXI+XG4gIClcbn1cblxuZXhwb3J0IGRlZmF1bHQgRm9vdGVyOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IGRpc21pc3NpYmxlLCBhbGVydExldmVsLCBvbkNsb3NlTWVzc2FnZX0gPSBwcm9wcztcbiAgY29uc3QgZGlzbWlzc0J1dHRvbiA9IGRpc21pc3NpYmxlXG4gICAgPyA8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzc05hbWU9XCJjbG9zZVwiIG9uQ2xpY2s9e29uQ2xvc2VNZXNzYWdlfT48c3Bhbj4mdGltZXM7PC9zcGFuPjwvYnV0dG9uPlxuICAgIDogbnVsbDtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImFsZXJ0XCIsIGBhbGVydC0ke2FsZXJ0TGV2ZWx9YCwge1wiYWxlcnQtZGlzbWlzc2libGVcIjogZGlzbWlzc2libGV9KX0gcm9sZT1cImFsZXJ0XCI+XG4gICAgICB7ZGlzbWlzc0J1dHRvbn1cbiAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICA8L2Rpdj5cbiAgKVxufTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IEZvb3RlciBmcm9tIFwiLi9mb290ZXJcIjtcblxuY29uc3QgRk9PVEVSX0hFSUdIVCA9IDgxO1xuXG5mdW5jdGlvbiBQYWdlKHByb3BzKSB7XG4gIGNvbnN0IGZvb3RlcnMgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKGNoaWxkKSA9PiBjaGlsZC5wcm9wcy50eXBlID09PSBcImZvb3Rlci1ib2R5XCIpO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJwYWdlXCI+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cInNtYWxsLW1hcmdpbiBoaS1HcmVlbiBjb250YWluZXItZmx1aWRcIj5cbiAgICAgICAgPG5hdiBjbGFzc05hbWU9XCJuYXZiYXIgXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwibmF2YmFyLWhlYWRlclwiPiA8YSBjbGFzc05hbWU9XCJuYXZiYXItYnJhbmRcIiBocmVmPVwiI1wiPjxpbWcgc3JjPVwiaW1hZ2VzL2xvZ28tdGltYnVjdG9vLnN2Z1wiIGNsYXNzTmFtZT1cImxvZ29cIiBhbHQ9XCJ0aW1idWN0b29cIi8+PC9hPiA8L2Rpdj5cbiAgICAgICAgICAgIDxkaXYgaWQ9XCJuYXZiYXJcIiBjbGFzc05hbWU9XCJuYXZiYXItY29sbGFwc2UgY29sbGFwc2VcIj5cbiAgICAgICAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXZiYXItbmF2IG5hdmJhci1yaWdodFwiPlxuICAgICAgICAgICAgICAgIHtwcm9wcy51c2VybmFtZSA/IDxsaT48YSBocmVmPXtwcm9wcy51c2VybG9jYXRpb24gfHwgJyMnfT48c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXVzZXJcIi8+IHtwcm9wcy51c2VybmFtZX08L2E+PC9saT4gOiBudWxsfVxuICAgICAgICAgICAgICA8L3VsPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvbmF2PlxuICAgICAgPC9kaXY+XG4gICAgICA8ZGl2ICBzdHlsZT17e21hcmdpbkJvdHRvbTogYCR7Rk9PVEVSX0hFSUdIVCAqIGZvb3RlcnMubGVuZ3RofXB4YH19PlxuICAgICAgICB7UmVhY3QuQ2hpbGRyZW4udG9BcnJheShwcm9wcy5jaGlsZHJlbikuZmlsdGVyKChjaGlsZCkgPT4gY2hpbGQucHJvcHMudHlwZSAhPT0gXCJmb290ZXItYm9keVwiKX1cbiAgICAgIDwvZGl2PlxuICAgICAgPEZvb3Rlcj5cbiAgICAgICAge2Zvb3RlcnN9XG4gICAgICA8L0Zvb3Rlcj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgUGFnZTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBSZWFjdERPTSBmcm9tIFwicmVhY3QtZG9tXCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCB7c2V0VnJlfSBmcm9tIFwiLi9hY3Rpb25zL3ZyZVwiO1xuXG5pbXBvcnQgcm91dGVyIGZyb20gXCIuL3JvdXRlclwiO1xuaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5cbmNvbnN0IHNldFVzZXIgPSAodXNlcikgPT4ge1xuXHQvLyBUT0RPOiB2YWxpZGF0ZSB1c2VyIHNlc3Npb24uXG5cdGlmICh1c2VyKSB7XG5cdFx0eGhyKHtcblx0XHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL3N5c3RlbS91c2Vycy9tZS92cmVzYCxcblx0XHRcdGhlYWRlcnM6IHtcblx0XHRcdFx0J0F1dGhvcml6YXRpb24nOiB1c2VyLnRva2VuXG5cdFx0XHR9XG5cdFx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdFx0aWYgKGVyciB8fCByZXNwLnN0YXR1c0NvZGUgPj0gMzAwKSB7XG5cdFx0XHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNFU1NJT05fRVhQSVJFRFwifSk7XG5cdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdFx0XHRpZiAoIWRhdGEubWluZSB8fCBPYmplY3Qua2V5cyhkYXRhLm1pbmUpLmluZGV4T2YoZ2V0VnJlSWQoKSkgPCAwKSB7XG5cdFx0XHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiRVJST1JfTUVTU0FHRVwiLCBtZXNzYWdlOiBcIllvdSBhcmUgbm90IGFsbG93ZWQgdG8gZWRpdCB0aGlzIHZyZVwifSk7XG5cdFx0XHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VTU0lPTl9FWFBJUkVEXCJ9KTtcblx0XHRcdFx0fVxuXHRcdFx0fVxuXHRcdH0pO1xuXG5cdFx0eGhyKHtcblx0XHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL3N5c3RlbS91c2Vycy9tZWAsXG5cdFx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcdCdBdXRob3JpemF0aW9uJzogdXNlci50b2tlblxuXHRcdFx0fVxuXHRcdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRcdHRyeSB7XG5cdFx0XHRcdGNvbnN0IHVzZXJEYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTRVRfVVNFUl9EQVRBXCIsIHVzZXJEYXRhOiB1c2VyRGF0YX0pO1xuXHRcdFx0fSBjYXRjaCAoZSkge1xuXHRcdFx0XHRjb25zb2xlLndhcm4oZSk7XG5cdFx0XHR9XG5cdFx0fSk7XG5cdH1cblxuXHRyZXR1cm4ge1xuXHRcdHR5cGU6IFwiU0VUX1VTRVJcIixcblx0XHR1c2VyOiB1c2VyXG5cdH07XG59O1xuXG5mdW5jdGlvbiBnZXRWcmVJZCgpIHtcblx0bGV0IHBhdGggPSB3aW5kb3cubG9jYXRpb24uc2VhcmNoLnN1YnN0cigxKTtcblx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdGZvcihsZXQgaSBpbiBwYXJhbXMpIHtcblx0XHRsZXQgW2tleSwgdmFsdWVdID0gcGFyYW1zW2ldLnNwbGl0KFwiPVwiKTtcblx0XHRpZihrZXkgPT09IFwidnJlSWRcIikge1xuXHRcdFx0cmV0dXJuIHZhbHVlO1xuXHRcdH1cblx0fVxuXHRyZXR1cm4gXCJXb21lbldyaXRlcnNcIjtcbn1cblxuZG9jdW1lbnQuYWRkRXZlbnRMaXN0ZW5lcihcIkRPTUNvbnRlbnRMb2FkZWRcIiwgKCkgPT4ge1xuXG5cdGZ1bmN0aW9uIGluaXRSb3V0ZXIoKSB7XG5cdFx0UmVhY3RET00ucmVuZGVyKHJvdXRlciwgZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoXCJhcHBcIikpO1xuXHR9XG5cblxuXG5cdGZ1bmN0aW9uIGdldExvZ2luKCkge1xuXHRcdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdFx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdFx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdFx0bGV0IFtrZXksIHZhbHVlXSA9IHBhcmFtc1tpXS5zcGxpdChcIj1cIik7XG5cdFx0XHRpZihrZXkgPT09IFwiaHNpZFwiKSB7XG5cdFx0XHRcdGxvY2FsU3RvcmFnZS5zZXRJdGVtKFwidG9rZW5cIiwgSlNPTi5zdHJpbmdpZnkoe3VzZXI6IHZhbHVlLCB0b2tlbjogdmFsdWV9KSk7XG5cdFx0XHRcdGxvY2F0aW9uLmhyZWYgPSB3aW5kb3cubG9jYXRpb24uaHJlZi5yZXBsYWNlKFwiaHNpZD1cIiArIHZhbHVlLCBcIlwiKTtcblx0XHRcdFx0cmV0dXJuO1xuXHRcdFx0fVxuXHRcdH1cblx0XHRyZXR1cm4gSlNPTi5wYXJzZShsb2NhbFN0b3JhZ2UuZ2V0SXRlbShcInRva2VuXCIpIHx8IFwibnVsbFwiKTtcblx0fVxuXG5cdHN0b3JlLmRpc3BhdGNoKHNldFZyZShnZXRWcmVJZCgpLCBpbml0Um91dGVyKSk7XG5cdHN0b3JlLmRpc3BhdGNoKHNldFVzZXIoZ2V0TG9naW4oKSkpO1xufSk7IiwiaW1wb3J0IHNldEluIGZyb20gXCIuLi91dGlsL3NldC1pblwiO1xuXG5sZXQgaW5pdGlhbFN0YXRlID0ge1xuXHRkYXRhOiB7XG5cdFx0XCJAcmVsYXRpb25zXCI6IFtdXG5cdH0sXG5cdGRvbWFpbjogbnVsbCxcblx0ZXJyb3JNZXNzYWdlOiBudWxsLFxuXHRwZW5kaW5nOiBmYWxzZVxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXG5cdFx0Y2FzZSBcIkJFRk9SRV9GRVRDSF9FTlRJVFlcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YToge1xuXHRcdFx0XHRcdFwiQHJlbGF0aW9uc1wiOiBbXVxuXHRcdFx0XHR9LFxuXHRcdFx0XHRwZW5kaW5nOiB0cnVlXG5cdFx0XHR9fTtcblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YTogYWN0aW9uLmRhdGEsXG5cdFx0XHRcdGRvbWFpbjogYWN0aW9uLmRvbWFpbixcblx0XHRcdFx0ZXJyb3JNZXNzYWdlOiBhY3Rpb24uZXJyb3JNZXNzYWdlIHx8IG51bGwsXG5cdFx0XHRcdHBlbmRpbmc6IGZhbHNlXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IHNldEluKGFjdGlvbi5maWVsZFBhdGgsIGFjdGlvbi52YWx1ZSwgc3RhdGUuZGF0YSlcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZX0ZBSUxVUkVcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YToge1xuXHRcdFx0XHRcdFwiQHJlbGF0aW9uc1wiOiBbXVxuXHRcdFx0XHR9LFxuXHRcdFx0XHRlcnJvck1lc3NhZ2U6IGFjdGlvbi5lcnJvck1lc3NhZ2UsXG5cdFx0XHRcdHBlbmRpbmc6IGZhbHNlXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJTRVRfVlJFXCI6IHtcblx0XHRcdHJldHVybiBpbml0aWFsU3RhdGU7XG5cdFx0fVxuXG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwiaW1wb3J0IHtjb21iaW5lUmVkdWNlcnN9IGZyb20gXCJyZWR1eFwiO1xuXG5pbXBvcnQgZW50aXR5IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IG1lc3NhZ2VzIGZyb20gXCIuL21lc3NhZ2VzXCI7XG5pbXBvcnQgdXNlciBmcm9tIFwiLi91c2VyXCI7XG5pbXBvcnQgdnJlIGZyb20gXCIuL3ZyZVwiO1xuaW1wb3J0IHF1aWNrU2VhcmNoIGZyb20gXCIuL3F1aWNrLXNlYXJjaFwiO1xuXG5leHBvcnQgZGVmYXVsdCBjb21iaW5lUmVkdWNlcnMoe1xuXHR2cmU6IHZyZSxcblx0ZW50aXR5OiBlbnRpdHksXG5cdHVzZXI6IHVzZXIsXG5cdG1lc3NhZ2VzOiBtZXNzYWdlcyxcblx0cXVpY2tTZWFyY2g6IHF1aWNrU2VhcmNoXG59KTsiLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcblx0bG9nOiBbXVxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJSRVFVRVNUX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiU1VDQ0VTU19NRVNTQUdFXCI6XG5cdFx0XHRzdGF0ZS5sb2cucHVzaCh7bWVzc2FnZTogYWN0aW9uLm1lc3NhZ2UsIHR5cGU6IGFjdGlvbi50eXBlLCB0aW1lOiBuZXcgRGF0ZSgpfSk7XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0Y2FzZSBcIkVSUk9SX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiRElTTUlTU19NRVNTQUdFXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0bG9nOiBzZXRJbihbYWN0aW9uLm1lc3NhZ2VJbmRleCwgXCJkaXNtaXNzZWRcIl0sIHRydWUsIHN0YXRlLmxvZylcblx0XHRcdH07XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0c3RhcnQ6IDAsXG5cdGxpc3Q6IFtdLFxuXHRyb3dzOiA1MCxcblx0cXVlcnk6IFwiXCJcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIHN0YXJ0OiBhY3Rpb24uc3RhcnR9O1xuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWV9MSVNUXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5kYXRhXG5cdFx0XHR9fTtcblx0XHRjYXNlIFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCI6IHtcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0cXVlcnk6IGFjdGlvbi52YWx1ZVxuXHRcdFx0fX07XG5cdFx0fVxuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0gbnVsbDtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfVVNFUlwiOlxuXHRcdFx0aWYgKGFjdGlvbi51c2VyKSB7XG5cdFx0XHRcdHJldHVybiBhY3Rpb24udXNlcjtcblx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRcdH1cblx0XHRcdGJyZWFrO1xuXHRcdGNhc2UgXCJTRVNTSU9OX0VYUElSRURcIjpcblx0XHRcdHJldHVybiBudWxsO1xuXHRcdGNhc2UgXCJTRVRfVVNFUl9EQVRBXCI6XG5cdFx0XHRyZXR1cm4gc3RhdGVcblx0XHRcdFx0PyB7Li4uc3RhdGUsIHVzZXJEYXRhOiBhY3Rpb24udXNlckRhdGEgfVxuXHRcdFx0XHQ6IG51bGw7XG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImxldCBpbml0aWFsU3RhdGUgPSB7XG5cdHZyZUlkOiBudWxsLFxuXHRsaXN0OiBbXSxcblx0Y29sbGVjdGlvbnM6IHt9LFxuXHRkb21haW46IG51bGxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1ZSRVwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdHZyZUlkOiBhY3Rpb24udnJlSWQsXG5cdFx0XHRcdGNvbGxlY3Rpb25zOiBhY3Rpb24uY29sbGVjdGlvbnMgfHwgbnVsbCxcblx0XHRcdFx0bGlzdDogYWN0aW9uLmxpc3QgfHwgc3RhdGUubGlzdFxuXHRcdFx0fTtcblxuXHRcdGNhc2UgXCJMSVNUX1ZSRVNcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRsaXN0OiBhY3Rpb24ubGlzdCxcblx0XHRcdFx0Y29sbGVjdGlvbnM6IG51bGxcblx0XHRcdH07XG5cdFx0Y2FzZSBcIlNFVF9ET01BSU5cIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRkb21haW46IGFjdGlvbi5kb21haW5cblx0XHRcdH07XG5cblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHtSb3V0ZXIsIFJlZGlyZWN0LCBSb3V0ZSwgaGFzaEhpc3Rvcnl9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7UHJvdmlkZXIsIGNvbm5lY3R9IGZyb20gXCJyZWFjdC1yZWR1eFwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuL3N0b3JlXCI7XG5pbXBvcnQgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIGZyb20gXCIuL2FjdGlvbnMvYXV0b2NvbXBsZXRlXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9hY3Rpb25zXCI7XG5cbmltcG9ydCBFZGl0R3VpIGZyb20gXCIuL2NvbXBvbmVudHMvZWRpdC1ndWkvZWRpdC1ndWlcIjtcbmltcG9ydCB7dXJsc30gZnJvbSBcIi4vdXJsc1wiO1xuXG5leHBvcnQgZnVuY3Rpb24gbmF2aWdhdGVUbyhrZXksIGFyZ3MpIHtcblx0aGFzaEhpc3RvcnkucHVzaCh1cmxzW2tleV0uYXBwbHkobnVsbCwgYXJncykpO1xufVxuXG5jb25zdCBkZWZhdWx0Q29ubmVjdCA9IGNvbm5lY3QoXG5cdHN0YXRlID0+ICh7Li4uc3RhdGUsIGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzfSksXG5cdGRpc3BhdGNoID0+IGFjdGlvbnMobmF2aWdhdGVUbywgZGlzcGF0Y2gpXG4pO1xuXG5cbmV4cG9ydCBkZWZhdWx0IChcblx0PFByb3ZpZGVyIHN0b3JlPXtzdG9yZX0+XG5cdFx0PFJvdXRlciBoaXN0b3J5PXtoYXNoSGlzdG9yeX0+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5yb290KCl9IGNvbXBvbmVudHM9e2RlZmF1bHRDb25uZWN0KEVkaXRHdWkpfSAvPlxuXHRcdFx0PFJvdXRlIHBhdGg9e3VybHMubmV3RW50aXR5KCl9IGNvbXBvbmVudHM9e2RlZmF1bHRDb25uZWN0KEVkaXRHdWkpfSAvPlxuXHRcdFx0PFJvdXRlIHBhdGg9e3VybHMuZW50aXR5KCl9IGNvbXBvbmVudHM9e2RlZmF1bHRDb25uZWN0KEVkaXRHdWkpfSAvPlxuXHRcdDwvUm91dGVyPlxuXHQ8L1Byb3ZpZGVyPlxuKTtcbiIsImltcG9ydCB7Y3JlYXRlU3RvcmUsIGFwcGx5TWlkZGxld2FyZX0gZnJvbSBcInJlZHV4XCI7XG5pbXBvcnQgdGh1bmtNaWRkbGV3YXJlIGZyb20gXCJyZWR1eC10aHVua1wiO1xuXG5pbXBvcnQgcmVkdWNlcnMgZnJvbSBcIi4uL3JlZHVjZXJzXCI7XG5cbmNvbnN0IGxvZ2dlciA9ICgpID0+IG5leHQgPT4gYWN0aW9uID0+IHtcbiAgaWYgKGFjdGlvbi5oYXNPd25Qcm9wZXJ0eShcInR5cGVcIikpIHtcbiAgICBjb25zb2xlLmxvZyhcIltSRURVWF1cIiwgYWN0aW9uLnR5cGUsIGFjdGlvbik7XG4gIH1cblxuICByZXR1cm4gbmV4dChhY3Rpb24pO1xufTtcblxubGV0IGNyZWF0ZVN0b3JlV2l0aE1pZGRsZXdhcmUgPSBhcHBseU1pZGRsZXdhcmUoLypsb2dnZXIsKi8gdGh1bmtNaWRkbGV3YXJlKShjcmVhdGVTdG9yZSk7XG5leHBvcnQgZGVmYXVsdCBjcmVhdGVTdG9yZVdpdGhNaWRkbGV3YXJlKHJlZHVjZXJzKTtcbiIsImNvbnN0IHVybHMgPSB7XG5cdHJvb3QoKSB7XG5cdFx0cmV0dXJuIFwiL1wiO1xuXHR9LFxuXHRuZXdFbnRpdHkoY29sbGVjdGlvbikge1xuXHRcdHJldHVybiBjb2xsZWN0aW9uXG5cdFx0XHQ/IGAvJHtjb2xsZWN0aW9ufS9uZXdgXG5cdFx0XHQ6IFwiLzpjb2xsZWN0aW9uL25ld1wiO1xuXHR9LFxuXHRlbnRpdHkoY29sbGVjdGlvbiwgaWQpIHtcblx0XHRyZXR1cm4gY29sbGVjdGlvbiAmJiBpZFxuXHRcdFx0PyBgLyR7Y29sbGVjdGlvbn0vJHtpZH1gXG5cdFx0XHQ6IFwiLzpjb2xsZWN0aW9uLzppZFwiO1xuXHR9XG59O1xuXG5leHBvcnQgeyB1cmxzIH0iLCJmdW5jdGlvbiBkZWVwQ2xvbmU5KG9iaikge1xuICAgIHZhciBpLCBsZW4sIHJldDtcblxuICAgIGlmICh0eXBlb2Ygb2JqICE9PSBcIm9iamVjdFwiIHx8IG9iaiA9PT0gbnVsbCkge1xuICAgICAgICByZXR1cm4gb2JqO1xuICAgIH1cblxuICAgIGlmIChBcnJheS5pc0FycmF5KG9iaikpIHtcbiAgICAgICAgcmV0ID0gW107XG4gICAgICAgIGxlbiA9IG9iai5sZW5ndGg7XG4gICAgICAgIGZvciAoaSA9IDA7IGkgPCBsZW47IGkrKykge1xuICAgICAgICAgICAgcmV0LnB1c2goICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV0gKTtcbiAgICAgICAgfVxuICAgIH0gZWxzZSB7XG4gICAgICAgIHJldCA9IHt9O1xuICAgICAgICBmb3IgKGkgaW4gb2JqKSB7XG4gICAgICAgICAgICBpZiAob2JqLmhhc093blByb3BlcnR5KGkpKSB7XG4gICAgICAgICAgICAgICAgcmV0W2ldID0gKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gcmV0O1xufVxuXG5leHBvcnQgZGVmYXVsdCBkZWVwQ2xvbmU5OyIsImltcG9ydCBjbG9uZSBmcm9tIFwiLi9jbG9uZS1kZWVwXCI7XG5cbi8vIERvIGVpdGhlciBvZiB0aGVzZTpcbi8vICBhKSBTZXQgYSB2YWx1ZSBieSByZWZlcmVuY2UgaWYgZGVyZWYgaXMgbm90IG51bGxcbi8vICBiKSBTZXQgYSB2YWx1ZSBkaXJlY3RseSBpbiB0byBkYXRhIG9iamVjdCBpZiBkZXJlZiBpcyBudWxsXG5jb25zdCBzZXRFaXRoZXIgPSAoZGF0YSwgZGVyZWYsIGtleSwgdmFsKSA9PiB7XG5cdChkZXJlZiB8fCBkYXRhKVtrZXldID0gdmFsO1xuXHRyZXR1cm4gZGF0YTtcbn07XG5cbi8vIFNldCBhIG5lc3RlZCB2YWx1ZSBpbiBkYXRhIChub3QgdW5saWtlIGltbXV0YWJsZWpzLCBidXQgYSBjbG9uZSBvZiBkYXRhIGlzIGV4cGVjdGVkIGZvciBwcm9wZXIgaW1tdXRhYmlsaXR5KVxuY29uc3QgX3NldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA9IG51bGwpID0+XG5cdHBhdGgubGVuZ3RoID4gMSA/XG5cdFx0X3NldEluKHBhdGgsIHZhbHVlLCBkYXRhLCBkZXJlZiA/IGRlcmVmW3BhdGguc2hpZnQoKV0gOiBkYXRhW3BhdGguc2hpZnQoKV0pIDpcblx0XHRzZXRFaXRoZXIoZGF0YSwgZGVyZWYsIHBhdGhbMF0sIHZhbHVlKTtcblxuY29uc3Qgc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEpID0+XG5cdF9zZXRJbihjbG9uZShwYXRoKSwgdmFsdWUsIGNsb25lKGRhdGEpKTtcblxuZXhwb3J0IGRlZmF1bHQgc2V0SW47Il19
