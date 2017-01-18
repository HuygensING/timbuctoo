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
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDRWUsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFJLFVBQVU7QUFDYixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEYSxFQUFkOztBQUlBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxPQUFLLEtBQUssS0FBTCxDQUFXLElBQVgsRUFBaUIsR0FBakIsQ0FBcUIsVUFBQyxDQUFELEVBQU87QUFBRSxVQUFPLEVB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TyxJQUFQLEVBQWEsS0FBYixFQUFvQixJQUFwQixFQUEwQixRQUFRLE1BQU0sS0FBSyxLQUFMLEVBQU4sQ0FBUixHQUE4QixLQUFLLEtBQUssS0FBTCxFQUFMLENBQXhELENBREQsR0FFQyxVQUFVLElBQVYsRUFBZ0IsS0FBaEIsRUFBdUIsS0FBSyxDQUFMLENBQXZCLEVBQWdDLEtBQWhDLENBSGE7QUFBQSxDQUFmOztBQUtBLElBQU0sUUFBUSxTQUFSLEtBQVEsQ0FBQyxJQUFELEVBQU8sS0FBUCxFQUFjLElBQWQ7QUFBQSxRQUNiLE9BQU8seUJBQU0sSUFBTixDQUFQLEVBQW9CLEtBQXBCLEVBQTJCLHlCQUFNLElBQU4sQ0FBM0IsQ0FEYTtBQUFBLENBQWQ7O2tCQUdlLEsiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocGF0aCwgcXVlcnksIGRvbmUpIHtcblx0bGV0IG9wdGlvbnMgPSB7XG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvJHtwYXRoLnJlcGxhY2UoL15cXC92W14vXStcXC8vLCBcIlwiKX0/cXVlcnk9JHtxdWVyeX0qYFxuXHR9O1xuXG5cdGxldCB4aHJEb25lID0gZnVuY3Rpb24oZXJyLCByZXNwb25zZSwgYm9keSkge1xuXHRcdGRvbmUoSlNPTi5wYXJzZShib2R5KS5tYXAoKGQpID0+IHsgcmV0dXJuIHtrZXk6IGQua2V5LnJlcGxhY2UoL14uK1xcLy8sIFwiXCIpLCB2YWx1ZTogZC52YWx1ZX07IH0pKTtcblx0fTtcblxuXHRzZXJ2ZXIuZmFzdFhocihvcHRpb25zLCB4aHJEb25lKTtcbn0iLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuXG5jb25zdCBzYXZlTmV3RW50aXR5ID0gKGRvbWFpbiwgc2F2ZURhdGEsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJQT1NUXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0Ym9keTogSlNPTi5zdHJpbmdpZnkoc2F2ZURhdGEpLFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn1gXG5cdH0sIG5leHQsIGZhaWwsIGBDcmVhdGUgbmV3ICR7ZG9tYWlufWApO1xuXG5jb25zdCB1cGRhdGVFbnRpdHkgPSAoZG9tYWluLCBzYXZlRGF0YSwgdG9rZW4sIHZyZUlkLCBuZXh0LCBmYWlsKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIlBVVFwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdGJvZHk6IEpTT04uc3RyaW5naWZ5KHNhdmVEYXRhKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7c2F2ZURhdGEuX2lkfWBcblx0fSwgbmV4dCwgZmFpbCwgYFVwZGF0ZSAke2RvbWFpbn1gKTtcblxuY29uc3QgZGVsZXRlRW50aXR5ID0gKGRvbWFpbiwgZW50aXR5SWQsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJERUxFVEVcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7ZW50aXR5SWR9YFxuXHR9LCBuZXh0LCBmYWlsLCBgRGVsZXRlICR7ZG9tYWlufWApO1xuXG5jb25zdCBmZXRjaEVudGl0eSA9IChsb2NhdGlvbiwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJ9LFxuXHRcdHVybDogbG9jYXRpb25cblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGNvbnN0IGRhdGEgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0bmV4dChkYXRhKTtcblx0fSwgZmFpbCwgXCJGZXRjaCBlbnRpdHlcIik7XG5cbmNvbnN0IGZldGNoRW50aXR5TGlzdCA9IChkb21haW4sIHN0YXJ0LCByb3dzLCBuZXh0KSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIn0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufT9yb3dzPSR7cm93c30mc3RhcnQ9JHtzdGFydH1gXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdG5leHQoZGF0YSk7XG5cdH0pO1xuXG5jb25zdCBjcnVkID0ge1xuXHRzYXZlTmV3RW50aXR5OiBzYXZlTmV3RW50aXR5LFxuXHR1cGRhdGVFbnRpdHk6IHVwZGF0ZUVudGl0eSxcblx0ZGVsZXRlRW50aXR5OiBkZWxldGVFbnRpdHksXG5cdGZldGNoRW50aXR5OiBmZXRjaEVudGl0eSxcblx0ZmV0Y2hFbnRpdHlMaXN0OiBmZXRjaEVudGl0eUxpc3Rcbn07XG5cbmV4cG9ydCB7c2F2ZU5ld0VudGl0eSwgdXBkYXRlRW50aXR5LCBkZWxldGVFbnRpdHksIGZldGNoRW50aXR5LCBmZXRjaEVudGl0eUxpc3QsIGNydWR9OyIsImltcG9ydCBjbG9uZSBmcm9tIFwiLi4vdXRpbC9jbG9uZS1kZWVwXCI7XG5pbXBvcnQgeyBjcnVkIH0gZnJvbSBcIi4vY3J1ZFwiO1xuaW1wb3J0IHNhdmVSZWxhdGlvbnMgZnJvbSBcIi4vc2F2ZS1yZWxhdGlvbnNcIjtcbmltcG9ydCBhdXRvY29tcGxldGUgZnJvbSBcIi4vYXV0b2NvbXBsZXRlXCI7XG5cbi8vIFNrZWxldG9uIGJhc2UgZGF0YSBwZXIgZmllbGQgZGVmaW5pdGlvblxuY29uc3QgaW5pdGlhbERhdGEgPSB7XG5cdG5hbWVzOiBbXSxcblx0bXVsdGlzZWxlY3Q6IFtdLFxuXHRsaW5rczogW10sXG5cdGtleXdvcmQ6IFtdLFxuXHRcImxpc3Qtb2Ytc3RyaW5nc1wiOiBbXSxcblx0YWx0bmFtZXM6IFtdLFxuXHR0ZXh0OiBcIlwiLFxuXHRzdHJpbmc6IFwiXCIsXG5cdHNlbGVjdDogXCJcIixcblx0ZGF0YWJsZTogXCJcIlxufTtcblxuLy8gUmV0dXJuIHRoZSBpbml0aWFsIGRhdGEgZm9yIHRoZSB0eXBlIGluIHRoZSBmaWVsZCBkZWZpbml0aW9uXG5jb25zdCBpbml0aWFsRGF0YUZvclR5cGUgPSAoZmllbGREZWYpID0+XG5cdGZpZWxkRGVmLmRlZmF1bHRWYWx1ZSB8fCAoZmllbGREZWYudHlwZSA9PT0gXCJyZWxhdGlvblwiIHx8IGZpZWxkRGVmLnR5cGUgPT09IFwia2V5d29yZFwiID8ge30gOiBpbml0aWFsRGF0YVtmaWVsZERlZi50eXBlXSk7XG5cbmNvbnN0IGFkZEZpZWxkc1RvRW50aXR5ID0gKGZpZWxkcykgPT4gKGRpc3BhdGNoKSA9PiB7XG5cdGZpZWxkcy5mb3JFYWNoKChmaWVsZCkgPT4ge1xuXHRcdGlmIChmaWVsZC50eXBlID09PSBcInJlbGF0aW9uXCIpIHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBbXCJAcmVsYXRpb25zXCIsIGZpZWxkLm5hbWVdLCB2YWx1ZTogW119KTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0VOVElUWV9GSUVMRF9WQUxVRVwiLCBmaWVsZFBhdGg6IFtmaWVsZC5uYW1lXSwgdmFsdWU6IGluaXRpYWxEYXRhRm9yVHlwZShmaWVsZCl9KTtcblx0XHR9XG5cdH0pXG59O1xuXG5jb25zdCBmZXRjaEVudGl0eUxpc3QgPSAoZG9tYWluLCBuZXh0ID0gKCkgPT4ge30pID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IDB9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZG9tYWluLCAwLCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiB7XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSk7XG5cdFx0bmV4dChkYXRhKTtcblx0fSk7XG59O1xuXG5jb25zdCBwYWdpbmF0ZUxlZnQgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IG5ld1N0YXJ0ID0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5zdGFydCAtIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cztcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIG5ld1N0YXJ0IDwgMCA/IDAgOiBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3QgcGFnaW5hdGVSaWdodCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgbmV3U3RhcnQgPSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnN0YXJ0ICsgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogbmV3U3RhcnR9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBuZXdTdGFydCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhfSkpO1xufTtcblxuY29uc3Qgc2VuZFF1aWNrU2VhcmNoID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSB9ID0gZ2V0U3RhdGUoKTtcblx0aWYgKHF1aWNrU2VhcmNoLnF1ZXJ5Lmxlbmd0aCkge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiAwfSk7XG5cdFx0Y29uc3QgY2FsbGJhY2sgPSAoZGF0YSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiLCBkYXRhOiBkYXRhLm1hcCgoZCkgPT4gKFxuXHRcdFx0e1xuXHRcdFx0XHRfaWQ6IGQua2V5LnJlcGxhY2UoLy4qXFwvLywgXCJcIiksXG5cdFx0XHRcdFwiQGRpc3BsYXlOYW1lXCI6IGQudmFsdWVcblx0XHRcdH1cblx0XHQpKX0pO1xuXHRcdGF1dG9jb21wbGV0ZShgZG9tYWluLyR7ZW50aXR5LmRvbWFpbn0vYXV0b2NvbXBsZXRlYCwgcXVpY2tTZWFyY2gucXVlcnksIGNhbGxiYWNrKTtcblx0fSBlbHNlIHtcblx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZW50aXR5LmRvbWFpbikpO1xuXHR9XG59O1xuXG5jb25zdCBzZWxlY3REb21haW4gPSAoZG9tYWluKSA9PiAoZGlzcGF0Y2gpID0+IHtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RPTUFJTlwiLCBkb21haW59KTtcblx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGRvbWFpbikpO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIiwgdmFsdWU6IFwiXCJ9KTtcbn07XG5cbi8vIDEpIEZldGNoIGVudGl0eVxuLy8gMikgRGlzcGF0Y2ggUkVDRUlWRV9FTlRJVFkgZm9yIHJlbmRlclxuY29uc3Qgc2VsZWN0RW50aXR5ID0gKGRvbWFpbiwgZW50aXR5SWQsIGVycm9yTWVzc2FnZSA9IG51bGwsIHN1Y2Nlc3NNZXNzYWdlID0gbnVsbCwgbmV4dCA9ICgpID0+IHsgfSkgPT5cblx0KGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRcdGNvbnN0IHsgZW50aXR5OiB7IGRvbWFpbjogY3VycmVudERvbWFpbiB9IH0gPSBnZXRTdGF0ZSgpO1xuXHRcdGlmIChjdXJyZW50RG9tYWluICE9PSBkb21haW4pIHtcblx0XHRcdGRpc3BhdGNoKHNlbGVjdERvbWFpbihkb21haW4pKTtcblx0XHR9XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiQkVGT1JFX0ZFVENIX0VOVElUWVwifSlcblx0XHRjcnVkLmZldGNoRW50aXR5KGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59LyR7ZW50aXR5SWR9YCwgKGRhdGEpID0+IHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZXCIsIGRvbWFpbjogZG9tYWluLCBkYXRhOiBkYXRhLCBlcnJvck1lc3NhZ2U6IGVycm9yTWVzc2FnZX0pO1xuXHRcdFx0aWYgKHN1Y2Nlc3NNZXNzYWdlICE9PSBudWxsKSB7XG5cdFx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNVQ0NFU1NfTUVTU0FHRVwiLCBtZXNzYWdlOiBzdWNjZXNzTWVzc2FnZX0pO1xuXHRcdFx0fVxuXHRcdH0sICgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0ZBSUxVUkVcIiwgZXJyb3JNZXNzYWdlOiBgRmFpbGVkIHRvIGZldGNoICR7ZG9tYWlufSB3aXRoIElEICR7ZW50aXR5SWR9YH0pKTtcblx0XHRuZXh0KCk7XG5cdH07XG5cblxuLy8gMSkgRGlzcGF0Y2ggUkVDRUlWRV9FTlRJVFkgd2l0aCBlbXB0eSBlbnRpdHkgc2tlbGV0b24gZm9yIHJlbmRlclxuY29uc3QgbWFrZU5ld0VudGl0eSA9IChkb21haW4sIGVycm9yTWVzc2FnZSA9IG51bGwpID0+XG5cdChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IGRpc3BhdGNoKHtcblx0XHR0eXBlOiBcIlJFQ0VJVkVfRU5USVRZXCIsXG5cdFx0ZG9tYWluOiBkb21haW4sXG5cdFx0ZGF0YToge1wiQHJlbGF0aW9uc1wiOiB7fX0sXG5cdFx0ZXJyb3JNZXNzYWdlOiBlcnJvck1lc3NhZ2Vcblx0fSk7XG5cbmNvbnN0IGRlbGV0ZUVudGl0eSA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y3J1ZC5kZWxldGVFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCxcblx0XHQoKSA9PiB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTVUNDRVNTX01FU1NBR0VcIiwgbWVzc2FnZTogYFN1Y2Vzc2Z1bGx5IGRlbGV0ZWQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kb21haW59IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gfSk7XG5cdFx0XHRkaXNwYXRjaChtYWtlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpO1xuXHRcdFx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpO1xuXHRcdH0sXG5cdFx0KCkgPT4gZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGBGYWlsZWQgdG8gZGVsZXRlICR7Z2V0U3RhdGUoKS5lbnRpdHkuZG9tYWlufSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YCkpKTtcbn07XG5cbi8vIDEpIFNhdmUgYW4gZW50aXR5XG4vLyAyKSBTYXZlIHRoZSByZWxhdGlvbnMgZm9yIHRoaXMgZW50aXR5XG4vLyAzKSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5jb25zdCBzYXZlRW50aXR5ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCBjb2xsZWN0aW9uTGFiZWwgPSBnZXRTdGF0ZSgpLnZyZS5jb2xsZWN0aW9uc1tnZXRTdGF0ZSgpLmVudGl0eS5kb21haW5dLmNvbGxlY3Rpb25MYWJlbC5yZXBsYWNlKC9zJC8sIFwiXCIpO1xuXG5cdC8vIE1ha2UgYSBkZWVwIGNvcHkgb2YgdGhlIGRhdGEgdG8gYmUgc2F2ZWQgaW4gb3JkZXIgdG8gbGVhdmUgYXBwbGljYXRpb24gc3RhdGUgdW5hbHRlcmVkXG5cdGxldCBzYXZlRGF0YSA9IGNsb25lKGdldFN0YXRlKCkuZW50aXR5LmRhdGEpO1xuXHQvLyBNYWtlIGEgZGVlcCBjb3B5IG9mIHRoZSByZWxhdGlvbiBkYXRhIGluIG9yZGVyIHRvIGxlYXZlIGFwcGxpY2F0aW9uIHN0YXRlIHVuYWx0ZXJlZFxuXHRsZXQgcmVsYXRpb25EYXRhID0gY2xvbmUoc2F2ZURhdGFbXCJAcmVsYXRpb25zXCJdKSB8fCB7fTtcblx0Ly8gRGVsZXRlIHRoZSByZWxhdGlvbiBkYXRhIGZyb20gdGhlIHNhdmVEYXRhIGFzIGl0IGlzIG5vdCBleHBlY3RlZCBieSB0aGUgc2VydmVyXG5cdGRlbGV0ZSBzYXZlRGF0YVtcIkByZWxhdGlvbnNcIl07XG5cblx0aWYgKGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0Ly8gMSkgVXBkYXRlIHRoZSBlbnRpdHkgd2l0aCBzYXZlRGF0YVxuXHRcdGNydWQudXBkYXRlRW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgc2F2ZURhdGEsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsIChlcnIsIHJlc3ApID0+XG5cdFx0XHQvLyAyKSBTYXZlIHJlbGF0aW9ucyB1c2luZyBzZXJ2ZXIgcmVzcG9uc2UgZm9yIGN1cnJlbnQgcmVsYXRpb25zIHRvIGRpZmYgYWdhaW5zdCByZWxhdGlvbkRhdGFcblx0XHRcdGRpc3BhdGNoKChyZWRpc3BhdGNoKSA9PiBzYXZlUmVsYXRpb25zKEpTT04ucGFyc2UocmVzcC5ib2R5KSwgcmVsYXRpb25EYXRhLCBnZXRTdGF0ZSgpLnZyZS5jb2xsZWN0aW9uc1tnZXRTdGF0ZSgpLmVudGl0eS5kb21haW5dLnByb3BlcnRpZXMsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsICgpID0+XG5cdFx0XHRcdC8vIDMpIFJlZmV0Y2ggZW50aXR5IGZvciByZW5kZXJcblx0XHRcdFx0cmVkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgbnVsbCwgYFN1Y2Nlc2Z1bGx5IHNhdmVkICR7Y29sbGVjdGlvbkxhYmVsfSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YCwgKCkgPT4gZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpKSkpKSwgKCkgPT5cblx0XHRcdFx0XHQvLyAyYSkgSGFuZGxlIGVycm9yIGJ5IHJlZmV0Y2hpbmcgYW5kIHBhc3NpbmcgYWxvbmcgYW4gZXJyb3IgbWVzc2FnZVxuXHRcdFx0XHRcdGRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBgRmFpbGVkIHRvIHNhdmUgJHtjb2xsZWN0aW9uTGFiZWx9IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gKSkpO1xuXG5cdH0gZWxzZSB7XG5cdFx0Ly8gMSkgQ3JlYXRlIG5ldyBlbnRpdHkgd2l0aCBzYXZlRGF0YVxuXHRcdGNydWQuc2F2ZU5ld0VudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIHNhdmVEYXRhLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoZXJyLCByZXNwKSA9PlxuXHRcdFx0Ly8gMikgRmV0Y2ggZW50aXR5IHZpYSBsb2NhdGlvbiBoZWFkZXJcblx0XHRcdGRpc3BhdGNoKChyZWRpc3BhdGNoKSA9PiBjcnVkLmZldGNoRW50aXR5KHJlc3AuaGVhZGVycy5sb2NhdGlvbiwgKGRhdGEpID0+XG5cdFx0XHRcdC8vIDMpIFNhdmUgcmVsYXRpb25zIHVzaW5nIHNlcnZlciByZXNwb25zZSBmb3IgY3VycmVudCByZWxhdGlvbnMgdG8gZGlmZiBhZ2FpbnN0IHJlbGF0aW9uRGF0YVxuXHRcdFx0XHRzYXZlUmVsYXRpb25zKGRhdGEsIHJlbGF0aW9uRGF0YSwgZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5wcm9wZXJ0aWVzLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoKSA9PlxuXHRcdFx0XHRcdC8vIDQpIFJlZmV0Y2ggZW50aXR5IGZvciByZW5kZXJcblx0XHRcdFx0XHRyZWRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGRhdGEuX2lkLCBudWxsLCBgU3VjY2VzZnVsbHkgc2F2ZWQgJHtjb2xsZWN0aW9uTGFiZWx9YCwgKCkgPT4gZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbikpKSkpKSksICgpID0+XG5cdFx0XHRcdFx0XHQvLyAyYSkgSGFuZGxlIGVycm9yIGJ5IHJlZmV0Y2hpbmcgYW5kIHBhc3NpbmcgYWxvbmcgYW4gZXJyb3IgbWVzc2FnZVxuXHRcdFx0XHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGBGYWlsZWQgdG8gc2F2ZSBuZXcgJHtjb2xsZWN0aW9uTGFiZWx9YCkpKTtcblx0fVxufTtcblxuXG5leHBvcnQgeyBzYXZlRW50aXR5LCBzZWxlY3RFbnRpdHksIG1ha2VOZXdFbnRpdHksIGRlbGV0ZUVudGl0eSwgZmV0Y2hFbnRpdHlMaXN0LCBwYWdpbmF0ZVJpZ2h0LCBwYWdpbmF0ZUxlZnQsIHNlbmRRdWlja1NlYXJjaCwgc2VsZWN0RG9tYWluLCBhZGRGaWVsZHNUb0VudGl0eSB9OyIsImltcG9ydCB7IHNhdmVFbnRpdHksIHNlbGVjdEVudGl0eSwgbWFrZU5ld0VudGl0eSwgZGVsZXRlRW50aXR5LCBhZGRGaWVsZHNUb0VudGl0eSxcblx0c2VsZWN0RG9tYWluLCBwYWdpbmF0ZUxlZnQsIHBhZ2luYXRlUmlnaHQsIHNlbmRRdWlja1NlYXJjaCwgZmV0Y2hFbnRpdHlMaXN0IH0gZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQgeyBzZXRWcmUgfSBmcm9tIFwiLi92cmVcIjtcblxuZXhwb3J0IGRlZmF1bHQgKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSA9PiAoe1xuXHRvbk5ldzogKGRvbWFpbikgPT4gZGlzcGF0Y2gobWFrZU5ld0VudGl0eShkb21haW4pKSxcblx0b25TZWxlY3Q6IChyZWNvcmQpID0+IGRpc3BhdGNoKHNlbGVjdEVudGl0eShyZWNvcmQuZG9tYWluLCByZWNvcmQuaWQpKSxcblx0b25TYXZlOiAoKSA9PiBkaXNwYXRjaChzYXZlRW50aXR5KCkpLFxuXHRvbkRlbGV0ZTogKCkgPT4gZGlzcGF0Y2goZGVsZXRlRW50aXR5KCkpLFxuXHRvbkNoYW5nZTogKGZpZWxkUGF0aCwgdmFsdWUpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBmaWVsZFBhdGgsIHZhbHVlOiB2YWx1ZX0pLFxuXHRvbkFkZFNlbGVjdGVkRmllbGRzOiAoZmllbGRzKSA9PiBkaXNwYXRjaChhZGRGaWVsZHNUb0VudGl0eShmaWVsZHMpKSxcblxuXHRvblJlZGlyZWN0VG9GaXJzdDogKGNvbGxlY3Rpb24pID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChjb2xsZWN0aW9uLCAobGlzdCkgPT4ge1xuXHRcdGlmIChsaXN0Lmxlbmd0aCA+IDApIHtcblx0XHRcdG5hdmlnYXRlVG8oJ2VudGl0eScsIFtjb2xsZWN0aW9uLCBsaXN0WzBdLl9pZF0pO1xuXHRcdH1cblx0fSkpLFxuXG5cdG9uTG9naW5DaGFuZ2U6IChyZXNwb25zZSkgPT4gZGlzcGF0Y2goc2V0VXNlcihyZXNwb25zZSkpLFxuXHRvblNlbGVjdFZyZTogKHZyZUlkKSA9PiBkaXNwYXRjaChzZXRWcmUodnJlSWQpKSxcblx0b25EaXNtaXNzTWVzc2FnZTogKG1lc3NhZ2VJbmRleCkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiRElTTUlTU19NRVNTQUdFXCIsIG1lc3NhZ2VJbmRleDogbWVzc2FnZUluZGV4fSksXG5cdG9uU2VsZWN0RG9tYWluOiAoZG9tYWluKSA9PiB7XG5cdFx0ZGlzcGF0Y2goc2VsZWN0RG9tYWluKGRvbWFpbikpO1xuXHR9LFxuXHRvblBhZ2luYXRlTGVmdDogKCkgPT4gZGlzcGF0Y2gocGFnaW5hdGVMZWZ0KCkpLFxuXHRvblBhZ2luYXRlUmlnaHQ6ICgpID0+IGRpc3BhdGNoKHBhZ2luYXRlUmlnaHQoKSksXG5cdG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZTogKHZhbHVlKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIiwgdmFsdWU6IHZhbHVlfSksXG5cdG9uUXVpY2tTZWFyY2g6ICgpID0+IGRpc3BhdGNoKHNlbmRRdWlja1NlYXJjaCgpKVxufSk7IiwiaW1wb3J0IHsgc2F2ZU5ld0VudGl0eSwgdXBkYXRlRW50aXR5IH0gZnJvbSBcIi4vY3J1ZFwiO1xuXG5jb25zdCBzYXZlUmVsYXRpb25zVjIxID0gKGRhdGEsIHJlbGF0aW9uRGF0YSwgZmllbGREZWZzLCB0b2tlbiwgdnJlSWQsIG5leHQpID0+IHtcblx0Ly8gUmV0dXJucyB0aGUgZG9tYWluIGJhc2VkIG9uIHRoZSBmaWVsZERlZmluaXRpb25zIGFuZCB0aGUgcmVsYXRpb24ga2V5IChpLmUuIFwiaGFzQmlydGhQbGFjZVwiKVxuXHRjb25zdCBtYWtlUmVsYXRpb25BcmdzID0gKHJlbGF0aW9uLCBrZXksIGFjY2VwdGVkID0gdHJ1ZSwgaWQgPSBudWxsLCByZXYgPSBudWxsKSA9PiB7XG5cdFx0Y29uc3QgZmllbGREZWYgPSBmaWVsZERlZnMuZmluZCgoZGVmKSA9PiBkZWYubmFtZSA9PT0ga2V5KTtcblxuXG5cdFx0Y29uc3Qgc291cmNlVHlwZSA9IGRhdGFbXCJAdHlwZVwiXS5yZXBsYWNlKC9zJC8sIFwiXCIpLnJlcGxhY2UoL153dy8sIFwiXCIpO1xuXHRcdGNvbnN0IHRhcmdldFR5cGUgPSBmaWVsZERlZi5yZWxhdGlvbi50YXJnZXRDb2xsZWN0aW9uLnJlcGxhY2UoL3MkLywgXCJcIikucmVwbGFjZSgvXnd3LywgXCJcIik7XG5cblx0XHRjb25zdCByZWxhdGlvblNhdmVEYXRhID0ge1xuXHRcdFx0XCJAdHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24ucmVwbGFjZSgvcyQvLCBcIlwiKSwgLy8gY2hlY2tcblx0XHRcdFwiXnNvdXJjZUlkXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gcmVsYXRpb24uaWQgOiBkYXRhLl9pZCwgLy8gY2hlY2tcblx0XHRcdFwiXnNvdXJjZVR5cGVcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyB0YXJnZXRUeXBlIDogc291cmNlVHlwZSwgLy8gY2hlY2tcblx0XHRcdFwiXnRhcmdldElkXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gZGF0YS5faWQgOiByZWxhdGlvbi5pZCwgLy8gY2hlY2tcblx0XHRcdFwiXnRhcmdldFR5cGVcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyBzb3VyY2VUeXBlIDogdGFyZ2V0VHlwZSxcblx0XHRcdFwiXnR5cGVJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5yZWxhdGlvblR5cGVJZCwgLy8gY2hlY2tcblx0XHRcdGFjY2VwdGVkOiBhY2NlcHRlZFxuXHRcdH07XG5cblx0XHRpZihpZCkgeyByZWxhdGlvblNhdmVEYXRhLl9pZCA9IGlkOyB9XG5cdFx0aWYocmV2KSB7IHJlbGF0aW9uU2F2ZURhdGFbXCJecmV2XCJdID0gcmV2OyB9XG5cdFx0cmV0dXJuIFtcblx0XHRcdGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uQ29sbGVjdGlvbiwgLy8gZG9tYWluXG5cdFx0XHRyZWxhdGlvblNhdmVEYXRhXG5cdFx0XTtcblx0fTtcblxuXHQvLyBDb25zdHJ1Y3RzIGFuIGFycmF5IG9mIGFyZ3VtZW50cyBmb3Igc2F2aW5nIG5ldyByZWxhdGlvbnM6XG5cdC8vIFtcblx0Ly8gICBbXCJ3d3JlbGF0aW9uc1wiLCB7IC4uLiB9XSxcblx0Ly8gICBbXCJ3d3JlbGF0aW9uc1wiLCB7IC4uLiB9XSxcblx0Ly8gXVxuXHRjb25zdCBuZXdSZWxhdGlvbnMgPSBPYmplY3Qua2V5cyhyZWxhdGlvbkRhdGEpLm1hcCgoa2V5KSA9PlxuXHRcdFx0cmVsYXRpb25EYXRhW2tleV1cblx0XHRcdC8vIEZpbHRlcnMgb3V0IGFsbCByZWxhdGlvbnMgd2hpY2ggYXJlIG5vdCBhbHJlYWR5IGluIGRhdGFbXCJAcmVsYXRpb25zXCJdXG5cdFx0XHRcdC5maWx0ZXIoKHJlbGF0aW9uKSA9PiAoZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XSB8fCBbXSkubWFwKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5pZCkuaW5kZXhPZihyZWxhdGlvbi5pZCkgPCAwKVxuXHRcdFx0XHQvLyBNYWtlIGFyZ3VtZW50IGFycmF5IGZvciBuZXcgcmVsYXRpb25zOiBbXCJ3d3JlbGF0aW9uc1wiLCB7IC4uLiB9XVxuXHRcdFx0XHQubWFwKChyZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhyZWxhdGlvbiwga2V5KSlcblx0XHQvLyBGbGF0dGVuIG5lc3RlZCBhcnJheXNcblx0KS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSk7XG5cblxuXHQvLyBSZWFjdGl2YXRlIHByZXZpb3VzbHkgYWRkZWQgcmVsYXRpb25zIHVzaW5nIFBVVCB3aGljaCB3ZXJlICdkZWxldGVkJyBhZnRlciB1c2luZyBQVVRcblx0Y29uc3QgcmVBZGRSZWxhdGlvbnMgPSBPYmplY3Qua2V5cyhyZWxhdGlvbkRhdGEpLm1hcCgoa2V5KSA9PlxuXHRcdChkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldIHx8IFtdKVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uYWNjZXB0ZWQgPT09IGZhbHNlKVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiAocmVsYXRpb25EYXRhW2tleV0gfHwgW10pLmZpbHRlcigocmVsYXRpb24pID0+IHJlbGF0aW9uLmFjY2VwdGVkKS5tYXAoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5pZCkuaW5kZXhPZihvcmlnUmVsYXRpb24uaWQpID4gLTEpXG5cdFx0XHQubWFwKChvcmlnUmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3Mob3JpZ1JlbGF0aW9uLCBrZXksIHRydWUsIG9yaWdSZWxhdGlvbi5yZWxhdGlvbklkLCBvcmlnUmVsYXRpb24ucmV2KSlcblx0KS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSk7XG5cblx0Ly8gRGVhY3RpdmF0ZSBwcmV2aW91c2x5IGFkZGVkIHJlbGF0aW9ucyB1c2luZyBQVVRcblx0Y29uc3QgZGVsZXRlUmVsYXRpb25zID0gT2JqZWN0LmtleXMoZGF0YVtcIkByZWxhdGlvbnNcIl0pLm1hcCgoa2V5KSA9PlxuXHRcdGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV1cblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmFjY2VwdGVkKVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiAocmVsYXRpb25EYXRhW2tleV0gfHwgW10pLm1hcCgocmVsYXRpb24pID0+IHJlbGF0aW9uLmlkKS5pbmRleE9mKG9yaWdSZWxhdGlvbi5pZCkgPCAwKVxuXHRcdFx0Lm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKG9yaWdSZWxhdGlvbiwga2V5LCBmYWxzZSwgb3JpZ1JlbGF0aW9uLnJlbGF0aW9uSWQsIG9yaWdSZWxhdGlvbi5yZXYpKVxuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXHQvLyBDb21iaW5lcyBzYXZlTmV3RW50aXR5IGFuZCBkZWxldGVFbnRpdHkgaW5zdHJ1Y3Rpb25zIGludG8gcHJvbWlzZXNcblx0Y29uc3QgcHJvbWlzZXMgPSBuZXdSZWxhdGlvbnNcblx0Ly8gTWFwIG5ld1JlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiBzYXZlTmV3RW50aXR5XG5cdFx0Lm1hcCgoYXJncykgPT4gbmV3IFByb21pc2UoKHJlc29sdmUsIHJlamVjdCkgPT4gc2F2ZU5ld0VudGl0eSguLi5hcmdzLCB0b2tlbiwgdnJlSWQsIHJlc29sdmUsIHJlamVjdCkgKSlcblx0XHQvLyBNYXAgcmVhZGRSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2YgdXBkYXRlRW50aXR5XG5cdFx0LmNvbmNhdChyZUFkZFJlbGF0aW9ucy5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHVwZGF0ZUVudGl0eSguLi5hcmdzLCB0b2tlbiwgdnJlSWQsIHJlc29sdmUsIHJlamVjdCkpKSlcblx0XHQvLyBNYXAgZGVsZXRlUmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHVwZGF0ZUVudGl0eVxuXHRcdC5jb25jYXQoZGVsZXRlUmVsYXRpb25zLm1hcCgoYXJncykgPT4gbmV3IFByb21pc2UoKHJlc29sdmUsIHJlamVjdCkgPT4gdXBkYXRlRW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSkpKTtcblxuXHQvLyBJbnZva2UgYWxsIENSVUQgb3BlcmF0aW9ucyBmb3IgdGhlIHJlbGF0aW9uc1xuXHRQcm9taXNlLmFsbChwcm9taXNlcykudGhlbihuZXh0LCBuZXh0KTtcbn07XG5cbmV4cG9ydCBkZWZhdWx0IHNhdmVSZWxhdGlvbnNWMjE7IiwiaW1wb3J0IHhociBmcm9tIFwieGhyXCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4uL3N0b3JlXCI7XG5cbmV4cG9ydCBkZWZhdWx0IHtcblx0cGVyZm9ybVhocjogZnVuY3Rpb24gKG9wdGlvbnMsIGFjY2VwdCwgcmVqZWN0ID0gKCkgPT4geyBjb25zb2xlLndhcm4oXCJVbmRlZmluZWQgcmVqZWN0IGNhbGxiYWNrISBcIik7IH0sIG9wZXJhdGlvbiA9IFwiU2VydmVyIHJlcXVlc3RcIikge1xuXHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlJFUVVFU1RfTUVTU0FHRVwiLCBtZXNzYWdlOiBgJHtvcGVyYXRpb259OiAke29wdGlvbnMubWV0aG9kIHx8IFwiR0VUXCJ9ICR7b3B0aW9ucy51cmx9YH0pO1xuXHRcdHhocihvcHRpb25zLCAoZXJyLCByZXNwLCBib2R5KSA9PiB7XG5cdFx0XHRpZihyZXNwLnN0YXR1c0NvZGUgPj0gNDAwKSB7XG5cdFx0XHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIkVSUk9SX01FU1NBR0VcIiwgbWVzc2FnZTogYCR7b3BlcmF0aW9ufSBmYWlsZWQgd2l0aCBjYXVzZTogJHtyZXNwLmJvZHl9YH0pO1xuXHRcdFx0XHRyZWplY3QoZXJyLCByZXNwLCBib2R5KTtcblx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdGFjY2VwdChlcnIsIHJlc3AsIGJvZHkpO1xuXHRcdFx0fVxuXHRcdH0pO1xuXHR9LFxuXG5cdGZhc3RYaHI6IGZ1bmN0aW9uKG9wdGlvbnMsIGFjY2VwdCkge1xuXHRcdHhocihvcHRpb25zLCBhY2NlcHQpO1xuXHR9LFxuXG5cdG1ha2VIZWFkZXJzOiBmdW5jdGlvbih0b2tlbiwgdnJlSWQpIHtcblx0XHRyZXR1cm4ge1xuXHRcdFx0XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCIsXG5cdFx0XHRcIkNvbnRlbnQtdHlwZVwiOiBcImFwcGxpY2F0aW9uL2pzb25cIixcblx0XHRcdFwiQXV0aG9yaXphdGlvblwiOiB0b2tlbixcblx0XHRcdFwiVlJFX0lEXCI6IHZyZUlkXG5cdFx0fTtcblx0fVxufTtcbiIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9pbmRleFwiO1xuaW1wb3J0IHttYWtlTmV3RW50aXR5fSBmcm9tIFwiLi9lbnRpdHlcIjtcbmltcG9ydCB7ZmV0Y2hFbnRpdHlMaXN0fSBmcm9tIFwiLi9lbnRpdHlcIjtcblxuY29uc3QgbGlzdFZyZXMgPSAoKSA9PiAoZGlzcGF0Y2gpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1xuXHRcdFx0XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJcblx0XHR9LFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL3N5c3RlbS92cmVzYFxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiTElTVF9WUkVTXCIsIGxpc3Q6IEpTT04ucGFyc2UocmVzcC5ib2R5KX0pO1xuXHR9LCBudWxsLCBcIkxpc3QgVlJFc1wiKTtcblxuY29uc3Qgc2V0VnJlID0gKHZyZUlkLCBuZXh0ID0gKCkgPT4geyB9KSA9PiAoZGlzcGF0Y2gpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1xuXHRcdFx0XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJcblx0XHR9LFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL21ldGFkYXRhLyR7dnJlSWR9P3dpdGhDb2xsZWN0aW9uSW5mbz10cnVlYFxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0aWYgKHJlc3Auc3RhdHVzQ29kZSA9PT0gMjAwKSB7XG5cdFx0XHR2YXIgYm9keSA9IEpTT04ucGFyc2UocmVzcC5ib2R5KTtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9WUkVcIiwgdnJlSWQ6IHZyZUlkLCBjb2xsZWN0aW9uczogYm9keX0pO1xuXG5cdFx0XHRsZXQgZGVmYXVsdERvbWFpbiA9IE9iamVjdC5rZXlzKGJvZHkpXG5cdFx0XHRcdC5tYXAoY29sbGVjdGlvbk5hbWUgPT4gYm9keVtjb2xsZWN0aW9uTmFtZV0pXG5cdFx0XHRcdC5maWx0ZXIoY29sbGVjdGlvbiA9PiAhY29sbGVjdGlvbi51bmtub3duICYmICFjb2xsZWN0aW9uLnJlbGF0aW9uQ29sbGVjdGlvbilbMF1cblx0XHRcdFx0LmNvbGxlY3Rpb25OYW1lO1xuXG5cdFx0XHRkaXNwYXRjaChtYWtlTmV3RW50aXR5KGRlZmF1bHREb21haW4pKVxuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX0RPTUFJTlwiLCBkZWZhdWx0RG9tYWlufSk7XG5cdFx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZGVmYXVsdERvbWFpbikpO1xuXHRcdFx0bmV4dCgpO1xuXHRcdH1cblx0fSwgKCkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1ZSRVwiLCB2cmVJZDogdnJlSWQsIGNvbGxlY3Rpb25zOiB7fX0pLCBgRmV0Y2ggVlJFIGRlc2NyaXB0aW9uIGZvciAke3ZyZUlkfWApO1xuXG5cbmV4cG9ydCB7bGlzdFZyZXMsIHNldFZyZX07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2xhc3NuYW1lcyBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuaW1wb3J0IHt1cmxzfSBmcm9tIFwiLi4vLi4vdXJsc1wiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcblxuY2xhc3MgQ29sbGVjdGlvblRhYnMgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgY29sbGVjdGlvbnMsIGFjdGl2ZURvbWFpbiwgb25SZWRpcmVjdFRvRmlyc3QgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgZG9tYWlucyA9IE9iamVjdC5rZXlzKGNvbGxlY3Rpb25zIHx8IHt9KTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lciBiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXYtdGFic1wiPlxuICAgICAgICAgIHtkb21haW5zXG4gICAgICAgICAgICAuZmlsdGVyKGQgPT4gIShjb2xsZWN0aW9uc1tkXS51bmtub3duIHx8IGNvbGxlY3Rpb25zW2RdLnJlbGF0aW9uQ29sbGVjdGlvbikpXG4gICAgICAgICAgICAubWFwKChkb21haW4pID0+IChcbiAgICAgICAgICAgICAgPGxpIGNsYXNzTmFtZT17Y2xhc3NuYW1lcyh7YWN0aXZlOiBkb21haW4gPT09IGFjdGl2ZURvbWFpbn0pfSBrZXk9e2RvbWFpbn0+XG4gICAgICAgICAgICAgICAgPGEgb25DbGljaz17KCkgPT4gb25SZWRpcmVjdFRvRmlyc3QoZG9tYWluKX0+XG4gICAgICAgICAgICAgICAgICB7Y29sbGVjdGlvbnNbZG9tYWluXS5jb2xsZWN0aW9uTGFiZWx9XG4gICAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkNvbGxlY3Rpb25UYWJzLnByb3BUeXBlcyA9IHtcblx0b25OZXc6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvblNlbGVjdERvbWFpbjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdGNvbGxlY3Rpb25zOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRhY3RpdmVEb21haW46IFJlYWN0LlByb3BUeXBlcy5zdHJpbmdcbn07XG5cbmV4cG9ydCBkZWZhdWx0IENvbGxlY3Rpb25UYWJzO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFBhZ2UgZnJvbSBcIi4uL3BhZ2UuanN4XCI7XG5cbmltcG9ydCBQYWdpbmF0ZSBmcm9tIFwiLi9lbnRpdHktaW5kZXgvcGFnaW5hdGVcIjtcbmltcG9ydCBRdWlja1NlYXJjaCBmcm9tIFwiLi9lbnRpdHktaW5kZXgvcXVpY2tzZWFyY2hcIjtcbmltcG9ydCBFbnRpdHlMaXN0IGZyb20gXCIuL2VudGl0eS1pbmRleC9saXN0XCI7XG5cbmltcG9ydCBTYXZlRm9vdGVyIGZyb20gXCIuL2VudGl0eS1mb3JtL3NhdmUtZm9vdGVyXCI7XG5pbXBvcnQgRW50aXR5Rm9ybSBmcm9tIFwiLi9lbnRpdHktZm9ybS9mb3JtXCI7XG5cbmltcG9ydCBDb2xsZWN0aW9uVGFicyBmcm9tIFwiLi9jb2xsZWN0aW9uLXRhYnNcIjtcbmltcG9ydCBNZXNzYWdlcyBmcm9tIFwiLi9tZXNzYWdlcy9saXN0XCI7XG5pbXBvcnQgTWVzc2FnZSBmcm9tIFwiLi4vbWVzc2FnZVwiO1xuXG5jbGFzcyBFZGl0R3VpIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGNvbnN0IHsgb25TZWxlY3QsIG9uTmV3LCBvblNlbGVjdERvbWFpbiB9ID0gdGhpcy5wcm9wcztcblxuXHRcdC8vIFRyaWdnZXJzIGZldGNoIGRhdGEgZnJvbSBzZXJ2ZXIgYmFzZWQgb24gaWQgZnJvbSByb3V0ZS5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQgIT09IG5leHRQcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdG9uU2VsZWN0KHtkb21haW46IG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IG5leHRQcm9wcy5wYXJhbXMuaWR9KTtcblx0XHR9XG5cdH1cblxuXHRjb21wb25lbnREaWRNb3VudCgpIHtcblxuXHRcdGlmICh0aGlzLnByb3BzLnBhcmFtcy5pZCkge1xuXHRcdFx0dGhpcy5wcm9wcy5vblNlbGVjdCh7ZG9tYWluOiB0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uLCBpZDogdGhpcy5wcm9wcy5wYXJhbXMuaWR9KTtcblx0XHR9IGVsc2UgaWYgKCF0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uICYmICF0aGlzLnByb3BzLmxvY2F0aW9uLnBhdGhuYW1lLm1hdGNoKC9uZXckLykgJiYgdGhpcy5wcm9wcy5lbnRpdHkuZG9tYWluKSB7XG5cdFx0XHR0aGlzLnByb3BzLm9uUmVkaXJlY3RUb0ZpcnN0KHRoaXMucHJvcHMuZW50aXR5LmRvbWFpbilcblx0XHR9IGVsc2UgaWYgKHRoaXMucHJvcHMubG9jYXRpb24ucGF0aG5hbWUubWF0Y2goL25ldyQvKSkge1xuXHRcdFx0dGhpcy5wcm9wcy5vbk5ldyh0aGlzLnByb3BzLmVudGl0eS5kb21haW4pO1xuXHRcdH1cblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG9uU2VsZWN0LCBvbk5ldywgb25TYXZlLCBvbkRlbGV0ZSwgb25TZWxlY3REb21haW4sIG9uRGlzbWlzc01lc3NhZ2UsIG9uQ2hhbmdlLCBvbkFkZFNlbGVjdGVkRmllbGRzLCBvblJlZGlyZWN0VG9GaXJzdCB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZSwgb25RdWlja1NlYXJjaCwgb25QYWdpbmF0ZUxlZnQsIG9uUGFnaW5hdGVSaWdodCB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IGdldEF1dG9jb21wbGV0ZVZhbHVlcyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSwgbWVzc2FnZXMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgY3VycmVudE1vZGUgPSBlbnRpdHkuZG9tYWluICYmIGVudGl0eS5kYXRhLl9pZCA/IFwiZWRpdFwiIDogXCJuZXdcIjtcblxuXHRcdGlmIChlbnRpdHkuZG9tYWluID09PSBudWxsIHx8ICF2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0pIHsgcmV0dXJuIG51bGw7IH1cblx0XHRjb25zdCBsb2dpbk1lc3NhZ2UgPSB0aGlzLnByb3BzLnVzZXIgPyBudWxsIDogKFxuXHRcdFx0PE1lc3NhZ2UgZGlzbWlzc2libGU9e2ZhbHNlfSBhbGVydExldmVsPVwid2FybmluZ1wiPlxuXHRcdFx0XHQ8Zm9ybSBhY3Rpb249XCJodHRwczovL3NlY3VyZS5odXlnZW5zLmtuYXcubmwvc2FtbDIvbG9naW5cIiBtZXRob2Q9XCJQT1NUXCIgc3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBmbG9hdDogXCJyaWdodFwifX0+XG5cdFx0XHRcdFx0PGlucHV0IG5hbWU9XCJoc3VybFwiIHZhbHVlPXtgJHtsb2NhdGlvbi5ocmVmfWB9IHR5cGU9XCJoaWRkZW5cIiAvPlxuXHRcdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi13YXJuaW5nIGJ0bi1zbVwiIHR5cGU9XCJzdWJtaXRcIj5cblx0XHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tbG9nLWluXCIgLz4gTG9nIGluXG5cdFx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHRcdDwvZm9ybT5cblx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgLz57XCIgXCJ9XG5cdFx0XHRcdFlvdSBhcmUgbm90IGxvZ2dlZCBpbiwgeW91ciBzZXNzaW9uIGhhcyBleHBpcmVkLCBvciB5b3UgYXJlIG5vdCBhbGxvd2VkIHRvIGVkaXQgdGhpcyBkYXRhc2V0XG5cdFx0XHQ8L01lc3NhZ2U+XG5cdFx0KTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8UGFnZSB1c2VybmFtZT17dGhpcy5wcm9wcy51c2VyICYmIHRoaXMucHJvcHMudXNlci51c2VyRGF0YSAmJiB0aGlzLnByb3BzLnVzZXIudXNlckRhdGEuZGlzcGxheU5hbWUgPyB0aGlzLnByb3BzLnVzZXIudXNlckRhdGEuZGlzcGxheU5hbWUgOiBcIlwifT5cblx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIiBzdHlsZT17e3RleHRBbGlnbjogXCJyaWdodFwifX0+XG5cdFx0XHRcdFx0VGhpcyBlZGl0IGludGVyZmFjZSBpcyBtYWNoaW5lLWdlbmVyYXRlZCBiYXNlZCBvbiB0aGUgZGF0YS1tb2RlbC4gPGEgaHJlZj1cImh0dHBzOi8vZ2l0aHViLmNvbS9odXlnZW5zaW5nL3RpbWJ1Y3Rvby9pc3N1ZXMvbmV3XCIgdGFyZ2V0PVwiX2JsYW5rXCI+U3VnZ2VzdGlvbnM8L2E+IGZvciBpbXByb3ZlbWVudCBhcmUgdmVyeSB3ZWxjb21lIVxuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PENvbGxlY3Rpb25UYWJzIGNvbGxlY3Rpb25zPXt2cmUuY29sbGVjdGlvbnN9IG9uTmV3PXtvbk5ld30gb25TZWxlY3REb21haW49e29uU2VsZWN0RG9tYWlufSBvblJlZGlyZWN0VG9GaXJzdD17b25SZWRpcmVjdFRvRmlyc3R9XG5cdFx0XHRcdFx0YWN0aXZlRG9tYWluPXtlbnRpdHkuZG9tYWlufSAvPlxuXHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuXHRcdFx0XHRcdHtsb2dpbk1lc3NhZ2V9XG5cdFx0XHRcdFx0PE1lc3NhZ2VzXG5cdFx0XHRcdFx0XHR0eXBlcz17W1wiU1VDQ0VTU19NRVNTQUdFXCIsIFwiRVJST1JfTUVTU0FHRVwiXX1cblx0XHRcdFx0XHRcdG1lc3NhZ2VzPXttZXNzYWdlc31cblx0XHRcdFx0XHRcdG9uRGlzbWlzc01lc3NhZ2U9e29uRGlzbWlzc01lc3NhZ2V9IC8+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJyb3dcIj5cblx0XHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLTRcIj5cblx0XHRcdFx0XHRcdFx0PFF1aWNrU2VhcmNoXG5cdFx0XHRcdFx0XHRcdFx0b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlPXtvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2V9XG5cdFx0XHRcdFx0XHRcdFx0b25RdWlja1NlYXJjaD17b25RdWlja1NlYXJjaH1cblx0XHRcdFx0XHRcdFx0XHRxdWVyeT17cXVpY2tTZWFyY2gucXVlcnl9IC8+XG5cdFx0XHRcdFx0XHRcdDxFbnRpdHlMaXN0XG5cdFx0XHRcdFx0XHRcdFx0c3RhcnQ9e3F1aWNrU2VhcmNoLnN0YXJ0fVxuXHRcdFx0XHRcdFx0XHRcdGxpc3Q9e3F1aWNrU2VhcmNoLmxpc3R9XG5cdFx0XHRcdFx0XHRcdFx0b25TZWxlY3Q9e29uU2VsZWN0fVxuXHRcdFx0XHRcdFx0XHRcdGRvbWFpbj17ZW50aXR5LmRvbWFpbn1cblx0XHRcdFx0XHRcdFx0XHRzZWxlY3RlZElkPXtlbnRpdHkuZGF0YS5faWR9XG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5UGVuZGluZz17ZW50aXR5LnBlbmRpbmd9XG5cdFx0XHRcdFx0XHRcdC8+XG5cdFx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHRcdHtlbnRpdHkucGVuZGluZyA/IChcblx0XHRcdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5Mb2FkaW5nLCBwbGVhc2Ugd2FpdC4uLjwvZGl2PlxuXHRcdFx0XHRcdFx0KSA6IGVudGl0eS5kb21haW4gPyAoXG5cdFx0XHRcdFx0XHRcdDxFbnRpdHlGb3JtIGN1cnJlbnRNb2RlPXtjdXJyZW50TW9kZX0gZ2V0QXV0b2NvbXBsZXRlVmFsdWVzPXtnZXRBdXRvY29tcGxldGVWYWx1ZXN9XG5cdFx0XHRcdFx0XHRcdFx0b25BZGRTZWxlY3RlZEZpZWxkcz17b25BZGRTZWxlY3RlZEZpZWxkc31cblx0XHRcdFx0XHRcdFx0XHRlbnRpdHk9e2VudGl0eX0gb25OZXc9e29uTmV3fSBvbkRlbGV0ZT17b25EZWxldGV9IG9uQ2hhbmdlPXtvbkNoYW5nZX0gdXNlcj17dGhpcy5wcm9wcy51c2VyfVxuXHRcdFx0XHRcdFx0XHRcdHByb3BlcnRpZXM9e3ZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXS5wcm9wZXJ0aWVzfSBcblx0XHRcdFx0XHRcdFx0XHRlbnRpdHlMYWJlbD17dnJlLmNvbGxlY3Rpb25zW2VudGl0eS5kb21haW5dLmNvbGxlY3Rpb25MYWJlbC5yZXBsYWNlKC9zJC8sIFwiXCIpIH0gLz5cblx0XHRcdFx0XHRcdCkgOiBudWxsIH1cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PC9kaXY+XG5cblx0XHRcdFx0PGRpdiB0eXBlPVwiZm9vdGVyLWJvZHlcIiBjbGFzc05hbWU9XCJyb3dcIj5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC00XCIgc3R5bGU9e3t0ZXh0QWxpZ246IFwibGVmdFwiLCBwYWRkaW5nOiAnMCd9fT5cblx0XHRcdFx0XHRcdDxQYWdpbmF0ZVxuXHRcdFx0XHRcdFx0XHRzdGFydD17cXVpY2tTZWFyY2guc3RhcnR9XG5cdFx0XHRcdFx0XHRcdGxpc3RMZW5ndGg9e3F1aWNrU2VhcmNoLmxpc3QubGVuZ3RofVxuXHRcdFx0XHRcdFx0XHRyb3dzPXs1MH1cblx0XHRcdFx0XHRcdFx0b25QYWdpbmF0ZUxlZnQ9e29uUGFnaW5hdGVMZWZ0fVxuXHRcdFx0XHRcdFx0XHRvblBhZ2luYXRlUmlnaHQ9e29uUGFnaW5hdGVSaWdodH0gLz5cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC04XCIgc3R5bGU9e3t0ZXh0QWxpZ246IFwibGVmdFwiLCBwYWRkaW5nOiAnMCd9fT5cblx0XHRcdFx0XHRcdHshZW50aXR5LnBlbmRpbmcgP1xuXHRcdFx0XHRcdFx0XHQ8U2F2ZUZvb3RlciBvblNhdmU9e29uU2F2ZX0gb25DYW5jZWw9eygpID0+IGN1cnJlbnRNb2RlID09PSBcImVkaXRcIiA/XG5cdFx0XHRcdFx0XHRcdFx0b25TZWxlY3Qoe2RvbWFpbjogZW50aXR5LmRvbWFpbiwgaWQ6IGVudGl0eS5kYXRhLl9pZH0pIDogb25OZXcoZW50aXR5LmRvbWFpbil9IHVzZXI9e3RoaXMucHJvcHMudXNlcn0vPiA6IG51bGxcblx0XHRcdFx0XHRcdH1cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L1BhZ2U+XG5cdFx0KVxuXHR9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVkaXRHdWk7XG4iLCJleHBvcnQgZGVmYXVsdCAoY2FtZWxDYXNlKSA9PiBjYW1lbENhc2VcbiAgLnJlcGxhY2UoLyhbQS1aMC05XSkvZywgKG1hdGNoKSA9PiBgICR7bWF0Y2gudG9Mb3dlckNhc2UoKX1gKVxuICAucmVwbGFjZSgvXi4vLCAobWF0Y2gpID0+IG1hdGNoLnRvVXBwZXJDYXNlKCkpO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0geyBuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwiIH07XG5cdH1cblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGlmIChuZXh0UHJvcHMuZW50aXR5LmRhdGEuX2lkICE9PSB0aGlzLnByb3BzLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGlmICh0aGlzLnN0YXRlLm5ld0xhYmVsLmxlbmd0aCA+IDAgJiYgdGhpcy5zdGF0ZS5uZXdVcmwubGVuZ3RoID4gMCkge1xuXHRcdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh7XG5cdFx0XHRcdGxhYmVsOiB0aGlzLnN0YXRlLm5ld0xhYmVsLFxuXHRcdFx0XHR1cmw6IHRoaXMuc3RhdGUubmV3VXJsXG5cdFx0XHR9KSk7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwifSk7XG5cdFx0fVxuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXVxuXHRcdFx0LmZpbHRlcigodmFsKSA9PiB2YWwudXJsICE9PSB2YWx1ZS51cmwpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWUudXJsfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz5cblx0XHRcdFx0XHQ8YSBocmVmPXt2YWx1ZS51cmx9IHRhcmdldD1cIl9ibGFua1wiPlxuXHRcdFx0XHRcdFx0e3ZhbHVlLmxhYmVsfVxuXHRcdFx0XHRcdDwvYT5cblx0XHRcdFx0PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCJ9fT5cblx0XHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2wgcHVsbC1sZWZ0XCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3TGFiZWx9XG5cdFx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkxhYmVsIGZvciB1cmwuLi5cIlxuXHRcdFx0XHRcdFx0c3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXhXaWR0aDogXCI1MCVcIn19IC8+XG5cdFx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sIHB1bGwtbGVmdFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld1VybH1cblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld1VybDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gdGhpcy5vbkFkZCgpIDogZmFsc2V9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIlVybC4uLlwiXG5cdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIG1heFdpZHRoOiBcImNhbGMoNTAlIC0gODBweClcIn19IC8+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuIHB1bGwtbGVmdFwiPlxuXHRcdFx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9PkFkZCBsaW5rPC9idXR0b24+XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHQ8L2Rpdj5cblxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBjbGVhcjogXCJsZWZ0XCJ9fSAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdGNvbnN0cnVjdG9yKHByb3BzKSB7XG5cdFx0c3VwZXIocHJvcHMpO1xuXG5cdFx0dGhpcy5zdGF0ZSA9IHsgbmV3VmFsdWU6IFwiXCIgfTtcblx0fVxuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0aWYgKG5leHRQcm9wcy5lbnRpdHkuZGF0YS5faWQgIT09IHRoaXMucHJvcHMuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh2YWx1ZSkpO1xuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKHZhbCkgPT4gdmFsICE9PSB2YWx1ZSkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZX0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e3ZhbHVlfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdWYWx1ZX1cblx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0b25LZXlQcmVzcz17KGV2KSA9PiBldi5rZXkgPT09IFwiRW50ZXJcIiA/IHRoaXMub25BZGQoZXYudGFyZ2V0LnZhbHVlKSA6IGZhbHNlfVxuXHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiQWRkIGEgdmFsdWUuLi5cIiAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRvbkFkZCh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHZhbHVlKSk7XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigodmFsKSA9PiB2YWwgIT09IHZhbHVlKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57dmFsdWV9PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxTZWxlY3RGaWVsZCBvbkNoYW5nZT17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfSBub0NsZWFyPXt0cnVlfSBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG5cdFx0XHRcdFx0PHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG5cdFx0XHRcdFx0XHRTZWxlY3Qge2xhYmVsLnRvTG93ZXJDYXNlKCl9XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHRcdHtvcHRpb25zLmZpbHRlcigob3B0KSA9PiB2YWx1ZXMuaW5kZXhPZihvcHQpIDwgMCkubWFwKChvcHRpb24pID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9TZWxlY3RGaWVsZD5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5XG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBvbkFkZCgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQoe1xuICAgICAgY29tcG9uZW50czogW3t0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn1dXG4gICAgfSkpO1xuICB9XG5cbiAgb25BZGRDb21wb25lbnQoaXRlbUluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAuY29uY2F0KHt0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn0pXG4gICAgKTtcbiAgfVxuXG4gIG9uUmVtb3ZlQ29tcG9uZW50KGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5maWx0ZXIoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggIT09IGNvbXBvbmVudEluZGV4KVxuICAgICk7XG4gIH1cblxuICBvbkNoYW5nZUNvbXBvbmVudFZhbHVlKGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgsIHZhbHVlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHZhbHVlOiB2YWx1ZX0gOiBjb21wb25lbnRcbiAgICApKTtcbiAgfVxuXG4gIG9uQ2hhbmdlQ29tcG9uZW50VHlwZShpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4LCB0eXBlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHR5cGU6IHR5cGV9IDogY29tcG9uZW50XG4gICAgKSk7XG4gIH1cblxuICBvblJlbW92ZShpdGVtSW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKChuYW1lLCBpZHgpID0+IGlkeCAhPT0gaXRlbUluZGV4KSk7XG4gIH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXG4gICAgY29uc3QgbmFtZUVsZW1lbnRzID0gdmFsdWVzLm1hcCgobmFtZSwgaSkgPT4gKFxuICAgICAgPGRpdiBrZXk9e2Ake25hbWV9LSR7aX1gfSBjbGFzc05hbWU9XCJuYW1lcy1mb3JtIGl0ZW0tZWxlbWVudFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInNtYWxsLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG4gICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKGkpfVxuICAgICAgICAgICAgdHlwZT1cImJ1dHRvblwiPlxuICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgIDxzdHJvbmc+XG4gICAgICAgICAgICB7bmFtZS5jb21wb25lbnRzLm1hcCgoY29tcG9uZW50KSA9PiBjb21wb25lbnQudmFsdWUpLmpvaW4oXCIgXCIpfVxuICAgICAgICAgIDwvc3Ryb25nPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPHVsIGtleT1cImNvbXBvbmVudC1saXN0XCI+XG4gICAgICAgICAge25hbWUuY29tcG9uZW50cy5tYXAoKGNvbXBvbmVudCwgaikgPT4gKFxuICAgICAgICAgICAgPGxpIGtleT17YCR7aX0tJHtqfS1jb21wb25lbnRgfT5cbiAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cFwiIGtleT1cImNvbXBvbmVudC12YWx1ZXNcIj5cbiAgICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICAgICAgICAgICAgPFNlbGVjdEZpZWxkIHZhbHVlPXtjb21wb25lbnQudHlwZX0gbm9DbGVhcj17dHJ1ZX1cbiAgICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWwpID0+IHRoaXMub25DaGFuZ2VDb21wb25lbnRUeXBlKGksIGosIHZhbCl9XG4gICAgICAgICAgICAgICAgICAgIGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cbiAgICAgICAgICAgICAgICAgICAge29wdGlvbnMubWFwKChvcHRpb24pID0+IChcbiAgICAgICAgICAgICAgICAgICAgICA8c3BhbiB2YWx1ZT17b3B0aW9ufSBrZXk9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG4gICAgICAgICAgICAgICAgICAgICkpfVxuICAgICAgICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgICAgICA8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiBrZXk9e2BpbnB1dC0ke2l9LSR7an1gfVxuICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gdGhpcy5vbkNoYW5nZUNvbXBvbmVudFZhbHVlKGksIGosIGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcj17Y29tcG9uZW50LnR5cGV9IHZhbHVlPXtjb21wb25lbnQudmFsdWV9IC8+XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgICAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmVDb21wb25lbnQoaSwgail9ID5cbiAgICAgICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgICAgICAgPC9zcGFuPlxuICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICAgICAgPGJ1dHRvbiBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uQWRkQ29tcG9uZW50KGkpfVxuICAgICAgICAgICAgIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdCBidG4teHMgcHVsbC1yaWdodFwiIHR5cGU9XCJidXR0b25cIj5cbiAgICAgICAgICAgIEFkZCBjb21wb25lbnRcbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICA8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBoZWlnaHQ6IFwiNnB4XCIsIGNsZWFyOiBcInJpZ2h0XCJ9fSAvPlxuICAgICAgPC9kaXY+XG4gICAgKSlcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuICAgICAgICB7bmFtZUVsZW1lbnRzfVxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e3RoaXMub25BZGQuYmluZCh0aGlzKX0+XG4gICAgICAgICAgQWRkIG5hbWVcbiAgICAgICAgPC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXksXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQgeyB1cmxzIH0gZnJvbSBcIi4uLy4uLy4uLy4uL3VybHNcIjtcblxuY2xhc3MgUmVsYXRpb25GaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIHF1ZXJ5OiBcIlwiLFxuICAgICAgc3VnZ2VzdGlvbnM6IFtdLFxuICAgICAgYmx1cklzQmxvY2tlZDogZmFsc2VcbiAgICB9XG4gIH1cblxuICBvblJlbW92ZSh2YWx1ZSkge1xuICAgIGNvbnN0IGN1cnJlbnRWYWx1ZXMgPSB0aGlzLnByb3BzLmVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuXG4gICAgdGhpcy5wcm9wcy5vbkNoYW5nZShcbiAgICAgIFtcIkByZWxhdGlvbnNcIiwgdGhpcy5wcm9wcy5uYW1lXSxcbiAgICAgIGN1cnJlbnRWYWx1ZXMuZmlsdGVyKChjdXJWYWwpID0+IGN1clZhbC5pZCAhPT0gdmFsdWUuaWQpXG4gICAgKTtcblxuICB9XG5cbiAgb25BZGQoc3VnZ2VzdGlvbikge1xuICAgIGNvbnN0IGN1cnJlbnRWYWx1ZXMgPSB0aGlzLnByb3BzLmVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuICAgIGlmIChjdXJyZW50VmFsdWVzLm1hcCgodmFsKSA9PiB2YWwuaWQpLmluZGV4T2Yoc3VnZ2VzdGlvbi5rZXkpID4gLTEpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG4gICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IFtdLCBxdWVyeTogXCJcIiwgYmx1cklzQmxvY2tlZDogZmFsc2V9KTtcblxuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoXG4gICAgICBbXCJAcmVsYXRpb25zXCIsIHRoaXMucHJvcHMubmFtZV0sXG4gICAgICBjdXJyZW50VmFsdWVzLmNvbmNhdCh7XG4gICAgICAgIGlkOiBzdWdnZXN0aW9uLmtleSxcbiAgICAgICAgZGlzcGxheU5hbWU6IHN1Z2dlc3Rpb24udmFsdWUsXG4gICAgICAgIGFjY2VwdGVkOiB0cnVlXG4gICAgICB9KVxuICAgICk7XG4gIH1cblxuICBvblF1ZXJ5Q2hhbmdlKGV2KSB7XG4gICAgY29uc3QgeyBnZXRBdXRvY29tcGxldGVWYWx1ZXMsIHBhdGggfSA9IHRoaXMucHJvcHM7XG4gICAgdGhpcy5zZXRTdGF0ZSh7cXVlcnk6IGV2LnRhcmdldC52YWx1ZX0pO1xuICAgIGlmIChldi50YXJnZXQudmFsdWUgPT09IFwiXCIpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXX0pO1xuICAgIH0gZWxzZSB7XG4gICAgICBnZXRBdXRvY29tcGxldGVWYWx1ZXMocGF0aCwgZXYudGFyZ2V0LnZhbHVlLCAocmVzdWx0cykgPT4ge1xuICAgICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogcmVzdWx0c30pO1xuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgb25RdWVyeUNsZWFyKGV2KSB7XG4gICAgaWYgKCF0aGlzLnN0YXRlLmJsdXJJc0Jsb2NrZWQpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXSwgcXVlcnk6IFwiXCJ9KTtcbiAgICB9XG4gIH1cblxuICBvbkJsdXJCbG9jayh0b2dnbGUpIHtcbiAgICB0aGlzLnNldFN0YXRlKHtibHVySXNCbG9ja2VkOiB0b2dnbGV9KTtcbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UsIHRhcmdldENvbGxlY3Rpb24gfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgdmFsdWVzID0gZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdW3RoaXMucHJvcHMubmFtZV0gfHwgW107XG4gICAgY29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLmZpbHRlcigodmFsKSA9PiB2YWwuYWNjZXB0ZWQpLm1hcCgodmFsdWUsIGkpID0+IChcbiAgICAgIDxkaXYga2V5PXtgJHtpfS0ke3ZhbHVlLmlkfWB9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuICAgICAgICA8TGluayB0bz17dXJscy5lbnRpdHkodGFyZ2V0Q29sbGVjdGlvbiwgdmFsdWUuaWQpfSA+e3ZhbHVlLmRpc3BsYXlOYW1lfTwvTGluaz5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcbiAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgIDwvZGl2PlxuICAgICkpO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIDxoND57Y2FtZWwybGFiZWwobmFtZSl9PC9oND5cbiAgICAgICAge2l0ZW1FbGVtZW50c31cbiAgICAgICAgPGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG4gICAgICAgICAgICAgICBvbkJsdXI9e3RoaXMub25RdWVyeUNsZWFyLmJpbmQodGhpcyl9XG4gICAgICAgICAgICAgICBvbkNoYW5nZT17dGhpcy5vblF1ZXJ5Q2hhbmdlLmJpbmQodGhpcyl9XG4gICAgICAgICAgICAgICB2YWx1ZT17dGhpcy5zdGF0ZS5xdWVyeX0gcGxhY2Vob2xkZXI9XCJTZWFyY2guLi5cIiAvPlxuXG4gICAgICAgIDxkaXYgb25Nb3VzZU92ZXI9eygpID0+IHRoaXMub25CbHVyQmxvY2sodHJ1ZSl9XG4gICAgICAgICAgICAgb25Nb3VzZU91dD17KCkgPT4gdGhpcy5vbkJsdXJCbG9jayhmYWxzZSl9XG4gICAgICAgICAgICAgc3R5bGU9e3tvdmVyZmxvd1k6IFwiYXV0b1wiLCBtYXhIZWlnaHQ6IFwiMzAwcHhcIn19PlxuICAgICAgICAgIHt0aGlzLnN0YXRlLnN1Z2dlc3Rpb25zLm1hcCgoc3VnZ2VzdGlvbiwgaSkgPT4gKFxuICAgICAgICAgICAgPGEga2V5PXtgJHtpfS0ke3N1Z2dlc3Rpb24ua2V5fWB9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiXG4gICAgICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHRoaXMub25BZGQoc3VnZ2VzdGlvbil9PlxuICAgICAgICAgICAgICB7c3VnZ2VzdGlvbi52YWx1ZX1cbiAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IFJlbGF0aW9uRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vLi4vLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudCA9IGVudGl0eS5kYXRhW25hbWVdICYmIGVudGl0eS5kYXRhW25hbWVdLmxlbmd0aCA+IDAgPyAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPntlbnRpdHkuZGF0YVtuYW1lXX08L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiBvbkNoYW5nZShbbmFtZV0sIFwiXCIpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSA6IG51bGw7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnR9XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uQ2hhbmdlKFtuYW1lXSwgdmFsdWUpfVxuXHRcdFx0XHRcdG5vQ2xlYXI9e3RydWV9IGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cblx0XHRcdFx0XHQ8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIj5cblx0XHRcdFx0XHRcdFNlbGVjdCB7bGFiZWwudG9Mb3dlckNhc2UoKX1cblx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdFx0e29wdGlvbnMubWFwKChvcHRpb24pID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9TZWxlY3RGaWVsZD5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5XG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBTdHJpbmdGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdDxpbnB1dCBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIlxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IG9uQ2hhbmdlKFtuYW1lXSwgZXYudGFyZ2V0LnZhbHVlKX1cblx0XHRcdFx0XHR2YWx1ZT17ZW50aXR5LmRhdGFbbmFtZV0gfHwgXCJcIn1cblx0XHRcdFx0XHRwbGFjZWhvbGRlcj17YEVudGVyICR7bGFiZWwudG9Mb3dlckNhc2UoKX1gfVxuXHRcdFx0XHQvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5TdHJpbmdGaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTdHJpbmdGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIlxuXG5pbXBvcnQgU3RyaW5nRmllbGQgZnJvbSBcIi4vZmllbGRzL3N0cmluZy1maWVsZFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9zZWxlY3RcIjtcbmltcG9ydCBNdWx0aVNlbGVjdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9tdWx0aS1zZWxlY3RcIjtcbmltcG9ydCBSZWxhdGlvbkZpZWxkIGZyb20gXCIuL2ZpZWxkcy9yZWxhdGlvblwiO1xuaW1wb3J0IFN0cmluZ0xpc3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvbGlzdC1vZi1zdHJpbmdzXCI7XG5pbXBvcnQgTGlua0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9saW5rc1wiO1xuaW1wb3J0IE5hbWVzRmllbGQgZnJvbSBcIi4vZmllbGRzL25hbWVzXCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHsgdXJscyB9IGZyb20gXCIuLi8uLi8uLi91cmxzXCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vZmllbGRzL2NhbWVsMmxhYmVsXCI7XG5cbmNvbnN0IGZpZWxkTWFwID0ge1xuXHRcInN0cmluZ1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcInRleHRcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJkYXRhYmxlXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwibXVsdGlzZWxlY3RcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxNdWx0aVNlbGVjdEZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gb3B0aW9ucz17ZmllbGREZWYub3B0aW9uc30gLz4pLFxuXHRcInNlbGVjdFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFNlbGVjdEZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gb3B0aW9ucz17ZmllbGREZWYub3B0aW9uc30gLz4pLFxuXHRcInJlbGF0aW9uXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8UmVsYXRpb25GaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IHRhcmdldENvbGxlY3Rpb249e2ZpZWxkRGVmLnJlbGF0aW9uLnRhcmdldENvbGxlY3Rpb259IHBhdGg9e2ZpZWxkRGVmLnF1aWNrc2VhcmNofSAvPiksXG4gIFwibGlzdC1vZi1zdHJpbmdzXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nTGlzdEZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuICBcImxpbmtzXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8TGlua0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcIm5hbWVzXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8TmFtZXNGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KVxufTtcblxuY29uc3QgYXBwbHlGaWx0ZXIgPSAoZmllbGQsIGZpbHRlcikgPT5cbiAgICBmaWVsZC50b0xvd2VyQ2FzZSgpLmluZGV4T2YoZmlsdGVyLnRvTG93ZXJDYXNlKCkpID4gLTEgfHxcbiAgICBjYW1lbDJsYWJlbChmaWVsZCkudG9Mb3dlckNhc2UoKS5pbmRleE9mKGZpbHRlci50b0xvd2VyQ2FzZSgpKSA+IC0xO1xuXG5jbGFzcyBFbnRpdHlGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBjb25zdHJ1Y3Rvcihwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBmaWVsZHNUb0FkZDogW10sXG4gICAgICBhZGRGaWVsZEZpbHRlcjogXCJcIlxuICAgIH1cbiAgfVxuXG4gIG9uRmlsdGVyQ2hhbmdlKGV2KSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7YWRkRmllbGRGaWx0ZXI6IGV2LnRhcmdldC52YWx1ZX0sICgpID0+IHtcbiAgICAgIGNvbnN0IGZpbHRlcmVkID0gdGhpcy5nZXRBZGRhYmxlRmllbGRzRnJvbVByb3BlcnRpZXMoKS5maWx0ZXIocHJvcCA9PiBhcHBseUZpbHRlcihwcm9wLm5hbWUsIHRoaXMuc3RhdGUuYWRkRmllbGRGaWx0ZXIpKTtcbiAgICAgIGlmIChmaWx0ZXJlZC5sZW5ndGggPiAwKSB7XG4gICAgICAgIGlmICh0aGlzLnN0YXRlLmFkZEZpZWxkRmlsdGVyID09PSBcIlwiKSB7XG4gICAgICAgICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IFtdfSlcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB0aGlzLnNldFN0YXRlKHtmaWVsZHNUb0FkZDogW2ZpbHRlcmVkWzBdLm5hbWVdfSlcbiAgICAgICAgfVxuICAgICAgfVxuICAgIH0pO1xuICB9XG5cbiAgb25GaWx0ZXJLZXlEb3duKGV2KSB7XG4gICAgaWYgKGV2LmtleSA9PT0gXCJFbnRlclwiICYmIHRoaXMuc3RhdGUuZmllbGRzVG9BZGQubGVuZ3RoID4gMCkge1xuICAgICAgdGhpcy5vbkFkZFNlbGVjdGVkRmllbGRzKCk7XG4gICAgfVxuICB9XG5cbiAgdG9nZ2xlRmllbGRUb0FkZChmaWVsZE5hbWUpIHtcbiAgICBpZiAodGhpcy5zdGF0ZS5maWVsZHNUb0FkZC5pbmRleE9mKGZpZWxkTmFtZSkgPiAtMSkge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IHRoaXMuc3RhdGUuZmllbGRzVG9BZGQuZmlsdGVyKChmQWRkKSA9PiBmQWRkICE9PSBmaWVsZE5hbWUpfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiB0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLmNvbmNhdChmaWVsZE5hbWUpfSk7XG4gICAgfVxuICB9XG5cbiAgb25BZGRTZWxlY3RlZEZpZWxkcygpIHtcbiAgICBjb25zdCB7IHByb3BlcnRpZXMgfSA9IHRoaXMucHJvcHM7XG5cbiAgICB0aGlzLnByb3BzLm9uQWRkU2VsZWN0ZWRGaWVsZHModGhpcy5zdGF0ZS5maWVsZHNUb0FkZC5tYXAoKGZBZGQpID0+ICh7XG4gICAgICBuYW1lOiBmQWRkLFxuICAgICAgdHlwZTogcHJvcGVydGllcy5maW5kKChwcm9wKSA9PiBwcm9wLm5hbWUgPT09IGZBZGQpLnR5cGVcbiAgICB9KSkpO1xuXG4gICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IFtdLCBhZGRGaWVsZEZpbHRlcjogXCJcIn0pO1xuICB9XG5cbiAgZ2V0QWRkYWJsZUZpZWxkc0Zyb21Qcm9wZXJ0aWVzKCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBwcm9wZXJ0aWVzIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgcmV0dXJuIHByb3BlcnRpZXNcbiAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiAhZW50aXR5LmRhdGEuaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkgJiYgIWVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXS5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi5uYW1lKSlcblxuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25EZWxldGUsIG9uQ2hhbmdlLCBnZXRBdXRvY29tcGxldGVWYWx1ZXMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBlbnRpdHksIGN1cnJlbnRNb2RlLCBwcm9wZXJ0aWVzLCBlbnRpdHlMYWJlbCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGZpZWxkc1RvQWRkLCBhZGRGaWVsZEZpbHRlciB9ID0gdGhpcy5zdGF0ZTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC04XCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAgPExpbmsgdG89e3VybHMubmV3RW50aXR5KGVudGl0eS5kb21haW4pfSBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnkgcHVsbC1yaWdodFwiPlxuICAgICAgICAgICAgTmV3IHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICA8L0xpbms+XG4gICAgICAgIDwvZGl2PlxuXG5cbiAgICAgICAge3Byb3BlcnRpZXNcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gIWZpZWxkTWFwLmhhc093blByb3BlcnR5KGZpZWxkRGVmLnR5cGUpKVxuICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PiAoPGRpdiBrZXk9e2l9IHN0eWxlPXt7XCJjb2xvclwiOiBcInJlZFwifX0+PHN0cm9uZz5GaWVsZCB0eXBlIG5vdCBzdXBwb3J0ZWQ6IHtmaWVsZERlZi50eXBlfTwvc3Ryb25nPjwvZGl2PikpfVxuXG4gICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+IGZpZWxkTWFwLmhhc093blByb3BlcnR5KGZpZWxkRGVmLnR5cGUpKVxuICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBlbnRpdHkuZGF0YS5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi5uYW1lKSB8fCBlbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl0uaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkpXG4gICAgICAgICAgLm1hcCgoZmllbGREZWYsIGkpID0+XG4gICAgICAgICAgZmllbGRNYXBbZmllbGREZWYudHlwZV0oZmllbGREZWYsIHtcblx0XHRcdFx0XHRcdGtleTogYCR7aX0tJHtmaWVsZERlZi5uYW1lfWAsXG5cdFx0XHRcdFx0XHRlbnRpdHk6IGVudGl0eSxcblx0XHRcdFx0XHRcdG9uQ2hhbmdlOiBvbkNoYW5nZSxcblx0XHRcdFx0XHRcdGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzXG5cdFx0XHRcdFx0fSlcbiAgICAgICAgKX1cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpbiBhZGQtZmllbGQtZm9ybVwiPlxuICAgICAgICAgIDxoND5BZGQgZmllbGRzPC9oND5cbiAgICAgICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIgdmFsdWU9e2FkZEZpZWxkRmlsdGVyfSBwbGFjZWhvbGRlcj1cIkZpbHRlci4uLlwiXG4gICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXt0aGlzLm9uRmlsdGVyQ2hhbmdlLmJpbmQodGhpcyl9XG4gICAgICAgICAgICAgICAgIG9uS2V5UHJlc3M9e3RoaXMub25GaWx0ZXJLZXlEb3duLmJpbmQodGhpcyl9XG4gICAgICAgICAgLz5cbiAgICAgICAgICA8ZGl2IHN0eWxlPXt7bWF4SGVpZ2h0OiBcIjI1MHB4XCIsIG92ZXJmbG93WTogXCJhdXRvXCJ9fT5cbiAgICAgICAgICAgIHt0aGlzLmdldEFkZGFibGVGaWVsZHNGcm9tUHJvcGVydGllcygpXG4gICAgICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBhcHBseUZpbHRlcihmaWVsZERlZi5uYW1lLCBhZGRGaWVsZEZpbHRlcikpXG4gICAgICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PiAoXG4gICAgICAgICAgICAgICAgPGRpdiBrZXk9e2l9IG9uQ2xpY2s9eygpID0+IHRoaXMudG9nZ2xlRmllbGRUb0FkZChmaWVsZERlZi5uYW1lKX1cbiAgICAgICAgICAgICAgICAgICAgIGNsYXNzTmFtZT17ZmllbGRzVG9BZGQuaW5kZXhPZihmaWVsZERlZi5uYW1lKSA+IC0xID8gXCJzZWxlY3RlZFwiIDogXCJcIn0+XG4gICAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0XCI+KHtmaWVsZERlZi50eXBlfSk8L3NwYW4+XG4gICAgICAgICAgICAgICAgICB7Y2FtZWwybGFiZWwoZmllbGREZWYubmFtZSl9XG4gICAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgICkpXG4gICAgICAgICAgICB9XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkU2VsZWN0ZWRGaWVsZHMuYmluZCh0aGlzKX0+QWRkIHNlbGVjdGVkIGZpZWxkczwvYnV0dG9uPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAge2N1cnJlbnRNb2RlID09PSBcImVkaXRcIlxuICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAgICAgIDxoND5EZWxldGU8L2g0PlxuICAgICAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGFuZ2VyXCIgb25DbGljaz17b25EZWxldGV9IGRpc2FibGVkPXshdGhpcy5wcm9wcy51c2VyfT5cbiAgICAgICAgICAgICAgICBEZWxldGUge2VudGl0eUxhYmVsfVxuICAgICAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVudGl0eUZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25TYXZlLCBvbkNhbmNlbCwgdXNlciB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2PlxuICAgICAgPGJ1dHRvbiBkaXNhYmxlZD17IXVzZXJ9IGNsYXNzTmFtZT1cImJ0biBidG4tcHJpbWFyeVwiIG9uQ2xpY2s9e29uU2F2ZX0+U2F2ZTwvYnV0dG9uPlxuICAgICAge1wiIFwifW9ye1wiIFwifVxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWxpbmtcIiBvbkNsaWNrPXtvbkNhbmNlbH0+Q2FuY2VsPC9idXR0b24+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHsgdXJscyB9IGZyb20gXCIuLi8uLi8uLi91cmxzXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgc3RhcnQsIGxpc3QsIGRvbWFpbiwgc2VsZWN0ZWRJZCwgZW50aXR5UGVuZGluZyB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cInJlc3VsdC1saXN0IHJlc3VsdC1saXN0LWVkaXRcIj5cbiAgICAgIDxvbCBzdGFydD17c3RhcnQgKyAxfSBzdHlsZT17e2NvdW50ZXJSZXNldDogYHN0ZXAtY291bnRlciAke3N0YXJ0fWB9fT5cbiAgICAgICAge2xpc3QubWFwKChlbnRyeSwgaSkgPT4gKFxuICAgICAgICAgIDxsaSBrZXk9e2Ake2l9LSR7ZW50cnkuX2lkfWB9PlxuICAgICAgICAgICAge2VudGl0eVBlbmRpbmdcbiAgICAgICAgICAgICAgPyAoXG4gICAgICAgICAgICAgICAgPGEgc3R5bGU9e3tcbiAgICAgICAgICAgICAgICAgIGRpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIHdpZHRoOiBcImNhbGMoMTAwJSAtIDMwcHgpXCIsIGhlaWdodDogXCIxMDAlXCIsIHBhZGRpbmc6IFwiMC41ZW0gMFwiLFxuICAgICAgICAgICAgICAgICAgY3Vyc29yOiBcImRlZmF1bHRcIiwgb3BhY2l0eTogXCIwLjVcIiwgdGV4dERlY29yYXRpb246IFwibm9uZVwiLCBmb250V2VpZ2h0OiBcIjMwMFwiXG4gICAgICAgICAgICAgICAgfX0+XG4gICAgICAgICAgICAgICAgICB7ZW50cnlbXCJAZGlzcGxheU5hbWVcIl19XG4gICAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgICApIDogKFxuICAgICAgICAgICAgICAgIDxMaW5rIHRvPXt1cmxzLmVudGl0eShkb21haW4sIGVudHJ5Ll9pZCl9IHN0eWxlPXt7XG4gICAgICAgICAgICAgICAgICBkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCB3aWR0aDogXCJjYWxjKDEwMCUgLSAzMHB4KVwiLCBoZWlnaHQ6IFwiMTAwJVwiLCBwYWRkaW5nOiBcIjAuNWVtIDBcIixcbiAgICAgICAgICAgICAgICAgIGZvbnRXZWlnaHQ6IHNlbGVjdGVkSWQgPT09IGVudHJ5Ll9pZCA/IFwiNTAwXCIgOiBcIjMwMFwiXG4gICAgICAgICAgICAgICAgfX0+XG5cbiAgICAgICAgICAgICAgICAgIHtlbnRyeVtcIkBkaXNwbGF5TmFtZVwiXX1cbiAgICAgICAgICAgICAgICA8L0xpbms+XG4gICAgICAgICAgICAgIClcbiAgICAgICAgICAgIH1cbiAgICAgICAgICA8L2xpPlxuICAgICAgICApKX1cbiAgICAgIDwvb2w+XG4gICAgPC9kaXY+XG4gIClcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBvblBhZ2luYXRlTGVmdCwgb25QYWdpbmF0ZVJpZ2h0IH0gPSBwcm9wcztcbiAgY29uc3QgeyBzdGFydCwgcm93cywgbGlzdExlbmd0aCB9ID0gcHJvcHM7XG5cblxuXG4gIHJldHVybiAoXG4gICAgPGRpdj5cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgZGlzYWJsZWQ9e3N0YXJ0ID09PSAwfSBvbkNsaWNrPXtvblBhZ2luYXRlTGVmdH0+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tY2hldnJvbi1sZWZ0XCIgLz5cbiAgICAgIDwvYnV0dG9uPlxuICAgICAge1wiIFwifXtzdGFydCArIDF9IC0ge3N0YXJ0ICsgcm93c317XCIgXCJ9XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIGRpc2FibGVkPXtsaXN0TGVuZ3RoIDwgcm93c30gb25DbGljaz17b25QYWdpbmF0ZVJpZ2h0fT5cbiAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1jaGV2cm9uLXJpZ2h0XCIgLz5cbiAgICAgIDwvYnV0dG9uPlxuICAgIDwvZGl2PlxuICApO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZSwgb25RdWlja1NlYXJjaCwgcXVlcnkgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cCBzbWFsbC1tYXJnaW4gXCI+XG4gICAgICA8aW5wdXQgdHlwZT1cInRleHRcIiBwbGFjZWhvbGRlcj1cIlNlYXJjaCBmb3IuLi5cIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIlxuICAgICAgICBvbkNoYW5nZT17KGV2KSA9PiBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UoZXYudGFyZ2V0LnZhbHVlKX1cbiAgICAgICAgb25LZXlQcmVzcz17KGV2KSA9PiBldi5rZXkgPT09IFwiRW50ZXJcIiA/IG9uUXVpY2tTZWFyY2goKSA6IGZhbHNlfVxuICAgICAgICB2YWx1ZT17cXVlcnl9XG4gICAgICAgIC8+XG4gICAgICA8c3BhbiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cC1idG5cIj5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXtvblF1aWNrU2VhcmNofT5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXNlYXJjaFwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmtcIiBvbkNsaWNrPXsoKSA9PiB7IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZShcIlwiKTsgb25RdWlja1NlYXJjaCgpOyB9fT5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuICAgICAgPC9zcGFuPlxuICAgIDwvZGl2PlxuICApO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5pbXBvcnQgTWVzc2FnZSBmcm9tIFwiLi4vLi4vbWVzc2FnZVwiO1xuXG5jb25zdCBMQUJFTFMgPSB7XG5cdFwiU1VDQ0VTU19NRVNTQUdFXCI6IFwiXCIsXG5cdFwiRVJST1JfTUVTU0FHRVwiOiAoXG5cdFx0PHNwYW4+XG5cdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWV4Y2xhbWF0aW9uLXNpZ25cIiAvPiBXYXJuaW5nIVxuXHRcdDwvc3Bhbj5cblx0KVxufTtcblxuY29uc3QgQUxFUlRfTEVWRUxTID0ge1xuXHRcIlNVQ0NFU1NfTUVTU0FHRVwiOiBcImluZm9cIixcblx0XCJFUlJPUl9NRVNTQUdFXCI6IFwiZGFuZ2VyXCJcbn07XG5cbmNsYXNzIE1lc3NhZ2VzIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbWVzc2FnZXMsIHR5cGVzLCBvbkRpc21pc3NNZXNzYWdlIH0gPSB0aGlzLnByb3BzO1xuXG5cdFx0Y29uc3QgZmlsdGVyZWRNZXNzYWdlcyA9IG1lc3NhZ2VzLmxvZ1xuXHRcdFx0Lm1hcCgobXNnLCBpZHgpID0+ICh7bWVzc2FnZTogbXNnLm1lc3NhZ2UsIGluZGV4OiBpZHgsIHR5cGU6IG1zZy50eXBlLCBkaXNtaXNzZWQ6IG1zZy5kaXNtaXNzZWQgfSkpXG5cdFx0XHQuZmlsdGVyKChtc2cpID0+IHR5cGVzLmluZGV4T2YobXNnLnR5cGUpID4gLTEgJiYgIW1zZy5kaXNtaXNzZWQpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXY+XG5cdFx0XHRcdHtmaWx0ZXJlZE1lc3NhZ2VzLm1hcCgobXNnKSA9PiAoXG5cdFx0XHRcdFx0PE1lc3NhZ2Uga2V5PXttc2cuaW5kZXh9XG5cdFx0XHRcdFx0XHRkaXNtaXNzaWJsZT17dHJ1ZX1cblx0XHRcdFx0XHRcdGFsZXJ0TGV2ZWw9e0FMRVJUX0xFVkVMU1ttc2cudHlwZV19XG5cdFx0XHRcdFx0XHRvbkNsb3NlTWVzc2FnZT17KCkgPT4gb25EaXNtaXNzTWVzc2FnZShtc2cuaW5kZXgpfT5cblx0XHRcdFx0XHRcdDxzdHJvbmc+e0xBQkVMU1ttc2cudHlwZV19PC9zdHJvbmc+IDxzcGFuPnttc2cubWVzc2FnZX08L3NwYW4+XG5cdFx0XHRcdFx0PC9NZXNzYWdlPlxuXHRcdFx0XHQpKX1cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuTWVzc2FnZXMucHJvcFR5cGVzID0ge1xuXHRtZXNzYWdlczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0b25EaXNtaXNzTWVzc2FnZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMuaXNSZXF1aXJlZCxcblx0dHlwZXM6IFJlYWN0LlByb3BUeXBlcy5hcnJheS5pc1JlcXVpcmVkXG59O1xuXG5leHBvcnQgZGVmYXVsdCBNZXNzYWdlcztcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBSZWFjdERPTSBmcm9tIFwicmVhY3QtZG9tXCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuY2xhc3MgU2VsZWN0RmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3Rvcihwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBpc09wZW46IGZhbHNlXG4gICAgfTtcbiAgICB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciA9IHRoaXMuaGFuZGxlRG9jdW1lbnRDbGljay5iaW5kKHRoaXMpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQoKSB7XG4gICAgZG9jdW1lbnQuYWRkRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG4gIH1cblxuICBjb21wb25lbnRXaWxsVW5tb3VudCgpIHtcbiAgICBkb2N1bWVudC5yZW1vdmVFdmVudExpc3RlbmVyKFwiY2xpY2tcIiwgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIsIGZhbHNlKTtcbiAgfVxuXG4gIHRvZ2dsZVNlbGVjdCgpIHtcbiAgICBpZih0aGlzLnN0YXRlLmlzT3Blbikge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiBmYWxzZX0pO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtpc09wZW46IHRydWV9KTtcbiAgICB9XG4gIH1cblxuICBoYW5kbGVEb2N1bWVudENsaWNrKGV2KSB7XG4gICAgY29uc3QgeyBpc09wZW4gfSA9IHRoaXMuc3RhdGU7XG4gICAgaWYgKGlzT3BlbiAmJiAhUmVhY3RET00uZmluZERPTU5vZGUodGhpcykuY29udGFpbnMoZXYudGFyZ2V0KSkge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7XG4gICAgICAgIGlzT3BlbjogZmFsc2VcbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uQ2hhbmdlLCBvbkNsZWFyLCB2YWx1ZSwgYnRuQ2xhc3MsIG5vQ2xlYXIgfSA9IHRoaXMucHJvcHM7XG5cbiAgICBjb25zdCBzZWxlY3RlZE9wdGlvbiA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSA9PT0gdmFsdWUpO1xuICAgIGNvbnN0IHBsYWNlaG9sZGVyID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnR5cGUgPT09IFwicGxhY2Vob2xkZXJcIik7XG4gICAgY29uc3Qgb3RoZXJPcHRpb25zID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnZhbHVlICYmIG9wdC5wcm9wcy52YWx1ZSAhPT0gdmFsdWUpO1xuXG4gICAgcmV0dXJuIChcblxuICAgICAgPGRpdiBjbGFzc05hbWU9e2N4KFwiZHJvcGRvd25cIiwge29wZW46IHRoaXMuc3RhdGUuaXNPcGVufSl9PlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT17Y3goXCJidG5cIiwgXCJkcm9wZG93bi10b2dnbGVcIiwgYnRuQ2xhc3MgfHwgXCJidG4tYmxhbmtcIil9IG9uQ2xpY2s9e3RoaXMudG9nZ2xlU2VsZWN0LmJpbmQodGhpcyl9PlxuICAgICAgICAgIHtzZWxlY3RlZE9wdGlvbi5sZW5ndGggPyBzZWxlY3RlZE9wdGlvbiA6IHBsYWNlaG9sZGVyfSA8c3BhbiBjbGFzc05hbWU9XCJjYXJldFwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuXG4gICAgICAgIDx1bCBjbGFzc05hbWU9XCJkcm9wZG93bi1tZW51XCI+XG4gICAgICAgICAgeyB2YWx1ZSAmJiAhbm9DbGVhciA/IChcbiAgICAgICAgICAgIDxsaT5cbiAgICAgICAgICAgICAgPGEgb25DbGljaz17KCkgPT4geyBvbkNsZWFyKCk7IHRoaXMudG9nZ2xlU2VsZWN0KCk7fX0+XG4gICAgICAgICAgICAgICAgLSBjbGVhciAtXG4gICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSA6IG51bGx9XG4gICAgICAgICAge290aGVyT3B0aW9ucy5tYXAoKG9wdGlvbiwgaSkgPT4gKFxuICAgICAgICAgICAgPGxpIGtleT17aX0+XG4gICAgICAgICAgICAgIDxhIHN0eWxlPXt7Y3Vyc29yOiBcInBvaW50ZXJcIn19IG9uQ2xpY2s9eygpID0+IHsgb25DaGFuZ2Uob3B0aW9uLnByb3BzLnZhbHVlKTsgdGhpcy50b2dnbGVTZWxlY3QoKTsgfX0+e29wdGlvbn08L2E+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufVxuXG5TZWxlY3RGaWVsZC5wcm9wVHlwZXMgPSB7XG4gIG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgb25DbGVhcjogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIHZhbHVlOiBSZWFjdC5Qcm9wVHlwZXMuYW55LFxuICBidG5DbGFzczogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcbiAgbm9DbGVhcjogUmVhY3QuUHJvcFR5cGVzLmJvb2xcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFNlbGVjdEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5mdW5jdGlvbiBGb290ZXIocHJvcHMpIHtcbiAgY29uc3QgaGlMb2dvID0gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgY29sLW1kLTFcIj5cbiAgICAgIDxpbWcgY2xhc3NOYW1lPVwiaGktbG9nb1wiIHNyYz1cImltYWdlcy9sb2dvLWh1eWdlbnMtaW5nLnN2Z1wiIC8+XG4gICAgPC9kaXY+XG4gICk7XG5cbiAgY29uc3QgY2xhcmlhaExvZ28gPSAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMSBjb2wtbWQtMVwiPlxuICAgICAgPGltZyBjbGFzc05hbWU9XCJsb2dvXCIgc3JjPVwiaW1hZ2VzL2xvZ28tY2xhcmlhaC5zdmdcIiAvPlxuICAgIDwvZGl2PlxuICApO1xuXG4gIGNvbnN0IGZvb3RlckJvZHkgPSBSZWFjdC5DaGlsZHJlbi5jb3VudChwcm9wcy5jaGlsZHJlbikgPiAwID9cbiAgICBSZWFjdC5DaGlsZHJlbi5tYXAocHJvcHMuY2hpbGRyZW4sIChjaGlsZCwgaSkgPT4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJ3aGl0ZS1iYXJcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICB7aSA9PT0gUmVhY3QuQ2hpbGRyZW4uY291bnQocHJvcHMuY2hpbGRyZW4pIC0gMVxuICAgICAgICAgICAgPyAoPGRpdiBjbGFzc05hbWU9XCJyb3dcIj57aGlMb2dvfTxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEwIGNvbC1tZC0xMCB0ZXh0LWNlbnRlclwiPntjaGlsZH08L2Rpdj57Y2xhcmlhaExvZ299PC9kaXY+KVxuICAgICAgICAgICAgOiAoPGRpdiBjbGFzc05hbWU9XCJyb3dcIj57Y2hpbGR9PC9kaXY+KVxuICAgICAgICAgIH1cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApKSA6IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwid2hpdGUtYmFyXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJyb3dcIj5cbiAgICAgICAgICAgIHtoaUxvZ299XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMCBjb2wtbWQtMTAgdGV4dC1jZW50ZXJcIj5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAge2NsYXJpYWhMb2dvfVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG5cblxuICByZXR1cm4gKFxuICAgIDxmb290ZXIgY2xhc3NOYW1lPVwiZm9vdGVyXCI+XG4gICAgICB7Zm9vdGVyQm9keX1cbiAgICA8L2Zvb3Rlcj5cbiAgKVxufVxuXG5leHBvcnQgZGVmYXVsdCBGb290ZXI7IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgZGlzbWlzc2libGUsIGFsZXJ0TGV2ZWwsIG9uQ2xvc2VNZXNzYWdlfSA9IHByb3BzO1xuICBjb25zdCBkaXNtaXNzQnV0dG9uID0gZGlzbWlzc2libGVcbiAgICA/IDxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzTmFtZT1cImNsb3NlXCIgb25DbGljaz17b25DbG9zZU1lc3NhZ2V9PjxzcGFuPiZ0aW1lczs8L3NwYW4+PC9idXR0b24+XG4gICAgOiBudWxsO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9e2N4KFwiYWxlcnRcIiwgYGFsZXJ0LSR7YWxlcnRMZXZlbH1gLCB7XCJhbGVydC1kaXNtaXNzaWJsZVwiOiBkaXNtaXNzaWJsZX0pfSByb2xlPVwiYWxlcnRcIj5cbiAgICAgIHtkaXNtaXNzQnV0dG9ufVxuICAgICAge3Byb3BzLmNoaWxkcmVufVxuICAgIDwvZGl2PlxuICApXG59OyIsImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgRm9vdGVyIGZyb20gXCIuL2Zvb3RlclwiO1xuXG5jb25zdCBGT09URVJfSEVJR0hUID0gODE7XG5cbmZ1bmN0aW9uIFBhZ2UocHJvcHMpIHtcbiAgY29uc3QgZm9vdGVycyA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkocHJvcHMuY2hpbGRyZW4pLmZpbHRlcigoY2hpbGQpID0+IGNoaWxkLnByb3BzLnR5cGUgPT09IFwiZm9vdGVyLWJvZHlcIik7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cInBhZ2VcIj5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwic21hbGwtbWFyZ2luIGhpLUdyZWVuIGNvbnRhaW5lci1mbHVpZFwiPlxuICAgICAgICA8bmF2IGNsYXNzTmFtZT1cIm5hdmJhciBcIj5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJuYXZiYXItaGVhZGVyXCI+IDxhIGNsYXNzTmFtZT1cIm5hdmJhci1icmFuZFwiIGhyZWY9XCIjXCI+PGltZyBzcmM9XCJpbWFnZXMvbG9nby10aW1idWN0b28uc3ZnXCIgY2xhc3NOYW1lPVwibG9nb1wiIGFsdD1cInRpbWJ1Y3Rvb1wiLz48L2E+IDwvZGl2PlxuICAgICAgICAgICAgPGRpdiBpZD1cIm5hdmJhclwiIGNsYXNzTmFtZT1cIm5hdmJhci1jb2xsYXBzZSBjb2xsYXBzZVwiPlxuICAgICAgICAgICAgICA8dWwgY2xhc3NOYW1lPVwibmF2IG5hdmJhci1uYXYgbmF2YmFyLXJpZ2h0XCI+XG4gICAgICAgICAgICAgICAge3Byb3BzLnVzZXJuYW1lID8gPGxpPjxhIGhyZWY9e3Byb3BzLnVzZXJsb2NhdGlvbiB8fCAnIyd9PjxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tdXNlclwiLz4ge3Byb3BzLnVzZXJuYW1lfTwvYT48L2xpPiA6IG51bGx9XG4gICAgICAgICAgICAgIDwvdWw+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9uYXY+XG4gICAgICA8L2Rpdj5cbiAgICAgIDxkaXYgIHN0eWxlPXt7bWFyZ2luQm90dG9tOiBgJHtGT09URVJfSEVJR0hUICogZm9vdGVycy5sZW5ndGh9cHhgfX0+XG4gICAgICAgIHtSZWFjdC5DaGlsZHJlbi50b0FycmF5KHByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKGNoaWxkKSA9PiBjaGlsZC5wcm9wcy50eXBlICE9PSBcImZvb3Rlci1ib2R5XCIpfVxuICAgICAgPC9kaXY+XG4gICAgICA8Rm9vdGVyPlxuICAgICAgICB7Zm9vdGVyc31cbiAgICAgIDwvRm9vdGVyPlxuICAgIDwvZGl2PlxuICApO1xufVxuXG5leHBvcnQgZGVmYXVsdCBQYWdlO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi9zdG9yZVwiO1xuaW1wb3J0IHtzZXRWcmV9IGZyb20gXCIuL2FjdGlvbnMvdnJlXCI7XG5cbmltcG9ydCByb3V0ZXIgZnJvbSBcIi4vcm91dGVyXCI7XG5pbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcblxuY29uc3Qgc2V0VXNlciA9ICh1c2VyKSA9PiB7XG5cdC8vIFRPRE86IHZhbGlkYXRlIHVzZXIgc2Vzc2lvbi5cblx0aWYgKHVzZXIpIHtcblx0XHR4aHIoe1xuXHRcdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvc3lzdGVtL3VzZXJzL21lL3ZyZXNgLFxuXHRcdFx0aGVhZGVyczoge1xuXHRcdFx0XHQnQXV0aG9yaXphdGlvbic6IHVzZXIudG9rZW5cblx0XHRcdH1cblx0XHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0XHRpZiAoZXJyIHx8IHJlc3Auc3RhdHVzQ29kZSA+PSAzMDApIHtcblx0XHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiU0VTU0lPTl9FWFBJUkVEXCJ9KTtcblx0XHRcdH0gZWxzZSB7XG5cdFx0XHRcdGNvbnN0IGRhdGEgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0XHRcdGlmICghZGF0YS5taW5lIHx8IE9iamVjdC5rZXlzKGRhdGEubWluZSkuaW5kZXhPZihnZXRWcmVJZCgpKSA8IDApIHtcblx0XHRcdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJFUlJPUl9NRVNTQUdFXCIsIG1lc3NhZ2U6IFwiWW91IGFyZSBub3QgYWxsb3dlZCB0byBlZGl0IHRoaXMgdnJlXCJ9KTtcblx0XHRcdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJTRVNTSU9OX0VYUElSRURcIn0pO1xuXHRcdFx0XHR9XG5cdFx0XHR9XG5cdFx0fSk7XG5cblx0XHR4aHIoe1xuXHRcdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvc3lzdGVtL3VzZXJzL21lYCxcblx0XHRcdGhlYWRlcnM6IHtcblx0XHRcdFx0J0F1dGhvcml6YXRpb24nOiB1c2VyLnRva2VuXG5cdFx0XHR9XG5cdFx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdFx0dHJ5IHtcblx0XHRcdFx0Y29uc3QgdXNlckRhdGEgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0XHRcdHN0b3JlLmRpc3BhdGNoKHt0eXBlOiBcIlNFVF9VU0VSX0RBVEFcIiwgdXNlckRhdGE6IHVzZXJEYXRhfSk7XG5cdFx0XHR9IGNhdGNoIChlKSB7XG5cdFx0XHRcdGNvbnNvbGUud2FybihlKTtcblx0XHRcdH1cblx0XHR9KTtcblx0fVxuXG5cdHJldHVybiB7XG5cdFx0dHlwZTogXCJTRVRfVVNFUlwiLFxuXHRcdHVzZXI6IHVzZXJcblx0fTtcbn07XG5cbmZ1bmN0aW9uIGdldFZyZUlkKCkge1xuXHRsZXQgcGF0aCA9IHdpbmRvdy5sb2NhdGlvbi5zZWFyY2guc3Vic3RyKDEpO1xuXHRsZXQgcGFyYW1zID0gcGF0aC5zcGxpdChcIiZcIik7XG5cblx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoXCI9XCIpO1xuXHRcdGlmKGtleSA9PT0gXCJ2cmVJZFwiKSB7XG5cdFx0XHRyZXR1cm4gdmFsdWU7XG5cdFx0fVxuXHR9XG5cdHJldHVybiBcIldvbWVuV3JpdGVyc1wiO1xufVxuXG5kb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiRE9NQ29udGVudExvYWRlZFwiLCAoKSA9PiB7XG5cblx0ZnVuY3Rpb24gaW5pdFJvdXRlcigpIHtcblx0XHRSZWFjdERPTS5yZW5kZXIocm91dGVyLCBkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKSk7XG5cdH1cblxuXG5cblx0ZnVuY3Rpb24gZ2V0TG9naW4oKSB7XG5cdFx0bGV0IHBhdGggPSB3aW5kb3cubG9jYXRpb24uc2VhcmNoLnN1YnN0cigxKTtcblx0XHRsZXQgcGFyYW1zID0gcGF0aC5zcGxpdChcIiZcIik7XG5cblx0XHRmb3IobGV0IGkgaW4gcGFyYW1zKSB7XG5cdFx0XHRsZXQgW2tleSwgdmFsdWVdID0gcGFyYW1zW2ldLnNwbGl0KFwiPVwiKTtcblx0XHRcdGlmKGtleSA9PT0gXCJoc2lkXCIpIHtcblx0XHRcdFx0bG9jYWxTdG9yYWdlLnNldEl0ZW0oXCJ0b2tlblwiLCBKU09OLnN0cmluZ2lmeSh7dXNlcjogdmFsdWUsIHRva2VuOiB2YWx1ZX0pKTtcblx0XHRcdFx0bG9jYXRpb24uaHJlZiA9IHdpbmRvdy5sb2NhdGlvbi5ocmVmLnJlcGxhY2UoXCJoc2lkPVwiICsgdmFsdWUsIFwiXCIpO1xuXHRcdFx0XHRyZXR1cm47XG5cdFx0XHR9XG5cdFx0fVxuXHRcdHJldHVybiBKU09OLnBhcnNlKGxvY2FsU3RvcmFnZS5nZXRJdGVtKFwidG9rZW5cIikgfHwgXCJudWxsXCIpO1xuXHR9XG5cblx0c3RvcmUuZGlzcGF0Y2goc2V0VnJlKGdldFZyZUlkKCksIGluaXRSb3V0ZXIpKTtcblx0c3RvcmUuZGlzcGF0Y2goc2V0VXNlcihnZXRMb2dpbigpKSk7XG59KTsiLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5cbmxldCBpbml0aWFsU3RhdGUgPSB7XG5cdGRhdGE6IHtcblx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0fSxcblx0ZG9tYWluOiBudWxsLFxuXHRlcnJvck1lc3NhZ2U6IG51bGwsXG5cdHBlbmRpbmc6IGZhbHNlXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cblx0XHRjYXNlIFwiQkVGT1JFX0ZFVENIX0VOVElUWVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiB7XG5cdFx0XHRcdFx0XCJAcmVsYXRpb25zXCI6IFtdXG5cdFx0XHRcdH0sXG5cdFx0XHRcdHBlbmRpbmc6IHRydWVcblx0XHRcdH19O1xuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiBhY3Rpb24uZGF0YSxcblx0XHRcdFx0ZG9tYWluOiBhY3Rpb24uZG9tYWluLFxuXHRcdFx0XHRlcnJvck1lc3NhZ2U6IGFjdGlvbi5lcnJvck1lc3NhZ2UgfHwgbnVsbCxcblx0XHRcdFx0cGVuZGluZzogZmFsc2Vcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YTogc2V0SW4oYWN0aW9uLmZpZWxkUGF0aCwgYWN0aW9uLnZhbHVlLCBzdGF0ZS5kYXRhKVxuXHRcdFx0fX07XG5cblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlfRkFJTFVSRVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiB7XG5cdFx0XHRcdFx0XCJAcmVsYXRpb25zXCI6IFtdXG5cdFx0XHRcdH0sXG5cdFx0XHRcdGVycm9yTWVzc2FnZTogYWN0aW9uLmVycm9yTWVzc2FnZSxcblx0XHRcdFx0cGVuZGluZzogZmFsc2Vcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlNFVF9WUkVcIjoge1xuXHRcdFx0cmV0dXJuIGluaXRpYWxTdGF0ZTtcblx0XHR9XG5cblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn0iLCJpbXBvcnQge2NvbWJpbmVSZWR1Y2Vyc30gZnJvbSBcInJlZHV4XCI7XG5cbmltcG9ydCBlbnRpdHkgZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQgbWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXNcIjtcbmltcG9ydCB1c2VyIGZyb20gXCIuL3VzZXJcIjtcbmltcG9ydCB2cmUgZnJvbSBcIi4vdnJlXCI7XG5pbXBvcnQgcXVpY2tTZWFyY2ggZnJvbSBcIi4vcXVpY2stc2VhcmNoXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGNvbWJpbmVSZWR1Y2Vycyh7XG5cdHZyZTogdnJlLFxuXHRlbnRpdHk6IGVudGl0eSxcblx0dXNlcjogdXNlcixcblx0bWVzc2FnZXM6IG1lc3NhZ2VzLFxuXHRxdWlja1NlYXJjaDogcXVpY2tTZWFyY2hcbn0pOyIsImltcG9ydCBzZXRJbiBmcm9tIFwiLi4vdXRpbC9zZXQtaW5cIjtcblxuY29uc3QgaW5pdGlhbFN0YXRlID0ge1xuXHRsb2c6IFtdXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlJFUVVFU1RfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJTVUNDRVNTX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiRVJST1JfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJESVNNSVNTX01FU1NBR0VcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRsb2c6IHNldEluKFthY3Rpb24ubWVzc2FnZUluZGV4LCBcImRpc21pc3NlZFwiXSwgdHJ1ZSwgc3RhdGUubG9nKVxuXHRcdFx0fTtcblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0ge1xuXHRzdGFydDogMCxcblx0bGlzdDogW10sXG5cdHJvd3M6IDUwLFxuXHRxdWVyeTogXCJcIlxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgc3RhcnQ6IGFjdGlvbi5zdGFydH07XG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0bGlzdDogYWN0aW9uLmRhdGFcblx0XHRcdH19O1xuXHRcdGNhc2UgXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIjoge1xuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRxdWVyeTogYWN0aW9uLnZhbHVlXG5cdFx0XHR9fTtcblx0XHR9XG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImxldCBpbml0aWFsU3RhdGUgPSBudWxsO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9VU0VSXCI6XG5cdFx0XHRpZiAoYWN0aW9uLnVzZXIpIHtcblx0XHRcdFx0cmV0dXJuIGFjdGlvbi51c2VyO1xuXHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdFx0fVxuXHRcdFx0YnJlYWs7XG5cdFx0Y2FzZSBcIlNFU1NJT05fRVhQSVJFRFwiOlxuXHRcdFx0cmV0dXJuIG51bGw7XG5cdFx0Y2FzZSBcIlNFVF9VU0VSX0RBVEFcIjpcblx0XHRcdHJldHVybiBzdGF0ZVxuXHRcdFx0XHQ/IHsuLi5zdGF0ZSwgdXNlckRhdGE6IGFjdGlvbi51c2VyRGF0YSB9XG5cdFx0XHRcdDogbnVsbDtcblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0dnJlSWQ6IG51bGwsXG5cdGxpc3Q6IFtdLFxuXHRjb2xsZWN0aW9uczoge30sXG5cdGRvbWFpbjogbnVsbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfVlJFXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0dnJlSWQ6IGFjdGlvbi52cmVJZCxcblx0XHRcdFx0Y29sbGVjdGlvbnM6IGFjdGlvbi5jb2xsZWN0aW9ucyB8fCBudWxsLFxuXHRcdFx0XHRsaXN0OiBhY3Rpb24ubGlzdCB8fCBzdGF0ZS5saXN0XG5cdFx0XHR9O1xuXG5cdFx0Y2FzZSBcIkxJU1RfVlJFU1wiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5saXN0LFxuXHRcdFx0XHRjb2xsZWN0aW9uczogbnVsbFxuXHRcdFx0fTtcblx0XHRjYXNlIFwiU0VUX0RPTUFJTlwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGRvbWFpbjogYWN0aW9uLmRvbWFpblxuXHRcdFx0fTtcblxuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQge1JvdXRlciwgUmVkaXJlY3QsIFJvdXRlLCBoYXNoSGlzdG9yeX0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHtQcm92aWRlciwgY29ubmVjdH0gZnJvbSBcInJlYWN0LXJlZHV4XCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCBnZXRBdXRvY29tcGxldGVWYWx1ZXMgZnJvbSBcIi4vYWN0aW9ucy9hdXRvY29tcGxldGVcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2FjdGlvbnNcIjtcblxuaW1wb3J0IEVkaXRHdWkgZnJvbSBcIi4vY29tcG9uZW50cy9lZGl0LWd1aS9lZGl0LWd1aVwiO1xuaW1wb3J0IHt1cmxzfSBmcm9tIFwiLi91cmxzXCI7XG5cbmV4cG9ydCBmdW5jdGlvbiBuYXZpZ2F0ZVRvKGtleSwgYXJncykge1xuXHRoYXNoSGlzdG9yeS5wdXNoKHVybHNba2V5XS5hcHBseShudWxsLCBhcmdzKSk7XG59XG5cbmNvbnN0IGRlZmF1bHRDb25uZWN0ID0gY29ubmVjdChcblx0c3RhdGUgPT4gKHsuLi5zdGF0ZSwgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzOiBnZXRBdXRvY29tcGxldGVWYWx1ZXN9KSxcblx0ZGlzcGF0Y2ggPT4gYWN0aW9ucyhuYXZpZ2F0ZVRvLCBkaXNwYXRjaClcbik7XG5cblxuZXhwb3J0IGRlZmF1bHQgKFxuXHQ8UHJvdmlkZXIgc3RvcmU9e3N0b3JlfT5cblx0XHQ8Um91dGVyIGhpc3Rvcnk9e2hhc2hIaXN0b3J5fT5cblx0XHRcdDxSb3V0ZSBwYXRoPXt1cmxzLnJvb3QoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5uZXdFbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5lbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0PC9Sb3V0ZXI+XG5cdDwvUHJvdmlkZXI+XG4pO1xuIiwiaW1wb3J0IHtjcmVhdGVTdG9yZSwgYXBwbHlNaWRkbGV3YXJlfSBmcm9tIFwicmVkdXhcIjtcbmltcG9ydCB0aHVua01pZGRsZXdhcmUgZnJvbSBcInJlZHV4LXRodW5rXCI7XG5cbmltcG9ydCByZWR1Y2VycyBmcm9tIFwiLi4vcmVkdWNlcnNcIjtcblxuY29uc3QgbG9nZ2VyID0gKCkgPT4gbmV4dCA9PiBhY3Rpb24gPT4ge1xuICBpZiAoYWN0aW9uLmhhc093blByb3BlcnR5KFwidHlwZVwiKSkge1xuICAgIGNvbnNvbGUubG9nKFwiW1JFRFVYXVwiLCBhY3Rpb24udHlwZSwgYWN0aW9uKTtcbiAgfVxuXG4gIHJldHVybiBuZXh0KGFjdGlvbik7XG59O1xuXG5sZXQgY3JlYXRlU3RvcmVXaXRoTWlkZGxld2FyZSA9IGFwcGx5TWlkZGxld2FyZSgvKmxvZ2dlciwqLyB0aHVua01pZGRsZXdhcmUpKGNyZWF0ZVN0b3JlKTtcbmV4cG9ydCBkZWZhdWx0IGNyZWF0ZVN0b3JlV2l0aE1pZGRsZXdhcmUocmVkdWNlcnMpO1xuIiwiY29uc3QgdXJscyA9IHtcblx0cm9vdCgpIHtcblx0XHRyZXR1cm4gXCIvXCI7XG5cdH0sXG5cdG5ld0VudGl0eShjb2xsZWN0aW9uKSB7XG5cdFx0cmV0dXJuIGNvbGxlY3Rpb25cblx0XHRcdD8gYC8ke2NvbGxlY3Rpb259L25ld2Bcblx0XHRcdDogXCIvOmNvbGxlY3Rpb24vbmV3XCI7XG5cdH0sXG5cdGVudGl0eShjb2xsZWN0aW9uLCBpZCkge1xuXHRcdHJldHVybiBjb2xsZWN0aW9uICYmIGlkXG5cdFx0XHQ/IGAvJHtjb2xsZWN0aW9ufS8ke2lkfWBcblx0XHRcdDogXCIvOmNvbGxlY3Rpb24vOmlkXCI7XG5cdH1cbn07XG5cbmV4cG9ydCB7IHVybHMgfSIsImZ1bmN0aW9uIGRlZXBDbG9uZTkob2JqKSB7XG4gICAgdmFyIGksIGxlbiwgcmV0O1xuXG4gICAgaWYgKHR5cGVvZiBvYmogIT09IFwib2JqZWN0XCIgfHwgb2JqID09PSBudWxsKSB7XG4gICAgICAgIHJldHVybiBvYmo7XG4gICAgfVxuXG4gICAgaWYgKEFycmF5LmlzQXJyYXkob2JqKSkge1xuICAgICAgICByZXQgPSBbXTtcbiAgICAgICAgbGVuID0gb2JqLmxlbmd0aDtcbiAgICAgICAgZm9yIChpID0gMDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgICAgICByZXQucHVzaCggKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXSApO1xuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcmV0ID0ge307XG4gICAgICAgIGZvciAoaSBpbiBvYmopIHtcbiAgICAgICAgICAgIGlmIChvYmouaGFzT3duUHJvcGVydHkoaSkpIHtcbiAgICAgICAgICAgICAgICByZXRbaV0gPSAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuICAgIHJldHVybiByZXQ7XG59XG5cbmV4cG9ydCBkZWZhdWx0IGRlZXBDbG9uZTk7IiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuLy8gRG8gZWl0aGVyIG9mIHRoZXNlOlxuLy8gIGEpIFNldCBhIHZhbHVlIGJ5IHJlZmVyZW5jZSBpZiBkZXJlZiBpcyBub3QgbnVsbFxuLy8gIGIpIFNldCBhIHZhbHVlIGRpcmVjdGx5IGluIHRvIGRhdGEgb2JqZWN0IGlmIGRlcmVmIGlzIG51bGxcbmNvbnN0IHNldEVpdGhlciA9IChkYXRhLCBkZXJlZiwga2V5LCB2YWwpID0+IHtcblx0KGRlcmVmIHx8IGRhdGEpW2tleV0gPSB2YWw7XG5cdHJldHVybiBkYXRhO1xufTtcblxuLy8gU2V0IGEgbmVzdGVkIHZhbHVlIGluIGRhdGEgKG5vdCB1bmxpa2UgaW1tdXRhYmxlanMsIGJ1dCBhIGNsb25lIG9mIGRhdGEgaXMgZXhwZWN0ZWQgZm9yIHByb3BlciBpbW11dGFiaWxpdHkpXG5jb25zdCBfc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID0gbnVsbCkgPT5cblx0cGF0aC5sZW5ndGggPiAxID9cblx0XHRfc2V0SW4ocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID8gZGVyZWZbcGF0aC5zaGlmdCgpXSA6IGRhdGFbcGF0aC5zaGlmdCgpXSkgOlxuXHRcdHNldEVpdGhlcihkYXRhLCBkZXJlZiwgcGF0aFswXSwgdmFsdWUpO1xuXG5jb25zdCBzZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSkgPT5cblx0X3NldEluKGNsb25lKHBhdGgpLCB2YWx1ZSwgY2xvbmUoZGF0YSkpO1xuXG5leHBvcnQgZGVmYXVsdCBzZXRJbjsiXX0=
