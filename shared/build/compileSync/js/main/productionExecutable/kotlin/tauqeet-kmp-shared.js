(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['com.tauqeet:shared'] = factory(typeof this['com.tauqeet:shared'] === 'undefined' ? {} : this['com.tauqeet:shared']);
}(globalThis, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=tauqeet-kmp-shared.js.map
