(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['tauqeet-kmp:shared'] = factory(typeof this['tauqeet-kmp:shared'] === 'undefined' ? {} : this['tauqeet-kmp:shared']);
}(globalThis, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=tauqeet-kmp-shared.js.map
