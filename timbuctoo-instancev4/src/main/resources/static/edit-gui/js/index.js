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
			return _react2.default.createElement(
				_page2.default,
				null,
				_react2.default.createElement(_collectionTabs2.default, { collections: vre.collections, onNew: onNew, onSelectDomain: onSelectDomain, onRedirectToFirst: onRedirectToFirst,
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
							entity: entity, onNew: onNew, onDelete: onDelete, onChange: onChange,
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
							} }) : null
					)
				)
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

},{"../../../urls":37,"./fields/camel2label":10,"./fields/links":11,"./fields/list-of-strings":12,"./fields/multi-select":13,"./fields/names":14,"./fields/relation":15,"./fields/select":16,"./fields/string-field":17,"react":"react","react-router":"react-router"}],19:[function(require,module,exports){
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
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDRWUsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFJLFVBQVU7QUFDYixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEYSxFQUFkOztBQUlBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxPQUFLLEtBQUssS0FBTCxDQUFXLElBQVgsRUFBaUIsR0FBakIsQ0FBcUIsVUFBQyxDQUFELEVBQU87QUFBRSxVQUFPLEVB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QVAsRUFBYyxJQUFkO0FBQUEsUUFDYixPQUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHBhdGgsIHF1ZXJ5LCBkb25lKSB7XG5cdGxldCBvcHRpb25zID0ge1xuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xLyR7cGF0aC5yZXBsYWNlKC9eXFwvdlteL10rXFwvLywgXCJcIil9P3F1ZXJ5PSR7cXVlcnl9KmBcblx0fTtcblxuXHRsZXQgeGhyRG9uZSA9IGZ1bmN0aW9uKGVyciwgcmVzcG9uc2UsIGJvZHkpIHtcblx0XHRkb25lKEpTT04ucGFyc2UoYm9keSkubWFwKChkKSA9PiB7IHJldHVybiB7a2V5OiBkLmtleS5yZXBsYWNlKC9eLitcXC8vLCBcIlwiKSwgdmFsdWU6IGQudmFsdWV9OyB9KSk7XG5cdH07XG5cblx0c2VydmVyLmZhc3RYaHIob3B0aW9ucywgeGhyRG9uZSk7XG59IiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcblxuY29uc3Qgc2F2ZU5ld0VudGl0eSA9IChkb21haW4sIHNhdmVEYXRhLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiUE9TVFwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdGJvZHk6IEpTT04uc3RyaW5naWZ5KHNhdmVEYXRhKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59YFxuXHR9LCBuZXh0LCBmYWlsLCBgQ3JlYXRlIG5ldyAke2RvbWFpbn1gKTtcblxuY29uc3QgdXBkYXRlRW50aXR5ID0gKGRvbWFpbiwgc2F2ZURhdGEsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJQVVRcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHRib2R5OiBKU09OLnN0cmluZ2lmeShzYXZlRGF0YSksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke3NhdmVEYXRhLl9pZH1gXG5cdH0sIG5leHQsIGZhaWwsIGBVcGRhdGUgJHtkb21haW59YCk7XG5cbmNvbnN0IGRlbGV0ZUVudGl0eSA9IChkb21haW4sIGVudGl0eUlkLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiREVMRVRFXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke2VudGl0eUlkfWBcblx0fSwgbmV4dCwgZmFpbCwgYERlbGV0ZSAke2RvbWFpbn1gKTtcblxuY29uc3QgZmV0Y2hFbnRpdHkgPSAobG9jYXRpb24sIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1wiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwifSxcblx0XHR1cmw6IGxvY2F0aW9uXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdG5leHQoZGF0YSk7XG5cdH0sIGZhaWwsIFwiRmV0Y2ggZW50aXR5XCIpO1xuXG5jb25zdCBmZXRjaEVudGl0eUxpc3QgPSAoZG9tYWluLCBzdGFydCwgcm93cywgbmV4dCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJ9LFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0/cm93cz0ke3Jvd3N9JnN0YXJ0PSR7c3RhcnR9YFxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0Y29uc3QgZGF0YSA9IEpTT04ucGFyc2UocmVzcC5ib2R5KTtcblx0XHRuZXh0KGRhdGEpO1xuXHR9KTtcblxuY29uc3QgY3J1ZCA9IHtcblx0c2F2ZU5ld0VudGl0eTogc2F2ZU5ld0VudGl0eSxcblx0dXBkYXRlRW50aXR5OiB1cGRhdGVFbnRpdHksXG5cdGRlbGV0ZUVudGl0eTogZGVsZXRlRW50aXR5LFxuXHRmZXRjaEVudGl0eTogZmV0Y2hFbnRpdHksXG5cdGZldGNoRW50aXR5TGlzdDogZmV0Y2hFbnRpdHlMaXN0XG59O1xuXG5leHBvcnQge3NhdmVOZXdFbnRpdHksIHVwZGF0ZUVudGl0eSwgZGVsZXRlRW50aXR5LCBmZXRjaEVudGl0eSwgZmV0Y2hFbnRpdHlMaXN0LCBjcnVkfTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4uL3V0aWwvY2xvbmUtZGVlcFwiO1xuaW1wb3J0IHsgY3J1ZCB9IGZyb20gXCIuL2NydWRcIjtcbmltcG9ydCBzYXZlUmVsYXRpb25zIGZyb20gXCIuL3NhdmUtcmVsYXRpb25zXCI7XG5pbXBvcnQgYXV0b2NvbXBsZXRlIGZyb20gXCIuL2F1dG9jb21wbGV0ZVwiO1xuXG4vLyBTa2VsZXRvbiBiYXNlIGRhdGEgcGVyIGZpZWxkIGRlZmluaXRpb25cbmNvbnN0IGluaXRpYWxEYXRhID0ge1xuXHRuYW1lczogW10sXG5cdG11bHRpc2VsZWN0OiBbXSxcblx0bGlua3M6IFtdLFxuXHRrZXl3b3JkOiBbXSxcblx0XCJsaXN0LW9mLXN0cmluZ3NcIjogW10sXG5cdGFsdG5hbWVzOiBbXSxcblx0dGV4dDogXCJcIixcblx0c3RyaW5nOiBcIlwiLFxuXHRzZWxlY3Q6IFwiXCIsXG5cdGRhdGFibGU6IFwiXCJcbn07XG5cbi8vIFJldHVybiB0aGUgaW5pdGlhbCBkYXRhIGZvciB0aGUgdHlwZSBpbiB0aGUgZmllbGQgZGVmaW5pdGlvblxuY29uc3QgaW5pdGlhbERhdGFGb3JUeXBlID0gKGZpZWxkRGVmKSA9PlxuXHRmaWVsZERlZi5kZWZhdWx0VmFsdWUgfHwgKGZpZWxkRGVmLnR5cGUgPT09IFwicmVsYXRpb25cIiB8fCBmaWVsZERlZi50eXBlID09PSBcImtleXdvcmRcIiA/IHt9IDogaW5pdGlhbERhdGFbZmllbGREZWYudHlwZV0pO1xuXG5jb25zdCBhZGRGaWVsZHNUb0VudGl0eSA9IChmaWVsZHMpID0+IChkaXNwYXRjaCkgPT4ge1xuXHRmaWVsZHMuZm9yRWFjaCgoZmllbGQpID0+IHtcblx0XHRpZiAoZmllbGQudHlwZSA9PT0gXCJyZWxhdGlvblwiKSB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCIsIGZpZWxkUGF0aDogW1wiQHJlbGF0aW9uc1wiLCBmaWVsZC5uYW1lXSwgdmFsdWU6IFtdfSk7XG5cdFx0fSBlbHNlIHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBbZmllbGQubmFtZV0sIHZhbHVlOiBpbml0aWFsRGF0YUZvclR5cGUoZmllbGQpfSk7XG5cdFx0fVxuXHR9KVxufTtcblxuY29uc3QgZmV0Y2hFbnRpdHlMaXN0ID0gKGRvbWFpbiwgbmV4dCA9ICgpID0+IHt9KSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiAwfSk7XG5cdGNydWQuZmV0Y2hFbnRpdHlMaXN0KGRvbWFpbiwgMCwgZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzLCAoZGF0YSkgPT4ge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIiwgZGF0YTogZGF0YX0pO1xuXHRcdG5leHQoZGF0YSk7XG5cdH0pO1xufTtcblxuY29uc3QgcGFnaW5hdGVMZWZ0ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCBuZXdTdGFydCA9IGdldFN0YXRlKCkucXVpY2tTZWFyY2guc3RhcnQgLSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3M7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiBuZXdTdGFydCA8IDAgPyAwIDogbmV3U3RhcnR9KTtcblx0Y3J1ZC5mZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBuZXdTdGFydCA8IDAgPyAwIDogbmV3U3RhcnQsIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cywgKGRhdGEpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIiwgZGF0YTogZGF0YX0pKTtcbn07XG5cbmNvbnN0IHBhZ2luYXRlUmlnaHQgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IG5ld1N0YXJ0ID0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5zdGFydCArIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cztcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IG5ld1N0YXJ0fSk7XG5cdGNydWQuZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgbmV3U3RhcnQsIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cywgKGRhdGEpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIiwgZGF0YTogZGF0YX0pKTtcbn07XG5cbmNvbnN0IHNlbmRRdWlja1NlYXJjaCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgeyBxdWlja1NlYXJjaCwgZW50aXR5LCB2cmUgfSA9IGdldFN0YXRlKCk7XG5cdGlmIChxdWlja1NlYXJjaC5xdWVyeS5sZW5ndGgpIHtcblx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogMH0pO1xuXHRcdGNvbnN0IGNhbGxiYWNrID0gKGRhdGEpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIiwgZGF0YTogZGF0YS5tYXAoKGQpID0+IChcblx0XHRcdHtcblx0XHRcdFx0X2lkOiBkLmtleS5yZXBsYWNlKC8uKlxcLy8sIFwiXCIpLFxuXHRcdFx0XHRcIkBkaXNwbGF5TmFtZVwiOiBkLnZhbHVlXG5cdFx0XHR9XG5cdFx0KSl9KTtcblx0XHRhdXRvY29tcGxldGUoYGRvbWFpbi8ke2VudGl0eS5kb21haW59L2F1dG9jb21wbGV0ZWAsIHF1aWNrU2VhcmNoLnF1ZXJ5LCBjYWxsYmFjayk7XG5cdH0gZWxzZSB7XG5cdFx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGVudGl0eS5kb21haW4pKTtcblx0fVxufTtcblxuY29uc3Qgc2VsZWN0RG9tYWluID0gKGRvbWFpbikgPT4gKGRpc3BhdGNoKSA9PiB7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9ET01BSU5cIiwgZG9tYWlufSk7XG5cdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChkb21haW4pKTtcblx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCIsIHZhbHVlOiBcIlwifSk7XG59O1xuXG4vLyAxKSBGZXRjaCBlbnRpdHlcbi8vIDIpIERpc3BhdGNoIFJFQ0VJVkVfRU5USVRZIGZvciByZW5kZXJcbmNvbnN0IHNlbGVjdEVudGl0eSA9IChkb21haW4sIGVudGl0eUlkLCBlcnJvck1lc3NhZ2UgPSBudWxsLCBzdWNjZXNzTWVzc2FnZSA9IG51bGwsIG5leHQgPSAoKSA9PiB7IH0pID0+XG5cdChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0XHRjb25zdCB7IGVudGl0eTogeyBkb21haW46IGN1cnJlbnREb21haW4gfSB9ID0gZ2V0U3RhdGUoKTtcblx0XHRpZiAoY3VycmVudERvbWFpbiAhPT0gZG9tYWluKSB7XG5cdFx0XHRkaXNwYXRjaChzZWxlY3REb21haW4oZG9tYWluKSk7XG5cdFx0fVxuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIkJFRk9SRV9GRVRDSF9FTlRJVFlcIn0pXG5cdFx0Y3J1ZC5mZXRjaEVudGl0eShgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke2VudGl0eUlkfWAsIChkYXRhKSA9PiB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLCBkb21haW46IGRvbWFpbiwgZGF0YTogZGF0YSwgZXJyb3JNZXNzYWdlOiBlcnJvck1lc3NhZ2V9KTtcblx0XHRcdGlmIChzdWNjZXNzTWVzc2FnZSAhPT0gbnVsbCkge1xuXHRcdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTVUNDRVNTX01FU1NBR0VcIiwgbWVzc2FnZTogc3VjY2Vzc01lc3NhZ2V9KTtcblx0XHRcdH1cblx0XHR9LCAoKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9GQUlMVVJFXCIsIGVycm9yTWVzc2FnZTogYEZhaWxlZCB0byBmZXRjaCAke2RvbWFpbn0gd2l0aCBJRCAke2VudGl0eUlkfWB9KSk7XG5cdFx0bmV4dCgpO1xuXHR9O1xuXG5cbi8vIDEpIERpc3BhdGNoIFJFQ0VJVkVfRU5USVRZIHdpdGggZW1wdHkgZW50aXR5IHNrZWxldG9uIGZvciByZW5kZXJcbmNvbnN0IG1ha2VOZXdFbnRpdHkgPSAoZG9tYWluLCBlcnJvck1lc3NhZ2UgPSBudWxsKSA9PlxuXHQoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiBkaXNwYXRjaCh7XG5cdFx0dHlwZTogXCJSRUNFSVZFX0VOVElUWVwiLFxuXHRcdGRvbWFpbjogZG9tYWluLFxuXHRcdGRhdGE6IHtcIkByZWxhdGlvbnNcIjoge319LFxuXHRcdGVycm9yTWVzc2FnZTogZXJyb3JNZXNzYWdlXG5cdH0pO1xuXG5jb25zdCBkZWxldGVFbnRpdHkgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNydWQuZGVsZXRlRW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsXG5cdFx0KCkgPT4ge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU1VDQ0VTU19NRVNTQUdFXCIsIG1lc3NhZ2U6IGBTdWNlc3NmdWxseSBkZWxldGVkICR7Z2V0U3RhdGUoKS5lbnRpdHkuZG9tYWlufSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YH0pO1xuXHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKTtcblx0XHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKTtcblx0XHR9LFxuXHRcdCgpID0+IGRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBgRmFpbGVkIHRvIGRlbGV0ZSAke2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbn0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWApKSk7XG59O1xuXG4vLyAxKSBTYXZlIGFuIGVudGl0eVxuLy8gMikgU2F2ZSB0aGUgcmVsYXRpb25zIGZvciB0aGlzIGVudGl0eVxuLy8gMykgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuY29uc3Qgc2F2ZUVudGl0eSA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgY29sbGVjdGlvbkxhYmVsID0gZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5jb2xsZWN0aW9uTGFiZWwucmVwbGFjZSgvcyQvLCBcIlwiKTtcblxuXHQvLyBNYWtlIGEgZGVlcCBjb3B5IG9mIHRoZSBkYXRhIHRvIGJlIHNhdmVkIGluIG9yZGVyIHRvIGxlYXZlIGFwcGxpY2F0aW9uIHN0YXRlIHVuYWx0ZXJlZFxuXHRsZXQgc2F2ZURhdGEgPSBjbG9uZShnZXRTdGF0ZSgpLmVudGl0eS5kYXRhKTtcblx0Ly8gTWFrZSBhIGRlZXAgY29weSBvZiB0aGUgcmVsYXRpb24gZGF0YSBpbiBvcmRlciB0byBsZWF2ZSBhcHBsaWNhdGlvbiBzdGF0ZSB1bmFsdGVyZWRcblx0bGV0IHJlbGF0aW9uRGF0YSA9IGNsb25lKHNhdmVEYXRhW1wiQHJlbGF0aW9uc1wiXSkgfHwge307XG5cdC8vIERlbGV0ZSB0aGUgcmVsYXRpb24gZGF0YSBmcm9tIHRoZSBzYXZlRGF0YSBhcyBpdCBpcyBub3QgZXhwZWN0ZWQgYnkgdGhlIHNlcnZlclxuXHRkZWxldGUgc2F2ZURhdGFbXCJAcmVsYXRpb25zXCJdO1xuXG5cdGlmIChnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdC8vIDEpIFVwZGF0ZSB0aGUgZW50aXR5IHdpdGggc2F2ZURhdGFcblx0XHRjcnVkLnVwZGF0ZUVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIHNhdmVEYXRhLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoZXJyLCByZXNwKSA9PlxuXHRcdFx0Ly8gMikgU2F2ZSByZWxhdGlvbnMgdXNpbmcgc2VydmVyIHJlc3BvbnNlIGZvciBjdXJyZW50IHJlbGF0aW9ucyB0byBkaWZmIGFnYWluc3QgcmVsYXRpb25EYXRhXG5cdFx0XHRkaXNwYXRjaCgocmVkaXNwYXRjaCkgPT4gc2F2ZVJlbGF0aW9ucyhKU09OLnBhcnNlKHJlc3AuYm9keSksIHJlbGF0aW9uRGF0YSwgZ2V0U3RhdGUoKS52cmUuY29sbGVjdGlvbnNbZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluXS5wcm9wZXJ0aWVzLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLCAoKSA9PlxuXHRcdFx0XHQvLyAzKSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5cdFx0XHRcdHJlZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIG51bGwsIGBTdWNjZXNmdWxseSBzYXZlZCAke2NvbGxlY3Rpb25MYWJlbH0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWAsICgpID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKSkpKSksICgpID0+XG5cdFx0XHRcdFx0Ly8gMmEpIEhhbmRsZSBlcnJvciBieSByZWZldGNoaW5nIGFuZCBwYXNzaW5nIGFsb25nIGFuIGVycm9yIG1lc3NhZ2Vcblx0XHRcdFx0XHRkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgYEZhaWxlZCB0byBzYXZlICR7Y29sbGVjdGlvbkxhYmVsfSB3aXRoIElEICR7Z2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWR9YCkpKTtcblxuXHR9IGVsc2Uge1xuXHRcdC8vIDEpIENyZWF0ZSBuZXcgZW50aXR5IHdpdGggc2F2ZURhdGFcblx0XHRjcnVkLnNhdmVOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBzYXZlRGF0YSwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKGVyciwgcmVzcCkgPT5cblx0XHRcdC8vIDIpIEZldGNoIGVudGl0eSB2aWEgbG9jYXRpb24gaGVhZGVyXG5cdFx0XHRkaXNwYXRjaCgocmVkaXNwYXRjaCkgPT4gY3J1ZC5mZXRjaEVudGl0eShyZXNwLmhlYWRlcnMubG9jYXRpb24sIChkYXRhKSA9PlxuXHRcdFx0XHQvLyAzKSBTYXZlIHJlbGF0aW9ucyB1c2luZyBzZXJ2ZXIgcmVzcG9uc2UgZm9yIGN1cnJlbnQgcmVsYXRpb25zIHRvIGRpZmYgYWdhaW5zdCByZWxhdGlvbkRhdGFcblx0XHRcdFx0c2F2ZVJlbGF0aW9ucyhkYXRhLCByZWxhdGlvbkRhdGEsIGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0ucHJvcGVydGllcywgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKCkgPT5cblx0XHRcdFx0XHQvLyA0KSBSZWZldGNoIGVudGl0eSBmb3IgcmVuZGVyXG5cdFx0XHRcdFx0cmVkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBkYXRhLl9pZCwgbnVsbCwgYFN1Y2Nlc2Z1bGx5IHNhdmVkICR7Y29sbGVjdGlvbkxhYmVsfWAsICgpID0+IGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4pKSkpKSkpLCAoKSA9PlxuXHRcdFx0XHRcdFx0Ly8gMmEpIEhhbmRsZSBlcnJvciBieSByZWZldGNoaW5nIGFuZCBwYXNzaW5nIGFsb25nIGFuIGVycm9yIG1lc3NhZ2Vcblx0XHRcdFx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBgRmFpbGVkIHRvIHNhdmUgbmV3ICR7Y29sbGVjdGlvbkxhYmVsfWApKSk7XG5cdH1cbn07XG5cblxuZXhwb3J0IHsgc2F2ZUVudGl0eSwgc2VsZWN0RW50aXR5LCBtYWtlTmV3RW50aXR5LCBkZWxldGVFbnRpdHksIGZldGNoRW50aXR5TGlzdCwgcGFnaW5hdGVSaWdodCwgcGFnaW5hdGVMZWZ0LCBzZW5kUXVpY2tTZWFyY2gsIHNlbGVjdERvbWFpbiwgYWRkRmllbGRzVG9FbnRpdHkgfTsiLCJpbXBvcnQgeyBzYXZlRW50aXR5LCBzZWxlY3RFbnRpdHksIG1ha2VOZXdFbnRpdHksIGRlbGV0ZUVudGl0eSwgYWRkRmllbGRzVG9FbnRpdHksXG5cdHNlbGVjdERvbWFpbiwgcGFnaW5hdGVMZWZ0LCBwYWdpbmF0ZVJpZ2h0LCBzZW5kUXVpY2tTZWFyY2gsIGZldGNoRW50aXR5TGlzdCB9IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IHsgc2V0VnJlIH0gZnJvbSBcIi4vdnJlXCI7XG5cbmV4cG9ydCBkZWZhdWx0IChuYXZpZ2F0ZVRvLCBkaXNwYXRjaCkgPT4gKHtcblx0b25OZXc6IChkb21haW4pID0+IGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZG9tYWluKSksXG5cdG9uU2VsZWN0OiAocmVjb3JkKSA9PiBkaXNwYXRjaChzZWxlY3RFbnRpdHkocmVjb3JkLmRvbWFpbiwgcmVjb3JkLmlkKSksXG5cdG9uU2F2ZTogKCkgPT4gZGlzcGF0Y2goc2F2ZUVudGl0eSgpKSxcblx0b25EZWxldGU6ICgpID0+IGRpc3BhdGNoKGRlbGV0ZUVudGl0eSgpKSxcblx0b25DaGFuZ2U6IChmaWVsZFBhdGgsIHZhbHVlKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCIsIGZpZWxkUGF0aDogZmllbGRQYXRoLCB2YWx1ZTogdmFsdWV9KSxcblx0b25BZGRTZWxlY3RlZEZpZWxkczogKGZpZWxkcykgPT4gZGlzcGF0Y2goYWRkRmllbGRzVG9FbnRpdHkoZmllbGRzKSksXG5cblx0b25SZWRpcmVjdFRvRmlyc3Q6IChjb2xsZWN0aW9uKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoY29sbGVjdGlvbiwgKGxpc3QpID0+IHtcblx0XHRpZiAobGlzdC5sZW5ndGggPiAwKSB7XG5cdFx0XHRuYXZpZ2F0ZVRvKCdlbnRpdHknLCBbY29sbGVjdGlvbiwgbGlzdFswXS5faWRdKTtcblx0XHR9XG5cdH0pKSxcblxuXHRvbkxvZ2luQ2hhbmdlOiAocmVzcG9uc2UpID0+IGRpc3BhdGNoKHNldFVzZXIocmVzcG9uc2UpKSxcblx0b25TZWxlY3RWcmU6ICh2cmVJZCkgPT4gZGlzcGF0Y2goc2V0VnJlKHZyZUlkKSksXG5cdG9uRGlzbWlzc01lc3NhZ2U6IChtZXNzYWdlSW5kZXgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIkRJU01JU1NfTUVTU0FHRVwiLCBtZXNzYWdlSW5kZXg6IG1lc3NhZ2VJbmRleH0pLFxuXHRvblNlbGVjdERvbWFpbjogKGRvbWFpbikgPT4ge1xuXHRcdGRpc3BhdGNoKHNlbGVjdERvbWFpbihkb21haW4pKTtcblx0fSxcblx0b25QYWdpbmF0ZUxlZnQ6ICgpID0+IGRpc3BhdGNoKHBhZ2luYXRlTGVmdCgpKSxcblx0b25QYWdpbmF0ZVJpZ2h0OiAoKSA9PiBkaXNwYXRjaChwYWdpbmF0ZVJpZ2h0KCkpLFxuXHRvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2U6ICh2YWx1ZSkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1FVSUNLU0VBUkNIX1FVRVJZXCIsIHZhbHVlOiB2YWx1ZX0pLFxuXHRvblF1aWNrU2VhcmNoOiAoKSA9PiBkaXNwYXRjaChzZW5kUXVpY2tTZWFyY2goKSlcbn0pOyIsImltcG9ydCB7IHNhdmVOZXdFbnRpdHksIHVwZGF0ZUVudGl0eSB9IGZyb20gXCIuL2NydWRcIjtcblxuY29uc3Qgc2F2ZVJlbGF0aW9uc1YyMSA9IChkYXRhLCByZWxhdGlvbkRhdGEsIGZpZWxkRGVmcywgdG9rZW4sIHZyZUlkLCBuZXh0KSA9PiB7XG5cdC8vIFJldHVybnMgdGhlIGRvbWFpbiBiYXNlZCBvbiB0aGUgZmllbGREZWZpbml0aW9ucyBhbmQgdGhlIHJlbGF0aW9uIGtleSAoaS5lLiBcImhhc0JpcnRoUGxhY2VcIilcblx0Y29uc3QgbWFrZVJlbGF0aW9uQXJncyA9IChyZWxhdGlvbiwga2V5LCBhY2NlcHRlZCA9IHRydWUsIGlkID0gbnVsbCwgcmV2ID0gbnVsbCkgPT4ge1xuXHRcdGNvbnN0IGZpZWxkRGVmID0gZmllbGREZWZzLmZpbmQoKGRlZikgPT4gZGVmLm5hbWUgPT09IGtleSk7XG5cblxuXHRcdGNvbnN0IHNvdXJjZVR5cGUgPSBkYXRhW1wiQHR5cGVcIl0ucmVwbGFjZSgvcyQvLCBcIlwiKS5yZXBsYWNlKC9ed3cvLCBcIlwiKTtcblx0XHRjb25zdCB0YXJnZXRUeXBlID0gZmllbGREZWYucmVsYXRpb24udGFyZ2V0Q29sbGVjdGlvbi5yZXBsYWNlKC9zJC8sIFwiXCIpLnJlcGxhY2UoL153dy8sIFwiXCIpO1xuXG5cdFx0Y29uc3QgcmVsYXRpb25TYXZlRGF0YSA9IHtcblx0XHRcdFwiQHR5cGVcIjogZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25Db2xsZWN0aW9uLnJlcGxhY2UoL3MkLywgXCJcIiksIC8vIGNoZWNrXG5cdFx0XHRcIl5zb3VyY2VJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHJlbGF0aW9uLmlkIDogZGF0YS5faWQsIC8vIGNoZWNrXG5cdFx0XHRcIl5zb3VyY2VUeXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gdGFyZ2V0VHlwZSA6IHNvdXJjZVR5cGUsIC8vIGNoZWNrXG5cdFx0XHRcIl50YXJnZXRJZFwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IGRhdGEuX2lkIDogcmVsYXRpb24uaWQsIC8vIGNoZWNrXG5cdFx0XHRcIl50YXJnZXRUeXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLmRpcmVjdGlvbiA9PT0gXCJJTlwiID8gc291cmNlVHlwZSA6IHRhcmdldFR5cGUsXG5cdFx0XHRcIl50eXBlSWRcIjogZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25UeXBlSWQsIC8vIGNoZWNrXG5cdFx0XHRhY2NlcHRlZDogYWNjZXB0ZWRcblx0XHR9O1xuXG5cdFx0aWYoaWQpIHsgcmVsYXRpb25TYXZlRGF0YS5faWQgPSBpZDsgfVxuXHRcdGlmKHJldikgeyByZWxhdGlvblNhdmVEYXRhW1wiXnJldlwiXSA9IHJldjsgfVxuXHRcdHJldHVybiBbXG5cdFx0XHRmaWVsZERlZi5yZWxhdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24sIC8vIGRvbWFpblxuXHRcdFx0cmVsYXRpb25TYXZlRGF0YVxuXHRcdF07XG5cdH07XG5cblx0Ly8gQ29uc3RydWN0cyBhbiBhcnJheSBvZiBhcmd1bWVudHMgZm9yIHNhdmluZyBuZXcgcmVsYXRpb25zOlxuXHQvLyBbXG5cdC8vICAgW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV0sXG5cdC8vICAgW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV0sXG5cdC8vIF1cblx0Y29uc3QgbmV3UmVsYXRpb25zID0gT2JqZWN0LmtleXMocmVsYXRpb25EYXRhKS5tYXAoKGtleSkgPT5cblx0XHRcdHJlbGF0aW9uRGF0YVtrZXldXG5cdFx0XHQvLyBGaWx0ZXJzIG91dCBhbGwgcmVsYXRpb25zIHdoaWNoIGFyZSBub3QgYWxyZWFkeSBpbiBkYXRhW1wiQHJlbGF0aW9uc1wiXVxuXHRcdFx0XHQuZmlsdGVyKChyZWxhdGlvbikgPT4gKGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV0gfHwgW10pLm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uaWQpLmluZGV4T2YocmVsYXRpb24uaWQpIDwgMClcblx0XHRcdFx0Ly8gTWFrZSBhcmd1bWVudCBhcnJheSBmb3IgbmV3IHJlbGF0aW9uczogW1wid3dyZWxhdGlvbnNcIiwgeyAuLi4gfV1cblx0XHRcdFx0Lm1hcCgocmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3MocmVsYXRpb24sIGtleSkpXG5cdFx0Ly8gRmxhdHRlbiBuZXN0ZWQgYXJyYXlzXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cblx0Ly8gUmVhY3RpdmF0ZSBwcmV2aW91c2x5IGFkZGVkIHJlbGF0aW9ucyB1c2luZyBQVVQgd2hpY2ggd2VyZSAnZGVsZXRlZCcgYWZ0ZXIgdXNpbmcgUFVUXG5cdGNvbnN0IHJlQWRkUmVsYXRpb25zID0gT2JqZWN0LmtleXMocmVsYXRpb25EYXRhKS5tYXAoKGtleSkgPT5cblx0XHQoZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XSB8fCBbXSlcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmFjY2VwdGVkID09PSBmYWxzZSlcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gKHJlbGF0aW9uRGF0YVtrZXldIHx8IFtdKS5maWx0ZXIoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5hY2NlcHRlZCkubWFwKChyZWxhdGlvbikgPT4gcmVsYXRpb24uaWQpLmluZGV4T2Yob3JpZ1JlbGF0aW9uLmlkKSA+IC0xKVxuXHRcdFx0Lm1hcCgob3JpZ1JlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKG9yaWdSZWxhdGlvbiwga2V5LCB0cnVlLCBvcmlnUmVsYXRpb24ucmVsYXRpb25JZCwgb3JpZ1JlbGF0aW9uLnJldikpXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cdC8vIERlYWN0aXZhdGUgcHJldmlvdXNseSBhZGRlZCByZWxhdGlvbnMgdXNpbmcgUFVUXG5cdGNvbnN0IGRlbGV0ZVJlbGF0aW9ucyA9IE9iamVjdC5rZXlzKGRhdGFbXCJAcmVsYXRpb25zXCJdKS5tYXAoKGtleSkgPT5cblx0XHRkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5hY2NlcHRlZClcblx0XHRcdC5maWx0ZXIoKG9yaWdSZWxhdGlvbikgPT4gKHJlbGF0aW9uRGF0YVtrZXldIHx8IFtdKS5tYXAoKHJlbGF0aW9uKSA9PiByZWxhdGlvbi5pZCkuaW5kZXhPZihvcmlnUmVsYXRpb24uaWQpIDwgMClcblx0XHRcdC5tYXAoKG9yaWdSZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhvcmlnUmVsYXRpb24sIGtleSwgZmFsc2UsIG9yaWdSZWxhdGlvbi5yZWxhdGlvbklkLCBvcmlnUmVsYXRpb24ucmV2KSlcblx0KS5yZWR1Y2UoKGEsIGIpID0+IGEuY29uY2F0KGIpLCBbXSk7XG5cblx0Ly8gQ29tYmluZXMgc2F2ZU5ld0VudGl0eSBhbmQgZGVsZXRlRW50aXR5IGluc3RydWN0aW9ucyBpbnRvIHByb21pc2VzXG5cdGNvbnN0IHByb21pc2VzID0gbmV3UmVsYXRpb25zXG5cdC8vIE1hcCBuZXdSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2Ygc2F2ZU5ld0VudGl0eVxuXHRcdC5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHNhdmVOZXdFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpICkpXG5cdFx0Ly8gTWFwIHJlYWRkUmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHVwZGF0ZUVudGl0eVxuXHRcdC5jb25jYXQocmVBZGRSZWxhdGlvbnMubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiB1cGRhdGVFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpKSkpXG5cdFx0Ly8gTWFwIGRlbGV0ZVJlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiB1cGRhdGVFbnRpdHlcblx0XHQuY29uY2F0KGRlbGV0ZVJlbGF0aW9ucy5tYXAoKGFyZ3MpID0+IG5ldyBQcm9taXNlKChyZXNvbHZlLCByZWplY3QpID0+IHVwZGF0ZUVudGl0eSguLi5hcmdzLCB0b2tlbiwgdnJlSWQsIHJlc29sdmUsIHJlamVjdCkpKSk7XG5cblx0Ly8gSW52b2tlIGFsbCBDUlVEIG9wZXJhdGlvbnMgZm9yIHRoZSByZWxhdGlvbnNcblx0UHJvbWlzZS5hbGwocHJvbWlzZXMpLnRoZW4obmV4dCwgbmV4dCk7XG59O1xuXG5leHBvcnQgZGVmYXVsdCBzYXZlUmVsYXRpb25zVjIxOyIsImltcG9ydCB4aHIgZnJvbSBcInhoclwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuLi9zdG9yZVwiO1xuXG5leHBvcnQgZGVmYXVsdCB7XG5cdHBlcmZvcm1YaHI6IGZ1bmN0aW9uIChvcHRpb25zLCBhY2NlcHQsIHJlamVjdCA9ICgpID0+IHsgY29uc29sZS53YXJuKFwiVW5kZWZpbmVkIHJlamVjdCBjYWxsYmFjayEgXCIpOyB9LCBvcGVyYXRpb24gPSBcIlNlcnZlciByZXF1ZXN0XCIpIHtcblx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJSRVFVRVNUX01FU1NBR0VcIiwgbWVzc2FnZTogYCR7b3BlcmF0aW9ufTogJHtvcHRpb25zLm1ldGhvZCB8fCBcIkdFVFwifSAke29wdGlvbnMudXJsfWB9KTtcblx0XHR4aHIob3B0aW9ucywgKGVyciwgcmVzcCwgYm9keSkgPT4ge1xuXHRcdFx0aWYocmVzcC5zdGF0dXNDb2RlID49IDQwMCkge1xuXHRcdFx0XHRzdG9yZS5kaXNwYXRjaCh7dHlwZTogXCJFUlJPUl9NRVNTQUdFXCIsIG1lc3NhZ2U6IGAke29wZXJhdGlvbn0gZmFpbGVkIHdpdGggY2F1c2U6ICR7cmVzcC5ib2R5fWB9KTtcblx0XHRcdFx0cmVqZWN0KGVyciwgcmVzcCwgYm9keSk7XG5cdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRhY2NlcHQoZXJyLCByZXNwLCBib2R5KTtcblx0XHRcdH1cblx0XHR9KTtcblx0fSxcblxuXHRmYXN0WGhyOiBmdW5jdGlvbihvcHRpb25zLCBhY2NlcHQpIHtcblx0XHR4aHIob3B0aW9ucywgYWNjZXB0KTtcblx0fSxcblxuXHRtYWtlSGVhZGVyczogZnVuY3Rpb24odG9rZW4sIHZyZUlkKSB7XG5cdFx0cmV0dXJuIHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiLFxuXHRcdFx0XCJDb250ZW50LXR5cGVcIjogXCJhcHBsaWNhdGlvbi9qc29uXCIsXG5cdFx0XHRcIkF1dGhvcml6YXRpb25cIjogdG9rZW4sXG5cdFx0XHRcIlZSRV9JRFwiOiB2cmVJZFxuXHRcdH07XG5cdH1cbn07XG4iLCJpbXBvcnQgc2VydmVyIGZyb20gXCIuL3NlcnZlclwiO1xuaW1wb3J0IGFjdGlvbnMgZnJvbSBcIi4vaW5kZXhcIjtcbmltcG9ydCB7bWFrZU5ld0VudGl0eX0gZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQge2ZldGNoRW50aXR5TGlzdH0gZnJvbSBcIi4vZW50aXR5XCI7XG5cbmNvbnN0IGxpc3RWcmVzID0gKCkgPT4gKGRpc3BhdGNoKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiXG5cdFx0fSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9zeXN0ZW0vdnJlc2Bcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGRpc3BhdGNoKHt0eXBlOiBcIkxJU1RfVlJFU1wiLCBsaXN0OiBKU09OLnBhcnNlKHJlc3AuYm9keSl9KTtcblx0fSwgbnVsbCwgXCJMaXN0IFZSRXNcIik7XG5cbmNvbnN0IHNldFZyZSA9ICh2cmVJZCwgbmV4dCA9ICgpID0+IHsgfSkgPT4gKGRpc3BhdGNoKSA9PlxuXHRzZXJ2ZXIucGVyZm9ybVhocih7XG5cdFx0bWV0aG9kOiBcIkdFVFwiLFxuXHRcdGhlYWRlcnM6IHtcblx0XHRcdFwiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwiXG5cdFx0fSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9tZXRhZGF0YS8ke3ZyZUlkfT93aXRoQ29sbGVjdGlvbkluZm89dHJ1ZWBcblx0fSwgKGVyciwgcmVzcCkgPT4ge1xuXHRcdGlmIChyZXNwLnN0YXR1c0NvZGUgPT09IDIwMCkge1xuXHRcdFx0dmFyIGJvZHkgPSBKU09OLnBhcnNlKHJlc3AuYm9keSk7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfVlJFXCIsIHZyZUlkOiB2cmVJZCwgY29sbGVjdGlvbnM6IGJvZHl9KTtcblxuXHRcdFx0bGV0IGRlZmF1bHREb21haW4gPSBPYmplY3Qua2V5cyhib2R5KVxuXHRcdFx0XHQubWFwKGNvbGxlY3Rpb25OYW1lID0+IGJvZHlbY29sbGVjdGlvbk5hbWVdKVxuXHRcdFx0XHQuZmlsdGVyKGNvbGxlY3Rpb24gPT4gIWNvbGxlY3Rpb24udW5rbm93biAmJiAhY29sbGVjdGlvbi5yZWxhdGlvbkNvbGxlY3Rpb24pWzBdXG5cdFx0XHRcdC5jb2xsZWN0aW9uTmFtZTtcblxuXHRcdFx0ZGlzcGF0Y2gobWFrZU5ld0VudGl0eShkZWZhdWx0RG9tYWluKSlcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9ET01BSU5cIiwgZGVmYXVsdERvbWFpbn0pO1xuXHRcdFx0ZGlzcGF0Y2goZmV0Y2hFbnRpdHlMaXN0KGRlZmF1bHREb21haW4pKTtcblx0XHRcdG5leHQoKTtcblx0XHR9XG5cdH0sICgpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9WUkVcIiwgdnJlSWQ6IHZyZUlkLCBjb2xsZWN0aW9uczoge319KSwgYEZldGNoIFZSRSBkZXNjcmlwdGlvbiBmb3IgJHt2cmVJZH1gKTtcblxuXG5leHBvcnQge2xpc3RWcmVzLCBzZXRWcmV9O1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNsYXNzbmFtZXMgZnJvbSBcImNsYXNzbmFtZXNcIjtcbmltcG9ydCB7dXJsc30gZnJvbSBcIi4uLy4uL3VybHNcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5cbmNsYXNzIENvbGxlY3Rpb25UYWJzIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IGNvbGxlY3Rpb25zLCBhY3RpdmVEb21haW4sIG9uUmVkaXJlY3RUb0ZpcnN0IH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGRvbWFpbnMgPSBPYmplY3Qua2V5cyhjb2xsZWN0aW9ucyB8fCB7fSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb250YWluZXIgYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2LXRhYnNcIj5cbiAgICAgICAgICB7ZG9tYWluc1xuICAgICAgICAgICAgLmZpbHRlcihkID0+ICEoY29sbGVjdGlvbnNbZF0udW5rbm93biB8fCBjb2xsZWN0aW9uc1tkXS5yZWxhdGlvbkNvbGxlY3Rpb24pKVxuICAgICAgICAgICAgLm1hcCgoZG9tYWluKSA9PiAoXG4gICAgICAgICAgICAgIDxsaSBjbGFzc05hbWU9e2NsYXNzbmFtZXMoe2FjdGl2ZTogZG9tYWluID09PSBhY3RpdmVEb21haW59KX0ga2V5PXtkb21haW59PlxuICAgICAgICAgICAgICAgIDxhIG9uQ2xpY2s9eygpID0+IG9uUmVkaXJlY3RUb0ZpcnN0KGRvbWFpbil9PlxuICAgICAgICAgICAgICAgICAge2NvbGxlY3Rpb25zW2RvbWFpbl0uY29sbGVjdGlvbkxhYmVsfVxuICAgICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICAgICkpfVxuICAgICAgICA8L3VsPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5Db2xsZWN0aW9uVGFicy5wcm9wVHlwZXMgPSB7XG5cdG9uTmV3OiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b25TZWxlY3REb21haW46IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRjb2xsZWN0aW9uczogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0YWN0aXZlRG9tYWluOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nXG59O1xuXG5leHBvcnQgZGVmYXVsdCBDb2xsZWN0aW9uVGFicztcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBQYWdlIGZyb20gXCIuLi9wYWdlLmpzeFwiO1xuXG5pbXBvcnQgUGFnaW5hdGUgZnJvbSBcIi4vZW50aXR5LWluZGV4L3BhZ2luYXRlXCI7XG5pbXBvcnQgUXVpY2tTZWFyY2ggZnJvbSBcIi4vZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoXCI7XG5pbXBvcnQgRW50aXR5TGlzdCBmcm9tIFwiLi9lbnRpdHktaW5kZXgvbGlzdFwiO1xuXG5pbXBvcnQgU2F2ZUZvb3RlciBmcm9tIFwiLi9lbnRpdHktZm9ybS9zYXZlLWZvb3RlclwiO1xuaW1wb3J0IEVudGl0eUZvcm0gZnJvbSBcIi4vZW50aXR5LWZvcm0vZm9ybVwiO1xuXG5pbXBvcnQgQ29sbGVjdGlvblRhYnMgZnJvbSBcIi4vY29sbGVjdGlvbi10YWJzXCI7XG5pbXBvcnQgTWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXMvbGlzdFwiO1xuXG5cbmNsYXNzIEVkaXRHdWkgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0Y29uc3QgeyBvblNlbGVjdCwgb25OZXcsIG9uU2VsZWN0RG9tYWluIH0gPSB0aGlzLnByb3BzO1xuXG5cdFx0Ly8gVHJpZ2dlcnMgZmV0Y2ggZGF0YSBmcm9tIHNlcnZlciBiYXNlZCBvbiBpZCBmcm9tIHJvdXRlLlxuXHRcdGlmICh0aGlzLnByb3BzLnBhcmFtcy5pZCAhPT0gbmV4dFByb3BzLnBhcmFtcy5pZCkge1xuXHRcdFx0b25TZWxlY3Qoe2RvbWFpbjogbmV4dFByb3BzLnBhcmFtcy5jb2xsZWN0aW9uLCBpZDogbmV4dFByb3BzLnBhcmFtcy5pZH0pO1xuXHRcdH1cblx0fVxuXG5cdGNvbXBvbmVudERpZE1vdW50KCkge1xuXG5cdFx0aWYgKHRoaXMucHJvcHMucGFyYW1zLmlkKSB7XG5cdFx0XHR0aGlzLnByb3BzLm9uU2VsZWN0KHtkb21haW46IHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24sIGlkOiB0aGlzLnByb3BzLnBhcmFtcy5pZH0pO1xuXHRcdH0gZWxzZSBpZiAoIXRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24gJiYgIXRoaXMucHJvcHMubG9jYXRpb24ucGF0aG5hbWUubWF0Y2goL25ldyQvKSAmJiB0aGlzLnByb3BzLmVudGl0eS5kb21haW4pIHtcblx0XHRcdHRoaXMucHJvcHMub25SZWRpcmVjdFRvRmlyc3QodGhpcy5wcm9wcy5lbnRpdHkuZG9tYWluKVxuXHRcdH0gZWxzZSBpZiAodGhpcy5wcm9wcy5sb2NhdGlvbi5wYXRobmFtZS5tYXRjaCgvbmV3JC8pKSB7XG5cdFx0XHR0aGlzLnByb3BzLm9uTmV3KHRoaXMucHJvcHMuZW50aXR5LmRvbWFpbik7XG5cdFx0fVxuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgb25TZWxlY3QsIG9uTmV3LCBvblNhdmUsIG9uRGVsZXRlLCBvblNlbGVjdERvbWFpbiwgb25EaXNtaXNzTWVzc2FnZSwgb25DaGFuZ2UsIG9uQWRkU2VsZWN0ZWRGaWVsZHMsIG9uUmVkaXJlY3RUb0ZpcnN0IH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlLCBvblF1aWNrU2VhcmNoLCBvblBhZ2luYXRlTGVmdCwgb25QYWdpbmF0ZVJpZ2h0IH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IHsgcXVpY2tTZWFyY2gsIGVudGl0eSwgdnJlLCBtZXNzYWdlcyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBjdXJyZW50TW9kZSA9IGVudGl0eS5kb21haW4gJiYgZW50aXR5LmRhdGEuX2lkID8gXCJlZGl0XCIgOiBcIm5ld1wiO1xuXG5cdFx0aWYgKGVudGl0eS5kb21haW4gPT09IG51bGwgfHwgIXZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXSkgeyByZXR1cm4gbnVsbDsgfVxuXHRcdHJldHVybiAoXG5cdFx0XHQ8UGFnZT5cblx0XHRcdFx0PENvbGxlY3Rpb25UYWJzIGNvbGxlY3Rpb25zPXt2cmUuY29sbGVjdGlvbnN9IG9uTmV3PXtvbk5ld30gb25TZWxlY3REb21haW49e29uU2VsZWN0RG9tYWlufSBvblJlZGlyZWN0VG9GaXJzdD17b25SZWRpcmVjdFRvRmlyc3R9XG5cdFx0XHRcdFx0YWN0aXZlRG9tYWluPXtlbnRpdHkuZG9tYWlufSAvPlxuXHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuXHRcdFx0XHRcdDxNZXNzYWdlc1xuXHRcdFx0XHRcdFx0dHlwZXM9e1tcIlNVQ0NFU1NfTUVTU0FHRVwiLCBcIkVSUk9SX01FU1NBR0VcIl19XG5cdFx0XHRcdFx0XHRtZXNzYWdlcz17bWVzc2FnZXN9XG5cdFx0XHRcdFx0XHRvbkRpc21pc3NNZXNzYWdlPXtvbkRpc21pc3NNZXNzYWdlfSAvPlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG5cdFx0XHRcdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC00XCI+XG5cdFx0XHRcdFx0XHRcdDxRdWlja1NlYXJjaFxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZT17b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlfVxuXHRcdFx0XHRcdFx0XHRcdG9uUXVpY2tTZWFyY2g9e29uUXVpY2tTZWFyY2h9XG5cdFx0XHRcdFx0XHRcdFx0cXVlcnk9e3F1aWNrU2VhcmNoLnF1ZXJ5fSAvPlxuXHRcdFx0XHRcdFx0XHQ8RW50aXR5TGlzdFxuXHRcdFx0XHRcdFx0XHRcdHN0YXJ0PXtxdWlja1NlYXJjaC5zdGFydH1cblx0XHRcdFx0XHRcdFx0XHRsaXN0PXtxdWlja1NlYXJjaC5saXN0fVxuXHRcdFx0XHRcdFx0XHRcdG9uU2VsZWN0PXtvblNlbGVjdH1cblx0XHRcdFx0XHRcdFx0XHRkb21haW49e2VudGl0eS5kb21haW59XG5cdFx0XHRcdFx0XHRcdFx0c2VsZWN0ZWRJZD17ZW50aXR5LmRhdGEuX2lkfVxuXHRcdFx0XHRcdFx0XHRcdGVudGl0eVBlbmRpbmc9e2VudGl0eS5wZW5kaW5nfVxuXHRcdFx0XHRcdFx0XHQvPlxuXHRcdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0XHR7ZW50aXR5LnBlbmRpbmcgPyAoXG5cdFx0XHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+TG9hZGluZywgcGxlYXNlIHdhaXQuLi48L2Rpdj5cblx0XHRcdFx0XHRcdCkgOiBlbnRpdHkuZG9tYWluID8gKFxuXHRcdFx0XHRcdFx0XHQ8RW50aXR5Rm9ybSBjdXJyZW50TW9kZT17Y3VycmVudE1vZGV9IGdldEF1dG9jb21wbGV0ZVZhbHVlcz17Z2V0QXV0b2NvbXBsZXRlVmFsdWVzfVxuXHRcdFx0XHRcdFx0XHRcdG9uQWRkU2VsZWN0ZWRGaWVsZHM9e29uQWRkU2VsZWN0ZWRGaWVsZHN9XG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5PXtlbnRpdHl9IG9uTmV3PXtvbk5ld30gb25EZWxldGU9e29uRGVsZXRlfSBvbkNoYW5nZT17b25DaGFuZ2V9XG5cdFx0XHRcdFx0XHRcdFx0cHJvcGVydGllcz17dnJlLmNvbGxlY3Rpb25zW2VudGl0eS5kb21haW5dLnByb3BlcnRpZXN9IFxuXHRcdFx0XHRcdFx0XHRcdGVudGl0eUxhYmVsPXt2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0uY29sbGVjdGlvbkxhYmVsLnJlcGxhY2UoL3MkLywgXCJcIikgfSAvPlxuXHRcdFx0XHRcdFx0KSA6IG51bGwgfVxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8L2Rpdj5cblxuXHRcdFx0XHQ8ZGl2IHR5cGU9XCJmb290ZXItYm9keVwiIGNsYXNzTmFtZT1cInJvd1wiPlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLTRcIiBzdHlsZT17e3RleHRBbGlnbjogXCJsZWZ0XCIsIHBhZGRpbmc6ICcwJ319PlxuXHRcdFx0XHRcdFx0PFBhZ2luYXRlXG5cdFx0XHRcdFx0XHRcdHN0YXJ0PXtxdWlja1NlYXJjaC5zdGFydH1cblx0XHRcdFx0XHRcdFx0bGlzdExlbmd0aD17cXVpY2tTZWFyY2gubGlzdC5sZW5ndGh9XG5cdFx0XHRcdFx0XHRcdHJvd3M9ezUwfVxuXHRcdFx0XHRcdFx0XHRvblBhZ2luYXRlTGVmdD17b25QYWdpbmF0ZUxlZnR9XG5cdFx0XHRcdFx0XHRcdG9uUGFnaW5hdGVSaWdodD17b25QYWdpbmF0ZVJpZ2h0fSAvPlxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLThcIiBzdHlsZT17e3RleHRBbGlnbjogXCJsZWZ0XCIsIHBhZGRpbmc6ICcwJ319PlxuXHRcdFx0XHRcdFx0eyFlbnRpdHkucGVuZGluZyA/XG5cdFx0XHRcdFx0XHRcdDxTYXZlRm9vdGVyIG9uU2F2ZT17b25TYXZlfSBvbkNhbmNlbD17KCkgPT4gY3VycmVudE1vZGUgPT09IFwiZWRpdFwiID9cblx0XHRcdFx0XHRcdFx0XHRvblNlbGVjdCh7ZG9tYWluOiBlbnRpdHkuZG9tYWluLCBpZDogZW50aXR5LmRhdGEuX2lkfSkgOiBvbk5ldyhlbnRpdHkuZG9tYWluKX0vPiA6IG51bGxcblx0XHRcdFx0XHRcdH1cblx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0PC9kaXY+XG5cdFx0XHQ8L1BhZ2U+XG5cdFx0KVxuXHR9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVkaXRHdWk7XG4iLCJleHBvcnQgZGVmYXVsdCAoY2FtZWxDYXNlKSA9PiBjYW1lbENhc2VcbiAgLnJlcGxhY2UoLyhbQS1aMC05XSkvZywgKG1hdGNoKSA9PiBgICR7bWF0Y2gudG9Mb3dlckNhc2UoKX1gKVxuICAucmVwbGFjZSgvXi4vLCAobWF0Y2gpID0+IG1hdGNoLnRvVXBwZXJDYXNlKCkpO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0geyBuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwiIH07XG5cdH1cblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGlmIChuZXh0UHJvcHMuZW50aXR5LmRhdGEuX2lkICE9PSB0aGlzLnByb3BzLmVudGl0eS5kYXRhLl9pZCkge1xuXHRcdFx0dGhpcy5zZXRTdGF0ZSh7bmV3TGFiZWw6IFwiXCIsIG5ld1VybDogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGlmICh0aGlzLnN0YXRlLm5ld0xhYmVsLmxlbmd0aCA+IDAgJiYgdGhpcy5zdGF0ZS5uZXdVcmwubGVuZ3RoID4gMCkge1xuXHRcdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh7XG5cdFx0XHRcdGxhYmVsOiB0aGlzLnN0YXRlLm5ld0xhYmVsLFxuXHRcdFx0XHR1cmw6IHRoaXMuc3RhdGUubmV3VXJsXG5cdFx0XHR9KSk7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwifSk7XG5cdFx0fVxuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXVxuXHRcdFx0LmZpbHRlcigodmFsKSA9PiB2YWwudXJsICE9PSB2YWx1ZS51cmwpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWUudXJsfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz5cblx0XHRcdFx0XHQ8YSBocmVmPXt2YWx1ZS51cmx9IHRhcmdldD1cIl9ibGFua1wiPlxuXHRcdFx0XHRcdFx0e3ZhbHVlLmxhYmVsfVxuXHRcdFx0XHRcdDwvYT5cblx0XHRcdFx0PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCJ9fT5cblx0XHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2wgcHVsbC1sZWZ0XCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3TGFiZWx9XG5cdFx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIkxhYmVsIGZvciB1cmwuLi5cIlxuXHRcdFx0XHRcdFx0c3R5bGU9e3tkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCBtYXhXaWR0aDogXCI1MCVcIn19IC8+XG5cdFx0XHRcdFx0PGlucHV0IHR5cGU9XCJ0ZXh0XCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sIHB1bGwtbGVmdFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld1VybH1cblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld1VybDogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0XHRvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gdGhpcy5vbkFkZCgpIDogZmFsc2V9XG5cdFx0XHRcdFx0XHRwbGFjZWhvbGRlcj1cIlVybC4uLlwiXG5cdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIG1heFdpZHRoOiBcImNhbGMoNTAlIC0gODBweClcIn19IC8+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuIHB1bGwtbGVmdFwiPlxuXHRcdFx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9PkFkZCBsaW5rPC9idXR0b24+XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHQ8L2Rpdj5cblxuXHRcdFx0XHQ8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBjbGVhcjogXCJsZWZ0XCJ9fSAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdGNvbnN0cnVjdG9yKHByb3BzKSB7XG5cdFx0c3VwZXIocHJvcHMpO1xuXG5cdFx0dGhpcy5zdGF0ZSA9IHsgbmV3VmFsdWU6IFwiXCIgfTtcblx0fVxuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0aWYgKG5leHRQcm9wcy5lbnRpdHkuZGF0YS5faWQgIT09IHRoaXMucHJvcHMuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogXCJcIn0pXG5cdFx0fVxuXHR9XG5cblx0b25BZGQodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh2YWx1ZSkpO1xuXHR9XG5cblx0b25SZW1vdmUodmFsdWUpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0b25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKHZhbCkgPT4gdmFsICE9PSB2YWx1ZSkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZX0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e3ZhbHVlfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnRzfVxuXHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdWYWx1ZX1cblx0XHRcdFx0XHRvbkNoYW5nZT17KGV2KSA9PiB0aGlzLnNldFN0YXRlKHtuZXdWYWx1ZTogZXYudGFyZ2V0LnZhbHVlfSl9XG5cdFx0XHRcdFx0b25LZXlQcmVzcz17KGV2KSA9PiBldi5rZXkgPT09IFwiRW50ZXJcIiA/IHRoaXMub25BZGQoZXYudGFyZ2V0LnZhbHVlKSA6IGZhbHNlfVxuXHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiQWRkIGEgdmFsdWUuLi5cIiAvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRvbkFkZCh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHZhbHVlKSk7XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigodmFsKSA9PiB2YWwgIT09IHZhbHVlKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57dmFsdWV9PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxTZWxlY3RGaWVsZCBvbkNoYW5nZT17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfSBub0NsZWFyPXt0cnVlfSBidG5DbGFzcz1cImJ0bi1kZWZhdWx0XCI+XG5cdFx0XHRcdFx0PHNwYW4gdHlwZT1cInBsYWNlaG9sZGVyXCI+XG5cdFx0XHRcdFx0XHRTZWxlY3Qge2xhYmVsLnRvTG93ZXJDYXNlKCl9XG5cdFx0XHRcdFx0PC9zcGFuPlxuXHRcdFx0XHRcdHtvcHRpb25zLmZpbHRlcigob3B0KSA9PiB2YWx1ZXMuaW5kZXhPZihvcHQpIDwgMCkubWFwKChvcHRpb24pID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9TZWxlY3RGaWVsZD5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5XG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBvbkFkZCgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQoe1xuICAgICAgY29tcG9uZW50czogW3t0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn1dXG4gICAgfSkpO1xuICB9XG5cbiAgb25BZGRDb21wb25lbnQoaXRlbUluZGV4KSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAuY29uY2F0KHt0eXBlOiBvcHRpb25zWzBdLCB2YWx1ZTogXCJcIn0pXG4gICAgKTtcbiAgfVxuXG4gIG9uUmVtb3ZlQ29tcG9uZW50KGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5maWx0ZXIoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggIT09IGNvbXBvbmVudEluZGV4KVxuICAgICk7XG4gIH1cblxuICBvbkNoYW5nZUNvbXBvbmVudFZhbHVlKGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgsIHZhbHVlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHZhbHVlOiB2YWx1ZX0gOiBjb21wb25lbnRcbiAgICApKTtcbiAgfVxuXG4gIG9uQ2hhbmdlQ29tcG9uZW50VHlwZShpdGVtSW5kZXgsIGNvbXBvbmVudEluZGV4LCB0eXBlKSB7XG4gICAgY29uc3QgeyBlbnRpdHksIG5hbWUsICBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBjdXJyZW50Q29tcG9uZW50cyA9IGVudGl0eS5kYXRhW25hbWVdW2l0ZW1JbmRleF0uY29tcG9uZW50cztcbiAgICBvbkNoYW5nZShbbmFtZSwgaXRlbUluZGV4LCBcImNvbXBvbmVudHNcIl0sIGN1cnJlbnRDb21wb25lbnRzXG4gICAgICAubWFwKChjb21wb25lbnQsIGlkeCkgPT4gaWR4ID09PSBjb21wb25lbnRJbmRleFxuICAgICAgICA/IHsuLi5jb21wb25lbnQsIHR5cGU6IHR5cGV9IDogY29tcG9uZW50XG4gICAgKSk7XG4gIH1cblxuICBvblJlbW92ZShpdGVtSW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKChuYW1lLCBpZHgpID0+IGlkeCAhPT0gaXRlbUluZGV4KSk7XG4gIH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXG4gICAgY29uc3QgbmFtZUVsZW1lbnRzID0gdmFsdWVzLm1hcCgobmFtZSwgaSkgPT4gKFxuICAgICAgPGRpdiBrZXk9e2Ake25hbWV9LSR7aX1gfSBjbGFzc05hbWU9XCJuYW1lcy1mb3JtIGl0ZW0tZWxlbWVudFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInNtYWxsLW1hcmdpblwiPlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG4gICAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKGkpfVxuICAgICAgICAgICAgdHlwZT1cImJ1dHRvblwiPlxuICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgIDxzdHJvbmc+XG4gICAgICAgICAgICB7bmFtZS5jb21wb25lbnRzLm1hcCgoY29tcG9uZW50KSA9PiBjb21wb25lbnQudmFsdWUpLmpvaW4oXCIgXCIpfVxuICAgICAgICAgIDwvc3Ryb25nPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPHVsIGtleT1cImNvbXBvbmVudC1saXN0XCI+XG4gICAgICAgICAge25hbWUuY29tcG9uZW50cy5tYXAoKGNvbXBvbmVudCwgaikgPT4gKFxuICAgICAgICAgICAgPGxpIGtleT17YCR7aX0tJHtqfS1jb21wb25lbnRgfT5cbiAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cFwiIGtleT1cImNvbXBvbmVudC12YWx1ZXNcIj5cbiAgICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICAgICAgICAgICAgPFNlbGVjdEZpZWxkIHZhbHVlPXtjb21wb25lbnQudHlwZX0gbm9DbGVhcj17dHJ1ZX1cbiAgICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyh2YWwpID0+IHRoaXMub25DaGFuZ2VDb21wb25lbnRUeXBlKGksIGosIHZhbCl9XG4gICAgICAgICAgICAgICAgICAgIGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cbiAgICAgICAgICAgICAgICAgICAge29wdGlvbnMubWFwKChvcHRpb24pID0+IChcbiAgICAgICAgICAgICAgICAgICAgICA8c3BhbiB2YWx1ZT17b3B0aW9ufSBrZXk9e29wdGlvbn0+e29wdGlvbn08L3NwYW4+XG4gICAgICAgICAgICAgICAgICAgICkpfVxuICAgICAgICAgICAgICAgICAgPC9TZWxlY3RGaWVsZD5cbiAgICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgICAgICA8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIiBrZXk9e2BpbnB1dC0ke2l9LSR7an1gfVxuICAgICAgICAgICAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gdGhpcy5vbkNoYW5nZUNvbXBvbmVudFZhbHVlKGksIGosIGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcj17Y29tcG9uZW50LnR5cGV9IHZhbHVlPXtjb21wb25lbnQudmFsdWV9IC8+XG4gICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgICAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmVDb21wb25lbnQoaSwgail9ID5cbiAgICAgICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgICAgICAgPC9zcGFuPlxuICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICAgICAgPGJ1dHRvbiBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uQWRkQ29tcG9uZW50KGkpfVxuICAgICAgICAgICAgIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdCBidG4teHMgcHVsbC1yaWdodFwiIHR5cGU9XCJidXR0b25cIj5cbiAgICAgICAgICAgIEFkZCBjb21wb25lbnRcbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICA8ZGl2IHN0eWxlPXt7d2lkdGg6IFwiMTAwJVwiLCBoZWlnaHQ6IFwiNnB4XCIsIGNsZWFyOiBcInJpZ2h0XCJ9fSAvPlxuICAgICAgPC9kaXY+XG4gICAgKSlcblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuICAgICAgICB7bmFtZUVsZW1lbnRzfVxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e3RoaXMub25BZGQuYmluZCh0aGlzKX0+XG4gICAgICAgICAgQWRkIG5hbWVcbiAgICAgICAgPC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXksXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQgeyB1cmxzIH0gZnJvbSBcIi4uLy4uLy4uLy4uL3VybHNcIjtcblxuY2xhc3MgUmVsYXRpb25GaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIHF1ZXJ5OiBcIlwiLFxuICAgICAgc3VnZ2VzdGlvbnM6IFtdLFxuICAgICAgYmx1cklzQmxvY2tlZDogZmFsc2VcbiAgICB9XG4gIH1cblxuICBvblJlbW92ZSh2YWx1ZSkge1xuICAgIGNvbnN0IGN1cnJlbnRWYWx1ZXMgPSB0aGlzLnByb3BzLmVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuXG4gICAgdGhpcy5wcm9wcy5vbkNoYW5nZShcbiAgICAgIFtcIkByZWxhdGlvbnNcIiwgdGhpcy5wcm9wcy5uYW1lXSxcbiAgICAgIGN1cnJlbnRWYWx1ZXMuZmlsdGVyKChjdXJWYWwpID0+IGN1clZhbC5pZCAhPT0gdmFsdWUuaWQpXG4gICAgKTtcblxuICB9XG5cbiAgb25BZGQoc3VnZ2VzdGlvbikge1xuICAgIGNvbnN0IGN1cnJlbnRWYWx1ZXMgPSB0aGlzLnByb3BzLmVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXVt0aGlzLnByb3BzLm5hbWVdIHx8IFtdO1xuICAgIGlmIChjdXJyZW50VmFsdWVzLm1hcCgodmFsKSA9PiB2YWwuaWQpLmluZGV4T2Yoc3VnZ2VzdGlvbi5rZXkpID4gLTEpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG4gICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IFtdLCBxdWVyeTogXCJcIiwgYmx1cklzQmxvY2tlZDogZmFsc2V9KTtcblxuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoXG4gICAgICBbXCJAcmVsYXRpb25zXCIsIHRoaXMucHJvcHMubmFtZV0sXG4gICAgICBjdXJyZW50VmFsdWVzLmNvbmNhdCh7XG4gICAgICAgIGlkOiBzdWdnZXN0aW9uLmtleSxcbiAgICAgICAgZGlzcGxheU5hbWU6IHN1Z2dlc3Rpb24udmFsdWUsXG4gICAgICAgIGFjY2VwdGVkOiB0cnVlXG4gICAgICB9KVxuICAgICk7XG4gIH1cblxuICBvblF1ZXJ5Q2hhbmdlKGV2KSB7XG4gICAgY29uc3QgeyBnZXRBdXRvY29tcGxldGVWYWx1ZXMsIHBhdGggfSA9IHRoaXMucHJvcHM7XG4gICAgdGhpcy5zZXRTdGF0ZSh7cXVlcnk6IGV2LnRhcmdldC52YWx1ZX0pO1xuICAgIGlmIChldi50YXJnZXQudmFsdWUgPT09IFwiXCIpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXX0pO1xuICAgIH0gZWxzZSB7XG4gICAgICBnZXRBdXRvY29tcGxldGVWYWx1ZXMocGF0aCwgZXYudGFyZ2V0LnZhbHVlLCAocmVzdWx0cykgPT4ge1xuICAgICAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogcmVzdWx0c30pO1xuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgb25RdWVyeUNsZWFyKGV2KSB7XG4gICAgaWYgKCF0aGlzLnN0YXRlLmJsdXJJc0Jsb2NrZWQpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiBbXSwgcXVlcnk6IFwiXCJ9KTtcbiAgICB9XG4gIH1cblxuICBvbkJsdXJCbG9jayh0b2dnbGUpIHtcbiAgICB0aGlzLnNldFN0YXRlKHtibHVySXNCbG9ja2VkOiB0b2dnbGV9KTtcbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UsIHRhcmdldENvbGxlY3Rpb24gfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgdmFsdWVzID0gZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdW3RoaXMucHJvcHMubmFtZV0gfHwgW107XG4gICAgY29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLmZpbHRlcigodmFsKSA9PiB2YWwuYWNjZXB0ZWQpLm1hcCgodmFsdWUsIGkpID0+IChcbiAgICAgIDxkaXYga2V5PXtgJHtpfS0ke3ZhbHVlLmlkfWB9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuICAgICAgICA8TGluayB0bz17dXJscy5lbnRpdHkodGFyZ2V0Q29sbGVjdGlvbiwgdmFsdWUuaWQpfSA+e3ZhbHVlLmRpc3BsYXlOYW1lfTwvTGluaz5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcbiAgICAgICAgICBvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgIDwvZGl2PlxuICAgICkpO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgIDxoND57Y2FtZWwybGFiZWwobmFtZSl9PC9oND5cbiAgICAgICAge2l0ZW1FbGVtZW50c31cbiAgICAgICAgPGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG4gICAgICAgICAgICAgICBvbkJsdXI9e3RoaXMub25RdWVyeUNsZWFyLmJpbmQodGhpcyl9XG4gICAgICAgICAgICAgICBvbkNoYW5nZT17dGhpcy5vblF1ZXJ5Q2hhbmdlLmJpbmQodGhpcyl9XG4gICAgICAgICAgICAgICB2YWx1ZT17dGhpcy5zdGF0ZS5xdWVyeX0gcGxhY2Vob2xkZXI9XCJTZWFyY2guLi5cIiAvPlxuXG4gICAgICAgIDxkaXYgb25Nb3VzZU92ZXI9eygpID0+IHRoaXMub25CbHVyQmxvY2sodHJ1ZSl9XG4gICAgICAgICAgICAgb25Nb3VzZU91dD17KCkgPT4gdGhpcy5vbkJsdXJCbG9jayhmYWxzZSl9XG4gICAgICAgICAgICAgc3R5bGU9e3tvdmVyZmxvd1k6IFwiYXV0b1wiLCBtYXhIZWlnaHQ6IFwiMzAwcHhcIn19PlxuICAgICAgICAgIHt0aGlzLnN0YXRlLnN1Z2dlc3Rpb25zLm1hcCgoc3VnZ2VzdGlvbiwgaSkgPT4gKFxuICAgICAgICAgICAgPGEga2V5PXtgJHtpfS0ke3N1Z2dlc3Rpb24ua2V5fWB9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiXG4gICAgICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHRoaXMub25BZGQoc3VnZ2VzdGlvbil9PlxuICAgICAgICAgICAgICB7c3VnZ2VzdGlvbi52YWx1ZX1cbiAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IFJlbGF0aW9uRmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcbmltcG9ydCBTZWxlY3RGaWVsZCBmcm9tIFwiLi4vLi4vLi4vZmllbGRzL3NlbGVjdC1maWVsZFwiO1xuXG5jbGFzcyBGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudCA9IGVudGl0eS5kYXRhW25hbWVdICYmIGVudGl0eS5kYXRhW25hbWVdLmxlbmd0aCA+IDAgPyAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPntlbnRpdHkuZGF0YVtuYW1lXX08L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiBvbkNoYW5nZShbbmFtZV0sIFwiXCIpfT5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG5cdFx0XHRcdDwvYnV0dG9uPlxuXHRcdFx0PC9kaXY+XG5cdFx0KSA6IG51bGw7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cblx0XHRcdFx0PGg0PntsYWJlbH08L2g0PlxuXHRcdFx0XHR7aXRlbUVsZW1lbnR9XG5cdFx0XHRcdDxTZWxlY3RGaWVsZFxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsodmFsdWUpID0+IG9uQ2hhbmdlKFtuYW1lXSwgdmFsdWUpfVxuXHRcdFx0XHRcdG5vQ2xlYXI9e3RydWV9IGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cblx0XHRcdFx0XHQ8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIj5cblx0XHRcdFx0XHRcdFNlbGVjdCB7bGFiZWwudG9Mb3dlckNhc2UoKX1cblx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdFx0e29wdGlvbnMubWFwKChvcHRpb24pID0+IChcblx0XHRcdFx0XHRcdDxzcGFuIGtleT17b3B0aW9ufSB2YWx1ZT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cblx0XHRcdFx0XHQpKX1cblx0XHRcdFx0PC9TZWxlY3RGaWVsZD5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG5cdG9uQ2hhbmdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0b3B0aW9uczogUmVhY3QuUHJvcFR5cGVzLmFycmF5XG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuXG5jbGFzcyBTdHJpbmdGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdDxpbnB1dCBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2xcIlxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IG9uQ2hhbmdlKFtuYW1lXSwgZXYudGFyZ2V0LnZhbHVlKX1cblx0XHRcdFx0XHR2YWx1ZT17ZW50aXR5LmRhdGFbbmFtZV0gfHwgXCJcIn1cblx0XHRcdFx0XHRwbGFjZWhvbGRlcj17YEVudGVyICR7bGFiZWwudG9Mb3dlckNhc2UoKX1gfVxuXHRcdFx0XHQvPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5TdHJpbmdGaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTdHJpbmdGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIlxuXG5pbXBvcnQgU3RyaW5nRmllbGQgZnJvbSBcIi4vZmllbGRzL3N0cmluZy1maWVsZFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9zZWxlY3RcIjtcbmltcG9ydCBNdWx0aVNlbGVjdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9tdWx0aS1zZWxlY3RcIjtcbmltcG9ydCBSZWxhdGlvbkZpZWxkIGZyb20gXCIuL2ZpZWxkcy9yZWxhdGlvblwiO1xuaW1wb3J0IFN0cmluZ0xpc3RGaWVsZCBmcm9tIFwiLi9maWVsZHMvbGlzdC1vZi1zdHJpbmdzXCI7XG5pbXBvcnQgTGlua0ZpZWxkIGZyb20gXCIuL2ZpZWxkcy9saW5rc1wiO1xuaW1wb3J0IE5hbWVzRmllbGQgZnJvbSBcIi4vZmllbGRzL25hbWVzXCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHsgdXJscyB9IGZyb20gXCIuLi8uLi8uLi91cmxzXCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vZmllbGRzL2NhbWVsMmxhYmVsXCI7XG5cbmNvbnN0IGZpZWxkTWFwID0ge1xuXHRcInN0cmluZ1wiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcInRleHRcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJkYXRhYmxlXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwibXVsdGlzZWxlY3RcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxNdWx0aVNlbGVjdEZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gb3B0aW9ucz17ZmllbGREZWYub3B0aW9uc30gLz4pLFxuXHRcInNlbGVjdFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFNlbGVjdEZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gb3B0aW9ucz17ZmllbGREZWYub3B0aW9uc30gLz4pLFxuXHRcInJlbGF0aW9uXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8UmVsYXRpb25GaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IHRhcmdldENvbGxlY3Rpb249e2ZpZWxkRGVmLnJlbGF0aW9uLnRhcmdldENvbGxlY3Rpb259IHBhdGg9e2ZpZWxkRGVmLnF1aWNrc2VhcmNofSAvPiksXG4gIFwibGlzdC1vZi1zdHJpbmdzXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nTGlzdEZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuICBcImxpbmtzXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8TGlua0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcIm5hbWVzXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8TmFtZXNGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IG9wdGlvbnM9e2ZpZWxkRGVmLm9wdGlvbnN9IC8+KVxufTtcblxuY29uc3QgYXBwbHlGaWx0ZXIgPSAoZmllbGQsIGZpbHRlcikgPT5cbiAgICBmaWVsZC50b0xvd2VyQ2FzZSgpLmluZGV4T2YoZmlsdGVyLnRvTG93ZXJDYXNlKCkpID4gLTEgfHxcbiAgICBjYW1lbDJsYWJlbChmaWVsZCkudG9Mb3dlckNhc2UoKS5pbmRleE9mKGZpbHRlci50b0xvd2VyQ2FzZSgpKSA+IC0xO1xuXG5jbGFzcyBFbnRpdHlGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBjb25zdHJ1Y3Rvcihwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBmaWVsZHNUb0FkZDogW10sXG4gICAgICBhZGRGaWVsZEZpbHRlcjogXCJcIlxuICAgIH1cbiAgfVxuXG4gIG9uRmlsdGVyQ2hhbmdlKGV2KSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7YWRkRmllbGRGaWx0ZXI6IGV2LnRhcmdldC52YWx1ZX0sICgpID0+IHtcbiAgICAgIGNvbnN0IGZpbHRlcmVkID0gdGhpcy5nZXRBZGRhYmxlRmllbGRzRnJvbVByb3BlcnRpZXMoKS5maWx0ZXIocHJvcCA9PiBhcHBseUZpbHRlcihwcm9wLm5hbWUsIHRoaXMuc3RhdGUuYWRkRmllbGRGaWx0ZXIpKTtcbiAgICAgIGlmIChmaWx0ZXJlZC5sZW5ndGggPiAwKSB7XG4gICAgICAgIGlmICh0aGlzLnN0YXRlLmFkZEZpZWxkRmlsdGVyID09PSBcIlwiKSB7XG4gICAgICAgICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IFtdfSlcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB0aGlzLnNldFN0YXRlKHtmaWVsZHNUb0FkZDogW2ZpbHRlcmVkWzBdLm5hbWVdfSlcbiAgICAgICAgfVxuICAgICAgfVxuICAgIH0pO1xuICB9XG5cbiAgb25GaWx0ZXJLZXlEb3duKGV2KSB7XG4gICAgaWYgKGV2LmtleSA9PT0gXCJFbnRlclwiICYmIHRoaXMuc3RhdGUuZmllbGRzVG9BZGQubGVuZ3RoID4gMCkge1xuICAgICAgdGhpcy5vbkFkZFNlbGVjdGVkRmllbGRzKCk7XG4gICAgfVxuICB9XG5cbiAgdG9nZ2xlRmllbGRUb0FkZChmaWVsZE5hbWUpIHtcbiAgICBpZiAodGhpcy5zdGF0ZS5maWVsZHNUb0FkZC5pbmRleE9mKGZpZWxkTmFtZSkgPiAtMSkge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IHRoaXMuc3RhdGUuZmllbGRzVG9BZGQuZmlsdGVyKChmQWRkKSA9PiBmQWRkICE9PSBmaWVsZE5hbWUpfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2ZpZWxkc1RvQWRkOiB0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLmNvbmNhdChmaWVsZE5hbWUpfSk7XG4gICAgfVxuICB9XG5cbiAgb25BZGRTZWxlY3RlZEZpZWxkcygpIHtcbiAgICBjb25zdCB7IHByb3BlcnRpZXMgfSA9IHRoaXMucHJvcHM7XG5cbiAgICB0aGlzLnByb3BzLm9uQWRkU2VsZWN0ZWRGaWVsZHModGhpcy5zdGF0ZS5maWVsZHNUb0FkZC5tYXAoKGZBZGQpID0+ICh7XG4gICAgICBuYW1lOiBmQWRkLFxuICAgICAgdHlwZTogcHJvcGVydGllcy5maW5kKChwcm9wKSA9PiBwcm9wLm5hbWUgPT09IGZBZGQpLnR5cGVcbiAgICB9KSkpO1xuXG4gICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IFtdLCBhZGRGaWVsZEZpbHRlcjogXCJcIn0pO1xuICB9XG5cbiAgZ2V0QWRkYWJsZUZpZWxkc0Zyb21Qcm9wZXJ0aWVzKCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBwcm9wZXJ0aWVzIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgcmV0dXJuIHByb3BlcnRpZXNcbiAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiAhZW50aXR5LmRhdGEuaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkgJiYgIWVudGl0eS5kYXRhW1wiQHJlbGF0aW9uc1wiXS5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi5uYW1lKSlcblxuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25EZWxldGUsIG9uQ2hhbmdlLCBnZXRBdXRvY29tcGxldGVWYWx1ZXMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBlbnRpdHksIGN1cnJlbnRNb2RlLCBwcm9wZXJ0aWVzLCBlbnRpdHlMYWJlbCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGZpZWxkc1RvQWRkLCBhZGRGaWVsZEZpbHRlciB9ID0gdGhpcy5zdGF0ZTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC04XCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAgPExpbmsgdG89e3VybHMubmV3RW50aXR5KGVudGl0eS5kb21haW4pfSBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnkgcHVsbC1yaWdodFwiPlxuICAgICAgICAgICAgTmV3IHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICA8L0xpbms+XG4gICAgICAgIDwvZGl2PlxuXG5cbiAgICAgICAge3Byb3BlcnRpZXNcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gIWZpZWxkTWFwLmhhc093blByb3BlcnR5KGZpZWxkRGVmLnR5cGUpKVxuICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PiAoPGRpdiBrZXk9e2l9IHN0eWxlPXt7XCJjb2xvclwiOiBcInJlZFwifX0+PHN0cm9uZz5GaWVsZCB0eXBlIG5vdCBzdXBwb3J0ZWQ6IHtmaWVsZERlZi50eXBlfTwvc3Ryb25nPjwvZGl2PikpfVxuXG4gICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+IGZpZWxkTWFwLmhhc093blByb3BlcnR5KGZpZWxkRGVmLnR5cGUpKVxuICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBlbnRpdHkuZGF0YS5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi5uYW1lKSB8fCBlbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl0uaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkpXG4gICAgICAgICAgLm1hcCgoZmllbGREZWYsIGkpID0+XG4gICAgICAgICAgZmllbGRNYXBbZmllbGREZWYudHlwZV0oZmllbGREZWYsIHtcblx0XHRcdFx0XHRcdGtleTogYCR7aX0tJHtmaWVsZERlZi5uYW1lfWAsXG5cdFx0XHRcdFx0XHRlbnRpdHk6IGVudGl0eSxcblx0XHRcdFx0XHRcdG9uQ2hhbmdlOiBvbkNoYW5nZSxcblx0XHRcdFx0XHRcdGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzXG5cdFx0XHRcdFx0fSlcbiAgICAgICAgKX1cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpbiBhZGQtZmllbGQtZm9ybVwiPlxuICAgICAgICAgIDxoND5BZGQgZmllbGRzPC9oND5cbiAgICAgICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIgdmFsdWU9e2FkZEZpZWxkRmlsdGVyfSBwbGFjZWhvbGRlcj1cIkZpbHRlci4uLlwiXG4gICAgICAgICAgICAgICAgIG9uQ2hhbmdlPXt0aGlzLm9uRmlsdGVyQ2hhbmdlLmJpbmQodGhpcyl9XG4gICAgICAgICAgICAgICAgIG9uS2V5UHJlc3M9e3RoaXMub25GaWx0ZXJLZXlEb3duLmJpbmQodGhpcyl9XG4gICAgICAgICAgLz5cbiAgICAgICAgICA8ZGl2IHN0eWxlPXt7bWF4SGVpZ2h0OiBcIjI1MHB4XCIsIG92ZXJmbG93WTogXCJhdXRvXCJ9fT5cbiAgICAgICAgICAgIHt0aGlzLmdldEFkZGFibGVGaWVsZHNGcm9tUHJvcGVydGllcygpXG4gICAgICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBhcHBseUZpbHRlcihmaWVsZERlZi5uYW1lLCBhZGRGaWVsZEZpbHRlcikpXG4gICAgICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PiAoXG4gICAgICAgICAgICAgICAgPGRpdiBrZXk9e2l9IG9uQ2xpY2s9eygpID0+IHRoaXMudG9nZ2xlRmllbGRUb0FkZChmaWVsZERlZi5uYW1lKX1cbiAgICAgICAgICAgICAgICAgICAgIGNsYXNzTmFtZT17ZmllbGRzVG9BZGQuaW5kZXhPZihmaWVsZERlZi5uYW1lKSA+IC0xID8gXCJzZWxlY3RlZFwiIDogXCJcIn0+XG4gICAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJwdWxsLXJpZ2h0XCI+KHtmaWVsZERlZi50eXBlfSk8L3NwYW4+XG4gICAgICAgICAgICAgICAgICB7Y2FtZWwybGFiZWwoZmllbGREZWYubmFtZSl9XG4gICAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgICkpXG4gICAgICAgICAgICB9XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBvbkNsaWNrPXt0aGlzLm9uQWRkU2VsZWN0ZWRGaWVsZHMuYmluZCh0aGlzKX0+QWRkIHNlbGVjdGVkIGZpZWxkczwvYnV0dG9uPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAge2N1cnJlbnRNb2RlID09PSBcImVkaXRcIlxuICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAgICAgIDxoND5EZWxldGU8L2g0PlxuICAgICAgICAgICAgICA8YnV0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kYW5nZXJcIiBvbkNsaWNrPXtvbkRlbGV0ZX0+XG4gICAgICAgICAgICAgICAgRGVsZXRlIHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICAgICAgPC9idXRvbj5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IEVudGl0eUZvcm07XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25TYXZlLCBvbkNhbmNlbCB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2PlxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnlcIiBvbkNsaWNrPXtvblNhdmV9PlNhdmU8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn1vcntcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1saW5rXCIgb25DbGljaz17b25DYW5jZWx9PkNhbmNlbDwvYnV0dG9uPlxuICAgIDwvZGl2PlxuICApO1xufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vdXJsc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IHN0YXJ0LCBsaXN0LCBkb21haW4sIHNlbGVjdGVkSWQsIGVudGl0eVBlbmRpbmcgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJyZXN1bHQtbGlzdCByZXN1bHQtbGlzdC1lZGl0XCI+XG4gICAgICA8b2wgc3RhcnQ9e3N0YXJ0ICsgMX0gc3R5bGU9e3tjb3VudGVyUmVzZXQ6IGBzdGVwLWNvdW50ZXIgJHtzdGFydH1gfX0+XG4gICAgICAgIHtsaXN0Lm1hcCgoZW50cnksIGkpID0+IChcbiAgICAgICAgICA8bGkga2V5PXtgJHtpfS0ke2VudHJ5Ll9pZH1gfT5cbiAgICAgICAgICAgIHtlbnRpdHlQZW5kaW5nXG4gICAgICAgICAgICAgID8gKFxuICAgICAgICAgICAgICAgIDxhIHN0eWxlPXt7XG4gICAgICAgICAgICAgICAgICBkaXNwbGF5OiBcImlubGluZS1ibG9ja1wiLCB3aWR0aDogXCJjYWxjKDEwMCUgLSAzMHB4KVwiLCBoZWlnaHQ6IFwiMTAwJVwiLCBwYWRkaW5nOiBcIjAuNWVtIDBcIixcbiAgICAgICAgICAgICAgICAgIGN1cnNvcjogXCJkZWZhdWx0XCIsIG9wYWNpdHk6IFwiMC41XCIsIHRleHREZWNvcmF0aW9uOiBcIm5vbmVcIiwgZm9udFdlaWdodDogXCIzMDBcIlxuICAgICAgICAgICAgICAgIH19PlxuICAgICAgICAgICAgICAgICAge2VudHJ5W1wiQGRpc3BsYXlOYW1lXCJdfVxuICAgICAgICAgICAgICAgIDwvYT5cbiAgICAgICAgICAgICAgKSA6IChcbiAgICAgICAgICAgICAgICA8TGluayB0bz17dXJscy5lbnRpdHkoZG9tYWluLCBlbnRyeS5faWQpfSBzdHlsZT17e1xuICAgICAgICAgICAgICAgICAgZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgd2lkdGg6IFwiY2FsYygxMDAlIC0gMzBweClcIiwgaGVpZ2h0OiBcIjEwMCVcIiwgcGFkZGluZzogXCIwLjVlbSAwXCIsXG4gICAgICAgICAgICAgICAgICBmb250V2VpZ2h0OiBzZWxlY3RlZElkID09PSBlbnRyeS5faWQgPyBcIjUwMFwiIDogXCIzMDBcIlxuICAgICAgICAgICAgICAgIH19PlxuXG4gICAgICAgICAgICAgICAgICB7ZW50cnlbXCJAZGlzcGxheU5hbWVcIl19XG4gICAgICAgICAgICAgICAgPC9MaW5rPlxuICAgICAgICAgICAgICApXG4gICAgICAgICAgICB9XG4gICAgICAgICAgPC9saT5cbiAgICAgICAgKSl9XG4gICAgICA8L29sPlxuICAgIDwvZGl2PlxuICApXG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25QYWdpbmF0ZUxlZnQsIG9uUGFnaW5hdGVSaWdodCB9ID0gcHJvcHM7XG4gIGNvbnN0IHsgc3RhcnQsIHJvd3MsIGxpc3RMZW5ndGggfSA9IHByb3BzO1xuXG5cblxuICByZXR1cm4gKFxuICAgIDxkaXY+XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIGRpc2FibGVkPXtzdGFydCA9PT0gMH0gb25DbGljaz17b25QYWdpbmF0ZUxlZnR9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWNoZXZyb24tbGVmdFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICAgIHtcIiBcIn17c3RhcnQgKyAxfSAtIHtzdGFydCArIHJvd3N9e1wiIFwifVxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17bGlzdExlbmd0aCA8IHJvd3N9IG9uQ2xpY2s9e29uUGFnaW5hdGVSaWdodH0+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tY2hldnJvbi1yaWdodFwiIC8+XG4gICAgICA8L2J1dHRvbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UsIG9uUXVpY2tTZWFyY2gsIHF1ZXJ5IH0gPSBwcm9wcztcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAgc21hbGwtbWFyZ2luIFwiPlxuICAgICAgPGlucHV0IHR5cGU9XCJ0ZXh0XCIgcGxhY2Vob2xkZXI9XCJTZWFyY2ggZm9yLi4uXCIgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgb25DaGFuZ2U9eyhldikgPT4gb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlKGV2LnRhcmdldC52YWx1ZSl9XG4gICAgICAgIG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyBvblF1aWNrU2VhcmNoKCkgOiBmYWxzZX1cbiAgICAgICAgdmFsdWU9e3F1ZXJ5fVxuICAgICAgICAvPlxuICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17b25RdWlja1NlYXJjaH0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1zZWFyY2hcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rXCIgb25DbGljaz17KCkgPT4geyBvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2UoXCJcIik7IG9uUXVpY2tTZWFyY2goKTsgfX0+XG4gICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cbiAgICAgIDwvc3Bhbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuaW1wb3J0IE1lc3NhZ2UgZnJvbSBcIi4uLy4uL21lc3NhZ2VcIjtcblxuY29uc3QgTEFCRUxTID0ge1xuXHRcIlNVQ0NFU1NfTUVTU0FHRVwiOiBcIlwiLFxuXHRcIkVSUk9SX01FU1NBR0VcIjogKFxuXHRcdDxzcGFuPlxuXHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1leGNsYW1hdGlvbi1zaWduXCIgLz4gV2FybmluZyFcblx0XHQ8L3NwYW4+XG5cdClcbn07XG5cbmNvbnN0IEFMRVJUX0xFVkVMUyA9IHtcblx0XCJTVUNDRVNTX01FU1NBR0VcIjogXCJpbmZvXCIsXG5cdFwiRVJST1JfTUVTU0FHRVwiOiBcImRhbmdlclwiXG59O1xuXG5jbGFzcyBNZXNzYWdlcyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG1lc3NhZ2VzLCB0eXBlcywgb25EaXNtaXNzTWVzc2FnZSB9ID0gdGhpcy5wcm9wcztcblxuXHRcdGNvbnN0IGZpbHRlcmVkTWVzc2FnZXMgPSBtZXNzYWdlcy5sb2dcblx0XHRcdC5tYXAoKG1zZywgaWR4KSA9PiAoe21lc3NhZ2U6IG1zZy5tZXNzYWdlLCBpbmRleDogaWR4LCB0eXBlOiBtc2cudHlwZSwgZGlzbWlzc2VkOiBtc2cuZGlzbWlzc2VkIH0pKVxuXHRcdFx0LmZpbHRlcigobXNnKSA9PiB0eXBlcy5pbmRleE9mKG1zZy50eXBlKSA+IC0xICYmICFtc2cuZGlzbWlzc2VkKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2PlxuXHRcdFx0XHR7ZmlsdGVyZWRNZXNzYWdlcy5tYXAoKG1zZykgPT4gKFxuXHRcdFx0XHRcdDxNZXNzYWdlIGtleT17bXNnLmluZGV4fVxuXHRcdFx0XHRcdFx0ZGlzbWlzc2libGU9e3RydWV9XG5cdFx0XHRcdFx0XHRhbGVydExldmVsPXtBTEVSVF9MRVZFTFNbbXNnLnR5cGVdfVxuXHRcdFx0XHRcdFx0b25DbG9zZU1lc3NhZ2U9eygpID0+IG9uRGlzbWlzc01lc3NhZ2UobXNnLmluZGV4KX0+XG5cdFx0XHRcdFx0XHQ8c3Ryb25nPntMQUJFTFNbbXNnLnR5cGVdfTwvc3Ryb25nPiA8c3Bhbj57bXNnLm1lc3NhZ2V9PC9zcGFuPlxuXHRcdFx0XHRcdDwvTWVzc2FnZT5cblx0XHRcdFx0KSl9XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbk1lc3NhZ2VzLnByb3BUeXBlcyA9IHtcblx0bWVzc2FnZXM6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG9uRGlzbWlzc01lc3NhZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWQsXG5cdHR5cGVzOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXkuaXNSZXF1aXJlZFxufTtcblxuZXhwb3J0IGRlZmF1bHQgTWVzc2FnZXM7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IGN4IGZyb20gXCJjbGFzc25hbWVzXCI7XG5cbmNsYXNzIFNlbGVjdEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgaXNPcGVuOiBmYWxzZVxuICAgIH07XG4gICAgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIgPSB0aGlzLmhhbmRsZURvY3VtZW50Q2xpY2suYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50KCkge1xuICAgIGRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQoKSB7XG4gICAgZG9jdW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcihcImNsaWNrXCIsIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyLCBmYWxzZSk7XG4gIH1cblxuICB0b2dnbGVTZWxlY3QoKSB7XG4gICAgaWYodGhpcy5zdGF0ZS5pc09wZW4pIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogZmFsc2V9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7aXNPcGVuOiB0cnVlfSk7XG4gICAgfVxuICB9XG5cbiAgaGFuZGxlRG9jdW1lbnRDbGljayhldikge1xuICAgIGNvbnN0IHsgaXNPcGVuIH0gPSB0aGlzLnN0YXRlO1xuICAgIGlmIChpc09wZW4gJiYgIVJlYWN0RE9NLmZpbmRET01Ob2RlKHRoaXMpLmNvbnRhaW5zKGV2LnRhcmdldCkpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe1xuICAgICAgICBpc09wZW46IGZhbHNlXG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICByZW5kZXIoKSB7XG4gICAgY29uc3QgeyBvbkNoYW5nZSwgb25DbGVhciwgdmFsdWUsIGJ0bkNsYXNzLCBub0NsZWFyIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3Qgc2VsZWN0ZWRPcHRpb24gPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgPT09IHZhbHVlKTtcbiAgICBjb25zdCBwbGFjZWhvbGRlciA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy50eXBlID09PSBcInBsYWNlaG9sZGVyXCIpO1xuICAgIGNvbnN0IG90aGVyT3B0aW9ucyA9IFJlYWN0LkNoaWxkcmVuLnRvQXJyYXkodGhpcy5wcm9wcy5jaGlsZHJlbikuZmlsdGVyKChvcHQpID0+IG9wdC5wcm9wcy52YWx1ZSAmJiBvcHQucHJvcHMudmFsdWUgIT09IHZhbHVlKTtcblxuICAgIHJldHVybiAoXG5cbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImRyb3Bkb3duXCIsIHtvcGVuOiB0aGlzLnN0YXRlLmlzT3Blbn0pfT5cbiAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9e2N4KFwiYnRuXCIsIFwiZHJvcGRvd24tdG9nZ2xlXCIsIGJ0bkNsYXNzIHx8IFwiYnRuLWJsYW5rXCIpfSBvbkNsaWNrPXt0aGlzLnRvZ2dsZVNlbGVjdC5iaW5kKHRoaXMpfT5cbiAgICAgICAgICB7c2VsZWN0ZWRPcHRpb24ubGVuZ3RoID8gc2VsZWN0ZWRPcHRpb24gOiBwbGFjZWhvbGRlcn0gPHNwYW4gY2xhc3NOYW1lPVwiY2FyZXRcIiAvPlxuICAgICAgICA8L2J1dHRvbj5cblxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwiZHJvcGRvd24tbWVudVwiPlxuICAgICAgICAgIHsgdmFsdWUgJiYgIW5vQ2xlYXIgPyAoXG4gICAgICAgICAgICA8bGk+XG4gICAgICAgICAgICAgIDxhIG9uQ2xpY2s9eygpID0+IHsgb25DbGVhcigpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpO319PlxuICAgICAgICAgICAgICAgIC0gY2xlYXIgLVxuICAgICAgICAgICAgICA8L2E+XG4gICAgICAgICAgICA8L2xpPlxuICAgICAgICAgICkgOiBudWxsfVxuICAgICAgICAgIHtvdGhlck9wdGlvbnMubWFwKChvcHRpb24sIGkpID0+IChcbiAgICAgICAgICAgIDxsaSBrZXk9e2l9PlxuICAgICAgICAgICAgICA8YSBzdHlsZT17e2N1cnNvcjogXCJwb2ludGVyXCJ9fSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2hhbmdlKG9wdGlvbi5wcm9wcy52YWx1ZSk7IHRoaXMudG9nZ2xlU2VsZWN0KCk7IH19PntvcHRpb259PC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC91bD5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuU2VsZWN0RmllbGQucHJvcFR5cGVzID0ge1xuICBvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG4gIG9uQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICB2YWx1ZTogUmVhY3QuUHJvcFR5cGVzLmFueSxcbiAgYnRuQ2xhc3M6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG5vQ2xlYXI6IFJlYWN0LlByb3BUeXBlcy5ib29sXG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3RGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcblxuZnVuY3Rpb24gRm9vdGVyKHByb3BzKSB7XG4gIGNvbnN0IGhpTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImhpLWxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1odXlnZW5zLWluZy5zdmdcIiAvPlxuICAgIDwvZGl2PlxuICApO1xuXG4gIGNvbnN0IGNsYXJpYWhMb2dvID0gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEgY29sLW1kLTFcIj5cbiAgICAgIDxpbWcgY2xhc3NOYW1lPVwibG9nb1wiIHNyYz1cImltYWdlcy9sb2dvLWNsYXJpYWguc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBmb290ZXJCb2R5ID0gUmVhY3QuQ2hpbGRyZW4uY291bnQocHJvcHMuY2hpbGRyZW4pID4gMCA/XG4gICAgUmVhY3QuQ2hpbGRyZW4ubWFwKHByb3BzLmNoaWxkcmVuLCAoY2hpbGQsIGkpID0+IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwid2hpdGUtYmFyXCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAge2kgPT09IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSAtIDFcbiAgICAgICAgICAgID8gKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2hpTG9nb308ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xMCBjb2wtbWQtMTAgdGV4dC1jZW50ZXJcIj57Y2hpbGR9PC9kaXY+e2NsYXJpYWhMb2dvfTwvZGl2PilcbiAgICAgICAgICAgIDogKDxkaXYgY2xhc3NOYW1lPVwicm93XCI+e2NoaWxkfTwvZGl2PilcbiAgICAgICAgICB9XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKSkgOiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwicm93XCI+XG4gICAgICAgICAgICB7aGlMb2dvfVxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIHtjbGFyaWFoTG9nb31cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuXG5cbiAgcmV0dXJuIChcbiAgICA8Zm9vdGVyIGNsYXNzTmFtZT1cImZvb3RlclwiPlxuICAgICAge2Zvb3RlckJvZHl9XG4gICAgPC9mb290ZXI+XG4gIClcbn1cblxuZXhwb3J0IGRlZmF1bHQgRm9vdGVyOyIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IGRpc21pc3NpYmxlLCBhbGVydExldmVsLCBvbkNsb3NlTWVzc2FnZX0gPSBwcm9wcztcbiAgY29uc3QgZGlzbWlzc0J1dHRvbiA9IGRpc21pc3NpYmxlXG4gICAgPyA8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzc05hbWU9XCJjbG9zZVwiIG9uQ2xpY2s9e29uQ2xvc2VNZXNzYWdlfT48c3Bhbj4mdGltZXM7PC9zcGFuPjwvYnV0dG9uPlxuICAgIDogbnVsbDtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPXtjeChcImFsZXJ0XCIsIGBhbGVydC0ke2FsZXJ0TGV2ZWx9YCwge1wiYWxlcnQtZGlzbWlzc2libGVcIjogZGlzbWlzc2libGV9KX0gcm9sZT1cImFsZXJ0XCI+XG4gICAgICB7ZGlzbWlzc0J1dHRvbn1cbiAgICAgIHtwcm9wcy5jaGlsZHJlbn1cbiAgICA8L2Rpdj5cbiAgKVxufTsiLCJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IEZvb3RlciBmcm9tIFwiLi9mb290ZXJcIjtcblxuY29uc3QgRk9PVEVSX0hFSUdIVCA9IDgxO1xuXG5mdW5jdGlvbiBQYWdlKHByb3BzKSB7XG4gIGNvbnN0IGZvb3RlcnMgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKGNoaWxkKSA9PiBjaGlsZC5wcm9wcy50eXBlID09PSBcImZvb3Rlci1ib2R5XCIpO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJwYWdlXCI+XG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpbiBoaS1HcmVlbiBjb250YWluZXItZmx1aWRcIj5cbiAgICAgICAgPG5hdiBjbGFzc05hbWU9XCJuYXZiYXIgXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwibmF2YmFyLWhlYWRlclwiPiA8YSBjbGFzc05hbWU9XCJuYXZiYXItYnJhbmRcIiBocmVmPVwiI1wiPjxpbWcgc3JjPVwiaW1hZ2VzL2xvZ28tdGltYnVjdG9vLnN2Z1wiIGNsYXNzTmFtZT1cImxvZ29cIiBhbHQ9XCJ0aW1idWN0b29cIi8+PC9hPiA8L2Rpdj5cbiAgICAgICAgICAgIDxkaXYgaWQ9XCJuYXZiYXJcIiBjbGFzc05hbWU9XCJuYXZiYXItY29sbGFwc2UgY29sbGFwc2VcIj5cbiAgICAgICAgICAgICAgPHVsIGNsYXNzTmFtZT1cIm5hdiBuYXZiYXItbmF2IG5hdmJhci1yaWdodFwiPlxuICAgICAgICAgICAgICAgIHtwcm9wcy51c2VybmFtZSA/IDxsaT48YSBocmVmPXtwcm9wcy51c2VybG9jYXRpb24gfHwgJyMnfT48c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXVzZXJcIi8+IHtwcm9wcy51c2VybmFtZX08L2E+PC9saT4gOiBudWxsfVxuICAgICAgICAgICAgICA8L3VsPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvbmF2PlxuICAgICAgPC9kaXY+XG4gICAgICA8ZGl2ICBzdHlsZT17e21hcmdpbkJvdHRvbTogYCR7Rk9PVEVSX0hFSUdIVCAqIGZvb3RlcnMubGVuZ3RofXB4YH19PlxuICAgICAgICB7UmVhY3QuQ2hpbGRyZW4udG9BcnJheShwcm9wcy5jaGlsZHJlbikuZmlsdGVyKChjaGlsZCkgPT4gY2hpbGQucHJvcHMudHlwZSAhPT0gXCJmb290ZXItYm9keVwiKX1cbiAgICAgIDwvZGl2PlxuICAgICAgPEZvb3Rlcj5cbiAgICAgICAge2Zvb3RlcnN9XG4gICAgICA8L0Zvb3Rlcj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cblxuZXhwb3J0IGRlZmF1bHQgUGFnZTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBSZWFjdERPTSBmcm9tIFwicmVhY3QtZG9tXCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2FjdGlvbnNcIjtcbmltcG9ydCB7c2V0VnJlfSBmcm9tIFwiLi9hY3Rpb25zL3ZyZVwiO1xuaW1wb3J0IEFwcCBmcm9tIFwiLi9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpXCI7XG5pbXBvcnQgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIGZyb20gXCIuL2FjdGlvbnMvYXV0b2NvbXBsZXRlXCI7XG5cbmltcG9ydCByb3V0ZXIgZnJvbSBcIi4vcm91dGVyXCI7XG5cbmNvbnN0IHNldFVzZXIgPSAocmVzcG9uc2UpID0+IHtcblx0cmV0dXJuIHtcblx0XHR0eXBlOiBcIlNFVF9VU0VSXCIsXG5cdFx0dXNlcjogcmVzcG9uc2Vcblx0fTtcbn07XG5cbmRvY3VtZW50LmFkZEV2ZW50TGlzdGVuZXIoXCJET01Db250ZW50TG9hZGVkXCIsICgpID0+IHtcblxuXHRmdW5jdGlvbiBpbml0Um91dGVyKCkge1xuXHRcdFJlYWN0RE9NLnJlbmRlcihyb3V0ZXIsIGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKFwiYXBwXCIpKTtcblx0fVxuXG5cdGZ1bmN0aW9uIGdldFZyZUlkKCkge1xuXHRcdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdFx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdFx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdFx0bGV0IFtrZXksIHZhbHVlXSA9IHBhcmFtc1tpXS5zcGxpdChcIj1cIik7XG5cdFx0XHRpZihrZXkgPT09IFwidnJlSWRcIikge1xuXHRcdFx0XHRyZXR1cm4gdmFsdWU7XG5cdFx0XHR9XG5cdFx0fVxuXHRcdHJldHVybiBcIldvbWVuV3JpdGVyc1wiO1xuXHR9XG5cblx0ZnVuY3Rpb24gZ2V0TG9naW4oKSB7XG5cdFx0bGV0IHBhdGggPSB3aW5kb3cubG9jYXRpb24uc2VhcmNoLnN1YnN0cigxKTtcblx0XHRsZXQgcGFyYW1zID0gcGF0aC5zcGxpdChcIiZcIik7XG5cblx0XHRmb3IobGV0IGkgaW4gcGFyYW1zKSB7XG5cdFx0XHRsZXQgW2tleSwgdmFsdWVdID0gcGFyYW1zW2ldLnNwbGl0KFwiPVwiKTtcblx0XHRcdGlmKGtleSA9PT0gXCJoc2lkXCIpIHtcblx0XHRcdFx0cmV0dXJuIHt1c2VyOiB2YWx1ZSwgdG9rZW46IHZhbHVlfTtcblx0XHRcdH1cblx0XHR9XG5cdFx0cmV0dXJuIHVuZGVmaW5lZDtcblx0fVxuXHRzdG9yZS5kaXNwYXRjaChzZXRWcmUoZ2V0VnJlSWQoKSwgaW5pdFJvdXRlcikpO1xuXHRzdG9yZS5kaXNwYXRjaChzZXRVc2VyKGdldExvZ2luKCkpKTtcbn0pOyIsImltcG9ydCBzZXRJbiBmcm9tIFwiLi4vdXRpbC9zZXQtaW5cIjtcblxubGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0ZGF0YToge1xuXHRcdFwiQHJlbGF0aW9uc1wiOiBbXVxuXHR9LFxuXHRkb21haW46IG51bGwsXG5cdGVycm9yTWVzc2FnZTogbnVsbCxcblx0cGVuZGluZzogZmFsc2Vcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblxuXHRcdGNhc2UgXCJCRUZPUkVfRkVUQ0hfRU5USVRZXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IHtcblx0XHRcdFx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0XHRcdFx0fSxcblx0XHRcdFx0cGVuZGluZzogdHJ1ZVxuXHRcdFx0fX07XG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IGFjdGlvbi5kYXRhLFxuXHRcdFx0XHRkb21haW46IGFjdGlvbi5kb21haW4sXG5cdFx0XHRcdGVycm9yTWVzc2FnZTogYWN0aW9uLmVycm9yTWVzc2FnZSB8fCBudWxsLFxuXHRcdFx0XHRwZW5kaW5nOiBmYWxzZVxuXHRcdFx0fX07XG5cblx0XHRjYXNlIFwiU0VUX0VOVElUWV9GSUVMRF9WQUxVRVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiBzZXRJbihhY3Rpb24uZmllbGRQYXRoLCBhY3Rpb24udmFsdWUsIHN0YXRlLmRhdGEpXG5cdFx0XHR9fTtcblxuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWV9GQUlMVVJFXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdGRhdGE6IHtcblx0XHRcdFx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0XHRcdFx0fSxcblx0XHRcdFx0ZXJyb3JNZXNzYWdlOiBhY3Rpb24uZXJyb3JNZXNzYWdlLFxuXHRcdFx0XHRwZW5kaW5nOiBmYWxzZVxuXHRcdFx0fX07XG5cblx0XHRjYXNlIFwiU0VUX1ZSRVwiOiB7XG5cdFx0XHRyZXR1cm4gaW5pdGlhbFN0YXRlO1xuXHRcdH1cblxuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufSIsImltcG9ydCB7Y29tYmluZVJlZHVjZXJzfSBmcm9tIFwicmVkdXhcIjtcblxuaW1wb3J0IGVudGl0eSBmcm9tIFwiLi9lbnRpdHlcIjtcbmltcG9ydCBtZXNzYWdlcyBmcm9tIFwiLi9tZXNzYWdlc1wiO1xuaW1wb3J0IHVzZXIgZnJvbSBcIi4vdXNlclwiO1xuaW1wb3J0IHZyZSBmcm9tIFwiLi92cmVcIjtcbmltcG9ydCBxdWlja1NlYXJjaCBmcm9tIFwiLi9xdWljay1zZWFyY2hcIjtcblxuZXhwb3J0IGRlZmF1bHQgY29tYmluZVJlZHVjZXJzKHtcblx0dnJlOiB2cmUsXG5cdGVudGl0eTogZW50aXR5LFxuXHR1c2VyOiB1c2VyLFxuXHRtZXNzYWdlczogbWVzc2FnZXMsXG5cdHF1aWNrU2VhcmNoOiBxdWlja1NlYXJjaFxufSk7IiwiaW1wb3J0IHNldEluIGZyb20gXCIuLi91dGlsL3NldC1pblwiO1xuXG5jb25zdCBpbml0aWFsU3RhdGUgPSB7XG5cdGxvZzogW11cbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiUkVRVUVTVF9NRVNTQUdFXCI6XG5cdFx0XHRzdGF0ZS5sb2cucHVzaCh7bWVzc2FnZTogYWN0aW9uLm1lc3NhZ2UsIHR5cGU6IGFjdGlvbi50eXBlLCB0aW1lOiBuZXcgRGF0ZSgpfSk7XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0Y2FzZSBcIlNVQ0NFU1NfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJFUlJPUl9NRVNTQUdFXCI6XG5cdFx0XHRzdGF0ZS5sb2cucHVzaCh7bWVzc2FnZTogYWN0aW9uLm1lc3NhZ2UsIHR5cGU6IGFjdGlvbi50eXBlLCB0aW1lOiBuZXcgRGF0ZSgpfSk7XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0Y2FzZSBcIkRJU01JU1NfTUVTU0FHRVwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGxvZzogc2V0SW4oW2FjdGlvbi5tZXNzYWdlSW5kZXgsIFwiZGlzbWlzc2VkXCJdLCB0cnVlLCBzdGF0ZS5sb2cpXG5cdFx0XHR9O1xuXHR9XG5cblx0cmV0dXJuIHN0YXRlO1xufSIsImxldCBpbml0aWFsU3RhdGUgPSB7XG5cdHN0YXJ0OiAwLFxuXHRsaXN0OiBbXSxcblx0cm93czogNTAsXG5cdHF1ZXJ5OiBcIlwiXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCI6XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCBzdGFydDogYWN0aW9uLnN0YXJ0fTtcblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlfTElTVFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRsaXN0OiBhY3Rpb24uZGF0YVxuXHRcdFx0fX07XG5cdFx0Y2FzZSBcIlNFVF9RVUlDS1NFQVJDSF9RVUVSWVwiOiB7XG5cdFx0XHRyZXR1cm4gey4uLnN0YXRlLCAuLi57XG5cdFx0XHRcdHF1ZXJ5OiBhY3Rpb24udmFsdWVcblx0XHRcdH19O1xuXHRcdH1cblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IG51bGw7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1VTRVJcIjpcblx0XHRcdGlmIChhY3Rpb24udXNlcikge1xuXHRcdFx0XHRyZXR1cm4gYWN0aW9uLnVzZXI7XG5cdFx0XHR9IGVsc2Uge1xuXHRcdFx0XHRyZXR1cm4gc3RhdGU7XG5cdFx0XHR9XG5cdFx0XHRicmVhaztcblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwibGV0IGluaXRpYWxTdGF0ZSA9IHtcblx0dnJlSWQ6IG51bGwsXG5cdGxpc3Q6IFtdLFxuXHRjb2xsZWN0aW9uczoge30sXG5cdGRvbWFpbjogbnVsbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfVlJFXCI6XG5cdFx0XHRyZXR1cm4ge1xuXHRcdFx0XHQuLi5zdGF0ZSxcblx0XHRcdFx0dnJlSWQ6IGFjdGlvbi52cmVJZCxcblx0XHRcdFx0Y29sbGVjdGlvbnM6IGFjdGlvbi5jb2xsZWN0aW9ucyB8fCBudWxsLFxuXHRcdFx0XHRsaXN0OiBhY3Rpb24ubGlzdCB8fCBzdGF0ZS5saXN0XG5cdFx0XHR9O1xuXG5cdFx0Y2FzZSBcIkxJU1RfVlJFU1wiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGxpc3Q6IGFjdGlvbi5saXN0LFxuXHRcdFx0XHRjb2xsZWN0aW9uczogbnVsbFxuXHRcdFx0fTtcblx0XHRjYXNlIFwiU0VUX0RPTUFJTlwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdGRvbWFpbjogYWN0aW9uLmRvbWFpblxuXHRcdFx0fTtcblxuXHRcdGRlZmF1bHQ6XG5cdFx0XHRyZXR1cm4gc3RhdGU7XG5cdH1cbn0iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQge1JvdXRlciwgUmVkaXJlY3QsIFJvdXRlLCBoYXNoSGlzdG9yeX0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuaW1wb3J0IHtQcm92aWRlciwgY29ubmVjdH0gZnJvbSBcInJlYWN0LXJlZHV4XCI7XG5pbXBvcnQgc3RvcmUgZnJvbSBcIi4vc3RvcmVcIjtcbmltcG9ydCBnZXRBdXRvY29tcGxldGVWYWx1ZXMgZnJvbSBcIi4vYWN0aW9ucy9hdXRvY29tcGxldGVcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2FjdGlvbnNcIjtcblxuaW1wb3J0IEVkaXRHdWkgZnJvbSBcIi4vY29tcG9uZW50cy9lZGl0LWd1aS9lZGl0LWd1aVwiO1xuaW1wb3J0IHt1cmxzfSBmcm9tIFwiLi91cmxzXCI7XG5cbmV4cG9ydCBmdW5jdGlvbiBuYXZpZ2F0ZVRvKGtleSwgYXJncykge1xuXHRoYXNoSGlzdG9yeS5wdXNoKHVybHNba2V5XS5hcHBseShudWxsLCBhcmdzKSk7XG59XG5cbmNvbnN0IGRlZmF1bHRDb25uZWN0ID0gY29ubmVjdChcblx0c3RhdGUgPT4gKHsuLi5zdGF0ZSwgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzOiBnZXRBdXRvY29tcGxldGVWYWx1ZXN9KSxcblx0ZGlzcGF0Y2ggPT4gYWN0aW9ucyhuYXZpZ2F0ZVRvLCBkaXNwYXRjaClcbik7XG5cblxuZXhwb3J0IGRlZmF1bHQgKFxuXHQ8UHJvdmlkZXIgc3RvcmU9e3N0b3JlfT5cblx0XHQ8Um91dGVyIGhpc3Rvcnk9e2hhc2hIaXN0b3J5fT5cblx0XHRcdDxSb3V0ZSBwYXRoPXt1cmxzLnJvb3QoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5uZXdFbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5lbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0PC9Sb3V0ZXI+XG5cdDwvUHJvdmlkZXI+XG4pO1xuIiwiaW1wb3J0IHtjcmVhdGVTdG9yZSwgYXBwbHlNaWRkbGV3YXJlfSBmcm9tIFwicmVkdXhcIjtcbmltcG9ydCB0aHVua01pZGRsZXdhcmUgZnJvbSBcInJlZHV4LXRodW5rXCI7XG5cbmltcG9ydCByZWR1Y2VycyBmcm9tIFwiLi4vcmVkdWNlcnNcIjtcblxuY29uc3QgbG9nZ2VyID0gKCkgPT4gbmV4dCA9PiBhY3Rpb24gPT4ge1xuICBpZiAoYWN0aW9uLmhhc093blByb3BlcnR5KFwidHlwZVwiKSkge1xuICAgIGNvbnNvbGUubG9nKFwiW1JFRFVYXVwiLCBhY3Rpb24udHlwZSwgYWN0aW9uKTtcbiAgfVxuXG4gIHJldHVybiBuZXh0KGFjdGlvbik7XG59O1xuXG5sZXQgY3JlYXRlU3RvcmVXaXRoTWlkZGxld2FyZSA9IGFwcGx5TWlkZGxld2FyZSgvKmxvZ2dlciwqLyB0aHVua01pZGRsZXdhcmUpKGNyZWF0ZVN0b3JlKTtcbmV4cG9ydCBkZWZhdWx0IGNyZWF0ZVN0b3JlV2l0aE1pZGRsZXdhcmUocmVkdWNlcnMpO1xuIiwiY29uc3QgdXJscyA9IHtcblx0cm9vdCgpIHtcblx0XHRyZXR1cm4gXCIvXCI7XG5cdH0sXG5cdG5ld0VudGl0eShjb2xsZWN0aW9uKSB7XG5cdFx0cmV0dXJuIGNvbGxlY3Rpb25cblx0XHRcdD8gYC8ke2NvbGxlY3Rpb259L25ld2Bcblx0XHRcdDogXCIvOmNvbGxlY3Rpb24vbmV3XCI7XG5cdH0sXG5cdGVudGl0eShjb2xsZWN0aW9uLCBpZCkge1xuXHRcdHJldHVybiBjb2xsZWN0aW9uICYmIGlkXG5cdFx0XHQ/IGAvJHtjb2xsZWN0aW9ufS8ke2lkfWBcblx0XHRcdDogXCIvOmNvbGxlY3Rpb24vOmlkXCI7XG5cdH1cbn07XG5cbmV4cG9ydCB7IHVybHMgfSIsImZ1bmN0aW9uIGRlZXBDbG9uZTkob2JqKSB7XG4gICAgdmFyIGksIGxlbiwgcmV0O1xuXG4gICAgaWYgKHR5cGVvZiBvYmogIT09IFwib2JqZWN0XCIgfHwgb2JqID09PSBudWxsKSB7XG4gICAgICAgIHJldHVybiBvYmo7XG4gICAgfVxuXG4gICAgaWYgKEFycmF5LmlzQXJyYXkob2JqKSkge1xuICAgICAgICByZXQgPSBbXTtcbiAgICAgICAgbGVuID0gb2JqLmxlbmd0aDtcbiAgICAgICAgZm9yIChpID0gMDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgICAgICByZXQucHVzaCggKHR5cGVvZiBvYmpbaV0gPT09IFwib2JqZWN0XCIgJiYgb2JqW2ldICE9PSBudWxsKSA/IGRlZXBDbG9uZTkob2JqW2ldKSA6IG9ialtpXSApO1xuICAgICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgICAgcmV0ID0ge307XG4gICAgICAgIGZvciAoaSBpbiBvYmopIHtcbiAgICAgICAgICAgIGlmIChvYmouaGFzT3duUHJvcGVydHkoaSkpIHtcbiAgICAgICAgICAgICAgICByZXRbaV0gPSAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG4gICAgfVxuICAgIHJldHVybiByZXQ7XG59XG5cbmV4cG9ydCBkZWZhdWx0IGRlZXBDbG9uZTk7IiwiaW1wb3J0IGNsb25lIGZyb20gXCIuL2Nsb25lLWRlZXBcIjtcblxuLy8gRG8gZWl0aGVyIG9mIHRoZXNlOlxuLy8gIGEpIFNldCBhIHZhbHVlIGJ5IHJlZmVyZW5jZSBpZiBkZXJlZiBpcyBub3QgbnVsbFxuLy8gIGIpIFNldCBhIHZhbHVlIGRpcmVjdGx5IGluIHRvIGRhdGEgb2JqZWN0IGlmIGRlcmVmIGlzIG51bGxcbmNvbnN0IHNldEVpdGhlciA9IChkYXRhLCBkZXJlZiwga2V5LCB2YWwpID0+IHtcblx0KGRlcmVmIHx8IGRhdGEpW2tleV0gPSB2YWw7XG5cdHJldHVybiBkYXRhO1xufTtcblxuLy8gU2V0IGEgbmVzdGVkIHZhbHVlIGluIGRhdGEgKG5vdCB1bmxpa2UgaW1tdXRhYmxlanMsIGJ1dCBhIGNsb25lIG9mIGRhdGEgaXMgZXhwZWN0ZWQgZm9yIHByb3BlciBpbW11dGFiaWxpdHkpXG5jb25zdCBfc2V0SW4gPSAocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID0gbnVsbCkgPT5cblx0cGF0aC5sZW5ndGggPiAxID9cblx0XHRfc2V0SW4ocGF0aCwgdmFsdWUsIGRhdGEsIGRlcmVmID8gZGVyZWZbcGF0aC5zaGlmdCgpXSA6IGRhdGFbcGF0aC5zaGlmdCgpXSkgOlxuXHRcdHNldEVpdGhlcihkYXRhLCBkZXJlZiwgcGF0aFswXSwgdmFsdWUpO1xuXG5jb25zdCBzZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSkgPT5cblx0X3NldEluKGNsb25lKHBhdGgpLCB2YWx1ZSwgY2xvbmUoZGF0YSkpO1xuXG5leHBvcnQgZGVmYXVsdCBzZXRJbjsiXX0=
