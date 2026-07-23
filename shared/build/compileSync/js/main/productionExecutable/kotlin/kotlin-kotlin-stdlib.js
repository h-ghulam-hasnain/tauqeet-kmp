//region block: polyfills
(function () {
  if (typeof globalThis === 'object')
    return;
  Object.defineProperty(Object.prototype, '__magic__', {get: function () {
    return this;
  }, configurable: true});
  __magic__.globalThis = __magic__;
  delete Object.prototype.__magic__;
}());
//endregion
(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['kotlin-kotlin-stdlib'] = factory(typeof this['kotlin-kotlin-stdlib'] === 'undefined' ? {} : this['kotlin-kotlin-stdlib']);
}(globalThis, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  function defineProp(obj, name, getter, setter) {
    return Object.defineProperty(obj, name, {configurable: true, get: getter, set: setter});
  }
  function protoOf(constructor) {
    return constructor.prototype;
  }
  var VOID;
  return _;
}));

//# sourceMappingURL=kotlin-kotlin-stdlib.js.map
