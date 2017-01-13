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
	return function (dispatch, getState) {
		dispatch({ type: "SET_PAGINATION_START", start: 0 });
		_crud.crud.fetchEntityList(domain, 0, getState().quickSearch.rows, function (data) {
			dispatch({ type: "RECEIVE_ENTITY_LIST", data: data });
			next(data);
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
				null,
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

var _vre = require("./actions/vre");

var _router = require("./router");

var _router2 = _interopRequireDefault(_router);

var _xhr = require("xhr");

var _xhr2 = _interopRequireDefault(_xhr);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var setUser = function setUser(response) {
	// TODO: validate user session.
	(0, _xhr2.default)({
		url: "" + "/v2.1/system/users/me/vres",
		headers: {
			'Authorization': response.token
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
	return {
		type: "SET_USER",
		user: response
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
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDRWUsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFJLFVBQVU7QUFDYixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEYSxFQUFkOztBQUlBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxPQUFLLEtBQUssS0FBTCxDQUFXLElBQVgsRUFBaUIsR0FBakIsQ0FBcUIsVUFBQyxDQUFELEVBQU87QUFBRSxVQUFPLEVB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QUFoQixJQUF1QixHQUF2QjtBQUNBLFFBQU8sSUFBUDtBQUNBLENBSEQ7O0FBS0E7QUFDQSxJQUFNLFNBQVMsU0FBVCxNQUFTLENBQUMsSUFBRCxFQUFPLEtBQVAsRUFBYyxJQUFkO0FBQUEsS0FBb0IsS0FBcEIsdUVBQTRCLElBQTVCO0FBQUEsUUFDZCxLQUFLLE1BQUwsR0FBYyxDQUFkLEdBQ0MsT0FBTyxJQUFQLEVBQWEsS0FBYixFQUFvQixJQUFwQixFQUEwQixRQUFRLE1BQU0sS0FBSyxLQUFMLEVBQU4sQ0FBUixHQUE4QixLQUFLLEtBQUssS0FBTCxFQUFMLENBQXhELENBREQsR0FFQyxVQUFVLElBQVYsRUFBZ0IsS0FBaEIsRUFBdUIsS0FBSyxDQUFMLENBQXZCLEVBQWdDLEtBQWhDLENBSGE7QUFBQSxDQUFmOztBQUtBLElBQU0sUUFBUSxTQUFSLEtBQVEsQ0FBQyxJQUFELEVBQU8sS0FBUCxFQUFjLElBQWQ7QUFBQSxRQUNiLE9BQU8seUJBQU0sSUFBTixDQUFQLEVBQW9CLEtBQXBCLEVBQTJCLHlCQUFNLElBQU4sQ0FBM0IsQ0FEYTtBQUFBLENBQWQ7O2tCQUdlLEsiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocGF0aCwgcXVlcnksIGRvbmUpIHtcblx0bGV0IG9wdGlvbnMgPSB7XG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvJHtwYXRoLnJlcGxhY2UoL15cXC92W14vXStcXC8vLCBcIlwiKX0/cXVlcnk9JHtxdWVyeX0qYFxuXHR9O1xuXG5cdGxldCB4aHJEb25lID0gZnVuY3Rpb24oZXJyLCByZXNwb25zZSwgYm9keSkge1xuXHRcdGRvbmUoSlNPTi5wYXJzZShib2R5KS5tYXAoKGQpID0+IHsgcmV0dXJuIHtrZXk6IGQua2V5LnJlcGxhY2UoL14uK1xcLy8sIFwiXCIpLCB2YWx1ZTogZC52YWx1ZX07IH0pKTtcblx0fTtcblxuXHRzZXJ2ZXIuZmFzdFhocihvcHRpb25zLCB4aHJEb25lKTtcbn0iLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuXG5jb25zdCBzYXZlTmV3RW50aXR5ID0gKGRvbWFpbiwgc2F2ZURhdGEsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJQT1NUXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0Ym9keTogSlNPTi5zdHJpbmdpZnkoc2F2ZURhdGEpLFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn1gXG5cdH0sIG5leHQsIGZhaWwsIGBDcmVhdGUgbmV3ICR7ZG9tYWlufWApO1xuXG5jb25zdCB1cGRhdGVFbnRpdHkgPSAoZG9tYWluLCBzYXZlRGF0YSwgdG9rZW4sIHZyZUlkLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIlBVVFwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdGJvZHk6IEpTT04uc3RyaW5naWZ5KHNhdmVEYXRhKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7c2F2ZURhdGEuX2lkfWBcblx0fSwgbmV4dCwgZmFpbCwgYFVwZGF0ZSAke2RvbWFpbn1gKTtcblxuY29uc3QgZGVsZXRlRW50aXR5ID0gKGRvbWFpbiwgZW50aXR5SWQsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJERUxFVEVcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7ZW50aXR5SWR9YFxuXHR9LCBuZXh0LCBmYWlsLCBgRGVsZXRlICR7ZG9tYWlufWApO1xuXG5jb25zdCBmZXRjaEVudGl0eSA9IChsb2NhdGlvbiwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJ9LFxuXHRcdHVybDogbG9jYXRpb25cblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGNvbnN0IGRhdGEgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0bmV4dChkYXRhKTtcblx0fSwgZmFpbCwgXCJGZXRjaCBlbnRpdHlcIik7XG5cbmNvbnN0IGZldGNoRW50aXR5TGlzdCA9IChkb21haW4sIHN0YXJ0LCByb3dzLCBuZXh0KSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIn0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufT9yb3dzPSR7cm93c30mc3RhcnQ9JHtzdGFydH1gXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdG5leHQoZGF0YSk7XG5cdH0pO1xuXG5jb25zdCBjcnVkID0ge1xuXHRzYXZlTmV3RW50aXR5OiBzYXZlTmV3RW50aXR5LFxuXHR1cGRhdGVFbnRpdHk6IHVwZGF0ZUVudGl0eSxcblx0ZGVsZXRlRW50aXR5OiBkZWxldGVFbnRpdHksXG5cdGZldGNoRW50aXR5OiBmZXRjaEVudGl0eSxcblx0ZmV0Y2hFbnRpdHlMaXN0OiBmZXRjaEVudGl0eUxpc3Rcbn07XG5cbmV4cG9ydCB7c2F2ZU5ld0VudGl0eSwgdXBkYXRlRW50aXR5LCBkZWxldGVFbnRpdHksIGZldGNoRW50aXR5LCBmZXRjaEVudGl0eUxpc3QsIGNydWR9OyIsImltcG9ydCBjbG9uZSBmcm9tIFwiLi4vdXRpbC9jbG9uZS1kZWVwXCI7XG5pbXBvcnQgeyBjcnVkIH0gZnJvbSBcIi4vY3J1ZFwiO1xuaW1wb3J0IHNhdmVSZWxhdGlvbnMgZnJvbSBcIi4vc2F2ZS1yZWxhdGlvbnNcIjtcbmltcG9ydCBhdXRvY29tcGxldGUgZnJvbSBcIi4vYXV0b2NvbXBsZXRlXCI7XG5cbi8vIFNrZWxldG9uIGJhc2UgZGF0YSBwZXIgZmllbGQgZGVmaW5pdGlvblxuY29uc3QgaW5pdGlhbERhdGEgPSB7XG5cdG5hbWVzOiBbXSxcblx0bXVsdGlzZWxlY3Q6IFtdLFxuXHRsaW5rczogW10sXG5cdGtleXdvcmQ6IFtdLFxuXHRcImxpc3Qtb2Ytc3RyaW5nc1wiOiBbXSxcblx0YWx0bmFtZXM6IFtdLFxuXHR0ZXh0OiBcIlwiLFxuXHRzdHJpbmc6IFwiXCIsXG5cdHNlbGVjdDogXCJcIixcblx0ZGF0YWJsZTogXCJcIlxufTtcblxuLy8gUmV0dXJuIHRoZSBpbml0aWFsIGRhdGEgZm9yIHRoZSB0eXBlIGluIHRoZSBmaWVsZCBkZWZpbml0aW9uXG5jb25zdCBpbml0aWFsRGF0YUZvclR5cGUgPSAoZmllbGREZWYpID0+XG5cdGZpZWxkRGVmLmRlZmF1bHRWYWx1ZSB8fCAoZmllbGREZWYudHlwZSA9PT0gXCJyZWxhdGlvblwiIHx8IGZpZWxkRGVmLnR5cGUgPT09IFwia2V5d29yZFwiID8ge30gOiBpbml0aWFsRGF0YVtmaWVsZERlZi50eXBlXSk7XG5cbmNvbnN0IGFkZEZpZWxkc1RvRW50aXR5ID0gKGZpZWxkcykgPT4gKGRpc3BhdGNoKSA9PiB7XG5cdGZpZWxkcy5mb3JFYWNoKChmaWVsZCkgPT4ge1xuXHRcdGlmIChmaWVsZC50eXBlID09PSBcInJlbGF0aW9uXCIpIHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBbXCJAcmVsYXRpb25zXCIsIGZpZWxkLm5hbWVdLCB2YWx1ZTogW119KTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0VOVElUWV9GSUVMRF9WQUxVRVwiLCBmaWVsZFBhdGg6IFtmaWVsZC5uYW1lXSwgdmFsdWU6IGluaXRpYWxEYXRhRm9yVHlwZShmaWVsZCl9KTtcblx0XHR9XG5cdH0pXG59O1xuXG5jb25zdCBmZXRjaEVudGl0eUxpc3QgPSAoZG9tYWluLCBuZXh0ID0gKCkgPT4ge30pID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IDB9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZG9tYWluLCAwLCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiB7XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSk7XG5cdFx0bmV4dChkYXRhKTtcblx0fSk7XG59O1xuXG5jb25zdCBwYWdpbmF0ZUxlZnQgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IG5ld1N0YXJ0ID0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5zdGFydCAtIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cztcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3QgcGFnaW5hdGVSaWdodCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgbmV3U3RhcnQgPSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnN0YXJ0ICsgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogbmV3U3RhcnR9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3Qgc2VuZFF1aWNrU2VhcmNoID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSB9ID0gZ2V0U3RhdGUoKTtcblx0aWYgKHF1aWNrU2VhcmNoLnF1ZXJ5Lmxlbmd0aCkge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiAwfSk7XG5cdFx0Y29uc3QgY2FsbGJhY2sgPSAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhLm1hcCgoZCkgPT4gKFxuXHRcdFx0e1xuXHRcdFx0XHRfaWQ6IGQua2V5LnJlcGxhY2UoLy4qXFwvLywgXCJcIiksXG5cdFx0XHRcdFwiQGRpc3BsYXlOYW1lXCI6IGQudmFsdWVcblx0XHRcdH1cblx0XHQpKX0pO1xuXHRcdGF1dG9jb21wbGV0ZShgZG9tYWluLyR7ZW50aXR5LmRvbWFpbn0vYXV0b2NvbXBsZXRlYCwgcXVpY2tTZWFyY2gucXVlcnksIGNhbGxiYWNrKTtcblx0fSBlbHNlIHtcblx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZW50aXR5LmRvbWFpbikpO1xuXHR9XG59O1xuXG5jb25zdCBzZWxlY3REb21haW4gPSAoZG9tYWluKSA9PiAoZGlzcGF0Y2gpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RPTUFJTlwiLCBkb21haW59KTtcblx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGRvbWFpbikpO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIiwgdmFsdWU6IFwiXCJ9KTtcbn07XG5cbi8vIDEpIEZldGNoIGVudGl0eVxuLy8gMikgRGlzcGF0Y2ggUkVDRUlWRV9FTlRJVFkgZm9yIHJlbmRlclxuY29uc3Qgc2VsZWN0RW50aXR5ID0gKGRvbWFpbiwgZW50aXR5SWQsIGVycm9yTWVzc2FnZSA9IG51bGwsIHN1Y2Nlc3NNZXNzYWdlID0gbnVsbCwgbmV4dCA9ICgpID0+IHsgfSkgPT5cblx0KGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRcdGNvbnN0IHsgZW50aXR5OiB7IGRvbWFpbjogY3VycmVudERvbWFpbiB9IH0gPSBnZXRTdGF0ZSgpO1xuXHRcdGlmIChjdXJyZW50RG9tYWluICE9PSBkb21haW4pIHtcblx0XHRcdGRpc3BhdGNoKHNlbGVjdERvbWFpbihkb21haW4pKTtcblx0XHR9XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQkVGT1JFX0ZFVENIX0VOVElUWVwifSlcblx0XHRjcnVkLmZldGNoRW50aXR5KGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7ZW50aXR5SWR9YCwgKGRhdGEpID0+IHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZXCIsIGRvbWFpbjogZG9tYWluLCBkYXRhOiBkYXRhLCBlcnJvck1lc3NhZ2U6IGVycm9yTWVzc2FnZX0pO1xuXHRcdFx0aWYgKHN1Y2Nlc3NNZXNzYWdlICE9PSBudWxsKSB7XG5cdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNVQ0NFU1NfTUVTU0FHRVwiLCBtZXNzYWdlOiBzdWNjZXNzTWVzc2FnZX0pO1xuXHRcdFx0fVxuXHRcdH0sICgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0ZBSUxVUkVcIiwgZXJyb3JNZXNzYWdlOiBgRmFpbGVkIHRvIGZldGNoICR7ZG9tYWlufSB3aXRoIElEICR7ZW50aXR5SWR9YH0pKTtcblx0XHRuZXh0KCk7XG5cdH07XG5cblxuLy8gMSkgRGlzcGF0Y2ggUkVDRUlWRV9FTlRJVFkgd2l0aCBlbXB0eSBlbnRpdHkgc2tlbGV0b24gZm9yIHJlbmRlclxuY29uc3QgbWFrZU5ld0VudGl0eSA9IChkb21haW4sIGVycm9yTWVzc2FnZSA9IG51bGwpID0+XG5cdChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IGRpc3BhdGNoKHtcblx0XHR0eXBlOiBcIlJFQ0VJVkVfRU5USVRZXCIsXG5cdFx0ZG9tYWluOiBkb21haW4sXG5cdFx0ZGF0YToge1wiQHJlbGF0aW9uc1wiOiB7fX0sXG5cdFx0ZXJyb3JNZXNzYWdlOiBlcnJvck1lc3NhZ2Vcblx0fSk7XG5cbmNvbnN0IGRlbGV0ZUVudGl0eSA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y3J1ZC5kZWxldGVFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCxcblx0XHQoKSA9PiB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTVUNDRVNTX01FU1NBR0VcIiwgbWVzc2FnZTogYFN1Y2Vzc2Z1bGx5IGRlbGV0ZWQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kb21haW59IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gfSk7XG5cdFx0XHRkaXNwYXRjaChtYWtlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpO1xuXHRcdFx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpO1xuXHRcdH0sXG5cdFx0KCkgPT4gZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGBGYWlsZWQgdG8gZGVsZXRlICR7Z2V0U3RhdGUoKS5lbnRpdHkuZG9tYWlufSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YCkpKTtcbn07XG5cbi8vIDEpIFNhdmUgYW4gZW50aXR5XG4vLyAyKSBTYXZlIHRoZSByZWxhdGlvbnMgZm9yIHRoaXMgZW50aXR5XG4vLyAzKSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5jb25zdCBzYXZlRW50aXR5ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCBjb2xsZWN0aW9uTGFiZWwgPSBnZXRTdGF0ZSgpLnZyZS5jb2xsZWN0aW9uc1tnZXRTdGF0ZSgpLmVudGl0eS5kb21haW5dLmNvbGxlY3Rpb25MYWJlbC5yZXBsYWNlKC9zJC8sIFwiXCIpO1xuXG5cdC8vIE1ha2UgYSBkZWVwIGNvcHkgb2YgdGhlIGRhdGEgdG8gYmUgc2F2ZWQgaW4gb3JkZXIgdG8gbGVhdmUgYXBwbGljYXRpb24gc3RhdGUgdW5hbHRlcmVkXG5cdGxldCBzYXZlRGF0YSA9IGNsb25lKGdldFN0YXRlKCkuZW50aXR5LmRhdGEpO1xuXHQvLyBNYWtlIGEgZGVlcCBjb3B5IG9mIHRoZSByZWxhdGlvbiBkYXRhIGluIG9yZGVyIHRvIGxlYXZlIGFwcGxpY2F0aW9uIHN0YXRlIHVuYWx0ZXJlZFxuXHRsZXQgcmVsYXRpb25EYXRhID0gY2xvbmUoc2F2ZURhdGFbXCJAcmVsYXRpb25zXCJdKSB8fCB7fTtcblx0Ly8gRGVsZXRlIHRoZSByZWxhdGlvbiBkYXRhIGZyb20gdGhlIHNhdmVEYXRhIGFzIGl0IGlzIG5vdCBleHBlY3RlZCBieSB0aGUgc2VydmVyXG5cdGRlbGV0ZSBzYXZlRGF0YVtcIkByZWxhdGlvbnNcIl07XG5cblx0aWYgKGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0Ly8gMSkgVXBkYXRlIHRoZSBlbnRpdHkgd2l0aCBzYXZlRGF0YVxuXHRcdGNydWQudXBkYXRlRW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgc2F2ZURhdGEsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsIChlcnIsIHJlc3ApID0+XG5cdFx0XHQvLyAyKSBTYXZlIHJlbGF0aW9ucyB1c2luZyBzZXJ2ZXIgcmVzcG9uc2UgZm9yIGN1cnJlbnQgcmVsYXRpb25zIHRvIGRpZmYgYWdhaW5zdCByZWxhdGlvbkRhdGFcblx0XHRcdGRpc3BhdGNoKChyZWRpc3BhdGNoKSA9PiBzYXZlUmVsYXRpb25zKEpTT04ucGFyc2UocmVzcC5ib2R5KSwgcmVsYXRpb25EYXRhLCBnZXRTdGF0ZSgpLnZyZS5jb2xsZWN0aW9uc1tnZXRTdGF0ZSgpLmVudGl0eS5kb21haW5dLnByb3BlcnRpZXMsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsICgpID0+XG5cdFx0XHRcdC8vIDMpIFJlZmV0Y2ggZW50aXR5IGZvciByZW5kZXJcblx0XHRcdFx0cmVkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgbnVsbCwgYFN1Y2Nlc2Z1bGx5IHNhdmVkICR7Y29sbGVjdGlvbkxhYmVsfSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YCwgKCkgPT4gZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpKSkpKSwgKCkgPT5cblx0XHRcdFx0XHQvLyAyYSkgSGFuZGxlIGVycm9yIGJ5IHJlZmV0Y2hpbmcgYW5kIHBhc3NpbmcgYWxvbmcgYW4gZXJyb3IgbWVzc2FnZVxuXHRcdFx0XHRcdGRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBgRmFpbGVkIHRvIHNhdmUgJHtjb2xsZWN0aW9uTGFiZWx9IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gKSkpO1xuXG5cdH0gZWxzZSB7XG5cdFx0Ly8gMSkgQ3JlYXRlIG5ldyBlbnRpdHkgd2l0aCBzYXZlRGF0YVxuXHRcdGNydWQuc2F2ZU5ld0VudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIHNhdmVEYXRhLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoZXJyLCByZXNwKSA9PlxuXHRcdFx0Ly8gMikgRmV0Y2ggZW50aXR5IHZpYSBsb2NhdGlvbiBoZWFkZXJcblx0XHRcdGRpc3BhdGNoKChyZWRpc3BhdGNoKSA9PiBjcnVkLmZldGNoRW50aXR5KHJlc3AuaGVhZGVycy5sb2NhdGlvbiwgKGRhdGEpID0+XG5cdFx0XHRcdC8vIDMpIFNhdmUgcmVsYXRpb25zIHVzaW5nIHNlcnZlciByZXNwb25zZSBmb3IgY3VycmVudCByZWxhdGlvbnMgdG8gZGlmZiBhZ2FpbnN0IHJlbGF0aW9uRGF0YVxuXHRcdFx0XHRzYXZlUmVsYXRpb25zKGRhdGEsIHJlbGF0aW9uRGF0YSwgZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5wcm9wZXJ0aWVzLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoKSA9PlxuXHRcdFx0XHRcdC8vIDQpIFJlZmV0Y2ggZW50aXR5IGZvciByZW5kZXJcblx0XHRcdFx0XHRyZWRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGRhdGEuX2lkLCBudWxsLCBgU3VjY2VzZnVsbHkgc2F2ZWQgJHtjb2xsZWN0aW9uTGFiZWx9YCwgKCkgPT4gZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpKSkpKSksICgpID0+XG5cdFx0XHRcdFx0XHQvLyAyYSkgSGFuZGxlIGVycm9yIGJ5IHJlZmV0Y2hpbmcgYW5kIHBhc3NpbmcgYWxvbmcgYW4gZXJyb3IgbWVzc2FnZVxuXHRcdFx0XHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGBGYWlsZWQgdG8gc2F2ZSBuZXcgJHtjb2xsZWN0aW9uTGFiZWx9YCkpKTtcblx0fVxufTtcblxuXG5leHBvcnQgeyBzYXZlRW50aXR5LCBzZWxlY3RFbnRpdHksIG1ha2VOZXdFbnRpdHksIGRlbGV0ZUVudGl0eSwgZmV0Y2hFbnRpdHlMaXN0LCBwYWdpbmF0ZVJpZ2h0LCBwYWdpbmF0ZUxlZnQsIHNlbmRRdWlja1NlYXJjaCwgc2VsZWN0RG9tYWluLCBhZGRGaWVsZHNUb0VudGl0eSB9OyIsImltcG9ydCB7IHNhdmVFbnRpdHksIHNlbGVjdEVudGl0eSwgbWFrZU5ld0VudGl0eSwgZGVsZXRlRW50aXR5LCBhZGRGaWVsZHNUb0VudGl0eSxcblx0c2VsZWN0RG9tYWluLCBwYWdpbmF0ZUxlZnQsIHBhZ2luYXRlUmlnaHQsIHNlbmRRdWlja1NlYXJjaCwgZmV0Y2hFbnRpdHlMaXN0IH0gZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQgeyBzZXRWcmUgfSBmcm9tIFwiLi92cmVcIjtcblxuZXhwb3J0IGRlZmF1bHQgKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSA9PiAoe1xuXHRvbk5ldzogKGRvbWFpbikgPT4gZGlzcGF0Y2gobWFrZU5ld0VudGl0eShkb21haW4pKSxcblx0b25TZWxlY3Q6IChyZWNvcmQpID0+IGRpc3BhdGNoKHNlbGVjdEVudGl0eShyZWNvcmQuZG9tYWluLCByZWNvcmQuaWQpKSxcblx0b25TYXZlOiAoKSA9PiBkaXNwYXRjaChzYXZlRW50aXR5KCkpLFxuXHRvbkRlbGV0ZTogKCkgPT4gZGlzcGF0Y2goZGVsZXRlRW50aXR5KCkpLFxuXHRvbkNoYW5nZTogKGZpZWxkUGF0aCwgdmFsdWUpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBmaWVsZFBhdGgsIHZhbHVlOiB2YWx1ZX0pLFxuXHRvbkFkZFNlbGVjdGVkRmllbGRzOiAoZmllbGRzKSA9PiBkaXNwYXRjaChhZGRGaWVsZHNUb0VudGl0eShmaWVsZHMpKSxcblxuXHRvblJlZGlyZWN0VG9GaXJzdDogKGNvbGxlY3Rpb24pID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChjb2xsZWN0aW9uLCAobGlzdCkgPT4ge1xuXHRcdGlmIChsaXN0Lmxlbmd0aCA+IDApIHtcblx0XHRcdG5hdmlnYXRlVG8oJ2VudGl0eScsIFtjb2xsZWN0aW9uLCBsaXN0WzBdLl9pZF0pO1xuXHRcdH1cblx0fSkpLFxuXG5cdG9uTG9naW5DaGFuZ2U6IChyZXNwb25zZSkgPT4gZGlzcGF0Y2goc2V0VXNlcihyZXNwb25zZSkpLFxuXHRvblNlbGVjdFZyZTogKHZyZUlkKSA9PiBkaXNwYXRjaChzZXRWcmUodnJlSWQpKSxcblx0b25EaXNtaXNzTWVzc2FnZTogKG1lc3NhZ2VJbmRleCkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiRElTTUlTU19NRVNTQUdFXCIsIG1lc3NhZ2VJbmRleDogbWVzc2FnZUluZGV4fSksXG5cdG9uU2VsZWN0RG9tYWluOiAoZG9tYWluKSA9PiB7XG5cdFx0ZGlzcGF0Y2goc2VsZWN0RG9tYWluKGRvbWFpbikpO1xuXHR9LFxuXHRvblBhZ2luYXRlTGVmdDogKCkgPT4gZGlzcGF0Y2gocGFnaW5hdGVMZWZ0KCkpLFxuXHRvblBhZ2luYXRlUmlnaHQ6ICgpID0+IGRpc3BhdGNoKHBhZ2luYXRlUmlnaHQoKSksXG5cdG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZTogKHZhbHVlKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIiwgdmFsdWU6IHZhbHVlfSksXG5cdG9uUXVpY2tTZWFyY2g6ICgpID0+IGRpc3BhdGNoKHNlbmRRdWlja1NlYXJjaCgpKVxufSk7IiwiaW1wb3J0IHsgc2F2ZU5ld0VudGl0eSwgdXBkYXRlRW50aXR5IH0gZnJvbSBcIi4vY3J1ZFwiO1xuXG5jb25zdCBzYXZlUmVsYXRpb25zVjIxID0gKGRhdGEsIHJlbGF0aW9uRGF0YSwgZmllbGREZWZzLCB0b2tlbiwgdnJlSWQsIG5leHQpID0+IHtcblx0Ly8gUmV0dXJucyB0aGUgZG9tYWluIGJhc2VkIG9uIHRoZSBmaWVsZERlZmluaXRpb25zIGFuZCB0aGUgcmVsYXRpb24ga2V5IChpLmUuIFwiaGFzQmlydGhQbGFjZVwiKVxuXHRjb25zdCBtYWtlUmVsYXRpb25BcmdzID0gKHJlbGF0aW9uLCBrZXksIGFjY2VwdGVkID0gdHJ1ZSwgaWQgPSBudWxsLCByZXYgPSBudWxsKSA9PiB7XG5cdFx0Y29uc3QgZmllbGREZWYgPSBmaWVsZERlZnMuZmluZCgoZGVmKSA9PiBkZWYubmFtZSA9PT0ga2V5KTtcblxuXG5cdFx0Y29uc3Qgc291cmNlVHlwZSA9IGRhdGFbXCJAdHlwZVwiXS5yZXBsYWNlKC9zJC8sIFwiXCIpLnJlcGxhY2UoL153dy8sIFwiXCIpO1xuXHRcdGNvbnN0IHRhcmdldFR5cGUgPSBmaWVsZERlZi5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uLnJlcGxhY2UoL3MkLywgXCJcIikucmVwbGFjZSgvXnd3LywgXCJcIik7XG5cblx0XHRjb25zdCByZWxhdGlvblNhdmVEYXRhID0ge1xuXHRcdFx0XCJAdHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24ucmVwbGFjZSgvcyQvLCBcIlwiKSwgLy8gY2hlY2tcblx0XHRcdFwiXnNvdXJjZUlkXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gcmVsYXRpb24uaWQgOiBkYXRhLl9pZCwgLy8gY2hlY2tcblx0XHRcdFwiXnNvdXJjZVR5cGVcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyB0YXJnZXRUeXBlIDogc291cmNlVHlwZSwgLy8gY2hlY2tcblx0XHRcdFwiXnRhcmdldElkXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gZGF0YS5faWQgOiByZWxhdGlvbi5pZCwgLy8gY2hlY2tcblx0XHRcdFwiXnRhcmdldFR5cGVcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyBzb3VyY2VUeXBlIDogdGFyZ2V0VHlwZSxcblx0XHRcdFwiXnR5cGVJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5yZWxhdGlvblR5cGVJZCwgLy8gY2hlY2tcblx0XHRcdGFjY2VwdGVkOiBhY2NlcHRlZFxuXHRcdH07XG5cblx0XHRpZihpZCkgeyByZWxhdGlvblNhdmVEYXRhLl9pZCA9IGlkOyB9XG5cdFx0aWYocmV2KSB7IHJlbGF0aW9uU2F2ZURhdGFbXCJecmV2XCJdID0gcmV2OyB9XG5cdFx0cmV0dXJuIFtcblx0XHRcdGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uQ29sbGVjdGlvbiwgLy8gZG9tYWluXG5cdFx0XHRyZWxhdGlvblNhdmVEYXRhXG5cdFx0XTtcblx0fTtcblxuXHQvLyBDb25zdHJ1Y3RzIGFuIGFycmF5IG9mIGFyZ3VtZW50cyBmb3Igc2F2aW5nIG5ldyByZWxhdGlvbnM6XG5cdC8vIFtcblx0Ly8gICBbXCJ3d3JlbGF0aW9uc1wiLCB7IC4uLiB9XSxcblx0Ly8gICBbXCJ3d3JlbGF0aW9uc1wiLCB7IC4uLiB9XSxcblx0Ly8gXVxuXHRjb25zdCBuZXdSZWxhdGlvbnMgPSBPYmplY3Qua2V5cyhyZWxhdGlvbkRhdGEpLm1hcCgoa2V5KSA9PlxuXHRcdFx0cmVsYXRpb25EYXRhW2tleV1cblx0XHRcdC8vIEZpbHRlcnMgb3V0IGFsbCByZWxhdGlvbnMgd2hpY2ggYXJlIG5vdCBhbHJlYWR5IGluIGRhdGFbXCJAcmVsYXRpb25zXCJdXG5cdFx0XHRcdC5maWx0ZXIoKHJlbGF0aW9uKSA9PiAoZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XSB8fCBbXSkubWFwKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5pZCkuaW5kZXhPZihyZWxhdGlvbi5pZCkgPCAwKVxuXHRcdFx0XHQvLyBNYWtlIGFyZ3VtZW50IGFycmF5IGZvciBuZXcgcmVsYXRpb25zOiBbXCJ3d3JlbGF0aW9uc1wiLCB7IC4uLiB9XVxuXHRcdFx0XHQubWFwKChyZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhyZWxhdGlvbiwga2V5KSlcblx0XHQvLyBGbGF0dGVuIG5lc3RlZCBhcnJheXNcblx0KS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSk7XG5cblxuXHQvLyBSZWFjdGl2YXRlIHByZXZpb3VzbHkgYWRkZWQgcmVsYXRpb25zIHVzaW5nIFBVVCB3aGljaCB3ZXJlICdkZWxldGVkJyBhZnRlciB1c2luZyBQVVRcblx0Y29uc3QgcmVBZGRSZWxhdGlvbnMgPSBPYmplY3Qua2V5cyhyZWxhdGlvbkRhdGEpLm1hcCgoa2V5KSA9PlxuXHRcdChkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldIHx8IFtdKVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uYWNjZXB0ZWQgPT09IGZhbHNlKVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiAocmVsYXRpb25EYXRhW2tleV0gfHwgW10pLmZpbHRlcigocmVsYXRpb24pID0+IHJlbGF0aW9uLmFjY2VwdGVkKS5tYXAoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5pZCkuaW5kZXhPZihvcmlnUmVsYXRpb24uaWQpID4gLTEpXG5cdFx0XHQubWFwKChvcmlnUmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3Mob3JpZ1JlbGF0aW9uLCBrZXksIHRydWUsIG9yaWdSZWxhdGlvbi5yZWxhdGlvbklkLCBvcmlnUmVsYXRpb24ucmV2KSlcblx0KS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSk7XG5cblx0Ly8gRGVhY3RpdmF0ZSBwcmV2aW91c2x5IGFkZGVkIHJlbGF0aW9ucyB1c2luZyBQVVRcblx0Y29uc3QgZGVsZXRlUmVsYXRpb25zID0gT2JqZWN0LmtleXMoZGF0YVtcIkByZWxhdGlvbnNcIl0pLm1hcCgoa2V5KSA9PlxuXHRcdGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV1cblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmFjY2VwdGVkKVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiAocmVsYXRpb25EYXRhW2tleV0gfHwgW10pLm1hcCgocmVsYXRpb24pID0+IHJlbGF0aW9uLmlkKS5pbmRleE9mKG9yaWdSZWxhdGlvbi5pZCkgPCAwKVxuXHRcdFx0Lm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKG9yaWdSZWxhdGlvbiwga2V5LCBmYWxzZSwgb3JpZ1JlbGF0aW9uLnJlbGF0aW9uSWQsIG9yaWdSZWxhdGlvbi5yZXYpKVxuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXHQvLyBDb21iaW5lcyBzYXZlTmV3RW50aXR5IGFuZCBkZWxldGVFbnRpdHkgaW5zdHJ1Y3Rpb25zIGludG8gcHJvbWlzZXNcblx0Y29uc3QgcHJvbWlzZXMgPSBuZXdSZWxhdGlvbnNcblx0Ly8gTWFwIG5ld1JlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiBzYXZlTmV3RW50aXR5XG5cdFx0Lm1hcCgoYXJncykgPT4gbmV3IFByb21pc2UoKHJlc29sdmUsIHJlamVjdCkgPT4gc2F2ZU5ld0VudGl0eSguLi5hcmdzLCB0b2tlbiwgdnJlSWQsIHJlc29sdmUsIHJlamVjdCkgKSlcblx0XHQvLyBNYXAgcmVhZGRSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2YgdXBkYXRlRW50aXR5XG5cdFx0LmNvbmNhdChyZUFkZFJlbGF0aW9ucy5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHVwZGF0ZUVudGl0eSguLi5hcmdzLCB0b2tlbiwgdnJlSWQsIHJlc29sdmUsIHJlamVjdCkpKSlcblx0XHQvLyBNYXAgZGVsZXRlUmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHVwZGF0ZUVudGl0eVxuXHRcdC5jb25jYXQoZGVsZXRlUmVsYXRpb25zLm1hcCgoYXJncykgPT4gbmV3IFByb21pc2UoKHJlc29sdmUsIHJlamVjdCkgPT4gdXBkYXRlRW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSkpKTtcblxuXHQvLyBJbnZva2UgYWxsIENSVUQgb3BlcmF0aW9ucyBmb3IgdGhlIHJlbGF0aW9uc1xuXHRQcm9taXNlLmFsbChwcm9taXNlcykudGhlbihuZXh0LCBuZXh0KTtcbn07XG5cbmV4cG9ydCBkZWZhdWx0IHNhdmVSZWxhdGlvbnNWMjE7IiwiaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4uL3N0b3JlXCI7XG5cbmV4cG9ydCBkZWZhdWx0IHtcblx0cGVyZm9ybVhocjogZnVuY3Rpb24gKG9wdGlvbnMsIGFjY2VwdCwgcmVqZWN0ID0gKCkgPT4geyBjb25zb2xlLndhcm4oXCJVbmRlZmluZWQgcmVqZWN0IGNhbGxiYWNrISBcIik7IH0sIG9wZXJhdGlvbiA9IFwiU2VydmVyIHJlcXVlc3RcIikge1xuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlJFUVVFU1RfTUVTU0FHRVwiLCBtZXNzYWdlOiBgJHtvcGVyYXRpb259OiAke29wdGlvbnMubWV0aG9kIHx8IFwiR0VUXCJ9ICR7b3B0aW9ucy51cmx9YH0pO1xuXHRcdHhocihvcHRpb25zLCAoZXJyLCByZXNwLCBib2R5KSA9PiB7XG5cdFx0XHRpZihyZXNwLnN0YXR1c0NvZGUgPj0gNDAwKSB7XG5cdFx0XHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkVSUk9SX01FU1NBR0VcIiwgbWVzc2FnZTogYCR7b3BlcmF0aW9ufSBmYWlsZWQgd2l0aCBjYXVzZTogJHtyZXNwLmJvZHl9YH0pO1xuXHRcdFx0XHRyZWplY3QoZXJyLCByZXNwLCBib2R5KTtcblx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdGFjY2VwdChlcnIsIHJlc3AsIGJvZHkpO1xuXHRcdFx0fVxuXHRcdH0pO1xuXHR9LFxuXG5cdGZhc3RYaHI6IGZ1bmN0aW9uKG9wdGlvbnMsIGFjY2VwdCkge1xuXHRcdHhocihvcHRpb25zLCBhY2NlcHQpO1xuXHR9LFxuXG5cdG1ha2VIZWFkZXJzOiBmdW5jdGlvbih0b2tlbiwgdnJlSWQpIHtcblx0XHRyZXR1cm4ge1xuXHRcdFx0XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCIsXG5cdFx0XHRcIkNvbnRlbnQtdHlwZVwiOiBcImFwcGxpY2F0aW9uL2pzb25cIixcblx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiB0b2tlbixcblx0XHRcdFwiVlJFX0lEXCI6IHZyZUlkXG5cdFx0fTtcblx0fVxufTtcbiIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9pbmRleFwiO1xuaW1wb3J0IHttYWtlTmV3RW50aXR5fSBmcm9tIFwiLi9lbnRpdHlcIjtcbmltcG9ydCB7ZmV0Y2hFbnRpdHlMaXN0fSBmcm9tIFwiLi9lbnRpdHlcIjtcblxuY29uc3QgbGlzdFZyZXMgPSAoKSA9PiAoZGlzcGF0Y2gpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1xuXHRcdFx0XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJcblx0XHR9LFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL3N5c3RlbS92cmVzYFxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiTElTVF9WUkVTXCIsIGxpc3Q6IEpTT04ucGFyc2UocmVzcC5ib2R5KX0pO1xuXHR9LCBudWxsLCBcIkxpc3QgVlJFc1wiKTtcblxuY29uc3Qgc2V0VnJlID0gKHZyZUlkLCBuZXh0ID0gKCkgPT4geyB9KSA9PiAoZGlzcGF0Y2gpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1xuXHRcdFx0XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJcblx0XHR9LFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL21ldGFkYXRhLyR7dnJlSWR9P3dpdGhDb2xsZWN0aW9uSW5mbz10cnVlYFxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0aWYgKHJlc3Auc3RhdHVzQ29kZSA9PT0gMjAwKSB7XG5cdFx0XHR2YXIgYm9keSA9IEpTT04ucGFyc2UocmVzcC5ib2R5KTtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9WUkVcIiwgdnJlSWQ6IHZyZUlkLCBjb2xsZWN0aW9uczogYm9keX0pO1xuXG5cdFx0XHRsZXQgZGVmYXVsdERvbWFpbiA9IE9iamVjdC5rZXlzKGJvZHkpXG5cdFx0XHRcdC5tYXAoY29sbGVjdGlvbk5hbWUgPT4gYm9keVtjb2xsZWN0aW9uTmFtZV0pXG5cdFx0XHRcdC5maWx0ZXIoY29sbGVjdGlvbiA9PiAhY29sbGVjdGlvbi51bmtub3duICYmICFjb2xsZWN0aW9uLnJlbGF0aW9uQ29sbGVjdGlvbilbMF1cblx0XHRcdFx0LmNvbGxlY3Rpb25OYW1lO1xuXG5cdFx0XHRkaXNwYXRjaChtYWtlTmV3RW50aXR5KGRlZmF1bHREb21haW4pKVxuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RPTUFJTlwiLCBkZWZhdWx0RG9tYWlufSk7XG5cdFx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZGVmYXVsdERvbWFpbikpO1xuXHRcdFx0bmV4dCgpO1xuXHRcdH1cblx0fSwgKCkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1ZSRVwiLCB2cmVJZDogdnJlSWQsIGNvbGxlY3Rpb25zOiB7fX0pLCBgRmV0Y2ggVlJFIGRlc2NyaXB0aW9uIGZvciAke3ZyZUlkfWApO1xuXG5cbmV4cG9ydCB7bGlzdFZyZXMsIHNldFZyZX07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2xhc3NuYW1lcyBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuaW1wb3J0IHt1cmxzfSBmcm9tIFwiLi4vLi4vdXJsc1wiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcblxuY2xhc3MgQ29sbGVjdGlvblRhYnMgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgY29sbGVjdGlvbnMsIGFjdGl2ZURvbWFpbiwgb25SZWRpcmVjdFRvRmlyc3QgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgZG9tYWlucyA9IE9iamVjdC5rZXlzKGNvbGxlY3Rpb25zIHx8IHt9KTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXYtdGFic1wiPlxuICAgICAgICAgIHtkb21haW5zXG4gICAgICAgICAgICAuZmlsdGVyKGQgPT4gIShjb2xsZWN0aW9uc1tkXS51bmtub3duIHx8IGNvbGxlY3Rpb25zW2RdLnJlbGF0aW9uQ29sbGVjdGlvbikpXG4gICAgICAgICAgICAubWFwKChkb21haW4pID0+IChcbiAgICAgICAgICAgICAgPGxpIGNsYXNzTmFtZT17Y2xhc3NuYW1lcyh7YWN0aXZlOiBkb21haW4gPT09IGFjdGl2ZURvbWFpbn0pfSBrZXk9e2RvbWFpbn0+XG4gICAgICAgICAgICAgICAgPGEgb25DbGljaz17KCkgPT4gb25SZWRpcmVjdFRvRmlyc3QoZG9tYWluKX0+XG4gICAgICAgICAgICAgICAgICB7Y29sbGVjdGlvbnNbZG9tYWluXS5jb2xsZWN0aW9uTGFiZWx9XG4gICAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkNvbGxlY3Rpb25UYWJzLnByb3BUeXBlcyA9IHtcblx0b25OZXc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNlbGVjdERvbWFpbjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdGNvbGxlY3Rpb25zOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRhY3RpdmVEb21haW46IFJlYWN0LlByb3BUeXBlcy5zdHJpbmdcbn07XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25UYWJzO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFBhZ2UgZnJvbSBcIi4uL3BhZ2UuanN4XCI7XG5cbmltcG9ydCBQYWdpbmF0ZSBmcm9tIFwiLi9lbnRpdHktaW5kZXgvcGFnaW5hdGVcIjtcbmltcG9ydCBRdWlja1NlYXJjaCBmcm9tIFwiLi9lbnRpdHktaW5kZXgvcXVpY2tzZWFyY2hcIjtcbmltcG9ydCBFbnRpdHlMaXN0IGZyb20gXCIuL2VudGl0eS1pbmRleC9saXN0XCI7XG5cbmltcG9ydCBTYXZlRm9vdGVyIGZyb20gXCIuL2VudGl0eS1mb3JtL3NhdmUtZm9vdGVyXCI7XG5pbXBvcnQgRW50aXR5Rm9ybSBmcm9tIFwiLi9lbnRpdHktZm9ybS9mb3JtXCI7XG5cbmltcG9ydCBDb2xsZWN0aW9uVGFicyBmcm9tIFwiLi9jb2xsZWN0aW9uLXRhYnNcIjtcbmltcG9ydCBNZXNzYWdlcyBmcm9tIFwiLi9tZXNzYWdlcy9saXN0XCI7XG5pbXBvcnQgTWVzc2FnZSBmcm9tIFwiLi4vbWVzc2FnZVwiO1xuXG5jbGFzcyBFZGl0R3VpIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGNvbnN0IHsgb25TZWxlY3QsIG9uTmV3LCBvblNlbGVjdERvbWFpbiB9ID0gdGhpcy5wcm9wcztcblxuXHRcdC8vIFRyaWdnZXJzIGZldGNoIGRhdGEgZnJvbSBzZXJ2ZXIgYmFzZWQgb24gaWQgZnJvbSByb3V0ZS5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQgIT09IG5leHRQcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdG9uU2VsZWN0KHtkb21haW46IG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IG5leHRQcm9wcy5wYXJhbXMuaWR9KTtcblx0XHR9XG5cdH1cblxuXHRjb21wb25lbnREaWRNb3VudCgpIHtcblxuXHRcdGlmICh0aGlzLnByb3BzLnBhcmFtcy5pZCkge1xuXHRcdFx0dGhpcy5wcm9wcy5vblNlbGVjdCh7ZG9tYWluOiB0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uLCBpZDogdGhpcy5wcm9wcy5wYXJhbXMuaWR9KTtcblx0XHR9IGVsc2UgaWYgKCF0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uICYmICF0aGlzLnByb3BzLmxvY2F0aW9uLnBhdGhuYW1lLm1hdGNoKC9uZXckLykgJiYgdGhpcy5wcm9wcy5lbnRpdHkuZG9tYWluKSB7XG5cdFx0XHR0aGlzLnByb3BzLm9uUmVkaXJlY3RUb0ZpcnN0KHRoaXMucHJvcHMuZW50aXR5LmRvbWFpbilcblx0XHR9IGVsc2UgaWYgKHRoaXMucHJvcHMubG9jYXRpb24ucGF0aG5hbWUubWF0Y2goL25ldyQvKSkge1xuXHRcdFx0dGhpcy5wcm9wcy5vbk5ldyh0aGlzLnByb3BzLmVudGl0eS5kb21haW4pO1xuXHRcdH1cblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG9uU2VsZWN0LCBvbk5ldywgb25TYXZlLCBvbkRlbGV0ZSwgb25TZWxlY3REb21haW4sIG9uRGlzbWlzc01lc3NhZ2UsIG9uQ2hhbmdlLCBvbkFkZFNlbGVjdGVkRmllbGRzLCBvblJlZGlyZWN0VG9GaXJzdCB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZSwgb25RdWlja1NlYXJjaCwgb25QYWdpbmF0ZUxlZnQsIG9uUGFnaW5hdGVSaWdodCB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IGdldEF1dG9jb21wbGV0ZVZhbHVlcyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSwgbWVzc2FnZXMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgY3VycmVudE1vZGUgPSBlbnRpdHkuZG9tYWluICYmIGVudGl0eS5kYXRhLl9pZCA/IFwiZWRpdFwiIDogXCJuZXdcIjtcblxuXHRcdGlmIChlbnRpdHkuZG9tYWluID09PSBudWxsIHx8ICF2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0pIHsgcmV0dXJuIG51bGw7IH1cblx0XHRjb25zdCBsb2dpbk1lc3NhZ2UgPSB0aGlzLnByb3BzLnVzZXIgPyBudWxsIDogKFxuXHRcdFx0PE1lc3NhZ2UgZGlzbWlzc2libGU9e2ZhbHNlfSBhbGVydExldmVsPVwid2FybmluZ1wiPlxuXHRcdFx0XHQ8Zm9ybSBhY3Rpb249XCJodHRwczovL3NlY3VyZS5odXlnZW5zLmtuYXcubmwvc2FtbDIvbG9naW5cIiBtZXRob2Q9XCJQT1NUXCIgc3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBmbG9hdDogXCJyaWdodFwifX0+XG5cdFx0XHRcdFx0PGlucHV0IG5hbWU9XCJoc3VybFwiIHZhbHVlPXtgJHtsb2NhdGlvbi5ocmVmfWB9IHR5cGU9XCJoaWRkZW5cIiAvPlxuXHRcdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi13YXJuaW5nIGJ0bi1zbVwiIHR5cGU9XCJzdWJtaXRcIj5cblx0XHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tbG9nLWluXCIgLz4gTG9nIGluXG5cdFx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHRcdDwvZm9ybT5cblx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgLz57XCIgXCJ9XG5cdFx0XHRcdFlvdSBhcmUgbm90IGxvZ2dlZCBpbiwgeW91ciBzZXNzaW9uIGhhcyBleHBpcmVkLCBvciB5b3UgYXJlIG5vdCBhbGxvd2VkIHRvIGVkaXQgdGhpcyBkYXRhc2V0XG5cdFx0XHQ8L01lc3NhZ2U+XG5cdFx0KTtcblx0XHRyZXR1cm4gKFxuXHRcdFx0PFBhZ2U+XG5cdFx0XHRcdDxDb2xsZWN0aW9uVGFicyBjb2xsZWN0aW9ucz17dnJlLmNvbGxlY3Rpb25zfSBvbk5ldz17b25OZXd9IG9uU2VsZWN0RG9tYWluPXtvblNlbGVjdERvbWFpbn0gb25SZWRpcmVjdFRvRmlyc3Q9e29uUmVkaXJlY3RUb0ZpcnN0fVxuXHRcdFx0XHRcdGFjdGl2ZURvbWFpbj17ZW50aXR5LmRvbWFpbn0gLz5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cblx0XHRcdFx0XHR7bG9naW5NZXNzYWdlfVxuXHRcdFx0XHRcdDxNZXNzYWdlc1xuXHRcdFx0XHRcdFx0dHlwZXM9e1tcIlNVQ0NFU1NfTUVTU0FHRVwiLCBcIkVSUk9SX01FU1NBR0VcIl19XG5cdFx0XHRcdFx0XHRtZXNzYWdlcz17bWVzc2FnZXN9XG5cdFx0XHRcdFx0XHRvbkRpc21pc3NNZXNzYWdlPXtvbkRpc21pc3NNZXNzYWdlfSAvPlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG5cdFx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC00XCI+XG5cdFx0XHRcdFx0XHRcdDxRdWlja1NlYXJjaFxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZT17b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlfVxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2g9e29uUXVpY2tTZWFyY2h9XG5cdFx0XHRcdFx0XHRcdFx0cXVlcnk9e3F1aWNrU2VhcmNoLnF1ZXJ5fSAvPlxuXHRcdFx0XHRcdFx0XHQ8RW50aXR5TGlzdFxuXHRcdFx0XHRcdFx0XHRcdHN0YXJ0PXtxdWlja1NlYXJjaC5zdGFydH1cblx0XHRcdFx0XHRcdFx0XHRsaXN0PXtxdWlja1NlYXJjaC5saXN0fVxuXHRcdFx0XHRcdFx0XHRcdG9uU2VsZWN0PXtvblNlbGVjdH1cblx0XHRcdFx0XHRcdFx0XHRkb21haW49e2VudGl0eS5kb21haW59XG5cdFx0XHRcdFx0XHRcdFx0c2VsZWN0ZWRJZD17ZW50aXR5LmRhdGEuX2lkfVxuXHRcdFx0XHRcdFx0XHRcdGVudGl0eVBlbmRpbmc9e2VudGl0eS5wZW5kaW5nfVxuXHRcdFx0XHRcdFx0XHQvPlxuXHRcdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0XHR7ZW50aXR5LnBlbmRpbmcgPyAoXG5cdFx0XHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+TG9hZGluZywgcGxlYXNlIHdhaXQuLi48L2Rpdj5cblx0XHRcdFx0XHRcdCkgOiBlbnRpdHkuZG9tYWluID8gKFxuXHRcdFx0XHRcdFx0XHQ8RW50aXR5Rm9ybSBjdXJyZW50TW9kZT17Y3VycmVudE1vZGV9IGdldEF1dG9jb21wbGV0ZVZhbHVlcz17Z2V0QXV0b2NvbXBsZXRlVmFsdWVzfVxuXHRcdFx0XHRcdFx0XHRcdG9uQWRkU2VsZWN0ZWRGaWVsZHM9e29uQWRkU2VsZWN0ZWRGaWVsZHN9XG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5PXtlbnRpdHl9IG9uTmV3PXtvbk5ld30gb25EZWxldGU9e29uRGVsZXRlfSBvbkNoYW5nZT17b25DaGFuZ2V9IHVzZXI9e3RoaXMucHJvcHMudXNlcn1cblx0XHRcdFx0XHRcdFx0XHRwcm9wZXJ0aWVzPXt2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0ucHJvcGVydGllc30gXG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5TGFiZWw9e3ZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXS5jb2xsZWN0aW9uTGFiZWwucmVwbGFjZSgvcyQvLCBcIlwiKSB9IC8+XG5cdFx0XHRcdFx0XHQpIDogbnVsbCB9XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDxkaXYgdHlwZT1cImZvb3Rlci1ib2R5XCIgY2xhc3NOYW1lPVwicm93XCI+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtNFwiIHN0eWxlPXt7dGV4dEFsaWduOiBcImxlZnRcIiwgcGFkZGluZzogJzAnfX0+XG5cdFx0XHRcdFx0XHQ8UGFnaW5hdGVcblx0XHRcdFx0XHRcdFx0c3RhcnQ9e3F1aWNrU2VhcmNoLnN0YXJ0fVxuXHRcdFx0XHRcdFx0XHRsaXN0TGVuZ3RoPXtxdWlja1NlYXJjaC5saXN0Lmxlbmd0aH1cblx0XHRcdFx0XHRcdFx0cm93cz17NTB9XG5cdFx0XHRcdFx0XHRcdG9uUGFnaW5hdGVMZWZ0PXtvblBhZ2luYXRlTGVmdH1cblx0XHRcdFx0XHRcdFx0b25QYWdpbmF0ZVJpZ2h0PXtvblBhZ2luYXRlUmlnaHR9IC8+XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtOFwiIHN0eWxlPXt7dGV4dEFsaWduOiBcImxlZnRcIiwgcGFkZGluZzogJzAnfX0+XG5cdFx0XHRcdFx0XHR7IWVudGl0eS5wZW5kaW5nID9cblx0XHRcdFx0XHRcdFx0PFNhdmVGb290ZXIgb25TYXZlPXtvblNhdmV9IG9uQ2FuY2VsPXsoKSA9PiBjdXJyZW50TW9kZSA9PT0gXCJlZGl0XCIgP1xuXHRcdFx0XHRcdFx0XHRcdG9uU2VsZWN0KHtkb21haW46IGVudGl0eS5kb21haW4sIGlkOiBlbnRpdHkuZGF0YS5faWR9KSA6IG9uTmV3KGVudGl0eS5kb21haW4pfSB1c2VyPXt0aGlzLnByb3BzLnVzZXJ9Lz4gOiBudWxsXG5cdFx0XHRcdFx0XHR9XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDwvZGl2PlxuXHRcdFx0PC9QYWdlPlxuXHRcdClcblx0fVxufVxuXG5leHBvcnQgZGVmYXVsdCBFZGl0R3VpO1xuIiwiZXhwb3J0IGRlZmF1bHQgKGNhbWVsQ2FzZSkgPT4gY2FtZWxDYXNlXG4gIC5yZXBsYWNlKC8oW0EtWjAtOV0pL2csIChtYXRjaCkgPT4gYCAke21hdGNoLnRvTG93ZXJDYXNlKCl9YClcbiAgLnJlcGxhY2UoL14uLywgKG1hdGNoKSA9PiBtYXRjaC50b1VwcGVyQ2FzZSgpKTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdGNvbnN0cnVjdG9yKHByb3BzKSB7XG5cdFx0c3VwZXIocHJvcHMpO1xuXG5cdFx0dGhpcy5zdGF0ZSA9IHsgbmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIiB9O1xuXHR9XG5cblx0Y29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcblx0XHRpZiAobmV4dFByb3BzLmVudGl0eS5kYXRhLl9pZCAhPT0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YS5faWQpIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld0xhYmVsOiBcIlwiLCBuZXdVcmw6IFwiXCJ9KVxuXHRcdH1cblx0fVxuXG5cdG9uQWRkKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRpZiAodGhpcy5zdGF0ZS5uZXdMYWJlbC5sZW5ndGggPiAwICYmIHRoaXMuc3RhdGUubmV3VXJsLmxlbmd0aCA+IDApIHtcblx0XHRcdG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQoe1xuXHRcdFx0XHRsYWJlbDogdGhpcy5zdGF0ZS5uZXdMYWJlbCxcblx0XHRcdFx0dXJsOiB0aGlzLnN0YXRlLm5ld1VybFxuXHRcdFx0fSkpO1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIn0pO1xuXHRcdH1cblx0fVxuXG5cdG9uUmVtb3ZlKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV1cblx0XHRcdC5maWx0ZXIoKHZhbCkgPT4gdmFsLnVybCAhPT0gdmFsdWUudXJsKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlLnVybH0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+XG5cdFx0XHRcdFx0PGEgaHJlZj17dmFsdWUudXJsfSB0YXJnZXQ9XCJfYmxhbmtcIj5cblx0XHRcdFx0XHRcdHt2YWx1ZS5sYWJlbH1cblx0XHRcdFx0XHQ8L2E+XG5cdFx0XHRcdDwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwifX0+XG5cdFx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sIHB1bGwtbGVmdFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld0xhYmVsfVxuXHRcdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IGV2LnRhcmdldC52YWx1ZX0pfVxuXHRcdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJMYWJlbCBmb3IgdXJsLi4uXCJcblx0XHRcdFx0XHRcdHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgbWF4V2lkdGg6IFwiNTAlXCJ9fSAvPlxuXHRcdFx0XHRcdDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbCBwdWxsLWxlZnRcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdVcmx9XG5cdFx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdVcmw6IGV2LnRhcmdldC52YWx1ZX0pfVxuXHRcdFx0XHRcdFx0b25LZXlQcmVzcz17KGV2KSA9PiBldi5rZXkgPT09IFwiRW50ZXJcIiA/IHRoaXMub25BZGQoKSA6IGZhbHNlfVxuXHRcdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJVcmwuLi5cIlxuXHRcdFx0XHRcdFx0c3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXhXaWR0aDogXCJjYWxjKDUwJSAtIDgwcHgpXCJ9fSAvPlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0biBwdWxsLWxlZnRcIj5cblx0XHRcdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfT5BZGQgbGluazwvYnV0dG9uPlxuXHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0PC9kaXY+XG5cblx0XHRcdFx0PGRpdiBzdHlsZT17e3dpZHRoOiBcIjEwMCVcIiwgY2xlYXI6IFwibGVmdFwifX0gLz5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRjb25zdHJ1Y3Rvcihwcm9wcykge1xuXHRcdHN1cGVyKHByb3BzKTtcblxuXHRcdHRoaXMuc3RhdGUgPSB7IG5ld1ZhbHVlOiBcIlwiIH07XG5cdH1cblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGlmIChuZXh0UHJvcHMuZW50aXR5LmRhdGEuX2lkICE9PSB0aGlzLnByb3BzLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3VmFsdWU6IFwiXCJ9KVxuXHRcdH1cblx0fVxuXG5cdG9uQWRkKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQodmFsdWUpKTtcblx0fVxuXG5cdG9uUmVtb3ZlKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKCh2YWwpID0+IHZhbCAhPT0gdmFsdWUpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWV9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPnt2YWx1ZX08L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50c31cblx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3VmFsdWV9XG5cdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3VmFsdWU6IGV2LnRhcmdldC52YWx1ZX0pfVxuXHRcdFx0XHRcdG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyB0aGlzLm9uQWRkKGV2LnRhcmdldC52YWx1ZSkgOiBmYWxzZX1cblx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkFkZCBhIHZhbHVlLi4uXCIgLz5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vLi4vLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0b25BZGQodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh2YWx1ZSkpO1xuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKHZhbCkgPT4gdmFsICE9PSB2YWx1ZSkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZX0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e3ZhbHVlfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8U2VsZWN0RmllbGQgb25DaGFuZ2U9e3RoaXMub25BZGQuYmluZCh0aGlzKX0gbm9DbGVhcj17dHJ1ZX0gYnRuQ2xhc3M9XCJidG4tZGVmYXVsdFwiPlxuXHRcdFx0XHRcdDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPlxuXHRcdFx0XHRcdFx0U2VsZWN0IHtsYWJlbC50b0xvd2VyQ2FzZSgpfVxuXHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0XHR7b3B0aW9ucy5maWx0ZXIoKG9wdCkgPT4gdmFsdWVzLmluZGV4T2Yob3B0KSA8IDApLm1hcCgob3B0aW9uKSA9PiAoXG5cdFx0XHRcdFx0XHQ8c3BhbiBrZXk9e29wdGlvbn0gdmFsdWU9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG5cdFx0XHRcdFx0KSl9XG5cdFx0XHRcdDwvU2VsZWN0RmllbGQ+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheVxufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vLi4vLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgb25BZGQoKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHtcbiAgICAgIGNvbXBvbmVudHM6IFt7dHlwZTogb3B0aW9uc1swXSwgdmFsdWU6IFwiXCJ9XVxuICAgIH0pKTtcbiAgfVxuXG4gIG9uQWRkQ29tcG9uZW50KGl0ZW1JbmRleCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLmNvbmNhdCh7dHlwZTogb3B0aW9uc1swXSwgdmFsdWU6IFwiXCJ9KVxuICAgICk7XG4gIH1cblxuICBvblJlbW92ZUNvbXBvbmVudChpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAuZmlsdGVyKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ICE9PSBjb21wb25lbnRJbmRleClcbiAgICApO1xuICB9XG5cbiAgb25DaGFuZ2VDb21wb25lbnRWYWx1ZShpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4LCB2YWx1ZSkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLm1hcCgoY29tcG9uZW50LCBpZHgpID0+IGlkeCA9PT0gY29tcG9uZW50SW5kZXhcbiAgICAgICAgPyB7Li4uY29tcG9uZW50LCB2YWx1ZTogdmFsdWV9IDogY29tcG9uZW50XG4gICAgKSk7XG4gIH1cblxuICBvbkNoYW5nZUNvbXBvbmVudFR5cGUoaXRlbUluZGV4LCBjb21wb25lbnRJbmRleCwgdHlwZSkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLm1hcCgoY29tcG9uZW50LCBpZHgpID0+IGlkeCA9PT0gY29tcG9uZW50SW5kZXhcbiAgICAgICAgPyB7Li4uY29tcG9uZW50LCB0eXBlOiB0eXBlfSA6IGNvbXBvbmVudFxuICAgICkpO1xuICB9XG5cbiAgb25SZW1vdmUoaXRlbUluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigobmFtZSwgaWR4KSA9PiBpZHggIT09IGl0ZW1JbmRleCkpO1xuICB9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblxuICAgIGNvbnN0IG5hbWVFbGVtZW50cyA9IHZhbHVlcy5tYXAoKG5hbWUsIGkpID0+IChcbiAgICAgIDxkaXYga2V5PXtgJHtuYW1lfS0ke2l9YH0gY2xhc3NOYW1lPVwibmFtZXMtZm9ybSBpdGVtLWVsZW1lbnRcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzbWFsbC1tYXJnaW5cIj5cbiAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuICAgICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZShpKX1cbiAgICAgICAgICAgIHR5cGU9XCJidXR0b25cIj5cbiAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICA8c3Ryb25nPlxuICAgICAgICAgICAge25hbWUuY29tcG9uZW50cy5tYXAoKGNvbXBvbmVudCkgPT4gY29tcG9uZW50LnZhbHVlKS5qb2luKFwiIFwiKX1cbiAgICAgICAgICA8L3N0cm9uZz5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDx1bCBrZXk9XCJjb21wb25lbnQtbGlzdFwiPlxuICAgICAgICAgIHtuYW1lLmNvbXBvbmVudHMubWFwKChjb21wb25lbnQsIGopID0+IChcbiAgICAgICAgICAgIDxsaSBrZXk9e2Ake2l9LSR7an0tY29tcG9uZW50YH0+XG4gICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXBcIiBrZXk9XCJjb21wb25lbnQtdmFsdWVzXCI+XG4gICAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cC1idG5cIj5cbiAgICAgICAgICAgICAgICAgIDxTZWxlY3RGaWVsZCB2YWx1ZT17Y29tcG9uZW50LnR5cGV9IG5vQ2xlYXI9e3RydWV9XG4gICAgICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsodmFsKSA9PiB0aGlzLm9uQ2hhbmdlQ29tcG9uZW50VHlwZShpLCBqLCB2YWwpfVxuICAgICAgICAgICAgICAgICAgICBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG4gICAgICAgICAgICAgICAgICAgIHtvcHRpb25zLm1hcCgob3B0aW9uKSA9PiAoXG4gICAgICAgICAgICAgICAgICAgICAgPHNwYW4gdmFsdWU9e29wdGlvbn0ga2V5PXtvcHRpb259PntvcHRpb259PC9zcGFuPlxuICAgICAgICAgICAgICAgICAgICApKX1cbiAgICAgICAgICAgICAgICAgIDwvU2VsZWN0RmllbGQ+XG4gICAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgICAgPGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIga2V5PXtgaW5wdXQtJHtpfS0ke2p9YH1cbiAgICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMub25DaGFuZ2VDb21wb25lbnRWYWx1ZShpLCBqLCBldi50YXJnZXQudmFsdWUpfVxuICAgICAgICAgICAgICAgICAgcGxhY2Vob2xkZXI9e2NvbXBvbmVudC50eXBlfSB2YWx1ZT17Y29tcG9uZW50LnZhbHVlfSAvPlxuICAgICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICAgICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlQ29tcG9uZW50KGksIGopfSA+XG4gICAgICAgICAgICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgICAgICAgIDwvc3Bhbj5cbiAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuICAgICAgICAgIDxidXR0b24gb25DbGljaz17KCkgPT4gdGhpcy5vbkFkZENvbXBvbmVudChpKX1cbiAgICAgICAgICAgICBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHQgYnRuLXhzIHB1bGwtcmlnaHRcIiB0eXBlPVwiYnV0dG9uXCI+XG4gICAgICAgICAgICBBZGQgY29tcG9uZW50XG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgPGRpdiBzdHlsZT17e3dpZHRoOiBcIjEwMCVcIiwgaGVpZ2h0OiBcIjZweFwiLCBjbGVhcjogXCJyaWdodFwifX0gLz5cbiAgICAgIDwvZGl2PlxuICAgICkpXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cbiAgICAgICAge25hbWVFbGVtZW50c31cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9PlxuICAgICAgICAgIEFkZCBuYW1lXG4gICAgICAgIDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcbiAgb3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHsgdXJscyB9IGZyb20gXCIuLi8uLi8uLi8uLi91cmxzXCI7XG5cbmNsYXNzIFJlbGF0aW9uRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3Rvcihwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBxdWVyeTogXCJcIixcbiAgICAgIHN1Z2dlc3Rpb25zOiBbXSxcbiAgICAgIGJsdXJJc0Jsb2NrZWQ6IGZhbHNlXG4gICAgfVxuICB9XG5cbiAgb25SZW1vdmUodmFsdWUpIHtcbiAgICBjb25zdCBjdXJyZW50VmFsdWVzID0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcblxuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoXG4gICAgICBbXCJAcmVsYXRpb25zXCIsIHRoaXMucHJvcHMubmFtZV0sXG4gICAgICBjdXJyZW50VmFsdWVzLmZpbHRlcigoY3VyVmFsKSA9PiBjdXJWYWwuaWQgIT09IHZhbHVlLmlkKVxuICAgICk7XG5cbiAgfVxuXG4gIG9uQWRkKHN1Z2dlc3Rpb24pIHtcbiAgICBjb25zdCBjdXJyZW50VmFsdWVzID0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcbiAgICBpZiAoY3VycmVudFZhbHVlcy5tYXAoKHZhbCkgPT4gdmFsLmlkKS5pbmRleE9mKHN1Z2dlc3Rpb24ua2V5KSA+IC0xKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXSwgcXVlcnk6IFwiXCIsIGJsdXJJc0Jsb2NrZWQ6IGZhbHNlfSk7XG5cbiAgICB0aGlzLnByb3BzLm9uQ2hhbmdlKFxuICAgICAgW1wiQHJlbGF0aW9uc1wiLCB0aGlzLnByb3BzLm5hbWVdLFxuICAgICAgY3VycmVudFZhbHVlcy5jb25jYXQoe1xuICAgICAgICBpZDogc3VnZ2VzdGlvbi5rZXksXG4gICAgICAgIGRpc3BsYXlOYW1lOiBzdWdnZXN0aW9uLnZhbHVlLFxuICAgICAgICBhY2NlcHRlZDogdHJ1ZVxuICAgICAgfSlcbiAgICApO1xuICB9XG5cbiAgb25RdWVyeUNoYW5nZShldikge1xuICAgIGNvbnN0IHsgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzLCBwYXRoIH0gPSB0aGlzLnByb3BzO1xuICAgIHRoaXMuc2V0U3RhdGUoe3F1ZXJ5OiBldi50YXJnZXQudmFsdWV9KTtcbiAgICBpZiAoZXYudGFyZ2V0LnZhbHVlID09PSBcIlwiKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogW119KTtcbiAgICB9IGVsc2Uge1xuICAgICAgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzKHBhdGgsIGV2LnRhcmdldC52YWx1ZSwgKHJlc3VsdHMpID0+IHtcbiAgICAgICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IHJlc3VsdHN9KTtcbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIG9uUXVlcnlDbGVhcihldikge1xuICAgIGlmICghdGhpcy5zdGF0ZS5ibHVySXNCbG9ja2VkKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogW10sIHF1ZXJ5OiBcIlwifSk7XG4gICAgfVxuICB9XG5cbiAgb25CbHVyQmxvY2sodG9nZ2xlKSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7Ymx1cklzQmxvY2tlZDogdG9nZ2xlfSk7XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCB0YXJnZXRDb2xsZWN0aW9uIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHZhbHVlcyA9IGVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuICAgIGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5maWx0ZXIoKHZhbCkgPT4gdmFsLmFjY2VwdGVkKS5tYXAoKHZhbHVlLCBpKSA9PiAoXG4gICAgICA8ZGl2IGtleT17YCR7aX0tJHt2YWx1ZS5pZH1gfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cbiAgICAgICAgPExpbmsgdG89e3VybHMuZW50aXR5KHRhcmdldENvbGxlY3Rpb24sIHZhbHVlLmlkKX0gPnt2YWx1ZS5kaXNwbGF5TmFtZX08L0xpbms+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG4gICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICA8L2Rpdj5cbiAgICApKTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICA8aDQ+e2NhbWVsMmxhYmVsKG5hbWUpfTwvaDQ+XG4gICAgICAgIHtpdGVtRWxlbWVudHN9XG4gICAgICAgIDxpbnB1dCBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIlxuICAgICAgICAgICAgICAgb25CbHVyPXt0aGlzLm9uUXVlcnlDbGVhci5iaW5kKHRoaXMpfVxuICAgICAgICAgICAgICAgb25DaGFuZ2U9e3RoaXMub25RdWVyeUNoYW5nZS5iaW5kKHRoaXMpfVxuICAgICAgICAgICAgICAgdmFsdWU9e3RoaXMuc3RhdGUucXVlcnl9IHBsYWNlaG9sZGVyPVwiU2VhcmNoLi4uXCIgLz5cblxuICAgICAgICA8ZGl2IG9uTW91c2VPdmVyPXsoKSA9PiB0aGlzLm9uQmx1ckJsb2NrKHRydWUpfVxuICAgICAgICAgICAgIG9uTW91c2VPdXQ9eygpID0+IHRoaXMub25CbHVyQmxvY2soZmFsc2UpfVxuICAgICAgICAgICAgIHN0eWxlPXt7b3ZlcmZsb3dZOiBcImF1dG9cIiwgbWF4SGVpZ2h0OiBcIjMwMHB4XCJ9fT5cbiAgICAgICAgICB7dGhpcy5zdGF0ZS5zdWdnZXN0aW9ucy5tYXAoKHN1Z2dlc3Rpb24sIGkpID0+IChcbiAgICAgICAgICAgIDxhIGtleT17YCR7aX0tJHtzdWdnZXN0aW9uLmtleX1gfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIlxuICAgICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uQWRkKHN1Z2dlc3Rpb24pfT5cbiAgICAgICAgICAgICAge3N1Z2dlc3Rpb24udmFsdWV9XG4gICAgICAgICAgICA8L2E+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBSZWxhdGlvbkZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uLy4uLy4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnQgPSBlbnRpdHkuZGF0YVtuYW1lXSAmJiBlbnRpdHkuZGF0YVtuYW1lXS5sZW5ndGggPiAwID8gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57ZW50aXR5LmRhdGFbbmFtZV19PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gb25DaGFuZ2UoW25hbWVdLCBcIlwiKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkgOiBudWxsO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50fVxuXHRcdFx0XHQ8U2VsZWN0RmllbGRcblx0XHRcdFx0XHRvbkNoYW5nZT17KHZhbHVlKSA9PiBvbkNoYW5nZShbbmFtZV0sIHZhbHVlKX1cblx0XHRcdFx0XHRub0NsZWFyPXt0cnVlfSBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG5cdFx0XHRcdFx0PHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG5cdFx0XHRcdFx0XHRTZWxlY3Qge2xhYmVsLnRvTG93ZXJDYXNlKCl9XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHRcdHtvcHRpb25zLm1hcCgob3B0aW9uKSA9PiAoXG5cdFx0XHRcdFx0XHQ8c3BhbiBrZXk9e29wdGlvbn0gdmFsdWU9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG5cdFx0XHRcdFx0KSl9XG5cdFx0XHRcdDwvU2VsZWN0RmllbGQ+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheVxufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgU3RyaW5nRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHQ8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcblx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiBvbkNoYW5nZShbbmFtZV0sIGV2LnRhcmdldC52YWx1ZSl9XG5cdFx0XHRcdFx0dmFsdWU9e2VudGl0eS5kYXRhW25hbWVdIHx8IFwiXCJ9XG5cdFx0XHRcdFx0cGxhY2Vob2xkZXI9e2BFbnRlciAke2xhYmVsLnRvTG93ZXJDYXNlKCl9YH1cblx0XHRcdFx0Lz5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuU3RyaW5nRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgU3RyaW5nRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCJcblxuaW1wb3J0IFN0cmluZ0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9zdHJpbmctZmllbGRcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvc2VsZWN0XCI7XG5pbXBvcnQgTXVsdGlTZWxlY3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvbXVsdGktc2VsZWN0XCI7XG5pbXBvcnQgUmVsYXRpb25GaWVsZCBmcm9tIFwiLi9maWVsZHMvcmVsYXRpb25cIjtcbmltcG9ydCBTdHJpbmdMaXN0RmllbGQgZnJvbSBcIi4vZmllbGRzL2xpc3Qtb2Ytc3RyaW5nc1wiO1xuaW1wb3J0IExpbmtGaWVsZCBmcm9tIFwiLi9maWVsZHMvbGlua3NcIjtcbmltcG9ydCBOYW1lc0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9uYW1lc1wiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vdXJsc1wiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2ZpZWxkcy9jYW1lbDJsYWJlbFwiO1xuXG5jb25zdCBmaWVsZE1hcCA9IHtcblx0XCJzdHJpbmdcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJ0ZXh0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwiZGF0YWJsZVwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcIm11bHRpc2VsZWN0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8TXVsdGlTZWxlY3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KSxcblx0XCJzZWxlY3RcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTZWxlY3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KSxcblx0XCJyZWxhdGlvblwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFJlbGF0aW9uRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSB0YXJnZXRDb2xsZWN0aW9uPXtmaWVsZERlZi5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9ufSBwYXRoPXtmaWVsZERlZi5xdWlja3NlYXJjaH0gLz4pLFxuICBcImxpc3Qtb2Ytc3RyaW5nc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0xpc3RGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcbiAgXCJsaW5rc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPExpbmtGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJuYW1lc1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPE5hbWVzRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPilcbn07XG5cbmNvbnN0IGFwcGx5RmlsdGVyID0gKGZpZWxkLCBmaWx0ZXIpID0+XG4gICAgZmllbGQudG9Mb3dlckNhc2UoKS5pbmRleE9mKGZpbHRlci50b0xvd2VyQ2FzZSgpKSA+IC0xIHx8XG4gICAgY2FtZWwybGFiZWwoZmllbGQpLnRvTG93ZXJDYXNlKCkuaW5kZXhPZihmaWx0ZXIudG9Mb3dlckNhc2UoKSkgPiAtMTtcblxuY2xhc3MgRW50aXR5Rm9ybSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgZmllbGRzVG9BZGQ6IFtdLFxuICAgICAgYWRkRmllbGRGaWx0ZXI6IFwiXCJcbiAgICB9XG4gIH1cblxuICBvbkZpbHRlckNoYW5nZShldikge1xuICAgIHRoaXMuc2V0U3RhdGUoe2FkZEZpZWxkRmlsdGVyOiBldi50YXJnZXQudmFsdWV9LCAoKSA9PiB7XG4gICAgICBjb25zdCBmaWx0ZXJlZCA9IHRoaXMuZ2V0QWRkYWJsZUZpZWxkc0Zyb21Qcm9wZXJ0aWVzKCkuZmlsdGVyKHByb3AgPT4gYXBwbHlGaWx0ZXIocHJvcC5uYW1lLCB0aGlzLnN0YXRlLmFkZEZpZWxkRmlsdGVyKSk7XG4gICAgICBpZiAoZmlsdGVyZWQubGVuZ3RoID4gMCkge1xuICAgICAgICBpZiAodGhpcy5zdGF0ZS5hZGRGaWVsZEZpbHRlciA9PT0gXCJcIikge1xuICAgICAgICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiBbXX0pXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IFtmaWx0ZXJlZFswXS5uYW1lXX0pXG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9KTtcbiAgfVxuXG4gIG9uRmlsdGVyS2V5RG93bihldikge1xuICAgIGlmIChldi5rZXkgPT09IFwiRW50ZXJcIiAmJiB0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLmxlbmd0aCA+IDApIHtcbiAgICAgIHRoaXMub25BZGRTZWxlY3RlZEZpZWxkcygpO1xuICAgIH1cbiAgfVxuXG4gIHRvZ2dsZUZpZWxkVG9BZGQoZmllbGROYW1lKSB7XG4gICAgaWYgKHRoaXMuc3RhdGUuZmllbGRzVG9BZGQuaW5kZXhPZihmaWVsZE5hbWUpID4gLTEpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiB0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLmZpbHRlcigoZkFkZCkgPT4gZkFkZCAhPT0gZmllbGROYW1lKX0pO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtmaWVsZHNUb0FkZDogdGhpcy5zdGF0ZS5maWVsZHNUb0FkZC5jb25jYXQoZmllbGROYW1lKX0pO1xuICAgIH1cbiAgfVxuXG4gIG9uQWRkU2VsZWN0ZWRGaWVsZHMoKSB7XG4gICAgY29uc3QgeyBwcm9wZXJ0aWVzIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgdGhpcy5wcm9wcy5vbkFkZFNlbGVjdGVkRmllbGRzKHRoaXMuc3RhdGUuZmllbGRzVG9BZGQubWFwKChmQWRkKSA9PiAoe1xuICAgICAgbmFtZTogZkFkZCxcbiAgICAgIHR5cGU6IHByb3BlcnRpZXMuZmluZCgocHJvcCkgPT4gcHJvcC5uYW1lID09PSBmQWRkKS50eXBlXG4gICAgfSkpKTtcblxuICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiBbXSwgYWRkRmllbGRGaWx0ZXI6IFwiXCJ9KTtcbiAgfVxuXG4gIGdldEFkZGFibGVGaWVsZHNGcm9tUHJvcGVydGllcygpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgcHJvcGVydGllcyB9ID0gdGhpcy5wcm9wcztcblxuICAgIHJldHVybiBwcm9wZXJ0aWVzXG4gICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gZmllbGRNYXAuaGFzT3duUHJvcGVydHkoZmllbGREZWYudHlwZSkpXG4gICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gIWVudGl0eS5kYXRhLmhhc093blByb3BlcnR5KGZpZWxkRGVmLm5hbWUpICYmICFlbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl0uaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkpXG5cbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uRGVsZXRlLCBvbkNoYW5nZSwgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgZW50aXR5LCBjdXJyZW50TW9kZSwgcHJvcGVydGllcywgZW50aXR5TGFiZWwgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBmaWVsZHNUb0FkZCwgYWRkRmllbGRGaWx0ZXIgfSA9IHRoaXMuc3RhdGU7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtOFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgIDxMaW5rIHRvPXt1cmxzLm5ld0VudGl0eShlbnRpdHkuZG9tYWluKX0gY2xhc3NOYW1lPVwiYnRuIGJ0bi1wcmltYXJ5IHB1bGwtcmlnaHRcIj5cbiAgICAgICAgICAgIE5ldyB7ZW50aXR5TGFiZWx9XG4gICAgICAgICAgPC9MaW5rPlxuICAgICAgICA8L2Rpdj5cblxuXG4gICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+ICFmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT4gKDxkaXYga2V5PXtpfSBzdHlsZT17e1wiY29sb3JcIjogXCJyZWRcIn19PjxzdHJvbmc+RmllbGQgdHlwZSBub3Qgc3VwcG9ydGVkOiB7ZmllbGREZWYudHlwZX08L3N0cm9uZz48L2Rpdj4pKX1cblxuICAgICAgICB7cHJvcGVydGllc1xuICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gZW50aXR5LmRhdGEuaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkgfHwgZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdLmhhc093blByb3BlcnR5KGZpZWxkRGVmLm5hbWUpKVxuICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PlxuICAgICAgICAgIGZpZWxkTWFwW2ZpZWxkRGVmLnR5cGVdKGZpZWxkRGVmLCB7XG5cdFx0XHRcdFx0XHRrZXk6IGAke2l9LSR7ZmllbGREZWYubmFtZX1gLFxuXHRcdFx0XHRcdFx0ZW50aXR5OiBlbnRpdHksXG5cdFx0XHRcdFx0XHRvbkNoYW5nZTogb25DaGFuZ2UsXG5cdFx0XHRcdFx0XHRnZXRBdXRvY29tcGxldGVWYWx1ZXM6IGdldEF1dG9jb21wbGV0ZVZhbHVlc1xuXHRcdFx0XHRcdH0pXG4gICAgICAgICl9XG5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW4gYWRkLWZpZWxkLWZvcm1cIj5cbiAgICAgICAgICA8aDQ+QWRkIGZpZWxkczwvaDQ+XG4gICAgICAgICAgPGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiIHZhbHVlPXthZGRGaWVsZEZpbHRlcn0gcGxhY2Vob2xkZXI9XCJGaWx0ZXIuLi5cIlxuICAgICAgICAgICAgICAgICBvbkNoYW5nZT17dGhpcy5vbkZpbHRlckNoYW5nZS5iaW5kKHRoaXMpfVxuICAgICAgICAgICAgICAgICBvbktleVByZXNzPXt0aGlzLm9uRmlsdGVyS2V5RG93bi5iaW5kKHRoaXMpfVxuICAgICAgICAgIC8+XG4gICAgICAgICAgPGRpdiBzdHlsZT17e21heEhlaWdodDogXCIyNTBweFwiLCBvdmVyZmxvd1k6IFwiYXV0b1wifX0+XG4gICAgICAgICAgICB7dGhpcy5nZXRBZGRhYmxlRmllbGRzRnJvbVByb3BlcnRpZXMoKVxuICAgICAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gYXBwbHlGaWx0ZXIoZmllbGREZWYubmFtZSwgYWRkRmllbGRGaWx0ZXIpKVxuICAgICAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT4gKFxuICAgICAgICAgICAgICAgIDxkaXYga2V5PXtpfSBvbkNsaWNrPXsoKSA9PiB0aGlzLnRvZ2dsZUZpZWxkVG9BZGQoZmllbGREZWYubmFtZSl9XG4gICAgICAgICAgICAgICAgICAgICBjbGFzc05hbWU9e2ZpZWxkc1RvQWRkLmluZGV4T2YoZmllbGREZWYubmFtZSkgPiAtMSA/IFwic2VsZWN0ZWRcIiA6IFwiXCJ9PlxuICAgICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwicHVsbC1yaWdodFwiPih7ZmllbGREZWYudHlwZX0pPC9zcGFuPlxuICAgICAgICAgICAgICAgICAge2NhbWVsMmxhYmVsKGZpZWxkRGVmLm5hbWUpfVxuICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICApKVxuICAgICAgICAgICAgfVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17dGhpcy5vbkFkZFNlbGVjdGVkRmllbGRzLmJpbmQodGhpcyl9PkFkZCBzZWxlY3RlZCBmaWVsZHM8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIHtjdXJyZW50TW9kZSA9PT0gXCJlZGl0XCJcbiAgICAgICAgICA/ICg8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgICAgICA8aDQ+RGVsZXRlPC9oND5cbiAgICAgICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRhbmdlclwiIG9uQ2xpY2s9e29uRGVsZXRlfSBkaXNhYmxlZD17IXRoaXMucHJvcHMudXNlcn0+XG4gICAgICAgICAgICAgICAgRGVsZXRlIHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICApIDogbnVsbH1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBFbnRpdHlGb3JtO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IG9uU2F2ZSwgb25DYW5jZWwsIHVzZXIgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdj5cbiAgICAgIDxidXR0b24gZGlzYWJsZWQ9eyF1c2VyfSBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnlcIiBvbkNsaWNrPXtvblNhdmV9PlNhdmU8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn1vcntcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1saW5rXCIgb25DbGljaz17b25DYW5jZWx9PkNhbmNlbDwvYnV0dG9uPlxuICAgIDwvZGl2PlxuICApO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vdXJsc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IHN0YXJ0LCBsaXN0LCBkb21haW4sIHNlbGVjdGVkSWQsIGVudGl0eVBlbmRpbmcgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJyZXN1bHQtbGlzdCByZXN1bHQtbGlzdC1lZGl0XCI+XG4gICAgICA8b2wgc3RhcnQ9e3N0YXJ0ICsgMX0gc3R5bGU9e3tjb3VudGVyUmVzZXQ6IGBzdGVwLWNvdW50ZXIgJHtzdGFydH1gfX0+XG4gICAgICAgIHtsaXN0Lm1hcCgoZW50cnksIGkpID0+IChcbiAgICAgICAgICA8bGkga2V5PXtgJHtpfS0ke2VudHJ5Ll9pZH1gfT5cbiAgICAgICAgICAgIHtlbnRpdHlQZW5kaW5nXG4gICAgICAgICAgICAgID8gKFxuICAgICAgICAgICAgICAgIDxhIHN0eWxlPXt7XG4gICAgICAgICAgICAgICAgICBkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCB3aWR0aDogXCJjYWxjKDEwMCUgLSAzMHB4KVwiLCBoZWlnaHQ6IFwiMTAwJVwiLCBwYWRkaW5nOiBcIjAuNWVtIDBcIixcbiAgICAgICAgICAgICAgICAgIGN1cnNvcjogXCJkZWZhdWx0XCIsIG9wYWNpdHk6IFwiMC41XCIsIHRleHREZWNvcmF0aW9uOiBcIm5vbmVcIiwgZm9udFdlaWdodDogXCIzMDBcIlxuICAgICAgICAgICAgICAgIH19PlxuICAgICAgICAgICAgICAgICAge2VudHJ5W1wiQGRpc3BsYXlOYW1lXCJdfVxuICAgICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgICAgKSA6IChcbiAgICAgICAgICAgICAgICA8TGluayB0bz17dXJscy5lbnRpdHkoZG9tYWluLCBlbnRyeS5faWQpfSBzdHlsZT17e1xuICAgICAgICAgICAgICAgICAgZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgd2lkdGg6IFwiY2FsYygxMDAlIC0gMzBweClcIiwgaGVpZ2h0OiBcIjEwMCVcIiwgcGFkZGluZzogXCIwLjVlbSAwXCIsXG4gICAgICAgICAgICAgICAgICBmb250V2VpZ2h0OiBzZWxlY3RlZElkID09PSBlbnRyeS5faWQgPyBcIjUwMFwiIDogXCIzMDBcIlxuICAgICAgICAgICAgICAgIH19PlxuXG4gICAgICAgICAgICAgICAgICB7ZW50cnlbXCJAZGlzcGxheU5hbWVcIl19XG4gICAgICAgICAgICAgICAgPC9MaW5rPlxuICAgICAgICAgICAgICApXG4gICAgICAgICAgICB9XG4gICAgICAgICAgPC9saT5cbiAgICAgICAgKSl9XG4gICAgICA8L29sPlxuICAgIDwvZGl2PlxuICApXG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25QYWdpbmF0ZUxlZnQsIG9uUGFnaW5hdGVSaWdodCB9ID0gcHJvcHM7XG4gIGNvbnN0IHsgc3RhcnQsIHJvd3MsIGxpc3RMZW5ndGggfSA9IHByb3BzO1xuXG5cblxuICByZXR1cm4gKFxuICAgIDxkaXY+XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIGRpc2FibGVkPXtzdGFydCA9PT0gMH0gb25DbGljaz17b25QYWdpbmF0ZUxlZnR9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWNoZXZyb24tbGVmdFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn17c3RhcnQgKyAxfSAtIHtzdGFydCArIHJvd3N9e1wiIFwifVxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17bGlzdExlbmd0aCA8IHJvd3N9IG9uQ2xpY2s9e29uUGFnaW5hdGVSaWdodH0+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tY2hldnJvbi1yaWdodFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UsIG9uUXVpY2tTZWFyY2gsIHF1ZXJ5IH0gPSBwcm9wcztcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAgc21hbGwtbWFyZ2luIFwiPlxuICAgICAgPGlucHV0IHR5cGU9XCJ0ZXh0XCIgcGxhY2Vob2xkZXI9XCJTZWFyY2ggZm9yLi4uXCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlKGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgIG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyBvblF1aWNrU2VhcmNoKCkgOiBmYWxzZX1cbiAgICAgICAgdmFsdWU9e3F1ZXJ5fVxuICAgICAgICAvPlxuICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17b25RdWlja1NlYXJjaH0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1zZWFyY2hcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rXCIgb25DbGljaz17KCkgPT4geyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UoXCJcIik7IG9uUXVpY2tTZWFyY2goKTsgfX0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgIDwvc3Bhbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuaW1wb3J0IE1lc3NhZ2UgZnJvbSBcIi4uLy4uL21lc3NhZ2VcIjtcblxuY29uc3QgTEFCRUxTID0ge1xuXHRcIlNVQ0NFU1NfTUVTU0FHRVwiOiBcIlwiLFxuXHRcIkVSUk9SX01FU1NBR0VcIjogKFxuXHRcdDxzcGFuPlxuXHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgLz4gV2FybmluZyFcblx0XHQ8L3NwYW4+XG5cdClcbn07XG5cbmNvbnN0IEFMRVJUX0xFVkVMUyA9IHtcblx0XCJTVUNDRVNTX01FU1NBR0VcIjogXCJpbmZvXCIsXG5cdFwiRVJST1JfTUVTU0FHRVwiOiBcImRhbmdlclwiXG59O1xuXG5jbGFzcyBNZXNzYWdlcyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG1lc3NhZ2VzLCB0eXBlcywgb25EaXNtaXNzTWVzc2FnZSB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IGZpbHRlcmVkTWVzc2FnZXMgPSBtZXNzYWdlcy5sb2dcblx0XHRcdC5tYXAoKG1zZywgaWR4KSA9PiAoe21lc3NhZ2U6IG1zZy5tZXNzYWdlLCBpbmRleDogaWR4LCB0eXBlOiBtc2cudHlwZSwgZGlzbWlzc2VkOiBtc2cuZGlzbWlzc2VkIH0pKVxuXHRcdFx0LmZpbHRlcigobXNnKSA9PiB0eXBlcy5pbmRleE9mKG1zZy50eXBlKSA+IC0xICYmICFtc2cuZGlzbWlzc2VkKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2PlxuXHRcdFx0XHR7ZmlsdGVyZWRNZXNzYWdlcy5tYXAoKG1zZykgPT4gKFxuXHRcdFx0XHRcdDxNZXNzYWdlIGtleT17bXNnLmluZGV4fVxuXHRcdFx0XHRcdFx0ZGlzbWlzc2libGU9e3RydWV9XG5cdFx0XHRcdFx0XHRhbGVydExldmVsPXtBTEVSVF9MRVZFTFNbbXNnLnR5cGVdfVxuXHRcdFx0XHRcdFx0b25DbG9zZU1lc3NhZ2U9eygpID0+IG9uRGlzbWlzc01lc3NhZ2UobXNnLmluZGV4KX0+XG5cdFx0XHRcdFx0XHQ8c3Ryb25nPntMQUJFTFNbbXNnLnR5cGVdfTwvc3Ryb25nPiA8c3Bhbj57bXNnLm1lc3NhZ2V9PC9zcGFuPlxuXHRcdFx0XHRcdDwvTWVzc2FnZT5cblx0XHRcdFx0KSl9XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbk1lc3NhZ2VzLnByb3BUeXBlcyA9IHtcblx0bWVzc2FnZXM6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG9uRGlzbWlzc01lc3NhZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWQsXG5cdHR5cGVzOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXkuaXNSZXF1aXJlZFxufTtcblxuZXhwb3J0IGRlZmF1bHQgTWVzc2FnZXM7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgaXNPcGVuOiBmYWxzZVxuICAgIH07XG4gICAgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50KCkge1xuICAgIGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG4gIH1cblxuICB0b2dnbGVTZWxlY3QoKSB7XG4gICAgaWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG4gICAgfVxuICB9XG5cbiAgaGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuICAgIGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuICAgIGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe1xuICAgICAgICBpc09wZW46IGZhbHNlXG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbkNoYW5nZSwgb25DbGVhciwgdmFsdWUsIGJ0bkNsYXNzLCBub0NsZWFyIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qgc2VsZWN0ZWRPcHRpb24gPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgPT09IHZhbHVlKTtcbiAgICBjb25zdCBwbGFjZWhvbGRlciA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy50eXBlID09PSBcInBsYWNlaG9sZGVyXCIpO1xuICAgIGNvbnN0IG90aGVyT3B0aW9ucyA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSAmJiBvcHQucHJvcHMudmFsdWUgIT09IHZhbHVlKTtcblxuICAgIHJldHVybiAoXG5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImRyb3Bkb3duXCIsIHtvcGVuOiB0aGlzLnN0YXRlLmlzT3Blbn0pfT5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9e2N4KFwiYnRuXCIsIFwiZHJvcGRvd24tdG9nZ2xlXCIsIGJ0bkNsYXNzIHx8IFwiYnRuLWJsYW5rXCIpfSBvbkNsaWNrPXt0aGlzLnRvZ2dsZVNlbGVjdC5iaW5kKHRoaXMpfT5cbiAgICAgICAgICB7c2VsZWN0ZWRPcHRpb24ubGVuZ3RoID8gc2VsZWN0ZWRPcHRpb24gOiBwbGFjZWhvbGRlcn0gPHNwYW4gY2xhc3NOYW1lPVwiY2FyZXRcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cblxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwiZHJvcGRvd24tbWVudVwiPlxuICAgICAgICAgIHsgdmFsdWUgJiYgIW5vQ2xlYXIgPyAoXG4gICAgICAgICAgICA8bGk+XG4gICAgICAgICAgICAgIDxhIG9uQ2xpY2s9eygpID0+IHsgb25DbGVhcigpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpO319PlxuICAgICAgICAgICAgICAgIC0gY2xlYXIgLVxuICAgICAgICAgICAgICA8L2E+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgICAgIHtvdGhlck9wdGlvbnMubWFwKChvcHRpb24sIGkpID0+IChcbiAgICAgICAgICAgIDxsaSBrZXk9e2l9PlxuICAgICAgICAgICAgICA8YSBzdHlsZT17e2N1cnNvcjogXCJwb2ludGVyXCJ9fSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2hhbmdlKG9wdGlvbi5wcm9wcy52YWx1ZSk7IHRoaXMudG9nZ2xlU2VsZWN0KCk7IH19PntvcHRpb259PC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC91bD5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuU2VsZWN0RmllbGQucHJvcFR5cGVzID0ge1xuICBvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIG9uQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICB2YWx1ZTogUmVhY3QuUHJvcFR5cGVzLmFueSxcbiAgYnRuQ2xhc3M6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG5vQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5ib29sXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZnVuY3Rpb24gRm9vdGVyKHByb3BzKSB7XG4gIGNvbnN0IGhpTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImhpLWxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1odXlnZW5zLWluZy5zdmdcIiAvPlxuICAgIDwvZGl2PlxuICApO1xuXG4gIGNvbnN0IGNsYXJpYWhMb2dvID0gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgY29sLW1kLTFcIj5cbiAgICAgIDxpbWcgY2xhc3NOYW1lPVwibG9nb1wiIHNyYz1cImltYWdlcy9sb2dvLWNsYXJpYWguc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBmb290ZXJCb2R5ID0gUmVhY3QuQ2hpbGRyZW4uY291bnQocHJvcHMuY2hpbGRyZW4pID4gMCA/XG4gICAgUmVhY3QuQ2hpbGRyZW4ubWFwKHByb3BzLmNoaWxkcmVuLCAoY2hpbGQsIGkpID0+IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwid2hpdGUtYmFyXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAge2kgPT09IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSAtIDFcbiAgICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2hpTG9nb308ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMCBjb2wtbWQtMTAgdGV4dC1jZW50ZXJcIj57Y2hpbGR9PC9kaXY+e2NsYXJpYWhMb2dvfTwvZGl2PilcbiAgICAgICAgICAgIDogKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2NoaWxkfTwvZGl2PilcbiAgICAgICAgICB9XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKSkgOiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG4gICAgICAgICAgICB7aGlMb2dvfVxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIHtjbGFyaWFoTG9nb31cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuXG5cbiAgcmV0dXJuIChcbiAgICA8Zm9vdGVyIGNsYXNzTmFtZT1cImZvb3RlclwiPlxuICAgICAge2Zvb3RlckJvZHl9XG4gICAgPC9mb290ZXI+XG4gIClcbn1cblxuZXhwb3J0IGRlZmF1bHQgRm9vdGVyOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IGRpc21pc3NpYmxlLCBhbGVydExldmVsLCBvbkNsb3NlTWVzc2FnZX0gPSBwcm9wcztcbiAgY29uc3QgZGlzbWlzc0J1dHRvbiA9IGRpc21pc3NpYmxlXG4gICAgPyA8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzc05hbWU9XCJjbG9zZVwiIG9uQ2xpY2s9e29uQ2xvc2VNZXNzYWdlfT48c3Bhbj4mdGltZXM7PC9zcGFuPjwvYnV0dG9uPlxuICAgIDogbnVsbDtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImFsZXJ0XCIsIGBhbGVydC0ke2FsZXJ0TGV2ZWx9YCwge1wiYWxlcnQtZGlzbWlzc2libGVcIjogZGlzbWlzc2libGV9KX0gcm9sZT1cImFsZXJ0XCI+XG4gICAgICB7ZGlzbWlzc0J1dHRvbn1cbiAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICA8L2Rpdj5cbiAgKVxufTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IEZvb3RlciBmcm9tIFwiLi9mb290ZXJcIjtcblxuY29uc3QgRk9PVEVSX0hFSUdIVCA9IDgxO1xuXG5mdW5jdGlvbiBQYWdlKHByb3BzKSB7XG4gIGNvbnN0IGZvb3RlcnMgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKGNoaWxkKSA9PiBjaGlsZC5wcm9wcy50eXBlID09PSBcImZvb3Rlci1ib2R5XCIpO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJwYWdlXCI+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpbiBoaS1HcmVlbiBjb250YWluZXItZmx1aWRcIj5cbiAgICAgICAgPG5hdiBjbGFzc05hbWU9XCJuYXZiYXIgXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwibmF2YmFyLWhlYWRlclwiPiA8YSBjbGFzc05hbWU9XCJuYXZiYXItYnJhbmRcIiBocmVmPVwiI1wiPjxpbWcgc3JjPVwiaW1hZ2VzL2xvZ28tdGltYnVjdG9vLnN2Z1wiIGNsYXNzTmFtZT1cImxvZ29cIiBhbHQ9XCJ0aW1idWN0b29cIi8+PC9hPiA8L2Rpdj5cbiAgICAgICAgICAgIDxkaXYgaWQ9XCJuYXZiYXJcIiBjbGFzc05hbWU9XCJuYXZiYXItY29sbGFwc2UgY29sbGFwc2VcIj5cbiAgICAgICAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXZiYXItbmF2IG5hdmJhci1yaWdodFwiPlxuICAgICAgICAgICAgICAgIHtwcm9wcy51c2VybmFtZSA/IDxsaT48YSBocmVmPXtwcm9wcy51c2VybG9jYXRpb24gfHwgJyMnfT48c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXVzZXJcIi8+IHtwcm9wcy51c2VybmFtZX08L2E+PC9saT4gOiBudWxsfVxuICAgICAgICAgICAgICA8L3VsPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvbmF2PlxuICAgICAgPC9kaXY+XG4gICAgICA8ZGl2ICBzdHlsZT17e21hcmdpbkJvdHRvbTogYCR7Rk9PVEVSX0hFSUdIVCAqIGZvb3RlcnMubGVuZ3RofXB4YH19PlxuICAgICAgICB7UmVhY3QuQ2hpbGRyZW4udG9BcnJheShwcm9wcy5jaGlsZHJlbikuZmlsdGVyKChjaGlsZCkgPT4gY2hpbGQucHJvcHMudHlwZSAhPT0gXCJmb290ZXItYm9keVwiKX1cbiAgICAgIDwvZGl2PlxuICAgICAgPEZvb3Rlcj5cbiAgICAgICAge2Zvb3RlcnN9XG4gICAgICA8L0Zvb3Rlcj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgUGFnZTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBSZWFjdERPTSBmcm9tIFwicmVhY3QtZG9tXCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCB7c2V0VnJlfSBmcm9tIFwiLi9hY3Rpb25zL3ZyZVwiO1xuXG5pbXBvcnQgcm91dGVyIGZyb20gXCIuL3JvdXRlclwiO1xuaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5cbmNvbnN0IHNldFVzZXIgPSAocmVzcG9uc2UpID0+IHtcblx0Ly8gVE9ETzogdmFsaWRhdGUgdXNlciBzZXNzaW9uLlxuXHR4aHIoe1xuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL3N5c3RlbS91c2Vycy9tZS92cmVzYCxcblx0XHRoZWFkZXJzOiB7XG5cdFx0XHQnQXV0aG9yaXphdGlvbic6IHJlc3BvbnNlLnRva2VuXG5cdFx0fVxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0aWYgKGVyciB8fCByZXNwLnN0YXR1c0NvZGUgPj0gMzAwKSB7XG5cdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTRVNTSU9OX0VYUElSRURcIn0pO1xuXHRcdH0gZWxzZSB7XG5cdFx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdFx0aWYgKCFkYXRhLm1pbmUgfHwgT2JqZWN0LmtleXMoZGF0YS5taW5lKS5pbmRleE9mKGdldFZyZUlkKCkpIDwgMCkge1xuXHRcdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJFUlJPUl9NRVNTQUdFXCIsIG1lc3NhZ2U6IFwiWW91IGFyZSBub3QgYWxsb3dlZCB0byBlZGl0IHRoaXMgdnJlXCJ9KTtcblx0XHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VTU0lPTl9FWFBJUkVEXCJ9KTtcblxuXHRcdFx0fVxuXHRcdH1cblx0fSk7XG5cdHJldHVybiB7XG5cdFx0dHlwZTogXCJTRVRfVVNFUlwiLFxuXHRcdHVzZXI6IHJlc3BvbnNlXG5cdH07XG59O1xuXG5mdW5jdGlvbiBnZXRWcmVJZCgpIHtcblx0bGV0IHBhdGggPSB3aW5kb3cubG9jYXRpb24uc2VhcmNoLnN1YnN0cigxKTtcblx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdGZvcihsZXQgaSBpbiBwYXJhbXMpIHtcblx0XHRsZXQgW2tleSwgdmFsdWVdID0gcGFyYW1zW2ldLnNwbGl0KFwiPVwiKTtcblx0XHRpZihrZXkgPT09IFwidnJlSWRcIikge1xuXHRcdFx0cmV0dXJuIHZhbHVlO1xuXHRcdH1cblx0fVxuXHRyZXR1cm4gXCJXb21lbldyaXRlcnNcIjtcbn1cblxuZG9jdW1lbnQuYWRkRXZlbnRMaXN0ZW5lcihcIkRPTUNvbnRlbnRMb2FkZWRcIiwgKCkgPT4ge1xuXG5cdGZ1bmN0aW9uIGluaXRSb3V0ZXIoKSB7XG5cdFx0UmVhY3RET00ucmVuZGVyKHJvdXRlciwgZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoXCJhcHBcIikpO1xuXHR9XG5cblxuXG5cdGZ1bmN0aW9uIGdldExvZ2luKCkge1xuXHRcdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdFx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdFx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdFx0bGV0IFtrZXksIHZhbHVlXSA9IHBhcmFtc1tpXS5zcGxpdChcIj1cIik7XG5cdFx0XHRpZihrZXkgPT09IFwiaHNpZFwiKSB7XG5cdFx0XHRcdGxvY2FsU3RvcmFnZS5zZXRJdGVtKFwidG9rZW5cIiwgSlNPTi5zdHJpbmdpZnkoe3VzZXI6IHZhbHVlLCB0b2tlbjogdmFsdWV9KSk7XG5cdFx0XHRcdGxvY2F0aW9uLmhyZWYgPSB3aW5kb3cubG9jYXRpb24uaHJlZi5yZXBsYWNlKFwiaHNpZD1cIiArIHZhbHVlLCBcIlwiKTtcblx0XHRcdFx0cmV0dXJuO1xuXHRcdFx0fVxuXHRcdH1cblx0XHRyZXR1cm4gSlNPTi5wYXJzZShsb2NhbFN0b3JhZ2UuZ2V0SXRlbShcInRva2VuXCIpIHx8IFwibnVsbFwiKTtcblx0fVxuXG5cdHN0b3JlLmRpc3BhdGNoKHNldFZyZShnZXRWcmVJZCgpLCBpbml0Um91dGVyKSk7XG5cdHN0b3JlLmRpc3BhdGNoKHNldFVzZXIoZ2V0TG9naW4oKSkpO1xufSk7IiwiaW1wb3J0IHNldEluIGZyb20gXCIuLi91dGlsL3NldC1pblwiO1xuXG5sZXQgaW5pdGlhbFN0YXRlID0ge1xuXHRkYXRhOiB7XG5cdFx0XCJAcmVsYXRpb25zXCI6IFtdXG5cdH0sXG5cdGRvbWFpbjogbnVsbCxcblx0ZXJyb3JNZXNzYWdlOiBudWxsLFxuXHRwZW5kaW5nOiBmYWxzZVxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXG5cdFx0Y2FzZSBcIkJFRk9SRV9GRVRDSF9FTlRJVFlcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YToge1xuXHRcdFx0XHRcdFwiQHJlbGF0aW9uc1wiOiBbXVxuXHRcdFx0XHR9LFxuXHRcdFx0XHRwZW5kaW5nOiB0cnVlXG5cdFx0XHR9fTtcblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YTogYWN0aW9uLmRhdGEsXG5cdFx0XHRcdGRvbWFpbjogYWN0aW9uLmRvbWFpbixcblx0XHRcdFx0ZXJyb3JNZXNzYWdlOiBhY3Rpb24uZXJyb3JNZXNzYWdlIHx8IG51bGwsXG5cdFx0XHRcdHBlbmRpbmc6IGZhbHNlXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IHNldEluKGFjdGlvbi5maWVsZFBhdGgsIGFjdGlvbi52YWx1ZSwgc3RhdGUuZGF0YSlcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZX0ZBSUxVUkVcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YToge1xuXHRcdFx0XHRcdFwiQHJlbGF0aW9uc1wiOiBbXVxuXHRcdFx0XHR9LFxuXHRcdFx0XHRlcnJvck1lc3NhZ2U6IGFjdGlvbi5lcnJvck1lc3NhZ2UsXG5cdFx0XHRcdHBlbmRpbmc6IGZhbHNlXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJTRVRfVlJFXCI6IHtcblx0XHRcdHJldHVybiBpbml0aWFsU3RhdGU7XG5cdFx0fVxuXG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwiaW1wb3J0IHtjb21iaW5lUmVkdWNlcnN9IGZyb20gXCJyZWR1eFwiO1xuXG5pbXBvcnQgZW50aXR5IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IG1lc3NhZ2VzIGZyb20gXCIuL21lc3NhZ2VzXCI7XG5pbXBvcnQgdXNlciBmcm9tIFwiLi91c2VyXCI7XG5pbXBvcnQgdnJlIGZyb20gXCIuL3ZyZVwiO1xuaW1wb3J0IHF1aWNrU2VhcmNoIGZyb20gXCIuL3F1aWNrLXNlYXJjaFwiO1xuXG5leHBvcnQgZGVmYXVsdCBjb21iaW5lUmVkdWNlcnMoe1xuXHR2cmU6IHZyZSxcblx0ZW50aXR5OiBlbnRpdHksXG5cdHVzZXI6IHVzZXIsXG5cdG1lc3NhZ2VzOiBtZXNzYWdlcyxcblx0cXVpY2tTZWFyY2g6IHF1aWNrU2VhcmNoXG59KTsiLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5cbmNvbnN0IGluaXRpYWxTdGF0ZSA9IHtcblx0bG9nOiBbXVxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJSRVFVRVNUX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiU1VDQ0VTU19NRVNTQUdFXCI6XG5cdFx0XHRzdGF0ZS5sb2cucHVzaCh7bWVzc2FnZTogYWN0aW9uLm1lc3NhZ2UsIHR5cGU6IGFjdGlvbi50eXBlLCB0aW1lOiBuZXcgRGF0ZSgpfSk7XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0Y2FzZSBcIkVSUk9SX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiRElTTUlTU19NRVNTQUdFXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0bG9nOiBzZXRJbihbYWN0aW9uLm1lc3NhZ2VJbmRleCwgXCJkaXNtaXNzZWRcIl0sIHRydWUsIHN0YXRlLmxvZylcblx0XHRcdH07XG5cdH1cblxuXHRyZXR1cm4gc3RhdGU7XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0c3RhcnQ6IDAsXG5cdGxpc3Q6IFtdLFxuXHRyb3dzOiA1MCxcblx0cXVlcnk6IFwiXCJcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIHN0YXJ0OiBhY3Rpb24uc3RhcnR9O1xuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWV9MSVNUXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5kYXRhXG5cdFx0XHR9fTtcblx0XHRjYXNlIFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCI6IHtcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0cXVlcnk6IGFjdGlvbi52YWx1ZVxuXHRcdFx0fX07XG5cdFx0fVxuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0gbnVsbDtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfVVNFUlwiOlxuXHRcdFx0aWYgKGFjdGlvbi51c2VyKSB7XG5cdFx0XHRcdHJldHVybiBhY3Rpb24udXNlcjtcblx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRcdH1cblx0XHRcdGJyZWFrO1xuXHRcdGNhc2UgXCJTRVNTSU9OX0VYUElSRURcIjpcblx0XHRcdHJldHVybiBudWxsO1xuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0ge1xuXHR2cmVJZDogbnVsbCxcblx0bGlzdDogW10sXG5cdGNvbGxlY3Rpb25zOiB7fSxcblx0ZG9tYWluOiBudWxsXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9WUkVcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHR2cmVJZDogYWN0aW9uLnZyZUlkLFxuXHRcdFx0XHRjb2xsZWN0aW9uczogYWN0aW9uLmNvbGxlY3Rpb25zIHx8IG51bGwsXG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5saXN0IHx8IHN0YXRlLmxpc3Rcblx0XHRcdH07XG5cblx0XHRjYXNlIFwiTElTVF9WUkVTXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0bGlzdDogYWN0aW9uLmxpc3QsXG5cdFx0XHRcdGNvbGxlY3Rpb25zOiBudWxsXG5cdFx0XHR9O1xuXHRcdGNhc2UgXCJTRVRfRE9NQUlOXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0ZG9tYWluOiBhY3Rpb24uZG9tYWluXG5cdFx0XHR9O1xuXG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCB7Um91dGVyLCBSZWRpcmVjdCwgUm91dGUsIGhhc2hIaXN0b3J5fSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQge1Byb3ZpZGVyLCBjb25uZWN0fSBmcm9tIFwicmVhY3QtcmVkdXhcIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IGdldEF1dG9jb21wbGV0ZVZhbHVlcyBmcm9tIFwiLi9hY3Rpb25zL2F1dG9jb21wbGV0ZVwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vYWN0aW9uc1wiO1xuXG5pbXBvcnQgRWRpdEd1aSBmcm9tIFwiLi9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpXCI7XG5pbXBvcnQge3VybHN9IGZyb20gXCIuL3VybHNcIjtcblxuZXhwb3J0IGZ1bmN0aW9uIG5hdmlnYXRlVG8oa2V5LCBhcmdzKSB7XG5cdGhhc2hIaXN0b3J5LnB1c2godXJsc1trZXldLmFwcGx5KG51bGwsIGFyZ3MpKTtcbn1cblxuY29uc3QgZGVmYXVsdENvbm5lY3QgPSBjb25uZWN0KFxuXHRzdGF0ZSA9PiAoey4uLnN0YXRlLCBnZXRBdXRvY29tcGxldGVWYWx1ZXM6IGdldEF1dG9jb21wbGV0ZVZhbHVlc30pLFxuXHRkaXNwYXRjaCA9PiBhY3Rpb25zKG5hdmlnYXRlVG8sIGRpc3BhdGNoKVxuKTtcblxuXG5leHBvcnQgZGVmYXVsdCAoXG5cdDxQcm92aWRlciBzdG9yZT17c3RvcmV9PlxuXHRcdDxSb3V0ZXIgaGlzdG9yeT17aGFzaEhpc3Rvcnl9PlxuXHRcdFx0PFJvdXRlIHBhdGg9e3VybHMucm9vdCgpfSBjb21wb25lbnRzPXtkZWZhdWx0Q29ubmVjdChFZGl0R3VpKX0gLz5cblx0XHRcdDxSb3V0ZSBwYXRoPXt1cmxzLm5ld0VudGl0eSgpfSBjb21wb25lbnRzPXtkZWZhdWx0Q29ubmVjdChFZGl0R3VpKX0gLz5cblx0XHRcdDxSb3V0ZSBwYXRoPXt1cmxzLmVudGl0eSgpfSBjb21wb25lbnRzPXtkZWZhdWx0Q29ubmVjdChFZGl0R3VpKX0gLz5cblx0XHQ8L1JvdXRlcj5cblx0PC9Qcm92aWRlcj5cbik7XG4iLCJpbXBvcnQge2NyZWF0ZVN0b3JlLCBhcHBseU1pZGRsZXdhcmV9IGZyb20gXCJyZWR1eFwiO1xuaW1wb3J0IHRodW5rTWlkZGxld2FyZSBmcm9tIFwicmVkdXgtdGh1bmtcIjtcblxuaW1wb3J0IHJlZHVjZXJzIGZyb20gXCIuLi9yZWR1Y2Vyc1wiO1xuXG5jb25zdCBsb2dnZXIgPSAoKSA9PiBuZXh0ID0+IGFjdGlvbiA9PiB7XG4gIGlmIChhY3Rpb24uaGFzT3duUHJvcGVydHkoXCJ0eXBlXCIpKSB7XG4gICAgY29uc29sZS5sb2coXCJbUkVEVVhdXCIsIGFjdGlvbi50eXBlLCBhY3Rpb24pO1xuICB9XG5cbiAgcmV0dXJuIG5leHQoYWN0aW9uKTtcbn07XG5cbmxldCBjcmVhdGVTdG9yZVdpdGhNaWRkbGV3YXJlID0gYXBwbHlNaWRkbGV3YXJlKC8qbG9nZ2VyLCovIHRodW5rTWlkZGxld2FyZSkoY3JlYXRlU3RvcmUpO1xuZXhwb3J0IGRlZmF1bHQgY3JlYXRlU3RvcmVXaXRoTWlkZGxld2FyZShyZWR1Y2Vycyk7XG4iLCJjb25zdCB1cmxzID0ge1xuXHRyb290KCkge1xuXHRcdHJldHVybiBcIi9cIjtcblx0fSxcblx0bmV3RW50aXR5KGNvbGxlY3Rpb24pIHtcblx0XHRyZXR1cm4gY29sbGVjdGlvblxuXHRcdFx0PyBgLyR7Y29sbGVjdGlvbn0vbmV3YFxuXHRcdFx0OiBcIi86Y29sbGVjdGlvbi9uZXdcIjtcblx0fSxcblx0ZW50aXR5KGNvbGxlY3Rpb24sIGlkKSB7XG5cdFx0cmV0dXJuIGNvbGxlY3Rpb24gJiYgaWRcblx0XHRcdD8gYC8ke2NvbGxlY3Rpb259LyR7aWR9YFxuXHRcdFx0OiBcIi86Y29sbGVjdGlvbi86aWRcIjtcblx0fVxufTtcblxuZXhwb3J0IHsgdXJscyB9IiwiZnVuY3Rpb24gZGVlcENsb25lOShvYmopIHtcbiAgICB2YXIgaSwgbGVuLCByZXQ7XG5cbiAgICBpZiAodHlwZW9mIG9iaiAhPT0gXCJvYmplY3RcIiB8fCBvYmogPT09IG51bGwpIHtcbiAgICAgICAgcmV0dXJuIG9iajtcbiAgICB9XG5cbiAgICBpZiAoQXJyYXkuaXNBcnJheShvYmopKSB7XG4gICAgICAgIHJldCA9IFtdO1xuICAgICAgICBsZW4gPSBvYmoubGVuZ3RoO1xuICAgICAgICBmb3IgKGkgPSAwOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgICAgIHJldC5wdXNoKCAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldICk7XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICByZXQgPSB7fTtcbiAgICAgICAgZm9yIChpIGluIG9iaikge1xuICAgICAgICAgICAgaWYgKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkge1xuICAgICAgICAgICAgICAgIHJldFtpXSA9ICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV07XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHJldDtcbn1cblxuZXhwb3J0IGRlZmF1bHQgZGVlcENsb25lOTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4vY2xvbmUtZGVlcFwiO1xuXG4vLyBEbyBlaXRoZXIgb2YgdGhlc2U6XG4vLyAgYSkgU2V0IGEgdmFsdWUgYnkgcmVmZXJlbmNlIGlmIGRlcmVmIGlzIG5vdCBudWxsXG4vLyAgYikgU2V0IGEgdmFsdWUgZGlyZWN0bHkgaW4gdG8gZGF0YSBvYmplY3QgaWYgZGVyZWYgaXMgbnVsbFxuY29uc3Qgc2V0RWl0aGVyID0gKGRhdGEsIGRlcmVmLCBrZXksIHZhbCkgPT4ge1xuXHQoZGVyZWYgfHwgZGF0YSlba2V5XSA9IHZhbDtcblx0cmV0dXJuIGRhdGE7XG59O1xuXG4vLyBTZXQgYSBuZXN0ZWQgdmFsdWUgaW4gZGF0YSAobm90IHVubGlrZSBpbW11dGFibGVqcywgYnV0IGEgY2xvbmUgb2YgZGF0YSBpcyBleHBlY3RlZCBmb3IgcHJvcGVyIGltbXV0YWJpbGl0eSlcbmNvbnN0IF9zZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPSBudWxsKSA9PlxuXHRwYXRoLmxlbmd0aCA+IDEgP1xuXHRcdF9zZXRJbihwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPyBkZXJlZltwYXRoLnNoaWZ0KCldIDogZGF0YVtwYXRoLnNoaWZ0KCldKSA6XG5cdFx0c2V0RWl0aGVyKGRhdGEsIGRlcmVmLCBwYXRoWzBdLCB2YWx1ZSk7XG5cbmNvbnN0IHNldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhKSA9PlxuXHRfc2V0SW4oY2xvbmUocGF0aCksIHZhbHVlLCBjbG9uZShkYXRhKSk7XG5cbmV4cG9ydCBkZWZhdWx0IHNldEluOyJdfQ==
