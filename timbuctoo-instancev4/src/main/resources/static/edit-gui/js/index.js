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

		onRedirectToFirst: function onRedirectToFirst(collection, id) {
			return navigateTo("entity", [collection, id]);
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
								{ to: _urls.urls.firstEntity(domain) },
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
			    onSelectDomain = _props.onSelectDomain,
			    onRedirectToFirst = _props.onRedirectToFirst;

			// Triggers fetch data from server based on id from route.

			if (this.props.params.id !== nextProps.params.id) {
				onSelect({ domain: nextProps.params.collection, id: nextProps.params.id });
			} else if (this.props.params.collection !== nextProps.params.collection) {
				onNew(nextProps.params.collection);
				onSelectDomain(nextProps.params.collection);
			}if ((nextProps.location.pathname.match(/\/first$/) || nextProps.location.pathname === "/") && nextProps.quickSearch.list.length > 0 && nextProps.quickSearch.list[0]["@type"] === (nextProps.entity.domain || "").replace(/s$/, "")) {

				onRedirectToFirst(nextProps.entity.domain, nextProps.quickSearch.list[0]._id);
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
			} else {
				console.log(this.props.location);
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
			    onAddSelectedFields = _props2.onAddSelectedFields;
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

var EntityForm = function (_React$Component) {
  _inherits(EntityForm, _React$Component);

  function EntityForm(props) {
    _classCallCheck(this, EntityForm);

    var _this = _possibleConstructorReturn(this, (EntityForm.__proto__ || Object.getPrototypeOf(EntityForm)).call(this, props));

    _this.state = {
      fieldsToAdd: []
    };
    return _this;
  }

  _createClass(EntityForm, [{
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

      this.setState({ fieldsToAdd: [] });
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var _props = this.props,
          onDelete = _props.onDelete,
          onChange = _props.onChange,
          getAutocompleteValues = _props.getAutocompleteValues;
      var _props2 = this.props,
          entity = _props2.entity,
          currentMode = _props2.currentMode,
          properties = _props2.properties,
          entityLabel = _props2.entityLabel;
      var fieldsToAdd = this.state.fieldsToAdd;


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
          _react2.default.createElement(
            "div",
            { style: { maxHeight: "250px", overflowY: "auto" } },
            properties.filter(function (fieldDef) {
              return fieldMap.hasOwnProperty(fieldDef.type);
            }).filter(function (fieldDef) {
              return !entity.data.hasOwnProperty(fieldDef.name) && !entity.data["@relations"].hasOwnProperty(fieldDef.name);
            }).map(function (fieldDef, i) {
              return _react2.default.createElement(
                "div",
                { key: i, onClick: function onClick() {
                    return _this2.toggleFieldToAdd(fieldDef.name);
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
		_react2.default.createElement(_reactRouter.Route, { path: _urls.urls.firstEntity(), components: defaultConnect(_editGui2.default) }),
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
	firstEntity: function firstEntity(collection) {
		return collection ? "/" + collection + "/first" : "/:collection/first";
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
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvYWN0aW9ucy9hdXRvY29tcGxldGUuanMiLCJzcmMvYWN0aW9ucy9jcnVkLmpzIiwic3JjL2FjdGlvbnMvZW50aXR5LmpzIiwic3JjL2FjdGlvbnMvaW5kZXguanMiLCJzcmMvYWN0aW9ucy9zYXZlLXJlbGF0aW9ucy5qcyIsInNyYy9hY3Rpb25zL3NlcnZlci5qcyIsInNyYy9hY3Rpb25zL3ZyZS5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2NvbGxlY3Rpb24tdGFicy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VkaXQtZ3VpLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2NhbWVsMmxhYmVsLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpbmtzLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL2xpc3Qtb2Ytc3RyaW5ncy5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2ZpZWxkcy9tdWx0aS1zZWxlY3QuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvbmFtZXMuanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvcmVsYXRpb24uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9maWVsZHMvc2VsZWN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWZvcm0vZmllbGRzL3N0cmluZy1maWVsZC5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1mb3JtL2Zvcm0uanMiLCJzcmMvY29tcG9uZW50cy9lZGl0LWd1aS9lbnRpdHktZm9ybS9zYXZlLWZvb3Rlci5qcyIsInNyYy9jb21wb25lbnRzL2VkaXQtZ3VpL2VudGl0eS1pbmRleC9saXN0LmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3BhZ2luYXRlLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvZW50aXR5LWluZGV4L3F1aWNrc2VhcmNoLmpzIiwic3JjL2NvbXBvbmVudHMvZWRpdC1ndWkvbWVzc2FnZXMvbGlzdC5qcyIsInNyYy9jb21wb25lbnRzL2ZpZWxkcy9zZWxlY3QtZmllbGQuanMiLCJzcmMvY29tcG9uZW50cy9mb290ZXIuanMiLCJzcmMvY29tcG9uZW50cy9tZXNzYWdlLmpzIiwic3JjL2NvbXBvbmVudHMvcGFnZS5qc3giLCJzcmMvaW5kZXguanMiLCJzcmMvcmVkdWNlcnMvZW50aXR5LmpzIiwic3JjL3JlZHVjZXJzL2luZGV4LmpzIiwic3JjL3JlZHVjZXJzL21lc3NhZ2VzLmpzIiwic3JjL3JlZHVjZXJzL3F1aWNrLXNlYXJjaC5qcyIsInNyYy9yZWR1Y2Vycy91c2VyLmpzIiwic3JjL3JlZHVjZXJzL3ZyZS5qcyIsInNyYy9yb3V0ZXIuanMiLCJzcmMvc3RvcmUvaW5kZXguanMiLCJzcmMvdXJscy5qcyIsInNyYy91dGlsL2Nsb25lLWRlZXAuanMiLCJzcmMvdXRpbC9zZXQtaW4uanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7a0JDRWUsVUFBUyxJQUFULEVBQWUsS0FBZixFQUFzQixJQUF0QixFQUE0QjtBQUMxQyxLQUFJLFVBQVU7QUFDYixPQUFRLFFBQVEsR0FBUixDQUFZLE1BQXBCLGNBQW1DLEtBQUssT0FBTCxDQUFhLGFBQWIsRUFBNEIsRUFBNUIsQ0FBbkMsZUFBNEUsS0FBNUU7QUFEYSxFQUFkOztBQUlBLEtBQUksVUFBVSxTQUFWLE9BQVUsQ0FBUyxHQUFULEVBQWMsUUFBZCxFQUF3QixJQUF4QixFQUE4QjtBQUMzQyxPQUFLLEtBQUssS0FBTCxDQUFXLElBQVgsRUFBaUIsR0FBakIsQ0FBcUIsVUFBQyxDQUFELEVBQU87QUFBRSxVQUFPLEVB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QUFPLHlCQUFNLElBQU4sQ0FBUCxFQUFvQixLQUFwQixFQUEyQix5QkFBTSxJQUFOLENBQTNCLENBRGE7QUFBQSxDQUFkOztrQkFHZSxLIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsImltcG9ydCBzZXJ2ZXIgZnJvbSBcIi4vc2VydmVyXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHBhdGgsIHF1ZXJ5LCBkb25lKSB7XG5cdGxldCBvcHRpb25zID0ge1xuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xLyR7cGF0aC5yZXBsYWNlKC9eXFwvdlteL10rXFwvLywgXCJcIil9P3F1ZXJ5PSR7cXVlcnl9KmBcblx0fTtcblxuXHRsZXQgeGhyRG9uZSA9IGZ1bmN0aW9uKGVyciwgcmVzcG9uc2UsIGJvZHkpIHtcblx0XHRkb25lKEpTT04ucGFyc2UoYm9keSkubWFwKChkKSA9PiB7IHJldHVybiB7a2V5OiBkLmtleS5yZXBsYWNlKC9eLitcXC8vLCBcIlwiKSwgdmFsdWU6IGQudmFsdWV9OyB9KSk7XG5cdH07XG5cblx0c2VydmVyLmZhc3RYaHIob3B0aW9ucywgeGhyRG9uZSk7XG59IiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcblxuY29uc3Qgc2F2ZU5ld0VudGl0eSA9IChkb21haW4sIHNhdmVEYXRhLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiUE9TVFwiLFxuXHRcdGhlYWRlcnM6IHNlcnZlci5tYWtlSGVhZGVycyh0b2tlbiwgdnJlSWQpLFxuXHRcdGJvZHk6IEpTT04uc3RyaW5naWZ5KHNhdmVEYXRhKSxcblx0XHR1cmw6IGAke3Byb2Nlc3MuZW52LnNlcnZlcn0vdjIuMS9kb21haW4vJHtkb21haW59YFxuXHR9LCBuZXh0LCBmYWlsLCBgQ3JlYXRlIG5ldyAke2RvbWFpbn1gKTtcblxuY29uc3QgdXBkYXRlRW50aXR5ID0gKGRvbWFpbiwgc2F2ZURhdGEsIHRva2VuLCB2cmVJZCwgbmV4dCwgZmFpbCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJQVVRcIixcblx0XHRoZWFkZXJzOiBzZXJ2ZXIubWFrZUhlYWRlcnModG9rZW4sIHZyZUlkKSxcblx0XHRib2R5OiBKU09OLnN0cmluZ2lmeShzYXZlRGF0YSksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke3NhdmVEYXRhLl9pZH1gXG5cdH0sIG5leHQsIGZhaWwsIGBVcGRhdGUgJHtkb21haW59YCk7XG5cbmNvbnN0IGRlbGV0ZUVudGl0eSA9IChkb21haW4sIGVudGl0eUlkLCB0b2tlbiwgdnJlSWQsIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiREVMRVRFXCIsXG5cdFx0aGVhZGVyczogc2VydmVyLm1ha2VIZWFkZXJzKHRva2VuLCB2cmVJZCksXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvZG9tYWluLyR7ZG9tYWlufS8ke2VudGl0eUlkfWBcblx0fSwgbmV4dCwgZmFpbCwgYERlbGV0ZSAke2RvbWFpbn1gKTtcblxuY29uc3QgZmV0Y2hFbnRpdHkgPSAobG9jYXRpb24sIG5leHQsIGZhaWwpID0+XG5cdHNlcnZlci5wZXJmb3JtWGhyKHtcblx0XHRtZXRob2Q6IFwiR0VUXCIsXG5cdFx0aGVhZGVyczoge1wiQWNjZXB0XCI6IFwiYXBwbGljYXRpb24vanNvblwifSxcblx0XHR1cmw6IGxvY2F0aW9uXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRjb25zdCBkYXRhID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdG5leHQoZGF0YSk7XG5cdH0sIGZhaWwsIFwiRmV0Y2ggZW50aXR5XCIpO1xuXG5jb25zdCBmZXRjaEVudGl0eUxpc3QgPSAoZG9tYWluLCBzdGFydCwgcm93cywgbmV4dCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XCJBY2NlcHRcIjogXCJhcHBsaWNhdGlvbi9qc29uXCJ9LFxuXHRcdHVybDogYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0/cm93cz0ke3Jvd3N9JnN0YXJ0PSR7c3RhcnR9YFxuXHR9LCAoZXJyLCByZXNwKSA9PiB7XG5cdFx0Y29uc3QgZGF0YSA9IEpTT04ucGFyc2UocmVzcC5ib2R5KTtcblx0XHRuZXh0KGRhdGEpO1xuXHR9KTtcblxuY29uc3QgY3J1ZCA9IHtcblx0c2F2ZU5ld0VudGl0eTogc2F2ZU5ld0VudGl0eSxcblx0dXBkYXRlRW50aXR5OiB1cGRhdGVFbnRpdHksXG5cdGRlbGV0ZUVudGl0eTogZGVsZXRlRW50aXR5LFxuXHRmZXRjaEVudGl0eTogZmV0Y2hFbnRpdHksXG5cdGZldGNoRW50aXR5TGlzdDogZmV0Y2hFbnRpdHlMaXN0XG59O1xuXG5leHBvcnQge3NhdmVOZXdFbnRpdHksIHVwZGF0ZUVudGl0eSwgZGVsZXRlRW50aXR5LCBmZXRjaEVudGl0eSwgZmV0Y2hFbnRpdHlMaXN0LCBjcnVkfTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4uL3V0aWwvY2xvbmUtZGVlcFwiO1xuaW1wb3J0IHsgY3J1ZCB9IGZyb20gXCIuL2NydWRcIjtcbmltcG9ydCBzYXZlUmVsYXRpb25zIGZyb20gXCIuL3NhdmUtcmVsYXRpb25zXCI7XG5pbXBvcnQgYXV0b2NvbXBsZXRlIGZyb20gXCIuL2F1dG9jb21wbGV0ZVwiO1xuXG4vLyBTa2VsZXRvbiBiYXNlIGRhdGEgcGVyIGZpZWxkIGRlZmluaXRpb25cbmNvbnN0IGluaXRpYWxEYXRhID0ge1xuXHRuYW1lczogW10sXG5cdG11bHRpc2VsZWN0OiBbXSxcblx0bGlua3M6IFtdLFxuXHRrZXl3b3JkOiBbXSxcblx0XCJsaXN0LW9mLXN0cmluZ3NcIjogW10sXG5cdGFsdG5hbWVzOiBbXSxcblx0dGV4dDogXCJcIixcblx0c3RyaW5nOiBcIlwiLFxuXHRzZWxlY3Q6IFwiXCIsXG5cdGRhdGFibGU6IFwiXCJcbn07XG5cbi8vIFJldHVybiB0aGUgaW5pdGlhbCBkYXRhIGZvciB0aGUgdHlwZSBpbiB0aGUgZmllbGQgZGVmaW5pdGlvblxuY29uc3QgaW5pdGlhbERhdGFGb3JUeXBlID0gKGZpZWxkRGVmKSA9PlxuXHRmaWVsZERlZi5kZWZhdWx0VmFsdWUgfHwgKGZpZWxkRGVmLnR5cGUgPT09IFwicmVsYXRpb25cIiB8fCBmaWVsZERlZi50eXBlID09PSBcImtleXdvcmRcIiA/IHt9IDogaW5pdGlhbERhdGFbZmllbGREZWYudHlwZV0pO1xuXG5jb25zdCBhZGRGaWVsZHNUb0VudGl0eSA9IChmaWVsZHMpID0+IChkaXNwYXRjaCkgPT4ge1xuXHRmaWVsZHMuZm9yRWFjaCgoZmllbGQpID0+IHtcblx0XHRpZiAoZmllbGQudHlwZSA9PT0gXCJyZWxhdGlvblwiKSB7XG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRU5USVRZX0ZJRUxEX1ZBTFVFXCIsIGZpZWxkUGF0aDogW1wiQHJlbGF0aW9uc1wiLCBmaWVsZC5uYW1lXSwgdmFsdWU6IFtdfSk7XG5cdFx0fSBlbHNlIHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBbZmllbGQubmFtZV0sIHZhbHVlOiBpbml0aWFsRGF0YUZvclR5cGUoZmllbGQpfSk7XG5cdFx0fVxuXHR9KVxufTtcblxuY29uc3QgZmV0Y2hFbnRpdHlMaXN0ID0gKGRvbWFpbikgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogMH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChkb21haW4sIDAsIGdldFN0YXRlKCkucXVpY2tTZWFyY2gucm93cywgKGRhdGEpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIiwgZGF0YTogZGF0YX0pKTtcbn07XG5cbmNvbnN0IHBhZ2luYXRlTGVmdCA9ICgpID0+IChkaXNwYXRjaCwgZ2V0U3RhdGUpID0+IHtcblx0Y29uc3QgbmV3U3RhcnQgPSBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnN0YXJ0IC0gZ2V0U3RhdGUoKS5xdWlja1NlYXJjaC5yb3dzO1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiLCBzdGFydDogbmV3U3RhcnQgPCAwID8gMCA6IG5ld1N0YXJ0fSk7XG5cdGNydWQuZmV0Y2hFbnRpdHlMaXN0KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgbmV3U3RhcnQgPCAwID8gMCA6IG5ld1N0YXJ0LCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGF9KSk7XG59O1xuXG5jb25zdCBwYWdpbmF0ZVJpZ2h0ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjb25zdCBuZXdTdGFydCA9IGdldFN0YXRlKCkucXVpY2tTZWFyY2guc3RhcnQgKyBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3M7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9QQUdJTkFUSU9OX1NUQVJUXCIsIHN0YXJ0OiBuZXdTdGFydH0pO1xuXHRjcnVkLmZldGNoRW50aXR5TGlzdChnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIG5ld1N0YXJ0LCBnZXRTdGF0ZSgpLnF1aWNrU2VhcmNoLnJvd3MsIChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGF9KSk7XG59O1xuXG5jb25zdCBzZW5kUXVpY2tTZWFyY2ggPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IHsgcXVpY2tTZWFyY2gsIGVudGl0eSwgdnJlIH0gPSBnZXRTdGF0ZSgpO1xuXHRpZiAocXVpY2tTZWFyY2gucXVlcnkubGVuZ3RoKSB7XG5cdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1BBR0lOQVRJT05fU1RBUlRcIiwgc3RhcnQ6IDB9KTtcblx0XHRjb25zdCBjYWxsYmFjayA9IChkYXRhKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJSRUNFSVZFX0VOVElUWV9MSVNUXCIsIGRhdGE6IGRhdGEubWFwKChkKSA9PiAoXG5cdFx0XHR7XG5cdFx0XHRcdF9pZDogZC5rZXkucmVwbGFjZSgvLipcXC8vLCBcIlwiKSxcblx0XHRcdFx0XCJAZGlzcGxheU5hbWVcIjogZC52YWx1ZVxuXHRcdFx0fVxuXHRcdCkpfSk7XG5cdFx0YXV0b2NvbXBsZXRlKGBkb21haW4vJHtlbnRpdHkuZG9tYWlufS9hdXRvY29tcGxldGVgLCBxdWlja1NlYXJjaC5xdWVyeSwgY2FsbGJhY2spO1xuXHR9IGVsc2Uge1xuXHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChlbnRpdHkuZG9tYWluKSk7XG5cdH1cbn07XG5cbmNvbnN0IHNlbGVjdERvbWFpbiA9IChkb21haW4pID0+IChkaXNwYXRjaCkgPT4ge1xuXHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRE9NQUlOXCIsIGRvbWFpbn0pO1xuXHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZG9tYWluKSk7XG5cdGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9RVUlDS1NFQVJDSF9RVUVSWVwiLCB2YWx1ZTogXCJcIn0pO1xufTtcblxuLy8gMSkgRmV0Y2ggZW50aXR5XG4vLyAyKSBEaXNwYXRjaCBSRUNFSVZFX0VOVElUWSBmb3IgcmVuZGVyXG5jb25zdCBzZWxlY3RFbnRpdHkgPSAoZG9tYWluLCBlbnRpdHlJZCwgZXJyb3JNZXNzYWdlID0gbnVsbCwgc3VjY2Vzc01lc3NhZ2UgPSBudWxsLCBuZXh0ID0gKCkgPT4geyB9KSA9PlxuXHQoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdFx0Y29uc3QgeyBlbnRpdHk6IHsgZG9tYWluOiBjdXJyZW50RG9tYWluIH0gfSA9IGdldFN0YXRlKCk7XG5cdFx0aWYgKGN1cnJlbnREb21haW4gIT09IGRvbWFpbikge1xuXHRcdFx0ZGlzcGF0Y2goc2VsZWN0RG9tYWluKGRvbWFpbikpO1xuXHRcdH1cblx0XHRkaXNwYXRjaCh7dHlwZTogXCJCRUZPUkVfRkVUQ0hfRU5USVRZXCJ9KVxuXHRcdGNydWQuZmV0Y2hFbnRpdHkoYCR7cHJvY2Vzcy5lbnYuc2VydmVyfS92Mi4xL2RvbWFpbi8ke2RvbWFpbn0vJHtlbnRpdHlJZH1gLCAoZGF0YSkgPT4ge1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlcIiwgZG9tYWluOiBkb21haW4sIGRhdGE6IGRhdGEsIGVycm9yTWVzc2FnZTogZXJyb3JNZXNzYWdlfSk7XG5cdFx0XHRpZiAoc3VjY2Vzc01lc3NhZ2UgIT09IG51bGwpIHtcblx0XHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU1VDQ0VTU19NRVNTQUdFXCIsIG1lc3NhZ2U6IHN1Y2Nlc3NNZXNzYWdlfSk7XG5cdFx0XHR9XG5cdFx0fSwgKCkgPT4gZGlzcGF0Y2goe3R5cGU6IFwiUkVDRUlWRV9FTlRJVFlfRkFJTFVSRVwiLCBlcnJvck1lc3NhZ2U6IGBGYWlsZWQgdG8gZmV0Y2ggJHtkb21haW59IHdpdGggSUQgJHtlbnRpdHlJZH1gfSkpO1xuXHRcdG5leHQoKTtcblx0fTtcblxuXG4vLyAxKSBEaXNwYXRjaCBSRUNFSVZFX0VOVElUWSB3aXRoIGVtcHR5IGVudGl0eSBza2VsZXRvbiBmb3IgcmVuZGVyXG5jb25zdCBtYWtlTmV3RW50aXR5ID0gKGRvbWFpbiwgZXJyb3JNZXNzYWdlID0gbnVsbCkgPT5cblx0KGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4gZGlzcGF0Y2goe1xuXHRcdHR5cGU6IFwiUkVDRUlWRV9FTlRJVFlcIixcblx0XHRkb21haW46IGRvbWFpbixcblx0XHRkYXRhOiB7XCJAcmVsYXRpb25zXCI6IHt9fSxcblx0XHRlcnJvck1lc3NhZ2U6IGVycm9yTWVzc2FnZVxuXHR9KTtcblxuY29uc3QgZGVsZXRlRW50aXR5ID0gKCkgPT4gKGRpc3BhdGNoLCBnZXRTdGF0ZSkgPT4ge1xuXHRjcnVkLmRlbGV0ZUVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBnZXRTdGF0ZSgpLnVzZXIudG9rZW4sIGdldFN0YXRlKCkudnJlLnZyZUlkLFxuXHRcdCgpID0+IHtcblx0XHRcdGRpc3BhdGNoKHt0eXBlOiBcIlNVQ0NFU1NfTUVTU0FHRVwiLCBtZXNzYWdlOiBgU3VjZXNzZnVsbHkgZGVsZXRlZCAke2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbn0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWB9KTtcblx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSk7XG5cdFx0XHRkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSk7XG5cdFx0fSxcblx0XHQoKSA9PiBkaXNwYXRjaChzZWxlY3RFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZCwgYEZhaWxlZCB0byBkZWxldGUgJHtnZXRTdGF0ZSgpLmVudGl0eS5kb21haW59IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gKSkpO1xufTtcblxuLy8gMSkgU2F2ZSBhbiBlbnRpdHlcbi8vIDIpIFNhdmUgdGhlIHJlbGF0aW9ucyBmb3IgdGhpcyBlbnRpdHlcbi8vIDMpIFJlZmV0Y2ggZW50aXR5IGZvciByZW5kZXJcbmNvbnN0IHNhdmVFbnRpdHkgPSAoKSA9PiAoZGlzcGF0Y2gsIGdldFN0YXRlKSA9PiB7XG5cdGNvbnN0IGNvbGxlY3Rpb25MYWJlbCA9IGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0uY29sbGVjdGlvbkxhYmVsLnJlcGxhY2UoL3MkLywgXCJcIik7XG5cblx0Ly8gTWFrZSBhIGRlZXAgY29weSBvZiB0aGUgZGF0YSB0byBiZSBzYXZlZCBpbiBvcmRlciB0byBsZWF2ZSBhcHBsaWNhdGlvbiBzdGF0ZSB1bmFsdGVyZWRcblx0bGV0IHNhdmVEYXRhID0gY2xvbmUoZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YSk7XG5cdC8vIE1ha2UgYSBkZWVwIGNvcHkgb2YgdGhlIHJlbGF0aW9uIGRhdGEgaW4gb3JkZXIgdG8gbGVhdmUgYXBwbGljYXRpb24gc3RhdGUgdW5hbHRlcmVkXG5cdGxldCByZWxhdGlvbkRhdGEgPSBjbG9uZShzYXZlRGF0YVtcIkByZWxhdGlvbnNcIl0pIHx8IHt9O1xuXHQvLyBEZWxldGUgdGhlIHJlbGF0aW9uIGRhdGEgZnJvbSB0aGUgc2F2ZURhdGEgYXMgaXQgaXMgbm90IGV4cGVjdGVkIGJ5IHRoZSBzZXJ2ZXJcblx0ZGVsZXRlIHNhdmVEYXRhW1wiQHJlbGF0aW9uc1wiXTtcblxuXHRpZiAoZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQpIHtcblx0XHQvLyAxKSBVcGRhdGUgdGhlIGVudGl0eSB3aXRoIHNhdmVEYXRhXG5cdFx0Y3J1ZC51cGRhdGVFbnRpdHkoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluLCBzYXZlRGF0YSwgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKGVyciwgcmVzcCkgPT5cblx0XHRcdC8vIDIpIFNhdmUgcmVsYXRpb25zIHVzaW5nIHNlcnZlciByZXNwb25zZSBmb3IgY3VycmVudCByZWxhdGlvbnMgdG8gZGlmZiBhZ2FpbnN0IHJlbGF0aW9uRGF0YVxuXHRcdFx0ZGlzcGF0Y2goKHJlZGlzcGF0Y2gpID0+IHNhdmVSZWxhdGlvbnMoSlNPTi5wYXJzZShyZXNwLmJvZHkpLCByZWxhdGlvbkRhdGEsIGdldFN0YXRlKCkudnJlLmNvbGxlY3Rpb25zW2dldFN0YXRlKCkuZW50aXR5LmRvbWFpbl0ucHJvcGVydGllcywgZ2V0U3RhdGUoKS51c2VyLnRva2VuLCBnZXRTdGF0ZSgpLnZyZS52cmVJZCwgKCkgPT5cblx0XHRcdFx0Ly8gMykgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuXHRcdFx0XHRyZWRpc3BhdGNoKHNlbGVjdEVudGl0eShnZXRTdGF0ZSgpLmVudGl0eS5kb21haW4sIGdldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkLCBudWxsLCBgU3VjY2VzZnVsbHkgc2F2ZWQgJHtjb2xsZWN0aW9uTGFiZWx9IHdpdGggSUQgJHtnZXRTdGF0ZSgpLmVudGl0eS5kYXRhLl9pZH1gLCAoKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSkpKSkpLCAoKSA9PlxuXHRcdFx0XHRcdC8vIDJhKSBIYW5kbGUgZXJyb3IgYnkgcmVmZXRjaGluZyBhbmQgcGFzc2luZyBhbG9uZyBhbiBlcnJvciBtZXNzYWdlXG5cdFx0XHRcdFx0ZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZ2V0U3RhdGUoKS5lbnRpdHkuZGF0YS5faWQsIGBGYWlsZWQgdG8gc2F2ZSAke2NvbGxlY3Rpb25MYWJlbH0gd2l0aCBJRCAke2dldFN0YXRlKCkuZW50aXR5LmRhdGEuX2lkfWApKSk7XG5cblx0fSBlbHNlIHtcblx0XHQvLyAxKSBDcmVhdGUgbmV3IGVudGl0eSB3aXRoIHNhdmVEYXRhXG5cdFx0Y3J1ZC5zYXZlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgc2F2ZURhdGEsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsIChlcnIsIHJlc3ApID0+XG5cdFx0XHQvLyAyKSBGZXRjaCBlbnRpdHkgdmlhIGxvY2F0aW9uIGhlYWRlclxuXHRcdFx0ZGlzcGF0Y2goKHJlZGlzcGF0Y2gpID0+IGNydWQuZmV0Y2hFbnRpdHkocmVzcC5oZWFkZXJzLmxvY2F0aW9uLCAoZGF0YSkgPT5cblx0XHRcdFx0Ly8gMykgU2F2ZSByZWxhdGlvbnMgdXNpbmcgc2VydmVyIHJlc3BvbnNlIGZvciBjdXJyZW50IHJlbGF0aW9ucyB0byBkaWZmIGFnYWluc3QgcmVsYXRpb25EYXRhXG5cdFx0XHRcdHNhdmVSZWxhdGlvbnMoZGF0YSwgcmVsYXRpb25EYXRhLCBnZXRTdGF0ZSgpLnZyZS5jb2xsZWN0aW9uc1tnZXRTdGF0ZSgpLmVudGl0eS5kb21haW5dLnByb3BlcnRpZXMsIGdldFN0YXRlKCkudXNlci50b2tlbiwgZ2V0U3RhdGUoKS52cmUudnJlSWQsICgpID0+XG5cdFx0XHRcdFx0Ly8gNCkgUmVmZXRjaCBlbnRpdHkgZm9yIHJlbmRlclxuXHRcdFx0XHRcdHJlZGlzcGF0Y2goc2VsZWN0RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgZGF0YS5faWQsIG51bGwsIGBTdWNjZXNmdWxseSBzYXZlZCAke2NvbGxlY3Rpb25MYWJlbH1gLCAoKSA9PiBkaXNwYXRjaChmZXRjaEVudGl0eUxpc3QoZ2V0U3RhdGUoKS5lbnRpdHkuZG9tYWluKSkpKSkpKSwgKCkgPT5cblx0XHRcdFx0XHRcdC8vIDJhKSBIYW5kbGUgZXJyb3IgYnkgcmVmZXRjaGluZyBhbmQgcGFzc2luZyBhbG9uZyBhbiBlcnJvciBtZXNzYWdlXG5cdFx0XHRcdFx0XHRkaXNwYXRjaChtYWtlTmV3RW50aXR5KGdldFN0YXRlKCkuZW50aXR5LmRvbWFpbiwgYEZhaWxlZCB0byBzYXZlIG5ldyAke2NvbGxlY3Rpb25MYWJlbH1gKSkpO1xuXHR9XG59O1xuXG5cbmV4cG9ydCB7IHNhdmVFbnRpdHksIHNlbGVjdEVudGl0eSwgbWFrZU5ld0VudGl0eSwgZGVsZXRlRW50aXR5LCBmZXRjaEVudGl0eUxpc3QsIHBhZ2luYXRlUmlnaHQsIHBhZ2luYXRlTGVmdCwgc2VuZFF1aWNrU2VhcmNoLCBzZWxlY3REb21haW4sIGFkZEZpZWxkc1RvRW50aXR5IH07IiwiaW1wb3J0IHsgc2F2ZUVudGl0eSwgc2VsZWN0RW50aXR5LCBtYWtlTmV3RW50aXR5LCBkZWxldGVFbnRpdHksIGFkZEZpZWxkc1RvRW50aXR5LFxuXHRzZWxlY3REb21haW4sIHBhZ2luYXRlTGVmdCwgcGFnaW5hdGVSaWdodCwgc2VuZFF1aWNrU2VhcmNoIH0gZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQgeyBzZXRWcmUgfSBmcm9tIFwiLi92cmVcIjtcblxuZXhwb3J0IGRlZmF1bHQgKG5hdmlnYXRlVG8sIGRpc3BhdGNoKSA9PiAoe1xuXHRvbk5ldzogKGRvbWFpbikgPT4gZGlzcGF0Y2gobWFrZU5ld0VudGl0eShkb21haW4pKSxcblx0b25TZWxlY3Q6IChyZWNvcmQpID0+IGRpc3BhdGNoKHNlbGVjdEVudGl0eShyZWNvcmQuZG9tYWluLCByZWNvcmQuaWQpKSxcblx0b25TYXZlOiAoKSA9PiBkaXNwYXRjaChzYXZlRW50aXR5KCkpLFxuXHRvbkRlbGV0ZTogKCkgPT4gZGlzcGF0Y2goZGVsZXRlRW50aXR5KCkpLFxuXHRvbkNoYW5nZTogKGZpZWxkUGF0aCwgdmFsdWUpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIiwgZmllbGRQYXRoOiBmaWVsZFBhdGgsIHZhbHVlOiB2YWx1ZX0pLFxuXHRvbkFkZFNlbGVjdGVkRmllbGRzOiAoZmllbGRzKSA9PiBkaXNwYXRjaChhZGRGaWVsZHNUb0VudGl0eShmaWVsZHMpKSxcblxuXHRvblJlZGlyZWN0VG9GaXJzdDogKGNvbGxlY3Rpb24sIGlkKSA9PiAobmF2aWdhdGVUbyhcImVudGl0eVwiLCBbY29sbGVjdGlvbiwgaWRdKSksXG5cblx0b25Mb2dpbkNoYW5nZTogKHJlc3BvbnNlKSA9PiBkaXNwYXRjaChzZXRVc2VyKHJlc3BvbnNlKSksXG5cdG9uU2VsZWN0VnJlOiAodnJlSWQpID0+IGRpc3BhdGNoKHNldFZyZSh2cmVJZCkpLFxuXHRvbkRpc21pc3NNZXNzYWdlOiAobWVzc2FnZUluZGV4KSA9PiBkaXNwYXRjaCh7dHlwZTogXCJESVNNSVNTX01FU1NBR0VcIiwgbWVzc2FnZUluZGV4OiBtZXNzYWdlSW5kZXh9KSxcblx0b25TZWxlY3REb21haW46IChkb21haW4pID0+IHtcblx0XHRkaXNwYXRjaChzZWxlY3REb21haW4oZG9tYWluKSk7XG5cdH0sXG5cdG9uUGFnaW5hdGVMZWZ0OiAoKSA9PiBkaXNwYXRjaChwYWdpbmF0ZUxlZnQoKSksXG5cdG9uUGFnaW5hdGVSaWdodDogKCkgPT4gZGlzcGF0Y2gocGFnaW5hdGVSaWdodCgpKSxcblx0b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlOiAodmFsdWUpID0+IGRpc3BhdGNoKHt0eXBlOiBcIlNFVF9RVUlDS1NFQVJDSF9RVUVSWVwiLCB2YWx1ZTogdmFsdWV9KSxcblx0b25RdWlja1NlYXJjaDogKCkgPT4gZGlzcGF0Y2goc2VuZFF1aWNrU2VhcmNoKCkpXG59KTsiLCJpbXBvcnQgeyBzYXZlTmV3RW50aXR5LCB1cGRhdGVFbnRpdHkgfSBmcm9tIFwiLi9jcnVkXCI7XG5cbmNvbnN0IHNhdmVSZWxhdGlvbnNWMjEgPSAoZGF0YSwgcmVsYXRpb25EYXRhLCBmaWVsZERlZnMsIHRva2VuLCB2cmVJZCwgbmV4dCkgPT4ge1xuXHQvLyBSZXR1cm5zIHRoZSBkb21haW4gYmFzZWQgb24gdGhlIGZpZWxkRGVmaW5pdGlvbnMgYW5kIHRoZSByZWxhdGlvbiBrZXkgKGkuZS4gXCJoYXNCaXJ0aFBsYWNlXCIpXG5cdGNvbnN0IG1ha2VSZWxhdGlvbkFyZ3MgPSAocmVsYXRpb24sIGtleSwgYWNjZXB0ZWQgPSB0cnVlLCBpZCA9IG51bGwsIHJldiA9IG51bGwpID0+IHtcblx0XHRjb25zdCBmaWVsZERlZiA9IGZpZWxkRGVmcy5maW5kKChkZWYpID0+IGRlZi5uYW1lID09PSBrZXkpO1xuXG5cblx0XHRjb25zdCBzb3VyY2VUeXBlID0gZGF0YVtcIkB0eXBlXCJdLnJlcGxhY2UoL3MkLywgXCJcIikucmVwbGFjZSgvXnd3LywgXCJcIik7XG5cdFx0Y29uc3QgdGFyZ2V0VHlwZSA9IGZpZWxkRGVmLnJlbGF0aW9uLnRhcmdldENvbGxlY3Rpb24ucmVwbGFjZSgvcyQvLCBcIlwiKS5yZXBsYWNlKC9ed3cvLCBcIlwiKTtcblxuXHRcdGNvbnN0IHJlbGF0aW9uU2F2ZURhdGEgPSB7XG5cdFx0XHRcIkB0eXBlXCI6IGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uQ29sbGVjdGlvbi5yZXBsYWNlKC9zJC8sIFwiXCIpLCAvLyBjaGVja1xuXHRcdFx0XCJec291cmNlSWRcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyByZWxhdGlvbi5pZCA6IGRhdGEuX2lkLCAvLyBjaGVja1xuXHRcdFx0XCJec291cmNlVHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHRhcmdldFR5cGUgOiBzb3VyY2VUeXBlLCAvLyBjaGVja1xuXHRcdFx0XCJedGFyZ2V0SWRcIjogZmllbGREZWYucmVsYXRpb24uZGlyZWN0aW9uID09PSBcIklOXCIgPyBkYXRhLl9pZCA6IHJlbGF0aW9uLmlkLCAvLyBjaGVja1xuXHRcdFx0XCJedGFyZ2V0VHlwZVwiOiBmaWVsZERlZi5yZWxhdGlvbi5kaXJlY3Rpb24gPT09IFwiSU5cIiA/IHNvdXJjZVR5cGUgOiB0YXJnZXRUeXBlLFxuXHRcdFx0XCJedHlwZUlkXCI6IGZpZWxkRGVmLnJlbGF0aW9uLnJlbGF0aW9uVHlwZUlkLCAvLyBjaGVja1xuXHRcdFx0YWNjZXB0ZWQ6IGFjY2VwdGVkXG5cdFx0fTtcblxuXHRcdGlmKGlkKSB7IHJlbGF0aW9uU2F2ZURhdGEuX2lkID0gaWQ7IH1cblx0XHRpZihyZXYpIHsgcmVsYXRpb25TYXZlRGF0YVtcIl5yZXZcIl0gPSByZXY7IH1cblx0XHRyZXR1cm4gW1xuXHRcdFx0ZmllbGREZWYucmVsYXRpb24ucmVsYXRpb25Db2xsZWN0aW9uLCAvLyBkb21haW5cblx0XHRcdHJlbGF0aW9uU2F2ZURhdGFcblx0XHRdO1xuXHR9O1xuXG5cdC8vIENvbnN0cnVjdHMgYW4gYXJyYXkgb2YgYXJndW1lbnRzIGZvciBzYXZpbmcgbmV3IHJlbGF0aW9uczpcblx0Ly8gW1xuXHQvLyAgIFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dLFxuXHQvLyAgIFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dLFxuXHQvLyBdXG5cdGNvbnN0IG5ld1JlbGF0aW9ucyA9IE9iamVjdC5rZXlzKHJlbGF0aW9uRGF0YSkubWFwKChrZXkpID0+XG5cdFx0XHRyZWxhdGlvbkRhdGFba2V5XVxuXHRcdFx0Ly8gRmlsdGVycyBvdXQgYWxsIHJlbGF0aW9ucyB3aGljaCBhcmUgbm90IGFscmVhZHkgaW4gZGF0YVtcIkByZWxhdGlvbnNcIl1cblx0XHRcdFx0LmZpbHRlcigocmVsYXRpb24pID0+IChkYXRhW1wiQHJlbGF0aW9uc1wiXVtrZXldIHx8IFtdKS5tYXAoKG9yaWdSZWxhdGlvbikgPT4gb3JpZ1JlbGF0aW9uLmlkKS5pbmRleE9mKHJlbGF0aW9uLmlkKSA8IDApXG5cdFx0XHRcdC8vIE1ha2UgYXJndW1lbnQgYXJyYXkgZm9yIG5ldyByZWxhdGlvbnM6IFtcInd3cmVsYXRpb25zXCIsIHsgLi4uIH1dXG5cdFx0XHRcdC5tYXAoKHJlbGF0aW9uKSA9PiBtYWtlUmVsYXRpb25BcmdzKHJlbGF0aW9uLCBrZXkpKVxuXHRcdC8vIEZsYXR0ZW4gbmVzdGVkIGFycmF5c1xuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXG5cdC8vIFJlYWN0aXZhdGUgcHJldmlvdXNseSBhZGRlZCByZWxhdGlvbnMgdXNpbmcgUFVUIHdoaWNoIHdlcmUgJ2RlbGV0ZWQnIGFmdGVyIHVzaW5nIFBVVFxuXHRjb25zdCByZUFkZFJlbGF0aW9ucyA9IE9iamVjdC5rZXlzKHJlbGF0aW9uRGF0YSkubWFwKChrZXkpID0+XG5cdFx0KGRhdGFbXCJAcmVsYXRpb25zXCJdW2tleV0gfHwgW10pXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IG9yaWdSZWxhdGlvbi5hY2NlcHRlZCA9PT0gZmFsc2UpXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IChyZWxhdGlvbkRhdGFba2V5XSB8fCBbXSkuZmlsdGVyKChyZWxhdGlvbikgPT4gcmVsYXRpb24uYWNjZXB0ZWQpLm1hcCgocmVsYXRpb24pID0+IHJlbGF0aW9uLmlkKS5pbmRleE9mKG9yaWdSZWxhdGlvbi5pZCkgPiAtMSlcblx0XHRcdC5tYXAoKG9yaWdSZWxhdGlvbikgPT4gbWFrZVJlbGF0aW9uQXJncyhvcmlnUmVsYXRpb24sIGtleSwgdHJ1ZSwgb3JpZ1JlbGF0aW9uLnJlbGF0aW9uSWQsIG9yaWdSZWxhdGlvbi5yZXYpKVxuXHQpLnJlZHVjZSgoYSwgYikgPT4gYS5jb25jYXQoYiksIFtdKTtcblxuXHQvLyBEZWFjdGl2YXRlIHByZXZpb3VzbHkgYWRkZWQgcmVsYXRpb25zIHVzaW5nIFBVVFxuXHRjb25zdCBkZWxldGVSZWxhdGlvbnMgPSBPYmplY3Qua2V5cyhkYXRhW1wiQHJlbGF0aW9uc1wiXSkubWFwKChrZXkpID0+XG5cdFx0ZGF0YVtcIkByZWxhdGlvbnNcIl1ba2V5XVxuXHRcdFx0LmZpbHRlcigob3JpZ1JlbGF0aW9uKSA9PiBvcmlnUmVsYXRpb24uYWNjZXB0ZWQpXG5cdFx0XHQuZmlsdGVyKChvcmlnUmVsYXRpb24pID0+IChyZWxhdGlvbkRhdGFba2V5XSB8fCBbXSkubWFwKChyZWxhdGlvbikgPT4gcmVsYXRpb24uaWQpLmluZGV4T2Yob3JpZ1JlbGF0aW9uLmlkKSA8IDApXG5cdFx0XHQubWFwKChvcmlnUmVsYXRpb24pID0+IG1ha2VSZWxhdGlvbkFyZ3Mob3JpZ1JlbGF0aW9uLCBrZXksIGZhbHNlLCBvcmlnUmVsYXRpb24ucmVsYXRpb25JZCwgb3JpZ1JlbGF0aW9uLnJldikpXG5cdCkucmVkdWNlKChhLCBiKSA9PiBhLmNvbmNhdChiKSwgW10pO1xuXG5cdC8vIENvbWJpbmVzIHNhdmVOZXdFbnRpdHkgYW5kIGRlbGV0ZUVudGl0eSBpbnN0cnVjdGlvbnMgaW50byBwcm9taXNlc1xuXHRjb25zdCBwcm9taXNlcyA9IG5ld1JlbGF0aW9uc1xuXHQvLyBNYXAgbmV3UmVsYXRpb25zIHRvIHByb21pc2VkIGludm9jYXRpb25zIG9mIHNhdmVOZXdFbnRpdHlcblx0XHQubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiBzYXZlTmV3RW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSApKVxuXHRcdC8vIE1hcCByZWFkZFJlbGF0aW9ucyB0byBwcm9taXNlZCBpbnZvY2F0aW9ucyBvZiB1cGRhdGVFbnRpdHlcblx0XHQuY29uY2F0KHJlQWRkUmVsYXRpb25zLm1hcCgoYXJncykgPT4gbmV3IFByb21pc2UoKHJlc29sdmUsIHJlamVjdCkgPT4gdXBkYXRlRW50aXR5KC4uLmFyZ3MsIHRva2VuLCB2cmVJZCwgcmVzb2x2ZSwgcmVqZWN0KSkpKVxuXHRcdC8vIE1hcCBkZWxldGVSZWxhdGlvbnMgdG8gcHJvbWlzZWQgaW52b2NhdGlvbnMgb2YgdXBkYXRlRW50aXR5XG5cdFx0LmNvbmNhdChkZWxldGVSZWxhdGlvbnMubWFwKChhcmdzKSA9PiBuZXcgUHJvbWlzZSgocmVzb2x2ZSwgcmVqZWN0KSA9PiB1cGRhdGVFbnRpdHkoLi4uYXJncywgdG9rZW4sIHZyZUlkLCByZXNvbHZlLCByZWplY3QpKSkpO1xuXG5cdC8vIEludm9rZSBhbGwgQ1JVRCBvcGVyYXRpb25zIGZvciB0aGUgcmVsYXRpb25zXG5cdFByb21pc2UuYWxsKHByb21pc2VzKS50aGVuKG5leHQsIG5leHQpO1xufTtcblxuZXhwb3J0IGRlZmF1bHQgc2F2ZVJlbGF0aW9uc1YyMTsiLCJpbXBvcnQgeGhyIGZyb20gXCJ4aHJcIjtcbmltcG9ydCBzdG9yZSBmcm9tIFwiLi4vc3RvcmVcIjtcblxuZXhwb3J0IGRlZmF1bHQge1xuXHRwZXJmb3JtWGhyOiBmdW5jdGlvbiAob3B0aW9ucywgYWNjZXB0LCByZWplY3QgPSAoKSA9PiB7IGNvbnNvbGUud2FybihcIlVuZGVmaW5lZCByZWplY3QgY2FsbGJhY2shIFwiKTsgfSwgb3BlcmF0aW9uID0gXCJTZXJ2ZXIgcmVxdWVzdFwiKSB7XG5cdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiUkVRVUVTVF9NRVNTQUdFXCIsIG1lc3NhZ2U6IGAke29wZXJhdGlvbn06ICR7b3B0aW9ucy5tZXRob2QgfHwgXCJHRVRcIn0gJHtvcHRpb25zLnVybH1gfSk7XG5cdFx0eGhyKG9wdGlvbnMsIChlcnIsIHJlc3AsIGJvZHkpID0+IHtcblx0XHRcdGlmKHJlc3Auc3RhdHVzQ29kZSA+PSA0MDApIHtcblx0XHRcdFx0c3RvcmUuZGlzcGF0Y2goe3R5cGU6IFwiRVJST1JfTUVTU0FHRVwiLCBtZXNzYWdlOiBgJHtvcGVyYXRpb259IGZhaWxlZCB3aXRoIGNhdXNlOiAke3Jlc3AuYm9keX1gfSk7XG5cdFx0XHRcdHJlamVjdChlcnIsIHJlc3AsIGJvZHkpO1xuXHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0YWNjZXB0KGVyciwgcmVzcCwgYm9keSk7XG5cdFx0XHR9XG5cdFx0fSk7XG5cdH0sXG5cblx0ZmFzdFhocjogZnVuY3Rpb24ob3B0aW9ucywgYWNjZXB0KSB7XG5cdFx0eGhyKG9wdGlvbnMsIGFjY2VwdCk7XG5cdH0sXG5cblx0bWFrZUhlYWRlcnM6IGZ1bmN0aW9uKHRva2VuLCB2cmVJZCkge1xuXHRcdHJldHVybiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIixcblx0XHRcdFwiQ29udGVudC10eXBlXCI6IFwiYXBwbGljYXRpb24vanNvblwiLFxuXHRcdFx0XCJBdXRob3JpemF0aW9uXCI6IHRva2VuLFxuXHRcdFx0XCJWUkVfSURcIjogdnJlSWRcblx0XHR9O1xuXHR9XG59O1xuIiwiaW1wb3J0IHNlcnZlciBmcm9tIFwiLi9zZXJ2ZXJcIjtcbmltcG9ydCBhY3Rpb25zIGZyb20gXCIuL2luZGV4XCI7XG5pbXBvcnQge21ha2VOZXdFbnRpdHl9IGZyb20gXCIuL2VudGl0eVwiO1xuaW1wb3J0IHtmZXRjaEVudGl0eUxpc3R9IGZyb20gXCIuL2VudGl0eVwiO1xuXG5jb25zdCBsaXN0VnJlcyA9ICgpID0+IChkaXNwYXRjaCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIlxuXHRcdH0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvc3lzdGVtL3ZyZXNgXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRkaXNwYXRjaCh7dHlwZTogXCJMSVNUX1ZSRVNcIiwgbGlzdDogSlNPTi5wYXJzZShyZXNwLmJvZHkpfSk7XG5cdH0sIG51bGwsIFwiTGlzdCBWUkVzXCIpO1xuXG5jb25zdCBzZXRWcmUgPSAodnJlSWQsIG5leHQgPSAoKSA9PiB7IH0pID0+IChkaXNwYXRjaCkgPT5cblx0c2VydmVyLnBlcmZvcm1YaHIoe1xuXHRcdG1ldGhvZDogXCJHRVRcIixcblx0XHRoZWFkZXJzOiB7XG5cdFx0XHRcIkFjY2VwdFwiOiBcImFwcGxpY2F0aW9uL2pzb25cIlxuXHRcdH0sXG5cdFx0dXJsOiBgJHtwcm9jZXNzLmVudi5zZXJ2ZXJ9L3YyLjEvbWV0YWRhdGEvJHt2cmVJZH0/d2l0aENvbGxlY3Rpb25JbmZvPXRydWVgXG5cdH0sIChlcnIsIHJlc3ApID0+IHtcblx0XHRpZiAocmVzcC5zdGF0dXNDb2RlID09PSAyMDApIHtcblx0XHRcdHZhciBib2R5ID0gSlNPTi5wYXJzZShyZXNwLmJvZHkpO1xuXHRcdFx0ZGlzcGF0Y2goe3R5cGU6IFwiU0VUX1ZSRVwiLCB2cmVJZDogdnJlSWQsIGNvbGxlY3Rpb25zOiBib2R5fSk7XG5cblx0XHRcdGxldCBkZWZhdWx0RG9tYWluID0gT2JqZWN0LmtleXMoYm9keSlcblx0XHRcdFx0Lm1hcChjb2xsZWN0aW9uTmFtZSA9PiBib2R5W2NvbGxlY3Rpb25OYW1lXSlcblx0XHRcdFx0LmZpbHRlcihjb2xsZWN0aW9uID0+ICFjb2xsZWN0aW9uLnVua25vd24gJiYgIWNvbGxlY3Rpb24ucmVsYXRpb25Db2xsZWN0aW9uKVswXVxuXHRcdFx0XHQuY29sbGVjdGlvbk5hbWU7XG5cblx0XHRcdGRpc3BhdGNoKG1ha2VOZXdFbnRpdHkoZGVmYXVsdERvbWFpbikpXG5cdFx0XHRkaXNwYXRjaCh7dHlwZTogXCJTRVRfRE9NQUlOXCIsIGRlZmF1bHREb21haW59KTtcblx0XHRcdGRpc3BhdGNoKGZldGNoRW50aXR5TGlzdChkZWZhdWx0RG9tYWluKSk7XG5cdFx0XHRuZXh0KCk7XG5cdFx0fVxuXHR9LCAoKSA9PiBkaXNwYXRjaCh7dHlwZTogXCJTRVRfVlJFXCIsIHZyZUlkOiB2cmVJZCwgY29sbGVjdGlvbnM6IHt9fSksIGBGZXRjaCBWUkUgZGVzY3JpcHRpb24gZm9yICR7dnJlSWR9YCk7XG5cblxuZXhwb3J0IHtsaXN0VnJlcywgc2V0VnJlfTtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjbGFzc25hbWVzIGZyb20gXCJjbGFzc25hbWVzXCI7XG5pbXBvcnQge3VybHN9IGZyb20gXCIuLi8uLi91cmxzXCI7XG5pbXBvcnQgeyBMaW5rIH0gZnJvbSBcInJlYWN0LXJvdXRlclwiO1xuXG5jbGFzcyBDb2xsZWN0aW9uVGFicyBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgY29sbGVjdGlvbnMsIGFjdGl2ZURvbWFpbiB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBkb21haW5zID0gT2JqZWN0LmtleXMoY29sbGVjdGlvbnMgfHwge30pO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyIGJhc2ljLW1hcmdpblwiPlxuICAgICAgICA8dWwgY2xhc3NOYW1lPVwibmF2IG5hdi10YWJzXCI+XG4gICAgICAgICAge2RvbWFpbnNcbiAgICAgICAgICAgIC5maWx0ZXIoZCA9PiAhKGNvbGxlY3Rpb25zW2RdLnVua25vd24gfHwgY29sbGVjdGlvbnNbZF0ucmVsYXRpb25Db2xsZWN0aW9uKSlcbiAgICAgICAgICAgIC5tYXAoKGRvbWFpbikgPT4gKFxuICAgICAgICAgICAgICA8bGkgY2xhc3NOYW1lPXtjbGFzc25hbWVzKHthY3RpdmU6IGRvbWFpbiA9PT0gYWN0aXZlRG9tYWlufSl9IGtleT17ZG9tYWlufT5cbiAgICAgICAgICAgICAgICA8TGluayB0bz17dXJscy5maXJzdEVudGl0eShkb21haW4pfT5cbiAgICAgICAgICAgICAgICAgIHtjb2xsZWN0aW9uc1tkb21haW5dLmNvbGxlY3Rpb25MYWJlbH1cbiAgICAgICAgICAgICAgICA8L0xpbms+XG4gICAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgICApKX1cbiAgICAgICAgPC91bD5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuQ29sbGVjdGlvblRhYnMucHJvcFR5cGVzID0ge1xuXHRvbk5ldzogUmVhY3QuUHJvcFR5cGVzLmZ1bmMsXG5cdG9uU2VsZWN0RG9tYWluOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcblx0Y29sbGVjdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdGFjdGl2ZURvbWFpbjogUmVhY3QuUHJvcFR5cGVzLnN0cmluZ1xufTtcblxuZXhwb3J0IGRlZmF1bHQgQ29sbGVjdGlvblRhYnM7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUGFnZSBmcm9tIFwiLi4vcGFnZS5qc3hcIjtcblxuaW1wb3J0IFBhZ2luYXRlIGZyb20gXCIuL2VudGl0eS1pbmRleC9wYWdpbmF0ZVwiO1xuaW1wb3J0IFF1aWNrU2VhcmNoIGZyb20gXCIuL2VudGl0eS1pbmRleC9xdWlja3NlYXJjaFwiO1xuaW1wb3J0IEVudGl0eUxpc3QgZnJvbSBcIi4vZW50aXR5LWluZGV4L2xpc3RcIjtcblxuaW1wb3J0IFNhdmVGb290ZXIgZnJvbSBcIi4vZW50aXR5LWZvcm0vc2F2ZS1mb290ZXJcIjtcbmltcG9ydCBFbnRpdHlGb3JtIGZyb20gXCIuL2VudGl0eS1mb3JtL2Zvcm1cIjtcblxuaW1wb3J0IENvbGxlY3Rpb25UYWJzIGZyb20gXCIuL2NvbGxlY3Rpb24tdGFic1wiO1xuaW1wb3J0IE1lc3NhZ2VzIGZyb20gXCIuL21lc3NhZ2VzL2xpc3RcIjtcblxuXG5jbGFzcyBFZGl0R3VpIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuXHRjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzKG5leHRQcm9wcykge1xuXHRcdGNvbnN0IHsgb25TZWxlY3QsIG9uTmV3LCBvblNlbGVjdERvbWFpbiwgb25SZWRpcmVjdFRvRmlyc3QgfSA9IHRoaXMucHJvcHM7XG5cblx0XHQvLyBUcmlnZ2VycyBmZXRjaCBkYXRhIGZyb20gc2VydmVyIGJhc2VkIG9uIGlkIGZyb20gcm91dGUuXG5cdFx0aWYgKHRoaXMucHJvcHMucGFyYW1zLmlkICE9PSBuZXh0UHJvcHMucGFyYW1zLmlkKSB7XG5cdFx0XHRvblNlbGVjdCh7ZG9tYWluOiBuZXh0UHJvcHMucGFyYW1zLmNvbGxlY3Rpb24sIGlkOiBuZXh0UHJvcHMucGFyYW1zLmlkfSk7XG5cdFx0fSBlbHNlIGlmICh0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uICE9PSBuZXh0UHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pIHtcblx0XHRcdG9uTmV3KG5leHRQcm9wcy5wYXJhbXMuY29sbGVjdGlvbik7XG5cdFx0XHRvblNlbGVjdERvbWFpbihuZXh0UHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pO1xuXHRcdH0gaWYgKChuZXh0UHJvcHMubG9jYXRpb24ucGF0aG5hbWUubWF0Y2goL1xcL2ZpcnN0JC8pIHx8IG5leHRQcm9wcy5sb2NhdGlvbi5wYXRobmFtZSA9PT0gXCIvXCIpICYmXG5cdFx0XHRcdG5leHRQcm9wcy5xdWlja1NlYXJjaC5saXN0Lmxlbmd0aCA+IDAgJiZcblx0XHRcdFx0bmV4dFByb3BzLnF1aWNrU2VhcmNoLmxpc3RbMF1bXCJAdHlwZVwiXSA9PT0gKG5leHRQcm9wcy5lbnRpdHkuZG9tYWluIHx8IFwiXCIpLnJlcGxhY2UoL3MkLywgXCJcIikpIHtcblxuXHRcdFx0b25SZWRpcmVjdFRvRmlyc3QobmV4dFByb3BzLmVudGl0eS5kb21haW4sIG5leHRQcm9wcy5xdWlja1NlYXJjaC5saXN0WzBdLl9pZCk7XG5cdFx0fVxuXHR9XG5cblx0Y29tcG9uZW50RGlkTW91bnQoKSB7XG5cblx0XHRpZiAodGhpcy5wcm9wcy5wYXJhbXMuaWQpIHtcblx0XHRcdHRoaXMucHJvcHMub25TZWxlY3Qoe2RvbWFpbjogdGhpcy5wcm9wcy5wYXJhbXMuY29sbGVjdGlvbiwgaWQ6IHRoaXMucHJvcHMucGFyYW1zLmlkfSk7XG5cdFx0fSBlbHNlIGlmICh0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKSB7XG5cdFx0XHR0aGlzLnByb3BzLm9uTmV3KHRoaXMucHJvcHMucGFyYW1zLmNvbGxlY3Rpb24pO1xuXHRcdFx0dGhpcy5wcm9wcy5vblNlbGVjdERvbWFpbih0aGlzLnByb3BzLnBhcmFtcy5jb2xsZWN0aW9uKTtcblx0XHR9IGVsc2Uge1xuXHRcdFx0Y29uc29sZS5sb2codGhpcy5wcm9wcy5sb2NhdGlvbik7XG5cdFx0fVxuXG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBvblNlbGVjdCwgb25OZXcsIG9uU2F2ZSwgb25EZWxldGUsIG9uU2VsZWN0RG9tYWluLCBvbkRpc21pc3NNZXNzYWdlLCBvbkNoYW5nZSwgb25BZGRTZWxlY3RlZEZpZWxkcyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZSwgb25RdWlja1NlYXJjaCwgb25QYWdpbmF0ZUxlZnQsIG9uUGFnaW5hdGVSaWdodCB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IGdldEF1dG9jb21wbGV0ZVZhbHVlcyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCB7IHF1aWNrU2VhcmNoLCBlbnRpdHksIHZyZSwgbWVzc2FnZXMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgY3VycmVudE1vZGUgPSBlbnRpdHkuZG9tYWluICYmIGVudGl0eS5kYXRhLl9pZCA/IFwiZWRpdFwiIDogXCJuZXdcIjtcblxuXHRcdGlmIChlbnRpdHkuZG9tYWluID09PSBudWxsIHx8ICF2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0pIHsgcmV0dXJuIG51bGw7IH1cblx0XHRyZXR1cm4gKFxuXHRcdFx0PFBhZ2U+XG5cdFx0XHRcdDxDb2xsZWN0aW9uVGFicyBjb2xsZWN0aW9ucz17dnJlLmNvbGxlY3Rpb25zfSBvbk5ldz17b25OZXd9IG9uU2VsZWN0RG9tYWluPXtvblNlbGVjdERvbWFpbn1cblx0XHRcdFx0XHRhY3RpdmVEb21haW49e2VudGl0eS5kb21haW59IC8+XG5cdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG5cdFx0XHRcdFx0PE1lc3NhZ2VzXG5cdFx0XHRcdFx0XHR0eXBlcz17W1wiU1VDQ0VTU19NRVNTQUdFXCIsIFwiRVJST1JfTUVTU0FHRVwiXX1cblx0XHRcdFx0XHRcdG1lc3NhZ2VzPXttZXNzYWdlc31cblx0XHRcdFx0XHRcdG9uRGlzbWlzc01lc3NhZ2U9e29uRGlzbWlzc01lc3NhZ2V9IC8+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJyb3dcIj5cblx0XHRcdFx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTYgY29sLW1kLTRcIj5cblx0XHRcdFx0XHRcdFx0PFF1aWNrU2VhcmNoXG5cdFx0XHRcdFx0XHRcdFx0b25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlPXtvblF1aWNrU2VhcmNoUXVlcnlDaGFuZ2V9XG5cdFx0XHRcdFx0XHRcdFx0b25RdWlja1NlYXJjaD17b25RdWlja1NlYXJjaH1cblx0XHRcdFx0XHRcdFx0XHRxdWVyeT17cXVpY2tTZWFyY2gucXVlcnl9IC8+XG5cdFx0XHRcdFx0XHRcdDxFbnRpdHlMaXN0XG5cdFx0XHRcdFx0XHRcdFx0c3RhcnQ9e3F1aWNrU2VhcmNoLnN0YXJ0fVxuXHRcdFx0XHRcdFx0XHRcdGxpc3Q9e3F1aWNrU2VhcmNoLmxpc3R9XG5cdFx0XHRcdFx0XHRcdFx0b25TZWxlY3Q9e29uU2VsZWN0fVxuXHRcdFx0XHRcdFx0XHRcdGRvbWFpbj17ZW50aXR5LmRvbWFpbn1cblx0XHRcdFx0XHRcdFx0XHRzZWxlY3RlZElkPXtlbnRpdHkuZGF0YS5faWR9XG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5UGVuZGluZz17ZW50aXR5LnBlbmRpbmd9XG5cdFx0XHRcdFx0XHRcdC8+XG5cdFx0XHRcdFx0XHQ8L2Rpdj5cblx0XHRcdFx0XHRcdHtlbnRpdHkucGVuZGluZyA/IChcblx0XHRcdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5Mb2FkaW5nLCBwbGVhc2Ugd2FpdC4uLjwvZGl2PlxuXHRcdFx0XHRcdFx0KSA6IGVudGl0eS5kb21haW4gPyAoXG5cdFx0XHRcdFx0XHRcdDxFbnRpdHlGb3JtIGN1cnJlbnRNb2RlPXtjdXJyZW50TW9kZX0gZ2V0QXV0b2NvbXBsZXRlVmFsdWVzPXtnZXRBdXRvY29tcGxldGVWYWx1ZXN9XG5cdFx0XHRcdFx0XHRcdFx0b25BZGRTZWxlY3RlZEZpZWxkcz17b25BZGRTZWxlY3RlZEZpZWxkc31cblx0XHRcdFx0XHRcdFx0XHRlbnRpdHk9e2VudGl0eX0gb25OZXc9e29uTmV3fSBvbkRlbGV0ZT17b25EZWxldGV9IG9uQ2hhbmdlPXtvbkNoYW5nZX1cblx0XHRcdFx0XHRcdFx0XHRwcm9wZXJ0aWVzPXt2cmUuY29sbGVjdGlvbnNbZW50aXR5LmRvbWFpbl0ucHJvcGVydGllc30gXG5cdFx0XHRcdFx0XHRcdFx0ZW50aXR5TGFiZWw9e3ZyZS5jb2xsZWN0aW9uc1tlbnRpdHkuZG9tYWluXS5jb2xsZWN0aW9uTGFiZWwucmVwbGFjZSgvcyQvLCBcIlwiKSB9IC8+XG5cdFx0XHRcdFx0XHQpIDogbnVsbCB9XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDxkaXYgdHlwZT1cImZvb3Rlci1ib2R5XCIgY2xhc3NOYW1lPVwicm93XCI+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtNFwiIHN0eWxlPXt7dGV4dEFsaWduOiBcImxlZnRcIiwgcGFkZGluZzogJzAnfX0+XG5cdFx0XHRcdFx0XHQ8UGFnaW5hdGVcblx0XHRcdFx0XHRcdFx0c3RhcnQ9e3F1aWNrU2VhcmNoLnN0YXJ0fVxuXHRcdFx0XHRcdFx0XHRsaXN0TGVuZ3RoPXtxdWlja1NlYXJjaC5saXN0Lmxlbmd0aH1cblx0XHRcdFx0XHRcdFx0cm93cz17NTB9XG5cdFx0XHRcdFx0XHRcdG9uUGFnaW5hdGVMZWZ0PXtvblBhZ2luYXRlTGVmdH1cblx0XHRcdFx0XHRcdFx0b25QYWdpbmF0ZVJpZ2h0PXtvblBhZ2luYXRlUmlnaHR9IC8+XG5cdFx0XHRcdFx0PC9kaXY+XG5cdFx0XHRcdFx0PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tNiBjb2wtbWQtOFwiIHN0eWxlPXt7dGV4dEFsaWduOiBcImxlZnRcIiwgcGFkZGluZzogJzAnfX0+XG5cdFx0XHRcdFx0XHR7IWVudGl0eS5wZW5kaW5nID9cblx0XHRcdFx0XHRcdFx0PFNhdmVGb290ZXIgb25TYXZlPXtvblNhdmV9IG9uQ2FuY2VsPXsoKSA9PiBjdXJyZW50TW9kZSA9PT0gXCJlZGl0XCIgP1xuXHRcdFx0XHRcdFx0XHRcdG9uU2VsZWN0KHtkb21haW46IGVudGl0eS5kb21haW4sIGlkOiBlbnRpdHkuZGF0YS5faWR9KSA6IG9uTmV3KGVudGl0eS5kb21haW4pfS8+IDogbnVsbFxuXHRcdFx0XHRcdFx0fVxuXHRcdFx0XHRcdDwvZGl2PlxuXHRcdFx0XHQ8L2Rpdj5cblx0XHRcdDwvUGFnZT5cblx0XHQpXG5cdH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgRWRpdEd1aTtcbiIsImV4cG9ydCBkZWZhdWx0IChjYW1lbENhc2UpID0+IGNhbWVsQ2FzZVxuICAucmVwbGFjZSgvKFtBLVowLTldKS9nLCAobWF0Y2gpID0+IGAgJHttYXRjaC50b0xvd2VyQ2FzZSgpfWApXG4gIC5yZXBsYWNlKC9eLi8sIChtYXRjaCkgPT4gbWF0Y2gudG9VcHBlckNhc2UoKSk7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY2FtZWwybGFiZWwgZnJvbSBcIi4vY2FtZWwybGFiZWxcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRjb25zdHJ1Y3Rvcihwcm9wcykge1xuXHRcdHN1cGVyKHByb3BzKTtcblxuXHRcdHRoaXMuc3RhdGUgPSB7IG5ld0xhYmVsOiBcIlwiLCBuZXdVcmw6IFwiXCIgfTtcblx0fVxuXG5cdGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMobmV4dFByb3BzKSB7XG5cdFx0aWYgKG5leHRQcm9wcy5lbnRpdHkuZGF0YS5faWQgIT09IHRoaXMucHJvcHMuZW50aXR5LmRhdGEuX2lkKSB7XG5cdFx0XHR0aGlzLnNldFN0YXRlKHtuZXdMYWJlbDogXCJcIiwgbmV3VXJsOiBcIlwifSlcblx0XHR9XG5cdH1cblxuXHRvbkFkZCgpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG5cdFx0aWYgKHRoaXMuc3RhdGUubmV3TGFiZWwubGVuZ3RoID4gMCAmJiB0aGlzLnN0YXRlLm5ld1VybC5sZW5ndGggPiAwKSB7XG5cdFx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHtcblx0XHRcdFx0bGFiZWw6IHRoaXMuc3RhdGUubmV3TGFiZWwsXG5cdFx0XHRcdHVybDogdGhpcy5zdGF0ZS5uZXdVcmxcblx0XHRcdH0pKTtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld0xhYmVsOiBcIlwiLCBuZXdVcmw6IFwiXCJ9KTtcblx0XHR9XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdXG5cdFx0XHQuZmlsdGVyKCh2YWwpID0+IHZhbC51cmwgIT09IHZhbHVlLnVybCkpO1xuXHR9XG5cblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cdFx0Y29uc3QgaXRlbUVsZW1lbnRzID0gdmFsdWVzLm1hcCgodmFsdWUpID0+IChcblx0XHRcdDxkaXYga2V5PXt2YWx1ZS51cmx9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPlxuXHRcdFx0XHRcdDxhIGhyZWY9e3ZhbHVlLnVybH0gdGFyZ2V0PVwiX2JsYW5rXCI+XG5cdFx0XHRcdFx0XHR7dmFsdWUubGFiZWx9XG5cdFx0XHRcdFx0PC9hPlxuXHRcdFx0XHQ8L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50c31cblx0XHRcdFx0PGRpdiBzdHlsZT17e3dpZHRoOiBcIjEwMCVcIn19PlxuXHRcdFx0XHRcdDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbCBwdWxsLWxlZnRcIiB2YWx1ZT17dGhpcy5zdGF0ZS5uZXdMYWJlbH1cblx0XHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld0xhYmVsOiBldi50YXJnZXQudmFsdWV9KX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiTGFiZWwgZm9yIHVybC4uLlwiXG5cdFx0XHRcdFx0XHRzdHlsZT17e2Rpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIG1heFdpZHRoOiBcIjUwJVwifX0gLz5cblx0XHRcdFx0XHQ8aW5wdXQgdHlwZT1cInRleHRcIiBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2wgcHVsbC1sZWZ0XCIgdmFsdWU9e3RoaXMuc3RhdGUubmV3VXJsfVxuXHRcdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gdGhpcy5zZXRTdGF0ZSh7bmV3VXJsOiBldi50YXJnZXQudmFsdWV9KX1cblx0XHRcdFx0XHRcdG9uS2V5UHJlc3M9eyhldikgPT4gZXYua2V5ID09PSBcIkVudGVyXCIgPyB0aGlzLm9uQWRkKCkgOiBmYWxzZX1cblx0XHRcdFx0XHRcdHBsYWNlaG9sZGVyPVwiVXJsLi4uXCJcblx0XHRcdFx0XHRcdHN0eWxlPXt7ZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgbWF4V2lkdGg6IFwiY2FsYyg1MCUgLSA4MHB4KVwifX0gLz5cblx0XHRcdFx0XHQ8c3BhbiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cC1idG4gcHVsbC1sZWZ0XCI+XG5cdFx0XHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e3RoaXMub25BZGQuYmluZCh0aGlzKX0+QWRkIGxpbms8L2J1dHRvbj5cblx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdDwvZGl2PlxuXG5cdFx0XHRcdDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCIsIGNsZWFyOiBcImxlZnRcIn19IC8+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0Y29uc3RydWN0b3IocHJvcHMpIHtcblx0XHRzdXBlcihwcm9wcyk7XG5cblx0XHR0aGlzLnN0YXRlID0geyBuZXdWYWx1ZTogXCJcIiB9O1xuXHR9XG5cblx0Y29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyhuZXh0UHJvcHMpIHtcblx0XHRpZiAobmV4dFByb3BzLmVudGl0eS5kYXRhLl9pZCAhPT0gdGhpcy5wcm9wcy5lbnRpdHkuZGF0YS5faWQpIHtcblx0XHRcdHRoaXMuc2V0U3RhdGUoe25ld1ZhbHVlOiBcIlwifSlcblx0XHR9XG5cdH1cblxuXHRvbkFkZCh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSkuY29uY2F0KHZhbHVlKSk7XG5cdH1cblxuXHRvblJlbW92ZSh2YWx1ZSkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRvbkNoYW5nZShbbmFtZV0sIGVudGl0eS5kYXRhW25hbWVdLmZpbHRlcigodmFsKSA9PiB2YWwgIT09IHZhbHVlKSk7XG5cdH1cblxuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdGNvbnN0IGxhYmVsID0gY2FtZWwybGFiZWwobmFtZSk7XG5cdFx0Y29uc3QgdmFsdWVzID0gKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKTtcblx0XHRjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMubWFwKCh2YWx1ZSkgPT4gKFxuXHRcdFx0PGRpdiBrZXk9e3ZhbHVlfSBjbGFzc05hbWU9XCJpdGVtLWVsZW1lbnRcIj5cblx0XHRcdFx0PHN0cm9uZz57dmFsdWV9PC9zdHJvbmc+XG5cdFx0XHRcdDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFuayBidG4teHMgcHVsbC1yaWdodFwiXG5cdFx0XHRcdFx0b25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZSh2YWx1ZSl9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpKTtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudHN9XG5cdFx0XHRcdDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiIHZhbHVlPXt0aGlzLnN0YXRlLm5ld1ZhbHVlfVxuXHRcdFx0XHRcdG9uQ2hhbmdlPXsoZXYpID0+IHRoaXMuc2V0U3RhdGUoe25ld1ZhbHVlOiBldi50YXJnZXQudmFsdWV9KX1cblx0XHRcdFx0XHRvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gdGhpcy5vbkFkZChldi50YXJnZXQudmFsdWUpIDogZmFsc2V9XG5cdFx0XHRcdFx0cGxhY2Vob2xkZXI9XCJBZGQgYSB2YWx1ZS4uLlwiIC8+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cbkZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uLy4uLy4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG5cdG9uQWRkKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgKGVudGl0eS5kYXRhW25hbWVdIHx8IFtdKS5jb25jYXQodmFsdWUpKTtcblx0fVxuXG5cdG9uUmVtb3ZlKHZhbHVlKSB7XG5cdFx0Y29uc3QgeyBuYW1lLCBlbnRpdHksIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuXHRcdG9uQ2hhbmdlKFtuYW1lXSwgZW50aXR5LmRhdGFbbmFtZV0uZmlsdGVyKCh2YWwpID0+IHZhbCAhPT0gdmFsdWUpKTtcblx0fVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG5cdFx0Y29uc3QgbGFiZWwgPSBjYW1lbDJsYWJlbChuYW1lKTtcblx0XHRjb25zdCB2YWx1ZXMgPSAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50cyA9IHZhbHVlcy5tYXAoKHZhbHVlKSA9PiAoXG5cdFx0XHQ8ZGl2IGtleT17dmFsdWV9IGNsYXNzTmFtZT1cIml0ZW0tZWxlbWVudFwiPlxuXHRcdFx0XHQ8c3Ryb25nPnt2YWx1ZX08L3N0cm9uZz5cblx0XHRcdFx0PGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcblx0XHRcdFx0XHRvbkNsaWNrPXsoKSA9PiB0aGlzLm9uUmVtb3ZlKHZhbHVlKX0+XG5cdFx0XHRcdFx0PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZW1vdmVcIiAvPlxuXHRcdFx0XHQ8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCkpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0e2l0ZW1FbGVtZW50c31cblx0XHRcdFx0PFNlbGVjdEZpZWxkIG9uQ2hhbmdlPXt0aGlzLm9uQWRkLmJpbmQodGhpcyl9IG5vQ2xlYXI9e3RydWV9IGJ0bkNsYXNzPVwiYnRuLWRlZmF1bHRcIj5cblx0XHRcdFx0XHQ8c3BhbiB0eXBlPVwicGxhY2Vob2xkZXJcIj5cblx0XHRcdFx0XHRcdFNlbGVjdCB7bGFiZWwudG9Mb3dlckNhc2UoKX1cblx0XHRcdFx0XHQ8L3NwYW4+XG5cdFx0XHRcdFx0e29wdGlvbnMuZmlsdGVyKChvcHQpID0+IHZhbHVlcy5pbmRleE9mKG9wdCkgPCAwKS5tYXAoKG9wdGlvbikgPT4gKFxuXHRcdFx0XHRcdFx0PHNwYW4ga2V5PXtvcHRpb259IHZhbHVlPXtvcHRpb259PntvcHRpb259PC9zcGFuPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHQ8L1NlbGVjdEZpZWxkPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXlcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4uLy4uLy4uL2ZpZWxkcy9zZWxlY3QtZmllbGRcIjtcblxuY2xhc3MgRmllbGQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXG4gIG9uQWRkKCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgb25DaGFuZ2UoW25hbWVdLCAoZW50aXR5LmRhdGFbbmFtZV0gfHwgW10pLmNvbmNhdCh7XG4gICAgICBjb21wb25lbnRzOiBbe3R5cGU6IG9wdGlvbnNbMF0sIHZhbHVlOiBcIlwifV1cbiAgICB9KSk7XG4gIH1cblxuICBvbkFkZENvbXBvbmVudChpdGVtSW5kZXgpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5jb25jYXQoe3R5cGU6IG9wdGlvbnNbMF0sIHZhbHVlOiBcIlwifSlcbiAgICApO1xuICB9XG5cbiAgb25SZW1vdmVDb21wb25lbnQoaXRlbUluZGV4LCBjb21wb25lbnRJbmRleCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgY3VycmVudENvbXBvbmVudHMgPSBlbnRpdHkuZGF0YVtuYW1lXVtpdGVtSW5kZXhdLmNvbXBvbmVudHM7XG4gICAgb25DaGFuZ2UoW25hbWUsIGl0ZW1JbmRleCwgXCJjb21wb25lbnRzXCJdLCBjdXJyZW50Q29tcG9uZW50c1xuICAgICAgLmZpbHRlcigoY29tcG9uZW50LCBpZHgpID0+IGlkeCAhPT0gY29tcG9uZW50SW5kZXgpXG4gICAgKTtcbiAgfVxuXG4gIG9uQ2hhbmdlQ29tcG9uZW50VmFsdWUoaXRlbUluZGV4LCBjb21wb25lbnRJbmRleCwgdmFsdWUpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5tYXAoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggPT09IGNvbXBvbmVudEluZGV4XG4gICAgICAgID8gey4uLmNvbXBvbmVudCwgdmFsdWU6IHZhbHVlfSA6IGNvbXBvbmVudFxuICAgICkpO1xuICB9XG5cbiAgb25DaGFuZ2VDb21wb25lbnRUeXBlKGl0ZW1JbmRleCwgY29tcG9uZW50SW5kZXgsIHR5cGUpIHtcbiAgICBjb25zdCB7IGVudGl0eSwgbmFtZSwgIG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGN1cnJlbnRDb21wb25lbnRzID0gZW50aXR5LmRhdGFbbmFtZV1baXRlbUluZGV4XS5jb21wb25lbnRzO1xuICAgIG9uQ2hhbmdlKFtuYW1lLCBpdGVtSW5kZXgsIFwiY29tcG9uZW50c1wiXSwgY3VycmVudENvbXBvbmVudHNcbiAgICAgIC5tYXAoKGNvbXBvbmVudCwgaWR4KSA9PiBpZHggPT09IGNvbXBvbmVudEluZGV4XG4gICAgICAgID8gey4uLmNvbXBvbmVudCwgdHlwZTogdHlwZX0gOiBjb21wb25lbnRcbiAgICApKTtcbiAgfVxuXG4gIG9uUmVtb3ZlKGl0ZW1JbmRleCkge1xuICAgIGNvbnN0IHsgZW50aXR5LCBuYW1lLCAgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgb25DaGFuZ2UoW25hbWVdLCBlbnRpdHkuZGF0YVtuYW1lXS5maWx0ZXIoKG5hbWUsIGlkeCkgPT4gaWR4ICE9PSBpdGVtSW5kZXgpKTtcbiAgfVxuXG5cdHJlbmRlcigpIHtcblx0XHRjb25zdCB7IG5hbWUsIGVudGl0eSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IHZhbHVlcyA9IChlbnRpdHkuZGF0YVtuYW1lXSB8fCBbXSk7XG5cbiAgICBjb25zdCBuYW1lRWxlbWVudHMgPSB2YWx1ZXMubWFwKChuYW1lLCBpKSA9PiAoXG4gICAgICA8ZGl2IGtleT17YCR7bmFtZX0tJHtpfWB9IGNsYXNzTmFtZT1cIm5hbWVzLWZvcm0gaXRlbS1lbGVtZW50XCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic21hbGwtbWFyZ2luXCI+XG4gICAgICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWJsYW5rIGJ0bi14cyBwdWxsLXJpZ2h0XCJcbiAgICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUoaSl9XG4gICAgICAgICAgICB0eXBlPVwiYnV0dG9uXCI+XG4gICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICAgPHN0cm9uZz5cbiAgICAgICAgICAgIHtuYW1lLmNvbXBvbmVudHMubWFwKChjb21wb25lbnQpID0+IGNvbXBvbmVudC52YWx1ZSkuam9pbihcIiBcIil9XG4gICAgICAgICAgPC9zdHJvbmc+XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8dWwga2V5PVwiY29tcG9uZW50LWxpc3RcIj5cbiAgICAgICAgICB7bmFtZS5jb21wb25lbnRzLm1hcCgoY29tcG9uZW50LCBqKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtgJHtpfS0ke2p9LWNvbXBvbmVudGB9PlxuICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwXCIga2V5PVwiY29tcG9uZW50LXZhbHVlc1wiPlxuICAgICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiaW5wdXQtZ3JvdXAtYnRuXCI+XG4gICAgICAgICAgICAgICAgICA8U2VsZWN0RmllbGQgdmFsdWU9e2NvbXBvbmVudC50eXBlfSBub0NsZWFyPXt0cnVlfVxuICAgICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KHZhbCkgPT4gdGhpcy5vbkNoYW5nZUNvbXBvbmVudFR5cGUoaSwgaiwgdmFsKX1cbiAgICAgICAgICAgICAgICAgICAgYnRuQ2xhc3M9XCJidG4tZGVmYXVsdFwiPlxuICAgICAgICAgICAgICAgICAgICB7b3B0aW9ucy5tYXAoKG9wdGlvbikgPT4gKFxuICAgICAgICAgICAgICAgICAgICAgIDxzcGFuIHZhbHVlPXtvcHRpb259IGtleT17b3B0aW9ufT57b3B0aW9ufTwvc3Bhbj5cbiAgICAgICAgICAgICAgICAgICAgKSl9XG4gICAgICAgICAgICAgICAgICA8L1NlbGVjdEZpZWxkPlxuICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICAgIDxpbnB1dCB0eXBlPVwidGV4dFwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiIGtleT17YGlucHV0LSR7aX0tJHtqfWB9XG4gICAgICAgICAgICAgICAgICBvbkNoYW5nZT17KGV2KSA9PiB0aGlzLm9uQ2hhbmdlQ29tcG9uZW50VmFsdWUoaSwgaiwgZXYudGFyZ2V0LnZhbHVlKX1cbiAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyPXtjb21wb25lbnQudHlwZX0gdmFsdWU9e2NvbXBvbmVudC52YWx1ZX0gLz5cbiAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJpbnB1dC1ncm91cC1idG5cIj5cbiAgICAgICAgICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17KCkgPT4gdGhpcy5vblJlbW92ZUNvbXBvbmVudChpLCBqKX0gPlxuICAgICAgICAgICAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgICAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgICAgICAgICA8L3NwYW4+XG4gICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApKX1cbiAgICAgICAgPC91bD5cbiAgICAgICAgICA8YnV0dG9uIG9uQ2xpY2s9eygpID0+IHRoaXMub25BZGRDb21wb25lbnQoaSl9XG4gICAgICAgICAgICAgY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0IGJ0bi14cyBwdWxsLXJpZ2h0XCIgdHlwZT1cImJ1dHRvblwiPlxuICAgICAgICAgICAgQWRkIGNvbXBvbmVudFxuICAgICAgICAgIDwvYnV0dG9uPlxuICAgICAgICAgIDxkaXYgc3R5bGU9e3t3aWR0aDogXCIxMDAlXCIsIGhlaWdodDogXCI2cHhcIiwgY2xlYXI6IFwicmlnaHRcIn19IC8+XG4gICAgICA8L2Rpdj5cbiAgICApKVxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG4gICAgICAgIHtuYW1lRWxlbWVudHN9XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17dGhpcy5vbkFkZC5iaW5kKHRoaXMpfT5cbiAgICAgICAgICBBZGQgbmFtZVxuICAgICAgICA8L2J1dHRvbj5cblx0XHRcdDwvZGl2PlxuXHRcdCk7XG5cdH1cbn1cblxuRmllbGQucHJvcFR5cGVzID0ge1xuXHRlbnRpdHk6IFJlYWN0LlByb3BUeXBlcy5vYmplY3QsXG5cdG5hbWU6IFJlYWN0LlByb3BUeXBlcy5zdHJpbmcsXG4gIG9wdGlvbnM6IFJlYWN0LlByb3BUeXBlcy5hcnJheSxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBGaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IHsgTGluayB9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7IHVybHMgfSBmcm9tIFwiLi4vLi4vLi4vLi4vdXJsc1wiO1xuXG5jbGFzcyBSZWxhdGlvbkZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgcXVlcnk6IFwiXCIsXG4gICAgICBzdWdnZXN0aW9uczogW10sXG4gICAgICBibHVySXNCbG9ja2VkOiBmYWxzZVxuICAgIH1cbiAgfVxuXG4gIG9uUmVtb3ZlKHZhbHVlKSB7XG4gICAgY29uc3QgY3VycmVudFZhbHVlcyA9IHRoaXMucHJvcHMuZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdW3RoaXMucHJvcHMubmFtZV0gfHwgW107XG5cbiAgICB0aGlzLnByb3BzLm9uQ2hhbmdlKFxuICAgICAgW1wiQHJlbGF0aW9uc1wiLCB0aGlzLnByb3BzLm5hbWVdLFxuICAgICAgY3VycmVudFZhbHVlcy5maWx0ZXIoKGN1clZhbCkgPT4gY3VyVmFsLmlkICE9PSB2YWx1ZS5pZClcbiAgICApO1xuXG4gIH1cblxuICBvbkFkZChzdWdnZXN0aW9uKSB7XG4gICAgY29uc3QgY3VycmVudFZhbHVlcyA9IHRoaXMucHJvcHMuZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdW3RoaXMucHJvcHMubmFtZV0gfHwgW107XG4gICAgaWYgKGN1cnJlbnRWYWx1ZXMubWFwKCh2YWwpID0+IHZhbC5pZCkuaW5kZXhPZihzdWdnZXN0aW9uLmtleSkgPiAtMSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cbiAgICB0aGlzLnNldFN0YXRlKHtzdWdnZXN0aW9uczogW10sIHF1ZXJ5OiBcIlwiLCBibHVySXNCbG9ja2VkOiBmYWxzZX0pO1xuXG4gICAgdGhpcy5wcm9wcy5vbkNoYW5nZShcbiAgICAgIFtcIkByZWxhdGlvbnNcIiwgdGhpcy5wcm9wcy5uYW1lXSxcbiAgICAgIGN1cnJlbnRWYWx1ZXMuY29uY2F0KHtcbiAgICAgICAgaWQ6IHN1Z2dlc3Rpb24ua2V5LFxuICAgICAgICBkaXNwbGF5TmFtZTogc3VnZ2VzdGlvbi52YWx1ZSxcbiAgICAgICAgYWNjZXB0ZWQ6IHRydWVcbiAgICAgIH0pXG4gICAgKTtcbiAgfVxuXG4gIG9uUXVlcnlDaGFuZ2UoZXYpIHtcbiAgICBjb25zdCB7IGdldEF1dG9jb21wbGV0ZVZhbHVlcywgcGF0aCB9ID0gdGhpcy5wcm9wcztcbiAgICB0aGlzLnNldFN0YXRlKHtxdWVyeTogZXYudGFyZ2V0LnZhbHVlfSk7XG4gICAgaWYgKGV2LnRhcmdldC52YWx1ZSA9PT0gXCJcIikge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IFtdfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIGdldEF1dG9jb21wbGV0ZVZhbHVlcyhwYXRoLCBldi50YXJnZXQudmFsdWUsIChyZXN1bHRzKSA9PiB7XG4gICAgICAgIHRoaXMuc2V0U3RhdGUoe3N1Z2dlc3Rpb25zOiByZXN1bHRzfSk7XG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICBvblF1ZXJ5Q2xlYXIoZXYpIHtcbiAgICBpZiAoIXRoaXMuc3RhdGUuYmx1cklzQmxvY2tlZCkge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7c3VnZ2VzdGlvbnM6IFtdLCBxdWVyeTogXCJcIn0pO1xuICAgIH1cbiAgfVxuXG4gIG9uQmx1ckJsb2NrKHRvZ2dsZSkge1xuICAgIHRoaXMuc2V0U3RhdGUoe2JsdXJJc0Jsb2NrZWQ6IHRvZ2dsZX0pO1xuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSwgdGFyZ2V0Q29sbGVjdGlvbiB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB2YWx1ZXMgPSBlbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl1bdGhpcy5wcm9wcy5uYW1lXSB8fCBbXTtcbiAgICBjb25zdCBpdGVtRWxlbWVudHMgPSB2YWx1ZXMuZmlsdGVyKCh2YWwpID0+IHZhbC5hY2NlcHRlZCkubWFwKCh2YWx1ZSwgaSkgPT4gKFxuICAgICAgPGRpdiBrZXk9e2Ake2l9LSR7dmFsdWUuaWR9YH0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG4gICAgICAgIDxMaW5rIHRvPXt1cmxzLmVudGl0eSh0YXJnZXRDb2xsZWN0aW9uLCB2YWx1ZS5pZCl9ID57dmFsdWUuZGlzcGxheU5hbWV9PC9MaW5rPlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuICAgICAgICAgIG9uQ2xpY2s9eygpID0+IHRoaXMub25SZW1vdmUodmFsdWUpfT5cbiAgICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlbW92ZVwiIC8+XG4gICAgICAgIDwvYnV0dG9uPlxuICAgICAgPC9kaXY+XG4gICAgKSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW5cIj5cbiAgICAgICAgPGg0PntjYW1lbDJsYWJlbChuYW1lKX08L2g0PlxuICAgICAgICB7aXRlbUVsZW1lbnRzfVxuICAgICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCJcbiAgICAgICAgICAgICAgIG9uQmx1cj17dGhpcy5vblF1ZXJ5Q2xlYXIuYmluZCh0aGlzKX1cbiAgICAgICAgICAgICAgIG9uQ2hhbmdlPXt0aGlzLm9uUXVlcnlDaGFuZ2UuYmluZCh0aGlzKX1cbiAgICAgICAgICAgICAgIHZhbHVlPXt0aGlzLnN0YXRlLnF1ZXJ5fSBwbGFjZWhvbGRlcj1cIlNlYXJjaC4uLlwiIC8+XG5cbiAgICAgICAgPGRpdiBvbk1vdXNlT3Zlcj17KCkgPT4gdGhpcy5vbkJsdXJCbG9jayh0cnVlKX1cbiAgICAgICAgICAgICBvbk1vdXNlT3V0PXsoKSA9PiB0aGlzLm9uQmx1ckJsb2NrKGZhbHNlKX1cbiAgICAgICAgICAgICBzdHlsZT17e292ZXJmbG93WTogXCJhdXRvXCIsIG1heEhlaWdodDogXCIzMDBweFwifX0+XG4gICAgICAgICAge3RoaXMuc3RhdGUuc3VnZ2VzdGlvbnMubWFwKChzdWdnZXN0aW9uLCBpKSA9PiAoXG4gICAgICAgICAgICA8YSBrZXk9e2Ake2l9LSR7c3VnZ2VzdGlvbi5rZXl9YH0gY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCJcbiAgICAgICAgICAgICAgb25DbGljaz17KCkgPT4gdGhpcy5vbkFkZChzdWdnZXN0aW9uKX0+XG4gICAgICAgICAgICAgIHtzdWdnZXN0aW9uLnZhbHVlfVxuICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICkpfVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn1cblxuZXhwb3J0IGRlZmF1bHQgUmVsYXRpb25GaWVsZDtcbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9jYW1lbDJsYWJlbFwiO1xuaW1wb3J0IFNlbGVjdEZpZWxkIGZyb20gXCIuLi8uLi8uLi9maWVsZHMvc2VsZWN0LWZpZWxkXCI7XG5cbmNsYXNzIEZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSwgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXHRcdGNvbnN0IGl0ZW1FbGVtZW50ID0gZW50aXR5LmRhdGFbbmFtZV0gJiYgZW50aXR5LmRhdGFbbmFtZV0ubGVuZ3RoID4gMCA/IChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiaXRlbS1lbGVtZW50XCI+XG5cdFx0XHRcdDxzdHJvbmc+e2VudGl0eS5kYXRhW25hbWVdfTwvc3Ryb25nPlxuXHRcdFx0XHQ8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tYmxhbmsgYnRuLXhzIHB1bGwtcmlnaHRcIlxuXHRcdFx0XHRcdG9uQ2xpY2s9eygpID0+IG9uQ2hhbmdlKFtuYW1lXSwgXCJcIil9PlxuXHRcdFx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cblx0XHRcdFx0PC9idXR0b24+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpIDogbnVsbDtcblxuXHRcdHJldHVybiAoXG5cdFx0XHQ8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuXHRcdFx0XHQ8aDQ+e2xhYmVsfTwvaDQ+XG5cdFx0XHRcdHtpdGVtRWxlbWVudH1cblx0XHRcdFx0PFNlbGVjdEZpZWxkXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyh2YWx1ZSkgPT4gb25DaGFuZ2UoW25hbWVdLCB2YWx1ZSl9XG5cdFx0XHRcdFx0bm9DbGVhcj17dHJ1ZX0gYnRuQ2xhc3M9XCJidG4tZGVmYXVsdFwiPlxuXHRcdFx0XHRcdDxzcGFuIHR5cGU9XCJwbGFjZWhvbGRlclwiPlxuXHRcdFx0XHRcdFx0U2VsZWN0IHtsYWJlbC50b0xvd2VyQ2FzZSgpfVxuXHRcdFx0XHRcdDwvc3Bhbj5cblx0XHRcdFx0XHR7b3B0aW9ucy5tYXAoKG9wdGlvbikgPT4gKFxuXHRcdFx0XHRcdFx0PHNwYW4ga2V5PXtvcHRpb259IHZhbHVlPXtvcHRpb259PntvcHRpb259PC9zcGFuPlxuXHRcdFx0XHRcdCkpfVxuXHRcdFx0XHQ8L1NlbGVjdEZpZWxkPlxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5GaWVsZC5wcm9wVHlwZXMgPSB7XG5cdGVudGl0eTogUmVhY3QuUHJvcFR5cGVzLm9iamVjdCxcblx0bmFtZTogUmVhY3QuUHJvcFR5cGVzLnN0cmluZyxcblx0b25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuXHRvcHRpb25zOiBSZWFjdC5Qcm9wVHlwZXMuYXJyYXlcbn07XG5cbmV4cG9ydCBkZWZhdWx0IEZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IGNhbWVsMmxhYmVsIGZyb20gXCIuL2NhbWVsMmxhYmVsXCI7XG5cbmNsYXNzIFN0cmluZ0ZpZWxkIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblx0cmVuZGVyKCkge1xuXHRcdGNvbnN0IHsgbmFtZSwgZW50aXR5LCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcblx0XHRjb25zdCBsYWJlbCA9IGNhbWVsMmxhYmVsKG5hbWUpO1xuXG5cdFx0cmV0dXJuIChcblx0XHRcdDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG5cdFx0XHRcdDxoND57bGFiZWx9PC9oND5cblx0XHRcdFx0PGlucHV0IGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG5cdFx0XHRcdFx0b25DaGFuZ2U9eyhldikgPT4gb25DaGFuZ2UoW25hbWVdLCBldi50YXJnZXQudmFsdWUpfVxuXHRcdFx0XHRcdHZhbHVlPXtlbnRpdHkuZGF0YVtuYW1lXSB8fCBcIlwifVxuXHRcdFx0XHRcdHBsYWNlaG9sZGVyPXtgRW50ZXIgJHtsYWJlbC50b0xvd2VyQ2FzZSgpfWB9XG5cdFx0XHRcdC8+XG5cdFx0XHQ8L2Rpdj5cblx0XHQpO1xuXHR9XG59XG5cblN0cmluZ0ZpZWxkLnByb3BUeXBlcyA9IHtcblx0ZW50aXR5OiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRuYW1lOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuXHRvbkNoYW5nZTogUmVhY3QuUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFN0cmluZ0ZpZWxkO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiXG5cbmltcG9ydCBTdHJpbmdGaWVsZCBmcm9tIFwiLi9maWVsZHMvc3RyaW5nLWZpZWxkXCI7XG5pbXBvcnQgU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL3NlbGVjdFwiO1xuaW1wb3J0IE11bHRpU2VsZWN0RmllbGQgZnJvbSBcIi4vZmllbGRzL211bHRpLXNlbGVjdFwiO1xuaW1wb3J0IFJlbGF0aW9uRmllbGQgZnJvbSBcIi4vZmllbGRzL3JlbGF0aW9uXCI7XG5pbXBvcnQgU3RyaW5nTGlzdEZpZWxkIGZyb20gXCIuL2ZpZWxkcy9saXN0LW9mLXN0cmluZ3NcIjtcbmltcG9ydCBMaW5rRmllbGQgZnJvbSBcIi4vZmllbGRzL2xpbmtzXCI7XG5pbXBvcnQgTmFtZXNGaWVsZCBmcm9tIFwiLi9maWVsZHMvbmFtZXNcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQgeyB1cmxzIH0gZnJvbSBcIi4uLy4uLy4uL3VybHNcIjtcbmltcG9ydCBjYW1lbDJsYWJlbCBmcm9tIFwiLi9maWVsZHMvY2FtZWwybGFiZWxcIjtcblxuY29uc3QgZmllbGRNYXAgPSB7XG5cdFwic3RyaW5nXCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U3RyaW5nRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwidGV4dFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPFN0cmluZ0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gLz4pLFxuXHRcImRhdGFibGVcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdGaWVsZCB7Li4ucHJvcHN9IG5hbWU9e2ZpZWxkRGVmLm5hbWV9IC8+KSxcblx0XCJtdWx0aXNlbGVjdFwiOiAoZmllbGREZWYsIHByb3BzKSA9PiAoPE11bHRpU2VsZWN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPiksXG5cdFwic2VsZWN0XCI6IChmaWVsZERlZiwgcHJvcHMpID0+ICg8U2VsZWN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSBvcHRpb25zPXtmaWVsZERlZi5vcHRpb25zfSAvPiksXG5cdFwicmVsYXRpb25cIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxSZWxhdGlvbkZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gdGFyZ2V0Q29sbGVjdGlvbj17ZmllbGREZWYucmVsYXRpb24udGFyZ2V0Q29sbGVjdGlvbn0gcGF0aD17ZmllbGREZWYucXVpY2tzZWFyY2h9IC8+KSxcbiAgXCJsaXN0LW9mLXN0cmluZ3NcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxTdHJpbmdMaXN0RmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG4gIFwibGlua3NcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxMaW5rRmllbGQgey4uLnByb3BzfSBuYW1lPXtmaWVsZERlZi5uYW1lfSAvPiksXG5cdFwibmFtZXNcIjogKGZpZWxkRGVmLCBwcm9wcykgPT4gKDxOYW1lc0ZpZWxkIHsuLi5wcm9wc30gbmFtZT17ZmllbGREZWYubmFtZX0gb3B0aW9ucz17ZmllbGREZWYub3B0aW9uc30gLz4pXG59O1xuXG5jbGFzcyBFbnRpdHlGb3JtIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcblxuICBjb25zdHJ1Y3Rvcihwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBmaWVsZHNUb0FkZDogW11cbiAgICB9XG4gIH1cblxuICB0b2dnbGVGaWVsZFRvQWRkKGZpZWxkTmFtZSkge1xuICAgIGlmICh0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLmluZGV4T2YoZmllbGROYW1lKSA+IC0xKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtmaWVsZHNUb0FkZDogdGhpcy5zdGF0ZS5maWVsZHNUb0FkZC5maWx0ZXIoKGZBZGQpID0+IGZBZGQgIT09IGZpZWxkTmFtZSl9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7ZmllbGRzVG9BZGQ6IHRoaXMuc3RhdGUuZmllbGRzVG9BZGQuY29uY2F0KGZpZWxkTmFtZSl9KTtcbiAgICB9XG4gIH1cblxuICBvbkFkZFNlbGVjdGVkRmllbGRzKCkge1xuICAgIGNvbnN0IHsgcHJvcGVydGllcyB9ID0gdGhpcy5wcm9wcztcblxuICAgIHRoaXMucHJvcHMub25BZGRTZWxlY3RlZEZpZWxkcyh0aGlzLnN0YXRlLmZpZWxkc1RvQWRkLm1hcCgoZkFkZCkgPT4gKHtcbiAgICAgIG5hbWU6IGZBZGQsXG4gICAgICB0eXBlOiBwcm9wZXJ0aWVzLmZpbmQoKHByb3ApID0+IHByb3AubmFtZSA9PT0gZkFkZCkudHlwZVxuICAgIH0pKSk7XG5cbiAgICB0aGlzLnNldFN0YXRlKHtmaWVsZHNUb0FkZDogW119KTtcbiAgfVxuXG4gIHJlbmRlcigpIHtcbiAgICBjb25zdCB7IG9uRGVsZXRlLCBvbkNoYW5nZSwgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgZW50aXR5LCBjdXJyZW50TW9kZSwgcHJvcGVydGllcywgZW50aXR5TGFiZWwgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBmaWVsZHNUb0FkZCB9ID0gdGhpcy5zdGF0ZTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS02IGNvbC1tZC04XCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiYmFzaWMtbWFyZ2luXCI+XG4gICAgICAgICAgPExpbmsgdG89e3VybHMubmV3RW50aXR5KGVudGl0eS5kb21haW4pfSBjbGFzc05hbWU9XCJidG4gYnRuLXByaW1hcnkgcHVsbC1yaWdodFwiPlxuICAgICAgICAgICAgTmV3IHtlbnRpdHlMYWJlbH1cbiAgICAgICAgICA8L0xpbms+XG4gICAgICAgIDwvZGl2PlxuXG5cbiAgICAgICAge3Byb3BlcnRpZXNcbiAgICAgICAgICAuZmlsdGVyKChmaWVsZERlZikgPT4gIWZpZWxkTWFwLmhhc093blByb3BlcnR5KGZpZWxkRGVmLnR5cGUpKVxuICAgICAgICAgIC5tYXAoKGZpZWxkRGVmLCBpKSA9PiAoPGRpdiBrZXk9e2l9IHN0eWxlPXt7XCJjb2xvclwiOiBcInJlZFwifX0+PHN0cm9uZz5GaWVsZCB0eXBlIG5vdCBzdXBwb3J0ZWQ6IHtmaWVsZERlZi50eXBlfTwvc3Ryb25nPjwvZGl2PikpfVxuXG4gICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+IGZpZWxkTWFwLmhhc093blByb3BlcnR5KGZpZWxkRGVmLnR5cGUpKVxuICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBlbnRpdHkuZGF0YS5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi5uYW1lKSB8fCBlbnRpdHkuZGF0YVtcIkByZWxhdGlvbnNcIl0uaGFzT3duUHJvcGVydHkoZmllbGREZWYubmFtZSkpXG4gICAgICAgICAgLm1hcCgoZmllbGREZWYsIGkpID0+XG4gICAgICAgICAgZmllbGRNYXBbZmllbGREZWYudHlwZV0oZmllbGREZWYsIHtcblx0XHRcdFx0XHRcdGtleTogYCR7aX0tJHtmaWVsZERlZi5uYW1lfWAsXG5cdFx0XHRcdFx0XHRlbnRpdHk6IGVudGl0eSxcblx0XHRcdFx0XHRcdG9uQ2hhbmdlOiBvbkNoYW5nZSxcblx0XHRcdFx0XHRcdGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzXG5cdFx0XHRcdFx0fSlcbiAgICAgICAgKX1cblxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpbiBhZGQtZmllbGQtZm9ybVwiPlxuICAgICAgICAgIDxoND5BZGQgZmllbGRzPC9oND5cbiAgICAgICAgICA8ZGl2IHN0eWxlPXt7bWF4SGVpZ2h0OiBcIjI1MHB4XCIsIG92ZXJmbG93WTogXCJhdXRvXCJ9fT5cbiAgICAgICAgICAgIHtwcm9wZXJ0aWVzXG4gICAgICAgICAgICAgIC5maWx0ZXIoKGZpZWxkRGVmKSA9PiBmaWVsZE1hcC5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi50eXBlKSlcbiAgICAgICAgICAgICAgLmZpbHRlcigoZmllbGREZWYpID0+ICFlbnRpdHkuZGF0YS5oYXNPd25Qcm9wZXJ0eShmaWVsZERlZi5uYW1lKSAmJiAhZW50aXR5LmRhdGFbXCJAcmVsYXRpb25zXCJdLmhhc093blByb3BlcnR5KGZpZWxkRGVmLm5hbWUpKVxuICAgICAgICAgICAgICAubWFwKChmaWVsZERlZiwgaSkgPT4gKFxuICAgICAgICAgICAgICAgIDxkaXYga2V5PXtpfSBvbkNsaWNrPXsoKSA9PiB0aGlzLnRvZ2dsZUZpZWxkVG9BZGQoZmllbGREZWYubmFtZSl9XG4gICAgICAgICAgICAgICAgICAgICBjbGFzc05hbWU9e2ZpZWxkc1RvQWRkLmluZGV4T2YoZmllbGREZWYubmFtZSkgPiAtMSA/IFwic2VsZWN0ZWRcIiA6IFwiXCJ9PlxuICAgICAgICAgICAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwicHVsbC1yaWdodFwiPih7ZmllbGREZWYudHlwZX0pPC9zcGFuPlxuICAgICAgICAgICAgICAgICAge2NhbWVsMmxhYmVsKGZpZWxkRGVmLm5hbWUpfVxuICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICApKVxuICAgICAgICAgICAgfVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgb25DbGljaz17dGhpcy5vbkFkZFNlbGVjdGVkRmllbGRzLmJpbmQodGhpcyl9PkFkZCBzZWxlY3RlZCBmaWVsZHM8L2J1dHRvbj5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIHtjdXJyZW50TW9kZSA9PT0gXCJlZGl0XCJcbiAgICAgICAgICA/ICg8ZGl2IGNsYXNzTmFtZT1cImJhc2ljLW1hcmdpblwiPlxuICAgICAgICAgICAgICA8aDQ+RGVsZXRlPC9oND5cbiAgICAgICAgICAgICAgPGJ1dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGFuZ2VyXCIgb25DbGljaz17b25EZWxldGV9PlxuICAgICAgICAgICAgICAgIERlbGV0ZSB7ZW50aXR5TGFiZWx9XG4gICAgICAgICAgICAgIDwvYnV0b24+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICApIDogbnVsbH1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBFbnRpdHlGb3JtO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IG9uU2F2ZSwgb25DYW5jZWwgfSA9IHByb3BzO1xuXG4gIHJldHVybiAoXG4gICAgPGRpdj5cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1wcmltYXJ5XCIgb25DbGljaz17b25TYXZlfT5TYXZlPC9idXR0b24+XG4gICAgICB7XCIgXCJ9b3J7XCIgXCJ9XG4gICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tbGlua1wiIG9uQ2xpY2s9e29uQ2FuY2VsfT5DYW5jZWw8L2J1dHRvbj5cbiAgICA8L2Rpdj5cbiAgKTtcbn1cbiIsImltcG9ydCBSZWFjdCBmcm9tIFwicmVhY3RcIjtcbmltcG9ydCB7IExpbmsgfSBmcm9tIFwicmVhY3Qtcm91dGVyXCI7XG5pbXBvcnQgeyB1cmxzIH0gZnJvbSBcIi4uLy4uLy4uL3VybHNcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBzdGFydCwgbGlzdCwgZG9tYWluLCBzZWxlY3RlZElkLCBlbnRpdHlQZW5kaW5nIH0gPSBwcm9wcztcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwicmVzdWx0LWxpc3QgcmVzdWx0LWxpc3QtZWRpdFwiPlxuICAgICAgPG9sIHN0YXJ0PXtzdGFydCArIDF9IHN0eWxlPXt7Y291bnRlclJlc2V0OiBgc3RlcC1jb3VudGVyICR7c3RhcnR9YH19PlxuICAgICAgICB7bGlzdC5tYXAoKGVudHJ5LCBpKSA9PiAoXG4gICAgICAgICAgPGxpIGtleT17YCR7aX0tJHtlbnRyeS5faWR9YH0+XG4gICAgICAgICAgICB7ZW50aXR5UGVuZGluZ1xuICAgICAgICAgICAgICA/IChcbiAgICAgICAgICAgICAgICA8YSBzdHlsZT17e1xuICAgICAgICAgICAgICAgICAgZGlzcGxheTogXCJpbmxpbmUtYmxvY2tcIiwgd2lkdGg6IFwiY2FsYygxMDAlIC0gMzBweClcIiwgaGVpZ2h0OiBcIjEwMCVcIiwgcGFkZGluZzogXCIwLjVlbSAwXCIsXG4gICAgICAgICAgICAgICAgICBjdXJzb3I6IFwiZGVmYXVsdFwiLCBvcGFjaXR5OiBcIjAuNVwiLCB0ZXh0RGVjb3JhdGlvbjogXCJub25lXCIsIGZvbnRXZWlnaHQ6IFwiMzAwXCJcbiAgICAgICAgICAgICAgICB9fT5cbiAgICAgICAgICAgICAgICAgIHtlbnRyeVtcIkBkaXNwbGF5TmFtZVwiXX1cbiAgICAgICAgICAgICAgICA8L2E+XG4gICAgICAgICAgICAgICkgOiAoXG4gICAgICAgICAgICAgICAgPExpbmsgdG89e3VybHMuZW50aXR5KGRvbWFpbiwgZW50cnkuX2lkKX0gc3R5bGU9e3tcbiAgICAgICAgICAgICAgICAgIGRpc3BsYXk6IFwiaW5saW5lLWJsb2NrXCIsIHdpZHRoOiBcImNhbGMoMTAwJSAtIDMwcHgpXCIsIGhlaWdodDogXCIxMDAlXCIsIHBhZGRpbmc6IFwiMC41ZW0gMFwiLFxuICAgICAgICAgICAgICAgICAgZm9udFdlaWdodDogc2VsZWN0ZWRJZCA9PT0gZW50cnkuX2lkID8gXCI1MDBcIiA6IFwiMzAwXCJcbiAgICAgICAgICAgICAgICB9fT5cblxuICAgICAgICAgICAgICAgICAge2VudHJ5W1wiQGRpc3BsYXlOYW1lXCJdfVxuICAgICAgICAgICAgICAgIDwvTGluaz5cbiAgICAgICAgICAgICAgKVxuICAgICAgICAgICAgfVxuICAgICAgICAgIDwvbGk+XG4gICAgICAgICkpfVxuICAgICAgPC9vbD5cbiAgICA8L2Rpdj5cbiAgKVxufVxuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihwcm9wcykge1xuICBjb25zdCB7IG9uUGFnaW5hdGVMZWZ0LCBvblBhZ2luYXRlUmlnaHQgfSA9IHByb3BzO1xuICBjb25zdCB7IHN0YXJ0LCByb3dzLCBsaXN0TGVuZ3RoIH0gPSBwcm9wcztcblxuXG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2PlxuICAgICAgPGJ1dHRvbiBjbGFzc05hbWU9XCJidG4gYnRuLWRlZmF1bHRcIiBkaXNhYmxlZD17c3RhcnQgPT09IDB9IG9uQ2xpY2s9e29uUGFnaW5hdGVMZWZ0fT5cbiAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1jaGV2cm9uLWxlZnRcIiAvPlxuICAgICAgPC9idXR0b24+XG4gICAgICB7XCIgXCJ9e3N0YXJ0ICsgMX0gLSB7c3RhcnQgKyByb3dzfXtcIiBcIn1cbiAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCIgZGlzYWJsZWQ9e2xpc3RMZW5ndGggPCByb3dzfSBvbkNsaWNrPXtvblBhZ2luYXRlUmlnaHR9PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJnbHlwaGljb24gZ2x5cGhpY29uLWNoZXZyb24tcmlnaHRcIiAvPlxuICAgICAgPC9idXR0b24+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHByb3BzKSB7XG4gIGNvbnN0IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlLCBvblF1aWNrU2VhcmNoLCBxdWVyeSB9ID0gcHJvcHM7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImlucHV0LWdyb3VwIHNtYWxsLW1hcmdpbiBcIj5cbiAgICAgIDxpbnB1dCB0eXBlPVwidGV4dFwiIHBsYWNlaG9sZGVyPVwiU2VhcmNoIGZvci4uLlwiIGNsYXNzTmFtZT1cImZvcm0tY29udHJvbFwiXG4gICAgICAgIG9uQ2hhbmdlPXsoZXYpID0+IG9uUXVpY2tTZWFyY2hRdWVyeUNoYW5nZShldi50YXJnZXQudmFsdWUpfVxuICAgICAgICBvbktleVByZXNzPXsoZXYpID0+IGV2LmtleSA9PT0gXCJFbnRlclwiID8gb25RdWlja1NlYXJjaCgpIDogZmFsc2V9XG4gICAgICAgIHZhbHVlPXtxdWVyeX1cbiAgICAgICAgLz5cbiAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImlucHV0LWdyb3VwLWJ0blwiPlxuICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiIG9uQ2xpY2s9e29uUXVpY2tTZWFyY2h9PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tc2VhcmNoXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPVwiYnRuIGJ0bi1ibGFua1wiIG9uQ2xpY2s9eygpID0+IHsgb25RdWlja1NlYXJjaFF1ZXJ5Q2hhbmdlKFwiXCIpOyBvblF1aWNrU2VhcmNoKCk7IH19PlxuICAgICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tcmVtb3ZlXCIgLz5cbiAgICAgICAgPC9idXR0b24+XG4gICAgICA8L3NwYW4+XG4gICAgPC9kaXY+XG4gICk7XG59XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcbmltcG9ydCBNZXNzYWdlIGZyb20gXCIuLi8uLi9tZXNzYWdlXCI7XG5cbmNvbnN0IExBQkVMUyA9IHtcblx0XCJTVUNDRVNTX01FU1NBR0VcIjogXCJcIixcblx0XCJFUlJPUl9NRVNTQUdFXCI6IChcblx0XHQ8c3Bhbj5cblx0XHRcdDxzcGFuIGNsYXNzTmFtZT1cImdseXBoaWNvbiBnbHlwaGljb24tZXhjbGFtYXRpb24tc2lnblwiIC8+IFdhcm5pbmchXG5cdFx0PC9zcGFuPlxuXHQpXG59O1xuXG5jb25zdCBBTEVSVF9MRVZFTFMgPSB7XG5cdFwiU1VDQ0VTU19NRVNTQUdFXCI6IFwiaW5mb1wiLFxuXHRcIkVSUk9SX01FU1NBR0VcIjogXCJkYW5nZXJcIlxufTtcblxuY2xhc3MgTWVzc2FnZXMgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuXHRyZW5kZXIoKSB7XG5cdFx0Y29uc3QgeyBtZXNzYWdlcywgdHlwZXMsIG9uRGlzbWlzc01lc3NhZ2UgfSA9IHRoaXMucHJvcHM7XG5cblx0XHRjb25zdCBmaWx0ZXJlZE1lc3NhZ2VzID0gbWVzc2FnZXMubG9nXG5cdFx0XHQubWFwKChtc2csIGlkeCkgPT4gKHttZXNzYWdlOiBtc2cubWVzc2FnZSwgaW5kZXg6IGlkeCwgdHlwZTogbXNnLnR5cGUsIGRpc21pc3NlZDogbXNnLmRpc21pc3NlZCB9KSlcblx0XHRcdC5maWx0ZXIoKG1zZykgPT4gdHlwZXMuaW5kZXhPZihtc2cudHlwZSkgPiAtMSAmJiAhbXNnLmRpc21pc3NlZCk7XG5cblx0XHRyZXR1cm4gKFxuXHRcdFx0PGRpdj5cblx0XHRcdFx0e2ZpbHRlcmVkTWVzc2FnZXMubWFwKChtc2cpID0+IChcblx0XHRcdFx0XHQ8TWVzc2FnZSBrZXk9e21zZy5pbmRleH1cblx0XHRcdFx0XHRcdGRpc21pc3NpYmxlPXt0cnVlfVxuXHRcdFx0XHRcdFx0YWxlcnRMZXZlbD17QUxFUlRfTEVWRUxTW21zZy50eXBlXX1cblx0XHRcdFx0XHRcdG9uQ2xvc2VNZXNzYWdlPXsoKSA9PiBvbkRpc21pc3NNZXNzYWdlKG1zZy5pbmRleCl9PlxuXHRcdFx0XHRcdFx0PHN0cm9uZz57TEFCRUxTW21zZy50eXBlXX08L3N0cm9uZz4gPHNwYW4+e21zZy5tZXNzYWdlfTwvc3Bhbj5cblx0XHRcdFx0XHQ8L01lc3NhZ2U+XG5cdFx0XHRcdCkpfVxuXHRcdFx0PC9kaXY+XG5cdFx0KTtcblx0fVxufVxuXG5NZXNzYWdlcy5wcm9wVHlwZXMgPSB7XG5cdG1lc3NhZ2VzOiBSZWFjdC5Qcm9wVHlwZXMub2JqZWN0LFxuXHRvbkRpc21pc3NNZXNzYWdlOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYy5pc1JlcXVpcmVkLFxuXHR0eXBlczogUmVhY3QuUHJvcFR5cGVzLmFycmF5LmlzUmVxdWlyZWRcbn07XG5cbmV4cG9ydCBkZWZhdWx0IE1lc3NhZ2VzO1xuIiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IFJlYWN0RE9NIGZyb20gXCJyZWFjdC1kb21cIjtcbmltcG9ydCBjeCBmcm9tIFwiY2xhc3NuYW1lc1wiO1xuXG5jbGFzcyBTZWxlY3RGaWVsZCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIGlzT3BlbjogZmFsc2VcbiAgICB9O1xuICAgIHRoaXMuZG9jdW1lbnRDbGlja0xpc3RlbmVyID0gdGhpcy5oYW5kbGVEb2N1bWVudENsaWNrLmJpbmQodGhpcyk7XG4gIH1cblxuICBjb21wb25lbnREaWRNb3VudCgpIHtcbiAgICBkb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiY2xpY2tcIiwgdGhpcy5kb2N1bWVudENsaWNrTGlzdGVuZXIsIGZhbHNlKTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50KCkge1xuICAgIGRvY3VtZW50LnJlbW92ZUV2ZW50TGlzdGVuZXIoXCJjbGlja1wiLCB0aGlzLmRvY3VtZW50Q2xpY2tMaXN0ZW5lciwgZmFsc2UpO1xuICB9XG5cbiAgdG9nZ2xlU2VsZWN0KCkge1xuICAgIGlmKHRoaXMuc3RhdGUuaXNPcGVuKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtpc09wZW46IGZhbHNlfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe2lzT3BlbjogdHJ1ZX0pO1xuICAgIH1cbiAgfVxuXG4gIGhhbmRsZURvY3VtZW50Q2xpY2soZXYpIHtcbiAgICBjb25zdCB7IGlzT3BlbiB9ID0gdGhpcy5zdGF0ZTtcbiAgICBpZiAoaXNPcGVuICYmICFSZWFjdERPTS5maW5kRE9NTm9kZSh0aGlzKS5jb250YWlucyhldi50YXJnZXQpKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtcbiAgICAgICAgaXNPcGVuOiBmYWxzZVxuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgcmVuZGVyKCkge1xuICAgIGNvbnN0IHsgb25DaGFuZ2UsIG9uQ2xlYXIsIHZhbHVlLCBidG5DbGFzcywgbm9DbGVhciB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHNlbGVjdGVkT3B0aW9uID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheSh0aGlzLnByb3BzLmNoaWxkcmVuKS5maWx0ZXIoKG9wdCkgPT4gb3B0LnByb3BzLnZhbHVlID09PSB2YWx1ZSk7XG4gICAgY29uc3QgcGxhY2Vob2xkZXIgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudHlwZSA9PT0gXCJwbGFjZWhvbGRlclwiKTtcbiAgICBjb25zdCBvdGhlck9wdGlvbnMgPSBSZWFjdC5DaGlsZHJlbi50b0FycmF5KHRoaXMucHJvcHMuY2hpbGRyZW4pLmZpbHRlcigob3B0KSA9PiBvcHQucHJvcHMudmFsdWUgJiYgb3B0LnByb3BzLnZhbHVlICE9PSB2YWx1ZSk7XG5cbiAgICByZXR1cm4gKFxuXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17Y3goXCJkcm9wZG93blwiLCB7b3BlbjogdGhpcy5zdGF0ZS5pc09wZW59KX0+XG4gICAgICAgIDxidXR0b24gY2xhc3NOYW1lPXtjeChcImJ0blwiLCBcImRyb3Bkb3duLXRvZ2dsZVwiLCBidG5DbGFzcyB8fCBcImJ0bi1ibGFua1wiKX0gb25DbGljaz17dGhpcy50b2dnbGVTZWxlY3QuYmluZCh0aGlzKX0+XG4gICAgICAgICAge3NlbGVjdGVkT3B0aW9uLmxlbmd0aCA/IHNlbGVjdGVkT3B0aW9uIDogcGxhY2Vob2xkZXJ9IDxzcGFuIGNsYXNzTmFtZT1cImNhcmV0XCIgLz5cbiAgICAgICAgPC9idXR0b24+XG5cbiAgICAgICAgPHVsIGNsYXNzTmFtZT1cImRyb3Bkb3duLW1lbnVcIj5cbiAgICAgICAgICB7IHZhbHVlICYmICFub0NsZWFyID8gKFxuICAgICAgICAgICAgPGxpPlxuICAgICAgICAgICAgICA8YSBvbkNsaWNrPXsoKSA9PiB7IG9uQ2xlYXIoKTsgdGhpcy50b2dnbGVTZWxlY3QoKTt9fT5cbiAgICAgICAgICAgICAgICAtIGNsZWFyIC1cbiAgICAgICAgICAgICAgPC9hPlxuICAgICAgICAgICAgPC9saT5cbiAgICAgICAgICApIDogbnVsbH1cbiAgICAgICAgICB7b3RoZXJPcHRpb25zLm1hcCgob3B0aW9uLCBpKSA9PiAoXG4gICAgICAgICAgICA8bGkga2V5PXtpfT5cbiAgICAgICAgICAgICAgPGEgc3R5bGU9e3tjdXJzb3I6IFwicG9pbnRlclwifX0gb25DbGljaz17KCkgPT4geyBvbkNoYW5nZShvcHRpb24ucHJvcHMudmFsdWUpOyB0aGlzLnRvZ2dsZVNlbGVjdCgpOyB9fT57b3B0aW9ufTwvYT5cbiAgICAgICAgICAgIDwvbGk+XG4gICAgICAgICAgKSl9XG4gICAgICAgIDwvdWw+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59XG5cblNlbGVjdEZpZWxkLnByb3BUeXBlcyA9IHtcbiAgb25DaGFuZ2U6IFJlYWN0LlByb3BUeXBlcy5mdW5jLFxuICBvbkNsZWFyOiBSZWFjdC5Qcm9wVHlwZXMuZnVuYyxcbiAgdmFsdWU6IFJlYWN0LlByb3BUeXBlcy5hbnksXG4gIGJ0bkNsYXNzOiBSZWFjdC5Qcm9wVHlwZXMuc3RyaW5nLFxuICBub0NsZWFyOiBSZWFjdC5Qcm9wVHlwZXMuYm9vbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgU2VsZWN0RmllbGQ7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5cbmZ1bmN0aW9uIEZvb3Rlcihwcm9wcykge1xuICBjb25zdCBoaUxvZ28gPSAoXG4gICAgPGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMSBjb2wtbWQtMVwiPlxuICAgICAgPGltZyBjbGFzc05hbWU9XCJoaS1sb2dvXCIgc3JjPVwiaW1hZ2VzL2xvZ28taHV5Z2Vucy1pbmcuc3ZnXCIgLz5cbiAgICA8L2Rpdj5cbiAgKTtcblxuICBjb25zdCBjbGFyaWFoTG9nbyA9IChcbiAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1zbS0xIGNvbC1tZC0xXCI+XG4gICAgICA8aW1nIGNsYXNzTmFtZT1cImxvZ29cIiBzcmM9XCJpbWFnZXMvbG9nby1jbGFyaWFoLnN2Z1wiIC8+XG4gICAgPC9kaXY+XG4gICk7XG5cbiAgY29uc3QgZm9vdGVyQm9keSA9IFJlYWN0LkNoaWxkcmVuLmNvdW50KHByb3BzLmNoaWxkcmVuKSA+IDAgP1xuICAgIFJlYWN0LkNoaWxkcmVuLm1hcChwcm9wcy5jaGlsZHJlbiwgKGNoaWxkLCBpKSA9PiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIndoaXRlLWJhclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbnRhaW5lclwiPlxuICAgICAgICAgIHtpID09PSBSZWFjdC5DaGlsZHJlbi5jb3VudChwcm9wcy5jaGlsZHJlbikgLSAxXG4gICAgICAgICAgICA/ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPntoaUxvZ299PGRpdiBjbGFzc05hbWU9XCJjb2wtc20tMTAgY29sLW1kLTEwIHRleHQtY2VudGVyXCI+e2NoaWxkfTwvZGl2PntjbGFyaWFoTG9nb308L2Rpdj4pXG4gICAgICAgICAgICA6ICg8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPntjaGlsZH08L2Rpdj4pXG4gICAgICAgICAgfVxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICkpIDogKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJ3aGl0ZS1iYXJcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJjb250YWluZXJcIj5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInJvd1wiPlxuICAgICAgICAgICAge2hpTG9nb31cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29sLXNtLTEwIGNvbC1tZC0xMCB0ZXh0LWNlbnRlclwiPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICB7Y2xhcmlhaExvZ299XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcblxuXG4gIHJldHVybiAoXG4gICAgPGZvb3RlciBjbGFzc05hbWU9XCJmb290ZXJcIj5cbiAgICAgIHtmb290ZXJCb2R5fVxuICAgIDwvZm9vdGVyPlxuICApXG59XG5cbmV4cG9ydCBkZWZhdWx0IEZvb3RlcjsiLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgY3ggZnJvbSBcImNsYXNzbmFtZXNcIjtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24ocHJvcHMpIHtcbiAgY29uc3QgeyBkaXNtaXNzaWJsZSwgYWxlcnRMZXZlbCwgb25DbG9zZU1lc3NhZ2V9ID0gcHJvcHM7XG4gIGNvbnN0IGRpc21pc3NCdXR0b24gPSBkaXNtaXNzaWJsZVxuICAgID8gPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3NOYW1lPVwiY2xvc2VcIiBvbkNsaWNrPXtvbkNsb3NlTWVzc2FnZX0+PHNwYW4+JnRpbWVzOzwvc3Bhbj48L2J1dHRvbj5cbiAgICA6IG51bGw7XG5cbiAgcmV0dXJuIChcbiAgICA8ZGl2IGNsYXNzTmFtZT17Y3goXCJhbGVydFwiLCBgYWxlcnQtJHthbGVydExldmVsfWAsIHtcImFsZXJ0LWRpc21pc3NpYmxlXCI6IGRpc21pc3NpYmxlfSl9IHJvbGU9XCJhbGVydFwiPlxuICAgICAge2Rpc21pc3NCdXR0b259XG4gICAgICB7cHJvcHMuY2hpbGRyZW59XG4gICAgPC9kaXY+XG4gIClcbn07IiwiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBGb290ZXIgZnJvbSBcIi4vZm9vdGVyXCI7XG5cbmNvbnN0IEZPT1RFUl9IRUlHSFQgPSA4MTtcblxuZnVuY3Rpb24gUGFnZShwcm9wcykge1xuICBjb25zdCBmb290ZXJzID0gUmVhY3QuQ2hpbGRyZW4udG9BcnJheShwcm9wcy5jaGlsZHJlbikuZmlsdGVyKChjaGlsZCkgPT4gY2hpbGQucHJvcHMudHlwZSA9PT0gXCJmb290ZXItYm9keVwiKTtcblxuICByZXR1cm4gKFxuICAgIDxkaXYgY2xhc3NOYW1lPVwicGFnZVwiPlxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJiYXNpYy1tYXJnaW4gaGktR3JlZW4gY29udGFpbmVyLWZsdWlkXCI+XG4gICAgICAgIDxuYXYgY2xhc3NOYW1lPVwibmF2YmFyIFwiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiY29udGFpbmVyXCI+XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cIm5hdmJhci1oZWFkZXJcIj4gPGEgY2xhc3NOYW1lPVwibmF2YmFyLWJyYW5kXCIgaHJlZj1cIiNcIj48aW1nIHNyYz1cImltYWdlcy9sb2dvLXRpbWJ1Y3Rvby5zdmdcIiBjbGFzc05hbWU9XCJsb2dvXCIgYWx0PVwidGltYnVjdG9vXCIvPjwvYT4gPC9kaXY+XG4gICAgICAgICAgICA8ZGl2IGlkPVwibmF2YmFyXCIgY2xhc3NOYW1lPVwibmF2YmFyLWNvbGxhcHNlIGNvbGxhcHNlXCI+XG4gICAgICAgICAgICAgIDx1bCBjbGFzc05hbWU9XCJuYXYgbmF2YmFyLW5hdiBuYXZiYXItcmlnaHRcIj5cbiAgICAgICAgICAgICAgICB7cHJvcHMudXNlcm5hbWUgPyA8bGk+PGEgaHJlZj17cHJvcHMudXNlcmxvY2F0aW9uIHx8ICcjJ30+PHNwYW4gY2xhc3NOYW1lPVwiZ2x5cGhpY29uIGdseXBoaWNvbi11c2VyXCIvPiB7cHJvcHMudXNlcm5hbWV9PC9hPjwvbGk+IDogbnVsbH1cbiAgICAgICAgICAgICAgPC91bD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L25hdj5cbiAgICAgIDwvZGl2PlxuICAgICAgPGRpdiAgc3R5bGU9e3ttYXJnaW5Cb3R0b206IGAke0ZPT1RFUl9IRUlHSFQgKiBmb290ZXJzLmxlbmd0aH1weGB9fT5cbiAgICAgICAge1JlYWN0LkNoaWxkcmVuLnRvQXJyYXkocHJvcHMuY2hpbGRyZW4pLmZpbHRlcigoY2hpbGQpID0+IGNoaWxkLnByb3BzLnR5cGUgIT09IFwiZm9vdGVyLWJvZHlcIil9XG4gICAgICA8L2Rpdj5cbiAgICAgIDxGb290ZXI+XG4gICAgICAgIHtmb290ZXJzfVxuICAgICAgPC9Gb290ZXI+XG4gICAgPC9kaXY+XG4gICk7XG59XG5cbmV4cG9ydCBkZWZhdWx0IFBhZ2U7XG4iLCJpbXBvcnQgUmVhY3QgZnJvbSBcInJlYWN0XCI7XG5pbXBvcnQgUmVhY3RET00gZnJvbSBcInJlYWN0LWRvbVwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuL3N0b3JlXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9hY3Rpb25zXCI7XG5pbXBvcnQge3NldFZyZX0gZnJvbSBcIi4vYWN0aW9ucy92cmVcIjtcbmltcG9ydCBBcHAgZnJvbSBcIi4vY29tcG9uZW50cy9lZGl0LWd1aS9lZGl0LWd1aVwiO1xuaW1wb3J0IGdldEF1dG9jb21wbGV0ZVZhbHVlcyBmcm9tIFwiLi9hY3Rpb25zL2F1dG9jb21wbGV0ZVwiO1xuXG5pbXBvcnQgcm91dGVyIGZyb20gXCIuL3JvdXRlclwiO1xuXG5jb25zdCBzZXRVc2VyID0gKHJlc3BvbnNlKSA9PiB7XG5cdHJldHVybiB7XG5cdFx0dHlwZTogXCJTRVRfVVNFUlwiLFxuXHRcdHVzZXI6IHJlc3BvbnNlXG5cdH07XG59O1xuXG5kb2N1bWVudC5hZGRFdmVudExpc3RlbmVyKFwiRE9NQ29udGVudExvYWRlZFwiLCAoKSA9PiB7XG5cblx0ZnVuY3Rpb24gaW5pdFJvdXRlcigpIHtcblx0XHRSZWFjdERPTS5yZW5kZXIocm91dGVyLCBkb2N1bWVudC5nZXRFbGVtZW50QnlJZChcImFwcFwiKSk7XG5cdH1cblxuXHRmdW5jdGlvbiBnZXRWcmVJZCgpIHtcblx0XHRsZXQgcGF0aCA9IHdpbmRvdy5sb2NhdGlvbi5zZWFyY2guc3Vic3RyKDEpO1xuXHRcdGxldCBwYXJhbXMgPSBwYXRoLnNwbGl0KFwiJlwiKTtcblxuXHRcdGZvcihsZXQgaSBpbiBwYXJhbXMpIHtcblx0XHRcdGxldCBba2V5LCB2YWx1ZV0gPSBwYXJhbXNbaV0uc3BsaXQoXCI9XCIpO1xuXHRcdFx0aWYoa2V5ID09PSBcInZyZUlkXCIpIHtcblx0XHRcdFx0cmV0dXJuIHZhbHVlO1xuXHRcdFx0fVxuXHRcdH1cblx0XHRyZXR1cm4gXCJXb21lbldyaXRlcnNcIjtcblx0fVxuXG5cdGZ1bmN0aW9uIGdldExvZ2luKCkge1xuXHRcdGxldCBwYXRoID0gd2luZG93LmxvY2F0aW9uLnNlYXJjaC5zdWJzdHIoMSk7XG5cdFx0bGV0IHBhcmFtcyA9IHBhdGguc3BsaXQoXCImXCIpO1xuXG5cdFx0Zm9yKGxldCBpIGluIHBhcmFtcykge1xuXHRcdFx0bGV0IFtrZXksIHZhbHVlXSA9IHBhcmFtc1tpXS5zcGxpdChcIj1cIik7XG5cdFx0XHRpZihrZXkgPT09IFwiaHNpZFwiKSB7XG5cdFx0XHRcdHJldHVybiB7dXNlcjogdmFsdWUsIHRva2VuOiB2YWx1ZX07XG5cdFx0XHR9XG5cdFx0fVxuXHRcdHJldHVybiB1bmRlZmluZWQ7XG5cdH1cblx0c3RvcmUuZGlzcGF0Y2goc2V0VnJlKGdldFZyZUlkKCksIGluaXRSb3V0ZXIpKTtcblx0c3RvcmUuZGlzcGF0Y2goc2V0VXNlcihnZXRMb2dpbigpKSk7XG59KTsiLCJpbXBvcnQgc2V0SW4gZnJvbSBcIi4uL3V0aWwvc2V0LWluXCI7XG5cbmxldCBpbml0aWFsU3RhdGUgPSB7XG5cdGRhdGE6IHtcblx0XHRcIkByZWxhdGlvbnNcIjogW11cblx0fSxcblx0ZG9tYWluOiBudWxsLFxuXHRlcnJvck1lc3NhZ2U6IG51bGwsXG5cdHBlbmRpbmc6IGZhbHNlXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cblx0XHRjYXNlIFwiQkVGT1JFX0ZFVENIX0VOVElUWVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiB7XG5cdFx0XHRcdFx0XCJAcmVsYXRpb25zXCI6IFtdXG5cdFx0XHRcdH0sXG5cdFx0XHRcdHBlbmRpbmc6IHRydWVcblx0XHRcdH19O1xuXHRcdGNhc2UgXCJSRUNFSVZFX0VOVElUWVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiBhY3Rpb24uZGF0YSxcblx0XHRcdFx0ZG9tYWluOiBhY3Rpb24uZG9tYWluLFxuXHRcdFx0XHRlcnJvck1lc3NhZ2U6IGFjdGlvbi5lcnJvck1lc3NhZ2UgfHwgbnVsbCxcblx0XHRcdFx0cGVuZGluZzogZmFsc2Vcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlNFVF9FTlRJVFlfRklFTERfVkFMVUVcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0ZGF0YTogc2V0SW4oYWN0aW9uLmZpZWxkUGF0aCwgYWN0aW9uLnZhbHVlLCBzdGF0ZS5kYXRhKVxuXHRcdFx0fX07XG5cblx0XHRjYXNlIFwiUkVDRUlWRV9FTlRJVFlfRkFJTFVSRVwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRkYXRhOiB7XG5cdFx0XHRcdFx0XCJAcmVsYXRpb25zXCI6IFtdXG5cdFx0XHRcdH0sXG5cdFx0XHRcdGVycm9yTWVzc2FnZTogYWN0aW9uLmVycm9yTWVzc2FnZSxcblx0XHRcdFx0cGVuZGluZzogZmFsc2Vcblx0XHRcdH19O1xuXG5cdFx0Y2FzZSBcIlNFVF9WUkVcIjoge1xuXHRcdFx0cmV0dXJuIGluaXRpYWxTdGF0ZTtcblx0XHR9XG5cblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn0iLCJpbXBvcnQge2NvbWJpbmVSZWR1Y2Vyc30gZnJvbSBcInJlZHV4XCI7XG5cbmltcG9ydCBlbnRpdHkgZnJvbSBcIi4vZW50aXR5XCI7XG5pbXBvcnQgbWVzc2FnZXMgZnJvbSBcIi4vbWVzc2FnZXNcIjtcbmltcG9ydCB1c2VyIGZyb20gXCIuL3VzZXJcIjtcbmltcG9ydCB2cmUgZnJvbSBcIi4vdnJlXCI7XG5pbXBvcnQgcXVpY2tTZWFyY2ggZnJvbSBcIi4vcXVpY2stc2VhcmNoXCI7XG5cbmV4cG9ydCBkZWZhdWx0IGNvbWJpbmVSZWR1Y2Vycyh7XG5cdHZyZTogdnJlLFxuXHRlbnRpdHk6IGVudGl0eSxcblx0dXNlcjogdXNlcixcblx0bWVzc2FnZXM6IG1lc3NhZ2VzLFxuXHRxdWlja1NlYXJjaDogcXVpY2tTZWFyY2hcbn0pOyIsImltcG9ydCBzZXRJbiBmcm9tIFwiLi4vdXRpbC9zZXQtaW5cIjtcblxuY29uc3QgaW5pdGlhbFN0YXRlID0ge1xuXHRsb2c6IFtdXG59O1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlJFUVVFU1RfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJTVUNDRVNTX01FU1NBR0VcIjpcblx0XHRcdHN0YXRlLmxvZy5wdXNoKHttZXNzYWdlOiBhY3Rpb24ubWVzc2FnZSwgdHlwZTogYWN0aW9uLnR5cGUsIHRpbWU6IG5ldyBEYXRlKCl9KTtcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0XHRjYXNlIFwiRVJST1JfTUVTU0FHRVwiOlxuXHRcdFx0c3RhdGUubG9nLnB1c2goe21lc3NhZ2U6IGFjdGlvbi5tZXNzYWdlLCB0eXBlOiBhY3Rpb24udHlwZSwgdGltZTogbmV3IERhdGUoKX0pO1xuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdGNhc2UgXCJESVNNSVNTX01FU1NBR0VcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRsb2c6IHNldEluKFthY3Rpb24ubWVzc2FnZUluZGV4LCBcImRpc21pc3NlZFwiXSwgdHJ1ZSwgc3RhdGUubG9nKVxuXHRcdFx0fTtcblx0fVxuXG5cdHJldHVybiBzdGF0ZTtcbn0iLCJsZXQgaW5pdGlhbFN0YXRlID0ge1xuXHRzdGFydDogMCxcblx0bGlzdDogW10sXG5cdHJvd3M6IDUwLFxuXHRxdWVyeTogXCJcIlxufTtcblxuZXhwb3J0IGRlZmF1bHQgZnVuY3Rpb24oc3RhdGU9aW5pdGlhbFN0YXRlLCBhY3Rpb24pIHtcblx0c3dpdGNoIChhY3Rpb24udHlwZSkge1xuXHRcdGNhc2UgXCJTRVRfUEFHSU5BVElPTl9TVEFSVFwiOlxuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgc3RhcnQ6IGFjdGlvbi5zdGFydH07XG5cdFx0Y2FzZSBcIlJFQ0VJVkVfRU5USVRZX0xJU1RcIjpcblx0XHRcdHJldHVybiB7Li4uc3RhdGUsIC4uLntcblx0XHRcdFx0bGlzdDogYWN0aW9uLmRhdGFcblx0XHRcdH19O1xuXHRcdGNhc2UgXCJTRVRfUVVJQ0tTRUFSQ0hfUVVFUllcIjoge1xuXHRcdFx0cmV0dXJuIHsuLi5zdGF0ZSwgLi4ue1xuXHRcdFx0XHRxdWVyeTogYWN0aW9uLnZhbHVlXG5cdFx0XHR9fTtcblx0XHR9XG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImxldCBpbml0aWFsU3RhdGUgPSBudWxsO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbihzdGF0ZT1pbml0aWFsU3RhdGUsIGFjdGlvbikge1xuXHRzd2l0Y2ggKGFjdGlvbi50eXBlKSB7XG5cdFx0Y2FzZSBcIlNFVF9VU0VSXCI6XG5cdFx0XHRpZiAoYWN0aW9uLnVzZXIpIHtcblx0XHRcdFx0cmV0dXJuIGFjdGlvbi51c2VyO1xuXHRcdFx0fSBlbHNlIHtcblx0XHRcdFx0cmV0dXJuIHN0YXRlO1xuXHRcdFx0fVxuXHRcdFx0YnJlYWs7XG5cdFx0ZGVmYXVsdDpcblx0XHRcdHJldHVybiBzdGF0ZTtcblx0fVxufSIsImxldCBpbml0aWFsU3RhdGUgPSB7XG5cdHZyZUlkOiBudWxsLFxuXHRsaXN0OiBbXSxcblx0Y29sbGVjdGlvbnM6IHt9LFxuXHRkb21haW46IG51bGxcbn07XG5cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uKHN0YXRlPWluaXRpYWxTdGF0ZSwgYWN0aW9uKSB7XG5cdHN3aXRjaCAoYWN0aW9uLnR5cGUpIHtcblx0XHRjYXNlIFwiU0VUX1ZSRVwiOlxuXHRcdFx0cmV0dXJuIHtcblx0XHRcdFx0Li4uc3RhdGUsXG5cdFx0XHRcdHZyZUlkOiBhY3Rpb24udnJlSWQsXG5cdFx0XHRcdGNvbGxlY3Rpb25zOiBhY3Rpb24uY29sbGVjdGlvbnMgfHwgbnVsbCxcblx0XHRcdFx0bGlzdDogYWN0aW9uLmxpc3QgfHwgc3RhdGUubGlzdFxuXHRcdFx0fTtcblxuXHRcdGNhc2UgXCJMSVNUX1ZSRVNcIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRsaXN0OiBhY3Rpb24ubGlzdCxcblx0XHRcdFx0Y29sbGVjdGlvbnM6IG51bGxcblx0XHRcdH07XG5cdFx0Y2FzZSBcIlNFVF9ET01BSU5cIjpcblx0XHRcdHJldHVybiB7XG5cdFx0XHRcdC4uLnN0YXRlLFxuXHRcdFx0XHRkb21haW46IGFjdGlvbi5kb21haW5cblx0XHRcdH07XG5cblx0XHRkZWZhdWx0OlxuXHRcdFx0cmV0dXJuIHN0YXRlO1xuXHR9XG59IiwiaW1wb3J0IFJlYWN0IGZyb20gXCJyZWFjdFwiO1xuaW1wb3J0IHtSb3V0ZXIsIFJlZGlyZWN0LCBSb3V0ZSwgaGFzaEhpc3Rvcnl9IGZyb20gXCJyZWFjdC1yb3V0ZXJcIjtcbmltcG9ydCB7UHJvdmlkZXIsIGNvbm5lY3R9IGZyb20gXCJyZWFjdC1yZWR1eFwiO1xuaW1wb3J0IHN0b3JlIGZyb20gXCIuL3N0b3JlXCI7XG5pbXBvcnQgZ2V0QXV0b2NvbXBsZXRlVmFsdWVzIGZyb20gXCIuL2FjdGlvbnMvYXV0b2NvbXBsZXRlXCI7XG5pbXBvcnQgYWN0aW9ucyBmcm9tIFwiLi9hY3Rpb25zXCI7XG5cbmltcG9ydCBFZGl0R3VpIGZyb20gXCIuL2NvbXBvbmVudHMvZWRpdC1ndWkvZWRpdC1ndWlcIjtcbmltcG9ydCB7dXJsc30gZnJvbSBcIi4vdXJsc1wiO1xuXG5cblxuXG5leHBvcnQgZnVuY3Rpb24gbmF2aWdhdGVUbyhrZXksIGFyZ3MpIHtcblx0aGFzaEhpc3RvcnkucHVzaCh1cmxzW2tleV0uYXBwbHkobnVsbCwgYXJncykpO1xufVxuXG5jb25zdCBkZWZhdWx0Q29ubmVjdCA9IGNvbm5lY3QoXG5cdHN0YXRlID0+ICh7Li4uc3RhdGUsIGdldEF1dG9jb21wbGV0ZVZhbHVlczogZ2V0QXV0b2NvbXBsZXRlVmFsdWVzfSksXG5cdGRpc3BhdGNoID0+IGFjdGlvbnMobmF2aWdhdGVUbywgZGlzcGF0Y2gpXG4pO1xuXG5cbmV4cG9ydCBkZWZhdWx0IChcblx0PFByb3ZpZGVyIHN0b3JlPXtzdG9yZX0+XG5cdFx0PFJvdXRlciBoaXN0b3J5PXtoYXNoSGlzdG9yeX0+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5yb290KCl9IGNvbXBvbmVudHM9e2RlZmF1bHRDb25uZWN0KEVkaXRHdWkpfSAvPlxuXHRcdFx0PFJvdXRlIHBhdGg9e3VybHMuZmlyc3RFbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5uZXdFbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0XHQ8Um91dGUgcGF0aD17dXJscy5lbnRpdHkoKX0gY29tcG9uZW50cz17ZGVmYXVsdENvbm5lY3QoRWRpdEd1aSl9IC8+XG5cdFx0PC9Sb3V0ZXI+XG5cdDwvUHJvdmlkZXI+XG4pO1xuXG4iLCJpbXBvcnQge2NyZWF0ZVN0b3JlLCBhcHBseU1pZGRsZXdhcmV9IGZyb20gXCJyZWR1eFwiO1xuaW1wb3J0IHRodW5rTWlkZGxld2FyZSBmcm9tIFwicmVkdXgtdGh1bmtcIjtcblxuaW1wb3J0IHJlZHVjZXJzIGZyb20gXCIuLi9yZWR1Y2Vyc1wiO1xuXG5jb25zdCBsb2dnZXIgPSAoKSA9PiBuZXh0ID0+IGFjdGlvbiA9PiB7XG4gIGlmIChhY3Rpb24uaGFzT3duUHJvcGVydHkoXCJ0eXBlXCIpKSB7XG4gICAgY29uc29sZS5sb2coXCJbUkVEVVhdXCIsIGFjdGlvbi50eXBlLCBhY3Rpb24pO1xuICB9XG5cbiAgcmV0dXJuIG5leHQoYWN0aW9uKTtcbn07XG5cbmxldCBjcmVhdGVTdG9yZVdpdGhNaWRkbGV3YXJlID0gYXBwbHlNaWRkbGV3YXJlKC8qbG9nZ2VyLCovIHRodW5rTWlkZGxld2FyZSkoY3JlYXRlU3RvcmUpO1xuZXhwb3J0IGRlZmF1bHQgY3JlYXRlU3RvcmVXaXRoTWlkZGxld2FyZShyZWR1Y2Vycyk7XG4iLCJjb25zdCB1cmxzID0ge1xuXHRyb290KCkge1xuXHRcdHJldHVybiBcIi9cIjtcblx0fSxcblx0bmV3RW50aXR5KGNvbGxlY3Rpb24pIHtcblx0XHRyZXR1cm4gY29sbGVjdGlvblxuXHRcdFx0PyBgLyR7Y29sbGVjdGlvbn0vbmV3YFxuXHRcdFx0OiBcIi86Y29sbGVjdGlvbi9uZXdcIjtcblx0fSxcblx0Zmlyc3RFbnRpdHkoY29sbGVjdGlvbikge1xuXHRcdHJldHVybiBjb2xsZWN0aW9uXG5cdFx0XHQ/IGAvJHtjb2xsZWN0aW9ufS9maXJzdGBcblx0XHRcdDogXCIvOmNvbGxlY3Rpb24vZmlyc3RcIjtcblx0fSxcblx0ZW50aXR5KGNvbGxlY3Rpb24sIGlkKSB7XG5cdFx0cmV0dXJuIGNvbGxlY3Rpb24gJiYgaWRcblx0XHRcdD8gYC8ke2NvbGxlY3Rpb259LyR7aWR9YFxuXHRcdFx0OiBcIi86Y29sbGVjdGlvbi86aWRcIjtcblx0fVxufTtcblxuZXhwb3J0IHsgdXJscyB9IiwiZnVuY3Rpb24gZGVlcENsb25lOShvYmopIHtcbiAgICB2YXIgaSwgbGVuLCByZXQ7XG5cbiAgICBpZiAodHlwZW9mIG9iaiAhPT0gXCJvYmplY3RcIiB8fCBvYmogPT09IG51bGwpIHtcbiAgICAgICAgcmV0dXJuIG9iajtcbiAgICB9XG5cbiAgICBpZiAoQXJyYXkuaXNBcnJheShvYmopKSB7XG4gICAgICAgIHJldCA9IFtdO1xuICAgICAgICBsZW4gPSBvYmoubGVuZ3RoO1xuICAgICAgICBmb3IgKGkgPSAwOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgICAgIHJldC5wdXNoKCAodHlwZW9mIG9ialtpXSA9PT0gXCJvYmplY3RcIiAmJiBvYmpbaV0gIT09IG51bGwpID8gZGVlcENsb25lOShvYmpbaV0pIDogb2JqW2ldICk7XG4gICAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgICByZXQgPSB7fTtcbiAgICAgICAgZm9yIChpIGluIG9iaikge1xuICAgICAgICAgICAgaWYgKG9iai5oYXNPd25Qcm9wZXJ0eShpKSkge1xuICAgICAgICAgICAgICAgIHJldFtpXSA9ICh0eXBlb2Ygb2JqW2ldID09PSBcIm9iamVjdFwiICYmIG9ialtpXSAhPT0gbnVsbCkgPyBkZWVwQ2xvbmU5KG9ialtpXSkgOiBvYmpbaV07XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHJldDtcbn1cblxuZXhwb3J0IGRlZmF1bHQgZGVlcENsb25lOTsiLCJpbXBvcnQgY2xvbmUgZnJvbSBcIi4vY2xvbmUtZGVlcFwiO1xuXG4vLyBEbyBlaXRoZXIgb2YgdGhlc2U6XG4vLyAgYSkgU2V0IGEgdmFsdWUgYnkgcmVmZXJlbmNlIGlmIGRlcmVmIGlzIG5vdCBudWxsXG4vLyAgYikgU2V0IGEgdmFsdWUgZGlyZWN0bHkgaW4gdG8gZGF0YSBvYmplY3QgaWYgZGVyZWYgaXMgbnVsbFxuY29uc3Qgc2V0RWl0aGVyID0gKGRhdGEsIGRlcmVmLCBrZXksIHZhbCkgPT4ge1xuXHQoZGVyZWYgfHwgZGF0YSlba2V5XSA9IHZhbDtcblx0cmV0dXJuIGRhdGE7XG59O1xuXG4vLyBTZXQgYSBuZXN0ZWQgdmFsdWUgaW4gZGF0YSAobm90IHVubGlrZSBpbW11dGFibGVqcywgYnV0IGEgY2xvbmUgb2YgZGF0YSBpcyBleHBlY3RlZCBmb3IgcHJvcGVyIGltbXV0YWJpbGl0eSlcbmNvbnN0IF9zZXRJbiA9IChwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPSBudWxsKSA9PlxuXHRwYXRoLmxlbmd0aCA+IDEgP1xuXHRcdF9zZXRJbihwYXRoLCB2YWx1ZSwgZGF0YSwgZGVyZWYgPyBkZXJlZltwYXRoLnNoaWZ0KCldIDogZGF0YVtwYXRoLnNoaWZ0KCldKSA6XG5cdFx0c2V0RWl0aGVyKGRhdGEsIGRlcmVmLCBwYXRoWzBdLCB2YWx1ZSk7XG5cbmNvbnN0IHNldEluID0gKHBhdGgsIHZhbHVlLCBkYXRhKSA9PlxuXHRfc2V0SW4oY2xvbmUocGF0aCksIHZhbHVlLCBjbG9uZShkYXRhKSk7XG5cbmV4cG9ydCBkZWZhdWx0IHNldEluOyJdfQ==
